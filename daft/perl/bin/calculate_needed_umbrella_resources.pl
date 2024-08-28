#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.37
#  Date     : 2024-04-23 18:10:19
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

use strict;
use warnings;

use File::Basename qw(dirname);
use Cwd qw(abs_path);
use lib dirname(dirname abs_path $0) . '/lib';

use ADP::Kubernetes_Operations;
use General::Data_Structure_Operations;
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Yaml_Operations;

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my @data;
my $debug = 0;
my $dot_separated_to_stderr = 0;
my %file_resources;
# Required resources according to component values.yaml file.
#   1st key: Resource Name
#   2nd key: One of the following:
#       limits
#       requests
#   3rd key: One of the following
#       cpu
#       ephemeral-storage
#       memory
my @files;
my @files_with_updated_resources = ();
my $formatting_remove_space = 1;    # Change this to 0 if you want "1 Ki" instead of "1Ki"
my $helm_executable = "helm";
my $hide_fixable_parsing_errors = 0;
my $human_formatting = 0;
my %ignore_resource;
    # Enter a list of pod.container resources that will not be counted because
    # they don't seem to be deployed for some reason maybe because they are
    # either a resource used for cleanup or of Kind=Job or Kind=CronJob that is
    # not started.
    $ignore_resource{'eric-bsf-wcdb-cd-configure-keyspaces.cassandra'} = 1;
    $ignore_resource{'eric-bsf-wcdb-cd-create-auth.cassandra'} = 1;
    $ignore_resource{'eric-bsf-wcdb-cd-tls-restarter.cassandra'} = 1;
    $ignore_resource{'eric-data-document-database-pg-hook-cleanup.hook-cleanup'} = 1;
    $ignore_resource{'eric-data-wide-column-database-cd-configure-keyspaces.cassandra'} = 1;
    $ignore_resource{'eric-data-wide-column-database-cd-create-auth.cassandra'} = 1;
    $ignore_resource{'eric-data-wide-column-database-cd-tls-restarter.cassandra'} = 1;
    $ignore_resource{'eric-sc-bragent.eric-sc-bragent'} = 1;

    $ignore_resource{'eric-sc-monitor.eric-sc-monitor'} = 1;
    $ignore_resource{'eric-sec-ldap-server-pre-upgrade-job.ldap-pre-upgrade-job'} = 1;
my $ignore_resources = 0;
my $include_job = 0;
my $input_directory = "";
my $input_file = "";
my $kubeconfig = "";
my $logfile = "";
my $max_resource_name = 0;
my @multiple_files = ();
    # If multiple helm charts should be checked the umbrella files and values files should be passed
    # using this parameter (-m or --multiple-files) where the data passed should look like this:
    # "packed_umbrella_file_1.tgz,values-file-1-1.yaml[,values-file-1-2,...etc]
    # I.e. always specify the packed umbrella file first, followed by a comma "," and then the first main values yaml file, optionally followed by one or more other values files.
    # The parameter can be specified as many times as is necessary.
my $printout_file = "";
my $rc;
my %replica_dependencies_other_resource;
    # Enter a list of pods where the replicas count depends on another pod
    $replica_dependencies_other_resource{'eric-data-search-engine-curator'} = "eric-data-search-engine-master";
my @replica_dependencies_traffic_profile = (
    # Enter a list of pods where the replicas count depends on a traffic profile.
    # NOTE: This is currently not checked by this script.
    "eric-bsf-diameter",
    "eric-bsf-wcdb-cd",
    "eric-bsf-worker",
    "eric-data-wide-column-database-cd",
    "eric-sc-slf",
    "eric-sc-spr-fe",
    "eric-scp-worker",
    "eric-sepp-worker",
    "eric-stm-diameter",
);
my @replica_dependencies_workers = (
    # Enter a list of pods where the replicas count depends on the number of worker nodes and more recently also control-plane (master) nodes.
    "eric-log-shipper",
    "eric-tm-ingress-controller-cr-envoy",
    "eric-dsc-coredump-handler",
);
my %required_container_resources;
# The final required container resources.
#   1st key: Resource Name (container)
#   2nd key: One of the following:
#       limits
#       requests
#   3rd key: One of the following
#       cpu
#       ephemeral-storage
#       memory
my %required_pod_resources;
# The final required pod resources.
#   1st key: Resource Name (pod)
#   2nd key: One of the following:
#       limits
#       requests
#       replicas
#   3rd key: One of the following when 2nd key is limits or requests.
#       cpu
#       cpu_no_replicas
#       ephemeral-storage
#       ephemeral-storage_no_replicas
#       memory
#       memory_no_replicas
my @required_pod_resources_cpu = ();
my @required_pod_resources_memory = ();
my @required_pod_resources_storage = ();
my %resource_information;
# Information about the resource.
#   1st key: Resource Name
#   2nd key: One of the following
#       kind
#       replicas
my @result;
my $script_dir = dirname abs_path $0;
my $show_help = 0;
my %top_pod_resources;
# Highest (top) resources required based on all pod values.yaml files.
#   1st key: One of the following:
#       limits
#       requests
#   2nd key: One of the following
#       cpu
#       ephemeral-storage
#       memory
$top_pod_resources{'limits'}{'cpu'} = 0;
$top_pod_resources{'limits'}{'memory'} = 0;
$top_pod_resources{'requests'}{'cpu'} = 0;
$top_pod_resources{'requests'}{'memory'} = 0;
my %total_container_resources;
# Total resources required based on all component values.yaml files.
#   1st key: One of the following:
#       limits
#       requests
#   2nd key: One of the following
#       cpu
#       ephemeral-storage
#       memory
$total_container_resources{'limits'}{'cpu'} = 0;
$total_container_resources{'limits'}{'memory'} = 0;
$total_container_resources{'requests'}{'cpu'} = 0;
$total_container_resources{'requests'}{'memory'} = 0;
my %total_pod_resources;
# Total resources required based on all pod values.yaml files.
#   1st key: One of the following:
#       limits
#       requests
#   2nd key: One of the following
#       cpu
#       ephemeral-storage
#       memory
$total_pod_resources{'limits'}{'cpu'} = 0;
$total_pod_resources{'limits'}{'memory'} = 0;
$total_pod_resources{'requests'}{'cpu'} = 0;
$total_pod_resources{'requests'}{'memory'} = 0;
my %total_required_container_resources;
# Total resources required based on all component values.yaml files.
#   1st key: One of the following:
#       limits
#       requests
#   2nd key: One of the following
#       cpu
#       ephemeral-storage
#       memory
$total_required_container_resources{'limits'}{'cpu'} = 0;
$total_required_container_resources{'limits'}{'memory'} = 0;
$total_required_container_resources{'requests'}{'cpu'} = 0;
$total_required_container_resources{'requests'}{'memory'} = 0;
my %total_required_pod_resources;
# Total resources required based on all component values.yaml files.
#   1st key: One of the following:
#       limits
#       requests
#   2nd key: One of the following
#       cpu
#       ephemeral-storage
#       memory
$total_required_pod_resources{'limits'}{'cpu'} = 0;
$total_required_pod_resources{'limits'}{'memory'} = 0;
$total_required_pod_resources{'requests'}{'cpu'} = 0;
$total_required_pod_resources{'requests'}{'memory'} = 0;
my %updated_resources;
# Updated resources from top level values.yaml file, or other files.
#   1st key: Resource Name
#   2nd key: One of the following:
#       limits
#       requests
#   3rd key: One of the following
#       cpu
#       ephemeral-storage
#       memory
my @umbrella_structure = ();
# Array of hash:
#   {
#       "umbrella-file" => "/path/to/umbrella.tgz",
#       "value-files    => @value_files,
#   }
my @value_files = ();
my $worker_cnt = 0;

# *********************************
# *                               *
# * Parse command line parameters *
# *                               *
# *********************************

use Getopt::Long;          # standard Perl module
GetOptions (
    "debug"                     => \$debug,
    "dot-separated-to-stderr"   => \$dot_separated_to_stderr,
    "d|directory=s"             => \$input_directory,
    "e|helm-executable=s"       => \$helm_executable,
    "f|file=s"                  => \$input_file,
    "h|help"                    => \$show_help,
    "hide-fixable-errors"       => \$hide_fixable_parsing_errors,
    "human-formatting"          => \$human_formatting,
    "i|ignore-resources"        => \$ignore_resources,
    "j|include-job"             => \$include_job,
    "m|multiple-files=s"        => \@multiple_files,
    "printout-from-file=s"      => \$printout_file,
    "kubeconfig=s"              => \$kubeconfig,
    "l|log-file=s"              => \$logfile,
    "v|value-file=s"            => \@value_files,
    "w|worker-count=i"          => \$worker_cnt,
);

if ($show_help) {
    usage();
    exit 0;
}

if ($kubeconfig ne "" && -f "$kubeconfig") {
    # Set kubeconfig for any used library functions
    ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);

    $kubeconfig = " --kubeconfig=$kubeconfig";
} elsif ($kubeconfig ne "") {
    print "The kubeconfig file does not exist\n";
    exit 1;
}

my $temp_cnt = 0;
$ temp_cnt++ if ($input_file ne "");
$ temp_cnt++ if ($input_directory ne "");
$ temp_cnt++ if ($printout_file ne "");
$ temp_cnt++ if (@multiple_files);
if ($temp_cnt > 1) {
    print "You can only specify one of --file, --printout-from-file, --directory or --multiple-files\n";
    exit 1;
}
if ($input_file ne "") {
    # Check that the umbrella file exists
    unless (-f "$input_file") {
        print "File '$input_file' does not exist or cannot be accessed\n";
        exit 1;
    }
    $input_file = abs_path $input_file;
} elsif ($input_directory ne "") {
    unless (-d "$input_directory") {
        print "Directory '$input_directory' does not exist or cannot be accessed\n";
        exit 1;
    }
    $input_directory = abs_path $input_directory;
} elsif ($printout_file ne "") {
    # Check that the umbrella file exists
    unless (-f "$printout_file") {
        print "File '$printout_file' does not exist or cannot be accessed\n";
        exit 1;
    }
    $printout_file = abs_path $printout_file;
} elsif (@multiple_files) {
    if (@value_files) {
        print "You cannot specify both --multiple-files and --value-file\n";
        exit 1;
    }
    for my $file_pair (@multiple_files) {
        my @files = split /,/, $file_pair;
        if (scalar @files < 2) {
            print "There must at least be a pair of files specified in the --multiple-files parameter: $file_pair\n";
            exit 1;
        }
        my $packed_umbrella_filename = shift @files;
        unless (-f "$packed_umbrella_filename") {
            print "--multiple-files parameter packed umbrella file '$packed_umbrella_filename' does not exist or cannot be accessed\n";
            exit 1;
        }
        unless ($packed_umbrella_filename =~ /^.+\.(tar\.gz|tgz)$/) {
            print "--multiple-files parameter packed umbrella file '$packed_umbrella_filename' is not a packed 'tar.gz' or 'tgz' file\n";
            exit 1;
        }
        $packed_umbrella_filename = abs_path $packed_umbrella_filename;
        my @temp_value_files = ();
        for my $filename (@files) {
            unless (-f "$filename") {
                print "--multiple-files parameter values file '$filename' does not exist or cannot be accessed\n";
                exit 1;
            }
            unless ($filename =~ /^.+\.yaml$/) {
                print "--multiple-files parameter values file '$filename' is not a 'yaml file\n";
                exit 1;
            }
            push @temp_value_files, abs_path $filename;
        }
        push @umbrella_structure, {
            "umbrella-file" => $packed_umbrella_filename,
            "value-files"   => \@temp_value_files,
        };
    }
    # use Data::Dumper;
    # print Dumper(@umbrella_structure);
} else {
    print "You must specify either a file with --file or --printout-from-file or a directory with --directory or multiple files with --multiple-files\n";
    exit 1;
}

for (@value_files) {
    if (-f "$_") {
        $_ = abs_path $_;
    } else {
        print "File '$_' does not exist or cannot be read\n";
        exit 1;
    }
}

if ($ignore_resources == 0) {
    undef %ignore_resource;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Start logging of output to file
if ($logfile) {
    General::Logging::log_enable("$logfile");
}

if ($worker_cnt == 0) {
    if (control_plane_taints_active() == 1) {
        # Taints active so only count worker nodes
        $worker_cnt = scalar ADP::Kubernetes_Operations::get_worker_nodes();
    } else {
        # Taints not active so count all nodes
        $worker_cnt = scalar ADP::Kubernetes_Operations::get_nodes();
    }
}

if ($input_file ne "") {
    # Parse the packed umbrella file

    parse_umbrella_file();
} elsif ($printout_file ne "") {
    # Parse the output from the helm command stored in a local file

    parse_template_printout_file();
} elsif (@umbrella_structure) {
    # Parse multiple packed umbrella files

    parse_multiple_umbrella_files();
} else {
    # Parse the unpacked umbrella directory

    find_values_yaml_files();

    parse_values_yaml_files();

    if (@value_files) {
        parse_extra_values_yaml_files();
    }
}

print_common_data();

print_component_file_data();

print_updated_resources_data();

print_required_resources_data();

# Stop logging of output to file
if ($logfile) {
    General::Logging::log_disable();
}

exit 0;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
#
# Check if taints are enabled on control-plane nodes.
#
sub control_plane_taints_active {
    my $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl describe node $kubeconfig | grep -P '^Taints:\\s+node-role.kubernetes.io/(control-plane|master):NoSchedule'",
            "hide-output"   => 1,
        }
    );
    if ($rc == 0) {
        # Taints found, so we shall not use control plane nodes in calculations
        return 1;
    } elsif ($rc == 1) {
        # No taints found, so we shall use control plane nodes in calculations
        return 0;
    } else {
        # Some error detected, just assume that we should not use control plane nodes in calculation
        return 1;
    }
}

# -----------------------------------------------------------------------------
#
# Convert values.yaml data from yaml format into dot-separated data on one line.
#
sub convert_values_yaml_data {
    my $yaml_file = shift;
    my @parsed_data = ();
    my %yaml_data;

    # Parse the YAML data in the file into Perl hash structure
    if (General::Yaml_Operations::read_file({ "filename" => $yaml_file, "output-ref" => \%yaml_data }) != 0) {
        General::Logging::log_user_error_message("Failed to read and parse yaml data in $yaml_file file");
        return ();
    }

    # Convert the Perl hash structure into dot separated output
    General::Data_Structure_Operations::dump_dot_separated_hash_ref_to_variable(\%yaml_data, "", \@parsed_data);
    if (scalar @parsed_data == 0) {
        General::Logging::log_user_error_message("Failed to convert yaml data in $yaml_file file");
    }

    return @parsed_data;
}

# -----------------------------------------------------------------------------
#
# Find all values.yaml files in the specified directory.
#
sub find_values_yaml_files {
    @files = General::File_Operations::find_file(
        {
            "directory"     => $input_directory,
            "filename"      => "values.yaml",
            "maxdepth"      => 3,
        }
    );
}

# -----------------------------------------------------------------------------
#
# Format the output into a more readable human format. e.g. 1024 will be 1 Ki.
#
sub format_output {
    my $number = shift;

    if ($human_formatting) {
        if ($formatting_remove_space) {
            $number = ADP::Kubernetes_Operations::convert_number_to_3_decimals($number);
            $number =~ s/\s+//g;
            return $number;
        } else {
            return ADP::Kubernetes_Operations::convert_number_to_3_decimals($number);
        }
    } else {
        return $number;
    }
}

# -----------------------------------------------------------------------------
#
# Parse any other specified 'values.yaml' files.
#
sub parse_extra_values_yaml_files {
    my @data;
    my $resource_name;
    my $value;

    for my $filename (@value_files) {
        print "\nDBG:filename=$filename\n" if $debug;
        push @files_with_updated_resources, $filename;
        @data = convert_values_yaml_data($filename);

        for (@data) {
            if (/^(\S+)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
                $resource_name = "$1";
                $max_resource_name = length($resource_name) if ($max_resource_name < length($resource_name));
                $value = ADP::Kubernetes_Operations::convert_number_unit($4);
                printf "DBG:1=$1,\t2=$2,\t3=$3,\t4=$4, converted 4=%s\n", $value if $debug;
                $required_container_resources{$resource_name}{$2}{$3} = $value;
                $updated_resources{$resource_name}{$2}{$3} = $value;
                print "DBG:Value \$file_resources{$resource_name}{$2}{$3} changed from $file_resources{$resource_name}{$2}{$3} to $required_container_resources{$resource_name}{$2}{$3}\n" if $debug;
            }
        }
    }
}

# -----------------------------------------------------------------------------
#
# Parse the packed umbrella file.
#
sub parse_section_data {
    my $kind = shift;
    my $name = shift;
    my $arrayref = shift;

    my $container_name = "UNKNOWN";
    my @data = ();
    my $error_details_printed = 0;
    my $fix_attempted = 0;
    my $previous_line_with_error = 0;
    my $replicas = "-";
    my $resource_name;
    my $value;
    my %yaml_data;

    # Remove any ", ' or spaces at start and end of variable
    $kind =~ s/^[\s"']*(\S+?)[\s"']*$/$1/;
    $name =~ s/^[\s"']*(\S+?)[\s"']*$/$1/;

    return unless ($kind =~ /^(CassandraCluster|Deployment|DaemonSet|StatefulSet|CronJob|Job)$/i);
    return if ($kind =~ /^Job$/i && $include_job == 0);

    print STDERR "@{$arrayref}[0]\n" if ($dot_separated_to_stderr == 1);

PARSE_SECTION_DATA:

    # Read yaml data in $arrayref into hash
    if (General::Yaml_Operations::read_array({ "input" => $arrayref, "output-ref" => \%yaml_data, "hide-error-messages" => 1 }) == 1) {
        my $lines = "Failure to parse the input.\n" . $General::Yaml_Operations::last_yaml_error_details;
        if ($error_details_printed == 0) {
            # Write the data causing the error, on first error
            $error_details_printed = 1;
            my $linecnt = 0;
            $lines .= ">>>> Start of data >>>>\n";
            for (@{$arrayref}) {
                $linecnt++;
                $lines .= sprintf "%5d : %s\n", $linecnt, $_;
            }
            $lines .= "<<<< End of data <<<<\n\n";
        }
        if ($logfile ne "") {
            # Write the details only to the log file
            General::Logging::log_write($lines);
        } else {
            if ($hide_fixable_parsing_errors == 0) {
                # Since no log is active write the message also to the user
                General::Logging::log_user_message($lines);
            }
        }
        if ($General::Yaml_Operations::last_yaml_error_details =~ /Message\s+:\s+(Bad indendation in FLOWSEQ|Wrong indendation or missing closing quote).+/s &&
            $General::Yaml_Operations::last_yaml_error_details =~ /Line\s+:\s+(\d+).+/s) {
            # Look for something like this:
            #
            # Line      : 62
            # Column    : 9
            # Message   : Bad indendation in FLOWSEQ
            # Where     : /proj/DSC_GIT/eustone/dsc_suf/esc/daft/perl/lib/YAML/PP/Parser.pm line 197
            # YAML      : "\"\"chown -fR 62055:0 /etc/confluent/docker /etc/kafka /var/lib/kafka/data /etc/kafka/secrets || exit 0\"]\n"
            #   at /proj/DSC_GIT/eustone/dsc_suf/esc/daft/perl/lib/YAML/PP/Loader.pm line 94.
            #
            # or
            #
            # Line      : 352
            # Column    : 1
            # Message   : Wrong indendation or missing closing quote <">
            # Where     : /local/eustone/git/5g_proto/daft/perl/lib/YAML/PP/Lexer.pm line 649
            # YAML      : "/usr/bin/catatonit -- "
            #   at /local/eustone/git/5g_proto/daft/perl/lib/YAML/PP/Loader.pm line 94.

            # Below is a special workaround for a problem that the parser cannot handle.
            # Details:
            # From file eric-sc-umbrella/charts/eric-data-message-bus-kf/templates/kafka-ss.yaml
            # Change from:
            #   initContainers:
            #   - name: check-zk-ready
            #       image: "k8s-registry.eccd.local/proj-adp-message-bus-kf-drop/eric-data-message-bus-kf:1.11.0-33"
            #       command: ['sh', '-c', 'until kafka-topics --zookeeper eric-data-coordinator-zk 2181 --list; do echo waiting for data coordinator zk ready; sleep 2; done;',
            #       "chown -fR 62055:0 /etc/confluent/docker /etc/kafka /var/lib/kafka/data /etc/kafka/secrets || exit 0"]
            # Change to:
            #   initContainers:
            #   - name: check-zk-ready
            #       image: "k8s-registry.eccd.local/proj-adp-message-bus-kf-drop/eric-data-message-bus-kf:1.11.0-33"
            #       command: ['sh', '-c', 'until kafka-topics --zookeeper eric-data-coordinator-zk 2181 --list; do echo waiting for data coordinator zk ready; sleep 2; done;',
            #         "chown -fR 62055:0 /etc/confluent/docker /etc/kafka /var/lib/kafka/data /etc/kafka/secrets || exit 0"]
            my $line = $1 - 1;
            if ($line != $previous_line_with_error) {
                # Attempt to fix the problem for as many times as needed, as long as it's on a different line
                $previous_line_with_error = $line;
                my $indent_by = "  ";
                if (@{$arrayref}[$line-1] =~ /^(\s+)/) {
                    # Take the indentation level from the line above and add 2 spaces
                    $indent_by = "$1  ";
                }
                @{$arrayref}[$line] =~ s/^\s*/$indent_by/;
                $fix_attempted++;
                if ($logfile eq "" && $hide_fixable_parsing_errors == 1) {
                    # Hide the message until it fails
                } elsif ($logfile ne "") {
                    # Only log the message to file
                    General::Logging::log_write("Attempt $fix_attempted, to try to fix the indentation issue on the line and run the decode once more\n");
                } else {
                    General::Logging::log_user_message("Attempt $fix_attempted, to try to fix the indentation issue on the line and run the decode once more");
                }
                goto PARSE_SECTION_DATA;
            } else {
                if ($logfile eq "" && $hide_fixable_parsing_errors == 1) {
                    # Since we did not show the message above, we need to show it now
                    General::Logging::log_user_message("$lines\nThe fix did not work, same error again");
                } else {
                    General::Logging::log_user_message("The fix did not work, same error again");
                }
            }
        }
        General::Logging::log_user_error_message("Failed to parse YAML data into hash variable\n" . $General::Yaml_Operations::last_yaml_error_details);
        return;
    }

    if ($fix_attempted > 0) {
        if ($logfile eq "" && $hide_fixable_parsing_errors == 1) {
            # Hide the message since the parse was successful
        } elsif ($logfile ne "") {
            # Only log the message to file
            General::Logging::log_write("$fix_attempted fixes done, parsing of YAML data was successful");
        } else {
            General::Logging::log_user_message("$fix_attempted fixes done, parsing of YAML data was successful");
        }
    }
    # Convert data into dot separated lines of data
    General::Data_Structure_Operations::dump_dot_separated_hash_ref_to_variable(\%yaml_data, "", \@data);
    for (@data) {
        print STDERR "$_\n" if ($dot_separated_to_stderr == 1);
        if ($kind =~ /^CassandraCluster$/i) {
            # Special handling for eric-data-wide-column-database-cd and eric-bsf-wcdb-cd since the useful information
            # for this is stored in a different format and we need to convert it to the naming
            # that will be present after the deployment.

            my $type1;
            my $type2;
            my $type3;
            my $type4;

            if (/^(spec\.dataCenters\.\d+\.initContainers\.initDc\.resourceRequirements)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
                # This is the container name after the pod has been deployed
                $container_name = "init-dc";
                $type1 = $1;
                $type2 = $2;
                $type3 = $3;
                $type4 = $4;
            } elsif (/^(spec\.dataCenters\.\d+\.cassandra\.resourceRequirements)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
                # This is the container name after the pod has been deployed
                $container_name = "cassandra";
                $type1 = $1;
                $type2 = $2;
                $type3 = $3;
                $type4 = $4;
            } elsif (/^(spec\.dataCenters\.\d+\.backupAndRestore\.brsc\.resourceRequirements)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
                # This is the container name after the pod has been deployed
                $container_name = "brsc";
                $type1 = $1;
                $type2 = $2;
                $type3 = $3;
                $type4 = $4;
            } elsif (/^(spec\.dataCenters\.\d+\.automaticRepair\.ecChronos\.resourceRequirements)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
                # This is the container name after the pod has been deployed
                $container_name = "ecchronos";
                $type1 = $1;
                $type2 = $2;
                $type3 = $3;
                $type4 = $4;
            } elsif (/^(spec\.dataCenters\.\d+\.automaticRepair\.ecChronos\.tlsRefresher\.resourceRequirements)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
                # This is the container name after the pod has been deployed
                $container_name = "ecchronos-tls-refresher";
                $type1 = $1;
                $type2 = $2;
                $type3 = $3;
                $type4 = $4;
            } else {
                next;
            }
            $resource_name = "$name.$container_name";
            $max_resource_name = length($resource_name) if ($max_resource_name < length($resource_name));
            $value = ADP::Kubernetes_Operations::convert_number_unit($type4);
            printf "DBG:1=$type1,\t2=$type2,\t3=$type3,\t4=$type4, converted 4=%s\n", $value if $debug;
            unless (exists $required_container_resources{$resource_name}) {
                # Initialize at first value
                $file_resources{$resource_name}{'limits'}{'cpu'} = 0;
                $file_resources{$resource_name}{'limits'}{'ephemeral-storage'} = 0;
                $file_resources{$resource_name}{'limits'}{'memory'} = 0;
                $file_resources{$resource_name}{'requests'}{'cpu'} = 0;
                $file_resources{$resource_name}{'requests'}{'ephemeral-storage'} = 0;
                $file_resources{$resource_name}{'requests'}{'memory'} = 0;
                $required_container_resources{$resource_name}{'limits'}{'cpu'} = 0;
                $required_container_resources{$resource_name}{'limits'}{'ephemeral-storage'} = 0;
                $required_container_resources{$resource_name}{'limits'}{'memory'} = 0;
                $required_container_resources{$resource_name}{'requests'}{'cpu'} = 0;
                $required_container_resources{$resource_name}{'requests'}{'ephemeral-storage'} = 0;
                $required_container_resources{$resource_name}{'requests'}{'memory'} = 0;
                $resource_information{$resource_name}{'replicas'} = "-";
                if (exists $yaml_data{'spec'} && exists $yaml_data{'spec'}->{'dataCenters'} && exists $yaml_data{'spec'}->{'dataCenters'}->[0] && exists $yaml_data{'spec'}->{'dataCenters'}->[0]->{'replicas'}) {
                    # This was needed because the dot separated array is sorted in alphabetical order which might
                    # mean that the replicas value might not already have been read, so instead read the data from the YAML hash.
                    $replicas = $yaml_data{'spec'}->{'dataCenters'}->[0]->{'replicas'};
                }
            }
            $file_resources{$resource_name}{$type2}{$type3} = $value;
            $resource_information{$resource_name}{'replicas'} = $replicas;
            $resource_information{$name}{'replicas'} = $replicas;
            $required_container_resources{$resource_name}{$type2}{$type3} = $value;   # Create a copy in case we need to change the value from another values.yaml file
            # Store the 'kind'
            if (exists $resource_information{$resource_name}{'kind'}) {
                if ($resource_information{$resource_name}{'kind'} ne $kind) {
                    if ($logfile ne "") {
                        # Only log the message to file
                        General::Logging::log_write("Different 'kind' information for container name '$resource_name': Already stored=$resource_information{$resource_name}{'kind'}, New=$kind\n");
                    }
                }
            } else {
                $resource_information{$resource_name}{'kind'} = $kind;
            }
            if (exists $resource_information{$name}{'kind'}) {
                if ($resource_information{$name}{'kind'} ne $kind) {
                    if ($logfile ne "") {
                        # Only log the message to file
                        General::Logging::log_write("Different 'kind' information for pod name '$name': Already stored=$resource_information{$name}{'kind'}, New=$kind\n");
                    }
                }
            } else {
                $resource_information{$name}{'kind'} = $kind;
            }
        } elsif (/^.*[c|C]ontainers\.\d+\.name=(\S+)\s*$/) {
            # containers[0] or initContainers[0]
            $container_name = $1;
            $resource_name = "$name.$container_name";
            # Initialize the values
            $file_resources{$resource_name}{'limits'}{'cpu'} = 0;
            $file_resources{$resource_name}{'limits'}{'ephemeral-storage'} = 0;
            $file_resources{$resource_name}{'limits'}{'memory'} = 0;
            $file_resources{$resource_name}{'requests'}{'cpu'} = 0;
            $file_resources{$resource_name}{'requests'}{'ephemeral-storage'} = 0;
            $file_resources{$resource_name}{'requests'}{'memory'} = 0;
            $required_container_resources{$resource_name}{'limits'}{'cpu'} = 0;
            $required_container_resources{$resource_name}{'limits'}{'ephemeral-storage'} = 0;
            $required_container_resources{$resource_name}{'limits'}{'memory'} = 0;
            $required_container_resources{$resource_name}{'requests'}{'cpu'} = 0;
            $required_container_resources{$resource_name}{'requests'}{'ephemeral-storage'} = 0;
            $required_container_resources{$resource_name}{'requests'}{'memory'} = 0;
            $resource_information{$resource_name}{'replicas'} = "-";
            if (exists $yaml_data{'spec'} && exists $yaml_data{'spec'}->{'replicas'}) {
                # This was needed because the dot separated array is sorted in alphabetical order which might
                # mean that the replicas value might not already have been read, so instead read the data from the YAML hash.
                $replicas = $yaml_data{'spec'}->{'replicas'};
            }
        } elsif (/^(\S+)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
            #$resource_name = "$name.$1";
            $resource_name = "$name.$container_name";
            $max_resource_name = length($resource_name) if ($max_resource_name < length($resource_name));
            $value = ADP::Kubernetes_Operations::convert_number_unit($4);
            printf "DBG:1=$1,\t2=$2,\t3=$3,\t4=$4, converted 4=%s\n", $value if $debug;
            $file_resources{$resource_name}{$2}{$3} = $value;
            $resource_information{$resource_name}{'replicas'} = $replicas;
            $resource_information{$name}{'replicas'} = $replicas;
            $required_container_resources{$resource_name}{$2}{$3} = $value;   # Create a copy in case we need to change the value from another values.yaml file
            # Store the 'kind'
            if (exists $resource_information{$resource_name}{'kind'}) {
                if ($resource_information{$resource_name}{'kind'} ne $kind) {
                    if ($logfile ne "") {
                        # Only log the message to file
                        General::Logging::log_write("Different 'kind' information for container name '$resource_name': Already stored=$resource_information{$resource_name}{'kind'}, New=$kind\n");
                    }
                }
            } else {
                $resource_information{$resource_name}{'kind'} = $kind;
            }
            if (exists $resource_information{$name}{'kind'}) {
                if ($resource_information{$name}{'kind'} ne $kind) {
                    if ($logfile ne "") {
                        # Only log the message to file
                        General::Logging::log_write("Different 'kind' information for pod name '$name': Already stored=$resource_information{$name}{'kind'}, New=$kind\n");
                    }
                }
            } else {
                $resource_information{$name}{'kind'} = $kind;
            }
        }
    }
}

# -----------------------------------------------------------------------------
#
# Parse the printout file of the helm template output.
#
sub parse_template_printout_file {
    my $rc;
    my @section_data = ();
    my $section_kind;
    my $section_name;
    my @template_data;

    $rc = General::File_Operations::read_file(
        {
            "filename"              => $printout_file,
            "output-ref"            => \@template_data,
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Failed to read '$printout_file' file");
        exit 1;
    }

    for my $line (@template_data) {
        if ($line =~ /^---\s*$/) {
            # Start of new section
            if (@section_data) {
                # We have already collected the data from a previous section, now process it
                parse_section_data($section_kind, $section_name, \@section_data);
            }
            @section_data = ();
            $section_kind = "";
            $section_name = "";
            next;
        } else {
            push @section_data, $line;
            if ($line =~ /^kind:\s+(\S+)/) {
                $section_kind = $1;
                next;
            } elsif ($line =~ /^  name:\s+(\S+)/ && $section_name eq "") {
                $section_name = $1;
                next;
            }
            next;
        }
    }
    if (@section_data) {
        # We need to process the data from the last section
        parse_section_data($section_kind, $section_name, \@section_data);
    }
}

# -----------------------------------------------------------------------------
#
# Parse the packed umbrella file.
#
sub parse_multiple_umbrella_files {
    my $extra_parameters = "-g";
    my $rc;
    my @result;
    my @section_data = ();
    my $section_kind;
    my $section_name;
    my @std_error = ();
    my @template_data;

    #for my %temp_hash (@umbrella_structure) {
    for (@umbrella_structure) {

        my %temp_hash = %{$_};
        @section_data = ();
        $extra_parameters = "-g";
        for (@{$temp_hash{'value-files'}}) {
            $extra_parameters .= " -f $_";
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$helm_executable template $temp_hash{'umbrella-file'} $extra_parameters" . $kubeconfig,
                "hide-output"   => 1,
                "return-output" => \@template_data,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute '$helm_executable template' command");
            exit 1;
        }

        for my $line (@template_data) {
            if ($line =~ /^---\s*$/) {
                # Start of new section
                if (@section_data) {
                    # We have already collected the data from a previous section, now process it
                    parse_section_data($section_kind, $section_name, \@section_data);
                }
                @section_data = ();
                $section_kind = "";
                $section_name = "";
                next;
            } else {
                push @section_data, $line;
                if ($line =~ /^kind:\s+(\S+)/) {
                    $section_kind = $1;
                    next;
                } elsif ($line =~ /^  name:\s+(\S+)/ && $section_name eq "") {
                    $section_name = $1;
                    next;
                }
                next;
            }
        }
        if (@section_data) {
            # We need to process the data from the last section
            parse_section_data($section_kind, $section_name, \@section_data);
        }
    }
}

# -----------------------------------------------------------------------------
#
# Parse the packed umbrella file.
#
sub parse_umbrella_file {
    my $extra_parameters = "-g";
    my $rc;
    my @result;
    my @section_data = ();
    my $section_kind;
    my $section_name;
    my @std_error = ();
    my @template_data;

    for (@value_files) {
        $extra_parameters .= " -f $_";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "$helm_executable template $input_file $extra_parameters" . $kubeconfig,
            "hide-output"   => 1,
            "return-output" => \@template_data,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to execute '$helm_executable template' command");
        exit 1;
    }

    for my $line (@template_data) {
        if ($line =~ /^---\s*$/) {
            # Start of new section
            if (@section_data) {
                # We have already collected the data from a previous section, now process it
                parse_section_data($section_kind, $section_name, \@section_data);
            }
            @section_data = ();
            $section_kind = "";
            $section_name = "";
            next;
        } else {
            push @section_data, $line;
            if ($line =~ /^kind:\s+(\S+)/) {
                $section_kind = $1;
                next;
            } elsif ($line =~ /^  name:\s+(\S+)/ && $section_name eq "") {
                $section_name = $1;
                next;
            }
            next;
        }
    }
    if (@section_data) {
        # We need to process the data from the last section
        parse_section_data($section_kind, $section_name, \@section_data);
    }
}

# -----------------------------------------------------------------------------
#
# Parse the collected values.yaml data.
#
sub parse_values_yaml_files {
    my @data;
    my $pod;
    my $resource_name;
    my @top_level_values_data = ();
    my $value;

    for my $filename (@files) {
        print "\nDBG:filename=$filename\n" if $debug;
        @data = convert_values_yaml_data($filename);
        if ($filename =~ /^.+\/charts\/([^\/]+)\/values\.yaml$/) {
            $pod = $1;
            $resource_information{$pod}{'replicas'} = "-";
        } elsif ($filename =~ /^.+\/values\.yaml$/) {
            # This is the top level values.yaml file that might override the values
            # from the individual component values.yaml files.
            # For now we just save the data and will parse it after all other values
            # files has been parsed.
            @top_level_values_data = @data;
            push @files_with_updated_resources, $filename;
            next;
        } else {
            # Ignore this file since it does not match the expected file path
            next;
        }

        for (@data) {
            if (/^(\S+)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
                $resource_name = "$pod.$1";
                $max_resource_name = length($resource_name) if ($max_resource_name < length($resource_name));
                $value = ADP::Kubernetes_Operations::convert_number_unit($4);
                printf "DBG:1=$1,\t2=$2,\t3=$3,\t4=$4, converted 4=%s\n", $value if $debug;
                #if (exists $file_resources{$resource_name} && exists $file_resources{$resource_name}{$2} && exists $file_resources{$resource_name}{$2}{$3}) {
               #if (exists $file_resources{$resource_name}) {
               #    print "DBG:>>>>>>>> \$file_resource{$resource_name} exists, ";
               #    if (exists $file_resources{$resource_name}{$2}) {
               #        print "\$file_resources{$resource_name}{$2} exists, ";
               #        if (exists $file_resources{$resource_name}{$2}{$3}) {
               #            print "\$file_resources{$resource_name}{$2}{$3} exists";
               #        }
               #    }
               #    print " <<<<<<<<\n";
               #    #print "DBG:ERROR >>>>>>>>>>>>>> {$resource_name}{$2}{$3} exists <<<<<<<<<<<<<<<<<<<<<\n";
               #}
                $file_resources{$resource_name}{$2}{$3} = $value;
                $required_container_resources{$resource_name}{$2}{$3} = $value;   # Create a copy in case we need to change the value from another values.yaml file
                $resource_information{$resource_name}{'replicas'} = "-";
            }
        }
    }

    if (@top_level_values_data) {
        # Now check if we need to override any values from the top level values.yaml file
        for (@top_level_values_data) {
            if (/^(\S+)\.(limits|requests)\.(cpu|ephemeral-storage|memory)=(\S+)/) {
                $resource_name = "$1";
                $max_resource_name = length($resource_name) if ($max_resource_name < length($resource_name));
                $value = ADP::Kubernetes_Operations::convert_number_unit($4);
                printf "DBG:1=$1,\t2=$2,\t3=$3,\t4=$4, converted 4=%s\n", $value if $debug;
                $required_container_resources{$resource_name}{$2}{$3} = $value;
                $updated_resources{$resource_name}{$2}{$3} = $value;
                print "DBG:Value \$file_resources{$resource_name}{$2}{$3} changed from $file_resources{$resource_name}{$2}{$3} to $required_container_resources{$resource_name}{$2}{$3}\n" if $debug;
            }
        }
    }
}

# -----------------------------------------------------------------------------
#
# Print common data.
#
sub print_common_data {
    print "\n***********************\n";
    print "* General Information *\n";
    print "***********************\n";

    print "\nCalculations printed below are based on having $worker_cnt worker nodes.\n";

    print "\nThe following PODs have the number of replicas depending on the number of worker nodes:\n";
    for (@replica_dependencies_workers) {
        print "  - $_\n";
    }

    print "\nThe following PODs have the number of replicas depending on the traffic profile:\n";
    for (@replica_dependencies_traffic_profile) {
        print "  - $_\n";
    }

    print "\nThe following PODs have the number of replicas depending on the replicas of another POD:\n";
    for (sort keys %replica_dependencies_other_resource) {
        print "  - $_ same replicas as $replica_dependencies_other_resource{$_}\n";
    }

    if ($ignore_resources == 1) {
        print "\nThe following POD.CONTAINER resources are not counted:\n";
        for (sort keys %ignore_resource) {
            print "  - $_\n";
        }
    }
}

# -----------------------------------------------------------------------------
#
# Print required container and pod resources from values.yaml files.
#
sub print_component_file_data {
    my $limits_cpu;
    my $limits_memory;
    my $limits_storage;
    my %pod_resources;
    # Required resources according to pod values.yaml file.
    #   1st key: Pod Name
    #   2nd key: One of the following:
    #       limits
    #       requests
    #   3rd key: One of the following
    #       cpu
    #       memory
    my $requests_cpu;
    my $requests_memory;
    my $requests_storage;

    print "\n*********************************\n";
    print "* Resources from umbrella files *\n";
    print "*********************************\n";

    if ($input_directory ne "") {
        print "\nContainer resource requirements relative to directory path: $input_directory\n\n";
    } elsif ($input_file ne "") {
        print "\nContainer resource requirements relative to file path: $input_file\n\n";
    } elsif ($printout_file ne "") {
        print "\nContainer resource requirements relative to file path: $printout_file\n\n";
    } elsif (@multiple_files) {
        print "\nContainer resource requirements relative to file path: Multiple files specified\n\n";
    }
    printf "%-${max_resource_name}s  %-8s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %s\n",
        "Resource Name",
        "Replicas",
        "Limits CPU",
        "Limits Memory",
        "Limits Storage",
        "Requests CPU",
        "Requests Memory",
        "Requests Storage",
        "Kind";
    printf "%${max_resource_name}s  %8s  %20s  %20s  %20s  %20s  %20s  %20s  %s\n",
        "-"x$max_resource_name,
        "-"x8,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x4;
    for my $resource_name (sort keys %file_resources) {
        next if (exists $ignore_resource{$resource_name});

        $limits_cpu = return_existing_file_resources_value($resource_name, 'limits', 'cpu');
        $limits_memory = return_existing_file_resources_value($resource_name, 'limits', 'memory');
        $limits_storage = return_existing_file_resources_value($resource_name, 'limits', 'ephemeral-storage');
        $requests_cpu = return_existing_file_resources_value($resource_name, 'requests', 'cpu');
        $requests_memory = return_existing_file_resources_value($resource_name, 'requests', 'memory');
        $requests_storage = return_existing_file_resources_value($resource_name, 'requests', 'ephemeral-storage');

        printf "%-${max_resource_name}s  %8s  %20s  %20s  %20s  %20s  %20s  %20s  %s\n",
            $resource_name,
            return_existing_file_replicas_value($resource_name),
            $limits_cpu,
            format_output($limits_memory),
            format_output($limits_storage),
            $requests_cpu,
            format_output($requests_memory),
            format_output($requests_storage),
            $resource_information{$resource_name}{'kind'};

        if ($resource_name =~ /^([^\.]+)\..+/) {
            $pod_resources{$1}{'limits'}{'cpu'} += $limits_cpu;
            $pod_resources{$1}{'limits'}{'memory'} += $limits_memory;
            $pod_resources{$1}{'limits'}{'ephemeral-storage'} += $limits_storage;
            $pod_resources{$1}{'requests'}{'cpu'} += $requests_cpu;
            $pod_resources{$1}{'requests'}{'memory'} += $requests_memory;
            $pod_resources{$1}{'requests'}{'ephemeral-storage'} += $requests_storage;
            $total_pod_resources{'limits'}{'cpu'} += $limits_cpu;
            $total_pod_resources{'limits'}{'memory'} += $limits_memory;
            $total_pod_resources{'limits'}{'ephemeral-storage'} += $limits_storage;
            $total_pod_resources{'requests'}{'cpu'} += $requests_cpu;
            $total_pod_resources{'requests'}{'memory'} += $requests_memory;
            $total_pod_resources{'requests'}{'ephemeral-storage'} += $requests_storage;
        }
    }
    printf "%${max_resource_name}s  %8s  %20s  %20s  %20s  %20s  %20s  %20s  %s\n",
        "-"x$max_resource_name,
        "-"x8,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x4;
    printf "%-${max_resource_name}s  %8s  %20s  %20s  %20s  %20s  %20s  %20s  %s\n",
        "All Resources",
        "-",
        $total_container_resources{'limits'}{'cpu'},
        format_output($total_container_resources{'limits'}{'memory'}),
        format_output($total_container_resources{'limits'}{'ephemeral-storage'}),
        $total_container_resources{'requests'}{'cpu'},
        format_output($total_container_resources{'requests'}{'memory'}),
        format_output($total_container_resources{'requests'}{'ephemeral-storage'}),
        "-";

    print "\nPod resource requirements relative to directory path: $input_directory\n\n";
    printf "%-${max_resource_name}s  %-8s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %s\n",
        "Resource Name",
        "Replicas",
        "Limits CPU",
        "Limits Memory",
        "Limits Storage",
        "Requests CPU",
        "Requests Memory",
        "Requests Storage",
        "Kind";
    printf "%${max_resource_name}s  %8s  %20s  %20s  %20s  %20s  %20s  %20s  %s\n",
        "-"x$max_resource_name,
        "-"x8,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x4;
    for my $pod_name (sort keys %pod_resources) {
        printf "%-${max_resource_name}s  %8s  %20s  %20s  %20s  %20s  %20s  %20s  %s\n",
            $pod_name,
            return_existing_file_replicas_value($pod_name),
            $pod_resources{$pod_name}{'limits'}{'cpu'},
            format_output($pod_resources{$pod_name}{'limits'}{'memory'}),
            format_output($pod_resources{$pod_name}{'limits'}{'ephemeral-storage'}),
            $pod_resources{$pod_name}{'requests'}{'cpu'},
            format_output($pod_resources{$pod_name}{'requests'}{'memory'}),
            format_output($pod_resources{$pod_name}{'requests'}{'ephemeral-storage'}),
            $resource_information{$pod_name}{'kind'};
    }
    printf "%${max_resource_name}s  %8s  %20s  %20s  %20s  %20s  %20s  %20s  %4s\n",
        "-"x$max_resource_name,
        "-"x8,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x4;
    printf "%-${max_resource_name}s  %8s  %20s  %20s  %20s  %20s  %20s  %20s  %s\n",
        "All Resources",
        "-",
        $total_pod_resources{'limits'}{'cpu'},
        format_output($total_pod_resources{'limits'}{'memory'}),
        format_output($total_pod_resources{'limits'}{'ephemeral-storage'}),
        $total_pod_resources{'requests'}{'cpu'},
        format_output($total_pod_resources{'requests'}{'memory'}),
        format_output($total_pod_resources{'requests'}{'ephemeral-storage'}),
        "-";
}

# -----------------------------------------------------------------------------
#
# Print required resources from values.yaml files and from top level
# values.yaml or other files.
#
sub print_required_resources_data {
    my $limits_cpu;
    my $limits_memory;
    my $limits_storage;
    my $requests_cpu;
    my $requests_memory;
    my $requests_storage;

    print "\n**********************************************\n";
    print "* Required Resources after reading all files *\n";
    print "* and applying replicas calculation          *\n";
    print "**********************************************\n";

    #
    # Container data
    #

    print "\nRequired resources per container:\n\n";
    printf "%-${max_resource_name}s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s\n",
        "Resource Name",
        "Limits CPU",
        "Limits Memory",
        "Limits Storage",
        "Requests CPU",
        "Requests Memory",
        "Requests Storage";
    printf "%${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_resource_name,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    for my $resource_name (sort keys %required_container_resources) {
        next if (exists $ignore_resource{$resource_name});

        $limits_cpu = return_existing_required_container_resources_value($resource_name, 'limits', 'cpu');
        $limits_memory = return_existing_required_container_resources_value($resource_name, 'limits', 'memory');
        $limits_storage = return_existing_required_container_resources_value($resource_name, 'limits', 'ephemeral-storage');
        $requests_cpu = return_existing_required_container_resources_value($resource_name, 'requests', 'cpu');
        $requests_memory = return_existing_required_container_resources_value($resource_name, 'requests', 'memory');
        $requests_storage = return_existing_required_container_resources_value($resource_name, 'requests', 'ephemeral-storage');

        printf "%-${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            $resource_name,
            $limits_cpu,
            format_output($limits_memory),
            format_output($limits_storage),
            $requests_cpu,
            format_output($requests_memory),
            format_output($requests_storage);
    }
    printf "%${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_resource_name,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    printf "%-${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "All Resources",
        $total_required_container_resources{'limits'}{'cpu'},
        format_output($total_required_container_resources{'limits'}{'memory'}),
        format_output($total_required_container_resources{'limits'}{'ephemeral-storage'}),
        $total_required_container_resources{'requests'}{'cpu'},
        format_output($total_required_container_resources{'requests'}{'memory'}),
        format_output($total_required_container_resources{'requests'}{'ephemeral-storage'});

    #
    # Pod data
    #

    print "\nRequired resources per pod\n\n";
    printf "%-${max_resource_name}s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s\n",
        "Resource Name",
        "Limits CPU",
        "Limits Memory",
        "Limits Storage",
        "Requests CPU",
        "Requests Memory",
        "Requests Storage";
    printf "%${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_resource_name,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    for my $pod_name (sort keys %required_pod_resources) {
        printf "%-${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            $pod_name,
            return_existing_required_pod_resources_value($pod_name, 'limits', 'cpu'),
            format_output(return_existing_required_pod_resources_value($pod_name, 'limits', 'memory')),
            format_output(return_existing_required_pod_resources_value($pod_name, 'limits', 'ephemeral-storage')),
            return_existing_required_pod_resources_value($pod_name, 'requests', 'cpu'),
            format_output(return_existing_required_pod_resources_value($pod_name, 'requests', 'memory')),
            format_output(return_existing_required_pod_resources_value($pod_name, 'requests', 'ephemeral-storage'));
    }
    printf "%${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_resource_name,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    printf "%-${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "All Resources",
        $total_required_pod_resources{'limits'}{'cpu'},
        format_output($total_required_pod_resources{'limits'}{'memory'}),
        format_output($total_required_pod_resources{'limits'}{'ephemeral-storage'}),
        $total_required_pod_resources{'requests'}{'cpu'},
        format_output($total_required_pod_resources{'requests'}{'memory'}),
        format_output($total_required_pod_resources{'requests'}{'ephemeral-storage'});

    #
    # Print total requirement summary
    #
    print "\n***********************\n";
    print "* Information Summary *\n";
    print "***********************\n";

    print "\n";
    print "Required CPU Resources:\n-----------------------\n";
    print "Total required resource limits CPU:         $total_required_pod_resources{'limits'}{'cpu'}\n";
    print "Total required resource requests CPU:       $total_required_pod_resources{'requests'}{'cpu'}\n";
    print "Highest required pod resource limits CPU:   $top_pod_resources{'limits'}{'cpu'}\n";
    print "Highest required pod resource requests CPU: $top_pod_resources{'requests'}{'cpu'}\n";
    printf "Required pod resource requests CPU:         %s\n\n", return_all_required_pod_resource_values("cpu_no_replicas");

    print "Required Memory Resources:\n--------------------------\n";
    printf "Total required resource limits Memory:         %s\n",   format_output($total_required_pod_resources{'limits'}{'memory'});
    printf "Total required resource requests Memory:       %s\n",   format_output($total_required_pod_resources{'requests'}{'memory'});
    printf "Highest required pod resource limits Memory:   %s\n",   format_output($top_pod_resources{'limits'}{'memory'});
    printf "Highest required pod resource requests Memory: %s\n",   format_output($top_pod_resources{'requests'}{'memory'});
    printf "Required pod resource requests Memory:         %s\n\n", return_all_required_pod_resource_values("memory_no_replicas");

    print "Required Storage Resources:\n---------------------------\n";
    printf "Total required resource limits Storage:         %s\n",   format_output($total_required_pod_resources{'limits'}{'ephemeral-storage'});
    printf "Total required resource requests Storage:       %s\n",   format_output($total_required_pod_resources{'requests'}{'ephemeral-storage'});
    printf "Highest required pod resource limits Storage:   %s\n",   format_output($top_pod_resources{'limits'}{'ephemeral-storage'});
    printf "Highest required pod resource requests Storage: %s\n",   format_output($top_pod_resources{'requests'}{'ephemeral-storage'});
    printf "Required pod resource requests Storage:         %s\n\n", return_all_required_pod_resource_values("ephemeral-storage_no_replicas");
}

# -----------------------------------------------------------------------------
#
# Print updated resources from top level values.yaml or other yaml files.
#
sub print_updated_resources_data {
    return unless (@files_with_updated_resources);

    print "\n**************************************\n";
    print "* Updated Resources from other files *\n";
    print "**************************************\n";

    print "\nUpdated resources per container due to the following file(s):\n";
    for (@files_with_updated_resources) {
        print "  $_\n";
    }
    print "\n";

    printf "%-${max_resource_name}s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s\n",
        "Resource Name",
        "Limits CPU",
        "Limits Memory",
        "Limits Storage",
        "Requests CPU",
        "Requests Memory",
        "Requests Storage";
    printf "%${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_resource_name,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    for my $resource_name (sort keys %updated_resources) {
        next if (exists $ignore_resource{$resource_name});

        printf "%-${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            $resource_name,
            return_existing_updated_resources_value($resource_name, 'limits', 'cpu'),
            format_output(return_existing_updated_resources_value($resource_name, 'limits', 'memory')),
            format_output(return_existing_updated_resources_value($resource_name, 'limits', 'ephemeral-storage')),
            return_existing_updated_resources_value($resource_name, 'requests', 'cpu'),
            format_output(return_existing_updated_resources_value($resource_name, 'requests', 'memory')),
            format_output(return_existing_updated_resources_value($resource_name, 'requests', 'ephemeral-storage'));
    }
    printf "%${max_resource_name}s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_resource_name,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
}

# -----------------------------------------------------------------------------
#
# Return a scalar with all required_container_resources values, sorted in
# decending order from largest to lowest value.
#
sub return_all_required_pod_resource_values {
    my $type = shift;   # cpu_no_replicas, ephemeral-storage_no_replicas, memory_no_replicas
    my $pod = "";
    my $values = "";
    my @values = ();

    return "" unless ($type =~ /^(cpu_no_replicas|ephemeral-storage_no_replicas|memory_no_replicas)$/);

    for $pod (keys %required_pod_resources) {
        if (exists $required_pod_resources{$pod}{'requests'}{$type}) {
            for (my $i =1; $i <= $required_pod_resources{$pod}{'replicas'}; $i++) {
                if ($required_pod_resources{$pod}{'requests'}{$type} != 0) {
                    push @values, $required_pod_resources{$pod}{'requests'}{$type};
                }
            }
        }
    }
    for my $value (sort { $b <=> $a } @values) {
        if ($type eq "cpu_no_replicas") {
            $values .= "$value ";
        } else {
            $values .= sprintf "%s ", format_output($value);
        }
    }
    $values =~ s/\s+$//;

    return $values;
}

# -----------------------------------------------------------------------------
#
# Return existing file_resources replicas value, with special handling if value
# does not exist or is "-" or 0.
#
sub return_existing_file_replicas_value {
    my $key1 = shift;
    my $pod = "";
    my $replicas = "";

    if ($key1 =~ /^([^\.]+)\..+/) {
        # We got a dot separated pod.container name as input, extract the pod name
        $pod = $1;
    } else {
        # We already got a pod name as an input
        $pod = $key1;
    }
    if (exists $resource_information{$pod} && exists $resource_information{$pod}{'replicas'}) {
        $replicas = $resource_information{$pod}{'replicas'};
        if ($replicas eq "-" || $replicas == 0) {
            # Replicas value not set, apply special handling
            if ( grep(/^$pod$/, @replica_dependencies_workers) ) {
                # Special handling if the pod is in the @replica_dependencies_workers array
                $replicas = $worker_cnt;
            } elsif ( exists $replica_dependencies_other_resource{$pod} ) {
                # Special handling if the pod is in the %replica_dependencies_other_resource hash
                $replicas = return_existing_file_replicas_value($replica_dependencies_other_resource{$pod});
            } elsif ($replicas eq "-") {
                $replicas = 1;
            }
        }
    } else {
        $replicas = "-";
    }

    return $replicas;
}

# -----------------------------------------------------------------------------
#
# Return existing file_resources value, or value 0 if not existing.
#
sub return_existing_file_resources_value {
    my $key1 = shift;
    my $key2 = shift;
    my $key3 = shift;

    if (exists $file_resources{$key1} && exists $file_resources{$key1}{$key2} && exists $file_resources{$key1}{$key2}{$key3}) {
        $total_container_resources{$key2}{$key3} += $file_resources{$key1}{$key2}{$key3};
        return $file_resources{$key1}{$key2}{$key3};
    } else {
        print "DBG:\$file_resources{$key1}{$key2}{$key3} does not exist\n" if $debug;
        return 0;
    }
}

# -----------------------------------------------------------------------------
#
# Return existing required_container_resources value, or value 0 if not existing.
#
sub return_existing_required_container_resources_value {
    my $key1 = shift;
    my $key2 = shift;
    my $key3 = shift;
    my $pod = "";
    my $replicas = 1;
    my $value;
    my $value_with_replicas;

    if (exists $required_container_resources{$key1} && exists $required_container_resources{$key1}{$key2} && exists $required_container_resources{$key1}{$key2}{$key3}) {
        $value = $required_container_resources{$key1}{$key2}{$key3};

        if ($key1 =~ /^([^\.]+)\..+/) {
            $pod = $1;
        }

        # Multiply the value by the number of replicas
        $replicas = $resource_information{$key1}{'replicas'};
        if ($replicas eq "-" || $replicas == 0) {
            # Replicas value not set, apply special handling
            if ( grep(/^$pod$/, @replica_dependencies_workers) ) {
                # Special handling if the pod is in the @replica_dependencies_workers array
                $replicas = $worker_cnt;
            } elsif ( exists $replica_dependencies_other_resource{$pod} ) {
                # Special handling if the pod is in the %replica_dependencies_dother_resource hash
                $replicas = return_existing_file_replicas_value($replica_dependencies_other_resource{$pod});
            } elsif ($replicas eq "-") {
                $replicas = 1;
            }
        }
        $value_with_replicas = $value * $replicas;

        $total_required_container_resources{$key2}{$key3} += $value_with_replicas;
        if ($pod ne "") {
            $required_pod_resources{$pod}{$key2}{$key3} += $value_with_replicas;
            $total_required_pod_resources{$key2}{$key3} += $value_with_replicas;
            $required_pod_resources{$pod}{'replicas'} = $replicas;
            if ($key3 eq "cpu") {
                $required_pod_resources{$pod}{$key2}{'cpu_no_replicas'} += $value;
            } elsif ($key3 eq "ephemeral-storage") {
                $required_pod_resources{$pod}{$key2}{'ephemeral-storage_no_replicas'} += $value;
            } elsif ($key3 eq "memory") {
                $required_pod_resources{$pod}{$key2}{'memory_no_replicas'} += $value;
            }
        }
        return $value_with_replicas;
    } else {
        print "DBG:\$required_container_resources{$key1}{$key2}{$key3} does not exist\n" if $debug;
        return 0;
    }
}

# -----------------------------------------------------------------------------
#
# Return existing required_pod_resources value, or value 0 if not existing.
#
sub return_existing_required_pod_resources_value {
    my $key1 = shift;
    my $key2 = shift;
    my $key3 = shift;
    my $value;

    if (exists $required_pod_resources{$key1} && exists $required_pod_resources{$key1}{$key2} && exists $required_pod_resources{$key1}{$key2}{$key3}) {
        $value = $required_pod_resources{$key1}{$key2}{$key3};
        $top_pod_resources{$key2}{$key3} = $value if (!exists $top_pod_resources{$key2} || !exists $top_pod_resources{$key2}{$key3} || $top_pod_resources{$key2}{$key3} < $value);
        return $value;
    } else {
        print "DBG:\$required_pod_resources{$key1}{$key2}{$key3} does not exist\n" if $debug;
        return 0;
    }
}

# -----------------------------------------------------------------------------
#
# Return existing updated_resources value, or value - if not existing.
#
sub return_existing_updated_resources_value {
    my $key1 = shift;
    my $key2 = shift;
    my $key3 = shift;

    if (exists $updated_resources{$key1} && exists $updated_resources{$key1}{$key2} && exists $updated_resources{$key1}{$key2}{$key3}) {
        return $updated_resources{$key1}{$key2}{$key3};
    } else {
        print "DBG:\$updated_resources{$key1}{$key2}{$key3} does not exist\n" if $debug;
        return "-";
    }
}

# -----------------------------------------------------------------------------
#
# Show usage information.
#
sub usage {
    my $description;
    my $ignored = "";

    for my $resource (sort keys %ignore_resource) {
        $ignored .= "    - $resource\n";
    }

    print <<EOF;

Description:
============

This script is used for calculating needed resources based on data provided in
either a packed umbrella file or in values.yaml files in a specified unpacked
umbrella directory structure.
It's recommended to used the packed umbrella file method because it also takes
into account the replicas parameter settings since it uses the helm (version 3)
template function that parse all values.yaml and template files.

NOTE:
(1) This script should be run on either the director node or on a system that
    have direct access with helm command to the node to check.

(2) It only works with helm version 3 (or higher).


Syntax:
=======

$0 [<OPTIONAL>] <MANDTORY>

<MANDTORY> is one of the following parameters:

  -d <dirpath>  | --directory <dirpath>
  -f <filename> | --file <filename>
  -m <string>   | --multiple-files <string>
                  --printout-from-file <filename>

<OPTIONAL> are one or more of the following parameters:

                  --dot-separated-to-stderr
  -e <path>     | --helm-executable <path>
  -h            | --help
                  --hide-fixable-errors
                  --human-formatting
  -i            | --ignore-resources
  -j            | --include-job
                  --kubeconfig <filename>
  -l <filename> | --log-file <filename>
  -v <filename> | --value-file <filename>
  -w <integer>  | --worker-count <integer>

where:

  -d <dirpath>
  --directory <dirpath>
  ---------------------
  When specified it should point to a directory that contains the whole directory
  structure of an unpacked eric-sc-umbrella*.tgz file and it will look for files
  named 'values.yaml' in this directory structure.
  Either this parameter or the --file parameter must be specified, but not both.


  --dot-separated-to-stderr
  -------------------------
  If specified it will generate a dot separated output of the parsed template
  data to STDERR. This might be useful for debugging of the script and could
  be redirected to file using e.g. the '2>/path/to/file' parameter.


  -e <path>
  --helm-executable <path>
  ------------------------
  When specified it should point to the helm executable to be used.
  If not specified it is assumed that the executable is named 'helm' and it
  must exist in the \$PATH.


  -f <filename>
  --file <filename>
  -----------------
  When specified it should point to a packed umbrella 'eric-sc-umbrella*.tgz'
  file.
  Either this parameter or the --directory parameter must be specified, but not
  both.


  -h
  --help
  ------
  Shows this help information.


  --hide-fixable-errors
  ---------------------
  When specified and no log file is specified with parameter --log-file then
  if there are parsing errors that can be fixed by indenting the code then
  those errors will not be shown to the user. But if the errors cannot be
  fixed by indenting the code then they are always shown.
  If a log file is specified then the parsing errors are only written to the
  log file and not shown to the user.


  --human-formatting
  ------------------
  When specified then all memory and ephemeral disk usage figures will be
  printed in a human easier to read format e.g. 1024 will be printed as 1Ki
  which makes it easier to read large numbers.
  By default if not specified then the full numbers are printed.


  -i
  --ignore-resources
  ------------------
  If specified it will not include the following resources in the calculation:
$ignored


  -j
  --include-job
  -------------
  If specified then also the 'kind' Job is included in the calculations.
  By default Jobs are not included in the resource calculations.


  --kubeconfig <filename>
  -----------------------
  If specified then it will use this file as the config file for helm and
  kubectl commands.
  If not specified then it will use the default file which is normally the
  file ~/.kube/config.


  -l <filename>
  --log-file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  If not specified then no log file is created and only what is printed to STDOUT
  will be shown.


  -m <string>
  --multiple-files <string>
  -------------------------
  When specified it will calculate the resource requirements for multiple helm
  charts and the format of <string> should be as follows:
  <umbrella file>,<values file 1>[<values file 2>[,<values file 3>]etc.]
  Where <umbrella file> should contain the file path to the packed umbrella file
  (.tgz) and <value file 1...> should contain the file path to the used values
  file (.yaml).
  This parameter can be specified multiple times if resource requirements for
  multiple helm charts should be calculated.
  This parameter cannot be specified together with the --file, --directory,
  --printout-from-file or --value-file parameters.


  --printout-from-file <filename>
  -------------------------------
  If specified it should point to a file that contains the output from the
    helm template .... command

  This option might be nice as a quick way to test changes to this script without
  having to execute any helm command and should then be seen as replacement for the
  -f/--file parameter which is using the 'helm template' command.


  -v <filename>
  --value-file <filename>
  -----------------------
  If resource usage should be either reduced or increased from the values given
  in the umbrella files then this paramater can be given to point to other so
  called 'values.yaml' files.
  This parameter can be given multiple times and if a specific value is updated
  by multiple files, it's always the last mentioned file value that will be used.
  I.e. the order is always like this where the last file values always override
  the previous file values:
    - The individual umbrella 'values.yaml' files for each component.
    - The top level 'values.yaml' file in the umbrella directory.
    - Any other file given with with the --value-file parameter.
  This parameter cannot be specified together with the --multiple-files parameter.

  -w <integer>
  --worker-count <integer>
  ------------------------
  Specifies how many worker nodes should be used for calculation of certain values.
  If not specified it will try to read the number of worker nodes are used by
  the current node by executing 'kubectl get nodes'.


Output:
=======

The script prints the calculated resource usages to STDOUT.


Examples:
=========

  Example 1
  ---------
  Calculate resource usage from values.yaml files inside the current directory.

  $0 --directory .

  Example 2
  ---------
  Calculate resource usage from a file containing output from the 'helm template ...'
  command which might be useful to execute the script on a computer that does not
  have the 'helm' command.

  $0 --printout-from-file ./helm_template_output.txt

  Example 3
  ---------
  Calculate resource usage from a helm chart file and include a special values.yaml file.
  This example will use the 'helm template' command to extract resource information.

  $0 --file ./eric-sc-umbrella-1.2.2+263.tgz --value-file ./eric-sc-values.yaml


Return code:
============

   0: Successful, the execution was successful.
   1: Unsuccessful, some failure was reported.

EOF
}

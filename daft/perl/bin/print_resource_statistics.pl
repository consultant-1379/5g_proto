#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.50
#  Date     : 2024-06-12 16:20:55
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
use General::Logging;
use General::OS_Operations;

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $all_namespaces = 0;
my $all_limits_cpu = 0;
my $all_limits_memory = 0;
my $all_limits_storage = 0;
my $all_requests_cpu = 0;
my $all_requests_memory = 0;
my $all_requests_storage = 0;
my $all_top_cpu = 0;
my $all_top_memory = 0;
my %availability_statistics;
#   key:
#       total_master_availability_cpu
#       total_master_availability_memory
#       total_master_availability_pods
#       total_master_availability_storage
#       total_worker_availability_cpu
#       total_worker_availability_memory
#       total_worker_availability_pods
#       total_worker_availability_storage
#       top_master_availability_cpu
#       top_master_availability_memory
#       top_master_availability_pods
#       top_master_availability_storage
#       top_worker_availability_cpu
#       top_worker_availability_memory
#       top_worker_availability_pods
#       top_worker_availability_storage
my $container_name;
my $csv_file = "";
my @data;
my @failures_detected = ();
my $formatting_remove_space = 1;    # Change this to 0 if you want "1 Ki" instead of "1Ki"
my $kubeconfig = "";
my $logfile = "";
my $max_container_length = 14;
my $max_namespace_length = 14;
my $max_node_length = 0;
my $max_pod_length = 0;
my $max_pvc_length = 0;
my $namespace = "";
my $namespace_limits_cpu = 0;
my $namespace_limits_memory = 0;
my $namespace_limits_storage = 0;
my $namespace_requests_cpu = 0;
my $namespace_requests_memory = 0;
my $namespace_requests_storage = 0;
my $namespace_top_cpu = 0;
my $namespace_top_memory = 0;
my $no_formatting = 0;
my $node;
my @nodes = ();
my @nodes_master = ();
my @nodes_worker = ();
my %node_statistics;
#   1st Key: node name
#   2nd Key:
#       allocatable_cpu
#       allocatable_memory
#       allocatable_pods
#       allocatable_storage
#       allocated_limits_cpu
#       allocated_limits_memory
#       allocated_limits_storage
#       allocated_requests_cpu
#       allocated_requests_memory
#       allocated_requests_storage
#       capacity_cpu
#       capacity_memory
#       capacity_pods
#       capacity_storage
#       none_terminated_pods
#       pod_count
#       pod_limits_cpu
#       pod_requests_cpu
#       pod_limits_memory
#       pod_requests_memory
#       pod_limits_storage
#       pod_requests_storage
#       top_cpu
#       top_cpu_percent
#       top_memory
#       top_memory_percent
my $node_role = "all";
my $output_format = "table";
my $pod_name;
my %pod_statistics;
#   1st Key: namespace
#   2nd Key: pod name
#   3rd Key: container name
#   4th Key: 'limits', 'node', 'requests', 'state' or 'top'.
#   5th Key: 'cpu', 'memory' or 'ephemeral-storage' (only when 4th key is 'limits', 'requests' or 'top')
my $pvc_capacity;
my $pvc_name;
my %pvc_statistics;
#   1st Key: namespace
#   2nd Key: pvc name
#   3rd Key: mounted by pod name
my $reading_containers = 0;
my $reading_limits = 0;
my $reading_requests = 0;
my $rc;
my @result;
my %role_statistics;
#   1st key: all, master or worker
#   2nd Key:
#       allocatable_cpu
#       allocatable_memory
#       allocatable_pods
#       allocatable_storage
#       allocated_limits_cpu
#       allocated_limits_memory
#       allocated_limits_storage
#       allocated_requests_cpu
#       allocated_requests_memory
#       allocated_requests_storage
#       capacity_cpu
#       capacity_memory
#       capacity_pods
#       capacity_storage
#       none_terminated_pods
#       pod_limits_cpu
#       pod_requests_cpu
#       pod_limits_memory
#       pod_requests_memory
#       pod_limits_storage
#       pod_requests_storage
#       top_cpu
#       top_memory
my $show_help = 0;
my $skip_terminated_pods = 0;   # Change to 1 if Pods in State Terminated should not be counted
my @stderr;
my $value;
my $wanted_namespace = "";

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|all-namespaces"  => \$all_namespaces,
    "c|csv-file=s"      => \$csv_file,
    "h|help"            => \$show_help,
    "l|log-file=s"      => \$logfile,
    "n|namespace=s"     => \$wanted_namespace,
    "o|output-format=s" => \$output_format,
    "r|node-role=s"     => \$node_role,
    "s|skip-terminated" => \$skip_terminated_pods,
    "kubeconfig=s"      => \$kubeconfig,
    "no-formatting"     => \$no_formatting,
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

if ($node_role !~ /^(all|master|worker)$/) {
    print "Incorrect --node-role given, only 'all', 'master' or 'worker' allowed\n";
    exit 1;
}

if ($output_format !~ /^(csv|table)$/) {
    print "Incorrect --output-format given, only 'csv' or 'table' allowed\n";
    exit 1;
}

# Initialize values
for my $role ( qw/all master worker/) {
    for my $type (qw/allocatable_cpu allocatable_memory allocatable_pods allocatable_storage allocated_limits_cpu allocated_limits_memory allocated_limits_storage allocated_requests_cpu allocated_requests_memory allocated_requests_storage capacity_cpu capacity_memory capacity_pods capacity_storage none_terminated_pods pod_limits_cpu pod_requests_cpu pod_limits_memory pod_requests_memory pod_limits_storage pod_requests_storage/) {
        $role_statistics{$role}{$type} = 0;
    }
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

#
# Collect data
#

collect_node_data();

collect_pod_data();

collect_top_node_data();

collect_top_pod_data();

collect_pvc_data();

#
# Print data
#

if ($output_format eq "table") {
    print_collected_pod_data();

    print_collected_pvc_data();
}

if ($csv_file ne "" || $output_format eq "csv") {
    print_collected_pod_data_csv();
}

if ($output_format eq "table") {
    print_collected_node_data();
}

# Stop logging of output to file
if ($logfile) {
    General::Logging::log_disable();
}

if (scalar @failures_detected == 0) {
    exit 0;
} else {
    print "Failure detected while collecting the following information so the printout might not be complete:\n" . join "", @failures_detected;
    exit 2;
}

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
# Sort alphanumeric, i.e. atmport=1, atmport=2, atmport=10
# instead of:             atmport=1, atmport=10, atmport=2
sub alphanum {
    my $a0 = $a;
    my $b0 = $b;
    for (;;)
    {
        my ($a1, $a2) = ($a0 =~ m/^([^\d]+)(.*)/);
        my ($b1, $b2) = ($b0 =~ m/^([^\d]+)(.*)/);
        $a1 = "" if (!defined $a1);
        $b1 = "" if (!defined $b1);
        $a2 = "" if (!defined $a2);
        $b2 = "" if (!defined $b2);
        $a2 = $a0 if (!$a1 and !$a2);
        $b2 = $b0 if (!$b1 and !$b2);
        if ($a1 ne $b1) { return ($a1 cmp $b1); }
        $a0 = $a2;
        $b0 = $b2;
        return 0 if (($a0 eq "") and ($b0 eq ""));

        ($a1, $a2) = ($a0 =~ m/^(\d+)(.*)/);
        ($b1, $b2) = ($b0 =~ m/^(\d+)(.*)/);
        $a1 = "" if (!defined $a1);
        $b1 = "" if (!defined $b1);
        $a2 = "" if (!defined $a2);
        $b2 = "" if (!defined $b2);
        $a2 = $a0 if (!$a1 and !$a2);
        $b2 = $b0 if (!$b1 and !$b2);
        if ($a1 != $b1) { return ($a1 <=> $b1); }
        $a0 = $a2;
        $b0 = $b2;
        return 0 if (($a0 eq "") and ($b0 eq ""));
    }
}

# -----------------------------------------------------------------------------
#
# Collect Node Data
#
sub collect_node_data {
    my $role;
    my $retry_cnt = 2;

    @nodes_master = ADP::Kubernetes_Operations::get_master_nodes();
    for $node (@nodes_master) {
        $max_node_length = length($node) if (length($node) > $max_node_length);
        $node_statistics{$node}{'node_role'} = "master";
    }
    @nodes_worker = ADP::Kubernetes_Operations::get_worker_nodes();
    for $node (@nodes_worker) {
        $max_node_length = length($node) if (length($node) > $max_node_length);
        $node_statistics{$node}{'node_role'} = "worker";
    }
    if ($node_role eq "worker") {
        @nodes = @nodes_worker;
    } elsif ($node_role eq "master") {
        @nodes = @nodes_master;
    } else {
        @nodes = @nodes_master;
        push @nodes, @nodes_worker;
    }

    # Calculate total Capacity och Allocatable
    for $node (@nodes) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl describe node $node" . $kubeconfig,
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@stderr,
            }
        );
        if ($rc != 0) {
            if ($stderr[0] =~ /^Unable to connect to the server: dial tcp .+ i\/o timeout/) {
                # Maybe some temporary error, try again
                if ($retry_cnt == 0) {
                    General::Logging::log_user_error_message("Failed to get node information from node $node, after trying 2 times");
                    next;
                } else {
                    # Try the command again
                    $retry_cnt--;
                    General::Logging::log_write("Failed to get node information from node $node, trying again retry_cnt=$retry_cnt");
                    redo;
                }
            } else {
                General::Logging::log_user_error_message("Failed to get node information from node $node");
                next;
            }
        }
        # Reset the counter
        $retry_cnt = 2;

        $role = $node_statistics{$node}{'node_role'};

        # Set initial values for the node
        $node_statistics{$node}{'allocatable_cpu'} = 0;
        $node_statistics{$node}{'allocatable_memory'} = 0;
        $node_statistics{$node}{'allocatable_pods'} = 0;
        $node_statistics{$node}{'allocatable_storage'} = 0;
        $node_statistics{$node}{'allocated_limits_cpu'} = 0;
        $node_statistics{$node}{'allocated_limits_memory'} = 0;
        $node_statistics{$node}{'allocated_limits_storage'} = 0;
        $node_statistics{$node}{'allocated_requests_cpu'} = 0;
        $node_statistics{$node}{'allocated_requests_memory'} = 0;
        $node_statistics{$node}{'allocated_requests_storage'} = 0;
        $node_statistics{$node}{'capacity_cpu'} = 0;
        $node_statistics{$node}{'capacity_memory'} = 0;
        $node_statistics{$node}{'capacity_pods'} = 0;
        $node_statistics{$node}{'capacity_storage'} = 0;
        $node_statistics{$node}{'none_terminated_pods'} = 0;
        $node_statistics{$node}{'pod_limits_cpu'} = 0;
        $node_statistics{$node}{'pod_requests_cpu'} = 0;
        $node_statistics{$node}{'pod_limits_memory'} = 0;
        $node_statistics{$node}{'pod_requests_memory'} = 0;
        $node_statistics{$node}{'pod_limits_storage'} = 0;
        $node_statistics{$node}{'pod_requests_storage'} = 0;

        #
        # Check Allocatable figures
        #
        @data = ADP::Kubernetes_Operations::get_top_level_section_information("Allocatable", \@result);
        for (@data) {
            if (/^\s+cpu:\s+(\S+)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'allocatable_cpu'} = $value;
                $role_statistics{$role}{'allocatable_cpu'} += $value;
                $role_statistics{'all'}{'allocatable_cpu'} += $value;
            } elsif (/^\s+ephemeral-storage:\s+(\S+)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'allocatable_storage'} = $value;
                $role_statistics{$role}{'allocatable_storage'} += $value;
                $role_statistics{'all'}{'allocatable_storage'} += $value;
            } elsif (/^\s+memory:\s+(\S+)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'allocatable_memory'} = $value;
                $role_statistics{$role}{'allocatable_memory'} += $value;
                $role_statistics{'all'}{'allocatable_memory'} += $value;
            } elsif (/^\s+pods:\s+(\d+)\s*$/) {
                $value = $1;
                $node_statistics{$node}{'allocatable_pods'} = $value;
                $role_statistics{$role}{'allocatable_pods'} += $value;
                $role_statistics{'all'}{'allocatable_pods'} += $value;
            }
        }

        #
        # Check Capacity figures
        #
        @data = ADP::Kubernetes_Operations::get_top_level_section_information("Capacity", \@result);
        for (@data) {
            if (/^\s+cpu:\s+(\S+)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'capacity_cpu'} = $value;
                $role_statistics{$role}{'capacity_cpu'} += $value;
                $role_statistics{'all'}{'capacity_cpu'} += $value;
            } elsif (/^\s+ephemeral-storage:\s+(\S+)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'capacity_storage'} = $value;
                $role_statistics{$role}{'capacity_storage'} += $value;
                $role_statistics{'all'}{'capacity_storage'} += $value;
            } elsif (/^\s+memory:\s+(\S+)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'capacity_memory'} = $value;
                $role_statistics{$role}{'capacity_memory'} += $value;
                $role_statistics{'all'}{'capacity_memory'} += $value;
            } elsif (/^\s+pods:\s+(\d+)\s*$/) {
                $value = $1;
                $node_statistics{$node}{'capacity_pods'} = $value;
                $role_statistics{$role}{'capacity_pods'} += $value;
                $role_statistics{'all'}{'capacity_pods'} += $value;
            }
        }

        #
        # Check Allocated resources figures
        #
        @data = ADP::Kubernetes_Operations::get_top_level_section_information("Allocated resources", \@result);
        # Allocated resources:
        #   (Total limits may be over 100 percent, i.e., overcommitted.)
        #   Resource           Requests     Limits
        #   --------           --------     ------
        #   cpu                1005m (25%)  200m (5%)
        #   memory             150Mi (1%)   370Mi (2%)
        #   ephemeral-storage  0 (0%)       0 (0%)
        for (@data) {
            if (/^\s+cpu\s+(\S+)\s+\((\d+)\%\)\s+(\S+)\s+\((\d+)\%\)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'allocated_requests_cpu'} = $value;
                $role_statistics{$role}{'allocated_requests_cpu'} += $value;
                $role_statistics{'all'}{'allocated_requests_cpu'} += $value;

                $value = ADP::Kubernetes_Operations::convert_number_unit($3);
                $node_statistics{$node}{'allocated_limits_cpu'} = $value;
                $role_statistics{$role}{'allocated_limits_cpu'} += $value;
                $role_statistics{'all'}{'allocated_limits_cpu'} += $value;
            } elsif (/^\s+ephemeral-storage\s+(\S+)\s+\((\d+)\%\)\s+(\S+)\s+\((\d+)\%\)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'allocated_requests_storage'} = $value;
                $role_statistics{$role}{'allocated_requests_storage'} += $value;
                $role_statistics{'all'}{'allocated_requests_storage'} += $value;

                $value = ADP::Kubernetes_Operations::convert_number_unit($3);
                $node_statistics{$node}{'allocated_limits_storage'} = $value;
                $role_statistics{$role}{'allocated_limits_storage'} += $value;
                $role_statistics{'all'}{'allocated_limits_storage'} += $value;
            } elsif (/^\s+memory\s+(\S+)\s+\((\d+)\%\)\s+(\S+)\s+\((\d+)\%\)\s*$/) {
                $value = ADP::Kubernetes_Operations::convert_number_unit($1);
                $node_statistics{$node}{'allocated_requests_memory'} = $value;
                $role_statistics{$role}{'allocated_requests_memory'} += $value;
                $role_statistics{'all'}{'allocated_requests_memory'} += $value;

                $value = ADP::Kubernetes_Operations::convert_number_unit($3);
                $node_statistics{$node}{'allocated_limits_memory'} = $value;
                $role_statistics{$role}{'allocated_limits_memory'} += $value;
                $role_statistics{'all'}{'allocated_limits_memory'} += $value;
            }
        }

        #
        # Check Non-terminated Pods
        #
        for (@result) {
            if (/^Non-terminated Pods:\s+\((\d+) in total\)\s*$/) {
                #Non-terminated Pods:          (111 in total)
                $value = $1;
                $node_statistics{$node}{'none_terminated_pods'} = $value;
                $role_statistics{$role}{'none_terminated_pods'} += $value;
                $role_statistics{'all'}{'none_terminated_pods'} += $value;
                last;
            }
        }
    }
}

# -----------------------------------------------------------------------------
#
# Collect Pod Data
#
sub collect_pod_data {
    my $role;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl describe pod" . $kubeconfig . ($all_namespaces ? " -A" : ($wanted_namespace ne "" ? " -n $wanted_namespace" : "")),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@stderr,
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Failed to get pod information, might result in incomplete data in output");
        push @failures_detected, " - Failed to get pod information, might result in incomplete data in output.\n";
    }
    for (@result) {
        if (/^Name:\s+(\S+)\s*$/) {
            $pod_name = $1;
            $max_pod_length = length($pod_name) if (length($pod_name) > $max_pod_length);
            $namespace = "";
            $node = "-";
            $role = "-";
            $reading_containers = 0;
            next;
        } elsif (/^Namespace:\s+(\S+)\s*$/) {
            $namespace = $1;
            $max_namespace_length = length($namespace) if (length($namespace) > $max_namespace_length);
            $pod_statistics{$namespace}{$pod_name}{"-"}{'top'}{'cpu'} = "-";
            $pod_statistics{$namespace}{$pod_name}{"-"}{'top'}{'memory'} = "-";
            next;
        } elsif (/^Node:\s+(\S+)\/\S+\s*$/) {
            $node = $1;
            $role = $node_statistics{$node}{'node_role'};
            $max_node_length = length($node) if (length($node) > $max_node_length);
            next;
        } elsif (/^Init Containers:\s*$/) {
            $reading_containers = 1;
            next;
        } elsif (/^Containers:\s*$/) {
            $reading_containers = 1;
            next;
        } elsif (/^\S+:\s*$/) {
            $reading_containers = 0;
            next;
        }

        if ($reading_containers == 1) {
            if (/^  (\S+):\s*$/) {
                $container_name = $1;
                $max_container_length = length($container_name) if (length($container_name) > $max_container_length);
                $reading_limits = 0;
                $reading_requests = 0;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'cpu'} = 0;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'memory'} = 0;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'ephemeral-storage'} = 0;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'node'} = $node;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'cpu'} = 0;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'memory'} = 0;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'ephemeral-storage'} = 0;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'state'} = "-";
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'cpu'} = 0;
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'memory'} = 0;
                next;
            }
            if (/^    Limits:\s*$/) {
                $reading_limits = 1;
                $reading_requests = 0;
                next;
            } elsif (/^    Requests:\s*$/) {
                $reading_limits = 0;
                $reading_requests = 1;
                next;
            } elsif (/^    State:\s+(\S+)/) {
                $pod_statistics{$namespace}{$pod_name}{$container_name}{'state'} = $1;
            } elsif (/^    \S+:/) {
                $reading_limits = 0;
                $reading_requests = 0;
                next;
            } elsif ($reading_limits == 1) {
                if (/^      (cpu|memory|ephemeral-storage):\s+(\S+)\s*$/) {
                    # Ignore pod containers in certain states because they don't actually consume any resources
                    next if ($skip_terminated_pods == 1 && $pod_statistics{$namespace}{$pod_name}{$container_name}{'state'} =~ /^(Terminated)$/);

                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{$1} = $2;
                    $value = ADP::Kubernetes_Operations::convert_number_unit($2);
                    if ($1 eq "cpu") {
                        $node_statistics{$node}{'pod_limits_cpu'} += $value unless ($node eq "-");
                        $role_statistics{$role}{'pod_limits_cpu'} += $value unless ($role eq "-");
                        $role_statistics{'all'}{'pod_limits_cpu'} += $value unless ($role eq "-");
                    } elsif ($1 eq "memory") {
                        $node_statistics{$node}{'pod_limits_memory'} += $value unless ($node eq "-");
                        $role_statistics{$role}{'pod_limits_memory'} += $value unless ($role eq "-");
                        $role_statistics{'all'}{'pod_limits_memory'} += $value unless ($role eq "-");
                    } else {
                        $node_statistics{$node}{'pod_limits_storage'} += $value unless ($node eq "-");
                        $role_statistics{$role}{'pod_limits_storage'} += $value unless ($role eq "-");
                        $role_statistics{'all'}{'pod_limits_storage'} += $value unless ($role eq "-");
                    }
                }
                next;
            } elsif ($reading_requests == 1) {
                if (/^      (cpu|memory|ephemeral-storage):\s+(\S+)\s*$/) {
                    # Ignore pod containers in certain states because they don't actually consume any resources
                    next if ($skip_terminated_pods == 1 && $pod_statistics{$namespace}{$pod_name}{$container_name}{'state'} =~ /^(Terminated)$/);

                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{$1} = $2;
                    $value = ADP::Kubernetes_Operations::convert_number_unit($2);
                    if ($1 eq "cpu") {
                        $node_statistics{$node}{'pod_requests_cpu'} += $value unless ($node eq "-");
                        $role_statistics{$role}{'pod_requests_cpu'} += $value unless ($role eq "-");
                        $role_statistics{'all'}{'pod_requests_cpu'} += $value unless ($role eq "-");
                    } elsif ($1 eq "memory") {
                        $node_statistics{$node}{'pod_requests_memory'} += $value unless ($node eq "-");
                        $role_statistics{$role}{'pod_requests_memory'} += $value unless ($role eq "-");
                        $role_statistics{'all'}{'pod_requests_memory'} += $value unless ($role eq "-");
                    } else {
                        $node_statistics{$node}{'pod_requests_storage'} += $value unless ($node eq "-");
                        $role_statistics{$role}{'pod_requests_storage'} += $value unless ($role eq "-");
                        $role_statistics{'all'}{'pod_requests_storage'} += $value unless ($role eq "-");
                    }
                }
                next;
            }
        }
    }
}

# -----------------------------------------------------------------------------
#
# Collect Persistent Volume Clains (PVC) Data
#
sub collect_pvc_data {
    my $role;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl describe pvc" . $kubeconfig . ($all_namespaces ? " -A" : ($wanted_namespace ne "" ? " -n $wanted_namespace" : "")),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@stderr,
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Failed to get pvc information, might result in incomplete data in output");
        push @failures_detected, " - Failed to get pvc information, might result in incomplete data in output.\n";
    }
    for (@result) {
        if (/^Name:\s+(\S+)\s*$/) {
            $pvc_name = $1;
            $max_pvc_length = length($pvc_name) if (length($pvc_name) > $max_pvc_length);
            $namespace = "-";
            $pod_name = "-";
            $pvc_capacity = 0;
            next;
        } elsif (/^Namespace:\s+(\S+)\s*$/) {
            $namespace = $1;
            $max_namespace_length = length($namespace) if (length($namespace) > $max_namespace_length);
            next;
        } elsif (/^Capacity:\s+(\S+)\s*$/) {
            $pvc_capacity = ADP::Kubernetes_Operations::convert_number_unit($1);
            next;
        } elsif (/^(Mounted|Used) By:\s+(\S+)\s*$/) {
            $pod_name = $2;
            $max_pod_length = length($pod_name) if (length($pod_name) > $max_pod_length);
            $pvc_statistics{$namespace}{$pvc_name}{$pod_name} += $pvc_capacity;
            next;
        }
    }
}

# -----------------------------------------------------------------------------
#
# Collect top node Data.
#
sub collect_top_node_data {
    my @result = ();
    my $role = "";
    my $value;

    if ($node_role eq "master") {
        @result = ADP::Kubernetes_Operations::get_node_top_master();
    } elsif ($node_role eq "worker") {
        @result = ADP::Kubernetes_Operations::get_node_top_worker();
    } else {
        @result = ADP::Kubernetes_Operations::get_node_top();
    }
    for (@result) {
        if (/^(\S+)\s+(\d+\S*)\s+(\d+\%)\s+(\d+\S*)\s+(\d+\%)\s*$/) {
            # node-10-63-142-76   1118m   13%   8344Mi   28%
            $node_statistics{$1}{'top_cpu'} = "$2";
            $node_statistics{$1}{'top_cpu_percent'} = "$3";
            $node_statistics{$1}{'top_memory'} = "$4";
            $node_statistics{$1}{'top_memory_percent'} = "$5";

            # Update role statistics as well
            $role = $node_statistics{$1}{'node_role'};
            $value = ADP::Kubernetes_Operations::convert_number_unit($2);
            $role_statistics{$role}{'top_cpu'} += $value;
            $role_statistics{'all'}{'top_cpu'} += $value;
            $value = ADP::Kubernetes_Operations::convert_number_unit($4);
            $role_statistics{$role}{'top_memory'} += $value;
            $role_statistics{'all'}{'top_memory'} += $value;
        }
    }
}

# -----------------------------------------------------------------------------
#
# Collect top pod Data.
#
sub collect_top_pod_data {
    # Collect top data for all namespaces
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl top pod -A --containers" . $kubeconfig,
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@stderr,
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Failed to get top pod information, might result in incomplete data in output");
        push @failures_detected, " - Failed to get top pod information, might result in incomplete data in output.\n";
    }
    for (@result) {
        if (/^(\S+)\s+(\S+)\s+(\S+)\s+(\d+\S*)\s+(\d+\S*)\s*$/) {
            # NAMESPACE        POD                                                               NAME                                                          CPU(cores)   MEMORY(bytes)
            # eiffelesc        eric-cm-yang-provider-694659b995-jz7c5                            notification-controller                                       1m           31Mi
            next if ($3 eq "POD");
            $max_namespace_length = length($1) if (length($1) > $max_namespace_length);
            $pod_statistics{$1}{$2}{$3}{'top'}{'cpu'} = $4;
            $pod_statistics{$1}{$2}{$3}{'top'}{'memory'} = $5;
        }
    }
}

# -----------------------------------------------------------------------------
#
# Format the output into a more readable human format. e.g. 1024 will be 1 Ki.
#
sub format_output {
    my $number = shift;

    if ($no_formatting) {
        if (length($number) > 12) {
            # Remove anything behind the decimal point
            if ($number =~ /^(\d+)\.(\d+)$/) {
                $number = $1;
            }
        }
        return $number;
    } else {
        if ($formatting_remove_space) {
            $number = ADP::Kubernetes_Operations::convert_number_to_3_decimals($number);
            $number =~ s/\s+//g;
            return $number;
        } else {
            return ADP::Kubernetes_Operations::convert_number_to_3_decimals($number);
        }
    }
}

# -----------------------------------------------------------------------------
#
# Print Collected Node Data
#
sub print_collected_node_data {
    my $available_resources;

    if ($max_node_length < 12) {
        $max_node_length = 12;  # Length of "All Combined"
    }

    print "Detailed Node Summary:\n";
    print "======================\n";

    #
    # CPU
    #

    print "\nCPU in cpu units:\n";
    printf "%-${max_node_length}s  %-6s  %-7s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s\n",
        "Node",
        "Role",
        "Type",
        "Capacity",
        "Allocatable",
        "Allocated Limits",
        "Allocated Requests",
        "Pod Limits",
        "Pod Requests",
        "Top",
        "Available Resources";
    printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_node_length,
        "-"x6,
        "-"x7,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    for my $node (sort alphanum keys %node_statistics) {
        next if ($node_role ne "all" && $node_role ne $node_statistics{$node}{'node_role'});
        $available_resources = sprintf "%s", round_to_3_decimals($node_statistics{$node}{'allocatable_cpu'} - $node_statistics{$node}{'allocated_requests_cpu'});
        if ($node_statistics{$node}{'node_role'} eq "master") {
            $availability_statistics{'top_master_availability_cpu'} = exists $availability_statistics{'top_master_availability_cpu'} ? ($availability_statistics{'top_master_availability_cpu'} < $available_resources ? $available_resources : $availability_statistics{'top_master_availability_cpu'}) : $available_resources;
        } else {
            $availability_statistics{'top_worker_availability_cpu'} = exists $availability_statistics{'top_worker_availability_cpu'} ? ($availability_statistics{'top_worker_availability_cpu'} < $available_resources ? $available_resources : $availability_statistics{'top_worker_availability_cpu'}) : $available_resources;
        }
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            $node,
            $node_statistics{$node}{'node_role'},
            "CPU",
            $node_statistics{$node}{'capacity_cpu'},
            (sprintf "%s (%i%%)", round_to_3_decimals($node_statistics{$node}{'allocatable_cpu'}), ($node_statistics{$node}{'allocatable_cpu'}/$node_statistics{$node}{'capacity_cpu'})*100),
            (sprintf "%s (%i%%)", round_to_3_decimals($node_statistics{$node}{'allocated_limits_cpu'}), ($node_statistics{$node}{'allocated_limits_cpu'}/$node_statistics{$node}{'allocatable_cpu'})*100),
            (sprintf "%s (%i%%)", round_to_3_decimals($node_statistics{$node}{'allocated_requests_cpu'}), ($node_statistics{$node}{'allocated_requests_cpu'}/$node_statistics{$node}{'allocatable_cpu'})*100),
            (exists $node_statistics{$node}{'pod_limits_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($node_statistics{$node}{'pod_limits_cpu'}), ($node_statistics{$node}{'pod_limits_cpu'}/$node_statistics{$node}{'allocatable_cpu'})*100) : "-"),
            (exists $node_statistics{$node}{'pod_requests_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($node_statistics{$node}{'pod_requests_cpu'}), ($node_statistics{$node}{'pod_requests_cpu'}/$node_statistics{$node}{'allocatable_cpu'})*100) : "-"),
            (exists $node_statistics{$node}{'top_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals(ADP::Kubernetes_Operations::convert_number_unit($node_statistics{$node}{'top_cpu'})), (ADP::Kubernetes_Operations::convert_number_unit($node_statistics{$node}{'top_cpu'})/$node_statistics{$node}{'allocatable_cpu'})*100) : "-"),
            (sprintf "%s (%i%%)", round_to_3_decimals($available_resources), ($available_resources/$node_statistics{$node}{'allocatable_cpu'})*100);
    }
    printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_node_length,
        "-"x6,
        "-"x7,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    if ($node_role eq "all") {
        for my $role ( qw "master worker") {
            # Check if any node exists with this role, if not then skip it
            next if ($role_statistics{$role}{'capacity_cpu'} == 0);

            $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{$role}{'allocatable_cpu'} - $role_statistics{$role}{'allocated_requests_cpu'});
            if ($role eq "master") {
                $availability_statistics{'total_master_availability_cpu'} = $available_resources;
            } else {
                $availability_statistics{'total_worker_availability_cpu'} = $available_resources;
            }
            printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
                "All $role",
                "$role",
                "CPU",
                $role_statistics{$role}{'capacity_cpu'},
                (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$role}{'allocatable_cpu'}), ($role_statistics{$role}{'allocatable_cpu'}/$role_statistics{$role}{'capacity_cpu'})*100),
                (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$role}{'allocated_limits_cpu'}), ($role_statistics{$role}{'allocated_limits_cpu'}/$role_statistics{$role}{'allocatable_cpu'})*100),
                (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$role}{'allocated_requests_cpu'}), ($role_statistics{$role}{'allocated_requests_cpu'}/$role_statistics{$role}{'allocatable_cpu'})*100),
                (exists $role_statistics{$role}{'pod_limits_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$role}{'pod_limits_cpu'}), ($role_statistics{$role}{'pod_limits_cpu'}/$role_statistics{$role}{'allocatable_cpu'})*100) : "-"),
                (exists $role_statistics{$role}{'pod_requests_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$role}{'pod_requests_cpu'}), ($role_statistics{$role}{'pod_requests_cpu'}/$role_statistics{$role}{'allocatable_cpu'})*100) : "-"),
                (exists $role_statistics{$role}{'top_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$role}{'top_cpu'}), ($role_statistics{$role}{'top_cpu'}/$role_statistics{$role}{'allocatable_cpu'})*100) : "-"),
                (sprintf "%s (%i%%)", round_to_3_decimals($available_resources), ($available_resources/$role_statistics{$role}{'allocatable_cpu'})*100);
        }
        $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{'all'}{'allocatable_cpu'} - $role_statistics{'all'}{'allocated_requests_cpu'});
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            "All Combined",
            "Any",
            "CPU",
            $role_statistics{'all'}{'capacity_cpu'},
            (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{'all'}{'allocatable_cpu'}), ($role_statistics{'all'}{'allocatable_cpu'}/$role_statistics{'all'}{'capacity_cpu'})*100),
            (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{'all'}{'allocated_limits_cpu'}), ($role_statistics{'all'}{'allocated_limits_cpu'}/$role_statistics{'all'}{'allocatable_cpu'})*100),
            (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{'all'}{'allocated_requests_cpu'}), ($role_statistics{'all'}{'allocated_requests_cpu'}/$role_statistics{'all'}{'allocatable_cpu'})*100),
            (exists $role_statistics{'all'}{'pod_limits_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{'all'}{'pod_limits_cpu'}), ($role_statistics{'all'}{'pod_limits_cpu'}/$role_statistics{'all'}{'allocatable_cpu'})*100) : "-"),
            (exists $role_statistics{'all'}{'pod_requests_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{'all'}{'pod_requests_cpu'}), ($role_statistics{'all'}{'pod_requests_cpu'}/$role_statistics{'all'}{'allocatable_cpu'})*100) : "-"),
            (exists $role_statistics{'all'}{'top_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{'all'}{'top_cpu'}), ($role_statistics{'all'}{'top_cpu'}/$role_statistics{'all'}{'allocatable_cpu'})*100) : "-"),
            (sprintf "%s (%i%%)", round_to_3_decimals($available_resources), ($available_resources/$role_statistics{'all'}{'allocatable_cpu'})*100);
    } elsif ($role_statistics{$node_role}{'capacity_cpu'} != 0) {
        $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{$node_role}{'allocatable_cpu'} - $role_statistics{$node_role}{'allocated_requests_cpu'});
        if ($node_role eq "master") {
            $availability_statistics{'total_master_availability_cpu'} = $available_resources;
        } else {
            $availability_statistics{'total_worker_availability_cpu'} = $available_resources;
        }
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            "All $node_role",
            "$node_role",
            "CPU",
            $role_statistics{$node_role}{'capacity_cpu'},
            (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$node_role}{'allocatable_cpu'}), ($role_statistics{$node_role}{'allocatable_cpu'}/$role_statistics{$node_role}{'capacity_cpu'})*100),
            (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$node_role}{'allocated_limits_cpu'}), ($role_statistics{$node_role}{'allocated_limits_cpu'}/$role_statistics{$node_role}{'allocatable_cpu'})*100),
            (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$node_role}{'allocated_requests_cpu'}), ($role_statistics{$node_role}{'allocated_requests_cpu'}/$role_statistics{$node_role}{'allocatable_cpu'})*100),
            (exists $role_statistics{$node_role}{'pod_limits_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$node_role}{'pod_limits_cpu'}), ($role_statistics{$node_role}{'pod_limits_cpu'}/$role_statistics{$node_role}{'allocatable_cpu'})*100) : "-"),
            (exists $role_statistics{$node_role}{'pod_requests_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$node_role}{'pod_requests_cpu'}), ($role_statistics{$node_role}{'pod_requests_cpu'}/$role_statistics{$node_role}{'allocatable_cpu'})*100) : "-"),
            (exists $role_statistics{$node_role}{'top_cpu'} ? (sprintf "%s (%i%%)", round_to_3_decimals($role_statistics{$node_role}{'top_cpu'}), ($role_statistics{$node_role}{'top_cpu'}/$role_statistics{$node_role}{'allocatable_cpu'})*100) : "-"),
            (sprintf "%s (%i%%)", round_to_3_decimals($available_resources), ($available_resources/$role_statistics{$node_role}{'allocatable_cpu'})*100);
    }

    #
    # Memory
    #

    print "\nMemory in bytes:\n";
    printf "%-${max_node_length}s  %-6s  %-7s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s\n",
        "Node",
        "Role",
        "Type",
        "Capacity",
        "Allocatable",
        "Allocated Limits",
        "Allocated Requests",
        "Pod Limits",
        "Pod Requests",
        "Top",
        "Available Resources";
    printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_node_length,
        "-"x6,
        "-"x7,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    for my $node (sort alphanum keys %node_statistics) {
        next if ($node_role ne "all" && $node_role ne $node_statistics{$node}{'node_role'});
        $available_resources = sprintf "%s", round_to_3_decimals($node_statistics{$node}{'allocatable_memory'} - $node_statistics{$node}{'allocated_requests_memory'});
        if ($node_statistics{$node}{'node_role'} eq "master") {
            $availability_statistics{'top_master_availability_memory'} = exists $availability_statistics{'top_master_availability_memory'} ? ($availability_statistics{'top_master_availability_memory'} < $available_resources ? $available_resources : $availability_statistics{'top_master_availability_memory'}) : $available_resources;
        } else {
            $availability_statistics{'top_worker_availability_memory'} = exists $availability_statistics{'top_worker_availability_memory'} ? ($availability_statistics{'top_worker_availability_memory'} < $available_resources ? $available_resources : $availability_statistics{'top_worker_availability_memory'}) : $available_resources;
        }
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            $node,
            $node_statistics{$node}{'node_role'},
            "Memory",
            format_output($node_statistics{$node}{'capacity_memory'}),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'allocatable_memory'}), ($node_statistics{$node}{'allocatable_memory'}/$node_statistics{$node}{'capacity_memory'})*100),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'allocated_limits_memory'}), ($node_statistics{$node}{'allocated_limits_memory'}/$node_statistics{$node}{'allocatable_memory'})*100),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'allocated_requests_memory'}), ($node_statistics{$node}{'allocated_requests_memory'}/$node_statistics{$node}{'allocatable_memory'})*100),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'pod_limits_memory'}), ($node_statistics{$node}{'pod_limits_memory'}/$node_statistics{$node}{'allocatable_memory'})*100),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'pod_requests_memory'}), ($node_statistics{$node}{'pod_requests_memory'}/$node_statistics{$node}{'allocatable_memory'})*100),
            (exists $node_statistics{$node}{'top_memory'} ? (sprintf "%s (%s)", format_output(ADP::Kubernetes_Operations::convert_number_unit($node_statistics{$node}{'top_memory'})), $node_statistics{$node}{'top_memory_percent'}) : "-"),
            (sprintf "%s (%i%%)", format_output($available_resources), ($available_resources/$node_statistics{$node}{'allocatable_memory'})*100);
    }
    printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_node_length,
        "-"x6,
        "-"x7,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    if ($node_role eq "all") {
        for my $role ( qw "master worker") {
            # Check if any node exists with this role, if not then skip it
            next if ($role_statistics{$role}{'capacity_memory'} == 0);

            $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{$role}{'allocatable_memory'} - $role_statistics{$role}{'allocated_requests_memory'});
            if ($role eq "master") {
                $availability_statistics{'total_master_availability_memory'} = $available_resources;
            } else {
                $availability_statistics{'total_worker_availability_memory'} = $available_resources;
            }
            printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
                "All $role",
                "$role",
                "Memory",
                format_output($role_statistics{$role}{'capacity_memory'}),
                (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'allocatable_memory'}), ($role_statistics{$role}{'allocatable_memory'}/$role_statistics{$role}{'capacity_memory'})*100),
                (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'allocated_limits_memory'}), ($role_statistics{$role}{'allocated_limits_memory'}/$role_statistics{$role}{'allocatable_memory'})*100),
                (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'allocated_requests_memory'}), ($role_statistics{$role}{'allocated_requests_memory'}/$role_statistics{$role}{'allocatable_memory'})*100),
                (exists $role_statistics{$role}{'pod_limits_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'pod_limits_memory'}), ($role_statistics{$role}{'pod_limits_memory'}/$role_statistics{$role}{'allocatable_memory'})*100) : "-"),
                (exists $role_statistics{$role}{'pod_requests_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'pod_requests_memory'}), ($role_statistics{$role}{'pod_requests_memory'}/$role_statistics{$role}{'allocatable_memory'})*100) : "-"),
                (exists $role_statistics{$role}{'top_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'top_memory'}), ($role_statistics{$role}{'top_memory'}/$role_statistics{$role}{'allocatable_memory'})*100) : "-"),
                (sprintf "%s (%i%%)", format_output($available_resources), ($available_resources/$role_statistics{$role}{'allocatable_memory'})*100);
        }
        $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{'all'}{'allocatable_memory'} - $role_statistics{'all'}{'allocated_requests_memory'});
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            "All Combined",
            "Any",
            "Memory",
            format_output($role_statistics{'all'}{'capacity_memory'}),
            (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'allocatable_memory'}), ($role_statistics{'all'}{'allocatable_memory'}/$role_statistics{'all'}{'capacity_memory'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'allocated_limits_memory'}), ($role_statistics{'all'}{'allocated_limits_memory'}/$role_statistics{'all'}{'allocatable_memory'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'allocated_requests_memory'}), ($role_statistics{'all'}{'allocated_requests_memory'}/$role_statistics{'all'}{'allocatable_memory'})*100),
            (exists $role_statistics{'all'}{'pod_limits_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'pod_limits_memory'}), ($role_statistics{'all'}{'pod_limits_memory'}/$role_statistics{'all'}{'allocatable_memory'})*100) : "-"),
            (exists $role_statistics{'all'}{'pod_requests_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'pod_requests_memory'}), ($role_statistics{'all'}{'pod_requests_memory'}/$role_statistics{'all'}{'allocatable_memory'})*100) : "-"),
            (exists $role_statistics{'all'}{'top_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'top_memory'}), ($role_statistics{'all'}{'top_memory'}/$role_statistics{'all'}{'allocatable_memory'})*100) : "-"),
            (sprintf "%s (%i%%)", format_output($available_resources), ($available_resources/$role_statistics{'all'}{'allocatable_memory'})*100);
    } elsif ($role_statistics{$node_role}{'capacity_memory'} != 0) {
        $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{$node_role}{'allocatable_memory'} - $role_statistics{$node_role}{'allocated_requests_memory'});
        if ($node_role eq "master") {
            $availability_statistics{'total_master_availability_memory'} = $available_resources;
        } else {
            $availability_statistics{'total_worker_availability_memory'} = $available_resources;
        }
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            "All $node_role",
            "$node_role",
            "Memory",
            format_output($role_statistics{$node_role}{'capacity_memory'}),
            (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'allocatable_memory'}), ($role_statistics{$node_role}{'allocatable_memory'}/$role_statistics{$node_role}{'capacity_memory'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'allocated_limits_memory'}), ($role_statistics{$node_role}{'allocated_limits_memory'}/$role_statistics{$node_role}{'allocatable_memory'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'allocated_requests_memory'}), ($role_statistics{$node_role}{'allocated_requests_memory'}/$role_statistics{$node_role}{'allocatable_memory'})*100),
            (exists $role_statistics{$node_role}{'pod_limits_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'pod_limits_memory'}), ($role_statistics{$node_role}{'pod_limits_memory'}/$role_statistics{$node_role}{'allocatable_memory'})*100) : "-"),
            (exists $role_statistics{$node_role}{'pod_requests_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'pod_requests_memory'}), ($role_statistics{$node_role}{'pod_requests_memory'}/$role_statistics{$node_role}{'allocatable_memory'})*100) : "-"),
            (exists $role_statistics{$node_role}{'top_memory'} ? (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'top_memory'}), ($role_statistics{$node_role}{'top_memory'}/$role_statistics{$node_role}{'allocatable_memory'})*100) : "-"),
            (sprintf "%s (%i%%)", format_output($available_resources), ($available_resources/$role_statistics{$node_role}{'allocatable_memory'})*100);
    }

    #
    # Storage
    #

    print "\nStorage in bytes:\n";
    printf "%-${max_node_length}s  %-6s  %-7s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s\n",
        "Node",
        "Role",
        "Type",
        "Capacity",
        "Allocatable",
        "Allocated Limits",
        "Allocated Requests",
        "Pod Limits",
        "Pod Requests",
        "Top",
        "Available Resources";
    printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_node_length,
        "-"x6,
        "-"x7,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    for my $node (sort alphanum keys %node_statistics) {
        next if ($node_role ne "all" && $node_role ne $node_statistics{$node}{'node_role'});
        $available_resources = sprintf "%s", round_to_3_decimals($node_statistics{$node}{'allocatable_storage'} - $node_statistics{$node}{'allocated_requests_storage'});
        if ($node_statistics{$node}{'node_role'} eq "master") {
            $availability_statistics{'top_master_availability_storage'} = exists $availability_statistics{'top_master_availability_storage'} ? ($availability_statistics{'top_master_availability_storage'} < $available_resources ? $available_resources : $availability_statistics{'top_master_availability_storage'}) : $available_resources;
        } else {
            $availability_statistics{'top_worker_availability_storage'} = exists $availability_statistics{'top_worker_availability_storage'} ? ($availability_statistics{'top_worker_availability_storage'} < $available_resources ? $available_resources : $availability_statistics{'top_worker_availability_storage'}) : $available_resources;
        }
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            $node,
            $node_statistics{$node}{'node_role'},
            "Storage",
            format_output($node_statistics{$node}{'capacity_storage'}),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'allocatable_storage'}), ($node_statistics{$node}{'allocatable_storage'}/$node_statistics{$node}{'capacity_storage'})*100),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'allocated_limits_storage'}), ($node_statistics{$node}{'allocated_limits_storage'}/$node_statistics{$node}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'allocated_requests_storage'}), ($node_statistics{$node}{'allocated_requests_storage'}/$node_statistics{$node}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'pod_limits_storage'}), ($node_statistics{$node}{'pod_limits_storage'}/$node_statistics{$node}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($node_statistics{$node}{'pod_requests_storage'}), ($node_statistics{$node}{'pod_requests_storage'}/$node_statistics{$node}{'allocatable_storage'})*100),
            "-",
            (sprintf "%s (%i%%)", format_output($available_resources), ($available_resources/$node_statistics{$node}{'allocatable_storage'})*100);
    }
    printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
        "-"x$max_node_length,
        "-"x6,
        "-"x7,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    if ($node_role eq "all") {
        for my $role ( qw "master worker") {
            # Check if any node exists with this role, if not then skip it
            next if ($role_statistics{$role}{'capacity_storage'} == 0);

            $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{$role}{'allocatable_storage'} - $role_statistics{$role}{'allocated_requests_storage'});
            if ($role eq "master") {
                $availability_statistics{'total_master_availability_storage'} = $available_resources;
            } else {
                $availability_statistics{'total_worker_availability_storage'} = $available_resources;
            }
            printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
                "All $role",
                "$role",
                "Storage",
                format_output($role_statistics{$role}{'capacity_storage'}),
                (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'allocatable_storage'}), ($role_statistics{$role}{'allocatable_storage'}/$role_statistics{$role}{'capacity_storage'})*100),
                (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'allocated_limits_storage'}), ($role_statistics{$role}{'allocated_limits_storage'}/$role_statistics{$role}{'allocatable_storage'})*100),
                (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'allocated_requests_storage'}), ($role_statistics{$role}{'allocated_requests_storage'}/$role_statistics{$role}{'allocatable_storage'})*100),
                (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'pod_limits_storage'}), ($role_statistics{$role}{'pod_limits_storage'}/$role_statistics{$role}{'allocatable_storage'})*100),
                (sprintf "%s (%i%%)", format_output($role_statistics{$role}{'pod_requests_storage'}), ($role_statistics{$role}{'pod_requests_storage'}/$role_statistics{$role}{'allocatable_storage'})*100),
                "-",
                (sprintf "%s (%i%%)", format_output($available_resources), ($available_resources/$role_statistics{$role}{'allocatable_storage'})*100);
        }
        $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{'all'}{'allocatable_storage'} - $role_statistics{'all'}{'allocated_requests_storage'});
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            "All Combined",
            "Any",
            "Storage",
            format_output($role_statistics{'all'}{'capacity_storage'}),
            (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'allocatable_storage'}), ($role_statistics{'all'}{'allocatable_storage'}/$role_statistics{'all'}{'capacity_storage'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'allocated_limits_storage'}), ($role_statistics{'all'}{'allocated_limits_storage'}/$role_statistics{'all'}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'allocated_requests_storage'}), ($role_statistics{'all'}{'allocated_requests_storage'}/$role_statistics{'all'}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'pod_limits_storage'}), ($role_statistics{'all'}{'pod_limits_storage'}/$role_statistics{'all'}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{'all'}{'pod_requests_storage'}), ($role_statistics{'all'}{'pod_requests_storage'}/$role_statistics{'all'}{'allocatable_storage'})*100),
            "-",
            (sprintf "%s (%i%%)", format_output($available_resources), ($available_resources/$role_statistics{'all'}{'allocatable_storage'})*100);
    } elsif ($role_statistics{$node_role}{'capacity_storage'} != 0) {
        $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{$node_role}{'allocatable_storage'} - $role_statistics{$node_role}{'allocated_requests_storage'});
        if ($node_role eq "master") {
            $availability_statistics{'total_master_availability_storage'} = $available_resources;
        } else {
            $availability_statistics{'total_worker_availability_storage'} = $available_resources;
        }
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s\n",
            "All $node_role",
            "$node_role",
            "Storage",
            format_output($role_statistics{$node_role}{'capacity_storage'}),
            (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'allocatable_storage'}), ($role_statistics{$node_role}{'allocatable_storage'}/$role_statistics{$node_role}{'capacity_storage'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'allocated_limits_storage'}), ($role_statistics{$node_role}{'allocated_limits_storage'}/$role_statistics{$node_role}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'allocated_requests_storage'}), ($role_statistics{$node_role}{'allocated_requests_storage'}/$role_statistics{$node_role}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'pod_limits_storage'}), ($role_statistics{$node_role}{'pod_limits_storage'}/$role_statistics{$node_role}{'allocatable_storage'})*100),
            (sprintf "%s (%i%%)", format_output($role_statistics{$node_role}{'pod_requests_storage'}), ($role_statistics{$node_role}{'pod_requests_storage'}/$role_statistics{$node_role}{'allocatable_storage'})*100),
            "-",
            (sprintf "%s (%i%%)", format_output($available_resources), ($available_resources/$role_statistics{$node_role}{'allocatable_storage'})*100);
    }


    #
    # Pods
    #

    print "\nPods:\n";
    printf "%-${max_node_length}s  %-6s  %-7s  %-20s  %-20s  %-20s  %-20s\n",
        "Node",
        "Role",
        "Type",
        "Capacity",
        "Allocatable",
        "None-Terminated Pods",
        "Available Resources";
    printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s\n",
        "-"x$max_node_length,
        "-"x6,
        "-"x7,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    for my $node (sort alphanum keys %node_statistics) {
        next if ($node_role ne "all" && $node_role ne $node_statistics{$node}{'node_role'});
        $available_resources = sprintf "%s", round_to_3_decimals($node_statistics{$node}{'allocatable_pods'} - $node_statistics{$node}{'none_terminated_pods'});
        if ($node_statistics{$node}{'node_role'} eq "master") {
            $availability_statistics{'top_master_availability_pods'} = exists $availability_statistics{'top_master_availability_pods'} ? ($availability_statistics{'top_master_availability_pods'} < $available_resources ? $available_resources : $availability_statistics{'top_master_availability_pods'}) : $available_resources;
        } else {
            $availability_statistics{'top_worker_availability_pods'} = exists $availability_statistics{'top_worker_availability_pods'} ? ($availability_statistics{'top_worker_availability_pods'} < $available_resources ? $available_resources : $availability_statistics{'top_worker_availability_pods'}) : $available_resources;
        }
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s\n",
            $node,
            $node_statistics{$node}{'node_role'},
            "Pods",
            $node_statistics{$node}{'capacity_pods'},
            (sprintf "%s (%i%%)", $node_statistics{$node}{'allocatable_pods'}, ($node_statistics{$node}{'allocatable_pods'}/$node_statistics{$node}{'capacity_pods'})*100),
            (sprintf "%s (%i%%)", $node_statistics{$node}{'none_terminated_pods'}, ($node_statistics{$node}{'none_terminated_pods'}/$node_statistics{$node}{'allocatable_pods'})*100),
            (sprintf "%s (%i%%)", $available_resources, ($available_resources/$node_statistics{$node}{'allocatable_pods'})*100);
    }
    printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s\n",
        "-"x$max_node_length,
        "-"x6,
        "-"x7,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20;
    if ($node_role eq "all") {
        for my $role ( qw "master worker") {
            # Check if any node exists with this role, if not then skip it
            next if ($role_statistics{$role}{'capacity_pods'} == 0);

            $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{$role}{'allocatable_pods'} - $role_statistics{$role}{'none_terminated_pods'});
            if ($role eq "master") {
                $availability_statistics{'total_master_availability_pods'} = $available_resources;
            } else {
                $availability_statistics{'total_worker_availability_pods'} = $available_resources;
            }
            printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s\n",
                "All $role",
                "$role",
                "Pods",
                $role_statistics{$role}{'capacity_pods'},
                (sprintf "%s (%i%%)", $role_statistics{$role}{'allocatable_pods'}, ($role_statistics{$role}{'allocatable_pods'}/$role_statistics{$role}{'capacity_pods'})*100),
                (sprintf "%s (%i%%)", $role_statistics{$role}{'none_terminated_pods'}, ($role_statistics{$role}{'none_terminated_pods'}/$role_statistics{$role}{'allocatable_pods'})*100),
                (sprintf "%s (%i%%)", $available_resources, ($available_resources/$role_statistics{$role}{'allocatable_pods'})*100);
        }
        $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{'all'}{'allocatable_pods'} - $role_statistics{'all'}{'none_terminated_pods'});
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s\n",
            "All Combined",
            "Any",
            "Pods",
            $role_statistics{'all'}{'capacity_pods'},
            (sprintf "%s (%i%%)", $role_statistics{'all'}{'allocatable_pods'}, ($role_statistics{'all'}{'allocatable_pods'}/$role_statistics{'all'}{'capacity_pods'})*100),
            (sprintf "%s (%i%%)", $role_statistics{'all'}{'none_terminated_pods'}, ($role_statistics{'all'}{'none_terminated_pods'}/$role_statistics{'all'}{'allocatable_pods'})*100),
            (sprintf "%s (%i%%)", $available_resources, ($available_resources/$role_statistics{'all'}{'allocatable_pods'})*100);
    } elsif ($role_statistics{$node_role}{'capacity_pods'} != 0) {
        $available_resources = sprintf "%s", round_to_3_decimals($role_statistics{$node_role}{'allocatable_pods'} - $role_statistics{$node_role}{'none_terminated_pods'});
        if ($node_role eq "master") {
            $availability_statistics{'total_master_availability_pods'} = $available_resources;
        } else {
            $availability_statistics{'total_worker_availability_pods'} = $available_resources;
        }
        printf "%-${max_node_length}s  %-6s  %-7s  %20s  %20s  %20s  %20s\n",
            "All $node_role",
            "$node_role",
            "Pods",
            $role_statistics{$node_role}{'capacity_pods'},
            (sprintf "%s (%i%%)", $role_statistics{$node_role}{'allocatable_pods'}, ($role_statistics{$node_role}{'allocatable_pods'}/$role_statistics{$node_role}{'capacity_pods'})*100),
            (sprintf "%s (%i%%)", $role_statistics{$node_role}{'none_terminated_pods'}, ($role_statistics{$node_role}{'none_terminated_pods'}/$role_statistics{$node_role}{'allocatable_pods'})*100),
            (sprintf "%s (%i%%)", $available_resources, ($available_resources/$role_statistics{$node_role}{'allocatable_storage'})*100);
    }

    # Fetch all available node resources
    my @master_availability_cpu = ();
    my @master_availability_memory = ();
    my @master_availability_pods = ();
    my @master_availability_storage = ();
    my @worker_availability_cpu = ();
    my @worker_availability_memory = ();
    my @worker_availability_pods = ();
    my @worker_availability_storage = ();
    for my $node (keys %node_statistics) {
        next if ($node_role ne "all" && $node_statistics{$node}{'node_role'} ne $node_role);
        if ($node_statistics{$node}{'node_role'} eq "master") {
            push @master_availability_cpu, round_to_3_decimals($node_statistics{$node}{'allocatable_cpu'} - $node_statistics{$node}{'allocated_requests_cpu'});
            push @master_availability_memory, round_to_3_decimals($node_statistics{$node}{'allocatable_memory'} - $node_statistics{$node}{'allocated_requests_memory'});
            push @master_availability_pods, round_to_3_decimals($node_statistics{$node}{'allocatable_pods'} - $node_statistics{$node}{'none_terminated_pods'});
            push @master_availability_storage, round_to_3_decimals($node_statistics{$node}{'allocatable_storage'} - $node_statistics{$node}{'allocated_requests_storage'});
        } else {
            push @worker_availability_cpu, round_to_3_decimals($node_statistics{$node}{'allocatable_cpu'} - $node_statistics{$node}{'allocated_requests_cpu'});
            push @worker_availability_memory, round_to_3_decimals($node_statistics{$node}{'allocatable_memory'} - $node_statistics{$node}{'allocated_requests_memory'});
            push @worker_availability_pods, round_to_3_decimals($node_statistics{$node}{'allocatable_pods'} - $node_statistics{$node}{'none_terminated_pods'});
            push @worker_availability_storage, round_to_3_decimals($node_statistics{$node}{'allocatable_storage'} - $node_statistics{$node}{'allocated_requests_storage'});
        }
    }
    # Sort in decrementing order
    my $master_availability_cpu;
    for (sort { $b <=> $a } @master_availability_cpu) {
        $master_availability_cpu .= " " . $_;
    }
    my $master_availability_memory;
    for (sort { $b <=> $a } @master_availability_memory) {
        $master_availability_memory .= " " . format_output($_);
    }
    my $master_availability_pods;
    for (sort { $b <=> $a } @master_availability_pods) {
        $master_availability_pods .= " " . $_;
    }
    my $master_availability_storage;
    for (sort { $b <=> $a } @master_availability_storage) {
        $master_availability_storage .= " " . format_output($_);
    }
    my $worker_availability_cpu;
    for (sort { $b <=> $a } @worker_availability_cpu) {
        $worker_availability_cpu .= " " . $_;
    }
    my $worker_availability_memory;
    for (sort { $b <=> $a } @worker_availability_memory) {
        $worker_availability_memory .= " " . format_output($_);
    }
    my $worker_availability_pods;
    for (sort { $b <=> $a } @worker_availability_pods) {
        $worker_availability_pods .= " " . $_;
    }
    my $worker_availability_storage;
    for (sort { $b <=> $a } @worker_availability_storage) {
        $worker_availability_storage .= " " . format_output($_);
    }

    # Print availability statistics for the master and/or worker
    print  "\n***********************\n";
    print  "* Information Summary *\n";
    print  "***********************\n";
    print  "\n";
    print  "Available CPU Resources:\n------------------------\n";
    print  "Total available master node resources CPU:       $availability_statistics{'total_master_availability_cpu'}\n" if (exists $availability_statistics{'total_master_availability_cpu'});
    print  "Highest available master node resources CPU:     $availability_statistics{'top_master_availability_cpu'}\n"   if (exists $availability_statistics{'top_master_availability_cpu'});
    print  "Available master node resources CPU:            $master_availability_cpu\n\n"                                 if (exists $availability_statistics{'top_master_availability_cpu'});
    print  "Total available worker node resources CPU:       $availability_statistics{'total_worker_availability_cpu'}\n" if (exists $availability_statistics{'total_worker_availability_cpu'});
    print  "Highest available worker node resources CPU:     $availability_statistics{'top_worker_availability_cpu'}\n"   if (exists $availability_statistics{'top_worker_availability_cpu'});
    print  "Available worker node resources CPU:            $worker_availability_cpu\n\n"                                 if (exists $availability_statistics{'top_worker_availability_cpu'});

    print  "Available Memory Resources:\n---------------------------\n";
    printf "Total available master node resources Memory:    %s\n",   format_output($availability_statistics{'total_master_availability_memory'}) if (exists $availability_statistics{'total_master_availability_memory'});
    printf "Highest available master node resources Memory:  %s\n", format_output($availability_statistics{'top_master_availability_memory'})     if (exists $availability_statistics{'top_master_availability_memory'});
    print  "Available master node resources Memory:         $master_availability_memory\n\n"                                                      if (exists $availability_statistics{'top_master_availability_memory'});
    printf "Total available worker node resources Memory:    %s\n",   format_output($availability_statistics{'total_worker_availability_memory'}) if (exists $availability_statistics{'total_worker_availability_memory'});
    printf "Highest available worker node resources Memory:  %s\n", format_output($availability_statistics{'top_worker_availability_memory'})     if (exists $availability_statistics{'top_worker_availability_memory'});
    print  "Available worker node resources Memory:         $worker_availability_memory\n\n"                                                      if (exists $availability_statistics{'top_worker_availability_memory'});

    print  "Available Storage Resources:\n----------------------------\n";
    printf "Total available master node resources Storage:   %s\n",   format_output($availability_statistics{'total_master_availability_storage'}) if (exists $availability_statistics{'total_master_availability_storage'});
    printf "Highest available master node resources Storage: %s\n", format_output($availability_statistics{'top_master_availability_storage'})     if (exists $availability_statistics{'top_master_availability_storage'});
    print  "Available master node resources Storage:        $master_availability_storage\n\n"                                                      if (exists $availability_statistics{'top_master_availability_storage'});
    printf "Total available worker node resources Storage:   %s\n",   format_output($availability_statistics{'total_worker_availability_storage'}) if (exists $availability_statistics{'total_worker_availability_storage'});
    printf "Highest available worker node resources Storage: %s\n", format_output($availability_statistics{'top_worker_availability_storage'})     if (exists $availability_statistics{'top_worker_availability_storage'});
    printf "Available worker node resources Storage:        $worker_availability_storage\n\n"                                                      if (exists $availability_statistics{'top_worker_availability_storage'});

    print  "Available Pods Resources:\n-------------------------\n";
    printf "Total available master node resources Pods:      %s\n",   $availability_statistics{'total_master_availability_pods'}                   if (exists $availability_statistics{'total_master_availability_pods'});
    printf "Highest available master node resources Pods:    %s\n", $availability_statistics{'top_master_availability_pods'}                       if (exists $availability_statistics{'top_master_availability_pods'});
    print  "Available master node resources Pods:           $master_availability_pods\n\n"                                                         if (exists $availability_statistics{'top_master_availability_pods'});
    printf "Total available worker node resources Pods:      %s\n",   $availability_statistics{'total_worker_availability_pods'}                   if (exists $availability_statistics{'total_worker_availability_pods'});
    printf "Highest available worker node resources Pods:    %s\n", $availability_statistics{'top_worker_availability_pods'}                       if (exists $availability_statistics{'top_worker_availability_pods'});
    printf "Available worker node resources Pods:           $worker_availability_pods\n\n"                                                         if (exists $availability_statistics{'top_worker_availability_pods'});
}

# -----------------------------------------------------------------------------
#
# Print Collected Pod Data
#
sub print_collected_pod_data {
    print "\nDetailed Pod Summary:\n";
    print "=====================\n\n";

    for $namespace (sort keys %pod_statistics) {

        $namespace_limits_cpu       = 0;
        $namespace_limits_memory    = 0;
        $namespace_limits_storage   = 0;
        $namespace_requests_cpu     = 0;
        $namespace_requests_memory  = 0;
        $namespace_requests_storage = 0;
        $namespace_top_cpu          = 0;
        $namespace_top_memory       = 0;

        next unless ($all_namespaces == 1 || $namespace eq $wanted_namespace);

        printf "%-${max_namespace_length}s  %-${max_pod_length}s  %-${max_container_length}s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-20s  %-${max_node_length}s  %-10s\n",
            "Namespace",
            "Pod",
            "Container",
            "CPU Limits",
            "CPU Requests",
            "CPU Top",
            "Memory Limits",
            "Memory Requests",
            "Memory Top",
            "Storage Limits",
            "Storage Requests",
            "Node",
            "State";
        printf "%-${max_namespace_length}s  %-${max_pod_length}s  %-${max_container_length}s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %-${max_node_length}s  %-10s\n",
            "-"x$max_namespace_length,
            "-"x$max_pod_length,
            "-"x$max_container_length,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x$max_node_length,
            "-"x10;

        for $pod_name (sort keys %{$pod_statistics{$namespace}}) {

            for $container_name (sort keys %{$pod_statistics{$namespace}{$pod_name}}) {
                # Ignore container "-" which includes top data that is only available on the pod and not on container level
                next if ($container_name eq "-");

                # Ignore pod containers in certain states because they don't actually consume any resources
                next if ($skip_terminated_pods == 1 && $pod_statistics{$namespace}{$pod_name}{$container_name}{'state'} =~ /^(Terminated)$/);

                # Add totals
                $namespace_limits_cpu       += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'cpu'});
                $namespace_limits_memory    += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'memory'});
                $namespace_limits_storage   += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'ephemeral-storage'});
                $namespace_requests_cpu     += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'cpu'});
                $namespace_requests_memory  += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'memory'});
                $namespace_requests_storage += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'ephemeral-storage'});
                $namespace_top_cpu          += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'cpu'});
                $namespace_top_memory       += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'memory'});
                $all_limits_cpu             += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'cpu'});
                $all_limits_memory          += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'memory'});
                $all_limits_storage         += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'ephemeral-storage'});
                $all_requests_cpu           += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'cpu'});
                $all_requests_memory        += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'memory'});
                $all_requests_storage       += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'ephemeral-storage'});
                $all_top_cpu                += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'cpu'});
                $all_top_memory             += ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'memory'});

                # Print Pod and Container statistics, add top data for all containers even though it's only available on pod level
                printf "%-${max_namespace_length}s  %-${max_pod_length}s  %-${max_container_length}s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %-${max_node_length}s  %-10s\n",
                    $namespace,
                    $pod_name,
                    $container_name,
                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'cpu'},
                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'cpu'},
                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'cpu'},
                    format_output($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'memory'}),
                    format_output($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'memory'}),
                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'memory'},
                    format_output($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'ephemeral-storage'}),
                    format_output($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'ephemeral-storage'}),
                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'node'},
                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'state'};
            }

        }

        # Print Namespace statistics
        printf "%-${max_namespace_length}s  %-${max_pod_length}s  %-${max_container_length}s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %-${max_node_length}s  %-10s\n",
            "-"x$max_namespace_length,
            "-"x$max_pod_length,
            "-"x$max_container_length,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x20,
            "-"x$max_node_length,
            "-"x10;
        printf "%-${max_namespace_length}s  %-${max_pod_length}s  %-${max_container_length}s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %-${max_node_length}s  %-10s\n\n",
            $namespace,
            "All Pods",
            "All Containers",
            round_to_3_decimals($namespace_limits_cpu),
            round_to_3_decimals($namespace_requests_cpu),
            round_to_3_decimals($namespace_top_cpu),
            format_output($namespace_limits_memory),
            format_output($namespace_requests_memory),
            format_output($namespace_top_memory),
            format_output($namespace_limits_storage),
            format_output($namespace_requests_storage),
            "-",
            "-";
    }

    if ($all_namespaces == 1) {
        # Print All Pod statistics
        printf "%-${max_namespace_length}s  %-${max_pod_length}s  %-${max_container_length}s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %20s  %-${max_node_length}s  %-10s\n\n",
            "All Namespaces",
            "All Pods",
            "All Containers",
            round_to_3_decimals($all_limits_cpu),
            round_to_3_decimals($all_requests_cpu),
            round_to_3_decimals($all_top_cpu),
            format_output($all_limits_memory),
            format_output($all_requests_memory),
            format_output($all_top_memory),
            format_output($all_limits_storage),
            format_output($all_requests_storage),
            "-",
            "-";
    }
}

# -----------------------------------------------------------------------------
#
# Print Collected PVC Data
#
sub print_collected_pvc_data {
    my $capacity_namespace;

    print "Detailed PVC Summary:\n";
    print "=====================\n";

    for $namespace (sort keys %pvc_statistics) {

        next unless ($all_namespaces == 1 || $namespace eq $wanted_namespace);

        $capacity_namespace = 0;

        printf "\n%-${max_namespace_length}s  %-${max_pvc_length}s  %-${max_pod_length}s  %-20s\n",
            "Namespace",
            "Name",
            "Mounted/Used by",
            "Capacity";
        printf "%-${max_namespace_length}s  %-${max_pvc_length}s  %-${max_pod_length}s  %-20s\n",
            "-"x$max_namespace_length,
            "-"x$max_pvc_length,
            "-"x$max_pod_length,
            "-"x20;

        for $pvc_name (sort keys %{$pvc_statistics{$namespace}}) {

            for $pod_name (sort keys %{$pvc_statistics{$namespace}{$pvc_name}}) {
                printf "%-${max_namespace_length}s  %-${max_pvc_length}s  %-${max_pod_length}s  %20s\n",
                    $namespace,
                    $pvc_name,
                    $pod_name,
                    format_output($pvc_statistics{$namespace}{$pvc_name}{$pod_name});

                $capacity_namespace += $pvc_statistics{$namespace}{$pvc_name}{$pod_name};
            }
        }

        printf "%-${max_namespace_length}s  %-${max_pvc_length}s  %-${max_pod_length}s  %-20s\n",
            "-"x$max_namespace_length,
            "-"x$max_pvc_length,
            "-"x$max_pod_length,
            "-"x20;

        printf "%-${max_namespace_length}s  %-${max_pvc_length}s  %-${max_pod_length}s  %20s\n",
            $namespace,
            "All",
            "All",
            format_output($capacity_namespace);
    }
    print "\n";
}

# -----------------------------------------------------------------------------
#
# Print Collected Pod Data
#
sub print_collected_pod_data_csv {
    my $container_limits_cpu;
    my $container_limits_memory;
    my $container_limits_storage;
    my $container_requests_cpu;
    my $container_requests_memory;
    my $container_requests_storage;
    my $container_top_cpu;
    my $container_top_memory;
    my @lines = ();
    my @namespace_lines = ();
    my $pod_limits_cpu;
    my $pod_limits_memory;
    my $pod_limits_storage;
    my $pod_node;
    my $pod_requests_cpu;
    my $pod_requests_memory;
    my $pod_requests_storage;
    my @pod_lines = ();
    my $pod_top_cpu;
    my $pod_top_memory;
    my $pvc_size_all;
    my $pvc_size_namespace;
    my $pvc_size_pod;

    push @lines, '"Namespace","Pod","Container","CPU Limits (cpu)","CPU Requests (cpu)","CPU Top (cpu)","Memory Limits (bytes)","Memory Requests (bytes)","Memory Top (bytes)","Ephemeral-Storage Limits (bytes)","Ephemeral-Storage Requests (bytes)","PVC (bytes)","Node","State"';

    $all_limits_cpu = 0;
    $all_limits_memory = 0;
    $all_limits_storage = 0;
    $all_requests_cpu = 0;
    $all_requests_memory = 0;
    $all_requests_storage = 0;
    $all_top_cpu = 0;
    $all_top_memory = 0;
    $pvc_size_all = 0;

    push @lines, "#";
    push @lines, "# Resource Usage Per Container:";

    for $namespace (sort keys %pod_statistics) {

        $namespace_limits_cpu       = 0;
        $namespace_limits_memory    = 0;
        $namespace_limits_storage   = 0;
        $namespace_requests_cpu     = 0;
        $namespace_requests_memory  = 0;
        $namespace_requests_storage = 0;
        $namespace_top_cpu     = 0;
        $namespace_top_memory  = 0;
        $pvc_size_namespace         = 0;

        next unless ($all_namespaces == 1 || $namespace eq $wanted_namespace);

        for $pod_name (sort keys %{$pod_statistics{$namespace}}) {

            $pod_limits_cpu       = 0;
            $pod_limits_memory    = 0;
            $pod_limits_storage   = 0;
            $pod_node             = "-";
            $pod_requests_cpu     = 0;
            $pod_requests_memory  = 0;
            $pod_requests_storage = 0;
            $pvc_size_pod         = 0;

            for $container_name (sort keys %{$pod_statistics{$namespace}{$pod_name}}) {
                # Ignore container "-" which includes top data that is only available on the pod and not on container level
                next if ($container_name eq "-");

                # Ignore pod containers in certain states because they don't actually consume any resources
                next if ($skip_terminated_pods == 1 && $pod_statistics{$namespace}{$pod_name}{$container_name}{'state'} =~ /^(Terminated)$/);

                $container_limits_cpu       = ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'cpu'});
                $container_limits_memory    = ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'memory'});
                $container_limits_storage   = ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'limits'}{'ephemeral-storage'});
                $container_requests_cpu     = ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'cpu'});
                $container_requests_memory  = ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'memory'});
                $container_requests_storage = ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'requests'}{'ephemeral-storage'});
                $container_top_cpu          = ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'cpu'});
                $container_top_memory       = ADP::Kubernetes_Operations::convert_number_unit($pod_statistics{$namespace}{$pod_name}{$container_name}{'top'}{'memory'});

                # Add totals
                $pod_limits_cpu             += $container_limits_cpu;
                $pod_limits_memory          += $container_limits_memory;
                $pod_limits_storage         += $container_limits_storage;
                $pod_node                   =  $pod_statistics{$namespace}{$pod_name}{$container_name}{'node'},
                $pod_requests_cpu           += $container_requests_cpu;
                $pod_requests_memory        += $container_requests_memory;
                $pod_requests_storage       += $container_requests_storage;
                $pod_top_cpu                += $container_top_cpu;
                $pod_top_memory             += $container_top_memory;
                $namespace_limits_cpu       += $container_limits_cpu;
                $namespace_limits_memory    += $container_limits_memory;
                $namespace_limits_storage   += $container_limits_storage;
                $namespace_requests_cpu     += $container_requests_cpu;
                $namespace_requests_memory  += $container_requests_memory;
                $namespace_requests_storage += $container_requests_storage;
                $namespace_top_cpu          += $container_top_cpu;
                $namespace_top_memory       += $container_top_memory;
                $all_limits_cpu             += $container_limits_cpu;
                $all_limits_memory          += $container_limits_memory;
                $all_limits_storage         += $container_limits_storage;
                $all_requests_cpu           += $container_requests_cpu;
                $all_requests_memory        += $container_requests_memory;
                $all_requests_storage       += $container_requests_storage;
                $all_top_cpu                += $container_top_cpu;
                $all_top_memory             += $container_top_memory;

                # Print Pod and Container statistics, top and pvc data is only available on pod level
                push @lines, sprintf '"%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s"',
                    $namespace,
                    $pod_name,
                    $container_name,
                    $container_limits_cpu,
                    $container_requests_cpu,
                    $container_top_cpu,
                    $container_limits_memory,
                    $container_requests_memory,
                    $container_top_memory,
                    $container_limits_storage,
                    $container_requests_storage,
                    "-",
                    $pod_node,
                    $pod_statistics{$namespace}{$pod_name}{$container_name}{'state'};
            }

            $pvc_size_pod = return_pvc_size_for_pod($namespace, $pod_name),
            $pvc_size_namespace += $pvc_size_pod;
            $pvc_size_all += $pvc_size_pod;

            # Print Pod, top data is only available on pod level
            push @pod_lines, sprintf '"%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s"',
                $namespace,
                $pod_name,
                "All Containers",
                round_to_3_decimals($pod_limits_cpu),
                round_to_3_decimals($pod_requests_cpu),
                round_to_3_decimals($pod_top_cpu),
                $pod_limits_memory,
                $pod_requests_memory,
                $pod_top_memory,
                $pod_limits_storage,
                $pod_requests_storage,
                $pvc_size_pod,
                $pod_node,
                "-";
        }
        push @lines, "#";
        push @pod_lines, "#";

        # Print Namespace statistics
        push @namespace_lines, sprintf '"%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s"',
            $namespace,
            "All Pods",
            "All Containers",
            round_to_3_decimals($namespace_limits_cpu),
            round_to_3_decimals($namespace_requests_cpu),
            round_to_3_decimals($namespace_top_cpu),
            $namespace_limits_memory,
            $namespace_requests_memory,
            $namespace_top_memory,
            $namespace_limits_storage,
            $namespace_requests_storage,
            $pvc_size_namespace,
            "-",
            "-";
    }

    push @lines, "#";
    push @lines, "# Resource Usage Per POD:";
    push @lines, @pod_lines;
    push @lines, "#";
    push @lines, "# Resource Usage Per Namespace:";
    push @lines, @namespace_lines;

    if ($all_namespaces == 1) {
        push @lines, "#";
        push @lines, "# Resource Usage For All Namespaces:";
        # Print All Pod statistics
        push @lines, sprintf '"%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s","%s"',
            "All Namespaces",
            "All Pods",
            "All Containers",
            round_to_3_decimals($all_limits_cpu),
            round_to_3_decimals($all_requests_cpu),
            round_to_3_decimals($all_top_cpu),
            $all_limits_memory,
            $all_requests_memory,
            $all_top_memory,
            $all_limits_storage,
            $all_requests_storage,
            $pvc_size_all,
            "-",
            "-";
    }

    if ($output_format eq "csv") {
        for (@lines) {
            print "$_\n";
        }
    }
    if ($csv_file ne "" && General::File_Operations::write_file(
        {
            "filename"      => $csv_file,
            "output-ref"    => \@lines,
            "eol-char"      => "\n",
        }
    ) != 0) {
        General::Logging::log_user_error_message("Failed to write CSV file '$csv_file'");
    }
}

# -----------------------------------------------------------------------------
#
# Return the PVC data size for a specific namespace and pod.
# If nothing found it will return 0.
#
sub return_pvc_size_for_pod {
    my $namespace = shift;
    my $pod_name = shift;
    my $size = 0;

    return 0 unless exists $pvc_statistics{$namespace};

    for my $pvc_name (keys %{$pvc_statistics{$namespace}}) {
        if (exists $pvc_statistics{$namespace}{$pvc_name}{$pod_name}) {
            $size += $pvc_statistics{$namespace}{$pvc_name}{$pod_name};
        }
    }

    return $size;
}

# -----------------------------------------------------------------------------
#
# Round the value to max 3 decimals.
#
sub round_to_3_decimals {
    my $value = shift;
    return 0 unless defined $value;

    if ($value =~ /^\d+\.\d+$/) {
        $value = sprintf "%.3f", $value;
        $value =~ s/^(\d+\.\d+?)0+$/$1/;
    }
    return $value;
}

# -----------------------------------------------------------------------------
#
# Show usage information.
#
sub usage {
    my $description;

    print <<EOF;

Description:
============

This script is used for printing out resource statistics from the Kubernetes
deployments.
It will report CPU, Memory, Storage and Top statistics for the nodes and pods.

NOTE:
(1) This script should be run on either the director node or on a system that
    have direct access with kubectl command to the node to check.


Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:

  -a            | --all-namespaces
  -c <filename> | --csv-file <filename>
  -h            | --help
                  --kubeconfig <filename>
  -l <filename> | --log-file <filename>
  -n <name>     | --namespace <name>
                  --no-formatting
  -o <format>   | --output-format <format>
  -r <noderole> | --node-role <noderole>
  -s            | --skip-terminated

where:

  -a
  --all-namespaces
  ----------------
  When specified it will print out POD resource statistics from all namespaces
  instead of just one namespace which is the default or the namespace specified
  with parameter --namespace.


  -c <filename>
  --csv-file <filename>
  ---------------------
  If specified then resource usage will also be printed in comma separated format
  (CSV) into the specified file.
  If not specified then no CSV file is created and only what is printed to STDOUT
  will be shown.


  -h
  --help
  ------
  Shows this help information.


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


  -n <name>
  --namespace <name>
  ------------------
  If specified then the Pod resource statistics will be fetched from this
  namespace.
  If not specified then it will fetch Pod resource from the default namespace
  given by the ~/.kube/config file or from all namespaces if --all-namespaces
  is given.


  --no-formatting
  ---------------
  When specified then all numbers are printed in full details.
  If not specified which is the default then all memory and ephemeral disk
  usage figures will be printed in a human easier to read format e.g.
  1024 will be printed as 1Ki which makes it easier to read large numbers.


  -o <format>
  --output-format <format>
  ------------------------
  If specified then the output format can be changed from table to csv formatted
  output.
  The <format> parameter can take the following values:
    - csv
      Output will be in comma separated value format, the same as printed to file
      with the -c/--csv-file but printed to STDOUT.
      NOTE: This output format will not include PVC or node data which is printed
      in 'table' format. So basically the only thing printed is POD statistics.

    - table
      Output will be in table format, this is also the default output format if
      this parameter is not specified.


  -r <noderole>
  --node-role <noderole>
  ----------------------
  Specifies the node roles to fetch resource statistics from.
  Allowed values are:
    - all
    - master
    - worker
  If not specified then 'all' will be used.


  -s
  --skip-terminated
  -----------------
  If specified then Pods and containers in state "Terminated" will not be counted
  in the overall result.
  By default they are counted in the overall utilization even though they might not
  use any resources.


Output:
=======

The script prints the node and pod resource statistics to STDOUT and to the
logfile specified with the --log-file parameter.


Examples:
=========

  Example 1
  ---------
  Show Resource statistics using default namespace.
  $0

  Example 2
  ---------
  Show Resource statistics using default namespace and log to file.
  $0 -l /tmp/$ENV{USER}.log

  Example 3
  ---------
  Show Resource statistics using specified namespace.
  $0 -n 5g-bsf-$ENV{USER}

  Example 4
  ---------
  Show Resource statistics for all namespaces.
  $0 -a


Return code:
============

   0: Successful, the execution was successful.
   1: Unsuccessful, some failure was reported.
   2: Unsuccessful, failure detected when collecting information so printout might not be complete.

EOF
}

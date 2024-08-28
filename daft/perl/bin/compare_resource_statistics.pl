#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.11
#  Date     : 2023-04-14 10:11:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2023
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
use General::File_Operations;

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my %available_after_deployment;
#   1st key: 'Total' or 'Highest'
#   2nd key: 'CPU', 'Memory' or 'Storage'
my %available_before_deployment;
#   1st key: 'Total' or 'Highest'
#   2nd key: 'CPU', 'Memory' or 'Storage'
my $combine_master_and_worker_resources = 0;
my %estimated_requested;
#   1st key: 'Total' or 'Highest'
#   2nd key: 'CPU', 'Memory' or 'Storage'
my $filename_after = "";
my $filename_before = "";
my $filename_check = "";
my $filename_required = "";
my $margin = "-";
my $max_container_length = 8;
my $max_pod_length = 14;
my %required_before_deployment;
#   1st key: 'Total' or 'Highest'
#   2nd key: 'limits' or 'requests'
#   3rd key: 'CPU', 'Memory' or 'Storage'
my %resource_statistics;
my $show_help = 0;
my $wanted_namespace = "";

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|resources-after=s"       => \$filename_after,
    "b|resources-before=s"      => \$filename_before,
    "c|resources-check=s"       => \$filename_check,
    "r|resources-required=s"    => \$filename_required,
    "h|help"                    => \$show_help,
    "n|namespace=s"             => \$wanted_namespace,
);

if ($show_help) {
    usage();
    exit 0;
}

unless (-f "$filename_before") {
    print "Unable to read the $filename_before file\n";
    exit 1;
}

unless (-f "$filename_check") {
    print "Unable to read the $filename_check file\n";
    exit 1;
}

unless (-f "$filename_after") {
    print "Unable to read the $filename_after file\n";
    exit 1;
}

unless (-f "$filename_required") {
    print "Unable to read the $filename_required file\n";
    exit 1;
}

if ($wanted_namespace eq "") {
    print "No namespace was specified\n";
    exit 1;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

read_check_file();

read_before_file();

read_required_file();

read_after_file();

print_detailed_differences();

print_summary_differences();

exit 0;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
#
# Calculate the difference in percent and return with max 1 decimal.
#
sub percent_difference_one_decimal {
    my $new = shift;
    my $orig = shift;

    if ($orig == 0) {
        return 100;
    }
    my $difference = (($new - $orig) / $orig) * 100;
    if ($difference =~ /^\d+$/) {
        return $difference;
    } elsif ($difference =~ /^(\S+)\.0+$/) {
        # All zeros in fraction part, remove it
        return $1;
    } else {
        return sprintf "%.1f", $difference;
    }
}

# -----------------------------------------------------------------------------
#
# Print a detailed list of container differences.
#
sub print_detailed_differences {
    my $after_value;
    my $before_value;
    my $difference;
    my $sign;

    print "Detailed Resource Information Before and After Deployment:\n";
    print "==========================================================\n\n";

    for my $pod_name (sort keys %resource_statistics) {
        printf "%-${max_pod_length}s  %-${max_container_length}s  %-16s  %20s %1s %-20s  %s\n",
            "Pod Name",
            "Container Name",
            "Parameter",
            "Before",
            " ",
            "After",
            "Difference";
        printf "%-${max_pod_length}s  %-${max_container_length}s  %-16s  %20s %1s %-20s  %s\n",
            "-"x$max_pod_length,
            "-"x$max_container_length,
            "-"x16,
            "-"x20,
            "-",
            "-"x20,
            "-"x10;

        for my $container_name (sort keys %{$resource_statistics{$pod_name}}) {
            for my $before_after (sort keys %{$resource_statistics{$pod_name}{$container_name}}) {
                if ($before_after eq "after" && exists $resource_statistics{$pod_name}{$container_name}{'before'}) {
                    # Both exists
                    for my $key (sort keys %{$resource_statistics{$pod_name}{$container_name}{$before_after}}) {
                        $before_value = $resource_statistics{$pod_name}{$container_name}{'before'}{$key};
                        $after_value  = $resource_statistics{$pod_name}{$container_name}{'after'}{$key};
                        if ($before_value == $after_value) {
                            $sign = "=";
                            $difference = "0";
                        } elsif ($before_value eq $after_value) {
                            $sign = "=";
                            $difference = "0";
                        } elsif ($before_value > $after_value) {
                            $sign = ">";
                            $difference = percent_difference_one_decimal($after_value, $before_value);
                        } else {
                            $sign = "<";
                            $difference = percent_difference_one_decimal($after_value, $before_value);
                        }
                        printf "%-${max_pod_length}s  %-${max_container_length}s  %-16s  %20s %1s %-20s  %s\n",
                            $pod_name,
                            $container_name,
                            $key,
                            $before_value,
                            $sign,
                            $after_value,
                            "$difference%";
                    }
                } elsif ($before_after eq "after") {
                    # Only after exists
                    for my $key (sort keys %{$resource_statistics{$pod_name}{$container_name}{$before_after}}) {
                        $after_value  = $resource_statistics{$pod_name}{$container_name}{'after'}{$key};
                        $sign = "!";
                        printf "%-${max_pod_length}s  %-${max_container_length}s  %-16s  %20s %1s %-20s  %s\n",
                            $pod_name,
                            $container_name,
                            $key,
                            "-",
                            $sign,
                            $after_value,
                            "100%";
                    }
                } elsif ($before_after eq "before" && ! exists $resource_statistics{$pod_name}{$container_name}{'after'}) {
                    # Only before exists
                    for my $key (sort keys %{$resource_statistics{$pod_name}{$container_name}{$before_after}}) {
                        $before_value = $resource_statistics{$pod_name}{$container_name}{'before'}{$key};
                        $sign = "!";
                        printf "%-${max_pod_length}s  %-${max_container_length}s  %-16s  %20s %1s %-20s  %s\n",
                            $pod_name,
                            $container_name,
                            $key,
                            $before_value,
                            $sign,
                            "-",
                            "-100%";
                    }
                }
            }
            print "\n";
        }
    }
}

# -----------------------------------------------------------------------------
#
# Print a summary list of container differences.
#
sub print_summary_differences {
    my $divisor;
    my $line = "";
    my $rc = 0;
    my @result;

    #
    # Print summary information
    #

    if ($combine_master_and_worker_resources == 1) {
        $line = "Resource Information on Master and Worker Nodes Before and After Deployment:\n";
        $line .= "============================================================================\n\n";
    } else {
        $line = "Resource Information on Worker Nodes Before and After Deployment:\n";
        $line .= "=================================================================\n\n";
    }

    $line .= sprintf "%-20s  %-20s  %-20s  %-6s  %-20s  %-20s  %-20s  %-20s  %-12s\n",
        "Resource Type",
        "Available Before",
        "Calculated Requested",
        "Margin",
        "Estimated Requested",
        "Estimated After",
        "Real Available After",
        "Difference Real-Est",
        "Difference %";

    $line .= sprintf "%-20s  %20s  %20s  %6s  %20s  %20s  %20s  %20s  %12s\n",
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x6,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x12;

    for my $type ('CPU','Memory','Storage') {
        unless (exists $available_after_deployment{'Total'} &&
            exists $available_after_deployment{'Total'}{$type} &&
            exists $available_before_deployment{'Total'} &&
            exists $available_before_deployment{'Total'}{$type} &&
            exists $estimated_requested{'Total'} &&
            exists $estimated_requested{'Total'}{$type} &&
            exists $required_before_deployment{'Total'}{'requests'} &&
            exists $required_before_deployment{'Total'}{'requests'}{$type}) {

            $line .= sprintf "%-20s  %s\n",
                "$type Resources",
                "Missing data....";
            next;
        }
        #$divisor = $available_after_deployment{'Total'}{$type};

        $line .= sprintf "%-20s  %20s  %20s  %6s  %20s  %20s  %20s  %20s  %11.3f%%\n",
            "$type Resources",
            $available_before_deployment{'Total'}{$type},
            $required_before_deployment{'Total'}{'requests'}{$type},
            $margin,
            $estimated_requested{'Total'}{$type},
            ($available_before_deployment{'Total'}{$type} - $estimated_requested{'Total'}{$type}),
            $available_after_deployment{'Total'}{$type},
            ($available_after_deployment{'Total'}{$type} - ($available_before_deployment{'Total'}{$type} - $estimated_requested{'Total'}{$type})),
            ((($available_after_deployment{'Total'}{$type} - ($available_before_deployment{'Total'}{$type} - $estimated_requested{'Total'}{$type})) / $available_after_deployment{'Total'}{$type}) * 100);
    }

    $line .= sprintf "%-20s  %20s  %20s  %6s  %20s  %20s  %20s  %20s  %12s\n",
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x6,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x20,
        "-"x12;

    print $line;
}

# -----------------------------------------------------------------------------
#
# Read and parse the file containing resource usage after deployment.
#
sub read_after_file {
    my $container_name;
    my $cpu_limits;
    my $cpu_requests;
    my $cpu_top;
    my $memory_limits;
    my $memory_requests;
    my $memory_top;
    my $pod_name;
    my $rc;
    my @result;
    my $storage_limits;
    my $storage_requests;

    $rc = General::File_Operations::read_file(
        {
            "filename"              => $filename_after,
            "output-ref"            => \@result,
        }
    );
    if ($rc != 0) {
        print "Failed to read file '$filename_after'";
        exit 1;
    }

    for (@result) {
        if (/^$wanted_namespace\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+\S+\s+\S+\s*$/) {
            $pod_name         = $1;
            $container_name   = $2;
            $cpu_limits       = ADP::Kubernetes_Operations::convert_number_unit($3);
            $cpu_requests     = ADP::Kubernetes_Operations::convert_number_unit($4);
            $cpu_top          = ADP::Kubernetes_Operations::convert_number_unit($5);
            $memory_limits    = ADP::Kubernetes_Operations::convert_number_unit($6);
            $memory_requests  = ADP::Kubernetes_Operations::convert_number_unit($7);
            $memory_top       = ADP::Kubernetes_Operations::convert_number_unit($8);
            $storage_limits   = ADP::Kubernetes_Operations::convert_number_unit($9);
            $storage_requests = ADP::Kubernetes_Operations::convert_number_unit($10);
            if ($pod_name =~ /^(\S+?)-([a-f0-9]{7,10}-[bcdfghjklmnpqrstvwxz2456789]{5}|[a-f0-9]{8,9}[bcdfghjklmnpqrstvwxz2456789]{5}|[a-z0-9]{14}|[bcdfghjklmnpqrstvwxz2456789]{5}|\d+)$/) {
                # eric-cm-mediator-7b8b8c6844-l2c57
                # eric-cm-mediator-key-init-7kxgj
                # eric-cm-yang-provider-ff648977f-r7gk5
                # eric-log-shipper-6njqf
                # eric-pm-server-0
                # eric-data-search-engine-curator-1612454220-4crdb
                $pod_name = $1;
            }
            $resource_statistics{$pod_name}{$container_name}{'after'}{'Replicas'}++;
            $resource_statistics{$pod_name}{$container_name}{'after'}{'CPU Limits'} += $cpu_limits;
            $resource_statistics{$pod_name}{$container_name}{'after'}{'CPU Requests'} += $cpu_requests;
            $resource_statistics{$pod_name}{$container_name}{'after'}{'Memory Limits'} += $memory_limits;
            $resource_statistics{$pod_name}{$container_name}{'after'}{'Memory Requests'} += $memory_requests;
            $resource_statistics{$pod_name}{$container_name}{'after'}{'Storage Limits'} += $storage_limits;
            $resource_statistics{$pod_name}{$container_name}{'after'}{'Storage Requests'} += $storage_requests;

            $max_pod_length = length($pod_name) if (length($pod_name) > $max_pod_length);
            $max_container_length = length($container_name) if (length($container_name) > $max_container_length);
        } elsif (/^(Total|Highest) available worker node resources (CPU|Memory|Storage):\s+(\S+)/) {
            $available_after_deployment{$1}{$2} += $3;
        } elsif ($combine_master_and_worker_resources == 1 && /^(Total) available master node resources (CPU|Memory|Storage):\s+(\S+)/) {
            $available_after_deployment{$1}{$2} += $3;
        }
    }
}

# -----------------------------------------------------------------------------
#
# Read and parse the file containing resource usage before deployment.
#
sub read_before_file {
    my $rc = 0;
    my @result;

    if (-f "$filename_before") {
        $rc = General::File_Operations::read_file(
            {
                "filename"              => $filename_before,
                "output-ref"            => \@result,
            }
        );
        if ($rc == 0) {
            # Logic:
            #  - Read required resources
            #  - Read available resources
            #  - Calculate expected resources left after deployment
            for (@result) {
                if (/^(Total|Highest) available worker node resources (CPU|Memory|Storage):\s+(\S+)/) {
                    $available_before_deployment{$1}{$2} += $3;
                } elsif ($combine_master_and_worker_resources == 1 && /^(Total) available master node resources (CPU|Memory|Storage):\s+(\S+)/) {
                    $available_before_deployment{$1}{$2} += $3;
                }
            }
        } else {
            print "Cannot compare resources because the following file could not be read:\n$filename_before\n";
            exit 1;
        }
    } else {
        print "Cannot compare resources because the following file is missing:\n$filename_before\n";
        exit 1;
    }
}

# -----------------------------------------------------------------------------
#
# Read and parse the file containing the resource check result before deployment.
#
sub read_check_file {
    my $rc = 0;
    my @result;

    if (-f "$filename_check") {
        $rc = General::File_Operations::read_file(
            {
                "filename"              => $filename_check,
                "output-ref"            => \@result,
            }
        );
        if ($rc == 0) {
            # Logic:
            #  - Read required resources
            #  - Read available resources
            #  - Calculate expected resources left after deployment
            for (@result) {
                if (/^(OK|Not OK)\s+Available worker (CPU|Memory|Storage) resources (\S+) (>=|<) Requested (CPU|Memory|Storage) resources (\S+) including safety margin of (\S+)/) {
                    $estimated_requested{'Total'}{$2} = $6;
                    $margin = $7;
                    $combine_master_and_worker_resources = 0;
                } elsif (/^(OK|Not OK)\s+Available master and worker (CPU|Memory|Storage) resources (\S+) (>=|<) Requested (CPU|Memory|Storage) resources (\S+) including safety margin of (\S+)/) {
                    $estimated_requested{'Total'}{$2} = $6;
                    $margin = $7;
                    $combine_master_and_worker_resources = 1;
                } elsif (/^(OK|Not OK)\s+Highest available worker (CPU|Memory|Storage) resources (\S+) (>=|<) Highest requested (CPU|Memory|Storage) POD resources (\S+) including safety margin of (\S+)/) {
                    $estimated_requested{'Highest'}{$2} = $6;
                }
            }
        } else {
            print "Cannot compare resources because the following file could not be read:\n$filename_check\n";
            exit 1;
        }
    } else {
        print "Cannot compare resources because the following file is missing:\n$filename_check\n";
        exit 1;
    }
}

# -----------------------------------------------------------------------------
#
# Read and parse the file containing resource estimations before deployment.
#
sub read_required_file {
    my $container_name;
    my $cpu_limits;
    my $cpu_requests;
    my $cpu_top;
    my $memory_limits;
    my $memory_requests;
    my $memory_top;
    my $pod_name;
    my $reading_resources = 0;
    my $rc;
    my $replicas;
    my $resource_name;
    my @result;
    my $storage_limits;
    my $storage_requests;

    $rc = General::File_Operations::read_file(
        {
            "filename"              => $filename_required,
            "output-ref"            => \@result,
        }
    );
    if ($rc != 0) {
        print "Failed to read file '$filename_required'";
        exit 1;
    }

    for (@result) {
        if (/^(Total|Highest) required resource (limits|requests) (CPU|Memory|Storage):\s+(\S+)/) {
            $required_before_deployment{$1}{$2}{$3} = $4;
        }

        if (/^Container resource requirements relative to (directory|file) path:.+/) {
            $reading_resources = 1;
        } elsif ($reading_resources == 1) {
            if (/^(\S+?)\.(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+\S+\s*$/) {
                $pod_name         = $1;
                $container_name   = $2;
                $replicas         = $3;
                $cpu_limits       = ADP::Kubernetes_Operations::convert_number_unit($4);
                $memory_limits    = ADP::Kubernetes_Operations::convert_number_unit($5);
                $storage_limits   = ADP::Kubernetes_Operations::convert_number_unit($6);
                $cpu_requests     = ADP::Kubernetes_Operations::convert_number_unit($7);
                $memory_requests  = ADP::Kubernetes_Operations::convert_number_unit($8);
                $storage_requests = ADP::Kubernetes_Operations::convert_number_unit($9);
                if ($replicas eq "-") {
                    # Start by setting it to 1 until special logic is added to set different values depending on pod name
                    $replicas = 1;
                }
                $resource_statistics{$pod_name}{$container_name}{'before'}{'Replicas'} = $replicas;
                $resource_statistics{$pod_name}{$container_name}{'before'}{'CPU Limits'} = $cpu_limits * $replicas;
                $resource_statistics{$pod_name}{$container_name}{'before'}{'CPU Requests'} = $cpu_requests * $replicas;
                $resource_statistics{$pod_name}{$container_name}{'before'}{'Memory Limits'} = $memory_limits * $replicas;
                $resource_statistics{$pod_name}{$container_name}{'before'}{'Memory Requests'} = $memory_requests * $replicas;
                $resource_statistics{$pod_name}{$container_name}{'before'}{'Storage Limits'} = $storage_limits * $replicas;
                $resource_statistics{$pod_name}{$container_name}{'before'}{'Storage Requests'} = $storage_requests * $replicas;

                $max_pod_length = length($pod_name) if (length($pod_name) > $max_pod_length);
                $max_container_length = length($container_name) if (length($container_name) > $max_container_length);
            } elsif (/^Pod resource requirements relative to directory path:\s*$/) {
                $reading_resources = 0;
            }
        }
    }
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

This script is used for comparing the estimated resource usage before doing a
deployment of the SC software against the real usages after finished deployment.

It will read four different logfiles created by the following scripts:
    - calculate_needed_umbrella_resources.pl        (required log)
    - check_if_enough_resources_for_deployment.pl   (check log)
    - print_resource_statistics.pl                  (before and after log)

It will print a detailed list of differences between estimated and real resource
values per container.


Syntax:
=======

$0 [<OPTIONAL>]

<MANDATORY> are all of the following parameters:
  -a <filename> | --resources-after <filename>
  -b <filename> | --resources-before <filename>
  -c <filename> | --resources-check <filename>
  -n <name>     | --namespace <name>
  -r <filename> | --resources-required <filename>

<OPTIONAL> are one or more of the following parameters:

  -h            | --help

where:

  -a <filename>
  --resources-after <filename>
  ----------------------------
  Specifies the logfile containing resource usage printed after deployment
  created by the print_resource_statistics.pl script.


  -b <filename>
  --resources-before <filename>
  -----------------------------
  Specifies the logfile containing resource usage printed before deployment
  created by the print_resource_statistics.pl script.


  -c <filename>
  --resources-check <filename>
  ----------------------------
  Specifies the logfile containing the result of the available resource check
  printed before deployment created by the check_if_enough_resources_for_deployment.pl
  script.


  -h
  --help
  ------
  Shows this help information.


  -n <name>
  --namespace <name>
  ------------------
  Specifies the namespace for which resource statistics will be checked.


  -r <filename>
  --resources-required <filename>
  -------------------------------
  Specifies the logfile containing estimated resource requirements printed before
  deployment created by the calculate_needed_umbrella_resources.pl script.


Output:
=======

The script prints a detailed output of differences between estimated resource requirements
before deployment and the actial used resources after the deployment and show percent
differences.


Examples:
=========

  Example 1
  ---------
  Print the differences for a specific namespace.
  $0 \\
    -b resource_usage_before_deployment.txt \\
    -c resource_usage_before_deployment.txt \\
    -r required_resources_for_deployment.txt \\
    -a resource_usage_after_deployment.txt \\
    -n eiffelesc


Return code:
============

   0: Successful, the execution was successful.
   1: Unsuccessful, some failure was reported.

EOF
}

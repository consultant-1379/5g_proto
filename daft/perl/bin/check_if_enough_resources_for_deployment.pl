#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.9
#  Date     : 2023-04-19 10:53:47
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021,2023
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

use General::File_Operations;

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my %available_node_resources;
#   1st key: 'CPU', 'Memory' or 'Storage'
#   value: The value in CPU units or Bytes.
my $detailed_check = 0;
my %highest_available_resources;
#   1st key: 'master' or 'worker'
#   2nd key: 'CPU', 'Memory' or 'Storage'
#   value: The value in CPU units or Bytes.
my %highest_required_pod_resources;
#   1st key: 'limits' or 'requests'
#   2nd key: 'CPU', 'Memory' or 'Storage'
#   value: The value in CPU units or Bytes.
my $include_master_resources = 0;
my %required_pod_resources;
#   1st key: 'CPU', 'Memory' or 'Storage'
#   value: The value in CPU units or Bytes.
my $required_resources_file = "";
my $return_code = 0;
my $safety_margin = 1;
my $show_help = 0;
my %total_available_resources;
#   1st key: 'master' or 'worker'
#   2nd key: 'CPU', 'Memory' or 'Storage'
#   value: The value in CPU units or Bytes.
my %total_required_resources;
#   1st key: 'limits' or 'requests'
#   2nd key: 'CPU', 'Memory' or 'Storage'
#   value: The value in CPU units or Bytes.
my $used_resources_file = "";
my $warning_only = 0;

# *********************************
# *                               *
# * Parse command line parameters *
# *                               *
# *********************************

use Getopt::Long;          # standard Perl module
GetOptions (
    "d|detailed-check"              => \$detailed_check,
    "h|help"                        => \$show_help,
    "i|include-master-resources"    => \$include_master_resources,
    "r|required-resources-file=s"   => \$required_resources_file,
    "u|used-resources-file=s"       => \$used_resources_file,
    "s|safety-margin=s"             => \$safety_margin,
    "w|warning-only"                => \$warning_only,
);

if ($show_help) {
    usage();
    exit 0;
}

if ($required_resources_file ne "") {
    # Check that the file exists
    unless (-f "$required_resources_file") {
        print "File '$required_resources_file' does not exist or cannot be accessed\n";
        exit 1;
    }
    $required_resources_file = abs_path $required_resources_file;
} else {
    print "You must specify a file with --required-resources-file parameter\n";
    exit 1;
}

if ($used_resources_file ne "") {
    # Check that the file exists
    unless (-f "$used_resources_file") {
        print "File '$used_resources_file' does not exist or cannot be accessed\n";
        exit 1;
    }
    $used_resources_file = abs_path $used_resources_file;
} else {
    print "You must specify a file with --used-resources-file parameter\n";
    exit 1;
}

# Check the safety margin value and convert from % to a value >= 1
if ($safety_margin =~ /^(\S+)\%$/) {
    # A percentage value was given, convert to a value > 1
    $safety_margin = sprintf "%f", (1 + $1/100);
}
if ($safety_margin < 1) {
    print "The safety margin cannot be < 1\n";
    exit 1;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

if ($return_code == 0) {
    $return_code = get_available_node_resources();
}

if ($return_code == 0) {
    $return_code = calculate_needed_resources();
}

if ($return_code == 0) {
    $return_code = check_if_enough_resources_is_available();
}

exit $return_code;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
#
# Calculate the needed resources based on the umbrella and possible other
# eric-sc-values.yaml files.
#
sub calculate_needed_resources {
    my $found_cnt = 0;
    my $rc;
    my @result;

    $rc = General::File_Operations::read_file(
        {
            "filename"              => $required_resources_file,
            "output-ref"            => \@result,
        }
    );
    if ($rc != 0) {
        print "Failed to read the '$required_resources_file' file";
        return 1;
    }

    # Parse the output and look for specific data in the output
    for (@result) {
        if (/^Total required resource (limits|requests) (CPU|Memory|Storage):\s*(\S+)\s*$/) {
            $total_required_resources{$1}{$2} = $3;
            $found_cnt++;
        } elsif (/^Highest required pod resource (limits|requests) (CPU|Memory|Storage):\s*(\S+)\s*$/) {
            $highest_required_pod_resources{$1}{$2} = $3;
            $found_cnt++;
        } elsif (/^Required pod resource requests (CPU|Memory|Storage):\s*(.+)/) {
            $required_pod_resources{$1} = $2;
            $found_cnt++;
        }
    }

    if ($found_cnt == 12 || $found_cnt == 15) {
        return 0;
    } else {
        print "Not all expected values from file '$required_resources_file' was found";
        return 1;
    }
}

# -----------------------------------------------------------------------------
#
#
sub check_all_resources {
    my @available = sort { $b <=> $a } split /\s+/, shift;
    my @requested = sort { $b <=> $a } split /\s+/, shift;
    my $result = "OK";
    my $details = "";

    while (@requested) {
        my $requested = $requested[0];
        if ($requested * $safety_margin <= $available[0]) {
            # Enough space
            shift @requested;
            $available[0] -= $requested;
            # Re-sort the available array
            @available = sort { $b <=> $a } @available;
        } else {
            $result = "Not OK";
            $details = join " ", @requested;
            last;
        }
    }

    return ($result, $details);
}

# -----------------------------------------------------------------------------
#
# Check if the available resources are exceeding the needed resources to allow
# a deployment of the software.
#
sub check_if_enough_resources_is_available {
    my $available;
    my $details;
    my $failed_check = 0;
    my $required;
    my $result;

    print "************************************************************\n";
    print "* Check if enough resources are available for a deployment *\n";
    print "************************************************************\n\n";
    printf "%-6s  %s\n", "Result", "Check performed";
    printf "%-6s  %s\n", "-"x6, "-"x150;

    if ($include_master_resources == 0) {
        # Check that total available resources for the workers is larger
        # than required resources plus a safety margin for the deployment.
        $available = $total_available_resources{'worker'}{'CPU'};
        $required  = $total_required_resources{'requests'}{'CPU'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Available worker CPU resources $available >= Requested CPU resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Available worker CPU resources $available < Requested CPU resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
        $available = $total_available_resources{'worker'}{'Memory'};
        $required  = $total_required_resources{'requests'}{'Memory'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Available worker Memory resources $available >= Requested Memory resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Available worker Memory resources $available < Requested Memory resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
        $available = $total_available_resources{'worker'}{'Storage'};
        $required  = $total_required_resources{'requests'}{'Storage'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Available worker Storage resources $available >= Requested Storage resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Available worker Storage resources $available < Requested Storage resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
    } else {
        # Check that total available resources for the master and workers is larger
        # than required resources plus a safety margin for the deployment.
        $available = $total_available_resources{'master'}{'CPU'} + $total_available_resources{'worker'}{'CPU'};
        $required  = $total_required_resources{'requests'}{'CPU'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Available master and worker CPU resources $available >= Requested CPU resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Available master and worker CPU resources $available < Requested CPU resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
        $available = $total_available_resources{'master'}{'Memory'} + $total_available_resources{'worker'}{'Memory'};
        $required  = $total_required_resources{'requests'}{'Memory'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Available master and worker Memory resources $available >= Requested Memory resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Available master and worker Memory resources $available < Requested Memory resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
        $available = $total_available_resources{'master'}{'Storage'} + $total_available_resources{'worker'}{'Storage'};
        $required  = $total_required_resources{'requests'}{'Storage'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Available master and worker Storage resources $available >= Requested Storage resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Available master and worker Storage resources $available < Requested Storage resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
    }

    if ($detailed_check == 0) {
        #
        # Old compare where we just check the highest required resource against highest available resource.
        #
        # NOTE: This logic does not handle combining master and worker nodes and therefore should not be used
        # on nodes where also the control-plane nodes are used for deploying nodes.
        # But this should really be of any concern because the playlists calling this script should now always
        # cause $detailed_check to be 1 and then the list of available resources will already include master nodes.
        #

        # Check that highest available resources for the workers is larger
        # than the highest required resources plus a safety margin for the deployment.
        $available = $highest_available_resources{'worker'}{'CPU'};
        $required  = $highest_required_pod_resources{'requests'}{'CPU'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Highest available worker CPU resources $available >= Highest requested POD CPU resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Highest available worker CPU resources $available < Highest requested POD CPU resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
        $available = $highest_available_resources{'worker'}{'Memory'};
        $required  = $highest_required_pod_resources{'requests'}{'Memory'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Highest available worker Memory resources $available >= Highest requested POD Memory resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Highest available worker Memory resources $available < Highest requested POD Memory resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
        $available = $highest_available_resources{'worker'}{'Storage'};
        $required  = $highest_required_pod_resources{'requests'}{'Storage'} * $safety_margin;
        if ($available >= $required) {
            printf "%-6s  %s\n", "OK", "Highest available worker Storage resources $available >= Highest requested POD Storage resources $required including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "Highest available worker Storage resources $available < Highest requested POD Storage resources $required including safety margin of $safety_margin";
            $failed_check++;
        }
    } else {
        #
        # New compare where we check that all required resources will fit in all available resources.
        #

        # Check CPU
        ($result, $details) = check_all_resources($available_node_resources{'CPU'}, $required_pod_resources{'CPU'});
        if ($result eq "OK") {
            printf "%-6s  %s\n", "OK", "All requested POD CPU resources fit in the available CPU resources including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "The following requested POD CPU resources does not fit in the available CPU resources including safety margin of $safety_margin: $details";
            $failed_check++;
        }

        # Check Memory
        ($result, $details) = check_all_resources($available_node_resources{'Memory'}, $required_pod_resources{'Memory'});
        if ($result eq "OK") {
            printf "%-6s  %s\n", "OK", "All requested POD Memory resources fit in the available Memory resources including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "The following requested POD Memory resources does not fit in the available Memory resources including safety margin of $safety_margin: $details";
            $failed_check++;
        }

        # Check Storage
        ($result, $details) = check_all_resources($available_node_resources{'Storage'}, $required_pod_resources{'Storage'});
        if ($result eq "OK") {
            printf "%-6s  %s\n", "OK", "All requested POD Storage resources fit in the available Storage resources including safety margin of $safety_margin";
        } else {
            printf "%-6s  %s\n", "Not OK", "The following requested POD Storage resources does not fit in the available Storage resources including safety margin of $safety_margin: $details";
            $failed_check++;
        }

    }

    printf "%-6s  %s\n", "-"x6, "-"x150;
    if ($failed_check == 0) {
        printf "%-6s  %s\n", "OK", "All checks passed, there seems to be enough resources available for a deployment";
    } elsif ($warning_only == 1) {
        printf "%-6s  %s\n", "Not OK", "$failed_check checks failed, there seems NOT to be enough resources available for a deployment, but we continue anyway";
        $failed_check = 0;
    }else {
        printf "%-6s  %s\n", "Not OK", "$failed_check checks failed, there seems NOT to be enough resources available for a deployment";
        $failed_check = 2;
    }

    return $failed_check;
}

# -----------------------------------------------------------------------------
#
# Get the available resources from the node.
#
sub get_available_node_resources {
    my $found_cnt = 0;
    my $rc;
    my @result;

    $rc = General::File_Operations::read_file(
        {
            "filename"              => $used_resources_file,
            "output-ref"            => \@result,
        }
    );
    if ($rc != 0) {
        print "Failed to read the '$used_resources_file' file";
        return 1;
    }

    # Parse the output and look for specific data in the output
    for (@result) {
        if (/^Total available (master|worker) node resources (CPU|Memory|Storage):\s*(\S+)\s*$/) {
            $total_available_resources{$1}{$2} = $3;
            $found_cnt++;
        } elsif (/^Highest available (master|worker) node resources (CPU|Memory|Storage):\s*(\S+)\s*$/) {
            $highest_available_resources{$1}{$2} = $3;
            $found_cnt++;
        } elsif ($include_master_resources == 1 && /^Available master node resources (CPU|Memory|Storage):\s*(.+)/) {
            $available_node_resources{$1} = "$2 ";
            $found_cnt++;
        } elsif (/^Available worker node resources (CPU|Memory|Storage):\s*(.+)/) {
            $available_node_resources{$1} .= $2;
            $found_cnt++;
        }
    }

    if ($found_cnt == 12 || $found_cnt == 15 || $found_cnt == 18) {
        return 0;
    } else {
        print "Not all expected values from file '$used_resources_file' was found";
        return 1;
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

This script is used for comparing needed resources based on data provided in
a file and then it compares this against available resources in another file
to see if there are enough resources to deploy the software.

NOTE:
(1) This script depends on output created by the following two script files
    which exist in the same directory as this script:
        - calculate_needed_umbrella_resources.pl
        - print_resource_statistics.pl

Syntax:
=======

$0 [<OPTIONAL>] <MANDTORY>

<MANDTORY> is one of the following parameters:

  -r <filename> | --required-resources-file <filename>
  -u <filename> | --used-resources-file <filename>

<OPTIONAL> are one or more of the following parameters:

  -d            | --detailed-check
  -h            | --help
  -i            | --include-master-resources
  -s <value>    | --safety-margin <value>
  -w            | --warning-only

where:

  -d
  --detailed-check
  ----------------
  If specified then a detailed check will be performed where resource request
  values for all pods will be compared to all available node resources.
  If not specified then a check is only done against the highest resource
  request value agains the highest available node resource value.


  -h
  --help
  ------
  Shows this help information.


  -i
  --include-master-resources
  --------------------------
  If specified then also 'master' type nodes are included in the resource check.
  If not specified then 'master' nodes are not included in the check.
  This parameter is only used when also -d / --detailed-check is specified.
  This parameter should only be specified if the pods to check are also deployed
  on 'master' type nodes.


  -r <filename>
  --required-resources-file <filename>
  ------------------------------------
  Specifies the file that contains the printout from the calculate_needed_umbrella_resources.pl
  script and it lists the required resources for the deployment.


  -s <value>
  --safety-margin <value>
  -----------------------
  If specified then the total available resources must be at least this much
  larger than requested resources.
  The value can be specified as a, positive integer or floating point value that
  is greater than or equal to 1, or as a positive percentage value.
  If a percentage value is given then this value will be converted to a floating
  point value accoring to the following formula:
    fvalue = 1 + pvalue / 100
  For example: -s 10%   => 1.100000
               -s 100%  => 2.000000
               -s 200%  => 3.000000
               -s 0%    => 1.000000 (i.e. no safety margin will be applied)
  The check applied will be as follows:
    if available resources >= (requested resources * safety margin)
        then enough resources is available for deployment
    else not enough resources is available for deployment
  If not specified then the default safety margin used will be 1 (or 0%).


  -u <filename>
  --used-resources-file <filename>
  --------------------------------
  Specifies the file that contains the printout from the print_resource_statistics.pl
  script and it lists the used resources of the node before the deployment.


  -w
  --warning-only
  --------------
  If specified and the estimated required resources exceed the available resources
  then only issue a warning message and still return a successful return code.
  If not specified and estimated required resources exceed the available resources
  then an unsuccessful return code will be given.


Output:
=======

The script prints the result of the resource check to STDOUT.


Examples:
=========

  Example 1
  ---------
  Check if there is enough resources for a deployment.

  $0 \\
    --required-resources-file=required_resources_for_deployment.txt \\
    --used-resources-file=resource_usage_before_deployment.txt


Return code:
============

   0: Successful, there is enough resources to deploy the software.
   1: Unsuccessful, some failure was reported.
   2: Unsuccessful, there is NOT enough resources to deploy the software.

EOF
}

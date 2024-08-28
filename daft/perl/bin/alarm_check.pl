#!/usr/bin/perl

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 2.7
#  Date     : 2023-09-13 10:21:37
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2022
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
use General::Json_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;

my $script_dir = dirname abs_path $0;

my $alarm_file = "";
my @alarm_names = ();
my $alarm_pod = "";
my $alarms;     # Reference to parsed JSON data structure
my $alarms_allow_list;
my @allow_files = ();
my $debug = 0;
my $full_alarm_list = 0;
my $kubeconfig = "";
my $logfile = "";
my $namespace = "eiffelesc";
my $rc = 0;
my $show_help = 0;
my $summary_list = 0;
my $use_allowlist = 0;
my $use_tls = 0;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|alarm_file=s"           => \$alarm_file,
    "f|log_file=s"             => \$logfile,
    "h|help"                   => \$show_help,
    "l|allow_file=s"           => \@allow_files,
    "n|namespace=s"            => \$namespace,
    "s|summary_list"           => \$summary_list,
    "t|use_tls"                => \$use_tls,
    "u|use_allowlist"          => \$use_allowlist,
    "w|use_whitelist"          => \$use_allowlist,
    "alarm-name=s"             => \@alarm_names,
    "full_alarm_list"          => \$full_alarm_list,
    "kubeconfig=s"             => \$kubeconfig,
    "debug_messages"           => \$debug,
);

read_allow_list();

if ($kubeconfig ne "" && -f "$kubeconfig") {
    ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);
} elsif ($kubeconfig ne "") {
    print "The kubeconfig file does not exist\n";
    exit 1;
}

if ( $show_help ) {
    show_help();
    exit 0;
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

if ($alarm_file eq "") {
    fetch_alarms();
} else {
    read_alarm_file();
}

if ($alarms) {
    if (scalar @{$alarms} == 0) {
        print "\nNo alarms found\n";
        $rc = 0;
    } elsif ($use_allowlist) {
        check_allow_list();
    } else {
        print_alarms();
    }
}

if ($summary_list) {
    print_summary();
}

# Stop logging of output to file
if ($logfile) {
    General::Logging::log_disable();
}

exit $rc;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub check_allow_list {
    my @result_allowed = ();
    my @result_not_allowed = ();

    $rc = General::Data_Structure_Operations::compare_structure_against_allow_list(
        {
            "allow-ref"                 => $alarms_allow_list,
            "input-ref"                 => $alarms,
            "debug-messages"            => $debug,
            "result-allowed-ref"        => \@result_allowed,
            "result-not-allowed-ref"    => \@result_not_allowed,
            "no-color"                  => 0,
        }
    );
    if ($rc == 0) {
        print "\nAll Alarms are in the Allow List:\n", General::Json_Operations::write_to_scalar({ "input-ref" => \@result_allowed, "pretty-print" => 1, "sort-output" => 1 });
    } elsif ($rc > 0) {
        print "\nAlarms in the Allow List:\n", General::Json_Operations::write_to_scalar({ "input-ref" => \@result_allowed, "pretty-print" => 1, "sort-output" => 1 });
        print "\nAlarms NOT in the Allow List:\n", General::Json_Operations::write_to_scalar({ "input-ref" => \@result_not_allowed, "pretty-print" => 1, "sort-output" => 1 });
        $rc = 1;
    } else {
        print "Failed to check alarms against the Allow list\n";
        $rc = 1;
    }
}

# -----------------------------------------------------------------------------
sub fetch_alarms {
    my @json_lines = ADP::Kubernetes_Operations::get_alarms(
        {
            "alarm-names"       => \@alarm_names,
            "full-alarm-list"   => $full_alarm_list,
            "hide-output"       => 1,
            "namespace"         => $namespace,
            "pretty-print"      => 1,
            "use-tls"           => $use_tls,
        }
    );
    if (@json_lines) {
        $alarms = General::Json_Operations::read_array_return_reference(
            {
                "input"            => \@json_lines,
            }
        );
    } else {
        print "Failed to fetch alarms from the node using namespace '$namespace'\n";
        $rc = 1;
    }
}

# -----------------------------------------------------------------------------
sub print_alarms {
    print "\nFound Alarms:\n", General::Json_Operations::write_to_scalar({ "input-ref" => $alarms, "pretty-print" => 1, "sort-output" => 1 });
    $rc = 1;
}

# -----------------------------------------------------------------------------
sub print_summary {
    my %alarm_hash;
    my $alarm_name;
    my $max_length = 0;

    for my $alarm_ref (@{$alarms}) {
        $alarm_name = $alarm_ref->{'alarmName'};
        $alarm_hash{$alarm_name}++;
        $max_length = length($alarm_name) if ($max_length < length($alarm_name));
    }

    printf "\nAlarm Summary:\n%-${max_length}s  %5s\n", "Alarm Name", "Count";
    printf "%-${max_length}s  %5s\n", "-"x$max_length, "-"x5;
    for $alarm_name (sort keys %alarm_hash) {
        printf "%-${max_length}s  %5d\n", $alarm_name, $alarm_hash{$alarm_name};
    }
}

# -----------------------------------------------------------------------------
sub read_alarm_file {
    if (-f "$alarm_file") {
        $alarms = General::Json_Operations::read_file_return_reference({ "filename" => $alarm_file });
        unless (defined $alarms) {
            print "Problem to read and decode JSON alarm file '$alarm_file'\n";
            exit 1;
        }
    } else {
        print "The 'alarm_file' $alarm_file does not exist or cannot be read\n";
        exit 1;
    }
}

# -----------------------------------------------------------------------------
sub read_allow_list {
    my $allow_file;
    my @discarded;
    my $rc;
    my @tmp_alarms_allow_list;

    if (scalar @allow_files == 0) {
        # No allow files were added byt the user, so add the default one
        push @allow_files, "$script_dir/../../templates/system_health_check_files/default_allowlist.json";
    }
    for $allow_file (@allow_files) {
        if (-f "$allow_file") {
            $allow_file = abs_path $allow_file;

            $rc = General::File_Operations::parse_allow_list_file(
                {
                    "filename"              => $allow_file,
                    "output-ref"            => \@tmp_alarms_allow_list,
                    "entry-type"            => "alarm_allowlist",
                    "append-output-ref"     => 1,
                    "discarded-ref"         => \@discarded,
                    "hide-error-messages"   => 1,
                }
            );
            if ($rc != 0) {
                print "Problem to read and decode JSON allow file '$allow_file'\n";
                exit 1;
            }
            push @{$alarms_allow_list}, @tmp_alarms_allow_list;
        } elsif ($show_help == 0) {
            print "The 'allow_file' $allow_file does not exist or cannot be read\n";
            exit 1;
        }
    }
}

# -----------------------------------------------------------------------------
sub show_help {
    print <<EOF;

Description:
============

This script checks for alarms with an option to ignore specific allowed alarms.


Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:

  -a <filename> | --alarm_file <filename>
                  --alarm-name <string>
                  --debug_messages
                  --full_alarm_list
  -f <filename> | --log_file <filename>
  -h            | --help
                  --kubeconfig <filename>
  -l <filename> | --allow_file <filename>
  -n <string>   | --namespace <string>
  -s            | --summary
  -t            | --use_tls
  -w            | --use_allowlist

where:

  -a <filename>
  --alarm_file <filename>
  -----------------------
  If specified then alarms are read from this file instead of from the node.
  This parameter is mainly for debug purposes to test that the logic detects the
  alarms properly.


  --alarm-name <string>
  ---------------------
  If specified then only alarms with this name (alarmName) will be checked.
  This parameter can be given multiple times if more than one specific alarm
  should be checked.
  If the parameter is not specified then all alarms will be checked.


  --debug_messages
  ----------------
  Enable debug messages when parsing the allow list.
  This parameter might be useful when you have created your own allow list file
  and are having problems to get the allow list logic to work.


  --full_alarm_list
  -----------------
  If specified then extra fields like 'history', 'vendor', 'code' etc. will also
  be included in the printout.
  This parameter might not work on SC releases earlier than 1.10.


  -f <filename>
  --log_file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  If not specified then no log file is created and only what is printed to STDOUT
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
  --allow_file <filename>
  -----------------------
  If specified then alarms to allow and not reported as a problem is loaded from
  this file. This file most be in JSON format and follow the same layout as the
  alarm list which is also in JSON format.
  If not specified then the default values are read from the file store in the
  following relative path from the script:
   ../../templates/system_health_check_files/default_allowlist.json
  This parameter can be specified multiple times if more than one file should
  be loaded.


  -n <string>
  --namespace <string>
  --------------------
  Application namespace used for for alarm checks.
  If not specified it will default to 'eiffelesc' namespace.


  -s
  --summary
  ---------
  If specified then a summary of the different alarms names are printed with a
  count of how many of each type.


  -t
  --use_tls
  ---------
  If specified then secure connection will be used towards the alarm handler.
  This parameter might not work on SC releases earlier than 1.10.


  -u
  -w
  --use_allowlist
  --use_whitelist
  ---------------
  If specified then a list of allowed alarms will be checked to see if the
  specific alarms should be ignored.
  The logic is that it will check if all fields for a specific alarm in the
  allow list match with the same fields in the actual alarm, and if this is the
  case then this is an allowed alarm, but if one or more fields from the allow
  list does not match with the same field in the actual alarm then it's is
  reported as a not allowed allarm.

EOF
    if ($alarms_allow_list) {
        print "Alarms in the Allow List:\n", General::Json_Operations::write_to_scalar({ "input-ref" => $alarms_allow_list, "pretty-print" => 1, "sort-output" => 1 });
    }
}

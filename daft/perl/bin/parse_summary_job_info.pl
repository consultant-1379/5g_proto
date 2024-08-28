#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.6
#  Date     : 2024-03-08 18:15:15
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2019,2022-2024
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

use File::Basename qw(dirname basename);
use Cwd qw(abs_path cwd);
use lib dirname(dirname abs_path $0) . '/lib';
use Time::Local;

use General::File_Operations;

sub show_help {
    print <<EOF;

Description:
============

This script is used for parsing the summary.txt created by DAFT when executing
a Playlist. It will list the following information about the job:

 - File name
 - Execution time for the job
 - Job Status
 - Job Type
 For Node Type DSC:
 - Number of reboots
 - DSC Commercial Release before and after
 - DSC Cluster Version before and after
 For Node Type SC:
 - SC Release and Build version before and after
 - ECCD Version

This can e.g. be useful to get an overview of the different jobs that has been
executed, if they were successful or not and how long time they took.


Syntax:
=======

$0 [<OPTIONAL>] <file or directory name> [<file or directory name> ...]

<OPTIONAL> are one or more of the following parameters:

  -h                 | --help
  -n <type>          | --node-type <type>
  -o <file>          | --output-file <file>
  -s <regex>         | --job-status <regex>
  -t <regex>         | --job-type <regex>

where:

  -h
  --help
  ------
  Shows this help information.


  -n <type>
  --node-type <type>
  ------------------
  Specifies what kind of node type the log files are from where <type> is either
  'DSC' or 'SC' where 'SC' is the default if not given.


  -o <file>
  --output-file <file>
  --------------------
  If specified then output is written to file instead of STDOUT which is the
  default.


  -s <regex>
  --job-status <regex>
  --------------------
  If specified then only jobs with a matching "Job Status" value will be included
  in the printout.
  By default all job status values will be included if not specified.


  -t <regex>
  --job-type <regex>
  ------------------
  If specified then only jobs with a matching "Job Type" value will be included
  in the printout.
  By default all job type values will be included if not specified.


Output:
=======

The script writes the output to STDOUT unless an output file is specified with
parameter -o or --output-file.


Examples:
=========

  Example 1:
  ----------
  Print a summary of all executed jobs in specific directory or sub directories.

  $0 /proj/CBA_comp/esm/daft/workspaces/


  Example 2:
  ----------
  Print a summary for two specific files.

  $0 \\
    /proj/CBA_comp/esm/daft/workspaces/JOB1_20190528_120000_32/summary.txt \\
    /proj/CBA_comp/esm/daft/workspaces/JOB2_20190528_123244_41/summary.txt

EOF
}

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $debug = 0;

my @data = ();
my @dir_paths = ();
my @dsc_cluster_release = ();
my $dsc_cluster_release_after = "";
my $dsc_cluster_release_before = "";
my @dsc_cluster_version = ();
my $dsc_cluster_version_after = "";
my $dsc_cluster_version_before = "";
my @dsc_commercial_release = ();
my $dsc_commercial_release_after = "";
my $dsc_commercial_release_before = "";
my $file_cnt = 0;
my @file_paths = ();
my $file_total_cnt = 0;
my $job_hours = 0;
my $job_minutes = 0;
my $job_reboots = 0;
my $job_seconds = 0;
my $job_start_time_yyyymmdd_hhmmss = "";
my $job_stop_time_yyyymmdd_hhmmss = "";
my $job_status = "";
my $job_time = "";
my $job_type = "";
my $job_type_length = 8;
my %month_str_to_num = ( "Jan" => 1, "Feb" => 2, "Mar" => 3, "Apr" => 4, "May" => 5, "Jun" => 6, "Jul" => 7, "Aug" => 8, "Sep" => 9, "Oct" => 10, "Nov" => 11, "Dec" => 12 );
my $name = "";
my $node_type = "SC";
my $output_file = "";
my $rc;
my $sc_eccd_release = "";
my @sc_release_build_version = ();
my $sc_release_build_version_after  = "";
my $sc_release_build_version_after_length  = 10;
my $sc_release_build_version_before = "";
my $sc_release_build_version_before_length = 10;
my $show_help = 0;
my %statistics_job;
my $time = 0;
my $wanted_job_status = "";
my $wanted_job_type = "";

# *****************************
# *                           *
# * Validate Input Parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "h|help"                   => \$show_help,
    "n|node-type=s"            => \$node_type,
    "o|output-file=s"          => \$output_file,
    "s|job-status=s"           => \$wanted_job_status,
    "t|job-type=s"             => \$wanted_job_type,
);

$node_type = uc($node_type);
if ($node_type !~ /^(DSC|SC)$/) {
    print "Wrong --node-type given ($node_type), only DSC or SC allowed\n";
    exit 1;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

if ($show_help) {
    show_help();
    exit 0;
}

for (@ARGV) {
    if (-f "$_") {
        if (/^.+\/summary.txt$/) {
            push @file_paths, abs_path $_;
        } else {
            print "SKIP: Not a summary.txt file, $_\n";
        }
    } elsif (-d "$_") {
        push @dir_paths, abs_path $_;
    } else {
        print "ERROR: Invalid file or directory path, $_\n";
    }
}

if (@dir_paths) {
    # Find out if there are any summary.txt files in the specified directories
    for my $dir (@dir_paths) {
        $dir .= "/" unless $dir =~ /\/$/;
        print "Looking for summary.txt files in directory: $dir\n";
        my @files = `find $dir -name summary.txt -type f 2>/dev/null`;
        if (scalar @files > 0) {
            printf "  %d files found\n", scalar @files;
            push @file_paths, @files;
        } else {
            print "  No files found\n";
        }
    }
}
print "\n";

if ($output_file ne "") {
    unless (open OUTF, ">$output_file") {
        print "ERROR: Unable to open file for output, $output_file\n";
        $output_file = "";
    }
}

# Read all found files
$file_total_cnt = scalar @file_paths;
for my $file_path (@file_paths) {
    $file_path =~ s/[\r\n]//g;
    $file_cnt++;
    @dsc_cluster_release = ();
    $dsc_cluster_release_after = "";
    $dsc_cluster_release_before = "";
    @dsc_cluster_version = ();
    $dsc_cluster_version_after = "";
    $dsc_cluster_version_before = "";
    @dsc_commercial_release = ();
    $dsc_commercial_release_after = "";
    $dsc_commercial_release_before = "";
    $job_reboots = 0;
    $job_status = "";
    $job_time = "";
    $job_type = "";
    $job_hours = 0;
    $job_minutes = 0;
    $job_seconds = 0;
    $sc_eccd_release = "";
    @sc_release_build_version = ();
    $sc_release_build_version_after  = "";
    $sc_release_build_version_before = "";

    printf "Processing file %4d of %d: %s\n", $file_cnt, $file_total_cnt, $file_path;
    if ($output_file ne "") {
        printf OUTF "Processing file %4d of %d: %s\n", $file_cnt, $file_total_cnt, $file_path;
    }

    $rc = General::File_Operations::read_file(
        {
            "filename"              => "$file_path",
            "output-ref"            => \@data,
            "hide-error-messages"   => 1,
            "ignore-empty-lines"    => 1,
        }
    );
    if ($rc != 0) {
        print "  ERROR: Problem reading file\n";
        next;
    }

    for (@data) {
        #
        # DSC and SC Common Information
        #
        if (/^\s+Job Elapsed Time :\s+(.+)/) {
            $job_time = $1;
        } elsif (/^\s+Job Type :\s+(.+)/) {
            $job_type = $1;
            $job_type_length = length($job_type) if ($job_type_length < length($job_type));
        } elsif (/^\s+Job Status :\s+(.+)/) {
            $job_status = $1;
        } elsif (/^\s+Job Start Time :\s+\S+\s+(\S+)\s+(\d+)\s+(\d+):(\d+):(\d+)\s+(\d+)\s*$/) {
            #     Job Start Time : Tue Feb  6 21:39:40 2024
            $job_start_time_yyyymmdd_hhmmss = sprintf "%04d%02d%02d %02d%02d%02d", $6, $month_str_to_num{$1}, $2, $3, $4, $5;
        } elsif (/^\s+Job End Time :\s+\S+\s+(\S+)\s+(\d+)\s+(\d+):(\d+):(\d+)\s+(\d+)\s*$/) {
            #       Job End Time : Tue Feb  6 21:39:41 2024
            $job_stop_time_yyyymmdd_hhmmss = sprintf "%04d%02d%02d %02d%02d%02d", $6, $month_str_to_num{$1}, $2, $3, $4, $5;
        #
        # DSC Specific Information
        #
        } elsif (/^Number of Reboots:\s+(\d+)/) {
            $job_reboots = $1;
        } elsif (/^DSC Cluster Version;\s+(\S+);\s+Release:\s+(\S+\s+\S+)/) {
            # DSC Cluster Version; 1.11.0;        Release: CXP9024055_8 R1A
            push @dsc_cluster_version, $1;
            push @dsc_cluster_release, $2;
        } elsif (/^DSC Cluster Version:\s+(\S+)/) {
            # DSC Cluster Version:    1.12.0
            push @dsc_cluster_version, $1;
        } elsif (/^DSC Cluster Release:\s+(\S+\s+\S+)/) {
            # DSC Cluster Release:    CXP9024055_9 R1A
            push @dsc_cluster_release, $1;
        } elsif (/^DSC Commercial Release:\s+(\S+)/) {
            # DSC Commercial Release: 1.12
            push @dsc_commercial_release, $1;
        #
        # SC Specific Information
        #
        } elsif (/^SC Release Version:\s+(\S+)/) {
            # SC Release Version: 1.3.1
            push @sc_release_build_version, $1;
        } elsif (/^SC Release Build:\s+(\S+)/) {
            # SC Release Build:   +327
            $sc_release_build_version[-1] .= $1;
        } elsif (/^ECCD Image Release:\s+(\S+)/) {
            # ECCD Image Release: 2.17.0
            $sc_eccd_release = $1;
        }
    }

    # Check cluster and commercial release data
    if ($node_type eq "DSC") {
        if ($job_type eq "INSTALLATION") {
            if (scalar @dsc_cluster_release == 1) {
                $dsc_cluster_release_after = $dsc_cluster_release[0];
            }

            if (scalar @dsc_cluster_version == 1) {
                $dsc_cluster_version_after = $dsc_cluster_version[0];
            }

            if (scalar @dsc_commercial_release == 1) {
                $dsc_commercial_release_after = $dsc_commercial_release[0];
            }
        } elsif ($job_type eq "UPGRADE") {
            if (scalar @dsc_cluster_release == 1) {
                $dsc_cluster_release_before = $dsc_cluster_release[0];
            } elsif (scalar @dsc_cluster_release > 1) {
                $dsc_cluster_release_before = $dsc_cluster_release[-2];
                $dsc_cluster_release_after = $dsc_cluster_release[-1];
            }

            if (scalar @dsc_cluster_version == 1) {
                $dsc_cluster_version_before = $dsc_cluster_version[0];
            } elsif (scalar @dsc_cluster_version > 1) {
                $dsc_cluster_version_before = $dsc_cluster_version[-2];
                $dsc_cluster_version_after = $dsc_cluster_version[-1];
            }

            if (scalar @dsc_commercial_release == 1) {
                $dsc_commercial_release_before = $dsc_commercial_release[0];
            } elsif (scalar @dsc_commercial_release > 1) {
                $dsc_commercial_release_before = $dsc_commercial_release[-2];
                $dsc_commercial_release_after = $dsc_commercial_release[-1];
            }
        } else {
            if (scalar @dsc_cluster_release == 1) {
                if ($job_status eq "SUCCESSFUL") {
                    $dsc_cluster_release_after = $dsc_cluster_release[0];
                } else {
                    $dsc_cluster_release_before = $dsc_cluster_release[0];
                }
            } elsif (scalar @dsc_cluster_release > 1) {
                $dsc_cluster_release_before = $dsc_cluster_release[-2];
                $dsc_cluster_release_after = $dsc_cluster_release[-1];
            }

            if (scalar @dsc_cluster_version == 1) {
                if ($job_status eq "SUCCESSFUL") {
                    $dsc_cluster_version_after = $dsc_cluster_version[0];
                } else {
                    $dsc_cluster_version_before = $dsc_cluster_version[0];
                }
            } elsif (scalar @dsc_cluster_version > 1) {
                $dsc_cluster_version_before = $dsc_cluster_version[-2];
                $dsc_cluster_version_after = $dsc_cluster_version[-1];
            }

            if (scalar @dsc_commercial_release == 1) {
                if ($job_status eq "SUCCESSFUL") {
                    $dsc_commercial_release_after = $dsc_commercial_release[0];
                } else {
                    $dsc_commercial_release_before = $dsc_commercial_release[0];
                }
            } elsif (scalar @dsc_commercial_release > 1) {
                $dsc_commercial_release_before = $dsc_commercial_release[-2];
                $dsc_commercial_release_after = $dsc_commercial_release[-1];
            }
        }
    } else {
        # SC
        if (scalar @sc_release_build_version == 1) {
            if ($job_type eq "DEPLOY") {
                $sc_release_build_version_after  = $sc_release_build_version[0];
            } else {
                $sc_release_build_version_before = $sc_release_build_version[0];
            }
        } elsif (scalar @sc_release_build_version > 1) {
            $sc_release_build_version_before = $sc_release_build_version[0];
            $sc_release_build_version_after  = $sc_release_build_version[1];
        }
    }

    # Check job time and convert it to hh:mm:ss format
    if ($job_time =~ /\s*(\d+)\s+hours/) {
        $job_hours = $1;
    }
    if ($job_time =~ /\s*(\d+)\s+minutes/) {
        $job_minutes = $1;
    }
    if ($job_time =~ /\s*(\d+)\s+second/) {
        $job_seconds = $1;
    }
    $job_time = sprintf "%02d:%02d:%02d", $job_hours, $job_minutes, $job_seconds;

    # Check if we should ignore the job based on job status or job type
    if ($wanted_job_status ne "" && $job_status !~ /$wanted_job_status/i) {
        # We don't want to report this job status
        next;
    }
    if ($wanted_job_type ne "" && $job_type !~ /$wanted_job_type/i) {
        # We don't want to report this job status
        next;
    }

    $statistics_job{$file_path}{'job_type'} = $job_type;
    $statistics_job{$file_path}{'job_status'} = $job_status;
    $statistics_job{$file_path}{'job_time'} = $job_time;
    $statistics_job{$file_path}{'job_start'} = $job_start_time_yyyymmdd_hhmmss;
    $statistics_job{$file_path}{'job_stop'} = $job_stop_time_yyyymmdd_hhmmss;
    # DSC Information
    $statistics_job{$file_path}{'job_reboots'} = $job_reboots;
    $statistics_job{$file_path}{'commercial_release_after'} = $dsc_commercial_release_after;
    $statistics_job{$file_path}{'commercial_release_before'} = $dsc_commercial_release_before;
    $statistics_job{$file_path}{'cluster_version_after'} = $dsc_cluster_version_after;
    $statistics_job{$file_path}{'cluster_version_before'} = $dsc_cluster_version_before;
    $statistics_job{$file_path}{'cluster_release_after'} = $dsc_cluster_release_after;
    $statistics_job{$file_path}{'cluster_release_before'} = $dsc_cluster_release_before;
    # SC Information
    $statistics_job{$file_path}{'sc_release_after'} = $sc_release_build_version_after;
    $sc_release_build_version_after_length = length($sc_release_build_version_after) if ($sc_release_build_version_after_length < length($sc_release_build_version_after));
    $statistics_job{$file_path}{'sc_release_before'} = $sc_release_build_version_before;
    $sc_release_build_version_before_length = length($sc_release_build_version_before) if ($sc_release_build_version_before_length < length($sc_release_build_version_before));
    $statistics_job{$file_path}{'sc_eccd_release'} = $sc_eccd_release;
}

if ($node_type eq "DSC") {
    if ($output_file ne "") {
        print  OUTF "\nSummary of Job Information:\n";
        print  OUTF "-----------------------------\n\n";
        printf OUTF "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-7s  %-18s  %-16s  %-42s\n",
            " ",
            " ",
            "Job Time",
            "Job Start",
            "Job End",
            " ",
            "Commercial Release",
            "Cluster Version",
            "Cluster Release";
        printf OUTF "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-7s  %-8s  %-8s  %-7s  %-7s  %-20s  %-20s  %s\n",
            "Job Type",
            "Job Status",
            "HH:MM:SS",
            "YYYYMMDD HHMMSS",
            "YYYYMMDD HHMMSS",
            "Reboots",
            "Before",
            "After",
            "Before",
            "After",
            "Before",
            "After",
            "File Name";
        printf OUTF "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-7s  %-8s  %-8s  %-7s  %-7s  %-20s  %-20s  %s\n",
            "-"x${job_type_length},
            "-"x10,
            "-"x10,
            "-"x15,
            "-"x15,
            "-"x7,
            "-"x8,
            "-"x8,
            "-"x7,
            "-"x7,
            "-"x20,
            "-"x20,
            "-"x9;
    } else {
        print  "\nSummary of Job Information:\n";
        print  "-----------------------------\n\n";
        printf "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-7s  %-18s  %-16s  %-42s\n",
            " ",
            " ",
            "Job Time",
            "Job Start",
            "Job End",
            " ",
            "Commercial Release",
            "Cluster Version",
            "Cluster Release";
        printf "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-7s  %-8s  %-8s  %-7s  %-7s  %-20s  %-20s  %s\n",
            "Job Type",
            "Job Status",
            "HH:MM:SS",
            "YYYYMMDD HHMMSS",
            "YYYYMMDD HHMMSS",
            "Reboots",
            "Before",
            "After",
            "Before",
            "After",
            "Before",
            "After",
            "File Name";
        printf "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-7s  %-8s  %-8s  %-7s  %-7s  %-20s  %-20s  %s\n",
            "-"x${job_type_length},
            "-"x10,
            "-"x10,
            "-"x15,
            "-"x15,
            "-"x7,
            "-"x8,
            "-"x8,
            "-"x7,
            "-"x7,
            "-"x20,
            "-"x20,
            "-"x9;
    }
} else {
    # SC
    if ($output_file ne "") {
        print  OUTF "\nSummary of Job Information:\n";
        print  OUTF "-----------------------------\n\n";
        printf OUTF "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %s\n",
            " ",
            " ",
            "Job Time",
            "Job Start",
            "Job End",
            "SC Version";
        printf OUTF "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  -${sc_release_build_version_before_length}s  %-${sc_release_build_version_after_length}s  %-12s  %s\n",
            "Job Type",
            "Job Status",
            "HH:MM:SS",
            "YYYYMMDD HHMMSS",
            "YYYYMMDD HHMMSS",
            "Before",
            "After",
            "ECCD Release",
            "File Name";
        printf OUTF "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-${sc_release_build_version_before_length}s  %-${sc_release_build_version_after_length}s  %-12s  %s\n",
            "-"x${job_type_length},
            "-"x10,
            "-"x10,
            "-"x15,
            "-"x15,
            "-"x${sc_release_build_version_before_length},
            "-"x${sc_release_build_version_after_length},
            "-"x12,
            "-"x9;
    } else {
        print  "\nSummary of Job Information:\n";
        print  "-----------------------------\n\n";
        printf "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %s\n",
            " ",
            " ",
            "Job Time",
            "Job Start",
            "Job End",
            "SC Version";
        printf "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-${sc_release_build_version_before_length}s  %-${sc_release_build_version_after_length}s  %-12s  %s\n",
            "Job Type",
            "Job Status",
            "HH:MM:SS",
            "YYYYMMDD HHMMSS",
            "YYYYMMDD HHMMSS",
            "Before",
            "After",
            "ECCD Release",
            "File Name";
        printf "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-${sc_release_build_version_before_length}s  %-${sc_release_build_version_after_length}s  %-12s  %s\n",
            "-"x${job_type_length},
            "-"x10,
            "-"x10,
            "-"x15,
            "-"x15,
            "-"x${sc_release_build_version_before_length},
            "-"x${sc_release_build_version_after_length},
            "-"x12,
            "-"x9;
    }
}

# Calculate average values and print output for steps
for $name (sort keys %statistics_job) {
    if ($output_file ne "") {
        if ($node_type eq "DSC") {
            printf OUTF "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %7d  %-8s  %-8s  %-7s  %-7s  %-20s  %-20s  %s\n",
                $statistics_job{$name}{'job_type'},
                $statistics_job{$name}{'job_status'},
                $statistics_job{$name}{'job_time'},
                $statistics_job{$name}{'job_start'},
                $statistics_job{$name}{'job_stop'},
                $statistics_job{$name}{'job_reboots'},
                $statistics_job{$name}{'commercial_release_before'},
                $statistics_job{$name}{'commercial_release_after'},
                $statistics_job{$name}{'cluster_version_before'},
                $statistics_job{$name}{'cluster_version_after'},
                $statistics_job{$name}{'cluster_release_before'},
                $statistics_job{$name}{'cluster_release_after'},
                $name;
        } else {
            # SC
            printf OUTF "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-${sc_release_build_version_before_length}s  %-${sc_release_build_version_after_length}s  %-12s  %s\n",
                $statistics_job{$name}{'job_type'},
                $statistics_job{$name}{'job_status'},
                $statistics_job{$name}{'job_time'},
                $statistics_job{$name}{'job_start'},
                $statistics_job{$name}{'job_stop'},
                $statistics_job{$name}{'sc_release_before'},
                $statistics_job{$name}{'sc_release_after'},
                $statistics_job{$name}{'sc_eccd_release'},
                $name;
        }
    } else {
        if ($node_type eq "DSC") {
            printf "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %7d  %-8s  %-8s  %-7s  %-7s  %-20s  %-20s  %s\n",
                $statistics_job{$name}{'job_type'},
                $statistics_job{$name}{'job_status'},
                $statistics_job{$name}{'job_time'},
                $statistics_job{$name}{'job_start'},
                $statistics_job{$name}{'job_stop'},
                $statistics_job{$name}{'job_reboots'},
                $statistics_job{$name}{'commercial_release_before'},
                $statistics_job{$name}{'commercial_release_after'},
                $statistics_job{$name}{'cluster_version_before'},
                $statistics_job{$name}{'cluster_version_after'},
                $statistics_job{$name}{'cluster_release_before'},
                $statistics_job{$name}{'cluster_release_after'},
                $name;
        } else {
            # SC
            printf "%-${job_type_length}s  %-10s  %-10s  %-15s  %-15s  %-${sc_release_build_version_before_length}s  %-${sc_release_build_version_after_length}s  %-12s  %s\n",
                $statistics_job{$name}{'job_type'},
                $statistics_job{$name}{'job_status'},
                $statistics_job{$name}{'job_time'},
                $statistics_job{$name}{'job_start'},
                $statistics_job{$name}{'job_stop'},
                $statistics_job{$name}{'sc_release_before'},
                $statistics_job{$name}{'sc_release_after'},
                $statistics_job{$name}{'sc_eccd_release'},
                $name;
        }
    }
}

if ($output_file ne "") {
    close OUTF;
    print "Output written to file $output_file\n";
}

exit 0;

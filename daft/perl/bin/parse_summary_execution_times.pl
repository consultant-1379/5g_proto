#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.14
#  Date     : 2019-05-29 13:28:37
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2019
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
a Playlist. It can read all found summary.txt files when provided a directory path
and it displays a summary with minimum, maximum and average execution times for
all found tasks and steps and displays it in a readable format.
This can e.g. be useful to find the average execution time for a number of
different upgrade jobs.


Syntax:
=======

$0 [<OPTIONAL>] <file or directory name> [<file or directory name> ...]

<OPTIONAL> are one or more of the following parameters:

  -a                 | --all-values
  -h                 | --help
  -i <regex>         | --include-pattern <regex>
  -n                 | --not-same-to-same
  -o <file>          | --output-file <file>
  -s <regex>         | --job-status <regex>
  -t <regex>         | --job-type <regex>
  -v <integer>       | --values-per-line <integer>
  -x <regex>         | --exclude-pattern <regex>

where:

  -a
  --all-values
  ------------
  Also print all found values.


  -h
  --help
  ------
  Shows this help information.


  -i <regex>
  --include-pattern <regex>
  -------------------------
  If specified then only patterns of the Step or Task name matching the <regex>
  will be included in the printout.
  By default all Step and Task names will be included if not specified.


  -n
  --not-same-to-same
  ------------------
  If specified then upgrades that have the same software version before and after
  the Upgrade will be skipped and not included in the printout.
  It looks for 'DSC Cluster Version' and 'DSC Commercial Release' and compares
  the two values before and after and if the same then the file is skipped.


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


  -v <integer>
  --values-perl-line <integer>
  ----------------------------
  When used together with parameter -a/--all-values you can specify how many
  values to print per line.
  The default value if not specified is 10.


  -x <regex>
  --exclude-pattern <regex>
  -------------------------
  If specified then patterns of the Step or Task name matching the <regex>
  will be excluded from the printout.
  If also -i/--include-pattern is specified then the exclude pattern has
  higher priority.
  By default nothing is excluded if not specified.


Output:
=======

The script writes the output to STDOUT unless an output file is specified with
parameter -o or --output-file.


Examples:
=========

  Example 1:
  ----------
  Print a summary of all times in specific directory or sub directories.

  $0 /proj/CBA_comp/esm/daft/workspaces/


  Example 2:
  ----------
  Print a summary of all times for two specific files.

  $0 \\
    /proj/CBA_comp/esm/daft/workspaces/JOB1_20190528_120000_32/summary.txt \\
    /proj/CBA_comp/esm/daft/workspaces/JOB2_20190528_123244_41/summary.txt


  Example 3:
  ----------
  Print a summary of all times for all Upgrade jobs for a specific release
  and a specific node.

  $0 /proj/CBA_comp/esm/daft/workspaces/vDSC_Peanuts_Rerun_Upgrade_NDP1.12.0_Upgrade_SW*


  Example 4:
  ----------
  Print a summary of all times for all successful Upgrade jobs for a specific release
  and a specific node.

  $0 /proj/CBA_comp/esm/daft/workspaces/vDSC_Peanuts_Rerun_Upgrade_NDP1.12.0_Upgrade_SW* \\
     --job-status='(SUCCESSFUL|ONGOING)' \\
     --job-type=UPGRADE

EOF
}

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $debug = 0;

my $all_values = 0;
my @data = ();
my @dir_paths = ();
my @dsc_cluster_version = ();
my @dsc_commercial_release = ();
my $exclude_pattern = "";
my $file_cnt = 0;
my @file_paths = ();
my $file_total_cnt = 0;
my $include_pattern = "";
my $job_status = "";
my $job_type = "";
my $median;
my $name = "";
my $not_same_to_same = 0;
my $output_file = "";
my $rc;
my $reading_steps = 0;
my $reading_tasks = 0;
my $show_help = 0;
my %statistics_step;
my %statistics_task;
my $time = 0;
my @values = ();
my $value_cnt = 0;
my $values_line = "";
my $values_per_line = 10;

# *****************************
# *                           *
# * Validate Input Parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|all-values"             => \$all_values,
    "h|help"                   => \$show_help,
    "i|include-pattern=s"      => \$include_pattern,
    "n|not-same-to-same"       => \$not_same_to_same,
    "o|output-file=s"          => \$output_file,
    "s|job-status=s"           => \$job_status,
    "t|job-type=s"             => \$job_type,
    "v|values-per-line=i"      => \$values_per_line,
    "x|exclude-pattern=s"      => \$exclude_pattern,
);

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
    @dsc_cluster_version = ();
    @dsc_commercial_release = ();

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
    $reading_steps = 0;
    $reading_tasks = 0;
    for (@data) {
        if (/^Summary of total execution times per Step:\s*$/) {
            $reading_steps = 1;
            $reading_tasks = 0;
            next;
        } elsif (/^Summary of total execution times per Task:\s*$/) {
            $reading_steps = 0;
            $reading_tasks = 1;
            next;
        } elsif (/^\s+Job Type : (.+)/) {
            if ($job_type ne "") {
                my $type = $1;
                unless ($type =~ /$job_type/i) {
                    print "  Ignoring file because 'Job Type' $type does not match $job_type\n";
                    if ($output_file ne "") {
                        print OUTF "  Ignoring file because 'Job Type' $type does not match $job_type\n";
                    }
                    last;
                }
            }
        } elsif (/^\s+Job Status : (.+)/) {
            if ($job_status ne "") {
                my $status = $1;
                unless ($status =~ /$job_status/i) {
                    print "  Ignoring file because 'Job Status' $status does not match $job_status\n";
                    if ($output_file ne "") {
                        print OUTF "  Ignoring file because 'Job Status' $status does not match $job_status\n";
                    }
                    last;
                }
            }
        } elsif (/^DSC Cluster Version[:;]\s+(.+)/) {
            if ($not_same_to_same == 1) {
                push @dsc_cluster_version, $1;
                if (scalar @dsc_cluster_version == 2) {
                    if ($dsc_cluster_version[0] eq $dsc_cluster_version[1]) {
                        print "  Ignoring file because 'DSC Cluster Version' before and after Upgrade are the same\n";
                        if ($output_file ne "") {
                            print OUTF "  Ignoring file because 'DSC Commercial Release' before and after Upgrade are the same\n";
                        }
                        last;
                    }
                }
            }
        } elsif (/^DSC Commercial Release:\s+(.+)/) {
            if ($not_same_to_same == 1) {
                push @dsc_commercial_release, $1;
                if (scalar @dsc_commercial_release == 2) {
                    if ($dsc_commercial_release[0] eq $dsc_commercial_release[1]) {
                        print "  Ignoring file because 'DSC Commercial Release' before and after Upgrade are the same\n";
                        if ($output_file ne "") {
                            print OUTF "  Ignoring file because 'DSC Commercial Release' before and after Upgrade are the same\n";
                        }
                        last;
                    }
                }
            }

        } elsif ($reading_steps == 1 && /^\s*(\d+\.\d+)\s+(Playlist::\d{3}_.+_P\d{3}S\d{2})\s*$/) {
            #   627.175922  Playlist::001_Installation_or_Upgrade_DSC::JOBTYPE_Upgrade_P001S07
            $time = $1;
            $name = $2;

            # Check if we need to exclude or include certain Steps or Tasks
            if ($exclude_pattern ne "") {
                next if (/$exclude_pattern/i);
            }
            if ($include_pattern ne "") {
                next unless (/$include_pattern/i);
            }

            # Save the statistics to a hash variable
            if (exists $statistics_step{$name}) {
                $statistics_step{$name}{'total_time'} += $time;
                $statistics_step{$name}{'total_cnt'}  += 1;
                $statistics_step{$name}{'max_time'}    = $time if ($statistics_step{$name}{'max_time'} < $time);
                $statistics_step{$name}{'min_time'}    = $time if ($statistics_step{$name}{'min_time'} > $time);
                push @{$statistics_step{$name}{'values'}}, $time;
            } else {
                $statistics_step{$name}{'total_time'}  = $time;
                $statistics_step{$name}{'total_cnt'}   = 1;
                $statistics_step{$name}{'max_time'}    = $time;
                $statistics_step{$name}{'min_time'}    = $time;
                push @{$statistics_step{$name}{'values'}}, $time;
            }
        } elsif ($reading_tasks == 1 && /^\s*(\d+\.\d+)\s+(Playlist::\d{3}_.+_P\d{3}S\d{2}T\d{2}|Playlist::\d{3}_.+::main)\s*$/) {
            #   754.374788  Playlist::001_Installation_or_Upgrade_DSC::main
            #   385.242003  Playlist::906_Perform_ESM_Upgrade::Activate_Upgrade_Package_P906S05T01
            $time = $1;
            $name = $2;

            # Check if we need to exclude or include certain Steps or Tasks
            if ($exclude_pattern ne "") {
                next if (/$exclude_pattern/i);
            }
            if ($include_pattern ne "") {
                next unless (/$include_pattern/i);
            }

            # Save the statistics to a hash variable
            if (exists $statistics_task{$name}) {
                $statistics_task{$name}{'total_time'} += $time;
                $statistics_task{$name}{'total_cnt'}  += 1;
                $statistics_task{$name}{'max_time'}   = $time if ($statistics_task{$name}{'max_time'} < $time);
                $statistics_task{$name}{'min_time'}   = $time if ($statistics_task{$name}{'min_time'} > $time);
                push @{$statistics_task{$name}{'values'}}, $time;
            } else {
                $statistics_task{$name}{'total_time'} = $time;
                $statistics_task{$name}{'total_cnt'}  = 1;
                $statistics_task{$name}{'max_time'}   = $time;
                $statistics_task{$name}{'min_time'}   = $time;
                push @{$statistics_task{$name}{'values'}}, $time;
            }
        }
    }
}

if ($output_file ne "") {
    print  OUTF "\nSummary of total execution times per Step:\n";
    print  OUTF "------------------------------------------\n\n";
    printf OUTF "%-14s  %-14s  %-14s  %-14s  %-6s  %s\n", "Median Time", "Average Time", "Minimum Time", "Maximum Time", "Values", "Step Name";
    printf OUTF "%s  %s  %s  %s  %s  %s\n",             "-"x14,        "-"x14,         "-"x14,         "-"x14,         "-"x6,    "-"x9;
} else {
    print       "\nSummary of total execution times per Step:\n";
    print       "------------------------------------------\n\n";
    printf      "%-14s  %-14s  %-14s  %-14s  %-6s  %s\n", "Median Time", "Average Time", "Minimum Time", "Maximum Time", "Values", "Step Name";
    printf      "%s  %s  %s  %s  %s  %s\n",             "-"x14,        "-"x14,         "-"x14,         "-"x14,         "-"x6,    "-"x9;
}

# Calculate average values and print output for steps
for $name (sort keys %statistics_step) {
    @values = @{$statistics_step{$name}{'values'}};
    $median = median(@values);
    $value_cnt = scalar @values;
    if ($output_file ne "") {
        printf OUTF "%14.6f  %14.6f  %14.6f  %14.6f  %6d  %s\n", $median, ($statistics_step{$name}{'total_time'} / $statistics_step{$name}{'total_cnt'}), $statistics_step{$name}{'min_time'}, $statistics_step{$name}{'max_time'}, $value_cnt, $name;
        print  OUTF $values_line if ($all_values == 1);

    } else {
        printf      "%14.6f  %14.6f  %14.6f  %14.6f  %6d  %s\n", $median, ($statistics_step{$name}{'total_time'} / $statistics_step{$name}{'total_cnt'}), $statistics_step{$name}{'min_time'}, $statistics_step{$name}{'max_time'}, $value_cnt, $name;
        print       $values_line if ($all_values == 1);
    }
}

if ($output_file ne "") {
    print  OUTF "\nSummary of total execution times per Task:\n";
    print  OUTF "------------------------------------------\n\n";
    printf OUTF "%-14s  %-14s  %-14s  %-14s  %-6s  %s\n", "Median Time", "Average Time", "Minimum Time", "Maximum Time", "Values", "Step Name";
    printf OUTF "%s  %s  %s  %s  %s  %s\n",             "-"x14,        "-"x14,         "-"x14,         "-"x14,         "-"x6,    "-"x9;
} else {
    print       "\nSummary of total execution times per Task:\n";
    print       "------------------------------------------\n\n";
    printf      "%-14s  %-14s  %-14s  %-14s  %-6s  %s\n", "Median Time", "Average Time", "Minimum Time", "Maximum Time", "Values", "Step Name";
    printf      "%s  %s  %s  %s  %s  %s\n",             "-"x14,        "-"x14,         "-"x14,         "-"x14,         "-"x6,    "-"x9;
}

# Calculate average values and print output for steps
for $name (sort keys %statistics_task) {
    @values = @{$statistics_task{$name}{'values'}};
    $median = median(@values);
    $value_cnt = scalar @values;
    if ($output_file ne "") {
        printf OUTF "%14.6f  %14.6f  %14.6f  %14.6f  %6d  %s\n", $median, ($statistics_task{$name}{'total_time'} / $statistics_task{$name}{'total_cnt'}), $statistics_task{$name}{'min_time'}, $statistics_task{$name}{'max_time'}, $value_cnt, $name;
        print  OUTF $values_line if ($all_values == 1);
    } else {
        printf      "%14.6f  %14.6f  %14.6f  %14.6f  %6d  %s\n", $median, ($statistics_task{$name}{'total_time'} / $statistics_task{$name}{'total_cnt'}), $statistics_task{$name}{'min_time'}, $statistics_task{$name}{'max_time'}, $value_cnt, $name;
        print       $values_line if ($all_values == 1);
    }
}

if ($output_file ne "") {
    close OUTF;
    print "Output written to file $output_file\n";
}

exit 0;

#
# Subroutines
# -----------
#

# -----------------------------------------------------------------------------
sub median {
    my @vals = sort {$a <=> $b} @_;
    my $len = @vals;
    my $cnt = 0;

    $values_line = "";

    if ($all_values == 1) {
        for (my $i=0; $i < $len; $i++) {
            $values_line .= sprintf "%14.6f  ", $vals[$i];
            $cnt++;
            if ($cnt == $values_per_line) {
                $values_line .= "\n";
                $cnt = 0;
            }
        }
        if ($values_line !~ /\n$/) {
            $values_line .= "\n";
        }
        $values_line = "\nValues printed with $values_per_line per line starting with lowest value:\n$values_line\n";
    }

    if($len%2) {
        #odd
        return $vals[int($len/2)];
    } else {
        #even
        return ($vals[int($len/2)-1] + $vals[int($len/2)])/2;
    }
}


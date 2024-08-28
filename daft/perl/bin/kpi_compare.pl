#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.11
#  Date     : 2023-01-30 15:31:22
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022-2023
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
use version;

use General::File_Operations;

my $entry_filename = "";
my @errors = ();
my $exit_code = 0;
my $exit_filename = "";
my $hide_metrics_query = 0;
my %kpi_statistics;
my $max_kpi_difference = 10;
my $max_length_change_average = 13;
my $max_length_change_high = 13;
my $max_length_change_low = 12;
my $max_length_description = 11;
my $max_length_entry_average = 13;
my $max_length_entry_count = 8;
my $max_length_entry_high = 13;
my $max_length_entry_low = 12;
my $max_length_exit_average = 13;
my $max_length_exit_count = 8;
my $max_length_exit_high = 13;
my $max_length_exit_low = 12;
my $max_length_query = 13;
my $max_length_verdict = 7;
my $output_directory = "/tmp/kpi_compare";
my $progress_messages = 0;
my $show_help = 0;
my $traffic_performance_check = 0;
my $traffic_kpi_difference = 10;
my %traffic_vs_load_matrix;
    # The 1st key is the traffic key, the 2nd key is the load key.
    # The keys should contain the value from the 'Description' column of the KPI statistics data.
    # It will fetch the data values from the {<Description>}{'entry'}{average'} and {<Description>}{'exit'}{average'}
    # hash.

    # **********************
    # * BSF Traffic matrix *
    # **********************
    #$traffic_vs_load_matrix{'BSF Diameter [TPS]'}{'BSF Diameter FE Load (Average) [CPU Millicores]'} = 1;
    $traffic_vs_load_matrix{'BSF Diameter [TPS]'}{'BSF Diameter Load (Average) [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'BSF Diameter [TPS]'}{'BSF Diameter Proxy gRPC Load (Average) [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'BSF Diameter [TPS]'}{'BSF Manager Load [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'BSF Diameter [TPS]'}{'BSF Wide Column Database (Average) [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'BSF Diameter [TPS]'}{'BSF Worker Load (Average) [CPU Millicores]'} = 1;
    #
    #$traffic_vs_load_matrix{'BSF HTTP [TPS]'}{'BSF Diameter FE Load (Average) [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'BSF HTTP [TPS]'}{'BSF Diameter Load (Average) [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'BSF HTTP [TPS]'}{'BSF Diameter Proxy gRPC Load (Average) [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'BSF HTTP [TPS]'}{'BSF Manager Load [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'BSF HTTP [TPS]'}{'BSF Wide Column Database (Average) [CPU Millicores]'} = 1;
    $traffic_vs_load_matrix{'BSF HTTP [TPS]'}{'BSF Worker Load (Average) [CPU Millicores]'} = 1;

    # **********************
    # * SCP Traffic matrix *
    # **********************
    #$traffic_vs_load_matrix{'SCP Egress Requests [MPS]'}{'SCP Manager Load [CPU Millicores]'} = 1;
    $traffic_vs_load_matrix{'SCP Egress Requests [MPS]'}{'SCP Worker Load (Average) [CPU Millicores]'} = 1;
    #
    #$traffic_vs_load_matrix{'SCP Ingress Requests [MPS]'}{'SCP Manager Load [CPU Millicores]'} = 1;
    $traffic_vs_load_matrix{'SCP Ingress Requests [MPS]'}{'SCP Worker Load (Average) [CPU Millicores]'} = 1;
    #
    #$traffic_vs_load_matrix{'SCP NRF Discovery Requests [MPS]'}{'SCP Manager Load [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'SCP NRF Discovery Requests [MPS]'}{'SCP Worker Load (Average) [CPU Millicores]'} = 1;
    #
    #$traffic_vs_load_matrix{'SLF NRF Discovery Requests [MPS]'}{'SCP Manager Load [CPU Millicores]'} = 1;
    #$traffic_vs_load_matrix{'SLF NRF Discovery Requests [MPS]'}{'SCP Worker Load (Average) [CPU Millicores]'} = 1;

    # ***********************
    # * SEPP Traffic matrix *
    # ***********************
    #$traffic_vs_load_matrix{'SEPP Egress Requests [MPS]'}{'SEPP Manager Load [CPU Millicores]'} = 1;
    $traffic_vs_load_matrix{'SEPP Egress Requests [MPS]'}{'SEPP Worker Load (Average) [CPU Millicores]'} = 1;
    #
    #$traffic_vs_load_matrix{'SEPP Ingress Requests [MPS]'}{'SEPP Manager Load [CPU Millicores]'} = 1;
    $traffic_vs_load_matrix{'SEPP Ingress Requests [MPS]'}{'SEPP Worker Load (Average) [CPU Millicores]'} = 1;

my %traffic_vs_load_statistics;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "c|traffic-performance-check"           => \$traffic_performance_check,
    "d|traffic-difference=f"                => \$traffic_kpi_difference,
    "e|entry-filename=s"                    => \$entry_filename,
    "h|help"                                => \$show_help,
    "m|max-difference=f"                    => \$max_kpi_difference,
    "o|output-directory=s"                  => \$output_directory,
    "p|progress-messages"                   => \$progress_messages,
    "q|hide-metrics-query"                  => \$hide_metrics_query,
    "x|exit-filename=s"                     => \$exit_filename,
);

if ( $show_help ) {
    show_help();
    exit 0;
}

unless (-d "$output_directory") {
    `mkdir -p $output_directory`;
    if ($? != 0) {
        print "Unable to create ouput direcctory: $output_directory\n";
        exit 1;
    }
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Read Entry KPI statistics into %kpi_statistics hash
read_file("entry", $entry_filename);

# Read Exit KPI statistics into %kpi_statistics hash
read_file("exit", $exit_filename);

# Compare Entry and Exit statistics in %kpi_statistics hash
compare_statistics();

if ($traffic_performance_check == 1) {
    calculate_and_compare_traffic_performance();
}

# Print the differences
print_output();

# Return success
exit $exit_code;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub calculate_and_compare_traffic_performance {
    my $change_average;
    my $entry_characteristics;
    my $exit_characteristics;
    my $load_entry_average;
    my $load_exit_average;
    my $traffic_entry_average;
    my $traffic_exit_average;
    my $verdict;

    for my $traffic_description (keys %traffic_vs_load_matrix) {
        if (exists $kpi_statistics{$traffic_description} &&
            exists $kpi_statistics{$traffic_description}{'entry'} &&
            exists $kpi_statistics{$traffic_description}{'entry'}{'average'} &&
            exists $kpi_statistics{$traffic_description}{'exit'} &&
            exists $kpi_statistics{$traffic_description}{'exit'}{'average'}) {

            for my $load_description (keys %{$traffic_vs_load_matrix{$traffic_description}}) {
                if (exists $kpi_statistics{$load_description} &&
                    exists $kpi_statistics{$load_description}{'entry'} &&
                    exists $kpi_statistics{$load_description}{'entry'}{'average'} &&
                    exists $kpi_statistics{$load_description}{'exit'} &&
                    exists $kpi_statistics{$load_description}{'exit'}{'average'}) {

                    $traffic_entry_average = $kpi_statistics{$traffic_description}{'entry'}{'average'};
                    $traffic_exit_average = $kpi_statistics{$traffic_description}{'exit'}{'average'};
                    $load_entry_average = $kpi_statistics{$load_description}{'entry'}{'average'};
                    $load_exit_average = $kpi_statistics{$load_description}{'exit'}{'average'};

                    $traffic_entry_average = 0 if ($traffic_entry_average eq "-");
                    $traffic_exit_average = 0 if ($traffic_exit_average eq "-");
                    $load_entry_average = 1 if ($load_entry_average eq "-" || $load_entry_average < 1);
                    $load_exit_average = 1 if ($load_exit_average eq "-" || $load_exit_average < 1);

                    $entry_characteristics = sprintf "%.3f", $traffic_entry_average / $load_entry_average;
                    $exit_characteristics = sprintf "%.3f", $traffic_exit_average / $load_exit_average;

                    # Calculate differences for 'average' value
                    if ($entry_characteristics != 0) {
                        # Non zero entry value, calculate difference in percent
                        $change_average = sprintf "%f", (($exit_characteristics - $entry_characteristics) / $entry_characteristics) * 100;
                    } elsif ($exit_characteristics != 0) {
                        # Zero entry value and non zero exit value, 100 percent difference
                        $change_average = sprintf "%f", 100;
                    } else {
                        # Both entry and exit values are zero, so no difference
                        $change_average = sprintf "%f", 0;
                    }

                    $traffic_vs_load_statistics{"$traffic_description / $load_description"}{'entry'} = $entry_characteristics;
                    $traffic_vs_load_statistics{"$traffic_description / $load_description"}{'exit'} = $exit_characteristics;
                    $traffic_vs_load_statistics{"$traffic_description / $load_description"}{'change'} = $change_average;

                    if ($change_average < 0 && abs($change_average) > $traffic_kpi_difference) {
                        # There is a decrease (negative value) of the calculated traffic / load and it's higher than allowed max value.
                        $verdict = sprintf "Average value difference decrease of %.1f%% is > %.1f%%, ", abs($change_average), $traffic_kpi_difference;
                    } else {
                        # Either the decrease is lower than specified max allowed value, or there is an increase indicating an improved traffic / load ratio.
                        $verdict = "OK";
                    }
                    $traffic_vs_load_statistics{"$traffic_description / $load_description"}{'verdict'} = $verdict;

                    $max_length_description = length("$traffic_description / $load_description") if ($max_length_description < length("$traffic_description / $load_description"));
                    $max_length_verdict     = length($verdict)                                   if ($max_length_verdict     < length($verdict));
                }
            }
        }
    }
}

# -----------------------------------------------------------------------------
sub compare_statistics {
    my $change_average;
    my $change_count;
    my $change_high;
    my $change_low;
    my $description;
    my $entry_average;
    my $entry_count;
    my $entry_high;
    my $entry_low;
    my $exit_average;
    my $exit_count;
    my $exit_high;
    my $exit_low;
    my $filename;
    my $not_existing;
    my $query;
    my $rc = 0;
    my @result;
    my $verdict;

    # Compare entry and exit values and calculate max column lengths
    print "Comparing Entry and Exit KPI files\n" if $progress_messages;
    for my $description (sort keys %kpi_statistics) {
        $query = $kpi_statistics{$description}{'query'};

        $not_existing = 0;
        if (exists $kpi_statistics{$description}{'entry'}) {
            $entry_average  = $kpi_statistics{$description}{'entry'}{'average'};
            $entry_low   = $kpi_statistics{$description}{'entry'}{'low'};
            $entry_high  = $kpi_statistics{$description}{'entry'}{'high'};
            $entry_count = $kpi_statistics{$description}{'entry'}{'count'};
        } else {
            push @errors, sprintf "Entry KPI for '%s' does not exist\n", $description;
            $not_existing = 1;
            $entry_average  = "-";
            $entry_low   = "-";
            $entry_high  = "-";
            $entry_count = "-";
            # Store dummy data
            $kpi_statistics{$description}{'entry'}{'average'} = "-";
            $kpi_statistics{$description}{'entry'}{'low'} = "-";
            $kpi_statistics{$description}{'entry'}{'high'} = "-";
            $kpi_statistics{$description}{'entry'}{'count'} = "-";
        }
        if (exists $kpi_statistics{$description}{'exit'}) {
            $exit_average  = $kpi_statistics{$description}{'exit'}{'average'};
            $exit_low   = $kpi_statistics{$description}{'exit'}{'low'};
            $exit_high  = $kpi_statistics{$description}{'exit'}{'high'};
            $exit_count = $kpi_statistics{$description}{'exit'}{'count'};
        } else {
            push @errors, sprintf "Exit KPI for '%s' does not exist\n", $description;
            $not_existing = 1;
            $exit_average  = "-";
            $exit_low   = "-";
            $exit_high  = "-";
            $exit_count = "-";
            # Store dummy data
            $kpi_statistics{$description}{'exit'}{'average'} = "-";
            $kpi_statistics{$description}{'exit'}{'low'} = "-";
            $kpi_statistics{$description}{'exit'}{'high'} = "-";
            $kpi_statistics{$description}{'exit'}{'count'} = "-";
        }

        $verdict = "";
        if ($not_existing == 0) {
            # Values from both Entry and Exit exists
            # Calculate differences for 'average' value
            if ($kpi_statistics{$description}{'entry'}{'average'} != 0) {
                # Non zero entry value, calculate difference in percent
                $change_average = sprintf "%f", (($kpi_statistics{$description}{'exit'}{'average'} - $kpi_statistics{$description}{'entry'}{'average'}) / $kpi_statistics{$description}{'entry'}{'average'}) * 100;
            } elsif ($kpi_statistics{$description}{'exit'}{'average'} != 0) {
                # Zero entry value and non zero exit value, 100 percent difference
                $change_average = sprintf "%f", 100;
            } else {
                # Both entry and exit values are zero, so no difference
                $change_average = sprintf "%f", 0;
            }

            # Calculate differences for 'low' value
            if ($kpi_statistics{$description}{'entry'}{'low'} != 0) {
                # Non zero entry value, calculate difference in percent
                $change_low = sprintf "%f", (($kpi_statistics{$description}{'exit'}{'low'} - $kpi_statistics{$description}{'entry'}{'low'}) / $kpi_statistics{$description}{'entry'}{'low'}) * 100;
            } elsif ($kpi_statistics{$description}{'exit'}{'low'} != 0) {
                # Zero entry value and non zero exit value, 100 percent difference
                $change_low = sprintf "%f", 100;
            } else {
                # Both entry and exit values are zero, so no difference
                $change_low = sprintf "%f", 0;
            }

            # Calculate differences for 'high' value
            if ($kpi_statistics{$description}{'entry'}{'high'} != 0) {
                # Non zero entry value, calculate difference in percent
                $change_high = sprintf "%f", (($kpi_statistics{$description}{'exit'}{'high'} - $kpi_statistics{$description}{'entry'}{'high'}) / $kpi_statistics{$description}{'entry'}{'high'}) * 100;
            } elsif ($kpi_statistics{$description}{'exit'}{'high'} != 0) {
                # Zero entry value and non zero exit value, 100 percent difference
                $change_high = sprintf "%f", 100;
            } else {
                # Both entry and exit values are zero, so no difference
                $change_high = sprintf "%f", 0;
            }

            if (abs($change_average) > $max_kpi_difference) {
                $verdict .= sprintf "Average value difference %+.1f%% is > %.1f%%, ", $change_average, $max_kpi_difference;
            }
            if (abs($change_low) > $max_kpi_difference) {
                $verdict .= sprintf "Low value difference %+.1f%% is > %.1f%%, ", $change_low, $max_kpi_difference;
            }
            if (abs($change_high) > $max_kpi_difference) {
                $verdict .= sprintf "High value difference %+.1f%% is > %.1f%%, ", $change_high, $max_kpi_difference;
            }
            $verdict =~ s/,\s+$//;
            if ($verdict eq "") {
                $verdict = "OK";
            }
        } else {
            # One of the two values are missing
            $change_average = "-";
            $change_low = "-";
            $change_high = "-";
            $verdict = "Missing values, no compare done";
        }

        # Store change values
        $kpi_statistics{$description}{'change'}{'average'} = $change_average;
        $kpi_statistics{$description}{'change'}{'low'} = $change_low;
        $kpi_statistics{$description}{'change'}{'high'} = $change_high;
        $kpi_statistics{$description}{'verdict'} = $verdict;

        # Calculate max column width
        $max_length_change_high    = length($change_high)    if ($max_length_change_high    < length($change_high));
        $max_length_change_low     = length($change_low)     if ($max_length_change_low     < length($change_low));
        $max_length_change_average = length($change_average) if ($max_length_change_average < length($change_average));
        $max_length_description    = length($description)    if ($max_length_description    < length($description));
        $max_length_entry_count    = length($entry_count)    if ($max_length_entry_count    < length($entry_count));
        $max_length_entry_high     = length($entry_high)     if ($max_length_entry_high     < length($entry_high));
        $max_length_entry_low      = length($entry_low)      if ($max_length_entry_low      < length($entry_low));
        $max_length_entry_average  = length($entry_average)  if ($max_length_entry_average  < length($entry_average));
        $max_length_exit_count     = length($exit_count)     if ($max_length_exit_count     < length($exit_count));
        $max_length_exit_high      = length($exit_high)      if ($max_length_exit_high      < length($exit_high));
        $max_length_exit_low       = length($exit_low)       if ($max_length_exit_low       < length($exit_low));
        $max_length_exit_average   = length($exit_average)   if ($max_length_exit_average   < length($exit_average));
        $max_length_query          = length($query)          if ($max_length_query          < length($query));
        $max_length_verdict        = length($verdict)        if ($max_length_verdict        < length($verdict));
    }
}

# -----------------------------------------------------------------------------
sub print_output {
    my $change_average;
    my $change_high;
    my $change_low;
    my @errors = ();
    my $filename;
    my @summary_lines = ();
    my @tsv_lines = ();
    my $rc;
    my $verdict_kpi_not_ok_cnt = 0;
    my $verdict_traffic_performance_not_ok_cnt = 0;

    #
    # KPI Counter Details
    # As calculated from KPI counters collected from the node.
    #

    push @summary_lines, ("KPI Counter Details", "===================", "");

    # Update the @summary_lines and @tsv_lines arrays with headings before printing it.
    if ($hide_metrics_query == 0) {

        push @summary_lines, sprintf "%-${max_length_entry_average}s  %-${max_length_entry_low}s  %-${max_length_entry_high}s  %-${max_length_entry_count}s  %-${max_length_exit_average}s  %-${max_length_exit_low}s  %-${max_length_exit_high}s  %-${max_length_exit_count}s  %-${max_length_change_average}s  %-${max_length_change_low}s  %s",
            "Entry",
            "Entry",
            "Entry",
            "Entry",
            "Exit",
            "Exit",
            "Exit",
            "Exit",
            "Change",
            "Change",
            "Change";

        push @summary_lines, sprintf "%-${max_length_entry_average}s  %-${max_length_entry_low}s  %-${max_length_entry_high}s  %-${max_length_entry_count}s  %-${max_length_exit_average}s  %-${max_length_exit_low}s  %-${max_length_exit_high}s  %-${max_length_exit_count}s  %-${max_length_change_average}s  %-${max_length_change_low}s  %-${max_length_change_high}s  %-${max_length_description}s  %-${max_length_query}s  %s",
            "Average Value",
            "Lowest Value",
            "Highest Value",
            "# Values",
            "Average Value",
            "Lowest Value",
            "Highest Value",
            "# Values",
            "Average Value",
            "Lowest Value",
            "Highest Value",
            "Description",
            "Metrics Query",
            "Verdict";

        push @summary_lines, sprintf "%s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s",
            "-" x ${max_length_entry_average},
            "-" x ${max_length_entry_low},
            "-" x ${max_length_entry_high},
            "-" x ${max_length_entry_count},
            "-" x ${max_length_exit_average},
            "-" x ${max_length_exit_low},
            "-" x ${max_length_exit_high},
            "-" x ${max_length_exit_count},
            "-" x ${max_length_change_average},
            "-" x ${max_length_change_low},
            "-" x ${max_length_change_high},
            "-" x ${max_length_description},
            "-" x ${max_length_query},
            "-" x ${max_length_verdict};

    } else {

        push @summary_lines, sprintf "%-${max_length_description}s  %s",
            "Description",
            "Metrics Query";

        push @summary_lines, sprintf "%s  %s",
            "-" x ${max_length_description},
            "-" x ${max_length_query};

        for my $description (sort keys %kpi_statistics) {

            push @summary_lines, sprintf "%-${max_length_description}s  %s",
                $description,
                $kpi_statistics{$description}{'query'};
        }

        push @summary_lines, "";

        push @summary_lines, sprintf "%-${max_length_entry_average}s  %-${max_length_entry_low}s  %-${max_length_entry_high}s  %-${max_length_entry_count}s  %-${max_length_exit_average}s  %-${max_length_exit_low}s  %-${max_length_exit_high}s  %-${max_length_exit_count}s  %-${max_length_change_average}s  %-${max_length_change_low}s  %s",
            "Entry",
            "Entry",
            "Entry",
            "Entry",
            "Exit",
            "Exit",
            "Exit",
            "Exit",
            "Change",
            "Change",
            "Change";

        push @summary_lines, sprintf "%-${max_length_entry_average}s  %-${max_length_entry_low}s  %-${max_length_entry_high}s  %-${max_length_entry_count}s  %-${max_length_exit_average}s  %-${max_length_exit_low}s  %-${max_length_exit_high}s  %-${max_length_exit_count}s  %-${max_length_change_average}s  %-${max_length_change_low}s  %-${max_length_change_high}s  %-${max_length_description}s  %s",
            "Average Value",
            "Lowest Value",
            "Highest Value",
            "# Values",
            "Average Value",
            "Lowest Value",
            "Highest Value",
            "# Values",
            "Average Value",
            "Lowest Value",
            "Highest Value",
            "Description",
            "Verdict";

        push @summary_lines, sprintf "%s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s",
            "-" x ${max_length_entry_average},
            "-" x ${max_length_entry_low},
            "-" x ${max_length_entry_high},
            "-" x ${max_length_entry_count},
            "-" x ${max_length_exit_average},
            "-" x ${max_length_exit_low},
            "-" x ${max_length_exit_high},
            "-" x ${max_length_exit_count},
            "-" x ${max_length_change_average},
            "-" x ${max_length_change_low},
            "-" x ${max_length_change_high},
            "-" x ${max_length_description},
            "-" x ${max_length_verdict};

    }

    push @tsv_lines, sprintf "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
        "Description",
        "Metrics Query",
        "Entry Average Value",
        "Entry Lowest Value",
        "Entry Highest Value",
        "Entry # Values",
        "Exit Average Value",
        "Exit Lowest Value",
        "Exit Highest Value",
        "Exit # Values",
        "Change Average Value",
        "Change Lowest Value",
        "Change Highest Value",
        "Verdict";

    # Format the output
    print "Formatting the Output\n" if $progress_messages;
    for my $description (sort keys %kpi_statistics) {

        push @tsv_lines, sprintf "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%%\t%s%%\t%s%%\t%s",
            $description,
            $kpi_statistics{$description}{'query'},
            $kpi_statistics{$description}{'entry'}{'average'},
            $kpi_statistics{$description}{'entry'}{'low'},
            $kpi_statistics{$description}{'entry'}{'high'},
            $kpi_statistics{$description}{'entry'}{'count'},
            $kpi_statistics{$description}{'exit'}{'average'},
            $kpi_statistics{$description}{'exit'}{'low'},
            $kpi_statistics{$description}{'exit'}{'high'},
            $kpi_statistics{$description}{'exit'}{'count'},
            $kpi_statistics{$description}{'change'}{'average'},
            $kpi_statistics{$description}{'change'}{'low'},
            $kpi_statistics{$description}{'change'}{'high'},
            $kpi_statistics{$description}{'verdict'};

        if ($kpi_statistics{$description}{'change'}{'average'} ne "-") {
            $change_average = sprintf "%+.1f%%", $kpi_statistics{$description}{'change'}{'average'};
        } else {
            $change_average = "-";
        }
        if ($kpi_statistics{$description}{'change'}{'low'} ne "-") {
            $change_low = sprintf "%+.1f%%", $kpi_statistics{$description}{'change'}{'low'};
        } else {
            $change_low = "-";
        }
        if ($kpi_statistics{$description}{'change'}{'high'} ne "-") {
            $change_high = sprintf "%+.1f%%", $kpi_statistics{$description}{'change'}{'high'};
        } else {
            $change_high = "-";
        }

        if ($hide_metrics_query == 0) {

            push @summary_lines, sprintf "%${max_length_entry_average}s  %${max_length_entry_low}s  %${max_length_entry_high}s  %${max_length_entry_count}s  %${max_length_exit_average}s  %${max_length_exit_low}s  %${max_length_exit_high}s  %${max_length_exit_count}s  %${max_length_change_average}s  %${max_length_change_low}s  %${max_length_change_high}s  %-${max_length_description}s  %-${max_length_query}s  %s",
                $kpi_statistics{$description}{'entry'}{'average'},
                $kpi_statistics{$description}{'entry'}{'low'},
                $kpi_statistics{$description}{'entry'}{'high'},
                $kpi_statistics{$description}{'entry'}{'count'},
                $kpi_statistics{$description}{'exit'}{'average'},
                $kpi_statistics{$description}{'exit'}{'low'},
                $kpi_statistics{$description}{'exit'}{'high'},
                $kpi_statistics{$description}{'exit'}{'count'},
                $change_average,
                $change_low,,
                $change_high,
                $description,
                $kpi_statistics{$description}{'query'},
                $kpi_statistics{$description}{'verdict'};

        } else {

            push @summary_lines, sprintf "%${max_length_entry_average}s  %${max_length_entry_low}s  %${max_length_entry_high}s  %${max_length_entry_count}s  %${max_length_exit_average}s  %${max_length_exit_low}s  %${max_length_exit_high}s  %${max_length_exit_count}s  %${max_length_change_average}s  %${max_length_change_low}s  %${max_length_change_high}s  %-${max_length_description}s  %s",
                $kpi_statistics{$description}{'entry'}{'average'},
                $kpi_statistics{$description}{'entry'}{'low'},
                $kpi_statistics{$description}{'entry'}{'high'},
                $kpi_statistics{$description}{'entry'}{'count'},
                $kpi_statistics{$description}{'exit'}{'average'},
                $kpi_statistics{$description}{'exit'}{'low'},
                $kpi_statistics{$description}{'exit'}{'high'},
                $kpi_statistics{$description}{'exit'}{'count'},
                $change_average,
                $change_low,,
                $change_high,
                $description,
                $kpi_statistics{$description}{'verdict'};

        }

        if ($kpi_statistics{$description}{'verdict'} ne "OK") {
            $verdict_kpi_not_ok_cnt++;
            push @errors, sprintf "%-${max_length_description}s  %s",
                $description,
                $kpi_statistics{$description}{'verdict'};
        }
    }

    #
    # Traffic Characteristics Check
    # As calculated from average entry vs exit traffic and load values.
    #

    if (%traffic_vs_load_statistics) {
        push @summary_lines, ("", "Traffic Performance Check", "=========================", "");

        push @summary_lines, sprintf "%-${max_length_entry_average}s  %-${max_length_exit_average}s  %s",
            "Entry",
            "Exit",
            "Change";

        push @summary_lines, sprintf "%-${max_length_entry_average}s  %-${max_length_exit_average}s  %-${max_length_change_average}s  %-${max_length_description}s  %s",
            "Average Value",
            "Average Value",
            "Value",
            "Description",
            "Verdict";

        push @summary_lines, sprintf "%s  %s  %s  %s  %s",
            "-" x ${max_length_entry_average},
            "-" x ${max_length_exit_average},
            "-" x ${max_length_change_average},
            "-" x ${max_length_description},
            "-" x ${max_length_verdict};

        for my $description (sort keys %traffic_vs_load_statistics) {
            $change_average = sprintf "%+.1f%%", $traffic_vs_load_statistics{$description}{'change'};
            push @summary_lines, sprintf "%${max_length_entry_average}s  %${max_length_exit_average}s  %${max_length_change_average}s  %-${max_length_description}s  %s",
                $traffic_vs_load_statistics{$description}{'entry'},
                $traffic_vs_load_statistics{$description}{'exit'},
                $change_average,
                $description,
                $traffic_vs_load_statistics{$description}{'verdict'};

            if ($traffic_vs_load_statistics{$description}{'verdict'} ne "OK") {
                $verdict_traffic_performance_not_ok_cnt++;
                push @errors, sprintf "%-${max_length_description}s  %s",
                    $description,
                    $traffic_vs_load_statistics{$description}{'verdict'};
            }
        }
    }

    #
    # Errors
    #

    if (@errors) {
        unshift @errors, ( "", "Errors", "======", "", (sprintf "%-${max_length_description}s  %s", "Description", "Verdict"), (sprintf "%s  %s", "-" x ${max_length_description}, "-" x ${max_length_verdict}) );
        push @summary_lines, @errors;
        $exit_code = 2;
    }
    push @summary_lines, ("", "Result Summary", "==============", "", "Verdict  Check", "-------  -----------------------------");

    if ($verdict_kpi_not_ok_cnt == 0) {
        push @summary_lines, "OK       KPI Counter Check";
    } else {
        push @summary_lines, "Not OK   KPI Counter Check";
    }
    if (%traffic_vs_load_statistics) {
        if ($verdict_traffic_performance_not_ok_cnt == 0) {
            push @summary_lines, "OK       Traffic Performance Check";
        } else {
            push @summary_lines, "Not OK   Traffic Performance Check";
        }
    }

    print join "\n", @summary_lines;
    print "\n";

    $filename = "$output_directory/difference_entry_exit_kpi.txt";
    $rc = General::File_Operations::write_file(
        {
            "filename"      => $filename,
            "output-ref"    => \@summary_lines,
        }
    );
    if ($rc != 0) {
        print "Failed to write file '$filename'\n";
        exit 1;
    }

    $filename = "$output_directory/difference_entry_exit_kpi.tsv";
    $rc = General::File_Operations::write_file(
        {
            "filename"      => $filename,
            "output-ref"    => \@tsv_lines,
        }
    );
    if ($rc != 0) {
        print "Failed to write file '$filename'\n";
        exit 1;
    }
}

# -----------------------------------------------------------------------------
sub read_file {
    my $type = shift;
    my $filename = shift;

    my $average;
    my $low;
    my $high;
    my $first;
    my $last;
    my $count;
    my $description;
    my $query;
    my $rc;
    my @result;

    unless (-f "$filename") {
        print "Not able to find the file '$filename'\n";
        exit 1;
    }

    print "Processing KPI file: $filename\n" if $progress_messages;
    $rc = General::File_Operations::read_file(
        {
            "filename"            => $filename,
            "output-ref"          => \@result,
            "include-pattern"     => '^\s*(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\d+)\s+(.+)\s+(\S+)\s*$',
        }
    );
    if ($rc != 0) {
        print "Unable to read the file '$filename'\n";
        exit 1;
    }
    if (scalar @result == 0) {
        print "No KPI data found in the file '$filename'\n";
        exit 1;
    }
    for (@result) {
        if (/^\s*(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\d+)\s+(.+)\s+(\S+)\s*$/) {
            #         0.271         0.225          0.403         0.266          0.399      1677  BSF DSL Load [%]                                         avg(bsf_dsl_load)
            $average = $1;
            $low = $2;
            $high = $3;
            $first = $4;
            $last = $5;
            $count = $6;
            $description = $7;
            $query = $8;

            $description =~ s/\s+$//;
            $query =~ s/^\s+//;
            $query =~ s/\s+$//;
            $kpi_statistics{$description}{$type}{'average'} = $average;
            $kpi_statistics{$description}{$type}{'low'} = $low;
            $kpi_statistics{$description}{$type}{'high'} = $high;
            $kpi_statistics{$description}{$type}{'first'} = $first;
            $kpi_statistics{$description}{$type}{'last'} = $last;
            $kpi_statistics{$description}{$type}{'count'} = $count;

            if (exists $kpi_statistics{$description}{'query'}) {
                if ($kpi_statistics{$description}{'query'} ne $query) {
                    push @errors, "$type KPI with description ($description) and query ($query) has a different query than previously stored ($kpi_statistics{$description}{'query'})\n";
                }
            } else {
                $kpi_statistics{$description}{'query'} = $query;
            }
        }
    }
}

# -----------------------------------------------------------------------------
sub show_help {
    print <<EOF;

Description:
============

This script compares KPI statistics between two collections, so called Entry and
Exit collection.
It will read files that has been created by the kpi_statistics.pl script in files
called kpi_statistics_summary.txt.
It will compare the values and write some statistics including a verdict if some
changes are above a specified limit.

It will write information to two files in the specified output directory:
    - difference_entry_exit_kpi.txt
    - difference_entry_exit_kpi.tsv


Syntax:
=======

$0 <MANDATORY> [<OPTIONAL>]

<MANDATORY> is one of the following parameters:

  -e <filename>         | --entry-filename <filename>
  -x <filename>         | --exit-filename <filename>

<OPTIONAL> are one or more of the following parameters:

  -c                    | --traffic-performance-check
  -d <float>            | --traffic-difference <float>
  -h                    | --help
  -m <float>            | --max-difference <float>
  -o <dirname>          | --output-directory <dirname>
  -p                    | --progress-messages
  -q                    | --hide-metrics-query


where:

  -c
  --traffic-performance-check
  ---------------------------
  If specified then the script will calculate the differences of the average values of
  MPS counters divided by the load in millicores and check that the difference is within
  the limits.


  -d <float>
  --traffic-difference <float>
  ----------------------------
  This specifies the maximum difference between calculated values as descibed in
  --traffic-performance-check above between entry and exit values. It is specified
  in percent which is calculated by the following formula:

      ((<KPI exit value> - <KPI entry value>) / <KPI entry value>) * 100

  So if the difference is greater than this value then the KPI is marked with an
  error.
  The default value if not specified is 10.


  -e <filename>
  --entry-filename <filename>
  ---------------------------
  Specifies the filename for the entry KPI values.


  -h
  --help
  ------
  Shows this help information.


  -m <float>
  --max-difference <float>
  ------------------------
  This specifies the maximum difference between KPI values collected e.g. before
  (entry) and after (exit) the upgrade. It is specified in percent which is
  calculated by the following formula:

      ((<KPI exit value> - <KPI entry value>) / <KPI entry value>) * 100

  So if the difference is greater than this value then the KPI is marked with an
  error.
  The default value if not specified is 10.


  -o <dirname>
  --output-directory <dirname>
  ----------------------------
  If specified then the output files (difference_entry_exit_kpi.txt and
  difference_entry_exit_kpi.tsv) will be written to this directory instead of
  the default directory called /tmp/kpi_compare.


  -p
  --progress-messages
  -------------------
  If specified then it will print progress messages to the STDOUT, if not specified
  then only the result is printed.


  -q
  --hide-metrics-query
  --------------------
  If specified then the metric-query column is not shown in the table printed
  to the screen, instead it is printed as a separate table showing the description
  to query information.
  This does not affect the tsv file output.


  -x <filename>
  --exit-filename <filename>
  --------------------------
  Specifies the filename for the exit KPI values.


Return code:
============

   0:  Successful, the execution was successful.
   1: Unsuccessful, some failure was reported.
   2: Unsuccessful, some KPI counters are outside of allowed values.

EOF
}

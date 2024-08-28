#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 2.16
#  Date     : 2024-05-07 13:30:23
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022-2024
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

use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Json_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;

# Uncomment the following 2 lines if you need to use this module to print debug information.
# use Data::Dumper;
# $Data::Dumper::Sortkeys = 1;

# Change the value below to 1 to use 'echo' to append to output file.
# NOTE:
# Just observe that changing it to 1 will increase the time it takes to parse
# data from file substantially.
# For example parsing a file with KPI data for 7 hours takes about 11 seconds
# when the value is 0 and about 5 minutes and 30 seconds when using value 1.
my $use_echo_to_append_output_file = 0;

my $abort_execution = 0;
my $begin_timestamp = "";
my $cmyp_ip;
my $cmyp_new_password = "";
my $cmyp_password = "rootroot";
my $cmyp_port;
my $cmyp_user = "expert";
my $ctrl_c_pressed = 0;
my $current_epoch = time();
my $date_offset = "";
my $end_timestamp = "";
my $error_cnt = 0;
my $future_information = "";
my $generate_ascii_graph = 1;
my $generate_png_graph = 1;
my $image_size_ascii = "300,75";
my $image_size_png = "1920,1080";
my $input_file = "";
my $kpi_file = dirname(dirname(dirname(abs_path $0))) . "/templates/kpi/all_sc_kpi.json";
my $kpi_load_file = "";
my %kpi_hash;
    # key1: metrics-query string.
    # key2: The following:
    #   "description": Short description of the KPI.
    #   "unit":        What type of unit to use on the Y-axis in the graphs.
    #                  This is the type mentions in the "[ ]" part of the description.
    #   "values":      An array with all collected values, which includes the time stamp
    #                  and the value, separated by a tab character.
    #   "average":     Calculated average value.
    #   "lowest":      Calculated lowest value.
    #   "highest":     Calculated highest value.
    #   "first":       The first value.
    #   "last":        The last value.
my $kubeconfig = "";
my $logfile = "";
my $namespace = "";
my $no_data_files = 0;
my $no_graphs = 0;
my $no_logfile = 0;
my $no_tty = 0;
my $output_directory = ".";
my $output_file = "";
my $past_information = "";
my $prefer_recording_rule = 1;
my $printout_file = "";
# For debug purpose remove the comment from the following line.
# $printout_file = "/proj/DSC_GIT/eustone/_miscellaneous_files/bsf_kpi_values.log";
my $rc;
my $repeat_count = 0;
my $repeat_time = 0;
my $resolution = 15;
my @result;
my $return_code = 0;
my $show_help = 0;
my $sleep_time;
my $stop_time;
my $time_range = "";
my $verbose = 0;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "c|kubeconfig=s"           => \$kubeconfig,
    "d|date-offset=s"          => \$date_offset,
    "f|future=s"               => \$future_information,
    "h|help"                   => \$show_help,
    "i|input-file=s"           => \$input_file,
    "k|kpi-file=s"             => \$kpi_file,
    "l|log-file=s"             => \$logfile,
    "n|namespace=s"            => \$namespace,
    "o|output-directory=s"     => \$output_directory,
    "p|past=s"                 => \$past_information,
    "r|resolution=i"           => \$resolution,
    "v|verbose"                => \$verbose,
    "image-size-ascii=s"       => \$image_size_ascii,
    "image-size-png=s"         => \$image_size_png,
    "cmyp-ip=s"                => \$cmyp_ip,
    "cmyp-new-password=s"      => \$cmyp_new_password,
    "cmyp-password=s"          => \$cmyp_password,
    "cmyp-port=i"              => \$cmyp_port,
    "cmyp-user=s"              => \$cmyp_user,
    "no-ascii-graph"           => sub { $generate_ascii_graph = 0; },
    "no-png-graph"             => sub { $generate_png_graph = 0; },
    "no-data-files"            => \$no_data_files,
    "no-logfile"               => \$no_logfile,
    "no-graphs"                => \$no_graphs,
    "no-tty"                   => \$no_tty,
    "output-file=s"            => \$output_file,
    "prefer-metrics-query"     => sub { $prefer_recording_rule = 0; },
    "prefer-recording-rule"    => \$prefer_recording_rule,
);

if ( $show_help ) {
    show_help();
    exit 0;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Check validity of command parameters
check_parameters();

# Create the output directory, if needed
handle_output_directory();

# Start log file, if needed
handle_log_file();

# Read the KPI definition file
handle_kpi_file();

if ($input_file ne "") {
    # Read data from file instead of from the node
    handle_input_file();
} else {
    # Install a CTRL-C handler
    $SIG{INT} = sub { $ctrl_c_pressed++; };
    # Install a handler for SIGABRT
    $SIG{ABRT} = sub { $abort_execution++; };

    read_kpi_data_from_node();
}

if ($error_cnt > 0) {
    cleanup_and_exit(1);
} else {
    cleanup_and_exit($return_code);
}

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub check_parameters {
    if ($kubeconfig ne "" && -f "$kubeconfig") {
        # Set kubeconfig for any used library functions
        ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);

        $kubeconfig = " --kubeconfig=$kubeconfig";
    } elsif ($kubeconfig ne "") {
        print "The kubeconfig file does not exist\n" if ($no_tty == 0);
        exit 1;
    }

    unless ($image_size_ascii =~ /^\d+,\d+$/) {
        print "Incorrect format of --image-size-ascii=$image_size_ascii, should be e.g. --image-size-ascii=300,75\n" if ($no_tty == 0);
        exit 1;
    }

    unless ($image_size_png =~ /^\d+,\d+$/) {
        print "Incorrect format of --image-size-png=$image_size_png, should be e.g. --image-size-png=1920,1080\n" if ($no_tty == 0);
        exit 1;
    }

    # Check if we should fetch KPI data from some other timestamp than current time.
    if ($date_offset =~ /^\d{10}$/) {
        # Epoch time between the dates 'Sun Sep  9 03:46:40 CEST 2001' and 'Sat Nov 20 18:46:39 CET 2286'
        # We are good to go
    } elsif ($date_offset ne "") {
        # The date offset is specified in some other kind of format, so we need to try to convert it to epoch time
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "date -d '$date_offset' +\%s",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0 || $result[0] !~ /^\d{10}$/) {
            print "Incorrect date format given in --date-offset\n" . join "\n", @result;
            exit 1;
        }
        $date_offset = $result[0];
    }

    if ($past_information ne "" && $future_information ne "") {
        print "You cannot specify both --past and --future, pick one\n" if ($no_tty == 0);
        exit 1;
    } elsif ($past_information ne "") {
        if ($past_information =~ /^\d+$/) {
            # No time unit specified, so assume it's a counter of how many collections from
            # the past should be fetched which means we need to convert it into the proper format.
            if ($past_information < 2) {
                # Only need to fetch one snapshot of the current value
                $past_information = "";
            } else {
                # Convert it to the proper format to be used in the request to prometheus
                $time_range = sprintf '[%is:%is]', ($past_information * $resolution), $resolution;
                if ($date_offset ne "") {
                    $time_range .= "\@$date_offset";
                }
            }
        } elsif ($past_information =~ /^\d+[smhdw]$/) {
            # A time unit was given, just change the format
            $time_range = sprintf '[%s:%is]', $past_information, $resolution;
            if ($date_offset ne "") {
                $time_range .= "\@$date_offset";
            }
        } else {
            print "Incorrect format of --past=$past_information, should be a number or a number followed by 's', 'm', 'h', 'd' or 'w' e.g. 10 or 120s\n" if ($no_tty == 0);
            exit 1;
        }
    } elsif ($future_information ne "") {
        if ($date_offset ne "") {
            print "WARNING: It makes no sense to specify --date-offset for future collection, so this parameter is ignored\n";
        }
        if ($future_information =~ /^\d+$/) {
            # No time unit specified, so assume it's a counter of how many collections from
            # the past should be fetched which means we need to convert it into the proper format.
            if ($future_information < 2) {
                # Only need to fetch one snapshot of the current value
                $repeat_count = 1;
            } else {
                # Update $repeat_count variable
                $repeat_count = $future_information;
            }
        } elsif ($future_information =~ /^(\d+)([smh])$/) {
            # A time unit was given, update the $repeat_time variable
            if ($1 == 0) {
                print "Incorrect format of --future=$future_information, should be a number >0\n" if ($no_tty == 0);
                exit 1;
            }
            if ($2 eq "s") {
                $repeat_time = $1;
            } elsif ($2 eq "m") {
                $repeat_time = sprintf "%d", ($1 * 60);
            } elsif ($2 eq "h") {
                $repeat_time = sprintf "%d", ($1 * 3600);
            } elsif ($2 eq "d") {
                $repeat_time = sprintf "%d", ($1 * 3600 * 24);
            } elsif ($2 eq "w") {
                $repeat_time = sprintf "%d", ($1 * 3600 * 24 * 7);
            }
            $stop_time = time() + $repeat_time;
        } else {
            print "Incorrect format of --future=$future_information, should be a number or a number followed by 's', 'm', 'h', 'd' or 'w' e.g. 10 or 120s\n" if ($no_tty == 0);
            exit 1;
        }
    }
}

# -----------------------------------------------------------------------------
sub cleanup_and_exit {
    my $rc = shift;

    # Close the output file
    if ($output_file ne "") {
        if ($use_echo_to_append_output_file == 0) {
            close(OUTF);
        }
        print_message("\nOutput file written to $output_file\n", ! $verbose);
    }

    if ($no_data_files == 1 && -f "$kpi_load_file") {
        # Remove the file but ignore the result
        `rm -fr $kpi_load_file >/dev/null 2>&1`;
    }

    if ($no_data_files == 0) {
        # Remove empty directories, ignore any errors
        General::OS_Operations::send_command(
            {
                "command"       => "rmdir --ignore-fail-on-non-empty $output_directory/gnuplot $output_directory/graphs $output_directory",
                "hide-output"   => 1,
            }
        );

        if (-d "$output_directory") {
            print_message("\nOutput files written to directory $output_directory\n");
        }
    }

    # Stop logging of output to file
    if ($logfile) {
        print_message("\nDetailed log file written to $logfile\n", ! $verbose);
        General::Logging::log_disable();
    }

    exit $rc;
}

# -----------------------------------------------------------------------------
sub create_statistics {
    my $average = 0;
    my $col_length_avg = 13;
    my $col_length_min = 12;
    my $col_length_max = 13;
    my $col_length_cnt = 8;
    my $col_length_des = 11;
    my $col_length_met = 13;
    my $col_length_fir = 11;
    my $col_length_las = 10;
    my $count = 0;
    my $description;
    my $file_description;
    my $filename;
    my $first;
    my %kpi_data;
    my $last;
    my $max = 0;
    my $message;
    my $min = 999999999999999999999;
    my @script_commands_ascii = (
        '#!/bin/bash',
        '# This script will generate ASCII graphs of KPI values from collected data using gnuplot.',
        'script_dir=$(dirname $(realpath $0))',
        'current_dir=$(pwd)',
        'cd "$script_dir"',
        'if [ $? -ne 0 ]; then echo "Failed to change to directory $script_dir"; exit 1; fi',
        'gnuplot_cmd=$(which gnuplot 2>/dev/null)',
        'if [ $? -ne 0 ]; then echo -en "The \"gnuplot\" command is not in the path.\nTry to execute the following from the command shell and then try the script again:\nmodule add gnuplot\n"; exit 1; fi',
        '',
        'if [ $? -ne 0 ]; then',
        '    echo "Trying to load module gnuplot"',
        '    module add gnuplot',
        '    if [ $? -ne 0 ]; then cd "$current_dir"; exit 1; fi',
        '    gnuplot_cmd=$(which gnuplot 2>/dev/null)',
        '    if [ $? -ne 0 ]; then echo "Not able to find gnuplot"; cd "$current_dir"; exit 1; fi',
        'fi',
        'mkdir -p "$script_dir/../graphs/ascii"',
        'if [ $? -ne 0 ]; then echo "Failed to create directory $script_dir/../graphs/ascii/"; exit 1; fi',
        'output_dir=$(realpath "$script_dir/../graphs")',
        'echo "Creating ASCII graphs using gnuplot to directory \"$output_dir\""',
        'errorcnt=0',
    );
    my @script_commands_png = (
        '#!/bin/bash',
        '# This script will generate PNG graphs of KPI values from collected data using gnuplot.',
        'script_dir=$(dirname $(realpath $0))',
        'current_dir=$(pwd)',
        'cd "$script_dir"',
        'if [ $? -ne 0 ]; then echo "Failed to change to directory $script_dir"; exit 1; fi',
        'gnuplot_cmd=$(which gnuplot 2>/dev/null)',
        'if [ $? -ne 0 ]; then echo -en "The \"gnuplot\" command is not in the path.\nTry to execute the following from the command shell and then try the script again:\nmodule add gnuplot\n"; exit 1; fi',
        '',
        'if [ $? -ne 0 ]; then',
        '    echo "Trying to load module gnuplot"',
        '    module add gnuplot',
        '    if [ $? -ne 0 ]; then cd "$current_dir"; exit 1; fi',
        '    gnuplot_cmd=$(which gnuplot 2>/dev/null)',
        '    if [ $? -ne 0 ]; then echo "Not able to find gnuplot"; cd "$current_dir"; exit 1; fi',
        'fi',
        'mkdir -p "$script_dir/../graphs/png"',
        'if [ $? -ne 0 ]; then echo "Failed to create directory $script_dir/../graphs/png/"; exit 1; fi',
        'output_dir=$(realpath "$script_dir/../graphs")',
        'echo "Creating PNG graphs using gnuplot to directory \"$output_dir\""',
        'errorcnt=0',
    );
    my $sum = 0;
    my $value;
    my $y_axis;

    print_message("Creating gnuplot files\n");

    for my $key (sort keys %kpi_hash) {
        # Calculate, average, lowest and highest values
        $average = 0;
        $count = 0;
        $max = 0;
        $min = "";
        $sum = 0;
        $first = 0;
        $last = 0;
        for (@{$kpi_hash{$key}{'values'}}) {
            s/NaN/0/;
            if (/^(.+)\t(.+)$/) {
                $value = $2;
                $count++;
                if ($min ne "") {
                    $min = $value if ($value < $min);
                } else {
                    $min = $value;
                }
                $max = $value if ($value > $max);
                $sum = $sum + $value;
            }
        }
        $average = ($sum / $count) if ($count > 0);
        if ($average =~ /\./) {
            # A fractal number, shorten it to 3 decimal points
            $average = sprintf "%.3f", $average;
        }
        if ($max =~ /\./) {
            # A fractal number, shorten it to 3 decimal points
            $max = sprintf "%.3f", $max;
        }
        if ($min =~ /\./) {
            # A fractal number, shorten it to 3 decimal points
            $min = sprintf "%.3f", $min;
        }
        if (@{$kpi_hash{$key}{'values'}} && $kpi_hash{$key}{'values'}->[0] =~ /^.+\t(.+)$/) {
            $first = $1;
            if ($first =~ /\./) {
                # A fractal number, shorten it to 3 decimal points
                $first = sprintf "%.3f", $first;
            }
        }
        if (@{$kpi_hash{$key}{'values'}} && $kpi_hash{$key}{'values'}->[-1] =~ /^.+\t(.+)$/) {
            $last = $1;
            if ($last =~ /\./) {
                # A fractal number, shorten it to 3 decimal points
                $last = sprintf "%.3f", $last;
            }
        }
        $description = $kpi_hash{$key}{'description'};
        $kpi_hash{$key}{'average'} = $average;
        $kpi_hash{$key}{'lowest'} = $min;
        $kpi_hash{$key}{'highest'} = $max;
        $kpi_hash{$key}{'first'} = $first;
        $kpi_hash{$key}{'last'} = $last;
        $col_length_avg = length($kpi_hash{$key}{'average'}) if (length($kpi_hash{$key}{'average'}) > $col_length_avg);
        $col_length_min = length($kpi_hash{$key}{'lowest'}) if (length($kpi_hash{$key}{'lowest'}) > $col_length_min);
        $col_length_max = length($kpi_hash{$key}{'highest'}) if (length($kpi_hash{$key}{'highest'}) > $col_length_max);
        $col_length_fir = length($kpi_hash{$key}{'first'}) if (length($kpi_hash{$key}{'first'}) > $col_length_fir);
        $col_length_las = length($kpi_hash{$key}{'last'}) if (length($kpi_hash{$key}{'last'}) > $col_length_las);
        $col_length_cnt = length($count) if (length($count) > $col_length_cnt);
        $col_length_des = length($kpi_hash{$key}{'description'}) if (length($kpi_hash{$key}{'description'}) > $col_length_des && scalar @{$kpi_hash{$key}{'values'}} > 0);
        #$col_length_met = length($key) if (length($key) > $col_length_met);
        if (exists $kpi_data{$description}) {
            print_message("Error:Not unique description detected: $description\n");
        } else {
            $kpi_data{$description}{'average'} = $average;
            $kpi_data{$description}{'lowest'} = $min;
            $kpi_data{$description}{'highest'} = $max;
            $kpi_data{$description}{'first'} = $first;
            $kpi_data{$description}{'last'} = $last;
            $kpi_data{$description}{'metrics-query'} = $key;
            $kpi_data{$description}{'count'} = $count;
        }

        if ($no_data_files == 0) {
            if ($count > 1) {
                # Write GnuPlot data to file
                $file_description = $description;
                $file_description =~ s/\s+/_/g;
                $filename = "$output_directory/gnuplot/$file_description";
                $rc = General::File_Operations::write_file(
                    {
                        "filename"      => "$filename.data",
                        "output-ref"    => \@{$kpi_hash{$key}{'values'}},
                    }
                );
                if ($rc != 0) {
                    print_message("Failure to write GnuPlot KPI data to file: $filename.data\n");
                    $error_cnt++;
                    next;
                }

                if ($kpi_hash{$key}{'description'} =~ /^.+\[([^\]]+)\]$/) {
                    $y_axis = $1;
                } else {
                    $y_axis = $kpi_hash{$key}{'description'};
                }

                # Write GnuPlot config commands to file
                if ($y_axis eq '%') {
                    # To make the 100% case more visible show the scale from -10 to 110 %
                    # instead of starting from 0.
                    $rc = General::File_Operations::write_file(
                        {
                            "filename"      => "${filename}_ascii.conf",
                            "output-ref"    => [
                                "set title \"$description\"",
                                'set xdata time',
                                'set timefmt "%Y-%m-%dT%H:%M:%S"',
                                'set format x "%H:%M:%S"',
                                'set xtics rotate',
                                "set xlabel \"Time (from $begin_timestamp to $end_timestamp)\"",
                                "set ylabel \"$y_axis\"",
                                # 'set key right center',
                                "set terminal dumb size $image_size_ascii",
                                "set output \"../graphs/ascii/$file_description.ascii\"",
                                "plot \"$file_description.data\" using 1:2 with lines title \"$key\"",
                                'set yrange [-10:110]',
                                "set output \"../graphs/ascii/${file_description}_ZERO.ascii\"",
                                "plot \"$file_description.data\" using 1:2 with lines title \"$key\"",
                            ],
                        }
                    );
                    if ($rc != 0) {
                        print_message("Failure to write GnuPlot KPI config to file: ${filename}_ascii.conf\n");
                        $error_cnt++;
                        next;
                    }
                    $rc = General::File_Operations::write_file(
                        {
                            "filename"      => "${filename}_png.conf",
                            "output-ref"    => [
                                "set title \"$description\"",
                                'set xdata time',
                                'set timefmt "%Y-%m-%dT%H:%M:%S"',
                                'set format x "%H:%M:%S"',
                                'set xtics rotate',
                                "set xlabel \"Time (from $begin_timestamp to $end_timestamp)\"",
                                "set ylabel \"$y_axis\"",
                                # 'set key right center',
                                "set term png size $image_size_png",
                                "set output \"../graphs/png/$file_description.png\"",
                                "plot \"$file_description.data\" using 1:2 with lines title \"$key\"",
                                'set yrange [-10:110]',
                                "set output \"../graphs/png/${file_description}_ZERO.png\"",
                                "plot \"$file_description.data\" using 1:2 with lines title \"$key\"",
                            ],
                        }
                    );
                    if ($rc != 0) {
                        print_message("Failure to write GnuPlot KPI config to file: ${filename}_png.conf\n");
                        $error_cnt++;
                        next;
                    }
                } else {
                    $rc = General::File_Operations::write_file(
                        {
                            "filename"      => "${filename}_ascii.conf",
                            "output-ref"    => [
                                "set title \"$description\"",
                                'set xdata time',
                                'set timefmt "%Y-%m-%dT%H:%M:%S"',
                                'set format x "%H:%M:%S"',
                                'set xtics rotate',
                                "set xlabel \"Time (from $begin_timestamp to $end_timestamp)\"",
                                "set ylabel \"$y_axis\"",
                                # 'set key right center',
                                "set terminal dumb size $image_size_ascii",
                                "set output \"../graphs/ascii/$file_description.ascii\"",
                                "plot \"$file_description.data\" using 1:2 with lines title \"$key\"",
                                'set yrange [0:]',
                                "set output \"../graphs/ascii/${file_description}_ZERO.ascii\"",
                                "plot \"$file_description.data\" using 1:2 with lines title \"$key\"",
                            ],
                        }
                    );
                    if ($rc != 0) {
                        print_message("Failure to write GnuPlot KPI config to file: ${filename}_ascii.conf\n");
                        $error_cnt++;
                        next;
                    }
                    $rc = General::File_Operations::write_file(
                        {
                            "filename"      => "${filename}_png.conf",
                            "output-ref"    => [
                                "set title \"$description\"",
                                'set xdata time',
                                'set timefmt "%Y-%m-%dT%H:%M:%S"',
                                'set format x "%H:%M:%S"',
                                'set xtics rotate',
                                "set xlabel \"Time (from $begin_timestamp to $end_timestamp)\"",
                                "set ylabel \"$y_axis\"",
                                # 'set key right center',
                                "set term png size $image_size_png",
                                "set output \"../graphs/png/$file_description.png\"",
                                "plot \"$file_description.data\" using 1:2 with lines title \"$key\"",
                                'set yrange [0:]',
                                "set output \"../graphs/png/${file_description}_ZERO.png\"",
                                "plot \"$file_description.data\" using 1:2 with lines title \"$key\"",
                            ],
                        }
                    );
                    if ($rc != 0) {
                        print_message("Failure to write GnuPlot KPI config to file: ${filename}_png.conf\n");
                        $error_cnt++;
                        next;
                    }
                }

                push @script_commands_ascii, '';
                push @script_commands_ascii, sprintf 'echo "gnuplot \"%s_ascii.conf\""', $file_description;
                push @script_commands_ascii, sprintf 'gnuplot "%s_ascii.conf"', $file_description;
                push @script_commands_ascii, 'if [ $? -ne 0 ]; then ((errorcnt++)); fi';

                push @script_commands_png, '';
                push @script_commands_png, sprintf 'echo "gnuplot \"%s_png.conf\""', $file_description;
                push @script_commands_png, sprintf 'gnuplot "%s_png.conf"', $file_description;
                push @script_commands_png, 'if [ $? -ne 0 ]; then ((errorcnt++)); fi';
            } elsif ($count == 1) {
                print_message("Generation of graphs skipped because only 1 data point available for:\n  KPI Description: $description\n  metrics-query: $key\n\n", ! $verbose);
            } else {
                print_message("Generation of graphs skipped because no data available for:\n  KPI Description: $description\n  metrics-query: $key\n\n", ! $verbose);
                next;
            }
        }
    }

    if ($no_data_files == 0) {
        push @script_commands_ascii, '';
        push @script_commands_ascii, 'cd "$current_dir"';
        push @script_commands_ascii, 'if [ $? -ne 0 ]; then echo "Failed to change to directory $current_dir"; exit 1; fi';
        push @script_commands_ascii, 'if [ $errorcnt -eq 0 ]; then';
        push @script_commands_ascii, '    echo "Graphs successfully created in directory \"$output_dir/\""';
        push @script_commands_ascii, '    exit 0';
        push @script_commands_ascii, 'else';
        push @script_commands_ascii, '    echo "$errorcnt graphs could not be created, see directory \"$output_dir/\" for created graphs"';
        push @script_commands_ascii, '    exit 1';
        push @script_commands_ascii, 'fi';

        $filename = "$output_directory/gnuplot/generate_graphs_ascii.bash";
        $rc = General::File_Operations::write_file(
            {
                "filename"          => $filename,
                "output-ref"        => \@script_commands_ascii,
                "file-access-mode"  => "755",
            }
        );
        if ($rc != 0) {
            print_message("Failure to write graph generation bash script file: $filename\n");
            $error_cnt++;
        }

        push @script_commands_png, '';
        push @script_commands_png, 'cd "$current_dir"';
        push @script_commands_png, 'if [ $? -ne 0 ]; then echo "Failed to change to directory $current_dir"; exit 1; fi';
        push @script_commands_png, 'if [ $errorcnt -eq 0 ]; then';
        push @script_commands_png, '    echo "Graphs successfully created in directory \"$output_dir/\""';
        push @script_commands_png, '    exit 0';
        push @script_commands_png, 'else';
        push @script_commands_png, '    echo "$errorcnt graphs could not be created, see directory \"$output_dir/\" for created graphs"';
        push @script_commands_png, '    exit 1';
        push @script_commands_png, 'fi';

        $filename = "$output_directory/gnuplot/generate_graphs_png.bash";
        $rc = General::File_Operations::write_file(
            {
                "filename"          => $filename,
                "output-ref"        => \@script_commands_png,
                "file-access-mode"  => "755",
            }
        );
        if ($rc != 0) {
            print_message("Failure to write graph generation bash script file: $filename\n");
            $error_cnt++;
        }

        if ($no_graphs == 0) {
            if (General::OS_Operations::command_in_path("gnuplot") == 0) {
                $filename = "$output_directory/gnuplot/generate_graphs_ascii.bash";
                if ($generate_ascii_graph == 1) {
                    print_message("Creating ASCII graphs\n");

                    # Create the ASCII file using gnuplot
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "bash \"$filename\"",
                            "hide-output"   => 1,
                            "return-output" => \@result,
                        }
                    );
                    if ($rc != 0) {
                        print_message("\nFailed to create the ASCII graph files\n" . (join "\n", @result) . "\n");
                        $error_cnt++;
                    }
                } else {
                    my $temp_message = "";
                    General::Message_Generation::print_message_box(
                        {
                            "messages"  => [
                                "No ASCII graphs wanted, but if you want to generate graphs later then execute the following script:",
                                "  $filename",
                            ],
                            "max-length" => 0,
                            "return-output" => \$temp_message,
                        }
                    );
                    print_message($temp_message);
                }

                $filename = "$output_directory/gnuplot/generate_graphs_png.bash";
                if ($generate_png_graph == 1) {
                    print_message("Creating PNG graphs\n");

                    # Create the PNG file using gnuplot
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "bash \"$filename\"",
                            "hide-output"   => 1,
                            "return-output" => \@result,
                        }
                    );
                    if ($rc != 0) {
                        print_message("\nFailed to create the PNG graph files\n" . (join "\n", @result) . "\n");
                        $error_cnt++;
                    }
                } else {
                    my $temp_message = "";
                    General::Message_Generation::print_message_box(
                        {
                            "messages"  => [
                                "No PNG graphs wanted, but if you want to generate graphs later then execute the following script:",
                                "  $filename",
                            ],
                            "max-length" => 0,
                            "return-output" => \$temp_message,
                        }
                    );
                    print_message($temp_message);
                }
            } else {
                my $temp_message = "";
                General::Message_Generation::print_message_box(
                    {
                        "messages"  => [
                            "The command 'gnuplot' does not exist in the path so no graphs will be generated.",
                            "Maybe you need to execute 'module add gnuplot' on your VDI",
                            "or '/home/eccd/gnuplot_files/install_gnuplot.bash' on director-0",
                            "and then execute the following scripts:",
                            "  $output_directory/gnuplot/generate_graphs_ascii.bash",
                            "  $output_directory/gnuplot/generate_graphs_png.bash",
                        ],
                        "max-length" => 0,
                        "return-output" => \$temp_message,
                    }
                );
                print_message($temp_message);
            }
        } else {
            my $temp_message = "";
            General::Message_Generation::print_message_box(
                {
                    "messages"  => [
                        "No graphs wanted, but if you want to generate graphs later then execute the following scripts:",
                        "  $output_directory/gnuplot/generate_graphs_ascii.bash",
                        "  $output_directory/gnuplot/generate_graphs_png.bash",
                    ],
                    "max-length" => 0,
                    "return-output" => \$temp_message,
                }
            );
            print_message($temp_message);
        }
    }

    $message = "\nStatistics Summary\n";
    $message .= "==================\n\n";
    $message .= sprintf "%-${col_length_avg}s  %-${col_length_min}s  %-${col_length_max}s  %-${col_length_fir}s  %-${col_length_las}s  %-${col_length_cnt}s  %-${col_length_des}s  %-${col_length_met}s\n",
        "Average Value",
        "Lowest Value",
        "Highest Value",
        "First Value",
        "Last Value",
        "# Values",
        "Description",
        "Metrics Query";
    $message .= sprintf "%s  %s  %s  %s  %s  %s  %s  %s\n",
        "-"x$col_length_avg,
        "-"x$col_length_min,
        "-"x$col_length_max,
        "-"x$col_length_fir,
        "-"x$col_length_las,
        "-"x$col_length_cnt,
        "-"x$col_length_des,
        "-"x$col_length_met;
    if (1) {
        # The active code that sorts on the 'description' column.
        for my $key (sort keys %kpi_data) {
            if ($kpi_data{$key}{'count'} > 0) {
                $message .= sprintf "%${col_length_avg}s  %${col_length_min}s  %${col_length_max}s  %${col_length_fir}s  %${col_length_las}s  %${col_length_cnt}s  %-${col_length_des}s  %s\n",
                    $kpi_data{$key}{'average'},
                    $kpi_data{$key}{'lowest'},
                    $kpi_data{$key}{'highest'},
                    $kpi_data{$key}{'first'},
                    $kpi_data{$key}{'last'},
                    $kpi_data{$key}{'count'},
                    $key,
                    $kpi_data{$key}{'metrics-query'};
            }
        }
    } else {
        # TODO: This is currently dead code, use this if in the future we want to be able to sort on the 'metrics-query' column.
        for my $key (sort keys %kpi_hash) {
            if (scalar @{$kpi_hash{$key}{'values'}} > 0) {
                $message .= sprintf "%${col_length_avg}s  %${col_length_min}s  %${col_length_max}s  %${col_length_fir}s  %${col_length_las}s  %${col_length_cnt}s  %-${col_length_des}s  %s\n",
                    $kpi_hash{$key}{'average'},
                    $kpi_hash{$key}{'lowest'},
                    $kpi_hash{$key}{'highest'},
                    $kpi_hash{$key}{'first'},
                    $kpi_hash{$key}{'last'},
                    scalar @{$kpi_hash{$key}{'values'}},
                    $kpi_hash{$key}{'description'},
                    $key;
            }
        }
    }
    print_message($message);
    if ($no_data_files == 0 && open TMPF, ">>$output_directory/kpi_statistics_summary.txt") {
        print TMPF $message;
        close(TMPF);
    }
}

# -----------------------------------------------------------------------------
# This collects the KPI (Key Performance Index) values by connecting to CMYP
# CLI and issues the 'metrics-query <query string>' command.
# It then prints the found values in a different format to make it easier to parse.
#
sub get_kpi_from_node {
    my $command = dirname(dirname(dirname(abs_path $0))) . "/expect/bin/send_command_to_ssh.exp";
    my @std_error;

    # Add more parameter for fetching KPI values from CMYP
    $command .= " --user=$cmyp_user --password='$cmyp_password' --ip=$cmyp_ip --timeout=60 --stop-on-error --command-file='$kpi_load_file'";
    if ($cmyp_port) {
        $command .= " --port=$cmyp_port";
    }
    if ($cmyp_new_password ne "") {
        $command .= " --new-password='$cmyp_new_password'";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );

    if ( $rc == 0 ) {
        # Check STDOUT if password was changed
        for (@result) {
            if (/^Reenter new Password:\s*$/) {
                # It looks like the password was changed, update the $cmyp_password for next execution
                print_message("The password for user '$cmyp_user' has been changed\n");
                $cmyp_password = $cmyp_new_password;
                $cmyp_new_password = "";
                last;
            }
        }
        # Check STDERR if password was changed
        for (@std_error) {
            if (/^Reenter new Password:\s*$/) {
                # It looks like the password was changed, update the $cmyp_password for next execution
                print_message("The password for user '$cmyp_user' has been changed\n");
                $cmyp_password = $cmyp_new_password;
                $cmyp_new_password = "";
                last;
            }
        }
        parse_node_kpi_data();
    } else {
        print_message("Command '$command' failed.\n" . (join "\n", @std_error, @result) . "\n");
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub handle_input_file {
    my $metrics_query;
    my $parsed_data_cnt = 0;
    my $timestamp;
    my $value;

    unless (-f "$input_file") {
        print_message("Input file '$input_file' does not exist or cannot be read\n");
        cleanup_and_exit(1);
    }

    print_message("Reading input file\n");

    # Find out in what format the file is.
    # If raw data from the CMYP CLI, or processes data from this script.
    $rc = General::File_Operations::read_file(
        {
            "filename"              => $input_file,
            "output-ref"            => \@result,
        }
    );
    if ($rc != 0) {
        print_message("Failed to read data from the input file '$input_file'\n");
        cleanup_and_exit(1);
    }

    for (@result) {
        s/[\r\n]//g;

        if (/^([^\t]+)\t([^\t]+)\t([^\t]+)$/) {
            # Looks like we are reading already parsed data, so update the %kpi_hash
            $metrics_query = $1;
            $timestamp = $2;
            $value = $3;

            if ($begin_timestamp eq "" || $begin_timestamp gt $timestamp) {
                $begin_timestamp = $timestamp;
            }
            if ($end_timestamp eq "" || $end_timestamp lt $timestamp) {
                $end_timestamp = $timestamp;
            }

            if (exists $kpi_hash{$metrics_query}) {
                # A known query
                push @{$kpi_hash{$metrics_query}{'values'}}, "$timestamp\t$value";
            } else {
                # An unknown query, Create a new one and use the query as the documentation
                $kpi_hash{$metrics_query}{'description'} = $metrics_query;
                $kpi_hash{$metrics_query}{'unit'} = "Value";
                push @{$kpi_hash{$metrics_query}{'values'}}, "$timestamp\t$value";
            }
            $parsed_data_cnt++;
        }
    }

    if ($parsed_data_cnt == 0) {
        parse_node_kpi_data();
    }

    # Now handle the creation of graphs
    create_statistics();
}

# -----------------------------------------------------------------------------
sub handle_kpi_file {
    my $description;
    my @kpi_definitions;
    my $kpi_ref;
    my $json_ref;
    my $metrics_query;
    my @metrics_query_commands = ();

    if ( ! -f "$kpi_file") {
        print_message("The --kpi-file '$kpi_file' does not exist\n");
        cleanup_and_exit(1);
    }

    print_message("Reading KPI definition file\n");

    $rc = General::File_Operations::read_file(
        {
            "filename"              => $kpi_file,
            "output-ref"            => \@kpi_definitions,
        }
    );
    if ($rc != 0) {
        print_message("Failed to read KPI definition file '$kpi_file'\n");
        cleanup_and_exit(1);
    }

    $json_ref = General::Json_Operations::read_array_return_reference( { "input" => \@kpi_definitions, "hide-error-messages" => 1 } );

    # Check that we have valid data, undef means an error was detected during deconding
    unless ($json_ref) {
        print_message("Unable to decode the JSON data for KPI definitions '$kpi_file'.\n");
        cleanup_and_exit(1);
    }

    if (ref($json_ref) eq "HASH") {
        # HASH Reference
        # Check that the returned data follows the expected data structure
        # i.e. that the data->result->value[] array contains 2 indexes
        # and that the second index [1] contains a valid integer or floating
        # point value.
        # If valid then update the %kpi_hash variable with data.
        if (exists $json_ref->{'kpi-data'} && ref($json_ref->{'kpi-data'}) eq "ARRAY") {
            unless (exists $json_ref->{'kpi-data'}->[0]->{'description'} && exists $json_ref->{'kpi-data'}->[0]->{'metrics-query'}) {
                print_message("Not a valid file format of KPI definition file '$kpi_file'\n");
                cleanup_and_exit(1);
            }

            my $count = 0;
            for $kpi_ref (@{$json_ref->{'kpi-data'}}) {
                if (exists $kpi_ref->{'recording-rule'} && $kpi_ref->{'recording-rule'} ne "") {
                    if ($kpi_ref->{'metrics-query'} eq "") {
                        # New format to use the "recording-rule" instead of the "metrics-query"
                        $kpi_ref->{'metrics-query'} = $kpi_ref->{'recording-rule'};
                    } elsif ($prefer_recording_rule) {
                        $kpi_ref->{'metrics-query'} = $kpi_ref->{'recording-rule'};
                    }
                }
                if (exists $kpi_ref->{'metrics-query'}) {
                    $metrics_query = $kpi_ref->{'metrics-query'};
                    $kpi_hash{$metrics_query}{'description'} = $metrics_query;
                    $kpi_hash{$metrics_query}{'unit'} = "Value";
                    $kpi_hash{$metrics_query}{'values'} = ();

                    # Add command to use to fetch the metrics data from the node
                    if ($time_range ne "") {
                        # We should fetch data from the past, so modify the query string to include
                        # how far back to fetch and with which resolution.
                        push @metrics_query_commands, "metrics-query ($metrics_query)$time_range";
                    } else {
                        # Only need to fetch one snapshot or whatever the query string says
                        push @metrics_query_commands, "metrics-query $metrics_query";
                    }
                } else {
                    print_message("Invalid format in array index $count, attribute 'metrics-query' missing.\n");
                    for my $key (sort keys %{$kpi_ref}) {
                        print_message("  '$key': '$kpi_ref->{$key}'\n");
                    }
                    cleanup_and_exit(1);
                }
                if (exists $kpi_ref->{'description'}) {
                    $description = $kpi_ref->{'description'};
                    $kpi_hash{$metrics_query}{'description'} = $description;
                    if ($description =~ /^.+\[([^\]]+)\]$/) {
                        $kpi_hash{$metrics_query}{'unit'} = $1;
                    }
                }
            }
        }
    } else {
        print_message("Not a valid file format of KPI definition file '$kpi_file'\n");
        cleanup_and_exit(1);
    }

    if ($no_data_files == 0) {
        $kpi_load_file = "$output_directory/kpi_statistics.cli";
    } else {
        $kpi_load_file = sprintf '/tmp/kpi_statistics_%d_%d.cli', time(), $$;
    }
    $rc = General::File_Operations::write_file(
        {
            "filename"              => $kpi_load_file,
            "output-ref"            => \@metrics_query_commands,
        }
    );
    if ($rc != 0) {
        print_message("Failed to create KPI load file '$kpi_load_file'\n");
        cleanup_and_exit(1);
    }
}

# -----------------------------------------------------------------------------
sub handle_log_file {

    if ($no_logfile == 1) {
        $logfile = "";
    } elsif ($logfile eq "" && $no_data_files == 0) {
        $logfile = "$output_directory/kpi_statistics.log";
    }

    # Start logging of output to file
    if ($logfile) {
        $rc = General::Logging::log_enable("$logfile");
        if ($rc != 0) {
            exit 1;
        }
        $logfile = abs_path $logfile;
    }
}

# -----------------------------------------------------------------------------
sub handle_output_directory {
    if ($no_data_files == 1) {
        # Do not create any directories or files
        return;
    }

    $output_directory =~ s/\/$//g;   # Remove trailing /
    if ($output_directory eq ".") {
        $output_directory = "./kpi_statistics_$current_epoch";
    }
    if (-d "$output_directory") {
        # Directory exists
        $output_directory = abs_path $output_directory;
    } else {
        # The directory does not exist, so create it
        # print_message("Output directory '$output_directory' does not exists, trying to create it\n");
        $rc = General::OS_Operations::send_command(
            {
                "commands"       => [ "mkdir -p $output_directory/gnuplot", "mkdir -p $output_directory/graphs" ],
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            $output_directory = abs_path $output_directory;
        } else {
            print_message("Failed to create the output directory\n  '$output_directory/gnuplot'\n  or\n  '$output_directory/graphs'\n" . (join "\n", @result) . "\n");
            exit 1;
        }
    }

    # Create file that contains collected data
    if ($output_file eq "") {
        $output_file = "$output_directory/kpi_statistics.txt";
    }

    unless (open OUTF, ">>$output_file") {
        print_message("Failed to open output file '$output_file' for writing\n");
        cleanup_and_exit(1);
    }
    if ($use_echo_to_append_output_file == 1) {
        # Close the file again since all appending will be done with 'echo' commands
        close(OUTF);
    }
}

# -----------------------------------------------------------------------------
sub parse_node_kpi_data {
    my $command_found = 0;
    my $json_ref;
    my @kpi_data = ();
    my $message;
    my $metrics_query = "";
    my $prompt = "";
    my $separator = "\t";
    my $timestamp;
    my $value;

    # Parse all KPIs and store them into the %kpi_hash
    for (@result) {
        s/[\r\n]//g;
        if ($command_found) {
            push @kpi_data, $_;
            if (/^}\s*$/) {
                # End of KPI data detected, now process the data
                $command_found = 0;
                $json_ref = General::Json_Operations::read_array_return_reference( { "input" => \@kpi_data, "hide-error-messages" => 1 } );

                # Check that we have valid data, undef means an error was detected during deconding
                unless ($json_ref) {
                    print_message("Unable to decode the JSON event data for KPI query '$metrics_query'.\n" . (join "", @kpi_data), ! $verbose);
                    # Ignore this error
                    next;
                }

                if (ref($json_ref) eq "HASH") {
                    # HASH Reference
                    # Check that the returned data follows the expected data structure
                    # i.e. that the data->result->value[] array contains 2 indexes
                    # and that the second index [1] contains a valid integer or floating
                    # point value.
                    # If valid then update the %kpi_hash variable with data.
                    if (exists $json_ref->{'data'} && exists $json_ref->{'data'}->{'result'}) {
                        my $result_data_array_ref = $json_ref->{'data'}->{'result'};
                        next if (scalar @$result_data_array_ref == 0);
                        if (exists $result_data_array_ref->[0]->{'value'}) {
                            # Single value pair:
                            #   "data": {
                            #       "result": [
                            #           {
                            #               "metric": {},
                            #               "value": [
                            #                   "2022-02-21T16:24:37Z",
                            #                   "0"
                            #               ]
                            #           }
                            #       ],
                            #       "resultType": "vector"
                            #   },
                            my @values = @{$result_data_array_ref->[0]->{'value'}};
                            next if (scalar @values != 2);
                            # If we reach here we have a valid value pair where:
                            #   index 0: Timestamp e.g. 2022-01-27T17:40:08Z
                            #   index 1: integer or floating point value, or NaN (Not a Number), e.g. 0 or 124.1 or NaN
                            $timestamp = $values[0];
                            $value = $values[1];

                            if ($begin_timestamp eq "" || $begin_timestamp gt $timestamp) {
                                $begin_timestamp = $timestamp;
                            }
                            if ($end_timestamp eq "" || $end_timestamp lt $timestamp) {
                                $end_timestamp = $timestamp;
                            }

                            if (exists $kpi_hash{$metrics_query}) {
                                # A known query
                                push @{$kpi_hash{$metrics_query}{'values'}}, "$timestamp\t$value";
                            } else {
                                # An unknown query, Create a new one and use the query as the documentation
                                $kpi_hash{$metrics_query}{'description'} = $metrics_query;
                                $kpi_hash{$metrics_query}{'unit'} = "Value";
                                push @{$kpi_hash{$metrics_query}{'values'}}, "$timestamp\t$value";
                            }
                            if ($output_file ne "") {
                                if ($use_echo_to_append_output_file == 1) {
                                    $message = sprintf "%s%s%s%s%s",    $metrics_query,
                                                                        $separator,
                                                                        $timestamp,
                                                                        $separator,
                                                                        $value;
                                    # Append to the output file
                                    `echo "$message" >> $output_file`;
                                } else {
                                    printf OUTF "%s%s%s%s%s\n",  $metrics_query,
                                                                 $separator,
                                                                 $timestamp,
                                                                 $separator,
                                                                 $value;
                                }
                            }
                        } elsif (exists $result_data_array_ref->[0]->{'values'}) {
                            # Multiple value pairs:
                            #   "data": {
                            #       "result": [
                            #           {
                            #               "metric": {},
                            #               "values": [
                            #                   [
                            #                       "2022-02-21T15:21:45Z",
                            #                       "0.6643354306537597"
                            #                   ],
                            #                   [
                            #                       "2022-02-21T15:22:00Z",
                            #                       "0.6412823694124038"
                            #                   ],
                            #                      :
                            #                      :
                            #                   [
                            #                       "2022-02-21T16:21:30Z",
                            #                       "0.6579717314360359"
                            #                   ]
                            #               ]
                            #           }
                            #       ],
                            #       "resultType": "matrix"
                            my @values_array = @{$result_data_array_ref->[0]->{'values'}};
                            for my $values_ref (@values_array) {
                                my @values = @{$values_ref};
                                next if (scalar @values != 2);
                                # If we reach here we have a valid value pair where:
                                #   index 0: Timestamp e.g. 2022-01-27T17:40:08Z
                                #   index 1: integer or floating point value, or NaN (Not a Number), e.g. 0 or 124.1 or NaN
                                $timestamp = $values[0];
                                $value = $values[1];

                                if ($begin_timestamp eq "" || $begin_timestamp gt $timestamp) {
                                    $begin_timestamp = $timestamp;
                                }
                                if ($end_timestamp eq "" || $end_timestamp lt $timestamp) {
                                    $end_timestamp = $timestamp;
                                }

                                if (exists $kpi_hash{$metrics_query}) {
                                    # A known query
                                    push @{$kpi_hash{$metrics_query}{'values'}}, "$timestamp\t$value";
                                } else {
                                    # An unknown query, Create a new one and use the query as the documentation
                                    $kpi_hash{$metrics_query}{'description'} = $metrics_query;
                                    $kpi_hash{$metrics_query}{'unit'} = "Value";
                                    push @{$kpi_hash{$metrics_query}{'values'}}, "$timestamp\t$value";
                                }
                                if ($output_file ne "") {
                                    if ($use_echo_to_append_output_file == 1) {
                                        $message = sprintf "%s%s%s%s%s",    $metrics_query,
                                                                            $separator,
                                                                            $timestamp,
                                                                            $separator,
                                                                            $value;
                                        # Append to the output file
                                        `echo "$message" >> $output_file`;
                                    } else {
                                        printf OUTF "%s%s%s%s%s\n",  $metrics_query,
                                                                    $separator,
                                                                    $timestamp,
                                                                    $separator,
                                                                    $value;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    # Incorrect format, ignore it
                    next;
                }
            }
        } elsif (/^(.+)metrics-query\s+(.+)$/) {
            # For example:
            # avg(scp_load)
            #   or
            # (avg(scp_load))[60m:15s]
            $command_found = 1;
            $prompt = $1;
            $metrics_query = $2;
            # Remove strange CTRL-H and other CTRL-x characters and spaces from the query
            # since it causes strange file names and it cannot find the description.
            $metrics_query =~ s/[\cA-\cZ\s]//g;
            if ($metrics_query =~ /^\((.+)\)\[\d+.:\d+.\]$/) {
                # For example:
                # (avg(scp_load))[60m:15s]
                $metrics_query = $1;
            } elsif ($metrics_query =~ /^\((.+)\)\[\d+.:\d+.\]\@\d+$/) {
                # For example:
                # (avg(scp_load))[60m:15s]@1668619480
                $metrics_query = $1;
            }
            @kpi_data = ();
        }
    }
}

# -----------------------------------------------------------------------------
sub print_message {
    my $text = shift;
    my $only_log = 0;

    if (scalar @_ > 0) {
        $only_log = shift;
    }

    if ($logfile ne "") {
        General::Logging::log_write($text);
    }

    if ($only_log == 0) {
        print $text if ($no_tty == 0);
    }
}

# -----------------------------------------------------------------------------
sub read_kpi_data_from_node {
    my $command = dirname(dirname(dirname(abs_path $0))) . "/expect/bin/send_command_to_ssh.exp";
    my $command_end_time;
    my $command_start_time;
    my $counter = 0;
    my $message;

    unless (-f "$command") {
        print_message("Unable to find the '$command' script.\n");
        cleanup_and_exit(1);
    }

    # Check if namespace exists
    if ( $namespace ne "" ) {
        my $namespace_check = ADP::Kubernetes_Operations::namespace_exists($namespace);
        if ( $namespace_check != 1 ) {
            # The namespace does not exist (=0) or error fetching the namespace data (=2)
            print_message("Namespace $namespace does not exist or problem checking the namespace\n");
            cleanup_and_exit(1);
        }
    } else {
        print_message("No namespace provided\n");
        cleanup_and_exit(1);
    }

    # Fetch CMYP IP address
    unless ($cmyp_ip) {
        $cmyp_ip = ADP::Kubernetes_Operations::get_cmyp_ip($namespace);
    }
    unless ($cmyp_ip) {
        print_message("Failed to fetch CMYP IP_address.\n");
        cleanup_and_exit(1);
    }

    remove_cmyp_ip_from_ssh_knownhosts_file();

    if ($repeat_time > 0) {
        # This means we look into the future and need to wait the collection interval
        # before we fetch the first values.
        print_message("Sleeping $resolution seconds before fetching first set of data from the node\n");
        sleep($resolution);
    }

    print_message("Fetching KPI data from the node\n");

    # Collect the KPI's in a loop
    $counter = 0;
    while (1) {
        $counter++;

        # Reset the counter
        $ctrl_c_pressed = 0;

        $command_start_time = time();

        $return_code = get_kpi_from_node();

        $command_end_time = time();

        # Check if user interrupted the KPI collection from the node
        if ($ctrl_c_pressed > 0 && $no_tty == 0) {
            print_message("\nFetching of KPI counters was interrupted by CTRL-C\n");
            last;
        }
        if ($abort_execution > 0) {
            print_message("\nAbort execution ordered by SIGABRT\n");
            last;
        }
        # Reset the counter
        $ctrl_c_pressed = 0;

        # We ignore any potential error here because it will be properly handled below
        # if it is time to cleanup_and_exit()this loop in which case the $return_code variable
        # contains the failure code which is returned to the user.

        # Check if we should cleanup_and_exit()the loop
        if ($repeat_count > 0) {
            if ($counter >= $repeat_count) {
                last;
            }
            $message = sprintf "(%d repetitions left of data collection)", ($repeat_count - $counter);
        } elsif ($repeat_time > 0) {
            if ( $counter < ($repeat_time / $resolution) ) {
                # We have not yet collected enough samples, to wait a while and collect some more
            } elsif ($command_end_time >= $stop_time) {
                # We have collected enough samples
                last;
            }
            $message = sprintf "(%d seconds left of data collection)", ($stop_time - $command_end_time);
        } else {
            last;
        }

        $sleep_time = $resolution - ($command_end_time - $command_start_time);
        if ($sleep_time > 0) {
            #print_message("\nSleeping $sleep_time seconds\n");
            print_message("Sleeping $sleep_time seconds before fetching new data from the node $message                                        \r");
            sleep($sleep_time);

            # Check if user interrupted the sleep
            if ($ctrl_c_pressed > 0 && $no_tty == 0) {
                print_message("\nSleep was interrupted by CTRL-C\n");
                last;
            }
        }
        if ($abort_execution > 0) {
            print_message("\nAbort execution ordered by SIGABRT\n");
            last;
        }
        # Reset the counter
        $ctrl_c_pressed = 0;
    }
    # Clear the previously written message
    print_message("                                                                                                                    \r");

    # Now handle the creation of graphs
    create_statistics();
}

# -----------------------------------------------------------------------------
sub remove_cmyp_ip_from_ssh_knownhosts_file {
    # Remove cached SSH key, ignore the result
    General::OS_Operations::send_command(
        {
            "command"       => "ssh-keygen -R $cmyp_ip",
            "hide-output"   => 1,
        }
    );
}

# -----------------------------------------------------------------------------
sub show_help {
    print <<EOF;

Description:
============

This script collects KPI data from a specific Kubernetes deployment (namespace)
and writes easy to parse data about the different KPI counters to a file and then
creates PNG and ASCII graphs of the collected data.

The script can also read data from a file instead of fetching it from the
Kubernetes deployment and then create PNG and ASCII graphs of the data in the file.


Syntax:
=======

$0 <MANDATORY> [<OPTIONAL>]

<MANDATORY> is one of the following parameters:

  -i <filename>         | --input-file <filename>
    or
  -n <string>           | --namespace <string>

<OPTIONAL> are one or more of the following parameters:

  -c <filename>         | --kubeconfig <filename>
  -d <integer|string>   | --date-offset <integer|string>
  -f <value>            | --future <value>
  -h                    | --help
  -k <filename>         | --kpi-file <filename>
  -l <filename>         | --log-file <filename>
  -o <dirname>          | --output-directory <dirname>
  -p <value>            | --past <value>
  -r <integer>          | --resolution <integer>
  -v                    | --verbose
                          --cmyp-new-password <string>
                          --cmyp-password <string>
                          --cmyp-user <string>
                          --image-size-ascii <string>
                          --image-size-png <string>
                          --no-ascii-graph
                          --no-data-files
                          --no-graphs
                          --no-logfile
                          --no-png-graph
                          --no-tty
                          --output-file <filename>
                          --prefer-metrics-query
                          --prefer-recording-rule

where:

  -c <filename>
  --kubeconfig <filename>
  -----------------------
  If specified then it will use this file as the config file for helm and
  kubectl commands.
  If not specified then it will use the default file which is normally the
  file ~/.kube/config.


  --d <integer|string>
  --date-offset <integer|string>
  ------------------------------
  Specify this parameter if the KPI data should be fetched somewhere in the
  past instead of starting from current time into the past.
  The value can be either specified as an integer value indicating the epoch
  time stamp where KPI collection should be started and then looking into
  the past as specified by the --past parameter.
  It can also be specified as string which should then be a valid date and time
  format that the 'date' command can handle and it will use this as the start
  value and then looking into the past as specified by the --past parameter.

  Both these examples will generate the same output i.e. that it will fetch
  KPI data for 1m into the past with a resolution of 15 seconds starting with
  the date ""Wed Nov 16 09:54:46 CET 2022", i.e. it will fetch 4 values:

  --past 1m --resolution 15 --date-offset "Wed Nov 16 09:54:46 CET 2022"
  --past 1m --resolution 15 --date-offset 1668588886

  To convert a human readable date into an epoch time you can e.g. use the
  following command:

  date -d"Wed Nov 16 09:54:46 CET 2022" +%s
  date -d"2022-11-16T09:54:46CET" +%s


  -f <value>
  --future <value>
  ----------------
  If specified then KPI values are collected at interval specified by the
  --resolution parameter.
  If the <value> is an integer value then this is how many sets of KPI values are
  fetched from the node with an interval of --resolution seconds.
  If the <value> is an integer value followed by the character 's', 'm', 'h', 'd'
  or 'w' then this is how long time the collection is done specified in seconds,
  minutes, hours, days or weeks. Between each collection there is a delay of
  --resolution seconds.

  If neither --future or --past is specified then only a single snapshot if fetched
  from the node or from the history.


  -h
  --help
  ------
  Shows this help information.


  -i <filename>
  --input-file <filename>
  -----------------------
  The input file to use for creating the graphs, this file should be in the same
  format as the output produced by this script (see parameter --output-file) and
  should be containing one or more lines where the first column is the
  metrics-query string, the second column the timestamp in YYYY-MM-DDTHH:MM:SSZ
  format and the third column the actual value, all columns separated by a tab
  character.
  For example:
  avg(bsf_load)\t2022-02-04T14:14:44Z\t41.225735393387076

  This parameter is mandatory unless the --namespace parameter is specified.
  If both parameters are specified then --input-file will be used.


  -k <filename>
  --kpi-file <filename>
  ---------------------
  If specified then this file should contain KPI definitions which will be used
  when parsing KPI data to translate the query strings into an easier to understand
  KPI string which will also be used for the created output files.

  If not specified then the template file containing all defined KPI's (which are
  known at the time of last update of that file) which is stored in the following
  relative directory from this script will be used:

    ../../templates/kpi/all_sc_kpi.json

  This file should be in JSON format and look something like this:

    {
        "kpi-data": [
            {
                "description": "SCP Ingress Requests [MPS]",
                "metrics-query": "sum(rate(envoy_downstream_rq_total{nf_type='scp'}[10s]))"
            },
            {
                "description": "SCP Egress Requests [MPS]",
                "metrics-query": "sum(rate(envoy_upstream_rq_total{nf_type='scp'}[10s]))"
            },
            {
                "description": "SCP Ingress Request Success Rate [%]",
                "metrics-query": "(sum(rate(envoy_downstream_rq_xx{group='ingress',envoy_response_code_class='2',nf_type='scp'}[10s]))/sum(rate(envoy_downstream_rq_total{nf_type='scp'}[10s])))*100"
            },
            :
            :
            {
                "description": "SEPP Load [%]",
                "metrics-query": "avg(sepp_load)"
            }
        ]
    }

  The "kpi-data", ""description" and "metrics-query" attributes must exist and should
  contain the following data:
  "kpi-data":
      Is an array definition.

  "description":
      Should contain a short description of what the KPI query is for.
      Anything in the "[ ]" part is describing what unit the data is in.

  "metrics-query":
      Is the query to be used when fetching KPI data from CMYP CLI.


  -l <filename>
  --log-file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  If not specified then a log file is created the /tmp/ directory with a file
  name of:
    kpi_collect_XXXX.log
  where the XXXX is the epoch timestamp (seconds since Jan 1 1970).
  See also --no_logfile if no log file should be created.


  -n <string>
  --namespace <string>
  -----------------------
  The namespace of the SC deployment to check.
  This parameter is mandatory unless the --input-file parameter is specified.
  If both parameters are specified then --input-file will be used.


  -o <dirname>
  --output-directory <dirname>
  ----------------------------
  If specified then the output files (log, raw data and graphs) will be written to
  this directory instead of the default directory called kpi_statistics_XXXX in the
  current directory where XXXX is the current epoch time (seconds since Jan 1 1970).


  -p <value>
  --past <value>
  --------------
  If specified then KPI values are collected from the past with an interval
  specified by the --resolution parameter.
  If the <value> is an integer value then this is how many sets of KPI values are
  fetched from the past history with an interval of --resolution seconds.
  If the <value> is an integer value followed by the character 's', 'm', 'h', 'd'
  or 'w' then this is how far back in time the collection is done specified in seconds,
  minutes, hours, days or weeks. Between each collection there is a delay of
  --resolution seconds.

  If neither --future or --past is specified then only a single snapshot if fetched
  from the node or from the history.


  -r <integer>
  --resolution <integer>
  ----------------------
  Specifies which resolution in seconds to use between repeated KPI checks
  or between kpi data fetched from the past.
  The default value is 15 seconds if not specified.

  The actual delay time that will be used between repeated KPI checks
  depends on how long time it took to fetch and decode the previous KPI's.
  So if e.g. the fetching of the KPI's took 5 seconds then the delay will
  only be 10 seconds. This is done to ensure that the delay between two
  KPI fetch operations is about 15 seconds.

  This parameter only makes sense if also parameters --past or --future
  is also specified.


  -v
  --verbose
  ---------
  Specify this if verbose output showing more details should be written to
  STDOUT.
  By default only the minimum amount of printouts are shown on STDOUT.


  --cmyp-ip <string>
  ------------------
  The IP-address or FQDN to use when logging into the CMYP CLI, if not
  specified then the value automatically determined.
  NOTE:
  This parameter might have to be specified when fetching KPIs from KaaS
  clusters where no load balancer is used, in which case it should be a
  worker IP address (kubectl get nodes -o wide --no-headers | grep worker | head -1).


  --cmyp-new-password <string>
  ----------------------------
  If specified then the script will be able to automatically handle request
  to change the password and it will use the specified password as the new
  value.
  This parameter only make sense when you run a test case where the CMYP
  user account expire due to e.g. a time shift test that moves the time into
  the future.


  --cmyp-password <string>
  ------------------------
  The password to use when logging into the CMYP CLI, if not specified the value
  'rootroot' is used.
  See also --cmyp-user.


  --cmyp-port <integer>
  ---------------------
  The port to use when logging into the CMYP CLI, if not specified then the value
  automatically determined.
  NOTE:
  This parameter might have to be specified when fetching KPIs from KaaS
  clusters where no load balancer is used.


  --cmyp-user <string>
  --------------------
  The user to use when logging into the CMYP CLI, if not specified the value
  'expert' is used.
  See also --cmyp-password.


  --image-size-ascii <string>
  ---------------------------
  Specify this parameter if the generated ASCII graph file should be of
  a different size than the default of "300,75".
  The format of <string> is: <columns>,<lines>


  --image-size-png <string>
  -------------------------
  Specify this parameter if the generated PNG graph file should be of
  a different size than the default of "1920,1080".
  The format of <string> is: <pixels width>,<pixels height>


  --no-ascii-graph
  ----------------
  Specify this parameter if you don't want to generate ASCII graphs.
  By default will ASCII graphs be generated unless --no-data-files or
  --no-graphs.


  --no-data-files
  ---------------
  Specify this parameter if you don't want to create any files at all
  except for if --log-file is given.
  By using this parameter also no gnuplot files will be created and the
  only output from the script is just the "Statistics Summary" printout.


  --no-graphs
  -----------
  Specify this parameter if no graphs should be generated but the tool.
  Instead only the files with gnuplot instructions will be created so the
  user can easilly create the graphs at a different time.
  This parameter might be useful when the node where the script is running
  does not have the 'gnuplot' binary.
  By default if this parameter is not specified then PNG graph files will
  be generated by the script.


  --no-logfile
  ------------
  Specify this parameter if no log file with detailed information should be
  created and information is ONLY printed to STDOUT and STDERR, by default a
  log file is created either in the file specified with -f/--log-file or in the
  /tmp/ directory with a file name of:
    kpi_collect_XXXX.log
  where the XXXX is the epoch timestamp (seconds since Jan 1 1970).


  --no-png-graph
  --------------
  Specify this parameter if you don't want to generate PNG graphs.
  By default will PNG graphs be generated unless --no-data-files or
  --no-graphs.


  --no-tty
  --------
  Specify this parameter if the script is running in the background and you
  don't want to have any printouts to STDOUT.
  Using this parameter will also disable the CTRL-C mechanism so it's not possible
  to interrupt the sleep, and the only way to abort the program ahead of time
  will be to send the SIGABRT signal.

    kill -s SIGABRT <pid>


  --output-file <filename>
  ------------------------
  If specified then the output, which is the raw data collected by this script and
  that is used for the graph generation, will be written to this file instead of to
  a file called kpi_statistics.txt in the output directory (--output-directory).
  The format of this file should be containing one or more lines where the first
  column is the metrics-query string, the second column the timestamp in
  YYYY-MM-DDTHH:MM:SSZ format and the third column the actual value, all columns
  separated by a tab character.
  For example:
  avg(bsf_load)\t2022-02-04T14:14:44Z\t41.225735393387076


  --prefer-metrics-query
  ----------------------
  If specified and the KPI definition file given by the --kpi-file parameter
  contains the "metrics-query" attribute then that will be used instead of the
  "recording-rule" attribute when fetching data from the node.
  If the "metrics-query" attribute does not exist then the script exit with an error.
  The reason for this parameter is to support backwards compatibility for older
  releases without recording rules since starting with SC releases 1.15.0 all
  KPI will by default use "recording-rule" instead of "metrics-query".
  For older releases before SC 1.8.0 no recording rules existed so they can
  only use the metrics-query strings.


  --prefer-recording-rule
  -----------------------
  This is the default behavior so it does not need to be specified, but exists
  for backwards compatibility.
  If specified and the KPI definition file given by the --kpi-file parameter
  contains the "recording-rule" attribute then that will be used instead of the
  "metrics-query" attribute when fetching data from the node.
  The reason for this parameter is in SC releases starting with SC 1.8.0 all
  KPI should be fetched using recording rules because it's more efficient.
  But for older releases before SC 1.8.0 no recording rules existed so they can
  only use the metrics-query strings.


Examples:
=========

  - Fetch a snapshot of current KPI statistics from the node using a special user:
    $0 -n eiffelesc --cmyp-user expert1 --cmyp-password rootroot

  - Fetch KPI counters from the node for 10 minutes using default user:
    $0 -n eiffelesc --future 10m

  - Fetch 20 sets of KPI counters from the node with a 30 second resolution:
    $0 -n eiffelesc --future 20 --resolution 30

  - Fetch KPI counters from 1 hours into the past:
    $0 -n eiffelesc --past 1h

  - Fetch KPI counters starting on a specific date for 1 hours into the past
    starting from this date:
    $0 -n eiffelesc --past 1h --date-offset 2022-11-14T12:00:00Z


Return code:
============

   0:  Successful, the execution was successful.
   >0: Unsuccessful, some failure was reported.

EOF
}

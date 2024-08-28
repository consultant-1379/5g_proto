#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.22
#  Date     : 2022-03-04 15:38:09
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2018-2020, 2022
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

# Colors: black  red  green  yellow  blue  magenta  cyan  white
#         bright_black  bright_red  bright_green  bright_yellow
#         bright_blue  bright_magenta  bright_cyan  bright_white
#         on_black  on_red  on_green  on yellow
#         on_blue  on_magenta  on_cyan  on_white
my $color_cyan = color("cyan on_black");
my $color_green = color("green on_black");
my $color_red = color("red on_black");
my $color_reset = color("reset");
my $color_yellow = color("yellow on_black");

sub show_help {
    print <<EOF;

Description:
============

This script is used for parsing log files under /var/log/ directory and
it will sort the different files in the proper timestamp order and apply
color highlighting of certain words.


Syntax:
=======

$0 [<OPTIONAL>] <file name> [<file name> ...]

<OPTIONAL> are one or more of the following parameters:

  -a                   --clear-default-color-patterns
  -c <pattern>       | --match-cyan-pattern <pattern>
  -e <timestamp>     | --end-timestamp <timestamp>
  -f <filename>      | --filter-file <filename>
  -g <pattern>       | --match-green-pattern <pattern>
  -h                 | --help
  -i <pattern>       | --include-pattern <pattern>
  -n                 | --no-sort
  -r <pattern>       | --match-red-pattern <pattern>
  -s <timestamp>     | --start-timestamp <timestamp>
  -t                 | --use-tempfile
  -x <pattern>       | --exclude-pattern <pattern>
  -y <pattern>       | --match-yellow-pattern <pattern>

where:

  -a
  --clear-default-color-patterns
  ------------------------------
  When specified then all default color patterns will be cleared and only
  patterns given by the user in --match-cyan-pattern, --match-green-patterns,
  --match-red-pattern and --match-yellow-pattern will be colored.
  The order of the parameters matter when using this parameter, which should be
  entered before any of the other --match-... parameters, if this is not
  followed then this parameter will clear any already entered patterns by the
  user.


  -c <pattern>
  --match-cyan-pattern <pattern>
  ------------------------------
  When specified and a valid Perl regular expression pattern is given then
  matching words will be highlighted in color $color_cyan"CYAN ON BLACK"$color_reset.
  The default pattern used if not given is :

  '\\b(NO|NOTIFICATION|NOTICE|IN|INFORMATION)\\b'
  '(DAFT|daft_deployment|daft_cleanup).+'

  This parameter can be specified multiple times in which case all given
  patterns will be matched and given the proper color.
  NOTE: Patterns are case sensitive.


  -e <timestamp>
  --end-timestamp <timestamp>
  ---------------------------
  When specified, only log entries that are same or older than the specified
  time stamp will be included in the output.
  Can also be used together with -s/--start-timestamp to limit output even
  further.

  <timestamp> should be in one of the following formats:
    YYYY-MM-DDTHH:MM:SS  or  YYYYMMDDTHHMMSS
    YYYY-MM-DD HH:MM:SS  or  YYYYMMDD HHMMS
    YYYY-MM-DDHH:MM:SS   or  YYYYMMDDHHMMSS
    HH:MM:SS             or  HHMMSS
    HH:MM                or  HHMM


  -f <filename>
  --filter-file <filename>
  ------------------------
  When specified, it will load filters from a file instead of specifying them
  with the parameters -c/--match-cyan-pattern, -g/--match-green-patterns,
  -i/--include-patterns, -r/--match-red-pattern, -x/--exclude-pattern or
  -y/--match-yellow-pattern.

  The contents of the file should look like this:
  c:<pattern>
   or
  match-cyan-pattern:<pattern>

  g:<pattern>
   or
  match-green-pattern:<pattern>

  i:<pattern>
   or
  include-pattern:<pattern>

  r:<pattern>
   or
  match-red-pattern:<pattern>

  x:<pattern>
   or
  exclude-pattern:<pattern>

  y:<pattern>
   or
  match-yellow-pattern:<pattern>


  --match-green-pattern <pattern>
  -------------------------------
  When specified and a valid Perl regular expression pattern is given then
  matching words will be highlighted in color $color_green"GREEN ON BLACK"$color_reset.
  The default pattern used if not given is :

  'CAMP: .+'
  'PROC: .+'
  'STEP: .+'

  This parameter can be specified multiple times in which case all given
  patterns will be matched and given the proper color.
  NOTE: Patterns are case sensitive.


  -h
  --help
  ------
  Shows this help information.


  -i <pattern>
  --include-pattern <pattern>
  ---------------------------
  When specified and a valid Perl regular expression pattern is given then
  only matching log entries will be included in the displayed output.
  This parameter has no default value, i.e. all lines will be displayed.

  This parameter can be specified multiple times in which case all given
  patterns will be matched and included in  the displayed information.
  NOTE: Patterns are case sensitive.

  Exclude patterns has higher priority than include patterns.


  -n
  --no-sort
  ---------
  When specified then no sorting of the printouts will take place and printouts
  will be printed in the way they are received by the script and only highligthing
  and filtering will take place.
  This might be useful if you want to apply color and filtering to printouts
  coming from a pipe e.g. tail -f /var/log/messages | $0


  -r <pattern>
  --match-red-pattern <pattern>
  -----------------------------
  When specified and a valid Perl regular expression pattern is given then
  matching words will be highlighted in color $color_red"RED ON BLACK"$color_reset.
  The default pattern used if not given is :

  '\b(ER|ERR|ERROR|FAIL|FAILED|FAILURE)\b'

  This parameter can be specified multiple times in which case all given
  patterns will be matched and given the proper color.
  NOTE: Patterns are case sensitive.


  -s <timestamp>
  --start-timestamp <timestamp>
  -----------------------------
  When specified, only log entries that are same or newer than the specified
  time stamp will be included in the output.
  Can also be used together with -e/--end-timestamp to limit output even
  further.

  <timestamp> should be in one of the following formats:
    YYYY-MM-DDTHH:MM:SS  or  YYYYMMDDTHHMMSS
    YYYY-MM-DD HH:MM:SS  or  YYYYMMDD HHMMS
    YYYY-MM-DDHH:MM:SS   or  YYYYMMDDHHMMSS
    HH:MM:SS             or  HHMMSS
    HH:MM                or  HHMM


  -t
  --use-tempfile
  --------------
  If specified, then the external UNIX 'sort' program will be used to sort
  the output data instead of using Perl's internal sort function.
  It will use less RAM since it doesn't keep the data in a hash but written
  to temporary files.
  The difference on a large node with 18 PL's is e.g. 684 MB vs. 26 MB when
  using -t.
  The temporary files used will be written to the current directory and
  will have the names:
    tmp_unsorted_file.tmp
    tmp_sorted_file.tmp


  -x <pattern>
  --exclude-pattern <pattern>
  ---------------------------
  When specified and a valid Perl regular expression pattern is given then
  matching log entries will be excluded from the displayed output.
  This parameter has no default value, i.e. all lines will be displayed.

  This parameter can be specified multiple times in which case all given
  patterns will be matched and excluded from the displayed information.
  NOTE: Patterns are case sensitive.

  Exclude patterns has higher priority than include patterns.


  -y <pattern>
  --match-yellow-pattern <pattern>
  ------------------------------
  When specified and a valid Perl regular expression pattern is given then
  matching words will be highlighted in color $color_yellow"YELLOW ON BLACK"$color_reset.
  The default pattern used if not given is :

  '\b(WA|WARNING)\b'

  This parameter can be specified multiple times in which case all given
  patterns will be matched and given the proper color.
  NOTE: Patterns are case sensitive.


Output:
=======

The script writes the output to STDOUT.


Examples:
=========

  Example 1 Exclude all entries containing "DHCP" in the text
  -----------------------------------------------------------
  $0 \\
      -x DHCP \\
      /var/log/*/messages


  Example 2 Highlight Upgrade related messages with CYAN color
  ------------------------------------------------------------
  $0 \\
      -c ecimswm \\
      /var/log/*/messages


  Example 3 Include the most important Upgrade related messages
  -------------------------------------------------------------
  $0 \\
      -i '(ecimswm|DAFT|osafsmfd|CMW:)' \\
      /var/log/??-*/messages


  Example 4 Highlight all data written to /var/log/messages for
            all blades on the Cluster.
            Interrupt the script with CTRL-C.
  -------------------------------------------------------------
  tail -f /var/log/??-*/messages | $0 -n


  Example 5 Highlight all data written to /var/log/messages for
            all blades on the Cluster.
            Exclude empty lines  and lines starting with ==> and
            certain EVIP related messages.
            Interrupt the script with CTRL-C.
  -------------------------------------------------------------
  tail -f /var/log/??-*/messages | $0 -n -x '^\\s*\$' -x '^==> ' -x 'evip.+service'


  Example 6 Highlight all data written to /var/log/messages for
            all blades on the Cluster.
            Only include the most important Upgrade related
            messages.
  -------------------------------------------------------------
  tail -f /var/log/??-*/messages | $0 -n -i ' (SC|PL)-\\d+ (rpm|osafamfd|osafamfnd|osafsmfd|osafsmfnd|CMW|cmw|cmwea|SMF|DAFT|ecimswm|daft_cleanup|daft_deployment)[ \\[:]' -x 'cmw-utility immlist -a uniquePrompt'

EOF
}

use Time::HiRes;
use Time::Local;
use File::Basename;
use Term::ANSIColor;

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $debug = 0;
my ($current_sec, $current_min, $current_hour, $current_day, $current_month, $current_year) = localtime;
$current_year += 1900;

my $append_unknown_to_previous_timestamp = 1;   # Change to 0 to get old behaviour where all unknown timestamp are saved to time stamp 0
my $filename;
my $filepath;
my @files = ();
my @files_with_problem = ();
my $filter_file = "";
my $found = 0;
my @include = ();
my $include_timestamp_start = 0;
my $include_timestamp_stop = 0;
my $line;
my %log;
my $match;
my @match_cyan = (
    '\b(NO|NOTIFICATION|NOTICE|IN|INFORMATION)\b',
    '(DAFT|daft_deployment|daft_cleanup).+'
);
my @match_green = (
    # Campaign related messages from osafsmfd:
    'CAMP: .+',
    'PROC: .+',
    'STEP: .+'
);
my @match_red = ('\b(ER|ERR|ERROR|FAIL|FAILED|FAILURE)\b');
my @match_yellow = ('\b(WA|WARNING)\b');
my $max_filename_length = 0;
# Conerting a month string or number into Time::Local format
my %mon_to_num = (
    "Jan" => 0,
    "Feb" => 1,
    "Mar" => 2,
    "Apr" => 3,
    "May" => 4,
    "Jun" => 5,
    "Jul" => 6,
    "Aug" => 7,
    "Sep" => 8,
    "Oct" => 9,
    "Nov" => 10,
    "Dec" => 11,
    "1"   => 0,
    "2"   => 1,
    "3"   => 2,
    "4"   => 3,
    "5"   => 4,
    "6"   => 5,
    "7"   => 6,
    "8"   => 7,
    "9"   => 8,
    "01"  => 0,
    "02"  => 1,
    "03"  => 2,
    "04"  => 3,
    "05"  => 4,
    "06"  => 5,
    "07"  => 6,
    "08"  => 7,
    "09"  => 8,
    "10"  => 9,
    "11"  => 10,
    "12"  => 11,
);
my $no_sort = 0;
my @exclude = ('\bMDNS\b');
my $show_help = 0;
my $timestamp = "0000000000.000000";;
my $use_temp_file_for_sorting = 0;

# *****************************
# *                           *
# * Validate Input Parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|clear-default-color-patterns"    => sub { @match_cyan = (); @match_green = (); @match_red = (); @match_yellow = (); },
    "c|match-cyan-pattern=s"            => \@match_cyan,
    "d|debug"                           => \$debug,
    "e|end-timestamp=s"                 => \&parse_timestamp_parameter,
    "f|filter-file=s"                   => \$filter_file,
    "g|match-green-pattern=s"           => \@match_green,
    "h|help"                            => \$show_help,
    "i|include-pattern=s"               => \@include,
    "n|no-sort"                         => \$no_sort,
    "r|match-red-pattern=s"             => \@match_red,
    "s|start-timestamp=s"               => \&parse_timestamp_parameter,
    "t|use-tempfile"                    => \$use_temp_file_for_sorting,
    "x|exclude-pattern=s"               => \@exclude,
    "y|match-yellow-pattern=s"          => \@match_yellow,
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

# Parse filters from a file if specified
if ($filter_file ne "" and -f "$filter_file") {
    if (open(INF, $filter_file)) {
        for (<INF>) {
            s/[\r\n]//g;
            if (/^(c|match-cyan-pattern):(.+)/) {
                push @match_cyan, $2;
            } elsif (/^(g|match-green-pattern):(.+)/) {
                push @match_green, $2;
            } elsif (/^(i|include-pattern):(.+)/) {
                push @include, $2;
            } elsif (/^(r|match-red-pattern):(.+)/) {
                push @match_red, $2;
            } elsif (/^(x|exclude-pattern):(.+)/) {
                push @exclude, $2;
            } elsif (/^(y|match-yellow-pattern):(.+)/) {
                push @match_yellow, $2;
            } elsif (/^\s*#/) {
                # Ignore comment lines
                next;
            } elsif (/^\s*$/) {
                # Ignore empty lines
                next;
            } else {
                print "Incorrect filter line: $_\n";
            }
        }
        close(INF);
    } else {
        print "Unable to open the filter file: $filter_file\n";
        exit 1;
    }
}

if ($no_sort) {
    # No sorting of input will take place, instead it will just parse everything coming on STDIN

    # For each log entry with same time stamp add color and print line
    while (<>) {
        if (@include) {
            $found = 0;
            for $match (@include) {
                if (/$match/) {
                    $found = 1;
                    last;
                }
            }
            if ($found == 0) {
                # Not wanted line, take next line
                next;
            }
        }
        if (@exclude) {
            $found = 0;
            for $match (@exclude) {
                if (/$match/) {
                    $found = 1;
                    last;
                }
            }
            if ($found == 1) {
                # Not wanted line, take next line
                next;
            }
        }

        # Add colors for special strings
        for $match (@match_cyan) {
            s/($match)/$color_cyan$1$color_reset/g;
        }
        for $match (@match_green) {
            s/($match)/$color_green$1$color_reset/g;
        }
        for $match (@match_red) {
            s/($match)/$color_red$1$color_reset/g;
        }
        for $match (@match_yellow) {
            s/($match)/$color_yellow$1$color_reset/g;
        }

        # Print the line
        print $_;
    }
} else {
    # Sorting will take place

    # Fetch file names from the command line
    @files = @ARGV;

    if (scalar @files > 0 && $use_temp_file_for_sorting == 1) {
        unless (open TEMPFILE, ">eustone_unsorted_file.tmp") {
            print "Unable to open temporary unsorted file for writing\n";
            $use_temp_file_for_sorting = 0;
        }
    }

    for (@files) {
        $filepath = $_;
        $filename = basename($filepath);
        $max_filename_length = length($filename) if (length($filename) > $max_filename_length);
        printf STDERR "Processing File: %s\n", $filepath;
        if (open INF, "$filepath") {
            # Every time we start a new file reset the time stamp in case the first lines in the file contains no timestamp
            $timestamp = "0000000000.000000";

            while (<INF>) {
                $line = $_;

                # Look for time stamp on line
                if (/^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+(\d+)\s+(\d{2}):(\d{2}):(\d{2})\s+(\d{4})\s+.+/) {
                    # Format 1:
                    # Jan  1 01:02:03 2018 some log text....
                    print STDERR "DBG 1:$line" if $debug;
                    $timestamp = convert_to_timestamp($6,$1,$2,$3,$4,$5,0);

                } elsif (/^(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+(\d+)\s+(\d{2}):(\d{2}):(\d{2})\.(\d+)\s+(\d{4})\s+.+/) {
                    # Format 2:
                    # Sun Jan  1 01:02:03.123456 2018 some log text....
                    print STDERR "DBG 2:$line" if $debug;
                    $timestamp = convert_to_timestamp($8,$2,$3,$4,$5,$6,$7);

                } elsif (/^(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+(\d+)\s+(\d{2}):(\d{2}):(\d{2})\s+(\d{4})\s+.+/) {
                    # Format 3:
                    # Sun Jan  1 01:02:03 2018 some log text....
                    print STDERR "DBG 3:$line" if $debug;
                    $timestamp = convert_to_timestamp($7,$2,$3,$4,$5,$6,0);

                } elsif (/^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+(\d+)\s+(\d{2}):(\d{2}):(\d{2})\s+.+/) {
                    # Format 4:
                    # Jan  1 01:02:03 some log text....
                    print STDERR "DBG 4:$line" if $debug;
                    $timestamp = convert_to_timestamp(0,$1,$2,$3,$4,$5,0);

                } elsif (/^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+(\d+)\s+(\d{2}):(\d{2}):(\d{2})\.(\d+)\s+.+/) {
                    # Format 5:
                    # Jan  1 01:02:03.936 some log text....
                    print STDERR "DBG 5:$line" if $debug;
                    $timestamp = convert_to_timestamp(0,$1,$2,$3,$4,$5,$6);

                } elsif (/^(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+(\d+)\s+(\d{2}):(\d{2}):(\d{2})\.(\d+)\s+.+/) {
                    # Format 6:
                    # Sun Jan  1 01:02:03.123456 some log text....
                    print STDERR "DBG 6:$line" if $debug;
                    $timestamp = convert_to_timestamp(0,$2,$3,$4,$5,$6,$7);

                } elsif (/^(\d{4})[-\/](\d{2})[-\/](\d{2}) (\d{2}):(\d{2}):(\d{2})\s+.+/) {
                    # Format 7:
                    # 2018-03-10 18:48:39 some log text....
                    # 2018/03/10 18:48:39 some log text....
                    print STDERR "DBG 7:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,0);

                } elsif (/^\[(\d{2})\.(\d{2})\.(\d{4}) (\d{2}):(\d{2}):(\d{2})\]\s+.+/) {
                    # Format 8:
                    # [21.05.2016 18:48:39] some log text....
                    print STDERR "DBG 8:$line" if $debug;
                    $timestamp = convert_to_timestamp($3,$2,$1,$4,$5,$6,0);

                } elsif (/^\[(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+(\d+)\s+(\d{2}):(\d{2}):(\d{2})\.(\d+)\]\s+.+/) {
                    # Format 9:
                    # [Sun Jan  1 01:02:03.123456] some log text....
                    print STDERR "DBG 9:$line" if $debug;
                    $timestamp = convert_to_timestamp(0,$1,$2,$3,$4,$5,$6);

                } elsif (/^\[(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+(\d+)\s+(\d{2}):(\d{2}):(\d{2})\.(\d+)\s+(\d{4})\]\s+.+/) {
                    # Format 10:
                    # [Sun Jan  1 01:02:03.123456 2018] some log text....
                    print STDERR "DBG 10:$line" if $debug;
                    $timestamp = convert_to_timestamp($8,$2,$3,$4,$5,$6,$7);

                } elsif (/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d+)([-|+])(\d{2}:\d{2}|\d{4})\s+.+/) {
                    # Format 11:
                    # 2019-05-06T02:40:57.170+00:00 some log text....
                    # 2019-05-06T02:40:57.170+0000 some log text....
                    # Starting with DSC 1.12 now using this format.
                    print STDERR "DBG 11:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/^type=\S+ msg=audit\((\d+)\.(\d+):\d+\):\s+.+/) {
                    # Format 12:
                    # type=USER_AUTH msg=audit(1565792757.382:75694): some log text....
                    # Coming from /var/log/audit/audit.log
                    print STDERR "DBG 12:$line" if $debug;
                    $line = " "x30 . $line;
                    $timestamp = sprintf "%010d.%s", $1, sprintf("%-6s", $2) =~ tr/ /0/r;

                } elsif (/^<\d+>\d+ (\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d+)([-|+])(\d{2}):(\d{2})\s+.+/) {
                    # Format 13:
                    # <141>1 2019-08-15T15:35:54.795348+02:00 SC-1 some log text....
                    # Coming from /var/log/opensaf/osaf.log
                    print STDERR "DBG 13:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/^(\d{2})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})\.(\d+)\s+.+/) {
                    # Format 14:
                    # 19-07-30 10:31:35.291 some log text....
                    # Coming from sec/sec-cert.log
                    print STDERR "DBG 14:$line" if $debug;
                    if ($1 < 70) {
                        # 00-69 => 2000-2069
                        $timestamp = convert_to_timestamp(2000+$1,$2,$3,$4,$5,$6,$7);
                    } else {
                        # 70-99 => 1970-1999
                        $timestamp = convert_to_timestamp(1900+$1,$2,$3,$4,$5,$6,$7);
                    }

                } elsif (/^<\d+>\d+ (\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d+)Z\s+.+/) {
                    # Format 15:
                    # <141>1 2019-08-15T15:35:54.795348Z SC-1 some log text....
                    # Coming from /var/log/opensaf/osaf.log
                    print STDERR "DBG 15:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})[,\.](\d+)\s+.+/) {
                    # Format 16:
                    # 2019-09-05 09:02:00.022 some log text....
                    # 2019-09-05 09:02:00,022 some log text....
                    # Coming from /opt/cdclsv/storage/log/* log files
                    print STDERR "DBG 16:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})([-|+])(\d{2}):\s+.+/) {
                    # Format 17:
                    # 2019-05-06T02:40:57+00: some log text....
                    # Starting with DSC 1.14 now using this format, but it looks like a bug
                    # from LDE or something else because it only happens sometimes.
                    print STDERR "DBG 17:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,0);

                } elsif (/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d+):(\d{2})\s+.+/) {
                    # Format 18:
                    # 2019-05-06T02:40:57.123:00 some log text....
                    # Starting with DSC 1.14 now using this format, but it looks like a bug
                    # from LDE or something else because it only happens sometimes.
                    print STDERR "DBG 18:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d+)Z?\s+.+/) {
                    # Format 19:
                    # 2022-03-04T02:29:16.650Z some log text....
                    # Coming from Kubernetes log files
                    print STDERR "DBG 19:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/"timestamp"\s*:\s*"(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d+)Z"/) {
                    # Format 20:
                    # {"version": "1.1.0", "timestamp": "2022-03-04T02:29:15.645Z", some log text....
                    # Coming from Kubernetes log files
                    print STDERR "DBG 20:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/^\[(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})\.(\d+)\].+/) {
                    # Format 21:
                    # [2022-03-04 03:26:06.366]some log text....
                    # Coming from Kubernetes log files
                    print STDERR "DBG 21:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/"timestamp"\s*:\s*"(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d+)[+-](\d{2}:\d{2}|\d{4})"/) {
                    # Format 22:
                    # some log text "timestamp":"2022-03-04T03:25:52.681+00:00" some log text....
                    # some log text "timestamp":"2022-03-04T03:25:52.681+0000" some log text....
                    # Coming from Kubernetes log files
                    print STDERR "DBG 22:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } elsif (/"timestamp"\s*:\s*"(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})Z?"/) {
                    # Format 23:
                    # "timestamp":"2022-03-04T02:29:15Z" some log text....
                    # Coming from Kubernetes log files
                    print STDERR "DBG 23:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,0);

                } elsif (/^time="(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})Z?"/) {
                    # Format 24:
                    # time="2022-03-04T03:27:05Z" some log text....
                    # Coming from Kubernetes log files
                    print STDERR "DBG 24:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,0);

                } elsif (/ts=(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d+)Z?\s+/) {
                    # Format 25:
                    # ts=2022-03-04T03:28:43.138742817Z some log text....
                    # Coming from Kubernetes log files
                    print STDERR "DBG 25:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,0);

                } elsif (/^(\d{4})-(\d{2})-(\d{2})T?(\d{2}):(\d{2}):(\d{2})\s+.+/) {
                    # Format 26:
                    # 2019-09-05 09:02:00 some log text....
                    # Coming from Kubernetes log files
                    print STDERR "DBG 26:$line" if $debug;
                    $timestamp = convert_to_timestamp($1,$2,$3,$4,$5,$6,$7);

                } else {
                    print STDERR "DBG 99:$line" if $debug;
                    if ($append_unknown_to_previous_timestamp == 1) {
                        if ($timestamp eq "0000000000.000000") {
                            $line = ">> Unknown Time Format >> $line";
                        }
                    } else {
                        $timestamp = "0000000000.000000";
                        $line = ">> Unknown Time Format >> $line";
                    }

                }

                if (@include) {
                    $found = 0;
                    for $match (@include) {
                        if (/$match/) {
                            $found = 1;
                            last;
                        }
                    }
                    if ($found == 0) {
                        # Not wanted line, take next line
                        next;
                    }
                }
                if (@exclude) {
                    $found = 0;
                    for $match (@exclude) {
                        if (/$match/) {
                            $found = 1;
                            last;
                        }
                    }
                    if ($found == 1) {
                        # Not wanted line, take next line
                        next;
                    }
                }
                if ($include_timestamp_start > 0 && $timestamp < $include_timestamp_start) {
                    # Not wanted line, take next line
                    next;
                }
                if ($include_timestamp_stop > 0 && $timestamp > $include_timestamp_stop) {
                    # Not wanted line, take next line
                    next;
                }

                # Save the line
                if ($use_temp_file_for_sorting) {
                    print TEMPFILE "$timestamp: $line";
                } else {
                    push @{$log{$timestamp}}, $line;
                }
            }
            close INF;
        } else {
            push @files_with_problem, "  $filepath\n";
        }
    }

    if ($use_temp_file_for_sorting) {
        close TEMPFILE;

        print "Sorting temp file, please wait...\n";
        `sort tmp_unsorted_file.tmp > tmp_sorted_file.tmp`;

        # Now read the sorted file and remove the timestamp and write back to STDOUT
        # and perform the color highlighting
        unless (open TEMPFILE, "tmp_sorted_file.tmp") {
            print "Unable to open temporary sorted file for reading\n";
            exit 1;
        }
        print STDERR "Applying color filters\n";
        while (<TEMPFILE>) {
            # Remove timestamp
            s/^\d+\.\d+: //;

            # Add colors for special strings
            for $match (@match_cyan) {
                s/($match)/$color_cyan$1$color_reset/g;
            }
            for $match (@match_green) {
                s/($match)/$color_green$1$color_reset/g;
            }
            for $match (@match_red) {
                s/($match)/$color_red$1$color_reset/g;
            }
            for $match (@match_yellow) {
                s/($match)/$color_yellow$1$color_reset/g;
            }

            # Print the line
            print $_;
        }
        close TEMPFILE;

        # Remove temporary files
        unlink "tmp_unsorted_file.tmp";
        unlink "tmp_sorted_file.tmp";
    } else {
        print STDERR "Sorting log entries and applying color filters\n";
        #print "Maxlength=$max_filename_length\n";

        # Go through all collected log entries and add color and print them out
        for my $key (sort keys %log) {
            # For each log entry with same time stamp add color and print line
            for (@{$log{$key}}) {
                # Add colors for special strings
                for $match (@match_cyan) {
                    s/($match)/$color_cyan$1$color_reset/g;
                }
                for $match (@match_green) {
                    s/($match)/$color_green$1$color_reset/g;
                }
                for $match (@match_red) {
                    s/($match)/$color_red$1$color_reset/g;
                }
                for $match (@match_yellow) {
                    s/($match)/$color_yellow$1$color_reset/g;
                }

                # Print the line
                print $_;
            }
        }
    }

    if (@files_with_problem) {
        print "\nThe following files could not be opened:\n";
        for (@files_with_problem) {
            print $_;
        }
    }
}

exit 0;

#
# Subroutines
# -----------
#

# -----------------------------------------------------------------------------
sub convert_to_timestamp {
    my $year   = shift;
    my $month  = shift;
    my $day    = shift;
    my $hour   = shift;
    my $minute = shift;
    my $second = shift;
    my $usecs  = shift;

    my $time;

    # Convert passed in month value into proper format
    $month = $mon_to_num{"$month"};

    # Convert year 0 into a proper year
    if ($year == 0) {
        if ($month > $current_month) {
            $year = $current_year - 1;
        } else {
            $year = $current_year;
        }
    }

    # Convert milliseconds to microseconds
    $usecs = sprintf("%-6s", $usecs) =~ tr/ /0/r;

    $time = Time::Local::timelocal($second,$minute,$hour,$day,$month,$year);
    return sprintf "%010d.%s", $time, $usecs;
}

# -----------------------------------------------------------------------------
sub parse_timestamp_parameter
{
    my ($opt_name, $opt_value) = @_;

    my $day;
    my $hour;
    my $minute;
    my $month;
    my $second;
    my $timestamp;
    my $year;

    if ($opt_value =~ /^(\d{4})-?(\d{2})-?(\d{2})[T ]?(\d{2}):?(\d{2}):?(\d{2})$/) {
        # E.g. "2020-02-07T11:22:30", or "2020-02-07 11:22:30"
        $year   = $1;
        $month  = $2;
        $day    = $3;
        $hour   = $4;
        $minute = $5;
        $second = $6;
    } elsif ($opt_value =~ /^(\d{2}):?(\d{2}):?(\d{2})$/) {
        # E.g. "11:22:30"
        $year   = $current_year;
        $month  = $current_month+1;
        $day    = $current_day;
        $hour   = $1;
        $minute = $2;
        $second = $3;
    } elsif ($opt_value =~ /^(\d{2}):?(\d{2})$/) {
        # E.g. "11:22"
        $year   = $current_year;
        $month  = $current_month+1;
        $day    = $current_day;
        $hour   = $1;
        $minute = $2;
        $second = 0;
    } else {
        print "Invalid timestamp format: $opt_name $opt_value\n";
        exit 1;
    }

    $timestamp = convert_to_timestamp($year,$month,$day,$hour,$minute,$second,0);
    if ($opt_name eq "e" || $opt_name eq "end-timestamp") {
        $include_timestamp_stop = $timestamp;
    } else {
        $include_timestamp_start = $timestamp;
    }
    #printf "DBG:opt_name=$opt_name, opt_value=$opt_value, year=$year, month=$month, day=$day, hour=$hour, minute=$minute, second=$second, timestamp=$timestamp, localtime=%s\n", scalar localtime($timestamp);

    return 1;
}

#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.1
#  Date     : 2020-07-10 16:04:29
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2016, 2020
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
use File::Basename;

sub show_help {
    print <<EOF;

Description:
============

This script reads a file and looks for a copyright header at the beginning of the
file that contains the following strings:

Revision : x.y
Date     : yyyy-mm-dd hh:mm:ss

And if found then it will increment the revision information and set the current
time stamp as the date.
The changes will be done directly in the file on disk and no backup file will
be generated.
The output will always be in UNIX (LF) format even if the input file was in
DOS (CRLF) format.

Syntax:
=======

 $0 [<OPTIONAL>] <MANDATORY>


 <MANDATORY> are all of the following parameters:

  <filename>

 <OPTIONAL> are one or more of the following parameters:

  -a | --author <name>
  -d | --date <yyyy-mm-dd hh:mm:ss>
  -n | --no-confirmation
  -o | --only-date
  -h | --help
  -r | --revision <new revision>
  -s | --stdout


 where:

  <filename>
  Specifies the file that should be updated.
  Multiple files can be specified.


  --author <name>
  -a <name>
  If specified then it will also change the Author line with the provided name.


  --date <yyyy-mm-dd hh:mm:ss>
  -d <yyyy-mm-dd hh:mm:ss>
  If specified then it will change the Date with the the provided date instead
  of using the current time stamp.


  --help
  -h
  Shows this help.


  --no-confirmation
  -n
  If specified then it will not ask for permission before changing the file
  on disk.


  --only-date
  -o
  If specified then it will only update the date information, revision will be
  left as-is.


  --revision <new revision>
  -r <new revision>
  If specified then it will change the Revision to the specified version
  instead of incrementing the revision.


  --stdout
  -s
  If specified then the output will be written to STDOUT instead of back to
  disk. No changes will be done to the file stored on disk.


Examples:
=========

  Example 1:
  ----------
  To step revision information for a file:

  $0 script_file.exp

  Example 2:
  ----------
  To set specific date and revision for a file:

  $0 --date="2016-05-24 12:10:00" --revision=2.0 script_file.exp

  Example 3:
  ----------
  To step revision and date and set Author tag for two files:

  $0 --author=eustone script_file1.exp script_file2.pl


Return code:
============

 0: Successful
 1: Unsuccessful, some failure detected.

EOF
}

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $debug = 0;
    # 0: No debug information printed
    # 1: Debug information printed
my $author = "";
my $changes = 0;
my $datestr ="";
my $filename = "";
my @file_names = ();
my @input_data = ();
my $no_confirmation = 0;
my $only_update_date = 0;
my $revision = "";
my $show_all = 0;
my $use_stdout = 0;
my $show_help = 0;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;            # standard Perl module
GetOptions (
    "d|date=s"             => \$datestr,
    "debug"                => \$debug,
    "a|author=s"           => \$author,
    "n|no-confirmation"    => \$no_confirmation,
    "o|only-date"          => \$only_update_date,
    "r|revision=s"         => \$revision,
    "s|stdout"             => \$use_stdout,
    "h|help"               => \$show_help
);

if ($show_help) {
    show_help();
    exit 0;
}

while ($_ = $ARGV[0]) {
    $filename = shift;
    unless (-f $filename) {
        print STDERR "ERROR: '$filename' file does not exist, ignored\n";
        next;
    }
    push @file_names, $filename;

}

if (scalar @file_names == 0) {
    print STDERR "ERROR: You have to specify at least one input file\n";
    exit 1;
}

if ($datestr eq "") {
    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime;
    $datestr = sprintf "%4d-%02d-%02d %02d:%02d:%02d", $year+1900, $mon+1, $mday, $hour, $min, $sec;
    print STDERR "Date=$datestr\n" if $debug;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

for $filename (@file_names) {
    print STDERR "Processing file '$filename'\n";

    # Read file into an array
    read_file($filename, \@input_data);

    # Change date and revision information
    $changes = process_data(\@input_data, $author, $datestr, $revision);

    print STDERR "Changes=$changes\n" if $debug;
    if ($changes > 0) {
        write_file($filename, \@input_data);
    }

    # Clear read data
    @input_data = ();
}

# Exit with success
exit 0;

#
# Subroutines
# -----------
#

#
# Increment the revision string.
#
# Input:
#  - Current revision string
#
# Output:
#  - New revision string
#
sub increment_revision {
    my $current_value = shift;

    my $new_value;
    my $num;
    my $char;

    if ($current_value =~ /^(\d+)\.(\d+)$/) {
        $new_value = sprintf "%d.%d", $1, $2+1;
    } elsif ($current_value =~ /^R(\d+)([A-Z])$/) {
        $num = $1;
        $char = $2;
        if (ord($char) == 90) {     # Z
            # If we have already reached Z revision, then step the revision number
            $num++;
            $char = "A";
        } else {
            $char = chr( ord($char) + 1);
        }
        $new_value = sprintf "R%d%s", $num, $char;
    } else {
        print STDERR "WARNING: Not supported revision '$current_value', setting default '1.0' value\n" if $debug;
        $new_value = "1.0";
    }
}

# Parse data and store in a hash.
#
# Input:
#  - Reference to array used as input.
#  - Author string
#  - Date string
#  - Revision string
#
# Output:
#  - The hash is updated with all found data.
#
sub process_data {
    my $array_ref = shift;
    my $new_author = shift;
    my $new_datestr = shift;
    my $new_revision = shift;

    my $before;
    my $wanted;
    my $after;
    my $new;
    my $linecnt = 0;
    my $changecnt = 0;

    print STDERR "Updating file\n" if $debug;
    #for ($linecnt=0; $i <= @{$array_ref}) {
    for ($linecnt=0; $linecnt <= scalar @{$array_ref}; $linecnt++) {
        $_ = $array_ref->[$linecnt];
        # print "debug:Line=$_\n";
        if (/^(#\s*\$*Author\s*:\s*)(\S+)(.*)$/) {
            next if $only_update_date;
            $before = $1;
            $wanted = $2;
            $after  = $3;
            if ($new_author eq "") {
                if ($wanted eq "username") {
                    $new = lc($ENV{"USER"});
                } else {
                    $new = $wanted;
                }
            } else {
                $new = $new_author;
            }
            if ($new ne $wanted) {
                print STDERR "Changing line:\n$_\nTo:\n$before$new$after\n\n" if $debug;
                $array_ref->[$linecnt] = "$before$new$after";
                $changecnt++;
            }
        } elsif (/^(<!--\s*\$*Author\s*:\s*)(\S+)(.*)$/) {
            next if $only_update_date;
            $before = $1;
            $wanted = $2;
            $after  = $3;
            if ($new_author eq "") {
                if ($wanted eq "username") {
                    $new = lc($ENV{"USER"});
                } else {
                    $new = $wanted;
                }
            } else {
                $new = $new_author;
            }
            if ($new ne $wanted) {
                print STDERR "Changing line:\n$_\nTo:\n$before$new$after\n\n" if $debug;
                $array_ref->[$linecnt] = "$before$new$after";
                $changecnt++;
            }
        } elsif (/^(#\s*\$*Date\s*:\s*)(....[-\/]..[-\/].. ..:..:..)(.*)$/) {
            $before = $1;
            $wanted = $2;
            $after  = $3;
            $new = $new_datestr;
            if ($new ne $wanted) {
                print STDERR "Changing line:\n$_\nTo:\n$before$new$after\n\n" if $debug;
                $array_ref->[$linecnt] = "$before$new$after";
                $changecnt++;
            }
        } elsif (/^(<!--\s*\$*Date\s*:\s*)(....[-\/]..[-\/].. ..:..:..)(.*)$/) {
            $before = $1;
            $wanted = $2;
            $after  = $3;
            $new = $new_datestr;
            if ($new ne $wanted) {
                print STDERR "Changing line:\n$_\nTo:\n$before$new$after\n\n" if $debug;
                $array_ref->[$linecnt] = "$before$new$after";
                $changecnt++;
            }
        } elsif (/^(#\s*\$*Date\s*:\s*)(\S+)(.*)$/) {
            $before = $1;
            $wanted = $2;
            $after  = $3;
            $new = $new_datestr;
            if ($new ne $wanted) {
                print STDERR "Changing line:\n$_\nTo:\n$before$new$after\n\n" if $debug;
                $array_ref->[$linecnt] = "$before$new$after";
                $changecnt++;
            }
        } elsif (/^(<!--\s*\$*Date\s*:\s*)(\S+)(.*)$/) {
            $before = $1;
            $wanted = $2;
            $after  = $3;
            $new = $new_datestr;
            if ($new ne $wanted) {
                print STDERR "Changing line:\n$_\nTo:\n$before$new$after\n\n" if $debug;
                $array_ref->[$linecnt] = "$before$new$after";
                $changecnt++;
            }
        } elsif (/^(#\s*\$*Revision\s*:\s*)(\S+)(.*)$/) {
            next if $only_update_date;
            $before = $1;
            $wanted = $2;
            $after  = $3;
            if ($new_revision eq "") {
                $new = increment_revision($wanted);
            } else {
                $new = $new_revision;
            }
            if ($new ne $wanted) {
                print STDERR "Changing line:\n$_\nTo:\n$before$new$after\n\n" if $debug;
                $array_ref->[$linecnt] = "$before$new$after";
                $changecnt++;
            }
        } elsif (/^(<!--\s*\$*Revision\s*:\s*)(\S+)(.*)$/) {
            next if $only_update_date;
            $before = $1;
            $wanted = $2;
            $after  = $3;
            if ($new_revision eq "") {
                $new = increment_revision($wanted);
            } else {
                $new = $new_revision;
            }
            if ($new ne $wanted) {
                print STDERR "Changing line:\n$_\nTo:\n$before$new$after\n\n" if $debug;
                $array_ref->[$linecnt] = "$before$new$after";
                $changecnt++;
            }
        }

        # Only look for the information at the top of the file
        last if $linecnt > 25;
    }

    return $changecnt;
}

#
# Read the input file and store data without CR LF into array reference.
#
# Input:
#  - File name
#  - Reference to array where data is saved
#
# Output:
#  - The array is updated with all found data.
#
sub read_file {
    my $filename = shift;
    my $array_ref = shift;

    my @temp_array = ();

    if (open INF, "$filename") {
        print STDERR "Opening file '$filename'\n" if $debug;
        while (<INF>) {
            s/[\r\n]//g;
            push @temp_array, $_;
        }
        close INF;
        @{$array_ref} = @temp_array;
    } else {
        print STDERR "ERROR: Unable to open the file '$filename'\n";
        exit 1;
    }
}

#
# Write the input data in array reference to file.
#
# Input:
#  - File name
#  - Reference to array where data is saved
#
# Output:
#  -
#
sub write_file {
    my $filename = shift;
    my $array_ref = shift;
    my $tempfilename = "/tmp/step_revision_backup_file.$$";
    my $writeerrorcnt = 0;

    if ($use_stdout) {
        print STDERR "Writing to STDOUT\n";
        for (@{$array_ref}) {
            print "$_\n";
        }
    } else {
        # Create a temporary backup file
        print STDERR "Creating temporary backup file '$tempfilename'\n" if $debug;
        `cp -pf $filename $tempfilename`;
        if ($? == 0) {
            print STDERR "Writing to file '$filename'\n";
            if (open OUTF, "+>$filename") {
                for (@{$array_ref}) {
                    unless (print OUTF "$_\n") {
                        print STDERR "ERROR: Failed to write line '$_' to file '$filename'\n";
                        $writeerrorcnt++;
                        last;
                    }
                }
                unless (close OUTF) {
                    print STDERR "ERROR: Failed to close file '$filename'\n";
                    $writeerrorcnt++;
                }
            } else {
                print STDERR "ERROR: Unable to open the file '$filename' for writing\n";
                $writeerrorcnt++;
            }

            if ($writeerrorcnt) {
                print STDERR "If the original file '$filename' have been corrupted for any reason then check backup file '$tempfilename'\n";
                exit 1;
            }

            # Remove the temporary backup file
            print STDERR "Deleting temporary backup file '$tempfilename'\n" if $debug;
            `rm -f $tempfilename`;
            if ($?) {
                print STDERR "ERROR: Unable to remove backup file '$tempfilename'\n";
                exit 1;
            }
        } else {
            print STDERR "ERROR: Unable to create backup file '$tempfilename'\n";
            exit 1;
        }
    }
}

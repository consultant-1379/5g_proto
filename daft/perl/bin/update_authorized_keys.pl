#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.2
#  Date     : 2023-01-20 12:28:48
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2023
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
use Cwd qw(abs_path);
use File::Basename;

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $create_ssh_directory = 0;
my @duplicated_entries = ();
my $filename = "";
my %key_hash;
my %key_hash_new;
my %key_hash_existing;
my @new_entries = ();
my $no_backup_file = 0;
my $number_of_existing_keys = 0;
my $number_of_keys = 0;
my $number_of_new_keys = 0;
my @output = ();
my $output_path = "$ENV{'HOME'}/.ssh";
my $output_file = "$output_path/authorized_keys";
my $script_path = dirname abs_path($0);
my $show_help = 0;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;            # standard Perl module
GetOptions (
    "c|create-ssh-directory"    => \$create_ssh_directory,
    "f|filename=s"              => \$filename,
    "h|help"                    => \$show_help,
    "n|no-backup-file"          => \$no_backup_file,
);

if ($show_help) {
    show_help();
    exit 0;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

#
# Check input file if specified, otherwise use the template file
#

if ($filename eq "") {
    if (-f "$script_path/../../templates/authorized_keys/authorized_keys_Template") {
        $filename = abs_path("$script_path/../../templates/authorized_keys/authorized_keys_Template");
        print "- No filename given so using default file: $filename\n";
    } else {
        print "Error: No filename given containing authorized keys to add to $output_file file\n";
        exit 1;
    }
} elsif (! -f "$filename") {
    print "Error: Specified filename $filename does not exist\n";
    exit 1;
}

#
# Read the input file
#

print "- Reading filename $filename\n";
open INF, "$filename" or  die "Error: Cannot open file $filename for reading\n";
while (<INF>) {
    s/[\r\n]//g;
    next if /^\s*$/;
    $key_hash{$_}++;
    $key_hash_new{$_}++;
}
close INF;
printf "  Found %d unique entries\n", scalar keys %key_hash_new;

#
# Create output directory if not existing
#

unless (-d "$output_path") {
    if ($create_ssh_directory == 1) {
        # No .ssh directory exist but we should create it
        print "- No $output_path directory exists, creating it\n";
        `mkdir -m 0700 -p $output_path`;
        if ($? != 0) {
            print "Error: Failed to create the directory\n";
            exit 1;
        }
    } else {
        print "Error: No $output_path directory exists\n";
        exit 1;
    }
}

#
# Read output file if existing, otherwise just copy the input file
#

if (-f "$output_file") {
    print "- Reading filename $output_file\n";
    open INF, "$output_file" or  die "Error: Cannot open file $output_file for reading\n";
    while (<INF>) {
        s/[\r\n]//g;
        next if /^\s*$/;
        $key_hash{$_}++;
        $key_hash_existing{$_}++;
    }
    close INF;
    printf "  Found %d unique entries\n", scalar keys %key_hash_existing;
} else {
    # File does not exist, all we need to do is to copy the input file to the output file
    print "- Copying $filename to $output_file\n";
    `cp $filename $output_file`;
    if ($? != 0) {
        print "Error: Failed to copy the file\n";
        exit 1;
    }
    print "- Changing access right of file to 0600\n";
    `chmod 0600 $output_file`;
    if ($? != 0) {
        print "Error: Failed to change access right\n";
        exit 1;
    }
    $number_of_keys = scalar keys %key_hash;
    print "- New $output_path/autorized_keys file created with $number_of_keys entries\n";
    exit 0;
}

#
# Check if output file needs to be updated
#

print "- Checking if $output_file file needs to be updated\n";
for my $key (sort keys %key_hash) {
    push @output, "$key\n";
    $number_of_keys++;
    if ($key_hash{$key} > 1) {
        # Check where the duplication is coming from, new file or existing file
        if (exists $key_hash_new{$key} && ! exists $key_hash_existing{$key}) {
            # The key is new and did not exist before
            $number_of_new_keys++;
            push @new_entries, "  $key";
        } elsif (exists $key_hash_new{$key} && exists $key_hash_existing{$key} && $key_hash_existing{$key} > 1) {
            # The key already existed before and was already duplicated in the old file
            push @duplicated_entries, "  $key";
        } elsif (exists $key_hash_new{$key} && exists $key_hash_existing{$key} && $key_hash_existing{$key} == 1) {
            # The key already existed before so no need to mark it as new
            $number_of_existing_keys++;
        } elsif (! exists $key_hash_new{$key}) {
            # The key was already duplicated in the old file
            push @duplicated_entries, "  $key";
        } else {
            printf "***** We should not end up here: line=%d *****\n", __LINE__;
            print "key=$key\n";
            printf "Exist in:\n\%key_hash_existing=%s\n\%key_hash_new=%s\n", exists $key_hash_existing{$key} ? "yes" : "no", exists $key_hash_new{$key} ? "yes" : "no";
        }
    } elsif (exists $key_hash_new{$key} && ! exists $key_hash_existing{$key}) {
        # The key is new and did not exist before
        $number_of_new_keys++;
        push @new_entries, "  $key";
    } elsif (exists $key_hash_existing{$key} && ! exists $key_hash_new{$key}) {
        # The key only exist in the old file, so no need to mark it as new or count as existing
    } else {
        printf "***** We should not end up here: line=%d *****\n", __LINE__;
        print "key=$key\n";
        printf "Exist in:\n\%key_hash_existing=%s\n\%key_hash_new=%s\n", exists $key_hash_existing{$key} ? "yes" : "no", exists $key_hash_new{$key} ? "yes" : "no";
    }
}

if (@new_entries || @duplicated_entries) {
    # There are changes that needs to be written to file

    #
    # Create backup file, if wanted
    #

    if ($no_backup_file == 0) {
        my $backup_file = sprintf "${output_file}_backup_%i", time();
        print "- Creating backup file: $backup_file\n";
        `cp -pf $output_file $backup_file`;
        if ($? != 0) {
            print "Error: Failed to create backup file\n";
            exit 1;
        }
    }

    #
    # Update the output file
    #

    print "- Updating $output_file file with missing keys and sorting keys\n";
    open OUTF, ">$output_file" or  die "Error: Cannot open file $output_file for writing\n";
    for my $key (@output) {
        print OUTF $key;
        if ($? != 0) {
            close OUTF;
            print "Error: Failed to write key '$key' to file $output_file\nThe file might not be complete, so make sure to manually add the following keys to the file:\n\n";
            for my $key2 (@output) {
                print $key2;
            }
            exit 1;
        }
    }
    close OUTF;

    #
    # Print status message after sucessful update
    #

    if ($number_of_new_keys > 0) {
        if ($number_of_existing_keys > 0) {
            print "- File $output_file updated with $number_of_keys entries where $number_of_new_keys are new entries and $number_of_existing_keys already existed before\n";
        } else {
            print "- File $output_file updated with $number_of_keys entries where $number_of_new_keys are new entries\n";
        }
    } else {
        if ($number_of_existing_keys > 0) {
            print "- File $output_file updated with $number_of_keys entries, no new entries added and $number_of_existing_keys already existed before\n";
        } else {
            print "- File $output_file updated with $number_of_keys entries, no new entries added\n";
        }
    }

    if (@duplicated_entries) {
        print "\n- The following duplicated entries were found and removed:\n" . (join "\n", @duplicated_entries) . "\n";
    }

    if (@new_entries) {
        print "\n- The following new entries were added:\n" . (join "\n", @new_entries) . "\n";
    }
} else {
    print "\n- There are no new or duplicated keys, so no need to update the $output_file file\n";
}

# Exit with success
exit 0;

#
# Subroutines
# -----------
#

#
# Show help information.
#
# Input:
#  -
#
# Output:
#  - Help information printed to STDOUT.
#
sub show_help {
    print <<EOF;

Description:
============

This script updates the $output_path/autorized_keys file
with new key entries taken from an input file or from a default file
and writes back the unique entries in sorted order to the file.
It can also create backup file before changing it.

Syntax:
=======

 $0 [<OPTIONAL>] <MANDATORY>


 <MANDATORY> are all of the following parameters:


 <OPTIONAL> are one or more of the following parameters:

  -c            | --create-ssh-directory
  -f <filename> | --filename <filename>
  -h            | --help
  -n            | --no-backup-file


 where:

  --create-ssh-directory
  -c
  ----------------------
  If specified then it will create the $output_path/ directory if it does not
  already exists.
  If not speficied and the directory does not exists then the script will
  exit with an error.


  --filename <filename>
  -f <filename>
  ---------------------
  Specifies the file that contain a list of autorized keys to add to the
  $output_path/authorized_keys file.
  If not specified then it will look for the file relative to the path of
  this script ../../templates/authorized_keys/authorized_keys_Template.


  --help
  -h
  ------
  Shows this help.


  --no-backup-file
  -n
  --------------------
  If specified then it will skip creating a copy for the $output_path/autorized_keys
  file before changing it.
  By default if not specified then it will create a backup file in the same directory called
  $output_path/autorized_keys_backup_xxxxx where xxxxx is the current epoch
  (seconds since Jan 1, 1970 at 00:00:00 UTC).


Examples:
=========

  Example 1:
  ----------
  To update the $output_path/authorized_keys file with the contents from the
  $script_path/../../templates/authorized_keys/authorized_keys_Template file:

  $0

  Example 2:
  ----------
  To update the $output_path/authorized_keys file with the contents from the
  specified file:

  $0 \\
    --filename /path/to/my_authorized_keys.txt


Return code:
============

 0: Successful
 1: Unsuccessful, some failure detected.

EOF
}

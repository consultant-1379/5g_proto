#!/usr/bin/perl

################################################################################
#
#  Author   : eustone, xgiobal
#
#  Revision : 1.14
#  Date     : 2024-04-15 12:07:19
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2015,2016,2017,2018,2023,2024
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

sub show_help {
    print <<EOF;

Description:
============

This script checks either the server that runs the DAFT framework (local)
or the cluster (remote), in order to verify that the required programs are
in the PATH variable for the local server and also that there is enough
disk space available for both local and remote so the installation or
upgrade procedure can be executed without any issues.

The script still remains backwards compatible with SUF server, which means
that if it called without parameters, it will check if the programs exist
in the PATH variable and also if at least 2048 mega bytes (M) of free disk
space is available at "/var/lib/sufstorage" path.


Syntax:
=======

 $0 [<OPTIONAL>]


 <OPTIONAL> are one or more of the following parameters:

  --check-command <command name>
  --check-free-space <size>,<directory path>[,sudo]
  --help
  --verbose


 where:

  --check-command <command name>
  -c <command name>
  ------------------------------
  Checks if the specified command is in the PATH which allows it to executed
  without using the fully qualified path to the command.
  This parameter can be specified multiple times if more than one check is
  needed.


  --check-free-space <size>,<directory path>[,sudo]
  -f <size>,<directory path>
  -------------------------------------------------
  Check that the directory path has enough free space available where the
  <size> can be specified as:

    - <integer>
      The <integer> is in mega bytes (M).
      For example: 2048

    - <integer><unit>
      The <unit> is specified as one of:
        - K,M,G,T,P,E,Z,Y           (powers of 1024)
        - KB,MB,GB,TB,PB,EB,ZB,YB   (powers of 1000)

  If the optional ",sudo" attribute is specified then the check will be done with
  'sudo' rights to be able to access directory path's that can only be accessed
  by root.
  This parameter can be specified multiple times if more than one path check
  is needed.


  --help
  -h
  ------
  Shows this help.


  --verbose
  -v
  ---------
  Print more detailed data.


Examples:
=========

  Example 1:
  ----------
  Check for programs available and for at least 2048 mega bytes (M) free on the DAFT Server:

  $0

  Example 2:
  ----------
  Check for programs available and for at least 2048 mega bytes (M) free on the DAFT Server
  and print detailed information:

  $0 --verbose

  Example 3:
  ----------
  Do a check for at least 7 giga bytes (G) free disk space (e.g. on a target node) under
  the /cluster partition:

  $0 --check-free-space 7168,/cluster
  $0 --check-free-space 7G,/cluster

  Example 4:
  ----------
  Check for programs available and for at least 2 giga bytes (G) free disk space on the DAFT
  Server.

  $0 --check-command perl --check-command expect --check-command python --check-free-space 2048,/var/lib/sufstorage
  $0 --check-command perl --check-command expect --check-command python --check-free-space 2G,/var/lib/sufstorage

  Example 5:
  ----------
  Show help information:

  $0 --help


Return code:
============

 0: Successful
 1: Unsuccessful, some failure detected.

EOF
}

my @check_command = ();
my @check_free_space = ();
my $cnt_errors = 0;
my $cnt_warnings = 0;
my $separator_length = 76;
my $show_help = 0;
my @summary = ();
my $verbose = 0;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;            # standard Perl module
Getopt::Long::Configure ("bundling");   # To allow -vv instead of -v -v
GetOptions (
    "c|check-command=s"     => \@check_command,
    "f|check-free-space=s"  => \@check_free_space,
    "h|help"                => \$show_help,
    "v|verbose:+"           => \$verbose
);

if ($show_help) {
    show_help();
    exit 0;
}

if (scalar @check_command == 0) {
    if (scalar @check_free_space == 0) {
        # To be compatible with the old SUF framework
        push @check_command, "expect";
        push @check_command, "perl";
        push @check_command, "python";
        push @check_free_space, "2048,/var/lib/sufstorage";
    }
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Check if programs are in the PATH
for (@check_command) {
    program_in_path($_);
}

# Check free disk space
for (@check_free_space) {
    if (/^(\d+),(.+)$/i) {
        disk_size_free($2, $1);
    } elsif (/^(\d+)(K|M|G|T|P|E|Z|Y|KB|MB|GB|TB|PB|EB|ZB|YB),(.+)$/i) {
        disk_size_free($3, "$1$2");
    } else {
        print "Invalid format for parameter '--check-free-space=$_'\n";
        exit 1;
    }
}

if ($cnt_errors+$cnt_warnings == 0) {
    print_result("The status of the server is OK!");
    exit 0;
} else {
    print_result("The status of the server is NOT OK!");
    exit 1;
}

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------
sub disk_size_free {
    my $path = shift;
    my $wanted_free_size = shift;

    my $cmd;
    my @disk_free = ();
    my $current_free_value = 0;
    my $sudo_needed = 0;
    my $wanted_free_value;
    my $unit;

    # Check if sudo rights are needed to access the directory path
    if ($path =~ /^(.+),sudo$/) {
        $path = $1;
        $sudo_needed = 1;
    }

    if ($wanted_free_size =~ /^(\d+)$/) {
        $wanted_free_value = $1;
        $unit = "M";
    } elsif ($wanted_free_size =~ /^(\d+)(K|M|G|T|P|E|Z|Y|KB|MB|GB|TB|PB|EB|ZB|YB)$/i) {
        $wanted_free_value = $1;
        $unit = uc($2);
    } else {
        print_error("NOT OK, Incorrect disk size value specified ($wanted_free_size)");
        print_separator();
        push @summary, "NOT OK: Disk Size of $path";
        return;
    }
    print_heading("Disk Size of $path");

    if ($sudo_needed == 1) {
        $cmd = "sudo --non-interactive df -B $unit $path";
    } else {
        $cmd = "df -B $unit $path";
    }
    @disk_free = `$cmd 2>&1`;
    if ($verbose > 0) {
        print "$cmd\n";
        print for (@disk_free);
        print "\n";
    }
    if ($?) {
        print_error("NOT OK, Unable to check disk size, command '$cmd' returned exit code $?");
        print_separator();
        push @summary, "NOT OK: Disk Size of $path";
        return;
    }

    for (@disk_free) {
        if (/^\S+\s+\d+$unit\s+\d+$unit\s+(\d+)$unit\s+\d+\%\s+.+/) {
            # Filesystem     1K-blocks     Used Available Use% Mounted on
            # /dev/sda2       48062344 34802740  10818132  77% /
            $current_free_value = $1;
        } elsif (/^\s+\d+$unit\s+\d+$unit\s+(\d+)$unit\s+\d+\%\s+.+/) {
            # Filesystem           1K-blocks    Used Available Use% Mounted on
            # /dev/mapper/VolGroup00-LogVol00
            #                        8415964 2489152   5492636  32% /
            $current_free_value = $1;
        }
    }

    if ($current_free_value >= $wanted_free_value) {
        print "OK, disk $path has $current_free_value $unit free, which is at least the same or larger than the wanted $wanted_free_value $unit free\n";
        push @summary, "    OK: Disk Size of $path";
    } else {
        print_warning("NOT OK, disk $path only has $current_free_value $unit free but needs $wanted_free_value $unit free.");
        push @summary, "NOT OK: Disk Size of $path";
    }
    print_separator();
}

# -----------------------------------------------
sub print_error {
    my $line = shift;

    print "$line\n";
    $cnt_errors++;
}

# -----------------------------------------------
sub print_heading {
    my $line = shift;

    printf "%s\n\n", "="x$separator_length;
    print "$line\n\n";
    printf "%s\n\n", "-"x$separator_length;
}

# -----------------------------------------------
sub print_result {
    my $line = shift;

    printf "%s\n\n", "="x$separator_length;
    print "Summary:\n";
    for (@summary) {
        print "$_\n";
    }
    print "\n$line\n\n";
    printf "%s\n\n", "="x$separator_length;
}

# -----------------------------------------------
sub print_separator {
    printf "\n%s\n\n\n", "="x$separator_length;
}

# -----------------------------------------------
sub print_warning {
    my $line = shift;

    print "$line\n";
    $cnt_warnings++;
}

# -----------------------------------------------
sub program_in_path {
    my $program = shift;

    print_heading("Checking if program '$program' exists in \$PATH");

    my $cmd = "which $program";
    my @result = `$cmd 2>&1`;
    if ($verbose > 0) {
        print "$cmd\n";
        print for (@result);
        print "\n";
    }
    if ($?) {
        print_error("NOT OK, Unable to find program, command '$cmd' returned exit code $?");
        push @summary, "NOT OK: Checking if program '$program' exists in \$PATH";
    } else {
        print "OK, program '$program' found in \$PATH\n";
        push @summary, "    OK: Checking if program '$program' exists in \$PATH";
    }
    print_separator();
    return;
}

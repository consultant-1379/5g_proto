#!/usr/bin/perl

################################################################################
#
#  Author   : everhel
#
#  Revision : 1.3
#  Date     : 2024-03-07 10:07:13
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022,2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################
#
# 1.2 : eedwiq (DND-33247)
#       - added check if protected directory contains unpacked csar, if so remove it
#       - moved check for show help to first check after GetOp‭tions()
#       - replaced sub send_cmd_read_output(), with new sub send_cmd_read_directory("directory","grep-filter")
#       - added sub send_cmd_read_directory("directory","grep-filter"), loads directory content into an array
#         and returns the resulting array
#       - changed behaviour to keep packages that are part of protected files but without a build number in the
#         protected files file. For example protected_files.txt contains 1.7.2/ then all packages containing
#         1.7.2 will be considered as protected.
#
use strict;
use warnings;

my $directory_to_list = "";
my $path_to_file = "";
my $rc = 0;
my $show_help;

my @cmd_output = ();
my @lines_in_file = ();

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "d|directory=s" => \$directory_to_list,
    "f|file=s"      => \$path_to_file,
    "h|help"        => \$show_help,
);

if ( $show_help ) {
    show_help();
    exit 0;
}

if ( $directory_to_list eq "" ) {
    print "Path to directory not provided\n";
    exit 1;
}

if ( $path_to_file eq "" ) {
    print "Path to file not provided\n";
    exit 1;
}


# **************
# *            *
# * Main logic *
# *            *
# **************

@cmd_output = send_cmd_read_directory($directory_to_list,"\/\$");

$rc = read_file_to_array();

if ( $rc == 1 ) {
    print "Something went wrong trying to read file\n";
    exit $rc;
}

$rc = clean_directory();

if ( $rc == 1 ) {
    print "Something went wrong during directory removal\n";
    exit $rc;
}

exit $rc;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub clean_directory {

    my $mark_for_removal = 0;
    my $rc = 0;

    print "Files and directories to protect\n";
    foreach my $line ( @lines_in_file )
    {
        print "$line\n";
    }

    print "\nDirectories in provided directory\n";
    foreach my $line ( @cmd_output )
    {
        print "$line\n";
    }

    print "\n";
    foreach my $directory_to_remove ( @cmd_output ) {
        $mark_for_removal = 0;
        print "checking directory: '$directory_to_remove'\n";
        foreach my $protected_file ( @lines_in_file ) {
            if ( $directory_to_remove ne $protected_file ) {
                my $chopped = $protected_file;
                chop($chopped);
                if ((!$protected_file) || (index($directory_to_remove,$chopped) == -1 )) {
                    $mark_for_removal = 1;
                } else {
                    $mark_for_removal = 0;
                    my $cmd;
                    # added check if directory contains unpacked csar, if so remove it
                    my @dir_content = send_cmd_read_directory ("$directory_to_list/$directory_to_remove/csar","-v csar");
                    if (scalar @dir_content != 0 ) {
                        print "   Removing (unpacked) files:\n";
                        foreach my $dir_entry (@dir_content) {
                            print "      $directory_to_list/$directory_to_remove/csar/$dir_entry\n";
                            $cmd = "rm -rf $directory_to_list/$directory_to_remove/csar/$dir_entry";
                            `$cmd`;
                        }
                    }
                    @dir_content = send_cmd_read_directory ("$directory_to_list/$directory_to_remove/tools","-v tools");
                    if (scalar @dir_content != 0 ) {
                        print "   Removing (unpacked) files:\n";
                        foreach my $dir_entry (@dir_content) {
                            print "      $directory_to_list/$directory_to_remove/tools/$dir_entry\n";
                            $cmd = "rm -rf $directory_to_list/$directory_to_remove/tools/$dir_entry";
                            `$cmd`;
                        }
                    }
                    last;
                }
            } else {
                $mark_for_removal = 0;
                my $cmd;
                # added check if directory contains unpacked csar, if so remove it
                my @dir_content = send_cmd_read_directory ("$directory_to_list/$directory_to_remove/csar","-v csar");
                if (scalar @dir_content != 0 ) {
                    print "   Removing (unpacked) files:\n";
                    foreach my $dir_entry (@dir_content) {
                        print "      $directory_to_list/$directory_to_remove/csar/$dir_entry\n";
                        $cmd = "rm -rf $directory_to_list/$directory_to_remove/csar/$dir_entry";
                        `$cmd`;
                    }
                }
                @dir_content = send_cmd_read_directory ("$directory_to_list/$directory_to_remove/tools","-v tools");
                if (scalar @dir_content != 0 ) {
                    print "   Removing (unpacked) files:\n";
                    foreach my $dir_entry (@dir_content) {
                        print "      $directory_to_list/$directory_to_remove/tools/$dir_entry\n";
                        $cmd = "rm -rf $directory_to_list/$directory_to_remove/tools/$dir_entry";
                        `$cmd`;
                    }
                }
                last;
            }
        }

        if ( $mark_for_removal == 1 ) {
            print "   Removing $directory_to_list/$directory_to_remove\n";
            my $cmd = "rm -rf $directory_to_list/$directory_to_remove";
            `$cmd`;
            $rc = $?;
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub read_file_to_array {

    my $rc = 0;

    if (open my $handle, '<', "$path_to_file") {
        chomp( @lines_in_file = <$handle> );
        close $handle;

        if (!$lines_in_file[-1]) {
            # last element in array is 'empty', remove it
            pop(@lines_in_file);
        }
        if ( scalar @lines_in_file == 0 ) {
            # No elements in array
            $rc = 1;
        }
    } else {
        print "Failed to read the file: $!\n";
        $rc = 1;
    }

    if ($rc == 0) {
        my $comments    = '#';
        my $dir_marker  = '/';
        my $master      = 'master';
        my $keep_no     = '<keep-on-node=no>';
        my $keep_yes    = '<keep-on-node=yes>';
        @lines_in_file = grep {!/$comments/} @lines_in_file;    # Discard comments
        @lines_in_file = grep {/$dir_marker/} @lines_in_file;   # Filter directories (csar packages are stored within GA-Version directory)
        @lines_in_file = grep {!/$master/} @lines_in_file;      # Discard master directory
        @lines_in_file = grep {/$keep_yes/} @lines_in_file;     # Filter directories to keep only 'node' protected ones (<keep-on-node=yes>)
        # strip <keep-on-node=yes> from remaining entries in the array
        foreach my $entry (@lines_in_file) {
            $entry =~ s/$keep_yes//;
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub send_cmd_read_directory {
    my $dir     = $_[0];
    my $filter  = $_[1];
    my $cmd;
    my $rc = 0;

    $cmd = "ls -F $dir";
    if ($filter) {
        $cmd .= "| grep $filter";
    }

    my @cmd_output = `$cmd`;
    $rc = $?;
    chomp @cmd_output;

    if ( $rc == 1 ) {
        print "Something went wrong trying to list directories\n";
        exit $rc;
    }
    return @cmd_output;
}

# -----------------------------------------------------------------------------
sub show_help {
    print <<EOF;

Description:
============

This script

Syntax:
=======

$0 <MANDATORY> [<OPTIONAL>]

<MANDATORY> are the following parameters:

  -d <full path to directory>    | --directory <full path directory>
  -f <full path to file>         | --file <full path to file>

<OPTIONAL> are one or more of the following parameters:

  -h    | --help

where:

  -d <full path to directory>
  --directory <full path to directory>
  ------------------------------------

  Full path in which directories to remove are located.

  -f <full path to file>
  --file <full path to file>
  --------------------------

  Full path to file which contains list of directories which
  should be ignored.

  -h
  --help
  ------
  Shows this help information.

EOF
}

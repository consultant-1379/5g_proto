#!/usr/bin/perl

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 9.12
#  Date     : 2024-05-24 17:24:45
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2016-2023
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

# Standard Perl Modules
use Data::Dumper;
use File::Find;
use Getopt::Long;
use Text::Tabs;

#
# Variables to change for every release
# =====================================
#
my $default_release_name = "NDP1.13";                           # Needs to be updated for each new release
my $default_release_product_id = "_DAFT_CXP9036965_10_R1A_";    # Needs to be updated for each new release

sub show_help {
    print <<EOF;

Description:
============

This script is used to generate either a DAFT playlist package used for testing purposes
or a full DAFT playlist release package in the form of a tar.gz file with the correct
templates and checksum and last commit info file.

The script copies the needed directory structure to a temporary directory, replaces all
tab characters with 4 spaces and removes white spaces from the wanted files, specified by
file extension.

Syntax:
=======

$0 [<OPTIONAL>] <MANDATORY>

<MANDATORY> are all of the following parameters:

<OPTIONAL> are one or more of the following parameters:

       --delete-playlist-file
  -e | --esc-package
  -g | --git-repository <path>
  -h | --help
  -c | --include-checksums
  -o | --output-directory <path>
  -n | --package-name <name>
  -r | --release-name <name>
  -p | --release-package
  -i | --release-product-id <product identity>
  -m | --rpm-directory <path>
  -t | --timestamp <yyyymmdd_hhmmss>

where:

  --delete-playlist-file
  ----------------------
  Flag to indicate that the generated playlist package file will be deleted after the release file
  has been created. The default is to always keep the generated playlist package file in the output
  directory.
  Only applicable when parameter --release is also given.


  --esc-package
  -e
  -------------
  If given then an ESC package is created to be used for Signaling Controller upgrade or Deployment.
  If not specified then a standard ESM package is created to be used for DSC Diameter Signaling
  Controller Upgrade or Installation.


  --git-repository <path>
  -g <path>
  -----------------------
  Path to git repository which should be used, defaults to the parent directory of the directory
  where this script is located.
  For example if this script is located at: /home/signum/git/dsc_suf/bin/create_daft_package.pl
  then the git repository directory is: /home/signum/git/dsc_suf/


  --help
  -h
  ------
  Show this help text.


  --include-checksums
  -c
  -------------------
  Flag to include the file 'info.log' in the created release package file:
  Only applicable when parameter --release is also given.


  --rpm-directory <path>
  -m <path>
  ----------------------
  Path to directory where the DAFT reboot monitor RPM software is located, defaults to the
  <git-repository path>/esm/rpmbuild/ directory.


  --output-directory <path>
  -o <path>
  -------------------------
  Directory in which the playlist package should be stored, defaults to current working directory.


  --package-name <name>
  -n <name>
  ---------------------
  Name of the DAFT package which should be created, if not specified will be
  generated automatically by the script in the format "DAFT_yyyymmdd_hhmmss".


  --release-name <name>
  -r <name>
  ---------------------
  Release identifier in the form of NDPx.y, if not specified defaults to
  variable \$default_release_name which is currently set to "$default_release_name".
  To create a release package you must also specify the parameter --release-package.


  --release-package
  -p
  -----------------
  To create a release package this parameter is given, the default name of the package
  will be "${default_release_name}${default_release_product_id}.tar.gz" but the name
  can be changed with parameters --release-name and --release-product-id.


  --release-product-id <product identifier>
  -i <product identifier>
  -----------------------------------------
  Release identifier in the form of NDPx.y, if not specified defaults to
  variable \$default_release_product_id which is currently set to "$default_release_product_id".
  To create a release package you must also specify the parameter --release-package.


  --timestamp <yyyymmdd_hhmmss>
  -t <yyyymmdd_hhmmss>
  -----------------------------
  Timestamp to use for for the generated DAFT package and DAFT release package.
  It should be in the yyyymmdd_hhmmss format and if not specified then a value is
  generated.


Examples:
=========

Example 1
---------
Create a DAFT playlist package with default name in the current directory.

$0


Example 2
---------
Create a DAFT playlist package with user specified name in the current directory.

$0 --package-name=DAFT_Test_Package


Example 3
---------
Create a DAFT relase package with default name in the current directory.

$0 --release-package


Example 4
---------
Create a DAFT playlist package with user specified names in the current directory.

$0 --release-package --release-name=NDP1.12 --release-product-id=_DAFT_CXP9036965_9_R1C_


Example 5
---------
Create a DAFT playlist package for 5G ESC node with default name in the current directory.

$0 --release-package --esc-package


Return code:
============

0: Successful package creation
1: Failed package creation

EOF
}


# *************************
# *                       *
# * Variable Declarations *
# *                       *
# *************************

my $script_name = abs_path $0;

my $arguments = 0;
my $create_release_package = 0;
my $debug = 0;
my $delete_playlist_file = 0;
my $esc_package = 0;    # When creating esc package (=1) instead of esm package (=0) which is the default.
my @files;
my $git_repository;
my $include_checksums = 0;
my $package_name = "";
my $output_directory;
my $release_name = "";
my $release_product_id = "";
my $rpm_directory = "";
my $show_help = 0;
my $temp_directory = "/tmp/$ENV{USER}_DAFT_$$";
my $timestamp = generate_timestamp();
my @wanted_extensions = ("sh", "bash", "exp", "inc", "pl", "pm", "txt", "xml", "yaml");
my @wanted_files;


# *****************************
# *                           *
# * Validate Input Parameters *
# *                           *
# *****************************

# Parse command line parameters
GetOptions (
    "c|include-checksums"    => \$include_checksums,
    "d|debug"                => \$debug,          # Not documented parameter
    "e|esc-package"          => \$esc_package,
    "g|git-repository=s"     => \$git_repository,
    "h|help"                 => \$show_help,
    "i|release-product-id=s" => \$release_product_id,
    "n|package-name=s"       => \$package_name,
    "m|rpm-directory=s"      => \$rpm_directory,
    "o|output-directory=s"   => \$output_directory,
    "p|release-package"      => \$create_release_package,
    "r|release-name=s"       => \$release_name,
    "t|timestamp=s"          => \$timestamp,
    "delete-playlist-file"   => \$delete_playlist_file,
);

if ($show_help) {
    show_help();
    exit 0;
}

if (! $output_directory) {
    $output_directory = `pwd`;
    # Strip new line from pwd output.
    $output_directory =~ s/\n//g;
    print "\nPackage will be stored in $output_directory\n";
} else {
    mkdir abs_path($output_directory) unless -d abs_path($output_directory);
    $output_directory = abs_path($output_directory);
}

if (! $git_repository) {
    $git_repository = `dirname \$(dirname $script_name) 2>/dev/null`;
    # Strip new line from find output.
    $git_repository =~ s/\n//g;
    print "\nTrying to use default DAFT GIT repository $git_repository\n";
}
$git_repository = abs_path $git_repository;

if ($esc_package == 0) {
    # We are creating a DSC DAFT package
    if (! $rpm_directory) {
        $rpm_directory = "$git_repository/esm/rpmbuild";
        print "\nTrying to use default RPM directory $rpm_directory\n";
    }
    unless (-d "$rpm_directory") {
        print "\nNo such RPM directory $rpm_directory\n";
        exit 1;
    }
    $rpm_directory = abs_path $rpm_directory;
} else {
    # We are creating a ESC DAFT package
}

if ($create_release_package) {
    if ($release_name) {
        $default_release_name = $release_name;
    } else {
        print "\nRelease name not given, default value '$default_release_name' will be used\n";
    }
    if ($release_product_id) {
        $default_release_product_id = $release_product_id;
    } else {
        print "\nRelease product id not given, default value '$default_release_product_id' will be used\n";
    }

    if ($package_name ne "") {
        # For release packages we should always generate the proper DAFT package name
        print "\nOfficial package name will be used instead of '$package_name'\n";
        $package_name = "";
    }
} else {
    if (! $package_name) {
        print "\nPackage name not given, it will be generated automatically\n";
    }
}

unless ($timestamp =~ /^\d{8}_\d{6}$/) {
    print "\nTimestamp is not in expected 'yyyymmdd_hhmmss' format\n";
    exit 1;
}

# **************
# *            *
# * Main Logic *
# *            *
# **************

# Create either a release package which contains playlist package, templates files,
# and commit and checksum file, or create an playlist package to be used during testing.
if ($create_release_package) {
    create_release_package(
        {
            "debug" => "$debug",
            "output_directory" => "$output_directory",
            "git_repository" => "$git_repository",
            "package_name" => "$package_name",
            "package_type" => $esc_package ? "esc" : "esm",
            "rpm_directory" => "$rpm_directory",
            "temp_directory" => "$temp_directory",
            "timestamp" => "$timestamp"
        }
    );
} else {
    create_playlist_package(
        {
            "debug" => "$debug",
            "output_directory" => "$output_directory",
            "git_repository" => "$git_repository",
            "package_name" => "$package_name",
            "package_type" => $esc_package ? "esc" : "esm",
            "rpm_directory" => "$rpm_directory",
            "temp_directory" => "$temp_directory",
            "timestamp" => "$timestamp"
        }
    );
}

print "\nCreated package stored in " . abs_path($output_directory) . "/$package_name.tar.gz\n";

# Clean up directory used to store temporary files in /tmp/<user id>_DAFT_<process id>.
clean_up(
    {
        "debug" => "$debug",
        "temp_directory" => "$temp_directory"
    }
);

# ****************
# *              *
# * Sub Routines *
# *              *
# ****************


#
# Try to automatically generate the correct revision for the a
# new release package based on existing playlist packages in
# he provided directory. Returns the the digit "1", "2" .. "9",
# "10" etc.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "output_directory" => String, directory in which the package is stored
#        "package_name" => String, name of package to create without revision e.g. DSC_DAFT_Playlist-CXP9036965_7-R1A_
#
# Return values:
#    $revision => String, letter representing the revision that will be used
#
sub calculate_digit_revision {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};
    my @files = ();
    my $hex_string;
    my $package_name;
    my $revision = 1;

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    $package_name = "$arguments{package_name}";

    opendir(DIR, $arguments{output_directory}) or die $!;

    # Try to find existing package in output directory
    while (my $file = readdir(DIR)) {
        # Use a regular expression to ignore files beginning with a period
        if ($file =~ m/$package_name.*\.tar\.gz/) {
            push @files, $file;
        }
    }

    closedir(DIR);

    # Based on found files, calculate next revision to be used
    foreach my $file (@files) {
        if ($file =~ $package_name) {
            $revision++;
        } else {
            last;
        }
    }

    return $revision;
}


#
# Check if provided git repository is actually a git repository.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "git_repository" => String, path to wanted git repository
#
# Return values:
#     0 = True, provided directory is a git repository
#     1 = False, provided directory is not a git repository
#
sub check_git_repository {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    # Check if provided git directory contains the mandatory .git directory
    if ( -d "$arguments{git_repository}/.git" ) {
        print "\nProvided directory $arguments{git_repository} is a GIT repository\n";
        return 0;
    } else {
        print "\nERROR: Provided directory $arguments{git_repository} is NOT a GIT repository\n";
        return 1;
    }
}


#
# Remove temporary directory and other non-essentials.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#
# Return values:
#     0, if debug is on and clean up should be skipped
#
sub clean_up {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
        # Cleanup step skipped if debug is on.
        return 0;
    }

    # Remove created temporary directory
    if (-d "$arguments{temp_directory}") {
        `rm -rf $arguments{temp_directory}`;
        print "\nRemoved temporary directory $arguments{temp_directory}\n";
    }
}


#
# Remove unwanted files and directories from the temp directory.
# The temp directory might contain files that should not be included in the created
# package and this sub routine removes these files and directories.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#
# Return values:
#
#
sub clean_temp_dir_of_unwanted_files {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    if (-d "$arguments{temp_directory}/openstack_env_files") {
        print "\nRemoved unwanted directory $arguments{temp_directory}/openstack_env_files\n";
        `rm -fr "$arguments{temp_directory}/openstack_env_files"`;
    }

    if (-d "$arguments{temp_directory}/network_config_files") {
        print "\nRemoved unwanted directory $arguments{temp_directory}/network_config_files\n";
        `rm -fr "$arguments{temp_directory}/network_config_files"`;
    }

    if (-f "$arguments{temp_directory}/Makefile") {
        print "\nRemoved unwanted file $arguments{temp_directory}/Makefile\n";
        `rm -fr "$arguments{temp_directory}/Makefile"`;
    }

    # Remove VIM swap files
    for (`find $arguments{temp_directory} -name '*.swp'`) {
        s/[\r\n]//g;
        print "\nRemoved unwanted file $_\n";
        `rm -fr "$_"`;
    }
}


#
# Remove tabs and unwanted whitespace from wanted files.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#
# Return values:
#
#
sub clean_wanted_files {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    # Find all files with wanted extensions, see sub find_wanted_files
    find({ wanted => \&find_wanted_files, no_chdir=>1}, $arguments{temp_directory});

    convert_tabs_to_spaces(
        {
            "debug" => "$arguments{debug}",
            "temp_directory" => "$arguments{temp_directory}",
            "wanted_files" => "@wanted_files"
        }
    );

    remove_trailing_white_space(
        {
            "debug" => "$arguments{debug}",
            "temp_directory" => "$arguments{temp_directory}",
            "wanted_files" => "@wanted_files"
        }
    );
}


#
# Convert tabs to 4 spaces in wanted files.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "wanted_files" => Array, contains all files which should be modified
#
# Return values:
#
#
sub convert_tabs_to_spaces {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    # If debug enabled the behavior is changed to keep a copy of the old file
    # before any changes were applied, also prints the line number where match
    # was found along with the line itself.
    if ($arguments{debug}) {
        foreach my $file (@wanted_files) {
            my $line_number = 0;

            open(FH, $file) or die "Can not open file: $!";

            while (<FH>) {
                my $line = $_;

                if ($line =~ /\t/) {
                    print "\nFound TAB in file: $file";
                    print "\nLine Number: $line_number\n";
                    print Data::Dumper::qquote($line);
                    print "\n";
                }

                $line_number++;
            }

            close(FH);
        }

        # Backup old file before making changes
        local($^I, @ARGV) = ('.old.tabs', @wanted_files);

        while (<>) {
            s/\t/    /g;
            print;
            close ARGV if eof;
        }
    } else {
        print "\nReplacing TABs with 4 spaces\n";

        # Make changes to files without backup
        local($^I, @ARGV) = ('', @wanted_files);

        while (<>) {
            s/\t/    /g;
            print;
            close ARGV if eof;
        }
    }
}


#
# Copy needed directory structure from DAFT GIT.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "git_repository" => String, path to wanted git repository
#
# Return values:
#
#
sub copy_directories_from_git {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};
    my $result;

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    # Find the directory which contains the DAFT playlist directory structure
    if (-d "$arguments{git_repository}/daft") {
        $result = `find $arguments{git_repository}/daft -type d -path "*perl" | grep "daft" | sed -n -e 's/perl//p' | grep -v "templates"`;
    } else {
        print "Cannot find the directory path to daft ($arguments{git_repository}/daft)\n";
        exit 1;
    }
    $result =~ s/\n//g;

    print "\nCopying directory structure from $result\n";
    `cp -prL $result/bin $result/expect $result/perl $result/playlist_extensions $result/templates $arguments{temp_directory}`;
    if ($? != 0) {
        print "Failed to copy files into $arguments{temp_directory}\n";
        exit 1;
    }

    print "\nCopying download_csar.pl script into DAFT package\n";
    `cp -pfrL $arguments{git_repository}/scripts/download_csar.pl $arguments{temp_directory}/perl/bin`;
    if ($? != 0) {
        print "Failed to copy download_csar.pl into $arguments{temp_directory}/perl/bin\n";
        exit 1;
    }

    print "\nCopying official httpproxy files into DAFT package\n";
    `mkdir $arguments{temp_directory}/scripts`;
    `cp -pfrL $arguments{git_repository}/scripts/httpproxy $arguments{temp_directory}/scripts`;
    if ($? != 0) {
        print "Failed to copy httpproxy files into $arguments{temp_directory}/scripts\n";
        exit 1;
    }
}


#
# Sub routine used to generate the information file for release which
# contains checksums of all provided files along with the latest commit.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "git_repository" => String, path to wanted git repository
#
# Return values:
#
#
sub create_commit_checksum_file {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    get_commit_id(
        {
            "debug" => "$arguments{debug}",
            "temp_directory" => "$arguments{temp_directory}",
            "git_repository" => "$arguments{git_repository}"
        }
    );

    if ($include_checksums) {
        generate_checksums(
            {
                "debug" => "$arguments{debug}",
                "temp_directory" => "$arguments{temp_directory}"
            }
        );
    }
}


#
# Copy needed directory structure from DAFT GIT.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "git_repository" => String, path to wanted git repository
#
# Return values:
#
#
sub create_daft_rpm {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};
    my $current_directory = `pwd`;
    my $result;
    my $rpm_file;

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    # Change into the directory where the Makefile for the DAFT rpm is located
    unless (chdir $arguments{rpm_directory}) {
        print "\nUnable to change into directory $arguments{rpm_directory}\n";
        exit 1;
    }

    # First cleanup any previous built RPM's
    `make clean`;
    if ($?) {
        print "\nFailed to execute 'make clean'\n";
        chdir $current_directory;
        exit 1;
    }

    # Now create a new DAFT RPM
    print "\nCreating new DAFT RPM file\n";
    `make rpm`;
    if ($?) {
        print "\nFailed to execute 'make clean'\n";
        chdir $current_directory;
        exit 1;
    }
    $rpm_file = `find $arguments{rpm_directory}/rpms -name 'daft-*.x86_64.rpm'`;
    if ($?) {
        print "\nUnable to find generated DAFT RPM\n";
        chdir $current_directory;
        exit 1;
    }
    $rpm_file =~ s/[\r\n]//g;
    unless (-f "$rpm_file") {
        print "\nUnable to find generated DAFT RPM $rpm_file\n";
        chdir $current_directory;
        exit 1;
    }

    print "\nCopying RPM file $rpm_file\n";
    `cp -p $rpm_file $arguments{temp_directory}/bin/`;
    if ($? != 0) {
        print "Failed to copy $rpm_file file into $arguments{temp_directory}/bin\n";
        exit 1;
    }

    # Clean files from current build
    `make clean`;
    if ($?) {
        print "\nFailed to execute 'make clean'\n";
    }

    chdir $current_directory;
}


#
# Store generated play list package in users home directory (default),
# or store in defined directory.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#
# Return values:
#
#
sub create_temp_directory {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};
    my $result;

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    # Check if a temporary directory already exists, if not try to create the directory
    if (-d "$arguments{temp_directory}") {
        print "\nDirectory already exists in $arguments{temp_directory}\n";
    } else {
        $result = `mkdir -p $arguments{temp_directory} | echo $?`;
        if ($result == 0) {
            print "\nCreated temporary directory in directory $arguments{temp_directory}\n";
        } else {
            print "\nERROR: Could not create directory in provided directory $arguments{temp_directory}\n";
            exit 1;
        }
    }
}


#
# Sub routine used to create the DAFT playlist package.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "git_repository" => String, path to wanted git repository
#        "output_directory" => String, directory in which the package is stored
#        "package_name" => String, name of the package that wil be created
#        "timestamp" => String, timestamp in YYYYMMDD_HHMMSS format
#
# Return values:
#    $package_anme => String, name of the package neded if further steps are taken
#
sub create_playlist_package {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    my $is_git_repository = check_git_repository(
        {
            "debug" => "$arguments{debug}",
            "git_repository" => "$arguments{git_repository}"
        }
    );

    if ($is_git_repository == 0) {
        create_temp_directory(
            {
                "debug" => "$arguments{debug}",
                "temp_directory" => "$arguments{temp_directory}"
            }
        );

        copy_directories_from_git(
            {
                "debug" => "$arguments{debug}",
                "git_repository" => "$arguments{git_repository}",
                "package_type" => "$arguments{package_type}",
                "temp_directory" => "$arguments{temp_directory}"
            }
        );

        if ($arguments{package_type} eq "esm") {
            create_daft_rpm(
                {
                    "debug" => "$arguments{debug}",
                    "git_repository" => "$arguments{git_repository}",
                    "rpm_directory" => "$arguments{rpm_directory}",
                    "temp_directory" => "$arguments{temp_directory}"
                }
            );
        }

        clean_temp_dir_of_unwanted_files(
            {
                "debug" => "$arguments{debug}",
                "temp_directory" => "$arguments{temp_directory}"
            }
        );

        clean_wanted_files(
            {
                "debug" => "$arguments{debug}",
                "temp_directory" => "$arguments{temp_directory}"
            }
        );

        if ($arguments{package_name} eq "") {
            $package_name = generate_package_name(
                {
                    "debug" => "$arguments{debug}",
                    "git_repository" => "$arguments{git_repository}",
                    "output_directory" => "$arguments{output_directory}",
                    "temp_directory" => "$arguments{temp_directory}",
                    "timestamp" => "$arguments{timestamp}"
                }
            );
        }

        create_playlist_tar_ball(
            {
                "debug" => "$arguments{debug}",
                "output_directory" => "$arguments{output_directory}",
                "package_name" => "$package_name",
                "temp_directory" => "$arguments{temp_directory}"
            }
        );
    } else {
        exit 1;
    }

    return $package_name;
}


#
# Create DAFT playlist tar ball.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "output_directory" => String, directory in which the package is stored
#        "package_name" => String, name of the package that wil be created
#
# Return values:
#
#
sub create_playlist_tar_ball {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    `printf "daft_git_branch=%s\n" \$(git branch --show-current) >> $arguments{temp_directory}/package.config`;
    `printf "daft_git_commit=%s\n" \$(git rev-parse HEAD) >> $arguments{temp_directory}/package.config`;
    my @git_status = `git status -s`;
    if (@git_status) {
        `echo "daft_git_status=dirty" >> $arguments{temp_directory}/package.config`;
        for (@git_status) {
            `echo -n "# $_" >> $arguments{temp_directory}/package.config`;
        }
    } else {
        `echo "daft_git_status=clean" >> $arguments{temp_directory}/package.config`;
    }
    `echo "daft_original_package_name=$arguments{package_name}" >> $arguments{temp_directory}/package.config`;
    `printf "# The latest 5 commits for DAFT\n" >> $arguments{temp_directory}/package.config`;
    `git log --max-count=5 . >> $arguments{temp_directory}/package.config`;

    my $current_directory = Cwd::getcwd();
    chdir($arguments{temp_directory});
    `tar --format=gnu -chvzf "$arguments{output_directory}"/"$arguments{package_name}".tar.gz *`;
    if ($? != 0) {
        print "Failed to create the $arguments{output_directory}/$arguments{package_name}.tar.gz package file\n";
        exit 1;
    }
    chdir($current_directory);
}



#
# Pack into releases tar.gz (included BSP_cofig.yaml, network_configuration_template_laptop, network_configuration_template_remote).
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "git_repository" => String, path to wanted git repository
#        "output_directory" => String, directory in which the package is stored
#        "package_name" => String, name of the package that wil be created
#        "timestamp" => String, timestamp in YYYYMMDD_HHMMSS format
#
# Return values:
#
#
sub create_release_package {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    my $package_name = create_playlist_package(
        {
            "debug" => "$debug",
            "output_directory" => "$output_directory",
            "git_repository" => "$git_repository",
            "package_name" => "$package_name",
            "package_type" => $esc_package ? "esc" : "esm",
            "rpm_directory" => "$rpm_directory",
            "temp_directory" => "$temp_directory",
            "timestamp" => "$arguments{timestamp}"
        }
    );

    create_commit_checksum_file(
        {
            "debug" => "$arguments{debug}",
            "git_repository" => "$arguments{git_repository}",
            "temp_directory" => "$arguments{temp_directory}"
        }
    );

    create_release_tar_ball(
        {
            "debug" => "$arguments{debug}",
            "output_directory" => "$arguments{output_directory}",
            "package_name" => "$package_name",
            "package_type" => $esc_package ? "esc" : "esm",
            "temp_directory" => "$arguments{temp_directory}",
            "timestamp" => "$arguments{timestamp}"
        }
    );
}


#
# Create DAFT playlist release tar ball.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "output_directory" => String, directory in which the package is stored
#        "package_name" => String, name of the package that wil be created
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "timestamp" => String, timestamp in YYYYMMDD_HHMMSS format
#
# Return values:
#
#
sub create_release_tar_ball {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};
    my $daft_cleanup_script = "daft_cleanup.bash";
    my $daft_deployment_script = "daft_deployment.bash";
    my $dsc_template_generic = "dsc-template-generic.yaml";
    my $dsc_template_standard = "dsc-template-standard.yaml";
    my $esc_template_file = "Network_Config_Template.xml";
    my $laptop_template_file = "Network_Config_Template_Laptop.xml";
    my $output_file = "";
    my $release_info_log = "info.log";
    my $revision = "";
    my $remote_template_file = "Network_Config_Template_Remote.xml";
    my $vdsc_template_file = "Network_Config_Template_vDSC.xml";

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    print "\nCreating release tar ball in directory $arguments{output_directory}\n";

    if ($delete_playlist_file) {
        `mv "$arguments{output_directory}/$arguments{package_name}.tar.gz" $arguments{temp_directory}`;
        if ($? != 0) {
            print "Failed to move file $arguments{output_directory}/$arguments{package_name}.tar.gz into $arguments{temp_directory}\n";
            exit 1;
        }
    } else {
        `cp -p "$arguments{output_directory}/$arguments{package_name}.tar.gz" $arguments{temp_directory}`;
        if ($? != 0) {
            print "Failed to copy file $arguments{output_directory}/$arguments{package_name}.tar.gz into $arguments{temp_directory}\n";
            exit 1;
        }
        print "\nCreated DAFT package stored in \"$arguments{output_directory}/$arguments{package_name}.tar.gz\"\n";
    }

    if ($arguments{package_type} eq "esm") {
        my $error_cnt = 0;
        `cp -p $arguments{temp_directory}/templates/$laptop_template_file $arguments{temp_directory}`;
        $error_cnt++ if ($? != 0);
        `cp -p $arguments{temp_directory}/templates/$remote_template_file $arguments{temp_directory}`;
        $error_cnt++ if ($? != 0);
        `cp -p $arguments{temp_directory}/templates/$vdsc_template_file $arguments{temp_directory}`;
        $error_cnt++ if ($? != 0);
        `cp -p $arguments{temp_directory}/templates/$dsc_template_generic $arguments{temp_directory}`;
        $error_cnt++ if ($? != 0);
        `cp -p $arguments{temp_directory}/templates/$dsc_template_standard $arguments{temp_directory}`;
        $error_cnt++ if ($? != 0);
        `cp -p $arguments{temp_directory}/bin/$daft_cleanup_script $arguments{temp_directory}`;
        $error_cnt++ if ($? != 0);
        `cp -p $arguments{temp_directory}/bin/$daft_deployment_script $arguments{temp_directory}`;
        $error_cnt++ if ($? != 0);
        if ($error_cnt != 0) {
            print "Failed to copy files into $arguments{temp_directory} directory\n";
            exit 1;
        }
    }

    if (exists $arguments{timestamp}) {
        $revision = $arguments{timestamp};
    } else {
        $revision = calculate_digit_revision(
            {
                "debug" => "$arguments{debug}",
                "output_directory" => "$arguments{output_directory}",
                "package_name" => "$default_release_name$default_release_product_id",
            }
        );
    }

    # Add digit revision
    $output_file = "$default_release_name$default_release_product_id" . "$revision" . ".tar.gz";

    my $current_directory = Cwd::getcwd();
    chdir($arguments{temp_directory});
    if ($arguments{package_type} eq "esm") {
        `tar --format=gnu -chvzf "$arguments{output_directory}"/$output_file "$arguments{package_name}".tar.gz $release_info_log $daft_cleanup_script $daft_deployment_script`;
    } else {
        # ESC package
        `tar --format=gnu -chvzf "$arguments{output_directory}"/$output_file "$arguments{package_name}".tar.gz $release_info_log`;
    }
    if ($? != 0) {
        print "Failed to create package file $arguments{output_directory}/$output_file\n";
        exit 1;
    }
    chdir($current_directory);

    $package_name = "$default_release_name$default_release_product_id$revision";
}


#
# Using the Find::File module, generate a list of files
# located inside provided directory.
#
# Input variables:
#
# Return values:
#
#
sub find_all_files {
    my $complete_file_pathname = $File::Find::name;

    push @files, $complete_file_pathname;
}


#
# Using the Find::File module, generate a list of files
# that match the wanted extensions.
#
# Input variables:
#
# Return values:
#
#
sub find_wanted_files {
    my $complete_file_pathname = $File::Find::name;

    foreach my $extension (@wanted_extensions) {
        if ($complete_file_pathname =~ /\.$extension$/ ) {
            if ($debug) {
                print "\nExtension Used: $extension\nMatched File: $complete_file_pathname\n";
            }
            push @wanted_files, $complete_file_pathname;
        }
    }
}


#
# Generate Checksum for each file before packing.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#
# Return values:
#
#
sub generate_checksums {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    print "\nGenerating checksums\n";

    # Find all files with provided directory, see sub find_all_files
    find({ wanted => \&find_all_files, no_chdir=>1}, $arguments{temp_directory});

    # Generate md5 checksum of each file and store it into info.log
    foreach my $file (@files) {
        if ($arguments{debug}) {
            print "\n$file\n" if -f $file;
        }

        `md5sum $file >> $arguments{temp_directory}/info.log` if -f $file;
    }
}


#
# Generate the playlist package name according to naming convention.
# Try to automatically find the correct revision based on existing playlist
# packages in the provided directory.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "git_repository" => String, path to wanted git repository
#        "output_directory" => String, directory in which the package is stored
#        "timestamp" => String, timestamp in YYYYMMDD_HHMMSS format
#
# Return values:
#     $package_name
#
sub generate_package_name {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};
    my $revision;

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    $package_name = "DAFT" . "_" . $arguments{timestamp};

    return $package_name;
}


#
# Generate a timestamp that can be used for the two package files.
#
# Input variables:
#    -
#
# Return values:
#     $date in YYYYMMDD_HHMMSS format.
#
sub generate_timestamp {
    my $date;

    $date = `date +%Y%m%d_%H%M%S`;
    $date =~ s/[\r\n]//g;

    return $date;
}


#
# Generate Commit ID to keep track of release.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "git_repository" => String, path to wanted git repository
#        "temp_directory" => String, path to directory which contains copied DAFT files
#
# Return values:
#
#
sub get_commit_id {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    print "\nGetting commit id from directory $arguments{git_repository}\n";

    # Get the last commit on current branch, cleanup the print to only show commit ID and date will be stored in info.log
    `git --git-dir=$arguments{git_repository}/.git log -1 --pretty=format:"Commit: %H%nDate: %cd%n" >> $arguments{temp_directory}/info.log`
}


#
# Generate release name based on the current GIT branch or $default_release_name if 'master'.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "git_repository" => String, path to wanted git repository
#        "temp_directory" => String, path to directory which contains copied DAFT files
#
# Return values:
#
#
sub get_release_name {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};
    my $release;

    # Get the name of the current working branch, if master will be set to $default_release_name
    # this will work as long as we continue to use branch naming based on release
    $release = `git --git-dir=$arguments{git_repository}/.git rev-parse --abbrev-ref HEAD`;
    $release =~ s/\n//g;

    if ($release =~ "master") {
        $release = $default_release_name;
    }

    return $release;
}


#
# Remove trailing whitespace.
#
# Input variables:
#    Anonymous hash with expected key pair values of
#        "debug" => Integer, 1 or 0
#        "temp_directory" => String, path to directory which contains copied DAFT files
#        "wanted_files" => Array, contains all files which should be modified
#
# Return values:
#
#
sub remove_trailing_white_space {
    # Dereference provided hash of arguments
    my %arguments = %{$_[0]};

    # Debug to print all received key value pairs in anonymous hash and
    # show the executing subroutine and the caller of this subroutine
    if ($arguments{debug}) {
        print "\nDEBUG subroutine ",(caller 0)[3],":\n";
        print "The calling subroutine is ",(caller 1)[3],"\n";
        for my $argument (keys %arguments) {
            print "'$argument' is $arguments{$argument}\n";
        }
    }

    # If debug enabled the behavior is changed to keep a copy of the old file
    # before any changes were applied, also prints the line number where match
    # was found along with the line itself.
    if ($arguments{debug}) {
        foreach my $file (@wanted_files) {
            my $line_number = 1;

            open(FH, $file) or die "Can not open file: $!";

            while (<FH>) {
                my $line = $_;

                if ($line =~ /\s+\n$/) {
                    print "\nFound TRAILING SPACES in file: $file";
                    print "\nLine Number: $line_number\n";
                    print Data::Dumper::qquote($line);
                    print "\n";
                }

                $line_number++;
            }

            close(FH);
        }

        # Backup old file before making changes
        local($^I, @ARGV) = ('.old.spaces', @wanted_files);

        while (<>) {
            s/\s+\n$/\n/g;
            print;
            close ARGV if eof;
        }
    } else {
        print "\nReplacing white spaces before EOL with single newline\n";

        # Make changes to files without backup
        local($^I, @ARGV) = ('', @wanted_files);

        while (<>) {
            s/\s+\n$/\n/g;
            print;
            close ARGV if eof;
        }
    }
}

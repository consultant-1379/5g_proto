#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.3
#  Date     : 2024-02-16 10:00:01
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2023-2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

# Used Libraries
# --------------
use strict;

use File::Basename qw(dirname basename);
use Cwd qw(abs_path cwd);
use lib dirname(dirname abs_path $0) . '/lib';

# Used Variables
# --------------
my $also_update_release = 0;
my $build = "";
my $command;
my @curl_commands = ();
my $delete_files = 0;
my $directory_path = "";
my $dry_run = 0;
my $error_cnt = 0;
my $full_artifactory_path = "";
my @ignore_files = (
    "templates\/base_stability_traffic\/traffic_config\/vtap\/pvtb_vtap_DT_Template.netconf",
    "templates\/base_stability_traffic\/traffic_config\/vtap\/scp_vtap_DT_Template.netconf",
    "templates\/base_stability_traffic\/traffic_config\/vtap\/sepp_vtap_DT_Template.netconf",
);
my @files;
my $package_dir = dirname(dirname(dirname abs_path $0));
my $release = "";
my $show_help = 0;
my $token = "AKCp8kr1CdTKYVJc9EtxyN3Sbz8YiNzRgFPzbsTmrjRsWeVFKnReVv6kMSYoBcfHpAGgEBQGV";    # Belongs to eiffelesc user
my $url_base_development = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc";
my $url_base_release = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/GA-Versions";

# Parse command line parameters
# -----------------------------
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|also-update-release"     => \$also_update_release,
    "b|build=s"                 => \$build,
    "delete-files"              => \$delete_files,
    "d|directory-path=s"        => \$directory_path,
    "f|full-artifactory-path=s" => \$full_artifactory_path,
    "h|help"                    => \$show_help,
    "n|dry-run"                 => \$dry_run,
    "r|url-base-release=s"      => \$url_base_release,
    "t|token=s"                 => \$token,
    "u|url-base-development=s"  => \$url_base_development,
);

if ($show_help) {
    show_help();
    exit 0;
}

if ($full_artifactory_path ne "") {
    # Nothing more need to be checked, we use this url as-is
} elsif ($build eq "") {
    print "Error: You must specify a build number e.g. --build=1.12.0+43\n";
    exit 1;
} elsif ($build =~ /^(\d+\.\d+\.(\d+))[+]\d+$/) {
    if ($2 == 25) {
        # Unreleased software
        $release = "master";
    } else {
        $release = "SC$1";
    }
} else {
    print "Error: Unexpected build number format ($build), expected e.g. --build=1.12.0+43\n";
    exit 1;
}

if ($directory_path eq "") {
    $directory_path = "$package_dir/templates/base_stability_traffic";
} else {
    $directory_path =~ s/\/$//;
    if ($directory_path =~ /^.+\/base_stability_traffic/) {
        if (-d $directory_path) {
            $directory_path = abs_path $directory_path;
        } else {
            print "Error: The --directory-path directory $directory_path does not exist\n";
            exit 1;
        }
    } elsif (-d "$directory_path/base_stability_traffic") {
        $directory_path = abs_path "$directory_path/base_stability_traffic";
    } else {
        print "Error: The --directory-path directory '$directory_path' does not not contain the 'base_stability_traffic' sub-directory\n";
        exit 1;
    }
}

# Main Program
# ------------

if ($delete_files == 0) {
    # Find the files and symbolic links that should be uploaded
    $command = "find $directory_path \\( -type f -o -type l \\)";
    print "\n$command\n";
    @files = `$command`;
    print @files;
    if ($? != 0) {
        print "Error: Unable to find the base_stability_traffic files\n";
        exit 1;
    }

    # Go through all files and create the needed 'curl' commands
    print "\nGenerating curl commands for found files\n";
    for my $filepath (@files) {
        $filepath =~ s/[\r\n]//g;   # Remove CR LF characters
        if ($filepath =~ /\.sha256$/) {
            print "File is ignored: $filepath\n";
            next;
        }
        my $ignore = 0;
        for my $ignore_path (@ignore_files) {
            if ($filepath =~ /$ignore_path$/) {
                $ignore = 1;
                last;
            }
        }
        if ($ignore == 1) {
            print "File in ignore list: $filepath\n";
            next;
        }

        if ($filepath =~ /^.+\/(base_stability_traffic\/.+)/) {
            if ($full_artifactory_path ne "") {
                push @curl_commands, "curl -H \"X-JFrog-Art-Api:$token\" -f -k --upload-file $filepath $full_artifactory_path/$1";
            } else {
                push @curl_commands, "curl -H \"X-JFrog-Art-Api:$token\" -f -k --upload-file $filepath $url_base_development/$release/$build/$1";
                if ($also_update_release == 1 && $release ne "master") {
                    push @curl_commands, "curl -H \"X-JFrog-Art-Api:$token\" -f -k --upload-file $filepath $url_base_release/$release/$build/$1";
                }
            }
        } else {
            print "Warning: Unexpected file path, file ignored: $filepath\n";
        }
    }
} else {
    # Delete all files under base_stability_traffic path on artifactor for specified build number
    print "\nGenerating curl command for deleting files from artifactory\n";
    if ($full_artifactory_path ne "") {
        push @curl_commands, "curl -H \"X-JFrog-Art-Api:$token\" -f -k -X DELETE $full_artifactory_path/base_stability_traffic";
    } else {
        push @curl_commands, "curl -H \"X-JFrog-Art-Api:$token\" -f -k -X DELETE $url_base_development/$release/$build/base_stability_traffic";
        if ($also_update_release == 1 && $release ne "master") {
            push @curl_commands, "curl -H \"X-JFrog-Art-Api:$token\" -f -k -X DELETE $url_base_release/$release/$build/base_stability_traffic";
        }
    }
}

if ($dry_run == 0) {
    if ($delete_files == 0) {
        print "\nUploading files to artifactory:\n";
    } else {
        print "\nDeleting files from artifactory:\n";
    }
} else {
    if ($delete_files == 0) {
        print "\n--dry-run specified, here are the curl commands that will upload the files to artifactory:\n";
    } else {
        print "\n--dry-run specified, here are the curl commands that will delete the files from artifactory:\n";
    }
}

for $command (@curl_commands) {
    print "\n$command\n";
    if ($dry_run == 0) {
        my @result = `$command`;
        print @result;
        if ($? != 0) {
            print "Error: Failed to execute the command\n";
            $error_cnt++;
        }
    }
}

if ($error_cnt == 0) {
    if ($dry_run == 0) {
        if ($delete_files == 0) {
            print "\nSuccess: All files were uploaded\n";
        } else {
            print "\nSuccess: All files were deleted\n";
        }
    }
    exit 0;
} else {
    if ($delete_files == 0) {
        print "\nFailure: $error_cnt files was not uploaded\n";
    } else {
        print "\nFailure: $error_cnt files was not deleted\n";
    }
    exit 1;
}

# ***************
# * Subroutines *
# ***************

# -----------------------------------------------------------------------------
sub show_help {
        print <<EOF;

Description:
============

This script will upload base stability traffic files to artifactory or delete
the files from artifactory.

It will manipulate files under the following URLs:

$url_base_development/<release>/<build>/base_stability_traffic/
  and
$url_base_release/<release>/<build>/base_stability_traffic/

***********
* WARNING *
***********

These changes cannot be undone!


Syntax:
=======

$0 [<OPTIONAL>] <MANDATORY>

<MANDATORY> are one of the following parameters:

  -b <build number>     | --build <build number>
     or
  -f <url>              | --full-artifactory-path <url>

<OPTIONAL> are one or more of the following parameters:

  -a                    | --also-update-release
  -d <directory>        | --directory-path <directory>
  -h                    | --help
                        | --delete-files
  -n                    | --dry-run
  -r <url>              | --url-base-release <url>
  -t <string>           | --token <string>
  -u <url>              | --url-base-development <url>

where:

  -a
  --also-update-release
  ---------------------
  If specified then also the official GA-release package will updated.
  By default will only the development package be updated.


  -b <build number>
  --build <build number>
  ----------------------
  Specifies the build number of the artifactory package to update.
  This should be a string like: 1.12.0+43


  --delete-files
  --------------
  If specified then all base_stability_traffic files for the specified
  build will be deleted from artifactory.


  -d <directory>
  --directory-path <directory>
  ----------------------------
  If specified then the <directory> should point to the top-level directory
  that contains the 'base_stability_traffic' directory that contains all
  files that should be uploaded to artifactory.
  This parameter should be specified if the files should be taken from a
  different directory than where the GIT repository of the script is running
  from.
  If not specified then the files are taken from the following directory path:
  $package_dir/templates/base_stability_traffic


  -f <url>
  --full-artifactory-path <url>
  -----------------------------
  If specified, you can give the full path to the artifactory link to be
  used for uploading the base_stability_traffic files
  Under this path will the base_stability_traffic directory and files be created.

  For example:
  https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/master/1.14.25+468
  https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.13.0/1.13.0+4
  https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/GA-Versions/SC1.13.0/1.13.0+4
  https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/rebels_dnd50188/1.14.25+678

  Under this path will the base_stability_traffic/ directory structure be created.

  NOTE: This parameter is mutually exclusive for -b/--build, -r/--url-base-release
  and -u/--url-base-development parameters.


  -h
  --help
  ------
  Shows this help information.


  -n
  --dry-run
  ---------
  If specified then nothing will be uploaded, instead only the commands
  to upload the files will be shown.


  -r <url>
  --url-base-release <url>
  ------------------------
  If specified, you can change the url to be used for for uploading files
  to the official GA-release packages.
  If not specified then the following default value will be used:

  $url_base_release

  After this base url will the following be added: /<release>/<build>/<filepath>
  For example:
  $url_base_release/SC1.12.0/1.12.0+43/base_stability_traffic/....


  -t <string>
  --token <string>
  ----------------
  The token to be used for logging into artifactory.
  If not specified then a default token will be used.


  -u <url>
  --url-base-development <url>
  ----------------------------
  If specified, you can change the url to be used for for uploading files
  to the development release packages.
  If not specified then the following default value will be used:

  $url_base_development

  After this base url will the following be added: /<release>/<build>/<filepath>
  For example:
  $url_base_development/SC1.12.0/1.12.0+43/base_stability_traffic/....
    or for current not released version (ending with .25):
  $url_base_development/master/1.13.25+1033/base_stability_traffic/....
EOF
}

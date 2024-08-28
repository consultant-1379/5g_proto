#!/usr/bin/perl

################################################################################
#
#  Author   : everhel
#
#  Revision : 1.1
#  Date     : 2021-11-23 12:28:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2021
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

use Data::Dumper qw(Dumper);

use File::Basename qw(dirname);
use Cwd qw(abs_path);
use lib dirname(dirname abs_path $0) . '/lib';
use version;

my $daft_directory = "";
my $dry_run = 0;
my $extra_variables_certificates = "";
my $extra_variables_configuration = "";
my $extra_variables_deploy = "";
my $extra_variables_healthcheck = "";
my $extra_variables_upgrade = "";
my $extra_variables_user_provisioning = "";
my $extra_variables_evnfm_deployment = "";
my $healthcheck_type = "";
my $network_config_file = "";
my $rc = 0;
my $run_certificates = 0;
my $run_configuration = 0;
my $run_deploy = 0;
my $run_evnfm_deployment = 0;
my $run_healthcheck = 0;
my $run_upgrade = 0;
my $run_user_provisioning = 0;
my $show_help = 0;
my $skip_extract_daft = 0;
my $software_directory = "";
my $workspace = "";

my @messages = ();

my %playlists;

# **************
# *            *
# * Main logic *
# *            *
# **************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|healthcheck"                             => \$run_healthcheck,
    "c|load_configuration"                      => \$run_configuration,
    "d|deploy"                                  => \$run_deploy,
    "e|network_config_file=s"                   => \$network_config_file,
    "f|daft_directory=s"                        => \$daft_directory,
    "h|help"                                    => \$show_help,
    "l|load_certificates"                       => \$run_certificates,
    "p|user_provisioning"                       => \$run_user_provisioning,
    "s|software_directory=s"                    => \$software_directory,
    "u|upgrade"                                 => \$run_upgrade,
    "v|evnfm_deployment"                        => \$run_evnfm_deployment,
    "va|extra_variables_healthcheck=s"          => \$extra_variables_healthcheck,
    "vc|extra_variables_configuration=s"        => \$extra_variables_configuration,
    "vd|extra_variables_deploy=s"               => \$extra_variables_deploy,
    "ve|extra_variables_certificates=s"         => \$extra_variables_certificates,
    "vp|extra_variables_user_provisioning=s"    => \$extra_variables_user_provisioning,
    "vu|extra_variables_upgrade=s"              => \$extra_variables_upgrade,
    "vv|extra_variables_evnfm_deployment=s"     => \$extra_variables_evnfm_deployment,
    "w|workspace=s",                            => \$workspace,
    "y|dry_run"                                 => \$dry_run,
);

if ($show_help == 1) {
    show_help();
    exit 0
}

if ($daft_directory eq "") {
    $daft_directory = "/home/eccd/daft/";
    $skip_extract_daft = 1;
}

if ($software_directory eq "") {
    my $software_regex = "[0-9]*\.[0-9]*\.[0-9]*\+[0-9]*";
    my $default_software_directory = "/home/eccd/download/";
    $software_directory = find_latest_directory($default_software_directory, "$software_regex");
}

if ($workspace eq "") {
    $workspace = "/home/eccd/workspaces";
}

if ($run_healthcheck == 1) {
    print "Healthcheck selected, please specify pre or post: ";
    $healthcheck_type = <STDIN>;
    chomp $healthcheck_type;
}

update_hash(
    $software_directory,
    $network_config_file,
    $workspace,
    $extra_variables_certificates,
    $extra_variables_configuration,
    $extra_variables_deploy,
    $extra_variables_healthcheck,
    $extra_variables_upgrade,
    $extra_variables_user_provisioning,
    $extra_variables_evnfm_deployment,
);

if ($skip_extract_daft == 0) {
    $rc = extract_framework($daft_directory);
    if ($rc == 0) {
        $daft_directory .= "/tools/";
    }
}

if ($network_config_file eq "") {
    @messages = ();
    push @messages, "No network configuration file defined";
    push @messages, "Execution stopped";
    print_message("@messages");
    exit 1;
}

if ($rc == 0) {
    $rc = run_playlists();
    exit $rc;
} else {
    exit 1;
}

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# Check for running daft process
sub check_for_running_daft_processes {
    my $execute_playlist_running = "";
    my @messages = ();
    my $rc = 0;

    `ps -aef | grep perl | grep -c execute_playlist.pl`;
    $execute_playlist_running = $?;
    $execute_playlist_running =~ s/[\r\n]+$//;

    if ( $execute_playlist_running != 0 ) {
        push @messages, "Found that execute_playlist.pl is already running";
        print_message("@messages");
        $rc = 1;
    }

    return $rc;
}

sub execute_system_cmd {
    my $cmd = shift;

    open(OUTPUT, "$cmd|") or die "Failed to create process: $!\n";

    while (<OUTPUT>) {
        print;
    }

    close(OUTPUT);

    $rc = $?;

    return $rc;
}

# Extract DAFT
sub extract_framework {
    my $directory_to_use = shift;

    my $command = "";
    my $daft_regex = "DAFT*.tar.gz";
    my @messages = ();
    my $rc = 0;

    my $latest_daft_package = find_latest_package($directory_to_use, "$daft_regex");

    if ($latest_daft_package eq "") {
        @messages = ();
        push @messages, "No DAFT package found in $directory_to_use";
        push @messages, "Execution stopped";
        print_message("@messages");

        $rc = 1
    } else {
        $daft_directory = "$directory_to_use";

        if (! -d "$daft_directory") {
            @messages = ();
            push @messages, "Creating $daft_directory";
            print_message("@messages");

            `mkdir -m 755 -p $daft_directory`;
        } else {
            @messages = ();
            push @messages, "$daft_directory already exists";
            print_message("@messages");
        }

        if (! -d "$directory_to_use/network_config_files") {
            @messages = ();
            push @messages, "Creating $directory_to_use/network_config_files";
            print_message("@messages");

            `mkdir -m 755 -p $directory_to_use/network_config_files`;
        } else {
            @messages = ();
            push @messages, "$directory_to_use/network_config_files already exists";
            print_message("@messages");
        }

        if (! -d "$directory_to_use/workspaces") {
            @messages = ();
            push @messages, ("Creating $directory_to_use/workspaces");
            print_message("@messages");

            `mkdir -m 755 -p $directory_to_use/workspaces`;
        } else {
            @messages = ();
            push @messages, "$directory_to_use/workspaces already exists";
            print_message("@messages");
        }

        if (! -d "$directory_to_use/tools") {
            @messages = ();
            push @messages, "Creating $directory_to_use/tools";
            print_message("@messages");

            `mkdir -m 755 -p $directory_to_use/tools`;
        } else {
            @messages = ();
            push @messages, "$directory_to_use/tools already exists";
            print_message("@messages");
        }

        $command = "tar -xvf $latest_daft_package -C $directory_to_use/tools";
        $rc = execute_system_cmd($command);

        if ($rc == 0) {
            $command = "cp $directory_to_use/tools/templates/Network_Config_Template_New.xml $directory_to_use/network_config_files/Network_Config_Template.xml";
            $rc = execute_system_cmd($command);
        }
    }

    return $rc;
}

# Find latest package
sub find_latest_package {
    my $directory_to_use = shift;
    my $latest_daft_package = "";
    my $regex = shift;

    my @found_packages = `find $directory_to_use -maxdepth 1 -name "$regex" -type f -print`;
    my @sorted_packages = sort @found_packages;
    $latest_daft_package = $sorted_packages[-1];
    $latest_daft_package =~ s/[\r\n]+$//;

    return $latest_daft_package;
}

# Find latest package
sub find_latest_directory {
    my $directory_to_use = shift;
    my $latest_daft_package = "";
    my $regex = shift;

    my @found_packages = `find $directory_to_use -maxdepth 1 -name "$regex" -type d -print`;
    my @sorted_packages = sort @found_packages;
    $latest_daft_package = $sorted_packages[-1];
    $latest_daft_package =~ s/[\r\n]+$//;

    return $latest_daft_package;
}

sub print_message {
    my $line = "";
    my @messages = shift;

    foreach $line (@messages) {
        print "$line\n";
    }
}

sub run_playlists {

    my $certificates_rc = 0;
    my $configuration_rc = 0;
    my $daft_running = 0;
    my $deploy_rc = 0;
    my $job_name = "";
    my $execute_playlist_cmd = "";
    my $rc = 0;
    my $timestamp = "";
    my $upgrade_rc = 0;
    my $user_provisioning_rc = 0;

    my @messages = ();

    $daft_running = check_for_running_daft_processes();

    if ($daft_running == 0) {
        #print Dumper \%playlists;

        if ($playlists{"deploy"}{run} == 1 && $rc == 0) {
            $timestamp = `date +%s`;
            $timestamp =~ s/[\r\n]+$//;
            $job_name = "deploy_sc_$timestamp";
            $execute_playlist_cmd = "perl $daft_directory/perl/bin/execute_playlist.pl $playlists{deploy}{base_cmds} -j $job_name";

            if ($playlists{"deploy"}{extra_cmds}) {
                $execute_playlist_cmd .= " $playlists{deploy}{extra_cmds}";
            }

            if ($dry_run == 1) {
                $execute_playlist_cmd .= " -v DRY_RUN=yes";
            }

            $rc = execute_system_cmd($execute_playlist_cmd);
            $deploy_rc = $rc;

            if ($rc == 1) {
                @messages = ();
                push @messages, ("Deployment of SC failed\n");
                print_message("@messages");
            }
        }

        if ($playlists{"evnfm_deployment"}{run} == 1 && $rc == 0) {
            $timestamp = `date +%s`;
            $timestamp =~ s/[\r\n]+$//;
            $job_name = "deploy_sc_evnfm_$timestamp";
            $execute_playlist_cmd = "perl $daft_directory/perl/bin/execute_playlist.pl $playlists{evnfm_deployment}{base_cmds} -j $job_name";

            if ($playlists{"evnfm_deployment"}{extra_cmds}) {
                $execute_playlist_cmd .= " $playlists{evnfm_deployment}{extra_cmds}";
            }

            if ($dry_run == 1) {
                $execute_playlist_cmd .= " -v DRY_RUN=yes";
            }

            $rc = execute_system_cmd($execute_playlist_cmd);
            $deploy_rc = $rc;

            if ($rc == 1) {
                @messages = ();
                push @messages, ("Deployment of SC with EVNFM failed\n");
                print_message("@messages");
            }
        }

        if ($playlists{"user_provisioning"}{run} == 1 && $rc == 0) {
            $timestamp = `date +%s`;
            $timestamp =~ s/[\r\n]+$//;
            $job_name = "user_provisioning_sc_$timestamp";
            $execute_playlist_cmd = "perl $daft_directory/perl/bin/execute_playlist.pl $playlists{user_provisioning}{base_cmds} -j $job_name";

            if ($playlists{"user_provisioning"}{extra_cmds}) {
                $execute_playlist_cmd .= " $playlists{user_provisioning}{extra_cmds}";
            }

            if ($dry_run == 1) {
                $execute_playlist_cmd .= " -v DRY_RUN=yes";
            }

            print "$execute_playlist_cmd";

            $rc = execute_system_cmd($execute_playlist_cmd);
            $upgrade_rc = $rc;

            if ($rc == 1) {
                @messages = ();
                push @messages, ("User provisioning failed\n");
                print_message("@messages");
            }
        }

        if ($playlists{"certificates"}{run} == 1 && $rc == 0) {
            $timestamp = `date +%s`;
            $timestamp =~ s/[\r\n]+$//;
            $job_name = "certificates_sc_$timestamp";
            $execute_playlist_cmd = "perl $daft_directory/perl/bin/execute_playlist.pl $playlists{certificates}{base_cmds} -j $job_name";

            if ($playlists{"certificates"}{extra_cmds}) {
                $execute_playlist_cmd .= " $playlists{certificates}{extra_cmds}";
            }

            if ($dry_run == 1) {
                $execute_playlist_cmd .= " -v DRY_RUN=yes";
            }

            print "$execute_playlist_cmd";

            $rc = execute_system_cmd($execute_playlist_cmd);
            $upgrade_rc = $rc;

            if ($rc == 1) {
                @messages = ();
                push @messages, ("Certificate loading failed\n");
                print_message("@messages");
            }
        }

        if ($playlists{"configuration"}{run} == 1 && $rc == 0) {
            $timestamp = `date +%s`;
            $timestamp =~ s/[\r\n]+$//;
            $job_name = "configuration_sc_$timestamp";
            print "$daft_directory/perl/bin/execute_playlist.pl $playlists{configuration}{base_cmds} -j $job_name\n";
            $configuration_rc = 1;
        }

        if ($playlists{"upgrade"}{run} == 1 && $rc == 0) {
            $timestamp = `date +%s`;
            $timestamp =~ s/[\r\n]+$//;
            $job_name = "upgrade_sc_$timestamp";

            if ($playlists{"upgrade"}{extra_cmds}) {
                $execute_playlist_cmd .= " $playlists{upgrade}{extra_cmds}";
            }

            if ($dry_run == 1) {
                $execute_playlist_cmd .= " -v DRY_RUN=yes";
            }

            $rc = execute_system_cmd($execute_playlist_cmd);
            $upgrade_rc = $rc;

            if ($rc == 1) {
                @messages = ();
                push @messages, ("Upgrade of SC failed\n");
                print_message("@messages");
            }
        }

        if ($playlists{"healthcheck"}{run} == 1 && $rc == 0) {
            $timestamp = `date +%s`;
            $timestamp =~ s/[\r\n]+$//;
            $job_name = "healthcheck_sc_$timestamp";
            $execute_playlist_cmd = "perl $daft_directory/perl/bin/execute_playlist.pl $playlists{healthcheck}{base_cmds} -j $job_name";

            if ($playlists{"healthcheck"}{extra_cmds}) {
                $execute_playlist_cmd .= " $playlists{healthcheck}{extra_cmds}";
            }

            if ($dry_run == 1) {
                $execute_playlist_cmd .= " -v DRY_RUN=yes";
            }

            $rc = execute_system_cmd($execute_playlist_cmd);
            $deploy_rc = $rc;

            if ($rc == 1) {
                @messages = ();
                push @messages, ("Healthcheck of SC failed\n");
                print_message("@messages");
            }
        }
    }

    return $rc;
}

sub update_hash {

    my $software_directory = shift;
    my $network_config_file = shift;
    my $workspace = shift;
    my $extra_variables_certificates = shift;
    my $extra_variables_configuration = shift;
    my $extra_variables_deploy = shift;
    my $extra_variables_healthcheck = shift;
    my $extra_variables_upgrade = shift;
    my $extra_variables_user_provisioning = shift;
    my $extra_variables_evnfm_deployment = shift;

    $playlists{"deploy"} = {
        base_cmds => "-p 001_Deploy_SC -v SOFTWARE_DIR=$software_directory -n $network_config_file -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v INPUT_AVAILABLE=no -w $workspace",
        extra_cmds => "$extra_variables_deploy",
        run => $run_deploy,
    };
    $playlists{"evnfm_deployment"} = {
        base_cmds => "-p 201_EVNFM_Deploy_SC -n $network_config_file -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v INPUT_AVAILABLE=no -w $workspace",
        extra_cmds => "$extra_variables_evnfm_deployment",
        run => $run_evnfm_deployment,
    };
    $playlists{"upgrade"} = {
        base_cmds => "-p 002_Upgrade_SC -v SOFTWARE_DIR=$software_directory -n $network_config_file -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v INPUT_AVAILABLE=no -w $workspace",
        extra_cmds => "$extra_variables_upgrade",
        run => $run_upgrade,
    };
    $playlists{"certificates"} = {
        base_cmds => "-p 007_Certificate_Management -n $network_config_file -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v INPUT_AVAILABLE=no -v GENERATE_CERTIFICATES=yes -w $workspace",
        extra_cmds => "$extra_variables_certificates",
        run => $run_certificates,
    };
    $playlists{"configuration"} = {
        base_cmds => "",
        extra_cmds => "$extra_variables_configuration",
        run => $run_configuration,
    };
    $playlists{"healthcheck"} = {
        base_cmds => "-p 006_SC_Healthcheck -n $network_config_file -v INPUT_AVAILABLE=no -v SC_HEALTHCHECK=$healthcheck_type -w $workspace",
        extra_cmds => "$extra_variables_healthcheck",
        run => $run_healthcheck,
    };
    $playlists{"user_provisioning"} = {
        base_cmds => "-p 005_User_Management -n $network_config_file -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v INPUT_AVAILABLE=no -w $workspace",
        extra_cmds => "$extra_variables_user_provisioning",
        run => $run_user_provisioning,
    };
}

sub show_help {
    print <<EOF;

Description:
============

This script is used as a coordinator script to deploy and upgrade
an SC node using DAFT.

NOTE:
(1) This script should be run on the director node

Syntax:
=======

$0 [<OPTIONAL>] <MANDTORY>

<MANDTORY> is one of the following parameters:

  -e <full filepath> | --network_config_file <full filepath>

<OPTIONAL> are one or more of the following parameters:

  -a                   | --healthcheck
  -c                   | --load_configuration
  -d                   | --deploy
  -f <directory path>  | --daft_directory <directory path>
  -h                   | --help
  -l                   | --load_certificates
  -p                   | --user_provisioning
  -s <directory path>  | --software_directory <directory path>
  -u                   | --upgrade
  -va                  | --extra_variables_healthcheck
  -vc                  | --extra_variables_configuration
  -vd                  | --extra_variables_deploy
  -ve                  | --extra_variables_certificates
  -vp                  | --extra_variables_user_provisioning
  -vu                  | --extra_variables_upgrade
  -w <directory path>  | --workspace <directory path>
  -y                   | --dry_run

where:

  -e <full filepath>
  --network_config_file <full filepath>
  -------------------------------------
  Full path to the network configuration file that should be used.

  -a
  --healthcheck
  -------------
  Toggle to execute playlist 006_SC_Healthcheck, the user will be promted to
  either execute the pre or post healthcheck.

  -c
  --load_configuration
  --------------------

  -d
  --deploy
  --------
  Toggle to execute playlist 001_Deploy_SC.

  -f <directory path>
  --daft_directory <directory path>
  ---------------------------------
  Directory path into which the DAFT tar ball will be extracted, this
  directory will also be used during playlist execution.

  -h
  --help
  ------
  Shows this help information.

  -l
  --load_certificates
  -------------------
  Toggle to execute playlist 007_Certificate_Management.

  -p
  --user_provisioning
  -------------------
  Toggle to execute playlist 005_User_Management.

  -s <directory path>
  --software_directory <directory path>
  -------------------------------------
  Directory where the csar files for SC are located.

  -u
  --upgrade
  ---------
  Toggle to execute playlist 002_Upgrade_SC.

  -v
  --evnfm_deployment
  ---------
  Toggle to execute playlist 201_EVNFM_Deploy_SC.

  -va
  --extra_variables_healthcheck
  -----------------------------
  Add extra job paramters to the healthcheck execution.

  -vc
  -extra_variables_configuration
  ------------------------------

  -vd
  --extra_variables_deploy
  ------------------------
  Add extra job paramters to the deploy execution.

  -ve
  --extra_variables_certificates
  ------------------------------
  Add extra job paramters to certificate loading execution.

  -vp
  --extra_variables_user_provisioning
  -----------------------------------
  Add extra job paramters to user provisioning execution

  -vu
  --extra_variables_upgrade
  -------------------------
  Add extra job paramters to the upgrade execution.

  -vv
  --extra_variables_evnfm_deployment
  -------------------------
  Add extra job paramters to the EVNFM deployment execution.

  -w <directory path>
  --workspace <directory path>
  ----------------------------
  Path to workspace directory, if left empty /tmp/workspace is used.

  -y
  --dry_run
  ---------
  Toggle to execute all playlists with the parameter -v DRY_RUN=yes,
  This parameter will force playlists to jump over certain actions.

EOF
}

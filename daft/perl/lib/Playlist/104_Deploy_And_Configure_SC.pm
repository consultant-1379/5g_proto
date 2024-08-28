package Playlist::104_Deploy_And_Configure_SC;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.4
#  Date     : 2024-03-26 15:42:49
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

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

#
# Used Perl package files
#

use General::Logging;
use General::Playlist_Operations;

use Playlist::001_Deploy_SC;
use Playlist::003_Undeploy_SC;
use Playlist::004_Config_Management;
use Playlist::005_User_Management;
use Playlist::007_Certificate_Management;
use Playlist::103_Tools_Management;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

# -----------------------------------------------------------------------------
# Playlist logic.
#
# Input variables:
#  -
#
# Output variables:
#  -
#
# Return values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub main {

    my $node_count = 0;
    my $rc;
    my $valid;
    my $var_name;

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "DEPLOY_AND_CONFIGURE";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
        General::Logging::log_user_error_message("You must specify a network configuration file for this Playlist to run");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    $rc = General::Playlist_Operations::execute_step( \&Call_Main_Playlists_P104S01, \&Fallback001_P104S99 );
    return $rc if $rc < 0;

    return $rc;
}

####################
#                  #
# Step Definitions #
#                  #
####################

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Call_Main_Playlists_P104S01 {

    my $rc;

    # We always want to read the network config file for each playlist
    General::State_Machine::always_execute_task("Playlist::920_Check_Network_Config_Information::.+");

    # We also always want to execute the pre and post health check playlists
    General::State_Machine::always_execute_task("Playlist::917_Pre_Healthcheck::.+");
    General::State_Machine::always_execute_task("Playlist::918_Post_Healthcheck::.+");

    # To avoid removing directories before all tasks has been executed we will
    # only execute the cleanup at the end.
    General::State_Machine::always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    print_start_playlist_message("103_Tools_Management");
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::103_Tools_Management::main } );
    return $rc if $rc < 0;

    print_start_playlist_message("003_Undeploy_SC");
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::003_Undeploy_SC::main } );
    return $rc if $rc < 0;

    print_start_playlist_message("001_Deploy_SC");
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::001_Deploy_SC::main } );
    return $rc if $rc < 0;

    print_start_playlist_message("005_User_Management");
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::005_User_Management::main } );
    return $rc if $rc < 0;

    if ( ($::JOB_PARAMS{'CERTIFICATE_DATA_FILE'} && $::JOB_PARAMS{'CERTIFICATE_DATA_FILE'} ne "") || ( exists $::JOB_PARAMS{'GENERATE_CERTIFICATES'} && $::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes") ) {
        print_start_playlist_message("007_Certificate_Management");
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::007_Certificate_Management::main } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_DATA_FILE'} && $::JOB_PARAMS{'CONFIG_DATA_FILE'} ne "") {
        print_start_playlist_message("004_Config_Management");
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::004_Config_Management::main } );
        return $rc if $rc < 0;
    }

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "DEPLOY_AND_CONFIGURE";

    print_start_playlist_message("902_Cleanup_Job_Environment");
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if (($rc == 0) && ($::JOB_PARAMS{'DRY_RUN'} eq "no")) {
        # Mark that the Job was successful
        $::JOB_PARAMS{'JOBSTATUS'} = "SUCCESSFUL";
    }

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P104S99 {

    my $message = "";
    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "DEPLOY_AND_CONFIGURE";

    # Now we need to execute the cleanup.
    General::State_Machine::always_execute_task("Playlist::902_Cleanup_Job_Environment::main");

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    return 0;
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
# This subroutine print a message to the progress log and to the status log for
# every started playlist.
#
sub print_start_playlist_message {
    my $playlist_name = shift;
    my $message = "";

    General::Message_Generation::print_message_box(
        {
            "messages"      => [
                "",
                "Playlist $playlist_name",
                "",
            ],
            "align-text"    => "left",
            "return-output" => \$message,
        }
    );
    General::Logging::log_user_message($message);
    push @::JOB_STATUS, "(-) <<<<< Starting Playlist $playlist_name >>>>>";
}

# -----------------------------------------------------------------------------
# This subroutine reads all parameters from the specified network configuration
# file and updates the %::NETWORK_CONFIG_PARAMS and %::JOB_PARAMS hash variables
# after applying filters to remove unwanted parameters.
#
sub parse_network_config_parameters {
    return Playlist::920_Check_Network_Config_Information::parse_network_config_parameters();
}


# -----------------------------------------------------------------------------
# We override one variable that was mandatory in the 004_Config_Management.pm
# but is optional in this playlist.
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'COLLECT_LOGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the steps and tasks inside the playlist 914_Collect_Logs
will be executed or not.

Select "yes" which is also the default value if not specified, to always execute
the steps and tasks inside the 914_Collect_Logs playlist when it's being called.
Setting it to "yes" does not mean that logs will always be collected, it just
means that they will be collected if the playlist is called. I.e. setting
SKIP_COLLECT_LOGS=yes will skip calling the playlist for the normal cases.

Select "no", to skip all steps and tasks inside the playlist 914_Collect_Logs, i.e.
to not collect any log information done by this playlist.

This parameter has higher priority than for example SKIP_COLLECT_LOGS which only
affects normal log collection and not log collection done when doing fallback,
which is always collected if the main playlist has the logic to call the
914_Collect_Logs playlist.
I.e. if COLLECT_LOGS=no then logs will not be collected at all by the playlist
914_Collect_Logs no matter if the playlist was successful or ended up in a
fallback. This can for example be useful if you know that a playlist execution
will fail and you are not interested in the logs collected by the 914_Collect_Logs
playlist which could save you 15 minutes or more from collecting the logs.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_DATA_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The path to a file containing configuration data to load.

If 'USE_NETCONF=yes' is specified then the data in the file MUST BE in NETCONF
format for loading configuration data.
This file should contain all data for successfully loading the data, which
should include any <hello and <edit-config> tags.

If 'USE_NETCONF=no' is specified then the data in the file MUST BE in CLI format
for loading configuration data.
This file should contain all the needed commands for loading the data, which
includes 'config', '...' and 'commit' commands.

NOTE 1:
If multiple configurations files should be loaded then these files should be
specified with job parameter CONFIG_DATA_FILE_1, CONFIG_DATA_FILE_2 etc. but
the first file to be loaded is always specified with the CONFIG_DATA_FILE job
parameter.

NOTE 2:
If the files should be loaded using different users and passwords then the
files to load must follow a specific naming convention:

<filename prefix>,user=<username>[,password=<password>].<cli|netconf>

    For example: -v CONFIG_DATA_FILE=/path/scp_config,user=scp-admin.netconf
                 -v CONFIG_DATA_FILE_1=/path/csa_config,user=csa-admin.netconf
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist script is used for performing Undeployment, Deployment, adding
Users, loading Certificates and loading of configuration data on an ESC node
and it will be called from the execute_playlist.pl script.

Used Job Parameters:
====================
EOF
    # Special handling for this playlist so we print out all variable information from
    # the main playlist and the all other called main playlists.
    use File::Basename qw(dirname basename);
    my $length;
    my $name;
    my $message;
    my $path_to_playlist = dirname(__FILE__);
    my $playlist_name = basename(__FILE__);
    $playlist_name =~ s/\.pm//;
    my %temp_hash = ( %Playlist::103_Tools_Management::playlist_variables, %Playlist::007_Certificate_Management::playlist_variables, %Playlist::005_User_Management::playlist_variables, %Playlist::004_Config_Management::playlist_variables, %Playlist::003_Undeploy_SC::playlist_variables, %Playlist::001_Deploy_SC::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    no strict;
    for $playlist_name ("001_Deploy_SC", "003_Undeploy_SC", "004_Config_Management", "005_User_Management", "007_Certificate_Management", "103_Tools_Management") {
        $message = "# Global variable access in playlist $playlist_name #";
        $length = length($message);
        printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
        $name = "Playlist::${playlist_name}::playlist_variables";
        General::Playlist_Operations::print_used_job_variables("$path_to_playlist/$playlist_name.pm", \%$$name);
        General::Playlist_Operations::print_used_network_config_variables("$path_to_playlist/$playlist_name.pm");
    }
    use strict;
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::103_Tools_Management::playlist_variables, %Playlist::007_Certificate_Management::playlist_variables, %Playlist::005_User_Management::playlist_variables, %Playlist::004_Config_Management::playlist_variables, %Playlist::003_Undeploy_SC::playlist_variables, %Playlist::001_Deploy_SC::playlist_variables, %playlist_variables );
}

1;

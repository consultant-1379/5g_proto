package Playlist::403_Maintainability_Test_Scaling;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.15
#  Date     : 2024-06-03 17:22:28
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
use version;

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::Data_Structure_Operations;
use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::932_Test_Case_Common_Logic;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

my $debug_command = "";         # Change this value from "echo " to "" when ready to test on real system

# The supported objects that can be scaled.
my %scaling_deployment_check = (
    "bsf" => [
        "deployments,eric-bsf-worker",
        "deployments,eric-bsf-diameter",
        "deployments,eric-stm-diameter",
        'statefulsets,eric-bsf-wcdb-cd\S*',    # eric-bsf-wcdb-cd or eric-bsf-wcdb-cd-datacenter1-rack1
    ],
    "dsc" => [
        "deployments,eric-dsc-fdr",
        "deployments,eric-dsc-agent",
    ],
    "scp" => [
        "deployments,eric-scp-worker",
        "deployments,eric-sc-slf",
        "deployments,eric-sc-rlf",
    ],
    "sepp" => [
        "deployments,eric-sepp-worker",
        "deployments,eric-sc-rlf",
    ],
);
my %scaling_data = (
    "bsf" => [
        "eric-bsf.spec.worker.replicaCount=x",
        "eric-bsf-diameter.replicaCount=x",
        "eric-stm-diameter.replicaCount=x",
        "eric-bsf-wcdb-cd.replicaCount=x",
    ],
    "dsc" => [
        "eric-dsc-fdr.replicaCount=x",
        "eric-dsc-agent.replicaCount=x",
    ],
    "scp" => [
        "eric-scp.spec.worker.replicaCount=x",
        "eric-sc-slf.spec.slf.replicaCount=x",
        "eric-sc-rlf.spec.rlf.replicaCount=x",
    ],
    "sepp" => [
        "eric-sepp.spec.worker.replicaCount=x",
        "eric-sc-rlf.spec.rlf.replicaCount=x",
    ],
);

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
    my $rc;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "ROBUSTNESS_TEST_SCALING";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P403S01, \&Fallback001_P403S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P403S02, \&Fallback001_P403S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P403S03, \&Fallback001_P403S99 );
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
sub Perform_Pre_Test_Checks_P403S01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    return $rc;
}

# -----------------------------------------------------------------------------
# Step logic.
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
#
# -----------------------------------------------------------------------------
sub Perform_Test_Case_P403S02 {

    my $application = $::JOB_PARAMS{'SCALE_APPLICATION'};
    my $index = exists $::JOB_PARAMS{'SCALE_INDEX'} ? $::JOB_PARAMS{'SCALE_INDEX'} : 0;
    my $length;
    my $message;
    my $original_replicaCount;
    my $rc;
    my $scaling_command;
    my @scaling_data = @{$scaling_data{$application}};
    my $scaling_value;

    $message = "# Test Cases #";
    $length = length($message);
    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P403S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_Replica_Count_Values_P403S02T02 } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'} = exists $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'} ? $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'} : 1;
    $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'} = exists $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'} ? $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'} : 1;
    $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'} = exists $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'} ? $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'} : 1;
    $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_IN'}  = exists $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_IN'}  ? $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_IN'}  : 1;
    $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_IN'}  = exists $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_IN'}  ? $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_IN'}  : 1;
    $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_IN'}  = exists $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_IN'}  ? $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_IN'}  : 1;

    # Repeat the test case a number of times
    for (; $index <= $#scaling_data; $index++) {

        $::JOB_PARAMS{'SCALE_INDEX'} = $index;
        $scaling_command = $scaling_data[$index];
        $scaling_command =~ s/=\S+$//;
        if ($scaling_command =~ /^(eric-bsf|eric-stm).+/) {
            $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'};
            $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = $::JOB_PARAMS{'SC_BSF_UMBRELLA_FILE'};
        } elsif ($scaling_command =~ /^eric-dsc.+/) {
            $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_NAME'};
            $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = $::JOB_PARAMS{'SC_DSC_UMBRELLA_FILE'};
        } elsif ($scaling_command =~ /^(eric-scp|eric-sc-slf).+/) {
            $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_NAME'};
            $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = $::JOB_PARAMS{'SC_SCP_UMBRELLA_FILE'};
        } elsif ($scaling_command =~ /^eric-sepp.+/) {
            $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_NAME'};
            $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = $::JOB_PARAMS{'SC_SEPP_UMBRELLA_FILE'};
        } elsif ($scaling_command =~ /^eric-sc-rlf.+/) {
            $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::JOB_PARAMS{'OLD_SC_CS_RELEASE_NAME'};
            $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'};
        }

        $message = sprintf "# Repetition %d of %d, Scaling: %s #", $index + 1, $#scaling_data + 1, $scaling_command;
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        #############################
        # Scale out the Application #
        #############################

        # Set start time for this repetition in case we want to have KPI verdicts for each scale operation
        $::JOB_PARAMS{'KPI_START_TIME'} = time();

        # Fecth the original replicaCount value from the deployed values
        if (exists $::JOB_PARAMS{$scaling_command}) {
            $original_replicaCount = $::JOB_PARAMS{$scaling_command};
            $scaling_value = $original_replicaCount + $::JOB_PARAMS{'SCALE_INCREMENT'};
            $::JOB_PARAMS{'SCALE_VALUE'} = $scaling_value;
            $::JOB_PARAMS{'SCALE_SET_VALUE'} = "$scaling_command=$scaling_value";

            # Set the description to be used for status messages
            $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Scaling (%s)", $index + 1, $::JOB_PARAMS{'SCALE_SET_VALUE'};
        } else {
            # Set the description to be used for status messages
            $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Scaling (%s)", $index + 1, $scaling_command;

            push @::JOB_STATUS, "(-) $::JOB_PARAMS{'TEST_DESCRIPTION'} could not be executed, current value not found";
            General::Logging::log_user_warning_message("Could not find the original value for '$scaling_command', possibly because it's not deployed.\nNot able to scale this application.");
            next;
        }

        if ($original_replicaCount eq "null") {
            # This NF is not deployed, e.g. 'rlf' on an UCC deployment
            push @::JOB_STATUS, "(-) $::JOB_PARAMS{'TEST_DESCRIPTION'} could not be executed, current replicaCount is null";
            General::Logging::log_user_message("Could not perform scale out/in of '$scaling_command', because the current replicaCount is 'null' probably because the NF is not deployed");
            next;
        }

        $message = sprintf "# Scale Out: %s #", $::JOB_PARAMS{'SCALE_SET_VALUE'};
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        if ($scaling_command ne "eric-bsf-wcdb-cd.replicaCount") {
            #
            # Apply normal handling where we first scale out and then scale in.
            #

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Scale_Application_P403S02T03 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Verify_Scaled_Application_P403S02T04 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Stable_Traffic_P403S02T05 } );
                return $rc if $rc < 0;

                # This call will wait for all PODs to be up and then check node health
                $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'} = 0;
            }

            #############################
            # Scale in the Application #
            #############################

            # Set start time for this repetition in case we want to have KPI verdicts for each scale operation
            $::JOB_PARAMS{'KPI_START_TIME'} = time();

            # Restore the original replicaCount value
            $::JOB_PARAMS{'SCALE_VALUE'} = $original_replicaCount;
            $::JOB_PARAMS{'SCALE_SET_VALUE'} = "$scaling_command=$original_replicaCount";

            $message = sprintf "# Scale In: %s #", $::JOB_PARAMS{'SCALE_SET_VALUE'};
            $length = length($message);
            General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

            # Set the description to be used for status messages
            $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Scaling (%s)", $index + 1, $::JOB_PARAMS{'SCALE_SET_VALUE'};

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T03_IN'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Scale_Application_P403S02T03 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_IN'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T04_IN'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Verify_Scaled_Application_P403S02T04 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_IN'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T05_IN'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Stable_Traffic_P403S02T05 } );
                return $rc if $rc < 0;

                # This call will wait for all PODs to be up and then check node health
                $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_IN'} = 0;
            }

        } else {

            #
            # Apply special handling for "eric-bsf-wcdb-cd.replicaCount" because it does not
            # support the scale in operation and other special tasks also must be done to cleanup the database.
            #

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T06_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Find_Existing_Wide_Column_Database_Pods_P403S02T06 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T06_OUT'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Scale_Application_P403S02T03 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Verify_Scaled_Application_P403S02T04 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T07_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Cleanup_Keyspaces_P403S02T07 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T07_OUT'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T08_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Write_Access_And_Restore_Write_Permissions_P403S02T08 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T08_OUT'} = 0;
            }

            if ($::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Stable_Traffic_P403S02T05 } );
                return $rc if $rc < 0;

                # This call will wait for all PODs to be up and then check node health
                $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'} = 0;
            }
        }

        $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'} = 1;
        $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'} = 1;
        $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'} = 1;
        $::JOB_PARAMS{'P403_EXECUTE_P403S02T06_OUT'} = 1;
        $::JOB_PARAMS{'P403_EXECUTE_P403S02T07_OUT'} = 1;
        $::JOB_PARAMS{'P403_EXECUTE_P403S02T08_OUT'} = 1;
        $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_IN'}  = 1;
        $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_IN'}  = 1;
        $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_IN'}  = 1;
    }

    # Skip the final post healthcheck since we have already done it above
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_OUT'};
    delete $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_OUT'};
    delete $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_OUT'};
    delete $::JOB_PARAMS{'P403_EXECUTE_P403S02T03_IN'};
    delete $::JOB_PARAMS{'P403_EXECUTE_P403S02T04_IN'};
    delete $::JOB_PARAMS{'P403_EXECUTE_P403S02T05_IN'};

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
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
    sub Check_Job_Parameters_P403S02T01 {

        my $rc = 0;

        # Check that the wanted SCALE_APPLICATION match the deployed application
        if ($::JOB_PARAMS{'nodetype'} !~ /$::JOB_PARAMS{'SCALE_APPLICATION'}/) {
            if (exists $::JOB_PARAMS{'APPLICATION_TYPE'} && $::JOB_PARAMS{'APPLICATION_TYPE'} =~ /$::JOB_PARAMS{'SCALE_APPLICATION'}/) {
                # It's still supported because it might be 'dsc'
            } else {
                General::Logging::log_user_error_message("The application to scale \'$::JOB_PARAMS{'SCALE_APPLICATION'}\' is not in the list of deployed applications: $::JOB_PARAMS{'nodetype'}");
                return 1;
            }
        }

        if (version->parse($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}) < version->parse("1.15.0")) {
            # Old release which we no longer suppoort
            General::Logging::log_user_error_message("This playlist no longer support releases older than 1.15.0 and it detected version \'$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}\'.");
            return 1;
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
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
    sub Fetch_Replica_Count_Values_P403S02T02 {

        my $command;
        my $message = "The following original values will be used as default values for the scaling:\n";
        my $rc = 0;
        my $release_name;
        my @result;
        my $var_name;

        General::Logging::log_user_message("Get Values from SC deployment");
        for (@{$scaling_data{$application}}) {

            # bsf
            if (/^(eric-bsf\.spec\.worker\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'} | jq '.\"eric-bsf\".\"spec\".\"worker\".\"replicaCount\"'";
            } elsif (/^(eric-bsf-diameter\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'} | jq '.\"eric-bsf-diameter\".\"replicaCount\"'";
            } elsif (/^(eric-stm-diameter\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'} | jq '.\"eric-stm-diameter\".\"replicaCount\"'";
            } elsif (/^(eric-bsf-wcdb-cd\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'} | jq '.\"eric-bsf-wcdb-cd\".\"replicaCount\"'";

            # dsc
            } elsif (/^(eric-dsc-fdr\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_NAME'} | jq '.\"eric-dsc-fdr\".\"replicaCount\"'";
            } elsif (/^(eric-dsc-agent\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_NAME'} | jq '.\"eric-dsc-agent\".\"replicaCount\"'";

            # scp
            } elsif (/^(eric-scp\.spec\.worker\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_NAME'} | jq '.\"eric-scp\".\"spec\".\"worker\".\"replicaCount\"'";
            } elsif (/^(eric-sc-slf\.spec\.slf\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_NAME'} | jq '.\"eric-sc-slf\".\"spec\".\"slf\".\"replicaCount\"'";
            } elsif (/^(eric-sc-rlf\.spec\.rlf\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_CS_RELEASE_NAME'} | jq '.\"eric-sc-rlf\".\"spec\".\"rlf\".\"replicaCount\"'";

            # sepp
            } elsif (/^(eric-sepp\.spec\.worker\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_NAME'} | jq '.\"eric-sepp\".\"spec\".\"worker\".\"replicaCount\"'";
            } elsif (/^(eric-sc-rlf\.spec\.rlf\.replicaCount)=.+/) {
                $var_name = $1;
                $command = "$::JOB_PARAMS{'HELM_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get values --all -o json $::JOB_PARAMS{'OLD_SC_CS_RELEASE_NAME'} | jq '.\"eric-sc-rlf\".\"spec\".\"rlf\".\"replicaCount\"'";
            } else {
                next;
            }

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc == 0) {
                if (scalar @result == 1 && $result[0] =~ /^['"]*(\d+|null)['"]*$/) {
                    $::JOB_PARAMS{$var_name} = $1;
                    $message .= "  $var_name=$1\n";
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to find replicaCount integer value for $var_name");
                    return 1;
                }
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch replicaCount value for $var_name");
                return 1;
            }
        }

        General::Logging::log_user_message($message);

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
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
    sub Scale_Application_P403S02T03 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my $sc_umbrella_file = $::JOB_PARAMS{'SC_UMBRELLA_FILE'};
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT'};

        if ($sc_umbrella_file eq "") {
            # New way of deploying software like DSC
            $sc_umbrella_file = $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'};
        }

        if ($::JOB_PARAMS{'HELM_VERSION'} != 2) {
            $timeout .= "s";    # Add an extra 's' for number of seconds which is needed for helm 3 or higher
        }

        General::Logging::log_user_message("Scaling application to $::JOB_PARAMS{'SCALE_SET_VALUE'}.\nThis will take a while to complete.\n");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace upgrade $sc_release_name --set $::JOB_PARAMS{'SCALE_SET_VALUE'} $sc_umbrella_file --timeout $timeout --reuse-values --debug",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} was successful");
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
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
    sub Verify_Scaled_Application_P403S02T04 {

        my $kubectl_executable = $::JOB_PARAMS{'KUBECTL_EXECUTABLE'};
        my $max_wait_time = $::JOB_PARAMS{'SCALE_MAX_WAIT_TIME'};
        my $max_wait_epoch = time() + $max_wait_time;
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $scale_app   = $::JOB_PARAMS{'SCALE_APPLICATION'};
        my $scale_index = $::JOB_PARAMS{'SCALE_INDEX'};
        my $scale_value = $::JOB_PARAMS{'SCALE_VALUE'};
        my $deployment_check  = $scaling_deployment_check{$scale_app}[$scale_index];
        my $check_name;
        my $check_type;

        if ($deployment_check =~ /^(\S+),(\S+)$/) {
            $check_type = $1;
            $check_name = $2;
        } else {
            $check_type = "deployments";
            $check_name = $deployment_check;
        }

        General::Logging::log_user_message("Checking that $check_type $check_name has scaled to $scale_value for a maximum of $max_wait_time seconds");

        while (1) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$kubectl_executable --namespace $sc_namespace get $check_type --no-headers",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} failed because the kubectl command failed with rc=$rc");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";

                return $rc;
            }

            my $found = 0;
            for (@result) {
                if ($check_type eq "deployments" && /^$check_name\s+(\d+)\/(\d+)\s+(\d+)\s+(\d+)\s+\S+/) {
                    # NAME               READY   UP-TO-DATE   AVAILABLE   AGE
                    # eric-sepp-worker   0/2     2            0           22h
                    if ($1 == $scale_value && $2 == $scale_value && $3 == $scale_value && $4 == $scale_value) {
                        $found = 1;
                        last;
                    }
                } elsif ($check_type eq "statefulsets" && /^$check_name\s+(\d+)\/(\d+)\s+\S+/) {
                    # NAME                                                  READY   AGE
                    # eric-bsf-wcdb-cd-datacenter1-rack1   2/2     108m
                    if ($1 == $scale_value && $2 == $scale_value) {
                        $found = 1;
                        last;
                    }
                }
            }
            if ($found == 1) {
                # The deployment has properly scaled
                General::Logging::log_user_message("The $check_type $check_name has been properly scaled.");
                last;
            } elsif (time() < $max_wait_epoch) {
                # We need to wait longer for it to finish the scaling operation
                $rc = General::OS_Operations::sleep_with_progress(
                    {
                        "allow-interrupt"   => 1,
                        "confirm-interrupt" => 0,
                        "progress-message"  => 0,
                        "seconds"           => 5,
                        "use-logging"       => 1,
                    }
                );
                if ($rc == 1) {
                    General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
                    push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} was interrupted by the user";
                    return $rc;
                }
                # We now try the command again
            } else {
                # We have waited long enough, throw an error
                General::Logging::log_user_error_message("The $check_type $check_name is not yet ready or not scaled to $scale_value.\n$_\n");
                $rc = 1;
                last;
            }
        }

        if ($rc != 0) {
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
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
    sub Wait_For_Stable_Traffic_P403S02T05 {

        my $rc = 0;

        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => 0,
                "progress-message"  => 1,
                "seconds"           => $::JOB_PARAMS{'WAIT_TIME_BETWEEN_SCALE_OPERATIONS'},
                "use-logging"       => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was successful";
        } else {
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} was interrupted by the user";
            General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
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
    sub Find_Existing_Wide_Column_Database_Pods_P403S02T06 {

        my $rc = 0;

        $::JOB_PARAMS{'P403_OLD_WCDB_PODS'} = "";
        my @pod_names = ADP::Kubernetes_Operations::get_pod_names(
            {
                "hide-output"       => 1,
                "namespace"         => $::JOB_PARAMS{'SC_NAMESPACE'},
                "pod-include-list"  => [ '^eric-bsf-wcdb-cd\S*-\d+$' ],
            }
        );
        $::JOB_PARAMS{'P403_OLD_WCDB_PODS'} = join ",", @pod_names;

        if ($::JOB_PARAMS{'P403_OLD_WCDB_PODS'} ne "") {
            return 0;
        } else {
            General::Logging::log_user_error_message("Failed to find eric-bsf-wcdb-cd-xxx pods.");
            return 1;
        }
    }

    # -----------------------------------------------------------------------------
    # Task logic.
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
    sub Cleanup_Keyspaces_P403S02T07 {

        my $failure_cnt = 0;
        my $kubectl_executable = $::JOB_PARAMS{'KUBECTL_EXECUTABLE'};
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my @pod_names = split /,/, $::JOB_PARAMS{'P403_OLD_WCDB_PODS'};
        my $rc = 0;
        my @result;

        for my $pod_name (@pod_names) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${kubectl_executable} exec $pod_name --namespace $namespace -c cassandra -- nodetool cleanup",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to cleanup the cassandra database in pod $pod_name.\n" . join "\n", @result);
                $failure_cnt++;
            }
        }

        if ($failure_cnt == 0) {
            General::Logging::log_user_message("Cleanup of Keyspaces successfule");
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} Cleanup of Keyspaces successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Cleanup of Keyspaces failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} Cleanup of Keyspaces failed";
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }
    }

    # -----------------------------------------------------------------------------
    # Task logic.
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
    sub Check_Write_Access_And_Restore_Write_Permissions_P403S02T08 {

        my $cmyp_ip = ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $initialize_database = 0;
        my $rc = 0;
        my @result;
        my $user_name = $::JOB_PARAMS{'BSF_USER'};
        my $user_password = "";

        unless ($cmyp_ip) {
            General::Logging::log_user_error_message("Failed to find the CMYP IP-address");
            return 1;
        }

        # Find the password for the user
        for my $key (keys %::NETWORK_CONFIG_PARAMS) {
            if (exists $::NETWORK_CONFIG_PARAMS{$key}{'user'} && $::NETWORK_CONFIG_PARAMS{$key}{'user'} eq $user_name) {
                $user_password = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                last;
            }
        }
        if ($user_password eq "" || $user_password eq "CHANGEME") {
            General::Logging::log_user_error_message("Failed to find a valid password for the user '$user_name'");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        # Check database schema
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                    "--ip=$cmyp_ip " .
                    "--user=$user_name " .
                    "--password='$user_password' " .
                    "--timeout=$expect_timeout " .
                    "--command='bsf-function nf-instance bsf1 bsf-service serviceName1 binding-database check-db-schema' " .
                    "--command='exit' " ,
                "hide-output"         => 1,
                "return-output"       => \@result,
             }
        );
        # We don't care what the $rc contains, we always look for a specific string in the output
        for (@result) {
            if (/Database User does not exist or does not have the required permissions/i) {
                $initialize_database = 1;
                last;
            }
        }

        # Check if we need to initialize the database again
        if ($initialize_database == 0) {
            General::Logging::log_user_message("Initialize the database is not needed");
        } else {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                        "--ip=$cmyp_ip " .
                        "--user=$user_name " .
                        "--password='$user_password' " .
                        "--timeout=$expect_timeout " .
                        "--command='bsf-function nf-instance bsf1 bsf-service serviceName1 binding-database initialize-db datacenter { name datacenter1 replication-factor 2 }' " .
                        "--command='exit' " ,
                    "hide-output"         => 1,
                    "return-output"       => \@result,
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("Initialize the database was successful");
            } else {
                General::Logging::log_user_error_message("Failed to initialize the database" . join "\n", @result);
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }
        }

        return 0;
    }
}

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
sub Perform_Post_Test_Checks_P403S03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P403S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

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
# This subroutine reads all parameters from the specified network configuration
# file and updates the %::NETWORK_CONFIG_PARAMS and %::JOB_PARAMS hash variables
# after applying filters to remove unwanted parameters.
#
sub parse_network_config_parameters {
    return Playlist::932_Test_Case_Common_Logic::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    # Define playlist specific variables that is not already defined in the
    # sub playlist Playlist::932_Test_Case_Common_Logic.
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'BSF_USER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "bsf-admin",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the user id to use when connecting to CMYP CLI to perform BSF actions.
The password to be used will be fetched from the network configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCALE_APPLICATION' => {
            'case_check'    => "lowercase",  # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the application that should be scaled out and in.
EOF
            'validity_mask' => '(bsf|dsc|scp|sepp)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCALE_INCREMENT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "1",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the number of steps to increment and decrement when scaling out and
scaling in i.e. how many replicaCount to add and remove to the existing value.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCALE_MAX_WAIT_TIME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "300",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the maximum number of seconds to wait for the scaling to be applied i.e.
how long to wait after the scaling operation until the deployment shows x/x.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SOFTWARE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds the software to be used for the helm
upgrade operation used for scaling out and in the application, i.e. the
directory that contains the 'eric-sc-*.csar' and 'eric-sc-tools-*.tgz' files.
This directory should contain the same software that is currently deployed on
the cluster, from this directory the playlist will extract out the SC umbrella
file which is used by by this Robustness playlists when applying the different
scale out and in by using the 'helm upgrade' command.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'WAIT_TIME_BETWEEN_SCALE_OPERATIONS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "60",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the time in seconds to wait between each scale operation
to make sure the traffic stabilize.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist performs a maintainability test case that verifies scale out and
scale in of a specified application.

Used Job Parameters:
====================
EOF
    # Special handling for the 3xx_Robustness_Test_xxxx playlists so we print out all variable
    # information from the main playlist and the 932_Test_Case_Common_Logic sub playlist.
    use File::Basename qw(dirname basename);
    my $length;
    my $message;
    my $path_to_932_playlist = dirname(__FILE__) . "/932_Test_Case_Common_Logic.pm";
    my $playlist_name = basename(__FILE__);
    $playlist_name =~ s/\.pm//;
    my %temp_hash = ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    $message = "# Global variable access in playlist 932_Test_Case_Common_Logic #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables($path_to_932_playlist, \%Playlist::932_Test_Case_Common_Logic::playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables($path_to_932_playlist);
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );
}

1;

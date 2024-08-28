package Playlist::205_EVNFM_Scaling_SC;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.13
#  Date     : 2024-05-14 10:20:43
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2024
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

use ADP::EVNFM_Operations;
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
#  1: The call failed
#  255: Stepout wanted
#
# -----------------------------------------------------------------------------
sub main {
    my $rc;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "SCALING";

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

    if ($::JOB_PARAMS{'SCALING_TYPE'} eq "OUT_THEN_IN") {
        # We need to allow executing some tasks multiple times.
        General::State_Machine::always_execute_task('Playlist::205_EVNFM_Scaling_SC::.+_P205S02T\d\d');
        General::State_Machine::always_execute_task("Playlist::918_Post_Healthcheck.+");
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P205S01, \&Fallback001_P205S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P205S02, \&Fallback001_P205S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P205S03, \&Fallback001_P205S99 );
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
sub Perform_Pre_Test_Checks_P205S01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_Part_3_P205S01T01 } );
    return $rc if $rc < 0;

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
    sub Check_Job_Parameters_Part_3_P205S01T01 {

        my $clustername = "";
        my $ingress_host_address = "";
        my $password = "";
        my $rc = 0;
        my $username = "";

        # Get the proper CLUSTERNAME and CONFIG_FILE_CHANGE_VNF_PACKAGE
        General::Logging::log_user_message("Checking Job parameter 'CLUSTERNAME' and 'CONFIG_FILE_CHANGE_VNF_PACKAGE' and Network Config parameter 'evnfm_clustername'");
        if ($::JOB_PARAMS{'CLUSTERNAME'} ne "") {
            $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'CLUSTERNAME'} = $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'};
                $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
            } elsif ($::JOB_PARAMS{'CONFIG_FILE_CHANGE_VNF_PACKAGE'} eq "") {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_clustername' has not been set and Job parameter 'CLUSTERNAME' or 'CONFIG_FILE_CHANGE_VNF_PACKAGE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'CLUSTERNAME' nor 'CONFIG_FILE_CHANGE_VNF_PACKAGE' or Network Config parameter 'evnfm_clustername' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper INGRESS_HOST_ADDRESS
        General::Logging::log_user_message("Checking Job parameter 'INGRESS_HOST_ADDRESS' and Network Config parameter 'evnfm_ingress_host_address'");
        if ($::JOB_PARAMS{'INGRESS_HOST_ADDRESS'} ne "") {
            $ingress_host_address = $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_ingress_host_address'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_ingress_host_address'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'} = $::NETWORK_CONFIG_PARAMS{'evnfm_ingress_host_address'}{'value'};
                $ingress_host_address = $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_ingress_host_address' has not been set and Job parameter 'INGRESS_HOST_ADDRESS' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'INGRESS_HOST_ADDRESS' nor Network Config parameter 'evnfm_ingress_host_address' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper PASSWORD
        General::Logging::log_user_message("Checking Job parameter 'PASSWORD' and Network Config parameter 'evnfm_password'");
        if ($::JOB_PARAMS{'PASSWORD'} ne "") {
            $password = $::JOB_PARAMS{'PASSWORD'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_password'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_password'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'PASSWORD'} = $::NETWORK_CONFIG_PARAMS{'evnfm_password'}{'value'};
                $password = $::JOB_PARAMS{'PASSWORD'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_password' has not been set and Job parameter 'PASSWORD' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'PASSWORD' nor Network Config parameter 'evnfm_password' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper USERNAME
        General::Logging::log_user_message("Checking Job parameter 'USERNAME' and Network Config parameter 'evnfm_username'");
        if ($::JOB_PARAMS{'USERNAME'} ne "") {
            $username = $::JOB_PARAMS{'USERNAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_username'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_username'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'USERNAME'} = $::NETWORK_CONFIG_PARAMS{'evnfm_username'}{'value'};
                $username = $::JOB_PARAMS{'USERNAME'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_username' has not been set and Job parameter 'USERNAME' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'USERNAME' nor Network Config parameter 'evnfm_username' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message(
            sprintf "%s%s%s",
                $clustername ne "" ? "CLUSTERNAME=$clustername\n" : "",
                "INGRESS_HOST_ADDRESS=$ingress_host_address\n",
                "USERNAME=$username\n"
        );

        return $rc;
    }
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
sub Perform_Test_Case_P205S02 {

    my $length;
    my $message;
    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Session_P205S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Instance_P205S02T02 } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} && $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} ne "") {

        if ($::JOB_PARAMS{'SCALING_TYPE'} eq "OUT_THEN_IN") {
            # Perform a scale out then after a delay a scale in.

            #############################
            # Scale out the Application #
            #############################

            # Set start time for this repetition in case we want to have KPI verdicts for each scale operation
            $::JOB_PARAMS{'KPI_START_TIME'} = time();

            # First a scale out
            $::JOB_PARAMS{'SCALING_TYPE'} = "OUT";

            # Set the description to be used for status messages
            $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case EVNFM Scale Out (%s)", $::JOB_PARAMS{'SCALING_ASPECT_ID'};

            $message = sprintf "# Scale Out: %s #", $::JOB_PARAMS{'SCALING_ASPECT_ID'};
            $length = length($message);
            General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Scale_VNF_P205S02T03 } );
            return $rc if $rc < 0;

            if (exists $::JOB_PARAMS{'SCALE_JOB_URL'} && $::JOB_PARAMS{'SCALE_JOB_URL'} ne "") {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Monitor_Scale_Status_P205S02T04 } );
                return $rc if $rc < 0;
            }

            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Stable_Traffic_P205S02T05 } );
            return $rc if $rc < 0;

            # This call will wait for all PODs to be up and then check node health
            $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
            return $rc if $rc < 0;

            ############################
            # Scale in the Application #
            ############################

            # Set start time for this repetition in case we want to have KPI verdicts for each scale operation
            $::JOB_PARAMS{'KPI_START_TIME'} = time();

            # Next a scale in
            $::JOB_PARAMS{'SCALING_TYPE'} = "IN";

            # Set the description to be used for status messages
            $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case EVNFM Scale In (%s)", $::JOB_PARAMS{'SCALING_ASPECT_ID'};

            $message = sprintf "# Scale In: %s #", $::JOB_PARAMS{'SCALING_ASPECT_ID'};
            $length = length($message);
            General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Scale_VNF_P205S02T03 } );
            return $rc if $rc < 0;

            if (exists $::JOB_PARAMS{'SCALE_JOB_URL'} && $::JOB_PARAMS{'SCALE_JOB_URL'} ne "") {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Monitor_Scale_Status_P205S02T04 } );
                return $rc if $rc < 0;
            }

            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Stable_Traffic_P205S02T05 } );
            return $rc if $rc < 0;

            # This call will wait for all PODs to be up and then check node health
            $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
            return $rc if $rc < 0;

            # Restore the original value
            $::JOB_PARAMS{'SCALING_TYPE'} = "OUT_THEN_IN";

        } else {

            # Perform a scale out or a scale in.
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Scale_VNF_P205S02T03 } );
            return $rc if $rc < 0;

            if (exists $::JOB_PARAMS{'SCALE_JOB_URL'} && $::JOB_PARAMS{'SCALE_JOB_URL'} ne "") {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Monitor_Scale_Status_P205S02T04 } );
                return $rc if $rc < 0;
            }

            # Set the description to be used for status messages
            if ($::JOB_PARAMS{'SCALING_TYPE'} eq "OUT") {
                $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "EVNFM Scale Out (%s)", $::JOB_PARAMS{'SCALING_ASPECT_ID'};
            } else {
                $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "EVNFM Scale In (%s)", $::JOB_PARAMS{'SCALING_ASPECT_ID'};
            }

            # This call will wait for all PODs to be up and then check node health
            $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
            return $rc if $rc < 0;

        }
    }

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
    sub Create_Session_P205S02T01 {

        General::Logging::log_user_message("Creating a session");
        $::JOB_PARAMS{'SESSION_ID'} = ADP::EVNFM_Operations::create_session_id(
            {
                "debug-messages"        => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "hide-output"           => 1,
                "password"              => $::JOB_PARAMS{'PASSWORD'},
                "username"              => $::JOB_PARAMS{'USERNAME'},
            }
        );

        if ($::JOB_PARAMS{'SESSION_ID'} ne "") {
            General::Logging::log_user_message("SESSION_ID = $::JOB_PARAMS{'SESSION_ID'}");
            push @::JOB_STATUS, "(/) Create Session Id successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Failed to create a session");
            push @::JOB_STATUS, "(x) Create Session Id failed";
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
    sub Get_Instance_P205S02T02 {
        my $found = "";
        my $json_data;
        my $lines = "";
        my $instance_id = "";
        my $multiple_instances = 0;
        my $attempt = 1;
        my $max_attempts = 3;

        General::Logging::log_user_message("Get VNF instance information");

        while ($attempt < $max_attempts+1) {
            General::Logging::log_user_message("Attempt number : $attempt");
            $lines = "";
            $json_data = ADP::EVNFM_Operations::get_vnf_instance_info(
                {
                    "debug-messages"        => 1,
                    "hide-output"           => 1,
                    "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                    "instance-id"           => $instance_id,        # all instances $instance_id=""
                    "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                }
            );
            $::JOB_PARAMS{'PACKAGE_OLD_ID'} = "";
            $::JOB_PARAMS{'PACKAGE_OLD_VNFDID'} = "";
            if (ref($json_data) eq "HASH") {
                # HASH Reference
                $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                    "vnfInstanceId",
                    "vnfdId",
                    "instantiationState",
                    "vnfSoftwareVersion",
                    "vnfInstanceName",
                    "clusterName",
                    "namespace";
                $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                    "-"x36,
                    "-"x36,
                    "-"x18,
                    "-"x18,
                    "-"x36,
                    "-"x25,
                    "-"x25;

                # Look for instances where clusterName and namespace match with the wanted deployment
                # and store them in job variables (PACKAGE_VNFDINSTANCEID among others.
                for my $key (sort keys %$json_data) {
                    if (exists $json_data->{$key}{"clusterName"}) {
                        my $ns = $json_data->{$key}{"instantiatedVnfInfo"}{"vnfcResourceInfo"};
                        my $namespace = $ns->[0]{"computeResource"}{"vimLevelAdditionalResourceInfo"}{"namespace"};
                        my $sw_ver = (exists $json_data->{$key}{"vnfSoftwareVersion"} ? $json_data->{$key}{"vnfSoftwareVersion"}  : "-");
                        $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                            $key,
                            (exists $json_data->{$key}{"vnfdId"}                ? $json_data->{$key}{"vnfdId"}              : "-"),
                            (exists $json_data->{$key}{"instantiationState"}    ? $json_data->{$key}{"instantiationState"}  : "-"),
                            $sw_ver,
                            (exists $json_data->{$key}{"vnfInstanceName"}       ? $json_data->{$key}{"vnfInstanceName"}     : "-"),
                            $json_data->{$key}{"clusterName"},
                            $namespace;

                        if ((exists $json_data->{$key}{"vnfdId"}) && (exists $json_data->{$key}{"instantiationState"})) {
                            if ($json_data->{$key}{"clusterName"} eq $::JOB_PARAMS{'CLUSTERNAME'}) {
                                if ($namespace) {
                                    if ($namespace eq $::JOB_PARAMS{'SC_NAMESPACE'}) {
                                        if (!$multiple_instances) {
                                            $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} = $key;
                                            $::JOB_PARAMS{'PACKAGE_OLD_VNFDID'} = $json_data->{$key}{"vnfdId"};
                                            $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'} = $namespace;
                                            $found .= "Found instance on node $::JOB_PARAMS{'CLUSTERNAME'}.\n    vnfInstanceId: $key\n        namespace: $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'}\n          package: $sw_ver\n";
                                        } else {
                                            if ($multiple_instances == 1) {
                                                $found .= "Multiple instances found on $::JOB_PARAMS{'CLUSTERNAME'}.\n    vnfInstanceId: $key\n        namespace: $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'}\n          package: $sw_ver\n";
                                            } else {
                                                $found .= "\n    vnfInstanceId: $key\n        namespace: $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'}\n          package: $sw_ver\n";
                                            }
                                            $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID-'.$multiple_instances} = $key;
                                            $::JOB_PARAMS{'PACKAGE_OLD_VNFDID-'.$multiple_instances} = $json_data->{$key}{"vnfdId"};
                                            $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE-'.$multiple_instances} = $namespace;
                                        }
                                        $multiple_instances++;
                                        $attempt = $max_attempts;
                                    } else {
                                        $found .= "Cluster found but namespace : $namespace not same as desired: $::JOB_PARAMS{'SC_NAMESPACE'}\n";
                                    }
                                } else {
                                    $found .= "Cluster found but without namespace.\n    vnfInstanceId: $key\n        namespace: -\n          package: $sw_ver\n";
                                    $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} = $key;
                                    $::JOB_PARAMS{'PACKAGE_OLD_VNFDID'} = $json_data->{$key}{"vnfdId"};
                                    $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'} = $namespace;
                                }
                            }
                        }
                    }
                }
            } else {
                General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
                return 0;
            }
            $attempt++;
        }
        General::Logging::log_user_message("$lines\n$found");
        return 0;
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
    sub Scale_VNF_P205S02T03 {

        my $url;
        my $scaling_type = "";

        if ($::JOB_PARAMS{'SCALING_TYPE'} eq "IN") {
            $scaling_type = "SCALE_IN";
        } elsif ($::JOB_PARAMS{'SCALING_TYPE'} eq "OUT") {
            $scaling_type = "SCALE_OUT";
        }

        General::Logging::log_user_message("Scale $::JOB_PARAMS{'SCALING_TYPE'} SC VNF $::JOB_PARAMS{'SCALING_ASPECT_ID'} with $::JOB_PARAMS{'SCALING_STEPS'} steps");
        $::JOB_PARAMS{'SCALE_JOB_URL'} = ADP::EVNFM_Operations::scale(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-id"           => $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'},
                "no-monitoring"         => 1,
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                "type"                  => $scaling_type,
                "aspectid"              => $::JOB_PARAMS{'SCALING_ASPECT_ID'},
                "steps"                 => $::JOB_PARAMS{'SCALING_STEPS'},
            }
        );

        if ($::JOB_PARAMS{'SCALE_JOB_URL'} ne "") {
            General::Logging::log_user_message("Scale $::JOB_PARAMS{'SCALING_TYPE'} was successfully started");
            push @::JOB_STATUS, "(/) Scale $::JOB_PARAMS{'SCALING_TYPE'} VNF $::JOB_PARAMS{'SCALING_ASPECT_ID'} started";
            return 0;
        } else {
            General::Logging::log_user_error_message("Scale $::JOB_PARAMS{'SCALING_TYPE'} failed");
            push @::JOB_STATUS, "(x) Scale $::JOB_PARAMS{'SCALING_TYPE'} VNF $::JOB_PARAMS{'SCALING_ASPECT_ID'} failed";
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
    sub Monitor_Scale_Status_P205S02T04 {

        my $rc;

        General::Logging::log_user_message("Monitoring scaling $::JOB_PARAMS{'SCALING_TYPE'} status for max 1 hour.\nThis will take a while to complete.\n");
        $rc = ADP::EVNFM_Operations::check_operation_status(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "monitor-timeout"       => 3600,
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                "type"                  => "scaling",
                "url"                   => $::JOB_PARAMS{'SCALE_JOB_URL'},
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Scale $::JOB_PARAMS{'SCALING_TYPE'} was successful");
            push @::JOB_STATUS, "(/) Scale $::JOB_PARAMS{'SCALING_TYPE'} VNF $::JOB_PARAMS{'SCALING_ASPECT_ID'} successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Scale $::JOB_PARAMS{'SCALING_TYPE'} failed");
            push @::JOB_STATUS, "(x) Scale $::JOB_PARAMS{'SCALING_TYPE'} VNF $::JOB_PARAMS{'SCALING_ASPECT_ID'} failed";
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
    sub Wait_For_Stable_Traffic_P205S02T05 {

        my $rc = 0;

        General::Logging::log_user_message("Waiting $::JOB_PARAMS{'WAIT_TIME_BETWEEN_SCALE_OPERATIONS'} seconds after Scale $::JOB_PARAMS{'SCALING_TYPE'}");
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
            push @::JOB_STATUS, "(/) Wait after Scale $::JOB_PARAMS{'SCALING_TYPE'} was successful";
        } else {
            push @::JOB_STATUS, "(x) Wait after Scale $::JOB_PARAMS{'SCALING_TYPE'} was interrupted by the user";
            General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
            $rc = 0;
        }

        return $rc;
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
sub Perform_Post_Test_Checks_P205S03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    # We don't need to do another health check and collect logs because we
    # have already done that after the scale operation.
    # So only do KPI collection and cleanup.
    my $old_skip_post_heathcheck = $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'};
    my $old_skip_collect_logs = $::JOB_PARAMS{'SKIP_COLLECT_LOGS'};
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    # Restore old values.
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = $old_skip_post_heathcheck;
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = $old_skip_collect_logs;
    return $rc if $rc < 0;

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P205S99 {

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
    return Playlist::920_Check_Network_Config_Information::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'CLUSTERNAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name of the cluster that EVNFM will scale the SC software to, this parameter
is manadory unless the network configuration file parameter 'evnfm_clustername'
is filled with a valid value.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

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
        'INGRESS_HOST_ADDRESS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the ingress host address to the EVNFM, this parameter is mandatory unless
the network configuration file parameter 'evnfm_ingress_host_address' is filled
with a valid value.
For example: "evnfm02.eoaas.n17.sc.sero.gic.ericsson.se"
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the password to use to access EVNFM, this parameter is mandatory unless
the network configuration file parameter 'evnfm_password' is filled with a valid
value.
For example: "DefaultP12345!"
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCALING_ASPECT_ID' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The identifier of the scaling aspect.
Currently known identifiers are:

    - nlf_scaling
    - rlf_scaling
    - scp_worker_scaling
    - sepp_worker_scaling
    - slf_scaling
EOF
            'validity_mask' => '(nlf|rlf|scp_worker|sepp_worker|slf)_scaling',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCALING_STEPS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "1",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The number of steps the scale operation should use, by default scale in or out is
just done with one step.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCALING_TYPE' => {
            'case_check'    => "uppercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The type of scaling to be done, can be one of:

    - IN
      Removing VNFC instances from the VNF to release unused capacity.

    - OUT
      Adding additional VNFC instances to the VNF to increase capacity.

    - OUT_THEN_IN
      Adding additional VNFC instances to the VNF to increase capacity followed
      by a delay as specified by WAIT_TIME_BETWEEN_SCALE_OPERATIONS and then
      removing VNFC instances to release unused capacity.
      This option can be used to verify the maintainability test case for scaling
      by using EVNFM.
EOF
            'validity_mask' => '(IN|OUT|OUT_THEN_IN)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'USERNAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the user name to use to access EVNFM, this parameter is mandatory unless
the network configuration file parameter 'evnfm_username' is filled with a valid
value.
For example: "vnfm-user"
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

This Playlist scales the SC application using EVNFM.

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

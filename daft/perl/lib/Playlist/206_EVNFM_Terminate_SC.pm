package Playlist::206_EVNFM_Terminate_SC;

################################################################################
#
#  Author   : eedwiq, eustone
#
#  Revision : 1.8
#  Date     : 2024-03-26 15:42:49
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
    usage
    usage_return_playlist_variables
    validate_network_config_parameter
    );

use ADP::EVNFM_Operations;
use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::920_Check_Network_Config_Information;

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
    $::JOB_PARAMS{'JOBTYPE'} = "EVNFM_TERMINATE";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (General::Playlist_Operations::is_mandatory_network_config_loaded() != 0) {
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P206S01, \&Fallback001_P206S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P206S02, \&Fallback001_P206S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Terminate_SC_Via_EVNFM_P206S06, \&Fallback001_P206S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_P206S09, \&Fallback001_P206S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P206S10, \&Fallback001_P206S99 );
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
sub Initialize_Job_Environment_P206S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::901_Initialize_Job_Environment::main } );
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
sub Check_Job_Parameters_P206S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P206S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
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
    sub Check_Job_Parameters_P206S02T01 {

        my $clustername = "";
        my $ingress_host_address = "";
        my $password = "";
        my $sc_namespace = "";
        my $rc = 0;
        my @result;
        my $username = "";

        # Get the proper SC_NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'SC_NAMESPACE' and Network Config parameter 'sc_namespace'");
        if ($::JOB_PARAMS{'SC_NAMESPACE'} ne "") {
            $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'SC_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'};
                $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'sc_namespace' has not been set and Job parameter 'SC_NAMESPACE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'SC_NAMESPACE', nor Network Config parameter 'sc_namespace' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper CLUSTERNAME and CONFIG_FILE_INSTANTIATE_VNF_REQUEST
        General::Logging::log_user_message("Checking Job parameter 'CLUSTERNAME' and 'CONFIG_FILE_INSTANTIATE_VNF_REQUEST' and Network Config parameter 'evnfm_clustername'");
        if ($::JOB_PARAMS{'CLUSTERNAME'} ne "") {
            $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'CLUSTERNAME'} = $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'};
                $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
            } elsif ($::JOB_PARAMS{'CONFIG_FILE_INSTANTIATE_VNF_REQUEST'} eq "") {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_clustername' has not been set and Job parameter 'CLUSTERNAME' or 'CONFIG_FILE_INSTANTIATE_VNF_REQUEST' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'CLUSTERNAME' nor 'CONFIG_FILE_INSTANTIATE_VNF_REQUEST' or Network Config parameter 'evnfm_clustername' has been set");
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
            sprintf "SC_NAMESPACE=$sc_namespace\n%s%s%s%s",
                $clustername ne "" ? "CLUSTERNAME=$clustername\n" : "",
                $::JOB_PARAMS{'CONFIG_FILE_INSTANTIATE_VNF_REQUEST'} ne "" ? $::JOB_PARAMS{'CONFIG_FILE_INSTANTIATE_VNF_REQUEST'} : "",
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
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Terminate_SC_Via_EVNFM_P206S06 {

    my $rc;
    my $skip_rest=0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Session_P206S06T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&List_Packages_P206S06T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Instance_P206S06T03 } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} && $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} ne "") {
        General::Logging::log_user_message("Terminate : ".$::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'});
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Terminate_VNF_P206S06T04 } );
        return $rc if $rc < 0;

        if (exists $::JOB_PARAMS{'TERMINATE_JOB_URL'} && $::JOB_PARAMS{'TERMINATE_JOB_URL'} ne "") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Monitor_Termination_Status_P206S06T05 } );
            return $rc if $rc < 0;
        }
    } else {
        General::Logging::log_user_message("No vnfInstanceId found matching cluster name and namespace, so no need to terminate any deployment");
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_Namespace_P206S06T06 } );
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
    sub Create_Session_P206S06T01 {

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
    sub List_Packages_P206S06T02 {

        my $json_data;
        my $lines = "";
        my %eligable_for_delete;

        General::Logging::log_user_message("Get VNF package information");
        $json_data = ADP::EVNFM_Operations::get_vnf_package_info(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "package-id"            => "",  # All packages
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );
        if (ref($json_data) eq "HASH") {
            # HASH Reference
            $lines .= sprintf "%-36s  %-36s  %-25s  %-18s  %-25s  %-15s  %-16s  %-10s\n",
                "vnfPkgId",
                "vnfdId",
                "vnfProductName",
                "vnfSoftwareVersion",
                "vnfdVersion",
                "onboardingState",
                "operationalState",
                "usageState";
            $lines .= sprintf "%-36s  %-36s  %-25s  %-18s  %-25s  %-15s  %-16s  %-10s\n",
                "-"x36,
                "-"x36,
                "-"x25,
                "-"x18,
                "-"x25,
                "-"x15,
                "-"x16,
                "-"x10;
            for my $key (sort keys %$json_data) {
                $lines .= sprintf "%-36s  %-36s  %-25s  %-18s  %-25s  %-15s  %-16s  %-10s\n",
                    $key,
                    (exists $json_data->{$key}{"vnfdId"}                ? $json_data->{$key}{"vnfdId"}              : "-"),
                    (exists $json_data->{$key}{"vnfProductName"}        ? $json_data->{$key}{"vnfProductName"}      : "-"),
                    (exists $json_data->{$key}{"vnfSoftwareVersion"}    ? $json_data->{$key}{"vnfSoftwareVersion"}  : "-"),
                    (exists $json_data->{$key}{"vnfdVersion"}           ? $json_data->{$key}{"vnfdVersion"}         : "-"),
                    (exists $json_data->{$key}{"onboardingState"}       ? $json_data->{$key}{"onboardingState"}     : "-"),
                    (exists $json_data->{$key}{"operationalState"}      ? $json_data->{$key}{"operationalState"}    : "-"),
                    (exists $json_data->{$key}{"usageState"}            ? $json_data->{$key}{"usageState"}          : "-");

                if ((exists $json_data->{$key}{"usageState"}) && ($json_data->{$key}{"usageState"} eq "NOT_IN_USE")) {
                    if (!exists $eligable_for_delete{$key}) {
                        $eligable_for_delete{$key}{"COUNT"} = 1;
                    } else {
                        $eligable_for_delete{$key}{"COUNT"} += 1;
                    }
                    $eligable_for_delete{$key}{"PRODUCT"} = $json_data->{$key}{"vnfProductName"};
                    $eligable_for_delete{$key}{"SOFTWARE-VERSION"} = $json_data->{$key}{"vnfSoftwareVersion"};
                }
            }
        } else {
            General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
            return 0;
        }

        General::Logging::log_user_message($lines);
        General::Logging::log_user_message("Eligable for deletion:");
        foreach my $package_to_delete (keys %eligable_for_delete) {
            if ($eligable_for_delete{$package_to_delete}{"PRODUCT"} eq "Signaling_Controller") {
                General::Logging::log_user_message(sprintf "%20s   Occurrences: %i", $eligable_for_delete{$package_to_delete}{"SOFTWARE-VERSION"}, $eligable_for_delete{$package_to_delete}{"COUNT"});
            }
        }

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
    sub Get_Instance_P206S06T03 {
        my $found = "";
        my $json_data;
        my $lines = "";
        my $instance_id = "";

        General::Logging::log_user_message("Get VNF instance information");

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
        $::JOB_PARAMS{'PACKAGE_ID'} = "";
        $::JOB_PARAMS{'PACKAGE_VNFDID'} = "";
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
            my $this_cluster = "";
            for my $key (sort keys %$json_data) {
                if (exists $json_data->{$key}{"clusterName"}) {
                    $this_cluster = $json_data->{$key}{"clusterName"};
                    my $vnfcResourceInfo_ref    = $json_data->{$key}{"instantiatedVnfInfo"}{"vnfcResourceInfo"};
                    my $vnfstate                = $json_data->{$key}{"instantiatedVnfInfo"}{"vnfState"};
                    my $vnfcResourceInfo_entry  = 0;
                    my $previous_namespace      = "";
                    my $exit_whileloop          = 0;
                    my $found_match             = " ";
                    # going through all entries in the vnf resource info
                    while (exists $vnfcResourceInfo_ref->[$vnfcResourceInfo_entry]) {
                        my $namespace = $vnfcResourceInfo_ref->[$vnfcResourceInfo_entry]{"computeResource"}{"vimLevelAdditionalResourceInfo"}{"namespace"};
                        if ($namespace ne $previous_namespace) {
                            $previous_namespace = $namespace;
                            my $skip_redundant_evnfm_info = 0;
                            my $sw_ver = (exists $json_data->{$key}{"vnfSoftwareVersion"} ? $json_data->{$key}{"vnfSoftwareVersion"}  : "-");
                            if ((exists $json_data->{$key}{"vnfdId"}) && (exists $json_data->{$key}{"instantiationState"})) {
                                if ($this_cluster eq $::JOB_PARAMS{'CLUSTERNAME'}) {
                                    if ($namespace eq $::JOB_PARAMS{'SC_NAMESPACE'}) {
                                            $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} = $key;
                                            $::JOB_PARAMS{'PACKAGE_VNFDID'} = $json_data->{$key}{"vnfdId"};
                                            $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'} = $namespace;
                                            $found .= "Found instance on node $::JOB_PARAMS{'CLUSTERNAME'}\n    vnfInstanceId: $key\n        namespace: $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'}\n          package: $sw_ver\n";
                                        $exit_whileloop = 1;
                                        $found_match = "*";
                                    } else {
                                        $skip_redundant_evnfm_info = 0;
                                    }
                                } # if ($this_cluster eq $::JOB_PARAMS{'CLUSTERNAME'})
                            } # if ((exists $json_data->{$key}{"vnfdId"}) && (exists $json_data->{$key}{"instantiationState"}))
                            if ((!$skip_redundant_evnfm_info) && (exists $json_data->{$key}{"instantiationState"})){
                                $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s %1s\n",
                                    $key,
                                    (exists $json_data->{$key}{"vnfdId"}                ? $json_data->{$key}{"vnfdId"}              : "-"),
                                    (exists $json_data->{$key}{"instantiationState"}    ? $json_data->{$key}{"instantiationState"}  : "-"),
                                    $sw_ver,
                                    (exists $json_data->{$key}{"vnfInstanceName"}       ? $json_data->{$key}{"vnfInstanceName"}     : "-"),
                                    $this_cluster,
                                    $namespace,
                                    $found_match;
                            }
                        } # if ($namespace ne $previous_namespace)
                        if ($exit_whileloop) {
                            last; # put this here because the variable $lines needs would otherwise either need to be used at 2 places OR the last entry would not be added to the printout
                        }
                        $vnfcResourceInfo_entry++;
                    } # while (exists $vnfcResourceInfo_ref->[$vnfcResourceInfo_entry])
                }
            }
        } else {
            General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
            return 0;
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
    sub Terminate_VNF_P206S06T04 {

        my $url;

        General::Logging::log_user_message("Terminate SC VNF");

        my $parameter = "CONFIG_FILE_INSTANTIATE_VNF_REQUEST";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/instantiateVnfRequest.json";
        my $rc = 0;

        if (exists $::JOB_PARAMS{'CLEANUP_RESOURCES'}) {
            if (uc $::JOB_PARAMS{'CLEANUP_RESOURCES'} eq "NO") {
                $::JOB_PARAMS{'CLEANUP_RESOURCES'} = "false";
            } else {
                $::JOB_PARAMS{'CLEANUP_RESOURCES'} = "true";
            }
        } else {
            $::JOB_PARAMS{'CLEANUP_RESOURCES'} = "true";
        }

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            General::Logging::log_user_warning_message("Task skipped because DEBUG_PLAYLIST=yes");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        $::JOB_PARAMS{'TERMINATE_JOB_URL'} = ADP::EVNFM_Operations::terminate_vnf(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-id"           => $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'},
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                "terminate-method"      => $::JOB_PARAMS{'TERMINATE_METHOD'},
                "cleanup-resources"     => $::JOB_PARAMS{'CLEANUP_RESOURCES'},
            }
        );

        if ($::JOB_PARAMS{'TERMINATE_JOB_URL'} ne "") {
            if (index($::JOB_PARAMS{'TERMINATE_JOB_URL'},"FAILED:") != -1) {
                if (index($::JOB_PARAMS{'TERMINATE_JOB_URL'},"409") != -1) {
                    General::Logging::log_user_message("This resource is not in the INSTANTIATED state, try deleting the instance");
                    $rc = ADP::EVNFM_Operations::delete_vnf_instance_id(
                        {
                            "debug-messages"        => 1,
                            "hide-output"           => 1,
                            "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                            "instance-id"           => $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'},
                            "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                        }
                    );
                    if ($rc == 0) {
                        $::JOB_PARAMS{'TERMINATE_JOB_URL'} = "";
                        return 0;
                    }
                }
                General::Logging::log_user_error_message("Termination failed");
                push @::JOB_STATUS, "(x) Terminate VNF failed";
                return 1;

            }
            General::Logging::log_user_message("Termination was successfully started");
            push @::JOB_STATUS, "(/) Terminate VNF started";
            return 0;
        } else {
            General::Logging::log_user_error_message("Termination failed");
            push @::JOB_STATUS, "(x) Terminate VNF failed";
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
    sub Monitor_Termination_Status_P206S06T05 {

        my $rc;

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            General::Logging::log_user_warning_message("Task skipped because DEBUG_PLAYLIST=yes");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Monitoring termination status for max 1 hour.\nThis will take a while to complete.\n");
        $rc = ADP::EVNFM_Operations::check_operation_status(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "monitor-timeout"       => 3600,
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                "url"                   => $::JOB_PARAMS{'TERMINATE_JOB_URL'},
                "type"                  => "termination",
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Termination was successful");
            push @::JOB_STATUS, "(/) Terminate VNF successful";

            General::Logging::log_user_message("Also trying to delete the vnfInstanceId");
            $rc = ADP::EVNFM_Operations::delete_vnf_instance_id(
                {
                    "debug-messages"        => 1,
                    "hide-output"           => 1,
                    "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                    "instance-id"           => $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'},
                    "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("vnfInstanceId successfully deleted");
                push @::JOB_STATUS, "(/) Removal of vnfInstanceId successful";
            } else {
                push @::JOB_STATUS, "(x) Removal of vnfInstanceId failed";
                General::Logging::log_user_warning_message("vnfInstanceId could not be deleted, but ignored");
            }
            return 0;
        } else {
            General::Logging::log_user_error_message("Termination failed");
            push @::JOB_STATUS, "(x) Terminate VNF failed";
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
    sub Delete_Namespace_P206S06T06 {
        my $rc;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $log_shipper             = "eric-log-shipper-".$::JOB_PARAMS{'SC_NAMESPACE'};
        my $cmd_check_log_shipper   = "get ClusterRole -o wide | grep $log_shipper";
        my $cmd_delete_log_shipper  = "delete ClusterRole $log_shipper";

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            General::Logging::log_user_warning_message("Task skipped because DEBUG_PLAYLIST=yes");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Remove SC '$sc_namespace' namespace.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete namespace $sc_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Removal of Namespace successful";
        } elsif ($result[0] =~ /Error from server \(NotFound\): namespaces "\S+" not found/) {
            push @::JOB_STATUS, "(-) Removal of Namespace not needed, already removed";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to delete SC namespace");
            push @::JOB_STATUS, "(x) Removal of Namespace failed";
        }

        # check if eric-log-shipper still present
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} $cmd_check_log_shipper",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if (index($result[0],$log_shipper) != -1) {
            # eric-slog-shipper still present, delete it
            General::Logging::log_user_message("Deleting $log_shipper");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} $cmd_delete_log_shipper",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
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
sub Collect_Logs_P206S09 {

    my $rc = 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_AT_SUCCESS'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Collection of logs at successful job not wanted");
    }

    return $rc;
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
sub Cleanup_Job_Environment_P206S10 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if ($rc == 0) {
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
sub Fallback001_P206S99 {

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
sub set_playlist_variables {
    %playlist_variables = (

        # ---------------------------------------------------------------------
        'CLEANUP_RESOURCES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if cleanup of resources should be done (=yes) or not
(=no).
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CLUSTERNAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name of the cluster that EVNFM will deploy the SC software to, this parameter
is manadory unless the network configuration file parameter 'evnfm_clustername'
is filled with a valid value or a special configuration file is specified with
job parameter 'CONFIG_FILE_INSTANTIATE_VNF_REQUEST'.
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
        'COLLECT_LOGS_AT_SUCCESS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if detailed log files should be collected at successful
job termination.
Normally detailed logs are only collected when the job fails to aid with trouble
shooting efforts.

If value is 'yes' when the playlist finish with an success then detailed ADP
logs and other information will be collected and stored in the all.log or under
the troubleshooting_logs sub directory of the job.
The default value is 'no' if the variable is not specified.

If value is 'no' then no collection of detailed logs will be done at successful
job termination.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'HELM_VERSION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The version of 'helm' command to use if you want to override the parameter from
the network configuration file called 'helm_version'.
If you don't specify this parameter then it will take the value from the network
configuration file.
Valid values are 2 (for helm 2) or 3 (for helm 3).
EOF
            'validity_mask' => '(2|3)',
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
        'KUBECONFIG' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then this file will be used as the config file for all helm and
kubectl commands.
If you don't specify this parameter then it will take the default file which
is usually called ~/.kube/config.
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
        'SC_NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name to use for the SC namespace if you want to override the parameter from
the network configuration file called 'sc_namespace'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TERMINATE_METHOD' => {
            'case_check'    => "uppercase", # <lowercase|no|uppercase>
            'default_value' => "GRACEFUL",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the method to use for the terminate.
EOF
            'validity_mask' => '(GRACEFUL|FORCEFUL)',
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
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist Terminates the SC application using EVNFM.

Used Job Parameters:
====================
EOF
    General::Playlist_Operations::print_info_about_job_variables(\%playlist_variables);
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return %playlist_variables;
}

# -----------------------------------------------------------------------------
# This subroutine validates each network configuration file parameters to see
# if it is valid in regards to some job parameters.
#
sub validate_network_config_parameter {
    my $hash_ref = shift;

    return Playlist::920_Check_Network_Config_Information::validate_network_config_parameter($hash_ref);
}

1;

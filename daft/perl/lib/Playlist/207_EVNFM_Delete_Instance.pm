package Playlist::207_EVNFM_Delete_Instance;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.3
#  Date     : 2023-10-12 11:45:08
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

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    usage_return_playlist_variables
    );

use ADP::EVNFM_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;

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
    my $rc;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "DELETE_EVNFM_INSTANCE";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (General::Playlist_Operations::is_optional_network_config_loaded() != 0) {
        General::Logging::log_user_warning_message("No network config file is loaded, any required input must be specified as job variables");
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P207S01, \&Fallback001_P207S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P207S02, \&Fallback001_P207S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&List_And_Delete_EVNFM_Instances_P207S03, \&Fallback001_P207S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P207S04, \&Fallback001_P207S99 );
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
sub Initialize_Job_Environment_P207S01 {

    my $rc;

    # This playlist does not use kubectl or helm commands to skip this check
    $::JOB_PARAMS{'SKIP_HELM_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_KUBECTL_CHECK'} = "yes";

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
sub Check_Job_Parameters_P207S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P207S02T01 } );
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
    sub Check_Job_Parameters_P207S02T01 {

        my $clustername = "";
        my $ingress_host_address = "";
        my $password = "";
        my $rc = 0;
        my $sc_namespace = "";
        my $sc_release_name = "";
        my $username = "";

        # Get the proper CLUSTERNAME
        General::Logging::log_user_message("Checking Job parameter 'CLUSTERNAME' and Network Config parameter 'evnfm_clustername'");
        if ($::JOB_PARAMS{'CLUSTERNAME'} ne "") {
            $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'} && $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'} ne "CHANGEME") {
            $::JOB_PARAMS{'CLUSTERNAME'} = $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'};
            $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
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

        # Get the proper SC_NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'SC_NAMESPACE' and Network Config parameter 'sc_namespace'");
        if ($::JOB_PARAMS{'SC_NAMESPACE'} ne "") {
            $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} && $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} ne "CHANGEME") {
            $::JOB_PARAMS{'SC_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'};
            $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        }

        # Get the proper SC_RELEASE_NAME
        General::Logging::log_user_message("Checking Job parameter 'SC_RELEASE_NAME' and Network Config parameter 'sc_release_name'");
        if ($::JOB_PARAMS{'SC_RELEASE_NAME'} ne "") {
            $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'} && $::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'} ne "CHANGEME") {
            $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'};
            $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
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
            sprintf "%s%s%s%s%s",
                "CLUSTERNAME=$clustername\n",
                "INGRESS_HOST_ADDRESS=$ingress_host_address\n",
                "SC_NAMESPACE=$sc_namespace\n",
                "SC_RELEASE_NAME=$sc_release_name\n",
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
sub List_And_Delete_EVNFM_Instances_P207S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Session_P207S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&List_Instances_P207S03T02 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} ne "") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_VNF_Instance_P207S03T03 } );
        return $rc if $rc < 0;
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
    sub Create_Session_P207S03T01 {

        General::Logging::log_user_message("Creating a SESSION_ID");
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
            return 0;
        } else {
            General::Logging::log_user_error_message("Failed to create a session");
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
    sub List_Instances_P207S03T02 {

        my $json_data;
        my $length;
        my $lines = "";
        my $match_count = $::JOB_PARAMS{'MATCH_COUNT'};
        my %matches;
        my $maxlength_vnfProductName = 14;
        my $maxlength_vnfSoftwareVersion = 18;
        my $maxlength_vnfdVersion = 11;
        my $maxlength_vnfInstanceName = 18;
        my $maxlength_clusterName = 11;
        my $maxlength_namespace = 9;

        my $our_cluster = $::JOB_PARAMS{'CLUSTERNAME'};
        my $our_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $our_release = $::JOB_PARAMS{'SC_RELEASE_NAME'};

        General::Logging::log_user_message("Get VNF instance information");
        $json_data = ADP::EVNFM_Operations::get_vnf_instance_info(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-id"           => "",  # All instances
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );
        if (ref($json_data) eq "HASH") {
            # HASH Reference

            # Calculate column lengths
            for my $key (keys %$json_data) {
                if (exists $json_data->{$key}{"vnfProductName"}) {
                    $length = length($json_data->{$key}{"vnfProductName"});
                    $maxlength_vnfProductName = $length if $length > $maxlength_vnfProductName;
                }
                if (exists $json_data->{$key}{"vnfSoftwareVersion"}) {
                    $length = length($json_data->{$key}{"vnfSoftwareVersion"});
                    $maxlength_vnfSoftwareVersion = $length if $length > $maxlength_vnfSoftwareVersion;
                }
                if (exists $json_data->{$key}{"vnfdVersion"}) {
                    $length = length($json_data->{$key}{"vnfdVersion"});
                    $maxlength_vnfdVersion = $length if $length > $maxlength_vnfdVersion;
                }
                if (exists $json_data->{$key}{"vnfInstanceName"}) {
                    $length = length($json_data->{$key}{"vnfInstanceName"});
                    $maxlength_vnfInstanceName = $length if $length > $maxlength_vnfInstanceName;
                }
                if (exists $json_data->{$key}{"clusterName"}) {
                    $length = length($json_data->{$key}{"clusterName"});
                    $maxlength_clusterName = $length if $length > $maxlength_clusterName;
                }
                if (exists $json_data->{$key}{"namespace"}) {
                    $length = length($json_data->{$key}{"namespace"});
                    $maxlength_namespace = $length if $length > $maxlength_namespace;
                }
            }

            # Display data
            $lines .= sprintf "%-36s  %-36s  %-${maxlength_vnfProductName}s  %-${maxlength_vnfSoftwareVersion}s  %-${maxlength_vnfdVersion}s  %-18s  %-${maxlength_vnfInstanceName}s  %-${maxlength_clusterName}s  %-${maxlength_namespace}s\n",
                "vnfInstanceId",
                "vnfdId",
                "vnfProductName",
                "vnfSoftwareVersion",
                "vnfdVersion",
                "instantiationState",
                "vnfInstanceName",
                "clusterName",
                "namespace";
            $lines .= sprintf "%-36s  %-36s  %-${maxlength_vnfProductName}s  %-${maxlength_vnfSoftwareVersion}s  %-${maxlength_vnfdVersion}s  %-18s  %-${maxlength_vnfInstanceName}s  %-${maxlength_clusterName}s  %-${maxlength_namespace}s\n",
                "-"x36,
                "-"x36,
                "-"x$maxlength_vnfProductName,
                "-"x$maxlength_vnfSoftwareVersion,
                "-"x$maxlength_vnfdVersion,
                "-"x18,
                "-"x$maxlength_vnfInstanceName,
                "-"x$maxlength_clusterName,
                "-"x$maxlength_namespace;
            for my $key (sort keys %$json_data) {
                my $vnfcResourceInfo_ref    = $json_data->{$key}{"instantiatedVnfInfo"}{"vnfcResourceInfo"};
                my $namespace = "";
                my $match = "";

                # Find the namespace
                if ($json_data->{$key}{"instantiationState"} eq "INSTANTIATED") {
                    $namespace = find_namespace($vnfcResourceInfo_ref, $key);
                } else {
                    $namespace = "";
                }

                # Check what information match
                if ($our_cluster eq $json_data->{$key}{"clusterName"}) {
                    $match .= "Cluster Name ($our_cluster) and ";
                    $matches{$key}{'count'}++;
                }
                if ($our_release eq $json_data->{$key}{"vnfInstanceName"}) {
                    $match .= "Instance Name ($our_release) and ";
                    $matches{$key}{'count'}++;
                }
                if ($namespace =~ /(?:^|\W)$our_namespace(?:$|\W)/) {
                    $match .= "Namespace ($our_namespace) and ";
                    $matches{$key}{'count'}++;
                }
                $match =~ s/ and $//;
                if ($match ne "") {
                    $match .= " match";
                    $matches{$key}{'string'} = $match;
                }

                $lines .= sprintf "%-36s  %-36s  %-${maxlength_vnfProductName}s  %-${maxlength_vnfSoftwareVersion}s  %-${maxlength_vnfdVersion}s  %-18s  %-${maxlength_vnfInstanceName}s  %-${maxlength_clusterName}s  %s\n",
                    $key,
                    (exists $json_data->{$key}{"vnfdId"}                ? $json_data->{$key}{"vnfdId"}              : "-"),
                    (exists $json_data->{$key}{"vnfProductName"}        ? $json_data->{$key}{"vnfProductName"}      : "-"),
                    (exists $json_data->{$key}{"vnfSoftwareVersion"}    ? $json_data->{$key}{"vnfSoftwareVersion"}  : "-"),
                    (exists $json_data->{$key}{"vnfdVersion"}           ? $json_data->{$key}{"vnfdVersion"}         : "-"),
                    (exists $json_data->{$key}{"instantiationState"}    ? $json_data->{$key}{"instantiationState"}  : "-"),
                    (exists $json_data->{$key}{"vnfInstanceName"}       ? $json_data->{$key}{"vnfInstanceName"}     : "-"),
                    (exists $json_data->{$key}{"clusterName"}           ? $json_data->{$key}{"clusterName"}         : "-"),
                    $namespace;
            }

            if (%matches) {
                $lines .= "\nInstance Id's that match $match_count or more network config file or user input data:\n\n";
                $lines .= sprintf "%-36s  %14s  %-20s\n", "vnfInstanceId", "Matching Count", "Matching Information";
                $lines .= sprintf "%-36s  %14s  %-20s\n", "-"x36, "-"x14, "-"x20;
                for my $key (sort keys %matches) {
                    if ($matches{$key}{'count'} >= $match_count) {
                        $lines .= sprintf "%-36s  %14d  %s\n", $key, $matches{$key}{'count'}, $matches{$key}{'string'};
                    }
                }
            } else {
                $lines .= "\nThere are no Instance Id's that match one or more network config file or user input data\n";
            }

            General::Logging::log_user_message($lines);

            while (1) {
                if ($::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} eq "") {
                    General::Logging::log_user_warning_message("No VNFINSTANCEID_TO_DELETE specified, no deletion will be done.");
                    return General::Playlist_Operations::RC_STEPOUT;
                } elsif ($::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} =~ /^ask$/i) {
                    General::Logging::log_user_prompt("Type 'vnfInstanceId' to delete or an empty string to exit: ");
                    my $answer = General::OS_Operations::get_user_input("", 0);   # No prompt, `0=Not hidden
                    if (defined $answer) {
                        $::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} = $answer;
                    } else {
                        General::Logging::log_user_error_message("No terminal input possible.");
                        return General::Playlist_Operations::RC_FALLBACK;
                    }
                } elsif (exists $json_data->{$::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'}}) {
                    if ($json_data->{$::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'}}{'instantiationState'} eq "INSTANTIATED") {
                        General::Logging::log_user_warning_message("vnfInstanceId $::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} in state INSTANTIATED, you need to terminate it first.");
                        $::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} = "ask";
                    } else {
                        General::Logging::log_user_warning_message("Attempt to delete vnfInstanceId $::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} will be done.");
                        last;
                    }
                } else {
                    General::Logging::log_user_warning_message("vnfInstanceId $::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} does not exist, check the value again.");
                    $::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'} = "ask";
                }
            }
        } else {
            General::Logging::log_user_message("Either no instances to print or something went wrong when fetching instance information");
            return General::Playlist_Operations::RC_STEPOUT;
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
    sub Delete_VNF_Instance_P207S03T03 {

        General::Logging::log_user_message("Delete vnfInstanceId $::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'}");
        my $result = ADP::EVNFM_Operations::delete_vnf_instance_id(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-id"           => $::JOB_PARAMS{'VNFINSTANCEID_TO_DELETE'},
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );

        if ($result == 0) {
            General::Logging::log_user_message("VNF instance id was deleted");
            return 0;
        } else {
            General::Logging::log_user_error_message("Failed to delete VNF instance id");
            return 1;
        }
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
sub Cleanup_Job_Environment_P207S04 {

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
sub Fallback001_P207S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

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
sub find_namespace {
    my $vnfcResourceInfo_ref = shift;
    my $vnfdInstanceId = shift;
    my %namespaces;

CHECK_AGAIN:
    if (scalar @{$vnfcResourceInfo_ref} > 0) {
        # We have found resource info data, now find the namespace(s)
        for (my $vnfcResourceInfo_entry=0; $vnfcResourceInfo_entry < scalar @{$vnfcResourceInfo_ref}; $vnfcResourceInfo_entry++) {
            # Count all found namespaces
            if (exists $vnfcResourceInfo_ref->[$vnfcResourceInfo_entry]{"computeResource"} &&
                exists $vnfcResourceInfo_ref->[$vnfcResourceInfo_entry]{"computeResource"}{"vimLevelAdditionalResourceInfo"} &&
                exists $vnfcResourceInfo_ref->[$vnfcResourceInfo_entry]{"computeResource"}{"vimLevelAdditionalResourceInfo"}{"namespace"} &&
                $vnfcResourceInfo_ref->[$vnfcResourceInfo_entry]{"computeResource"}{"vimLevelAdditionalResourceInfo"}{"namespace"} ne "") {

                if ($vnfcResourceInfo_ref->[$vnfcResourceInfo_entry]{"computeResource"}{"vimLevelAdditionalResourceInfo"}{"status"} eq "Running") {
                    # Only count Running resources
                    $namespaces{$vnfcResourceInfo_ref->[$vnfcResourceInfo_entry]{"computeResource"}{"vimLevelAdditionalResourceInfo"}{"namespace"}}++;
                }
            }
        }
    } elsif ($vnfdInstanceId ne "") {
        my $json_data = ADP::EVNFM_Operations::get_vnf_instance_info(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-id"           => $vnfdInstanceId,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );
        if (ref($json_data) eq "HASH") {
            # HASH Reference
            $vnfcResourceInfo_ref    = $json_data->{$vnfdInstanceId}{"instantiatedVnfInfo"}{"vnfcResourceInfo"};
            #use Data::Dumper;
            #print "DBG:$vnfdInstanceId\n", Dumper($vnfcResourceInfo_ref);
            $vnfdInstanceId = "";
            goto CHECK_AGAIN;
        }
    }

    if (scalar keys %namespaces == 0) {
        return "";
    } else {
        #my $namespace = "";
        #for my $key (keys %namespaces) {
        #    $namespace .= "$key ($namespaces{$key}),";
        #}
        #return $namespace;
        return join ",", keys %namespaces;
    }
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
The name of the cluster that EVNFM have used for deploying the SC software to,
this parameter is used when showing matching instance id's and if not specified
then it uses the network configuration file parameter 'evnfm_clustername'
if filled with a valid value.
EOF
            'validity_mask' => '.+',
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
        'MATCH_COUNT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "3",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies how many items that must match before a instance id is printed as
matching.
Information that is checked are:
    - Cluster Name
    - Instance Name (Release Name)
    - Namespace
By default all 3 must match before the instance id is printed as matching, but
if this value is lowered then also other instance id's that match will also be
printed as matching.
EOF
            'validity_mask' => '(1|2|3)',
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
This parameter is used when showing matching instance id's.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SC_RELEASE_NAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name to use for the SC release name if you want to override the parameter
from the network configuration file called 'sc_release_name'.
If you don't specify this parameter then it will take the value from the network
configuration file.
This parameter is used when showing matching instance id's.
EOF
            'validity_mask' => '.+',
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
        'VNFINSTANCEID_TO_DELETE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the VNF Instance Id in EVNFM to delete.
If specified as 'ask' then the playlist execution stops waiting for the user to
provide a valid id that is not in INSTANTIATED state.
If specified as an empty string or not specified at all then no deletion will be
done.

For example: 24a6f003-0990-4591-a7f3-3314b97731f5
             ask
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist prints information about the installed instances from EVNFM.

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

1;

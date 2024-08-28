package Playlist::938_Node_Information;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.5
#  Date     : 2024-06-18 07:58:52
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2024
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
    replace_place_holders_in_array
    replace_place_holders_in_file
    usage
    usage_return_playlist_variables
    );

#
# Used Perl package files
#

use File::Basename qw(dirname basename);

use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;
use General::String_Operations;

#
# Variable Declarations
#

our %playlist_variables;
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

    my $rc = General::Playlist_Operations::RC_TASKOUT;

    # Set default values, if needed
    $::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'}       = "no"  unless (exists $::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'});
    $::JOB_PARAMS{'P938_GET_TOOLS_NODE_INFORMATION'}    = "no"  unless (exists $::JOB_PARAMS{'P938_GET_TOOLS_NODE_INFORMATION'});
    $::JOB_PARAMS{'P938_INPUT_FILE_NAME'}               = ""    unless (exists $::JOB_PARAMS{'P938_INPUT_FILE_NAME'});
    $::JOB_PARAMS{'P938_OUTPUT_FILE_ACCESSMODE'}        = "666" unless (exists $::JOB_PARAMS{'P938_OUTPUT_FILE_ACCESSMODE'});
    $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'}              = ""    unless (exists $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'});
    $::JOB_PARAMS{'P938_REPLACE_FILE_PLACEHOLDERS'}     = "no"  unless (exists $::JOB_PARAMS{'P938_REPLACE_FILE_PLACEHOLDERS'});

    # Set tasks to always execute
    General::State_Machine::always_execute_task('Playlist::938_Node_Information.+');

    if ($::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'} eq "yes" || $::JOB_PARAMS{'P938_GET_TOOLS_NODE_INFORMATION'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Get_Node_Specific_Information_P938S01, \&Fallback001_P938S99 );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'P938_REPLACE_FILE_PLACEHOLDERS'} eq "yes" && $::JOB_PARAMS{'P938_INPUT_FILE_NAME'} ne "" && $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'} ne "") {
        $rc = General::Playlist_Operations::execute_step( \&Replace_File_Place_Holders_P938S02, \&Fallback001_P938S99 );
        return $rc if $rc < 0;
    }

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
sub Get_Node_Specific_Information_P938S01 {

    my $rc = 0;

    if ($::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_SC_Node_Specific_Information_P938S01T01 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'} eq "yes" || $::JOB_PARAMS{'P938_GET_TOOLS_NODE_INFORMATION'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Tools_Node_Specific_Information_P938S01T02 } );
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
    sub Get_SC_Node_Specific_Information_P938S01T01 {

        my @all_ports;
        my $message;
        my @nodeports;
        my @ports;
        my $rc = 0;
        my @result;
        my $svc_cluster_ip;
        my $svc_external_ip;
        my $svc_ip;
        my $svc_name;
        my $svc_ports;
        my $svc_type;

        # Initialize node specific variables
        for my $placeholder ( qw/
            BSF_WRKR_IP
            BSF_WRKR_NODEPORT
            BSF_WRKR_PORT
            BSF_WRKR_TLS_NODEPORT
            BSF_WRKR_TLS_PORT
            CMYP_CLI_NODEPORT
            CMYP_CLI_PORT
            CMYP_IP
            CMYP_NETCONF_NODEPORT
            CMYP_NETCONF_PORT
            CMYP_NETCONF_TLS_NODEPORT
            CMYP_NETCONF_TLS_PORT
            CSA_WRKR_IP
            CSA_WRKR_NODEPORT
            CSA_WRKR_PORT
            CSA_WRKR_TLS_NODEPORT
            CSA_WRKR_TLS_PORT
            FIRST_WORKER_IP
            NODE_TRAFFIC_IP
            NODE_TRAFFIC_IPV6
            SCP_WRKR_IP
            SCP_WRKR_NODEPORT
            SCP_WRKR_PORT
            SCP_WRKR_TLS_NODEPORT
            SCP_WRKR_TLS_PORT
            SEPP_WRKR_IP
            SEPP_WRKR_NODEPORT
            SEPP_WRKR_PORT
            SEPP_WRKR_TLS_NODEPORT
            SEPP_WRKR_TLS_PORT
            VIP_SIG_BSF
            VIP_SIG_SCP
            VIP_SIG_SEPP
            VIP_SIG2_SEPP
            WORKER_IP
            /) {
            $::JOB_PARAMS{$placeholder} = "";
        }

        #
        # CM Yang Provider
        #

        General::Logging::log_user_message("Get CMYP Information");
        (undef, $::JOB_PARAMS{'CMYP_IP'}, $::JOB_PARAMS{'CMYP_CLI_PORT'}, $::JOB_PARAMS{'CMYP_NETCONF_PORT'},  $::JOB_PARAMS{'CMYP_NETCONF_TLS_PORT'}) = ADP::Kubernetes_Operations::get_cmyp_info($::JOB_PARAMS{'SC_NAMESPACE'});

        #
        # Other SC Node Information
        #

        General::Logging::log_user_message("Get Service Information from namespace $::JOB_PARAMS{'SC_NAMESPACE'}");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $::JOB_PARAMS{'SC_NAMESPACE'} get svc",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0 ) {
            General::Logging::log_user_error_message("Failed to get service information");
            return 1;
        }
        for (@result) {
            # Check and parse generic information
            if (/^(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+\S+\s*$/) {
                $svc_name           = $1;
                $svc_type           = $2;
                $svc_cluster_ip     = $3;
                $svc_external_ip    = $4;
                $svc_ports          = $5;

                if ($svc_external_ip ne "<none>") {
                    $svc_ip = $svc_external_ip;
                } else {
                    $svc_ip = $svc_cluster_ip;
                }

                @all_ports          = split /,/, $svc_ports;
                @ports              = ();
                @nodeports          = ();
                for (@all_ports) {
                    s/\/(TCP|UDP)//g;
                    if (/^(\d+):(\d+)$/) {
                        push @ports, $1;
                        push @nodeports, $2;
                    } elsif (/^(\d+)$/) {
                        push @ports, $1;
                    }
                }
            } else {
                next;
            }

            # Set service specific variables
            if ($svc_name =~ /^eric-cm-yang-provider.*/) {
                #eric-cm-yang-provider-external                        LoadBalancer   10.100.144.16    10.221.168.105   830:31690/TCP,22:31784/TCP,6513:31562/TCP      3h18m
                $::JOB_PARAMS{'CMYP_IP'} = $svc_ip if ($::JOB_PARAMS{'CMYP_IP'} eq "");
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{'CMYP_NETCONF_PORT'} = $ports[0] if ($::JOB_PARAMS{'CMYP_NETCONF_PORT'} eq "");
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{'CMYP_NETCONF_NODEPORT'} = $nodeports[0] if ($::JOB_PARAMS{'CMYP_NETCONF_NODEPORT'} eq "");
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{'CMYP_CLI_PORT'} = $ports[1] if ($::JOB_PARAMS{'CMYP_CLI_PORT'} eq "");
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{'CMYP_CLI_NODEPORT'} = $nodeports[1] if ($::JOB_PARAMS{'CMYP_CLI_NODEPORT'} eq "");
                }
                if (scalar @ports >= 3) {
                    $::JOB_PARAMS{'CMYP_NETCONF_TLS_PORT'} = $ports[2] if ($::JOB_PARAMS{'CMYP_NETCONF_TLS_PORT'} eq "");
                }
                if (scalar @nodeports >= 3) {
                    $::JOB_PARAMS{'CMYP_NETCONF_TLS_NODEPORT'} = $nodeports[2] if ($::JOB_PARAMS{'CMYP_NETCONF_TLS_NODEPORT'} eq "");
                }
                next;
            }
            if ($svc_name eq "eric-bsf-worker") {
                #eric-bsf-worker                                       LoadBalancer   10.105.130.181   10.117.44.94     80:31503/TCP,443:30202/TCP                     3h18m
                $::JOB_PARAMS{'BSF_WRKR_IP'} = $svc_ip;
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{'BSF_WRKR_PORT'} = $ports[0];
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{'BSF_WRKR_NODEPORT'} = $nodeports[0];
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{'BSF_WRKR_TLS_PORT'} = $ports[1];
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{'BSF_WRKR_TLS_NODEPORT'} = $nodeports[1];
                }
                next;
            }
            if ($svc_name eq "eric-csa-worker") {
                #
                $::JOB_PARAMS{'CSA_WRKR_IP'} = $svc_ip;
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{'CSA_WRKR_PORT'} = $ports[0];
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{'CSA_WRKR_NODEPORT'} = $nodeports[0];
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{'CSA_WRKR_TLS_PORT'} = $ports[1];
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{'CSA_WRKR_TLS_NODEPORT'} = $nodeports[1];
                }
                next;
            }
            if ($svc_name eq "eric-scp-worker") {
                #eric-scp-worker                                       LoadBalancer   10.98.140.237    214.14.14.147    1090:32048/TCP,443:32277/TCP                   3h18m
                $::JOB_PARAMS{'SCP_WRKR_IP'} = $svc_ip;
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{'SCP_WRKR_PORT'} = $ports[0];
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{'SCP_WRKR_NODEPORT'} = $nodeports[0];
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{'SCP_WRKR_TLS_PORT'} = $ports[1];
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{'SCP_WRKR_TLS_NODEPORT'} = $nodeports[1];
                }
                next;
            }
            if ($svc_name eq "eric-sepp-worker") {
                #eric-sepp-worker                                      LoadBalancer   10.99.220.151    214.14.14.145    80:30138/TCP,443:32221/TCP                     3h18m
                $::JOB_PARAMS{'SEPP_WRKR_IP'} = $svc_ip;
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{'SEPP_WRKR_PORT'} = $ports[0];
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{'SEPP_WRKR_NODEPORT'} = $nodeports[0];
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{'SEPP_WRKR_TLS_PORT'} = $ports[1];
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{'SEPP_WRKR_TLS_NODEPORT'} = $nodeports[1];
                }
                next;
            }
            if ($svc_name eq "eric-sepp-worker-2") {
                #eric-sepp-worker-2                                    LoadBalancer   10.106.9.241     214.14.14.146    80:30979/TCP,443:30102/TCP                     3h18m
                $::JOB_PARAMS{'SEPP_WRKR_2_IP'} = $svc_ip;
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{'SEPP_WRKR_2_PORT'} = $ports[0];
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{'SEPP_WRKR_2_NODEPORT'} = $nodeports[0];
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{'SEPP_WRKR_2_TLS_PORT'} = $ports[1];
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{'SEPP_WRKR_2_TLS_NODEPORT'} = $nodeports[1];
                }
                next;
            }
        }


        #
        # Network Configuration File Parameters
        #

        General::Logging::log_user_message("Reading Network Configuration File Parameters");
        if (exists $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_BSF'} &&
            $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_BSF'}{'value'} ne "'CHANGEME'") {

            $::JOB_PARAMS{'VIP_SIG_BSF'} = $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_BSF'}{'value'};
            $::JOB_PARAMS{'VIP_SIG_BSF'} =~ s/'//g;
        }
        if (exists $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_SCP'} &&
            $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_SCP'}{'value'} ne "'CHANGEME'") {

            $::JOB_PARAMS{'VIP_SIG_SCP'} = $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_SCP'}{'value'};
            $::JOB_PARAMS{'VIP_SIG_SCP'} =~ s/'//g;
        }
        if (exists $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_SEPP'} &&
            $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_SEPP'}{'value'} ne "'CHANGEME'") {

            $::JOB_PARAMS{'VIP_SIG_SEPP'} = $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_SEPP'}{'value'};
            $::JOB_PARAMS{'VIP_SIG_SEPP'} =~ s/'//g;
        }
        if (exists $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG2_SEPP'} &&
            $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG2_SEPP'}{'value'} ne "'CHANGEME'") {

            $::JOB_PARAMS{'VIP_SIG2_SEPP'} = $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG2_SEPP'}{'value'};
            $::JOB_PARAMS{'VIP_SIG2_SEPP'} =~ s/'//g;
        }

        #
        # Worker IP
        #

        General::Logging::log_user_message("Get First Worker IP Address");
        $::JOB_PARAMS{'FIRST_WORKER_IP'} = ADP::Kubernetes_Operations::get_node_ip(
            {
                "index"         => 0,
                "kubeconfig"    => $::JOB_PARAMS{'KUBECONFIG'},
                "type"          => "worker",
            }
        );
        if (exists $::NETWORK_CONFIG_PARAMS{'node_worker_address'} && $::NETWORK_CONFIG_PARAMS{'node_worker_address'}{'value'} ne "CHANGEME") {
            $::JOB_PARAMS{'NODE_TRAFFIC_IP'} = $::NETWORK_CONFIG_PARAMS{'node_worker_address'}{'value'};
        } else {
            $::JOB_PARAMS{'NODE_TRAFFIC_IP'} = $::JOB_PARAMS{'FIRST_WORKER_IP'};
        }
        # Separate out IPv4 and IPv6 addresses
        my ($ipv4, $ipv6) = General::OS_Operations::return_ip_addresses($::JOB_PARAMS{'NODE_TRAFFIC_IP'});
        # Store IPv4 address
        if (@$ipv4) {
            $::JOB_PARAMS{'NODE_TRAFFIC_IP'} = $ipv4->[0];
        } else {
            # We set a special value so we can remove the line, if needed
            $::JOB_PARAMS{'NODE_TRAFFIC_IP'} = ""
        }
        $::JOB_PARAMS{'WORKER_IP'} = $::JOB_PARAMS{'NODE_TRAFFIC_IP'};  # For backward compatibility
        # Store IPv6 address
        if (@$ipv6) {
            $::JOB_PARAMS{'NODE_TRAFFIC_IPV6'} = $ipv6->[0];
        } else {
            # We set a special value so we can remove the line, if needed
            $::JOB_PARAMS{'NODE_TRAFFIC_IPV6'} = ""
        }

        $message = "Collected Information:\n";
        for my $placeholder ( qw/
            BSF_WRKR_IP
            BSF_WRKR_NODEPORT
            BSF_WRKR_PORT
            BSF_WRKR_TLS_NODEPORT
            BSF_WRKR_TLS_PORT
            CMYP_CLI_NODEPORT
            CMYP_CLI_PORT
            CMYP_IP
            CMYP_NETCONF_NODEPORT
            CMYP_NETCONF_PORT
            CMYP_NETCONF_TLS_NODEPORT
            CMYP_NETCONF_TLS_PORT
            CSA_WRKR_IP
            CSA_WRKR_NODEPORT
            CSA_WRKR_PORT
            CSA_WRKR_TLS_NODEPORT
            CSA_WRKR_TLS_PORT
            FIRST_WORKER_IP
            NODE_TRAFFIC_IP
            NODE_TRAFFIC_IPV6
            SCP_WRKR_IP
            SCP_WRKR_NODEPORT
            SCP_WRKR_PORT
            SCP_WRKR_TLS_NODEPORT
            SCP_WRKR_TLS_PORT
            SEPP_WRKR_IP
            SEPP_WRKR_NODEPORT
            SEPP_WRKR_PORT
            SEPP_WRKR_TLS_NODEPORT
            SEPP_WRKR_TLS_PORT
            VIP_SIG_BSF
            VIP_SIG_SCP
            VIP_SIG_SEPP
            VIP_SIG2_SEPP
            WORKER_IP
            /) {
            if ($::JOB_PARAMS{$placeholder} ne "") {
                # We have detected a value, so print it
                $message .= sprintf "%-30s %s\n", "$placeholder:", $::JOB_PARAMS{$placeholder};
            }
        }
        General::Logging::log_user_message($message);

        # Return success regardless of results of each command
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
    sub Get_Tools_Node_Specific_Information_P938S01T02 {

        my @all_ports;
        my $message;
        my @nodeports;
        my @ports;
        my $rc = 0;
        my @result;
        my $svc_cluster_ip;
        my $svc_external_ip;
        my $svc_ip;
        my $svc_name;
        my $svc_ports;
        my $svc_type;

        # Initialize node specific variables
        for my $placeholder ( qw/
            CHFSIM_1_IP
            CHFSIM_1_NODEPORT_HTTP
            CHFSIM_1_NODEPORT_HTTPS
            CHFSIM_1_PORT_HTTP
            CHFSIM_1_PORT_HTTPS
            CHFSIM_2_IP
            CHFSIM_2_NODEPORT_HTTP
            CHFSIM_2_NODEPORT_HTTPS
            CHFSIM_2_PORT_HTTP
            CHFSIM_2_PORT_HTTPS
            CHFSIM_3_IP
            CHFSIM_3_NODEPORT_HTTP
            CHFSIM_3_NODEPORT_HTTPS
            CHFSIM_3_PORT_HTTP
            CHFSIM_3_PORT_HTTPS
            CHFSIM_4_IP
            CHFSIM_4_NODEPORT_HTTP
            CHFSIM_4_NODEPORT_HTTPS
            CHFSIM_4_PORT_HTTP
            CHFSIM_4_PORT_HTTPS
            CHFSIM_5_IP
            CHFSIM_5_NODEPORT_HTTP
            CHFSIM_5_NODEPORT_HTTPS
            CHFSIM_5_PORT_HTTP
            CHFSIM_5_PORT_HTTPS
            CHFSIM_6_IP
            CHFSIM_6_NODEPORT_HTTP
            CHFSIM_6_NODEPORT_HTTPS
            CHFSIM_6_PORT_HTTP
            CHFSIM_6_PORT_HTTPS
            CHFSIM_7_IP
            CHFSIM_7_NODEPORT_HTTP
            CHFSIM_7_NODEPORT_HTTPS
            CHFSIM_7_PORT_HTTP
            CHFSIM_7_PORT_HTTPS
            CHFSIM_8_IP
            CHFSIM_8_NODEPORT_HTTP
            CHFSIM_8_NODEPORT_HTTPS
            CHFSIM_8_PORT_HTTP
            CHFSIM_8_PORT_HTTPS
            CHFSIM_9_IP
            CHFSIM_9_NODEPORT_HTTP
            CHFSIM_9_NODEPORT_HTTPS
            CHFSIM_9_PORT_HTTP
            CHFSIM_9_PORT_HTTPS
            CHFSIM_10_IP
            CHFSIM_10_NODEPORT_HTTP
            CHFSIM_10_NODEPORT_HTTPS
            CHFSIM_10_PORT_HTTP
            CHFSIM_10_PORT_HTTPS
            CHFSIM_11_IP
            CHFSIM_11_NODEPORT_HTTP
            CHFSIM_11_NODEPORT_HTTPS
            CHFSIM_11_PORT_HTTP
            CHFSIM_11_PORT_HTTPS
            CHFSIM_12_IP
            CHFSIM_12_NODEPORT_HTTP
            CHFSIM_12_NODEPORT_HTTPS
            CHFSIM_12_PORT_HTTP
            CHFSIM_12_PORT_HTTPS
            FIRST_WORKER_IP_TOOLS
            NODE_TRAFFIC_IP_TOOLS
            NODE_TRAFFIC_IPV6_TOOLS
            NRFSIM_IP
            NRFSIM_NODEPORT_HTTP
            NRFSIM_NODEPORT_HTTPS
            NRFSIM_PORT
            NRFSIM_PORT_HTTP
            NRFSIM_PORT_HTTPS
            SEPPSIM_P1_IP
            SEPPSIM_P1_NODEPORT
            SEPPSIM_P1_NODEPORT_HTTP
            SEPPSIM_P1_NODEPORT_HTTPS
            SEPPSIM_P1_PORT_HTTP
            SEPPSIM_P1_PORT_HTTPS
            SEPPSIM_P2_IP
            SEPPSIM_P2_NODEPORT
            SEPPSIM_P2_NODEPORT_HTTP
            SEPPSIM_P2_NODEPORT_HTTPS
            SEPPSIM_P2_PORT_HTTP
            SEPPSIM_P2_PORT_HTTPS
            SEPPSIM_P3_IP
            SEPPSIM_P3_NODEPORT
            SEPPSIM_P3_NODEPORT_HTTP
            SEPPSIM_P3_NODEPORT_HTTPS
            SEPPSIM_P3_PORT_HTTP
            SEPPSIM_P3_PORT_HTTPS
            SEPPSIM_P4_IP
            SEPPSIM_P4_NODEPORT
            SEPPSIM_P4_NODEPORT_HTTP
            SEPPSIM_P4_NODEPORT_HTTPS
            SEPPSIM_P4_PORT_HTTP
            SEPPSIM_P4_PORT_HTTPS
            SEPPSIM_P5_IP
            SEPPSIM_P5_NODEPORT
            SEPPSIM_P5_NODEPORT_HTTP
            SEPPSIM_P5_NODEPORT_HTTPS
            SEPPSIM_P5_PORT_HTTP
            SEPPSIM_P5_PORT_HTTPS
            SEPPSIM_P6_IP
            SEPPSIM_P6_NODEPORT
            SEPPSIM_P6_NODEPORT_HTTP
            SEPPSIM_P6_NODEPORT_HTTPS
            SEPPSIM_P6_PORT_HTTP
            SEPPSIM_P6_PORT_HTTPS
            SEPPSIM_P7_IP
            SEPPSIM_P7_NODEPORT
            SEPPSIM_P7_NODEPORT_HTTP
            SEPPSIM_P7_NODEPORT_HTTPS
            SEPPSIM_P7_PORT_HTTP
            SEPPSIM_P7_PORT_HTTPS
            SEPPSIM_P8_IP
            SEPPSIM_P8_NODEPORT
            SEPPSIM_P8_NODEPORT_HTTP
            SEPPSIM_P8_NODEPORT_HTTPS
            SEPPSIM_P8_PORT_HTTP
            SEPPSIM_P8_PORT_HTTPS
            WORKER_IP_TOOLS
            /) {
            $::JOB_PARAMS{$placeholder} = "";
        }

        #
        # Other Tools Node Information
        #

        General::Logging::log_user_message("Get Service Information from namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'}");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} --namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'} get svc",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0 ) {
            General::Logging::log_user_error_message("Failed to get service information");
            return 1;
        }
        for (@result) {
            # Check and parse generic information
            if (/^(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+\S+\s*$/) {
                $svc_name           = $1;
                $svc_type           = $2;
                $svc_cluster_ip     = $3;
                $svc_external_ip    = $4;
                $svc_ports          = $5;

                if ($svc_external_ip ne "<none>") {
                    $svc_ip = $svc_external_ip;
                } else {
                    $svc_ip = $svc_cluster_ip;
                }

                @all_ports          = split /,/, $svc_ports;
                @ports              = ();
                @nodeports          = ();
                for (@all_ports) {
                    s/\/(TCP|UDP)//g;
                    if (/^(\d+):(\d+)$/) {
                        push @ports, $1;
                        push @nodeports, $2;
                    } elsif (/^(\d+)$/) {
                        push @ports, $1;
                    }
                }
            } else {
                next;
            }

            # Set service specific variables
            if ($svc_name =~ /^eric-chfsim-(\d+)$/) {
                #eric-chfsim-1                    NodePort   10.99.239.219    <none>        80:31397/TCP,443:30209/TCP     3d
                $::JOB_PARAMS{"CHFSIM_$1_IP"} = $svc_ip;
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{"CHFSIM_$1_PORT_HTTP"} = $ports[0];
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{"CHFSIM_$1_NODEPORT_HTTP"} = $nodeports[0];
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{"CHFSIM_$1_PORT_HTTPS"} = $ports[1];
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{"CHFSIM_$1_NODEPORT_HTTPS"} = $nodeports[1];
                }
                next;
            }
            if ($svc_name eq "eric-nrfsim") {
                #eric-nrfsim                      NodePort   10.216.92.55     <none>        80:31444/TCP,443:31002/TCP   125m
                $::JOB_PARAMS{'NRFSIM_IP'} = $svc_ip;
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{'NRFSIM_PORT_HTTP'} = $ports[0];
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{'NRFSIM_NODEPORT_HTTP'} = $nodeports[0];
                    $::JOB_PARAMS{'NRFSIM_PORT'} = $nodeports[0];   # Backwards compatibility
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{'NRFSIM_PORT_HTTPS'} = $ports[1];
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{'NRFSIM_NODEPORT_HTTPS'} = $nodeports[1];
                }
                next;
            }
            if ($svc_name =~ /^eric-seppsim-p(\d+)-mcc-.+/) {
                #eric-seppsim-p1-mcc-206-mnc-33   NodePort   10.221.230.101   <none>        80:30396/TCP,443:30519/TCP   122m
                #eric-seppsim-p2-mcc-206-mnc-33   NodePort   10.219.159.187   <none>        80:30749/TCP,443:32052/TCP   122m
                #eric-seppsim-p3-mcc-262-mnc-73   NodePort   10.209.226.202   <none>        80:30258/TCP,443:32237/TCP   122m
                #eric-seppsim-p4-mcc-262-mnc-73   NodePort   10.208.138.103   <none>        80:30898/TCP,443:30923/TCP   122m
                #eric-seppsim-p5-mcc-262-mnc-73   NodePort   10.209.163.152   <none>        80:31412/TCP,443:30799/TCP   122m
                #eric-seppsim-p6-mcc-262-mnc-73   NodePort   10.213.141.236   <none>        80:32464/TCP,443:32462/TCP   122m
                #eric-seppsim-p7-mcc-262-mnc-73   NodePort   10.214.142.152   <none>        80:31559/TCP,443:31079/TCP   122m
                #eric-seppsim-p8-mcc-262-mnc-73   NodePort   10.218.95.95     <none>        80:31195/TCP,443:31338/TCP   122m
                $::JOB_PARAMS{"SEPPSIM_P$1_IP"} = $svc_ip;
                if (scalar @ports >= 1) {
                    $::JOB_PARAMS{"SEPPSIM_P$1_PORT_HTTP"} = $ports[0];
                }
                if (scalar @nodeports >= 1) {
                    $::JOB_PARAMS{"SEPPSIM_P$1_NODEPORT_HTTP"} = $nodeports[0];
                }
                if (scalar @ports >= 2) {
                    $::JOB_PARAMS{"SEPPSIM_P$1_PORT_HTTPS"} = $ports[1];
                }
                if (scalar @nodeports >= 2) {
                    $::JOB_PARAMS{"SEPPSIM_P$1_NODEPORT_HTTPS"} = $nodeports[1];
                    $::JOB_PARAMS{"SEPPSIM_P$1_NODEPORT"} = $nodeports[1];   # Backwards compatibility
                }
                next;
            }
        }

        #
        # Worker IP
        #

        General::Logging::log_user_message("Get First Worker IP Address for tools");
        $::JOB_PARAMS{'FIRST_WORKER_IP_TOOLS'} = ADP::Kubernetes_Operations::get_node_ip(
            {
                "index"         => 0,
                "kubeconfig"    => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                "type"          => "worker",
            }
        );
        if (exists $::NETWORK_CONFIG_PARAMS{'tools_node_worker_address'} && $::NETWORK_CONFIG_PARAMS{'tools_node_worker_address'}{'value'} ne "CHANGEME") {
            $::JOB_PARAMS{'NODE_TRAFFIC_IP_TOOLS'} = $::NETWORK_CONFIG_PARAMS{'tools_node_worker_address'}{'value'};
        } else {
            $::JOB_PARAMS{'NODE_TRAFFIC_IP_TOOLS'} = $::JOB_PARAMS{'FIRST_WORKER_IP_TOOLS'};
        }
        # Separate out IPv4 and IPv6 addresses
        my ($ipv4, $ipv6) = General::OS_Operations::return_ip_addresses($::JOB_PARAMS{'NODE_TRAFFIC_IP_TOOLS'});
        # Store IPv4 address
        if (@$ipv4) {
            $::JOB_PARAMS{'NODE_TRAFFIC_IP_TOOLS'} = $ipv4->[0];
        } else {
            # We set a special value so we can remove the line, if needed
            $::JOB_PARAMS{'NODE_TRAFFIC_IP_TOOLS'} = ""
        }
        $::JOB_PARAMS{'WORKER_IP_TOOLS'} = $::JOB_PARAMS{'NODE_TRAFFIC_IP_TOOLS'};  # For backward compatibility
        # Store IPv6 address
        if (@$ipv6) {
            $::JOB_PARAMS{'NODE_TRAFFIC_IPV6_TOOLS'} = $ipv6->[0];
        } else {
            # We set a special value so we can remove the line, if needed
            $::JOB_PARAMS{'NODE_TRAFFIC_IPV6_TOOLS'} = ""
        }

        $message = "Collected Information:\n";
        for my $placeholder ( qw/
            CHFSIM_1_IP
            CHFSIM_1_NODEPORT_HTTP
            CHFSIM_1_NODEPORT_HTTPS
            CHFSIM_1_PORT_HTTP
            CHFSIM_1_PORT_HTTPS
            CHFSIM_2_IP
            CHFSIM_2_NODEPORT_HTTP
            CHFSIM_2_NODEPORT_HTTPS
            CHFSIM_2_PORT_HTTP
            CHFSIM_2_PORT_HTTPS
            CHFSIM_3_IP
            CHFSIM_3_NODEPORT_HTTP
            CHFSIM_3_NODEPORT_HTTPS
            CHFSIM_3_PORT_HTTP
            CHFSIM_3_PORT_HTTPS
            CHFSIM_4_IP
            CHFSIM_4_NODEPORT_HTTP
            CHFSIM_4_NODEPORT_HTTPS
            CHFSIM_4_PORT_HTTP
            CHFSIM_4_PORT_HTTPS
            CHFSIM_5_IP
            CHFSIM_5_NODEPORT_HTTP
            CHFSIM_5_NODEPORT_HTTPS
            CHFSIM_5_PORT_HTTP
            CHFSIM_5_PORT_HTTPS
            CHFSIM_6_IP
            CHFSIM_6_NODEPORT_HTTP
            CHFSIM_6_NODEPORT_HTTPS
            CHFSIM_6_PORT_HTTP
            CHFSIM_6_PORT_HTTPS
            CHFSIM_7_IP
            CHFSIM_7_NODEPORT_HTTP
            CHFSIM_7_NODEPORT_HTTPS
            CHFSIM_7_PORT_HTTP
            CHFSIM_7_PORT_HTTPS
            CHFSIM_8_IP
            CHFSIM_8_NODEPORT_HTTP
            CHFSIM_8_NODEPORT_HTTPS
            CHFSIM_8_PORT_HTTP
            CHFSIM_8_PORT_HTTPS
            CHFSIM_9_IP
            CHFSIM_9_NODEPORT_HTTP
            CHFSIM_9_NODEPORT_HTTPS
            CHFSIM_9_PORT_HTTP
            CHFSIM_9_PORT_HTTPS
            CHFSIM_10_IP
            CHFSIM_10_NODEPORT_HTTP
            CHFSIM_10_NODEPORT_HTTPS
            CHFSIM_10_PORT_HTTP
            CHFSIM_10_PORT_HTTPS
            CHFSIM_11_IP
            CHFSIM_11_NODEPORT_HTTP
            CHFSIM_11_NODEPORT_HTTPS
            CHFSIM_11_PORT_HTTP
            CHFSIM_11_PORT_HTTPS
            CHFSIM_12_IP
            CHFSIM_12_NODEPORT_HTTP
            CHFSIM_12_NODEPORT_HTTPS
            CHFSIM_12_PORT_HTTP
            CHFSIM_12_PORT_HTTPS
            FIRST_WORKER_IP_TOOLS
            NODE_TRAFFIC_IP_TOOLS
            NODE_TRAFFIC_IPV6_TOOLS
            NRFSIM_IP
            NRFSIM_NODEPORT_HTTP
            NRFSIM_NODEPORT_HTTPS
            NRFSIM_PORT
            NRFSIM_PORT_HTTP
            NRFSIM_PORT_HTTPS
            SEPPSIM_P1_IP
            SEPPSIM_P1_NODEPORT
            SEPPSIM_P1_NODEPORT_HTTP
            SEPPSIM_P1_NODEPORT_HTTPS
            SEPPSIM_P1_PORT_HTTP
            SEPPSIM_P1_PORT_HTTPS
            SEPPSIM_P2_IP
            SEPPSIM_P2_NODEPORT
            SEPPSIM_P2_NODEPORT_HTTP
            SEPPSIM_P2_NODEPORT_HTTPS
            SEPPSIM_P2_PORT_HTTP
            SEPPSIM_P2_PORT_HTTPS
            SEPPSIM_P3_IP
            SEPPSIM_P3_NODEPORT
            SEPPSIM_P3_NODEPORT_HTTP
            SEPPSIM_P3_NODEPORT_HTTPS
            SEPPSIM_P3_PORT_HTTP
            SEPPSIM_P3_PORT_HTTPS
            SEPPSIM_P4_IP
            SEPPSIM_P4_NODEPORT
            SEPPSIM_P4_NODEPORT_HTTP
            SEPPSIM_P4_NODEPORT_HTTPS
            SEPPSIM_P4_PORT_HTTP
            SEPPSIM_P4_PORT_HTTPS
            SEPPSIM_P5_IP
            SEPPSIM_P5_NODEPORT
            SEPPSIM_P5_NODEPORT_HTTP
            SEPPSIM_P5_NODEPORT_HTTPS
            SEPPSIM_P5_PORT_HTTP
            SEPPSIM_P5_PORT_HTTPS
            SEPPSIM_P6_IP
            SEPPSIM_P6_NODEPORT
            SEPPSIM_P6_NODEPORT_HTTP
            SEPPSIM_P6_NODEPORT_HTTPS
            SEPPSIM_P6_PORT_HTTP
            SEPPSIM_P6_PORT_HTTPS
            SEPPSIM_P7_IP
            SEPPSIM_P7_NODEPORT
            SEPPSIM_P7_NODEPORT_HTTP
            SEPPSIM_P7_NODEPORT_HTTPS
            SEPPSIM_P7_PORT_HTTP
            SEPPSIM_P7_PORT_HTTPS
            SEPPSIM_P8_IP
            SEPPSIM_P8_NODEPORT
            SEPPSIM_P8_NODEPORT_HTTP
            SEPPSIM_P8_NODEPORT_HTTPS
            SEPPSIM_P8_PORT_HTTP
            SEPPSIM_P8_PORT_HTTPS
            WORKER_IP_TOOLS
            /) {
            if ($::JOB_PARAMS{$placeholder} ne "") {
                # We have detected a value, so print it
                $message .= sprintf "%-30s %s\n", "$placeholder:", $::JOB_PARAMS{$placeholder};
            }
        }
        General::Logging::log_user_message($message);

        # Return success regardless of results of each command
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
sub Replace_File_Place_Holders_P938S02 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Find_And_Replace_Place_Holders_In_File_P938S02T01 } );
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
    sub Find_And_Replace_Place_Holders_In_File_P938S02T01 {

        return replace_place_holders_in_file($::JOB_PARAMS{'P938_INPUT_FILE_NAME'}, $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'}, $::JOB_PARAMS{'P938_OUTPUT_FILE_ACCESSMODE'});

    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P938S99 {

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
# This subroutine replace <xxxx> place holders in an array with the value from
# the %::JOB_PARAMS hash.
#
# Input:
#   - Array Reference for data to be changed.
#
# Output:
#   - Return code, 0=success, >0=failure.
#   - Number of changes.
#
sub replace_place_holders_in_array {
    my $array_ref = shift;

    my $data_changed = 0;
    my $index = 0;
    my $last_index = $#$array_ref;
    my $rc = 0;

    while ($index <= $last_index) {
        # Read the input data that might contain place holders and remove any trailing whitespace characters
        $array_ref->[$index] =~ s/\s+$//;

        # Check if we have any place holders in the line, e.g. <xxxx>
        unless ($array_ref->[$index] =~ /<\S+>/) {
            # No place holder found, take next line.
            $index++;
            next;
        }

        # If we come down here we either, have place holders that need to be replaced,
        # or we are reading XML tags that might or might not contain place holders.

        # Change place holders in data
        # ----------------------------

        for my $placeholder ( sort keys %::JOB_PARAMS) {
            if ($array_ref->[$index] =~ /<$placeholder>/) {
                # We found a place holder that needs to be replaced.
                my $placeholder_value = $::JOB_PARAMS{$placeholder};

                if ($array_ref->[$index] =~ /^(.*)(<ipv[46]-address><$placeholder><\/ipv[46]-address>|ipv[46]-address\s+<$placeholder>)/) {
                    # A case where we have either an ipv4 or ipv6 address that must be handled in a special way
                    # because of the way the input files has been written.
                    # The file might only have on e.g. <ipv4-address> but we have two IP-addresses, one IPv4 and one IPv6
                    # in the place holder variable, then we need to add one more line with <ipv6-address>.

                    # Store the data before the tag, in case we need to add a new line so we indent it properly
                    my $before_tag = $1;

                    # Separate out IPv4 and IPv6 addresses from the place holder variable
                    my ($ipv4, $ipv6) = General::OS_Operations::return_ip_addresses($::JOB_PARAMS{$placeholder});

                    # Change <ipv6-address> to <ipv4-address> and viseverse, if the placeholder value is other address type
                    # or delete the line if placeholder value is empty, or add a new line with the other address type if
                    # we have both address types.

                    if ($array_ref->[$index] =~ /<ipv4-address><$placeholder><\/ipv4-address>/) {
                        # CMYP NETCONF
                        # First handle the IPv4 address, if any
                        if (@$ipv4) {
                            # We have an IPv4 address, so let's use it.
                            $placeholder_value = $ipv4->[0];
                        } else {
                            # We have no IPv4 address, so we need to remove this parameter otherwise it will cause an error when loading the file.
                            if ($array_ref->[$index] =~ /^\s+<ipv4-address><$placeholder><\/ipv4-address>/) {
                                # Remove white space at beginning of line
                                $array_ref->[$index] =~ s/^\s+//;
                            }
                            # Remove all matching <ipv4-address> parameters from the line
                            $array_ref->[$index] =~ s/<ipv4-address><$placeholder><\/ipv4-address>//g;
                            $data_changed++;
                        }

                        if (@$ipv6) {
                            # We also have an IPv6 address, now we need to check if the previous and next line in the
                            # file contains the <ipv6-address> tag or not.
                            if ($index-1 >= 0 && $array_ref->[$index-1] =~ /<ipv6-address>[^<]*<\/ipv6-address>/) {
                                # The previous line already contained the IPv6 address, so we do nothing special
                                # with the IPv6 address, since the place holder would already have been replaced.
                            } elsif ($array_ref->[$index] =~ /<ipv6-address><$placeholder><\/ipv6-address>/) {
                                # The current line also contains the string: <ipv6-address><placeholder></ipv6-address>
                                # So we already now replace that place holder with the IPv6 address.
                                # NOTE: This will probably never happen.
                                $array_ref->[$index] =~ s/<ipv6-address><$placeholder><\/ipv6-address>/<ipv6-address>$ipv6->[0]<\/ipv6-address>/g;
                                $data_changed++;
                            } elsif ($index+1 <= $last_index && $array_ref->[$index+1] !~ /<ipv6-address><[^>]+><\/ipv6-address>/) {
                                # The next line does not contain the string: <ipv6-address><placeholder></ipv6-address>
                                # so we need to add this line after the current line since we seem to be dealing with
                                # an old DT file with just one <ipv4-address> tag but deployed on a dual stack node.
                                if ($array_ref->[$index] eq "") {
                                    # We have already removed the <ipv4-address> tags because we have no IPv4 address
                                    # so just replace the <ipv4-address> tags with <ipv6-address>.
                                    $array_ref->[$index] = "$before_tag<ipv6-address>$ipv6->[0]</ipv6-address>";
                                } else {
                                    # We have an IPv4 address and need to add a new line with <ipv6-address> tag.
                                    $array_ref->[$index] = "$array_ref->[$index]\n$before_tag<ipv6-address>$ipv6->[0]</ipv6-address>";
                                }
                                $data_changed++;
                            } else {
                                # The next line contains the <ipv6-address> tag, we do nothing with the IPv6 address
                                # since it will be replaced when we handle the next line.
                            }
                        }
                    } elsif ($array_ref->[$index] =~ /<ipv6-address><$placeholder><\/ipv6-address>/) {
                        # CMYP NETCONF
                        # First handle the IPv6 address, if any
                        if (@$ipv6) {
                            # We have an IPv6 address, so let's use it.
                            $placeholder_value = $ipv6->[0];
                        } else {
                            # We have no IPv6 address, so we need to remove this parameter otherwise it will cause an error when loading the file.
                            if ($array_ref->[$index] =~ /^\s+<ipv6-address><$placeholder><\/ipv6-address>/) {
                                # Remove white space at beginning of line
                                $array_ref->[$index] =~ s/^\s+//;
                            }
                            # Remove all matching <ipv6-address> parameters from the line
                            $array_ref->[$index] =~ s/<ipv6-address><$placeholder><\/ipv6-address>//g;
                            $data_changed++;
                        }

                        if (@$ipv4) {
                            # We also have an IPv4 address, now we need to check if the previous and next line in the
                            # file contains the <ipv4-address> tag or not.
                            if ($index-1 >= 0 && $array_ref->[$index-1] =~ /<ipv4-address>[^<]*<\/ipv4-address>/) {
                                # The previous line already contained the IPv4 address, so we do nothing special
                                # with the IPv4 address, since the place holder would already have been replaced.
                            } elsif ($array_ref->[$index] =~ /<ipv4-address><$placeholder><\/ipv4-address>/) {
                                # The current line also contains the string: <ipv4-address><placeholder></ipv4-address>
                                # So we already now replace that place holder with the IPv4 address.
                                # NOTE: This will probably never happen.
                                $array_ref->[$index] =~ s/<ipv4-address><$placeholder><\/ipv4-address>/<ipv4-address>$ipv4->[0]<\/ipv4-address>/g;
                                $data_changed++;
                            } elsif ($index+1 <= $last_index && $array_ref->[$index+1] !~ /<ipv4-address><[^>]+><\/ipv4-address>/) {
                                # The next line does not contain the string: <ipv4-address><placeholder></ipv4-address>
                                # so we need to add this line after the current line since we seem to be dealing with
                                # an old DT file with just one <ipv6-address> tag but deployed on a dual stack node.
                                if ($array_ref->[$index] eq "") {
                                    # We have already removed the <ipv6-address> tags because we have no IPv6 address
                                    # so just replace the <ipv6-address> tags with <ipv4-address>.
                                    $array_ref->[$index] = "$before_tag<ipv4-address>$ipv4->[0]</ipv4-address>";
                                } else {
                                    # We have an IPv6 address and need to add a new line with <ipv4-address> tag.
                                    $array_ref->[$index] = "$array_ref->[$index]\n$before_tag<ipv4-address>$ipv4->[0]</ipv4-address>";
                                }
                                $data_changed++;
                            } else {
                                # The next line contains the <ipv4-address> tag, we do nothing with the IPv4 address
                                # since it will be replaced when we handle the next line.
                            }
                        }
                    } elsif ($array_ref->[$index] =~ /ipv4-address\s+<$placeholder>/) {
                        # CMYP CLI
                        # First handle the IPv4 address, if any
                        if (@$ipv4) {
                            # We have an IPv4 address, so let's use it.
                            $placeholder_value = $ipv4->[0];
                        } else {
                            # We have no IPv4 address, so we need to remove this parameter otherwise it will cause an error when loading the file.
                            if ($array_ref->[$index] =~ /^\s+ipv4-address\s+<$placeholder>/) {
                                # Remove white space at beginning of line
                                $array_ref->[$index] =~ s/^\s+//;
                            }
                            # Remove all matching ipv4-address parameters from the line
                            $array_ref->[$index] =~ s/ipv4-address\s+<$placeholder>//g;
                            $data_changed++;
                        }

                        if (@$ipv6) {
                            # We also have an IPv6 address, now we need to check if the previous and next line in the
                            # file contains the <ipv6-address> tag or not.
                            if ($index-1 >= 0 && $array_ref->[$index-1] =~ /ipv6-address\s+\S+/) {
                                # The previous line already contained the IPv6 address, so we do nothing special
                                # with the IPv6 address, since the place holder would already have been replaced.
                            } elsif ($array_ref->[$index] =~ /ipv6-address\s+<$placeholder>/) {
                                # The current line also contains the string: ipv6-address <placeholder>
                                # So we already now replace that place holder with the IPv6 address.
                                # NOTE: This will probably never happen.
                                $array_ref->[$index] =~ s/ipv6-address\s+<$placeholder>/ipv6-address $ipv6->[0]/g;
                                $data_changed++;
                            } elsif ($index+1 <= $last_index && $array_ref->[$index+1] !~ /ipv6-address\s+<[^>]+>/) {
                                # The next line does not contain the string: ipv6-address <placeholder>
                                # so we need to add this line after the current line since we seem to be dealing with
                                # an old DT file with just one ipv4-address tag but deployed on a dual stack node.
                                if ($array_ref->[$index] eq "") {
                                    # We have already removed the ipv4-address tags because we have no IPv4 address
                                    # so just replace the ipv4-address tags with ipv6-address.
                                    $array_ref->[$index] = "${before_tag}ipv6-address $ipv6->[0]";
                                } else {
                                    # We have an IPv4 address and need to add a new line with ipv6-address tag.
                                    $array_ref->[$index] = "$array_ref->[$index]\n${before_tag}ipv6-address $ipv6->[0]";
                                }
                                $data_changed++;
                            } else {
                                # The next line contains the ipv6-address tag, we do nothing with the IPv6 address
                                # since it will be replaced when we handle the next line.
                            }
                        }
                    } elsif ($array_ref->[$index] =~ /ipv6-address\s+<$placeholder>/) {
                        # CMYP CLI
                        # First handle the IPv6 address, if any
                        if (@$ipv6) {
                            # We have an IPv6 address, so let's use it.
                            $placeholder_value = $ipv6->[0];
                        } else {
                            # We have no IPv6 address, so we need to remove this parameter otherwise it will cause an error when loading the file.
                            if ($array_ref->[$index] =~ /^\s+ipv6-address\s+<$placeholder>/) {
                                # Remove white space at beginning of line
                                $array_ref->[$index] =~ s/^\s+//;
                            }
                            # Remove all matching <ipv6-address> parameters from the line
                            $array_ref->[$index] =~ s/ipv6-address\s+<$placeholder>//g;
                            $data_changed++;
                        }

                        if (@$ipv4) {
                            # We also have an IPv4 address, now we need to check if the previous and next line in the
                            # file contains the ipv4-address tag or not.
                            if ($index-1 >= 0 && $array_ref->[$index-1] =~ /ipv4-address\s+\S+/) {
                                # The previous line already contained the IPv4 address, so we do nothing special
                                # with the IPv4 address, since the place holder would already have been replaced.
                            } elsif ($array_ref->[$index] =~ /ipv4-address\s+<$placeholder>/) {
                                # The current line also contains the string: ipv4-address <placeholder>
                                # So we already now replace that place holder with the IPv4 address.
                                # NOTE: This will probably never happen.
                                $array_ref->[$index] =~ s/ipv4-address\s+<$placeholder>/ipv4-address $ipv4->[0]/g;
                                $data_changed++;
                            } elsif ($index+1 <= $last_index && $array_ref->[$index+1] !~ /ipv4-address\s+<[^>]+>/) {
                                # The next line does not contain the string: ipv4-address <placeholder>
                                # so we need to add this line after the current line since we seem to be dealing with
                                # an old DT file with just one <ipv6-address> tag but deployed on a dual stack node.
                                if ($array_ref->[$index] eq "") {
                                    # We have already removed the ipv6-address tags because we have no IPv6 address
                                    # so just replace the ipv6-address tags with ipv4-address.
                                    $array_ref->[$index] = "${before_tag}ipv4-address $ipv4->[0]";
                                } else {
                                    # We have an IPv6 address and need to add a new line with ipv4-address tag.
                                    $array_ref->[$index] = "$array_ref->[$index]\n${before_tag}ipv4-address $ipv4->[0]";
                                }
                                $data_changed++;
                            } else {
                                # The next line contains the ipv4-address tag, we do nothing with the IPv4 address
                                # since it will be replaced when we handle the next line.
                            }
                        }
                    }
                } elsif ($placeholder eq "BSF_WRKR_PORT" &&
                         exists $::JOB_PARAMS{'BSF_WRKR_TLS_PORT'} &&
                         $::JOB_PARAMS{'BSF_WRKR_TLS_PORT'} ne "" &&
                         $array_ref->[$index] =~ /^(.*)(<port><BSF_WRKR_PORT><\/port>)/) {
                    # Special handling for very old DT files where we had no <tls-port>, in which case we need to add it
                    # unless the previous or next line contains it.

                    # Store the data before the tag, in case we need to add a new line so we indent it properly
                    my $before_tag = $1;

                    if ($index-1 >= 0 && $array_ref->[$index-1] =~ /<tls-port>[^<]*<\/tls-port>/) {
                        # The previous line already contained the tls-port, so we do nothing special
                        # with the port, since the place holder would already have been replaced.
                    } elsif ($array_ref->[$index] =~ /<tls-port><$placeholder><\/tls-port>/) {
                        # The current line also contains the string: <tls-port><placeholder></tls-port>
                        # So we already now replace that place holder with the tls-port value.
                        # NOTE: This will probably never happen.
                        $array_ref->[$index] =~ s/<tls-port><$placeholder><\/tls-port>/<tls-port>$::JOB_PARAMS{'BSF_WRKR_TLS_PORT'}<\/tls-port>/g;
                        $data_changed++;
                    } elsif ($index+1 <= $last_index && $array_ref->[$index+1] !~ /<tls-port><[^<]+><\/tls-port>/) {
                        # The next line does not contain the string: <tls-port><placeholder></tls-port>
                        # so we need to add this line after the current line since we seem to be dealing with
                        # an old DT file with just one <port>> tag but we have also an tls-port.
                        $array_ref->[$index] = "$array_ref->[$index]\n${before_tag}<tls-port>$::JOB_PARAMS{'BSF_WRKR_TLS_PORT'}</tls-port>";
                        $data_changed++;
                    } else {
                        # The next line contains the <tls-port> tag, we do nothing with the port
                        # since it will be replaced when we handle the next line.
                    }
                } elsif ($placeholder_value eq "") {
                    # Since the place holder value is empty we leave it intact, e.g.  <placeholder> which will probably cause a failure when loading the DT
                    next;
                }

                $array_ref->[$index] =~ s/<$placeholder>/$placeholder_value/g;
                $data_changed++;
            }
        }
        $index++;
    }

    if ($data_changed == 0) {
        General::Logging::log_user_message("No need to update the data since no placeholders were changed.");
    } else {
        General::Logging::log_user_message("Replaced $data_changed placeholders.");
    }

    return (0, $data_changed);
}

# -----------------------------------------------------------------------------
# This subroutine replace <xxxx> place holders in a file with the value from
# the %::JOB_PARAMS hash.
#
# Input:
#   - Input file path.
#   - Output ffile path (can be the same as input file path).
#   - File access mode for output file (default 666 if not specified).
#
# Output:
#   - Return code, 0=success, >0=failure.
#
sub replace_place_holders_in_file {
    my $filename_in = shift;
    my $filename_out = shift;
    my $file_accessmode = shift;

    my @config_data = ();
    my $data_changed = 0;
    my $rc = 0;

    if ($filename_in) {
        General::Logging::log_user_message("Reading input file: $filename_in");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => $filename_in,
                "output-ref"          => \@config_data,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("  Failed to read file with configuration data");
            return $rc;
        }
    } else {
        General::Logging::log_user_error_message("  No input file specified");
        return 1;
    }

    unless ($filename_out) {
        General::Logging::log_user_error_message("  No output file specified");
        return 1;
    }

    # Change placeholders in the file data
    ($rc, $data_changed) = replace_place_holders_in_array( \@config_data );

    if ($rc == 0) {
        if ($data_changed == 0 && $filename_in eq $filename_out) {
            General::Logging::log_user_message("No need to update the file since no placeholders were changed and in and out files are the same");
        } else {
            $file_accessmode = "666" unless $file_accessmode;
            General::Logging::log_user_message("Writing output file: $filename_out");
            $rc = General::File_Operations::write_file(
                {
                    "filename"          => $filename_out,
                    "output-ref"        => \@config_data,
                    "file-access-mode"  => $file_accessmode,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to write file with configuration data");
            }
        }
    } else {
        General::Logging::log_user_error_message("Failed to change the file data");
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
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
        'KUBECONFIG_TOOLS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then this file will be used as the config file for all helm and
kubectl commands towards the tools deployment.
If you don't specify this parameter then it will take the default file which
is usually called ~/.kube/config.
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
        'TOOLS_NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls in which namespace the tools are installed on the node
and should be specified if you want to override the parameter from the network
configuration file called 'tools_namespace'.
If you don't specify this parameter then it will take the value from the network
configuration file.
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

This Playlist script is used for collecting node information like IP-addresses
and ports for an SC or Tools deployment and store the information in the
job variables hash \%::JOB_PARAMS, if so wanted.

It will also if so wanted, replace place holders <xxxx> in a specific file with the
values from the \%::JOB_PARAMS hash, for example the place holder <BSF_WRKR_PORT>
will be replaced by the value from the \$::JOB_PARAMS{'BSF_WRKR_PORT'} variable.

The following job/playlist variables controls if information is collected
and stored in the \%::JOB_PARAMS hash:

    - P938_GET_SC_NODE_INFORMATION (yes or no, default no)
    - P938_GET_TOOLS_NODE_INFORMATION (yes or no, default no)

The following information will be stored in the job variables hash:

    - From the SC Namespace
        BSF_WRKR_IP
        BSF_WRKR_NODEPORT
        BSF_WRKR_PORT
        BSF_WRKR_TLS_NODEPORT
        BSF_WRKR_TLS_PORT
        CMYP_CLI_NODEPORT
        CMYP_CLI_PORT
        CMYP_IP
        CMYP_NETCONF_NODEPORT
        CMYP_NETCONF_PORT
        CMYP_NETCONF_TLS_NODEPORT
        CMYP_NETCONF_TLS_PORT
        CSA_WRKR_IP
        CSA_WRKR_NODEPORT
        CSA_WRKR_PORT
        CSA_WRKR_TLS_NODEPORT
        CSA_WRKR_TLS_PORT
        FIRST_WORKER_IP (INTERNAL-IP of first worker node)
        NODE_TRAFFIC_IP (from network config parameter 'node_worker_address', which is normally IPv4 address from eth2 interface of first worker node)
        NODE_TRAFFIC_IPV6 (from network config parameter 'node_worker_address', which is normally IPv6 address from eth2 interface of first worker node)
        SCP_WRKR_IP
        SCP_WRKR_NODEPORT
        SCP_WRKR_PORT
        SCP_WRKR_TLS_NODEPORT
        SCP_WRKR_TLS_PORT
        SEPP_WRKR_IP
        SEPP_WRKR_NODEPORT
        SEPP_WRKR_PORT
        SEPP_WRKR_TLS_NODEPORT
        SEPP_WRKR_TLS_PORT
        VIP_SIG_BSF (from network config parameter 'eric_sc_values_anchor_parameter_VIP_SIG_BSF')
        VIP_SIG_SCP (from network config parameter 'eric_sc_values_anchor_parameter_VIP_SIG_SCP')
        VIP_SIG_SEPP (from network config parameter 'eric_sc_values_anchor_parameter_VIP_SIG_SEPP')
        VIP_SIG2_SEPP (from network config parameter 'eric_sc_values_anchor_parameter_VIP_SIG2_SEPP')
        WORKER_IP (deprecated, same as NODE_TRAFFIC_IP)

    - From the Tools Namespace
        CHFSIM_1_IP
        CHFSIM_1_NODEPORT_HTTP
        CHFSIM_1_NODEPORT_HTTPS
        CHFSIM_1_PORT_HTTP
        CHFSIM_1_PORT_HTTPS
        CHFSIM_2_IP
        CHFSIM_2_NODEPORT_HTTP
        CHFSIM_2_NODEPORT_HTTPS
        CHFSIM_2_PORT_HTTP
        CHFSIM_2_PORT_HTTPS
        CHFSIM_3_IP
        CHFSIM_3_NODEPORT_HTTP
        CHFSIM_3_NODEPORT_HTTPS
        CHFSIM_3_PORT_HTTP
        CHFSIM_3_PORT_HTTPS
        CHFSIM_4_IP
        CHFSIM_4_NODEPORT_HTTP
        CHFSIM_4_NODEPORT_HTTPS
        CHFSIM_4_PORT_HTTP
        CHFSIM_4_PORT_HTTPS
        CHFSIM_5_IP
        CHFSIM_5_NODEPORT_HTTP
        CHFSIM_5_NODEPORT_HTTPS
        CHFSIM_5_PORT_HTTP
        CHFSIM_5_PORT_HTTPS
        CHFSIM_6_IP
        CHFSIM_6_NODEPORT_HTTP
        CHFSIM_6_NODEPORT_HTTPS
        CHFSIM_6_PORT_HTTP
        CHFSIM_6_PORT_HTTPS
        CHFSIM_7_IP
        CHFSIM_7_NODEPORT_HTTP
        CHFSIM_7_NODEPORT_HTTPS
        CHFSIM_7_PORT_HTTP
        CHFSIM_7_PORT_HTTPS
        CHFSIM_8_IP
        CHFSIM_8_NODEPORT_HTTP
        CHFSIM_8_NODEPORT_HTTPS
        CHFSIM_8_PORT_HTTP
        CHFSIM_8_PORT_HTTPS
        CHFSIM_9_IP
        CHFSIM_9_NODEPORT_HTTP
        CHFSIM_9_NODEPORT_HTTPS
        CHFSIM_9_PORT_HTTP
        CHFSIM_9_PORT_HTTPS
        CHFSIM_10_IP
        CHFSIM_10_NODEPORT_HTTP
        CHFSIM_10_NODEPORT_HTTPS
        CHFSIM_10_PORT_HTTP
        CHFSIM_10_PORT_HTTPS
        CHFSIM_11_IP
        CHFSIM_11_NODEPORT_HTTP
        CHFSIM_11_NODEPORT_HTTPS
        CHFSIM_11_PORT_HTTP
        CHFSIM_11_PORT_HTTPS
        CHFSIM_12_IP
        CHFSIM_12_NODEPORT_HTTP
        CHFSIM_12_NODEPORT_HTTPS
        CHFSIM_12_PORT_HTTP
        CHFSIM_12_PORT_HTTPS
        FIRST_WORKER_IP_TOOLS (INTERNAL-IP of first worker node)
        NODE_TRAFFIC_IP_TOOLS (from network config parameter 'tools_node_worker_address', which is normally IPv4 address from eth2 interface of first worker node)
        NODE_TRAFFIC_IPV6_TOOLS (from network config parameter 'tools_node_worker_address', which is normally IPv4 address from eth2 interface of first worker node)
        NRFSIM_IP
        NRFSIM_NODEPORT_HTTP
        NRFSIM_NODEPORT_HTTPS
        NRFSIM_PORT (deprecated, same as NRFSIM_NODEPORT_HTTP)
        NRFSIM_PORT_HTTP
        NRFSIM_PORT_HTTPS
        SEPPSIM_P1_IP
        SEPPSIM_P1_NODEPORT (deprecated, same port as SEPPSIM_P1_NODEPORT_HTTPS)
        SEPPSIM_P1_NODEPORT_HTTP
        SEPPSIM_P1_NODEPORT_HTTPS
        SEPPSIM_P1_PORT_HTTP
        SEPPSIM_P1_PORT_HTTPS
        SEPPSIM_P2_IP
        SEPPSIM_P2_NODEPORT (deprecated, same port as SEPPSIM_P2_NODEPORT_HTTPS)
        SEPPSIM_P2_NODEPORT_HTTP
        SEPPSIM_P2_NODEPORT_HTTPS
        SEPPSIM_P2_PORT_HTTP
        SEPPSIM_P2_PORT_HTTPS
        SEPPSIM_P3_IP
        SEPPSIM_P3_NODEPORT (deprecated, same port as SEPPSIM_P3_NODEPORT_HTTPS)
        SEPPSIM_P3_NODEPORT_HTTP
        SEPPSIM_P3_NODEPORT_HTTPS
        SEPPSIM_P3_PORT_HTTP
        SEPPSIM_P3_PORT_HTTPS
        SEPPSIM_P4_IP
        SEPPSIM_P4_NODEPORT (deprecated, same port as SEPPSIM_P4_NODEPORT_HTTPS)
        SEPPSIM_P4_NODEPORT_HTTP
        SEPPSIM_P4_NODEPORT_HTTPS
        SEPPSIM_P4_PORT_HTTP
        SEPPSIM_P4_PORT_HTTPS
        SEPPSIM_P5_IP
        SEPPSIM_P5_NODEPORT (deprecated, same port as SEPPSIM_P5_NODEPORT_HTTPS)
        SEPPSIM_P5_NODEPORT_HTTP
        SEPPSIM_P5_NODEPORT_HTTPS
        SEPPSIM_P5_PORT_HTTP
        SEPPSIM_P5_PORT_HTTPS
        SEPPSIM_P6_IP
        SEPPSIM_P6_NODEPORT (deprecated, same port as SEPPSIM_P6_NODEPORT_HTTPS)
        SEPPSIM_P6_NODEPORT_HTTP
        SEPPSIM_P6_NODEPORT_HTTPS
        SEPPSIM_P6_PORT_HTTP
        SEPPSIM_P6_PORT_HTTPS
        SEPPSIM_P7_IP
        SEPPSIM_P7_NODEPORT (deprecated, same port as SEPPSIM_P7_NODEPORT_HTTPS)
        SEPPSIM_P7_NODEPORT_HTTP
        SEPPSIM_P7_NODEPORT_HTTPS
        SEPPSIM_P7_PORT_HTTP
        SEPPSIM_P7_PORT_HTTPS
        SEPPSIM_P8_IP
        SEPPSIM_P8_NODEPORT (deprecated, same port as SEPPSIM_P8_NODEPORT_HTTPS)
        SEPPSIM_P8_NODEPORT_HTTP
        SEPPSIM_P8_NODEPORT_HTTPS
        SEPPSIM_P8_PORT_HTTP
        SEPPSIM_P8_PORT_HTTPS
        WORKER_IP_TOOLS (deprecated, same as NODE_TRAFFIC_IP_TOOLS)

The following job/playlist variables variables controls if place holder in a file
is replaced by values from the \%::JOB_PARAMS hash:

    - P938_REPLACE_FILE_PLACEHOLDERS (yes or no, default is no)
    - P938_INPUT_FILE_NAME (file path to input file)
    - P938_OUTPUT_FILE_NAME (file path to output file, can be same as P938_INPUT_FILE_NAME)
    - P938_OUTPUT_FILE_ACCESSMODE (default '666')

It is also possible to do the replacement of place holders by calling the following
sub-routine in this playlist with the following values:

    - \&Playlist::938_Node_Information::replace_place_holders_in_file(
            "input file name",
            "output file name",
            "output file access rights"
        )
        It returns 0 at success and 1 at failure.

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

package Playlist::906_Deploy_Postprocess;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.11
#  Date     : 2023-12-21 10:27:41
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2023
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
    );

# Used Perl package files
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;
use Playlist::909_Execute_Playlist_Extensions;

my $debug_command = "";

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

    # Check if we should skip the execution of certain commands
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Show_Message_P906S01, undef );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Process_Playlist_Extensions_Command_Files_P906S02, undef );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Post_Deploy_Tasks_P906S03, undef );
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
sub Show_Message_P906S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Show_Tasks_To_Be_Done_P906S01T01 } );
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
    sub Show_Tasks_To_Be_Done_P906S01T01 {

        my $message = "";
        my $rc = 0;

        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Deployment Post Processing",
                "messages"      => [
                    "",
                    "Tasks to be done:",
                    "- Execute playlist extensions command files, if any",
                    "",
                ],
                "align-text"    => "left",
                "return-output" => \$message,
            }
        );
        General::Logging::log_user_message($message);

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
sub Process_Playlist_Extensions_Command_Files_P906S02 {

    my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
    my $rc;

    # Set 909 playlist parameters to get default behaviour
    $::JOB_PARAMS{'P909_HIDE_ERROR_MESSAGES'} = "no";
    $::JOB_PARAMS{'P909_JUST_PRINT'} = "no";
    $::JOB_PARAMS{'P909_MATCH_REGEX'} = "";
    $::JOB_PARAMS{'P909_SHOW_APPLIED_FILTERS'} = "yes";
    $::JOB_PARAMS{'P909_SHOW_COMMAND_EXECUTION'} = "no";
    $::JOB_PARAMS{'P909_SHOW_IGNORED_FILES'} = "no";
    $::JOB_PARAMS{'P909_SHOW_PROGRESS'} = "yes";
    $::JOB_PARAMS{'P909_SHOW_SKIPPED_FILES'} = "no";
    $::JOB_PARAMS{'P909_SHOW_VALID_FILES'} = "no";
    $::JOB_PARAMS{'P909_STOP_ON_ERROR'} = "yes";

    $::JOB_PARAMS{'PLAYLIST_EXTENSION_PATH'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/playlist_extensions/$app_type/deploy/post";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::909_Execute_Playlist_Extensions::main } );
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
sub Post_Deploy_Tasks_P906S03 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Authorized_Keys_File_P906S03T01 } );
    return $rc if $rc < 0;

    # Commented out this logic on request from Kay 2023-12-01 14:00
    #$rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_SEPP_coredns_Settings_P906S03T02 } );
    #return $rc if $rc < 0;

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
    sub Update_Authorized_Keys_File_P906S03T01 {

        my $rc = 0;

        if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/authorized_keys/authorized_keys_Template") {
            General::Logging::log_user_message("Updating the authorized_keys file");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/update_authorized_keys.pl -f $::JOB_PARAMS{'_PACKAGE_DIR'}/templates/authorized_keys/authorized_keys_Template",
                    "hide-output"   => 1,
                }
            );

            if ($rc == 0) {
                push @::JOB_STATUS, '(/) Updated the ~/.ssh/autorized_keys file on the node';
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to update authorized_keys file");
            }
        } else {
            General::Logging::log_user_message("No template file exists, so nothing to be done");
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Added as part of DND-43536.
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
    sub Update_SEPP_coredns_Settings_P906S03T02 {

        my $filename;
        my @coredns_configmap = ();
        my @coredns_deployment = (
            '{',
            '    "spec": {',
            '        "template": {',
            '            "spec": {',
            '                "volumes": [',
            '                    {',
            '                        "configMap": {',
            '                            "items": [',
            '                                {',
            '                                    "key": "Corefile",',
            '                                    "path": "Corefile"',
            '                                },',
            '                                {',
            '                                    "key": "psepp11.db",',
            '                                    "path": "psepp11.db"',
            '                                },',
            '                                {',
            '                                    "key": "psepp12.db",',
            '                                    "path": "psepp12.db"',
            '                                },',
            '                                {',
            '                                    "key": "psepp21.db",',
            '                                    "path": "psepp21.db"',
            '                                },',
            '                                {',
            '                                    "key": "psepp31.db",',
            '                                    "path": "psepp31.db"',
            '                                },',
            '                                {',
            '                                    "key": "nfudm.db",',
            '                                    "path": "nfudm.db"',
            '                                },',
            '                                {',
            '                                    "key": "osepp3.db",',
            '                                    "path": "osepp3.db"',
            '                                },',
            '                                {',
            '                                    "key": "osepp5.db",',
            '                                    "path": "osepp5.db"',
            '                                },',
            '                                {',
            '                                    "key": "osepp8.db",',
            '                                    "path": "osepp8.db"',
            '                                },',
            '                                {',
            '                                    "key": "osepp12.db",',
            '                                    "path": "osepp12.db"',
            '                                }',
            '                            ]',
            '                        },',
            '                        "name": "config-volume"',
            '                    }',
            '                ]',
            '            }',
            '        }',
            '    }',
            '}',
        );
        my @ip = split /,/, $::NETWORK_CONFIG_PARAMS{'node_worker_address'}{'value'};   # Or should this be tools_node_worker_address?
        my $ipv4 = "";
        my $ipv6 = "";
        my $rc = 0;

        if (scalar @ip == 1) {
            if ($ip[0] =~ /:/) {
                $ipv6 = $ip[0];
            } else {
                $ipv4 = $ip[0];
            }
        } elsif (scalar @ip == 2) {
            if ($ip[0] =~ /:/) {
                $ipv6 = $ip[0];
            } else {
                $ipv4 = $ip[0];
            }
            if ($ip[1] =~ /:/) {
                $ipv6 = $ip[1];
            } else {
                $ipv4 = $ip[1];
            }
        }

        if ($ipv4 ne "" && $ipv6 ne "") {
            # Multiple addresses
            @coredns_configmap = (
                '{',
                '    "data": {',
                '        "Corefile": ".:53 {\n    log\n    errors\n    health {\n        lameduck 5s\n    }\n    ready\n    prometheus :9153\n    kubernetes cluster.local in-addr.arpa ip6.arpa {\n        pods insecure\n        fallthrough in-addr.arpa ip6.arpa\n        ttl 30\n    }\n    file /etc/coredns/psepp11.db psepp11.5gc.mnc012.mcc210.3gppnetwork.org\n    file /etc/coredns/psepp12.db psepp12.5gc.mnc012.mcc210.3gppnetwork.org\n    file /etc/coredns/psepp21.db pSepp21.5gc.mnc123.mcc321.3gppnetwork.org\n    file /etc/coredns/psepp31.db pSepp31.5gc.mnc234.mcc432.3gppnetwork.org\n    file /etc/coredns/nfudm.db   5gc.mnc567.mcc765.3gppnetwork.org\n    file /etc/coredns/osepp12.db  region1.udm.5gc.mnc073.mcc262.3gppnetwork.org\n    file /etc/coredns/osepp3.db   region1.amf.5gc.mnc073.mcc262.3gppnetwork.org\n    file /etc/coredns/osepp5.db   region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org\n    file /etc/coredns/osepp8.db   region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org\n    hosts {\n        fallthrough\n    }\n    forward . dns://10.221.16.10 dns://10.221.16.11 {\n        prefer_udp\n    }\n    cache 30 {\n      success 9984 30\n      denial 9984 30\n    }\n    rewrite name k8s-registry.eccd.local  ingress-nginx.ingress-nginx.svc.cluster.local\n    rewrite name auth.eccd.local  ingress-nginx.ingress-nginx.svc.cluster.local\n    loop\n    reload\n    loadbalance\n}\n",',
                (sprintf '        "nfudm.db": "; 5gc.mnc567.mcc765.3gppnetwork.org test file\n5gc.mnc567.mcc765.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfUdm11.5gc.mnc567.mcc765.3gppnetwork.org.            IN      A       %s\nnfUdm12.5gc.mnc567.mcc765.3gppnetwork.org.            IN      A       %s\nnfUdm13.5gc.mnc567.mcc765.3gppnetwork.org.            IN      A       %s\nnfUdm14.5gc.mnc567.mcc765.3gppnetwork.org.            IN      A       %s\nnfUdm11.5gc.mnc567.mcc765.3gppnetwork.org.            IN      AAAA    %s\nnfUdm12.5gc.mnc567.mcc765.3gppnetwork.org.            IN      AAAA    %s\nnfUdm13.5gc.mnc567.mcc765.3gppnetwork.org.            IN      AAAA    %s\nnfUdm14.5gc.mnc567.mcc765.3gppnetwork.org.            IN      AAAA    %s\n",', $ipv4, $ipv4, $ipv4, $ipv4, $ipv6, $ipv6, $ipv6, $ipv6),
                (sprintf '        "osepp3.db":  "; region1.amf.5gc.mnc073.mcc262.3gppnetwork.org test file\nregion1.amf.5gc.mnc073.mcc262.3gppnetwork.org.    IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfamf.region1.amf.5gc.mnc073.mcc262.3gppnetwork.org.    IN      A       %s\nnfamf.region1.amf.5gc.mnc073.mcc262.3gppnetwork.org.    IN      AAAA    %s\n",', $ipv4, $ipv6),
                (sprintf '        "osepp5.db":  "; region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org test file\nregion1.sepp.5gc.mnc033.mcc206.3gppnetwork.org.  IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nsepp-be.region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org. IN      A       %s\nsepp-be.region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org. IN      AAAA    %s\n",', $ipv4, $ipv6),
                (sprintf '        "osepp8.db":  "; region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org test file\nregion1.sepp.5gc.mnc060.mcc240.3gppnetwork.org.  IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nsepp-se.region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org. IN      A       %s\nsepp-se.region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org. IN      AAAA    %s\n",', $ipv4, $ipv6),
                (sprintf '        "osepp12.db": "; region1.udm.5gc.mnc073.mcc262.3gppnetwork.org test file\nregion1.udm.5gc.mnc073.mcc262.3gppnetwork.org.    IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfudm1.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org.   IN      A       %s\nnfudm2.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org.  IN      A       %s\nnfudm2.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org.  IN      AAAA    %s\n",', $ipv4, $ipv4, $ipv6, $ipv6),
                (sprintf '        "psepp11.db": "; psepp11.5gc.mnc012.mcc210.3gppnetwork.org test file\npsepp11.5gc.mnc012.mcc210.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp11.5gc.mnc012.mcc210.3gppnetwork.org.            IN      A       %s\npSepp11.5gc.mnc012.mcc210.3gppnetwork.org.            IN      AAAA    %s\n",', $ipv4, $ipv6),
                (sprintf '        "psepp12.db": "; psepp12.5gc.mnc012.mcc210.3gppnetwork.org test file\npsepp12.5gc.mnc012.mcc210.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp12.5gc.mnc012.mcc210.3gppnetwork.org.            IN      A       %s\npSepp12.5gc.mnc012.mcc210.3gppnetwork.org.            IN      AAAA    %s\n",', $ipv4, $ipv6),
                (sprintf '        "psepp21.db": "; psepp21.5gc.mnc123.mcc321.3gppnetwork.org test file\npsepp21.5gc.mnc123.mcc321.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp21.5gc.mnc123.mcc321.3gppnetwork.org.            IN      A       %s\npSepp21.5gc.mnc123.mcc321.3gppnetwork.org.            IN      AAAA    %s\n",', $ipv4, $ipv6),
                (sprintf '        "psepp31.db": "; pSepp31.5gc.mnc234.mcc432.3gppnetwork.org test file\npSepp31.5gc.mnc234.mcc432.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp31.5gc.mnc234.mcc432.3gppnetwork.org.            IN      A       %s\npSepp31.5gc.mnc234.mcc432.3gppnetwork.org.            IN      AAAA    %s\n"', $ipv4, $ipv6),
                '    }',
                '}',
            );
        } elsif ($ipv4 ne "") {
            @coredns_configmap = (
                '{',
                '    "data": {',
                '        "Corefile": ".:53 {\n    log\n    errors\n    health {\n        lameduck 5s\n    }\n    ready\n    prometheus :9153\n    kubernetes cluster.local in-addr.arpa ip6.arpa {\n        pods insecure\n        fallthrough in-addr.arpa ip6.arpa\n        ttl 30\n    }\n    file /etc/coredns/psepp11.db psepp11.5gc.mnc012.mcc210.3gppnetwork.org\n    file /etc/coredns/psepp12.db psepp12.5gc.mnc012.mcc210.3gppnetwork.org\n    file /etc/coredns/psepp21.db pSepp21.5gc.mnc123.mcc321.3gppnetwork.org\n    file /etc/coredns/psepp31.db pSepp31.5gc.mnc234.mcc432.3gppnetwork.org\n    file /etc/coredns/nfudm.db   5gc.mnc567.mcc765.3gppnetwork.org\n    file /etc/coredns/osepp12.db  region1.udm.5gc.mnc073.mcc262.3gppnetwork.org\n    file /etc/coredns/osepp3.db   region1.amf.5gc.mnc073.mcc262.3gppnetwork.org\n    file /etc/coredns/osepp5.db   region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org\n    file /etc/coredns/osepp8.db   region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org\n    hosts {\n        fallthrough\n    }\n    forward . dns://10.221.16.10 dns://10.221.16.11 {\n        prefer_udp\n    }\n    cache 30 {\n      success 9984 30\n      denial 9984 30\n    }\n    rewrite name k8s-registry.eccd.local  ingress-nginx.ingress-nginx.svc.cluster.local\n    rewrite name auth.eccd.local  ingress-nginx.ingress-nginx.svc.cluster.local\n    loop\n    reload\n    loadbalance\n}\n",',
                (sprintf '        "nfudm.db": "; 5gc.mnc567.mcc765.3gppnetwork.org test file\n5gc.mnc567.mcc765.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfUdm11.5gc.mnc567.mcc765.3gppnetwork.org.            IN      A       %s\nnfUdm12.5gc.mnc567.mcc765.3gppnetwork.org.            IN      A       %s\nnfUdm13.5gc.mnc567.mcc765.3gppnetwork.org.            IN      A       %s\nnfUdm14.5gc.mnc567.mcc765.3gppnetwork.org.            IN      A       %s\n",', $ipv4, $ipv4, $ipv4, $ipv4),
                (sprintf '        "osepp3.db":  "; region1.amf.5gc.mnc073.mcc262.3gppnetwork.org test file\nregion1.amf.5gc.mnc073.mcc262.3gppnetwork.org.    IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfamf.region1.amf.5gc.mnc073.mcc262.3gppnetwork.org.    IN      A       %s\n",', $ipv4),
                (sprintf '        "osepp5.db":  "; region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org test file\nregion1.sepp.5gc.mnc033.mcc206.3gppnetwork.org.  IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nsepp-be.region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org. IN      A       %s\n",', $ipv4),
                (sprintf '        "osepp8.db":  "; region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org test file\nregion1.sepp.5gc.mnc060.mcc240.3gppnetwork.org.  IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nsepp-se.region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org. IN      A       %s\n",', $ipv4),
                (sprintf '        "osepp12.db": "; region1.udm.5gc.mnc073.mcc262.3gppnetwork.org test file\nregion1.udm.5gc.mnc073.mcc262.3gppnetwork.org.    IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfudm1.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org.   IN      A       %s\nnfudm2.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org.  IN      A       %s\n",', $ipv4, $ipv4),
                (sprintf '        "psepp11.db": "; psepp11.5gc.mnc012.mcc210.3gppnetwork.org test file\npsepp11.5gc.mnc012.mcc210.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp11.5gc.mnc012.mcc210.3gppnetwork.org.            IN      A       %s\n",', $ipv4),
                (sprintf '        "psepp12.db": "; psepp12.5gc.mnc012.mcc210.3gppnetwork.org test file\npsepp12.5gc.mnc012.mcc210.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp12.5gc.mnc012.mcc210.3gppnetwork.org.            IN      A       %s\n",', $ipv4),
                (sprintf '        "psepp21.db": "; psepp21.5gc.mnc123.mcc321.3gppnetwork.org test file\npsepp21.5gc.mnc123.mcc321.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp21.5gc.mnc123.mcc321.3gppnetwork.org.            IN      A       %s\n",', $ipv4),
                (sprintf '        "psepp31.db": "; pSepp31.5gc.mnc234.mcc432.3gppnetwork.org test file\npSepp31.5gc.mnc234.mcc432.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp31.5gc.mnc234.mcc432.3gppnetwork.org.            IN      A       %s\n"', $ipv4),
                '    }',
                '}',
            );
        } elsif ($ipv6 ne "") {
            @coredns_configmap = (
                '{',
                '    "data": {',
                '        "Corefile": ".:53 {\n    log\n    errors\n    health {\n        lameduck 5s\n    }\n    ready\n    prometheus :9153\n    kubernetes cluster.local in-addr.arpa ip6.arpa {\n        pods insecure\n        fallthrough in-addr.arpa ip6.arpa\n        ttl 30\n    }\n    file /etc/coredns/psepp11.db psepp11.5gc.mnc012.mcc210.3gppnetwork.org\n    file /etc/coredns/psepp12.db psepp12.5gc.mnc012.mcc210.3gppnetwork.org\n    file /etc/coredns/psepp21.db pSepp21.5gc.mnc123.mcc321.3gppnetwork.org\n    file /etc/coredns/psepp31.db pSepp31.5gc.mnc234.mcc432.3gppnetwork.org\n    file /etc/coredns/nfudm.db   5gc.mnc567.mcc765.3gppnetwork.org\n    file /etc/coredns/osepp12.db  region1.udm.5gc.mnc073.mcc262.3gppnetwork.org\n    file /etc/coredns/osepp3.db   region1.amf.5gc.mnc073.mcc262.3gppnetwork.org\n    file /etc/coredns/osepp5.db   region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org\n    file /etc/coredns/osepp8.db   region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org\n    hosts {\n        fallthrough\n    }\n    forward . dns://10.221.16.10 dns://10.221.16.11 {\n        prefer_udp\n    }\n    cache 30 {\n      success 9984 30\n      denial 9984 30\n    }\n    rewrite name k8s-registry.eccd.local  ingress-nginx.ingress-nginx.svc.cluster.local\n    rewrite name auth.eccd.local  ingress-nginx.ingress-nginx.svc.cluster.local\n    loop\n    reload\n    loadbalance\n}\n",',
                (sprintf '        "nfudm.db": "; 5gc.mnc567.mcc765.3gppnetwork.org test file\n5gc.mnc567.mcc765.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfUdm11.5gc.mnc567.mcc765.3gppnetwork.org.            IN      AAAA    %s\nnfUdm12.5gc.mnc567.mcc765.3gppnetwork.org.            IN      AAAA    %s\nnfUdm13.5gc.mnc567.mcc765.3gppnetwork.org.            IN      AAAA    %s\nnfUdm14.5gc.mnc567.mcc765.3gppnetwork.org.            IN      AAAA    %s\n",', $ipv6, $ipv6, $ipv6, $ipv6),
                (sprintf '        "osepp3.db":  "; region1.amf.5gc.mnc073.mcc262.3gppnetwork.org test file\nregion1.amf.5gc.mnc073.mcc262.3gppnetwork.org.    IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfamf.region1.amf.5gc.mnc073.mcc262.3gppnetwork.org.    IN      AAAA    %s\n",', $ipv6),
                (sprintf '        "osepp5.db":  "; region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org test file\nregion1.sepp.5gc.mnc033.mcc206.3gppnetwork.org.  IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nsepp-be.region1.sepp.5gc.mnc033.mcc206.3gppnetwork.org. IN      AAAA    %s\n",', $ipv6),
                (sprintf '        "osepp8.db":  "; region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org test file\nregion1.sepp.5gc.mnc060.mcc240.3gppnetwork.org.  IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nsepp-se.region1.sepp.5gc.mnc060.mcc240.3gppnetwork.org. IN      AAAA    %s\n",', $ipv6),
                (sprintf '        "osepp12.db": "; region1.udm.5gc.mnc073.mcc262.3gppnetwork.org test file\nregion1.udm.5gc.mnc073.mcc262.3gppnetwork.org.    IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\nnfudm1.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org.   IN      AAAA    %s\nnfudm2.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org.  IN      A       %s\n",', $ipv6, $ipv6),
                (sprintf '        "psepp11.db": "; psepp11.5gc.mnc012.mcc210.3gppnetwork.org test file\npsepp11.5gc.mnc012.mcc210.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp11.5gc.mnc012.mcc210.3gppnetwork.org.            IN      AAAA    %s\n",', $ipv6),
                (sprintf '        "psepp12.db": "; psepp12.5gc.mnc012.mcc210.3gppnetwork.org test file\npsepp12.5gc.mnc012.mcc210.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp12.5gc.mnc012.mcc210.3gppnetwork.org.            IN      AAAA    %s\n",', $ipv6),
                (sprintf '        "psepp21.db": "; psepp21.5gc.mnc123.mcc321.3gppnetwork.org test file\npsepp21.5gc.mnc123.mcc321.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp21.5gc.mnc123.mcc321.3gppnetwork.org.            IN      AAAA    %s\n",', $ipv6),
                (sprintf '        "psepp31.db": "; pSepp31.5gc.mnc234.mcc432.3gppnetwork.org test file\npSepp31.5gc.mnc234.mcc432.3gppnetwork.org.            IN      SOA     sns.dns.icann.org. noc.dns.icann.org. 2022082541 7200 3600 1209600 3600\npSepp31.5gc.mnc234.mcc432.3gppnetwork.org.            IN      AAAA    %s\n"', $ipv6),
                '    }',
                '}',
            );
        } else {
            # Should not happen
            General::Logging::log_user_error_message("Failed to find one or two IP-addresses in network config parameter 'node_worker_address'");
            return 1;
        }

        #
        # configmap
        #

        $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/coredns_configmap.json";
        $rc = General::File_Operations::write_file(
            {
                "filename"          => $filename,
                "output-ref"        => \@coredns_configmap,
                "file-access-mode"  => "666"
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to create 'coredns_configmap.json' file in workspace directory");
            return 1;
        }

        General::Logging::log_user_message("Printing the coredns configmap");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get configmap coredns -n kube-system -o json",
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to print coredns configmap");
            return 1;
        }

        General::Logging::log_user_message("Updating the coredns configmap");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} patch configmap coredns -n kube-system --type merge -p \"\$(cat $filename)\"",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Update of coredns configmap successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, '(x) Update of coredns configmap failed';
            General::Logging::log_user_error_message("Failed to update coredns configmap");
            return 1;
        }

        #
        # deployment
        #

        $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/coredns_deployment.json";
        $rc = General::File_Operations::write_file(
            {
                "filename"          => $filename,
                "output-ref"        => \@coredns_deployment,
                "file-access-mode"  => "666"
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to create 'coredns_deployment.json' file in workspace directory");
            return 1;
        }

        General::Logging::log_user_message("Printing the coredns deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get deployment coredns -n kube-system -o json",
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to print coredns deployment");
            return 1;
        }

        General::Logging::log_user_message("Updating the coredns deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} patch deployment coredns -n kube-system -p \"\$(cat $filename)\"",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Update of coredns deployment successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, '(x) Update of coredns deployment failed';
            General::Logging::log_user_error_message("Failed to update coredns deployment");
            return 1;
        }

        return $rc;
    }
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist is used for executing tasks after an Installation has been done.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;

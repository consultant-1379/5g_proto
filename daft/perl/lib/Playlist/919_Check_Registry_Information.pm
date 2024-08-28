package Playlist::919_Check_Registry_Information;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.11
#  Date     : 2024-05-27 11:07:27
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2023
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
use General::Json_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

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

    $rc = General::Playlist_Operations::execute_step( \&Prepare_Registry_Information_P919S01, undef );
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
sub Prepare_Registry_Information_P919S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Registry_Information_P919S01T01 } );
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
    sub Check_Registry_Information_P919S01T01 {

        my $crd_registry_password       = exists $::NETWORK_CONFIG_PARAMS{'private_crd_registry_password'} ? $::NETWORK_CONFIG_PARAMS{'private_crd_registry_password'}{'value'} : "";
        my $crd_registry_port           = exists $::NETWORK_CONFIG_PARAMS{'private_crd_registry_port'}     ? $::NETWORK_CONFIG_PARAMS{'private_crd_registry_port'}{'value'}     : "";
        my $crd_registry_secret_name    = exists $::NETWORK_CONFIG_PARAMS{'private_crd_registry_secret'}   ? $::NETWORK_CONFIG_PARAMS{'private_crd_registry_secret'}{'value'}   : "";
        my $crd_registry_url            = exists $::NETWORK_CONFIG_PARAMS{'private_crd_registry_url'}      ? $::NETWORK_CONFIG_PARAMS{'private_crd_registry_url'}{'value'}      : "";
        my $crd_registry_user           = exists $::NETWORK_CONFIG_PARAMS{'private_crd_registry_user'}     ? $::NETWORK_CONFIG_PARAMS{'private_crd_registry_user'}{'value'}     : "";
        my $key_name_secret             = "";
        my $key_name_url                = "";
        my $param_name;
        my $rc                          = 0;
        my @result;
        my $sc_registry_password        = exists $::NETWORK_CONFIG_PARAMS{'private_registry_password'}     ? $::NETWORK_CONFIG_PARAMS{'private_registry_password'}{'value'}     : "";
        my $sc_registry_port            = exists $::NETWORK_CONFIG_PARAMS{'private_registry_port'}         ? $::NETWORK_CONFIG_PARAMS{'private_registry_port'}{'value'}         : "";
        my $sc_registry_secret_name     = "";
        my $sc_registry_url             = "";
        my $sc_registry_user            = exists $::NETWORK_CONFIG_PARAMS{'private_registry_user'}         ? $::NETWORK_CONFIG_PARAMS{'private_registry_user'}{'value'}         : "";
        my $tools_registry_password     = exists $::NETWORK_CONFIG_PARAMS{'tools_registry_password'}       ? $::NETWORK_CONFIG_PARAMS{'tools_registry_password'}{'value'}       : "";
        my $tools_registry_port         = exists $::NETWORK_CONFIG_PARAMS{'tools_registry_port'}           ? $::NETWORK_CONFIG_PARAMS{'tools_registry_port'}{'value'}           : "";
        my $tools_registry_secret_name  = exists $::NETWORK_CONFIG_PARAMS{'tools_pull_secret'}             ? $::NETWORK_CONFIG_PARAMS{'tools_pull_secret'}{'value'}             : "";
        my $tools_registry_url          = exists $::NETWORK_CONFIG_PARAMS{'tools_registry_url'}            ? $::NETWORK_CONFIG_PARAMS{'tools_registry_url'}{'value'}            : "";
        my $tools_registry_user         = exists $::NETWORK_CONFIG_PARAMS{'tools_registry_user'}           ? $::NETWORK_CONFIG_PARAMS{'tools_registry_user'}{'value'}           : "";

        # Discard CHANGEME and REPLACEMEWITH_xxx values
        $crd_registry_password      = "" if ($crd_registry_password      =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $crd_registry_port          = "" if ($crd_registry_port          =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $crd_registry_secret_name   = "" if ($crd_registry_secret_name   =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $crd_registry_url           = "" if ($crd_registry_url           =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $crd_registry_user          = "" if ($crd_registry_user          =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $sc_registry_password       = "" if ($sc_registry_password       =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $sc_registry_port           = "" if ($sc_registry_port           =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $sc_registry_user           = "" if ($sc_registry_user           =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $tools_registry_password    = "" if ($tools_registry_password    =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $tools_registry_port        = "" if ($tools_registry_port        =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $tools_registry_secret_name = "" if ($tools_registry_secret_name =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $tools_registry_url         = "" if ($tools_registry_url         =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);
        $tools_registry_user        = "" if ($tools_registry_user        =~ /^(CHANGEME|REPLACEMEWITH_\S+)$/);

        #
        # Look for the registry URL and secret name in parameters read from the network configuration file
        #

        for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
            if ($key =~ /^(eric_sc_values_hash_parameter|eric_sc_values_anchor_parameter|eric_values_hash_parameter)_(\S+)$/) {
                $param_name = $2;
                if ($param_name =~ /^\d{2}$/) {
                    # Old naming format with a 2 digit number e.g. eric_sc_values_hash_parameter_28 or eric_sc_values_anchor_parameter_10
                    if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^(global\.registry\.url|private_registry_url)=(\S+)$/) {
                        $key_name_url = $key;
                        next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                        $sc_registry_url = $2;
                    } elsif ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^(global\.pullSecret|private_registry_secret_name)=(\S+)$/) {
                        $key_name_secret = $key;
                        next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                        $sc_registry_secret_name = $2;
                    }
                } else {
                    # New naming format with parameter name included e.g. eric_sc_values_hash_parameter_global.registry.url or eric_sc_values_anchor_parameter_private_registry_url
                    if ($param_name eq "global.registry.url" || $param_name eq "private_registry_url") {
                        $key_name_url = $key;
                        next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^CHANGEME$/);
                        $sc_registry_url = $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                    } elsif ($param_name eq "global.pullSecret" || $param_name eq "private_registry_secret_name") {
                        $key_name_secret = $key;
                        next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^CHANGEME$/);
                        $sc_registry_secret_name = $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                    }
                }
            }
        }

        # Set default values
        $::JOB_PARAMS{'CRD_REGISTRY_PASSWORD'} = "";
        $::JOB_PARAMS{'CRD_REGISTRY_PORT'} = "";
        $::JOB_PARAMS{'CRD_REGISTRY_SECRET_NAME'} = "";
        $::JOB_PARAMS{'CRD_REGISTRY_URL'} = "";
        $::JOB_PARAMS{'CRD_REGISTRY_USER'} = "";
        $::JOB_PARAMS{'OWN_REGISTRY_URL'} = "";
        $::JOB_PARAMS{'OWN_REGISTRY_PORT'} = "";
        $::JOB_PARAMS{'OWN_REGISTRY_USER'} = "";
        $::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'} = "";
        $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'} = $sc_registry_password;
        $::JOB_PARAMS{'SC_REGISTRY_PORT'} = $sc_registry_port;
        $::JOB_PARAMS{'SC_REGISTRY_SECRET_NAME'} = $sc_registry_secret_name;
        $::JOB_PARAMS{'SC_REGISTRY_URL'} = $sc_registry_url;
        $::JOB_PARAMS{'SC_REGISTRY_USER'} = $sc_registry_user;
        $::JOB_PARAMS{'TOOLS_REGISTRY_PASSWORD'} = $tools_registry_password;
        $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'} = $tools_registry_port;
        $::JOB_PARAMS{'TOOLS_REGISTRY_SECRET_NAME'} = $tools_registry_secret_name;
        $::JOB_PARAMS{'TOOLS_REGISTRY_URL'} = $tools_registry_url;
        $::JOB_PARAMS{'TOOLS_REGISTRY_USER'} = $tools_registry_user;

        #
        # Check if we have an own local registry
        #

        General::Logging::log_user_message("Check if own registry URL is used");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get ingress -n kube-system -o=custom-columns=NAME:.metadata.name,HOSTS:.spec.rules[*].host",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            # Try to find the own internal registry URL
            for (@result) {
                if (/^eric-lcm-container-registry-ingress\s+(\S+)/) {
                    next if ($1 eq "*");
                    # NAME                                  HOSTS
                    # ...
                    # eric-lcm-container-registry-ingress   k8s-registry.eccd.local
                    $::JOB_PARAMS{'OWN_REGISTRY_URL'} = $1;
                }
            }

            if ($::JOB_PARAMS{'OWN_REGISTRY_URL'} ne "") {
                # Try to find the own internal registry port number
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n ingress-nginx get svc ingress-nginx",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0) {
                    for (@result) {
                        if (/^ingress-nginx\s+\S+\s+\S+\s+\S+\s+\d+:\d+\/TCP,\d+:(\d+)\/TCP\s+\S+/) {
                            # NAME            TYPE       CLUSTER-IP                            EXTERNAL-IP   PORT(S)                      AGE
                            # ingress-nginx   NodePort   2001:1b70:6220:ad60:ffff:fffe::c1c1   <none>        80:30253/TCP,443:32398/TCP   41d
                            #                                                                                                 ----- <--What we need
                            $::JOB_PARAMS{'OWN_REGISTRY_PORT'} = "$1";
                        }
                    }
                }

                # Try to find the own internal registry user and password
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "sudo -n cat /root/.docker/config.json",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0) {
                    # The output might look something like this:
                    #{
                    #    "auths": {
                    #        "k8s-registry.eccd.local:31544": {
                    #            "auth": "YWRtaW46aEcyWHgwU2dudGJTSHJHcVNFSG9rM0paOXFrQlNaYnc="
                    #        }
                    #    }
                    #}
                    #
                    # or
                    #
                    #{
                    #    "auths": {
                    #        "armdocker.rnd.ericsson.se": {
                    #            "auth": "Y2VueGRlcGxveTpzamFnNDVrTkdIMmdqYUtISjMzNDRqVUFJMTIz"
                    #        },
                    #        "k8s-registry.eccd.local": {
                    #            "auth": "Y25pc2FkbWluOkNuaXNBZG1pbjEyM0A="
                    #        },
                    #        "k8s-registry.eccd.local:443": {
                    #            "auth": "Y25pc2FkbWluOkNuaXNBZG1pbjEyM0A="
                    #        },
                    #        "registry01.cni-104.seli.gic.ericsson.se": {
                    #            "auth": "ZXZuZm0tdXNlcjpEZWZhdWx0UC4xMjM0NQ=="
                    #        },
                    #        "registry01.cni-104.seli.gic.ericsson.se:443": {
                    #            "auth": "ZXZuZm0tdXNlcjpEZWZhdWx0UC4xMjM0NQ=="
                    #        }
                    #    }
                    #}
                    my %json;
                    $rc = General::Json_Operations::read_array(
                        {
                            "input"               => \@result,
                            "output-ref"          => \%json,
                        }
                    );
                    if ($rc == 0) {
                        if (exists $json{"auths"}) {
                            if ($::JOB_PARAMS{'OWN_REGISTRY_PORT'} ne "" &&
                                exists $json{"auths"}->{"$::JOB_PARAMS{'OWN_REGISTRY_URL'}:$::JOB_PARAMS{'OWN_REGISTRY_PORT'}"} &&
                                exists $json{"auths"}->{"$::JOB_PARAMS{'OWN_REGISTRY_URL'}:$::JOB_PARAMS{'OWN_REGISTRY_PORT'}"}->{"auth"}) {

                                my $decoded = General::OS_Operations::base64_decode( $json{"auths"}->{"$::JOB_PARAMS{'OWN_REGISTRY_URL'}:$::JOB_PARAMS{'OWN_REGISTRY_PORT'}"}->{"auth"} );
                                if ($decoded =~ /^(\S+?):(\S+)$/) {
                                    General::Logging::log_user_message(sprintf "Changed OWN_REGISTRY_USER on line %i\n", __LINE__) if ($::JOB_PARAMS{'OWN_REGISTRY_USER'} ne $1);
                                    General::Logging::log_user_message(sprintf "Changed OWN_REGISTRY_PASSWORD on line %i\n", __LINE__) if ($::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'} ne $2);
                                    $::JOB_PARAMS{'OWN_REGISTRY_USER'} = $1;
                                    $::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'} = $2;
                                    #General::Logging::log_user_message("Using the following Registry Information:\n  Registry Password:  $::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'}\n  Registry User:  $::JOB_PARAMS{'OWN_REGISTRY_USER'}\n");
                                }
                            } elsif (exists $json{"auths"}->{"$::JOB_PARAMS{'OWN_REGISTRY_URL'}"} &&
                                     exists $json{"auths"}->{"$::JOB_PARAMS{'OWN_REGISTRY_URL'}"}->{"auth"}) {

                                my $decoded = General::OS_Operations::base64_decode( $json{"auths"}->{"$::JOB_PARAMS{'OWN_REGISTRY_URL'}"}->{"auth"} );
                                if ($decoded =~ /^(\S+?):(\S+)$/) {
                                    General::Logging::log_user_message(sprintf "Changed OWN_REGISTRY_USER on line %i\n", __LINE__) if ($::JOB_PARAMS{'OWN_REGISTRY_USER'} ne $1);
                                    General::Logging::log_user_message(sprintf "Changed OWN_REGISTRY_PASSWORD on line %i\n", __LINE__) if ($::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'} ne $2);
                                    $::JOB_PARAMS{'OWN_REGISTRY_USER'} = $1;
                                    $::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'} = $2;
                                    #General::Logging::log_user_message("Using the following Registry Information:\n  Registry Password:  $::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'}\n  Registry User:  $::JOB_PARAMS{'OWN_REGISTRY_USER'}\n");
                                }
                            }
                        }
                    }
                }
            }
        }

        if ($::JOB_PARAMS{'OWN_REGISTRY_URL'} ne "" && $::JOB_PARAMS{'OWN_REGISTRY_PORT'} ne "") {
            # There is an own local registry, check if network config file contains a port number
            if ($::JOB_PARAMS{'SC_REGISTRY_PORT'} eq "") {
                if ($::JOB_PARAMS{'OWN_REGISTRY_URL'} eq $::JOB_PARAMS{'SC_REGISTRY_URL'}) {
                    General::Logging::log_user_message(sprintf "Changed SC_REGISTRY_PORT on line %i\n", __LINE__) if ($::JOB_PARAMS{'SC_REGISTRY_PORT'} ne $::JOB_PARAMS{'OWN_REGISTRY_PORT'});
                    $::JOB_PARAMS{'SC_REGISTRY_PORT'} = $::JOB_PARAMS{'OWN_REGISTRY_PORT'};
                } else {
                    General::Logging::log_user_error_message("No 'private_registry_port' specified and the own local registry read from node ($::JOB_PARAMS{'OWN_REGISTRY_URL'}) does not match '$key_name_url' read from network config file ($sc_registry_url)");
                    return 1;
                }
            }
        } else {
            # There is no own local registry, check if network config file contains a port number
            if ($::JOB_PARAMS{'SC_REGISTRY_PORT'} eq "") {
                General::Logging::log_user_error_message("No 'private_registry_port' specified in network config file");
                return 1;
            }
        }

        if ($::JOB_PARAMS{'SC_REGISTRY_PASSWORD'} eq "" || $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'} eq "CHANGEME") {
            if ($::JOB_PARAMS{'SC_REGISTRY_URL'} eq $::JOB_PARAMS{'OWN_REGISTRY_URL'} && $::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'} ne "") {
                General::Logging::log_user_message(sprintf "Changed SC_REGISTRY_PASSWORD on line %i\n", __LINE__) if ($::JOB_PARAMS{'SC_REGISTRY_PASSWORD'} ne $::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'});
                $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'} = $::JOB_PARAMS{'OWN_REGISTRY_PASSWORD'};
                #General::Logging::log_user_message("Using the following Registry Information:\n  Registry Password:  $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'}\n");
            } else {
                General::Logging::log_user_error_message("The network configuration parameter 'private_registry_password' must have a valid value");
                return 1;
            }
        }

        if ($::JOB_PARAMS{'SC_REGISTRY_PORT'} eq "") {
            General::Logging::log_user_error_message("The network configuration parameter 'private_registry_port' must have a valid value");
            return 1;
        }

        if ($::JOB_PARAMS{'SC_REGISTRY_SECRET_NAME'} eq "") {
            General::Logging::log_user_error_message("The network configuration parameter '$key_name_secret' must have a valid value");
            return 1;
        }

        if ($::JOB_PARAMS{'SC_REGISTRY_URL'} eq "") {
            General::Logging::log_user_error_message("The network configuration parameter '$key_name_url' must have a valid value");
            return 1;
        }

        if ($::JOB_PARAMS{'SC_REGISTRY_USER'} eq "" || $::JOB_PARAMS{'SC_REGISTRY_USER'} eq "CHANGEME") {
            if ($::JOB_PARAMS{'SC_REGISTRY_URL'} eq $::JOB_PARAMS{'OWN_REGISTRY_URL'} && $::JOB_PARAMS{'OWN_REGISTRY_USER'} ne "") {
                General::Logging::log_user_message(sprintf "Changed SC_REGISTRY_USER on line %i\n", __LINE__) if ($::JOB_PARAMS{'SC_REGISTRY_USER'} ne $::JOB_PARAMS{'OWN_REGISTRY_USER'});
                $::JOB_PARAMS{'SC_REGISTRY_USER'} = $::JOB_PARAMS{'OWN_REGISTRY_USER'};
                #General::Logging::log_user_message("Using the following Registry Information:\n  Registry User:  $::JOB_PARAMS{'SC_REGISTRY_USER'}\n");
            } else {
                General::Logging::log_user_error_message("The network configuration parameter 'private_registry_user' must have a valid value");
                return 1;
            }
        }

        #
        # Check if network config file contains CRD registry information, if not reuse information from SC registry
        #

        if ($crd_registry_password ne "") {
            $::JOB_PARAMS{'CRD_REGISTRY_PASSWORD'} = $crd_registry_password;
        } else {
            $::JOB_PARAMS{'CRD_REGISTRY_PASSWORD'} = $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'};
        }

        if ($crd_registry_secret_name ne "") {
            $::JOB_PARAMS{'CRD_REGISTRY_SECRET_NAME'} = $crd_registry_secret_name;
        } else {
            $::JOB_PARAMS{'CRD_REGISTRY_SECRET_NAME'} = $::JOB_PARAMS{'SC_REGISTRY_SECRET_NAME'};
        }

        if ($crd_registry_url ne "") {
            $::JOB_PARAMS{'CRD_REGISTRY_URL'} = $crd_registry_url;
        } else {
            $::JOB_PARAMS{'CRD_REGISTRY_URL'} = $::JOB_PARAMS{'SC_REGISTRY_URL'};
        }

        if ($crd_registry_user ne "") {
            $::JOB_PARAMS{'CRD_REGISTRY_USER'} = $crd_registry_user;
        } else {
            $::JOB_PARAMS{'CRD_REGISTRY_USER'} = $::JOB_PARAMS{'SC_REGISTRY_USER'};
        }

        if ($crd_registry_port ne "") {
            $::JOB_PARAMS{'CRD_REGISTRY_PORT'} = $crd_registry_port;
        } else {
            # No port number given
            if ($crd_registry_url eq $::JOB_PARAMS{'OWN_REGISTRY_URL'}) {
                $::JOB_PARAMS{'CRD_REGISTRY_PORT'} = $::JOB_PARAMS{'OWN_REGISTRY_PORT'};
            } else {
                $::JOB_PARAMS{'CRD_REGISTRY_PORT'} = $::JOB_PARAMS{'SC_REGISTRY_PORT'};
            }
        }

        #
        # Check if network config file contains tools registry information, if not reuse information from SC registry
        #

        if ($tools_registry_password ne "") {
            $::JOB_PARAMS{'TOOLS_REGISTRY_PASSWORD'} = $tools_registry_password;
        } else {
            $::JOB_PARAMS{'TOOLS_REGISTRY_PASSWORD'} = $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'};
        }

        if ($tools_registry_secret_name ne "") {
            $::JOB_PARAMS{'TOOLS_REGISTRY_SECRET_NAME'} = $tools_registry_secret_name;
        } else {
            $::JOB_PARAMS{'TOOLS_REGISTRY_SECRET_NAME'} = $::JOB_PARAMS{'SC_REGISTRY_SECRET_NAME'};
        }

        if ($tools_registry_url ne "") {
            $::JOB_PARAMS{'TOOLS_REGISTRY_URL'} = $tools_registry_url;
        } else {
            $::JOB_PARAMS{'TOOLS_REGISTRY_URL'} = $::JOB_PARAMS{'SC_REGISTRY_URL'};
        }

        if ($tools_registry_user ne "") {
            $::JOB_PARAMS{'TOOLS_REGISTRY_USER'} = $tools_registry_user;
        } else {
            $::JOB_PARAMS{'TOOLS_REGISTRY_USER'} = $::JOB_PARAMS{'SC_REGISTRY_USER'};
        }

        if ($tools_registry_port ne "") {
            $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'} = $tools_registry_port;
        } else {
            # No port number given
            if ($tools_registry_url eq $::JOB_PARAMS{'OWN_REGISTRY_URL'}) {
                $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'} = $::JOB_PARAMS{'OWN_REGISTRY_PORT'};
            } else {
                $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'} = $::JOB_PARAMS{'SC_REGISTRY_PORT'};
            }
        }

        #
        # Print information about the registry that will be used.
        #
        my $message = "Using the following Registry Information:\n";
        $message   .= "  CRD Registry Port:   $::JOB_PARAMS{'CRD_REGISTRY_PORT'}\n";
        $message   .= "  CRD Registry URL:    $::JOB_PARAMS{'CRD_REGISTRY_URL'}\n";
        $message   .= "  CRD Registry User:   $::JOB_PARAMS{'CRD_REGISTRY_USER'}\n";
        $message   .= "  CRD Secret Name:     $::JOB_PARAMS{'CRD_REGISTRY_SECRET_NAME'}\n";
        $message   .= "  SC Registry Port:    $::JOB_PARAMS{'SC_REGISTRY_PORT'}\n";
        $message   .= "  SC Registry URL:     $::JOB_PARAMS{'SC_REGISTRY_URL'}\n";
        $message   .= "  SC Registry User:    $::JOB_PARAMS{'SC_REGISTRY_USER'}\n";
        $message   .= "  SC Secret Name:      $::JOB_PARAMS{'SC_REGISTRY_SECRET_NAME'}\n";
        $message   .= "  TOOLS Registry Port: $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'}\n";
        $message   .= "  TOOLS Registry URL:  $::JOB_PARAMS{'TOOLS_REGISTRY_URL'}\n";
        $message   .= "  TOOLS Registry User: $::JOB_PARAMS{'TOOLS_REGISTRY_USER'}\n";
        $message   .= "  TOOLS Secret Name:   $::JOB_PARAMS{'TOOLS_REGISTRY_SECRET_NAME'}\n";
        General::Logging::log_user_message($message);

        return 0;
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

This playlist check network configuration parameters for registry information
and checks if an own local ECCD registry is deployed.
It will update the following global \$::JOB_PARAMS variables:

    - CRD_REGISTRY_PASSWORD
    - CRD_REGISTRY_PORT
    - CRD_REGISTRY_SECRET_NAME
    - CRD_REGISTRY_URL
    - CRD_REGISTRY_USER
    - OWN_REGISTRY_PORT
    - OWN_REGISTRY_URL
    - SC_REGISTRY_PASSWORD
    - SC_REGISTRY_PORT
    - SC_REGISTRY_SECRET_NAME
    - SC_REGISTRY_URL
    - SC_REGISTRY_USER

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;

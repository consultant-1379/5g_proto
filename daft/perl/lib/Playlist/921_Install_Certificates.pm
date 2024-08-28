package Playlist::921_Install_Certificates;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.26
#  Date     : 2023-08-09 16:17:15
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

use ADP::Kubernetes_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

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
    } else {
        $debug_command = "";
    }

    $rc = General::Playlist_Operations::execute_step( \&Check_Information_P921S01, \&Fallback001_P921S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Generate_Certificates_P921S02, \&Fallback001_P921S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Install_Certificates_P921S03, \&Fallback001_P921S99 );
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
sub Check_Information_P921S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_CMYP_Node_Specific_Information_P921S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Old_SSH_Keys_P921S01T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_SSH_Is_Available_To_CMYP_P921S01T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Helm_Chart_Information_P921S01T04 } );
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
    sub Get_CMYP_Node_Specific_Information_P921S01T01 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;

        # Initialize node specific variables
        $::JOB_PARAMS{'CMYP_IP'} = "";
        $::JOB_PARAMS{'CMYP_NETCONF_PORT'} = "";
        $::JOB_PARAMS{'CMYP_CLI_PORT'} = "";

        #
        # CM Yang Provider
        #

        General::Logging::log_user_message("Get CMYP Information");
        (undef, $::JOB_PARAMS{'CMYP_IP'}, $::JOB_PARAMS{'CMYP_CLI_PORT'}, $::JOB_PARAMS{'CMYP_NETCONF_PORT'}, undef) = ADP::Kubernetes_Operations::get_cmyp_info($sc_namespace);

        General::Logging::log_user_message("Collected Information:\nCMYP_IP:           $::JOB_PARAMS{'CMYP_IP'}\nCMYP_NETCONF_PORT: $::JOB_PARAMS{'CMYP_NETCONF_PORT'}\nCMYP_CLI_PORT:     $::JOB_PARAMS{'CMYP_CLI_PORT'}\n");

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
    sub Remove_Old_SSH_Keys_P921S01T02 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_cli_port = $::JOB_PARAMS{'CMYP_CLI_PORT'};
        my $cmyp_netconf_port = $::JOB_PARAMS{'CMYP_NETCONF_PORT'};
        my $errorcnt = 0;
        my $rc = 0;
        my $temp;

        # Create directory '$HOME/.ssh' if it doesn't exist
        $temp = "$ENV{'HOME'}/.ssh";
        unless (-d $temp) {
            General::Logging::log_user_message("Creating directory '$temp'\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "mkdir -p $temp; chmod 700 $temp",
                    "hide-output"   => 1,
                }
            );
            return $rc if $rc != 0;
        }

        # Create file '$HOME/.ssh/known_hosts' if it doesn't exist
        $temp = "$ENV{'HOME'}/.ssh/known_hosts";
        unless (-f $temp) {
            General::Logging::log_user_message("Creating file '$temp'\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "touch $temp; chmod 600 $temp",
                    "hide-output"   => 1,
                }
            );
            return $rc if $rc != 0;
        }

        General::Logging::log_user_message("Removing old SSH keys in SSH known_hosts file\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh-keygen -R $cmyp_ip -f $ENV{'HOME'}/.ssh/known_hosts",
                "hide-output"   => 1,
            }
        );
        $errorcnt++ if $rc != 0;

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh-keygen -R '[$cmyp_ip]:$cmyp_cli_port' -f $ENV{'HOME'}/.ssh/known_hosts",
                "hide-output"   => 1,
            }
        );
        $errorcnt++ if $rc != 0;

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh-keygen -R '[$cmyp_ip]:$cmyp_netconf_port' -f $ENV{'HOME'}/.ssh/known_hosts",
                "hide-output"   => 1,
            }
        );
        $errorcnt++ if $rc != 0;

        if ($errorcnt == 0) {
            return 0;
        } else {
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
    sub Check_If_SSH_Is_Available_To_CMYP_P921S01T03 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = $::JOB_PARAMS{'CMYP_CLI_PORT'};
        my $rc = 0;
        my $repeat_at_error = 0;
        my @result = ();
        my $timeout = 2;

        if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {
            $cmyp_port = $::JOB_PARAMS{'CMYP_NETCONF_PORT'};
        }

        General::Logging::log_user_message("Checking if SSH is available to CMYP on port $cmyp_port at host $cmyp_ip");
        if (General::OS_Operations::is_ssh_available($cmyp_ip, $cmyp_port, $timeout, $repeat_at_error) == 1) {
            # Yes it's available, continue with next task or step
            return 0;
        } else {
            # No it's not available
            General::Logging::log_user_error_message("Not able to connect to CMYP on port $cmyp_port at host $cmyp_ip");
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
    sub Check_Helm_Chart_Information_P921S01T04 {

        my %cnf_status;
        my @dot_separated_array = ();
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $message = "";
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $rc = 0;
        my @result;
        my %unique_certificate_names;
        my %unique_key_names;
        my $unique_truststore_count = 0;
        my %unique_truststore_names;
        my %yaml_data;

        General::Logging::log_user_message("Get Helm Chart Information");
        $rc = ADP::Kubernetes_Operations::get_helm_release_extended_information(
            {
                "command"         => "values --all",
                "hide-output"     => 1,
                "namespace"       => $sc_namespace,
                "output-format"   => "dot-separated",
                "release-name"    => $sc_release_name,
                "return-output"   => \@result,
                "return-patterns" => [
                    '^global\.ericsson\.\S+\.enabled=true',
                    'certificates.+(certificate|key|certificateList|trustedCertificateListName|asymmetricKeyCertificateName)=',
                ],
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get Helm Chart Information");
            return $rc;
        }

        # Initialize node specific variables
        $::JOB_PARAMS{'ENABLED_CNF'} = "";
        $::JOB_PARAMS{'SC_NBI_KEY'} = "";
        $::JOB_PARAMS{'SC_NBI_CERTIFICATE'} = "";
        $::JOB_PARAMS{'SCP_TRAF_KEY'} = "";
        $::JOB_PARAMS{'SCP_TRAF_CERTIFICATE'} = "";
        $::JOB_PARAMS{'SCP_NRF_KEY'} = "";
        $::JOB_PARAMS{'SCP_NRF_CERTIFICATE'} = "";
        $::JOB_PARAMS{'SC_SLF_KEY'} = "";
        $::JOB_PARAMS{'SC_SLF_CERTIFICATE'} = "";
        $::JOB_PARAMS{'BSF_TRAF_KEY'} = "";
        $::JOB_PARAMS{'BSF_TRAF_CERTIFICATE'} = "";
        $::JOB_PARAMS{'BSF_NRF_KEY'} = "";
        $::JOB_PARAMS{'BSF_NRF_CERTIFICATE'} = "";
        $::JOB_PARAMS{'STM_DIAMETER_KEY'} = "";
        $::JOB_PARAMS{'STM_DIAMETER_CERTIFICATE'} = "";
        $::JOB_PARAMS{'STM_DIAMETER_TRUSTED_CERTIFICATE_LIST'} = "";
        $::JOB_PARAMS{'SC_TRUSTEDCAS_CERTIFICATE_LIST'} = "";
        $::JOB_PARAMS{'SC_TRUSTEDCAS_SECRET'} = "";
        $::JOB_PARAMS{'CSA_TRAF_KEY'} = "";
        $::JOB_PARAMS{'CSA_TRAF_CERTIFICATE'} = "";
        $::JOB_PARAMS{'CSA_NRF_KEY'} = "";
        $::JOB_PARAMS{'CSA_NRF_CERTIFICATE'} = "";
        $::JOB_PARAMS{'WCDB_CLIENT_KEY'} = "";
        $::JOB_PARAMS{'WCDB_CLIENT_CERTIFICATE'} = "";
        $::JOB_PARAMS{'WCDB_INTERNODE_KEY'} = "";
        $::JOB_PARAMS{'WCDB_INTERNODE_CERTIFICATE'} = "";
        $::JOB_PARAMS{'WCDB_SERVER_KEY'} = "";
        $::JOB_PARAMS{'WCDB_SERVER_CERTIFICATE'} = "";
        $::JOB_PARAMS{'WCDB_CLIENT_CERTIFICATE_LIST'} = "";
        $::JOB_PARAMS{'WCDB_INTERNODE_CERTIFICATE_LIST'} = "";
        $::JOB_PARAMS{'WCDB_SERVER_CERTIFICATE_LIST'} = "";
        $::JOB_PARAMS{'LOG_TRANSFORMER_KEY'} = "";
        $::JOB_PARAMS{'LOG_TRANSFORMER_CERTIFICATE'} = "";
        $::JOB_PARAMS{'LOG_TRANSFORMER_CERTIFICATE_LIST'} = "";
        $::JOB_PARAMS{'SEPP_NRF_KEY'} = "";
        $::JOB_PARAMS{'SEPP_NRF_CERTIFICATE'} = "";

        # Parse the data
        for (@result) {
            #
            # Active CNF
            #
            if (/^global\.ericsson\.(\S+)\.enabled=true/i) {
                $::JOB_PARAMS{'ENABLED_CNF'} .= lc("$1,");
            #
            # SC NBI / CNOM Certificates
            #
            } elsif (/^eric-sc\.certificates\.nbi\.key=(\S+)$/i) {
                $::JOB_PARAMS{'SC_NBI_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-sc\.certificates\.nbi\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'SC_NBI_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            #
            # SCP Certificates
            #
            } elsif (/^eric-scp\.certificates\.traf\.key=(\S+)$/i) {
                $::JOB_PARAMS{'SCP_TRAF_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-scp\.certificates\.traf\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'SCP_TRAF_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            } elsif (/^eric-scp\.certificates\.nrf\.key=(\S+)$/i) {
                $::JOB_PARAMS{'SCP_NRF_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-scp\.certificates\.nrf\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'SCP_NRF_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            #
            # SLF Certificates
            #
            } elsif (/^eric-sc-slf\.certificates\.key=(\S+)$/i) {
                $::JOB_PARAMS{'SC_SLF_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-sc-slf\.certificates\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'SC_SLF_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            #
            # BSF Certificates
            #
            } elsif (/^eric-bsf\.certificates\.traf\.key=(\S+)$/i) {
                $::JOB_PARAMS{'BSF_TRAF_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-bsf\.certificates\.traf\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'BSF_TRAF_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            } elsif (/^eric-bsf\.certificates\.nrf\.key=(\S+)$/i) {
                $::JOB_PARAMS{'BSF_NRF_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-bsf\.certificates\.nrf\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'BSF_NRF_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            #
            # DIAMETER Certificates
            #
            } elsif (/^eric-stm-diameter\.service\.certificates\.asymmetricKeyCertificateName=(\S+)$/i) {
                $::JOB_PARAMS{'STM_DIAMETER_KEY'} = $1;
                $unique_key_names{$1}++;
                $::JOB_PARAMS{'STM_DIAMETER_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            } elsif (/^eric-stm-diameter\.service\.certificates\.trustedCertificateListName=(\S+)$/i) {
                $::JOB_PARAMS{'STM_DIAMETER_TRUSTED_CERTIFICATE_LIST'} = $1;
                $unique_truststore_names{$1}++;
            #
            # Trusted CA Certificates
            #
            } elsif (/^eric-sc\.certificates\.trustedCAs\.certificateList=(\S+)$/i) {
                $::JOB_PARAMS{'SC_TRUSTEDCAS_CERTIFICATE_LIST'} = $1;
                $unique_truststore_names{$1}++;
           #} elsif (/^eric-sc\.certificates\.trustedCAs\.secret=(\S+)$/i) {
           #    $::JOB_PARAMS{'SC_TRUSTEDCAS_SECRET'} = $1;
           #    $truststore_secret_map_name{$1} = 'SC_TRUSTEDCAS_SECRET';
            #
            # CSA Certificates
            #
            } elsif (/^eric-csa\.certificates\.traf\.key=(\S+)$/i) {
                $::JOB_PARAMS{'CSA_TRAF_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-csa\.certificates\.traf\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'CSA_TRAF_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            } elsif (/^eric-csa\.certificates\.nrf\.key=(\S+)$/i) {
                $::JOB_PARAMS{'CSA_NRF_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-csa\.certificates\.nrf\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'CSA_NRF_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
            #
            # WCDB Certificates
            #
            } elsif (/^eric-data-wide-column-database-cd\.egress\.certificates\.asymmetricKeyCertificateName=(\S+)\/(\S+)$/i) {
                $::JOB_PARAMS{'WCDB_CLIENT_KEY'} = $1;
                $unique_key_names{$1}++;
                $::JOB_PARAMS{'WCDB_CLIENT_CERTIFICATE'} = $2;
                $unique_certificate_names{$2}++;
            } elsif (/^eric-data-wide-column-database-cd\.georeplication\.certificates\.asymmetricKeyCertificateName=(\S+)\/(\S+)$/i) {
                $::JOB_PARAMS{'WCDB_INTERNODE_KEY'} = $1;
                $unique_key_names{$1}++;
                $::JOB_PARAMS{'WCDB_INTERNODE_CERTIFICATE'} = $2;
                $unique_certificate_names{$2}++;
            } elsif (/^eric-data-wide-column-database-cd\.service\.certificates\.asymmetricKeyCertificateName=(\S+)\/(\S+)$/i) {
                $::JOB_PARAMS{'WCDB_SERVER_KEY'} = $1;
                $unique_key_names{$1}++;
                $::JOB_PARAMS{'WCDB_SERVER_CERTIFICATE'} = $2;
                $unique_certificate_names{$2}++;
            } elsif (/^eric-data-wide-column-database-cd\.egress\.certificates\.trustedCertificateListName=(\S+)$/i) {
                $::JOB_PARAMS{'WCDB_CLIENT_CERTIFICATE_LIST'} = $1;
                $unique_truststore_names{$1}++;
            } elsif (/^eric-data-wide-column-database-cd\.geoReplication\.certificates\.trustedCertificateListName=(\S+)$/i) {
                $::JOB_PARAMS{'WCDB_INTERNODE_CERTIFICATE_LIST'} = $1;
                $unique_truststore_names{$1}++;
            } elsif (/^eric-data-wide-column-database-cd\.service\.certificates\.trustedCertificateListName=(\S+)$/i) {
                $::JOB_PARAMS{'WCDB_SERVER_CERTIFICATE_LIST'} = $1;
                $unique_truststore_names{$1}++;
            #
            # Log Transformer Certificates
            #
            } elsif (/^eric-log-transformer\.syslog\.output\.tls\.certificates\.asymmetricKeyCertificateName=(\S+)\/(\S+)$/i) {
                $::JOB_PARAMS{'LOG_TRANSFORMER_KEY'} = $1;
                $unique_key_names{$1}++;
                $::JOB_PARAMS{'LOG_TRANSFORMER_CERTIFICATE'} = $2;
                $unique_certificate_names{$2}++;
            } elsif (/^eric-log-transformer\.syslog\.output\.tls\.certificates\.trustedCertificateListName=(\S+)$/i) {
                $::JOB_PARAMS{'LOG_TRANSFORMER_CERTIFICATE_LIST'} = $1;
                $unique_truststore_names{$1}++;
            #
            # SEPP Certificates
            #
           #} elsif (/^eric-sepp\.certificates\.nrf\.caSecret=(\S+)$/i) {
           #    $::JOB_PARAMS{'SEPP_NRF_CA_SECRET'} = $1;
           #    $unique_key_names{$1}++;
            } elsif (/^eric-sepp\.certificates\.nrf\.key=(\S+)$/i) {
                $::JOB_PARAMS{'SEPP_NRF_KEY'} = $1;
                $unique_key_names{$1}++;
            } elsif (/^eric-sepp\.certificates\.nrf\.certificate=(\S+)$/i) {
                $::JOB_PARAMS{'SEPP_NRF_CERTIFICATE'} = $1;
                $unique_certificate_names{$1}++;
           #} elsif (/^eric-sepp\.certificates\.traf\.asymmetric\.(\d+)\.key=(\S+)$/i) {
           #    $::JOB_PARAMS{'SEPP_TRAF_KEY$1'} = $2;
           #    $unique_key_names{$1}++;
           #} elsif (/^eric-sepp\.certificates\.traf\.asymmetric\.(\d+)\.certificate=(\S+)$/i) {
           #    $::JOB_PARAMS{'SEPP_TRAF_CERTIFICATE$1'} = $2;
           #    $unique_certificate_names{$1}++;
           #} elsif (/^eric-sepp\.certificates\.traf\.trustedAuthority\.(\d+)\.caList=(\S+)$/i) {
           #    $::JOB_PARAMS{'SEPP_TRAF_CERTIFICATE_LIST$1'} = $2;
           #    $unique_truststore_names{$2}++;
            }
        }
        $::JOB_PARAMS{'ENABLED_CNF'} =~ s/,$//;

        $message = "Enabled CNF: $::JOB_PARAMS{'ENABLED_CNF'}\n";

        $message .= "Unique Trusted Certificate List Instance in Truststore Names:\n";
        for my $key (sort keys %unique_truststore_names) {
            $message .= "    $key\n";
            $unique_truststore_count++;
        }
        if ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" && $unique_truststore_count > 1) {
            General::Logging::log_user_warning_message("There are $unique_truststore_count different Trusted Certificate Lists used but only one rootCA is generated.\nThis might result in faulty behavior for some certificates and to correct this a change is needed in a Playlist task.\nPlease read the TODO: statement in the 'Generate_Certificate_Authority_Certificates_P921S02T01' Playlist task.\n");
        }

        $message .= "Unique Certificate Key Names:\n";
        for my $key (sort keys %unique_key_names) {
            $message .= "    $key\n";
        }

        $message .= "Unique Certificate Names:\n";
        for my $key (sort keys %unique_certificate_names) {
            $message .= "    $key\n";
        }

        General::Logging::log_user_message($message);

        # Return success
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
sub Generate_Certificates_P921S02 {

    my $rc;

    if ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Generate_Certificate_Authority_Certificates_P921S02T01 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Generate_Other_Certificates_P921S02T02 } );
        return $rc if $rc < 0;

    } else {

        $rc = General::Playlist_Operations::RC_STEPOUT;

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
    sub Generate_Certificate_Authority_Certificates_P921S02T01 {

        my $create_cli_file = $::JOB_PARAMS{'USE_NETCONF'} eq "no" ? "yes" : "no";
        my $create_netconf_file = $::JOB_PARAMS{'USE_NETCONF'} eq "yes" ? "yes" : "no";

        # TODO: The following also needs to be handled:
        #   'STM_DIAMETER_TRUSTED_CERTIFICATE_LIST'
        #   'WCDB_CLIENT_CERTIFICATE_LIST'
        #   'WCDB_INTERNODE_CERTIFICATE_LIST'
        #   'WCDB_SERVER_CERTIFICATE_LIST'
        #   'LOG_TRANSFORMER_CERTIFICATE_LIST
        # This would of course complicate the logic because we would need to keep track of which rootCA file
        # belongs with which certificate file to be used in the next subroutine Generate_Other_Certificates_P921S02T02.
        # For now during the testing we seem to use the same 'sc-trusted-default-cas' name for all trusted
        # certificate lists so it's enough to just create one rootCA to keep the logic simple.
        if ($::JOB_PARAMS{'SC_TRUSTEDCAS_CERTIFICATE_LIST'} ne "") {
            return generate_ca_certificate_files(
                {
                    "file-prefix"       => "rootCA",
                    "subject"           => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=testca",
                    "key-name"          => $::JOB_PARAMS{'CERTIFICATE_NAME_CA'},
                    "list-name"         => $::JOB_PARAMS{'SC_TRUSTEDCAS_CERTIFICATE_LIST'},
                    "create-netconf"    => $create_netconf_file,
                    "create-cli"        => $create_cli_file,
                }
            );
        }

        General::Logging::log_user_message("No need to generate certificate");
        return General::Playlist_Operations::RC_TASKOUT;
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
    sub Generate_Other_Certificates_P921S02T02 {

        my $config_dir = $::JOB_PARAMS{'_JOB_CONFIG_DIR'};
        my $ca_file = "$config_dir/rootCA.crt";
        my $cakey_file = "$config_dir/rootCA.key";
        my $certname;
        my $certvalue;
        my $create_cli_file = $::JOB_PARAMS{'USE_NETCONF'} eq "no" ? "yes" : "no";
        my $create_netconf_file = $::JOB_PARAMS{'USE_NETCONF'} eq "yes" ? "yes" : "no";
        my $file_name;
        my $file_number = 1;
        my $file_number_str;
        my $key_length = $::JOB_PARAMS{'CERTIFICATE_KEY_LENGTH'};
        my %key_name_used;
        my $keyname;
        my $keyvalue;
        my $rc;
        my $subject;
        my %subject_list = (
            "BSF_NRF"           => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=bsf.ericsson.se",
            "BSF_TRAF"          => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=bsf.ericsson.se",
            "CSA_NRF"           => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=csa.ericsson.se",
            "CSA_TRAF"          => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=csa.ericsson.se",
            "K6"                => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=smf.ericsson.se",
            "LOG_TRANSFORMER"   => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=eric-log-transformer",
            "SC_SLF"            => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=eric-sc-slf",
            "SC_NBI"            => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=cnom.ericsson.se",
            "SCP_NRF"           => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=csa.ericsson.se",
            "SCP_TRAF"          => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=csa.ericsson.se",
            "SEPP_NRF"          => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=sepp.ericsson.se",  # ????
            "STM_DIAMETER"      => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=bsf.ericsson.se",
            "WCDB_CLIENT"       => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=eric-data-wide-column-database-cd",
            "WCDB_INTERNODE"    => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=eric-data-wide-column-database-cd",
            "WCDB_SERVER"       => "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=eric-data-wide-column-database-cd",
        );

        #
        # Miscellaneous Certificates
        #
        for my $name ('BSF_NRF', 'BSF_TRAF', 'CSA_NRF', 'CSA_TRAF', 'LOG_TRANSFORMER', 'SCP_NRF', 'SCP_TRAF', 'SC_SLF', 'SEPP_NRF', 'STM_DIAMETER', 'WCDB_CLIENT', 'WCDB_INTERNODE', 'WCDB_SERVER') {
            $keyname   = "${name}_KEY";
            $certname  = "${name}_CERTIFICATE";
            $keyvalue  = exists $::JOB_PARAMS{$keyname} ? $::JOB_PARAMS{$keyname} : "";
            $certvalue = exists $::JOB_PARAMS{$certname} ? $::JOB_PARAMS{$certname} : "";
            $subject   = exists $subject_list{$name} ? $subject_list{$name} : "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=csa.ericsson.se";

            next if ($keyvalue eq "");
            next if ($certvalue eq "");
            next if (exists $key_name_used{$keyvalue});
            if ($::JOB_PARAMS{'GEOGRAPHICAL_REDUNDANCY'} eq "no" && $name =~ /^(WCDB_CLIENT|WCDB_INTERNODE|WCDB_SERVER)$/) {
                # If this is not a geo-redundant node setup then skip generating these certificates
                General::Logging::log_user_warning_message("Generate of $name Certificate is skipped because GEOGRAPHICAL_REDUNDANCY=no");
                next;
            }

            $file_name = sprintf "certificate_%02d_%s", $file_number, $name;
            $rc = generate_certificate_files(
                {
                    "file-prefix"       => $file_name,
                    "subject"           => $subject,
                    "key-length"        => $key_length,
                    "key-name"          => $::JOB_PARAMS{$keyname},
                    "cert-name"         => $::JOB_PARAMS{$certname},
                    "create-netconf"    => $create_netconf_file,
                    "create-cli"        => $create_cli_file,
                    "ca-file"           => $ca_file,
                    "cakey-file"        => $cakey_file,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to generate $name certificate");
                return $rc;
            }
            $file_number_str = sprintf "%02d", $file_number;
            $key_name_used{$keyvalue} = 1;
            $::JOB_PARAMS{"CERTIFICATE_DATA_FILE_TO_LOAD_$file_number_str"} = $file_name;
            $::JOB_PARAMS{"CERTIFICATE_DATA_FILE_TO_LOAD_${file_number_str}_LOADED"} = "no";
            $file_number++;
        }

        #
        # NBI/CNOM Certificate
        #
        if ($::JOB_PARAMS{'SC_NBI_KEY'} ne "" && $::JOB_PARAMS{'SC_NBI_CERTIFICATE'} ne "" && ! exists $key_name_used{$::JOB_PARAMS{'SC_NBI_KEY'}}) {
            $file_name = sprintf "certificate_%02d_%s", $file_number, "SC_NBI";
            $rc = generate_certificate_files(
                {
                    "file-prefix"       => $file_name,
                    "subject"           => $subject_list{'SC_NBI'},
                    "key-length"        => $key_length,
                    "key-name"          => $::JOB_PARAMS{'SC_NBI_KEY'},
                    "cert-name"         => $::JOB_PARAMS{'SC_NBI_CERTIFICATE'},
                    "create-netconf"    => $create_netconf_file,
                    "create-cli"        => $create_cli_file,
                    "ca-file"           => $ca_file,
                    "cakey-file"        => $cakey_file,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to generate SC_NBI certificate");
                return $rc;
            }
            $file_number_str = sprintf "%02d", $file_number;
            $key_name_used{$::JOB_PARAMS{'SC_NBI_KEY'}} = 1;
            $::JOB_PARAMS{"CERTIFICATE_DATA_FILE_TO_LOAD_$file_number_str"} = $file_name;
            $::JOB_PARAMS{"CERTIFICATE_DATA_FILE_TO_LOAD_${file_number_str}_LOADED"} = "no";
            $file_number++;
        }

        #
        # K6 Certificate
        #
        $rc = generate_certificate_files(
            {
                "file-prefix"       => "K6",
                "subject"           => $subject_list{'K6'},
                "key-length"        => 3072,
                "create-netconf"    => "no",
                "create-cli"        => "no",
                "ca-file"           => $ca_file,
                "cakey-file"        => $cakey_file,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to generate K6 certificate");
            return $rc;
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
sub Install_Certificates_P921S03 {

    my $rc;

    if ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_Certificate_Authority_Certificate_P921S03T01 } );
        return $rc if $rc < 0;

    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_Other_Certificates_P921S03T02 } );
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
    sub Install_Certificate_Authority_Certificate_P921S03T01 {

        my $config_dir = $::JOB_PARAMS{'_JOB_CONFIG_DIR'};
        my $file_path = $::JOB_PARAMS{'USE_NETCONF'} eq "no" ? "$config_dir/rootCA.cli" : "$config_dir/rootCA.netconf";

        return install_certificate_file($file_path);
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
    sub Install_Other_Certificates_P921S03T02 {

        my $file_ext = $::JOB_PARAMS{'USE_NETCONF'} eq "no" ? "cli" : "netconf";
        my @files = General::File_Operations::find_file( { "directory" => $::JOB_PARAMS{'_JOB_CONFIG_DIR'}, "filename" => "certificate_*.$file_ext" } );
        my $job_parameter;
        my $rc;

        for my $filepath (@files) {
            if ($filepath =~ /^.+\/certificate_(\d+).*\.$file_ext$/) {
                $job_parameter = "CERTIFICATE_DATA_FILE_TO_LOAD_$1";
                if ($::JOB_PARAMS{"${job_parameter}_LOADED"} eq "yes") {
                    General::Logging::log_user_message("File $::JOB_PARAMS{$job_parameter} has already been loaded, skipping this file");
                    next;
                }
            } else {
                $job_parameter = "";
            }
            $rc = install_certificate_file($filepath);
            return $rc if ($rc != 0);
            $::JOB_PARAMS{"${job_parameter}_LOADED"} = "yes" if ($job_parameter ne "");
        }

        return 0;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P921S99 {

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    return 0;
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
sub generate_ca_certificate_files {
    my %params = %{$_[0]};

    # Initialize local variables
    my $create_cli     = exists $params{"create-cli"} ? $params{"create-cli"} : "no";
    my $create_netconf = exists $params{"create-netconf"} ? $params{"create-netconf"} : "no";
    my $file_prefix    = exists $params{"file-prefix"} ? $params{"file-prefix"} : "";
    my $key_name       = exists $params{"key-name"} ? $params{"key-name"} : "CA_cert_1";
    my $list_name      = exists $params{"list-name"} ? $params{"list-name"} : "sc-trusted-default-cas";
    my $subject        = exists $params{"subject"} ? $params{"subject"} : "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=testca";

    my $config_dir = $::JOB_PARAMS{'_JOB_CONFIG_DIR'};
    my $filename;
    my $p12_line;
    my $rc = 0;
    my @result;
    my $validity_days = exists $::JOB_PARAMS{'CA_VALIDITY_DAYS'} ? $::JOB_PARAMS{'CA_VALIDITY_DAYS'} : 730;
    my $valid_from = "";
    my $valid_to = "";

    General::Logging::log_user_message("Generate $file_prefix Certificate");

    #
    # Generate key file
    #
    $filename = "$file_prefix.key";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "openssl genrsa -out $config_dir/$filename 3072",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();
        General::Logging::log_user_error_message("Failed to generate $filename file");
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
        return $rc;
    }

    #
    # Generate crt file
    #
    $filename = "$file_prefix.crt";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "openssl req -new -x509 -days $validity_days -key $config_dir/$file_prefix.key -out $config_dir/$filename -subj \"$subject\"",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();
        General::Logging::log_user_error_message("Failed to generate $filename file");
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
        return $rc;
    }

    #
    # Generate p12 file
    #
    $filename = "$file_prefix.p12";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "base64 -w 0 $config_dir/$file_prefix.crt > $config_dir/$filename",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();
        General::Logging::log_user_error_message("Failed to generate $filename file");
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
        return $rc;
    }

    if ($create_cli eq "yes" || $create_netconf eq "yes") {

        #
        # Read p12 file
        #
        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$config_dir/$filename",
                "output-ref"          => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::Logging::log_user_error_message("Failed to read $filename file");
            push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
            return $rc;
        }
        if (scalar @result != 1) {
            # Display the result in case of error
            General::Logging::log_user_error_message("File $filename does not contain expected format");
            push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
            return $rc;
        }
        $p12_line = $result[0];

        if ($create_cli eq "yes") {

            #
            # Write cli file
            #
            @result = (
                "config",
                "truststore certificates $list_name",
                "commit",
                "install-certificate-pem name $key_name pem $p12_line",
            );
            $filename = "$file_prefix.cli";
            $rc = General::File_Operations::write_file(
                {
                    "filename"            => "$config_dir/$filename",
                    "output-ref"          => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::Logging::log_user_error_message("Failed to write $filename file");
                push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
                return $rc;
            }

        }

        if ($create_netconf eq "yes") {

            #
            # Create netconf data
            #
            @result = (
                '<?xml version="1.0" encoding="UTF-8"?>',
                '<hello xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '    <capabilities>',
                '        <capability>urn:ietf:params:netconf:base:1.0</capability>',
                '        <capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>',
                '        <capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</capability>',
                '    </capabilities>',
                '</hello>',
                ']]>]]>',
                '<rpc message-id="1"',
                '    xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '    <edit-config>',
                '        <target>',
                '            <running/>',
                '        </target>',
                '        <config xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '            <truststore xmlns="urn:ietf:params:xml:ns:yang:ietf-truststore"',
                '                xmlns:ts="urn:ietf:params:xml:ns:yang:ietf-truststore">',
                '                <certificates>',
                "                    <name>$list_name</name>",
                '                    <description>A list of CA certificates</description>',
                '                </certificates>',
                '            </truststore>',
                '        </config>',
                '    </edit-config>',
                '</rpc>',
                ']]>]]>',
                '<rpc message-id="1" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '<action xmlns="urn:ietf:params:xml:ns:yang:1"',
                '    xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '    <truststore xmlns="urn:ietf:params:xml:ns:yang:ietf-truststore">',
                '    <certificates>',
                "             <name>$list_name</name>",
                '            <install-certificate-pem xmlns="urn:rdns:com:ericsson:oammodel:ericsson-truststore-ext">',
                "                <name>$key_name</name>",
                "                <pem>$p12_line</pem>",
                '            </install-certificate-pem>',
                '    </certificates>',
                '    </truststore>',
                '</action>',
                '</rpc>',
                ']]>]]>',
                '<rpc message-id="1" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '    <close-session/>',
                '</rpc>',
                ']]>]]>',
            );

            #
            # Write netconf file
            #
            $filename = "$file_prefix.netconf";
            $rc = General::File_Operations::write_file(
                {
                    "filename"            => "$config_dir/$filename",
                    "output-ref"          => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::Logging::log_user_error_message("Failed to write $filename file");
                push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
                return $rc;
            }

        }

    }

    #
    # Read validity time for certificate
    #
    $filename = "$file_prefix.crt";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl --file $config_dir/$filename --print-simple-table",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();
        General::Logging::log_user_error_message("Failed to read $filename validity information");
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
        return $rc;
    }
    for (@result) {
        if (/^First Not Before\s+(.+)\s+$config_dir\/$filename/) {
            $valid_from = $1;
        } elsif (/^Last Not After\s+(.+)\s+$config_dir\/$filename/) {
            $valid_to = $1;
        }
    }
    if ($valid_from ne "" || $valid_to ne "") {
        my $message = "  Certificate Validity Information:\n";
        $message .= "    From Date: $valid_from\n" if ($valid_from ne "");
        $message .= "      To Date: $valid_to\n" if ($valid_to ne "");
        General::Logging::log_user_message($message);
    }

    if ($rc == 0) {
        push @::JOB_STATUS, "(/) Generate $file_prefix Certificate was successful";
    } else {
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub generate_certificate_files {
    my %params = %{$_[0]};

    # Initialize local variables
    my $config_dir     = $::JOB_PARAMS{'_JOB_CONFIG_DIR'};
    my $ca_file        = exists $params{"ca-file"} ? $params{"ca-file"} : "$config_dir/rootCA.crt";
    my $cakey_file     = exists $params{"cakey-file"} ? $params{"cakey-file"} : "$config_dir/rootCA.key";
    my $cert_name      = exists $params{"cert-name"} ? $params{"cert-name"} : "";
    my $create_cli     = exists $params{"create-cli"} ? $params{"create-cli"} : "no";
    my $create_netconf = exists $params{"create-netconf"} ? $params{"create-netconf"} : "no";
    my $file_prefix    = exists $params{"file-prefix"} ? $params{"file-prefix"} : "";
    my $key_length     = exists $params{"key-length"} ? $params{"key-length"} : 3072;
    my $key_name       = exists $params{"key-name"} ? $params{"key-name"} : "";
    my $subject        = exists $params{"subject"} ? $params{"subject"} : "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$file_prefix.ericsson.se";

    my $filename;
    my $p12_line;
    my $password = exists $::JOB_PARAMS{'CERTIFICATE_PASSWORD'} ? $::JOB_PARAMS{'CERTIFICATE_PASSWORD'} : "rootroot";
    my $rc = 0;
    my @result;
    my $validity_days = exists $::JOB_PARAMS{'CERTIFICATE_VALIDITY_DAYS'} ? $::JOB_PARAMS{'CERTIFICATE_VALIDITY_DAYS'} : 365;
    my $valid_from = "";
    my $valid_to = "";

    General::Logging::log_user_message("Generate $file_prefix Certificate");

    #
    # Generate key and crt files
    #
    $filename = "$file_prefix.key and $file_prefix.crt";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "openssl req -nodes -newkey rsa:$key_length -keyout $config_dir/$file_prefix.key -out $config_dir/$file_prefix.crt -subj \"$subject\"",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();
        General::Logging::log_user_error_message("Failed to generate $filename files");
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
        return $rc;
    }

    #
    # Sign crt file with rootCA
    #
    $filename = "$file_prefix.crt";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "openssl x509 -req -in $config_dir/$filename -CA $ca_file -CAkey $cakey_file -CAcreateserial -days $validity_days -out $config_dir/$filename",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();
        General::Logging::log_user_error_message("Failed to sign $filename file");
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
        return $rc;
    }

    #
    # Generate p12 file
    #
    $filename = "$file_prefix.p12";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "cat $config_dir/$file_prefix.key $config_dir/$file_prefix.crt | openssl pkcs12 -export -passout pass:\"$password\" -passin pass:\"$password\" | base64 -w 0 > $config_dir/$filename",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();
        General::Logging::log_user_error_message("Failed to generate $filename file");
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
        return $rc;
    }

    if ($create_cli eq "yes" || $create_netconf eq "yes") {

        #
        # Read p12 file
        #
        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$config_dir/$filename",
                "output-ref"          => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::Logging::log_user_error_message("Failed to read $filename file");
            push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
            return $rc;
        }
        if (scalar @result != 1) {
            # Display the result in case of error
            General::Logging::log_user_error_message("File $filename does not contain expected format");
            push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
            return $rc;
        }
        $p12_line = $result[0];

        #
        # CLI file
        #
        if ($create_cli eq "yes") {

            #
            # Write cli file
            #
            @result = (
                "keystore asymmetric-keys install-asymmetric-key-pkcs12 name $key_name certificate-name $cert_name p12 $p12_line p12-password $password",
            );
            $filename = "$file_prefix.cli";
            $rc = General::File_Operations::write_file(
                {
                    "filename"            => "$config_dir/$filename",
                    "output-ref"          => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::Logging::log_user_error_message("Failed to write $filename file");
                push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
                return $rc;
            }

        }

        #
        # NETCONF file
        #
        if ($create_netconf eq "yes") {

            #
            # Create netconf data
            #
            @result = (
                '<?xml version="1.0" encoding="UTF-8"?>',
                '<hello xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '    <capabilities>',
                '        <capability>urn:ietf:params:netconf:base:1.0</capability>',
                '        <capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>',
                '        <capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</capability>',
                '    </capabilities>',
                '</hello>',
                ']]>]]>',
                '<rpc message-id="1" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '<action xmlns="urn:ietf:params:xml:ns:yang:1"',
                '    xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '    <keystore xmlns="urn:ietf:params:xml:ns:yang:ietf-keystore">',
                '        <asymmetric-keys>',
                '            <install-asymmetric-key-pkcs12 xmlns="urn:rdns:com:ericsson:oammodel:ericsson-keystore-ext">',
                "                <name>$key_name</name>",
                "                <certificate-name>$cert_name</certificate-name>",
                "                <p12>$p12_line</p12>",
                "                <p12-password>$password</p12-password>",
                '            </install-asymmetric-key-pkcs12>',
                '        </asymmetric-keys>',
                '    </keystore>',
                '</action>',
                '</rpc>',
                ']]>]]>',
                '<rpc message-id="1" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">',
                '    <close-session/>',
                '</rpc>',
                ']]>]]>',
            );

            #
            # Write netconf file
            #
            $filename = "$file_prefix.netconf";
            $rc = General::File_Operations::write_file(
                {
                    "filename"            => "$config_dir/$filename",
                    "output-ref"          => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::Logging::log_user_error_message("Failed to write $filename file");
                push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
                return $rc;
            }

        }
    }

    #
    # Read validity time for certificate
    #
    $filename = "$file_prefix.crt";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl --file $config_dir/$filename --print-simple-table",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();
        General::Logging::log_user_error_message("Failed to read $filename validity information");
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
        return $rc;
    }
    for (@result) {
        if (/^First Not Before\s+(.+)\s+$config_dir\/$filename/) {
            $valid_from = $1;
        } elsif (/^Last Not After\s+(.+)\s+$config_dir\/$filename/) {
            $valid_to = $1;
        }
    }
    if ($valid_from ne "" || $valid_to ne "") {
        my $message = "  Certificate Validity Information:\n";
        $message .= "    From Date: $valid_from\n" if ($valid_from ne "");
        $message .= "      To Date: $valid_to\n" if ($valid_to ne "");
        General::Logging::log_user_message($message);
    }

    if ($rc == 0) {
        push @::JOB_STATUS, "(/) Generate $file_prefix Certificate was successful";
    } else {
        push @::JOB_STATUS, "(x) Generate $file_prefix Certificate was unsuccessful";
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub install_certificate_file {
    my $file_name  = shift;

    my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
    my $cmyp_port;
    my $file_prefix;
    my $rc = 0;
    my $use_netconf;
    my $user_name = $::JOB_PARAMS{'CERTIFICATE_USER_NAME'};
    my $user_password = $::JOB_PARAMS{'CERTIFICATE_USER_PASSWORD'};

    if ($file_name =~ /^.+\/(\S+)\.(cli|netconf)$/) {
        $file_prefix = $1;
    } else {
        $file_prefix = $file_name;
    }
    if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {
        $cmyp_port = $::JOB_PARAMS{'CMYP_NETCONF_PORT'};
        General::Logging::log_user_message("Loading Certificate for $file_prefix via NETCONF");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$cmyp_ip " .
                                        "--user=$user_name " .
                                        "--password='$user_password' " .
                                        "--port=$cmyp_port " .
                                        "--shell=netconf " .
                                        "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                        "--command-file='$file_name' ",
                "hide-output"   => 1,
            }
        );
    } else {
        $cmyp_port = $::JOB_PARAMS{'CMYP_CLI_PORT'};
        General::Logging::log_user_message("Loading Certificate for $file_prefix via CLI");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$cmyp_ip " .
                                        "--user=$user_name " .
                                        "--password='$user_password' " .
                                        "--port=$cmyp_port " .
                                        "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                        "--command-file='$file_name' ",
                "hide-output"   => 1,
            }
        );
    }
    if ($rc == 0) {
        push @::JOB_STATUS, "(/) Installing $file_prefix Certificate was successful";
    } else {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to load config data");
        push @::JOB_STATUS, "(x) Installing $file_prefix Certificate failed";
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This playlist creates and installs the needed test certificates for:

    - sc-trusted-default-cas and CA_cert_1 (Certificate Authority)
    - sc-nrf-default-cert (Manager Certificate)
    - sc-traf-default-cert (Worker Certificate)
    - sc-nbi-default-cert (CNOM Certificate)
    - K6
    - chfsim (not yet implemented)
    - nrfsim (not yet implemented)

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;

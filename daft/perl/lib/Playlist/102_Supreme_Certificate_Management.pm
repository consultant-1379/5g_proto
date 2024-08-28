package Playlist::102_Supreme_Certificate_Management;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.28
#  Date     : 2024-04-12 17:31:03
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

use Cwd qw(abs_path cwd);

use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::920_Check_Network_Config_Information;
use Playlist::936_Check_Deployed_Software;

#
# Variable Declarations
#
my $debug_command = "";

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

    my $rc;

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    } else {
        $debug_command = "";
    }

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "CERTIFICATE_MANAGEMENT";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
        General::Logging::log_user_warning_message("No network configuration file specified, all mandatory parameters must be given as job parameters or the job will fail");
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P102S01, \&Fallback001_P102S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P102S02, \&Fallback001_P102S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" || $::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Generate_Install_Certificates_P102S03, \&Fallback001_P102S99 );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P102S04, \&Fallback001_P102S99 );
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
sub Initialize_Job_Environment_P102S01 {

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
sub Check_Job_Parameters_P102S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P102S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_SOFTWARE_DIR_And_Load_Docker_Image_If_Needed_P102S02T02 } );
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
    sub Check_Job_Parameters_P102S02T01 {

        my $message;
        my $namespace = "";
        my $rc = 0;

        # Get the proper NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'NAMESPACE' and Network Config parameter 'sc_namespace'");
        if ($::JOB_PARAMS{'NAMESPACE'} ne "") {
            $namespace = $::JOB_PARAMS{'NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'};
                $namespace = $::JOB_PARAMS{'NAMESPACE'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'sc_namespace' has not been set and Job parameter 'NAMESPACE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'NAMESPACE', nor Network Config parameter 'sc_namespace' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message("Checking Job parameters 'GENERATE_CERTIFICATES', 'INSTALL_CERTIFICATES', 'CERTIFICATES_TO_GENERATE' and 'CERTIFICATES_TO_INSTALL'");
        if ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "no" && $::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "no") {
            General::Logging::log_user_warning_message("Both Job parameter 'GENERATE_CERTIFICATES', and 'INSTALL_CERTIFICATES' are set to 'no', so there is nothing to do");
            return General::Playlist_Operations::RC_STEPOUT;
        } elsif ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" && $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} eq "") {
            General::Logging::log_user_error_message("You must specify which certificate to generate with parameter 'CERTIFICATES_TO_GENERATE'");
            return General::Playlist_Operations::RC_FALLBACK;
        }
        $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} =~ s/\s+//g;

        if ($::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "yes" && $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} eq "" && $::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" && $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} ne "") {
            # Just copy the CERTIFICATES_TO_GENERATE value
            General::Logging::log_user_message("Set CERTIFICATES_TO_INSTALL=$::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'}");
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} = $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'};
        } elsif ($::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "yes" && $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} eq "") {
            General::Logging::log_user_error_message("You must specify which certificate to install with parameter 'CERTIFICATES_TO_INSTALL'");
            return General::Playlist_Operations::RC_FALLBACK;
        } elsif ($::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "yes") {
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} =~ s/\s+//g;
        }

        if ($::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} eq "") {
            $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/certificates";
        }
        if (-d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}") {
            if ($::JOB_PARAMS{'SKIP_CERTIFICATE_GENERATE_IF_EXISTING'} eq "yes" && $::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" && $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} ne "") {
                # Now we need to check if the certificate files already exist.
                # If all wanted certificate files exist then we will not generate any certificates but just use the existing files.
                # If however one or more certificate files are missing then these needed certificates will be generated.
                # Initialize information about which files are needed for known certificate types
                my %certificate_files = (
                    "bsfload" =>
                    [
                        "bsfload/cert.pem",
                        "bsfload/key.pem"
                    ],

                    "bsfmgr" =>
                    [
                        "bsfmgr/cert.pem",
                        "bsfmgr/key.pem"
                    ],

                    "bsfwrk" =>
                    [
                        "bsfwrk/cert.pem",
                        "bsfwrk/key.pem"
                    ],

                    "cassandra-internode-external" =>
                    [
                        "cassandra-internode-external/cert.pem",
                        "cassandra-internode-external/key.pem"
                    ],

                    "chfsim" =>
                    [
                        "chfsim/cert.pem",
                        "chfsim/key.pem"
                    ],

                    "chfsim-sepp" =>
                    [
                        "chfsim/cert.pem",
                        "chfsim/key.pem"
                    ],

                    "cql-client-external" =>
                    [
                        "cql-client-external/cert.pem",
                        "cql-client-external/key.pem"
                    ],

                    "cql-server-external" =>
                    [
                        "cql-server-external/cert.pem",
                        "cql-server-external/key.pem"
                    ],

                    "diameter" =>
                    [
                        "diameter/cert.pem",
                        "diameter/key.pem"
                    ],

                    "dscload" =>
                    [
                        "dscload/cert.pem",
                        "dscload/key.pem"
                    ],

                    "ext-lj" =>
                    [
                        "ext-lj/cert.pem",
                        "ext-lj/key.pem"
                    ],

                    "ext-lj-x" =>
                    [
                        "ext-lj-1/cert.pem",
                        "ext-lj-1/key.pem",
                        "ext-lj-2/cert.pem",
                        "ext-lj-2/key.pem",
                        "ext-lj-3/cert.pem",
                        "ext-lj-3/key.pem",
                        "ext-lj-4/cert.pem",
                        "ext-lj-4/key.pem",
                        "ext-lj-5/cert.pem",
                        "ext-lj-5/key.pem"
                    ],

                    "influxdb" =>
                    [
                        "influxdb/cert.pem",
                        "influxdb/key.pem"
                    ],

                    "internal-ldap" =>
                    [
                        "ldap/cert.pem",
                        "ldap/key.pem"
                    ],

                    "k6" =>
                    [
                        "k6/cert.pem",
                        "k6/key.pem",
                        "k6/rp1/cert.pem",
                        "k6/rp1/key.pem",
                        "k6/rp2/cert.pem",
                        "k6/rp2/key.pem",
                        "k6/rp3/cert.pem",
                        "k6/rp3/key.pem",
                        "k6/sepp/cert.pem",
                        "k6/sepp/key.pem"
                    ],

                    "nbi" =>
                    [
                        "nbi/cert.pem",
                        "nbi/key.pem"
                    ],

                    "netconf-tls-client" =>
                    [
                        "netconf-tls-client/cert.pem",
                        "netconf-tls-client/key.pem"
                    ],

                    "nlf" =>
                    [
                        "nlf/cert.pem",
                        "nlf/key.pem"
                    ],

                    "nrfsim" =>
                    [
                        "nrfsim/cert.pem",
                        "nrfsim/key.pem"
                    ],

                    "pmrw" =>
                    [
                        "pmrw/cert.pem",
                        "pmrw/key.pem"
                    ],

                    "pvtb" =>
                    [
                        "pvtb/cert.pem",
                        "pvtb/key.pem",
                        "cert-probe-consumer/cert.pem",
                        "cert-probe-consumer/key.pem",
                        "consumer-ca/cert.pem",
                        "consumer-ca/key.pem"
                    ],

                    "referral-ldap" =>
                    [
                        "referral-ldap/cert.pem",
                        "referral-ldap/key.pem"
                    ],

                    "rootca" =>
                    [
                        "rootca/cert.pem",
                        "rootca/key.pem"
                    ],

                    "sc-monitor" =>
                    [
                        "sc-monitor/client.crt",
                        "sc-monitor/client.key"
                    ],

                    "scpmgr" =>
                    [
                        "scpmgr/cert.pem",
                        "scpmgr/key.pem"
                    ],

                    "scpwrk" =>
                    [
                        "scpwrk/cert.pem",
                        "scpwrk/key.pem"
                    ],

                    "seppmgr" =>
                    [
                        "seppmgr/cert.pem",
                        "seppmgr/key.pem"
                    ],

                    "seppsim" =>
                    [
                        "seppsim/cert.pem",
                        "seppsim/key.pem",
                        "seppsim/c/cert.pem",
                        "seppsim/c/key.pem",
                        "seppsim/p/cert.pem",
                        "seppsim/p/key.pem",
                        "seppsim/p1/cert.pem",
                        "seppsim/p1/key.pem",
                        "seppsim/p2/cert.pem",
                        "seppsim/p2/key.pem",
                        "seppsim/p3/cert.pem",
                        "seppsim/p3/key.pem",
                        "seppsim/p4/cert.pem",
                        "seppsim/p4/key.pem",
                        "seppsim/p5/cert.pem",
                        "seppsim/p5/key.pem",
                        "seppsim/p6/cert.pem",
                        "seppsim/p6/key.pem",
                        "seppsim/p7/cert.pem",
                        "seppsim/p7/key.pem",
                        "seppsim/p8/cert.pem",
                        "seppsim/p8/key.pem"
                    ],

                    "seppsim-n32c" =>
                    [
                        "rp1ca/cert.pem",
                        "rp1ca/key.pem",
                        "rp2ca/cert.pem",
                        "rp2ca/key.pem",
                        "rp3ca/cert.pem",
                        "rp3ca/key.pem",
                        "seppsim-n32c/c/cert.pem",
                        "seppsim-n32c/c/key.pem",
                        "seppsim-n32c/p/cert.pem",
                        "seppsim-n32c/p/key.pem",
                        "seppsim-n32c/p1/cert.pem",
                        "seppsim-n32c/p1/key.pem",
                        "seppsim-n32c/p2/cert.pem",
                        "seppsim-n32c/p2/key.pem",
                        "seppsim-n32c/p3/cert.pem",
                        "seppsim-n32c/p3/key.pem",
                        "seppsim-n32c/p4/cert.pem",
                        "seppsim-n32c/p4/key.pem",
                        "seppsim-n32c/p5/cert.pem",
                        "seppsim-n32c/p5/key.pem",
                        "seppsim-n32c/p6/cert.pem",
                        "seppsim-n32c/p6/key.pem",
                        "seppsim-n32c/p7/cert.pem",
                        "seppsim-n32c/p7/key.pem",
                        "seppsim-n32c/p8/cert.pem",
                        "seppsim-n32c/p8/key.pem"
                    ],

                    "seppsim-scp" =>
                    [
                        "seppsim-scp/cert.pem",
                        "seppsim-scp/key.pem",
                        "seppsim-scp/c/cert.pem",
                        "seppsim-scp/c/key.pem",
                        "seppsim-scp/p/cert.pem",
                        "seppsim-scp/p/key.pem",
                        "seppsim-scp/p1/cert.pem",
                        "seppsim-scp/p1/key.pem",
                        "seppsim-scp/p2/cert.pem",
                        "seppsim-scp/p2/key.pem",
                        "seppsim-scp/p3/cert.pem",
                        "seppsim-scp/p3/key.pem",
                        "seppsim-scp/p4/cert.pem",
                        "seppsim-scp/p4/key.pem",
                        "seppsim-scp/p5/cert.pem",
                        "seppsim-scp/p5/key.pem",
                        "seppsim-scp/p6/cert.pem",
                        "seppsim-scp/p6/key.pem",
                        "seppsim-scp/p7/cert.pem",
                        "seppsim-scp/p7/key.pem",
                        "seppsim-scp/p8/cert.pem",
                        "seppsim-scp/p8/key.pem"
                    ],

                    "seppwrk" =>
                    [
                        "seppwrk-ext/cert.pem",
                        "seppwrk-ext/key.pem",
                        "seppwrk-ext-ca/cert.pem",
                        "seppwrk-ext-ca/key.pem",
                        "seppwrk-int/cert.pem",
                        "seppwrk-int/key.pem",
                        "seppwrk-int-ca/cert.pem",
                        "seppwrk-int-ca/key.pem"
                    ],

                    "slf" =>
                    [
                        "slf/cert.pem",
                        "slf/key.pem"
                    ],

                    "syslog" =>
                    [
                        "syslog/cert.pem",
                        "syslog/key.pem"
                    ],

                    "telegraf" =>
                    [
                        "telegraf/cert.pem",
                        "telegraf/key.pem"
                    ],

                    "transformer" =>
                    [
                        "transformer/cert.pem",
                        "transformer/key.pem"
                    ],

                    "yang-provider" =>
                    [
                        "yang-provider/cert.pem",
                        "yang-provider/key.pem"
                    ],
                );
                my %needed_certificates;
                my $needed_generate_scenarios = "";

                for my $scenario (split ",", $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'}) {
                    $needed_certificates{$scenario}{'generate'} = 1;
                }
                for my $scenario (split ",", $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'}) {
                    $needed_certificates{$scenario}{'install'} = 1;
                }

                for my $scenario (sort keys %needed_certificates) {
                    if (exists $certificate_files{$scenario}) {
                        # A known scenario, check if certificate files already so we don't need to generate them again.
                        for my $filename (@{$certificate_files{$scenario}}) {
                            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/$filename") {
                                # No need to generate the certificate
                            } else {
                                # We need to generate the certificate
                                next if ($needed_generate_scenarios =~ /(^$scenario,|,$scenario,)/);     # Don't store duplicate scenarios
                                if ($scenario eq "rootca") {
                                    $needed_generate_scenarios = "$scenario," . $needed_generate_scenarios;
                                } else {
                                    $needed_generate_scenarios .= "$scenario,";
                                }
                            }
                        }
                    } else {
                        # An unknown scenario
                        if ($scenario eq "rootca") {
                            $needed_generate_scenarios = "$scenario," . $needed_generate_scenarios;
                        } else {
                            $needed_generate_scenarios .= "$scenario,";
                        }
                    }
                }
                $needed_generate_scenarios =~ s/,$//;
                if ($needed_generate_scenarios ne $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'}) {
                    $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} = $needed_generate_scenarios;
                    General::Logging::log_user_message("Change CERTIFICATES_TO_GENERATE=$::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'}");
                }
                if ($::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} eq "") {
                    $::JOB_PARAMS{'GENERATE_CERTIFICATES'} = "no";
                    General::Logging::log_user_message("No need to generate any certificates because they already exist.\nChange GENERATE_CERTIFICATES=no\n");
                    if ($::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "no") {
                        General::Logging::log_user_warning_message("Both Job parameter 'GENERATE_CERTIFICATES', and 'INSTALL_CERTIFICATES' are set to 'no', so there is nothing to do");
                        return General::Playlist_Operations::RC_STEPOUT;
                    }
                }
            }
        } else {
            # Create the directory
            General::Logging::log_user_message("Creating output directory");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "mkdir -p $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to create directory");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        }
        $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} = abs_path $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'};

        if ($::JOB_PARAMS{'KUBECONFIG'} eq "") {
            # Use the default file
            $::JOB_PARAMS{'KUBECONFIG'} = "$ENV{'HOME'}/.kube/config";
        }

        $message  = "NAMESPACE=$namespace\n";
        $message .= "CERTIFICATES_TO_GENERATE=$::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'}\n";
        $message .= "CERTIFICATES_TO_INSTALL=$::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'}\n";
        $message .= "CERTIFICATE_DIRECTORY=$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}\n";
        $message .= "KUBECONFIG=$::JOB_PARAMS{'KUBECONFIG'}\n";
        $message .= "SUPREME_VERSION=$::JOB_PARAMS{'SUPREME_VERSION'}\n";
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
    sub Check_SOFTWARE_DIR_And_Load_Docker_Image_If_Needed_P102S02T02 {

        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'SOFTWARE_DIR'} eq "") {
            General::Logging::log_user_message("No SOFTWARE_DIR specified, so we try to fetch the docker image from artifactory when running the supreme tool.\n");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif ($::JOB_PARAMS{'SOFTWARE_DIR'} ne "") {
            unless (-f "$::JOB_PARAMS{'SOFTWARE_DIR'}/tools/docker/eric-supreme-$::JOB_PARAMS{'SUPREME_VERSION'}.tar") {
                General::Logging::log_user_message("The file $::JOB_PARAMS{'SOFTWARE_DIR'}/tools/docker/eric-supreme-$::JOB_PARAMS{'SUPREME_VERSION'}.tar does not exist.\nWe try to fetch the docker image from artifactory when running the supreme tool.\n");
                return General::Playlist_Operations::RC_TASKOUT;
            }
        }

        General::Logging::log_user_message("Loading docker image $::JOB_PARAMS{'SOFTWARE_DIR'}/tools/docker/eric-supreme-$::JOB_PARAMS{'SUPREME_VERSION'}.tar");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'CONTAINER_COMMAND'} image load -i $::JOB_PARAMS{'SOFTWARE_DIR'}/tools/docker/eric-supreme-$::JOB_PARAMS{'SUPREME_VERSION'}.tar",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_warning_message("Failed to load the docker image, we ignore this error and hope that it can fetch the image from artifactory when running the supreme tool.");
            return General::Playlist_Operations::RC_TASKOUT;
        }
        my $image_id = "";
        for (@result) {
            if (/^Loaded image ID: sha256:([0-9a-f]{64})\s*$/) {
                #Loaded image ID: sha256:b964d5ad2f2224e46d9f76bc67e5d934a19b8c13a3df00e07e9ffe67de59ea12
                $image_id = $1;
                last;
            } elsif (/^Loaded image: armdocker\.rnd\.ericsson\.se\/proj-5g-bsf\/supreme\/supreme:$::JOB_PARAMS{'SUPREME_VERSION'}/) {
                #Loaded image: armdocker.rnd.ericsson.se/proj-5g-bsf/supreme/supreme:1.0.12
                # The image must have been loaded before so we don't need to tag it.
                General::Logging::log_user_message("The docker image must have been loaded before, so no need to tag it");
                return 0;
            }
        }
        if ($image_id eq "") {
            General::Logging::log_user_warning_message("Failed to find the docker image id, we ignore this error and hope that it can fetch the image from artifactory when running the supreme tool.");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Tagging docker image $image_id as armdocker.rnd.ericsson.se/proj-5g-bsf/supreme/supreme:$::JOB_PARAMS{'SUPREME_VERSION'}");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'CONTAINER_COMMAND'} tag $image_id armdocker.rnd.ericsson.se/proj-5g-bsf/supreme/supreme:$::JOB_PARAMS{'SUPREME_VERSION'}",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_warning_message("Failed to tag the docker image, we ignore this error and hope that it can fetch the image from artifactory when running the supreme tool.");
            return General::Playlist_Operations::RC_TASKOUT;
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
sub Generate_Install_Certificates_P102S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Kube_Config_File_P102S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Supreme_Config_File_P102S03T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Collect_User_Id_And_Group_P102S03T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Generate_Install_Certificates_P102S03T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Generate_Install_Special_SEPP_RP_20322_Certificate_P102S03T05 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Generate_Special_K6_RP_206033_Certificate_P102S03T06 } );
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
    sub Copy_Kube_Config_File_P102S03T01 {

        my $rc = 0;

        General::Logging::log_user_message("Copying kube config file to workspace directory");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "cp -p $::JOB_PARAMS{'KUBECONFIG'} $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/kube_config",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy file");
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
    sub Create_Supreme_Config_File_P102S03T02 {

        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/supreme_$::JOB_PARAMS{'NAMESPACE'}.yaml";
        my $rc = 0;
        my @lines = ();

        General::Logging::log_user_message("Get CMYP Information");
        (undef, $::JOB_PARAMS{'CERTIFICATE_CMYP_IP'}, $::JOB_PARAMS{'CERTIFICATE_CMYP_CLI_PORT'}, $::JOB_PARAMS{'CERTIFICATE_CMYP_NETCONF_PORT'}, undef) = ADP::Kubernetes_Operations::get_cmyp_info($::JOB_PARAMS{'NAMESPACE'});

        push @lines, "defaultScenarios:";
        push @lines, "  outputDir: $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}";
        push @lines, "  expirationDays: $::JOB_PARAMS{'CERTIFICATE_VALIDITY_DAYS'}";
        push @lines, "admin:";
        push @lines, "  namespace: $::JOB_PARAMS{'NAMESPACE'}";
        push @lines, "  yangProvider:";
        push @lines, "    username: $::JOB_PARAMS{'CERTIFICATE_USER_NAME'}";
        push @lines, "    password: $::JOB_PARAMS{'CERTIFICATE_USER_PASSWORD'}";
        push @lines, "    ip: $::JOB_PARAMS{'CERTIFICATE_CMYP_IP'}" if ($::JOB_PARAMS{'CERTIFICATE_CMYP_IP'} ne "");
        push @lines, "    port: $::JOB_PARAMS{'CERTIFICATE_CMYP_NETCONF_PORT'}" if ($::JOB_PARAMS{'CERTIFICATE_CMYP_NETCONF_PORT'} ne "");

        General::Logging::log_user_message("Creating config file");
        $rc = General::File_Operations::write_file(
            {
                "filename"            => $filename,
                "output-ref"          => \@lines,
                "eol-char"            => "\n",
                "append-file"         => 0,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to create file");
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
    sub Collect_User_Id_And_Group_P102S03T03 {

        my $rc = 0;
        my @result;

        $::JOB_PARAMS{'USER_UID'} = "";
        $::JOB_PARAMS{'USER_GID'} = "";
        General::Logging::log_user_message("Fetching user information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "id",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch user information");
            return 1;
        }
        for (@result) {
            if (/^\s*uid=(\d+).+gid=(\d+).+/) {
                # uid=1001(eccd) gid=1002(eccd) groups=1002(eccd),482(wheel),486(systemd-journal),1001(adm)
                $::JOB_PARAMS{'USER_UID'} = $1;
                $::JOB_PARAMS{'USER_GID'} = $2;
            }
        }

        General::Logging::log_user_message("USER=$ENV{'USER'}\nUSER_UID=$::JOB_PARAMS{'USER_UID'}\nUSER_GID=$::JOB_PARAMS{'USER_GID'}\n");

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
    sub Generate_Install_Certificates_P102S03T04 {

        my $command = "";
        my $rc = 0;

        if ($::JOB_PARAMS{'CONTAINER_COMMAND'} =~ /nerdctl/) {
            # Removed --user since it seems to cause issues when loading secrets via kube-api on newer versions of nerdctl
            $command = sprintf "%s%s run --init-binary docker-init --rm --network host --env USER=%s --volume %s:%s:rw --workdir %s ",
                ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes" ? "echo " : ""),
                $::JOB_PARAMS{'CONTAINER_COMMAND'},    # docker / sudo docker
                $ENV{'USER'},                       # --env USER=
                $::JOB_PARAMS{'_JOB_CONFIG_DIR'},   # --volume first part
                $::JOB_PARAMS{'_JOB_CONFIG_DIR'},   # --volume second part
                $::JOB_PARAMS{'_JOB_CONFIG_DIR'};   # --workdir
        } else {
            $command = sprintf "%s%s run --init --rm --network host --env USER=%s --volume %s:%s:rw --workdir %s --user %i:%i ",
                ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes" ? "echo " : ""),
                $::JOB_PARAMS{'CONTAINER_COMMAND'},    # docker / sudo docker
                $ENV{'USER'},                       # --env USER=
                $::JOB_PARAMS{'_JOB_CONFIG_DIR'},   # --volume first part
                $::JOB_PARAMS{'_JOB_CONFIG_DIR'},   # --volume second part
                $::JOB_PARAMS{'_JOB_CONFIG_DIR'},   # --workdir
                $::JOB_PARAMS{'USER_UID'},          # --user first part
                $::JOB_PARAMS{'USER_GID'};          # --user second part
        }

        $command .= sprintf "--volume %s:%s:rw ",
            $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'},
            $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'};
        $command .= "armdocker.rnd.ericsson.se/proj-5g-bsf/supreme/supreme:$::JOB_PARAMS{'SUPREME_VERSION'} ";
        $command .= "-p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/supreme_$::JOB_PARAMS{'NAMESPACE'}.yaml ";
        $command .= "--namespace $::JOB_PARAMS{'NAMESPACE'} ";
        $command .= "--kubeconfig $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/kube_config ";
        $command .= "--loglevel debug";

        if ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" && $::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "no") {
            General::Logging::log_user_message("Generating Certificates.\nThis will take a while to complete.\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command generate -d $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'}",
                    "hide-output"   => 1,
                }
            );
        } elsif ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "no" && $::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "yes") {
            General::Logging::log_user_message("Installing Certificates.\nThis will take a while to complete.\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command install -d $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'}",
                    "hide-output"   => 1,
                }
            );
        } elsif ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" && $::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "yes") {
            if ($::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} eq $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'}) {
                General::Logging::log_user_message("Generating and Installing Certificates.\nThis will take a while to complete.\n");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$command generate install -d $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'}",
                        "hide-output"   => 1,
                    }
                );
            } else {
                # Different certificates to generate and install
                General::Logging::log_user_message("Generating Certificates.\nThis will take a while to complete.\n");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$command generate -d $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'}",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to execute the supreme tool");
                    return $rc;
                }

                General::Logging::log_user_message("Installing Certificates.\nThis will take a while to complete.\n");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$command install -d $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'}",
                        "hide-output"   => 1,
                    }
                );
            }
        }

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Certificates/Secrets generated/installed successfully";
            # Write extra status messages to file
            General::File_Operations::write_file(
                {
                    "filename"          => "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/extra_status_messages.txt",
                    "append-file"       => 1,
                    "output-ref"        => [
                        "Certificate Files",
                        "=================",
                        "",
                        "The generated certificate files are stored in the following directory:",
                        "  $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/",
                        "",
                    ],
                }
            );
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the supreme tool");
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
    sub Generate_Install_Special_SEPP_RP_20322_Certificate_P102S03T05 {

        my %certs_to_generate = map { $_ => 1 } split /,/, $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'};
        my %certs_to_install  = map { $_ => 1 } split /,/, $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'};
        my $cmyp_ip;
        my $pkcs_traf1 = "";
        my $rc = 0;
        my @result;
        my $rp20322ca = "";

        if ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" && exists $certs_to_generate{'seppwrk'}) {
            #
            # Create output directory
            #

            if ( -d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322") {
                General::Logging::log_user_message("Directory sepp_rp20322 already exist");
            } else {
                General::Logging::log_user_message("Creating directory for sepp_rp20322 files");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "mkdir -p $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the sepp_rp20322 directory");
                    return 1;
                }
            }

            #
            # Creating RP_20322.cli file
            #

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1_input.txt") {
                General::Logging::log_user_message("File traf1_input.txt already exist");
            } else {
                General::Logging::log_user_message("Creating traf1_input.txt file");
                $rc = General::File_Operations::write_file(
                    {
                        "filename"            => "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1_input.txt",
                        "output-ref"          => [
                            'subjectAltName=DNS:traf1.domain.tld',
                        ],
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write the traf1_input.txt file");
                    return 1;
                }
            }

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1.key") {
                General::Logging::log_user_message("File traf1.key already exist");
            } else {
                General::Logging::log_user_message("Creating traf1.key file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "openssl req -newkey rsa:2048 -nodes -keyout $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1.key -subj \"/C=SE/ST=ST/L=ST/O=Ericsson/CN=sepp.ericsson.se\" -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1.csr",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the traf1.key file");
                    return 1;
                }
            }

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1.crt") {
                General::Logging::log_user_message("File traf1.crt already exist");
            } else {
                General::Logging::log_user_message("Creating traf1.crt file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "openssl x509 -req -extfile $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1_input.txt -in $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1.csr -CA $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/cert.pem -CAkey $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/key.pem -CAcreateserial -days $::JOB_PARAMS{'CERTIFICATE_VALIDITY_DAYS'} -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1.crt",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the traf1.crt file");
                    return 1;
                }
            }

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/pkcs_traf1.p12") {
                General::Logging::log_user_message("Creating pkcs_traf1.p12 file");
            } else {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "openssl pkcs12 -export -inkey $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1.key -in $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/traf1.crt -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/pkcs_traf1.p12 -password pass:rootroot",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the pkcs_traf1.p12 file");
                    return 1;
                }
            }

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/RP_20322.cli") {
                General::Logging::log_user_message("File RP_20322.cli already exist");
            } else {
                General::Logging::log_user_message("base64 encoding pkcs_traf1.p12 file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "base64 -w 0 $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/pkcs_traf1.p12",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0 && scalar @result == 1) {
                    $pkcs_traf1 = $result[0];
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to encode the pkcs_traf1.p12 file");
                    return 1;
                }

                General::Logging::log_user_message("base64 encoding rootca/cert.pem file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "base64 -w 0 $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/cert.pem",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0 && scalar @result == 1) {
                    $rp20322ca = $result[0];
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to encode the rootca/cert.pem file");
                    return 1;
                }

                General::Logging::log_user_message("Creating RP_20322.cli file");
                $rc = General::File_Operations::write_file(
                    {
                        "filename"            => "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/RP_20322.cli",
                        "output-ref"          => [
                            "config",
                            "truststore certificates sc-traf-root-ca-list1",
                            "commit",
                            "end",
                            "truststore certificates sc-traf-root-ca-list1 install-certificate-pem name RP20322CA pem $rp20322ca",
                            "keystore asymmetric-keys install-asymmetric-key-pkcs12 name sc-traf-default-key1 certificate-name sc-traf-default-cert1 p12-password rootroot p12 $pkcs_traf1",
                            "exit",
                        ],
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write the RP_20322.cli file");
                    return 1;
                }
            }
        } else {
            General::Logging::log_user_message("No need to create special RP_20322.cli file");
        }

        #
        # Creating RP_20322.cli file
        #

        if ($::JOB_PARAMS{'INSTALL_CERTIFICATES'} eq "yes" && exists $certs_to_install{'seppwrk'}) {
            if (-f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/RP_20322.cli") {
                # The file exist, so install it into CMYP CLI
                General::Logging::log_user_message("Finding CMYP IP-address");
                $cmyp_ip = ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'NAMESPACE'});

                General::Logging::log_user_message("Loading RP_20322.cli file into CMYP CLI");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                                "--ip=$cmyp_ip " .
                                                "--user=$::JOB_PARAMS{'CERTIFICATE_USER_NAME'} " .
                                                "--password='$::JOB_PARAMS{'CERTIFICATE_USER_PASSWORD'}' " .
                                                "--port=22 " .
                                                "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                                "--command-file='$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/RP_20322.cli' ",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to load RP_20322.cli into CMYP CLI");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("The following file does not exist and cannot be loaded which might result in problems to run SEPP traffic:\n$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/sepp_rp20322/RP_20322.cli");
            }
        } else {
            General::Logging::log_user_message("No need to install special RP_20322.cli file");
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
    sub Generate_Special_K6_RP_206033_Certificate_P102S03T06 {

        my %certs_to_generate = map { $_ => 1 } split /,/, $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'};
        my $cmyp_ip;
        my $pkcs_traf1 = "";
        my $rc = 0;
        my @result;
        my $rp20322ca = "";

        if ($::JOB_PARAMS{'GENERATE_CERTIFICATES'} eq "yes" && exists $certs_to_generate{'k6'} && exists $certs_to_generate{'seppsim'}) {
            #
            # Create output directory
            #

            if ( -d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033") {
                General::Logging::log_user_message("Directory k6/rp206033 already exist");
            } else {
                General::Logging::log_user_message("Creating directory for k6/rp206033 files");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "mkdir -p $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the k6 directory");
                    return 1;
                }
            }

            #
            # Creating RP_206033.crt and RP_206033.key files
            #

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033_input.txt") {
                General::Logging::log_user_message("File RP_206033_input.txt already exist");
            } else {
                General::Logging::log_user_message("Creating RP_206033_input.txt file");
                $rc = General::File_Operations::write_file(
                    {
                        "filename"            => "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033_input.txt",
                        "output-ref"          => [
                            '[req]',
                            'distinguished_name = dn',
                            'x509_extensions = v3_req',
                            'req_extensions = v3_req',
                            'prompt = no',
                            '',
                            '[dn]',
                            'C = BE',
                            'L = Brussels',
                            'O = Ericsson',
                            'CN = nfamf.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org',
                            '',
                            '[v3_req]',
                            'subjectAltName = @alt_names',
                            '',
                            '[alt_names]',
                            'DNS.1 = *.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org',
                        ],
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write the RP_206033_input.txt file");
                    return 1;
                }
            }

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.key") {
                General::Logging::log_user_message("File RP_206033.key already exist");
            } else {
                General::Logging::log_user_message("Creating RP_206033.key file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "openssl req -nodes -newkey rsa:2048 -keyout $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.key -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/csr206033.pem -config $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033_input.txt",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the RP_206033.key file");
                    return 1;
                }
            }

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.crt") {
                General::Logging::log_user_message("File RP_206033.crt already exist");
            } else {
                General::Logging::log_user_message("Creating RP_206033.crt file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "openssl x509 -req -in $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/csr206033.pem -CA $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/cert.pem -CAkey $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/key.pem -CAcreateserial -days $::JOB_PARAMS{'CERTIFICATE_VALIDITY_DAYS'} -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.crt -extensions 'v3_req' -extfile $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033_input.txt",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the RP_206033.crt file");
                    return 1;
                }
            }
        } else {
            General::Logging::log_user_message("No need to create special RP_206033 certificate files");
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
sub Cleanup_Job_Environment_P102S04 {

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
sub Fallback001_P102S99 {

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
        'CERTIFICATE_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then this directory will be used for storing or reading the
certificate files.
If you don't specify this parameter then it will take the default directory which
is stored in job parameter _JOB_CONFIG_DIR/certificates/.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CERTIFICATE_USER_NAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "sec-admin",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which user name is used for loading the certificate.

This parameter is optional and if not specified then the default value is used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CERTIFICATE_USER_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "secsec",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which password to be used for loading the certificate.

This parameter is optional and if not specified then the default value is used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CERTIFICATES_TO_GENERATE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which certificates to generate, it should be specified
as a comma separated list of certificates to generate.

Known values as of the last update of this playlist and valid for version
1.0.12 of the Supreme tool are the following:

    - rootca
    - bsfload
    - bsfmgr
    - bsfwrk
    - cassandra-internode-external
    - chfsim
    - chfsim-sepp (this overwrites the same files as 'chfsim' but with different data)
    - cql-client-external
    - cql-server-external
    - diameter
    - dscload
    - ext-lj
    - ext-lj-x
    - influxdb
    - internal-ldap
    - k6
    - nbi
    - netconf-tls-client
    - nlf
    - nrfsim
    - pmrw
    - pvtb
    - referral-ldap
    - scpmgr
    - scpwrk
    - seppmgr
    - seppsim
    - seppsim-n32c
    - seppsim-scp
    - seppwrk
    - slf
    - syslog
    - telegraf
    - transformer
    - yang-provider

If not specified and GENERATE_CERTIFICATES=yes then the playlist will fail.

The 'rootca' will be automatically created if not specified and the certificate
does not already exist in the CERTIFICATE_DIRECTORY.

NOTE:
Already existing certificates in the CERTIFICATE_DIRECTORY will be OVERWRITTEN, except
for the 'rootca' which is never overwritten if it already exists.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CERTIFICATES_TO_INSTALL' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which certificates to install, it should be specified as
a comma separated list of certificates to generate.
For known values, see CERTIFICATES_TO_GENERATE above.
If not set then it will use the same value as specified by CERTIFICATES_TO_GENERATE.

If not set and also CERTIFICATES_TO_GENERATE is not set and INSTALL_CERTIFICATES=yes
then the playlist will fail.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CERTIFICATE_VALIDITY_DAYS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "365",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls how long the created certificates will be valid for.

If not specified then a default value is used.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'GENERATE_CERTIFICATES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if new certificates should be generated or not.

If value is 'no' which is also the default value then no new certificates are
created, instead it will just load the specified certificate files.

If value is 'yes' then new certificate files are created and installed if also
INSTALL_CERTIFICATES is 'yes'.

WARNING:
Generating new certificates and installing them on top of already existing
certificates can cause big problems, especially if BSF/WCDB  Geographical
Redundancy is used because it would require a re-deployment of the node/network.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'INSTALL_CERTIFICATES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certificates should be installed or not.

If value is 'no' which is also the default value then no certificates are
install.
See also GENERATE_CERTIFICATES.

If value is 'yes' then certificate files are installed.

WARNING:
Generating new certificates and installing them on top of already existing
certificates can cause big problems, especially if BSF/WCDB  Geographical
Redundancy is used because it would require a re-deployment of the node/network.
EOF
            'validity_mask' => '(yes|no)',
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
        'NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The namespace to use for generating and/or installing the secrets or certificates
into.
If you don't specify this parameter then it will take the value from the network
configuration file parameter 'sc_namespace'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_CERTIFICATE_GENERATE_IF_EXISTING' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certificates should be generated if they already
exist.

If value is 'yes' which is also the default value then no certificates are
generated if the certificates already exist.
See also GENERATE_CERTIFICATES.

If value is 'no' then certificate files are always generated even if they
already exist. The only exception is the RootCA which is never overwritten if
it already exist.

WARNING:
Generating new certificates and installing them on top of already existing
certificates can cause big problems, especially if BSF/WCDB  Geographical
Redundancy is used because it would require a re-deployment of the node/network.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SOFTWARE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds all the software that was used for deploying
or upgrading the SC software, i.e. the directory that contains the 'eric-sc-*.csar' and
'eric-sc-tools-*.tgz' files.
This directory will be used to find the supreme docker image file that is used for
generating and installing the certificates.
This file is located under the tools/docker/ sub-directory and it should contain
a file named e.g. 'eric-supreme-1.0.12.tar'.
If this parameter is not specified then the supreme docker image is fetched from
artifactory.
NOTE: The version of the file e.g. 1.0.12 should match the version specified by
the SUPREME_VERSION variable, if not then also the image is fetched from artifactory.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SUPREME_VERSION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "1.0.12",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The version of the supreme tool to use for generating/installing certificates
and secrets.
EOF
            'validity_mask' => '\d+\.\d+\.\d+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist script is used for generating and installing certificate and/or
secret data to an ESC node and it will be called from the execute_playlist.pl
script.

It will fetch and use a docker image for the 'supreme' tool.

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

package Playlist::209_EVNFM_Delete_Package_SC;

################################################################################
#
#  Author   : eedwiq, eustone
#
#  Revision : 1.1
#  Date     : 2024-03-26 15:35:40
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
    $::JOB_PARAMS{'JOBTYPE'} = "EVNFM_DELETE_PACKAGE";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (General::Playlist_Operations::is_mandatory_network_config_loaded() != 0) {
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P209S01, \&Fallback001_P209S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P209S02, \&Fallback001_P209S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Delete_Package_SC_Via_EVNFM_P209S03, \&Fallback001_P209S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P209S04, \&Fallback001_P209S99 );
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
sub Initialize_Job_Environment_P209S01 {

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
sub Check_Job_Parameters_P209S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P209S02T01 } );
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
    sub Check_Job_Parameters_P209S02T01 {

        my $ingress_host_address = "";
        my $password = "";
        my $rc = 0;
        my @result;
        my $username = "";

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
sub Delete_Package_SC_Via_EVNFM_P209S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Session_P209S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_Packages_P209S03T02 } );
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
    sub Create_Session_P209S03T01 {

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
    sub Delete_Packages_P209S03T02 {

        my $json_data;
        my $list_of_packages        = "";
        my %eligable_for_delete;
        my %exempted_from_delete;
        my $remove_for              = "Signaling_Controller"; # future changes to distinguish between SC, cnDSC and other
        my %application_type;
        my $matching                = ""; # default is to remove no packages

        $application_type{"SC"}     = "Signaling_Controller";
        $application_type{"DSC"}    = "DSC";
        $application_type{"CNDSC"}  = "DSC";
        $application_type{"ALL"}    = "ALL";
        $application_type{"GARBAGE"}= "GARBAGE";

        if ($::JOB_PARAMS{'APP_TYPE'}) {
            if ( exists $application_type{uc($::JOB_PARAMS{'APP_TYPE'})} ) {
                $remove_for = $application_type{uc($::JOB_PARAMS{'APP_TYPE'})};
            } else {
                General::Logging::log_user_message("Parameter APP_TYPE contains an incorrect value: ".$::JOB_PARAMS{'APP_TYPE'});
                General::Logging::log_user_message("Script will continue with default: $remove_for");
            }
        }
        if ($::JOB_PARAMS{'PACKAGE_CONTAINS'}) {
            if (uc($::JOB_PARAMS{'PACKAGE_CONTAINS'}) eq "MASTER") {
                $matching = ".25";
            } else {
                $matching = $::JOB_PARAMS{'PACKAGE_CONTAINS'};
            }
        }

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
            $list_of_packages .= sprintf "%-36s  %-36s  %-25s  %-18s  %-25s  %-15s  %-16s  %-10s\n",
                "vnfPkgId",
                "vnfdId",
                "vnfProductName",
                "vnfSoftwareVersion",
                "vnfdVersion",
                "onboardingState",
                "operationalState",
                "usageState";
            $list_of_packages .= sprintf "%-36s  %-36s  %-25s  %-18s  %-25s  %-15s  %-16s  %-10s\n",
                "-"x36,
                "-"x36,
                "-"x25,
                "-"x18,
                "-"x25,
                "-"x15,
                "-"x16,
                "-"x10;

            for my $key (sort keys %$json_data) {
                $list_of_packages .= sprintf "%-36s  %-36s  %-25s  %-18s  %-25s  %-15s  %-16s  %-10s\n",
                    $key,
                    (exists $json_data->{$key}{"vnfdId"}                ? $json_data->{$key}{"vnfdId"}              : "-"),
                    (exists $json_data->{$key}{"vnfProductName"}        ? $json_data->{$key}{"vnfProductName"}      : "-"),
                    (exists $json_data->{$key}{"vnfSoftwareVersion"}    ? $json_data->{$key}{"vnfSoftwareVersion"}  : "-"),
                    (exists $json_data->{$key}{"vnfdVersion"}           ? $json_data->{$key}{"vnfdVersion"}         : "-"),
                    (exists $json_data->{$key}{"onboardingState"}       ? $json_data->{$key}{"onboardingState"}     : "-"),
                    (exists $json_data->{$key}{"operationalState"}      ? $json_data->{$key}{"operationalState"}    : "-"),
                    (exists $json_data->{$key}{"usageState"}            ? $json_data->{$key}{"usageState"}          : "-");

                if ((exists $json_data->{$key}{"usageState"}) && ($json_data->{$key}{"usageState"} eq "NOT_IN_USE")) {
                    if ((uc($json_data->{$key}{"vnfProductName"}) eq uc($remove_for)) || (uc($remove_for) eq "ALL") || (uc($remove_for) eq "GARBAGE")){  # check APPLICATION_TYPE (Signalling Controller , DSC)
                        if ($matching ne "") {  # check if specific (range) Software version needs to be removed
                            if (((index($json_data->{$key}{"vnfSoftwareVersion"},$matching) != -1) || (uc($matching) eq "ALL")) && (uc($remove_for) ne "GARBAGE")) {
                                $eligable_for_delete{$key}{"PRODUCT"} = $json_data->{$key}{"vnfProductName"};
                                $eligable_for_delete{$key}{"SOFTWARE-VERSION"} = $json_data->{$key}{"vnfSoftwareVersion"};
                            } elsif (uc($remove_for) eq "GARBAGE") {
                                if ( (uc($json_data->{$key}{"vnfProductName"}) ne uc($application_type{"SC"})) &&
                                     (uc($json_data->{$key}{"vnfProductName"}) ne uc($application_type{"DSC"})) &&
                                     (uc($json_data->{$key}{"vnfProductName"}) ne uc($application_type{"CNDSC"}))) {
                                    $eligable_for_delete{$key}{"PRODUCT"} = $json_data->{$key}{"vnfProductName"};
                                    $eligable_for_delete{$key}{"SOFTWARE-VERSION"} = $json_data->{$key}{"vnfSoftwareVersion"};
                                }
                            } else {
                                $exempted_from_delete{$key}{"PRODUCT"}          = $json_data->{$key}{"vnfProductName"};
                                $exempted_from_delete{$key}{"SOFTWARE-VERSION"} = $json_data->{$key}{"vnfSoftwareVersion"};
                                if ($matching ne ".25") {
                                    $exempted_from_delete{$key}{"REASON"}           = "package does not contain '$matching'";
                                } else {
                                    $exempted_from_delete{$key}{"REASON"}           = "package is not 'master'";
                                }
                            }
                        } else {
                            $exempted_from_delete{$key}{"PRODUCT"}          = $json_data->{$key}{"vnfProductName"};
                            $exempted_from_delete{$key}{"SOFTWARE-VERSION"} = $json_data->{$key}{"vnfSoftwareVersion"};
                            $exempted_from_delete{$key}{"REASON"}           = "-v PACKAGE_CONTAINS is either empty or parameter is NOT provided";
                        }
                    } else {
                        $exempted_from_delete{$key}{"PRODUCT"}          = $json_data->{$key}{"vnfProductName"};
                        $exempted_from_delete{$key}{"SOFTWARE-VERSION"} = $json_data->{$key}{"vnfSoftwareVersion"};
                        $exempted_from_delete{$key}{"REASON"}           = "-v APP_TYPE $remove_for is not matching ".$json_data->{$key}{"vnfProductName"};
                    }
                }
            }
        } else {
            General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
            return 0;
        }
        $list_of_packages .= "\n";
        General::Logging::log_user_message($list_of_packages);

        if (%exempted_from_delete) {
            General::Logging::log_user_message("\nPackages NOT marked for deletion");
            General::Logging::log_user_message(sprintf "   %-36s   %-36s   %-48s","vnfPkgId","vnfSoftwareVersion","Reason for not deleting");
            General::Logging::log_user_message(sprintf "   %-36s   %-36s   %-48s","-"x36,"-"x36,"-"x48);
            foreach my $package_exempted (keys %exempted_from_delete) {
                General::Logging::log_user_message(sprintf "   %-36s   %-36s   %-48s", $package_exempted, $exempted_from_delete{$package_exempted}{"SOFTWARE-VERSION"}, $exempted_from_delete{$package_exempted}{"REASON"});
            }
        }

        if (%eligable_for_delete) {
            if ($matching ne ".25") {
                General::Logging::log_user_message("\nDeleting Packages (Matching '$matching') for application type $remove_for :");
            } else {
                General::Logging::log_user_message("\nDeleting Packages (Matching 'master'):");
            }
            General::Logging::log_user_message(sprintf "   %-36s   %-36s","vnfPkgId","vnfSoftwareVersion");
            General::Logging::log_user_message(sprintf "   %-36s   %-36s","-"x36,"-"x36);

            foreach my $package_to_delete (keys %eligable_for_delete) {
                General::Logging::log_user_message(sprintf "   %-36s   %-36s", $package_to_delete, $eligable_for_delete{$package_to_delete}{"SOFTWARE-VERSION"});
                $json_data = ADP::EVNFM_Operations::delete_vnf_package(
                    {
                        "debug-messages"        => 1,
                        "hide-output"           => 1,
                        "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                        "package-id"            => $package_to_delete,
                        "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                    }
                );
            }
        } else {
            General::Logging::log_user_message("\nNothing to delete, there are no packages that are 'NOT IN USE' matching '$matching'");
        }
        General::Logging::log_user_message("\n");
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
sub Cleanup_Job_Environment_P209S04 {

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
sub Fallback001_P209S99 {

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

sub set_playlist_variables {
    %playlist_variables = (

        # ---------------------------------------------------------------------
        'APP_TYPE' => {
            'case_check'    => "no", # <lowercase|no|uppercase>
            'default_value' => "SC",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls for which application type packages are being removed.
Current possible values are:
  SC for type Signalling Controller
  DSC for type DSC
  cnDSC for type DSC
  ALL for SC and DSC and any other type present in evnfm
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
        'PACKAGE_CONTAINS' => {
            'case_check'    => "no", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter indicated that only packages containing the match should be
deleted. This can either be one specific package (ex. 1.10.0+9) or all versions
of a package (ex. 1.10.0). In addition the package may also be "master" to
remove all master packages, it can also be "all" to remove all unused packages.
In case an empty string is passed nothing will be deleted.
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

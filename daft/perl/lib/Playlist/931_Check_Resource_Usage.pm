package Playlist::931_Check_Resource_Usage;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.9
#  Date     : 2023-04-19 13:00:21
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
    usage_return_playlist_variables
    );

#
# Used Perl package files
#

use ADP::Kubernetes_Operations;
use General::Data_Structure_Operations;
use General::File_Operations;
use General::Json_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;
use General::State_Machine;
use General::String_Operations;
use General::Yaml_Operations;

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

    my $rc = 0;

    # If the playlist needs to have certain job parameter values for the execution
    # of the playlist then it needs to check if they have already been given by the
    # user when starting the job, otherwise the user should be prompted for their
    # value.
    if (General::Playlist_Operations::parse_playlist_variables(\%playlist_variables, \%::JOB_PARAMS, 0) != 0) {
        General::Logging::log_user_error_message("Failed to parse the playlist variables");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Set tasks to always execute all tasks in this playlist
    General::State_Machine::always_execute_task("Playlist::931_Check_Resource_Usage.+");

    # Execute the different steps in this playlist

    if (exists $::JOB_PARAMS{'P931_COLLECT_RESOURCE_USAGE'} && $::JOB_PARAMS{'P931_COLLECT_RESOURCE_USAGE'} eq "yes") {

        $rc = General::Playlist_Operations::execute_step( \&Collect_Resource_Usage_P931S01, undef );
        return $rc if $rc < 0;

    }

    if ( (exists $::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES'} && $::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES'} eq "yes") ||
        (exists $::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES_MULTIPLE_FILES'} && $::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES_MULTIPLE_FILES'} eq "yes") ) {

        $rc = General::Playlist_Operations::execute_step( \&Calculate_Needed_Resources_P931S02, undef );
        return $rc if $rc < 0;

    }

    if (exists $::JOB_PARAMS{'P931_CHECK_REQUIRED_RESOURCES'} && $::JOB_PARAMS{'P931_CHECK_REQUIRED_RESOURCES'} eq "yes") {

        $rc = General::Playlist_Operations::execute_step( \&Check_Required_Resources_P931S03, undef );
        return $rc if $rc < 0;

    }

    if (exists $::JOB_PARAMS{'P931_COMPARE_RESOURCE_USAGE'} && $::JOB_PARAMS{'P931_COMPARE_RESOURCE_USAGE'} eq "yes") {

        $rc = General::Playlist_Operations::execute_step( \&Compare_Resource_Usage_P931S04, undef );
        return $rc if $rc < 0;

    }

    # Delete any input job variables named P931_xxxx so the next time
    # this playlist is called we parse the %playlist_variables hash
    # correctly.
    my @variables_to_delete = ();
    for my $key (keys %::JOB_PARAMS) {
        push @variables_to_delete, $key if ($key =~ /^P931_.+/);
    }
    for (@variables_to_delete) {
        delete $::JOB_PARAMS{$_};
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
sub Collect_Resource_Usage_P931S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Collect_Resource_Usage_Information_P931S01T01 } );
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
    sub Collect_Resource_Usage_Information_P931S01T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/print_resource_statistics.pl";
        my $command_parameters = "--no-formatting --all-namespaces --node-role all --log-file=$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log";
        my $output_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage.txt";
        my $rc;
        my @result;
        my @std_err;

        if ($::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} ne "") {
            $output_file = $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'};
        }

        if ($::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_CSV'} ne "") {
            $command_parameters .= " --csv-file=$::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_CSV'}";
        } else {
            $command_parameters .= " --csv-file=$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage.csv";
        }

        General::Logging::log_user_message("Print used resources.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command $command_parameters",
                "hide-output"   => 1,
                "save-to-file"  => $output_file,
                "return-output" => \@result,
                "return-stderr" => \@std_err,
            }
        );
        if ($rc == 0 || $rc == 2) {
            # Only print parts of the total printout
            my $info = "";
            my $count = 0;
            my $found = 0;
            for (@result) {
                if ($found == 0) {
                    if (/^Available CPU Resources:\s*$/) {
                        # Skip all lines up until this line
                        $found = 1;
                        $info .= "$_\n";
                    }
                } else {
                    $info .= "$_\n";
                    if (/^(Total|Highest) available (master|worker) node resources (CPU|Memory|Storage):.+/) {
                        $count++;
                    } elsif (/^Available (master|worker) node resources (CPU|Memory|Storage):.+/) {
                        $count++;
                    }
                }
            }
            if ($count > 0 && $info ne "") {
                General::Logging::log_user_message("$info");
                if ($rc == 2) {
                    General::Logging::log_user_warning_message("Data might be incomplete because 'describe pod' command failed before all data was printed");
                }
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the '$command' command" . join "\n", @std_err);
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the '$command' command" . join "\n", @std_err);
        }

        # Always return success
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
sub Calculate_Needed_Resources_P931S02 {

    my $rc = 0;

    if ($::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Calculate_Needed_Resources_Information_P931S02T01 } );
        return $rc if $rc < 0;
    } elsif ($::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES_MULTIPLE_FILES'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Calculate_Needed_Resources_Information_Multiple_Files_P931S02T02 } );
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
    sub Calculate_Needed_Resources_Information_P931S02T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/calculate_needed_umbrella_resources.pl";
        my $command_parameters = "--log-file=$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log";
        my $helm_executable = $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my $output_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement.txt";
        my @result = ();
        my @std_err;

        if ($::JOB_PARAMS{'P931_FILENAME_UMBRELLA'} ne "" && -f $::JOB_PARAMS{'P931_FILENAME_UMBRELLA'}) {
            $command_parameters .= " --file=$::JOB_PARAMS{'P931_FILENAME_UMBRELLA'}";
        } else {
            General::Logging::log_user_error_message("No valid umbrella file specified with job parameter 'P931_FILENAME_UMBRELLA'");
            return 1;
        }

        if ($::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} ne "") {
            $output_file = $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'};
        }

        $command_parameters .= " --helm-executable=$helm_executable";

        if ($::JOB_PARAMS{'P931_FILENAME_CHART'} ne "" && -f $::JOB_PARAMS{'P931_FILENAME_CHART'}) {
            $command_parameters .= " --value-file=$::JOB_PARAMS{'P931_FILENAME_CHART'}";
        } else {
            General::Logging::log_user_error_message("No valid chart file specified with job parameter 'P931_FILENAME_CHART'");
            return 1;
        }

        # Check if we have any extra eric-sc-values files to include
        for my $job_parameter (General::String_Operations::sort_hash_keys_alphanumerically(\%::JOB_PARAMS)) {
            if ($job_parameter =~ /^P931_FILENAME_CHART_\d+$/) {
                if ($::JOB_PARAMS{$job_parameter} ne "" && -f $::JOB_PARAMS{$job_parameter}) {
                    $command_parameters .= " --value-file=$::JOB_PARAMS{$job_parameter}";
                } elsif ($::JOB_PARAMS{$job_parameter} ne "") {
                    General::Logging::log_user_error_message("No valid chart file specified with job parameter '$job_parameter'");
                    return 1;
                } else {
                    # Ignore this case because no extra chart files are specified
                    next;
                }
            }
        }

        General::Logging::log_user_message("Calculating required resources.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command $command_parameters",
                "hide-output"   => 1,
                "save-to-file"  => $output_file,
                "return-output" => \@result,
                "return-stderr" => \@std_err,
            }
        );
        if ($rc == 0) {
            # Only print parts of the total printout
            my $info = "";
            my $found = 0;
            for (@result) {
                if (/^Required CPU Resources:\s*$/) {
                    $found = 1;
                    $info .= "$_\n";
                } elsif ($found == 1) {
                    $info .= "$_\n";
                }
            }
            General::Logging::log_user_message("$info");
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to calculate required resources" . join "\n", @std_err);
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
    sub Calculate_Needed_Resources_Information_Multiple_Files_P931S02T02 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/calculate_needed_umbrella_resources.pl";
        my $command_parameters = "--log-file=$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log";
        my $helm_executable = $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my $output_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement.txt";
        my @result = ();
        my @std_err;

        if ($::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} ne "") {
            $output_file = $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'};
        }

        $command_parameters .= " --helm-executable=$helm_executable";

        for my $job_parameter (General::String_Operations::sort_hash_keys_alphanumerically(\%::JOB_PARAMS)) {
            if ($job_parameter =~ /^P931_FILENAME_PAIR_\d+$/) {
                # Any checks for valid file names will be handled by the calculate_needed_umbrella_resources.pl script
                $command_parameters .= " --multiple-files=$::JOB_PARAMS{$job_parameter}";
            }
        }

        General::Logging::log_user_message("Calculating required resources.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command $command_parameters",
                "hide-output"   => 1,
                "save-to-file"  => $output_file,
                "return-output" => \@result,
                "return-stderr" => \@std_err,
            }
        );
        if ($rc == 0) {
            # Only print parts of the total printout
            my $info = "";
            my $found = 0;
            for (@result) {
                if (/^Required CPU Resources:\s*$/) {
                    $found = 1;
                    $info .= "$_\n";
                } elsif ($found == 1) {
                    $info .= "$_\n";
                }
            }
            General::Logging::log_user_message("$info");
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to calculate required resources" . join "\n", @std_err);
            return 1;
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
sub Check_Required_Resources_P931S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_Control_Plane_Nodes_Should_Be_Used_In_Calculation_P931S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Required_Resources_Information_P931S03T02 } );
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
    sub Check_If_Control_Plane_Nodes_Should_Be_Used_In_Calculation_P931S03T01 {

        my $rc = 0;

        General::Logging::log_user_message("Check if control-plane should not schedule any pods");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} describe node | grep -P '^Taints:\\s+node-role.kubernetes.io/(control-plane|master):NoSchedule'",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            # Taints found, so we shall not use control plane nodes in calculations
            $::JOB_PARAMS{'P931_USE_CONTROL_PLANE_NODES'} = "no";
            General::Logging::log_user_message("Control-plane nodes will NOT be used");
        } elsif ($rc == 1) {
            # No taints found, so we shall use control plane nodes in calculations
            $::JOB_PARAMS{'P931_USE_CONTROL_PLANE_NODES'} = "yes";
            General::Logging::log_user_message("Control-plane nodes will be used");
            $rc = 0;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check control-plane taints");
            $rc = 1;
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
    sub Check_Required_Resources_Information_P931S03T02 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/check_if_enough_resources_for_deployment.pl";
        my $command_parameters = "--safety-margin=1 --detailed-check";
        my $rc = 0;
        my $output_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_check.txt";
        my @result = ();
        my @std_err;

        if ($::JOB_PARAMS{'P931_USE_CONTROL_PLANE_NODES'} eq "yes") {
            $command_parameters .= " --include-master-resources";
        }

        if ($::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} ne "") {
            $command_parameters .= " --required-resources-file=$::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'}";
        } else {
            $command_parameters .= " --required-resources-file=$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement.txt";
        }

        if ($::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} ne "") {
            $command_parameters .= " --used-resources-file=$::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'}";
        } else {
            $command_parameters .= " --used-resources-file=$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage.txt";
        }

        if ($::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'} ne "") {
            $output_file = $::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'};
        }

        General::Logging::log_user_message("Check if there are enough resources available for the deployment.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command $command_parameters",
                "hide-output"   => 1,
                "save-to-file"  => $output_file,
                "return-output" => \@result,
                "return-stderr" => \@std_err,
            }
        );
        if ($rc == 0 || $rc == 2) {
            # Only print parts of the total printout
            my $info = "";
            for (@result) {
                if (/^(Total|Highest) available worker node resources (CPU|Memory|Storage):.+/) {
                    $info .= "$_\n";
                }
            }
            $info .= "\n" if ($info ne "");

            for (@result) {
                if (/^Result  Check performed\s*$/) {
                    $info .= "$_\n";
                } elsif (/^(OK|Not OK)\s+.+/) {
                    $info .= "$_\n";
                } elsif (/^------  -------+\s*+$/) {
                    $info .= "$_\n";
                }
            }
            General::Logging::log_user_message("$info");

            if ($rc == 2 && $::JOB_PARAMS{'IGNORE_FAILED_RESOURCE_CHECK'} eq "yes") {
                General::Logging::log_user_warning_message("Because Job parameter 'IGNORE_FAILED_RESOURCE_CHECK=yes' we ignore this error");
                $rc = 0;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check if enough resource are available" . join "\n", @std_err);
            return 1;
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
sub Compare_Resource_Usage_P931S04 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Compare_Resource_Usage_Information_P931S04T01 } );
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
    sub Compare_Resource_Usage_Information_P931S04T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/compare_resource_statistics.pl";
        my $command_parameters = "--namespace=$::JOB_PARAMS{'SC_NAMESPACE'}";
        my $output_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_differences.txt";
        my $rc;
        my @result;
        my @std_err;

        if ($::JOB_PARAMS{'P931_FILENAME_RESOURCE_DIFFERENCES'} ne "") {
            $output_file = $::JOB_PARAMS{'P931_FILENAME_RESOURCE_DIFFERENCES'};
        }

        $command_parameters .= " --resources-before=$::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_BEFORE'}";

        if ($::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'} ne "" && -f $::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'}) {
            $command_parameters .= " --resources-check=$::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'}";
        } else {
            General::Logging::log_user_error_message("No valid file specified with job parameter 'P931_FILENAME_RESOURCE_CHECK'");
            return 1;
        }

        if ($::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} ne "" && -f $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'}) {
            $command_parameters .= " --resources-required=$::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'}";
        } else {
            General::Logging::log_user_error_message("No valid file specified with job parameter 'P931_FILENAME_NEEDED_RESOURCES'");
            return 1;
        }

        $command_parameters .= " --resources-after=$::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_AFTER'}";

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command $command_parameters",
                "hide-output"   => 1,
                "save-to-file"  => $output_file,
                "return-output" => \@result,
                "return-stderr" => \@std_err,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the '$command' command" . join "\n", @std_err);
            # Always return success
            return 0;
        }

        # Only print last part of the total printout
        my $info = "";
        my $found = 0;
        for (@result) {
            if (/^Resource Information on (Master and Worker|Worker) Nodes Before and After Deployment:\s*$/) {
                $found = 1;
                $info .= "$_\n";
            } elsif ($found == 1) {
                $info .= "$_\n";
            }
        }
        General::Logging::log_user_message($info);

        # Always return success
        return 0;
    }
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
        'P931_CALCULATE_NEEDED_RESOURCES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if needed resources should be calculated from the
specified umbrella and HELM chart(s) files which MUST be specified by the
parameters 'P931_FILENAME_UMBRELLA' and 'P931_FILENAME_CHART' and optionally
'P931_FILENAME_CHART_1', 'P931_FILENAME_CHART_2' etc.
This parameter can be combined with parameters 'P931_COLLECT_RESOURCE_USAGE'
and/or 'P931_COMPARE_RESOURCE_USAGE'.

See also the parameter 'P931_FILENAME_NEEDED_RESOURCES' which controls where the
collected information is saved to.

If value is 'yes' when the playlist will calculate needed resources from the HELM
charts and if the value is 'no', which is also the default, then no calculation
of needed resources is done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_CHECK_REQUIRED_RESOURCES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if a check should be done of required resources against
available resources to see if a deployment would be possible.
It will compare data collected by 'P931_COLLECT_RESOURCE_USAGE' and
'P931_CALCULATE_NEEDED_RESOURCES'.
This parameter can be combined with parameters 'P931_CALCULATE_NEEDED_RESOURCES'
and/or 'P931_COMPARE_RESOURCE_USAGE'.

See also the parameter 'P931_FILENAME_RESOURCE_CHECK' which controls where the
collected information is saved to and the parameters 'P931_FILENAME_NEEDED_RESOURCES'
and 'P931_FILENAME_RESOURCE_USAGE' which points to input data for the check.

If value is 'yes' when the playlist will check available resources against the
needed resources and if the value is 'no', which is also the default, then no
check is done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_COLLECT_RESOURCE_USAGE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if resource usage should be collected from the cluster
and saved to a file for later use.
This parameter can be combined with parameters 'P931_CHECK_REQUIRED_RESOURCES'
and/or 'P931_COMPARE_RESOURCE_USAGE'.

See also the parameter 'P931_FILENAME_RESOURCE_USAGE' which controls where the
collected information is saved to.

If value is 'yes' when the playlist will collect resource usage from the cluster
and if the value is 'no', which is also the default, then no resource usage is
collected.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_COMPARE_RESOURCE_USAGE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if resource usage should be compared between what was
collected before and after e.g. a deployment and it presents how close the
estimated resource usage match with the actual usage.
This parameter can be combined with parameter 'P931_COLLECT_RESOURCE_USAGE'.

See also the parameter 'P931_FILENAME_RESOURCE_DIFFERENCES' which controls where
the collected information is saved to and the parameters
'P931_FILENAME_RESOURCE_USAGE_BEFORE', 'P931_FILENAME_RESOURCE_USAGE_AFTER',
'P931_FILENAME_NEEDED_RESOURCES' and 'P931_FILENAME_RESOURCE_CHECK' which
contains input data for the compare.

If value is 'yes' when the playlist will compare resource information before and
after e.g. a deployment and if the value is 'no', which is also the default,
then no compare is done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_CHART' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the file which contains the chart parameters to be used
when deploying the SC software (so called eric-sc-values.yaml file).
This file must be specified when 'P931_CALCULATE_NEEDED_RESOURCES=yes' is used.
If multiple chart parameter files should used in the calculation then they can
be specified with parameters 'P931_FILENAME_CHART_1', 'P931_FILENAME_CHART_2' etc.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_NEEDED_RESOURCES' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls in which file the calculated needed resource information
is written to and if specified it should contain the full path to the file.

If this parameter is not specified and also the parameter 'P931_CALCULATE_NEEDED_RESOURCES'
is set to 'yes' then the default value 'resource_requirement.txt' will be used and
it will be written to the directory pointed to by the job variable '_JOB_LOG_DIR'.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_RESOURCE_CHECK' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls in which file the results of the check if enough resources
are available for e.g. a deployment is written to and if specified it should
contain the full path to the file.

If this parameter is not specified and also the parameter 'P931_CHECK_REQUIRED_RESOURCES'
is set to 'yes' then the default value 'resource_check.txt' will be used and
it will be written to the directory pointed to by the job variable '_JOB_LOG_DIR'.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_RESOURCE_DIFFERENCES' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls in which file the results of the comparison of resource
usage before and after e.g. an deployment is written to and if specified it should
contain the full path to the file.

If this parameter is not specified and also the parameter 'P931_COMPARE_RESOURCE_USAGE'
is set to 'yes' then the default value 'resource_differences.txt' will be used and
it will be written to the directory pointed to by the job variable '_JOB_LOG_DIR'.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_RESOURCE_USAGE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls in which file the collected resource usage information
is written to in table format and if specified it should contain the full path to
the file.

If this parameter is not specified and also the parameter 'P931_COMPARE_RESOURCE_USAGE'
is set to 'yes' then the default value 'resource_usage.txt' will be used and
it will be written to the directory pointed to by the job variable '_JOB_LOG_DIR'.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_RESOURCE_USAGE_AFTER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the file which contains the collected resource usage
e.g. after a deployment and is used when comparing the before and after resource
usage with estimated resource usage together with the parameter
'P931_COMPARE_RESOURCE_USAGE'.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_RESOURCE_USAGE_BEFORE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the file which contains the collected resource usage
e.g. before a deployment and is used when comparing the before and after resource
usage with estimated resource usage together with the parameter
'P931_COMPARE_RESOURCE_USAGE'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_RESOURCE_USAGE_CSV' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls in which file the collected resource usage information
is written to in CSV format and if specified it should contain the full path to
the file.

If this parameter is not specified and also the parameter 'P931_COMPARE_RESOURCE_USAGE'
is set to 'yes' then the default value 'resource_usage.csv' will be used and
it will be written to the directory pointed to by the job variable '_JOB_LOG_DIR'.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P931_FILENAME_UMBRELLA' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the file which contains the compressed umbrella files
to be used when deploying the SC software (so called eric-sc-umbrella-*.t*gz file).
This file must be specified when 'P931_CALCULATE_NEEDED_RESOURCES=yes' is used.
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

This Playlist script is used for checking resource usage e.g. before and after
an Deployment.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.

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

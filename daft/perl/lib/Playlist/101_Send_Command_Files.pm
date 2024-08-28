package Playlist::101_Send_Command_Files;

################################################################################
#
#  Author   : eustone, everhel
#
#  Revision : 1.6
#  Date     : 2022-06-03 15:11:58
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2022
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

use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;
use General::Send_Command_Files;

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
#  1: The call failed
#  255: Stepout wanted
#
# -----------------------------------------------------------------------------
sub main {
    my $rc;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "SEND_COMMAND_FILES";

    # Check if network configuration file is specified
    if (General::Playlist_Operations::is_optional_network_config_loaded() != 0) {
        General::Logging::log_user_warning_message("Only command files executed on localhost will work");
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P101S01, \&Fallback001_P101S12 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P101S02, \&Fallback001_P101S12 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Send_Command_Files_P101S03, \&Fallback001_P101S12 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P101S04, \&Fallback001_P101S12 );
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
sub Initialize_Job_Environment_P101S01 {

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
sub Check_Job_Parameters_P101S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Find_Files_P101S02T01 } );
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
    sub Find_Files_P101S02T01 {

        my $filecnt = 0;
        my @files_to_load = ();
        my $message;
        my $rc = 0;

        # Show everything
        #General::Send_Command_Files::set_verbose_level(3);

        # Set flag to use the General::Logging.pm module
        General::Send_Command_Files::set_use_logging(1);

        # Find command files to load
        @files_to_load = General::Send_Command_Files::find_command_files(
            $::JOB_PARAMS{'FILE_DIRECTORY'},
            {
                "file-extension"                        => $::JOB_PARAMS{'FILE_EXTENSION'},
                "hide-error-messages"                   => $::JOB_PARAMS{'HIDE_ERROR_MESSAGES'} eq "yes" ? 1 : 0,
                "show-applied-filters"                  => $::JOB_PARAMS{'SHOW_APPLIED_FILTERS'} eq "yes" ? 1 : 0,
                "show-ignored-files"                    => $::JOB_PARAMS{'SHOW_IGNORED_FILES'} eq "yes" ? 1 : 0,
                "show-skipped-files"                    => $::JOB_PARAMS{'SHOW_SKIPPED_FILES'} eq "yes" ? 1 : 0,
                "show-valid-files"                      => $::JOB_PARAMS{'SHOW_VALID_FILES'} eq "yes" ? 1 : 0,
                "skip-files"                            => [ ],
            }
        );

        $filecnt = scalar @files_to_load;
        if ($filecnt > 0) {
            General::Logging::log_user_message(sprintf "%d file%s found", $filecnt, $filecnt > 1 ? "s" : "");
        } else {
            General::Logging::log_user_error_message("No files found");
            $rc = General::Playlist_Operations::RC_FALLBACK;
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
#
# -----------------------------------------------------------------------------
sub Send_Command_Files_P101S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_Files_P101S03T01 } );
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
    sub Load_Files_P101S03T01 {

        my %already_loaded_files;
        my @failed_files = ();
        my %failed_files;
        my $filecnt = 0;
        my $filecnt_success = 0;
        my $filename;
        my @files_to_load = ();
        my $message = "";
        my @remaining_files_to_load = ();
        my $rc = 0;
        my @skip_files = ();
        my @successful_files = ();
        my %successful_files;

        # Set flag to use the General::Logging.pm module
        General::Send_Command_Files::set_use_logging(1);

        # Read if any files has already been loaded and should be skipped and
        # clear JOB_PARAMS hash keeping track of already loaded and failed files
        for my $key (sort alphanum keys %::JOB_PARAMS) {
            if ($key =~ /^FILE_LOADING_SUCCESSFUL_(\d+)$/) {
                $filecnt_success = $1;
                # Skip file when searching for files to be loaded
                $filename = $::JOB_PARAMS{$key};
                push @skip_files, $filename;
                $already_loaded_files{$filename} = 1;
            } elsif ($key =~ /^FILE_LOADING_FAILED_\d+$/) {
                # Delete the key from the hash
                delete $::JOB_PARAMS{$key};
            }
        }

        # Find command files to load
        @files_to_load = General::Send_Command_Files::find_command_files(
            $::JOB_PARAMS{'FILE_DIRECTORY'},
            {
                "file-extension"                        => $::JOB_PARAMS{'FILE_EXTENSION'},
                "hide-error-messages"                   => $::JOB_PARAMS{'HIDE_ERROR_MESSAGES'} eq "yes" ? 1 : 0,
                "show-applied-filters"                  => $::JOB_PARAMS{'SHOW_APPLIED_FILTERS'} eq "yes" ? 1 : 0,
                "show-ignored-files"                    => $::JOB_PARAMS{'SHOW_IGNORED_FILES'} eq "yes" ? 1 : 0,
                "show-skipped-files"                    => $::JOB_PARAMS{'SHOW_SKIPPED_FILES'} eq "yes" ? 1 : 0,
                "show-valid-files"                      => $::JOB_PARAMS{'SHOW_VALID_FILES'} eq "yes" ? 1 : 0,
                "skip-files"                            => \@skip_files,
            }
        );

        $filecnt = scalar @files_to_load;
        if ($filecnt > 0) {
            General::Logging::log_user_message(sprintf "%d file%s to load", $filecnt, $filecnt > 1 ? "s" : "");
        } else {
            General::Logging::log_user_error_message("No files to load");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Write %::JOB_PARAMS hash to file to make sure we have access to an updated
        # version of the file for each execution of a playlist extension script.
        General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);

        # Set connection variables etc.
        General::Send_Command_Files::set_job_variables(\%::JOB_PARAMS);

        # Execute command files
        $rc = General::Send_Command_Files::execute_command_files(
            {
                "files"                                 => \@files_to_load,
                "hide-error-messages"                   => $::JOB_PARAMS{'HIDE_ERROR_MESSAGES'} eq "yes" ? 1 : 0,
                "indent-progress-messages"              => "  ",
                "just-print"                            => $::JOB_PARAMS{'JUST_PRINT'} eq "yes" ? 1 : 0,
                "return-failed-files"                   => \@failed_files,
                "return-successful-files"               => \@successful_files,
                "set-environment-variables"             => [
                    "JOB_WORKSPACE_PATH_DAFT=$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}",
                    "SOFTWARE_PATH_CLUSTER=/home/daft/software",
                    "SOFTWARE_PATH_DAFT=$::JOB_PARAMS{'SOFTWARE_DIR'}",
                    "TOOL_PATH_CLUSTER=/home/daft/tools",
                    "TOOL_PATH_DAFT=$::JOB_PARAMS{'_PACKAGE_DIR'}",
                ],
                "show-command-execution"                => $::JOB_PARAMS{'SHOW_COMMAND_EXECUTION'} eq "yes" ? 1 : 0,
                "show-progress"                         => $::JOB_PARAMS{'SHOW_PROGRESS'} eq "yes" ? 1 : 0,
                "stop-on-error"                         => $::JOB_PARAMS{'STOP_ON_ERROR'} eq "yes" ? 1 : 0,
            }
        );

        # Update hash with successful files
        for (@successful_files) {
            $already_loaded_files{$_} = 1;
            $successful_files{$_} = 1;
        }
        # Update hash with failed files
        for (@failed_files) {
            $failed_files{$_} = 1;
        }
        # Update job parameter and status hashes
        for (@files_to_load) {
            if (exists $successful_files{$_}) {
                push @::JOB_STATUS, "(/) Success: $_";
                $filecnt_success = sprintf "%03d", $filecnt_success += 1;
                $::JOB_PARAMS{"FILE_LOADING_SUCCESSFUL_$filecnt_success"} = $_;
            } elsif (exists $failed_files{$_}) {
                push @::JOB_STATUS, "(x) Failure: $_";
                $filecnt_success = sprintf "%03d", $filecnt_success += 1;
                $::JOB_PARAMS{"FILE_LOADING_FAILED_$filecnt_success"} = $_;
            } else {
                push @remaining_files_to_load, $_;
            }
        }

        if ($rc == 0) {
            General::Logging::log_user_message("Files loaded successfully");
        } else {
            $message = "Failed to load one or more files\n";
            if (@failed_files) {
                $message .= "Failed files:\n";
                for (@failed_files) {
                    $message .= "  $_\n";
                }
            }
            if (@remaining_files_to_load) {
                $message .= "Files that remains to be loaded:\n";
                for (@remaining_files_to_load) {
                    $message .= "  $_\n";
                }
            }
            General::Logging::log_user_error_message($message);
        }

        # Update the job_parameters.conf file on disk
        General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);

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
sub Cleanup_Job_Environment_P101S04 {

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
sub Fallback001_P101S12 {

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
# Sort alphanumeric, i.e. atmport=1, atmport=2, atmport=10
# instead of:             atmport=1, atmport=10, atmport=2
sub alphanum {
    my $a0 = $a;
    my $b0 = $b;
    for (;;)
    {
        my ($a1, $a2) = ($a0 =~ m/^([^\d]+)(.*)/);
        my ($b1, $b2) = ($b0 =~ m/^([^\d]+)(.*)/);
        $a1 = "" if (!defined $a1);
        $b1 = "" if (!defined $b1);
        $a2 = "" if (!defined $a2);
        $b2 = "" if (!defined $b2);
        $a2 = $a0 if (!$a1 and !$a2);
        $b2 = $b0 if (!$b1 and !$b2);
        if ($a1 ne $b1) { return ($a1 cmp $b1); }
        $a0 = $a2;
        $b0 = $b2;
        return 0 if (($a0 eq "") and ($b0 eq ""));

        ($a1, $a2) = ($a0 =~ m/^(\d+)(.*)/);
        ($b1, $b2) = ($b0 =~ m/^(\d+)(.*)/);
        $a1 = "" if (!defined $a1);
        $b1 = "" if (!defined $b1);
        $a2 = "" if (!defined $a2);
        $b2 = "" if (!defined $b2);
        $a2 = $a0 if (!$a1 and !$a2);
        $b2 = $b0 if (!$b1 and !$b2);
        if ($a1 != $b1) { return ($a1 <=> $b1); }
        $a0 = $a2;
        $b0 = $b2;
        return 0 if (($a0 eq "") and ($b0 eq ""));
    }
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'FILE_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds all the files that should be loaded.
Any file that match with the wanted FILE_EXTENSION will be loaded unless it
should be ignored based on some filters.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'FILE_EXTENSION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "apply",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select the file extension to use for finding files to load.

If not specified then the default value 'apply' is used as file extension.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'HIDE_ERROR_MESSAGES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'yes' then any error messages will not be printed to
the user.
The default is to alsways show error messages.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'JUST_PRINT' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'yes' then commands to be executed is just printed to
the screen instead of the default which is to execute the commands.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SHOW_APPLIED_FILTERS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'yes' then applied filters will be shown to the user.
The default is not to show them.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SHOW_COMMAND_EXECUTION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'yes' then executed commands and their output is shown
to the user.
The default is not to show it.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SHOW_IGNORED_FILES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'yes' then any files that is ignored and not loaded
will be shown to the user.
The default is not to show them.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SHOW_PROGRESS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'no' then no progress information will be shown to the
user. Progress information is e.g. files to be loaded.
The default is to show them.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SHOW_SKIPPED_FILES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'yes' then any files that is skipped due to e.g. not
matching file extension and not loaded will be shown to the user.
The default is not to show them.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SHOW_VALID_FILES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'yes' then files that was found as valid and will be
loaded will be shown to the user.
The default is not to show them.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'STOP_ON_ERROR' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and value is 'yes' then if a file or a command fails to load then
execution will be immediately stopped and no more files or commands will be
loaded.
The default is to continue to load all files and commands and then stop with an
error message.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist will take as input a directory containing command files that
should be executed on remote nodes and it will execute the commands specified
in the order that have been given by the sort order of the files.
The file names in the directory must follow a certain naming convention that
is used to determine on which interface to load the data.

    <node name>-<sequence number>[-<description>]_<interface>.config
       or to ignore errors for sent commands
    <node name>-<sequence number>[-<description>]_<interface>.config_ignore_errors

    Where:

    <node name>
    Is the name of the node, e.g. Eevee.

    <sequence number>
    Is a number that indicates in which order the files needs to be loaded
    e.g. 01.

    <description>
    Is a description of what the file is doing, e.g. SC-DT.
    This is an optional parameter.

    <interface>
    Is the interface where the file will be loaded and can be one of the
    following:

        DAFT
        localhost

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

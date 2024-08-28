package Playlist::909_Execute_Playlist_Extensions;

################################################################################
#
#  Author   : eustone
#
#  Revision : 2.3
#  Date     : 2022-08-03 10:49:37
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2019-2022
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

# Used Perl package files
use File::Basename qw(dirname basename);
use General::Logging;
use General::Playlist_Operations;
use General::Send_Command_Files;

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

    # If the playlist needs to have certain job parameter values for the execution
    # of the playlist then it needs to check if they have already been given by the
    # user when starting the job, otherwise the user should be prompted for their
    # value.
    if (General::Playlist_Operations::parse_playlist_variables(\%playlist_variables, \%::JOB_PARAMS) != 0) {
        General::Logging::log_user_error_message("Failed to parse the playlist variables");
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Send_Command_Files_P909S01, undef );
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
sub Send_Command_Files_P909S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_Files_P909S01T01 } );
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
    sub Load_Files_P909S01T01 {

        my %already_loaded_files;
        my @failed_files = ();
        my %failed_files;
        my $filecnt = 0;
        my $filecnt_success = 0;
        my $filename;
        my @files_to_load = ();
        my @job_variables_file_stats_after = ();
        my @job_variables_file_stats_before = ();
        my @local_job_status = ();
        my $message = "";
        my $playlist_extension_path = exists $::JOB_PARAMS{'PLAYLIST_EXTENSION_PATH'} ? $::JOB_PARAMS{'PLAYLIST_EXTENSION_PATH'} : "";
        my $playlist_extension_path_cluster = "";
        my @remaining_files_to_load = ();
        my $rc = 0;
        my @skip_files = ();
        my @successful_files = ();
        my %successful_files;

        if ($playlist_extension_path eq "") {
            General::Logging::log_user_error_message("No playlist extension path set");
            return 1;
        }

        if ($playlist_extension_path =~ /^.+(playlist_extensions\/.+)/) {
            $playlist_extension_path_cluster = "/home/daft/tools/$1";
        }

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
            $playlist_extension_path,
            {
                "file-extension"                => $::JOB_PARAMS{'FILE_EXTENSION'},
                "filter-callback"               => \&playlist_extension_filter,
                "filter-regex"                  => '^((APPLY|IGNORE)_(NEW|OLD)_RELEASE_(BUILD|VERSION)|(APPLY|IGNORE)_(NEW|OLD)_COMMERCIAL_RELEASE_NAME|JOBTYPE)=.+',
                "hide-error-messages"           => $::JOB_PARAMS{'P909_HIDE_ERROR_MESSAGES'} eq "yes" ? 1 : 0,
                "match-regex"                   => $::JOB_PARAMS{'P909_MATCH_REGEX'},
                "show-applied-filters"          => $::JOB_PARAMS{'P909_SHOW_APPLIED_FILTERS'} eq "yes" ? 1 : 0,
                "show-ignored-files"            => $::JOB_PARAMS{'P909_SHOW_IGNORED_FILES'} eq "yes" ? 1 : 0,
                "show-skipped-files"            => $::JOB_PARAMS{'P909_SHOW_SKIPPED_FILES'} eq "yes" ? 1 : 0,
                "show-valid-files"              => $::JOB_PARAMS{'P909_SHOW_VALID_FILES'} eq "yes" ? 1 : 0,
                "skip-files"                    => \@skip_files,
            }
        );

        $filecnt = scalar @files_to_load;
        if ($filecnt > 0) {
            General::Logging::log_user_message(sprintf "%d '$playlist_extension_path' file%s to load:", $filecnt, $filecnt > 1 ? "s" : "");
        } else {
            General::Logging::log_user_message("No '$playlist_extension_path' files to load");
            push @::JOB_STATUS, "(-) Execution of '$playlist_extension_path' skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Mark that BRF Backup restore is needed from this point on, in case a
        # fallback is invoked.
        $::JOB_PARAMS{'BACKUP_RESTORE_NEEDED'} = "yes";

        # Write %::JOB_PARAMS hash to file to make sure we have access to an updated
        # version of the file for each execution of a playlist extension script.
        General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);

        # Set connection variables etc.
        General::Send_Command_Files::set_job_variables(\%::JOB_PARAMS);

        # Get file statistics for the job parameters file before calling the playlist extensions
        @job_variables_file_stats_before = stat $::JOB_PARAMS{'_JOB_PARAMS_FILE'};

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "no") {
            # Execute command files
            $rc = General::Send_Command_Files::execute_command_files(
                {
                    "files"                                 => \@files_to_load,
                    "hide-error-messages"                   => $::JOB_PARAMS{'P909_HIDE_ERROR_MESSAGES'} eq "yes" ? 1 : 0,
                    "indent-progress-messages"              => "  ",
                    "just-print"                            => $::JOB_PARAMS{'P909_JUST_PRINT'} eq "yes" ? 1 : 0,
                    "return-failed-files"                   => \@failed_files,
                    "return-successful-files"               => \@successful_files,
                    "set-environment-variables"             => [
                        "JOB_WORKSPACE_PATH_DAFT=$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}",
                        "PLAYLIST_EXTENSION_PATH_CLUSTER=$playlist_extension_path_cluster",
                        "PLAYLIST_EXTENSION_PATH_DAFT=$playlist_extension_path",
                        "SOFTWARE_PATH_CLUSTER=/home/daft/software",
                        "SOFTWARE_PATH_DAFT=$::JOB_PARAMS{'SOFTWARE_DIR'}",
                        "TOOL_PATH_CLUSTER=/cluster/home/daft/tools",
                        "TOOL_PATH_DAFT=$::JOB_PARAMS{'_PACKAGE_DIR'}",
                    ],
                    "show-command-execution"                => $::JOB_PARAMS{'P909_SHOW_COMMAND_EXECUTION'} eq "yes" ? 1 : 0,
                    "show-progress"                         => $::JOB_PARAMS{'P909_SHOW_PROGRESS'} eq "yes" ? 1 : 0,
                    "stop-on-error"                         => $::JOB_PARAMS{'P909_STOP_ON_ERROR'} eq "yes" ? 1 : 0,
                }
            );
        } else {
            General::Logging::log_user_message("  Loading of files skipped because DEBUG_PLAYLIST=yes\n");
        }

        # Update hash with successful files
        for (@successful_files) {
            $already_loaded_files{$_} = 1;
            $successful_files{$_} = 1;
        }
        # Update hash with failed files
        for (@failed_files) {
            $failed_files{$_} = 1;
        }

        # Get file statistics for the job parameters file after calling the playlist extensions
        @job_variables_file_stats_after = stat $::JOB_PARAMS{'_JOB_PARAMS_FILE'};
        # Check if the file was modified by any of the playlist extensions
        if ($job_variables_file_stats_before[9] != $job_variables_file_stats_after[9]) {
            # The file was modified by at least one of the playlist extensions and
            # we need to read back the file again and update the %::JOB_PARAMS hash
            # with the new values. If any parameters were deleted then these parameters
            # will still remain, so we only update changed parameters and add new parameters.
            if (-f "$::JOB_PARAMS{'_JOB_PARAMS_FILE'}") {
                General::Playlist_Operations::job_parameters_read($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);
            }
        }

        # Update job parameter and status hashes
        for (@files_to_load) {
            $filename = basename($_);
            if (exists $successful_files{$_}) {
                push @local_job_status, "(/)   Success: $filename";
                $filecnt_success = sprintf "%03d", $filecnt_success += 1;
                $::JOB_PARAMS{"FILE_LOADING_SUCCESSFUL_$filecnt_success"} = $_;
            } elsif (exists $failed_files{$_}) {
                push @local_job_status, "(x)   Failure: $filename";
                $filecnt_success = sprintf "%03d", $filecnt_success += 1;
                $::JOB_PARAMS{"FILE_LOADING_FAILED_$filecnt_success"} = $_;
            } else {
                push @remaining_files_to_load, $_;
            }
        }

        if ($rc == 0) {
            General::Logging::log_user_message("Files loaded successfully");
            unshift @local_job_status, "(/) Execution of '$playlist_extension_path' successful";
            delete $::JOB_PARAMS{'PLAYLIST_EXTENSION_PATH'};
        } else {
            $message = "Failed to load one or more files\n";
            unshift @local_job_status, "(x) Execution of '$playlist_extension_path' failed";
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

        # Update the global JOB_STATUS array
        push @::JOB_STATUS, @local_job_status;

        # Update the job_parameters.conf file on disk
        General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);

        return $rc;
    }
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
# Check filter lines and return if file should be kept or removed.
#
# This checks filters found in the playlist_extension files that end with a
# .config if it contains any of the following lines:
#
#   'APPLY_NEW_RELEASE_BUILD=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'SC_RELEASE_BUILD'}" and if so the file
#   will be marked to be returned to the user. If the <pattern> does
#   not match then the file will be marked to not be returned to the
#   user.
#
#   'APPLY_NEW_RELEASE_VERSION=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'SC_RELEASE_VERSION'}" and if so the file
#   will be marked to be returned to the user. If the <pattern> does
#   not match then the file will be marked to not be returned to the
#   user.
#
#   'IGNORE_NEW_RELEASE_BUILD=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'SC_RELEASE_BUILD'}" and if so the file
#   will be marked to not be returned to the user. If the <pattern>
#   does not match then the file will be marked to be returned to the
#   user.
#
#   'IGNORE_NEW_RELEASE_VERSION=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'SC_RELEASE_VERSION'}" and if so the file
#   will be marked to not be returned to the user. If the <pattern>
#   does not match then the file will be marked to be returned to the
#   user.
#
#   'APPLY_OLD_RELEASE_BUILD=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}" and if so the file
#   will be marked to be returned to the user. If the <pattern> does not
#   match then the file will be marked to not be returned to the user.
#
#   'APPLY_OLD_RELEASE_VERSION=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}" and if so the file
#   will be marked to be returned to the user. If the <pattern> does not
#   match then the file will be marked to not be returned to the user.
#
#   'IGNORE_OLD_RELEASE_BUILD=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}" and if so the file
#   will be marked to not be returned to the user. If the <pattern>
#   does not match then the file will be marked to be returned to the
#   user.
#
#   'IGNORE_OLD_RELEASE_VERSION=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}" and if so the file
#   will be marked to not be returned to the user. If the <pattern>
#   does not match then the file will be marked to be returned to the
#   user.
#
#   'JOBTYPE=<pattern>'
#   Then a check is done if the <pattern> match with the value of the
#   "$::JOB_PARAMS{'JOBTYPE'}" and if so the file will be marked to be
#   returned to the user. If the <pattern> does not match then the file
#   will be marked to not be returned to the user.
#
#   If the different filters applied to a file result in both to be
#   returned and not returned then the file will not be returned to the
#   user.
#
sub playlist_extension_filter {
    my $filter_line = shift;

    my $filter;
    my $new_release_build = exists $::JOB_PARAMS{'SC_RELEASE_BUILD'} ? $::JOB_PARAMS{'SC_RELEASE_BUILD'} : "";
    my $new_release_version = exists $::JOB_PARAMS{'SC_RELEASE_VERSION'} ? $::JOB_PARAMS{'SC_RELEASE_VERSION'} : "";
    my $old_release_build = exists $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} ? $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} : "";
    my $old_release_version = exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ? $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} : "";

    if ($filter_line =~ /^APPLY_NEW_RELEASE_BUILD=(\S+)/) {
        $filter = $1;
        if ($new_release_build =~ /$filter/) {
            # Filter matched, keep file
            return "keep";
        } else {
            # Filter not matched, remove file
            return "remove";
        }
    } elsif ($filter_line =~ /^APPLY_NEW_RELEASE_VERSION=(\S+)/) {
        $filter = $1;
        if ($new_release_version =~ /$filter/) {
            # Filter matched, keep file
            return "keep";
        } else {
            # Filter not matched, remove file
            return "remove";
        }
    } elsif ($filter_line =~ /^APPLY_OLD_RELEASE_BUILD=(\S+)/) {
        $filter = $1;
        if ($old_release_build =~ /$filter/) {
            # Filter matched, keep file
            return "keep";
        } else {
            # Filter not matched, remove file
            return "remove";
        }
    } elsif ($filter_line =~ /^APPLY_OLD_RELEASE_VERSION=(\S+)/) {
        $filter = $1;
        if ($old_release_version =~ /$filter/) {
            # Filter matched, keep file
            return "keep";
        } else {
            # Filter not matched, remove file
            return "remove";
        }
    } elsif ($filter_line =~ /^IGNORE_NEW_RELEASE_BUILD=(\S+)/) {
        $filter = $1;
        if ($new_release_build =~ /$filter/) {
            # Filter matched, remove file
            return "remove";
        } else {
            # Filter not matched, keep file
            return "keep";
        }
    } elsif ($filter_line =~ /^IGNORE_NEW_RELEASE_VERSION=(\S+)/) {
        $filter = $1;
        if ($new_release_version =~ /$filter/) {
            # Filter matched, remove file
            return "remove";
        } else {
            # Filter not matched, keep file
            return "keep";
        }
    } elsif ($filter_line =~ /^IGNORE_OLD_RELEASE_BUILD=(\S+)/) {
        $filter = $1;
        if ($old_release_build =~ /$filter/) {
            # Filter matched, remove file
            return "remove";
        } else {
            # Filter not matched, keep file
            return "keep";
        }
    } elsif ($filter_line =~ /^IGNORE_OLD_RELEASE_VERSION=(\S+)/) {
        $filter = $1;
        if ($old_release_version =~ /$filter/) {
            # Filter matched, remove file
            return "remove";
        } else {
            # Filter not matched, keep file
            return "keep";
        }
    } elsif ($filter_line =~ /^JOBTYPE=(\S+)/) {
        $filter = $1;
        if ($::JOB_PARAMS{'JOBTYPE'} =~ /$filter/) {
            # Filter matched, keep file
            return "keep";
        } else {
            # Filter not matched, remove file
            return "remove";
        }
    } else {
        return "ignore";
    }
}


# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'P909_HIDE_ERROR_MESSAGES' => {
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
        'P909_JUST_PRINT' => {
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
        'P909_MATCH_REGEX' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and not empty then the value will be used as a Perl regular
expression to filter out files so only matching files will be loaded.
This might be useful to restrict loading to only one file or a number of files
and not all files that would otherwise match from the specified directory.
If not specified or if empty then all files will match, this is also the default
behavior.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P909_SHOW_APPLIED_FILTERS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
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
        'P909_SHOW_COMMAND_EXECUTION' => {
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
        'P909_SHOW_IGNORED_FILES' => {
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
        'P909_SHOW_PROGRESS' => {
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
        'P909_SHOW_SKIPPED_FILES' => {
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
        'P909_SHOW_VALID_FILES' => {
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
        'P909_STOP_ON_ERROR' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
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

This Playlist script is used to execute command/script files from the
playlist_extensions directory.

This playlist can be called from the main Deploy and Upgrade playlists.

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

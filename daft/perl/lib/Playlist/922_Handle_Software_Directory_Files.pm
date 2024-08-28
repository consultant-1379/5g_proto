package Playlist::922_Handle_Software_Directory_Files;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.33
#  Date     : 2024-06-11 18:41:27
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
use version;

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    );

#
# Used Perl package files
#

use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::Playlist_Operations;

#
# Variable Declarations
#

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

    # Initialize playlist specific variables if not existing
    $::JOB_PARAMS{'CHECK_CSAR_FILE'}                = "yes" unless exists $::JOB_PARAMS{'CHECK_CSAR_FILE'};
    $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}        = "yes" unless exists $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'};
    $::JOB_PARAMS{'CHECK_TOOLS_FILE'}               = "yes" unless exists $::JOB_PARAMS{'CHECK_TOOLS_FILE'};
    $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}       = "yes" unless exists $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'};
    $::JOB_PARAMS{'CHECK_VALUES_FILE'}              = "yes" unless exists $::JOB_PARAMS{'CHECK_VALUES_FILE'};
    $::JOB_PARAMS{'COPY_UMBRELLA_FILE'}             = "yes" unless exists $::JOB_PARAMS{'COPY_UMBRELLA_FILE'};
    $::JOB_PARAMS{'COPY_VALUES_FILE'}               = "yes" unless exists $::JOB_PARAMS{'COPY_VALUES_FILE'};
    $::JOB_PARAMS{'UNPACK_ALL_UMBRELLA_TGZ_FILES'}  = "yes" unless exists $::JOB_PARAMS{'UNPACK_ALL_UMBRELLA_TGZ_FILES'};
    $::JOB_PARAMS{'UNPACK_CSAR_FILE'}               = "yes" unless exists $::JOB_PARAMS{'UNPACK_CSAR_FILE'};
    $::JOB_PARAMS{'UNPACK_TOOLS_FILE'}              = "yes" unless exists $::JOB_PARAMS{'UNPACK_TOOLS_FILE'};
    $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}           = "yes" unless exists $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'};

    if ($::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'} eq "yes") {
        # This also assumes that the following is set
        $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'} = "yes";
    }

    General::Logging::log_user_message(
        "Playlist valled with the following parameters:\n" .
        "CHECK_CSAR_FILE=$::JOB_PARAMS{'CHECK_CSAR_FILE'}\n" .
        "CHECK_CSAR_FILE_CONTENT=$::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}\n" .
        "CHECK_TOOLS_FILE=$::JOB_PARAMS{'CHECK_TOOLS_FILE'}\n" .
        "CHECK_TOOLS_FILE_CONTENT=$::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}\n" .
        "CHECK_VALUES_FILE=$::JOB_PARAMS{'CHECK_VALUES_FILE'}\n" .
        "COPY_UMBRELLA_FILE=$::JOB_PARAMS{'COPY_UMBRELLA_FILE'}\n" .
        "COPY_VALUES_FILE=$::JOB_PARAMS{'COPY_VALUES_FILE'}\n" .
        "UNPACK_ALL_UMBRELLA_TGZ_FILES=$::JOB_PARAMS{'UNPACK_ALL_UMBRELLA_TGZ_FILES'}\n" .
        "UNPACK_CSAR_FILE=$::JOB_PARAMS{'UNPACK_CSAR_FILE'}\n" .
        "UNPACK_TOOLS_FILE=$::JOB_PARAMS{'UNPACK_TOOLS_FILE'}\n" .
        "UNPACK_UMBRELLA_FILE=$::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}\n"
    );

    if (exists $::JOB_PARAMS{'SOFTWARE_DIR'} && $::JOB_PARAMS{'SOFTWARE_DIR'} ne "") {
        $rc = General::Playlist_Operations::execute_step( \&Check_Software_Files_P922S01, \&Fallback001_P922S99 );
        return $rc if $rc < 0;

        if ($::JOB_PARAMS{'UNPACK_CSAR_FILE'} eq "yes" || $::JOB_PARAMS{'UNPACK_TOOLS_FILE'} eq "yes") {
            $rc = General::Playlist_Operations::execute_step( \&Unpack_Software_Files_P922S02, \&Fallback001_P922S99 );
            return $rc if $rc < 0;

            if ($::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'} eq "yes" || $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'} eq "yes") {
                $rc = General::Playlist_Operations::execute_step( \&Check_Software_Files_Content_P922S03, \&Fallback001_P922S99 );
                return $rc if $rc < 0;
            }
        } elsif ($::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'} eq "yes") {
            $rc = General::Playlist_Operations::execute_step( \&Unpack_Umbrella_Files_P922S04, \&Fallback001_P922S99 );
            return $rc if $rc < 0;
        } elsif ($::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'} eq "yes" || $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'} eq "yes") {
            $rc = General::Playlist_Operations::execute_step( \&Check_Software_Files_Content_P922S03, \&Fallback001_P922S99 );
            return $rc if $rc < 0;
        }
    } else {
        # No SOFTWARE_DIR job variable set, skip this playlist
        $rc = General::Playlist_Operations::RC_TASKOUT;
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
sub Check_Software_Files_P922S01 {

    my $rc = 0;

    if ($::JOB_PARAMS{'CHECK_CSAR_FILE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CSAR_File_P922S01T01 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CHECK_TOOLS_FILE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Tools_File_P922S01T02 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CHECK_VALUES_FILE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Values_File_P922S01T03 } );
        return $rc if $rc < 0;

        if ($::JOB_PARAMS{'COPY_VALUES_FILE'} eq "yes") {
            # This task only make sense if the files has been checked
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Values_Files_To_Config_Directory_P922S01T04 } );
            return $rc if $rc < 0;
        }
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
    sub Check_CSAR_File_P922S01T01 {

        my $dirname = $::JOB_PARAMS{'SOFTWARE_DIR'};
        my $filename = "";
        my $message;
        my $rc = 0;
        my @result = ();
        # I only map up to number 25 since that it probably the highest 1.x.25
        # number we will use. But the official Ericsson version handling reads
        # something like this:
        # R-State has format R\d{1,4}[A-Z]{1,4}
        # Maximum length is 7 characters
        # Letters I, O, P, Q, R and W are forbidden
        # If 3 or 4 letters long, vowels (A, E, U, Y) are forbidden in letter position 2 and 3
        #
        my %rstate_to_number = (
            "A" => 0,
            "B" => 1,
            "C" => 2,
            "D" => 3,
            "E" => 4,
            "F" => 5,
            "G" => 6,
            "H" => 7,
            "J" => 8,
            "K" => 9,
            "L" => 10,
            "M" => 11,
            "N" => 12,
            "S" => 13,
            "T" => 14,
            "U" => 15,
            "V" => 16,
            "X" => 17,
            "Y" => 18,
            "Z" => 19,
            "AA" => 20,
            "AB" => 21,
            "AC" => 22,
            "AD" => 23,
            "AE" => 24,
            "AF" => 25
        );

        # Initialize SOFTWARE path
        $::JOB_PARAMS{'SC_RELEASE_BUILD'} = "";
        $::JOB_PARAMS{'SC_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'SOFTWARE_DIR_NAME'} = "";
        $::JOB_PARAMS{'SOFTWARE_FILE_NAME'} = "";

        # Checking for CSAR software file used for deployment or upgrade
        $filename = "*.csar";
        General::Logging::log_user_message("Checking for file '$filename'\n");
        @result = General::File_Operations::find_file(
            {
                "filename"  => $filename,
                "directory" => $dirname,
                "maxdepth"  => 2,
            }
        );
        if (scalar @result == 1) {
            if ($result[0] =~ /^(.+)\/(.+-(\d+\.\d+\.\d+)(.+)\.csar)/) {
                # For example: eric-sc-1.14.0+74.csar
                $::JOB_PARAMS{'SOFTWARE_DIR_NAME'} = $1;
                $::JOB_PARAMS{'SOFTWARE_FILE_NAME'} = $2;
                $::JOB_PARAMS{'SC_RELEASE_VERSION'} = $3;
                $::JOB_PARAMS{'SC_RELEASE_BUILD'} = $4;
                if ($::JOB_PARAMS{'SC_RELEASE_BUILD'} =~ /\.unsigned/) {
                    $::JOB_PARAMS{'SC_RELEASE_BUILD'} =~ s/\.unsigned//;
                }
                # Set intial value of APPLICATION_TYPE based on the file name just in case we don't
                # unpack the CSAR file and check the value below.
                if ($result[0] =~ /eric-sc-\d+\.\d+\.\d+/) {
                    # eric-sc-1.13.25+1169.csar
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
                } elsif ($result[0] =~ /eric-dsc-\d+\.\d+\.\d+/) {
                    # eric-dsc-1.11.1+46.csar
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "dsc";
                } else {
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "unknown";
                }
                $rc = 0;
            } elsif ($result[0] =~ /^(.+)\/(ERIC-SC-CXP_\d+_\d+_(\d+)_R(\d{1,4})([A-HJ-NS-VX-Z]{1,4})_(\S+)\.csar)/) {
                # New CNCS package starting with SC 1.15.
                # ERIC-SC-CXP_903_8365_1_R16Z_313.unsigned.csar
                #  or
                # ERIC-SC-CXP_903_8365_1_R16Z_313.csar
                # Both corresponding to SC 1.15.25+313
                # _1_ => 1 (as in 1.x.x+x
                # R16 => 15 (as in x.15.x+x
                # AF   => 25 (as in x.x.25.x, A=0, B=1, .. Y=18, Z=19, AA=20, .. AF=25)
                $::JOB_PARAMS{'SOFTWARE_DIR_NAME'} = $1;
                $::JOB_PARAMS{'SOFTWARE_FILE_NAME'} = $2;
                if (exists $rstate_to_number{$5}) {
                    $::JOB_PARAMS{'SC_RELEASE_VERSION'} = sprintf "%i.%i.%i", $3, $4-1, $rstate_to_number{$5};
                } else {
                    # We just fake the last part
                    $::JOB_PARAMS{'SC_RELEASE_VERSION'} = sprintf "%i.%i.%i", $3, $4-1, 999;
                }
                $::JOB_PARAMS{'SC_RELEASE_BUILD'} = "+$6";
                if ($::JOB_PARAMS{'SC_RELEASE_BUILD'} =~ /\.unsigned/) {
                    $::JOB_PARAMS{'SC_RELEASE_BUILD'} =~ s/\.unsigned//;
                }
                # Set intial value of APPLICATION_TYPE based on the file name just in case we don't
                # unpack the CSAR file and check the value below.
                $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
                $rc = 0;
            } else {
                General::Message_Generation::print_message_box(
                    {
                        "messages"      => [
                            "",
                            "Didn't find the expected file '*.csar' in the file list:",
                            "$result[0]",
                            "",
                            "It can also be because the Rxxyy version is not following the correct format.",
                            "",
                        ],
                        "align-text"    => "center",
                        "max-length"    => 0,
                        "type"          => "error",
                        "return-output" => \$message,
                    }
                );
                General::Logging::log_user_error_message($message);
                $rc = General::Playlist_Operations::RC_FALLBACK;
            }
        } elsif (scalar @result == 0) {
            General::Message_Generation::print_message_box(
                {
                    "messages"      => [
                        "",
                        "Specified Software Directory does not contain any '*.csar' file:",
                        "$dirname",
                        "",
                    ],
                    "align-text"    => "center",
                    "max-length"    => 0,
                    "type"          => "error",
                    "return-output" => \$message,
                }
            );
            General::Logging::log_user_error_message($message);
            $rc = General::Playlist_Operations::RC_FALLBACK;
        } else {
            General::Message_Generation::print_message_box(
                {
                    "messages"      => [
                        "",
                        "There should be only one '*.csar' file in directory:",
                        "$dirname",
                        "",
                    ],
                    "align-text"    => "center",
                    "max-length"    => 0,
                    "type"          => "error",
                    "return-output" => \$message,
                }
            );
            General::Logging::log_user_error_message($message);
            $rc = General::Playlist_Operations::RC_FALLBACK;
        }

        # return with error if no file found
        return $rc if $rc != 0;

        #
        # Check the checksum of the file
        #

        $filename = "$::JOB_PARAMS{'SOFTWARE_DIR_NAME'}/$::JOB_PARAMS{'SOFTWARE_FILE_NAME'}";
        $rc = checksum_file( $filename );

        if ($rc == 0) {
            General::Logging::log_user_message("Software Package Version:\n  APPLICATION_TYPE=$::JOB_PARAMS{'APPLICATION_TYPE'}\n  SC_RELEASE_VERSION=$::JOB_PARAMS{'SC_RELEASE_VERSION'}\n  SC_RELEASE_BUILD=$::JOB_PARAMS{'SC_RELEASE_BUILD'}\n");
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
    sub Check_Tools_File_P922S01T02 {

        my $dirname = $::JOB_PARAMS{'SOFTWARE_DIR'};
        my $filename = "";
        my $message;
        my $rc = 0;
        my @result = ();

        # Initialize SOFTWARE path
        $::JOB_PARAMS{'TOOLS_DIR_NAME'} = "";
        $::JOB_PARAMS{'TOOLS_FILE_NAME'} = "";

        # Checking for tools file used for deployment and upgrade
        $filename = "eric-*-tools*.t*gz";
        General::Logging::log_user_message("Checking for file '$filename'\n");
        @result = General::File_Operations::find_file(
            {
                "filename"  => $filename,
                "directory" => $dirname,
                "maxdepth"  => 2,
            }
        );
        if (scalar @result == 1) {
            if ($result[0] =~ /^(.+)\/(.+\.tgz)/) {
                $::JOB_PARAMS{'TOOLS_DIR_NAME'} = $1;
                $::JOB_PARAMS{'TOOLS_FILE_NAME'} = $2;
                $rc = 0;
            } else {
                General::Message_Generation::print_message_box(
                    {
                        "messages"      => [
                            "",
                            "Didn't find the expected file '*.tgz' in the file list:",
                            "$result[0]",
                            "",
                        ],
                        "align-text"    => "center",
                        "type"          => "error",
                        "return-output" => \$message,
                    }
                );
                General::Logging::log_user_error_message($message);
                $rc = General::Playlist_Operations::RC_FALLBACK;
            }
        } elsif (scalar @result == 0) {
            General::Message_Generation::print_message_box(
                {
                    "messages"      => [
                        "",
                        "Specified Software Directory does not contain any tools '*.tgz' file:",
                        "$dirname",
                        "",
                    ],
                    "align-text"    => "center",
                    "type"          => "error",
                    "return-output" => \$message,
                }
            );
            General::Logging::log_user_error_message($message);
            $rc = General::Playlist_Operations::RC_FALLBACK;
        } else {
            General::Message_Generation::print_message_box(
                {
                    "messages"      => [
                        "",
                        "There should be only one tools '*.tgz' file in directory:",
                        "$dirname",
                        "",
                    ],
                    "align-text"    => "center",
                    "type"          => "error",
                    "return-output" => \$message,
                }
            );
            General::Logging::log_user_error_message($message);
            $rc = General::Playlist_Operations::RC_FALLBACK;
        }

        # return with error if no file found
        return $rc if $rc != 0;

        #
        # Check the checksum of the file
        #

        $filename = "$::JOB_PARAMS{'TOOLS_DIR_NAME'}/$::JOB_PARAMS{'TOOLS_FILE_NAME'}";
        $rc = checksum_file( $filename );

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
    sub Check_Values_File_P922S01T03 {

        my $dirname = $::JOB_PARAMS{'SOFTWARE_DIR'};
        my $filename = "";
        my $message;
        my $rc = 0;
        my @result = ();

        # Initialize variables for keeping values file paths
        $::JOB_PARAMS{'VALUES_FILE_NAME'} = "";
        $::JOB_PARAMS{'UCC_VALUES_FILE_NAME'} = "";

        # Checking for tools file used for deployment
        $filename = "eric-*-values*.yaml";
        General::Logging::log_user_message("Checking for file '$filename'\n");
        @result = General::File_Operations::find_file(
            {
                "filename"  => $filename,
                "directory" => $dirname,
                "maxdepth"  => 2,
            }
        );
        if (scalar @result > 0) {
            for (@result) {
                if (/^.+\/eric-(sc|dsc-exposed|dsc)-values.*\.yaml$/) {
                    # E.g. eric-sc-values-1.7.25+293.yaml, eric-dsc-values.yaml or eric-dsc-exposed-values.yaml
                    # NOTE: We only support one file in the directory.
                    if ($::JOB_PARAMS{'VALUES_FILE_NAME'} eq "") {
                        $::JOB_PARAMS{'VALUES_FILE_NAME'} = "$_";
                    } else {
                        General::Logging::log_user_error_message("Multiple 'eric-*-values*.yaml' files found. Unknown which file to use, please check software package");
                        return General::Playlist_Operations::RC_FALLBACK;
                    }
                } elsif (/^.+\/eric-sc-ucc-values.*\.yaml$/) {
                    # E.g. eric-sc-ucc-values-1.15.25+978.yaml
                    # NOTE: We only support one file in the directory.
                    if ($::JOB_PARAMS{'UCC_VALUES_FILE_NAME'} eq "") {
                        $::JOB_PARAMS{'UCC_VALUES_FILE_NAME'} = "$_";
                    } else {
                        General::Logging::log_user_error_message("Multiple 'eric-sc-ucc-values*.yaml' files found. Unknown which file to use, please check software package");
                        return General::Playlist_Operations::RC_FALLBACK;
                    }
                }
            }

            # Check that all expected files were found
            if ($::JOB_PARAMS{'VALUES_FILE_NAME'} eq "") {
                General::Logging::log_user_error_message("Did not find the expected 'eric-sc-values-*.yaml' or 'eric-dsc-exposed-values.yaml' files");
                $rc = General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Message_Generation::print_message_box(
                {
                    "messages"      => [
                        "",
                        "Specified Software Directory does not contain any values '$filename' files:",
                        "$dirname",
                        "",
                    ],
                    "align-text"    => "center",
                    "type"          => "error",
                    "return-output" => \$message,
                }
            );
            General::Logging::log_user_error_message($message);
            $rc = General::Playlist_Operations::RC_FALLBACK;
        }

        # return with error if no file found
        return $rc if $rc != 0;

        #
        # Check the checksum of the file(s)
        #

        my @files_to_check = split /\|/, $::JOB_PARAMS{'VALUES_FILE_NAME'};
        my $checksum_failures = 0;

        for $filename (@files_to_check) {
            $rc = checksum_file( $filename );
            if ($rc != 0) {
                $checksum_failures++;
            }
        }

        @files_to_check = split /\|/, $::JOB_PARAMS{'UCC_VALUES_FILE_NAME'};

        for $filename (@files_to_check) {
            $rc = checksum_file( $filename );
            if ($rc != 0) {
                $checksum_failures++;
            }
        }

        if ($checksum_failures == 0) {
            return 0;
        } else {
            return General::Playlist_Operations::RC_FALLBACK;
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
    sub Copy_Values_Files_To_Config_Directory_P922S01T04 {

        my $config_dir = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";
        my $failures = 0;
        my @files_to_check = split /\|/, $::JOB_PARAMS{'VALUES_FILE_NAME'};
        my $rc = 0;
        my @result = ();

        General::Logging::log_user_message("Copy values file(s) to workspace directory for safe keeping:");
        for my $filename (@files_to_check) {
            General::Logging::log_user_message("  $filename");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -pf $filename $config_dir",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("  Failed to copy the values file");
                $failures++;
            }
        }

        if ($failures == 0) {
            return 0;
        } else {
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
sub Unpack_Software_Files_P922S02 {

    my $rc = 0;

    if ($::JOB_PARAMS{'CHECK_CSAR_FILE'} eq "yes" && $::JOB_PARAMS{'UNPACK_CSAR_FILE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_CSAR_File_P922S02T01 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CHECK_TOOLS_FILE'} eq "yes" && $::JOB_PARAMS{'UNPACK_TOOLS_FILE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_Tools_File_P922S02T02 } );
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
    sub Unpack_CSAR_File_P922S02T01 {

        my $rc = 0;
        my $software_file = "$::JOB_PARAMS{'SOFTWARE_DIR_NAME'}/$::JOB_PARAMS{'SOFTWARE_FILE_NAME'}";

        General::Logging::log_user_message("Unpacking software file.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "unzip -o $software_file -d $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to unpack the software file");
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
    sub Unpack_Tools_File_P922S02T02 {

        my $rc = 0;
        my $tools_file = "$::JOB_PARAMS{'TOOLS_DIR_NAME'}/$::JOB_PARAMS{'TOOLS_FILE_NAME'}";

        General::Logging::log_user_message("Unpacking tools file");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "tar xvf $tools_file -C $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to unpack the tools file");
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
sub Check_Software_Files_Content_P922S03 {

    my $rc = 0;

    if ($::JOB_PARAMS{'CHECK_CSAR_FILE'} eq "yes" && $::JOB_PARAMS{'UNPACK_CSAR_FILE'} eq "yes" && $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Unpacked_CSAR_File_Content_P922S03T01 } );
        return $rc if $rc < 0;

        if ($::JOB_PARAMS{'COPY_UMBRELLA_FILE'} eq "yes") {
            # This task only make sense if the files has been unpacked and checked
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Umbrella_Files_To_Config_Directory_P922S03T02 } );
            return $rc if $rc < 0;
        }

        if ($::JOB_PARAMS{'COPY_VALUES_FILE'} eq "yes") {
            # This task only make sense if the files has been checked
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Internal_Values_Files_To_Config_Directory_P922S01T03 } );
            return $rc if $rc < 0;
        }

        if ($::JOB_PARAMS{'UNPACK_ALL_UMBRELLA_TGZ_FILES'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_All_Umbrella_TGZ_Files_To_Software_Directory_P922S03T04 } );
            return $rc if $rc < 0;
        }
    } elsif ($::JOB_PARAMS{'CHECK_CSAR_FILE'} eq "yes" && $::JOB_PARAMS{'UNPACK_CSAR_FILE'} eq "no" && $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Packed_CSAR_File_Content_P922S03T05 } );
        return $rc if $rc < 0;

        if ($::JOB_PARAMS{'COPY_UMBRELLA_FILE'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Packed_Umbrella_Files_From_CSAR_To_Config_Directory_P922S03T06 } );
            return $rc if $rc < 0;
        }

        if ($::JOB_PARAMS{'COPY_VALUES_FILE'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Packed_Internal_Values_Files_To_Config_Directory_P922S01T07 } );
            return $rc if $rc < 0;
        }

        if ($::JOB_PARAMS{'UNPACK_ALL_UMBRELLA_TGZ_FILES'} eq "yes" && $::JOB_PARAMS{'COPY_UMBRELLA_FILE'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_All_Config_Directory_Umbrella_TGZ_Files_To_Software_Directory_P922S03T08 } );
            return $rc if $rc < 0;
        }
    }

    if ($::JOB_PARAMS{'CHECK_TOOLS_FILE'} eq "yes" && $::JOB_PARAMS{'UNPACK_TOOLS_FILE'} eq "yes" && $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Tools_File_Content_P922S03T09 } );
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
    sub Check_Unpacked_CSAR_File_Content_P922S03T01 {

        my $dir = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software";
        my $rc = 0;
        my @result = ();

        General::Logging::log_user_message("Check that expected files are included in the software file");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ls -1 --color=never $dir/Definitions/OtherTemplates/eric-* $dir/Files/images/docker.tar $dir/Files/images.txt",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Not all expected files was found in the software file");
            return 1;
        }

        return check_csar_file_content(\@result);
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
    sub Copy_Umbrella_Files_To_Config_Directory_P922S03T02 {

        return copy_umbrella_files();

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
    sub Copy_Internal_Values_Files_To_Config_Directory_P922S01T03 {

        my $config_dir = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";
        my $failures = 0;
        my @files_to_check = ();
        my $rc = 0;
        my @result = ();

        push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} ne "");
        push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} ne "");
        push @files_to_check, $::JOB_PARAMS{'SC_CS_VALUES_FILE'} if ($::JOB_PARAMS{'SC_CS_VALUES_FILE'} ne "");

        General::Logging::log_user_message("Copy internal values file(s) to workspace directory for safe keeping:");
        for my $filename (@files_to_check) {
            General::Logging::log_user_message("  $filename");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -pf $filename $config_dir",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("  Failed to copy the values file");
                $failures++;
            }
        }

        if ($failures == 0) {
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
    sub Unpack_All_Umbrella_TGZ_Files_To_Software_Directory_P922S03T04 {

        return extract_all_umbrella_tgz_files();
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
    sub Check_Packed_CSAR_File_Content_P922S03T05 {

        my $docker_file_exist = 0;
        my $helm_file_exist = 0;
        my $images_file_exist = 0;
        my $rc = 0;
        my @result;
        my $software_file = "$::JOB_PARAMS{'SOFTWARE_DIR_NAME'}/$::JOB_PARAMS{'SOFTWARE_FILE_NAME'}";

        General::Logging::log_user_message("Check that expected files are included in the software file");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "unzip -Z1 $software_file",  # -Z1 means use zipinfo to just list the file names without any extra size etc. information
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Not able to read contents of the software file");
            return 1;
        }

        # Now we need to check the contents and prepend the path of the CSAR file.
        # What will be later stored in variables when calling XXXXXXXXXXXXX subroutine will of course
        # not contain valid file paths but we do this on purpose to just show where the files would
        # be found in the CSAR file.
        for (@result) {
            if (/Definitions\/OtherTemplates\/eric-.*/) {
                $_ = "$software_file:" . $_;
                $helm_file_exist++;
            } elsif (/Files\/images\/docker\.tar/) {
                $_ = "$software_file:" . $_;
                $docker_file_exist++;
            } elsif (/Files\/images\.txt/) {
                $_ = "$software_file:" . $_;
                $images_file_exist++;
            }
        }
        if ($docker_file_exist == 0 || $helm_file_exist == 0 || $images_file_exist == 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Not all expected files was found in the software file");
            return 1;
        }

        return check_csar_file_content(\@result);
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
    sub Copy_Packed_Umbrella_Files_From_CSAR_To_Config_Directory_P922S03T06 {

        my $config_dir = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";
        my $copy_file;
        my $failures = 0;
        my @files_to_check = split /\|/, $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'};
        my $rc = 0;
        my @result = ();
        my $zip_file;

        push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} ne "");
        push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} ne "");
        push @files_to_check, $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} if ($::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "");

        General::Logging::log_user_message("Copy umbrella file(s) to workspace directory for safe keeping:");
        for my $filename (@files_to_check) {
            General::Logging::log_user_message("  $filename");
            if ($filename =~ /^(.+):(.+)/) {
                # E.g. /path/to/filename.csar:Definitions/OtherTemplates/eric-sc-sepp-1.1.0-3-hd3fb128.tgz
                $zip_file = $1;
                $copy_file = $2;
            } else {
                # Wrong format, ignore it
                next;
            }
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "unzip -jo $zip_file -d $config_dir $copy_file",     # -jo = Junk path, overwrite
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy the umbrella file");
                $failures++;
            }
        }

        if ($failures == 0) {
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
    sub Copy_Packed_Internal_Values_Files_To_Config_Directory_P922S01T07 {

        my $config_dir = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";
        my $copy_file;
        my $failures = 0;
        my @files_to_check = ();
        my $rc = 0;
        my @result = ();
        my $zip_file;

        push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} ne "");
        push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} ne "");
        push @files_to_check, $::JOB_PARAMS{'SC_CS_VALUES_FILE'} if ($::JOB_PARAMS{'SC_CS_VALUES_FILE'} ne "");

        General::Logging::log_user_message("Copy internal values file(s) to workspace directory for safe keeping:");
        for my $filename (@files_to_check) {
            General::Logging::log_user_message("  $filename");
            if ($filename =~ /^(.+):(.+)/) {
                # E.g. /path/to/filename.csar:Definitions/OtherTemplates/eric-cloud-native-base-152.2.0.yaml
                $zip_file = $1;
                $copy_file = $2;
            } else {
                # Wrong format, ignore it
                next;
            }
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "unzip -jo $zip_file -d $config_dir $copy_file",     # -jo = Junk path, overwrite
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("  Failed to copy the values file");
                $failures++;
            }
        }

        if ($failures == 0) {
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
    sub Unpack_All_Config_Directory_Umbrella_TGZ_Files_To_Software_Directory_P922S03T08 {

        my $rc = 0;

        # Variables contains e.g. /path/to/filename.csar:Definitions/OtherTemplates/eric-sc-sepp-1.1.0-3-hd3fb128.tgz
        # Save old values
        my $temp_APPLICATION_UMBRELLA_FILE = $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'};
        my $temp_CLOUD_NATIVE_BASE_UMBRELLA_FILE = $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'};
        my $temp_CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE = $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'};
        my $temp_SC_CS_UMBRELLA_FILE = $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'};

        # Replace the path names so they point to the already copied files in the _JOB_CONFIG_DIR
        # I.e. /path/to/filename.csar:Definitions/OtherTemplates/eric-sc-sepp-1.1.0-3-hd3fb128.tgz
        # will be replaced with:
        # /path/to/workspaces/jobname/configurationfiles/eric-sc-sepp-1.1.0-3-hd3fb128.tgz
        $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} = "";
        for my $name (split /\|/, $temp_APPLICATION_UMBRELLA_FILE) {
            if ($name =~ /^.+\/(.+)/) {
                $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} .= "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$1|";
            }
        }
        $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} =~ s/\|$//;

        if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} ne "" && $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} =~ /^.+\/(.+)/) {
            $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$1";
        } else {
            $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} = "";
        }

        if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} ne "" && $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} =~ /^.+\/(.+)/) {
            $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$1";
        } else {
            $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} = "";
        }

        if ($::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "" && $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} =~ /^.+\/(.+)/) {
            $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$1";
        } else {
            $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} = "";
        }

        # Create the directory path
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "mkdir -p $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/Definitions/OtherTemplates",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create output directory");
            return 1;
        }

        $rc = extract_all_umbrella_tgz_files();

        # restore old values
        $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} = $temp_APPLICATION_UMBRELLA_FILE;
        $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} = $temp_CLOUD_NATIVE_BASE_UMBRELLA_FILE;
        $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} = $temp_CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE;
        $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} = $temp_SC_CS_UMBRELLA_FILE;

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
    sub Check_Tools_File_Content_P922S03T09 {

        my $rc = 0;
        my $dir = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools";
        my @result;

        General::Logging::log_user_message("Check that expected files are included in the tools file");
        @result = General::File_Operations::find_file(
            {
                "directory" => $dir,
                "filename"  => "retagger.sh",
                "maxdepth"  => 2,
            }
        );
        if (scalar @result == 1) {
            $::JOB_PARAMS{'RETAGGER_SCRIPT'} = $result[0];
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Expected 'retagger.sh' file was not found in the tools file");
            return 1;
        }

        $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} = "no" unless (exists $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'});
        if ($::JOB_PARAMS{'UNPACK_CSAR_FILE'} eq "yes" && $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'} eq "yes" && $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "no") {
            $::JOB_PARAMS{'CRDS_FILE'} = "" unless (exists $::JOB_PARAMS{'CRDS_FILE'});
            $::JOB_PARAMS{'CRDS_CSAR_FILE'} = "";
            # Look for the CRDS in the inside the tools package
            @result = General::File_Operations::find_file(
                {
                    "directory" => $dir,
                    "filename"  => "eric-sc-crds-*.csar",
                    "maxdepth"  => 2,
                }
            );
            if (scalar @result == 1) {
                General::Logging::log_user_message("CRDS included in tools file");
                $::JOB_PARAMS{'CRDS_CSAR_FILE'} = $result[0];

                General::Logging::log_user_message("Unpacking CRDS CSAR file.\nThis will take a while to complete.\n");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "unzip -o $::JOB_PARAMS{'CRDS_CSAR_FILE'} -d $dir",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to unpack the CRDS CSAR file");
                    return 1;
                }

                # Look for the eric-sc-crds* file, if found here it's the new way off delivering the CRDS
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "find $dir/Definitions/OtherTemplates -name 'eric-sc-crds-*.t*gz'",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0 && scalar @result == 1) {
                    $::JOB_PARAMS{'CRDS_FILE'} = $result[0];
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Did not find the 'eric-sc-crds-*.t*gz' file in the tools file");
                    return 1;
                }
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Did not find the 'eric-sc-crds-*.t*gz' file in neither the software nor the tools file");
                return 1;
            }
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
sub Unpack_Umbrella_Files_P922S04 {

    my $rc = 0;

    if ($::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'} eq "yes" && $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_Umbrella_Files_P922S04T01 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CSAR_File_Umbrella_Content_P922S04T02 } );
        return $rc if $rc < 0;

        # This task only make sense if the files has been unpacked and checked
        if ($::JOB_PARAMS{'COPY_UMBRELLA_FILE'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Umbrella_Files_To_Config_Directory_P922S04T03 } );
            return $rc if $rc < 0;
        }
        if ($::JOB_PARAMS{'UNPACK_ALL_UMBRELLA_TGZ_FILES'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_All_Umbrella_TGZ_Files_To_Software_Directory_P922S04T04 } );
            return $rc if $rc < 0;
        }
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
    sub Unpack_Umbrella_Files_P922S04T01 {

        my $rc = 0;
        my $software_file = "$::JOB_PARAMS{'SOFTWARE_DIR_NAME'}/$::JOB_PARAMS{'SOFTWARE_FILE_NAME'}";

        General::Logging::log_user_message("Unpacking Umbrella files(s) from software file.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "unzip -o $software_file 'Definitions/OtherTemplates/eric-*.t*gz' -d $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to unpack the software file");
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
    sub Check_CSAR_File_Umbrella_Content_P922S04T02 {

        my $dir = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software";
        my $rc = 0;
        my @result = ();

        # Initialize variables for files
        $::JOB_PARAMS{'APPLICATION_TYPE'} = "";
        $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} = "";
        $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} = "";
        $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} = "";
        $::JOB_PARAMS{'CRDS_FILE'} = "";
        $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} = "no";
        $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} = "";
        $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = "";

        General::Logging::log_user_message("Check that expected files are included in the software file");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ls -1 --color=never $dir/Definitions/OtherTemplates/eric-*.t*gz",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Not all expected files was found in the software file");
            return 1;
        }

        return check_csar_file_content(\@result);
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
    sub Copy_Umbrella_Files_To_Config_Directory_P922S04T03 {

        return copy_umbrella_files();
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
    sub Unpack_All_Umbrella_TGZ_Files_To_Software_Directory_P922S04T04 {

        return extract_all_umbrella_tgz_files();
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P922S99 {

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
sub check_csar_file_content {
    my $array_ref = shift;

    my $rc = 0;
    my $release_version = "";
    my $release_build   = "";
    my @result = ();

    # Initialize variables for files
    $::JOB_PARAMS{'APPLICATION_TYPE'} = "";
    $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} = "";
    $::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_BUILD'} = "";
    $::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_VERSION'} = "";
    $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} = "";
    $::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} = "";
    $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'} = "";
    $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'} = "";
    $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} = "";
    $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} = "";
    $::JOB_PARAMS{'CRDS_FILE'} = "";
    $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} = "no";
    $::JOB_PARAMS{'SC_BSF_RELEASE_BUILD'} = "";
    $::JOB_PARAMS{'SC_BSF_RELEASE_VERSION'} = "";
    $::JOB_PARAMS{'SC_BSF_UMBRELLA_FILE'} = "";
    $::JOB_PARAMS{'SC_CS_RELEASE_BUILD'} = "";
    $::JOB_PARAMS{'SC_CS_RELEASE_VERSION'} = "";
    $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} = "";
    $::JOB_PARAMS{'SC_CS_VALUES_FILE'} = "";
    $::JOB_PARAMS{'SC_DSC_RELEASE_BUILD'} = "";
    $::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'} = "";
    $::JOB_PARAMS{'SC_DSC_UMBRELLA_FILE'} = "";
    $::JOB_PARAMS{'SC_SCP_RELEASE_BUILD'} = "";
    $::JOB_PARAMS{'SC_SCP_RELEASE_VERSION'} = "";
    $::JOB_PARAMS{'SC_SCP_UMBRELLA_FILE'} = "";
    $::JOB_PARAMS{'SC_SEPP_RELEASE_BUILD'} = "";
    $::JOB_PARAMS{'SC_SEPP_RELEASE_VERSION'} = "";
    $::JOB_PARAMS{'SC_SEPP_UMBRELLA_FILE'} = "";
    $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = "";

    for (@$array_ref) {
        if (/^.+(eric-cloud-native-base-|eric-cloud-native-nf-additions-|eric-dsc-|eric-sc-bsf-|eric-sc-cs-|eric-sc-diameter-|eric-sc-scp-|eric-sc-sepp-)(\d+\.\d+\.\d+)(\S*)(\.tar\.gz|\.tgz)/) {
            $release_version = $2;
            $release_build   = $3;
        } else {
            $release_version = "";
            $release_build   = "";
        }
        if (/^(.+eric-sc-umbrella-.+)$/) {
            # Old package naming for SC 1.0 to 1.10 (maybe later) where only one complete
            # Umbrella file was delivered containing both ADP and Application data.
            $::JOB_PARAMS{'APPLICATION_TYPE'} .= "sc|";
            $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = $1;
            $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} .= "$_|";
        } elsif (/^(.+eric.+-crd*.+)$/) {
            # New package format where the CRDs are delivered in the CSAR file.
            $::JOB_PARAMS{'CRDS_FILE'} .= "$_|";
            $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} = "yes";
        } elsif (/^(.+eric-cloud-native-base-.+(\.tar\.gz|\.tgz))$/) {
            # New package naming for cnDSC and SC 1.11 (maybe later) where the ADP files are in separate files.
            $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} = "$_";
            $::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_BUILD'} = $release_build;
            $::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_VERSION'} = $release_version;
        } elsif (/^(.+eric-cloud-native-base-.+(\.yaml|\.yml))$/) {
            # New file for cnDSC and SC 1.14 (maybe later) where the ADP files are in separate files.
            $::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} = "$_";
        } elsif (/^(.+eric-cloud-native-nf-additions-.+(\.tar\.gz|\.tgz))$/) {
            # New package naming for cnDSC and SC 1.11 (maybe later) where the ADP files are in separate files.
            $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} = "$_";
            $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'} = $release_build;
            $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'} = $release_version;
        } elsif (/^(.+eric-cloud-native-nf-additions-.+(\.yaml|\.yml))$/) {
            # New file for cnDSC and SC 1.14 (maybe later) where the ADP files are in separate files.
            $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} = "$_";
        } elsif (/^(.+eric-sc-cs-.+(\.tar\.gz|\.tgz))$/) {
            # New package naming for cnDSC and SC 1.11 (maybe later) where the ADP files are in separate files.
            $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} = "$_";
            $::JOB_PARAMS{'SC_CS_RELEASE_BUILD'} = $release_build;
            $::JOB_PARAMS{'SC_CS_RELEASE_VERSION'} = $release_version;
        } elsif (/^(.+eric-sc-cs-.+(\.yaml|\.yml))$/) {
            # New file for cnDSC and SC 1.14 (maybe later) where the ADP files are in separate files.
            $::JOB_PARAMS{'SC_CS_VALUES_FILE'} = "$_";
        } elsif (/^(.+eric-.+(\.tar\.gz|\.tgz))$/) {
            # New package naming for cnDSC and SC 1.11 (maybe later) where multiple Umbrella files
            # were delivered containing multiple CNF files e.g. SCP, SEPP etc.
            $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} .= "$_|";
            if (/^.+\/eric-([^-]+)-.+/) {
                # Normally sc or dsc
                $::JOB_PARAMS{'APPLICATION_TYPE'} .= "$1|";
            }
            if (/^.+\/eric-dsc-.+/) {
                $::JOB_PARAMS{'SC_DSC_RELEASE_BUILD'} = $release_build;
                $::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'} = $release_version;
                $::JOB_PARAMS{'SC_DSC_UMBRELLA_FILE'} = $_;
            } elsif (/^.+\/eric-sc-diameter-.+/) {
                $::JOB_PARAMS{'SC_DSC_RELEASE_BUILD'} = $release_build;
                $::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'} = $release_version;
                $::JOB_PARAMS{'SC_DSC_UMBRELLA_FILE'} = $_;
            } elsif (/^.+\/eric-sc-bsf-.+/) {
                $::JOB_PARAMS{'SC_BSF_RELEASE_BUILD'} = $release_build;
                $::JOB_PARAMS{'SC_BSF_RELEASE_VERSION'} = $release_version;
                $::JOB_PARAMS{'SC_BSF_UMBRELLA_FILE'} = $_;
            } elsif (/^.+\/eric-sc-scp-.+/) {
                $::JOB_PARAMS{'SC_SCP_RELEASE_BUILD'} = $release_build;
                $::JOB_PARAMS{'SC_SCP_RELEASE_VERSION'} = $release_version;
                $::JOB_PARAMS{'SC_SCP_UMBRELLA_FILE'} = $_;
            } elsif (/^.+\/eric-sc-sepp-.+/) {
                $::JOB_PARAMS{'SC_SEPP_RELEASE_BUILD'} = $release_build;
                $::JOB_PARAMS{'SC_SEPP_RELEASE_VERSION'} = $release_version;
                $::JOB_PARAMS{'SC_SEPP_UMBRELLA_FILE'} = $_;
            }
        }
    }

    $::JOB_PARAMS{'APPLICATION_TYPE'} =~ s/\|$//;
    $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} =~ s/\|$//;
    $::JOB_PARAMS{'CRDS_FILE'} =~ s/\|$//;

    if ($::JOB_PARAMS{'APPLICATION_TYPE'} =~ /\|/) {
        # We have multiple application types in the file, find unique ones and pick one of them.
        my %app_types;
        for my $app (split /\|/, $::JOB_PARAMS{'APPLICATION_TYPE'}) {
            $app_types{$app}++;
        }
        my @unique_types = sort keys %app_types;
        if (scalar @unique_types == 1) {
            # Only one type of application, so pick it
            $::JOB_PARAMS{'APPLICATION_TYPE'} = $unique_types[0];
        } elsif (scalar @unique_types > 1) {
            # More than one type, check if we have both dsc and sc
            if (exists $app_types{'dsc'} && exists $app_types{'sc'}) {
                # Both dsc and sc exists, now we need to pick one based on the software release version
                if (version->parse($::JOB_PARAMS{'SC_RELEASE_VERSION'}) < version->parse("1.15.0")) {
                    # It looks like an old cndsc package < 1.15.0, assume it's dsc for backwards compatibility
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "dsc";
                } else {
                    # SC software version 1.15.0 or higher with new CNCS package containing both sc and dsc, assume it's sc.
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
                }
            } elsif (exists $app_types{'sc'}) {
                # We don't care what other application types are included, we assume sc
                $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
            } elsif (exists $app_types{'dsc'}) {
                # We don't care what other application types are included, we assume dsc
                $::JOB_PARAMS{'APPLICATION_TYPE'} = "dsc";
            } else {
                # We don't know what other types are included, so we just pick the first one in the array.
                $::JOB_PARAMS{'APPLICATION_TYPE'} = $unique_types[0];
            }
        } else {
            # No application types found, it should not happen, but we assume sc
            $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
        }
    }

    my $message = "";
    $message .= "APPLICATION_TYPE:                          $::JOB_PARAMS{'APPLICATION_TYPE'}\n" if ($::JOB_PARAMS{'APPLICATION_TYPE'} ne "");
    $message .= "\nFiles:\n";
    $message .= "APPLICATION_UMBRELLA_FILE:                 $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'}\n" if ($::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} ne "");
    $message .= "CLOUD_NATIVE_BASE_UMBRELLA_FILE:           $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'}\n" if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} ne "");
    $message .= "CLOUD_NATIVE_BASE_VALUES_FILE:             $::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'}\n" if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} ne "");
    $message .= "CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE:   $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'}\n" if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} ne "");
    $message .= "CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE:     $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'}\n" if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} ne "");
    $message .= "CRDS_FILE:                                 $::JOB_PARAMS{'CRDS_FILE'}\n" if ($::JOB_PARAMS{'CRDS_FILE'} ne "");
    $message .= "CRDS_IN_SOFTWARE_CSAR_FILE:                $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'}\n";
    $message .= "SC_CS_UMBRELLA_FILE:                       $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'}\n" if ($::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "");
    $message .= "SC_CS_VALUES_FILE:                         $::JOB_PARAMS{'SC_CS_VALUES_FILE'}\n" if ($::JOB_PARAMS{'SC_CS_VALUES_FILE'} ne "");
    $message .= "SC_UMBRELLA_FILE:                          $::JOB_PARAMS{'SC_UMBRELLA_FILE'}\n" if ($::JOB_PARAMS{'SC_UMBRELLA_FILE'} ne "");
    $message .= "\nVersions:\n";
    $message .= "CLOUD_NATIVE_BASE_RELEASE_VERSION:         $::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_VERSION'}\n" if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_VERSION'} ne "");
    $message .= "CLOUD_NATIVE_BASE_RELEASE_BUILD:           $::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_BUILD'}\n" if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_BUILD'} ne "");
    $message .= "CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION: $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'}\n" if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'} ne "");
    $message .= "CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD:   $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'}\n" if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'} ne "");
    $message .= "SC_BSF_RELEASE_VERSION:                    $::JOB_PARAMS{'SC_BSF_RELEASE_VERSION'}\n" if ($::JOB_PARAMS{'SC_BSF_RELEASE_VERSION'} ne "");
    $message .= "SC_BSF_RELEASE_BUILD:                      $::JOB_PARAMS{'SC_BSF_RELEASE_BUILD'}\n" if ($::JOB_PARAMS{'SC_BSF_RELEASE_BUILD'} ne "");
    $message .= "SC_CS_RELEASE_VERSION:                     $::JOB_PARAMS{'SC_CS_RELEASE_VERSION'}\n" if ($::JOB_PARAMS{'SC_CS_RELEASE_VERSION'} ne "");
    $message .= "SC_CS_RELEASE_BUILD:                       $::JOB_PARAMS{'SC_CS_RELEASE_BUILD'}\n" if ($::JOB_PARAMS{'SC_CS_RELEASE_BUILD'} ne "");
    $message .= "SC_DSC_RELEASE_VERSION:                    $::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'}\n" if ($::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'} ne "");
    $message .= "SC_DSC_RELEASE_BUILD:                      $::JOB_PARAMS{'SC_DSC_RELEASE_BUILD'}\n" if ($::JOB_PARAMS{'SC_DSC_RELEASE_BUILD'} ne "");
    $message .= "SC_SCP_RELEASE_VERSION:                    $::JOB_PARAMS{'SC_SCP_RELEASE_VERSION'}\n" if ($::JOB_PARAMS{'SC_SCP_RELEASE_VERSION'} ne "");
    $message .= "SC_SCP_RELEASE_BUILD:                      $::JOB_PARAMS{'SC_SCP_RELEASE_BUILD'}\n" if ($::JOB_PARAMS{'SC_SCP_RELEASE_BUILD'} ne "");
    $message .= "SC_SEPP_RELEASE_VERSION:                   $::JOB_PARAMS{'SC_SEPP_RELEASE_VERSION'}\n" if ($::JOB_PARAMS{'SC_SEPP_RELEASE_VERSION'} ne "");
    $message .= "SC_SEPP_RELEASE_BUILD:                     $::JOB_PARAMS{'SC_SEPP_RELEASE_BUILD'}\n" if ($::JOB_PARAMS{'SC_SEPP_RELEASE_BUILD'} ne "");
    General::Logging::log_user_message($message);

    # Check that all expected files were found
    if ($::JOB_PARAMS{'SC_UMBRELLA_FILE'} eq "" && $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} eq "" && $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} eq "" && $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} eq "" && $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} eq "") {
        General::Logging::log_user_error_message("Did not find the expected 'eric-sc-umbrella-*t*gz' file");
        $rc = 1;
    } elsif ($::JOB_PARAMS{'SC_UMBRELLA_FILE'} eq "" && ($::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} eq "" || $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} eq "" || $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} eq "" || $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} eq "")) {
        General::Logging::log_user_error_message("Did not find the expected 'eric-(dsc|sc-bsf|sc-cs|sc-diameter|sc-scp|sc-sepp)-*t*gz', 'eric-dsc-*t*gz',  'eric-cloud-native-base-*t*gz' or 'eric-cloud-native-nf-additions-*t*gz' files");
        $rc = 1;
    } elsif ($::JOB_PARAMS{'CRDS_FILE'} eq "") {
        General::Logging::log_user_warning_message("Looks like you might be deploying or upgrading with an old SC package where the CRD files are not in the CSAR package but in the tools package");
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub checksum_file {
    my $filename = shift;

    my $check_only_one_checksum_file = 1;    # Change it to 0 to check all available checksum files, it will of course take longer if we check all checksum files
    my $checksum_count = 0;
    my $checksum_failures = 0;
    my $expected_checksum;
    my $extension;
    my $file_checksum;
    my $rc;
    my @result;

    if (exists $::JOB_PARAMS{'SKIP_CHECKSUM_CHECK'} && $::JOB_PARAMS{'SKIP_CHECKSUM_CHECK'} eq "yes") {
        # The user want to skip the checksum check
        return 0;
    }

    for $extension ("sha256", "sha1", "md5") {
        next unless (-f "$filename.$extension");

        General::Logging::log_user_message("Checking $extension checksum on file: $filename");
        $rc = General::File_Operations::read_file(
            {
                "filename"              => "$filename.$extension",
                "output-ref"            => \@result,
            }
        );
        $checksum_count++;
        if ($rc == 0 && scalar @result == 1) {
            if ($extension eq "md5") {
                if ($result[0] =~ /^([0-9a-fA-F]{32})/) {
                    $expected_checksum = $1;
                    $file_checksum = General::File_Operations::checksum_file_md5($filename);
                } else {
                    General::Logging::log_user_error_message("Wrong $extension checksum format in file $filename.extension: $result[0]");
                    $checksum_failures++;
                    last;
                }
            } elsif ($extension eq "sha1") {
                if ($result[0] =~ /^([0-9a-fA-F]{40})/) {
                    $expected_checksum = $1;
                    $file_checksum = General::File_Operations::checksum_file_sha1($filename);
                } else {
                    General::Logging::log_user_error_message("Wrong $extension checksum format in file $filename.extension: $result[0]");
                    $checksum_failures++;
                    last;
                }
            } elsif ($extension eq "sha256") {
                if ($result[0] =~ /^([0-9a-fA-F]{64})/) {
                    $expected_checksum = $1;
                    $file_checksum = General::File_Operations::checksum_file_sha256($filename);
                } else {
                    General::Logging::log_user_error_message("Wrong $extension checksum format in file $filename.extension: $result[0]");
                    $checksum_failures++;
                    last;
                }
            } else {
                # Should not happen, if it does then the 'for' loop or the 'if' statements are missing some entries.
                next;
            }

            if ($file_checksum eq "") {
                General::Logging::log_user_error_message("Failed to check $extension checksum on file, checksum command failed or returned wrong format");
                $checksum_failures++;
                last;
            } elsif ($expected_checksum ne $file_checksum) {
                General::Logging::log_user_error_message("Wrong $extension checksum '$file_checksum' on file, expected '$expected_checksum'");
                $checksum_failures++;
                last;
            }
            General::Logging::log_user_message("  Correct $extension checksum '$file_checksum', file is OK");
            if ($check_only_one_checksum_file == 1) {
                last;
            } else {
                next;
            }
        } else {
            General::Logging::log_user_error_message("Failed to read $filename.$extension checksum file");
            $checksum_failures++;
            last;
        }
    }

    if ($checksum_count == 0) {
        # No checksum files found in the same directory, assume that file is correct
        General::Logging::log_user_warning_message("No .md5, .sha1 or .sha256 files found, no check done on file: $filename");
    }

    if ($checksum_failures == 0) {
        return 0;
    } else {
        return General::Playlist_Operations::RC_FALLBACK;
    }
}

# -----------------------------------------------------------------------------
sub copy_umbrella_files {
    my $config_dir = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";
    my $failures = 0;
    my @files_to_check = split /\|/, $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'};
    my $rc = 0;
    my @result = ();

    push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} ne "");
    push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} ne "");
    push @files_to_check, $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} if ($::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "");

    General::Logging::log_user_message("Copy umbrella file(s) to workspace directory for safe keeping:");
    for my $filename (@files_to_check) {
        General::Logging::log_user_message("  $filename");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "cp -pf $filename $config_dir",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy the umbrella file");
            $failures++;
        }
    }

    if ($failures == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
sub extract_all_umbrella_tgz_files {
    my $output_dir = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/Definitions/OtherTemplates/";
    my $failures = 0;
    my @files_to_check = split /\|/, $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'};
    my $rc = 0;
    my @result = ();

    push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} ne "");
    push @files_to_check, $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} ne "");
    push @files_to_check, $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} if ($::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "");

    General::Logging::log_user_message("Extracting all umbrella .tgz file(s) to workspace directory temporarilly:");
    for my $filename (@files_to_check) {
        General::Logging::log_user_message("  $filename");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "tar xf $filename -C $output_dir",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to extract the umbrella file");
            $failures++;
        }
    }

    if ($failures == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist script is used for checking the files included in the software
delivery directory and it performs a check that all expected files are present
and that the check sums for the files are correct.

If wanted it also unpacks and checks the contents of the files.

It uses the following job parameter that MUST be pointing to the top level
directory where the software files are located, if not set at all or set to an
empty string then no checks are done on the software directory:

    - SOFTWARE_DIR

It also checks the following variables and if the parameters does not exist they
will all be set to the value "yes" and all files will be checked, unpacked and
file contents checked.

    - CHECK_CSAR_FILE
    - CHECK_CSAR_FILE_CONTENT
    - CHECK_TOOLS_FILE
    - CHECK_TOOLS_FILE_CONTENT
    - CHECK_VALUES_FILE
    - UNPACK_CSAR_FILE
    - UNPACK_TOOLS_FILE
    - UNPACK_UMBRELLA_FILE

It will create or update the following job parameters:

    - APPLICATION_UMBRELLA_FILE
    - CLOUD_NATIVE_BASE_UMBRELLA_FILE
    - CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE
    - SC_CS_UMBRELLA_FILE
    - CRDS_CSAR_FILE
    - CRDS_FILE
    - CRDS_FILE
    - CRDS_IN_SOFTWARE_CSAR_FILE
    - CRDS_IN_SOFTWARE_CSAR_FILE
    - JOBSTATUS
    - RETAGGER_SCRIPT
    - SC_RELEASE_VERSION
    - SC_UMBRELLA_FILE
    - SOFTWARE_DIR_NAME
    - SOFTWARE_FILE_NAME
    - TOOLS_DIR_NAME
    - TOOLS_FILE_NAME
    - VALUES_FILE_NAME

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;

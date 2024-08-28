package Playlist::701_Test_Parse_Network_Config_File;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2024-05-28 10:00:00
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
    usage
    usage_return_playlist_variables
    );

#
# Used Perl package files
#

use ADP::Kubernetes_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

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

    my $node_count = 0;
    my $rc;
    my $valid;
    my $var_name;

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "TEST_PARSE_NETWORK_CONFIG_FILE";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (General::Playlist_Operations::is_mandatory_network_config_loaded() != 0) {
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    $rc = General::Playlist_Operations::execute_step( \&Check_Network_Config_File_P701S01, undef );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'JOBSTATUS'} = "SUCCESSFUL";

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
sub Check_Network_Config_File_P701S01 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Initial_Job_Parameters_File_P701S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P701S01T02 } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Final_Job_Parameters_File_P701S01T03 } );
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
    sub Copy_Initial_Job_Parameters_File_P701S01T01 {

        my $rc = 0;

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "cp -fp $::JOB_PARAMS{'_JOB_PARAMS_FILE'} $::JOB_PARAMS{_JOB_CONFIG_DIR}/job_parameters_At_Startup.conf",
                "hide-output"   => 1,
            }
        );

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
    sub Check_Job_Parameters_P701S01T02 {

        delete $::JOB_PARAMS{'APPLICATION_TYPE'} if ($::JOB_PARAMS{'APPLICATION_TYPE'} eq "");
        delete $::JOB_PARAMS{'DEPLOYED_CNF'} if ($::JOB_PARAMS{'DEPLOYED_CNF'} eq "");
        delete $::JOB_PARAMS{'ENABLED_CNF'} if ($::JOB_PARAMS{'ENABLED_CNF'} eq "");
        delete $::JOB_PARAMS{'PASSWORD_EXPIRE'} if ($::JOB_PARAMS{'PASSWORD_EXPIRE'} eq "");
        delete $::JOB_PARAMS{'SC_RELEASE_VERSION'} if ($::JOB_PARAMS{'SC_RELEASE_VERSION'} eq "");

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
    sub Copy_Final_Job_Parameters_File_P701S01T03 {

        my $rc = 0;

        # Since we might have updated the job parameters then we need to write %::JOB_PARAMS hash to file
        General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "cp -fp $::JOB_PARAMS{'_JOB_PARAMS_FILE'} $::JOB_PARAMS{_JOB_CONFIG_DIR}/job_parameters_At_Finish.conf",
                "hide-output"   => 1,
            }
        );

        return $rc;
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
        'APPLICATION_TYPE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The application type to emulate, it should be specified in the following format:
dsc
sc

If not specified then the playlist will not see this parameter at all, i.e. it
will be deleted.

Will be used to check against network config parameter attribute 'application_type'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DEPLOYED_CNF' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The deployed cnf type to emulate.

If not specified then the playlist will not see this parameter at all, i.e. it
will be deleted.

Will be used to check against network config parameter attribute 'cnf_type'.

Specify the Containerized Network Functions (CNF) that should be enabled on the
deployed software.

If more than one CNF should be enabled then separate each CNF with either a
dash (-), comma (,) or a space ( ).
For example: "bsf-wcdb", "bsf,wcdb" or "bsf wcdb".

Currently known CNF values are the following:
    - bsf
    - csa (deprecated)
    - dsc
    - scp
    - sepp

But can also contain one of the following but should not really be usedL
    - bsfdiameter
    - nlf
    - objectstorage
    - pvtb
    - rlf
    - sftp
    - slf
    - spr
    - wcdb

Not all combinations are valid or make sense, so know what combination to use.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ENABLED_CNF' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The enabled CNF type to emulate.
The DEPLOYED_CNF has higher priority than this parameter.

If not specified then the playlist will not see this parameter at all, i.e. it
will be deleted.

Will be used to check against network config parameter attribute 'cnf_type'.

Specify the Containerized Network Functions (CNF) that should be enabled on the
deployed software.

If more than one CNF should be enabled then separate each CNF with either a
dash (-), comma (,) or a space ( ).
For example: "bsf-wcdb", "bsf,wcdb" or "bsf wcdb".

Currently known CNF values are the following:
    - bsf
    - csa (deprecated)
    - dsc
    - scp
    - sepp

But can also contain one of the following but should not really be usedL
    - bsfdiameter
    - nlf
    - objectstorage
    - pvtb
    - rlf
    - sftp
    - slf
    - spr
    - wcdb

Not all combinations are valid or make sense, so know what combination to use.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'PASSWORD_EXPIRE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The password expire value to emulate, it should be specified in the following
format:
yes
no

If not specified then the playlist will not see this parameter at all, i.e. it
will be deleted.

Will be used to check against network config parameter attribute 'password_expire'.

This parameter controls if the password for newly created user accounts expire
on first login requiring a change of the password.

The default behavior is that all newly created user accounts will have the
password expire on the first login.

If this parameter is set to the value 'no' then a hack workaround will be used
to modify the LDAP data to mark the account to not expire. This "no" value
should only be used if you know what you are doing since it will bypass some
of the node hardening and will make the node less secure.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SC_RELEASE_VERSION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The software release version to emulate, it should be specified in the following
format:
1.14.0
1.15.25

If not specified then the playlist will not see this parameter at all, i.e. it
will be deleted.

Will be used to check against network config parameter attribute 'valid_releases'.
EOF
            'validity_mask' => '(\d+\.\d+\.\d+)',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist script is used for checking logic that reads the network config
file and removes parameters that should not be used.

It will be called from the execute_playlist.pl script.

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

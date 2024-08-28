package Playlist::804_Timeshift_Traffic_Setup;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.5
#  Date     : 2023-09-25 12:08:19
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022-2023
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
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

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
#  1: The call failed
#  255: Stepout wanted
#
# -----------------------------------------------------------------------------
sub main {
    my $rc;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "TIMESHIFT_TRAFFIC_SETUP";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P804S01, \&Fallback001_P804S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P804S02, \&Fallback001_P804S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Create_Package_P804S03, \&Fallback001_P804S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P804S04, \&Fallback001_P804S99 );
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
sub Initialize_Job_Environment_P804S01 {

    my $rc;

    # This playlist does not use docker, kubectl or helm commands so skip these checks
    $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_HELM_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_KUBECTL_CHECK'} = "yes";

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
sub Check_Job_Parameters_P804S02 {

        my $rc = 0;

        # Get the proper DIRECTOR_IP
        General::Logging::log_user_message("Checking Job parameter 'DIRECTOR_IP'");
        if ($::JOB_PARAMS{'DIRECTOR_IP'} eq "") {
            General::Logging::log_user_error_message("Job parameter 'DIRECTOR_IP' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper CERTIFICATE_JOB_WS
        General::Logging::log_user_message("Checking Job parameter 'CERTIFICATE_JOB_WS'");
        if ($::JOB_PARAMS{'CERTIFICATE_JOB_WS'} eq "") {
            General::Logging::log_user_error_message("Job parameter 'CERTIFICATE_JOB_WS' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

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
sub Create_Package_P804S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Deploy_K6_P804S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Deploy_DSC_Load_P804S03T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Session_Binding_P804S03T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_Bindings_DSC_Load_P804S03T04 } );
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
    sub Deploy_K6_P804S03T01 {

        my $director_ip = $::JOB_PARAMS{'DIRECTOR_IP'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $certificate_job_ws = $::JOB_PARAMS{'CERTIFICATE_JOB_WS'};

        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Deploy K6");
        my $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                    "--ip=$director_ip " .
                    "--user=eccd " .
                    "--password='notneeded' " .
                    "--timeout=$expect_timeout " .
                    "--command='cd /home/eccd/tools/' " .
                    "--command='./load_deploy.sh eiffelesc' " .
                    "--command='kubectl -n eiffelesc cp $certificate_job_ws/configurationfiles/K6.crt \$(kubectl -n eiffelesc get pod | grep eric-k6-deployment-| cut -d\" \" -f1):/certs/' " .
                    "--command='kubectl -n eiffelesc cp $certificate_job_ws/configurationfiles/K6.key \$(kubectl -n eiffelesc get pod | grep eric-k6-deployment-| cut -d\" \" -f1):/certs/' " .
                    "--command='kubectl -n eiffelesc cp /home/eccd/tools/bsf_test.js \$(kubectl -n eiffelesc get pod | grep eric-k6-deployment- | cut -d\" \" -f1):/tests/' " .
                    "--command='kubectl -n eiffelesc exec -it \$(kubectl -n eiffelesc get pod | grep eric-k6-deployment-| cut -d\" \" -f1) -- sh' " .
                    "--command='chown root:root /tests/bsf_test.js' " .
                    "--command='chmod 644 /tests/bsf_test.js' " .
                    "--command='chown root:root /certs/K6*' " .
                    "--command='chmod 644 /certs/K6*' " .
                    "--command='exit' " ,
                "hide-output"         => 1,
                "return-output"       => \@result,
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
    sub Deploy_DSC_Load_P804S03T02 {

        my $director_ip = $::JOB_PARAMS{'DIRECTOR_IP'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};

        my $rc = 0;
        my @result;

        # TODO: This command does not work, it causes unexpected exit when executing the ./retagger.sh script.

        General::Logging::log_user_message("Deploy DSC load");
        my $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                    "--ip=$director_ip " .
                    "--user=eccd " .
                    "--password='notneeded' " .
                    "--timeout=$expect_timeout " .
                    "--command='cd /home/eccd/tools/dscload' " .
                    "--command='sudo -n docker load --input eric-dscload-1.4.25-ha70a8c551.dirty.tar' " .
                    "--command='./retagger.sh k8s-registry.eccd.local:\$(kubectl -n ingress-nginx get svc ingress-nginx -o jsonpath=\"{.spec.ports[1].nodePort}\") images.txt' " .
                    "--command='helm -n eiffelesc install dscload eric-dscload-1.4.25-ha70a8c551.dirty.tgz -f values_modified.yaml' " .
                    "--command='kubectl -n eiffelesc get pod | grep dscload' " .
                    "--command=\"printf \"\\naf-\%s\\n\%s:\%d\\n\\n\" \$(kubectl -n eiffelesc get pod | grep eric-dscload-deployment- | head -1 | awk \'{ print \$1 }\') \$(kubectl get --namespace eiffelesc -o jsonpath='{.spec.clusterIP}' services eric-stm-diameter-traffic-tcp) \$(kubectl get --namespace eiffelesc -o jsonpath=\"{.spec.ports[0].port}\" services eric-stm-diameter-traffic-tcp)\" " .
                    "--command='kubectl -n eiffelesc exec -it \$(kubectl -n eiffelesc get pod | grep eric-dscload-deployment- | head -1) -- bash' " .
                    "--command='ip addr show dev eth0' " .
                    #"--command='vim /opt/dsc-load/load.cfg' " .
                    "--command='exit' " ,
                "hide-output"         => 1,
                "return-output"       => \@result,
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
    sub Create_Session_Binding_P804S03T03 {

        my $director_ip = $::JOB_PARAMS{'DIRECTOR_IP'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};

        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Create session binding");
        my $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                    "--ip=$director_ip " .
                    "--user=eccd " .
                    "--password='notneeded' " .
                    "--timeout=$expect_timeout " .
                    "--command='/home/eccd/daft/expect/bin/send_command_to_ssh.exp --ip=10.221.168.104 --port=830 --user=bsf-admin --password=bsfbsf --shell=netconf --command-file=/home/eccd/DT/bsf/sample_bsf_func_and_diameter.netconf' " .
                    "--command='ssh bsf-admin@10.221.168.104' " .
                    "--command='config' " .
                    "--command='bsf-function nf-instance bsf1 bsf-service serviceName1 binding-database initialize-db datacenter { name datacenter1 replication-factor 2 }' " .
                    "--command='exit' " .
                    "--command='exit' " ,
                "hide-output"         => 1,
                "return-output"       => \@result,
             }
        );

        my $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                    "--ip=10.221.168.104 " .
                    "--user=bsf-admin " .
                    "--password='bsfbsf' " .
                    "--timeout=$expect_timeout " .
                    "--command='config' " .
                    "--command='bsf-function nf-instance bsf1 bsf-service serviceName1 binding-database initialize-db datacenter { name datacenter1 replication-factor 2 }' " .
                    "--command='exit' " .
                    "--command='exit' " ,
                "hide-output"         => 1,
                "return-output"       => \@result,
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
    sub Load_Bindings_DSC_Load_P804S03T04 {

        my $director_ip = $::JOB_PARAMS{'DIRECTOR_IP'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};

        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Load bindings");
        my $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                    "--ip=$director_ip " .
                    "--user=eccd " .
                    "--password='notneeded' " .
                    "--timeout=$expect_timeout " .
                    "--command='export BINDING_IPV4='{\"supi\":\"imsi-12345\",\"gpsi\":\"msisdn-306972909290\",\"ipv4Addr\":\"10.0.0.1\",\"dnn\":\"testDnn\",\"pcfFqdn\":\"pcf.ericsson.se\",\"pcfIpEndPoints\":[{\"ipv4Address\":\"10.0.0.1\",\"transport\":\"TCP\",\"port\":1024}],\"pcfDiamHost\":\"pcf-diamhost.com\",\"pcfDiamRealm\":\"pcf-diamrealm.com\",\"snssai\":{\"sst\":2,\"sd\":\"DEADF0\"}}'' " .
                    "--command='export BSFWRK_PORT=\$(kubectl get svc --namespace eiffelesc eric-bsf-worker -o jsonpath=\"{.spec.ports[0].nodePort}\")' " .
                    "--command='export NAMESPACE=eiffelesc' " .
                    "--command='curl -si -d \"\$BINDING_IPV4\" -H \"Content-Type: application/json\" -X POST http://\$NODE_IP:\$BSFWRK_PORT/nbsf-management/v1/pcfBindings' " .
                    "--command='curl -s -v http://\$NODE_IP:\$BSFWRK_PORT/nbsf-management/v1/pcfBindings?ipv4Addr=10.0.0.1' " ,
                "hide-output"         => 1,
                "return-output"       => \@result,
             }
        );

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
sub Cleanup_Job_Environment_P804S04 {

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
sub Fallback001_P804S99 {

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
        'CERTIFICATE_JOB_WS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls where on the director node the workspace directory for
the Certificate generation playlist has stored the K6 certificates.
It's usually something like this: /home/eccd/workspaces/<certificate generation playlist>
and this playlist will look for the needed certificate files under the
"configurationfiles" subdirectory.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DIRECTOR_IP' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the IP-address to reach the director node where most of
the logic is executed.
NOTE: That the known_host file on the director must include the public ssh key
of the current user because it needs to be able to login to the director without
giving a password.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DEBUG_PLAYLIST' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist execution should be
executed as normal (=no) or if special handling should be applied (=yes).

If the value is "no" which is also the default value then normal playlist
execution will be done and all applicable commands will be executed.

If the value is "yes" then playlist execution will be altered so that any
command that changes the data on the running cluster will not be executed and
instead will just be echoed to the log files and no change is done, i.e. no
Deployment will take place.
Any command that will just change files or data on the job workspace directory
or any command that just reads data from the cluster will still be executed.
This parameter can be useful to check if the overall logic is working without
doing any changes to the running system.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EXPECT_DEFAULT_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "7200",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the default timeout of the commands executed by the
send_command_to_ssh.exp script used by some playlists to execute commands on
a remote node.
The value specifies how many seconds to wait before giving up on a command that
does not return a new prompt.
Some playlists can still use some other hard coded timeout value in which case
the value set by this parameter is not used, check each playlist which calls the
expect script to see what default value is being used.

By default the value is set to 2 hours (7200 seconds) which should be long
enough for most commands.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },
    )
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist installs the K6 and dscload traffic generators for BSF traffic
that is used for the Time Shift test case.

NOTE: This playlist is not yet debugged and is not properly working.

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

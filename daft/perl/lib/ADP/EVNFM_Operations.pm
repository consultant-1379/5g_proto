package ADP::EVNFM_Operations;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.15
#  Date     : 2023-09-18 13:10:40
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
use warnings;

use Exporter qw(import);

our @EXPORT_OK = qw(
    change_vnfpkg
    check_onboarding_status
    check_operation_status
    create_session_id
    create_vnf_instance_id
    create_vnf_package_id
    delete_vnf_instance_id
    delete_vnf_package
    get_namespaceInfo
    get_vnf_instance_info
    get_vnf_package_info
    instantiate_vnf
    onboard_vnf_package
    scale
    terminate_vnf
    );

use General::OS_Operations;
use General::Logging;
use General::Json_Operations;
use General::Yaml_Operations;

our $last_error_details = "";
my $latest_instance_id = "";
my $latest_package_id = "";
my $latest_session_id = "";
my %session_info;
    # 1st key: session uuid
    # 2nd key: One of the following
    #   create-epoch:           Epoch time stamp when session id was created
    #   ingress-host-address:   Host address
    #   password:               Password used for login
    #   username:               Username for login

my $insecure = "--insecure";      # If needed change again this value from "" to "--insecure"

# -----------------------------------------------------------------------------
# Change the VNF.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "change-file":
#           Specifies the file containing information about the node and
#           namespace etc. to be used for the change of the SC software.
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "instance-id":
#           Specifies the instance id to instantiate. If not specified then the
#           previously created instance id from the call to the
#           'create_vnf_instance_id' call will be used, if that also does not
#           exists then an error is reported.
#       "monitor-timeout":
#           If specified then the monitoring of the onboarding status will
#           continue for a maximum of the specified number of seconds.
#           If not specified then the default maximum time is 7200 seconds
#           or about 2 hours.
#       "no-monitoring":
#           If specified and =1 then no monitoring of successful onboarding
#           will take place, only starting of the onboarding will be done and
#           it's up to the called to monitor for successful onboarding status
#           e.g. by calling the check_onboarding_status subroutine themselves.
#       "progress-messages":
#           If specified and =1 then progress messages are written to STDOUT
#           and if =0 or not specified then no progress message are written
#           during the onboarding procedure.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#       "values-file":
#           Specifies the file containing node specific values about the
#           software to be instantiated (eric-sc-values.yaml file).
#
# Return values:
#    - <url to monitor instantiate job>: The instantiation either started or
#      was successful.
#    - "": An empty string is returned when the instantiation failed for
#      some reason.
#
# -----------------------------------------------------------------------------
sub change_vnfpkg {
    my %params = %{$_[0]};

    # Initialize local variables
    my $change_file             = exists $params{"change-file"} ? $params{"change-file"} : "";
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $instance_id             = exists $params{"instance-id"} ? $params{"instance-id"} : "";
    my $monitor_timeout         = exists $params{"monitor-timeout"} ? $params{"monitor-timeout"} : 7200;
    my $no_monitoring           = exists $params{"no-monitoring"} ? $params{"no-monitoring"} : 0;
    my $progress_messages       = exists $params{"progress-messages"} ? $params{"progress-messages"} : 0;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";
    my $values_file             = exists $params{"values-file"} ? $params{"values-file"} : "";
    my $vnfd_id                 = exists $params{"vnfd-id"} ? $params{"vnfd-id"} : "";
    my $cluster_name            = exists $params{"cluster-name"} ? $params{"cluster-name"} : "";
    my $namespace               = exists $params{"namespace"} ? $params{"namespace"} : "";

    my $curl_command = "";
    my $max_time;
    my $rc = 0;
    my @result;
    my $url;

    if ($instance_id eq "") {
        # No package id specified, check if a package id was previously created
        if ($latest_instance_id ne "") {
            $instance_id = $latest_instance_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'instance-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }
    if ($change_file eq "") {
        # No instantiate information file name provided, report an error
        $last_error_details = "No 'instantiate-file' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if ($values_file eq "") {
        # No values file name provided, report an error
        $last_error_details = "No 'values-file' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    $curl_command = "curl -i -X POST https://$ingress_host_address/vnflcm/v1/vnf_instances/$instance_id/change_vnfpkg -H 'Content-Type: multipart/form-data' -H 'cookie:JSESSIONID=$session_id' -F changeCurrentVnfPkgRequest=\@$change_file -F valuesFile=\@$values_file --silent --show-error $insecure";

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if (scalar @result > 0) {
        # Check that an 202 HTTP answer is received
        my $return_code = -1;
        for (@result) {
            if (/^HTTP\S*\s+(\d+)/) {
                $return_code = $1;
                if ($return_code != 202) {
                    $last_error_details = "Curl command failed with HTTP response code $return_code (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                    General::Logging::log_write("$last_error_details\n") if $debug_messages;
                    return "";
                }
            } elsif (/^location:\s+(https:.+)/) {
                $url = $1;
            }
        }
        unless ($return_code == 202 && $url ne "") {
            $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    } else {
        $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    if ($no_monitoring) {
        # We return success even though the onboarding might not be finished.
        return $url;
    } else {
        # Start the monitoring
        $rc = check_operation_status(
            {
                "debug-messages"        =>  $debug_messages,
                "hide-output"           =>  $hide_output,
                "progress-messages"     =>  $progress_messages,
                "session-id"            =>  $session_id,
                "monitor-timeout"       =>  $monitor_timeout,
                "type"                  =>  "upgrade",
                "url"                   =>  $url,
            }
        );
        if ($rc == 0) {
            return $url;
        } else {
            return "";
        }
    }
}

# -----------------------------------------------------------------------------
# Monitor the onboarding status.
# This subroutine should be called as part of calling onboard_vnf_package or
# directly after calling that function and it checks the monitoring status for
# a maximum time or until the onboardingState shows ONBOARDED.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "monitor-timeout":
#           If specified then the monitoring of the onboarding status will
#           continue for a maximum of the specified number of seconds.
#           If not specified then the default maximum time is 7200 seconds
#           or about 2 hours.
#       "package-id":
#           Specifies the package id to delete. If not specified then the
#           previously created package id from the call to the
#           'create_vnf_package_id' call will be used, if that also does not
#           exists then an error is reported.
#       "progress-messages":
#           If specified and =1 then progress messages are written to STDOUT
#           and if =0 or not specified then no progress message are written
#           during the onboarding procedure.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#    - 0: The onboarding was successful.
#    - 1: The onboarding failed for some reason.
#    - 2: Timeout occurred while waiting for the onboarding to finish.
#
# -----------------------------------------------------------------------------
sub check_onboarding_status {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $monitor_timeout         = exists $params{"monitor-timeout"} ? $params{"monitor-timeout"} : 7200;
    my $package_id              = exists $params{"package-id"} ? $params{"package-id"} : "";
    my $progress_messages       = exists $params{"progress-messages"} ? $params{"progress-messages"} : 0;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";

    my $curl_command = "";
    my $max_time;
    my $rc = 0;
    my @result;

    if ($package_id eq "") {
        # No package id specified, check if a package id was previously created
        if ($latest_package_id ne "") {
            $package_id = $latest_package_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'package-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    if ($progress_messages == 1) {
        General::Logging::log_user_message(sprintf "Monitoring the onboarding status:\n%-20s  %-20s  %s\n%-20s  %-20s  %s\n", "onboardingState", "operationalState", "usageState", "-"x15, "-"x16, "-"x10);
    }

    # Set maximum time to wait for the onboarding to finish
    $max_time = time() + $monitor_timeout;

    while (1) {
        # Monitor the onboarding status
        $curl_command = "curl -X GET https://$ingress_host_address/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages/$package_id -H 'Accept: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure | jq .";

        $rc = General::OS_Operations::send_command(
            {
                "command"           => $curl_command,
                "hide-output"       => $hide_output,
                "return-output"     => \@result,
            }
        );
        my $onboarding_state = "";
        my $operational_state = "";
        my $usage_state = "";
        for (@result) {
            if (/^\s*"onboardingState":\s*"(\S+)"/) {
                $onboarding_state = $1;
            } elsif (/^\s*"operationalState":\s*"(\S+)"/) {
                $operational_state = $1;
            } elsif (/^\s*"usageState":\s*"(\S+)"/) {
                $usage_state = $1;
            } elsif (/^\s*"title":\s*"Onboarding Failed",/) {
                # Onboarding failed
                $last_error_details = "Onboarding failed (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                General::Logging::log_write("$last_error_details\n") if $debug_messages;
                return 1;
            }
        }
        if ($progress_messages == 1) {
            General::Logging::log_user_message(sprintf "%-20s  %-20s  %s\n", $onboarding_state, $operational_state, $usage_state);
        }
        if ($onboarding_state eq "ONBOARDED") {
            # Onboarding successful
            $rc = 0;
            last;
        } elsif ($onboarding_state eq "CREATED") {
            # Onboarding failed since it should basically be UPLOADING -> PROCESSING -> ONBOARDED
            $last_error_details = "Onboarding failed because onboardingState went back to CREATED (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            $rc = 1;
            last;
        } elsif (time() >= $max_time) {
            # Maximum time to wait for onboarding to finish has expired
            $last_error_details = "Maximum time ($monitor_timeout seconds) for onboarding to finish has expired (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            $rc = 2;
            last;
        }

        # Wait 10 seconds before checking status again
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => 0,
                "progress-message"  => 0,
                "seconds"           => 10,
            }
        );
        if ($rc == 1) {
            # User pressed CTRL-C to interrupt the sleep
            $last_error_details = "Onboarding status check failed because the user interrupted it by pressing CTRL-C (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            $rc = 1;
            last;
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Monitor the operation status.
# This subroutine should be called as part of instantiate_vnf, change_vnfpkg,
# scale or terminate_vnf or directly after calling that function and it checks
# the operation status for a maximum time or until the operationState shows
# COMPLETED or FAILED.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "monitor-timeout":
#           If specified then the monitoring of the instantiation status will
#           continue for a maximum of the specified number of seconds.
#           If not specified then the default maximum time is 7200 seconds
#           or about 2 hours.
#       "progress-messages":
#           If specified and =1 then progress messages are written to STDOUT
#           and if =0 or not specified then no progress message are written
#           during the instantiation procedure.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#       "type":
#           Specifies the type of operation to monitor, e.g. instantiation,
#           scale, terminate or upgrade, but can be any string and the type
#           is only used in status messages.
#       "url":
#           Specifies the URL to use for monitoring of the operation status.
#           If not specified then then an error is reported.
#
# Return values:
#    - 0: The instantiation was successful.
#    - 1: The instantiation failed for some reason.
#    - 2: Timeout occurred while waiting for the instantiation to finish.
#
# -----------------------------------------------------------------------------
sub check_operation_status {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $monitor_timeout         = exists $params{"monitor-timeout"} ? $params{"monitor-timeout"} : 7200;
    my $progress_messages       = exists $params{"progress-messages"} ? $params{"progress-messages"} : 0;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";
    my $url                     = exists $params{"url"} ? $params{"url"} : "";
    my $type                    = exists $params{"type"} ? $params{"type"} : "instantiation";

    my $curl_command = "";
    my $max_time;
    my $rc = 0;
    my @result;

    if ($url eq "") {
        # No session id was previous created, report an error
        $last_error_details = "No 'url' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return 1;
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    if ($progress_messages == 1) {
        General::Logging::log_user_message(sprintf "Monitoring the $type operation status:\n%-14s\n%-14s\n", "operationState", "-"x14);
    }

    # Set maximum time to wait to finish
    $max_time = time() + $monitor_timeout;

    while (1) {
        # Monitor the status
        $curl_command = "curl -X GET $url -H 'Accept: application/json' -H 'Content-Type: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure | jq";

        $rc = General::OS_Operations::send_command(
            {
                "command"           => $curl_command,
                "hide-output"       => $hide_output,
                "return-output"     => \@result,
            }
        );
        my $operation_state = "";
        for (@result) {
            if (/^\s*"operationState":\s*"(\S+)"/) {
                $operation_state = $1;
            }
        }
        if ($progress_messages == 1) {
            General::Logging::log_user_message(sprintf "%-14s\n", $operation_state);
        }
        if ($operation_state eq "COMPLETED") {
            # instantiation successful
            $rc = 0;
            last;
        } elsif ($operation_state eq "FAILED") {
            # instantiation failed
            $rc = 1;
            last;
        } elsif (time() >= $max_time) {
            # Maximum time to wait to finish has expired
            $last_error_details = "Maximum time ($monitor_timeout seconds) for $type to finish has expired (" . ( caller(0) )[3] . " line " . __LINE__ . ")";

            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            $rc = 2;
            last;
        }

        # Wait 10 seconds before checking status again
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => 0,
                "progress-message"  => 0,
                "seconds"           => 10,
            }
        );
        if ($rc == 1) {
            # User pressed CTRL-C to interrupt the sleep
            $last_error_details = "$type status check failed because the user interrupted it by pressing CTRL-C (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            $rc = 1;
            last;
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Create a JSESSIONID cookie to be used for all Onboarding Service and Life
# Cycle Management Service requests.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "password":
#           Specifies the password to be used.
#       "username":
#           Specifies the user name to be used.
#
# Return values:
#    - uuid string: The request was successful and a UUID string is returned to
#      the caller as a JSESSIONID to be used for most EVNFM operations.
#    - "": An empty string is returned if the request failed for some reason.
#
# -----------------------------------------------------------------------------
sub create_session_id {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $password                = exists $params{"password"} ? $params{"password"} : "";
    my $username                = exists $params{"username"} ? $params{"username"} : "";

    my $rc = 0;
    my @result;

    if ($ingress_host_address eq "") {
        $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if ($password eq "") {
        $last_error_details = "No 'password' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if ($username eq "") {
        $last_error_details = "No 'username' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"           => "curl -X POST -H 'Content-Type: application/json' -H 'X-login: $username' -H 'X-password: $password' https://$ingress_host_address/auth/v1 $insecure --silent --show-error $insecure",
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if (scalar @result == 1 && $result[0] =~ /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i) {
        # A successful execution looks like this:
        # curl -X POST -H "Content-Type: application/json" -H "X-login: vnfm-user" -H 'X-password: DefaultP12345!' https://evnfm02.eoaas.n17.sc.sero.gic.ericsson.se/auth/v1 $insecure
        # 8192d521-64ed-4db1-bf1c-9666a3e07168
        #
        # And an unsuccessful printout looks something like this:
        # curl -X POST -H "Content-Type: application/json" -H "X-login: vnfm-user" -H 'X-password: invalidpsw' https://evnfm02.eoaas.n17.sc.sero.gic.ericsson.se/auth/v1 $insecure
        # {"timestamp":"2021-10-05T16:12:17.820+0000","path":"/auth/v1","status":500,"error":"Internal Server Error","message":"[invalid_grant] Invalid user credentials","requestId":"c501a382-1715710"}
        #
        # In both cases $? returns back 0, so I cannot trust the return code from the curl command.

        # Save information in case it's needed for other calls
        $latest_session_id = $result[0];
        $session_info{$latest_session_id}{'create-epoch'} = time();
        $session_info{$latest_session_id}{'ingress-host-address'} = $ingress_host_address;
        $session_info{$latest_session_id}{'password'} = $password;
        $session_info{$latest_session_id}{'username'} = $username;
        return $latest_session_id;
    } else {
        $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
}

# -----------------------------------------------------------------------------
# Description....
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "instance-name":
#           Specifies the name to use for the instance, if not specified then
#           the default value 'daft-evnf-trial' will be used.
#       "package-vnfdid":
#           Specifies the package vnfdId to use for creating the VNF instance id.
#           This parameter is mandatory.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#    - uuid string: The request was successful and a UUID string is returned to
#      the caller as an VNF instance id to be used for e.g. instantiate operation.
#    - "": An empty string is returned if the request failed for some reason.
#
# -----------------------------------------------------------------------------
sub create_vnf_instance_id {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $instance_name           = exists $params{"instance-name"} ? $params{"instance-name"} : "daft-evnf-trial";
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";
    my $package_vnfdid          = exists $params{"package-vnfdid"} ? $params{"package-vnfdid"} : "";

    my $curl_command = "";
    my $rc = 0;
    my @result;

    if ($package_vnfdid eq "") {
        # No package vnfdid specified, report an error
        $last_error_details = "No 'package-vnfdid' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    $curl_command = "curl -X POST https://$ingress_host_address/vnflcm/v1/vnf_instances -H 'Accept: application/json' -H 'Content-Type: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";
    $curl_command .= sprintf " -d '{\"vnfdId\": \"'%s'\",\"vnfInstanceName\": \"'%s'\"}'", $package_vnfdid, $instance_name;
    $curl_command .= " | jq -r '.id'";

    my $tries=0;
    while ($tries < 60) {
        $rc = General::OS_Operations::send_command(
            {
                "command"           => "$curl_command",
                "hide-output"       => $hide_output,
                "return-output"     => \@result,
            }
        );
        if ($rc != 0) {
            $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
        if (scalar @result == 1 && $result[0] =~ /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i) {
            # A successful execution looks like this:
            # curl -X POST -H "Content-Type: application/json" -H "X-login: vnfm-user" -H 'X-password: DefaultP12345!' https://evnfm02.eoaas.n17.sc.sero.gic.ericsson.se/auth/v1 $insecure
            # 8192d521-64ed-4db1-bf1c-9666a3e07168
            #
            # And an unsuccessful printout looks something like this:
            # curl -X POST -H "Content-Type: application/json" -H "X-login: vnfm-user" -H 'X-password: invalidpsw' https://evnfm02.eoaas.n17.sc.sero.gic.ericsson.se/auth/v1 $insecure
            # {"timestamp":"2021-10-05T16:12:17.820+0000","path":"/auth/v1","status":500,"error":"Internal Server Error","message":"[invalid_grant] Invalid user credentials","requestId":"c501a382-1715710"}
            #
            # In both cases $? returns back 0, so I cannot trust the return code from the curl command.

            # Save information in case it's needed for other calls
            $latest_instance_id = $result[0];
            return $latest_instance_id;
        } else {
            $tries++;
            if ($tries != 60) {
                General::Logging::log_user_message("Creating VNF instance failed, retrying for maximum 1 hour (Retry attempt: $tries)");
                $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . "). The command will be retried (Retry: $tries)";
                General::Logging::log_write("$last_error_details\n") if $debug_messages;
                # Wait 60 seconds before sending curl command again
                $rc = General::OS_Operations::sleep_with_progress(
                    {
                        "allow-interrupt"   => 1,
                        "confirm-interrupt" => 0,
                        "progress-message"  => 0,
                        "seconds"           => 60,
                    }
                );
                if ($rc == 1) {
                    # User pressed CTRL-C to interrupt the sleep
                    $last_error_details = "Creating vnf instance id failed because the user interrupted it by pressing CTRL-C (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                    General::Logging::log_write("$last_error_details\n") if $debug_messages;
                    $rc = 1;
                    last;
                }
            }
        }
    }
    $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
    General::Logging::log_write("$last_error_details\n") if $debug_messages;
    return "";
}

# -----------------------------------------------------------------------------
# Create a VNF package identifier.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "data":
#           If specified then it should contain a properly formatted data
#           parameter. For example:
#           "data" => '{"userDefinedData":{"description": "testing CreateVnfPkgInfoRequest"}}'
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#    - uuid string: The request was successful and a UUID string is returned to
#      the caller as a package id to be used for most EVNFM onboarding operations.
#    - "": An empty string is returned if the request failed for some reason.
#
# -----------------------------------------------------------------------------
sub create_vnf_package_id {
    my %params = %{$_[0]};

    # Initialize local variables
    my $data                    = exists $params{"data"} ? $params{"data"} : "";
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";

    my $curl_command = "";
    my $rc = 0;
    my @result;

    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    $curl_command = "curl -X POST https://$ingress_host_address/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";

    if ($data ne "") {
        $curl_command .= " -d '$data'";
    }

    $curl_command .= " | jq -r '.id'";

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if (scalar @result == 1 && $result[0] =~ /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i) {
        # A successful execution looks like this:
        # curl -X POST https://evnfm02.eoaas.n17.sc.sero.gic.ericsson.se/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{"userDefinedData":{"description": "testing CreateVnfPkgInfoRequest"}}' -H 'cookie:JSESSIONID=8192d521-64ed-4db1-bf1c-9666a3e07168' | jq -r '.id'
        # 1f313c5e-f1c6-4c99-a036-0b103d1f87d3
        #
        # And an unsuccessful printout looks something like this:
        # {"timestamp":"2021-10-05T16:12:17.820+0000",......
        #
        # In both cases $? returns back 0, so I cannot trust the return code from the curl command.

        # Save information in case it's needed for other calls
        $latest_package_id = $result[0];
        return $latest_package_id;
    } else {
        $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
}

# -----------------------------------------------------------------------------
# Description....
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "instance-name":
#           Specifies the name to use for the instance, if not specified then
#           the default value 'daft-evnf-trial' will be used.
#       "package-vnfdid":
#           Specifies the package vnfdId to use for creating the VNF instance id.
#           This parameter is mandatory.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#    - 0: Delete was successful
#    - 1: Delete failed for some reason.
#
# -----------------------------------------------------------------------------
sub delete_vnf_instance_id {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $instance_id             = exists $params{"instance-id"} ? $params{"instance-id"} : "";
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";

    my $curl_command = "";
    my $json_ref;
    my %instance_delete_info;
    my $rc = 0;
    my @result;

    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    if ($instance_id eq "") {
        # No session id was previous created, report an error
        $last_error_details = "No 'instance-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return 1;
    } else {
        $curl_command = "curl -X DELETE https://$ingress_host_address/vnflcm/v1/vnf_instances/$instance_id -H 'Content-Type: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return 1;
    }
    if (@result) {
        General::Logging::log_user_message("Delete instance ($instance_id) result:");
        foreach(@result) {
            General::Logging::log_user_message("   $_");
        }
        return 1;
    } else {
        General::Logging::log_user_message("Delete instance result is empty, considered as OK");
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Delete an existing VNF package.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "package-id":
#           Specifies the package id to delete. If not specified then the
#           previously created package id from the call to the
#           'create_vnf_package_id' call will be used, if that also does not
#           exists then an error is reported.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#    - "": An empty string is returned if the deletion was successful.
#    - Any other returned value indicates an error and the returned data is
#      the error message.
#
# -----------------------------------------------------------------------------
sub delete_vnf_package {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";
    my $package_id              = exists $params{"package-id"} ? $params{"package-id"} : "";

    my $curl_command = "";
    my $rc = 0;
    my @result;

    if ($package_id eq "") {
        # No package id specified, check if a package id was previously created
        if ($latest_package_id ne "") {
            $package_id = $latest_package_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'package-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    $curl_command = "curl -X DELETE https://$ingress_host_address/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages/$package_id -H 'Accept: */*' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if (scalar @result == 0) {
        # A successful execution should not return anything

        # Save information in case it's needed for other calls
        $latest_package_id = "";
        return "";
    } else {
        $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return join "\n", @result;
    }
}

# -----------------------------------------------------------------------------
# Get information about existing VNF instances.
# The returned data is a perl structure of decoded JSON data either as a
# reference to an array or hash.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "package-id":
#           If specified it will only fetch information about the specified
#           package and return it to the caller.
#           If not specified then data about all existing packages are
#           returned to the caller.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#   The data as a reference to a hash or undef if failure or if no instances
#   found.
# -----------------------------------------------------------------------------
sub get_vnf_instance_info {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $instance_id             = exists $params{"instance-id"} ? $params{"instance-id"} : "";
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";

    my $curl_command = "";
    my $json_ref;
    my %packages;
    my $rc = 0;
    my @result;

    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    if ($instance_id eq "") {
        $curl_command = "curl -X GET https://$ingress_host_address/vnflcm/v1/vnf_instances -H 'Content-Type: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";
    } else {
        $curl_command = "curl -X GET https://$ingress_host_address/vnflcm/v1/vnf_instances/$instance_id -H 'Content-Type: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    $json_ref = General::Json_Operations::read_array_return_reference( { "input" => \@result } );

    if (ref($json_ref) eq "HASH") {
        # HASH Reference
        if (exists $json_ref->{'id'}) {
            $packages{$json_ref->{'id'}} = $json_ref;
        } else {
            $last_error_details = "Could not find the key 'id' in the returned hash, returning 'undef' (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return undef;
        }
    } elsif(ref($json_ref) eq "ARRAY") {
        # ARRAY Reference
        for my $data (@$json_ref) {
            if (ref($data) eq "HASH" && exists $data->{'id'}) {
                #print "DBG:1\n" . Dumper($data);
                $packages{$data->{'id'}} = $data;
            } elsif (ref($data) eq "ARRAY") {
                #print "DBG:2\n";
                $last_error_details = "Not supported data type array of arrays, array data ignored (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                General::Logging::log_write("$last_error_details\n") if $debug_messages;
            }
        }
    } else {
        $last_error_details = "Either no instances to print or something went wrong when fetching instance information, returning 'undef' (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return undef;
    }

    return \%packages;
}

# -----------------------------------------------------------------------------
# Get information about existing VNF packages.
# The returned data is a perl structure of decoded JSON data either as a
# reference to an array or hash.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "package-id":
#           If specified it will only fetch information about the specified
#           package and return it to the caller.
#           If not specified then data about all existing packages are
#           returned to the caller.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#   The data as a reference to a hash or undef if failure or if no packages
#   found.
# -----------------------------------------------------------------------------
sub get_vnf_package_info {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";
    my $package_id              = exists $params{"package-id"} ? $params{"package-id"} : "";

    my $curl_command = "";
    my $json_ref;
    my %packages;
    my $rc = 0;
    my @result;

    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    if ($package_id eq "") {
        $curl_command = "curl -X GET https://$ingress_host_address/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages -H 'Content-Type: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";
    } else {
        $curl_command = "curl -X GET https://$ingress_host_address/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages/$package_id -H 'Content-Type: application/json' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    $json_ref = General::Json_Operations::read_array_return_reference( { "input" => \@result } );

    if (ref($json_ref) eq "HASH") {
        # HASH Reference
        if (exists $json_ref->{'id'}) {
            $packages{$json_ref->{'id'}} = $json_ref;
        } else {
            $last_error_details = "Could not find the key 'id' in the returned hash, returning 'undef' (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return undef;
        }
    } elsif(ref($json_ref) eq "ARRAY") {
        # ARRAY Reference
        for my $data (@$json_ref) {
            if (ref($data) eq "HASH" && exists $data->{'id'}) {
                #print "DBG:1\n" . Dumper($data);
                $packages{$data->{'id'}} = $data;
            } elsif (ref($data) eq "ARRAY") {
                #print "DBG:2\n";
                $last_error_details = "Not supported data type array of arrays, array data ignored (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                General::Logging::log_write("$last_error_details\n") if $debug_messages;
            }
        }
    } else {
        $last_error_details = "Either no packages to print or something went wrong when fetching package information, returning 'undef' (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return undef;
    }

    return \%packages;
}

# -----------------------------------------------------------------------------
# Get information about existing VNF instances.
# The returned data is a perl structure of decoded JSON data either as a
# reference to an array or hash.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "package-id":
#           If specified it will only fetch information about the specified
#           package and return it to the caller.
#           If not specified then data about all existing packages are
#           returned to the caller.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#   The data as a reference to a hash or undef if failure or if no instances
#   found.
# -----------------------------------------------------------------------------
sub get_namespaceInfo {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $cism_id                 = exists $params{"cism-id"} ? $params{"cism-id"} : "";
    my $id                      = exists $params{"id"} ? $params{"id"} : "";
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";

    my $curl_command = "";
    my $json_ref;
    my %packages;
    my $rc = 0;
    my @result;

    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if (($id eq "") && ($cism_id eq "")) {
        # No id specified, report an error
        $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    if ($cism_id ne "") {
        $curl_command = "curl -X GET https://$ingress_host_address/vnflcm/namespaces ".
                            "-H 'cookie:JSESSIONID=$session_id' ".
                            "-H 'Content-Type: application/json' ".
                            "-d '{\"cismId\": \"$cism_id\" }' ".
                            "--silent --show-error $insecure";
    } else {
        $curl_command = "curl -X GET https://$ingress_host_address/vnflcm/v1/namespaces/$id ".
                            "-H 'cookie:JSESSIONID=$session_id' ".
                            "-H 'Content-Type: application/json' ".
                            "--silent --show-error $insecure";

    }

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    $json_ref = General::Json_Operations::read_array_return_reference( { "input" => \@result } );

    if (ref($json_ref) eq "HASH") {
        # HASH Reference
        if (exists $json_ref->{'id'}) {
            $packages{$json_ref->{'id'}} = $json_ref;
        } else {
            $last_error_details = "Could not find the key 'id' in the returned hash, returning 'undef' (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return undef;
        }
    } elsif(ref($json_ref) eq "ARRAY") {
        # ARRAY Reference
        for my $data (@$json_ref) {
            if (ref($data) eq "HASH" && exists $data->{'id'}) {
                #print "DBG:1\n" . Dumper($data);
                $packages{$data->{'id'}} = $data;
            } elsif (ref($data) eq "ARRAY") {
                #print "DBG:2\n";
                $last_error_details = "Not supported data type array of arrays, array data ignored (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                General::Logging::log_write("$last_error_details\n") if $debug_messages;
            }
        }
    } else {
        $last_error_details = "Either no instances to print or something went wrong when fetching instance information, returning 'undef' (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return undef;
    }

    return \%packages;
}

# -----------------------------------------------------------------------------
# Instantiate the VNF.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "instance-id":
#           Specifies the instance id to instantiate. If not specified then the
#           previously created instance id from the call to the
#           'create_vnf_instance_id' call will be used, if that also does not
#           exists then an error is reported.
#       "instantiate-file":
#           Specifies the file containing information about the node and
#           namespace etc. to be used for the instantiation of the SC software.
#       "monitor-timeout":
#           If specified then the monitoring of the onboarding status will
#           continue for a maximum of the specified number of seconds.
#           If not specified then the default maximum time is 7200 seconds
#           or about 2 hours.
#       "no-monitoring":
#           If specified and =1 then no monitoring of successful onboarding
#           will take place, only starting of the onboarding will be done and
#           it's up to the called to monitor for successful onboarding status
#           e.g. by calling the check_onboarding_status subroutine themselves.
#       "progress-messages":
#           If specified and =1 then progress messages are written to STDOUT
#           and if =0 or not specified then no progress message are written
#           during the onboarding procedure.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#       "values-file":
#           Specifies the file containing node specific values about the
#           software to be instantiated (eric-sc-values.yaml file).
#
# Return values:
#    - <url to monitor instantiate job>: The instantiation either started or
#      was successful.
#    - "": An empty string is returned when the instantiation failed for
#      some reason.
#
# -----------------------------------------------------------------------------
sub instantiate_vnf {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $instance_id             = exists $params{"instance-id"} ? $params{"instance-id"} : "";
    my $instantiate_file        = exists $params{"instantiate-file"} ? $params{"instantiate-file"} : "";
    my $monitor_timeout         = exists $params{"monitor-timeout"} ? $params{"monitor-timeout"} : 7200;
    my $no_monitoring           = exists $params{"no-monitoring"} ? $params{"no-monitoring"} : 0;
    my $progress_messages       = exists $params{"progress-messages"} ? $params{"progress-messages"} : 0;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";
    my $values_file             = exists $params{"values-file"} ? $params{"values-file"} : "";

    my $curl_command = "";
    my $max_time;
    my $rc = 0;
    my @result;
    my $url;

    if ($instance_id eq "") {
        # No package id specified, check if a package id was previously created
        if ($latest_instance_id ne "") {
            $instance_id = $latest_instance_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'instance-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }
    if ($instantiate_file eq "") {
        # No instantiate information file name provided, report an error
        $last_error_details = "No 'instantiate-file' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if ($values_file eq "") {
        # No values file name provided, report an error
        $last_error_details = "No 'values-file' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    $curl_command = "curl -i -X POST https://$ingress_host_address/vnflcm/v1/vnf_instances/$instance_id/instantiate -H 'Content-Type: multipart/form-data' -H 'cookie:JSESSIONID=$session_id' -F instantiateVnfRequest=\@$instantiate_file -F valuesFile=\@$values_file --silent --show-error $insecure";

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if (scalar @result > 0) {
        # Check that an 202 HTTP answer is received
        my $return_code = -1;
        for (@result) {
            if (/^HTTP\S*\s+(\d+)/) {
                $return_code = $1;
                if ($return_code != 202) {
                    $last_error_details = "Curl command failed with HTTP response code $return_code (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                    General::Logging::log_write("$last_error_details\n") if $debug_messages;
                    return "";
                }
            } elsif (/^location:\s+(https:.+)/) {
                $url = $1;
            }
        }
        unless ($return_code == 202 && $url ne "") {
            $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    } else {
        $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    if ($no_monitoring) {
        # We return success even though the onboarding might not be finished.
        return $url;
    } else {
        # Start the monitoring
        $rc = check_operation_status(
            {
                "debug-messages"        =>  $debug_messages,
                "hide-output"           =>  $hide_output,
                "progress-messages"     =>  $progress_messages,
                "session-id"            =>  $session_id,
                "monitor-timeout"       =>  $monitor_timeout,
                "type"                  =>  "instantiation",
                "url"                   =>  $url,
            }
        );
        if ($rc == 0) {
            return $url;
        } else {
            return "";
        }
    }
}

# -----------------------------------------------------------------------------
# Onboard a CSAR package.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "file":
#           Specifies the CSAR file to be onboarded.
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "monitor-timeout":
#           If specified then the monitoring of the onboarding status will
#           continue for a maximum of the specified number of seconds.
#           If not specified then the default maximum time is 7200 seconds
#           or about 2 hours.
#       "no-monitoring":
#           If specified and =1 then no monitoring of successful onboarding
#           will take place, only starting of the onboarding will be done and
#           it's up to the called to monitor for successful onboarding status
#           e.g. by calling the check_onboarding_status subroutine themselves.
#       "package-id":
#           Specifies the package id to delete. If not specified then the
#           previously created package id from the call to the
#           'create_vnf_package_id' call will be used, if that also does not
#           exists then an error is reported.
#       "progress-messages":
#           If specified and =1 then progress messages are written to STDOUT
#           and if =0 or not specified then no progress message are written
#           during the onboarding procedure.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#    - 0: The onboarding was successful.
#    - 1: The onboarding failed for some reason.
#
# -----------------------------------------------------------------------------
sub onboard_vnf_package {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $file                    = exists $params{"file"} ? $params{"file"} : "";
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $monitor_timeout         = exists $params{"monitor-timeout"} ? $params{"monitor-timeout"} : 7200;
    my $no_monitoring           = exists $params{"no-monitoring"} ? $params{"no-monitoring"} : 0;
    my $package_id              = exists $params{"package-id"} ? $params{"package-id"} : "";
    my $progress_messages       = exists $params{"progress-messages"} ? $params{"progress-messages"} : 0;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";

    my $curl_command = "";
    my $max_time;
    my $rc = 0;
    my @result;

    if ($package_id eq "") {
        # No package id specified, check if a package id was previously created
        if ($latest_package_id ne "") {
            $package_id = $latest_package_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'package-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }
    if ($file eq "") {
        # No CSAR file name provided, report an error
        $last_error_details = "No 'file' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return 1;
    }

    $curl_command = "curl -i -X PUT https://$ingress_host_address/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages/$package_id/package_content -H 'Accept: */*' -H 'Content-Type: multipart/form-data' -H 'cookie:JSESSIONID=$session_id' -F file=\@$file --silent --show-error $insecure";

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return 1;
    }
    if (scalar @result > 0) {
        # Check that an 202 HTTP answer is received
        my $return_code = -1;
        for (@result) {
            if (/^HTTP\S*\s+(\d+)/) {
                $return_code = $1;
                if ($return_code == 202) {
                    # Expected answer returned
                    last;
                } else {
                    $last_error_details = "Curl command failed with HTTP response code $return_code (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                    General::Logging::log_write("$last_error_details\n") if $debug_messages;
                    return 1;
                }
            }
        }
        if ($return_code == -1) {
            $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return 1;
        }
    } else {
        $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return 1;
    }

    if ($no_monitoring) {
        # We return success even though the onboarding might not be finished.
        $rc = 0;;
    } else {
        # Start the monitoring
        $rc = check_onboarding_status(
            {
                "debug-messages"        =>  $debug_messages,
                "ingress-host-address"  =>  $ingress_host_address,
                "hide-output"           =>  $hide_output,
                "package-id"            =>  $package_id,
                "progress-messages"     =>  $progress_messages,
                "session-id"            =>  $session_id,
                "monitor-timeout"       =>  $monitor_timeout,
            }
        );
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Change the VNF.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "aspectid":
#           Specifies the scale aspect identifier to use, the currently known
#           and supported types for the SC application are:
#           - scp_worker_scaling
#           - sepp_worker_scaling
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "instance-id":
#           Specifies the instance id to scale. If not specified then the
#           previously created instance id from the call to the
#           'create_vnf_instance_id' call will be used, if that also does not
#           exists then an error is reported.
#       "monitor-timeout":
#           If specified then the monitoring of the onboarding status will
#           continue for a maximum of the specified number of seconds.
#           If not specified then the default maximum time is 7200 seconds
#           or about 2 hours.
#       "no-monitoring":
#           If specified and =1 then no monitoring of successful onboarding
#           will take place, only starting of the onboarding will be done and
#           it's up to the called to monitor for successful onboarding status
#           e.g. by calling the check_onboarding_status subroutine themselves.
#       "progress-messages":
#           If specified and =1 then progress messages are written to STDOUT
#           and if =0 or not specified then no progress message are written
#           during the onboarding procedure.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#       "steps":
#           Specifies the number of steps to scale in our out, if not specified
#           then the default value will be used which is 1.
#       "type":
#           Specifies the type of scaling to perform, the supported types are:
#           - SCALE_IN
#           - SCALE_OUT
#
# Return values:
#    - <url to monitor scale job>: The scale either started or was successful.
#    - "": An empty string is returned when the scaling failed for
#      some reason.
#
# -----------------------------------------------------------------------------
sub scale {
    my %params = %{$_[0]};

    # Initialize local variables
    my $aspectid                = exists $params{"aspectid"} ? $params{"aspectid"} : "";
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $instance_id             = exists $params{"instance-id"} ? $params{"instance-id"} : "";
    my $monitor_timeout         = exists $params{"monitor-timeout"} ? $params{"monitor-timeout"} : 7200;
    my $no_monitoring           = exists $params{"no-monitoring"} ? $params{"no-monitoring"} : 0;
    my $progress_messages       = exists $params{"progress-messages"} ? $params{"progress-messages"} : 0;
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";
    my $steps                   = exists $params{"steps"} ? $params{"steps"} : 1;
    my $type                    = exists $params{"type"} ? $params{"type"} : "";

    my $curl_command = "";
    my $max_time;
    my $rc = 0;
    my @result;
    my $url;

    if ($instance_id eq "") {
        # No package id specified, check if a package id was previously created
        if ($latest_instance_id ne "") {
            $instance_id = $latest_instance_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'instance-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }
    if ($type eq "") {
        # No scaling type provided, report an error
        $last_error_details = "No 'type' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if ($aspectid eq "") {
        # No scaling type provided, report an error
        $last_error_details = "No 'aspectid' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    $curl_command = "curl -i -X POST https://$ingress_host_address/vnflcm/v1/vnf_instances/$instance_id/scale -H 'Content-Type: application/json' -d '{\"type\": \"$type\", \"aspectId\": \"$aspectid\", \"numberOfSteps\": $steps}' -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if (scalar @result > 0) {
        # Check that an 202 HTTP answer is received
        my $return_code = -1;
        for (@result) {
            if (/^HTTP\S*\s+(\d+)/) {
                $return_code = $1;
                if ($return_code != 202) {
                    $last_error_details = "Curl command failed with HTTP response code $return_code (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                    General::Logging::log_write("$last_error_details\n") if $debug_messages;
                    return "";
                }
            } elsif (/^location:\s+(https:.+)/) {
                $url = $1;
            }
        }
        unless ($return_code == 202 && $url ne "") {
            $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    } else {
        $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    if ($no_monitoring) {
        # We return success even though the onboarding might not be finished.
        return $url;
    } else {
        # Start the monitoring
        $rc = check_operation_status(
            {
                "debug-messages"        =>  $debug_messages,
                "hide-output"           =>  $hide_output,
                "progress-messages"     =>  $progress_messages,
                "session-id"            =>  $session_id,
                "monitor-timeout"       =>  $monitor_timeout,
                "type"                  =>  "scaling",
                "url"                   =>  $url,
            }
        );
        if ($rc == 0) {
            return $url;
        } else {
            return "";
        }
    }
}

# -----------------------------------------------------------------------------
# Terminate the VNF.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "cleanup-resources":
#           Specifies if the resources should be cleaned up (=true) or not (=false).
#       "debug-messages":
#           If specified then debug messages are written to the log file (if
#           open).
#       "hide-output":
#           If specified and =1 then no output if shown when executing the
#           request, this is also the default if not specified. If specified
#           and =0 then all output is shown to the user.
#       "ingress-host-address":
#           Specifies the ingress host address of the EVNFM node to be used for
#           all curl requests. If not specified then the previously used
#           ingress-host-address from the 'create_session_id' call will be used.
#       "instance-id":
#           Specifies the instance id to instantiate. If not specified then the
#           previously created instance id from the call to the
#           'create_vnf_instance_id' call will be used, if that also does not
#           exists then an error is reported.
#       "terminate-method":
#           Specifies the method to use for the termination.
#           Can be 'GRACEFUL' or 'FORCEFUL'.
#       "session-id":
#           Specifies the session id to use for the request. If not specified
#           then the previously created session id from the call to the
#           'create_session_id' call will be used, if that also does not
#           exists then an error is reported.
#
# Return values:
#    - <url to monitor instantiate job>: The instantiation either started or
#      was successful.
#    - "": An empty string is returned when the instantiation failed for
#      some reason.
#
# -----------------------------------------------------------------------------
sub terminate_vnf {
    my %params = %{$_[0]};

    # Initialize local variables
    my $cleanup_resources       = exists $params{"cleanup-resources"} ? $params{"cleanup-resources"} : "true";
    my $debug_messages          = exists $params{"debug-messages"} ? 1 : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 1;
    my $ingress_host_address    = exists $params{"ingress-host-address"} ? $params{"ingress-host-address"} : "";
    my $instance_id             = exists $params{"instance-id"} ? $params{"instance-id"} : "";
    my $session_id              = exists $params{"session-id"} ? $params{"session-id"} : "";
    my $method                  = exists $params{"terminate-method"} ? $params{"terminate-method"} : "GRACEFUL";

    my $curl_command = "";
    my $max_time;
    my $rc = 0;
    my @result;
    my $url;

    if ($instance_id eq "") {
        # No package id specified, check if a package id was previously created
        if ($latest_instance_id ne "") {
            $instance_id = $latest_instance_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'instance-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($session_id eq "") {
        # No session id specified, check if a session id was previously created
        if ($latest_session_id ne "") {
            $session_id = $latest_session_id;
        } else {
            # No session id was previous created, report an error
            $last_error_details = "No 'session-id' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($ingress_host_address eq "") {
        # No address provided, check if we can reuse the address from the session id.
        if (exists $session_info{$session_id} && $session_info{$session_id}{'ingress-host-address'} ne "") {
            $ingress_host_address = $session_info{$session_id}{'ingress-host-address'};
        } else {
            $last_error_details = "No 'ingress-host-address' specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    }
    if ($hide_output) {
        # Always convert specified value (other than 0) to value 1.
        $hide_output = 1;
    }

    #
    # Prepare the TerminateVnfRequest.json file, which serves as the request body for the operation
    #   curl -v -X POST -H "Content-Type: application/json" -u <http_user> --data @TerminateVnfRequest.json <vevnfm_api_root>/vnf_instances/<vnf id>/terminate
    # Save the <operation_url> contained in the response Location header
    # Monitor the operation status using a notification status listener or by periodically polling the <operation_url> received in the response Location header
    #   curl -v -u <http_user> <operation_url>
    #
    $curl_command = "curl -i -X POST https://$ingress_host_address/vnflcm/v1/vnf_instances/$instance_id/terminate".
                    " -H 'Content-Type: application/json'".
                    " -d '{\"terminationType\": \"$method\", \"additionalParams\": {\"cleanUpResources\": \"$cleanup_resources\", \"applicationTimeOut\": \"900\", \"skipJobVerification\" : \"True\"} }'".
                    " -H 'cookie:JSESSIONID=$session_id' --silent --show-error $insecure";

    $rc = General::OS_Operations::send_command(
        {
            "command"           => $curl_command,
            "hide-output"       => $hide_output,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        $last_error_details = "Curl request failed with rc=$rc (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }
    if (scalar @result > 0) {
        # Check that an 202 HTTP answer is received
        my $return_code = -1;
        for (@result) {
            if (/^HTTP\S*\s+(\d+)/) {
                $return_code = $1;
                if ($return_code != 202) {
                    $last_error_details = "Curl command failed with HTTP response code $return_code (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
                    General::Logging::log_write("$last_error_details\n") if $debug_messages;
                    return "FAILED: $return_code";
                }
            } elsif (/^location:\s+(https:.+)/) {
                $url = $1;
            }
        }
        unless ($return_code == 202 && $url ne "") {
            $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
            General::Logging::log_write("$last_error_details\n") if $debug_messages;
            return "";
        }
    } else {
        $last_error_details = "Curl command did not return the expected result (" . ( caller(0) )[3] . " line " . __LINE__ . ")";
        General::Logging::log_write("$last_error_details\n") if $debug_messages;
        return "";
    }

    if ($rc == 0) {
        return $url;
    } else {
        return "";
    }
}

1;

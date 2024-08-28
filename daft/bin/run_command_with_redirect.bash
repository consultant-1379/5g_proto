#!/bin/bash

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2020-12-04 18:17:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

#-------------------------------------------------------------------------------
# Execute a command with possible redirect of STDOUT and STDERR to either
# /dev/null or to a file.
# This script is used to trick the Perl exec, `` or system calls that sometimes
# executes the command without a shell and sometimes with a shell which can
# result in "hanging" processes if running the commands with forking.
# Since we here rely on that bash is executing the command we should bypass
# these "hanging" issues.
#
# This script will execute the specified command, replacing the process id used
# for calling this script.
#
# Call this script with at least 3 parameters where the different parameters are
# as follows.
#
# The value for the first parameter is one of:
#   - no_redirect: No redirect will be done of STDOUT and it will be sent to the
#     standard file handle.
#   - discard: Discard STDOUT by redirecting it to /dev/null.
#   - to_file: Redirect STDOUT to a file which is specified in the second
#     parameter.
#
# The value for the second (or third) parameter is one of:
#   - no_redirect: No redirect will be done of STDERR and it will be sent to the
#     standard file handle.
#   - discard: Discard STDERR by redirecting it to /dev/null.
#   - to_file: Redirect STDERR to a file which is specified in the second
#     parameter.
#   - to_stdout: Redirect STDERR to same place as STDOUT.
#
# The value for the third (or fourth) parameter is the command to execute.
#
# Any other parameters are parameters for the command.
#
# Example 1: No redirection at all of STDOUT and STDERR.
#
#   ./run_command_with_redirect.bash no_redirect no_redirect command_to_run param1 param2
#
# Example 2: Redirect STDOUT to /dev/null.
#
#   ./run_command_with_redirect.bash discard no_redirect command_to_run param1 param2
#
# Example 3: Redirect STDERR to /dev/null.
#
#   ./run_command_with_redirect.bash no_redirect discard command_to_run param1 param2
#
# Example 2: Redirect STDOUT and STDERR to a file.
#
#   ./run_command_with_redirect.bash to_file /path/to/log_file to_stdout command_to_run param1 param2
#-------------------------------------------------------------------------------

redirect_stdout=$1
shift
if [ -z $redirect_stdout ]; then
    printf "\nYou must specify at least 3 parameters.\n"
    exit 1
fi
if [ $redirect_stdout == "no_redirect" ]; then
    extra_parameters=""
elif [ $redirect_stdout == "discard" ]; then
    extra_parameters=" >/dev/null"
elif [ $redirect_stdout == "to_file" ]; then
    stdout_filename=$1
    shift
    extra_parameters=" >$stdout_filename"
else
    printf "\nIncorrect value for first parameter\n"
    exit 1;
fi

redirect_stderr=$1
shift
if [ -z $redirect_stderr ]; then
    printf "\nYou must specify at least 3 parameters.\n"
    exit 1
fi
if [ $redirect_stderr == "no_redirect" ]; then
    extra_parameters+=""
elif [ $redirect_stderr == "discard" ]; then
    extra_parameters+=" 2>/dev/null"
elif [ $redirect_stderr == "to_file" ]; then
    stderr_filename=$1
    shift
    extra_parameters=" 2>$stderr_filename"
elif [ $redirect_stderr == "to_stdout" ]; then
    extra_parameters+=" 2>&1"
else
    printf "\nIncorrect value for second or third parameter\n"
    exit 1;
fi

commandstr=$1
if [ -z $redirect_stderr ]; then
    printf "\nYou must specify a command to execute.\n"
    exit 1
fi

if [ $redirect_stdout == "no_redirect" ]; then

    if [ $redirect_stderr == "no_redirect" ]; then
        exec $command $@

    elif [ $redirect_stderr == "discard" ]; then
        exec $command $@ 2>/dev/null

    elif [ $redirect_stderr == "to_file" ]; then
        exec $command $@ 2>$stderr_filename

    elif [ $redirect_stderr == "to_stdout" ]; then
        exec $command $@ 2>&1

    fi

elif [ $redirect_stdout == "discard" ]; then

    if [ $redirect_stderr == "no_redirect" ]; then
        exec $command $@ >/dev/null

    elif [ $redirect_stderr == "discard" ]; then
        exec $command $@ >/dev/null 2>/dev/null

    elif [ $redirect_stderr == "to_file" ]; then
        exec $command $@ >/dev/null 2>$stderr_filename

    elif [ $redirect_stderr == "to_stdout" ]; then
        exec $command $@ >/dev/null 2>&1

    fi

elif [ $redirect_stdout == "to_file" ]; then

    if [ $redirect_stderr == "no_redirect" ]; then
        exec $command $@ >$stdout_filename

    elif [ $redirect_stderr == "discard" ]; then
        exec $command $@ >$stdout_filename 2>/dev/null

    elif [ $redirect_stderr == "to_file" ]; then
        exec $command $@ >$stdout_filename 2>$stderr_filename

    elif [ $redirect_stderr == "to_stdout" ]; then
        exec $command $@ >$stdout_filename 2>&1

    fi

fi

exit

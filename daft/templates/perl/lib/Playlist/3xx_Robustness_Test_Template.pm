#REMOVE The following line must be changed to match the name of the file minus the
#REMOVE ".pm" file extension and adding the "package Playlist::"
#REMOVE in front and ending with a semicolon.
#REMOVE The "xx" in "3xx" should be replaced by an incrementing number of the
#REMOVE next avaibale number e.g. 302, 303 etc.
#REMOVE All Robustness files must also have the start "Robustness_Test_" in the name
#REMOVE or the pre and post health checks will not work properly.
#REMOVE So basically what you do is replace the "xx" with a number and replace
#REMOVE "Template" with a short decriptive name using an "_" to separate the words
#REMOVE for example "package Playlist::301_Robustness_Test_Timezone_Change;"
package Playlist::3xx_Robustness_Test_Template;

################################################################################
#
#REMOVE Add your user name instead of <username> below.
#  Author   : <username>
#
#REMOVE Update this revision number every time before you commit minor changes
#REMOVE by stepping the number after the dot, e.g. step from 1.0 to 1.1 etc. or
#REMOVE when making major not backwards compatible changes by stepping the number
#REMOVE before the dot, e.g. step from 1.0 to 2.0.
#REMOVE This update can also be done automatically by calling the following script
#REMOVE and passing in the path to the file:
#REMOVE 5g_proto/daft/perl/bin/update_revision_information.pl <path to file>
#  Revision : 1.1
#REMOVE Before pushing any changes change the date below to the current date in
#REMOVE suggested format e.g. 2021-11-04 17:48:00.
#REMOVE This update can also be done automatically by calling the following script
#REMOVE and passing in the path to the file:
#REMOVE 5g_proto/daft/perl/bin/update_revision_information.pl <path to file>
#  Date     : <yyyy-mm-dd hh:mm:ss>
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

#REMOVE This is a template file that can be used for creating new playlists for Robustness Tests.
#REMOVE It shows an example of what the file should look like and it gives you instructions
#REMOVE of what the different logic is needed for and what should be kept as-is and where you
#REMOVE should add your own code.
#REMOVE
#REMOVE By using this example file as a base you will save a lot of time when writing the robustness
#REMOVE test case because a lot of needed logic has already been provided for you like:
#REMOVE - Initializing the needed job variables.
#REMOVE - Performing pre health check of the node.
#REMOVE - Defining a number of usefull job variables that can be used by the user to control the
#REMOVE   execution and the input to the playlist so you don't need to add then and you can
#REMOVE   just concentrate on adding job variables that are special for your playlist.
#REMOVE - Collecting trouble shooting logs
#REMOVE - Example of what the test code logic that you add might look like.
#REMOVE - Performing post health check of the node.
#REMOVE - Cleaning up at the end of the job.
#REMOVE
#REMOVE The procedure for starting writing a new Robustness Test Playlist is as follows:
#REMOVE
#REMOVE     - Copy this file (5g_proto/daft/templates/perl/lib/Playlist/3xx_Robustness_Test_Template.pm)
#REMOVE       to the 5g_proto/daft/perl/lib/Playlist/ directory and naming it properly as described
#REMOVE       at the top of this file e.g. 5g_proto/daft/perl/lib/Playlist/302_Robustness_Test_My_Wonderful_Test.pm
#REMOVE
#REMOVE     - Edit this new file following all the "#REMOVE" instructions.
#REMOVE
#REMOVE     - In your editor you should replace all instances of the string "P3xx" in the file with the selected
#REMOVE       playlist number e.g. if your playlist will have the number 302 then you should replace it with the
#REMOVE       string "P302".
#REMOVE       In the VIM editor you could do:
#REMOVE         - Press the "Esc" key to go into command mode.
#REMOVE         - Press the ":" key to get to the command line.
#REMOVE         - Type the following text without the double quotes: "%s/P3xx/P302/g"
#REMOVE         - Press the ENTER key to execute the command.
#REMOVE
#REMOVE     - Save your changes.
#REMOVE
#REMOVE     - Check your playlist for syntax errors (not runtime errors) you can execute the following command:
#REMOVE
#REMOVE       5g_proto/daft/perl/bin/execute_playlist.pl -p 302_Robustness_Test_My_Wonderful_Test -h
#REMOVE
#REMOVE     - Test your playlist by executing it using the "execute_playlist.pl" script for
#REMOVE       example like this:
#REMOVE
#REMOVE       5g_proto/daft/perl/bin/execute_playlist.pl -p 302_Robustness_Test_My_Wonderful_Test -j "Test_Of_My_Wonderful_Playlist" -n 5g_proto/daft/network_config_files/<mynode.xml> -c dark ....
#REMOVE
#REMOVE       Where the .... means that you can add job specific variables by adding one or more "-v <jobvar>=<value>.
#REMOVE       By default the following job variables are provided to you by the helper playlist file
#REMOVE       "932_Test_Case_Common_Logic.pm":
#REMOVE         - DEBUG_PLAYLIST
#REMOVE         - HELM_TIMEOUT
#REMOVE         - HELM_VERSION
#REMOVE         - IGNORE_ALARMS
#REMOVE         - IGNORE_FAILED_HEALTH_CHECK
#REMOVE         - POD_STATUS_TIMEOUT
#REMOVE         - SC_NAMESPACE
#REMOVE         - SC_RELEASE_NAME
#REMOVE         - SKIP_COLLECT_LOGS
#REMOVE         - SKIP_PRE_HEALTHCHECK
#REMOVE         - SKIP_POST_HEALTHCHECK
#REMOVE         - SOFTWARE_DIR
#REMOVE
#REMOVE       For example to add a software directory path you would add:
#REMOVE         -v SOFTWARE_DIR=/path/to/directory/where/software_files/are_stored
#REMOVE       e.g.
#REMOVE         -v SOFTWARE_DIR=/proj/CBA_comp/esc/timeshiftbomb/1.5.0+200/
#REMOVE
#REMOVE     - When you have finished adding your own code and no longer need the special instructions
#REMOVE       in this file i.e. all lines starting with the string "#REMOVE" then you should delete
#REMOVE       them to make the file more readable.
#REMOVE       To delete them in a simple way all you need to do is execute the following shell command:
#REMOVE
#REMOVE         sed -i '/^\s*#REMOVE/d' <path to file>

#REMOVE Do not touch the following line because it makes Perl more strict to e.g. not allow
#REMOVE use of variables that has not been declared previously and it helps in trouble shooting
#REMOVE of problems.
use strict;

#REMOVE Do not touch the following line.
use Exporter qw(import);

#REMOVE Do not touch the following lines.
our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

#REMOVE Do not touch the following lines.
use ADP::Kubernetes_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::932_Test_Case_Common_Logic;
#REMOVE Add more "use" statements if you need to import other Perl library files.

#REMOVE Do not touch the following lines.
#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

#REMOVE The following line is useful to have in case you want to do a test run of the playlist without actually
#REMOVE changing anything on the cluster. It also depends on that you actually also implement the needed logic
#REMOVE to skip/echo commands that do changes to the cluster.
my $debug_command = "";         # Change this value from "echo " to "" when ready to test on real system

#REMOVE The following lines shows an example of a global playlist array variable that could for example contain
#REMOVE defaults values to use for repeating the test case multiple times with different input values.
#REMOVE These values can e.g. be overridden by job variables as shown in task "Check_Job_Parameters_P3xxS02T01".
#REMOVE If you have no need for such a variable then these lines can be deleted.
#REMOVE If you need such variable(s) then you might want to change the name to something more descriptive for
#REMOVE your test case.
# Timezones to use for each repetition, index 0 will store the original timezone
# which will be used to restore the node timezone back to this timezone after the
# last repetition of the timezone change.
# For timezone values see e.g. https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
my @timezones = ( "", "Asia/Beirut", "Europe/Moscow", "Asia/Shanghai" );

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

    #REMOVE The following line should be changed to replace the XXXX part with a short description what the playlist
    #REMOVE is used for e.g. ROBUSTNESS_TEST_TIMEZONE_CHANGE, ROBUSTNESS_TEST_K8S_WORKER_RESTART etc. and it should
    #REMOVE be in all UPPERCASE and MUST ALWAYS start with ROBUSTNESS_TEST_.
    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "ROBUSTNESS_TEST_XXXX";

    #REMOVE The following line is useful to have in case you want to do a test run of the playlist without actually
    #REMOVE changing anything on the cluster. It also depends on that you actually also implement the needed logic
    #REMOVE to skip/echo commands that do changes to the cluster.
    # Check if we should skip the execution of certain docker, helm and kubectl commands
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    #REMOVE Do not touch the following lines.
    #REMOVE Inside the Perform_Pre_Test_Checks_P3xxS01 step it will perform all needed tasks like initialization
    #REMOVE of the job variables and performing a pre test health check and collection of log files.
    #REMOVE There should be no need for changes inside this step.
    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P3xxS01, \&Fallback001_P3xxS99 );
    return $rc if $rc < 0;

    #REMOVE Do not touch the following lines.
    #REMOVE Inside the called Perform_Test_Case_P3xxS02 step you should add all your test logic.
    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P3xxS02, \&Fallback001_P3xxS99 );
    return $rc if $rc < 0;

    #REMOVE Do not touch the following lines.
    #REMOVE Inside the Perform_Post_Test_Checks_P3xxS03 step it will perform all needed tasks like cleanup
    #REMOVE of the job variables and files and performing a post test health check and collection of log files.
    #REMOVE There should be no need for changes inside this step.
    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P3xxS03, \&Fallback001_P3xxS99 );
    return $rc if $rc < 0;

    #REMOVE Do not touch the following line.
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
#REMOVE Do not touch the following lines.
#REMOVE Inside the Perform_Pre_Test_Checks_P3xxS01 step it will perform all needed tasks like initialization
#REMOVE of the job variables and performing a pre test health check and collection of log files.
#REMOVE There should be no need for changes inside this step.
sub Perform_Pre_Test_Checks_P3xxS01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
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
#REMOVE Inside this subroutine you will add all your needed code to perform the test case.
#REMOVE You can add as many task calls as needed.
#REMOVE
#REMOVE
#REMOVE
#REMOVE
#REMOVE
sub Perform_Test_Case_P3xxS02 {

    #REMOVE Add or remove variable definition statements as needed for your step logic.
    my $length;
    my $message;
    my $rc;

    #REMOVE These lines shows an example of how you can parse special job playlist variables.
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P3xxS02T01 } );
    return $rc if $rc < 0;

    #REMOVE These lines shows an example of how you can fetch a current value from the node under test and
    #REMOVE then update the global playlist array variable.
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Current_Timezone_Value_P3xxS02T02 } );
    return $rc if $rc < 0;

    #REMOVE These lines shows an example of how you can create a step playlist variable that
    #REMOVE is initialized to a value stored in a global job variable if it exists or otherwise
    #REMOVE get a default value of 1.
    #REMOVE Doing logic like this would allow the job to be interrupted and re-runned again
    #REMOVE and the logic picks up from where is last stopped.
    #REMOVE If you see no need to have such functionality, (which would only be needed in case
    #REMOVE of testing a fixing playlist logical errors, then you can ignore this complexity and
    #REMOVE just initialize step variables in the usual way.
    # Set start value either to 1 if never executed or to value of P3xx_TIMEZONE_CNT job variable
    # if job has been interrupted and rerun.
    my $timezone_cnt = exists $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'} ? $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}+1 : 1;
    $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}       = exists $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}       ? $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}       : 1;
    $::JOB_PARAMS{'P3xx_EXECUTE_P301S02T03'} = exists $::JOB_PARAMS{'P3xx_EXECUTE_P301S02T03'} ? $::JOB_PARAMS{'P3xx_EXECUTE_P301S02T03'} : 1;

    #REMOVE These lines shows an example of how you can loop over a global playlist variable to perform a test case
    #REMOVE multiple times with different input values each time it's executed.
    # Repeat the test case a number of times
    for (; $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'} <= $#timezones;) {
        #REMOVE This line shows how you can pass variable values from the step into the task
        #REMOVE based on e.g. the contents of a global playlist variable.
        $::JOB_PARAMS{'P3xx_TIMEZONE_VALUE'} = $timezones[$::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}];

        #REMOVE Do not touch the following lines.
        #REMOVE This shows how the start time for each loop repetition is set and it will be used
        #REMOVE for calculating the time for fetching the KPI values.
        # Set start time for this repetition in case we want to have KPI verdicts for each repetition
        $::JOB_PARAMS{'KPI_START_TIME'} = time();

        #REMOVE The following line should be modified to describe what the test case is for and it will
        #REMOVE be used for updating the status messages for the job.
        # Set the description to be used for status messages
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Time Zone Change (%s)", $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}, $::JOB_PARAMS{'P3xx_TIMEZONE_VALUE'};

        #REMOVE These lines shows an example of you you can print a progress message showing which test case
        #REMOVE repetition is being executed and to have it visible standing out, for example like this:
        #REMOVE
        #REMOVE ###########################################
        #REMOVE # Repetition 1 of 3, Timezone=Asia/Beirut #
        #REMOVE ###########################################
        $message = sprintf "# Repetition %d of %d, Timezone=%s #", $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}, $#timezones, $timezones[$::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}];
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        #REMOVE These lines shows an example how to call the actual test case including how to skip
        #REMOVE a task if it's already been executed during e.g. a re-run of an interrupted job.
        if ($::JOB_PARAMS{'P3xx_EXECUTE_P3xxS02T03'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Timezone_P3xxS02T03 } );
            return $rc if $rc < 0;
            #REMOVE This line shows an example how to avoid calling this task again during a re-run
            #REMOVE of an aborted job when the abort occurred in the P932 playlist code below.
            ::JOB_PARAMS{'P3xx_EXECUTE_P3xxS02T03'} = 0;
        }

        #REMOVE These lines shows an example how to call existing logic in another playlist that is waiting
        #REMOVE for the node to become stable again after the test case, which in this case waits for all
        #REMOVE K8s POD to come up again and then perform an after test case health check of the node to
        #REMOVE determine that the node was not negativly affected by the test case.
        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        return $rc if $rc < 0;

        #REMOVE These lines shows an example how to step the loop counter and to reset the flag
        #REMOVE for if a task should be executed again.
        # Step the counter and reset the executed tasks
        $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'}++;
        $::JOB_PARAMS{'P3xx_EXECUTE_P3xxS02T03'} = 1;
    }

    #REMOVE These lines shows an example of how you can restore the original value after finishing the suite of test cases.
    #REMOVE It shows in this case how you would call the same logic as performed during the test cases but now with the
    #REMOVE original value which was saved off into the global playlist array variable in index 0.
    #REMOVE It also waits for the node to become stable again after restoring the original value and it also persoms a
    #REMOVE new health check again.
    # Restore original timezone value if known
    if ($::JOB_PARAMS{'SKIP_RESTORE_TIMEZONE'} eq "no" && $timezones[0] ne "" && $timezones[0] ne $::JOB_PARAMS{'TIMEZONE'}) {
        # We have an original timezone value and it's not the same as last used value, so change it back

        $message = sprintf "# Restoring original Timezone=%s #", $timezones[0];
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'} = 0;
        $::JOB_PARAMS{'P3xx_TIMEZONE_VALUE'} = $timezones[0];
        #REMOVE The following line should be updated, if needed.
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case Time Zone Restore (%s)", $::JOB_PARAMS{'P3xx_TIMEZONE_VALUE'};
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Timezone_P3xxS02T03 } );
        return $rc if $rc < 0;

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        return $rc if $rc < 0;
    }

    #REMOVE These lines shows how you could skip the final health check which is not really needed because the check has
    #REMOVE already been performed as part of the '$::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";' call above.
    # Skip the final post healthcheck since we have already done it above
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";

    #REMOVE These lines shows how you could delete global job variables which no longer serve a purpose after completed
    #REMOVE test.
    # Delete these temporary variables
    delete $::JOB_PARAMS{'P3xx_EXECUTE_P3xxS02T03'};
    delete $::JOB_PARAMS{'P3xx_TIMEZONE_CNT'};
    delete $::JOB_PARAMS{'P3xx_TIMEZONE_VALUE'};

    #REMOVE This line should not be changed since it returns back the result code to the "main" task.
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
    #REMOVE These lines shows an example of how you can parse special job playlist variables.
    sub Check_Job_Parameters_P3xxS02T01 {

        my $rc = 0;

        # Check if the user want to modify the time zone values to use
        if ($::JOB_PARAMS{'TIMEZONES'} ne "") {
            @timezones = ( "", split /[,|]+/, $::JOB_PARAMS{'TIMEZONES'} );
        }

        General::Logging::log_user_message("Used Time zones are:" . join "\n - ", @timezones);

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
    #REMOVE These lines shows an example of how you can fetch a current value from the node under test and
    #REMOVE then update the global playlist array variable.
    sub Get_Current_Timezone_Value_P3xxS02T02 {

        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Getting currently used timezone value.\nThis will take a while to complete.\n");
        $rc = ADP::Kubernetes_Operations::get_helm_release_extended_information(
            {
                "command"         => "values --all",
                "hide-output"     => 1,
                "namespace"       => $::JOB_PARAMS{'SC_NAMESPACE'},
                "output-format"   => "dot-separated",
                "release-name"    => $::JOB_PARAMS{'SC_RELEASE_NAME'},
                "return-output"   => \@result,
                "return-patterns" => [ '^global\.timezone=' ],
            }
        );

        if ($rc == 0 && scalar @result == 1) {
            if ($result[0] =~ /^global\.timezone=(\S+)\s*$/) {
                # Update index 0 of array with current timezone value
                $timezones[0] = $1;
                General::Logging::log_user_message("Currently used time zone is $1");
            } else {
                General::Logging::log_user_warning_message("Unexpected value ($result[0]) returned for current time zone value, so no attempt is done to set it back after the test");
            }
        } else {
            General::Logging::log_user_warning_message("Unable to fetch the currently used time zone value (rc=$rc), so no attempt is done to set it back after the test");
            $rc = 0;
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
    #REMOVE These lines shows an example of what the test code might look like and it uses some global job variables
    #REMOVE as input values.
    #REMOVE It also shows how you can update the global job JOB_STATUS array to show if a test case was successful or not.
    sub Change_Timezone_P3xxS02T03 {

        #REMOVE These lines initializes local variables used by this task, taking some of it's values from global
        #REMOVE job variables.
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my $sc_umbrella_file = $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'};
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT'};

        if ($::JOB_PARAMS{'HELM_VERSION'} != 2) {
            $timeout .= "s";    # Add an extra 's' for number of seconds which is needed for helm 3 or higher
        }

        #REMOVE This line shows how you show a progress message to the user.
        General::Logging::log_user_message("Changing timezone to $::JOB_PARAMS{'P3xx_TIMEZONE_VALUE'}.\nThis will take a while to complete.\n");

        #REMOVE These lines shows how you can execute a command on the system and it automatically logs the output from the command.
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace upgrade $sc_release_name --set global.timezone=$::JOB_PARAMS{'P3xx_TIMEZONE_VALUE'} $sc_umbrella_file --timeout $timeout --reuse-values --debug",
                "hide-output"   => 1,
            }
        );

        #REMOVE These lines shows how you check the result of the command execution where $rc == 0 means it was successful.
        #REMOVE It also updates job status messages shown to the user at the end of the job.
        if ($rc == 0) {
            General::Logging::log_user_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} was successful");
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was successful";
        } else {
            # Display the result in case of error
            #REMOVE This shows how to display to the user the output of the command when it failed.
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
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
#REMOVE Do not touch the following lines.
#REMOVE Inside the Perform_Post_Test_Checks_P3xxS03 step it will perform all needed tasks like cleanup
#REMOVE of the job variables and files and performing a post test health check and collection of log files.
#REMOVE There should be no need for changes inside this step.
sub Perform_Post_Test_Checks_P3xxS03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
#REMOVE Normally you would not need to change any of these lines which is called when a job failed and a fallback is executed.
#REMOVE These lines will collect ADP log files and cleanup the job environment after such failure.
#REMOVE You would only need to change this logic if you need to do something special in case of a fallback.
sub Fallback001_P3xxS99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    return 0;
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

#REMOVE Do not touch the following lines.
#REMOVE They add logic that will delete network configuration file parameters from
#REMOVE %::JOB_PARAMS and %::NETTWORK_CONFIG_PARAMS hashes that are not applicable
#REMOVE for the release of the software being used.
# -----------------------------------------------------------------------------
# This subroutine reads all parameters from the specified network configuration
# file and updates the %::NETWORK_CONFIG_PARAMS and %::JOB_PARAMS hash variables
# after applying filters to remove unwanted parameters.
#
sub parse_network_config_parameters {
    return Playlist::932_Test_Case_Common_Logic::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
#REMOVE These lines should normally be changed to add special Playlist variables
#REMOVE that this playlist needs for have initialized or collected from the user.
#REMOVE These variables are just an example of what this template playlist needs.
sub set_playlist_variables {
    # Define playlist specific variables that is not already defined in the
    # sub playlist Playlist::932_Test_Case_Common_Logic.
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'SKIP_RESTORE_TIMEZONE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should skip restoring the original time zone after
completed test.

Select "no" which is also the default value if not specified, to have the
Playlist always restore the original time zone after completed test.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SOFTWARE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds all the software to be deployed or
upgraded on the node, i.e. the directory that contains the 'eric-sc-*.csar' and
'eric-sc-tools-*.tgz' files.
This directory should contain the same software that is currently deployed on
the cluster, from this directory the playlist will extract out the SC umbrella
file which is used by by this Robustness playlists when applying the different
timezones by using the 'helm upgrade' command.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TIMEZONE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the time zone value to change the node to.
If multiple time zones should be tested, one-after-another then these can be
specified with the TIMEZONE_1, TIMEZONE_2 etc. parameters

If multiple time zones should be tested one-after-another then just add job
parameters TIMEZONE_1, TIMEZONE_2 etc.
These time zones will be tested in the numbered order of the job parameters but
TIMEZONE will be tested first.

If the parameter is not specified then a default set of timezones will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    #REMOVE Update the Description text to reflect what the playlist is used for.
    print <<EOF;

Description:
============

This Playlist performs a robustness test case that verifies ....

Used Job Parameters:
====================
EOF
    #REMOVE Do not touch the following lines.
    #REMOVE They adds printout of used playlist variables when running the execute_playlist.pl
    #REMOVE command with the playlist name "-p" and help "-h" parameters.
    # Special handling for the 3xx_Robustness_Test_xxxx playlists so we print out all variable
    # information from the main playlist and the 932_Test_Case_Common_Logic sub playlist.
    use File::Basename qw(dirname basename);
    my $length;
    my $message;
    my $path_to_932_playlist = dirname(__FILE__) . "/932_Test_Case_Common_Logic.pm";
    my $playlist_name = basename(__FILE__);
    $playlist_name =~ s/\.pm//;
    my %temp_hash = ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    $message = "# Global variable access in playlist 932_Test_Case_Common_Logic #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables($path_to_932_playlist, \%Playlist::932_Test_Case_Common_Logic::playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables($path_to_932_playlist);
}

#REMOVE Do not touch the following lines.
#REMOVE They add logic that will return back common used playlist variables for
#REMOVE all Robustness Playlists to the execute_playlist.pl script.
# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );
}

#REMOVE Do not touch this line because it is needed or loading of this Playlist file will fail.
1;

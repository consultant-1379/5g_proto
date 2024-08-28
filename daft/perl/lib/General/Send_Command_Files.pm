package General::Send_Command_Files;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.14
#  Date     : 2019-12-04 13:53:55
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2019
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
    execute_command_files
    find_command_files
    set_job_variables
    set_use_logging
    set_verbose_level
);

use File::Basename qw(dirname basename);
use Cwd qw(abs_path cwd);

use CBA::Cluster_Operations;
use General::File_Operations;
use General::Logging;

# *************************
# * Variable Declarations *
# *************************
my $job_bsp_ip = "";
my $job_bsp_password = "";
my $job_bsp_username = "";
my $job_cluster_script_path = "";
my $job_connection_type = "";
my $job_executing_on_cluster = "";
my $job_gep_ip = "";
my $job_gep_password = "";
my $job_gep_username = "";
my $job_oam_vip_ip = "";
my $job_script_path = "";
my $job_scx_advanced_password = "";
my $job_scx_ip = "";
my $use_logging = 0;    # Print to screen, don't use logging
my $verbose_level = 0;

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
# Executes scripts or loads command files on the local host or a remote node.
#
# Input variables:
#    - Mandatory hash reference %params = Input parameters to the sub routine
#      that control which files should be loaded or executed and different
#      option that controls how thjey are loaded or executed.
#      One or more of the following keys can be used:
#       "files":
#           This is an MANDATORY parameter that is an array reference with a
#           list of files including directory path to the files to be loaded
#           or executed.
#       "hide-error-messages":
#           If speficied and >0 then any detected errors will not be printed to the
#           user.
#           The default is to always show any detected errors to the user.
#       "indent-progress-messages":
#           If specified then any shown progess messages will be indented with
#           the specified characters.
#           By default no indentation will be done, i.e. all progress messages
#           will be printed at the start of the line.
#       "just-print":
#           If speficied and >0 then the command to be executed is just printed
#           but not executed.
#           The default is to always execute the commands.
#       "return-failed-files":
#           This is an array reference which will contain all files that failed
#           to load properly and is returned to the caller if this parameter is
#           specified.
#       "return-successful-files":
#           This is an array reference which will contain all files that was
#           loaded properly without any errors and is returned to the caller if
#           this parameter is specified.
#       "set-environment-variables":
#           This is an array reference which contains environment variables that
#           should be set on the target node (where it makes sense) before
#           executing a command.
#           This is an optional parameter and if not set then no environment
#           variables will be set.
#       "show-command-execution":
#           If speficied and >0 then command execution will be printed to the
#           user.
#           The default is to always hide command execution from the user.
#       "show-progress":
#           If speficied and >0 then the files to be loaded will be printed
#           to the user.
#           The default is not to show any loaded files.
#       "stop-on-error":
#           If specified then execution of commands will stop at the first
#           command that reports an error.
#           This might result in partially loaded command files.
#           By default execution of all command files will be done and then at
#           the end it will be reported if one or more commands had an error.
#
# Return values:
#    - Return code
#      0: Successful exection of command files.
#      1: Failure while execting command files.
#
# -----------------------------------------------------------------------------
sub execute_command_files {
    my %params = %{$_[0]};

    # General variables
    my $command;
    my $command_file;
    my $command_parameters;
    my @commands = ();
    my $config_file;
    my $config_param;
    my @config_params = ();
    my $config_value;
    my @environment_lines = ();
    my @environment_variables = exists $params{'set-environment-variables'} ? @{$params{'set-environment-variables'}} : ();
    my $failure_cnt = 0;
    my $file_name;
    my @files = exists $params{'files'} ? @{$params{'files'}} : ();
    my $hide_error_messages = exists $params{'hide-error-messages'} ? $params{'hide-error-messages'} : 0;
    my $hide_output;
    my $ignore_errors;
    my $indent_progress_messages = exists $params{'indent-progress-messages'} ? $params{'indent-progress-messages'} : "";
    my $just_print = exists $params{'just-print'} ? $params{'just-print'} : 0;
    my $load_as_one_line_commands = 0;
    my @load_result_successful = ();
    my @load_result_failure = ();
    my $rc = 0;
    my $return_failed_files = exists $params{'return-failed-files'} ? 1 : 0;
    my $return_successful_files = exists $params{'return-successful-files'} ? 1 : 0;
    my $show_command_execution = exists $params{'show-command-execution'} ? $params{'show-command-execution'} : 0;
    my $show_progress = exists $params{'show-progress'} ? $params{'show-progress'} : 0;
    my $stop_on_error = exists $params{'stop-on-error'} ? $params{'stop-on-error'} : 0;
    my $stop_on_error_param = "";
    my $target_blade = "";

    # Connection variables

    if (scalar @files == 0) {
        print_error_message_to_user("No files specified with the key 'files'\n") unless $hide_error_messages;
        return 1;
    }

    if ($show_command_execution) {
        $hide_output = 0;
    } else {
        $hide_output = 1;
    }

    if ($stop_on_error) {
        $stop_on_error_param = "--stop-on-error";
    } else {
        $stop_on_error_param = "";
    }

    # Go through all files
    for $command_file (@files) {
        $file_name = basename($command_file);

        # Show progress message, if wanted
        if ($show_progress) {
            if ($use_logging == 0) {
                print "${indent_progress_messages}Loading file '$file_name'\n";
            } else {
                General::Logging::log_user_message("${indent_progress_messages}Loading file '$file_name'\n");
            }
        }

        # Set default values for each file
        $command_parameters = "";
        @commands = ();
        @environment_lines = @environment_variables;
        $ignore_errors = 0;
        $load_as_one_line_commands = 0;
        $target_blade = "";

        # Check if we have a .config file with special tags that affect
        # the loading behavior for the command file.
        $config_file = $command_file;
        $config_file =~ s/^(.+)\.\S+/$1.config/;     # Replace file extension
        if (-f "$config_file") {
            # Process the .config file
            print_message_to_user(sprintf "Checking config file '%s'\n", basename($config_file)) if $verbose_level > 0;
            General::File_Operations::read_file(
                {
                    "filename"              => $config_file,
                    "output-ref"            => \@config_params,
                    "hide-error-messages"   => $hide_error_messages,
                    "include-pattern"       => '^(COMMAND_PARAMETER|ENVIRONMENT_VARIABLE|IGNORE_ERRORS|LOAD_AS_ONE_LINE_COMMANDS|TARGET_BLADE)=.+',
                }
            );
            for (@config_params) {
                print_message_to_user("  $_") if $verbose_level > 0;

                s/\s+$//;   # Remove whitespace at end of line
                if (/^(\w+)=(.+)/) {
                    $config_param = $1;
                    $config_value = $2;
                } else {
                    # Incorrect format, ignore this line
                    next;
                }
                if ($config_param =~ /^COMMAND_PARAMETER$/i) {
                    $command_parameters .= "$config_value ";
                } elsif ($config_param =~ /^ENVIRONMENT_VARIABLE$/i) {
                    push @environment_lines, $config_value;
                } elsif ($config_param =~ /^IGNORE_ERRORS$/i) {
                    $ignore_errors = $config_value =~ m/^yes$/i ? 1 : 0;
                } elsif ($config_param =~ /^LOAD_AS_ONE_LINE_COMMANDS$/i) {
                    if (lc($config_value) eq "yes") {
                        $load_as_one_line_commands = 1;
                    } else {
                        $load_as_one_line_commands = 0;
                    }
                } elsif ($config_param =~ /^TARGET_BLADE$/i) {
                    if ($config_value =~ /^(activecontroller|sc-[12]|pl-\d+)$/i) {
                        $target_blade = lc($config_value);
                    } else {
                        print_error_message_to_user("Not Supported value '$config_value'") unless $hide_error_messages;
                    }
                }
            }
        }

        # Check interface type and build command to execute
        if ($command_file =~ /^.+_(DAFT|localhost)\.\S+$/i) {

            # *************
            # * localhost *
            # *************

            # Set environment variables, if any
            for (@environment_lines) {
                if (/^(\w+)=(.*)/) {
                    $ENV{$1} = "$2";
                }
            }

            $command = "$command_file $command_parameters";

        } elsif ($command_file =~ /^.+_(GEP-Cliss)\.\S+$/i) {

            # *************
            # * GEP-Cliss *
            # *************

            if ($job_executing_on_cluster eq "yes") {
                # We are currently running DAFT on the Cluster and since we do not want to
                # use Expect on the cluster anymore we need to read in the commands from
                # the file as individual commands and call the 'send_command_cliss' function.
                $target_blade = "cliss";

                # Read the command file in as commands for cliss
                $rc = General::File_Operations::read_file(
                    {
                        "filename"              => "$command_file",
                        "output-ref"            => \@commands,
                        "hide-error-messages"   => 1,
                        "ignore-empty-lines"    => 1,
                    }
                );
                if ($rc) {
                    # Failure while reading file
                    print_error_message_to_user("Failed to read the file '$command_file'") unless $hide_error_messages;

                    if ($ignore_errors == 1) {
                        push @load_result_successful, $command_file;
                    } else {
                        push @load_result_failure, $command_file;
                        # Step failure counter
                        $failure_cnt++;

                        # Check if we should stop executing files on first error
                        last if $stop_on_error;
                    }
                    # Continue with next file
                    next;
                }
            } else {
                # We are currently running DAFT remotely so we need to use Expect to connect
                # to the cluster.
                if ($job_connection_type eq "nbi") {
                    # nbi
                    $command = "expect $job_script_path/expect/bin/send_command_to_ssh.exp --command-file='$command_file' $stop_on_error_param --ip=$job_oam_vip_ip --user=$job_gep_username --password='$job_gep_password' --port=830 --replace-managedelement --shell=cliss ";
                } else {
                    # direct_ssh or laptop
                    $command = "expect $job_script_path/expect/bin/send_command_to_ssh.exp --command-file='$command_file' $stop_on_error_param --ip=$job_gep_ip --user=$job_gep_username --password='$job_gep_password' --replace-managedelement --shell=cliss --target-blade=activecontroller ";
                }
                if ($ignore_errors) {
                    $command .= "--ignore-errors "
                }
            }

        } elsif ($command_file =~ /^.+_(GEP-Linux)\.\S+$/i) {

            # *************
            # * GEP-Linux *
            # *************

            if ($job_executing_on_cluster eq "yes") {
                # We are currently running DAFT on the Cluster and since we do not want to
                # use Expect on the cluster anymore we need to do some extra checks.

                # Set environment variables, if any
                for (@environment_lines) {
                    if (/^(\w+)=(.*)/) {
                        $ENV{$1} = "$2";
                    }
                }

                if ($load_as_one_line_commands == 1) {
                    # We read in the commands from the file as individual commands and send them
                    # one by one.

                    # Read the command file in as commands for cliss
                    $rc = General::File_Operations::read_file(
                        {
                            "filename"              => "$command_file",
                            "output-ref"            => \@commands,
                            "hide-error-messages"   => 1,
                            "ignore-empty-lines"    => 1,
                        }
                    );
                    if ($rc) {
                        # Failure while reading file
                        print_error_message_to_user("Failed to read the file '$command_file'") unless $hide_error_messages;

                        if ($ignore_errors == 1) {
                            push @load_result_successful, $command_file;
                        } else {
                            push @load_result_failure, $command_file;
                            # Step failure counter
                            $failure_cnt++;

                            # Check if we should stop executing files on first error
                            last if $stop_on_error;
                        }
                        # Continue with next file
                        next;
                    }
                } else {
                    # Execution of the command file as a script on the target node

                    # Change the path to the file because we will execute the file that
                    # has already been uploaded to the cluster.
                    my $modified_command_file = $command_file;
                    $modified_command_file =~ s/^$job_script_path/$job_cluster_script_path/;

                    if ($command_parameters eq "") {
                        $command = $modified_command_file;
                    } else {
                        $command = "$modified_command_file $command_parameters";
                    }
                }
            } else {
                # We are currently running DAFT remotely so we need to use Expect to connect
                # to the cluster.
                if ($load_as_one_line_commands == 1) {
                    # Execution of the command file as one command at the time
                    $command = "expect $job_script_path/expect/bin/send_command_to_ssh.exp ";

                    if ($job_connection_type eq "nbi") {
                        # nbi
                        if ($target_blade eq "") {
                            $command .= "--ip=$job_oam_vip_ip --user=$job_gep_username --password='$job_gep_password' --port=830 ";
                        } else {
                            $command .= "--ip=$job_oam_vip_ip --user=$job_gep_username --password='$job_gep_password' --port=830 --target-blade=$target_blade ";
                        }
                    } else {
                        # direct_ssh or laptop
                        if ($target_blade eq "") {
                            $command .= "--ip=$job_gep_ip --user=$job_gep_username --password='$job_gep_password' ";
                        } else {
                            $command .= "--ip=$job_gep_ip --user=$job_gep_username --password='$job_gep_password' --target-blade=$target_blade ";
                        }
                    }

                    if ($ignore_errors) {
                        $command .= "--ignore-errors "
                    }

                    # Set environment variables, if any
                    for (@environment_lines) {
                        $command .= "--command='export $_' ";
                    }

                    $command .= "--command-file='$command_file' $stop_on_error_param ";

                    # Reset the target_blade variable so we don't start executing it locally
                    $target_blade = "";
                } else {
                    # Execution of the command file as a script on the target node

                    # Change the path to the file because we will execute the file that
                    # has already been uploaded to the cluster.
                    my $modified_command_file = $command_file;
                    $modified_command_file =~ s/^$job_script_path/$job_cluster_script_path/;

                    $command = "expect $job_script_path/expect/bin/send_command_to_ssh.exp ";

                    if ($target_blade eq "") {
                        $target_blade = "activecontroller";
                    }

                    if ($job_connection_type eq "nbi") {
                        # nbi
                        $command .= "--ip=$job_oam_vip_ip --user=$job_gep_username --password='$job_gep_password' --port=830 --target-blade=$target_blade ";
                    } else {
                        # direct_ssh or laptop
                        $command .= "--ip=$job_gep_ip --user=$job_gep_username --password='$job_gep_password' --target-blade=$target_blade ";
                    }

                    if ($ignore_errors) {
                        $command .= "--ignore-errors "
                    }

                    # Set environment variables, if any
                    for (@environment_lines) {
                        $command .= "--command='export $_' ";
                    }

                    if ($command_parameters eq "") {
                        $command .= "--command='$modified_command_file' $stop_on_error_param ";
                    } else {
                        $command .= "--command='$modified_command_file $command_parameters' $stop_on_error_param ";
                    }

                    # Reset the target_blade variable so we don't start executing it locally
                    $target_blade = "";
                }
            }

        } elsif ($command_file =~ /^.+_(GEP-Signmcli)\.\S+$/i) {

            # ****************
            # * GEP-Signmcli *
            # ****************

            if ($job_executing_on_cluster eq "yes") {
                # We are currently running DAFT on the Cluster and since we do not want to
                # use Expect on the cluster anymore we need to read in the commands from
                # the file as individual commands and call the 'send_command_signmcli' function.
                $target_blade = "signmcli";

                # Read the command file in as commands for cliss
                $rc = General::File_Operations::read_file(
                    {
                        "filename"              => "$command_file",
                        "output-ref"            => \@commands,
                        "hide-error-messages"   => 1,
                        "ignore-empty-lines"    => 1,
                    }
                );
                if ($rc) {
                    # Failure while reading file
                    print_error_message_to_user("Failed to read the file '$command_file'") unless $hide_error_messages;

                    if ($ignore_errors == 1) {
                        push @load_result_successful, $command_file;
                    } else {
                        push @load_result_failure, $command_file;
                        # Step failure counter
                        $failure_cnt++;

                        # Check if we should stop executing files on first error
                        last if $stop_on_error;
                    }
                    # Continue with next file
                    next;
                }
            } else {
                # We are currently running DAFT remotely so we need to use Expect to connect
                # to the cluster.
                if ($job_connection_type eq "nbi") {
                    # nbi
                    $command = "expect $job_script_path/expect/bin/send_command_to_ssh.exp --command-file='$command_file' $stop_on_error_param --ip=$job_oam_vip_ip --user=$job_gep_username --password='$job_gep_password' --port=830 --shell=signmcli ";
                } else {
                    # direct_ssh or laptop
                    $command = "expect $job_script_path/expect/bin/send_command_to_ssh.exp --command-file='$command_file' $stop_on_error_param --ip=$job_gep_ip --user=$job_gep_username --password='$job_gep_password' --shell=signmcli ";
                }
                if ($ignore_errors) {
                    $command .= "--ignore-errors "
                }
            }

        } elsif ($command_file =~ /^.+_(BSP-Cliss)\.\S+$/i) {

            # *************
            # * BSP-Cliss *
            # *************

            $command = "expect $job_script_path/expect/bin/send_command_to_bsp_cliss.exp --command-file='$command_file' $stop_on_error_param --advanced-password='$job_scx_advanced_password' ";
            if ($job_connection_type eq "nbi") {
                # nbi
                $command .= "--nbi-ip=$job_bsp_ip --nbi-user=$job_bsp_username --nbi-password='$job_bsp_password' ";
            } else {
                # direct_ssh or laptop
                $command .= "--scxb-ip=$job_scx_ip ";
            }

        } else {

            # ***************************
            # * Not Supported Interface *
            # ***************************

            print_error_message_to_user("Not Supported Interface in file '$file_name'") unless $hide_error_messages;

            next;

        }

        #
        # Send the command
        #
        if (@commands) {
            # We have more than one command to send
            if ($just_print) {
                # Only print the commands to send
                for (@commands) {
                    print_message_to_user("$_");
                }
            } else {
                # Execute the commands
                if ($target_blade eq "cliss") {
                    # We need to send the commands to cliss
                    $rc = CBA::Cluster_Operations::send_command_cliss(
                        {
                            "commands"      => \@commands,
                            "hide-output"   => $hide_output,
                            "ignore-errors" => $ignore_errors,
                        }
                    );
                } elsif ($target_blade eq "signmcli") {
                    # We need to send the commands to the signalling manager
                    $rc = CBA::Cluster_Operations::send_command_signmcli(
                        {
                            "commands"      => \@commands,
                            "hide-output"   => $hide_output,
                            "ignore-errors" => $ignore_errors,
                        }
                    );
                } elsif ($target_blade ne "") {
                    # We need to send the commands to a specific blade
                    $rc = CBA::Cluster_Operations::send_command_targetblade(
                        {
                            "commands"          => \@commands,
                            "command-in-output" => 1,
                            "hide-output"       => $hide_output,
                            "ignore-errors"     => $ignore_errors,
                            "target-blade"      => $target_blade,
                        }
                    );
                } else {
                    # No special handling send the commands, one by one
                    $rc = General::OS_Operations::send_command(
                        {
                            "commands"          => \@commands,
                            "command-in-output" => 1,
                            "hide-output"       => $hide_output,
                            "ignore-errors"     => $ignore_errors,
                        }
                    );
                }
            }
        } else {
            # Just a single command
            $command =~ s/\s+$//;   # Remove whitespace at end of line

            if ($just_print) {
                print_message_to_user("$command");
            } else {
                if ($target_blade ne "") {
                    # We need to send the commands to a specific blade
                    $rc = CBA::Cluster_Operations::send_command_targetblade(
                        {
                            "command"       => "$command",
                            "hide-output"   => $hide_output,
                            "ignore-errors" => $ignore_errors,
                            "target-blade"  => $target_blade,
                        }
                    );
                } else {
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "$command",
                            "hide-output"   => $hide_output,
                            "ignore-errors" => $ignore_errors,
                        }
                    );
                }
            }
        }

        # Check result of the command
        if ($rc == 0) {
            push @load_result_successful, $command_file;
        } else {
            if ($ignore_errors == 1) {
                push @load_result_successful, $command_file;
            } else {
                push @load_result_failure, $command_file;

                # Display the result in case of error
                unless ($hide_error_messages) {
                    General::OS_Operations::write_last_temporary_output_to_progress();
                    print_error_message_to_user("Failure rc=$rc in file '$file_name'");
                }
                # Step failure counter
                $failure_cnt++;

                # Check if we should stop executing files on first error
                last if $stop_on_error;
            }
        }
    }

    # Check if we need to return failed and successful files
    if ($return_failed_files) {
        @{$params{'return-failed-files'}} = @load_result_failure;
    }
    if ($return_successful_files) {
        @{$params{'return-successful-files'}} = @load_result_successful;
    }

    # Return code to the caller
    if ($failure_cnt == 0) {
        # No errors
        return 0;
    } else {
        # One or more errors
        return 1;
    }
}

# -----------------------------------------------------------------------------
# Return an array with file names matching conditions that can be executed
# using the 'execute_command_files' function.
#
# Input variables:
#    - Directory path to search for files in. (MANDATORY)
#    - Optional hash reference %params = Input parameters to the sub routine
#      that control which files should be returned to the user and other options
#      about what information is printed to the user. (OPTIONAL)
#      One or more of the following keys can be used:
#       "file-extension":
#           Specifies the file extension of the files to be returned to the
#           caller. Currently the only files types that makes sense for this
#           functionality are "apply" and "undo".
#           If not specified then file extension "apply" is assumed.
#       "filter-callback":
#           This is a callback subroutine reference that is used when a filter
#           should be applied to a file, see 'filter-regex'.
#           This subroutine will be called with an array of the found filter
#           lines in the .config file and the subroutine should return one of
#           the strings:
#             'keep': The file should be kept (returned to the caller).
#             'remove': The file should be removed (not returned to the caller).
#             'ignore': Filter is ignored and the file can be kept or removed
#                     depending on any other filters.
#
#           If a file is marked to both be returned (keep) and not returned
#           (remove) then the file will not be returned to the user (removed).
#       "filter-regex":
#           This filter string is used to check if a file should be included
#           or not in files to return to the caller.
#           It should contain a Perl regular expression that will be used to
#           find special lines in '.config' files which will be used when
#           calling the subroutine reference passed in the parameter
#           'filter-callback'.
#       "hide-error-messages":
#           If speficied and >0 then any detected errors will not be printed to the
#           user.
#           The default is to always show any detected errorsa to the user.
#       "match-regex":
#           This filter string is used to check if a file should be included
#           or not in files to return to the caller.
#           It should contain a Perl regular expression that will be used to
#           match the file path of potential files to be returned to the caller.
#           If a file path match this regular expression then the file will be
#           checked furter against the 'filter-regex' pattern, and if it does
#           not match the regular expression then the file will be ignored.
#           If this regular expression key is not specified then the file will
#           automatically be checked agains the 'filter-regex'.
#
#           The idea with this key is to allow execution of single files or a
#           number of files even if there are more files that would otherwise
#           match.
#       "show-applied-filters":
#           If speficied and >0 then applied filters will be printed to the user.
#           The default is not to show any applied filters.
#       "show-ignored-files":
#           If speficied and >0 then ignored files will be printed to the user.
#           An ignored file is a file that does not match the wanted file
#           extension.
#           The default is not to show any ignored files.
#       "show-skipped-files":
#           If speficied and >0 then skipped files will be printed to the user.
#           A skipped file is a file that has already been loaded before.
#           The default is not to show any skipped files.
#       "show-valid-files":
#           If speficied and >0 then the files matching with the wanted file
#           extension and that is not removed because of a filter will be
#           printed to the user.
#           The default is not to show any valid files.
#       "skip-files":
#           If specified it should be an array reference with file names that
#           should be skipped.
#
# Return values:
#    - Returns an array of file names matching the wanted conditions or an empty
#      array if nothing matches.
#
# Example of usage:
#       my $directory = "/var/lib/daft/tools/playlist_extensions/upgrade/pre/";
#       my @files = General::Send_Command_Files::find_command_files($directory,
#           {
#               "file-extension"                => "apply",
#               "filter-callback"               => \&filter_subroutine,
#               "filter-regex"                  => '^(APPLY|IGNORE)_(NEW|OLD)_COMMERCIAL_RELEASE_NAME=.+',
#               "filter-old-commercial-release" => "1.10",
#               "filter-new-commercial-release" => "1.11",
#               "hide-error-messages"           => 0,
#               "show-applied-filters"          => 1,
#               "show-ignored-files"            => 1,
#               "show-skipped-files"            => 1,
#               "show-valid-files"              => 1,
#               "use-logging"                   => 1,
#               "skip-files"                    => [
#                   "/var/lib/daft/tools/playlist_extensions/upgrade/pre/08_CC-15281_GEP-Linux.apply",
#               ],
#           }
#       );
#
# -----------------------------------------------------------------------------
sub find_command_files {
    my $directory = shift;
    my %params;
    if (@_) {
        %params = %{$_[0]};
    }
    my @all_files = ();
    my $file_extension = exists $params{'file-extension'} ? $params{'file-extension'} : "";
    my $filter;
    my $filter_applied;
    my $filter_file;
    my @filters = ();
    my $filter_callback = exists $params{'filter-callback'} ? 1 : 0;
    my $filter_regex = exists $params{'filter-regex'} ? $params{'filter-regex'} : "";
    my $filter_result = "";
    my $find_specific_file_extensions = 0;
    my $heading_printed = 0;
    my $hide_error_messages = exists $params{'hide-error-messages'} ? $params{'hide-error-messages'} : 0;
    my @ignored_files = ();
    my $keep_file;
    my $match_regex = exists $params{'match-regex'} ? $params{'match-regex'} : "";
    my $message = "";
    my $remove_file;
    my $show_applied_filters = exists $params{'show-applied-filters'} ? $params{'show-applied-filters'} : 0;
    my $show_ignored_files = exists $params{'show-ignored-files'} ? $params{'show-ignored-files'} : 0;
    my $show_skipped_files = exists $params{'show-skipped-files'} ? $params{'show-skipped-files'} : 0;
    my $show_valid_files = exists $params{'show-valid-files'} ? $params{'show-valid-files'} : 0;
    my $skip_files = exists $params{'skip-files'} ? 1 : 0;
    my %skip_files;
    my %used_interfaces;
    my $valid_file;
    my @valid_files = ();

    # Check the directory parameter
    unless (-d "$directory") {
        # Not a directory, return an empty array
        return ();
    }
    $directory = abs_path $directory;

    # Check the file-extention parameter
    if ($file_extension) {
        unless ($file_extension =~ /^(apply|undo)$/i) {
            print_error_message_to_user("Invalid action '$file_extension' specified\n") unless $hide_error_messages;
            return ();
        }
        $find_specific_file_extensions = 1;
    } else {
        $file_extension = "apply";
        $find_specific_file_extensions = 0;
    }

    # Check if any filters should be checked
    if ($filter_regex ne "") {
        if ($filter_callback == 0) {
            print_error_message_to_user("Parameter 'filter-callback' is missing") unless $hide_error_messages;
            return ();
        }
    } elsif ($filter_callback == 1) {
        print_error_message_to_user("Parameter 'filter-regex' is missing") unless $hide_error_messages;
        return ();
    }

    # Check the skip-files parameter
    if ($skip_files == 1) {
        # Read file names from array reference and add to local hash variable
        for (@{$params{'skip-files'}}) {
            $skip_files{$_} = 1;
        }
    }

    # Read files in the specified directory, sort in alphabetical order
    $message .= "Finding files to execute\n" if (($show_ignored_files + $show_skipped_files + $show_valid_files) > 0);
    if ($find_specific_file_extensions) {
        @all_files = `find $directory \\( -type f -o -type l \\) -name '*.$file_extension'`;
    } else {
        @all_files = `find $directory \\( -type f -o -type l \\)`;
    }
    @all_files = sort alphanum @all_files;

    # Find files matching a specific naming convention
    for (@all_files) {
        s/[\r\n]//g;
        next if $_ eq "";
        if (/^.+_(DAFT|localhost)\.$file_extension$/i) {
            $used_interfaces{"DAFT"} = 1;
            push @valid_files, $_ unless exists $skip_files{$_};
        } elsif (/^.+_(GEP-Cliss|GEP-Linux|GEP-Signmcli)\.$file_extension$/i) {
            $used_interfaces{"GEP"} = 1;
            push @valid_files, $_ unless exists $skip_files{$_};
        } elsif (/^.+_(BSP-Cliss)\.$file_extension$/i) {
            $used_interfaces{"BSP"} = 1;
            push @valid_files, $_ unless exists $skip_files{$_};
        } else {
            push @ignored_files, $_;
        }
    }

    # Check filters against valid files and remove files that
    # does not match filters
    @all_files = @valid_files;
    @valid_files = ();
    $message .= "\nChecking if any filters needs to be applied\n" if $show_applied_filters;
    for $valid_file (@all_files) {
        if ($match_regex ne "") {
            $message .= "\nApplied match_regex for file $valid_file:\n" if $show_applied_filters;
            $message .= "  $match_regex\n" if $show_applied_filters;
            if ($valid_file =~ /$match_regex/) {
                # File path match, continue checking for other filters for this file
                $message .= "    match_regex matched, keep file for now\n" if $show_applied_filters;
            } else {
                # File path does not match, ignore this file
                push @ignored_files, $valid_file;
                $message .= "    match_regex not matched, file removed\n" if $show_applied_filters;
                next;
            }
        }

        if ($filter_regex eq "") {
            # No filter check wanted, mark the file as valid
            push @valid_files, $valid_file;
            next;
        }

        # Check if we have a config file with possible filters
        $filter_file = $valid_file;
        $filter_file =~ s/^(.+)\.\S+/$1.config/;    # Remove extention .apply or .undo and replace with .config
        unless (-f "$filter_file") {
            # No .config file found, mark the file as valid
            push @valid_files, $valid_file;
            next;
        }
        # If we are down here we need to check if the filters match
        General::File_Operations::read_file(
            {
                "filename"              => $filter_file,
                "output-ref"            => \@filters,
                "hide-error-messages"   => $hide_error_messages,
                "include-pattern"       => $filter_regex,
            }
        );
        $filter_applied = 0;
        $keep_file = 0;
        $remove_file = 0;

        if (@filters) {
            $message .= "\nApplied filters for file $valid_file:\n" if $show_applied_filters;

            for $filter (@filters) {
                $message .= "  $filter\n" if $show_applied_filters;

                # Call the subroutine reference in 'filter-callback'
                $filter_result = &{$params{'filter-callback'}}($filter);

                if ($filter_result eq "keep") {
                    $message .= "    Filter applied, keep file\n" if $show_applied_filters;
                    $keep_file++;
                    $filter_applied = 1;
                } elsif ($filter_result eq "remove") {
                    $message .= "    Filter applied, remove file\n" if $show_applied_filters;
                    $remove_file++;
                    $filter_applied = 1;
                } elsif ($filter_result eq "ignore") {
                    $message .= "    Filter ignored, keep file\n" if $show_applied_filters;
                } else {
                    print_error_message_to_user("Wrong result (=$filter_result) returned from 'filter-callback'") unless $hide_error_messages;
                    $message .= "    Filter ignored, keep file\n" if $show_applied_filters;
                }
            }
            if ($filter_applied) {
                # Filter applied, either file is kept or removed
                if ($remove_file) {
                    # Remove file has higher priority than keep file
                    $message .= "  File removed\n" if $show_applied_filters;
                    push @ignored_files, $valid_file;
                    next;
                } elsif ($keep_file) {
                    $message .= "  File kept\n" if $show_applied_filters;
                    push @valid_files, $valid_file;
                    next;
                }
            } else {
                # File should be marked as valid
                $message .= "  No filter applied, file kept\n" if $show_applied_filters;
                push @valid_files, $valid_file;
                next;
            }
        } else {
            # No filters in the .config file, File should be marked as valid
            push @valid_files, $valid_file;
            next;
        }
    }

    # Display valid files if wanted
    if (scalar @valid_files > 0 && $show_valid_files == 1) {
        $message .= "\nValid files:\n";
        for (@valid_files) {
            $message .= "  $_\n";
        }
    }

    # Display skipped files if wanted
    if ($show_skipped_files == 1) {
        $heading_printed = 0;
        for my $filename (sort alphanum keys %skip_files) {
            if ($heading_printed == 0) {
                $message .= "\nSkipped already loaded files:\n";
                $heading_printed = 1;
            }
            $message .= "  $filename\n";
        }
    }

    # Display ignore files
    if (scalar @ignored_files > 0 && $show_ignored_files == 1) {
        $message .= "\nIgnored files:\n";
        for (@ignored_files) {
            $message .= "  $_\n";
        }
    }

    if ($message ne "") {
        print_message_to_user($message);
    }

    return @valid_files;
}

# -----------------------------------------------------------------------------
# Print an error message to the user.
#
# Input variables:
#    - Message to be printed.
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub print_error_message_to_user {
    my $message = shift;

    unless ($message =~ /\n/s) {
        # Add a newline character if not existing
        $message .= "\n";
    }
    if ($use_logging == 0) {
        print $message;
    } else {
        $message =~ s/^\n//;
        General::Logging::log_user_error_message($message);
    }
}

# -----------------------------------------------------------------------------
# Print a message to the user.
#
# Input variables:
#    - Message to be printed.
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub print_message_to_user {
    my $message = shift;

    unless ($message =~ /\n/s) {
        # Add a newline character if not existing
        $message .= "\n";
    }
    if ($use_logging == 0) {
        print $message;
    } else {
        $message =~ s/^\n//;
        General::Logging::log_user_message($message);
    }
}

# -----------------------------------------------------------------------------
# Set the job related variables used by this package which contols how to
# connect to the supported interfaces of the nodes.
#
# Input variables:
#    - Hash reference to the variable that contains the job variables in the
#      main script which is used to populate the variables used by this
#      package.
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub set_job_variables {
    my %job_var = %{$_[0]};

    my $message = "";

    if (exists $job_var{'bsp-nbi-ip_address'}) {
        $job_bsp_ip = $job_var{'bsp-nbi-ip_address'};
    }
    if (exists $job_var{'bsp-nbi-ip_password'}) {
        $job_bsp_password = $job_var{'bsp-nbi-ip_password'};
    }
    if (exists $job_var{'bsp-nbi-ip_username'}) {
        $job_bsp_username = $job_var{'bsp-nbi-ip_username'};
    }
    if (exists $job_var{'SC1_SCRIPT_PATH'}) {
        $job_cluster_script_path = "$job_var{'SC1_SCRIPT_PATH'}";
        if (-d "$job_cluster_script_path") {
            # Get the real path (e.g. /home/daft/tools is really /cluster/home/daft/tools
            $job_cluster_script_path = abs_path $job_cluster_script_path;
        }
    }
    if (exists $job_var{'CONNECTION_TYPE'}) {
        $job_connection_type = $job_var{'CONNECTION_TYPE'};
    }
    if (exists $job_var{'EXECUTING_ON_CLUSTER'}) {
        $job_executing_on_cluster = $job_var{'EXECUTING_ON_CLUSTER'};
    }
    if (exists $job_var{'sc-1-ssh_address'}) {
        $job_gep_ip = $job_var{'sc-1-ssh_address'};
    }
    if (exists $job_var{'sc-1-ssh_password'}) {
        $job_gep_password = $job_var{'sc-1-ssh_password'};
    }
    if (exists $job_var{'sc-1-ssh_username'}) {
        $job_gep_username = $job_var{'sc-1-ssh_username'};
    }
    if (exists $job_var{'_oam_vip'}) {
        $job_oam_vip_ip = $job_var{'_oam_vip'};
    }
    if (exists $job_var{'DAFT_SCRIPT_PATH'}) {
        $job_script_path = "$job_var{'DAFT_SCRIPT_PATH'}";
        if (-d "$job_script_path") {
            # Get the real path (e.g. /home/daft/tools is really /cluster/home/daft/tools
            $job_script_path = abs_path $job_script_path;
        }
    }
    if (exists $job_var{'scxb-default-ip_username'}) {
        $job_scx_advanced_password = $job_var{'scxb-default-ip_username'};
    }
    if (exists $job_var{'scxb-default-ip_address'}) {
        $job_scx_ip = $job_var{'scxb-default-ip_address'};
    }

    if ($verbose_level > 0) {
        $message .= "Used Job Variables:\n";
        $message .= "  job_bsp_ip=$job_bsp_ip\n";
        $message .= "  job_bsp_password=$job_bsp_password\n";
        $message .= "  job_bsp_username=$job_bsp_username\n";
        $message .= "  job_cluster_script_path=$job_cluster_script_path\n";
        $message .= "  job_connection_type=$job_connection_type\n";
        $message .= "  job_executing_on_cluster=$job_executing_on_cluster\n";
        $message .= "  job_gep_ip=$job_gep_ip\n";
        $message .= "  job_gep_password=$job_gep_password\n";
        $message .= "  job_gep_username=$job_gep_username\n";
        $message .= "  job_oam_vip_ip=$job_oam_vip_ip\n";
        $message .= "  job_script_path=$job_script_path\n";
        $message .= "  job_scx_advanced_password=$job_scx_advanced_password\n";
        $message .= "  job_scx_ip=$job_scx_ip\n";
        print_message_to_user($message);
    }
}

# -----------------------------------------------------------------------------
# If called with a value >0 then any messages printed will be using the
# General::Logging.pm module to print information to the screen.
# The default and if value is 0 is to just print normal text to the STDOUT.
#
# Input variables:
#    - 0=Print information to STDOUT
#      1=Print information using the General::Logging.pm module
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub set_use_logging {
    $use_logging = shift;
    unless ($use_logging =~ /^\d+$/) {
        # Invalid value
        print "set_use_logging: Invalid parameter '$use_logging' value\n";
        $use_logging = 0;
    }
}

# -----------------------------------------------------------------------------
# Set the verbosity level of the functions.
# This controls how much information is presented to the user.
#
# Input variables:
#    - Verbose level:
#      0=No information other than error messages.
#      1=Few status messages and error messages.
#      2=More details and error messages.
#      3=All details including executed commands and their results and error
#        messages.
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub set_verbose_level {
    my $new_level = shift;

    if ($new_level =~ /^\d+$/) {
        $verbose_level = $new_level;
    } else {
        print_error_message_to_user("set_verbose_level: Invalid parameter '$new_level' value\n");
    }
}

1;

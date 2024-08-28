#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.4
#  Date     : 2024-05-06 12:06:16
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2023-2024
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

use File::Basename qw(dirname basename);
use Cwd qw(abs_path cwd);
use lib dirname(dirname abs_path $0) . '/lib';

use ADP::Kubernetes_Operations;
use General::OS_Operations;

my $graceful_kill = 1;
my @kill_commands = ();
my $kill_traffic = 0;
my $log_file = "";
my $namespace = "eiffelesc-tools";
my $show_help = 0;

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "h|help"            => \$show_help,
    "k|kill-traffic"    => \$kill_traffic,
    "l|log-file=s"      => \$log_file,
    "n|namespace=s"     => \$namespace,
);

if ($show_help) {
    show_help();
    exit 0;
}

# Start logging of output to file
if ($log_file ne "") {
    General::Logging::log_enable($log_file);
    $log_file = abs_path $log_file;
}

find_and_print_traffic_pids();

if ($kill_traffic == 1 && @kill_commands) {
    kill_traffic_pids();
    # Now check to see if we killed everything, and if not we do make the pid's for forceful kill (-9)
    $graceful_kill = 0;
    find_and_print_traffic_pids();
    if (@kill_commands) {
        # Some processes still didn't get killed so now try to kill them with force and then check again.
        kill_traffic_pids();
        find_and_print_traffic_pids();
    }
}

# Stop logging of output to file
if ($log_file) {
    General::Logging::log_disable();
    print "\nDetailed Logfile written to $log_file\n";
}

exit 0;

# ***************
# * Subroutines *
# ***************

# -----------------------------------------------------------------------------
sub find_and_print_traffic_pids {
    my $found_cnt = 0;
    my $message;
    my @pid_info = ();
    my @pod_names;
    my $rc = 0;
    my @result;

    @kill_commands = ();

    # Find all pids
    print "Fetching running traffic processes.\n";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ps -fel",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ($rc != 0) {
        print "Failed to fetch process id's\n";
        return ();
    }

    # For example:
    #F S UID         PID   PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY          TIME CMD
    #4 S root          1      0  0  80   0 - 56382 -      Feb15 ?        00:00:24 /usr/lib/systemd/systemd --system --deserialize 22
    #  :
    #0 S eccd      66778      1  0  80   0 -  3061 do_wai 09:45 ?        00:00:00 sh -c /home/eccd/daft/bin/run_command_with_redirect.bash no_redirect no_redirect /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/configurationfiles/bsf_dscload_start_traffic.bash  > /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/logfiles/bsf_dscload_traffic_stdo
    #0 S eccd      66780  66778  0  80   0 -  3061 do_wai 09:45 ?        00:00:00 /bin/bash /home/eccd/daft/bin/run_command_with_redirect.bash no_redirect no_redirect /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/configurationfiles/bsf_dscload_start_traffic.bash
    #0 S eccd      66781  66780  0  80   0 - 189431 futex_ 09:45 ?       00:00:01 kubectl --kubeconfig=/home/eccd/.kube/config -n tools exec eric-dscload-deployment-6fcddb5dbf-9nwfz -- /opt/dsc-load/dsc_load
    #0 S eccd      66812      1  0  80   0 -  3061 do_wai 09:45 ?        00:00:00 sh -c /home/eccd/daft/bin/run_command_with_redirect.bash no_redirect no_redirect /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/configurationfiles/scp_k6_start_traffic.bash  > /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/logfiles/scp_k6_traffic_stdout.log 2>
    #0 S eccd      66814  66812  0  80   0 -  3061 do_wai 09:45 ?        00:00:00 /bin/bash /home/eccd/daft/bin/run_command_with_redirect.bash no_redirect no_redirect /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/configurationfiles/scp_k6_start_traffic.bash
    #0 S eccd      66815  66814  0  80   0 - 189495 futex_ 09:45 ?       00:00:02 kubectl --kubeconfig=/home/eccd/.kube/config -n tools exec eric-k6-deployment-544f54c496-v5fzr -- /usr/bin/k6 run /tests/scp_stability_test.js --no-color --no-summary --no-thresholds --no-usage-report --no-vu-connection-reuse=false -e SCP_HOST=scp.ericsson.se -e SCP_IP=214.14.14.147 -e SCP_PORT=443 -e
    #0 S eccd      66924      1  0  80   0 -  3061 do_wai 09:45 ?        00:00:00 sh -c /home/eccd/daft/bin/run_command_with_redirect.bash no_redirect no_redirect /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/configurationfiles/sepp_k6_start_traffic.bash  > /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/logfiles/sepp_k6_traffic_stdout.log 2
    #0 S eccd      66926  66924  0  80   0 -  3061 do_wai 09:45 ?        00:00:00 /bin/bash /home/eccd/daft/bin/run_command_with_redirect.bash no_redirect no_redirect /home/eccd/workspaces/Start_BSF_SCP_SEPP_Traffic_20230228_094541_12/configurationfiles/sepp_k6_start_traffic.bash
    #0 S eccd      66927  66926  0  80   0 - 189431 futex_ 09:45 ?       00:00:02 kubectl --kubeconfig=/home/eccd/.kube/config -n tools exec eric-k6-deployment-544f54c496-v5fzr -- /usr/bin/k6 run /tests/sepp_stability_test.js --no-color --no-summary --no-thresholds --no-usage-report -e CERT=RP_206033.crt -e KEY=RP_206033.key -e RPS=1000 -e DURATION=604000s -e MAX_VUS=40 -e SEPP_IP=2

    for (@result) {
        if (/^\d+\s+\S+\s+\S+\s+(\d+)\s+\d+\s+.+(run_command_with_redirect.bash|kubectl.+exec (eric-dscload-deployment-|eric-k6-deployment-).+(dsc_load|k6 run))/) {
            $found_cnt++;
            if ($graceful_kill == 1) {
                push @kill_commands, "kill $1";
            } else {
                push @kill_commands, "kill -9 $1";
            }
            push @pid_info, $_;
        } elsif (/^F\s+S\s+UID\s+PID\s+PPID.+/) {
            push @pid_info, $_;
        }
    }
    push @pid_info, "";

    print "Fetching traffic pod's in namespace $namespace.\n";
    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "hide-output"       => 1,
            "namespace"         => $namespace,
            "pod-include-list"  => [ "eric-dscload-deployment-", "eric-k6-deployment-" ],
        }
    );
    if (@pod_names) {
        for my $pod_name (@pod_names) {
            print "Checking for running traffic processes in Pod $pod_name\n";
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "kubectl -n $namespace exec $pod_name -- ps -fel",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                print "  Failed to fetch process id's from POD $pod_name\n";
                next;
            }
            # For example:
            #F S UID         PID   PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY          TIME CMD
            #4 S root          1      0  0  80   0 -   381 hrtime Feb17 ?        00:00:16 tail -f /dev/null
            #4 S root        277      0  7  80   0 - 235501 futex_ 14:44 ?       00:03:58 /usr/bin/k6 run /tests/sepp_stability_test.js -e CERT=RP_206033.crt -e KEY=RP_206033.key -e RPS=1000 -e DURATION=604000s -e MAX_VUS=40 -e SEPP_IP=214.14.14.145 -e SEPP_HOST=sepp1.region1.sepp.5gc.mnc073.mcc262.3gppnetwork.org -e SEPP_PORT=4
            #4 S root        300      0  0  80   0 -   400 do_wai 15:32 pts/0    00:00:00 sh
            #0 R root        311    300  0  80   0 -   406 -      15:37 pts/0    00:00:00 ps -fel
            #
            #    or
            #
            #F S UID         PID   PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY          TIME CMD
            #4 S root          1      0  0  80   0 -  1087 hrtime Feb17 ?        00:00:17 tail -f /dev/null
            #4 S root     140388      0 22  80   0 - 1112420 futex_ 09:08 ?      00:00:08 /opt/dsc-load/dsc_load
            #4 S root     140556      0  0  80   0 -  3551 do_wai 09:09 pts/0    00:00:00 sh
            #0 R root     140574 140556  0  80   0 -  8719 -      09:09 pts/0    00:00:00 ps -fel
            $message = "Traffic Processes in pod $pod_name:";
            push @pid_info, $message;
            push @pid_info, "-" x length($message);
            for (@result) {
                if (/^\d+\s+\S+\s+\S+\s+(\d+)\s+\d+.+(k6 run|dsc_load).*/) {
                    $found_cnt++;
                    if ($graceful_kill == 1) {
                        push @kill_commands, "kubectl -n $namespace exec $pod_name -- kill $1";
                    } else {
                        push @kill_commands, "kubectl -n $namespace exec $pod_name -- kill -9 $1";
                    }
                    push @pid_info, $_;
                } elsif (/^F\s+S\s+UID\s+PID\s+PPID.+/) {
                    push @pid_info, $_;
                }
            }
            push @pid_info, "";
        }
    }

    if ($found_cnt > 0) {
        print "\nTraffic process information:\n";
        print   "----------------------------\n";
        for (@pid_info) {
            print "$_\n";
        }

        print "\nCommands to stop the traffic:\n";
        print   "-----------------------------\n";
        for (@kill_commands) {
            print "$_\n";
        }
    } else {
        print "\nNo traffic processes found\n";
    }
}

# -----------------------------------------------------------------------------
sub kill_traffic_pids {
    my $command;
    my $rc = 0;
    my @result;


    print "\nKilling traffic processes:\n";
    print   "--------------------------\n";
    for $command (@kill_commands) {
        print "Executing: $command\n";
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            print "  Failed to kill the process, rc=$rc\n";
            next;
        }
    }
}

# -----------------------------------------------------------------------------
sub show_help {
        print <<EOF;

Description:
============

This script checks if any traffic processes are running and gives the commands
to stop them.

What it looks for are any processes that contains the following commands:
    - run_command_with_redirect.bash
    - kubectl...exec eric-dscload-deployment-...dsc_load
    - kubectl...exec eric-k6-deployment-...k6 run

And in the eric-dscload-deployment- pod's any processes that contain the
following commands:
    - dsc_load

And in the eric-k6-deployment- pod's any processes that contain the
following commands:
    - k6 run


Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:

  -h                    | --help
  -k                    | --kill-traffic
  -l <file path>        | --log-file <file path>
  -n <string>           | --namespace <string>

where:

  -h
  --help
  ------
  Shows this help information.


  -k
  --kill-traffic
  --------------
  If traffic processes found, then also kill these processes.
  By default if this parameter is not given then only the commands to
  kill the processes will be printed.


  --log-file <file path>
  -l <file path>
  -------------------------
  The file to write detailed log information about executed commands
  and their printouts.
  If not specified then no detailed log file is created.


  --namespace <string>
  -n <string>
  --------------------
  The namespace in which the traffic tools dscload and k6 are installed.
  If not specified then the default '$namespace' is assumed.
EOF
}

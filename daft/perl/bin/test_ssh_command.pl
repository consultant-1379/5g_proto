#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2023-12-06 17:13:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2023
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

use File::Basename qw(dirname);
use Cwd qw(abs_path);
use lib dirname(dirname abs_path $0) . '/lib';
use version;

use General::Logging;
use General::OS_Operations;

my @commands = ();
my $expect_debug = 0;
my $ip="localhost";
my $logfile = "";
my $password = "notneeded";
my $port = 22;
my $rc;
my @result;
my $show_help = 0;
my @std_error = ();
my $stop_on_error = 0;
my $timeout = -1;
my $user = $ENV{'USER'};
my $verbose = 0;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "c|command=s"              => \@commands,
    "d|debug"                  => \$expect_debug,
    "h|help"                   => \$show_help,
    "i|ip=s"                   => \$ip,
    "l|log-file=s"             => \$logfile,
    "P|password=s"             => \$password,
    "p|port=i"                 => \$port,
    "s|stop-on-error"          => \$stop_on_error,
    "t|timeout=i"              => \$timeout,
    "u|user=s"                 => \$user,
    "v|verbose"                => \$verbose,
);

if ( $show_help ) {
    show_help();
    exit 0;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Start logging of output to file
if ($logfile) {
    $rc = General::Logging::log_enable("$logfile");
    if ($rc != 0) {
        exit 1;
    }
    $logfile = abs_path $logfile;
}

if (@commands) {
    $rc = General::OS_Operations::send_command_via_ssh(
        {
            "commands"            => \@commands,
            "expect-debug"        => $expect_debug,
            "hide-output"         => ($verbose ? 0 : 1),
            "ip"                  => $ip,
            "port"                => $port,
            "password"            => $password,
            "return-output"       => \@result,
            "return-stderr"       => \@std_error,
            "stop-on-error"       => $stop_on_error,
            "timeout"             => $timeout,
            "user"                => $user,
        }
    );
} else {
    print "No commands to execute\n";
}

if (@std_error) {
    print "\nBegin STDERR Output:\n";
    print "="x80 . "\n";
    for (@std_error) {
        print "$_\n";
    }
    print "="x80 . "\n";
    print "End STDERR Output\n";
}

if ($verbose == 0 && @result) {
    print "\nBegin STDOUT Output:\n";
    print "="x80 . "\n";
    for (@result) {
        print "$_\n";
    }
    print "="x80 . "\n";
    print "End STDOUT Output\n";
}

# Stop logging of output to file
if ($logfile) {
    General::Logging::log_disable();
    print "\nDetailed log file written to $logfile\n" if $verbose;
}

exit $rc;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub show_help {
    print <<EOF;

Description:
============

This script executes one or more commands using the snd_command_to_ssh.exep script
and it's only used for testing the General::OS_Operations::send_command_via_ssh
function.


Syntax:
=======

$0 <MANDATORY> [<OPTIONAL>]

<MANDATORY> is one of the following parameters:

  -c <string>           | --command <string>

<OPTIONAL> are one or more of the following parameters:

  -d                    | --debug
  -h                    | --help
  -i <string>           | --ip <string>
  -l <filename>         | --log-file <filename>
  -P <string>           | --password <string>
  -p <integer>          | --port <integer>
  -s                    | --stop-on-error
  -t <integer>          | --timeout <integer>
  -u <string>           | --user <string>
  -v                    | --verbose

where:

  -c <string>
  --command <string>
  -----------------------
  The command to be executed.
  It can be specified multiple times if more than one command should be executed.


  -d
  --debug
  -----------------------
  If specified then detailed expect debug printouts will be collected and printed
  to the user.


  -h
  --help
  ------
  Shows this help information.


  -i <string>
  --ip <string>
  -------------
  If specified it should be an IP-address or a host name to connect to with SSH.
  If not specified or when using 'localhost' then the commands are sent to the
  localhost instead of via ssh.


  -l <filename>
  --log-file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  By default no log file is created.


  -P <string>
  --password <string>
  -------------------
  The password to be used when logging in to the remote node.


  -p <integer>
  --port <integer>
  ----------------
  The port to use for logging in to the remote node, if not specified then port 22
  is used.

  -s
  --stop-on-error
  ---------------
  If specified then execution of commands will stop immediately when an error is detected.
  By default all commands will be executed and then only if there was an error was detected
  will it be reported at the end of the last command.


  -t <integer>
  --timeout <integer>
  -------------------
  Specifies the default timeout to use when executing commands, by default there is no timeout
  which is the same as giving timeout value -1.


  -u <string>
  --user <string>
  ---------------
  The user to be used when logging in to the remote node.


  -v
  --verbose
  ---------
  Specify this if you want to see the output of the commands as they are being executed.
  If not specified then a printout is only printed after all commands has been executed.


Examples:
=========

  - Execute a command on localhost (without ssh):
    $0 -c hostname

  - Execute a command on a remote node (using ssh):
    $0 -c hostname -i 10.221.169.167 -u eccd -p notneeded


Return code:
============

   0:  Successful, the execution was successful.
   >0: Unsuccessful, some failure was reported.

EOF
}

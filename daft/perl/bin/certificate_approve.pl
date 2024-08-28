#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2022-01-26 18:42:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022
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

use ADP::Kubernetes_Operations;
use General::Logging;

my $delay_time = 10;
my $kubeconfig = "";
my $logfile = "";
my $max_attempts = 0;
my $max_time = 0;
my $rc = 0;
my $repeated_checks = 0;
my $show_help = 0;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|max-attempts=i"         => \$max_attempts,
    "d|delay-time=i"           => \$delay_time,
    "f|log_file=s"             => \$logfile,
    "h|help"                   => \$show_help,
    "t|max-time=i"             => \$max_time,
    "kubeconfig=s"             => \$kubeconfig,
);

if ( $show_help ) {
    show_help();
    exit 0;
}

if ($kubeconfig ne "" && -f "$kubeconfig") {
    ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);
} elsif ($kubeconfig ne "") {
    print "The kubeconfig file does not exist\n";
    exit 1;
}

if ($max_attempts != 0 || $max_time != 0) {
    $repeated_checks = 1;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Start logging of output to file
if ($logfile) {
    General::Logging::log_enable("$logfile");
}

$rc = ADP::Kubernetes_Operations::approve_pending_certificates(
    {
        "delay-time"    => $delay_time,
        "hide-output"   => 0,
        "max-attempts"  => $max_attempts,
        "max-time"      => $max_time,
        "repeated-checks"   => $repeated_checks,
    }
);

# Stop logging of output to file
if ($logfile) {
    General::Logging::log_disable();
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

This script checks if there are Pending Certificate Signing Requests and if so
approves them all.

The commands used by this script are:

  kubectl get csr
  kubectl certificate approve <names...>


Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:

  -a <integer>  | --max-attempts <integer>
  -d <integer>  | --delay-time <integer>
  -f <filename> | --log_file <filename>
  -h            | --help
  -t <integer>  | --max-time <integer>
                  --kubeconfig <filename>

where:

  -a <integer>
  --max-attempts <integer>
  ------------------------
  Set the maximum number of attempts the script will try to check if there are
  Pending Certificate Signing Requests.
  If not specified then the check is only done once.
  See also parameter "max-time".


  -d <integer>
  --delay-time <integer>
  ----------------------
  Set the delay time between each repeated check.
  If not specified then the default value is 10 seconds.
  See also parameters "max-attempts" and "max-time".


  -f <filename>
  --log-file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  If not specified then no log file is created and only what is printed to STDOUT
  will be shown.


  -h
  --help
  ------
  Shows this help information.


  -t <integer>
  --max-time <integer>
  --------------------
  Set the maximum number of seconds the script will try to check if there are
  Pending Certificate Signing Requests.
  If not specified then the check is only done once.
  See also parameter "max-attempts".


  --kubeconfig <filename>
  -----------------------
  If specified then it will use this file as the config file for helm and
  kubectl commands.
  If not specified then it will use the default file which is normally the
  file ~/.kube/config.

EOF
}

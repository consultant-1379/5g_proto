#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.2
#  Date     : 2021-12-10 17:51:39
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021
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
use General::Logging;

my $command = "values";
my @filter = ();
my $kubeconfig = "";
my $logfile = "";
my $namespace = "";
my $output_format = "raw";
my $release_name = "";
my $rc;
my @result = ();
my $revision = "";
my $show_help = 0;

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "c|command=s"               => \$command,
    "f|filter=s"                => \@filter,
    "h|help"                    => \$show_help,
    "kubeconfig=s"              => \$kubeconfig,
    "l|log-file=s"              => \$logfile,
    "n|namespace=s"             => \$namespace,
    "o|output-format=s"         => \$output_format,
    "r|release-name=s"          => \$release_name,
    "v|revision=i"              => \$revision,
);

if ($show_help) {
    show_help();
    exit 0;
}

if ($kubeconfig ne "" && -f "$kubeconfig") {
    # Set kubeconfig for any used library functions
    ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);
} elsif ($kubeconfig ne "") {
    print "The kubeconfig file does not exist\n";
    exit 1;
}

# Start logging of output to file
if ($logfile) {
    General::Logging::log_enable("$logfile");
}

if (@filter) {
    $rc = ADP::Kubernetes_Operations::get_helm_release_extended_information(
        {
            "command"         => $command,
            "hide-output"     => 1,
            "namespace"       => $namespace,
            "output-format"   => $output_format,
            "release-name"    => $release_name,
            "return-output"   => \@result,
            "return-patterns" => \@filter,
        }
    );
} else {
    $rc = ADP::Kubernetes_Operations::get_helm_release_extended_information(
        {
            "command"         => $command,
            "hide-output"     => 1,
            "namespace"       => $namespace,
            "output-format"   => $output_format,
            "release-name"    => $release_name,
            "return-output"   => \@result,
        }
    );
}
if ($rc != 0) {
    print "Problem to get helm extended information\n";
    exit 1;
}


for (@result) {
    print "$_\n";
}

# Stop logging of output to file
if ($logfile) {
    General::Logging::log_disable();
}

exit 0;

# ***************
# * Subroutines *
# ***************

# -----------------------------------------------------------------------------
sub show_help {
        print <<EOF;

Description:
============

This script parse the data from the 'helm get <command> ...' command and print
the fetched data possibly filtered with some user specified strings.
This script is mainly used to test the function get_helm_release_extended_information
subroutine in the ADP::Kubernetes_Operations.pm file.


Syntax:
=======

$0 [<OPTIONAL>] <MANDATORY>

<MANDATORY> are all of the following parameters:

  -r <string>           | --release-name <string>

<OPTIONAL> are one or more of the following parameters:

  -c <string>           | --command <string>
  -f <string>           | --filter <string>
  -h                    | --help
                          --kubeconfig <filename>
  -l <string>           | --log-file <string>
  -n <string>           | --namespace <string>
  -o <string>           | --output-format <string>
  -v <integer>          | --revision <integer>

where:

  --command <string>
  -c <string>
  ------------------
  Specifies the command to use.
  Example:
     -c values
     -c "values --all"

  If not specified then the default is '-c values'.


  --filter <string>
  -f <string>
  -----------------
  This parameter can be used to limit the output to lines that contains
  the specified string.
  The parameter can be specified multiple times if more than one
  filter should be applied.
  Example:
  -f global
  -f 'certificate.+certificate'


  -h
  --help
  ------
  Shows this help information.


  --kubeconfig <filename>
  -----------------------
  If specified then it will use this file as the config file for helm and
  kubectl commands.
  If not specified then it will use the default file which is normally the
  file ~/.kube/config.


  --log-file <string>
  -l <string>
  -------------------
  The file to write the collected command output to.
  If not specified then no log file is created and the only thing visible
  is the result returned to the user.


  --namespace <string>
  -n <string>
  --------------------
  The namespace to print data from, this parameter can be left out if the
  used .kube/config file contains the specic namespace otherwise it's mandatory.


  --output-format <string>
  -o <string>
  ------------------------
  Specify the output format to print.
  Only allowed values are:
  -o dot-separated
  -o raw

  If not specified then the default is '-o raw'


  --release-name <string>
  -r <string>
  -----------------------
  The release name to print data from, this parameter is mandatory and must be
  specified.


  --revision <integer>
  -v <integer>
  --------------------
  Specifes the release to print data from, if only needs to be specified if a release name
  have more than one version and you want to print data from any release other than the last.
EOF
}

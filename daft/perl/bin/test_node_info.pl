#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2022-05-04 16:09:00
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

use File::Basename qw(dirname basename);
use Cwd qw(abs_path cwd);
use lib dirname(dirname abs_path $0) . '/lib';

use ADP::Kubernetes_Operations;

my $filter = "";
my $kubeconfig = "";
my %node_data;
my $node_index = "";
my $node_name = "";
my $node_type = "";
my $show_help = 0;
my $show_resources = 0;
my $use_data_dumper = 0;

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "f|filter=s"                => \$filter,
    "h|help"                    => \$show_help,
    "i|index=i"                 => \$node_index,
    "n|name=s"                  => \$node_name,
    "r|resources"               => \$show_resources,
    "t|type=s"                  => \$node_type,
    "kubeconfig=s"              => \$kubeconfig,
    "use-data-dumper"           => \$use_data_dumper,
);

if ($show_help) {
    show_help();
    exit 0;
}

if ($kubeconfig ne "" && -f "$kubeconfig") {
    ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);
} elsif ($kubeconfig ne "") {
    print "The kubeconfig file does not exist\n";
    exit 1;
}

if ($use_data_dumper) {
    use Data::Dumper;
}

if ($show_resources) {
    %node_data = ADP::Kubernetes_Operations::get_node_resources(
        {
            "name"          => $node_name,
            "index"         => $node_index,
            "type"          => $node_type,
        }
    );

    if ($use_data_dumper) {
        $Data::Dumper::Sortkeys = 1;
        print Dumper(%node_data);
    }

    for my $node_name (sort keys %node_data) {
        print "\n$node_name:\n";
        if (exists $node_data{$node_name}{'Allocated resources'}) {
            print "\n    Allocated resources:\n";
            for my $section (sort keys %{$node_data{$node_name}{'Allocated resources'}}) {
                printf "        %-35s  %s%s\n", $section, $node_data{$node_name}{'Allocated resources'}{$section}, ($section =~ /Percent$/ ? '%' : '');
            }
        }
    }
} else {
    %node_data = ADP::Kubernetes_Operations::get_node_information(
        {
            "name"          => $node_name,
            "index"         => $node_index,
            "return-filter" => $filter,
            "type"          => $node_type,
        }
    );

    if ($use_data_dumper) {
        $Data::Dumper::Sortkeys = 1;
        print Dumper(%node_data);
    }

    for my $node_name (sort keys %node_data) {
        print "\n$node_name:\n";
        for my $section (sort keys %{$node_data{$node_name}}) {
            print "\n    $section:\n";
            for (@{$node_data{$node_name}{$section}}) {
                print "        $_\n";
            }
        }
    }
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

This print information about the kubernetes nodes to screen and makes it possible
to easy filter out only wanted information.
This script is mainly used to test the Perl module functions:

    - ADP::Kubernetes_Operations::get_node_information
    - ADP::Kubernetes_Operations::get_node_resources


Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:

  -f <string>           | --filter <string>
  -h                    | --help
  -i <integer>          | --index <integer>
  -n <string>           | --name <string>
  -r                    | --resources
  -t <string>           | --type <string>
                          --kubeconfig <file path>
                          --use-data-dumper


where:

  --filter <string>
  -f <string>
  -----------------
  A Perl regular expression that can be used to filter out so only the
  matching information is printed.
  For example: -f 'System Info'


  -h
  --help
  ------
  Shows this help information.


  --print-data
  -p
  ------------
  Print the parsed data to the screen.


  --index <integer>
  -i <integer>
  -----------------
  Only print data for the node index specified, the index is the order
  of the nodes printed with the 'kubectl get nodes' command and it is
  starting with index 0.
  If not specified and also not '--name' is specified then data for
  all nodes will be printed.


  --name <string>
  -n <string>
  ---------------
  Only print data for the node name specified.
  If not specified and also not '--index' is specified then data for
  all nodes will be printed.


  --resources
  -r
  -----------
  Only print resource data for the node, i.e. how many CPU, Memory and disk
  space the node is using.
  If not specified then all data for the node will be printed.
  If specified together with '--filter' then the '--filter' value is ignored.


  --type <string>
  -t <string>
  ---------------
  Only print data for the nodes that match the type, currently the following
  types are allowed:

    - master
    - worker

  If not specified then data for all node types will be printed.


  --kubeconfig <file path>
  ------------------------
  The file that should be used for reaching the kubernetes cluster, by
  default it is '$ENV{HOME}/.kube/config' if not specified.


  --use-data-dumper
  -----------------
  Print Perl internal data structures of the parsed YAML data using the
  Data::Dumper module.
EOF
}

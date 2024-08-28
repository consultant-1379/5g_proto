#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.5
#  Date     : 2024-03-12 19:26:00
#
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022,2024
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
use General::OS_Operations;

my $command;
my $director_ip = "";
my $first_worker_eth2_ipv4 = "";
my $first_worker_eth2_ipv6 = "";
my $first_worker_ip = "";
my $force_labeling = 0;
my $hide_output = 1;
my $just_print = 0;
my $kubeconfig = "";
my $label_with_ipv4_needed = 0;
my $label_with_ipv6_needed = 0;
my @labeled_node_names_ipv4 = ();
my @labeled_node_names_ipv6 = ();
my $logfile = "";
my $rc;
my @result = ();
my $same;
my $show_help = 0;
my @worker_node_names = ();

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "d|director-ip=s"           => \$director_ip,
    "f|force"                   => \$force_labeling,
    "h|help"                    => \$show_help,
    "kubeconfig=s"              => \$kubeconfig,
    "l|log-file=s"              => \$logfile,
    "p|just-print"              => \$just_print,
);

if ($show_help) {
    show_help();
    exit 0;
}

if ($kubeconfig ne "" && -f "$kubeconfig") {
    # Set kubeconfig for any used library functions
    ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);
    $kubeconfig = " --kubeconfig $kubeconfig";
} elsif ($kubeconfig ne "") {
    print "The kubeconfig file does not exist\n";
    exit 1;
}

# Start logging of output to file
if ($logfile) {
    General::Logging::log_enable("$logfile");
} else {
    $hide_output = 0;
}

# Fetch worker node names
print "\nGet a list of worker nodes.\n";
@worker_node_names = ADP::Kubernetes_Operations::get_worker_nodes();

# Fetch IP address of first worker node
print "\nGet IP address of first worker node.\n";
$first_worker_ip = ADP::Kubernetes_Operations::get_node_ip(
    {
        "index" => 0,
        "type"  => "worker",
    }
);
if ($first_worker_ip eq "") {
    print "Unable to find IP address of first worker node\n";
    # Stop logging of output to file
    if ($logfile) {
        General::Logging::log_disable();
    }
    exit 1;
}

# Fetch IP address information for eth2 from first worker
print "\nGet IPv4 and IPv6 addresses of eth2 on first worker node.\n";
$command = "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$first_worker_ip ip addr show eth2";
if ($director_ip eq "") {
    # We are executing the script already on the directory so we should have direct access to
    # login to the worker node.
    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => $hide_output,
            "return-output" => \@result,
        }
    );
} else {
    # We are executing the script from a remote node so we need to connect via the director
    # and from there accessing the working node.
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ssh -t -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$director_ip $command",
            "hide-output"   => $hide_output,
            "return-output" => \@result,
        }
    );
}
if ($rc != 0) {
    print "Failed to get IP address for eth2 from first worker node.\n" . join "\n", @result;
    # Stop logging of output to file
    if ($logfile) {
        General::Logging::log_disable();
    }
    exit 1;
}
# Find the IPv4 and IPv6 address
#4: eth2: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
#    link/ether fa:16:3e:65:41:35 brd ff:ff:ff:ff:ff:ff
#    altname enp0s5
#    altname ens5
#    inet 10.117.41.78/28 brd 10.117.41.79 scope global eth2
#    valid_lft forever preferred_lft forever
#    inet6 2001:1b70:8230:700::a/64 scope global
#    valid_lft forever preferred_lft forever
#    inet6 fe80::f816:3eff:fe65:4135/64 scope link
#    valid_lft forever preferred_lft forever
#Connection to 10.117.19.203 closed.
for (@result) {
    if (/^\s*inet\s+(\S+)\/\d+\s+/) {
        $first_worker_eth2_ipv4 = $1;
    } elsif (/^\s+inet6 (\S+)\/\d+\s+.+/) {
        if ($1 !~ /^fe80::/i) {
            $first_worker_eth2_ipv6 = $1;
            $first_worker_eth2_ipv6 =~ s/:/_/g; # Replace : with _
        }
    }
}
if ($first_worker_eth2_ipv4 eq "" && $first_worker_eth2_ipv6 eq "") {
    # We did not find the IP address
    print "Failed to find IP address for eth2 from first worker node.\n" . join "\n", @result;
    # Stop logging of output to file
    if ($logfile) {
        General::Logging::log_disable();
    }
    exit 1;
}

# Check if nodes are already labeled
# We need to turn off warnings before using the '~~' smartmatch operator on
# newer versions of Perl. We could have used the following on newer version
# to get the same result, but then on older versions we would get a compile error
# because the 'experimental feature did not exists:
# use experimental 'smartmatch';
# So turning of warnings just for this statement is the best option for both cases.
if ($first_worker_eth2_ipv4 ne "") {
    # Fetch worker node names with label ci/ip
    print "\nGet a list of nodes already labeled with 'ci/ip'.\n";
    @labeled_node_names_ipv4 = ADP::Kubernetes_Operations::get_nodes_with_label("ci/ip");

    no warnings;    # Needed to avoid a warning when executing ~~ on newer Perl versions
    # The ~~ operator compares if the contents of the two arrays are exactly the same.
    # The order matters, the potential smaller array should be on the left.
    $same = @labeled_node_names_ipv4 ~~ @worker_node_names;
    use warnings;
    if ($same == 1) {
        if ($force_labeling == 0) {
            print "All the worker nodes are already labeled with 'ci/ip', so no 'ci/ip' labeling will be done.\n";
        } else {
            print "All the worker nodes are already labeled with 'ci/ip', but '--force' given so they will be re-labeled.\n";
            $label_with_ipv4_needed = 1;
        }
    } elsif (scalar @labeled_node_names_ipv4 == 0) {
        print "No nodes are labeled with 'ci/ip', we need to label all worker nodes.\n";
        $label_with_ipv4_needed = 1;
    } elsif (scalar @labeled_node_names_ipv4 > 0) {
        print "Some nodes but not all are already labeled with 'ci/ip', to be safe we re-label all worker nodes.\n";
        $label_with_ipv4_needed = 1;
    }
}
if ($first_worker_eth2_ipv6 ne "") {
    # Fetch worker node names with label ci/ip6
    print "\nGet a list of nodes already labeled with 'ci/ip6'.\n";
    @labeled_node_names_ipv6 = ADP::Kubernetes_Operations::get_nodes_with_label("ci/ip6");

    no warnings;    # Needed to avoid a warning when executing ~~ on newer Perl versions
    # The ~~ operator compares if the contents of the two arrays are exactly the same.
    # The order matters, the potential smaller array should be on the left.
    $same = @labeled_node_names_ipv6 ~~ @worker_node_names;
    use warnings;
    if ($same == 1) {
        if ($force_labeling == 0) {
            print "All the worker nodes are already labeled with 'ci/ip6', so no 'ci/ip6' labeling will be done.\n";
        } else {
            print "All the worker nodes are already labeled with 'ci/ip6', but '--force' given so they will be re-labeled.\n";
            $label_with_ipv6_needed = 1;
        }
    } elsif (scalar @labeled_node_names_ipv6 == 0) {
        print "No nodes are labeled with 'ci/ip6', we need to label all worker nodes.\n";
        $label_with_ipv6_needed = 1;
    } elsif (scalar @labeled_node_names_ipv6 > 0) {
        print "Some nodes but not all are already labeled with 'ci/ip6', to be safe we re-label all worker nodes.\n";
        $label_with_ipv6_needed = 1;
    }
}
if ($label_with_ipv4_needed == 0 && $label_with_ipv6_needed == 0) {
    # None of the labels 'ci/ip' or 'ci/ip6' need to be added.
    print "\nNo need to add label 'ci/ip' and/or 'ci/ip6' to any of the nodes because they are already present.\n";
    # Stop logging of output to file
    if ($logfile) {
        General::Logging::log_disable();
    }
    exit 0;
}

# If we come here then at least one of the labels need to be added to one or more nodes

# Print label information before changing anything, ignore any errors
print "Print all labels of nodes before changing anything.\n";
General::OS_Operations::send_command(
    {
        "command"       => "kubectl get nodes -o wide --show-labels $kubeconfig",
        "hide-output"   => $hide_output,
    }
);

# Now add or replace the label for all worker nodes
for my $node (@worker_node_names) {
    if ($label_with_ipv4_needed == 1 && $label_with_ipv6_needed == 1) {
        print "\nAdd or change label 'ci/ip=$first_worker_eth2_ipv4' and 'ci/ip6=$first_worker_eth2_ipv6' on node $node.\n";
        $command = "kubectl label nodes $node ci/ip=$first_worker_eth2_ipv4 ci/ip6=$first_worker_eth2_ipv6 --overwrite $kubeconfig";
    } elsif ($label_with_ipv4_needed == 1) {
        print "\nAdd or change label 'ci/ip=$first_worker_eth2_ipv4' on node $node.\n";
        $command = "kubectl label nodes $node ci/ip=$first_worker_eth2_ipv4 --overwrite $kubeconfig";
    } elsif ($label_with_ipv6_needed == 1) {
        print "\nAdd or change label 'ci/ip6=$first_worker_eth2_ipv6' on node $node.\n";
        $command = "kubectl label nodes $node ci/ip6=$first_worker_eth2_ipv6 --overwrite $kubeconfig";
    }

    if ($just_print == 1) {
        print "Not executed: $command\n";
    } else {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            print "Failed to set label on node $node.\n" . join "\n", @result;
            # Stop logging of output to file
            if ($logfile) {
                General::Logging::log_disable();
            }
            exit 1;
        }
    }
}

if ($just_print == 0) {
    # Print label information after changing anything, ignore any errors
    print "\nPrint all labels of nodes after changing anything.\n";
    General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes -o wide --show-labels $kubeconfig",
            "hide-output"   => $hide_output,
        }
    );
}

if ($label_with_ipv4_needed == 1 && $label_with_ipv6_needed == 1) {
    print "\nLabel 'ci/ip=$first_worker_eth2_ipv4' and 'ci/ip6=$first_worker_eth2_ipv6' added to all worker nodes.\n";
} elsif ($label_with_ipv4_needed == 1) {
    print "\nLabel 'ci/ip=$first_worker_eth2_ipv4' added to all worker nodes.\n";
} elsif ($label_with_ipv6_needed == 1) {
    print "\nLabel 'ci/ip6=$first_worker_eth2_ipv6' added to all worker nodes.\n";
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

This script will add the label 'ci/ip=<ipv4 address>' and/or 'ci/ip6=<ipv6 address>
to all worker nodes where the <ipv4 address> is the IPv4 address and <ipv6 address>
is the IPv6 address for interface eth2 from the first worker node.
This is used by the CI framework used during design for sending traffic into
the node.


Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:

  -d <ip address>       | --director-ip <ip address>
  -f                    | --force
  -h                    | --help
                          --kubeconfig <filename>
  -l <string>           | --log-file <string>
  -p                    | --just-print

where:

  --director-ip <ip address>
  -d <ip address>
  --------------------------
  If the script is executed from outside of the director node with no direct
  login access to the worker nodes then this parameter specifies the IP address
  of the director or control-plane node that have access to login to the worker.
  It uses this IP address to connect to user 'eccd' on the node which must be
  possible to login to without specifying a password.

  If not specified then it's assumed that it is possible to login directly to
  the first worker IP address.


  -f
  --force
  -------
  If specified and the node is already labeled then it will be re-labeled i.e.
  the old labels will be overwritten.
  By default, if all worker nodes have the 'ci/ip' label then the script will
  just exit without doing any changes.


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


  --just-print
  -p
  ------------
  If specified then no changes of the labels are done, it will just print
  information about what changes are needed, if any.
EOF
}

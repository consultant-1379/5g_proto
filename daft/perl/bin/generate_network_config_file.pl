#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.31
#  Date     : 2024-06-13 11:59:35
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2024
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
use Cwd qw(abs_path);
use File::Basename;

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $debug = 0;
    # 0: No debug information printed
    # 1: Minimal level of debug information printed
    # 2: Medium level of debug information printed
    # 3: Max level of debug information printed
my @duplicate_parameters = ();
my $filename = "";
my $generate_param_help = 0;
my $keep_missing_parameters = 0;
my @new_data = ();
my @missing_in_template = ();
my @missing_in_template_but_added_back = ();
my @missing_replacemewith = ();
my $only_changeme = 0;
my @old_data = ();
my $old_file = "";
my %old_tags;
my $output_file = "";
my $output_format = "";
my @parameters = ();
my %replacemewith;
my $script_path = abs_path($0);
my $show_help = 0;
my $status_file = "";
my $strip_comments = 0;
my $strip_empty_lines = 0;
my @template_data = ();
my $template_file = "";
my %template_tags;
my @updated_values = ();

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;            # standard Perl module
GetOptions (
    "d|debug=i"              => \$debug,
    "g|generate-param-help"  => \$generate_param_help,
    "k|keep-missing-params"  => \$keep_missing_parameters,
    "f|old-file=s"           => \$old_file,
    "m|only-changeme"        => \$only_changeme,
    "o|output-file=s"        => \$output_file,
    "p|parameter=s"          => \@parameters,
    "s|status-file=s"        => \$status_file,
    "c|strip-comments"       => \$strip_comments,
    "e|strip-empty-lines"    => \$strip_empty_lines,
    "t|template-file=s"      => \$template_file,
    "h|help"                 => \$show_help,
    "r|output-format=s"      => \$output_format,
);

if ($show_help) {
    show_help();
    exit 0;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

if ($output_format ne "" && $output_format !~ /^(one-line|multiple-lines)$/) {
    print "The parameter -r or --output-format can only have the values:\n - one-line\n - multiple-lines\n\n";
    exit 1;
}

if ($template_file) {
    # Read template file
    read_file($template_file, \@template_data, $strip_comments);
} else {
    $template_file = dirname($script_path) . "/../../templates/Network_Config_Template.xml";
    if (-f $template_file) {
        read_file($template_file, \@template_data);
    } else {
        print "Unable to find a template file to use under file:\n\n";
        print "$template_file\n\n";
        print "Please specify one with parameter --template-file\n";
        exit 1;
    }
}
print STDERR "\nParsing tags in the file template_file:\n---------------------------------------\n\n" if $debug > 0;
parse_tag_data(\@template_data, \%template_tags, $strip_comments);

if ($old_file) {
    # Read old network configuration file, always strip away comments
    read_file($old_file, \@old_data, 1);
    print STDERR "\nParsing tags in the file old_file:\n----------------------------------\n\n" if $debug > 0;
    parse_tag_data(\@old_data, \%old_tags, 1);

    if ($debug > 0) {
        print STDERR "\nTags found in the file $old_file:\n\n";
        for my $key (sort keys %old_tags) {
            print STDERR "Tag=$key\n";
            for my $param (sort keys %{$old_tags{$key}}) {
                print STDERR "  $param=\"$old_tags{$key}{$param}\"\n";
            }
            print STDERR "\n";
        }
    }

    if (@parameters) {
        # Some specific parameters should have their values set
        for (@parameters) {
            if (/^(\S+)\.(\S+?)=(.+)/) {
                $old_tags{$1}{$2} = $3;
            } elsif (/^(\S+?)=(.+)/) {
                $old_tags{$1}{'value'} = $2;
            } else {
                print "Wrong parameter format ($_), should be <name>=<value>\n";
            }
        }
    }

    # Temporary solution to copy over values from old variables to new variables.
    my $execute_temporary_fix = 0;
    print STDERR "\nExecuting temporary fix number $execute_temporary_fix:\n---------------------------------\n\n" if ($debug > 0 && $execute_temporary_fix > 0);
    if ($execute_temporary_fix == 1) {
        # Added this fix for DND-45402 (Base Traffic Adaptations for Mixed Deployments and IPv6InteralIPv6External) to copy the
        # values from the old variables into the new variables at the beginning and then overwrite all variables where the
        # template file contains REPLACEMEWITH_xxxx values.
        if (exists $template_tags{'cluster_ssh_vip'} && ! exists $old_tags{'cluster_ssh_vip'} && exists $old_tags{'node_vip_address'}) {
            $old_tags{'cluster_ssh_vip'}{'value'} = $old_tags{'node_vip_address'}{'value'};
        }
        if (exists $template_tags{'cluster_ssh_vip_tools'} && ! exists $old_tags{'cluster_ssh_vip_tools'} && exists $old_tags{'node_vip_address'}) {
            $old_tags{'cluster_ssh_vip_tools'}{'value'} = $old_tags{'node_vip_address'}{'value'};
        }

        if (exists $template_tags{'oam_vip_ipv4'} && ! exists $old_tags{'oam_vip_ipv4'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_OAM'}) {
            $old_tags{'oam_vip_ipv4'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_OAM'}{'value'};
            $old_tags{'oam_vip_ipv4'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'oam_vip_ipv6'} && ! exists $old_tags{'oam_vip_ipv6'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_OAM'}) {
            $old_tags{'oam_vip_ipv6'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_OAM'}{'value_IntIPv6ExtIPv6'};
            $old_tags{'oam_vip_ipv6'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'snmp_trap_target'} && ! exists $old_tags{'snmp_trap_target'} && exists $old_tags{'snmp_secret_hash_parameter_trapTargets.0.address'}) {
            $old_tags{'snmp_trap_target'}{'value'} = $old_tags{'snmp_secret_hash_parameter_trapTargets.0.address'}{'value'};
        }
        if (exists $template_tags{'vip_sig_bsf_ipv4'} && ! exists $old_tags{'vip_sig_bsf_ipv4'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_BSF'}) {
            $old_tags{'vip_sig_bsf_ipv4'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_BSF'}{'value'};
            $old_tags{'vip_sig_bsf_ipv4'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig_bsf_ipv6'} && ! exists $old_tags{'vip_sig_bsf_ipv6'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_BSF'}) {
            $old_tags{'vip_sig_bsf_ipv6'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_BSF'}{'value_IntIPv6ExtIPv6'};
            $old_tags{'vip_sig_bsf_ipv6'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig_diameter_ipv4'} && ! exists $old_tags{'vip_sig_diameter_ipv4'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}) {
            $old_tags{'vip_sig_diameter_ipv4'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}{'value'};
            $old_tags{'vip_sig_diameter_ipv4'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig_diameter_ipv6'} && ! exists $old_tags{'vip_sig_diameter_ipv6'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}) {
            $old_tags{'vip_sig_diameter_ipv6'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}{'value_IntIPv6ExtIPv6'};
            $old_tags{'vip_sig_diameter_ipv6'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig_scp_ipv4'} && ! exists $old_tags{'vip_sig_scp_ipv4'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_SCP'}) {
            $old_tags{'vip_sig_scp_ipv4'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_SCP'}{'value'};
            $old_tags{'vip_sig_scp_ipv4'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig_scp_ipv6'} && ! exists $old_tags{'vip_sig_scp_ipv6'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_SCP'}) {
            $old_tags{'vip_sig_scp_ipv6'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_SCP'}{'value_IntIPv6ExtIPv6'};
            $old_tags{'vip_sig_scp_ipv6'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig2_scp_ipv4'} && ! exists $old_tags{'vip_sig2_scp_ipv4'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG2_SCP'}) {
            $old_tags{'vip_sig2_scp_ipv4'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG2_SCP'}{'value'};
            $old_tags{'vip_sig2_scp_ipv4'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig2_scp_ipv6'} && ! exists $old_tags{'vip_sig2_scp_ipv6'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG2_SCP'}) {
            $old_tags{'vip_sig2_scp_ipv6'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG2_SCP'}{'value_IntIPv6ExtIPv6'};
            $old_tags{'vip_sig2_scp_ipv6'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig_sepp_ipv4'} && ! exists $old_tags{'vip_sig_sepp_ipv4'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_SEPP'}) {
            $old_tags{'vip_sig_sepp_ipv4'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_SEPP'}{'value'};
            $old_tags{'vip_sig_sepp_ipv4'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig_sepp_ipv6'} && ! exists $old_tags{'vip_sig_sepp_ipv6'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_SEPP'}) {
            $old_tags{'vip_sig_sepp_ipv6'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG_SEPP'}{'value_IntIPv6ExtIPv6'};
            $old_tags{'vip_sig_sepp_ipv6'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig2_sepp_ipv4'} && ! exists $old_tags{'vip_sig2_sepp_ipv4'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG2_SEPP'}) {
            $old_tags{'vip_sig2_sepp_ipv4'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG2_SEPP'}{'value'};
            $old_tags{'vip_sig2_sepp_ipv4'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'vip_sig2_sepp_ipv6'} && ! exists $old_tags{'vip_sig2_sepp_ipv6'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG2_SEPP'}) {
            $old_tags{'vip_sig2_sepp_ipv6'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_SIG2_SEPP'}{'value_IntIPv6ExtIPv6'};
            $old_tags{'vip_sig2_sepp_ipv6'}{'value'} =~ s/'//g;
        }

        if (exists $template_tags{'dsc_oam_vip'} && ! exists $old_tags{'dsc_oam_vip'} && exists $old_tags{'eric_sc_values_anchor_parameter_VIP_OAM'}) {
            $old_tags{'dsc_oam_vip'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_VIP_OAM'}{'value'};
            $old_tags{'dsc_oam_vip'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'dsc_snmp_trap_target'} && ! exists $old_tags{'dsc_snmp_trap_target'} && exists $old_tags{'snmp_secret_hash_parameter_trapTargets.0.address'}) {
            $old_tags{'dsc_snmp_trap_target'}{'value'} = $old_tags{'snmp_secret_hash_parameter_trapTargets.0.address'}{'value_DSC'};
        }
        if (exists $template_tags{'dsc_vip_sig_diameter'} && ! exists $old_tags{'dsc_vip_sig_diameter'} && exists $old_tags{'eric_sc_values_hash_parameter_eric-dsc-fdr.service.diameter.loadBalancerIP'}) {
            $old_tags{'dsc_vip_sig_diameter'}{'value'} = $old_tags{'eric_sc_values_hash_parameter_eric-dsc-fdr.service.diameter.loadBalancerIP'}{'value'};
            $old_tags{'dsc_vip_sig_diameter'}{'value'} =~ s/'//g;
        }
        if (exists $template_tags{'dsc_vip_sig_sctp'} && ! exists $old_tags{'dsc_vip_sig_sctp'} && exists $old_tags{'eric_sc_values_hash_parameter_eric-dsc-fdr.service.sctp.loadBalancerIP'}) {
            $old_tags{'dsc_vip_sig_sctp'}{'value'} = $old_tags{'eric_sc_values_hash_parameter_eric-dsc-fdr.service.sctp.loadBalancerIP'}{'value'};
            $old_tags{'dsc_vip_sig_sctp'}{'value'} =~ s/'//g;
        }

        for my $key (keys %template_tags) {
            for my $key2 (keys %{$template_tags{$key}}) {
                if ($template_tags{$key}{$key2} =~ /REPLACEMEWITH/) {
                    $old_tags{$key}{$key2} = $template_tags{$key}{$key2};
                }
            }
        }
    # END of 'if ($execute_temporary_fix == 1) {'

    } elsif ($execute_temporary_fix == 2) {
        # Added this when adding the new <tools> section to populate the new parameters from other parameters.
        if (exists $template_tags{'tools_namespace'} && ! exists $old_tags{'tools_namespace'} && exists $old_tags{'sc_namespace'}) {
            $old_tags{'tools_namespace'}{'value'} = "$old_tags{'sc_namespace'}{'value'}-tools";
        }
        if (exists $template_tags{'tools_pull_secret'} && ! exists $old_tags{'tools_pull_secret'} && exists $old_tags{'eric_sc_values_hash_parameter_global.pullSecret'}) {
            $old_tags{'tools_pull_secret'}{'value'} = $old_tags{'eric_sc_values_hash_parameter_global.pullSecret'}{'value'};
            $old_tags{'tools_pull_secret'}{'value_EVNFM'} = $old_tags{'eric_sc_values_hash_parameter_global.pullSecret'}{'value_EVNFM'};
        }
        if (exists $template_tags{'tools_registry_password'} && ! exists $old_tags{'tools_registry_password'} && exists $old_tags{'private_registry_password'}) {
            $old_tags{'tools_registry_password'}{'value'} = $old_tags{'private_registry_password'}{'value'};
            $old_tags{'tools_registry_password'}{'value_EVNFM'} = $old_tags{'private_registry_password'}{'value_EVNFM'};
        }
        if (exists $template_tags{'tools_registry_port'} && ! exists $old_tags{'tools_registry_port'} && exists $old_tags{'private_registry_port'}) {
            $old_tags{'tools_registry_port'}{'value'} = $old_tags{'private_registry_port'}{'value'};
            $old_tags{'tools_registry_port'}{'value_EVNFM'} = $old_tags{'private_registry_port'}{'value_EVNFM'};
        }
        if (exists $template_tags{'tools_registry_url'} && ! exists $old_tags{'tools_registry_url'} && exists $old_tags{'eric_sc_values_hash_parameter_global.registry.url'}) {
            $old_tags{'tools_registry_url'}{'value'} = $old_tags{'eric_sc_values_hash_parameter_global.registry.url'}{'value'};
            $old_tags{'tools_registry_url'}{'value_EVNFM'} = $old_tags{'eric_sc_values_hash_parameter_global.registry.url'}{'value_EVNFM'};
        }
        if (exists $template_tags{'tools_registry_user'} && ! exists $old_tags{'tools_registry_user'} && exists $old_tags{'private_registry_user'}) {
            $old_tags{'tools_registry_user'}{'value'} = $old_tags{'private_registry_user'}{'value'};
            $old_tags{'tools_registry_user'}{'value_EVNFM'} = $old_tags{'private_registry_user'}{'value_EVNFM'};
        }
        if (exists $template_tags{'tools_storage_class_name'} && ! exists $old_tags{'tools_storage_class_name'} && exists $old_tags{'eric_sc_values_anchor_parameter_oam_storage_class'}) {
            $old_tags{'tools_storage_class_name'}{'value'} = $old_tags{'eric_sc_values_anchor_parameter_oam_storage_class'}{'value'};
        }
    # END of 'if ($execute_temporary_fix == 2) {'

    } elsif ($execute_temporary_fix == 3) {
        # Added this fix for DND-50185 (Investigate Refactor Stability) to copy the values from the old variables into the new variables
        # at the beginning and then overwrite all variables where the template file contains REPLACEMEWITH_xxxx values.
        if (exists $template_tags{'cluster_ssh_vip_tools'} && ! exists $old_tags{'cluster_ssh_vip_tools'} && exists $old_tags{'cluster_ssh_vip'}) {
            $old_tags{'cluster_ssh_vip_tools'}{'value'} = $old_tags{'cluster_ssh_vip'}{'value'};
        }
        if (! exists $template_tags{'dsc_cluster_ssh_vip'} && exists $old_tags{'dsc_cluster_ssh_vip'}) {
            delete $old_tags{'dsc_cluster_ssh_vip'}{'value'};
            delete $old_tags{'dsc_cluster_ssh_vip'};
            delete $replacemewith{'dsc_cluster_ssh_vip'};
        }
        if (! exists $template_tags{'dsc_worker_ip'} && exists $old_tags{'dsc_worker_ip'}) {
            delete $old_tags{'dsc_worker_ip'}{'value'};
            delete $old_tags{'dsc_worker_ip'};
            delete $replacemewith{'dsc_worker_ip'};
        }
        if (exists $template_tags{'worker_ip_ds_tools'} && ! exists $old_tags{'worker_ip_ds_tools'} && exists $old_tags{'worker_ip_ds'}) {
            $old_tags{'worker_ip_ds_tools'}{'value'} = $old_tags{'worker_ip_ds'}{'value'};
        }
        if (exists $template_tags{'worker_ip_ipv4_tools'} && ! exists $old_tags{'worker_ip_ipv4_tools'} && exists $old_tags{'worker_ip_ipv4'}) {
            $old_tags{'worker_ip_ipv4_tools'}{'value'} = $old_tags{'worker_ip_ipv4'}{'value'};
        }
        if (exists $template_tags{'worker_ip_ipv6_tools'} && ! exists $old_tags{'worker_ip_ipv6_tools'} && exists $old_tags{'worker_ip_ipv6'}) {
            $old_tags{'worker_ip_ipv6_tools'}{'value'} = $old_tags{'worker_ip_ipv6'}{'value'};
        }
    # END of 'if ($execute_temporary_fix == 3) {'

    } elsif ($execute_temporary_fix == 4) {
        # Added this to move parameter values when introducing SC into 5G where we removed some parameter values and moved some to other places in the network config file.
        if (exists $template_tags{'oam_vip_ipv4'} && exists $template_tags{'oam_vip_ipv4'}{'value_DSC'} && exists $old_tags{'dsc_oam_vip'}) {
            $old_tags{'oam_vip_ipv4'}{'value_DSC'} = $old_tags{'dsc_oam_vip'}{'value'};
            if ($old_tags{'dsc_oam_vip'}{'value'} =~ /:/) {
                # It contains an IPv6 address so also store it inside another parameter
                $old_tags{'oam_vip_ipv6'}{'value_DSC'} = $old_tags{'dsc_oam_vip'}{'value'};
            }
            delete $old_tags{'dsc_oam_vip'};
            for my $temp_tag (keys %old_tags) {
                if ($old_tags{$temp_tag}{'value'} eq "REPLACEMEWITH_dsc_oam_vip") {
                    $old_tags{$temp_tag}{'value'} = "REPLACEMEWITH_oam_vip_ipv4";
                    $old_tags{$temp_tag}{'value_IntIPv4ExtIPv4'} = "'REPLACEMEWITH_oam_vip_ipv4'";
                    $old_tags{$temp_tag}{'value_IntIPv6ExtIPv6'} = "'REPLACEMEWITH_oam_vip_ipv6'";
                    $old_tags{$temp_tag}{'value_IntIPv6ExtIPv6forSignalingExtIPv4forOamExtIPv6forBsfHttpExtIPv4forDiameter'} = "'REPLACEMEWITH_oam_vip_ipv6'";
                }
            }
        }
        if (exists $template_tags{'snmp_trap_target'} && exists $template_tags{'snmp_trap_target'}{'value_DSC'} && exists $old_tags{'dsc_snmp_trap_target'}) {
            $old_tags{'snmp_trap_target'}{'value_DSC'} = $old_tags{'dsc_snmp_trap_target'}{'value'};
            delete $old_tags{'dsc_snmp_trap_target'};
            $old_tags{'snmp_secret_hash_parameter_trapTargets.0.address'}{'value'} = "REPLACEMEWITH_snmp_trap_target";
            delete $old_tags{'snmp_secret_hash_parameter_trapTargets.0.address'}{'value_DSC'};
        }
        if (exists $template_tags{'vip_sig_diameter_ipv4'} && exists $template_tags{'vip_sig_diameter_ipv4'}{'value_DSC'} && exists $old_tags{'dsc_vip_sig_diameter'}) {
            $old_tags{'vip_sig_diameter_ipv4'}{'value_DSC'} = $old_tags{'dsc_vip_sig_diameter'}{'value'};
            if ($old_tags{'dsc_vip_sig_diameter'}{'value'} =~ /:/) {
                # It contains an IPv6 address so also store it inside another parameter
                $old_tags{'vip_sig_diameter_ipv6'}{'value_DSC'} = $old_tags{'dsc_vip_sig_diameter'}{'value'};
            }
            delete $old_tags{'dsc_vip_sig_diameter'};
            $old_tags{'eric_sc_values_hash_parameter_eric-dsc-fdr.service.diameter.loadBalancerIP'}{'value'} = "REPLACEMEWITH_vip_sig_diameter_ipv4";
        }
        if (exists $template_tags{'vip_sig_sctp'} && exists $template_tags{'vip_sig_sctp'}{'value_DSC'} && exists $old_tags{'dsc_vip_sig_sctp'}) {
            # New parameter
            $old_tags{'vip_sig_sctp'}{'value'} = $old_tags{'dsc_vip_sig_sctp'}{'value'};
            $old_tags{'vip_sig_sctp'}{'value_DSC'} = $old_tags{'dsc_vip_sig_sctp'}{'value'};
            delete $old_tags{'dsc_vip_sig_sctp'};
            $old_tags{'eric_sc_values_hash_parameter_eric-dsc-fdr.service.sctp.loadBalancerIP'}{'value'} = "REPLACEMEWITH_vip_sig_sctp";
        }
        delete $old_tags{'eric_sc_values_hash_parameter_eric-dsc.routes.nbi.fqdn'};
    # END of 'if ($execute_temporary_fix == 4) {'

    } elsif ($execute_temporary_fix == 5) {
        # Added this to move parameter values when introducing SC into 5G where we removed some parameter values and moved some to other places in the network config file.
        if (exists $template_tags{'snmp_trap_target_secret_name'} && exists $old_tags{'eric_sc_values_hash_parameter_eric-fh-snmp-alarm-provider.service.secretName'}) {
            $old_tags{'eric_sc_values_hash_parameter_eric-fh-snmp-alarm-provider.service.secretName'}{'value'} = "REPLACEMEWITH_snmp_trap_target_secret_name";
            delete $old_tags{'eric_sc_values_hash_parameter_eric-fh-snmp-alarm-provider.service.secretName'}{'value_DSC'};
        }
    # END of 'if ($execute_temporary_fix == 5) {'

    } elsif ($execute_temporary_fix == 6) {
        # Added this to move parameter values when introducing SC into 5G where we removed some parameter values and moved some to other places in the network config file.
        if (exists $template_tags{'vip_sig_diameter_dsc'} && ! exists $old_tags{'vip_sig_diameter_dsc'} && exists $old_tags{'vip_sig_diameter_ipv4'}{'value_DSC'}) {
            $old_tags{'vip_sig_diameter_dsc'}{'value'} = $old_tags{'vip_sig_diameter_ipv4'}{'value_DSC'};
            $old_tags{'eric_sc_values_hash_parameter_eric-dsc-fdr.service.diameter.loadBalancerIP'}{'value'} = "REPLACEMEWITH_vip_sig_diameter_dsc";
            delete $old_tags{'vip_sig_diameter_ds'}{'value_DSC'};
            delete $old_tags{'vip_sig_diameter_ipv4'}{'value_DSC'};
            delete $old_tags{'vip_sig_diameter_ipv6'}{'value_DSC'};
        }
    # END of 'if ($execute_temporary_fix == 6) {'

    } elsif ($execute_temporary_fix == 7) {
        # Added this to fix wrong passwords on some of the default_users_xx variable, so we just delete all the old values and just use the values from the template file instead.
        for my $key (keys %template_tags) {
            next unless $key =~ /^default_user_\d+$/;
            delete $old_tags{$key};
        }
    # END of 'if ($execute_temporary_fix == 7) {'

    } elsif ($execute_temporary_fix == 8) {
        # Add values to some new UCC related parameters.
        print STDERR "DBG: oam_vip_ds=$old_tags{'oam_vip_ds'}{'value'}\n";
        $old_tags{'oam_vip_ds'}{'value_UCC1'} = $old_tags{'oam_vip_ds'}{'value'};
        $old_tags{'oam_vip_ipv4'}{'value_UCC1'} = $old_tags{'oam_vip_ipv4'}{'value'};
        $old_tags{'oam_vip_ipv6'}{'value_UCC1'} = $old_tags{'oam_vip_ipv6'}{'value'};
        $old_tags{'vip_sig_sepp_ds'}{'value_UCC1'} = $old_tags{'vip_sig_sepp_ds'}{'value'};
        $old_tags{'vip_sig_sepp_ipv4'}{'value_UCC1'} = $old_tags{'vip_sig_sepp_ipv4'}{'value'};
        $old_tags{'vip_sig_sepp_ipv6'}{'value_UCC1'} = $old_tags{'vip_sig_sepp_ipv6'}{'value'};
        $old_tags{'vip_sig2_sepp_ds'}{'value_UCC1'} = $old_tags{'vip_sig2_sepp_ds'}{'value'};
        $old_tags{'vip_sig2_sepp_ipv4'}{'value_UCC1'} = $old_tags{'vip_sig2_sepp_ipv4'}{'value'};
        $old_tags{'vip_sig2_sepp_ipv6'}{'value_UCC1'} = $old_tags{'vip_sig2_sepp_ipv6'}{'value'};
        $old_tags{'sc_namespace'}{'value_UCC1'} = "$old_tags{'sc_namespace'}{'value'}-ucc1";
        $old_tags{'sc_namespace'}{'value_UCC2'} = "$old_tags{'sc_namespace'}{'value'}-ucc2";
    # END of 'if ($execute_temporary_fix == 8) {'

    } elsif ($execute_temporary_fix == 9) {
        # Added this because they changed again the name eric-sc-diameter to eric-dsc
        if (exists $template_tags{'eric_sc_values_hash_parameter_eric-dsc.routes.nbi.fqdn'} && exists $old_tags{'eric_sc_values_hash_parameter_eric-sc.routes.nbi.fqdn'}) {
            $old_tags{'eric_sc_values_hash_parameter_eric-dsc.routes.nbi.fqdn'}{'value'} = $old_tags{'eric_sc_values_hash_parameter_eric-sc.routes.nbi.fqdn'}{'value'};
            $old_tags{'eric_sc_values_hash_parameter_eric-sc-diameter.routes.nbi.fqdn'}{'value'} = $old_tags{'eric_sc_values_hash_parameter_eric-sc.routes.nbi.fqdn'}{'value'};
        }
    # END of 'if ($execute_temporary_fix == 9) {'

    } elsif ($execute_temporary_fix == 10) {
        if (exists $template_tags{'vip_sig_bsf_diameter_ds'} && exists $old_tags{'vip_sig_diameter_ds'} && ! exists $old_tags{'vip_sig_bsf_diameter_ds'}) {
            $old_tags{'vip_sig_bsf_diameter_ds'}{'value'} = $old_tags{'vip_sig_diameter_ds'}{'value'};
            delete $old_tags{'vip_sig_diameter_ds'};
        }
        if (exists $template_tags{'vip_sig_bsf_diameter_ipv4'} && exists $old_tags{'vip_sig_diameter_ipv4'} && ! exists $old_tags{'vip_sig_bsf_diameter_ipv4'}) {
            $old_tags{'vip_sig_bsf_diameter_ipv4'}{'value'} = $old_tags{'vip_sig_diameter_ipv4'}{'value'};
            delete $old_tags{'vip_sig_diameter_ipv4'};
        }
        if (exists $template_tags{'vip_sig_bsf_diameter_ipv6'} && exists $old_tags{'vip_sig_diameter_ipv6'} && ! exists $old_tags{'vip_sig_bsf_diameter_ipv6'}) {
            $old_tags{'vip_sig_diameter_ipv6'}{'value'} = $old_tags{'vip_sig_diameter_ipv6'}{'value'};
            delete $old_tags{'vip_sig_diameter_ipv6'};
        }
        if (exists $template_tags{'vip_sig_dsc_diameter'} && exists $old_tags{'vip_sig_diameter_dsc'} && ! exists $old_tags{'vip_sig_dsc_diameter'}) {
            $old_tags{'vip_sig_dsc_diameter'}{'value'} = $old_tags{'vip_sig_diameter_dsc'}{'value'};
            delete $old_tags{'vip_sig_diameter_dsc'};
        }
    # END of 'if ($execute_temporary_fix == 10) {'

    }

    # Check if any old tags is missing in the new template file
    for my $key (sort keys %old_tags) {
        if (exists $template_tags{$key}) {
            # Check if each parameter exists in the template file
            my $message = "";
            for my $param (sort keys %{$old_tags{$key}}) {
                unless (exists $template_tags{$key}{$param}) {
                    $message .= "  $param=\"$old_tags{$key}{$param}\"\n";
                }
            }
            push @missing_in_template, "name=\"$key\"\n$message" if ($message ne "");
        } else {
            my $message = "";
            for my $param (sort keys %{$old_tags{$key}}) {
                if ($keep_missing_parameters == 1) {
                    $message .= "$param=\"$old_tags{$key}{$param}\" ";
                } else {
                    $message .= "  $param=\"$old_tags{$key}{$param}\"\n";
                }
            }
            if ($keep_missing_parameters == 1) {
                # The missing parameter will be added to the end of file
                $message = "        <parameter name=\"$key\" $message/>";
                # Check if we want to have a different output format instead of having all parameters
                # on the same line.
                if ($output_format ne "one-line") {
                    # Modify the $line to split it into multiple lines
                    $message = split_line($message);
                }
                push @missing_in_template_but_added_back, "$message";
            } else {
                # The missing parameter is marked as deleted from the new file
                push @missing_in_template, "name=\"$key\"\n$message";
            }
        }
    }

    # Check for missing REPLACEMEWITH_xxx values
    for my $key (sort keys %replacemewith) {
        my $message = "";
        unless (exists $template_tags{$key}) {
            $message .= "  Missing definition in template file\n"
        }
        unless (exists $old_tags{$key}) {
            $message .= "  Missing definition in old file\n"
        }
        push @missing_replacemewith, "name=\"$key\"\n$message" if ($message ne "");
    }

    change_new_data(\@new_data, \@template_data, \%old_tags, \%template_tags);
} elsif (@parameters) {
    # Some specific parameters should have their values set
    for (@parameters) {
        if (/^(\S+)\.(\S+?)=(.+)/) {
            $old_tags{$1}{$2} = $3;
        } elsif (/^(\S+?)=(.+)/) {
            $old_tags{$1}{'value'} = $2;
        } else {
            print "Wrong parameter format ($_), should be <name>=<value>\n";
        }
    }

    change_new_data(\@new_data, \@template_data, \%old_tags, undef);
} else {
    # No old data to change, just use the template data as output data
    change_new_data(\@new_data, \@template_data, \%template_tags, \%template_tags);
}

# Open output file
if ($output_file) {
    $filename = $output_file;
    unless (open OUTF, ">$filename") {
        print "Unable to open output file '$filename' for writing\n";
        exit 1;
    }
}

# Output data
for (@new_data) {
    if (/^\s*$/) {
        next if $strip_empty_lines;
    }
    if ($output_file) {
        print OUTF "$_\n";
    } else {
        print "$_\n";
    }
}

# Close output file
if ($output_file) {
    close OUTF;

    print "\nOutput written to file $output_file\n";
}

if ($status_file ne "" && (@duplicate_parameters || @missing_in_template || @missing_in_template_but_added_back || @missing_replacemewith || @updated_values)) {
    unless (open STATUSF, ">>$status_file") {
        print "Unable to open status file '$status_file' for writing\n";
        exit 1;
    }

    print STATUSF "\n" . "="x80 . "\n";
    print STATUSF "Old File: $old_file\n" if $old_file;
    print STATUSF "="x80 . "\n" if $old_file;

    if (@duplicate_parameters) {
        print STATUSF "\nThe following parameters are included more than once in the data and will result in unpredictable output in the new file:\n";
        print STATUSF "-------------------------------------------------------------------------------------------------------------------------\n";
        for (@duplicate_parameters) {
            print STATUSF "$_\n";
        }
    }

    if (@missing_in_template) {
        print STATUSF "\nThe following parameters was included in the old file but are missing in the template file and thus these parameters have been deleted in the new file:\n";
        print STATUSF "-------------------------------------------------------------------------------------------------------------------------------------------------------\n";
        for (@missing_in_template) {
            print STATUSF "$_\n";
        }
    }

    if (@missing_in_template_but_added_back) {
        print STATUSF "\nThe following parameters was included in the old file but are missing in the template file and added back at the end of the new file:\n";
        print STATUSF "-------------------------------------------------------------------------------------------------------------------------------------\n";
        for (@missing_in_template_but_added_back) {
            print STATUSF "$_\n";
        }
    }

    if (@missing_replacemewith) {
        print STATUSF "\nThe following parameters are missing definitions because they are refered to by REPLACEMEWITH_xxxx definitions:\n";
        print STATUSF "-----------------------------------------------------------------------------------------------------------------\n";
        for (@missing_replacemewith) {
            print STATUSF "$_\n";
        }
    }

    if (@updated_values) {
        print STATUSF "\nThe following parameters was updated where the value was taken from the template file instead of the old file:\n";
        print STATUSF "--------------------------------------------------------------------------------------------------------------\n";
        for (@updated_values) {
            print STATUSF "$_\n";
        }
    }

    close STATUSF;
}

# Exit with success
exit 0;

#
# Subroutines
# -----------
#

#
# Return an array filled with template data and values from old network configuration file.
#
# Input:
#  - Reference to array where data is saved
#  - Reference to template array where data is coming from
#  - Reference to hash where old values are taken from
#  - Reference to hash where template values are taken from
#
# Output:
#  - The array is updated with all changed data.
#
sub change_new_data {
    my $new_array_ref = shift;
    my $template_array_ref = shift;
    my $old_hash_ref = shift;
    my $template_hash_ref = shift;

    my $inside_comment = 0;
    my $line;
    my $old_line;
    my $pending_line = "";
    my $old_value = "";

    # Clear new array
    @$new_array_ref = ();

    for (@$template_array_ref) {
        s/[\r\n]//g;
        $line = $_;
        # Find comments and empty lines and just put them directly to the output data and
        # for all tag lines combine multi-line tags into one line to make it easier to replace
        # the text.
        if ($inside_comment) {
            if ($line =~ /^.*?-->.*/) {
                # End of multi line comment
                $inside_comment = 0;
                push @$new_array_ref, $line;
                next;
            } else {
                # Still inside a multi line comment.
                push @$new_array_ref, $line;
                next;
            }
        } elsif ($line =~ /^.*?<!--.+?-->.*/) {
            # One line comment
            push @$new_array_ref, $line;
            next;
        } elsif ($line =~ /^.*?<!--.*/) {
            # Start of a multi line comment
            $inside_comment = 1;
            push @$new_array_ref, $line;
            next;
        # Find blank lines and just put them directly to the output data.
        } elsif ($line =~ /^\s*$/) {
            push @$new_array_ref, $line;
            next;
        # Find complete expected tag line
        } elsif ($line =~ /^\s*<(access|parameter)\s+.+\/>\s*$/) {
            # Exit out of "if" statement to process the line
        # Find start of expected tag line
        } elsif ($line =~ /^\s*<(access|parameter)\s*$/) {
            if ($output_format ne "") {
                # We want to reformat the output so make sure all parameters are on one line
                $pending_line = "$line";
            } else {
                # Keep the original format but make the line into a multi-line string
                $pending_line = "$line\n";
            }
            next;
        # Find start of expected tag line
        } elsif ($line =~ /^\s*<(access|parameter)\s+/) {
            if ($output_format ne "") {
                # We want to reformat the output so maybe sure all parameters are on one line
                $pending_line = "$line";
            } else {
                # Keep the original format but make the line into a multi-line string
                $pending_line = "$line\n";
            }
            next;
        # Find end of expected tag line
        } elsif ($pending_line ne "") {
            if ($output_format ne "") {
                # We want to reformat the output so maybe sure all parameters are on one line
                if ($line =~ /^\s*(.*\/>.*)/) {
                    $line = "$pending_line"." $1";
                    $pending_line = "";
                    # Exit out of "if" statement to process the line
                } elsif ($line =~ /^\s+(.+)/) {
                    $pending_line .= " $1";
                    next;
                } else {
                    $pending_line .= "$line";
                    next;
                }
            } else {
                # Keep the original format but make the line into a multi-line string
                if ($line =~ /^.*\/>.*/) {
                    $line = "$pending_line"."$line";
                    $pending_line = "";
                    # Exit out of "if" statement to process the line
                } else {
                    $pending_line .= "$line\n";
                    next;
                }
            }
        } else {
            # Some other line we don't care about
            if ($line =~ /^\s*<\/document>\s*$/ && @missing_in_template_but_added_back) {
                # We need to add back missing parameters to the end of the file before the </document> tag
                push @$new_array_ref, "    <temporary_parameters>";
                push @$new_array_ref, "" unless $strip_empty_lines;
                unless ($strip_comments) {
                    push @$new_array_ref, "        <!--";
                    push @$new_array_ref, "            *******************************************************************************************************************************************************************";
                    push @$new_array_ref, "            * Temporary parameters that does not exist in the Template file and should at some point be added to the template or removed from this file when no longer needed *";
                    push @$new_array_ref, "            *******************************************************************************************************************************************************************";
                    push @$new_array_ref, "        -->";
                }
                push @$new_array_ref, "" unless $strip_empty_lines;
                for (@missing_in_template_but_added_back) {
                    push @$new_array_ref, $_;
                }
                push @$new_array_ref, "" unless $strip_empty_lines;
                push @$new_array_ref, "    </temporary_parameters>";
            }

            push @$new_array_ref, $line;
            next;
        }

        # When we reach here we have found a tag line which has been combined into
        # a single line with all attributes on the same line.
        # Find all old tags names that are still the same in the new template and
        # update them with the old value.
        $old_line = $line;
        for my $key (sort keys %$old_hash_ref) {
            if ($line =~ /.+name="$key"/m) {
                # We have found what we are looking for, now change parameter values

                my $message = "";
                for my $key2 (sort keys %{$old_hash_ref->{$key}}) {
                    $old_value = $old_hash_ref->{$key}{$key2};
                    print STDERR "\nkey=$key, key2=$key2, old_value=$old_value\n" if ($debug > 2);
                    if ($old_value ne "") {
                        if ($only_changeme) {
                            if ($line =~ / $key2="CHANGEME"/) {
                                $line =~ s/ $key2=".*?"/ $key2="$old_value"/m;
                            }
                        } else {
                            if ($key2 =~ /^(application_type|description|playlist|valid_from_releases|valid_releases)$/ && exists $template_hash_ref->{$key}{$key2} && $template_hash_ref->{$key}{$key2} ne $old_value) {
                                # Special handling for the "application_type", "description", "playlist", "valid_from_releases' and "valid_releases" tags.
                                # We ALWAYS take the value from the template file since it might have been updated
                                # to allow new releases or have new information.
                                $line =~ s/ $key2=".*?"/ $key2="$template_hash_ref->{$key}{$key2}"/m;
                                my $new_text = "  $key2=\"$template_hash_ref->{$key}{$key2}\"";
                                $message .= sprintf "$new_text%s (old value \"$old_value\")\n", length($new_text) > 70 ? "  " : " " x (70-length($new_text));
                            } else {
                                # We take the old value and copy it into the new value.
                                $line =~ s/ $key2=".*?"/ $key2="$old_value"/m;
                            }
                        }
                    }
                }
                push @updated_values, "name=\"$key\"\n$message" if ($message ne "");

                print STDERR "\nChanged name '$key'\nFrom:\n$old_line\nTo:\n$line\n" if ($debug > 0);
                # Now exit and take next line
                last;
            } elsif ($key =~ /^(.+_(anchor|hash)_parameter_)(\d{2})$/) {
                # Old formatted parameters where the name end with 2 digits
                # and the value contains a parameter part followed by = and
                # then a second value.
                my $param_part_1 = $1;
                my $param_part_2 = $3;
                if (exists $old_hash_ref->{$key}{'value'} && $old_hash_ref->{$key}{'value'} =~ /^(.+?)=(.+)$/) {
                    my $value_part_1 = $1;
                    my $value_part_2 = $2;
                    if ($line =~ /.+name="$param_part_1$value_part_1"/m) {
                        # We have found a parameter that have changed naming format from having 2-digits
                        # at the end of the name to having the first part of the value at the end.
                        if ($line =~ / value="(CHANGEME|'CHANGEME')"/) {
                            print STDERR "\nReplacing CHANGEME value with value from old key=$key, new key=$param_part_1$value_part_1, new value=$value_part_2\n" if ($debug > 2);
                            $line =~ s/ value=".*?"/ value="$value_part_2"/m;
                        }
                    }
                }
            }
        }

        # Check if we want to have a different output format instead of having all parameters
        # on the same line.
        if ($output_format eq "multiple-lines" && $line =~ /^\s*<(access|parameter).*/) {
            # Modify the $line to split it into multiple lines
            $line = split_line($line);
        }

        push @$new_array_ref, $line;
    }
}

#
# Extract each parameter and store in hash reference.
#
# Input:
#  - Input data containing one or more parameters
#  - Reference to output hash
#
# Output:
#  - The output hash is updated to have the each parameter as the key.
#
sub extract_all_parameters {
    my $line = shift;
    my $hash_ref  = shift;

    my $key;
    my $param;
    my $value;

    # Strip leading and trailing spaces
    $line =~ s/^\s*//;
    $line =~ s/\s*$//;

    while ($line =~ /^\s*(\w+)="(.*?)"\s*/) {
        $param = $1;
        $value = $2;
        #$line =~ s/^$1//;
        $line =~ s/^\s*\w+=".*?"\s*//;
        print STDERR "  Parameter=\"$param\"\n  Value=\"$value\"\n\n" if ($debug > 2);
        $hash_ref->{$param} = $value;
    }
}

#
# Extract each parameter and store in hash reference where the first key
# is the value of the "name" parameter and the sub-keys are all the other
# parameters.
#
# Input:
#  - Input data containing one or more parameters
#  - Reference to output hash
#
# Output:
#  - The output hash is updated to have the "name" as the main key.
#
sub extract_parameters {
    my $line = shift;
    my $hash_ref  = shift;

    my $key;
    my $param;
    my $value;

    # Strip leading and trailing spaces
    $line =~ s/^\s*//;
    $line =~ s/\s*$//;

    if ($line =~ /\s*name="(.+?)"\s*/) {
        $key = $1;
        if (exists $hash_ref->{$key}) {
            push @duplicate_parameters, "name=\"$key\"\n  $line\n";
        }
        $line =~ s/\s*name="$key"\s*/ /;
    }
    print STDERR "Key=$key\n" if ($debug > 2);
    while ($line =~ /^\s*(\w+)="(.*?)"\s*/) {
        $param = $1;
        $value = $2;
        #$line =~ s/^$1//;
        $line =~ s/^\s*\w+=".*?"\s*//;
        $hash_ref->{$key}{$param} = $value;
        if ($value =~ /^REPLACEMEWITH_(\S+)/) {
            $replacemewith{$1}++;
        }
    }
}

#
# Put one XML tag per line.
#
# Input:
#  - Reference to input array
#  - Reference to output array
#
# Output:
#  - The output array is updated to have one parameter per line.
#
sub one_tag_per_line {
    my $array_ref_in  = shift;
    my $array_ref_out = shift;

    my $inside_comment = 0;
    my $pending_line = "";

    # Empty the output array
    @$array_ref_out = ();

    for (@$array_ref_in) {
        # Strip trailing spaces
        s/\s*$//;
        # Take next line if empty
        next if (/^\s*$/);

        if ($inside_comment) {
            $pending_line .= "$_\\n";
            if (/-->/) {
                push @$array_ref_out, "$pending_line";
                $pending_line = "";
                $inside_comment = 0;
            }
        } elsif (/^.*?<.+>$/) {
            # The parameter is already on one line
            push @$array_ref_out, "$_";
            $pending_line = "";
        } elsif (/^.*?<!--.*/) {
            $inside_comment = 1;
            $pending_line .= "$_\\n";
        } elsif (/^.*?<.+/) {
            $pending_line .= "$_ ";
        } elsif ($pending_line ne "") {
            if (/^.*>.*/) {
                # Found the end tag "/>"

                # Remove preceeding spaces
                s/^\s*//;

                $pending_line .= "$_";
                push @$array_ref_out, "$pending_line";
                $pending_line = "";
            } else {
                # Still waiting for end tag "/>"

                # Remove leading spaces
                s/^\s*//;

                $pending_line .= "$_ ";
            }
        } else {
            # Any normal line
            push @$array_ref_out, "$_";
            $pending_line = "";
        }
    }
}

#
# Parse data and store in a hash.
#
# Input:
#  - Reference to array used as input.
#  - Reference to hash where output is stored.
#  - If comments should be stripped away (=1) or not (=0).
#
# Output:
#  - The hash is updated with all found data.
#
sub parse_tag_data {
    my $array_ref = shift;
    my $hash_ref = shift;
    my $strip_comments = shift;

    my @input = ();
    my @no_comments = ();

    # Strip away all comments to make the file easier to read
    strip_comment_lines($array_ref, \@no_comments, $strip_comments);

    # Put one XML tag per line
    one_tag_per_line(\@no_comments, \@input);

    for (@input) {
        # Ignore empty lines
        next if (/^\s*$/);

        if (/^\s*<(access|parameter)\s+(.+)\/>\s*$/) {
            # Extract parameters
            print STDERR "    Wanted: $_\n" if ($debug > 1);
            extract_parameters($2, $hash_ref);
        } else {
            # Not wanted parameters
            print STDERR "Not wanted: $_\n" if ($debug > 1);
            next;
        }
    }
}

#
# Read the input file looking for comments and return remaining data.
#
# Input:
#  - File name
#  - Reference to array where data is saved
#  - If comments should be stripped away (=1) or not (=0).
#
# Output:
#  - The array is updated with all found data.
#
sub read_file {
    my $filename = shift;
    my $array_ref = shift;
    my $strip_comments = shift;

    my @temp_array = ();

    if (open INF, "$filename") {
        while (<INF>) {
            s/[\r\n]//g;
            push @temp_array, $_;
        }
        close INF;
        strip_comment_lines(\@temp_array, $array_ref, $strip_comments);
    } else {
        print "ERROR: Unable to open the file '$filename'\n";
        exit 1;
    }
}

#
# Show help information.
#
# Input:
#  -
#
# Output:
#  - Help information printed to STDOUT.
#
sub show_help {
    print <<EOF;

Description:
============

This script generates a network configuration file to be used by the SC DAFT
playlist packages. The file can be generated from scratch with default data or
it can take a template file as input and an old network config file that already
have filled in data and use the values from the old file to fill in the same
values from the template file and then generate a new output file that can then
later be edited to fill in the missing data.

Syntax:
=======

 $0 [<OPTIONAL>] <MANDATORY>


 <MANDATORY> are all of the following parameters:


 <OPTIONAL> are one or more of the following parameters:

  -d | --debug <integer>
  -g | --generate-param-help
  -k | --keep-missing-params
  -h | --help
  -f | --old-file <filename>
  -m | --only-changeme
  -o | --output-file <filename>
  -r | --output-format [one-line|multiple-lines]
  -p | --parameter <name>=<value>
  -c | --strip-comments
  -e | --strip-empty-lines
  -t | --template-file <filename>


 where:

  --debug <integere>
  -d <integer>
  ------------------
  Print extra debug information to STDERR.
    0: No debug information printed
    1: Minimal level of debug information printed
    2: Medium level of debug information printed
    3: Max level of debug information printed


  --generate-param-help
  -g
  ---------------------
  If specified then it will generate the help information from the template file
  and write it into a text file where it only shows the parameter names and the
  help text for each parameter.


  --keep-missing-params
  -k
  ---------------------
  If specified then it will keep parameters that exist in the old file but not
  in the template file and these parameters will be added to the end of the new file.
  By default these missing parameters are deleted and not included in the output file.


  --help
  -h
  ------
  Shows this help.


  --old-file <filename>
  -f <filename>
  ---------------------
  Specifies the file that contain an old network configuration file with already
  filled out data. The data from this file will be used to fill in the same named
  parameters from the template file.


  --only-changeme
  -m
  ---------------
  If this parameter is given then only parameters containing the string CHANGEME
  are changed from the template data.


  --output-file <filename>
  -o <filename>
  ------------------------
  Specifies the output file that contain the generated network configuration data.
  If this parameter is not specified then output will be printed to STDOUT.


  --output-format [one-line|multiple-lines]
  -r [one-line|multiple-lines]
  -----------------------------------------
  If specified and with value "multiple-lines" then the written output will have
  each parameter split up into muliple line where each parameter attribute is on
  a new line.

  If specified and with value "one-line" then each parameter is written as a
  long line with each parameter attribute on the same line.

  If not specified then each parameter is written using the same format as the
  template file, i.e. either as a single line or multiple lines with kept
  formatting, this is the default.


  --parameter <name>=<value>
  -p <name>=<value>
  --------------------------
  If specified then it will update the specified named parameter with the specified
  value instead of using the value from the old file (if specified) or use the
  default value from the template file.
  This parameter can be specified multiple times if more than one parameter should
  be changed.

  Examples of how to change a parameter value:

  1) If the parameter has only one "name" and one "value" attribute, and the value
     attribute should be changed, for example:

     <parameter name="crd_release_name" value="CHANGEME"/>

     --parameter crd_release_name=eiffelesc

  2) If the parameter has only one "name" and one "value" attribute, but the value
     consists of another parameter with a value, and the value of that parameter
     attribute should be changed, for example:

     <parameter name="eric_sc_values_hash_parameter_01" value="global.ericsson.scp.enabled=CHANGEME"/>

     --parameter eric_sc_values_hash_parameter_01=global.ericsson.scp.enabled=true

  3) If the parameter has only one "name" and multiple other attributes, and you want
     to change the value of one of these, then specify the name of the parameter and
     separate the attribute name with a "." followed by the attribute name, for example:

     <parameter name="default_user_01" user="bsf-admin" initial_password="bsfbsf" password="CHANGEME" groups="bsf-admin"/>

     --parameter default_user_01.password=a1b1c1.A1B1C1

  4) If the parameter has only one "name" but it contains multiple "." and multiple other attributes,
     and you want to change the value of one of these, then specify the name of the parameter and
     separate the attribute name with a "." followed by the attribute name, for example:

     <parameter name="day0_admin_secret_hash_parameter_metadata.name"
        application_type="^dsc\$"
        description=" This parameter specifies the name of the secret used for the day 0 admin user and it will be used to update the day0adminsecret_Template.yaml file.\\n
        This parameter can be overridden by the playlist variable SC_RELEASE_NAME which has higher priority, but at least one must be specified."
        playlist="^(001_Deploy_SC|002_Upgrade_SC|201_EVNFM_Deploy_SC)\$"
        reuse_deployed_helm_value="CHANGEME"
        valid_releases="^1\\.\\d+\\.\\d+\$"
        value="CHANGEME"
     />

     --parameter day0_admin_secret_hash_parameter_metadata.name.reuse_deployed_helm_value=true
     --parameter day0_admin_secret_hash_parameter_metadata.name.value=eric-dsc-day0adminsecret

     Or similar to 1) above, it is more consistent to always use it like this:

     <parameter name="crd_release_name" value="CHANGEME"/>

     --parameter crd_release_name.value=eiffelesc


  --strip-comments
  -c
  ----------------
  If specified then the output will not contain any comments.


  --strip-empty-lines
  -e
  -------------------
  If specified then the output will not contain any empty lines.


  --template-file <filename>
  -t <filename>
  --------------------------
  Specifies the template file to use as a base. If this parameter is not specified
  then the tool will use the built-in template.


Examples:
=========

  Example 1:
  ----------
  To generate a new network configuration file from the built-in template:

  $0 \\
    --output-file SC_Node.xml

  Example 2:
  ----------
  To generate a new network configuration file from a specified template file:

  $0 \\
    --template-file /path/to/Network_Config_Template.xml \\
    --output-file SC_Node.xml

  Example 3:
  ----------
  To generate a new network configuration file from a specified template file and using data
  from an old configuration file:

  $0 \\
    --template-file /path/to/Network_Config_Template.xml \\
    --old-file old/SC_Node_Old.xml \\
    --output-file SC_Node_New.xml

  Example 4:
  ----------
  To generate a new network configuration file from the built-in template, specifying hard coded
  parameters on the command line for some of the parameters and outputing to STDOUT:

  $0 \\
    --old-file old/SC_Node_Old.xml \\
    --parameter eric_sc_values_hash_parameter_global.ericsson.scp.enabled.value=true \\
    --parameter eric_sc_values_hash_parameter_global.ericsson.spr.enabled.value=true \\
    --parameter eric_sc_values_anchor_parameter_VIP_SIG_Diameter.value=\\'10.155.107.140\\' \\
    --parameter default_user_001.initial_password=rootroot \\
    --parameter default_user_001.password=a1b1c1.A1B1C

  Note: To get single quotes (') around the IP-address the single quotes must be escaped,
  see last parameter above.

  Example 5:
  ----------
  To generate a file with help information for all parameters:

  $0 \\
    --generate-param-help \\
    --output-file ParameterHelp.txt


Return code:
============

 0: Successful
 1: Unsuccessful, some failure detected.

EOF
}

#
# Split one <access or <parameter line from one line into multiple
# lines.
#
# Input:
#  - Line to split.
#
# Output:
#  - Multiple lines as one string.
#
sub split_line {
    my $line = shift;

    my $lf_at_end = 0;
    my $tag;
    my %tag_data;
    my $whitespace;

    if ($line =~ /.+\n$/s) {
        $lf_at_end = 1;
    }

    if ($line =~ /^(\s*)<(\w+)\s+/) {
        $whitespace = $1;
        $tag = $2;
        $line =~ s/^(\s*)<(\w+)\s+//;
    } else {
        # Unexpected format, just return back data unchanged
        return $line;
    }

    # Split the included parameters
    extract_all_parameters($line, \%tag_data);

    # First output the "name" parameter if it exists
    if (exists $tag_data{"name"}) {
        $line = "$whitespace<$tag name=\"$tag_data{name}\"\n";
    } else {
        $line = "$whitespace<$tag\n";
    }

    for my $key (sort keys %tag_data) {
        next if $key eq "name";
        $line .= "$whitespace    $key=\"$tag_data{$key}\"\n";
    }
    if ($lf_at_end) {
        $line .= "$whitespace/>\n";
    } else {
        $line .= "$whitespace/>";
    }

    return $line;
}

#
# Strip XML comments from input array.
#
# Input:
#  - Input array reference
#  - Output array reference
#  - If comments should be stripped away (=1) or not (=0).
#
# Output:
#  - The output array is updated with all found data.
#
sub strip_comment_lines {
    my $in_array_ref = shift;
    my $out_array_ref = shift;
    my $strip_comments = shift;

    my $inside_comment = 0;
    my $line;
    my $indentation_str = "";

    if ($generate_param_help) {
        push @$out_array_ref, "# Network Configuration File Parameters #";
        push @$out_array_ref, "Below is a description of the different parameter values that might need to be configured.";
        push @$out_array_ref, "The Usage information for a parameter determines if a parameter is used or not for a specific job type and thus if it needs to be edited or not.";
        push @$out_array_ref, "**Note:**";
        push @$out_array_ref, "Ensure that all applicable parameters has been changed. If a parameter value is not changed from the default CHANGEME value and it is used by the job then the job might fail due to an invalid parameter value.";
    }

    for (@$in_array_ref) {
        s/[\r\n]//g;
        $line = $_;
        if ($generate_param_help) {
            # Don't strip comments, instead only save the comments
            if ($inside_comment) {
                if ($line =~ /^(.*?)-->(.*)/) {
                    $inside_comment = 0;
                    $line = "$1$2";
                    if ($line =~ /^\s*$/) {
                        # Nothing but spaces left
                        $line = "";
                    }
                    next if ($line eq "");  # Take next line, generate no output
                } else {
                    # Still inside a multi line comment.
                    $line =~ s/^$indentation_str/    /;
                    if ($line =~ /^\s*=+\s*$/) {
                        $line = "";
                    }
                    push @$out_array_ref, $line;
                    next;
                }
            }

            # Look for one line comments
            while ($line =~ /^(.*?)<!--.+?-->(.*)/) {
                $line = "$1$2";
                if ($line =~ /^\s*$/) {
                    # Nothing but spaces left
                    $line = "";
                    last;
                }
            }
            if ($line eq "") {
                if ($line ne $_) {
                    # We have removed comments and there is nothing left. Take next line, generate no output
                    next;
                }
            }

            # Look for start of comment
            if ($line =~ /^(.*?)<!--.*/) {
                $line = $1;
                $inside_comment = 1;
                if ($line =~ /^\s*$/) {
                    # Nothing but spaces left
                    $indentation_str = $line;
                    $line = "";
                }
                next if ($line eq "");  # Take next line, generate no output
            }

            if ($line =~ /^\s*<(node|host)\s+name="(.+?)"/) {
                push @$out_array_ref, "## $2 ##";
            } elsif ($line =~ /^\s*<parameters>/) {
                push @$out_array_ref, "## > User Defined Parameters ##";
            } elsif ($line =~ /^\s*<(access|parameter)\s+name="(.+?)"\s*/) {
                push @$out_array_ref, "* $2";
            }
            # Take next line
            next;
        } else {
            # Don't generate help file, check if comments should be removed
            if ($inside_comment) {
                if ($line =~ /^(.*?)-->(.*)/) {
                    $inside_comment = 0;
                    $line = "$2";
                    if ($line =~ /^\s*$/) {
                        # Nothing but spaces left
                        $line = "";
                    }
                    next if ($line eq "");  # Take next line, generate no output
                } else {
                    # Still inside a multi line comment.
                    # Take next line, generate no output
                    next;
                }
            }
            if ($strip_comments) {
                # Look for one line comments
                while ($line =~ /^(.*?)<!--.+?-->(.*)/) {
                    $line = "$1$2";
                    if ($line =~ /^\s*$/) {
                        # Nothing but spaces left
                        $line = "";
                        last;
                    }
                }
                if ($line eq "") {
                    if ($line ne $_) {
                        # We have removed comments and there is nothing left. Take next line, generate no output
                        next;
                    }
                }

                # Look for start of comment
                if ($line =~ /^(.*?)<!--.*/) {
                    $line = $1;
                    $inside_comment = 1;
                    if ($line =~ /^\s*$/) {
                        # Nothing but spaces left
                        $line = "";
                    }
                    next if ($line eq "");  # Take next line, generate no output
                }
            }
        }
        push @$out_array_ref, $line;
    }
}

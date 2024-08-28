#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2023-06-21 14:26:00
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

# Use this script to extract IP-addresses from the network configuration files.
# Example usage:
#
#   *   perl/bin/extract_ip_addresses.pl network_config_files/Eevee.xml
#
#   *   cat network_config_files/Eevee.xml | perl/bin/extract_ip_addresses.pl
#
#   *   for f in $(ls -1 network_config_files/*.xml); do printf "\n\nFile: $f\n"; perl/bin/extract_ip_addresses.pl $f; done;

use strict;
use warnings;

#  * rg -B6 -iP 'value.*=.*(\d+\.\d+\.\d+\.\d+|[a-f0-9]+:)' network_config_files/*.xml > ip_addresses_v2.log
#  * cat ip_addresses_v2.log| perl -e 'while (<>){if (/^(\S+\.xml).*name="(.+)"/){$f=$1;$n=$2;}elsif(/ value.*="(.+)"/){$v=$1;next if ($v=~/eric-data-wide-column-database-cd/);$h{"$f,$v"}++;printf "%-100s  %-100s  %s\n", $f, $n, $v;}};print "-"x80 . "\n";for $k (sort keys %h){($f,$v)=split ",", $k;printf "%-100s  %-100s  %s\n", $f, $v, $h{$k};}' | sed "s/'//g" > ip_addresses_v2_sorted.log

my %ip;
my %name_info;

while (<>) {
    my $name="";
    my $v1="";
    my $v2="";
    my $v3="";
    my $v4="";
    my $v5="";
    if (/ name="([^"]+)".*value="([^"]+)"/) {
        $name=$1;
        $v1=$2;
        $v1 =~ s/'//g;
    }
    if (/ name="([^"]+)".*value_EVNFM="([^"]+)"/) {
        $name=$1;
        $v2=$2;
        $v2 =~ s/'//g;
    }
    if (/ name="([^"]+)".*value_IntIPv4ExtIPv4="([^"]+)"/) {
        $name=$1;
        $v3=$2;
        $v3 =~ s/'//g;
    }
    if (/ name="([^"]+)".*value_IntIPv6ExtIPv6="([^"]+)"/) {
        $name=$1;
        $v4=$2;
        $v4 =~ s/'//g;
    }
    if (/ name="([^"]+)".*value_IntIPv6ExtIPv6forSignalingExtIPv4forOamExtIPv6forBsfHttpExtIPv4forDiameter="([^"]+)"/) {
        $name=$1;
        $v5=$2;
        $v5 =~ s/'//g;
    }

    if ($name ne "") {
        next unless ($v1 =~ /(\d+\.\d+\.\d+\.\d+|[a-f0-9]+:)/ || $v2 =~ /(\d+\.\d+\.\d+\.\d+|[a-f0-9]+:)/ || $v3 =~ /(\d+\.\d+\.\d+\.\d+|[a-f0-9]+:)/ || $v4 =~ /(\d+\.\d+\.\d+\.\d+|[a-f0-9]+:)/ || $v5 =~ /(\d+\.\d+\.\d+\.\d+|[a-f0-9]+:)/);
        next if ($v1 =~ /eric-data-wide-column-database-cd/);
        $ip{$v1}{$name}++ if ($v1);
        $ip{$v2}{$name}++ if ($v2);
        $ip{$v3}{$name}++ if ($v3);
        $ip{$v4}{$name}++ if ($v4);
        $ip{$v5}{$name}++ if ($v5);
        $name_info{ sprintf "%-150s  %-30s  %-30s  %-30s  %-30s  %s", $name, $v1, $v2, $v3, $v4, $v5 }++;
    }
}

printf "Parameters with IP-addresses:\n%-150s  %-30s  %-30s  %-30s  %-30s  %s\n", "Name", "Value", "EVNFM", "IntIPv4ExtIPv4", "IntIPv6ExtIPv6", "IntIPv6ExtIPv6forSignalingExtIPv4forOamExtIPv6forBsfHttpExtIPv4forDiameter";
for my $name (sort keys %name_info) {
    print "$name\n";
}

printf "\nUsed IP addresses:\n%2s  %-30s  %s\n", "", "IP", "Names";
my $cnt = 1;
for my $key (sort keys %ip) {
    next if ($key eq "CHANGEME");
    printf "%2d  %-30s  %s\n", $cnt++, $key, (join ", ", sort keys %{$ip{$key}});
}

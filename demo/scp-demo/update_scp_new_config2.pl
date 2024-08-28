#!/usr/bin/perl
use strict;
use warnings;

my $fqdn = "";
my $file = shift;
$fqdn = shift;
my $toolsns = "";
$toolsns = shift;

my $in_service_address = 0;
my $in_nrf = 0;
my $in_nf_instance = 0;
my $in_vpn = 0;

$fqdn = "scp1.region1.scp.5gc.mnc073.mcc262.3gppnetwork.org" if ($fqdn eq "");
$toolsns = $ENV{NAMESPACE} if ($toolsns eq "");

my $scp_port=`kubectl get --namespace $ENV{NAMESPACE} -o jsonpath="{.spec.ports[0].nodePort}" services eric-scp-worker`;
my $scp_tls_port=`kubectl get --namespace $ENV{NAMESPACE} -o jsonpath="{.spec.ports[1].nodePort}" services eric-scp-worker`;
my $node_ip =`kubectl get nodes --namespace $ENV{NAMESPACE} -o jsonpath="{.items[3].status.addresses[0].address}"`;
my $nrf_cluster_ip = `kubectl get --namespace $toolsns -o jsonpath="{.items[0].status.podIP}" pod -l "app=eric-nrfsim"; `;

print "namespace for tools: $toolsns\n";
print "nrf_cluster_ip: $nrf_cluster_ip\n";
print "node_ip: $node_ip\n";
print "scp_port $scp_port\n";
print "scp_tls_port $scp_tls_port\n";
print "fqdn $fqdn\n";

# read file into array
print "Reading file $file\n";

my @array;
open(FILE, "<", $file)
    or die "Failed to open file: $!\n";
@array = <FILE>;
 
close FILE;
chomp(@array);


print "Analysing file $file\n";

while (my ($ArrayIndex, $ArrayItem) = each @array) {

	$in_nf_instance = 1 if ($ArrayItem =~ /<nf-instance/);
	$in_nf_instance = 0 if ($ArrayItem =~ /<\/nf-instance/);

	$in_service_address = 1 if ($ArrayItem =~ /<service-address/);
	$in_service_address = 0 if ($ArrayItem =~ /<\/service-address/);
	
	$in_vpn = 1 if ($ArrayItem =~ /<vpn/);
	$in_vpn = 0 if ($ArrayItem =~ /<\/vpn/);

	$in_nrf = 1 if ($ArrayItem =~ /<nrf/);
	$in_nrf = 0 if ($ArrayItem =~ /<\/nrf/);

    #<own-fqdn>scp1.region1.scp.5gc.mnc073.mcc262.3gppnetwork.org</own-fqdn>
	if ($ArrayItem =~ /^(\s*)<own-fqdn>(\S+)<\/own-fqdn>/ && $in_nf_instance) {
		$array[$ArrayIndex] = "$1<own-fqdn>$fqdn</own-fqdn>"
	}

    #<port>31313</port>
	if ($ArrayItem =~ /^(\s*)<port>(\S+)<\/port>/ && $in_vpn) {
		$array[$ArrayIndex] = "$1<port>$scp_port</port>"
	}

	#<ipv4-address>100.100.100.100</ipv4-address>
	if ($ArrayItem =~ /^(\s*)<ipv4-address>\S+<\/ipv4-address>/ && $in_vpn) {
		$array[$ArrayIndex] = "$1<ipv4-address>$node_ip</ipv4-address>"
	}

	#<fqdn>scp1.region1.scp.5gc.mnc073.mcc262.3gppnetwork.org</fqdn>
	if ($ArrayItem =~ /^(\s*)<fqdn>(\S+)<\/fqdn>/ && $in_service_address) {
		$array[$ArrayIndex] = "$1<fqdn>$fqdn</fqdn>"
	}
	
	#<port>30000</port>
	if ($ArrayItem =~ /^(\s*)<port>\d+<\/port>/ && $in_service_address) {
		$array[$ArrayIndex] = "$1<port>$scp_port</port>"
	}
	
	#<tls-port>30002</tls-port>
	if ($ArrayItem =~ /^(\s*)<tls-port>\d+<\/tls-port>/ && $in_service_address) {
		$array[$ArrayIndex] = "$1<tls-port>$scp_tls_port</tls-port>"
	}	

	#<ipv4-address>100.100.100.100</ipv4-address>
	if ($ArrayItem =~ /^(\s*)<ipv4-address>\S+<\/ipv4-address>/ && $in_service_address) {
		$array[$ArrayIndex] = "$1<ipv4-address>$node_ip</ipv4-address>"
	}
	
	#<ipv4-address>100.100.100.100</ipv4-address>
	if ($ArrayItem =~ /^(\s*)<ipv4-address>\S+<\/ipv4-address>/ && $in_nrf) {
		$array[$ArrayIndex] = "$1<ipv4-address>$nrf_cluster_ip</ipv4-address>"
	}	

	#<port>80/port>
	if ($ArrayItem =~ /^(\s*)<port>\S+<\/port>/ && $in_nrf) {
		$array[$ArrayIndex] = "$1<port>80</port>"
	}
}

print "Writing file ~/scp_sample_config.xml\n";

open (OFILE, "> $ENV{HOME}/scp_sample_config.xml") || die "problem opening $!\n";
while (my ($ArrayIndex, $ArrayItem) = each @array) {
	print OFILE "$ArrayItem\n";
}
close OFILE;

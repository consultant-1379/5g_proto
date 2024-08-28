#!/usr/bin/perl
use strict;
use warnings;
use Getopt::Std;

my %options=();
getopts("f:q:t:s:n:", \%options);
#print "-f $options{f}\n" if defined $options{h};

my $file = $options{f};
my $fqdn = "sepp1.region1.sepp.5gc.mnc072.mcc262.3gppnetwork.org";
$fqdn = $options{q} if exists $options{q};
my $toolsns = $ENV{NAMESPACE};
$toolsns = $options{t} if exists $options{t};
my $namespace = $ENV{NAMESPACE};
$namespace = $options{n} if exists $options{n};
my $sepp_rp_ns = "";
$sepp_rp_ns = $options{s} if exists $options{s};

my $in_service_address = 0;
my $in_service_address_internal = 0;
my $in_service_address_external = 0;
my $in_static_nf_instance_data = 0;
my $in_static_nf_rp = 0;
my $in_nrf = 0;
my $in_nf_instance = 0;
my $in_vpn = 0;


my $sepp_port=`kubectl get --namespace $namespace -o jsonpath="{.spec.ports[0].nodePort}" services eric-sepp-worker`;
my $sepp_tls_port=`kubectl get --namespace $namespace -o jsonpath="{.spec.ports[1].nodePort}" services eric-sepp-worker`;
my $sepp2_port=`kubectl get --namespace $namespace -o jsonpath="{.spec.ports[0].nodePort}" services eric-sepp-worker-2`;
my $sepp2_tls_port=`kubectl get --namespace $namespace -o jsonpath="{.spec.ports[1].nodePort}" services eric-sepp-worker-2`;
my $node_ip =`kubectl get nodes --namespace $namespace -o jsonpath="{.items[3].status.addresses[0].address}"`;
my $nrf_cluster_ip = `kubectl get --namespace $toolsns -o jsonpath="{.items[0].status.podIP}" pod -l "app=eric-nrfsim"; `;
my $sepp_rp_tls_port;
if ($sepp_rp_ns != "") {
    $sepp_rp_tls_port = `kubectl get --namespace $sepp_rp_ns -o jsonpath="{.spec.ports[1].nodePort}" services eric-sepp-worker-2`;
} else {
    $sepp_rp_tls_port = "443";
}

print "\nVariables:\n---------\n";
printf ("%-30s: %s\n","namespace", $namespace);
printf ("%-30s: %s\n","namespace for tools", $toolsns);
printf ("%-30s: %s\n","2nd sepp (RP-SE) namespace", $sepp_rp_ns);
printf ("%-30s: %s\n","nrf_cluster_ip", $nrf_cluster_ip);
printf ("%-30s: %s\n","node_ip", $node_ip);
printf ("%-30s: %s\n","sepp_port (int)", $sepp_port);
printf ("%-30s: %s\n","sepp_tls_port (int)", $sepp_tls_port);
printf ("%-30s: %s\n","sepp2_port (ext)", $sepp2_port);
printf ("%-30s: %s\n","sepp2_tls_port (ext)", $sepp2_tls_port);
printf ("%-30s: %s\n","fqdn", $fqdn);
printf ("%-30s: %s\n","2nd sepp (RP-SE) port", $sepp_rp_tls_port);
print "\nFiles:\n---------\n";
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
	
	$in_service_address_external = 1 if ($ArrayItem =~ /<name>.*external/i);
	$in_service_address_external = 0 if ($ArrayItem =~ /<\/service-address/);

	$in_service_address_internal = 1 if ($ArrayItem =~ /<name>.*internal/i);
	$in_service_address_internal = 0 if ($ArrayItem =~ /<\/service-address/);

    $in_static_nf_instance_data = 1 if ($ArrayItem =~ /<static-nf-instance-data>/i);
    $in_static_nf_instance_data = 0 if ($ArrayItem =~ /<\/static-nf-instance-data>/i);
	$in_static_nf_rp = 1 if ($ArrayItem =~ /<fqdn>sepp-\w+.region/i); 
	$in_static_nf_rp = 0 if ($ArrayItem =~ /<\/multiple-ip-endpoint/);

	$in_vpn = 1 if ($ArrayItem =~ /<vpn/);  # not used anymore
	$in_vpn = 0 if ($ArrayItem =~ /<\/vpn/);

	$in_nrf = 1 if ($ArrayItem =~ /<nrf/);
	$in_nrf = 0 if ($ArrayItem =~ /<\/nrf/);

    #<own-fqdn>sepp1.region1.sepp.5gc.mnc073.mcc262.3gppnetwork.org</own-fqdn>
	if ($ArrayItem =~ /^(\s*)<own-fqdn>(\S+)<\/own-fqdn>/ && $in_nf_instance) {
		$array[$ArrayIndex] = "$1<own-fqdn>$fqdn</own-fqdn>"
	}

    #<port>31313</port>
	if ($ArrayItem =~ /^(\s*)<port>(\S+)<\/port>/ && $in_vpn) {
		$array[$ArrayIndex] = "$1<port>$sepp_port</port>"
	}

	#<ipv4-address>100.100.100.100</ipv4-address>
	if ($ArrayItem =~ /^(\s*)<ipv4-address>\S+<\/ipv4-address>/ && $in_vpn) {
		$array[$ArrayIndex] = "$1<ipv4-address>$node_ip</ipv4-address>"
	}

	#<fqdn>sepp1.region1.sepp.5gc.mnc073.mcc262.3gppnetwork.org</fqdn>
	if ($ArrayItem =~ /^(\s*)<fqdn>(\S+)<\/fqdn>/ && $in_service_address) {
		$array[$ArrayIndex] = "$1<fqdn>$fqdn</fqdn>"
	}
	
	#<port>30000</port>
	if ($ArrayItem =~ /^(\s*)<port>\d+<\/port>/ && $in_service_address && $in_service_address_internal) {
		$array[$ArrayIndex] = "$1<port>$sepp_port</port>"
	}

	#<port>30000</port>
	if ($ArrayItem =~ /^(\s*)<port>\d+<\/port>/ && $in_service_address && $in_service_address_external) {
		$array[$ArrayIndex] = "$1<port>$sepp2_port</port>"
	}
	
	#<tls-port>30002</tls-port>
	if ($ArrayItem =~ /^(\s*)<tls-port>\d+<\/tls-port>/ && $in_service_address && $in_service_address_internal) {
		$array[$ArrayIndex] = "$1<tls-port>$sepp_tls_port</tls-port>"
	}	

	#<tls-port>30002</tls-port>
	if ($ArrayItem =~ /^(\s*)<tls-port>\d+<\/tls-port>/ && $in_service_address && $in_service_address_external) {
		$array[$ArrayIndex] = "$1<tls-port>$sepp2_tls_port</tls-port>"
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

	#<port>80/port>
	if ($ArrayItem =~ /^(\s*)<port>\S+<\/port>/ && $in_static_nf_rp && $in_static_nf_instance_data) {
		$array[$ArrayIndex] = "$1<port>$sepp_rp_tls_port</port>"
	}
}

print "\nWriting file ~/sepp_sample_config.xml\n";

open (OFILE, "> $ENV{HOME}/sepp_sample_config.xml") || die "problem opening $!\n";
while (my ($ArrayIndex, $ArrayItem) = each @array) {
	print OFILE "$ArrayItem\n";
}
close OFILE;

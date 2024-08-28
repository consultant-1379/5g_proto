#!/usr/bin/perl


################################################################################
# $Revision: 1.0 $                                                             #
#   $Author: EEDCSI $                                                          #
#     $Date: 2022-12-07 20:00:00 $                                             #
################################################################################
#                                                                              #
# (C) COPYRIGHT ERICSSON GMBH 2022                                             #
#                                                                              #
# The copyright to the computer program(s) herein is the property of           #
# Ericsson GmbH, Germany.                                                      #
#                                                                              #
# The program(s) may be used and/or copied only with the written permission    #
# of Ericsson GmbH in accordance with the terms and conditions stipulated in   #
# the agreement/contract under which the program(s) have been supplied.        #
#                                                                              #
################################################################################


use strict;
use warnings;

use Data::Dumper;
use File::Find;
use File::Compare;
use JSON;
use IO::Socket::SSL::Utils;
#use Net::SSLeay;
#use Crypt::OpenSSL::X509;
use Getopt::Long;

#
my %hash_files;
my $key;
my ($i,$j,$k);
my %a;
my $traf1_cert;
my $traf2_cert;
my $external_rootca_list;
my $internal_rootca_list;
my @external_rootca;
my @internal_rootca;
my @temparray;
my $openssl_out ;
my $part;
my %traffic_certificates;
my %rootCAs;
my $name;
my $show_help;
my $noextract = 0;
my @file_list = ();
my $sepp_config_file = "/cmm_config/ericsson-sepp_config.json";
my %asymmetric_key;
my %service_address;
my %external_network;
my %roaming_partners;
my %key_reference;
my %own_network;

my $dir = "/home/eedcsi/2_sc/SCDS-1062/logs_sc_2022-12-02-11-20-38";
my $pem_dir;

my %config;
my $envoy_config_dump_file;


# *****************************
# *                           *
# * HELP                      *
# *                           *
# *****************************

sub show_help {
    print <<EOF;

Description:
============

This script analyses the loaded Certifcates in a SEPP

Syntax:
=======

 $0 [-h] [-i <dir>] [-o <dir>] [-n]

    -h / --help                 Shows this help

    -i / --inputdir <dir>       Use input directory to read collect_ADP_logs
								
    -p / --pemdir <dir>         Use this directory to store extracted certificate pem files
                                or to analyse exiting pem files
	
    -n / --noextract            Don't extract certificates, just pritty-print 
                                certificates in the  the output dir
								
EOF
}


# *****************************
# *                           *
# * read collect_ADP_log      *
# *   structure               *
# *                           *
# *****************************
#
sub parse_tree {
    my ($root_path) = @_;

    my %root;
    my %dl;
    my %count;

    my $path_checker = sub {
      my $name = $File::Find::name;
      if (-d $name) {
        my $r = \%root;
        my $tmp = $name;
        $tmp =~ s|^\Q$root_path\E/?||;
        $r = $r->{$_} ||= {} for split m|/|, $tmp; #/
        $dl{$name} ||= $r;
      }
      elsif (-f $name) {
        my $dir = $File::Find::dir;
        #my $key = "file". ++$count{ $dir };
        #$dl{$dir}{$key} = $_;
        #my $key = "file". ++$count{ $dir };
        $dl{$dir}{$_} = 1;		
      }
    };
    find($path_checker, $root_path);

    return %root;
}

# *****************************
# *                           *
# * Spit List of Certificates *
# *                           *
# *****************************
sub split_certificate_list {
	my @array = [];
    my ($list) = @_;
  
    if ($list =~ /(-----BEGIN CERTIFICATE-----.*-----END CERTIFICATE-----)/s) {
		print $1;
	}
	
	return @array
}



# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************
sub load_parameters {
    # Parse command line parameters

    GetOptions (
        "h|help"                 => \$show_help,
        "i|inputdir=s"           => \$dir,
        "p|pemdir=s"             => \$pem_dir,		
        "n|noextract"            => \$noextract,

    );		
		
	if ($dir eq ".") {
		$dir = `pwd`;
		chomp($dir);
	}
	
	if ($show_help) {
        show_help();
        exit 0;
    }	
}


# *****************************
# *                           *
# * Print PEM File Summary    *
# *                           *
# *****************************
sub print_pem_file_summary {

	my $key1;
	my $key2;
    my $pem;
	
    my ($dirname) = @_;
	my $filename;
    my $pemfilename;

    print "\n\n========================================================================\n";
	print "Summary of found certificates in directory \n";
	print " $dirname\n";	
	print "========================================================================\n";

	foreach $pemfilename (@file_list){
		
		if (-f $pemfilename) {

			open(OS, "openssl x509 -in $pemfilename -text -noout 2> /dev/null | ") or die $!; 
			local $/ = undef; # read complete output into variable
			$openssl_out = <OS> ;
			close(OS);
			
			next unless  ($openssl_out =~ /Serial Number/s);
			
			print "\n------------------------------------------------------------------------\n";
			print "$pemfilename\n";
			print "------------------------------------------------------------------------\n";

			
			if ( $openssl_out =~ /(Serial Number.*)Subject Public Key Info/sg) {
				$part = $1."\n";	
			}
			
			#X509v3 Basic Constraints: critical
			#                CA:TRUE
			#    Signature Algorithm
			if ( $openssl_out =~ /X509v3 Basic Constraints: \w+\s+(CA:\w+)/sg) {
				$part =  $part. $1."\n" 
			}
			
			
			#        X509v3 extensions:
			#            X509v3 Subject Alternative Name: 
			#                DNS:*.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org
			#
			
			if ( $openssl_out =~ /X509v3 Subject Alternative Name:\s+(.+?)\s+X509v3/sg) {
				$part =  $part. $1."\n" 
			} elsif ( $openssl_out =~ /X509v3 Subject Alternative Name:\s+(.+?)\s+Signature Algorithm:/sg) {
				$part =  $part. $1."\n" 
			}
			
			print $part;
			
			$part = "";
		}
	}	
}


# *****************************
# *                           *
# * Analyse PEM Files         *
# *                           *
# *****************************
sub analyse_pem_files {
	
	my $key1;
	my $key2;
    my $pem;

	print "\n\n========================================================================\n";
	print "Analysis of Certificates\n";
	print "========================================================================\n";

	
	foreach $key1 (@file_list) {
		foreach $key2 (@file_list) {
			next if ($key1 eq $key2);             # if both files are the same, skip
			next unless (compare($key1, $key2));  # if content of both files is the same, skip
			
			#print "openssl verify -CAfile ${key1} ${key2} \n";
			
			open(OS, "openssl verify -CAfile ${key1} ${key2} 2> /dev/null| ") or die $!; 
			local $/ = undef; # read complete output into variable
			$openssl_out = <OS> ;
			#print "$openssl_out\n";
		
			close(OS);
			
	 		unless ( $openssl_out =~ /error\s\d+/sg) {
				if ( $openssl_out =~ /OK/sg) {					
					print "CA $key1 was used to sign $key2\n";
				}
			}	
		}
	}
	print "------------------------------------------------------------------------\n";

}

# *****************************
# *                           *
# * Recusively called         *
# * subroutine                *
# *                           *
# *****************************
sub search_dir {

	my ($dir) = @_;
	my $dh; # handle
	
	if ( !opendir ($dh, $dir)) {
		warn "Unable to open $dir: $!\n";
		return;
	}
	
	my @FILES = grep { $_ ne '.' && $_ ne '..' } readdir($dh);
		
	foreach my $file (@FILES) {
		my $path = "$dir/$file";    
		if ( -d $path ) {
			search_dir ($path); 
		} else {
			push(@file_list, $path);
		}
	}
	closedir ($dh);
	
}


# *****************************
# *                           *
# *****************************
sub read_sepp_config_and_extract_certificates {
	my $json;
	my $json_text;
	my $sepp_config;
	
	$json_text = do {
		open(my $json_fh, "<:encoding(UTF-8)", $dir.$sepp_config_file)
			or die("Can't open \"$envoy_config_dump_file\": $!\n");
		local $/;
		<$json_fh>
	};	
	
	$json = JSON->new;
	$sepp_config = $json->decode($json_text);
		

	#find asymmetric-key -> certificate
	foreach $i ( 0..$#{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"asymmetric-key"}}) {
		if (exists $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"asymmetric-key"}[$i]{"certificate"}) {
			$asymmetric_key{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"asymmetric-key"}[$i]{name}} =  $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"asymmetric-key"}[$i]{"certificate"} ;
		}
	}
	

	#find service-address -> asymmetric-key-ref
	foreach $i ( 0..$#{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"service-address"}}) {
		if (exists $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"service-address"}[$i]{"asymmetric-key-ref"}) {
			$service_address{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"service-address"}[$i]{name}} =  $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"service-address"}[$i]{"asymmetric-key-ref"} ;
		}
	}
	
	#find own-network -> service-address-ref
	foreach $i ( 0..$#{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"own-network"}}) { 
		if (exists $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"own-network"}[$i]{"service-address-ref"}) {
			$own_network{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"own-network"}[$i]{name}}{"service-address-ref"} = $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"own-network"}[$i]{"service-address-ref"} ;
		}
		if (exists $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"own-network"}[$i]{"trusted-certificate-list"}) {
			$own_network{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"own-network"}[$i]{name}}{"trusted-certificate-list"} = $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"own-network"}[$i]{"trusted-certificate-list"} ;
		}
	}
		
	#find external-network -> service-address-ref
	foreach $i ( 0..$#{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"external-network"}}) {
		if (exists $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"external-network"}[$i]{"service-address-ref"}) {
			$external_network{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"external-network"}[$i]{name}} = $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"external-network"}[$i]{"service-address-ref"} ;
		}
		
			
		#find roaming-partner -> trusted-certificate-list
		foreach $j ( 0..$#{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"external-network"}[$i]{"roaming-partner"}}) {
			if (exists $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"external-network"}[$i]{"roaming-partner"}[$j]{"trusted-certificate-list"}) {
				$roaming_partners{$sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"external-network"}[$i]{"roaming-partner"}[$j]{name}} = $sepp_config->{data}{"ericsson-sepp:sepp-function"}{"nf-instance"}[0]{"external-network"}[$i]{"roaming-partner"}[$j]{"trusted-certificate-list"} ;
			}
		}
	}

	print "\n\n========================================================================\n";
	print "Analysis of SEPP Configuration\n";
	print "========================================================================\n";

	
	print "Own Network:\n";
	
	foreach $key (keys %own_network) {
		printf ("%s\t%s\t%s\t%s\n",  $key, $own_network{$key}{"service-address-ref"},$service_address{$own_network{$key}{"service-address-ref"}},$asymmetric_key{$service_address{$own_network{$key}{"service-address-ref"}}} );
	}
	foreach $key (keys %own_network) {
		printf ("%s\t%s\t%s\t%s\n",  $key, $own_network{$key}{"trusted-certificate-list"}, "", "" );
	}

	print "\nExternal Network:\n";
	
	foreach $key (keys %external_network) {
		printf ("%s\t%s\t%s\t%s\n",  $key, $external_network{$key},$service_address{$external_network{$key}},$asymmetric_key{$service_address{$external_network{$key}}} );
	}
	
	print "\nRoaming Partner:\n";
	
	foreach $key (keys %roaming_partners) {
		printf ("%s\t%s\t%s\t%s\n",  $key, $roaming_partners{$key}, "", "" );
	}	

}

# *****************************
# *                           *
# *****************************
sub read_envoy_config_and_extract_certificates {
	
	foreach $i (keys $hash_files{"sc_config"}{"sepp"}) {
		$envoy_config_dump_file = $dir."/sc_config/sepp/".$i."/config_dump.txt"  # take the last sepp-worker found
	}
	
	my $json_text = do {
		open(my $json_fh, "<:encoding(UTF-8)", $envoy_config_dump_file)
			or die("Can't open \"$envoy_config_dump_file\": $!\n");
		local $/;
		<$json_fh>
	};	
	
	my $json = JSON->new;
	my $envoy_config = $json->decode($json_text);
	
	
	# find the right array that holds the rootCA and traffic certificates in the envoy config dump
	foreach $i ( 0..$#{$envoy_config->{configs}} ) {
		$j = $i if (exists $envoy_config->{configs}[$i]{dynamic_active_secrets});
	}
		
	foreach $i ( 0..$#{$envoy_config->{configs}[$j]{dynamic_active_secrets}} ) {
		
		if (exists $envoy_config->{configs}[$j]{dynamic_active_secrets}[$i]{secret}{tls_certificate}) { #traffic certificate
			$name = $envoy_config->{configs}[$j]{dynamic_active_secrets}[$i]{secret}{name};
			next if ($name =~ /pm_/);
			
			$traffic_certificates{$name} = $envoy_config->{configs}[$j]{dynamic_active_secrets}[$i]{secret}{tls_certificate}{certificate_chain}{inline_string};
		
		} elsif (exists $envoy_config->{configs}[$j]{dynamic_active_secrets}[$i]{secret}{validation_context}) { #rootCA
			$name = $envoy_config->{configs}[$j]{dynamic_active_secrets}[$i]{secret}{name};
			next if ($name =~ /pm_/);
	
			# split certifcate list into individual certificates  .*?/s  take as less as possible, but include new line
			@temparray = ($envoy_config->{configs}[$j]{dynamic_active_secrets}[$i]{secret}{validation_context}{trusted_ca}{inline_string}  =~ /(-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----)/sg);
			push @{$rootCAs{$name} }, @temparray;
		}
	}
}

# *****************************
# *                           *
# *****************************
sub write_envoy_certifcates {
	foreach $key (keys %traffic_certificates) {
		$name = $key;
		$name =~ s/[\#!:]/_/g;
		
		open(OUT, '>',  "${pem_dir}/trafcert_${name}.pem") or die $!; 
		print OUT $traffic_certificates{$key};
		close(OUT);
	}	
	
	foreach $key (keys %rootCAs) {
		$name = $key;
		$name =~ s/[\#!:]/_/g;   # replace a few characters from the envoy dump certifcate name
		
		@temparray = @{$rootCAs{$key}};
		
	
		foreach $i ( 0..$#temparray) {
		
			open(OUT, '>',  "${pem_dir}/rootCA_${i}_${name}.pem") or die $!; 
			print OUT $temparray[$i];
			close(OUT);	
		}
	}	
}

# **************
# *            *
# * Main logic *
# *            *
# **************

load_parameters();  # Parse command line parameters

%hash_files = parse_tree($dir) unless ($noextract);

read_sepp_config_and_extract_certificates();


unless ($noextract) {
	
	#print encode_json(%hash_files);
	
	read_envoy_config_and_extract_certificates();
	
	write_envoy_certifcates();
}

search_dir($pem_dir);

print_pem_file_summary($pem_dir);

analyse_pem_files($pem_dir);


#print Dumper @file_list;

# my $cert = PEM_string2cert($external_rootca[1]);      
# $cert = PEM_string2cert($traf1_cert);
# 
# print Dumper CERT_asHash($cert);
#$VAR1 = {
#          'subject' => {
#                         'commonName' => 'csepp.region1.5gc.mnc033.mcc206.3gppnetwork.org',
#                         'organizationalUnitName' => 'ISP',
#                         'organizationName' => 'Services',
#                         'stateOrProvinceName' => 'Lab',
#                         'countryName' => 'SE',
#                         'localityName' => 'SC1'
#                       },
#          'serial' => -1,
#          'version' => 2,
#          'not_before' => 1669971181,
#          'not_after' => 1985331181
#        };
#
#$VAR1 = {
#          'subject' => {
#                         'commonName' => 'nfamf.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org',
#                         'organizationalUnitName' => 'ISP',
#                         'organizationName' => 'Services',
#                         'stateOrProvinceName' => 'Lab',
#                         'countryName' => 'SE',
#                         'localityName' => 'SC1'
#                       },
#          'serial' => -1,
#          'version' => 2,
#          'not_before' => 1669971267,
#          'subjectAltNames' => [
#                                 [
#                                   'DNS',
#                                   '*.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org'
#                                 ]
#                               ],
#          'not_after' => 1701507267
#        };

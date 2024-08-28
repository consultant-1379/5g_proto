#! /usr/bin/perl
use Data::Dumper;
use strict;
use warnings;
use Getopt::Long;            # standard Perl module

my $NORMAL_EXIT             =  0;       # No error, all went well we hope and pray
my $ERR_Standard            =  1;       # Standard return value for errors
my $ERR_PackageNotFound     =  2;       # Package not found at site
my $ERR_Directory           =  3;       # Problems creating or switching to directory
my $ERR_PackageName         =  4;       # Package name not provided
my $ERR_Download            =  5;       # Error during download
my $ERR_Failed_md5          =  6;       # MD5/SHA256 check failed
my $ERR_Open_file           =  7;       # Could not open file
my $ERR_Missing_file        =  8;       # File missing
my $ERR_Package_size        =  9;       # Package size not within expected range

my $nfinstance = "instance_1";
my $extNW = "external_Network"; #"externalNetwork";
my $routing_case_ext = "routing_from_external";
my $routing_case_int = "routing_from_internal";
my $fop = "fob_default";   #"fop1";


my $show_help;
my $external_network;
my $static_nf;
my $nf_pool;
my $routing_rule;
my $start=0;
my $end=0;
my $out="";
my $id;

my $debug = 0;


sub show_help {
  print << "END"
NAME
   $0 - Create NETCONF XML configuration files for loading to SEPP

SYNOPSIS
   $0 [-h] [-x] [-s] [-p] [-r] [-a <start>] [-b <end>] 

DESCRIPTION
  Create NETCONF XML configuration files for testing a specified amount of Romaing Partners.
  A few parameters are hardcoded in the script as variables:  
    - nfinstance
    - external_Network
    - routing_case_ext
    - routing_case_int
    - failover-profile

  This script can generate several parts of the configuration, depending on given command line options:
    - external-network
    - static-nf
    - nf-pool
    - routing-rule



OPTIONS

  -h, --help                     Display this help and exit	
  -x, --external-network         Generate code for external-network
  -s, --static-nf                Generate code for static_nf
  -p, --nf-pool                  Generate code for nf_pool
  -r, --routing-rule             Generate code for routing-rule
  -a, --start=s                  Specifies the start number of roaming parter 
  -b, --end=s                    Specifies the last number of roaming parter 


EXAMPLES
   $0 -a 1 -b 100 -x -s -p -r 


END
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
        "x|external-network"     => \$external_network,
        "s|static-nf"            => \$static_nf,
        "p|nf-pool"              => \$nf_pool,
        "r|routing-rule"         => \$routing_rule,
        "a|start=s"              => \$start,
        "b|end=s"                => \$end
		);


    if ($show_help) {
        show_help();
        exit $NORMAL_EXIT;
    }
	
	if ($start - $end > 0) {
		print "Start number has to be less than the end value\n\n";
		
        show_help();
        exit $NORMAL_EXIT;		
	}
}


# ********************
# *                  *
# * external-network *
# *                  *
# ********************
sub external_network {
    $id = shift;

    $out = $out.  << "END";
        <roaming-partner>
          <name>RP_${id}</name>
          <comment>Trusted roaming partner in PLMN 222-${id}</comment>
          <routing-case-ref>${routing_case_ext}</routing-case-ref>
          <domain-name>*.region1.amf.5gc.mnc${id}.mcc222.3gppnetwork.org</domain-name>
          <trusted-certificate-list>sc-traf-root-ca-list1</trusted-certificate-list>
          <supports-target-apiroot>true</supports-target-apiroot>
        </roaming-partner>
END

}
	  
	  
# ***************************
# *                         *
# * static-nf-instance-data *
# *                         *
# ***************************
sub static_nf_instance_data {
    $id = shift;

    $out = $out.  << "END";
      <static-nf-instance-data>
        <name>static_sepp_RP_${id}_data</name>
        <static-nf-instance>
          <name>sepp_RP-${id}</name>
          <nf-type>sepp</nf-type>
          <nf-set-id></nf-set-id>
          <scp-domain></scp-domain>
          <static-nf-service>
            <name>default</name>
            <capacity>10</capacity>
            <priority>10</priority>
            <set-id></set-id>
            <address>
              <scheme>http</scheme>
              <fqdn>sepp-${id}.region1.sepp.5gc.mnc${id}.mcc222.3gppnetwork.org</fqdn>
            </address>
          </static-nf-service>
        </static-nf-instance>
      </static-nf-instance-data>
END

}
	  
	  
# ***************************
# *                         *
# * nf-pool                 *
# *                         *
# ***************************
sub nf_pool {
    $id = shift;

    $out = $out.  << "END";
      <nf-pool>
        <name>sepp_RP_${id}_pool</name>
        <nf-pool-discovery>
          <name>sepp_RP_${id}_pool_discovery</name>
          <static-nf-instance-data-ref>static_sepp_RP_${id}_data</static-nf-instance-data-ref>
          <update-interval>1h</update-interval>
        </nf-pool-discovery>
        <roaming-partner-ref>RP_${id}</roaming-partner-ref>
      </nf-pool>
END

}
	  
	  
# ***************************
# *                         *
# * routing_rule            *
# *                         *
# ***************************
sub routing_rule {
    $id = shift;

    $out = $out.  << "END";
        <routing-rule>
          <name>internal_to_RP_${id}_sepp_rr</name>
          <condition>var.mcc=='${id}' and var.mnc=='222'</condition>
          <routing-action>
            <name>action-route-round-robin</name>
            <action-route-round-robin>
              <failover-profile-ref>${fop}</failover-profile-ref>
              <target-roaming-partner>
                <roaming-partner-ref>RP_${id}</roaming-partner-ref>
              </target-roaming-partner>
            </action-route-round-robin>
          </routing-action>
        </routing-rule>
END

}

# **************
# *            *
# * Main logic *
# *            *
# **************

load_parameters();      


# **************  external-network ******************

if ($external_network) {
    $out = $out.  << "END";
      <external-network>
        <name>${extNW}</name>
END
	
	for (my $i=$start; $i<=$end; $i++) {
		$i = sprintf("%03d", $i);
		external_network($i);
	}

    $out = $out.  << "END";
      </external-network>
END
}	


# **************  static-nf-instance-data ******************

if ($static_nf) {
	
	for (my $i=$start; $i<=$end; $i++) {
		$i = sprintf("%03d", $i);
		static_nf_instance_data($i);
	}

}	



# **************  nf-pool ******************

if ($nf_pool) {
	
	for (my $i=$start; $i<=$end; $i++) {
		$i = sprintf("%03d", $i);
		nf_pool($i);
	}

}	


# **************  routing-rule ******************

if ($routing_rule) {
    $out = $out.  << "END";
      <routing-case>
        <name>${routing_case_int}</name>
END
	
	for (my $i=$start; $i<=$end; $i++) {
		$i = sprintf("%03d", $i);
		routing_rule($i);
	}

    $out = $out.  << "END";
      </routing-case>
END

}	

print << "EOF";
 <hello xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
   <capabilities>
     <capability>urn:ietf:params:netconf:base:1.0</capability>
   </capabilities>
 </hello>
EOF

print "]]>]]>\n\n";

print << "EOF";
 <?xml version="1.0"?>
 <rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:ns2="urn:com:ericsson:ecim:1.0" xmlns:ns3="http://tail-f.com/ns/netconf/actions/1.0" xmlns:ns4="urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring" xmlns:ns5="urn:ietf:params:xml:ns:netconf:notification:1.0" message-id="0">
   <edit-config>
     <target>
       <running/>
     </target>
     <config>
       <sepp-function xmlns="urn:rdns:com:ericsson:oammodel:ericsson-sepp">
  	     <nf-instance xmlns="urn:rdns:com:ericsson:oammodel:ericsson-sepp">
           <name>${nfinstance}</name>
EOF


print "$out\n";


print << "EOF";
         </nf-instance>
       </sepp-function>
     </config>
   </edit-config>
 </rpc>
EOF

print "]]>]]>\n";

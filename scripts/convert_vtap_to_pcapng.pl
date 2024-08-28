#!/usr/bin/perl

################################################################################
# $Revision: 1.0 $                                                             #
#   $Author: EEDCSI $                                                          #
#     $Date: 2021-07-21 $                                                      #
################################################################################
#                                                                              #
# (C) COPYRIGHT ERICSSON GMBH 2021                                             #
#                                                                              #
# The copyright to the computer program(s) herein is the property of           #
# Ericsson GmbH, Germany.                                                      #
#                                                                              #
# The program(s) may be used and/or copied only with the written permission    #
# of Ericsson GmbH in accordance with the terms and conditions stipulated in   #
# the agreement/contract under which the program(s) have been supplied.        #
#                                                                              #
################################################################################
#
# v1.0     : Initial version
#



use MIME::Base64;
use strict;
use Getopt::Std;

our ($opt_h, $opt_m, $opt_f, $opt_o, $opt_r, $opt_d, $opt_n);
&getopts("hdnmf:o:r:")||exit 1;

my $inputfile = $opt_f;
my $outputdir = $opt_o;
my $replace_port = $opt_r;

my $encoded;
my $decoded;
my $direction = "";
my $local_address_ip = "127.0.0.1";
my $local_address_port = 1234;
my $remote_address_ip = "";
my $remote_address_port = 0;
my $trace_id = 0;
my $filehandle = "";
my $hex = "";
my $command = "";
my $outfile = "";
my $date = "";
my $timestamp =  "";

my %trace;

#my $in_socket_streamed_trace_segment = 0;
my $in_connection = 0;
my $in_local_address = 0;
my $in_remote_address = 0;
my $in_read_event = 0;
my $in_write_event = 0;
my $in_closed = 0;


if ($opt_h) {
	# print help here: wget http://localhost:9901/tap -O - --post-file vtag_ingress_tap_serviceAddress1_tls.json | ~/convert_vtap_to_pcapng.pl -f - -o out -m
	
    print <<EOF;

Description:
============

This script converts Envoy tap output (in json format) into Wireshark pcapng output.
Message timestamps, direction, ports and IP addresses will be converted also.


Syntax:
=======

 $0 [<OPTIONAL>][<MANDATORY>]

    <MANDATORY> are one or more of the following parameters:

    -f    Input file name. For STDIN, use "-"


    <OPTIONAL> are one or more of the following parameters:

    -h    Shows this help
    -o    Output directory, if not existing, the script will create it. 
          If parameter is not given, output will be written in current directory
    -m 	  Merge output pcapnp files into one file. 
          The file name will contain the timestamp of first message
    -r    Replace port 443 with another port of all messages.
          Might make it easier to view the output in Wireshark



Examples:
=========

  Example 1:
  ----------
  Convert file 'tap' in this directory to one or more pcapng files and convert port 443 if present to port 80.
  Use "control-c" to close the conversion.

   $0 -f tap -r 80 

  Example 2:
  ----------
  Get the trace directly from Envoy (e.g. scp-worker) and convert to one pcapng file in directory "out"

  wget http://localhost:9901/tap -O - --post-file vtap_config_id.json | $0 -f - -o out -m 

EOF
	exit;
}

if ($outputdir !~ /^\//) {
	my $pwd = `pwd`;
	chomp $pwd;
	$outputdir = $pwd."/".$outputdir;
}


unless(-e $outputdir or mkdir $outputdir) {
    die "Unable to create $outputdir\n";
}


$SIG{INT} = \&postprocess;

sub postprocess {
    $SIG{INT} = \&postprocess;           # See "Writing A Signal Handler"
}

sub hexdump($)
{
    my $offset = 0;
	my $output = "" ;
        
    foreach my $chunk (unpack "(a16)*", $_[0])
    {
        my $hex = unpack "H*", $chunk; # hexadecimal magic
        $chunk =~ tr/ -~/./c;          # replace unprintables
        $hex   =~ s/(.{1,2})/$1 /gs;   # insert spaces
        $output .= sprintf("%08x  %-*s %s\n", $offset, 36, $hex, $chunk);
        $offset += 16;
    }
	return $output;
}

open(FILE,$inputfile) || (die "ERROR: cannot open $inputfile\n");

# Loop to read the input file starts here
while(<FILE>) {
	next if (/^\#/);    #ignore comments
 	
	if (/\"trace_id\": \"(\d+)\"/) {
		$trace_id = $1;
		$in_connection = 0;
		$in_local_address = 0;
		$in_remote_address = 0;
		$in_read_event = 0;
		$in_write_event = 0;
		$in_closed = 0;		
		$trace{$trace_id}{file_open} = 0;
		
		#set default values for remote_address since it might not be present in tap output.
		#might be overwritten later by real values
		unless (exists $trace{$trace_id}{remote_address_ip} ) {
			if ($trace{$trace_id}{local_address_ip} =~ /:/) {
				$trace{$trace_id}{remote_address_ip} = "::2";				
			} else {
				$trace{$trace_id}{remote_address_ip} = "127.0.0.2";
			}
			$trace{$trace_id}{remote_address_port} = "5678";
		}
		
		#set default values for local_address since it might not be present in tap output.
		#might be overwritten later by real values
		unless (exists $trace{$trace_id}{local_address_ip} ) {
			if ($trace{$trace_id}{remote_address_ip} =~ /:/) {
				$trace{$trace_id}{local_address_ip} = "::1";				
			} else {
				$trace{$trace_id}{local_address_ip} = "127.0.0.1";
			}
			$trace{$trace_id}{local_address_port} = "1234";
		}
		
	} elsif (/\"connection\":/) {
		$in_connection = 1;
		$in_local_address = 0;
		$in_remote_address = 0;		
	} elsif ($in_connection && /\"local_address\":/) {
		$in_local_address = 1;
		$in_remote_address = 0;
	} elsif ($in_connection && /\"remote_address\":/) {
		$in_local_address = 0;
		$in_remote_address = 1;		
	} elsif ($in_local_address && /\"address\": \"(\S+)\"/) {
		$trace{$trace_id}{local_address_ip} = $1;
		print STDERR "local IP: $1 \n" if $opt_d;
	} elsif ($in_local_address && /\"port_value\": (\d+)/) {
		if ($1 == 443) { 
			if ($opt_r) {
				$trace{$trace_id}{local_address_port} = $replace_port;
			} else {
				$trace{$trace_id}{local_address_port} = $1;
			}
		} else {
			$trace{$trace_id}{local_address_port} = $1;
		}

		print STDERR "local port: $1 \n" if $opt_d;
	} elsif ($in_remote_address && /\"address\": \"(\S+)\"/) {
		$trace{$trace_id}{remote_address_ip} = $1;
		print STDERR "remote IP: $1 \n" if $opt_d;
						
	} elsif ($in_remote_address && /\"port_value\": (\d+)/) {
		if ($1 == 443) { 
			$trace{$trace_id}{remote_address_port} = $replace_port;
		} else {
			$trace{$trace_id}{remote_address_port} = $1;
		}		
		#print  "remote port: $1 \n";	
	}

	if (/\"read\":/) {
		$in_read_event = 1;
		$in_write_event = 0;	
		$in_closed = 0;		
	} elsif (/\"write\":/) {
		$in_write_event = 1;				
		$in_read_event = 0;
		$in_closed = 0;
	} elsif (/\"closed\":/) {
		$in_closed = 1;
		$in_write_event = 0;				
		$in_read_event = 0;		
	}
	
	
	if (/\"as_bytes\": \"(\S+)\"/) {
		$encoded=$1;
		$decoded = decode_base64($encoded);
		$hex =  hexdump($decoded);

	}
	
	if (! $in_closed  && /\"timestamp\": \"(\S+)\"/) {

	    unless($timestamp) {  # store the frist timestamp for merge file, if required
			$timestamp = $1;
			$timestamp =~ s/:/_/g;
		}
		$outfile = "$outputdir/${trace_id}_hex.txt";
		$date = $1;
		$date =~ s/T/ /;
		$date =~ s/Z/ /;

		unless ($trace{$trace_id}{file_open}) {
			#print STDERR "opening 'text2pcap -i 4 -4 ${trace{$trace_id}{local_address_ip}},${trace{$trace_id}{remote_address_ip}}  -T ${trace{$trace_id}{local_address_port}},${trace{$trace_id}{remote_address_port}}  -n - ~/${trace_id}_out.pcapng' \n";
			open($trace{$trace_id}{filehandle}, ">>",  $outfile ) || die "can't fork: $!";  # add -q for quite
			
			#open($trace{$trace_id}{filehandle}, "|  text2pcap -i 4 -4 ${trace{$trace_id}{local_address_ip}},${trace{$trace_id}{remote_address_ip}}  -T ${trace{$trace_id}{local_address_port}},${trace{$trace_id}{remote_address_port}}  -n - /home/eedcsi/${trace_id}_out.pcapng") || die "can't fork: $!";  # add -q for quite
			$trace{$trace_id}{file_open} = 1;
		}
		
		$filehandle = $trace{$trace_id}{filehandle};
		
		#print STDERR "Writing to /home/eedcsi/${trace_id}_hex.txt\n";
		
		#		  -D                     the text before the packet starts with an I or an O,
		if ($in_read_event) {
			print $filehandle "O\n";
		} elsif ($in_write_event) {
			print $filehandle "I\n";
			
		}
		
		print $filehandle "$date\n";
		
		#    "timestamp": "2021-07-21T09:26:06.841705280Z"
		#  -t "\%Y-\%M-\%DT\%H:\%M:\%S."
		#  -t <timefmt>           treat the text before the packet as a date/time code;
		#                         the specified argument is a format string of the sort
		#                         supported by strptime.
		#                         Example: The time "10:15:14.5476" has the format code
		#                         "%H:%M:%S."
		#                         NOTE: The subsecond component delimiter, '.', must be
		#                         given, but no pattern is required; the remaining
		#                         number is assumed to be fractions of a second.
		#                         NOTE: Date/time fields from the current date/time are
		#	  				      used as the default for unspecified fields.


		

		print $filehandle $hex;
				
	}

	
}
foreach $trace_id (keys %trace) {
	
	$filehandle = $trace{$trace_id}{filehandle};

	close($filehandle) if ($filehandle);
		
	if (-e $outputdir."/".$trace_id."_hex.txt" ) {
		
		if ($trace{$trace_id}{remote_address_ip} =~ /\./) {
			$command = "text2pcap -i 4 -4 ${trace{$trace_id}{local_address_ip}},${trace{$trace_id}{remote_address_ip}} -t \"%Y-%m-%d %H:%M:%S.\" -T ${trace{$trace_id}{local_address_port}},${trace{$trace_id}{remote_address_port}} -q -D -n $outputdir/${trace_id}_hex.txt $outputdir/${trace_id}.pcapng";
		} elsif ($trace{$trace_id}{remote_address_ip} =~ /:/) {
			$command = "text2pcap -i 6 -6 ${trace{$trace_id}{local_address_ip}},${trace{$trace_id}{remote_address_ip}} -t \"%Y-%m-%d %H:%M:%S.\" -T ${trace{$trace_id}{local_address_port}},${trace{$trace_id}{remote_address_port}} -q -D -n $outputdir/${trace_id}_hex.txt $outputdir/${trace_id}.pcapng";
		} else {
			my $error = $trace{$trace_id}{remote_address_ip};
			
			print STDERR "Something went wrong, remote_address_ip ($error) cannot be determined\n";
		}
		
		system($command);
		
		system("rm -rf $outputdir/${trace_id}_hex.txt") unless $opt_n;
	}
}

#merge all files if required
if ($opt_m) {
	$command = "mergecap -w $outputdir/${timestamp}_merged.pcapng $outputdir/*pcapng";
	system($command);
	
	print STDERR "\n\nEnvoy trace converted to one pcap file: $outputdir/${timestamp}_merged.pcapng\n\n";

} else {
	print STDERR "\n\nEnvoy trace converted to several pcap files in directory: $outputdir\n\n";

}


close(FILE);


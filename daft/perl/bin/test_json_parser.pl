#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.2
#  Date     : 2021-10-12 14:06:12
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

use General::Data_Structure_Operations;
use General::Json_Operations;

my $data_only = 0;
my $input_file = "";
my $json_data;
my $output_file = "";
my $print_data = 0;
my $type;
my @set_params = ();
my $show_help = 0;
my $use_data_dumper = 0;

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "d|data-only"               => \$data_only,
    "h|help"                    => \$show_help,
    "i|input-file=s"            => \$input_file,
    "o|output-file=s"           => \$output_file,
    "p|print-data"              => \$print_data,
    "s|set-param=s"             => \@set_params,
    "use-data-dumper"           => \$use_data_dumper,
);

if ($show_help) {
    show_help();
    exit 0;
}

if ($use_data_dumper) {
    use Data::Dumper;
}

if ($input_file eq "") {
    print "Please specify a JSON filename\n";
    exit 1;
}

# Read input JSON file into Perl hash
print "Reading JSON input file '$input_file'\n\n" if ($data_only == 0);
$json_data = General::Json_Operations::read_file_return_reference({ "filename" => $input_file });
unless (defined $json_data) {
    print "Problem to read JSON file '$input_file'\n";
    exit 1;
}

unless (ref($json_data) =~ /^(ARRAY|HASH)$/) {
    print "Unknown data type detected, only ARRAY and HASH types are supported\n";
    exit 1;
}

if ($use_data_dumper) {
    print "-"x80,"\nData:Dumper output before any updates:\n";
    $Data::Dumper::Sortkeys = 1;
    print Dumper($json_data);
}

if (scalar @set_params > 0) {
    print "Creating or Updating values\n\n";
    # Add or update the hash value
    my $rc = General::Data_Structure_Operations::update_hash_values(
        {
            "output-ref"    => $json_data,
            "values"        => \@set_params,
        }
    );
    if ($rc != 0) {
        print "Failed to update the hash with new values";
        exit 1;
    }
}

if ($use_data_dumper) {
    print "-"x80,"\nData:Dumper output after any updates:\n";
    $Data::Dumper::Sortkeys = 1;
    print Dumper($json_data);
}

if ($print_data) {
    if ($data_only == 0) {
        if (ref($json_data) eq "HASH") {
            # HASH Reference
            print "\nTop level keys:\n===============\n";
            for my $key2 (sort keys %$json_data) {
                print "Key=$key2\n";
            }
        } else {
            # ARRAY Reference
            my $index = 0;
            print "\nTop level indexes:\n==================\n";
            for my $data (@$json_data) {
                print "\nIndex=$index\n";
                if (ref($data) eq "HASH") {
                    print "\n  Second level keys:\n  ==================\n";
                    for my $key2 (sort keys %{@$json_data[$index]}) {
                        print "  Key=$key2\n";
                    }
                } elsif (ref($data) eq "ARRAY") {
                    print "Not supported data type array of arrays\n";
                    exit 1;
                }
                $index++;
            }
        }

        print "\nParsed hash:\n============\n";
    }

    if (ref($json_data) =~ /^(ARRAY|HASH)$/) {
        General::Data_Structure_Operations::dump_dot_separated_ref_to_stdout($json_data, "");
    } else {
        print "Unknown data type detected, only ARRAY and HASH types are supported\n";
        exit 1;
    }
}

if ($output_file ne "") {
    print "Writing JSON output file '$output_file'\n\n";
    if (General::Json_Operations::write_file({ "filename" => $output_file, "output-ref" => $json_data }) != 0) {
        print "Problem to write JSON file '$output_file'\n";
        exit 1;
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

This script parse a JSON file and prints data to screen as a dot-separated list
and also outputs a copy to another file if wanted.
This script is mainly used to test the Perl module JSON::PP.


Syntax:
=======

$0 [<OPTIONAL>] <MANDATORY>

<MANDATORY> are all the following parameters:

  -i <file path>        | --input-file <file path>

<OPTIONAL> are one or more of the following parameters:

  -d                    | --data-only
  -h                    | --help
  -o <file path>        | --output-file <file path>
  -p                    | --print-data
  -s <value>            | --set-params <value>
                          --use-data-dumper

where:

  --data-only
  -d
  -----------
  When used together with the -p / --print-data it will only printed the
  parsed data and nothing extra.


  --input-file <file path>
  -i <file path>
  ------------------------
  The input JSON file to read.


  -h
  --help
  ------
  Shows this help information.


  --output-file <file path>
  -o <file path>
  -------------------------
  The file to write the parsed and potentially modified JSON data to.


  --print-data
  -p
  ------------
  Print the parsed data to the screen.


  --set-params <value>
  -s <value>
  --------------------
  Update or add parameters to the parsed data that will if wanted be
  written back to file.
  The parameter can be specified multiple times if more than one
  parameter should be updated or added.
  Example:
  -s global.ericsson.scp.enabled=true


  --use-data-dumper
  -----------------
  Print Perl internal data structures of the parsed JSON data before and
  after modifying the data using the Data::Dumper module.
EOF
}

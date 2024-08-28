#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.23
#  Date     : 2023-10-20 15:30:06
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2023
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

# ********************************
# *                              *
# * External module declarations *
# *                              *
# ********************************

use strict;
use warnings;
use File::Basename qw(dirname basename);
use Cwd qw(abs_path cwd);

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $created_for_kind = "-";
    # Indicates what kind the secret is created for and indirectly what SIP-TLS API is being used.
    # Known kind values:
    #   - CertificateAuthority
    #       Using Old API.
    #   - ClientCertificate
    #       Using Old API.
    #   - InternalCertificate
    #       Using New API.
    #   - InternalUserCA
    #       Using New API.
    #   - ServerCertificate
    #       Using Old API.
    # The New API have an upper limit of "eric-sec-sip-tls.internalCertificate.validLifetimeSeconds=315576000"
    # which is about 10 years, where the Old API the value can be even higher.
    # But >80% of certificates are using the New API.
my @directories = ();
my $error_cnt = 0;
my @exclude_pattern = ();
my @filedata = ();
my $filename;
my @files = ();
my $first_not_after = "";
my $first_not_after_file = "";
my $first_not_before = "";
my $first_not_before_file = "";
my @include_pattern = ();
my @input_files = ();
my $kubeconfig = "";
my $last_not_after = "";
my $last_not_after_file = "";
my $last_not_before = "";
my $last_not_before_file = "";
my $namespace = "";
my $output_csv_data = 0;
my $output_tsv_data = 0;
my $output_file = "";
my $print_all = 0;
my $print_simple_table = 0;
my $search_directory = "";
my $secret_name = "";
my $show_help = 0;
my $sort_by = "";
my %sorted_hash;

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "b|sort-by=s"               => \$sort_by,
    "c|output-csv-data"         => \$output_csv_data,
    "d|directory=s"             => \@directories,
    "e|exclude=s"               => \@exclude_pattern,
    "f|file=s"                  => \@input_files,
    "h|help"                    => \$show_help,
    "i|include=s"               => \@include_pattern,
    "kubeconfig=s"              => \$kubeconfig,
    "m|print-simple-table"      => \$print_simple_table,
    "n|namespace=s"             => \$namespace,
    "o|output-file=s"           => \$output_file,
    "p|print-all"               => \$print_all,
    "r|secret-name=s"           => \$secret_name,
    "s|search-directory=s"      => \$search_directory,
    "t|output-tsv-data"         => \$output_tsv_data,
);

if ($output_csv_data == 1 && $output_tsv_data == 1) {
    print "You can only specify one of -c/--output-csv-data or -t/--output-tsv-data, not both\n";
    exit 1;
}

if ($sort_by eq "name") {
    # This is anyway the default sort order, just unset this variable to make
    # the output go faster.
    $sort_by = "";
} elsif ($sort_by ne "" && $sort_by !~ /^(kind|not-before|not-after)$/) {
    print "Parameter -b/--sort-by can only have values kind, name, not-before or not-after\n";
    exit 1;
}

if ($kubeconfig ne "" && -f "$kubeconfig") {
    $kubeconfig = " --kubeconfig=$kubeconfig";
} elsif ($kubeconfig ne "") {
    print "The kubeconfig file does not exist\n";
    exit 1;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

if ($show_help) {
    usage();
    exit 0;
}

if ($namespace eq "") {
    check_for_files();
}

if ($output_file) {
    unless (open OUTF, ">>$output_file") {
        print "Error opening output file '$output_file'\n";
        exit 1;
    }
}

if ($namespace eq "") {
    parse_input_files();
} else {
    parse_kubernetes_secrets();
}

if ($output_file) {
    close OUTF;
}

exit 0;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub check_for_files {
    # Check if a directory should be searched
    if ($search_directory) {
        if (-d "$search_directory") {
            $search_directory = abs_path $search_directory;
            my @found_files  = `find $search_directory -name '*.crt'`;
            push @found_files, `find $search_directory -name '*.pem'`;
            for (@found_files) {
                s/[\r\n]//g;
                push @files, $_;
            }
        } else {
            print "No such directory '$search_directory'\n";
            exit 1;
        }
    }
    # Check if files are specified
    if (@input_files) {
        for $filename (@input_files) {
            if (-f "$filename") {
                push @files, abs_path $filename;
            }
        }
    }
    # Check if directories are specified
    if (@directories) {
        for my $directory (@directories) {
            if (-d "$directory") {
                for $filename (<"$directory/*.crt">) {
                    push @files, abs_path $filename;
                }
                for $filename (<"$directory/*.pem">) {
                    push @files, abs_path $filename;
                }
            }
        }
    }
    # If no files already specified then check for files in current directory
    unless (@files) {
        for $filename (<*.crt>) {
            push @files, abs_path $filename;
        }
        for $filename (<*.pem>) {
            push @files, abs_path $filename;
        }
    }
    if (scalar @files == 0) {
        print "No files specified and no .crt files in current directory\n";
        exit 1;
    }

    # Sort the files
    @files = sort @files;
}

# -----------------------------------------------------------------------------
sub decode_base64_data {
    my $encoded = shift;

    my $decoded;
    my @decoded = ();

    $decoded = `echo $encoded | base64 -d`;
    $decoded =~ s/\n/\\n/mg;
    unless ($decoded =~ /^.+\\n$/) {
        $decoded = "$decoded\\n";
    }

    # In case there was nothing decoded just return an empty array
    return ("NOT A VALID CERTIFICATE") if ($decoded eq '\n');

    @decoded = split /-----END CERTIFICATE-----\\n/, $decoded;

    # Add back the -----END CERTIFICATE-----\n line
    for (@decoded) {
        $_ = "$_-----END CERTIFICATE-----\\n";
    }

    return @decoded;
}

# -----------------------------------------------------------------------------
sub decode_certificate_data {
    my $name = shift;
    my $certificate_array_ref = shift;

    my $certname;
    my $count = 1;
    my @data = ();
    my $issuer;
    my $line;
    my $max_count = scalar @$certificate_array_ref;
    my %month_to_number = (
        "Jan" => "01",
        "Feb" => "02",
        "Mar" => "03",
        "Apr" => "04",
        "May" => "05",
        "Jun" => "06",
        "Jul" => "07",
        "Aug" => "08",
        "Sep" => "09",
        "Oct" => "10",
        "Nov" => "11",
        "Dec" => "12",
    );
    my $not_after;
    my $not_before;
    my $subject;

    # For each certificate decode it into a human readable format
    for my $certificate (@$certificate_array_ref) {
        if ($certificate !~ /(NOT A VALID CERTIFICATE|BEGIN X509 CRL|BEGIN RSA PRIVATE KEY)/) {
            @data = `echo -en "$certificate" | openssl x509 -text -noout -in /dev/stdin`;
            if ($? != 0) {
                print "Error decoding certificate data for $name ($count of $max_count)\ncertificate=$certificate\n";
                $error_cnt++;
                next;
            }
        } else {
            # Not a valid certificate, just report it as an empty certificate with no date information
            @data = ();
        }

        # Now parse the decoded data looking for "Issuer:", "Not Before:", "Not After :" and "Subject:"
        $not_after = "";
        $not_before = "";
        $issuer = "-";
        $subject = "-";
        for my $line (@data) {
            print STDERR "     >>> Decoded >>> $line" if $print_all;

            $line =~ s/[\r\n]//g;

            if ($line =~ /^\s*Not Before\s*:\s*(\S{3})\s+(\d+)\s+(\d{2}:\d{2}:\d{2})\s+(\d{4})\s+(\S+)/) {
                # Not Before: Jul  1 10:35:16 2019 GMT
                $not_before = sprintf "%04d-%02d-%02d %s %s", $4, $month_to_number{$1}, $2, $3, $5;
            } elsif ($line =~ /^\s*Not After\s*:\s*(\S{3})\s+(\d+)\s+(\d{2}:\d{2}:\d{2})\s+(\d{4})\s+(\S+)/) {
                # Not After : Feb 20 10:35:16 2044 GMT
                $not_after = sprintf "%04d-%02d-%02d %s %s", $4, $month_to_number{$1}, $2, $3, $5;
            } elsif ($line =~ /^\s+Issuer:\s*(.+)$/) {
                $issuer = $1;
                if ($output_csv_data == 1) {
                    $issuer =~ s/,//g;
                }
            } elsif ($line =~ /^\s+Subject:\s*(.+)$/) {
                $subject = $1;
                if ($output_csv_data == 1) {
                    $subject =~ s/,//g;
                }
            }
        }
        print STDERR "\n" if $print_all;

        if ($max_count == 1) {
            $certname = "$name";
        } else {
            $certname = "$name ($count of $max_count)";
        }

        if ($not_before ne "") {
            if ($first_not_before eq "") {
                $first_not_before = $not_before;
                $first_not_before_file = "$certname";
            } elsif ($first_not_before gt $not_before) {
                $first_not_before = $not_before;
                $first_not_before_file = "$certname";
            }
            if ($last_not_before eq "") {
                $last_not_before = $not_before;
                $last_not_before_file = "$certname";
            } elsif ($last_not_before lt $not_before) {
                $last_not_before = $not_before;
                $last_not_before_file = "$certname";
            }
        }

        if ($not_after ne "") {
            if ($first_not_after eq "") {
                $first_not_after = $not_after;
                $first_not_after_file = "$certname";
            } elsif ($first_not_after gt $not_after) {
                $first_not_after = $not_after;
                $first_not_after_file = "$certname";
            }
            if ($last_not_after eq "") {
                $last_not_after = $not_after;
                $last_not_after_file = "$certname";
            } elsif ($last_not_after lt $not_after) {
                $last_not_after = $not_after;
                $last_not_after_file = "$certname";
            }
        }

        if ($output_csv_data == 1) {
            $line = sprintf "'%s','%s','%s','%s','%s','%s'", $not_before ? $not_before : "-", $not_after ? $not_after : "-", $created_for_kind, $issuer, $subject, $certname;
        } elsif ($output_tsv_data == 1) {
            $line = sprintf "%s\t%s\t%s\t%s\t%s\t%s", $not_before ? $not_before : "-", $not_after ? $not_after : "-", $created_for_kind, $issuer, $subject, $certname;
        } elsif ($print_simple_table == 1) {
            $line = sprintf "%-25s  %-25s  %-20s  %s", $not_before ? $not_before : "-", $not_after ? $not_after : "-", $created_for_kind, $certname;
            if ($sort_by eq "not-before") {
                push @{$sorted_hash{$not_before}}, $line;
            } elsif ($sort_by eq "not-after") {
                push @{$sorted_hash{$not_after}}, $line;
            } elsif ($sort_by eq "kind") {
                push @{$sorted_hash{$created_for_kind}}, $line;
            }
        } else {
            $line = sprintf "\n$certname\n\n  Issuer           : $issuer\n  Subject          : $subject\n  Created for Kind : $created_for_kind\n  Not Before       : %s\n  Not After        : %s", $not_before ? $not_before : "-", $not_after ? $not_after : "-";
        }

        if ($sort_by eq "") {
            print_line($line);
            print STDERR "-"x40, "\n\n" if $print_all;
        }
        $count++;
    }
}

# -----------------------------------------------------------------------------
sub parse_input_files {
    my $line;

    if ($output_csv_data == 1) {
        $line = "'Not Before','Not After','Issuer','Subject','File'";
        print_line($line);
    } elsif ($output_tsv_data == 1) {
        $line = "Not Before\tNot After\tIssuer\tSubject\tFile";
        print_line($line);
    } elsif ($print_simple_table == 1) {
        $line = sprintf "%-25s  %-25s  %-20s  %s", "Not Before", "Not After", "Created for Kind", "File";
        print_line($line);
    }

    if ($output_csv_data == 0 && $output_tsv_data == 0 && $print_simple_table == 1) {
        $line = sprintf "%-25s  %-25s  %-20s  %s", "-"x25, "-"x25, "-"x20, "-"x4;
        print_line($line);
    }

    for $filename (@files) {
        @filedata = `cat $filename 2>/dev/null`;
        @filedata = pack_certificate_data(\@filedata);

        decode_certificate_data($filename, \@filedata);
    }

    if ($sort_by ne "") {
        # we need to print the data now instead of inside of the decode_certificate_data sub routine
        for my $key (sort keys %sorted_hash) {
            for (@{$sorted_hash{$key}}) {
                print_line($_);
            }
        }
    }

    print_line("");
    if ($output_csv_data == 1) {
        $line = "'First Not Before','$first_not_before','-','-','$first_not_before_file'";
        print_line($line);
        $line = "'Last Not Before','$last_not_before','-','-','$last_not_before_file'";
        print_line($line);
        $line = "'First Not After','$first_not_after','-','-','$first_not_after_file'";
        print_line($line);
        $line = "'Last Not After','$last_not_after','-','-','$last_not_after_file'";
        print_line($line);
    } elsif ($output_tsv_data == 1) {
        $line = "First Not Before\t$first_not_before\t-\t-\t$first_not_before_file";
        print_line($line);
        $line = "Last Not Before\t$last_not_before\t-\t-\t$last_not_before_file";
        print_line($line);
        $line = "First Not After\t$first_not_after\t-\t-\t$first_not_after_file";
        print_line($line);
        $line = "Last Not After\t$last_not_after\t-\t-\t$last_not_after_file";
        print_line($line);
    } else {
        $line = sprintf "First Not Before  %-25s  %s", $first_not_before, $first_not_before_file;
        print_line($line);
        $line = sprintf "Last Not Before   %-25s  %s", $last_not_before, $last_not_before_file;
        print_line($line);
        $line = sprintf "First Not After   %-25s  %s", $first_not_after, $first_not_after_file;
        print_line($line);
        $line = sprintf "Last Not After    %-25s  %s", $last_not_after, $last_not_after_file;
        print_line($line);
    }
}

# -----------------------------------------------------------------------------
sub pack_certificate_data {
    my $array_ref = shift;

    my $packed_line = "";
    my @packed = ();
    my $packing = 0;

    for (@$array_ref) {
        s/[\r\n]//g;
        if (/^-----(BEGIN CERTIFICATE|BEGIN TRUSTED CERTIFICATE)-----$/) {
            $packed_line = "$_\\n";
            $packing = 1;
        } elsif ($packing == 1) {
            $packed_line .= "$_\\n";
            if (/^-----(END CERTIFICATE|END TRUSTED CERTIFICATE)-----$/) {
                push @packed, $packed_line;
                $packed_line = "";
                $packing = 0;
            }
        }
    }

    return @packed;
}

# -----------------------------------------------------------------------------
sub parse_kubernetes_secrets {
    my $line;
    my @section_data = ();
    my $section_kind = "";
    my $section_name = "";
    my @yaml_secrets = ();

    # Get secrets for the namespace using:
    if ($secret_name eq "") {
        @yaml_secrets = `kubectl -n $namespace get secrets -o yaml $kubeconfig`;
    } else {
        @yaml_secrets = `kubectl -n $namespace get secrets $secret_name -o yaml $kubeconfig`;
    }
    if ($? != 0) {
        print "Failed to get secrets\n";
        exit 1;
    }

    if ($output_csv_data == 1) {
        $line = "'Not Before','Not After','Created for Kind','Issuer','Subject','Secret Name and (Certificate File)'";
        print_line($line);
    } elsif ($output_tsv_data == 1) {
        $line = "Not Before\tNot After\tCreated for Kind\tIssuer\tSubject\tSecret Name and (Certificate File)";
        print_line($line);
    } elsif ($print_simple_table == 1) {
        $line = sprintf "%-25s  %-25s  %-20s  %s", "Not Before", "Not After", "Created for Kind", "Secret Name and (Certificate File)";
        print_line($line);
    }

    if ($output_csv_data == 0 && $output_tsv_data == 0 && $print_simple_table == 1) {
        $line = sprintf "%-25s  %-25s  %-20s  %s", "-"x25, "-"x25, "-"x20, "-"x34;
        print_line($line);
    }

    # Parse the yaml data and look for "name: ", ".crt: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t" or ".pem: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t"
    for my $line (@yaml_secrets) {
        $line =~ s/[\r\n]//g;

        if ($line =~ /^- apiVersion: \S+\s*$/) {
            # Start of new section
            if (@section_data) {
                # We have already collected the data from a previous section, now process it
                parse_kubernetes_secrets_section_data($section_kind, $section_name, \@section_data);
            }
            @section_data = ();
            $section_kind = "";
            $section_name = "";
            $created_for_kind = "-";
            push @section_data, $line;
            next;
        } else {
            push @section_data, $line;
            if ($line =~ /^  kind:\s+(\S+)/) {
                $section_kind = $1;
                next;
            } elsif ($line =~ /^\s{2,4}name:\s+(\S+)/ && $section_name eq "") {
                $section_name = $1;
                next;
            } elsif ($line =~ /^\s{6}com\.ericsson\.sec\.tls\/created-for-kind:\s+(\S+)/ && $created_for_kind eq "-") {
                $created_for_kind = $1;
                next;
            }
            next;
        }
    }
    if (@section_data) {
        # We need to process the data from the last section
        parse_kubernetes_secrets_section_data($section_kind, $section_name, \@section_data);
    }

    if ($sort_by ne "") {
        # we need to print the data now instead of inside of the decode_certificate_data sub routine
        for my $key (sort keys %sorted_hash) {
            for (@{$sorted_hash{$key}}) {
                print_line($_);
            }
        }
    }

    print_line("");
    if ($output_csv_data == 1) {
        $line = "'First Not Before','$first_not_before','-','-','$first_not_before_file'";
        print_line($line);
        $line = "'Last Not Before','$last_not_before','-','-','$last_not_before_file'";
        print_line($line);
        $line = "'First Not After','$first_not_after','-','-','$first_not_after_file'";
        print_line($line);
        $line = "'Last Not After','$last_not_after','-','-','$last_not_after_file'";
        print_line($line);
    } elsif ($output_tsv_data == 1) {
        $line = "First Not Before\t$first_not_before\t-\t-\t$first_not_before_file";
        print_line($line);
        $line = "Last Not Before\t$last_not_before\t-\t-\t$last_not_before_file";
        print_line($line);
        $line = "First Not After\t$first_not_after\t-\t-\t$first_not_after_file";
        print_line($line);
        $line = "Last Not After\t$last_not_after\t-\t-\t$last_not_after_file";
        print_line($line);
    } else {
        $line = sprintf "First Not Before  %-25s  %s", $first_not_before, $first_not_before_file;
        print_line($line);
        $line = sprintf "Last Not Before   %-25s  %s", $last_not_before, $last_not_before_file;
        print_line($line);
        $line = sprintf "First Not After   %-25s  %s", $first_not_after, $first_not_after_file;
        print_line($line);
        $line = sprintf "Last Not After    %-25s  %s", $last_not_after, $last_not_after_file;
        print_line($line);
    }
}

# -----------------------------------------------------------------------------
sub parse_kubernetes_secrets_section_data {
    my $kind = shift;
    my $name = shift;
    my $arrayref = shift;

    my $base64_data = "";
    my @base64_data_decoded;
    my $certfile;

    # Remove any ", ' or spaces at start and end of variable
    $kind =~ s/^[\s"']*(\S+?)[\s"']*$/$1/;
    $name =~ s/^[\s"']*(\S+?)[\s"']*$/$1/;

    # Check if this secret should be ignored
    if (@exclude_pattern) {
        for my $pattern (@exclude_pattern) {
            if ($name =~ /$pattern/) {
                # We should ignore this secret
                return;
            }
        }
    }

    # Check if the secret should be included
    if (@include_pattern) {
        my $found = 0;
        for my $pattern (@include_pattern) {
            if ($name =~ /$pattern/) {
                # We should include this secret
                $found = 1;
                last;
            }
        }
        if ($found == 0) {
            # Secret should not be inckluded
            return;
        }
    }

    #return unless ($kind =~ /^(Deployment|DaemonSet|StatefulSet|CronJob|Job)$/i);

    print STDERR "="x80, "\n" if $print_all;

    # Convert data into dot separated lines of data
    for (@{$arrayref}) {
        print STDERR "$_\n" if $print_all;

        if (/^\s+(\S+\.(crt|pem)):\s+(LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t\S+)\s*$/) {
            $certfile = $1;
            $base64_data = $3;

            # Decode the base64 encoded data into an array because each encoded string
            # can contain multiple certificates.
            @base64_data_decoded = decode_base64_data($base64_data);

            decode_certificate_data("$name ($certfile)", \@base64_data_decoded);
        } elsif (/^\s+(\S+\.(crt|pem)):\s+(\S+)\s*$/) {
            $certfile = $1;
            $base64_data = $3;

            next if ($base64_data eq "{}");

            # Decode the base64 encoded data into an array because each encoded string
            # can contain multiple certificates.
            @base64_data_decoded = decode_base64_data($base64_data);

            if (@base64_data_decoded > 0 && $base64_data_decoded[0] =~ /^-----BEGIN(\s+|\s+EC\s+)PRIVATE KEY-----/) {
                # Not a valid certificate but a key, empty the array
                @base64_data_decoded = ();
            }

            decode_certificate_data("$name ($certfile)", \@base64_data_decoded);
        }
    }
}

# -----------------------------------------------------------------------------
sub print_line {
    my $line = shift;

    if ($output_file) {
        print OUTF "$line\n";
    } else {
        print "$line\n";
    }
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This script is used for parsing certificate (.crt) files and displaying the
validity dates for the certificates.
The tool is using the following command to decode the CRT files:

openssl x509 -text -noout -in <filename>

It takes specific files, directories or even a directory search path and looks
for all .crt files.
It can also decode certificates stored in secrets for a namespace directly from
kubernetes.


Syntax:
=======

$0 [<OPTIONAL>]

<MANDATORY> are one or more of the following parameters:

  -d <directory path>   | --directory <directory path>
  -f <file path>        | --file <file path>
  -s <directory path>   | --search-directory <directory path>

  or one of the following parameters:

  -n <namespace>        | --namespace <namespace>

<OPTIONAL> are one or more of the following parameters:

  -b <sort-order>       | --sort-by <sort-order>
  -c                    | --output-csv-data
  -e <regexp>           | --exclude <regexp>
  -i <regexp>           | --include <regexp>
  -h                    | --help
                          --kubeconfig <filename>
  -m                    | --print-simple-table
  -o <filename>         | --output-file <filename>
  -p                    | --print-all
  -r <secret name>      | --secret-name <secret name>
  -t                    | --output-tsv-data

where:

  --sort-by <sort-order>
  -b <sort-order>
  ----------------------
  If specified and together with the --print-simple-table parameter then the
  output will be sorted by either the "Not Before", "Not After",
  "Created for Kind" or "Secret Name and (Certificate File)" columns.
  The allowed values for <sort-order> can be either:
    - not-before
    - not-after
    - kind
    - name

  If not specified which is also the default, then the output will be sorted
  by the "Secret Name and (Certificate File)" column.


  --output-csv-data
  -c
  -----------------
  If specified then the output will be comma separated instead of printed in
  ASCII table format.


  --directory <directory path>
  -d <directory path>
  ----------------------------
  If specified then all files with .crt or .pem extension will be included
  in list of files to parse.
  This parameter can be specified multiple times.


  --exclude <regexp>
  -e <regexp>
  ------------------
  If specified then any secret name matching the <regexp> will be ignored and
  not printed.
  This parameter can be specified multiple times.

  Note:
  --exclude has higher priority than --include, meaning if a name match both
  --include and --exclude patterns then the secret will not be printed.


  --file <file path>
  -f <file path>
  ----------------------------
  If specified then the file will be included in list of files to parse.
  This parameter can be specified multiple times.


  -h
  --help
  ------
  Shows this help information.


  --include <regexp>
  -i <regexp>
  ------------------
  If specified then any secret name matching the <regexp> will be included and
  printed, which means that anything not matching will not be printed.
  This parameter can be specified multiple times.

  Note:
  --exclude has higher priority than --include, meaning if a name match both
  --include and --exclude patterns then the secret will not be printed.


  --kubeconfig <filename>
  -----------------------
  If specified then it will use this file as the config file for helm and
  kubectl commands.
  If not specified then it will use the default file which is normally the
  file ~/.kube/config.


  --print-simple-table
  -m
  --------------------
  If specified it will print the output as a simple table instead of a more
  detailed list of data which is the default. It only prints the following
  columns:
    - Not Before
    - Not After
    - Secret Name and (Certificate File)


  --namespace <namespace>
  -n <namespace>
  -----------------------
  If specified then certificate data will be obtained and decoded from
  'kubectl get secrets -n <namespace>' command on the deployed system.


  --output-file <filename>
  -o <filename>
  ------------------------
  If specified then the output will be written to the specified file instead
  of to STDOUT if parameter has not been specified.


  --print-all
  -p
  -----------
  If specified it will also print all the secret data including decoded
  certificates to STDERR.


  --secret-name <secret name>
  -r <secret name>
  ---------------------------
  If specified and together with --namespace then certificate data will be
  obtained and decoded from one specific secret using the following command:
  'kubectl get secrets <secret name> -n <namespace>' command on the deployed system.


  --search-directory <directory path>
  -s <directory path>
  -----------------------------------
  If specified then the directory and all sub directories underneath will be
  searched for files with .crt or .pem extension will be included in list of
  files to parse.


  --output-tsv-data
  -t
  -----------------
  If specified then the output will be tab separated instead of printed in
  ASCII table format.


Examples:
=========

Show certificate validity data for all secrets in a namespace.
$0 \\
    --namespace eiffelesc

Show certificate validity data for a specific secret in a namespace.
$0 \\
    --namespace eiffelesc \\
    --secret-name eric-tm-ingress-controller-cr-contour-emergency

Save certificate validity data for all secrets in a namespace to a tab separated value
file and display all decoded secret data to the screen (STDERR).
$0 \\
    --namespace eiffelesc \\
    --output-file certificate_data.tsv \\
    --output-tsv-data \\
    --print-all

Show certificate validity data for client and cql secrets in a namespace except for
CA and emergency secrets.
$0 \\
    --namespace eiffelesc \\
    --include '(client|cql)' \\
    --exclude '(-ca-|-ca\$|emergency)'

EOF
}

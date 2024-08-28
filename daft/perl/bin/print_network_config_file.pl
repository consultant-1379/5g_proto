#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.2
#  Date     : 2023-06-21 17:19:47
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
my @old_data = ();
my $old_file = "";
my %old_tags;
my $output_file = "";
my $output_format = "";
my %remove_attribute;
my $script_path = abs_path($0);
my $show_help = 0;
my $strip_comments = 0;
my $strip_empty_lines = 0;
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
    "a|remove-attributes=s"  => sub {
                                        if ($_[1] =~ /^(\S+)?=(.+)/) {
                                            $remove_attribute{$1} = $2;
                                        } elsif ($_[1] =~ /^(\S+)/) {
                                            $remove_attribute{$1} = "";
                                        }
                                    },
    "d|debug=i"              => \$debug,
    "g|generate-param-help"  => \$generate_param_help,
    "f|old-file=s"           => \$old_file,
    "o|output-file=s"        => \$output_file,
    "c|strip-comments"       => \$strip_comments,
    "e|strip-empty-lines"    => \$strip_empty_lines,
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

if (%remove_attribute) {
    # Force removal of comments and empty lines
    $strip_empty_lines = 1;
    $strip_comments = 1;
}

if ($old_file) {
    # Read old network configuration file, always strip away comments
    read_file($old_file, \@old_data, $strip_comments);
    parse_tag_data(\@old_data, \%old_tags, $strip_comments);

    if ($debug > 0) {
        print "\nTags found in the file $old_file:\n\n";
        for my $key (sort keys %old_tags) {
            print "Tag=$key\n";
            for my $param (sort keys %{$old_tags{$key}}) {
                print "  $param=\"$old_tags{$key}{$param}\"\n";
            }
            print "\n";
        }
    }

    if (%remove_attribute) {
        remove_attributes(\@old_data, \%old_tags, \%remove_attribute);

        if ($debug > 0) {
            print "\nTags after removing attributes:\n\n";
            for my $key (sort keys %old_tags) {
                print "Tag=$key\n";
                for my $param (sort keys %{$old_tags{$key}}) {
                    print "  $param=\"$old_tags{$key}{$param}\"\n";
                }
                print "\n";
            }
        }
    }
} else {
    print "No old file specified, nothing to print\n";
    exit 0;
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
for (@old_data) {
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

# Exit with success
exit 0;

#
# Subroutines
# -----------
#

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
        print "  Parameter=\"$param\"\n  Value=\"$value\"\n\n" if ($debug > 2);
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
    print "Key=$key\n" if ($debug > 2);
    while ($line =~ /^\s*(\w+)="(.*?)"\s*/) {
        $param = $1;
        $value = $2;
        #$line =~ s/^$1//;
        $line =~ s/^\s*\w+=".*?"\s*//;
        print "  Parameter=\"$param\"\n  Value=\"$value\"\n\n" if ($debug > 2);
        if (exists $remove_attribute{$param}) {
            # Ignore this attribute
            next;
        }
        $hash_ref->{$key}{$param} = $value;
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

    my $pending_line = "";

    # Empty the output array
    @$array_ref_out = ();

    for (@$array_ref_in) {
        # Strip trailing spaces
        s/\s*$//;
        # Take next line if empty
        next if (/^\s*$/);

        if (/^.*?<.+>$/) {
            # The parameter is already on one line
            push @$array_ref_out, "$_";
            $pending_line = "";
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
            print "    Wanted: $_\n" if ($debug > 1);
            extract_parameters($2, $hash_ref);
        } else {
            # Not wanted parameters
            print "Not wanted: $_\n" if ($debug > 1);
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

    my @input = ();
    my @temp_array = ();

    if (open INF, "$filename") {
        while (<INF>) {
            s/[\r\n]//g;
            push @temp_array, $_;
        }
        close INF;
        if ($output_format eq "one-line") {
            strip_comment_lines(\@temp_array, \@input, $strip_comments);
            # Put one XML tag per line
            one_tag_per_line(\@input, $array_ref);
        } else {
            strip_comment_lines(\@temp_array, $array_ref, $strip_comments);
        }
    } else {
        print "ERROR: Unable to open the file '$filename'\n";
        exit 1;
    }
}

#
# Update the array reference and hash reference and remove the attributes
# that should no longer be printed.
#
# Input:
#  - Reference to array where data is stored
#  - Reference to hash where data is stored
#  - Reference to hash with attributes to remove.
#
# Output:
#  - The array and hash references are updated with removed attributes.
#
sub remove_attributes {
    my $array_ref = shift;
    my $hash_ref = shift;
    my $remove_ref = shift;

    my @delete_keys = ();

    # First remove attributes from the array since it contains one parameter per line it's easy to just remove them
    for (@$array_ref) {
        for my $param (keys %$remove_ref) {
            if ($param eq "name") {
                if ($remove_ref->{$param} eq "") {
                    # Remove the attribute regardless of it's contents
                    if ($output_format eq "one-line") {
                        # Remove the complete line
                        s/^.*//;
                        # We don't need to check any more attributes
                        last;
                    } else {
                        # Remove the attribute regardless of it's contents
                        s/ $param="[^"]*"//;
                    }
                } elsif (/ $param="$remove_ref->{$param}"/) {
                    # Remove only attributes where it's contents match the wanted data
                    if ($output_format eq "one-line") {
                        # Remove the complete line
                        s/^.*//;
                        # We don't need to check any more attributes
                        last;
                    } else {
                        # Remove the attribute regardless of it's contents
                        s/ $param="[^"]*"//;
                    }
                }
            } else {
                # Not the 'name' attribute
                if ($remove_ref->{$param} eq "") {
                    # Remove the attribute regardless of it's contents
                    s/ $param="[^"]*"//;
                } elsif (/ $param="$remove_ref->{$param}"/) {
                    # Remove only attributes where it's contents match the wanted data
                    s/ $param="[^"]*"//;
                }
            }
        }
    }

    # Next remove the attributes from the hash
    for my $key (keys %$hash_ref) {
        for my $param (keys %$remove_ref) {
            if ($param eq "name") {
                if ($remove_ref->{$param} eq "") {
                    # Remove the attribute regardless of it's contents
                    push @delete_keys, $key;
                } elsif ($key =~ /$remove_ref->{$param}/) {
                    push @delete_keys, $key;
                }
            } else {
                # Not the 'name' attribute
                if (exists $hash_ref->{$key}{$param}) {
                    if ($remove_ref->{$param} eq "") {
                        # Remove the attribute regardless of it's contents
                        delete $hash_ref->{$key}{$param};
                    } elsif ($hash_ref->{$key}{$param} =~ /$remove_ref->{$param}/) {
                        # Remove only attributes where it's contents match the wanted data
                        delete $hash_ref->{$key}{$param};
                    }
                }
            }
        }
    }

    # Finally remove any complete hash entries
    if (@delete_keys) {
        for my $key (@delete_keys) {
            delete $hash_ref->{$key};
        }
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

This script print a network configuration file used by the SC DAFT playlist packages.
It can for example be used to remove empty lines and comment lines and to print the
parameters on one line instead of multiple lines.

Syntax:
=======

 $0 [<OPTIONAL>]


 <OPTIONAL> are one or more of the following parameters:

  -g | --generate-param-help
  -h | --help
  -f | --old-file <filename>
  -o | --output-file <filename>
  -r | --output-format [one-line|multiple-lines]
  -a | --remove-attributes <string>
  -c | --strip-comments
  -e | --strip-empty-lines


 where:

  --generate-param-help
  -g
  ---------------------
  If specified then it will generate the help information from the template file
  and write it into a text file where it only shows the parameter names and the
  help text for each parameter.


  --help
  -h
  ------
  Shows this help.


  --old-file <filename>
  -f <filename>
  ---------------------
  Specifies the file that contain an old network configuration file with already
  filled out data. The data from this file will be used to print the contents to
  the script or to a new output file.


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


  --remove-attributes <string>
  -a <string>
  ----------------------------
  If specified it will remove the specified attribute from the printout.
  E.g. if '-a description' is specified then the printed parameters will not
  include the 'description=""' attribute and it's value.
  If the <string> contains an equal sign like <attribute=value> then the
  attribute matching the value will be removed.
  For example:
  -a reuse_deployed_helm_value=true
  will only remote the attribute when: reuse_deployed_helm_value="true"

  This parameter can be specified multiple times to remove many attributes.


  --strip-comments
  -c
  ----------------
  If specified then the output will not contain any comments.


  --strip-empty-lines
  -e
  -------------------
  If specified then the output will not contain any empty lines.


Examples:
=========

  Example 1:
  ----------
  To print the contents of a network config file to STDOUT without any formatting applied.

  $0 \\
    --old-file SC_Node.xml

  Example 2:
  ----------
  To print the contents of a network config file to a new file without any formatting applied.

  $0 \\
    --old-file SC_Node.xml \\
    --output-file SC_Node_NEW.xml

  Example 3:
  ----------
  To print the contents of a network config file to STDOUT but removing all empty lines and
  all comment lines and print each parameter on one line.

  $0 \\
    --old-file SC_Node.xml \\
    --strip-comments \\
    --strip-empty-lines \\
    --output-format one-line

  Example 4:
  ----------
  To print the contents of a network config file to STDOUT but removing all empty lines and
  all comment lines and remove some attributes including all parameters containing user account
  data and print each parameter on one line.

  $0 \\
    --old-file SC_Node.xml \\
    --strip-comments \\
    --strip-empty-lines \\
    --remove-attributes application_type \\
    --remove-attributes cnf_type \\
    --remove-attributes description \\
    --remove-attributes name=default_user.+ \\
    --remove-attributes playlist \\
    --remove-attributes reuse_deployed_helm_value \\
    --remove-attributes valid_from_releases \\
    --remove-attributes valid_releases \\
    --output-format one-line


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

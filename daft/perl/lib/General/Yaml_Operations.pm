package General::Yaml_Operations;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.14
#  Date     : 2022-03-16 19:16:12
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2021
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
use warnings;

use Exporter qw(import);

our @EXPORT_OK = qw(
    read_array
    read_file
    read_file_return_reference
    read_scalar
    update_file_anchor_values
    write_file
    );

use File::Basename qw(dirname basename);
use Cwd qw(abs_path);

use boolean;
use General::Data_Structure_Operations;
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use YAML::PP;
use YAML::PP::Common qw/ PRESERVE_SCALAR_STYLE /;

our $last_yaml_error_details = "";

#
# Read the input YAML data from an array variable and return the parsed data as
# an hash reference.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input":
#           Array reference of variable containing input with YAML data to parse.
#           This data should be without any CR or LF characters.
#           This parameter is mandatory.
#       "output-ref":
#           Hash reference of variable where the contents of the file is
#           returned to the caller.
#           This parameter is mandatory.
#    OPTIONAL
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#
# Example:
# my @temp = ();
# my $rc = General::Yaml_Operations::read_scalar(
#     {
#         "input"               => \@yaml_data,
#         "output-ref"          => \%hash,
#     }
# );
#
# Output:
#  - The hash is updated with all YAML data from the scalar.
#
# Return code:
#  0: Success, the scalar was read and parsed.
#  1: Failure to read and parse the scalar.
sub read_array {
    my %params = %{$_[0]};

    # Initialize local variables
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $input               = exists $params{"input"} ? $params{"input"} : undef;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;
    my $hashref;
    my $yaml_string = "";

    # Check that we have input to read
    unless (defined $input) {
        write_message("ERROR: You have to specify an input to read\n", $hide_error_msg);
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        write_message("ERROR: You have to specify a hash variable to write data to\n", $hide_error_msg);
        return 1;
    }

    # Convert the input data into a scalar variable with multiple lines.
    $yaml_string = join("\n", @$input);

    # Preserve values like '10.0.10.1' should be eventually written back as '10.0.10.1' and not 10.0.10.1
    # also write boolean values as true and false and not 'true' and 'false'.
    my $ypp = YAML::PP->new( preserve => PRESERVE_SCALAR_STYLE, boolean => 'boolean' );
    eval { $hashref = $ypp->load_string($yaml_string); };
    if ($@) {
        write_message("ERROR: Failure to parse the input\n$@\n", $hide_error_msg);
        $last_yaml_error_details = $@;
        return 1;
    }
   # I had to do this "ugly" workaround because I could not figure out how to
   # copy the hash reference to an anonymous hash into an hash reference, so now
   # it copies the top level keys as normal keys into the hash reference.
   for my $key (sort keys %$hashref) {
       $output_ref->{$key} = $hashref->{$key};
   }

    return 0;
}

#
# Read the YAML input from a file and return the data in an hash reference.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "filename":
#           The file to read.
#           This parameter is mandatory.
#       "output-ref":
#           Hash reference of variable where the contents of the file is
#           returned to the caller.
#    OPTIONAL
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#
# Example:
# my @temp = ();
# my $rc = General::Yaml_Operations::read_file(
#     {
#         "filename"            => "/path/to/file.yaml",
#         "output-ref"          => \%hash,
#     }
# );
#
# Output:
#  - The hash is updated with all data from the file.
#
# Return code:
#  0: Success, file was read.
#  1: Failure to read the file.
sub read_file {
    my %params = %{$_[0]};

    # Initialize local variables
    my $filename            = exists $params{"filename"} ? $params{"filename"} : undef;
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;
    my $hashref;

    # Check that we have a file to read
    unless (defined $filename) {
        write_message("ERROR: You have to specify a filename to read\n", $hide_error_msg);
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        write_message("ERROR: You have to specify a hash variable to write data to\n", $hide_error_msg);
        return 1;
    }

    # Preserve values like '10.0.10.1' should be eventually written back as '10.0.10.1' and not 10.0.10.1
    # also write boolean values as true and false and not 'true' and 'false'.
    my $ypp = YAML::PP->new( preserve => PRESERVE_SCALAR_STYLE, boolean => 'boolean' );
    eval { $hashref = $ypp->load_file($filename); };
    if ($@) {
        write_message("ERROR: Failure to load or parse the file '$filename'\n$@\n", $hide_error_msg);
        $last_yaml_error_details = $@;
        return 1;
    }
   # I had to do this "ugly" workaround because I could not figure out how to
   # copy the hash reference to an anonymous hash into an hash reference, so now
   # it copies the top level keys as normal keys into the hash reference.
   for my $key (sort keys %$hashref) {
       $output_ref->{$key} = $hashref->{$key};
   }

    return 0;
}

#
# Read the input file and return the data as a reference to an array or hash.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "filename":
#           The file to read.
#           This parameter is mandatory.
#    OPTIONAL
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#
# Example:
# my @temp = ();
# my $dataref = General::Json_Operations::read_file_return_reference(
#     {
#         "filename"            => "/path/to/file.json",
#     }
# );
# unless (defined $dataref) {
#     print "Unable to decode data\n";
# }
# if (ref($dataref) eq "ARRAY) {
#     # An array is returned
#     my $index = 0;
#     for (@$dataref) {
#         printf "index=$index, type=%s\n", ref($dataref[$index]);
#         $index++;
#     }
# } elsif (ref($dataref) eq "HASH) {
#     # An hash is returned
#     for my $key (sort keys %$dataref) {
#         printf "key=$key, type=%s\n", ref($dataref->{$key});
#     }
# } elsif (! defined $dataref) {
#     print "No data found\n";
# } else {
#     print "Some other unsupported data type\n";
# }
#
# Returns
#   The data as a reference to an array or hash, or undef if failure,
sub read_file_return_reference {
    my %params = %{$_[0]};

    # Initialize local variables
    my $filename            = exists $params{"filename"} ? $params{"filename"} : undef;
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $yaml_ref;
    my $yaml;

    # Check that we have a file to read
    unless (defined $filename) {
        write_message("ERROR: You have to specify a filename to read\n", $hide_error_msg);
        return undef;
    }

    # Preserve values like '10.0.10.1' should be eventually written back as '10.0.10.1' and not 10.0.10.1
    # also write boolean values as true and false and not 'true' and 'false'.
    $yaml = YAML::PP->new( preserve => PRESERVE_SCALAR_STYLE, boolean => 'boolean' );
    eval { $yaml_ref = $yaml->load_file($filename); };
    if ($@) {
        write_message("ERROR: Failure to load or parse the file '$filename'\n$@\n", $hide_error_msg);
        $last_yaml_error_details = $@;
        return 1;
    }

    return $yaml_ref;
}

#
# Read the input from a scalar variable and return the data in an hash reference.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input":
#           The input data in YAML format to read and parse.
#           This parameter is mandatory.
#       "output-ref":
#           Hash reference of variable where the contents of the file is
#           returned to the caller.
#           This parameter is mandatory.
#    OPTIONAL
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#
# Example:
# my @temp = ();
# my $rc = General::Yaml_Operations::read_scalar(
#     {
#         "input"               => "yaml data line 1\nyaml data line 2\nyaml data line 3",
#         "output-ref"          => \%hash,
#     }
# );
#
# Output:
#  - The hash is updated with all YAML data from the scalar.
#
# Return code:
#  0: Success, the scalar was read and parsed.
#  1: Failure to read and parse the scalar.
sub read_scalar {
    my %params = %{$_[0]};

    # Initialize local variables
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $input               = exists $params{"input"} ? $params{"input"} : undef;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;
    my $hashref;

    # Check that we have input to read
    unless (defined $input) {
        write_message("ERROR: You have to specify an input to read\n", $hide_error_msg);
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        write_message("ERROR: You have to specify a hash variable to write data to\n", $hide_error_msg);
        return 1;
    }

    # Preserve values like '10.0.10.1' should be eventually written back as '10.0.10.1' and not 10.0.10.1
    # also write boolean values as true and false and not 'true' and 'false'.
    my $ypp = YAML::PP->new( preserve => PRESERVE_SCALAR_STYLE, boolean => 'boolean' );
    eval { $hashref = $ypp->load_string($input); };
    if ($@) {
        write_message("ERROR: Failure to parse the input\n$@\n", $hide_error_msg);
        $last_yaml_error_details = $@;
        return 1;
    }
   # I had to do this "ugly" workaround because I could not figure out how to
   # copy the hash reference to an anonymous hash into an hash reference, so now
   # it copies the top level keys as normal keys into the hash reference.
   for my $key (sort keys %$hashref) {
       $output_ref->{$key} = $hashref->{$key};
   }

    return 0;
}

#
# Update all anchor values in the specified file.
# The data passed in the "output-ref" variable should be a reference to an array
# where each index contains <anchor name>=<anchor value>.
# It will look for entries in the file that looks something like this:
# <anchored content>: &<anchor name> "<anchor value>"
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "filename":
#           The file to read.
#           This parameter is mandatory.
#       "output-ref":
#           Array reference that contains a list of anchor names and the values
#           to assign to the anchors in the file.
#    OPTIONAL
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#
# Example:
# my @temp = ();
# my $rc = General::Yaml_Operations::update_file_anchor_values(
#     {
#         "filename"            => "/path/to/file.yaml",
#         "output-ref"          => \@array,
#     }
# );
#
# Output:
#  - The file is updated with new anchor values.
#
# Return code:
#  0: Success, file was updated.
#  1: Failure to update the file.
sub update_file_anchor_values {
    my %params = %{$_[0]};

    # Initialize local variables
    my $filename            = exists $params{"filename"} ? $params{"filename"} : undef;
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;

    my $anchor_name;
    my $anchor_value;
    my %anchor_values;
    my @file_data = ();
    my $prefix;
    my $rc;
    my $white_space;
    my $write_file = 0;

    # Check that we have a file to read
    unless (defined $filename) {
        write_message("ERROR: You have to specify a filename to update\n", $hide_error_msg);
        return 1;
    }
    # Check that we have a variable that contains anchor name and value pairs
    unless (defined $output_ref) {
        write_message("ERROR: You have to specify an array variable that contains anchor name and value pairs\n", $hide_error_msg);
        return 1;
    }
    if (scalar @$output_ref == 0) {
        write_message("ERROR: The 'output-ref' array reference does not contain any anchor name and value pairs\n", $hide_error_msg);
        return 1;
    }
    for (@$output_ref) {
        if (/^(\S+?)=(.+)/) {
            $anchor_values{$1} = $2;
        } elsif (/^(\S+?)=$/) {
            $anchor_values{$1} = "''";
        } else {
            write_message("ERROR: The 'output-ref' array reference does not contain a valid anchor name and value pair '$_'\n", $hide_error_msg);
            return 1;
        }
    }

    $rc = General::File_Operations::read_file(
        {
            "filename"              => "$filename",
            "output-ref"            => \@file_data,
            "hide-error-messages"   => $hide_error_msg,
        }
    );
    if ($rc != 0) {
        write_message("ERROR: The '$filename' file could not be read\n", $hide_error_msg);
        return 1;
    }

    for (@file_data) {
        next if (/^\s*#/);
        if (/^(\s*\S+:\s*\&)(\S+)(\s+)(.+)/) {
            $prefix = $1;
            $anchor_name = $2;
            $white_space = $3;
            $anchor_value = $4;

            if (exists $anchor_values{$anchor_name}) {
                # Update the line with new anchor value
                $_ = "$prefix$anchor_name$white_space$anchor_values{$anchor_name}";
                $write_file = 1;
            }
        }
    }

    $rc = General::File_Operations::write_file(
        {
            "filename"              => "$filename",
            "output-ref"            => \@file_data,
            "hide-error-messages"   => $hide_error_msg,
        }
    );
    if ($rc != 0) {
        write_message("ERROR: The '$filename' file could not be written\n", $hide_error_msg);
        return 1;
    }

    return 0;
}

#
# Write provided data in array to specified output file.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "filename":
#           The file to write.
#           This parameter is mandatory.
#       "output-ref":
#           Hash reference of variable that contains the contents to be written
#           to the file.
#
#    OPTIONAL
#       "file-access-mode":
#           Set file access mode of the file that is created or updated.
#           The default is to always use whatever the default access right of the
#           user is set to when creating new files.
#           Example if usage:
#           "file-access-mode" => "666"
#           "file-access-mode" => "a+w"
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#
# Example:
# my @temp = ();
# my $rc = General::Yaml_Operations::write_file(
#     {
#         "filename"            => "/path/to/file.txt",
#         "output-ref"          => \@temp,
#     }
# );
#
# Output:
#  - The output file is updated with all data from the provided output reference variable.
#
# Return code:
#  0: Success, the file was written.
#  1: Failure to write to the file.
sub write_file {
    my %params = %{$_[0]};

    # Initialize local variables
    my $filename            = exists $params{"filename"} ? $params{"filename"} : undef;
    my $file_access_mode    = exists $params{"file-access-mode"} ? $params{"file-access-mode"} : undef;
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;

    # Check that we have a file to read
    unless (defined $filename) {
        write_message("ERROR: You have to specify a filename to read\n", $hide_error_msg);
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        write_message("ERROR: You have to specify a hash variable with contents to write to file\n", $hide_error_msg);
        return 1;
    }

    # Preserve values like '10.0.10.1' should be eventually written back as '10.0.10.1' and not 10.0.10.1
    # also write boolean values as true and false and not 'true' and 'false'.
    my $ypp = YAML::PP->new( preserve => PRESERVE_SCALAR_STYLE, boolean => 'boolean' );
    eval { $ypp->dump_file($filename, $output_ref); };
    if ($@) {
        write_message("ERROR: Failure to write the file '$filename'\n$@\n", $hide_error_msg);
        $last_yaml_error_details = $@;
        return 1;
    }

    if (defined $file_access_mode) {
        my $rc = General::OS_Operations::send_command(
            {
                "command"           => "chmod $file_access_mode $filename",
                "hide-output"       => 1,
            }
        );
        if ($rc != 0) {
            write_message("ERROR: Unable to change access mode '$file_access_mode' of the file '$filename'\n", $hide_error_msg);
            return 1;
        }
    }
    return 0;
}

#
# Write provided message to screen (if wanted) and to log file (if opened).
#
sub write_message {
    my $message = shift;
    my $hide_message = shift;

    print $message unless $hide_message;

    General::Logging::log_write($message);

    return 0;
}

1;

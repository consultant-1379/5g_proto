package General::Json_Operations;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.9
#  Date     : 2022-02-18 18:26:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2022
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
    read_array_return_reference
    read_file
    read_file_return_reference
    read_scalar
    write_file
    write_to_array
    write_to_scalar
    );

use boolean;
use File::Basename qw(dirname basename);
use Cwd qw(abs_path);
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use JSON::PP;

our $last_json_error_details = "";

#
# Read the input JSON data from an array variable and return the parsed data as
# an hash reference.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input":
#           Array reference of variable containing input with JSON data to parse.
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
# my $rc = General::Json_Operations::read_scalar(
#     {
#         "input"               => \@json_data,
#         "output-ref"          => \%hash,
#     }
# );
#
# Output:
#  - The hash is updated with all JSON data from the scalar.
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
    my $json;
    my $json_string = "";

    # Check that we have input to read
    unless (defined $input) {
        write_message("ERROR: You have to specify input read\n", $hide_error_msg);
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        write_message("ERROR: You have to specify a hash variable to write data to\n", $hide_error_msg);
        return 1;
    }

    # Convert the input data into a scalar variable with multiple lines.
    $json_string = join("\n", @$input);

    $json = JSON::PP->new;
    eval { $hashref = $json->decode($json_string); };
    if ($@) {
        write_message("ERROR: Failure to parse the input\n$@\n", $hide_error_msg);
        $last_json_error_details = $@;
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
# Read the input JSON data from an array variable and return the parsed data as
# a reference to an array or hash.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input":
#           Array reference of variable containing input with JSON data to parse.
#           This data should be without any CR or LF characters.
#           This parameter is mandatory.
#    OPTIONAL
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#
# Example:
# my $dataref = General::Json_Operations::read_array_return_reference(
#     {
#         "input"            => \@json_lines,
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
sub read_array_return_reference {
    my %params = %{$_[0]};

    # Initialize local variables
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $input               = exists $params{"input"} ? $params{"input"} : undef;
    my $json_ref;
    my $json;
    my $json_string = "";

    # Check that we have input to read
    unless (defined $input) {
        write_message("ERROR: You have to specify input read\n", $hide_error_msg);
        return undef;
    }

    # Convert the input data into a scalar variable with multiple lines.
    $json_string = join("\n", @$input);

    $json = JSON::PP->new;
    eval { $json_ref = $json->decode($json_string); };
    if ($@) {
        write_message("ERROR: Failure to parse the input\n$@\n", $hide_error_msg);
        $last_json_error_details = $@;
        return undef;
    }

    return $json_ref;
}

#
# Read the input file and return the data in an array.
#
# NOTE: This only works if the returned data is a hash.
# If the JSON data starts with an array then this function will not work, in this
# case use the subroutine 'read_file_return_reference' instead.
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
# my $rc = General::Json_Operations::read_file(
#     {
#         "filename"            => "/path/to/file.json",
#         "output-ref"          => \%hash,
#     }
# );
#
# Output:
#  - The hash is updated with all data from the file with CR and LF characters
#    removed.
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
    my $json;
    my $json_text;

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

    $json_text = do {
        if (open(my $json_fh, "<:encoding(UTF-8)", $filename)) {
            local $/;
            <$json_fh>
        } else {
            write_message("ERROR: Failed to open '$filename'\n", $hide_error_msg);
            return 1;
        }
    };

    $json = JSON::PP->new;
    eval { $hashref = $json->decode($json_text); };
    if ($@) {
        write_message("ERROR: Failure to load or parse the file '$filename'\n$@\n", $hide_error_msg);
        $last_json_error_details = $@;
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
    my $json_ref;
    my $json;
    my $json_text;

    # Check that we have a file to read
    unless (defined $filename) {
        write_message("ERROR: You have to specify a filename to read\n", $hide_error_msg);
        return undef;
    }

    $json_text = do {
        if (open(my $json_fh, "<:encoding(UTF-8)", $filename)) {
            local $/;
            <$json_fh>
        } else {
            write_message("ERROR: Failed to open '$filename'\n", $hide_error_msg);
            return undef;
        }
    };

    $json = JSON::PP->new;
    eval { $json_ref = $json->decode($json_text); };
    if ($@) {
        write_message("ERROR: Failure to load or parse the file '$filename'\n$@\n", $hide_error_msg);
        $last_json_error_details = $@;
        return undef;
    }

    return $json_ref;
}

#
# Read the input from a scalar variable and return the data in an hash reference.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input":
#           The input data in JSON format to read and parse.
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
# my $rc = General::Json_Operations::read_scalar(
#     {
#         "input"               => "json data line 1\njson data line 2\njson data line 3",
#         "output-ref"          => \%hash,
#     }
# );
#
# Output:
#  - The hash is updated with all JSON data from the scalar.
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
    my $json;

    # Check that we have input to read
    unless (defined $input) {
        write_message("ERROR: You have to specify input read\n", $hide_error_msg);
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        write_message("ERROR: You have to specify a hash variable to write data to\n", $hide_error_msg);
        return 1;
    }

    $json = JSON::PP->new;
    eval { $hashref = $json->decode($input); };
    if ($@) {
        write_message("ERROR: Failure to parse the input\n$@\n", $hide_error_msg);
        $last_json_error_details = $@;
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
#       "sort-output":
#           Sort the data written to the file if the value passed is "1" or write
#           the data in whatever order if the value passed is "0" or if the parameter
#           is not specified.
#
# Example:
# my @temp = ();
# my $rc = General::Json_Operations::write_file(
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
    my $sort_output         = exists $params{"sort-output"} ? $params{"sort-output"} : 0;
    my $json;
    my $json_text;

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

    # Convert the hash reffereence to JSON text
    $json = JSON::PP->new;
    $json = $json->pretty([1]);
    $json = $json->canonical([1]) if $sort_output;
    eval { $json_text = $json->encode($output_ref); };
    if ($@) {
        write_message("ERROR: Failure to encode JSON data\n$@\n", $hide_error_msg);
        $last_json_error_details = $@;
        return 1;
    }

    # Write the JSON text to file
    if (open(my $json_fh, ">:encoding(UTF-8)", $filename)) {
        print $json_fh "$json_text";
        close $json_fh;
    } else {
        write_message("ERROR: Failure to write the file '$filename'\n", $hide_error_msg);
        return 1;
    }

    # Change file access mode, if needed
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

#
# Write provided Perl structure in input reference as JSON text array to output.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input-ref":
#           Array or Hash reference of variable that contains the Perl data structure
#           to be converted to JSON format and written to the output reference.
#           This parameter is mandatory.
#
#    OPTIONAL
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#       "pretty-print":
#           Return the output in an easy human readable format (=1) or return it a
#           compact format (=0) or if not specified.
#       "sort-output":
#           Sort the data written to the output if the value passed is "1", or write
#           the data in whatever order if the value passed is "0" or if the parameter
#           is not specified.
#       "strip-eol":
#           If specified (=1) then NL characters will not be included in the output array.
#           By default if not specified or when =0 then NL characters are returned.
#
# Example:
# my $ref_perl_structure;
# my @json_text = General::Json_Operations::write_to_array(
#     {
#         "input-ref"   => \$ref_perl_structure,
#     }
# );
#
# Output:
#  - The data structure is converted to JSON text and returned as an array or at error
#    an empty aray is returned.
#
# Return code:
#  -
sub write_to_array {
    my %params = %{$_[0]};

    # Initialize local variables
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $input_ref           = exists $params{"input-ref"} ? $params{"input-ref"} : undef;
    my @output              = ();
    my $pretty_print        = exists $params{"pretty-print"} ? $params{"pretty-print"} : 0;
    my $sort_output         = exists $params{"sort-output"} ? $params{"sort-output"} : 0;
    my $strip_eol           = exists $params{"strip-eol"} ? $params{"strip-eol"} : 0;
    my $json;
    my $json_text;

    # Check that we have a variable to read data from
    unless (defined $input_ref) {
        write_message("ERROR: You have to specify an array or hash reference variable with contents as 'input-ref'\n", $hide_error_msg);
        return @output;
    }

    # Convert the hash reffereence to JSON text
    $json = JSON::PP->new;
    $json = $json->pretty([1]) if $pretty_print;
    $json = $json->canonical([1]) if $sort_output;
    eval { $json_text = $json->encode($input_ref); };
    if ($@) {
        write_message("ERROR: Failure to encode JSON data\n$@\n", $hide_error_msg);
        $last_json_error_details = $@;
        return @output;
    }

    # Convert the scalar multi-line text into an array witout EOL
    @output = split /\r*\n/, $json_text;

    # If so wanted put back NL to each index
    if ($strip_eol == 0) {
        # Put back NL characters
        @output = join "\n", @output;
    }

    return @output;
}

#
# Write provided Perl structure in input reference as JSON text scalar to output.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input-ref":
#           Array or Hash reference of variable that contains the Perl data structure
#           to be converted to JSON format and written to the output reference.
#           This parameter is mandatory.
#
#    OPTIONAL
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#       "pretty-print":
#           Return the output in an easy human readable format (=1) or return it a
#           compact format (=0) or if not specified.
#       "sort-output":
#           Sort the data written to the output if the value passed is "1", or write
#           the data in whatever order if the value passed is "0" or if the parameter
#           is not specified.
#
# Example:
# my $ref_perl_structure;
# my $json_text = General::Json_Operations::write_to_scalar(
#     {
#         "input-ref"   => \$ref_perl_structure,
#     }
# );
#
# Output:
#  - The data structure is converted to JSON text and returned as a multi-line scalar
#    variable or at error an empty string is returned.
#
# Return code:
#  -
sub write_to_scalar {
    my %params = %{$_[0]};

    # Initialize local variables
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $input_ref           = exists $params{"input-ref"} ? $params{"input-ref"} : undef;
    my $pretty_print        = exists $params{"pretty-print"} ? $params{"pretty-print"} : 0;
    my $sort_output         = exists $params{"sort-output"} ? $params{"sort-output"} : 0;
    my $json;
    my $json_text;

    # Check that we have a variable to read data from
    unless (defined $input_ref) {
        write_message("ERROR: You have to specify an array or hash reference variable with contents as 'input-ref'\n", $hide_error_msg);
        return "";
    }

    # Convert the hash reffereence to JSON text
    $json = JSON::PP->new;
    $json = $json->pretty([1]) if $pretty_print;
    $json = $json->canonical([1]) if $sort_output;
    eval { $json_text = $json->encode($input_ref); };
    if ($@) {
        write_message("ERROR: Failure to encode JSON data\n$@\n", $hide_error_msg);
        $last_json_error_details = $@;
        return "";
    }

    return $json_text;
}

1;

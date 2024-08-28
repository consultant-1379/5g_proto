package General::File_Operations;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.35
#  Date     : 2024-05-29 14:00:59
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2017-2024
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
    checksum_file_md5
    checksum_file_sha1
    checksum_file_sha256
    copy_file
    find_file
    parse_allow_list_file
    parse_network_configuration_file
    read_file
    replace_network_configuration_variable_placeholders
    show_file_content
    tempfile_create
    tempfile_delete
    tempfile_delete_all
    update_job_variables_with_network_configuration_variables
    write_file
    );

use File::Basename qw(dirname basename);
use Cwd qw(abs_path);
use General::Json_Operations;
use General::Logging;
use General::OS_Operations;

# Hash that contains information about any temporary files created by DAFT.
# $created_tempfiles{$filepath}{'status'}: Contains the value =1 if the file has been created or value =0 if already deleted.
# $created_tempfiles{$filepath}{'created-by-file'}: Contains the filename where the temporary file was created from.
# $created_tempfiles{$filepath}{'created-by-line'}: Contains the line number where the temporary file was created from.
# $created_tempfiles{$filepath}{'created-by-sub'}: Contains the subroutine where the temporary file was created from.
# $created_tempfiles{$filepath}{'deleted-by-file'}: Contains the filename where the temporary file was deleted from.
# $created_tempfiles{$filepath}{'deleted-by-line'}: Contains the line number where the temporary file was deleted from.
# $created_tempfiles{$filepath}{'deleted-by-sub'}: Contains the subroutine where the temporary file was deleted from.
our %created_tempfiles;
my $debug = 0;

#
# Do an md5sum checksum of the provided file and result the result.
#
# Input:
#  - Filename to do an md5sum of.
#
# Output:
#  - The checksum value for the file, or empty string if error.
#
sub checksum_file_md5 {
    my $filename = shift;
    my @result = ();

    General::OS_Operations::send_command(
        {
            "command"           => "md5sum $filename",
            "discard-stderr"    => 1,
            "hide-output"       => 1,
            "return-output"     => \@result,
        }
    );
    if (scalar @result == 1) {
        if ($result[0] =~ /^([0-9a-fA-F]{32})\s+.+/) {
            return $1;
        } else {
            General::Logging::log_user_error_message("Wrong format of returned checksum value");
            return "";
        }
    } else {
        General::Logging::log_user_error_message("Too few or too many lines in returned checksum");
        return "";
    }
}

#
# Do an sha1sum checksum of the provided file and result the result.
#
# Input:
#  - Filename to do an sha1sum of.
#
# Output:
#  - The checksum value for the file, or empty string if error.
#
sub checksum_file_sha1 {
    my $filename = shift;
    my @result = ();

    General::OS_Operations::send_command(
        {
            "command"           => "sha1sum $filename",
            "discard-stderr"    => 1,
            "hide-output"       => 1,
            "return-output"     => \@result,
        }
    );
    if (scalar @result == 1) {
        if ($result[0] =~ /^([0-9a-fA-F]{40})\s+.+/) {
            return $1;
        } else {
            General::Logging::log_user_error_message("Wrong format of returned checksum value");
            return "";
        }
    } else {
        General::Logging::log_user_error_message("Too few or too many lines in returned checksum");
        return "";
    }
}

#
# Do an sha256sum checksum of the provided file and result the result.
#
# Input:
#  - Filename to do an sha256sum of.
#
# Output:
#  - The checksum value for the file, or empty string if error.
#
sub checksum_file_sha256 {
    my $filename = shift;
    my @result = ();

    General::OS_Operations::send_command(
        {
            "command"           => "sha256sum $filename",
            "discard-stderr"    => 1,
            "hide-output"       => 1,
            "return-output"     => \@result,
        }
    );
    if (scalar @result == 1) {
        if ($result[0] =~ /^([0-9a-fA-F]{64})\s+.+/) {
            return $1;
        } else {
            General::Logging::log_user_error_message("Wrong format of returned checksum value");
            return "";
        }
    } else {
        General::Logging::log_user_error_message("Too few or too many lines in returned checksum");
        return "";
    }
}

#
# Copy a file to another file.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "from-filename":
#           The file to copy from.
#           This parameter is mandatory.
#       "to-filename":
#           The file to copy to.
#           This parameter is mandatory.
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
#       "overwrite-file":
#           If specified and if the file already exist then the file is overwritten
#           otherwise if not specified and if the file exist then the copy fails.
#
# Example:
# my $rc = General::File_Operations::copy_file(
#     {
#         "from-filename"       => "/path/to/oldfile.txt",
#         "to-filename"         => "/path/to/newfile.txt",
#     }
# );
#
# Output:
#  - The output file is updated with all data from the provided output reference variable.
#
# Return code:
#  0: Success, the file was copied.
#  1: Failure to copy to the file.
sub copy_file {
    my %params = %{$_[0]};

    # Initialize local variables
    my $file_access_mode    = exists $params{"file-access-mode"} ? $params{"file-access-mode"} : undef;
    my $from_filename       = exists $params{"from-filename"} ? $params{"from-filename"} : undef;
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $overwrite_file      = exists $params{"overwrite-file"} ? $params{"overwrite-file"} : 0;
    my $to_filename         = exists $params{"to-filename"} ? $params{"to-filename"} : undef;
    my $rc;
    my @result;

    # Check that we have a file to read
    if (defined $from_filename) {
        unless (-f $from_filename) {
            print "ERROR: The filename to copy from does not exist\n" unless $hide_error_msg;
            return 1;
        }
    } else {
        print "ERROR: You have to specify a filename to copy from, 'from-filename' is missing\n" unless $hide_error_msg;
        return 1;
    }
    # Check that we have a file to write
    if (defined $to_filename) {
        if (-f $to_filename && $overwrite_file == 0) {
            print "ERROR: The file to copy to already exist and 'overwrite-file' was not specified\n" unless $hide_error_msg;
            return 1;
        }
    } else {
        print "ERROR: You have to specify a filename to copy to, 'to-filename' is missing\n" unless $hide_error_msg;
        return 1;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"           => "cp -pf $from_filename $to_filename",
            "hide-output"       => 1,
            "raw-output"        => 1,
            "return-output"     => \@result,
        }
    );
    if ($rc != 0) {
        print "ERROR: The copy failed\n" . @result unless $hide_error_msg;
        return 1;
    }

    if (defined $file_access_mode) {
        $rc = General::OS_Operations::send_command(
            {
                "command"           => "chmod $file_access_mode $to_filename",
                "hide-output"       => 1,
                "raw-output"        => 1,
                "return-output"     => \@result,
            }
        );
        if ($rc != 0) {
            print "ERROR: The change of access mode failed\n" . @result unless $hide_error_msg;
            return 1;
        }
    }

    return 0;
}

#
# Extract each parameter and store in hash reference.
#
# Input:
#  - Input data containing one or more parameters
#  - Reference to output hash
#
# Output:
#  - The output hash is updated to have the "name" as the main key.
#
sub extract_xml_tag_parameters {
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
        $line =~ s/\s*name="$key"\s*/ /;
    } elsif ($line =~ /\s*name='(.+?)'\s*/) {
        $key = $1;
        $line =~ s/\s*name='$key'\s*/ /;
    }
    print "Key=$key\n" if ($debug > 2);
    while (1) {
        if ($line =~ /^\s*(\w+)="(.*?)"\s*/) {
            $param = $1;
            $value = $2;
            $line =~ s/^\s*\w+=".*?"\s*//;
        } elsif ($line =~ /^\s*(\w+)='(.*?)'\s*/) {
            $param = $1;
            $value = $2;
            $line =~ s/^\s*\w+='.*?'\s*//;
        } else {
            last;
        }
        print "  Attribute=\"$param\"\n  Value=\"$value\"\n\n" if ($debug > 2);
        $hash_ref->{$key}{$param} = $value;
    }
}

#
# Find file(s) in specified directory or sub directories.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#
#    OPTIONAL
#       "directory":
#            Directory path where search should start, default is current directory.
#       "filename":
#           If specified it will limit the search to only files matching the pattern.
#       "maxdepth":
#           How deep to search for files, default is 1.
# Output:
#  - The output is an array with found files, or an empty array if nothing found or
#    if an error was detected.
#
sub find_file {
    my %params = %{$_[0]};

    # Initialize local variables
    my $dirname  = exists $params{"directory"} ? $params{"directory"} : ".";
    my $filename = exists $params{"filename"} ? $params{"filename"} : "*";
    my $maxdepth = exists $params{"maxdepth"} ? $params{"maxdepth"} : 1;
    my @files    = ();

    if (-d "$dirname") {
        $dirname = abs_path $dirname;
    } else {
        # Not a valid directory"
        General::Logging::log_user_error_message("No such directory ($dirname)");
        return @files;
    }

    General::OS_Operations::send_command(
        {
            "command"           => "find $dirname -maxdepth $maxdepth -name '$filename' -type f",
            "discard-stderr"    => 1,
            "hide-output"       => 1,
            "return-output"     => \@files,
        }
    );

    return @files;
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
sub one_xml_tag_per_line {
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
# Read the input JSON file which should contain an array of hashes with allow
# values to use for checking the alarm list and core dumps.
# It return the data in an array reference, with options to filter away certain data.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#
#    MANDATORY
#       "filename":
#           The file to read that contains the allow list entries as JSON coded data.
#           This parameter is mandatory.
#       "output-ref":
#           Array reference of variable where the parsed data will be stored.
#           This parameter is mandatory.
#
#           This array will look something like this for alarm_allowlist:
#           @$output_ref = (
#               {
#                   "alarmName" => "DiaadpConfigurationIncomplete",
#                   "severity" => "Minor"
#               },
#               {
#                   "alarmName" => "PodFailure",
#                   "severity" => "(Major|Minor)"
#               }
#           )
#
#           or for coredump_allowlist:
#           @$output_ref = (
#               {
#                   "filename_regexp" => "core\.dsc_.+",
#               },
#               {
#                   "filename_regexp" => "core\.cm_core_yang_li.+",
#               }
#           )
#
#    OPTIONAL
#       "append-output-ref":
#           If specified and =1 then the file entries are appended to whatever
#           already exists in the output-ref variable.
#           If =0, which is also the default, then the output-ref array is first
#           initialized to an empty list before adding file entries.
#       "discarded-ref":
#           If specified it should be a reference to an array that will contain a
#           list of the allow file entries that was discarded because of not
#           matching the conditions.
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#       "application-type":
#           Check parameter @@APPLICATION_TYPE@@ against this value.
#           If the parameter match this value then the record is returned in @$output_ref.
#           If not matching this value then it's discarded.
#           If not specified then the record is automatically returned unless some
#           other parameter value probibits this.
#
#           The currently known supported types are:
#               - dsc
#               - sc
#           But any type that exist in the file that matches this value will be
#           returned.
#       "entry-type":
#           Check parameter @@ENTRY_TYPE@@ against this value.
#           If the parameter match this value then the record is returned in @$output_ref.
#           If not matching this value then it's discarded.
#           If not specified then the record is automatically returned unless some
#           other parameter value probibits this.
#
#           The currently known supported types are:
#               - alarm_allowlist
#               - coredump_allowlist
#               - pod_restart_allowlist
#           But any type that exist in the file that matches this value will be
#           returned.
#       "node-name":
#           Check parameter @@NODE_NAME@@ against this value.
#           If the parameter match this value then the record is returned in @$output_ref.
#           If not matching this value then it's discarded.
#           If not specified then the record is automatically returned unless some
#           other parameter value probibits this.
#
#           Example values:
#               - eevee
#               - snorlax
#       "software-version":
#           Check parameter @@SOFTWARE_VERSION@@ against this value.
#           If the parameter match this value then the record is returned in @$output_ref.
#           If not matching this value then it's discarded.
#           If not specified then the record is automatically returned unless some
#           other parameter value probibits this.
#
#           Example values:
#               - 1.12.0
#               - 1.11.0
#
# Example:
# $rc = General::File_Operations::parse_allow_list_file(
#     {
#         "filename"              => $default_allow_file,
#         "output-ref"            => \@allow_list_coredumps,
#         "entry-type"            => "coredump_allowlist",
#         "discarded-ref"         => \@discarded,
#         "hide-error-messages"   => 1,
#         "application-type"      => $result_information->{'application_type'},
#         "node-name"             => $result_information->{'node_name'},
#         "software-version"      => $result_information->{'software_version'},
#     }
# );
#
# Output:
#  - The hash is updated with all <access> and <parameter> XML tag values.
#
# Return code:
#  0: Successful parsing of allow list file.
#  1: Failure to parse allow list file.
sub parse_allow_list_file {
    my %params = %{$_[0]};

    # Initialize local variables
    my $append_output_ref       = exists $params{"append-output-ref"} ? $params{"append-output-ref"} : 0;
    my $discarded_ref           = exists $params{"discarded-ref"} ? $params{"discarded-ref"} : undef;
    my $filename                = exists $params{"filename"} ? $params{"filename"} : undef;
    my $hide_error_msg          = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $output_ref              = exists $params{"output-ref"} ? $params{"output-ref"} : undef;
    my $wanted_application_type = exists $params{"application-type"} ? $params{"application-type"} : "";
    my $wanted_entry_type       = exists $params{"entry-type"} ? $params{"entry-type"} : "";
    my $wanted_node_name        = exists $params{"node-name"} ? $params{"node-name"} : "";
    my $wanted_software_version = exists $params{"software-version"} ? $params{"software-version"} : "";

    my @input = ();
    my $rc;

    # Check that we have a file to read
    unless (defined $filename) {
        print "ERROR: You have to specify a filename to read\n" unless $hide_error_msg;
        return 1;
    }
    # Check that we have a variable to return data to
   #unless (defined $output_ref) {
   #    print "ERROR: You have to specify a hash variable to write data to\n" unless $hide_error_msg;
   #    return 1;
   #}

    my $json_ref = General::Json_Operations::read_file_return_reference( { "filename" => $filename, "hide-error-messages" => $hide_error_msg } );
    unless ($json_ref) {
        print "ERROR: Failed to parse the global allow list: $filename\nNo global allow list will be used.\n" unless $hide_error_msg;
        return 1;
    }

    # Empty the output array
    if ($append_output_ref != 0) {
        @$output_ref = ();
    }

    for (my $i=0; $i < scalar @$json_ref; $i++) {
        my $hash_ref = $json_ref->[$i];
        my $hash_details = "";
        my $application_type = "";
        my $discard_reason = "";
        my $entry_type = "";
        my $node_name = "";
        my $software_version = "";

        for my $key (sort keys %$hash_ref) {
            $hash_details .= "    $key=$hash_ref->{$key}\n";
        }

        if (exists $hash_ref->{'@@APPLICATION_TYPE@@'}) {
            $application_type = $hash_ref->{'@@APPLICATION_TYPE@@'};
            delete $hash_ref->{'@@APPLICATION_TYPE@@'};
            if ($wanted_application_type ne "" && $wanted_application_type !~ /^$application_type$/i) {
                # The application (sc or dsc) does not match, so ignore this entry
                $discard_reason .= "application_type: $wanted_application_type != $application_type & ";
            }
        }
        if (exists $hash_ref->{'@@DESCRIPTION@@'}) {
            # This is just a comment field that can e.g. be used to describe why this entry was added
            # and serves no other purpose for the allow list handling. So we just delete this field.
            delete $hash_ref->{'@@DESCRIPTION@@'};
        }
        if (exists $hash_ref->{'@@ENTRY_TYPE@@'}) {
            $entry_type = $hash_ref->{'@@ENTRY_TYPE@@'};
            delete $hash_ref->{'@@ENTRY_TYPE@@'};
            if ($wanted_entry_type ne "" && $wanted_entry_type !~ /^$entry_type$/i) {
                $discard_reason .= "entry_type: $wanted_entry_type != $entry_type & ";
            }
        }
        if (exists $hash_ref->{'@@NODE_NAME@@'}) {
            $node_name = $hash_ref->{'@@NODE_NAME@@'};
            delete $hash_ref->{'@@NODE_NAME@@'};
            if ($wanted_node_name ne "" && $wanted_node_name !~ /$node_name/i) {
                # The node name does not match, so ignore this entry
                $discard_reason .= "node_name: $wanted_node_name != $node_name & ";
            }
        }
        if (exists $hash_ref->{'@@SOFTWARE_VERSION@@'}) {
            $software_version = $hash_ref->{'@@SOFTWARE_VERSION@@'};
            delete $hash_ref->{'@@SOFTWARE_VERSION@@'};
            if ($wanted_software_version ne "" && $wanted_software_version !~ /$software_version/i) {
                # The software version 1.x.x does not match, so ignore this entry
                $discard_reason .= "software_version: $wanted_software_version != $software_version & ";
            }
        }

        # Save remaining entries to array reference variables for later use
        if ($discard_reason eq "") {
            push @$output_ref, $hash_ref;
        } else {
            $discard_reason =~ s/ & $//;
            push @$discarded_ref, "  Array index $i is discarded because of $discard_reason\n$hash_details" if $discarded_ref;
        }
    }

    return 0;
}

#
# Read the input file and return the data in an array, with options to filter
# away certain data.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#
#    MANDATORY
#       "filename":
#           The file to read.
#           This parameter is mandatory.
#       "output-ref":
#           Hash reference of variable where the network configuration file
#           parameters will be stored.
#
#           This hash will look something like this:
#           %$output_ref{"name"}{"parameter"} = "value"
#
#           Example:
#           For '<access>' type tags
#               %$output_ref{"sc-1-ssh"}{"connection"} = "SSH"
#               %$output_ref{"sc-1-ssh"}{"username"} = "root"
#               %$output_ref{"sc-1-ssh"}{"password"} = "rootroot"
#               %$output_ref{"sc-1-ssh"}{"address"} = "10.87.229.52"
#           or for '<parameter>' type tags:
#               %$output_ref{"_gep_type"}{"type"} = "STRING"
#               %$output_ref{"_gep_type"}{"value"} = "5"
#               %$output_ref{"_gep_type"}{"description"} = "Type of GEP boards (Only value 5 is supported)"
#
#    OPTIONAL
#       "discarded-ref":
#           If specified it should be a reference to an array that will contain a
#           list of the names of the network config parameters that was discarded
#           by the validator reference.
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#       "include-ref":
#           If specified it should be a reference to an array that will contain a
#           list of Perl regular expressions for lines to include.
#           The check for the regular expressions will be applied to the line after
#           all attributes of the tag from the file has been combined into one line.
#           For example:
#
#               "include-ref" => [ 'name="sc_namespace"', 'name="sc_release_name"' ],
#
#           Lines not matching any of the list of regular expressions will be discarded.
#       "validator-ref":
#           If specified it should be a reference to a subroutine that will perform
#           validation of each found parameter which is delivered into the function
#           as a hash reference. The function should return a true value (>1) if the
#           parameter is valid and should be kept, or return a false value (=0) if
#           the parameter should be discarded and not be stored in the $output_ref
#           hash reference i.e. not returned to the caller.
#           Example of what this function might look like:
#
#           sub validate_network_config_parameter {
#               my $hash_ref = shift;
#
#               # If we don't have an SC_RELEASE_VERSION job variable return back that the parameter is valid
#               # since we cannot check it.
#               return 1 unless (exists $::JOB_PARAMS{'SC_RELEASE_VERSION'});
#
#               for my $key (keys %{$hash_ref}) {
#                   if (exists $hash_ref->{$key}{'valid_releases'} && $::JOB_PARAMS{'SC_RELEASE_VERSION'} !~ /$hash_ref->{$key}{'valid_releases'}/) {
#                       # This parameter is not valid for the current release, so return back false to ignore the parameter
#                       return 0;
#                   }
#               }
#
#               # The parameter seems to be valid
#               return 1;
#           }
#
# Example:
# my @temp = ();
# my $rc = General::File_Operations::parse_network_configuration_file(
#     {
#         "filename"            => "/path/to/file.xml",
#         "output-ref"          => \%temp,
#     }
# );
#
# Output:
#  - The hash is updated with all <access> and <parameter> XML tag values.
#
# Return code:
#  0: Successful parsing of network configuration file.
#  1: Failure to parse network configuration file.
sub parse_network_configuration_file {
    my %params = %{$_[0]};

    # Initialize local variables
    my $discarded_ref       = exists $params{"discarded-ref"} ? $params{"discarded-ref"} : undef;
    my $filename            = exists $params{"filename"} ? $params{"filename"} : undef;
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $include_ref         = exists $params{"include-ref"} ? $params{"include-ref"} : undef;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;
    my $validator_ref       = exists $params{"validator-ref"} ? $params{"validator-ref"} : undef;

    my @input = ();
    my $line;
    my @no_comments = ();
    my $rc;
    my %temp_hash;

    # Check that we have a file to read
    unless (defined $filename) {
        print "ERROR: You have to specify a filename to read\n" unless $hide_error_msg;
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        print "ERROR: You have to specify a hash variable to write data to\n" unless $hide_error_msg;
        return 1;
    }

    # Empty the output hash
    undef %$output_ref;

    # Read the Network Configuration XML file
    my @temp = ();
    $rc = read_file(
        {
            "filename"              => "$filename",
            "output-ref"            => \@input,
            "hide-error-messages"   => 1,
            "ignore-empty-lines"    => 1,
        }
    );
    if ($rc) {
        # Failure reading of file
        print "ERROR: Unable to open the Network Configuration file '$filename'\n" unless $hide_error_msg;
        return 1;
    }

    # Strip away all comments to make the file easier to read
    strip_xml_comment_lines(\@input, \@no_comments);

    # Put one XML tag per line
    one_xml_tag_per_line(\@no_comments, \@input);

    for (@input) {
        # Ignore empty lines
        next if (/^\s*$/);
        $line = $_;

        if (/^\s*<(access|parameter)\s+(.+)\/>\s*$/) {
            # Extract parameters
            if ($include_ref) {
                my $found = 0;
                for my $regexp (@{$include_ref}) {
                    next unless $regexp;
                    if ($line =~ /$regexp/) {
                        $found = 1;
                        last;
                    }
                }
                if ($found == 0) {
                    # None of the include lines matched, so ignore this parameter
                    push @$discarded_ref, "Discarded by the include-ref:  $line";
                    next;
                }
            }
            print "    Wanted: $_\n" if ($debug > 1);
            if ($validator_ref) {
                # Validation of parameter is needed before adding to $output_ref
                undef %temp_hash;
                extract_xml_tag_parameters($2, \%temp_hash);
                if (&$validator_ref(\%temp_hash)) {
                    # The parameter is valid and should be added to $output_ref
                    for my $key (keys %temp_hash) {
                        for my $subkey (keys %{$temp_hash{$key}}) {
                            $output_ref->{$key}{$subkey} = $temp_hash{$key}{$subkey};
                        }
                    }
                } else {
                    # The parameter is not valid and should be ignored
                    if ($discarded_ref) {
                        for my $key (keys %temp_hash) {
                            push @$discarded_ref, "Discarded by the validator:  $line";
                        }
                    }
                    next;
                }
            } else {
                # No validation is needed
                extract_xml_tag_parameters($2, $output_ref);
            }
        } else {
            # Not wanted parameters
            print "Not wanted: $_\n" if ($debug > 1);
            push @$discarded_ref, "Discarded due to the syntax: $line" if $discarded_ref;
            next;
        }
    }

    return 0;
}

#
# Read the input file and return the data in an array, with options to filter
# away certain data.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "filename":
#           The file to read.
#           This parameter is mandatory.
#       "output-ref":
#           Array reference of variable where the contents of the file is
#           returned to the caller.
#    OPTIONAL
#       "append-output-ref":
#           Append read lines from the file to the array reference given by the
#           "output-ref" parameter.
#           The default is to always empty the "output-ref" variable before
#           returning the file contents.
#       "hide-error-messages":
#           Hide error messages to the user, which means in case the file cannot
#           be found or opened then it's not visible to the user, other than the
#           return code indicates failure.
#           The default is to always show the error messages to the user.
#       "ignore-empty-lines":
#           Ignore empty lines, including lines containing just white space
#           like space and tab characters.
#           The default is to always include empty lines.
#       "ignore-pattern":
#           If specified then any line that matches the given Perl regular
#           expression string will be ignored and not returned to the user.
#       "include-pattern":
#           If specified then only lines that matches the given Perl regular
#           expression string will be returned to the user.
#       "keep-eol-char":
#           If specified and with value =1 then the End Of Line (EOL) characters,
#           which is normally CR and/or LF, will be kept.
#           The default is to always remove the EOL characters from the returned
#           data.
#
# Example:
# my @temp = ();
# my $rc = General::File_Operations::read_file(
#     {
#         "filename"            => "/path/to/file.txt",
#         "output-ref"          => \@temp,
#         "ignore-empty-lines"  => 1,
#     }
# );
#
# Output:
#  - The array is updated with all data from the file with CR and LF characters
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
    my $append_output_ref   = exists $params{"append-output-ref"} ? $params{"append-output-ref"} : 0;
    my $ignore_empty_lines  = exists $params{"ignore-empty-lines"} ? $params{"ignore-empty-lines"} : 0;
    my $ignore_pattern      = exists $params{"ignore-pattern"} ? $params{"ignore-pattern"} : "";
    my $include_pattern     = exists $params{"include-pattern"} ? $params{"include-pattern"} : "";
    my $keep_eol            = exists $params{"keep-eol-char"} ? $params{"keep-eol-char"} : 0;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;

    # Check that we have a file to read
    unless (defined $filename) {
        print "ERROR: You have to specify a filename to read\n" unless $hide_error_msg;
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        print "ERROR: You have to specify a array variable to write data to\n" unless $hide_error_msg;
        return 1;
    }

    # Empty the output array
    unless ($append_output_ref) {
        @$output_ref = ();
    }

    if (open INF, "$filename") {
        for (<INF>) {
            s/[\r\n]//g unless $keep_eol;
            if ($ignore_empty_lines) {
                next if /^\s*$/;
            }
            if ($ignore_pattern) {
                next if /$ignore_pattern/;
            }
            if ($include_pattern) {
                push @$output_ref, $_ if /$include_pattern/;
            } else {
                push @$output_ref, $_;
            }
        }
        if (close INF) {
            return 0;
        } else {
            print "ERROR: Unable to close the file '$filename'\n" unless $hide_error_msg;
            return 1;
        }
    } else {
        print "ERROR: Unable to open the file '$filename'\n" unless $hide_error_msg;
        return 1;
    }
}

#
# Replace any REPLACEMEWITH_xxxx values with the value from the variable pointer to
# with the xxxx name.
# This makes it possible to specify a value only once and then point to this value
# from other parameters.
# Update network configuration parameters in the "input-ref" hash reference where the
# 'value' key contains the string 'REPLACEMEWITH_xxxx'.
# If the parameter indicated by the 'xxxx' part does not exist then the parameter is
# removed.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input-ref":
#           Hash reference of variable containing the already parsed network
#           configuration file data that will be copied to the hash reference variable
#           specified with "output-ref".
#           This parameter is mandatory.
#    OPTIONAL
#       "discarded-ref":
#           If specified it should be a reference to an array that will contain a
#           list of the names of the network config parameters that was discarded.
#
# Example:
# my %NETWORK_CONFIG_PARAMS;
# my $rc = General::File_Operations::replace_network_configuration_variable_placeholders(
#     {
#         "input-ref"           => \%NETWORK_CONFIG_PARAMS,
#     }
# );
#
# Output:
#  - The input reference hash is updated.
#
# Return code:
#  -
sub replace_network_configuration_variable_placeholders {
    my %params = %{$_[0]};

    # Initialize local variables
    my $discarded_ref       = exists $params{"discarded-ref"} ? $params{"discarded-ref"} : undef;
    my $input_ref           = exists $params{"input-ref"} ? $params{"input-ref"} : undef;
    my %keys_to_delete;
    my $value;

    # Update the hash where the 'value' secondary key contains the value REPLACEMEWITH_xxxx
    for my $key (sort keys %{$input_ref}) {
        for my $key2 (sort keys %{$input_ref->{$key}}) {
            next unless ($key2 =~ /^value.*/);
            if (exists $input_ref->{$key}{$key2} && $input_ref->{$key}{$key2} =~ /^(.*)REPLACEMEWITH_(\w+)(.*)$/) {
                my $prefix =$1;
                my $parent_key = $2;
                my $suffix =$3;
                if (exists $input_ref->{$parent_key} && exists $input_ref->{$parent_key}{'value'}) {
                    print "DBG:Updating key: $key\n      from $key2: $input_ref->{$key}->{$key2}\n        to $key2: $prefix$input_ref->{$parent_key}{'value'}$suffix\n" if ($debug > 0);
                    $input_ref->{$key}->{$key2} = $prefix . $input_ref->{$parent_key}{'value'} . $suffix;
                } else {
                    # The parent key does not exist, so we just delete this whole parameter
                    print "DBG:Marking key $key to be deleted\n" if ($debug > 0);
                    $keys_to_delete{$key}++;
                    # The parameter is not valid and should be ignored
                    if ($discarded_ref) {
                        push @$discarded_ref, "Discarded variable '$key' because referenced variable value '$parent_key' does not exist";
                    }
                }
            }
        }
    }
    for my $key (sort keys %keys_to_delete) {
        print "DBG:Deleting key $key\n" if ($debug > 0);
        delete $input_ref->{$key};
    }
}

#
# Show the content of a file to the user on the screen.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "filename":
#           The file to read.
#           This parameter is mandatory.
#
#    OPTIONAL
#       -
#
# Example:
# my $rc = General::File_Operations::show_file_content(
#     {
#         "filename"            => "/path/to/file.txt",
#     }
# );
#
# Output:
#  -
#
# Return code:
#  0: Success, file was shown.
#  1: Failure to read the file.
sub show_file_content {
    my %params = %{$_[0]};

    # Initialize local variables
    my $filename            = exists $params{"filename"} ? $params{"filename"} : undef;

    # Check that we have a file to read
    unless (defined $filename) {
        General::Logging::log_user_error_message("You have to specify a filename to read");
        return 1;
    }

    # Empty the output array
    my @content = ();

    if ( read_file(
            {
                "filename"      => $filename,
                "output-ref"    => \@content,
            }
        ) == 0) {
        my $oneline = join "\n", @content;
        General::Logging::log_user_message($oneline);
    } else {
        return 1;
    }
}

#
# Strip XML comments from input array.
#
# Input:
#  - Input array reference
#  - Output array reference
#
# Output:
#  - The output array is updated with all found data.
#
sub strip_xml_comment_lines {
    my $in_array_ref = shift;
    my $out_array_ref = shift;

    my $inside_comment = 0;
    my $line;

    # Empty output array
    @$out_array_ref = ();

    for (@$in_array_ref) {
        s/[\r\n]//g;
        $line = $_;
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
                # Take next line, generate no output
                next;
            }
        }
        #
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
        push @$out_array_ref, $line;
    }
}

#
# Create a temporary file using the specified template or if not specified
# with the following template /tmp/DAFT_created_temporary_file_pidYYYY_XXXXXXXX
# where YYYY is the process id of the current running Perl instance.
#
# Input:
#  - Template path that should include a bunch of XXXX and the end, if not specified then
#    a default value will be used.
#    This can contain a directory path.
#
# Output:
#  - The path to the created temporary file or an empty string at failure.
#
sub tempfile_create {
    my $template = shift;
    my ($caller_package, $caller_filename, $caller_line) = caller(0);
    my ($junk1, $junk2, $junk3, $caller_subroutine) = caller(1);
    my $directory = "";
    my $rc;
    my @result;
    my $tempfile = "";

    # Set default value if no template was given
    unless ($template) {
        $template = "/tmp/DAFT_created_temporary_file_pid${$}_XXXXXXXX";
    }

    if ($template =~ /^(.+\/)(\S+)$/) {
        $directory = $1;
        $template = $2;
    }

    if ($template !~ /X{3,}/) {
        # Not enough X's in the template, at least 3 XXX in a row is needed
        return "";
    }

    if ($directory ne "") {
        unless ( -d "$directory") {
            # Directory does not exist, so create it
            $rc = General::OS_Operations::send_command(
                {
                    "command"           => "mkdir -p $directory",
                    "hide-output"       => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                return "";
            }
        }
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"           => "mktemp -p $directory $template",
            "hide-output"       => 1,
            "return-output"     => \@result,
        }
    );
    if ($rc == 0 && scalar @result == 1) {
        $tempfile = abs_path $result[0];
    } else {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        return "";
    }

    $created_tempfiles{$tempfile}{'status'} = 1;
    $created_tempfiles{$tempfile}{'created-by-file'} = $caller_filename;
    $created_tempfiles{$tempfile}{'created-by-line'} = $caller_line;
    $created_tempfiles{$tempfile}{'created-by-sub'} = $caller_subroutine;

    return $tempfile;
}

#
# Delete the specified temporary file that was previously created by calling
# tempfile_create.
#
# Input:
#  - The path to the temporary file to delete.
#
# Output:
#  -
#
sub tempfile_delete {
    my $tempfile = shift;
    my ($caller_package, $caller_filename, $caller_line) = caller(0);
    my ($junk1, $junk2, $junk3, $caller_subroutine) = caller(1);

    if (exists $created_tempfiles{$tempfile} && $created_tempfiles{$tempfile}{'status'} == 1) {
        if ( -f "$tempfile") {
            unlink $tempfile;
        }
        $created_tempfiles{$tempfile}{'status'} = 0;
        $created_tempfiles{$tempfile}{'deleted-by-file'} = $caller_filename;
        $created_tempfiles{$tempfile}{'deleted-by-line'} = $caller_line;
        $created_tempfiles{$tempfile}{'deleted-by-sub'} = $caller_subroutine;
    }
    return "";
}

#
# Delete all temporary files previously created by calling tempfile_create.
#
# Input:
#  -
#
# Output:
#  -
#
sub tempfile_delete_all {
    my ($caller_package, $caller_filename, $caller_line) = caller(0);
    my ($junk1, $junk2, $junk3, $caller_subroutine) = caller(1);

    for my $tempfile (keys %created_tempfiles) {
        if ($created_tempfiles{$tempfile}{'status'} == 1) {
            if ( -f "$tempfile") {
                unlink $tempfile;
            }
            $created_tempfiles{$tempfile}{'status'} = 0;
            $created_tempfiles{$tempfile}{'deleted-by-file'} = $caller_filename;
            $created_tempfiles{$tempfile}{'deleted-by-line'} = $caller_line;
            $created_tempfiles{$tempfile}{'deleted-by-sub'} = $caller_subroutine;
        }
    }
}

#
# Add network configuration parameters in the "input-ref" hash reference to the job
# parameters in the "output-ref" hash reference.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "input-ref":
#           Hash reference of variable containing the already parsed network
#           configuration file data that will be copied to the hash reference variable
#           specified with "output-ref".
#           This parameter is mandatory.
#       "output-ref":
#           Hash reference of variable containing the job variable data that will be
#           updated with the data from the network configuration data provided in the
#           hash reference variable specified with "input-ref".
#           This parameter is mandatory.
#
#    OPTIONAL
#       "discarded-ref":
#           If specified it should be a reference to an array that will contain a
#           list of the names of the network config parameters that was discarded.
#       "ignore-attributes":
#           If specified it should contain an array reference of attributes that SHOULD NOT be
#           copied from the "input-ref" to "output-ref".
#           The data should be in the following formats:
#
#               - <attribute name>
#                 Any attributes matching this name will be ignore.
#
#               - <attribute name>=<perl regexp>
#                 Any attribute where the name and the contents of the attribute match
#                 will be ignore.
#
#           For example: "ignore-attributes" => ["application_type", "description"]
#           will cause the values of attributes "application_type" and "description" not to be
#           copied.
#
# Example:
# my %NETWORK_CONFIG_PARAMS;
# my %JOB_PARAMS;
# my $rc = General::File_Operations::update_job_variables_with_network_configuration_variables(
#     {
#         "input-ref"           => \%::NETWORK_CONFIG_PARAMS,
#         "output-ref"          => \%::JOB_PARAMS,
#         "ignore-attributes"   => ["application_type", "description", "playlist", "reuse_deployed_helm_value", "valid_releases"],
#     }
# );
#
# Output:
#  - The output reference hash is updated with all data from the provided input reference hash.
#
# Return code:
#  -
sub update_job_variables_with_network_configuration_variables {
    my %params = %{$_[0]};

    # Initialize local variables
    my $discarded_ref       = exists $params{"discarded-ref"} ? $params{"discarded-ref"} : undef;
    my $input_ref           = exists $params{"input-ref"} ? $params{"input-ref"} : undef;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;
    my %ignore_attributes;
    my @ignore_attributes   = exists $params{"ignore-attributes"} ? @{$params{"ignore-attributes"}} : ();
    my $value;

    # Map the array into a hash for quicker access
    for $value (@ignore_attributes) {
        next unless $value;
        if ($value =~ /^([^=]+)=(.*)/) {
            $ignore_attributes{$1} = "$2";
        } else {
            $ignore_attributes{$value} = "";
        }
    }

    # Add network configuration file parameter to %JOB_PARAMS hash
    for my $key (sort keys %{$input_ref}) {
        for my $subkey (sort keys %{$input_ref->{$key}}) {
            $value = $input_ref->{$key}{$subkey};

            # Check if the attribute or attribute value should be ignored
            for my $attribute (sort keys %ignore_attributes) {
                if ($subkey =~ /$attribute/) {
                    # we have a potential match
                    if ($ignore_attributes{$attribute} eq "") {
                        # The attribute match, Ignore this attribute
                        if ($discarded_ref) {
                            push @$discarded_ref, "Job variable '$key' with attribute '$subkey' and any value is not stored because it should be ignored";
                        }
                        delete $output_ref->{"${key}_$subkey"} if (exists $output_ref->{"${key}_$subkey"});
                        goto NEXT_SUBKEY;
                    } elsif ($ignore_attributes{$attribute} ne "" && $value =~ /$ignore_attributes{$attribute}/) {
                        # The attribute and value match, Ignore this attribute
                        if ($discarded_ref) {
                            push @$discarded_ref, "Job variable '$key' with attribute '$subkey' and value '$ignore_attributes{$attribute}' is not stored because it should be ignored";
                        }
                        delete $output_ref->{"${key}_$subkey"} if (exists $output_ref->{"${key}_$subkey"});
                        goto NEXT_SUBKEY;
                    }
                    # The value part did not match, so still include this attribute
                    last;
                }
            }

            if ($subkey eq "value") {
                $output_ref->{$key} = $value;
            } else {
                $output_ref->{"${key}_$subkey"} = $value;
            }

            NEXT_SUBKEY:
        }
    }
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
#           Array reference of variable that contains the contents to be written
#           to the file. By default the array should not contain any EOL characters
#           because the function will automatically add them. But if the user wants
#           to add it themselves to the output-ref array then the function should
#           be called with the parameter "-no-eol-char".
#
#    OPTIONAL
#       "append-file":
#           If specified and if the file already exist then new data is appended
#           to the end of the file instead of as default overwrite the file.
#       "eol-char":
#           If specified then it should contain the EOL (End-Of-Line) character(s)
#           to use for each line.
#           The default is to use LF ("\n") as EOL character for each line which
#           is the standard UNIX format.
#           For DOS format pass in the eol-char as "\r\n".
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
#       "ignore-empty-lines":
#           Ignore empty lines, including lines containing just white space
#           like space and tab characters.
#           The default is to always include empty lines in written data.
#       "ignore-pattern":
#           If specified then any line that matches the given Perl regular
#           expression string will be ignored and not written to the file.
#           By default all lines will be written to the file.
#       "no-eol-char":
#           If specified then the user have to include EOL characters in the
#           output-ref because the function will not write any EOL characters to
#           the file.
#
# Example:
# my @temp = ();
# my $rc = General::File_Operations::write_file(
#     {
#         "filename"            => "/path/to/file.txt",
#         "output-ref"          => \@temp,
#         "eol-char"            => "\r\n",
#         "append-file"         => 1,
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
    my $append_file         = exists $params{"append-file"} ? $params{"append-file"} : 0;
    my $eol_char            = exists $params{"eol-char"} ? $params{"eol-char"} : "\n";
    my $filename            = exists $params{"filename"} ? $params{"filename"} : undef;
    my $file_access_mode    = exists $params{"file-access-mode"} ? $params{"file-access-mode"} : undef;
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $ignore_empty_lines  = exists $params{"ignore-empty-lines"} ? $params{"ignore-empty-lines"} : 0;
    my $ignore_pattern      = exists $params{"ignore-pattern"} ? $params{"ignore-pattern"} : "";
    my $no_eol_char         = exists $params{"no-eol-char"} ? $params{"no-eol-char"} : 0;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;

    # Check that we have a file to read
    unless (defined $filename) {
        print "ERROR: You have to specify a filename to read\n" unless $hide_error_msg;
        return 1;
    }
    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        print "ERROR: You have to specify a array variable with contents to write to file\n" unless $hide_error_msg;
        return 1;
    }

    if ($append_file) {
        unless (open OUTF, ">>$filename") {
            print "ERROR: Unable to append to the file '$filename'\n" unless $hide_error_msg;
            return 1;
        }
    } else {
        unless (open OUTF, ">$filename") {
            print "ERROR: Unable to create/clear the file '$filename'\n" unless $hide_error_msg;
            return 1;
        }
    }

    for (@$output_ref) {
        if ($ignore_empty_lines) {
            next if /^\s*$/;
        }
        if ($ignore_pattern) {
            next if /$ignore_pattern/;
        }
        if ($no_eol_char) {
            unless (print OUTF "$_") {
                print "ERROR: Unable to write to the file '$filename'\n" unless $hide_error_msg;
                return 1;
            }
        } else {
            s/\r*\n$//; # Any existing EOL character at the end
            unless (print OUTF "$_$eol_char") {
                print "ERROR: Unable to write to the file '$filename'\n" unless $hide_error_msg;
                return 1;
            }
        }
    }
    if (close OUTF) {
        if (defined $file_access_mode) {
            `chmod $file_access_mode $filename`;
            if ($?) {
                print "ERROR: Unable to change access mode '$file_access_mode' of the file '$filename'\n" unless $hide_error_msg;
                return 1;
            }
        }
        return 0;
    } else {
        print "ERROR: Unable to close the file '$filename'\n" unless $hide_error_msg;
        return 1;
    }
}

1;

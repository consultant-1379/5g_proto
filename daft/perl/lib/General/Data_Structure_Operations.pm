package General::Data_Structure_Operations;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.2
#  Date     : 2022-05-06 13:00:50
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2022
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
    compare_structure_against_allow_list
    dump_dot_separated_array_ref_to_stdout
    dump_dot_separated_array_ref_to_variable
    dump_dot_separated_hash_ref_to_stdout
    dump_dot_separated_hash_ref_to_variable
    dump_dot_separated_ref_to_stdout
    update_hash_values
    );

use boolean;
use General::Logging;

#
# Compares two Perl data sctructures against each other and can return two
# data structures where one includes the parts that matched and another the
# parts that did not match.
#
# A typical use case it to compare an alarm list (originally in JSON format
# that was converted to a Perl data structure) agains an 'allow' list which
# contains a similarly formatted list with alarms that should be allowed and
# not reported as a problem.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "allow-ref":
#           Array or Hash reference of variable which contains the Perl data
#           structure that should be compared against the "input-ref" data.
#           This can for example be a data structure that contains a reference
#           to an array or hashes, with multiple levels.
#           It can also be a reference to a hash of hashes or hash or arrays.
#
#           Both the "allow-ref" and "input-ref" data structures must look
#           alike e.g. array of hashes, otherwise an error will be reported or
#           matches will not be found.
#
#           TODO: Currently the subroutine does not yet support a reference to
#           and array of arrays.
#       "input-ref":
#           Array or Hash reference of variable which contains the Perl data
#           structure that should be compared against the "allow-ref" data.
#           This can for example be a data structure that contains a reference
#           to an array or hashes, with multiple levels.
#           It can also be a reference to a hash of hashes or hash or arrays.
#
#           Both the "allow-ref" and "input-ref" data structures must look
#           alike e.g. array of hashes, otherwise an error will be reported or
#           matches will not be found.
#
#           TODO: Currently the subroutine does not yet support a reference to
#           and array of arrays.
#    OPTIONAL
#       "debug-messages":
#           If this parameter is provided and set to =1 then extra debug messages
#           will be printed that might aid in trouble shooting why the result is
#           not as expected e.g. why records are not matched.
#           The default if not specified is to not print this information.
#       "hide-error-messages":
#           Hide error messages to the user, which means in case something goes
#           wrong then it's not visible to the user, other than the return code
#           indicates failure.
#           The default is to always show the error messages to the user.
#       "indent-str":
#           Contains the initial indentation (spaces at the beginning of the line)
#           for printed messages.
#           If not specified then the printed messages will start at the left
#           column of the line. This parameter is mostly used when there are
#           multiple levels of the data structure and debug messages are printed
#           to make it easier to see which data belongs together.
#       "no-color":
#           If specified and =1 then no colors are used for the messages.
#           If not specified or =0 then the color green is used to indicate
#           something that is OK or matched, color red for errors or where
#           something did not match, and color yellow for warnings like when
#           something has not been implemented yet.
#       "result-allowed-ref":
#           If specified then it will be used to pass back a data stucture of
#           data in the "input-ref" that matched the data in the "allow-ref".
#           If the "input-ref" and "allow-ref" e.g. contains a reference of an
#           array of hashes, then the "result-allowed-ref" will also contain
#           a reference to an array of hashes.
#
#           If not specified then the only way for the caller to know if all
#           matched or not or if an error was detected is to look at the
#           return code of this subroutine.
#       "result-not-allowed-ref":
#           If specified then it will be used to pass back a data stucture of
#           data in the "input-ref" that DID NOT match the data in the
#           "allow-ref".
#           If the "input-ref" and "allow-ref" e.g. contains a reference of an
#           array of hashes, then the "result-allowed-ref" will also contain
#           a reference to an array of hashes.
#
#           If not specified then the only way for the caller to know if all
#           matched or not or if an error was detected is to look at the
#           return code of this subroutine.
#
# Example:
#   $rc = General::Data_Structure_Operations::compare_structure_against_allow_list(
#       {
#           "allow-ref"                 => $alarms_allow_list,
#           "input-ref"                 => $alarms,
#           "debug-messages"            => $debug,
#           "result-allowed-ref"        => \@result_allowed,
#           "result-not-allowed-ref"    => \@result_not_allowed,
#           "no-color"                  => 0,
#       }
#   );
#
# Output:
#  - If specified with "result-allowed-ref" or "result-not-allowed-ref" then
#    data is returned there, otherwise only the return code indicates the result
#    of the comparison.
#
# Return code:
#    -  0: Successful compare, the whole structure matched.
#    - >0: This many mismatches was found.
#    - -1: Unsuccessful compare, some error occurred.similarally
sub compare_structure_against_allow_list {
    my %params = %{$_[0]};

    # Initialize local variables
    my $allow_ref               = exists $params{"allow-ref"} ? $params{"allow-ref"} : undef;
    my $debug_msg               = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $hide_error_msg          = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $indent_str              = exists $params{"indent-str"} ? $params{"indent-str"} : "";
    my $input_ref               = exists $params{"input-ref"} ? $params{"input-ref"} : undef;
    my $no_color                = exists $params{"no-color"} ? $params{"no-color"} : 0;
    my $result_allowed_ref      = exists $params{"result-allowed-ref"} ? $params{"result-allowed-ref"} : undef;
    my $result_not_allowed_ref  = exists $params{"result-not-allowed-ref"} ? $params{"result-not-allowed-ref"} : undef;

    my $color_green  = "\e[0m\e[32m";
    my $color_red    = "\e[0m\e[31m";
    my $color_reset  = "\e[0m";
    my $color_yellow = "\e[0m\e[33m";
    my $mismatch_cnt = 0;

    unless ($input_ref) {
        write_message("${indent_str}ERROR: You have to specify an 'input-ref'\n", $hide_error_msg);
        return -1;
    }

    unless ($allow_ref) {
        write_message("${indent_str}ERROR: You have to specify an 'allow-ref'\n", $hide_error_msg);
        return -1;
    }

    if ($no_color) {
        $color_green  = "";
        $color_red    = "";
        $color_reset  = "";
        $color_yellow = "";
    }

    my $allow_type = ref($allow_ref);
    my $input_type = ref($input_ref);
    if ($input_type ne $allow_type) {
        write_message("ERROR: The 'input-ref' (=$input_type) and 'allow-ref' (=$allow_type) must be of the same type\n", $hide_error_msg);
        return -1;
    }

    if ($input_type eq "ARRAY") {

        my @input_array = @{$input_ref};
        my @allow_array = @{$allow_ref};
        # For each index in the 'input_array' check if there is any index in the 'allow_array' where
        # all data matches with the data in the 'input_array'.
        #
        # For example if the 'input_array' is a list of alarms and the 'allow_array' contains a list
        # of alarms that should be allowed i.e. not seen as a problem. Then for each alarm from the
        # 'input_array' check if the conditions in one of the 'allow_array' match all conditions in
        # the allow_array index, if this is the case then this alarm should be ignored and not seen
        # as a problem. If not all conditions of the 'allow_array' index match and none of the
        # other 'allow_array' indexes match then this alarm should be considered as a problematic
        # alarm.
        my $i1_match_count = 0;
        for (my $i1=0; $i1<=$#input_array; $i1++) {
            write_message("\n${indent_str}Array 'input-ref' index $i1\n", !$debug_msg) if $debug_msg;
            my $i1_ref = $input_array[$i1];
            my $i1_type = ref($i1_ref);
            $i1_match_count = 0;
            if ($i1_type eq "HASH") {
                # Array of Hashes, $input_ref[]->{}
                # Go through the 'allow_array' and check if any of them match with the data in 'input_array'.
                for (my $a1=0; $a1<=$#allow_array; $a1++) {
                    write_message("\n${indent_str}  Array 'allow-ref' index $a1\n", !$debug_msg) if $debug_msg;
                    my $a1_ref = $allow_array[$a1];
                    my $a1_type = ref($a1_ref);
                    if ($a1_type ne "HASH") {
                        write_message("${indent_str}    ${color_red}ERROR: Incompatible 'allow-ref' type detected (=$a1_type) for array index $a1, only 'HASH' allowed${color_reset}\n", $hide_error_msg);
                        next;
                    }
                    my $a1_match_count = 0;
                    my $a1_key_count = 0;
                    # Array of Hashes, $allow_ref[]->{}
                    for my $a1_hash_key (sort keys %{$a1_ref}) {
                        $a1_key_count++;

                        # Now check if the current allow key is found in the input hash
                        if (exists $i1_ref->{$a1_hash_key}) {
                            if (ref($a1_ref->{$a1_hash_key})) {
                                # Another reference, e.g. HASH or ARRAY
                                if (ref($a1_ref->{$a1_hash_key}) ne ref($i1_ref->{$a1_hash_key})) {
                                    write_message((sprintf "%s    %sERROR: The key '%s' of 'input-ref' (=%s) and 'allow-ref' (=%s) must be of the same type%s\n", $indent_str, $color_red, $a1_hash_key, ref($i1_ref->{$a1_hash_key}), ref($a1_ref->{$a1_hash_key}), $color_reset), $hide_error_msg);
                                    next;
                                }
                                # Make a recursive call to the same subroutine to check the next structure
                                write_message((sprintf "${indent_str}    Key 'allow-ref'->[%d]->{%s}, type=%s\n", $a1, $a1_hash_key, ref($i1_ref->{$a1_hash_key})), !$debug_msg) if $debug_msg;
                                my $a1_mismatch_cnt = compare_structure_against_allow_list(
                                    {
                                        "allow-ref"         => $a1_ref->{$a1_hash_key},
                                        "input-ref"         => $i1_ref->{$a1_hash_key},
                                        "debug-messages"    => $debug_msg,
                                        "indent-str"        => "$indent_str      ",
                                    }
                                );
                                if ($a1_mismatch_cnt == 0) {
                                    $a1_match_count++;
                                }

                                next;
                            } else {
                                # Not a reference, most likely a SCALAR which we should compare with
                                write_message((sprintf "%s    Key 'allow-ref'->[%d]->{%s}='%s'\n", $indent_str, $a1, $a1_hash_key, $a1_ref->{$a1_hash_key}), !$debug_msg) if $debug_msg;

                                if ($i1_ref->{$a1_hash_key} =~ /^\[(.+)\]$/) {
                                    # The contents seems to be looking like an array, so we should split the data into individual
                                    # components and then check each against the allow list
                                    my $no_match = 0;
                                    for my $temp (split /,/, $1) {
                                        if ($temp !~ /$a1_ref->{$a1_hash_key}/) {
                                            $no_match = 1;
                                            last;
                                        }
                                    }
                                    if ($no_match == 0) {
                                        # It seems like all values matched the allow condition, so count it
                                        $a1_match_count++;
                                        write_message((sprintf "%s      %sKey 'allow-ref'->[%d]->{%s}='%s' match 'input-ref'->[%s]->{%s}='%s'%s\n", $indent_str, $color_green, $a1, $a1_hash_key, $a1_ref->{$a1_hash_key}, $i1, $a1_hash_key, $i1_ref->{$a1_hash_key}, $color_reset), !$debug_msg) if $debug_msg;
                                    } else {
                                        write_message((sprintf "%s      %sKey 'allow-ref'->[%d]->{%s}='%s' does not match 'input-ref'->[%s]->{%s}='%s'%s\n", $indent_str, $color_red, $a1, $a1_hash_key, $a1_ref->{$a1_hash_key}, $i1, $a1_hash_key, $i1_ref->{$a1_hash_key}, $color_reset), !$debug_msg) if $debug_msg;
                                    }
                                } elsif ($i1_ref->{$a1_hash_key} =~ /$a1_ref->{$a1_hash_key}/) {
                                    # This key matched the allow condition, so count it
                                    $a1_match_count++;
                                    write_message((sprintf "%s      %sKey 'allow-ref'->[%d]->{%s}='%s' match 'input-ref'->[%s]->{%s}='%s'%s\n", $indent_str, $color_green, $a1, $a1_hash_key, $a1_ref->{$a1_hash_key}, $i1, $a1_hash_key, $i1_ref->{$a1_hash_key}, $color_reset), !$debug_msg) if $debug_msg;
                                } else {
                                    write_message((sprintf "%s      %sKey 'allow-ref'->[%d]->{%s}='%s' does not match 'input-ref'->[%s]->{%s}='%s'%s\n", $indent_str, $color_red, $a1, $a1_hash_key, $a1_ref->{$a1_hash_key}, $i1, $a1_hash_key, $i1_ref->{$a1_hash_key}, $color_reset), !$debug_msg) if $debug_msg;
                                }
                                next;
                            }
                        } else {
                            # The key did not exist, check next key
                            #print "DBG:key '$i1_hash_key' not found in 'allow-ref' for array index $a1, skipped\n";
                            write_message((sprintf "%s      Key 'input-ref'->[%d]->{%s} not found, 'allow-ref'->[%d]->{%s} ignored\n", $indent_str, $i1, $a1_hash_key, $a1, $a1_hash_key), !$debug_msg) if $debug_msg;
                            next;
                        }
                    }
                    if ($a1_match_count > 0 && $a1_match_count == $a1_key_count) {
                        write_message((sprintf "%s    %sThe 'input-ref'->[%d] matched all %d keys from 'allow_ref'->[%d]%s\n", $indent_str, $color_green, $i1, $a1_match_count, $a1, $color_reset), !$debug_msg) if $debug_msg;
                        $i1_match_count++;
                    } else {
                        write_message((sprintf "%s    %sThe 'input-ref'->[%d] matched %d of %d from 'allow_ref'->[%d]%s\n", $indent_str, $color_reset, $i1, $a1_match_count, $a1_key_count, $a1, $color_reset), !$debug_msg) if $debug_msg;
                    }
                }
            } elsif ($i1_type eq "ARRAY") {
                # Array of Arrays, $input_ref[]->[]

                #
                # TODO: Add this logic when needed.
                #

                write_message("${indent_str}  ${color_yellow}WARNING: Not yet implemented i1=$i1, i1_type=$i1_type${color_reset}\n", $hide_error_msg);
                next;
            } else {
                write_message("${indent_str}  ${color_red}ERROR: Incompatible 'input-ref' type given (=$i1_type) for array index $i1, only 'ARRAY' or 'HASH' allowed${color_reset}\n", $hide_error_msg);
                next;
            }

            # Check if this 'input_ref' array index was found in 'allow_ref' array.
            if ($i1_match_count > 0) {
                write_message("\n${indent_str}  ${color_green}Array 'input-ref' index $i1 is in 'allow_ref' array${color_reset}\n", !$debug_msg) if $debug_msg;
                push @{$result_allowed_ref}, $i1_ref if ($result_allowed_ref);
            } else {
                write_message("\n${indent_str}  ${color_red}Array 'input-ref' index $i1 is not in 'allow_ref' array${color_reset}\n", !$debug_msg) if $debug_msg;
                push @{$result_not_allowed_ref}, $i1_ref if ($result_not_allowed_ref);
                $mismatch_cnt++;
            }
        }

    } elsif ($input_type eq "HASH") {

        my %input_hash = %{$input_ref};
        my %allow_hash = %{$allow_ref};
        # For each key in the 'input_hash' check if there is any key in the 'allow_hash' where
        # all data matches with the data in the 'input_hash'.
        #
        # For example if the 'input_array' is a list of alarms and the 'allow_array' contains a list
        # of alarms that should be allowed i.e. not seen as a problem. Then for each alarm from the
        # 'input_array' check if the conditions in one of the 'allow_array' match all conditions in
        # the allow_array index, if this is the case then this alarm should be ignored and not seen
        # as a problem. If not all conditions of the 'allow_array' index match and none of the
        # other 'allow_array' indexes match then this alarm should be considered as a problematic
        # alarm.
        my $i1_match_count = 0;
        my $i1_key_count = 0;
        for my $i1_hash_key (sort keys %input_hash) {
            write_message("\n${indent_str}Hash 'input-ref' key '$i1_hash_key'\n", !$debug_msg) if $debug_msg;
            $i1_key_count++;
            if (exists $allow_hash{$i1_hash_key}) {
                if (ref($input_hash{$i1_hash_key})) {
                    # Another reference, e.g. HASH or ARRAY, or possibly JSON::PP::Boolean
                    if (ref($input_hash{$i1_hash_key}) ne ref($allow_hash{$i1_hash_key})) {
                        write_message((sprintf "%s  %sERROR: The type of 'input-ref'->{%s} (=%s) and 'allow-ref'->{%s} (=%s) must be of the same type%s\n", $indent_str, $color_red, $i1_hash_key, ref($input_hash{$i1_hash_key}), $i1_hash_key, ref($allow_hash{$i1_hash_key}), $color_reset), $hide_error_msg);
                        next;
                    }
                    if (ref($input_hash{$i1_hash_key}) ne "JSON::PP::Boolean") {
                        # Make a recursive call to the same subroutine to check the next structure
                        write_message((sprintf "%s  Key 'input-ref'->{%s}, type=%s\n", $indent_str, $i1_hash_key, ref($input_hash{$i1_hash_key})), !$debug_msg) if $debug_msg;
                        my $i1_mismatch_cnt = compare_structure_against_allow_list(
                            {
                                "allow-ref"         => $allow_hash{$i1_hash_key},
                                "input-ref"         => $input_hash{$i1_hash_key},
                                "debug-messages"    => $debug_msg,
                                "indent-str"        => "$indent_str    ",
                            }
                        );
                        if ($i1_mismatch_cnt == 0) {
                            $i1_match_count++;
                        }
                        next;
                    }
                }
                # Not a reference, most likely a SCALAR which we should compare with
                write_message((sprintf "%s  Key 'input-ref'->{%s}='%s'\n", $indent_str, $i1_hash_key, $input_hash{$i1_hash_key}), !$debug_msg) if $debug_msg;

                if ($input_hash{$i1_hash_key} =~ /^\[(.+)\]$/) {
                    # The contents seems to be looking like an array, so we should split the data into individual
                    # components and then check each against the allow list
                    my $no_match = 0;
                    for my $temp (split /,/, $1) {
                        if ($temp !~ /$allow_hash{$i1_hash_key}/) {
                            $no_match = 1;
                            last;
                        }
                    }
                    if ($no_match == 0) {
                        # It seems like all values matched the allow condition, so count it
                        $i1_match_count++;
                        write_message((sprintf "%s      %sKey 'allow-ref'->{%s}='%s' match 'input-ref'->{%s}='%s'%s\n", $indent_str, $color_green, $i1_hash_key, $allow_hash{$i1_hash_key}, $i1_hash_key, $input_hash{$i1_hash_key}, $color_reset), !$debug_msg) if $debug_msg;
                    } else {
                        write_message((sprintf "%s      %sKey 'allow-ref'->{%s}='%s' does not match 'input-ref'->{%s}='%s'%s\n", $indent_str, $color_red, $i1_hash_key, $allow_hash{$i1_hash_key}, $i1_hash_key, $input_hash{$i1_hash_key}, $color_reset), !$debug_msg) if $debug_msg;
                    }
                } elsif ($input_hash{$i1_hash_key} =~ /$allow_hash{$i1_hash_key}/) {
                    # This key matched the allow condition, so count it
                    $i1_match_count++;
                    write_message((sprintf "%s      %sKey 'allow-ref'->{%s}='%s' match 'input-ref'->{%s}='%s'%s\n", $indent_str, $color_green, $i1_hash_key, $allow_hash{$i1_hash_key}, $i1_hash_key, $input_hash{$i1_hash_key}, $color_reset), !$debug_msg) if $debug_msg;
                } else {
                    write_message((sprintf "%s      %sKey 'allow-ref'->{%s}='%s' does not match 'input-ref'->{%s}='%s'%s\n", $indent_str, $color_red, $i1_hash_key, $allow_hash{$i1_hash_key}, $i1_hash_key, $input_hash{$i1_hash_key}, $color_reset), !$debug_msg) if $debug_msg;
                }
                next;
            }
        }

        if ($i1_match_count > 0 && $i1_match_count == $i1_key_count) {
            write_message((sprintf "%s    %sThe 'input-ref' matched all %d keys from 'allow_ref'%s\n", $indent_str, $color_green, $i1_match_count, $color_reset), !$debug_msg) if $debug_msg;
            %{$result_allowed_ref} = %input_hash if ($result_allowed_ref);
        } else {
            write_message((sprintf "%s    %sThe 'input-ref' matched %d of %d keys from 'allow_ref'%s\n", $indent_str, $color_reset, $i1_match_count, $i1_key_count, $color_reset), !$debug_msg) if $debug_msg;
            %{$result_not_allowed_ref} = %input_hash if ($result_not_allowed_ref);
            $mismatch_cnt++;
        }
    } else {

        write_message("${indent_str}${color_red}ERROR: Incompatible 'input-ref' type given (=$input_type), only 'ARRAY' or 'HASH' allowed${color_reset}\n", $hide_error_msg);
        return -1;

    }

    return $mismatch_cnt;
}

#
# ***********************************************************************************
# * NOTE:                                                                           *
# * Deprecated function, please use the "dump_dot_separated_ref_to_stdout" instead. *
# ***********************************************************************************
#
# Print in-memory array reference into a dot (.) separated line, depth first, to STDOUT.
# This will traverse the array reference, depth first as a recursive call, separating
# each level with a dot until it reaches the end that contains a value which is printed
# to STDOUT, then it traverse back up the recursion tree to process other data, if existing.
#
# It will e.g. dump the parsed JSON/YAML data from calling e.g.:
#   - General::Json_Operations::read_file()
#   - General::Json_Operations::read_array()
#   - General::Yaml_Operations::read_file()
#   - General::Yaml_Operations::read_array()
# into something that looks like this:
#   spec.worker.tag=1.3.0-61
#   spec.worker.tlsport=443
#   spec.worker.tolerations.0.effect=NoExecute
#   spec.worker.tolerations.0.key=node.kubernetes.io/not-ready
#
# Input:
#    - Array reference of array variable to dump into a dot separated line.
#    - Prefix to add in front of the printed data (on first call this normally would
#      be an empty string).
#
# Output:
#    - The data is printed to STDOUT.
#
# Example:
# my @array;
# # Add data to the array
# General::Data_Structure_Operations::dump_dot_separated_array_ref_to_stdout(\@array, "");
#
# Return code:
#    -
sub dump_dot_separated_array_ref_to_stdout {
    my $arrayref = shift;
    my $prefix = shift;
    my $prefix_orig = $prefix;
    my $type;

    # To avoid warnings for using undefined values in push statements because
    # the json/yaml data contains nothing after the = we disable the warnings for now.
    no warnings;
    for (my $i=0; $i < scalar @$arrayref; $i++) {
        $type = ref(@$arrayref[$i]);
        if ($type eq "HASH") {
            $prefix = "$prefix.$i.";
            dump_dot_separated_hash_ref_to_stdout(@$arrayref[$i], $prefix);
            $prefix = $prefix_orig;
        } elsif ($type eq "ARRAY") {
            $prefix = "$prefix";
            dump_dot_separated_array_ref_to_stdout(@$arrayref[$i], $prefix);
            $prefix = $prefix_orig;
        } else {
            # Not a hash or array, i.e. a value
            if ($type eq "boolean") {
                printf "%s%s=%s\n", $prefix, ".$i", (@$arrayref[$i] == 1 ? "true" : "false");
            } else {
                printf "%s%s=%s\n", $prefix, ".$i", @$arrayref[$i];
            }
        }
    }
    use warnings;
}

#
# *************************************************************************************
# * NOTE:                                                                             *
# * Deprecated function, please use the "dump_dot_separated_ref_to_variable" instead. *
# *************************************************************************************
#
# Print in-memory array reference into a dot (.) separated line, depth first, to an
# array variable.
# This will traverse the array reference, depth first as a recursive call, separating
# each level with a dot until it reaches the end that contains a value which is printed
# to an array variable, then it traverse back up the recursion tree to process other
# data, if existing.
#
# It will e.g. dump the parsed JSON/YAML data from calling e.g.:
#   - General::Json_Operations::read_file()
#   - General::Json_Operations::read_array()
#   - General::Yaml_Operations::read_file()
#   - General::Yaml_Operations::read_array()
# into something that looks like this:
#   spec.worker.tag=1.3.0-61
#   spec.worker.tlsport=443
#   spec.worker.tolerations.0.effect=NoExecute
#   spec.worker.tolerations.0.key=node.kubernetes.io/not-ready
#
# Input:
#    - Array reference of array variable to dump into a dot separated line.
#    - Prefix to add in front of the printed data (on first call this normally would
#      be an empty string).
#    - Array reference of array variable where the output will be written.
#
# Output:
#    - The data is printed to the specified array variable.
#
# Example:
# my @array;
# # Add data to the array
# General::Data_Structure_Operations::dump_dot_separated_array_ref_to_stdout(\@array, "");
#
# Return code:
#    -
sub dump_dot_separated_array_ref_to_variable {
    my $arrayref = shift;
    my $prefix = shift;
    my $outputref = shift;
    my $prefix_orig = $prefix;
    my $type;

    # To avoid warnings for using undefined values in push statements because
    # the json/yaml data contains nothing after the = we disable the warnings for now.
    no warnings;
    for (my $i=0; $i < scalar @$arrayref; $i++) {
        $type = ref(@$arrayref[$i]);
        if ($type eq "HASH") {
            $prefix = "$prefix.$i.";
            dump_dot_separated_hash_ref_to_variable(@$arrayref[$i], $prefix, $outputref);
            $prefix = $prefix_orig;
        } elsif ($type eq "ARRAY") {
            $prefix = "$prefix";
            dump_dot_separated_array_ref_to_variable(@$arrayref[$i], $prefix, $outputref);
            $prefix = $prefix_orig;
        } else {
            # Not a hash or array, i.e. a value
            if ($type eq "boolean") {
                push @$outputref, sprintf "%s%s=%s", $prefix, ".$i", (@$arrayref[$i] == 1 ? "true" : "false");
            } else {
                push @$outputref, sprintf "%s%s=%s", $prefix, ".$i", @$arrayref[$i];
            }
        }
    }
    use warnings;
}

#
# ***********************************************************************************
# * NOTE:                                                                           *
# * Deprecated function, please use the "dump_dot_separated_ref_to_stdout" instead. *
# ***********************************************************************************
#
# Print in-memory hash reference into a dot (.) separated line, depth first, to STDOUT.
# This will traverse the hash reference, depth first as a recursive call, separating
# each level with a dot until it reaches the end that contains a value which is printed
# to STDOUT, then it traverse back up the recursion tree to process other data, if existing.
#
# It will e.g. dump the parsed JSON/YAML data from calling e.g.:
#   - General::Json_Operations::read_file()
#   - General::Json_Operations::read_array()
#   - General::Yaml_Operations::read_file()
#   - General::Yaml_Operations::read_array()
# into something that looks like this:
#   spec.worker.tag=1.3.0-61
#   spec.worker.tlsport=443
#   spec.worker.tolerations.0.effect=NoExecute
#   spec.worker.tolerations.0.key=node.kubernetes.io/not-ready
#
# Input:
#    - Hash reference of hash variable to dump into a dot separated line.
#    - Prefix to add in front of the printed data (on first call this normally would
#      be an empty string).
#
# Output:
#    - The data is printed to STDOUT.
#
# Example:
# my %hash;
# # Add data to hash
# General::Data_Structure_Operations::dump_dot_separated_hash_ref_to_stdout(\%hash, "");
#
# Return code:
#    -
sub dump_dot_separated_hash_ref_to_stdout {
    my $hashref = shift;
    my $prefix = shift;
    my $prefix_orig = $prefix;
    my $type;

    # To avoid warnings for using undefined values in push statements because
    # the json/yaml data contains nothing after the = we disable the warnings for now.
    no warnings;
    for my $key (sort keys %$hashref) {
        $type = ref($hashref->{$key});
        if ($type eq "HASH") {
            $prefix = "$prefix$key.";
            dump_dot_separated_hash_ref_to_stdout($hashref->{$key}, $prefix);
            $prefix = $prefix_orig;
        } elsif ($type eq "ARRAY") {
            $prefix = "$prefix$key";
            dump_dot_separated_array_ref_to_stdout($hashref->{$key}, $prefix);
            $prefix = $prefix_orig;
        } else {
            # Not a hash or array, i.e. a value
            if ($type eq "boolean") {
                printf "%s%s=%s\n", $prefix, $key, ($hashref->{$key} == 1 ? "true" : "false");
            } else {
                printf "%s%s=%s\n", $prefix, $key, $hashref->{$key};
            }
        }
    }
    use warnings;
}

#
# *************************************************************************************
# * NOTE:                                                                             *
# * Deprecated function, please use the "dump_dot_separated_ref_to_variable" instead. *
# *************************************************************************************
#
# Print in-memory hash reference into a dot (.) separated line, depth first, to STDOUT.
# This will traverse the hash reference, depth first as a recursive call, separating
# each level with a dot until it reaches the end that contains a value which is printed
# to STDOUT, then it traverse back up the recursion tree to process other data, if existing.
#
# It will e.g. dump the parsed JSON/YAML data from calling e.g.:
#   - General::Json_Operations::read_file()
#   - General::Json_Operations::read_array()
#   - General::Yaml_Operations::read_file()
#   - General::Yaml_Operations::read_array()
# into something that looks like this:
#   spec.worker.tag=1.3.0-61
#   spec.worker.tlsport=443
#   spec.worker.tolerations.0.effect=NoExecute
#   spec.worker.tolerations.0.key=node.kubernetes.io/not-ready
#
# Input:
#    - Hash reference of hash variable to dump into a dot separated line.
#    - Prefix to add in front of the printed data (on first call this normally would
#      be an empty string).
#    - Array reference of array variable where the output will be written.
#
# Output:
#    - The data is printed to the specified array variable.
#
# Example:
# my %hash;
# # Add data to hash
# General::Data_Structure_Operations::dump_dot_separated_hash_ref_to_stdout(\%hash, "");
#
# Return code:
#    -
sub dump_dot_separated_hash_ref_to_variable {
    my $hashref = shift;
    my $prefix = shift;
    my $outputref = shift;
    my $prefix_orig = $prefix;
    my $type;

    # To avoid warnings for using undefined values in push statements because
    # the json/yaml data contains nothing after the = we disable the warnings for now.
    no warnings;
    for my $key (sort keys %$hashref) {
        $type = ref($hashref->{$key});
        if ($type eq "HASH") {
            $prefix = "$prefix$key.";
            dump_dot_separated_hash_ref_to_variable($hashref->{$key}, $prefix, $outputref);
            $prefix = $prefix_orig;
        } elsif ($type eq "ARRAY") {
            $prefix = "$prefix$key";
            dump_dot_separated_array_ref_to_variable($hashref->{$key}, $prefix, $outputref);
            $prefix = $prefix_orig;
        } else {
            # Not a hash or array, i.e. a value
            if ($type eq "boolean") {
                push @$outputref, sprintf "%s%s=%s", $prefix, $key, ($hashref->{$key} == 1 ? "true" : "false");
            } else {
                push @$outputref, sprintf "%s%s=%s", $prefix, $key, $hashref->{$key};
            }
        }
    }
    use warnings;
}

#
# Print in-memory array or hash reference into a dot (.) separated line, depth first, to STDOUT.
# This will traverse the array or hash reference, depth first as a recursive call, separating
# each level with a dot until it reaches the end that contains a value which is printed
# to STDOUT, then it traverse back up the recursion tree to process other data, if existing.
#
# It will e.g. dump the parsed JSON/YAML data from calling e.g.:
#   - General::Json_Operations::read_file()
#   - General::Json_Operations::read_array()
#   - General::Yaml_Operations::read_file()
#   - General::Yaml_Operations::read_array()
# into something that looks like this:
#   spec.worker.tag=1.3.0-61
#   spec.worker.tlsport=443
#   spec.worker.tolerations.0.effect=NoExecute
#   spec.worker.tolerations.0.key=node.kubernetes.io/not-ready
#
# Input:
#    - Array or hash reference of array or hash variable to dump into a dot separated line.
#    - Prefix to add in front of the printed data (on first call this normally would
#      be an empty string).
#
# Output:
#    - The data is printed to STDOUT.
#
# Example:
# my @array;
# # Add data to the array
# General::Data_Structure_Operations::dump_dot_separated_ref_to_stdout(\@array, "");
# General::Data_Structure_Operations::dump_dot_separated_ref_to_stdout($ref_to_array, "");
#  or
# General::Data_Structure_Operations::dump_dot_separated_ref_to_stdout(\%hash, "");
# General::Data_Structure_Operations::dump_dot_separated_ref_to_stdout($ref_to_hash, "");
#
# Return code:
#    -
sub dump_dot_separated_ref_to_stdout {
    my $array_hash_ref = shift;
    my $prefix = shift;
    my $prefix_orig = $prefix;
    my $type;

    # To avoid warnings for using undefined values in push statements because
    # the json/yaml data contains nothing after the = we disable the warnings for now.
    no warnings;
    $type = ref($array_hash_ref);
    if ($type eq "ARRAY") {
        for (my $i=0; $i < scalar @$array_hash_ref; $i++) {
            $type = ref(@$array_hash_ref[$i]);
            if ($type eq "HASH") {
                $prefix = "$prefix.$i.";
                dump_dot_separated_ref_to_stdout(@$array_hash_ref[$i], $prefix);
                $prefix = $prefix_orig;
            } elsif ($type eq "ARRAY") {
                $prefix = "$prefix";
                dump_dot_separated_ref_to_stdout(@$array_hash_ref[$i], $prefix);
                $prefix = $prefix_orig;
            } else {
                # Not a hash or array, i.e. a value
                $prefix =~ s/^\.(\d+)\./$1./;   # Remove initial . if followed by a number e.g. ".0.text=value" becomes "0.text=value"
                if ($type eq "boolean") {
                    printf "%s%s=%s\n", $prefix, ".$i", (@$array_hash_ref[$i] == 1 ? "true" : "false");
                } else {
                    printf "%s%s=%s\n", $prefix, ".$i", @$array_hash_ref[$i];
                }
            }
        }
    } elsif ($type eq "HASH") {
        for my $key (sort keys %$array_hash_ref) {
            $type = ref($array_hash_ref->{$key});
            if ($type eq "HASH") {
                $prefix = "$prefix$key.";
                dump_dot_separated_ref_to_stdout($array_hash_ref->{$key}, $prefix);
                $prefix = $prefix_orig;
            } elsif ($type eq "ARRAY") {
                $prefix = "$prefix$key";
                dump_dot_separated_ref_to_stdout($array_hash_ref->{$key}, $prefix);
                $prefix = $prefix_orig;
            } else {
                # Not a hash or array, i.e. a value
                $prefix =~ s/^\.(\d+)\./$1./;   # Remove initial . if followed by a number e.g. ".0.text=value" becomes "0.text=value"
                if ($type eq "boolean") {
                    printf "%s%s=%s\n", $prefix, $key, ($array_hash_ref->{$key} == 1 ? "true" : "false");
                } else {
                    printf "%s%s=%s\n", $prefix, $key, $array_hash_ref->{$key};
                }
            }
        }
    } else {
        write_message("Unknown type '$type' detected\n", 0);
    }
    use warnings;
}

#
# Print in-memory array or hash reference into a dot (.) separated line, depth first, to a
# specified array variable.
# This will traverse the array or hash reference, depth first as a recursive call, separating
# each level with a dot until it reaches the end that contains a value which is printed
# to a specified array variable, then it traverse back up the recursion tree to process other
# data, if existing.
#
# It will e.g. dump the parsed JSON/YAML data from calling e.g.:
#   - General::Json_Operations::read_file()
#   - General::Json_Operations::read_array()
#   - General::Yaml_Operations::read_file()
#   - General::Yaml_Operations::read_array()
# into something that looks like this:
#   spec.worker.tag=1.3.0-61
#   spec.worker.tlsport=443
#   spec.worker.tolerations.0.effect=NoExecute
#   spec.worker.tolerations.0.key=node.kubernetes.io/not-ready
#
# Input:
#    - Array or hash reference of array or hash variable to dump into a dot separated line.
#    - Prefix to add in front of the printed data (on first call this normally would
#      be an empty string).
#    - Array reference of array variable where the output will be written.
#
# Output:
#    - The data is stored in the specified array variable.
#
# Example:
# my @array;
# my %hash;
# my @output_array;
# # Add data to the array
# General::Data_Structure_Operations::dump_dot_separated_ref_to_variable(\@array, "", \@output_array);
#  or
# General::Data_Structure_Operations::dump_dot_separated_ref_to_variable(\%hash, "", \@output_array);
#
# Return code:
#    -
sub dump_dot_separated_ref_to_variable {
    my $array_hash_ref = shift;
    my $prefix = shift;
    my $outputref = shift;
    my $prefix_orig = $prefix;
    my $type;

    # To avoid warnings for using undefined values in push statements because
    # the json/yaml data contains nothing after the = we disable the warnings for now.
    no warnings;
    $type = ref($array_hash_ref);
    if ($type eq "ARRAY") {
        for (my $i=0; $i < scalar @$array_hash_ref; $i++) {
            $type = ref(@$array_hash_ref[$i]);
            if ($type eq "HASH") {
                $prefix = "$prefix.$i.";
                dump_dot_separated_ref_to_variable(@$array_hash_ref[$i], $prefix, $outputref);
                $prefix = $prefix_orig;
            } elsif ($type eq "ARRAY") {
                $prefix = "$prefix";
                dump_dot_separated_ref_to_variable(@$array_hash_ref[$i], $prefix), $outputref;
                $prefix = $prefix_orig;
            } else {
                # Not a hash or array, i.e. a value
                $prefix =~ s/^\.(\d+)\./$1./;   # Remove initial . if followed by a number e.g. ".0.text=value" becomes "0.text=value"
                if ($type eq "boolean") {
                    push @$outputref, sprintf "%s%s=%s\n", $prefix, ".$i", (@$array_hash_ref[$i] == 1 ? "true" : "false");
                } else {
                    push @$outputref, sprintf "%s%s=%s\n", $prefix, ".$i", @$array_hash_ref[$i];
                }
            }
        }
    } elsif ($type eq "HASH") {
        for my $key (sort keys %$array_hash_ref) {
            $type = ref($array_hash_ref->{$key});
            if ($type eq "HASH") {
                $prefix = "$prefix$key.";
                dump_dot_separated_ref_to_variable($array_hash_ref->{$key}, $prefix, $outputref);
                $prefix = $prefix_orig;
            } elsif ($type eq "ARRAY") {
                $prefix = "$prefix$key";
                dump_dot_separated_ref_to_variable($array_hash_ref->{$key}, $prefix, $outputref);
                $prefix = $prefix_orig;
            } else {
                # Not a hash or array, i.e. a value
                $prefix =~ s/^\.(\d+)\./$1./;   # Remove initial . if followed by a number e.g. ".0.text=value" becomes "0.text=value"
                if ($type eq "boolean") {
                    push @$outputref, sprintf "%s%s=%s\n", $prefix, $key, ($array_hash_ref->{$key} == 1 ? "true" : "false");
                } else {
                    push @$outputref, sprintf "%s%s=%s\n", $prefix, $key, $array_hash_ref->{$key};
                }
            }
        }
    } else {
        write_message("Unknown type '$type' detected\n", 0);
    }
    use warnings;
}

#
# Update (nested) hash with specific value.
# Subroutine copied from the Deep::Hash::Utils module from CPAN.
#
# Updates to the copied code to support changing of array references
# as well.
# And to properly handle boolean values to make sure they are written
# as true or false objects instead of 'true' or 'false'.
#
# Input:
#    - Hash or array reference of variable to be updated, the referenced variable
#      should either contain a hash of hashes of hashes, or hash of arrays of hashes
#      etc., or an array of hashes of hashes, or array of hashes of arrays of hashes
#      etc.
#    - Array with values to update the hash with.
#      if the hash looks something like this:
#      hash{node1}{node2}{node3}{node4}{node5}=value6
#      And you want to change the "value6" into something different
#      e.g. "value7" then the array should contain the following:
#      array[0]="node1"
#      array[1]="node2"
#      array[2]="node3"
#      array[3]="node4"
#      array[4]="node5"
#      array[5]="value7"
#
# Example:
# my %hash;
# my @temp = ( "key1", "key2", "key3", "value" );
# General::Data_Structure_Operations::nest(\%hash, @temp);
#
# Output:
#  - The hash is updated with data.
#
# Return code:
#  -
sub nest {
    my $hr = shift;
    my $key = shift;
    my $next_key;
    my $type = ref $hr;
    my $ref;
    if ($type eq "HASH") {
        $hr->{$key} ||= {};
        $ref = $hr->{$key};
    } elsif ($type eq "ARRAY") {
        $hr ||= [];
        $ref = $hr->[$key];
    } else {
        $hr->{$key} ||= {};
        $ref = $hr->{$key};
    }
    my $value;

    while (@_) {
        $key = shift @_;
        $hr = $ref;
        $type = ref $hr;
        if (@_ > 1) {
            # Find next key, needed to know if we need to initialize a hash or array reference
            if (@_) {
                $next_key = $_[0];
            } else {
                $next_key = "";
            }
            if ($type eq "HASH") {
                if ($next_key =~ /^\d+$/) {
                    # Next key looks like an array index, so initialize as an array reference if not already existing
                    $hr->{$key} ||= [];
                } else {
                    # Next key looks like an hash key, so initialize as a hash reference if not already existing
                    $hr->{$key} ||= {};
                }
                $ref = $hr->{$key};
            } elsif ($type eq "ARRAY" && $key =~ /^\d+$/) {
                if (defined $hr->[$key]) {
                    $ref = $hr->[$key];
                } else {
                    if ($next_key =~ /^\d+$/) {
                        # Next key looks like another array index, so initialize as an array reference
                        $hr->[$key] = [];
                    } else {
                        # Next key looks like an hash key, so initialize as a hash reference
                        $hr->[$key] = {};
                    }
                    $ref = $hr->[$key];
                }
            } else {
                write_message("Unknown type '$type' detected, key '$key' is ignored\n", 0);
            }
        } else {
            # End of the line, Write the value with special handling for boolean types.
            $value = shift;
            if ($value =~ /^true$/i) {
                $hr->{$key} = boolean::true;
            } elsif ($value =~ /^false$/i) {
                $hr->{$key} = boolean::false;
            } else {
                $hr->{$key} = $value;
            }
        }
    }
    return $hr;
}

#
# Update the hash reference passed in the "output-ref" variable with values passed
# in the "values" array.
#
# Input:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#    MANDATORY
#       "output-ref":
#           Hash reference of variable which should be updated with values
#           passed in the "values" array.
#           This parameter is mandatory.
#       "values":
#           The values to be updated in the hash reference, this is passed as
#           a reference to an array with the following format:
#           <key path>=<value>
#           Where <key path> is of the format <key1.key2.key3> and <value> is the
#           value to write into the last key.
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
# my $rc = General::Data_Structure_Operations::update_hash_values(
#     {
#         "output-ref"          => \%hash,
#         "values"              => \@values,
#     }
# );
#
# Output:
#  - The hash reference is updated with updated or new values.
#
# Return code:
#  0: Success, hash was updated.
#  1: Failure to update the hash.
sub update_hash_values {
    my %params = %{$_[0]};

    # Initialize local variables
    my $hide_error_msg      = exists $params{"hide-error-messages"} ? $params{"hide-error-messages"} : 0;
    my $output_ref          = exists $params{"output-ref"} ? $params{"output-ref"} : undef;
    my $values_ref          = exists $params{"values"} ? $params{"values"} : undef;

    my $rc = 0;
    my @values = ();

    # Check that we have a variable to return data to
    unless (defined $output_ref) {
        write_message("ERROR: You have to specify a hash variable to update\n", $hide_error_msg);
        return 1;
    }
    # Check that we have an array to read values from
    unless (defined $values_ref) {
        write_message("ERROR: You have to specify an array reference with values to update the hash with\n", $hide_error_msg);
        return 1;
    }

    # Split the values into an array where each part of the value is stored into
    # a separate array which is then used to update the hash reference.
    for my $value (@$values_ref) {
        @values = ();
        if ($value =~ /^(.+?)=(.*)$/) {
            @values = split /\./, $1;
            push @values, $2;
        } else {
            write_message("ERROR: Wrong format of data '$value'\n", $hide_error_msg);
            return 1;
        }

        if (scalar @values < 2) {
            write_message("ERROR: Not enough data in the data '$value'\n", $hide_error_msg);
            return 1;
        }

        # Update the hash with the value
        nest($output_ref, @values);
    }

    return $rc;
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

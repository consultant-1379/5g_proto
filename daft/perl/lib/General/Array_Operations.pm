package General::Array_Operations;

################################################################################
#
#  Author   : everhel
#
#  Revision : 1.1
#  Date     : 2017-04-24 18:30:35
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2016
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

our @EXPORT_OK = qw(find_pattern get_longest_string remove_header_and_footer);

#
#
# Input variables:
#
# Return values:
#
sub find_pattern {
    my $index = 0;
    my $message = "";
    my $messages = shift;
    my $pattern;
    my $pattern_matches = shift;
    my @patterns_found;
    my $regex;
    my $routine_name = "find_pattern_and_remove";

    $regex = generate_regex_from_tags($pattern_matches);

    foreach $message (@{$messages}) {
        if ($message =~ m/$regex/g) {
            foreach $pattern (@{$pattern_matches}) {
                if ($message =~ /$pattern/) {
                    push @patterns_found, $message;
                }
            }
        }
        $index++;
    }

    return @patterns_found;
}

#
# Generate regular expression from known tags, map used here to easily walk the
# provided array and apply quotemeta to on each sting escaping all non ASCII characters
# before joining indexes to create regex
#
# Input variables:
#    array reference $pattern_matches = array containing strings to match
#
# Return values:
#    regular expression $regex = generated regex to be used
#
sub generate_regex_from_tags {
    my $pattern_matches = shift;
    my $regex;
    my $routine_name = "generate_regex_from_tags";

    $regex = join '|', map{quotemeta} @{$pattern_matches};
    # Apply regex like quotes
    $regex = qr/($regex)/;

    return $regex;
}

#
# Go through array and provided strings and return the longest
# string found
#
# Input variables:
#    array reference $messages = provided array in which to do operations
#    string $footer_text = footer text to get the length of
#    string $header_text = header text to get the length of
#
# Return values:
#    int $message_longest = longest string found in provided array and variables
#
sub get_longest_string {
    my $message = "";
    my $messages = shift;
    my $footer_text = shift;
    my $header_text = shift;
    my $message_length = 0;
    my $message_longest = 0;
    my $routine_name = "get_longest_string";

    if ($footer_text) {
        $message_length = length $footer_text;

        if ($message_length > $message_longest) {
            $message_longest = $message_length;
        }
    }

    if ($header_text) {
        $message_length = length $header_text;

        if ($message_length > $message_longest) {
            $message_longest = $message_length;
        }
    }

    foreach $message (@{$messages}) {
        $message_length = length $message;

        if ($message_length > $message_longest) {
            $message_longest = $message_length;
        }
    }

    return $message_longest;
}

#
# Look back elements in provided array reference starting from
# provided index and removing unnecessary lines
#
# Input variables:
#    integer $index = index in array from witch to start
#    array reference $messages = provided array in which to do operations
#
sub look_back_remove {
    my $index = shift;
    my $messages = shift;
    my $messages_size = @{$messages};
    my $routine_name = "look_back";

    for (my $iterator = $index; $iterator >= 0; $iterator--) {
        remove_line($index, $messages);
    }
}

#
# Look forward elements in provided array reference starting from
# provided index and removing unnecessary lines
#
# Input variables:
#    integer $index = index in array from witch to start
#    array reference $messages = provided array in which to do operations
#
sub look_forward_remove {
    my $index = shift;
    my $messages = shift;
    my $messages_size = @{$messages};
    my $routine_name = "look_forward";

    for (my $iterator = $index; $iterator <= $messages_size; $iterator++) {
        remove_line($index, $messages);
    }
}

#
# Look for specific header and footer patterns in provided array and
# remove them and leave only one blank line in their place
#
# Input variables:
#    string $footer_text = footer text containing tags
#    string $header_text = header text containing tags
#    array reference $messages = provided array in which to do operations
#
# Return values:
#    string $footer_text = text to be used as footer
#    string $header_text = text to be used as header
#
sub remove_header_and_footer {
    my $footer_text = shift;
    my $header_text = shift;
    my $index = 0;
    my $message = "";
    my $messages = shift;
    my $regex;
    my $routine_name = "remove_header_and_footer";

    foreach $message (@{$messages}) {
        if ($message =~ /(.*):(.*)/) {
            if ($1 eq "header") {
                $header_text = $2;
                look_back_remove($index, $messages);
            } elsif ($1 eq "footer") {
                $footer_text = $2;
                look_forward_remove($index, $messages);
            }
        }
        $index++;
    }
    return ($header_text, $footer_text);
}

#
# Remove element in provided index from array reference
#
# Input variables:
#    integer $index = index in array to check
#    array reference $messages = provided array in which to do operations
#
sub remove_line {
    my $index = shift;
    my $messages = shift;
    my $messages_size = @{$messages};
    my $routine_name = "remove_line";

    splice(@{$messages}, $index, 1);
}

1;

package General::String_Operations;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.4
#  Date     : 2022-11-18 17:40:26
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2016, 2020, 2022
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
    cut_string_to_max_length
    prepend_characters
    sort_hash_keys_alphanumerically
    strip_leading_characters
    strip_leading_trailing_white_spaces
    );

# -----------------------------------------------------------------------------
# Sort all the keys of the provided hash reference in an alpha numerical way
# and return an array of the sorted keys.
sub sort_hash_keys_alphanumerically {
    my $hashref = shift;

    return sort alphanum keys %$hashref;
}

# -----------------------------------------------------------------------------
# Sort alphanumeric, i.e. atmport=1, atmport=2, atmport=10
# instead of:             atmport=1, atmport=10, atmport=2
sub alphanum {
    my $a0 = $a;
    my $b0 = $b;
    for (;;)
    {
        my ($a1, $a2) = ($a0 =~ m/^([^\d]+)(.*)/);
        my ($b1, $b2) = ($b0 =~ m/^([^\d]+)(.*)/);
        $a1 = "" if (!defined $a1);
        $b1 = "" if (!defined $b1);
        $a2 = "" if (!defined $a2);
        $b2 = "" if (!defined $b2);
        $a2 = $a0 if (!$a1 and !$a2);
        $b2 = $b0 if (!$b1 and !$b2);
        if ($a1 ne $b1) { return ($a1 cmp $b1); }
        $a0 = $a2;
        $b0 = $b2;
        return 0 if (($a0 eq "") and ($b0 eq ""));

        ($a1, $a2) = ($a0 =~ m/^(\d+)(.*)/);
        ($b1, $b2) = ($b0 =~ m/^(\d+)(.*)/);
        $a1 = "" if (!defined $a1);
        $b1 = "" if (!defined $b1);
        $a2 = "" if (!defined $a2);
        $b2 = "" if (!defined $b2);
        $a2 = $a0 if (!$a1 and !$a2);
        $b2 = $b0 if (!$b1 and !$b2);
        if ($a1 != $b1) { return ($a1 <=> $b1); }
        $a0 = $a2;
        $b0 = $b2;
        return 0 if (($a0 eq "") and ($b0 eq ""));
    }
}

#
# Go through given array and make sure each line is less than or
# equal to the max length, if not cut the line at the correct place
# and all expand array accordingly.
#
# Input variables:
#    integer $max_length = provided max length for text
#    array reference $strings = provided array in which to do operations
#
sub cut_string_to_max_length {
    my $last_position = 0;
    my $indentation;
    my $index = 0;
    my $keep_format = shift;
    my $max_length = shift;
    my $string = "";
    my $strings = shift;
    my $string_length = 0;
    my $routine_name = "cut_string_to_max_length";
    my $string_part1 = "";
    my $string_part2 = "";

    for ($index = 0; $index < scalar @{$strings}; $index++) {
        $string = @{$strings}[$index];
        $string_length = length $string;
        if ($string_length > $max_length) {
            # Line is too long it needs to be split at the proper space or / or $max_length

            # Calculate length of sub-string containing leading spaces to keep indentation
            # else do not indent and handle formatting somewhere else
            if ($keep_format == 1) {
                $indentation = length (($string =~ /^(\s+).*/)[0]);
            } else {
                $indentation = 0;
            }

            if ($string =~ / /) {
                # Line contains at least one space.
                # Split the line at the first space that is below $max_length.
                $last_position = $string_length;
                while (($last_position = rindex $string, " ", $last_position) >= 0) {
                    if ($last_position <= $max_length) {
                        last;
                    }
                    $last_position--;
                }
                if ($last_position > $max_length) {
                    $last_position = $max_length;
                } elsif ($last_position == 0) {
                    $last_position = $max_length;
                }
                # Split the long line into two parts, current index is updated
                # with the shortened string and the remainder goes into the next index.
                $string_part1 = substr $string, 0, $last_position;
                $string_part2 = substr $string, ($last_position + 1);
                splice @{$strings}, $index, 1, (" " x ($indentation)) . $string_part2;
                splice @{$strings}, $index, 0, $string_part1;
            } elsif ($string =~ /\//) {
                # Line contains at least one /.
                # Split the line at the first / that is below $max_length.
                $last_position = $string_length;
                while (($last_position = rindex $string, "/", $last_position) >= 0) {
                    if ($last_position <= $max_length) {
                        last;
                    }
                    $last_position--;
                }
                if ($last_position > $max_length) {
                    $last_position = $max_length;
                } elsif ($last_position == 0) {
                    $last_position = $max_length;
                }
                # Split the long line into two parts, current index is updated
                # with the shortened string and the remainder goes into the next index.
                $string_part1 = substr $string, 0, $last_position;
                $string_part2 = substr $string, ($last_position + 1);
                splice @{$strings}, $index, 1, (" " x ($indentation)) . $string_part2;
                splice @{$strings}, $index, 0, "$string_part1/";
            } else {
                # Split line at $max_length
                # Split the long line into two parts, current index is updated
                # with the shortened string and the remainder goes into the next index.
                $string_part1 = substr $string, 0, $max_length;
                $string_part2 = substr $string, ($max_length + 1);
                splice @{$strings}, $index, 1, (" " x ($indentation)) . $string_part2;
                splice @{$strings}, $index, 0, $string_part1;
            }
        }
    }
}

#
# Add specific characters to the beginning of the string on all lines.
#
# Input variables:
#    - String to add to the beginning of each line.
#    - String to change (can be a multi-line string).
#
# Output:
#    - The modified string.
#
sub prepend_characters {
    my $add = shift;
    my $string = shift;

    $string =~ s/^/$add/mg;
    return $string;
}

#
# Strip specific characters from the beginning of the string on all lines.
#
# Input variables:
#    - String to remove from the beginning of each line.
#    - String to change (can be a multi-line string).
#
# Output:
#    - The modified string.
#
sub strip_leading_characters {
    my $strip = shift;
    my $string = shift;

    $string =~ s/^$strip//mg;
    return $string;
}

#
# Strip leading and trailing white spaces from all elements in
# provided array
#
# Input variables:
#    array reference $strings = provided array in which to do operations
#
sub strip_leading_trailing_white_spaces {
    my $string = "";
    my $strings = shift;
    my $routine_name = "strip_leading_trailing_white_spaces";

    foreach $string (@{$strings}) {
        $string =~ s/^\s+|\s+$//g;
    }
}

1;

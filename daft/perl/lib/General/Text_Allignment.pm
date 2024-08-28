package General::Text_Allignment;

################################################################################
#
#  Author   : everhel
#
#  Revision : 1.1
#  Date     : 2017-04-05 19:39:29
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

our @EXPORT_OK = qw(align_text_center align_text_left align_text_right align_text_with_longest);

#
# Align text from the middle
#
# Input variables:
#    array reference $messages = provided array in which to do operations
#    integer $message_longest = longest string length to be used
#
sub align_text_center {
    my $message = "";
    my $messages = shift;
    my $message_length = 0;
    my $message_longest = shift;
    my $message_middle = 0;
    my $message_logest_middle = 0;
    my $messages_start = 0;
    my $routine_name = "align_text_center";

    foreach $message (@{$messages}) {
        $message_length = length $message;
        $message_logest_middle = ($message_longest / 2);
        $message_middle = ($message_length / 2);
        $messages_start = $message_logest_middle - $message_middle;

        my $string = " " x ($messages_start);
        $message = "$string$message$string";
    }
}

#
# Align text from the left
#
# Input variables:
#    array reference $messages = provided array in which to do operations
#    integer $message_longest = longest string length to be used
#
sub align_text_left {
    my $message = "";
    my $messages = shift;
    my $message_length = 0;
    my $message_longest = shift;
    my $messages_start = 0;
    my $routine_name = "align_text_left";

    foreach $message (@{$messages}) {
        $message_length = length $message;

        my $string = " " x ($message_longest - $message_length);
        $message .= $string;
    }
}

#
# Align text from the right
#
# Input variables:
#    array reference $messages = provided array in which to do operations
#    integer $message_longest = longest string length to be used
#
sub align_text_right {
    my $message = "";
    my $messages = shift;
    my $message_length = 0;
    my $message_longest = shift;
    my $messages_start = 0;
    my $routine_name = "align_text_right";

    foreach $message (@{$messages}) {
        $message_length = length $message;
        $messages_start = ($message_longest - $message_length);

        my $string = " " x ($messages_start);
        $message = $string .= $message;
    }
}

#
# Make sure each message each equal in length, padded with spaces
#
# Input variables:
#    string $message = string that should be padded with spaces
#    integer $message_longest = longest string length to be used
#
# Return variables:
#    string $message = provided string padded with spaces
#
sub align_text_with_longest {
    my $message = shift;
    my $message_length = 0;
    my $message_longest = shift;
    my $routine_name = "align_text_with_longest";

    $message_length = length $message;

    if ($message_length < $message_longest) {
        until ($message_length == $message_longest) {
            $message .= " ";
            $message_length = length $message;
        }
    }

    return $message;
}

1;

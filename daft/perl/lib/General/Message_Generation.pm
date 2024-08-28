package General::Message_Generation;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.8
#  Date     : 2022-11-18 17:40:26
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2017,2018,2019,2022
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
    draw_message_box
    print_message_box
    print_underlined_message
);

use General::Array_Operations;
use General::String_Operations;
use General::Text_Allignment;

#
# Generate body text based based on provided array reference
#
# Input variables:
#    array reference $messages = provided array in which to do operations
#    array reference $output_message = array which is used for output
#
sub add_body {
    my $message = "";
    my $messages = shift;
    my $output_message = shift;
    my $routine_name = "add_body";

    foreach $message (@{$messages}) {
        push @{$output_message}, $message;
    }
}

#
# Generate footer text based on provided message type or
# create footer from provided text
#
# Input variables:
#    string $footer_text = text to be used in footer message
#    integer $message_longest = longest string length to be used
#    string $message_type = type of message (INFO WARNING ERROR)
#    array reference $output_message = array which is used for output
#
sub add_footer {
    my $blank_line = "";
    my $footer = "";
    my $footer_text = shift;
    my $message_longest = shift;
    my $message_type = lc (shift);
    my $output_message = shift;
    my $routine_name = "add_footer";

    $footer = "#" x ($message_longest + 4);

    push @{$output_message}, $footer .= "\n";

    # Generate predefined footer messages
    if ($message_type eq "error") {
        calculate_spacing("error", $message_longest, $output_message);
        push @{$output_message}, $footer;
    } elsif ($message_type eq "info") {
        calculate_spacing("info", $message_longest, $output_message);
        push @{$output_message}, $footer;
    } elsif ($message_type eq "warning") {
        calculate_spacing("warning", $message_longest, $output_message);
        push @{$output_message}, $footer;
    } else {
        # Check if footer text provided
        if ($footer_text) {
            $blank_line = join "", "# ", (" " x ($message_longest)), " #\n";
            push @{$output_message}, $blank_line;
            push @{$output_message}, $footer_text;
            push @{$output_message}, $blank_line;
            push @{$output_message}, $footer;
        }
    }
}

#
# Generate header text based on provided message type or
# create header from provided text
#
# Input variables:
#    string $header_text = text to be used in header message
#    integer $message_longest = longest string length to be used
#    string $message_type = type of message (INFO WARNING ERROR)
#    array reference $output_message = array which is used for output
#
sub add_header {
    my $blank_line = "";
    my $header = "";
    my $header_text = shift;
    my $message_longest = shift;
    my $message_type = lc (shift);
    my $output_message = shift;
    my $routine_name = "add_header";

    $header = "#" x ($message_longest + 4);

    push @{$output_message}, $header .= "\n";

    # Generate predefined header messages
    if ($message_type eq "error") {
        calculate_spacing("error", $message_longest, $output_message);
        push @{$output_message}, $header;
    } elsif ($message_type eq "info") {
        calculate_spacing("info", $message_longest, $output_message);
        push @{$output_message}, $header;
    } elsif ($message_type eq "warning") {
        calculate_spacing("warning", $message_longest, $output_message);
        push @{$output_message}, $header;
    } else {
        # Check if header text provided
        if ($header_text) {
            $blank_line = join "", "# ", (" " x ($message_longest)), " #\n";
            push @{$output_message}, $blank_line;
            push @{$output_message}, $header_text;
            push @{$output_message}, $blank_line;
            push @{$output_message}, $header;
        }
    }
}

#
# Calculate spacing and repetition for notification messages based on
# longest message, padded with spaces when necessary
#
# Input variables:
#    string $info_type = info text based on header type
#    integer $message_longest = longest string length to be used
#    array reference $output_message = array which is used for output
#
sub calculate_spacing {
    my $info_type = shift;
    my $header_message = "";
    my $header_message_length = 0;
    my $message_longest = shift;
    my $missing_spaces = 0;
    my $output_message = shift;
    my $notification_lenght = 0;
    my $notification_repeats = 0;
    my $odd_number_of_prints;
    my $position = 0;
    my $routine_name = "calculate_spacing";

    $notification_lenght = length " " . $info_type . " ";
    $notification_repeats = int($message_longest / $notification_lenght);
    $odd_number_of_prints = $notification_repeats % 2;

    $header_message = " $info_type " x $notification_repeats;
    $header_message = join "", "# ", uc($header_message), " #";
    $header_message_length = length $header_message;

    if ($odd_number_of_prints) {
        $missing_spaces = ($message_longest + 4) - $header_message_length;
        until ($missing_spaces == 0) {
            if ($missing_spaces % 2) {
                $position = 0 + 1;
                substr $header_message, $position, 0, " ";
                $missing_spaces = $missing_spaces - 1;
                $header_message_length = length $header_message;
            } else {
                $position = $header_message_length - 1;
                substr $header_message, $position, 0, " ";
                $missing_spaces = $missing_spaces - 1;
                $header_message_length = length $header_message;
            }
        }
    } else {
        $missing_spaces = ($message_longest + 4) - $header_message_length;
        until ($missing_spaces == 0) {
            if ($missing_spaces == 1) {
                $position = ($header_message_length / 2);
                substr $header_message, $position, 0, " ";
                $missing_spaces = $missing_spaces - 1;
                $header_message_length = length $header_message;
            } elsif ($missing_spaces % 2) {
                $position = 0 + 1;
                substr $header_message, $position, 0, " ";
                $missing_spaces = $missing_spaces - 1;
                $header_message_length = length $header_message;
            } else {
                $position = $header_message_length - 1;
                substr $header_message, $position, 0, " ";
                $missing_spaces = $missing_spaces - 1;
                $header_message_length = length $header_message;
            }
        }
    }

    push @{$output_message}, $header_message .= "\n";
}

#
# Draw message box around text in provided array
#
# Input variables:
#    string $footer_text = footer text to be used
#    string $header_text = header text to be used
#    array reference $messages = provided array in which to do operations
#    string $message_longest = longest string length to be used
#    string $message_type = type of message (INFO WARNING ERROR)
#    array reference $output_message = array which is used for output
#
sub draw_message_box {
    my $footer_text = shift;
    my $header_text = shift;
    my $message = "";
    my $messages = shift;
    my $message_longest = shift;
    my $message_type = shift;
    my $output_message = shift;
    my $routine_name = "draw_message_box";

    if ($header_text) {
        $header_text = join "", "# ", $header_text, " #\n";
    }

    if ($footer_text) {
        $footer_text = join "", "# ", $footer_text, " #\n";
    }

    foreach $message (@{$messages}) {
        $message = join "", "# ", $message, " #\n";
    }

    add_header($header_text, $message_longest, $message_type, $output_message);
    add_body($messages, $output_message);
    add_footer($footer_text, $message_longest, $message_type, $output_message);
}

#
# Print a message box on the screen or returned to the user.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "align-text":
#           Controls how the text is aligned inside the message box.
#           Allowed values are: left, center, right
#       "empty-line-after-box":
#           Controls if an empty line is added after the box.
#       "empty-line-before-box":
#           Controls if an empty line is added before the box.
#       "file":
#           Specifies a file name to read the text to be included into the
#           message box.
#       "footer-text":
#           A string that is printed at the bottom of the message box.
#       "header-text":
#           A string that is printed at the top of the message box.
#       "max-length":
#           Specifies how long the lines inside the box can be before a long
#           line is wrapped into two or more lines.
#           If length is set to 0 then no wrapping will take place.
#           The default is 72 characters.
#       "type":
#           Specifies if a special header and footer should be printed.
#           Allowed values are: error, info, warning
#       "messages":
#           Array reference of text lines to be shown in the message box.
#       "return-output":
#           Scalar reference of variable where the result of the command is
#           returned to the caller.
#           If specified then nothing is printed on screen, that is left up
#           to the user to do.
#
# Example:
# my @messages = ("Line 1", "Line 2");
# my $rc = General::Message_Generation::print_message_box(
#     {
#         "messages"      => \@messages,
#         "header-text"   => "Important Message",
#     }
# );
#
# or
#
# my $rc = General::Message_Generation::print_message_box(
#     {
#         "messages"      => [ "Line 1", "Line2" ],
#         "header-text"   => "Important Message",
#     }
# );
#
# Return values:
#    0: Successful printing of message box
#    1: Some failure detected
#
sub print_message_box {
    my %params = %{$_[0]};

    # Initialize local variables
    my $align_text = exists $params{"align-text"} ? $params{"align-text"} : "";
    my $empty_line_after_box = exists $params{"empty-line-after-box"} ? 1 : 0;
    my $empty_line_before_box = exists $params{"empty-line-before-box"} ? 1 : 0;
    my $file_handle;
    my $footer_text = exists $params{"footer-text"} ? $params{"footer-text"} : "";
    my $header_text = exists $params{"header-text"} ? $params{"header-text"} : "";
    my $input_file = exists $params{"file"} ? $params{"file"} : "";
    my $max_length = exists $params{"max-length"} ? $params{"max-length"} : 72;
    my $message = "";
    my $message_longest = 0;
    my $message_type = exists $params{"type"} ? $params{"type"} : "";
    my @messages = exists $params{"messages"} ? @{$params{"messages"}} : ();
    my @output_message;
    my $return_output    = exists $params{"return-output"} ? 1 : 0;
    my $routine_name = "print_message_box";
    my $row_in_file = "";

    # Read messages from file, if specified
    if ($input_file) {
        if (open($file_handle, '<', $input_file)) {
            while ($row_in_file = <$file_handle>) {
                chomp $row_in_file;
                push @messages, $row_in_file;
            }
        } else {
            print "\nCould not open file '$input_file' $!\n";
            return 1;
        }
    }

    # Check that we have something to print
    if (@messages eq "") {
        print "\nThe parameter 'messages' cannot be left empty\n";
        return 1;
    }

    if ($align_text) {
        unless ($align_text =~ /^(center|left|right)$/i) {
            print "\nThe parameter 'align-text' can only have values 'center', 'left' or 'right'\n";
            return 1;
        }
    }

    if ($message_type) {
        unless ($message_type =~ /^(error|info|warning)$/i) {
            print "\nThe parameter 'type' can only have values 'error', 'info' or 'warning'\n";
            return 1;
        }
    }

    # Strip header and footer from @messages and store in variables
    ($header_text, $footer_text) = General::Array_Operations::remove_header_and_footer(
        $footer_text,
        $header_text,
        \@messages
    );

    # Split lines if longer than $max_lenght
    if ($max_length > 0) {
        General::String_Operations::cut_string_to_max_length(
            0,
            $max_length,
            \@messages
        );
    }

    # Get longest line in @messages
    $message_longest = General::Array_Operations::get_longest_string(
        \@messages,
        $header_text,
        $footer_text
    );

    # Aligning each $message in @messages
    if ($align_text eq "center") {
        General::Text_Allignment::align_text_center(
            \@messages,
            $message_longest
        );

        $message_longest = General::Array_Operations::get_longest_string(
            \@messages
        );
    } elsif ($align_text eq "left") {
        General::Text_Allignment::align_text_left(
            \@messages,
            $message_longest
        );

        $message_longest = General::Array_Operations::get_longest_string(
            \@messages
        );
    } elsif ($align_text eq "right") {
        General::Text_Allignment::align_text_right(
            \@messages,
            $message_longest
        );

        $message_longest = General::Array_Operations::get_longest_string(
            \@messages
        );
    }

    # If provided align header text with with longest $message
    if ($header_text) {
        $header_text = General::Text_Allignment::align_text_with_longest(
            $header_text,
            $message_longest
        );
    }

    # If provided align footer text with with longest $message
    if ($footer_text) {
        $footer_text = General::Text_Allignment::align_text_with_longest(
            $footer_text,
            $message_longest
        );
    }

    # Align each $message in @messages with longest $message
    foreach $message (@messages) {
        $message = General::Text_Allignment::align_text_with_longest(
            $message,
            $message_longest
        );
    }

    General::Message_Generation::draw_message_box(
        $footer_text,
        $header_text,
        \@messages,
        $message_longest,
        $message_type,
        \@output_message
    );

    unshift @output_message, "\n" if ($empty_line_before_box);
    push @output_message, "\n" if ($empty_line_after_box);

    # Copy the result to user variable if wanted
    if ($return_output) {
        my $oneline = join '', @output_message;
        ${$params{"return-output"}} = $oneline;
    } else {
        print @output_message;
    }

    return 0;
}

#
# Print a message that is underlined on the screen or returned to the user.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "eol-char":
#           If specified then it should contain the EOL (End-Of-Line) character(s)
#           to use for each line.
#           The default is to use LF ("\n") as EOL character for each line which
#           is the standard UNIX format.
#           For DOS format pass in the eol-char as "\r\n".
#       "indent-string":
#           If specified then it should be specified as scalar string which will
#           be used to add in front of the message and the underline line.
#           If not specified then no indentation will be done.
#       "message":
#           Scalar of text line to be underlined, if not specified then only
#           the underline will be printed with the length given by
#           'underline-length' or 80 characters if not specified.
#       "return-output":
#           Scalar reference of variable where the result of the command is
#           returned to the caller.
#           If specified then nothing is printed on screen, that is left up
#           to the user to do.
#       "underline-char":
#           Specifies what character to use as undeline character, if not
#           specified then a dash "-" is used.
#       "underline-length":
#           If specified then the underline will be of the specified length.
#           If not specified then the length with be as long as the message
#           length, and if message is not specified then the length will be
#           80 characters.
#
# Example:
# my $message = "Very Important Message";
# General::Message_Generation::print_underlined_message(
#     {
#         "message"         => $message,
#         "underline-char"  => "=",
#     }
# );
#
# or
#
# General::Message_Generation::print_underlined_message(
#     {
#         "message"         => "Very Important Message",
#         "underline-char"  => "=",
#     }
# );
#
# or
#
# my $message = "";
# General::Message_Generation::print_underlined_message(
#     {
#         "message"         => "Very Important Message",
#         "underline-char"  => "=",
#         "return-output"   => \$message,
#     }
# );
#
# or to get just an underline with 80 characters
#
# General::Message_Generation::print_underlined_message();
#
# Return values:
#    0: Successful printing of message box
#    1: Some failure detected
#
sub print_underlined_message {
    #my %params = %{$_[0]};
    my %params;
    if (@_) {
        %params = %{$_[0]};
    }

    # Initialize local variables
    my $eol_char = exists $params{"eol-char"} ? $params{"eol-char"} : "\n";
    my $indent_string = exists $params{"indent-string"} ? $params{"indent-string"} : "";
    my $message = exists $params{"message"} ? $params{"message"} : "";
    my $output_message = "";
    my $return_output = exists $params{"return-output"} ? 1 : 0;
    my $routine_name = "print_underlined_message";
    my $underline_char = exists $params{"underline-char"} ? $params{"underline-char"} : "-";
    my $underline_length = exists $params{"underline-length"} ? $params{"underline-length"} : 0;

    $message =~ s/$eol_char$//;     # Remove EOL Character
    if ($message eq "") {
        if ($underline_length == 0) {
            $underline_length = 80;
        }
    } else {
        if ($underline_length == 0) {
            $underline_length = length($message);
        }
    }

    if ($message) {
        $output_message = "$indent_string$message$eol_char";
    }

    $output_message .= $indent_string . substr $underline_char x $underline_length, 0, $underline_length;
    $output_message .= "$eol_char";

    # Copy the result to user variable if wanted
    if ($return_output) {
        ${$params{"return-output"}} = $output_message;
    } else {
        print $output_message;
    }

    return 0;
}

1;

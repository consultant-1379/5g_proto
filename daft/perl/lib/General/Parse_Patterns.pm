package General::Parse_Patterns;

################################################################################
#
#  Author   : eedvam, everhel
#
#  Revision : 1.0
#  Date     : 2016-11-14 17:00:00
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

our @EXPORT_OK = qw(ignore_specific_patterns parse_pattern_list);

#
# [IGNORE_SPECIFIC_ERRORS]
#
# This will check to see if the error found belongs to a set of "specific errors". If so, a marker/flag needs to be set in order for the
# error to be reported. The flag should be checked for existence *before* this subroutine is called.
#
# Return codes :
#     either  1 if the snippet contains a specific error pattern
#     or      0 if the snippet doesn't contain any specific error patterns

sub ignore_specific_errors {

    my $snippet = shift;
    my $ignore_patterns = shift;

    foreach ( @{$ignore_patterns} ) {
        if ( $snippet =~ /$_/ ) {
            return 1;
        }
    }

    return 0;
}

#
# [PARSE_PATTERN_LIST]
#
# This opens the list of pattern and ignore patterns and populates the arrays @pattern_matches and @ignore_patterns.
# Comments and empty lines are ignored, markers for the pattern and ignore patterns searched for and then the patterns taken as values for the arrays.
# The pattern list has the following format:
#
# # Comments
# SEARCH FOR {
#
#   pattern1
#   pattern2
#   pattern3
#   ...
#
# }
#
# IGNORE {
#
#   pattern4
#   pattern5
#   pattern6
#   ...
# }
#
# Regular expressions can be used in this pattern list, they should apply to the perl regular expression formats.
#
# Return variables:
#     array ref \@pattern_matches = array reference to @pattern_matches
#     array ref \@ignore_patterns =  array reference to @ignore_patterns
#     array ref \@keep_all = array reference to @keep_all
#     array ref \@specific_patterns = array reference to @specific_patterns
#

sub parse_pattern_list {

    my $pattern_list = shift;
    chomp($pattern_list);

    open( PATTERNLIST, "<", $pattern_list )
        or die "Could not open file '$pattern_list' $!";

    my $search_for = 0;
    my $ignore = 0;
    my $specific_patterns = 0;
    my $keep_all_patterns = 0;

    my @pattern_matches = ();
    my @ignore_patterns = ();
    my @keep_all = ();
    my @specific_patterns = ();

    # Extract the patterns that indicate patterns in the log file and assign these as array values to the array @pattern_matches.

    while (<PATTERNLIST>) {

        # Skip empty and comment lines
        next if $_ =~ /^\s*$/ or $_ =~ /^#/;

        if ( $search_for == 1 && $_ !~ /\}/ ) {
            # Insert pattern into the pattern matches array
            $_ =~ s/^\s+|\s+$//g;
            push( @pattern_matches, $_ );
        } elsif ( $search_for == 1 && $_ =~ /\}/ ) {
            $search_for = 0;
        } elsif ( $ignore == 1 && $_ !~ /\}/ ) {
            # Insert pattern into the ignore array
            $_ =~ s/^\s+|\s+$//g;
            push( @ignore_patterns, $_ );
        } elsif ( $ignore == 1 && $_ =~ /\}/ ) {
            $ignore = 0;
        } elsif ( $specific_patterns == 1 && $_ !~/\}/ ) {
            # Insert pattern into the specific patterns array
            $_ =~  s/^\s+|\s+$//g;
            push( @specific_patterns, $_ );
        } elsif ($specific_patterns == 1 && $_ =~ /\}/) {
            $specific_patterns = 0;
        } elsif ( $keep_all_patterns == 1 && $_ !~ /\}/ ) {
            # Insert pattern into the ignore array
            $_ =~ s/^\s+|\s+$//g;
            push( @keep_all, $_ );
        } elsif ( $keep_all_patterns == 1 && $_ =~ /\}/ ) {
            $keep_all_patterns = 0;
        } elsif ( $_ =~ /^SEARCH FOR \{/ ) {
            $search_for = 1;
            next;
        } elsif ( $_ =~ /^IGNORE \{/ ) {
            $ignore = 1;
            next;
        } elsif ($_ =~ /SPECIFIC PATTERNS \{/) {
            $specific_patterns = 1;
            next;
        } elsif ($_ =~ /KEEP ALL \{/) {
            $keep_all_patterns = 1;
            next;
        }
    }

    return (\@pattern_matches, \@ignore_patterns, \@keep_all, \@specific_patterns);
}

1;

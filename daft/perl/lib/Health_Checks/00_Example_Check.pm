package Health_Checks::00_Example_Check;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2021-07-15 17:40:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

################################################################################
#
# This is an example plugin health check script that will be executed by the
# system_health_check.pl script if this specific health check is wanted and it
# can be used as a starting point to write your own plugin script.
#
# Your own script must follow certain rules in order for it to be usable by the
# system_health_check.pl script.
#
#  1. The first line in the file must start with the package definition line
#     which must include the string "package Health_Checks::" followed by the
#     name of the package which MUST BE THE SAME as the file name minus the
#     ".pm" extension.
#
#  2. The last line in the file must be the string "1;"
#
#  3. At the top of the file somewhere after the package line you should
#     specify the following lines:
#
#     use strict;
#
#     use Exporter qw(import);
#     our @EXPORT_OK = qw(
#         main
#         usage
#     );
#
#  4. In the "main" subroutine you should have the code that performs the
#     specific health check you want to do and this function is declared
#     as follows:
#
#     sub main {
#         # Below follows the Perl code that performs the health check
#     }
#
#     This is the subroutine that will be called by the system_health_check.pl
#     script.
#
#     If the logic detects a fault which should cause the health check to
#     fail then a call to the following subroutine should be done:
#         &::report_failed("Text that describes what the failure is");
#     The logic in the "main" function can either exit directly by calling
#     "return;", or continue checking more things if this is wanted, but as
#     soon as report_failed has been called then the health check is reported
#     as failed.
#
#     If the logic detects that the healthcheck should be skipped for whatever
#     reason then a call to the following subroutine should be done:
#         &::report_skipped("Text that describes why the check was skipped");
#     The logic in the "main" function should in this case immediately
#     exit by calling "return;".
#     This will mark the health check as skipped.
#
#     If neither "&::report_failed" nor "&::report_skipped" is called
#     when the logic calls "return;" then the check is reported as successful.
#
#     If the logic for some reason want to report some detailed information
#     without reporting it as failed or skipped it call also call the following
#     subroutine:
#         &::report_details("Text that contains some details about the check";
#     Calling this subroutine will not effect the status of the check.
#
#  5. In the "usage" subroutine you can add text describing what the check is
#     used for.
#
#  6. The script can call other functions that you define inside of your script
#     or call other subroutines in existing Perl modules, in which case they
#     must be loaded with the "use" statement and they must exist in the used
#     library path.
#
################################################################################

use strict;

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
);

# Help functions used by this script.
use General::OS_Operations;

# -----------------------------------------------------------------------------
# The "main" subroutine is the one called to execute the specific health check.
#
sub main {
    # Declare variables used by this subroutine
    my $rc;
    my @result;

    # If this script depends on that the SC namespace is given then perform the
    # following check.
    if ( &::get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    # To execute any system level command and easily handle proper logging of
    # the executed command and returning back the printout into an array for
    # easy parsing use this function.
    # To see all features of this function and others look at the following file:
    # ../General/OS_Operations.pm
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ls -l /tmp/",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    # Check the return code to make sure the command execution was successful
    if ( $rc == 0 ) {
        # The command executed successfully
        if ( @result != 0 ) {
            # And it also returned some data that can be parsed
            for ( @result ) {
                # For each line printed do something
                if ( /some pattern to look for/ ) {
                    # Do something
                } else {
                    # Do something else
                }
            }
            &::report_details("The 'ls' command returned the following data:\n" . join "\n", @result);
        } else {
            &::report_details("The 'ls' command returned nothing.\n");
        }
    } else {
        # The command failed for some reason
        &::report_failed("The 'ls' command failed for some reason with return code $rc and here are the details:\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
# The "usage" subroutine might be called if the user want to see help text
# about what the health check is doing, or it can just be used to document what
# this script is doing.
#
sub usage {
    print <<EOF;

Description:
============

This script is just an example of what a health check plugin script might look
like and it performs no real check.

This script is a plugin for the ../../bin/system_health_check.pl script.
EOF

}

# The following line MUST ALWAYS BE THERE or loading of this package will fail.
1;

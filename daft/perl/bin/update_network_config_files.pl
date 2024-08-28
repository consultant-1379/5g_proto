#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.9
#  Date     : 2024-05-28 15:49:40
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2024
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
use File::Basename;

# Variables
my $backup_directory = "";
my $command = "";
my $current_date = `date +%Y%m%d_%H%M%S`;
$current_date =~ s/[\r\n]+//g;
my $debug = 0;
my $errorcnt = 0;
my $extra_parameters = "";
my $filename = "";
my $file_pattern = "*.xml";
my $found_cnt = 0;
my $input_directory = "";
my @input_files = ();
my $keep_missing_parameters = 0;
my @lines = ();
my $new_value = "";
my $old_param_name = "";
my %old_param_value;
my $old_value = "";
my $only_changeme = 0;
my $output_format = "";
my @parameters = ();
my $param_to_change = "";
my $script_directory = `dirname $0`;
$script_directory =~ s/[\r\n]+//g;
my $show_help = 0;
my $special_handling = 0;
my $status_file = "status_$current_date.txt";
my $template_file = "";
my $value_to_use = "";

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;            # standard Perl module
GetOptions (
    "b|backup-directory=s"          => \$backup_directory,
    "d|debug=i"                     => \$debug,
    "f|file-pattern=s"              => \$file_pattern,
    "i|input-directory=s"           => \$input_directory,
    "k|keep-missing-params"         => \$keep_missing_parameters,
    "only-changeme"                 => \$only_changeme,
    "r|output-format=s"             => \$output_format,
    "p|parameter=s"                 => \@parameters,
    "special-handling"              => \$special_handling,
    "s|status-file=s"               => \$status_file,
    "t|template-file=s"             => \$template_file,
    "h|help"                        => \$show_help
);

if ($show_help) {
    show_help();
    exit 0;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Check input parameters

if ($output_format ne "" && $output_format !~ /^(one-line|multiple-lines)$/) {
    print "The parameter -r or --output-format can only have the values:\n - one-line\n - multiple-lines\n\n";
    exit 1;
}

# Remove any trailing / character in the directory name
$input_directory =~ s/\/$//g;
if ($input_directory eq "") {
    print "You have to specify parameter --input-directory\n";
    exit 1;
}

if ($template_file ne "") {
    if (! -f $template_file) {
        print "The template file does not exist\n";
        exit 1;
    }
}
if ($backup_directory eq "") {
    $backup_directory = "$input_directory/old_$current_date";
}
# Create backup directory
`mkdir -p $backup_directory`;
if ($?) {
    print "An error occurred while creating backup directory\n";
    exit 1;
}

if (! -f "$script_directory/generate_network_config_file.pl") {
    print "The mandatory script $script_directory/generate_network_config_file.pl was not found\n";
    exit 1;
}

# Store all file names of in specified directory in array
@input_files = `find $input_directory -maxdepth 1 -type f -name '$file_pattern'`;

# Setup the command parameters for calling the script that does the generation of the config file
$extra_parameters = "";
$extra_parameters .= " --template-file=$template_file" if $template_file;
$extra_parameters .= " --only-changeme" if $only_changeme;
$extra_parameters .= " --output-format=$output_format " if $output_format;
$extra_parameters .= " --keep-missing-params " if $keep_missing_parameters;
$extra_parameters .= " --debug=$debug" if $debug > 0;
if (@parameters) {
    for (@parameters) {
        $extra_parameters .= " --parameter=$_";
    }
}
# Process all files, one by one
for $filename (@input_files) {
    $filename =~ s/[\r\n]//g;

    # Only process XML files
    next unless $filename =~ /\.xml$/i;
    $found_cnt++;

    print "\nProcessing file: $filename\n";

    # Run script that creates a new network configuration file from the old file
    $command = "perl $script_directory/generate_network_config_file.pl --old-file=$filename --status-file=$status_file $extra_parameters";
    @lines = `$command`;
    if ($?) {
        print "  ERROR: Failed to generate new network configuration file. Error=$?\n";
        $errorcnt++;
        next;
    }

    # Fetch current value of the _oam_network parameter and update value for _oam_network2 with same value
    $value_to_use = "";
    my $temp_name;
    for (@lines) {
        s/[\r\n]+//g;

        if ($special_handling == 1) {
           #if (/name="(_special_eth0_routing_addresses)" type="STRING" value="CHANGEME"/) {
           #    $param_to_change = $1;
           #    $new_value = "10.87.247.152 10.87.247.211 10.87.247.234 137.58.113.0/24 137.58.117.0/24 137.58.120.0/24 164.48.141.30";
           #    s/name="$param_to_change" type="STRING" value=""/name="$param_to_change" type="STRING" value="$new_value"/;
           #    print "  Changing value \"\" for parameter $param_to_change to the value \"$new_value\"\n";
           #}

           ## Replace CHANGEME value for parameter _oam_network2 to value of _oam_network parameter
           #if (/name="(_oam_network)" type="STRING" value="([^"]*)"/) {
           #    # Save value for parameter
           #    $old_param_value{$1} = $2;
           #} elsif (/name="(_oam_network2)" type="STRING" value="CHANGEME"/) {
           #    $param_to_change = $1;
           #    $old_param_name = "_oam_network";
           #    $value_to_use = $old_param_value{"$old_param_name"};
           #    # Replace the CHANGEME value of new parameter with value of existing parameter
           #    s/name="$param_to_change" type="STRING" value="CHANGEME"/name="$param_to_change" type="STRING" value="$value_to_use"/;
           #    print "  Changing value for parameter $param_to_change to the value from parameter $old_param_name (=$value_to_use)\n";
           #}

           # Code to remove the extra single quotes for all "'CHANGEME'" values except for _VIP_OAM and _VIP_SIG... parameters
           if (/name="(\S+)"/) {
               $temp_name = $1;
           } elsif (/"'CHANGEME'"/) {
               next if ($temp_name =~ /(_VIP_SIG|_VIP_OAM)/);  # Do not change for VIP_SIG type addresses where we need the single quote.
               s/"'CHANGEME'"/"CHANGEME"/;
           }
        }
    }

    # Move existing file to backup directory
    print "  Moving file $filename to $backup_directory\n";
    `mv $filename $backup_directory`;
    if ($?) {
        print "  ERROR: Moving file failed. Error code $?\n";
        $errorcnt++;
    } elsif (open OUTF, ">$filename") {
        print "  Creating new file $filename based on template file\n";
        for (@lines) {
            print OUTF "$_\n";
        }
        close OUTF;
    } else {
        print "  Error: Failed to create file $filename\n";
        $errorcnt++;
    }
}
print "\n";

if (-f "$status_file") {
    print "###########################################################################################################\n";
    print "# Potential issues seen when updating the network configuration files that might need manual intervention #\n";
    print "###########################################################################################################\n";
    system("cat '$status_file'");
}

if (scalar @input_files == 0) {
    print "No Network Configuration Files (*.xml) found\n";
    $errorcnt++;
}

if ($errorcnt == 0) {
    exit 0;
} else {
    exit 1;
}

#
# Subroutines
# -----------
#

#
# Show help information.
#
# Input:
#  -
#
# Output:
#  - Help information printed to STDOUT.
#
sub show_help {
    print <<EOF;

Description:
============

This script reads all XML files in the input directory containing network
configuration files and creates new files based on a new template and
copies over the values from the old file.
Then it changes values for parameters that is new in the template
which contains CHANGEME value and replaces them with values from another
parameter in the old file.

This script uses the script "generate_network_config_file.pl" to parse and
generate the new network configuration file and that script must exist in
the samne directory as this script.


Syntax:
=======

 $0 [<OPTIONAL>] <MANDATORY>


 <MANDATORY> are all of the following parameters:

  -i | --input-directory <directory name>

 <OPTIONAL> are one or more of the following parameters:

  -h | --help
  -b | --backup-directory <directory name>
  -d | --debug <integer>
  -f | --file-pattern <file name regex>
  -k | --keep-missing-params
  -r | --output-format [one-line|multiple-lines]
  -p | --parameter <name>=<value>
       --only-changeme
       --special-handling
  -s | --status-file <filename>
  -t | --template-file <filename>


 where:

  --help
  -h
  Shows this help.


  --backup-directory <directory name>
  -b <directory name>
  Specifies the directory where backup files of the changed files from the
  input directory will be stored.
  If this parameter is not given then the backup directory will be created inside
  the same directory as the --input-directory with the name "old_YYYYMMDD_HHMMSS"
  where YYYYMMDD_HHMMSS is the current date and time stamp.


  --debug <integer>
  -d <integer>
  Enable debug printouts in the generate_network_config_file.pl script.


  --file-pattern <file name regex>
  -f <file name regex>
  Specifies the file pattern to use to find files to update.
  The specified pattern will be used by the Unix 'find' command so it must be a
  valid pattern that it understands.
  If this parameter is not given then the default value '*.xml' will be used.


  --input-directory <directory name>
  -i <directory name>
  Specifies the directory where the old network configuration files are stored
  and where the new files will be stored.
  !! NOTE !!
  The new files will be stored with the same file name as the old files and the
  old files is moved into the directory named with the --backup-directory
  parameter.


  --keep-missing-params
  -k
  ---------------------
  If specified then it will keep parameters that exist in the old file but not
  in the template file and these parameters will be added to the end of the new file.
  By default these missing parameters are deleted and not included in the output file.


  --only-changeme
  If this parameter is given then only parameters containing the string CHANGEME
  are changed from the template data.


  --output-format [one-line|multiple-lines]
  -r [one-line|multiple-lines]
  If specified and with value "multiple-lines" then the written output will have
  each parameter split up into muliple line where each parameter attribute is on
  a new line.

  If specified and with value "one-line" then each parameter is written as a
  long line with each parameter attribute on the same line.

  If not specified then each parameter is written using the same format as the
  template file, i.e. either as a single line or multiple lines with kept
  formatting, this is the default.


  --parameter <name>=<value>
  -p <name>=<value>
  If specified then it will update the specified named parameter with the specified
  value instead of using the value from the old file (if specified) or use the
  default value from the template file.
  This parameter can be specified multiple times if more than one parameter should
  be changed.


  --special-handling
  If specified then special handling is done for the following new
  parameters to change the value to:

   xxxxx                                   to same name as the .....

  NOTE: Nothing implemented yet.


  --status-file <filename>
  -s <filename>
  Specifies a status file where potentially found issues with the update of
  the network config file is written.
  Issues can e.g. be parameters in the old file that does not exist in the
  template file and thus will not be copied into the new updated file.
  If this parameter is not specified then a file called status_<timestamp>.txt
  is created in the current directory.


  --template-file <filename>
  -t <filename>
  Specifies the template file to use as a base.
  If this parameter is not specified then the built-in template file is used.


Examples:
=========

  Example 1:
  ----------
  To update all network configuration files and store the backup files in the default directory:

  $0 \\
      --input-directory Network_Config_Files \\
      --template-file templates/Network_Config_Template.xml

  Example 2:
  ----------
  To update all network configuration files and store the backup files in the
  specified directory and to generate a new network configuration file from a
  specified template file:

  $0 \\
      --input-directory Network_Config_Files \\
      --template-file templates/Network_Config_Template.xml \\
      --backup-directory Network_Config_Files/old_20201027/

  Example 3:
  ----------
  To update all network configuration files and store the backup files in the
  specified directory using the build-in template as a base and to set a number
  of parameters to specified value:

  $0 \\
      --input-directory Network_Config_Files \\
      --backup-directory Network_Config_Files/old_20201027/ \\
      --parameter eric_sc_values_hash_parameter_01=global.ericsson.scp.enabled=true \\
      --parameter eric_sc_values_hash_parameter_04=global.ericsson.spr.enabled=true \\
      --parameter "eric_sc_values_anchor_parameter_06=VIP_SIG_BSF_Diameter=\\'10.155.107.140\\'"

  Note: To get single quotes (') around the IP-address the whole parameter must be
  double quoted (") and the single quotes must be escaped, see last parameter above.

  Example 4:
  ----------
  To update all network configuration files and store the backup files in the
  default directory using built-in template file and doing special handling for
  new parameters:

  $0 \\
      --input-directory Network_Config_Files \\
      --special-handling

  Example 5:
  ----------
  To update all network configuration files matching a specific file pattern and
  store the backup files in the default directory:

  $0 \\
      --input-directory Network_Config_Files \\
      --file-pattern SC_*.xml


Return code:
============

 0: Successful
 1: Unsuccessful, some failure detected.

EOF
}

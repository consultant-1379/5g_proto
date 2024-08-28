#!/usr/bin/perl

################################################################################
# $Revision: 1.3.0 $                                                           #
#   $Author: EEDWIQ $                                                          #
#     $Date: 2022/12/09 09:00:00 $                                             #
################################################################################
#                                                                              #
# (C) COPYRIGHT ERICSSON GMBH 2015                                             #
#                                                                              #
# The copyright to the computer program(s) herein is the property of           #
# Ericsson GmbH, Germany.                                                      #
#                                                                              #
# The program(s) may be used and/or copied only with the written permission    #
# of Ericsson GmbH in accordance with the terms and conditions stipulated in   #
# the agreement/contract under which the program(s) have been supplied.        #
#                                                                              #
################################################################################
#
# v1.0      : Initial version
#
# v1.1      : Added support for following parameters
#                   --filter-string
#                   --keep-latest
#                   --remove-dirs
#                   --force-remove
#
# v1.2      : Added parameter --job to indicate that the script is called
#             from Jenkins, this will suppress asking for user confirmation
#
# v1.2.1    : fixed bugs, added sorting of arrays to provide better readable
#             output, added version printing at start of script.
#
# v1.2.2    : replaced actual deleting with printout of delete command for
#             testing
#
# v1.2.3    : Changed printouts to be more clear, removed spelling errors
#
# v1.2.4    :
#
# v1.2.5    : Adapted log file layout, renamed @/$header_text to @/$log_text
#
# v1.2.6    : Changed behaviour user anonymous is no longer allowed, instead
#             use artifactory token
#
# v1.2.7    : Added option --remove-all to remove all files in provided directory
#
# v1.3.0    : Use secure artifactory as provider for the protected packages
#
# v1.3.1    : Minor update to include script extension
#
# use:
#     https://arm.seli.gic.ericsson.se/ui/repos/tree/General/proj-5g-bsf-generic-local/eiffelesc
# to copy (selecting: copy to a custom path) packages to secure artifactory:
#     https://arm.seli.gic.ericsson.se/ui/repos/tree/General/proj-5g-sc-generic-local/GA-Versions
#

use strict;
use Cwd;
use Getopt::Long;            # standard Perl module
use Storable;
use Time::HiRes qw(gettimeofday tv_interval);
use POSIX qw(strftime);

# ********************************
# *                              *
# * Global Variable declarations *
# *                              *
# ********************************
##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
my $REVISION = "1.3.1";
my $provided_uri;
my $provided_dir;
my $provided_date;
my $provided_string;
my $keep_latest;
my $dry_run;
#my $start_date;
#my $end_date;
#my $start_limit;        # indicates if date should be included or not eg > or >=
#my $end_limit;          # indicates if date should be included or not eg < or =<
my %value;
my $sha256_count=0;
my $targz_count=0;
my @work_var_file_date;
my @work_var_enddate;
my @work_var_temp;
my $temp_var;
my $valid_entry;
my $user_startlength;
my $user_endlength;
my @user_startdate;
my @user_enddate;
my $date_seperator;
my $number_of_valid_files=0;
my $artifactory_link = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/";
my $secure_artifactory_link = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/";
my $curl_del_cmd;
my %dir_content;
my @filenames;
my $number_of_files;
my $total_number_of_files;
my @files_to_remove;

my $SILENT;
my $NO_ARTIFACTORY_PROGRESS = 1;
my $DEBUG_ON                        = 0;
my $DEBUG_INDENT                    = 0;
my @DEBUG_THIS;

# * FIXED Global Variable declarations *
##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
my %MONTH                           = ("Jan" => 1, "Feb" => 2, "Mar" => 3, "Apr" =>  4, "Mai" =>  5, "Jun" =>  6,
                                       "Jul" => 7, "Aug" => 8, "Sep" => 9, "Oct" => 10, "Nov" => 11, "Dec" => 12);

my %PROTECTED     = (
                        "EVNFM_n103-ccd-pccpcg-vnflcm.xml"                                  => 1,
                        "ESC1.2_DAFT_CXP9036965_99_R1A_20210104_122850.tar.gz"              => 1,
                        "ESC1.4_DAFT_CXP9036965_99_R1A_20210922_112126.tar.gz"              => 1,
                        "ESC1.5_DAFT_CXP9036965_99_R1A_20220105_165653.tar.gz"              => 1,
                        "ESC1.6_DAFT_CXP9036965_99_R1A_20220214_061818.tar.gz"              => 1,
                        "ESC1.7_DAFT_CXP9036965_99_R1A_20220302_102853.tar.gz"              => 1
                    );

my $SYSTEM_PROTECT = 4;
my @source;
    $source[0] = "Reserved";
    $source[1] = "Hardcoded in script";
    $source[2] = "Parameter --protect";
    $source[3] = "Parameter --protect-list ";
    $source[4] = "Directory still containing files";
    $source[5] = "Copy available on Secure Artifactory";

# ********************************
# *                              *
# * Subroutines                  *
# *                              *
# ********************************
##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub mprint {
    my $text    = $_[0];
    my $option  = $_[1];

    print $text if (!$SILENT && ($option ne "HIDE"));
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub dprint {
    my ($package, $filename, $line) = caller;
    my $text    = $_[0];
    my $linenum = $_[1];
    my $indent  = $_[2];

    if ($DEBUG_ON) {
        if ((!$indent) && ( ($linenum eq "-") || ($linenum eq "+") ) ) {
            $indent = $linenum;
            $linenum = "";
        }
        if (($indent eq "-") && ($DEBUG_INDENT > 0)) { $DEBUG_INDENT--; }
        $text = ' ' x (2*$DEBUG_INDENT).$text;

        mprint "DEBUG:";
        my $line_num;
        if ($linenum) {
            $line_num = sprintf("%5s",$linenum);
        } else {
            $line_num = sprintf("%5s",$line);
        }
        mprint(" ($line_num) $text\n");
        if (($indent eq "+")) { $DEBUG_INDENT++; }
    }
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub clear_hash {
    my %hash_to_clear     = %{$_[0]};
    foreach my $hash_key (keys %hash_to_clear) {
        delete $hash_to_clear{$hash_key};
    }
    return %hash_to_clear;
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub load_secure_artifactory_content {
    my %arguments = %{$_[0]};
    my @return_entries;

    my %parameters;
    my %this_dir_content;
    my @entries;

    # First we do the GA-Versions
    if (!exists($arguments{"NO-GA-VERSIONS"})) {
        mprint("Loading GA Versions from secure artifactory");
        $parameters{"URI"} = $secure_artifactory_link."GA-Versions/";
        $parameters{"CHECKSUM"} = 1;
        $parameters{"ARTIFACTORY-TOKEN"} = $arguments{"ARTIFACTORY-TOKEN"};
        %this_dir_content = load_dir_content(\%parameters);
        @entries = keys %this_dir_content;
        foreach my $subdir (@entries) {
            $parameters{"URI"} = $secure_artifactory_link."GA-Versions/".$subdir;
            mprint(".");
            my %sub_dir_content = load_dir_content(\%parameters);
            my @subdir_entries = keys %sub_dir_content;
            foreach (@subdir_entries) {
                push(@return_entries,"$_");
            }
        }
        mprint("\n");
    }

    #Next we do the daft versions
    if (!exists($arguments{"NO-DAFT-VERSIONS"})) {    mprint("Loading daft Versions from secure artifactory");
        $parameters{"URI"} = $secure_artifactory_link."esc-daft/";
        %this_dir_content = load_dir_content(\%parameters);
        @entries = keys %this_dir_content;
        foreach my $subdir (@entries) {
            mprint(".");
            push(@return_entries,"$subdir");
        }
        mprint("\n");
        if ($DEBUG_ON)
        {
            mprint("\nFiles/directories to protect as per secure artifactory:\n");
            foreach (@return_entries) {
                mprint(sprintf("   %-70s ",$_));
                if (substr($_, -1) eq "/") {
                    mprint("GA   version\n");
                } else {
                    mprint("daft version\n");
                }
            }
            mprint("\n");
        }
    }
    return @return_entries;
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub prompt_user {
    my $output_prompt   = $_[0];
    my $accepted_values = $_[1];
    my %testvalue = ("YESNO" => 1, "STR" => 1, "ANYKEY" => 1, "ALL" => 1);
    my $return_value;
    if ((!$output_prompt) && (!$accepted_values)) {
        $accepted_values = "ALL";
    } elsif (!$accepted_values) {
        if ($testvalue{$output_prompt}) {
            $accepted_values = $output_prompt;
            $output_prompt = "";
        }
    }
    if (!$testvalue{$accepted_values}) {
        $accepted_values = "ALL"; # Can be adapted to return an error because the value is actually unknown
    }
    if ($output_prompt) {
        print($output_prompt);
    }
    my $valid_return;
    if (($accepted_values eq "ALL") || ($accepted_values eq "ANYKEY")) {
        use Term::ReadKey;
        ReadMode('cbreak');
        $valid_return = ReadKey(0);
        ReadMode('normal');
        $valid_return = 1;
    } else {
        while (!$valid_return) {
            $return_value = <STDIN>;
            if (($accepted_values ne "ALL") && ($accepted_values ne "ANYKEY")) {
                chomp $return_value;
            }
            if ($accepted_values eq "YESNO") {
                if ((uc($return_value) eq "Y") || (uc($return_value) eq "N")) {
                    $valid_return = 1;
                }
            }
            if ($accepted_values eq "STR") {
                if ($return_value) {
                    $valid_return = 1;
                }
            }
            if (($accepted_values eq "ALL") || ($accepted_values eq "ANYKEY")) {
                $valid_return = 1;
            }
        }
    }
    return $return_value;
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub show_help {
    my $help_option = $_[0];
    mprint <<EOF;

Description:
============

This script cleans a specified artifactory directory by removing files that fullfill the cleanup criteria

Syntax:
=======

 $0 [<OPTIONAL>][<MANDATORY>]


    <MANDATORY> are one or more of the following parameters (EXCEPT IF -h / --help IS GIVEN):

    --uri                       Path to clean
                                If provided, this parameter overrides --directory

    --directory                 Directory on artifactory to clean, if this parameter is provided then
                                the --uri is not needed
                                    netwok_config
                                    esc-daft
                                    eiffelesc/SC1.2.2/
                                It is assumed that the directories exist on:
                                    https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/

    <OPTIONAL> are one or more of the following parameters:

    -h / --help                 Shows this help

    --protect                   Provide filename of file that should not be removed, this parameter can
                                be entered several times: --protect file_a.xml --protect file_b.xml
                                Can be combined with --protect-list
                                Default protected files are:
                                    EVNFM_n103-ccd-pccpcg-vnflcm.xml
                                    ESC1.2_DAFT_CXP9036965_99_R1A_20210104_122850.tar.gz
                                    ESC1.4_DAFT_CXP9036965_99_R1A_20210922_112126.tar.gz
                                    ESC1.5_DAFT_CXP9036965_99_R1A_20220105_165653.tar.gz
                                    ESC1.6_DAFT_CXP9036965_99_R1A_20220210_092418.tar.gz

    --protect-list-filename     Filename that contains a list of files that should not be removed.
                                    --protect-list files_to_keep.txt
                                Can be combined with --protect

    --filter-date               Date frame for removing files following format:
                                    "yyyy-mm-dd"                    or  "dd-mm-yyyy"                    Specific day
                                    "yyyy-mm"                       or  "mm-yyyy"                       Specific month
                                    "yyyy"                                                              Specific year
                                    "> yyyy-mm-dd"                  or  "> dd-mm-yyyy"                  After Specific day (or month or year)
                                    "< yyyy-mm-dd"                  or  "< dd-mm-yyyy"                  Before Specific day (or month or year)
                                    "<= yyyy-mm-dd"                 or  "<= dd-mm-yyyy"                 Before (including) Specific day (or month or year)
                                    "yyyy-mm-dd - yyyy-mm-dd"       or  "dd-mm-yyyy - dd-mm-yyyy"       Between two dates, exluding provided dates
                                    "[yyyy-mm-dd - yyyy-mm-dd"      or  "[dd-mm-yyyy - dd-mm-yyyy"      Between two dates, including first date
                                    "yyyy-mm-dd - yyyy-mm-dd]"      or  "dd-mm-yyyy - dd-mm-yyyy]"      Between two dates, including second date
                                    "[yyyy-mm-dd - yyyy-mm-dd]"     or  "[dd-mm-yyyy - dd-mm-yyyy]"     Between two dates, including provided dates

    --filter-string             Limit operations to the provided string only

    --keep-latest               This will only keep the latest version of all existing versions

    --dry-run                   Provide list of files that will be removed without the dry-run option


    --remove-dirs               Also remove directories

    --force-remove              Force removal of directories protected by the script (directories that still contain files)
                                This will NOT remove directories which are either hardcode protected or protected by
                                --protect or --protect-list-filename


    In case options (--filter-date, --filter-string, --keep-latet) are combined then the sequence is as follows:
    First the script will limit the files on --filter-string, the resulting list will then be processed by --filter-date (if
    this parameter is provided) then the remaining files will be processed by --keep-latest (if this parameter is provided).


    EXAMPLES:

    $0 --uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/network_config/ --filter-date "8-2-2022"

    $0 --uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/network_config/ --filter-date ">1-2-2022"

    $0 --config --filter-date "<8-2-2022" --dry-run

    $0 --daft --filter-date "[1-10-2021 - 31-12-2021]" --dry-run

    $0 --daft --filter-date "<8-2-2022" --dry-run --protect ESC1.6_DAFT_CXP9036965_99_R1A_20220107_105459.tar.gz --protect ESC1.6_DAFT_CXP9036965_99_R1A_20220117_093204.tar.gz --protect-list-filename protect_us
                Contect of file protect_us:
                    ESC1.5_DAFT_CXP9036965_99_R1A_20220104_005233.tar.gz
                    ESC1.5_DAFT_CXP9036965_99_R1A_20220104_011732.tar.gz
                    ESC1.5_DAFT_CXP9036965_99_R1A_20220104_013944.tar.gz
                    ESC1.5_DAFT_CXP9036965_99_R1A_20220104_023442.tar.gz

    $0 --directory /network_config/ --filter-string "N103" --keep-latest --dry-run

EOF
    if ($help_option) {
        exit 0;
    }
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub send_curl_cmd {
    my %arguments = %{$_[0]};

    my $token               = $arguments{"ARTIFACTORY-TOKEN"};
    if (!$token) {
        $token = $ENV{'ARTIFACTORY_TOKEN'};
    }
    my $cmd                 = $arguments{"COMMAND"};
    my $no_output           = $arguments{"SILENT"};
    my $cmd_type            = $arguments{"TYPE"};
    my $number_of_retries   = $arguments{"UPLOAD_RETRIES"};
    my $upload_file_name    = $arguments{"UPLOAD_FILE"};
    my $curl_token          = "-H \"X-JFrog-Art-Api:".$token."\"";
    my $debug_txt;

    dprint("==> send_curl_cmd($cmd , $no_output)","+");
    #if (!$DEBUG_ON) {
        if (($no_output) || ($NO_ARTIFACTORY_PROGRESS) || ($SILENT)) {
            if (index($cmd,"silent") == -1) {
                $cmd = " --silent $cmd";
            }
        }
    #}
    my $art_token_text = "-H \"X-JFrog-Art-Api:";
    if (index($cmd,$art_token_text) == -1) {
        dprint("Artifactory token NOT provided, check environment");
        $debug_txt = "-H \"X-JFrog-Art-Api:<TOKEN>\" ".$cmd;
        $cmd = $curl_token." ".$cmd;
    } else {
        dprint("Artifactory token provided");
        my $art_tokenstart = index($cmd, $art_token_text);
        my $art_tokenend   = index($cmd, "\" ", $art_tokenstart+1)+2;
        my $new_cmd = substr($cmd,0,$art_tokenstart).substr($cmd,$art_tokenend);
        $debug_txt = "-H \"X-JFrog-Art-Api:<TOKEN>\" ".$new_cmd;
    }
    dprint("CURL:\n   curl $debug_txt\n");
    my $curl_output = `curl $cmd`;
    if (uc($cmd_type) eq "UPLOAD") {
        while (index($curl_output, $upload_file_name) == -1) {
            # Something went wrong, retry
            mprint("\nUpload FAILED, retrying\n");
            $number_of_retries--;
            if ($number_of_retries > 0) {
                sleep(10);
                $curl_output = `curl $cmd`;
                #$curl_output = send_curl_cmd("-f -k -H \"X-JFrog-Art-Api:$artifact_token\" --upload-file $filename $file_to_process",$NO_ARTIFACTORY_PROGRESS);
            } else {
                mprint("$curl_output\n");
                mprint("Uploading of $upload_file_name failed $number_of_retries times, exiting script\n");
                exit 1;
            }
        }
    }
    dprint("return from curl command:\n$curl_output\n\n");
    dprint("<== send_curl_cmd()\n","-");
    return $curl_output;
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub curl_check_succes {
    my $curl_return     = $_[0];
    my $file_location   = $_[1];
    my $exit_on_error   = $_[2];
    my $silent          = $_[3];
    dprint("==> curl_check_succes(curl_return: <content_of_curl_cmd> , file_location: $file_location ,exit_on_error: $exit_on_error ,silent: $silent)","+");
    my $error=0;

    if (index($curl_return,"errors") == -1) {
        dprint("curl_check_succes() => Succes - $error","-");
        return $error;  # Succes
    }
    if (index($curl_return,"status") != -1) {
        if (index($curl_return,"404") != -1) {
            # netconf files not found
            $error = 404;
            if (!$silent) { mprint("\nERROR 404, directory does not exist: \n    $file_location\n"); }
        } elsif ((index($curl_return,"401") != -1) || (index($curl_return,"403") != -1)){
            # not authorized
            $error = 401;
            if (!$silent) { mprint("\nERROR 401 or 403, NOT AUTHORIZED to access: \n    $file_location\n"); }
        } elsif (index($curl_return,"28")) {
            $error =28;
            if (!$silent) { mprint("\nERROR 28, Timeout: \n    $file_location\n"); }
        }
    } else {
        $error = 999; # Unkown Error
        if (!$silent) {
            mprint("\nERROR Please investigate\n");
            mprint("\n$curl_return\n");
        }
    }
    if ($exit_on_error) {
        if (!$silent) { mprint("webhook NOT sent\n"); }
        exit 0;
    }
    dprint("<== curl_check_succes() => Failed - $error","-");
    return $error; # Fail
}


##########################################################
#                                                        #
#    RETRIEVE FILE INFO FROM FULL PATH                   #
#                                                        #
##########################################################
sub retrieve_fileinfo {
    my $full_path       = $_[0];
    my $only_file_name;
    my $path_to_file;
    my $comment;
    my $divider_pos = rindex($full_path,"/");
    if ($divider_pos != -1) {
        if ($divider_pos < length($full_path)) {
            $only_file_name = substr($full_path,$divider_pos+1);
            $path_to_file = substr($full_path,0,$divider_pos+1);
        }
    }
    if ($only_file_name) {
        $full_path = $only_file_name;
    }
    $divider_pos = rindex($full_path,"[");
    if ($divider_pos != -1) {
        if ($divider_pos < length($full_path)) {
            $only_file_name = substr($full_path,0,$divider_pos);
            $comment        = substr($full_path,$divider_pos);
        }
    }
    $only_file_name =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
    $path_to_file   =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
    $comment        =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
    if ($comment) {
        $only_file_name = sprintf("%-90s $comment",$only_file_name);
    }
    return ($only_file_name,$path_to_file,$comment);
}


##########################################################
#                                                        #
#    LOAD DIRECTORY CONTENT FROM ARTIFACTORY             #
#                                                        #
##########################################################
sub load_dir_content {
    my %received_arguments     = %{$_[0]};
    if ($DEBUG_ON) {
        my ($package, $filename, $line) = caller;
       $line = sprintf("%5s",$line);
        mprint("DEBUG: ($line) ==> load_dir_content( ");
        foreach my $arg(keys %received_arguments) {
            mprint("[$arg : $received_arguments{$arg}] ");
        }
        mprint(")\n");
    }
    my %MONTH                       = ("Jan" => "01", "Feb" => "02", "Mar" => "03", "Apr" => "04", "May" => "05", "Jun" => "06",
                                       "Jul" => "07", "Aug" => "08", "Sep" => "09", "Oct" => "10", "Nov" => "11", "Dec" => "12");

    my $uri             = $received_arguments{"URI"};
    my $exit_on_error   = 0;
    my $ignore_chksum   = 0;
    my $store_full_path = 0;

    push(@DEBUG_THIS,$uri);

    my $ignore_checksum_files;
    if ($received_arguments{"EXIT_ON_ERROR"}) {
        $exit_on_error = 1;
    }
    if ($received_arguments{"CHECKSUM"}) {
        $ignore_checksum_files = 1;
    }
    if ($received_arguments{"PATH_TO_STORE"} eq "FULL") {
        $store_full_path = 1;
    }

    my %arguments;
        $arguments{"ARTIFACTORY-TOKEN"} = $received_arguments{"ARTIFACTORY-TOKEN"};
        $arguments{"COMMAND"}           = $uri;
        $arguments{"SILENT"}            = $NO_ARTIFACTORY_PROGRESS;
        $arguments{"TYPE"}              = "GET_DIRECTORY";
        $arguments{"UPLOAD_RETRIES"}    = 1;
        $arguments{"UPLOAD_FILE"}       = "";
    my $content = send_curl_cmd(\%arguments);
    my %content_list;
    my $offset=0;
    my $remainder = substr($content,$offset);
    if (!curl_check_succes($content,$uri,$exit_on_error)) {
        $offset = index($remainder,"<a href=\"");
        while ($offset != -1) {
            my $filename;
            my $date_time;
            my $file_size;
            my @split_line;
            $remainder = substr($remainder ,$offset+length("<a href=\""));
            $filename  = substr($remainder , index($remainder,"\">")+2);
            $filename  = substr($filename  , 0 , index($filename,"<"));
            $date_time = substr($remainder , index($remainder,"</a>")+4);
            $date_time = substr($date_time , 0 , index($date_time,"\n")+4);
            $date_time =~ s/^\s+//;
            @split_line = split(" ",$date_time);
            if ($filename ne "../") {
                if ($store_full_path) {
                    if (substr($filename,-1) ne "/") {
                        #my $fullpath = substr($uri,length($artifactory_link));
                        #$filename = $fullpath.$filename;
                        my $split_line_size = @split_line;
                        $filename = $uri.$filename;
                        $file_size = $split_line[2];
                        if ($split_line_size > 3) {
                            if ($split_line[3] eq "GB") {
                                $file_size = sprintf("%2.0f", $file_size * (1024**3));
                            }
                            if ($split_line[3] eq "MB") {
                                $file_size = sprintf("%2.0f", $file_size * (1024**2));
                            }
                            if ($split_line[3] eq "KB") {
                                $file_size = sprintf("%2.0f", $file_size * 1024);
                            }
                        }
                    } else {
                        $filename = $filename;
                    }
                }

                if (substr($filename,-1) eq "/") {
                     $content_list{$filename}{"F-TYPE"} = "D";
                } else {
                     $content_list{$filename}{"F-TYPE"} = "F";
                }
                my %api_args;
                $api_args{"URI"} = $received_arguments{"URI"};
                my %api_folder_info = api_folder_info(\%api_args);
                foreach my $key (keys %api_folder_info) {
                    $content_list{$filename}{$key} = $api_folder_info{$key};
                }
                if ($ignore_checksum_files) {
                    if ((index($filename,".sha256") == -1) && (index($filename,".md5") == -1)) {
                        #dprint("$filename  :   $split_line[0] $split_line[1]\n");
                        $content_list{$filename}{"DATE-TIME"} = $split_line[0]." ".$split_line[1];
                        $content_list{$filename}{"SIZE"}      = $file_size;
                        dprint("Added $filename $content_list{$filename}  Filter on checksum ACTIVE");
                    }
                } else {
                    $content_list{$filename}{"DATE-TIME"} = $split_line[0]." ".$split_line[1];
                    $content_list{$filename}{"SIZE"}      = $file_size;
                    dprint("Added $filename $content_list{$filename}");
                }
            }
            $offset = index($remainder,"<a href=\"");
        }
    }
    dprint("<== load_dir_content()","-");
    return %content_list;
}



##########################################################
#                                                        #
#    CHECK IF DIRECTORY CONTENT IS EMPTY OR NOT          #
#                                                        #
##########################################################
sub dir_is_empty {
    my $full_path       = $_[0];

    my $filename;
    my $path;
    my %parameters;
    my %directory_content;
    if (substr($full_path,-1) ne "/") {
        ($filename,$path)   = retrieve_fileinfo($full_path);
    } else {
        $path = $full_path;
    }
    $parameters{"URI"}  = $path;
    %directory_content  = load_dir_content(\%parameters);
    if (%directory_content) {
        return "";
    }
    return $path;
}



##########################################################
#                                                        #
#    READ DIRECTORY INFORMATION USING API FROM           #
#    ARTIFACTORY                                         #
#                                                        #
#    Example:                                            #
#       curl -H "X-JFrog-Art-Api:<token>"                #
#            -s https://armdocker.rnd.ericsson.se/artifactory/api/storage/proj-5g-bsf-generic-local/eiffelesc   #
#            -H "content-type:text/plain"                #
##########################################################
sub api_folder_info {
    my %received_arguments     = %{$_[0]};
    dprint("sub api_folder_info(".$received_arguments{"URI"}.")","+");
    my $link            = $received_arguments{"URI"};
    my $repo_and_folder = substr($link,index($link,"proj-"));
    my $info_url        = "https://armdocker.rnd.ericsson.se/artifactory/api/storage";
    my $content_type    = "-H \"content-type:text/plain\"";
    my $file_list       = ' -d \'items.list({"deep":"1"},{"depth":"3"},{"listFolders":"1"})\' ';
#    my $file_list       = ' -d \'items.list({"deep":"1"},{"listFolders":"1"})\' ';
    

# download_csar : -d 'items.find({"repo":{"$eq":"proj-5g-bsf-generic-local"}},{"type":{"$eq":"folder"}},{"name":{"$match":"1.10.4+21"}})'
# cleanup_arti  : -d 'items.list({"deep":"1"},{"depth":"3"},{"listFolders":"1"})'

    my %return_value;

    my %curl_arguments;
        $curl_arguments{"ARTIFACTORY-TOKEN"} = $received_arguments{"ARTIFACTORY-TOKEN"};
        $curl_arguments{"COMMAND"}           = "-s $info_url/$repo_and_folder $file_list $content_type";
        $curl_arguments{"SILENT"}            = $NO_ARTIFACTORY_PROGRESS;
        $curl_arguments{"TYPE"}              = "GET_DIRECTORY_INFO";
    my $content = send_curl_cmd(\%curl_arguments);
    
    if (index($content,"createdBy") != -1) {
        $return_value{"CREATED_BY"} = substr($content,index($content,"createdBy")+length("createdBy")+5);    # the +5 is because format is: "createdBy" : "  so the +5 catches the quotationmarks and colon
        $return_value{"CREATED_BY"} = substr($return_value{"CREATED_BY"},0,index($content,",")-1);          # -1 because the comma is preceeded by a quotationmark
    }
    if (index($content,"modifiedBy") != -1) {
        $return_value{"MODIFIED_BY"} = substr($content,index($content,"modifiedBy")+length("modifiedBy")+5);
        $return_value{"MODIFIED_BY"} = substr($return_value{"MODIFIED_BY"},0,index($content,",")-1);
    }
    if (index($content,"size") != -1) {
        $return_value{"SIZE_IN_BYTES"} = substr($content,index($content,"size")+length("size")+5);
        $return_value{"SIZE_IN_BYTES"} = substr($return_value{"SIZE_IN_BYTES"},0,index($content,",")-1);
    }
    dprint("end api_folder_info()","-");
    return %return_value;
}


##########################################################
#                                                        #
#    LOAD DIRECTORY STRUCTURE (RECURSIVE) FROM           #
#    ARTIFACTORY                                         #
#                                                        #
##########################################################
sub read_directory {
    my %arguments     = %{$_[0]};
    dprint("sub read_directory(".$arguments{"URI"}." , ".$arguments{"NOT_RECURSIVE"}." , ".$arguments{"COUNTER"}." , ".$arguments{"PATH_TO_STORE"}.")","+");
    #if ($DEBUG_ON) {
    #    my ($package, $filename, $line) = caller;
    #   $line = sprintf("%5s",$line);
    #    mprint("DEBUG: ($line) ==> read_directory( ");
    #    foreach my $arg(keys %arguments) {
    #        mprint("[$arg : $arguments{$arg}] ");
    #    }
    #    mprint(")\n");
    #}
    if (!(exists $arguments{"RECURSIVE_CALL"})) {
        mprint("READING DIRECTORY ".$arguments{"URI"}." FROM ARTIFACTORY\n");
    }

    my $directory_name      = $arguments{"URI"};
    my $no_recursive_read   = $arguments{"NOT_RECURSIVE"};
    my $counter = 0;

    if (exists $arguments{"COUNTER"}) {
        $counter = $arguments{"COUNTER"};
    }

    if (!$DEBUG_ON) {
        if ($counter == 2) {
            #mprint("\nLoading directory $directory_name ");
            mprint(".");
        }
        #} elsif ($counter > 2) {
        #    mprint(".");
        #}

        if ($counter > 5) {
            $counter = 1;
        } else {
            $counter++;
        }
    }
    my %parameters;
    $parameters{"ARTIFACTORY-TOKEN"} = $arguments{"ARTIFACTORY-TOKEN"};
    $parameters{"URI"}               = $directory_name;
    $parameters{"CHECKSUM"}          = 1;
    if ($arguments{"PATH_TO_STORE"}) {
        $parameters{"PATH_TO_STORE"} = $arguments{"PATH_TO_STORE"};
    }
    $parameters{"PATH_TO_STORE"} = "FULL";
    my %entries = load_dir_content(\%parameters);

    my @filenames = keys %entries;
    my $number_of_entries = @filenames;                       # needed later to adjust the first entries to include the complete link


    if (!$no_recursive_read) {
        foreach my $local_entry (@filenames) {
            if (substr($local_entry,-1) eq "/") {       # Check if another directory
                $parameters{"ARTIFACTORY-TOKEN"} = $arguments{"ARTIFACTORY-TOKEN"};
                $parameters{"URI"}              = $directory_name.$local_entry;
                $parameters{"CHECKSUM"}         = 1;
                $parameters{"COUNTER"}          = $counter;
                $parameters{"RECURSIVE_CALL"}   = 1;
                if ($arguments{"PATH_TO_STORE"}) {
                    $parameters{"PATH_TO_STORE"} = $arguments{"PATH_TO_STORE"};
                }

                my %recursive_entries = read_directory(\%parameters);

                foreach my $recursive_entry (keys %recursive_entries)
                {
                    $entries{$recursive_entry}{"DATE-TIME"} = $recursive_entries{$recursive_entry}{"DATE-TIME"};
                    $entries{$recursive_entry}{"SIZE"} = $recursive_entries{$recursive_entry}{"SIZE"};
                }
                delete $entries{$local_entry};
            }
        }
    }
    dprint("end read_directory() = %entries","-");
    return %entries;
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub remove_zeros_from_date {
    my $date    = $_[0];
    dprint("remove_zeros_from_date($date)");

    if (index($date,"-") != -1) {
        $date_seperator = "-";
    } elsif (index($date,".") != -1) {
        $date_seperator = '.';
    }

    dprint("  using date seperator: $date_seperator");
    my @datesplit = split($date_seperator,$date);
    my $size = @datesplit;
    my $updated_date;
    my $date_format_YMD;
    my $date_format_DMY;
    foreach my $work (@datesplit) {
        dprint("\$work = $work");
        $work =~ s/^0+(?=[0-9])//; # remove leading 0's
        if ((!$date_format_YMD) && (!$date_format_DMY)) {
            if ($work > 32) {
                $date_format_YMD = 1;   dprint("Date Format = YYYY-MM-DD, since first entry $work > 32");
            } else {
                $date_format_DMY = 1;   dprint("Date Format = DD-MM-YYYY, since first entry $work <= 32");
            }
        }
        if ($updated_date) {
            $updated_date .= $date_seperator.$work;
        } else {
            $updated_date = $work;
        }
    }
    if ($date_format_YMD) {
        my @datesplit2 = split($date_seperator,$updated_date);
        dprint("Convert date");
        dprint("    From : $date");
        if ($size == 3) {
            $updated_date = $datesplit2[2].$date_seperator.$datesplit2[1].$date_seperator.$datesplit2[0]
        } elsif ($size == 2) {
            $updated_date = $datesplit2[1].$date_seperator.$datesplit2[0];
        }
        dprint("    To   : $updated_date");
    }
    $date = $updated_date;
    dprint("remove_zeros_from_date() => $date");
    return $date;
}


##########################################################
#                                                        #
#                                                        #
#                                                        #
##########################################################
sub convert_date {
    my $date            = $_[0]; # convert 01-Feb-2000 to 1-2-2000 (if what_to_return is equal to "DMY"
    my $what_to_return  = $_[1]; # DMY : Day-Month-Year  MY : Month-Year  Y: Year
    my $date_seperator  = $_[2]; # character to use as date seperator - or . or userdefined
    dprint("convert_date($date, $what_to_return, $date_seperator)");

    my @work_var_file_date  = split("-",$date);
    my $converted_date      = "";
    if (!$date_seperator) {
        $date_seperator = "-";
    }

    if (uc($what_to_return) eq "DMY") {
        # Day, month and year dd.mm.yyyy
        $work_var_file_date[0] =~ s/^0+(?=[0-9])//; # remove leading 0's
        $converted_date = $work_var_file_date[0].$date_seperator.$MONTH{$work_var_file_date[1]}.$date_seperator.$work_var_file_date[2];
    } elsif (uc($what_to_return) eq "MY") {
        # month and year mm.yyyy
        $converted_date = $MONTH{"$work_var_file_date[1]"}.$date_seperator.$work_var_file_date[2];
    } else {
        # year yyyy
        $converted_date = $work_var_file_date[2];
    }

    dprint("convert_date() => $converted_date");
    return $converted_date;
}


##########################################################
#                                                        #
#    PARSE_INITIAL_DATA                                  #
#      INPUT  : Fixed date or date limit                 #
#                 dd-mm-yyyy                             #
#                 >dd-mm-yyyy                            #
#                 >=dd-mm-yyyy                           #
#                 <dd-mm-yyyy                            #
#                 <=dd-mm-yyyy                           #
#                 [dd-mm-yyyy - dd-mm-yyyy]   including  #
#                 {dd-mm-yyyy - dd-mm-yyyy}   excluding  #
#                                                        #
#      OUTPUT : Hash containing                          #
#                 Startdate                              #
#                 Startlimit > , >= , < , [ , ] , { , }  #
#                 Enddate                                #
#                 Endlimit > , >= , < , [ , ] , { , }    #
#                                                        #
##########################################################
sub parse_initial_data {
    my $date    = $_[0];
    dprint("==> parse_initial_data($date)","+");
    my %return_data_hash = ("start_date" => "" , "start_limit" => "" , "end_date" => "" , "end_limit" => "");
    my $start_date;
    my $start_limit;
    my $end_date;
    my $end_limit;
    my $working_date = $date;
    if ($working_date) {
        # check how the filter looks like
        # remove trailing and leading spaces
        $working_date =~ s/^\s+|\s+$//g;
        # check if first character is numeral or special character: < , <= , [
        my $first_char = substr($working_date,0,1);
        if ($first_char eq "<") {
            if (substr($working_date,1,1) eq "=") {
                $start_limit = "$first_char=";
                $working_date = substr($working_date,2);
            } else {
                $start_limit = $first_char;
                $working_date = substr($working_date,1);
            }
            $working_date =~ s/^\s+|\s+$//g;
        } elsif ($first_char eq ">") {
            if (substr($working_date,1,1) eq "=") {
                $start_limit = "$first_char=";
                $working_date = substr($working_date,2);
            } else {
                $start_limit = $first_char;
                $working_date = substr($working_date,1);
            }
            $working_date =~ s/^\s+|\s+$//g;
        } elsif (($first_char eq "[") || ($first_char eq "{")){
            $start_limit = $first_char;
            $working_date = substr($working_date,1);
            $working_date =~ s/^\s+|\s+$//g;
        }
        my $last_char = substr($working_date,-1);
        if (($last_char eq "]") || ($last_char eq "}")){
            $end_limit = $last_char;
            chop($working_date);
        }
        my $multi_date = index($working_date," - ");
        if ($multi_date != -1) {
            # multiple dates (begin and end)
            $start_date = substr($working_date,0,$multi_date);
            $end_date   = substr($working_date,$multi_date+3);
        } else {
            $start_date = $working_date;
        }
    }
    $start_date = remove_zeros_from_date($start_date);
    $end_date   = remove_zeros_from_date($end_date);

    $return_data_hash{"start_date"}  = $start_date;
    $return_data_hash{"start_limit"} = $start_limit;
    $return_data_hash{"end_date"}    = $end_date;
    $return_data_hash{"end_limit"}   = $end_limit;
    dprint("start_date    : $return_data_hash{\"start_date\"}");
    dprint("start_limit   : $return_data_hash{\"start_limit\"}");
    dprint("end_date      : $return_data_hash{\"end_date\"}");
    dprint("end_limit     : $return_data_hash{\"end_limit\"}");
    dprint("<== parse_initial_data()","-");
    return %return_data_hash;
}


sub print_message {
    my $which_message = $_[0];
    my @lines_in_message = split('\n',$which_message);
    my %box;

    my $maxlen=-1;
    my $singleline;
    foreach (@lines_in_message) {
        if (length($_) > $maxlen) {
            $maxlen = length($_);
        }
    }
    if ($maxlen == -1) {
        $maxlen     = length($which_message);
        $singleline = 1;
    }

    $box{"MAXLEN"}  = $maxlen;
    $box{"EDGE"}    = "!!!";
    $box{"FILLER"}  = "  ";
    $box{"SPACES"}  = " "x$maxlen;
    $box{"SPACES"}  = $box{"EDGE"}.$box{"FILLER"}.$box{"SPACES"}.$box{"FILLER"}.$box{"EDGE"};
    $maxlen         = length($box{"SPACES"});
    #$maxlen         += (2 * length($box{"EDGE"}));
    #$maxlen         += (2 * length($box{"FILLER"}));
    $box{"LINE"}    = "!"x$maxlen;

    mprint("\n");
    mprint($box{"LINE"}."\n");
    mprint($box{"SPACES"}."\n");
    if ($singleline) {
        mprint($box{"EDGE"}.$box{"FILLER"}.$which_message.$box{"FILLER"}.$box{"EDGE"}."\n");
    } else {
        foreach (@lines_in_message) {
            my $line_length = length($_);
            if ($line_length < $box{"MAXLEN"}) {
                my $spaces_to_add = $box{"MAXLEN"} - $line_length;
                $_ .= " "x$spaces_to_add;
            }
            mprint($box{"EDGE"}.$box{"FILLER"}.$_.$box{"FILLER"}.$box{"EDGE"}."\n");
        }
    }
    mprint($box{"SPACES"}."\n");
    mprint($box{"LINE"}."\n");

#    if ($which_message eq "DRY-RUN") {
#        mprint("\n");
#        mprint("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
#        mprint("!!!                                                            !!!\n");
#        mprint("!!!  Files/Directories not deleted due to parameter --dry-run  !!!\n");
#        mprint("!!!                                                            !!!\n");
#        mprint("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
#        mprint("\n");
#    }
}


##########################################################
#                                                        #
#    START OF MAIN                                       #
#                                                        #
##########################################################
my $show_help;
my $show_debug_code;
my $use_daft_dir;
my $use_config_dir;
my $use_eiffel_dir;
my @protect_files;
my $protect_list_filename;
my $show_protected_files;
my $remove_directories;
my $force_removal;
my $temp_str;
my $job;
my $auto_answer;
my $delete_file_list;
my %parameters;
my $remove_all;
my $artifact_token = $ENV{'ARTIFACTORY_TOKEN'};
my $curl_del       = "curl -f -k -H \"X-JFrog-Art-Api:$artifact_token\" -X DELETE ";
my $curl_del_print = "curl -f -k -H \"X-JFrog-Art-Api:<artifact_token>\" -X DELETE ";


##########################################################
#                                                        #
#    LOAD PROVIDED ARGUMENTS                             #
#                                                        #
##########################################################
my @Arguments = @ARGV;
my $result = GetOptions (
                        "h|help"                    => \$show_help,
                        "debug"                     => \$show_debug_code,
                        "job=s"                     => \$job,
                        "uri=s"                     => \$provided_uri,
                        "directory=s"               => \$provided_dir,
                        "daft"                      => \$use_daft_dir,
                        "config"                    => \$use_config_dir,
                        "csar"                      => \$use_eiffel_dir,
                        "d|filter-date=s"           => \$provided_date,
                        "s|filter-string=s"         => \$provided_string,
                        "keep-latest=s"             => \$keep_latest,
                        "delete-list-filename=s"    => \$delete_file_list,
                        "p|protect=s"               => \@protect_files,
                        "l|protect-list-filename=s" => \$protect_list_filename,
                        "r|remove-dirs=s"           => \$remove_directories,
                        "f|force-remove=s"          => \$force_removal,
                        "remove-all"                => \$remove_all,
                        "show-protected"            => \$show_protected_files,
                        "dry-run=s"                 => \$dry_run,
                        );
$DEBUG_ON = $show_debug_code;
my @split_scriptname = split("/",$0);
my $scriptname = substr($split_scriptname[-1],0,index($split_scriptname[-1],".pl"));
my $tmp_text;
my @log_text;

$tmp_text = "perl ".$scriptname.".pl";
my $quote_next;
foreach (@Arguments) {
    if ($quote_next) {
        $_ = "\"$_\"";
        $quote_next = 0;
    }
    $tmp_text .= " $_";
    if (($_ eq "-d") ||
        ($_ eq "-s") ||
        ($_ eq "-r") ||
        ($_ eq "-f") ||
        ($_ eq "--filter-date") ||
        ($_ eq "--filter-string") ||
        ($_ eq "--keep-latest") ||
        ($_ eq "--dry-run") ||
        ($_ eq "--remove-all") ||
        ($_ eq "--remove-dirs") ||
        ($_ eq "--force-remove")) {
        $quote_next = 1;
    }
}
$tmp_text .= "\n\n";
push(@log_text,$tmp_text);
push(@log_text,sprintf("%-29s: %-67s  %-29s: %-30s\n","Script",$scriptname,"Script version",$REVISION));
push(@log_text,sprintf("%-29s: %-67s  %-29s: %-30s\n","--dry-run",$dry_run,"--job",$job));
push(@log_text,sprintf("%-29s: %-67s  \n","--uri",$provided_uri));
push(@log_text,sprintf("%-29s: %-67s  \n","--directory",$provided_dir));
push(@log_text,sprintf("%-29s: %-67s  %-29s: %-30s\n","--filter-date",$provided_date,"Remove-All",$remove_all));
push(@log_text,sprintf("%-29s: %-67s  \n","--filter-string",$provided_string));
push(@log_text,sprintf("%-29s: %-67s  %-29s: %-30s\n","--remove-dirs",$remove_directories,"--force-remove",$force_removal));
push(@log_text,sprintf("\n"));
#$log_text[0] = sprintf("%-29s: %-67s  %-29s: %-30s\n","Script",$scriptname,"Script version",$REVISION);
#$log_text[1] = sprintf("%-29s: %-67s  %-29s: %-30s\n","--dry-run",$dry_run,"--job",$job);
#$log_text[2] = sprintf("%-29s: %-67s  \n","--uri",$provided_uri);
#$log_text[3] = sprintf("%-29s: %-67s  \n","--directory",$provided_dir);
#$log_text[4] = sprintf("%-29s: %-67s  %-29s: %-30s\n","--filter-date",$provided_date,"Remove-All",$remove_all);
#$log_text[5] = sprintf("%-29s: %-67s  \n","--filter-string",$provided_string);
#$log_text[6] = sprintf("%-29s: %-67s  %-29s: %-30s\n","--remove-dirs",$remove_directories,"--force-remove",$force_removal);
#$log_text[7] = sprintf("\n");
foreach(@log_text) {
    mprint("$_");
}

##########################################################
#                                                        #
#    STANDARD LIST OF FILES TO PROTECT                   #
#                                                        #
##########################################################
my $standard_list="protected_artifactory_files.txt";
my @standard_list_data;
my @copy_data;
my $standard_list_size=0;
my $file_location;
if (index(getcwd(),"scripts") == -1) {
    $file_location = "./scripts/$standard_list";
} else {
    $file_location = "./$standard_list";
}
mprint("LOAD STANDARD LIST OF FILES TO PROTECT ($file_location)\n");
$source[3] .= $standard_list." ";
open my $std_file_handle, '<', "$file_location";
@standard_list_data = <$std_file_handle>;
close $std_file_handle;
#if (!@standard_list_data) {
#    open my $std_file_handle, '<', "./scripts/$standard_list";
#    @standard_list_data = <$std_file_handle>;
#    close $std_file_handle;
#}
dprint(" ","+");
my $keep_no     = '<keep-on-node=no>';
my $keep_yes    = '<keep-on-node=yes>';
my $comments    = '#';
@standard_list_data = grep {!/$comments/} @standard_list_data;   # Discard comments
foreach (@standard_list_data) {
    if (substr($_,-1) eq "\n") {
        chomp($_);
    }
    $_ =~ s/$keep_yes//;    # remove <keep-on-node=yes>
    $_ =~ s/$keep_no//;     # remove <keep-on-node=no>
    $_ =~ s/^\s+|\s+$//g;   # remove leading and trailing spaces
    if ($_) {
        #$standard_list_size++;
        push(@copy_data,$_);
        $PROTECTED{$_} = 3;
        $tmp_text = sprintf("%-70s [%s",$_,$source[3]);
        mprint("$tmp_text\n");
    }
}
@standard_list_data = @copy_data;
$standard_list_size = @standard_list_data;
mprint("\n");
my %secure_arg = ("ARTIFACTORY-TOKEN" => $artifact_token);
my @secure_list_data = load_secure_artifactory_content(\%secure_arg);
my $secure_list_size = @secure_list_data;
my %test_hash;
    $test_hash{"CEO"}       = "";
    $test_hash{"MANAGER"}   = "";
    $test_hash{"HR"}        = "";
    $test_hash{"FILE"}      = \@standard_list_data;
    $test_hash{"SECURE"}    = \@secure_list_data;

    mprint("\ncompare Secure Artifactory with $standard_list:\n");
    if ($standard_list_size == $secure_list_size) {
        mprint("   Number of entries in both lists are the same ($secure_list_size)\n\n");
    }
    if ($standard_list_size < $secure_list_size) {
        my $size_diff = $secure_list_size - $standard_list_size;
        $test_hash{"CEO"}       = "SECURE";
        $test_hash{"MANAGER"}   = "FILE";
        $test_hash{"CONTROL"}   = "MANAGER";
        mprint("   Number of entries in $standard_list is smaller than number of entries on secure artifactory (difference: $size_diff)\n\n");
    }
    if ($standard_list_size > $secure_list_size) {
        my $size_diff = $standard_list_size - $secure_list_size;
        $test_hash{"CEO"}       = "FILE";
        $test_hash{"MANAGER"}   = "SECURE";
        $test_hash{"CONTROL"}   = "CEO";
        mprint("   Number of entries in $standard_list is greater than number of entries on secure artifactory (difference: $size_diff)\n\n");
    }
    my $added_new=0;
    foreach (@secure_list_data){
        if ($PROTECTED{$_} != 3) {
            mprint("  Missing file Protect: $_\n");
            $PROTECTED{$_} = 3;
            $added_new++;
        }
    }
    if (!$added_new) {
        mprint("  No new files encountered\n");
    }
    mprint("\n");

    my @list1 = @{$test_hash{$test_hash{"CEO"}}};
    my @list2 = @{$test_hash{$test_hash{"MANAGER"}}};
    @list1 = sort { lc($a) cmp lc($b) } @list1;
    @list2 = sort { lc($a) cmp lc($b) } @list2;
    my $counter=0;
    my $size2 = @list2;
    mprint(sprintf("%-70s  %-70s\n",$standard_list,"Secure artifactory"));
    foreach (@list1) {
        if ($counter < $size2) {
            if ($test_hash{"CONTROL"} eq "CEO") {
                mprint(sprintf("%-70s  %-70s\n",$_,$list2[$counter]));
            } else {
                mprint(sprintf("%-70s  %-70s\n",$list2[$counter],$_));
            }
            $counter++;
        } else {
            if ($test_hash{"CONTROL"} eq "CEO") {
                mprint(sprintf("%-70s  %-70s\n",$_," "));
            } else {
                mprint(sprintf("%-70s  %-70s\n"," ",$_));
            }
        }
    }

dprint(" ","-");
##########################################################
#                                                        #
#    SHOW FILES THAT ARE PROTECTED AND EXIT              #
#                                                        #
#    --show-protected                                    #
#                                                        #
##########################################################
if ($show_protected_files) {
    foreach (@protect_files) {
        $PROTECTED{$_} = 2;
        #$source[2] .= "--protect $_ ";
    }
    if ($protect_list_filename) {
        my @temp_data;
        $source[3] .= $protect_list_filename." ";
        open my $file_handle, '<', "./$protect_list_filename";
        @temp_data = <$file_handle>;
        close $file_handle;
        foreach (@temp_data) {
            if (substr($_,-1) eq "\n") {
                chomp($_);
            }
            $_ =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
            $PROTECTED{$_} = 3;
        }
    }

    #my @print_list;
    #mprint("Hardcoded files to protect:\n");
    #push(@print_list,"Hardcoded files to protect:");
    my $print_list;
    $print_list = "Hardcoded files to protect:\n";
    foreach my $name (keys %PROTECTED)
    {
        #push(@print_list,"   $name");
        $print_list .= "   $name\n";
    }
    #print_message(\@print_list);
    print_message($print_list);
    exit 0;
}

##########################################################
#                                                        #
#    IN CASE THE SCRIPT IS CALLED BY JENKINS THEN        #
#    DO NOT REQUEST FOR USER INPUT, ASSUME "Y" FOR       #
#    ALL CASES                                           #
#                                                        #
#    --job                                               #
#                                                        #
##########################################################
if ($job) {
    $auto_answer = "Y";
    print_message("HARDCODED: Script is called from a (Jenkins) job, act as if '--dry-run TRUE' was used");
    $dry_run     = 1;
    $auto_answer = "";
}


##########################################################
#                                                        #
#    CONVERT PROVIDED TRUE/FALSE/YES/NO ARGUMENTS        #
#    TO 0: FALSE OR 1: TRUE                              #
#                                                        #
#    ARGUMENTS TO CONSIDER:                              #
#      --dry-run                                         #
#      --keep-latest                                     #
#      --remove-dirs                                     #
#      --force-remove                                    #
#                                                        #
##########################################################
if ($dry_run) {
    if ((uc substr($dry_run,0,1) eq "T") || (uc substr($dry_run,0,1) eq "Y") || ($dry_run == 1)) {
        $dry_run = 1;
    } else {
        $dry_run = 0;
    }
}

if ($keep_latest) {
    if ((uc substr($keep_latest,0,1) eq "T") || (uc substr($keep_latest,0,1) eq "Y") || ($keep_latest == 1)) {
        $keep_latest = 1;
    } else {
        $keep_latest = 0;
    }
}

if ($remove_directories) {
    if ((uc substr($remove_directories,0,1) eq "T") || (uc substr($remove_directories,0,1) eq "Y") || ($remove_directories == 1)) {
        $remove_directories = 1;
    } else {
        $remove_directories = 0;
    }
}

if ($remove_all) {
    if ((uc substr($remove_all,0,1) eq "T") || (uc substr($remove_all,0,1) eq "Y") || ($remove_all == 1)) {
        $remove_all = 1;
    } else {
        $remove_all = 0;
    }
}

if ($force_removal) {
    if ((uc substr($force_removal,0,1) eq "T") || (uc substr($force_removal,0,1) eq "Y") || ($force_removal == 1)) {
        $force_removal = 1;
    } else {
        $force_removal = 0;
    }
}

##########################################################
#                                                        #
#    PREPARE FOR GOING TO BATTLE                         #
#                                                        #
##########################################################
$parameters{"ARTIFACTORY-TOKEN"} = $artifact_token;
if ($dry_run) {
    print_message("Files/Directories not deleted due to parameter --dry-run");
}

if ($remove_all) {
    if ((!$provided_date) && (!$provided_string)){
        mprint("Change filtered date to: >1-1-2000\n");
        $provided_date = "\>1-1-2000";
    } else {
        if (!$provided_date) {
            mprint("ERROR --removal-all is used in combination with --filter-date\n");
        }
        if (!$provided_string) {
            mprint("ERROR --removal-all is used in combination with --filter-string\n");
        }
        mprint("ERROR --removal-all may not be used with options --filter-xxx)\n");
        exit 1;
    }
}

if (($force_removal) && ($auto_answer ne "Y")) {
    mprint("WARNING --force-removal is used, this will remove directories which are not empty\n");
    my $user_response = prompt_user("Are you sure you want to proceed? (Y/N) : ","YESNO");
    if (!(uc($user_response) eq "Y")) {
        exit 0;
    }
}

if ((!$provided_uri) && ($provided_dir)) {
    if (substr($provided_dir,-1) ne "/") {
        $provided_dir .= "/";
    }
    if (substr($provided_dir,0,1) eq "/") {
        $provided_dir = substr($provided_dir,1);
    }
    $provided_uri = $artifactory_link.$provided_dir;
}

if ((!$provided_uri) && ($use_daft_dir)) {
    $provided_uri = $artifactory_link."esc-daft/";
}
if ((!$provided_uri) && ($use_config_dir)) {
    $provided_uri = $artifactory_link."network_config/";
}
#if ((!$provided_uri) && ($use_eiffel_dir)) {
#    $provided_uri = $artifactory_link."eiffelesc/";
#}

if ($delete_file_list) {
    $provided_uri = $artifactory_link;
}

if ((!$provided_uri) || ($show_help)) {
    show_help($show_help);
    mprint("\nPLEASE PROVIDE URI OR DELETE-FILE-LIST\n");
    exit(1);
}

if (substr($provided_uri,-1) ne "/") {
    mprint("PROVIDED URI IS INCORRECT : $provided_uri\n");
    exit(1);
}

foreach (@protect_files) {
    $PROTECTED{$_} = 2;
}

if ((!$provided_date) && (!$keep_latest) && (!$provided_string)) {
    if (!$delete_file_list) {
        $provided_date = "1-1-2000";
    }
}

##########################################################
#                                                        #
#    LOAD PROTECTED FILES FROM FILE                      #
#                                                        #
#    --protect-list-filename                             #
#                                                        #
##########################################################
if ($protect_list_filename) {
    mprint("LOAD PROTECTED FILES --protect-list-filename $protect_list_filename\n");
    my @temp_data;
    $source[3] .= $protect_list_filename." ";
    open my $file_handle, '<', "./$protect_list_filename";
    @temp_data = <$file_handle>;
    close $file_handle;
    foreach (@temp_data){
        if (substr($_,-1) eq "\n") {
            chomp($_);
        }
        $_ =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
        $PROTECTED{$_} = 3;
    }
}

$number_of_valid_files=0;


##########################################################
#                                                        #
#    USE FILE CONTAINING WHICH FILES/DIRECTORIES         #
#    SHOULD BE REMOVED                                   #
#                                                        #
#    --delete-list-filename                              #
#                                                        #
##########################################################
if ($delete_file_list) {
    mprint("LOAD FILES TO DELETE --delete-list-filename $delete_file_list\n");
    mprint("Comparing list of files to remove in $delete_file_list against files on artifactory, this can take a while\n");
    $remove_directories = 1;
    $parameters{"PATH_TO_STORE"} = "FULL";
    if (!$provided_dir) {
        #%dir_content = read_directory($provided_uri,1);
        $parameters{"URI"} = $provided_uri;
    } else {
        #%dir_content = read_directory($provided_uri.$provided_dir,1);
        $parameters{"URI"} = $provided_uri.$provided_dir;
    }
    %dir_content = read_directory(\%parameters);
    mprint("\n");

    #store \%dir_content, 'eedwiq_hash.txt';
    @filenames = keys %dir_content;
    $number_of_files = @filenames;
    $total_number_of_files = $number_of_files;
    mprint("\n\$total_number_of_files = $total_number_of_files\n");
    my @temp_data;
    open my $file_handle, '<', "./$delete_file_list";
    @temp_data = <$file_handle>;
    close $file_handle;
    $file_handle = @temp_data;
    mprint("Number of entries in $delete_file_list is $file_handle\n");
    foreach my $check_file (@temp_data){
        if (substr($check_file,-1) eq "\n") {
            chomp($check_file);
        }
        $check_file =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
        mprint("Checkfile : $check_file\n");
        foreach my $target_file (keys %dir_content) {
            if (index($target_file,$check_file) != -1) {
                mprint("REMOVE : $target_file\n");
                push(@files_to_remove,$target_file);
                $number_of_valid_files++;
            }
        }
    }
    if (($provided_string) || ($provided_date) || ($keep_latest)){
        @filenames = @files_to_remove;
        if ($DEBUG_ON) {
            foreach(@filenames){
                dprint("$_\n");
            }
        }
    }
} else {
    #%dir_content = read_directory($provided_uri,1,1);
    mprint("START FILTERING FILES TO DELETE\n");
    $parameters{"URI"} = $provided_uri;
    #$parameters{"PATH_TO_STORE"} = "FULL";
    if (!$provided_dir) {
        $parameters{"NOT_RECURSIVE"} = 1;
    }

    %dir_content = read_directory(\%parameters);
    mprint("\n");

    @filenames = keys %dir_content;
    $number_of_files = @filenames;
    $total_number_of_files = $number_of_files;
    if ($DEBUG_ON) {
        mprint("RECEIVED LIST OF FILES\n");
        foreach (@filenames) {
            mprint("   $_\n");
        }
    }
}


##########################################################
#                                                        #
#    FILTER ON STRING                                    #
#                                                        #
#    --filter-string                                     #
#                                                        #
##########################################################

if ($provided_string) {
    mprint("FILTER ON STRING\n");
    mprint("Scanning files/directories containing $provided_string\n");
    foreach my $file (@filenames) {
        if (index($file,$provided_string) != -1) {
            if (index($file,".sha256") == -1) {
                dprint("File $file contains $provided_string\n");
                dprint("    DATE and TIME : $file ".$dir_content{$file}{"DATE-TIME"}."\n");
                dprint("    SIZE          : $file ".$dir_content{$file}{"SIZE"}."\n");
                push(@files_to_remove,$file);
                $number_of_valid_files++;
            }
            if (index($file,".sha256") != -1 ) {
                $sha256_count++;
            } else {
                $targz_count++;
            }
        }
    }
    if (($provided_date) || ($keep_latest)){
        @filenames = @files_to_remove;
        if ($DEBUG_ON) {
            foreach(@filenames){
                dprint("$_\n");
            }
        }
    }
}


##########################################################
#                                                        #
#    FILTER ON DATE                                      #
#                                                        #
#    --filter-date                                       #
#                                                        #
##########################################################
if ($provided_date) {
    my $year_only=1;
    my $year_only_end=1;
    @files_to_remove = ();
    %value = parse_initial_data($provided_date);
    mprint("FILTER ON DATE\n");
    mprint("Scanning files/directories for --filter-date $provided_date\n");
    dprint("Start_date: $value{'start_date'}\n");

    if (index($value{'start_date'},"-") != -1) {
        $date_seperator = "-";
        $year_only=0;
    } elsif (index($value{'start_date'},".") != -1) {
        $date_seperator = '.';
        $year_only=0;
    }

    if (!$year_only) {
        dprint("Date containts at least month and year");
        @user_startdate = split("$date_seperator",$value{'start_date'});
        $user_startlength = @user_startdate;
    } else {
        dprint("Date containts only year");
        $user_startlength = 1;
    }

    if (($value{'end_date'}) && (index($value{'end_date'},$date_seperator) != -1)) {
        dprint("Date containts at least month and year");
        @user_enddate = split("$date_seperator",$value{'end_date'});
        $user_endlength = @user_enddate;
    } elsif ($value{'end_date'}) {
        dprint("Date containts only year");
        $user_endlength = 1;
    }
    dprint("start date ($value{'start_date'}  s:$date_seperator) has $user_startlength entries (1 = year only, 2= year AND month, 3= year AND month AND day\n");
    foreach my $file (@filenames) {
        $valid_entry = 0;
        my $temp_var = $dir_content{$file}{"DATE-TIME"};
        $temp_var =~ s/\ .*//;    # Remove everything after a space
        @work_var_file_date = split("-",$temp_var);
        dprint("\n\nFILE $file");
        dprint("split: ".$dir_content{$file}{"DATE-TIME"}."    ");

        #if (length($value{'start_date'}) > 7) {
        if ($user_startlength == 3) {
            # date is complete date dd.mm.yyyy
            $work_var_file_date[0] =~ s/^0+(?=[0-9])//; # remove leading 0's
            $temp_var = $work_var_file_date[0].$date_seperator.$MONTH{$work_var_file_date[1]}.$date_seperator.$work_var_file_date[2];
            dprint("date ($value{'start_date'}) is complete date dd.mm.yyyy  => use $temp_var from file date $temp_var");
        #} elsif (length($value{'start_date'}) > 4) {
        } elsif ($user_startlength == 2) {
            # date is month and year mm.yyyy
            $temp_var = $MONTH{"$work_var_file_date[1]"}.$date_seperator.$work_var_file_date[2];
            dprint("date ($value{'start_date'}) is month and year mm.yyyy  => use $temp_var from file date $temp_var");
        } else {
            # date is a year yyyy
            $temp_var = $work_var_file_date[2];
            dprint("date ($value{'start_date'}) is a year yyyy  => use $temp_var from file date $temp_var");
        }

        if ($user_startlength != 1) {
            @work_var_file_date = split("$date_seperator",$temp_var);
            my $file_date_size = @work_var_file_date;
            dprint("\$file_date_size = $file_date_size");
        }
        if (!$value{'end_date'}) {
            dprint("No End Date provided");
            if (!$value{'start_limit'}) {
                dprint("No Start Limit provided");
                if ($temp_var eq $value{'start_date'}) {
                    dprint("Start Date is equal to file date");
                    $valid_entry = 1;
                } else {
                    dprint("Start Date is NOT equal to file date");
                }
            } else {
                if ((index($value{'start_limit'},"<") != -1) && (index($value{'start_limit'},">") != -1)) {
                    if ($user_startlength == 1) {
                        dprint("file: $temp_var  filter: $value{'start_date'}  &&  <$value{'start_limit'}");
                        if (($temp_var < $value{'start_date'}) && (index($value{'start_limit'},"=") == -1)){
                            dprint("$temp_var < $value{'start_date'}  &&  $value{'start_limit'}");
                            $valid_entry = 1;
                        } elsif (($temp_var <= $value{'start_date'}) && (index($value{'start_limit'},"=") != -1)){
                            dprint("$temp_var <= $value{'start_date'}  &&  $value{'start_limit'}");
                            $valid_entry = 1;
                        }
                    } elsif ($user_startlength == 2) {
                        dprint("YEAR AND MONTH   ");
                        if (index($value{'start_limit'},"=") == -1) {
                            if ($work_var_file_date[1] < $user_startdate[1]) {
                                dprint("$work_var_file_date[1] < $user_startdate[1]   ");
                                $valid_entry = 1;
                            } elsif (($work_var_file_date[1] == $user_startdate[1]) && ($work_var_file_date[0] < $user_startdate[0])) {
                                dprint("($work_var_file_date[1] = $user_startdate[1]) && ($work_var_file_date[0] < $user_startdate[0])   ");
                                $valid_entry = 1;
                            }
                        } else {
                            dprint("LESS THAN EQUAL '<='   ");
                            if ($work_var_file_date[1] < $user_startdate[1]) {
                                $valid_entry = 1;
                            } elsif (($work_var_file_date[1] == $user_startdate[1]) && ($work_var_file_date[0] <= $user_startdate[0])) {
                                $valid_entry = 1;
                            }

                        }
                    } elsif ($user_startlength == 3) {
                        dprint("YEAR MONTH AND DAY   ");
                        if (index($value{'start_limit'},"=") == -1) {
                            dprint("'<'\n");
                            if ($work_var_file_date[2] < $user_startdate[2]) {
                                dprint("   $work_var_file_date[2] < $user_startdate[2]\n");
                                $valid_entry = 1;   # Year less than user input
                            } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                     ($work_var_file_date[1] < $user_startdate[1])) {
                                dprint("   ($work_var_file_date[2] = $user_startdate[2]) && ($work_var_file_date[1] < $user_startdate[1])\n");
                                $valid_entry = 1;   # Year equal and month less than user input
                            } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                     ($work_var_file_date[1] == $user_startdate[1]) &&
                                     ($work_var_file_date[0] < $user_startdate[0])) {
                                dprint("   ($work_var_file_date[2] = $user_startdate[2]) && ($work_var_file_date[1] = $user_startdate[1]) && ($work_var_file_date[0] < $user_startdate[0])\n");
                                $valid_entry = 1;   # Year and month equal and day less than user input
                            } else {
                                dprint("   ($work_var_file_date[2] <=> $user_startdate[2]) && ($work_var_file_date[1] <=> $user_startdate[1]) && ($work_var_file_date[0] <=> $user_startdate[0])\n");
                            }
                        } else {
                            dprint("'<='\n");
                            if ($work_var_file_date[2] < $user_startdate[2]) {
                                $valid_entry = 1;   # Year less than user input
                            } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                     ($work_var_file_date[1] < $user_startdate[1])) {
                                $valid_entry = 1;   # Year equal and month less than user input
                            } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                     ($work_var_file_date[1] == $user_startdate[1]) &&
                                     ($work_var_file_date[0] <= $user_startdate[0])) {
                                $valid_entry = 1;   # Year and month equal and day less than or equal to user input
                            }
                        }
                    }
                } elsif (index($value{'start_limit'},"<") != -1) {
                    # Before Specific day (or month or year)
                    if ($value{'start_limit'} eq "<") {
                        #mprint("Before Specific day (or month or year) (Excluding the input date)\n");
                        if ($user_startlength == 1) {
                            dprint("YEAR ONLY   EXCLUDING");
                            if ($work_var_file_date[0] < $user_startdate[0]) {
                                $valid_entry = 1; # Year is less then user date
                            }
                        } elsif ($user_startlength == 2) {
                            dprint("YEAR AND MONTH   EXCLUDING");
                            if ($work_var_file_date[1] < $user_startdate[1]) {
                                $valid_entry = 1; # Year is less then user date
                            } elsif (($work_var_file_date[1] == $user_startdate[1]) && ($work_var_file_date[0] < $user_startdate[0])) {
                                $valid_entry = 1; # Year and Month are less then user date
                            }
                        } elsif ($user_startlength == 3) {
                            dprint("YEAR MONTH AND DAY   EXCLUDING");
                            if ($work_var_file_date[2] < $user_startdate[2]) {
                                $valid_entry = 1;   # Year less than user input
                            } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                     ($work_var_file_date[1] < $user_startdate[1])) {
                                $valid_entry = 1;   # Year equal and month less than user input
                            } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                     ($work_var_file_date[1] == $user_startdate[1]) &&
                                     ($work_var_file_date[0] < $user_startdate[0])) {
                                $valid_entry = 1;   # Year and month equal and day less than or equal to user input
                            }
                        }
                    } else {
                        #mprint("Before Specific day (or month or year) (Including the input date)\n");
                        if ($user_startlength == 1) {
                            dprint("YEAR ONLY   INCLUDING");
                            if ($work_var_file_date[0] <= $user_startdate[0]) {
                                $valid_entry = 1; # Year is less then user date
                            }
                        } elsif ($user_startlength == 2) {
                            dprint("YEAR AND MONTH   INCLUDING");
                            if ($work_var_file_date[1] <= $user_startdate[1]) {
                                $valid_entry = 1; # Year is less then user date
                            } elsif (($work_var_file_date[1] == $user_startdate[1]) && ($work_var_file_date[0] <= $user_startdate[0])) {
                                $valid_entry = 1; # Year and Month are less then user date
                            }
                        } elsif ($user_startlength == 3) {
                            dprint("YEAR MONTH AND DAY   INCLUDING");
                            if ($work_var_file_date[2] < $user_startdate[2]) {
                                mprint("Year less or equal than user input :\n");
                                mprint("     $work_var_file_date[2] <= $user_startdate[2]\n");
                                $valid_entry = 1;   # Year less than user input
                            } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                     ($work_var_file_date[1] < $user_startdate[1])) {
                                mprint("Year equal and month less or equal than user input :\n");
                                mprint("     $work_var_file_date[2] <= $user_startdate[2]\n");
                                mprint("     $work_var_file_date[1] <= $user_startdate[1]\n");
                                $valid_entry = 1;   # Year equal and month less than user input
                            } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                     ($work_var_file_date[1] == $user_startdate[1]) &&
                                     ($work_var_file_date[0] <= $user_startdate[0])) {
                                mprint("Year and month equal and day less or equal than user input :\n");
                                mprint("     $work_var_file_date[2] <= $user_startdate[2]\n");
                                mprint("     $work_var_file_date[1] <= $user_startdate[1]\n");
                                mprint("     $work_var_file_date[0] <= $user_startdate[0]\n");
                                $valid_entry = 1;   # Year and month equal and day less than or equal to user input
                            }
                        }
                    }
                } elsif (index($value{'start_limit'},">") != -1) {
                    # After Specific day (or month or year)
                    if ($user_startlength == 1) {
                        dprint("YEAR ONLY   EXCLUDING");
                        if ($work_var_file_date[0] > $user_startdate[0]) {
                            $valid_entry = 1; # Year is less then user date
                        }
                    } elsif ($user_startlength == 2) {
                        dprint("YEAR AND MONTH   EXCLUDING");
                        if ($work_var_file_date[1] > $user_startdate[1]) {
                            $valid_entry = 1; # Year is less then user date
                        } elsif (($work_var_file_date[1] == $user_startdate[1]) && ($work_var_file_date[0] > $user_startdate[0])) {
                            $valid_entry = 1; # Year and Month are less then user date
                        }
                    } elsif ($user_startlength == 3) {
                        dprint("YEAR MONTH AND DAY   EXCLUDING");
                        dprint("\$work_var_file_date[2] = $work_var_file_date[2]   \$user_startdate[2] = $user_startdate[2]");
                        dprint("\$work_var_file_date[1] = $work_var_file_date[1]   \$user_startdate[1] = $user_startdate[1]");
                        dprint("\$work_var_file_date[0] = $work_var_file_date[0]   \$user_startdate[0] = $user_startdate[0]");
                        if ($work_var_file_date[2] > $user_startdate[2]) {
                            $valid_entry = 1;   # Year less than user input
                            dprint("Year less than user input");
                        } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                 ($work_var_file_date[1] > $user_startdate[1])) {
                            $valid_entry = 1;   # Year equal and month less than user input
                            dprint("Year equal and month less than user input");
                        } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                                 ($work_var_file_date[1] == $user_startdate[1]) &&
                                 ($work_var_file_date[0] > $user_startdate[0])) {
                            $valid_entry = 1;   # Year and month equal and day less than or equal to user input
                            dprint("Year and month equal and day less than user input");
                        }
                    }
                }
            }
        } else {
            # Date range [dd.mm.yyyy - dd.mm.yyyy] is provided
            dprint("\$valid_entry : $valid_entry");
            if (index($value{'start_limit'},"[") != -1) {
                dprint("RANGE   ");
                if ($user_startlength == 1) {
                    dprint("YEAR ONLY   $work_var_file_date[0] > $user_startdate[0]");
                    if ($work_var_file_date[0] >= $user_startdate[0]) {
                        $valid_entry = 1;
                    }
                } elsif ($user_startlength == 2) {
                    dprint("YEAR AND MONTH   $work_var_file_date[1] <> $user_startdate[1]  $work_var_file_date[0] > $user_startdate[0]");
                    if ($work_var_file_date[1] > $user_startdate[1]) {
                        $valid_entry = 1;
                    } elsif (($work_var_file_date[1] == $user_startdate[1]) && ($work_var_file_date[0] >= $user_startdate[0])) {
                        $valid_entry = 1;
                    }
                } elsif ($user_startlength == 3) {
                    dprint("YEAR MONTH AND DAY   $work_var_file_date[2] <> $user_startdate[2]  $work_var_file_date[1] <> $user_startdate[1]  $work_var_file_date[0] > $user_startdate[0]");
                    if ($work_var_file_date[2] > $user_startdate[2]) {
                        dprint("   $work_var_file_date[2] > $user_startdate[2]\n");
                        $valid_entry = 1;   # Year less than user input
                    } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                             ($work_var_file_date[1] > $user_startdate[1])) {
                        $valid_entry = 1;   # Year equal and month less than user input
                        dprint("   $work_var_file_date[2] = $user_startdate[2]  &&  $work_var_file_date[1] > $user_startdate[1]\n");
                    } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                             ($work_var_file_date[1] == $user_startdate[1]) &&
                             ($work_var_file_date[0] >= $user_startdate[0])) {
                        $valid_entry = 1;   # Year and month equal and day less than user input
                        dprint("   $work_var_file_date[2] = $user_startdate[2]  &&  $work_var_file_date[1] = $user_startdate[1]  &&  $work_var_file_date[0] > $user_startdate[0]\n");
                    }
                }
            }
            dprint("\$valid_entry : $valid_entry");
            if (index($value{'start_limit'},"{") != -1) {
                dprint("RANGE   ");
                if ($user_startlength == 1) {
                    dprint("YEAR ONLY   EXCLUDING");
                    if ($work_var_file_date[0] > $user_startdate[0]) {
                        $valid_entry = 1;
                    }
                } elsif ($user_startlength == 2) {
                    dprint("YEAR AND MONTH   EXCLUDING");
                    if ($work_var_file_date[1] > $user_startdate[1]) {
                        $valid_entry = 1;
                    } elsif (($work_var_file_date[1] == $user_startdate[1]) && ($work_var_file_date[0] > $user_startdate[0])) {
                        $valid_entry = 1;
                    }
                } elsif ($user_startlength == 3) {
                    dprint("YEAR MONTH AND DAY   EXCLUDING");
                    if ($work_var_file_date[2] > $user_startdate[2]) {
                        $valid_entry = 1;   # Year less than user input
                    } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                             ($work_var_file_date[1] > $user_startdate[1])) {
                        $valid_entry = 1;   # Year equal and month less than user input
                    } elsif (($work_var_file_date[2] == $user_startdate[2]) &&
                             ($work_var_file_date[1] == $user_startdate[1]) &&
                             ($work_var_file_date[0] > $user_startdate[0])) {
                        $valid_entry = 1;   # Year and month equal and day less than or equal to user input
                    }
                }
            }
            dprint("\$valid_entry : $valid_entry");
            if (index($value{'end_limit'},"]") != -1) {
                dprint("RANGE   ENDDATE");
                if ($user_endlength == 1) {
                    dprint("YEAR ONLY   $user_enddate[0]");
                    if ($work_var_file_date[0] <= $user_enddate[0]) {
                        $valid_entry++;
                    }
                } elsif ($user_endlength == 2) {
                    dprint("YEAR AND MONTH   $user_enddate[1] $user_enddate[0]");
                    if ($work_var_file_date[1] < $user_enddate[1]) {
                        $valid_entry++;
                    } elsif (($work_var_file_date[1] == $user_enddate[1]) && ($work_var_file_date[0] <= $user_enddate[0])) {
                        $valid_entry++;
                    }
                } elsif ($user_endlength == 3) {
                    dprint("YEAR MONTH AND DAY   $work_var_file_date[2] <> $user_enddate[2]  $work_var_file_date[1] <> $user_enddate[1]  $work_var_file_date[0] > $user_enddate[0]    $user_enddate[2] $user_enddate[1] $user_enddate[0]");
                    if ($work_var_file_date[2] < $user_enddate[2]) {
                        dprint("   $work_var_file_date[2] > $user_enddate[2]\n");
                        $valid_entry++;   # Year less than user input
                    } elsif (($work_var_file_date[2] == $user_enddate[2]) &&
                             ($work_var_file_date[1] < $user_enddate[1])) {
                        $valid_entry++;   # Year equal and month less than user input
                        dprint("   $work_var_file_date[2] = $user_enddate[2]  &&  $work_var_file_date[1] > $user_enddate[1]\n");
                    } elsif (($work_var_file_date[2] == $user_enddate[2]) &&
                             ($work_var_file_date[1] == $user_enddate[1]) &&
                             ($work_var_file_date[0] <= $user_enddate[0])) {
                        $valid_entry++;   # Year and month equal and day less than user input
                        dprint("   $work_var_file_date[2] = $user_enddate[2]  &&  $work_var_file_date[1] = $user_enddate[1]  &&  $work_var_file_date[0] > $user_enddate[0]\n");
                    }
                }
            }
            dprint("\$valid_entry : $valid_entry");
            if (index($value{'end_limit'},"}") != -1) {
                dprint("RANGE   ");
                if ($user_endlength == 1) {
                    dprint("YEAR ONLY   EXCLUDING");
                    if ($work_var_file_date[0] < $user_enddate[0]) {
                        $valid_entry++;
                    }
                } elsif ($user_endlength == 2) {
                    dprint("YEAR AND MONTH   EXCLUDING");
                    if ($work_var_file_date[1] < $user_enddate[1]) {
                        $valid_entry++;
                    } elsif (($work_var_file_date[1] == $user_enddate[1]) && ($work_var_file_date[0] < $user_enddate[0])) {
                        $valid_entry++;
                    }
                } elsif ($user_endlength == 3) {
                    dprint("YEAR MONTH AND DAY   EXCLUDING");
                    if ($work_var_file_date[2] < $user_enddate[2]) {
                        $valid_entry++;   # Year less than user input
                    } elsif (($work_var_file_date[2] == $user_enddate[2]) &&
                             ($work_var_file_date[1] < $user_enddate[1])) {
                        $valid_entry++;   # Year equal and month less than user input
                    } elsif (($work_var_file_date[2] == $user_enddate[2]) &&
                             ($work_var_file_date[1] == $user_enddate[1]) &&
                             ($work_var_file_date[0] < $user_enddate[0])) {
                        $valid_entry++;   # Year and month equal and day less than or equal to user input
                    }
                }
            }
            dprint("\$valid_entry : $valid_entry");

            if ($valid_entry) {
                $valid_entry--;
            }
        }
        if ($valid_entry) {dprint("\$valid_entry : $valid_entry"); }
        if (($valid_entry) && (index($file,".sha256") == -1)){
            dprint("date/time: $file ".$dir_content{$file}{"DATE-TIME"}."  size: ".$dir_content{$file}{"SIZE"}."\n");
            push(@files_to_remove,$file);
            $number_of_valid_files++;
            $valid_entry=0;
        }
        if (index($file,".sha256") != -1 ) {
            $sha256_count++;
        } else {
            $targz_count++;
        }
    }
    if ($keep_latest) {
        @filenames = @files_to_remove;
        if ($DEBUG_ON) {
            foreach(@filenames){
                dprint("$_\n");
            }
        }
    }
}


##########################################################
#                                                        #
#    CHECK IF LATEST FILES SHOULD BE KEPT                #
#                                                        #
#    --keep-latest                                       #
#                                                        #
##########################################################

if ($keep_latest) {
    my $latest_file_date;
    my $this_files_date;
    @files_to_remove = ();
    mprint("CHECK IF LATEST FILES SHOULD BE KEPT\n");
    mprint("Scanning files/directories for --keep-latest $latest_file_date\n");
    foreach my $file (@filenames) {
        $this_files_date = $dir_content{$file}{"DATE-TIME"};
        $this_files_date =~ s/\ .*//;    # Remove everything after a space "17-Feb-2022 13:45" => "17-Feb-2022"
        $this_files_date = convert_date($this_files_date,"DMY","-");
        if (!$latest_file_date) {
            $latest_file_date = $this_files_date;
        } else {
            my @work_var_file_date = split("-",$this_files_date);
            my @work_var_latest    = split("-",$latest_file_date);
            if ($work_var_file_date[2] > $work_var_latest[2]) {
                $latest_file_date = $this_files_date;
            } elsif ($work_var_file_date[2] == $work_var_latest[2]) {
                if ($work_var_file_date[1] > $work_var_latest[1]) {
                    $latest_file_date = $this_files_date;
                } elsif ($work_var_file_date[1] == $work_var_latest[1]) {
                    if ($work_var_file_date[0] >= $work_var_latest[0]) {
                        $latest_file_date = $this_files_date;
                    }
                }
            }
        }
    }

    foreach my $file (@filenames) {
        $this_files_date = $dir_content{$file}{"DATE-TIME"};
        $this_files_date =~ s/\ .*//;
        $this_files_date = convert_date($this_files_date,"DMY","-");
        dprint("Check file date ($this_files_date) against latest ($latest_file_date)\n");
        if ($this_files_date ne $latest_file_date) {
            if (index($file,".sha256") == -1 ) {
                dprint("REMOVE $file due to --keep-latest\n");
                push(@files_to_remove,$file);
            }
        } else {
            my $string_fixlen = sprintf("%-66s",$file);
            mprint("NOT removed $string_fixlen [ due to --keep-latest\n");
        }
    }
}

my $protected_files=0;
my $array_size = @files_to_remove;
my @the_chosen;
my $user_response = 0;

##########################################################
#                                                        #
#    IN CASE THERE ARE NO FILES TO DELETE: EXIT          #
#                                                        #
##########################################################
if ($array_size == 0) {
    mprint("No files meet the criteria, nothing to delete\n");
    push(@log_text, "No files meet the criteria, nothing to delete\n");
    $protected_files = @the_chosen;
    @the_chosen = sort { lc($a) cmp lc($b) } @the_chosen;
    if ($protected_files) {
        mprint("\n$protected_files FILES/DIRECTORIES ARE PROTECTED FROM DELETION :\n");
    }
    foreach my $the_one (@the_chosen) {
        mprint("   $the_one\n");
    }

    exit 0;
}

##########################################################
#                                                        #
#    THERE ARE FILES TO DELETE, CHECK USER RESPONSE      #
#    FOR DISPLAYING A LIST OF FILES TO DELETE            #
#                                                        #
##########################################################
if ((!$dry_run) && ($auto_answer ne "Y")) {
    $user_response = prompt_user("Continue with a list of all files which are considered for removal","ALL");
} else {
    $user_response = "Y";
}


#if (uc($user_response) eq "Y") {
dprint("\nList of files to remove:");
mprint("DISPLAYING A LIST OF PROTECTED FILES AND FILES TO DELETE\n");
my $user_key;
my $max_item_len=0;
my $this_package;
my $prev_package;
my $this_folder;
my @processed_folders;
    @files_to_remove = sort { lc($a) cmp lc($b) } @files_to_remove;
    foreach my $item (@files_to_remove) {
        my $start_point = index($item,"+");
        my $end_point;
        if ($start_point != -1) {
            $end_point    = index($item,"/",$start_point);
            $start_point  = rindex($item,"/",$start_point);
            $this_package = substr($item,$start_point+1,$end_point-$start_point);
        }
        if ($prev_package) {
            if ($prev_package ne $this_package) {
                $prev_package = $this_package;
                $this_folder  = substr($item,0,$end_point);
                push(@log_text, "\nProcessing folder: $this_folder\n");
                push(@processed_folders, $this_folder);
                mprint("\nProcessing folder: $this_folder\n");
            }
        } else {
            $prev_package = $this_package;
            $this_folder  = substr($item,0,$end_point);
            push(@log_text, "Processing folder: $this_folder\n");
            push(@processed_folders, $this_folder);
            mprint("Processing folder: $this_folder\n");
        }
        mprint("Checking: $item\n");

        if (substr($item,-1) eq "/") {
            # Directory
            $tmp_text = "DIR  ";
            mprint("DIR  ");
            #my %this_dir_content = load_dir_content($provided_uri.$item,"CHECKSUM");
            %parameters = clear_hash(\%parameters);

            if ($provided_dir) {
                if (index("/$provided_dir",$provided_uri) != -1) {
                    $parameters{"URI"} = $provided_uri.$provided_dir.$item;
                } else {
                    $parameters{"URI"} = $provided_uri.$item;
                }
                #mprint("URI to use (--directory) : ".$parameters{"URI"}."\n");
                #mprint("    URI        : $provided_uri\n");
                #mprint("    DIRECTORY  : $provided_dir\n");
                #mprint("    FILENAME   : $item\n");
            } else {
                $parameters{"URI"} = $provided_uri.$item;
            }
            $parameters{"CHECKSUM"} = 1;
            $parameters{"ARTIFACTORY-TOKEN"} = $artifact_token;
            my %this_dir_content = load_dir_content(\%parameters);
            my $number_of_files_in_dir = keys %this_dir_content;
            if ((!$remove_directories) || ($number_of_files_in_dir)){
                $temp_str = sprintf("KEEP DIRS :  %-200s",$item);
                if ($number_of_files_in_dir){
                    if ($force_removal) {
                        mprint("$temp_str [ WARNING directory will be removed due to --force-removal ]  <==\n");
                    } else {
                        mprint("$temp_str [ Not removed because directory contains $number_of_files_in_dir files ]\n");
                    }
                } else {
                    mprint("$temp_str [ Not removed because directory parameter --remove-dirs is not provided\n");
                }
                $PROTECTED{$item} = $SYSTEM_PROTECT;
                $temp_str = sprintf("   %-200s",$item);
                $temp_str .= " [ ".$source[$PROTECTED{$item}];
                push(@the_chosen,$temp_str);
                push(@log_text,"$temp_str\n");
                next;
            }
        } else {
            # File
            mprint("FILE ");
            $tmp_text = "FILE ";
            #if (index(lc($item),"/csar/") != -1) {
            #    push(@log_text, "\n");
            #}
        }

        foreach my $protected_item(keys %PROTECTED) {
            if (index($item,$protected_item) != -1) {
                $PROTECTED{$item} = $PROTECTED{$protected_item};
                last;
            }
        }
        my $item_len = length($item);
        if ($item_len > $max_item_len) {
            $max_item_len = $item_len;
        }
        my $item_print = substr($item,length($artifactory_link));
        if ($PROTECTED{$item}) {
            mprint("PROTECTED :  $item_print\n");
            $protected_files++;
            $temp_str = sprintf("   %-200s",$item);
            $temp_str .= " [ ".$source[$PROTECTED{$item}];
            push(@the_chosen,$temp_str);
            if ($dry_run) {
                push(@log_text, "DRY-RUN (PROTECTED) :  $item_print");
                push(@log_text, "                           [REASON: $source[$PROTECTED{$item}]\n");
            } else {
                push(@log_text, "PROTECTED :  $item_print\n");
                push(@log_text, "                 [REASON: $source[$PROTECTED{$item}]\n");
            }
        } else {
            mprint("REMOVE    :  $item_print\n");
            if ($dry_run) {
                push(@log_text, "DRY-RUN (REMOVE)    : $item_print\n");
            } else {
                push(@log_text, "REMOVE    :  $item_print\n");
            }
        }
    }


#}

##########################################################
#                                                        #
#    SHOW FILES THAT ARE PROTECTED FROM DELETING         #
#                                                        #
##########################################################
$protected_files = @the_chosen;
if ($protected_files) {
    mprint("\nNUMBER OF FILES/DIRECTORIES WHICH ARE PROTECTED FROM DELETION : $protected_files\n\n");
}

my $filename_protected_file;
my $path_protected_file;
my $comment_protected_file;
my $last_path;
@the_chosen = sort { lc($a) cmp lc($b) } @the_chosen;
foreach my $the_one (@the_chosen) {
    ($filename_protected_file, $path_protected_file, $comment_protected_file) = retrieve_fileinfo($the_one);
    if ($last_path ne $path_protected_file) {
        if ($last_path) {
            mprint("\n");
        }
        mprint("PATH: $path_protected_file\n");
        $last_path = $path_protected_file;
    }
    #if ($provided_uri) {
    #    $filename_protected_file = substr($the_one,length($provided_uri));
    #}
    #if ($provided_dir) {
        #$filename_protected_file = $provided_dir.$filename_protected_file;
    #}
    mprint("   $filename_protected_file\n");
}
dprint("\n");

##########################################################
#                                                        #
#    SOME SIMPLE STATISTICS                              #
#                                                        #
##########################################################
$number_of_valid_files = @files_to_remove;
my $num_to_delete = $number_of_valid_files - $protected_files;
mprint("\nProvided URI ($provided_uri) stats:\n");
#mprint("   sha256 files: $sha256_count   tar.gz files: $targz_count   Total: $number_of_files\n");
mprint("   Total number of files at location    : $total_number_of_files\n");
mprint("   Files meeting selection criteria     : $number_of_valid_files\n");
mprint("   Files marked for deletion            : $num_to_delete\n");
if (!$remove_directories) {
    mprint("==> Directories will NOT be removed, in case you want to remove directories use --remove-dirs\n");
}
mprint("==> Empty directories will be removed automatically by artifactory\n");

##########################################################
#                                                        #
#    USER RESPONSE TO ACTUALLY DELETING                  #
#                                                        #
##########################################################
$user_response = 0;
if  (!$dry_run) {
    if ($auto_answer) {
        $user_response = $auto_answer;
    } else {
        $user_response = prompt_user("Are you sure to delete these files? (Y/N) : ","YESNO");
    }
    if (uc($user_response) eq "Y") {
        $user_response = 1;
    } else {
        exit 0;
    }
}
if ($dry_run) {
    print_message("Files/Directories not deleted due to parameter --dry-run");
    push(@log_text, "\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
    push(@log_text, "!!! Files/Directories not deleted due to parameter --dry-run !!!\n");
    push(@log_text, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
} else {
    mprint("Deleting Files/Directories\n");
}

##########################################################
#                                                        #
#    ACTUAL REMOVAL OF FILES/DIRECTORIES                 #
#                                                        #
##########################################################
#mprint("ACTUAL REMOVAL OF FILES/DIRECTORIES\n");
my $total_size=0;
my $curl_print;
$protected_files=0;
@the_chosen=();
foreach my $uri_delete (@files_to_remove) {
    if (!$PROTECTED{$uri_delete}) {
        if (!$provided_dir) {
            $curl_del_cmd = $curl_del.$provided_uri.$uri_delete;
            $curl_print = $curl_del_print.$provided_uri.$uri_delete;
        } else {
            #$curl_del_cmd = $curl_del.$provided_uri.$provided_dir.$uri_delete;
            $curl_del_cmd = $curl_del.$uri_delete;
            $curl_print = $curl_del_print.$uri_delete;
        }
        mprint("DEL CMD: $curl_print\n");
    } else {
        $curl_del_cmd = "PROTECTED, NOT DELETED";
    }
    if (substr($uri_delete,-1) eq "/") {
        # Directory
        if ($remove_directories) {
            #  parameter --remove-dirs is given
            if ((!$dry_run) && ($user_response)) {
                mprint("DIR  ");
                if ((index($curl_del_cmd,"PROTECTED") != -1) && (!$force_removal)) {
                    my $temp_str = sprintf("PROTECTED :  %-60s [ $source[$PROTECTED{$uri_delete}]",$uri_delete);
                    #mprint("PROTECTED :  $uri_delete\n");
                    mprint("$temp_str\n");
                    $protected_files++;
                    push(@the_chosen,$uri_delete."  [ ".$source[$PROTECTED{$uri_delete}]);
                    next;
                }
                if ($force_removal) {
                    if ($PROTECTED{$uri_delete} == $SYSTEM_PROTECT) {
                        mprint("FORCED    :  $uri_delete\n");
                        $curl_del_cmd = $curl_del.$provided_uri.$uri_delete;
                    } else {
                        #my $temp_str = sprintf("PROTECTED :  %-60s [ $source[$PROTECTED{$uri_delete}]",$uri_delete);
                        #mprint("PROTECTED :  $uri_delete\n");
                        #mprint("$temp_str\n");
                        #$protected_files++;
                        #push(@the_chosen,$uri_delete."  [ ".$source[$PROTECTED{$uri_delete}]);
                        #next;
                        mprint("FORCED    :  $uri_delete\n");
                    }
                    push(@log_text, "DIR  FORCED    :  $uri_delete\n");
                } else {
                    mprint("REMOVED   :  $uri_delete\n");
                    push(@log_text, "DIR  REMOVED   :  $uri_delete\n");
                }
                `$curl_del_cmd`;
                chop($uri_delete);
                my $divider_pos = rindex($uri_delete,"/");
                if ($divider_pos != -1) {
                    $uri_delete = substr($uri_delete,0,$divider_pos+1);
                }
                my $empty_path = dir_is_empty($uri_delete);
                if ($empty_path) {
                    push(@files_to_remove,$empty_path);
                    mprint("INCLUDED PATH ($empty_path) TO REMOVAL LIST\n");
                    push(@log_text, "INCLUDED PATH ($empty_path) TO REMOVAL LIST\n");
                }
            }
        } else {
            if (!$dry_run) {
                mprint("DIR  ");
                mprint("keep dirs :  $uri_delete\n");
                $protected_files++;
                $temp_str = sprintf("   %-72s",$uri_delete);
                $temp_str .= " [ ".$source[$PROTECTED{$uri_delete}];
                push(@the_chosen,$temp_str);
            }
        }
    } else {
        # File
        if ($dry_run) {
            if (index($curl_del_cmd,"PROTECTED") != -1) {
                mprint("PROTECTED :  $uri_delete\n");
                $protected_files++;
                push(@the_chosen,$uri_delete."  [ ".$source[$PROTECTED{$uri_delete}]);
            } else {
                mprint("REMOVE    :  $uri_delete\n");
                $total_size += $dir_content{$uri_delete}{"SIZE"};
            }
        } else {
            mprint("FILE ");
            if (index($curl_del_cmd,"PROTECTED") != -1) {
                mprint("PROTECTED :  $uri_delete\n");
                $protected_files++;
                $temp_str = sprintf("   %-72s",$uri_delete);
                $temp_str .= " [ ".$source[$PROTECTED{$uri_delete}];
                push(@the_chosen,$temp_str);
            } elsif ($user_response) {
                `$curl_del_cmd`;
                #mprint("$curl_del_cmd\n");
                mprint("REMOVED   :  $uri_delete\n");
                $total_size += $dir_content{$uri_delete}{"SIZE"};
                push(@log_text, "FILE REMOVED   :  $uri_delete\n");
                my $empty_path = dir_is_empty($uri_delete);
                if ($empty_path) {
                    push(@files_to_remove,$empty_path);
                    mprint("INCLUDED PATH ($empty_path) TO REMOVAL LIST\n");
                    push(@log_text, "INCLUDED PATH ($empty_path) TO REMOVAL LIST\n");
                }
            }
        }
    }
}
$protected_files = @the_chosen;
if ($total_size < 1024) {
    $total_size =  "$total_size bytes";
} elsif ($total_size >= 1024 && $total_size < 1024 * 1024) {
    $total_size = $total_size / 1024;
    $total_size = sprintf("%.2f KB",$total_size);
} elsif ($total_size >= 1024 * 1024 && $total_size < 1024 * 1024 * 1024) {
    $total_size = $total_size / (1024 * 1024);
    $total_size = sprintf("%.2f MB",$total_size);
} elsif ($total_size >= 1024 * 1024 * 1024) {
    $total_size = $total_size / (1024 * 1024 * 1024) ;
    $total_size = sprintf("%.2f GB",$total_size);
}


if ($protected_files) {
    mprint("\n$protected_files FILES/DIRECTORIES ARE PROTECTED FROM DELETION :\n");
}


my $prot_filename;
my $prot_path;
my $prot_comment;
@the_chosen = sort { lc($a) cmp lc($b) } @the_chosen;
foreach my $the_one (@the_chosen) {
    mprint("   $the_one\n");
    ($prot_filename,$prot_path,$prot_comment)   = retrieve_fileinfo($the_one);
    if (rindex($prot_filename,"[") != -1) {
        $prot_filename = substr($prot_filename,0,rindex($prot_filename,"["));
        $prot_filename   =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
    }
    push(@log_text, "      $prot_path\n");
    push(@log_text, "      $prot_filename\n");
    push(@log_text, "      $prot_comment\n\n");
}

if (!$remove_directories) {
    mprint("==> Directories containing files will NOT be removed, in case you want to remove none empty directories use --remove-dirs\n");
}
mprint("==> Empty directories will be removed automatically by artifactory\n");
mprint("\nSize of files deleted                : $total_size\n");

push(@log_text, "\nProvided URI ($provided_uri) stats:\n");
push(@log_text, "   Total number of files at location    : $total_number_of_files\n");
push(@log_text, "   Files meeting selection criteria     : $number_of_valid_files\n");
push(@log_text, "   Number of files deleted              : $num_to_delete\n");
push(@log_text, "   Size of files deleted                : $total_size\n");
push(@log_text, "   Number of files PROTECTED            : $protected_files\n");

my $timestamp       = strftime("%Y%m%d-%H%M%S", localtime);
my $this_dir        = getcwd();
my $log_filename    = "CLEANUP_ARTIFACTORY-".$ENV{'USER'}."_$timestamp";
if ($dry_run) {
    $log_filename .= "_DRYRUN";
} else {
    $log_filename .= "_REMOVE";
}
$log_filename .= ".log";

my $log_list        = "$this_dir/$log_filename";
my $log_remote_fn   = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/CleanUp_Logs/$log_filename";
my $log_fh;
open($log_fh, ">", $log_list) or die "File ($log_list) couldn't be opened";
foreach(@log_text) {
    print $log_fh $_;
}
if (@processed_folders) {
    print $log_fh "\nProcessed Folders:\n";
    foreach(@processed_folders) {
        print $log_fh "   $_\n";
    }
}
print $log_fh "\n";
close $log_fh or "couldn't close";
my %arguments;
$arguments{"ARTIFACTORY-TOKEN"} = $artifact_token;
#$arguments{"COMMAND"}         = "-f -k -H \"X-JFrog-Art-Api:$artifact_token\" --upload-file $log_list $log_remote_fn";
$arguments{"COMMAND"}         = "--upload-file $log_list $log_remote_fn";
$arguments{"SILENT"}          = $NO_ARTIFACTORY_PROGRESS;
$arguments{"TYPE"}            = "UPLOAD_FILE";
$arguments{"UPLOAD_RETRIES"}  = 9;
$arguments{"UPLOAD_FILE"}     = "$log_filename";
my $curl_output = send_curl_cmd(\%arguments);

`rm -rf $log_list`;
if ($dry_run) {
    print_message("Files/Directories not deleted due to parameter --dry-run");
}
exit 0;

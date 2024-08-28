#!/usr/bin/perl

################################################################################
# $Revision: 1.0.0 $                                                           #
#   $Author: EEDWIQ $                                                          #
#     $Date: 2023/02/20 12:00:00 $                                             #
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
# v1.2      : Added logic to let the script determine most entries by itself
#             This is achieved by queries towards artifactory and compare
#             the artifactory content against the command line input
#
# v1.3      : Added logic to accept cnf values as string, download the config
#             from artifactory, change the cnf values in the file and then
#             upload it to artifactory
#
# v1.3.1    : Masterversion changed from 1.4.25 to 1.5.25
#
# v1.3.2    : Enhanced locig to recognize master version and change it automatically
#
# v1.4      : New handling for reception/sending of network specific files (.netconf)
#
# v1.4.1    : Bug fixes in handling for reception/sending of network specific files (.netconf)
#
# v1.4.2    : Bug fixes in case cnf's contain all (BUG: cnf specific files are not taken into account)
#
# v1.4.3    : Added printout of script revision at start of script
#
# v1.4.4    : Added parameters --deploy-branch and --upgrade-branch to indicate which artifactory branch under
#             eiffelesc should be used (this can also be accomplished by using --csar-uri and --csar-upgrade-uri
#
# v1.5.0    : Added artifactory token due to removal of user anonymous from server
#
# v1.5.1    : Added line to remove spaces from $send_webhook
#
# v1.5.2    : Fixed problem with CNF values not being changed in file stored on artifactory
#
# v1.5.3    : Added exception for CNF bsfdiameter to not change it
#             Created exception list for this purpose
#
# v1.6.0    : Updated subroutine determine_version to be more readable
#             Included changes from DND-62283 Adaptations for Unsigned CSAR Handling in EVNFM Staging and Solution CI
#
# v1.6.1    : changes for cncs implemented
#
# v1.6.2    : bugfixes
#
# payload structure:
#   {
#   "download_uri_csar" : "<csar_uri>" ,                                fetched automatically depending on csar package
#   "version_csar" : "<csar_version>" ,                                 fetched automatically from <csar_uri>
#   "download_uri_configFile" : "<configFile_uri>" ,                    fetched automatically depending on csar package
#   "version_configFile" : "<configFile_version>" ,                     fetched automatically from <configFile_uri>
#   "download_uri_upgrade_csar" : "<csar_upgrade_uri>" ,                fetched automatically depending on csar upgrade package
#   "version_upgrade_csar" : "<csar_upgrade_version>" ,                 fetched automatically from <csar_upgrade_uri>
#   "download_uri_upgrade_configFile" : "<configFile_upgrade_uri>" ,    fetched automatically depending on csar upgrade package
#   "version_upgrade_configFile" : "<configFile_upgrade_version>" ,     fetched automatically from <configFile_upgrade_uri>
#   "download_uri_daft" : "<daft_uri>" ,                                fetched automatically
#   "version_daft" : "<daft_version>" ,                                 fetched automatically from <daft_uri>
#   "netconf_deploy" : ["netconf_uri" : "<netconf_uri>"]                    fetched automatically depending on cnf's and package
#   "netconf_upgrade" : ["netconf_upgrade_4uri" : "<netconf_upgrade_uri>"]   fetched automatically depending on cnf's and package
#   }


use strict;
use Cwd;
use POSIX qw(strftime);

# ********************************
# *                              *
# * Global Variable declarations *
# *                              *
# ********************************
my $REVISION                =  "v1.6.2";
my $CSAR_URI                = "download_uri_csar";
my $CSAR_VERSION            = "version_csar";
my $CSAR_UPGRADE_URI        = "download_uri_upgrade_csar";          # "download_uri_csarUpgrade";
my $CSAR_UPGRADE_VERSION    = "version_upgrade_csar";               # "version_csarUpgrade";
my $CSAR_VIA_URI            = "download_uri_via_csar";              # "download_uri_csarVia";
my $CSAR_VIA_VERSION        = "version_via_csar";                   # "version_csarVia;
my $CONFIG_URI              = "download_uri_configFile";
my $CONFIG_VERSION          = "version_configFile";
my $CONFIG_VIA_URI          = "download_uri_via_configFile";        # "download_uri_configFileVia";
my $CONFIG_VIA_VERSION      = "version_via_configFile";             # "version_configFileVia";
my $CONFIG_UPGRADE_URI      = "download_uri_upgrade_configFile";    # "download_uri_configFileUpgrade";
my $CONFIG_UPGRADE_VERSION  = "version_upgrade_configFile";         # "version_configFileUpgrade";
my $DAFT_URI                = "download_uri_daft";
my $DAFT_VERSION            = "version_daft";
my $NETCONF_DEPLOY          = "netconf_deploy";
my $NETCONF_UPGRADE         = "netconf_upgrade";
my $NETCONF_URI             = "netconf_uri";
my $NETCONF_UPGRADE_URI     = "netconf_upgrade_uri";
my $IS_FULL_STACK           = "is_full_stack";
my $IS_SMALL_STACK          = "is_small_stack";
my $TARGET                  = "test";
my $MASTER_VERSION                      = "1.15.25";
my $AUTO_UPLOAD_CONFIG_TO_ARTIFACTORY   = 0;                        # 0: Do NOT upload
                                                                    # 1: Upload allowed
my $artifact_token = $ENV{'ARTIFACTORY_TOKEN'};

my @supported_cnfs;
    $supported_cnfs[0] = "scp";
    $supported_cnfs[1] = "spr";
    $supported_cnfs[2] = "slf";
    $supported_cnfs[3] = "bsf";
    $supported_cnfs[4] = "sepp";
    $supported_cnfs[5] = "csa";
    $supported_cnfs[6] = "wcdb";
    $supported_cnfs[7] = "rlf";
    $supported_cnfs[8] = "pvtb";
my %cnf_exemptions;
    $cnf_exemptions{"BSF"}{"MANDATORY"}="wcdb";
    $cnf_exemptions{"SCP"}{"MANDATORY"}="rlf";
    $cnf_exemptions{"SEPP"}{"MANDATORY"}="rlf";
my @cnf_exceptions;
    $cnf_exceptions[0] = "bsfdiameter";

my $SILENT;
my $NO_ARTIFACTORY_PROGRESS;
my $DEBUG_ON                        = 0;
my $DEBUG_INDENT                    = 0;
my @DEBUG_THIS;


my %standard_location;
    $standard_location{"BSF"}           = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/";
    $standard_location{"CNCS"}          = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/cncs/eiffelesc/";
    $standard_location{"SOLUTION-CI"}   = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/Solution_CI_Storage/";

my $bsf_conf        = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/network_config/";
my $bsf_daft        = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/esc-daft/";
my $network_specific_cnf_location           = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/";
my $network_specific_cnf_upgrade_location   = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/";
my $netconf_config_deploy_location   = "sc-config-sample/";
my $netconf_config_upgrade_location  = "sc-config-sample/";

my %package_info;
    $package_info{"BASE"}{"RELEASE"}        = "";   # 9.9.9
    $package_info{"BASE"}{"BUILD"}          = "";   # 9
    $package_info{"BASE"}{"SIGNED"}         = 0;    # 0: unsigned ; 1: signed
    $package_info{"UPGRADE"}{"RELEASE"}     = "";
    $package_info{"UPGRADE"}{"BUILD"}       = "";
    $package_info{"UPGRADE"}{"SIGNED"}      = 0;
my %server;
    $server{"info"}{"DOWNLOAD"}     = "seli";
    $server{"info"}{"UPLOAD"}       = "seli";
    $server{"seli"}{"NAMING"}       = "arm.seli.gic.ericsson.se";
    $server{"seli"}{"TOKEN"}        = $ENV{'ARTIFACTORY_TOKEN'};
    $server{"sero"}{"NAMING"}       = "arm.sero.gic.ericsson.se";
    $server{"sero"}{"TOKEN"}        = $ENV{'ARTIFACTORY_TOKEN'};

my $config_base_url;
my $config_base_version;
my $config_filename;
my $config_deploy_url;
my $config_deploy_version;
my $config_deploy_filename;
my $config_upgrade_url;
my $config_upgrade_version;
my $config_upgrade_filename;

my $uri_specified;                              # Possible values: "DEP" "UPG" "DEPUPG" Defines which kind of uri is used

my %paths_checked;                              # which paths have already been checked, and what was the result
my %directory_content;                          # Contains references to already downloaded directories
my @directory;
my @contents_of_directory;
my %cnf_for_deploy;                             # Future release, store received cnf's in hash
my %cnf_for_upgrade;                            # which is easier to read/maintain

my %global;
    $global{"artifact_token"}           = $ENV{'ARTIFACTORY_TOKEN'};
    $global{"bsf_csar"}                 = $standard_location{"CNCS"};;
    $global{"url-to-use"}               = $standard_location{"CNCS"};;
    $global{"package_base"}             = "";
    $global{"package_upgrade"}          = "";
# ********************************
# *                              *
# * Subroutines                  *
# *                              *
# ********************************
sub mprint {
    my $text    = $_[0];
    print $text if !$SILENT;
}

sub debug_print {
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

sub set_var {
    my ($package, $filename, $line) = caller;
    my $var   = $_[0];
    my $value = $_[1];

    $line++;
    if (exists $global{$var}) {
        debug_print("Changed value for \$global{\"$var\"} at line $line");
        debug_print("    From : ".$global{$var});
        debug_print("    To   : ".$value);
    } else {
        debug_print("Created value for \$global{\"$var\"} = ".$value." at line $line");
    }
    $global{$var} = $value;
}

sub isnum {
    my $var_to_check = $_[0];
    debug_print("isnum($var_to_check)");
    my $return_value;
    if ($var_to_check eq  $var_to_check+0) {
        $return_value = 1;
    }
    if (!$return_value) {
        my $dec_point_at = index($var_to_check,".");
        if ($dec_point_at != -1) {
            my @splitted = split('\.',$var_to_check);
#            my $intpart = substr($var_to_check,0,$dec_point_at);
#            my $floatpart= substr($var_to_check,$dec_point_at+1);
            my $intpart   = $splitted[0];
            my $floatpart = $splitted[1];
            if (($intpart eq $intpart+0) && ($floatpart eq $floatpart+0)) {
                $return_value = 1;
            } elsif (($intpart eq $intpart+0) && ($floatpart+0 == 0)) {
                $return_value = 1;
            }
        }
    }
    debug_print("isnum() : $return_value");
    return $return_value;
}

sub send_curl_cmd {
    my $cmd         = $_[0];
    my $no_output   = $_[1];
    debug_print("send_curl_cmd($cmd , $no_output)");
    #if (!$DEBUG_ON) {
        if (($no_output) || ($NO_ARTIFACTORY_PROGRESS) || ($SILENT)) {
            if (index($cmd,"silent") == -1) {
                $cmd = " --silent $cmd";
            }
        }
    #}
    my $print_token = substr($artifact_token,0,3)."...".substr($artifact_token,-3);
    debug_print("CURL:\n   curl -H \"X-JFrog-Art-Api:$print_token\" $cmd\n");
    my $curl_output = `curl -H "X-JFrog-Art-Api:$artifact_token" $cmd`;
    #debug_print("send_curl_cmd() : $curl_output");
    return $curl_output;
}

sub determine_version {
    my $file_type   = $_[0];
    my $link        = $_[1];
    my $version     = $_[2];
    if (!$version) {
        $version = 0;
    } else {
        $version =~ s/[^0-9]/./g;                                                  # 1.3.0+61 => 1.3.0.61
    }

    debug_print("determine_version($file_type ,","+");
    debug_print("                  $link ,");
    debug_print("                  $version)");
    if (($link) && (!$version)) {
        if ($file_type eq "CSAR") {
            # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar
            # ==> Version = 1.3.0.61
            # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.unsigned.csar
            # ==> Version = 1.3.0.61

            my $csar_filename = substr($link,rindex($link,"/")+1);                          # eric-sc-1.3.0+61.csar or eric-sc-1.3.0+61.unsigned.csar
            my $csar_package  = substr($csar_filename,rindex($csar_filename,"-")+1);        # 1.3.0+61.csar or 1.3.0+61.unsigned.csar
            $csar_package  = substr($csar_package,0,index($csar_package,".csar"));          # 1.3.0+61 or 1.3.0+61.unsigned
            if (index(lc $csar_package,"unsigned") != -1) {
                # remove .unsigned from package
                $csar_package  = substr($csar_package,0,index($csar_package,"unsigned")-1);  # 1.3.0+61
            }
            $csar_package =~ s/[^0-9]/./g;                                                  # 1.3.0+61 => 1.3.0.61
            debug_print("version: ".$csar_package);
            $version = $csar_package;
        }
        if ($file_type eq "CONF") {
            # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/network_config/SC130_daft_config_N103_20210225_163000.xml
            # ==> version = 20210225.163000
            # curl -s -v -X HEAD https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/network_config/EVNFM_n103-ccd-pccpcg-vnflcm.xml
            my @split_result = split( '\.',substr($link,rindex($link,"/")+1));  # [ 0]= SC130_daft_config_N103_20210225_163000     [1]= xml
            @split_result  = split('_',$split_result[0]);                       # [-2]= 20210225     [-1]= 163000
            $version       = $split_result[-2].".".$split_result[-1];           # $version= 20210225.163000
            debug_print("version : $version");
            if (!isnum($version)) {
                debug_print("version not found, try reading date and timestamp");
                my $curl_output =  send_curl_cmd("-sI $link | grep 'Last-Modified' | cut -d' ' -f 2-");
                debug_print("Search for 'Last-Modified' in:\n$curl_output");
                debug_print("Found 'Last-Modified' date");
                debug_print("Last modified on: $curl_output (convert to verion number)");
                my @splitted = split(" ",$curl_output);
                my $timestamp = $splitted[4];
                $timestamp =~ tr/://d;
                if ($splitted[5] eq "GMT") {
                    debug_print("Corrected timestamp due to GMT time");
                    debug_print("   from : $timestamp");
                    $timestamp += 20000;
                    debug_print("   to   : $timestamp");
                }
                debug_print("Converted timestamp: $timestamp");
                my $year  = $splitted[3];
                my $month = $splitted[2];
                my $day   = $splitted[1];
                my %mon2num = qw(jan 01  feb 02  mar 03  apr 04  may 05  jun 06 jul 07  aug 08  sep 09  oct 10 nov 11 dec 12);
                my $datestamp = $year.$mon2num{lc($month)}.$day;  # yyyymmdd
                debug_print("Converted date: $datestamp");
                $version = $datestamp.".".$timestamp;
            }
        }
        if ($file_type eq "DAFT") {
            # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/esc-daft/ESC1.4_DAFT_CXP9036965_99_R1A_20210301_145601.tar.gz
            # ==> version = 1.4.20210301.145601
            my @split_result = split( '_',substr($link,rindex($link,"/")+1));   # [0]=ESC1.4   [1]=DAFT   [2]=CXP9036965   [3]=99   [4]=R1A   [5]=20210301   [6]=145601.tar.gz
            my $pre_fix = substr($split_result[0],3).".".$split_result[-2];     # 1.4.20210301
            $version = $pre_fix.".".substr($split_result[-1],0,index($split_result[-1],"."));
        }
    }
    if ($version == 0) {
        $version = "";
    }
    debug_print("determine_version() => $version","-");
    return $version;
}

sub curl_check_succes {
    my $curl_return     = $_[0];
    my $file_location   = $_[1];
    my $exit_on_error   = $_[2];
    my $silent          = $_[3];
    debug_print("curl_check_succes(curl_return: \$curl_return ,\nfile_location: $file_location ,\nexit_on_error: $exit_on_error ,\nsilent: $silent)","+");
    my $error=0;

    if (index($curl_return,"errors") == -1) {
        debug_print("curl_check_succes() => Succes - $error","-");
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
    debug_print("curl_check_succes() => Failed - $error","-");
    return $error; # Fail
}

sub load_dir_content {
    my ($package, $filename, $line, $subroutine) = caller;
    my $uri             = $_[0];
    my $exit_on_error   = $_[1];
    debug_print("load_dir_content($uri , $exit_on_error)","+");
    debug_print("Line        : $line");
    debug_print("Subroutine  : $subroutine");
    push(@DEBUG_THIS,$uri);
    my $content = send_curl_cmd("$uri",$NO_ARTIFACTORY_PROGRESS);
    my @content;
    my $offset=0;
    my $remainder = substr($content,$offset);
    if (!curl_check_succes($content,$uri,$exit_on_error)) {
        $offset = index($remainder,"<a href=\"");
        while ($offset != -1) {
            my $filename;
            $remainder = substr($remainder,$offset+length("<a href=\""));
            $filename = substr($remainder , index($remainder,"\">")+2);
            $filename = substr($filename, 0 , index($filename,"<"));
            if ($filename ne "../") {
                push(@content, $filename);
                #debug_print("Added $filename");
            }
            $offset = index($remainder,"<a href=\"");
        }
    }
    debug_print("load_dir_content()","-");
    return @content;
}

sub filter_files {
    my $include_files_containing    = $_[0];
    my $exclude_files_containing    = $_[1];
    my @files_to_filter             = @{$_[2]};

    debug_print("filter_files($include_files_containing , $exclude_files_containing)","+");
    my @filtered_files;
    my @incl_filter;
    my @excl_filter;

    @incl_filter = split(" ",$include_files_containing);
    @excl_filter = split(" ",$exclude_files_containing);

    foreach my $file (@files_to_filter) {
        my $include=0;
        my $exclude=0;
        if (@incl_filter) {
            foreach my $incl (@incl_filter) {
                if (index($file,$incl) != -1) {
                    if (@excl_filter) {
                        foreach my $excl (@excl_filter) {
                            if (index($file,$excl) != -1) {
                                $exclude = 1;
                                last;
                            }
                        }
                        if (!$exclude) {
                            $include = 1;
                        }
                    } else {
                        $include = 1;
                    }
                }
            }
        } elsif (@excl_filter) {
            foreach my $excl (@excl_filter) {
                if (index($file,$excl) != -1) {
                    $exclude = 1;
                    last;
                }
            }
            if (!$exclude) {
                $include = 1;
            }
        }
        if ($include) {
            push(@filtered_files,$file);
        }
    }

    if ($DEBUG_ON) {
        debug_print("Filtered files:");
        foreach (@filtered_files) {
            debug_print("   $_");
        }
    }

    debug_print("filter_files()","-");
    return(@filtered_files);
}

sub load_content_config_directory {
    my $uri     = $_[0];            # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SCx.x.x/x.x.x+x/sc-config-xxxx/

    debug_print("load_content_config_directory($uri)","+");
    my @content = load_dir_content($uri , 1);
    #my @filtered = filter_files(".netconf",".sha256", \@content);
    #@content = @filtered;
    @content = filter_files(".netconf",".sha256", \@content);

    if ($DEBUG_ON) {
        debug_print("Config files:");
        foreach (@content) {
            debug_print("   $_");
        }
    }
    debug_print("load_content_config_directory()","-");
    return @content;
}

sub config_file_name {
    my $location = $_[0];
    my $package  = $_[1];

    debug_print("config_file_name($location , $package)","+");
    if (index($package,"https://") != -1) {
        mprint("not allowed to use url as package\n");
        exit 1;
    }

    my @splitted = split('\+',$package);
    my $plus_version = substr($package,index($package,"+"));
    my $release = substr($package,0,index($package,"+"));
    debug_print ("\$splitted[1] = $splitted[1]    \$plus_version = $plus_version");
    debug_print ("\$splitted[0] = $splitted[0]    \$release = $release");
    my %latest_version;
    my $curl_location = $location;
    mprint("loading directory listing: $curl_location\n");
    my @split_output = load_dir_content($curl_location,1);
    my $filename;
    $release =~ tr/.//d;        # Remove dots '.' from release number
    $release = "SC".$release;   # Build according to SC<release> for example: SC130 or SC131
    foreach (@split_output) {
        if (index($_,$release) != -1) {
            if ((index($_,".xml") != -1) && (index($_,".md5") == -1) && (index($_,".sha256") == -1)) {
                my $pos_start   = index($_,$release);
                my $pos_length  = index($_,"\">") - $pos_start;
                $filename = substr($_,$pos_start,$pos_length);
                last;
            }
        }
    }
    if (!$filename) {
        # couldn't find a config file, let's try again this time without last digit of the release
        # => search for SC13 instead of SC131
        my $chopped_digit = chop($release);
        foreach (@split_output) {
            if (index($_,$release) != -1) {
                if ((index($_,".xml") != -1) && (index($_,".md5") == -1) && (index($_,".sha256") == -1)) {
                    my $pos_start   = index($_,$release);
                    my $pos_length  = index($_,"\">") - $pos_start;
                    $filename = substr($_,$pos_start,$pos_length);
                    last;
                }
            }
        }
    }
    debug_print("config_file_name() => $filename","-");
    return $filename;
}

# If package version is +latest then find the latest version of the package
# on artifactory and return the package including latest version to caller
sub check_package_version {
    my $location = $_[0];
    my $package  = $_[1];
    debug_print(" ");
    debug_print( "check_package_version($location , $package)","+");

    my $plus_version = substr($package,index($package,"+")+1);  # ex: 1.5.0+100  plus_version = 100
    my $release = substr($package,0,index($package,"+"));
    if (index($package,"latest") != -1) {
        my $curl_cmd;
        if ($release ne $MASTER_VERSION) {
            debug_print("NOT master version");
            $curl_cmd = $location."SC".$release."/";
        } else {
            debug_print("master version");
            $curl_cmd = $location."master/";
        }
        my @split_output = load_dir_content($curl_cmd);
        my $markup_start = "<a href=\"";
        my $markup_end = "/\">";
        my $version;
        my $version_start;
        my $version_var;
        my $latest_version=-1;
        my $latest_version_var;
        my $latest_found;
        foreach (@split_output) {
            debug_print("Processing: $_");
            if (index($_,"/") != -1) {
                chop($_);
            }
            $version = substr($_,index($_,"+")+1);
            if ($version > $latest_version) {
                debug_print("\$latest_version was : $latest_version");
                $latest_version_var = $_;
                $latest_version     = $version;
                $latest_found=1;
                debug_print("\$latest_version NOW : $latest_version");
            }

        }
        if ($latest_found) {
            $package = $latest_version_var;
        }
    }

    debug_print( "check_package_version() => $package","-");
    return $package;
}

sub package_file_name {
    my $location = $_[0];
    my $package  = $_[1];
    my $task     = $_[2];  # "DEP" : Deploy  "UPG" : Upgrade
    my $curl_output;
    my $curl_cmd;
    my $use_latest;
    my $latest_version=-1;
    my $latest_version_var;
    my $solution_ci = (index(uc $location , "SOLUTION_CI_STORAGE") != -1 ? 1 : 0);
    debug_print("\n");
    debug_print( "package_file_name($location , $package)","+");
    if (index($package,"https://") != -1) {
        mprint("not allowed to use url as package\n");
        exit 1;
    }
    my $plus_version = substr($package,index($package,"+"));
    my $release = substr($package,0,index($package,"+"));

    if (index($package,"latest") != -1) {
        # <a href="1.3.0+1/">1.3.0+1/</a>   09-De
        my $version_var;
        debug_print("Release: '$release'    MASTER: '$MASTER_VERSION'");
        if ($release ne $MASTER_VERSION) {
            debug_print("NOT master version");
            if (($uri_specified) && ($task) && (index($uri_specified,$task) != -1)) {
                debug_print("uri is provided by user");
                $curl_cmd = $location;
            } else {
                $curl_cmd = $location."SC".$release."/";
            }
        } else {
            debug_print("master version");
            $curl_cmd = $location."master/";
        }
        mprint("Find latest package at location: $curl_cmd\n");
        my @split_output = load_dir_content($curl_cmd);
        my $markup_start = "<a href=\"";
        my $markup_end = "/\">";
        my $version;
        my $version_start;
        foreach (@split_output) {
            debug_print("Processing: $_");
            if (index($_,"/") != -1) {
                chop($_);
            }
            $version = substr($_,index($_,"+")+1);
            if ($version > $latest_version) {
                debug_print("\$latest_version was : $latest_version");
                $latest_version_var = $_;
                $latest_version     = $version;
                $use_latest=1;
                debug_print("\$latest_version NOW : $latest_version");
            }

        }
    }
    if ($use_latest) {
        debug_print("Use latest version for SC$release : $latest_version_var");
        $package = $latest_version_var;
        $location = find_package_fast($location, $package);
    }

    debug_print("Release: '$release'    MASTER: '$MASTER_VERSION'");
    debug_print("Check release against master-version");
    if ($release ne $MASTER_VERSION) {
        if (($uri_specified) && ($task) && (index($uri_specified,$task) != -1)) {
            debug_print("uri is provided by user");
            #$curl_cmd = $location.$package."/csar/";
            $curl_cmd = $location."csar/";
        } else {
            #$curl_cmd = $location."SC".$release."/".$package."/csar/";
            $curl_cmd = $location."csar/";
        }
    } else {
        #$curl_cmd = $location."master/".$package."/csar/";
        $curl_cmd = $location."csar/";
    }

    mprint("Get directory of package to use: $curl_cmd\n");
    my @split_output = load_dir_content($curl_cmd);
    @split_output = filter_files(".csar",".sha256 .md5 .sha1", \@split_output);
    my $filename;
    my $signed;
    my $unsigned;
    foreach (@split_output) {
        if (index($_,"unsigned") != -1) {
            $unsigned = $_;
            $filename = $_;
        } else {
            $signed = $_;
        }
    }
    mprint("\n");
    if (($signed) && (!$solution_ci)) {
        # Signed package is preffered for EVNFM staging
        mprint("Signed CSAR package detected on remote site, use this CSAR for EVNFM staging\n");
        $filename = $signed;
    } elsif (($signed) && (!$unsigned)) {
        # Probably an older unsigned package with incorrect naming
        # in this case variable $signed contains the unsigned filename
        mprint("Remote site containd probably an older unsigned package with incorrect naming\n");
        $unsigned = $signed;
        $filename = $signed;  
    }
    if ($solution_ci) {
        mprint("For Solution CI always use unsigned CSAR\n");
    }
    
    if ($filename) {
        $filename = $location."csar/".$filename;
    } else {
        # file does not exist, exit with faultcode and print error message
        mprint("\nError in package $package  Could not find package at:\n$location"."SC".$release."/\n\n");
        exit 1;
    }

    debug_print("package_file_name() => $filename\n","-");
    return $filename;
}

sub daft_versions {
    my $location   = $_[0];
    my $revision   = $_[1];
    my $timestamp  = $_[2];

    debug_print("daft_versions($location , $revision , $timestamp)","+");
    my %latest_version;
    my $specific_version;

    mprint("Loading content daft directory: $location\n");
    my @daft = load_dir_content($location);
    foreach(@daft) {
        if (index($_,".sha256") == -1) {
            if (index($_,".tar") != -1) {                                       # ESC1.5_DAFT_CXP9036965_99_R1A_20211118_102441.tar.gz
                my $filename_start  = index($_,"ESC");                          # => 0
                my $ver_start       = $filename_start + 3;                      # => 3
                my $ver_end         = index($_,"_DAFT");                        # => 6
                my $ver_len   = $ver_end - $ver_start;                          # => 3
                my $version = substr($_,$ver_start,$ver_len);                   # => 1.5
                my $new_filename = $_;
                if (($revision) && ($timestamp)) {
                    if ((index($new_filename,$revision) != -1) && (index($new_filename,$timestamp) != -1)) {
                        $specific_version = $new_filename;
                        last;
                    }
                }

                if (exists $latest_version{$version}) {
                    # already a version stored, check which one is newer by comparing date-time stamp
                    my @breakup_new_name  = split("_",$new_filename);
                    my $date_new = $breakup_new_name[-2];
                    my @time_new = split('\.',$breakup_new_name[-1]);
                    my @breakup_old_name  = split("_",$latest_version{$version});
                    my $date_old = $breakup_old_name[-2];
                    my @time_old = split('\.',$breakup_old_name[-1]);
                    my $date_chng;
                    my $time_chng;
                    if ($date_new > $date_old) {
                        $latest_version{$version} = $new_filename;
                    } elsif (($date_new == $date_old)) {
                        if ($time_new[0] > $time_old[0]) {
                            $latest_version{$version} = $new_filename;
                        }
                    }
                } else {
                    # unkown version, store it as a new 'latest' one
                    $latest_version{$version} = $new_filename;
                }
            }
        }
    }
    if (($revision) && ($timestamp)) {
        debug_print("daft_versions() => $specific_version","-");
        return $specific_version;
    } else {
        my $count = keys %latest_version;
        debug_print("Hash \%latest_version: $count Versions in hash","-");
        foreach (keys %latest_version)
        {
          debug_print("daft_versions() => DAFT $_ LATEST= $latest_version{$_}");
        }
        return %latest_version;
    }
}

sub latest_daft {
    my $location = $_[0];
    debug_print("latest_daft($location)","+");
    my %daft_versions = daft_versions($location);
    my $latest_ver;

    my $daft_rev;
    debug_print("Determine latest from packages on server");
    foreach my $key (keys %daft_versions) {
        if (!$latest_ver) {
            $latest_ver = $key;
        } else {
            my @deconstruct_key = split('\.',$key);
            my @deconstruct_latest = split('\.',$latest_ver);
            if ($deconstruct_key[0] > $deconstruct_latest[0]) {
                $latest_ver = $key;
            } elsif ($deconstruct_key[1] > $deconstruct_latest[1]) {
                    $latest_ver = $key;
            }
        }
    }

    if ($latest_ver) {
        $latest_ver = $daft_versions{$latest_ver};
    }
    debug_print("latest_daft() => $latest_ver","-");

    return $latest_ver;
}


sub make_plus_version {
    my $version  = $_[0];
    debug_print("make_plus_version($version)","+");
    my $reversed_version = scalar reverse($version);
    my $updated_version = substr($reversed_version,0,index($reversed_version,"."))."+".substr($reversed_version,index($reversed_version,".")+1);
    $updated_version = scalar reverse($updated_version);
    debug_print("make_plus_version() => $updated_version","-");
    return $updated_version;
}

sub package_release {
    my $plus_version = $_[0];
    debug_print("package_release($plus_version)","+");
    if (index($plus_version,"+") != -1 ) {
        $plus_version =~ s/\+.*//;    # Remove everything after a space
    }
    debug_print("package_release() => $plus_version","-");
    return $plus_version;
}

sub check_filename {
    my $filename  = $_[0];                                                  # eric-sc-1.3.0+61.csar
    my $location  = $_[1];                                                  # https://arm.seli.gic.ericsson.se/artifactory/ ...
                                                                            #  ... proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/csar/
    my $stop_on_error  = $_[2];
    debug_print("check_filename($filename , $location)","+");

    # check if filename is containing the path.
    # if it is not containing the path then build it in front of the filename
    # eric-sc-1.3.0+61.csar
    my @splitted = split('/',$filename);
    my $num_entries = @splitted;                                            # $num_entries = 1
    if ($num_entries == 1) {
        # no path provided
        my $original_filename = $filename;
        my $curlcmd;
        my $workstr;
        my $workstr2;                                                       # $filename   = eric-sc-1.3.0+61.csar
        my $work_start = index($filename,".csar");                          # $work_start = 16
        if ($work_start != -1) {
            $workstr  = substr($filename,0,$work_start);                    # $workstr    = eric-sc-1.3.0+61
            @splitted = split('-',$workstr);
            $workstr  = $splitted[-1];                                      # $workstr    = 1.3.0+61
            if (index($location,$workstr) == -1) {
                $workstr2 = substr($workstr,0,index($workstr,"+"));         # $workstr2   = 1.3.0
                $filename = "SC".$workstr2."/".$workstr."/csar/".$filename; # $workstr    = SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar
                # check if file exists at this location
                $curlcmd = "$location/SC".$workstr2."/".$workstr."/csar/";
            } else {
                $curlcmd = "$location";
            }
            mprint("Checking if $filename exists at $location\n");
            my $curloutput = send_curl_cmd("$curlcmd",$NO_ARTIFACTORY_PROGRESS);
            debug_print("curl command:");
            debug_print("     curl $curlcmd");
            if (index($curloutput,$original_filename) == -1) {
                # file does not exist, exit with faultcode and print error message
                mprint("Error in filename, file $original_filename\nCould not be found at:\n$location/SC".$workstr2."/".$workstr."/csar/\n\n");
                if ($stop_on_error) {
                    $filename = "";
                } else {
                    exit 1;
                }
            }
        } else {
            # not a package, check if file exists in provided directory
            $curlcmd = "$location";
            if (substr($location,-1) ne "/") {
                $curlcmd .= "/";
            }
            mprint("Checking if $filename exists at $location\n");
            my $curloutput = send_curl_cmd("$curlcmd",$NO_ARTIFACTORY_PROGRESS);
            debug_print("curl command:");
            debug_print("     curl $curlcmd");
            #debug_print("return = $curloutput");
            if (index($curloutput,$original_filename) == -1) {
                # file does not exist, exit with faultcode and print error message
                mprint("Error in filename, file $original_filename\nCould not be found at:\n$location\n\n");
                if ($stop_on_error) {
                    $filename = "";
                } else {
                    exit 1;
                }
            } else {
                $filename = "$location$filename";
            }
        }
    }
    debug_print("check_filename() => $filename","-");
    return $filename;
}

sub uri_exist {
    my $uri             = $_[0];
    my $stop_on_error   = $_[1];
    my $exists=0;
    debug_print("uri_exist($uri)","+");
    if ($uri) {
        my @splitted = split('/',$uri);
        my $filename = $splitted[-1];
        my $path = substr($uri,0,index($uri,$filename));
        if (check_filename($filename,$path,$stop_on_error)) {
            $exists = 1;
        }
    }
    debug_print("uri_exist() => $exists","-");
    if ($uri) {
        return $exists;
    } else {
        return 1;
    }
}

sub prompt_user {
    my $output_prompt = $_[0];

    if ($output_prompt) {
        print($output_prompt);
    }
    my $return_value = <STDIN>;
    chomp $return_value;

    return $return_value;
}

sub download_file {
    my $file_to_download   = $_[0];
    debug_print("download_file($file_to_download)");

    # download file
    mprint("Downloading: $file_to_download\n");
    my $curl_output;
    $curl_output = send_curl_cmd("$file_to_download -O --retry 3",$NO_ARTIFACTORY_PROGRESS);
    if ($curl_output) {
        mprint("UNEXPECTED result = $curl_output");
        exit 1;
    }
    my @splitted = split('/',$file_to_download);
    my $file     = $splitted[-1];

    debug_print("download_file() => $file");
    return $file;
}

sub check_cnf {
    my $cnf = $_[0];
    debug_print("check_cnf($cnf)","+");
mprint("check_cnf($cnf)\n");
    $cnf =~ s/\ .*//;    # Remove everything after a space
    if (lc($cnf) eq "restore") {
        $cnf = "scp-spr-slf-bsf-wcdb-csa";
        mprint("Changed provided cnf from 'restore' to '$cnf'\n");
    }
    if (lc($cnf) eq "all") {
        $cnf="";
        foreach(@supported_cnfs){
            if ($cnf)  {
                $cnf .= "-$_";
            } else {
                $cnf= $_;
            }
        }
        #$cnf = "scp-spr-slf-bsf-wcdb-sepp-csa-rlf";
        mprint("Changed provided cnf from 'all' to '$cnf'\n");
    }
    my @split_cnf = split("-",$cnf);
    foreach my $this_cnf (@split_cnf) {
        mprint("checking exemptions for: $this_cnf\n");
        if (exists $cnf_exemptions{uc($this_cnf)}) {
            mprint("   - exemption exists\n");
            my @exemption_cnfs = split("-",lc($cnf_exemptions{uc($this_cnf)}{"MANDATORY"}));
            foreach my $this_exemption_cnf (@exemption_cnfs) {
                mprint("   - check if ".$this_exemption_cnf." is present\n");
                if (index( lc($cnf) , $this_exemption_cnf) == -1) {
                    mprint("   - adding : ".$this_exemption_cnf."\n");
                    $cnf .= "-".$this_exemption_cnf;
                }
#                mprint("   - check if ".lc($cnf_exemptions{uc($this_cnf)}{"MANDATORY"})." is present\n");
#                if (index( lc($cnf) , lc($cnf_exemptions{uc($this_cnf)}{"MANDATORY"}) ) == -1) {
#                    mprint("   - adding : ".lc($cnf_exemptions{uc($this_cnf)}{"MANDATORY"})."\n");
#                    $cnf .= "-".lc($cnf_exemptions{uc($this_cnf)}{"MANDATORY"});
#                }
            }
            mprint("   - ".lc($cnf_exemptions{uc($this_cnf)}{"MANDATORY"})." is present\n");
        }
    }
#    if ((index(lc($cnf),"bsf") != -1) && (index(lc($cnf),"wcdb") == -1)) {
#        # for bsf it is mandatory to use also wcdb
#        $cnf .= "-wcdb";
#        mprint("Changed provided cnf to include 'wcdb' : $cnf  ['wcdb' is mandatory in case of 'bsf']\n");
#    }
#    if ((index(lc($cnf),"rlf") == -1) && ((index(lc($cnf),"scp") != -1) || (index(lc($cnf),"sepp") != -1)) {
#        # for scp and/or sepp it is mandatory to use also rlf
#        $cnf .= "-rlf";
#        mprint("Changed provided cnf to include 'rlf' : $cnf  ['rls' is mandatory in case of 'scp' or 'sepp']\n");
#    }

    mprint("$cnf = check_cnf()\n");
    debug_print("check_cnf() => $cnf","-");
    return $cnf;
}

sub load_cnf_into_hash {
    my $cnf = $_[0];
    debug_print("load_cnf_into_hash($cnf)");

    my %return_hash;
    my @cnfs = split("-",$cnf);
    foreach(@cnfs){
        $return_hash{$_} = "$_";
    }
    if ($DEBUG_ON) {
        mprint("DEBUG:           load_cnf_into_hash() => [  ");
        foreach (keys %return_hash)
        {
            mprint("$_  ");
        }
        mprint("]\n");
    }
    debug_print(" ");
    return %return_hash;
}

sub adapt_cnf {
    my $enable_cnf          = $_[0];
    my $config_uri_base     = $_[1];
    my $config_uri_upgrade  = $_[2];
    my $config_upgrade_cnf  = $_[3];
    debug_print("\nadapt_cnf($enable_cnf , $config_uri_base , $config_uri_upgrade , $config_upgrade_cnf )","+");
    my $workingDir = getcwd();
    my $file_handle;
    my $upload_changes = $AUTO_UPLOAD_CONFIG_TO_ARTIFACTORY;
    my $artifact_token = $server{"seli"}{"TOKEN"};
    my $base_size;
    my $upgrade_size;

    if (index($artifact_token,"-") != -1) {
        mprint("Environment variable is a credential\n");
        $artifact_token = "";
    }
    if ((!$artifact_token) || ($artifact_token eq "\n")) {
        mprint("BUP Artifactory token used\n");
                        #  AKCp5fU4abs3qKgJt459AF7BpRPSXrScwjnWCkJ9SGpZxmtmyPaenHW6boQAimMtm5cA4Tb6T
        $artifact_token = "AKCp5fU4abs3qKgJt459AF7BpRPSXrScwjnWCkJ9SGpZxmtmyPaenHW6boQAimMtm5cA4Tb6T";
    }

    if ($enable_cnf) {
        #$enable_cnf = check_cnf($enable_cnf);
        #if ($config_upgrade_cnf) {
        #    $config_upgrade_cnf = check_cnf($config_upgrade_cnf);
        #}
        #debug_print("Change cnf's to: $enable_cnf");
        my @files_to_process;
        push(@files_to_process,$config_uri_base);
        if (($config_uri_base ne $config_uri_upgrade) && ($config_upgrade_cnf)){
            push(@files_to_process,$config_uri_upgrade);
        } else {
            mprint("Base and Upgrade config file are the same => only check/change once\n");
        }
        my $cnf_values = $enable_cnf;
        my $file_size;
        foreach my $file_to_process (@files_to_process) {
            if (($config_upgrade_cnf) && ($file_to_process eq $config_uri_upgrade)) {
                debug_print("Processing cnf for $MASTER_VERSION");
                $enable_cnf = $config_upgrade_cnf;
            }
            mprint("Working on: $file_to_process\n");
            if (uri_exist($file_to_process)) {
                # Download the config file
                my $filename = download_file($file_to_process);
                $file_size = -s $filename;

                # Change the cnf settings
                # load config file into array
                my @data;
                my @temp_data;
                open my $file_handle, '<', "./$filename";
                #chomp(@temp_data = <$file_handle>);
                @temp_data = <$file_handle>;
                close $file_handle;
                my $convert="";
                my $last_convert=0;
#                foreach(@temp_data) {
#mprint("\@temp_data : $_\n");
#                    if ((index($_,"<parameter") != -1) && (index($_,"name") != -1)) {
#                        if (index($_,"value") == -1) {
#                            $convert = $_;
#mprint("    CONVERT\n");
#                            $last_convert=0;
#                        }
#                    } elsif ($convert) {
#                        $convert .= $_;
#mprint("    CONVERT\n");
#                        if (index($_,"/>") != -1) {
#                            $last_convert=1;
#                        }
#                    }
#                    if (!$convert) {
#                        push(@data,$_);
#                    } else {
#                        if ($last_convert) {
#                            push(@data,$convert);
#mprint("    CONVERTED INTO: $convert\n");
#                            $convert="";
#                        }
#                    }
#                }
@data=@temp_data;
                my $size = @data;
                debug_print("There are $size of entries in file: $filename\n");

                # search pattern for cnf's that need to be changed
                my $pattern_pre             = "<parameter name=\"eric_sc_values_hash_parameter_global.ericsson.";
                my $pattern_post            = ".enabled\"";
                my $pattern_value_false     = " value=\"false\"";
                my $pattern_value_true      = " value=\"true\"";
                my $pattern_value_changeme  = " value=\"CHANGEME\"";
                my $pattern_value           = " value=";
                my $scan_for_value          = 0;
                my $write_file;                 # indicates if changes are done and file needs to be uploaded
                my %cnf_to_enable;
                foreach my $cnf(split('-',$enable_cnf)) {
                    $cnf_to_enable{$cnf} = 1;
                    debug_print("ENABLE: $cnf");
                }

                my @splitcnf;
                my $flip_to_true;
                my $replace;
                my $data_index=0;
                my @changed_index;
                my $size_change=0;
                # determine all cnf's in the file
                foreach my $line(@data) {
                    if (((index($line,$pattern_pre) != -1) && (index($line,$pattern_post) != -1)) ||
                        ($scan_for_value)) {
                        if (!$scan_for_value) {
                            # count number of spaces at start of line
                            $line =~ /^(\s*)/;
                            my $count = length($1);
                            my $this_cnf = substr($line,length($pattern_pre)+$count,length($line)-(length($pattern_pre)+length($pattern_post)+$count+1));
                            mprint("CNF: $this_cnf\n");
                            my $proceed_with_next;
                            foreach my $except (@cnf_exceptions) {
                                if (lc($this_cnf) eq lc($except)) {
                                    mprint("      Value of this CNF excempted from changing\n");
                                    $proceed_with_next = 1;
                                    last;
                                }
                            }
                            if ($proceed_with_next) {
                                next;
                            }
                            # Entry for a cnf found, check if this cnf needs to be set to TRUE or FALSE
                            debug_print("Entry for a cnf found, check if this cnf needs to be set to TRUE or FALSE");
                        }
                        if (!$scan_for_value) {
                            @splitcnf = split('\.',$line);
                            if ($cnf_to_enable{$splitcnf[2]}) {
                                $flip_to_true = 1;
                            }
                        }
                        if ($flip_to_true) {
                            if (index($line,$pattern_value) == -1) {
                                $scan_for_value=1;
                                next;
                            }
                            $scan_for_value=0;

                            mprint("      Change CNF $splitcnf[2] to TRUE ");
                            if (index($line,$pattern_value_false) != -1) {
                                $replace = $line;
                                $replace =~ s/false/true/ig;
                                mprint("=> changed from FALSE to TRUE <=");
                                $size_change -= 1;
                                debug_print("\n   Org  $line");
                                debug_print("   New  $replace");
                            } elsif (index($line,$pattern_value_changeme) != -1){
                                $replace = $line;
                                $replace =~ s/CHANGEME/true/ig;
                                mprint("=> changed from CHANGEME to TRUE <=");
                                $size_change -= 4;
                                debug_print("\n   Org  $line");
                                debug_print("   New  $replace");
                            } else {
                                debug_print("\n   Org  $line");
                                mprint("=> no change needed <=");
                            }
                            mprint("\n");
                        } else {
                            if (index($line,$pattern_value) == -1) {
                                $scan_for_value=1;
                                next;
                            }
                            $scan_for_value=0;

                            mprint("      Change CNF $splitcnf[2] to FALSE ");
                            if (index($line,$pattern_value_true) != -1) {
                                $replace = $line;
                                $replace =~ s/true/false/ig;
                                mprint("=> changed from TRUE to FALSE <=");
                                $size_change += 1;
                                debug_print("\n   Org  $line");
                                debug_print("   New  $replace");
                            } elsif (index($line,$pattern_value_changeme) != -1){
                                $replace = $line;
                                $replace =~ s/CHANGEME/false/ig;
                                mprint("=> changed from CHANGEME to FALSE <=");
                                $size_change -= 3;
                                debug_print("\n   Org  $line");
                                debug_print("   New  $replace");
                            } else {
                                debug_print("\n   Org  $line");
                                mprint("=> no change needed <=");
                            }
                            mprint("\n");
                        }
                        $flip_to_true=0;
                        if ($replace) {
                            $line = $replace;
                            $replace = "";
                            $write_file = 1;
                        }
                    }
                    $data_index++;
                }
                
                # Upload the config file
                debug_print("\$write_file = $write_file\n\$upload_changes = $upload_changes");
                #if ($write_file) {
                    if ($upload_changes) {
                        my $file_size_before = -s $filename;

                        open my $filehandle, '>', "./$filename" or die "Cannot open output.txt: $!";
                        # Loop over the array
                        foreach (@data)
                        {
                            #print $filehandle "$_\n"; # Print each entry in our array to the file
                            print $filehandle "$_"; # Print each entry in our array to the file
                        }
                        close $filehandle; # Not necessary, but nice to do
                        my $file_size_after = -s $filename;
                        mprint("Upload modified config file: $filename\n");
                        if ($file_size_before != $file_size_after) {
                            mprint("File size before changes: $file_size_before\nFile size after changes : $file_size_after\n");
                        }
                        if ($size_change == ($file_size_after - $file_size_before)) {
                            mprint("Change in filesize has expected value\n");
                        }

                        my $changed_filename;
                        my $user_id = $ENV{'USER'};
                        my $changed_store_file;
                        my @splitfile = split('\.',$file_to_process);
                        my $timestamp = strftime "%Y%m%d-%H%M%S", localtime;

                        if (index($splitfile[0],"http") != -1) {
                            debug_print("Target is on artifactory");
                            my @splitfile2 = split('/',$file_to_process);
                            my @splitfile3 = split('\.',$splitfile2[-1]);
                            $changed_filename = $splitfile3[0]."_$user_id"."_$timestamp.".$splitfile3[1];
                            $changed_store_file = $splitfile[0].".".$splitfile[1].".".$splitfile[2].".".$splitfile[3].".".$splitfile[4]."_$user_id"."_$timestamp.".$splitfile[-1];
                        } else {
                            $changed_filename = $splitfile[0]."_$user_id"."_$timestamp.".$splitfile[1];
                            $changed_store_file = $splitfile[0]."_$user_id"."_$timestamp.".$splitfile[1];
                        }
                        #if ($config_base_url eq $file_to_process) {
                            $file_to_process = $changed_store_file;
                            $config_base_url = $changed_store_file;
                        #}
                        #if ($config_upgrade_url eq $file_to_process) {
                            #$file_to_process = $changed_filename;
                            if ($config_upgrade_url) {
                                $config_upgrade_url = $changed_store_file;
                            }
                        #}
                        #if ($config_deploy_url eq $file_to_process) {
                            #$file_to_process = $changed_filename;
                            $config_deploy_url = $changed_store_file;
                        #}

                        mprint("Uploading to               : $file_to_process\n");
                        #mprint("Saving copy of $filename  =>  $changed_filename\n");
                        #`cp $filename $changed_filename`;

                        my $curl_output = send_curl_cmd("-f -k --upload-file $filename $file_to_process",$NO_ARTIFACTORY_PROGRESS);
                        my $number_of_retries=0;
                        while (index($curl_output, $file_to_process) == -1) {
                            # Something went wrong, retry
                            mprint("\nUpload FAILED, retrying\n");
                            if (!$number_of_retries) {
                                mprint("$curl_output\n");
                            }
                            $number_of_retries++;
                            if ($number_of_retries < 10) {
                                sleep(10);
                                $curl_output = send_curl_cmd("-f -k -H \"X-JFrog-Art-Api:$artifact_token\" --upload-file $filename $file_to_process",$NO_ARTIFACTORY_PROGRESS);
                            } else {
                                mprint("Uploading of $filename failed $number_of_retries times, exiting script\n");
                                exit 1;
                            }
                        }
                        if (index($curl_output, $file_to_process) == -1) {
                            mprint("Upload result: $curl_output\n");
                        } else {
                            mprint("Config file $filename uploaded\n");                     # 0123 45678901234 5678
                            $curl_output = send_curl_cmd("-L -I $file_to_process");         # blo\nbla: 12345\nbli
                            debug_print($curl_output);
                            if (index($curl_output, "Content-Length") != -1) {
                                my $start = index($curl_output, "Content-Length")+16;        # 4+5 = 9
                                my $end   = index($curl_output, "\n", $start);               # 14
                                my $uploaded_size = substr($curl_output,$start,$end-$start); # 9,5 => 12345
                                mprint("File size uploaded file: $uploaded_size\n");
                                if ($file_size_after != $uploaded_size) {
                                    mprint("File size of local file and uploaded file not the same!\n");
                                    mprint("   Local file size   : $file_size_after\n");
                                    mprint("   Uploaded file size: $uploaded_size\n");
                                }
                            } else {
                                mprint("Could not verify file size for uploaded file\n");
                            }
                        }
                    }  else {
                        mprint("Skipping upload of changed network config files, changes are due to parameter --enable-cnf $enable_cnf\n");
                        mprint("Reason for skip: ");
                        mprint("user response\n");
                    }
                #} else {
                #    mprint("File needs no changes, cnf's in file $filename are already set to: $enable_cnf\n");
                #}
            }
        }
    }
    debug_print("adapt_cnf() =>\n","-");
    mprint("\n");
}

sub get_latest_master {
    debug_print("get_latest_master()\n");
    my @master_content = load_dir_content("https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/master/");
    my $latest_master;
    my $master_version;
    foreach(@master_content) {
        my @master_info = split('\+',$_);
        if (substr($master_info[1],-1) eq "/") {
            chop($master_info[1]);
        }
        if ($latest_master) {
            if ($latest_master < $master_info[1]) {
                $latest_master = $master_info[1];
            }
        } else {
            $master_version = $master_info[0];
            $latest_master  = $master_info[1];
        }
    }
    debug_print("get_latest_master() => $master_version+$latest_master\n");
    return "$master_version"."+"."$latest_master";
}

sub find_package_fast {
    my $link            = $_[0];
    my $package_to_find = $_[1];
    debug_print("sub find_package_fast($link , $package_to_find)","+");

    if (!$package_to_find) {
        return "";
    }

    my $repo = substr($link,index($link,"proj-"));
    $repo = substr($repo,0,index($repo,"/"));

    my $find        = "\"path\" : \"";
    my $find_user   = "\"created_by\" : \"";
    my $find_user2  = "\"modified_by\" : \"";
    my $find_date   = "\"created\" : \"";
    my $find_date2  = "\"modified\" : \"";
    my $find_date3  = "\"updated\" : \"";

    my $path;

    my $token       = "-H \"X-JFrog-Art-Api:$artifact_token\"";
    my $search_url  = "https://armdocker.rnd.ericsson.se/artifactory/api/search/aql";
    my $content_type= "-H \"content-type:text/plain\"";
    my $output = `curl -s $token $search_url -d 'items.find({"repo":{"\$eq":"$repo"}},{"type":{"\$eq":"folder"}},{"name":{"\$match":"$package_to_find"}})' $content_type`;
    debug_print("curl return from api search: ".$output."\n");
    if (index($output,$find) != -1) {
        my $try_closest_fit;
        if (index($output,"range") != -1) {
            my $total_entries = substr( $output , index( $output ,"total" ,index($output,"range") ) );
            $total_entries = substr( $total_entries , index($total_entries,":")+2 );
            $total_entries = substr( $total_entries , 0 , index($total_entries,"\n") );
            if ($total_entries > 1) {
                mprint("Multiple occurances of the package found: $total_entries\n");
                my $start_ = index($output,$find);
                while ($start_ != -1) {
                    my $found_path = substr($output,$start_+length($find));
                    my $found_user = substr($output,index($output,$find_user,$start_)+length($find_user));
                    my $found_user2 = substr($output,index($output,$find_user2,$start_)+length($find_user2));
                    my $found_date = substr($output,index($output,$find_date,$start_)+length($find_date));
                    my $found_date2 = substr($output,index($output,$find_date2,$start_)+length($find_date2));
                    my $found_date3 = substr($output,index($output,$find_date3,$start_)+length($find_date3));
                    $found_path = substr($found_path,0,index($found_path,"\","));
                    $found_user = substr($found_user,0,index($found_user,"\","));
                    $found_user2 = substr($found_user2,0,index($found_user2,"\","));
                    $found_date = substr($found_date,0,index($found_date,"\","));
                    $found_date2 = substr($found_date2,0,index($found_date2,"\","));
                    $found_date3 = substr($found_date3,0,index($found_date3,"\""));
                    $found_path = "https://".$server{"seli"}{"NAMING"}."/artifactory/$repo/".$found_path."/$package_to_find/";
                    mprint("   $found_path");
                    mprint(sprintf("      Created by: %-12s   Modified by: %-12s   Created: $found_date   Modified: $found_date2   Updated: $found_date3\n",$found_user,$found_user2));
                    if (index($found_path,$link) != -1) {
                        if (!$try_closest_fit) {
                            $try_closest_fit = $found_path;
                        }
                    } elsif ((!$try_closest_fit) && (index($found_path,"Solution_CI") == -1)) {
                        $try_closest_fit = $found_path;
                        if (index($found_path,"cncs") != -1) {
                            set_var("url-to-use",$standard_location{"CNCS"});
                        }
                    }
                    $start_ = index($output,$find,($start_ + length($find)));
                }
                if (!$try_closest_fit) {
                    mprint("Either adapt argument -l/--link or remove incorrect package from artifactory\n");
                    exit 1;
                } else {
                    mprint("Trying with closest fit: $try_closest_fit\n");
                }
            }
        }
        if (!$try_closest_fit) {
            $path = substr($output,index($output,$find)+length($find));
            $path = substr($path,0,index($path,"\","));
            $path = "https://".$server{"seli"}{"NAMING"}."/artifactory/$repo/".$path."/$package_to_find/";
        } else {
            $path = $try_closest_fit;
            mprint("Package found at: $path\n");
        }
    } else {
        debug_print("PACKAGE NOT FOUND using api search\n");
        debug_print("$output\n");
    }
    debug_print("sub find_package_fast() => $path","-");
    return $path;
}

sub show_help {
    mprint <<EOF;

Description:
============

This script creates the webhooks for deploy or upgrade and prints them out

Syntax:
=======

 $0 [<OPTIONAL>][<MANDATORY>]


    <MANDATORY> are one or more of the following parameters (EXCEPT IF -h / --help IS GIVEN):


    --csar-uri                  uri (or path) towards the csar deployment file
                                This parameter can NOT be used together with --csar-file, only ONE of the parameters is allowed.

    --daft-uri                  uri (or path) towards the daft package to be used, is mandatory in case --daft-latest is NOT used
                                This parameter can NOT be used together with --daft-file, only ONE of the parameters is allowed.

    <OPTIONAL> are one or more of the following parameters:

    -h / --help                 Shows this help

    --csar-file                 filename (including path below eiffel) of the csar deployment file, for example the filename
                                for uri:
                                https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar
                                will be:
                                SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar
                                This parameter can NOT be used together with --csar-uri, only ONE of the parameters is allowed.

    --package-base              Which 'plus' package to use,
                                for example: 1.2.3+50  1.2.3+65  OR 1.2.3+latest  OR master (for latest master)

    --package-upgrade           Which 'plus' package to use for the upgrade (upgrade to),
                                for example: 1.3.2+10  OR 1.3.2+latest  OR master (for latest master)

    --conf-uri                  uri (or path) towards the network configuration file for the deployment (this is the COMPLETE link)
                                This parameter can NOT be used together with --conf-file, only ONE of the parameters is allowed.

    --conf-file                 filename of the network config file for the deployment (ONLY filename is provided)
                                This parameter can NOT be used together with --conf-uri, only ONE of the parameters is allowed.

    --csar-upgrade-uri          uri (or path) towards the csar upgrade file
                                This parameter can NOT be used together with --csar-upgrade-file, only ONE of the parameters is allowed.

    --csar-upgrade-file         filename of the csar upgrade file
                                This parameter can NOT be used together with --csar-upgrade-uri, only ONE of the parameters is allowed.

    --conf-upgrade-uri          uri (or path) towards the network configuration for the upgrade file
                                This parameter can NOT be used together with --confupgrade-file, only ONE of the parameters is allowed.

    --conf-upgrade-file         filename of the network config for the upgrade file
                                This parameter can NOT be used together with --conf-upgrade-uri, only ONE of the parameters is allowed.

    --csar-version              versionnumber of the csar file, if not provided the script tries to generate it from the url

    --csar-upgrade-version      versionnumber of the csar upgrade file, if not provided the script tries to generate it from the url

    --conf-version              versionnumber of the network configuration file, if not provided the script tries to generate it from the url

    --conf-upgrade-version      versionnumber of the network configuration upgrade file, if not provided the script tries to generate it from the url

    --daft-file                 filename of the daft package to be used
                                This parameter can NOT be used together with --daft-uri, only ONE of the parameters is allowed.

    --daft-version              versionnumber of the daft package, if not provided the script tries to generate it from the url

    --daft-latest               Determine (and use) latest version of daft on artifactory, no need to use parameter daft-url

    --daft-specific-revision    Can only be given in combination with --daft-latest, --daft-specific specifies de daft version for which the latest
                                needs to be used.
                                for example: --daft-specific-revision 1.2

    --target                    prepare version of the webhook for use by evfnm or test, if target is not provided the default is test
                                --target evnfm    OR --target test

    --send-webhook              send prepared webhook for deploy, upgrade or both.
                                --send-webhook DEPLOY
                                --send-webhook UPGRADE
                                --send-webhook BOTH

    --silent                    Use this option if no screen output is desired, the script returns either 0 for succes or 1 for failure


    EXAMPLES:

    UPGRADE from 1.3.0+61 to 1.3.1+0
    webhook.pl --csar-uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar --conf-uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/network_config/SC130_daft_config_N103_20210225_163000.xml --csar-upgrade-uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.1/1.3.1+0/csar/eric-sc-1.3.1+0.csar --conf-upgrade-uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/network_config/SC131_daft_config_N103_20210301_163000.xml --daft-uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/esc-daft/ESC1.4_DAFT_CXP9036965_99_R1A_20210301_145601.tar.gz

    DEPLOY 1.3.0+61
    webhook.pl --csar-uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar --conf-uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/network_config/SC130_daft_config_N103_20210225_163000.xml --daft-uri https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/esc-daft/ESC1.4_DAFT_CXP9036965_99_R1A_20210301_145601.tar.gz

    DEPLOY 1.3.0+61 with optional filename as parameter
    webhook.pl --csar-file SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar --conf-file SC130_daft_config_N103_20210225_163000.xml --daft-file ESC1.4_DAFT_CXP9036965_99_R1A_20210301_145601.tar.gz

    DEPLOY 1.3.0+61 with optional parameter --test-version
    webhook.pl --csar-file SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar --conf-file SC130_daft_config_N103_20210225_163000.xml --daft-file ESC1.4_DAFT_CXP9036965_99_R1A_20210301_145601.tar.gz --test-version

    DEPLOY 1.3.0+61 with optional parameter and --test-version --daft-latest
    perl webhook.pl --csar-file SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar --conf-file SC130_daft_config_N103_20210225_163000.xml --daft-latest --use-generic-local

    DEPLOY 1.3.0+61 with optional parameter --use-generic-local and --test-version --daft-latest --daft-specific-revision 1.2
    perl webhook.pl --csar-url SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar --conf-url SC130_daft_config_N103_20210225_163000.xml --daft-latest --daft-specific-revision 1.2 --use-generic-local

    DEPLOY 1.3.0+61 with optional parameter --use-generic-local and --test-version --daft-latest --daft-specific-revision 1.2
    perl webhook.pl --csar-url SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar --daft-latest --daft-specific-revision 1.2 --use-generic-local

    DEPLOY 1.3.0+61 with optional parameter --use-generic-local and --test-version --daft-latest --daft-specific-revision 1.2
    perl webhook.pl --package-base 1.3.0+61 --daft-latest --daft-specific-revision 1.2 --use-generic-local

    UPGRADE from 1.3.0+61 to 1.3.1+2
    perl webhook.pl --package-base 1.3.0+61 --package-upgrade 1.3.1+2 --daft-latest --daft-specific-revision 1.2 --use-generic-local

    UPGRADE from 1.3.0+61 to 1.3.1+latest
    perl webhook.pl --package-base 1.3.0+61 --package-upgrade 1.3.1+latest --daft-latest --daft-specific-revision 1.4 --use-generic-local

    UPGRADE from 1.3.0+61 to 1.3.1+latest --daft-specific-revision 1.4 --daft-specific-timestamp 20210308_135325
    perl webhook.pl --package-base 1.3.0+61 --package-upgrade 1.3.1+latest --daft-latest --daft-specific-revision 1.4 --daft-specific-timestamp 20210308_135325 --use-generic-local

    UPGRADE from 1.3.0+latest to 1.3.1+latest --daft-latest
    perl webhook.pl --package-base 1.3.0+latest --package-upgrade 1.3.1+latest --daft-latest

    UPGRADE from 1.3.0+61 to 1.3.1+223 using latest daft and sending webhooks for deploy and upgrade to evnfm
    perl webhook.pl --package-base 1.3.0+61 --package-upgrade 1.3.1+223 --daft-latest --target evnfm --send-webhook both

EOF
    exit 0;
}

# ********************************
# *                              *
# * MAIN                         *
# *                              *
# ********************************
my $main_start = __LINE__;
my $show_help;
my $csar_base_url;
my $csar_base_version;
my $csar_filename;
my $csar_upgrade_url;
my $csar_upgrade_version;
my $csar_upgrade_filename;
my $csar_via_url;
my $csar_via_version;
my $csar_via_filename;
my $package_via;
my @cnf_config_base;
my $cnf_config_deploy;
my @cnf_config_upgrade;
my $config_via_url;
my $config_via_version;
my $config_via_filename;
my $daft_url;
my $daft_version;
my $daft_filename;
my @netconf_config_deploy;
my @netconf_config_upgrade;

my $use_bsf_main;
my $send_hook = "both";
my %daft_versions;
my $daft_latest;
my $daft_specific_revision;
my $daft_specific_timestamp;
my $config_generic_filename;
my $enable_cnf="";
my $enable_upgrade_cnf="";
my $upload_cnf;
my $netconf_base;
my $netconf_upgrade;
my $upload_daft;
my $netconf_type="";
my $is_full_stack;
my $is_small_stack;
my $is_dry_run;
my $deploy_branch;
my $upgrade_branch;

use Getopt::Long;            # standard Perl module

my $result = GetOptions (
                        "h|help"                    => \$show_help,
                        "csar-uri=s"                => \$csar_base_url,
                        "deploy-branch=s"           => \$deploy_branch,
                        "csar-file=s"               => \$csar_filename,
                        "csar-version=s"            => \$csar_base_version,
                        "conf-uri=s"                => \$config_base_url,
                        "conf-file=s"               => \$config_filename,
                        "conf-version=s"            => \$config_base_version,
                        "csar-upgrade-uri=s"        => \$csar_upgrade_url,
                        "upgrade-branch=s"          => \$upgrade_branch,
                        "csar-upgrade-file=s"       => \$csar_upgrade_filename,
                        "csar-upgrade-version=s"    => \$csar_upgrade_version,
                        "conf-upgrade-uri=s"        => \$config_upgrade_url,
                        "conf-upgrade-file=s"       => \$config_upgrade_filename,
                        "conf-upgrade-version=s"    => \$config_upgrade_version,
                        "conf-via-version=s"        => \$config_via_version,
                        "conf-via-uri=s"            => \$config_via_url,
                        "conf-via-file=s"           => \$config_via_filename,
                        "conf-generic=s"            => \$config_generic_filename,
                        "daft-uri=s"                => \$daft_url,
                        "daft-file=s"               => \$daft_filename,
                        "daft-version=s"            => \$daft_version,
                        "daft-latest"               => \$daft_latest,
                        "daft-specific-revision=s"  => \$daft_specific_revision,
                        "daft-specific-timestamp=s" => \$daft_specific_timestamp,
                        "package-base=s"            => \$global{"package_base"},
                        "package-upgrade=s"         => \$global{"package_upgrade"},
                        "cnf-base=s"                => \@cnf_config_base,
                        "cnf-uprade=s"              => \@cnf_config_upgrade,
                        "package-via=s"             => \$package_via,
                        "use-generic-local"         => \$use_bsf_main,
                        "target=s"                  => \$TARGET,
                        "send-webhook=s"            => \$send_hook,
                        "enable-cnf=s"              => \$enable_cnf,
                        "enable-upgrade-cnf=s"      => \$enable_upgrade_cnf,
                        "upload-cnf=s"              => \$upload_cnf,
                        "debug"                     => \$DEBUG_ON,
                        "silent"                    => \$SILENT,
                        "no-progress"               => \$NO_ARTIFACTORY_PROGRESS,
                        "daft-package=s"            => \$upload_daft,
                        "netconf-type=s"            => \$netconf_type,
                        "full-stack=s"              => \$is_full_stack,
                        "small-stack=s"             => \$is_small_stack,
                        "dry-run"                   => \$is_dry_run,
                        );
my @split_scriptname = split("/",$0);
my $scriptname = substr($split_scriptname[-1],0,index($split_scriptname[-1],".pl"));
mprint("\n$scriptname Script version: $REVISION\n\n");

if ((!$global{"package_base"}) && ($global{"package_upgrade"})) {
    mprint("Upgrade package needs also a base package, please fix !!!");
    exit 1;
}

my $package_location_deploy  = find_package_fast($global{"bsf_csar"},$global{"package_base"});
my $package_location_upgrade = find_package_fast($global{"bsf_csar"},$global{"package_upgrade"});

if ($deploy_branch) {
    if (!$csar_base_url) {
        $csar_base_url = $deploy_branch;
    } else {
        mprint("Only provide --deploy-branch  OR  --csar-uri  NOT BOTH\n");
        exit 1;
    }
}
if ($upgrade_branch) {
    if (!$csar_upgrade_url) {
        $csar_upgrade_url = $upgrade_branch;
    } else {
        mprint("Only provide --upgrade-branch  OR  --csar-upgrade-uri  NOT BOTH\n");
        exit 1;
    }
}

if (uc($enable_upgrade_cnf) eq "NONE") {
    $enable_upgrade_cnf = "";
}
if (uc($global{"package_upgrade"}) eq "NONE") {
    $global{"package_upgrade"} = "";
}

if ($enable_cnf) {
        $enable_cnf = check_cnf($enable_cnf);
}
if ($enable_upgrade_cnf) {
        $enable_upgrade_cnf = check_cnf($enable_upgrade_cnf);
}

# Check if daft version needs to be uploaded to artifactory
if ($upload_daft) {
    $daft_filename = $upload_daft;
    if ($daft_latest) {
        $daft_latest = "";
    }
}

if ($package_location_deploy) {
    mprint("Deploy package location (OLD)  : $csar_base_url\n");
    $csar_base_url = $package_location_deploy;
    mprint("Deploy package location (NEW)  : $csar_base_url\n");
}
if ($package_location_upgrade) {
    mprint("Upgrade package location (OLD) : $csar_upgrade_url\n");
    $csar_upgrade_url = $package_location_upgrade;
    mprint("Upgrade package location (NEW) : $csar_upgrade_url\n");
}

#if (($csar_base_url) && (index(uc($csar_base_url),"HTTP") == -1)) {
#    if (substr($csar_base_url,-1) ne "/") {
#        $csar_base_url .= "/";
#    }
#    $uri_specified = "DEP";
#}
if (($csar_upgrade_url) && (index(uc($csar_upgrade_url),"HTTP") == -1)) {
    if (substr($csar_upgrade_url,-1) ne "/") {
        $csar_upgrade_url .= "/";
    }
    $uri_specified .= "UPG";
}

my $netconf_string="      CNFs:\n";
foreach(@netconf_config_deploy) {
    $netconf_string .= "          - cnf-deploy             [string]    => $_\n";
}
foreach(@netconf_config_upgrade) {
    $netconf_string .= "          - cnf-upgrade            [string]    => $_\n";
}

debug_print("Main()\n".
"   Parameters:\n".
"      - h|help                                 => $show_help\n".
"      - csar-url                   [string]    => $csar_base_url\n".
"      - csar-version               [string]    => $csar_base_version\n".
"      - conf-url                   [string]    => $config_base_url\n".
"      - conf-version               [string]    => $config_base_version\n".
"      - conf-generic               [string]    => $config_generic_filename\n".
"      - csar-upgrade-url           [string]    => $csar_upgrade_url\n".
"      - csar-upgrade-version       [string]    => $csar_upgrade_version\n".
"      - conf-upgrade-url           [string]    => $config_upgrade_url\n".
"      - conf-upgrade-version       [string]    => $config_upgrade_version\n".
"      - daft-url                   [string]    => $daft_url\n".
"      - daft-version               [string]    => $daft_version\n".
"      - daft-latest                            => $daft_latest\n".
"      - daft-specific-revision     [string]    => $daft_specific_revision\n".
"      - daft-specific-timestamp    [string]    => $daft_specific_timestamp\n".
"      - package-base               [string]    => ".$global{"package_base"}."\n".
"      - package-upgrade            [string]    => ".$global{"package_upgrade"}."\n".
$netconf_string.
"      - use-generic-local                      => $use_bsf_main\n".
"      - target                     [string]    => $TARGET\n".
"      - send-webhook               [string]    => $send_hook\n".
"      - enable-cnf                 [string]    => $enable_cnf\n".
"      - enable-upgrade-cnf         [string]    => $enable_upgrade_cnf\n".
"      - upload-cnf                             => $upload_cnf\n".
"      - small-stack                            => $is_small_stack\n".
"      - full-stack                             => $is_full_stack\n".
"      - silent                                 => $SILENT\n".
"      - no-progress (\$no-artifactory-progress) => $NO_ARTIFACTORY_PROGRESS\n".
"   Result : $result\n".
"\n","+");

my $mem_small_stack = $is_small_stack;
if ($is_small_stack) {
    if ((uc substr($is_small_stack,0,1) eq "T") || (uc substr($is_small_stack,0,1) eq "Y")) {
        $is_small_stack = "true";
    } else {
        $is_small_stack = 0;
    }
} elsif (!$is_full_stack) {
    $is_small_stack = "true";
}
if ($is_full_stack) {
    if ((uc substr($is_full_stack,0,1) eq "T") || (uc substr($is_full_stack,0,1) eq "Y")) {
        $is_full_stack = "true";
    } else {
        $is_full_stack = 0;
    }
} elsif (!$mem_small_stack) {
    $is_full_stack = "true";
}

if ((!$is_full_stack) && (!$is_small_stack)) {
    # Either small stack and/or full stack has to be provided
    # If no option is provided default to small stack
    $is_small_stack = "true";
    $is_full_stack = "true";
}

if ((!$daft_url) && (!$daft_version) && (!$daft_filename) && (!$daft_specific_revision) && (!$daft_specific_timestamp) && (!$daft_latest)) {
    mprint("No parameter for daft, assume latest version is needed\n\n");
    $daft_latest = 1;
}

if (index(uc $global{"package_base"},"MASTER") != -1) {
    # Determine master from artifactory
    #$package_base = get_latest_master();
    set_var("package_base",get_latest_master());
}

if (index(uc $global{"package_upgrade"},"MASTER") != -1) {
    # Determine master from artifactory
    $global{"package_upgrade"} = get_latest_master();
}

if ($enable_cnf) {
    $enable_cnf = check_cnf($enable_cnf);
    %cnf_for_deploy = load_cnf_into_hash($enable_cnf);                         # store received cnf's in hash
}
if ($enable_upgrade_cnf) {
    $enable_upgrade_cnf = check_cnf($enable_upgrade_cnf);
    %cnf_for_upgrade = load_cnf_into_hash($enable_upgrade_cnf);                # store received cnf's in hash
}

my @package_detail;
@package_detail = split('\.',$global{"package_base"}); # 9.9.9+99 => [0]:9 [1]:9 [2]:9+99 [3]unsigned
$package_detail[2] = substr($package_detail[2],0,index($package_detail[2],"+")); # [2]= 9
if ($package_detail[2] eq "25") {
    $MASTER_VERSION = $package_detail[0].".".$package_detail[1].".".$package_detail[2];
    mprint("MASTER_VERSION set to: $MASTER_VERSION\n");
}
@package_detail = split('\.',$global{"package_upgrade"});
$package_detail[2] = substr($package_detail[2],0,index($package_detail[2],"+"));
if ($package_detail[2] eq "25") {
    $MASTER_VERSION = $package_detail[0].".".$package_detail[1].".".$package_detail[2];
    mprint("MASTER_VERSION set to: $MASTER_VERSION\n");
}


if ($upload_cnf) {
    if ((uc($upload_cnf) eq "Y") || (uc($upload_cnf) eq "YES")) {
        $AUTO_UPLOAD_CONFIG_TO_ARTIFACTORY = 1;
    }
}

if ($config_generic_filename) {
    $config_filename            = $config_generic_filename;
    $config_upgrade_filename    = $config_generic_filename;
} else {
    if ($global{"package_base"}) {
        if ((index($global{"package_base"},"1.2.2") != -1) || (index($global{"package_base"},"1.3.0") != -1)) {
            $config_filename = "EVNFM_n103-ccd-pccpcg-vnflcm_SC122-130.xml";
        } else {
            if (index($global{"package_upgrade"},"1.4") != -1) {
                $config_filename        = "EVNFM_n103-ccd-pccpcg-vnflcm_base.xml";
                $config_deploy_filename = "EVNFM_n103-ccd-pccpcg-vnflcm_base.xml";
            } else {
                $config_filename = "EVNFM_n103-ccd-pccpcg-vnflcm.xml";
            }
        }
        if ($global{"package_upgrade"}) {
            if ((index($global{"package_upgrade"},"1.2.2") != -1) || (index($global{"package_upgrade"},"1.3.0") != -1)) {
                $config_upgrade_filename = "EVNFM_n103-ccd-pccpcg-vnflcm_SC122-130.xml";
            }
            else {
                if (index($global{"package_upgrade"},"1.4") != -1) {
                    $config_upgrade_filename  = "EVNFM_n103-ccd-pccpcg-vnflcm_upgrade.xml";
                } else {
                    $config_upgrade_filename  = "EVNFM_n103-ccd-pccpcg-vnflcm.xml";
                }
            }
        }
    }
}

if ($TARGET) {
    if (!( (uc($TARGET) eq "EVNFM") || (uc($TARGET) eq "TEST") )) {
        mprint("--target needs to be either evnfm or test\n--target $TARGET is not a valid value\n\n");
        exit 1;
    }
}
if ($csar_filename)             { $csar_base_url        = check_filename($csar_filename, $global{"bsf_csar"});            $use_bsf_main = 1;}
if ($csar_upgrade_filename)     { $csar_upgrade_url     = check_filename($csar_upgrade_filename, $global{"bsf_csar"});    $use_bsf_main = 1;}
if ($csar_via_filename)         { $csar_via_url         = check_filename($csar_via_filename, $global{"bsf_csar"});        $use_bsf_main = 1;}
if ($config_filename)           { $config_base_url      = check_filename($config_filename, $bsf_conf);          $use_bsf_main = 1;}
if ($config_upgrade_filename)   { $config_upgrade_url   = check_filename($config_upgrade_filename, $bsf_conf);  $use_bsf_main = 1;}
if ($config_deploy_filename)    { $config_deploy_url    = check_filename($config_deploy_filename, $bsf_conf);   $use_bsf_main = 1;}
if ($config_via_filename)       { $config_via_url       = check_filename($config_via_filename, $bsf_conf);      $use_bsf_main = 1;}
if ($daft_filename)             { $daft_url             = check_filename($daft_filename, $bsf_daft);            $use_bsf_main = 1;}

mprint("\nLoading deploy package info\n");
if ($global{"package_base"}) {
    debug_print("csar_base_url (BEFORE Package_file_name) = $csar_base_url");
    if (!$csar_base_url) {
        mprint("Using base url:\n   ".$global{"url-to-use"}."\n");
        $csar_base_url = package_file_name($global{"url-to-use"},$global{"package_base"});
    } elsif (index(uc($csar_base_url),"HTTP") == -1) {
        mprint("Changing base url to:\n   ".$global{"url-to-use"}."$csar_base_url\n");
        $csar_base_url = package_file_name($global{"url-to-use"}.$csar_base_url,$global{"package_base"},"DEP");
    } else {
        mprint("Changing (http based) base url to:\n   "."$csar_base_url\n");
        $csar_base_url = package_file_name($csar_base_url,$global{"package_base"},"DEP");
    }
    debug_print("csar_base_url (AFTER Package_file_name)  = $csar_base_url");
}

mprint("\nLoading upgrade package info\n");
if ($global{"package_upgrade"}) {
    debug_print("csar_upgrade_url (BEFORE Package_file_name) = $csar_upgrade_url");
    if (!$csar_upgrade_url) {
        mprint("Using upgrade url:\n   ".$global{"bsf_csar"}."\n");
        $csar_upgrade_url = package_file_name($global{"bsf_csar"},$global{"package_upgrade"});
    } elsif (index(uc($csar_upgrade_url),"HTTP") == -1) {
        mprint("Changing upgrade url to:\n   ".$global{"bsf_csar"}."$csar_upgrade_url\n");
        $csar_upgrade_url = package_file_name($global{"bsf_csar"}.$csar_upgrade_url,$global{"package_upgrade"},"UPG");
        mprint("Upgrade url:\n   ".$csar_upgrade_url."\n");
    } else {
        mprint("Changing (http based) upgrade url to:\n   "."$csar_upgrade_url\n");
        $csar_upgrade_url = package_file_name($csar_upgrade_url,$global{"package_upgrade"},"UPG");
    }
    debug_print("csar_upgrade_url (AFTER Package_file_name)  = $csar_upgrade_url\n");
}

if (!$config_base_url) {
    # Deploy
    $config_base_url = config_file_name($bsf_conf,$global{"package_base"});
    if ($config_deploy_filename) {
        $config_deploy_url = $config_base_url;
    }

    if ($csar_upgrade_url) {
        # Upgrade
        $config_upgrade_url = config_file_name($bsf_conf,$global{"package_upgrade"});
    }
}

# determine which daft to use and get uri and version
if ($daft_latest) {
    debug_print("Use latest daft available");
    $daft_url = $bsf_daft;
    if ($daft_specific_revision) {
        %daft_versions = daft_versions($bsf_daft);
        $daft_url .= $daft_versions{$daft_specific_revision};
    } else {
        $daft_url .= latest_daft($bsf_daft);
        debug_print("Determined latest daft package:\n   $daft_url");
    }
} elsif ($daft_specific_revision) {
    debug_print("Use specific daft version");
    $daft_url = $bsf_daft;
    if ($daft_specific_timestamp) {
        $daft_url .= daft_versions($bsf_daft,$daft_specific_revision,$daft_specific_timestamp);
    } else {
        %daft_versions = daft_versions($bsf_daft);
        $daft_url .= $daft_versions{$daft_specific_revision};
    }
}

# remove double https references
my $base_config     = $config_base_url;
my $upgrade_config  = $config_upgrade_url;
my $deploy_config   = $config_deploy_url;
if (index($config_base_url,"http") == -1) {
    $base_config = $bsf_conf.$config_base_url;
}
if (index($config_upgrade_url,"http") == -1) {
    $upgrade_config = $bsf_conf.$config_upgrade_url;
}
if (index($config_deploy_url,"http") == -1) {
    $upgrade_config = $bsf_conf.$config_deploy_url;
}


# change cnf's in config files
if (!$config_deploy_filename) {
    adapt_cnf($enable_cnf,$base_config,$upgrade_config);
} else {
    adapt_cnf($enable_cnf,$deploy_config,$upgrade_config,$enable_upgrade_cnf);
}

# CNF Specific Configuration Files
my $package_base_rel;
my $package_upgrade_rel;
my $package_base_ver;
my $package_upgrade_ver;
my @base_cnfs;
my @upgrade_cnfs;
my @base_netconf;
my @upgrade_netconf;
my $netconf_info;


if (($uri_specified) && (index($uri_specified,"DEP") != -1)) {
    debug_print("base uri is provided by user");
    $package_base_rel    = check_package_version($global{"bsf_csar"}.$csar_base_url,$global{"package_base"});
} else {
    $package_base_rel    = check_package_version($package_location_deploy,$global{"package_base"});
}
if (($uri_specified) && (index($uri_specified,"UPG") != -1)) {
    debug_print("upgrade uri is provided by user");
    $package_upgrade_rel    = check_package_version($global{"bsf_csar"}.$csar_upgrade_url,$global{"package_upgrade"});
} else {
    $package_upgrade_rel    = check_package_version($package_location_upgrade,$global{"package_upgrade"});
}

$package_base_ver    = substr($package_base_rel,index($package_base_rel,"+")+1);
$package_upgrade_ver = substr($package_upgrade_rel,index($package_upgrade_rel,"+")+1);
@base_cnfs           = split("-",$enable_cnf);
@upgrade_cnfs        = split("-",$enable_upgrade_cnf);
my @config_files_base;
my @config_files_upgrade;

mprint("\nPreparing network specific config files\n");
my $deploy_pack  = substr($global{"package_base"},0,index($global{"package_base"},"+"));
my $upgrade_pack = substr($global{"package_upgrade"},0,index($global{"package_upgrade"},"+"));

my $cnf_location_extend;
if (($uri_specified) && (index($uri_specified,"DEP") != -1)) {
    debug_print("uri is specified AND it is a deployment uri");
    $cnf_location_extend = "$csar_base_url"."$deploy_pack";
    debug_print("location extended with: ".$cnf_location_extend);
    if (index($csar_base_url,"/csar") != -1) {
        $cnf_location_extend = substr($csar_base_url,0,index($csar_base_url,"/csar"));
        debug_print("change location extended (/csar detected) to: ".$cnf_location_extend);
    }
    if (index($cnf_location_extend,$global{"package_base"}) != -1) {
        $cnf_location_extend = substr($cnf_location_extend,0,index($cnf_location_extend,$global{"package_base"}));
        debug_print("change location extended (package_base not found in location) to: ".$cnf_location_extend);
    }
    if (substr($cnf_location_extend,-1) eq "/") {
        chop($cnf_location_extend);
    }
    $cnf_location_extend = "eiffelesc/".$cnf_location_extend;
    debug_print("change location extended to: ".$cnf_location_extend);
} else {
    debug_print("uri is NOT specified");
    $cnf_location_extend = "eiffelesc/SC$deploy_pack";
    debug_print("location extended with: ".$cnf_location_extend);
}

my $cnf_location_extend_upgrade;
if (($uri_specified) && (index($uri_specified,"UPG") != -1)) {
    if (index($csar_upgrade_url,"/csar") != -1) {
        $cnf_location_extend_upgrade = substr($csar_upgrade_url,0,index($csar_upgrade_url,"/csar"));
    }
    if (index($cnf_location_extend_upgrade,$global{"package_upgrade"}) != -1) {
        $cnf_location_extend_upgrade = substr($cnf_location_extend_upgrade,0,index($cnf_location_extend_upgrade,$global{"package_upgrade"}));
    }
    if (substr($cnf_location_extend_upgrade,-1) eq "/") {
        chop($cnf_location_extend_upgrade);
    }
    $cnf_location_extend_upgrade = "eiffelesc/".$cnf_location_extend_upgrade;
} else {
    $cnf_location_extend_upgrade = "eiffelesc/SC$upgrade_pack";
}

if ($deploy_pack eq $MASTER_VERSION) {
    $cnf_location_extend = "eiffelesc/master";
    debug_print("change location extended (MASTER) to: ".$cnf_location_extend);
}
if ($upgrade_pack eq $MASTER_VERSION) {
    $cnf_location_extend_upgrade = "eiffelesc/master";
}

if (index($global{"url-to-use"},"cncs") != -1) {
    debug_print("CHECK IF CONFIG DIRECTORY IS CORRECT");
    $network_specific_cnf_location .= "cncs/";
}

if (!$netconf_type) {
    my @dir = load_dir_content("$network_specific_cnf_location"."$cnf_location_extend/$package_base_rel/");
    foreach (@dir) {
        if (index($_,"sc-config-sample") != -1) {
            $netconf_type = "sc-config-sample";
        }
        if (index($_,"sc-config-reference") != -1) {
            $netconf_type = "sc-config-reference";
        }
    }
} elsif (index($netconf_type,"sc-config-") == -1) {
    $netconf_type = "sc-config-$netconf_type";
}

if ($netconf_type) {
    #$netconf_config_deploy_location = "$network_specific_cnf_location"."$cnf_location_extend/$package_base_rel/$netconf_type/";
    $netconf_config_deploy_location = "$package_location_deploy"."$netconf_type/";
    debug_print("netconf_config_deploy_location = $package_location_deploy $netconf_type/ => $package_location_deploy"."$netconf_type/");
    if ($global{"package_upgrade"}) {
        #$netconf_config_upgrade_location = "$network_specific_cnf_upgrade_location"."$cnf_location_extend_upgrade/$package_upgrade_rel/$netconf_type/";
        $netconf_config_upgrade_location = "$package_location_upgrade"."$netconf_type/";
    } else {
        $netconf_config_upgrade_location = "";
    }
    mprint("netconf location for DEPLOY:\n   $netconf_config_deploy_location\n");
    mprint("netconf location for UPGRADE:\n   $netconf_config_upgrade_location\n");

    if (scalar @base_cnfs != 0) {
        mprint("there are base cnf's, load config from: $netconf_config_deploy_location\n");
        my @temp_files = load_content_config_directory($netconf_config_deploy_location);
        foreach my $netconf_cnf (@base_cnfs) {
            mprint("\nAdding network specific config for base cnf: $netconf_cnf\n");
            if (scalar @temp_files != 0) {
                foreach my $file (@temp_files) {
                    if ((index($file,$netconf_cnf) != -1) || (uc($netconf_cnf) eq "ALL")) {
                        push(@config_files_base,$netconf_config_deploy_location.$file);
                        debug_print("Added config file: $netconf_config_deploy_location"."$file");
                    }
                }
            } else {
                debug_print("!!! DEPLOY \@temp_files = 0 !!!");
            }
        }
    }
    if (scalar @upgrade_cnfs != 0) {
        mprint("there are upgrade cnf's, load config from: $netconf_config_upgrade_location\n");
        my @temp_files = load_content_config_directory($netconf_config_upgrade_location);
        foreach my $netconf_cnf (@upgrade_cnfs) {
            mprint("\nAdding network specific config for base cnf: $netconf_cnf\n");
            if (scalar @temp_files != 0) {
                foreach my $file (@temp_files) {
                    if ((index($file,$netconf_cnf) != -1) || (uc($netconf_cnf) eq "ALL")) {
                        push(@config_files_upgrade,$netconf_config_upgrade_location.$file);
                        debug_print("Added config file: $netconf_config_upgrade_location"."$file");
                    }
                }
            } else {
                debug_print("!!! UPGRADE \@temp_files = 0 !!!");
            }
        }
    }
    if (scalar @config_files_base != 0) {
        if ((uc($send_hook) eq "DEPLOY") || (uc($send_hook) eq "UPGRADE") || (uc($send_hook) eq "BOTH")) {
            $netconf_base = "\"$NETCONF_DEPLOY\" : [";
            mprint "checking cnf specific config files for deploy...\n";
            my $first = 1;
            foreach (@config_files_base) {
                mprint "    $_\n";
                if ($first) {
                    $first--;
                } else {
                    $netconf_base .= " , ";
                }
                $netconf_base .= "{\"$NETCONF_URI\" : \"";
                $netconf_base .= $_."\"}";
            }
            $netconf_base .= "]";
            debug_print("\nDEPLOY NETCONF INFO: $netconf_base\n");
        }
    } else {
        mprint("\n!!!  NO CNF SPECIFIC CONFIG FILES FOR DEPLOY PRESENT  !!!\n\n");
    }
    my $netconf_upgrade_deploy; # needed in case both deploy and upgrade webhook need to be sent
    if (scalar @config_files_upgrade != 0) {
        if ((uc($send_hook) eq "UPGRADE") || (uc($send_hook) eq "BOTH")) {
            $netconf_upgrade = "\"$NETCONF_UPGRADE\" : [";
            $netconf_upgrade_deploy = "\"$NETCONF_DEPLOY\" : [";
            mprint "checking cnf specific config files for upgrade...\n";
            my $first = 1;
            foreach (@config_files_upgrade) {
                mprint "    $_\n";
                if ($first) {
                    $first--;
                } else {
                    $netconf_upgrade .= " , ";
                    $netconf_upgrade_deploy .= " , ";
                }
                $netconf_upgrade .= "{\"$NETCONF_UPGRADE_URI\" : \"";
                $netconf_upgrade_deploy .= "{\"$NETCONF_URI\" : \"";
                $netconf_upgrade .= $_."\"}";
                $netconf_upgrade_deploy .= $_."\"}";
            }
            $netconf_upgrade .= "]";
            $netconf_upgrade_deploy .= "]";
            debug_print("\nDEPLOY AND UPGRADE NETCONF INFO: $netconf_base\n");
        }
    } else {
        mprint("\n!!!  NO CNF SPECIFIC CONFIG FILES FOR UPGRADE PRESENT  !!!\n\n");
    }
    if ($netconf_base) {
        $netconf_info = $netconf_base;
    }
    if ($netconf_upgrade) {
        if ($netconf_info) {
            $netconf_info .= " , ";
        }
        $netconf_info .= $netconf_upgrade;
    }
    {
        $netconf_base = $netconf_upgrade_deploy;
        mprint "\n\n$netconf_base\n\n";
        mprint "\n\n$netconf_upgrade\n\n";
    }
}
if ($netconf_info) {
    mprint ("\n================================\n");
    mprint ("PAYLOAD FOR CNF SPECIFIC CONFIG:\n");
    mprint "$netconf_info\n\n";
}


if ((!$csar_base_url) && (!$config_base_url) && (!$daft_url) && (!$show_help)) {
    $show_help = 1;
}
if ($show_help) {
    show_help();
}

$send_hook =~ tr/ //d;        # Remove spaces
if (($send_hook) && ((uc($send_hook) ne "DEPLOY") && (uc($send_hook) ne "UPGRADE") && (uc($send_hook) ne "BOTH") && (uc($send_hook) ne "SKIP"))) {
    mprint "--send-webhook   needs either deploy , upgrade or both as parametervalue\n";
    mprint "provided: '$send_hook'\n";
    exit 1;
}

if (!$use_bsf_main) {
    #$csar_base_url    = $global{"bsf_csar"}.$csar_base_url;
    $config_base_url  = $bsf_conf.$config_base_url;
    #if ($csar_upgrade_url)      { $csar_upgrade_url = $global{"bsf_csar"}.$csar_upgrade_url; mprint("CHANGED \$csar_upgrade_url to $csar_upgrade_url\nat 2143\n"); }
    if ($config_upgrade_url)    { $config_upgrade_url = $bsf_conf.$config_upgrade_url; }
    if ($config_deploy_url)     { $config_deploy_url = $bsf_conf.$config_deploy_url; }
    if ($csar_via_url)          { $csar_via_url = $global{"bsf_csar"}.$csar_via_url; }
    if ($config_via_url)        { $config_via_url = $bsf_conf.$config_via_url; }
    if (!$daft_url)             { $daft_url = $bsf_daft.$daft_url; }
} else {
    if (index($csar_base_url,"https") == -1)                                    { $csar_base_url        = $global{"bsf_csar"}.$csar_base_url;       }
    if (index($config_base_url,"https") == -1)                                  { $config_base_url      = $bsf_conf.$config_base_url;     }
    if (($csar_upgrade_url) && (index($csar_upgrade_url,"https") == -1))        { $csar_upgrade_url     = $global{"bsf_csar"}.$csar_upgrade_url; }
    if (($config_upgrade_url) && (index($config_upgrade_url,"https") == -1))    { $config_upgrade_url   = $bsf_conf.$config_upgrade_url; }
    if (($config_deploy_url) && (index($config_deploy_url,"https") == -1))      { $config_deploy_url    = $bsf_conf.$config_deploy_url; }
    if (($csar_via_url) && (index($csar_via_url,"https") == -1))                { $csar_via_url         = $global{"bsf_csar"}.$csar_via_url; }
    if (($config_via_url) && (index($config_via_url,"https") == -1))            { $config_via_url       = $bsf_conf.$config_via_url; }
}

if (($csar_upgrade_url) && (!$config_upgrade_url)) {
    mprint "CSAR Upgrade information is provided, please also include the information for Configuration Upgrade\n";
    exit 1;
}
if ((!$csar_upgrade_url) && ($config_upgrade_url)) {
    mprint "Configuration Upgrade information is provided, please also include the information for CSAR Upgrade\n";
    exit 1;
}
if (($csar_via_url) && (!$config_via_url)) {
    mprint "CSAR Upgrade Via information is provided, please also include the information for Configuration Upgrade Via\n";
    exit 1;
}

if ((!$csar_via_url) && ($config_via_url)) {
    mprint "Configuration Upgrade Via information is provided, please also include the information for CSAR Upgrade Via\n";
    exit 1;
}


$csar_base_version          = determine_version("CSAR",$csar_base_url,$global{"package_base"});
$config_base_version        = determine_version("CONF",$config_base_url,$config_base_version);
$csar_upgrade_version       = determine_version("CSAR",$csar_upgrade_url,$global{"package_upgrade"});
$config_upgrade_version     = determine_version("CONF",$config_upgrade_url,$config_upgrade_version);
$config_deploy_version      = determine_version("CONF",$config_deploy_url,$config_deploy_version);
$csar_via_version           = determine_version("CSAR",$csar_via_url,$csar_via_version);
$config_via_version         = determine_version("CONF",$config_via_url,$config_via_version);
$daft_version               = determine_version("DAFT",$daft_url,$daft_version);

debug_print("\nCheck if uri for base exists");
uri_exist($csar_base_url,1);
debug_print("\nCheck if config for base exists");
uri_exist($config_base_url,1);
debug_print("\nCheck if uri for daft exists");
uri_exist($daft_url,1);
debug_print("\nCheck if uri for upgrade exists");
uri_exist($csar_upgrade_url,1);
debug_print("\nCheck if config for upgrade exists");
uri_exist($config_upgrade_url,1);
debug_print("\nCheck if config for deploy exists");
uri_exist($config_deploy_url,1);
debug_print("\nCheck if uri for intermediate exists");
uri_exist($csar_via_url,1);
debug_print("\nCheck if config for intermediate exists");
uri_exist($config_via_url,1);

my $payload = "{";
if ($csar_upgrade_url) {
    debug_print("preparing payload for upgrade");
    $payload .= "\"$CSAR_URI\" : \"".$csar_upgrade_url."\" , ";
    $payload .= "\"$CSAR_VERSION\" : \"".$csar_upgrade_version."\" , ";
} else {
    debug_print("preparing payload for deploy");
    $payload .= "\"$CSAR_URI\" : \"".$csar_base_url."\" , ";
    $payload .= "\"$CSAR_VERSION\" : \"".$csar_base_version."\" , ";
}
if (!$config_deploy_filename) {
    $payload .= "\"$CONFIG_URI\" : \"".$config_base_url."\" , ";
    $payload .= "\"$CONFIG_VERSION\" : \"".$config_base_version."\" , ";
} else {
    $payload .= "\"$CONFIG_URI\" : \"".$config_deploy_url."\" , ";
    $payload .= "\"$CONFIG_VERSION\" : \"".$config_deploy_version."\" , ";
}
if ($is_small_stack) {
    $payload .= "\"$IS_SMALL_STACK\" : \"true\" , ";
} else {
    $payload .= "\"$IS_SMALL_STACK\" : \"false\" , ";
}
if ($is_full_stack) {
    $payload .= "\"$IS_FULL_STACK\" : \"true\" , ";
} else {
    $payload .= "\"$IS_FULL_STACK\" : \"false\" , ";
}

my $payload_upgrade = $payload;
my $payload_via;
if ($csar_via_url)
{
    debug_print("INTERMEDIATE uri detected");
    $payload_via = $payload;
    $payload_upgrade = "{";
    $payload_upgrade .= "\"$CSAR_URI\" : \"".$csar_via_url."\" , ";
    $payload_upgrade .= "\"$CSAR_VERSION\" : \"".$csar_via_version."\" , ";
    $payload_upgrade .= "\"$CONFIG_URI\" : \"".$config_via_url."\" , ";
    $payload_upgrade .= "\"$CONFIG_VERSION\" : \"".$config_via_version."\" , ";

    $payload_via .= "\"$CSAR_UPGRADE_URI\" : \"".$csar_via_url."\" , ";
    $payload_via .= "\"$CSAR_UPGRADE_VERSION\" : \"".$csar_via_version."\" , ";
    if ($config_via_url) {
        $payload_via .= "\"$CONFIG_UPGRADE_URI\" : \"".$config_via_url."\" , ";
        $payload_via .= "\"$CONFIG_UPGRADE_VERSION\" : \"".$config_via_version."\" , ";
    }
    $payload_via .= "\"$DAFT_URI\" : \"".$daft_url."\" , ";
    $payload_via .= "\"$DAFT_VERSION\" : \"".$daft_version."\" ";
    $payload_via .= "}";
}

if ($csar_upgrade_url)
{
mprint("UPGRADE PAYLOAD URL FOR CSAR:\n   $csar_upgrade_url\n");
    $payload_upgrade = "{";
    $payload_upgrade .= "\"$CSAR_URI\" : \"".$csar_base_url."\" , ";
    $payload_upgrade .= "\"$CSAR_VERSION\" : \"".$csar_base_version."\" , ";
    $payload_upgrade .= "\"$CONFIG_URI\" : \"".$config_upgrade_url."\" , ";
    $payload_upgrade .= "\"$CONFIG_VERSION\" : \"".$config_upgrade_version."\" , ";

    $payload_upgrade .= "\"$CSAR_UPGRADE_URI\" : \"".$csar_upgrade_url."\" , ";
    $payload_upgrade .= "\"$CSAR_UPGRADE_VERSION\" : \"".$csar_upgrade_version."\" , ";
    if ($config_upgrade_url) {
        $payload_upgrade .= "\"$CONFIG_UPGRADE_URI\" : \"".$config_upgrade_url."\" , ";
        $payload_upgrade .= "\"$CONFIG_UPGRADE_VERSION\" : \"".$config_upgrade_version."\" , ";
    }
    if ($is_small_stack) {
        $payload_upgrade .= "\"$IS_SMALL_STACK\" : \"true\" , ";
    } else {
        $payload_upgrade .= "\"$IS_SMALL_STACK\" : \"false\" , ";
    }
    if ($is_full_stack) {
        $payload_upgrade .= "\"$IS_FULL_STACK\" : \"true\" , ";
    } else {
        $payload_upgrade .= "\"$IS_FULL_STACK\" : \"false\" , ";
    }

    $payload_upgrade .= "\"$DAFT_URI\" : \"".$daft_url."\" , ";
    $payload_upgrade .= "\"$DAFT_VERSION\" : \"".$daft_version."\" ";
    if ($netconf_info) {
        $payload_upgrade .= " , ".$netconf_info;
    }
    $payload_upgrade .= "}";
}

$payload .= "\"$DAFT_URI\" : \"".$daft_url."\" , ";
$payload .= "\"$DAFT_VERSION\" : \"".$daft_version."\" ";
if ($netconf_base) {
    $payload .= " , ".$netconf_base;
} elsif ($netconf_info) {
    $payload .= " , ".$netconf_info;
}
$payload .= "}";

my $curl;
my $curl_upgrade;
my $curl_via;
my $trigger = "SC_test_trigger";
if (uc($TARGET) eq "EVNFM") {
    $trigger = "sc_trigger";
}

$curl = "\"https://spinnaker-api.rnd.gic.ericsson.se/webhooks/webhook/".$trigger."\" -X POST -H \"content-type: application/json\" -d ' ".$payload." '";

if ($csar_via_url) {
    $trigger .= "_upgrade";
    $curl_via = "\"https://spinnaker-api.rnd.gic.ericsson.se/webhooks/webhook/".$trigger."\" -X POST -H \"content-type: application/json\" -d ' ". $payload_via." '";
}

if ($csar_upgrade_url) {
    $trigger .= "_upgrade";
    $curl_upgrade = "\"https://spinnaker-api.rnd.gic.ericsson.se/webhooks/webhook/".$trigger."\" -X POST -H \"content-type: application/json\" -d ' ". $payload_upgrade." '";
}

my $csar_base_plus_version = make_plus_version($csar_base_version);
my $csar_plus_version = make_plus_version($csar_base_version);
my $csar_upgrade_plus_version = make_plus_version($csar_upgrade_version);
my $csar_upgrade_via_plus_version = make_plus_version($csar_via_version);

if ($csar_upgrade_url) {
    mprint("Deploy $csar_upgrade_plus_version (Based on upgrade availability)\n");
} else {
    mprint("Deploy $csar_base_plus_version (Based on missing upgrade info)\n");
}
mprint("WEBHOOK: \n");
mprint("curl $curl");
if ($csar_via_url) {
    mprint("\n\n");
    mprint("Upgrade  $csar_plus_version => $csar_upgrade_via_plus_version (via)\n");
    mprint("WEBHOOK: \n");
    mprint("curl $curl_via\n");
}
if ($csar_upgrade_url) {
    mprint "\n\n";
    if ($csar_via_url) {
        mprint "Upgrade (via) $csar_upgrade_via_plus_version => $csar_upgrade_plus_version (Based on intermediate package)\n";
    } else {
        mprint "Upgrade  $csar_plus_version => $csar_upgrade_plus_version (Base on upgrade availability)\n";
    }
    mprint("WEBHOOK: \n");
    mprint("curl $curl_upgrade\n");
}

if ($is_dry_run) {
    $send_hook = "SKIP";
}

if (uc($send_hook) ne "SKIP") {
    if (!$csar_upgrade_url) {
        if ((uc($send_hook) eq "UPGRADE") || (uc($send_hook) eq "BOTH")) {
            # not possible to send upgrade webhook when there is no upgrade path
            # only deploy is possible in this case
            debug_print("UPGRADE WEBHOOK NOT AVAILABLE DUE TO UPGRADE PATH MISSING");
            $send_hook = "DEPLOY";
        }
    } else {
        debug_print("UPGRADE WEBHOOK DUE TO UPGRADE PATH AVAILABILITY");
    }
    if ((uc($send_hook) eq "DEPLOY") || (uc($send_hook) eq "BOTH")) {
        mprint "\n";
        debug_print("DEPLOY NEEDS TO BE SEND DUE TO \$send_hook = $send_hook");
        mprint("Sending DEPLOY webhook\n");
        my $curl_output = send_curl_cmd("$curl");

        mprint $curl_output;
        if (index($curl_output,"\"eventProcessed\":true") == -1) {
            mprint "Problem with sending DEPLOY webhook\n";
            exit 1;
        }
        sleep(5); # Wait 5 seconds before sending another webhook
    }
    if ((uc($send_hook) eq "UPGRADE") || (uc($send_hook) eq "BOTH")) {
        debug_print("UPGRADE NEEDS TO BE SEND DUE TO \$send_hook = $send_hook");
        if ($csar_via_url) {
            mprint "\n";
            mprint("Sending VIA webhook\n");
            my $curl_output = send_curl_cmd("$curl_via");
            mprint $curl_output;
            if (index($curl_output,"\"eventProcessed\":true") == -1) {
                mprint "Problem with sending VIA UPGRADE webhook\n";
                exit 1;
            }
            sleep(5); # Wait 5 seconds before sending another webhook
        }
        if ($csar_upgrade_url) {
            mprint "\n";
            mprint("Sending UPGRADE webhook\n");
            my $curl_output = send_curl_cmd("$curl_upgrade");
            mprint $curl_output;
            if (index($curl_output,"\"eventProcessed\":true") == -1) {
                mprint "Problem with sending UPGRADE webhook\n";
                exit 1;
            }
        }
    }
}
mprint "\n\n";
debug_print("called load_dir_content() with:");
foreach (@DEBUG_THIS) {
    debug_print("   $_");
}
exit 0;


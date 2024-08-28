#!/usr/bin/perl

################################################################################
# $Revision: 1.95 $                                                            #
#   $Author: EEDWIQ $                                                          #
#     $Date: 2024-05-23 11:34:00 $                                             #
################################################################################
#                                                                              #
# (C) COPYRIGHT ERICSSON GMBH 2020-2024                                        #
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
# CHANGE NOTES
#
# v1.39     : Added more output to all_files_present
#
# v1.40     : Added logging, logs will be deleted at successful exit
#
# v1.41     : Changed checking file size to include check on size=0
#             Check if more files present than expected
#
# v1.42     : Changed sizelimits for tools from 70M-100MB to 2KB-100MB
#           : Changed sizelimits for yaml files from 10KB-30KB to 10KB-60KB
#
# v1.43     : Changes to output in case files are missing from an already
#             downloaded package to make clearer printouts
#             Changed behaviour for checking package location in case option
#             --link is used pointing towards the location with the full path
#
# v1.50     : Fixed problem with --target containing ../../dirname/subdir and ./dirname/subdir
#
# v1.51     : Changed expected maximum file size for .yaml files from 60KB to 160KB
#             For newer yaml files (1.6.0+xx) the filesize was between 63KB and 64KB
#             Added new parameters:
#               --max-csar  changes defined excepted filesize range to provided one
#               --max-yaml      Units : 10 10KB 10MB 10GB
#               --max-tgz       Syntax: =10MB       (Exact 10MB is expected)
#                                       1KB-10MB    (filesize between 1KB and 10MB)
#                                       64KB        (filesize between 1Byte and 64KB)
#
# v1.52     : Removed netconf files from mandatory list
#             Printout of missing files (in case of missing files)
#             Print script revision number
#
# v1.53     : Moved printing of script revision number to be printed as first
#
#
# v1.54     : Increased size of package
#
# v1.60     : Include https://arm.seli.gic.ericsson.se/artifactory/proj-CNDSC-generic-local/eiffelesc/CNDSC/
#             in download options and do not check files downloaded from this link (using option --no-CNDSC-check
#
# v1.61     : Reduced minimum size for sc_vnf_descriptor from 600K to 400K
#
# v1.62     : In case csar file is corrupted (when checking already present version) try downloading it again
#
# v1.63     : Skip vnfd check for CNDSC packages
#
# v1.64     : More info printed out during vnfd check
#
# v1.65     : Updates for uploading towards secure artifactory
#                to secure package 1.10.0+88 call download_csar.pl with following parameters
#                   -l https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.10.0/
#                   -p 1.10.0+88
#                   --download-all
#                   -u https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/GA-Versions/SC1.10.0/1.10.0+88
#                   --dry-run
#                If everything looks good, then proceed with Y to do actual copying of package
#
# v1.66     : removed vnfd checks, introduced new global var $skip_vnfd and set it to 1, to invoke checks again set it to 0.
#
# v1.67     : reactivated vnfd checks, global var $skip_vnfd set it to 0
#
# v1.68     : Added handling for: traffic (tools and config)
#
# v1.69     : Bug Fixes
#
# v1.70     : Made script more robust, added check on package version versus mandatory directories
#
# v1.71     : Added handling for: base_stability_traffic directory
#
# v1.72     : Fixed return code 8 error even though base_stability_traffic/traffic_config/ and base_stability_traffic/traffic_scripts/k6/ directories were found and downloaded.
#
# v1.73     : Allow download of base_stability_traffic files starting from SC 1.8.1.
#             Fix issue with package compare logic when valid release is 1.8.1+ and actual package is 1.11.0+18 it did not download certain files.
#
# v1.74     : Reduced minimum size for sc_vnf_descriptor from 400K to 20K
#
# v1.75     : Fixed issue with master package if not under eiffelesc/master
#
# v1.76     : Faster find implemented
#
# v1.77     : Removed unused/out-commented code,
#             updated - $package_info{"base_stability_traffic"}{"MANDATORY"}                        from 1 to 0
#                     - $package_info{"base_stability_traffic/traffic_config"}{"MANDATORY"}         from 1 to 0
#                     - $package_info{"base_stability_traffic/traffic_scripts/k6"}{"MANDATORY"}     from 1 to 0
#
# v1.78     : Fixed problem with showing time for download
#             Added -m to curl, -m or --max-time is maximum time allowed in seconds for curl command to complete
#             After maximum time has passed a timeout happens
#
# v1.79     : Bugfix, remove any , from string before checking if it is a number...
#
# v1.80     : Added fetching of base_stability_traffic/traffic_simulators/ files.
#
# v1.81     : Added parameter to bypass fast find for cnDSC stuff
#
# v1.82     : Fetching tools/docker/ files where e.g. supreme image will be stored.
#
# v1.83     : changed global variables to be included in %global_var
#             included --silent and --progress-bar in curl upload
#
# v1.84     : Added base_stability_traffic/traffic_simulators/versions to %package_info
#
# v1.85     : - automatic detection for new record entries, print entries at end of script
#             - check before upload if the file to upload is already present on artifactory, if so skip uploading
#               to prevent unintentional changes on artifactory
#             - inlude new arguments --upload-all --upload-overwrite
#                 upload-all        : upload all source files to artifactoy, instead of only the ones in record %package_info
#                 upload-overwrite  : when uploading also upload files that are already present on artifactory, thus overwriting them
#
# v1.86     : Added parameter "NR-SUBDIR-ENTRIES" to the package record %package_info
#             Function is the same as for "NR-ENTRIES" except that when this parameter is used the script 'knows' that
#             all underlying subdirectories need to be considered, thus shortening the record length.
#
# v1.87     : Made the certificates directory valid from 1.8.1.
#
# v1.88     : Fixed probelms with --sync, several tests will now be done with local package against package stored on artifactory
#             Changes to --debug to have standard and detailed information
#             Added logic to determine which .csar to download (signed or unsigned)
#
# v1.89     : - Bugfixes
#             - Added support of checking downloaded package for presence of Files/certificate.cert (if present then it is a signed
#               package otherwise it is an unsigned package)
#             - Added support of new parameter --unsigned (Y/Yes/T/True N/No/F/False), this will set $global_var{"UNSIGNED"} to
#               value 1 in case of Y/Yes/T/True, variable will also be set to 1 if the -u parameter is used containing Solution_CI in the
#               provided upload link (ex: https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/Solution_CI)
#             - Added check to detemine if there will be enough diskspace available (check free space against expected maximum package size
#               as defined in variable $global_var{"PACKAGE-MAXSIZE"})
#
# v1.90     : - Bugfixes
#
# v1.91     : Download CSAR if package is found locally but .csar is missing (also valid for .md5 .sha256 .sha1)
#               Reason for missing .csar could either be that the local file is deleted or that in a previous download attempt the file
#               wasn´t downloaded due to (connection) problems with artifactory.
#
# v1.92     : Added support for cncs
#             Removed mandatory -l --link and made it optional
#             cncs versioning: ERIC-SC-CXP_903_8365_1_R16Z_378.unsigned.csar  _1_R16Z_378 : _1 = 1 _R16 = 15 Z = 25 _378 = 378 => 1.15.25+378
#
# v1.93     : Bugfixes
#
# v1.94     : Changed expected maximum file size for .yaml files from 60KB to 300KB
#
# v1.95     : Added .md5 .sha256 for files in the main package directory
#
# v1.96     : Bugfix for both signed and unsigned csar files locally downloaded
#
use strict;
use Cwd;
use Digest::MD5;
use Digest::SHA qw(sha256_hex);
use File::Find;
use File::Path;
use File::Copy;
use Time::HiRes qw(gettimeofday tv_interval);
use POSIX qw(strftime);
use Term::ANSIColor;
use version;


# ********************************
# *                              *
# * Global Variable declarations *
# *                              *
# ********************************
my $REVISION        = "1.95";
my $grep_str = "# \$Revision: ";
$REVISION = `cat $0 | grep '$grep_str'`;
$REVISION = substr($REVISION,index($REVISION,"\$")+11);
$REVISION = substr($REVISION,0,index($REVISION,"\$"));
$REVISION =~ s/^\s+|\s+//g;
my $script_name     = $0;
my $script_version  = $REVISION;

my $userName =  $ENV{'LOGNAME'};
my $artifact_token = $ENV{'ARTIFACTORY_TOKEN'};
my $token_received_as_argument;
my $orig_dir = getcwd();
#my $site = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/";
#my $site = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/";
my $BSF_url = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc";
my $SC_url = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/";
#my $CNDSC_url = "https://arm.seli.gic.ericsson.se/artifactory/proj-CNDSC-generic-local/eiffelesc/CNDSC/";
my $CNDSC_url = "https://arm.seli.gic.ericsson.se/artifactory/proj-cndsc-generic-local/eiffelesc/";
my %curl; # Used to construct the curl commands, variable is initialized by calling subroutine init_curl() or init_curl("API") or init_curl("IDENTITY")
my %server;
    $server{"info"}{"DOWNLOAD"}     = "seli";
    $server{"info"}{"UPLOAD"}       = "seli";
    $server{"seli"}{"NAMING"}       = "arm.seli.gic.ericsson.se";
    $server{"seli"}{"TOKEN"}        = $ENV{'ARTIFACTORY_TOKEN'};
    $server{"sero"}{"NAMING"}       = "arm.sero.gic.ericsson.se";
    $server{"sero"}{"TOKEN"}        = $ENV{'ARTIFACTORY_TOKEN'};

my %global_var;
    $global_var{"SITE"}                  = "https://".$server{"seli"}{"NAMING"}."/artifactory/proj-5g-bsf-generic-local/";
    $global_var{"SITE-ROOT"}             = "";
    $global_var{"NAMESPACE"}             = "eiffelesc";
    $global_var{"TARGET-DIRECTORY"}      = "/local/$userName/download";
    $global_var{"SOURCE-DIRECTORY"}      = "";      # used in combination with upload parameter
    $global_var{"PACKAGE-NAME"}          = "";      # Complete package release+version : 1.10.0+48
    $global_var{"PACKAGE-RELEASE"}       = "";      # Complete package release         : 1.10.0
    $global_var{"PACKAGE-VERSION"}       = "";      # Complete package version         : 48
    $global_var{"PACKAGE-SEPERATOR"}     = "+";     # Standard release and version are seperated by a '+' but it could be something else
    $global_var{"PACKAGE-MAXSIZE"}       = 18;      # Expected MAXIMUM size of package in GB
    $global_var{"PACKAGE-MINSIZE"}       = 3;       # Expected MINIMUM size of package in GB
    $global_var{"CURL-MAXTIME-DOWNLOAD"} = "300";
    $global_var{"CURL-MAXTIME-DEFAULT"}  = "300";
    $global_var{"GET_LATEST_BUILD"}      = 0;
    $global_var{"USE-FAST-FIND"}         = 1;
    $global_var{"DO-SHA256"}             = 1;
    $global_var{"COPY-ALL"}              = 0;
    $global_var{"PROGRESS-OFF"}          = 0;
    $global_var{"SILENT-ON"}             = 0;
    $global_var{"DRYRUN"}                = 0; # Option dry-run can only be used in combination with upload parameter, with dry-run the actual upload will not take place, only the printouts of what the script would do shown
    $global_var{"NO-MD5-CHECK"}          = 0;
    $global_var{"NO-SIZE-CHECK"}         = 0;
    $global_var{"NO-DOWNLOAD"}           = 0;
    $global_var{"SKIP-CHECKS"}           = 0;
    $global_var{"DOWNLOAD-ONLY"}         = 0;
    $global_var{"UPLOAD-DIR"}            = "";
    $global_var{"SYNC"}                  = 0;
    $global_var{"UNSIGNED"}              = 0;
    $global_var{"UNSIGNED-TEXT"}         = "";
    $global_var{"COPY-ONLY-CSAR"}        = 0;
    $global_var{"DOWNLOADED-CSAR-TYPE"}  = "";
    $global_var{"LOCAL-CSAR-TYPE"}       = "";
    $global_var{"CSAR-NAME"}             = "";
my %repo_info;
    $repo_info{"GENERIC"}{"DEFAULT_ARTIFACTORY_TYPE"}   = "SC";
    $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}           = $repo_info{"GENERIC"}{"DEFAULT_ARTIFACTORY_TYPE"};
    $repo_info{"GENERIC"}{"SITE"}                       = "https://".$server{"seli"}{"NAMING"}."/artifactory/";
    $repo_info{"GENERIC"}{"URL-TO-USE"}                 = "";
    # Repo info for SC
    $repo_info{"SC"}{"NAME"}                = "SC";
    $repo_info{"SC"}{"REPOSITORY"}          = "proj-5g-bsf-generic-local/";     # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc
    $repo_info{"SC"}{"HOME"}                = "eiffelesc/";
    $repo_info{"SC"}{"SKIP-FILECHECK"}      = 0;            # do NOT skip file checks
    # Repo info for SOLUTION CI
    $repo_info{"SOL_CI"}{"REPOSITORY"}      = "proj-5g-bsf-generic-local/";      # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/
    $repo_info{"SOL_CI"}{"HOME"}            = "Solution_CI_Storage/";
    $repo_info{"SOL_CI"}{"NAME"}            = "SOL_CI";
    $repo_info{"SOL_CI"}{"SKIP-FILECHECK"}  = 0;            # do NOT skip file checks
    # Repo info for CNDSC
    $repo_info{"CNDSC"}{"REPOSITORY"}       = "proj-cndsc-generic-local/";      # https://arm.seli.gic.ericsson.se/artifactory/proj-cndsc-generic-local/eiffelesc/
    $repo_info{"CNDSC"}{"HOME"}             = "eiffelesc/";
    $repo_info{"CNDSC"}{"NAME"}             = "CNDSC";
    $repo_info{"CNDSC"}{"SKIP-FILECHECK"}   = 0;            # skip file checks
    # Repo info for CNCS
    $repo_info{"CNCS"}{"REPOSITORY"}        = "proj-5g-bsf-generic-local/";      # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/cncs/eiffelesc/
    $repo_info{"CNCS"}{"HOME"}              = "cncs/eiffelesc/";
    $repo_info{"CNCS"}{"NAME"}              = "CNCS";
    $repo_info{"CNCS"}{"SKIP-FILECHECK"}    = 0;            # skip file checks
    # Repo info for SC GA Versions
    $repo_info{"SC-GA"}{"REPOSITORY"}       = "proj-5g-sc-generic-local/";      # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/
    $repo_info{"SC-GA"}{"HOME"}             = "GA-Versions/";
    $repo_info{"SC-GA"}{"NAME"}             = "SC-GA";
    $repo_info{"SC-GA"}{"SKIP-FILECHECK"}   = 0;            # do NOT skip file checks
my %timer;
my %site_entries;                       # hash containing all remote directory information needed within the script
my @stored_directory_content;           # Used during the redesign of download_csar to store the remote directory content, when complete hash is implemented this can be removed
my $curl_output;                        # Stores feedback received from cURL 'command'
my $use_alternative_dir;                # Variable is used in case the package name is not present in the
                                        # directory structure, for example all files at the HTTP site are
                                        # stored in /proj-5g-sc-generic-local/scp-bsf-rx-poc but the package
                                        # name provided is not "scp-bsf-rx-poc" but rather the revision "1.1.0+844"
                                        # The script will try to generate an alternative directory name from
                                        # provided information
my $previous_content;
my @md5_files;
my $md5_file;
my $download_all=0;
my $MIN_MANDATORY_FILES = 0;            # Add the NR-ENTRIES from all MANDATORY marked directory entries to have an estimate
my $skip_vnfd = 0;

# START New data structure

# Global var that contains a list of the package files that are locally stored
my %stored_lists;
my @local_file_list=();

# Global Var that contains all file_extensions, extracted from package_inf{}{}{"EXTENSION"}
my @all_package_file_extensions = ();

# Initial parameter is a directory from the package (example tools or csar)
# parameter                 description
# =======================   ==============================
# VALID-RELEASES            Determines for which releases this directory is valid
#                           Format for specific release                 : "9.9.9"
#                           Format for start from release               : "9.9.9+"
#                           Format for release range                    : "9.9.9-9.9.9" or for better readability "9.9.9 - 9.9.9"
#                           Format for combination of releases          : "9.9.9-9.9.9&9.9.9+" or for better readability "9.9.9 - 9.9.9 & 9.9.9+"
#                           example: directory "sc-config-sample" is ony starting from 1.6.0 onwards
#                               $package_info{"sc-config-sample"}{"VALID-RELEASES"} = "1.6.0+";
# VALID-RELEASE_TYPES       Possible values:
#                               SC
#                               CNDSC
#                               CNCS
#                               SC;CNDSC
#                               SC;CNCS
#                               CNCS;CNDSC
#                               SC;CNDSC;CNCS
# MANDATORY                 Determines if the directory is mandatory or not
# COPY                      Determines if the directory should be downloaded from artifactory or not
# NR-ENTRIES                The number of directory entries (number of 'x'), if for instance NR-ENTRIES = 2 then there will be DIR-CONTENTS-1 and
#                           DIR-CONTENTS-2
# NR-SUBDIR-ENTRIES         Same as previous parameter NR-ENTRIES, this parameter is to indicate that for this specific directory also all
#                           underlying subdirectories need to be taken into account.
#                           The files in each of those subdirectories are compared against the values layed out in the DIR-CONTENTS-x parameters.
#
# parameter         sub-paramter    description
# ===============   =============   ==============================
# DIR-CONTENTS-x    TYPE            values: FILE , CHECKSUM , DIR
#                                   If the checksum for a file needs to be checked then both FILE and CHECKSUM must be provided
# DIR-CONTENTS-x    EXTENSION       File extension for example: .csar for a filename that ends with .csar
# DIR-CONTENTS-x    MANDATORY       Is the presence of this file on artifactory (and downloaded package) mandatory or not
#                                   Even if the directory is NOT mandatory, the file can be set to mandatory, this means
#                                   that if the optional directory is present then the file MUST be present (when set to mandatory)
# DIR-CONTENTS-x    EXPECTED-SIZE   limits for the filesize, example a file between 512 Bytes and 20 Kilo Bytes would be represented
#                                   in this way:  $package_info{<directory>}{"DIR-CONTENTS-"+x}{"EXPECTED-SIZE"} = "512-20KB";
#                                   Allowed is : KB , MB , GB
#
my %package_info;
    $package_info{""}{"VALID-RELEASES"}          = "1.15.0+";
    $package_info{""}{"VALID-RELEASE_TYPES"}     = "SC;CNDSC";
    $package_info{""}{"MANDATORY"}               = 0;
    $package_info{""}{"COPY"}                    = 1;
    $package_info{""}{"NR-ENTRIES"}              = 3;
    $package_info{""}{"DIR-CONTENTS-1"}{"TYPE"}           = "FILE";
    $package_info{""}{"DIR-CONTENTS-1"}{"EXTENSION"}      = ".txt";
    $package_info{""}{"DIR-CONTENTS-1"}{"MANDATORY"}      = 1;
    $package_info{""}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}  = "1-10MB";
    $package_info{""}{"DIR-CONTENTS-2"}{"TYPE"}           = "FILE";
    $package_info{""}{"DIR-CONTENTS-2"}{"EXTENSION"}      = ".md5";
    $package_info{""}{"DIR-CONTENTS-2"}{"MANDATORY"}      = 1;
    $package_info{""}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}  = "32-256";
    $package_info{""}{"DIR-CONTENTS-3"}{"TYPE"}           = "FILE";
    $package_info{""}{"DIR-CONTENTS-3"}{"EXTENSION"}      = ".sha1";
    $package_info{""}{"DIR-CONTENTS-3"}{"MANDATORY"}      = 1;
    $package_info{""}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}  = "32-256";

    $package_info{"csar"}{"VALID-RELEASES"}                                 = "1.3.0+";             # From 1.3.0 onwards
    $package_info{"csar"}{"VALID-RELEASE_TYPES"}                            = "SC;CNDSC;CNCS;SOL_CI";      # valid for SC , CNDSC , SOL_CI , CNCS
    $package_info{"csar"}{"MANDATORY"}                                      = 1;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"csar"}{"COPY"}                                           = 1;                    # 1: COPY       0: DO NOT COPY
    $package_info{"csar"}{"NR-ENTRIES"}                                     = 4;
    $package_info{"csar"}{"DIR-CONTENTS-1"}{"TYPE"}                         = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"csar"}{"DIR-CONTENTS-1"}{"PRE-FIX"}                      = "eric-sc-";
    $package_info{"csar"}{"DIR-CONTENTS-1"}{"EXTENSION"}                    = ".csar";
    $package_info{"csar"}{"DIR-CONTENTS-1"}{"MANDATORY"}                    = 1;
    $package_info{"csar"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}                = "2.9GB-15.5GB";        # between  2.9GB - 8.5GB
    $package_info{"csar"}{"DIR-CONTENTS-2"}{"TYPE"}                         = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"csar"}{"DIR-CONTENTS-2"}{"EXTENSION"}                    = ".csar.md5";
    $package_info{"csar"}{"DIR-CONTENTS-2"}{"MANDATORY"}                    = 1;
    $package_info{"csar"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}                = "32-256";             # between 32 - 256 Bytes
    $package_info{"csar"}{"DIR-CONTENTS-3"}{"TYPE"}                         = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"csar"}{"DIR-CONTENTS-3"}{"EXTENSION"}                    = ".csar.sha256";
    $package_info{"csar"}{"DIR-CONTENTS-3"}{"MANDATORY"}                    = 1;
    $package_info{"csar"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}                = "64-256";             # between 64 - 256 Bytes
    $package_info{"csar"}{"DIR-CONTENTS-4"}{"TYPE"}                         = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"csar"}{"DIR-CONTENTS-4"}{"EXTENSION"}                    = ".csar.sha1";
    $package_info{"csar"}{"DIR-CONTENTS-4"}{"MANDATORY"}                    = 0;
    $package_info{"csar"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}                = "32-256";             # between 32 - 256 Bytes

    $package_info{"release-artifacts"}{"VALID-RELEASES"}                    = "1.3.0+";             # From 1.3.0 onwards
    $package_info{"release-artifacts"}{"VALID-RELEASE_TYPES"}                = "SC;CNDSC;SOL_CI;CNCS";     # valid for SC and CNDSC
    $package_info{"release-artifacts"}{"MANDATORY"}                         = 1;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"release-artifacts"}{"COPY"}                              = 1;                    # 1: COPY       0: DO NOT COPY
    $package_info{"release-artifacts"}{"NR-ENTRIES"}                        = 4;
    $package_info{"release-artifacts"}{"DIR-CONTENTS-1"}{"TYPE"}            = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"release-artifacts"}{"DIR-CONTENTS-1"}{"PRE-FIX"}         = "eric-sc-values-";
    $package_info{"release-artifacts"}{"DIR-CONTENTS-1"}{"EXTENSION"}       = ".yaml";
    $package_info{"release-artifacts"}{"DIR-CONTENTS-1"}{"MANDATORY"}       = 1;
    $package_info{"release-artifacts"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}   = "10B-300KB";          # between 10KB - 300KB
    $package_info{"release-artifacts"}{"DIR-CONTENTS-2"}{"TYPE"}            = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"release-artifacts"}{"DIR-CONTENTS-2"}{"EXTENSION"}       = ".yaml.sha256";
    $package_info{"release-artifacts"}{"DIR-CONTENTS-2"}{"MANDATORY"}       = 1;
    $package_info{"release-artifacts"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}   = "64-256";             # between 32 - 256 Bytes
    $package_info{"release-artifacts"}{"DIR-CONTENTS-3"}{"TYPE"}            = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"release-artifacts"}{"DIR-CONTENTS-3"}{"EXTENSION"}       = ".yaml.md5";
    $package_info{"release-artifacts"}{"DIR-CONTENTS-3"}{"MANDATORY"}       = 0;
    $package_info{"release-artifacts"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}   = "32-256";             # between 32 - 256 Bytes
    $package_info{"release-artifacts"}{"DIR-CONTENTS-4"}{"TYPE"}            = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"release-artifacts"}{"DIR-CONTENTS-4"}{"EXTENSION"}       = ".yaml.sha1";
    $package_info{"release-artifacts"}{"DIR-CONTENTS-4"}{"MANDATORY"}       = 0;
    $package_info{"release-artifacts"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}   = "32-256";             # between 32 - 256 Bytes

    $package_info{"tools"}{"VALID-RELEASES"}                                = "1.3.0+";             # From 1.3.0 onwards
    $package_info{"tools"}{"VALID-RELEASE_TYPES"}                           = "SC;CNDSC;SOL_CI;CNCS";      # valid for SC and CNDSC
    $package_info{"tools"}{"MANDATORY"}                                     = 1;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"tools"}{"COPY"}                                          = 1;                    # 1: COPY       0: DO NOT COPY
    $package_info{"tools"}{"NR-ENTRIES"}                                    = 4;
    $package_info{"tools"}{"DIR-CONTENTS-1"}{"TYPE"}                        = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"tools"}{"DIR-CONTENTS-1"}{"PRE-FIX"}                     = "eric-sc-tools-";
    $package_info{"tools"}{"DIR-CONTENTS-1"}{"EXTENSION"}                   = ".tgz";
    $package_info{"tools"}{"DIR-CONTENTS-1"}{"MANDATORY"}                   = 1;
    $package_info{"tools"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}               = "2KB-100MB";          # between 70MB - 100MB
    $package_info{"tools"}{"DIR-CONTENTS-2"}{"TYPE"}                        = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"tools"}{"DIR-CONTENTS-2"}{"EXTENSION"}                   = ".tgz.sha256";
    $package_info{"tools"}{"DIR-CONTENTS-2"}{"MANDATORY"}                   = 1;
    $package_info{"tools"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}               = "64-256";             # between 32 - 256 Bytes
    $package_info{"tools"}{"DIR-CONTENTS-3"}{"TYPE"}                        = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"tools"}{"DIR-CONTENTS-3"}{"EXTENSION"}                   = ".tgz.md5";
    $package_info{"tools"}{"DIR-CONTENTS-3"}{"MANDATORY"}                   = 0;
    $package_info{"tools"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}               = "32-256";             # between 32 - 256 Bytes
    $package_info{"tools"}{"DIR-CONTENTS-4"}{"TYPE"}                        = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"tools"}{"DIR-CONTENTS-4"}{"EXTENSION"}                   = ".tgz.sha1";
    $package_info{"tools"}{"DIR-CONTENTS-4"}{"MANDATORY"}                   = 0;
    $package_info{"tools"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}               = "32-256";             # between 32 - 256 Bytes

    $package_info{"tools/docker"}{"VALID-RELEASES"}                         = "1.8.1+";             # From 1.8.1 onwards
    $package_info{"tools/docker"}{"VALID-RELEASE_TYPES"}                    = "SC";                 # valid for SC
    $package_info{"tools/docker"}{"MANDATORY"}                              = 0;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"tools/docker"}{"COPY"}                                   = 1;                    # 1: COPY       0: DO NOT COPY
    $package_info{"tools/docker"}{"NR-ENTRIES"}                             = 2;
    $package_info{"tools/docker"}{"DIR-CONTENTS-1"}{"TYPE"}                 = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"tools/docker"}{"DIR-CONTENTS-1"}{"PRE-FIX"}              = "eric-supreme-";
    $package_info{"tools/docker"}{"DIR-CONTENTS-1"}{"EXTENSION"}            = ".tar";
    $package_info{"tools/docker"}{"DIR-CONTENTS-1"}{"MANDATORY"}            = 1;
    $package_info{"tools/docker"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}        = "250MB-400MB";        # between 250MB - 400MB
    $package_info{"tools/docker"}{"DIR-CONTENTS-2"}{"TYPE"}                 = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"tools/docker"}{"DIR-CONTENTS-2"}{"EXTENSION"}            = ".tar.sha256";
    $package_info{"tools/docker"}{"DIR-CONTENTS-2"}{"MANDATORY"}            = 1;
    $package_info{"tools/docker"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}        = "64-256";             # between 32 - 256 Bytes

    $package_info{"sc-config-sample"}{"VALID-RELEASES"}                     = "1.6.0+";             # From 1.6.0 onwards
    $package_info{"sc-config-sample"}{"VALID-RELEASE_TYPES"}                = "SC;";                # valid for SC
    $package_info{"sc-config-sample"}{"MANDATORY"}                          = 0;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"sc-config-sample"}{"COPY"}                               = 1;                    # 1: COPY       0: DO NOT COPY
    $package_info{"sc-config-sample"}{"NR-ENTRIES"}                         = 2;
    $package_info{"sc-config-sample"}{"DIR-CONTENTS-1"}{"TYPE"}             = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"sc-config-sample"}{"DIR-CONTENTS-1"}{"EXTENSION"}        = ".netconf";
    $package_info{"sc-config-sample"}{"DIR-CONTENTS-1"}{"MANDATORY"}        = 1;
    $package_info{"sc-config-sample"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}    = "1-256MB";           # between 70MB - 100MB
    $package_info{"sc-config-sample"}{"DIR-CONTENTS-2"}{"TYPE"}             = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"sc-config-sample"}{"DIR-CONTENTS-2"}{"EXTENSION"}        = ".netconf.sha256";
    $package_info{"sc-config-sample"}{"DIR-CONTENTS-2"}{"MANDATORY"}        = 1;
    $package_info{"sc-config-sample"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}    = "16-256";             # between 32 - 256 Bytes

    $package_info{"sc-config-reference"}{"VALID-RELEASES"}                  = "1.6.0+";             # From 1.6.0 onwards
    $package_info{"sc-config-reference"}{"MANDATORY"}                       = 0;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"sc-config-reference"}{"COPY"}                            = 1;                    # 1: COPY       0: DO NOT COPY

    $package_info{"cbos-age-reports"}{"VALID-RELEASES"}                     = "1.7.0+";             # From 1.7.0 onwards
    $package_info{"cbos-age-report"}{"VALID-RELEASE_TYPES"}                 = "SC";                 # valid for SC and CNDSC
    $package_info{"cbos-age-reports"}{"MANDATORY"}                          = 0;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"cbos-age-reports"}{"COPY"}                               = 0;                    # 1: COPY       0: DO NOT COPY
    $package_info{"cbos-age-reports"}{"NR-ENTRIES"}                         = 4;
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-1"}{"TYPE"}             = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-1"}{"EXTENSION"}        = ".html";
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-1"}{"MANDATORY"}        = 1;
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}    = "1B-1MB";             # between  1B - 1MB
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-2"}{"TYPE"}             = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-2"}{"EXTENSION"}        = ".html.sha256";
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-2"}{"MANDATORY"}        = 1;
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}    = "32-256";             # between 32 - 256 Bytes
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-3"}{"TYPE"}             = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-3"}{"EXTENSION"}        = ".json";
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-3"}{"MANDATORY"}        = 1;
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}    = "1B-1MB";             # between  1B - 1MB
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-4"}{"TYPE"}             = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-4"}{"EXTENSION"}        = ".json.sha256";
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-4"}{"MANDATORY"}        = 1;
    $package_info{"cbos-age-reports"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}    = "32-256";             # between 64 - 256 Bytes

    $package_info{"cbos"}{"VALID-RELEASES"}                                 = "1.7.0+";             # From 1.7.0 onwards
    $package_info{"cbos"}{"VALID-RELEASE_TYPES"}                            = "CNDSC";              # valid for SC and CNDSC
    $package_info{"cbos"}{"MANDATORY"}                                      = 0;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"cbos"}{"COPY"}                                           = 0;                    # 1: COPY       0: DO NOT COPY
    $package_info{"cbos"}{"NR-ENTRIES"}                                     = 4;
    $package_info{"cbos"}{"DIR-CONTENTS-1"}{"TYPE"}                         = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"cbos"}{"DIR-CONTENTS-1"}{"EXTENSION"}                    = ".html";
    $package_info{"cbos"}{"DIR-CONTENTS-1"}{"MANDATORY"}                    = 0;
    $package_info{"cbos"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}                = "1B-1MB";             # between  1B - 1MB
    $package_info{"cbos"}{"DIR-CONTENTS-2"}{"TYPE"}                         = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"cbos"}{"DIR-CONTENTS-2"}{"EXTENSION"}                    = ".html.sha256";
    $package_info{"cbos"}{"DIR-CONTENTS-2"}{"MANDATORY"}                    = 0;
    $package_info{"cbos"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}                = "32-256";             # between 32 - 256 Bytes
    $package_info{"cbos"}{"DIR-CONTENTS-3"}{"TYPE"}                         = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"cbos"}{"DIR-CONTENTS-3"}{"EXTENSION"}                    = ".json";
    $package_info{"cbos"}{"DIR-CONTENTS-3"}{"MANDATORY"}                    = 0;
    $package_info{"cbos"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}                = "1B-1MB";             # between  1B - 1MB
    $package_info{"cbos"}{"DIR-CONTENTS-4"}{"TYPE"}                         = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"cbos"}{"DIR-CONTENTS-4"}{"EXTENSION"}                    = ".json.sha256";
    $package_info{"cbos"}{"DIR-CONTENTS-4"}{"MANDATORY"}                    = 0;
    $package_info{"cbos"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}                = "32-256";             # between 64 - 256 Bytes

    $package_info{"base_stability_traffic"}{"VALID-RELEASES"}                     = "1.8.1+";             # From 1.8.1 onwards
    $package_info{"base_stability_traffic"}{"VALID-RELEASE_TYPES"}                = "SC";                 # valid for SC
    $package_info{"base_stability_traffic"}{"MANDATORY"}                          = 1;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"base_stability_traffic"}{"COPY"}                               = 1;                    # 1: COPY       0: DO NOT COPY
    $package_info{"base_stability_traffic"}{"NR-ENTRIES"}                         = 4;
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-1"}{"TYPE"}             = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-1"}{"EXTENSION"}        = ".config";
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-1"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}    = "1B-10MB";            # between 1B - 10MB
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-2"}{"TYPE"}             = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-2"}{"EXTENSION"}        = ".config.sha256";
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-2"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}    = "16-256";             # between 32 - 256 Bytes
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-3"}{"TYPE"}             = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-3"}{"EXTENSION"}        = ".txt";
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-3"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}    = "1B-10MB";            # between 1B - 10MB
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-4"}{"TYPE"}             = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-4"}{"EXTENSION"}        = ".txt.sha256";
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-4"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}    = "16-256";             # between 32 - 256 Bytes

    # base_stability_traffic/traffic_config
    $package_info{"base_stability_traffic/traffic_config"}{"VALID-RELEASES"}                     = "1.8.1+";             # From 1.8.1 onwards
    $package_info{"base_stability_traffic/traffic_config"}{"VALID-RELEASE_TYPES"}                = "SC";                 # valid for SC and CNDSC
    $package_info{"base_stability_traffic/traffic_config"}{"MANDATORY"}                          = 1;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"base_stability_traffic/traffic_config"}{"COPY"}                               = 1;                    # 1: COPY       0: DO NOT COPY
    $package_info{"base_stability_traffic/traffic_config"}{"NR-SUBDIR-ENTRIES"}                  = 4;
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-1"}{"TYPE"}             = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-1"}{"EXTENSION"}        = ".netconf";
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-1"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}    = "1B-10MB";            # between 1B - 10MB
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-2"}{"TYPE"}             = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-2"}{"EXTENSION"}        = ".netconf.sha256";
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-2"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}    = "16-256";             # between 32 - 256 Bytes
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-3"}{"TYPE"}             = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-3"}{"EXTENSION"}        = ".bash";
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-3"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}    = "1B-10MB";            # between 1B - 10MB
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-4"}{"TYPE"}             = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-4"}{"EXTENSION"}        = ".bash.sha256";
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-4"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic/traffic_config"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}    = "16-256";             # between 32 - 256 Bytes

    # base_stability_traffic/traffic_scripts/k6
    $package_info{"base_stability_traffic/traffic_scripts"}{"VALID-RELEASES"}                     = "1.8.1+";             # From 1.8.1 onwards
    $package_info{"base_stability_traffic/traffic_scripts"}{"VALID-RELEASE_TYPES"}                = "SC";                 # valid for SC and CNDSC
    $package_info{"base_stability_traffic/traffic_scripts"}{"MANDATORY"}                          = 1;                    # 1: MANDATORY  0: OPTIONAL
    $package_info{"base_stability_traffic/traffic_scripts"}{"COPY"}                               = 1;                    # 1: COPY       0: DO NOT COPY
    $package_info{"base_stability_traffic/traffic_scripts"}{"NR-SUBDIR-ENTRIES"}                  = 2;
    $package_info{"base_stability_traffic/traffic_scripts"}{"DIR-CONTENTS-1"}{"TYPE"}             = "FILE";               # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_scripts"}{"DIR-CONTENTS-1"}{"EXTENSION"}        = ".js";
    $package_info{"base_stability_traffic/traffic_scripts"}{"DIR-CONTENTS-1"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic/traffic_scripts"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}    = "1B-10MB";            # between 1B - 10MB
    $package_info{"base_stability_traffic/traffic_scripts"}{"DIR-CONTENTS-2"}{"TYPE"}             = "CHECKSUM";           # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_scripts"}{"DIR-CONTENTS-2"}{"EXTENSION"}        = ".js.sha256";
    $package_info{"base_stability_traffic/traffic_scripts"}{"DIR-CONTENTS-2"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic/traffic_scripts"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}    = "16-256";             # between 32 - 256 Bytes

    # base_stability_traffic/traffic_simulators/docker
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"VALID-RELEASES"}                     = "1.8.1+";               # From 1.8.1 onwards
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"VALID-RELEASE_TYPES"}                = "SC";                   # valid for SC and CNDSC
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"MANDATORY"}                          = 1;                      # 1: MANDATORY  0: OPTIONAL
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"COPY"}                               = 1;                      # 1: COPY       0: DO NOT COPY
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"NR-ENTRIES"}                         = 2;
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"DIR-CONTENTS-1"}{"TYPE"}             = "FILE";                 # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"DIR-CONTENTS-1"}{"EXTENSION"}        = ".tar";
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"DIR-CONTENTS-1"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}    = "3MB-1.5GB";            # between 30MB - 1.5GB
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"DIR-CONTENTS-2"}{"TYPE"}             = "CHECKSUM";             # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"DIR-CONTENTS-2"}{"EXTENSION"}        = ".tar.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"DIR-CONTENTS-2"}{"MANDATORY"}        = 1;
    $package_info{"base_stability_traffic/traffic_simulators/docker"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}    = "16-256";               # between 32 - 256 Bytes

    # base_stability_traffic/traffic_simulators/helm
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"VALID-RELEASES"}                       = "1.8.1+";               # From 1.8.1 onwards
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"VALID-RELEASE_TYPES"}                  = "SC";                   # valid for SC and CNDSC
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"MANDATORY"}                            = 1;                      # 1: MANDATORY  0: OPTIONAL
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"COPY"}                                 = 1;                      # 1: COPY       0: DO NOT COPY
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"NR-ENTRIES"}                           = 2;
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"DIR-CONTENTS-1"}{"TYPE"}               = "FILE";                 # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"DIR-CONTENTS-1"}{"EXTENSION"}          = ".tgz";
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"DIR-CONTENTS-1"}{"MANDATORY"}          = 1;
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}      = "1B-50KB";              # between 1B - 50KB
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"DIR-CONTENTS-2"}{"TYPE"}               = "CHECKSUM";             # FILE , CHECKSUM or DIR
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"DIR-CONTENTS-2"}{"EXTENSION"}          = ".tgz.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"DIR-CONTENTS-2"}{"MANDATORY"}          = 1;
    $package_info{"base_stability_traffic/traffic_simulators/helm"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}      = "16-256";               # between 32 - 256 Bytes

    # base_stability_traffic/traffic_simulators/versions  (automatic generated)
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"VALID-RELEASES"}                   = "1.8.1+";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"VALID-RELEASE_TYPES"}              = "SC";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"MANDATORY"}                        = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"COPY"}                             = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"NR-ENTRIES"}                       = 16;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-1"}{"TYPE"}           = "FILE";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-1"}{"EXTENSION"}      = ".dscload-version";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-1"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}  = "1-26";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-2"}{"TYPE"}           = "CHECKSUM";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-2"}{"EXTENSION"}      = ".dscload-version.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-2"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}  = "1-128";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-3"}{"TYPE"}           = "FILE";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-3"}{"EXTENSION"}      = ".influxdb-version";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-3"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}  = "1-26";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-4"}{"TYPE"}           = "CHECKSUM";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-4"}{"EXTENSION"}      = ".influxdb-version.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-4"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}  = "1-128";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-5"}{"TYPE"}           = "FILE";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-5"}{"EXTENSION"}      = ".k6-version";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-5"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-5"}{"EXPECTED-SIZE"}  = "1-26";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-6"}{"TYPE"}           = "CHECKSUM";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-6"}{"EXTENSION"}      = ".k6-version.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-6"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-6"}{"EXPECTED-SIZE"}  = "1-128";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-7"}{"TYPE"}           = "FILE";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-7"}{"EXTENSION"}      = ".nrfsim-version";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-7"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-7"}{"EXPECTED-SIZE"}  = "1-26";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-8"}{"TYPE"}           = "CHECKSUM";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-8"}{"EXTENSION"}      = ".nrfsim-version.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-8"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-8"}{"EXPECTED-SIZE"}  = "1-128";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-9"}{"TYPE"}           = "FILE";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-9"}{"EXTENSION"}      = ".seppsim-version";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-9"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-9"}{"EXPECTED-SIZE"}  = "1-26";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-10"}{"TYPE"}           = "CHECKSUM";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-10"}{"EXTENSION"}      = ".seppsim-version.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-10"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-10"}{"EXPECTED-SIZE"}  = "1-128";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-11"}{"TYPE"}           = "FILE";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-11"}{"EXTENSION"}      = ".sftp-version";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-11"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-11"}{"EXPECTED-SIZE"}  = "1-26";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-12"}{"TYPE"}           = "CHECKSUM";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-12"}{"EXTENSION"}      = ".sftp-version.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-12"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-12"}{"EXPECTED-SIZE"}  = "1-128";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-13"}{"TYPE"}           = "FILE";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-13"}{"EXTENSION"}      = ".chfsim-version";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-13"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-13"}{"EXPECTED-SIZE"}  = "1-26";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-14"}{"TYPE"}           = "CHECKSUM";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-14"}{"EXTENSION"}      = ".chfsim-version.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-14"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-14"}{"EXPECTED-SIZE"}  = "1-128";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-15"}{"TYPE"}           = "FILE";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-15"}{"EXTENSION"}      = ".redis-version";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-15"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-15"}{"EXPECTED-SIZE"}  = "1-26";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-16"}{"TYPE"}           = "CHECKSUM";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-16"}{"EXTENSION"}      = ".redis-version.sha256";
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-16"}{"MANDATORY"}      = 1;
    $package_info{"base_stability_traffic/traffic_simulators/versions"}{"DIR-CONTENTS-16"}{"EXPECTED-SIZE"}  = "1-128";

    # certificates  Including all it's subdirectories, each directory can contain files with
    #               extensions as defined below
    $package_info{"certificates"}{"VALID-RELEASES"}                         = "1.8.1+";
    $package_info{"certificates"}{"VALID-RELEASE_TYPES"}                    = "SC";
    $package_info{"certificates"}{"MANDATORY"}                              = 0;
    $package_info{"certificates"}{"COPY"}                                   = 1;
    $package_info{"certificates"}{"NR-SUBDIR-ENTRIES"}                      = 16;
    $package_info{"certificates"}{"DIR-CONTENTS-1"}{"TYPE"}                 = "FILE";
    $package_info{"certificates"}{"DIR-CONTENTS-1"}{"EXTENSION"}            = ".pem";
    $package_info{"certificates"}{"DIR-CONTENTS-1"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}        = "1-10KB";
    $package_info{"certificates"}{"DIR-CONTENTS-2"}{"TYPE"}                 = "CHECKSUM";
    $package_info{"certificates"}{"DIR-CONTENTS-2"}{"EXTENSION"}            = ".pem.sha256";
    $package_info{"certificates"}{"DIR-CONTENTS-2"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-2"}{"EXPECTED-SIZE"}        = "1-128";

    $package_info{"certificates"}{"DIR-CONTENTS-3"}{"TYPE"}                 = "FILE";
    $package_info{"certificates"}{"DIR-CONTENTS-3"}{"EXTENSION"}            = ".p12";
    $package_info{"certificates"}{"DIR-CONTENTS-3"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-3"}{"EXPECTED-SIZE"}        = "1-10KB";
    $package_info{"certificates"}{"DIR-CONTENTS-4"}{"TYPE"}                 = "CHECKSUM";
    $package_info{"certificates"}{"DIR-CONTENTS-4"}{"EXTENSION"}            = ".p12.sha256";
    $package_info{"certificates"}{"DIR-CONTENTS-4"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-4"}{"EXPECTED-SIZE"}        = "1-128";

    $package_info{"certificates"}{"DIR-CONTENTS-5"}{"TYPE"}                 = "FILE";
    $package_info{"certificates"}{"DIR-CONTENTS-5"}{"EXTENSION"}            = ".srl";
    $package_info{"certificates"}{"DIR-CONTENTS-5"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-5"}{"EXPECTED-SIZE"}        = "1-1KB";
    $package_info{"certificates"}{"DIR-CONTENTS-6"}{"TYPE"}                 = "CHECKSUM";
    $package_info{"certificates"}{"DIR-CONTENTS-6"}{"EXTENSION"}            = ".srl.sha256";
    $package_info{"certificates"}{"DIR-CONTENTS-6"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-6"}{"EXPECTED-SIZE"}        = "1-128";

    $package_info{"certificates"}{"DIR-CONTENTS-7"}{"TYPE"}                 = "FILE";
    $package_info{"certificates"}{"DIR-CONTENTS-7"}{"EXTENSION"}            = ".cli";
    $package_info{"certificates"}{"DIR-CONTENTS-7"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-7"}{"EXPECTED-SIZE"}        = "1-64KB";
    $package_info{"certificates"}{"DIR-CONTENTS-8"}{"TYPE"}                 = "CHECKSUM";
    $package_info{"certificates"}{"DIR-CONTENTS-8"}{"EXTENSION"}            = ".cli.sha256";
    $package_info{"certificates"}{"DIR-CONTENTS-8"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-8"}{"EXPECTED-SIZE"}        = "1-128";

    $package_info{"certificates"}{"DIR-CONTENTS-9"}{"TYPE"}                 = "FILE";
    $package_info{"certificates"}{"DIR-CONTENTS-9"}{"EXTENSION"}            = ".crt";
    $package_info{"certificates"}{"DIR-CONTENTS-9"}{"MANDATORY"}            = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-9"}{"EXPECTED-SIZE"}        = "1-10KB";
    $package_info{"certificates"}{"DIR-CONTENTS-10"}{"TYPE"}                = "CHECKSUM";
    $package_info{"certificates"}{"DIR-CONTENTS-10"}{"EXTENSION"}           = ".crt.sha256";
    $package_info{"certificates"}{"DIR-CONTENTS-10"}{"MANDATORY"}           = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-10"}{"EXPECTED-SIZE"}       = "1-128";

    $package_info{"certificates"}{"DIR-CONTENTS-11"}{"TYPE"}                = "FILE";
    $package_info{"certificates"}{"DIR-CONTENTS-11"}{"EXTENSION"}           = ".csr";
    $package_info{"certificates"}{"DIR-CONTENTS-11"}{"MANDATORY"}           = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-11"}{"EXPECTED-SIZE"}       = "1-10KB";
    $package_info{"certificates"}{"DIR-CONTENTS-12"}{"TYPE"}                = "CHECKSUM";
    $package_info{"certificates"}{"DIR-CONTENTS-12"}{"EXTENSION"}           = ".csr.sha256";
    $package_info{"certificates"}{"DIR-CONTENTS-12"}{"MANDATORY"}           = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-12"}{"EXPECTED-SIZE"}       = "1-128";

    $package_info{"certificates"}{"DIR-CONTENTS-13"}{"TYPE"}                = "FILE";
    $package_info{"certificates"}{"DIR-CONTENTS-13"}{"EXTENSION"}           = ".key";
    $package_info{"certificates"}{"DIR-CONTENTS-13"}{"MANDATORY"}           = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-13"}{"EXPECTED-SIZE"}       = "1-10KB";
    $package_info{"certificates"}{"DIR-CONTENTS-14"}{"TYPE"}                = "CHECKSUM";
    $package_info{"certificates"}{"DIR-CONTENTS-14"}{"EXTENSION"}           = ".key.sha256";
    $package_info{"certificates"}{"DIR-CONTENTS-14"}{"MANDATORY"}           = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-14"}{"EXPECTED-SIZE"}       = "1-128";

    $package_info{"certificates"}{"DIR-CONTENTS-15"}{"TYPE"}                = "FILE";
    $package_info{"certificates"}{"DIR-CONTENTS-15"}{"EXTENSION"}           = ".txt";
    $package_info{"certificates"}{"DIR-CONTENTS-15"}{"MANDATORY"}           = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-15"}{"EXPECTED-SIZE"}       = "1-10KB";
    $package_info{"certificates"}{"DIR-CONTENTS-16"}{"TYPE"}                = "CHECKSUM";
    $package_info{"certificates"}{"DIR-CONTENTS-16"}{"EXTENSION"}           = ".txt.sha256";
    $package_info{"certificates"}{"DIR-CONTENTS-16"}{"MANDATORY"}           = 1;
    $package_info{"certificates"}{"DIR-CONTENTS-16"}{"EXPECTED-SIZE"}       = "1-128";

# END New data structure

my @check_package_info;
my @add_to_package_info;

my %color_info;
    $color_info{"use_color"}        = 1;
    $color_info{"ERR_Color"}        = "red blink";
    $color_info{"INF_Color"}        = "bold yellow on_black";
    $color_info{"TIME_Color"}       = "green";
    $color_info{"DRYRUN_Color"}     = "blue";
    $color_info{"BLUE_Color"}       = "blue";
    $color_info{"RED_Color"}        = "red";

my %ERROR_CODE;
    $ERROR_CODE{"NormalExit"}           = 0;    # No error, all went well we hope and pray
    $ERROR_CODE{"Standard"}             = 1;    # Standard return value for errors
    $ERROR_CODE{"PackageNotFound"}      = 2;    # Package not found at site
    $ERROR_CODE{"Directory"}            = 3;    # Problems creating or switching to directory
    $ERROR_CODE{"PackageName"}          = 4;    # Package name not provided
    $ERROR_CODE{"Download"}             = 5;    # Error during download
    $ERROR_CODE{"Failed_md5"}           = 6;    # MD5/SHA256 check failed
    $ERROR_CODE{"Open_file"}            = 7;    # Could not open file
    $ERROR_CODE{"Missing_file"}         = 8;    # File missing
    $ERROR_CODE{"Package_size"}         = 9;    # Package size not within expected range
    $ERROR_CODE{"Timeout"}              = 10;   # Curl command timed out
    $ERROR_CODE{"Corrupted"}            = 11;   # File Corrupted
    $ERROR_CODE{"404"}                  = 12;
    $ERROR_CODE{"InvalidToken"}         = 13;

my %GLOBAL_DEBUG;
my %GLOBAL_LOGGING;
my $ALL_PRESENT = 0;

sub show_help {
    my $site = $global_var{"SITE"};
    print <<EOF;

Description:
============

This script copies CSAR package from artifactory and stores it either in
a default or a specified directory

Syntax:=======

 $0 [<OPTIONAL>][<MANDATORY>]

    <MANDATORY> are one or more of the following parameters:


    -p / --package <package>    Name of package to download


    <OPTIONAL> are one or more of the following parameters:

    -h / --help                 Shows this help

    -t / --target <target>      Use target directory to store files
                                Default: /local/$userName/download/<package_name>

    -l / --link <http-link>     Use <http-link> for downloading the package instead
                                of default link ($site), use with care

    -s / --silent               Do not provide output (except for errors), do not use color output

    -n / --no-progress          Do not show progress bar during download, rather guess
                                how far the download is and place bets on it

    -u / --upload               After finishing download of package and performing sanity check
                                start uploading the package to the release repo as indicated by
                                the target. If parameter is not provided this will be skipped.
                                Only files that are not present in the repository will be uploaded.
                                To replace remote files with local ones use option --upload-overwrite in addition.
                                example:
                                -u https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/SC1.2.0/PRA/1.2.0+14
                                There are NO default values for this parameter, use with care

    --upload-all                Upload all files in the source directory not found on the repository, to be used in combination with -u/--upload

    --upload-overwrite          Also replace existing files on the repository, to be used in combination with -u/--upload

    --artifactory-token         Provide Artifactory-token to be used instead of reading it from the environment

    --dry-run                   This parameter has only effect in combination with the -u/--upload parameter
                                When the parameter is provided the actual upload towards the release repo will
                                NOT take place, instead the commands to do the upload will be printed for verification
                                The user is prompted to perform the upload or skip it (by pressing y/Y when prompted
                                the upload will take place, any other input will make the script continue without uploading

    --source                    This parameter has only effect in combination with the -u/--upload parameter and
                                parameter --no-download
                                The parameter indicates the source directory from which to upload
                                There are NO default values for this parameter, use with care

    --download=<list>           Comma seperated list of directories that should be downloaded, example:
                                --download "csar,tools"
                                        will only download the csar and tools directory
                                --download "csar,tools,release-artifacts,sc-config-sample"
                                        will download the csar, tools, release-artifacts and sc-config-sample directory
                                --download "all"
                                        will download all directories, same as --download-all

    --sha256                    Perform sha256 check on all received files

    --no-download               Skip downloading the package

    --no-md5                    Skip md5 check

    --no-sizecheck              Skip checking packagesize

    --color=<string>            Supported values for <string> :
                                none        : no color output is to be used
                                no          : no color output is to be used

    --max-csar=<size>           changes defined excepted filesize range to provided one
    --max-yaml=<size>           Units : 10 10KB 10MB 10GB
    --max-tgz= <size>            Syntax: =10MB       (Exact 10MB is expected)
                                        1KB-10MB    (filesize between 1KB and 10MB)
                                        64KB        (filesize between 1Byte and 64KB)


Examples:
=========

  Example 1:
  ----------
  Download CSAR package : scp-bsf-rx-poc into default directory

  $0 --package scp-bsf-rx-poc


  Example 2:
  ----------
  Download CSAR package : 1.1.0+844 into default directory

  $0 --package 1.1.0+844


  Example 3:
  ----------
  Download CSAR package : scp-bsf-rx-poc into specific directory

  $0 --package scp-bsf-rx-poc --target /home/$userName/download

  Example 4:
  ----------
  Download CSAR package : 1.2.0+14
  Use download link     : https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/SC1.2.0/eiffelesc/
  Upload package to     : https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/SC1.2.0/PRA/1.2.0+14
  $0 -l https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/SC1.2.0/eiffelesc/ -p 1.2.0+14 -u https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/SC1.2.0/PRA/1.2.0+14

  Example 5:
  ----------
  Upload CSAR package   : 1.2.0+14
  package location      : /home/$userName/download
  Upload package to     : https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/SC1.2.0/PRA
  Upload all files and replace the repository ones.
  $0 --source /home/$userName/download -p 1.2.0+14 -u https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/SC1.2.0/PRA/ --upload-all --upload-overwrite

Return code:
============
  $ERROR_CODE{"NormalExit"}: Successful
  $ERROR_CODE{"Standard"}: Standard return value for errors, unspecified error
  $ERROR_CODE{"PackageNotFound"}: Package not found at site
  $ERROR_CODE{"Directory"}: Problems creating or switching to directory
  $ERROR_CODE{"PackageName"}: Package name not provided
  $ERROR_CODE{"Download"}: Error during download
  $ERROR_CODE{"Failed_md5"}: md5/sha256 check failed
  $ERROR_CODE{"Open_file"}: Could not open file
  $ERROR_CODE{"Missing_file"}: File is missing
  $ERROR_CODE{"Package_size"}: Package size not within expected range

EOF
}


# ***********************************************************************
# *                                                                     *
# * initialize %curl to use found and prefered tokens $curl{"USE"}      *
# * along with the standard curl command $curl{"CURL"}                  *
# *                                                                     *
# * return value is the prefered token type (API,ACCESS) to use         *
# ***********************************************************************
sub init_curl {
    my $use_token      = $_[0];
    my $use_token_2    = $_[1];
    my $artifact_token = $server{"seli"}{"TOKEN"};
    my $artifact_token_2 = $ENV{'ARTIFACTORY_TOKEN_2'};

    $curl{"USE"}                = "API";
    $curl{"OPTIONS"}            = " "; # standard options which are same for ecery curl command
    $curl{"DEL"}                = " -x DELETE";
    $curl{"TOKEN"}          = $artifact_token;
    $curl{"TOKEN_2"}        = $artifact_token_2;

    $curl{"ACCESS-AVAILABLE"}     = 0;

    if (($artifact_token eq "\n") || (!$artifact_token)) {
        $artifact_token = "";
        if (-e "~/.5g.devenv.profile") {
            my $token = `cat ~/.5g.devenv.profile | grep ARTIFACTORY_TOKEN`;
            my @tokens = split('\n',$token);
            foreach (@tokens) {
                if (substr($_,0,1) ne "#") {
                    $artifact_token = $_;
                    $artifact_token = substr($artifact_token,index($artifact_token,"="));
                    last;
                }
            }
        }
    }
    if ($artifact_token) {
        print "found API token: ".substr($artifact_token,0,4)."...".substr($artifact_token,-4)."\n";
        $curl{"TOKEN"}      = $artifact_token;
        $curl{"AVAILABLE"}  = 1;
        $curl{"CURL"}       = "curl".$curl{"OPTIONS"}." -H \"X-JFrog-Art-Api:".$curl{"TOKEN"}."\"";
        $curl{"CURL-PRINT"} = "curl".$curl{"OPTIONS"}." -H \"X-JFrog-Art-Api:".substr($artifact_token,0,4)."...".substr($artifact_token,-4)."\"";
    } else {
        $curl{"AVAILABLE"}  = 0;
    }
    if ($artifact_token_2) {
        print "found API token: ".substr($artifact_token_2,0,4)."...".substr($artifact_token_2,-4)."\n";
        $curl{"TOKEN_2"}      = $artifact_token_2;
        $curl{"AVAILABLE_2"}  = 1;
        $curl{"CURL_2"}       = "curl".$curl{"OPTIONS"}." -H \"X-JFrog-Art-Api:".$curl{"TOKEN_2"}."\"";
    } else {
        $curl{"AVAILABLE_2"}  = 0;
    }

    if ($use_token) {
        # check if a 'real' token was passed on
        my $test;
        my $found_token;
            my_print("Checking if provided token can be used as an Artifactory API token");
            $test = "curl --silent -H \"X-JFrog-Art-Api:$use_token\" -XGET \"https://".$server{"seli"}{"NAMING"}."/artifactory/proj-5g-bsf-generic-local/\"";
            $test = `$test`;
            if (index($test,"Authentication Token not found") == -1) {
                # most likely an artifactory api key
                $curl{"AVAILABLE"}  = 1;
                $curl{"TOKEN"}  = $use_token;
                $curl{"CURL"}   = "curl".$curl{"OPTIONS"}." -H \"X-JFrog-Art-Api:".$curl{"TOKEN"}."\"";
                $found_token        = 1;
            }
        if ($found_token) {
            my_print("Changed token due to command line parameter --artifactory-token");
        } else {
            my_print("Provided token is not a valid token, for --artifactory-token the argument must be either API or ACCESS or a valid token");
            exit_script($ERROR_CODE{"InvalidToken"});
        }
    }

    return ($curl{"CURL"} , $curl{"CURL_2"});
}



# Shorts:
# get_token(get_gic_server($<url>));
sub get_token {
    my $token_type = $_[0]; # type is one of: "UPLOAD" , "DOWNLOAD"
    my $gic_server;
    if (($token_type eq "UPLOAD") || ($token_type eq "DOWNLOAD")) {
        $gic_server = $server{"info"}{$token_type};
    } else {
        $gic_server = $token_type;
    }
    return $server{$gic_server}{"TOKEN"};
}

# Shorts:
# set_token(get_gic_server($<url>),$<token>);
sub set_token {
    my $gic_server  = $_[0];
    my $token       = $_[1];
    $server{$gic_server}{"TOKEN"} = $token;
}

sub get_gic_server {
    my $link = $_[0]; # url link from which to derive the server
    my $gic_server = "seli";
    foreach my $key (keys %server) {
        if ($key ne "info") {
            if (index($link,$server{$key}{"NAMING"}) != -1) {
                $gic_server = $key;
                last;
            }
        }
    }
    return $gic_server;
}

sub set_gic_info {
    my $gic_server = $_[0];
    my $info_type  = $_[1]; # UPLOAD , DOWNLOAD , BOTH , SWAP
    if ($info_type eq "SWAP") {
        my $temp = $server{"info"}{"DOWNLOAD"};
        $server{"info"}{"DOWNLOAD"} = $server{"info"}{"UPLOAD"};
        $server{"info"}{"UPLOAD"}   = $temp;
        return;
    }
    if (($info_type eq "DOWNLOAD") || ($info_type eq "BOTH")) {
        $server{"info"}{"DOWNLOAD"} = $gic_server;
    }
    if (($info_type eq "UPLOAD") || ($info_type eq "BOTH")) {
        $server{"info"}{"UPLOAD"} = $gic_server;
    }
}


# *****************************
# *                           *
# * print text, auto add \n   *
# * only print error lines if *
# * in silent mode            *
# *                           *
# *****************************
sub my_print {
    my $info_to_print    = $_[0];
    my $additional_info  = $_[1];  # Either an actual error code, a command or a color
    my $additional_info2 = $_[2];  # a color (example: "red")
    my $error_code;
    my $color_active;
    my $no_lf;
    my $debug_indent = 0;
    if (use_debug()) {
        $debug_indent = $GLOBAL_DEBUG{"INDENT"};
    }

    if ($color_info{"use_color"}) {
        print color("reset");
    }

    if (!$info_to_print) {
        $info_to_print = "\n";
    }

    if ($additional_info) {
        if (!$additional_info2) {
            if (isnum($additional_info)) {
                $error_code = $additional_info;
            } else {
                if ($additional_info eq "NOLF") {
                    $no_lf = 1;
                } else {
                    if ($color_info{"use_color"}) {
                        $color_active = 1;
                        print color($additional_info);
                    }
                }
            }
        } else {
            $error_code = $additional_info;
            if ($additional_info2 eq "NOLF") {
                $no_lf = 1;
            } else {
                if ($color_info{"use_color"}) {
                    $color_active = 1;
                    print color($additional_info2);
                }
            }
        }
    }
    if ((exists $GLOBAL_LOGGING{"logging_on"}) && ($GLOBAL_LOGGING{"logging_on"} == 1)) {
        my $log_line = $info_to_print;
        if (!$no_lf) {
            $log_line .= "\n";
        }
        $log_line = (' ' x (2*$debug_indent)).$log_line;
        $log_line = "               ".$log_line;
        write_log($log_line);
    }

    if (!$global_var{"SILENT-ON"}) {
        if (use_debug()) {
            $info_to_print = "               ".(' ' x (2*$debug_indent)).$info_to_print;
        }
        print $info_to_print;
        if (!$no_lf) {
            print "\n";
        }
    } elsif ($error_code) {
        $info_to_print = (' ' x (2*$debug_indent)).$info_to_print;
        print $info_to_print;
        if (!$no_lf) {
            print "\n";
        }
    }
    if ($color_active) {
        if ($color_info{"use_color"}) {
            print color("reset");
        }
    }
}

# *****************************
# *                           *
# * Debug information         *
# *                           *
# *****************************
sub debug_print {
    my ($package, $filename, $line) = caller;
    my $text        = $_[0];
    my $indent      = $_[1];

    # USE-LEVEL     LEVEL                   EFFECT
    # STANDARD      STANDARD /DETAILED      SHOW STANDARD DEBUG INFORMATION ONLY
    # DETAILED      STANDARD /DETAILED      SHOW ALL DEBUG INFORMATION
    my $show_this=0;
    if ($GLOBAL_DEBUG{"USE-LEVEL"} eq "DETAILED") {
        $show_this = 1;
    } elsif ($GLOBAL_DEBUG{"LEVEL"} eq "STANDARD") {
        $show_this = 1;
    }
    if ($show_this) {
        if (($indent eq $GLOBAL_DEBUG{"INDENT-DECREASE"}) && ($GLOBAL_DEBUG{"INDENT"} > 0)) {
            $GLOBAL_DEBUG{"INDENT"}--;
        }

        $text = ' ' x (2*$GLOBAL_DEBUG{"INDENT"}).$text;

        if ($GLOBAL_DEBUG{"USE-DEBUG"}) {
            if ($color_info{"use_color"}) {
                print color($color_info{"INF_Color"});
            }
            #print "DEBUG:";
            my $line_num = sprintf("%5s",$line);
            my_print("DEBUG: ($line_num) $text");
        }

        if ((exists $GLOBAL_LOGGING{"logging_on"}) && ($GLOBAL_LOGGING{"logging_on"} == 1)) {
            my $line_num = sprintf("%5s",$line);
            write_log("DEBUG: ($line_num) $text\n");
        }
        if (($indent eq $GLOBAL_DEBUG{"INDENT-INCREASE"})) {
            $GLOBAL_DEBUG{"INDENT"}++;
        }
    }
}

sub start_debug {
    my $lvl = $_[0];
    if ($lvl) {
        if (($lvl ne "STANDARD") && ($lvl ne "DETAILED")) {
            $lvl = "STANDARD";
        }
    } else {
        $lvl = "STANDARD";
    }
    my_print("DEBUG SWITCHED ON ".$GLOBAL_DEBUG{"USE-DEBUG"});
    if (!exists $GLOBAL_DEBUG{"USE-DEBUG"}) {
        $GLOBAL_DEBUG{"USE-DEBUG"}               = 1;
        if ($lvl) {
            $GLOBAL_DEBUG{"USE-LEVEL"}           = uc $lvl;     # USE-LEVEL: STANDARD/DETAILED (Changed by user/script by --debug-level or start_debug(<level>) )
        }
        $GLOBAL_DEBUG{"LEVEL"}                   = "STANDARD";  # LEVEL: STANDARD/DETAILED (Changed within script by assigning value a value to the variable)
        # USE-LEVEL     LEVEL                   EFFECT
        # STANDARD      STANDARD /DETAILED      SHOW STANDARD DEBUG INFORMATION ONLY
        # DETAILED      STANDARD /DETAILED      SHOW ALL DEBUG INFORMATION
        $GLOBAL_DEBUG{"INDENT-INCREASE"}         = "+";
        $GLOBAL_DEBUG{"INDENT-DECREASE"}         = "-";
        $GLOBAL_DEBUG{"INDENT"}                  = 0;
    } else {
        $GLOBAL_DEBUG{"USE-DEBUG"}               = 1;
    }
    return ($GLOBAL_DEBUG{"USE-DEBUG"});
}
sub use_debug {
    return ($GLOBAL_DEBUG{"USE-DEBUG"});
}
sub stop_debug {
    if (exists $GLOBAL_DEBUG{"USE-DEBUG"}) {
        $GLOBAL_DEBUG{"USE-DEBUG"}               = 0;
        my_print("DEBUG SWITCHED OFF ".$GLOBAL_DEBUG{"USE-DEBUG"});
    }
}


sub start_log {
    my $log_dir  = $_[0];
    my $log_name = $_[1];
    my $keeplog  = $_[2];
    if (substr($log_dir,-1) eq "/") {
        chop($log_dir);
    }
    my $file_handle;
    my $log = "$log_dir/$log_name";
    # Create log file
    open($file_handle, ">", $log) or die "File couldn't be opened";
    $GLOBAL_LOGGING{"logging_on"}   = 1;
    $GLOBAL_LOGGING{"keeplog"}      = $keeplog;
    $GLOBAL_LOGGING{"log_dir"}      = $log_dir;
    $GLOBAL_LOGGING{"log_filename"} = $log_name;
    $GLOBAL_LOGGING{"log_FN"}       = $log;
    $GLOBAL_LOGGING{"log_FH"}       = $file_handle;
    my_print("Started logging ".$GLOBAL_LOGGING{"log_FN"});
    my_print("   log_FN         : ".$GLOBAL_LOGGING{"log_FN"});
    my_print("   Keep log       : ".$GLOBAL_LOGGING{"keeplog"});
    my_print("   dir            : ".$GLOBAL_LOGGING{"log_dir"});
    my_print("   filename       : ".$GLOBAL_LOGGING{"log_filename"});

#    return ($file_handle,$log);
}

sub write_log {
    my $message = $_[0];

    if (exists $GLOBAL_LOGGING{"log_FH"}) {
        my $FH = {$GLOBAL_LOGGING{"file_handle"}};
        print {$GLOBAL_LOGGING{"log_FH"}} $message;
    }
}

sub close_log {
    my $exit_code = $_[0];
    if (!$exit_code) {
        $exit_code = 0;
    }
    if ((exists $GLOBAL_LOGGING{"logging_on"}) && ($GLOBAL_LOGGING{"logging_on"} == 1)) {
        my $result;
        if (exists $GLOBAL_LOGGING{"log_FH"}) {
            my_print("Close log file.");
            close $GLOBAL_LOGGING{"log_FH"} or "couldn't close";
        }
        my $log_date = strftime "%Y-%m-%d_%H:%M:%S", localtime;
        my $log_filename = $GLOBAL_LOGGING{"log_FN"};
        if ($exit_code != $ERROR_CODE{"NormalExit"}) {
            my $new_name = $GLOBAL_LOGGING{"log_dir"}."/download_csar_ERROR-"."$exit_code"."_"."$log_date".".log";
            $result=`mv $log_filename $new_name`;
            my_print("log file stored in $new_name");
        } else {
            if (!$GLOBAL_LOGGING{"keeplog"}) {
                $result=`rm -rf $log_filename`;
            } else {
                my $new_name = $GLOBAL_LOGGING{"log_dir"}."/download_csar_KEEP_"."$log_date".".log";
                $result=`mv $log_filename $new_name`;
                my_print("log file stored in $new_name");
            }
        }
    }
    $GLOBAL_LOGGING{"logging_on"}   = 0;
}

sub exit_script {
    my $exit_code = $_[0];

    my $total_exec_time_str = "download_csar execution";
    if ($global_var{"SYNC"}){
        $total_exec_time_str .= " (with synchronizing)";
    }

    foreach (keys %{$timer{"STORED"}}) {
        my_print($_,$color_info{"TIME_Color"});
    }
    $timer{"time_script_end"} = [gettimeofday()];

    print_elapsed_time({"start" => $timer{"time_script_start"},"end" => $timer{"time_script_end"},"reason" => $total_exec_time_str});
    if ($global_var{"DOWNLOADED-CSAR-TYPE"}) {
        if ($global_var{"LOCAL-CSAR-TYPE"}) {
            $global_var{"DOWNLOADED-CSAR-TYPE"} = $global_var{"LOCAL-CSAR-TYPE"}." due to ".$global_var{"UNSIGNED-TEXT"};
            $global_var{"LOCAL-CSAR-TYPE"} = "";
        }
        my_print("Downloaded CSAR package type : ".$global_var{"DOWNLOADED-CSAR-TYPE"});
    }
    if ($global_var{"LOCAL-CSAR-TYPE"}) {
        my_print("Local CSAR package type      : ".$global_var{"LOCAL-CSAR-TYPE"});
    }
    if ($global_var{"CSAR-NAME"}) {
        my_print("CSAR package name            : ".$global_var{"CSAR-NAME"});
    }
    debug_print("exit_script($exit_code)");
    close_log($exit_code);

    exit $exit_code;
}

sub load_change_notes {
    my $get_this    = $_[0];
    if (!(-e $script_name)) {
        debug_print("end load_change_notes() ".$ERROR_CODE{"Missing_file"},$GLOBAL_DEBUG{"INDENT-DECREASE"});
        return $ERROR_CODE{"Missing_file"};
    }
    my @notes;
    my $cwd = getcwd();
    my $file = "$cwd/$script_name";
    open my $handle, '<', $file;
    chomp(my @lines = <$handle>);
    close $handle;
    my $notes_started;
    foreach(@lines) {
        if (($get_this eq "VERSION") && (index($_,"# \$Revision: ") != -1)) {
            $script_version = substr($_,index($_,"# \$Revision: ")+length("# \$Revision: "));
            $script_version = substr($script_version,0,index($script_version,"\$"));
            return $script_version;
        }
        if (index($_,"CHANGE NOTES") != -1) {
            $notes_started = 1;
        } elsif ($notes_started) {
            if (substr($_,0,1) eq "#") {
                push(@notes,$_);
            } else {
                last;
            }
        }
    }
    @lines=();
    return @notes;
}

sub send_curl_command {
    my $command = $_[0];
    my $timeout = $_[1];
    my $use_second_token = $_[2];

    my $this_artifact_token;
    if (!$use_second_token) {
        $this_artifact_token = $curl{"CURL"};
    } else {
        $this_artifact_token = $curl{"CURL_2"};
    }
    debug_print("sub send_curl_command($command,$timeout)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    if (!$timeout) {
        $timeout = $global_var{"CURL-MAXTIME-DEFAULT"};
    }
    debug_print($curl{"CURL-PRINT"}." -m $timeout $command");
    my $returnvalue = `$this_artifact_token -m $timeout $command`;
    debug_print("sub send_curl_command() => $returnvalue",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $returnvalue;
}

# ********************************
# *                              *
# * Parse received string from   *
# * curl command to retrieve     *
# * directory content            *
# *                              *
# *                              *
# ********************************
sub parse_html_to_directory {
    my $string_to_parse   = $_[0];
    my $parsing_directory = $_[1];
    debug_print("sub parse_html_to_directory($string_to_parse)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    my @directory_content;

    debug_print("Comparing current string with previous parsed string");
    if ($previous_content) {
        if ($previous_content eq $string_to_parse) {
            debug_print("Skip scanning this string, it is the same as the previous one");
            return @directory_content;
        }
    }
    $previous_content = $string_to_parse;
    my @all_extensions = get_all_package_file_extensions(); # load all available extensions
    my @lines_in_string = split('\n',$string_to_parse);
    my $directory_entry;
    my $file_or_directory;  # 'D'irectory or 'F'ile
    my $markup_start = "<a href=\"";
    my $markup_title_start = "<title>Index of "; # '<title>Index of proj-5g-bsf-generic-local/eiffelesc/master/1.13.25+1578/release-artifacts</title>'
    my $markup_title_end   = "</title>";
    my $files_in_directory = 0;
    my $directories_in_directory = 0;
    my $tempVar;
    my $directory_name;

    foreach(@lines_in_string) {
        $tempVar = index($_,$markup_title_start);
        if ($tempVar != -1) {
            $tempVar += length($markup_title_start);
            my $end_of_entry = index($_,$markup_title_end);
            $directory_name = substr($_,$tempVar,($end_of_entry-$tempVar));
            my_print("Processing: $directory_name");
            if (!exists $site_entries{"INFO"}{"ROOT"}) {
                $site_entries{"INFO"}{"ROOT"} = substr($directory_name,rindex($directory_name,"/"));
            }
        }
        $tempVar = index($_,$markup_start);                                         # <a href="var.dscload-version">var.dscload-version</a>           20-Oct-2023 17:20  13 bytes
        if ($tempVar != -1) {
            $tempVar += length($markup_start);
            my $end_of_entry = index($_,"/\">");
            if ($end_of_entry == -1) {
                # End marker not found => this could be a file
                $end_of_entry = index($_,"\">");
            }
            $directory_entry = substr($_,$tempVar,($end_of_entry-$tempVar));        # var.dscload-version
            $directory_entry =~ s/%2C/,/g; # Replace all "%2C" with ","
            $directory_entry =~ s/%3D/=/g; # Replace all "%3D" with "="
            if (index($directory_entry,"..") == -1) {
                my $time;
                my $date;
                my $filesize=0;
                if (substr($_,-1) eq "-") {                                         # s  => file
                    $file_or_directory = "D";
                    $directories_in_directory++;
                } else {
                    $file_or_directory = "F";
                    $files_in_directory++;
                    # get file size from end of string currently being parsed
                    my @fields = split(' ',$_);                                     # [0]:<a [1]:href="var.dscload-version">var.dscload-version</a> [2]:20-Oct-2023 [3]:17:20 [4]:13 [5]:bytes
                    $time = $fields[-3];
                    $date = $fields[-4];
                    $fields[-2] =~ tr/,//d;
                    if (isnum($fields[-2])) {                                       # [-2] = [4] : 13
                        $filesize = $fields[-2];                                    # $filesize: 13
                        debug_print("Repo file size: $filesize $fields[-1]");
                        $filesize = number_of_bytes($fields[-2].$fields[-1]);
                        if (index($filesize,"bytes") != -1) {
                            $filesize = substr($filesize,0,index($filesize,"bytes"));
                        }
                        # At this point we have the actual number of bytes
                        debug_print("Actual nr of bytes on repo: $filesize");
                    }
                }
                my $key = "https://".$server{"seli"}{"NAMING"}."/artifactory/".$directory_name."/".$directory_entry;
                if (exists $site_entries{$key}{"TYPE"}) {
                    my_print("WARNING: Entry already exists for: ".$key,$color_info{"INF_Color"});
                }
                {
                    $site_entries{$key}{"TYPE"} = $file_or_directory;
                    if ($file_or_directory eq "F") {
                        my $file_extension = "Extension Not Found";
                        if (@all_extensions) {
                            foreach my $this_extension (@all_extensions) {
                                my $this_extensions_position = index($directory_entry,$this_extension);     # position of extension
                                if ($this_extensions_position != -1) {
                                    my $remaining_length = length($directory_entry)-length($this_extension);    # calculate filename length - extension length
                                    if ($this_extensions_position == $remaining_length) {
                                        $file_extension = $this_extension;
                                        last;
                                    }
                                }
                            }
                            if ($file_extension eq "Extension Not Found") {
#                  WARNING: probably missing declaration in %package_info for: base_stability_traffic/traffic_simulators/versions  TYPE FILE    var.dscload-version   Adding extension: .dscload-version
#                  WARNING: probably missing declaration in %package_info for: base_stability_traffic/traffic_simulators/versions  TYPE CHECKSUM var.dscload-version.sha256   Adding extension: .dscload-version.sha256
                                debug_print("\nExtension Not Found\n");
                                my $record_entry;
                                my $record_type;
                                if (index($directory_entry,".sha256") != -1) {  # probably not declared in the record at top of the script under variable %package_info
                                    my $this_extensions_position = index($directory_entry,".sha256");     # position of extension
                                    my $remaining_length = length($directory_entry)-length(".sha256");    # calculate filename length - extension length
                                    if ($this_extensions_position == $remaining_length) {
                                        $file_extension = substr($directory_entry,rindex($directory_entry,".",$this_extensions_position-1));
                                        $record_type = "CHECKSUM";
                                        $record_entry = substr( $directory_name, index($directory_name,$global_var{"PACKAGE-NAME"}) + length($global_var{"PACKAGE-NAME"}) + 1 );
                                        my $line = "WARNING: probably missing declaration in \%package_info for: ".$record_entry."  TYPE CHECKSUM ".$directory_entry."   Adding extension: ".$file_extension;
                                        push(@check_package_info,$line);
                                        my_print($line,$color_info{"INF_Color"});
                                    }
                                } elsif ( (rindex($directory_entry,".") != -1) ) {
                                    # var.sftp-version
                                    $file_extension = substr($directory_entry,rindex($directory_entry,"."));
                                    $record_type = "FILE";
                                    $record_entry = substr( $directory_name, index($directory_name,$global_var{"PACKAGE-NAME"}) + length($global_var{"PACKAGE-NAME"}) + 1 );
                                    my $line = "WARNING: probably missing declaration in \%package_info for: ".$record_entry."  TYPE FILE    ".$directory_entry."   Adding extension: ".$file_extension;
                                    push(@check_package_info,$line);
                                    my_print($line,$color_info{"INF_Color"});
                                }

                                if (!exists $package_info{$record_entry}) {
                                    $package_info{$record_entry}{"VALID-RELEASES"}                       = substr($global_var{"PACKAGE-NAME"},0,index($global_var{"PACKAGE-NAME"},"+"))."+"; # From this release onwards
                                    $package_info{$record_entry}{"VALID-RELEASE_TYPES"}                  = "SC;CNDSC";              # valid for SC and CNDSC
                                    $package_info{$record_entry}{"MANDATORY"}                            = 0;                       # 1: MANDATORY  0: OPTIONAL
                                    $package_info{$record_entry}{"COPY"}                                 = 1;                       # 1: COPY       0: DO NOT COPY
                                    $package_info{$record_entry}{"NR-ENTRIES"}                           = 1;
                                    $package_info{$record_entry}{"DIR-CONTENTS-1"}{"TYPE"}               = $record_type;            # FILE , CHECKSUM or DIR
                                    $package_info{$record_entry}{"DIR-CONTENTS-1"}{"EXTENSION"}          = $file_extension;
                                    $package_info{$record_entry}{"DIR-CONTENTS-1"}{"MANDATORY"}          = 1;
                                    #$package_info{$record_entry}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}      = "1B-".($filesize*2)."B"; # between 1B - ???B
                                    $package_info{$record_entry}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"}      = "1B-4GB"; # between 1B - ???B
                                    push(@add_to_package_info,$record_entry);
                                    #foreach my $record_key (keys $package_info{$record_entry}) {
                                    #    if (index($record_key,"DIR-CONTENTS") == -1) {
                                    #        push(@add_to_package_info,"   \$package_info{$record_entry}{$record_key}  = ".$package_info{$record_entry}{$record_key});
                                    #    } else {
                                    #        foreach my $content_key (keys $package_info{$record_entry}{$record_key}) {
                                    #            push(@add_to_package_info,"   \$package_info{$record_entry}{$record_key}{$content_key}  = ".$package_info{$record_entry}{$record_key}{$content_key});
                                    #        }
                                    #    }
                                    #}
                                } else {
                                    my $current_nr_entries = $package_info{$record_entry}{"NR-ENTRIES"};
                                    my $save_new_entry     = 1;
                                    while ($current_nr_entries) {
                                        if ($package_info{$record_entry}{"DIR-CONTENTS-".$current_nr_entries}{"EXTENSION"} eq $file_extension) {
                                            $save_new_entry = 0;
                                            last;
                                        }
                                        $current_nr_entries--;
                                    }
                                    if ($save_new_entry) {
                                        my $new_entry_nr = $package_info{$record_entry}{"NR-ENTRIES"}+1;
                                        $package_info{$record_entry}{"NR-ENTRIES"} = $new_entry_nr;
                                        $package_info{$record_entry}{"DIR-CONTENTS-".$new_entry_nr}{"TYPE"}               = $record_type;          # FILE , CHECKSUM or DIR
                                        $package_info{$record_entry}{"DIR-CONTENTS-".$new_entry_nr}{"EXTENSION"}          = $file_extension;
                                        $package_info{$record_entry}{"DIR-CONTENTS-".$new_entry_nr}{"MANDATORY"}          = 1;
                                        #$package_info{$record_entry}{"DIR-CONTENTS-".$new_entry_nr}{"EXPECTED-SIZE"}      = "1-".($filesize*2);    # between 1B - ???B
                                        $package_info{$record_entry}{"DIR-CONTENTS-".$new_entry_nr}{"EXPECTED-SIZE"}      = "1B-4GB";    # between 1B - ???B
                                    }
                                    #foreach my $record_key (keys $package_info{$record_entry}) {
                                    #    if (index($record_key,"DIR-CONTENTS") != -1) {
                                    #        foreach my $content_key (keys $package_info{$record_entry}{$record_key}) {
                                    #            push(@add_to_package_info,"   \$package_info{$record_entry}{$record_key}{$content_key}  = ".$package_info{$record_entry}{$record_key}{$content_key});
                                    #        }
                                    #    }
                                    #}
                                }
                            }
                        } else {
                            $file_extension = "Error Loading Array";
                            my_print("Could not load array \@all_extensions");
                        }
                        $site_entries{$key}{"NAME"} = $directory_entry;
                        if (exists $site_entries{"INFO"}{"ROOT"}) {
                            $site_entries{$key}{"DIRECTORY"} = substr($directory_name,index($directory_name,$site_entries{"INFO"}{"ROOT"})+1);
                        }
                        #$site_entries{$key}{"DIRECTORY"} = $directory_name;
                        $site_entries{$key}{"EXTENSION"} = $file_extension;
                        $site_entries{$key}{"SIZE"} = sprintf("%d",$filesize);
                        $site_entries{$key}{"TIME"} = $time;
                        $site_entries{$key}{"DATE"} = $date;
                    } else {
                        if (exists $site_entries{"INFO"}{"ROOT"}) {
                            $site_entries{$key}{"NAME"} = substr($directory_name,index($directory_name,$site_entries{"INFO"}{"ROOT"})+1);
                        }
                        #$site_entries{$key}{"NAME"} = $directory_name;
                    }
                    push(@directory_content,"$file_or_directory;$filesize;$directory_entry"); # F;13;var.dscload-version
                }
            }
        }
    }
    if ($directory_name) {
        $site_entries{"https://".$server{"seli"}{"NAMING"}."/artifactory/".$directory_name."/"}{"FILES"}       = $files_in_directory;
        $site_entries{"https://".$server{"seli"}{"NAMING"}."/artifactory/".$directory_name."/"}{"DIRECTORIES"} = $directories_in_directory;
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end parse_html_to_directory()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @directory_content;
}

# this sub should normally only be called by sub check_if_valid_release
sub check_if_valid_release_detail {
    my $this_release    = $_[0];
    my $valid_releases  = $_[1];
    $valid_releases =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
#print "detailed_check for \"$this_release\"  against: \"$valid_releases\"\n";
    # check if the provided release it a complete package indication or only the release
    # (check if for instance 1.8.0 or 1.8.0+180 was received and transform it into 1.8.0)
    if (index($this_release,$global_var{"PACKAGE-SEPERATOR"}) != -1) {
        $this_release = substr($this_release,0,index($this_release,$global_var{"PACKAGE-SEPERATOR"}));
    }
    my @detailed_this_release   = split('\.',$this_release);
    my @detailed_valid_release  = ();
    my $onwards_release         = 0;
    my $release_range           = 0;

    # Check the valid_releases
    if ((index($valid_releases,"+") == -1) && (index($valid_releases,"-") == -1)) {
        # only valid for one specific release
        if ($this_release eq $valid_releases) {
            return 1;
        }
        return 0;
    } else {
        if (index($valid_releases,"+") != -1) {
            $onwards_release = 1;
        }
        if (index($valid_releases,"-") != -1) {
            $release_range = 1;
        }
    }


    # start check if this is a valid release

    # There is only one range or one start release
    my $char = "+";
    my $count = () = $valid_releases =~ /\Q$char/g;

    if ($onwards_release) {
        $valid_releases =~ s/\+$//;
        if ( version->parse( $this_release ) >= version->parse( $valid_releases ) ) {
            return 1;
        } else {
            return 0;
        }
    } elsif (!$release_range) {
        # There is only one start release
        if ($count == 1) {
            $valid_releases =~ tr/\Q$char//d;
            if (index($valid_releases,"+") != -1) {
                $valid_releases = substr($valid_releases,0,index($valid_releases,"+"));
            }
            @detailed_valid_release    = split('\.',$valid_releases);
            my $cnt = @detailed_this_release;
            while ($cnt) {
                $cnt--;
                if ($detailed_this_release[$cnt] < $detailed_valid_release[$cnt]) {
                    return 0;
                }
            }
            return 1;
        }
        my_print("'Syntax' error in valid releases: $valid_releases (There are too many '+' signs");
        return 0;
    }

    # There is a release range, check if there is no start range (that would be an error)
    if ($count != 0) {
        my_print("'Syntax' error in valid releases: $valid_releases (There should not be a '+' sign when only a range is provided");
        return 0;
    }
    $char = "-";
    $count = () = $valid_releases =~ /\Q$char/g;
    if ($count != 1) {
        my_print("'Syntax' error in valid releases: $valid_releases (There are too many '-' signs");
        return 0;
    }

    my @release_range = split('-',$valid_releases);
    my $this_release_in_range = 0;
    $release_range[0] =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
    @detailed_valid_release    = split("\.",$release_range[0]);
    my $cnt = @detailed_this_release;
    while ($cnt) {
        $cnt--;
        if ($detailed_this_release[$cnt] < $detailed_valid_release[$cnt]) {
            return 0;
        }
    }

    $release_range[1] =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
    @detailed_valid_release    = split('\.',$release_range[1]);
    my $cnt = @detailed_this_release;
    while ($cnt) {
        $cnt--;
        if ($detailed_this_release[$cnt] > $detailed_valid_release[$cnt]) {
            return 0;
        }
    }
    return 1;
}

sub check_if_valid_release {
    my $this_release    = $_[0];
    my $valid_releases  = $_[1];

    # check if the provided release is a complete package indication or only the release
    # (check if for instance 1.8.0 or 1.8.0+180 was received and transform it into 1.8.0)
    if (index($this_release,"+") != -1) {
        my @detail_package = split('\+',$this_release);
        $this_release = $detail_package[0];
    }
    my @detailed_this_release   = split('\.',$this_release);
    my @detailed_valid_release  = ();
    my $onwards_release         = 0;
    my $release_range           = 0;
    my $additional_release      = 0;
    # Check the valid_releases
    if ((index($valid_releases,"+") == -1) && (index($valid_releases,"-") == -1) && (index($valid_releases,"&") == -1)) {
        # only valid for one specific release
        if ($this_release eq $valid_releases) {
            return 1;
        }
        return 0;
    } else {
        if (index($valid_releases,"+") != -1) {
            $onwards_release = 1;
        }
        if (index($valid_releases,"-") != -1) {
            $release_range = 1;
        }
        if (index($valid_releases,"&") != -1) {
            $additional_release = 1;
        }
    }


    # start check if this is a valid release
    if (!$additional_release) {
        return check_if_valid_release_detail($this_release , $valid_releases);
    } else {
        # There are additional releases... this is where the kaka hits the fan
        my @all_releases = split('&',$valid_releases);
        foreach (@all_releases) {
            if (check_if_valid_release_detail($this_release , $_)) {
                return 1;
            }
        }
    }
    return 0;
}

sub isnum {
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    debug_print("sub isnum($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    if ($_[0] eq  $_[0]+0) {
        debug_print("isnum($_[0]) = TRUE (1)",$GLOBAL_DEBUG{"INDENT-DECREASE"});
        return 1;
    }
    my $dec_point_at = index($_[0],".");
    if ($dec_point_at != -1) {
        # Since split on a . did not work for some reason...
        my $intpart = substr($_[0],0,$dec_point_at);
        my $floatpart= substr($_[0],$dec_point_at+1);
        if (($intpart eq $intpart+0) && ($floatpart eq $floatpart+0)) {
            debug_print("isnum($_[0]) = TRUE (1)",$GLOBAL_DEBUG{"INDENT-DECREASE"});
            return 1;
        } elsif (($intpart eq $intpart+0) && ($floatpart+0 == 0)) {
            debug_print("isnum($_[0]) = TRUE (1)",$GLOBAL_DEBUG{"INDENT-DECREASE"});
            return 1;
        }
    }
    debug_print("end isnum($_[0]) = FALSE (0)",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    return 0;
}

sub get_package_nr_entries {
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    debug_print("sub get_package_nr_entries($_[0],$_[1])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $key = $_[0];
    my $mandatory_and_optional = $_[1];
    my $number_of_entries;
    if ((exists($package_info{$key}{"NR-ENTRIES"})) && ($package_info{$key}{"NR-ENTRIES"} > 0)) {
        if ($mandatory_and_optional) {
            $number_of_entries = $package_info{$key}{"NR-ENTRIES"};
        } else {
            my $count_mandatory=0;
            my $content_index=1;
            if (exists($package_info{$key}{"DIR-CONTENTS-1"})) {
                $count_mandatory=0;
                while (exists($package_info{$key}{"DIR-CONTENTS-".$content_index})) {
                    if ($package_info{$key}{"DIR-CONTENTS-".$content_index}{"MANDATORY"}) {
                        $count_mandatory++;
                    }
                    $content_index++;
                }
            }
            $number_of_entries = $count_mandatory;
        }
    } elsif ((exists($package_info{$key}{"NR-SUBDIR-ENTRIES"})) && ($package_info{$key}{"NR-SUBDIR-ENTRIES"} > 0)) {
        if ($mandatory_and_optional) {
            $number_of_entries = $package_info{$key}{"NR-SUBDIR-ENTRIES"};
        } else {
            my $count_mandatory=0;
            my $content_index=1;
            if (exists($package_info{$key}{"DIR-CONTENTS-1"})) {
                $count_mandatory=0;
                while (exists($package_info{$key}{"DIR-CONTENTS-".$content_index})) {
                    if ($package_info{$key}{"DIR-CONTENTS-".$content_index}{"MANDATORY"}) {
                        $count_mandatory++;
                    }
                    $content_index++;
                }
            }
            $number_of_entries = $count_mandatory;
        }
    }
#    my $count=0;
#    if (exists($package_info{$key}{"DIR-CONTENTS-1"})) {
#        $count=1;
#        while (exists($package_info{$key}{"DIR-CONTENTS-".$count})) {
#            $count++;
#        }
#        $count--;
#    }
#    if ($count != $number_of_entries) {
#        my $str = "MISMATCH BETWEEN ACTUAL NUMBERS OF ENTRIES: $count AND ASSIGNED VALUE: $number_of_entries FOR $key";
#        my $str_line = "*" x length($str)+4;
#        my $str_fill = "*"." " x length($str)."*";
#        my_print("$str_line");
#        my_print("* $str *");
#        my_print("$str_line");
#    }
    debug_print("end get_package_nr_entries($_[0]) = $number_of_entries",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    return $number_of_entries;
}

sub get_mandatory_file_extensions {
    my $directory = $_[0];
    my @return_file_list;
    debug_print("sub get_mandatory_file_extensions($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    # check if this entry is valid for SC , CNDSC ,CNCS
    if (index($package_info{$directory}{"VALID-RELEASE_TYPES"}, $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}) != -1) {
        debug_print("$directory RELEASE-TYPE: ".$package_info{$directory}{"VALID-RELEASE_TYPES"}."  REPO_INFO: ".$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"});
        # How many entries are expected (entries can be either files or checksums)
        my $number_of_files_in_directory = get_package_nr_entries($directory);
        while ($number_of_files_in_directory) {
            if ($package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"MANDATORY"}) {
                push(@return_file_list,$package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"EXTENSION"});
            }
            $number_of_files_in_directory--;
        }
    }
    foreach (@return_file_list) {
        debug_print("   Mandatory : $_");
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("sub get_mandatory_file_extensions()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @return_file_list;
}

sub get_sha_file_extensions {
    my %return_file_list;
    debug_print("sub get_sha_file_extensions()",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    foreach my $directory (keys %package_info) {
        debug_print(" - checking: $directory");
        my $number_of_files_in_directory = get_package_nr_entries($directory);
        while ($number_of_files_in_directory) {
            if (($package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"TYPE"} eq "CHECKSUM") &&
                (index($package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"EXTENSION"},".sha256") != -1)) {
                my $this_extension      = $package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"EXTENSION"};
                my $end_of_sha          = index($this_extension,".sha256");
                my $rebuild_extension   = substr($this_extension , 0 , $end_of_sha);
                $return_file_list{$directory}{"DIR-CONTENTS"} = "DIR-CONTENTS-".$number_of_files_in_directory;
                $return_file_list{$directory}{"EXTENSION"}    = $rebuild_extension;
                debug_print(" - directory contains sha256 reference : ".$return_file_list{$directory}{"EXTENSION"});
            }
            $number_of_files_in_directory--;
        }
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("sub get_sha_file_extensions()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return %return_file_list;
}

sub get_package_file_extensions {
    my $directory = $_[0];
    my @return_file_list;

    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    debug_print("sub get_package_file_extensions($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $number_of_files_in_directory = get_package_nr_entries($directory,1);
    while ($number_of_files_in_directory) {
        my $extension = $package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"EXTENSION"};
        push(@return_file_list,$extension);
        debug_print(" - Added : $extension");
        $number_of_files_in_directory--;
    }
    debug_print("return get_package_file_extensions()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    return @return_file_list;
}

sub get_all_package_file_extensions {
    my @return_file_list;

    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    debug_print("sub get_all_package_file_extensions()",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    if (!@all_package_file_extensions) {
        foreach my $key (keys %package_info) {
            my @tmp_list = get_package_file_extensions($key);
            if (@tmp_list) {
                foreach my $received (@tmp_list) {
                    my $already_present = 0;
                    foreach my $return (@return_file_list) {
                        if ($received eq $return) {
                            $already_present = 1;
                            last;
                        }
                    }
                    if (!$already_present) {
                        push(@return_file_list, @tmp_list);
                    }
                }
                #push(@return_file_list, @tmp_list);
            }
        }
        if (use_debug()) {
            debug_print(" - Storing list of extensions");
            foreach (@return_file_list) {
                debug_print("    $_");
            }
        }
        @all_package_file_extensions = @return_file_list;
    } else {
        debug_print(" - Using previously loaded list of extensions");
        @return_file_list = @all_package_file_extensions;
    }

    debug_print("return get_all_package_file_extensions()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    return @return_file_list;
}


sub get_package_type {
    my $directory   = $_[0];
    my $type        = $_[1];
    my @return_file_list;
    debug_print("sub get_package_type($_[0] , $_[1])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $number_of_files_in_directory = get_package_nr_entries($directory);
    while ($number_of_files_in_directory) {
        if ($package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"TYPE"} eq $type) {
            push(@return_file_list,$package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"EXTENSION"});
        }
        $number_of_files_in_directory--;
    }
    debug_print("sub get_package_type()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @return_file_list;
}

sub get_all_package_type {
    my $type        = $_[0];
    my @return_file_list;
    debug_print("sub get_all_package_type($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    foreach my $key (keys %package_info) {
        my @tmp_list = get_package_type($key,$type);
        push(@return_file_list, @tmp_list);;
    }
    debug_print("sub get_all_package_type()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @return_file_list;
}

sub set_package_mandatory {
    my $directory   = $_[0];
    my $value       = $_[1];
    debug_print("sub set_package_mandatory($_[0] , $_[1])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $number_of_files_in_directory = get_package_nr_entries($directory);
    while ($number_of_files_in_directory) {
        $package_info{$directory}{"DIR-CONTENTS-".$number_of_files_in_directory}{"MANDATORY"} = $value;
        $number_of_files_in_directory--;
    }
    debug_print("sub set_package_mandatory()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
}

sub set_all_package_mandatory {
    my $value     = $_[0];
    debug_print("sub set_all_package_mandatory($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    foreach my $key (keys %package_info) {
        set_package_mandatory($key,$value);
    }
    debug_print("sub set_all_package_mandatory()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
}

sub bytes_to_text {
    my $bytes   = $_[0];

    my $scale = 0;
    my @scales = ("B", "KB", "MB", "GB", "TB", "PB");
    while ($bytes >= 1024) {
        $bytes = $bytes / 1024;
        $scale++;
    }
    $bytes = sprintf("%7.2f %-2s",$bytes,$scales[$scale]);
    return $bytes;
}

sub number_of_bytes {
    my $number = $_[0];
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    debug_print("sub number_of_bytes($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $multiplier = 1024;

    if (substr($number,-1) eq "B") {
        $number = substr($number,0,index($number,"B"));
    }
    my $return = $number;
    if (index($number,"G") != -1) {
        $return = $multiplier**3 * substr($number,0,index($number,"GB"));
    }
    if (index($number,"M") != -1) {
        $return = $multiplier**2 * substr($number,0,index($number,"MB"));
    }
    if (index($number,"K") != -1) {
        $return = $multiplier * substr($number,0,index($number,"KB"));
    }
    debug_print("end number_of_bytes Return= $return Bytes",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    return $return;
}

sub get_indicator {
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    debug_print("sub get_indicator($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $return = substr($_[0],0,1);
    debug_print("end get_indicator() = $return",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    return $return;
}

sub get_filesize {
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    debug_print("sub get_filesize($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my @splitted = split(";",$_[0]);
    debug_print("end get_filesize() = $splitted[1]",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    return $splitted[1];
}

sub get_filename {
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    debug_print("sub get_filename($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my @splitted = split(";",$_[0]);
    debug_print("end get_filename() = $splitted[2]",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    return $splitted[2];
}

sub get_remote_path {
    debug_print("sub get_remote_path($_[0])",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    my @splitted = split("/",get_filename($_[0]));
    my $len = @splitted;
    my $return_value;
    foreach(@splitted) {
        $return_value .= "$_/";
        $len--;
        if ($len <= 1) { last; }
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end get_remote_path() = $return_value",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $return_value;
}


# *****************************
# *                           *
# * load directory structure  *
# * from artifactory          *
# *                           *
# *****************************
sub read_directory_recursive {
#    my $directory_name      = $_[0];
#    my $adjust_first_part   = $_[1];
#    my $no_recursive_read   = $_[2];
    my %parameters          = %{$_[0]};
    my $directory_name      = (exists $parameters{"DIRECTORY"}     ? $parameters{"DIRECTORY"}      : "");
    my $adjust_first_part   = (exists $parameters{"ADJUST"}        ? $parameters{"ADJUST"}         : "");
    my $no_recursive_read   = (exists $parameters{"NO-RECURSIVE"}  ? $parameters{"NO-RECURSIVE"}   : "");

    debug_print("sub read_directory_recursive($directory_name , $adjust_first_part , $no_recursive_read)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $https_position;
    my $https_position2;
    $https_position  = index($directory_name,"https");
    # if there is a second https in the name then store this for later use
    $https_position2 = index($directory_name,"https",$https_position+5);
    debug_print("https position 1 : ".$https_position);
    debug_print("https position 2 : ".$https_position2);
    debug_print("wait for reply on curl -m 600 -s $directory_name");
    my $curl_output;
    if ($https_position2 == -1) {
        $curl_output = send_curl_command('-s '.$directory_name,$global_var{"CURL-MAXTIME-DEFAULT"});
    }
#    my $curl_output=`curl -s $directory_name`;
    if ((index($curl_output,"status") != -1) && (index($curl_output,"status\" : 28") != -1) && (($https_position2 == -1))) {
        my_print("\nTimeout for CURL $directory_name",$ERROR_CODE{"Timeout"},$color_info{"ERR_Color"});
    }
    if ((index($curl_output,"status") != -1) && (index($curl_output,"status\" : 401") != -1) && (($https_position2 == -1))) {
        my_print("\nNOT ALLOWED TO ACCESS $directory_name",$ERROR_CODE{"Timeout"},$color_info{"ERR_Color"});
        exit_script($ERROR_CODE{"PackageNotFound"});
    }
    if ((index($curl_output,"status") != -1) && (index($curl_output,"status\" : 404") != -1) && (($https_position2 == -1))) {
        my_print("\nCOULD NOT FIND $directory_name",$ERROR_CODE{"404"},$color_info{"ERR_Color"});
    } elsif ((index($curl_output,"status") != -1) && (($https_position2 == -1))) {
        my_print("\nProblem accessing: $directory_name\n$curl_output",$ERROR_CODE{"Timeout"},$color_info{"ERR_Color"});
    }

    debug_print("received curl output");
    my $tempVar;
    my $tempVar2;
    my $tempVar3;

    my $start_directory;
    my @entries = parse_html_to_directory($curl_output,$directory_name);
    my $number_of_entries;                       # needed later to adjust the first entries to include the complete link
#    if ($https_position2 != -1) {
#        @entries = parse_html_to_directory($curl_output,$directory_name);
#    }
    $number_of_entries = @entries;
    if (!$no_recursive_read) {
        foreach my $local_entry (@entries) {
            if (get_indicator($local_entry) eq "D") {       # Check if another directory
                my $sub_dir_name = get_filename($local_entry);
                my %recurive_parameters;
                $recurive_parameters{"DIRECTORY"}  = "$directory_name$sub_dir_name/";
                $recurive_parameters{"SUCCESSIVE"} = 1;
                my @recursive_entries = read_directory_recursive(\%recurive_parameters);  # /start/lev1/lev2/lev3/file1
                foreach my $recursive_entry (@recursive_entries) {
                    if (index($recursive_entry,"https://") == -1) {
                        $tempVar  = get_indicator($recursive_entry);
                        $tempVar2 = get_filename($recursive_entry);
                        $tempVar3 = get_filesize($recursive_entry);
                        $tempVar  = "$tempVar;$tempVar3;$directory_name$sub_dir_name/$tempVar2";
                    } else {
                        $tempVar = $recursive_entry;
                    }
                    push(@entries,$tempVar);
                }
            }
        }
    }
    if ($adjust_first_part) {
        for (my $i=0; $i<$number_of_entries; $i++) {
            if (index($entries[$i],"https://") == -1) {
                $tempVar  = get_indicator($entries[$i]);
                $tempVar2 = get_filename($entries[$i]);
                $tempVar3 = get_filesize($entries[$i]);
                $entries[$i]  = "$tempVar;$tempVar3;$directory_name$tempVar2";
            }
        }
    }
    debug_print("end read_directory_recursive() = @entries",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @entries;
}

sub read_directory {
    my %parameters          = %{$_[0]};
    my $directory_name      = (exists $parameters{"DIRECTORY"}     ? $parameters{"DIRECTORY"}      : "Not Available");
    my $no_recursive_read   = (exists $parameters{"NO-RECURSIVE"}  ? $parameters{"NO-RECURSIVE"}   : "");

    debug_print("sub read_directory($directory_name , $no_recursive_read)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    my @directory_content;

        if (keys %site_entries) {
            debug_print("Overwriting previously directory content of \%site_entries");
        }
        # clear existing hash
        %site_entries = ();

        @directory_content = read_directory_recursive(\%parameters);
        my $count_dirs=0; my $count_files=0;
        foreach my $key (keys %site_entries) {
            if ($site_entries{$key}{"TYPE"} eq "D") {
                $count_dirs++;
            } else {
                $count_files++;
            }
        }
        $site_entries{"INFO"}{"CONTENT-OF"}     = substr($directory_name,length($parameters{"SITE"}));
        $site_entries{"INFO"}{"SITE"}           = substr($parameters{"SITE"},length("https://".$server{"seli"}{"NAMING"}."/artifactory/"));
        $site_entries{"INFO"}{"SITE"}           = substr($parameters{"SITE"},0,index($site_entries{"INFO"}{"SITE"},"/"));
        $site_entries{"INFO"}{"NAMESPACE"}      = $parameters{"NAMESPACE"};
        $site_entries{"INFO"}{"NO-RECURSIVE"}   = $no_recursive_read;
        $site_entries{"INFO"}{"DIRECTORIES"}    = $count_dirs;
        $site_entries{"INFO"}{"FILES"}          = $count_files;
        @stored_directory_content = @directory_content;
    foreach my $key (keys %site_entries) {
        if ($key ne "INFO") {
            debug_print("stored key      : ".$key);
        } else {
            debug_print("   CONTENT-OF   : ".$site_entries{$key}{"CONTENT-OF"});
            debug_print("   SITE         : ".$site_entries{$key}{"SITE"});
            debug_print("   NAMESPACE    : ".$site_entries{$key}{"NAMESPACE"});
            debug_print("   NO-RECURSIVE : ".$site_entries{$key}{"NO-RECURSIVE"});
            debug_print("   DIRECTORIES  : ".$site_entries{$key}{"DIRECTORIES"});
            debug_print("   FILES        : ".$site_entries{$key}{"FILES"});
        }
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end read_directory()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @directory_content;
}


sub find_package_fast {
    my $link            = $_[0];
    my $package_to_find = $_[1];
    debug_print("sub find_package_fast($link , $package_to_find)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    debug_print("USE-FAST-FIND = ".$global_var{"USE-FAST-FIND"});
    my $path;
    if ($global_var{"USE-FAST-FIND"}) {
        my_print("using api search to find package: $package_to_find");
        my $repo = substr($link,index($link,"proj-")); # proj-5g-bsf-generic-local/eiffelesc    # change this wiqwork
        $repo = substr($repo,0,index($repo,"/"));      # proj-5g-bsf-generic-local

#        # Standard/default repo
#        my $repo = $repo_info{$repo_info{"GENERIC"}{"DEFAULT_ARTIFACTORY_TYPE"}}{"REPOSITORY"};
#        if (substr($repo,-1) eq "/") {
#            chop($repo);
#        }

        # other repos
        my @repos;
        push(@repos,$repo);
        foreach my $key (keys %repo_info) {
            if (exists $repo_info{$key}{"REPOSITORY"}) {
                my $this_repo = $repo_info{$key}{"REPOSITORY"};
                if (substr($this_repo,-1) eq "/") {
                    chop($this_repo);
                }

                # check if repo exists, if not then add it to the list
                my $add_this;
                foreach my $check_repo (@repos) {
                    if (substr($check_repo,-1) eq "/") {
                        chop($check_repo);
                    }
                    if ($check_repo eq $this_repo) {
                        $add_this = "";
                        last;
                    }
                    $add_this = $this_repo;
                }
                if ($add_this) {
                    push(@repos,$add_this);
                }
            }
        }

        my $find        = "\"path\" : \"";
        my $find_user   = "\"created_by\" : \"";
        my $find_user2  = "\"modified_by\" : \"";
        my $find_date   = "\"created\" : \"";
        my $find_date2  = "\"modified\" : \"";
        my $find_date3  = "\"updated\" : \"";
        my $search_url  = "https://armdocker.rnd.ericsson.se/artifactory/api/search/aql";
        my $content_type= "-H \"content-type:text/plain\"";
        my $output;

        foreach my $repo (@repos) {
            my $try_closest_fit;
            my $try_first_fit;
            my_print("Checking repo $repo");
            $output = send_curl_command("-s $search_url".' -d \'items.find({"repo":{"$eq":"'.$repo.'"}},{"type":{"$eq":"folder"}},{"name":{"$match":"'.$package_to_find.'"}})\' '.$content_type , $global_var{"CURL-MAXTIME-DEFAULT"});
            if (index($output,$find) != -1) {
                if (index($output,"range") != -1) {
                    my $total_entries = substr( $output , index( $output ,"total" ,index($output,"range") ) );
                    $total_entries = substr( $total_entries , index($total_entries,":")+2 );
                    $total_entries = substr( $total_entries , 0 , index($total_entries,"\n") );
                    if ($total_entries > 1) {
                        my_print("Multiple occurances of the package found: $total_entries\n",$ERROR_CODE{"PackageNotFound"},$color_info{"RED_Color"});
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
                            my_print("   $found_path");
                            my_print(sprintf("      Created by: %-12s   Modified by: %-12s   Created: $found_date   Modified: $found_date2   Updated: $found_date3\n",$found_user,$found_user2));
                            if ((!$global_var{"AUTO-SITE"}) && (index($found_path,$link) != -1)) {
                                if (!$try_closest_fit) {
                                    $try_closest_fit = $found_path;
                                }
                            }
                            if (!$try_first_fit) {
                                $try_first_fit = $found_path;
                            }
                            $start_ = index($output,$find,($start_ + length($find)));
                        }
                        if (!$try_closest_fit) {
                            #my_print("Either adapt argument -l/--link or remove incorrect package from artifactory");
                            #exit_script($ERROR_CODE{"PackageNotFound"});
                            my_print("Trying with first entry: $try_first_fit",$ERROR_CODE{"PackageNotFound"},$color_info{"RED_Color"});
                        } else {
                            my_print("Trying with closest fit: $try_closest_fit",$ERROR_CODE{"PackageNotFound"},$color_info{"RED_Color"});
                        }
                    }
                }
                if ((!$try_closest_fit) && (!$try_first_fit)) {
                    $path = substr($output,index($output,$find)+length($find));
                    $path = substr($path,0,index($path,"\","));
                    $path = "https://".$server{"seli"}{"NAMING"}."/artifactory/$repo/".$path."/$package_to_find/";
                } else {
                    if ($try_closest_fit) {
                        $path = $try_closest_fit;
                    } else {
                        $path = $try_first_fit;
                    }
                }
                my_print("Package found at: $path");
                last;
#        } else {
#            my_print("PACKAGE NOT FOUND using api search");
#            my_print($output);
            }
        }
        if (!$path) {
            my_print("PACKAGE NOT FOUND using api search");
            my_print($output);
        }
    }
    determine_repo_to_use($path);
    debug_print("sub find_package_fast() => $path",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $path;
}


# *****************************
# *                           *
# * Find package on site      *
# *                           *
# *****************************
sub find_package {
    my $package_to_find        = $_[0];
    my @dir_to_search          = @{$_[1]};
    my @package_breakdown = split('\+',$package_to_find);
    debug_print("sub find_package($package_to_find , $dir_to_search[0]...)",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    my_print("searching for $package_to_find");

    my @keys_found;
    my $dir_count=0;
    my $fil_count=0;
    my $nr_matching_files = 0;
    my @package_found;  # D
    my $dir_cnt=0;      # D
    my $fil_cnt=0;      # D
    my $missing_file=0;
    my $position;
    my $new_package_name="N";
    my $is_directory=0;

    foreach my $key (keys %site_entries) {
        debug_print("Check if $key contains $package_to_find");
        $position = index($key,$package_to_find);
        if ($position != -1) {
            my $copy_me = 0;
            if ($download_all) {
                $copy_me = 1;
            } else {
                foreach my $download_this (keys %package_info) {
                    if ((index($key,$download_this) != -1) &&
                        (check_if_valid_release($package_breakdown[0] , $package_info{$download_this}{"VALID-RELEASES"})) &&
                        (($package_info{$download_this}{"COPY"}) || ($package_info{$download_this}{"MANDATORY"})) ) {
                        $copy_me = 1;
                        last;
                    }
                }
            }
            if ($copy_me) {
                $site_entries{$key}{"MARKED"} = 1;
                if ($site_entries{$key}{"TYPE"} eq "D") {
                    if (substr($key,$position+length($package_to_find),1) eq "/") {
                        # Exact directory match, packagename found
                        $is_directory=1;
                    }
                    $dir_count++;
                } else {
                    $fil_count++;
                }
                debug_print("Added $key to list of downloads");
            }
        } else {
            debug_print("$key does not contain $package_to_find\n  position = $position");
        }
    }
    foreach my $cur_dir (@dir_to_search) {
        debug_print("Check if $cur_dir contains $package_to_find");
        $position = index($cur_dir,$package_to_find);
        if ($position != -1) {
            my $copy_me = 0;
            if ($download_all) {
                $copy_me = 1;
            } else {
                foreach my $download_this (keys %package_info) {
                    if ((index($cur_dir,$download_this) != -1) &&
                        (check_if_valid_release($package_breakdown[0] , $package_info{$download_this}{"VALID-RELEASES"})) &&
                        (($package_info{$download_this}{"COPY"}) || ($package_info{$download_this}{"MANDATORY"})) ) {
                        $copy_me = 1;
                        last;
                    }
                }
            }
            if ($copy_me) {
                push(@package_found,$cur_dir);
                if (get_indicator($cur_dir) eq "D") {
                    if (substr($cur_dir,$position+length($package_to_find),1) eq "/") {
                        # Exact directory match, packagename found
                        $is_directory=1;
                    }
                    $dir_cnt++;
                } else {
                    $fil_cnt++;
                }
                debug_print("Added $cur_dir to list of downloads");
            }
        } else {
            debug_print("$cur_dir does not contain $package_to_find\n  position = $position");
        }
    }

    if ($is_directory) {
        # scan the package and remove all entries that are not an exact match for the directory
        my @updated_list;
        debug_print("Only keep following files:");
        foreach(@package_found) {
            my $prev_char = substr($_,index($_,$package_to_find)-1,1);
            my $next_char = substr($_,index($_,$package_to_find)+length($package_to_find),1);

            if (($prev_char eq "/") && ($next_char eq "/")) {
                # keep this entry
                push(@updated_list,$_);
                debug_print("   $_");
            }
        }
        @package_found = @updated_list;
    }

    if (!$fil_count) {
        my_print("WARNING: Package $package_to_find NOT found",$ERROR_CODE{"PackageNotFound"},$color_info{"ERR_Color"});
        exit_script($ERROR_CODE{"PackageNotFound"});
    }
    if (!$fil_cnt) {
        my_print("WARNING: Package $package_to_find NOT found",$ERROR_CODE{"PackageNotFound"},$color_info{"ERR_Color"});
        exit_script($ERROR_CODE{"PackageNotFound"});
    }

    if (!$dir_cnt) {
        # No directories found, so probably packagename was a version number
        # retrieve directory information from files that need to be downloaded
        my @possible_directories;

        debug_print("No directories found, trying to deduce directories");

        foreach my $scan_all (@package_found) {
            my @breakdown = split('/',$scan_all);
            my $elements = @breakdown;
            my $existing_path = 0;
            my $build_dir;
            for(my $i=0; $i<$elements-1; $i++) {
                if ($breakdown[$i]) {
                    if ($build_dir) {
                        $build_dir = "$build_dir/$breakdown[$i]";
                    } else {
                        $build_dir = "Dhttps:/";
                    }
                }
            }
            if ($build_dir) {
                my $entry_exists;
                foreach (@possible_directories) {
                    if ($_ eq $build_dir) {
                        $entry_exists = 1;
                        last;
                    }
                }
                if (!$entry_exists) {
                    push(@possible_directories,$build_dir);
                    debug_print("Marked as possible directory: $build_dir");
                    if ($package_to_find ne $breakdown[-3]) {
                        debug_print("Packagename change needed old= $package_to_find  new= $breakdown[-3]");
                        $new_package_name = $breakdown[-3];
                    }
                }
            }
        }

        foreach (@possible_directories) {
            debug_print("Adding $_ to package files");
            push(@package_found,$_);
        }

    }

    if (!$global_var{"SKIP-CHECKS"}) {
        my @hash_keys = keys %site_entries;
        foreach my $key (keys %package_info) {
            if (($package_info{$key}{"COPY"}) && (check_if_valid_release($package_breakdown[0] , $package_info{$key}{"VALID-RELEASES"}))) {
#                if (($repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"} eq "SC") || (index($package_info{$key}{"VALID-RELEASE_TYPES"},$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}) != -1)) {
                if (index($package_info{$key}{"VALID-RELEASE_TYPES"},$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}) != -1) {
                    if ($package_info{$key}{"MANDATORY"}) {
                        my $count = get_package_nr_entries($key);
                        while ($count) {
                            if ($package_info{$key}{"DIR-CONTENTS-".$count}{"MANDATORY"}) {
                                my $expect_file = $package_info{$key}{"DIR-CONTENTS-".$count}{"EXTENSION"};
                                debug_print("Expected: ".$expect_file);
                                my $found_hash_file;
                                foreach my $package_entry (@hash_keys) {
                                    if (index($package_entry,$key) != -1) {
                                        my $str_position = index($package_entry,$expect_file);
                                        my $rest_str = substr($package_entry,$str_position+length($expect_file));

                                        if (($str_position != -1) && (index($rest_str,".") == -1)) {
                                            $found_hash_file = 1;
                                            $nr_matching_files++;
                                            debug_print("HASH===> FOUND : $expect_file (".$site_entries{$package_entry}{"NAME"}.") in ".$site_entries{$package_entry}{"DIRECTORY"});
                                        }
                                    }
                                }
                                my $found_file;
                                foreach my $package_entry (@package_found) {
                                    if (index($package_entry,$expect_file) != -1) {
                                        $found_file = 1;
                                        debug_print("===> FOUND : $expect_file");
                                        last;
                                    }
                                }
                                if (!$found_hash_file) {
                                    my_print("HASH===> NOT FOUND: $expect_file for MANDATORY directory $key");
                                    #$missing_file++;
                                }
                                if (!$found_file) {
                                    my_print("===> NOT FOUND: $expect_file for MANDATORY directory $key");
                                    $missing_file++;
                                }

                            }
                            $count--;
                        }
                    } else {
                        my $count = get_package_nr_entries($key);
                        while ($count) {
                            if ($package_info{$key}{"DIR-CONTENTS-".$count}{"MANDATORY"}) {
                                my $expect_file = $package_info{$key}{"DIR-CONTENTS-".$count}{"EXTENSION"};
                                debug_print("Optional directory, expects: ".$expect_file);
                                my $found_hash_file;
                                foreach my $package_entry (@hash_keys) {
                                    if (index($package_entry,$key) != -1) {
                                        my $str_position = index($package_entry,$expect_file);
                                        my $rest_str = substr($package_entry,$str_position+length($expect_file));

                                        if (($str_position != -1) && (index($rest_str,".") == -1)) {
                                            $found_hash_file = 1;
                                            $nr_matching_files++;
                                            debug_print("HASH===> FOUND : $expect_file (".$site_entries{$package_entry}{"NAME"}.") in ".$site_entries{$package_entry}{"DIRECTORY"});
                                        }
                                    }
                                }
                                my $found_file;
                                foreach my $package_entry (@package_found) {
                                    if (index($package_entry,$expect_file) != -1) {
                                        $found_file = 1;
                                        debug_print("===> FOUND : $expect_file");
                                        last;
                                    }
                                }
                                if (!$found_hash_file) {
                                    if (!exists($package_info{$key}{"NR-SUBDIR-ENTRIES"})) {
                                        my_print("HASH===> NOT FOUND: $expect_file for OPTIONAL directory $key");
                                    }
                                }
                                if (!$found_file) {
                                    if (!exists($package_info{$key}{"NR-SUBDIR-ENTRIES"})) {
                                        my_print("===> NOT FOUND: $expect_file for OPTIONAL directory $key");
                                    }
                                }

                            }
                            $count--;
                        }
                    }
                }
            }
        }
        if ($missing_file) {
            my_print("Fix missing files",$ERROR_CODE{"Missing_file"},$color_info{"ERR_Color"});
            exit_script($ERROR_CODE{"Missing_file"});
        } else {
            my_print("All files present at download site");
        }
    } else {
        my_print("Skipping checks due to \$global_var{\"SKIP-CHECKS\"} = ".$global_var{"SKIP-CHECKS"});
    }
#    debug_print("end find_package() = $nr_matching_files , $new_package_name , @package_found",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    debug_print("end find_package() = $nr_matching_files , $new_package_name , \@package_found",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    debug_print("Number of entries in \@package_found: ".scalar(@package_found));
    return ($nr_matching_files,$new_package_name, @package_found);
    # return $nr_matching_files;
}

sub adjust_dir {
    my $dir_name   = $_[0];
    my $dir_new    = $_[1];
    debug_print("sub adjust_dir($dir_name,$dir_new)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    my $o_dir_name = $dir_name."/".$dir_new;
    my $return_name = $dir_name;

    my $check_first = substr($dir_name,0,2);
    if (($check_first eq "./") || ($dir_name eq ".")) {
        debug_print("Adjust for current directory (./)");
        $check_first = getcwd();
        if ($dir_name eq ".") {
            $return_name = $check_first;
        } else {
            $dir_name = substr($dir_name,2);
            $return_name = "$check_first/$dir_name";
        }
    } elsif ($check_first eq "..") {
        debug_print("Adjust for previous directory (../)");
        my @splitted = split('/',$dir_name);
        my $counter = 0;
        my $new_dir_name;
        foreach (@splitted) {
            if ($_ eq "..") {
                $counter--;
            } else {
                $new_dir_name .= "/$_";
            }
        }
        if ($counter != 0) {
            my $directory = getcwd();
            my @splitted_dir = split('/',$directory);
            my $len_splitted_dir = @splitted_dir;
            if ($splitted_dir[0] eq "") {
                $len_splitted_dir--;
            }
            debug_print("Current directory                      : $directory");
            debug_print("Number of up (../)                     : ".abs($counter));
            debug_print("Number of directories in current path  : $len_splitted_dir");
            if ((abs($counter)) > $len_splitted_dir) {
                my_print("ERROR: Target path not specified correct",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                my_print("Incorrect target: $dir_name");
                my_print("Current path    : $directory");
                exit_script($ERROR_CODE{"Standard"});
            }
            my $dir_length = index($directory,$splitted_dir[$counter]);
            $return_name = substr($directory,0,$dir_length).$new_dir_name;
        }
    } elsif ($check_first eq "~/") {
        debug_print("Adjust for home directory (~/)");
        $return_name = $ENV{"HOME"}.substr($dir_name,2);
    } elsif (substr($dir_name,0,1) ne "/") {
        debug_print("Adjust to store in subdirectory: $dir_name");
        $return_name = getcwd()."/".$dir_name;
    }
    if ($return_name ne $o_dir_name) {
        debug_print("adjusted dir    : $return_name/$dir_new");
        debug_print("adjusted target : $return_name");
        $global_var{"TARGET-DIRECTORY"} = $return_name;
    }

    chdir $o_dir_name;
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end adjust_dir() : $return_name/$dir_new",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $return_name."/".$dir_new;
}

# *****************************
# *                           *
# * Create directory          *
# *                           *
# *****************************
sub create_dir {
    my $target = $_[0];
    my $newdir = $_[1];
#    my $dir_to_create = $_[0];
#    debug_print("create_dir($dir_to_create)");
    debug_print("sub create_dir($target,$newdir)",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    my $result;
    my $cur_dir = getcwd();
    chdir $orig_dir;
    my $dir_to_create = adjust_dir($target,$newdir);

    $result = chdir $dir_to_create;
    if ($result == 0) {                 # Check if exists
        my_print("Creating directory: $dir_to_create");
        $result = mkdir $dir_to_create;           # Create directory
        if ($result == 0) {
            my @breakdown = split('/',$dir_to_create);
            my $existing_path = 0;
            my $try_dir;
            my $last_correct_dir;
            my $skip_first_slash = 0;
            if (($breakdown[0] eq ".") || ($breakdown[0] eq "..")) {
                $skip_first_slash = 1;
            }
            foreach(@breakdown) {
                if ($_) {
                    if (!$skip_first_slash) {
                        $try_dir = "$try_dir/$_";
                    } else {
                        $try_dir = "$_";
                    }
                    debug_print("Trying to change into directory: $try_dir");

                    $result = chdir $try_dir;
                    if ($result == 0) {                 # Check if exists
                        debug_print("Failed to change into directory");
                        my $current_dir  = getcwd();
                        debug_print("\$current_dir = $current_dir");
                        if ($current_dir eq $last_correct_dir) {
                            $try_dir = $_;
                        }
                        my_print("    Creating      : $try_dir");
                        $result = mkdir $try_dir;           # Create directory
                        if ($result == 0) {
                            my_print("ERROR: Failed to create: $try_dir",$ERROR_CODE{"Directory"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Directory"});
                        } else {
                            chdir $try_dir;
                            $last_correct_dir = getcwd();
                            debug_print("\$last_correct_dir = $last_correct_dir");
                        }
                    }
                }
            }
        }
        $result = chdir $dir_to_create;    # retry changing into directory
        if ($result == 0) {
            my_print("ERROR: Failed to create and change to: $dir_to_create",$ERROR_CODE{"Directory"},$color_info{"ERR_Color"});
            exit_script($ERROR_CODE{"Directory"});
        } else {
            debug_print("Changed to directory: $dir_to_create");
        }
    } else {
        debug_print("Directory $dir_to_create already exists");
    }
    debug_print("end create_dir() : $dir_to_create",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    chdir $cur_dir;
}

sub find_latest {
    my $download_site    = $_[0];
    my $download_package = $_[1];

    debug_print("sub find_latest($download_site , $download_package)",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    my @download_list;          # list of files to download
    my @directory_content;      # List of files at (remote) site
    my $file_type;              # Type can be 'D' Directory or 'F' File
    my $latest_build;

    my_print("Loading directory structure of $download_site");
    if ((index(lc($download_package),"latest") != -1) || ($global_var{"GET_LATEST_BUILD"})){
        # request to download the latest build from the package
        my $version = $download_package;
        my $dir_checked;
        $version =~ s/\+.*//;
        if (index($download_site,$global_var{"NAMESPACE"}."/SC$version/") == -1) {
            if (rindex($version,".25") != -1) {
                $dir_checked = $download_site."master/";
            } else {
                if (index($download_site,$global_var{"NAMESPACE"}) == -1) {
                    $dir_checked = $download_site.$global_var{"NAMESPACE"}."/SC$version/";
                }
            }
        } else {
            $dir_checked = $download_site;
        }
        my %parameters;
        $parameters{"DIRECTORY"}    = $dir_checked;
        $parameters{"ADJUST"}       = 1;
        $parameters{"NO-RECURSIVE"} = 1;
        $parameters{"SITE"}         = $download_site;
        $parameters{"PACKAGE"}      = $download_package;
        $parameters{"CLEAR-HASH"}   = 1;
        my_print("Loading directory structure of $dir_checked");
        @directory_content = read_directory(\%parameters);
        if (!@directory_content) {
            $dir_checked = $download_site.$global_var{"NAMESPACE"}."/master/";
            $parameters{"DIRECTORY"}    = $dir_checked;
            @directory_content = read_directory(\%parameters);
            if (!@download_list) {                                                        # Check if package found
                my_print("Package ($version) NOT found",$ERROR_CODE{"PackageNotFound"},$color_info{"ERR_Color"});
                exit_script($ERROR_CODE{"PackageNotFound"});                      # Return error if package is not found
            }
        }
        my $largest=0;
        my $build;
        my_print("Checking $dir_checked");
        my_print("For latest build");
        foreach my $entry (@directory_content) {
            $build = substr($entry,index($entry,"+")+1);
            if (isnum($build)){
                if ($build > $largest) {
                    $largest = $build;
                }
            }
        }
        $latest_build = "$version+$largest";
        my_print("Latest build : $latest_build");
    }
    debug_print("end find_latest()  => $latest_build",$GLOBAL_DEBUG{"INDENT-DECREASE"});

    return $latest_build;
}

# ***********************************
# *                                 *
# * Do checks to verify package is  *
# * available on artifactory and    *
# * return a list of files to       *
# * download                        *
# *                                 *
# ***********************************
sub prepare_for_download {
    my $download_site    = $_[0];
    my $download_package = $_[1];
    my $skip_directory_creation = $_[2];
    debug_print("sub prepare_for_download($download_site , $download_package)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    my @download_list;          # list of files to download
    my @directory_content;      # List of files at (remote) site
    my $file_type;              # Type can be 'D' Directory or 'F' File
    my %parameters;             # Parameters to pass on when calling subroutine

    my_print("Loading directory structure of $download_site");
    my $fast_find = find_package_fast($download_site, $download_package);
    # first attempt is to check if site and package combined lead to the correct place
    # if that is not the case then a search starting at the site itself is initiated
    # Prepare list of all (remote) site files
    # parameters:   $site   HTTP link towards site (example:
    #                       https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-generic-local/
    #               1       First call to recursive subroutine

    my $version = $download_package;
    my $dir_checked;
    my $orig_dir_checked;
    $version =~ s/\+.*//;
    my $rest_of_url;
    my $user_specified_subdir;
    my %check_these_urls;
    if (!$fast_find) {
my_print("\n*** WARNING *** WARNING *** WARNING *** WARNING *** WARNING *** WARNING *** WARNING ***");
my_print("SUBROUTINE find_package_fast(\$download_site , \$download_package) FAILED !!!");
my_print("\$download_site    = $download_site");
my_print("\$download_package = $download_package");
my_print("Check code in subroutines find_package_fast(...) and prepare_for_download(...)\n\n");
#goto SKIP_CODE;
#        if ($global_var{"USE-FAST-FIND"}) {
#            my_print("Package NOT found with fast search, proceeding with slow search");                    # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/Solution_CI_Storage/1.12.0+43/
#        }
#        if (index($download_site,$global_var{"NAMESPACE"}) != -1) {
#            debug_print("Provided link contains namespace (".$global_var{"NAMESPACE"}.")");
#            $rest_of_url = substr($download_site,index($download_site,$global_var{"NAMESPACE"})+length($global_var{"NAMESPACE"}));  # /Solution_CI_Storage/1.12.0+43/
#            $user_specified_subdir = $rest_of_url;                                                          # /Solution_CI_Storage/1.12.0+43/
#            debug_print("Remainder after namespace : $rest_of_url");
#            if (index($rest_of_url,"SC$version") != -1) {
#                if (index($rest_of_url,$download_package) != -1) {
#                    $dir_checked = $download_site;
#                    $check_these_urls{"SC"} = $dir_checked;
#                } else {
#                    $dir_checked = $download_site."$download_package/";
#                    $check_these_urls{"PACKAGE"} = $dir_checked;
#                }
#            } else {
#                debug_print("Remainder does not contain SC-version => prepare link to check");
#                my @split_link = split('/',$download_site);
#                if ($split_link[-1] eq $global_var{"NAMESPACE"}) {
#                    $dir_checked = $download_site."SC$version/$download_package/";
#                    $check_these_urls{"SC-PACKAGE"} = $dir_checked;
#                } else {
#                    $dir_checked = $download_site."$download_package/";
#                    $check_these_urls{"PACKAGE"} = $dir_checked;
#                }
#            }
#        } else {
#            debug_print("Provided link does not contain namespace (".$global_var{"NAMESPACE"}.") insert it...");
#            $dir_checked = $download_site.$global_var{"NAMESPACE"}."/SC$version/$download_package/";
#            if ($download_site ne $dir_checked) {
#                $dir_checked = $download_site."$download_package/";
#            }
#            $check_these_urls{"PACKAGE"} = $dir_checked;
#        }
#        $orig_dir_checked = $dir_checked;
#
#        if ((index($download_package,".25") != -1) && (index($download_site,$global_var{"NAMESPACE"}) != -1) && (index($download_site,"master") == -1) ){
#            my $test_site = $download_site;
#            chop($test_site);
#            if (substr($download_site,rindex($download_site,"/")) eq $global_var{"NAMESPACE"}) {
#                $dir_checked = $download_site."master/$download_package/";
#                $check_these_urls{"MASTER"} = $dir_checked;
#            }
#        }
#SKIP_CODE:
    } else {
        $dir_checked = $fast_find;
        $check_these_urls{"PACKAGE"} = $dir_checked;
       # $download_site = $global_var{"SITE"};
    }
    my_print("Checking: $dir_checked");

    $parameters{"DIRECTORY"}    = $dir_checked;
#    $parameters{"DIRECTORY"}    = $check_these_urls{"PACKAGE"};
    $parameters{"ADJUST"}       = 1;
    $parameters{"SITE"}         = $download_site;
    $parameters{"PACKAGE"}      = $download_package;


    @directory_content = read_directory(\%parameters);
    if (!@directory_content) {
        if (index($dir_checked,"master") == -1) {
            $dir_checked = $download_site."master/$download_package/";
        } else {
            $dir_checked = $orig_dir_checked;
        }
        my_print("Checking alternative 1: $dir_checked");
        $parameters{"DIRECTORY"}    = $dir_checked;
        @directory_content = read_directory(\%parameters);
        if (!@directory_content) {
            $dir_checked = "$download_site$download_package/";
            $parameters{"DIRECTORY"}    = $dir_checked;
            my_print("Checking alternative 2: $dir_checked");
            @directory_content = read_directory(\%parameters);
            if (!@directory_content) {
                my_print("Checking alternative 3: $download_site");
                $parameters{"DIRECTORY"}    = $download_site;
                @directory_content = read_directory(\%parameters);
            }
        }
    }

    my_print("Loaded directory of $dir_checked");

    my_print("Searching package files");
    my $changed_packagename;
    my $found_package;
    ($found_package,$changed_packagename,@download_list) = find_package($download_package,       # Search for the package within the files from the HTTP site
                                   \@directory_content
                                   );
    debug_print("Received nr of entries in \@download_list: ".scalar(@download_list));

    if (scalar(@download_list) < 1) {                                                        # Check if package found
        my_print("Package NOT found",$ERROR_CODE{"PackageNotFound"},$color_info{"ERR_Color"});
        exit_script($ERROR_CODE{"PackageNotFound"});                      # Return error if package is not found
    }
    if (!$found_package) {                                                        # Check if package found
        my_print("Package NOT found",$ERROR_CODE{"PackageNotFound"},$color_info{"ERR_Color"});
        exit_script($ERROR_CODE{"PackageNotFound"});                      # Return error if package is not found
    }

    my_print("Determined files to download");
    if (use_debug()) {
        foreach my $key (sort keys %site_entries) {
            if (exists $site_entries{$key}{"MARKED"}) {
                foreach my $subkey (sort keys %{$site_entries{$key}}) {
                    debug_print("   site_entries{$key}{$subkey} = ".$site_entries{$key}{$subkey});
                }
            }
        }
    }
    if (!$skip_directory_creation) {
        my_print("Create directory structure");
        # Setup Directoy structure first
        # Under the Target Directory create the directories that are needed for the download
        # Prefered structure:  (<packageName> can for example be: scp-bsf-rx-poc)
        #  <targetDirectory>/<packageName>/
        #  <targetDirectory>/<packageName>/csar/
        #  <targetDirectory>/<packageName>/release-artifacts/
        #  <targetDirectory>/<packageName>/tools/
        #
        my @temp_list;
        foreach my $entry (@download_list) {
            $file_type = get_indicator($entry);
            if ( ($global_var{"COPY-ONLY-CSAR"} && (index(uc($entry),"CSAR") != -1)) ) {
                push(@temp_list,$entry);
            }
            if ($file_type eq "D") {
                if ( (!$global_var{"COPY-ONLY-CSAR"}) || ($global_var{"COPY-ONLY-CSAR"} && (index(uc($entry),"CSAR") != -1)) ) {
                    my $download_dir_start = index($entry,$download_package);
                    if ($download_dir_start == -1) {
                        my @breakdown = split('/',$entry);
                        $download_dir_start = index($entry,$breakdown[-2]);
                        debug_print(substr($entry,$download_dir_start));
                        debug_print("compare $breakdown[-2] with ".$global_var{"SITE-ROOT"});
                        if ($breakdown[-2] eq $global_var{"SITE-ROOT"}) {
                            $download_dir_start = index($entry,$breakdown[-1]);
                            $use_alternative_dir=1;
                        }
                    }
                    if ($download_dir_start != -1) {
                        my $create_new_dir;
                        if ($use_alternative_dir) {
                            my $sub_string = substr($entry,$download_dir_start);
                            $create_new_dir = "$download_package/$sub_string";
                            debug_print("combine \$download_package/\$sub_string into \$create_new_dir");
                        } else {
                            $create_new_dir = substr($entry,$download_dir_start);
                            debug_print("store substr($entry,$download_dir_start) into \$create_new_dir");
                        }
                        debug_print("\$global_var{\"TARGET-DIRECTORY\"} = ".$global_var{"TARGET-DIRECTORY"});
                        debug_print("\$create_new_dir   = $create_new_dir");
                        create_dir($global_var{"TARGET-DIRECTORY"},$create_new_dir);
                        if ($use_alternative_dir) {
                            debug_print("SET: \$global_var{\"TARGET-DIRECTORY\"} = \$global_var{\"TARGET-DIRECTORY\"}/\$download_package");
                            debug_print("SET: ".$global_var{"TARGET-DIRECTORY"}." = ".$global_var{"TARGET-DIRECTORY"}."/".$download_package);
                        }
                    } else {
                        my_print("UNEXPECTED ERROR",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                        exit_script($ERROR_CODE{"Standard"});
                    }
                }
            }
        }
        if (@temp_list) {
            @download_list = @temp_list;
        }
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end prepare_for_download() : $changed_packagename , @download_list",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return ($changed_packagename, @download_list);
}


sub get_main_package_folders {
    debug_print("get_main_package_folders()",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my @main_folders;
    foreach my $key (keys %package_info) {
        if (index($key,"/") == -1) {
            push(@main_folders,$key);
            debug_print("main folder: $key");
        }
    }
    debug_print("end get_main_package_folders()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @main_folders;
}

sub get_my_main_folder {
    my $folder          = $_[0];
    my @main_folders    = @{$_[1]};
    debug_print("get_my_main_folder(".$folder." , [".(join " ; ", @main_folders)."] )",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    foreach my $this_folder (@main_folders) {
        if (index($folder,$this_folder) != -1) {
            debug_print("end get_my_main_folder() : ".$this_folder);
            return $this_folder;
        }
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end get_my_main_folder() : NOT FOUND",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return "";
}

sub is_main_folder {
    my $folder          = $_[0];
    my @main_folders    = @{$_[1]};
    debug_print("is_main_folder(".$folder." , [".(join " ; ", @main_folders)."] )",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    my $work_var;
    foreach my $this_folder (@main_folders) {
        if (index($folder,$this_folder) != -1) {
            $work_var = substr($folder,index($folder,$this_folder)+length($this_folder));   # prefix/maindir  prefix/maindir/subdir  $work_var: ''   'subdir'
            debug_print("$folder could be a main folder, it contains $this_folder => check : '$work_var'");
            # https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/master/1.14.25+611/base_stability_traffic
            # base_stability_traffic
            # check : 'ricsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/master/1.14.25+611/base_stability_traffic'
            if (length($work_var) < 1) {
                # this is a main folder
                last;
            }
        }
        $work_var = "";
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end is_main_folder() : ".$work_var,$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $work_var;
}

sub mark_subfolders {
    my $main_folder = $_[0];
    my $mark_as     = $_[1];
    my %status      = %{$_[2]};
    debug_print("mark_subfolders(".$main_folder." , ".$mark_as." , \%status )",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    foreach my $key (keys %status) {
        if (index($key,$main_folder) != -1) {
            debug_print("Mark : ".$key." with value ".$mark_as);
            $status{$key}{"DOWNLOADED"} = $mark_as;
        }
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end mark_subfolders()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return %status;
}

# *****************************
# *                           *
# * Download a package        *
# *                           *
# *****************************
sub download_package {
    my @files_to_download           = @{$_[0]};
    my $download_package            = $_[1];
    my $skip_size_check             = $_[2];
    my $workingDir = $global_var{"TARGET-DIRECTORY"}."/".$global_var{"PACKAGE-NAME"};    # Used to indicate the current directory we are in
    my $file_type;                                      # Type can be 'D' Directory or 'F' File
    my $package_root;
    my %status;
    if (use_debug()) {
        my $all_files;
        foreach (@files_to_download) {
            if ($all_files) {
                $all_files .= "                 $_,\n";
            } else {
                $all_files = "$_,\n";
            }
        }
        debug_print("sub download_package($all_files\n                 $download_package)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    }
    my @main_folders = get_main_package_folders();
    # Check if only CSAR needs to be downloaded or the entire package
    # value of $global_var{"COPY-ONLY-CSAR"} is set to 1  => Copy only the CSAR
    # value of $global_var{"COPY-ONLY-CSAR"} is set to 0  => Copy the entire package, and thus check for signed/unsigned version
    if (!$global_var{"COPY-ONLY-CSAR"}) {
        my %available_csar_files;
        $available_csar_files{"SIGNED"}   = "";
        $available_csar_files{"UNSIGNED"} = "";
        foreach my $file (@files_to_download) {
            $status{$file}{"DOWNLOADED"} = 0; # mark all files/directories as not yet downloaded
            if (index(lc $file,".csar") != -1) {
                my $file_name = substr($file,rindex($file,"/"));
                # mark if signed and/or unsigned are present
                if (index(lc $file_name,"unsigned") != -1) {
                    if (index(lc $file_name,".md5") != -1) {
                        $available_csar_files{"UNSIGNED_MD5"} = $file;
                    } elsif (index(lc $file_name,".sha256") != -1) {
                        $available_csar_files{"UNSIGNED_SHA256"} = $file;
                    } else {
                        $available_csar_files{"UNSIGNED"} = $file;
                        my_print("Found UNSIGNED CSAR file : ".get_filename($file));
                    }
                } else {
                    if (index(lc $file_name,".md5") != -1) {
                        $available_csar_files{"SIGNED_MD5"} = $file;
                    } elsif (index(lc $file_name,".sha256") != -1) {
                        $available_csar_files{"SIGNED_SHA256"} = $file;
                    } elsif (index(lc $file_name,".sha1") != -1) {
                        $available_csar_files{"SIGNED_SHA1"} = $file;
                    } else {
                        $available_csar_files{"SIGNED"} = $file;
                        my_print("Found SIGNED CSAR file : ".get_filename($file));
                    }
                }
            }
        }
        # check presence of the signed/unsigned csar and make sure that only 1 version is downloaded
        my @temp_files;
        if (($available_csar_files{"SIGNED"} ne "") && ($available_csar_files{"UNSIGNED"} ne "")) {
            if (!$global_var{"UNSIGNED"}) {
                my_print("Both SIGNED and UNSIGNED CSAR files are present at artifactory, only downloading SIGNED package");
            } else {
                my_print("Both SIGNED and UNSIGNED CSAR files are present at artifactory, only downloading UNSIGNED package due ".$global_var{"UNSIGNED-TEXT"});
            }
            foreach my $file_info (@files_to_download) {
                if (!$global_var{"UNSIGNED"}) {
                    if ($file_info eq $available_csar_files{"UNSIGNED"}) {
                        $file_info = "";
                    }
                    if ($file_info eq $available_csar_files{"UNSIGNED_MD5"}) {
                        $file_info = "";
                    }
                    if ($file_info eq $available_csar_files{"UNSIGNED_SHA256"}) {
                        $file_info = "";
                    }
                } else {
                    if ($file_info eq $available_csar_files{"SIGNED"}) {
                        $file_info = "";
                    }
                    if ($file_info eq $available_csar_files{"SIGNED_MD5"}) {
                        $file_info = "";
                    }
                    if ($file_info eq $available_csar_files{"SIGNED_SHA256"}) {
                        $file_info = "";
                    }
                    if ($file_info eq $available_csar_files{"SIGNED_SHA1"}) {
                        $file_info = "";
                    }
                }
                if ($file_info) {
                    push(@temp_files,$file_info);
                }
            }
            @files_to_download = @temp_files;
        }
    }
    # Copy Files
    foreach my $file_info (@files_to_download) {
        $file_type = get_indicator($file_info);
        if ($file_type eq "F") {
            my $download_this = get_filename($file_info);
            my $remote_path = get_remote_path($file_info);

            my_print("Remote download from : $remote_path");
            my $download_dir_start = index($file_info,$global_var{"PACKAGE-NAME"});
            my $copy_to_dir;
            if ($use_alternative_dir) {
                my @breakdown = split('/',$file_info);
                debug_print("Altenative dir: $file_info");
                $download_dir_start = index($file_info,$breakdown[-2]);
            }
            if ($download_dir_start != -1) {
                if ($use_alternative_dir) {
                    my $sub_string = substr($_,$download_dir_start);
                    $copy_to_dir = "$download_package/$sub_string";
                    debug_print("Use alternative: $copy_to_dir   for package: $download_package");
                } else {
                    $copy_to_dir = substr($file_info,$download_dir_start);
                }
                $workingDir = $global_var{"TARGET-DIRECTORY"}."/$copy_to_dir";

                my @adjust_dir = split('/',$workingDir);
                $workingDir = "";
                for (my $i; $i < @adjust_dir-1; $i++) {
                    if ($adjust_dir[$i]) {
                        $workingDir = "$workingDir/$adjust_dir[$i]";
                    }
                }

                my_print("Changing directory to: $workingDir");
                if (!(-d $workingDir)) {
                    my_print("Directory $workingDir does not exist, creating directory");
                    create_dir( $global_var{"TARGET-DIRECTORY"} , substr($workingDir,length($global_var{"TARGET-DIRECTORY"})) );
                }
                chdir ($workingDir);
                my_print("Downloading          : $adjust_dir[@adjust_dir-1]");
                my $retries=1;
                my $successful_download;
                while ($retries) {
                    if (($global_var{"PROGRESS-OFF"}) || ($global_var{"SILENT-ON"})) {
                        $curl_output = send_curl_command("$download_this -O --retry 3 --silent" , $global_var{"CURL-MAXTIME-DOWNLOAD"});
                    } else {
                        $curl_output = send_curl_command("$download_this -O --retry 3 --progress-bar" , $global_var{"CURL-MAXTIME-DOWNLOAD"});
                    }

                    if ($curl_output) {
                        if ((index($curl_output,"status") != -1) && (index($curl_output,"28") != -1)) {
                            my_print("\nTimeout for CURL $download_this",$ERROR_CODE{"Timeout"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Timeout"});
                        } else {
                            my_print("UNEXPECTED result = $curl_output",$ERROR_CODE{"Download"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Download"});
                        }
                    }

                    # file is downloaded, check downloaded size against size on repo
                    my $repo_filesize = get_filesize($file_info);
                    my $downloaded_file = $workingDir."/".$adjust_dir[@adjust_dir-1];
                    my $downloaded_filesize = -s $downloaded_file;
                    if ($skip_size_check) {
                        $repo_filesize = $downloaded_filesize;
                    }
                    my $difference = abs($repo_filesize-$downloaded_filesize);
                    my $in_range;
                    if ($difference > 10) {
                        my $w_repo = $repo_filesize;
                        my $w_down = $downloaded_filesize;
                        my $done;
                        while (!$done) {
                            if (($w_repo > 1024) && ($w_down > 1024)) {
                                $w_repo /= 1024;
                                $w_down /= 1024;
                                $difference = abs($w_repo-$w_down);
                                if ($difference < 10) {
                                    $done = 1;
                                    $in_range = 1;
                                }
                            } else {
                                $done = 1;
                            }
                        }
                    } else {
                        $in_range = 1;
                    }
                    debug_print("Repo_FileSize        : $repo_filesize");
                    debug_print("Downloaded_FileSize  : $downloaded_filesize");
                    if ($in_range) {
                        debug_print("Sizes are within an acceptable range equal to eachother");
                        $retries = 0;
                        $successful_download = 1;
                    } else {
                        $retries--;
                    }
                }
                if (!$successful_download) {
                    my_print("!!! Failed download of $adjust_dir[@adjust_dir-1] !!!",$ERROR_CODE{"Download"},$color_info{"ERR_Color"});
                    exit_script($ERROR_CODE{"Download"});
                }
                my @split_file_name = split('\.',$adjust_dir[@adjust_dir-1]);
                my $file_name_extension = $split_file_name[@split_file_name-1];
                debug_print("File extension: $file_name_extension");
                if ($file_name_extension eq "csar") {
                    if (index($adjust_dir[@adjust_dir-1],"unsigned") != -1) {
                        $global_var{"DOWNLOADED-CSAR-TYPE"}  = "UNSIGNED";
                    } else {
                        $global_var{"DOWNLOADED-CSAR-TYPE"}  = "SIGNED";
                    }
                    $global_var{"CSAR-NAME"} = $adjust_dir[@adjust_dir-1];
                    debug_print "MD5 Check on: $workingDir/$adjust_dir[@adjust_dir-1]";
                    $md5_file = "$workingDir/$adjust_dir[@adjust_dir-1]";
                    push(@md5_files,$md5_file);
                }
            } else {
                my_print("UNEXPECTED ERROR",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                exit_script($ERROR_CODE{"Standard"});
            }
        } elsif (!$package_root) {
            $package_root = get_remote_path($file_info);
            $package_root = substr($package_root,0,rindex($package_root,"/"));
            my_print("\$package_root = $package_root");
        }
    }

    debug_print("end download_package()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return;
}


# *****************************
# *                           *
# * Provide directory listing *
# *                           *
# *****************************
sub list_dir_content {
    my $directory_to_check = $_[0];
    debug_print("sub list_dir_content($directory_to_check)",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    my @files;
#    if (exists $stored_lists{$directory_to_check}) {
#        debug_print(" - existing entry with stored directory content could be used");
#        @files = @{$stored_lists{$directory_to_check}};
#    } else {
        debug_print(" - prepare new entry for storing directory content");
        debug_print(" - for '$directory_to_check'");
        my $currDir = getcwd();
        my $exclude_files_from_dir = "";

        find({ wanted => sub
                         {
                            if (-d $_) {
                                # check if this is an expected directory, if not exclude it
                                $exclude_files_from_dir = ""; # /local/eedwiq/download/1.13.25+1351/base_stability_traffic/traffic_simulators/helm
                                debug_print("Check dir: $_");
                                my $key_start = rindex($_,"/")+1;
                                $key_start = length($directory_to_check);
                                if ($key_start != -1) {
                                    my $key = substr($_,$key_start);
                                    debug_print("  Key = $key");
                                    if (exists($package_info{$key})) {
                                        if (exists $package_info{$key}) {
                                            debug_print("push(\@files, $_)");
                                            push(@files, $_);
                                        } else {
                                            debug_print("File not Allowed/Expected");
                                            push(@files, $_);
                                        }
                                    } else {
                                        $exclude_files_from_dir = $_;
                                        push(@files, $_);
                                        if ((use_debug()) && ($key)){
                                            debug_print("*************************************",$color_info{"INF_Color"});
                                            debug_print("File not Allowed/Expected, key ($key) not found in record \%package_info this might be a directory which contains no files, please check",$color_info{"INF_Color"});
                                            debug_print("*************************************",$color_info{"INF_Color"});
                                        }
                                    }
                                }
                            } else {
                                if (($exclude_files_from_dir) &&
                                    (index($_,$exclude_files_from_dir) != -1)) {
                                    debug_print("skipping file due to exclude: $_");
                                    push(@files, $_);
                                } else {
                                    debug_print("push(\@files, $_)");
                                    push(@files, $_);
                                }
                            }
                         }
              , no_chdir => 1 }, $directory_to_check);
        $stored_lists{$directory_to_check} = [@files];
#    }
    debug_print("end list_dir_content() : @files",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @files;
}


# ********************************
# *                              *
# * Look if an exentions is      *
# * present in a (sub)directory  *
# *                              *
# ********************************
sub find_extension {
    my $dir                 = $_[0];
    my $extension_to_find   = $_[1];
    debug_print("sub find_extension($dir , $extension_to_find)",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    if (!@local_file_list) {
        @local_file_list = list_dir_content($dir);
    }
    #my @determineFile   = list_dir_content($dir);
    my $extension;
    my @foundFilename   = ();

    foreach (@local_file_list)
    {
        $extension = index(lc($_),lc($extension_to_find));
        if ($extension!= -1)
        {
            # check found position against expected position
            if ($extension == (length($_) - length($extension_to_find))) {
                debug_print("===> MATCH \$extension = $extension   AND \$extension_to_find = $extension_to_find <===");
                push(@foundFilename,substr($_,0));
                #last;
            } else {
                if ($extension_to_find) {
                    debug_print("!!!! NO MATCH \$extension = $extension   AND \$extension_to_find = $extension_to_find !!!!");
                }
            }
        } else {
            debug_print("NO MATCH for $_  : \$extension = $extension   AND \$extension_to_find = $extension_to_find");
        }
    }
    debug_print("end find_extension() : @foundFilename",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return @foundFilename;
}


# *****************************
# *                           *
# * Crude check if all files  *
# * are present, checks the   *
# * extensions of the files   *
# * against the expected      *
# * extensions                *
# *                           *
# *****************************
sub all_files_present {
    my $dir = $_[0];
    my @missing;
    my $error=$ERROR_CODE{"NormalExit"};
    my @package_breakdown = split('\+',$global_var{"PACKAGE-NAME"});
    my %missing_files;

    debug_print("sub all_files_present($dir)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    if (!$global_var{"SKIP-CHECKS"}) {
        my @files_downloaded   = list_dir_content($dir);
        my @dirs_in_record;
        my_print("Checking if all files in package are downloaded to directory: $dir");
        my @allowed_dirs;
        my @mandatory_dirs;
        my @optional_dirs;
        my @extra_dirs;
        my $files_marked_mandatory=0;
        my $count_opional_dirs=0;
        my $count_mandatory_dirs=0;
        foreach my $key (sort keys %package_info) {
            push(@allowed_dirs,$key);
            if ($package_info{$key}{"MANDATORY"}) {
                if (check_if_valid_release($package_breakdown[0] , $package_info{$key}{"VALID-RELEASES"})) {
                    if (($repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"} eq "SC") || (index($package_info{$key}{"VALID-RELEASE_TYPES"}, $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}) != -1)) {
                        my_print("directory $key is a valid release for this mandatory directory");
                        push(@mandatory_dirs,$key);
                        $MIN_MANDATORY_FILES += get_package_nr_entries($key);
                        $count_mandatory_dirs++;
                    }
                } else {
                    my_print("directory $key is NOT a valid release for this mandatory directory");
                }
            } else {
                if (check_if_valid_release($package_breakdown[0] , $package_info{$key}{"VALID-RELEASES"})) {
                    if (($repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"} eq "SC") || (index($package_info{$key}{"VALID-RELEASE_TYPES"}, $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}) != -1)) {
                        my_print("directory $key is a valid release for this optional directory");
                        push(@optional_dirs,$key);
                        $count_opional_dirs++;
                    }
                } else {
                    my_print("directory $key is NOT a valid release for this optional directory");
                }
            }
        }
        push(@dirs_in_record,@mandatory_dirs);
        push(@dirs_in_record,@optional_dirs);
        foreach my $this_downloaded_file (@files_downloaded) {
            if (-d $this_downloaded_file) {
                # check if this directory is part of the record
#my_print("CHECK DOWNLOADED : $this_downloaded_file");
                my $match_found;
                foreach my $this_file (@dirs_in_record) {
#my_print("   CHECK AGAINST : $this_file");
                    if (index($this_downloaded_file,$this_file) != -1) {
#my_print("           MATCH : $this_file");
                        $match_found = 1;
                        last;
                    }
                }
                if (!$match_found) {
                    push(@extra_dirs,$this_downloaded_file);
                }
            }
        }
        #push(@allowed_dirs,@mandatory_dirs);
        my $total_dirs = `find $dir -type d | wc -l`;
        my $total_mandatory_files= 0;
        my $number_expected_dirs = @allowed_dirs;
        my $number_expected_files = $MIN_MANDATORY_FILES;
        chomp($total_dirs);
        $total_dirs--;
        my_print("Number of downloaded directories ($total_dirs), total of mandatory dirs ($count_mandatory_dirs) and optional dirs ($count_opional_dirs)  = (".($count_mandatory_dirs+$count_opional_dirs).")");
#        my_print("Checking number of downloaded directories ($total_dirs) against total of mandatory dirs ($count_mandatory_dirs) and optional dirs ($count_opional_dirs)  = (".($count_mandatory_dirs+$count_opional_dirs).")");
#        if ($total_dirs != $number_expected_dirs) {
#            if ($total_dirs > $number_expected_dirs) {
#                my_print("Unexpected files/directories present in directory $dir",$color_info{"INF_Color"});
#            } else {
#                my_print("There could be files/directories missing from $dir");
#            }
#        }
#        if (@extra_dirs) {
#            my_print("There are files present which are not part of the record \%package_info, please check");
#            foreach (@extra_dirs) {
#                my_print("   $_");
#            }
#            my_print(" ");
#        }

        my $nr_of_files_missing = 0;
        foreach my $this_mandatory_dir (@mandatory_dirs) {
            my_print("Checking mandatory dir: $this_mandatory_dir");
            my $number_of_files=`find $dir/$this_mandatory_dir -type f | wc -l`;
            $number_of_files =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
            debug_print("Remove leading/trailing spaces: $number_of_files");

            if (isnum($number_of_files) && ($number_of_files != 0)) {
                my_print("   - There are $number_of_files files present in $this_mandatory_dir");
                $total_mandatory_files += $number_of_files;
                my @list_of_files;
                @list_of_files=split('\n',`find $dir/$this_mandatory_dir -type f`);
                if ((exists $package_info{$this_mandatory_dir}{"NR-ENTRIES"}) || (exists $package_info{$this_mandatory_dir}{"NR-SUBDIR-ENTRIES"})) {
                    my_print("   - There are mandatory files for this directory");
                    #my $count = $package_info{$this_mandatory_dir}{"NR-ENTRIES"};
                    my $count = get_package_nr_entries($this_mandatory_dir);
                    my $count_mandatory = 0;
                    my $count_optional  = 0;
                    my $count_how_many_present = 0;
                    while ($count > 0) {
                        my_print("   - checking ".$package_info{$this_mandatory_dir}{"DIR-CONTENTS-".$count}{"EXTENSION"});
                        if ($package_info{$this_mandatory_dir}{"DIR-CONTENTS-".$count}{"MANDATORY"}) {
                            $count_mandatory++;
                            my $found_extension = 0;
                            foreach my $file_from_list (@list_of_files) {
                                my $extension_position = index($file_from_list,$package_info{$this_mandatory_dir}{"DIR-CONTENTS-".$count}{"EXTENSION"});
                                if ($extension_position != -1) {
                                    my $predicted_position = length($file_from_list) - length($package_info{$this_mandatory_dir}{"DIR-CONTENTS-".$count}{"EXTENSION"});
                                    if ($predicted_position == $extension_position) {
                                        $found_extension = 1;
                                        $count_how_many_present++;
                                        #last;
                                    }
                                }
                            }
                            if (!$found_extension) {
                                $nr_of_files_missing++;
                                my $already_in_list;
                                foreach (@missing) {
                                    if ($_ eq $this_mandatory_dir) {
                                        $already_in_list=1;
                                        last;
                                    }
                                }
                                if (!$already_in_list) {
                                    my_print("         [ NOT FOUND : ".$package_info{$this_mandatory_dir}{"DIR-CONTENTS-".$count}{"EXTENSION"}." ]");
                                    push(@missing,$this_mandatory_dir);
                                }
                            }
                        } else {
                            $count_optional++;
                        }
                        $count--;
                    }
                    my $count_info_text = "     Minimum expected: $count_mandatory  Present: $count_how_many_present  [Number of optional files: $count_optional]";
                    my_print("$count_info_text");
                }
            } else {
                my_print("Can not find mandatory dir: $_");
                my_print("    number of files 'find' reported: $number_of_files");
                push(@missing,$this_mandatory_dir);
            }
        }
        foreach my $this_optional_dir (@optional_dirs) {
            my_print("Checking optional dir: $this_optional_dir");
            my $exists_optional_dir;
            if (-d "$dir/$this_optional_dir") {
                $exists_optional_dir = 1;
            }
#            if ((!$exists_optional_dir) && ($package_info{$this_optional_dir}{"COPY"})) {
#                push(@missing,$this_optional_dir);
#            }

            if ($exists_optional_dir) {
                my $number_of_files=`find $dir/$this_optional_dir -type f | wc -l`;
                $number_of_files =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
                debug_print("Remove leading/trailing spaces: $number_of_files");
                $total_mandatory_files += $number_of_files;
                if (isnum($number_of_files) && ($number_of_files != 0)) {
                    my_print("   - There are $number_of_files files present in $this_optional_dir");

                    my @list_of_files=split('\n',`find $dir/$this_optional_dir -type f`);
                    if ((exists $package_info{$this_optional_dir}{"NR-ENTRIES"})  || (exists $package_info{$this_optional_dir}{"NR-SUBDIR-ENTRIES"})) {
                        my_print("   - There are mandatory/optional files for this directory");
                        #my $count = $package_info{$this_optional_dir}{"NR-ENTRIES"};
                        my $count = get_package_nr_entries($this_optional_dir);
                        while ($count > 0) {
                            my_print("   - checking ".$package_info{$this_optional_dir}{"DIR-CONTENTS-".$count}{"EXTENSION"});
                            if ($package_info{$this_optional_dir}{"DIR-CONTENTS-".$count}{"MANDATORY"}) {
                                my $found_extension = 0;
                                foreach my $file_from_list (@list_of_files) {
                                    my $extension_position = index($file_from_list,$package_info{$this_optional_dir}{"DIR-CONTENTS-".$count}{"EXTENSION"});
                                    if ($extension_position != -1) {
                                        my $predicted_position = length($file_from_list) - length($package_info{$this_optional_dir}{"DIR-CONTENTS-".$count}{"EXTENSION"});
                                        if ($predicted_position == $extension_position) {
                                            $found_extension = 1;
                                            last;
                                        }
                                    }
                                }
                                if (!$found_extension) {
                                    $nr_of_files_missing++;
                                    my $already_in_list;
                                    foreach (@missing) {
                                        if ($_ eq $this_optional_dir) {
                                            $already_in_list=1;
                                            last;
                                        }
                                    }
                                    if (!$already_in_list) {
                                        my_print("         [ NOT FOUND : ".$package_info{$this_optional_dir}{"DIR-CONTENTS-".$count}{"EXTENSION"}." ]");
                                        push(@missing,$this_optional_dir);
                                    }
                                }
                            }
                            $count--;
                        }
                    }
                }
            } else {
                if ($package_info{$this_optional_dir}{"COPY"}) {
                    my_print("   Optional dir does not exist, but it should be copied if present remotely");
                } else {
                    my_print("   Optional dir does not exist, continue");
                }
            }

        }

        my_print("Checking number of downloaded files ($total_mandatory_files) against the minimum number of expected/allowed files ($number_expected_files)");
        if ($total_mandatory_files != $number_expected_files) {
            if ($total_mandatory_files < $number_expected_files) {
                if (!$global_var{"DOWNLOAD-ONLY"}) {
                    my_print("Mandatory files/directories are missing in directory $dir",$ERROR_CODE{"Missing_file"},$color_info{"ERR_Color"});
                }
            }
        }

        my_print("Looking if there are files missing");
        if (@missing) {
            my_print("Files ($nr_of_files_missing) missing in directory: $dir");
            foreach my $missing_file (@missing) {
                foreach my $key (keys %package_info) {
                    if (($package_info{$key}{"MANDATORY"}) && ($package_info{$key}{"COPY"}) && (check_if_valid_release($package_breakdown[0] , $package_info{$key}{"VALID-RELEASES"}))) {
                        if ((exists $package_info{$key}{"NR-ENTRIES"}) || (exists $package_info{$key}{"NR-SUBDIR-ENTRIES"})) {
                            #my $count = $package_info{$key}{"NR-ENTRIES"};
                            my $count = get_package_nr_entries($key);
                            while ($count > 0) {
                                if (exists $package_info{$key}{"DIR-CONTENTS-".$count}{"PRE-FIX"}) {
                                    if (index(uc($missing_file),uc($package_info{$key}{"DIR-CONTENTS-".$count}{"EXTENSION"})) != -1) {
                                        my $prefix   = $package_info{$key}{"DIR-CONTENTS-".$count}{"PRE-FIX"};
                                        my $filename = $prefix.$global_var{"PACKAGE-NAME"}.$missing_file;
                                        my_print("Could not find: $filename" , $ERROR_CODE{"Missing_file"} , $color_info{"ERR_Color"});
                                    }
                                }
                                $count--;
                            }
                        }
                    }
                }
            }
            if ($nr_of_files_missing) {
                $error = $ERROR_CODE{"Missing_file"};
            }
        } else {
            my_print("All files present");
        }
    }

    my $csar_dir;
    if (substr($dir,-1) eq "/") {
        $csar_dir = $dir."csar/*.csar";
    } else {
        $csar_dir = $dir."/csar/*.csar";
    }
    my @csar_files = split('\n',`ls -l $csar_dir`);
    foreach (@csar_files) {
        my $csar_name = substr($_,rindex($_," ")+1);
        if (index($csar_name,".csar") != -1) {
            my $certified = `unzip -l $csar_name | grep certificate.cert`;
            if (index($certified,"Files/certificate.cert") != -1) {
                my_print("$csar_name is signed properly");
            } elsif (index($csar_name,"unsigned") == -1) {
                my_print("\nNOTICE:");
                my_print("   $csar_name is not signed properly",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                my_print("   This could be an older package or an unsigned package\n",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                $global_var{"LOCAL-CSAR-TYPE"} = "UNSIGNED"." Decided after sanity check on package (Files/certificate.cert not found)";
            }
        }
    }

    my %file_to_check;
    if ($repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"} ne "CNDSC") {
        $file_to_check{"NAME"}          = "sc_vnf_descriptor.yaml";
        $file_to_check{"CHECK-SIZE"}    = 1;
        $file_to_check{"MIN-SIZE"}      = 20000;
        $file_to_check{"MIN-SIZE-TXT"}  = "20KB";
        $file_to_check{"MAX-SIZE"}      = 2500000;
        $file_to_check{"MAX-SIZE-TXT"}  = "2.5MB";
        $file_to_check{"ACTION"}        = "WARNING";
    } else {
        $file_to_check{"NAME"}          = "CNDSC.yaml";
        $file_to_check{"CHECK-SIZE"}    = 0;
        $file_to_check{"MIN-SIZE"}      = 100;
        $file_to_check{"MIN-SIZE-TXT"}  = "100B";
        $file_to_check{"MAX-SIZE"}      = 2500000;
        $file_to_check{"MAX-SIZE-TXT"}  = "2.5MB";
        $file_to_check{"ACTION"}        = "WARNING";
    }

    my_print("Checking file ".$file_to_check{"NAME"}." inside csar package");
    my $fn = $file_to_check{"NAME"};
    my $vnf_valid = `unzip -l $dir/csar/*.csar | grep -i $fn`;
    debug_print("unzip -l $dir/csar/*.csar | grep -i $fn");
    debug_print("RESULT: $vnf_valid");
    if (!$skip_vnfd) {
        if (index(uc $vnf_valid, uc $fn) != -1) {
            $vnf_valid =~ s/^\s+|\s+$//g; # remove leading and trailing spaces
            my @vnf_split = split(' ',$vnf_valid);
            my $vnf_size = $vnf_split[0];
            if ($vnf_size > 0) {
                my_print("   - File ".$vnf_split[-1]." is present in csar and not empty");
                my $filesize_text;
                if ($vnf_size > (1024*1024*1024)) {
                    $filesize_text = sprintf("%2.2f",($vnf_size / (1024*1024*1024)));
                    $filesize_text .= "GB";
                } elsif ($vnf_size > (1024*1024)){
                    $filesize_text = sprintf("%2.2f",($vnf_size / (1024*1024)));
                    $filesize_text .= "MB";
                } elsif ($vnf_size > 1024){
                    $filesize_text = sprintf("%2.2f",($vnf_size / 1024));
                    $filesize_text .= "KB";
                } else {
                    $filesize_text = $vnf_size;
                    $filesize_text .= "B";
                }
                if ($file_to_check{"CHECK-SIZE"}) {
                    debug_print("vnfd file size ($vnf_size) > 0");
                    if ($vnf_size < $file_to_check{"MIN-SIZE"}) {
                        my_print("file $fn inside csar package is probably incorrect/corrupt (size: $filesize_text minimal expected size: ".$file_to_check{"MIN-SIZE-TXT"}.")",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                        debug_print("vnfd file size ($vnf_size) < ".$file_to_check{"MIN-SIZE"});
                        exit_script($ERROR_CODE{"Standard"});
                    }
                    if ($vnf_size > $file_to_check{"MAX-SIZE"}) {
                        my_print("file $fn inside csar package is probably incorrect/corrupt (size: $filesize_text maximum expected size: ".$file_to_check{"MAX-SIZE-TXT"}.")",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                        debug_print("vnfd file size ($vnf_size) > ".$file_to_check{"MAX-SIZE"});
                        exit_script($ERROR_CODE{"Standard"});
                    }
                    my_print("   - Filesize ($filesize_text) is between: ".$file_to_check{"MIN-SIZE-TXT"}." and ".$file_to_check{"MAX-SIZE-TXT"});
                }
            } else {
                debug_print("vnfd file size ($vnf_size) = 0");
                my_print("file $fn inside csar package is empty",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                exit_script($ERROR_CODE{"Standard"});
            }
        } else {
            my_print("Could not find: ".$fn." inside csar package",$ERROR_CODE{"Corrupted"},$color_info{"ERR_Color"});
            if ($file_to_check{"ACTION"} eq "ERROR") {
                $error = $ERROR_CODE{"Corrupted"};
            }
        }
    } else {
        my_print("!!! Skipping vnfd checks due to \$skip_vnfd = $skip_vnfd !!!");
    }
    debug_print("end all_files_present() : $error",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $error;
}

# *****************************
# *                           *
# * Do sha256 checksum check  *
# * on all downloaded files   *
# *                           *
# *****************************
sub check_all_sha256 {
    my $dir = $_[0];
    my $failed;
    debug_print("sub check_all_sha256($dir)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    my_print("Checking all sha256 checksum files");
    my %sha_files = get_sha_file_extensions();
    if (%sha_files) {
        debug_print(" - Checking found sha256 files");
        foreach my $sha_dir (keys %sha_files) {
            my $contents  = $sha_files{$sha_dir}{"DIR-CONTENTS"};
            my $extension = $sha_files{$sha_dir}{"EXTENSION"};
            my @filename = find_extension($dir,$extension);
            if (@filename) {
                foreach my $file (@filename) {
                    debug_print("Invoking checksum check sha256 for: $file");
                    $failed = checksum_check($file,1); # call subroutine with second parameter set to invoke sha256 check
                    if ($failed) {
                        reload_files({"FILE" =>$file});
                        $failed = checksum_check($file,1); # call subroutine with second parameter set to invoke sha256 check
                        if ($failed) {
                            my_print("Checksum failed again after downloading $file from artifactory, please check!");
                        }
                    }
                }
            }
        }
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end check_all_sha256() : $failed",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $failed;
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

# Uses following (global) variables:
# $global_var{"UPLOAD-DIR"}             : URL towards artifactory base directory for storing the package
# $global_var{"PACKAGE-NAME"}           : package (eg. 1.10.0+88), this is the 'main' directory on the base directory for storing the package
# $global_var{"UPLOAD-ALL"}             : In case all files in the local directory need to be uploaded this variable is set by using argument --download-all at the command line
# $global_var{"UPLOAD-OVERWRITE"}       : In case all files in the local directory need to be overwrite already existing files on artifactory this variable is set by using argument --download-forced at the command line
sub do_upload {
    my $local_dir = $_[0];
    debug_print("sub do_upload($local_dir)",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    my @files   = list_dir_content($local_dir);
    #my $number_of_file_types = @expected_files;
    #my $number_of_file_types = keys %file_extensions;
    my @all_extensions = get_all_package_file_extensions();
    my $number_of_file_types = @all_extensions;
    if (index($global_var{"UPLOAD-DIR"},$global_var{"PACKAGE-NAME"}) == -1) {
        if (substr($global_var{"UPLOAD-DIR"},-1) eq "/") {
            $global_var{"UPLOAD-DIR"} .= $global_var{"PACKAGE-NAME"};
        } else {
            $global_var{"UPLOAD-DIR"} .= "/".$global_var{"PACKAGE-NAME"};
        }
    }
    my @md5_entries;
    my @other_entries;
    foreach (@files) {
        if (index($_,"md5") != -1) {
            push(@md5_entries,$_);
        } else {
            push(@other_entries,$_);
        }
    }
    push(@other_entries,@md5_entries);
    @files = @other_entries;
    @other_entries = ();
    @md5_entries   = ();

    foreach my $file (@files) {
        debug_print("Scanning file: $file");
        my $upload_file  = "";
        my $target_name  = "";
        my $position     = -1;
        # Prepare list with  files to upload and skip .sha256 files (these will be created by artifactory)
        if ($global_var{"UPLOAD-ALL"}) {
            if (index($file,"sha256") == -1) {
                debug_print("check if need to upload: $file");
                if (-f $file) {
                    $upload_file = $file;
                    debug_print("             Yes upload: $file");
                }
            }
        } else {
            foreach my $exp_file (@all_extensions) {
                $position = index($file,$exp_file);
                if ($position != -1) {
                    if (index($file,"sha256") == -1) {
                        $upload_file = $exp_file;
                    }
                    last;
                }
            }
        }


        if ($upload_file) {
            my $filename = $file;
            #foreach my $exp_dir (@expected_dir) {
#my_print("Working on $file");

            foreach my $exp_dir (sort keys %package_info) {
                $position = index($file,"/".$exp_dir."/");
                if ($position != -1) {
                    $target_name = substr($file,$position);
                    debug_print("\$target_name ($target_name) constructed (/$exp_dir/) from startpoint: $position in $file");
                } else {
                    $position = index($file,"/".$exp_dir);
                    if ($position != -1) {
                        $target_name = substr($file,$position);
                        debug_print("\$target_name ($target_name) constructed (/$exp_dir) from startpoint: $position in $file");
                    } else {
                        debug_print("\$target_name ($target_name) did not change, ($exp_dir) NOT part of $file");
                    }
                }
            }
            if ($global_var{"UPLOAD-ALL"}) {
                $position = index($file,$global_var{"PACKAGE-NAME"});
                if ($position != -1) {
                    $target_name = substr($file,$position+length($global_var{"PACKAGE-NAME"}));
                    debug_print("\$target_name ($target_name) constructed (from startpoint: $position in $file");
                }
            }

            if (substr($global_var{"UPLOAD-DIR"},-1) eq "/") {
                chop($global_var{"UPLOAD-DIR"});
            }
            if (substr($target_name,0,1) eq "/") {
                $target_name = substr($target_name,1);
            }

#my_print("\$target_name ($target_name)");
#my_print("package (".$global_var{"PACKAGE-NAME"}.")");
#my_print("upload-dir (".$global_var{"UPLOAD-DIR"}.")");
            debug_print("Uploading file: $file to: ".$global_var{"UPLOAD-DIR"}." Package: ".$global_var{"PACKAGE-NAME"});
            if (!$global_var{"DRYRUN"}) {
                # check if file is not yet present to avoid overwriting artifactory files
                my $this_artifact_token = "-H \"X-JFrog-Art-Api:$artifact_token\"";
                my $check_cmnd = $curl{"CURL"}." --silent -o /dev/null -Iw '\%{http_code}' ".$global_var{"UPLOAD-DIR"}."/".$target_name;
                debug_print("CURL: $check_cmnd");
                $curl_output = `$check_cmnd`;
                debug_print("CHECK FILE EXISTS RESULT: $curl_output");
                if (($curl_output ne "200") || ($global_var{"UPLOAD-OVERWRITE"})) {
                    if (($curl_output eq "404") || ($global_var{"UPLOAD-OVERWRITE"})) {
                        debug_print("curl -m 600 -f -k -H \"X-JFrog-Art-Api:".substr($this_artifact_token,16,4)."...".substr($this_artifact_token,-4)."\" --upload-file $file ".$global_var{"UPLOAD-DIR"}."/".$target_name);
                        my_print("  Uploading: ".$global_var{"UPLOAD-DIR"}."/".$target_name);
                        if (($global_var{"PROGRESS-OFF"}) || ($global_var{"SILENT-ON"})) {
                            $curl_output = send_curl_command("-f -k --silent --upload-file $file ".$global_var{"UPLOAD-DIR"}."/".$target_name , $global_var{"CURL-MAXTIME-DOWNLOAD"});
                        } else {
                            $curl_output = send_curl_command("-f -k --progress-bar --upload-file $file ".$global_var{"UPLOAD-DIR"}."/".$target_name , $global_var{"CURL-MAXTIME-DOWNLOAD"});
                        }
                        if ((index($curl_output,"status") != -1) && (index($curl_output,"28") != -1)) {
                            my_print("\nTimeout for CURL --upload-file $file",$ERROR_CODE{"Timeout"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Timeout"});
                        }
                    } else {
                        my_print("SKIPPED Upload of file : $file  DUE TO CURL RETURNCODE: $curl_output");
                    }
                } else {
                    my_print("SKIPPED Upload of file : $file");
                    my_print("File already present at: ".$global_var{"UPLOAD-DIR"}."/".$target_name);
                }
                debug_print("$curl_output");
            } else {
                my $this_artifact_token = $artifact_token;
                if ($curl{"USE"} eq "API") {
                    my_print("\nDRY-RUN: curl -m 600 -f -k -H \"X-JFrog-Art-Api:".substr($this_artifact_token,0,4)."...".substr($this_artifact_token,-4)."\"",$color_info{"DRYRUN_Color"});
                } else {
                    my_print("\nDRY-RUN: curl -m 600 -f -k -H \"Authorization: Bearer ".substr($this_artifact_token,0,4)."...".substr($this_artifact_token,-4)."\"",$color_info{"DRYRUN_Color"});
                }
                my_print("   --upload-file $file",$color_info{"DRYRUN_Color"});
                my_print("                 ".$global_var{"UPLOAD-DIR"}."/".$target_name,$color_info{"DRYRUN_Color"});
            }
        } else {
            debug_print("   Skip uploading: $upload_file");
        }
    }
    debug_print("end do_upload()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
}


# *****************************
# *                           *
# * Upload files towards      *
# * specified target with     *
# * parameter -u/--upload     *
# *                           *
# *****************************
sub upload_package {
    my $dir = $_[0];

    debug_print("sub upload_package($dir)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my_print("Uploading package to: ".$global_var{"UPLOAD-DIR"});

    if ($global_var{"DRYRUN"}) {
        my_print("\n");
        my_print("  ==================================================================================",$color_info{"DRYRUN_Color"});
        my_print("  DUE TO PARAMETER DRY-RUN THE FILES ARE NOT UPLOADED                               ",$color_info{"DRYRUN_Color"});
        my_print("  INSTEAD THE UPLOAD (CURL) COMMANDS WILL BE PRINTED FOR VERIFICATION               ",$color_info{"DRYRUN_Color"});
        my_print("  THE SCRIPT HALTS AND WAITS FOR YOUR INPUT                                         ",$color_info{"DRYRUN_Color"});
        my_print("  YOU HAVE 2 OPTIONS:                                                               ",$color_info{"DRYRUN_Color"});
        my_print("      1. VERIFY THE OUPUT IMMEDIATLY AND IF CORRECT PRESS y/Y (FOLLOWED BY ENTER)   ",$color_info{"DRYRUN_Color"});
        my_print("      2. AFTER VERIFICATION THE SCRIPT CAN BE RE-RUN PROVIDING FOLLOWING PARAMETERS:",$color_info{"DRYRUN_Color"});
        my_print("           --no-download           Prevent downloading the package again            ",$color_info{"DRYRUN_Color"});
        my_print("           --source <source>       Providing the path to the downloaded CSAR package",$color_info{"DRYRUN_Color"});
        my_print("           --no-md5                Skipping the md5 checksum check                  ",$color_info{"DRYRUN_Color"});
        my_print("           --no-sizecheck          Skipping the size check                          ",$color_info{"DRYRUN_Color"});
        my_print("  ==================================================================================",$color_info{"DRYRUN_Color"});
        my_print("\n");
    }
    do_upload($dir);
    if ($global_var{"DRYRUN"}) {

        my_print("Check the output for uploading the package",$color_info{"ERR_Color"});

        my_print("Either rerun the script at a later point in time or perform upload now");
        my $user_answer = prompt_user("Upload now? (y/n) : ");
        if (uc($user_answer) eq "Y") {
            undef $global_var{"DRYRUN"};
            do_upload($dir);
        }
    }
    debug_print("end upload_package()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
}


# *****************************
# *                           *
# * Search md5 file is used   *
# * in case download is       *
# * skipped, since package    *
# * already downloaded        *
# * Check parameters          *
# *                           *
# *****************************
sub find_md5_file {
    my $dir = $_[0];
    debug_print("sub find_md5_file($dir)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    my @determineFile   = list_dir_content($dir);
    my $foundFilename   = "";
    my %returnValue;
    my $md5_extension;
    my $entries=1;

    foreach (@determineFile)
    {
        $md5_extension = index(lc($_),lc("md5"));

        debug_print("Check if $_ contains 'md5'  RESULT: $md5_extension");

        if ($md5_extension != -1)
        {
            if (substr($_,-1) ne "~") {
                debug_print("valid md5 file found: $_");
                my $str_len = length($_);
                $str_len -= 4;
                $foundFilename = substr($_,0,$str_len);
                $returnValue{"FILEPATH-".$entries}                = substr($_,0,rindex($foundFilename,"/")+1);
                $returnValue{"MD5_OWN_FILENAME-".$entries}        = substr($_,rindex($foundFilename,"/")+1);
                $returnValue{"MD5_TARGET_FILENAME-".$entries}     = substr($foundFilename,rindex($foundFilename,"/")+1);
                $entries++;
                #last;
            }
        }
    }
    if (!$foundFilename) {
        debug_print("NO valid md5 file found: $_");
    }
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end find_md5_file() | PATH: ".$returnValue{"FILEPATH-1"}."  MD5-FILE: ".$returnValue{"MD5_OWN_FILENAME-1"}."  TARGET-FILE: ".$returnValue{"MD5_TARGET_FILENAME-1"},$GLOBAL_DEBUG{"INDENT-DECREASE"});
    #return $foundFilename;
    return %returnValue;
}


# *****************************
# *                           *
# * Do md5 or sha256 checksum *
# * check on file             *
# *                           *
# *****************************
sub checksum_check {
    my $file_to_check = $_[0];
    my $chksum_sha   = $_[1];   # if defined sha256 checksum otherwise md5 checksum
    my $chksum_file_name;
    my $file_handle;
    my $file_handle_chksum;
    my $checksum;
    my $chksum_to_compare;
    my $original_chksum;
    my $type;
    my $succes;

    debug_print("sub checksum_check($file_to_check)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    $GLOBAL_DEBUG{"LEVEL"} = "DETAILED";
    if ($chksum_sha) {
        $type = "sha256";
    } else {
        $type = "md5";
    }
    my_print("Performing ($type) checksum on $file_to_check");
    $chksum_file_name = "$file_to_check.$type";

    if (!(-e $chksum_file_name)) {
        debug_print("end checksum_check() ".$ERROR_CODE{"Missing_file"},$GLOBAL_DEBUG{"INDENT-DECREASE"});
        return $ERROR_CODE{"Missing_file"};
    }

    # open file, set to binary mode, process MD5 or SHA256 and close
    if (open($file_handle, '<', $file_to_check)) {
        binmode ($file_handle);
        if ($chksum_sha) {
            $checksum = Digest::SHA->new(256);
        } else {
            $checksum = Digest::MD5->new;
        }
        $checksum->addfile($file_handle);
        $chksum_to_compare = $checksum->hexdigest;

        if (!close($file_handle)) {
            my_print("Can't close file successfully: $file_to_check");
        }
    } else {
        my_print("Can't open file: $file_to_check",$ERROR_CODE{"Open_file"},$color_info{"ERR_Color"});
        debug_print("end checksum_check() ".$ERROR_CODE{"Open_file"},$GLOBAL_DEBUG{"INDENT-DECREASE"});
        return $ERROR_CODE{"Open_file"};
    }

    # check file.md5 or file.sha256 existence, open and read to
    if (open($file_handle_chksum, '<', "$chksum_file_name")) {
        $original_chksum = <$file_handle_chksum>;
        if (!close($file_handle_chksum)) {
            my_print("Can't close file successfully: $chksum_file_name");
        }
    } else {
        my_print("Can't open file: $file_to_check.$type",$ERROR_CODE{"Open_file"},$color_info{"ERR_Color"});
        debug_print("end checksum_check() ".$ERROR_CODE{"Open_file"},$GLOBAL_DEBUG{"INDENT-DECREASE"});
        return $ERROR_CODE{"Open_file"};
    }
    # check length of checksum (32 for md5 and 64 for scha256)
    my $len = 32;
    if ((length($original_chksum) > $len) && (!$chksum_sha)) {
        $original_chksum = substr($original_chksum,0,$len);
    } elsif ((length($original_chksum) > 2*$len) && ($chksum_sha)) {
        $original_chksum = substr($original_chksum,0,2*$len);
    }
    # check if both md5/sha256 values are the same
    if ($chksum_to_compare ne $original_chksum) {
        my_print("$type of file : $chksum_to_compare");
        my_print("orignal $type : $original_chksum");
        my_print("$type check FAILED",$ERROR_CODE{"Failed_md5"},$color_info{"ERR_Color"});
        debug_print("end checksum_check() ".$ERROR_CODE{"Failed_md5"},$GLOBAL_DEBUG{"INDENT-DECREASE"});
        return $ERROR_CODE{"Failed_md5"};
    } else {
        debug_print("$type of file : $chksum_to_compare");
        debug_print("orignal $type : $original_chksum");
    }
    my_print("   Result of checksum was successful");
    $GLOBAL_DEBUG{"LEVEL"} = "STANDARD";
    debug_print("end checksum_check() $succes",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $succes;
}


sub is_directory_empty {
    my $dirname = $_[0];
    debug_print("sub is_directory_empty($dirname)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $used=`du -s $dirname`;
    my $result;
    if ($used != 0) {
        debug_print("$dirname not empty => '$used'");
        my @split_result = split(' ',$used);
        $used = $split_result[0]+0;
        $result=0;
    } else {
        debug_print("$dirname empty");
        $result=1;
    }
    debug_print("end is_directory_empty() : $result",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $result;
}

sub min_max_size {
    my $limits = $_[0];
    debug_print("sub min_max_size($limits)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my @split_result;
    my $min;
    my $max;

    if (index($limits,"-") != -1) {
        @split_result = split('-',$limits);
        $min = $split_result[0];
        $max = $split_result[1];
    } else {
        $min = $limits;
        $max = $limits;
    }
    my $min_in_bytes = number_of_bytes($min);
    my $max_in_bytes = number_of_bytes($max);
    debug_print("end min_max_size() : $min , $max",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return($min,$max,$min_in_bytes,$max_in_bytes);
}

sub get_tree_size {
    my $directory   = $_[0];
    debug_print("sub get_tree_size($directory)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $item;
    my $total=0;
    my @files = list_dir_content($directory);
    for $item (@files) {
        debug_print("Checking: $item");
        if(-d $item) {
            debug_print("   Is a directory, check the directory");
            $total += get_tree_size($directory."/$item");
        } else {
            my $size = -s $item;        # /local/eedwiq/download/package/1.12.0+43/base_stability_traffic/traffic_simulators/docker/eric-dscload-1.13.25-1186.tar
            debug_print("   Is a file, size: $size");
            my $file_extension = "";
            my $curr_pos = index($item,"/");
            my $last_pos = -1;
            while ($curr_pos != -1) {
                $last_pos = $curr_pos;
                $curr_pos = index($item,"/",$curr_pos+1);
            }
            $last_pos = rindex($item,"/");
            if ($last_pos != -1) {
                $file_extension = substr($item,$last_pos+1);
                $file_extension = substr($file_extension,index($file_extension,"."));
                my @count = $file_extension =~ /\./g;
                my $check_dots = @count;
                while ($check_dots > 2) {
                    $file_extension = substr($file_extension,index($file_extension,".")+1);
                    $file_extension = substr($file_extension,index($file_extension,"."));
                    @count = $file_extension =~ /\./g;
                    $check_dots = @count;
                }
            }
            #foreach my $file_type (@expected_files) {
            #foreach my $file_type (keys %file_extensions) {
            my @all_extensions = get_all_package_file_extensions();
            my $hash_key = substr($item,index($item,$global_var{"PACKAGE-NAME"})+length($global_var{"PACKAGE-NAME"})+1);
            $hash_key = substr($hash_key,0,rindex($hash_key,"/"));
            debug_print("      HASK KEY: $hash_key"); # HASK KEY: /base_stability_traffic/traffic_scripts/k6/
            if ((exists $package_info{$hash_key}{"NR-ENTRIES"}) || (exists $package_info{$hash_key}{"NR-SUBDIR-ENTRIES"})){
                debug_print("Direct hash entry found, lookig for: $file_extension");
                #my $nr_entries = $package_info{$hash_key}{"NR-ENTRIES"};
                my $nr_entries = get_package_nr_entries($hash_key,1);
                my $expected_filesize;
                while ($nr_entries) {
                    if ($package_info{$hash_key}{"DIR-CONTENTS-".$nr_entries}{"EXTENSION"} eq $file_extension) {
                        $expected_filesize = $package_info{$hash_key}{"DIR-CONTENTS-".$nr_entries}{"EXPECTED-SIZE"};
                        last;
                    } else {
                        my $alt_file_extension = substr($file_extension,index($file_extension,".")+1);
                        $alt_file_extension = substr($alt_file_extension,index($alt_file_extension,"."));
                        if ($package_info{$hash_key}{"DIR-CONTENTS-".$nr_entries}{"EXTENSION"} eq $alt_file_extension) {
                            $expected_filesize = $package_info{$hash_key}{"DIR-CONTENTS-".$nr_entries}{"EXPECTED-SIZE"};
                        }
                    }
                    $nr_entries--;
                }
                my ($min,$max,$min_in_bytes,$max_in_bytes) = min_max_size($expected_filesize);

                my $working_size = bytes_to_text($size);

                if (($size >= number_of_bytes($min)) && ($size <= number_of_bytes($max))) {
                    if (number_of_bytes($min) == number_of_bytes($max)) {
                        my_print("Filesize $working_size is expected size[$min] for $item");
                    } else {
                        my_print("Filesize $working_size within expected limits".sprintf("[%6s - %6s]",$min,$max)." for $item");
                    }
                    $total += $size;
                } else {
                    my_print("Filesize $working_size NOT within expected limits[$min - $max] for $item",$color_info{"ERR_Color"}); exit 0;
                    if ($global_var{"NO-DOWNLOAD"}) {
                        exit_script($ERROR_CODE{"Standard"});
                    }
                }
            } else {
                debug_print("Scanning hash entries for file extension");
                foreach my $file_type (@all_extensions) {
                    $curr_pos = index($file_extension,$file_type);
                    if ($curr_pos != -1) {
                        $file_extension = substr($file_extension,$curr_pos);
                        if ($file_extension eq $file_type) {
                            my $expected_filesize;
                            foreach my $key (keys %package_info) {
                                    #my $nr_entries = $package_info{$key}{"NR-ENTRIES"};
                                    my $nr_entries = get_package_nr_entries($key,1);
                                    #my $test = 1;
                                    #while (exists $package_info{$key}{"DIR-CONTENTS-".$test}) {
                                    #    $test++;
                                    #}
                                    #$test--;
                                    #my_print("NR_ENTRIES FOUND BY SCANNING: ".$test);
                                    while ($nr_entries) {
                                        if ($package_info{$key}{"DIR-CONTENTS-".$nr_entries}{"EXTENSION"} eq $file_type) {
                                            $expected_filesize = $package_info{$key}{"DIR-CONTENTS-".$nr_entries}{"EXPECTED-SIZE"};
                                            last;
                                        }
                                        $nr_entries--;
                                    }
                            }
                            my ($min,$max,$min_in_bytes,$max_in_bytes) = min_max_size($expected_filesize);
                            my $working_size = $size;
                            my $scale = 0;
                            my @scales = ("B", "KB", "MB", "GB", "TB", "PB");
                            while ($working_size >= 1024) {
                                $working_size = $working_size / 1024;
                                $scale++;
                            }
                            $working_size = sprintf("%7.2f %-2s",$working_size,$scales[$scale]);

                            if (($size >= number_of_bytes($min)) && ($size <= number_of_bytes($max))) {
                                if (number_of_bytes($min) == number_of_bytes($max)) {
                                    my_print("Filesize $working_size is expected size[$min] for $item");

                                } else {
                                    my_print("Filesize $working_size within expected limits".sprintf("[%6s - %6s]",$min,$max)." for $item");
                                }
                                $total += $size;
                            } else {
                                my_print("Filesize $working_size NOT within expected limits[$min - $max] for $item",$color_info{"ERR_Color"});
                                if ($global_var{"NO-DOWNLOAD"}) {
                                    exit_script($ERROR_CODE{"Standard"});
                                }
                            }
                            last;
                        }
                    }
                }
            }
        }
    }
    debug_print("end get_tree_size() : $total",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $total;
}

# *****************************
# *                           *
# * Determine diskspace used  *
# *                           *
# *****************************
sub get_directory_size {
    my $directory   = $_[0];
    debug_print("sub get_directory_size($directory)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $directory_size_in_MB = sprintf("%d",get_tree_size($directory)/1048576);
    debug_print("end get_directory_size() : $directory_size_in_MB",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $directory_size_in_MB;
}


sub print_elapsed_time {
    my %parameters  = %{$_[0]};
    debug_print("sub print_elapsed_time(".$parameters{"start"}.",".$parameters{"end"}.",".$parameters{"reason"}.")",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $str = sprintf("Time for %-50s : ",$parameters{"reason"});
    my $elapsed_time = tv_interval($parameters{"start"},$parameters{"end"});

    debug_print("elapsed time in seconds: ".$elapsed_time);
    if ($elapsed_time < 90) {
        $str .= sprintf("%2d seconds",$elapsed_time);
    } else {
                                                                    # if more than 90 seconds, example: 120
        my $used_minutes = sprintf("%d",$elapsed_time/60);          # used_minutes = 2.00
        my $remaining_seconds = $elapsed_time - (60*$used_minutes); # remaining_seconds = 120 - (60*2) = 120 - 120 = 0
        $str .= sprintf("%2d minutes and %2d seconds",$used_minutes,$remaining_seconds);
    }
    my_print($str,$color_info{"TIME_Color"});
    $timer{"STORED"}{$str} = $elapsed_time;
    debug_print("end print_elapsed_time()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
}

sub check_upload_package {
    my $package_location = $_[0];
    debug_print("sub check_upload_package($package_location)",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    if ($global_var{"UPLOAD-DIR"}) {
        my_print("Initiate upload of package from: $package_location");
        if (($global_var{"SOURCE-DIRECTORY"}) && ($global_var{"NO-DOWNLOAD"})) {
            $package_location = $global_var{"SOURCE-DIRECTORY"};
        }
        $timer{"time_upload_start"} = [gettimeofday()];
        upload_package($package_location);
        $timer{"time_upload_end"} = [gettimeofday()];
        print_elapsed_time({"start" => $timer{"time_upload_start"},"end" => $timer{"time_upload_end"},"reason" => "upload of package"});
        my_print("Upload of package completed");
    }
    debug_print("end check_upload_package()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
}


sub package_within_size_limits {
    my $package_location = $_[0];
    debug_print("sub package_within_size_limits($package_location)",$GLOBAL_DEBUG{"INDENT-INCREASE"});
    my $return_value = $ERROR_CODE{"NormalExit"};

    my $size = get_directory_size($package_location);
    my $print_size;
    $size /= 1024;   # Calculate size in GB, received size is in MB
    $print_size = sprintf("%.2f",$size);
    if (($size > $global_var{"PACKAGE-MINSIZE"}) &&
        ($size < $global_var{"PACKAGE-MAXSIZE"})) {
        my_print("Package size $print_size"."GB is within expected range ".$global_var{"PACKAGE-MINSIZE"}."GB - ".$global_var{"PACKAGE-MAXSIZE"}."GB");
    } else {
        my_print("Package size $print_size"."GB NOT within expected range: ".$global_var{"PACKAGE-MINSIZE"}."GB - ".$global_var{"PACKAGE-MAXSIZE"}."GB",$color_info{"ERR_Color"});
        $return_value = $ERROR_CODE{"Package_size"};
    }
    debug_print("end package_within_size_limits() : $return_value",$GLOBAL_DEBUG{"INDENT-DECREASE"});
    return $return_value;
}

# load package files at remote site
# load package files stored local
# compare the lists and report files which are not stored locally
sub compare_local_against_remote {
    my %parameters          = %{$_[0]};

    my $package      = $parameters{"PACKAGE"};
    my $remote_site  = $parameters{"REMOTE-SITE"};
    my $local_dir    = $parameters{"LOCAL-DIR"};

    my $desired_setting_silent_on = $global_var{"SILENT-ON"};
    $global_var{"SILENT-ON"} = 1; # turn on silent during this subroutine

    my $packagename_change; # dummy var in this subroutine, normally used to detect a change in found package naming
    my @remote_stored_files;
    ($packagename_change, @remote_stored_files) = prepare_for_download($remote_site,$package,"skip-create-dirs");


    my @local_stored_files = list_dir_content($local_dir."/".$package);

    my @not_on_local;
    my @not_on_remote;

    foreach my $remote_file_record (@remote_stored_files) {
        my $found_this_file;
        my $remote_file_work = get_filename($remote_file_record);
        $remote_file_work = substr($remote_file_work,length($remote_site)+length($package)+1);

        foreach my $local_file (@local_stored_files) {
            my $local_file_work = substr($local_file,length($local_dir)+1+length($package)+1);
            if ($local_file_work eq $remote_file_work) {
                $found_this_file = 1;
                last;
            }
        }
        if (!$found_this_file) {

            if (exists $package_info{$remote_file_work}{"MANDATORY"}) {
                push(@not_on_local,$remote_file_work);
            }
        }
    }
    foreach my $local_file (@local_stored_files) {
        my $found_this_file;
        my $local_file_work = substr($local_file,length($local_dir)+1+length($package)+1);

        foreach my $remote_file_record (@remote_stored_files) {
            my $remote_file_work = get_filename($remote_file_record);
            $remote_file_work = substr($remote_file_work,length($remote_site)+length($package)+1);
            if ($remote_file_work eq $local_file_work) {
                $found_this_file = 1;
                last;
            }
        }
        if (!$found_this_file) {
            if ($local_file_work) {
                push(@not_on_remote,$local_file_work);
            }
        }
    }
    $global_var{"SILENT-ON"} = $desired_setting_silent_on; # restore silent at end of this subroutine
    if (@not_on_local) {
        my_print("Following remote files are not present at local directory");
        foreach my $local_file (@not_on_local) {
            my_print("   $local_file");
        }
    }
    if (@not_on_remote) {
        my_print("Following local files are not present at remote repository");
        foreach my $remote_file (@not_on_remote) {
            my_print("   $remote_file");
        }
    }
    if ((!@not_on_local) && (!@not_on_remote)) {
        my_print("Content of remote and local are the same");
    }

    return(@not_on_local);

}

sub reload_files {
    my %parameters          = %{$_[0]};
    if (exists $parameters{"FILE"}) {
        my $file_to_download        = $parameters{"FILE"};
        my $package                 = (exists $parameters{"PACKAGE"}       ? $parameters{"PACKAGE"}        : $global_var{"PACKAGE-NAME"});
        my $remote_site             = (exists $parameters{"REMOTE-SITE"}   ? $parameters{"REMOTE-SITE"}    : $repo_info{"GENERIC"}{"URL-TO-USE"});
        my $local_dir               = (exists $parameters{"LOCAL-DIR"}     ? $parameters{"LOCAL-DIR"}      : $global_var{"TARGET-DIRECTORY"}."/".$global_var{"PACKAGE-NAME"}."/");
        $parameters{"PACKAGE"}      = $package;
        $parameters{"REMOTE-SITE"}  = $remote_site;
        $parameters{"LOCAL-DIR"}    = $local_dir;

        $file_to_download = substr($file_to_download,index($file_to_download,$package));
        my_print("Reloading file : $file_to_download");
        my $desired_setting_silent_on = $global_var{"SILENT-ON"};
        my $desired_fast_find         = $global_var{"USE-FAST-FIND"};
        $global_var{"SILENT-ON"}      = 1; # turn on silent during this subroutine
        $global_var{"USE-FAST-FIND"}  = 1; # this should be valid for automated tests using eiffelesc

        my $packagename_change; # dummy var in this subroutine, normally used to detect a change in found package naming
        my @remote_stored_file_info;
        my $fast_find = find_package_fast($remote_site, $package);
        my @download_these_files;
        if (!$fast_find) {
            # Since we didn´t find the package this way (perhaps duplicate packages exist) let's try the slower search
            $global_var{"USE-FAST-FIND"}  = 0; # clear retry for a fast search in subroutine prepare_for_download
            ($packagename_change, @remote_stored_file_info) = prepare_for_download($remote_site,$package,"skip-create-dirs");
        } else {
            $parameters{"DIRECTORY"}    = $fast_find;
            $parameters{"ADJUST"}       = 1;
            $parameters{"SITE"}         = $remote_site;
            @remote_stored_file_info = read_directory(\%parameters);
        }

        foreach my $remote_file_info (@remote_stored_file_info) {
            if (index(get_filename($remote_file_info),$file_to_download) != -1) {
                push(@download_these_files,$remote_file_info);
            }
        }

        $global_var{"SILENT-ON"}        = $desired_setting_silent_on; # restore silent setting
        if (@download_these_files) {
            download_package(\@download_these_files,$package,"skip-files-size-check");
        }

        $global_var{"USE-FAST-FIND"}    = $desired_fast_find;
    }
}

# Check local stored CSAR against artifactory CSAR
#   Local       Artifactory         Action
#   Unsigned    Unsigned            Do nothing
#   Unsigned    Unsigned + Signed   Delete local Unsigned and download Signed
#   Signed      Unsigned + Signed   Do nothing
sub package_check {
    my %parameters          = %{$_[0]};
    my $package      = $parameters{"PACKAGE"};
    my $remote_site  = $parameters{"REMOTE-SITE"};
    my $local_dir    = $parameters{"LOCAL-DIR"};

    my_print("Performing check on signed/unsigned packages");
    my $desired_setting_silent_on = $global_var{"SILENT-ON"};
    my $desired_csar_only         = $global_var{"COPY-ONLY-CSAR"};
    my $desired_fast_find         = $global_var{"USE-FAST-FIND"};
    my $old_repo                  = $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"};

    $global_var{"COPY-ONLY-CSAR"} = 0; # We are only interested in the .csar files
    $global_var{"SILENT-ON"}      = 1; # turn on silent during this subroutine
    $global_var{"USE-FAST-FIND"}  = 1; # this should be valid for automated tests using eiffelesc

    my $packagename_change; # dummy var in this subroutine, normally used to detect a change in found package naming
    my @remote_stored_file_info;
    my $fast_find = find_package_fast($remote_site, $package);
    if (!$fast_find) {
        # Since we didn´t find the package this way let's try the slower search
        $global_var{"USE-FAST-FIND"}  = 0; # clear retry for a fast search in subroutine prepare_for_download
        ($packagename_change, @remote_stored_file_info) = prepare_for_download($remote_site,$package,"skip-create-dirs");
    } else {
        $parameters{"DIRECTORY"}    = $fast_find."csar/";
        $parameters{"ADJUST"}       = 1;
        $parameters{"SITE"}         = $remote_site;
        @remote_stored_file_info = read_directory(\%parameters);
    }

    my @local_stored_files = list_dir_content($local_dir."csar/");
    my %local_files;
    my @download_these_files;
    my @delete_these_files;
    my $download_missing_csar=1;
    my $csar_checksum_count=0;

    $global_var{"SILENT-ON"}        = $desired_setting_silent_on; # restore silent setting

    if ($old_repo ne $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}) {
        my_print("   INFO: Repo info changed from $old_repo to ".$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"});
    }

    $global_var{"LOCAL-CSAR-TYPE-SIGNED"}   = 0;
    $global_var{"LOCAL-DELETE-SIGNED"}      = 0;
    $global_var{"LOCAL-CSAR-TYPE-UNSIGNED"} = 0;
    $global_var{"LOCAL-DELETE-UNSIGNED"}    = 0;
    foreach my $local_file (@local_stored_files) {
        if (-f $local_file) {
            my $stripped_filename = substr($local_file,1+rindex($local_file,"/"));
            $local_files{$stripped_filename} = $local_file;
            if (index($stripped_filename,".csar") != -1) {
                # CSAR related file
                if (index($stripped_filename,".csar.") == -1) {
                    # this is the .csar file itself
                    $global_var{"CSAR-NAME"} = $stripped_filename;
                    if (index($stripped_filename,"unsigned") != -1) {
                        $global_var{"LOCAL-CSAR-TYPE-UNSIGNED"} = 1;
                        $global_var{"CSAR-NAME-UNSIGNED"} = $stripped_filename;
                        my_print("   Found Local CSAR  : UNSIGNED");
                        $global_var{"LOCAL-CSAR-TYPE"} = "UNSIGNED";
                        $download_missing_csar = 0;
                    } else {
                        $global_var{"LOCAL-CSAR-TYPE-SIGNED"} = 1;
                        $global_var{"CSAR-NAME-SIGNED"} = $stripped_filename;
                        my_print("   Found Local CSAR  : SIGNED");
                        $global_var{"LOCAL-CSAR-TYPE"} = "SIGNED";
                        $download_missing_csar = 0;
                    }
                } else {
                    # this is a CSAR checksum file
                    $csar_checksum_count++;
                }
            }
        }
    }

    if (!$download_missing_csar) {
        if ($global_var{"LOCAL-CSAR-TYPE"} eq "UNSIGNED") {
            if ($csar_checksum_count < 2) {
                $download_missing_csar = 2;
            }
        }
        if ($global_var{"LOCAL-CSAR-TYPE"} eq "SIGNED") {
            if ($csar_checksum_count < 2) {
                $download_missing_csar = 3;
            }
        }
    }

my_print("Local csar files:");
foreach my $key (keys %local_files) {
    if (index($key,".csar") != -1) {
        my_print("   $key       ".$local_files{$key});
    }
}
    if ($global_var{"LOCAL-CSAR-TYPE-SIGNED"} && $global_var{"LOCAL-CSAR-TYPE-UNSIGNED"}) {
        my_print("Both signed and unsigned package are stored local, this will lead to problems");
        if (!$global_var{"UNSIGNED"}) {
            my_print("Delete the unsigned version from local directory");
            $global_var{"LOCAL-DELETE-SIGNED"} = 1;
            $global_var{"LOCAL-CSAR-TYPE"} = "SIGNED";
            $global_var{"CSAR-NAME"} = $global_var{"CSAR-NAME-SIGNED"};
        } else {
            my_print("Delete the signed version from local directory due to parameter --unsigned");
            $global_var{"LOCAL-DELETE-UNSIGNED"} = 1;
            $global_var{"LOCAL-CSAR-TYPE"} = "UNSIGNED";
            $global_var{"CSAR-NAME"} = $global_var{"CSAR-NAME-UNSIGNED"};
        }
    }

    $global_var{"REMOTE-CSAR-NAME-UNSIGNED"} = "";
    $global_var{"REMOTE-CSAR-NAME-SIGNED"}   = "";
    foreach my $remote_file (@remote_stored_file_info) {
        # load the remote filename
        my $file = get_filename($remote_file);
        # strip directory information to keep only the filename
        my $stripped_filename = substr($file,1+rindex($file,"/"));
        if (substr($stripped_filename,index($stripped_filename,".csar")+5,1) ne ".") {
            # this is the .csar file itself
            if (index($stripped_filename,"unsigned") != -1) {
                my_print("   Found Remote CSAR : UNSIGNED");
                $global_var{"REMOTE-CSAR-NAME-UNSIGNED"} = $remote_file;
            } else {
                my_print("   Found Remote CSAR : SIGNED");
                $global_var{"REMOTE-CSAR-NAME-SIGNED"} = $remote_file;
            }
        }

# $global_var{"UNSIGNED"}  $global_var{"LOCAL-CSAR-TYPE"}   Remote              Download        Delete local
#           0                   SIGNED                          UNSIGNED            -               -
#           0                   UNSIGNED                        UNSIGNED            -               -
#           0                   SIGNED                          SIGNED              -               UNSIGNED
#           0                   UNSIGNED                        SIGNED              SIGNED          UNSIGNED
#           1                   SIGNED                          UNSIGNED            UNSIGNED        SIGNED
#           1                   UNSIGNED                        UNSIGNED            -
#           1                   SIGNED                          SIGNED              -
#           1                   UNSIGNED                        SIGNED              -
        if (!$global_var{"UNSIGNED"}) {
my_print("Check if remote $stripped_filename needs to be deleted or downloaded ".$local_files{$stripped_filename});
            if (exists $local_files{$stripped_filename}) {
my_print("   File exists local");
                if (index($stripped_filename,"unsigned") != -1) {
                    my_print("Add file to delete list ".$local_files{$stripped_filename});
                    push(@delete_these_files,$local_files{$stripped_filename});                         # The files are only deleted after downloading the signed package
                }
            } else {
my_print("   File does not exist local");
                if (index($stripped_filename,"unsigned") == -1) {
                    if (substr($stripped_filename,index($stripped_filename,".csar")+5,1) ne ".") {
                        my_print("   DOWNLOAD AND REPLACE ".$global_var{"CSAR-NAME"}." WITH : ".$stripped_filename);
                        $global_var{"LOCAL-CSAR-TYPE"} = "";
                    }
                    push(@download_these_files,$remote_file);
                }
            }
        } else {
            if (exists $local_files{$stripped_filename}) {
                if (index($stripped_filename,"unsigned") == -1) {
                    push(@delete_these_files,$local_files{$stripped_filename});                         # The files are only deleted after downloading the unsigned package
                }
            } else {
                if (index($stripped_filename,"unsigned") != -1) {
                    if (substr($stripped_filename,index($stripped_filename,".csar")+5,1) ne ".") {
                        my_print("   DOWNLOAD AND REPLACE ".$global_var{"CSAR-NAME"}." WITH : ".$stripped_filename." Due to: ".$global_var{"UNSIGNED-TEXT"});
                        $global_var{"LOCAL-CSAR-TYPE"} = "";
                    }
                    push(@download_these_files,$remote_file);
                }
            }
        }
    }

    if ($download_missing_csar) {
        # the local csar is missing, download it
        if ($download_missing_csar == 1) {
            my_print("   WARNING LOCAL CSAR MISSING");
            $global_var{"UNSIGNED-TEXT"} = "Missing CSAR file";
        } else {
            my_print("   WARNING LOCAL CSAR MISSING CHECKSUM FILE(S)");
            $global_var{"UNSIGNED-TEXT"} = "Missing CSAR Checksum file(s)";
        }
        if (($global_var{"REMOTE-CSAR-NAME-SIGNED"}) && (!$global_var{"UNSIGNED"})) {
            my $file = get_filename($global_var{"REMOTE-CSAR-NAME-SIGNED"});
            my $stripped_filename = substr($file,1+rindex($file,"/"));

            push(@download_these_files,$global_var{"REMOTE-CSAR-NAME-SIGNED"});
            push(@download_these_files,$global_var{"REMOTE-CSAR-NAME-SIGNED"}.".md5");
            push(@download_these_files,$global_var{"REMOTE-CSAR-NAME-SIGNED"}.".sha256");
            push(@download_these_files,$global_var{"REMOTE-CSAR-NAME-SIGNED"}.".sha1");
            @delete_these_files = ();
            $global_var{"CSAR-NAME"} = $stripped_filename;
            my_print("   DOWNLOADING SIGNED ".$global_var{"CSAR-NAME"});
        } elsif ($global_var{"REMOTE-CSAR-NAME-UNSIGNED"}) {
            my $file = get_filename($global_var{"REMOTE-CSAR-NAME-UNSIGNED"});
            my $stripped_filename = substr($file,1+rindex($file,"/"));

            push(@download_these_files,$global_var{"REMOTE-CSAR-NAME-UNSIGNED"});
            push(@download_these_files,$global_var{"REMOTE-CSAR-NAME-UNSIGNED"}.".md5");
            push(@download_these_files,$global_var{"REMOTE-CSAR-NAME-UNSIGNED"}.".sha256");
            @delete_these_files = ();
            $global_var{"CSAR-NAME"} = $stripped_filename;
            my_print("   DOWNLOADING UNSIGNED ".$global_var{"CSAR-NAME"});
        } else {
            my_print("   COULD NOT DETERMINE REMOTE CSAR !!!");
        }
    }

    if (@download_these_files) {
        my $csar_max_size = substr($package_info{"csar"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"},index($package_info{"csar"}{"DIR-CONTENTS-1"}{"EXPECTED-SIZE"},"-")+1);
        check_free_drivespace({"TARGET"         => substr($global_var{"TARGET-DIRECTORY"},0,index($global_var{"TARGET-DIRECTORY"},"/",1)) ,
                               "NEEDED"         => number_of_bytes($csar_max_size) ,
                               "LOWER-LIMIT"    => "4048" ,
                               "ACTION"         => "CHECK" ,
                               "ACTION-FAIL"    => "EXIT-SCRIPT"
                             });
        download_package(\@download_these_files,$package,"skip-files-size-check");
        foreach my $delete_file (@delete_these_files) {
            my_print("   Deleting: $delete_file");
            `rm -f $delete_file`;
        }
        $global_var{"SYNC"} = 1;
    } elsif (@delete_these_files) {
        my_print("   No files to download, but there are files marked for deletion");
        foreach my $delete_file (@delete_these_files) {
            my_print("   Deleting: $delete_file");
            `rm -f $delete_file`;
        }
        $global_var{"SYNC"} = 1;
    } else {
        my_print("   NO ACTION NEEDED");
    }

    $global_var{"USE-FAST-FIND"}    = $desired_fast_find;
    $global_var{"COPY-ONLY-CSAR"}   = $desired_csar_only;
    my_print("Check on signed/unsigned packages finished");
}


sub check_free_drivespace {
    my %parameters  = %{$_[0]};

    my $free_dir            = (exists $parameters{"TARGET"}         ? $parameters{"TARGET"}         : "");
    my $bytes_needed        = (exists $parameters{"NEEDED"}         ? $parameters{"NEEDED"}         : "");
    my $bytes_lower_limit   = (exists $parameters{"LOWER-LIMIT"}    ? $parameters{"LOWER-LIMIT"}    : "100000");
    my $action              = (exists $parameters{"ACTION"}         ? $parameters{"ACTION"}         : "REPORT");  # REPORT or CHECK (free - $parameters{"NEEDED"}
    my $action_fail         = (exists $parameters{"ACTION-FAIL"}    ? $parameters{"ACTION-FAIL"}    : "");  # EXIT-SCRIPT

    my $return_value = 0;

    my @free_space = split("\n",`df -H | grep $free_dir`);

    foreach my $line (@free_space) {
        my $drive = substr($line,rindex($line," ")+1);
        if ($action eq "CHECK") {
            if ($drive eq $free_dir) {
                my @drive_info = split(" ",$line);
                my $bytes_free = number_of_bytes($drive_info[-3]);
                if (($bytes_free - $bytes_needed) > $bytes_lower_limit) {
                    my_print("Enough free diskspace (".$bytes_free." Bytes), continue download process");
                } else {
                    my_print("NOT ENOUGH FREE DISKSPACE (free space $bytes_free Bytes) FOR DOWNLOAD (variable PACKAGE-MAXSIZE = $bytes_needed Bytes)");
                    if ($action_fail eq "EXIT-SCRIPT") {
                        exit_script($ERROR_CODE{"Standard"});
                    }
                }
                last;
            }
        } elsif ($action eq "REPORT") {
            if ($free_dir) {
                my $drive = substr($line,rindex($line," ")+1);
                if ($drive eq $free_dir) {
                    my @drive_info = split(" ",$line);
                    my $bytes_free = number_of_bytes($drive_info[-3]);
                    my_print("Free space on $drive = $bytes_free Bytes");
                    last;
                }
            } else {
                my_print($line);
            }
        } elsif ($action eq "GET") {
            if ($free_dir) {
                my $drive = substr($line,rindex($line," ")+1);
                if ($drive eq $free_dir) {
                    my @drive_info = split(" ",$line);
                    $return_value = number_of_bytes($drive_info[-3]);
                    last;
                }
            } else {
                my $drive = substr($line,rindex($line," ")+1);
                my @drive_info = split(" ",$line);
                $return_value += number_of_bytes($drive_info[-3]);
            }
        }
    }

    return $return_value;
}

sub determine_repo_to_use {
    my $link = $_[0];
    my $repo_changed = 0;
    my $old_repo = $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"};

    my $test_link = $global_var{"SITE"};
    my $use_default = 0;
    if ($link) {
        if ($link ne "DEFAULT") {
            $test_link = $link;
        } else {
            $use_default = 1;
            $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}   = $repo_info{"GENERIC"}{"DEFAULT_ARTIFACTORY_TYPE"};  # one of: BSF , SC , CNDSC
            $global_var{"SKIP-CHECKS"}                  = $repo_info{$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}}{"SKIP-FILECHECK"};
            $global_var{"SITE"}                         = $repo_info{"GENERIC"}{"SITE"}.$repo_info{$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}}{"REPOSITORY"}.$repo_info{$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}}{"HOME"};
        }
    }
    if (!$use_default) {
        my_print("Determine which repo to use");
        foreach my $key (keys %repo_info) {
            my_print("  PACKAGE_TYPE : ".$key);
            my_print("  REPO           ".$repo_info{$key}{"REPOSITORY"});
            my_print("  SITE           ".$repo_info{"GENERIC"}{"SITE"}.$repo_info{$key}{"REPOSITORY"}.$repo_info{$key}{"HOME"});
            if ((exists $repo_info{$key}{"REPOSITORY"}) && (index($test_link,$repo_info{$key}{"REPOSITORY"}.$repo_info{$key}{"HOME"}) != -1)) {
                my_print("                 REPO exists AND REPO is part of SITE");
                $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}   = $key;  # one of: BSF , SC , CNDSC
                $global_var{"SKIP-CHECKS"}                  = $repo_info{$key}{"SKIP-FILECHECK"};
                $global_var{"SITE"}                         = $repo_info{"GENERIC"}{"SITE"}.$repo_info{$key}{"REPOSITORY"}.$repo_info{$key}{"HOME"};
                last;
            }
            my_print("  CHECK NEXT PACKAGE_TYPE");
        }
    } else {
        my_print("Using default repo");
    }
    if ($old_repo ne $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}) {
        my_print("Repo info changed from $old_repo to ".$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"});
        my_print("Changed download link to ".$global_var{"SITE"}." for package download");
        $repo_changed = 1;
    } else {
        my_print("Repo info did not change (".$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}.")");
    }
    return $repo_changed;
}

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************
sub load_parameters {
    # Parse command line parameters
    use Getopt::Long;            # standard Perl module
    my $user_site;
    my $color;
    my $unsigned;
    my $allow_logging;
    my $token_as_argument;
    my $dirs_to_download;
    my $show_changes;
    my $skip_fastcheck;
    my $skip_checks;
    my $debug_on = 0;
    my $keep_log = 0;
    my $show_help;
    my $debug_lvl = "STANDARD";
    $global_var{"UPLOAD-ALL"} = 0;
    $global_var{"UPLOAD-OVERWRITE"} = 0;
    my $result = GetOptions (
                            "h|help"                 => \$show_help,
                            "t|target=s"             => \$global_var{"TARGET-DIRECTORY"},
                            "p|package=s"            => \$global_var{"PACKAGE-NAME"},
                            "l|link=s"               => \$user_site,
                            "u|upload=s"             => \$global_var{"UPLOAD-DIR"},
                            "source=s"               => \$global_var{"SOURCE-DIRECTORY"},
                            "artifact-token=s"       => \$token_as_argument,
                            "color=s"                => \$color,
                            "unsigned=s"             => \$unsigned,
                            "copy-csar=s"            => \$global_var{"COPY-ONLY-CSAR"},
                            "sha256"                 => \$global_var{"DO-SHA256"},
                            "get-latest-build"       => \$global_var{"GET_LATEST_BUILD"},
                            "no-md5"                 => \$global_var{"NO-MD5-CHECK"},
                            "no-sizecheck"           => \$global_var{"NO-SIZE-CHECK"},
                            "no-download"            => \$global_var{"NO-DOWNLOAD"},
                            "no-progress"            => \$global_var{"PROGRESS-OFF"},
                            "dry-run"                => \$global_var{"DRYRUN"},
                            "n|noprogress"           => \$global_var{"PROGRESS-OFF"},
                            "d|debug"                => \$debug_on,
                            "debug-level=s"          => \$debug_lvl,
                            "s|silent"               => \$global_var{"SILENT-ON"},
                            "allow-logging"          => \$allow_logging,
                            "keep-log"               => \$keep_log,
                            "download=s"             => \$dirs_to_download,
                            "download-all"           => \$download_all,
                            "upload-all"             => \$global_var{"UPLOAD-ALL"},
                            "upload-overwrite"       => \$global_var{"UPLOAD-OVERWRITE"},
                            "skip-checks"            => \$skip_checks,
                            "change-notes"           => \$show_changes,
                            "sync=s"                 => \$global_var{"SYNC"},
                            "copy-all"               => \$global_var{"COPY-ALL"},
                            "skip_fastcheck"         => \$skip_fastcheck,
                            );

    if ($debug_on) {
        start_debug(uc $debug_lvl);
    }
    if ($global_var{"SOURCE-DIRECTORY"}) {
        $global_var{"TARGET-DIRECTORY"} = $global_var{"SOURCE-DIRECTORY"};
    }

    if ($global_var{"SYNC"}) {
        if ( (substr(uc $global_var{"SYNC"},0,1) eq "Y") || (substr(uc $global_var{"SYNC"},0,1) eq "T") || ($global_var{"SYNC"} == 1) ){
            $global_var{"SYNC"} = 1;
        } else {
            $global_var{"SYNC"} = 0 ;
        }
    }

    if ($global_var{"COPY-ONLY-CSAR"}) {
        if ( (substr(uc $global_var{"COPY-ONLY-CSAR"},0,1) eq "Y") || (substr(uc $global_var{"COPY-ONLY-CSAR"},0,1) eq "T") ){
            $global_var{"COPY-ONLY-CSAR"} = 1;
        } else {
            $global_var{"COPY-ONLY-CSAR"} = 0 ;
        }
    }

    if ($token_as_argument) {
        $token_received_as_argument=1;
        init_curl($token_as_argument);
        $artifact_token = $curl{"CURL"};
    } else {
        init_curl($token_as_argument);
        $artifact_token = $curl{$curl{"USE"}."-TOKEN"};
    }
    if ((uc($color) eq "NO") || (uc($color) eq "NONE") || ($global_var{"SILENT-ON"})) {
        $color_info{"use_color"} = 0;
    }
    $script_name     = $0;
    $script_version  = $REVISION;
    my_print("\n$0 v"."$REVISION");
    if (!$result) {
        show_help();
        my_print("\n====> ERROR: Check parameters <====",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
        exit_script($ERROR_CODE{"Standard"});
    }

    if ($show_help) {
        show_help();
        exit_script($ERROR_CODE{"NormalExit"});
    }
    if ($show_changes) {
        load_change_notes();
        exit_script($ERROR_CODE{"NormalExit"});
    }

    if ((uc(substr($unsigned,0,1)) eq "Y") || (uc(substr($unsigned,0,1)) eq "T")) {
        $global_var{"UNSIGNED"} = 1;
    }
    if (($global_var{"UPLOAD-DIR"}) && (index(uc($global_var{"UPLOAD-DIR"}),"SOLUTION_CI_STORAGE") != -1)) {
        $global_var{"UNSIGNED"} = 1;
    }
    if ($global_var{"UNSIGNED"}) {
        if ($unsigned) {
            my_print("Download of the UNSIGNED .csar has priority due to --unsigned $unsigned");
            $global_var{"UNSIGNED-TEXT"} = "--unsigned $unsigned";
        } else {
            my_print("Download of the UNSIGNED .csar has priority due to detection of upload towards Solution_CI repo");
            $global_var{"UNSIGNED-TEXT"} = "detection of upload towards Solution_CI repo";
        }
    } else {
        my_print("Download of the SIGNED .csar has priority");
    }

    my $contains_up = index($global_var{"TARGET-DIRECTORY"},"..");
    if ($global_var{"TARGET-DIRECTORY"} eq ".") {
        $global_var{"TARGET-DIRECTORY"} = getcwd();
    } elsif ((substr($global_var{"TARGET-DIRECTORY"},0,2) eq "./") && ($contains_up == -1)){
        my $new_target_directory = getcwd()."/".substr($global_var{"TARGET-DIRECTORY"},2);
        $global_var{"TARGET-DIRECTORY"} = $new_target_directory;
    } elsif ($contains_up != -1) {
        #my $up = "..";
        #my $count_ups = () = $global_var{"TARGET-DIRECTORY"} =~ /$up/g;  # count how many times $up occurs in $global_var{"TARGET-DIRECTORY"}
        my @directory_parts = split("/",$global_var{"TARGET-DIRECTORY"});
        my $remaining_part  = "";
        foreach(@directory_parts) {
            if ($_ eq "..") {
                chdir "..";
            } else {
                if ($_ ne ".") {
                    $remaining_part .= "/$_";
                }
            }
        }
        my $new_target_directory = getcwd().$remaining_part;
        $global_var{"TARGET-DIRECTORY"} = $new_target_directory;
    }
    if (substr($global_var{"TARGET-DIRECTORY"},-1) != "/") {
        $global_var{"TARGET-DIRECTORY"} .= "/";
    }
    my_print("target_directory = '".$global_var{"TARGET-DIRECTORY"}."'\n");

    if (substr($global_var{"TARGET-DIRECTORY"},-1) eq "/") {
        $global_var{"TARGET-DIRECTORY"} = substr($global_var{"TARGET-DIRECTORY"},0,length($global_var{"TARGET-DIRECTORY"})-1);
        debug_print("\$global_var{\"TARGET-DIRECTORY\"} = '".$global_var{"TARGET-DIRECTORY"}."'\n");
    }
    $global_var{"SOURCE-DIRECTORY"} = $global_var{"TARGET-DIRECTORY"}.$global_var{"PACKAGE-NAME"}."/";
    if ($allow_logging) {
        $GLOBAL_LOGGING{"log_dir"} = $global_var{"TARGET-DIRECTORY"};
        start_log("/var/tmp/", "download_csar.log", $keep_log);
    }
    debug_print("sub load_parameters()",$GLOBAL_DEBUG{"INDENT-INCREASE"});

    if (!$global_var{"PACKAGE-NAME"}) {
        show_help();
        my_print("\n====> ERROR: Please provide package name <====",$ERROR_CODE{"PackageName"},$color_info{"ERR_Color"});

        exit_script($ERROR_CODE{"PackageName"});
    }
    $global_var{"PACKAGE-RELEASE"} = substr($global_var{"PACKAGE-NAME"},0,index($global_var{"PACKAGE-NAME"},$global_var{"PACKAGE-SEPERATOR"}));  # remove build nr     : 1.10.0+44 => 1.10.0
    $global_var{"PACKAGE-VERSION"} = substr($global_var{"PACKAGE-NAME"},index($global_var{"PACKAGE-NAME"},$global_var{"PACKAGE-SEPERATOR"})+1);  # keep only build nr  : 1.10.0+44 => 44

    # Overview of allowed combinations
    #
    # no-download       |   |   | x | x | x |   |   | x | x |   |   |   |
    # no-md5            |   |   | x | x |   | x |   | x |   | x | x |   |
    # no-sizecheck      |   |   | x |   |   | x | x |   | x | x |   | x |
    # upload            | x | x | x | x | x | x | x |   |   |   |   |   |
    # dry-run           |   | x |   |   |   | x | x |   |   |   |   |   |
    # source            |   |   | x | x | x |   |   |   |   |   |   |   |
    #
    # Overview of NOT allowed combinations, *= Implemented check  N= No effect in script when used anyway
    #
    # no-download       | * | * | * |   |   |   |   |   |   |   |   |   |
    # no-md5            | * | * | * |   |   |   |   |   |   |   |   |   |
    # no-sizecheck      | * | * | * |   |   |   |   |   |   |   |   |   |
    # upload            |   | * | * |   |   |   |   |   |   |   |   |   |
    # dry-run           |   |   | * | N | N |   |   |   |   |   |   |   |
    # source            |   |   |   |   | N |   |   |   |   |   |   |   |
    #
    # Other combinations not mentioned may not make sense and can be added
    # above preferably by using a C for check.

    if (($global_var{"NO-DOWNLOAD"}) && ($global_var{"NO-MD5-CHECK"}) && ($global_var{"NO-SIZE-CHECK"})) {
        if ($global_var{"UPLOAD-DIR"}) {
            if (!$global_var{"SOURCE-DIRECTORY"}) {
                my_print("Upload is only possible in combination with downloading package or using parameter --source",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
                exit_script($ERROR_CODE{"Standard"});
            }
        } else {
            my_print("Useless to skip ALL tasks",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
            exit_script($ERROR_CODE{"Standard"});
        }
    }

    if ($skip_fastcheck) {
        $global_var{"USE-FAST-FIND"} = 0;
    }
    if ($user_site) {
        $global_var{"AUTO-SITE"} = 0; # Mark that the site is received from the user
        # check if / is last character, if not then add a / to the end
        if (substr($user_site,-1) ne "/") {
            $user_site .= "/";
        }

        $repo_info{"GENERIC"}{"URL-TO-USE"} = $user_site;
        if (( $user_site ne $repo_info{"GENERIC"}{"SITE"}.$repo_info{"SC"}{"REPOSITORY"}.$repo_info{"SC"}{"HOME"} ) &&
            ( $user_site ne $repo_info{"GENERIC"}{"SITE"}.$repo_info{"CNDSC"}{"REPOSITORY"}.$repo_info{"CNDSC"}{"HOME"} ) &&
            ( $user_site ne $repo_info{"GENERIC"}{"SITE"}.$repo_info{"CNCS"}{"REPOSITORY"}.$repo_info{"CNCS"}{"HOME"} ) &&
            ( $user_site ne $repo_info{"GENERIC"}{"SITE"}.$repo_info{"SC-GA"}{"REPOSITORY"}.$repo_info{"SC-GA"}{"HOME"} ) &&
            ( $user_site ne $repo_info{"GENERIC"}{"SITE"}.$repo_info{"SOL_CI"}{"REPOSITORY"}.$repo_info{"SOL_CI"}{"HOME"} )) {
            $global_var{"USE-FAST-FIND"} = 0;
        }
    } else {
        $repo_info{"GENERIC"}{"URL-TO-USE"} = $repo_info{"GENERIC"}{"SITE"}.$repo_info{"SC"}{"REPOSITORY"}.$repo_info{"SC"}{"HOME"};
        $user_site = $repo_info{"GENERIC"}{"URL-TO-USE"};
        $global_var{"USE-FAST-FIND"} = 1;
        $global_var{"AUTO-SITE"} = 1; # Mark that the site is set by the script and not received from the user
    }
    $global_var{"SITE"} = $repo_info{"GENERIC"}{"URL-TO-USE"}; # use site provided by user (or auto generated)

    determine_repo_to_use(); # determine_repo_to_use("DEFAULT")
#    if (!$skip_checks) {
#    foreach my $key (keys %repo_info) {
#my_print("PACKAGE_TYPE : ".$key) if ($global_var{"AUTO-SITE"});
#my_print("REPO           ".$repo_info{$key}{"REPOSITORY"}) if ($global_var{"AUTO-SITE"});
#my_print("SITE           ".$global_var{"SITE"}) if ($global_var{"AUTO-SITE"});
#        if ((exists $repo_info{$key}{"REPOSITORY"}) && (index($global_var{"SITE"},$repo_info{$key}{"REPOSITORY"}.$repo_info{$key}{"HOME"}) != -1)) {
#my_print("               REPO exists AND REPO is part of SITE") if ($global_var{"AUTO-SITE"});
#                $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"}   = $key;  # one of: BSF , SC , CNDSC
#                $global_var{"SKIP-CHECKS"}                  = $repo_info{$key}{"SKIP-FILECHECK"};
#                last;
#        }
#my_print("               CHECK NEXT PACKAGE_TYPE") if ($global_var{"AUTO-SITE"});
#    }
    if ($skip_checks) {
        $global_var{"SKIP-CHECKS"}  = 1;
        $global_var{"NO-MD5-CHECK"} = 1;
        $global_var{"NO-SIZE-CHECK"}= 1;
    }

#    }
    my_print("ARTIFACTORY TYPE: ".$repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"});
#    $repo_info{"GENERIC"}{"URL-TO-USE"} = $repo_info{"GENERIC"}{"SITE"}.$repo_info{ $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"} }{"REPOSITORY"}.$repo_info{ $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"} }{"HOME"};
#    $global_var{"SITE"} = $repo_info{"GENERIC"}{"URL-TO-USE"};
    $global_var{"SITE-ROOT"} = $repo_info{ $repo_info{"GENERIC"}{"ARTIFACTORY_TYPE"} }{"HOME"};
    # Check if last character is a '\' if not then append it
    if (substr($global_var{"SITE"},-1) ne "/") {
        $global_var{"SITE"} .= "/";
    }
    if ($user_site ne $repo_info{"GENERIC"}{"URL-TO-USE"}) {
        my_print("User-Site : $user_site  NOT EQUAL TO ".$repo_info{"GENERIC"}{"URL-TO-USE"}."  => CLEARING USE-FAST-FIND");
        $global_var{"USE-FAST-FIND"} = 0;
        $repo_info{"GENERIC"}{"URL-TO-USE"} = $user_site;
#        $global_var{"SITE"} = $user_site;
    }

    if ($dirs_to_download) {
        if (uc($dirs_to_download) eq "ALL") {
            $download_all = 1;
        } else {
            $global_var{"DOWNLOAD-ONLY"} = 1;
            my @mark_dirs = split(",",$dirs_to_download);
            # First mark all directories as not to be copied
            foreach my $all_key (keys %package_info) {
                $package_info{$all_key}{"COPY"} = 0;
                # mark all files as not mandatory  $package_info{<dir>}{"DIR-CONTENTS-x"}{"MANDATORY"}
                set_package_mandatory($all_key,0);
            }
            # Now mark the directories that are requested by user to copy
            foreach my $copy_key (@mark_dirs) {
                $package_info{$copy_key}{"COPY"} = 1;
                set_package_mandatory($copy_key,1);  # mark files to copy as mandatory  $package_info{<dir>}{"DIR-CONTENTS-x"}{"MANDATORY"}
            }
        }
    }
    if ($global_var{"UPLOAD-DIR"}) {
        if (!$global_var{"COPY-ALL"}) {
            foreach my $all_key (keys %package_info) {
                $package_info{$all_key}{"COPY"} = 1;
            }
        }
    }
    debug_print("end load_parameters()",$GLOBAL_DEBUG{"INDENT-DECREASE"});
}

# **************
# *            *
# * Main logic *
# *            *
# **************
$timer{"time_script_start"} = [gettimeofday()];
my $time_download_start;
my $time_md5_start;

load_parameters();                                  # Parse command line parameters

{

    debug_print("Used/Received parameters  Either a string or '0': NO or '1': YES");
    debug_print("-t|--target            => \"".$global_var{"TARGET-DIRECTORY"}."\"");
    debug_print("-p|--package           => \"".$global_var{"PACKAGE-NAME"}."\"");
    debug_print("-l|--link              => \"".$global_var{"SITE"}."\"");
    debug_print("-u|--upload            => \"".$global_var{"UPLOAD-DIR"}."\"");
    debug_print("--source               => \"".$global_var{"SOURCE-DIRECTORY"}."\"");
    debug_print("--artifact-token       => \"".substr($artifact_token,0,4)."...".substr($artifact_token,-4)."\"");
    debug_print("--color                => ".$color_info{"use_color"});
    debug_print("--sha256               => ".$global_var{"DO-SHA256"});
    debug_print("--no-md5               => ".$global_var{"NO-MD5-CHECK"});
    debug_print("--no-sizecheck         => ".$global_var{"NO-SIZE-CHECK"});
    debug_print("--no-download          => ".$global_var{"NO-DOWNLOAD"});
    debug_print("--no-progress          => ".$global_var{"PROGRESS-OFF"});
    debug_print("--dry-run              => ".$global_var{"DRYRUN"});
    debug_print("-n|--noprogress        => ".$global_var{"PROGRESS-OFF"});
    debug_print("-d|--debug             => ".use_debug());
    debug_print("-s|--silent            => ".$global_var{"SILENT-ON"});
    debug_print("--keep-log             => ".(exists $GLOBAL_LOGGING{"keeplog"} ? $GLOBAL_LOGGING{"keeplog"} : "0"));
    debug_print("--download-all         => $download_all");
    debug_print("--sync                 => ".$global_var{"SYNC"});
    debug_print("USE-FAST-FIND          => ".$global_var{"USE-FAST-FIND"}."\n");

    debug_print("Let's start.....\n\n");
}

# Variables used
my @files_to_download;                              # list of files to download
my $path_to_package=$global_var{"TARGET-DIRECTORY"}."/".$global_var{"PACKAGE-NAME"}."/";
my $packagename_change="N";

debug_print("\$path_to_package = $path_to_package");
if ((index(lc$global_var{"PACKAGE-NAME"},"latest") != -1) || ($global_var{"GET_LATEST_BUILD"})){
    my_print("Searching latest package version");
    $global_var{"PACKAGE-NAME"} = find_latest($global_var{"SITE"},$global_var{"PACKAGE-NAME"});
    #repo_info{"GENERIC"}{"URL-TO-USE"}
    $path_to_package=$global_var{"TARGET-DIRECTORY"}."/".$global_var{"PACKAGE-NAME"};
    if ($global_var{"GET_LATEST_BUILD"}) {
        my_print("Latest available package: ".$global_var{"PACKAGE-NAME"});
        exit 0;
    }
    $global_var{"PACKAGE-RELEASE"} = substr($global_var{"PACKAGE-NAME"},0,index($global_var{"PACKAGE-NAME"},$global_var{"PACKAGE-SEPERATOR"}));  # remove build nr     : 1.10.0+44 => 1.10.0
    $global_var{"PACKAGE-VERSION"} = substr($global_var{"PACKAGE-NAME"},index($global_var{"PACKAGE-NAME"},$global_var{"PACKAGE-SEPERATOR"})+1);  # keep only build nr  : 1.10.0+44 => 44
}
if (!$global_var{"COPY-ONLY-CSAR"}) {
    my_print("checking if package is present at $path_to_package");
    if (-d $path_to_package) {
        if (!is_directory_empty($path_to_package)) {
            my $failure_detected;
            $timer{"time_package_check_start"} = [gettimeofday()];
            my %parameters;
                $parameters{"PACKAGE"}      = $global_var{"PACKAGE-NAME"};
                $parameters{"REMOTE-SITE"}  = $repo_info{"GENERIC"}{"URL-TO-USE"};
                $parameters{"LOCAL-DIR"}    = $path_to_package;
            package_check(\%parameters);
            $timer{"time_package_check_end"} = [gettimeofday()];
            print_elapsed_time({"start" => $timer{"time_package_check_start"},"end" => $timer{"time_package_check_end"},"reason" => "Checking signed/unsigned packages"});

            if ($global_var{"SYNC"}) {
                my %csar_info;
                $timer{"time_sync_start"} = [gettimeofday()];
                my_print("Package already exists in $path_to_package");
                my_print("Synchonize package with artifactory and check validity");
                my_print("Check downloaded checksum files against the checksum files on artifactory");
                my $desired_setting_silent_on = $global_var{"SILENT-ON"};
                my @local_md5_checksum_files = split("\n",`find $path_to_package | grep .md5`);
                my @local_sha256_checksum_files = split("\n",`find $path_to_package | grep .sha256`);
                my @local_all_files = split("\n",`find $path_to_package`);
                my @remote_md5_checksum_files;
                my @remote_sha256_checksum_files;
                my $packagename_change; # dummy var in this subroutine, normally used to detect a change in found package naming
                my @remote_directory_content;
                my_print("Load remote directory content");
                $global_var{"SILENT-ON"} = 1;
                ($packagename_change, @remote_directory_content) = prepare_for_download($repo_info{"GENERIC"}{"URL-TO-USE"},$global_var{"PACKAGE-NAME"},"skip-create-dirs");

                foreach (@local_all_files) {
                    if (index($_,".csar") != -1) {
                        if (index($_,"unsigned") != -1) {
                            if (!$csar_info{"LOCAL-UNSIGNED"}) {
                                my_print("Found Local CSAR  : UNSIGNED");
                            }
                            $csar_info{"LOCAL-UNSIGNED"} = 1;
                        } else {
                            if (!$csar_info{"LOCAL-SIGNED"}) {
                                my_print("Found Local CSAR  : SIGNED");
                            }
                            $csar_info{"LOCAL-SIGNED"} = 1;
                        }
                    }
                }
                foreach (@remote_directory_content) {
                    if (index($_,".csar") != -1) {
                        if (index($_,"unsigned") != -1) {
                            if (!$csar_info{"REMOTE-UNSIGNED"}) {
                                my_print("Found Remote CSAR : UNSIGNED");
                            }
                            $csar_info{"REMOTE-UNSIGNED"} = 1;
                        } else {
                            if (!$csar_info{"REMOTE-SIGNED"}) {
                                my_print("Found Remote CSAR : SIGNED");
                            }
                            $csar_info{"REMOTE-SIGNED"} = 1;
                        }
                    }
                }

                $global_var{"SILENT-ON"} = $desired_setting_silent_on;
                my_print("Load all local stored .sha256 files from: $path_to_package");
                # Add md5 checksum files to the sha256 files
                push(@local_sha256_checksum_files,@local_md5_checksum_files);
                my_print("check contents for all local checksum files against corresponding remote checksum files");
                my @sync_these_files;
                my @sync_these_files2;
                foreach my $local_entry (@local_sha256_checksum_files) {
                    open(FILE, $local_entry) or die "Can't read file 'filename' [$!]\n";
                    my $local_content = <FILE>;
                    close (FILE);
                    my $local_parent = substr($local_entry,0,index($local_entry,".sha256"));
                    foreach my $remote_entry (@remote_directory_content) {
                        if (get_indicator($remote_entry) eq "F") {
                            my $remote_file = get_filename($remote_entry);
                            my $check_this = substr($local_entry,index($local_entry,$global_var{"PACKAGE-NAME"})); # strip everything upto the package from the filename
                            if (index($remote_file, $check_this) != -1) {
                                # check files against eachother
                                my $remote_content = send_curl_command("$remote_file --silent" , $global_var{"CURL-MAXTIME-DOWNLOAD"});
                                if ($local_content ne $remote_content) {
                                    # download the missing file
                                    #$failure_detected = 1; # force download
#$check_this = 1.10.40+22/base_stability_traffic/traffic_simulators/docker/eric-atmoz-sftp-1.13.25-1186.tar.sha256

                                    my_print("Change in checksum file detected for ".$check_this);
                                    my $already_in_list;
                                    foreach my $sync_entry (@sync_these_files) {
                                        my $sync_file = get_filename($sync_entry);
                                        if ($sync_file eq $remote_file) {
                                            $already_in_list = 1;
                                            last;
                                        }
                                    }
                                    if (!$already_in_list) {
                                        push(@sync_these_files,$remote_entry);
                                        debug_print("File : ".$remote_file." Not found in local directory, added to list of files to be downloaded");
                                        #if (index($local_entry,".md5") != -1) {
                                            push(@sync_these_files,substr($remote_entry,0,index($remote_entry,".sha256")));
                                            debug_print("File : ".$sync_these_files[-1]." Not found in local directory, added to list of files to be downloaded");
                                        #}
                                    }
                                }
                            }
                        }
                    }
                }
                if (@sync_these_files) {
                    my_print("One or more files do not have the same checksum, download these files again");
                }

                my_print("check if any remote files are missing");
                foreach my $remote_entry (@remote_directory_content) {
                    if (get_indicator($remote_entry) eq "F") {
                        my $remote_file = get_filename($remote_entry);
                        my $this_is_checksum;
                        #if (index($remote_file,".sha256") == -1) {
                            if (index($remote_file,".sha256") != -1) {
                                $this_is_checksum = 1;
                            }
                            my $file_found;
                            foreach my $local_entry (@local_all_files) {
                                if (-f $local_entry) {
                                    # strip everything upto the package from the filename '+length($global_var{"PACKAGE-NAME"})'
                                    my $check_this = substr($local_entry,index($local_entry,$global_var{"PACKAGE-NAME"}));
                                    if ($check_this ne $global_var{"PACKAGE-NAME"}."/") {
                                        if (index($remote_file, $check_this) != -1) {
                                            if ($this_is_checksum) {
                                                if (index($local_entry,".sha256") != -1) {
                                                    $file_found = 1;
                                                    last;
                                                }
                                            } else {
                                                $file_found = 1;
                                                last;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!$file_found) {
                                # Check in record %package_info if the "COPY" flag is set to 1, if so add the file to the list
                                my $key = substr($remote_file,index($remote_file,$global_var{"PACKAGE-NAME"})+length($global_var{"PACKAGE-NAME"})+1);
                                $key = substr($key,0,rindex($key,"/"));
                                if ( (exists $package_info{$key}{"COPY"}) && ($package_info{$key}{"COPY"} == 1) ) {
                                    if (index($remote_entry,".csar") == -1) {
                                        push(@sync_these_files2,$remote_entry);
                                    }
                                } else {
                                    my @split_key = split("/",$key);
                                    my $check_key = "";
                                    foreach my $this_key (@split_key) {
                                        if (!$check_key) {
                                            $check_key = $this_key;
                                        } else {
                                            $check_key .= "/$this_key";
                                        }
                                        if ( (exists $package_info{$check_key}{"COPY"}) && ($package_info{$check_key}{"COPY"} == 1) ) {
                                            if (exists $package_info{$check_key}{"NR-SUBDIR-ENTRIES"}) {
                                                if (index($remote_entry,".csar") == -1) {
                                                    push(@sync_these_files2,$remote_entry);
                                                    #push(@sync_these_files2,$remote_entry.".sha256");
                                                    debug_print("File : ".$remote_file." Not found in local directory, added to list of files to be downloaded");
                                                    last;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        #}
                    }
                }
                if (@sync_these_files2) {
                    my_print("One or more files are new on artifactory, download these files");
                    push(@sync_these_files,@sync_these_files2);
                }
                $global_var{"SILENT-ON"} = $desired_setting_silent_on;
                #if ($failure_detected) {
                if (@sync_these_files) {
                    my_print("Downloading missing/changed files");
                    if (use_debug()) {
                        foreach (@sync_these_files) {
                            my_print("   $_");
                        }
                    }
                    $timer{"time_sync_download_start"} = [gettimeofday()];
                    download_package(\@sync_these_files,$global_var{"PACKAGE-NAME"},"skip-files-size-check");
                    $timer{"time_sync_download_end"} = [gettimeofday()];
                    print_elapsed_time({"start" => $timer{"time_sync_download_start"},"end" => $timer{"time_sync_download_end"},"reason" => "download synced files"});

                    my_print("Checking validity of updated package");
                } else {
                    my_print("No missing/changed files found in compare with artifactory.");
                    my_print("Checking validity of package");
                }
                $timer{"time_sync_end"} = [gettimeofday()];
                print_elapsed_time({"start" => $timer{"time_sync_start"},"end" => $timer{"time_sync_end"},"reason" => "synchronizing local files with remote"});
            } else {
                my_print("Package already exists, checking validity");
                if (all_files_present($path_to_package) != $ALL_PRESENT) {
                    if ($global_var{"NO-DOWNLOAD"}) {
                        exit_script($ERROR_CODE{"Missing_file"});
                    }
                    $failure_detected = 1;
                }
            }

            if (!$failure_detected) {
                if (!$global_var{"SKIP-CHECKS"}) {
                    my $md5_failed = 0;
                    my $md5_file;
                    $timer{"time_local_md5_start"} = [gettimeofday()];
                    my %md5_info = find_md5_file($path_to_package);
                    if (exists $md5_info{"MD5_TARGET_FILENAME-1"}) {
                        $md5_file = $md5_info{"FILEPATH-1"}.$md5_info{"MD5_TARGET_FILENAME-1"};
                    }
                    #$md5_file = find_md5_file($path_to_package);
                    if (!$md5_file) {
                        $md5_failed = $ERROR_CODE{"Missing_file"};
                    } else {
                        $md5_failed = checksum_check($md5_file);
                        if ( (!$md5_failed) && (exists $md5_info{"MD5_TARGET_FILENAME-2"}) ) {
                            $md5_file = $md5_info{"FILEPATH-2"}.$md5_info{"MD5_TARGET_FILENAME-2"};
                            $md5_failed = checksum_check($md5_file);
                        }
                    }
                    $timer{"time__local_md5_end"} = [gettimeofday()];
                    print_elapsed_time({"start" => $timer{"time_local_md5_start"},"end" => $timer{"time_local_md5_end"},"reason" => "local stored md5 check"});

                    if ($md5_failed) {
                        my_print("MD5 checksum failed");
                        $failure_detected=$md5_failed;
                        if (($md5_failed == $ERROR_CODE{"Missing_file"}) && ($global_var{"NO-DOWNLOAD"})) {
                            my_print("Aborting due to missing file",$ERROR_CODE{"Missing_file"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Missing_file"});
                        }
                        if (($md5_failed == $ERROR_CODE{"Failed_md5"}) && ($global_var{"NO-DOWNLOAD"})) {
                            my_print("Aborting due to failing md5 checksum",$ERROR_CODE{"Failed_md5"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Missing_file"});
                        }
                    }
                    $timer{"time_local_sha256_start"} = [gettimeofday()];
                    my $sha_failed = check_all_sha256("$path_to_package");
                    $timer{"time__local_sha256_end"} = [gettimeofday()];
                    print_elapsed_time({"start" => $timer{"time_local_sha256_start"},"end" => $timer{"time__local_sha256_end"},"reason" => "local stored sha256 check"});

                    if ($sha_failed) {
                        my_print("SHA256 checksum failed");
                        $failure_detected=$sha_failed;
                        debug_print("Error Code: $sha_failed   --no-download = ".$global_var{"NO-DOWNLOAD"});
                        if (($sha_failed == $ERROR_CODE{"Missing_file"}) && ($global_var{"NO-DOWNLOAD"})) {
                            my_print("Aborting due to missing file",$ERROR_CODE{"Missing_file"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Missing_file"});
                        }
                        if (($sha_failed == $ERROR_CODE{"Failed_md5"}) && ($global_var{"NO-DOWNLOAD"})) {
                            my_print("Aborting due to failing sha256 checksum",$ERROR_CODE{"Failed_md5"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Missing_file"});
                        }

                    }
                    my $size_failed = package_within_size_limits($path_to_package);
                    if ($size_failed) {
                        if (!$global_var{"NO-DOWNLOAD"}) {
                            # One or more checks failed download the package again
                            my_print("Repeating download of package");
                        } else {
                            my_print("Aborting due to incorrect package size",$ERROR_CODE{"Package_size"},$color_info{"ERR_Color"});
                            exit_script($ERROR_CODE{"Package_size"});
                        }
                    } else {
                        if (!$failure_detected) {
                            my_print("\nCheck of package completed\n");

                            check_upload_package($path_to_package);
                            exit_script($ERROR_CODE{"NormalExit"});
                        } else {
                            if ($global_var{"NO-DOWNLOAD"}) {
                                my_print("Aborting, error code: $failure_detected",$failure_detected,$color_info{"ERR_Color"});
                                my_print("parameter '--no-download' prevents download of package",$color_info{"BLUE_Color"});
                                exit_script($failure_detected);
                            }
                        }
                    }
                } else {
                    check_upload_package($path_to_package);
                    exit_script($ERROR_CODE{"NormalExit"});
                }
            }
        } elsif ($global_var{"NO-DOWNLOAD"}) {
            my_print("Option --no-download given, directory available but package not downloaded: $path_to_package",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
            exit_script($ERROR_CODE{"Standard"});
        }
    } elsif ($global_var{"NO-DOWNLOAD"}) {
        my_print("Option --no-download given, no package available at: $path_to_package",$ERROR_CODE{"Standard"},$color_info{"ERR_Color"});
        exit_script($ERROR_CODE{"Standard"});
    } else {
        my_print("Package not present at $path_to_package proceeding with download from artifactory");
    }
    $global_var{"CHECK-MISSING-FILES"}  = 1;
} else {
    $global_var{"NO-MD5-CHECK"}         = 1;
    $global_var{"NO-SIZE-CHECK"}        = 1;
    $global_var{"CHECK-MISSING-FILES"}  = 0;
}

if (!$global_var{"NO-DOWNLOAD"}) {
    $timer{"time_download_start"} = [gettimeofday()];
    my_print("Check if there is enough space on target location");
    check_free_drivespace({"TARGET"         => substr($global_var{"TARGET-DIRECTORY"},0,index($global_var{"TARGET-DIRECTORY"},"/",1)) ,
                       "NEEDED"         => number_of_bytes($global_var{"PACKAGE-MAXSIZE"}."G") ,
                       "LOWER-LIMIT"    => "4048" ,
                       "ACTION"         => "CHECK" ,
                       "ACTION-FAIL"    => "EXIT-SCRIPT"
                     });

    my_print("Using ".$global_var{"SITE"}." for package download");
    $timer{"time_download_start"} = [gettimeofday()];
    ($packagename_change, @files_to_download) = prepare_for_download($global_var{"SITE"},$global_var{"PACKAGE-NAME"});   # Check if package is available on artifactory
    if ($packagename_change ne "N") {
        debug_print("Changing packagename towards: $packagename_change");
        $global_var{"PACKAGE-NAME"} = $packagename_change;
        $path_to_package=$global_var{"TARGET-DIRECTORY"}."/".$global_var{"PACKAGE-NAME"};
    }
my_print("Start download of files");
    download_package(\@files_to_download,$global_var{"PACKAGE-NAME"});              # Package is available, download it
    $timer{"time_download_end"} = [gettimeofday()];
    print_elapsed_time({"start" => $timer{"time_download_start"},"end" => $timer{"time_download_end"},"reason" => "download package"});
    if ($global_var{"CHECK-MISSING-FILES"}) {
        my $missing_downloads = all_files_present("$path_to_package");    # Check if package is complete
        if ($missing_downloads != $ALL_PRESENT) {
            my_print("There are missing files");
            exit_script($ERROR_CODE{"Missing_file"});
        }
        $timer{"time_sha256_start"} = [gettimeofday()];
        my $failed = check_all_sha256("$path_to_package");
        $timer{"time_sha256_end"} = [gettimeofday()];
        print_elapsed_time({"start" => $timer{"time_sha256_start"},"end" => $timer{"time_sha256_end"},"reason" => "downloaded sha256 check"});

        if ($failed) {
            exit_script($failed);
        }
    } else {
        my_print("--copy-csar Yes  ==>  Skipping missing-files and sha256 check for ".$global_var{"PACKAGE-NAME"});
    }
    my_print("Package download completed");
} else {
    my_print("--no_download  ==>  Skipping download of ".$global_var{"PACKAGE-NAME"});
}

if (!$global_var{"NO-MD5-CHECK"}) {
    foreach my $md5_file (@md5_files) {
        if (!$md5_file) {                                                   # md5 check on downloaded package
            # md5 file not defined, could due to parameter no_download
            # check if target directory contains a md5 file
            my %md5_info = find_md5_file($path_to_package);
            if (exists $md5_info{"MD5_TARGET_FILENAME"}) {
                $md5_file = $md5_info{"FILEPATH"}.$md5_info{"MD5_TARGET_FILENAME"};
            }
            #$md5_file = find_md5_file($path_to_package);
            if ($md5_file) {
                my_print "Found md5 file: $md5_file";
            } else {
                my_print("md5 file is missing",$ERROR_CODE{"Missing_file"},$color_info{"ERR_Color"});
                exit_script($ERROR_CODE{"Missing_file"});
            }
        }
        $timer{"time_md5_start"} = [gettimeofday()];
        my $failed = checksum_check($md5_file);  # if the md5 check fails the script will return with an exit code
        $timer{"time_md5_end"} = [gettimeofday()];
        print_elapsed_time({"start" => $timer{"time_md5_start"} , "end" => $timer{"time_md5_end"},"reason" => "downloaded md5 check"});
        if ($failed) {
            exit_script($failed);
        }
    }
} else {
    my_print("--no_md5  ==>  Skipping md5 check for ".$global_var{"PACKAGE-NAME"});
}

if (!$global_var{"NO-SIZE-CHECK"}) {
    my $size_result = package_within_size_limits($path_to_package);
    if ($size_result) {
        exit_script($ERROR_CODE{"Package_size"});
    }
} else {
    my_print("--no_usage  ==>  Skipping size check for ".$global_var{"PACKAGE-NAME"});
}

if (@check_package_info) {
    my_print("PLEASE CHECK RECORD \%package_info inside script download_csar.pl for following entries:");
    foreach (@check_package_info) {
        my_print("   ".$_);
    }
}
if (@add_to_package_info) {
    my_print(" ");
    my_print("Perhaps add following to record \%package_info:");
    my_print(" ");
    foreach my $record_entry (@add_to_package_info) {
        my_print("    # ".$record_entry."  (automatic generated)");
        my_print("    \$package_info{\"$record_entry\"}{\"VALID-RELEASES\"}          = \"".$package_info{$record_entry}{"VALID-RELEASES"}."\";");
        my_print("    \$package_info{\"$record_entry\"}{\"VALID-RELEASE_TYPES\"}     = \"".$package_info{$record_entry}{"VALID-RELEASE_TYPES"}."\";");
        my_print("    \$package_info{\"$record_entry\"}{\"MANDATORY\"}               = ".$package_info{$record_entry}{"MANDATORY"}.";");
        my_print("    \$package_info{\"$record_entry\"}{\"COPY\"}                    = ".$package_info{$record_entry}{"COPY"}.";");
        my_print("    \$package_info{\"$record_entry\"}{\"NR-ENTRIES\"}              = ".$package_info{$record_entry}{"NR-ENTRIES"}.";");
        my $nr = 1;
        while ($nr <= $package_info{$record_entry}{"NR-ENTRIES"}) {
            my_print("    \$package_info{\"$record_entry\"}{\"DIR-CONTENTS-$nr\"}{\"TYPE\"}           = \"".$package_info{$record_entry}{"DIR-CONTENTS-$nr"}{"TYPE"}."\";");
            my_print("    \$package_info{\"$record_entry\"}{\"DIR-CONTENTS-$nr\"}{\"EXTENSION\"}      = \"".$package_info{$record_entry}{"DIR-CONTENTS-$nr"}{"EXTENSION"}."\";");
            my_print("    \$package_info{\"$record_entry\"}{\"DIR-CONTENTS-$nr\"}{\"MANDATORY\"}      = ".$package_info{$record_entry}{"DIR-CONTENTS-$nr"}{"MANDATORY"}.";");
            my_print("    \$package_info{\"$record_entry\"}{\"DIR-CONTENTS-$nr\"}{\"EXPECTED-SIZE\"}  = \"".$package_info{$record_entry}{"DIR-CONTENTS-$nr"}{"EXPECTED-SIZE"}."\";");
            $nr++;
        }
    }
}

my_print("\nDownload and check of package completed\n");

check_upload_package($path_to_package);

exit_script($ERROR_CODE{"NormalExit"});



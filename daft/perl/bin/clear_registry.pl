#!/usr/bin/perl

################################################################################
#
#  Author   : eedwiq
#
#  Revision : 2.51
#  Date     : 2023-10-17 10:45:19
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2023
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
use Cwd qw(abs_path cwd);
use File::Basename qw(dirname basename);
use Time::HiRes qw(gettimeofday tv_interval);
use Term::ANSIColor;
use lib dirname(dirname abs_path $0) . '/lib';
use ADP::Kubernetes_Operations;

# ********************************
# *                              *
# * Global Variable declarations *
# *                              *
# ********************************
my $REVISION = "2.51";
my $debug_on                =  0;
my $silent_on               =  0;
my %registry_info;

# Initialize the parameters for calling the script
    my $show_help           = 0;
    my $use_debug           = 0;
    my $prune_workers       = 0;
    my $registry_user       = "";
    my $registry_password   = "";
    my $registry_port       = "";
    my $namespace           = "";
    my $local_registry      = "";
    my $kubeconfig          = "";

my $use_color               = 1;
my $ERR_Color               = "red blink";
my $INF_Color               = "bold yellow on_black";
my $TIME_Color              = "green";
my $DRYRUN_Color            = "blue";
my $HEADER_Color            = "bright_blue";

my $NORMAL_EXIT             =  0;       # No error, all went well we hope and pray
my $ERR_Standard            =  1;       # Standard return value for errors

my $host                    =  $ENV{'HOST'};
my $std_user                = "admin";

sub show_help {
    print <<EOF;

Description:
============

This script can either list or clear the local registry.
During cleanup the entries from the provided images.txt file will be removed, if the images.txt file is NOT
provided a template file will be used. It is possible by use of option 'exact-match' to make sure that only
the images exactly matching the ones provided in images.txt (name and version) are removed.

Syntax:
=======

 $0 [<ACTION>][<OPTION>]

    <ACTION> are one or more of the following parameters:

        -h / --help                     Shows this help

        --check-size                    Provide printout of currently FREE and USED registry size


    <OPTION> are one or more of the following parameters:
        --color  <no|none>              No or none (not case-sensitive) will suppress the use of color

        --silent

        --prune-workers                 After clearing the registry and restarting the registry pod, prune the workers

        --registry-user <user>          Provide registry user

        --registry-password <passw>     Provide registry password

        --kubeconfig <file>             If specified then it will use this file as the config file for helm and
                                        kubectl commands.
                                        If not specified then it will use the default file which is normally the
                                        file ~/.kube/config.


Examples:
=========

  Example 1:
  ----------
  Clean registry and prune workers

  $0 --prune-workers


  Example 2:
  ----------
  Clean registry in silent mode

  $0 --silent


  Example 2:
  ----------
  Clean registry in silent mode use provided registry user and password

  $0 --silent --registry-user USER --registry-password PASSWORD


Return code:
============

  $NORMAL_EXIT: Successful
  $ERR_Standard: Standard return value for errors, unspecified error
EOF
}


sub isnum {
    debug_print("isnum($_[0])");

    if ($_[0] eq  $_[0]+0) {
        debug_print("isnum($_[0]) = TRUE (1)");
        return 1;
    }
    my $dec_point_at = index($_[0],".");
    if ($dec_point_at != -1) {
        # Since split on a . did not work for some reason...
        my $intpart = substr($_[0],0,$dec_point_at);
        my $floatpart= substr($_[0],$dec_point_at+1);
        if (($intpart eq $intpart+0) && ($floatpart eq $floatpart+0)) {
            debug_print("isnum($_[0]) = TRUE (1)");
            return 1;
        } elsif (($intpart eq $intpart+0) && ($floatpart+0 == 0)) {
            debug_print("isnum($_[0]) = TRUE (1)");
            return 1;
        }
    }
    debug_print("isnum($_[0]) = FALSE (0)");
    return 0;
}


sub my_print {
    my $info_to_print    = $_[0];
    my $additional_info  = $_[1];  # Either an actual error code, a command or a color
    my $additional_info2 = $_[2];  # a color (example: "red")
    my $error_code;
    my $color_active;
    my $no_lf;

    if ($info_to_print) {
        if ($use_color) {
            print color("reset");
        }

        if ($additional_info) {
            if (!$additional_info2) {
                if (isnum($additional_info)) {
                    $error_code = $additional_info;
                } else {
                    if ($additional_info eq "NOLF") {
                        $no_lf = 1;
                    } else {
                        if ($use_color) {
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
                    if ($use_color) {
                        $color_active = 1;
                        print color($additional_info2);
                    }
                }
            }
        }

        if (!$silent_on) {
            print $info_to_print;
            if (!$no_lf) {
                print "\n";
            }
        } elsif ($error_code) {
            print $info_to_print;
            if (!$no_lf) {
                print "\n";
            }
        }
        if ($color_active) {
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
    my $text    = $_[0];
    my $option  = $_[1];

    if ($debug_on) {
        if ($use_color) {
            print color($INF_Color);
        }
        print "INFO:";
        my_print(" ($line) $text",$option);
    }
}


sub kubectl {
    my $kube_cmd = $_[0];
    debug_print("kubectl($kube_cmd)");
    my $send = "kubectl $kube_cmd";
    my_print("requesting: $send");
    my $return_output = `$send`;
    debug_print("result    : $return_output");
    return($return_output);
}

sub calc_difference {
    my $first_val   = $_[0];
    my $first_ind   = $_[1];
    my $second_val  = $_[2];
    my $second_ind  = $_[3];

    my %factors;
    $factors{"B"}   = 1;
    $factors{"KB"}  = 1000;
    $factors{"MB"}  = 1000*$factors{"KB"};
    $factors{"GB"}  = 1000*$factors{"MB"};
    $factors{"TB"}  = 1000*$factors{"GB"};
    $factors{"PB"}  = 1000*$factors{"TB"};

    $first_val  = $first_val  * $factors{$first_ind};
    $second_val = $second_val * $factors{$second_ind};

    my %diff;
    $diff{"ORIGINAL"} = $first_val - $second_val;
    ($diff{"FORMATED"}{"SIZE"},$diff{"FORMATED"}{"UNIT"}) = format_size($diff{"ORIGINAL"});
}

sub format_size {
    my $size = $_[0];

    my $units;
    if ($size < 1024) {
      $units = "B";
    } elsif ($size < (1024*1024)) {
        $size /= 1024;
        $units = "KB";
    } elsif ($size < (1024*1024*1024)) {
        $size /= (1024*1024);
        $units = "MB";
    } elsif ($size < (1024*1024*1024*1024)) {
        $size /= (1024*1024*1024);
        $units = "GB";
    } else {
        $size /= (1024*1024*1024*1024);
        $units = "TB";
    }
    return ($size,$units);
}
sub determine_registry {
    debug_print("determine_registry()");
    my $result;

    $result = kubectl("get pods --all-namespaces $kubeconfig | grep eric-lcm-container-registry-registry");
    debug_print("   get pods result    : $result");
    my @splitted = split(" ",$result);
    my $return;
    if ($splitted[1]) {
        $return = $splitted[1];
    }
    debug_print("result    : $return");
    my_print("Registry pod: $return");
    return $return;
}


sub get_registry_size{
    debug_print("get_registry_size()");
    my_print("Read registry used and free size");
    my %return_value;
    my $free_size=0;
    my $used_size=0;
    my $used_percent="";
    my $indicator_free="G";
    my $indicator_used="G";

    #my $used=kubectl("-n kube-system exec -it eric-lcm-container-registry-registry-0 $kubeconfig /bin/bash | df /var/");
    my $used=kubectl("-n kube-system exec -i -t ".$registry_info{"POD"}." $kubeconfig -- /usr/bin/df -h");

    my @lines = split('\n',$used);
    my $i=0;
    my $info_at;
    foreach(@lines) {
        if (index($_,"var/lib/registry") != -1) {
            $info_at = $i;
            last;
        }
        $i++;
    }
    if ($info_at) {
        debug_print("processing line[0] content: $lines[0]");
        debug_print("processing line[$info_at] content: $lines[$info_at]");
        my $string = join(' ', split(' ', $lines[$info_at]));                       # Remove extra spaces
        my @breakup = split(' ', $string);                                         # 0: FILESYSTEM  1: 1K-BLOCKS  2: USED BLOCKS  3: FREE BLOCKS  4: USED %   5: MOUNT

        my_print("free: $breakup[3]");
        my_print("used: $breakup[2]");
        $indicator_free = chop($breakup[3]);
        $breakup[3] = sprintf("%2.1f",$breakup[3]);
        $indicator_used = chop($breakup[2]);
        $breakup[2] = sprintf("%2.1f",$breakup[2]);
        $used_size = $breakup[2];
        $free_size = $breakup[3];
        $used_percent=$breakup[4];

    }
#    if ($indicator_used eq "M") {
#        debug_print("Adjust size due to 'M'  old: $used_size  new: ","NOLF");
#        $used_size /= 1000;
#        debug_print("$used_size");
#    }
#    if ($indicator_free eq "M") {
#        debug_print("Adjust size due to 'M'  old: $free_size  new: ","NOLF");
#        $free_size /= 1000;
#        debug_print("$free_size");
#    }

    debug_print("get_registry_size() =>");
    debug_print("    \$free_size       $free_size,");
    debug_print("    \$used_size       $used_size,");
    debug_print("    \$used_percent    $used_percent,");
    debug_print("    \$indicator_free  $indicator_free,");
    debug_print("    \$indicator_used  $indicator_used");
    $return_value{"free_size"}      = $free_size;
    $return_value{"used_size"}      = $used_size;
    $return_value{"used_percent"}   = $used_percent;
    if ($indicator_free ne "B") { $indicator_free .= "B";}
    if ($indicator_used ne "B") { $indicator_used .= "B";}
    $return_value{"indicator_free"} = $indicator_free;
    $return_value{"indicator_used"} = $indicator_used;
    #return ($free_size,$used_size,$used_percent);
    return(%return_value);
}


# *****************************
# *                           *
# * Display elapsed time      *
# *                           *
# *****************************
sub print_elapsed_time {
    my $start_time   = $_[0];   # Time measurement started
    my $end_time     = $_[1];   # Time measurement ended
    my $str_reason   = $_[2];   # Reason or task for which time is calculated

    my $elapsed_time = tv_interval($start_time,$end_time);
    if ($elapsed_time < 90) {
        my_print("Time for $str_reason: $elapsed_time seconds");
    } else {
        my $used_minutes = sprintf("%d",$elapsed_time/60);
        $elapsed_time -= (60*$used_minutes);
        my_print("Time for $str_reason: $used_minutes minutes and $elapsed_time seconds");
    }
}


sub get_port_number {
    debug_print("get_port_number()");
    my $port_num;
    my $reg_port = kubectl("-n ingress-nginx get svc ingress-nginx $kubeconfig");
    my $reg_port_start = index($reg_port,"443:");
    if ($reg_port_start != -1) {
        my $reg_port_len = index($reg_port,"/TCP",$reg_port_start);
        $reg_port_len -= ($reg_port_start+4);
        $reg_port = substr($reg_port,($reg_port_start+4),$reg_port_len);
        if ($reg_port eq  $reg_port+0) {
            $port_num = $reg_port;
        } else {
            debug_print("Port found is not a number: $reg_port");
        }
    }
    if (!$port_num) {
        if ($registry_port) {
            $port_num = $registry_port;
        } else {
            my_print("Could not determine port number for registry, provide registry port",$ERR_Standard,$ERR_Color);
            my_print("with parameter --registry-port",$ERR_Standard,$ERR_Color);
            exit $ERR_Standard;
        }
    }
    my_print("Registry port number: $port_num");
    return $port_num;
}

sub get_worker_ip_addresses {
    debug_print("get_worker_ips()");
    my @ip_addresses;
    my $cmd_output = `kubectl get nodes -l node-role.kubernetes.io/worker -o wide $kubeconfig`;
    my @lines = split('\n',$cmd_output);                       # split cmd_output
    my $nr_of_workers = @lines - 1;                            # first index contains header, remaining are the workers

    for(my $i=0;$i<$nr_of_workers;$i++) {                      # workers info is stored starting at index 1, index 0 is the header
        my $string = join(' ', split(' ', $lines[$i+1]));      # Remove extra spaces
        my @breakup = split( ' ', $string);                    # Split string, IP address is the 5th element/index
        debug_print("ip address worker $i  : $breakup[5]");
        push(@ip_addresses,$breakup[5]);                       # add to ip_addresses
    }
    debug_print("get_worker_ips() => \@ip_addresses @ip_addresses");
    return @ip_addresses;
}


sub get_repo_and_yaml_location{
    my $which_location = $_[0];  # accepted values: "REPO" , "YAML" , "REPO-AND-YAML" | "REPOYAML"
    my $value;
    my %return_hash;
    debug_print("get_repo_and_yaml_location($which_location)");
    if (index($which_location,"REPO") != -1) {
        my_print("find reposity location");
        $value = `sudo -n find / | grep -m 1 'registry/docker/registry/v2/repositories'`;
        $value =~ s/\n//g;
        $return_hash{"REPO"} = $value;
        debug_print("REPO LOCATION: ".$return_hash{"REPO"});
    }
    if (index($which_location,"YAML") != -1) {
        my_print("find yaml location");
        $value = `sudo -n find / | grep -m 1 'registry/config.yml'`;
        $value =~ s/\n//g;
        $return_hash{"YAML"} = $value;
        debug_print("LOCATION REGISTRY YAML: ".$return_hash{"YAML"});
    }
    return (%return_hash);
}

sub get_registry_pod_info {
    my_print("get registry pod info");
    my $kubeconfig = $registry_info{"KUBECONFIG"};
    my $pod_status = `kubectl get pods -A $kubeconfig | grep eric-lcm-container-registry-registry`;
    my $pods_up;
    my $number_of_pods;
    my $pods_location = index($pod_status,"/");
    my $start_pods_up = rindex($pod_status," ",$pods_location);
    my $end_number_of_pods = index($pod_status," ",$pods_location);
    my %pod_info;
    $pods_up = substr($pod_status,$start_pods_up+1,$pods_location-($start_pods_up+1));
    $number_of_pods = substr($pod_status,$pods_location+1,$end_number_of_pods-($pods_location+1));
    $pod_info{"PODS-UP"}        = $pods_up;
    $pod_info{"PODS-TOTAL"}     = $number_of_pods;
    $pod_info{"POD-STATUS"}     = $pod_status;
    if (index(uc($pod_status),"RUNNING") != -1) {
        $pod_info{"POD-IS_RUNNING"} = 1;
    } else {
        $pod_info{"POD-IS_RUNNING"} = 0;
    }
    if ($pods_up eq $number_of_pods) {
        $pod_info{"POD-ALL_GOOD"} = 1;
    } else {
        $pod_info{"POD-ALL_GOOD"} = 0;
    }
    return %pod_info;
}


# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************
sub load_parameters {
    my $check_registry_size = 0;
    my $color;
    # Parse command line parameters

    use Getopt::Long;            # standard Perl module
    my $result = GetOptions (
        "h|help"                => \$show_help,
        "namespace=s"           => \$namespace,
        "check-size"            => \$check_registry_size,
        "registry-password=s"   => \$registry_password,
        "registry-user=s"       => \$registry_user,
        "p|prune-workers"       => \$prune_workers,
        "silent"                => \$silent_on,
        "color=s"               => \$color,
        "kubeconfig=s"          => \$kubeconfig,
        "registry-port=i"       => \$registry_port,             # Undocumented: Provide port to use
        "d|debug"               => \$debug_on,                  # Undocumented: PRINT EXTRA DEBUG INFORMATION, BE AWARE OF MORE OUTPUT AND LONGER EXECUTION TIMES

        "i|images=s"            => \my $image_list,                # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "c|cleanup"             => \my $cleanup,                   # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "l|list"                => \my $list_entries,              # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "r|remove-empty-dirs"   => \my $remove_empty_dirs,         # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "simulate"              => \my $simulate_only,             # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "exact-match"           => \my $exact_match,               # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "exclude-images"        => \my $exclude_images,            # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "m|multiple-images"     => \my $check_multiple_images,     # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "template-dir=s"        => \my $path_to_images_template,   # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "template-file=s"       => \my $images_template_name,      # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "t|tag=s"               => \my $global_tag,                # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR
        "a|all"                 => \my $list_all_entries,          # NO LONGER USED, KEPT FOR BACKWARD COMPATIBILITY AND NOT GENERATE AN ERROR

    );

    if ((uc($color) eq "NO") || (uc($color) eq "NONE") || ($silent_on)) {
        $use_color = 0;
    } else {
        $use_color = 1;
    }

    if (!$result) {
        show_help();
        my_print("\n====> ERROR: Check parameters <====",$ERR_Standard,$ERR_Color);
        exit $ERR_Standard;
    }

    if ($show_help) {
        show_help();
        exit $NORMAL_EXIT;
    }

    if ($kubeconfig ne "" && -f "$kubeconfig") {
        $kubeconfig = "--kubeconfig=$kubeconfig";
        $registry_info{"KUBECONFIG"} = $kubeconfig;
    } elsif ($kubeconfig ne "") {
        print "The kubeconfig file does not exist\n";
        exit $ERR_Standard;
    }

    if ($check_registry_size) {
        $registry_info{"POD"}   = determine_registry();
        my %receive_values      = get_registry_size();
        my $check_free          = $receive_values{"free_size"};
        my $check_used          = $receive_values{"used_size"};
        my $check_percent       = $receive_values{"used_percent"};

        my_print("Current registry size:");
        my_print(sprintf("   FREE : %3.2d",$check_free).$receive_values{"indicator_free"});
        my_print(sprintf("   USED : %3.2d",$check_used).$receive_values{"indicator_used"});
        exit $NORMAL_EXIT;
    }

    debug_print("Debug info switched ON");
}

# **************
# *            *
# * Main logic *
# *            *
# **************
my $start_time;
my $request_result;
my $user;
my $pass;
my $registry_port;
my %returned_values;
my @available_images;
my $prompt = $ENV{'HOST'};
my @check_start_of_string = ();
$prompt =~ s/-director//;                                    # remove -director part of prompt => flexikube-mc-ipv4-5576-director1   becomes flexikube-mc-ipv4-5576
$prompt =~ s/director-//;                                    # remove director- part of prompt => director-0-n200-vpod1-dsc8991-ipv6 becomes 0-n200-vpod1-dsc8991-ipv6
@check_start_of_string = split('-',$prompt);
if (isnum($check_start_of_string[0])) {
    $prompt = substr($prompt,index($prompt,"-")+1);         # remove <num>- part of prompt    => 0-n200-vpod1-dsc8991-ipv6 becomes n200-vpod1-dsc8991-ipv6
}

print "\n$0 v"."$REVISION\n";
$start_time = [gettimeofday()];
load_parameters();                  # Parse command line parameters
$registry_info{"DIRECTORY"}         = "/var/lib/registry";
$registry_info{"DELETE-SUBDIR"}     = "/v2/";
$registry_info{"POD"}               = determine_registry();
$registry_info{"PORT"}              = get_port_number();
%returned_values = get_repo_and_yaml_location("REPO-AND-YAML");
$registry_info{"LOCATION"}{"REPO"} = $returned_values{"REPO"};
$registry_info{"LOCATION"}{"YAML"} = $returned_values{"YAML"};

%returned_values = get_registry_size();
$registry_info{"INITIAL-SIZE"}{"FREE"}          = $returned_values{"free_size"};
$registry_info{"INITIAL-SIZE"}{"USED"}          = $returned_values{"used_size"};
$registry_info{"INITIAL-SIZE"}{"PERCENT"}       = $returned_values{"used_percent"};
$registry_info{"INITIAL-SIZE"}{"FREE-UNIT"}     = $returned_values{"indicator_free"};
$registry_info{"INITIAL-SIZE"}{"USED-UNIT"}     = $returned_values{"indicator_used"};

my_print("check registry usage before: USED: ".$registry_info{"INITIAL-SIZE"}{"USED"}." ".$returned_values{"indicator_used"}."  FREE: ".$registry_info{"INITIAL-SIZE"}{"FREE"}." ".$returned_values{"indicator_free"}."\n");

# Remove rigistry directory and restart registry pod
$request_result = kubectl("-n kube-system exec -i -t ".$registry_info{"POD"}." $kubeconfig -- ls -R ".$registry_info{"DIRECTORY"});
my @entries = split('\n',$request_result);
my %available_v2_directories;
my $v2_counter=0;
foreach (@entries) {
    if (index($_,$registry_info{"DELETE-SUBDIR"}) != -1) {
        my $v2_dir = substr($_,0,index($_,$registry_info{"DELETE-SUBDIR"})+length($registry_info{"DELETE-SUBDIR"}));
        if (!exists($available_v2_directories{$v2_dir})) {
            $available_v2_directories{$v2_dir} = $v2_dir;
            $v2_counter++;
        }
    }
}
if ($v2_counter > 1) {
    my_print("v2 subdirs found:");
} else {
    my_print("v2 subdir found:");
}
foreach my $key (keys %available_v2_directories) {
    my_print("   ".$key);
    $registry_info{"DIRECTORY"} = $key;
    my_print("Removing registry directory: ".$registry_info{"DIRECTORY"});
    $request_result = kubectl("-n kube-system exec -i -t ".$registry_info{"POD"}." $kubeconfig -- rm -rf cd ".$registry_info{"DIRECTORY"});
    my_print("$request_result");
}

my_print("Restart registry pod",$HEADER_Color);
$request_result = kubectl("delete pod ".$registry_info{"POD"}." -n kube-system $kubeconfig");
my_print("$request_result");
# After deleting the pod, wait until it restarts...
# check if registry pod is up and running
my_print("Check if pod is up and running ","NOLF");
my $pod_start = [gettimeofday()];
my $pod_status; # = `kubectl get pods -A $kubeconfig | grep eric-lcm-container-registry-registry`;
my %pod_info = get_registry_pod_info();
$pod_status = $pod_info{"POD-STATUS"};
my_print("Checking if current podstatus : ".$pod_info{"PODS-UP"}."/".$pod_info{"PODS-TOTAL"}." reaches ".$pod_info{"PODS-TOTAL"}."/".$pod_info{"PODS-TOTAL"});
debug_print("kubectl get pods -A $kubeconfig | grep eric-lcm-container-registry-registry    ====>");
debug_print("$pod_status\n<====");
while( (!$pod_info{"POD-IS_RUNNING"}) && (!$pod_info{"POD-ALL_GOOD"}) ) {
    sleep(1);
#    $pod_status = `kubectl get pods -A $kubeconfig | grep eric-lcm-container-registry-registry`;
    %pod_info = get_registry_pod_info();
    $pod_status = $pod_info{"POD-STATUS"};
    debug_print("kubectl get pods -A $kubeconfig | grep eric-lcm-container-registry-registry    ====>");
    debug_print("$pod_status\n<====");
    if (tv_interval($pod_start,[gettimeofday()]) > 180) {
        $pod_status = "TIMEOUT";
        last;
    }
    if ($use_color) {
        my_print(".","NOLF");
    }
}
my_print(".");
if ($pod_status eq "TIMEOUT") {
    my_print("Timeout after 180 sec, pod '".$registry_info{"POD"}."' did not come back",$ERR_Standard,$ERR_Color);
    exit $ERR_Standard;
}
$registry_info{"POD"} = determine_registry();
debug_print("Registry pod is up and running again: ".$registry_info{"POD"});

my $container_command = ADP::Kubernetes_Operations::get_docker_or_nerdctl_command(1);  # Hide status and warning/error messages from the user
my $container_images = "$container_command image list";
my_print("Reguesting: $container_images");
$request_result = `$container_images`;
my_print("Result    : \n$request_result");
my @image_list = split('\n',$request_result);
my $list_size = scalar @image_list;
my_print("number of images in list: ".$list_size-1);
if ($list_size >= 2) {
    # k8s-registry.eccd.local:30902/proj-document-database-pg-release/data/eric-data-document-database-brm13  => k8s-registry.eccd.local:30902
    my $container_host = substr($image_list[1],0,index($image_list[1],"/proj"));
    my $do_a_container_push = 1;
    foreach my $image (@image_list) {
        if (index($image,"brm13") != -1) {
            $do_a_container_push = 0;
            last;
        }
    }
    if ($do_a_container_push) {
        my $container_push = "$container_command push ".$container_host."/proj-document-database-pg-release/data/eric-data-document-database-brm13";
        my_print("Requesting: $container_push");
        $request_result = `$container_push`;
        my_print("Result    : $request_result");
    }
}

my_print("check change in registry size",$HEADER_Color);
%returned_values = get_registry_size();
$registry_info{"FINISHED-SIZE"}{"FREE"}         = $returned_values{"free_size"};
$registry_info{"FINISHED-SIZE"}{"USED"}         = $returned_values{"used_size"};
$registry_info{"FINISHED-SIZE"}{"PERCENT"}      = $returned_values{"used_percent"};
$registry_info{"FINISHED-SIZE"}{"FREE-UNIT"}    = $returned_values{"indicator_free"};
$registry_info{"FINISHED-SIZE"}{"USED-UNIT"}    = $returned_values{"indicator_used"};

($registry_info{"SIZE"}{"DIFFERENCE"} , $registry_info{"SIZE"}{"DIFFERENCE-UNIT"}) = calc_difference($registry_info{"INITIAL-SIZE"}{"USED"},$registry_info{"INITIAL-SIZE"}{"USED-UNIT"},$registry_info{"FINISHED-SIZE"}{"USED"},$registry_info{"FINISHED-SIZE"}{"USED-UNIT"});


#$registry_info{"SIZE"}{"DIFFERENCE"} = $registry_info{"INITIAL-SIZE"}{"USED"} - $registry_info{"FINISHED-SIZE"}{"USED"};
my_print("Registry before  USED: ".$registry_info{"INITIAL-SIZE"}{"USED"}." ".$registry_info{"INITIAL-SIZE"}{"USED-UNIT"}." (using ".$registry_info{"INITIAL-SIZE"}{"PERCENT"}.")");
my_print("Registry after   USED: ".$registry_info{"FINISHED-SIZE"}{"USED"}." ".$registry_info{"FINISHED-SIZE"}{"USED-UNIT"}." (using ".$registry_info{"FINISHED-SIZE"}{"PERCENT"}.")");
my_print("Difference           : ".$registry_info{"SIZE"}{"DIFFERENCE"}." ".$registry_info{"SIZE"}{"DIFFERENCE-UNIT"});

if ($prune_workers) {
    my_print("");
    my_print("Prune workers:",$HEADER_Color);
    my @ips = get_worker_ip_addresses();
    #my @worker_prune_result;
    my $ssh_result;

    foreach(@ips) {
        my_print("     PRUNE : $_");
        $ssh_result = `ssh -o "StrictHostKeyChecking no" -q $_ '$container_command system prune --all -f '`;
        #push(@worker_prune_result,$ssh_result);
        my_print("     $container_command system prune --all -f   RESULT: $ssh_result\n");
        $ssh_result = `ssh -o "StrictHostKeyChecking no" -q $_ 'sudo -n crictl rmi --all'`;
        #push(@worker_prune_result,$ssh_result);
        my_print("     crictl rmi --all               RESULT: $ssh_result\n");
    }
}


print_elapsed_time($start_time,[gettimeofday()],"cleaning the private registry");
exit $NORMAL_EXIT;



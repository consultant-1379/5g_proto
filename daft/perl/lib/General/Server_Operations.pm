package General::Server_Operations;

################################################################################
#
#  Author   : everhel
#
#  Revision : 1.5
#  Date     : 2019-02-08 15:21:27
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2018
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
    check_installed_services
    check_service_handling_new
    check_service_handling_old
    create_service_hash
    parse_wrapper_script
    js_backup
    js_check_dhcp_interface
    js_check_installed_rpms
    js_check_ip_route
    js_configure
    js_restore
    );

use General::Configuration_Templates;
use General::OS_Operations;

# -----------------------------------------------------------------------------
# Based on OS check either the rpm or the dpkg command to see that the used
# services have been installed
#
# Input variables
#     - Hash reference containing OS specific information
#       - Information included in hash:
#           - ID
#           - Version ID
#           - Result of uname
#     - Hash reference containing hashes for each service that will be populated
#       with parts of relevant information
#       - Information included in hash for each service:
#           - Installed RPMs
#           - Service Commands
#           - Used Config Files
#           - Initial Service Status
#           - Service Handler
#
# Returns:
#
# -----------------------------------------------------------------------------
sub check_installed_services {
    my %params = %{$_[0]};

    my $os_info = $params{"os_info"};
    my $services = $params{"service_hash"};

    my @dpkg_info;
    my %installed_files;
    my $rc;
    my @rpm_info;
    my $service;

    if ($$os_info{"id"} eq "ubuntu") {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "dpkg -l",
                "hide-output"   => 1,
                "return-output" => \@dpkg_info,
            }
        );

        if ($rc == 0) {
            foreach (@dpkg_info) {
                foreach $service (sort keys %$services) {
                    if ($_ =~ /((\S+)?$service\S+).+/) {
                        push @{$$services{$service}{"installed_rpms"}}, $1;
                    }
                }
            }
        }
    } else {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "rpm -qa",
                "hide-output"   => 1,
                "return-output" => \@rpm_info,
            }
        );

        if ($rc == 0) {
            foreach (@rpm_info) {
                foreach $service (sort keys %$services) {
                    if ($_ =~ /((\S+)?$service\S+).+/) {
                        push @{$$services{$service}{"installed_rpms"}}, $1;
                    }
                }
            }
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Routine used to fill the provided service hash with information regarding
# configuration files used by each service
#
# Input variables:
#     - Hash reference containing hashes for each service that will be populated
#       with parts of relevant information
#       - Information included in hash for each service:
#           - Installed RPMs
#           - Service Commands
#           - Used Config Files
#           - Initial Service Status
#           - Service Handler
#
# Returns:
# -----------------------------------------------------------------------------
sub check_service_handling_new {
    my %params = %{$_[0]};

    my $services = $params{"service_hash"};

    my @chkconfig_result;
    my $exec_start;
    my $rc;
    my $service;
    my $service_file;
    my @service_used_config_file = ();
    my @systemctl_cat;
    my @systemctl_list;
    my @systemctl_status;

    # Use the chkconfig command to get a list of services that are usually
    # lists the services monitored by xinetd
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "chkconfig",
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@chkconfig_result,
        }
    );

    # If the chkconfig command was successful check the returned array for
    # the specified services, if found set the service handler to xinetd
    # and store the configuration file for the service if it exists in the
    # standard location
    if ($rc == 0) {
        foreach $service (sort keys %$services) {
            foreach (@chkconfig_result) {
                if ($_ =~ /($service)\s*(xinetd|on)/) {
                    # Check for xinetd config files, if it exists add it to
                    # used_config_file key in hash of hash
                    if (-f "/etc/xinetd.d/$service") {
                        $$services{$service}{"service_handler"} = "xinetd";
                        push @{$$services{$service}{"used_config_file"}}, "/etc/xinetd.d/$service";
                    }
                }
            }
        }
    }

    # For each service check systemctl to find the proper name for the service
    # and store in the hash
    foreach $service (sort keys %$services) {
        @systemctl_list = ();

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "systemctl list-unit-files | grep $service",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@systemctl_list,
            }
        );

        if ($rc == 0 && $$services{$service}{"service_handler"} eq "") {
            foreach (sort @systemctl_list) {
                if ($_ =~ /\b((isc-)?$service(d?(-hpa)?|-?server)\.service)\b.+/) {
                    $$services{$service}{"service_handler"} = "$1";
                    last;
                }
            }
        }
    }

    # For each service check if configuration can be found using the systemctl
    # cat command, if not try to find the configuration file from inside the
    # services wrapper script
    foreach $service (sort keys %$services) {
        @systemctl_cat = ();

        if ($service eq "nfs") {
            if (-f "/etc/exports") {
                push @{$$services{$service}{"used_config_file"}}, "/etc/exports";
            }
            next;
        } elsif ($$services{$service}{service_handler} eq "xinetd") {
            next;
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "systemctl cat $$services{$service}{service_handler}",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@systemctl_cat,
            }
        );

        if ($rc == 0) {
            foreach (@systemctl_cat) {
                if ($_ =~ /^#\s?(.*.service)$/) {
                    $service_file = $1;
                } elsif ($_ =~ /(ConditionPathExists|EnvironmentFile)\=[|]?(.*$service(d)?(\.conf)?.*$)/) {
                    if (-f $2) {
                        push @{$$services{$service}{"used_config_file"}}, $2;
                    }
                } elsif ($_ =~ /SourcePath\=(.*$service.*)/) {
                    @service_used_config_file = parse_wrapper_script(
                        {
                            "wrapper_script"   => $1,
                            "used_config_file" => \@service_used_config_file,
                        }
                    );
                    @{$$services{$service}{"used_config_file"}} = @service_used_config_file;
                } elsif ($_ =~ /ExecStart\=(.*$service.*)/) {
                    $exec_start = $1;
                    if ($exec_start =~ /\s(\/.+$service(d)?\.conf)/) {
                        if (-f $1) {
                            push @{$$services{$service}{"used_config_file"}}, $1;
                        }
                    } elsif ($exec_start =~ /(.+$service(d)?\S*).*start/) {
                        @service_used_config_file = parse_wrapper_script(
                            {
                                "wrapper_script"   => $1,
                                "used_config_file" => \@service_used_config_file,
                            }
                        );
                        @{$$services{$service}{"used_config_file"}} = @service_used_config_file;
                    } elsif ($exec_start =~ /\s(\/.+$service(boot)?)/) {
                        if (-f $service_file) {
                            push @{$$services{$service}{"used_config_file"}}, $service_file;
                        }
                    }
                }
            }
        }

        # Check the used_config_files array for duplicates and get rid of them
        @{$$services{$service}{"used_config_file"}} = do {
            my %seen;
            grep { !$seen{$_}++ } @{$$services{$service}{"used_config_file"}}
        };
    }

    # Check initial status of services
    foreach $service (sort keys %$services) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "systemctl status $$services{$service}{service_handler}",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@systemctl_status,
            }
        );

        if ($rc == 0) {
            foreach (@systemctl_status) {
                if ($_ =~ /.*Active:\s(\w+)\s.*/) {
                    $$services{$service}{"initial_service_status"} = lc($1);
                }
            }
        }
    }

    # Create service commands
    foreach $service (sort keys %$services) {
        push @{$$services{$service}{"service_commands"}}, "systemctl start $$services{$service}{service_handler}";
        push @{$$services{$service}{"service_commands"}}, "systemctl stop $$services{$service}{service_handler}";
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Routine used to fill the provided service hash with information regarding
# configuration files used by each service
#
# Input variables:
#     - Hash reference containing hashes for each service that will be populated
#       with parts of relevant information
#       - Information included in hash for each service:
#           - Installed RPMs
#           - Service Commands
#           - Used Config Files
#           - Initial Service Status
#           - Service Handler
#
# Returns:
# -----------------------------------------------------------------------------
sub check_service_handling_old {
    my %params = %{$_[0]};

    my $services = $params{"service_hash"};

    my @chkconfig_result;
    my @etc_ls;
    my @sysconfig_ls;
    my $rc;
    my $service;
    my $service_file;
    my @service_list;
    my @service_status;

    # Use the chkconfig command to get a list of services that are usually
    # lists the services monitored by xinetd
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "chkconfig",
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@chkconfig_result,
        }
    );

    # If the chkconfig command was successful check the returned array for
    # the specified services, if found set the service handler to xinetd
    # and store the configuration file for the service if it exists in the
    # standard location
    if ($rc == 0) {
        foreach $service (sort keys %$services) {
            foreach (@chkconfig_result) {
                if ($_ =~ /($service)\s*(xinetd|on)/) {
                    # Check for xinetd config files, if it exists add it to
                    # used_config_file key in hash of hash
                    if (-f "/etc/xinetd.d/$service") {
                        $$services{$service}{"service_handler"} = "xinetd";
                        push @{$$services{$service}{"used_config_file"}}, "/etc/xinetd.d/$service";
                    }
                }
            }
        }
    }

    # For each service check chkconfig to find the proper name for the service
    # and store in the hash
    foreach $service (sort keys %$services) {
        @service_list = ();

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "chkconfig",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@service_list,
            }
        );

        if ($rc == 0 && $$services{$service}{"service_handler"} eq "") {
            foreach (sort @service_list) {
                if ($_ =~ /\b($service(d|server))\b.+/) {
                    $$services{$service}{"service_handler"} = "$1";
                    last;
                }
            }
        }
    }

    # For each service check if configuration can be found using the systemctl
    # cat command, if not try to find the configuration file from inside the
    # services wrapper script
    foreach $service (sort keys %$services) {
        @service_list = ();

        if ($service eq "nfs") {
            if (-f "/etc/exports") {
                push @{$$services{$service}{"used_config_file"}}, "/etc/exports";
            }
            next;
        } elsif ($$services{$service}{service_handler} eq "xinetd") {
            next;
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ls /etc/",
                "hide-output"   => 1,
                "return-output" => \@etc_ls,
            }
        );

        if ($rc == 0) {
            foreach (@etc_ls) {
                if ("$$services{$service}{service_handler}.conf" =~ /($_).*/) {
                    push @{$$services{$service}{"used_config_file"}}, "/etc/$$services{$service}{service_handler}.conf";
                }
            }
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ls /etc/sysconfig/",
                "hide-output"   => 1,
                "return-output" => \@sysconfig_ls,
            }
        );

        if ($rc == 0) {
            foreach (@sysconfig_ls) {
                if ($$services{$service}{service_handler} =~ /($_).*/) {
                    push @{$$services{$service}{"used_config_file"}}, "/etc/sysconfig/$$services{$service}{service_handler}";
                }
            }
        }

        # Check the used_config_files array for duplicates and get rid of them
        @{$$services{$service}{"used_config_file"}} = do {
            my %seen;
            grep {!$seen{$_}++} @{$$services{$service}{"used_config_file"}}
        };
    }

    # Check initial status of services
    foreach $service (sort keys %$services) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "service $$services{$service}{service_handler} status",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@service_status,
            }
        );

        if ($rc == 0) {
            foreach (@service_status) {
                if ($_ =~ /.*:.*\.\.(\w+).*/) {
                    $$services{$service}{"initial_service_status"} = lc($1);
                }
            }
        }
    }

    # Create service commands
    foreach $service (sort keys %$services) {
        push @{$$services{$service}{"service_commands"}}, "service $$services{$service}{service_handler} start";
        push @{$$services{$service}{"service_commands"}}, "service $$services{$service}{service_handler} stop";
    }
}

# -----------------------------------------------------------------------------
# Simple routine to initialize a hash based on the provided array reference
#
# Input variables:
#     - Array reference containing used services
#
# Returns:
#     - Reference to initialized service hash for further use
# -----------------------------------------------------------------------------
sub create_service_hash {
    my %params = %{$_[0]};

    my %services;
    my $used_services = $params{"used_services"};

    foreach (@$used_services) {
        $services{$_} = {
            "installed_rpms" => [],
            "initial_service_status" => "",
            "service_handler" => "",
            "service_commands" => [],
            "used_config_file" => [],
        };
    }

    return \%services;
}

# -----------------------------------------------------------------------------
# Routine used to parse the provided service wrapper script to find the path
# to the configuration file which will be used by the service
#
# Input variables:
#     - Wrapper script to parse
#     - Reference to array that should be used for storing the information
#
# Returns:
#     - Filled in array of the found configuration files
# -----------------------------------------------------------------------------
sub parse_wrapper_script {
    my %params = %{$_[0]};

    my $service_used_config_file = $params{"used_config_file"};
    my $service_wrapper_script = $params{"wrapper_script"};

    my @cat_service_wrapper;
    my $rc;
    my $used_config_file;

    if (-f $service_wrapper_script) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "cat $service_wrapper_script",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@cat_service_wrapper,
            }
        );

        if ($rc == 0) {
            foreach (@cat_service_wrapper) {
                if ($_ =~ /(DAEMON_CONF|DEFAULTS)=(\S+$)/) {
                    $used_config_file = $2;
                    $used_config_file =~ s/^"//;
                    $used_config_file =~ s/"$//;
                    if (-f $used_config_file) {
                        push @{$service_used_config_file}, $used_config_file;
                    }
                } elsif ($_ =~ /.*(\/etc\/sysconfig\/dhcpd).*/) {
                    push @{$service_used_config_file}, $1;
                }
            }
        }
    }

    return @{$service_used_config_file};
}

# -----------------------------------------------------------------------------
# Routine to backup all files as well as stop prided services
#
# Input variables:
#     - Directory used for backup files
#     - Hash reference containing hashes for each service that will be populated
#       with parts of relevant information
#       - Information included in hash for each service:
#           - Installed RPMs
#           - Service Commands
#           - Used Config Files
#           - Initial Service Status
#           - Service Handler
#
# Returns:
# -----------------------------------------------------------------------------
sub js_backup {
    my %params = %{$_[0]};

    my $backup_directory = $params{"backup_directory"};
    my $services = $params{"service_hash"};

    my $backup_file_name;
    my $rc;
    my $service;

    foreach $service (sort keys %$services) {
        foreach (@{$$services{$service}{"used_config_file"}}) {
            $backup_file_name = $_;
            $backup_file_name =~ s/\//_/g;
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp $_ $backup_directory/$backup_file_name.backup",
                    "hide-output"   => 1,
                }
            );
        }
    }

    # Stop all necessary services to prepare for setup
    foreach $service (sort keys %$services) {
        foreach (@{$$services{$service}{"service_commands"}}) {
            if ($_ =~ /.*stop/) {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$_",
                        "hide-output"   => 1,
                    }
                );
            }
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Routine to check that the configured interface comes up after reconfiguring
# the jump start server
#
# Input variables:
#     - Hash reference containing information used for configuration of the
#       jump start server
#       - Information included in hash for each service:
#           - Installation Step
#           - Storage Directory
#           - File Name
#           - DHCP Netmask
#           - DHCP Range
#           - DHCP Subnet
#           - DHCP Interface
#           - TFTPBOOT Dir
#           - TFTP Server IP Address
#
# Returns:
#
# -----------------------------------------------------------------------------
sub js_check_dhcp_interface {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};

    my @ethtool_printout;
    my $rc;

    # Bring up used dhcp interface
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ip link set $$configuration{dhcp_interface} up",
            "hide-output"   => 1,
        }
    );

    # Check that end to end connectivity is established
    if ($rc == 0) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ethtool $$configuration{dhcp_interface}",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@ethtool_printout,
            }
        );

        if ($rc == 0) {
            foreach(@ethtool_printout) {
                if ($_ =~ /Link detected:\s(yes)$/) {
                    $rc = 0;
                } else {
                    $rc = 1;
                }
            }
        }
    }

    return $rc
}

# -----------------------------------------------------------------------------
# Routine to check that the expected number of RPMs per service are installed
# on the jump start server
#
# Input variables
#     - Hash reference containing OS specific information
#       - Information included in hash:
#           - ID
#           - Version ID
#           - Result of uname
#     - Hash reference containing hashes for each service that will be populated
#       with parts of relevant information
#       - Information included in hash for each service:
#           - Installed RPMs
#           - Service Commands
#           - Used Config Files
#           - Initial Service Status
#           - Service Handler
#
# Returns:
#
# -----------------------------------------------------------------------------
sub js_check_installed_rpms {
    my %params = %{$_[0]};

    my $os_info = $params{"os_info"};
    my $return_output = exists $params{"return-output"} ? 1 : 0;
    my $services = $params{"service_hash"};

    my $matches_found = 0;
    my @result = ();
    my $rc = 0;
    my $rpm;
    my $service;

    foreach $service (sort keys %$services) {
        $matches_found = 0;

        if ($$os_info{"id"} eq "sles") {
            if ($service eq "dhcp") {
                foreach $rpm (@{$$services{$service}{installed_rpms}}) {
                    if ($rpm =~ "dhcp-server") {
                        $matches_found++;
                    }
                }

                if ($matches_found == 0) {
                    $rc = 1;
                    push @result,"Issues found when checking DHCP RPMs";
                    push @result,"We require that dhcp-server RPM is installed";
                    push @result,"Currently the following RPMs for DHCP are installed:";
                    push @result,@{$$services{$service}{installed_rpms}};
                }
            } elsif ($service eq "nfs") {
                foreach $rpm (@{$$services{$service}{installed_rpms}}){
                    if ($rpm =~ "nfs-server" || $rpm =~ "nfs-kernel-server") {
                        $matches_found++;
                    }
                }

                if ($matches_found == 0) {
                    $rc = 1;
                    push @result,"Issues found when checking NFS RPMs";
                    push @result,"We require that nfs-server or nfs-kernel-server RPM is installed";
                    push @result,"Currently the following RPMs for NFS are installed:";
                    push @result,@{$$services{$service}{installed_rpms}};
                }
            } elsif ($service eq "tftp") {
                foreach $rpm (@{$$services{$service}{installed_rpms}}) {
                    if ($rpm =~ "tftp-server") {
                        $matches_found++;
                    }
                }

                if ($matches_found == 0) {
                    $rc = 1;
                    push @result,"Issues found when checking TFTP RPMs";
                    push @result,"We require that tftp-server RPM is installed";
                    push @result,"Currently the following RPMs for TFTP are installed:";
                    push @result,@{$$services{$service}{installed_rpms}};
                }
            } else {
                $rc = 1;
                push @result,"Could not find any of the required RPMs";
                push @result,@{$$services{$service}{installed_rpms}};
            }
        } elsif ($$os_info{"id"} eq "rhel" || $$os_info{"id"} eq "ubuntu") {
            if ($service eq "dhcp") {
                foreach $rpm (@{$$services{$service}{installed_rpms}}) {
                    if ($rpm =~ "dhcp-server") {
                        $matches_found++;
                    }
                }

                if ($matches_found == 0) {
                    $rc = 1;
                    push @result,"Issues found when checking DHCP RPMs";
                    push @result,"We require that dhcp-server RPM is installed";
                    push @result,"Currently the following RPMs for DHCP are installed:";
                    push @result,@{$$services{$service}{installed_rpms}};
                }
            } elsif ($service eq "nfs") {
                foreach $rpm (@{$$services{$service}{installed_rpms}}) {
                    if ($rpm =~ "nfs-utils") {
                        $matches_found++;
                    }
                }

                if ($matches_found == 0) {
                    $rc = 1;
                    push @result,"Issues found when checking NFS RPMs";
                    push @result,"We require that nfs-utils RPM is installed";
                    push @result,"Currently the following RPMs for NFS are installed:";
                    push @result,@{$$services{$service}{installed_rpms}};
                }
            } elsif ($service eq "tftp") {
                foreach $rpm (@{$$services{$service}{installed_rpms}}) {
                    if ($rpm =~ "tftp-server") {
                        $matches_found++;
                    }
                }

                if ($matches_found == 0) {
                    $rc = 1;
                    push @result,"Issues found when checking TFTP RPMs";
                    push @result,"We require that tftp-server RPM is installed";
                    push @result,"Currently the following RPMs for TFTP are installed:";
                    push @result,@{$$services{$service}{installed_rpms}};
                }
            } else {
                $rc = 1;
                push @result,"Could not find any of the required RPMs";
                push @result,@{$$services{$service}{installed_rpms}};
            }
        }
    }

    @{$params{"return-output"}} = @result;
    return $rc;
}

# -----------------------------------------------------------------------------
# Routine to check that used route is in the routing table and if not then add
# it
#
# Input variables:
#     - Hash reference containing information used for configuration of the
#       jump start server
#       - Information included in hash for each service:
#           - Installation Step
#           - Storage Directory
#           - File Name
#           - DHCP Netmask
#           - DHCP Range
#           - DHCP Subnet
#           - DHCP Interface
#           - TFTPBOOT Dir
#           - TFTP Server IP Address
# Returns:
#
# -----------------------------------------------------------------------------
sub js_check_ip_route {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};

    my @ip_route;
    my $match_found = 1;
    my %netmask_to_value = (
        "255.0.0.0"       =>  8,
        "255.128.0.0"     =>  9,
        "255.192.0.0"     => 10,
        "255.224.0.0"     => 11,
        "255.240.0.0"     => 12,
        "255.248.0.0"     => 13,
        "255.252.0.0"     => 14,
        "255.254.0.0"     => 15,
        "255.255.0.0"     => 16,
        "255.255.128.0"   => 17,
        "255.255.192.0"   => 18,
        "255.255.224.0"   => 19,
        "255.255.240.0"   => 20,
        "255.255.248.0"   => 21,
        "255.255.252.0"   => 22,
        "255.255.254.0"   => 23,
        "255.255.255.0"   => 24,
        "255.255.255.128" => 25,
        "255.255.255.192" => 26,
        "255.255.255.224" => 27,
        "255.255.255.240" => 28,
        "255.255.255.248" => 29,
        "255.255.255.252" => 30
    );
    my $netmask_value = $netmask_to_value{$$configuration{dhcp_conf_netmask}};
    my $rc;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ip route",
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@ip_route,
        }
    );

    if ($rc == 0) {
        foreach (@ip_route) {
            if ($_ =~ /$$configuration{dhcp_conf_subnet}\/$netmask_value dev $$configuration{dhcp_interface}(  proto kernel)?  scope link  src $$configuration{tftp_server_ip_address}/i) {
                $match_found = 0;
            }
        }

        if ($match_found == 1) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "ip route add $$configuration{dhcp_conf_subnet}/$netmask_value src $$configuration{tftp_server_ip_address} dev $$configuration{dhcp_interface}",
                    "hide-output"   => 1,
                }
            );
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Routine to configure jump start server using the provided information
#
# Input variables:
#     - Directory used for backup files
#     - Hash reference containing information used for configuration of the
#       jump start server
#       - Information included in hash for each service:
#           - Installation Step
#           - Storage Directory
#           - File Name
#           - DHCP Netmask
#           - DHCP Range
#           - DHCP Subnet
#           - DHCP Interface
#           - TFTPBOOT Dir
#           - TFTP Server IP Address
#     - Hash reference containing hashes for each service that will be populated
#       with parts of relevant information
#       - Information included in hash for each service:
#           - Installed RPMs
#           - Service Commands
#           - Used Config Files
#           - Initial Service Status
#           - Service Handler
#
# Returns:
#
# -----------------------------------------------------------------------------
sub js_configure {
    my %params = %{$_[0]};

    my $backup_directory = $params{"backup_directory"};
    my $configuration = $params{"configuration_hash"};
    my $services = $params{"service_hash"};

    my $file_name;
    my $rc;
    my $service;
    my $template_file_name;

    # Generate all needed configuration files from template based on
    # provided hash
    foreach $service (sort keys %$services) {
        if ($service eq "dhcp") {
            foreach (@{$$services{"dhcp"}{"used_config_file"}}) {
                if ($_ =~ /.*\/(.*\.conf)/ || /.*\/($service)/) {
                    if ($$configuration{"installation_step"} eq "BSP") {
                        $file_name = $_;
                        $file_name =~ s/\//_/g;
                        $$configuration{"file_name"} = $file_name;
                        General::Configuration_Templates::create_template_data_dhcpd_conf_bsp(
                            {
                                "configuration_hash" => \%$configuration,
                            }
                        );
                    } elsif ($$configuration{"installation_step"} eq "DMX") {
                        $file_name = $_;
                        $file_name =~ s/\//_/g;
                        $$configuration{"file_name"} = $file_name;
                        General::Configuration_Templates::create_template_data_dhcpd_conf_dmx(
                            {
                                "configuration_hash" => \%$configuration,
                            }
                        );
                    } elsif ($$configuration{"installation_step"} eq "LDE") {
                        $file_name = $_;
                        $file_name =~ s/\//_/g;
                        $$configuration{"file_name"} = $file_name;
                        General::Configuration_Templates::create_template_data_dhcpd_conf_lde(
                            {
                                "configuration_hash" => \%$configuration,
                            }
                        );
                    }
                }
                if ($_ =~ /.*default\/(.*)/) {
                    $file_name = $_;
                    $file_name =~ s/\//_/g;
                    $$configuration{"file_name"} = $file_name;
                    General::Configuration_Templates::create_template_data_dhcpd_defaults(
                        {
                            "configuration_hash" => \%$configuration,
                        }
                    );
                }
                if ($_ =~ /.*sysconfig\/(.*)/) {
                    $file_name = $_;
                    $file_name =~ s/\//_/g;
                    $$configuration{"file_name"} = $file_name;
                    General::Configuration_Templates::create_template_data_dhcpd_sysconfig(
                        {
                            "configuration_hash" => \%$configuration,
                        }
                    );
                }
            }
        } elsif ($service eq "tftp") {
            foreach (@{$$services{"tftp"}{"used_config_file"}}) {
                if ($_ =~ /.*default\/(.*)/) {
                    $file_name = $_;
                    $file_name =~ s/\//_/g;
                    $$configuration{"file_name"} = $file_name;
                    General::Configuration_Templates::create_template_data_tftp_defaults(
                        {
                            "configuration_hash" => \%$configuration,
                        }
                    );
                }
                if ($_ =~ /.*\/(.*\.service)/) {
                    $file_name = $_;
                    $file_name =~ s/\//_/g;
                    $$configuration{"file_name"} = $file_name;
                    General::Configuration_Templates::create_template_data_tftp_service(
                        {
                            "configuration_hash" => \%$configuration,
                        }
                    );
                }
                if ($_ =~ /.*sysconfig\/(.*)/) {
                    $file_name = $_;
                    $file_name =~ s/\//_/g;
                    $$configuration{"file_name"} = $file_name;
                    General::Configuration_Templates::create_template_data_tftp_sysconfig(
                        {
                            "configuration_hash" => \%$configuration,
                        }
                    );
                }
                if ($_ =~ /.*xinetd\.d\/(tftp)/) {
                    $file_name = $_;
                    $file_name =~ s/\//_/g;
                    $$configuration{"file_name"} = $file_name;
                    General::Configuration_Templates::create_template_data_tftp_xinetd(
                        {
                            "configuration_hash" => \%$configuration,
                        }
                    );
                }
            }
        } elsif ($service eq "nfs") {
            next;
        }
    }

    # Copy generated files to the correct locations
    foreach $service (sort keys %$services) {
        foreach (@{$$services{$service}{"used_config_file"}}) {
            $template_file_name = $_;
            $template_file_name =~ s/\//_/g;
            if (-e "$backup_directory/$template_file_name.template") {
                General::OS_Operations::send_command(
                    {
                        "command"       => "cp $backup_directory/$template_file_name.template $_",
                        "hide-output"   => 1,
                    }
                );
            }
        }
    }

    # Start all necessary services to prepare for setup
    foreach $service (sort keys %$services) {
        foreach (@{$$services{$service}{"service_commands"}}) {
            if ($_ =~ /.*start/) {
                General::OS_Operations::send_command(
                    {
                        "command"       => "$_",
                        "hide-output"   => 1,
                    }
                );
            }
        }
    }

    $rc = js_check_dhcp_interface(
        {
             "configuration_hash" => \%$configuration,
        }
    );

    $rc = js_check_ip_route(
        {
             "configuration_hash" => \%$configuration,
        }
    );

    return $rc;
}

# -----------------------------------------------------------------------------
# Restore previous configuration on the jump start server
#
# Input variables:
#     - Directory used for backup files
#     - Hash reference containing hashes for each service that will be populated
#       with parts of relevant information
#       - Information included in hash for each service:
#           - Installed RPMs
#           - Service Commands
#           - Used Config Files
#           - Initial Service Status
#           - Service Handler
#
# Returns:
#
# -----------------------------------------------------------------------------
sub js_restore {
    my %params = %{$_[0]};

    my $backup_directory = $params{"backup_directory"};
    my $services = $params{"service_hash"};

    my $backup_file_name;
    my $rc;
    my $service;

    foreach $service (sort keys %$services) {
        foreach (@{$$services{$service}{"used_config_file"}}) {
            $backup_file_name = $_;
            $backup_file_name =~ s/\//_/g;
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp $backup_directory/$backup_file_name.backup $_",
                    "hide-output"   => 1,
                }
            );
        }
    }

    # Restore the services to their original active state
    foreach $service (sort keys %$services) {
        foreach (@{$$services{$service}{"service_commands"}}) {
            if ($_ =~ /.*start/ && $$services{$service}{"initial_service_status"} =~ /^(active|running)$/) {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$_",
                        "hide-output"   => 1,
                    }
                );
            } elsif ($_ =~ /.*stop/ && $$services{$service}{"initial_service_status"} =~ /^(inactive|unused)$/) {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$_",
                        "hide-output"   => 1,
                    }
                );
            }
        }
    }

    return $rc;
}

1;

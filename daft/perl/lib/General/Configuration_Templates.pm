package General::Configuration_Templates;

################################################################################
#
#  Author   : everhel
#
#  Revision : 1.2
#  Date     : 2018-10-15 17:22:13
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
    create_template_data_dhcpd_conf_bsp
    create_template_data_dhcpd_conf_dmx
    create_template_data_dhcpd_conf_lde
    create_template_data_dhcpd_defaults
    create_template_data_dhcpd_sysconfig
    create_template_data_tftp_defaults
    create_template_data_tftp_service
    create_template_data_tftp_sysconfig
    create_template_data_tftp_xinetd
    );

use Cwd 'abs_path';
use File::Find;

# -----------------------------------------------------------------------------
# Generate dhcpd.conf usable to jump start BSP, the subroutine finds the
# needed information for bootfile-name and dmx_sw_file from the tftpboot
# directory
#
# Expected structure under used tftpboot directory:
#  | -- CXC1732063/
#       | -- SCX.tar
#    -- CXP9025735_R12E02.tar
#    -- scx-metadata.xml
#
# Input variables
#     - Hash containing mandatory information to generate a dhcpd.conf
#       - dhcp_conf_netmask
#       - dhcp_conf_range
#       - dhcp_conf_subnet
#       - tftp_server_ip_address
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_dhcpd_conf_bsp {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $dhcp_conf_netmask = exists $$configuration{dhcp_conf_netmask} ? $$configuration{dhcp_conf_netmask} : undef;
    my $dhcp_conf_range = exists $$configuration{dhcp_conf_range} ? $$configuration{dhcp_conf_range} : undef;
    my $dhcp_conf_subnet = exists $$configuration{dhcp_conf_subnet} ? $$configuration{dhcp_conf_subnet} : undef;
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;
    my $tftpboot_dir = exists $$configuration{tftpboot_dir} ? $$configuration{tftpboot_dir} : undef;
    my $tftp_server_ip_address = exists $$configuration{tftp_server_ip_address} ? $$configuration{tftp_server_ip_address} : undef;

    my $boot_file_path;
    my $bsp_sw_file;
    my @regex;
    my @result_boot_file_path;
    my @result_bsp_sw_file;

    #
    @regex = (".*SCX.*");

    find(sub { wanted (
        {
            "regex_to_match" => \@regex,
            "result_array_ref" => \@result_boot_file_path,
        }
    ) }, $tftpboot_dir);

    # Flatten the array since we should on have
    # one matching result.
    $boot_file_path = join("", @result_boot_file_path);

    #
    @regex = (".*CXP.*");

    find(sub { wanted (
        {
            "regex_to_match" => \@regex,
            "result_array_ref" => \@result_bsp_sw_file,
        }
    ) }, $tftpboot_dir);

    # Flatten the array since we should on have
    # one matching result.
    $bsp_sw_file = join("", @result_bsp_sw_file);

    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

ddns-update-style none;
ddns-updates off;

# ericsson proprietary
#------------------------------------------------------------------
option startup-mode code 209 = text;
option forced-sw-download code 224 = text;
option dmx_sw_version code 227 = text;
option dmx_sw_file code 228 = text;
option bootfile-sw-version code 232 = text;
option shelf-mode code 236 = unsigned integer 8;
option startup-switch-mode code 233 = text;
#------------------------------------------------------------------
# In this example client identifier 00000__eth2 is used because the
# shelf address is 0 and the jumpstart server is connected to eth2
# on SCXB in slot 0
#
# replace YYYyy with BSP sw_version_numbers.
# e.g
# option bootfile-name "bsp/CXC1732063/SCX.tar";
# option dmx_sw_file "bsp/CXP9025735_R7C16.tar";
# option dmx_sw_version "optional";

class "scx_left_eth2" {
    match if(substring(option dhcp-client-identifier,12,11) = "00000__eth2");
    option startup-mode "cc";
}

group {
    ping-check TRUE;
    ping-timeout 3;

    option bootfile-name "$boot_file_path";

    option shelf-mode 0x80;
    option startup-switch-mode "000000000000008000000000";
    option dmx_sw_file "$bsp_sw_file";
    option dmx_sw_version "optional";
    option tftp-server-name "$tftp_server_ip_address";

    subnet $dhcp_conf_subnet netmask $dhcp_conf_netmask
    {
        #LEFT switch network
        pool {
            allow members of "scx_left_eth2";
            range $dhcp_conf_range;
        }
    }
}

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name" . ".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Generate dhcpd.conf usable to jump start DMX, the subroutine finds the
# needed information for boot_file_path, boot_file_without_suffix, dmx_sw_file,
# abd dmx_sw_version from the tftpboot directory
#
# Expected structure under used tftpboot directory:
#  | -- CMX.tgz
#    -- DMX_CXP9016673_R5H09.tar
#    -- OTP.tgz
#    -- SCX_CXC1729926_R5AF4.tar
#    -- SCX_CXC1732063_R5AF4.tar
#
# Input variables
#     - Hash containing mandatory information to generate a dhcpd.conf
#       - dhcp_conf_netmask
#       - dhcp_conf_range
#       - dhcp_conf_subnet
#       - tftp_server_ip_address
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_dhcpd_conf_dmx {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $dhcp_conf_netmask = exists $$configuration{dhcp_conf_netmask} ? $$configuration{dhcp_conf_netmask} : undef;
    my $dhcp_conf_range = exists $$configuration{dhcp_conf_range} ? $$configuration{dhcp_conf_range} : undef;
    my $dhcp_conf_subnet = exists $$configuration{dhcp_conf_subnet} ? $$configuration{dhcp_conf_subnet} : undef;
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;
    my $tftpboot_dir = exists $$configuration{tftpboot_dir} ? $$configuration{tftpboot_dir} : undef;
    my $tftp_server_ip_address = exists $$configuration{tftp_server_ip_address} ? $$configuration{tftp_server_ip_address} : undef;

    my $boot_file_path;
    my $boot_file_without_suffix;
    my $dmx_sw_file;
    my $dmx_sw_version;
    my @regex;
    my @result_boot_file_path;
    my @result_dmx_sw_file;

    #
    @regex = (".*CXC1732063.*");

    find(sub { wanted (
        {
            "regex_to_match" => \@regex,
            "result_array_ref" => \@result_boot_file_path,
        }
    ) }, $tftpboot_dir);

    # Flatten the array since we should on have
    # one matching result.
    $boot_file_path = join("", @result_boot_file_path);
    if ($boot_file_path =~ /(.*\/)?(.*).tar/) {
        $boot_file_without_suffix = $2;
    }

    #
    @regex = ("DMX_CXP9016673.*");

    find(sub { wanted (
        {
            "regex_to_match" => \@regex,
            "result_array_ref" => \@result_dmx_sw_file,
        }
    ) }, $tftpboot_dir);

    # Flatten the array since we should on have
    # one matching result.
    $dmx_sw_file = join("", @result_dmx_sw_file);
    if ($dmx_sw_file =~ /(.*\/)?.*_(.*).tar/) {
        $dmx_sw_version = $2;
    }

    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

ddns-update-style none;
ddns-updates off;

# ericsson proprietary
#------------------------------------------------------------------
option startup-mode code 209 = text;
option forced-sw-download code 224 = text;
option snmp-trap-receiver code 225 = array of ip-address;
option dmx_sw_version code 227 = text;
option dmx_sw_file code 228 = text;
option bootfile-sw-version code 232 = text;
option shelf-mode code 236 = unsigned integer 8;
#------------------------------------------------------------------

### Next part is only to be used when multiple cabinets are connected to one jumpstart server
#class "scx_left_eth1" {
#    match if((substring(option dhcp-client-identifier,11,11) = "00000__eth1") or (substring(option dhcp-client-identifier,12,11) = "00000__eth1"));
#       option startup-mode "cc";
#}

option startup-mode "cc";

group {
    ping-check TRUE;
    ping-timeout 3;
    # Note: either SCXB2 or SCXB3 should be commented out depending on
    # configuration.
    # SCXB3
    option bootfile-name "$boot_file_path";
    option bootfile-sw-version "$boot_file_without_suffix";
    # SCXB2
    # option bootfile-name "dmx/SCX_CXC1729926_R3A195.tar";
    # option bootfile-sw-version "SCX_CXC1729926_R3A195";

    option shelf-mode 0x80;
    option dmx_sw_file "$dmx_sw_file";
    option dmx_sw_version "$dmx_sw_version";
    option tftp-server-name "$tftp_server_ip_address";
    option snmp-trap-receiver 10.0.10.1;

    subnet $dhcp_conf_subnet netmask $dhcp_conf_netmask
    {
        #LEFT switch network
        pool {
            #allow members of "scx_left_eth1";
            range $dhcp_conf_range;
        }
    }
}

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name".".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Generate dhcpd.conf usable to jump start LDE
#
# Input variables
#     - Hash containing mandatory information to generate a dhcpd.conf
#       - dhcp_conf_netmask
#       - dhcp_conf_range
#       - dhcp_conf_subnet
#       - tftp_server_ip_address
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_dhcpd_conf_lde {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $dhcp_conf_netmask = exists $$configuration{dhcp_conf_netmask} ? $$configuration{dhcp_conf_netmask} : undef;
    my $dhcp_conf_range = exists $$configuration{dhcp_conf_range} ? $$configuration{dhcp_conf_range} : undef;
    my $dhcp_conf_subnet = exists $$configuration{dhcp_conf_subnet} ? $$configuration{dhcp_conf_subnet} : undef;
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $first_mac_address = exists $$configuration{first_mac_address} ? $$configuration{first_mac_address} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;
    my $tftp_server_ip_address = exists $$configuration{tftp_server_ip_address} ? $$configuration{tftp_server_ip_address} : undef;
    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

ddns-update-style none;
ddns-updates off;

#------------------------------------------------------------------
option startup-mode code 209 = text;
option forced-sw-download code 224 = text;
option snmp-trap-receiver code 225 = array of ip-address;
option lotc_sw_version code 227 = text;
#option lotc_sw_file code 228 = text;
#option bootfile-sw-version code 232 = text;
#------------------------------------------------------------------

group {
    ping-check TRUE;
    ping-timeout 3;

    option startup-mode "cc";
    option tftp-server-name "$tftp_server_ip_address";
    option snmp-trap-receiver 10.0.11.1;

    subnet $dhcp_conf_subnet netmask $dhcp_conf_netmask
    {
        filename "pxelinux.0";
        next-server $tftp_server_ip_address;
        host control1 {
            hardware ethernet $first_mac_address;
            fixed-address $dhcp_conf_range;
       }
    }
}

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name" . ".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Generate dhcpd defaults file, usually needed when running on Ubuntu with
# default installation of dhcp
#
# Input variables
#     - Hash containing mandatory information to generate a dhcpd config
#       under defaults
#       - dhcp_interface
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_dhcpd_defaults {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $dhcp_interface = exists $$configuration{dhcp_interface} ? $$configuration{dhcp_interface} : undef;
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;

    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

# Defaults for isc-dhcp-server (sourced by /etc/init.d/isc-dhcp-server)

# Path to dhcpd's config file (default: /etc/dhcp/dhcpd.conf).
#DHCPDv4_CONF=/etc/dhcp/dhcpd.conf
#DHCPDv6_CONF=/etc/dhcp/dhcpd6.conf

# Path to dhcpd's PID file (default: /var/run/dhcpd.pid).
#DHCPDv4_PID=/var/run/dhcpd.pid
#DHCPDv6_PID=/var/run/dhcpd6.pid

# Additional options to start dhcpd with.
#   Don't use options -cf or -pf here; use DHCPD_CONF/ DHCPD_PID instead
#OPTIONS=""

# On what interfaces should the DHCP server (dhcpd) serve DHCP requests?
#   Separate multiple interfaces with spaces, e.g. "eth0 eth1".
INTERFACESv4="$dhcp_interface"
INTERFACESv6=""

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name" . ".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Generate dhcpd sysconfig file, usually needed when running on SLES with
# default installation of dhcp
#
# Input variables
#     - Hash containing mandatory information to generate a dhcpd config
#       under sysconfig
#       - dhcp_interface
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_dhcpd_sysconfig {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $dhcp_interface = exists $$configuration{dhcp_interface} ? $$configuration{dhcp_interface} : undef;
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;

    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

## Path:    Network/DHCP/DHCP server
## Description: DHCP server settings
## Type:    string
## Default: ""
## ServiceRestart: dhcpd
# Command line options here
DHCPD_INTERFACE="$dhcp_interface"

## Type:    yesno
## Default: yes
## ServiceRestart: dhcpd
DHCPD_RUN_CHROOTED="yes"

## Type:    string
## Default: "dhcpd"
## ServiceRestart: dhcpd
DHCPD_RUN_AS="dhcpd"

#DHCPDARGS=

## Description: Restart dhcp server when interface goes up (again)
## Type:        list(yes,no,auto,)
## Default:
#
# When the dhcp server is listening on a virtual interface, e.g. bridge,
# bonding or vlan, and this interface gets deleted and recreated during
# a network restart, dhcpd will stop answering requests on this interface
# and needs a restart as well.
# Begining with SLE-10 SP3, we install an if-up.d post script (see ifup(8)
# and also ifservices(5)), enabled in auto mode by default. This variable
# can be used to force or avoid the dhcp server restart:
#
#   no:   do not restart dhcpd
#  yes:   force a dhcp server restart
# auto:   (default) restart for virtual interfaces (bond,bridge,vlan) when
#         all interfaces used in DHCPD_INTERFACE variable are up as well.
#
# Except of this global setting, the variable can be specified per interface
# in the interface configurations (/etc/sysconfig/network/ifcfg-\$name).
#
DHCPD_IFUP_RESTART=""

## Type:    string
## Default: ""
## ServiceRestart: dhcpd
#
# Since version 3, dhcpd.conf can contain include statements.
# If you enter the names of any include files here, _all_ conf
# files will be copied to \$chroot/etc/, when dhcpd is started in the
# chroot jail. (/etc/dhcpd.conf is always copied.)
#
# For your convenience, you can also specify entire directories, like
# "/etc/dhcpd.conf.d".
#
# Example: "/etc/dhcpd.conf.shared /etc/dhcpd.conf.bootp-clients"
#
DHCPD_CONF_INCLUDE_FILES=""

## Type:    string
## Default: ""
## ServiceRestart: dhcpd
#
# Other arguments that you want dhcpd to be started with
# (e.g. "-p 1234" for a non-standard port to listen on)
#
DHCPD_OTHER_ARGS=""

## Type:    string
## Default: ""
## ServiceRestart: dhcpd
#
# You may specify another dhcpd binary to be run.
# The full path needs to be specified.
#
# If empty, the default applies.
#
DHCPD_BINARY=""

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name" . ".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Generate tftp defaults file, usually needed when running on Ubuntu with
# default installation of tftp
#
# Input variables
#     - Hash containing mandatory information to generate a tftp config
#       under defaults
#       - tftpboot_dir
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_tftp_defaults {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;
    my $tftpboot_dir = exists $$configuration{tftpboot_dir} ? $$configuration{tftpboot_dir} : undef;

    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

# /etc/default/tftpd-hpa

TFTP_USERNAME="tftp"
TFTP_DIRECTORY="$tftpboot_dir"
TFTP_ADDRESS=":69"
TFTP_OPTIONS="--secure"

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name" . ".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Generate tftp service file, usually needed when running on RHEL with
# default installation of tftp
#
# Input variables
#     - Hash containing mandatory information to generate a tftp config
#       under service
#       - tftpboot_dir
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_tftp_service {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;
    my $tftpboot_dir = exists $$configuration{tftpboot_dir} ? $$configuration{tftpboot_dir} : undef;

    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

[Unit]
Description=Tftp Server
Requires=tftp.socket
Documentation=man:in.tftpd

[Service]
ExecStart=/usr/sbin/in.tftpd -s $tftpboot_dir
StandardInput=socket

[Install]
Also=tftp.socket

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name" . ".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Generate tftp sysconfig file, usually needed when running on SLES12 with
# default installation of tftp
#
# Input variables
#     - Hash containing mandatory information to generate a tftp config
#       under sysconfig
#       - tftpboot_dir
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_tftp_sysconfig {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;
    my $tftpboot_dir = exists $$configuration{tftpboot_dir} ? $$configuration{tftpboot_dir} : undef;

    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

## Description: TFTP Configuration
## Type:    string
## Default: "tftp"
#
#  daemon user (tftp)
#
TFTP_USER="tftp"

## Type:    string
## Default: ""
##
## INFO:
#
# tftp options
#
TFTP_OPTIONS=""

## Type:    string
## Default: "/srv/tftpboot"
## was "/tftpboot" but
## "/tftpboot" is not allowed anymore in FHS 2.2.
#
#  TFTP directory must be a world readable/writable directory.
#  By default /srv/tftpboot is assumed.
#
TFTP_DIRECTORY="$tftpboot_dir"

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name" . ".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Generate tftp xinetd configuration fiel, usually needed when running on
# SLES11 with default installation of tftp
#
# Input variables
#     - Hash containing mandatory information to generate a tftp config for
#       xinetd
#       - tftpboot_dir
#
# Returns:
#
# -----------------------------------------------------------------------------
sub create_template_data_tftp_xinetd {
    my %params = %{$_[0]};

    my $configuration = $params{"configuration_hash"};
    my $file_name = exists $$configuration{file_name} ? $$configuration{file_name} : undef;
    my $storage_directory = exists $$configuration{storage_directory} ? $$configuration{storage_directory} : undef;
    my $tftpboot_dir = exists $$configuration{tftpboot_dir} ? $$configuration{tftpboot_dir} : undef;

    my $temp_data = <<"EOF";
#
# Generated by DAFT Installation Script
#

# default: off
# description: tftp service is provided primarily for booting or when a \
#   router need an upgrade. Most sites run this only on machines acting as
#   "boot servers".

service tftp
{
    socket_type     = dgram
    protocol        = udp
    wait            = yes
    flags           = IPv6 IPv4
    user            = root
    server          = /usr/sbin/in.tftpd
    server_args     = -s $tftpboot_dir
    disable         = no
}

EOF

    my @out_put_file = split /\n/, $temp_data;

    open FILE, ">$storage_directory/$file_name" . ".template" or die $!;

    foreach(@out_put_file) {
        print FILE "$_\n";
    }

    close FILE;
}

# -----------------------------------------------------------------------------
# Modified wanted subroutine for the find command
#
# Input variables:
#   - Hash containing mandatory information
#     - Array of regular expressions to be matched
#     - Array reference that will be used to store the match of the regular
#       expression
#
# Returns:
#
# -----------------------------------------------------------------------------
sub wanted {
    my %params = %{$_[0]};

    my $wanted = $params{"result_array_ref"};

    foreach my $regex (@{$params{"regex_to_match"}}) {
        if ($_ =~ /$regex/) {
            my $fullpath = abs_path($_);
            $fullpath =~ s/${$params{"tftpboot_dir"}}(\/)?//;
            push @{$wanted}, $fullpath;
        }
    }
}

1;

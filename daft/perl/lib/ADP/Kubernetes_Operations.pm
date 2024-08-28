package ADP::Kubernetes_Operations;

################################################################################
#
#  Author   : eustone, everhel
#
#  Revision : 1.111
#  Date     : 2024-06-18 16:29:15
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
use warnings;

use Exporter qw(import);

our @EXPORT_OK = qw(
    approve_pending_certificates
    check_daemonset_status
    check_deployment_status
    check_pod_status
    check_scaling_status
    convert_number_to_kubernetes_unit
    convert_number_to_3_decimals
    convert_number_unit
    convert_time_unit_to_seconds
    get_alarms
    get_alarms_old_curl_way
    get_api_resources
    get_cmyp_info
    get_cmyp_ip
    get_cmyp_service_name
    get_docker_or_nerdctl_command
    get_eccd_version
    get_enabled_cnf
    get_enabled_cnf_from_helm
    get_helm_information
    get_helm_release_extended_information
    get_kubectl_information
    get_list_of_known_cnf_types
    get_list_of_known_sc_crds
    get_list_of_known_sc_crd_helm_charts
    get_log_cluster_allocation
    get_log_count
    get_master_nodes
    get_max_connections_default_value
    get_name_section_information
    get_node_role
    get_nodes
    get_nodes_with_label
    get_nodes_with_taints
    get_node_information
    get_node_ip
    get_node_resources
    get_node_status
    get_node_top
    get_node_top_master
    get_node_top_worker
    get_pod_names
    get_replicas
    get_resource_names
    get_sc_release_version
    get_section_names
    get_semantic_version
    get_service_ip
    get_service_name
    get_top_level_section_information
    get_worker_nodes
    namespace_exists
    set_used_kubeconfig_filename
    );

use General::Data_Structure_Operations;
use General::OS_Operations;
use General::Logging;
use General::Yaml_Operations;
use General::Json_Operations;

my $cached_alarm_pod_name = "";
my $cached_docker_nerdctl_command = "";
my %known_cnf_types = (

    #
    # sc
    #

    "sc" => {
        "latest" => [
            "bsf",
            "dsc",
            "scp",
            "sepp",
        ],
        "1.15.25" => [
            "bsf",
            "dsc",
            "scp",
            "sepp",
        ],
        "1.15.0" => [
            "bsf",
            "dsc",
            "scp",
            "sepp",
        ],
        "1.14.0" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.13.0" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.12.0" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.11.0" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.10.4" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.10.2" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.10.1" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.10.0" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.8.1" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.8.0" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.7.0" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.6.1" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.6.0" => [
            "bsf",
            "scp",
            "sepp",
        ],
        "1.5.1" => [
            "bsf",
            "csa",
            "scp",
            "sepp",
        ],
        "1.5.0" => [
            "bsf",
            "csa",
            "scp",
            "sepp",
        ],
        "1.4.1" => [
            "bsf",
            "csa",
            "scp",
        ],
        "1.4.0" => [
            "bsf",
            "csa",
            "scp",
        ],
        "1.3.4" => [
            "bsf",
            "scp",
        ],
        "1.3.3" => [
            "bsf",
            "scp",
        ],
        "1.3.2" => [
            "bsf",
            "scp",
        ],
        "1.3.1" => [
            "bsf",
            "scp",
        ],
        "1.3.0" => [
            "bsf",
            "scp",
        ],
        "1.2.3" => [
            "bsf",
            "scp",
        ],
        "1.2.2" => [
            "bsf",
            "scp",
        ],
    }
);
my %known_sc_crds = (

    #
    # dsc
    #

    "dsc" => {
        "latest" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.9.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.9.25" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
    },

    #
    # sc
    #

    "sc" => {
        "latest" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "redisclusters.kvdbrd.gs.ericsson.com",             # eric-data-key-value-database-rd-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.15.25" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "redisclusters.kvdbrd.gs.ericsson.com",             # eric-data-key-value-database-rd-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.15.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "redisclusters.kvdbrd.gs.ericsson.com",             # eric-data-key-value-database-rd-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.14.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.13.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd- (Not used by SC starting with SC 1.14.0 release but left in for older releases)
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd- (Not used by SC starting with SC 1.14.0 release but left in for older releases)
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd- (Not used by SC starting with SC 1.14.0 release but left in for older releases)
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd- (Not used by SC starting with SC 1.14.0 release but left in for older releases)
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.12.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.11.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.10.4" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.10.2" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.10.1" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.10.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.8.1" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "redisclusters.kvdbrd.gs.ericsson.com",             # eric-data-key-value-database-rd-crd- (Not used by SC starting with SC 1.10.0 release but left in for older releases)
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.8.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd-
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "redisclusters.kvdbrd.gs.ericsson.com",             # eric-data-key-value-database-rd-crd- (New for SC starting with SC 1.8.0)
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.7.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "cassandraclusters.wcdbcd.data.ericsson.com",       # eric-data-wide-column-database-cd-crd- (New for SC starting with SC 1.7.0)
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.6.1" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.6.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.5.1" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.5.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.4.1" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.4.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.3.4" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.3.3" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.3.2" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.3.1" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.3.0" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.2.3" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
        "1.2.2" => [
            # CRD Name                                          # Helm Chart Name:  kubectl describe crd | grep -P '(^Name:|^\s+meta.helm.sh.release-name|^\s+chart=)'
            # ------------------------------------------------    -----------------------------------------------------------
            "certificateauthorities.com.ericsson.sec.tls",      # eric-sec-sip-tls-crd-
            "clientcertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "externalcertificates.certm.sec.ericsson.com",      # eric-sec-certm-crd-
            "externalcertificates.com.ericsson.sec.certm",      # eric-sec-certm-crd-
            "extensionservices.projectcontour.io",              # eric-tm-ingress-controller-cr-crd-
            "httpproxies.projectcontour.io",                    # eric-tm-ingress-controller-cr-crd-
            "internalcertificates.siptls.sec.ericsson.com",     # eric-sec-sip-tls-crd-
            "internalusercas.siptls.sec.ericsson.com",          # eric-sec-sip-tls-crd-
            "servercertificates.com.ericsson.sec.tls",          # eric-sec-sip-tls-crd-
            "tlscertificatedelegations.projectcontour.io",      # eric-tm-ingress-controller-cr-crd-
        ],
    }
);
my %known_sc_crd_helm_charts = (

    #
    # dsc
    #

    "dsc" => {
        "latest" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.9.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.9.25" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
    },

    #
    # sc
    #

    "sc" => {
        "latest" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-key-value-database-rd-crd",              # 1
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.15.25" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-key-value-database-rd-crd",              # 1
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.15.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-key-value-database-rd-crd",              # 1
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.14.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.13.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.12.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.11.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.10.4" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.10.2" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.10.1" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.10.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.8.1" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-key-value-database-rd-crd",              # 1 (Not used by SC starting with SC 1.10.0 release but left in for older releases)
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.8.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-key-value-database-rd-crd",              # 1 (New for SC starting with SC 1.8.0)
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.7.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.6.1" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.6.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-data-wide-column-database-cd-crd",            # 1
            "eric-sec-certm-crd",                               # 2
            "eric-sec-sip-tls-crd",                             # 5
            "eric-tm-ingress-controller-cr-crd",                # 3
        ],
        "1.5.1" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10 (Old SC 1.2 to 1.5 name which included: eric-sec-certm-crd-, eric-sec-sip-tls-crd- and eric-tm-ingress-controller-cr-crd)
        ],
        "1.5.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.4.1" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.4.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.3.4" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.3.3" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.3.2" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.3.1" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.3.0" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.2.3" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
        "1.2.2" => [
            # CRD Helm Chart Name                               # Number of CRDs in Chart
            "eric-sc-crds",                                     # 10
        ],
    },
);
my $kubeconfig_file = "";
my %node_roles;
    # Key 1: kubeconfig file path or "-" if none specified.
    # Key 2: Node Role, e.g. master, worker
    # Value is just the value 1.

# -----------------------------------------------------------------------------
# Check the pod status of one or more pods if they are in wanted status or
# in Running state if nothing is specified.
# The user can specify the pods to check or if nothing specified then all pods
# will be checked.
# The user can also specify if they want to have a check for a specific STATUS
# or READY state. If nothing specified it will just check if the pod is in
# "Running" or "Completed" status.
# It can also check that the pods are in a specific READY state, if not specified
# then this is not checked.
# It can also wait for a specific time for the pod to get into the wanted state.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "delay-time":
#           Scalar variable with integer value of the number of seconds to wait
#           between each check.
#           If not specified then the default value is 10 seconds.
#           See also "max-attempts" and "max-time".
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "max-attempts":
#           Scalar variable with integer value of the maximum number of attempts
#           the script will try to check if there are Pending Certificate Signing
#           Requests.
#           See also "max-time" and "delay-time".
#           If not specified then no repeated check is done and the check is
#           only done once.
#       "max-time":
#           Scalar variable with integer value of the maximum number of seconds
#           the script will try to check if there are Pending Certificate Signing
#           Requests.
#           See also "max-time" and "delay-time".
#           If not specified then no repeated check is done and the check is
#           only done once.
#       "repeated-checks":
#           Scalar variable indicating if repeated checks should be done even
#           when there are no Pending Certificate Signing Requests.
#           If the value is "0" (also the default if not specified) then no
#           repeated checks will be done.
#           If the value is "1" then repeated checks will continue until
#           "max-attempts" or "max-time" expire whichever comes first.
#           This parameter can be useful when after approving all Pending
#           Certificate Signing Requests has been done then new ones come up
#           within a specific time perios.
#           This way we can wait until all the Pending Certificate Signing
#           Requests has been done.
#
# Return values:
#    - 0: All Pending certificate signing requests was approved.
#    - 1: Failed to approve pending certificate signing requests.
#
# -----------------------------------------------------------------------------
sub approve_pending_certificates {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages          = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $delay_time              = exists $params{"delay-time"} ? $params{"delay-time"} : 10;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $line;
    my $max_attempts            = exists $params{"max-attempts"} ? $params{"max-attempts"} : 0;
    my $max_time                = exists $params{"max-time"} ? $params{"max-time"} : 0;
    my $num_attempts            = 0;
    my $pending_names;
    my $rc;
    my @result                  = ();
    my $repeated_checks         = exists $params{"repeated-checks"} ? $params{"repeated-checks"} : 0;
    my @std_error = ();
    my $stop_time               = time + $max_time;

    while (1) {
        $num_attempts++;

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl get csr" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
                "hide-output"   => $hide_output,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc != 0) {
            General::Logging::log_write("Failed to execute the command to fetch Certificate Signing Requests\n") if $debug_messages;
            $rc = 1;
            last;
        }

        $pending_names = "";

        for $line (@result) {
            # NAME        AGE     SIGNERNAME                      REQUESTOR                                CONDITION
            # csr-4m2sk   105m    kubernetes.io/kubelet-serving   system:node:master-2-eccd-pikachu-ipv6   Pending
            if ($line =~ /^(\S+)\s+.+\s+Pending\s*$/) {
                # csr-4m2sk   105m    kubernetes.io/kubelet-serving   system:node:master-2-eccd-pikachu-ipv6   Pending
                $pending_names .= "$1 ";
            }
        }

        if ($pending_names eq "") {
            # No Pending Certificate Signing Requests
            $rc = 0;
            if ($repeated_checks == 0) {
                # We don't need to check anymore
                last;
            }
            # If we come here even though there are no Pending Certificate Signing Requests, we will continue to check if we need
            # to repeat the checks a number of times or for a number of seconds.
            # If no such conditions has been set then we will anyway exit the loop in the 'else' branch
            # below.
        } else {
            # One or Pending Certificate Signing Requests exists, now approve these.
            General::Logging::log_write("At least one Certificate Signing Requestss exists.\n") if $debug_messages;
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "kubectl certificate approve $pending_names" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
                    "hide-output"   => $hide_output,
                    "return-output" => \@result,
                    "return-stderr" => \@std_error,
                }
            );
            if ($rc != 0) {
                General::Logging::log_write("Failed to execute the command to approve the Certificate Signing Requests\n") if $debug_messages;
                $rc = 1;
                last;
            }
        }

        if ($max_attempts > 0) {
            if ($num_attempts >= $max_attempts) {
                General::Logging::log_write("Maximum number of attempts '$max_attempts' reached.\n") if $debug_messages;
                last;
            }
            if ($max_time > 0 && time >= $stop_time) {
                General::Logging::log_write("Maximum time '$max_time' reached before maximum number of attempts '$max_attempts'.\n") if $debug_messages;
                last;
            }
        } elsif ($max_time > 0) {
            if (time >= $stop_time) {
                General::Logging::log_write("Maximum time '$max_time' reached.\n") if $debug_messages;
                last;
            }
        } else {
            General::Logging::log_write("No more checking needed.\n") if $debug_messages;
            last;
        }

        General::Logging::log_write("Waiting '$delay_time' seconds before checking for Pending certificate signing requests again.\n") if $debug_messages;
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => $hide_output ? 0 : 1,
                "progress-interval" => $delay_time > 59 ? 10 : 1,
                "progress-message"  => $hide_output ? 0 : 1,
                "seconds"           => $delay_time,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            # CTRL-C pressed
            last;
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Check if a daemonset is up with the possibility to wait for a specific time
# for it to be fullfilled.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "check-name":
#           Scalar variable that contains the name of the type of resource to
#           check.
#       "delay-time":
#           Scalar variable with integer value of the number of seconds to wait
#           between each check.
#           If not specified then the default value is 10 seconds.
#           See also "max-attempts" and "max-time".
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "max-time":
#           Scalar variable with integer value of the maximum number of seconds
#           the script will wait for the deployments/statefulsets to have been
#           scaled to the wanted value.
#           If not specified or value is 0 then no repeated check is done and
#           the check is only done once.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#       "return-message":
#           Scalar reference to variable where the result message will be
#           written indicating for what reason the check failed.
#
# Return values:
#    - 0: The check was successful.
#    - 1: The check was unsuccessful.
#    - 2: Error while checking scaling status
#
# -----------------------------------------------------------------------------
sub check_daemonset_status {
    my %params = %{$_[0]};

    # Initialize local variables
    my $check_name              = exists $params{"check-name"} ? $params{"check-name"} : "";
    my $daemonset_line          = "";
    my $debug_messages          = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $delay_time              = exists $params{"delay-time"} ? $params{"delay-time"} : 10;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $max_time                = exists $params{"max-time"} ? $params{"max-time"} : 0;
    my $message                 = "";
    my $namespace               = exists $params{"namespace"} ? $params{"namespace"} : "";
    my $rc;
    my $remaining_time          = 0;
    my @result                  = ();
    my $return_message          = exists $params{"return-message"} ? $params{"return-message"} : undef;
    my $scale_line              = "";
    my @std_error = ();
    my $stop_time               = time + $max_time;

    if ($check_name eq "") {
        $message = "No 'check-name' specified'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    }

    if ($delay_time !~ /^\d+$/) {
        $message = "Parameter 'delay-time' ($delay_time) can only be an integer value'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    }

    if ($max_time !~ /^\d+$/) {
        $message = "Parameter 'max-time' ($max_time) can only be an integer value'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    }

    General::Logging::log_write("Checking that daemonset $check_name are all up, i.e. all values are the same for a maximum of $max_time seconds") if $debug_messages;

    while (1) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl get daemonset $check_name --no-headers" . ($namespace ne "" ? " --namespace $namespace" : "") . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
                "hide-output"   => $hide_output,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );

        if ($rc != 0) {
            $message = "daemonset check failed because the kubectl command failed with rc=$rc";
            General::Logging::log_write($message) if $debug_messages;
            $$return_message = $message if $return_message;
            return 2;
        }

        my $found = 0;
        $daemonset_line = "";
        for (@result) {
            if (/^$check_name\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+\S+\s+\S+/) {
                #NAME                                             DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR                            AGE
                #calico-node                                      7         7         6       7            6           kubernetes.io/os=linux                   184d
                #kube-multus-ds-amd64                             7         7         7       7            7           kubernetes.io/arch=amd64                 183d
                $daemonset_line = $_;
                # NAME               READY   UP-TO-DATE   AVAILABLE   AGE
                # eric-sepp-worker   2/2     2            2           22h
                if ($1 == $2 && $1 == $3 && $1 == $4 && $1 == $5) {
                    $found = 1;
                    last;
                }
            }
        }
        if ($found == 1) {
            # The deployment has properly scaled
            $message = "The daemonset $check_name is all up.";
            General::Logging::log_write($message) if $debug_messages;
            $$return_message = $message if $return_message;
            return 0;
        } elsif (time() < $stop_time) {
            # We need to wait longer for it to finish the scaling operation
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "confirm-interrupt" => 0,
                    "progress-message"  => 0,
                    "seconds"           => $delay_time,
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                $message = "Wait interrupted by user pressing CTRL-C";
                General::Logging::log_write($message) if $debug_messages;
                $$return_message = $message if $return_message;
                return 1;
            }
            # We now try the command again
        } else {
            # We have waited long enough, throw an error
            $message = "The daemonset $check_name is not yet all up.\n$daemonset_line\n";
            General::Logging::log_write($message) if $debug_messages;
            $$return_message = $message if $return_message;
            return 1;
        }
    }
}

# -----------------------------------------------------------------------------
# Check the deployment status of one or all deployments in a specific namespace
# to make sure READY, UP-TO-DATE and AVAILABLE all have the same value.
# It can also wait for a specific time for the deployments to get into the
# proper state.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "delay-time":
#           Scalar variable with integer value of the number of seconds to wait
#           between each check.
#           If not specified then the default value is 10 seconds.
#           See also "max-attempts" and "max-time".
#       "deployment-name":
#           Scalar variable of the name of the deployment to check, if not
#           specified then all deployments in the namespace will be checked.
#           It can also be a Perl regular expression in which case it checks that
#           all matching deployments fullfill the wanted conditions.
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "kubeconfig":
#           Scalar variable pointing to the config file to use if other than
#           the default $kubeconfig_file.
#       "max-attempts":
#           Scalar variable with integer value of the maximum number of attempts
#           the script will try for the check to fullfill the wanted conditions
#           where all values are the same.
#           See also "max-time" and "delay-time".
#           If not specified then no repeated check is done and the check is
#           only done once.
#       "max-time":
#           Scalar variable with integer value of the maximum number of seconds
#           the script will wait for the check to fullfill the wanted conditions
#           where all values are the same.
#           See also "max-attempts" and "delay-time".
#           If not specified then no repeated check is done and the check is
#           only done once.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#       "progress-messages":
#           Scalar variable indicating if a progress message should be printed
#           (=1) or not (=0) when repeated checks are done.
#           The default if not specified is to not show progress messages (=0).
#       "repeated-checks":
#           Scalar variable indicating if repeated checks should be done even
#           when all Deployments match expected status.
#           If the value is "0" (also the default if not specified) then no
#           repeated checks of Deployment status will be done.
#           If the value is "1" and if all conditions match, then repeated
#           checks will either continue until "max-attempts" or "max-time"
#           expire whichever comes first, or if repeated-check-time is set
#           to a non-zero value it will continue checking for this many seconds
#           that the conditions match, every time the conditions no longer
#           match is the timer reset.
#           This parameter can be useful when the status e.g. come up for a
#           short while and then goes down again and then comes up and stays
#           up. This way we can wait until the Deployments stays up.
#       "repeated-check-time":
#           Scalar variable with integer value of the number of seconds all
#           deployments must match the wanted conditions.
#           This is used to for example wait for deployments to stay in expected
#           condition before reporting success e.g. if you want the deployments to
#           come to wanted conditions and then stay there for 2 minutes then
#           call the sub routine with "reapeated-checks=1" and with
#           "repeated-check-time=120".
#       "return-failed-deployments":
#           Array reference of variable where information about deployments that
#           failed the check and for what reason the check failed is returned
#           to the caller.
#       "return-output":
#           Array reference of variable where the result of the last executed
#           check is returned to the caller.
#
# Return values:
#    - 0: The check was successful and it matched the wanted conditions.
#    - 1: The check was unsuccessful and it did not match the wanted conditions.
#    - 2: Error while checking POD status
#
# -----------------------------------------------------------------------------
sub check_deployment_status {
    my %params = %{$_[0]};

    # Initialize local variables
    my $all_deployments_are_ok;
    my $debug_messages            = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $delay_time                = exists $params{"delay-time"} ? $params{"delay-time"} : 10;
    my $deployment_name           = exists $params{"deployment-name"} ? $params{"deployment-name"} : "";
    my @failed_deployments        = ();
    my $hide_output               = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $kubeconfig                = exists $params{"kubeconfig"} ? $params{"kubeconfig"} : $kubeconfig_file;
    my $line;
    my $max_attempts              = exists $params{"max-attempts"} ? $params{"max-attempts"} : 0;
    my $max_time                  = exists $params{"max-time"} ? $params{"max-time"} : 0;
    my $namespace                 = exists $params{"namespace"} ? $params{"namespace"} : "";
    my $num_attempts              = 0;
    my $progress_messages         = exists $params{"progress-messages"} ? $params{"progress-messages"} : 0;
    my $rc;
    my $reattempt_cnt             = 0;
    my $remaining_time            = 0;
    my @result                    = ();
    my $repeated_checks           = exists $params{"repeated-checks"} ? $params{"repeated-checks"} : 0;
    my $repeated_check_time       = exists $params{"repeated-check-time"} ? $params{"repeated-check-time"} : 0;
    my $return_failed_deployments = exists $params{"return-failed-deployments"} ? 1 : 0;
    my $return_output             = exists $params{"return-output"} ? 1 : 0;
    my @std_error = ();
    my $stop_time                 = time + $max_time;

    # Set initial remaining time
    $remaining_time = $repeated_check_time;

    while (1) {
        $num_attempts++;

        $reattempt_cnt = 0;

TRY_COMMAND_AGAIN:

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl get deployment --namespace $namespace" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : ""),
                "hide-output"   => $hide_output,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc != 0) {
            if ($result[0] =~ /^Error from server: etcdserver: leader changed/) {
                if ($reattempt_cnt == 0) {
                    # Maybe some timing issue, wait a bit and try again
                    General::Logging::log_write("'leader changed': Waiting '$delay_time' seconds before checking Deployment status again.\n") if $debug_messages;
                    $rc = General::OS_Operations::sleep_with_progress(
                        {
                            "allow-interrupt"   => 1,
                            "confirm-interrupt" => $hide_output ? 0 : 1,
                            "progress-interval" => $delay_time > 59 ? 10 : 1,
                            "progress-message"  => $hide_output ? 0 : 1,
                            "seconds"           => $delay_time,
                            "use-logging"       => 1,
                        }
                    );
                    if ($rc == 0) {
                        # CTRL-C not pressed
                        $reattempt_cnt++;
                        goto TRY_COMMAND_AGAIN;
                    }
                }
            }

            General::Logging::log_write("Failed to execute the command\n") if $debug_messages;
            $rc = 2;
            last;
        }

        # Mark that all POD's are OK
        $all_deployments_are_ok = 1;
        @failed_deployments = ();
        my $found = 0;
        my $deployment;
        my $ready_1;
        my $ready_2;
        my $up_to_date;
        my $available;

        for $line (@result) {
            next if ($line =~ /^NAME\s+READY\s+UP-TO-DATE\s+AVAILABLE\s+AGE.*/);    # Ignore heading line
            if ($line =~ /^(\S+)\s+(\d+)\/(\d+)\s+(\d+)\s+(\d+)\s+\S+/) {
                # NAME               READY   UP-TO-DATE   AVAILABLE   AGE
                # eric-sepp-worker   0/2     2            0           22h
                $deployment = $1;
                $ready_1    = $2;
                $ready_2    = $3;
                $up_to_date = $4;
                $available  = $5;
                if ($deployment_name ne "") {
                    next unless ($deployment =~ /^$deployment_name/);
                }
                if ($ready_1 == $ready_2 && $ready_1 == $up_to_date && $ready_1 == $available) {
                    # All values are the same, everything is OK
                    $found++;
                } else {
                    $all_deployments_are_ok = 0;
                    general::logging::log_write("the READY, UP-TO-DATE and AVAILABLE are not the 'same'.\n$line\n") if $debug_messages;
                    push @failed_deployments, "$1 (READY is $2/$3, UP-TO-DATE is $4, AVAILABLE is $5)";
                }
            } else {
                # Unknown format, just ignore it
                General::Logging::log_write("Unknown line format detected, ignore line.\n$line\n") if $debug_messages;
                next;
            }
        }

        if ($all_deployments_are_ok == 1 && $found > 0) {
            # All wanted deployments are matching the conditions
            $rc = 0;
            if ($repeated_checks == 0) {
                # We don't need to check anymore
                if ($progress_messages) {
                    General::Logging::log_user_message("All deployments match the expected conditions.\n");
                } else {
                    General::Logging::log_write("All deployments match the expected conditions.\n") if $debug_messages;
                }
                last;
            }
            # If we come here even though all conditions match, we will continue to check if we need
            # to repeat the checks a number of times or for a number of seconds.
            # If no such conditions has been set then we will anyway exit the loop in the 'else' branch
            # below.
        } else {
            # One or more deployments are not matching one or more conditions
            if ($progress_messages) {
                General::Logging::log_user_message(sprintf "There are %d deployments not matching expected conditions.\n", scalar @failed_deployments);
            } else {
                General::Logging::log_write(sprintf "There are %d deployments not matching expected conditions.\n", scalar @failed_deployments) if $debug_messages;
            }
            $rc = 1;

            # Reset remaining time
            $remaining_time = $repeated_check_time;
        }

        if ($all_deployments_are_ok == 1 && $remaining_time > 0) {
            $remaining_time = $remaining_time - $delay_time;
            if ($remaining_time > 0) {
                if ($progress_messages) {
                    General::Logging::log_user_message("All deployments are OK, but waiting $remaining_time seconds to make sure they stay up.\n");
                } else {
                    General::Logging::log_write("All deployments are OK, but waiting $remaining_time seconds to make sure they stay up.\n");
                }
            } else {
                # Set remaining time to negative value so we are sure to exit the next time
                $remaining_time = -1;
            }
        } elsif ($all_deployments_are_ok == 1 && $remaining_time == -1) {
            # The deployments has matched the wanted conditions
            if ($progress_messages) {
                General::Logging::log_user_message("Maximum wait time '$repeated_check_time' reached where deployments are matching conditions.\n");
            } else {
                General::Logging::log_write("Maximum wait time '$repeated_check_time' reached where deployments are matching conditions.\n") if $debug_messages;
            }
            last;
        } elsif ($max_attempts > 0) {
            if ($num_attempts >= $max_attempts) {
                if ($progress_messages) {
                    General::Logging::log_user_message("Maximum number of attempts '$max_attempts' reached.\n");
                } else {
                    General::Logging::log_write("Maximum number of attempts '$max_attempts' reached.\n") if $debug_messages;
                }
                last;
            }
            if ($max_time > 0 && time >= $stop_time) {
                if ($progress_messages) {
                    General::Logging::log_user_message("Maximum time '$max_time' reached before maximum number of attempts '$max_attempts'.\n");
                } else {
                    General::Logging::log_write("Maximum time '$max_time' reached before maximum number of attempts '$max_attempts'.\n") if $debug_messages;
                }
                last;
            }
        } elsif ($max_time > 0) {
            if (time >= $stop_time) {
                if ($progress_messages) {
                    General::Logging::log_user_message("Maximum time '$max_time' reached.\n");
                } else {
                    General::Logging::log_write("Maximum time '$max_time' reached.\n") if $debug_messages;
                }
                last;
            }
        } else {
                if ($progress_messages) {
                    General::Logging::log_user_message("No more POD checking needed.\n");
                } else {
                    General::Logging::log_write("No more POD checking needed.\n") if $debug_messages;
                }
            last;
        }

        if (@failed_deployments) {
            General::Logging::log_write("Failed Deployments are:\n" . (join "\n", @failed_deployments) . "\n");
        }

        General::Logging::log_write("Waiting '$delay_time' seconds before checking Deployment status again.\n") if $debug_messages;
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => $hide_output ? 0 : 1,
                "progress-interval" => $delay_time > 59 ? 10 : 1,
                "progress-message"  => $hide_output ? 0 : 1,
                "seconds"           => $delay_time,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            # CTRL-C pressed
            last;
        }
    }   # while (1)

    # Check if we need to return the result from the last executed command
    if ($return_output) {
        @{$params{"return-output"}} = @result;
    }

    # Check if we need to return the failed deployments from the last executed command
    if ($return_failed_deployments) {
        @{$params{"return-failed-deplyments"}} = @failed_deployments;
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Check the pod status of one or more pods if they are in wanted status or
# in Running state if nothing is specified.
# The user can specify the pods to check or if nothing specified then all pods
# will be checked.
# The user can also specify if they want to have a check for a specific STATUS
# or READY state. If nothing specified it will just check if the pod is in
# "Running" or "Completed" status.
# It can also check that the pods are in a specific READY state, if not specified
# then this is not checked.
# It can also wait for a specific time for the pod to get into the wanted state.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "all-namespaces":
#           If specified then PODs from all name spaces will be checked.
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "delay-time":
#           Scalar variable with integer value of the number of seconds to wait
#           between each check.
#           If not specified then the default value is 10 seconds.
#           See also "max-attempts" and "max-time".
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "ignore-ready-check-for-completed-status":
#           If specified and set to value 1 then the ready check is ignored on
#           pods in "Completed" status. Pods in Completed status are usually
#           Jobs which will never reach x/x ready but is usually always 0/1 so
#           by setting this parameter these Pod's can be ignored from being
#           reported as faulty.
#           The default is to include these Completed pods also in the ready
#           check when parameter "wanted-ready" is specified.
#       "kubeconfig":
#           Scalar variable pointing to the config file to use if other than
#           the default $kubeconfig_file.
#       "max-attempts":
#           Scalar variable with integer value of the maximum number of attempts
#           the script will try for the check to fullfill the wanted conditions
#           like "wanted-ready", "wanted-status" etc.
#           See also "max-time" and "delay-time".
#           If not specified then no repeated check is done and the check is
#           only done once.
#       "max-time":
#           Scalar variable with integer value of the maximum number of seconds
#           the script will wait for the check to fullfill the wanted conditions
#           like "wanted-ready", "wanted-status" etc.
#           See also "max-attempts" and "delay-time".
#           If not specified then no repeated check is done and the check is
#           only done once.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#           See also the "all-namespaces" parameter.
#       "pod-exclude-list":
#           Array reference of variable containing a list of PODs to exclude
#           from the check, this parameter makes no sense if used with the
#           "pod-include-list" parameter since the pod-exlude-list has higher
#           precidence.
#           If this parameter is not included then the check will be done on
#           all PODs.
#       "pod-exclude-ready-list":
#           Array reference of variable containing a list of PODs to exclude
#           from the ready check, this parameter makes no sense unless also the
#           "wanted-ready" parameter is also specified.
#           If the pod is in the "pod-exclude-ready-list" then the ready
#           check is skipped for this pod.
#           If this parameter is not included and "wanted-ready" does not
#           match then then the pod is counted as not ready.
#       "pod-exclude-status-list":
#           Array reference of variable containing a list of PODs to exclude
#           from the status check, this parameter makes no sense unless also
#           the "wanted-status" parameter is also specified.
#           If the pod is in the "pod-exclude-status-list" then the status
#           check is skipped for this pod.
#           If this parameter is not included and "wanted-status" does not
#           match then then the pod is counted as not ready.
#       "pod-include-list":
#           Array reference of variable containing a list of PODs to check.
#           If this parameter is not included then the check will be done on
#           all PODs.
#       "progress-messages":
#           Scalar variable indicating if a progress message should be printed
#           (=1) or not (=0) when repeated checks are done.
#           The default if not specified is to not show progress messages (=0).
#       "repeated-checks":
#           Scalar variable indicating if repeated checks should be done even
#           when all PODs match expected status.
#           If the value is "0" (also the default if not specified) then no
#           repeated checks of POD status will be done.
#           If the value is "1" and if all conditions match, then repeated
#           checks will either continue until "max-attempts" or "max-time"
#           expire whichever comes first, or if repeated-check-time is set
#           to a non-zero value it will continue checking for this many seconds
#           that the conditions match, every time the conditions no longer
#           match is the timer reset.
#           This parameter can be useful when the status e.g. come up for a
#           short while and then goes down again and then comes up and stays
#           up. This way we can wait until the POD stays up.
#       "repeated-check-time":
#           Scalar variable with integer value of the number of seconds all
#           pods must match the wanted conditions.
#           This is used to for example wait for pods to stay in expected
#           condition before reporting success e.g. if you want the pods to
#           come to wanted conditions and then stay there for 2 minutes then
#           call the sub routine with "reapeated-checks=1" and with
#           "repeated-check-time=120".
#       "return-failed-pods":
#           Array reference of variable where information about POD's that
#           failed the check and for what reason the check failed is returned
#           to the caller.
#       "return-output":
#           Array reference of variable where the result of the last executed
#           check is returned to the caller.
#       "wanted-ready":
#           Scalar variable containing a Perl regular expression indicating
#           what the READY column should contain.
#           If not specified then no check is done on the READY column, but
#           if value is "same" then a check is done to make sure the "x/y"
#           value is the same e.g. "6/6".
#           Example of regular expression:
#               5\/6
#               ^([3-6]\/6$
#           See also parameter "pod-exclude-ready-list".
#       "wanted-status":
#           Scalar variable containing a Perl regular expression indicating
#           what the STATUS column should contain.
#           If not specified then no check is done on the STATUS column, but
#           if the value is "up" then a check will be done using the regular
#           expression "^(Running|Completed)$".
#           Example of regular expression:
#               Running
#               Completed
#               CrashLoopBackOff
#               ^(Running|Completed)$
#           See also parameter "pod-exclude-status-list".
#
# Return values:
#    - 0: The check was successful and it matched the wanted conditions.
#    - 1: The check was unsuccessful and it did not match the wanted conditions.
#    - 2: Error while checking POD status
#
# -----------------------------------------------------------------------------
sub check_pod_status {
    my %params = %{$_[0]};

    # Initialize local variables
    my $all_namespaces          = exists $params{"all-namespaces"} ? $params{"all-namespaces"} : 0;
    my $all_pods_are_ok;
    my $debug_messages          = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $delay_time              = exists $params{"delay-time"} ? $params{"delay-time"} : 10;
    my @failed_pods             = ();
    my %failed_pods;
    my $ignore_completed_status = exists $params{"ignore-ready-check-for-completed-status"} ? $params{"ignore-ready-check-for-completed-status"} : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $kubeconfig                = exists $params{"kubeconfig"} ? $params{"kubeconfig"} : $kubeconfig_file;
    my $line;
    my $max_attempts            = exists $params{"max-attempts"} ? $params{"max-attempts"} : 0;
    my $max_time                = exists $params{"max-time"} ? $params{"max-time"} : 0;
    my $namespace               = exists $params{"namespace"} ? $params{"namespace"} : "";
    my $num_attempts            = 0;
    my $pod_age;
    my $pod_exclude_list        = exists $params{"pod-exclude-list"} ? $params{"pod-exclude-list"} : undef;
    my $pod_exclude_ready_list  = exists $params{"pod-exclude-ready-list"} ? $params{"pod-exclude-ready-list"} : undef;
    my $pod_exclude_status_list = exists $params{"pod-exclude-status-list"} ? $params{"pod-exclude-status-list"} : undef;
    my $pod_include_list        = exists $params{"pod-include-list"} ? $params{"pod-include-list"} : undef;
    my $pod_name;
    my $pod_namespace;
    my $pod_ready;
    my $pod_ready_1;
    my $pod_ready_2;
    my $pod_restarts;
    my $pod_status;
    my $progress_messages       = exists $params{"progress-messages"} ? $params{"progress-messages"} : 0;
    my $rc;
    my $reattempt_cnt           = 0;
    my $remaining_time          = 0;
    my @result                  = ();
    my $repeated_checks         = exists $params{"repeated-checks"} ? $params{"repeated-checks"} : 0;
    my $repeated_check_time     = exists $params{"repeated-check-time"} ? $params{"repeated-check-time"} : 0;
    my $return_failed_pods      = exists $params{"return-failed-pods"} ? 1 : 0;
    my $return_output           = exists $params{"return-output"} ? 1 : 0;
    my @std_error = ();
    my $stop_time               = time + $max_time;
    my $wanted_ready            = exists $params{"wanted-ready"} ? $params{"wanted-ready"} : "";
    my $wanted_status           = exists $params{"wanted-status"} ? $params{"wanted-status"} : "";

    # Set initial remaining time
    $remaining_time = $repeated_check_time;

    while (1) {
        $num_attempts++;

        $reattempt_cnt = 0;

TRY_COMMAND_AGAIN:

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl get pod" . ($namespace ne "" ? " --namespace $namespace" : "") . ($all_namespaces ? " --all-namespaces" : "") . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : ""),
                "hide-output"   => $hide_output,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc != 0) {
            if ($std_error[0] =~ /^Error from server: etcdserver: leader changed/) {
                if ($reattempt_cnt == 0) {
                    # Maybe some timing issue, wait a bit and try again
                    General::Logging::log_write("'leader changed': Waiting '$delay_time' seconds before checking POD status again.\n") if $debug_messages;
                    $rc = General::OS_Operations::sleep_with_progress(
                        {
                            "allow-interrupt"   => 1,
                            "confirm-interrupt" => $hide_output ? 0 : 1,
                            "progress-interval" => $delay_time > 59 ? 10 : 1,
                            "progress-message"  => $hide_output ? 0 : 1,
                            "seconds"           => $delay_time,
                            "use-logging"       => 1,
                        }
                    );
                    if ($rc == 0) {
                        # CTRL-C not pressed
                        $reattempt_cnt++;
                        goto TRY_COMMAND_AGAIN;
                    }
                }
            }

            General::Logging::log_write("Failed to execute the command\n") if $debug_messages;
            $rc = 2;
            last;
        }

        # Mark that all POD's are OK
        $all_pods_are_ok = 1;
        @failed_pods = ();
        undef %failed_pods;

        for $line (@result) {
            next if ($line =~ /^NAMESPACE\s+NAME\s+READY.+/);    # Ignore heading line
            next if ($line =~ /^NAME\s+READY.+/);    # Ignore heading line
            if ($line =~ /^(\S+)\s+(\S+)\s+((\d+)\/(\d+))\s+(\S+)\s+(\d+)\s+(\S+)\s*$/) {
                # NAMESPACE       NAME                                                              READY   STATUS      RESTARTS   AGE
                # eiffelesc       eric-bsf-diameter-7dff78b4c7-p9vz9                                3/4     Running     0          34m   <--- This line
                $pod_namespace = $1;
                $pod_name      = $2;
                $pod_ready     = $3;
                $pod_ready_1   = $4;
                $pod_ready_2   = $5;
                $pod_status    = $6;
                $pod_restarts  = $7;
                $pod_age       = $8;

                # Check if we have the wanted namespace
                if ($namespace ne "" && $pod_namespace !~ /$namespace/) {
                    General::Logging::log_write("Not wanted namespace '$namespace'.\n$line\n") if $debug_messages;
                    next;
                }
            } elsif ($line =~ /^(\S+)\s+(\S+)\s+((\d+)\/(\d+))\s+(\S+)\s+(\d+)\s+\(.+\)\s+(\S+)\s*$/) {
                # NAMESPACE       NAME                                                              READY   STATUS      RESTARTS         AGE
                # eiffelesc       eric-bsf-diameter-7dff78b4c7-p9vz9                                3/4     Running     1 (33m ago)      34m   <--- This line
                $pod_namespace = $1;
                $pod_name      = $2;
                $pod_ready     = $3;
                $pod_ready_1   = $4;
                $pod_ready_2   = $5;
                $pod_status    = $6;
                $pod_restarts  = $7;
                $pod_age       = $8;

                # Check if we have the wanted namespace
                if ($namespace ne "" && $pod_namespace !~ /$namespace/) {
                    General::Logging::log_write("Not wanted namespace '$namespace'.\n$line\n") if $debug_messages;
                    next;
                }
            } elsif ($line =~ /^(\S+)\s+((\d+)\/(\d+))\s+(\S+)\s+(\d+)\s+(\S+)\s*$/) {
                # NAME                                                              READY   STATUS      RESTARTS   AGE
                # eric-bsf-diameter-7dff78b4c7-p9vz9                                3/4     Running     0          34m   <--- This line
                $pod_name      = $1;
                $pod_ready     = $2;
                $pod_ready_1   = $3;
                $pod_ready_2   = $4;
                $pod_status    = $5;
                $pod_restarts  = $6;
                $pod_age       = $7;
            } elsif ($line =~ /^(\S+)\s+((\d+)\/(\d+))\s+(\S+)\s+(\d+)\s+\(.+\)\s+(\S+)\s*$/) {
                # NAME                                                              READY   STATUS      RESTARTS         AGE
                # eric-bsf-diameter-7dff78b4c7-p9vz9                                3/4     Running     1 (33m ago)      34m   <--- This line
                $pod_name      = $1;
                $pod_ready     = $2;
                $pod_ready_1   = $3;
                $pod_ready_2   = $4;
                $pod_status    = $5;
                $pod_restarts  = $6;
                $pod_age       = $7;
            } else {
                # Unknown format, just ignore it
                General::Logging::log_write("Unknown line format detected, ignore line.\n$line\n") if $debug_messages;
                next;
            }
            # Check if some specified PODs should be excluded from the check
            if ($pod_exclude_list) {
                my $found = 0;
                my $lines = 0;
                for my $name (@$pod_exclude_list) {
                    $lines++;
                    if ($pod_name =~ /$name/) {
                        $found = 1;
                        last;
                    }
                }
                if ($lines > 0) {
                    if ($found) {
                        General::Logging::log_write("The pod '$pod_name' is in the exclude list.\n$line\n") if $debug_messages;
                        next;
                    }
                    General::Logging::log_write("The pod '$pod_name' is not in the exclude list.\n$line\n") if $debug_messages;
                }
            }
            # Check if some specified PODs should be included in the check
            if ($pod_include_list) {
                my $found = 0;
                my $lines = 0;
                for my $name (@$pod_include_list) {
                    $lines++;
                    if ($pod_name =~ /$name/) {
                        $found = 1;
                        last;
                    }
                }
                if ($lines > 0) {
                    unless ($found) {
                        General::Logging::log_write("The pod '$pod_name' is not in the include list.\n$line\n") if $debug_messages;
                        next;
                    }
                    General::Logging::log_write("The pod '$pod_name' is in the include list.\n$line\n") if $debug_messages;
                }
            }

            # If we reached this point we need to check if any of the other conditions are matching

            if ($wanted_status ne "") {
                if ($pod_exclude_status_list) {
                    my $found = 0;
                    my $lines = 0;
                    for my $name (@$pod_exclude_status_list) {
                        $lines++;
                        if ($pod_name =~ /$name/) {
                            $found = 1;
                            last;
                        }
                    }
                    if ($lines > 0) {
                        if ($found) {
                            # We should not check the status of this pod
                            General::Logging::log_write("The pod '$pod_name' is in the exclude status list.\n$line\n") if $debug_messages;
                            next;
                        }
                        General::Logging::log_write("The pod '$pod_name' is not in the exclude status list.\n$line\n") if $debug_messages;
                    }
                }
                if ($wanted_status eq "up") {
                    if ($pod_status !~ /^(Completed|Running)$/) {
                        $all_pods_are_ok = 0;
                        General::Logging::log_write("The pod STATUS is not 'up' (Completed or Running).\n$line\n") if $debug_messages;
                        push @{$failed_pods{$pod_name}}, "STATUS is $pod_status";
                    }
                } elsif ($pod_status !~ /$wanted_status/) {
                    $all_pods_are_ok = 0;
                    General::Logging::log_write("The pod STATUS is not matching the wanted '$wanted_status' value.\n$line\n") if $debug_messages;
                    push @{$failed_pods{$pod_name}}, "STATUS is $pod_status";
                }
                # If we end up here then the check for STATUS match and we need to continue checking other conditions
            }

            if ($wanted_ready ne "") {
                if ($pod_exclude_ready_list) {
                    my $found = 0;
                    my $lines = 0;
                    for my $name (@$pod_exclude_ready_list) {
                        $lines++;
                        if ($pod_name =~ /$name/) {
                            $found = 1;
                            last;
                        }
                    }
                    if ($lines > 0) {
                        if ($found) {
                            # We should not check the ready of this pod
                            General::Logging::log_write("The pod '$pod_name' is in the exclude ready list.\n$line\n") if $debug_messages;
                            next;
                        }
                        General::Logging::log_write("The pod '$pod_name' is not in the exclude ready list.\n$line\n") if $debug_messages;
                    }
                }
                if ($wanted_ready eq "same") {
                    if ($pod_ready_1 != $pod_ready_2) {
                        unless ($ignore_completed_status == 1 && $pod_status eq "Completed") {
                            $all_pods_are_ok = 0;
                            General::Logging::log_write("The pod READY are not the 'same'.\n$line\n") if $debug_messages;
                            push @{$failed_pods{$pod_name}}, "READY is $pod_ready_1/$pod_ready_2";
                        }
                    }
                } elsif ($pod_ready !~ /$wanted_ready/) {
                    unless ($ignore_completed_status == 1 && $pod_status eq "Completed") {
                        $all_pods_are_ok = 0;
                        General::Logging::log_write("The pod READY is not matching the wanted '$wanted_ready' value.\n$line\n") if $debug_messages;
                        push @{$failed_pods{$pod_name}}, "READY is $pod_ready";
                    }
                }
                # If we end up here then the check for READY match and we need to continue checking other conditions
            }
        }

        if ($all_pods_are_ok == 1) {
            # All wanted PODs are matching the conditions
            $rc = 0;
            if ($repeated_checks == 0) {
                # We don't need to check anymore
                if ($progress_messages) {
                    General::Logging::log_user_message("All PODs match the expected conditions.\n");
                } else {
                    General::Logging::log_write("All PODs match the expected conditions.\n") if $debug_messages;
                }
                last;
            }
            # If we come here even though all conditions match, we will continue to check if we need
            # to repeat the checks a number of times or for a number of seconds.
            # If no such conditions has been set then we will anyway exit the loop in the 'else' branch
            # below.
        } else {
            # One or more PODs are not matching one or more conditions
            if (%failed_pods) {
                for $pod_name (sort keys %failed_pods) {
                    my $message = "$pod_name (";
                    for (@{$failed_pods{$pod_name}}) {
                        $message .= "$_, ";
                    }
                    $message =~ s/,\s+$//g;
                    push @failed_pods, "$message)";
                }
            }
            if ($progress_messages) {
                General::Logging::log_user_message(sprintf "There are %d PODs not matching expected conditions.\n", scalar @failed_pods);
            } else {
                General::Logging::log_write(sprintf "There are %d PODs not matching expected conditions.\n", scalar @failed_pods) if $debug_messages;
            }
            $rc = 1;

            # Reset remaining time
            $remaining_time = $repeated_check_time;
        }

        if ($all_pods_are_ok == 1 && $remaining_time > 0) {
            $remaining_time = $remaining_time - $delay_time;
            if ($remaining_time > 0) {
                if ($progress_messages) {
                    General::Logging::log_user_message("All PODs are OK, but waiting $remaining_time seconds to make sure they stay up.\n");
                } else {
                    General::Logging::log_write("All PODs are OK, but waiting $remaining_time seconds to make sure they stay up.\n");
                }
            } else {
                # Set remaining time to negative value so we are sure to exit the next time
                $remaining_time = -1;
            }
        } elsif ($all_pods_are_ok == 1 && $remaining_time == -1) {
            # The pods has matched the wanted conditions
            if ($progress_messages) {
                General::Logging::log_user_message("Maximum wait time '$repeated_check_time' reached where pods are matching conditions.\n");
            } else {
                General::Logging::log_write("Maximum wait time '$repeated_check_time' reached where pods are matching conditions.\n") if $debug_messages;
            }
            last;
        } elsif ($max_attempts > 0) {
            if ($num_attempts >= $max_attempts) {
                if ($progress_messages) {
                    General::Logging::log_user_message("Maximum number of attempts '$max_attempts' reached.\n");
                } else {
                    General::Logging::log_write("Maximum number of attempts '$max_attempts' reached.\n") if $debug_messages;
                }
                last;
            }
            if ($max_time > 0 && time >= $stop_time) {
                if ($progress_messages) {
                    General::Logging::log_user_message("Maximum time '$max_time' reached before maximum number of attempts '$max_attempts'.\n");
                } else {
                    General::Logging::log_write("Maximum time '$max_time' reached before maximum number of attempts '$max_attempts'.\n") if $debug_messages;
                }
                last;
            }
        } elsif ($max_time > 0) {
            if (time >= $stop_time) {
                if ($progress_messages) {
                    General::Logging::log_user_message("Maximum time '$max_time' reached.\n");
                } else {
                    General::Logging::log_write("Maximum time '$max_time' reached.\n") if $debug_messages;
                }
                last;
            }
        } else {
                if ($progress_messages) {
                    General::Logging::log_user_message("No more POD checking needed.\n");
                } else {
                    General::Logging::log_write("No more POD checking needed.\n") if $debug_messages;
                }
            last;
        }

        if (@failed_pods) {
            General::Logging::log_write("Failed POD's are:\n" . (join "\n", @failed_pods) . "\n");
        }

        General::Logging::log_write("Waiting '$delay_time' seconds before checking POD status again.\n") if $debug_messages;
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => $hide_output ? 0 : 1,
                "progress-interval" => $delay_time > 59 ? 10 : 1,
                "progress-message"  => $hide_output ? 0 : 1,
                "seconds"           => $delay_time,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            # CTRL-C pressed
            last;
        }
    }   # while (1)

    # Check if we need to return the result from the last executed command
    if ($return_output) {
        @{$params{"return-output"}} = @result;
    }

    # Check if we need to return the failed pods from the last executed command
    if ($return_failed_pods) {
        @{$params{"return-failed-pods"}} = @failed_pods;
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# Check if a deployment or statefulset is scaled to a specific value with the
# possibility to wait for a specific time for it to be fullfilled.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "check-name":
#           Scalar variable that contains the name of the type of resource to
#           check.
#       "check-type":
#           Scalar variable that contains the type of resource to check, currently
#           only the following are supported:
#           - statefulset
#           - deployment
#       "delay-time":
#           Scalar variable with integer value of the number of seconds to wait
#           between each check.
#           If not specified then the default value is 10 seconds.
#           See also "max-attempts" and "max-time".
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "max-time":
#           Scalar variable with integer value of the maximum number of seconds
#           the script will wait for the deployments/statefulsets to have been
#           scaled to the wanted value.
#           If not specified or value is 0 then no repeated check is done and
#           the check is only done once.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#       "return-message":
#           Scalar reference to variable where the result message will be
#           written indicating for what reason the check failed.
#       "scale-value":
#           Scalar variable that contains an integer value that indicates
#           the value that the resource should have been scaled to.
#
# Return values:
#    - 0: The check was successful.
#    - 1: The check was unsuccessful.
#    - 2: Error while checking scaling status
#
# -----------------------------------------------------------------------------
sub check_scaling_status {
    my %params = %{$_[0]};

    # Initialize local variables
    my $check_name              = exists $params{"check-name"} ? $params{"check-name"} : "";
    my $check_type              = exists $params{"check-type"} ? $params{"check-type"} : "";
    my $debug_messages          = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $delay_time              = exists $params{"delay-time"} ? $params{"delay-time"} : 10;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $max_time                = exists $params{"max-time"} ? $params{"max-time"} : 0;
    my $message                 = "";
    my $namespace               = exists $params{"namespace"} ? $params{"namespace"} : "";
    my $rc;
    my $remaining_time          = 0;
    my @result                  = ();
    my $return_message          = exists $params{"return-message"} ? $params{"return-message"} : undef;
    my $scale_line              = "";
    my $scale_value             = exists $params{"scale-value"} ? $params{"scale-value"} : "";
    my @std_error = ();
    my $stop_time               = time + $max_time;

    if ($check_name eq "") {
        $message = "No 'check-name' specified'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    }

    if ($check_type eq "") {
        $message = "No 'check-type' specified, it should be 'deployment' or 'statefulset'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    } elsif ($check_type !~ /^(deployment|statefulset)$/) {
        # Wrong type
        $message = "Wrong 'check-type' specified ($check_type), only 'deployment' or 'statefulset' allowed\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    }

    if ($scale_value eq "") {
        $message = "No 'scale-value' specified'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    }

    if ($delay_time !~ /^\d+$/) {
        $message = "Parameter 'delay-time' ($delay_time) can only be an integer value'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    }

    if ($max_time !~ /^\d+$/) {
        $message = "Parameter 'max-time' ($max_time) can only be an integer value'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return 2;
    }

    General::Logging::log_write("Checking that $check_type $check_name has scaled to $scale_value for a maximum of $max_time seconds") if $debug_messages;

    while (1) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl get $check_type --no-headers" . ($namespace ne "" ? " --namespace $namespace" : "") . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
                "hide-output"   => $hide_output,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );

        if ($rc != 0) {
            $message = "Scaling check failed because the kubectl command failed with rc=$rc";
            General::Logging::log_write($message) if $debug_messages;
            $$return_message = $message if $return_message;
            return 2;
        }

        my $found = 0;
        for (@result) {
            if ($check_type eq "deployment" && /^$check_name\s+(\d+)\/(\d+)\s+(\d+)\s+(\d+)\s+\S+/) {
                $scale_line = $_;
                # NAME               READY   UP-TO-DATE   AVAILABLE   AGE
                # eric-sepp-worker   2/2     2            2           22h
                if ($1 == $scale_value && $2 == $scale_value && $3 == $scale_value && $4 == $scale_value) {
                    $found = 1;
                    last;
                }
            } elsif ($check_type eq "statefulset" && /^$check_name\s+(\d+)\/(\d+)\s+\S+/) {
                $scale_line = $_;
                # NAME                                                  READY   AGE
                # eric-data-wide-column-database-cd-datacenter1-rack1   2/2     108m
                if ($1 == $scale_value && $2 == $scale_value) {
                    $found = 1;
                    last;
                }
            }
        }
        if ($found == 1) {
            # The deployment has properly scaled
            $message = "The $check_type $check_name has been properly scaled.";
            General::Logging::log_write($message) if $debug_messages;
            $$return_message = $message if $return_message;
            return 0;
        } elsif (time() < $stop_time) {
            # We need to wait longer for it to finish the scaling operation
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "confirm-interrupt" => 0,
                    "progress-message"  => 0,
                    "seconds"           => $delay_time,
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                $message = "Wait interrupted by user pressing CTRL-C";
                General::Logging::log_write($message) if $debug_messages;
                $$return_message = $message if $return_message;
                return 1;
            }
            # We now try the command again
        } else {
            # We have waited long enough, throw an error
            $message = "The $check_type $check_name is not yet ready or not scaled to $scale_value.\n$scale_line\n";
            General::Logging::log_write($message) if $debug_messages;
            $$return_message = $message if $return_message;
            return 1;
        }
    }
}

# -----------------------------------------------------------------------------
# Convert a normal number into a Kubernetes unit number.
# The unit can be any of the units m,k,M,G,T,P,E,Ki,Mi,Gi,Ti,Pi,Ei.
# The returned number can be either an integer or floating point number.
#
# Input variables:
#    - Number to convert.
#    - Output unit type, one or m,k,M,G,T,P,E,Ki,Mi,Gi,Ti,Pi,Ei.
#
# Return values:
#    - Converted number.
#
# -----------------------------------------------------------------------------
sub convert_number_to_kubernetes_unit {
    my $number = shift;
    my $unit   = shift;

    my $output = 0;

    if ($unit =~ /^m$/) {
        $output = sprintf "%f", ($number * 1000);
    } elsif ($unit =~ /^k$/) {
        $output = sprintf "%f", ($number / 1000);
    } elsif ($unit =~ /^M$/) {
        $output = sprintf "%f", ($number / 1000 / 1000);
    } elsif ($unit =~ /^G$/) {
        $output = sprintf "%f", ($number / 1000 / 1000 / 1000);
    } elsif ($unit =~ /^T$/) {
        $output = sprintf "%f", ($number / 1000 / 1000 / 1000 / 1000);
    } elsif ($unit =~ /^P$/) {
        $output = sprintf "%f", ($number / 1000 / 1000 / 1000 / 1000 / 1000);
    } elsif ($unit =~ /^E$/) {
        $output = sprintf "%f", ($number / 1000 / 1000 / 1000 / 1000 / 1000 / 1000);
    } elsif ($unit =~ /^Ki$/) {
        $output = sprintf "%f", ($number / 1024);
    } elsif ($unit =~ /^Mi$/) {
        $output = sprintf "%f", ($number / 1024 / 1024);
    } elsif ($unit =~ /^Gi$/) {
        $output = sprintf "%f", ($number / 1024 / 1024 / 1024);
    } elsif ($unit =~ /^Ti$/) {
        $output = sprintf "%f", ($number / 1024 / 1024 / 1024 / 1024);
    } elsif ($unit =~ /^Pi$/) {
        $output = sprintf "%f", ($number / 1024 / 1024 / 1024 / 1024 / 1024);
    } elsif ($unit =~ /^Ei$/) {
        $output = sprintf "%f", ($number / 1024 / 1024 / 1024 / 1024 / 1024 / 1024);
    } elsif ($unit =~ /^(\d+|-)$/) {
        # Just return the number we received
        $output = $number;
    } else {
        # Just return what we received converted to a floating point number, hopefully it is a valid number
        $output = sprintf "%f", $number;
    }

    # Remove trailing fractal 0s
    if ($output =~ /^\d+\.\d+$/) {
        $output =~ s/(\d+\.\d+?)0+$/$1/;
    }
    # Remove the fractals if they are all 0
    if ($output =~ /^(\d+)\.0+$/) {
        $output = $1;
    }

    return "$output$unit";
}

# -----------------------------------------------------------------------------
# Convert a normal number into a Kubernetes unit number with max 3 decimals
# where numbers > 4 digits (>1024) will be converted to the next higher level
# unit Ki to Mi, Mi to Gi etc.
# Units returned will always be in Ki,Mi,Gi,Ti,Pi or Ei i.e. base 1024.
# The returned number can be either an integer or floating point number.
# Example:
#   1024 => 1 Ki
#
# Input variables:
#    - Number to convert.
#
# Return values:
#    - Converted number.
#
# -----------------------------------------------------------------------------
sub convert_number_to_3_decimals {
    my $number = convert_number_unit(shift);

    my $before = $number;
    my $converted;
    my $outunit;

    for my $unit (qw /Ki Mi Gi Ti Pi Ei/) {
        if ($before =~ /^\d+$/) {
            $before = $before;
        } elsif ($before =~ /^(\d+)\.\d+$/) {
            $before = $1;
        } else {
            # Some strange format, return it unchanged
            return $number;
        }

        if ($before >= (1024 * 1024)) {
            # Use next higher unit
            $before = sprintf "%d", ($before / 1024);
        } else {
            $converted = convert_number_to_kubernetes_unit($number,$unit);
            if ($converted =~ /^(.+?)(Ki|Mi|Gi|Ti|Pi|Ei)$/) {
                $converted = sprintf "%.3f", $1;
                $outunit = $2;
                # Remove trailing fractal 0s
                if ($converted =~ /^\d+\.\d+$/) {
                    $converted =~ s/(\d+\.\d+?)0+$/$1/;
                }
                # Remove the fractals if they are all 0
                if ($converted =~ /^(\d+)\.0+$/) {
                    $converted = $1;
                }
                return "$converted $unit";
            } else {
                return $converted;
            }
        }
    }
}

# -----------------------------------------------------------------------------
# Convert a Kubernetes unit into a normal number.
# If a number ends with any of the units m,k,M,G,T,P,E,Ki,Mi,Gi,Ti,Pi,Ei then
# this function will return the actual number which might be a floating point
# number in case of "m" and a whole number multiplied with a number of 1000
# in case of "k,M,G,T,P,E" or 1024 in case of "Ki,Mi,Gi,Ti,Pi,Ei".
#
# Input variables:
#    - Number to convert, if needed.
#
# Return values:
#    - Converted number.
#
# -----------------------------------------------------------------------------
sub convert_number_unit {
    my $input = shift;

    my $output = 0;

    unless (defined $input) {
        my $print_stack_trace = 0;
        if ($print_stack_trace == 1) {
            my $i = 1;
            print STDERR "Stack Trace:\n";
            while ( (my @call_details = (caller($i++))) ){
                print STDERR $call_details[1].":".$call_details[2]." in function ".$call_details[3]."\n";
            }
        }
        return "NaN";   # Not a Number
    }

    if ($input =~ /^(\d+|\d+\.\d+)m$/) {
        $output = sprintf "%f", ($1 / 1000);
    } elsif ($input =~ /^(\d+|\d+\.\d+)k$/) {
        $output = sprintf "%f", ($1 * 1000);
    } elsif ($input =~ /^(\d+|\d+\.\d+)M$/) {
        $output = sprintf "%f", ($1 * 1000 * 1000);
    } elsif ($input =~ /^(\d+|\d+\.\d+)G$/) {
        $output = sprintf "%f", ($1 * 1000 * 1000 * 1000);
    } elsif ($input =~ /^(\d+|\d+\.\d+)T$/) {
        $output = sprintf "%f", ($1 * 1000 * 1000 * 1000 * 1000);
    } elsif ($input =~ /^(\d+|\d+\.\d+)P$/) {
        $output = sprintf "%f", ($1 * 1000 * 1000 * 1000 * 1000 * 1000);
    } elsif ($input =~ /^(\d+|\d+\.\d+)E$/) {
        $output = sprintf "%f", ($1 * 1000 * 1000 * 1000 * 1000 * 1000 * 1000);
    } elsif ($input =~ /^(\d+|\d+\.\d+)Ki$/) {
        $output = sprintf "%f", ($1 * 1024);
    } elsif ($input =~ /^(\d+|\d+\.\d+)Mi$/) {
        $output = sprintf "%f", ($1 * 1024 * 1024);
    } elsif ($input =~ /^(\d+|\d+\.\d+)Gi$/) {
        $output = sprintf "%f", ($1 * 1024 * 1024 * 1024);
    } elsif ($input =~ /^(\d+|\d+\.\d+)Ti$/) {
        $output = sprintf "%f", ($1 * 1024 * 1024 * 1024 * 1024);
    } elsif ($input =~ /^(\d+|\d+\.\d+)Pi$/) {
        $output = sprintf "%f", ($1 * 1024 * 1024 * 1024 * 1024 * 1024);
    } elsif ($input =~ /^(\d+|\d+\.\d+)Ei$/) {
        $output = sprintf "%f", ($1 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024);
    } elsif ($input =~ /^(\d+|-)$/) {
        # Just return the number we received
        $output = $input;
    } else {
        # Just return what we received converted to a floating point number, hopefully it is a valid number
        $output = sprintf "%f", $input;
    }

    # Remove trailing fractal 0s
    if ($output =~ /^\d+\.\d+$/) {
        $output =~ s/(\d+\.\d+?)0+$/$1/;
    }
    # Remove the fractals if they are all 0
    if ($output =~ /^(\d+)\.0+$/) {
        $output = $1;
    }

    return $output;
}

# -----------------------------------------------------------------------------
# Convert a Kubernetes time unit into seconds.
# If a number ends with any of the units s,m,h,d,w then this function will
# return the actual number of seconds instead.
#
# Input variables:
#    - Time unit to convert, if needed.
#
# Return values:
#    - Number of seconds, or 0 if invalid input value.
#
# -----------------------------------------------------------------------------
sub convert_time_unit_to_seconds {
    my $input = shift;

    if ($input =~ /^\d+$/) {
        return $input;
    } elsif ($input =~ /^(\d+)s$/) {
        return $1;
    } elsif ($input =~ /^(\d+)m$/) {
        return sprintf "%d", ($1 * 60);
    } elsif ($input =~ /^(\d+)h$/) {
        return sprintf "%d", ($1 * 3600);
    } elsif ($input =~ /^(\d+)d$/) {
        return sprintf "%d", ($1 * 3600 * 24);
    } elsif ($input =~ /^(\d+)w$/) {
        return sprintf "%d", ($1 * 3600 * 24 * 7);
    } else {
        # Invalid format, just return back 0
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Return a list of alarms matching a specific condition.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "alarm-names":
#           Array reference of alarm names that should be returned.
#           It should contain the exact name of the alarm with proper case.
#           If not specified then all alarms will be fetched.
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#
# Return:
#    - An array of found alarms, if no alarms found it returns "[]" in index 0.
#    - An empty array indicates some error was detected.
#
# Examples:
# @alarms = ADP::Kubernetes_Operations::get_alarms(
#     {
#         "hide-output"     => 1,
#         "namespace"       => $namespace,
#     }
# );
# if (scalar @alarms == 1 && $alarms[0] eq "[]") {
#     # No alarms
# }
# @alarms = ADP::Kubernetes_Operations::get_alarms(
#     {
#         "alarm-names"     => [ "LicenseInformationUnavailable", "PodFailure" ],
#         "hide-output"     => 1,
#         "namespace"       => $namespace,
#     }
# );
# -----------------------------------------------------------------------------
sub get_alarms {
    my %params = %{$_[0]};

    # Initialize local variables
    my $alarm_names             = exists $params{"alarm-names"} ? $params{"alarm-names"} : undef;
    my $alarm_pod_name          = "";
    my $command                 = "";
    my $extra_parameters        ="";
    my $full_alarm_list         = exists $params{"full-alarm-list"} ? $params{"full-alarm-list"} : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $namespace               = exists $params{"namespace"} ? $params{"namespace"} : "";
    my @pod_names               = ();
    my @std_error = ();
    my $rc;
    my @result                  = ();

    return () if ($namespace eq "");

ALARM_TRY_AGAIN:

    if ($cached_alarm_pod_name ne "") {
        # Try to use the cached alarm pod name to speed up the fetching
        # and not to query for it every time.
        $alarm_pod_name = $cached_alarm_pod_name;
    } else {
        # Find the alarm pod name.
        @pod_names = get_pod_names(
            {
                "hide-output"       => $hide_output,
                "namespace"         => $namespace,
                "pod-include-list"  => [ "eric-fh-alarm-handler-" ],
            }
        );
        return () unless @pod_names;
        # Pick the first pod name if more are returned
        $alarm_pod_name = $pod_names[0];
    }

    $command = "kubectl -n $namespace exec -it $alarm_pod_name -c eric-fh-alarm-handler";
    $command .= ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : "");
    $command .= " -- ahurl list ";

    $extra_parameters = "";
    if ($full_alarm_list) {
        $extra_parameters = "-v ";
    }
    if (scalar @{$alarm_names} > 0) {
        $extra_parameters .= "--filters ";
        for (@{$alarm_names}) {
            $extra_parameters .= "alarmName=$_,";
        }
        $extra_parameters =~ s/,$//;
    }
    if ($extra_parameters ne "") {
        $command .= $extra_parameters;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => $hide_output,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0) {
        # Remember the pod name until the next time
        $cached_alarm_pod_name = $alarm_pod_name;
        return @result;
    } else {
        if (grep /pods "eric-fh-alarm-handler-.+" not found/, @result) {
            # Maybe we used a cached version of the pod name, try to fetch
            # a new pod name.
            $cached_alarm_pod_name = "";
            goto ALARM_TRY_AGAIN;
        } elsif (grep /runtime exec failed: exec failed: unable to start container process: exec: "curl": executable file not found in /, @std_error) {
            # 2024-05-02: Starting with the 1.15.25+975 package the curl command is no longer supported and not installed in the eric-fh-alarm-handler pod.
            # TODO: For now we just fake it to look like there are no alarms until a better solution can be found.
            return ( "[]" );
        } elsif (grep /Error from server \(BadRequest\): container eric-fh-alarm-handler is not valid for pod/, @std_error) {
            # 2024-06-03: Starting with the 1.15.25+1xxx package the container is no longer called 'eric-fh-alarm-handler'.
            # TODO: For now we just fake it to look like there are no alarms until a better solution can be found.
            return ( "[]" );
        } elsif (grep /OCI runtime exec failed: exec failed: unable to start container process: exec: "ahurl": executable file not found in/, @std_error) {
            # 2024-06-14: With the introduction of the "new" ahurl way it does not seem to work in 1.14 and older releases,
            # so we try to execute the old logic instead.
            return get_alarms_old_curl_way( \%params );
        }
        return ();
    }
}

# -----------------------------------------------------------------------------
# Return a list of alarms matching a specific condition using the old curl way.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "alarm-names":
#           Array reference of alarm names that should be returned.
#           It should contain the exact name of the alarm with proper case.
#           If not specified then all alarms will be fetched.
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#       "pretty-print":
#           If specified and =1 then the output will be printed in an easier
#           to read format.
#           For example when =0 or not specified:
#           [{"alarmName":"LicenseInformationUnavailable","serviceName":"ericsson-sc","severity":"Critical","eventTime":"2022-02-17T11:58:00.703000 UTC","faultyResource":"SC","description":"License information is unavailable","additionalInformation":{"empty":false,"map":{"failReasons":"[NeLS is not reachable]"}}}]
#           When =1:
#           [
#               {
#                   "alarmName": "LicenseInformationUnavailable",
#                   "serviceName": "ericsson-sc",
#                   "severity": "Critical",
#                   "eventTime": "2022-02-17T11:58:00.703000 UTC",
#                   "faultyResource": "SC",
#                   "description": "License information is unavailable",
#                   "additionalInformation": {
#                       "empty": false,
#                       "map": {
#                           "failReasons": "[NeLS is not reachable]"
#                       }
#                   }
#               }
#           ]
#
# Return:
#    - An array of found alarms, if no alarms found it returns "[]" in index 0.
#    - An empty array indicates some error was detected.
#
# Examples:
# @alarms = ADP::Kubernetes_Operations::get_alarms(
#     {
#         "hide-output"     => 1,
#         "namespace"       => $namespace,
#     }
# );
# if (scalar @alarms == 1 && $alarms[0] eq "[]") {
#     # No alarms
# }
# @alarms = ADP::Kubernetes_Operations::get_alarms(
#     {
#         "alarm-names"     => [ "LicenseInformationUnavailable", "PodFailure" ],
#         "hide-output"     => 1,
#         "namespace"       => $namespace,
#         "pretty-print"    => 1,
#     }
# );
# -----------------------------------------------------------------------------
sub get_alarms_old_curl_way {
    my %params = %{$_[0]};

    # Initialize local variables
    my $alarm_names             = exists $params{"alarm-names"} ? $params{"alarm-names"} : undef;
    my $alarm_pod_name          = "";
    my $command                 = "";
    my $extra_parameters        ="";
    my $full_alarm_list         = exists $params{"full-alarm-list"} ? $params{"full-alarm-list"} : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $namespace               = exists $params{"namespace"} ? $params{"namespace"} : "";
    my @pod_names               = ();
    my $pretty_print            = exists $params{"pretty-print"} ? $params{"pretty-print"} : 0;
    my @std_error = ();
    my $use_tls                 = exists $params{"use-tls"} ? $params{"use-tls"} : 0;
    my $rc;
    my @result                  = ();

    return () if ($namespace eq "");

ALARM_TRY_AGAIN:

    if ($cached_alarm_pod_name ne "") {
        # Try to use the cached alarm pod name to speed up the fetching
        # and not to query for it every time.
        $alarm_pod_name = $cached_alarm_pod_name;
    } else {
        # Find the alarm pod name.
        @pod_names = get_pod_names(
            {
                "hide-output"       => $hide_output,
                "namespace"         => $namespace,
                "pod-include-list"  => [ "eric-fh-alarm-handler-" ],
            }
        );
        return () unless @pod_names;
        # Pick the first pod name if more are returned
        $alarm_pod_name = $pod_names[0];
    }

    $command = "kubectl -n $namespace exec -it $alarm_pod_name -c eric-fh-alarm-handler";
    $command .= ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : "");
    $command .= " -- curl -sS -X GET ";
    $extra_parameters = "";
    if ($full_alarm_list) {
        $extra_parameters = "outputFormat=FullAlarmList&";
    }
    for (@{$alarm_names}) {
        $extra_parameters .= "alarmName=$_&";
    }
    if ($pretty_print) {
        $extra_parameters .= "pp";
    }

    if ($extra_parameters eq "") {
        if ($use_tls == 0) {
            $command = "$command 'http://localhost:5005/ah/api/v0/alarms'";
        } else {
            $command = "$command 'https://localhost:5006/ah/api/v0/alarms' --cacert /var/run/secrets/siptls-root/cacertbundle.pem --cert /var/run/secrets/client-cert/clicert.pem --key /var/run/secrets/client-cert/cliprivkey.pem";
        }
    } else {
        $extra_parameters =~ s/&$//;
        if ($use_tls == 0) {
            $command = "$command 'http://localhost:5005/ah/api/v0/alarms?$extra_parameters'";
        } else {
            $command = "$command 'https://localhost:5006/ah/api/v0/alarms?$extra_parameters' --cacert /var/run/secrets/siptls-root/cacertbundle.pem --cert /var/run/secrets/client-cert/clicert.pem --key /var/run/secrets/client-cert/cliprivkey.pem";
        }
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => $hide_output,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0) {
        # Remember the pod name until the next time
        $cached_alarm_pod_name = $alarm_pod_name;
        return @result;
    } else {
        if (grep /pods "eric-fh-alarm-handler-.+" not found/, @result) {
            # Maybe we used a cached version of the pod name, try to fetch
            # a new pod name.
            $cached_alarm_pod_name = "";
            goto ALARM_TRY_AGAIN;
        } elsif (grep /Failed to connect to localhost port 5005.*: (Connection refused|Couldn't connect to server)/, @result) {
            # Maybe we have switched to use TLS, try to use secure connection instead.
            if ($use_tls == 0) {
                $cached_alarm_pod_name = $alarm_pod_name;
                $use_tls = 1;
                goto ALARM_TRY_AGAIN;
            }
        } elsif (grep /runtime exec failed: exec failed: unable to start container process: exec: "curl": executable file not found in /, @std_error) {
            # 2024-05-02: Starting with the 1.15.25+975 package the curl command is no longer supported and not installed in the eric-fh-alarm-handler pod.
            # TODO: For now we just fake it to look like there are no alarms until a better solution can be found.
            return ( "[]" );
        } elsif (grep /Error from server \(BadRequest\): container eric-fh-alarm-handler is not valid for pod/, @std_error) {
            # 2024-06-03: Starting with the 1.15.25+1xxx package the container is no longer called 'eric-fh-alarm-handler'.
            # TODO: For now we just fake it to look like there are no alarms until a better solution can be found.
            return ( "[]" );
        }
        return ();
    }
}

# -----------------------------------------------------------------------------
# Return a list of defined API-resources supported by kuberentes.
#
# Input variables:
#    -
#
# Return values:
#    - An array with a list of api-resources
#
# -----------------------------------------------------------------------------
sub get_api_resources {
    my @api_resources = ();
    my $rc;
    my @result = ();
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl api-resources" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0) {
        for (@result) {
            if (/^NAME\s+SHORTNAMES\s+(APIGROUP|APIVERSION).+/) {
                # NAME                              SHORTNAMES                                    APIGROUP                       NAMESPACED   KIND
                #   or on newer releases
                # NAME                              SHORTNAMES                                    APIVERSION                             NAMESPACED   KIND
                next;
            }
            if (/^(\S+)\s+\S+.+/) {
                # bindings                                                                                                       true         Binding
                # componentstatuses                 cs                                                                           false        ComponentStatus
                # mutatingwebhookconfigurations                                                   admissionregistration.k8s.io   false        MutatingWebhookConfiguration
                # customresourcedefinitions         crd,crds                                      apiextensions.k8s.io           false        CustomResourceDefinition
                push @api_resources, $1;
            }
        }
    }

    return @api_resources;
}

# -----------------------------------------------------------------------------
# Return the IP-address, CLI and NETCONF port numbers of the CM Yang Provider
# Service for a specific namespace.
#
# Input variables:
#    - namespace
#
# Return values:
#    - An array with the service name, IP address, CLI port number, NETCONF port
#      and NETCONF TLS port number of the CM Yang Provider service or an empty
#      strings at error.
#      For example:
#      ("eric-cm-yang-provider-external", "10.158.33.236", "22", "830", "6513")
#
# -----------------------------------------------------------------------------
sub get_cmyp_info {
    my $namespace = shift;
    my @service_names = ();
    my $service_name = "";
    my $ip_address = "";
    my $cli_port = "";
    my $netconf_port = "";
    my $netconf_tls_port = "";
    my $rc;
    my @result;
    my @std_error = ();

    return ("", "", "", "", "") unless $namespace;

    my @ip_addresses =  get_service_ip(
        {
            "check-ssh"         => 1,
            "debug-messages"    => 1,
            "hide-output"       => 1,
            "include-list"      => [ '^eric-cm-yang-provider$', '^eric-cm-yang-provider-ipv4$', '^eric-cm-yang-provider-ipv6$', '^eric-cm-yang-provider-external$' ],
            "ip-type"           => "external",
            "namespace"         => $namespace,
            "port"              => 22,
            "repeat-at-error"   => 0,
            "return-only-one"   => 1,
            "service-names"     => \@service_names,
            "timeout"           => 2,
        }
    );

    if (scalar @ip_addresses == 0) {
        # No IP-addresses found, return empty string
        return ("", "", "", "", "");
    }

    # Always return back first IP-address.
    $ip_address = $ip_addresses[0];
    $service_name = $service_names[0];

    # Fetch the netconf port
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl -n $namespace get svc $service_name -o jsonpath='{.spec.ports[0].port}' " . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0 && scalar @result == 1) {
        $netconf_port = $result[0];
    }

    # Fetch the cli port
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl -n $namespace get svc $service_name -o jsonpath='{.spec.ports[1].port}' " . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0 && scalar @result == 1) {
        $cli_port = $result[0];
    }

    # Fetch the netconf tls port
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl -n $namespace get svc $service_name -o jsonpath='{.spec.ports[2].port}' " . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0 && scalar @result == 1) {
        $netconf_tls_port = $result[0];
    }

    # Return information to the user
    return ($service_name, $ip_address, $cli_port, $netconf_port, $netconf_tls_port);
}

# -----------------------------------------------------------------------------
# Return the IP-address of the CM Yang Provider Service for a specific namespace.
#
# Input variables:
#    - namespace
#
# Return values:
#    - The IP address of the CM Yang Provider service or an empty string at error.
#
# -----------------------------------------------------------------------------
sub get_cmyp_ip {
    my $namespace = shift;

    return "" unless $namespace;

    my @ip_addresses =  get_service_ip(
        {
            "check-ssh"         => 1,
            "debug-messages"    => 1,
            "hide-output"       => 1,
            "include-list"      => [ '^eric-cm-yang-provider$', '^eric-cm-yang-provider-ipv4$', '^eric-cm-yang-provider-ipv6$', '^eric-cm-yang-provider-external$' ],
            "ip-type"           => "external",
            "namespace"         => $namespace,
            "port"              => 22,
            "repeat-at-error"   => 0,
            "return-only-one"   => 1,
            "timeout"           => 2,
        }
    );

    if (scalar @ip_addresses > 0) {
        # Always return back first IP-address.
        return $ip_addresses[0];
    } else {
        # No IP-addresses found, return empty string
        return "";
    }
}

# -----------------------------------------------------------------------------
# Return the name of the CM Yang Provider Service for a specific namespace.
#
# Input variables:
#    - namespace
#
# Return values:
#    - The name of the CM Yang Provider service.
#
# -----------------------------------------------------------------------------
sub get_cmyp_service_name {
    my $namespace = shift;
    my @services;

    return "" unless $namespace;

    @services = get_resource_names(
        {
            "hide-output"       => 1,
            "include-list"      => [ '^eric-cm-yang-provider$', '^eric-cm-yang-provider-ipv4$', '^eric-cm-yang-provider-ipv6$', '^eric-cm-yang-provider-external$' ],
            "namespace"         => $namespace,
            "resource"          => "service",
        }
    );
    if (scalar @services == 0) {
        return "";
    } else {
        # Regardless of how many service names are returned we currently only return index 0
        return $services[0];
    }
}

# -----------------------------------------------------------------------------
# Return the command name to use for accessing docker or nerdctl images.
# 'docker' is the old command and 'nerdctl' is the new command *starting from
# ECCD 2.26.
# It will check if the command needs sudo right to execute and if so it will
# also include the 'sudo' command as part of the returned command, for example:
#   - docker
#   - nerdctl
#   - sudo docker
#   - duso nerdctl
#
# It will also use cached information if the check has been done before.
#
# Input variables:
#    - If messages should be hidden (=1) or not (=0) for the user.
#      If the value is 0 which is also the default if not specified, then
#      status messages and warning/error messages are shown to the user.
#      If the value is 1, then status and wraning/error messages are only
#      written to log file (if open).
#
# Return values:
#    - The command to use to access the docker or nerctl images.
#      Or an empty string if none of the commands could be found or are not
#      working.
#
# -----------------------------------------------------------------------------
sub get_docker_or_nerdctl_command {
    my $hide_messages = shift;
    my $rc;
    my @result;

    if ($hide_messages) {
        if ($hide_messages =~ /^(1|yes|true)$/) {
            $hide_messages = 1;
        } else {
            $hide_messages = 0;
        }
    } else {
        $hide_messages = 0;
    }

    # If we have already done the check then return the found command
    return $cached_docker_nerdctl_command if $cached_docker_nerdctl_command ne "";

    for my $command ("docker", "nerdctl") {
        if ($hide_messages == 0) {
            General::Logging::log_user_message("Checking if command '$command' needs 'root' access");
        } else {
            General::Logging::log_write("Checking if command '$command' needs 'root' access");
        }
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command ps --latest",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            $cached_docker_nerdctl_command = $command;
            last;
        } else {
            my $access_denied = 0;
            for (@result) {
                if (/permission denied while trying to connect to the Docker daemon socket at.+/) {
                    # docker command needs sudo rights
                    $access_denied = 1;
                } elsif (/rootless containerd not running\?.+/) {
                    # nerdctl command needs sudo rights
                    $access_denied = 1;
                }
            }
            if ($access_denied == 0) {
                # Display the result in case of error
                #General::OS_Operations::write_last_temporary_output_to_progress();

                if ($hide_messages == 0) {
                    General::Logging::log_user_warning_message("Command '$command' not working");
                } else {
                    General::Logging::log_write("WARNING Message:\nCommand '$command' not working");
                }
                next;
            }

            # Check if docker/nerdctl can be run using sudo without any password prompt
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "sudo -n $command ps --latest",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                $cached_docker_nerdctl_command = "sudo -n $command";
                last;
            } else {
                # Display the result in case of error
                #General::OS_Operations::write_last_temporary_output_to_progress();

                if ($hide_messages == 0) {
                    General::Logging::log_user_warning_message("Command 'sudo -n $command' require password or other error");
                } else {
                    General::Logging::log_write("WARNING Message:\nCommand 'sudo -n $command' require password or other error");
                }
                next;
            }
        }
    }

    if ($cached_docker_nerdctl_command eq "") {
        if ($hide_messages == 0) {
            General::Logging::log_user_error_message("Neither 'docker' nor 'nerdctl' commands are working");
        } else {
            General::Logging::log_write("ERROR Message:\nNeither 'docker' nor 'nerdctl' commands are working");
        }
    }

    return $cached_docker_nerdctl_command;
}

# -----------------------------------------------------------------------------
# Return the ECCD version, if available.
#
# Input variables:
#    -
#
# Return values:
#    - A string with the ECCD version in format x.y.z e.g. 2.28.0 or an
#      empty string if cannoth be found or at error.
#
# -----------------------------------------------------------------------------
sub get_eccd_version {
    my $eccd_version = "";
    my $rc;
    my @result;
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes --no-headers -o custom-columns=NAME:.metadata.labels.ccd/version"  . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_eccd_version: Failed to fetch ECCD Version.\n");
        return "";
    }

    for (@result) {
        if (/^(\d+\.\d+\.\d+)\s*$/) {
            if ($eccd_version eq "") {
                $eccd_version = $1;
            } elsif ($eccd_version ne $1) {
                # We have different versions of ECCD on the different nodes, should not happen
                General::Logging::log_write("ADP::Kubernetes_Operations::get_eccd_version: Found different ECCD versions on the different nodes, '$eccd_version' and '$1'.\n");
            }
        }
    }

    return $eccd_version;
}

# -----------------------------------------------------------------------------
# Return an array of enabled CNF taken from deployed pods in the specified
# namespace.
#
# Input variables:
#    - namespace
#
# Return values:
#    - An array with enabled features and network functions or an empty array
#      if nothing found.
#      Currently known CNF are:
#       - bsf
#       - bsfdiameter
#       - dsc
#       - nlf
#       - objectstorage
#       - pvtb
#       - rlf
#       - scp
#       - sepp
#       - sftp
#       - slf
#       - stmdiameter
#       - wcdb
#
# -----------------------------------------------------------------------------
sub get_enabled_cnf {
    my $namespace = shift;
    my %cnf;
    my @enabled_cnf = ();
    my $rc;
    my @result;
    my @std_error = ();

    return () unless $namespace;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get pod --namespace $namespace  --no-headers -o custom-columns=NAME:.metadata.name"  . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_enabled_cnf: Failed to fetch pod names.\n");
        return ();
    }

    for (@result) {
        if (/^eric-bsf-worker-.+/) {
            $cnf{'bsf'}++;
        } elsif (/^eric-bsf-diameter-.+/) {
            $cnf{'bsfdiameter'}++;
        } elsif (/^eric-dsc-(agent|director|fdr)-.+/) {
            $cnf{'dsc'}++;
        } elsif (/^eric-sc-nlf-.+/) {
            $cnf{'nlf'}++;
        } elsif (/^eric-data-object-storage-.+/) {
            $cnf{'objectstorage'}++;
        } elsif (/^eric-probe-virtual-tap-broker-.+/) {
            $cnf{'pvtb'}++;
        } elsif (/^eric-sc-rlf-.+/) {
            $cnf{'rlf'}++;
        } elsif (/^eric-scp-worker-.+/) {
            $cnf{'scp'}++;
        } elsif (/^eric-sepp-worker-.+/) {
            $cnf{'sepp'}++;
        } elsif (/^eric-data-sftp-server-.+/) {
            $cnf{'sftp'}++;
        } elsif (/^eric-sc-slf-.+/) {
            $cnf{'slf'}++;
        } elsif (/^eric-stm-diameter-.+/) {
            $cnf{'stmdiameter'}++;
        } elsif (/^eric-data-wide-column-database-.+/) {
            $cnf{'wcdb'}++;
        }
    }

    @enabled_cnf = sort keys %cnf;

    if (@enabled_cnf) {
        General::Logging::log_write(sprintf "Enabled Features and Network Functions: %s\n", join ", ", @enabled_cnf);
        return @enabled_cnf;
    } else {
        return ();
    }
}

# -----------------------------------------------------------------------------
# Return an array of enabled CNF which also includes features defined in the
# helm chart 'global' section.
#
# Input variables:
#    - namespace
#    - release name
#
# Return values:
#    - An array with enabled features and network functions or an empty array
#      if nothing found.
#
# -----------------------------------------------------------------------------
sub get_enabled_cnf_from_helm {
    my $namespace = shift;
    my $release = shift;
    my @enabled_cnf = ();
    my $rc;
    my @result;
    my @std_error = ();
    my %yaml_contents;

    return () unless $namespace;
    return () unless $release;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "helm get values --all -o yaml --namespace $namespace $release"  . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_enabled_cnf: Failed to fetch helm values.\n");
        return ();
    }

    # Only return back the "global" section
    @result = ADP::Kubernetes_Operations::get_top_level_section_information("global",\@result);

    General::Logging::log_user_message("Parsing YAML data");
    $rc = General::Yaml_Operations::read_array(
        {
            "input"         => \@result,
            "output-ref"    => \%yaml_contents,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("Failed to parse the YAML data:\n" . join "\n", @result);
        return ();
    }

    # Check which CNF's are currently enabled
    if (exists $yaml_contents{'global'} && exists $yaml_contents{'global'}->{'ericsson'}) {
        my $yaml = $yaml_contents{'global'}->{'ericsson'};
        for my $type (sort keys %$yaml) {
            if (exists $yaml->{$type}->{'enabled'} && $yaml->{$type}->{'enabled'} =~ /^(true|1)$/i) {
                # For example: bsf, ddc, objectStorage, pvtb, rlf, scdiameter, scp, sepp, sftp, slf, spr, wcdb
                push @enabled_cnf, lc($type);
            }
        }
    }
    if (exists $yaml_contents{'global'} && exists $yaml_contents{'global'}->{'security'}) {
        my $yaml = $yaml_contents{'global'}->{'security'};
        for my $type (sort keys %$yaml) {
            if (exists $yaml->{$type}->{'enabled'} && $yaml->{$type}->{'enabled'} =~ /^(true|1)$/i) {
                # For example: tls
                push @enabled_cnf, lc($type);
            }
        }
    }
    if (@enabled_cnf) {
        @enabled_cnf = sort @enabled_cnf;
        General::Logging::log_write(sprintf "Enabled Features and Network Functions: %s\n", join ", ", @enabled_cnf);
        return @enabled_cnf;
    } else {
        return ();
    }
}

# -----------------------------------------------------------------------------
# Return the helm version and executable to use.
#
# Input variables:
#    - Expected helm version
#
# Return values:
#    - The helm version and used executable as a hash reference.
#
# -----------------------------------------------------------------------------
sub get_helm_information {
    my $wanted_helm_version = shift;

    my $helm_directory;
    my $helm_executable = "";
    my $helm2_executable = "";
    my $helm3_executable = "";
    my $helm2_client_version;
    my $helm2_server_version;
    my $helm3_version;

    my %helm_information;

    my $rc = 0;
    my @result = ();
    my @std_error = ();
    my $version_info_fetched = 0;

    # Find helm executable directory
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "which helm",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        for (@result) {
            if (/^\s*(\S*)helm.*/) {
                $helm_directory = $1;
            }
        }
    } else {
        # helm command does not exist in the path
        return undef;
    }

    if ($helm_directory) {
        # Find helm executables
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ls --indicator-style=none -1 --color=never ${helm_directory}",
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );

        if ($rc == 0) {
            for (@result) {
                if (/^helm2$/) {
                    $helm2_executable = "helm2";
                    $helm3_executable = "helm";
                    $helm_information{'helm2_executable'} = $helm2_executable;
                    $helm_information{'helm3_executable'} = $helm3_executable;
                } elsif (/^helm3$/) {
                    $helm2_executable = "helm";
                    $helm3_executable = "helm3";
                    $helm_information{'helm2_executable'} = $helm2_executable;
                    $helm_information{'helm3_executable'} = $helm3_executable;
                } elsif (/^helm$/) {
                    $helm_executable = "helm";
                }
            }
        } else {
            # The command failed
            return undef;
        }
    }

    if ($helm2_executable eq "" && $helm3_executable eq "" && $helm_executable ne "") {
        # We have only one executable and we don't know yet if it is version 2 or 3
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${helm_executable} version" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ( $rc == 0 ) {
            my $helm_main_version = "";
            foreach( @result ) {
                if ( $_ =~ m/.*v([0-9]+)\.[0-9]+\.[0-9]+.*/ ) {
                    $helm_main_version = $1;
                }
            }
            if ($helm_main_version != $wanted_helm_version) {
                # The wanted and found helm version does not match
                return undef;
            } elsif ($wanted_helm_version == 2) {
                $helm2_executable = "helm";
                $helm3_executable = "";
                $helm_information{'helm2_executable'} = $helm2_executable;
                $helm_information{'helm3_executable'} = $helm3_executable;
            } elsif ($wanted_helm_version == 3) {
                $helm2_executable = "";
                $helm3_executable = "helm";
                $helm_information{'helm2_executable'} = $helm2_executable;
                $helm_information{'helm3_executable'} = $helm3_executable;
            } else {
                # Unknown helm version
                return undef;
            }
        } else {
            # The command failed
            return undef;
        }
        # Mark that we have already fetched the version information so we don't fetch it again below
        $version_info_fetched = 1;
    }

    # Find helm version
    if ($wanted_helm_version == 2) {
        if ($version_info_fetched == 0) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${helm2_executable} version" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
                    "hide-output"   => 1,
                    "raw-output"    => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@std_error,
                }
            );
        }

        if ( $rc == 0 ) {
            foreach( @result ) {
                if ( $_ =~ m/^Client:.*/ ) {
                    if ( $_ =~ m/.*v([0-9]+\.[0-9]+\.[0-9]+).*/ ) {
                        $helm2_client_version = $1;
                    }
                } elsif ( $_ =~ m/^Server:.*/ ) {
                    if ( $_ =~ m/.*v([0-9]+\.[0-9]+\.[0-9]+).*/ ) {
                        $helm2_server_version = $1;
                    }
                }
            }
        } else {
            # The command failed
            return undef;
        }
    } elsif ($wanted_helm_version == 3) {
        if ($version_info_fetched == 0) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${helm3_executable} version" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
                    "hide-output"   => 1,
                    "raw-output"    => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@std_error,
                }
            );
        }

        if ( $rc == 0 ) {
            foreach( @result ) {
                if ( $_ =~ m/^version\.BuildInfo.*/ ) {
                    if ( $_ =~ m/.*v([0-9]+\.[0-9]+\.[0-9]+).*/ ) {
                        $helm3_version = $1;
                    }
                }
            }
        } else {
            # The command failed
            return undef;
        }
    }

    if ($wanted_helm_version == 3) {
        $helm_information{'helm_executable'} = $helm3_executable;
        $helm_information{'helm_version'} = $helm3_version;
    } elsif ($wanted_helm_version == 2) {
        $helm_information{'helm_executable'} = $helm2_executable;
        $helm_information{'helm_server_version'} = $helm2_server_version;
        $helm_information{'helm_client_version'} = $helm2_client_version;
    }

    return \%helm_information
}

# -----------------------------------------------------------------------------
# Get extended information about an helm release.
# This can e.g. be the user provided values when deploying a chart.
# The user can specify in what format the output should be returned to the
# caller.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "command":
#           Scalar variable of the command to get data for.
#           This can be any allowed command, for example:
#               - "all"
#               - "hooks"
#               - "manifest"
#               - "notes"
#               - "values"
#               - "values --all"
#           Any specified command is added to the following command which is
#           executed:
#           helm get <command> [--namespace <namespace>] [--revision <revision>]
#           If not specified then the default will be "values --all".
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#       "output-format":
#           Scalar variable that specifies what format the output should have
#           when returned to the caller.
#           The allowed values are:
#               - dot-separated
#                 Will return a dot separated output of the information to make
#                 it easier to parse, e.g:
#                 name1.name2.name3=value
#               - raw
#                 Will return the raw unmodified output.
#                 This is also the default if not specified.
#       "release-name":
#           Scalar variable of the name of the release to check, THIS PARAMETER
#           IS MANDATORY AND MUST BE GIVEN.
#       "revision":
#           Scalar variable of the revision of the release to check, this must
#           be a valid integer value of an existing release.
#           To see existing revisions for a release use the command:
#               helm history <release name> [--namespace <namespace>]
#           If not specified then the current revision will be used.
#       "return-output":
#           Array reference of variable where the result of the check is
#           returned to the caller.
#           If not specified then nothing is returned to the caller and only
#           executed commands is visible in any opened log file or printed to
#           screen depending on setting of the 'hide-output' parameter.
#       "return-patterns":
#           Array reference of variable containing a list of lines to be
#           returned. The array can be any Perl regular expressions or any
#           other text and what is returned must match one or more of the
#           patterns.
#           If not specified then all lines are returned.
#
# Return values:
#    - 0:  Successful.
#    - >0: Some failure occurred.
#
# Examples:
# $rc = ADP::Kubernetes_Operations::get_helm_release_extended_information(
#     {
#         "command"         => "values --all",
#         "hide-output"     => 1,
#         "namespace"       => $namespace,
#         "output-format"   => "dot-separated",
#         "release-name"    => $release_name,
#         "return-output"   => \@result,
#         "return-patterns" => \@filter,
#     }
# );

# -----------------------------------------------------------------------------
sub get_helm_release_extended_information {
    my %params = %{$_[0]};

    # Initialize local variables
    my $command                 = exists $params{"command"} ? $params{"command"} : "values --all";
    my $debug_messages          = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my @errors                  = ();
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $line;
    my $namespace               = exists $params{"namespace"} ? $params{"namespace"} : "";
    my $output_format           = exists $params{"output-format"} ? lc($params{"output-format"}) : "raw";
    my $rc;
    my $release_name            = exists $params{"release-name"} ? $params{"release-name"} : "";
    my @result                  = ();
    my $return_output           = exists $params{"return-output"} ? 1 : 0;
    my $return_patterns         = exists $params{"return-patterns"} ? $params{"return-patterns"} : undef;
    my $revision                = exists $params{"revision"} ? $params{"revision"} : "";
    my %yaml_data;

    # Check input values
    if ($release_name eq "") {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_helm_release_extended_information: A release name must be specified.\n") if $debug_messages;
        return 1;
    }
    if ($output_format !~ /^(dot-separated|raw)$/) {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_helm_release_extended_information: Wrong 'output-format' ($output_format) given, only 'dot-separated' and 'raw' allowed.\n") if $debug_messages;
        return 1;
    }
    if ($revision ne "") {
        unless ($revision =~ /^\d+$/) {
            General::Logging::log_write("ADP::Kubernetes_Operations::get_helm_release_extended_information: Wrong 'revision' ($revision) given, only integer values are allowed.\n") if $debug_messages;
            return 1;
        }
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "helm get $command $release_name" . ($namespace ne "" ? " --namespace $namespace" : "") . ($revision ne "" ? " --revision $revision" : "") . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => $hide_output,
            "return-output" => \@result,
            "return-stderr" => \@errors,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_helm_release_extended_information: Failed to execute the command.\n") if $debug_messages;
        return $rc;
    }

    # Check if we need to reformat the collected data
    if ($output_format eq "dot-separated") {
        # Remove the first line of the output, e.g. "COMPUTED VALUES:" or "USER-SUPPLIED VALUES:"
        shift @result;

        # Parse the yaml data in the @result array and store it in the %yaml_data hash
        if (General::Yaml_Operations::read_array({ "input" => \@result, "output-ref" => \%yaml_data }) != 0) {
            General::Logging::log_write("ADP::Kubernetes_Operations::get_helm_release_extended_information: Problem to parse YAML data.\n") if $debug_messages;
            return 1;
        }

        # Empty the result array in order to replace it with dot separated output
        @result = ();

        General::Data_Structure_Operations::dump_dot_separated_hash_ref_to_variable(\%yaml_data, "", \@result);
    }

    # Check if we need to filter away unwanted lines
    if ($return_patterns) {
        my @temp = @result;
        @result = ();
        for $line (@temp) {
            for my $pattern (@$return_patterns) {
                if ($line =~ /$pattern/) {
                    push @result, $line;
                }
            }
        }
    }

    # Check if we need to return the result to the caller
    if ($return_output) {
        @{$params{"return-output"}} = @result;
    }

    if (scalar @result == 0) {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_helm_release_extended_information: No lines returned.\n") if $debug_messages;
    }

    return 0;
}

# -----------------------------------------------------------------------------
# Return the kubectl version and executable to use.
#
# Input variables:
#    -
#
# Return values:
#    - The kubectl version and used executable as a hash reference.
#
# -----------------------------------------------------------------------------
sub get_kubectl_information {

    my $kubectl_information;
    my $kubectl_path = "";
    my $rc = 0;
    my @result = ();
    my @std_error = ();

    # Find kubectl executable directory
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "which kubectl",
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );

    if ($rc == 0) {
        for (@result) {
            if (/^\S*kubectl\s*$/) {
                $kubectl_path = $_;
            }
        }
    } else {
        # kubectl command does not exist in the path
        return undef;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl version -o json" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );

    # Loop over command results and find kubectl client and server version
    if ( $rc == 0 && @result != 0 ) {
        $kubectl_information = General::Json_Operations::read_array_return_reference({ "input" => \@result });
        unless (defined $kubectl_information) {
            #print "Problem to read array\n" . join "\n", @result;
            return undef;
        }

        unless (ref($kubectl_information) =~ /^HASH$/) {
            #print "Unknown data type detected, only HASH type are supported\n";
            return undef;
        }
        ${$kubectl_information}{'kubectl_executable'} = $kubectl_path;
        if (exists ${$kubectl_information}{'clientVersion'} &&
            exists ${$kubectl_information}{'clientVersion'}{'gitVersion'} &&
            ${$kubectl_information}{'clientVersion'}{'gitVersion'} =~ /.*v([0-9]+\.[0-9]+\.[0-9]+).*/) {

            ${$kubectl_information}{'kubectl_client_version'} = $1;
        }

        if (exists ${$kubectl_information}{'serverVersion'} &&
            exists ${$kubectl_information}{'serverVersion'}{'gitVersion'} &&
            ${$kubectl_information}{'serverVersion'}{'gitVersion'} =~ /.*v([0-9]+\.[0-9]+\.[0-9]+).*/) {

            ${$kubectl_information}{'kubectl_server_version'} = $1;
        }

        return $kubectl_information;
    } else {
        # kubectl version command failed / kubectl not found
        return undef;
    }
}

# -----------------------------------------------------------------------------
# Return a list of known CNF types.
# It will only return the real CNF types like:
#   - bsf
#   - bsfdiameter
#   - csa
#   - scp
#   - sepp
# not NF features like:
#   - nlf
#   - objectStorage
#   - pvtb
#   - rlf
#   - sftp
#   - slf
#   - wcdb
#
#
# Input variables:
#    - Application type (sc or dsc)
#    - Release version (e.g. latest, 1.10.0 etc.)
#
# Return values:
#    - An array with a list of known CNF types.
#
# -----------------------------------------------------------------------------
sub get_list_of_known_cnf_types {
    my $app = @_ ? shift : "sc";
    my $release = @_ ? shift : "latest";

    if (exists $known_cnf_types{$app}) {
        if (exists $known_cnf_types{$app}{$release}) {
            return @{$known_cnf_types{$app}{$release}};
        } else {
            return @{$known_cnf_types{$app}{'latest'}};
        }
    } else {
        if (exists $known_cnf_types{'sc'}{$release}) {
            return @{$known_cnf_types{'sc'}{$release}};
        } else {
            return @{$known_cnf_types{'sc'}{'latest'}};
        }
    }
}

# -----------------------------------------------------------------------------
# Return a list of known SC CRDS.
#
# Input variables:
#    - Application type (sc or dsc)
#    - Release version (e.g. latest, 1.10.0 etc.)
#
# Return values:
#    - An array with a list of known SC CRDs
#
# -----------------------------------------------------------------------------
sub get_list_of_known_sc_crds {
    my $app = @_ ? shift : "sc";
    my $release = @_ ? shift : "latest";

    if (exists $known_sc_crds{$app}) {
        if (exists $known_sc_crds{$app}{$release}) {
            return @{$known_sc_crds{$app}{$release}};
        } else {
            return @{$known_sc_crds{$app}{'latest'}};
        }
    } else {
        if (exists $known_sc_crds{'sc'}{$release}) {
            return @{$known_sc_crds{'sc'}{$release}};
        } else {
            return @{$known_sc_crds{'sc'}{'latest'}};
        }
    }
}

# -----------------------------------------------------------------------------
# Return a list of known SC CRD Helm Charts.
#
# Input variables:
#    - Application type (sc or dsc)
#    - Release version (e.g. latest, 1.10.0 etc.)
#
# Return values:
#    - An array with a list of known SC CRD Helm Charts
#
# -----------------------------------------------------------------------------
sub get_list_of_known_sc_crd_helm_charts {
    my $app = @_ ? shift : "sc";
    my $release = @_ ? shift : "latest";

    if (exists $known_sc_crd_helm_charts{$app}) {
        if (exists $known_sc_crd_helm_charts{$app}{$release}) {
            return @{$known_sc_crd_helm_charts{$app}{$release}};
        } else {
            return @{$known_sc_crd_helm_charts{$app}{'latest'}};
        }
    } else {
        if (exists $known_sc_crd_helm_charts{'sc'}{$release}) {
            return @{$known_sc_crd_helm_charts{'sc'}{$release}};
        } else {
            return @{$known_sc_crd_helm_charts{'sc'}{'latest'}};
        }
    }
}

# -----------------------------------------------------------------------------
# Return an array reference with JSON data explaining about any cluster allocation
# issues found.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#           See also the "all-namespaces" parameter.
#
# Return values:
#    - It returns a reference to an array containing the found data in JSON
#      format.
#      No analysis or check of the data is done, it's left up to the user.
#
#      If a failure is detected then the 'undef' value is returned.
#
# -----------------------------------------------------------------------------
sub get_log_cluster_allocation {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages  = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $namespace       = exists $params{"namespace"} ? $params{"namespace"} : "";

    my $pod_name;
    my $rc;
    my @result = ();
    my @std_error = ();

    General::Logging::log_write("Subroutine ADP::Kubernetes_Operations::get_log_cluster_allocation() called.\n") if $debug_messages;

    if ($namespace eq "") {
        General::Logging::log_write("No namespace given.\n") if $debug_messages;
        return undef;
    }

    # Fetch the needed pod name
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl -n $namespace get pods -l 'app=eric-data-search-engine,role in (ingest-tls,ingest)'  -o jsonpath='{.items[0].metadata.name}'" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0) {
        $pod_name = $result[0];
    } else {
        General::Logging::log_write("Failed to fetch the pod name.\n") if $debug_messages;
        return undef;
    }

    # Fetch the wanted data
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl -n $namespace exec -c ingest $pod_name " . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : "") . " -- /bin/esRest GET '/_cluster/allocation/explain?pretty'",
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("Failed to fetch the cluster allocation data.\n") if $debug_messages;
        return undef;
    }

    return \@result;
}

# -----------------------------------------------------------------------------
# Return a count of how many log entries has been seen in the specified time
# period or within the last hour if not specified.
#
# For details how the elastic search engine works see:
# https://www.elastic.co/guide/en/elasticsearch/reference/current/
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#
#       "date":
#           Specifies which date to check.
#           Can be e.g.:
#               - "date" => "today"
#               - "date" => "yesterday"
#               - "date" => "2022.10.13"
#
#       "force-hash-result":
#           If specified and value is anything but "0" then the function will
#           always return the value(s) as a hash reference even if only one
#           log is specified.
#           If not specified or the value is "0" then the result depends on
#           how many counters are found as described below under "Return Values".
#
#       "log":
#           Specifies which log to check.
#           For example:
#               - all
#               - adp-app-logs
#               - sc-rlf-logs
#               - sc-scp-logs
#               - adp-app-asi-logs
#               - adp-app-audit-logs
#               - sc-logs
#               - (sc-scp-logs|sc-logs)
#               - etc...
#
#       "log-details-directory":
#           If specified and the count of log entries is >0 then the log details
#           will be written to this directory.
#           A log file for each log plane is created in this directory with the
#           file name being:
#
#               <log plane name>.json
#
#           Note that there is a maximum size of log entry details that can be
#           fetched, which by default is 10000 log entries.
#
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#           See also the "all-namespaces" parameter.
#
#       "severity":
#           The severity level to check, if more than one is specified then
#           they should be space separated.
#           If not specified then all severity levels are checked.
#           For example:
#               - "severity" => "critical"
#               - "severity" => "error"
#               - "severity" => "warning"
#               - "severity" => "info"
#               - "severity" => "debug"
#               - "severity" => "critical error warning"
#
#       "time-period":
#           The time period for how far back to check, can be specified as
#           an integer meaning how many seconds back in time from current time
#           to check. It can also be specified in minutes or hours or as a time
#           interval,
#           If not specified then a count of available entries are returned.
#           For example:
#               - 10 or 10s
#                 For 10 seconds back.
#               - 10m
#                 For 10 minutes back.
#               - 2h
#                 For 2 hours back.
#               - 2d
#                 For 2 days back.
#
# Return values:
#    - If only one log is specified (e.g. "log" => "sc-scp-logs") and the log
#      was found then it return back just an integer value of how many log
#      entries were found matching the conditions ("date", "time-period" and
#      "severity").
#      If a regular expression is specified or if all logs are wanted then it
#      returns a hash reference with the data that looks like this:
#      Key 1: The index name e.g. "sc-rlf-logs-2022.10.05"
#      Key 2: All of the following:
#             "doc.count": Counter of how many entries in total are in the index.
#             "count": Counter of how many matches the specified conditions.
#
#      If a failure is detected then the 'undef' value is returned.
#
# -----------------------------------------------------------------------------
sub get_log_count {
    my %params = %{$_[0]};

    # Initialize local variables
    my $debug_messages  = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $date            = exists $params{"date"} ? $params{"date"} : "today";
    my $hash_result     = exists $params{"force-hash-result"} ? $params{"force-hash-result"} : 0;
    my $log             = exists $params{"log"} ? $params{"log"} : "all";
    my $log_directory   = exists $params{"log-details-directory"} ? $params{"log-details-directory"} : "";
    my $namespace       = exists $params{"namespace"} ? $params{"namespace"} : "";
    my $severity        = exists $params{"severity"} ? $params{"severity"} : "";
    my $time_period     = exists $params{"time-period"} ? $params{"time-period"} : "";

    my $command;
    my $index;
    my $index_file_info;
        # Hash reference with the following keys:
        # Key1: Index name e.g. 'sc-logs-2022.10.14'
        # Key2: Each of the following:
        #           - 'doc.count'
        #             Total number of log entries in the index file.
        #           - 'count'
        #             Number of log entries that match the conditions.
        #           - 'average'
        #             The average (count/seconds) number of log entries that match per second.
    my $index_files;
    my $log_plane_info;
        # Hash reference with the following keys:
        # Key1: Log plane name e.g. 'sc-logs'
        # Key2: Each of the following
        #           - 'doc.count'
        #             Total number of log entries in all used index files.
        #           - 'count'
        #             Number of log entries that match the conditions.
        #           - 'average'
        #             The average (count/seconds) number of log entries that match per second.
        #           - 'index-files'
        #             A comma separated list of index files to use for the log plane.
        #             E.g. "sc-logs-2022.10.14,sc-logs-2022.10.15"
        #           - 'time-period'
        #             An integer value indicating how many seconds are in the used time interval.
    my $log_plane;
    my $pod_name;
    my $query_part1;
    my $query_part2;
    my $rc;
    my @result = ();
    my $return_hash = 0;
    my @std_error = ();
    my $time_in_seconds = 0;

    General::Logging::log_write("Subroutine ADP::Kubernetes_Operations::get_log_count() called.\n") if $debug_messages;

    if ($namespace eq "") {
        General::Logging::log_write("No namespace given.\n") if $debug_messages;
        return undef;
    }

    if (lc($log) eq "all" || $log eq "") {
        # Mark that the "log" might match multiple files so we need to return back a hash instead of an integer value
        $return_hash = 1;
        $log = '\S+';
        General::Logging::log_write("All logs wanted, will return a hash.\n") if $debug_messages;
    } elsif ($log !~ /[-_a-zA-Z0-9]/) {
        # If it match anything other that a to z, A to Z, a - or a _, then it's probably a regular expression and we nee to return a hash
        $return_hash = 1;
        General::Logging::log_write("Perl regular expression of logs wanted, will return a hash.\n") if $debug_messages;
    } elsif ($hash_result) {
        # The user specifically want the result to be a hash
        $return_hash = 1;
        General::Logging::log_write("User specifically asks for a hash result.\n") if $debug_messages;
    } else {
        General::Logging::log_write("Only one log wanted, will return a integer value.\n") if $debug_messages;
    }

    # Convert the date format to YYYY.MM.DD if needed
    if ($date eq "") {
        $date = "today";
    }
    if ($date !~ /^\d{4}\.\d{2}\.\d{2}$/) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "date -d '$date' +\%Y.\%m.\%d",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            $date = $result[0];
        } else {
            General::Logging::log_write("Failed to convert the date into YYYY.MM.DD format.\n") if $debug_messages;
            return undef;
        }
    }

    if ($time_period =~ /^\d+$/) {
        # We need to add the "s" to the end to signify seconds.
        $time_in_seconds = $time_period;
        $time_period = sprintf '"gte": "now-%ds"', $time_period;
    } elsif ($time_period =~ /^(\d+)s$/) {
        # Number of seconds
        $time_in_seconds = $1;
        $time_period = sprintf '"gte": "now-%s"', $time_period;
    } elsif ($time_period =~ /^(\d+)m$/) {
        # Number of minutes
        $time_in_seconds = $1 * 60;
        $time_period = sprintf '"gte": "now-%s"', $time_period;
    } elsif ($time_period =~ /^(\d+)h$/) {
        # Number of hours
        $time_in_seconds = $1 * 3600;
        $time_period = sprintf '"gte": "now-%s"', $time_period;
    } elsif ($time_period =~ /^(\d+)d$/) {
        # Number of days
        $time_in_seconds = $1 * 3600 * 24;
        $time_period = sprintf '"gte": "now-%s"', $time_period;
    } elsif ($time_period =~ /^(.+) to (.+)$/) {
        # A time interval is given, first find the epoch time (seconds since Jan 1 1970)
        my $time1 = $1;
        my $time2 = $2;
        if ($time1 !~ /^\d+$/) {
            # The time is not in epoch time, so convert it to epoch time
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "date -d '$time1' +\%s",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@std_error,
                }
            );
            if ($rc == 0) {
                $time1 = $result[0];
            } else {
                General::Logging::log_write("Failed to convert the date '$time1' into epoch time format.\n") if $debug_messages;
                return undef;
            }
        }
        if ($time2 !~ /^\d+$/) {
            # The time is not in epoch time, so convert it to epoch time
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "date -d '$time2' +\%s",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@std_error,
                }
            );
            if ($rc == 0) {
                $time2 = $result[0];
            } else {
                General::Logging::log_write("Failed to convert the date '$time2' into epoch time format.\n") if $debug_messages;
                return undef;
            }
        }

        # Check if the dates are turned around
        if ($time_period ne "$time1 to $time2") {
            if ($time1 < $time2) {
                General::Logging::log_write("'time-period' changed from '$time_period' to '$time1 to $time2'.\n") if $debug_messages;
                $time_period = "$time1 to $time2";
                $time_in_seconds = $time2 - $time1;
            } else {
                General::Logging::log_write("'time-period' changed from '$time_period' to '$time2 to $time1'.\n") if $debug_messages;
                $time_period = "$time2 to $time1";
                $time_in_seconds = $time1 - $time2;
            }
        } else {
            if ($time1 < $time2) {
                $time_in_seconds = $time2 - $time1;
            } else {
                General::Logging::log_write("'time-period' changed from '$time_period' to '$time2 to $time1'.\n") if $debug_messages;
                $time_period = "$time2 to $time1";
                $time_in_seconds = $time1 - $time2;
            }
        }

        # Next we need to find the date format in 'YYYY.MM.DD' format required for the log index names
        my $date1;
        my $date2;
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "date -d '\@$time1' +\%Y.\%m.\%d",
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc == 0) {
            $date1 = $result[0];
        } else {
            General::Logging::log_write("Failed to convert the epoch time $time1 into YYYY.MM.DD format.\n") if $debug_messages;
            return undef;
        }
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "date -d '\@$time2' +\%Y.\%m.\%d",
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc == 0) {
            $date2 = $result[0];
        } else {
            General::Logging::log_write("Failed to convert the epoch time $time2 into YYYY.MM.DD format.\n") if $debug_messages;
            return undef;
        }

        # Now we need to check if the time interval spans multiple days in which case we need to find all dates in between.
        if ($date1 eq $date2) {
            # Same date
            $date = $date1;
        } else {
            # Not the same date
            my $year1;
            my $month1;
            my $day1;
            my $year2;
            my $month2;
            my $day2;
            if ($date1 =~ /^(\d{4})\.(\d{2})\.(\d{2})$/) {
                $year1  = $1;
                $month1 = $2;
                $day1   = $3;
            }
            if ($date2 =~ /^(\d{4})\.(\d{2})\.(\d{2})$/) {
                $year2  = $1;
                $month2 = $2;
                $day2   = $3;
            }

            if ($year1 == $year2 && $month1 == $month2) {
                # Same month, now find all days between the start and end dates
                $date = "";
                for (my $day = $day1; $day <= $day2; $day++) {
                    $date .= sprintf "%4d.%2d.%2d|", $year1, $month1, $day;
                }
            } elsif ($year1 == $year2 && $month1+1 == $month2) {
                # Previous and current month, now find all days between the start and end dates
                $date = "";
                # First previous month
                for (my $day = $day1; $day <= 31; $day++) {
                    if ($month1 == 2 && $day > 29) {
                        # February has max 29 day, always include the 29 just in case even if not a leap year
                        last;
                    } elsif (($month1 == 4 || $month1 == 6 || $month1 == 9 || $month1 == 11) && $day > 30) {
                        # April, June, September and November has max 30 days
                        last;
                    }
                    $date .= sprintf "%4d.%2d.%2d|", $year1, $month1, $day;
                }
                # Next Current month
                for (my $day = $day2; $day <= 31; $day++) {
                    if ($month2 == 2 && $day > 29) {
                        # February has max 29 day, always include the 29 just in case even if not a leap year
                        last;
                    } elsif (($month2 == 4 || $month2 == 6 || $month2 == 9 || $month2 == 11) && $day > 30) {
                        # April, June, September and November has max 30 days
                        last;
                    }
                    $date .= sprintf "%4d.%2d.%2d|", $year2, $month2, $day;
                }
            } elsif ($year1+1 == $year2 && $month1 == 12 && $month2 == 1) {
                # December and January, now find all days between the start and end dates
                $date = "";
                # First December
                for (my $day = $day1; $day <= 31; $day++) {
                    $date .= sprintf "%4d.%2d.%2d|", $year1, $month1, $day;
                }
                # Next January
                for (my $day = $day2; $day <= 31; $day++) {
                    $date .= sprintf "%4d.%2d.%2d|", $year2, $month2, $day;
                }
            } else {
                # Either more than 1 year between the dates or more than 1 month between the dates.
                # This is unlikely to happen, so we just include all dates regardless if they should be included or not.
                $date = '\d{4}.\d{2}.\d{2}';
            }
            $date =~ s/\|$//;
            $date = "($date)";
        }
        General::Logging::log_write("Changing date to use $date.\n") if $debug_messages;

        # Convert the epoch time format to a valid YYYY-MM-DDTHH:MM:SS format
        $time_period = '"gte": ';
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "date -d '\@$time1' +\%Y-\%m-\%dT\%H:\%M:\%S",
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc == 0) {
            $time_period .= sprintf '"%s"', $result[0];
        } else {
            General::Logging::log_write("Failed to convert the epoch time $time1 into YYYY-MM-DDTTHH:MM:SS format.\n") if $debug_messages;
            return undef;
        }
        $time_period .= ', "lte": ';
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "date -d '\@$time2' +\%Y-\%m-\%dT\%H:\%M:\%S",
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc == 0) {
            $time_period .= sprintf '"%s"', $result[0];
        } else {
            General::Logging::log_write("Failed to convert the epoch time $time2 into YYYY-MM-DDTTHH:MM:SS format.\n") if $debug_messages;
            return undef;
        }
        General::Logging::log_write("Changing time-period to use $time_period.\n") if $debug_messages;
    } else {
        General::Logging::log_write("Unknown 'time-period' format given $time_period.\n") if $debug_messages;
        return undef;
    }

    # Fetch the needed pod name
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl -n $namespace get pods -l 'app=eric-data-search-engine,role in (ingest-tls,ingest)'  -o jsonpath='{.items[0].metadata.name}'" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0) {
        $pod_name = $result[0];
    } else {
        General::Logging::log_write("Failed to fetch the pod name.\n") if $debug_messages;
        return undef;
    }

    # Fetch the index files
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl -n $namespace exec -c ingest $pod_name " . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : "") . " -- /bin/esRest GET '/_cat/indices?v'",
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("Failed to fetch the indices.\n") if $debug_messages;
        return undef;
    }
    for (@result) {
        #health status index                         uuid                   pri rep docs.count docs.deleted store.size pri.store.size
        #green  open   adp-app-logs-2022.10.06       RkBFekWTQ16htqevvK9qcw   5   1     615567            0      349mb        175.9mb
        #green  open   sc-rlf-logs-2022.10.05        Gt9LTC18RrK5V_LUUIdGWQ   5   1          6            0    155.5kb         77.7kb
        # First check the index column
        if (/^\S+\s+\S+\s+($log-$date)\s+\S+\s+\d+\s+\d+\s+\d+\s+\d+\s+\S+\s+\S+\s*$/) {
            $index = $1;
            # Now extract the log plane
            if (/^\S+\s+\S+\s+(\S+)-\d{4}\.\d{2}\.\d{2}\s+\S+\s+\d+\s+\d+\s+\d+\s+\d+\s+\S+\s+\S+\s*$/) {
                $log_plane = $1;    # E.g. sc-rlf-logs
            }
            # Now extract the doc.count column
            if (/^\S+\s+\S+\s+\S+\s+\S+\s+\d+\s+\d+\s+(\d+)\s+\d+\s+\S+\s+\S+\s*$/) {
                $index_file_info->{$index}{'doc.count'} = $1;
                $log_plane_info->{$log_plane}{'doc.count'} += $1;
                $log_plane_info->{$log_plane}{'time-period'} = $time_in_seconds;
                if ($time_period eq "" && $severity eq "") {
                    # We only need to know how many logs are in the current log file
                    $index_file_info->{$index}{'count'} = $1;
                    $index_file_info->{$index}{'average'} = 0;
                    $index_file_info->{$index}{'log-plane'} = $log_plane;

                    $log_plane_info->{$log_plane}{'count'} += $1;
                    $log_plane_info->{$log_plane}{'average'} = 0;
                    $log_plane_info->{$log_plane}{'index-files'} .= "$index,";
                } else {
                    # We need to count the number of logs match the conditions, initialize the value to 0
                    $index_file_info->{$index}{'count'} = 0;
                    $index_file_info->{$index}{'average'} = 0;
                    $index_file_info->{$index}{'log-plane'} = $log_plane;

                    $log_plane_info->{$log_plane}{'count'} = 0;
                    $log_plane_info->{$log_plane}{'average'} = 0;
                    $log_plane_info->{$log_plane}{'index-files'} .= "$index,";
                }
            }
        }
    }

    # Check if we need to fetch any counters or if we already know everything
    if ($time_period eq "" && $severity eq "") {
        # No time period or severity checking needed, we already have all information we need.
        # Now we just need to find out if we need to return back a hash or just an integer.
        if ($return_hash == 1) {
            return $index_file_info;
        } else {
            # Just return in integer value
            for my $index (%$index_file_info) {
                return $index_file_info->{$index}{'count'};
            }
            # No index found
            return undef;
        }
    }

    # Create detailed log directory if wanted
    if ($log_directory ne "") {
        unless (-d "$log_directory") {
            # Directory does not exists, create it
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "mkdir -p '$log_directory'",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                General::Logging::log_write("Unable to create the directory for detailed logs, no logs will be ftehced.\n") if $debug_messages;
                $log_directory = "";
            }
        }
    }

    if ($log_plane_info) {
        $command = "kubectl -n $namespace exec -c ingest $pod_name " . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : "") . " -- /bin/esRest GET";
        for $log_plane (sort keys %$log_plane_info) {
            $log_plane_info->{$log_plane}{'index-files'} =~ s/,$//;
            $index_files = $log_plane_info->{$log_plane}{'index-files'};

            $query_part1 = "'/$index_files/_count?human=false&filter_path=-_shards'";
            if ($severity ne "" && $time_period ne "") {
                $query_part2 = "-H 'Content-Type: application/json' -d'{\"query\": {\"bool\": { \"must\": [{\"match\": {\"severity\": {\"query\": \"$severity\"}}},{\"range\": {\"timestamp\": {$time_period}}}]}}}'";
            } elsif ($severity ne "") {
                $query_part2 = "-H 'Content-Type: application/json' -d'{\"query\": {\"bool\": { \"must\": [{\"match\": {\"severity\": {\"query\": \"$severity\"}}}]}}}'";
            } elsif ($time_period ne "") {
                $query_part2 = "-H 'Content-Type: application/json' -d'{\"query\": {\"range\": {\"timestamp\": {$time_period}}}}'";
            } else {
                $query_part2 = "";
            }

            # Fetch the index files
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command $query_part1 $query_part2",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@std_error,
                }
            );
            if ($rc == 0) {
                if ($result[0] =~ /"count"\s*:\s*(\d+)/) {
                    $log_plane_info->{$log_plane}{'count'} = $1;
                    $log_plane_info->{$log_plane}{'average'} = $1 / $time_in_seconds;
                    if ($log_directory ne "" && $1 > 0) {
                        # We now fetch detailed log information and write the file to the specific directory
                        my $filename = "$log_directory/$log_plane";
                        if ($severity ne "") {
                            $filename .= "_$severity.json";
                        } else {
                            $filename .= ".json";
                        }
                        $query_part1 = "'/$index_files/_search?filter_path=-_shards&pretty&size=10000'";
                        # Ignore any failure in sending the command
                        General::OS_Operations::send_command(
                            {
                                "command"       => "$command $query_part1 $query_part2",
                                "hide-output"   => 1,
                                "save-to-file"  => $filename,
                            }
                        );
                    }
                } else {
                    General::Logging::log_write("Failed to find the \"count\" value in the output for log plane=$log_plane.\n") if $debug_messages;
                }
            } else {
                General::Logging::log_write("Failed to fetch data for log plane=$log_plane.\n") if $debug_messages;
            }
        }

        # Now we just need to find out if we need to return back a hash or just an integer.
        if ($return_hash == 1) {
            return $log_plane_info;
        } elsif (scalar keys %$log_plane_info == 1) {
            # Just return in integer value
            for my $index (%$log_plane_info) {
                return $log_plane_info->{$log_plane}{'count'};
            }
            # No index found
            return undef;
        } else {
            General::Logging::log_write("Multiple log indices found when only expecting one.\n") if $debug_messages;
            return undef;
        }
    } else {
        General::Logging::log_write("No logs matching the conditions found.\n") if $debug_messages;
        return undef;
    }
}

# -----------------------------------------------------------------------------
# Return a list of defined master nodes names supported by kuberentes.
# A master node is any node that does have the role "master".
#
# Input variables:
#    - The kubeconfig file to use, if any (optional)
#
# Return values:
#    - An array with a list of master nodes
#
# -----------------------------------------------------------------------------
sub get_master_nodes {
    my $kubeconfig = @_ ? shift : "";
    if ($kubeconfig eq "" && $kubeconfig_file ne "") {
        $kubeconfig = $kubeconfig_file;
    }

    my $rc;
    my @result = ();
    my $role = get_node_role("master", $kubeconfig);
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes --no-headers -o custom-columns=NAME:.metadata.name --selector='node-role.kubernetes.io/$role'" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    @result = () if ($rc != 0);

    return @result;
}

# -----------------------------------------------------------------------------
# Return the default value to be used or checked for the max_connections
# parameter in the eric-data-document-database-pg-0/1 pods.
#
# Input variables:
#    - Appliation type: default, dsc, sc
#    - Release version: E.g. default, 1.8.0, 1.10.0 etc.
#
# Return values:
#    - An integer value to be used for setting or checking the max_connections
#      value.
#
# -----------------------------------------------------------------------------
sub get_max_connections_default_value {
    my $application = shift;
    my $release_version = shift;

    my %value_matrix;
        # Key1: Application
        # Key2: Release version
        $value_matrix{'default'}{'default'} = 350;
        # dsc
        $value_matrix{'dsc'}{'default'} = 100;
        $value_matrix{'dsc'}{'1.9.0'} = 100;
        $value_matrix{'dsc'}{'1.9.25'} = 100;   # Pre-release version
        # sc
        $value_matrix{'sc'}{'default'} = $value_matrix{'default'}{'default'};
        $value_matrix{'sc'}{'1.2.2'} = 300;
        $value_matrix{'sc'}{'1.2.3'} = 300;
        $value_matrix{'sc'}{'1.3.0'} = 300;
        $value_matrix{'sc'}{'1.3.1'} = 300;
        $value_matrix{'sc'}{'1.3.2'} = 300;
        $value_matrix{'sc'}{'1.3.3'} = 300;
        $value_matrix{'sc'}{'1.3.4'} = 300;
        $value_matrix{'sc'}{'1.4.0'} = 300;
        $value_matrix{'sc'}{'1.4.1'} = 300;
        $value_matrix{'sc'}{'1.5.0'} = 300;
        $value_matrix{'sc'}{'1.5.1'} = 300;
        $value_matrix{'sc'}{'1.6.0'} = 300;
        $value_matrix{'sc'}{'1.6.1'} = 300;
        $value_matrix{'sc'}{'1.7.0'} = 300;
        $value_matrix{'sc'}{'1.8.0'} = 300;
        $value_matrix{'sc'}{'1.8.1'} = 300;
        $value_matrix{'sc'}{'1.10.0'} = 350;
        $value_matrix{'sc'}{'1.10.1'} = 350;
        $value_matrix{'sc'}{'1.10.2'} = 350;
        $value_matrix{'sc'}{'1.11.25'} = 350;   # Pre-release version

    $application = "default" unless defined $application;
    $release_version = "default" unless defined $release_version;

    if (exists $value_matrix{$application} && exists $value_matrix{$application}{$release_version}) {
        return $value_matrix{$application}{$release_version};
    } else {
        return $value_matrix{'default'}{'default'};
    }
}

# -----------------------------------------------------------------------------
# Get all information from a printout starting with the "Name:" section matching
# the specified name and return all data up until the next "Name:" section or
# until the end of file. Empty lines are not returned and only the first
# matching name section is returned.
# For example if the printout looks like this:
# Name:               master-2-n103-dsc5429-ipv6
# Roles:              control-plane,master
# Labels:             beta.kubernetes.io/arch=amd64
#                     beta.kubernetes.io/instance-type=m1.large.16g
# : etc.
# Events:              <none>
#
#
# Name:               worker-pool1-7kj2fqpv-n103-dsc5429-ipv6
# Roles:              worker
# Labels:             beta.kubernetes.io/arch=amd64
#                     beta.kubernetes.io/instance-type=ecmeoc4391-pool1
# : etc.
#
# If the user specifies name as "master-2-n103-dsc5429-ipv6" then the following
# information is returned to the caller:
# Name:               master-2-n103-dsc5429-ipv6
# Roles:              control-plane,master
# Labels:             beta.kubernetes.io/arch=amd64
#                     beta.kubernetes.io/instance-type=m1.large.16g
# : etc.
# Events:              <none>
#
# Input variables:
#    - Name of the wanted section
#    - A reference to an array containing the data to check.
#
# Return values:
#    - An array with the found information, or an empty array if not found.
#      The returned data will also contain the name string.
#
# -----------------------------------------------------------------------------
sub get_name_section_information {
    my $name = shift;
    my $array_ref = shift;

    my $reading = 0;
    my @result = ();

    return @result unless $name;
    return @result unless $array_ref;

    $name =~ s/^\s*//;
    $name =~ s/\s*$//;

    for (@$array_ref) {
        next if /^\s*$/;    # Ignore empty lines
        if (/^Name:\s*$name/) {
            $reading = 1;
            push @result, $_;
            next;
        }
        if ($reading == 1) {
            if (/^Name:\s*\S+/) {
                $reading = 0;
                # We only return data for the first matching section, so exit now.
                last;
            } else {
                push @result, $_;
                next;
            }
        }
    }

    return @result;
}

# -----------------------------------------------------------------------------
# Return the actual node role for a string.
# This can be used to e.g. translate the role "master" into the actual role name
# in case the label has not been set but the node instead e.g. have the role
# "control-plane", or to translate the role "worker" into the actual role name
# when the role is called "node".
#
# Input variables:
#    - The potential node role name, e.g. "control-plane", "master", "node" and "worker".
#    - The kubeconfig file to use, if any (optional)
#
# Return values:
#    - The actual node role name, e.g. "control-plane", "master", "node" and "worker".
#      For example input might be "master" but output might be "control-plane", or
#      input might be "worker" but output might be "node".
#
# -----------------------------------------------------------------------------
sub get_node_role {
    my $name = shift;       # Mandatory
    my $kubeconfig = shift; # Optional

    my $rc;
    my @result = ();
    my @roles = ();
    my @std_error = ();

    $kubeconfig = "-" unless ($kubeconfig);
    if ($kubeconfig eq "-" && $kubeconfig_file ne "") {
        $kubeconfig = $kubeconfig_file;
    }

    unless (exists $node_roles{$kubeconfig}) {
        # Need to find the available node roles.
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl describe nodes " . ($kubeconfig ne "-" ? " --kubeconfig=$kubeconfig" : ""),
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc != 0) {
            # Something went wrong, just return back the input value
            General::Logging::log_write("Something went wrong trying to fetch node roles, we just return back the input value '$name'\n");
            return $name;
        }
        for (@result) {
            # For example:
            #Roles:              edge,node
            #Roles:              node
            if (/^Roles:\s+(\S+)/) {
                push @roles, split ",", $1;
            }
        }
        # Now update the hash with found roles
        %{$node_roles{$kubeconfig}} = map { $_ => 1 } @roles;
    }

    if (exists $node_roles{$kubeconfig} && exists $node_roles{$kubeconfig}{$name}) {
        # The node role is correct and exists, so just return it.
        return $name;
    } else {
        # The actual node role does not exist, now try to convert it into another role that is used for the same thing.
        if ($name eq "master" && exists $node_roles{$kubeconfig} && exists $node_roles{$kubeconfig}{'control-plane'}) {
            return "control-plane";
        } elsif ($name eq "worker" && exists $node_roles{$kubeconfig} && exists $node_roles{$kubeconfig}{'node'}) {
            return "node";
        } else {
            # Unknow node role give, just return back the input value
            return $name;
        }
    }
}

# -----------------------------------------------------------------------------
# Return a list of defined nodes names supported by kuberentes.
#
# Input variables:
#    -
#
# Return values:
#    - An array with a list of nodes
#
# -----------------------------------------------------------------------------
sub get_nodes {
    my $rc;
    my @result = ();
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes --no-headers -o custom-columns=NAME:.metadata.name" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    @result = () if ($rc != 0);

    return @result;
}

# -----------------------------------------------------------------------------
# Return a list of defined nodes names supported by kuberentes that matches a
# specific label.
#
# Input variables:
#    - Label(s) to match, if more than one then separate them with a ",".
#      For example: usage=tools
#                   usage=tools,node-index=3
#
# Return values:
#    - An array with a list of nodes that match the label, or an empty list if
#      no node had the label or at error fetching the nodes.
#
# -----------------------------------------------------------------------------
sub get_nodes_with_label {
    my $label = shift;
    my $rc;
    my @result = ();
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes -l $label -o custom-columns=NAME:.metadata.name --no-headers" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    @result = () if ($rc != 0);

    return @result;
}

# -----------------------------------------------------------------------------
# Return a list of defined nodes names supported by kuberentes that matches a
# specific taints.
#
# Input variables:
#    - Taints to match.
#      For example: usage=tools:NoSchedule
#
# Return values:
#    - An array with a list of nodes that match the taintsl, or an empty list if
#      no node had the taints or at error fetching the nodes.
#
# -----------------------------------------------------------------------------
sub get_nodes_with_taints {
    my $taints = shift;
    my $rc;
    my @result = ();
    my $node;
    my @nodes = ();
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl describe nodes" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    @result = () if ($rc != 0);

    for (@result) {
        if (/^Name:\s+(\S+)/) {
            $node = $1;
        } elsif (/^Taints:\s+$taints/) {
            push @nodes, $node;
        } elsif (/^\s+$taints/) {
            push @nodes, $node;
        }
    }

    return @nodes;
}

# -----------------------------------------------------------------------------
# Return information about one or more nodes.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "index":
#           The index of the node (zero counting), if not specified and also
#           "name" is not specified then information about all found nodes are
#           returned.
#           Should not be combined with "name".
#       "kubeconfig":
#           Scalar variable pointing to the config file to use if other than
#           the default $kubeconfig_file.
#       "name":
#           The exact name of the node, if not specified and also "index"
#           is not specified then information about all found nodes are returned.
#           Should not be combined with "index" or "type".
#       "return-filter":
#           Only return top level sections matching this filter which is a Perl
#           regular expression.
#           For example: 'return-filter' => '^(Allocatable|Allocated resources|Capacity)$'
#       "type":
#           The type of node which can be one of:
#               - master
#               - worker
#           If not specified then all types will be checked.
#
# Return values:
#    - A hash of hash of array with all information is returned to the caller, or at
#      error an undefined (undef) is returned.
#      $node_info{$name}{$section} = ();
#      For example:
#      $node_info{'master-0-eccd-pikachu-ipv6'}{'Roles'}[0] = 'control-plane,master'
#      $node_info{'master-0-eccd-pikachu-ipv6'}{'Addresses'}[0] = 'InternalIP:  fd08::32c'
#      $node_info{'master-0-eccd-pikachu-ipv6'}{'Addresses'}[1] = 'Hostname:    master-0-eccd-pikachu-ipv6'
#
# -----------------------------------------------------------------------------
sub get_node_information {
    my %params = %{$_[0]};

    # Initialize local variables
    my $index     = exists $params{"index"} ? $params{"index"} : "";
    my $kubeconfig  = exists $params{"kubeconfig"} ? $params{"kubeconfig"} : $kubeconfig_file;
    my $name      = exists $params{"name"} ? $params{"name"} : "";
    my $filter    = exists $params{"return-filter"} ? $params{"return-filter"} : "";
    my $type      = exists $params{"type"} ? $params{"type"} : "";

    my $command = "kubectl get nodes --no-headers=true -o custom-columns=NAME:.metadata.name" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : "");
    my %node_info;
    my @node_names = ();
    my $rc;
    my @result = ();
    my $role = get_node_role("master", $kubeconfig);
    my @std_error = ();
    my @wanted_lines = ();

    # Check validity of parameters
    if ($index ne "" && $name ne "") {
        # Not valid combination, return empty scalar
        return;
    }
    if ($index ne "" && $index !~ /^\d+$/) {
        # The index must be a number, return empty scalar
        return;
    }
    if ($type eq "master") {
        $command .= " --selector='node-role.kubernetes.io/$role'";
    } elsif ($type eq "worker") {
        $command .= " --selector='!node-role.kubernetes.io/$role'";
    } elsif ($type ne "") {
        # Not supported type
        return;
    }

    if ($name ne "") {
        push @node_names, $name;
    } else {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc != 0) {
            # Something went wrong with the command
            return undef;
        }
        if ($index ne "") {
            if ($index > $#result) {
                # Too high index
                return undef;
            } else {
                push @node_names, $result[$index];
            }
        } else {
            # Use all found node names
            @node_names = @result;
        }
    }

    for $name (@node_names) {
        $command = "kubectl describe node $name" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : "");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc != 0) {
            # Something went wrong with the command
            return;
        }

        my $data;
        my $reading = 0;
        my $section;
        for (@result) {
            if ($reading == 1) {
                if (/^\S[^:]*:/) {
                    # Nex top level section, just redo that line
                    $reading = 0;
                    redo;
                } elsif (/^\s*(.+)/) {
                    # Remove any space at the beginning of the data
                    push @{$node_info{$name}{$section}}, $1;
                }
            } elsif (/^(\S[^:]*):\s*(.*)/) {
                $section = $1;
                $data = $2;
                if ($filter ne "" && $section !~ /$filter/) {
                    # The section name does not match the filter, ignore this section
                    next;
                }
                $reading = 1;
                if ($data) {
                    # Some extra information on the same line, push it to the array
                    push @{$node_info{$name}{$section}}, $data;
                }
            }
        }
    }

    return %node_info;
}

# -----------------------------------------------------------------------------
# Return a the internal IP address of one or more nodes.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "index":
#           The index of the node, if not specified and also "name" is
#           not specified then all found IP-addresses are returned.
#           Should not be combined with "name".
#       "kubeconfig":
#           Scalar variable pointing to the config file to use if other than
#           the default $kubeconfig_file.
#       "name":
#           The exact name of the node, if not specified and also "index"
#           is not specified then all found IP-addresses are returned.
#           Should not be combined with "index".
#       "type":
#           The type of node which can be one of:
#               - master
#               - worker
#           If not specified then all types will be checked.
#
# Return values:
#    - Either a scalar of a single IP-address if only one is wanted, or an
#      array if multiple IP-addresses are wanted.
#
# -----------------------------------------------------------------------------
sub get_node_ip {
    my %params = %{$_[0]};

    # Initialize local variables
    my $index       = exists $params{"index"} ? $params{"index"} : "";
    my $kubeconfig  = exists $params{"kubeconfig"} ? $params{"kubeconfig"} : $kubeconfig_file;
    my $name        = exists $params{"name"} ? $params{"name"} : "";
    my $type        = exists $params{"type"} ? $params{"type"} : "";

    my $command = "kubectl get nodes --no-headers=true -o wide" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : "");
    my $rc;
    my @result = ();
    my $role = get_node_role("master", $kubeconfig);
    my @std_error = ();
    my @wanted_lines = ();

    # Check validity of parameters
    if ($index ne "" && $name ne "") {
        # Not valid combination, return empty scalar
        return "";
    }
    if ($index ne "" && $index !~ /^\d+$/) {
        # The index must be a number, return empty scalar
        return "";
    }
    if ($type eq "master") {
        $command .= " --selector='node-role.kubernetes.io/$role'";
    } elsif ($type eq "worker") {
        $command .= " --selector='!node-role.kubernetes.io/$role'";
    } elsif ($type ne "") {
        # Not supported type
        if ($index ne "" || $name ne "") {
            # Return empty scalar
            return "";
        } else {
            # Return empty array
            return ();
        }
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        # Something went wrong with the command
        if ($index ne "" || $name ne "") {
            # Return empty scalar
            return "";
        } else {
            # Return empty array
            return ();
        }
    }
    if ($index ne "") {
        # Specific index wanted
        push @wanted_lines, $result[$index];
    } elsif ($name ne "") {
        # Specific name wanted
        for (@result) {
            if (/^$name\s+/) {
                push @wanted_lines, $_;
                last;
            }
        }
    } else {
        # More than one wanted
        @wanted_lines = @result;
    }

    # Empty the result array
    @result = ();

    for (@wanted_lines) {
        if (/^\S+\s+\S+\s+\S+\s+\S+\s+\S+\s+(\S+)\s+.+/) {
            # Example:
            # worker-pool1-001n3yln-eccd-eevee-ds   Ready   worker   29d   v1.21.1   10.10.10.239   <none>   SUSE Linux Enterprise Server 15 SP2   5.3.18-24.75.3.22886.0.PTF.1187468-default   containerd://1.4.4
            push @result, $1;
        }
    }

    if ($index ne "" || $name ne "") {
        # Only one value should be returned as a scalar
        if (scalar @result != 1) {
            # Something is wrong because we should only find 1 address, return empty scalar
            return "";
        } else {
            return $result[0];
        }
    } else {
        # Multiple values to return, just return what we found
        return @result;
    }
}

# -----------------------------------------------------------------------------
# Return information about the resources of one or more nodes.
# Resource information is for example the Capacity in CPU, Memory etc.,
# the Allocatable or Allocated resources of the node returned as a hash.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "index":
#           The index of the node (zero counting), if not specified and also
#           "name" is not specified then information about all found nodes are
#           returned.
#           Should not be combined with "name".
#       "name":
#           The exact name of the node, if not specified and also "index"
#           is not specified then information about all found nodes are returned.
#           Should not be combined with "index" or "type".
#       "type":
#           The type of node which can be one of:
#               - master
#               - worker
#           If not specified then all types will be checked.
#
# Return values:
#    - A hash of hashes with all information regarding the resources of the node
#      is returned to the caller, or at error an undefined (undef) is returned.
#      $node_info{$name}{$section} = ();
#      For example:
#      $node_info{'master-0-eccd-pikachu-ipv6'}{'Capacity'}{'cpu'} = 12
#      $node_info{'master-0-eccd-pikachu-ipv6'}{'Allocatable'}{'cpu'} = 11
#      $node_info{'master-0-eccd-pikachu-ipv6'}{'Allocated resources'}{'cpu Requests'} = 10825m
#
# -----------------------------------------------------------------------------
sub get_node_resources {
    my %params = %{$_[0]};

    # Initialize local variables
    my $index     = exists $params{"index"} ? $params{"index"} : "";
    my $name      = exists $params{"name"} ? $params{"name"} : "";
    my $filter    = '^(Allocatable|Allocated resources|Capacity)$';
    my $type      = exists $params{"type"} ? $params{"type"} : "";

    my %node_info;
    my %raw_node_info = get_node_information(
        {
            "name"          => $name,
            "index"         => $index,
            "return-filter" => $filter,
            "type"          => $type,
        }
    );

    for my $node (keys %raw_node_info) {
        if (exists $raw_node_info{$node}{'Allocatable'}) {
            #'Allocatable' => [
            #                    'cpu:                11',
            #                    'ephemeral-storage:  56864887098',
            #                    'hugepages-1Gi:      0',
            #                    'hugepages-2Mi:      0',
            #                    'memory:             32262388Ki',
            #                    'pods:               110'
            #                ],
            for (@{$raw_node_info{$node}{'Allocatable'}}) {
                if (/^\s*([^:]+):\s*(\S+)\s*$/) {
                    $node_info{$node}{'Allocatable'}{$1} = $2;
                }
            }
        }

        if (exists $raw_node_info{$node}{'Capacity'}) {
            #'Capacity' => [
            #                    'cpu:                11',
            #                    'ephemeral-storage:  56864887098',
            #                    'hugepages-1Gi:      0',
            #                    'hugepages-2Mi:      0',
            #                    'memory:             32262388Ki',
            #                    'pods:               110'
            #                ],
            for (@{$raw_node_info{$node}{'Capacity'}}) {
                if (/^\s*([^:]+):\s*(\S+)\s*$/) {
                    $node_info{$node}{'Capacity'}{$1} = $2;
                }
            }
        }

        if (exists $raw_node_info{$node}{'Allocated resources'}) {
            #'Allocated resources' => [
            #                            '(Total limits may be over 100 percent, i.e., overcommitted.)',
            #                            'Resource           Requests           Limits',
            #                            '--------           --------           ------',
            #                            'cpu                6945m (63%)        35870m (326%)',
            #                            'memory             15909380352 (48%)  55348474496 (167%)',
            #                            'ephemeral-storage  3522Mi (6%)        15086Mi (27%)'
            #                        ],
            for (@{$raw_node_info{$node}{'Allocated resources'}}) {
                if (/^\s*(cpu|memory|ephemeral-storage)\s+(\S+)\s+\((\d+)\%\)\s+(\S+)\s+\((\d+)\%\)\s*$/) {
                    $node_info{$node}{'Allocated resources'}{"$1 Requests"} = $2;
                    $node_info{$node}{'Allocated resources'}{"$1 Requests Percent"} = $3;
                    $node_info{$node}{'Allocated resources'}{"$1 Limits"} = $4;
                    $node_info{$node}{'Allocated resources'}{"$1 Limits Percent"} = $5;
                }
            }
        }
    }

    return %node_info;
}

# -----------------------------------------------------------------------------
# Return a the status of one or more nodes.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "index":
#           The index of the node, if not specified and also "name" is
#           not specified then all found status are returned.
#           Should not be combined with "name".
#       "kubeconfig":
#           Scalar variable pointing to the config file to use if other than
#           the default $kubeconfig_file.
#       "name":
#           The exact name of the node, if not specified and also "index"
#           is not specified then all found status are returned.
#           Should not be combined with "index".
#       "type":
#           The type of node which can be one of:
#               - master
#               - worker
#           If not specified then all types will be checked.
#
# Return values:
#    - Either a scalar of a single node status if only one is wanted, or an
#      array if multiple node status are wanted.
#
# -----------------------------------------------------------------------------
sub get_node_status {
    my %params = %{$_[0]};

    # Initialize local variables
    my $index     = exists $params{"index"} ? $params{"index"} : "";
    my $kubeconfig  = exists $params{"kubeconfig"} ? $params{"kubeconfig"} : $kubeconfig_file;
    my $name      = exists $params{"name"} ? $params{"name"} : "";
    my $type      = exists $params{"type"} ? $params{"type"} : "";

    my $command = "kubectl get nodes --no-headers=true" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : "");
    my $rc;
    my @result = ();
    my $role = get_node_role("master", $kubeconfig);
    my @std_error = ();
    my @wanted_lines = ();

    # Check validity of parameters
    if ($index ne "" && $name ne "") {
        # Not valid combination, return empty scalar
        return "";
    }
    if ($index ne "" && $index !~ /^\d+$/) {
        # The index must be a number, return empty scalar
        return "";
    }
    if ($type eq "master") {
        $command .= " --selector='node-role.kubernetes.io/$role'";
    } elsif ($type eq "worker") {
        $command .= " --selector='!node-role.kubernetes.io/$role'";
    } elsif ($type ne "") {
        # Not supported type
        if ($index ne "" || $name ne "") {
            # Return empty scalar
            return "";
        } else {
            # Return empty array
            return ();
        }
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        # Something went wrong with the command
        if ($index ne "" || $name ne "") {
            # Return empty scalar
            return "";
        } else {
            # Return empty array
            return ();
        }
    }
    if ($index ne "") {
        # Specific index wanted
        push @wanted_lines, $result[$index];
    } elsif ($name ne "") {
        # Specific name wanted
        for (@result) {
            if (/^$name\s+/) {
                push @wanted_lines, $_;
                last;
            }
        }
    } else {
        # More than one wanted
        @wanted_lines = @result;
    }

    # Empty the result array
    @result = ();

    for (@wanted_lines) {
        if (/^\S+\s+(\S+)\s+\S+\s+\S+\s+\S+.*/) {
            # Example:
            # worker-pool1-001n3yln-eccd-eevee-ds   Ready   worker   29d   v1.21.1
            push @result, $1;
        }
    }

    if ($index ne "" || $name ne "") {
        # Only one value should be returned as a scalar
        if (scalar @result != 1) {
            # Something is wrong because we should only find 1 address, return empty scalar
            return "";
        } else {
            return $result[0];
        }
    } else {
        # Multiple values to return, just return what we found
        return @result;
    }
}

# -----------------------------------------------------------------------------
# Return a list of top information for all nodes supported by kuberentes.
#
# Input variables:
#    -
#
# Return values:
#    - An array with top information for all nodes
#
# -----------------------------------------------------------------------------
sub get_node_top {
    my $rc;
    my @result = ();
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl top nodes --no-headers" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    @result = () if ($rc != 0);

    return @result;
}

# -----------------------------------------------------------------------------
# Return a list of top information for master nodes supported by kuberentes.
# A master node is any node that does have the role "master".
#
# Input variables:
#    -
#
# Return values:
#    - An array with top information of master nodes
#
# -----------------------------------------------------------------------------
sub get_node_top_master {
    my $kubeconfig = @_ ? shift : "";
    if ($kubeconfig eq "" && $kubeconfig_file ne "") {
        $kubeconfig = $kubeconfig_file;
    }

    my $rc;
    my @result = ();
    my $role = get_node_role("master", $kubeconfig);
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl top nodes --no-headers --selector='node-role.kubernetes.io/$role'" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    @result = () if ($rc != 0);

    return @result;
}

# -----------------------------------------------------------------------------
# Return a list of top information for worker nodes supported by kuberentes.
# A worker node is any node that does not have the role "master".
#
# Input variables:
#    -
#
# Return values:
#    - An array with top information of worker nodes
#
# -----------------------------------------------------------------------------
sub get_node_top_worker {
    my $kubeconfig = @_ ? shift : "";
    if ($kubeconfig eq "" && $kubeconfig_file ne "") {
        $kubeconfig = $kubeconfig_file;
    }

    my $rc;
    my @result = ();
    my $role = get_node_role("master", $kubeconfig);
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl top nodes --no-headers --selector='!node-role.kubernetes.io/$role'" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    @result = () if ($rc != 0);

    return @result;
}

# -----------------------------------------------------------------------------
# Get the actual pod name(s) matching user specified conditions.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "all-namespaces":
#           If specified then PODs from all name spaces will be checked.
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "kubeconfig":
#           Scalar variable pointing to the config file to use if other than
#           the default $kubeconfig_file.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#           See also the "all-namespaces" parameter.
#       "pod-exclude-list":
#           Array reference of variable containing a list of PODs to exclude
#           from the check, this parameter makes no sense if used with the
#           "pod-include-list" parameter since the pod-exlude-list has higher
#           precidence.
#           If this parameter is not included then the check will be done on
#           all PODs.
#       "pod-include-list":
#           Array reference of variable containing a list of PODs to check.
#           If this parameter is not included then the check will be done on
#           all PODs.
#
# Return values:
#    - The names of the pods matching the conditions are returned as an array
#      or an empty array if none are found or at error fetching the pod names.
#
# -----------------------------------------------------------------------------
sub get_pod_names {
    my %params = %{$_[0]};

    # Initialize local variables
    my $all_namespaces     = exists $params{"all-namespaces"} ? $params{"all-namespaces"} : 0;
    my $debug_messages     = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $hide_output        = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $kubeconfig         = exists $params{"kubeconfig"} ? $params{"kubeconfig"} : $kubeconfig_file;
    my $line;
    my $namespace          = exists $params{"namespace"} ? $params{"namespace"} : "";
    my $pod_exclude_list   = exists $params{"pod-exclude-list"} ? $params{"pod-exclude-list"} : undef;
    my $pod_include_list   = exists $params{"pod-include-list"} ? $params{"pod-include-list"} : undef;
    my $pod_name;
    my @pod_names = ();
    my $rc;
    my @result = ();
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get pod --no-headers -o custom-columns=NAME:.metadata.name" . ($namespace ne "" ? " --namespace $namespace" : "") . ($all_namespaces ? " --all-namespaces" : "") . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : ""),
            "hide-output"   => $hide_output,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("Failed to execute the command\n") if $debug_messages;
        return @pod_names;
    }

    for $line (@result) {
        if ($line =~ /^(\S+)\s*$/) {
            # eric-bsf-diameter-7dff78b4c7-p9vz9
            $pod_name = $1;
        } else {
            # Unknown format, just ignore it
            General::Logging::log_write("Unknown line format detected, ignore line.\n$line\n") if $debug_messages;
            next;
        }

        # Check if some specified PODs should be excluded from the check
        if ($pod_exclude_list) {
            my $found = 0;
            my $lines = 0;
            for my $name (@$pod_exclude_list) {
                $lines++;
                if ($pod_name =~ /$name/) {
                    $found = 1;
                    last;
                }
            }
            if ($lines > 0) {
                if ($found) {
                    General::Logging::log_write("The pod '$pod_name' is in the exclude list.\n$line\n") if $debug_messages;
                    next;
                }
                General::Logging::log_write("The pod '$pod_name' is not in the exclude list.\n$line\n") if $debug_messages;
            }
        }

        # Check if some specified PODs should be included in the check
        if ($pod_include_list) {
            my $found = 0;
            my $lines = 0;
            for my $name (@$pod_include_list) {
                $lines++;
                if ($pod_name =~ /$name/) {
                    $found = 1;
                    last;
                }
            }
            if ($lines > 0) {
                unless ($found) {
                    General::Logging::log_write("The pod '$pod_name' is not in the include list.\n$line\n") if $debug_messages;
                    next;
                }
                General::Logging::log_write("The pod '$pod_name' is in the include list.\n$line\n") if $debug_messages;
            }
        }

        # If we reached this point we have found a pod name that we want
        push @pod_names, $pod_name;
    }

    return @pod_names;
}

# -----------------------------------------------------------------------------
# Get the current set replicas of a deployment or statefulset.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "check-name":
#           Scalar variable that contains the name of the type of resource to
#           check.
#       "check-type":
#           Scalar variable that contains the type of resource to check, currently
#           only the following are supported:
#           - statefulset
#           - deployment
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "kubeconfig":
#           Scalar variable pointing to the config file to use if other than
#           the default $kubeconfig_file.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#       "return-message":
#           Scalar reference to variable where the result message will be
#           written indicating for what reason the check failed.
#
# Return values:
#    - undef if any failure was detected.
#    - Integer value of current replicas.
#
# -----------------------------------------------------------------------------
sub get_replicas {
    my %params = %{$_[0]};

    # Initialize local variables
    my $check_name              = exists $params{"check-name"} ? $params{"check-name"} : "";
    my $check_type              = exists $params{"check-type"} ? $params{"check-type"} : "";
    my $debug_messages          = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $hide_output             = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $kubeconfig              = exists $params{"kubeconfig"} ? $params{"kubeconfig"} : $kubeconfig_file;
    my $message                 = "";
    my $namespace               = exists $params{"namespace"} ? $params{"namespace"} : "";
    my $rc;
    my $replicas                = undef;
    my @result                  = ();
    my $return_message          = exists $params{"return-message"} ? $params{"return-message"} : undef;
    my $scale_line              = "";
    my @std_error = ();

    if ($check_name eq "") {
        $message = "No 'check-name' specified'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return undef;
    }

    if ($check_type eq "") {
        $message = "No 'check-type' specified, it should be 'deployment' or 'statefulset'\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return undef;
    } elsif ($check_type !~ /^(deployment|statefulset)$/) {
        # Wrong type
        $message = "Wrong 'check-type' specified ($check_type), only 'deployment' or 'statefulset' allowed\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return undef;
    }

    General::Logging::log_write("Checking replicas of $check_type $check_name") if $debug_messages;
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get $check_type --no-headers" . ($namespace ne "" ? " --namespace $namespace" : "") . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : ""),
            "hide-output"   => $hide_output,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );

    if ($rc != 0) {
        $message = "Fetching replicas failed because the kubectl command failed with rc=$rc";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return undef;
    }

    my $found = 0;
    for (@result) {
        if ($check_type eq "deployment" && /^$check_name\s+(\d+)\/(\d+)\s+(\d+)\s+(\d+)\s+\S+/) {
            $scale_line = $_;
            # NAME               READY   UP-TO-DATE   AVAILABLE   AGE
            # eric-sepp-worker   2/2     2            2           22test_get_worker_nodes.plh
            if ($2 == $1 && $2 == $3 && $2 == $4) {
                $found = 1;
                $replicas = $2;
                last;
            } else {
                # The deployment is not completely up yet, this might still be OK but we report this in the $return_message if the user want it
                $found = 1;
                $replicas = $2;
                $message = "The $check_type $check_name is not yet ready, up-to-date or available.\n$scale_line\n";
                General::Logging::log_write($message) if $debug_messages;
                $$return_message = $message if $return_message;
                last;
            }
        } elsif ($check_type eq "statefulset" && /^$check_name\s+(\d+)\/(\d+)\s+\S+/) {
            $scale_line = $_;
            # NAME                                                  READY   AGE
            # eric-data-wide-column-database-cd-datacenter1-rack1   2/2     108m
            if ($1 == $2) {
                $found = 1;
                $replicas = $2;
                last;
            } else {
                # The statefulset is not completely up yet, this might still be OK but we report this in the $return_message if the user want it
                $found = 1;
                $replicas = $2;
                $message = "The $check_type $check_name is not yet ready.\n$scale_line\n";
                General::Logging::log_write($message) if $debug_messages;
                $$return_message = $message if $return_message;
                last;
            }
        }
    }
    if ($found == 1) {
        # The replicas were found
        $message = "The $check_type $check_name replicas has been found (=$replicas).\n";
        General::Logging::log_write($message) if $debug_messages;
        return $replicas;
    } else {
        # This should not happen, but just in case we cover it.
        $message = "The $check_type $check_name failed because we could not find the number of replicas in the printout.\n";
        General::Logging::log_write($message) if $debug_messages;
        $$return_message = $message if $return_message;
        return undef;
    }
}

# -----------------------------------------------------------------------------
# Get the a list of resource name(s) matching user specified conditions.
# A resource can e.g. be of the following known to work types:
#   - crd
#   - job
#   - pod
#   - pvc
#   - secret
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "all-namespaces":
#           If specified then resources from all name spaces will be checked.
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "exclude-list":
#           Array reference of variable containing a list of resources to exclude
#           from the check, this parameter makes no sense if used with the
#           "include-list" parameter since the "exlude-list" has higher
#           precidence.
#           If this parameter is not included then the check will be done on
#           all resources.
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "include-list":
#           Array reference of variable containing a list of resources to check.
#           If this parameter is not included then the check will be done on
#           all resources.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#           See also the "all-namespaces" parameter.
#       "resource":
#           Scalar variable of the type of resource to check, and this is a
#           mandatory parameter to specify and it can e.g. be one of the following
#           known to work types:
#               - crd
#               - job
#               - namespace
#               - node
#               - pod
#               - pvc
#               - secret
#               - service
#
# Return values:
#    - The names of the resources matching the conditions are returned as an array
#      or an empty array if none are found or at error fetching the resource names.
#
# -----------------------------------------------------------------------------
sub get_resource_names {
    my %params = %{$_[0]};

    # Initialize local variables
    my $all_namespaces     = exists $params{"all-namespaces"} ? $params{"all-namespaces"} : 0;
    my $debug_messages     = exists $params{"debug-messages"} ? $params{"debug-messages"} : 0;
    my $exclude_list       = exists $params{"exclude-list"}   ? $params{"exclude-list"}   : undef;
    my $hide_output        = exists $params{"hide-output"}    ? $params{"hide-output"}    : 0;
    my $include_list       = exists $params{"include-list"}   ? $params{"include-list"}   : undef;
    my $line;
    my $namespace          = exists $params{"namespace"}      ? $params{"namespace"}      : "";
    my $rc;
    my $resource           = exists $params{"resource"}       ? $params{"resource"}       : "";
    my $resource_name;
    my @resource_names = ();
    my @result = ();
    my @std_error = ();

    if ($resource eq "") {
        General::Logging::log_write("You have to specify a resource name e.g. crd, job, pod, pvc, secret etc.\n") if $debug_messages;
        return @resource_names;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get $resource --no-headers -o custom-columns=NAME:.metadata.name" . ($namespace ne "" ? " --namespace $namespace" : "") . ($all_namespaces ? " --all-namespaces" : "") . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => $hide_output,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("Failed to execute the command\n") if $debug_messages;
        return @resource_names;
    }

    for $line (@result) {
        if ($line =~ /^(\S+)\s*$/) {
            # certificateauthorities.com.ericsson.sec.tls
            $resource_name = $1;
        } else {
            # Unknown format, just ignore it
            General::Logging::log_write("Unknown line format detected, ignore line.\n$line\n") if $debug_messages;
            next;
        }

        # Check if some specified resources should be excluded from the check
        if ($exclude_list) {
            my $found = 0;
            my $lines = 0;
            for my $name (@$exclude_list) {
                $lines++;
                if ($resource_name =~ /$name/) {
                    $found = 1;
                    last;
                }
            }
            if ($lines > 0) {
                if ($found) {
                    General::Logging::log_write("The resource '$resource_name' is in the exclude list.\n$line\n") if $debug_messages;
                    next;
                }
                General::Logging::log_write("The resource '$resource_name' is not in the exclude list.\n$line\n") if $debug_messages;
            }
        }

        # Check if some specified resources should be included in the check
        if ($include_list) {
            my $found = 0;
            my $lines = 0;
            for my $name (@$include_list) {
                $lines++;
                if ($resource_name =~ /$name/) {
                    $found = 1;
                    last;
                }
            }
            if ($lines > 0) {
                unless ($found) {
                    General::Logging::log_write("The resource '$resource_name' is not in the include list.\n$line\n") if $debug_messages;
                    next;
                }
                General::Logging::log_write("The resource '$resource_name' is in the include list.\n$line\n") if $debug_messages;
            }
        }

        # If we reached this point we have found a resource name that we want
        push @resource_names, $resource_name;
    }

    return @resource_names;
}

# -----------------------------------------------------------------------------
# Get the current running SC or DSC release version from the deployed helm
# umbrella file for a specific namespace.
#
# Input variables:
#    - SC Namespace
#
# Return values:
#    - The release version e.g. 1.10.0 or an empty string if nothing found or
#       when an error happened.
#
# -----------------------------------------------------------------------------
sub get_sc_release_version {
    my $namespace = shift;

    my $app_type;
    my $debug_messages = 1;
    my $rc;
    my @result = ();
    my @std_error = ();
    my $version = "";

    return $version unless $namespace;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "helm list --all --namespace $namespace" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_sc_release_version: Failed to execute the command.\n") if $debug_messages;
        return $version;
    }

    for (@result) {
        if ($_ =~ /^\S+\s+$namespace\s+.+deployed\s+eric-(dsc|sc-\S+)-(\d+\.\d+\.\d+)\S*.*/) {
            $app_type = $1;
            $version = $2;
            last;
        }
    }

    return $version;
}

# -----------------------------------------------------------------------------
# Get all names for each section from a printout where each section starts with
# the "Name:" string.
# This sub routine only returns the name of all found "Name:" sections.
# For example if the printout looks like this:
# Name:               master-2-n103-dsc5429-ipv6
# Roles:              control-plane,master
# Labels:             beta.kubernetes.io/arch=amd64
#                     beta.kubernetes.io/instance-type=m1.large.16g
# : etc.
# Events:              <none>
#
#
# Name:               worker-pool1-7kj2fqpv-n103-dsc5429-ipv6
# Roles:              worker
# Labels:             beta.kubernetes.io/arch=amd64
#                     beta.kubernetes.io/instance-type=ecmeoc4391-pool1
# : etc.
#
# Then the following information is returned to the user as an array:
# master-2-n103-dsc5429-ipv6
# worker-pool1-7kj2fqpv-n103-dsc5429-ipv6
#
# Input variables:
#    - A reference to an array containing the data to check.
#
# Return values:
#    - An array with the found names, or an empty array if none are found.
#
# -----------------------------------------------------------------------------
sub get_section_names {
    my $array_ref = shift;

    my @result = ();

    return @result unless $array_ref;

    for (@$array_ref) {
        if (/^Name:\s*(\S+)/) {
            push @result, $1;
        }
    }

    return @result;
}

# -----------------------------------------------------------------------------
# Get the semantic version of the SC deployment.
# The semantic version is the version including the build number, for example
# 1.15.25+1058.
# It will fetch this version either from the version of the eric-sc-umbrella
# deployment or from helm values of the eric-sc-cs deployment.
#
# Input variables:
#    - The namespace.
#
# Return values:
#    - The version including build number or an empty string at error.
#
# -----------------------------------------------------------------------------
sub get_semantic_version {
    my $namespace = shift;
    my $cmyp_user = shift;
    my $cmyp_password = shift;

    my $rc;
    my @result;
    my @std_error;
    my $release_name = "";
    my $sc_cs_release_name = "";
    my $app_type;
    my $version;
    my $build;
    my $app_version;
    my $dsc_version = "";

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "helm list --all --namespace $namespace"  . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("ADP::Kubernetes_Operations::get_semantic_version: Failed to fetch helm list.\n");
        return "";
    }

    for (@result) {
        if (/^(\S+)\s+$namespace\s+.+deployed\s+(\S+-)(\d+\.\d+\.\d+)(\S*)(.*)/) {
            $release_name = $1;
            $app_type = $2;
            $version = $3;
            $build = $4;
            $app_version = $5;
            if ($app_type =~ /^eric-sc-umbrella-\S*/) {
                # We found what we are looking for, no reason to check further
                return "$version$build";
            } elsif ($app_type =~ /^eric-dsc-\S*/) {
                if (defined $app_version && defined $build && $build !~ /^[-+]\d+$/ && $app_version =~ /^\s+(\d+\.\d+\.\d+)([-+]\d+)\s*$/) {
                    # A special case where the CHART column contains a build that is not a number.
                    # For example: "-1-h9ffcf88" as in "eric-dsc-1.9.0-1-h9ffcf88" but the APP VERSION
                    # contains the correct build number "-1015" as in "1.9.0-1015".
                    my $temp_app_version = $1;
                    my $temp_app_build = $2;
                    if ($version eq $temp_app_version) {
                        # We use the build number from the APP VERSION instead of CHART
                        $build = $temp_app_build;
                    }
                }
                # Save the version until later in case it's an older dsc release (1.9 to 1.14)
                # where no swim information is provided in the eric-sc-cs helm chart but only
                # inside the CMYP swim information.
                $dsc_version = "$version$build";
            } elsif ($app_type =~ /^eric-sc-cs-\S*/) {
                # New CNCS from SC 1.15.0 or older CNCDSC 1.9 to 1.14
                # We still need to continue to look in case it's the dsc deployment.
                # If we don't find the dsc then we need to fetch values from the sc-cs release to find the
                # swim information included in the eric-sc-cs helm chart.
                $sc_cs_release_name = $release_name;
            }
        }
    }

    if ($sc_cs_release_name eq "" && $dsc_version ne "") {
        # It's some strange CNDSC version maybe 1.9 or 1.10, so we just return back the found dsc version
        return $dsc_version;
    }

    if ($sc_cs_release_name ne "") {
        # CNDSC 1.9 to 1.14 or CNCS 1.15 and newer.
        # We now need to look inside the eric-sc-cs deployment to find the swim information.
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "helm get values --namespace $namespace $sc_cs_release_name"  . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
        if ($rc != 0) {
            General::Logging::log_write("ADP::Kubernetes_Operations::get_semantic_version: Failed to fetch helm get values.\n");
            return "";
        }

        my $swim_found = 0;
        for (@result) {
            if (/^swim:\s*$/) {
                $swim_found = 1;
            } elsif ($swim_found == 1 && /^\s+semanticVersion:\s*(\S+)/) {
                #  semanticVersion: 1.15.0+5
                # We found what we are looking for, no reason to check further
                return $1;
            }
        }
    }

    # If we reach here, we did not find any swim information inside the eric-sc-cs helm chart
    # so we need to try to connect to the CMYP and fetch the swim informnation from there.

    # Set user and password unless the user passed the information into the subroutine
    $cmyp_user = "expert" unless (defined $cmyp_user);
    $cmyp_password = "rootroot" unless (defined $cmyp_password);

    # Find the IP-address of the CMYP
    my $cmyp_ip = get_cmyp_ip($namespace);
    $rc = General::OS_Operations::send_command_via_ssh(
        {
            "commands"      => [ "show swim sw-version | display json | nomore" ],
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
            "ip"            => $cmyp_ip,
            "user"          => $cmyp_user,
            "password"      => $cmyp_password,
            "timeout"       => 10,
        }
    );
    if ($rc == 0) {
        for (@result) {
            if (/^\s+"semantic-version":\s+"(\S+)"/) {
                #            "semantic-version": "1.15.25+1081",
                # We found what we were looking for
                return $1;
            }
        }
    } else {
        General::Logging::log_write("General::OS_Operations::send_command_via_ssh: Failed to fetch swim information from CMYP.\n");
    }

    # If we reach here we did not find what we are looking for in CMYP
    if ($dsc_version ne "") {
        # It seems we found the dsc version above, so we return this.
        return $dsc_version;
    } else {
        # We found no version, return an empty string
        return "";
    }
}

# -----------------------------------------------------------------------------
# Return one or more IP addresses for a specific service.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "all-namespaces":
#           If specified then IP addresses from all name spaces that matches
#           the other conditions will be returned.
#       "check-ssh":
#           If specified and have value 'yes' or '1' and 'ip-type' is not
#           specified, then a check for SSH connectivity will be done before
#           selecting if cluster or external IP-address is returned.
#           Whichever can be reachable by SSH will be selected, if none is
#           working then the one returned depends on the order specified by
#           'ip-type' below.
#
#           If not specified or have value 'no' or 0' then no SSH check is
#           performed.
#       "debug-messages":
#           Scalar variable that indicates if special debug messages should be
#           printed to the log file, if opened, to aid in trouble shooting
#           issues with the logic.
#           If value is "0" then no debug messages will be printed, any other
#           value will print these messages.
#           The default, if not specified, is to not print any debug messages.
#       "exclude-list":
#           Array reference of variable containing a list of service names to
#           exclude from the check, this parameter makes no sense if used with
#           the "include-list" parameter since the "exlude-list" has higher
#           precidence.
#           If this parameter is not included then the check will be done on
#           all service names.
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "include-list":
#           Array reference of variable containing a list of service names to
#           check.
#           If this parameter is not included then the check will be done on
#           all service names.
#           NOTE: If multiple services is matching or if all namespaces has
#           the matching service then the caller will not know which IP-address
#           match which service or namespace.
#       "ip-type":
#           Scalar variable of the type of IP-address to return and it can
#           be one of the following types:
#               - cluster
#                 The IP-address is read from the CLUSTER-IP column.
#               - external
#                 The IP-address is read from the EXTERNAL-IP column.
#           It will return whatever value is stored in the wanted column, be
#           it a valid IP-address or the strings "None", "<none>" or even
#           "<pending>" etc.
#
#           If this is not specified then the address to be returned will
#           be picked in the following order:
#               - If the EXTERNAL-IP contains a valid value then this is
#                 returned.
#               - If the CLUSTER-IP contains a valid value then this is
#                 returned.
#               - If neither EXTERNAL-IP nor CLUSTER-IP contains a valid
#                 value then nothing is returned.
#       "namespace":
#           Scalar variable of the name of the namespace to check, if not
#           specified then the namespace will either need to be included in
#           the ~/.kube/config file or no namespace will be used (default
#           namespace).
#           See also the "all-namespaces" parameter.
#       "port":
#           Integer value indicating which port number to use when checking
#           if SSH is available.
#           If not specified then value 22 is used.
#           Only used if also 'check-ssh' is 1/yes.
#       "repeat-at-error":
#           Integer value indicating how many times it should try to check
#           if SSH is available at failure. A value of 0 means only one
#           attempt it tried, a value of 1 means it will try one extra time
#           at failure, 2 means try 2 times at failue, etc.
#           If not specified then value 0 is used.
#           Only used if also 'check-ssh' is 1/yes.
#       "return-only-one":
#           If specified and have value 'yes' or '1' then return only one
#           found IP-address regardless of how many valid addresses are found.
#           If not specified or have value 'no' or '0' then as many IP-addresses
#           that are found valid will be returned.
#       "service-names":
#           If specified it should contain a array reference where the found
#           service names are returned. The names in the array are in the same
#           order as the returned IP-addresses.
#       "timeout":
#           Integer value indicating how seconds to wait for answer to
#           SSH request before giving up.
#           If not specified then value 2 is used.
#           Only used if also 'check-ssh' is 1/yes.
#
# Return values:
#    - Either an array if one or more valid IP-addresses are found or an empty
#      array if none is found.
#      NOTE: Returning of multiple IP-addresses might not be of any use
#      other than to e.g. compare the different IP-addresses, because the caller
#      will not know which IP-address belongs to which service name or to which
#      namespace.
#      If a failure was detected then an undefined value is returned.
#
# -----------------------------------------------------------------------------
sub get_service_ip {
    my %params = %{$_[0]};

    # Initialize local variables
    my $all_namespaces     = exists $params{"all-namespaces"}  ? $params{"all-namespaces"}  : 0;
    my $check_ssh          = exists $params{"check-ssh"}       ? $params{"check-ssh"}       : 0;
    my $cluster_ip;
    my $debug_messages     = exists $params{"debug-messages"}  ? $params{"debug-messages"}  : 0;
    my $exclude_list       = exists $params{"exclude-list"}    ? $params{"exclude-list"}    : undef;
    my $external_ip;
    my $hide_output        = exists $params{"hide-output"}     ? $params{"hide-output"}     : 0;
    my $include_list       = exists $params{"include-list"}    ? $params{"include-list"}    : undef;
    my @ip_addresses = ();
    my $ip_type            = exists $params{"ip-type"}         ? $params{"ip-type"}         : "";
    my $line;
    my $namespace          = exists $params{"namespace"}       ? $params{"namespace"}       : "";
    my $port               = exists $params{"port"}            ? $params{"port"}            : 22;
    my $rc;
    my $repeat_at_error    = exists $params{"repeat-at-error"} ? $params{"repeat-at-error"} : 0;
    my @result = ();
    my $return_only_one    = exists $params{"return-only-one"} ? $params{"return-only-one"} : 0;
    my $service_name;
    my $service_names      = exists $params{"service-names"}   ? $params{"service-names"}   : undef;
    my @std_error = ();
    my $timeout            = exists $params{"timeout"}         ? $params{"timeout"}         : 2;

    $check_ssh = lc($check_ssh);
    if ($check_ssh eq "yes") {
        $check_ssh = 1;
    } elsif ($check_ssh eq "no") {
        $check_ssh = 0;
    } elsif ($check_ssh =~ /^\d+$/) {
        if ($check_ssh > 0) {
            $check_ssh = 1;
        } else {
            $check_ssh = 0;
        }
    } else {
        General::Logging::log_write("Incorrect 'check-ssh' specified ($check_ssh), only '1', 'yes', '0' or 'no' allowed\n") if $debug_messages;
        return undef;
    }

    $ip_type = lc($ip_type);
    if ($ip_type ne "") {
        unless ($ip_type =~ /^(cluster|external)$/) {
            General::Logging::log_write("Incorrect 'ip-type' specified ($ip_type), only 'cluster' or 'external' allowed\n") if $debug_messages;
            return undef;
        }
    }

    $return_only_one = lc($return_only_one);
    if ($return_only_one eq "yes") {
        $return_only_one = 1;
    } elsif ($return_only_one eq "no") {
        $return_only_one = 0;
    } elsif ($return_only_one =~ /^\d+$/) {
        if ($return_only_one > 0) {
            $return_only_one = 1;
        } else {
            $return_only_one = 0;
        }
    } else {
        General::Logging::log_write("Incorrect 'return-only-one' specified ($return_only_one), only '1', 'yes', '0' or 'no' allowed\n") if $debug_messages;
        return undef;
    }

    # Clear service_names if specified.
    @$service_names = () if $service_names;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get service --no-headers" . ($namespace ne "" ? " --namespace $namespace" : "") . ($all_namespaces ? " --all-namespaces" : "") . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => $hide_output,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc != 0) {
        General::Logging::log_write("Failed to execute the command\n") if $debug_messages;
        return undef;
    }

    for $line (@result) {
        if ($all_namespaces && $line =~ /^\S+\s+(\S+)\s+\S+\s+(\S+)\s+(\S+)\s+\S+\s+\S+\s*$/) {
            # eiffelesc        eric-cm-yang-provider                                 LoadBalancer   10.1.0.178   10.228.218.94   830:32047/TCP,22:30473/TCP                                       7d4h
            $service_name = $1;
            $cluster_ip   = $2;
            $external_ip  = $3;
        } elsif ($line =~ /^(\S+)\s+\S+\s+(\S+)\s+(\S+)\s+\S+\s+\S+\s*$/) {
            # eric-cm-yang-provider                                 LoadBalancer   10.1.0.178   10.228.218.94   830:32047/TCP,22:30473/TCP                                       7d4h
            $service_name = $1;
            $cluster_ip   = $2;
            $external_ip  = $3;
        } else {
            # Unknown format, just ignore it
            General::Logging::log_write("Unknown line format detected, ignore line.\n$line\n") if $debug_messages;
            next;
        }

        # Check if some specified resources should be excluded from the check
        if ($exclude_list) {
            my $found = 0;
            my $lines = 0;
            for my $name (@$exclude_list) {
                $lines++;
                if ($service_name =~ /$name/) {
                    $found = 1;
                    last;
                }
            }
            if ($lines > 0) {
                if ($found) {
                    General::Logging::log_write("The service '$service_name' is in the exclude list.\n$line\n") if $debug_messages;
                    next;
                }
                General::Logging::log_write("The service '$service_name' is not in the exclude list.\n$line\n") if $debug_messages;
            }
        }

        # Check if some specified resources should be included in the check
        if ($include_list) {
            my $found = 0;
            my $lines = 0;
            for my $name (@$include_list) {
                $lines++;
                if ($service_name =~ /$name/) {
                    $found = 1;
                    last;
                }
            }
            if ($lines > 0) {
                unless ($found) {
                    General::Logging::log_write("The service '$service_name' is not in the include list.\n$line\n") if $debug_messages;
                    next;
                }
                General::Logging::log_write("The service '$service_name' is in the include list.\n$line\n") if $debug_messages;
            }
        }

        # If we reached this point we have found a service name that we want
        if ($ip_type eq "") {
            # All IP types (external and Cluster) is checked, but first external then cluster and only
            # IP addresses with SSH connectivity will be returned.

            # First check the EXTERNAL-IP address.
            if ($external_ip !~ /^<\S+>$/) {
                # Looks like a valid EXTERNAL-IP address, now check if SSH connectivity is working.
                if ($check_ssh == 1 && General::OS_Operations::is_ssh_available($external_ip, $port, $timeout, $repeat_at_error) == 1) {
                    # SSH is working, so store the IP-address.
                    push @ip_addresses, $external_ip;
                    push @$service_names, $service_name if $service_names;
                    last if $return_only_one;
                } elsif ($check_ssh == 0) {
                    # SSH check not wanted, so store the IP-address.
                    push @ip_addresses, $external_ip;
                    push @$service_names, $service_name if $service_names;
                    last if $return_only_one;
                } else {
                    # SSH is not working, so ignore this IP-address
                    General::Logging::log_write("SSH towards external IP-address ($external_ip) for service name $service_name not working, address ignored\n") if $debug_messages;
                }
            }

            # Next check the CLUSTER-IP address.
            if ($cluster_ip ne "None") {
                # Looks like a valid CLUSTER-IP address, now check if SSH connectivity is working.
                if ($check_ssh == 1 && General::OS_Operations::is_ssh_available($cluster_ip, $port, $timeout, $repeat_at_error) == 1) {
                    push @ip_addresses, $cluster_ip;
                    push @$service_names, $service_name if $service_names;
                    last if $return_only_one;
                } elsif ($check_ssh == 0) {
                    # SSH check not wanted, so store the IP-address.
                    push @ip_addresses, $cluster_ip;
                    push @$service_names, $service_name if $service_names;
                    last if $return_only_one;
                } else {
                    # SSH is not working, so ignore this IP-address
                    General::Logging::log_write("SSH towards cluster IP-address ($cluster_ip) for service name $service_name not working, address ignored\n") if $debug_messages;
                }
            }
        } elsif ($ip_type eq "external") {
            if ($external_ip !~ /^<\S+>$/) {
                # Looks like a valid EXTERNAL-IP address, now check if SSH connectivity is working.
                if ($check_ssh == 1 && General::OS_Operations::is_ssh_available($external_ip, $port, $timeout, $repeat_at_error) == 1) {
                    # SSH is working, so store the IP-address.
                    push @ip_addresses, $external_ip;
                    push @$service_names, $service_name if $service_names;
                    last if $return_only_one;
                } elsif ($check_ssh == 0) {
                    # SSH check not wanted, so store the IP-address.
                    push @ip_addresses, $external_ip;
                    push @$service_names, $service_name if $service_names;
                    last if $return_only_one;
                } else {
                    # SSH is not working, so ignore this IP-address
                    General::Logging::log_write("SSH towards external IP-address ($external_ip) for service name $service_name not working, address ignored\n") if $debug_messages;
                }
            }
        } elsif ($ip_type eq "cluster") {
            if ($cluster_ip ne "None") {
                # Looks like a valid CLUSTER-IP address, now check if SSH connectivity is working.
                if ($check_ssh == 1 && General::OS_Operations::is_ssh_available($cluster_ip, $port, $timeout, $repeat_at_error) == 1) {
                    push @ip_addresses, $cluster_ip;
                    push @$service_names, $service_name if $service_names;
                    last if $return_only_one;
                } elsif ($check_ssh == 0) {
                    # SSH check not wanted, so store the IP-address.
                    push @ip_addresses, $cluster_ip;
                    push @$service_names, $service_name if $service_names;
                    last if $return_only_one;
                } else {
                    # SSH is not working, so ignore this IP-address
                    General::Logging::log_write("SSH towards cluster IP-address ($cluster_ip) for service name $service_name not working, address ignored\n") if $debug_messages;
                }
            }
        } else {
            General::Logging::log_write("Unexpected ip_type ($ip_type) for service name $service_name, addresses ignored\n") if $debug_messages;
            next;
        }
    }

    return @ip_addresses;
}

# -----------------------------------------------------------------------------
# Return the name of the Service for a specific namespace.
# It will look for the service name specified and also for names that also
# ends with '-ipv4' and '-ipv6'.
#
# Input variables:
#    - namespace
#    - service name
#
# Return values:
#    - The name of the service.
#
# -----------------------------------------------------------------------------
sub get_service_name {
    my $namespace = shift;
    my $service = shift;
    my @services;

    return "" unless $namespace;
    return "" unless $service;

    @services = get_resource_names(
        {
            "hide-output"       => 1,
            "include-list"      => [ "^$service\$", "^$service-ipv4\$", "^$service-ipv6\$" ],
            "namespace"         => $namespace,
            "resource"          => "service",
        }
    );
    if (scalar @services == 0) {
        return "";
    } else {
        # Regardless of how many service names are returned we currently only return index 0
        return $services[0];
    }
}

# -----------------------------------------------------------------------------
# Get information from a printout specified by a string which is the top level
# of the printout and return all other data until next top level top level.
# A top level is a string starting on the first column on a line (character 1)
# and ends with a :.
# For example if the printout looks like this:
# Addresses:
#   InternalIP:  10.10.10.11
#   Hostname:    master-0-eccd-sc-eevee
# Capacity:
#   cpu:                2
#   ephemeral-storage:  20931216Ki
#   hugepages-1Gi:      0
#   hugepages-2Mi:      0
#   memory:             4030984Ki
#   pods:               110
#
# If the user specifies top level as "Addresses" then the following information
# is returned to the caller:
# Addresses:
#   InternalIP:  10.10.10.11
#   Hostname:    master-0-eccd-sc-eevee
#
# Input variables:
#    - Name of the top level
#    - A reference to an array containing the data to check.
#
# Return values:
#    - An array with the found information, or an empty array if not found.
#      The returned data will also contain the top level string.
#
# -----------------------------------------------------------------------------
sub get_top_level_section_information {
    my $name = shift;
    my $array_ref = shift;

    my $reading = 0;
    my @result = ();

    return @result unless $name;
    return @result unless $array_ref;

    $name =~ s/:\s*$//;

    for (@$array_ref) {
        if (/^$name:/) {
            $reading = 1;
            push @result, $_;
            next;
        }
        if ($reading == 1 && /^\s+\S+/) {
            push @result, $_;
        } else {
            $reading = 0;
        }
    }

    return @result;
}

# -----------------------------------------------------------------------------
# Return a list of defined worker nodes names supported by kuberentes.
# A worker node is any node that does not have the role "master".
#
# Input variables:
#    -
#
# Return values:
#    - An array with a list of worker nodes
#
# -----------------------------------------------------------------------------
sub get_worker_nodes {
    my $kubeconfig = @_ ? shift : "";
    if ($kubeconfig eq "" && $kubeconfig_file ne "") {
        $kubeconfig = $kubeconfig_file;
    }

    my $rc;
    my @result = ();
    my $role = get_node_role("master", $kubeconfig);
    my @std_error = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes --no-headers -o custom-columns=NAME:.metadata.name --selector='!node-role.kubernetes.io/$role'" . ($kubeconfig ne "" ? " --kubeconfig=$kubeconfig" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    @result = () if ($rc != 0);

    return @result;
}

# -----------------------------------------------------------------------------
# Check if the specified namespace exists.
#
# Input variables:
#    - Name of the namespace to check.
#
# Return values:
#    - 0: The namespace does not exist.
#    - 1: The namespace exist.
#    - 2: Error while checking for namespace
#
# -----------------------------------------------------------------------------
sub namespace_exists {
    my $namespace = shift;

    my $found = 0;
    my $rc;
    my @result = ();
    my @std_error = ();

    return 0 unless $namespace;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get namespace" . ($kubeconfig_file ne "" ? " --kubeconfig=$kubeconfig_file" : ""),
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@std_error,
        }
    );
    if ($rc == 0) {
        for (@result) {
            next if (/^NAME\s+STATUS\s+AGE/);
            if (/^$namespace\s+Active\s+\S+/) {
                $found = 1;
                last;
            }
        }
    } else {
        # Something went wrong when getting namespace
        $found = 2;
    }

    return $found;
}

# -----------------------------------------------------------------------------
# Set kubeconfig file to be used for all kubectl and helm commands.
# If not set then whatever is defined under ~/.kube/config file will be used.
#
# Input variables:
#    - Name of kubeconfig file to use, it must exist or be "" to clear it.
#
# Return values:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub set_used_kubeconfig_filename {
    my $filename = shift;

    if ($filename eq "") {
        $kubeconfig_file = "";
    } elsif (-f "$filename") {
        $kubeconfig_file = $filename;
    } else {
        print "Specified file $filename does not exists, nothing changed.\n";
    }
}

1;

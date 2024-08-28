#!/bin/python

import json
import sys
import argparse
import collections



def find_key_single(json_d, key):
    
    if key in json_d: 
        return json_d[key]

    for k, v in json_d.items():
        if isinstance(v, dict):
            # nested search
            res = find_key_single(v, key)
            if res is not None:
                # key found! return
                return res


def replace_all_keys(json_d, key, classFqdn, jsonType):
    
    if key in json_d: 
        return json_d[key]

    for k, v in json_d.items():
        if isinstance(v, dict):
            # nested search
            res = replace_all_keys(v, key, classFqdn, jsonType)
            if res is not None:
                process_result(res, key, classFqdn, jsonType)


def process_result(result, key, classFqdn, jsonType):
    if result is not None:
        if result.get('type') == "array":
            result = result['items']

        print("Appending " + jsonType + '=\'' + str(classFqdn) + "' to " + str(result))
        result[jsonType] = classFqdn
    else:
        print("ERROR: key=" + key + " was not found")
        exit(1)


def appendNew(data, key, classFqdn, jsonType):
    sData = data

    if "." in key:
        # fqdn targeted search
        keys = key.split(".")
        for k in keys:
            sData = find_key_single(sData, k)
        
        # process the single result
        result = sData
        process_result(result, key, classFqdn, jsonType)
    else:
        # find all occurrences of key and process them all
        sData = replace_all_keys(sData, key, classFqdn, jsonType)


def appendNewJavaType(data, key, classFqdn):
    appendNew(data, key, classFqdn, "javaType")

def appendNewJavaInterfaces(data, key, classFqdn):
    appendNew(data, key, classFqdn, "javaInterfaces")

def appendNewExtendsJavaClass(data, key, classFqdn):
    appendNew(data, key, classFqdn, "extendsJavaClass")


def processNfm(data, nfmName, schemaName):
    
    ##### CLASSES
    appendNewJavaType(data, "ipv4-addr-range", nfmName + ".Ipv4AddrRange")
    appendNewJavaType(data, "ipv6-prefix-range", nfmName + ".Ipv6PrefixRange")
    appendNewJavaType(data, "plmn-id", nfmName + ".PlmnId")
    appendNewJavaType(data, "snssai", nfmName + ".Snssai")
    appendNewJavaType(data, "ip-endpoint", nfmName + ".IpEndpoint")
    appendNewJavaType(data, "endpoint-type", nfmName + ".EndpointType")
    appendNewJavaType(data, "service-address-type", nfmName + ".ServiceAddressType")
    appendNewJavaType(data, "supi-range", nfmName + ".SupiRange")
    appendNewJavaType(data, "identity-range", nfmName + ".IdentityRange")
    appendNewJavaType(data, "plmn-range", nfmName + ".PlmnRange")
    appendNewJavaType(data, "nf-profile", nfmName + ".NfProfile")
    appendNewJavaType(data, "nf-service", nfmName + ".NfService")
    appendNewJavaType(data, "chf-info", nfmName + ".ChfInfo")
    appendNewJavaType(data, "bsf-info", nfmName + ".BsfInfo")
    appendNewJavaType(data, "nrf-type", nfmName + ".NrfType")

    appendNewJavaInterfaces(data, "nf-profile", ["com.ericsson.utilities.common.IfNamedListItem"])

    ##### ENUMS

    ## nf-status
    appendNewJavaType(data, "nf-status", nfmName + ".NfStatus")

    ## nf-type
    appendNewJavaType(data, "nf-type", nfmName + ".NfType")

    # ## nf-service-scheme
    appendNewJavaType(data, "scheme", nfmName + ".Scheme")

    ## transport-protocol
    appendNewJavaType(data, "transport", nfmName + ".Transport")
    
    ## http-method
    appendNewJavaType(data, "http-method", nfmName + ".HttpMethod")

    ## update-interval
    appendNewJavaType(data, "update-interval", nfmName + ".UpdateInterval")

    appendNewJavaType(data, "nf-service.service-name", nfmName + ".ServiceName")

    if schemaName == "ericsson-sepp" or schemaName == "ericsson-scp":
        ## nf-service-name
        appendNewJavaType(data, "static-scp-instance.static-nf-service.name", nfmName + ".ServiceName")
        appendNewJavaType(data, "static-nf-instance.static-nf-service.name", nfmName + ".ServiceName")
        appendNewJavaType(data, "discovered-nf-service.name", nfmName + ".ServiceName")
        appendNewJavaType(data, "static-scp-instance.static-nf-service.name", nfmName + ".ServiceName")
        appendNewJavaType(data, "static-nf-instance.static-nf-service.name", nfmName + ".ServiceName")
        appendNewJavaType(data, "discovered-nf-service.name", nfmName + ".ServiceName")

        ## nf-service-status
        appendNewJavaType(data, "discovered-nf-service.status", nfmName + ".NfStatus")


def processCommonSeppScp(data, schemaName, commonName, glueName):

    ###################################################
    #  SEPP and gSCP related INTERFACES               #
    ###################################################

    if schemaName == "ericsson-scp" or schemaName == "ericsson-sepp":
        appendNewJavaInterfaces(data, "address", [glueName + ".IfAddress"])

        appendNewJavaInterfaces(data, "discovered-nf-instance", [glueName + ".IfDiscoveredNfInstance"])
        appendNewJavaInterfaces(data, "discovered-nf-service", [glueName + ".IfTypedNfService"])
        appendNewJavaInterfaces(data, "static-nf-service", [glueName + ".IfTypedNfService"])
        appendNewJavaInterfaces(data, "static-nf-instance", [glueName + ".IfStaticNfInstance", glueName + ".IfStaticNfInstance"])
        appendNewJavaInterfaces(data, "static-nf-instance-data", [glueName + ".IfStaticNfInstanceDatum"])
        appendNewJavaInterfaces(data, "static-scp-instance", [glueName + ".IfStaticNfInstance"])
        appendNewJavaInterfaces(data, "static-scp-instance-data", [glueName + ".IfStaticScpInstanceDatum"])

        appendNewJavaInterfaces(data, "failover-profile", [glueName + ".IfFailoverProfile"])
        appendNewJavaInterfaces(data, "message-data", ["com.ericsson.utilities.common.IfNamedListItem"])
        appendNewJavaInterfaces(data, "multiple-ip-endpoint", [glueName + ".IfMultipleIpEndpoint"])
        appendNewJavaInterfaces(data, "active-health-check", [glueName + ".IfActiveHealthCheck"])
        appendNewJavaInterfaces(data, "nf-instance", [glueName + ".IfNfInstance"])
        appendNewJavaInterfaces(data, "nrf-group", [glueName + ".IfNrfGroup"])
        appendNewJavaInterfaces(data, "nrf", [glueName + ".IfNrf"])
        appendNewJavaInterfaces(data, "nrf-service", [glueName + ".IfNrfService"])
        appendNewJavaInterfaces(data, "nf-pool", [glueName + ".IfNfPool"])
        appendNewJavaInterfaces(data, "nf-pool-discovery", [glueName + ".IfNfPoolDiscovery"])
        appendNewJavaInterfaces(data, "retry-condition", [glueName + ".IfRetryCondition"])
        appendNewJavaInterfaces(data, "routing-case", [glueName + ".IfRoutingCase"])
        appendNewJavaInterfaces(data, "service-address", [glueName + ".IfServiceAddress"])
        appendNewJavaInterfaces(data, "priority-group", [glueName + ".IfPriorityGroup"])
        appendNewJavaInterfaces(data, "temporary-blocking", [glueName + ".IfTemporaryBlocking"])
        appendNewJavaInterfaces(data, "pool-retry-budget", [glueName + ".IfPoolRetryBudget"])
        appendNewJavaInterfaces(data, "ingress-connection-profile", [glueName + ".IfIngressConnectionProfile"])
        appendNewJavaInterfaces(data, "egress-connection-profile", [glueName + ".IfEgressConnectionProfile"])

        appendNewJavaInterfaces(data, "routing-action", [glueName + ".IfRoutingAction"])
        appendNewJavaInterfaces(data, "routing-rule", [glueName + ".IfRoutingRule"])

        appendNewJavaInterfaces(data, "action-route-strict", [glueName + ".IfActionRouteBase", glueName + ".IfActionRouteTarget"])
        appendNewJavaInterfaces(data, "action-route-preferred", [glueName + ".IfActionRouteBase", glueName + ".IfActionRouteTarget"])
        appendNewJavaInterfaces(data, "action-route-round-robin", [glueName + ".IfActionRouteBase"])
        appendNewJavaInterfaces(data, "action-reject-message", [glueName + ".IfActionRejectMessageBase"])
        appendNewJavaInterfaces(data, "action-drop-message", [glueName + ".IfActionDropMessageBase"])
        appendNewJavaInterfaces(data, "action-log", [glueName + ".IfActionLogBase"])
        appendNewJavaInterfaces(data, "keep-authority-header", [glueName + ".IfKeepAuthorityHeader"])
        appendNewJavaInterfaces(data, "target-nf-pool", [glueName + ".IfTargetNfPool"])

        appendNewJavaInterfaces(data, "from-target-api-root-header", [glueName + ".IfFromTargetApiRootHeader"])
        appendNewJavaInterfaces(data, "from-authority-header", [glueName + ".IfFromAuthorityHeader"])

        appendNewExtendsJavaClass(data, "static-nf-service", glueName + ".NfServiceParams")
        appendNewExtendsJavaClass(data, "discovered-nf-service", glueName + ".NfServiceParams")

        appendNewJavaInterfaces(data, "request-screening-case", ["com.ericsson.utilities.common.IfNamedListItem"])
        appendNewJavaInterfaces(data, "response-screening-case", ["com.ericsson.utilities.common.IfNamedListItem"])
        
        appendNewJavaInterfaces(data, "routing-action", [glueName + ".IfRoutingAction"])
        appendNewJavaInterfaces(data, "routing-rule", [glueName + ".IfRoutingRule"])
        appendNewJavaInterfaces(data, "own-network", [glueName + ".IfNetwork"])
        appendNewJavaInterfaces(data, "local-rate-limit-profile", [glueName + ".IfLocalRateLimitProfile"])
        appendNewJavaInterfaces(data, "token-bucket", [glueName + ".IfTokenBucket"])
        appendNewJavaInterfaces(data, "add-response-header", [glueName + ".IfAddResponseHeader"])
        appendNewJavaInterfaces(data, "global-rate-limit-profile", [glueName + ".IfGlobalRateLimitProfile"])

        appendNewJavaInterfaces(data, "vtap", [glueName + ".IfVtap"])
        appendNewJavaInterfaces(data, "vtap-configuration", [glueName + ".IfVtapConfiguration"])
        appendNewJavaInterfaces(data, "proxy", [glueName + ".IfProxy"])
        appendNewJavaInterfaces(data, "ingress", [glueName + ".IfVtapIngress"])
        appendNewJavaInterfaces(data, "egress", [glueName + ".IfVtapEgress"])
        appendNewJavaInterfaces(data, "all-nf-pools", [glueName + ".IfAllNfPools"])
        
    if schemaName == "ericsson-scp":
        appendNewJavaInterfaces(data, "scp-function", [glueName + ".IfNfFunction"])
        appendNewJavaInterfaces(data, "slf-lookup-profile", ["com.ericsson.utilities.common.IfNamedListItem"])
        appendNewJavaType(data, "slf-lookup-profile.identity-type", commonName + ".IdentityType")
        appendNewJavaInterfaces(data, "action-route-remote-preferred", [glueName + ".IfActionRouteBase"])
        appendNewJavaInterfaces(data, "action-route-remote-round-robin", [glueName + ".IfActionRouteBase"])
        appendNewJavaType(data, "preserve-if-indirect-routing", commonName + ".PreserveIfIndirectRouting")
        appendNewJavaType(data, "global-rate-limit-profile.action-reject-message", "com.ericsson.sc.scp.model.RateLimitingActionRejectMessage")
        appendNewJavaType(data, "screening-action.action-reject-message", "com.ericsson.sc.scp.model.ActionRejectMessage")
        appendNewJavaType(data, "routing-action.action-reject-message", "com.ericsson.sc.scp.model.ActionRejectMessage")
        appendNewJavaType(data, "action-drop-message", "com.ericsson.sc.scp.model.ActionDropMessage")
        appendNewJavaType(data, "action-nf-discovery", "com.ericsson.sc.scp.model.ActionNfDiscovery")

    if schemaName == "ericsson-sepp":
        appendNewJavaInterfaces(data, "sepp-function", [glueName + ".IfNfFunction"])
        appendNewJavaInterfaces(data, "roaming-partner", ["com.ericsson.utilities.common.IfNamedListItem"])
        appendNewJavaInterfaces(data, "external-network", [glueName + ".IfNetwork"])
        appendNewJavaInterfaces(data, "asymmetric-key", [glueName + ".IfNamedListItem"])
        appendNewJavaInterfaces(data, "topology-hiding", [glueName + ".IfNamedListItem"])
        appendNewJavaType(data, "global-rate-limit-profile.action-reject-message", "com.ericsson.sc.sepp.model.RateLimitingActionRejectMessage")
        appendNewJavaType(data, "screening-action.action-reject-message", "com.ericsson.sc.sepp.model.ActionRejectMessage")
        appendNewJavaType(data, "action-drop-message", "com.ericsson.sc.sepp.model.ActionDropMessage")

        # FirewallProfile
        appendNewJavaInterfaces(data, "firewall-profile", [glueName + ".IfNamedListItem"])
        appendNewJavaType(data, "firewall-profile.request.validate-message-headers.allowed", "com.ericsson.sc.sepp.model.Allowed")
        appendNewJavaType(data, "firewall-profile.request.validate-message-headers.denied", "com.ericsson.sc.sepp.model.Denied")
        appendNewJavaType(data, "firewall-profile.request.validate-message-headers.action-forward-unmodified-message", "com.ericsson.sc.sepp.model.FirewallProfileActionForwardUnmodifiedMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-headers.action-forward-modified-message", "com.ericsson.sc.sepp.model.FirewallProfileActionForwardModifiedMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-headers.action-reject-message", "com.ericsson.sc.sepp.model.FirewallProfileActionRejectMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-headers.action-drop-message", "com.ericsson.sc.sepp.model.FirewallProfileActionDropMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-syntax.action-forward-unmodified-message", "com.ericsson.sc.sepp.model.FirewallProfileActionForwardUnmodifiedMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-syntax.action-reject-message", "com.ericsson.sc.sepp.model.FirewallProfileActionRejectMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-syntax.action-drop-message", "com.ericsson.sc.sepp.model.FirewallProfileActionDropMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-body-size.action-reject-message", "com.ericsson.sc.sepp.model.FirewallProfileActionRejectMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-body-size.action-drop-message", "com.ericsson.sc.sepp.model.FirewallProfileActionDropMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-leaves.action-reject-message", "com.ericsson.sc.sepp.model.FirewallProfileActionRejectMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-leaves.action-drop-message", "com.ericsson.sc.sepp.model.FirewallProfileActionDropMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-depth.action-reject-message", "com.ericsson.sc.sepp.model.FirewallProfileActionRejectMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-depth.action-drop-message", "com.ericsson.sc.sepp.model.FirewallProfileActionDropMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-message-headers", "com.ericsson.sc.sepp.model.RequestValidateMessageHeaders")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-syntax", "com.ericsson.sc.sepp.model.RequestValidateMessageJsonBodySyntax")
        appendNewJavaType(data, "firewall-profile.request.validate-message-body-size", "com.ericsson.sc.sepp.model.RequestValidateMessageBodySize")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-leaves", "com.ericsson.sc.sepp.model.RequestValidateMessageJsonBodyLeaves")
        appendNewJavaType(data, "firewall-profile.request.validate-message-json-body-depth", "com.ericsson.sc.sepp.model.RequestValidateMessageJsonBodyDepth")
        appendNewJavaType(data, "firewall-profile.response.validate-message-headers.allowed", "com.ericsson.sc.sepp.model.Allowed")
        appendNewJavaType(data, "firewall-profile.response.validate-message-headers.denied", "com.ericsson.sc.sepp.model.Denied")
        appendNewJavaType(data, "firewall-profile.response.validate-message-headers.action-forward-unmodified-message", "com.ericsson.sc.sepp.model.FirewallProfileActionForwardUnmodifiedMessage")
        appendNewJavaType(data, "firewall-profile.response.validate-message-headers.action-forward-modified-message", "com.ericsson.sc.sepp.model.FirewallProfileActionForwardModifiedMessage")
        appendNewJavaType(data, "firewall-profile.response.validate-message-headers.action-respond-with-error", "com.ericsson.sc.sepp.model.FirewallProfileActionRespondWithError")
        appendNewJavaType(data, "firewall-profile.response.validate-message-json-body-syntax.action-forward-unmodified-message", "com.ericsson.sc.sepp.model.FirewallProfileActionForwardUnmodifiedMessage")
        appendNewJavaType(data, "firewall-profile.response.validate-message-json-body-syntax.action-respond-with-error", "com.ericsson.sc.sepp.model.FirewallProfileActionRespondWithError")
        appendNewJavaType(data, "firewall-profile.response.validate-message-body-size.action-respond-with-error", "com.ericsson.sc.sepp.model.FirewallProfileActionRespondWithError")
        appendNewJavaType(data, "firewall-profile.response.validate-message-json-body-leaves.action-respond-with-error", "com.ericsson.sc.sepp.model.FirewallProfileActionRespondWithError")
        appendNewJavaType(data, "firewall-profile.response.validate-message-json-body-depth.action-respond-with-error", "com.ericsson.sc.sepp.model.FirewallProfileActionRespondWithError")
        appendNewJavaType(data, "firewall-profile.response.validate-message-headers", "com.ericsson.sc.sepp.model.ResponseValidateMessageHeaders")
        appendNewJavaType(data, "firewall-profile.response.validate-message-json-body-syntax", "com.ericsson.sc.sepp.model.ResponseValidateMessageJsonBodySyntax")
        appendNewJavaType(data, "firewall-profile.response.validate-message-body-size", "com.ericsson.sc.sepp.model.ResponseValidateMessageBodySize")
        appendNewJavaType(data, "firewall-profile.response.validate-message-json-body-leaves", "com.ericsson.sc.sepp.model.ResponseValidateMessageJsonBodyLeaves")
        appendNewJavaType(data, "firewall-profile.response.validate-message-json-body-depth", "com.ericsson.sc.sepp.model.ResponseValidateMessageJsonBodyDepth")
        appendNewJavaType(data, "firewall-profile.request.validate-service-operation", "com.ericsson.sc.sepp.model.RequestValidateServiceOperation")
        appendNewJavaType(data, "firewall-profile.request.validate-service-operation.action-reject-message", "com.ericsson.sc.sepp.model.FirewallProfileActionRejectMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-service-operation.action-drop-message", "com.ericsson.sc.sepp.model.FirewallProfileActionDropMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-service-operation.action-forward-unmodified-message", "com.ericsson.sc.sepp.model.FirewallProfileActionForwardUnmodifiedMessage")
        appendNewJavaType(data, "firewall-profile.request.validate-service-operation.additional-allowed-operations.http-method", "com.ericsson.sc.nfm.model.HttpMethod")
        appendNewJavaType(data, "firewall-profile.request.validate-service-operation.removed-default-operations.http-method", "com.ericsson.sc.nfm.model.HttpMethod")
        

def processBsf(data, schemaName, commonName, glueName):
    appendNewJavaInterfaces(data, "service-address", [glueName + ".IfServiceAddress"])
    appendNewJavaInterfaces(data, "nf-instance", [glueName + ".IfGenericNfInstance"])
    appendNewJavaInterfaces(data, "nrf-group", [glueName + ".IfNrfGroup"])
    appendNewJavaInterfaces(data, "nrf-service", [glueName + ".IfNrfService"])
    appendNewJavaInterfaces(data, "bsf-function", [glueName + ".IfNfFunction"])

def main():

    parser = argparse.ArgumentParser()
    parser.add_argument('--packageName', 
                        help='The NF package name')
    parser.add_argument('--schemaName', help='The schema name')
    parser.add_argument('--commonPkgName', help='The common package name')
    parser.add_argument('--gluePkgName', help='The package name for the glue code (ie. interfaces)')
    parser.add_argument('--nfmPkgName', help='The package name for the Nrf-R16')
    parser.add_argument('--output', help='The output folder')

    args = parser.parse_args()

    pkgName = args.packageName
    schemaName = args.schemaName
    folder = args.output
    commonName = args.commonPkgName
    glueName = args.gluePkgName
    nfmName = args.nfmPkgName

    readFilepath = folder + '/' + schemaName + '.json'
    writeFilepath = folder + '/' + schemaName + '-processed.json'

    # open json schema
    with open(readFilepath, 'r') as f:
        data = json.load(f, object_pairs_hook=collections.OrderedDict)

    ###################################################
    #  BSF                                            #
    ###################################################
    if schemaName == "ericsson-bsf":
        processBsf(data, schemaName, commonName, glueName)

    ###################################################
    #  SEPP and gSCP                                  #
    ###################################################
    if schemaName == "ericsson-sepp" or schemaName == "ericsson-scp":
        processCommonSeppScp(data, schemaName, commonName, glueName)

    ###################################################
    #  NFM                                            #
    ###################################################

    processNfm(data, nfmName, schemaName)

    # write the new json schema
    with open(writeFilepath, 'w') as fw:
        json.dump(data, fw, indent = 4)

if __name__ == "__main__":
    main()

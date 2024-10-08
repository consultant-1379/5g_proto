/*
 * Nnef_PFDmanagement Service API
 * Packet Flow Description Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29551.nnef.pfdmanagement;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents the content of a PFD for an application identifier.
 */
@ApiModel(description = "Represents the content of a PFD for an application identifier.")
@JsonPropertyOrder({ PfdContent.JSON_PROPERTY_PFD_ID,
                     PfdContent.JSON_PROPERTY_FLOW_DESCRIPTIONS,
                     PfdContent.JSON_PROPERTY_URLS,
                     PfdContent.JSON_PROPERTY_DOMAIN_NAMES,
                     PfdContent.JSON_PROPERTY_DN_PROTOCOL })
public class PfdContent
{
    public static final String JSON_PROPERTY_PFD_ID = "pfdId";
    private String pfdId;

    public static final String JSON_PROPERTY_FLOW_DESCRIPTIONS = "flowDescriptions";
    private List<String> flowDescriptions = null;

    public static final String JSON_PROPERTY_URLS = "urls";
    private List<String> urls = null;

    public static final String JSON_PROPERTY_DOMAIN_NAMES = "domainNames";
    private List<String> domainNames = null;

    public static final String JSON_PROPERTY_DN_PROTOCOL = "dnProtocol";
    private String dnProtocol;

    public PfdContent()
    {
    }

    public PfdContent pfdId(String pfdId)
    {

        this.pfdId = pfdId;
        return this;
    }

    /**
     * Identifies a PDF of an application identifier.
     * 
     * @return pfdId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies a PDF of an application identifier.")
    @JsonProperty(JSON_PROPERTY_PFD_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPfdId()
    {
        return pfdId;
    }

    @JsonProperty(JSON_PROPERTY_PFD_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPfdId(String pfdId)
    {
        this.pfdId = pfdId;
    }

    public PfdContent flowDescriptions(List<String> flowDescriptions)
    {

        this.flowDescriptions = flowDescriptions;
        return this;
    }

    public PfdContent addFlowDescriptionsItem(String flowDescriptionsItem)
    {
        if (this.flowDescriptions == null)
        {
            this.flowDescriptions = new ArrayList<>();
        }
        this.flowDescriptions.add(flowDescriptionsItem);
        return this;
    }

    /**
     * Represents a 3-tuple with protocol, server ip and server port for UL/DL
     * application traffic.
     * 
     * @return flowDescriptions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents a 3-tuple with protocol, server ip and server port for UL/DL application traffic. ")
    @JsonProperty(JSON_PROPERTY_FLOW_DESCRIPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getFlowDescriptions()
    {
        return flowDescriptions;
    }

    @JsonProperty(JSON_PROPERTY_FLOW_DESCRIPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFlowDescriptions(List<String> flowDescriptions)
    {
        this.flowDescriptions = flowDescriptions;
    }

    public PfdContent urls(List<String> urls)
    {

        this.urls = urls;
        return this;
    }

    public PfdContent addUrlsItem(String urlsItem)
    {
        if (this.urls == null)
        {
            this.urls = new ArrayList<>();
        }
        this.urls.add(urlsItem);
        return this;
    }

    /**
     * Indicates a URL or a regular expression which is used to match the
     * significant parts of the URL.
     * 
     * @return urls
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates a URL or a regular expression which is used to match the significant parts of the URL. ")
    @JsonProperty(JSON_PROPERTY_URLS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getUrls()
    {
        return urls;
    }

    @JsonProperty(JSON_PROPERTY_URLS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUrls(List<String> urls)
    {
        this.urls = urls;
    }

    public PfdContent domainNames(List<String> domainNames)
    {

        this.domainNames = domainNames;
        return this;
    }

    public PfdContent addDomainNamesItem(String domainNamesItem)
    {
        if (this.domainNames == null)
        {
            this.domainNames = new ArrayList<>();
        }
        this.domainNames.add(domainNamesItem);
        return this;
    }

    /**
     * Indicates an FQDN or a regular expression as a domain name matching criteria.
     * 
     * @return domainNames
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates an FQDN or a regular expression as a domain name matching criteria.")
    @JsonProperty(JSON_PROPERTY_DOMAIN_NAMES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDomainNames()
    {
        return domainNames;
    }

    @JsonProperty(JSON_PROPERTY_DOMAIN_NAMES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDomainNames(List<String> domainNames)
    {
        this.domainNames = domainNames;
    }

    public PfdContent dnProtocol(String dnProtocol)
    {

        this.dnProtocol = dnProtocol;
        return this;
    }

    /**
     * Possible values are - DNS_QNAME: Identifies the DNS protocol and the question
     * name in DNS query. - TLS_SNI: Identifies the Server Name Indication in TLS
     * ClientHello message. - TLS_SAN: Identifies the Subject Alternative Name in
     * TLS ServerCertificate message. - TLS_SCN: Identifies the Subject Common Name
     * in TLS ServerCertificate message.
     * 
     * @return dnProtocol
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are - DNS_QNAME: Identifies the DNS protocol and the question name in DNS query. - TLS_SNI: Identifies the Server Name Indication in TLS ClientHello message. - TLS_SAN: Identifies the Subject Alternative Name in TLS ServerCertificate message. - TLS_SCN: Identifies the Subject Common Name in TLS ServerCertificate message. ")
    @JsonProperty(JSON_PROPERTY_DN_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDnProtocol()
    {
        return dnProtocol;
    }

    @JsonProperty(JSON_PROPERTY_DN_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnProtocol(String dnProtocol)
    {
        this.dnProtocol = dnProtocol;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        PfdContent pfdContent = (PfdContent) o;
        return Objects.equals(this.pfdId, pfdContent.pfdId) && Objects.equals(this.flowDescriptions, pfdContent.flowDescriptions)
               && Objects.equals(this.urls, pfdContent.urls) && Objects.equals(this.domainNames, pfdContent.domainNames)
               && Objects.equals(this.dnProtocol, pfdContent.dnProtocol);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pfdId, flowDescriptions, urls, domainNames, dnProtocol);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PfdContent {\n");
        sb.append("    pfdId: ").append(toIndentedString(pfdId)).append("\n");
        sb.append("    flowDescriptions: ").append(toIndentedString(flowDescriptions)).append("\n");
        sb.append("    urls: ").append(toIndentedString(urls)).append("\n");
        sb.append("    domainNames: ").append(toIndentedString(domainNames)).append("\n");
        sb.append("    dnProtocol: ").append(toIndentedString(dnProtocol)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o)
    {
        if (o == null)
        {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

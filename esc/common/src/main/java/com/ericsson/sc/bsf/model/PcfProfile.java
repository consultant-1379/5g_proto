
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.esc.services.cm.model.Snssai;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "diameter-host",
                     "snssai",
                     "fqdn",
                     "dnn",
                     "diameter-realm",
                     "serving-ipv4-addr-ranges",
                     "ip-domain",
                     "nf-id",
                     "supi",
                     "gpsi",
                     "endpoint",
                     "serving-ipv6-prefix-ranges",
                     "mac-addr-48",
                     "name" })
public class PcfProfile
{

    @JsonProperty("diameter-host")
    private String diameterHost;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("snssai")
    private Snssai snssai;
    @JsonProperty("fqdn")
    private String fqdn;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dnn")
    private String dnn;
    @JsonProperty("diameter-realm")
    private String diameterRealm;
    @JsonProperty("serving-ipv4-addr-ranges")
    private List<ServingIpv4AddrRange> servingIpv4AddrRanges = new ArrayList<ServingIpv4AddrRange>();
    @JsonProperty("ip-domain")
    private String ipDomain;
    @JsonProperty("nf-id")
    private String nfId;
    @JsonProperty("supi")
    private String supi;
    @JsonProperty("gpsi")
    private String gpsi;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endpoint")
    private List<Endpoint> endpoint = new ArrayList<Endpoint>();
    @JsonProperty("serving-ipv6-prefix-ranges")
    private List<ServingIpv6PrefixRange> servingIpv6PrefixRanges = new ArrayList<ServingIpv6PrefixRange>();
    @JsonProperty("mac-addr-48")
    private String macAddr48;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;

    @JsonProperty("diameter-host")
    public String getDiameterHost()
    {
        return diameterHost;
    }

    @JsonProperty("diameter-host")
    public void setDiameterHost(String diameterHost)
    {
        this.diameterHost = diameterHost;
    }

    public PcfProfile withDiameterHost(String diameterHost)
    {
        this.diameterHost = diameterHost;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("snssai")
    public Snssai getSnssai()
    {
        return snssai;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("snssai")
    public void setSnssai(Snssai snssai)
    {
        this.snssai = snssai;
    }

    public PcfProfile withSnssai(Snssai snssai)
    {
        this.snssai = snssai;
        return this;
    }

    @JsonProperty("fqdn")
    public String getFqdn()
    {
        return fqdn;
    }

    @JsonProperty("fqdn")
    public void setFqdn(String fqdn)
    {
        this.fqdn = fqdn;
    }

    public PcfProfile withFqdn(String fqdn)
    {
        this.fqdn = fqdn;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dnn")
    public String getDnn()
    {
        return dnn;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dnn")
    public void setDnn(String dnn)
    {
        this.dnn = dnn;
    }

    public PcfProfile withDnn(String dnn)
    {
        this.dnn = dnn;
        return this;
    }

    @JsonProperty("diameter-realm")
    public String getDiameterRealm()
    {
        return diameterRealm;
    }

    @JsonProperty("diameter-realm")
    public void setDiameterRealm(String diameterRealm)
    {
        this.diameterRealm = diameterRealm;
    }

    public PcfProfile withDiameterRealm(String diameterRealm)
    {
        this.diameterRealm = diameterRealm;
        return this;
    }

    @JsonProperty("serving-ipv4-addr-ranges")
    public List<ServingIpv4AddrRange> getServingIpv4AddrRanges()
    {
        return servingIpv4AddrRanges;
    }

    @JsonProperty("serving-ipv4-addr-ranges")
    public void setServingIpv4AddrRanges(List<ServingIpv4AddrRange> servingIpv4AddrRanges)
    {
        this.servingIpv4AddrRanges = servingIpv4AddrRanges;
    }

    public PcfProfile withServingIpv4AddrRanges(List<ServingIpv4AddrRange> servingIpv4AddrRanges)
    {
        this.servingIpv4AddrRanges = servingIpv4AddrRanges;
        return this;
    }

    @JsonProperty("ip-domain")
    public String getIpDomain()
    {
        return ipDomain;
    }

    @JsonProperty("ip-domain")
    public void setIpDomain(String ipDomain)
    {
        this.ipDomain = ipDomain;
    }

    public PcfProfile withIpDomain(String ipDomain)
    {
        this.ipDomain = ipDomain;
        return this;
    }

    @JsonProperty("nf-id")
    public String getNfId()
    {
        return nfId;
    }

    @JsonProperty("nf-id")
    public void setNfId(String nfId)
    {
        this.nfId = nfId;
    }

    public PcfProfile withNfId(String nfId)
    {
        this.nfId = nfId;
        return this;
    }

    @JsonProperty("supi")
    public String getSupi()
    {
        return supi;
    }

    @JsonProperty("supi")
    public void setSupi(String supi)
    {
        this.supi = supi;
    }

    public PcfProfile withSupi(String supi)
    {
        this.supi = supi;
        return this;
    }

    @JsonProperty("gpsi")
    public String getGpsi()
    {
        return gpsi;
    }

    @JsonProperty("gpsi")
    public void setGpsi(String gpsi)
    {
        this.gpsi = gpsi;
    }

    public PcfProfile withGpsi(String gpsi)
    {
        this.gpsi = gpsi;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endpoint")
    public List<Endpoint> getEndpoint()
    {
        return endpoint;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endpoint")
    public void setEndpoint(List<Endpoint> endpoint)
    {
        this.endpoint = endpoint;
    }

    public PcfProfile withEndpoint(List<Endpoint> endpoint)
    {
        this.endpoint = endpoint;
        return this;
    }

    @JsonProperty("serving-ipv6-prefix-ranges")
    public List<ServingIpv6PrefixRange> getServingIpv6PrefixRanges()
    {
        return servingIpv6PrefixRanges;
    }

    @JsonProperty("serving-ipv6-prefix-ranges")
    public void setServingIpv6PrefixRanges(List<ServingIpv6PrefixRange> servingIpv6PrefixRanges)
    {
        this.servingIpv6PrefixRanges = servingIpv6PrefixRanges;
    }

    public PcfProfile withServingIpv6PrefixRanges(List<ServingIpv6PrefixRange> servingIpv6PrefixRanges)
    {
        this.servingIpv6PrefixRanges = servingIpv6PrefixRanges;
        return this;
    }

    @JsonProperty("mac-addr-48")
    public String getMacAddr48()
    {
        return macAddr48;
    }

    @JsonProperty("mac-addr-48")
    public void setMacAddr48(String macAddr48)
    {
        this.macAddr48 = macAddr48;
    }

    public PcfProfile withMacAddr48(String macAddr48)
    {
        this.macAddr48 = macAddr48;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public PcfProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PcfProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("diameterHost");
        sb.append('=');
        sb.append(((this.diameterHost == null) ? "<null>" : this.diameterHost));
        sb.append(',');
        sb.append("snssai");
        sb.append('=');
        sb.append(((this.snssai == null) ? "<null>" : this.snssai));
        sb.append(',');
        sb.append("fqdn");
        sb.append('=');
        sb.append(((this.fqdn == null) ? "<null>" : this.fqdn));
        sb.append(',');
        sb.append("dnn");
        sb.append('=');
        sb.append(((this.dnn == null) ? "<null>" : this.dnn));
        sb.append(',');
        sb.append("diameterRealm");
        sb.append('=');
        sb.append(((this.diameterRealm == null) ? "<null>" : this.diameterRealm));
        sb.append(',');
        sb.append("servingIpv4AddrRanges");
        sb.append('=');
        sb.append(((this.servingIpv4AddrRanges == null) ? "<null>" : this.servingIpv4AddrRanges));
        sb.append(',');
        sb.append("ipDomain");
        sb.append('=');
        sb.append(((this.ipDomain == null) ? "<null>" : this.ipDomain));
        sb.append(',');
        sb.append("nfId");
        sb.append('=');
        sb.append(((this.nfId == null) ? "<null>" : this.nfId));
        sb.append(',');
        sb.append("supi");
        sb.append('=');
        sb.append(((this.supi == null) ? "<null>" : this.supi));
        sb.append(',');
        sb.append("gpsi");
        sb.append('=');
        sb.append(((this.gpsi == null) ? "<null>" : this.gpsi));
        sb.append(',');
        sb.append("endpoint");
        sb.append('=');
        sb.append(((this.endpoint == null) ? "<null>" : this.endpoint));
        sb.append(',');
        sb.append("servingIpv6PrefixRanges");
        sb.append('=');
        sb.append(((this.servingIpv6PrefixRanges == null) ? "<null>" : this.servingIpv6PrefixRanges));
        sb.append(',');
        sb.append("macAddr48");
        sb.append('=');
        sb.append(((this.macAddr48 == null) ? "<null>" : this.macAddr48));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.nfId == null) ? 0 : this.nfId.hashCode()));
        result = ((result * 31) + ((this.diameterHost == null) ? 0 : this.diameterHost.hashCode()));
        result = ((result * 31) + ((this.snssai == null) ? 0 : this.snssai.hashCode()));
        result = ((result * 31) + ((this.fqdn == null) ? 0 : this.fqdn.hashCode()));
        result = ((result * 31) + ((this.dnn == null) ? 0 : this.dnn.hashCode()));
        result = ((result * 31) + ((this.ipDomain == null) ? 0 : this.ipDomain.hashCode()));
        result = ((result * 31) + ((this.servingIpv6PrefixRanges == null) ? 0 : this.servingIpv6PrefixRanges.hashCode()));
        result = ((result * 31) + ((this.supi == null) ? 0 : this.supi.hashCode()));
        result = ((result * 31) + ((this.gpsi == null) ? 0 : this.gpsi.hashCode()));
        result = ((result * 31) + ((this.endpoint == null) ? 0 : this.endpoint.hashCode()));
        result = ((result * 31) + ((this.diameterRealm == null) ? 0 : this.diameterRealm.hashCode()));
        result = ((result * 31) + ((this.servingIpv4AddrRanges == null) ? 0 : this.servingIpv4AddrRanges.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.macAddr48 == null) ? 0 : this.macAddr48.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PcfProfile) == false)
        {
            return false;
        }
        PcfProfile rhs = ((PcfProfile) other);
        return (((((((((((((((this.nfId == rhs.nfId) || ((this.nfId != null) && this.nfId.equals(rhs.nfId)))
                            && ((this.diameterHost == rhs.diameterHost) || ((this.diameterHost != null) && this.diameterHost.equals(rhs.diameterHost))))
                           && ((this.snssai == rhs.snssai) || ((this.snssai != null) && this.snssai.equals(rhs.snssai))))
                          && ((this.fqdn == rhs.fqdn) || ((this.fqdn != null) && this.fqdn.equals(rhs.fqdn))))
                         && ((this.dnn == rhs.dnn) || ((this.dnn != null) && this.dnn.equals(rhs.dnn))))
                        && ((this.ipDomain == rhs.ipDomain) || ((this.ipDomain != null) && this.ipDomain.equals(rhs.ipDomain))))
                       && ((this.servingIpv6PrefixRanges == rhs.servingIpv6PrefixRanges)
                           || ((this.servingIpv6PrefixRanges != null) && this.servingIpv6PrefixRanges.equals(rhs.servingIpv6PrefixRanges))))
                      && ((this.supi == rhs.supi) || ((this.supi != null) && this.supi.equals(rhs.supi))))
                     && ((this.gpsi == rhs.gpsi) || ((this.gpsi != null) && this.gpsi.equals(rhs.gpsi))))
                    && ((this.endpoint == rhs.endpoint) || ((this.endpoint != null) && this.endpoint.equals(rhs.endpoint))))
                   && ((this.diameterRealm == rhs.diameterRealm) || ((this.diameterRealm != null) && this.diameterRealm.equals(rhs.diameterRealm))))
                  && ((this.servingIpv4AddrRanges == rhs.servingIpv4AddrRanges)
                      || ((this.servingIpv4AddrRanges != null) && this.servingIpv4AddrRanges.equals(rhs.servingIpv4AddrRanges))))
                 && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                && ((this.macAddr48 == rhs.macAddr48) || ((this.macAddr48 != null) && this.macAddr48.equals(rhs.macAddr48))));
    }

}

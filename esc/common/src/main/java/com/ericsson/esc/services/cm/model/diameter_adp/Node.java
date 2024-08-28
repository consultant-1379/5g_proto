
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "origin-realm", "origin-host", "product-name", "vendor-id", "firmware-revision", "host-ip-address", "user-label", "tls-profile" })
public class Node
{

    /**
     * Used to specify the key of the node instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the node instance.")
    private String id;
    /**
     * Used to specify the origin realm of the Diameter Node represented by the node
     * instance. The provided value is to be expressed by complying to the Diameter
     * Identity (https://tools.ietf.org/html/rfc6733#section-4.3.1) data type
     * expression rules as defined by the Diameter Base Protocol. The provided value
     * is used to construct a Origin-Realm AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.4) which is placed in
     * capability exchange messages (CER/CEA messages) during related Own Diameter
     * Node linked peer connection establishment. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections linked to related Own
     * Diameter Node are dropped and reestablished with updated information.
     * (Required)
     * 
     */
    @JsonProperty("origin-realm")
    @JsonPropertyDescription("Used to specify the origin realm of the Diameter Node represented by the node instance. The provided value is to be expressed by complying to the Diameter Identity (https://tools.ietf.org/html/rfc6733#section-4.3.1) data type expression rules as defined by the Diameter Base Protocol. The provided value is used to construct a Origin-Realm AVP (https://tools.ietf.org/html/rfc6733#section-6.4) which is placed in capability exchange messages (CER/CEA messages) during related Own Diameter Node linked peer connection establishment. Update Apply: Immediate. Update Effect: All established diameter peer connections linked to related Own Diameter Node are dropped and reestablished with updated information.")
    private String originRealm;
    /**
     * Used to specify the origin host of the Diameter Node represented by the node
     * instance. The provided value is to be expressed by complying to the Diameter
     * Identity (https://tools.ietf.org/html/rfc6733#section-4.3.1) data type
     * expression rules as defined by the Diameter Base Protocol. The provided value
     * is used to construct a Origin-Host AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.3) which is placed in
     * capability exchange messages (CER/CEA messages) during related AAA Service
     * linked peer connection setup. The provided attribute value must be unique in
     * the context of the related origin realm. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections linked to related Own
     * Diameter Node are dropped and reestablished with updated information.
     * (Required)
     * 
     */
    @JsonProperty("origin-host")
    @JsonPropertyDescription("Used to specify the origin host of the Diameter Node represented by the node instance. The provided value is to be expressed by complying to the Diameter Identity (https://tools.ietf.org/html/rfc6733#section-4.3.1) data type expression rules as defined by the Diameter Base Protocol. The provided value is used to construct a Origin-Host AVP (https://tools.ietf.org/html/rfc6733#section-6.3) which is placed in capability exchange messages (CER/CEA messages) during related AAA Service linked peer connection setup. The provided attribute value must be unique in the context of the related origin realm. Update Apply: Immediate. Update Effect: All established diameter peer connections linked to related Own Diameter Node are dropped and reestablished with updated information.")
    private String originHost;
    /**
     * Used to specify the name of the product behind the Diameter Node implementing
     * different AAA Services by using the diameter stack (for example, EIR, SAPC,
     * IpWorks, MTAS, CSCF, HSS). The provided value is used to construct a
     * Product-Name AVP (https://tools.ietf.org/html/rfc6733#section-5.3.7) placed
     * in related capability exchange messages (CER/CEA messages) during peer
     * connection setup. The provided product name should remain constant across
     * firmware revisions for the same product (see also firmware-revision). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * linked to related Own Diameter Node are dropped and reestablished with
     * updated information. (Required)
     * 
     */
    @JsonProperty("product-name")
    @JsonPropertyDescription("Used to specify the name of the product behind the Diameter Node implementing different AAA Services by using the diameter stack (for example, EIR, SAPC, IpWorks, MTAS, CSCF, HSS). The provided value is used to construct a Product-Name AVP (https://tools.ietf.org/html/rfc6733#section-5.3.7) placed in related capability exchange messages (CER/CEA messages) during peer connection setup. The provided product name should remain constant across firmware revisions for the same product (see also firmware-revision). Update Apply: Immediate. Update Effect: All established diameter peer connections linked to related Own Diameter Node are dropped and reestablished with updated information.")
    private String productName;
    /**
     * Used to specify the identity of the vendor implementing the product specified
     * for product-name. The should take as value an IANA allocated SMI Network
     * Management Private Enterprise Code (see
     * https://www.iana.org/assignments/enterprise-integers/enterprise-integers )
     * assigned for the vendor implementing the product specified for product-name.
     * Unless the product developer center is not registered with own vendor
     * identity one should use the value 193 assigned to Ericsson AB. The provided
     * value is used to construct a Vendor-Id AVP
     * (https://tools.ietf.org/html/rfc6733#section-5.3.3) placed in related
     * capability exchange messages (CER/CEA messages) during peer connection setup.
     * Update Apply: Immediate. Update Effect: All established diameter peer
     * connections linked to related Own Diameter Node are dropped and reestablished
     * with updated information.
     * 
     */
    @JsonProperty("vendor-id")
    @JsonPropertyDescription("Used to specify the identity of the vendor implementing the product specified for product-name. The should take as value an IANA allocated SMI Network Management Private Enterprise Code (see https://www.iana.org/assignments/enterprise-integers/enterprise-integers ) assigned for the vendor implementing the product specified for product-name. Unless the product developer center is not registered with own vendor identity one should use the value 193 assigned to Ericsson AB. The provided value is used to construct a Vendor-Id AVP (https://tools.ietf.org/html/rfc6733#section-5.3.3) placed in related capability exchange messages (CER/CEA messages) during peer connection setup. Update Apply: Immediate. Update Effect: All established diameter peer connections linked to related Own Diameter Node are dropped and reestablished with updated information.")
    private Long vendorId = 193L;
    /**
     * Used to specify the revision of the software product specified for
     * product-name. If there is an value provided it is used to construct a
     * Firmware-Revision AVP (https://tools.ietf.org/html/rfc6733#section-5.3.4)
     * placed in related capability exchange messages (CER/CEA messages) during peer
     * connection setup. Update Apply: Immediate. Update Effect: All established
     * diameter peer connections linked to related Own Diameter Node are dropped and
     * reestablished with updated information.
     * 
     */
    @JsonProperty("firmware-revision")
    @JsonPropertyDescription("Used to specify the revision of the software product specified for product-name. If there is an value provided it is used to construct a Firmware-Revision AVP (https://tools.ietf.org/html/rfc6733#section-5.3.4) placed in related capability exchange messages (CER/CEA messages) during peer connection setup. Update Apply: Immediate. Update Effect: All established diameter peer connections linked to related Own Diameter Node are dropped and reestablished with updated information.")
    private Long firmwareRevision;
    /**
     * Used to specify the list of IP addresses (a list of IPv4 and/or IPv6
     * addresses) that can be used by a Diameter Peer to connect to the Own Diameter
     * Node. The IP addresses specified shall visible for related Diameter Peers
     * (for instance the IP address of external IP load balancer behind which the
     * Own Diameter Node is placed). The provided values are used to construct
     * relevant Host-IP-Address AVP
     * (https://tools.ietf.org/html/rfc6733#section-5.3.5) which is placed in
     * capability exchange messages (CER/CEA messages) during related AAA Service
     * linked peer connection setup. If no value is provided to this the
     * Host-IP-Address AVP is constructed by using the IP addresses provided for the
     * Own Diameter Node related Local Endpoints (see local-endpoint). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections linked to
     * related Own Diameter Node are dropped and reestablished with updated
     * information.
     * 
     */
    @JsonProperty("host-ip-address")
    @JsonPropertyDescription("Used to specify the list of IP addresses (a list of IPv4 and/or IPv6 addresses) that can be used by a Diameter Peer to connect to the Own Diameter Node. The IP addresses specified shall visible for related Diameter Peers (for instance the IP address of external IP load balancer behind which the Own Diameter Node is placed). The provided values are used to construct relevant Host-IP-Address AVP (https://tools.ietf.org/html/rfc6733#section-5.3.5) which is placed in capability exchange messages (CER/CEA messages) during related AAA Service linked peer connection setup. If no value is provided to this the Host-IP-Address AVP is constructed by using the IP addresses provided for the Own Diameter Node related Local Endpoints (see local-endpoint). Update Apply: Immediate. Update Effect: All established diameter peer connections linked to related Own Diameter Node are dropped and reestablished with updated information.")
    private List<String> hostIpAddress = new ArrayList<String>();
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * TLS profile to be associated with local-endpoints of the node. Properties of
     * tls-profile are used when securing connections for TCP transports with TLS
     * 1.2 or TLS 1.3.
     * 
     */
    @JsonProperty("tls-profile")
    @JsonPropertyDescription("TLS profile to be associated with local-endpoints of the node. Properties of tls-profile are used when securing connections for TCP transports with TLS 1.2 or TLS 1.3.")
    private List<TlsProfile> tlsProfile = new ArrayList<TlsProfile>();

    /**
     * Used to specify the key of the node instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the node instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public Node withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to specify the origin realm of the Diameter Node represented by the node
     * instance. The provided value is to be expressed by complying to the Diameter
     * Identity (https://tools.ietf.org/html/rfc6733#section-4.3.1) data type
     * expression rules as defined by the Diameter Base Protocol. The provided value
     * is used to construct a Origin-Realm AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.4) which is placed in
     * capability exchange messages (CER/CEA messages) during related Own Diameter
     * Node linked peer connection establishment. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections linked to related Own
     * Diameter Node are dropped and reestablished with updated information.
     * (Required)
     * 
     */
    @JsonProperty("origin-realm")
    public String getOriginRealm()
    {
        return originRealm;
    }

    /**
     * Used to specify the origin realm of the Diameter Node represented by the node
     * instance. The provided value is to be expressed by complying to the Diameter
     * Identity (https://tools.ietf.org/html/rfc6733#section-4.3.1) data type
     * expression rules as defined by the Diameter Base Protocol. The provided value
     * is used to construct a Origin-Realm AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.4) which is placed in
     * capability exchange messages (CER/CEA messages) during related Own Diameter
     * Node linked peer connection establishment. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections linked to related Own
     * Diameter Node are dropped and reestablished with updated information.
     * (Required)
     * 
     */
    @JsonProperty("origin-realm")
    public void setOriginRealm(String originRealm)
    {
        this.originRealm = originRealm;
    }

    public Node withOriginRealm(String originRealm)
    {
        this.originRealm = originRealm;
        return this;
    }

    /**
     * Used to specify the origin host of the Diameter Node represented by the node
     * instance. The provided value is to be expressed by complying to the Diameter
     * Identity (https://tools.ietf.org/html/rfc6733#section-4.3.1) data type
     * expression rules as defined by the Diameter Base Protocol. The provided value
     * is used to construct a Origin-Host AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.3) which is placed in
     * capability exchange messages (CER/CEA messages) during related AAA Service
     * linked peer connection setup. The provided attribute value must be unique in
     * the context of the related origin realm. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections linked to related Own
     * Diameter Node are dropped and reestablished with updated information.
     * (Required)
     * 
     */
    @JsonProperty("origin-host")
    public String getOriginHost()
    {
        return originHost;
    }

    /**
     * Used to specify the origin host of the Diameter Node represented by the node
     * instance. The provided value is to be expressed by complying to the Diameter
     * Identity (https://tools.ietf.org/html/rfc6733#section-4.3.1) data type
     * expression rules as defined by the Diameter Base Protocol. The provided value
     * is used to construct a Origin-Host AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.3) which is placed in
     * capability exchange messages (CER/CEA messages) during related AAA Service
     * linked peer connection setup. The provided attribute value must be unique in
     * the context of the related origin realm. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections linked to related Own
     * Diameter Node are dropped and reestablished with updated information.
     * (Required)
     * 
     */
    @JsonProperty("origin-host")
    public void setOriginHost(String originHost)
    {
        this.originHost = originHost;
    }

    public Node withOriginHost(String originHost)
    {
        this.originHost = originHost;
        return this;
    }

    /**
     * Used to specify the name of the product behind the Diameter Node implementing
     * different AAA Services by using the diameter stack (for example, EIR, SAPC,
     * IpWorks, MTAS, CSCF, HSS). The provided value is used to construct a
     * Product-Name AVP (https://tools.ietf.org/html/rfc6733#section-5.3.7) placed
     * in related capability exchange messages (CER/CEA messages) during peer
     * connection setup. The provided product name should remain constant across
     * firmware revisions for the same product (see also firmware-revision). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * linked to related Own Diameter Node are dropped and reestablished with
     * updated information. (Required)
     * 
     */
    @JsonProperty("product-name")
    public String getProductName()
    {
        return productName;
    }

    /**
     * Used to specify the name of the product behind the Diameter Node implementing
     * different AAA Services by using the diameter stack (for example, EIR, SAPC,
     * IpWorks, MTAS, CSCF, HSS). The provided value is used to construct a
     * Product-Name AVP (https://tools.ietf.org/html/rfc6733#section-5.3.7) placed
     * in related capability exchange messages (CER/CEA messages) during peer
     * connection setup. The provided product name should remain constant across
     * firmware revisions for the same product (see also firmware-revision). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * linked to related Own Diameter Node are dropped and reestablished with
     * updated information. (Required)
     * 
     */
    @JsonProperty("product-name")
    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public Node withProductName(String productName)
    {
        this.productName = productName;
        return this;
    }

    /**
     * Used to specify the identity of the vendor implementing the product specified
     * for product-name. The should take as value an IANA allocated SMI Network
     * Management Private Enterprise Code (see
     * https://www.iana.org/assignments/enterprise-integers/enterprise-integers )
     * assigned for the vendor implementing the product specified for product-name.
     * Unless the product developer center is not registered with own vendor
     * identity one should use the value 193 assigned to Ericsson AB. The provided
     * value is used to construct a Vendor-Id AVP
     * (https://tools.ietf.org/html/rfc6733#section-5.3.3) placed in related
     * capability exchange messages (CER/CEA messages) during peer connection setup.
     * Update Apply: Immediate. Update Effect: All established diameter peer
     * connections linked to related Own Diameter Node are dropped and reestablished
     * with updated information.
     * 
     */
    @JsonProperty("vendor-id")
    public Long getVendorId()
    {
        return vendorId;
    }

    /**
     * Used to specify the identity of the vendor implementing the product specified
     * for product-name. The should take as value an IANA allocated SMI Network
     * Management Private Enterprise Code (see
     * https://www.iana.org/assignments/enterprise-integers/enterprise-integers )
     * assigned for the vendor implementing the product specified for product-name.
     * Unless the product developer center is not registered with own vendor
     * identity one should use the value 193 assigned to Ericsson AB. The provided
     * value is used to construct a Vendor-Id AVP
     * (https://tools.ietf.org/html/rfc6733#section-5.3.3) placed in related
     * capability exchange messages (CER/CEA messages) during peer connection setup.
     * Update Apply: Immediate. Update Effect: All established diameter peer
     * connections linked to related Own Diameter Node are dropped and reestablished
     * with updated information.
     * 
     */
    @JsonProperty("vendor-id")
    public void setVendorId(Long vendorId)
    {
        this.vendorId = vendorId;
    }

    public Node withVendorId(Long vendorId)
    {
        this.vendorId = vendorId;
        return this;
    }

    /**
     * Used to specify the revision of the software product specified for
     * product-name. If there is an value provided it is used to construct a
     * Firmware-Revision AVP (https://tools.ietf.org/html/rfc6733#section-5.3.4)
     * placed in related capability exchange messages (CER/CEA messages) during peer
     * connection setup. Update Apply: Immediate. Update Effect: All established
     * diameter peer connections linked to related Own Diameter Node are dropped and
     * reestablished with updated information.
     * 
     */
    @JsonProperty("firmware-revision")
    public Long getFirmwareRevision()
    {
        return firmwareRevision;
    }

    /**
     * Used to specify the revision of the software product specified for
     * product-name. If there is an value provided it is used to construct a
     * Firmware-Revision AVP (https://tools.ietf.org/html/rfc6733#section-5.3.4)
     * placed in related capability exchange messages (CER/CEA messages) during peer
     * connection setup. Update Apply: Immediate. Update Effect: All established
     * diameter peer connections linked to related Own Diameter Node are dropped and
     * reestablished with updated information.
     * 
     */
    @JsonProperty("firmware-revision")
    public void setFirmwareRevision(Long firmwareRevision)
    {
        this.firmwareRevision = firmwareRevision;
    }

    public Node withFirmwareRevision(Long firmwareRevision)
    {
        this.firmwareRevision = firmwareRevision;
        return this;
    }

    /**
     * Used to specify the list of IP addresses (a list of IPv4 and/or IPv6
     * addresses) that can be used by a Diameter Peer to connect to the Own Diameter
     * Node. The IP addresses specified shall visible for related Diameter Peers
     * (for instance the IP address of external IP load balancer behind which the
     * Own Diameter Node is placed). The provided values are used to construct
     * relevant Host-IP-Address AVP
     * (https://tools.ietf.org/html/rfc6733#section-5.3.5) which is placed in
     * capability exchange messages (CER/CEA messages) during related AAA Service
     * linked peer connection setup. If no value is provided to this the
     * Host-IP-Address AVP is constructed by using the IP addresses provided for the
     * Own Diameter Node related Local Endpoints (see local-endpoint). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections linked to
     * related Own Diameter Node are dropped and reestablished with updated
     * information.
     * 
     */
    @JsonProperty("host-ip-address")
    public List<String> getHostIpAddress()
    {
        return hostIpAddress;
    }

    /**
     * Used to specify the list of IP addresses (a list of IPv4 and/or IPv6
     * addresses) that can be used by a Diameter Peer to connect to the Own Diameter
     * Node. The IP addresses specified shall visible for related Diameter Peers
     * (for instance the IP address of external IP load balancer behind which the
     * Own Diameter Node is placed). The provided values are used to construct
     * relevant Host-IP-Address AVP
     * (https://tools.ietf.org/html/rfc6733#section-5.3.5) which is placed in
     * capability exchange messages (CER/CEA messages) during related AAA Service
     * linked peer connection setup. If no value is provided to this the
     * Host-IP-Address AVP is constructed by using the IP addresses provided for the
     * Own Diameter Node related Local Endpoints (see local-endpoint). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections linked to
     * related Own Diameter Node are dropped and reestablished with updated
     * information.
     * 
     */
    @JsonProperty("host-ip-address")
    public void setHostIpAddress(List<String> hostIpAddress)
    {
        this.hostIpAddress = hostIpAddress;
    }

    public Node withHostIpAddress(List<String> hostIpAddress)
    {
        this.hostIpAddress = hostIpAddress;
        return this;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public Node withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * TLS profile to be associated with local-endpoints of the node. Properties of
     * tls-profile are used when securing connections for TCP transports with TLS
     * 1.2 or TLS 1.3.
     * 
     */
    @JsonProperty("tls-profile")
    public List<TlsProfile> getTlsProfile()
    {
        return tlsProfile;
    }

    /**
     * TLS profile to be associated with local-endpoints of the node. Properties of
     * tls-profile are used when securing connections for TCP transports with TLS
     * 1.2 or TLS 1.3.
     * 
     */
    @JsonProperty("tls-profile")
    public void setTlsProfile(List<TlsProfile> tlsProfile)
    {
        this.tlsProfile = tlsProfile;
    }

    public Node withTlsProfile(List<TlsProfile> tlsProfile)
    {
        this.tlsProfile = tlsProfile;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Node.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("originRealm");
        sb.append('=');
        sb.append(((this.originRealm == null) ? "<null>" : this.originRealm));
        sb.append(',');
        sb.append("originHost");
        sb.append('=');
        sb.append(((this.originHost == null) ? "<null>" : this.originHost));
        sb.append(',');
        sb.append("productName");
        sb.append('=');
        sb.append(((this.productName == null) ? "<null>" : this.productName));
        sb.append(',');
        sb.append("vendorId");
        sb.append('=');
        sb.append(((this.vendorId == null) ? "<null>" : this.vendorId));
        sb.append(',');
        sb.append("firmwareRevision");
        sb.append('=');
        sb.append(((this.firmwareRevision == null) ? "<null>" : this.firmwareRevision));
        sb.append(',');
        sb.append("hostIpAddress");
        sb.append('=');
        sb.append(((this.hostIpAddress == null) ? "<null>" : this.hostIpAddress));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("tlsProfile");
        sb.append('=');
        sb.append(((this.tlsProfile == null) ? "<null>" : this.tlsProfile));
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
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.originHost == null) ? 0 : this.originHost.hashCode()));
        result = ((result * 31) + ((this.originRealm == null) ? 0 : this.originRealm.hashCode()));
        result = ((result * 31) + ((this.vendorId == null) ? 0 : this.vendorId.hashCode()));
        result = ((result * 31) + ((this.hostIpAddress == null) ? 0 : this.hostIpAddress.hashCode()));
        result = ((result * 31) + ((this.tlsProfile == null) ? 0 : this.tlsProfile.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.productName == null) ? 0 : this.productName.hashCode()));
        result = ((result * 31) + ((this.firmwareRevision == null) ? 0 : this.firmwareRevision.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Node) == false)
        {
            return false;
        }
        Node rhs = ((Node) other);
        return ((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                       && ((this.originHost == rhs.originHost) || ((this.originHost != null) && this.originHost.equals(rhs.originHost))))
                      && ((this.originRealm == rhs.originRealm) || ((this.originRealm != null) && this.originRealm.equals(rhs.originRealm))))
                     && ((this.vendorId == rhs.vendorId) || ((this.vendorId != null) && this.vendorId.equals(rhs.vendorId))))
                    && ((this.hostIpAddress == rhs.hostIpAddress) || ((this.hostIpAddress != null) && this.hostIpAddress.equals(rhs.hostIpAddress))))
                   && ((this.tlsProfile == rhs.tlsProfile) || ((this.tlsProfile != null) && this.tlsProfile.equals(rhs.tlsProfile))))
                  && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                 && ((this.productName == rhs.productName) || ((this.productName != null) && this.productName.equals(rhs.productName))))
                && ((this.firmwareRevision == rhs.firmwareRevision)
                    || ((this.firmwareRevision != null) && this.firmwareRevision.equals(rhs.firmwareRevision))));
    }

}

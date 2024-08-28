package com.ericsson.sc.certnotifier;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "asymmetric-keys", "trusted-authorities" })
public class ListNotification
{

    @JsonProperty("asymmetric-keys")
    private List<AsymmetricKey> asymmetricKeys = new ArrayList<>();
    @JsonProperty("trusted-authorities")
    private List<TrustedAuthority> trustedAuthorities = new ArrayList<>();

    /**
     * No args constructor for use in serialization
     *
     */
    public ListNotification()
    {
    }

    /**
     *
     * @param trustedAuthorities
     * @param asymmetricKeys
     */
    public ListNotification(List<AsymmetricKey> asymmetricKeys,
                            List<TrustedAuthority> trustedAuthorities)
    {
        super();
        this.asymmetricKeys = asymmetricKeys;
        this.trustedAuthorities = trustedAuthorities;
    }

    @JsonProperty("asymmetric-keys")
    public List<AsymmetricKey> getAsymmetricKeys()
    {
        return asymmetricKeys;
    }

    @JsonProperty("asymmetric-keys")
    public void setAsymmetricKeys(List<AsymmetricKey> asymmetricKeys)
    {
        this.asymmetricKeys = asymmetricKeys;
    }

    public ListNotification withAsymmetricKeys(List<AsymmetricKey> asymmetricKeys)
    {
        this.asymmetricKeys = asymmetricKeys;
        return this;
    }

    @JsonProperty("trusted-authorities")
    public List<TrustedAuthority> getTrustedAuthorities()
    {
        return trustedAuthorities;
    }

    @JsonProperty("trusted-authorities")
    public void setTrustedAuthorities(List<TrustedAuthority> trustedAuthorities)
    {
        this.trustedAuthorities = trustedAuthorities;
    }

    public ListNotification withTrustedAuthorities(List<TrustedAuthority> trustedAuthorities)
    {
        this.trustedAuthorities = trustedAuthorities;
        return this;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append(ListNotification.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("asymmetricKeys");
        sb.append('=');
        sb.append(((this.asymmetricKeys == null) ? "<null>" : this.asymmetricKeys));
        sb.append(',');
        sb.append("trustedAuthorities");
        sb.append('=');
        sb.append(((this.trustedAuthorities == null) ? "<null>" : this.trustedAuthorities));
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
        var result = 1;
        result = ((result * 31) + ((this.asymmetricKeys == null) ? 0 : this.asymmetricKeys.hashCode()));
        result = ((result * 31) + ((this.trustedAuthorities == null) ? 0 : this.trustedAuthorities.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ListNotification) == false)
        {
            return false;
        }
        ListNotification rhs = ((ListNotification) other);
        return (((this.asymmetricKeys == rhs.asymmetricKeys) || ((this.asymmetricKeys != null) && this.asymmetricKeys.equals(rhs.asymmetricKeys)))
                && ((this.trustedAuthorities == rhs.trustedAuthorities)
                    || ((this.trustedAuthorities != null) && this.trustedAuthorities.equals(rhs.trustedAuthorities))));
    }
}

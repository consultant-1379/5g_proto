/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 05, 2020
 *     Author: eevagal
 */
package com.ericsson.esc.bsf.worker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.javatuples.Pair;

import com.ericsson.sc.bsf.model.Nrf;
import com.ericsson.sc.nfm.model.AllowedPlmn;
import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.nfm.model.Plmn;
import com.ericsson.sc.nfm.model.Snssai1;

/**
 * CM configuration relevant to the BSF. This accounts for specific
 * configuration yang fields that are used for bsf-worker related
 * functionalities. A part of the NfProfile object's fields are applicable to
 * this configuration (fields that are used in oauth) but for the sake of
 * complexity the whole object is taken into consideration.
 */
public final class BsfCmConfig
{

    private final int bindingTimeout;
    private final boolean outMessageHandling;
    private final String nfInstanceName;
    private final String nfInstanceId;
    private final Pair<Boolean, Boolean> oAuth; // value 0 is for non tls and value 1 for tls
    private List<Oauth2KeyProfile> oAuthkeyProfiles;
    private final Map<String, Oauth2KeyProfile> oAuthkeyProfilesMap = new HashMap<>();
    private final List<Nrf> nrfs;
    private final List<NfProfile> nfProfiles;

    private BsfCmConfig(Builder builder)
    {
        Objects.requireNonNull(builder.nfInstanceName);
        if (builder.bindingTimeout < 0)
            throw new IllegalArgumentException("Invalid bindingTimeout: " + builder.bindingTimeout);
        this.bindingTimeout = builder.bindingTimeout;
        this.outMessageHandling = builder.outMessageHandling;
        this.nfInstanceName = builder.nfInstanceName;
        this.nfInstanceId = builder.nfInstanceId;
        this.nrfs = builder.nrfs;
        this.nfProfiles = builder.nfProfiles;
        this.oAuth = builder.oAuth;
        this.oAuthkeyProfiles = builder.oAuthkeyProfiles;
        if (Objects.nonNull(this.oAuthkeyProfiles) && !this.oAuthkeyProfiles.isEmpty())
        {
            this.oAuthkeyProfiles.forEach(prof -> oAuthkeyProfilesMap.put(prof.getKeyId(), prof));
        }
    }

    public int getBindingTimeout()
    {
        return bindingTimeout;
    }

    public String getNfInstanceId()
    {
        return nfInstanceId;
    }

    public String getNfInstanceName()
    {
        return nfInstanceName;
    }

    public boolean isOutMessageHandling()
    {
        return outMessageHandling;
    }

    public Pair<Boolean, Boolean> getOauth()
    {
        return oAuth;
    }

    public List<Oauth2KeyProfile> getOAuthkeyProfiles()
    {
        return oAuthkeyProfiles;
    }

    public void setOAuthKeyProfiles(List<Oauth2KeyProfile> oAuthKeyProfilesList)
    {
        oAuthkeyProfiles = oAuthKeyProfilesList;
    }

    public Map<String, Oauth2KeyProfile> getOAuthkeyProfilesMap()
    {
        return oAuthkeyProfilesMap;
    }

    public void setOAuthKeyProfilesMap(List<Oauth2KeyProfile> oAuthKeyProfilesList)
    {
        oAuthKeyProfilesList.forEach(prof -> oAuthkeyProfilesMap.put(prof.getKeyId(), prof));
    }

    public List<Nrf> getNrfs()
    {
        return nrfs;
    }

    public List<NfProfile> getNfProfiles()
    {
        return nfProfiles;
    }

    public List<Plmn> getPlmn()
    {
        return CmConfigurationUtil.getPlmn(nfProfiles);
    }

    public List<String> getNfSetId()
    {
        return CmConfigurationUtil.getNfSetId(nfProfiles);
    }

    public List<Snssai1> getSnssai1()
    {
        return CmConfigurationUtil.getSnssai1(nfProfiles);
    }

    public List<String> getNsi()
    {
        return CmConfigurationUtil.getNsi(nfProfiles);
    }

    public List<AllowedPlmn> getAllowedPlmn()
    {
        return CmConfigurationUtil.getAllowedPlmn(nfProfiles);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bindingTimeout, nfInstanceName, nfInstanceId, nrfs, nfProfiles, oAuth, oAuthkeyProfiles, outMessageHandling);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BsfCmConfig other = (BsfCmConfig) obj;
        return bindingTimeout == other.bindingTimeout && outMessageHandling == other.outMessageHandling && Objects.equals(nfInstanceName, other.nfInstanceName)
               && Objects.equals(nfInstanceId, other.nfInstanceId) && Objects.equals(nrfs, other.nrfs) && Objects.equals(nfProfiles, other.nfProfiles)
               && Objects.equals(oAuth, other.oAuth) && Objects.equals(oAuthkeyProfiles, other.oAuthkeyProfiles);
    }

    // oAuthKey profile is not included in the method for security reasons
    @Override
    public String toString()
    {
        return "BsfCmConfig [bindingTimeout=" + bindingTimeout + ", outMessageHandling=" + outMessageHandling + ", nfInstanceName=" + nfInstanceName
               + ", nfInstanceId=" + nfInstanceId + ", oAuth=" + oAuth + ", nrfs=" + nrfs + ", nfProfiles=" + nfProfiles + "]";
    }

    public static class Builder
    {

        private int bindingTimeout;
        private boolean outMessageHandling;
        private String nfInstanceName;
        private String nfInstanceId;
        private Pair<Boolean, Boolean> oAuth;
        private List<Oauth2KeyProfile> oAuthkeyProfiles;
        private List<Nrf> nrfs;
        private List<NfProfile> nfProfiles;

        public Builder bindingTimeout(int bindingTimeout)
        {
            this.bindingTimeout = bindingTimeout;
            return this;
        }

        public Builder outMessageHandling(boolean outMessageHandling)
        {
            this.outMessageHandling = outMessageHandling;
            return this;
        }

        public Builder nfInstanceName(String nfInstanceName)
        {
            this.nfInstanceName = nfInstanceName;
            return this;
        }

        public Builder nfInstanceId(String nfInstanceId)
        {
            this.nfInstanceId = nfInstanceId;
            return this;
        }

        public Builder oAuth(Pair<Boolean, Boolean> oAuth)
        {
            this.oAuth = oAuth;
            return this;
        }

        public Builder oAuthkeyProfiles(List<Oauth2KeyProfile> oAuthkeyProfiles)
        {
            this.oAuthkeyProfiles = oAuthkeyProfiles;
            return this;
        }

        public Builder nrfs(List<Nrf> nrfs)
        {
            this.nrfs = nrfs;
            return this;
        }

        public Builder nfProfiles(List<NfProfile> nfProfiles)
        {
            this.nfProfiles = nfProfiles;
            return this;
        }

        public BsfCmConfig build()
        {
            return new BsfCmConfig(this);
        }
    }
}

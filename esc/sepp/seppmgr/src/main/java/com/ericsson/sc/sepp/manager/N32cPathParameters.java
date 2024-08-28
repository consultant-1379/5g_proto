package com.ericsson.sc.sepp.manager;

public class N32cPathParameters
{
    private String roamingPartner;
    private String nfInstanceRef;
    private String lastValue;

    public String getLastValue()
    {
        return lastValue;
    }

    public void setLastValue(String lastValue)
    {
        this.lastValue = lastValue;
    }

    public N32cPathParameters()
    {
    }

    public String getRoamingPartner()
    {
        return roamingPartner;
    }

    public String getNfInstanceRef()
    {
        return nfInstanceRef;
    }

    public void setRoamingPartner(String roamingPartner)
    {
        this.roamingPartner = roamingPartner;
    }

    public void setNfInstanceRef(String sepp)
    {
        this.nfInstanceRef = sepp;
    }

    @Override
    public String toString()
    {
        return "N32cPathParameters [roamingPartner=" + roamingPartner + ", nfInstanceRef=" + nfInstanceRef + ", lastValue=" + lastValue + "]";
    }

}
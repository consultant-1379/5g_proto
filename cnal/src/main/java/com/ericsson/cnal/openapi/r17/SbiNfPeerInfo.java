/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 7, 2023
 *     Author: eedstl
 */

package com.ericsson.cnal.openapi.r17;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Encapsulates parsing and generation of header 3gpp-Sbi-NF-Peer-Info.
 */
public class SbiNfPeerInfo
{
    private static final String PREFIX_SCP = "SCP-";
    private static final String PREFIX_SEPP = "SEPP-";

    private static final String DST_INST = "dstinst";
    private static final String DST_SERV_INST = "dstservinst";
    private static final String DST_SCP = "dstscp";
    private static final String DST_SEPP = "dstsepp";
    private static final String ORIG_SCP = "origscp";
    private static final String ORIG_SEPP = "origsepp";
    private static final String SRC_INST = "srcinst";
    private static final String SRC_SERV_INST = "srcservinst";
    private static final String SRC_SCP = "srcscp";
    private static final String SRC_SEPP = "srcsepp";
    private static final String TERM_SCP = "termscp";
    private static final String TERM_SEPP = "termsepp";

    public static SbiNfPeerInfo of()
    {
        return new SbiNfPeerInfo();
    }

    /**
     * Format definition of the header 3gpp-Sbi-NF-Peer-Info:
     * 
     * <pre>
     * 3gpp-Sbi-NF-Peer-Info = "3gpp-Sbi-NF-Peer-Info" ":" OWS peerinfo *(";" OWS peerinfo)
     * peerinfo = peertype "=" token
     * </pre>
     * 
     * The following peertype are defined:
     * <p>
     * <ul>
     * <li><code>srcinst</code> (Source NF instance): indicates the Source NF
     * Instance ID, as defined in 3GPP TS 29.510 [8];
     * <li><code>srcservinst</code> (Source NF service instance): indicates the
     * Source NF Service Instance ID, as defined in 3GPP TS 29.510 [8]; if this
     * parameter is present, srcinst shall also be present;
     * <li><code>srcscp</code> (Source SCP): indicates the FQDN of the Source SCP,
     * the format is "SCP-<SCP FQDN>"; this parameter shall only be included by an
     * SCP, i.e. when the HTTP request or response message is originated or relayed
     * by an SCP;
     * <li><code>srcsepp</code> (Source SEPP): indicates the FQDN of the Source
     * SEPP, the format is "SEPP-<SEPP FQDN>"; this parameter shall only be included
     * by a SEPP, i.e. when the HTTP request or response message is originated or
     * relayed by a SEPP;
     * <li><code>dstinst</code> (Destination NF instance): indicates the Destination
     * NF Instance ID, as defined in 3GPP TS 29.510 [8];
     * <li><code>dstservinst</code> (Destination NF service instance): indicates the
     * Destination NF Service Instance ID, as defined in 3GPP TS 29.510 [8]; if this
     * parameter is present, dstinst shall also be present;
     * <li><code>dstscp</code> (Destination SCP): indicates the FQDN of the
     * Destination SCP, the format is "SCP-<SCP FQDN>"; this parameter shall contain
     * the next-hop SCP of the HTTP request or response message to be included by an
     * SCP or SEPP or by clients/servers sending requests/responses to an SCP;
     * <li><code>dstsepp</code> (Destination SEPP): indicates the FQDN of the
     * Destination SEPP, the format is "SEPP-<SEPP FQDN>"; this parameter shall be
     * included by an SCP or by clients/servers sending requests/responses to a
     * SEPP; it may also be included by a SEPP, based on operator's policy. The
     * header shall contain the source peer information, and should contain the
     * destination peer information if available.
     * </ul>
     * EXAMPLE:
     * 
     * <pre>
     * 3gpp-Sbi-NF-Peer-Info: srcinst=54804518-4191-46b3-955c-ac631f953ed8; dstinst=54804518-4191-4453-569c-ac631f74765cd
     * </pre>
     * 
     * @param sbiNfPeerInfo The value of the header 3gpp-Sbi-NF-Peer-Info
     * @return A new instance of class SbiNfPeerInfo
     */
    public static SbiNfPeerInfo of(final String sbiNfPeerInfo)
    {
        final SbiNfPeerInfo newInstance = new SbiNfPeerInfo();

        if (sbiNfPeerInfo != null && !sbiNfPeerInfo.isEmpty())
        {
            final String[] fields = sbiNfPeerInfo.split(";");

            for (final String field : fields)
            {
                final String[] keyVal = field.split("=");

                final String val = keyVal[1].strip();

                if (!val.isEmpty())
                {
                    switch (keyVal[0].toLowerCase().strip())
                    {
                        case DST_INST:
                            newInstance.setDstInst(val);
                            break;

                        case DST_SERV_INST:
                            newInstance.setDstServInst(val);
                            break;

                        case DST_SCP:
                            newInstance.setDstScp(val);
                            break;

                        case DST_SEPP:
                            newInstance.setDstSepp(val);
                            break;

                        case SRC_INST:
                            newInstance.setSrcInst(val);
                            break;

                        case SRC_SERV_INST:
                            newInstance.setSrcServInst(val);
                            break;

                        case SRC_SCP:
                            newInstance.setSrcScp(val);
                            break;

                        case SRC_SEPP:
                            newInstance.setSrcSepp(val);
                            break;

                        default:
                            break;
                    }
                }
            }
        }

        return newInstance;
    }

    public static String swapSrcAndDstFields(final String sbiNfPeerInfo)
    {
        if (sbiNfPeerInfo == null || sbiNfPeerInfo.isEmpty())
            return null;

        final StringBuilder b = new StringBuilder();
        final String[] fields = sbiNfPeerInfo.split(";");

        for (final String field : fields)
        {
            if (b.length() > 0)
                b.append(";");

            final String[] keyVal = field.split("=");

            switch (keyVal[0].toLowerCase().strip())
            {
                case DST_INST:
                    b.append(SRC_INST);
                    break;

                case DST_SERV_INST:
                    b.append(SRC_SERV_INST);
                    break;

                case DST_SCP:
                    b.append(SRC_SCP);
                    break;

                case DST_SEPP:
                    b.append(SRC_SEPP);
                    break;

                case ORIG_SCP:
                    b.append(TERM_SCP);
                    break;

                case ORIG_SEPP:
                    b.append(TERM_SEPP);
                    break;

                case SRC_INST:
                    b.append(DST_INST);
                    break;

                case SRC_SERV_INST:
                    b.append(DST_SERV_INST);
                    break;

                case SRC_SCP:
                    b.append(DST_SCP);
                    break;

                case SRC_SEPP:
                    b.append(DST_SEPP);
                    break;

                case TERM_SCP: // Not applicable for swapping
                case TERM_SEPP: // Not applicable for swapping
                default:
                    continue;
            }

            b.append("=").append(keyVal[1].strip());
        }

        return b.toString();
    }

    private final Map<String, String> fields = new TreeMap<>(Collections.reverseOrder());

    public SbiNfPeerInfo()
    {
    }

    /**
     * @return the dstInst
     */
    public String getDstInst()
    {
        return this.fields.get(DST_INST);
    }

    /**
     * @return the dstScp
     */
    public String getDstScp()
    {
        return this.fields.get(DST_SCP);
    }

    /**
     * @return the dstSepp
     */
    public String getDstSepp()
    {
        return this.fields.get(DST_SEPP);
    }

    /**
     * @return the dstServInst
     */
    public String getDstServInst()
    {
        return this.fields.get(DST_SERV_INST);
    }

    /**
     * @return the origScp
     */
    public String getOrigScp()
    {
        return this.fields.get(ORIG_SCP);
    }

    /**
     * @return the origSepp
     */
    public String getOrigSepp()
    {
        return this.fields.get(ORIG_SEPP);
    }

    /**
     * @return the srcInst
     */
    public String getSrcInst()
    {
        return this.fields.get(SRC_INST);
    }

    /**
     * @return the srcScp
     */
    public String getSrcScp()
    {
        return this.fields.get(SRC_SCP);
    }

    /**
     * @return the srcSepp
     */
    public String getSrcSepp()
    {
        return this.fields.get(SRC_SEPP);
    }

    /**
     * @return the srcServInst
     */
    public String getSrcServInst()
    {
        return this.fields.get(SRC_SERV_INST);
    }

    /**
     * @return the termScp
     */
    public String getTermScp()
    {
        return this.fields.get(TERM_SCP);
    }

    /**
     * @return the termSepp
     */
    public String getTermSepp()
    {
        return this.fields.get(TERM_SEPP);
    }

    /**
     * @param value the dstInst to set
     * @return this
     */
    public SbiNfPeerInfo setDstInst(String value)
    {
        if (value != null && !value.isEmpty())
            this.fields.put(DST_INST, value);
        else
            this.fields.remove(DST_INST);

        return this;
    }

    /**
     * @param value the dstScp to set, will be prepended by "SCP-" if not already
     *              there
     * @return this
     */
    public SbiNfPeerInfo setDstScp(String value)
    {
        if (value != null && !value.isEmpty())
            this.fields.put(DST_SCP, value.startsWith(PREFIX_SCP) ? value : PREFIX_SCP + value);
        else
            this.fields.remove(DST_SCP);
        return this;
    }

    /**
     * @param value the dstSepp to set, will be prepended by "SEPP-" if not already
     *              there
     * @return this
     */
    public SbiNfPeerInfo setDstSepp(String value)
    {
        if (value != null && !value.isEmpty())
            this.fields.put(DST_SEPP, value.startsWith(PREFIX_SEPP) ? value : PREFIX_SEPP + value);
        else
            this.fields.remove(DST_SEPP);
        return this;
    }

    /**
     * @param value the dstServInst to set
     * @return this
     */
    public SbiNfPeerInfo setDstServInst(String value)
    {
        if (value != null && !value.isEmpty())
            this.fields.put(DST_SERV_INST, value);
        else
            this.fields.remove(DST_SERV_INST);
        return this;
    }

    /**
     * @param value the srcInst to set
     * @return this
     */
    public SbiNfPeerInfo setSrcInst(String value)
    {
        if (value != null)
            this.fields.put(SRC_INST, value);
        else
            this.fields.remove(SRC_INST);
        return this;
    }

    /**
     * @param value the srcScp to set, will be prepended by "SCP-" if not already
     *              there
     * @return this
     */
    public SbiNfPeerInfo setSrcScp(String value)
    {
        if (value != null && !value.isEmpty())
            this.fields.put(SRC_SCP, value.startsWith(PREFIX_SCP) ? value : PREFIX_SCP + value);
        else
            this.fields.remove(SRC_SCP);
        return this;
    }

    /**
     * @param value the srcSepp to set, will be prepended by "SEPP-" if not already
     *              there
     * @return this
     */
    public SbiNfPeerInfo setSrcSepp(String value)
    {
        if (value != null && !value.isEmpty())
            this.fields.put(SRC_SEPP, value.startsWith(PREFIX_SEPP) ? value : PREFIX_SEPP + value);
        else
            this.fields.remove(SRC_SEPP);
        return this;
    }

    /**
     * @param value the srcServInst to set
     * @return this
     */
    public SbiNfPeerInfo setSrcServInst(String value)
    {
        if (value != null && !value.isEmpty())
            this.fields.put(SRC_SERV_INST, value);
        else
            this.fields.remove(SRC_SERV_INST);
        return this;
    }

    public SbiNfPeerInfo shiftDstScpToSrcScp()
    {
        return this.setSrcScp(this.getDstScp()).setDstScp(null);
    }

    public SbiNfPeerInfo shiftDstSeppToSrcSepp()
    {
        return this.setSrcSepp(this.getDstSepp()).setDstSepp(null);
    }

    public SbiNfPeerInfo shiftSrcInstToDstInst()
    {
        return this.setDstInst(this.getSrcInst()).setSrcInst(null);
    }

    public SbiNfPeerInfo shiftSrcScpToDstScp()
    {
        return this.setDstScp(this.getSrcScp()).setSrcScp(null);
    }

    public SbiNfPeerInfo shiftSrcSeppToDstSepp()
    {
        return this.setDstSepp(this.getSrcSepp()).setSrcSepp(null);
    }

    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();

        for (final Entry<String, String> e : this.fields.entrySet())
        {
            if (b.length() > 0)
                b.append(";");

            b.append(e.getKey()).append("=").append(e.getValue());
        }

        return b.toString();
    }
}

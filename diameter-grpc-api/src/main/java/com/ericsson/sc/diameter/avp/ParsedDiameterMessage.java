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
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.diameter.avp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterMessage;
import com.ericsson.gs.tm.diameter.service.grpc.GroupedAvp;

/**
 * A {@link DiameterMessage} that has been parsed for known AVPs. A
 * {@link MessageParser} can be used to create parsed diameter message objects.
 */
public class ParsedDiameterMessage
{
    final DiameterMessage dm;
    private final Map<AvpDef<?>, List<Integer>> foundAvps;

    /**
     * 
     * @param dm             The Diameter message to parse
     * @param avpDefinitions A map containing the AVP definitions that need to be
     *                       parsed. The map key should be the AVP ID
     */
    ParsedDiameterMessage(DiameterMessage dm,
                          Map<AvpId, AvpDef<?>> avpDefinitions)
    {
        this.dm = dm;
        this.foundAvps = indexAvps(this.dm.getAvpsList(), avpDefinitions);
    }

    public DiameterMessage getDiameterMessage()
    {
        return this.dm;
    }

    public String getOriginHost()
    {
        return uniqueAvpValue(Avps.ORIGIN_HOST).orElseThrow(() -> new MissingAvpException(Avps.ORIGIN_HOST));
    }

    public String getOriginRealm()
    {
        return uniqueAvpValue(Avps.ORIGIN_REALM).orElseThrow(() -> new MissingAvpException(Avps.ORIGIN_REALM));
    }

    public <T> Optional<T> uniqueAvpValue(AvpDef<T> avp)
    {
        return Optional.ofNullable(this.foundAvps.get(avp)) //
                       .map(avps ->
                       {
                           if (avps.size() > 1)
                           {
                               // AVP occurs too many times in diameter message
                               throw new InvalidAvpCardinalityException(this.dm.getAvps(avps.get(1)));
                           }
                           return avps.get(0);
                       }) //
                       .map(this.dm::getAvps)
                       .map(avp.getType()::value);
    }

    public <T> Optional<T> anyAvpValue(AvpDef<T> avp)
    {
        return Optional.ofNullable(this.foundAvps.get(avp)) //
                       .map(avps -> avps.get(0)) //
                       .map(this.dm::getAvps)
                       .map(avp.getType()::value);
    }

    /**
     * 
     * @return A Transformer that can be used to mutate this Diameter message
     */
    public Transformer transform()
    {
        return new Transformer();
    }

    /**
     * Create an error answer for this Diameter message
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6733#section-6.2">RFC 6733</a>
     * 
     * @param resultCode  The content of the Result-Code AVP
     * @param failedAvps  The content of the Failed-AVP AVPs, of an empty List if no
     *                    failed AVPs are provided
     * @param originHost  The content of the Origin-Host AVP
     * @param originRealm The content of the Origin-Realm AVP
     * @return An error answer Diameter message, constructed from current diameter
     *         message
     */
    public DiameterMessage createErrorAnswerFromRequest(ResultCode resultCode,
                                                        List<DiameterAvp> failedAvps)
    {
        // Copy necessary AVPs for error message from request
        var copiedDiaAvps = avpIndex(Arrays.asList(Avps.SESSION_ID, Avps.PROXY_INFO)) //
                                                                                     .stream()
                                                                                     .map(this.dm::getAvps)

                                                                                     .collect(Collectors.toList());
        final var avpOriginHost = Avps.ORIGIN_HOST.withValue("");
        final var avpOriginRealm = Avps.ORIGIN_REALM.withValue("");

        final var avpResultCode = Avps.RESULT_CODE.withValue(resultCode.code);

        final var header = this.dm.getHeader() //
                                  .toBuilder()
                                  .setFlagR(false)
                                  .setFlagE(true)
                                  .build(); // Copy header and mofify the R bit
        final var errorMessageBuilder = DiameterMessage.newBuilder() //
                                                       .setHeader(header)
                                                       .addAvps(avpOriginHost)
                                                       .addAvps(avpOriginRealm)
                                                       .addAvps(avpResultCode)
                                                       .addAllAvps(copiedDiaAvps);
        if (!failedAvps.isEmpty())
        {
            errorMessageBuilder.addAvps(Avps.FAILED_AVP.withValue(GroupedAvp.newBuilder().addAllAvps(failedAvps).build()));
        }
        return errorMessageBuilder.build();
    }

    private <T> SortedSet<Integer> avpIndex(AvpDef<T> avp)
    {
        final var result = new TreeSet<Integer>();
        var found = this.foundAvps.get(avp);
        if (found != null)
        {
            result.addAll(found);
        }
        return result;
    }

    private SortedSet<Integer> avpIndex(List<AvpDef<?>> avps)
    {
        final var result = new TreeSet<Integer>();
        for (var i : avps)
        {
            result.addAll(avpIndex(i));
        }
        return result;
    }

    /**
     * Find the index within the diameter message for all given AVP definitions
     * 
     * @param msgAvps
     * @param searchLIst
     * @return
     */
    private static Map<AvpDef<?>, List<Integer>> indexAvps(List<DiameterAvp> msgAvps,
                                                           Map<AvpId, AvpDef<?>> searchLIst)
    {
        HashMap<AvpDef<?>, List<Integer>> found = new HashMap<>();
        for (var avpIndex = 0; avpIndex < msgAvps.size(); avpIndex++)
        {
            final var avpHeader = msgAvps.get(avpIndex).getHeader();
            // Compute "avpId", comprising of AVP code and Vendor-ID
            // Take into account Vendor-ID , only if V-flag is on
            final var avpId = avpHeader.getFlagV() ? AvpId.of(avpHeader.getCode(), avpHeader.getVendorId()) : AvpId.of(avpHeader.getCode());

            var avpDef = searchLIst.get(avpId);
            if (avpDef != null)
            {
                var foundAvpList = found.get(avpDef);
                if (foundAvpList == null)
                {
                    // Single AVP in message with given ID, create a new list containing the newly
                    // found index
                    final var indexList = new ArrayList<Integer>();
                    indexList.add(avpIndex);
                    found.put(avpDef, indexList);
                }
                else
                {
                    // AVPs with given ID encountered previously, append newly found to list
                    foundAvpList.add(avpIndex);
                }
            }
            else
            {
                // Ignore AVP contained in message not in search list
            }
        }
        return found;
    }

    public final class Transformer
    {
        private final DiameterMessage.Builder dmBuilder;

        private Transformer()
        {
            this.dmBuilder = dm.toBuilder();
        }

        public <T> Transformer addOrReplaceAvp(AvpDef<T> avp,
                                               T value)
        {
            final var indexes = avpIndex(avp);
            if (indexes.isEmpty())
            {
                this.dmBuilder.addAvps(avp.withValue(value)); // Append AVP, does not exist in original message
            }
            else
            {
                // Replace AVPs
                for (var i : indexes)
                {
                    this.dmBuilder.setAvps(i, avp.createAvp(value, dm.getAvps(i)));
                }
            }
            return this;
        }

        /**
         * Construct a Diameter message according to this Transformer
         * 
         * @return The transformed Diameter message
         */
        public DiameterMessage buildMessage()
        {
            return this.dmBuilder.build();
        }
    }

}

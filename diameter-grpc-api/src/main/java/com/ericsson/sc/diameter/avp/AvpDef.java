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

import java.util.Objects;

import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvpHeader;

/**
 * AVP Definition
 * 
 * @param <T> The Java data type corresponding to the AVP content
 */
public class AvpDef<T>
{
    private final AvpId id;
    private final AvpType<T> type;
    private final boolean mBit;

    public static class Builder<T>
    {
        private AvpId avpId;
        private final AvpType<T> avpType;
        private boolean mBit = false;

        public Builder(AvpType<T> avpType)
        {
            this.avpType = avpType;
        }

        public Builder<T> setMbit()
        {
            this.mBit = true;
            return this;
        }

        public Builder<T> setAvpCode(int avpCode)
        {
            this.avpId = AvpId.of(avpCode);
            return this;
        }

        public Builder<T> setAvpCode(int avpCode,
                                     VendorId vendorId)
        {
            this.avpId = AvpId.of(avpCode, vendorId.getId());
            return this;
        }

        public AvpDef<T> build()
        {
            return new AvpDef<>(this);
        }
    }

    public static <T> Builder<T> create(AvpType<T> type)
    {
        return new Builder<>(type);
    }

    private AvpDef(Builder<T> builder)
    {
        this.id = builder.avpId;
        this.type = builder.avpType;
        this.mBit = builder.mBit;
    }

    /**
     * Create an AVP definition for a possibly vendor specific AVP
     * 
     * @param id   The AVP code and AVP vendor Id
     * @param mBit The default value of the Mandatory bit
     * @param type The AVP type
     */
    public AvpDef(AvpId id,
                  boolean mBit,
                  AvpType<T> avpType)
    {
        this.id = id;
        this.type = avpType;
        this.mBit = mBit;

    }

    /**
     * 
     * @return The AVP code and AVP vendor ID
     */
    public AvpId getId()
    {
        return this.id;
    }

    /**
     * 
     * @return The default Mandatory bit value
     */
    public boolean hasMbit()
    {
        return this.mBit;
    }

    /**
     * 
     * @return true if this AVP is vendor specific
     */
    public boolean hasVbit()
    {
        return this.id.getVendorId() != VendorId.IETEF.getId();
    }

    /**
     * 
     * @return The AVP type
     */
    public AvpType<T> getType()
    {
        return this.type;
    }

    /**
     * Create an AVP containing the given value
     * 
     * @param value The value of the newly created AVP
     * @return A constructed DiameterAvp
     */
    public DiameterAvp withValue(T value)
    {
        Objects.requireNonNull(value);
        return createAvp(value);
    }

    /**
     * Create an empty AVP. The content of the AVP has correct minimum length and
     * contains zeroes
     * 
     * @return
     */
    public DiameterAvp withValue()
    {
        return createAvp(null);
    }

    /**
     * Create an AVP having the given value. The AVP header is copied from another
     * AVP
     * 
     * @param value  The new AVP value
     * @param oldAvp An AVP of the same type, to use as template
     * @return
     */
    DiameterAvp createAvp(T value,
                          DiameterAvp oldAvp)
    {
        // Sanity checks
        Objects.requireNonNull(value);
        Objects.requireNonNull(oldAvp);
        if (!oldAvp.getDataCase().equals(this.type.dataCase))
        {
            // oldAvp is of different type
            throw new InvalidAvpValueException(oldAvp);
        }

        final var avpBuilder = oldAvp.toBuilder();
        this.type.setValue(value, avpBuilder);
        return avpBuilder.build();
    }

    private DiameterAvp createAvp(T value)
    {
        final var avpBuilder = DiameterAvp.newBuilder();
        final var avpHeader = DiameterAvpHeader.newBuilder() //
                                               .setCode(this.getId().getAvpCode())
                                               .setFlagM(this.mBit);
        // If vendor specific AVP, set V-bit and vendorId
        if (this.hasVbit())
        {
            avpHeader.setFlagV(true).setVendorId(id.getVendorId());
        }
        avpBuilder.setHeader(avpHeader.build());
        this.type.setValue(value, avpBuilder);
        return avpBuilder.build();
    }

}

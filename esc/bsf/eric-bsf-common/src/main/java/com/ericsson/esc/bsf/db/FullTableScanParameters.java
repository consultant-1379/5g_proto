/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 25, 2022
 *     Author: zpavcha
 */

package com.ericsson.esc.bsf.db;

import java.util.Objects;

/**
 * Parameters for the full table scan execution.
 */
public class FullTableScanParameters
{
    private static final String PAGE_SIZE_POSITIVE = "Page size should be a positive value";
    private static final String PAGE_THROTTLING_POSITIVE = "Page throttling should be a positive value";
    private static final String DELETE_THROTTLING_POSITIVE = "Delete throttling should be a positive value";

    private final int pageSize;
    private final long pageThrottlingMillis;
    private final long deleteThrottlingMillis;

    private FullTableScanParameters(Builder builder)
    {
        final var inputPageSize = builder.pageSize;
        final var inputPageThrottlingMillis = builder.pageThrottlingMillis;
        final var inputDeleteThrottlingMillis = builder.deleteThrottlingMillis;

        if (inputPageSize > 0)
        {
            this.pageSize = inputPageSize;
        }
        else
        {
            throw new IllegalArgumentException(PAGE_SIZE_POSITIVE);
        }

        if (inputPageThrottlingMillis > 0)
        {
            this.pageThrottlingMillis = inputPageThrottlingMillis;
        }
        else
        {
            throw new IllegalArgumentException(PAGE_THROTTLING_POSITIVE);
        }

        if (inputDeleteThrottlingMillis > 0)
        {
            this.deleteThrottlingMillis = inputDeleteThrottlingMillis;
        }
        else
        {
            throw new IllegalArgumentException(DELETE_THROTTLING_POSITIVE);
        }

    }

    /**
     * Get the pageSize for the full table scan. This corresponds to the number of
     * rows of each page for the SELECT operation in Cassandra.
     * 
     * @return The pageSize.
     */
    public int getPageSize()
    {
        return this.pageSize;
    }

    /**
     * Get the sleep duration between each page fetch (throttling).
     * 
     * @return The pageThrottlingMillis.
     */
    public long getPageThrottlingMillis()
    {
        return this.pageThrottlingMillis;
    }

    /**
     * Get the sleep duration between delete operations (throttling).
     * 
     * @return
     */
    public long getDeleteThrottlingMillis()
    {
        return this.deleteThrottlingMillis;
    }

    public static class Builder
    {
        private int pageSize;
        private long pageThrottlingMillis;
        private long deleteThrottlingMillis;

        /**
         * Set the pageSize for the full table scan. This corresponds to the number of
         * rows of each page for the SELECT operation in Cassandra.
         * 
         * @param pageSize The pageSize.
         * @return Builder The builder.
         */
        public Builder setPageSize(final int pageSize)
        {
            this.pageSize = pageSize;
            return this;
        }

        /**
         * Set the sleep duration between each page fetch (throttling).
         * 
         * @param pageThrottlingMillis The sleep duration in milliseconds.
         * @return Builder The builder.
         */
        public Builder setPageThrottlingMillis(final long pageThrottlingMillis)
        {
            this.pageThrottlingMillis = pageThrottlingMillis;
            return this;
        }

        /**
         * Set the sleep duration between each delete operation (throttling).
         * 
         * @param deleteThrottlingMillis The sleep duration in milliseconds.
         * @return Builder The builder.
         */
        public Builder setDeleteThrottlingMillis(final long deleteThrottlingMillis)
        {
            this.deleteThrottlingMillis = deleteThrottlingMillis;
            return this;
        }

        /**
         * Create the FullTableScanParameters object.
         * 
         * @return FullTableScanParameters The parameters for the full table scan
         *         execution.
         */
        public FullTableScanParameters build()
        {
            return new FullTableScanParameters(this);
        }

    }

    @Override
    public int hashCode()
    {
        return Objects.hash(deleteThrottlingMillis, pageSize, pageThrottlingMillis);
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
        FullTableScanParameters other = (FullTableScanParameters) obj;
        return deleteThrottlingMillis == other.deleteThrottlingMillis && pageSize == other.pageSize && pageThrottlingMillis == other.pageThrottlingMillis;
    }

    @Override
    public String toString()
    {
        return String.format("FullTableScanParameters [pageSize=%s, pageThrottlingMillis=%s, deleteThrottlingMillis=%s]",
                             pageSize,
                             pageThrottlingMillis,
                             deleteThrottlingMillis);
    }

}

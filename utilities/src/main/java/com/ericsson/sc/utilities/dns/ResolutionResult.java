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
 * Created on: Feb 7, 2020
 *     Author: eedala
 */

package com.ericsson.sc.utilities.dns;

import java.util.Objects;
import java.util.Optional;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ResolutionResult encapsulates the result of a resolution, i.e. the possibly
 * resolved IP address and its ResolutionState.
 * <p>
 * ResolutionState tracks the status of name resolution. It has three possible
 * states:
 * <ul>
 * <li>resolved OK: the DNS lookup succeeded and this.ipAddr contains the IP
 * address
 * <li>resolved not found: the DNS lookup was attempted but the host was not
 * founda and this.ipAddr is null
 * <li>not resolved yet: no lookup was started yet, thus we cannot have an IP
 * address, this.ipAddr is null.
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "state", "ipAddr" })
public final class ResolutionResult
{
    private static final ObjectMapper json = Jackson.om();

    public enum ResolutionState
    {
        RESOLVED_OK,
        RESOLVED_NOT_FOUND,
        NOT_RESOLVED_YET
    }

    @JsonProperty("ipAddr")
    private String ipAddr;

    @JsonProperty("state")
    private ResolutionState state;

    public ResolutionResult()
    {
        this.ipAddr = null;
        this.state = ResolutionState.NOT_RESOLVED_YET;
    }

    public ResolutionResult(String value,
                            ResolutionState state)
    {
        this.ipAddr = value;
        this.state = state;
    }

    /**
     * Factory method to build a result that resolved ok and sets the value / IP
     * address
     *
     * @param val the value to set
     * @return a new initialized ResolutionResult object
     */
    @JsonIgnore
    public static ResolutionResult resolvedOk(String val)
    {
        return new ResolutionResult(val, ResolutionState.RESOLVED_OK);
    }

    /**
     * Factory method to build a result that failed to look up the hostname.
     *
     * @return a new initialized ResolutionResult object
     */
    @JsonIgnore
    public static ResolutionResult resolvedNotFound()
    {
        return new ResolutionResult(null, ResolutionState.RESOLVED_NOT_FOUND);
    }

    /**
     * Factory method to build a result that did not even start to look up the
     * hostname.
     *
     * @return a new initialized ResolutionResult object
     */
    @JsonIgnore
    public static ResolutionResult notResolvedYet()
    {
        return new ResolutionResult(null, ResolutionState.NOT_RESOLVED_YET);
    }

    /**
     * Gets the current value. It is null for RESOLVED_NOT_FOUND and
     * NOT_RESOLVED_YET.
     *
     * @return the value, may be null
     */
    @JsonIgnore
    public String get()
    {
        return this.ipAddr;
    }

    @JsonIgnore
    public boolean isResolvedOk()
    {
        return this.state == ResolutionState.RESOLVED_OK;
    }

    @JsonIgnore
    public boolean isResolvedNotFound()
    {
        return this.state == ResolutionState.RESOLVED_NOT_FOUND;
    }

    @JsonIgnore
    public boolean isNotResolvedYet()
    {
        return this.state == ResolutionState.NOT_RESOLVED_YET;
    }

    @JsonIgnore
    public Optional<String> toOptional()
    {
        return Optional.ofNullable(this.ipAddr);
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

        ResolutionResult other = (ResolutionResult) obj;

        return Objects.equals(this.ipAddr, other.ipAddr) && Objects.equals(this.state, other.state);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.ipAddr, this.state);
    }

    @Override
    public String toString()
    {
        try
        {
            return json.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return e.toString();
        }
    }
}

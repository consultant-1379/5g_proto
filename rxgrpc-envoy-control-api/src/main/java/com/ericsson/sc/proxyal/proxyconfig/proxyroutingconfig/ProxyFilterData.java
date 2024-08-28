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
 * Created on: Dec 16, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.Objects;
import java.util.Optional;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterData;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterData.Builder;

/**
 * 
 */
public class ProxyFilterData
{
    private final String name;
    // destination can be one of:
    private final Optional<String> variableName;
    private final Optional<String> extractorRegex;
    // source can be one of:
    private final Optional<Boolean> path;
    private final Optional<String> header;
    private final Optional<String> reqHeader;
    private final Optional<String> respHeader;
    private final Optional<String> bodyJsonPath;
    private final int namespace;

    public ProxyFilterData(String name,
                           String variableName,
                           String extractorRegex,
                           Boolean path,
                           String bodyJsonPath,
                           String header,
                           String reqHeader,
                           String respHeader,
                           int namespace)
    {
        this.name = name;
        this.variableName = Optional.ofNullable(variableName);
        this.extractorRegex = Optional.ofNullable(extractorRegex);
        this.path = Optional.ofNullable(path);
        this.bodyJsonPath = Optional.ofNullable(bodyJsonPath);
        this.header = Optional.ofNullable(header);
        this.reqHeader = Optional.ofNullable(reqHeader);
        this.respHeader = Optional.ofNullable(respHeader);
        this.namespace = namespace;
    }

    public ProxyFilterData(String name,
                           String variableName,
                           String extractorRegex,
                           Boolean path,
                           String bodyJsonPath,
                           String header,
                           String reqHeader,
                           String respHeader)
    {
        this(name, variableName, extractorRegex, path, bodyJsonPath, header, reqHeader, respHeader, 0);
    }

    public ProxyFilterData(ProxyFilterData anotherFilterData)
    {
        this.name = anotherFilterData.name;
        this.variableName = anotherFilterData.variableName;
        this.extractorRegex = anotherFilterData.extractorRegex;
        this.path = anotherFilterData.path;
        this.bodyJsonPath = anotherFilterData.bodyJsonPath;
        this.header = anotherFilterData.header;
        this.reqHeader = anotherFilterData.reqHeader;
        this.respHeader = anotherFilterData.respHeader;
        this.namespace = anotherFilterData.namespace;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the variableName
     */
    public Optional<String> getVariableName()
    {
        return variableName;
    }

    /**
     * @return the extractorRegex
     */
    public Optional<String> getExtractorRegex()
    {
        return extractorRegex;
    }

    /**
     * @return the path
     */
    public Optional<Boolean> getPath()
    {
        return path;
    }

    /**
     * @return the header
     */
    public Optional<String> getHeader()
    {
        return header;
    }

    /**
     * @return the reqHeader
     */
    public Optional<String> getReqHeader()
    {
        return reqHeader;
    }

    /**
     * @return the respHeader
     */
    public Optional<String> getRespHeader()
    {
        return respHeader;
    }

    /**
     * @return the bodyJsonPath
     */
    public Optional<String> getBodyJsonPath()
    {
        return bodyJsonPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyFilterData [name=" + name + ", variableName=" + variableName + ", extractorRegex=" + extractorRegex + ", path=" + path + ", header="
               + header + ", reqHeader=" + reqHeader + ", respHeader=" + respHeader + ", bodyJsonPath=" + bodyJsonPath + ", namespace=" + namespace + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(bodyJsonPath, extractorRegex, header, name, path, reqHeader, respHeader, variableName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyFilterData other = (ProxyFilterData) obj;
        return Objects.equals(bodyJsonPath, other.bodyJsonPath) && Objects.equals(extractorRegex, other.extractorRegex) && Objects.equals(header, other.header)
               && Objects.equals(name, other.name) && Objects.equals(path, other.path) && Objects.equals(reqHeader, other.reqHeader)
               && Objects.equals(respHeader, other.respHeader) && Objects.equals(variableName, other.variableName)
               && Objects.equals(namespace, other.namespace);
    }

    /**
     * @param ProxyFilterData object
     * @param FilterData      Builder
     * @return
     */
    public Builder initBuilder()
    {
        var fdBuilder = FilterData.newBuilder().setName(this.name);

        this.path.ifPresent(fdBuilder::setPath);
        this.header.ifPresent(fdBuilder::setHeader);
        this.reqHeader.ifPresent(fdBuilder::setRequestHeader);
        this.respHeader.ifPresent(fdBuilder::setResponseHeader);
        this.bodyJsonPath.ifPresent(fdBuilder::setBodyJsonPointer);
        this.variableName.ifPresent(fdBuilder::setVariableName);
        this.extractorRegex.ifPresent(fdBuilder::setExtractorRegex);
        fdBuilder.setNameSpaceValue(this.namespace);
        return fdBuilder;
    }
}

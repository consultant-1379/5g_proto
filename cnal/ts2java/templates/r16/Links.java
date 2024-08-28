/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 23, 2019
 *     Author: eedstl
 */

package com.ericsson.cnal.openapi.r17.nnrf.nfmanagement;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.cnal.openapi.r17.commondata.Link;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2019-05-07T13:42:46.835249+02:00[Europe/Berlin]")
public class Links
{
    @JsonProperty("self")
    private Link self;

    @JsonProperty("item")
    private List<Link> linkList = new ArrayList<>();

    public Links self(Link self)
    {
        this.self = self;
        return this;
    }

    /**
    * Get heartBeatTimer
    * @return heartBeatTimer
    **/
    @ApiModelProperty(value = "")
    public Link getSelf()
    {
        return this.self;
    }

    public void setSelf(Link self)
    {
        this.self = self;
    }

    public Links item(List<Link> linkList)
    {
        this.linkList = linkList;
        return this;
    }

    public Links addItemItem(Link linkListItem)
    {
        if (this.linkList == null)
        {
            this.linkList = new ArrayList<Link>();
        }
        this.linkList.add(linkListItem);
        return this;
    }

    /**
    * Get linkList
    * @return linkList
    **/
    @ApiModelProperty(value = "")
    public List<Link> getItem()
    {
        return linkList;
    }

    public void setItem(List<Link> linkList)
    {
        this.linkList = linkList;
    }
}

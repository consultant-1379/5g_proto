
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
 * Created on: Aug 23, 2023
 *     Author: zpitgio
 */

package com.ericsson.sc.sepp.config;

import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.sc.sepp.model.CustomFqdnLocator;
import com.ericsson.sc.sepp.model.RequestMessage;
import com.ericsson.sc.sepp.model.ResponseMessage;
import com.ericsson.sc.sepp.model.SearchInHeader;
import com.ericsson.sc.sepp.model.SearchInMessageBody;
import com.ericsson.sc.sepp.model.SearchInQueryParameter;
import com.ericsson.sc.sepp.model.CustomFqdnLocator.HttpMethod;
import com.ericsson.sc.sepp.model.CustomFqdnLocator.MessageOrigin;
import com.ericsson.utilities.common.Triplet;
import java.util.List;

/**
 * 
 */
public class TphMessageLocatorBuilder
{
    private CustomFqdnLocator customFqdnLocator;
    private RequestMessage requestMessage;
    private ResponseMessage responseMessage;

    public TphMessageLocatorBuilder()
    {
        this.customFqdnLocator = new CustomFqdnLocator();
        this.requestMessage = new RequestMessage();
        this.responseMessage = new ResponseMessage();
    }

    public void createMessageSelector(String name,
                                      String serviceName,
                                      String serviceVersion,
                                      Boolean notificationMessage,
                                      HttpMethod httpMethod,
                                      String resource,
                                      MessageOrigin messageOrigin)
    {
        this.customFqdnLocator.withName(name)
                              .withServiceName(serviceName)
                              .withServiceVersion(serviceVersion)
                              .withNotificationMessage(notificationMessage)
                              .withHttpMethod(httpMethod)
                              .withResource(resource)
                              .withMessageOrigin(messageOrigin);

    }

    public void createQueryParameterLocator(Set<Triplet<String, String, SearchInQueryParameter.PerformActionOnAttribute>> queryParamaterSet)
    {
        var searchInqueryParameterSet = queryParamaterSet.stream()
                                                         .map(triplet -> new SearchInQueryParameter().withQueryParameter(triplet.getFirst())
                                                                                                     .withMatchingCondition(triplet.getSecond())
                                                                                                     .withPerformActionOnAttribute(triplet.getThird()))
                                                         .collect(Collectors.toList());
        this.requestMessage.withSearchInQueryParameter(searchInqueryParameterSet);

    }

    public void createRequestHeaderLocator(Set<Triplet<String, String, SearchInHeader.PerformActionOnAttribute>> headerSet)
    {

        var searchInHeaderSet = createHeaderLocator(headerSet);
        this.requestMessage.withSearchInHeader(searchInHeaderSet);
    }

    public void createResponseHeaderLocator(Set<Triplet<String, String, SearchInHeader.PerformActionOnAttribute>> headerSet)
    {

        var searchInHeaderSet = createHeaderLocator(headerSet);
        this.responseMessage.withSearchInHeader(searchInHeaderSet);
    }

    public void createRequestMessageBodyLocator(Set<Triplet<String, String, SearchInMessageBody.PerformActionOnAttribute>> messageBodySet)
    {
        var searchInMessageBodySet = createMessageBodyLocator(messageBodySet);
        this.requestMessage.withSearchInMessageBody(searchInMessageBodySet);
    }

    public void createResponseMessageBodyLocator(Set<Triplet<String, String, SearchInMessageBody.PerformActionOnAttribute>> messageBodySet)
    {
        var searchInMessageBodySet = createMessageBodyLocator(messageBodySet);
        this.responseMessage.withSearchInMessageBody(searchInMessageBodySet);
    }

    public CustomFqdnLocator buildMessageLocator()
    {
        return this.customFqdnLocator.withRequestMessage(this.requestMessage).withResponseMessage(this.responseMessage);

    }

    private List<SearchInHeader> createHeaderLocator(Set<Triplet<String, String, SearchInHeader.PerformActionOnAttribute>> headerSet)
    {

        return headerSet.stream()
                        .map(triplet -> new SearchInHeader().withHeader(triplet.getFirst())
                                                            .withMatchingCondition(triplet.getSecond())
                                                            .withPerformActionOnAttribute(triplet.getThird()))
                        .collect(Collectors.toList());
    }

    private List<SearchInMessageBody> createMessageBodyLocator(Set<Triplet<String, String, SearchInMessageBody.PerformActionOnAttribute>> messageBodySet)
    {
        return messageBodySet.stream()
                             .map(triplet -> new SearchInMessageBody().withBodyJsonPointer(triplet.getFirst())
                                                                      .withMatchingCondition(triplet.getSecond())
                                                                      .withPerformActionOnAttribute(triplet.getThird()))
                             .collect(Collectors.toList());
    }

    public void createRequestMessageDataRef(List<String> requestMessageDataName)
    {
        this.requestMessage.withMessageDataRef(requestMessageDataName);
    }

    public void createResponseMessageDataRef(List<String> requestMessageDataName)
    {
        this.responseMessage.withMessageDataRef(requestMessageDataName);
    }
}

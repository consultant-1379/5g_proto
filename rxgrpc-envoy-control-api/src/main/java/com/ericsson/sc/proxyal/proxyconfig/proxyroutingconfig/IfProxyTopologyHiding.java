package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningRule;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding.Builder;

/**
 * 
 */
public interface IfProxyTopologyHiding
{

    public IfProxyTopologyHiding clone();

    public Builder initBuilder();

    public Builder initBuilder(Builder builder);

    public static class Selector
    {
        private String name;
        private Optional<String> serviceName;
        private Optional<String> serviceVersion;
        private Optional<String> httpMethod;
        private Boolean notificationMessage;
        private Optional<String> resource;
        private Integer messageOrigin;
        private Integer isResponse;

        public Selector selector()
        {
            return this;
        }

        public String getName()
        {
            return name;
        }

        public Selector withName(String name)
        {
            this.name = name;
            return this;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Optional<String> getServiceName()
        {
            return serviceName;
        }

        public void setServiceName(Optional<String> serviceName)
        {
            this.serviceName = serviceName;
        }

        public Selector withServiceName(Optional<String> serviceName)
        {
            this.serviceName = serviceName;
            return this;
        }

        public Optional<String> getServiceVersion()
        {
            return serviceVersion;
        }

        public void setServiceVersion(Optional<String> serviceVersion)
        {
            this.serviceVersion = serviceVersion;
        }

        public Selector withServiceVersion(Optional<String> serviceVersion)
        {
            this.serviceVersion = serviceVersion;
            return this;
        }

        public Boolean getNotificationMessage()
        {
            return notificationMessage;
        }

        public void setNotificationMessage(Boolean notificationMessage)
        {
            this.notificationMessage = notificationMessage;
        }

        public Selector withNotificationMessage(Boolean notificationMessage)
        {
            this.notificationMessage = notificationMessage;
            return this;
        }

        public Optional<String> getHttpMethod()
        {
            return httpMethod;
        }

        public void setHttpMethod(Optional<String> httpMethod)
        {
            this.httpMethod = httpMethod;
        }

        public Selector withHttpMethod(Optional<String> httpMethod)
        {
            this.httpMethod = httpMethod;
            return this;
        }

        public Integer getMessageOrigin()
        {
            return messageOrigin;
        }

        public void setMessageOrigin(Integer messageOrigin)
        {
            this.messageOrigin = messageOrigin;
        }

        public Selector withMessageOrigin(Integer messageOrigin)
        {
            this.messageOrigin = messageOrigin;
            return this;
        }

        public Optional<String> getResource()
        {
            return resource;
        }

        public void setResource(Optional<String> resource)
        {
            this.resource = resource;
        }

        public Selector withResource(Optional<String> resource)
        {
            this.resource = resource;
            return this;
        }

        public Integer getIsResponse()
        {
            return isResponse;
        }

        public void setIsResponse(Integer isResponse)
        {
            this.isResponse = isResponse;
        }

        public Selector withIsResponse(Integer resp)
        {
            this.isResponse = resp;
            return this;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
            {
                return true;
            }
            if (!(other instanceof Selector))
            {
                return false;
            }
            Selector sel = (Selector) other;
            return Objects.equals(this.getServiceName(), (sel.getServiceName())) && Objects.equals(this.getServiceVersion(), ((sel).getServiceVersion()))
                   && Objects.equals(this.getHttpMethod(), ((sel).getHttpMethod()))
                   && Objects.equals(this.getNotificationMessage(), ((sel).getNotificationMessage()))
                   && Objects.equals(this.getResource(), ((sel).getResource())) && Objects.equals(this.getMessageOrigin(), ((sel).getMessageOrigin()))
                   && Objects.equals(this.getIsResponse(), (sel.getIsResponse()));
        }

        @Override
        public int hashCode()
        {
            var result = 1;
            result = ((result * 31) + ((this.getServiceVersion() == null) ? 0 : this.getServiceVersion().hashCode()));
            result = ((result * 31) + ((this.getMessageOrigin() == null) ? 0 : this.getMessageOrigin().hashCode()));
            result = ((result * 31) + ((this.getResource() == null) ? 0 : this.getResource().hashCode()));
            result = ((result * 31) + ((this.getNotificationMessage() == null) ? 0 : this.getNotificationMessage().hashCode()));
            result = ((result * 31) + ((this.getServiceName() == null) ? 0 : this.getServiceName().hashCode()));
            result = ((result * 31) + ((this.getHttpMethod() == null) ? 0 : this.getHttpMethod().hashCode()));
            return result;
        }

    }

    public static class Locator
    {
        private List<ProxyScreeningRule> hidingProxyScreeningRules = new ArrayList<>();
        private List<ProxyScreeningRule> unHidingProxyScreeningRules = new ArrayList<>();
        private List<ProxyFilterData> messageData = new ArrayList<>();

        public Locator locator()
        {
            return this;
        }

        public List<ProxyScreeningRule> getUnHidingProxyScreeningRules()
        {
            return unHidingProxyScreeningRules;
        }

        public void setUnHidingProxyScreeningRules(List<ProxyScreeningRule> list)
        {
            this.unHidingProxyScreeningRules = list;
        }

        public void addUnHidingProxyScreeningRules(ProxyScreeningRule rule)
        {
            this.unHidingProxyScreeningRules.add(rule);
        }

        public Locator withUnHidingProxyScreeningRules(List<ProxyScreeningRule> list)
        {
            this.unHidingProxyScreeningRules = list;
            return this;
        }

        public List<ProxyScreeningRule> getHidingProxyScreeningRules()
        {
            return hidingProxyScreeningRules;
        }

        public void setHidingProxyScreeningRules(List<ProxyScreeningRule> list)
        {
            this.hidingProxyScreeningRules = list;
        }

        public void addHidingProxyScreeningRules(ProxyScreeningRule rule)
        {
            this.hidingProxyScreeningRules.add(rule);
        }

        public Locator withHidingProxyScreeningRules(List<ProxyScreeningRule> list)
        {
            this.hidingProxyScreeningRules = list;
            return this;
        }

        public List<ProxyFilterData> getMessageData()
        {
            return messageData;
        }

        public void setMessageData(List<ProxyFilterData> messageData)
        {
            this.messageData = messageData;
        }

        public Locator withMessageData(List<ProxyFilterData> messageData)
        {
            this.messageData = messageData;
            return this;
        }

    }

}
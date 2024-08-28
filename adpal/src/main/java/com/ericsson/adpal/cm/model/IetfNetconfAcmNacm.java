
package com.ericsson.adpal.cm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Parameters for NETCONF Access Control Model.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "enable-nacm",
                     "read-default",
                     "write-default",
                     "exec-default",
                     "enable-external-groups",
                     "denied-operations",
                     "denied-data-writes",
                     "denied-notifications",
                     "groups",
                     "rule-list" })
public class IetfNetconfAcmNacm
{

    /**
     * Enables or disables all NETCONF access control enforcement. If 'true', then
     * enforcement is enabled. If 'false', then enforcement is disabled.
     * 
     */
    @JsonProperty("enable-nacm")
    @JsonPropertyDescription("Enables or disables all NETCONF access control enforcement. If 'true', then enforcement is enabled. If 'false', then enforcement is disabled.")
    private Boolean enableNacm = true;
    /**
     * Controls whether read access is granted if no appropriate rule is found for a
     * particular read request.
     * 
     */
    @JsonProperty("read-default")
    @JsonPropertyDescription("Controls whether read access is granted if no appropriate rule is found for a particular read request.")
    private IetfNetconfAcmNacm.ReadDefault readDefault = IetfNetconfAcmNacm.ReadDefault.fromValue("deny");
    /**
     * Controls whether create, update, or delete access is granted if no
     * appropriate rule is found for a particular write request.
     * 
     */
    @JsonProperty("write-default")
    @JsonPropertyDescription("Controls whether create, update, or delete access is granted if no appropriate rule is found for a particular write request.")
    private IetfNetconfAcmNacm.WriteDefault writeDefault = IetfNetconfAcmNacm.WriteDefault.fromValue("deny");
    /**
     * Controls whether exec access is granted if no appropriate rule is found for a
     * particular protocol operation request.
     * 
     */
    @JsonProperty("exec-default")
    @JsonPropertyDescription("Controls whether exec access is granted if no appropriate rule is found for a particular protocol operation request.")
    private IetfNetconfAcmNacm.ExecDefault execDefault = IetfNetconfAcmNacm.ExecDefault.fromValue("deny");
    /**
     * Controls whether the server uses the groups reported by the NETCONF transport
     * layer when it assigns the user to a set of NACM groups. If this leaf has the
     * value 'false', any group names reported by the transport layer are ignored by
     * the server.
     * 
     */
    @JsonProperty("enable-external-groups")
    @JsonPropertyDescription("Controls whether the server uses the groups reported by the NETCONF transport layer when it assigns the user to a set of NACM groups. If this leaf has the value 'false', any group names reported by the transport layer are ignored by the server.")
    private Boolean enableExternalGroups = true;
    /**
     * Number of times since the server last restarted that a protocol operation
     * request was denied.
     * 
     */
    @JsonProperty("denied-operations")
    @JsonPropertyDescription("Number of times since the server last restarted that a protocol operation request was denied.")
    private Integer deniedOperations;
    /**
     * Number of times since the server last restarted that a protocol operation
     * request to alter a configuration datastore was denied.
     * 
     */
    @JsonProperty("denied-data-writes")
    @JsonPropertyDescription("Number of times since the server last restarted that a protocol operation request to alter a configuration datastore was denied.")
    private Integer deniedDataWrites;
    /**
     * Number of times since the server last restarted that a notification was
     * dropped for a subscription because access to the event type was denied.
     * 
     */
    @JsonProperty("denied-notifications")
    @JsonPropertyDescription("Number of times since the server last restarted that a notification was dropped for a subscription because access to the event type was denied.")
    private Integer deniedNotifications;
    /**
     * NETCONF Access Control Groups.
     * 
     */
    @JsonProperty("groups")
    @JsonPropertyDescription("NETCONF Access Control Groups.")
    private Groups groups;
    /**
     * An ordered collection of access control rules.
     * 
     */
    @JsonProperty("rule-list")
    @JsonPropertyDescription("An ordered collection of access control rules.")
    private List<Rule> ruleList = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public IetfNetconfAcmNacm()
    {
    }

    /**
     * 
     * @param writeDefault
     * @param deniedOperations
     * @param enableNacm
     * @param enableExternalGroups
     * @param ruleList
     * @param execDefault
     * @param groups
     * @param readDefault
     * @param deniedNotifications
     * @param deniedDataWrites
     */
    public IetfNetconfAcmNacm(Boolean enableNacm,
                              IetfNetconfAcmNacm.ReadDefault readDefault,
                              IetfNetconfAcmNacm.WriteDefault writeDefault,
                              IetfNetconfAcmNacm.ExecDefault execDefault,
                              Boolean enableExternalGroups,
                              Integer deniedOperations,
                              Integer deniedDataWrites,
                              Integer deniedNotifications,
                              Groups groups,
                              List<Rule> ruleList)
    {
        super();
        this.enableNacm = enableNacm;
        this.readDefault = readDefault;
        this.writeDefault = writeDefault;
        this.execDefault = execDefault;
        this.enableExternalGroups = enableExternalGroups;
        this.deniedOperations = deniedOperations;
        this.deniedDataWrites = deniedDataWrites;
        this.deniedNotifications = deniedNotifications;
        this.groups = groups;
        this.ruleList = ruleList;
    }

    /**
     * Enables or disables all NETCONF access control enforcement. If 'true', then
     * enforcement is enabled. If 'false', then enforcement is disabled.
     * 
     */
    @JsonProperty("enable-nacm")
    public Optional<Boolean> getEnableNacm()
    {
        return Optional.ofNullable(enableNacm);
    }

    /**
     * Enables or disables all NETCONF access control enforcement. If 'true', then
     * enforcement is enabled. If 'false', then enforcement is disabled.
     * 
     */
    @JsonProperty("enable-nacm")
    public void setEnableNacm(Boolean enableNacm)
    {
        this.enableNacm = enableNacm;
    }

    public IetfNetconfAcmNacm withEnableNacm(Boolean enableNacm)
    {
        this.enableNacm = enableNacm;
        return this;
    }

    /**
     * Controls whether read access is granted if no appropriate rule is found for a
     * particular read request.
     * 
     */
    @JsonProperty("read-default")
    public Optional<IetfNetconfAcmNacm.ReadDefault> getReadDefault()
    {
        return Optional.ofNullable(readDefault);
    }

    /**
     * Controls whether read access is granted if no appropriate rule is found for a
     * particular read request.
     * 
     */
    @JsonProperty("read-default")
    public void setReadDefault(IetfNetconfAcmNacm.ReadDefault readDefault)
    {
        this.readDefault = readDefault;
    }

    public IetfNetconfAcmNacm withReadDefault(IetfNetconfAcmNacm.ReadDefault readDefault)
    {
        this.readDefault = readDefault;
        return this;
    }

    /**
     * Controls whether create, update, or delete access is granted if no
     * appropriate rule is found for a particular write request.
     * 
     */
    @JsonProperty("write-default")
    public Optional<IetfNetconfAcmNacm.WriteDefault> getWriteDefault()
    {
        return Optional.ofNullable(writeDefault);
    }

    /**
     * Controls whether create, update, or delete access is granted if no
     * appropriate rule is found for a particular write request.
     * 
     */
    @JsonProperty("write-default")
    public void setWriteDefault(IetfNetconfAcmNacm.WriteDefault writeDefault)
    {
        this.writeDefault = writeDefault;
    }

    public IetfNetconfAcmNacm withWriteDefault(IetfNetconfAcmNacm.WriteDefault writeDefault)
    {
        this.writeDefault = writeDefault;
        return this;
    }

    /**
     * Controls whether exec access is granted if no appropriate rule is found for a
     * particular protocol operation request.
     * 
     */
    @JsonProperty("exec-default")
    public Optional<IetfNetconfAcmNacm.ExecDefault> getExecDefault()
    {
        return Optional.ofNullable(execDefault);
    }

    /**
     * Controls whether exec access is granted if no appropriate rule is found for a
     * particular protocol operation request.
     * 
     */
    @JsonProperty("exec-default")
    public void setExecDefault(IetfNetconfAcmNacm.ExecDefault execDefault)
    {
        this.execDefault = execDefault;
    }

    public IetfNetconfAcmNacm withExecDefault(IetfNetconfAcmNacm.ExecDefault execDefault)
    {
        this.execDefault = execDefault;
        return this;
    }

    /**
     * Controls whether the server uses the groups reported by the NETCONF transport
     * layer when it assigns the user to a set of NACM groups. If this leaf has the
     * value 'false', any group names reported by the transport layer are ignored by
     * the server.
     * 
     */
    @JsonProperty("enable-external-groups")
    public Optional<Boolean> getEnableExternalGroups()
    {
        return Optional.ofNullable(enableExternalGroups);
    }

    /**
     * Controls whether the server uses the groups reported by the NETCONF transport
     * layer when it assigns the user to a set of NACM groups. If this leaf has the
     * value 'false', any group names reported by the transport layer are ignored by
     * the server.
     * 
     */
    @JsonProperty("enable-external-groups")
    public void setEnableExternalGroups(Boolean enableExternalGroups)
    {
        this.enableExternalGroups = enableExternalGroups;
    }

    public IetfNetconfAcmNacm withEnableExternalGroups(Boolean enableExternalGroups)
    {
        this.enableExternalGroups = enableExternalGroups;
        return this;
    }

    /**
     * Number of times since the server last restarted that a protocol operation
     * request was denied.
     * 
     */
    @JsonProperty("denied-operations")
    public Optional<Integer> getDeniedOperations()
    {
        return Optional.ofNullable(deniedOperations);
    }

    /**
     * Number of times since the server last restarted that a protocol operation
     * request was denied.
     * 
     */
    @JsonProperty("denied-operations")
    public void setDeniedOperations(Integer deniedOperations)
    {
        this.deniedOperations = deniedOperations;
    }

    public IetfNetconfAcmNacm withDeniedOperations(Integer deniedOperations)
    {
        this.deniedOperations = deniedOperations;
        return this;
    }

    /**
     * Number of times since the server last restarted that a protocol operation
     * request to alter a configuration datastore was denied.
     * 
     */
    @JsonProperty("denied-data-writes")
    public Optional<Integer> getDeniedDataWrites()
    {
        return Optional.ofNullable(deniedDataWrites);
    }

    /**
     * Number of times since the server last restarted that a protocol operation
     * request to alter a configuration datastore was denied.
     * 
     */
    @JsonProperty("denied-data-writes")
    public void setDeniedDataWrites(Integer deniedDataWrites)
    {
        this.deniedDataWrites = deniedDataWrites;
    }

    public IetfNetconfAcmNacm withDeniedDataWrites(Integer deniedDataWrites)
    {
        this.deniedDataWrites = deniedDataWrites;
        return this;
    }

    /**
     * Number of times since the server last restarted that a notification was
     * dropped for a subscription because access to the event type was denied.
     * 
     */
    @JsonProperty("denied-notifications")
    public Optional<Integer> getDeniedNotifications()
    {
        return Optional.ofNullable(deniedNotifications);
    }

    /**
     * Number of times since the server last restarted that a notification was
     * dropped for a subscription because access to the event type was denied.
     * 
     */
    @JsonProperty("denied-notifications")
    public void setDeniedNotifications(Integer deniedNotifications)
    {
        this.deniedNotifications = deniedNotifications;
    }

    public IetfNetconfAcmNacm withDeniedNotifications(Integer deniedNotifications)
    {
        this.deniedNotifications = deniedNotifications;
        return this;
    }

    /**
     * NETCONF Access Control Groups.
     * 
     */
    @JsonProperty("groups")
    public Optional<Groups> getGroups()
    {
        return Optional.ofNullable(groups);
    }

    /**
     * NETCONF Access Control Groups.
     * 
     */
    @JsonProperty("groups")
    public void setGroups(Groups groups)
    {
        this.groups = groups;
    }

    public IetfNetconfAcmNacm withGroups(Groups groups)
    {
        this.groups = groups;
        return this;
    }

    /**
     * An ordered collection of access control rules.
     * 
     */
    @JsonProperty("rule-list")
    public Optional<List<Rule>> getRuleList()
    {
        return Optional.ofNullable(ruleList);
    }

    /**
     * An ordered collection of access control rules.
     * 
     */
    @JsonProperty("rule-list")
    public void setRuleList(List<Rule> ruleList)
    {
        this.ruleList = ruleList;
    }

    public IetfNetconfAcmNacm withRuleList(List<Rule> ruleList)
    {
        this.ruleList = ruleList;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IetfNetconfAcmNacm.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("enableNacm");
        sb.append('=');
        sb.append(((this.enableNacm == null) ? "<null>" : this.enableNacm));
        sb.append(',');
        sb.append("readDefault");
        sb.append('=');
        sb.append(((this.readDefault == null) ? "<null>" : this.readDefault));
        sb.append(',');
        sb.append("writeDefault");
        sb.append('=');
        sb.append(((this.writeDefault == null) ? "<null>" : this.writeDefault));
        sb.append(',');
        sb.append("execDefault");
        sb.append('=');
        sb.append(((this.execDefault == null) ? "<null>" : this.execDefault));
        sb.append(',');
        sb.append("enableExternalGroups");
        sb.append('=');
        sb.append(((this.enableExternalGroups == null) ? "<null>" : this.enableExternalGroups));
        sb.append(',');
        sb.append("deniedOperations");
        sb.append('=');
        sb.append(((this.deniedOperations == null) ? "<null>" : this.deniedOperations));
        sb.append(',');
        sb.append("deniedDataWrites");
        sb.append('=');
        sb.append(((this.deniedDataWrites == null) ? "<null>" : this.deniedDataWrites));
        sb.append(',');
        sb.append("deniedNotifications");
        sb.append('=');
        sb.append(((this.deniedNotifications == null) ? "<null>" : this.deniedNotifications));
        sb.append(',');
        sb.append("groups");
        sb.append('=');
        sb.append(((this.groups == null) ? "<null>" : this.groups));
        sb.append(',');
        sb.append("ruleList");
        sb.append('=');
        sb.append(((this.ruleList == null) ? "<null>" : this.ruleList));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.writeDefault == null) ? 0 : this.writeDefault.hashCode()));
        result = ((result * 31) + ((this.deniedOperations == null) ? 0 : this.deniedOperations.hashCode()));
        result = ((result * 31) + ((this.enableNacm == null) ? 0 : this.enableNacm.hashCode()));
        result = ((result * 31) + ((this.enableExternalGroups == null) ? 0 : this.enableExternalGroups.hashCode()));
        result = ((result * 31) + ((this.ruleList == null) ? 0 : this.ruleList.hashCode()));
        result = ((result * 31) + ((this.execDefault == null) ? 0 : this.execDefault.hashCode()));
        result = ((result * 31) + ((this.groups == null) ? 0 : this.groups.hashCode()));
        result = ((result * 31) + ((this.readDefault == null) ? 0 : this.readDefault.hashCode()));
        result = ((result * 31) + ((this.deniedNotifications == null) ? 0 : this.deniedNotifications.hashCode()));
        result = ((result * 31) + ((this.deniedDataWrites == null) ? 0 : this.deniedDataWrites.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof IetfNetconfAcmNacm) == false)
        {
            return false;
        }
        IetfNetconfAcmNacm rhs = ((IetfNetconfAcmNacm) other);
        return (((((((((((this.writeDefault == rhs.writeDefault) || ((this.writeDefault != null) && this.writeDefault.equals(rhs.writeDefault)))
                        && ((this.deniedOperations == rhs.deniedOperations)
                            || ((this.deniedOperations != null) && this.deniedOperations.equals(rhs.deniedOperations))))
                       && ((this.enableNacm == rhs.enableNacm) || ((this.enableNacm != null) && this.enableNacm.equals(rhs.enableNacm))))
                      && ((this.enableExternalGroups == rhs.enableExternalGroups)
                          || ((this.enableExternalGroups != null) && this.enableExternalGroups.equals(rhs.enableExternalGroups))))
                     && ((this.ruleList == rhs.ruleList) || ((this.ruleList != null) && this.ruleList.equals(rhs.ruleList))))
                    && ((this.execDefault == rhs.execDefault) || ((this.execDefault != null) && this.execDefault.equals(rhs.execDefault))))
                   && ((this.groups == rhs.groups) || ((this.groups != null) && this.groups.equals(rhs.groups))))
                  && ((this.readDefault == rhs.readDefault) || ((this.readDefault != null) && this.readDefault.equals(rhs.readDefault))))
                 && ((this.deniedNotifications == rhs.deniedNotifications)
                     || ((this.deniedNotifications != null) && this.deniedNotifications.equals(rhs.deniedNotifications))))
                && ((this.deniedDataWrites == rhs.deniedDataWrites)
                    || ((this.deniedDataWrites != null) && this.deniedDataWrites.equals(rhs.deniedDataWrites))));
    }

    /**
     * Controls whether exec access is granted if no appropriate rule is found for a
     * particular protocol operation request.
     * 
     */
    public enum ExecDefault
    {

        PERMIT("permit"),
        DENY("deny");

        private final String value;
        private final static Map<String, IetfNetconfAcmNacm.ExecDefault> CONSTANTS = new HashMap<String, IetfNetconfAcmNacm.ExecDefault>();

        static
        {
            for (IetfNetconfAcmNacm.ExecDefault c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private ExecDefault(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static IetfNetconfAcmNacm.ExecDefault fromValue(String value)
        {
            IetfNetconfAcmNacm.ExecDefault constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

    /**
     * Controls whether read access is granted if no appropriate rule is found for a
     * particular read request.
     * 
     */
    public enum ReadDefault
    {

        PERMIT("permit"),
        DENY("deny");

        private final String value;
        private final static Map<String, IetfNetconfAcmNacm.ReadDefault> CONSTANTS = new HashMap<String, IetfNetconfAcmNacm.ReadDefault>();

        static
        {
            for (IetfNetconfAcmNacm.ReadDefault c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private ReadDefault(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static IetfNetconfAcmNacm.ReadDefault fromValue(String value)
        {
            IetfNetconfAcmNacm.ReadDefault constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

    /**
     * Controls whether create, update, or delete access is granted if no
     * appropriate rule is found for a particular write request.
     * 
     */
    public enum WriteDefault
    {

        PERMIT("permit"),
        DENY("deny");

        private final String value;
        private final static Map<String, IetfNetconfAcmNacm.WriteDefault> CONSTANTS = new HashMap<String, IetfNetconfAcmNacm.WriteDefault>();

        static
        {
            for (IetfNetconfAcmNacm.WriteDefault c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private WriteDefault(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static IetfNetconfAcmNacm.WriteDefault fromValue(String value)
        {
            IetfNetconfAcmNacm.WriteDefault constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

}

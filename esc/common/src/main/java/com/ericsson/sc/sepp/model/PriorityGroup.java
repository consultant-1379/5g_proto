
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.glue.IfPriorityGroup;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "priority", "static-scp-instance-data-ref", "nf-match-condition", "scp-match-condition", "sepp-match-condition" })
public class PriorityGroup implements IfPriorityGroup
{

    /**
     * Name identifying the priority-group (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the priority-group")
    private String name;
    /**
     * The priority assigned to the group of NF instances, with a lower value
     * indicating higher priority (Required)
     * 
     */
    @JsonProperty("priority")
    @JsonPropertyDescription("The priority assigned to the group of NF instances, with a lower value indicating higher priority")
    private Integer priority;
    /**
     * Reference to an SCP instance data set
     * 
     */
    @JsonProperty("static-scp-instance-data-ref")
    @JsonPropertyDescription("Reference to an SCP instance data set")
    private List<String> staticScpInstanceDataRef = new ArrayList<String>();
    /**
     * Reference to a SEPP instance data set
     * 
     */
    @JsonProperty("static-sepp-instance-data-ref")
    @JsonPropertyDescription("Reference to a SEPP instance data set")
    private List<String> staticSeppInstanceDataRef = new ArrayList<String>();
    /**
     * NF instances of this nf-pool which satisfy the nf-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("nf-match-condition")
    @JsonPropertyDescription("NF instances of this nf-pool which satisfy the nf-match-condition are prioritized according to priority-group priority")
    private String nfMatchCondition;
    /**
     * SCP instances of this nf-pool which satisfy the scp-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("scp-match-condition")
    @JsonPropertyDescription("SCP instances of this nf-pool which satisfy the scp-match-condition are prioritized according to priority-group priority")
    private String scpMatchCondition;
    /**
     * SEPP instances of this nf-pool which satisfy the sepp-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("sepp-match-condition")
    @JsonPropertyDescription("SEPP instances of this nf-pool which satisfy the sepp-match-condition are prioritized according to priority-group priority")
    private String seppMatchCondition;

    /**
     * Name identifying the priority-group (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the priority-group (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public PriorityGroup withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The priority assigned to the group of NF instances, with a lower value
     * indicating higher priority (Required)
     * 
     */
    @JsonProperty("priority")
    public Integer getPriority()
    {
        return priority;
    }

    /**
     * The priority assigned to the group of NF instances, with a lower value
     * indicating higher priority (Required)
     * 
     */
    @JsonProperty("priority")
    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public PriorityGroup withPriority(Integer priority)
    {
        this.priority = priority;
        return this;
    }

    /**
     * Reference to an SCP instance data set
     * 
     */
    @JsonProperty("static-scp-instance-data-ref")
    public List<String> getStaticScpInstanceDataRef()
    {
        return staticScpInstanceDataRef;
    }

    /**
     * Reference to an SCP instance data set
     * 
     */
    @JsonProperty("static-scp-instance-data-ref")
    public void setStaticScpInstanceDataRef(List<String> staticScpInstanceDataRef)
    {
        this.staticScpInstanceDataRef = staticScpInstanceDataRef;
    }

    public PriorityGroup withStaticScpInstanceDataRef(List<String> staticScpInstanceDataRef)
    {
        this.staticScpInstanceDataRef = staticScpInstanceDataRef;
        return this;
    }

    /**
     * Reference to a SEPP instance data set
     * 
     */
    @JsonProperty("static-sepp-instance-data-ref")
    public List<String> getStaticSeppInstanceDataRef()
    {
        return staticSeppInstanceDataRef;
    }

    /**
     * Reference to a SEPP instance data set
     * 
     */
    @JsonProperty("static-sepp-instance-data-ref")
    public void setStaticSeppInstanceDataRef(List<String> staticSeppInstanceDataRef)
    {
        this.staticSeppInstanceDataRef = staticSeppInstanceDataRef;
    }

    public PriorityGroup withStaticSeppInstanceDataRef(List<String> staticSeppInstanceDataRef)
    {
        this.staticSeppInstanceDataRef = staticSeppInstanceDataRef;
        return this;
    }

    /**
     * NF instances of this nf-pool which satisfy the nf-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("nf-match-condition")
    public String getNfMatchCondition()
    {
        return nfMatchCondition;
    }

    /**
     * NF instances of this nf-pool which satisfy the nf-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("nf-match-condition")
    public void setNfMatchCondition(String nfMatchCondition)
    {
        this.nfMatchCondition = nfMatchCondition;
    }

    public PriorityGroup withNfMatchCondition(String nfMatchCondition)
    {
        this.nfMatchCondition = nfMatchCondition;
        return this;
    }

    /**
     * SCP instances of this nf-pool which satisfy the scp-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("scp-match-condition")
    public String getScpMatchCondition()
    {
        return scpMatchCondition;
    }

    /**
     * SCP instances of this nf-pool which satisfy the scp-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("scp-match-condition")
    public void setScpMatchCondition(String scpMatchCondition)
    {
        this.scpMatchCondition = scpMatchCondition;
    }

    public PriorityGroup withScpMatchCondition(String scpMatchCondition)
    {
        this.scpMatchCondition = scpMatchCondition;
        return this;
    }

    /**
     * SEPP instances of this nf-pool which satisfy the sepp-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("sepp-match-condition")
    public String getSeppMatchCondition()
    {
        return seppMatchCondition;
    }

    /**
     * SEPP instances of this nf-pool which satisfy the sepp-match-condition are
     * prioritized according to priority-group priority
     * 
     */
    @JsonProperty("sepp-match-condition")
    public void setSeppMatchCondition(String seppMatchCondition)
    {
        this.seppMatchCondition = seppMatchCondition;
    }

    public PriorityGroup withSeppMatchCondition(String seppMatchCondition)
    {
        this.seppMatchCondition = seppMatchCondition;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PriorityGroup.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("priority");
        sb.append('=');
        sb.append(((this.priority == null) ? "<null>" : this.priority));
        sb.append(',');
        sb.append("staticScpInstanceDataRef");
        sb.append('=');
        sb.append(((this.staticScpInstanceDataRef == null) ? "<null>" : this.staticScpInstanceDataRef));
        sb.append(',');
        sb.append("nfMatchCondition");
        sb.append('=');
        sb.append(((this.nfMatchCondition == null) ? "<null>" : this.nfMatchCondition));
        sb.append(',');
        sb.append("scpMatchCondition");
        sb.append('=');
        sb.append(((this.scpMatchCondition == null) ? "<null>" : this.scpMatchCondition));
        sb.append(',');
        sb.append("seppMatchCondition");
        sb.append('=');
        sb.append(((this.seppMatchCondition == null) ? "<null>" : this.seppMatchCondition));
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
        result = ((result * 31) + ((this.seppMatchCondition == null) ? 0 : this.seppMatchCondition.hashCode()));
        result = ((result * 31) + ((this.scpMatchCondition == null) ? 0 : this.scpMatchCondition.hashCode()));
        result = ((result * 31) + ((this.staticScpInstanceDataRef == null) ? 0 : this.staticScpInstanceDataRef.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.nfMatchCondition == null) ? 0 : this.nfMatchCondition.hashCode()));
        result = ((result * 31) + ((this.priority == null) ? 0 : this.priority.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PriorityGroup) == false)
        {
            return false;
        }
        PriorityGroup rhs = ((PriorityGroup) other);
        return (((((((this.seppMatchCondition == rhs.seppMatchCondition)
                     || ((this.seppMatchCondition != null) && this.seppMatchCondition.equals(rhs.seppMatchCondition)))
                    && ((this.scpMatchCondition == rhs.scpMatchCondition)
                        || ((this.scpMatchCondition != null) && this.scpMatchCondition.equals(rhs.scpMatchCondition))))
                   && ((this.staticScpInstanceDataRef == rhs.staticScpInstanceDataRef)
                       || ((this.staticScpInstanceDataRef != null) && this.staticScpInstanceDataRef.equals(rhs.staticScpInstanceDataRef))))
                  && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                 && ((this.nfMatchCondition == rhs.nfMatchCondition)
                     || ((this.nfMatchCondition != null) && this.nfMatchCondition.equals(rhs.nfMatchCondition))))
                && ((this.priority == rhs.priority) || ((this.priority != null) && this.priority.equals(rhs.priority))));
    }

}

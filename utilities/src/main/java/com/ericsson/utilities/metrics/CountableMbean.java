/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 29, 2018
 *     Author: eedstl
 */

package com.ericsson.utilities.metrics;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountableMbean implements DynamicMBean
{
    private static final Logger log = LoggerFactory.getLogger(CountableMbean.class);

    private final MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[1];
    private final MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[1];
    private final MBeanOperationInfo[] operations = new MBeanOperationInfo[1];
    private final String className;

    private MBeanInfo mbeanInfo = null;
    private Countable count;

    public CountableMbean(final Countable count) throws Exception
    {
        this.count = count;
        this.className = this.getClass().getName();
        this.buildDynamicMBeanInfo();
        ObjectName name = new ObjectName("com.ericsson.utilities.metrics:type=Counter,name=" + this.count.id().replace(":", "-"));
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        // Check attributeName to avoid NullPointerException later on
        if (attributeName == null)
        {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"),
                                                 "Cannot invoke a getter of " + this.className + " with null attribute name");
        }

        // Call the corresponding getter for a recognized attributeName
        if (attributeName.equals("Value"))
        {
            return this.count.get();
        }

        // If attributeName has not been recognized
        throw (new AttributeNotFoundException("Cannot find " + attributeName + " attribute in " + this.className));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
     */
    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        if (attribute == null)
        {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute cannot be null"),
                                                 "Cannot invoke a setter of " + this.className + " with null attribute");
        }

        String name = attribute.getName();
        Object value = attribute.getValue();

        if (name.equals("Value") && value != null)
        {
            this.count.set((Double) value);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    @Override
    public AttributeList getAttributes(String[] attributeNames)
    {
        // Check attributeNames to avoid NullPointerException later on
        if (attributeNames == null)
        {
            throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames[] cannot be null"),
                                                 "Cannot invoke a getter of " + this.className);
        }

        AttributeList resultList = new AttributeList();

        // if attributeNames is empty, return an empty result list
        if (attributeNames.length == 0)
            return resultList;

        // build the result attribute list
        for (int i = 0; i < attributeNames.length; i++)
        {
            try
            {
                Object value = getAttribute(attributeNames[i]);
                resultList.add(new Attribute(attributeNames[i], value));
            }
            catch (Exception e)
            {
                // print debug info but continue processing list
                log.error("Exception caught: ", e);
            }
        }

        return (resultList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
     */
    @Override
    public AttributeList setAttributes(AttributeList attributes)
    {
        // Check attributes to avoid NullPointerException later on
        if (attributes == null)
        {
            throw new RuntimeOperationsException(new IllegalArgumentException("AttributeList attributes cannot be null"),
                                                 "Cannot invoke a setter of " + this.className);
        }

        AttributeList resultList = new AttributeList();

        // If attributeNames is empty, nothing more to do
        if (attributes.isEmpty())
            return resultList;

        // Try to set each attribute and add to result list if successful
        for (Iterator<?> i = attributes.iterator(); i.hasNext();)
        {
            Attribute attr = (Attribute) i.next();

            try
            {
                this.setAttribute(attr);
                String name = attr.getName();
                Object value = this.getAttribute(name);
                resultList.add(new Attribute(name, value));
            }
            catch (Exception e)
            {
                // print debug info but keep processing list
                log.error("Exception caught: ", e);
            }
        }

        return (resultList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.management.DynamicMBean#invoke(java.lang.String,
     * java.lang.Object[], java.lang.String[])
     */
    @Override
    public Object invoke(String actionName,
                         Object[] params,
                         String[] signature) throws MBeanException, ReflectionException
    {
        // Check operationName to avoid NullPointerException later on
        if (actionName == null)
        {
            throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"),
                                                 "Cannot invoke a null operation in " + this.className);
        }

        // Call the corresponding operation for a recognized name
        if (actionName.equals("reset"))
        {
            // this code is specific to the internal "reset" method:
            reset(); // no parameters to check
            return null; // and no return value
        }
        else
        {
            // unrecognized operation name:
            throw new ReflectionException(new NoSuchMethodException(actionName), "Cannot find the operation " + actionName + " in " + this.className);
        }
    }

    // internal method for implementing the reset operation
    public void reset()
    {
        this.count.set(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    @Override
    public MBeanInfo getMBeanInfo()
    {
        return this.mbeanInfo;
    }

    private void buildDynamicMBeanInfo()
    {
        this.attributes[0] = new MBeanAttributeInfo("Value", "java.lang.Double", "Counter value.", true, false, false);

        Constructor<?>[] constructors = this.getClass().getConstructors();
        this.constructors[0] = new MBeanConstructorInfo("Constructs a " + "SimpleDynamic object", constructors[0]);

        MBeanParameterInfo[] params = null;
        this.operations[0] = new MBeanOperationInfo("reset", "reset Counter " + "attribute to its initial value", params, "void", MBeanOperationInfo.ACTION);

        this.mbeanInfo = new MBeanInfo(this.className, "Counter", this.attributes, this.constructors, this.operations, null);
    }
}

package com.ericsson.sc.validator;

import java.util.List;

/**
 * 
 */
public interface RuleSupplier<T>
{
    List<Rule<T>> getRules();

}

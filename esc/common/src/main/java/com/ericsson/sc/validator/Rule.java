package com.ericsson.sc.validator;

import com.ericsson.adpal.cm.validator.RuleResult;
import io.reactivex.Single;

public interface Rule<T>
{

    Single<RuleResult> apply(T nf);

}

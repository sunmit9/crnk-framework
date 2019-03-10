package io.crnk.core.queryspec.typed;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;

public class ResourcePathBase extends PathBase {

    protected ResourcePathBase(PathSpec pathSpec) {
        super(pathSpec);
    }

    public FilterSpec filter(FilterOperator operator, Object value) {
        return new FilterSpec(pathSpec, operator, value);
    }

    public FilterSpec eq(Object value) {
        return filter(FilterOperator.EQ, value);
    }

    public FilterSpec neq(Object value) {
        return filter(FilterOperator.NEQ, value);
    }
}

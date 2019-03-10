package io.crnk.core.queryspec.typed;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.SortSpec;

public class PrimitivePath extends PathBase {

    public PrimitivePath(PathSpec pathSpec) {
        super(pathSpec);
    }

    public SortSpec sort(Direction dir) {
        return new SortSpec(pathSpec, dir);
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

    public FilterSpec like(Object value) {
        return filter(FilterOperator.LIKE, value);
    }

    public FilterSpec gt(Object value) {
        return filter(FilterOperator.GT, value);
    }

    public FilterSpec ge(Object value) {
        return filter(FilterOperator.GE, value);
    }

    public FilterSpec lt(Object value) {
        return filter(FilterOperator.LT, value);
    }

    public FilterSpec le(Object value) {
        return filter(FilterOperator.LE, value);
    }
}

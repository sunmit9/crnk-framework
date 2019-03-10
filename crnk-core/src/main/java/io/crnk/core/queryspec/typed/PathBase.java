package io.crnk.core.queryspec.typed;

import io.crnk.core.queryspec.PathSpec;

public class PathBase extends PathSpec {

    protected PathSpec pathSpec;

    protected PathBase(PathSpec pathSpec) {
        elements = pathSpec.getElements();
    }
}

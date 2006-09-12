package org.intellij.trinkets.logFilter;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.JDOMExternalizable;

import java.util.Set;

/**
 * Configurable for {@link LogFilterManager}.
 *
 * @author Alexey Efimov
 */
public interface LogFilterConfigurable extends Configurable, JDOMExternalizable {
    void addNew();

    void removeSelected();

    boolean isVisible();

    boolean isItemSelected();

    Set<Filter> createFilters();
}

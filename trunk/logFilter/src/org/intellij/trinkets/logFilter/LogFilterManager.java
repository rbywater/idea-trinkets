package org.intellij.trinkets.logFilter;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleView;

import java.awt.*;
import java.util.Set;

/**
 * Log filter manager
 *
 * @author Alexey Efimov
 */
public interface LogFilterManager {
    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isFiltersAttached();

    Set<Filter> getConfiguredFilters();

    ConsoleView getTab(Filter filter, String name, Color tabColor, Filter highlightFilter);
}

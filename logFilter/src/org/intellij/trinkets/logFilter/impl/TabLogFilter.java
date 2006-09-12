package org.intellij.trinkets.logFilter.impl;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import org.intellij.trinkets.logFilter.LogFilterManager;

import java.awt.*;

/**
 * Filter implementation.
 *
 * @author Alexey Efimov
 */
final class TabLogFilter extends LogFilter {
    private final String tabName;

    public TabLogFilter(LogFilterManager manager, FilterConfiguration setting) {
        super(manager, setting);
        if (setting.isTabingEnabled() && !setting.isAutomaticalyTabName()) {
            tabName = setting.getTabName();
        } else {
            tabName = null;
        }

    }


    protected void performAdditional(Result result, String key, String line) {
        StringBuffer tabTitle = new StringBuffer(getSetting().getName());
        tabTitle.append(": ");
        if (tabName == null) {
            tabTitle.append(key);
        } else {
            tabTitle.append(tabName);
        }
        addToTab(tabTitle.toString(), line, result, tabName == null);
    }

    private void addToTab(String tabName, String line, Result result, boolean isAutomatical) {
        Filter filter = isAutomatical ? null : new LogFilter(getManager(), getSetting());
        Color tabColor =
                result != null && isAutomatical ?
                        (result.highlightAttributes.getForegroundColor() != null ?
                                result.highlightAttributes.getForegroundColor() :
                                result.highlightAttributes.getBackgroundColor()) : null;

        ConsoleView view = getManager().getTab(this, tabName, tabColor, filter);
        view.print(line, ConsoleViewContentType.NORMAL_OUTPUT);
    }
}

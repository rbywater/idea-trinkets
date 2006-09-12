package org.intellij.trinkets.logFilter.impl;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.containers.HashSet;
import org.intellij.trinkets.logFilter.LogFilterConfigurable;
import org.intellij.trinkets.logFilter.LogFilterManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import java.awt.*;

final class LogFilterConfigurableImpl implements LogFilterConfigurable, ProjectComponent {
    private static final Icon ICON = IconLoader.getIcon("/org/intellij/trinkets/logFilter/icons/configurableLogFilter.png");
    private static final Icon DISABLED_ICON = IconLoader.getDisabledIcon(ICON);

    private final List<FilterConfiguration> settings = new ArrayList<FilterConfiguration>(0);
    private final LogFilterManager filterManager;
    private final Icon icon = new LiveIcon();

    private LogFilterConfigurableForm form;

    public LogFilterConfigurableImpl(LogFilterManager filterManager) {
        this.filterManager = filterManager;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    @NonNls
    public String getComponentName() {
        return "LogFilterConfiguratble";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public void addNew() {
        if (form != null) {
            form.add();
        }
    }

    public void removeSelected() {
        if (form != null) {
            form.remove();
        }
    }

    public boolean isVisible() {
        return form != null;
    }

    public boolean isItemSelected() {
        return form != null && form.isSelected();
    }

    public Set<Filter> createFilters() {
        Set<Filter> filters = new HashSet<Filter>(settings.size());
        List<FilterConfiguration> wrongFilters = new ArrayList<FilterConfiguration>(0);
        for (FilterConfiguration setting : settings) {
            try {
                filters.add(new TabLogFilter(filterManager, setting));
            } catch (PatternSyntaxException e) {
                wrongFilters.add(setting);
            }
        }
        settings.removeAll(wrongFilters);
        return filters;
    }

    public String getDisplayName() {
        return "Log Filters";
    }

    public Icon getIcon() {
        return icon;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        if (form == null) {
            form = new LogFilterConfigurableForm();
        }
        return form.getRootPanel();
    }

    public boolean isModified() {
        try {

            return form != null &&
                    form.isModified(settings, filterManager.isEnabled(), filterManager.isFiltersAttached());
        } catch (ConfigurationException e) {
            return false;
        }
    }

    public void apply() throws ConfigurationException {
        if (form != null) {
            form.getData(settings);
            filterManager.setEnabled(form.isEnabled());
        }
    }

    public void reset() {
        if (form != null) {
            form.setData(settings);
            form.setEnabled(filterManager.isEnabled());
        }
    }

    public void disposeUIResources() {
        form = null;
    }

    public void readExternal(Element element) {
        filterManager.setEnabled(Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "enabled")));
        List list = element.getChildren("filter");
        for (Object aList : list) {
            Element filter = (Element) aList;
            FilterConfiguration setting = new FilterConfiguration();
            settings.add(setting);
            setting.readExternal(filter);
        }
    }

    public void writeExternal(Element element) throws WriteExternalException {
        JDOMExternalizerUtil.writeField(element, "enabled", String.valueOf(filterManager.isEnabled()));
        for (FilterConfiguration setting : settings) {
            Element filter = new Element("filter");
            setting.writeExternal(filter);
            element.addContent(filter);
        }
    }

    private class LiveIcon implements Icon {
        private Icon getDelegatedIcon() {
            return filterManager.isEnabled() ? ICON : DISABLED_ICON;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            getDelegatedIcon().paintIcon(c, g, x, y);
        }

        public int getIconWidth() {
            return getDelegatedIcon().getIconWidth();
        }

        public int getIconHeight() {
            return getDelegatedIcon().getIconHeight();
        }
    }
}

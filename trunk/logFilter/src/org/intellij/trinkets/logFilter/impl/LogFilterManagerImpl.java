package org.intellij.trinkets.logFilter.impl;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuidlerFactory;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentListener;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.containers.WeakHashMap;
import org.intellij.trinkets.logFilter.LogFilterConfigurable;
import org.intellij.trinkets.logFilter.LogFilterManager;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class LogFilterManagerImpl implements ProjectComponent, LogFilterManager {
    private static final String TOOL_WINDOW_ID = "Logs";
    private static final Icon LOG_TAB_ICON = IconLoader.getIcon("/runConfigurations/testOuput.png");

    private final Map<ExecutionConsole, Set<Filter>> filtersMap = new WeakHashMap<ExecutionConsole, Set<Filter>>(0);
    private final Map<Filter, Map<String, ConsoleView>> filterTabs = new HashMap<Filter, Map<String, ConsoleView>>(0);

    private final Project project;
    private final RunContentListener listener = new RunContentListenerImpl();
    private JTabbedPane tabsComponent;
    private boolean enabled;

    public LogFilterManagerImpl(Project project) {
        this.project = project;
    }

    public void initComponent() {
        ExecutionManager.getInstance(project).getContentManager().addRunContentListener(listener);
    }

    public void disposeComponent() {
        ExecutionManager.getInstance(project).getContentManager().removeRunContentListener(listener);
        filtersMap.clear();
        filterTabs.clear();
    }

    public String getComponentName() {
        return "LogFilterManager";
    }

    public void projectOpened() {
        tabsComponent = new JTabbedPane();
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(TOOL_WINDOW_ID, tabsComponent, ToolWindowAnchor.BOTTOM);
        toolWindow.setIcon(LOG_TAB_ICON);
        toolWindow.setAvailable(false, null);
    }

    public void projectClosed() {
        if (tabsComponent != null) {
            tabsComponent.removeAll();
            ToolWindowManager.getInstance(project).unregisterToolWindow(TOOL_WINDOW_ID);
            tabsComponent = null;
        }
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFiltersAttached() {
        return !filtersMap.isEmpty();
    }

    public Set<Filter> getConfiguredFilters() {
        return project.getComponent(LogFilterConfigurable.class).createFilters();
    }

    public ConsoleView getTab(Filter filter, String name, Color tabColor, Filter highlightFilter) {
        Map<String, ConsoleView> tabs = filterTabs.get(filter);
        if (tabs == null) {
            tabs = new HashMap<String, ConsoleView>(1);
            filterTabs.put(filter, tabs);
        }
        ConsoleView view = tabs.get(name);
        if (view == null) {
            TextConsoleBuilder consoleBuilder = TextConsoleBuidlerFactory.getInstance().createBuilder(project);
            view = consoleBuilder.getConsole();
            if (highlightFilter != null) {
                view.addMessageFilter(highlightFilter);
            }
            tabs.put(name, view);

            createTab(name, view, tabColor);
        }
        return view;
    }

    private void createTab(String name, ConsoleView view, final Color tabColor) {
        if (enabled && tabsComponent != null) {
            if (tabColor != null) {
                tabsComponent.addTab(name, new ColorIcon(tabColor), view.getComponent());
            } else {
                tabsComponent.addTab(name, view.getComponent());
            }
            ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
            if (window != null) {
                if (!window.isAvailable()) {
                    window.setAvailable(true, null);
                    window.show(null);
                }
            }
        }
    }

    private final class RunContentListenerImpl implements RunContentListener {
        public void contentSelected(RunContentDescriptor descriptor) {
            if (enabled && descriptor != null) {
                ExecutionConsole executionConsole = descriptor.getExecutionConsole();
                if (executionConsole instanceof ConsoleView && !filtersMap.containsKey(executionConsole)) {
                    ConsoleView consoleView = (ConsoleView) executionConsole;
                    Set<Filter> configuredFilters = getConfiguredFilters();
                    for (Filter filter : configuredFilters) {
                        consoleView.addMessageFilter(filter);
                    }
                    filtersMap.put(consoleView, configuredFilters);
                }
            }
        }

        public void contentRemoved(RunContentDescriptor descriptor) {
            ExecutionConsole executionConsole = descriptor.getExecutionConsole();
            Set<Filter> filters = filtersMap.remove(executionConsole);
            if (filters != null) {
                for (Filter filter : filters) {
                    Map<String, ConsoleView> map = filterTabs.remove(filter);
                    if (map != null) {
                        Collection<ConsoleView> views = map.values();
                        for (ConsoleView view : views) {
                            removeTab(view);
                        }
                    }
                }
            }
        }
    }

    private void removeTab(ConsoleView view) {
        if (tabsComponent != null) {
            tabsComponent.remove(view.getComponent());
            int count = tabsComponent.getTabCount();
            if (count == 0) {
                ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
                if (window != null) {
                    window.setAvailable(false, null);
                }
            }
        }
    }

    private static final class ColorIcon implements Icon {
        private final Color tabColor;

        public ColorIcon(Color tabColor) {
            this.tabColor = tabColor;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(tabColor);
            g.fillRect(x + 1, y + 1, 10, 10);
            g.setColor(tabColor.darker());
            g.fillRect(x, y, 12, 12);
        }

        public int getIconWidth() {
            return 12;
        }

        public int getIconHeight() {
            return 12;
        }
    }
}

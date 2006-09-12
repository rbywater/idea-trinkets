package org.intellij.trinkets.logFilter.impl;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LogFilterConfigurableForm {
    private static final Icon WARNING_ICON = IconLoader.getIcon("/actions/quickfixBulb.png");

    private final LogFilterDetailsForm detailsForm = new LogFilterDetailsForm();
    private final List<FilterConfiguration> configurationList = new ArrayList<FilterConfiguration>(0);
    private final SettingsListModel model = new SettingsListModel();

    private JList filterList;
    private JPanel toolbarPanel;
    private JPanel detailPanel;
    private JPanel rootPanel;
    private JCheckBox enablePlugin;
    private JLabel warningLabel;

    public LogFilterConfigurableForm() {
        detailPanel.setLayout(new BorderLayout());

        filterList.setModel(model);
        filterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filterList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int index = filterList.getSelectedIndex();
                detailPanel.removeAll();
                if (index != -1) {
                    FilterConfiguration configuration = configurationList.get(index);
                    detailsForm.setData(configuration);
                    detailPanel.add(detailsForm.getRootPanel(), BorderLayout.CENTER);
                }
                detailPanel.revalidate();
            }
        });

        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup group = (ActionGroup) actionManager.getAction("LogFilter.Configuration.ToolBar");
        ActionToolbar actionToolbar = actionManager.createActionToolbar(ActionPlaces.UNKNOWN, group, true);
        toolbarPanel.setLayout(new BorderLayout());
        toolbarPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        enablePlugin.setSelected(true);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public boolean isEnabled() {
        return enablePlugin.isSelected();
    }

    public void setData(List<FilterConfiguration> filterConfigurationList) {
        configurationList.clear();
        configurationList.addAll(filterConfigurationList);
        model.notifyExternalUpdate();
        filterList.clearSelection();
    }

    public void getData(List<FilterConfiguration> filterConfigurationList) throws ConfigurationException {
        int index = filterList.getSelectedIndex();
        if (index != -1) {
            detailsForm.getData(configurationList.get(index));
        }
        filterConfigurationList.clear();
        filterConfigurationList.addAll(configurationList);
    }

    public boolean isModified(List<FilterConfiguration> filterConfigurationList, boolean enabled, boolean filtersAttached) throws ConfigurationException {
        boolean modified = false;
        int index = filterList.getSelectedIndex();
        if (index != -1) {
            FilterConfiguration configuration = configurationList.get(index);
            modified = detailsForm.isModified(configuration);
        }
        modified |= !configurationList.equals(filterConfigurationList);
        modified |= enabled != enablePlugin.isSelected();
        if (modified && filtersAttached) {
            warningLabel.setIcon(WARNING_ICON);
            warningLabel.setText("<html>The previous configured filters are currently work in opened consoles.<br>New settings will used for new consoles only.</html>");
        } else {
            warningLabel.setIcon(null);
            warningLabel.setText(null);
        }
        return modified;
    }

    public void add() {
        model.add();
    }

    public void remove() {
        int index = filterList.getSelectedIndex();
        if (index != -1) {
            model.remove(index);
        }
    }

    public boolean isSelected() {
        int index = filterList.getSelectedIndex();
        return index != -1;
    }

    public void setEnabled(boolean enabled) {
        enablePlugin.setSelected(enabled);
    }

    private class SettingsListModel extends AbstractListModel {
        public void notifyExternalUpdate() {
            fireContentsChanged(this, 0, configurationList.size() - 1);
        }

        public void add() {
            FilterConfiguration configuration = new FilterConfiguration();
            configuration.setName("Unnamed");
            configuration.setDecoratingEnabled(true);
            configuration.setAutomaticalyColor(true);
            configurationList.add(configuration);
            fireIntervalAdded(this, configurationList.size() - 1, configurationList.size() - 1);
        }

        public void remove(int index) {
            configurationList.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public int getSize() {
            return configurationList.size();
        }

        public Object getElementAt(int index) {
            return configurationList.get(index).getName();
        }
    }
}

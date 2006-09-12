package org.intellij.trinkets.logFilter.impl;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.ColorPanel;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class LogFilterDetailsForm {
    private final TableModel dataModel = new GroupMonitoringModel();

    private JTextField name;
    private JTextField regexp;
    private JTextField testLine;
    private JTable regexpTable;
    private JPanel rootPanel;
    private JCheckBox isBackground;
    private ColorPanel background;
    private JCheckBox isForeground;
    private ColorPanel foreground;
    private JCheckBox automaticalyColor;
    private JCheckBox isTabing;
    private JCheckBox automaticalyTabName;
    private JTextField tabName;
    private JCheckBox isDecorating;
    private final ActionListener enablingListener = new EnablingListener();

    public LogFilterDetailsForm() {
        regexpTable.setModel(dataModel);
        regexpTable.getTableHeader().getColumnModel().getColumn(0).sizeWidthToFit();
        regexpTable.getColumnModel().getColumn(0).setPreferredWidth(regexpTable.getTableHeader().getColumnModel().getColumn(0).getWidth());
        regexpTable.getTableHeader().getColumnModel().getColumn(2).sizeWidthToFit();
        regexpTable.getColumnModel().getColumn(2).setPreferredWidth(regexpTable.getTableHeader().getColumnModel().getColumn(2).getWidth());

        automaticalyColor.addActionListener(enablingListener);
        automaticalyTabName.addActionListener(enablingListener);
        isTabing.addActionListener(enablingListener);
        isDecorating.addActionListener(enablingListener);
        isForeground.addActionListener(enablingListener);
        isBackground.addActionListener(enablingListener);

        isTabing.setSelected(false);
        isDecorating.setSelected(true);
        automaticalyColor.setSelected(true);
        automaticalyTabName.setSelected(true);

        enableControls();

        TableColumn monitorColumn = regexpTable.getColumnModel().getColumn(2);
        monitorColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        monitorColumn.setCellRenderer(new BooleanTableCellRenderer());

    }

    private void enableControls() {
        tabName.setEnabled(isTabing.isSelected() && !automaticalyTabName.isSelected());
        automaticalyTabName.setEnabled(isTabing.isSelected());
        regexpTable.setEnabled(automaticalyColor.isSelected() || isTabing.isSelected() && automaticalyTabName.isSelected());

        isForeground.setEnabled(isDecorating.isSelected() && !automaticalyColor.isSelected());
        isBackground.setEnabled(isDecorating.isSelected() && !automaticalyColor.isSelected());
        foreground.setEnabled(isDecorating.isSelected() && isForeground.isSelected() && !automaticalyColor.isSelected());
        background.setEnabled(isDecorating.isSelected() && isBackground.isSelected() && !automaticalyColor.isSelected());
        automaticalyColor.setEnabled(isDecorating.isSelected());

        regexpTable.setEnabled(isTabing.isSelected() && automaticalyTabName.isSelected() || isDecorating.isSelected() && automaticalyColor.isSelected());
    }

    public TableModel getDataModel() {
        return dataModel;
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    private void executeTest() {
        String regexpText = regexp.getText();
        String testLineText = testLine.getText();
        if (regexpText.trim().length() > 0 && testLineText.length() > 0) {
            try {
                Pattern pattern = Pattern.compile(regexpText);
                Matcher matcher = pattern.matcher(testLineText);
                if (matcher.find()) {
                    regexpTable.setEnabled(true);
                    int groupCount = matcher.groupCount();
                    for (int i = 0; i < dataModel.getRowCount(); i++) {
                        String s = i < groupCount ? matcher.group(i + 1) : "";
                        dataModel.setValueAt(s, i, 1);
                    }
                } else {
                    regexpTable.setEnabled(false);
                    for (int i = 0; i < dataModel.getRowCount(); i++) {
                        dataModel.setValueAt("", i, 1);
                    }
                }
            } catch (PatternSyntaxException e) {
                // Ignore
            }
        }
    }

    public void setData(FilterConfiguration data) {
        name.setText(data.getName());
        regexp.setText(data.getRegexp());
        testLine.setText(data.getTestLine());
        isForeground.setSelected(data.isForegroundEnabled());
        isTabing.setSelected(data.isTabingEnabled());
        automaticalyTabName.setSelected(data.isAutomaticalyTabName());
        tabName.setText(data.getTabName());
        isDecorating.setSelected(data.isDecoratingEnabled());
        automaticalyColor.setSelected(data.isAutomaticalyColor());
        isBackground.setSelected(data.isBackgroundEnabled());
        int rowCount = dataModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            dataModel.setValueAt(data.isGroupEnabled(i), i, 2);
        }
        enableControls();
    }

    public void getData(FilterConfiguration data) throws ConfigurationException {
        String nameValue = name.getText();
        if (nameValue.trim().length() == 0) {
            throw new ConfigurationException("Name is not specified");
        }
        data.setName(nameValue.trim());
        String regexpValue = regexp.getText();
        if (regexpValue.length() == 0) {
            throw new ConfigurationException("Regular expression is not specified");
        }
        try {
            Pattern.compile(regexpValue);
        } catch (PatternSyntaxException e) {
            throw new ConfigurationException("Regular expression syntax error");
        }
        data.setRegexp(regexpValue);
        data.setTestLine(testLine.getText());
        data.setForegroundEnabled(isForeground.isSelected());
        data.setTabingEnabled(isTabing.isSelected());
        data.setAutomaticalyTabName(automaticalyTabName.isSelected());
        String tabNameValue = tabName.getText();
        if (!automaticalyTabName.isSelected() && isTabing.isSelected() && tabNameValue.trim().length() == 0) {
            throw new ConfigurationException("Tab name is not specified");
        }
        data.setTabName(tabNameValue.trim());
        data.setDecoratingEnabled(isDecorating.isSelected());
        data.setAutomaticalyColor(automaticalyColor.isSelected());
        data.setBackgroundEnabled(isBackground.isSelected());
        int rowCount = dataModel.getRowCount();
        boolean isGroupSelected = false;
        for (int i = 0; i < rowCount && !isGroupSelected; i++) {
            if ((Boolean) dataModel.getValueAt(i, 2)) {
                isGroupSelected = true;
            }
        }
        if ((automaticalyTabName.isSelected() || automaticalyColor.isSelected()) && !isGroupSelected) {
            throw new ConfigurationException("Automaticaly modes is used, but no groups is monitored");
        }
        for (int i = 0; i < rowCount; i++) {
            data.setGroupEnabled(i, (Boolean) dataModel.getValueAt(i, 2));
        }
    }

    public boolean isModified(FilterConfiguration data) {
        executeTest();

        if (name.getText() != null ? !name.getText().equals(data.getName()) : data.getName() != null) return true;
        if (regexp.getText() != null ? !regexp.getText().equals(data.getRegexp()) : data.getRegexp() != null)
            return true;
        if (testLine.getText() != null ? !testLine.getText().equals(data.getTestLine()) : data.getTestLine() != null)
            return true;
        if (isForeground.isSelected() != data.isForegroundEnabled()) return true;
        if (isTabing.isSelected() != data.isTabingEnabled()) return true;
        if (automaticalyTabName.isSelected() != data.isAutomaticalyTabName()) return true;
        if (tabName.getText() != null ? !tabName.getText().equals(data.getTabName()) : data.getTabName() != null)
            return true;
        if (isDecorating.isSelected() != data.isDecoratingEnabled()) return true;
        if (automaticalyColor.isSelected() != data.isAutomaticalyColor()) return true;
        if (isBackground.isSelected() != data.isBackgroundEnabled()) return true;
        int rowCount = dataModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (!dataModel.getValueAt(i, 2).equals(data.isGroupEnabled(i))) {
                return true;
            }
        }
        return false;
    }

    private final class EnablingListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            enableControls();
        }
    }
}

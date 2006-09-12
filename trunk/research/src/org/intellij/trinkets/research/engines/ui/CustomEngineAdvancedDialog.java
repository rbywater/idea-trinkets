package org.intellij.trinkets.research.engines.ui;

import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.*;

public class CustomEngineAdvancedDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField queryStringPatternTextField;
    private JTextField hyperLinkPatternTextField;
    private JTable additionalParametersTable;
    private JButton addButton;
    private JButton removeButton;

    private String queryStringPattern;
    private String hyperLinkPattern;
    private String[][] additionalParameters;

    public CustomEngineAdvancedDialog(String defaultQueryStringPattern, String defaultHyperlinkPattern, String[][] defaultAdditionalParameters) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AdditionalParameterDialog dialog = new AdditionalParameterDialog(null, null);
                dialog.setVisible(true);
                String paramName = dialog.getParameterName();
                if (paramName != null) {
                    DefaultTableModel tableModel = (DefaultTableModel) additionalParametersTable.getModel();
                    tableModel.addRow(new Object[] {paramName, dialog.getParameterValue()});
                    tableModel.fireTableDataChanged();
                }
            }
        });
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (additionalParametersTable.getSelectedRowCount() > 0) {
                    if (Messages.showYesNoDialog(contentPane, "Delete selected parameters?", "Remove addition parameters", Messages.getQuestionIcon()) == 0) {
                        int[] rows = additionalParametersTable.getSelectedRows();
                        DefaultTableModel tableModel = (DefaultTableModel) additionalParametersTable.getModel();
                        for (int row : rows) {
                            tableModel.removeRow(row);
                        }
                        tableModel.fireTableDataChanged();
                    }
                }
            }
        });

        queryStringPatternTextField.setText(defaultQueryStringPattern);
        hyperLinkPatternTextField.setText(defaultHyperlinkPattern);

        DefaultTableModel tableModel = (DefaultTableModel) additionalParametersTable.getModel();
        tableModel.setColumnCount(2);
        tableModel.setColumnIdentifiers(new String[] {"Name", "Value"});
        for (String[] strings : defaultAdditionalParameters) {
            tableModel.addRow(strings);
        }
        tableModel.fireTableDataChanged();
    }

    private void onOK() {
        queryStringPattern = queryStringPatternTextField.getText();
        hyperLinkPattern = hyperLinkPatternTextField.getText();
        TableModel tableModel = additionalParametersTable.getModel();
        int rowCount = tableModel.getRowCount();
        if (rowCount > 0) {
            additionalParameters = new String[rowCount][2];
            for (int i = 0; i < additionalParameters.length; i++) {
                String[] additionalParameter = additionalParameters[i];
                additionalParameter[0] = (String) tableModel.getValueAt(i, 0);
                additionalParameter[1] = (String) tableModel.getValueAt(i, 1);
            }
        }
        if (queryStringPattern.trim().length() == 0) {
            Messages.showWarningDialog(contentPane, "Query string pattern must be not empty", "Advanced parameters");
        } else if (hyperLinkPattern.trim().length() == 0) {
            Messages.showWarningDialog(contentPane, "Hyperlink pattern must be not empty", "Advanced parameters");
        } else {
            dispose();
        }
    }

    private void onCancel() {
        queryStringPattern = null;
        hyperLinkPattern = null;
        additionalParameters = null;

        dispose();
    }


    public String getQueryStringPattern() {
        return queryStringPattern;
    }

    public String getHyperLinkPattern() {
        return hyperLinkPattern;
    }

    public String[][] getAdditionalParameters() {
        return additionalParameters;
    }
}

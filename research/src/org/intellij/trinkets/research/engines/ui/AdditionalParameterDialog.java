package org.intellij.trinkets.research.engines.ui;

import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.*;

public class AdditionalParameterDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameTextField;
    private JTextField valueTextField;

    private String parameterName;
    private String parameterValue;

    public AdditionalParameterDialog(String defaultParameterName, String defaultParameterValue) {
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

        nameTextField.setText(defaultParameterName);
        valueTextField.setText(defaultParameterValue);
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    private void onOK() {
// add your code here
        parameterName = nameTextField.getText().trim();
        parameterValue = valueTextField.getText().trim();
        if (parameterName.length() == 0) {
            Messages.showWarningDialog(contentPane, "Parameter name must be not empty", "Add additional parameter");
        } else {
            dispose();
        }
    }

    private void onCancel() {
        parameterName = null;
        parameterValue = null;

        dispose();
    }
}

package org.intellij.trinkets.research.engines.ui;

import org.intellij.trinkets.research.engines.HttpResearchEngine;
import org.intellij.trinkets.research.ResearchEngine;
import org.intellij.trinkets.research.ResearchResult;
import org.intellij.trinkets.research.ui.ResearchResultComponent;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.regex.Pattern;

import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.ui.Messages;

/**
 * @author Alexey Efimov
 */
public class CustomEngineForm {
    private JTextField engineNameTextField;
    private JTextField serverURLTextField;
    private JTextField engineIconTextField;
    private JTextField queryParameterTextField;
    private JTextField responseRegExpTextField;
    private JButton advancedButton;
    private JTextField searchActionTextField;
    private JButton testSearchButton;
    private JPanel previewPanel;
    private JTextField searchStringTextField;
    private JPanel contentPane;

    private String engineName;
    private String engineIcon;
    private String serverURL;
    private String queryParameter;
    private String responseRegexp;
    private String queryStringPattern;
    private String hyperlinkPattern;
    private String searchAction;
    private String[][] additionalParameters;

    private final ResearchResultComponent researchTestComponent = new ResearchResultComponent();

    public CustomEngineForm() {
        advancedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doAdvanced();
            }
        });
        testSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doTest();
            }
        });
        previewPanel.setLayout(new BorderLayout());
        previewPanel.add(researchTestComponent, BorderLayout.CENTER);
    }

    private void doAdvanced() {
        CustomEngineAdvancedDialog dialog = new CustomEngineAdvancedDialog(queryStringPattern, hyperlinkPattern, additionalParameters);
        dialog.setVisible(true);
        String pattern = dialog.getQueryStringPattern();
        if (pattern != null) {
            queryStringPattern = pattern;
            hyperlinkPattern = dialog.getHyperLinkPattern();
            additionalParameters = dialog.getAdditionalParameters();
        }
    }

    private void doTest() {
        if (validate()) {
            String query = searchStringTextField.getText().trim();
            if (query.length() == 0) {
                Messages.showWarningDialog(contentPane, "Please, enter search string for test", "Test custom engine");
                searchStringTextField.requestFocus();
            } else {
                try {
                    ResearchEngine researchEngine = new HttpResearchEngine(engineName, IconLoader.getIcon(engineIcon), Pattern.compile(responseRegexp), serverURL, searchAction, queryParameter, additionalParameters);
                    researchTestComponent.getModel().removeAll();
                    researchTestComponent.getModel().add(researchEngine);
                    ResearchResult researchResult = researchEngine.research(query);
                    if (researchResult.getNodes().size() == 0) {
                        Messages.showInfoMessage(contentPane, "Engine return no records. Please, check\nsearch string or regular expression", "Test custom engine");
                    }
                    researchTestComponent.updateUI();
                } catch (Throwable e) {
                    Messages.showWarningDialog(contentPane, "Engine has wrong configuration:\n" + e.getMessage(), "Test custom engine");
                }
            }
        }
    }

    private boolean validate() {
        return false;
    }
}

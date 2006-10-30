package org.intellij.trinkets.pluginPacker.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TextFieldWithStoredHistory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Plugin packer form.
 *
 * @author Alexey Efimov
 */
final class PluginPackerForm {
    @NonNls
    private static final String DEFAULT_PACKAGE_PATTERN = "${plugin.id}_${plugin.version}.zip";
    @NonNls
    private static final String DEFAULT_SOURCES_INBOX_PATTERN = "src_${plugin.id}.zip";
    @NonNls
    private static final String DEFAULT_SOURCES_OUTBOX_PATTERN = "${plugin.id}_${plugin.version}_src.zip";
    @NonNls
    private static final String PROPERTY_BUILD_SOURCES = "PluginPacker.BuildSources";
    @NonNls
    private static final String PROPERTY_INBOX_SOURCES = "PluginPacker.InboxSources";

    private final Module defaultModule;

    private ComponentWithBrowseButton<TextFieldWithStoredHistory> outputPathTextFieldWithBrowseButton;
    private JComboBox moduleComboBox;
    private TextFieldWithStoredHistory packagePatternTextFieldWithStoredHistory;
    private JPanel root;
    private JCheckBox buildSourcesPackageAlsoCheckBox;
    private JRadioButton inboxSourcesButton;
    private TextFieldWithStoredHistory inboxSourcesPatternTextFieldWithStoredHistory;
    private JRadioButton outboxSourcesButton;
    private TextFieldWithStoredHistory outboxSourcesPatternTextFieldWithStoredHistory;

    public PluginPackerForm(Module[] modules, Module defaultModule) {
        this.defaultModule = defaultModule;
        buildSourcesPackageAlsoCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = buildSourcesPackageAlsoCheckBox.isSelected();
                inboxSourcesButton.setEnabled(selected);
                inboxSourcesPatternTextFieldWithStoredHistory.setEnabled(selected && inboxSourcesButton.isSelected());
                outboxSourcesButton.setEnabled(selected);
                outboxSourcesPatternTextFieldWithStoredHistory.setEnabled(selected && outboxSourcesButton.isSelected());
            }
        });
        inboxSourcesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                inboxSourcesPatternTextFieldWithStoredHistory.setEnabled(true);
                outboxSourcesPatternTextFieldWithStoredHistory.setEnabled(false);
            }
        });
        outboxSourcesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                inboxSourcesPatternTextFieldWithStoredHistory.setEnabled(false);
                outboxSourcesPatternTextFieldWithStoredHistory.setEnabled(true);
            }
        });
        moduleComboBox.setRenderer(new ModuleListRenderer());
        moduleComboBox.setModel(new DefaultComboBoxModel(modules));
        if (defaultModule != null) {
            moduleComboBox.setSelectedItem(defaultModule);
        } else {
            moduleComboBox.setSelectedIndex(-1);
        }
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        boolean inboxSources = propertiesComponent.isTrueValue(PROPERTY_INBOX_SOURCES) || !propertiesComponent.isValueSet(PROPERTY_INBOX_SOURCES);
        inboxSourcesButton.setSelected(inboxSources);
        outboxSourcesButton.setSelected(!inboxSources);
        inboxSourcesPatternTextFieldWithStoredHistory.setEnabled(inboxSources);
        outboxSourcesPatternTextFieldWithStoredHistory.setEnabled(!inboxSources);
        boolean buildSources = propertiesComponent.isTrueValue(PROPERTY_BUILD_SOURCES) || !propertiesComponent.isValueSet(PROPERTY_BUILD_SOURCES);
        buildSourcesPackageAlsoCheckBox.setSelected(buildSources);
        inboxSourcesButton.setEnabled(buildSources);
        inboxSourcesPatternTextFieldWithStoredHistory.setEnabled(buildSources && inboxSourcesButton.isSelected());
        outboxSourcesButton.setEnabled(buildSources);
        outboxSourcesPatternTextFieldWithStoredHistory.setEnabled(buildSources && outboxSourcesButton.isSelected());
    }

    public final Module getModule() {
        return (Module) moduleComboBox.getSelectedItem();
    }

    public final String getPackagePattern() {
        return packagePatternTextFieldWithStoredHistory.getText();
    }

    public final String getSourcesPattern() {
        return inboxSourcesButton.isSelected() ? inboxSourcesPatternTextFieldWithStoredHistory.getText() :
                outboxSourcesButton.isSelected() ? outboxSourcesPatternTextFieldWithStoredHistory.getText() : null;
    }

    public final String getOutputPath() {
        return outputPathTextFieldWithBrowseButton.getChildComponent().getText();
    }

    public final boolean isBuildSources() {
        return buildSourcesPackageAlsoCheckBox.isSelected();
    }

    public final boolean isInboxSources() {
        return inboxSourcesButton.isSelected();
    }

    public final boolean validate() {
        Module module = (Module) moduleComboBox.getSelectedItem();
        if (module == null) {
            Messages.showWarningDialog(moduleComboBox, "Please, select module", "Packing plugin");
            moduleComboBox.requestFocus();
            return false;
        }
        String text = packagePatternTextFieldWithStoredHistory.getText();
        if (StringUtil.isEmptyOrSpaces(text)) {
            Messages.showWarningDialog(packagePatternTextFieldWithStoredHistory, "Please, enter pattern for plugin package", "Packing plugin");
            packagePatternTextFieldWithStoredHistory.requestFocus();
            return false;
        }

        if (buildSourcesPackageAlsoCheckBox.isSelected()) {
            if (inboxSourcesButton.isSelected()) {
                text = inboxSourcesPatternTextFieldWithStoredHistory.getText();
                if (StringUtil.isEmptyOrSpaces(text)) {
                    Messages.showWarningDialog(inboxSourcesPatternTextFieldWithStoredHistory, "Please, enter pattern for sources package", "Packing plugin");
                    inboxSourcesPatternTextFieldWithStoredHistory.requestFocus();
                    return false;
                }
            } else if (outboxSourcesButton.isSelected()) {
                text = outboxSourcesPatternTextFieldWithStoredHistory.getText();
                if (StringUtil.isEmptyOrSpaces(text)) {
                    Messages.showWarningDialog(outboxSourcesPatternTextFieldWithStoredHistory, "Please, enter pattern for sources package", "Packing plugin");
                    outboxSourcesPatternTextFieldWithStoredHistory.requestFocus();
                    return false;
                }
            }
        }

        text = outputPathTextFieldWithBrowseButton.getChildComponent().getText();
        if (StringUtil.isEmptyOrSpaces(text)) {
            Messages.showWarningDialog(outputPathTextFieldWithBrowseButton, "Please, enter output path", "Packing plugin");
            outputPathTextFieldWithBrowseButton.requestFocus();
            return false;
        }

        File file = new File(text);
        if (file.exists() && !file.isDirectory()) {
            Messages.showWarningDialog(outputPathTextFieldWithBrowseButton, "Output path invalid.\nPlease, enter valid output path.", "Packing plugin");
            outputPathTextFieldWithBrowseButton.requestFocus();
            return false;
        }
        return true;
    }

    public void saveFieldsHistory() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(PROPERTY_INBOX_SOURCES, String.valueOf(inboxSourcesButton.isSelected()));
        propertiesComponent.setValue(PROPERTY_BUILD_SOURCES, String.valueOf(buildSourcesPackageAlsoCheckBox.isSelected()));
        packagePatternTextFieldWithStoredHistory.addCurrentTextToHistory();
        inboxSourcesPatternTextFieldWithStoredHistory.addCurrentTextToHistory();
        outboxSourcesPatternTextFieldWithStoredHistory.addCurrentTextToHistory();
        outputPathTextFieldWithBrowseButton.getChildComponent().addCurrentTextToHistory();
    }

    @SuppressWarnings({"unchecked"})
    private void createUIComponents() {
        packagePatternTextFieldWithStoredHistory = createHistoryTextField("PluginPacker.PackagePattern", DEFAULT_PACKAGE_PATTERN);

        inboxSourcesPatternTextFieldWithStoredHistory = createHistoryTextField("PluginPacker.InboxSourcePattern", DEFAULT_SOURCES_INBOX_PATTERN);

        outboxSourcesPatternTextFieldWithStoredHistory = createHistoryTextField("PluginPacker.OutboxSourcePattern", DEFAULT_SOURCES_OUTBOX_PATTERN);

        String defaultOutputPath = "";
        if (defaultModule != null) {
            VirtualFile parent = defaultModule.getModuleFile().getParent();
            if (parent != null) {
                defaultOutputPath = parent.getPath();
            }
        }
        TextFieldWithStoredHistory outputPathHistory = createHistoryTextField("PluginPacker.OutputPath", defaultOutputPath);
        outputPathTextFieldWithBrowseButton = new ComponentWithBrowseButton<TextFieldWithStoredHistory>(
                outputPathHistory, new PathActionListener()
        );
    }

    @SuppressWarnings({"unchecked"})
    private static TextFieldWithStoredHistory createHistoryTextField(@NotNull String name, @NotNull String... defaultValues) {
        TextFieldWithStoredHistory storedHistory = new TextFieldWithStoredHistoryBugFixed(name);
        storedHistory.reset();
        List<String> list = (List<String>) storedHistory.getHistory();
        list.removeAll(Arrays.asList(defaultValues));
        if (list.isEmpty()) {
            // Default histories
            for (String defaultValue : defaultValues) {
                storedHistory.setText(defaultValue);
                storedHistory.addCurrentTextToHistory();
            }
        } else {
            storedHistory.setSelectedItem(list.get(list.size() - 1));
        }
        return storedHistory;
    }

    public final JComponent getRoot() {
        return root;
    }

    private final class PathActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(new Runnable() {
                public void run() {
                    TextFieldWithStoredHistory storedHistory = outputPathTextFieldWithBrowseButton.getChildComponent();
                    VirtualFile previous = LocalFileSystem.getInstance().refreshAndFindFileByPath(
                            storedHistory.getText().replace('\\', '/')
                    );

                    FileChooserDescriptor fileDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                    fileDescriptor.setShowFileSystemRoots(true);
                    fileDescriptor.setTitle("Select output path");
                    fileDescriptor.setDescription("Please, select output folder");
                    VirtualFile[] virtualFiles = FileChooser.chooseFiles(outputPathTextFieldWithBrowseButton, fileDescriptor, previous);

                    if (virtualFiles != null && virtualFiles.length > 0) {
                        String path = virtualFiles[0].getPath();
                        storedHistory.setText(path);
                        storedHistory.addCurrentTextToHistory();
                    }
                }
            });
        }
    }

    private static final class ModuleListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Module module = (Module) value;
            if (module != null) {
                component.setText(module.getName());
                component.setIcon(module.getModuleType().getNodeIcon(false));
            } else {
                component.setText(null);
                component.setIcon(null);
            }
            return component;
        }
    }

    /**
     * Fixed text component. Enabling is valid now.
     */
    private static final class TextFieldWithStoredHistoryBugFixed extends TextFieldWithStoredHistory {
        public TextFieldWithStoredHistoryBugFixed(String name) {
            super(name, false);
        }

        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            getTextEditor().setEnabled(enabled);
            getTextEditor().setEditable(enabled);
        }
    }
}

package org.intellij.trinkets.pluginPacker.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.TextFieldWithStoredHistory;
import org.intellij.trinkets.pluginPacker.util.PluginPackerBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.module.PluginModuleType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Plugin packer form.
 *
 * @author Alexey Efimov
 */
final class PluginPackerForm {
    @NonNls
    private static final String[] DEFAULT_PACKAGE_PATTERNS = {"${module.name}_${plugin.version}.zip", "${plugin.id}_${plugin.version}.zip"};
    @NonNls
    private static final String[] DEFAULT_SOURCES_INBOX_PATTERNS = {"src_${module.name}.zip", "src_${plugin.id}.zip"};
    @NonNls
    private static final String[] DEFAULT_SOURCES_OUTBOX_PATTERNS = {"${module.name}_${plugin.version}_src.zip", "${plugin.id}_${plugin.version}_src.zip"};
    @NonNls
    private static final String PROPERTY_BUILD_SOURCES = "PluginPacker.BuildSources";
    @NonNls
    private static final String PROPERTY_INBOX_SOURCES = "PluginPacker.InboxSources";
    @NonNls
    private static final String PROPERTY_SILENT_OVERWRITE = "PluginPacker.SilentOverwrite";
    @NonNls
    private static final String PACKAGE_PATTERN_KEY = "PluginPacker.PackagePattern";
    @NonNls
    private static final String INBOX_SOURCE_PATTERN_KEY = "PluginPacker.InboxSourcePattern";
    @NonNls
    private static final String OUTBOX_SOURCE_PATTERN_KEY = "PluginPacker.OutboxSourcePattern";
    @NonNls
    private static final String PLUGIN_PACKER_OUTPUT_PATH_KEY = "PluginPacker.OutputPath";

    private final Module defaultModule;

    private ComponentWithBrowseButton<TextFieldWithStoredHistory> outputPathTextFieldWithBrowseButton;
    private FileChooserDescriptor outputPathDescriptor;
    private JComboBox moduleComboBox;
    private TextFieldWithStoredHistory packagePatternTextFieldWithStoredHistory;
    private JPanel root;
    private JCheckBox buildSourcesPackageAlsoCheckBox;
    private JRadioButton inboxSourcesButton;
    private TextFieldWithStoredHistory inboxSourcesPatternTextFieldWithStoredHistory;
    private JRadioButton outboxSourcesButton;
    private TextFieldWithStoredHistory outboxSourcesPatternTextFieldWithStoredHistory;
    private JCheckBox silentOverwriteCheckBox;

    public PluginPackerForm(Project project, Module[] modules, Module defaultModule) {
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
        List<Module> nullableModuleList = new ArrayList<Module>(modules.length + 1);
        nullableModuleList.add(null);
        nullableModuleList.addAll(Arrays.asList(modules));
        moduleComboBox.setModel(new DefaultComboBoxModel(nullableModuleList.toArray(new Module[nullableModuleList.size()])));
        if (defaultModule != null) {
            moduleComboBox.setSelectedItem(defaultModule);
        } else {
            moduleComboBox.setSelectedIndex(0);
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

        outputPathDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        outputPathTextFieldWithBrowseButton.addActionListener(new ModuleDependedBrowseFolderActionListener(project));
        final Application application = ApplicationManager.getApplication();
        if (application != null && !application.isUnitTestMode() && !application.isHeadlessEnvironment()) {
            JTextField field = outputPathTextFieldWithBrowseButton.getChildComponent().getTextEditor();
            FileChooserFactory.getInstance().installFileCompletion(field, outputPathDescriptor, true, null);
        }
        silentOverwriteCheckBox.setSelected(propertiesComponent.isTrueValue(PROPERTY_SILENT_OVERWRITE));
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

    public boolean isSilentOverwrite() {
        return silentOverwriteCheckBox.isSelected();
    }

    public final boolean validate() {
        String text = packagePatternTextFieldWithStoredHistory.getText();
        if (StringUtil.isEmptyOrSpaces(text)) {
            Messages.showWarningDialog(packagePatternTextFieldWithStoredHistory, PluginPackerBundle.message("please.enter.pattern.for.plugin.package"), PluginPackerBundle.message("packing.plugin"));
            packagePatternTextFieldWithStoredHistory.requestFocus();
            return false;
        }

        if (buildSourcesPackageAlsoCheckBox.isSelected()) {
            if (inboxSourcesButton.isSelected()) {
                text = inboxSourcesPatternTextFieldWithStoredHistory.getText();
                if (StringUtil.isEmptyOrSpaces(text)) {
                    Messages.showWarningDialog(inboxSourcesPatternTextFieldWithStoredHistory, PluginPackerBundle.message("please.enter.pattern.for.sources.package"), PluginPackerBundle.message("packing.plugin"));
                    inboxSourcesPatternTextFieldWithStoredHistory.requestFocus();
                    return false;
                }
            } else if (outboxSourcesButton.isSelected()) {
                text = outboxSourcesPatternTextFieldWithStoredHistory.getText();
                if (StringUtil.isEmptyOrSpaces(text)) {
                    Messages.showWarningDialog(outboxSourcesPatternTextFieldWithStoredHistory, PluginPackerBundle.message("please.enter.pattern.for.sources.package"), PluginPackerBundle.message("packing.plugin"));
                    outboxSourcesPatternTextFieldWithStoredHistory.requestFocus();
                    return false;
                }
            }
        }

        text = outputPathTextFieldWithBrowseButton.getChildComponent().getText();
        if (StringUtil.isEmptyOrSpaces(text)) {
            Messages.showWarningDialog(outputPathTextFieldWithBrowseButton, PluginPackerBundle.message("please.enter.output.path"), PluginPackerBundle.message("packing.plugin"));
            outputPathTextFieldWithBrowseButton.requestFocus();
            return false;
        }

        File file = new File(text);
        if (file.exists() && !file.isDirectory()) {
            Messages.showWarningDialog(outputPathTextFieldWithBrowseButton, PluginPackerBundle.message("output.path.invalid.nplease.enter.valid.output.path"), PluginPackerBundle.message("packing.plugin"));
            outputPathTextFieldWithBrowseButton.requestFocus();
            return false;
        }
        return true;
    }

    public void saveFieldsHistory() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(PROPERTY_INBOX_SOURCES, String.valueOf(inboxSourcesButton.isSelected()));
        propertiesComponent.setValue(PROPERTY_BUILD_SOURCES, String.valueOf(buildSourcesPackageAlsoCheckBox.isSelected()));
        propertiesComponent.setValue(PROPERTY_SILENT_OVERWRITE, String.valueOf(silentOverwriteCheckBox.isSelected()));
        packagePatternTextFieldWithStoredHistory.addCurrentTextToHistory();
        inboxSourcesPatternTextFieldWithStoredHistory.addCurrentTextToHistory();
        outboxSourcesPatternTextFieldWithStoredHistory.addCurrentTextToHistory();
        outputPathTextFieldWithBrowseButton.getChildComponent().addCurrentTextToHistory();
    }

    @SuppressWarnings({"unchecked"})
    private void createUIComponents() {
        packagePatternTextFieldWithStoredHistory = createHistoryTextField(PACKAGE_PATTERN_KEY, DEFAULT_PACKAGE_PATTERNS);

        inboxSourcesPatternTextFieldWithStoredHistory = createHistoryTextField(INBOX_SOURCE_PATTERN_KEY, DEFAULT_SOURCES_INBOX_PATTERNS);

        outboxSourcesPatternTextFieldWithStoredHistory = createHistoryTextField(OUTBOX_SOURCE_PATTERN_KEY, DEFAULT_SOURCES_OUTBOX_PATTERNS);

        String defaultOutputPath = "";
        if (defaultModule != null) {
            VirtualFile moduleFile = defaultModule.getModuleFile();
            VirtualFile parent = moduleFile != null ? moduleFile.getParent() : null;
            if (parent != null) {
                defaultOutputPath = parent.getPath();
            }
        }
        final TextFieldWithStoredHistory outputPathHistory = createHistoryTextField(PLUGIN_PACKER_OUTPUT_PATH_KEY, defaultOutputPath);
        outputPathTextFieldWithBrowseButton = new ComponentWithBrowseButton<TextFieldWithStoredHistory>(outputPathHistory, null);
        fixButton(outputPathHistory, outputPathTextFieldWithBrowseButton);
    }

    private void fixButton(final TextFieldWithStoredHistory historyField, ComponentWithBrowseButton<TextFieldWithStoredHistory> control) {
        FixedSizeButton button = control.getButton();
        control.remove(button);
        BorderLayout borderLayout = new BorderLayout();
        JPanel buttonPanel = new JPanel(borderLayout);
        buttonPanel.setBorder(IdeBorderFactory.createEmptyBorder(4, 0, 4, 0));
        buttonPanel.add(button, BorderLayout.CENTER);
        control.add(buttonPanel, BorderLayout.EAST);
        button.setAttachedComponent(new JComponent() {
            public Dimension getPreferredSize() {
                Dimension size = historyField.getTextEditor().getPreferredSize();
                return new Dimension(-1, size.height + 6);
            }

        });
    }

    @SuppressWarnings({"unchecked"})
    private static TextFieldWithStoredHistory createHistoryTextField(@NotNull String name, @NotNull String... defaultValues) {
        TextFieldWithStoredHistory storedHistory = new TextFieldWithStoredHistoryBugFixed(name);
        storedHistory.reset();
        List<String> history = storedHistory.getHistory();
        if (history.isEmpty()) {
            history.addAll(Arrays.asList(defaultValues));
            // Default histories
            storedHistory.setText(defaultValues.length > 0 ? defaultValues[0] : "");
            storedHistory.addCurrentTextToHistory();
        } else {
            for (String defaultValue : defaultValues) {
                if (!history.contains(defaultValue)) {
                    history.add(defaultValue);
                }
            }
            storedHistory.setSelectedItem(history.get(0));
        }
        return storedHistory;
    }

    public final JComponent getRoot() {
        return root;
    }

    private static final class ModuleListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Module module = (Module) value;
            if (module != null) {
                component.setText(module.getName());
                component.setIcon(ModuleType.get(module).getNodeIcon(false));
            } else {
                component.setText(PluginPackerBundle.message("all.plugin.modules"));
                component.setIcon(PluginModuleType.getInstance().getNodeIcon(false));
            }
            return component;
        }
    }

    /**
     * Fixed text component. Enabling is valid now.
     */
    private static final class TextFieldWithStoredHistoryBugFixed extends TextFieldWithStoredHistory {
        private final String propertyName;

        public TextFieldWithStoredHistoryBugFixed(String name) {
            super(name);
            propertyName = name;
        }

        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            getTextEditor().setEnabled(enabled);
            getTextEditor().setEditable(enabled);
        }

        @SuppressWarnings({"unchecked"})
        public void addCurrentTextToHistory() {
            super.addCurrentTextToHistory();
            // Current text must be first one
            String current = getText();
            List<String> history = getHistory();
            history.remove(current);
            history.add(0, current);
            // Full replace property value
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < history.size(); i++) {
                if (i > 0) {
                    builder.append('\n');
                }
                builder.append(history.get(i));
            }
            PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
            propertiesComponent.setValue(propertyName, builder.toString());
        }
    }

    private class ModuleDependedBrowseFolderActionListener extends ComponentWithBrowseButton.BrowseFolderActionListener<TextFieldWithStoredHistory> {
        public ModuleDependedBrowseFolderActionListener(Project project) {
            super(PluginPackerBundle.message("select.output.path"), PluginPackerBundle.message("please.select.output.folder"), PluginPackerForm.this.outputPathTextFieldWithBrowseButton, project, PluginPackerForm.this.outputPathDescriptor, new TextComponentAccessor<TextFieldWithStoredHistory>() {
                public String getText(TextFieldWithStoredHistory component) {
                    return component.getText().replace('\\', '/');
                }

                public void setText(TextFieldWithStoredHistory component, String text) {
                    component.setText(text);
                    component.addCurrentTextToHistory();
                }
            });
        }
    }

}

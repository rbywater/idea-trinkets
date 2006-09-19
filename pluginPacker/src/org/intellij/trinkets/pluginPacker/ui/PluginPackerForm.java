package org.intellij.trinkets.pluginPacker.ui;

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

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * Plugin packer form.
 *
 * @author Alexey Efimov
 */
final class PluginPackerForm {
    private JCheckBox putSourcesTogetherWithCheckBox;
    private ComponentWithBrowseButton<TextFieldWithStoredHistory> outputPathTextFieldWithBrowseButton;
    private JComboBox moduleComboBox;
    private TextFieldWithStoredHistory packagePatternTextFieldWithStoredHistory;
    private TextFieldWithStoredHistory sourcesPatternTextFieldWithStoredHistory;
    private JPanel root;


    public PluginPackerForm(Module[] modules, Module defaultModule) {
        moduleComboBox.setRenderer(new ModuleListRenderer());
        moduleComboBox.setModel(new DefaultComboBoxModel(modules));
        if (defaultModule != null) {
            moduleComboBox.setSelectedItem(defaultModule);
        } else {
            moduleComboBox.setSelectedIndex(-1);
        }
        putSourcesTogetherWithCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                sourcesPatternTextFieldWithStoredHistory.setEnabled(putSourcesTogetherWithCheckBox.isSelected());
            }
        });
        TextFieldWithStoredHistory storedHistory = outputPathTextFieldWithBrowseButton.getChildComponent();
        storedHistory.reset();
        List list = storedHistory.getHistory();
        if (defaultModule != null) {
            VirtualFile parent = defaultModule.getModuleFile().getParent();
            if (parent != null) {
                if (list.isEmpty()) {
                    storedHistory.setText(parent.getPath());
                    storedHistory.addCurrentTextToHistory();
                }
            }
        }
        if (!list.isEmpty()) {
            storedHistory.setSelectedItem((String) list.get(list.size() - 1));
        }
    }

    public final Module getModule() {
        return (Module) moduleComboBox.getSelectedItem();
    }

    public final String getPackagePattern() {
        return packagePatternTextFieldWithStoredHistory.getText();
    }

    public final String getSourcesPattern() {
        return sourcesPatternTextFieldWithStoredHistory.getText();
    }

    public final String getOutputPath() {
        return outputPathTextFieldWithBrowseButton.getChildComponent().getText();
    }

    public final boolean isIncludeSources() {
        return putSourcesTogetherWithCheckBox.isSelected();
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
        packagePatternTextFieldWithStoredHistory.addCurrentTextToHistory();

        text = sourcesPatternTextFieldWithStoredHistory.getText();
        if (putSourcesTogetherWithCheckBox.isSelected() && StringUtil.isEmptyOrSpaces(text)) {
            Messages.showWarningDialog(sourcesPatternTextFieldWithStoredHistory, "Please, enter pattern for sources package", "Packing plugin");
            sourcesPatternTextFieldWithStoredHistory.requestFocus();
            return false;
        }
        sourcesPatternTextFieldWithStoredHistory.addCurrentTextToHistory();

        text = outputPathTextFieldWithBrowseButton.getChildComponent().getText();
        if (StringUtil.isEmptyOrSpaces(text)) {
            Messages.showWarningDialog(outputPathTextFieldWithBrowseButton, "Please, enter output path", "Packing plugin");
            outputPathTextFieldWithBrowseButton.requestFocus();
            return false;
        }
        outputPathTextFieldWithBrowseButton.getChildComponent().addCurrentTextToHistory();

        File file = new File(text);
        if (file.exists() && !file.isDirectory()) {
            Messages.showWarningDialog(outputPathTextFieldWithBrowseButton, "Output path invalid.\nPlease, enter valid output path.", "Packing plugin");
            outputPathTextFieldWithBrowseButton.requestFocus();
            return false;
        }
        return true;
    }

    private void createUIComponents() {
        packagePatternTextFieldWithStoredHistory = new TextFieldWithStoredHistory("PluginPacker.PackagePattern");
        packagePatternTextFieldWithStoredHistory.reset();
        List list = packagePatternTextFieldWithStoredHistory.getHistory();
        if (list.isEmpty()) {
            packagePatternTextFieldWithStoredHistory.setText("${plugin.id}_${plugin.version}.zip");
            packagePatternTextFieldWithStoredHistory.addCurrentTextToHistory();
        } else {
            packagePatternTextFieldWithStoredHistory.setSelectedItem((String) list.get(list.size() - 1));
        }
        sourcesPatternTextFieldWithStoredHistory = new TextFieldWithStoredHistory("PluginPacker.SourcePattern") {
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                getTextEditor().setEnabled(enabled);
                getTextEditor().setEditable(enabled);
            }
        };
        sourcesPatternTextFieldWithStoredHistory.reset();
        list = sourcesPatternTextFieldWithStoredHistory.getHistory();
        if (list.isEmpty()) {
            sourcesPatternTextFieldWithStoredHistory.setText("src_${plugin.id}.zip");
            sourcesPatternTextFieldWithStoredHistory.addCurrentTextToHistory();
        } else {
            sourcesPatternTextFieldWithStoredHistory.setSelectedItem((String) list.get(list.size() - 1));
        }
        outputPathTextFieldWithBrowseButton = new ComponentWithBrowseButton<TextFieldWithStoredHistory>(
                new TextFieldWithStoredHistory("PluginPacker.OutputPath"), new PathActionListener()
        );
    }

    public final JComponent getRoot() {
        return root;
    }

    private final class PathActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(new Runnable() {
                public void run() {
                    VirtualFile previous = LocalFileSystem.getInstance().refreshAndFindFileByPath(
                            outputPathTextFieldWithBrowseButton.getChildComponent().getText().replace('\\', '/')
                    );

                    FileChooserDescriptor fileDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                    fileDescriptor.setShowFileSystemRoots(true);
                    fileDescriptor.setTitle("Select output path");
                    fileDescriptor.setDescription("Please, select output folder");
                    VirtualFile[] virtualFiles = FileChooser.chooseFiles(outputPathTextFieldWithBrowseButton, fileDescriptor, previous);

                    if (virtualFiles != null && virtualFiles.length > 0) {
                        String path = virtualFiles[0].getPath();
                        outputPathTextFieldWithBrowseButton.getChildComponent().setText(path);
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
}

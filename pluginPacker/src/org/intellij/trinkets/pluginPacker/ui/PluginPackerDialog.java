package org.intellij.trinkets.pluginPacker.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Dialog for {@link org.intellij.trinkets.pluginPacker.ui.PluginPackerForm}
 *
 * @author Alexey Efimov
 */
public final class PluginPackerDialog extends DialogWrapper {
    private final Module[] modules;
    private final Module defaultModule;
    private PluginPackerForm form;

    public PluginPackerDialog(Project project, Module[] modules, Module defaultModule) {
        super(project, true);
        this.modules = modules;
        this.defaultModule = defaultModule;
        init();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        if (form == null) {
            form = new PluginPackerForm(modules, defaultModule);
        }
        return form.getRoot();
    }

    protected void doOKAction() {
        if (form.validate()) {
            super.doOKAction();
        }
    }

    public final Module getModule() {
        return form.getModule();
    }

    public final String getPackagePattern() {
        return form.getPackagePattern();
    }

    public final String getSourcesPattern() {
        return form.getSourcesPattern();
    }

    public final String getOutputPath() {
        return form.getOutputPath();
    }

    public final boolean isIncludeSources() {
        return form.isIncludeSources();
    }

}

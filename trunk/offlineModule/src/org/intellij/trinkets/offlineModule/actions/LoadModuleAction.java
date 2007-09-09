package org.intellij.trinkets.offlineModule.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.trinkets.offlineModule.OfflineModuleManager;
import org.jetbrains.annotations.NonNls;

/**
 * Load IML file to project.
 *
 * @author Alexey Efimov
 */
public final class LoadModuleAction extends AnAction {
    @NonNls
    private static final String IML_EXTENSION = "iml";

    public void actionPerformed(AnActionEvent e) {
        final Project project = DataKeys.PROJECT.getData(e.getDataContext());
        VirtualFile[] files = DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
        if (project != null && files != null && files.length > 0) {
            final ModuleManager moduleManager = ModuleManager.getInstance(project);
            Module[] modules = moduleManager.getModules();
            final OfflineModuleManager offlineModuleManager = OfflineModuleManager.getInstance(project);
            for (final VirtualFile file : files) {
                if (IML_EXTENSION.equalsIgnoreCase(file.getExtension())) {
                    if (isNotLoaded(modules, file)) {
                        offlineModuleManager.enableModule(file);
                    }
                }
            }
        }
    }

    public void update(AnActionEvent e) {
        super.update(e);
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        VirtualFile[] files = DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
        boolean visible = project != null && files != null && files.length > 0 && isIMLFiles(project, files);
        e.getPresentation().setEnabled(visible);
        e.getPresentation().setVisible(visible);
    }

    private boolean isIMLFiles(Project project, VirtualFile[] files) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getModules();
        for (VirtualFile file : files) {
            if (IML_EXTENSION.equalsIgnoreCase(file.getExtension())) {
                if (isNotLoaded(modules, file)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isNotLoaded(Module[] modules, VirtualFile file) {
        boolean notLoaded = true;
        for (int i = 0; i < modules.length && notLoaded; i++) {
            Module module = modules[i];
            if (file.equals(module.getModuleFile())) {
                notLoaded = false;
            }
        }
        return notLoaded;
    }
}

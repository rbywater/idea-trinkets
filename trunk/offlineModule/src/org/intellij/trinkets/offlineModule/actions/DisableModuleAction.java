package org.intellij.trinkets.offlineModule.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.offlineModule.OfflineModuleManager;

/**
 * Disabling module action.
 *
 * @author Alexey Efimov
 */
public final class DisableModuleAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        Module[] modules = DataKeys.MODULE_CONTEXT_ARRAY.getData(e.getDataContext());
        if (project != null && modules != null) {
            OfflineModuleManager offlineModuleManager = OfflineModuleManager.getInstance(project);
            for (Module module : modules) {
                offlineModuleManager.disableModule(module);
            }
        }
    }

    public void update(AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Module[] modules = DataKeys.MODULE_CONTEXT_ARRAY.getData(dataContext);
        boolean visible = modules != null && modules.length > 0 && DataKeys.PROJECT.getData(dataContext) != null;
        e.getPresentation().setEnabled(visible);
        e.getPresentation().setVisible(visible);
    }
}

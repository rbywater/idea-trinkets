package org.intellij.trinkets.pluginPacker.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.intellij.trinkets.pluginPacker.PluginPackerManager;
import org.intellij.trinkets.pluginPacker.ui.PluginPackerDialog;
import org.intellij.trinkets.pluginPacker.util.PluginModuleUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Pack action.
 *
 * @author Alexey Efimov
 */
public final class PackAction extends AnAction {
    private static final Module[] EMPTY_MODULE_ARRAY = new Module[]{};

    public void actionPerformed(AnActionEvent e) {
        final Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        if (project != null) {
            List<Module> pluginModules = getPluginModules(project);
            if (!pluginModules.isEmpty()) {
                Module selectedModule = (Module) e.getDataContext().getData(DataConstants.MODULE);
                final PluginPackerDialog dialog = new PluginPackerDialog(project, pluginModules.toArray(EMPTY_MODULE_ARRAY), selectedModule != null && PluginModuleUtil.isPluginModule(selectedModule) ? selectedModule : null);
                dialog.setTitle("Prepare Plugin for Deploying...");
                dialog.show();
                if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    final Module module = dialog.getModule();
                    CompilerManager.getInstance(project).make(module, new CompileStatusNotification() {
                        public void finished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
                            PluginPackerManager packerManager = PluginPackerManager.getInstance(project);
                            if (!aborted && errors == 0) {
                                String packagePattern = dialog.getPackagePattern();
                                String sourcesPattern = dialog.isBuildSources() ? dialog.getSourcesPattern() : null;
                                boolean isInboxSources = dialog.isInboxSources();
                                String directory = dialog.getOutputPath();
                                packerManager.packModule(module, packagePattern, sourcesPattern, isInboxSources, directory);
                            }
                        }
                    });
                }
            }
        }

    }

    private static List<Module> getPluginModules(Project project) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getSortedModules();
        List<Module> pluginModules = new ArrayList<Module>(modules.length);
        for (Module module : modules) {
            if (PluginModuleUtil.isPluginModule(module)) {
                pluginModules.add(module);
            }
        }
        return pluginModules;
    }

    public void update(AnActionEvent e) {
        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        boolean enabled = project != null && !getPluginModules(project).isEmpty();
        e.getPresentation().setEnabled(enabled);
        e.getPresentation().setVisible(enabled);
    }
}

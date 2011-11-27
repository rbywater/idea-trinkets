package org.intellij.trinkets.pluginPacker.actions;

import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.intellij.trinkets.pluginPacker.PluginPacker;
import org.intellij.trinkets.pluginPacker.ui.PluginPackerDialog;
import org.intellij.trinkets.pluginPacker.util.PluginPackerBundle;
import org.jetbrains.idea.devkit.module.PluginModuleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.File;

/**
 * Pack action.
 *
 * @author Alexey Efimov
 */
public final class PackAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(DataKeys.PROJECT);
        if (project != null) {
            List<Module> pluginModules = getPluginModules(project);
            if (!pluginModules.isEmpty()) {
                Module selectedModule = e.getData(DataKeys.MODULE);
                final PluginPackerDialog dialog = new PluginPackerDialog(
                        project,
                        pluginModules.toArray(new Module[pluginModules.size()]),
                        selectedModule != null &&
                                PluginModuleType.isOfType(selectedModule) ? selectedModule : null
                );
                dialog.setTitle(PluginPackerBundle.message("prepare.plugin.for.deploying"));
                dialog.show();
                if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    final List<Module> modules = new ArrayList<Module>();
                    selectedModule = dialog.getModule();
                    if (selectedModule != null) {
                        modules.add(selectedModule);
                    } else {
                        modules.addAll(pluginModules);
                    }
                    CompilerManager compilerManager = CompilerManager.getInstance(project);
                    compilerManager.make(
                            project,
                            modules.toArray(new Module[modules.size()]),
                            new CompileStatusNotification() {
                                public void finished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
                                    PluginPacker packer = new PluginPacker(project);
                                    if (!aborted && errors == 0) {
                                        final String directory = dialog.getOutputPath();
                                        String packagePattern = dialog.getPackagePattern();
                                        String sourcesPattern = dialog.isBuildSources() ? dialog.getSourcesPattern() : null;
                                        boolean isInboxSources = dialog.isInboxSources();
                                        boolean isSilentOverwrite = dialog.isSilentOverwrite();
                                        for (Module module : modules) {
                                            packer.packModule(module, packagePattern, sourcesPattern, isInboxSources, directory, isSilentOverwrite);
                                        }
                                        if (modules.size() > 0) {
                                            ApplicationManager.getApplication().runReadAction(new Runnable() {
                                                public void run() {
                                                    CompilerUtil.refreshIODirectories(Arrays.asList(new File(directory)));
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                    );
                }
            }
        }

    }

    private static List<Module> getPluginModules(Project project) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getSortedModules();
        List<Module> pluginModules = new ArrayList<Module>(modules.length);
        for (Module module : modules) {
            if (PluginModuleType.isOfType(module)) {
                pluginModules.add(module);
            }
        }
        return pluginModules;
    }

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getData(DataKeys.PROJECT);
        boolean enabled = project != null && !getPluginModules(project).isEmpty();
        e.getPresentation().setEnabled(enabled);
        e.getPresentation().setVisible(enabled);
    }
}

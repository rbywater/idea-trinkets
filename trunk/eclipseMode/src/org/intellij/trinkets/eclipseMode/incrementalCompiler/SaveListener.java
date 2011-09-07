package org.intellij.trinkets.eclipseMode.incrementalCompiler;

import com.intellij.analysis.AnalysisScopeBundle;
import com.intellij.compiler.impl.ModuleCompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.util.ui.UIUtil;
import org.intellij.trinkets.eclipseMode.EclipseMode;
import org.jetbrains.annotations.NotNull;

/**
 * VFS listener to handle save file event.
 *
 * @author Alexey Efimov
 */
public class SaveListener extends VirtualFileAdapter {
    @Override
    public void contentsChanged(VirtualFileEvent event) {
        executeMake(event);
    }

    @Override
    public void fileCreated(VirtualFileEvent event) {
        executeMake(event);
    }

    @Override
    public void fileDeleted(VirtualFileEvent event) {
        executeMake(event);
    }

    @Override
    public void fileMoved(VirtualFileMoveEvent event) {
        executeMake(event);
    }

    @Override
    public void fileCopied(VirtualFileCopyEvent event) {
        executeMake(event);
    }

    private void executeMake(final VirtualFileEvent event) {
        final Project project = ProjectUtil.guessProjectForFile(event.getFile());
        ProgressManager.getInstance().run(new Task.Backgroundable(project, AnalysisScopeBundle.message("analyzing.project"), true) {
            public void run(@NotNull ProgressIndicator indicator) {
                executeMakeInUIThread(event);
            }
        });
    }

    private void executeMakeInUIThread(VirtualFileEvent event) {
        EclipseMode eclipseMode = EclipseMode.getInstance();
        if (event.isFromSave() && eclipseMode.getSettings().INCREMENTAL_COMPILATION_ENABLED) {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (final Project project : projects) {
                if (project.isInitialized() && !project.isDisposed() &&
                        project.isOpen() && !project.isDefault()) {
                    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
                    final Module module = projectFileIndex.getModuleForFile(event.getFile());
                    if (module != null) {
                        final CompilerManager compilerManager = CompilerManager.getInstance(project);
                        if (!compilerManager.isCompilationActive() &&
                                !compilerManager.isExcludedFromCompilation(event.getFile()) &&
                                !compilerManager.isUpToDate(new ModuleCompileScope(module, false))) {
                            // Changed file found in module. Make it.
                            UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                                public void run() {
                                    compilerManager.make(module, null);
                                }
                            });
                        }
                    }
                }
            }
        }
    }
}

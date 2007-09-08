package org.intellij.trinkets.editorTree;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import org.intellij.trinkets.editorTree.view.EditorTreeViewManager;
import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.EditorTreeViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Editor groups manager component
 *
 * @author Alexey Efimov
 */
public class EditorTreeManager implements ProjectComponent, FileEditorManagerListener, EditorFactoryListener {
    protected final Project project;
    protected final EditorTreeViewManager viewManager;
    protected final FileEditorManagerEx fileEditorManager;
    protected final EditorFactory editorFactory;
    private static final int UPDATE_DELAY = 100;
    private final Alarm updateTrigger = new Alarm();
    private final Runnable modelUpdateTask = new ViewModelUpdateTask();

    public EditorTreeManager(EditorFactory editorFactory, Project project, EditorTreeViewManager viewManager, FileEditorManagerEx fileEditorManager) {
        this.editorFactory = editorFactory;
        this.project = project;
        this.viewManager = viewManager;
        this.fileEditorManager = fileEditorManager;
    }

    public static EditorTreeManager getInstance(Project project) {
        return project.getComponent(EditorTreeManager.class);
    }

    public EditorTreeViewManager getViewManager() {
        return viewManager;
    }

    public void projectOpened() {
        fileEditorManager.addFileEditorManagerListener(this);
    }

    public void projectClosed() {
        fileEditorManager.removeFileEditorManagerListener(this);
    }

    @NotNull
    @NonNls
    public String getComponentName() {
        return "EditorTreeManager";
    }

    public void initComponent() {
        editorFactory.addEditorFactoryListener(this);
    }

    public void disposeComponent() {
        editorFactory.removeEditorFactoryListener(this);
    }

    public void fileOpened(FileEditorManager source, VirtualFile file) {
        viewManager.getView().getModel().addFile(file);
    }

    public void fileClosed(FileEditorManager source, VirtualFile file) {
        viewManager.getView().getModel().removeFile(file);
    }

    public void selectionChanged(FileEditorManagerEvent event) {
        viewManager.getView().setSelectedFile(event.getNewFile());
    }

    private void rebuildEditorTree() {
        updateTrigger.cancelAllRequests();
        updateTrigger.addRequest(modelUpdateTask, UPDATE_DELAY);
    }

    public void editorCreated(EditorFactoryEvent event) {
        if (project.equals(event.getEditor().getProject())) {
            rebuildEditorTree();
        }
    }

    public void editorReleased(EditorFactoryEvent event) {
        // Since Bundles have wrong editor
        // if (project.equals(event.getEditor().getProject())) {
        rebuildEditorTree();
        // }
    }

    private class ViewModelUpdateTask implements Runnable {
        public void run() {
            Set<VirtualFile> openFiles = new HashSet<VirtualFile>(Arrays.asList(fileEditorManager.getOpenFiles()));
            EditorTreeView view = viewManager.getView();
            EditorTreeViewModel model = view.getModel();
            VirtualFile[] files = model.getFiles();
            for (VirtualFile file : files) {
                if (openFiles.contains(file)) {
                    openFiles.remove(file);
                } else {
                    model.removeFile(file);
                }
            }
            for (VirtualFile file : openFiles) {
                model.addFile(file);
            }
            view.setSelectedFile(fileEditorManager.getCurrentFile());
        }
    }
}

package org.intellij.trinkets.editorTree.impl;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import org.intellij.trinkets.editorTree.EditorTreeManager;
import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.EditorTreeViewManager;
import org.intellij.trinkets.editorTree.view.EditorTreeViewModel;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class EditorTreeManagerImpl extends EditorTreeManager implements ProjectComponent, FileEditorManagerListener, EditorFactoryListener {
    private final Project project;
    private final EditorTreeViewManager viewManager;
    private final FileEditorManagerEx fileEditorManager;
    private final EditorFactory editorFactory;

    private static final int UPDATE_DELAY = 100;

    private final Alarm updateTrigger = new Alarm();
    private final Runnable modelUpdateTask = new ViewModelUpdateTask();

    public EditorTreeManagerImpl(Project project, EditorTreeViewManager viewManager, FileEditorManagerEx fileEditorManager, EditorFactory editorFactory) {
        this.project = project;
        this.viewManager = viewManager;
        this.fileEditorManager = fileEditorManager;
        this.editorFactory = editorFactory;
    }

    public void projectOpened() {
        fileEditorManager.addFileEditorManagerListener(this);
    }

    public void projectClosed() {
        fileEditorManager.removeFileEditorManagerListener(this);
    }

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

    public EditorTreeViewManager getViewManager() {
        return viewManager;
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

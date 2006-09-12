package org.intellij.trinkets.editorTree.view.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeDataConstants;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeViewAction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Close All But This action.
 *
 * @author Alexey Efimov
 */
public final class CloseAllButThisAction extends EditorTreeViewAction {
    protected void actionPerformed(EditorTreeView view, AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = (Project)e.getDataContext().getData(EditorTreeDataConstants.PROJECT);
        VirtualFile[] files = (VirtualFile[])dataContext.getData(EditorTreeDataConstants.VIRTUAL_FILE_ARRAY);
        Set<VirtualFile> keepFiles = new HashSet<VirtualFile>(Arrays.asList(files));
        VirtualFile[] allFiles = view.getModel().getFiles();
        FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(project);
        for (VirtualFile file : allFiles) {
            if (keepFiles.contains(file)) {
                keepFiles.remove(file);
            } else {
                fileEditorManager.closeFile(file);
            }
        }
    }
}

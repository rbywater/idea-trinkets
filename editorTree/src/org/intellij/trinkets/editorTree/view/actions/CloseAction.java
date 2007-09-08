package org.intellij.trinkets.editorTree.view.actions;

import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeDataConstants;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeViewAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;

/**
 * Close action.
 *
 * @author Alexey Efimov
 */
public final class CloseAction extends EditorTreeViewAction {
    protected void actionPerformed(EditorTreeView view, AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        VirtualFile[] files = (VirtualFile[])dataContext.getData(EditorTreeDataConstants.VIRTUAL_FILE_ARRAY);
        if (files != null) {
            Project project = (Project)e.getDataContext().getData(EditorTreeDataConstants.PROJECT);
            FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(project);
            for (VirtualFile file : files) {
                fileEditorManager.closeFile(file);
            }
        }
    }
}

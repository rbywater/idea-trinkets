package org.intellij.trinkets.editorTree.view.actions;

import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeDataConstants;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeViewAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;

/**
 * Jump to editor.
 *
 * @author Alexey Efimov
 */
public final class JumpToEditorAction extends EditorTreeViewAction {
    protected void actionPerformed(EditorTreeView view, AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = (Project)e.getDataContext().getData(EditorTreeDataConstants.PROJECT);
        VirtualFile[] files = (VirtualFile[])dataContext.getData(EditorTreeDataConstants.VIRTUAL_FILE_ARRAY);
        FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(project);
        for (VirtualFile file : files) {
            fileEditorManager.openFile(file, true);
        }
        view.setVisible(false);
    }

    protected void update(EditorTreeView view, AnActionEvent e) {
        super.update(view, e);
        if (e.getPresentation().isEnabled()) {
            DataContext dataContext = e.getDataContext();
            VirtualFile[] files = (VirtualFile[])dataContext.getData(EditorTreeDataConstants.VIRTUAL_FILE_ARRAY);
            e.getPresentation().setEnabled(files != null && files.length == 1);
        }
    }
}

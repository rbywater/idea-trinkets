package org.intellij.trinkets.editorTree.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.intellij.trinkets.editorTree.EditorTreeManager;
import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.EditorTreeViewManager;
import org.intellij.trinkets.editorTree.view.ui.EditorTreeViewDialog;

public final class ShowEditorTreeAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = (Project)e.getDataContext().getData(DataConstants.PROJECT);
        EditorTreeViewManager manager = EditorTreeManager.getInstance(project).getViewManager();
        EditorTreeView view = manager.getView();
        if (view.getModel().getFilesCount() > 0) {
            DialogWrapper dialog = new EditorTreeViewDialog(project, view);
            dialog.show();
        }
    }

    public void update(AnActionEvent e) {
        Project project = (Project)e.getDataContext().getData(DataConstants.PROJECT);
        Presentation presentation = e.getPresentation();
        presentation.setVisible(project != null);
        if (project != null) {
            EditorTreeViewManager manager = EditorTreeManager.getInstance(project).getViewManager();
            presentation.setEnabled(manager.getView().getModel().getFilesCount() > 0);
        }
    }
}

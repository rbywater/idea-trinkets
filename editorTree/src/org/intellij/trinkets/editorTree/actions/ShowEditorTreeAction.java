package org.intellij.trinkets.editorTree.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.intellij.trinkets.editorTree.EditorTreeManager;
import org.intellij.trinkets.editorTree.util.EditorTreeBundle;
import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.EditorTreeViewManager;

import javax.swing.*;

public final class ShowEditorTreeAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        if (project != null) {
            EditorTreeViewManager manager = EditorTreeManager.getInstance(project).getViewManager();
            EditorTreeView view = manager.getView();
            if (view.getModel().getFilesCount() > 0) {
                JComponent content = view.getComponent();
                ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(content, content);
                popupBuilder.setCancelKeyEnabled(true);
                popupBuilder.setCancelOnOtherWindowOpen(true);
                popupBuilder.setCancelOnClickOutside(true);
                popupBuilder.setDimensionServiceKey(project, "EditorTree.Popup", false);
                popupBuilder.setLocateByContent(true);
                popupBuilder.setMovable(true);
                popupBuilder.setResizable(true);
                popupBuilder.setTitle(EditorTreeBundle.message("popup.title"));
                popupBuilder.setRequestFocus(true);
                JBPopup popup = popupBuilder.createPopup();
                popup.showCenteredInCurrentWindow(project);
            }
        }
    }

    public void update(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        Presentation presentation = e.getPresentation();
        presentation.setVisible(project != null);
        if (project != null) {
            EditorTreeViewManager manager = EditorTreeManager.getInstance(project).getViewManager();
            presentation.setEnabled(manager.getView().getModel().getFilesCount() > 0);
        }
    }
}

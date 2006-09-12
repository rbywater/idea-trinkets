package org.intellij.trinkets.editorTree.view.actionSystem;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import org.intellij.trinkets.editorTree.view.EditorTreeView;

/**
 * View action
 *
 * @author Alexey Efimov
 */
public abstract class EditorTreeViewAction extends AnAction {
    public final void actionPerformed(AnActionEvent e) {
        final EditorTreeView view = (EditorTreeView)e.getDataContext().getData(EditorTreeDataConstants.EDITOR_TREE_VIEW);
        if (view != null) {
            // Make view not closable by focus lost.
            // Closing editors will request focus on next opened editor
            EditorTreeViewActionMonitor monitor = EditorTreeViewActionMonitor.getInstance();
            monitor.notifyStartDispatching();
            try {
                actionPerformed(view, e);
            } finally {
                Application application = ApplicationManager.getApplication();
                application.invokeLater(new Runnable() {
                    public void run() {
                        EditorTreeViewActionMonitor monitor = EditorTreeViewActionMonitor.getInstance();
                        monitor.notifyStopDispatching();
                    }
                });
            }
        }
    }

    protected abstract void actionPerformed(EditorTreeView view, AnActionEvent e);

    public final void update(AnActionEvent e) {
        EditorTreeView view = (EditorTreeView)e.getDataContext().getData(EditorTreeDataConstants.EDITOR_TREE_VIEW);
        if (view != null) {
            e.getPresentation().setEnabled(true);
            update(view, e);
        } else {
            e.getPresentation().setEnabled(false);
        }
    }

    protected void update(EditorTreeView view, AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        VirtualFile[] files = (VirtualFile[])dataContext.getData(EditorTreeDataConstants.VIRTUAL_FILE_ARRAY);
        Project project = (Project)e.getDataContext().getData(EditorTreeDataConstants.PROJECT);
        e.getPresentation().setEnabled(project != null && files != null && files.length > 0);
    }
}

package org.intellij.trinkets.editorTree.view.impl;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.EditorTreeViewManager;
import org.jetbrains.annotations.NonNls;

final class EditorTreeViewManagerImpl implements EditorTreeViewManager, ProjectComponent {
    private final EditorTreeView view;

    public EditorTreeViewManagerImpl(Project project) {
        view = new EditorTreeViewImpl(project);
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    @NonNls
    public String getComponentName() {
        return "EditorTreeViewManager";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
        view.dispose();
    }

    public EditorTreeView getView() {
        return view;
    }
}

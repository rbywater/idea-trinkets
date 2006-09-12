package org.intellij.trinkets.editorTree;

import com.intellij.openapi.project.Project;
import org.intellij.trinkets.editorTree.view.EditorTreeViewManager;

/**
 * Editor groups manager component
 *
 * @author Alexey Efimov
 */
public abstract class EditorTreeManager {
    public static EditorTreeManager getInstance(Project project) {
        return project.getComponent(EditorTreeManager.class);
    }

    public abstract EditorTreeViewManager getViewManager();
}

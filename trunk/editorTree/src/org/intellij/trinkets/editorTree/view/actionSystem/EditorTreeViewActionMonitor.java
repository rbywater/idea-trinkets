package org.intellij.trinkets.editorTree.view.actionSystem;

import com.intellij.openapi.application.ApplicationManager;

import javax.swing.event.ChangeListener;

/**
 * Monitor to control actions executing
 *
 * @author Alexey Efimov
 */
public abstract class EditorTreeViewActionMonitor {
    public static EditorTreeViewActionMonitor getInstance() {
        return ApplicationManager.getApplication().getComponent(EditorTreeViewActionMonitor.class);
    }

    public abstract void notifyStartDispatching();

    public abstract void notifyStopDispatching();

    public abstract boolean isDispatchInProgress();

    public abstract void addChangeListener(ChangeListener listener);

    public abstract void removeChangeListener(ChangeListener listener);
}

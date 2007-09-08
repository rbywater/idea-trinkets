package org.intellij.trinkets.editorTree.view.actionSystem;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.HashSet;

/**
 * Monitor to control actions executing
 *
 * @author Alexey Efimov
 */
public class EditorTreeViewActionMonitor implements ApplicationComponent {
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>();
    private boolean dispatchInProgress = false;

    public static EditorTreeViewActionMonitor getInstance() {
        return ApplicationManager.getApplication().getComponent(EditorTreeViewActionMonitor.class);
    }

    public void notifyStartDispatching() {
        dispatchInProgress = true;
        fireChangeEvent();
    }

    public void notifyStopDispatching() {
        dispatchInProgress = false;
        fireChangeEvent();
    }

    public boolean isDispatchInProgress() {
        return dispatchInProgress;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    @NotNull
    @NonNls
    public String getComponentName() {
        return "EditorTreeViewActionMonitor";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
}

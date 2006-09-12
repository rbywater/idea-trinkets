package org.intellij.trinkets.editorTree.view.actionSystem.impl;

import com.intellij.openapi.components.ApplicationComponent;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeViewActionMonitor;
import org.jetbrains.annotations.NonNls;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Set;
import java.util.HashSet;

public class EditorTreeViewActionMonitorImpl extends EditorTreeViewActionMonitor implements ApplicationComponent {
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>();
    private boolean dispatchInProgress = false;


    @NonNls
    public String getComponentName() {
        return "EditorTreeViewActionMonitor";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
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

    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
}

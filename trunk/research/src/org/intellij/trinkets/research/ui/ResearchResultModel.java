package org.intellij.trinkets.research.ui;

import org.intellij.trinkets.research.ResearchEngine;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;

/**
 * Research results model.
 *
 * @author Alexey Efimov
 */
public class ResearchResultModel {
    private final List<ResearchEngine> items = new ArrayList<ResearchEngine>();
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(0);

    public void clear() {
        for (ResearchEngine engine : items) {
            engine.reset();
        }
        fireChangeEvent();
    }

    public int size() {
        return items.size();
    }

    public Iterator<ResearchEngine> engines() {
        return items.iterator();
    }

    public void add(ResearchEngine engine) {
        items.add(engine);
        fireChangeEvent();
    }

    public void remove(ResearchEngine engine) {
        items.remove(engine);
        fireChangeEvent();
    }

    public void removeAll() {
        items.clear();
        fireChangeEvent();
    }

    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
}

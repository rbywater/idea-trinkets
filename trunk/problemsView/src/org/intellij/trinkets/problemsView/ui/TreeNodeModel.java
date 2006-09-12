package org.intellij.trinkets.problemsView.ui;

import org.jetbrains.annotations.NotNull;

import javax.swing.event.ChangeListener;

/**
 * Tree model.
 *
 * @author Alexey Efimov
 */
public interface TreeNodeModel<T extends TreeNodeElement> extends Iterable<T> {
    void add(@NotNull T element);

    void remove(@NotNull T element);

    void removeAll();

    void addChangeListener(ChangeListener changeListener);

    void removeChangeListener(ChangeListener changeListener);

}

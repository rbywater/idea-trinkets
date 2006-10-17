package org.intellij.trinkets.editorTree.history.tree.elements;

import com.intellij.ui.ColoredTreeCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Comparator;

/**
 * Renderer delegate for tree element.
 *
 * @author Alexey Efimov
 */
public interface TreeElement<T> {
    TreeElement[] EMPTY_ARRAY = new TreeElement[]{};

    String getSpeedSearchText();

    void render(ColoredTreeCellRenderer renderer, JTree tree, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus);

    @NotNull
    T getValue();

    boolean isLeaf();

    int getChildCount();

    TreeElement getChild(int index);

    int getIndexOfChild(@NotNull TreeElement child);

    void sortChildren(@NotNull Comparator comparator);

    void addChild(TreeElement child);

    void removeChild(TreeElement child);

    TreeElement findChildByValue(@NotNull Object value);
}

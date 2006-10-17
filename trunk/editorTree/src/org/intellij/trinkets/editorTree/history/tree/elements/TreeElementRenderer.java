package org.intellij.trinkets.editorTree.history.tree.elements;

import com.intellij.ui.ColoredTreeCellRenderer;

import javax.swing.*;

/**
 * Renderer for {@link JTree}
 *
 * @author Alexey Efimov
 */
public final class TreeElementRenderer extends ColoredTreeCellRenderer {
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        TreeElement element = (TreeElement) value;
        if (element != null) {
            element.render(this, tree, selected, expanded, leaf, row, hasFocus);
        }
    }
}

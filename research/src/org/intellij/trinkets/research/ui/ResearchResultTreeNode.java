package org.intellij.trinkets.research.ui;

import com.intellij.ui.ColoredTreeCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Renderer for research result
 *
 * @author Alexey Efimov
 */
public interface ResearchResultTreeNode {
    String getPresentableName();

    void render(ColoredTreeCellRenderer coloredTreeCellRenderer);

    void notifyMouseClicked(MouseEvent e);

    void notifyMouseMoved(MouseEvent e);
}

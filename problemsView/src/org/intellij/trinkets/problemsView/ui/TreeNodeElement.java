package org.intellij.trinkets.problemsView.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Tree node.
 *
 * @author Alexey Efimov
 */
public interface TreeNodeElement {
    @Nullable
    Icon getIcon();

    @NotNull
    String getNodeText();

    @Nullable
    String getToolTipText();

    @Nullable
    String getStatusBarText();
}

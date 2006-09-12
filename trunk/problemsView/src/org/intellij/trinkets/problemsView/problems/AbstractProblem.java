package org.intellij.trinkets.problemsView.problems;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Abstract implementation.
 *
 * @author Alexey Efimov
 */
public abstract class AbstractProblem implements Problem {
    @Nullable
    public Icon getIcon() {
        return getType().getNodeIcon();
    }

    @Nullable
    public String getToolTipText() {
        return null;
    }

    @Nullable
    public String getStatusBarText() {
        return null;
    }
}

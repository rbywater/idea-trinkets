package org.intellij.trinkets.problemsView.problems;

import org.intellij.trinkets.problemsView.icons.ProblemViewIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Problem types.
 *
 * @author Alexey Efimov
 */
public enum ProblemType {
    INFO(ProblemViewIcons.NODE_INFO, ProblemViewIcons.TOOLWINDOW_INFO, 1),
    WARNING(ProblemViewIcons.NODE_WARNING, ProblemViewIcons.TOOLWINDOW_WARNING, 2),
    ERROR(ProblemViewIcons.NODE_ERROR, ProblemViewIcons.TOOLWINDOW_ERROR, 3);

    private final Icon nodeIcon;
    private final Icon toolWindowIcon;
    private final int priority;

    ProblemType(Icon nodeIcon, Icon toolWindowIcon, int priority) {
        this.nodeIcon = nodeIcon;
        this.toolWindowIcon = toolWindowIcon;
        this.priority = priority;
    }

    public Icon getNodeIcon() {
        return nodeIcon;
    }

    public Icon getToolWindowIcon() {
        return toolWindowIcon;
    }

    public int getPriority() {
        return priority;
    }

    @NotNull
    public static ProblemType priority(@NotNull ProblemType t1, @NotNull ProblemType t2) {
        return t1.priority >= t2.priority ? t1 : t2;
    }
}

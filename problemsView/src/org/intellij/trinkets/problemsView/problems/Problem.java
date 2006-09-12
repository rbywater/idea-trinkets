package org.intellij.trinkets.problemsView.problems;

import org.intellij.trinkets.problemsView.ui.TreeNodeElement;
import org.jetbrains.annotations.NotNull;

/**
 * Problem.
 *
 * @author Alexey Efimov
 */
public interface Problem extends TreeNodeElement {
    Problem[] EMPTY_PROBLEM_ARRAY = new Problem[]{};

    /**
     * Get problem type.
     *
     * @return Problem type.
     */
    @NotNull
    ProblemType getType();

    /**
     * Return fixes for this problem.
     *
     * @return Fixes array.
     */
    @NotNull
    ProblemFix[] getFixes();
}

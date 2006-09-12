package org.intellij.trinkets.problemsView.inspections;

import org.intellij.trinkets.problemsView.problems.Problem;
import org.jetbrains.annotations.NotNull;

/**
 * Problem inspection.
 *
 * @author Alexey Efimov
 */
public interface ProblemInspection {
    /**
     * Perform inspection and, is it found a problem, will report it.
     *
     * @return Reported problem, or <code>null</code> if no problem found
     */
    @NotNull
    Problem[] inspect();
}

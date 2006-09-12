package org.intellij.trinkets.problemsView.problems;

import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Problem manager.
 *
 * @author Alexey Efimov
 */
public interface ProblemManager extends Iterable<Problem> {
    void reinspect();

    /**
     * Append problem to list of problems.
     *
     * @param problem Problem
     */
    void addProblem(@NotNull Problem problem);

    /**
     * Remove problem from list (basicaly after resolved).
     *
     * @param problem Problem
     */
    void removeProblem(@NotNull Problem problem);

    /**
     * Append problem inspection.
     *
     * @param problemInspection Problem inspector
     */
    void addProblemInspection(@NotNull ProblemInspection problemInspection);

    /**
     * Remove problem inspection.
     *
     * @param problemInspection Problem inspector
     */
    void removeProblemInspection(@NotNull ProblemInspection problemInspection);

    void addProblemManagerListener(@NotNull ProblemManagerListener listener);

    void removeProblemManagerListener(@NotNull ProblemManagerListener listener);

    boolean isIdle();

    boolean isPaused();

    void setPaused(boolean paused);
}

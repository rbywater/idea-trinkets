package org.intellij.trinkets.problemsView;

import org.jetbrains.annotations.NotNull;

/**
 * Problem view manager.
 *
 * @author Alexey Efimov
 */
public interface ProblemViewManager {
    @NotNull
    ProblemView getView();
}

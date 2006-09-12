package org.intellij.trinkets.problemsView;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.jetbrains.annotations.NotNull;

/**
 * Problem view.
 *
 * @author Alexey Efimov
 */
public interface ProblemView extends Disposable {
    Project getProject();

    /**
     * Return currently selected problems.
     *
     * @return Array of selected problems
     */
    @NotNull
    Problem[] getSelectedProblems();
}

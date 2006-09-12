package org.intellij.trinkets.problemsView.problems;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * The quck fix of problem. This is automaticaly fix
 * can be invoked for problems, what has it.
 *
 * @author Alexey Efimov
 */
public interface ProblemFix {
    @NotNull
    String getName();

    void applyFix(@NotNull Project project);
}

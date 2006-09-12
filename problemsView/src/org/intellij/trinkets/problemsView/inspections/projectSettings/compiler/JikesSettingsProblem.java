package org.intellij.trinkets.problemsView.inspections.projectSettings.compiler;

import org.intellij.trinkets.problemsView.problems.AbstractProblem;
import org.intellij.trinkets.problemsView.problems.ProblemFix;
import org.intellij.trinkets.problemsView.problems.ProblemType;
import org.jetbrains.annotations.NotNull;

/**
 * Problem with compiler.
 *
 * @author Alexey Efimov
 */
public class JikesSettingsProblem extends AbstractProblem {
    private static final ProblemFix[] FIXES = new ProblemFix[]{
            new OpenCompilerSettingsFix()
    };

    @NotNull
    public ProblemType getType() {
        return ProblemType.ERROR;
    }

    @NotNull
    public ProblemFix[] getFixes() {
        return FIXES;
    }

    @NotNull
    public String getNodeText() {
        return "Jikes compiler not found";
    }
}

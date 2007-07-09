package org.intellij.trinkets.problemsView.inspections.projectSettings.compiler;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.openapi.project.Project;
import org.apache.oro.text.regex.MalformedPatternException;
import org.intellij.trinkets.problemsView.problems.AbstractProblem;
import org.intellij.trinkets.problemsView.problems.ProblemFix;
import org.intellij.trinkets.problemsView.problems.ProblemType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Check resources pattern. Some patterns maybe missed.
 *
 * @author Alexey Efimov
 */
public class ResourcesPatternProblem extends AbstractProblem {
    private final String[] missedPatterns;

    private final ProblemFix[] problemFixes = new ProblemFix[]{
            new AppendPatternsFix(),
            new OpenCompilerSettingsFix()
    };

    public ResourcesPatternProblem(@NotNull String[] missedPatterns) {
        this.missedPatterns = missedPatterns;
    }

    @NotNull
    public ProblemType getType() {
        return ProblemType.WARNING;
    }

    @NotNull
    public ProblemFix[] getFixes() {
        return problemFixes;
    }

    @NotNull
    public String getNodeText() {
        return "Missed <b>Resource Patterns</b> in Compiler settings " + Arrays.asList(missedPatterns);
    }

    private class AppendPatternsFix implements ProblemFix {
        @NotNull
        public String getName() {
            return "Append patterns to Compiler settings";
        }

        public void applyFix(@NotNull Project project) {
            CompilerConfiguration compilerConfiguration = project.getComponent(CompilerConfiguration.class);
            for (String pattern : missedPatterns) {
//                try {
//                    compilerConfiguration.addResourceFilePattern(pattern);
//                } catch (MalformedPatternException e) {
//                    // Ignore
//                }
            }
        }
    }

}

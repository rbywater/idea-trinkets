package org.intellij.trinkets.problemsView.inspections.projectSettings.compiler;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.impl.javaCompiler.jikes.JikesCompiler;
import com.intellij.compiler.impl.javaCompiler.jikes.JikesSettings;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.intellij.trinkets.problemsView.util.FindFileUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Inspection for jikes compiler availability.
 *
 * @author Alexey Efimov
 */
public class JikesSettingsInspection implements ProblemInspection {
    private final Project project;

    public JikesSettingsInspection(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    public Problem[] inspect() {
        CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(project);
        if (compilerConfiguration.getDefaultCompiler() instanceof JikesCompiler) {
            try {
                if (FindFileUtil.isValidUrl(JikesSettings.getInstance(project).JIKES_PATH)) {
                    return Problem.EMPTY_PROBLEM_ARRAY;
                }
            } catch (Throwable t) {
                // Ignore error here
            }
            return new Problem[]{new JikesSettingsProblem()};
        }
        return Problem.EMPTY_PROBLEM_ARRAY;

    }
}

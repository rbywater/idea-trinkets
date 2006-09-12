package org.intellij.trinkets.problemsView.inspections.applicationSettings.jdks;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectRootConfigurable;
import org.intellij.trinkets.problemsView.problems.AbstractProblem;
import org.intellij.trinkets.problemsView.problems.ConfigurableFix;
import org.intellij.trinkets.problemsView.problems.ProblemFix;
import org.intellij.trinkets.problemsView.problems.ProblemType;
import org.jetbrains.annotations.NotNull;

/**
 * Jdk Problem
 *
 * @author Alexey Efimov
 */
public class JdkProblem extends AbstractProblem {
    private final ProjectJdk jdk;

    public JdkProblem(ProjectJdk jdk) {
        this.jdk = jdk;
    }

    @NotNull
    public ProblemType getType() {
        return ProblemType.ERROR;
    }

    @NotNull
    public ProblemFix[] getFixes() {
        return new ProblemFix[]{
                new ConfigurableFix("Show Project Roots setting") {
                    @NotNull
                    protected Configurable getConfigurable(@NotNull Application application, @NotNull Project project) {
                        return project.getComponent(ProjectRootConfigurable.class);
                    }
                }
        };
    }

    @NotNull
    public String getNodeText() {
        return "JDK <b>" + jdk.getName() + "</b> is invalid (wrong location)";
    }
}

package org.intellij.trinkets.problemsView.inspections.projectSettings.libraries;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectRootConfigurable;
import org.intellij.trinkets.problemsView.problems.AbstractProblem;
import org.intellij.trinkets.problemsView.problems.ConfigurableFix;
import org.intellij.trinkets.problemsView.problems.ProblemFix;
import org.intellij.trinkets.problemsView.problems.ProblemType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Problem in library classes.
 *
 * @author Alexey Efimov
 */
public class LibraryProblem extends AbstractProblem {
    private static final ProblemFix[] FIXES = new ProblemFix[]{
            new ShowProjectRootConfigurableFix()
    };

    private final String text;
    private final ProblemType type;

    public LibraryProblem(String rootName, String name, @NotNull List<OrderRootType> rootTypes) {
        ProblemType type = ProblemType.INFO;
        String missed = "";
        if (rootTypes.contains(OrderRootType.CLASSES)) {
            type = ProblemType.priority(type, ProblemType.ERROR);
            missed += "<b>classes</b>";
        }

        if (rootTypes.contains(OrderRootType.SOURCES)) {
            type = ProblemType.priority(type, ProblemType.WARNING);
            missed += missed.length() == 0 ? "<b>sources</b>" : rootTypes.size() == 2 ? " and <b>sources</b>" : ", <b>sources</b>";
        }

        if (rootTypes.contains(OrderRootType.JAVADOC)) {
            type = ProblemType.priority(type, ProblemType.INFO);
            missed += rootTypes.size() == 1 ? "<b>JavaDocs</b>" : " and <b>JavaDocs</b>";
        }
        this.type = type;
        this.text = rootName + " <b>" + name + "</b> is invalid (wrong " + missed + " references)";
    }

    @NotNull
    public ProblemType getType() {
        return type;
    }

    @NotNull
    public ProblemFix[] getFixes() {
        return FIXES;
    }

    @NotNull
    public String getNodeText() {
        return text;
    }

    private static class ShowProjectRootConfigurableFix extends ConfigurableFix {
        public ShowProjectRootConfigurableFix() {
            super("Show Project Roots configuration");
        }

        @NotNull
        protected Configurable getConfigurable(@NotNull Application application, @NotNull Project project) {
            return project.getComponent(ProjectRootConfigurable.class);
        }
    }
}

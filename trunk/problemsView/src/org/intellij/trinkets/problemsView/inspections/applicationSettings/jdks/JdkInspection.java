package org.intellij.trinkets.problemsView.inspections.applicationSettings.jdks;

import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.ProjectRootType;
import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.intellij.trinkets.problemsView.util.FindFileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Inspection for JDK settings.
 *
 * @author Alexey Efimov
 */
public class JdkInspection implements ProblemInspection {

    @NotNull
    public Problem[] inspect() {
        List<Problem> problems = new ArrayList<Problem>();
        ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
        ProjectJdk[] projectJdks = jdkTable.getAllJdks();
        if (projectJdks != null && projectJdks.length > 0) {
            for (ProjectJdk jdk : projectJdks) {
                if (!FindFileUtil.isValidUrl(jdk.getBinPath()) ||
                        !FindFileUtil.isValidUrl(jdk.getHomePath()) ||
                        !FindFileUtil.isValidUrl(jdk.getRtLibraryPath()) ||
                        !FindFileUtil.isValidUrl(jdk.getToolsPath())
                        ) {
                    problems.add(new JdkProblem(jdk));
                }
            }
        } else {
            problems.add(new NoJdkProblem());
        }
        return problems.toArray(Problem.EMPTY_PROBLEM_ARRAY);
    }
}

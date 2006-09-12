package org.intellij.trinkets.problemsView.inspections.projectSettings;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.intellij.trinkets.problemsView.inspections.projectSettings.compiler.JikesSettingsInspection;
import org.intellij.trinkets.problemsView.inspections.projectSettings.compiler.ResourcesPatternInspection;
import org.intellij.trinkets.problemsView.inspections.projectSettings.libraries.LibraryInspection;
import org.intellij.trinkets.problemsView.problems.ProblemManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Project problem inspection registrator.
 *
 * @author Alexey Efimov
 */
public class ProjectProblemInspections implements ProjectComponent {
    private final ProblemInspection[] inspections;
    private final ProblemManager problemManager;

    public ProjectProblemInspections(Project project, ProblemManager problemManager) {
        this.problemManager = problemManager;
        // Register inspections
        this.inspections = new ProblemInspection[]{
                new ResourcesPatternInspection(project),
                new JikesSettingsInspection(project),
                new LibraryInspection(project)
        };
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "Trinkets.ApplicationProblemInspections";
    }

    public void initComponent() {
        for (ProblemInspection inspection : inspections) {
            problemManager.addProblemInspection(inspection);
        }
    }

    public void disposeComponent() {
        for (ProblemInspection inspection : inspections) {
            problemManager.removeProblemInspection(inspection);
        }
    }
}

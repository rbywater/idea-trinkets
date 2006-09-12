package org.intellij.trinkets.problemsView.inspections.applicationSettings;

import com.intellij.openapi.components.ApplicationComponent;
import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.intellij.trinkets.problemsView.inspections.applicationSettings.fileTypes.FileTypeInspection;
import org.intellij.trinkets.problemsView.inspections.applicationSettings.jdks.JdkInspection;
import org.intellij.trinkets.problemsView.problems.ProblemManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Application problem inspection registrator.
 *
 * @author Alexey Efimov
 */
public class ApplicationProblemInspections implements ApplicationComponent {
    private final ProblemInspection[] inspections;
    private final ProblemManager problemManager;

    public ApplicationProblemInspections(ProblemManager problemManager) {
        this.problemManager = problemManager;
        // Register inspections
        this.inspections = new ProblemInspection[]{
                new FileTypeInspection(),
                new JdkInspection()
        };
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

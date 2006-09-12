package org.intellij.trinkets.problemsView.impl;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.problemsView.ProblemView;
import org.intellij.trinkets.problemsView.ProblemViewManager;
import org.intellij.trinkets.problemsView.problems.ProblemManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

final class ProblemViewManagerImpl implements ProblemViewManager, ProjectComponent {
    private final Project project;
    private final ProblemManager problemManager;
    private ProblemView view;

    public ProblemViewManagerImpl(Project project, ProblemManager problemManager) {
        this.project = project;
        this.problemManager = problemManager;
    }

    @NotNull
    public ProblemView getView() {
        return view;
    }

    public void projectOpened() {
        this.view = new ProblemViewImpl(project, problemManager);
    }

    public void projectClosed() {
        if (view != null) {
            view.dispose();
        }
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "Trinkets.ProblemViewManager";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

}

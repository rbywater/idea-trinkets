package org.intellij.trinkets.problemsView.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import org.intellij.trinkets.problemsView.problems.ProblemManager;

/**
 * Puse/resume problems researching
 *
 * @author Alexey Efimov
 */
public class PauseAction extends ToggleAction {
    public boolean isSelected(AnActionEvent e) {
        ProblemManager problemManager = ApplicationManager.getApplication().getComponent(ProblemManager.class);
        return problemManager.isPaused();
    }

    public void setSelected(AnActionEvent e, boolean state) {
        ProblemManager problemManager = ApplicationManager.getApplication().getComponent(ProblemManager.class);
        problemManager.setPaused(state);
    }

    public void update(AnActionEvent e) {
        super.update(e);
        ProblemManager problemManager = ApplicationManager.getApplication().getComponent(ProblemManager.class);
        e.getPresentation().setEnabled(problemManager.isIdle());
    }
}

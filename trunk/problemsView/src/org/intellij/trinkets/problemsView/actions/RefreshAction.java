package org.intellij.trinkets.problemsView.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.intellij.trinkets.problemsView.problems.ProblemManager;

/**
 * Refresh problems view
 *
 * @author Alexey Efimov
 */
public class RefreshAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        ProblemManager problemManager = ApplicationManager.getApplication().getComponent(ProblemManager.class);
        if (problemManager.isIdle()) {
            problemManager.reinspect();
        }
    }

    public void update(AnActionEvent e) {
        super.update(e);
        ProblemManager problemManager = ApplicationManager.getApplication().getComponent(ProblemManager.class);
        e.getPresentation().setEnabled(problemManager.isIdle());
    }
}

package org.intellij.trinkets.research.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.intellij.trinkets.research.ResearchEngine;
import org.intellij.trinkets.research.ResearchManager;

/**
 * Closes reserach toolwindow.
 *
 * @author Alexey Efimov
 */
public class LearnMoreAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(DataKeys.PROJECT);
        ResearchManager researchManager = ResearchManager.getInstance(project);
        ResearchEngine researchEngine = researchManager.setSelectedEngine();
        if (researchEngine != null && researchEngine.getLastLocation() != null) {
            BrowserUtil.launchBrowser(researchEngine.getLastLocation());
        }
    }


    public void update(AnActionEvent e) {
        super.update(e);
        Project project = e.getData(DataKeys.PROJECT);
        ToolWindow window = e.getData(ResearchKeys.RESEARCH_TOOL_WINDOW_KEY);
        boolean enabled = project != null && window != null && window.isVisible();
        if (enabled) {
            ResearchManager researchManager = ResearchManager.getInstance(project);
            ResearchEngine researchEngine = researchManager.setSelectedEngine();
            enabled = researchEngine != null && researchEngine.getLastLocation() != null;
        }
        e.getPresentation().setEnabled(enabled);
    }
}

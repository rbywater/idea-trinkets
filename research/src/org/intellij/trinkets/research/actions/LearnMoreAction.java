package org.intellij.trinkets.research.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.ide.BrowserUtil;
import org.intellij.trinkets.research.ResearchManager;
import org.intellij.trinkets.research.ResearchEngine;

/**
 * Closes reserach toolwindow.
 *
 * @author Alexey Efimov
 */
public class LearnMoreAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        ResearchManager researchManager = ResearchManager.getInstance(project);
        ResearchEngine researchEngine = researchManager.setSelectedEngine();
        if (researchEngine != null && researchEngine.getLastLocation() != null) {
            BrowserUtil.launchBrowser(researchEngine.getLastLocation());
        }
    }


    public void update(AnActionEvent e) {
        super.update(e);
        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        ToolWindow window = (ToolWindow) e.getDataContext().getData(ResearchConstants.RESEARCH_TOOL_WINDOW);
        boolean enabled = project != null && window != null && window.isVisible();
        if (enabled) {
            ResearchManager researchManager = ResearchManager.getInstance(project);
            ResearchEngine researchEngine = researchManager.setSelectedEngine();
            enabled = researchEngine != null && researchEngine.getLastLocation() != null;
        }
        e.getPresentation().setEnabled(enabled);
    }
}

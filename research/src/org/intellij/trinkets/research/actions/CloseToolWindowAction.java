package org.intellij.trinkets.research.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.ToolWindow;

/**
 * Closes reserach toolwindow.
 *
 * @author Alexey Efimov
 */
public class CloseToolWindowAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        ToolWindow window = e.getData(ResearchKeys.RESEARCH_TOOL_WINDOW_KEY);
        if (window != null) {
            window.setAvailable(false, null);
        }
    }


    public void update(AnActionEvent e) {
        super.update(e);
        ToolWindow window = e.getData(ResearchKeys.RESEARCH_TOOL_WINDOW_KEY);
        e.getPresentation().setEnabled(window != null && window.isVisible());
    }
}

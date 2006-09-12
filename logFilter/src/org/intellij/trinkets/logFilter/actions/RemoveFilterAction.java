package org.intellij.trinkets.logFilter.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.logFilter.LogFilterConfigurable;

public class RemoveFilterAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        if (project != null) {
            LogFilterConfigurable configurable = project.getComponent(LogFilterConfigurable.class);
            configurable.removeSelected();
        }
    }

    public void update(AnActionEvent e) {
        super.update(e);
        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        if (project != null) {
            LogFilterConfigurable configurable = project.getComponent(LogFilterConfigurable.class);
            e.getPresentation().setEnabled(configurable.isVisible() && configurable.isItemSelected());
        } else {
            e.getPresentation().setEnabled(false);
        }
    }
}

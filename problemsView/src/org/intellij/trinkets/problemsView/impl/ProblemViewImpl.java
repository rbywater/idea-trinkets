package org.intellij.trinkets.problemsView.impl;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.Alarm;
import org.intellij.trinkets.problemsView.ProblemView;
import org.intellij.trinkets.problemsView.icons.ProblemViewIcons;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.intellij.trinkets.problemsView.problems.ProblemManager;
import org.intellij.trinkets.problemsView.problems.ProblemManagerListener;
import org.intellij.trinkets.problemsView.problems.ProblemType;
import org.intellij.trinkets.problemsView.ui.ProblemPanel;
import org.intellij.trinkets.problemsView.ui.TreeNodeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

final class ProblemViewImpl implements ProblemView {
    private static final String PROBLEMS_TITLE = "Problems";

    private final ProblemManagerListener problemManagerListener = new MyProblemManagerListener();
    private final Project project;
    private final ProblemManager problemManager;
    private final ToolWindow toolWindow;
    private final ProblemPanel problemsPanel;
    private final Alarm alarm = new Alarm();
    private final Runnable updateToolWindowTask = new Runnable() {
        public void run() {
            updateToolWindow();
            scheduleUpdater();
        }
    };

    public ProblemViewImpl(Project project, ProblemManager problemManager) {
        this.project = project;
        this.problemManager = problemManager;

        problemManager.addProblemManagerListener(problemManagerListener);

        JPanel panel = new JPanel(new BorderLayout());
        problemsPanel = new ProblemPanel(project);
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup toolbarGroup = (ActionGroup) actionManager.getAction("Trinkets.ProblemView.ToolBar");
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Trinkets.ProblemView.ToolBar", toolbarGroup, false);
        panel.add(actionToolbar.getComponent(), BorderLayout.WEST);
        panel.add(problemsPanel, BorderLayout.CENTER);
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindow = toolWindowManager.registerToolWindow(PROBLEMS_TITLE, panel, ToolWindowAnchor.BOTTOM);
        toolWindow.setAvailable(false, null);
        scheduleUpdater();
    }

    private void scheduleUpdater() {
        alarm.addRequest(updateToolWindowTask, 1000);
    }

    public Project getProject() {
        return project;
    }

    public void dispose() {
        alarm.cancelAllRequests();
        problemManager.removeProblemManagerListener(problemManagerListener);
    }

    @NotNull
    public Problem[] getSelectedProblems() {
        return problemsPanel.getSelectedProblems();
    }

    private final class MyProblemManagerListener implements ProblemManagerListener {
        public void inspectionStarted() {
            TreeNodeModel<Problem> model = problemsPanel.getModel();
            model.removeAll();
        }

        public void inspectionCompleted() {
            updateToolWindow();
        }

        public void inspectionFailed() {
            updateToolWindow();
        }

        public void problemAdded(Problem problem) {
            TreeNodeModel<Problem> model = problemsPanel.getModel();
            model.add(problem);
            updateToolWindow();
        }

        public void problemRemoved(Problem problem) {
            TreeNodeModel<Problem> model = problemsPanel.getModel();
            model.remove(problem);
            updateToolWindow();
        }

        public boolean canRunInspection() {
            return !toolWindow.isActive() && !toolWindow.isVisible();
        }
    }

    private void updateToolWindow() {
        if (problemManager.isIdle()) {
            ProblemType type = null;
            for (Problem problem : problemManager) {
                if (type == null) {
                    type = problem.getType();
                } else {
                    type = ProblemType.priority(type, problem.getType());
                }
            }
            toolWindow.setAvailable(type != null, null);
            if (type != null) {
                // Problems found
                if (problemManager.isPaused() || toolWindow.isActive() || toolWindow.isVisible()) {
                    // Paused icon
                    LayeredIcon icon = new LayeredIcon(2);
                    icon.setIcon(type.getToolWindowIcon(), 0);
                    icon.setIcon(ProblemViewIcons.TOOLWINDOW_PAUSE, 1);
                    toolWindow.setIcon(icon);
                } else {
                    toolWindow.setIcon(type.getToolWindowIcon());
                }
            } else {
                problemManager.setPaused(false);
            }
        } else {
            // Started icon
            LayeredIcon icon = new LayeredIcon(2);
            icon.setIcon(toolWindow.getIcon(), 0);
            icon.setIcon(ProblemViewIcons.TOOLWINDOW_RUNNED, 1);
            toolWindow.setIcon(icon);
        }
    }
}

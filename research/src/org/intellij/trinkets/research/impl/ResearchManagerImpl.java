package org.intellij.trinkets.research.impl;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Alarm;
import org.intellij.trinkets.research.*;
import org.intellij.trinkets.research.icons.ResearchIcons;
import org.intellij.trinkets.research.actions.ResearchConstants;
import org.intellij.trinkets.research.ui.ResearchResultComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;

final class ResearchManagerImpl extends ResearchManager implements ProjectComponent {
    private static final Logger LOGGER = Logger.getInstance("ResearchManager");
    @NonNls
    private static final String RESEARCH_TOOLWINDOW_ID = "Research";

    private final ResearchEngineManager engineManager;
    private final ToolWindowManager windowManager;
    private final ActionManager actionManager;
    private final ResearchResultComponent researchResultComponent = new ResearchResultComponent();
    private ToolWindow window;
    private Thread thread;
    private final Alarm alarm = new Alarm();

    public ResearchManagerImpl(ResearchEngineManager engineManager, ToolWindowManager windowManager, ActionManager actionManager) {
        this.engineManager = engineManager;
        this.windowManager = windowManager;
        this.actionManager = actionManager;
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "ResearchManager";
    }

    public void initComponent() {
        // Init search engines
        ResearchEngine[] engines = engineManager.getEngines();
        for (ResearchEngine engine : engines) {
            researchResultComponent.getModel().add(engine);
        }
    }

    public void disposeComponent() {
    }

    public void research(String word) {
        clearResults();
        if (!window.isAvailable()) {
            window.setAvailable(true, null);
        }
        if (!window.isVisible()) {
            window.show(null);
        }
        if (!window.isActive()) {
            window.activate(null);
        }
        try {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        } catch (Throwable e) {
            // Ignore
        }
        thread = new Thread(new ResearchTask(word));
        thread.setName(MessageFormat.format("ResearchDeamon-{0}", word));
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
        alarm.addRequest(new Runnable() {
            public void run() {
                if (thread != null && thread.isAlive()) {
                    researchResultComponent.updateUI();
                    alarm.addRequest(this, 500);
                }
            }
        }, 500);
    }

    public ResearchEngine setSelectedEngine() {
        return researchResultComponent.getSelectedEngine();
    }

    private void clearResults() {
        researchResultComponent.getModel().clear();
    }

    public void projectOpened() {
        // Init UI
        JPanel panel = new MyJPanel();
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        ActionGroup actionGroup = (ActionGroup) actionManager.getAction(ResearchConstants.RESEARCH_TOOL_WINDOW_TOOLBAR);
        if (actionGroup != null) {
            ActionToolbar actionToolbar = actionManager.createActionToolbar(ResearchConstants.RESEARCH_TOOL_WINDOW_ACTION_PLACE, actionGroup, true);
            toolbarPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
        }
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(researchResultComponent, BorderLayout.CENTER);
        window = windowManager.registerToolWindow(RESEARCH_TOOLWINDOW_ID, panel, ToolWindowAnchor.RIGHT);
        window.setAvailable(false, null);
        window.setIcon(ResearchIcons.TOOL_WINDOW);
    }

    public void projectClosed() {
        windowManager.unregisterToolWindow(RESEARCH_TOOLWINDOW_ID);
        researchResultComponent.dispose();
    }

    private final class MyJPanel extends JPanel implements DataProvider {
        public MyJPanel() {
            super(new BorderLayout());
        }

        @Nullable
        public Object getData(@NonNls String dataId) {
            if (ResearchConstants.RESEARCH_TOOL_WINDOW.equals(dataId)) {
                return window;
            }
            return null;
        }
    }

    private final class ResearchTask implements Runnable {
        private final String word;

        public ResearchTask(String word) {
            this.word = word;
        }

        public void run() {
            int found = 0;
            ResearchEngine[] engines = engineManager.getEngines();
            for (ResearchEngine engine : engines) {
                try {
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        public void run() {
                            window.setTitle(MessageFormat.format("{0}...", word));
                        }
                    }
                    );
                    ResearchResult researchResult = engine.research(word);
                    found += researchResult.getNodes().size();
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        public void run() {
                            researchResultComponent.updateStructure();
                        }
                    }
                    );
                } catch (IOException e) {
                    LOGGER.warn("Problems with research in engine " + engine.getName() + ": " + e);
                }
            }
            if (found > 0) {
                final int itemsFound = found;
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                        window.setTitle(MessageFormat.format("{0} - ({1} entries found)", word, itemsFound));
                    }
                }
                );
            }
        }
    }
}

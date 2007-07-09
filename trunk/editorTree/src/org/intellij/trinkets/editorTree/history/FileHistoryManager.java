package org.intellij.trinkets.editorTree.history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.intellij.trinkets.editorTree.history.tree.FileHistoryTreePanel;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * File history manager
 *
 * @author Alexey Efimov
 */
public final class FileHistoryManager implements ProjectComponent, JDOMExternalizable, FileEditorManagerListener, AnActionListener {
    private static final Logger LOGGER = Logger.getInstance("#EditorHistory");
    private final Set<FileHistory> histories = new HashSet<FileHistory>();
    private static final String TOOLWINDOW_TITLE = "Recent";
    private final Project project;

    private String lastSelectedUrl;
    private String lastActionId;
    private final FileHistoryTreePanel treePanel;

    public FileHistoryManager(Project project) {
        this.project = project;
        this.treePanel = new FileHistoryTreePanel(project);
    }

    public void projectOpened() {
        ActionManagerEx.getInstanceEx().addAnActionListener(this);
        FileEditorManagerEx.getInstanceEx(project).addFileEditorManagerListener(this);
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow window = toolWindowManager.registerToolWindow(TOOLWINDOW_TITLE, treePanel, ToolWindowAnchor.BOTTOM);
        window.setIcon(IconLoader.getIcon("/org/intellij/trinkets/editorTree/history/icons/history.png"));
        window.setTitle("History of Recently Opened Files");
        window.setAvailable(true, null);

    }

    public void projectClosed() {
        FileEditorManagerEx.getInstanceEx(project).removeFileEditorManagerListener(this);
        ActionManagerEx.getInstanceEx().removeAnActionListener(this);
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(TOOLWINDOW_TITLE);
        treePanel.dispose();
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "FileHistoryManager";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public void readExternal(Element element) throws InvalidDataException {
        List list = element.getChildren("h");
        for (Object o : list) {
            try {
                FileHistory history = new FileHistory((Element) o);
                addHistory(history);
            } catch (InvalidDataException e) {
                // Ignore
                LOGGER.warn(e.getMessage());
            }
        }
        removeOld();
        updateAll();
    }

    public void writeExternal(Element element) throws WriteExternalException {
        removeOld();
        for (FileHistory history : histories) {
            Element h = new Element("h");
            history.writeExternal(h);
            element.addContent(h);
        }
    }

    private void addHistory(FileHistory history) {
        Calendar day = FileHistoryUtil.day(history.getOpened());
        Iterator<FileHistory> iterator = histories.iterator();
        while (iterator.hasNext()) {
            FileHistory fileHistory = iterator.next();
            if (fileHistory.getUrl().equals(history.getUrl()) &&
                    day.equals(FileHistoryUtil.day(fileHistory.getOpened()))) {
                iterator.remove();
            }
        }
        histories.add(history);
    }

    private void removeOld() {
        // Scan to old entries and remove it
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Iterator<FileHistory> iterator = histories.iterator();
        while (iterator.hasNext()) {
            FileHistory history = iterator.next();
            if (cal.getTime().after(history.getOpened())) {
                iterator.remove();
            }
        }
    }

    private void updateAll() {
        treePanel.getModel().addAll(histories);
    }

    private void update(FileHistory fileHistory) {
        treePanel.getModel().changed(fileHistory);
    }

    private FileHistory findHistory(VirtualFile file) {
        for (FileHistory history : histories) {
            if (history.getUrl().equals(file.getUrl())) {
                return history;
            }
        }
        return null;
    }

    private void openHistory(VirtualFile file) {
        synchronized (this) {
            FileHistory history = findHistory(file);
            if (history != null) {
                history.setOpened(new Date());
                if (lastSelectedUrl != null) {
                    history.addFrom(lastSelectedUrl, lastActionId);
                    lastActionId = null;
                }
            } else {
                history = new FileHistory(new Date(), file.getUrl());
                addHistory(history);
            }
            updateAll();
        }
    }

    private void closeHistory(VirtualFile file) {
        synchronized (this) {
            FileHistory history = findHistory(file);
            if (history == null) {
                history = new FileHistory(new Date(), file.getUrl());
                addHistory(history);
                history.setClosed(new Date());
                updateAll();
            } else {
                history.setClosed(new Date());
                update(history);
            }
        }
    }

    public void fileOpened(FileEditorManager source, final VirtualFile file) {
        openHistory(file);
    }

    public void fileClosed(FileEditorManager source, VirtualFile file) {
        closeHistory(file);
    }

    public void selectionChanged(FileEditorManagerEvent event) {
        VirtualFile oldFile = event.getOldFile();
        lastSelectedUrl = oldFile != null ? oldFile.getUrl() : null;
    }

    public void beforeActionPerformed(AnAction anAction, DataContext dataContext) {
        synchronized (this) {
            lastActionId = ActionManagerEx.getInstanceEx().getId(anAction);
        }
    }

    public void afterActionPerformed(AnAction anAction, DataContext dataContext) {
    }

    public void beforeEditorTyping(char c, DataContext dataContext) {
    }
}

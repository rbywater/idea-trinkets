package org.intellij.trinkets.tabStack.impl;

import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.*;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.intellij.trinkets.tabStack.TabStackManager;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.*;

final class TabStackManagerImpl extends TabStackManager implements ProjectComponent, FileEditorManagerListener, EditorFactoryListener {
    private final Alarm rebuildAlarm = new Alarm();
    private final Alarm popAlarm = new Alarm();

    private final List<VirtualFile> stack = new ArrayList<VirtualFile>();
    private final List<VirtualFile> openedFiles = new ArrayList<VirtualFile>();

    private final FileEditorManager fileEditorManager;
    private final Runnable popAction = new PopStackAction();
    private final Runnable rebuildAction = new RebuildStackAction();

    private final VirtualFileListener vfsListerner = new MyVirtualFileAdapter();

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>();

    public TabStackManagerImpl(FileEditorManager fileEditorManager) {
        this.fileEditorManager = fileEditorManager;
    }

    public void projectOpened() {
        fileEditorManager.addFileEditorManagerListener(this);
    }

    public void projectClosed() {
        fileEditorManager.removeFileEditorManagerListener(this);
    }

    @NonNls
    public String getComponentName() {
        return "TabStackManager";
    }

    public void initComponent() {
        EditorFactory.getInstance().addEditorFactoryListener(this);
        VirtualFileManager.getInstance().addVirtualFileListener(vfsListerner);
    }

    public void disposeComponent() {
        popAlarm.cancelAllRequests();
        rebuildAlarm.cancelAllRequests();

        stack.clear();
        openedFiles.clear();

        VirtualFileManager.getInstance().removeVirtualFileListener(vfsListerner);
        EditorFactory.getInstance().removeEditorFactoryListener(this);
    }

    public void fileClosed(FileEditorManager source, VirtualFile file) {
        scheduleRebuild();
    }

    public void fileOpened(FileEditorManager source, VirtualFile file) {
        scheduleRebuild();
    }

    public void selectionChanged(FileEditorManagerEvent event) {
        scheduleRebuild();
    }

    public void editorCreated(EditorFactoryEvent event) {
        scheduleRebuild();
    }

    public void editorReleased(EditorFactoryEvent event) {
        schedulePop();
    }

    private void schedulePop() {
        synchronized (popAlarm) {
            popAlarm.cancelAllRequests();
            popAlarm.addRequest(popAction, 50);
        }
    }

    private void scheduleRebuild() {
        synchronized (rebuildAlarm) {
            rebuildAlarm.cancelAllRequests();
            rebuildAlarm.addRequest(rebuildAction, 150);
        }
    }

    private void popStack() {
        synchronized (popAlarm) {
            popAlarm.cancelAllRequests();
            UISettings uiSettings = UISettings.getInstance();
            int delta = uiSettings.EDITOR_TAB_LIMIT - fileEditorManager.getOpenFiles().length;
            VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();
            int oldStackSize = stack.size();
            while (stack.size() > 0 && delta-- > 0) {
                pop(false);
            }
            for (VirtualFile file : selectedFiles) {
                fileEditorManager.openFile(file, true);
            }
            if (oldStackSize != stack.size()) {
                fireChangeEvent();
            }
        }
    }

    private void rebuildStack() {
        synchronized (rebuildAlarm) {
            rebuildAlarm.cancelAllRequests();
            UISettings uiSettings = UISettings.getInstance();
            VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
            Set<VirtualFile> currentFiles = new HashSet<VirtualFile>(Arrays.asList(openFiles));
            Iterator<VirtualFile> files = openedFiles.iterator();
            int oldStackSize = stack.size();
            while (files.hasNext()) {
                VirtualFile file = files.next();
                if (currentFiles.contains(file)) {
                    currentFiles.remove(file);
                } else {
                    files.remove();
                    if (uiSettings.EDITOR_TAB_LIMIT <= openFiles.length) {
                        if (stack.contains(file)) {
                            stack.remove(file);
                        }
                        stack.add(file);
                    }
                }
            }
            if (oldStackSize != stack.size()) {
                fireChangeEvent();
            }
            for (VirtualFile file : currentFiles) {
                openedFiles.add(file);
            }
        }
    }

    public int getStackSize() {
        return stack.size();
    }

    @Nullable
    public VirtualFile pop(boolean focusEditor) {
        if (stack.size() > 0) {
            VirtualFile file = stack.remove(stack.size() - 1);
            fileEditorManager.openFile(file, focusEditor);
            return file;
        }
        return null;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    private final class PopStackAction implements Runnable {
        public void run() {
            rebuildStack();
            popStack();
        }
    }

    private final class RebuildStackAction implements Runnable {
        public void run() {
            rebuildStack();
        }
    }

    private final class MyVirtualFileAdapter extends VirtualFileAdapter {
        public void fileDeleted(VirtualFileEvent event) {
            stack.remove(event.getFile());
            fireChangeEvent();
        }
    }
}

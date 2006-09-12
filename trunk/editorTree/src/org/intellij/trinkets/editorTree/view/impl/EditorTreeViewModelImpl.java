package org.intellij.trinkets.editorTree.view.impl;

import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.trinkets.editorTree.view.EditorTreeViewModel;
import org.intellij.trinkets.editorTree.view.EditorTreeViewModelListener;

import java.util.HashSet;
import java.util.Set;

final class EditorTreeViewModelImpl implements EditorTreeViewModel {
    private final Set<EditorTreeViewModelListener> listeners = new HashSet<EditorTreeViewModelListener>(0);
    private final Set<VirtualFile> files = new HashSet<VirtualFile>(0);

    public void addFile(VirtualFile file) {
        if (files.add(file)) {
            fireFileAdded(file);
        }
    }

    public void removeFile(VirtualFile file) {
        if (files.remove(file)) {
            fireFileRemoved(file);
        }
    }

    public VirtualFile[] getFiles() {
        return files.toArray(VirtualFile.EMPTY_ARRAY);
    }

    public void addEditorsViewModelListener(EditorTreeViewModelListener listener) {
        listeners.add(listener);
    }

    public void removeEditorsViewModelListener(EditorTreeViewModelListener listener) {
        listeners.remove(listener);
    }

    public int getFilesCount() {
        return files.size();
    }

    private void fireFileAdded(VirtualFile file) {
        for (EditorTreeViewModelListener listener : listeners) {
            listener.fileAdded(file);
        }
    }

    private void fireFileRemoved(VirtualFile file) {
        for (EditorTreeViewModelListener listener : listeners) {
            listener.fileRemoved(file);
        }
    }
}

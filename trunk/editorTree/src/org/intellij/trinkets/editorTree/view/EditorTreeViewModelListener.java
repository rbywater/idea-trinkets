package org.intellij.trinkets.editorTree.view;

import com.intellij.openapi.vfs.VirtualFile;

public interface EditorTreeViewModelListener {
    void fileAdded(VirtualFile file);

    void fileRemoved(VirtualFile file);
}

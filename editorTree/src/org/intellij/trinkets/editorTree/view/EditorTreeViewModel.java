package org.intellij.trinkets.editorTree.view;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * Editor view model.
 *
 * @author Alexey Efimov
 */
public interface EditorTreeViewModel {
    void addFile(VirtualFile file);

    void removeFile(VirtualFile file);

    VirtualFile[] getFiles();

    void addEditorsViewModelListener(EditorTreeViewModelListener listener);

    void removeEditorsViewModelListener(EditorTreeViewModelListener listener);

    int getFilesCount();
}

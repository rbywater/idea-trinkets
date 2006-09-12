package org.intellij.trinkets.editorTree.view;

import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.MutableTreeNode;

/**
 * Tree node
 *
 * @author Alexey Efimov
 */
public interface EditorTreeNode extends MutableTreeNode, Comparable<EditorTreeNode> {
    VirtualFile getFile();

    void setFile(VirtualFile file);

    boolean append(VirtualFile file);

    boolean remove(VirtualFile file);

    void removeAll();
}

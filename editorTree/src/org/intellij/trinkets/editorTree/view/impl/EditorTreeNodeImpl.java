package org.intellij.trinkets.editorTree.view.impl;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.tree.TreeUtil;
import org.intellij.trinkets.editorTree.view.EditorTreeNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

final class EditorTreeNodeImpl extends DefaultMutableTreeNode implements EditorTreeNode {
    public EditorTreeNodeImpl() {
    }

    public EditorTreeNodeImpl(VirtualFile file) {
        super(file);
        setFile(file);
    }

    public final VirtualFile getFile() {
        return (VirtualFile)super.getUserObject();
    }

    public final void setFile(VirtualFile file) {
        super.setUserObject(file);
    }

    public boolean append(VirtualFile file) {
        VirtualFile rootFile = getFile();
        if (isAncestor(rootFile, file)) {
            if (rootFile.equals(file)) {
                return true;
            }
            // Append child of find this one
            VirtualFile nextChildFile = file;
            while (nextChildFile != null && !rootFile.equals(nextChildFile.getParent())) {
                nextChildFile = nextChildFile.getParent();
            }
            if (nextChildFile != null) {
                EditorTreeNode childNode = (EditorTreeNode)TreeUtil.findNodeWithObject(this, nextChildFile);
                if (childNode != null) {
                    // Child found append file recursive
                    return childNode.append(file);
                }

                // Child not found, create it
                EditorTreeNode node = new EditorTreeNodeImpl(nextChildFile);
                // Add node
                add(node);
                // Recursive append file
                return node.append(file);
            }
        }
        // Is not ancestor
        return false;
    }

    private static boolean isAncestor(@NotNull VirtualFile ancestor, @NotNull VirtualFile file) {
        // Fix troubles for files with diferend file systems
        VirtualFile parent = file.getParent();
        while (parent != null) {
            if (parent.equals(ancestor)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public boolean remove(VirtualFile file) {
        VirtualFile rootFile = getFile();
        if (rootFile.equals(file)) {
            removeFromParent();
            return true;
        }

        // Try to found child with file
        EditorTreeNode childNode = (EditorTreeNode)TreeUtil.findNodeWithObject(this, file);
        if (childNode != null) {
            childNode.removeFromParent();
            return true;
        }
        return false;
    }

    public void removeAll() {
        super.removeAllChildren();
    }

    public void remove(MutableTreeNode aChild) {
        super.remove(aChild);
        // Check that children count is not zero
        if (getChildCount() == 0 && getParent() != null) {
            // Remove recurcive parents with zero childs
            ((MutableTreeNode)getParent()).remove(this);
        }
    }

    public int compareTo(EditorTreeNode o) {
        if (o == this) {
            return 0;
        }
        if (o == null) {
            return 1;
        }
        VirtualFile otherFile = o.getFile();
        VirtualFile file = getFile();
        if (otherFile == file) {
            return 0;
        }
        if (otherFile == null) {
            return 1;
        }
        if (file == null) {
            return -1;
        }
        if (file.isDirectory() && otherFile.isDirectory() ||
            !file.isDirectory() && !otherFile.isDirectory()) {
            String pn = file.getPresentableName().toLowerCase();
            return pn.compareTo(otherFile.getPresentableName().toLowerCase());
        }
        if (file.isDirectory()) {
            return -1;
        }
        return 1;
    }
}

package org.intellij.trinkets.editorTree.view.impl;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import org.intellij.trinkets.editorTree.view.EditorTreeNode;

import javax.swing.*;

final class EditorTreeCellRenderer extends ColoredTreeCellRenderer {
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        VirtualFile file = ((EditorTreeNode)value).getFile();
        if (file != null) {
            append(file.getPresentableName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
            FileType fileType = file.getFileType();
            Icon fileTypeIcon = fileType != null ? fileType.getIcon() : null;
            if (file.isDirectory()) {
                if (StdFileTypes.ARCHIVE.equals(fileType)) {
                    setIcon(fileTypeIcon);
                } else {
                    setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
                }
            } else {
                if (fileTypeIcon != null) {
                    setIcon(fileTypeIcon);
                } else  {
                    Icon fileIcon = file.getIcon();
                    if (fileIcon != null) {
                        setIcon(fileIcon);
                    } else {
                        setIcon(StdFileTypes.UNKNOWN.getIcon());
                    }
                }
            }

            setToolTipText(file.getPath());
        } else {
            setIcon(null);
            setToolTipText(null);
        }
    }
}

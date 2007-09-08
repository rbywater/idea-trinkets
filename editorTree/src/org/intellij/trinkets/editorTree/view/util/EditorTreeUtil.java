package org.intellij.trinkets.editorTree.view.util;

import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.trinkets.editorTree.view.EditorTreeNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Arrays;

/**
 * Utility class for {@link org.intellij.trinkets.editorTree.view.EditorTreeView}.
 *
 * @author Alexey Efimov
 */
public final class EditorTreeUtil {
    private EditorTreeUtil() {
    }

    /**
     * Return all leaf files for node.
     * @param node Node
     * @return Files array
     */
    @NotNull
    public static VirtualFile[] getFiles(@NotNull EditorTreeNode node) {
        List<VirtualFile> files = new ArrayList<VirtualFile>(1);
        if (node.isLeaf()) {
            files.add(node.getFile());
        } else {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                EditorTreeNode child = (EditorTreeNode)enumeration.nextElement();
                VirtualFile[] inheritedFiles = getFiles(child);
                files.addAll(Arrays.asList(inheritedFiles));
            }
        }
        return files.toArray(VirtualFile.EMPTY_ARRAY);
    }
}

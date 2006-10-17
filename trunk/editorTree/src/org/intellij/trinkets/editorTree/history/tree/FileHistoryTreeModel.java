package org.intellij.trinkets.editorTree.history.tree;

import org.intellij.trinkets.editorTree.history.FileHistory;

import javax.swing.tree.TreeModel;

/**
 * File history tree model.
 *
 * @author Alexey Efimov
 */
public interface FileHistoryTreeModel extends TreeModel {
    void addAll(Iterable<FileHistory> histories);

    void add(FileHistory history);

    void remove(FileHistory history);

    void changed(FileHistory history);
}

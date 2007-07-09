package org.intellij.trinkets.editorTree.history.tree;

import com.intellij.openapi.project.Project;
import org.intellij.trinkets.editorTree.history.FileHistory;
import org.intellij.trinkets.editorTree.history.tree.elements.ProjectTreeElement;
import org.intellij.trinkets.editorTree.history.tree.elements.TreeElement;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Default model for history tree
 *
 * @author Alexey Efimov
 */
public final class DefaultFileHistoryTreeModel implements FileHistoryTreeModel {
    private final Set<TreeModelListener> listeners = new HashSet<TreeModelListener>(0);
    private final ProjectTreeElement root;


    public DefaultFileHistoryTreeModel(Project project) {
        root = new ProjectTreeElement(project);
    }

    private void fireAdded(TreePath[] added) {
        HashSet<TreeModelListener> listenersList = new HashSet<TreeModelListener>(listeners);
        for (TreePath treePath : added) {
            TreeModelEvent event = new TreeModelEvent(this, treePath);
            for (TreeModelListener listener : listenersList) {
                listener.treeNodesInserted(event);
            }
        }
    }

    private void fireRemoved(TreePath[] removed) {
        HashSet<TreeModelListener> listenersList = new HashSet<TreeModelListener>(listeners);
        for (TreePath treePath : removed) {
            TreeModelEvent event = new TreeModelEvent(this, treePath);
            for (TreeModelListener listener : listenersList) {
                listener.treeNodesRemoved(event);
            }
        }
    }

    private void fireChanged(TreePath changed) {
        HashSet<TreeModelListener> listenersList = new HashSet<TreeModelListener>(listeners);
        TreeModelEvent event = new TreeModelEvent(this, changed);
        for (TreeModelListener listener : listenersList) {
            listener.treeNodesChanged(event);
        }
    }

    private void fireStructureChanged() {
        HashSet<TreeModelListener> listenersList = new HashSet<TreeModelListener>(listeners);
        TreeModelEvent event = new TreeModelEvent(this, new TreePath(root));
        for (TreeModelListener listener : listenersList) {
            listener.treeStructureChanged(event);
        }
    }

    public void addAll(Iterable<FileHistory> histories) {
        fireAdded(root.addHistory(histories));
    }

    public void add(FileHistory history) {
        fireAdded(root.addHistory(Collections.singleton(history)));
    }

    public void remove(FileHistory history) {
        fireRemoved(root.removeHistory(Collections.singleton(history)));
    }

    public void changed(FileHistory history) {
        TreePath treePath = root.changed(history);
        if (treePath != null) {
            fireChanged(treePath);
        }
    }

    public void groupByDirectory(boolean enabled) {
        root.setDirectoryGroupingEnabled(enabled);
        fireStructureChanged();
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        TreeElement element = (TreeElement) parent;
        return element.getChild(index);
    }

    public int getChildCount(Object parent) {
        TreeElement element = (TreeElement) parent;
        return element.getChildCount();
    }

    public boolean isLeaf(Object node) {
        TreeElement element = (TreeElement) node;
        return element.isLeaf();
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
        TreeElement element = (TreeElement) parent;
        TreeElement childElement = (TreeElement) child;
        return element.getIndexOfChild(childElement);
    }

    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }
}

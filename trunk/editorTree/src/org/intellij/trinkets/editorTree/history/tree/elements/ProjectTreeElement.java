package org.intellij.trinkets.editorTree.history.tree.elements;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import org.intellij.trinkets.editorTree.history.FileHistory;
import org.intellij.trinkets.editorTree.history.FileHistoryUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.*;

/**
 * Root tree element for history
 *
 * @author Alexey Efimov
 */
public final class ProjectTreeElement extends AbstractTreeElement<Project> {
    private static final TreePath[] EMPTY_TREE_PATH_ARRAY = new TreePath[]{};
    private boolean directoryGroupingEnabled;

    public ProjectTreeElement(Project value) {
        super(value, false);
    }

    public String getSpeedSearchText() {
        return getValue().getName();
    }

    public void render(ColoredTreeCellRenderer renderer, JTree tree, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Project project = getValue();
        renderer.setIcon(Icons.PROJECT_ICON);
        renderer.append(project.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
    }

    @NotNull
    public TreePath[] addHistory(@NotNull Iterable<FileHistory> histories) {
        List<TreePath> added = new ArrayList<TreePath>(0);
        for (FileHistory history : histories) {
            if (history.getClosed() != null) {
                Date day = FileHistoryUtil.day(history.getClosed()).getTime();
                DateTreeElement child = (DateTreeElement) findChildByValue(day);
                if (child == null) {
                    child = new DateTreeElement(day);
                    addChild(child);
                }
                FileHistoryTreeElement element = (FileHistoryTreeElement) child.findChildByValue(history);
                if (element == null) {
                    element = new FileHistoryTreeElement(history);
                    child.addChild(element);
                    added.add(new TreePath(new Object[]{this, child, element}));
                }
                // Sort
                child.sortChildren(FileHistoryTreeElement.COMPARATOR);
            }
        }

        // Sorting
        sortChildren(DateTreeElement.COMPARATOR);
        return added.toArray(EMPTY_TREE_PATH_ARRAY);
    }

    @NotNull
    public TreePath[] removeHistory(@NotNull Iterable<FileHistory> histories) {
        List<TreePath> removed = new ArrayList<TreePath>(0);
        for (FileHistory history : histories) {
            if (history.getClosed() != null) {
                Date day = FileHistoryUtil.day(history.getClosed()).getTime();
                DateTreeElement child = (DateTreeElement) findChildByValue(day);
                if (child != null) {
                    FileHistoryTreeElement element = (FileHistoryTreeElement) child.findChildByValue(history);
                    if (element != null) {
                        removed.add(new TreePath(new Object[]{this, child, element}));
                        child.removeChild(element);
                        if (child.getChildCount() == 0) {
                            // Last item removed
                            removeChild(child);
                        }
                    }
                }
            }
        }
        return removed.toArray(EMPTY_TREE_PATH_ARRAY);
    }

    public TreePath changed(FileHistory history) {
        Date day = FileHistoryUtil.day(history.getClosed() != null ? history.getClosed() : history.getOpened()).getTime();
        DateTreeElement child = (DateTreeElement) findChildByValue(day);
        if (child != null) {
            FileHistoryTreeElement element = (FileHistoryTreeElement) child.findChildByValue(history);
            if (element != null) {
                // Sort
                child.sortChildren(FileHistoryTreeElement.COMPARATOR);
                return new TreePath(new Object[]{this, child, element});
            }
        }
        TreePath[] treePaths = addHistory(Collections.singleton(history));
        return treePaths.length > 0 ? treePaths[0] : null;
    }

    private Set<FileHistory> collectHistories(TreeElement element, Set<FileHistory> histories) {
        if (element instanceof FileHistoryTreeElement) {
            FileHistoryTreeElement fileHistoryTreeElement = (FileHistoryTreeElement) element;
            histories.add(fileHistoryTreeElement.getValue());
        }
        int count = element.getChildCount();
        for (int i = 0; i < count; i++) {
            collectHistories(element.getChild(i), histories);
        }
        return histories;
    }

    public TreeElement createTreeElement(FileHistory history) {
        if (directoryGroupingEnabled) {
            VirtualFile virtualFile = FileHistoryUtil.resolve(history);

        } else {

        }
        return null;
    }

    public void setDirectoryGroupingEnabled(boolean enabled) {
        Set<FileHistory> histories = collectHistories(this, new HashSet<FileHistory>());
        // Remove all
        clearChildren();
        directoryGroupingEnabled = enabled;
        // Add again
        addHistory(histories);
    }
}

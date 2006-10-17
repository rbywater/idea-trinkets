package org.intellij.trinkets.editorTree.history.tree.elements;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract tree element
 *
 * @author Alexey Efimov
 */
public abstract class AbstractTreeElement<T> implements TreeElement<T> {
    private final List<TreeElement> children;
    private final T value;

    protected AbstractTreeElement(T value, boolean leaf) {
        this.value = value;
        this.children = leaf ? null : new ArrayList<TreeElement>();
    }

    @NotNull
    public final T getValue() {
        return value;
    }

    public final boolean isLeaf() {
        return children == null;
    }

    public final int getChildCount() {
        return children != null ? children.size() : 0;
    }

    public final TreeElement getChild(int index) {
        return children != null ? children.get(index) : null;
    }

    public final int getIndexOfChild(@NotNull TreeElement child) {
        return children != null ? children.indexOf(child) : -1;
    }

    public final void sortChildren(@NotNull Comparator comparator) {
        if (children != null) {
            Collections.sort(children, new TreeElementComparator(comparator));
        }
    }

    public final void addChild(TreeElement child) {
        if (children != null) {
            children.add(child);
        }
    }

    public void removeChild(TreeElement child) {
        if (children != null) {
            children.remove(child);
        }
    }

    public TreeElement findChildByValue(@NotNull Object value) {
        for (TreeElement child : children) {
            if (value.equals(child.getValue())) {
                return child;
            }
        }
        return null;
    }

    private static class TreeElementComparator implements Comparator<TreeElement> {
        private final Comparator comparator;

        public TreeElementComparator(Comparator comparator) {
            this.comparator = comparator;
        }

        @SuppressWarnings({"unchecked"})
        public int compare(TreeElement o1, TreeElement o2) {
            return comparator.compare(o1.getValue(), o2.getValue());
        }
    }
}

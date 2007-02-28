package org.trinkets.win32.shell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * IContextMenu item.
 *
 * @author Alexey Efimov
 */
public final class IContextMenuItem implements Iterable<IContextMenuItem> {
    private final int id;
    private final String text;
    private final List<IContextMenuItem> children = new ArrayList<IContextMenuItem>(0);
    private IContextMenuItem parrent;

    public IContextMenuItem(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public IContextMenuItem getParrent() {
        return parrent;
    }

    public void addChild(IContextMenuItem item) {
        if (children.add(item)) {
            item.parrent = this;
        }
    }

    public void removeChild(IContextMenuItem item) {
        if (children.remove(item)) {
            item.parrent = null;
        }
    }

    public int getChildrenSize() {
        return children.size();
    }

    public IContextMenuItem getChild(int index) {
        return children.get(index);
    }

    public Iterator<IContextMenuItem> iterator() {
        return children.iterator();
    }
}

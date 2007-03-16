package org.trinkets.win32.shell.impl;

import org.trinkets.win32.shell.IContextMenu;
import org.trinkets.win32.shell.IContextMenuItem;

import java.awt.*;

/**
 * IContextMenu port to Java.
 * See MSDN link <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/shellcc/platform/shell/reference/ifaces/icontextmenu/icontextmenu.asp">IContextMenu Interface</a>.
 *
 * @author Alexey Efimov
 */
public final class IContextMenuImpl implements IContextMenu {
    static {
        System.loadLibrary("IContextMenu_JNI");
    }

    private static final int[] EMPTY_INT_ARRAY = {};
    private final String[] filePaths;

    public IContextMenuImpl(String[] filePaths) {
        this.filePaths = filePaths;
    }

    public IContextMenuItem[] getItems(Component owner, IContextMenuItem[] path) {
        while (owner != null && owner.isLightweight()) {
            owner = owner.getParent();
        }
        int[] menuPath = path != null ? new int[path.length] : EMPTY_INT_ARRAY;
        for (int i = 0; i < path.length; i++) {
            menuPath[i] = path[i].getId();
        }
        IContextMenuItem[] items = getItems0(owner, filePaths, menuPath);
        if (items != null) {
            if (path != null && path.length > 0) {
                IContextMenuItem parent = path[path.length - 1];
                for (IContextMenuItem item : items) {
                    item.setParent(parent);
                }
            }
            return items;
        }
        return IContextMenuItem.EMPTY_ARRAY;
    }

    public void invokeItem(IContextMenuItem item) {
        IContextMenuItem[] path = item.getParentPath();
        int[] menuPath = new int[path.length];
        for (int i = 0; i < path.length; i++) {
            menuPath[i] = path[i].getId();
        }
        invokeItem0(filePaths, menuPath, item.getId());
    }

    private native IContextMenuItem[] getItems0(Component owner, String[] filePath, int[] menuPath);

    private native void invokeItem0(String[] filePath, int[] menuPath, int item);
}

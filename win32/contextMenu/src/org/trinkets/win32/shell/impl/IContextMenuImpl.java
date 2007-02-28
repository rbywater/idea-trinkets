package org.trinkets.win32.shell.impl;

import org.trinkets.win32.shell.IContextMenu;
import org.trinkets.win32.shell.IContextMenuItem;

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

    private static final IContextMenuItem[] EMPTY_I_CONTEXT_MENU_ITEM_ARRAY = new IContextMenuItem[]{};

    private final String filePath;

    public IContextMenuImpl(String filePath) {
        this.filePath = filePath;
    }

    public IContextMenuItem[] getItems() {
        IContextMenuItem[] items = getItems0(filePath);
        return items != null ? items : EMPTY_I_CONTEXT_MENU_ITEM_ARRAY;
    }

    public void invokeItem(IContextMenuItem item) {
        invokeItem0(filePath, item.getId());
    }

    private native IContextMenuItem[] getItems0(String filePath);

    private native void invokeItem0(String filePath, int item);
}

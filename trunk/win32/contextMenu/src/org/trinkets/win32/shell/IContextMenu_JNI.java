package org.trinkets.win32.shell;

import org.jetbrains.annotations.NotNull;
import org.trinkets.util.jni.annotations.JNIBundle;
import org.trinkets.util.jni.annotations.JNILibrary;

import java.awt.*;

/**
 * IContextMenu port to Java. See MSDN link <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/shellcc/platform/shell/reference/ifaces/icontextmenu/icontextmenu.asp">IContextMenu
 * Interface</a>.
 *
 * @author Alexey Efimov
 */
@JNILibrary("IContextMenu_JNI")
@JNIBundle("IContextMenu_JNI.jar")
public final class IContextMenu_JNI implements IContextMenu {
    private final String[] filePaths;

    public IContextMenu_JNI(String[] filePaths) {
        this.filePaths = filePaths;
    }

    public IContextMenuItem[] getItems(Component owner, @NotNull IContextMenuItem[] path) {
        while (owner != null && owner.isLightweight()) {
            owner = owner.getParent();
        }
        int[] menuPath = new int[path.length];
        for (int i = 0; i < path.length; i++) {
            menuPath[i] = path[i].getId();
        }
        IContextMenuItem[] items = getItems0(owner, filePaths, menuPath);
        if (items != null) {
            if (path.length > 0) {
                IContextMenuItem parent = path[path.length - 1];
                for (IContextMenuItem item : items) {
                    item.setParent(parent);
                }
            }
            return items;
        }
        return IContextMenuItem.EMPTY_ARRAY;
    }

    public void invokeItem(Component owner, @NotNull IContextMenuItem item) {
        IContextMenuItem[] path = item.getParentPath();
        int[] menuPath = new int[path.length];
        for (int i = 0; i < path.length; i++) {
            menuPath[i] = path[i].getId();
        }
        // If we pass owner frame it can be hang up on Explore function
        // to prevent this we must use null as HWND
        invokeItem0(null, filePaths, menuPath, item.getId());
    }

    private native IContextMenuItem[] getItems0(Component owner, String[] filePath, int[] menuPath);

    private native void invokeItem0(Component owner, String[] filePath, int[] menuPath, int item);
}

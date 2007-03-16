package org.trinkets.win32.shell;

import java.awt.*;

/**
 * Wrapper to IContextMenu.
 * See MSDN link <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/shellcc/platform/shell/reference/ifaces/icontextmenu/icontextmenu.asp">IContextMenu Interface</a>.
 *
 * @author Alexey Efimov
 */
public interface IContextMenu {
    IContextMenuItem[] getItems(Component owner, IContextMenuItem[] path);

    void invokeItem(Component owner, IContextMenuItem item);
}

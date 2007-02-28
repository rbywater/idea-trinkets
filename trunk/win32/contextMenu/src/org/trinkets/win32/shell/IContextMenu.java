package org.trinkets.win32.shell;

/**
 * Wrapper to IContextMenu.
 * See MSDN link <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/shellcc/platform/shell/reference/ifaces/icontextmenu/icontextmenu.asp">IContextMenu Interface</a>.
 *
 * @author Alexey Efimov
 */
public interface IContextMenu {
    IContextMenuItem[] getItems();

    void invokeItem(IContextMenuItem item);
}

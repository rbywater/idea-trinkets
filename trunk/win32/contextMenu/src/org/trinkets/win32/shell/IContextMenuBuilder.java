package org.trinkets.win32.shell;

/**
 * IContextMenu builder.
 *
 * @author Alexey Efimov
 */
public interface IContextMenuBuilder {
    IContextMenu createContextMenu(String[] filePaths);
}

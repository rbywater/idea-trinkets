package org.trinkets.win32.shell.impl;

import org.trinkets.util.LibraryLoader;
import org.trinkets.win32.shell.IContextMenu;
import org.trinkets.win32.shell.IContextMenuBuilder;

/**
 * IContextMenu builder.
 *
 * @author Alexey Efimov
 */
public final class IContextMenuBuilderImpl implements IContextMenuBuilder {
    public IContextMenuBuilderImpl(LibraryLoader libraryLoader) {
        libraryLoader.extractLibrary("/org/trinkets/win32/shell/impl/IContextMenu_JNI.zip", "IContextMenu_JNI");
    }

    public IContextMenu createContextMenu(String[] filePaths) {
        return new IContextMenuImpl(filePaths);
    }
}

package org.intellij.trinkets.win32.contextMenu;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.jniwrapper.DefaultLibraryLoader;
import org.trinkets.util.LibraryLoader;

import java.io.File;

/**
 * Plugin point.
 *
 * @author Alexey Efimov
 */
public final class Plugin {
    private static final File LIBRARIES_HOME = new File(PathManager.getSystemPath(), "plugins.libraries");
    public static final Logger LOGGER = Logger.getInstance("#Win32.ContextMenu");
    public static final LibraryLoader LOADER = new LibraryLoader(LIBRARIES_HOME, DefaultLibraryLoader.getLibraryExtension());
}

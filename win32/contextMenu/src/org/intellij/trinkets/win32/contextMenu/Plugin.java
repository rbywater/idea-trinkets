package org.intellij.trinkets.win32.contextMenu;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;

/**
 * Plugin point.
 *
 * @author Alexey Efimov
 */
public final class Plugin {
    public static final File JNI_CACHE_DIR = new File(PathManager.getSystemPath(), "plugins.libraries");
    public static final Logger LOGGER = Logger.getInstance("#Win32.ContextMenu");
}

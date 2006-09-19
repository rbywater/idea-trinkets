package org.intellij.trinkets.pluginPacker.util;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.module.PluginModuleType;

/**
 * Utility for plugin module
 *
 * @author Alexey Efimov
 */
public final class PluginModuleUtil {
    private PluginModuleUtil() {
    }

    public static boolean isPluginModule(@NotNull Module module) {
        return module.getModuleType() instanceof PluginModuleType;
    }
}

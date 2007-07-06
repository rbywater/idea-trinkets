package org.intellij.trinkets.pluginPacker.util;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * Bundle for Plugin Packer plugin.
 *
 * @author Alexey Efimov
 */
public final class PluginPackerBundle {
    @NonNls
    private static final String BUNDLE_NAME = "org.intellij.trinkets.pluginPacker.util.PluginPackerBundle";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private PluginPackerBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME)String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }
}

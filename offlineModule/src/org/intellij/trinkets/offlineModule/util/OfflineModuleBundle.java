package org.intellij.trinkets.offlineModule.util;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * Plugin bundle.
 *
 * @author Alexey Efimov
 */
public final class OfflineModuleBundle {
    @NonNls
    private static final String BUNDLE_NAME = "org.intellij.trinkets.offlineModule.util.OfflineModuleBundle";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private OfflineModuleBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME)String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }
}

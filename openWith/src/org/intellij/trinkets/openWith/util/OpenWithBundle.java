package org.intellij.trinkets.openWith.util;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * Bundle for messages.
 *
 * @author Alexey Efimov
 */
public final class OpenWithBundle {
    @NonNls
    private static final String BUNDLE_NAME = "org.intellij.trinkets.openWith.util.OpenWithBundle";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private OpenWithBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME)String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }
}

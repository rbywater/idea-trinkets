package org.intellij.trinkets.native2ascii.util;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.PropertyKey;
import org.jetbrains.annotations.NonNls;

import java.util.ResourceBundle;

public final class Native2AsciiBundle {
    @NonNls
    private static final String BUNDLE_NAME = "org.intellij.trinkets.native2ascii.util.Native2AsciiBundle";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Native2AsciiBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }
}

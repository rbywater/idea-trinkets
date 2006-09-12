package org.intellij.trinkets.editorTree.util;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.PropertyKey;
import org.jetbrains.annotations.NonNls;

import java.util.ResourceBundle;

public final class EditorTreeBundle {
    @NonNls
    private static final String BUNDLE_NAME = "org.intellij.trinkets.editorTree.util.EditorTreeBundle";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private EditorTreeBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }
}

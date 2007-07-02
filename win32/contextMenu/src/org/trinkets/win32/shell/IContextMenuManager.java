package org.trinkets.win32.shell;

import org.jetbrains.annotations.NotNull;
import org.trinkets.util.jni.JNIBundleLoader;

import java.io.File;

/**
 * Manager for {@link org.trinkets.win32.shell.IContextMenu}.
 *
 * @author Alexey Efimov
 */
public final class IContextMenuManager {
    private final JNIBundleLoader jni;

    public IContextMenuManager() {
        this(new File(System.getProperty("user.dir")));
    }

    public IContextMenuManager(File jniCacheDir) {
        this.jni = new JNIBundleLoader(jniCacheDir);
        // Preload dlls (just test menu on folder)
        getMenu(System.getProperty("user.dir"));
    }

    @SuppressWarnings({"RedundantArrayCreation"})
    @NotNull
    public IContextMenu getMenu(@NotNull String... filePaths) {
        try {
            return jni.newJNI(IContextMenu_JNI.class, new Object[]{filePaths});
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public IContextMenu getMenu(@NotNull File... files) {
        return getMenu(toPaths(files));
    }

    @NotNull
    private static String[] toPaths(@NotNull File... files) {
        String[] filePaths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            filePaths[i] = file.getAbsolutePath();
        }
        return filePaths;
    }
}

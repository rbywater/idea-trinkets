package org.intellij.trinkets.problemsView.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;

/**
 * File utility
 *
 * @author Alexey Efimov
 */
public final class FindFileUtil {
    private FindFileUtil() {
    }


    public static boolean isValidUrl(String url) {
        try {
            File ioFile = new File(url);
            if (ioFile.exists()) {
                return true;
            }
            VirtualFileManager vfManager = VirtualFileManager.getInstance();
            VirtualFile file = vfManager.findFileByUrl(url);
            return file != null;
        } catch (Throwable e) {
            // Ignore exception but add problem
            return false;
        }
    }
}

package org.intellij.trinkets.native2ascii.util;

import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * Utility class for compiler
 *
 * @author Alexey Efimov
 */
public final class CompilerFileUtil {
    private static final char FILE_SEPARATOR = '/';
    private static final char PACKAGE_SEPARATOR = '.';

    /**
     * Write byte buffer into file.
     *
     * @param outFile    File
     * @param byteBuffer Buffer
     * @throws java.io.IOException
     */
    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
    public static void writeFile(@NotNull File outFile, @NotNull ByteBuffer byteBuffer) throws IOException {
        WritableByteChannel wbc = Channels.newChannel(new FileOutputStream(outFile));
        try {
            wbc.write(byteBuffer);
        } finally {
            wbc.close();
        }
    }

    @NotNull
    public static String getOutputFilePath(
        @NotNull ProjectFileIndex pfi,
        @NotNull VirtualFile src,
        @NotNull VirtualFile srcRoot,
        @NotNull String outRoot
    ) {
        StringBuffer targetPath = new StringBuffer(outRoot);
        String relativePath = VfsUtil.getRelativePath(src, srcRoot, FILE_SEPARATOR);
        String packageName = pfi.getPackageNameByDirectory(srcRoot);
        if (packageName != null && packageName.length() > 0) {
            targetPath.append(FILE_SEPARATOR);
            targetPath.append(packageName.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR));
        }
        targetPath.append(FILE_SEPARATOR);
        targetPath.append(relativePath);
        return targetPath.toString();
    }
}

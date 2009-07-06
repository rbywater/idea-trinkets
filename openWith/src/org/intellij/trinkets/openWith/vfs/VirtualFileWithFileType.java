package org.intellij.trinkets.openWith.vfs;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Virtual file type wrapper.
 *
 * @author Alexey Efimov
 */
public final class VirtualFileWithFileType extends VirtualFile {
    private final VirtualFile delegate;
    private final FileType fileType;

    public VirtualFileWithFileType(@NotNull VirtualFile file, @NotNull FileType fileType) {
        this.delegate = file;
        this.fileType = fileType;
    }

    @Override
    @NotNull
    public String getName() {
        return delegate.getName();
    }

    @Override
    @NotNull
    public VirtualFileSystem getFileSystem() {
        return delegate.getFileSystem();
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public boolean isWritable() {
        return delegate.isWritable();
    }

    @Override
    public boolean isDirectory() {
        return delegate.isDirectory();
    }

    @Override
    public boolean isValid() {
        return delegate.isValid();
    }

    @Override
    public VirtualFile getParent() {
        return delegate.getParent();
    }

    @Override
    public VirtualFile[] getChildren() {
        return delegate.getChildren();
    }


    @NotNull
    @Override
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        return delegate.getOutputStream(requestor, newModificationStamp, newTimeStamp);
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray() throws IOException {
        return delegate.contentsToByteArray();
    }

    @Override
    public long getTimeStamp() {
        return delegate.getTimeStamp();
    }

    @Override
    public long getLength() {
        return delegate.getLength();
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
        delegate.refresh(asynchronous, recursive, postRunnable);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return fileType;
    }

    @Override
    @NotNull
    public String getUrl() {
        return delegate.getUrl();
    }

    @Override
    public String getExtension() {
        return fileType.getDefaultExtension();
    }

    @Override
    @NotNull
    public String getNameWithoutExtension() {
        return delegate.getNameWithoutExtension();
    }

    @Override
    public void rename(Object requestor, @NotNull String newName) throws IOException {
        delegate.rename(requestor, newName);
    }

    @Override
    public VirtualFile findChild(@NotNull String name) {
        return delegate.findChild(name);
    }

    @Override
    public Icon getIcon() {
        return fileType.getIcon();
    }

    @Override
    public VirtualFile findFileByRelativePath(@NotNull String relPath) {
        return delegate.findFileByRelativePath(relPath);
    }

    @Override
    public VirtualFile createChildDirectory(Object requestor, String name) throws IOException {
        return delegate.createChildDirectory(requestor, name);
    }

    @Override
    public VirtualFile createChildData(Object requestor, @NotNull String name) throws IOException {
        return delegate.createChildData(requestor, name);
    }

    @Override
    public void delete(Object requestor) throws IOException {
        delegate.delete(requestor);
    }

    @Override
    public void move(Object requestor, VirtualFile newParent) throws IOException {
        delegate.move(requestor, newParent);
    }

    @Override
    public Charset getCharset() {
        return delegate.getCharset();
    }

    @Override
    public void setBinaryContent(byte[] content, long newModificationStamp, long newTimeStamp) throws IOException {
        delegate.setBinaryContent(content, newModificationStamp, newTimeStamp);
    }

    @Override
    public long getModificationStamp() {
        return delegate.getModificationStamp();
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive) {
        delegate.refresh(asynchronous, recursive);
    }

    @Override
    public <T> T getUserData(Key<T> key) {
        return delegate.getUserData(key);
    }

    @Override
    public <T> void putUserData(Key<T> key, T value) {
        delegate.putUserData(key, value);
    }

    @Override
    public String getPresentableName() {
        return delegate.getPresentableName();
    }

    @Override
    public long getModificationCount() {
        return delegate.getModificationCount();
    }

    @Override
    public void setCharset(Charset charset) {
        delegate.setCharset(charset);
    }

    @Override
    public byte[] getBOM() {
        return delegate.getBOM();
    }

    @Override
    public void setBOM(byte[] BOM) {
        delegate.setBOM(BOM);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof VirtualFileWithFileType) {
            VirtualFileWithFileType that = (VirtualFileWithFileType) o;
            return delegate.equals(that.delegate) && fileType.equals(that.fileType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + fileType.hashCode();
        return result;
    }
}

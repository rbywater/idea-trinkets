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

    @NotNull
    public String getName() {
        return delegate.getName();
    }

    @NotNull
    public VirtualFileSystem getFileSystem() {
        return delegate.getFileSystem();
    }

    public String getPath() {
        return delegate.getPath();
    }

    public boolean isWritable() {
        return delegate.isWritable();
    }

    public boolean isDirectory() {
        return delegate.isDirectory();
    }

    public boolean isValid() {
        return delegate.isValid();
    }

    public VirtualFile getParent() {
        return delegate.getParent();
    }

    public VirtualFile[] getChildren() {
        return delegate.getChildren();
    }


    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        return delegate.getOutputStream(requestor, newModificationStamp, newTimeStamp);
    }

    public byte[] contentsToByteArray() throws IOException {
        return delegate.contentsToByteArray();
    }

    public long getTimeStamp() {
        return delegate.getTimeStamp();
    }

    public long getLength() {
        return delegate.getLength();
    }

    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
        delegate.refresh(asynchronous, recursive, postRunnable);
    }

    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    public FileType getFileType() {
        return fileType;
    }

    @NotNull
    public String getUrl() {
        return delegate.getUrl();
    }

    public String getExtension() {
        return fileType.getDefaultExtension();
    }

    @NotNull
    public String getNameWithoutExtension() {
        return delegate.getNameWithoutExtension();
    }

    public void rename(Object requestor, String newName) throws IOException {
        delegate.rename(requestor, newName);
    }

    public VirtualFile findChild(String name) {
        return delegate.findChild(name);
    }

    public Icon getIcon() {
        return fileType.getIcon();
    }

    public VirtualFile findFileByRelativePath(String relPath) {
        return delegate.findFileByRelativePath(relPath);
    }

    public VirtualFile createChildDirectory(Object requestor, String name) throws IOException {
        return delegate.createChildDirectory(requestor, name);
    }

    public VirtualFile createChildData(Object requestor, String name) throws IOException {
        return delegate.createChildData(requestor, name);
    }

    public void delete(Object requestor) throws IOException {
        delegate.delete(requestor);
    }

    public void move(Object requestor, VirtualFile newParent) throws IOException {
        delegate.move(requestor, newParent);
    }

    public Charset getCharset() {
        return delegate.getCharset();
    }

    public void setBinaryContent(byte[] content, long newModificationStamp, long newTimeStamp) throws IOException {
        delegate.setBinaryContent(content, newModificationStamp, newTimeStamp);
    }

    public long getModificationStamp() {
        return delegate.getModificationStamp();
    }

    public void refresh(boolean asynchronous, boolean recursive) {
        delegate.refresh(asynchronous, recursive);
    }

    public <T> T getUserData(Key<T> key) {
        return delegate.getUserData(key);
    }

    public <T> void putUserData(Key<T> key, T value) {
        delegate.putUserData(key, value);
    }

    public String getPresentableName() {
        return delegate.getPresentableName();
    }

    public long getModificationCount() {
        return delegate.getModificationCount();
    }

    public void setCharset(Charset charset) {
        delegate.setCharset(charset);
    }

    public byte[] getBOM() {
        return delegate.getBOM();
    }

    public void setBOM(byte[] BOM) {
        delegate.setBOM(BOM);
    }

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

    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + fileType.hashCode();
        return result;
    }
}

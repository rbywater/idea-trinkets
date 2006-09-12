package org.intellij.trinkets.editorTree.view;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * Editors view
 *
 * @author Alexey Efimov
 */
public interface EditorTreeView extends Disposable {
    Project getProject();

    EditorTreeViewModel getModel();

    JComponent getComponent();

    void setSelectedFile(VirtualFile file);

    VirtualFile getSelectedFile();

    void setVisible(boolean visible);

    boolean isVisible();

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);
}

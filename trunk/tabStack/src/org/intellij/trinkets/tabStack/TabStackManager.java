package org.intellij.trinkets.tabStack;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ChangeListener;

/**
 * Tab stack manager listen editors closing.
 *
 * @author Alexey Efimov
 */
public abstract class TabStackManager {
    public static TabStackManager getInstance(Project project) {
        return project.getComponent(TabStackManager.class);
    }

    public abstract int getStackSize();

    @Nullable
    public abstract VirtualFile pop(boolean focusEditor);

    public abstract void addChangeListener(ChangeListener listener);

    public abstract void removeChangeListener(ChangeListener listener);
}

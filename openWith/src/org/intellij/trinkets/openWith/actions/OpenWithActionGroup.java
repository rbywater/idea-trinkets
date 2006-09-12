package org.intellij.trinkets.openWith.actions;

import com.intellij.ide.highlighter.UnknownFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Open with action group.
 *
 * @author Alexey Efimov
 */
public final class OpenWithActionGroup extends DefaultActionGroup {
    public void update(AnActionEvent e) {
        super.update(e);
        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        VirtualFile[] files = (VirtualFile[]) e.getDataContext().getData(DataConstants.VIRTUAL_FILE_ARRAY);
        boolean visible = project != null && files != null && files.length > 0;
        if (visible) {
            for (VirtualFile file : files) {
                if (visible && !(
                        (file.getFileType()instanceof LanguageFileType) ||
                                (file.getFileType()instanceof UnknownFileType)
                )
                        ) {
                    visible = false;
                }
            }
        }
        e.getPresentation().setVisible(visible);
        e.getPresentation().setEnabled(visible);
    }
}

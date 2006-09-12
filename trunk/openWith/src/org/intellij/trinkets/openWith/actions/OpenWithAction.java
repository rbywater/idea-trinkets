package org.intellij.trinkets.openWith.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.trinkets.openWith.util.OpenWithBundle;
import org.intellij.trinkets.openWith.vfs.VirtualFileWithFileType;

/**
 * Open with action.
 *
 * @author Alexey Efimov
 */
@SuppressWarnings({"ComponentNotRegistered"})
public final class OpenWithAction extends AnAction {
    private final FileType fileType;

    public OpenWithAction(FileType fileType) {
        super(OpenWithBundle.message("open.with.fileType", fileType.getDescription()), OpenWithBundle.message("open.with.fileType.description", fileType.getDescription()), fileType.getIcon());
        this.fileType = fileType;
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        VirtualFile[] files = (VirtualFile[]) e.getDataContext().getData(DataConstants.VIRTUAL_FILE_ARRAY);
        if (files != null && project != null) {
            FileEditorManager editorManager = FileEditorManager.getInstance(project);
            for (VirtualFile file : files) {
                editorManager.openFile(new VirtualFileWithFileType(file, fileType), true);
            }
        }
    }
}

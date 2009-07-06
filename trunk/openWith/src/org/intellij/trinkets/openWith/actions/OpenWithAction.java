package org.intellij.trinkets.openWith.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
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

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(DataKeys.PROJECT);
        VirtualFile[] files = e.getData(DataKeys.VIRTUAL_FILE_ARRAY);
        if (files != null && project != null) {
            FileEditorManager editorManager = FileEditorManager.getInstance(project);
            for (VirtualFile file : files) {
                editorManager.openFile(new VirtualFileWithFileType(file, fileType), true);
            }
        }
    }
}

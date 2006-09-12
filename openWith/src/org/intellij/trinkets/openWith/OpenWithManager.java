package org.intellij.trinkets.openWith;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.*;
import org.intellij.trinkets.openWith.actions.OpenWithAction;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Manager for registering actions.
 *
 * @author Alexey Efimov
 */
public final class OpenWithManager implements ApplicationComponent, FileTypeListener {
    private DefaultActionGroup actionGroup;

    @NonNls
    public String getComponentName() {
        return "OpenWithManager";
    }

    public void initComponent() {
        actionGroup = (DefaultActionGroup) ActionManager.getInstance().getAction("OpenWith.OpenWithGroup");
        FileTypeManager typeManager = FileTypeManager.getInstance();
        rebuildActionGroup(typeManager);
        typeManager.addFileTypeListener(this);
    }

    public void disposeComponent() {
        FileTypeManager typeManager = FileTypeManager.getInstance();
        typeManager.removeFileTypeListener(this);
    }

    private void rebuildActionGroup(FileTypeManager fileTypeManager) {
        if (actionGroup != null) {
            actionGroup.removeAll();
            FileType[] fileTypes = fileTypeManager.getRegisteredFileTypes();
            Arrays.sort(fileTypes, new FileTypeComparator());
            for (FileType fileType : fileTypes) {
                if (fileType instanceof LanguageFileType) {
                    actionGroup.add(new OpenWithAction(fileType));
                }
            }
        }
    }

    public void beforeFileTypesChanged(FileTypeEvent event) {
    }

    public void fileTypesChanged(FileTypeEvent event) {
        rebuildActionGroup(event.getManager());
    }

    private static final class FileTypeComparator implements Comparator<FileType> {
        public int compare(FileType o1, FileType o2) {
            return o1.getDescription().compareTo(o2.getDescription());
        }
    }
}

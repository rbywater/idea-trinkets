package org.intellij.trinkets.editorTree.history.tree.elements;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.intellij.trinkets.editorTree.history.FileHistory;
import org.intellij.trinkets.editorTree.history.FileHistoryUtil;

import javax.swing.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Element for {@link org.intellij.trinkets.editorTree.history.FileHistory}
 *
 * @author Alexey Efimov
 */
public final class FileHistoryTreeElement extends AbstractTreeElement<FileHistory> {
    public static final Comparator<FileHistory> COMPARATOR = new Comparator<FileHistory>() {
        public int compare(FileHistory o1, FileHistory o2) {
            return o1.compareTo(o2);
        }
    };
    private static final SimpleDateFormat TIME_DF = new SimpleDateFormat("H:mm");
    private final String text;
    private final Icon icon;

    public FileHistoryTreeElement(FileHistory history) {
        super(history, true);
        this.text = history.getPresentableName();
        VirtualFile virtualFile = FileHistoryUtil.resolve(history);
        if (virtualFile != null) {
            icon = FileTypeManager.getInstance().getFileTypeByFile(virtualFile).getIcon();
        } else {
            icon = null;
        }
    }

    public String getSpeedSearchText() {
        return text;
    }

    public void render(ColoredTreeCellRenderer renderer, JTree tree, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        renderer.setIcon(icon);
        renderer.append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        FileHistory history = getValue();
        VirtualFile virtualFile = FileHistoryUtil.resolve(history);
        if (virtualFile == null || !virtualFile.isValid()) {
            renderer.append("(Deleted)", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        } else {
            String wasClosed = "";
            String wasOpened = "";
            Date opened = history.getOpened();
            Date closed = history.getClosed();
            if (closed != null) {
                long time = (Calendar.getInstance().getTime().getTime() - closed.getTime()) / 60000;
                if (time > 0) {
                    if (time < 60) {
                        wasClosed = MessageFormat.format(", Closed {0} min. ago", time);
                    } else {
                        wasClosed = MessageFormat.format(", Closed at {0}", TIME_DF.format(closed));
                    }
                }
                time = (closed.getTime() - opened.getTime()) / 60000;
                if (time > 0) {
                    wasOpened = MessageFormat.format(" - {0} min. opened", time);
                }
            }
            renderer.append(MessageFormat.format(" ({0}{1}{2})", virtualFile.getPresentableUrl(), wasClosed, wasOpened), SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
    }
}

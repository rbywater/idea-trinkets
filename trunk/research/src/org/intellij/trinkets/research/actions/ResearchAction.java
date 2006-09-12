package org.intellij.trinkets.research.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.research.ResearchManager;

/**
 * Research action (like Alt+Click in MS Word).
 *
 * @author Alexey Efimov
 */
public class ResearchAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Editor editor = (Editor) e.getDataContext().getData(DataConstants.EDITOR);
        String searchString = getSearchString(editor);
        ResearchManager researchManager = ResearchManager.getInstance((Project) e.getDataContext().getData(DataConstants.PROJECT));
        researchManager.research(searchString);
    }

    private String getSearchString(Editor editor) {
        if (editor.getSelectionModel().hasSelection()) {
            return editor.getSelectionModel().getSelectedText();
        }

        int cursorOffset = editor.getCaretModel().getOffset();
        CharSequence editorText = editor.getDocument().getCharsSequence();
        if (editorText.length() == 0) {
            return null;
        }
        if (cursorOffset > 0 &&
                !Character.isJavaIdentifierPart(editorText.charAt(cursorOffset)) &&
                Character.isJavaIdentifierPart(editorText.charAt(cursorOffset - 1))) {
            cursorOffset--;
        }
        if (Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))) {
            int start = cursorOffset;
            int end = cursorOffset;
            while (start > 0 && Character.isJavaIdentifierPart(editorText.charAt(start - 1))) {
                start--;
            }
            while (end < editorText.length() && Character.isJavaIdentifierPart(editorText.charAt(end))) {
                end++;
            }
            return editorText.subSequence(start, end).toString();
        } else {
            return null;
        }
    }


    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(e.getDataContext().getData(DataConstants.PROJECT) != null);
    }
}

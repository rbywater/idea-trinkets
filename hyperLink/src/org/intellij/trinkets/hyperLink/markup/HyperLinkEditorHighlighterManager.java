package org.intellij.trinkets.hyperLink.markup;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryAdapter;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NonNls;

/**
 * Hyper link highlighter manager
 *
 * @author Alexey Efimov
 */
final class HyperLinkEditorHighlighterManager implements ApplicationComponent {
    private static final Key<HyperLinkEditorHighlighter> HYPERLINK_HIGHLIGHTER = Key.create("HYPERLINK_HIGHLIGHTER");

    private final EditorListener editorListener = new EditorListener();

    @NonNls public String getComponentName() {
        return "HyperLinkHighlighterManager";
    }

    public void initComponent() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorFactory.addEditorFactoryListener(editorListener);
    }

    public void disposeComponent() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorFactory.removeEditorFactoryListener(editorListener);
    }

    private static final class EditorListener extends EditorFactoryAdapter {
        public void editorCreated(EditorFactoryEvent event) {
            Editor editor = event.getEditor();
            HyperLinkEditorHighlighter hyperLinkEditorHighlighter = new HyperLinkEditorHighlighter(editor);
            editor.putUserData(HYPERLINK_HIGHLIGHTER, hyperLinkEditorHighlighter);
        }

        public void editorReleased(EditorFactoryEvent event) {
            Editor editor = event.getEditor();
            HyperLinkEditorHighlighter hyperLinkEditorHighlighter = editor.getUserData(HYPERLINK_HIGHLIGHTER);
            if (hyperLinkEditorHighlighter != null) {
                hyperLinkEditorHighlighter.dispose();
            }
        }
    }
}

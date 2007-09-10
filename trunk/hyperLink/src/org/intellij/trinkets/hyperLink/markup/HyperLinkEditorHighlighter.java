package org.intellij.trinkets.hyperLink.markup;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.IssueNavigationConfiguration;
import com.intellij.openapi.vcs.IssueNavigationLink;
import org.intellij.trinkets.hyperLink.HyperLinkReference;
import org.intellij.trinkets.hyperLink.HyperLinkReferenceManager;
import org.intellij.trinkets.hyperLink.IssueHyperLinkReference;
import org.intellij.trinkets.hyperLink.actions.HyperLinkAction;
import org.intellij.trinkets.hyperLink.actions.HyperLinkActions;
import org.intellij.trinkets.hyperLink.actions.HyperLinkEvent;
import org.intellij.trinkets.hyperLink.util.HyperLinkBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Link highlighter.
 *
 * @author Alexey Efimov
 */
final class HyperLinkEditorHighlighter implements Disposable {
    private final EditorMouseListener mouseListener = new MyEditorMouseAdapter();
    private final EditorMouseMotionListener mouseMotionListener = new MyEditorMouseMotionAdapter();
    private final KeyListener keyListener = new MyKeyAdapter();

    private final Editor editor;
    private boolean highlighEnabled;
    private MyMarker marker;

    public HyperLinkEditorHighlighter(Editor editor) {
        this.editor = editor;
        editor.addEditorMouseListener(mouseListener);
        editor.addEditorMouseMotionListener(mouseMotionListener);
        editor.getContentComponent().addKeyListener(keyListener);
    }

    public void dispose() {
        removeMarker();
        editor.removeEditorMouseListener(mouseListener);
        editor.removeEditorMouseMotionListener(mouseMotionListener);
        editor.getContentComponent().removeKeyListener(keyListener);
    }

    private void setHighlighEnabled(boolean highlighEnabled) {
        this.highlighEnabled = highlighEnabled;
        reparse();
    }

    private void updateMarker(@Nullable MyMarker parsedMarker) {
        try {
            if (marker != null) {
                if (marker.equals(parsedMarker)) {
                    // Previously highlighted is valid
                    marker.setVisible(highlighEnabled);
                    return;
                }
                removeMarker();
            }
            if (parsedMarker != null) {
                setMarker(parsedMarker);
            }
        } finally {
            updateCursor();
        }
    }

    private void setMarker(MyMarker marker) {
        // Set new highlighter
        this.marker = marker;
        this.marker.setToolTipVisible(true);
        this.marker.setVisible(highlighEnabled);
    }

    private void removeMarker() {
        if (marker != null) {
            marker.setToolTipVisible(false);
            marker.setVisible(false);
            marker = null;
        }
    }

    private void updateCursor() {
        if (highlighEnabled) {
            if (marker != null) {
                editor.getContentComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                editor.getContentComponent().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
        }
    }

    @Nullable
    private MyMarker getMarker() {
        Point mousePosition = editor.getContentComponent().getMousePosition();
        if (mousePosition != null) {
            LogicalPosition logicalPosition = editor.xyToLogicalPosition(mousePosition);
            Document document = editor.getDocument();
            if (logicalPosition.line < document.getLineCount()) {
                int lineStartOffset = document.getLineStartOffset(logicalPosition.line);
                int lineEndOffset = document.getLineEndOffset(logicalPosition.line);
                CharSequence line = document.getCharsSequence().subSequence(lineStartOffset, lineEndOffset);
                List<HyperLinkReference> references = new ArrayList<HyperLinkReference>();
                Project project = editor.getProject();
                try {
                    IssueNavigationConfiguration inc = IssueNavigationConfiguration.getInstance(project);
                    List<IssueNavigationLink> list = inc.getLinks();
                    for (IssueNavigationLink link : list) {
                        references.add(new IssueHyperLinkReference(link, HyperLinkColors.REFERENCE, HyperLinkActions.URL_ACTION));
                    }
                } catch (Exception e) {
                    // No issue configuration...
                    // TODO: Ask about NPE in getInstance to yole
                }
                references.addAll(Arrays.asList(HyperLinkReferenceManager.getInstance().getReferences()));
                for (HyperLinkReference reference : references) {
                    Pattern pattern = reference.getSearchPattern(null);
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        int start = matcher.start();
                        int end = matcher.end();
                        if (start <= logicalPosition.column && end >= logicalPosition.column) {
                            RangeMarker range = document.createRangeMarker(
                                    lineStartOffset + start, lineStartOffset + end
                            );

                            return new MyMarker(reference, range, matcher.reset(matcher.group()));
                        }
                    }
                }
            }
        }
        return null;
    }

    private void reparse() {
        updateMarker(getMarker());
    }

    private final class MyEditorMouseAdapter extends EditorMouseAdapter {
        @SuppressWarnings({"unchecked"})
        public void mouseClicked(EditorMouseEvent e) {
            if (!e.isConsumed()) {
                // Left click
                if ((e.getMouseEvent().getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
                    if (marker != null) {
                        HyperLinkAction action = marker.getReference().getAction();
                        action.performAction(marker);
                        e.consume();
                    }
                }
            }
        }
    }

    private final class MyEditorMouseMotionAdapter extends EditorMouseMotionAdapter {
        public void mouseMoved(EditorMouseEvent e) {
            reparse();
        }
    }

    private final class MyKeyAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
                setHighlighEnabled(true);
            }
        }

        public void keyReleased(KeyEvent e) {
            if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != KeyEvent.CTRL_DOWN_MASK) {
                setHighlighEnabled(false);
            }
        }
    }

    private final class MyMarker implements HyperLinkEvent {
        @NotNull
        private final HyperLinkReference reference;
        @NotNull
        private final RangeMarker range;
        private final Matcher matcher;
        private RangeHighlighter highlighter;

        public MyMarker(
                @NotNull HyperLinkReference reference,
                @NotNull RangeMarker range,
                @NotNull Matcher matcher
        ) {
            this.reference = reference;
            this.range = range;
            this.matcher = matcher;
        }

        @NotNull
        public HyperLinkReference getReference() {
            return reference;
        }

        @NotNull
        public Matcher getMatcher() {
            return matcher;
        }

        @NotNull
        public RangeMarker getRange() {
            return range;
        }

        @NotNull
        public String getReferenceText() {
            return getText();
        }

        private String getText() {
            String replacePattern = reference.getReplacePattern();
            matcher.reset();
            matcher.find();
            return matcher.replaceAll(replacePattern);
        }

        private String getToolTip() {
            return HyperLinkBundle.message("reference.tooltip", getText());
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof MyMarker) {
                MyMarker other = (MyMarker) obj;
                return reference.equals(other.reference) &&
                        range.getStartOffset() == other.getRange().getStartOffset() &&
                        range.getEndOffset() == other.getRange().getEndOffset() &&
                        range.getDocument().equals(other.getRange().getDocument());
            }
            return false;
        }

        public int hashCode() {
            int result = reference.hashCode();
            result = 29 * result + range.hashCode();
            return result;
        }

        public void setToolTipVisible(boolean visible) {
            if (visible) {
                editor.getContentComponent().setToolTipText(getToolTip());
            } else {
                editor.getContentComponent().setToolTipText(null);
            }
        }

        public void setVisible(boolean visible) {
            if (visible) {
                if (highlighter == null) {
                    highlighter = editor.getMarkupModel().addRangeHighlighter(
                            range.getStartOffset(),
                            range.getEndOffset(),
                            HighlighterLayer.ADDITIONAL_SYNTAX,
                            reference.getTextAttributes(),
                            HighlighterTargetArea.EXACT_RANGE
                    );
                }
            } else {
                if (highlighter != null) {
                    editor.getMarkupModel().removeHighlighter(highlighter);
                    highlighter = null;
                }
            }
        }
    }
}

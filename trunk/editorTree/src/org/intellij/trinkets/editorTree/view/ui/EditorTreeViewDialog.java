package org.intellij.trinkets.editorTree.view.ui;

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import org.intellij.trinkets.editorTree.util.EditorTreeBundle;
import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeViewActionsUtil;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeViewActionMonitor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog for tree view.
 *
 * @author Alexey Efimov
 */
public class EditorTreeViewDialog extends DialogWrapper {
    private static final Action[] EMPTY_ACTION_ARRAY = new Action[]{};

    private static final int RESIZE_DELAY = 200;
    private final Alarm modelTrigger = new Alarm();
    private final Runnable updateCommand = new UpdateCommand();

    private final EditorTreeView view;
    private final MouseListener doubleClickListener = new DblClickAdapter();
    private final KeyListener keyListener = new KeyboardAdapter();
    private final FocusListener focusGuard = new FocusGuard();

    private final ChangeListener viewChangeListener = new ViewChangeListener();
    private final ChangeListener actionMonitorChangeListener = new ActionMonitorChangeListener();

    private JPanel panel = null;
    private JScrollPane scroolPane = null;

    public EditorTreeViewDialog(Project project, EditorTreeView view) {
        super(project, true);
        this.view = view;

        EditorTreeViewActionsUtil.installShortcuts(view);
        init();
    }

    protected final void init() {
        setUndecorated(true);
        setModal(false);
        setResizable(false);

        EditorTreeViewActionMonitor.getInstance().addChangeListener(actionMonitorChangeListener);
        view.addChangeListener(viewChangeListener);

        super.init();
    }

    public void show() {
        view.setVisible(true);
        super.show();
    }

    protected Action[] createActions() {
        return EMPTY_ACTION_ARRAY;
    }

    protected Border createContentPaneBorder() {
        return BorderFactory.createEmptyBorder();
    }

    private void centerDialog() {
        Component parent = getOwner();
        Point parentPoint = parent.getLocation();
        Dimension parentDimension = parent.getSize();
        Dimension dialogDimension = getSize();
        int x = (int)(parentPoint.getX() + (parentDimension.getWidth() - dialogDimension.getWidth()) / 2D);
        int y = (int)(parentPoint.getY() + (parentDimension.getHeight() - dialogDimension.getHeight()) / 2D);
        setLocation(x, y);
    }

    protected JComponent createCenterPanel() {
        if (panel == null) {
            panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel();
            label.setText(EditorTreeBundle.message("popup.title"));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setHorizontalTextPosition(SwingConstants.CENTER);

            JComponent component = view.getComponent();
            component.addMouseListener(doubleClickListener);
            component.addFocusListener(focusGuard);
            component.addKeyListener(keyListener);


            scroolPane = new JScrollPane(component);
            scroolPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            panel.add(label, BorderLayout.NORTH);
            panel.add(scroolPane, BorderLayout.CENTER);


            if (!UIUtil.isMotifLookAndFeel()) {
                UIUtil.installPopupMenuBorder(panel);
            }
            UIUtil.installPopupMenuColorAndFonts(panel);
        }
        return panel;
    }

    protected void dispose() {
        super.dispose();

        view.removeChangeListener(viewChangeListener);
        EditorTreeViewActionMonitor.getInstance().removeChangeListener(actionMonitorChangeListener);

        JComponent component = view.getComponent();
        component.removeFocusListener(focusGuard);
        component.removeMouseListener(doubleClickListener);
        component.removeKeyListener(keyListener);

        scroolPane.removeAll();
        scroolPane = null;

        EditorTreeViewActionsUtil.uninstallShortcuts(view);
    }

    private class DblClickAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (!e.isConsumed()) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openCurrentFile(e);
                }
            }
        }
    }

    private class KeyboardAdapter extends KeyAdapter {
        @SuppressWarnings({"HardcodedLineSeparator", "MagicCharacter"})
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == '\n' || e.getKeyCode() == KeyEvent.VK_ENTER) {
                openCurrentFile(e);
            }
        }
    }

    private void openCurrentFile(InputEvent e) {
        VirtualFile selectedFile = view.getSelectedFile();
        if (selectedFile != null && !selectedFile.isDirectory()) {
            FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(view.getProject());
            fileEditorManager.openFile(selectedFile, true);
            e.consume();
            view.setVisible(false);
        }
    }

    private class FocusGuard implements FocusListener {
        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            Component oppositeComponent = e.getOppositeComponent();
            if (oppositeComponent == null || !getWindow().equals(oppositeComponent.getParent())) {
                view.setVisible(false);
            }
        }
    }

    private class UpdateCommand implements Runnable {
        public void run() {
            pack();
            centerDialog();
            view.getComponent().requestFocus(false);
        }
    }

    private final class ViewChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if (isVisible()) {
                modelTrigger.cancelAllRequests();
                if (view.isVisible() && view.getModel().getFilesCount() > 0) {
                    modelTrigger.addRequest(updateCommand, RESIZE_DELAY);
                } else {
                    close(0);
                }
            }
        }
    }

    private final class ActionMonitorChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            EditorTreeViewActionMonitor monitor = (EditorTreeViewActionMonitor)e.getSource();
            if (monitor.isDispatchInProgress()) {
                view.getComponent().removeFocusListener(focusGuard);
            } else {
                Application application = ApplicationManager.getApplication();
                application.invokeLater(new Runnable() {
                    public void run() {
                        view.getComponent().addFocusListener(focusGuard);
                    }
                });
            }
        }
    }
}

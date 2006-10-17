package org.intellij.trinkets.editorTree.history.tree;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.TreeToolTipHandler;
import com.intellij.util.Alarm;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.Tree;
import com.intellij.util.ui.tree.TreeModelAdapter;
import org.intellij.trinkets.editorTree.history.FileHistory;
import org.intellij.trinkets.editorTree.history.FileHistoryUtil;
import org.intellij.trinkets.editorTree.history.tree.elements.FileHistoryTreeElement;
import org.intellij.trinkets.editorTree.history.tree.elements.TreeElement;
import org.intellij.trinkets.editorTree.history.tree.elements.TreeElementRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * File history panel
 *
 * @author Alexey Efimov
 */
public final class FileHistoryTreePanel extends JPanel implements DataProvider, Disposable {
    private static final Navigatable[] EMPTY_NAVIGATABLE_ARRAY = new Navigatable[]{};
    private final Project project;

    private final TreeCellRenderer treeRenderer = new TreeElementRenderer();
    private final FileHistoryTreeModel model;
    private final Tree tree;
    private final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
    private final Runnable updateUITask = new Runnable() {
        public void run() {
            tree.updateUI();
            alarm.addRequest(this, 60000);
        }
    };

    public FileHistoryTreePanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        model = new DefaultFileHistoryTreeModel(project);
        tree = new Tree(model);
        tree.setCellRenderer(treeRenderer);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        // Smart expander
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            public void treeWillCollapse(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                TreeElement element = (TreeElement) path.getLastPathComponent();
                int count = element.getChildCount();
                for (int i = 0; i < count; i++) {
                    TreePath childPath = path.pathByAddingChild(element.getChild(i));
                    if (tree.isExpanded(childPath)) {
                        tree.collapsePath(childPath);
                    }
                }
            }

            public void treeWillExpand(TreeExpansionEvent event) {
            }
        });
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent event) {
            }

            public void treeExpanded(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                TreeElement element = (TreeElement) path.getLastPathComponent();
                if (element.getChildCount() == 1) {
                    TreePath firstChildPath = path.pathByAddingChild(element.getChild(0));
                    tree.expandPath(firstChildPath);
                }
            }
        });
        TreeToolTipHandler.install(tree);
        new TreeSpeedSearch(
                tree,
                new Convertor<TreePath, String>() {
                    public String convert(TreePath o) {
                        TreeElement treeNode = (TreeElement) o.getLastPathComponent();
                        return treeNode != null ? treeNode.getSpeedSearchText() : null;
                    }
                }
        );

        TreeSelectionModel treeSelectionModel = tree.getSelectionModel();
        treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (MouseEvent.BUTTON3 == e.getButton() && e.getClickCount() == 1) {
                    // Single right click
                    ActionManager actionManager = ActionManager.getInstance();
                    ActionGroup actionGroup = (ActionGroup) actionManager.getAction("EditorTree.History.PopupMenu");
                    if (actionGroup != null) {
                        ActionPopupMenu menu = actionManager.createActionPopupMenu("EditorTree.History", actionGroup);
                        JPopupMenu popupMenu = menu.getComponent();
                        popupMenu.pack();
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());

                        e.consume();
                    }
                }
                if (MouseEvent.BUTTON1 == e.getButton() && e.getClickCount() == 2) {
                    TreePath[] treePaths = tree.getSelectionModel().getSelectionPaths();
                    if (treePaths != null && treePaths.length == 1) {
                        TreeElement element = (TreeElement) treePaths[0].getLastPathComponent();
                        if (element.isLeaf()) {
                            FileHistory fileHistory = ((FileHistoryTreeElement) element).getValue();
                            FileEditorManager fileEditorManager = FileEditorManager.getInstance(FileHistoryTreePanel.this.project);
                            fileEditorManager.openFile(FileHistoryUtil.resolve(fileHistory), true);
                        }
                    }
                }
            }
        });
        model.addTreeModelListener(new TreeModelAdapter() {
            public void treeNodesInserted(TreeModelEvent treeModelEvent) {
                tree.updateUI();
            }

            public void treeNodesRemoved(TreeModelEvent treeModelEvent) {
                tree.updateUI();
            }

            public void treeNodesChanged(TreeModelEvent treeModelEvent) {
                tree.updateUI();
            }
        });
        add(new JScrollPane(tree), BorderLayout.CENTER);

        alarm.addRequest(updateUITask, 60000);
    }

    @NotNull
    public FileHistory[] getSelectedHistories() {
        Set<FileHistory> histories = new HashSet<FileHistory>();
        TreeSelectionModel treeSelectionModel = tree.getSelectionModel();
        TreePath[] treePaths = treeSelectionModel.getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                TreeElement element = (TreeElement) treePath.getLastPathComponent();
                if (element.isLeaf()) {
                    FileHistoryTreeElement fileElement = (FileHistoryTreeElement) element;
                    histories.add(fileElement.getValue());
                } else {
                    int count = element.getChildCount();
                    for (int i = 0; i < count; i++) {
                        FileHistoryTreeElement fileElement = (FileHistoryTreeElement) element.getChild(i);
                        histories.add(fileElement.getValue());
                    }
                }
            }
        }
        return histories.toArray(FileHistory.EMPTY_ARRAY);
    }

    public FileHistoryTreeModel getModel() {
        return model;
    }

    @Nullable
    public Object getData(String dataId) {
        if (DataConstantsEx.PROJECT.equals(dataId)) {
            return project;
        } else if (DataConstantsEx.VIRTUAL_FILE.equals(dataId)) {
            VirtualFile[] selectedFiles = getSelectedFiles();
            return selectedFiles.length > 0 ? selectedFiles[0] : null;
        } else if (DataConstantsEx.VIRTUAL_FILE_ARRAY.equals(dataId)) {
            return getSelectedFiles();
        } else if (DataConstantsEx.PSI_FILE.equals(dataId)) {
            return getData(DataConstantsEx.PSI_ELEMENT);
        } else if (DataConstants.PSI_ELEMENT.equals(dataId)) {
            VirtualFile[] selectedFiles = getSelectedFiles();
            return selectedFiles.length > 0 ? PsiManager.getInstance(project).findFile(selectedFiles[0]) : null;
        } else if (DataConstantsEx.PSI_ELEMENT_ARRAY.equals(dataId)) {
            return getSelectedElements();
        } else if (DataConstantsEx.NAVIGATABLE.equals(dataId)) {
            VirtualFile[] selectedFiles = getSelectedFiles();
            return new FilelNavigatable(selectedFiles.length > 0 ? selectedFiles[0] : null);
        } else if (DataConstantsEx.NAVIGATABLE_ARRAY.equals(dataId)) {
            VirtualFile[] selectedFiles = getSelectedFiles();
            Set<Navigatable> navigatables = new HashSet<Navigatable>(selectedFiles.length);
            for (VirtualFile selectedFile : selectedFiles) {
                if (!selectedFile.isDirectory()) {
                    navigatables.add(new FilelNavigatable(selectedFile));
                }
            }
            return navigatables.toArray(EMPTY_NAVIGATABLE_ARRAY);
        }

        return null;
    }

    @NotNull
    private PsiElement[] getSelectedElements() {
        VirtualFile[] selectedFiles = getSelectedFiles();
        Set<PsiElement> psiElements = new HashSet<PsiElement>(selectedFiles.length);
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile file : selectedFiles) {
            PsiFile psiFile = psiManager.findFile(file);
            PsiElement element = psiFile != null ? psiFile : psiManager.findDirectory(file);
            if (element != null) {
                psiElements.add(element);
            }
        }
        return psiElements.toArray(PsiElement.EMPTY_ARRAY);
    }

    private VirtualFile[] getSelectedFiles() {
        FileHistory[] histories = getSelectedHistories();
        return FileHistoryUtil.resolve(histories);
    }

    private final class FilelNavigatable implements Navigatable {
        private VirtualFile file;

        public FilelNavigatable(VirtualFile file) {
            this.file = file;
        }

        public void navigate(boolean requestFocus) {
            if (file != null) {
                FileEditorManager manager = FileEditorManager.getInstance(project);
                manager.openFile(file, true);
            }
        }

        public boolean canNavigate() {
            return file != null;
        }

        public boolean canNavigateToSource() {
            return file != null;
        }
    }

    public void dispose() {
        alarm.cancelAllRequests();
    }
}

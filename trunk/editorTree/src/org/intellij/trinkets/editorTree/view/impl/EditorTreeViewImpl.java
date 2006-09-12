package org.intellij.trinkets.editorTree.view.impl;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SmartExpander;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.TreeToolTipHandler;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.intellij.trinkets.editorTree.view.EditorTreeNode;
import org.intellij.trinkets.editorTree.view.EditorTreeView;
import org.intellij.trinkets.editorTree.view.EditorTreeViewModel;
import org.intellij.trinkets.editorTree.view.EditorTreeViewModelListener;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeDataConstants;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeViewActionPlaces;
import org.intellij.trinkets.editorTree.view.actionSystem.EditorTreeViewActions;
import org.intellij.trinkets.editorTree.view.util.EditorTreeUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;

final class EditorTreeViewImpl implements EditorTreeView, DataProvider {
    private final Project project;
    private final EditorTreeViewModel model = new EditorTreeViewModelImpl();
    private final EditorTreeViewModelListener modelListener = new EditorTreeChangeListener();
    private final EditorTreeNode root = new EditorTreeNodeImpl();
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(0);
    private final Tree tree = new EditorTreeComponent(root, this);

    private boolean visible = false;

    public EditorTreeViewImpl(Project project) {
        this.project = project;
        // Create listenres
        model.addEditorsViewModelListener(modelListener);

        tree.setRootVisible(false);
        tree.setExpandsSelectedPaths(true);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new EditorTreeCellRenderer());
        tree.addTreeExpansionListener(new ExpansionListener());
        tree.addTreeSelectionListener(new SelectionListener());
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.setLineStyleAngled();

        SmartExpander.installOn(tree);

        PopupHandler.installPopupHandler(tree, EditorTreeViewActions.POPUP_GROUP, EditorTreeViewActionPlaces.POPUP);

        TreeToolTipHandler.install(tree);

        TreeSelectionModel selectionModel = tree.getSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }

    public Project getProject() {
        return project;
    }

    public EditorTreeViewModel getModel() {
        return model;
    }

    public JComponent getComponent() {
        return tree;
    }

    public void setSelectedFile(VirtualFile file) {
        Enumeration enumeration = root.children();
        boolean selected = false;
        while (enumeration.hasMoreElements() && !selected) {
            EditorTreeNode node = (EditorTreeNode)enumeration.nextElement();
            TreeNode fileNode = TreeUtil.findNodeWithObject((DefaultMutableTreeNode)node, file);
            if (fileNode != null) {
                TreePath path = TreeUtil.getPathFromRoot(fileNode);
                tree.setSelectionPath(path);
                tree.expandPath(path);
                selected = true;
            }
        }
        TreeUtil.expandRootChildIfOnlyOne(tree);
    }

    public VirtualFile getSelectedFile() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            EditorTreeNode node = (EditorTreeNode)selectionPath.getLastPathComponent();
            return node.getFile();
        }
        return null;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        fireChanged();
    }

    public boolean isVisible() {
        return visible;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public void dispose() {
        // Dispose tree
        root.removeAll();
        // Remove listenres
        model.removeEditorsViewModelListener(modelListener);
    }

    private final class EditorTreeChangeListener implements EditorTreeViewModelListener {
        private final Comparator<EditorTreeNode> comparator = new NodeComparator();

        public void fileAdded(VirtualFile file) {
            Enumeration enumeration = root.children();
            boolean added = false;
            while (enumeration.hasMoreElements() && !added) {
                EditorTreeNode node = (EditorTreeNode)enumeration.nextElement();
                added = node.append(file);
            }
            if (!added) {
                rebuildTree();
            }
            revalidateUI();
        }

        public void fileRemoved(VirtualFile file) {
            Enumeration enumeration = root.children();
            boolean removed = false;
            while (enumeration.hasMoreElements() && !removed) {
                EditorTreeNode node = (EditorTreeNode)enumeration.nextElement();
                removed = node.remove(file);
            }
            if (removed) {
                // Roots can be changed, then must be full tree rebuild
                rebuildTree();
                revalidateUI();
            }
        }

        private void revalidateUI() {
            TreeUtil.sort((DefaultMutableTreeNode)root, comparator);
            DefaultTreeModel tableModel = (DefaultTreeModel)tree.getModel();
            tableModel.reload();
            fireChanged();
        }

        private void rebuildTree() {
            // Create new roots (rebuild tree)
            VirtualFile[] files = model.getFiles();
            if (files.length > 0) {
                VirtualFile[] roots = VfsUtil.getCommonAncestors(files);
                List<VirtualFile> filesPool = new ArrayList<VirtualFile>(Arrays.asList(files));

                // Update tree
                root.removeAll();

                int index = 0;
                for (VirtualFile filesRoot : roots) {
                    EditorTreeNode filesRootNode = new EditorTreeNodeImpl(filesRoot);
                    root.insert(filesRootNode, index++);
                    Iterator<VirtualFile> iterator = filesPool.iterator();
                    while (iterator.hasNext()) {
                        VirtualFile childFile = iterator.next();
                        if (filesRootNode.append(childFile)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        private class NodeComparator implements Comparator<EditorTreeNode> {
            public int compare(EditorTreeNode o1, EditorTreeNode o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                return o1.compareTo(o2);
            }
        }
    }

    @Nullable
    public Object getData(String dataId) {
        if (EditorTreeDataConstants.PROJECT.equals(dataId)) {
            return project;
        } else if (EditorTreeDataConstants.VIRTUAL_FILE_ARRAY.equals(dataId)) {
            return getSelected();
        } else if (EditorTreeDataConstants.VIRTUAL_FILE.equals(dataId)) {
            VirtualFile[] files = (VirtualFile[])getData(EditorTreeDataConstants.VIRTUAL_FILE_ARRAY);
            return files != null && files.length > 0 ? files[0] : null;
        } else if (EditorTreeDataConstants.EDITOR_TREE_VIEW.equals(dataId)) {
            return this;
        }
        return null;
    }

    private VirtualFile[] getSelected() {
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths != null && selectionPaths.length > 0) {
            List<VirtualFile> files = new ArrayList<VirtualFile>(selectionPaths.length);
            for (TreePath treePath : selectionPaths) {
                EditorTreeNode node = (EditorTreeNode)treePath.getLastPathComponent();
                VirtualFile[] inheritedFiles = EditorTreeUtil.getFiles(node);
                for (VirtualFile file : inheritedFiles) {
                    files.add(file);
                }
            }
            return files.toArray(VirtualFile.EMPTY_ARRAY);
        }
        return null;
    }

    private static final class EditorTreeComponent extends Tree implements DataProvider {
        private final DataProvider dataProvider;

        @SuppressWarnings({"ThisEscapedInObjectConstruction"})
        public EditorTreeComponent(EditorTreeNode root, DataProvider dataProvider) {
            super(root);
            this.dataProvider = dataProvider;
            new TreeSpeedSearch(
                this, new Convertor<TreePath, String>() {
                public String convert(TreePath o) {
                    EditorTreeNode node = (EditorTreeNode)o.getLastPathComponent();
                    VirtualFile file = node.getFile();
                    return file != null ? file.getPresentableName() : null;
                }
            }
            );
        }

        @Nullable
        public Object getData(String dataId) {
            return dataProvider.getData(dataId);
        }
    }

    private class ExpansionListener implements TreeExpansionListener {
        public void treeExpanded(TreeExpansionEvent event) {
            fireChanged();
        }

        public void treeCollapsed(TreeExpansionEvent event) {
            fireChanged();
        }
    }

    private class SelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            fireChanged();
        }
    }

    private void fireChanged() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(e);
        }
    }
}

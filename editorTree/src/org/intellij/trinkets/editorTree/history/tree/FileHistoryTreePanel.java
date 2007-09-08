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

        // TODO: Here exception:
        //Access is allowed from event dispatch thread only.
        //Details: Current thread: Thread[ApplicationImpl pooled thread,6,Idea Thread Group] 22029867
        //Our dispatch thread:Thread[AWT-EventQueue-1,6,Idea Thread Group] 19275957
        //SystemEventQueueThread: Thread[AWT-EventQueue-1,6,Idea Thread Group] 19275957
        //
        //java.lang.Throwable
        //    at com.intellij.openapi.diagnostic.Logger.error(Logger.java:52)
        //    at com.intellij.openapi.application.impl.ApplicationImpl.a(ApplicationImpl.java:77)
        //    at com.intellij.openapi.application.impl.ApplicationImpl.assertIsDispatchThread(ApplicationImpl.java:271)
        //    at com.intellij.openapi.application.impl.LaterInvocator.getCurrentModalEntities(LaterInvocator.java:94)
        //    at com.intellij.openapi.application.impl.ApplicationImpl.getCurrentModalityState(ApplicationImpl.java:94)
        //    at com.intellij.openapi.application.ModalityState.current(ModalityState.java:30)
        //    at com.intellij.util.Alarm.addRequest(Alarm.java:86)
        //    at org.intellij.trinkets.editorTree.history.tree.FileHistoryTreePanel.<init>(FileHistoryTreePanel.java:155)
        //    at org.intellij.trinkets.editorTree.history.FileHistoryManager.<init>(FileHistoryManager.java:46)
        //    at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        //    at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
        //    at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
        //    at java.lang.reflect.Constructor.newInstance(Constructor.java:494)
        //    at org.picocontainer.defaults.InstantiatingComponentAdapter.newInstance(InstantiatingComponentAdapter.java:193)
        //    at org.picocontainer.defaults.ConstructorInjectionComponentAdapter$1.run(ConstructorInjectionComponentAdapter.java:220)
        //    at org.picocontainer.defaults.ThreadLocalCyclicDependencyGuard.observe(ThreadLocalCyclicDependencyGuard.java:53)
        //    at org.picocontainer.defaults.ConstructorInjectionComponentAdapter.getComponentInstance(ConstructorInjectionComponentAdapter.java:248)
        //    at org.picocontainer.defaults.DecoratingComponentAdapter.getComponentInstance(DecoratingComponentAdapter.java:60)
        //    at org.picocontainer.defaults.CachingComponentAdapter.getComponentInstance(CachingComponentAdapter.java:58)
        //    at com.intellij.openapi.components.impl.ComponentManagerImpl$ComponentConfigComponentAdapter$1.getComponentInstance(ComponentManagerImpl.java:6)
        //    at com.intellij.openapi.components.impl.ComponentManagerImpl$ComponentConfigComponentAdapter.getComponentInstance(ComponentManagerImpl.java:32)
        //    at com.intellij.util.pico.DefaultPicoContainer.getLocalInstance(DefaultPicoContainer.java:193)
        //    at com.intellij.util.pico.DefaultPicoContainer.getInstance(DefaultPicoContainer.java:180)
        //    at com.intellij.util.pico.DefaultPicoContainer.getComponentInstance(DefaultPicoContainer.java:162)
        //    at org.picocontainer.alternatives.AbstractDelegatingMutablePicoContainer.getComponentInstance(AbstractDelegatingMutablePicoContainer.java:75)
        //    at com.intellij.openapi.components.impl.ComponentManagerImpl.a(ComponentManagerImpl.java:25)
        //    at com.intellij.openapi.components.impl.ComponentManagerImpl.a(ComponentManagerImpl.java:170)
        //    at com.intellij.openapi.components.impl.ComponentManagerImpl.initComponents(ComponentManagerImpl.java:19)
        //    at com.intellij.openapi.components.impl.ComponentManagerImpl.init(ComponentManagerImpl.java:28)
        //    at com.intellij.openapi.project.impl.ProjectImpl.init(ProjectImpl.java:70)
        //    at com.intellij.openapi.components.impl.stores.ProjectStoreImpl.loadProject(ProjectStoreImpl.java:164)
        //    at com.intellij.openapi.project.impl.ProjectManagerImpl.a(ProjectManagerImpl.java:197)
        //    at com.intellij.openapi.project.impl.ProjectManagerImpl.access$100(ProjectManagerImpl.java:88)
        //    at com.intellij.openapi.project.impl.ProjectManagerImpl$6.run(ProjectManagerImpl.java:8)
        //    at com.intellij.openapi.progress.impl.ProgressManagerImpl$2.run(ProgressManagerImpl.java:8)
        //    at com.intellij.openapi.progress.impl.ProgressManagerImpl.executeProcessUnderProgress(ProgressManagerImpl.java:79)
        //    at com.intellij.openapi.progress.impl.ProgressManagerImpl.runProcess(ProgressManagerImpl.java:2)
        //    at com.intellij.openapi.application.impl.ApplicationImpl$7$1.run(ApplicationImpl.java:4)
        //    at com.intellij.openapi.application.impl.ApplicationImpl$5.run(ApplicationImpl.java:2)
        //    at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:417)
        //    at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:269)
        //    at java.util.concurrent.FutureTask.run(FutureTask.java:123)
        //    at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:650)
        //    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:675)
        //    at java.lang.Thread.run(Thread.java:595)
        //    at com.intellij.openapi.application.impl.ApplicationImpl$1$1.run(ApplicationImpl.java:6)
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

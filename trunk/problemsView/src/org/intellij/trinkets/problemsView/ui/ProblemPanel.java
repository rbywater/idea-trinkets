package org.intellij.trinkets.problemsView.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.TreeToolTipHandler;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.Tree;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.intellij.trinkets.problemsView.problems.ProblemFix;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tree panel.
 *
 * @author Alexey Efimov
 */
public class ProblemPanel extends JPanel implements ChangeListener {
    @NonNls
    private static final String BOLD_HTML_TAG = "b";
    @NonNls
    private static final Pattern HTML_PATTERN = Pattern.compile("<([^>]+)>(.*?)</\\1>");

    private final TreeCellRenderer treeRenderer = new MyColoredTreeCellRenderer();
    private final StatusBar statusBar;
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    private final Tree tree = new Tree(root);

    private TreeNodeModel<Problem> model;

    public ProblemPanel(final Project project) {
        super(new BorderLayout());
        statusBar = WindowManager.getInstance().getStatusBar(project);
        tree.setCellRenderer(treeRenderer);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(false);
        TreeToolTipHandler.install(tree);
        new TreeSpeedSearch(
                tree,
                new Convertor<TreePath, String>() {
                    public String convert(TreePath o) {
                        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) o.getLastPathComponent();
                        TreeNodeElement treeNode = (TreeNodeElement) mutableTreeNode.getUserObject();
                        return treeNode != null ? treeNode.getNodeText() : null;
                    }
                }
        );

        TreeSelectionModel treeSelectionModel = tree.getSelectionModel();
        treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeSelectionModel.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getPath();
                if (path != null) {
                    DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    TreeNodeElement treeNode = (TreeNodeElement) mutableTreeNode.getUserObject();
                    if (treeNode != null) {
                        statusBar.setInfo(treeNode.getStatusBarText());
                    }
                }
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (MouseEvent.BUTTON3 == e.getButton() && e.getClickCount() == 1) {
                    // Single right click
                    ActionManager actionManager = ActionManager.getInstance();
                    DefaultActionGroup actionGroup = new DefaultActionGroup(null, true);

                    Problem[] selectedProblems = getSelectedProblems();
                    for (Problem problem : selectedProblems) {
                        ProblemFix[] problemFixes = problem.getFixes();
                        for (ProblemFix fix : problemFixes) {
                            actionGroup.add(new FixAction(project, fix));
                        }
                    }

                    ActionPopupMenu menu = actionManager.createActionPopupMenu("Trinkets.ToolWindow", actionGroup);
                    JPopupMenu popupMenu = menu.getComponent();
                    popupMenu.pack();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());

                    e.consume();
                }
            }
        });

        add(new JScrollPane(tree));
        setModel(new ProblemModel());
    }

    @NotNull
    public Problem[] getSelectedProblems() {
        List<Problem> list = new ArrayList<Problem>();
        TreeSelectionModel treeSelectionModel = tree.getSelectionModel();
        TreePath[] treePaths = treeSelectionModel.getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                TreeNodeElement treeNode = (TreeNodeElement) node.getUserObject();
                list.add((Problem) treeNode);
            }
        }
        return list.toArray(Problem.EMPTY_PROBLEM_ARRAY);
    }

    public TreeNodeModel<Problem> getModel() {
        return model;
    }

    public void setModel(TreeNodeModel<Problem> model) {
        if (this.model != null) {
            this.model.removeChangeListener(this);
        }
        this.model = model;

        if (this.model != null) {
            this.model.addChangeListener(this);
        }
    }

    public void dispose() {
        if (this.model != null) {
            this.model.removeChangeListener(this);
        }
    }

    public void stateChanged(ChangeEvent e) {
        updateStructure();
    }

    public void updateStructure() {
        root.removeAllChildren();
        for (Problem problem : model) {
            DefaultMutableTreeNode problemRoot = new DefaultMutableTreeNode(problem, true);
            root.add(problemRoot);
        }
        tree.updateUI();
    }

    private static class ProblemModel implements TreeNodeModel<Problem> {
        private final Collection<Problem> problems = new ArrayList<Problem>(0);
        private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(0);

        public void add(@NotNull Problem element) {
            problems.add(element);
            fireChanged();
        }

        public void remove(@NotNull Problem element) {
            problems.remove(element);
            fireChanged();
        }

        public void removeAll() {
            problems.clear();
            fireChanged();
        }

        private void fireChanged() {
            ChangeEvent event = new ChangeEvent(this);
            for (ChangeListener listener : listeners) {
                listener.stateChanged(event);
            }
        }

        public void addChangeListener(ChangeListener changeListener) {
            listeners.add(changeListener);
        }

        public void removeChangeListener(ChangeListener changeListener) {
            listeners.remove(changeListener);
        }

        public Iterator<Problem> iterator() {
            return problems.iterator();
        }
    }

    private class MyColoredTreeCellRenderer extends ColoredTreeCellRenderer {

        public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) value;
            TreeNodeElement treeNode = (TreeNodeElement) mutableTreeNode.getUserObject();
            if (treeNode != null) {
                setIcon(treeNode.getIcon());
                setToolTipText(treeNode.getToolTipText());
                String fragment = treeNode.getNodeText();
                Matcher matcher = HTML_PATTERN.matcher(fragment);
                int lastEnd = 0;
                while (matcher.find()) {
                    String tag = matcher.group(1);
                    String text = matcher.group(2);
                    int start = matcher.start();
                    append(fragment.substring(lastEnd, start), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    if (BOLD_HTML_TAG.equals(tag.toLowerCase())) {
                        append(text, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                    } else {
                        append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    }
                    lastEnd = matcher.end();
                }
                if (lastEnd > 0) {
                    append(fragment.substring(lastEnd), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                } else {
                    append(fragment, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                }
            }
        }
    }

    private static class FixAction extends AnAction {
        private final Project project;
        private final ProblemFix fix;

        public FixAction(Project project, ProblemFix fix) {
            super(fix.getName());
            this.project = project;
            this.fix = fix;
        }

        public void actionPerformed(AnActionEvent e) {
            fix.applyFix(project);
        }
    }
}

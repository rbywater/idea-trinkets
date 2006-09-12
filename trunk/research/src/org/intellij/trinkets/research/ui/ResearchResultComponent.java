package org.intellij.trinkets.research.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.util.Icons;
import com.intellij.util.ui.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.containers.Convertor;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.MessageFormat;

import org.intellij.trinkets.research.ResearchResultNode;
import org.intellij.trinkets.research.ResearchEngine;
import org.intellij.trinkets.research.ResearchResult;
import org.jetbrains.annotations.NonNls;

public class ResearchResultComponent extends JPanel implements ChangeListener, Disposable {
    private static final Pattern BOLD_PATTERN = Pattern.compile("<(b|strong)>(.*?)</\\1>");

    private ResearchResultModel model;
    private final TreeCellRenderer treeRenderer = new ColoredTreeCellRenderer() {
        public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) value;
            ResearchResultTreeNode treeNode = (ResearchResultTreeNode) mutableTreeNode.getUserObject();
            if (treeNode != null) {
                treeNode.render(this);
            }
        }
    };
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    private final Tree tree = new Tree(root);

    public ResearchResultComponent() {
        super(new BorderLayout());
        tree.setCellRenderer(treeRenderer);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        TreeToolTipHandler.install(tree);
        new TreeSpeedSearch(
            tree, new Convertor<TreePath, String>() {
            public String convert(TreePath o) {
                DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) o.getLastPathComponent();
                ResearchResultTreeNode treeNode = (ResearchResultTreeNode) mutableTreeNode.getUserObject();
                return treeNode.getPresentableName();
            }
        }
        );

        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                TreePath path = tree.getPathForLocation(point.x, point.y);
                if (path != null) {
                    Rectangle rectangle = tree.getPathBounds(path);
                    point.translate(-rectangle.x,  -rectangle.y);
                    DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    ResearchResultTreeNode treeNode = (ResearchResultTreeNode) mutableTreeNode.getUserObject();
                    treeNode.notifyMouseClicked(e);
                    tree.updateUI();
                } else {
                    setToolTipText(null);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        tree.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                Point point = e.getPoint();
                TreePath path = tree.getPathForLocation(point.x, point.y);
                if (path != null) {
                    Rectangle rectangle = tree.getPathBounds(path);
                    point.translate(-rectangle.x,  -rectangle.y);
                    DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    ResearchResultTreeNode treeNode = (ResearchResultTreeNode) mutableTreeNode.getUserObject();
                    treeNode.notifyMouseMoved(e);
                    tree.updateUI();
                } else {
                    setToolTipText(null);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        add(new JScrollPane(tree));
        setModel(new ResearchResultModel());
    }

    public ResearchResultModel getModel() {
        return model;
    }

    public void setModel(ResearchResultModel model) {
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
        Iterator<ResearchEngine> engines = model.engines();
        while (engines.hasNext()) {
            ResearchEngine engine = engines.next();
            DefaultMutableTreeNode engineRoot = new DefaultMutableTreeNode(new EngineTreeNode(engine), true);
            ResearchResult lastResult = engine.getLastResult();
            if (lastResult != null) {
                for (ResearchResultNode node : lastResult.getNodes()) {
                    engineRoot.add(new DefaultMutableTreeNode(new ResultTreeNode(node)));
                }
            }
            root.add(engineRoot);
        }
        tree.updateUI();
    }

    public ResearchEngine getSelectedEngine() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
            Object o = mutableTreeNode.getUserObject();
            if (o instanceof EngineTreeNode) {
                EngineTreeNode engineTreeNode = (EngineTreeNode) o;
                return engineTreeNode.engine;
            } else {
                EngineTreeNode engineTreeNode = (EngineTreeNode) ((DefaultMutableTreeNode)mutableTreeNode.getParent()).getUserObject();
                return engineTreeNode.engine;
            }
        }
        return null;
    }

    private final class EngineTreeNode implements ResearchResultTreeNode {
        @NonNls
        private static final String COUNT_PATTERN = "({0})";
        @NonNls
        private static final String RESEARCHING_STATUS = "(researching...)";

        private final ResearchEngine engine;

        public EngineTreeNode(ResearchEngine engine) {
            this.engine = engine;
        }

        public void notifyMouseClicked(MouseEvent e) {
        }

        public void notifyMouseMoved(MouseEvent e) {
        }

        public String getPresentableName() {
            return engine.getName();
        }

        public void render(ColoredTreeCellRenderer renderer) {
            renderer.setIcon(engine.getIcon());
            renderer.setIconTextGap(5);
            renderer.append(engine.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            renderer.append(" ", SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
            boolean searching = engine.isSearching();
            ResearchResult lastResult = engine.getLastResult();

            if (searching) {
                renderer.append(RESEARCHING_STATUS, SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            } else {
                if (lastResult != null) {
                    renderer.append(MessageFormat.format(COUNT_PATTERN, lastResult.getNodes().size()), SimpleTextAttributes.GRAY_ATTRIBUTES);
                }
            }
        }
    }

    private final class ResultTreeNode implements ResearchResultTreeNode {
        private final ResearchResultNode node;

        public ResultTreeNode(ResearchResultNode node) {
            this.node = node;
        }

        public void notifyMouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                BrowserUtil.launchBrowser(node.getURL());
            }
        }

        public void notifyMouseMoved(MouseEvent e) {
        }

        public String getPresentableName() {
            return node.getSubject();
        }

        public void render(ColoredTreeCellRenderer renderer) {
            renderer.setIcon(Icons.WEB_ICON);
            String fragment = node.getSubject();
            Matcher matcher = BOLD_PATTERN.matcher(fragment);
            int lastEnd = 0;
            while (matcher.find()) {
                String boldText = matcher.group(2);
                int start = matcher.start();
                renderer.append(fragment.substring(lastEnd, start), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                renderer.append(boldText, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                lastEnd = matcher.end();
            }
            if (lastEnd > 0) {
                renderer.append(fragment.substring(lastEnd), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            } else {
                renderer.append(fragment, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
            renderer.setToolTipText("Go to " + node.getURL());
        }
    }
}

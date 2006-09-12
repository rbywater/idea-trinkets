package org.intellij.trinkets.tabStack.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;
import org.intellij.trinkets.tabStack.TabStackManager;

import javax.swing.*;
import java.awt.*;

public class PopStackAction extends AnAction {
    private static final Icon ICON_SURFACE = IconLoader.getIcon("/general/stack.png");

    public void actionPerformed(AnActionEvent e) {
        Project project = (Project)e.getDataContext().getData(DataConstants.PROJECT);
        TabStackManager stackManager = TabStackManager.getInstance(project);
        stackManager.pop(true);
    }

    public void update(AnActionEvent e) {
        super.update(e);
        Project project = (Project)e.getDataContext().getData(DataConstants.PROJECT);
        if (project != null) {
            TabStackManager stackManager = TabStackManager.getInstance(project);
            e.getPresentation().setEnabled(stackManager.getStackSize() > 0);
            e.getPresentation().setIcon(new StackIcon(ICON_SURFACE, project));
        } else {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setIcon(null);
        }
    }

    private static class StackIcon implements Icon {
        private final Icon delegate;
        private final TabStackManager stackManager;
        private static final Color LABEL_COLOR = new Color(0x0f, 0x40, 0x86);

        public StackIcon(Icon delegate, Project project) {
            this.delegate = delegate;
            this.stackManager = TabStackManager.getInstance(project);
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            delegate.paintIcon(c, g, x, y);
            int count = stackManager.getStackSize();

            Font smallFont = getSmallFont();
            FontMetrics fontMetrics = g.getFontMetrics(smallFont);
            g.setFont(smallFont);
            g.setColor(LABEL_COLOR);
            String text = String.valueOf(count);
            g.drawString(text,
                x + 6 + (8 - fontMetrics.stringWidth(text)) / 2,
                y + 5 + fontMetrics.getAscent());
        }

        private static Font getSmallFont() {
            Font labelFont = UIUtil.getLabelFont();
            return labelFont.deriveFont(labelFont.getSize2D() - 3.0f);
        }

        public int getIconWidth() {
            return delegate.getIconWidth();
        }

        public int getIconHeight() {
            return delegate.getIconWidth();
        }
    }
}

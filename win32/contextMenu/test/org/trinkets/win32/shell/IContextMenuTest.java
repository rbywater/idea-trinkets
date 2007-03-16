package org.trinkets.win32.shell;

import org.trinkets.win32.shell.impl.IContextMenuImpl;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.MessageFormat;

/**
 * Test for {@link IContextMenu}.
 *
 * @author Alexey Efimov
 */
public class IContextMenuTest {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        System.loadLibrary("jawt");
        IContextMenu menu = new IContextMenuImpl(new String[]{
                new File("IContextMenu_JNI.dll").getAbsolutePath(),
                new File("IContextMenu_JNI.lib").getAbsolutePath()
        }
        );
        IContextMenuItem[] items = menu.getItems(panel, IContextMenuItem.EMPTY_ARRAY);

        for (IContextMenuItem item : items) {
            print(item, "", menu, panel);
        }
    }

    public static void print(IContextMenuItem item, String offset, IContextMenu cm, JPanel panel) {
        Icon icon = item.getIcon();
        int w = icon != null ? icon.getIconWidth() : 0;
        int h = icon != null ? icon.getIconHeight() : 0;
        JLabel comp = new JLabel(MessageFormat.format("{0}({1})", offset + item.getText(), item.getDescription()));
        comp.setIcon(icon);
        panel.add(comp);
        panel.doLayout();
        panel.repaint();
        if (icon != null) {
            System.out.println(MessageFormat.format("{0}: {1}({2}), image = {3} x {4}", item.getId(), offset + item.getText(), item.getDescription(), w, h));
        } else {
            System.out.println(MessageFormat.format("{0}: {1}({2})", item.getId(), offset + item.getText(), item.getDescription()));
        }
        if (item.getParent() != null && item.getParent().getId() == 6 && item.getId() == 1) {
            // Send to command
            System.out.println("\t\t* Execute this item...");
            cm.invokeItem(panel, item);
        }
        if (item.isSubMenu()) {
            IContextMenuItem[] items = cm.getItems(panel, item.getPath());
            for (IContextMenuItem child : items) {
                print(child, "\t" + offset, cm, panel);
            }
        }
    }
}

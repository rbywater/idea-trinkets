package org.intellij.trinkets.win32.contextMenu.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import org.intellij.trinkets.win32.contextMenu.Plugin;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.trinkets.util.LibraryLoader;
import org.trinkets.win32.shell.IContextMenu;
import org.trinkets.win32.shell.IContextMenuBuilder;
import org.trinkets.win32.shell.IContextMenuItem;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Windows explorer group.
 *
 * @author Alexey Efimov
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class IContextMenuGroup extends ActionGroup {
    static {
        Plugin.LOADER.registerNativeSupport("org.trinkets.win32.shell.impl.IContextMenuImpl");
    }

    IContextMenuBuilder BUILDER =
            (IContextMenuBuilder) Plugin.LOADER.newInstanceNativeSupport(
                    "org.trinkets.win32.shell.impl.IContextMenuBuilderImpl",
                    new Class[]{LibraryLoader.class}, new Object[]{Plugin.LOADER});

    @NonNls
    private static final String SEPARATOR_TEXT = "${separator}";

    @NonNls
    private static final String BITMAP_TEXT = "${bitmap}";

    @NonNls
    private static final String OWNER_TEXT = "${ownerdraw}";

    @NonNls
    private static final String UNKNOWN_TEXT = "${unknown}";

    private static String getItemText(IContextMenuItem item) {
        String s = item.getText();
        if (s == null || BITMAP_TEXT.equals(s) || OWNER_TEXT.equals(s)) {
            return "";
        }
        return s;
    }

    private static Icon getItemIcon(IContextMenuItem item) {
        Icon icon = item.getIcon();
        if (icon != null && getItemText(item).length() > 0) {
            // Item has text, cut off icon
            int height = icon.getIconHeight();
            if (height > 16) {
                height = 16;
            }

            //noinspection SuspiciousNameCombination
            BufferedImage image = new BufferedImage(height, height, BufferedImage.TYPE_INT_ARGB);
            image.getGraphics().drawImage(((ImageIcon) icon).getImage(), 0, 0, null);
            icon = new ImageIcon(image);
        }
        return icon;
    }

    public AnAction[] getChildren(@Nullable AnActionEvent event) {
        if (SystemInfo.isWindows && event != null) {
            Project project = event.getData(DataKeys.PROJECT);
            if (project != null) {
                JFrame frame = WindowManagerEx.getInstance().getFrame(project);
                VirtualFile[] files = event.getData(DataKeys.VIRTUAL_FILE_ARRAY);
                if (files != null && files.length > 0) {
                    String[] paths = new String[files.length];
                    for (int i = 0; i < files.length; i++) {
                        paths[i] = VfsUtil.virtualToIoFile(files[i]).getAbsolutePath();
                    }
                    List<AnAction> actions = new ArrayList<AnAction>();
                    IContextMenu menu = BUILDER.createContextMenu(paths);
                    IContextMenuItem[] items = menu.getItems(frame, IContextMenuItem.EMPTY_ARRAY);
                    for (IContextMenuItem item : items) {
                        String text = item.getText();
                        if (SEPARATOR_TEXT.equals(text)) {
                            actions.add(Separator.getInstance());
                        } else if (!UNKNOWN_TEXT.equals(text)) {
                            if (item.isSubMenu()) {
                                actions.add(new ContextMenuItemActionGroup(menu, item));
                            } else {
                                actions.add(new ContextMenuItemAction(menu, item));
                            }
                        }
                    }
                    return actions.toArray(AnAction.EMPTY_ARRAY);
                }
            }
        }
        return AnAction.EMPTY_ARRAY;
    }

    public void update(AnActionEvent e) {
        super.update(e);
        if (SystemInfo.isWindows) {
            e.getPresentation().setVisible(true);
            VirtualFile[] files = e.getData(DataKeys.VIRTUAL_FILE_ARRAY);
            e.getPresentation().setEnabled(files != null && files.length > 0);
        } else {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
        }
    }

    private static class ContextMenuItemAction extends AnAction {
        private final IContextMenu menu;
        private final IContextMenuItem item;

        public ContextMenuItemAction(IContextMenu menu, IContextMenuItem item) {
            super(getItemText(item), item.getDescription(), getItemIcon(item));
            this.menu = menu;
            this.item = item;
        }

        public void actionPerformed(AnActionEvent e) {
            Project project = e.getData(DataKeys.PROJECT);
            if (project != null) {
                JFrame frame = WindowManagerEx.getInstance().getFrame(project);
                menu.invokeItem(frame, item);
            }
        }
    }

    private static class ContextMenuItemActionGroup extends ActionGroup {
        private final IContextMenu menu;
        private final IContextMenuItem item;

        public ContextMenuItemActionGroup(@NotNull IContextMenu menu, @NotNull IContextMenuItem item) {
            super(getItemText(item), true);
            Presentation presentation = getTemplatePresentation();
            presentation.setIcon(getItemIcon(item));
            presentation.setDescription(item.getDescription());
            this.menu = menu;
            this.item = item;
        }

        public AnAction[] getChildren(@Nullable AnActionEvent event) {
            List<AnAction> actions = new ArrayList<AnAction>();
            if (event != null) {
                Project project = event.getData(DataKeys.PROJECT);
                if (project != null) {
                    JFrame frame = WindowManagerEx.getInstance().getFrame(project);
                    IContextMenuItem[] items = menu.getItems(frame, item.getPath());
                    for (IContextMenuItem item : items) {
                        String text = getItemText(item);
                        if (SEPARATOR_TEXT.equals(text)) {
                            actions.add(Separator.getInstance());
                        } else if (!UNKNOWN_TEXT.equals(text)) {
                            if (item.isSubMenu()) {
                                actions.add(new ContextMenuItemActionGroup(menu, item));
                            } else {
                                actions.add(new ContextMenuItemAction(menu, item));
                            }
                        }
                    }
                }
            }
            return actions.toArray(AnAction.EMPTY_ARRAY);
        }
    }
}

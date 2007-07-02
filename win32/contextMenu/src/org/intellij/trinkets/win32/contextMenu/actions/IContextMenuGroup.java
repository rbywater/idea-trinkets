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
import org.trinkets.win32.shell.IContextMenu;
import org.trinkets.win32.shell.IContextMenuItem;
import org.trinkets.win32.shell.IContextMenuManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Windows explorer group.
 *
 * @author Alexey Efimov
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class IContextMenuGroup extends ActionGroup {
    @NonNls
    private static final String SEPARATOR_TEXT = "${separator}";

    @NonNls
    private static final String BITMAP_TEXT = "${bitmap}";

    @NonNls
    private static final String OWNER_TEXT = "${ownerdraw}";

    @NonNls
    private static final String UNKNOWN_TEXT = "${unknown}";

    /**
     * IContextMenu manager
     */
    private final IContextMenuManager icmManager = new IContextMenuManager(Plugin.JNI_CACHE_DIR);

    private Reference<Project> lastProject;
    private Reference<VirtualFile[]> lastVirtualFiles;
    private AnAction[] actions = AnAction.EMPTY_ARRAY;

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

    public final AnAction[] getChildren(@Nullable AnActionEvent event) {
        if (event != null) {
            Project project = event.getData(DataKeys.PROJECT);
            VirtualFile[] files = event.getData(DataKeys.VIRTUAL_FILE_ARRAY);
            if (project != null && files != null) {
                set(project, files);
            }
        }
        return actions;
    }

    public final void update(AnActionEvent e) {
        super.update(e);
        if (SystemInfo.isWindows) {
            e.getPresentation().setVisible(true);
            Project project = e.getData(DataKeys.PROJECT);
            VirtualFile[] files = e.getData(DataKeys.VIRTUAL_FILE_ARRAY);
            e.getPresentation().setEnabled(project != null && files != null && files.length > 0);
        } else {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
        }
    }

    public void clear() {
        actions = AnAction.EMPTY_ARRAY;
        if (lastProject != null) {
            lastProject.clear();
        }
        if (lastVirtualFiles != null) {
            lastVirtualFiles.clear();
        }
    }

    public synchronized void set(@NotNull Project project, @NotNull VirtualFile[] files) {
        if (lastProject == null ||
                lastProject.get() == null ||
                !lastProject.get().equals(project) ||
                lastVirtualFiles == null ||
                lastVirtualFiles.get() == null ||
                !Arrays.equals(lastVirtualFiles.get(), files)) {
            clear();
            actions = createMenu(project, files);
            lastProject = new WeakReference<Project>(project);
            lastVirtualFiles = new WeakReference<VirtualFile[]>(files);
        }
    }

    private static final class ContextMenuItemAction extends AnAction {
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
                menu.invokeItem(null, item);
            }
        }
    }

    private static final class ContextMenuItemActionGroup extends ActionGroup {
        private final AnAction[] children;

        public ContextMenuItemActionGroup(Component frame, @NotNull IContextMenu menu, @NotNull IContextMenuItem item) {
            super(getItemText(item), true);
            Presentation presentation = getTemplatePresentation();
            presentation.setIcon(getItemIcon(item));
            presentation.setDescription(item.getDescription());
            List<AnAction> actions = new ArrayList<AnAction>();
            IContextMenuItem[] items = menu.getItems(frame, item.getPath());
            for (IContextMenuItem child : items) {
                String text = getItemText(child);
                if (SEPARATOR_TEXT.equals(text)) {
                    actions.add(Separator.getInstance());
                } else if (!UNKNOWN_TEXT.equals(text)) {
                    if (child.isSubMenu()) {
                        actions.add(new ContextMenuItemActionGroup(frame, menu, child));
                    } else {
                        actions.add(new ContextMenuItemAction(menu, child));
                    }
                }
            }
            this.children = actions.toArray(new AnAction[actions.size()]);
        }

        public AnAction[] getChildren(@Nullable AnActionEvent event) {
            return children;
        }
    }

    @NotNull
    private AnAction[] createMenu(@NotNull Project project, @NotNull VirtualFile[] files) {
        String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = VfsUtil.virtualToIoFile(files[i]).getAbsolutePath();
        }
        JFrame frame = WindowManagerEx.getInstance().getFrame(project);
        IContextMenu menu = icmManager.getMenu(paths);
        IContextMenuItem[] items = menu.getItems(frame, IContextMenuItem.EMPTY_ARRAY);
        List<AnAction> actions = new ArrayList<AnAction>(items.length);
        for (IContextMenuItem item : items) {
            String text = item.getText();
            if (SEPARATOR_TEXT.equals(text)) {
                actions.add(Separator.getInstance());
            } else if (!UNKNOWN_TEXT.equals(text)) {
                if (item.isSubMenu()) {
                    actions.add(new ContextMenuItemActionGroup(frame, menu, item));
                } else {
                    actions.add(new ContextMenuItemAction(menu, item));
                }
            }
        }
        return actions.toArray(new AnAction[actions.size()]);
    }

}

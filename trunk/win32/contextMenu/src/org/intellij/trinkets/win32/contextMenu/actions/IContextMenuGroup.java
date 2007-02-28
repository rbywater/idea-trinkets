package org.intellij.trinkets.win32.contextMenu.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.trinkets.win32.contextMenu.Plugin;
import org.jetbrains.annotations.NonNls;
import org.trinkets.util.LibraryLoader;
import org.trinkets.win32.shell.IContextMenu;
import org.trinkets.win32.shell.IContextMenuBuilder;
import org.trinkets.win32.shell.IContextMenuItem;

/**
 * Windows explorer group.
 *
 * @author Alexey Efimov
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class IContextMenuGroup extends DefaultActionGroup {
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
    private static final String IMAGE_TEXT = "${image}";

    public IContextMenuGroup() {
    }

    public IContextMenuGroup(String shortName, boolean popup) {
        super(shortName, popup);
    }

    public void update(AnActionEvent e) {
        super.update(e);
        if (SystemInfo.isWindows) {
            VirtualFile[] files = e.getData(DataKeys.VIRTUAL_FILE_ARRAY);
            boolean enabled = files != null && files.length == 1;
            e.getPresentation().setVisible(enabled);
            e.getPresentation().setEnabled(enabled);
            if (enabled) {
                VirtualFile file = files[0];
                removeAll();
                IContextMenu menu = BUILDER.createContextMenu(VfsUtil.virtualToIoFile(file).getAbsolutePath());
                IContextMenuItem[] items = menu.getItems();
                for (IContextMenuItem item : items) {
                    String text = item.getText();
                    if (SEPARATOR_TEXT.equals(text)) {
                        addSeparator();
                    } else if (!IMAGE_TEXT.equals(text)) {
                        if (item.getChildrenSize() > 0) {
                            add(new ContextMenuItemActionGroup(menu, item));
                        } else {
                            add(new ContextMenuItemAction(menu, item));
                        }
                    }
                }
            }
        } else {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
        }
    }

    private static class ContextMenuItemAction extends AnAction {
        private final IContextMenu menu;
        private final IContextMenuItem item;

        public ContextMenuItemAction(IContextMenu menu, IContextMenuItem item) {
            super(item.getText());
            this.menu = menu;
            this.item = item;
        }

        public void actionPerformed(AnActionEvent e) {
            menu.invokeItem(item);
        }
    }

    private static class ContextMenuItemActionGroup extends DefaultActionGroup {
        public ContextMenuItemActionGroup(IContextMenu menu, IContextMenuItem group) {
            super(group.getText(), true);
            for (IContextMenuItem item : group) {
                String text = item.getText();
                if (SEPARATOR_TEXT.equals(text)) {
                    addSeparator();
                } else if (!IMAGE_TEXT.equals(text)) {
                    if (item.getChildrenSize() > 0) {
                        add(new ContextMenuItemActionGroup(menu, item));
                    } else {
                        add(new ContextMenuItemAction(menu, item));
                    }
                }
            }
        }
    }
}

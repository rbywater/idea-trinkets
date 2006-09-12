package org.intellij.trinkets.editorTree.view.actionSystem;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import org.intellij.trinkets.editorTree.view.EditorTreeView;

/**
 * This class for actions utils.
 *
 * @author Alexey Efimov
 */
public final class EditorTreeViewActionsUtil {
    private EditorTreeViewActionsUtil() {
    }

    public static void installShortcuts(EditorTreeView view) {
        // Install action stortcuts
        ActionGroup action = (ActionGroup)ActionManager.getInstance().getAction(EditorTreeViewActions.POPUP_GROUP);
        AnAction[] children = action.getChildren(null);
        for (AnAction anAction : children) {
            anAction.registerCustomShortcutSet(anAction.getShortcutSet(), view.getComponent());
        }
    }

    public static void uninstallShortcuts(EditorTreeView view) {
        // Install action stortcuts
        ActionGroup action = (ActionGroup)ActionManager.getInstance().getAction(EditorTreeViewActions.POPUP_GROUP);
        AnAction[] children = action.getChildren(null);
        for (AnAction anAction : children) {
            anAction.unregisterCustomShortcutSet(view.getComponent());
        }
    }
}

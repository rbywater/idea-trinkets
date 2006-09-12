package org.intellij.trinkets.hyperLink.actions;

import org.jetbrains.annotations.NotNull;

/**
 * Hyper link action.
 *
 * @author Alexey Efimov
 */
public interface HyperLinkAction {
    void performAction(@NotNull HyperLinkEvent event);
}

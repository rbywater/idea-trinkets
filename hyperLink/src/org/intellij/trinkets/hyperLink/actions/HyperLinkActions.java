package org.intellij.trinkets.hyperLink.actions;

import com.intellij.ide.BrowserUtil;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

/**
 * Standard actions.
 *
 * @author Alexey Efimov
 */
public interface HyperLinkActions {
    /**
     * Open url by {@link BrowserUtil}.
     */
    HyperLinkAction URL_ACTION = new HyperLinkAction() {
        public void performAction(@NotNull HyperLinkEvent event) {
            BrowserUtil.launchBrowser(event.getReferenceText());
        }
    };

    /**
     * Open email with adding mailto: before mathed text and launch url by
     * {@link BrowserUtil}.
     */
    HyperLinkAction EMAIL_ACTION = new HyperLinkAction() {
        @SuppressWarnings({"HardCodedStringLiteral"})
        public void performAction(@NotNull HyperLinkEvent event) {
            BrowserUtil.launchBrowser(MessageFormat.format("mailto:{0}", event.getReferenceText()));
        }
    };
}

package org.intellij.trinkets.hyperLink;

import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.intellij.trinkets.hyperLink.actions.HyperLinkAction;

import java.util.regex.Pattern;

/**
 * Hyper link reference.
 *
 * @author Alexey Efimov
 */
public interface HyperLinkReference {
    @NotNull
    @NonNls
    String getName();

    @NotNull
    @NonNls
    Pattern getSearchPattern();

    @NotNull
    @NonNls
    String getReplacePattern();

    @NotNull
    TextAttributes getTextAttributes();

    @NotNull
    HyperLinkAction getAction();
}

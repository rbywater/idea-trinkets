package org.intellij.trinkets.hyperLink;

import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.hyperLink.actions.HyperLinkAction;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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
    Pattern getSearchPattern(Project project);

    @NotNull
    @NonNls
    String getReplacePattern();

    @NotNull
    TextAttributes getTextAttributes();

    @NotNull
    HyperLinkAction getAction();
}

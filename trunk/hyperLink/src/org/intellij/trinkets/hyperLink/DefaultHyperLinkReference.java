package org.intellij.trinkets.hyperLink;

import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.intellij.trinkets.hyperLink.actions.HyperLinkAction;

import java.util.regex.Pattern;

public class DefaultHyperLinkReference implements HyperLinkReference {
    private final String name;
    private final Pattern searchPattern;
    private final String replacePattern;
    private final TextAttributes textAttributes;
    private final HyperLinkAction action;

    public DefaultHyperLinkReference(
        @NotNull @NonNls String name,
        @NotNull @NonNls String searchPattern,
        @NotNull @NonNls String replacePattern,
        @NotNull TextAttributes textAttributes,
        @NotNull HyperLinkAction action
    ) {
        this.name = name;
        this.searchPattern = Pattern.compile(searchPattern);
        this.replacePattern = replacePattern;
        this.textAttributes = textAttributes;
        this.action = action;
    }

    @NotNull
    @NonNls
    public String getName() {
        return name;
    }

    @NotNull
    @NonNls
    public Pattern getSearchPattern() {
        return searchPattern;
    }

    @NotNull
    @NonNls
    public String getReplacePattern() {
        return replacePattern;
    }

    @NotNull
    public TextAttributes getTextAttributes() {
        return textAttributes;
    }

    @NotNull
    public HyperLinkAction getAction() {
        return action;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultHyperLinkReference) {
            DefaultHyperLinkReference other = (DefaultHyperLinkReference)obj;
            return name.equals(other.name) && searchPattern.equals(other.searchPattern);

        }
        return false;

    }

    public int hashCode() {
        return name.hashCode();
    }
}

package org.intellij.trinkets.hyperLink;

import org.intellij.trinkets.hyperLink.actions.HyperLinkActions;
import org.intellij.trinkets.hyperLink.markup.HyperLinkColors;

public interface HyperLinkReferences {
    HyperLinkReference URL = new DefaultHyperLinkReference(
        "url",
        "\\b((https?|file|ftp)://[^\\s<>\\\"']+)\\b",
        "$1",
        HyperLinkColors.REFERENCE,
        HyperLinkActions.URL_ACTION
    );
    HyperLinkReference MAILTO = new DefaultHyperLinkReference(
        "mailto",
        "\\bmailto:([\\w_-]+(\\.[\\w_-]+)*@[\\w_-]+(\\.[\\w_-]+)*\\.[A-Za-z]{2,4})\\b",
        "$1",
        HyperLinkColors.REFERENCE,
        HyperLinkActions.EMAIL_ACTION
    );
    HyperLinkReference EMAIL = new DefaultHyperLinkReference(
        "email",
        "\\b([\\w_-]+(\\.[\\w_-]+)*@[\\w_-]+(\\.[\\w_-]+)*\\.[A-Za-z]{2,4})\\b",
        "$1",
        HyperLinkColors.REFERENCE,
        HyperLinkActions.EMAIL_ACTION
    );
}

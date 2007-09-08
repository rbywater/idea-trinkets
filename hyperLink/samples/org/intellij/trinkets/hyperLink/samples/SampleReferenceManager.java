package org.intellij.trinkets.hyperLink.samples;

import com.intellij.openapi.components.ApplicationComponent;
import org.intellij.trinkets.hyperLink.DefaultHyperLinkReference;
import org.intellij.trinkets.hyperLink.HyperLinkReferenceManager;
import org.intellij.trinkets.hyperLink.actions.HyperLinkActions;
import org.intellij.trinkets.hyperLink.markup.HyperLinkColors;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * This reference is register JetBrains JIRA link highlighting.
 * Links like IDEABKL-XXXX, IDEADEV-XXX, IDEA-XXXX will translated
 * to http://www.jetbrains.net/jira/browse/$1.
 *
 * @author Alexey Efimov
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class SampleReferenceManager implements ApplicationComponent {
    private static final DefaultHyperLinkReference IDEA_JIRA_REFERENCE = new DefaultHyperLinkReference(
            "JetBrains.JIRA.IDEA",
            "\\b(IDEA(DEV|BKL)?-\\d+)\\b",
            "http://www.jetbrains.net/jira/browse/$1",
            HyperLinkColors.REFERENCE,
            HyperLinkActions.URL_ACTION
    );

    @NotNull
    @NonNls
    public String getComponentName() {
        return "JetBrains JIRA Reference Manager";
    }

    public void initComponent() {
        HyperLinkReferenceManager referenceManager = HyperLinkReferenceManager.getInstance();
        referenceManager.registerReference(IDEA_JIRA_REFERENCE);
    }

    public void disposeComponent() {
        HyperLinkReferenceManager referenceManager = HyperLinkReferenceManager.getInstance();
        referenceManager.unregisterReference(IDEA_JIRA_REFERENCE);
    }
}

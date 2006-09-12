package org.intellij.trinkets.hyperLink;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

/**
 * Reference manager.
 *
 * @author Alexey Efimov
 */
public abstract class HyperLinkReferenceManager {
    public static HyperLinkReferenceManager getInstance() {
        return ApplicationManager.getApplication().getComponent(HyperLinkReferenceManager.class);
    }

    @NotNull
    public abstract HyperLinkReference[] getReferences();

    public abstract void registerReference(@NotNull HyperLinkReference reference);

    public abstract void unregisterReference(@NotNull HyperLinkReference reference);

    public final HyperLinkReference getReference(String name) {
        HyperLinkReference[] references = getReferences();
        for (HyperLinkReference reference : references) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        return null;
    }
}

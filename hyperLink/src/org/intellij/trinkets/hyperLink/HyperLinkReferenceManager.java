package org.intellij.trinkets.hyperLink;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference manager.
 *
 * @author Alexey Efimov
 */
public class HyperLinkReferenceManager implements ApplicationComponent {
    private final List<HyperLinkReference> patterns = new ArrayList<HyperLinkReference>();

    public static HyperLinkReferenceManager getInstance() {
        return ApplicationManager.getApplication().getComponent(HyperLinkReferenceManager.class);
    }

    @NotNull
    public HyperLinkReference[] getReferences() {
        return patterns.toArray(new HyperLinkReference[patterns.size()]);
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    public void registerReference(@NotNull HyperLinkReference reference) {
        int index = patterns.indexOf(reference);
        if (index != -1) {
            // Replace reference
            patterns.set(index, reference);
        } else {
            patterns.add(reference);
        }
    }

    public void unregisterReference(@NotNull HyperLinkReference reference) {
        patterns.remove(reference);
    }

    public final HyperLinkReference getReference(String name) {
        HyperLinkReference[] references = getReferences();
        for (HyperLinkReference reference : references) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        return null;
    }

    @NotNull
    @NonNls
    public String getComponentName() {
        return "HyperLinkReferenceManager";
    }

    public void initComponent() {
        // Register default patterns
        registerReference(HyperLinkReferences.URL);
        registerReference(HyperLinkReferences.MAILTO);
        registerReference(HyperLinkReferences.EMAIL);
    }

    public void disposeComponent() {
        // Unregister default patterns
        unregisterReference(HyperLinkReferences.EMAIL);
        unregisterReference(HyperLinkReferences.MAILTO);
        unregisterReference(HyperLinkReferences.URL);
    }

    @NonNls
    public String getExternalFileName() {
        return "hyperlink.patterns";
    }
}

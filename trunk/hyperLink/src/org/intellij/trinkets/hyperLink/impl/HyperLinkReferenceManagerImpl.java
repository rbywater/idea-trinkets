package org.intellij.trinkets.hyperLink.impl;

import com.intellij.openapi.components.ApplicationComponent;
import org.intellij.trinkets.hyperLink.DefaultHyperLinkReference;
import org.intellij.trinkets.hyperLink.HyperLinkReference;
import org.intellij.trinkets.hyperLink.HyperLinkReferenceManager;
import org.intellij.trinkets.hyperLink.HyperLinkReferences;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class HyperLinkReferenceManagerImpl extends HyperLinkReferenceManager implements ApplicationComponent {
    private static final DefaultHyperLinkReference[] EMPTY_HYPERLINK_PATTER_ARRAY = new DefaultHyperLinkReference[]{};

    private List<HyperLinkReference> patterns = new ArrayList<HyperLinkReference>();

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

    @NotNull
    public HyperLinkReference[] getReferences() {
        return patterns.toArray(EMPTY_HYPERLINK_PATTER_ARRAY);
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

}

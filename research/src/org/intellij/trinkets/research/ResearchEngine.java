package org.intellij.trinkets.research;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

/**
 * Research engine.
 *
 * @author Alexey Efimov
 */
public interface ResearchEngine {
    String getName();

    Icon getIcon();

    ResearchResult getLastResult();

    String getLastResearchWord();

    String getLastLocation();

    boolean isSearching();

    @NotNull
    ResearchResult research(String researchWord) throws IOException;

    void reset();
}

package org.intellij.trinkets.research;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Alexey Efimov
 */
public interface ResearchResult {
    @NotNull
    List<ResearchResultNode> getNodes();
}

package org.intellij.trinkets.research.engines;

import org.jetbrains.annotations.NotNull;
import org.intellij.trinkets.research.ResearchResult;
import org.intellij.trinkets.research.ResearchResultNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

final class DefaultResearchResult implements ResearchResult {
    private final List<ResearchResultNode> nodes = new ArrayList<ResearchResultNode>();

    public DefaultResearchResult(Map<String, String> urlToSubject) {
        Set<String> urls = urlToSubject.keySet();
        for (String url : urls) {
            nodes.add(new DefaultResearchResultNode(url, urlToSubject.get(url)));
        }
    }

    @NotNull
    public List<ResearchResultNode> getNodes() {
        return nodes;
    }
}

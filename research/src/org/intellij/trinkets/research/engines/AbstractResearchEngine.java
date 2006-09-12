package org.intellij.trinkets.research.engines;

import org.intellij.trinkets.research.ResearchEngine;
import org.intellij.trinkets.research.ResearchResult;

import javax.swing.*;

/**
 * Default research engine.
 *
 * @author Alexey Efimov
 */
public abstract class AbstractResearchEngine implements ResearchEngine {
    protected final String name;
    protected final Icon icon;
    private boolean searching;
    private ResearchResult result;

    protected AbstractResearchEngine(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    public final String getName() {
        return name;
    }

    public final Icon getIcon() {
        return icon;
    }


    protected final synchronized void setSearching(boolean searching) {
        this.searching = searching;
    }

    protected final synchronized void setResult(ResearchResult result) {
        this.result = result;
    }

    public final ResearchResult getLastResult() {
        return result;
    }

    public final boolean isSearching() {
        return searching;
    }

    public void reset() {
        result = null;
    }
}

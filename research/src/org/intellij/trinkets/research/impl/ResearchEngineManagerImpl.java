package org.intellij.trinkets.research.impl;

import org.intellij.trinkets.research.ResearchEngineManager;
import org.intellij.trinkets.research.ResearchEngine;
import org.intellij.trinkets.research.engines.GoogleResearchEngine;
import org.intellij.trinkets.research.engines.JExamplesResearchEngine;
import org.intellij.trinkets.research.icons.ResearchIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.components.ApplicationComponent;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

final class ResearchEngineManagerImpl extends ResearchEngineManager implements ApplicationComponent {
    private static final ResearchEngine[] EMPTY_RESEARCH_ENGINE_ARRAY = {};
    private static final ResearchEngine[] PREDEFINED_ENGINES = {
            new GoogleResearchEngine("JavaAlmanac", ResearchIcons.JAVA_ALMANAC_ENGINE, "javaalmanac.com"),
            new GoogleResearchEngine("Java2s", ResearchIcons.JAVA_ALMANAC_ENGINE, "www.java2s.com/Code/Java/"),
            new JExamplesResearchEngine("JExamples", ResearchIcons.JEXAMPLES_ENGINE),
            new GoogleResearchEngine("Google", ResearchIcons.GOOGLE_ENGINE, null),
    };
    private final List<ResearchEngine> engines = new ArrayList<ResearchEngine>(0);

    @NonNls
    @NotNull
    public String getComponentName() {
        return "ResearchEngineManager";
    }

    public void initComponent() {
        engines.addAll(Arrays.asList(PREDEFINED_ENGINES));
    }

    public void disposeComponent() {
        engines.removeAll(Arrays.asList(PREDEFINED_ENGINES));
    }

    public ResearchEngine[] getEngines() {
        return engines.toArray(EMPTY_RESEARCH_ENGINE_ARRAY);
    }

    public void addEngine(ResearchEngine engine) {
        engines.add(engine);
    }

    public void removeEngine(ResearchEngine engine) {
        engines.remove(engine);
    }
}

package org.intellij.trinkets.research;

import com.intellij.openapi.application.ApplicationManager;

/**
 * Research engine manager.
 *
 * @author Alexey Efimov
 */
public abstract class ResearchEngineManager {
    public static ResearchEngineManager getInstance() {
        return ApplicationManager.getApplication().getComponent(ResearchEngineManager.class);
    }

    public abstract ResearchEngine[] getEngines();

    public abstract void addEngine(ResearchEngine engine);

    public abstract void removeEngine(ResearchEngine engine);
}

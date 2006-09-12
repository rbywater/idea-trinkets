package org.intellij.trinkets.research;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.Project;

/**
 * Reserch manager to perform researching.
 *
 * @author Alexey Efimov
 */
public abstract class ResearchManager {
    public static ResearchManager getInstance(Project project) {
        return project.getComponent(ResearchManager.class);
    }

    public abstract void research(String word);

    public abstract ResearchEngine setSelectedEngine();
}

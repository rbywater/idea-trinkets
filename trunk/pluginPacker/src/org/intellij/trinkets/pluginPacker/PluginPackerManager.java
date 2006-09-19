package org.intellij.trinkets.pluginPacker;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Manager for plugin packer plugin
 *
 * @author Alexey Efimov
 */
public abstract class PluginPackerManager {
    public static PluginPackerManager getInstance(Project project) {
        return project.getComponent(PluginPackerManager.class);
    }

    public abstract boolean packModule(@NotNull Module module, @NotNull String packagePattern, String sourcesPattern, boolean includeSources, @NotNull String outputDirectory);

    @NotNull
    public abstract Project getProject();
}

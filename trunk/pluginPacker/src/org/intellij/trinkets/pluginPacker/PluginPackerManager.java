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

    /**
     * Pack module.
     *
     * @param module          Module
     * @param packagePattern  Patern for binary package
     * @param sourcesPattern  Pattern for sources package. If <code>null</code> sources will not packed.
     * @param inboxSources    If <code>true</code> sources will packed together with binary package - the result was only one ZIP. Otherwise, two separate ZIPs was builded - binary and sources ZIPs.
     * @param outputDirectory Output directory where ZIP was builded
     * @return <code>true</code> if packing is successful
     */
    public abstract boolean packModule(@NotNull Module module,
                                       @NotNull String packagePattern,
                                       String sourcesPattern,
                                       boolean inboxSources,
                                       @NotNull String outputDirectory);

    @NotNull
    public abstract Project getProject();
}

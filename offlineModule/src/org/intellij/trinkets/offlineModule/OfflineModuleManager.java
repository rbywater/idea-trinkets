package org.intellij.trinkets.offlineModule;

import com.intellij.openapi.components.ProjectComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Offline module manager hold currently disabled modules and can show modules in Project View.
 *
 * @author Alexey Efimov
 */
public class OfflineModuleManager implements ProjectComponent {
    public void projectOpened() {

    }

    public void projectClosed() {

    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "OfflineModuleManager";
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }
}

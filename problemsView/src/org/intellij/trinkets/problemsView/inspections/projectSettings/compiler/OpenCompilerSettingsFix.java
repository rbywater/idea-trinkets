package org.intellij.trinkets.problemsView.inspections.projectSettings.compiler;

import com.intellij.compiler.options.CompilerConfigurable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.problemsView.problems.ConfigurableFix;
import org.jetbrains.annotations.NotNull;

/**
 * Open compiler settings.
 *
 * @author Alexey Efimov
 */
final class OpenCompilerSettingsFix extends ConfigurableFix {
    public OpenCompilerSettingsFix() {
        super("Show Compiler settings");
    }

    @NotNull
    protected Configurable getConfigurable(@NotNull Application application, @NotNull Project project) {
        return project.getComponent(CompilerConfigurable.class);
    }
}

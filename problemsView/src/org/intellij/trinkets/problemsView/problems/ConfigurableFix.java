package org.intellij.trinkets.problemsView.problems;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Open configurable to fix.
 *
 * @author Alexey Efimov
 */
public abstract class ConfigurableFix implements ProblemFix {
    private final String name;

    public ConfigurableFix(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    protected abstract Configurable getConfigurable(@NotNull Application application, @NotNull Project project);

    @NotNull
    public String getName() {
        return name;
    }

    public void applyFix(@NotNull final Project project) {
        // Show settings
        final Application application = ApplicationManager.getApplication();
        application.invokeLater(new Runnable() {
            public void run() {
                ShowSettingsUtil.getInstance().editConfigurable(project, getConfigurable(application, project));
            }
        });
    }
}

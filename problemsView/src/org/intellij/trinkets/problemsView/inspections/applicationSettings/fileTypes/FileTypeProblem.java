package org.intellij.trinkets.problemsView.inspections.applicationSettings.fileTypes;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.impl.FileTypeConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.intellij.trinkets.problemsView.problems.AbstractProblem;
import org.intellij.trinkets.problemsView.problems.ConfigurableFix;
import org.intellij.trinkets.problemsView.problems.ProblemFix;
import org.intellij.trinkets.problemsView.problems.ProblemType;
import org.jetbrains.annotations.NotNull;

/**
 * Check file types to have extensions.
 *
 * @author Alexey Efimov
 */
public class FileTypeProblem extends AbstractProblem {
    private final static ProblemFix[] FIXES = new ProblemFix[]{new ShowSettingsFix()};

    private final FileType type;

    public FileTypeProblem(FileType type) {
        this.type = type;
    }

    @NotNull
    public ProblemType getType() {
        return ProblemType.WARNING;
    }

    @NotNull
    public ProblemFix[] getFixes() {
        return FIXES;
    }

    @NotNull
    public String getNodeText() {
        return "File type <b>" + type.getName() + "</b> have no registered patterns";
    }

    private static class ShowSettingsFix extends ConfigurableFix {
        public ShowSettingsFix() {
            super("Show File Types settings");
        }

        @NotNull
        protected Configurable getConfigurable(@NotNull Application application, @NotNull Project project) {
            return application.getComponent(FileTypeConfigurable.class);
        }
    }
}

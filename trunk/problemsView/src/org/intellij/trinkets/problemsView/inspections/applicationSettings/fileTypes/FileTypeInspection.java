package org.intellij.trinkets.problemsView.inspections.applicationSettings.fileTypes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.ex.FakeFileType;
import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Inspection for file types.
 *
 * @author Alexey Efimov
 */
public class FileTypeInspection implements ProblemInspection {
    @NotNull
    public Problem[] inspect() {
        List<Problem> problems = new ArrayList<Problem>(0);
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        FileType[] fileTypes = fileTypeManager.getRegisteredFileTypes();
        for (FileType type : fileTypes) {
            if (!(type instanceof FakeFileType) &&
                    fileTypeManager.getAssociations(type).isEmpty()) {
                problems.add(new FileTypeProblem(type));
            }
        }
        return problems.toArray(Problem.EMPTY_PROBLEM_ARRAY);
    }
}

package org.intellij.trinkets.problemsView.inspections.projectSettings.compiler;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Inspection to check Compiler pattern settings.
 * Some patterns may be forgoten to include into pattern.
 *
 * @author Alexey Efimov
 */
public class ResourcesPatternInspection implements ProblemInspection {
    private final Project project;

    public ResourcesPatternInspection(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    public Problem[] inspect() {
        Problem problem = inspectSettings();
        if (problem != null) {
            return new Problem[]{problem};
        }
        return Problem.EMPTY_PROBLEM_ARRAY;
    }

    private ResourcesPatternProblem inspectSettings() {
        ResourcesPatternProblem problem = null;
        List<String> missed = new ArrayList<String>(0);
        CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(project);
        List<String> patterns = Arrays.asList(compilerConfiguration.getWildcardPatterns());
        // Scan project for used file types
        FileTypeManager typeManager = FileTypeManager.getInstance();
        String[] foundPatterns = scanFileTypes(typeManager);
        for (String string : foundPatterns) {
            String pattern = '?' + string;
            if (!patterns.contains(pattern)) {
                missed.add(pattern);
            }
        }
        if (missed.size() > 0) {
            problem = new ResourcesPatternProblem(missed.toArray(ArrayUtil.EMPTY_STRING_ARRAY));
        }
        return problem;
    }

    private String[] scanFileTypes(FileTypeManager typeManager) {
        Set<String> patterns = new HashSet<String>();
        CompilerManager compilerManager = CompilerManager.getInstance(project);
        ProjectRootManager instance = ProjectRootManager.getInstance(project);
        VirtualFile[] sourceRoots = instance.getContentSourceRoots();
        for (VirtualFile root : sourceRoots) {
            scanFileTypes(typeManager, compilerManager, root, patterns);
        }
        return patterns.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
    }

    private static void scanFileTypes(FileTypeManager typeManager, CompilerManager compilerManager, VirtualFile root, Set<String> patterns) {
        VirtualFile[] files = root.getChildren();
        for (VirtualFile file : files) {
            if (file.isDirectory()) {
                scanFileTypes(typeManager, compilerManager, file, patterns);
            } else {
                FileType type = file.getFileType();
                if (!compilerManager.isCompilableFileType(type) &&
                        (!(type instanceof LanguageFileType) || type == StdFileTypes.PROPERTIES) &&
                        type != StdFileTypes.GUI_DESIGNER_FORM &&
                        type != StdFileTypes.IDEA_MODULE &&
                        type != StdFileTypes.IDEA_PROJECT &&
                        type != StdFileTypes.IDEA_WORKSPACE
                        ) {
                    List<FileNameMatcher> matchers = typeManager.getAssociations(type);
                    for (FileNameMatcher matcher : matchers) {
                        if (matcher.accept(file.getName())) {
                            patterns.add(matcher.getPresentableString());
                            break;
                        }
                    }
                }
            }
        }
    }
}

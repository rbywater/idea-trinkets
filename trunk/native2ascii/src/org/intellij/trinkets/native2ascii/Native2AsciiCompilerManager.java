package org.intellij.trinkets.native2ascii;

import com.intellij.openapi.compiler.Compiler;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileTypes.StdFileTypes;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Native2Ascii compiler.
 *
 * @author Alexey Efimov
 */
public final class Native2AsciiCompilerManager implements ProjectComponent {
    private final CompilerManager compilerManager;
    private final Compiler compiler;

    public Native2AsciiCompilerManager(Project project, CompilerManager compilerManager) {
        this.compilerManager = compilerManager;
        this.compiler = new Native2AsciiCompiler(project);
    }

    @NotNull
    @NonNls
    public String getComponentName() {
        return "Native2AsciiCompilerManager";
    }

    public void projectOpened() {
        compilerManager.addCompiler(compiler);
        compilerManager.addCompilableFileType(StdFileTypes.PROPERTIES);
    }

    public void projectClosed() {
        compilerManager.removeCompilableFileType(StdFileTypes.PROPERTIES);
        compilerManager.removeCompiler(compiler);
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}

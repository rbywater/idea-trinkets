package org.intellij.trinkets.native2ascii;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.compiler.make.MakeUtil;
import com.intellij.compiler.options.CompilerConfigurable;
import com.intellij.lang.properties.charset.Native2AsciiCharset;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.impl.FileTypeConfigurable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.trinkets.native2ascii.util.CompilerFileUtil;
import org.intellij.trinkets.native2ascii.util.Native2AsciiBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;

/**
 * Compile native to ascii for properties files.
 *
 * @author Alexey Efimov
 */
public final class Native2AsciiCompiler implements TranslatingCompiler {
    private final Project project;

    public Native2AsciiCompiler(Project project) {
        this.project = project;
    }

    public boolean isCompilableFile(VirtualFile file, CompileContext context) {
        return StdFileTypes.PROPERTIES.equals(FileTypeManager.getInstance().getFileTypeByFile(file));
    }

    private boolean validateFileTypeConfiguration() {
        // Check that properties file type is have registered extensions
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        List<FileNameMatcher> associatedExtensions = fileTypeManager.getAssociations(StdFileTypes.PROPERTIES);
        if (associatedExtensions.size() == 0) {
            // Idea have no extensions assotiated with properties
            Messages.showErrorDialog(project, Native2AsciiBundle.message("error.file.types.message", StdFileTypes.PROPERTIES.getDescription()), Native2AsciiBundle.message("error.file.types.title"));
            // Show settings
            final Application application = ApplicationManager.getApplication();
            application.invokeLater(new Runnable() {
                public void run() {
                    FileTypeConfigurable ftConfigurable = application.getComponent(FileTypeConfigurable.class);
                    ShowSettingsUtil.getInstance().editConfigurable(project, ftConfigurable);
                }
            });
            return false;
        }
        return true;
    }

    private boolean validateCompilerCollisions(VirtualFile[] files) {
        if (files != null) {
            CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(project);
            for (VirtualFile file : files) {
                if (compilerConfiguration.isResourceFile(file.getName())) {
                    // Idea have no extensions assotiated with properties
                    Messages.showErrorDialog(project, Native2AsciiBundle.message("error.collision.message"), Native2AsciiBundle.message("error.collision.title"));
                    // Show settings
                    final Application application = ApplicationManager.getApplication();
                    application.invokeLater(new Runnable() {
                        public void run() {
                            CompilerConfigurable cConfigurable = project.getComponent(CompilerConfigurable.class);
                            ShowSettingsUtil.getInstance().editConfigurable(project, cConfigurable);
                        }
                    });
                    return false;
                }
            }
        }
        return true;
    }

    public ExitStatus compile(CompileContext context, VirtualFile[] files) {
        Application application = ApplicationManager.getApplication();
        Result result = application.runReadAction(new CompileAction(context, files));

        if (result.isNeedUpdateCaches()) {
            context.getProgressIndicator().pushState();
            context.getProgressIndicator().setText(Native2AsciiBundle.message("progress.updating.caches"));
            CompilerUtil.refreshIOFiles(result.getTranslatedFiles());
            context.getProgressIndicator().popState();
        }

        return new ExitStatusImpl(result.getOutputItems());
    }

    @NonNls
    @NotNull
    public String getDescription() {
        return "native2ascii";
    }

    public boolean validateConfiguration(CompileScope scope) {
        return validateFileTypeConfiguration() && validateCompilerCollisions(scope.getFiles(StdFileTypes.PROPERTIES, false));
    }

    private static final class ExitStatusImpl implements ExitStatus {
        private final OutputItem[] items;

        public ExitStatusImpl(OutputItem[] items) {
            this.items = items;
        }

        public OutputItem[] getSuccessfullyCompiled() {
            return items;
        }

        public VirtualFile[] getFilesToRecompile() {
            return VirtualFile.EMPTY_ARRAY;
        }
    }

    private static final class OutputItemImpl implements OutputItem {
        private final VirtualFile source;
        private final String outputFilePath;
        private final String outputRoot;

        public OutputItemImpl(VirtualFile source, String outputFilePath, String outputRootPath) {
            this.source = source;
            this.outputFilePath = outputFilePath;
            this.outputRoot = outputRootPath;
        }

        public String getOutputPath() {
            return outputFilePath;
        }

        public VirtualFile getSourceFile() {
            return source;
        }

        public String getOutputRootDirectory() {
            return outputRoot;
        }
    }

    private static final class Result {
        private final List<OutputItem> outputItems = new ArrayList<OutputItem>(0);
        private final List<File> translatedFiles = new ArrayList<File>(0);

        public void addOutputItem(OutputItem item) {
            outputItems.add(item);
        }

        public void addTranslatedFile(File file) {
            if (file != null) {
                translatedFiles.add(file);
            }
        }

        public OutputItem[] getOutputItems() {
            return outputItems.toArray(EMPTY_OUTPUT_ITEM_ARRAY);
        }

        public boolean isNeedUpdateCaches() {
            return translatedFiles.size() > 0;
        }

        @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
        public List<File> getTranslatedFiles() {
            return translatedFiles;
        }
    }

    /**
     * Compiler action.
     */
    private final class CompileAction implements Computable<Result> {
        private final CompileContext context;
        private final VirtualFile[] files;

        public CompileAction(CompileContext context, VirtualFile[] files) {
            this.context = context;
            this.files = files;
        }

        public Result compute() {
            return convert(new ParseAction(context, files).compute());
        }

        private Result convert(Map<Module, List<Command>> commandMap) {
            Result result = new Result();

            ProgressIndicator progress = context.getProgressIndicator();
            progress.pushState();
            for (Module module : commandMap.keySet()) {
                List<Command> commands = commandMap.get(module);
                progress.setText2(Native2AsciiBundle.message("progress.status", commands.size(), module.getName()));

                // Sort
                progress.setText(Native2AsciiBundle.message("progress.sorting"));
                Collections.sort(commands);

                for (int i = 0; i < commands.size() && !progress.isCanceled(); i++) {
                    Command command = commands.get(i);
                    try {
                        progress.setText(Native2AsciiBundle.message("progress.native2ascii", command.getPath()));
                        progress.setFraction((double) i / (double) commands.size());

                        OutputItem outputItem = command.getOutputItem();
                        File file = command.convert();
                        if (file != null) {
                            result.addTranslatedFile(file);
                        }
                        result.addOutputItem(outputItem);
                    } catch (IOException e) {
                        context.addMessage(CompilerMessageCategory.ERROR, e.getLocalizedMessage(), command.getUrl(), -1, -1);
                    }
                }
            }

            progress.popState();
            return result;
        }
    }

    /**
     * Parse source files. This action only collect information for conversion.
     */
    private final class ParseAction implements Computable<Map<Module, List<Command>>> {
        private final CompileContext context;
        private final VirtualFile[] files;

        public ParseAction(CompileContext context, VirtualFile[] files) {
            this.context = context;
            this.files = files;
        }

        public Map<Module, List<Command>> compute() {
            Map<Module, List<Command>> map = new HashMap<Module, List<Command>>(0);
            ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
            ProgressIndicator progress = context.getProgressIndicator();
            progress.pushState();
            for (int i = 0; i < files.length && !progress.isCanceled(); i++) {
                VirtualFile file = files[i];
                Module module = context.getModuleByFile(file);
                if (module != null) {
                    List<Command> commands = map.get(module);
                    if (commands == null) {
                        commands = new ArrayList<Command>(0);
                        map.put(module, commands);
                    }

                    progress.setText2(Native2AsciiBundle.message("progress.status", commands.size(), module.getName()));
                    progress.setText(Native2AsciiBundle.message("progress.parsing", file.getName()));
                    progress.setFraction((double) i / (double) files.length);

                    OutputItem outputItem = getOutputItem(fileIndex, module, file);
                    if (outputItem != null) {
                        commands.add(new Command(outputItem));
                    }
                }
            }
            progress.popState();
            return map;
        }

        @Nullable
        private OutputItem getOutputItem(@NotNull ProjectFileIndex fileIndex, @NotNull Module module, @NotNull VirtualFile source) {
            VirtualFile sourceRoot = MakeUtil.getSourceRoot(context, module, source);
            if (sourceRoot != null) {
                boolean isTestContent = fileIndex.isInTestSourceContent(source);
                String outRoot = CompilerPaths.getModuleOutputPath(module, isTestContent);
                if (outRoot != null) {
                    String targetPath = CompilerFileUtil.getOutputFilePath(fileIndex, source, sourceRoot, outRoot);
                    return new OutputItemImpl(source, targetPath, outRoot);
                }
            }
            return null;
        }
    }

    /**
     * Command to perform native2ascii conversion
     */
    private static final class Command implements Comparable<Command> {
        private final OutputItem outputItem;

        private final String url;
        private final String path;

        public Command(OutputItem outputItem) {
            this.outputItem = outputItem;
            VirtualFile source = outputItem.getSourceFile();
            this.url = source.getUrl();
            String outputRoot = outputItem.getOutputRootDirectory();
            String outputPath = outputItem.getOutputPath();
            this.path = outputPath.startsWith(outputRoot) && outputPath.length() > outputRoot.length() ? outputPath.substring(outputRoot.length() + 1) : outputPath;
        }

        public OutputItem getOutputItem() {
            return outputItem;
        }

        public String getPath() {
            return path;
        }

        public String getUrl() {
            return url;
        }

        public File convert() throws IOException {
            VirtualFile sourceFile = outputItem.getSourceFile();
            String outputPath = outputItem.getOutputPath();
            if (!sourceFile.getPath().equals(outputPath)) {
                // Encode
                File outputFile = new File(FileUtil.toSystemDependentName(outputPath));
                Charset charset = sourceFile.getCharset();
                String ntaName = Native2AsciiCharset.makeNative2AsciiEncodingName(charset.name());
                CharsetEncoder charsetEncoder = Native2AsciiCharset.forName(ntaName).newEncoder();
                ByteBuffer inputBuf = ByteBuffer.wrap(sourceFile.contentsToByteArray());
                ByteBuffer byteBuffer = charsetEncoder.encode(charset.decode(inputBuf));
                if (!outputFile.exists()) {
                    // Create file
                    outputFile.getParentFile().mkdirs();
                    outputFile.createNewFile();
                }
                CompilerFileUtil.writeFile(outputFile, byteBuffer);
                return outputFile;
            }
            return null;
        }

        public int compareTo(Command o) {
            return o != null ? path.compareTo(o.getPath()) : 1;
        }
    }
}

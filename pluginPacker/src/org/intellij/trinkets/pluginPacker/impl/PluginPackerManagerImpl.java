package org.intellij.trinkets.pluginPacker.impl;

import com.intellij.javaee.make.ManifestBuilder;
import com.intellij.javaee.make.ModuleBuildProperties;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import com.intellij.util.io.ZipUtil;
import org.intellij.trinkets.pluginPacker.PluginPackerManager;
import org.intellij.trinkets.pluginPacker.util.PluginXmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.build.PluginBuildUtil;
import org.jetbrains.idea.devkit.build.PluginModuleBuildProperties;

import java.io.*;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class PluginPackerManagerImpl extends PluginPackerManager implements ProjectComponent {
    @NonNls
    private static final String JAR_EXTENSION = ".jar";
    @NonNls
    private static final String ZIP_EXTENSION = ".zip";
    @NonNls
    private static final String TEMP_PREFIX = "temp";

    private static final Pattern PATTERN = Pattern.compile("\\$\\{([\\w\\.\\-\\_]+)\\}");

    private final Project project;

    public PluginPackerManagerImpl(Project project) {
        this.project = project;
    }

    @NotNull
    public Project getProject() {
        return project;
    }

    public boolean packModule(@NotNull final Module module,
                              @NotNull String packagePattern,
                              String sourcesPattern,
                              final boolean inboxSources,
                              @NotNull String outputDirectory) {
        Map<String, String> variables = new HashMap<String, String>();
        final String pluginId = StringUtil.decapitalize(PluginXmlUtil.getPluginId(module));
        variables.put("plugin.id", pluginId);
        variables.put("plugin.version", PluginXmlUtil.getPluginVersion(module));

        String packageName = replaceVariables(packagePattern, variables);
        final String sourcesName = sourcesPattern != null ? replaceVariables(sourcesPattern, variables) : null;

        try {
            final File binZipBuffer = createTempFile(ZIP_EXTENSION);
            final File srcZipBuffer = sourcesName != null && !inboxSources ? createTempFile(ZIP_EXTENSION) : null;

            final File outputBinFile = new File(outputDirectory, packageName);
            if (!confirmFileOverwriting(outputBinFile)) {
                return false;
            }
            final File outputSrcFile = srcZipBuffer != null ? new File(outputDirectory, sourcesName) : null;
            if (outputSrcFile != null && !confirmFileOverwriting(outputSrcFile)) {
                return false;
            }

            // Errors container
            final Set<String> errorSet = new HashSet<String>();

            // Dependencies
            final Set<Module> modules = new HashSet<Module>();
            PluginBuildUtil.getDependencies(module, modules);
            modules.add(module);
            final Set<Library> libs = new HashSet<Library>();
            for (Module module1 : modules) {
                PluginBuildUtil.getLibraries(module1, libs);
            }

            boolean isOk = ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
                public void run() {
                    final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
                    try {
                        // Build binary package 
                        if (progressIndicator != null) {
                            progressIndicator.setText("Preparing binary package...");
                            progressIndicator.setIndeterminate(true);
                        }
                        File jarFile = createTempPluginJar(module, modules);
                        File srcFile = null;
                        if (srcZipBuffer == null && sourcesName != null) {
                            if (progressIndicator != null) {
                                progressIndicator.setText("Preparing sources package...");
                                progressIndicator.setIndeterminate(true);
                            }
                            srcFile = createTempSourcesZip(modules);
                        }
                        if (progressIndicator != null) {
                            progressIndicator.setText("Building binary package " + outputBinFile.getName() + "...");
                            progressIndicator.setIndeterminate(true);
                        }
                        processLibraries(pluginId, jarFile, srcFile, sourcesName, binZipBuffer, libs, progressIndicator);
                        if (srcZipBuffer != null) {
                            if (progressIndicator != null) {
                                progressIndicator.setText("Building sources package " + outputSrcFile.getName() + "...");
                                progressIndicator.setIndeterminate(true);
                            }
                            zipSources(srcZipBuffer, modules);
                        }
                    }
                    catch (final IOException e1) {
                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                            public void run() {
                                errorSet.add("error");
                                Messages.showErrorDialog(e1.getMessage(), "Plugin packing error");
                            }
                        }, ModalityState.NON_MODAL);
                    }
                }
            }, "Preparing plugin pack...", true, module.getProject());

            if (isOk && errorSet.isEmpty()) {
                FileUtil.copy(binZipBuffer, outputBinFile);
                if (srcZipBuffer != null) {
                    FileUtil.copy(srcZipBuffer, outputSrcFile);
                    WindowManager.getInstance().getStatusBar(project).setInfo(MessageFormat.format("Files {0} and {1} saved successful", outputBinFile.getName(), outputSrcFile.getName()));
                } else {
                    WindowManager.getInstance().getStatusBar(project).setInfo(MessageFormat.format("File {0} saved successful", outputBinFile.getName()));
                }
                return true;
            }
        } catch (final IOException e) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    Messages.showErrorDialog(e.getMessage(), "Plugin packing error");
                }
            }, ModalityState.NON_MODAL);
        }
        return false;
    }

    private static File createTempFile(String extension) throws IOException {
        File tempFile = FileUtil.createTempFile(TEMP_PREFIX, extension);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private boolean confirmFileOverwriting(@NotNull File file) {
        if (file.exists()) {
            if (Messages.showYesNoDialog(project, "File already exist.\nDelete old file " + file.getPath() + "?", "Plugin packing",
                    Messages.getInformationIcon()) == DialogWrapper.OK_EXIT_CODE) {
                FileUtil.delete(file);
            } else {
                return false;
            }
        }
        return true;
    }

    private static String replaceVariables(String pattern, Map<String, String> variables) {
        Matcher matcher = PATTERN.matcher(pattern);
        while (matcher.find()) {
            String value = variables.get(matcher.group(1));
            if (StringUtil.isNotEmpty(value)) {
                int start = matcher.start();
                int end = matcher.end();
                pattern = pattern.substring(0, start) + value + pattern.substring(end);
                matcher.reset(pattern);
            }
        }
        return pattern;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "PluginPackerManager";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    private final FileTypeManager myFileTypeManager = FileTypeManager.getInstance();

    private void processLibraries(
            String name,
            final File jarFile,
            final File srcZipFile,
            final String sourcesName,
            final File zipFile,
            final Set<Library> libs,
            final ProgressIndicator progressIndicator
    ) throws IOException {
        if (zipFile.exists() || zipFile.createNewFile()) {
            ZipOutputStream zos = null;
            try {
                zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
                addStructure(name, zos);
                addStructure(name + "/" + "lib", zos);
                ZipUtil.addFileToZip(zos, jarFile, "/" + name + "/lib/" + name + JAR_EXTENSION, new HashSet<String>(), new FileFilter() {
                    public boolean accept(File pathname) {
                        if (progressIndicator != null) {
                            progressIndicator.setText2("");
                        }
                        return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getPath()));
                    }
                });
                if (srcZipFile != null) {
                    addStructure(name + "/" + "src", zos);
                    ZipUtil.addFileToZip(zos, srcZipFile, name + "/src/" + sourcesName, new HashSet<String>(), new FileFilter() {
                        public boolean accept(File pathname) {
                            if (progressIndicator != null) {
                                progressIndicator.setText2("Adding sources...");
                            }
                            return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getPath()));
                        }
                    });
                }
                Set<String> names = new HashSet<String>();
                for (Library library : libs) {
                    final VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
                    Set<File> excludedRoots = new HashSet<File>();
                    excludedRoots.add(new File(PathManager.getLibPath()));
                    excludedRoots.add(new File(PathManager.getPluginsPath()));
                    excludedRoots.add(new File(PathManager.getPreinstalledPluginsPath()));
                    for (VirtualFile virtualFile : files) {
                        boolean excluded = false;
                        for (File excludedFile : excludedRoots) {
                            excluded |= VfsUtil.isAncestor(excludedFile, VfsUtil.virtualToIoFile(virtualFile), false);
                        }

                        if (!excluded) {
                            if (virtualFile.getFileSystem() instanceof JarFileSystem) {
                                addLibraryJar(virtualFile, zipFile, name, zos, progressIndicator);
                            } else {
                                makeAndAddLibraryJar(virtualFile, zos, zipFile, name, library, names, progressIndicator);
                            }
                        }
                    }
                }
            }
            finally {
                if (zos != null) {
                    zos.close();
                }
            }
        }
    }

    private void makeAndAddLibraryJar(final VirtualFile virtualFile,
                                      final ZipOutputStream zos,
                                      final File zipFile,
                                      final String name,
                                      final Library library, final Set<String> names, final ProgressIndicator progressIndicator
    ) throws IOException {
        File libraryJar = createTempFile(JAR_EXTENSION);
        ZipOutputStream jar = null;
        try {
            jar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(libraryJar)));
            ZipUtil.addFileOrDirRecursively(jar, libraryJar, VfsUtil.virtualToIoFile(virtualFile), "", new FileFilter() {
                public boolean accept(File pathname) {
                    if (progressIndicator != null) {
                        progressIndicator.setText2("Adding " + pathname + "...");
                    }
                    return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getPath()));
                }
            }, new HashSet<String>());
        }
        finally {
            if (jar != null) {
                jar.close();
            }
        }
        ZipUtil.addFileOrDirRecursively(zos, zipFile, libraryJar,
                "/" + name + "/lib/" + getLibraryJarName(library, names, virtualFile) + JAR_EXTENSION,
                new FileFilter() {
                    public boolean accept(File pathname) {
                        if (progressIndicator != null) {
                            progressIndicator.setText2("");
                        }
                        return true;
                    }
                }, new HashSet<String>());
    }

    private String getLibraryJarName(Library library, Set<String> names, final VirtualFile virtualFile) {
        final String name = library.getName();
        if (name != null && !names.contains(name)) {
            return name;
        }
        String libraryName = virtualFile.getName();
        if (names.contains(libraryName)) {
            int i = 1;
            while (true) {
                if (!names.contains(libraryName + i)) {
                    libraryName += i;
                    break;
                }
                i++;
            }
        }
        names.add(libraryName);
        return libraryName;
    }

    private void addLibraryJar(final VirtualFile virtualFile,
                               final File zipFile,
                               final String name,
                               final ZipOutputStream zos,
                               final ProgressIndicator progressIndicator
    ) throws IOException {
        File ioFile = VfsUtil.virtualToIoFile(virtualFile);
        final FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                if (progressIndicator != null) {
                    progressIndicator.setText2("");
                }
                return true;
            }
        };
        ZipUtil.addFileOrDirRecursively(zos, zipFile, ioFile, "/" + name + "/lib/" + ioFile.getName(), filter, null);
    }

    private void addStructure(@NonNls final String relativePath, final ZipOutputStream zos) throws IOException {
        ZipEntry e = new ZipEntry(relativePath + "/");
        e.setMethod(ZipEntry.STORED);
        e.setSize(0);
        e.setCrc(0);
        zos.putNextEntry(e);
        zos.closeEntry();
    }

    private File createTempPluginJar(Module module, final Set<Module> modules) throws IOException {
        File jarFile = createTempFile(JAR_EXTENSION);
        jarPluginModule(jarFile, module, modules);
        return jarFile;
    }

    private void jarPluginModule(File jarFile, Module module, Set<Module> dependencies) throws IOException {
        final PluginModuleBuildProperties pluginModuleBuildProperties =
                ((PluginModuleBuildProperties) ModuleBuildProperties.getInstance(module));
        final Manifest manifest = createOrFindManifest(pluginModuleBuildProperties);
        ZipOutputStream jarPlugin = null;
        try {
            jarPlugin = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jarFile)), manifest);
            final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
            final HashSet<String> writtenItemRelativePaths = new HashSet<String>();
            for (Module dependence : dependencies) {
                final VirtualFile compilerOutputPath = ModuleRootManager.getInstance(dependence).getCompilerOutputPath();
                if (compilerOutputPath == null) {
                    continue; //pre-condition: output dirs for all modules are up-to-date
                }
                ZipUtil.addDirToZipRecursively(jarPlugin, jarFile, new File(compilerOutputPath.getPath()), "", new FileFilter() {
                    public boolean accept(File pathname) {
                        if (progressIndicator != null) {
                            progressIndicator.setText2("");
                        }
                        return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getName()));
                    }
                }, writtenItemRelativePaths);
            }
            final String pluginXmlPath = pluginModuleBuildProperties.getPluginXmlPath();
            @NonNls final String metainf = "/META-INF/plugin.xml";
            ZipUtil.addFileToZip(jarPlugin,
                    new File(pluginXmlPath),
                    metainf,
                    writtenItemRelativePaths,
                    new FileFilter() {
                        public boolean accept(File pathname) {
                            if (progressIndicator != null) {
                                progressIndicator.setText2("");
                            }
                            return true;
                        }
                    });

        }
        finally {
            if (jarPlugin != null) {
                jarPlugin.close();
            }
        }
    }

    private Manifest createOrFindManifest(final PluginModuleBuildProperties pluginModuleBuildProperties) throws IOException {
        final Manifest manifest = new Manifest();
        final VirtualFile vManifest = pluginModuleBuildProperties.getManifest();
        if (pluginModuleBuildProperties.isUseUserManifest() && vManifest != null) {
            InputStream in = null;
            try {
                in = new BufferedInputStream(vManifest.getInputStream());
                manifest.read(in);
            }
            finally {
                if (in != null) {
                    in.close();
                }
            }
        } else {
            Attributes mainAttributes = manifest.getMainAttributes();
            ManifestBuilder.setGlobalAttributes(mainAttributes);
        }
        return manifest;
    }

    private File createTempSourcesZip(final Set<Module> modules) throws IOException {
        File zipFile = createTempFile(ZIP_EXTENSION);
        zipSources(zipFile, modules);
        return zipFile;
    }

    private void zipSources(File zipFile, Set<Module> modules) throws IOException {
        ZipOutputStream zip = null;
        try {
            zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
            final HashSet<String> writtenItemRelativePaths = new HashSet<String>();
            for (Module dependence : modules) {
                final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(dependence);
                final VirtualFile[] roots = moduleRootManager.getContentRoots();
                for (VirtualFile root : roots) {
                    ZipUtil.addDirToZipRecursively(zip, zipFile, new File(root.getPath()), "", new FileFilter() {
                        public boolean accept(File pathname) {
                            if (progressIndicator != null) {
                                progressIndicator.setText2("");
                            }
                            VirtualFile[] files = moduleRootManager.getExcludeRoots();
                            for (VirtualFile excluded : files) {
                                if (VfsUtil.isAncestor(VfsUtil.virtualToIoFile(excluded), pathname, false)) {
                                    return false;
                                }
                            }
                            return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getName()));
                        }
                    }, writtenItemRelativePaths);
                }
            }
        }
        finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

}

package org.intellij.trinkets.problemsView.inspections.projectSettings.libraries;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectRootConfigurable;
import org.intellij.trinkets.problemsView.inspections.ProblemInspection;
import org.intellij.trinkets.problemsView.problems.Problem;
import org.intellij.trinkets.problemsView.util.FindFileUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Library inspection.
 *
 * @author Alexey Efimov
 */
public class LibraryInspection implements ProblemInspection {
    private static final String APPLICAION_SERVER_LIBRARY_ROOT = "Applicaion server library";
    private static final String PROJECT_LIBRARY_ROOT = "Project library";
    private static final String GLOBAL_LIBRARY_ROOT = "Global library";
    private static final String MODULE_LIBRARY_ROOT = "Module library";
    @NonNls
    private static final String APPLICATION_SERVER_LIBRARIES = "application_server_libraries";

    private final Project project;

    public LibraryInspection(Project project) {
        this.project = project;
    }

    public static void getLibraries(Module module, Set<Library> libs) {
        OrderEntry[] orderEntries = ModuleRootManager.getInstance(module).getOrderEntries();
        for (OrderEntry orderEntry : orderEntries) {
            if (orderEntry instanceof LibraryOrderEntry) {
                LibraryOrderEntry libEntry = (LibraryOrderEntry) orderEntry;
                Library lib = libEntry.getLibrary();
                if (lib != null) {
                    libs.add(lib);
                }
            }
        }
    }

    @NotNull
    public Problem[] inspect() {
        List<Problem> problems = new ArrayList<Problem>();
        LibraryTablesRegistrar libraryTablesRegistrar = LibraryTablesRegistrar.getInstance();
        checkLibraryTable(GLOBAL_LIBRARY_ROOT, libraryTablesRegistrar.getLibraryTable(), problems);
        checkLibraryTable(PROJECT_LIBRARY_ROOT, libraryTablesRegistrar.getLibraryTable(project), problems);
        checkLibraryTable(APPLICAION_SERVER_LIBRARY_ROOT, libraryTablesRegistrar.getLibraryTableByLevel(APPLICATION_SERVER_LIBRARIES, project), problems);

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getModules();
        for (Module module : modules) {
            checkModuleLibraries(module, problems);
        }

        return problems.toArray(Problem.EMPTY_PROBLEM_ARRAY);
    }

    private static void checkLibraryTable(String rootName, LibraryTable table, List<Problem> problems) {
        Library[] libraries = table.getLibraries();
        for (Library library : libraries) {
            checkLibrary(rootName, library.getName(), library, problems);
        }
    }

    private static void checkModuleLibraries(Module module, List<Problem> problems) {
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        OrderEntry[] orderEntries = moduleRootManager.getOrderEntries();
        for (OrderEntry entry : orderEntries) {
            if (entry instanceof LibraryOrderEntry) {
                LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) entry;
                Library library = libraryOrderEntry.getLibrary();
                if (library != null) {
                    String libName = ProjectRootConfigurable.trancateModuleLibraryName(libraryOrderEntry);
                    checkLibrary(MODULE_LIBRARY_ROOT, libName, library, problems);
                }
            }
        }
    }

    private static void checkLibrary(String rootName, String libraryName, Library library, List<Problem> problems) {
        boolean wrongClasses = checkLibraryRoot(library, OrderRootType.CLASSES);
        boolean wrongSources = checkLibraryRoot(library, OrderRootType.SOURCES);
        boolean wrongJavaDoc = checkLibraryRoot(library, OrderRootType.JAVADOC);
        if (wrongClasses || wrongSources || wrongJavaDoc) {
            List<OrderRootType> types = new ArrayList<OrderRootType>(3);
            if (wrongClasses) {
                types.add(OrderRootType.CLASSES);
            }
            if (wrongSources) {
                types.add(OrderRootType.SOURCES);
            }
            if (wrongJavaDoc) {
                types.add(OrderRootType.JAVADOC);
            }
            problems.add(new LibraryProblem(rootName, libraryName, types));
        }
    }

    private static boolean checkLibraryRoot(Library library, OrderRootType rootType) {
        String[] urls = library.getUrls(rootType);
        for (String url : urls) {
            if (!FindFileUtil.isValidUrl(url)) {
                return true;
            }
        }
        return false;
    }
}

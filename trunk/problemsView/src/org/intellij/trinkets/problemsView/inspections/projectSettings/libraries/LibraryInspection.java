package org.intellij.trinkets.problemsView.inspections.projectSettings.libraries;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
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

/**
 * Library inspection.
 *
 * @author Alexey Efimov
 */
public class LibraryInspection implements ProblemInspection {
    private final Project project;
    @NonNls
    private static final String APPLICATION_SERVER_LIBRARIES = "application_server_libraries";

    public LibraryInspection(Project project) {
        this.project = project;
    }

    @NotNull
    public Problem[] inspect() {
        List<Problem> problems = new ArrayList<Problem>();
        LibraryTablesRegistrar libraryTablesRegistrar = LibraryTablesRegistrar.getInstance();
        checkTable(libraryTablesRegistrar.getLibraryTable(), problems, "Global library");
        checkTable(libraryTablesRegistrar.getLibraryTable(project), problems, "Project library");
        checkTable(libraryTablesRegistrar.getLibraryTableByLevel(APPLICATION_SERVER_LIBRARIES, project), problems, "Applicaion server library");

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getModules();
        for (Module module : modules) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            ModifiableRootModel model = moduleRootManager.getModifiableModel();
            LibraryTable table = model.getModuleLibraryTable();
            Library[] libraries = table.getLibraries();
            for (Library library : libraries) {
                List<OrderRootType> types = new ArrayList<OrderRootType>(0);
                if (check(library, OrderRootType.CLASSES)) {
                    types.add(OrderRootType.CLASSES);
                }
                if (check(library, OrderRootType.SOURCES)) {
                    types.add(OrderRootType.SOURCES);
                }
                if (check(library, OrderRootType.JAVADOC)) {
                    types.add(OrderRootType.JAVADOC);
                }
                if (types.size() > 0) {
                    LibraryOrderEntry libraryOrderEntry = model.findLibraryOrderEntry(library);
                    if (libraryOrderEntry != null) {
                        problems.add(new LibraryProblem("Module library", ProjectRootConfigurable.trancateModuleLibraryName(libraryOrderEntry), types));
                    }
                }
            }
        }

        return problems.toArray(Problem.EMPTY_PROBLEM_ARRAY);
    }

    private static void checkTable(LibraryTable table, List<Problem> problems, String rootName) {
        Library[] libraries = table.getLibraries();
        for (Library library : libraries) {
            List<OrderRootType> types = new ArrayList<OrderRootType>(0);
            if (check(library, OrderRootType.CLASSES)) {
                types.add(OrderRootType.CLASSES);
            }
            if (check(library, OrderRootType.SOURCES)) {
                types.add(OrderRootType.SOURCES);
            }
            if (check(library, OrderRootType.JAVADOC)) {
                types.add(OrderRootType.JAVADOC);
            }
            if (types.size() > 0) {
                problems.add(new LibraryProblem(rootName, library.getName(), types));
            }
        }
    }

    private static boolean check(Library library, OrderRootType rootType) {
        String[] urls = library.getUrls(rootType);
        for (String url : urls) {
            if (!FindFileUtil.isValidUrl(url)) {
                return true;
            }
        }
        return false;
    }
}

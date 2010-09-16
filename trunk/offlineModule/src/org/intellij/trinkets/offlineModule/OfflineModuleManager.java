package org.intellij.trinkets.offlineModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.trinkets.offlineModule.util.OfflineModuleBundle;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Offline module manager hold currently disabled modules and can show modules in Project View.
 *
 * @author Alexey Efimov
 */
public final class OfflineModuleManager implements ProjectComponent, JDOMExternalizable {
    private final List<Pair<String, String[]>> disabledModules = new ArrayList<Pair<String, String[]>>();
    private final Project project;

    public static OfflineModuleManager getInstance(Project project) {
        return project.getComponent(OfflineModuleManager.class);
    }

    public static List<Pair<String, String[]>> getDisabledModules(Project project) {
        return getInstance(project).disabledModules;
    }

    public OfflineModuleManager(Project project) {
        this.project = project;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "OfflineModuleManager";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public void disableModule(final Module module) {
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                String[] groupPath = moduleManager.getModuleGroupPath(module);
                String filePath = module.getModuleFilePath();
                Iterator<Pair<String, String[]>> iterator = disabledModules.iterator();
                while (iterator.hasNext()) {
                    Pair<String, String[]> mp = iterator.next();
                    String first = mp.getFirst();
                    if (filePath.equals(first)) {
                        iterator.remove();
                    }
                }
                disabledModules.add(new Pair<String, String[]>(filePath, groupPath));
                moduleManager.disposeModule(module);
            }
        });
    }

    public void enableModule(final VirtualFile file) {
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    String path = file.getPath();
                    ModifiableModuleModel model = moduleManager.getModifiableModel();
                    Module module = model.loadModule(path);
                    model.commit();
                    Iterator<Pair<String, String[]>> it = disabledModules.iterator();
                    while (it.hasNext()) {
                        Pair<String, String[]> pair = it.next();
                        if (path.equals(pair.getFirst())) {
                            it.remove();
                            String[] groupPath = pair.getSecond();
                            if (groupPath != null && groupPath.length > 0) {
                                model.setModuleGroupPath(module, groupPath);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Messages.showErrorDialog(project, ex.getLocalizedMessage(), OfflineModuleBundle.message("can.t.load.module"));
                }
            }
        });
    }

    public void readExternal(Element element) throws InvalidDataException {
        List list = element.getChildren("module");
        if (list != null) {
            for (Object e : list) {
                Element module = (Element) e;
                String path = module.getAttributeValue("path");
                String[] group = null;
                List children = module.getChildren("group");
                if (children != null) {
                    List<String> groupList = new ArrayList<String>();
                    for (Object ge : children) {
                        Element groupPath = (Element) ge;
                        String value = groupPath.getAttributeValue("path");
                        if (value != null && !StringUtil.isEmptyOrSpaces(value)) {
                            groupList.add(value);
                        }
                    }
                    group = groupList.toArray(new String[groupList.size()]);
                }
                disabledModules.add(new Pair<String, String[]>(path, group));
            }
        }
    }

    public void writeExternal(Element element) throws WriteExternalException {
        for (Pair<String, String[]> pair : disabledModules) {
            Element module = new Element("module");
            module.setAttribute("path", pair.getFirst());
            String[] group = pair.getSecond();
            if (group != null) {
                for (String path : group) {
                    Element groupPath = new Element("group");
                    groupPath.setAttribute("path", path);
                    module.addContent(groupPath);
                }
            }
            element.addContent(module);
        }
    }
}

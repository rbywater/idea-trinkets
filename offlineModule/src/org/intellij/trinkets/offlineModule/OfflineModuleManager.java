package org.intellij.trinkets.offlineModule;

import com.intellij.ide.projectView.*;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.Icons;
import com.intellij.util.ui.EmptyIcon;
import org.intellij.trinkets.offlineModule.util.OfflineModuleBundle;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Offline module manager hold currently disabled modules and can show modules in Project View.
 *
 * @author Alexey Efimov
 */
public final class OfflineModuleManager implements ProjectComponent, JDOMExternalizable, TreeStructureProvider {
    private static final Icon ICON_MODULE_GROUP_OPEN = IconLoader.findIcon("/nodes/moduleGroupOpen.png");
    private static final Icon ICON_MODULE_GROUP_CLOSED = IconLoader.findIcon("/nodes/moduleGroupClosed.png");
    private static final Icon ICON_DISABLED_MODULE_GROUP_OPEN = layerIcons(ICON_MODULE_GROUP_OPEN, Icons.EXCLUDED_FROM_COMPILE_ICON);
    private static final Icon ICON_DISABLED_MODULE_GROUP_CLOSED = layerIcons(ICON_MODULE_GROUP_CLOSED, Icons.EXCLUDED_FROM_COMPILE_ICON);
    @NonNls
    private static final String JDOM_ATTR_PATH = "path";
    @NonNls
    private static final String JDOM_NODE_GROUP = "group";
    @NonNls
    private static final String JDOM_NODE_MODULE = "module";

    private static Icon layerIcons(Icon... layers) {
        if (layers != null && layers.length > 0) {
            LayeredIcon layeredIcon = new LayeredIcon(layers.length);
            for (int i = 0; i < layers.length; i++) {
                layeredIcon.setIcon(layers[i], i);
            }
            return layeredIcon;
        }
        return new EmptyIcon(0, 0);
    }

    public final List<Pair<String, String[]>> MODULES = new ArrayList<Pair<String, String[]>>();
    private final Project project;

    public static OfflineModuleManager getInstance(Project project) {
        return project.getComponent(OfflineModuleManager.class);
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
                Iterator<Pair<String, String[]>> iterator = MODULES.iterator();
                while (iterator.hasNext()) {
                    Pair<String, String[]> mp = iterator.next();
                    String first = mp.getFirst();
                    if (filePath.equals(first)) {
                        iterator.remove();
                    }
                }
                MODULES.add(new Pair<String, String[]>(filePath, groupPath));
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
                    Iterator<Pair<String, String[]>> it = MODULES.iterator();
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
        List list = element.getChildren(JDOM_NODE_MODULE);
        if (list != null) {
            for (Object e : list) {
                Element module = (Element) e;
                String path = module.getAttributeValue(JDOM_ATTR_PATH);
                String[] group = null;
                List children = module.getChildren(JDOM_NODE_GROUP);
                if (children != null) {
                    List<String> groupList = new ArrayList<String>();
                    for (Object ge : children) {
                        Element groupPath = (Element) ge;
                        String value = groupPath.getAttributeValue(JDOM_ATTR_PATH);
                        if (value != null && !StringUtil.isEmptyOrSpaces(value)) {
                            groupList.add(value);
                        }
                    }
                    group = groupList.toArray(new String[groupList.size()]);
                }
                MODULES.add(new Pair<String, String[]>(path, group));
            }
        }
    }

    public void writeExternal(Element element) throws WriteExternalException {
        for (Pair<String, String[]> pair : MODULES) {
            Element module = new Element(JDOM_NODE_MODULE);
            module.setAttribute(JDOM_ATTR_PATH, pair.getFirst());
            String[] group = pair.getSecond();
            if (group != null) {
                for (String path : group) {
                    Element groupPath = new Element(JDOM_NODE_GROUP);
                    groupPath.setAttribute(JDOM_ATTR_PATH, path);
                    module.addContent(groupPath);
                }
            }
            element.addContent(module);
        }
    }

    public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings) {
        if (settings.isShowModules()) {
            if (parent.getValue() instanceof Project && MODULES.size() > 0) {
                // Show here disabled modules
                ProjectViewNode<String> viewNode = new DisabledModulesNode();
                children.add(viewNode);
            }
        }
        return children;
    }

    @Nullable
    public Object getData(Collection<AbstractTreeNode> selected, String dataName) {
        return null;
    }

    private class DisabledModulesNode extends ProjectViewNode<String> {
        public DisabledModulesNode() {
            super(OfflineModuleManager.this.project, OfflineModuleBundle.message("disabled.modules"), ViewSettings.DEFAULT);
        }

        public boolean contains(@NotNull VirtualFile file) {
            for (Pair<String, String[]> pair : MODULES) {
                if (file.getPath().equals(pair.getFirst())) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        public Collection<? extends AbstractTreeNode> getChildren() {
            Collection<PsiFileNode> children = new ArrayList<PsiFileNode>();
            PsiManager psiManager = PsiManager.getInstance(project);
            for (Pair<String, String[]> pair : MODULES) {
                VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(pair.getFirst());
                if (vf != null) {
                    PsiFile psiFile = psiManager.findFile(vf);
                    if (psiFile != null) {
                        PsiFileNode node = new PsiFileNode(project, psiFile, ViewSettings.DEFAULT);
                        children.add(node);
                    }
                }

            }
            return children;
        }

        protected void update(PresentationData presentation) {
            presentation.setPresentableText(getValue());
            presentation.setOpenIcon(ICON_DISABLED_MODULE_GROUP_OPEN);
            presentation.setClosedIcon(ICON_DISABLED_MODULE_GROUP_CLOSED);
        }
    }
}

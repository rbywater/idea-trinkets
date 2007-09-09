package org.intellij.trinkets.offlineModule;

import com.intellij.ide.projectView.*;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.Icons;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.trinkets.offlineModule.util.OfflineModuleBundle;
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
@State(
        name = "OfflineModuleManager",
        storages = {
        @Storage(
                id = "disabled.modules",
                file = "$WORKSPACE_FILE$"
        )}
)
public final class OfflineModuleManager implements ProjectComponent, PersistentStateComponent<OfflineModuleManager>, TreeStructureProvider {
    private static final Icon ICON_MODULE_GROUP_OPEN = IconLoader.findIcon("/nodes/moduleGroupOpen.png");
    private static final Icon ICON_MODULE_GROUP_CLOSED = IconLoader.findIcon("/nodes/moduleGroupClosed.png");
    private static final Icon ICON_DISABLED_MODULE_GROUP_OPEN = layerIcons(ICON_MODULE_GROUP_OPEN, Icons.EXCLUDED_FROM_COMPILE_ICON);
    private static final Icon ICON_DISABLED_MODULE_GROUP_CLOSED = layerIcons(ICON_MODULE_GROUP_CLOSED, Icons.EXCLUDED_FROM_COMPILE_ICON);

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

    public final List<Object[]> MODULES = new ArrayList<Object[]>();
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
                Iterator<Object[]> iterator = MODULES.iterator();
                while (iterator.hasNext()) {
                    Object[] mp = iterator.next();
                    String first = (String) mp[0];
                    if (filePath.equals(first)) {
                        iterator.remove();
                    }
                }
                MODULES.add(new Object[]{filePath, groupPath});
                moduleManager.disposeModule(module);
                ProjectView.getInstance(project).refresh();
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
                    Iterator<Object[]> it = MODULES.iterator();
                    while (it.hasNext()) {
                        Object[] pair = it.next();
                        if (path.equals(pair[0])) {
                            it.remove();
                            model.setModuleGroupPath(module, (String[]) pair[1]);
                        }
                    }
                    ProjectView.getInstance(project).refresh();
                } catch (Exception ex) {
                    Messages.showErrorDialog(project, ex.getLocalizedMessage(), OfflineModuleBundle.message("can.t.load.module"));
                }
            }
        });
    }

    public OfflineModuleManager getState() {
        return this;
    }

    public void loadState(OfflineModuleManager state) {
        XmlSerializerUtil.copyBean(state, this);
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
            for (Object[] pair : MODULES) {
                if (file.getPath().equals(pair[0])) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        public Collection<? extends AbstractTreeNode> getChildren() {
            Collection<PsiFileNode> children = new ArrayList<PsiFileNode>();
            PsiManager psiManager = PsiManager.getInstance(project);
            for (Object[] pair : MODULES) {
                VirtualFile vf = LocalFileSystem.getInstance().findFileByPath((String) pair[0]);
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

package org.intellij.trinkets.offlineModule.projectView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.Icons;
import com.intellij.util.ui.EmptyIcon;
import org.intellij.trinkets.offlineModule.OfflineModuleManager;
import org.intellij.trinkets.offlineModule.util.OfflineModuleBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Efimov
 */
public class DisabledModulesTreeStructureProvider implements TreeStructureProvider {
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

    public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings) {
        if (settings.isShowModules()) {
            if (parent.getValue() instanceof Project) {
                for (AbstractTreeNode child : children) {
                    if (child instanceof DisabledModulesNode) {
                        return children;
                    }
                }
                Project project = (Project) parent.getValue();
                List<Pair<String,String[]>> disabledModules = OfflineModuleManager.getDisabledModules(project);
                if (disabledModules.size() > 0) {
                    // Show here disabled modules
                    children.add(new DisabledModulesNode(project, disabledModules));
                }
            }
        }
        return children;
    }

    @Nullable
    public Object getData(Collection<AbstractTreeNode> selected, String dataName) {
        return null;
    }

    private class DisabledModulesNode extends ProjectViewNode<String> {
        private final List<Pair<String, String[]>> disabledModules;

        public DisabledModulesNode(Project project, List<Pair<String, String[]>> disabledModules) {
            super(project, OfflineModuleBundle.message("disabled.modules"), ViewSettings.DEFAULT);
            this.disabledModules = disabledModules;
        }

        public boolean contains(@NotNull VirtualFile file) {
            for (Pair<String, String[]> pair : disabledModules) {
                if (file.getPath().equals(pair.getFirst())) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        public Collection<? extends AbstractTreeNode> getChildren() {
            Collection<PsiFileNode> children = new ArrayList<PsiFileNode>();
            PsiManager psiManager = PsiManager.getInstance(getProject());
            for (Pair<String, String[]> pair : disabledModules) {
                VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(pair.getFirst());
                if (vf != null) {
                    PsiFile psiFile = psiManager.findFile(vf);
                    if (psiFile != null) {
                        PsiFileNode node = new PsiFileNode(getProject(), psiFile, ViewSettings.DEFAULT);
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

package org.intellij.trinkets.pluginPacker.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.idea.devkit.module.PluginModuleType;

/**
 * Plugin XM utility
 *
 * @author Alexey Efimov
 */
public final class PluginXmlUtil {
    private PluginXmlUtil() {
    }

    public static String getPluginId(Module plugin) {
        XmlFile pluginXml = PluginModuleType.getPluginXml(plugin);
        if (pluginXml != null) {
            XmlDocument document = pluginXml.getDocument();
            if (document != null) {
                XmlTag rootTag = document.getRootTag();
                if (rootTag != null) {
                    XmlTag idTag = rootTag.findFirstSubTag("id");
                    if (idTag != null) {
                        return idTag.getValue().getTrimmedText();
                    }

                    XmlTag nameTag = rootTag.findFirstSubTag("name");
                    if (nameTag != null) {
                        String trimmedText = nameTag.getValue().getTrimmedText();
                        return StringUtil.capitalizeWords(trimmedText, true).replaceAll("\\s+", "");
                    }
                }
            }
        }
        return null;
    }

    public static String getPluginVersion(Module plugin) {
        XmlFile pluginXml = PluginModuleType.getPluginXml(plugin);
        if (pluginXml != null) {
            XmlDocument document = pluginXml.getDocument();
            if (document != null) {
                XmlTag rootTag = document.getRootTag();
                if (rootTag != null) {
                    XmlTag versionTag = rootTag.findFirstSubTag("version");
                    if (versionTag != null) {
                        return versionTag.getValue().getTrimmedText();
                    }
                }
            }
        }
        return null;
    }
}

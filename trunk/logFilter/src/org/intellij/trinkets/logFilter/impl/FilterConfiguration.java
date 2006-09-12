package org.intellij.trinkets.logFilter.impl;

import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

import java.awt.*;
import java.util.Arrays;

final class FilterConfiguration implements JDOMExternalizable {
    private String name;
    private String regexp;
    private String testLine;
    private boolean backgroundEnabled;
    private Color background;
    private boolean foregroundEnabled;
    private Color foreground;
    private boolean[] groupEnabled = new boolean[]{
            false, false, false, false, false, false, false, false, false
    };
    private boolean tabingEnabled;
    private boolean automaticalyTabName;
    private String tabName;
    private boolean decoratingEnabled;
    private boolean automaticalyColor;

    public FilterConfiguration() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(final String regexp) {
        this.regexp = regexp;
    }

    public String getTestLine() {
        return testLine;
    }

    public void setTestLine(final String testLine) {
        this.testLine = testLine;
    }

    public boolean isBackgroundEnabled() {
        return backgroundEnabled;
    }

    public void setBackgroundEnabled(final boolean backgroundEnabled) {
        this.backgroundEnabled = backgroundEnabled;
    }

    public boolean isForegroundEnabled() {
        return foregroundEnabled;
    }

    public void setForegroundEnabled(final boolean foregroundEnabled) {
        this.foregroundEnabled = foregroundEnabled;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public void setGroupEnabled(int group, boolean enabled) {
        groupEnabled[group] = enabled;
    }

    public boolean isGroupEnabled(int group) {
        return groupEnabled[group];
    }

    public void readExternal(Element element) {
        name = JDOMExternalizerUtil.readField(element, "name");
        regexp = JDOMExternalizerUtil.readField(element, "regexp");
        testLine = JDOMExternalizerUtil.readField(element, "testLine");
        tabName = JDOMExternalizerUtil.readField(element, "tabName");
        decoratingEnabled = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "decoratingEnabled"));
        tabingEnabled = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "tabingEnabled"));
        backgroundEnabled = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "backgroundEnabled"));
        foregroundEnabled = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "foregroundEnabled"));
        String backgroundValue = JDOMExternalizerUtil.readField(element, "background");
        if (backgroundValue != null) {
            background = new Color(Integer.parseInt(backgroundValue, 16));
        }
        String foregroundValue = JDOMExternalizerUtil.readField(element, "foreground");
        if (foregroundValue != null) {
            foreground = new Color(Integer.parseInt(foregroundValue, 16));
        }
        automaticalyTabName = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "automaticalyTabName"));
        automaticalyColor = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "automaticalyColor"));
        for (int i = 0; i < groupEnabled.length; i++) {
            groupEnabled[i] = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "group." + i));
        }
    }

    public void writeExternal(Element element) throws WriteExternalException {
        JDOMExternalizerUtil.writeField(element, "name", name);
        JDOMExternalizerUtil.writeField(element, "regexp", regexp);
        JDOMExternalizerUtil.writeField(element, "testLine", testLine);
        JDOMExternalizerUtil.writeField(element, "tabName", tabName);
        JDOMExternalizerUtil.writeField(element, "tabingEnabled", String.valueOf(tabingEnabled));
        JDOMExternalizerUtil.writeField(element, "decoratingEnabled", String.valueOf(decoratingEnabled));
        JDOMExternalizerUtil.writeField(element, "backgroundEnabled", String.valueOf(backgroundEnabled));
        JDOMExternalizerUtil.writeField(element, "foregroundEnabled", String.valueOf(foregroundEnabled));
        if (background != null) {
            JDOMExternalizerUtil.writeField(element, "background", Integer.toHexString(background.getRGB()));
        }
        if (foreground != null) {
            JDOMExternalizerUtil.writeField(element, "foreground", Integer.toHexString(foreground.getRGB()));
        }
        JDOMExternalizerUtil.writeField(element, "automaticalyColor", String.valueOf(automaticalyColor));
        JDOMExternalizerUtil.writeField(element, "automaticalyTabName", String.valueOf(automaticalyTabName));
        for (int i = 0; i < groupEnabled.length; i++) {
            JDOMExternalizerUtil.writeField(element, "group." + i, String.valueOf(groupEnabled[i]));
        }
    }

    public boolean isTabingEnabled() {
        return tabingEnabled;
    }

    public void setTabingEnabled(final boolean tabingEnabled) {
        this.tabingEnabled = tabingEnabled;
    }

    public boolean isAutomaticalyTabName() {
        return automaticalyTabName;
    }

    public void setAutomaticalyTabName(final boolean automaticalyTabName) {
        this.automaticalyTabName = automaticalyTabName;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(final String tabName) {
        this.tabName = tabName;
    }

    public boolean isDecoratingEnabled() {
        return decoratingEnabled;
    }

    public void setDecoratingEnabled(final boolean decoratingEnabled) {
        this.decoratingEnabled = decoratingEnabled;
    }

    public boolean isAutomaticalyColor() {
        return automaticalyColor;
    }

    public void setAutomaticalyColor(final boolean automaticalyColor) {
        this.automaticalyColor = automaticalyColor;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final FilterConfiguration that = (FilterConfiguration) o;

        if (automaticalyColor != that.automaticalyColor) return false;
        if (automaticalyTabName != that.automaticalyTabName) return false;
        if (backgroundEnabled != that.backgroundEnabled) return false;
        if (decoratingEnabled != that.decoratingEnabled) return false;
        if (foregroundEnabled != that.foregroundEnabled) return false;
        if (tabingEnabled != that.tabingEnabled) return false;
        if (background != null ? !background.equals(that.background) : that.background != null) return false;
        if (foreground != null ? !foreground.equals(that.foreground) : that.foreground != null) return false;
        if (!Arrays.equals(groupEnabled, that.groupEnabled)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (regexp != null ? !regexp.equals(that.regexp) : that.regexp != null) return false;
        if (tabName != null ? !tabName.equals(that.tabName) : that.tabName != null) return false;
        if (testLine != null ? !testLine.equals(that.testLine) : that.testLine != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (regexp != null ? regexp.hashCode() : 0);
        result = 31 * result + (testLine != null ? testLine.hashCode() : 0);
        result = 31 * result + (backgroundEnabled ? 1 : 0);
        result = 31 * result + (background != null ? background.hashCode() : 0);
        result = 31 * result + (foregroundEnabled ? 1 : 0);
        result = 31 * result + (foreground != null ? foreground.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(groupEnabled);
        result = 31 * result + (tabingEnabled ? 1 : 0);
        result = 31 * result + (automaticalyTabName ? 1 : 0);
        result = 31 * result + (tabName != null ? tabName.hashCode() : 0);
        result = 31 * result + (decoratingEnabled ? 1 : 0);
        result = 31 * result + (automaticalyColor ? 1 : 0);
        return result;
    }
}
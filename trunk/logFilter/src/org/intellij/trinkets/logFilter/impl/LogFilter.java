package org.intellij.trinkets.logFilter.impl;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.intellij.trinkets.logFilter.LogFilterManager;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter implementation.
 *
 * @author Alexey Efimov
 */
class LogFilter implements Filter {
    private static final Color[] PREDEFINED_COLORS = new Color[]{
            new Color(0x66, 0x0, 0x0),
            new Color(0x66, 0x66, 0x0),
            new Color(0x0, 0x66, 0x0),
            new Color(0x0, 0x66, 0x66),
            new Color(0x0, 0x0, 0x66),
            new Color(0x66, 0x0, 0x66),
    };

    private final Map<String, TextAttributes> pallete;
    private final FilterConfiguration setting;
    private final Pattern pattern;
    private final TextAttributes textAttributes;
    private final LogFilterManager manager;

    public LogFilter(LogFilterManager manager, FilterConfiguration setting) {
        this.manager = manager;
        this.setting = setting;
        pattern = Pattern.compile(setting.getRegexp());
        if (setting.isDecoratingEnabled()) {
            if (setting.isAutomaticalyColor()) {
                pallete = new HashMap<String, TextAttributes>(0);
                textAttributes = null;
            } else {
                textAttributes = new TextAttributes(setting.isForegroundEnabled() ? setting.getForeground() : null,
                        setting.isBackgroundEnabled() ? setting.getBackground() : null, null, null, 0);
                pallete = null;
            }
        } else {
            pallete = null;
            textAttributes = null;
        }
    }

    public Result applyFilter(String line, int entireLength) {
        Matcher matcher = pattern.matcher(line);
        Result result = null;
        if (matcher.find()) {
            StringBuffer marker = new StringBuffer();
            int groupCount = matcher.groupCount();
            for (int i = 1; i <= groupCount; i++) {
                if (setting.isGroupEnabled(i - 1)) {
                    marker.append(matcher.group(i));
                }
            }
            String key = marker.toString();
            if (textAttributes == null) {
                if (pallete != null) {
                    TextAttributes ta = pallete.get(key);
                    if (ta == null) {
                        // Generate new color
                        Color foreground = pallete.size() < PREDEFINED_COLORS.length ? PREDEFINED_COLORS[pallete.size()] : randomColor();
                        while (isPresent(foreground)) {
                            foreground = randomColor();
                        }
                        ta = new TextAttributes(foreground, null, null, null, 0);
                        pallete.put(key, ta);
                    }
                    result = new Result(entireLength - line.length(), entireLength, null, ta);
                }
            } else {
                result = new Result(entireLength - line.length(), entireLength, null, textAttributes);
            }

            performAdditional(result, key, line);
        }
        return result;
    }


    protected void performAdditional(Result result, String key, String line) {
    }

    public FilterConfiguration getSetting() {
        return setting;
    }

    public LogFilterManager getManager() {
        return manager;
    }

    private Color randomColor() {
        int r = (int) (Math.random() * (double) 51) + 51;
        int g = (int) (Math.random() * (double) 51) + 51;
        int b = (int) (Math.random() * (double) 51) + 51;
        return new Color(r, g, b);
    }

    private boolean isPresent(Color foreground) {
        Collection<TextAttributes> attributeses = pallete.values();
        for (TextAttributes attributes : attributeses) {
            if (attributes.getForegroundColor().equals(foreground)) {
                return true;
            }
        }
        return false;
    }
}

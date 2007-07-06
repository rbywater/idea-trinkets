package org.intellij.trinkets.research.actions;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.wm.ToolWindow;

/**
 * Keys for actions.
 *
 * @author Alexey Efimov
 */
public interface ResearchKeys {
    public static final DataKey<ToolWindow> RESEARCH_TOOL_WINDOW_KEY = DataKey.create(ResearchConstants.RESEARCH_TOOL_WINDOW);
}

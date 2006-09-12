package org.intellij.trinkets.hyperLink.actions;

import com.intellij.openapi.editor.RangeMarker;
import org.jetbrains.annotations.NotNull;

/**
 * Hyper link event.
 *
 * @author Alexey Efimov
 */
public interface HyperLinkEvent {
    @NotNull
    RangeMarker getRange();

    @NotNull
    String getReferenceText();
}

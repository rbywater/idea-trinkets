package org.intellij.trinkets.hyperLink.markup;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;

import java.awt.*;

public interface HyperLinkColors {
    TextAttributes REFERENCE = new TextAttributes(Color.BLUE, null, Color.BLUE, EffectType.LINE_UNDERSCORE, 0);
}

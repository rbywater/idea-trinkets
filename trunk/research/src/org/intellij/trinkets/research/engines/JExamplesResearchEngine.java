package org.intellij.trinkets.research.engines;

import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.regex.Pattern;

public final class JExamplesResearchEngine extends HttpResearchEngine {
    @NonNls
    private static final Pattern PATTERN = Pattern.compile("<li>[\\s]*<a href=\"([^\"]+)\">[\\s]*(.*?)[\\s]*<br/>[\\s]*</a>");
    @NonNls
    private static final String SERVER = "http://www.jexamples.com";
    @NonNls
    private static final String ACTION = "/fwd";
    @NonNls
    private static final String QUERY_PARAMETER = "queryText";
    private static final String[][] ADDITIONAL_PARAMENTERS = new String[][]{{"action", "srchRes"}};

    public JExamplesResearchEngine(String name, Icon icon) {
        super(name, icon, PATTERN, SERVER, ACTION, QUERY_PARAMETER, ADDITIONAL_PARAMENTERS);
    }
}

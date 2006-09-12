package org.intellij.trinkets.research.engines;

import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.regex.Pattern;

public final class GoogleResearchEngine extends HttpResearchEngine {
    private static final Pattern PATTERN = Pattern.compile("<p class=[^>]+><a class=[^\\s]+ href=\"([^\"]+)\">(.*?)</a>");
    @NonNls
    private static final String SERVER = "http://www.google.com";
    @NonNls
    private static final String ACTION = "/search";
    @NonNls
    private static final String QUERY_PARAMETER = "q";

    private String searchSite;

    public GoogleResearchEngine(String name, Icon icon, String searchSite) {
        super(name, icon, PATTERN, SERVER, ACTION, QUERY_PARAMETER, null);
        this.searchSite = searchSite;
    }

    public String getSearchSite() {
        return searchSite;
    }

    @Override
    protected String getQuery(String researchWord) {
        String query = super.getQuery(researchWord);
        return searchSite != null ? MessageFormat.format("site:{0} {1}", searchSite, query) : query;
    }
}

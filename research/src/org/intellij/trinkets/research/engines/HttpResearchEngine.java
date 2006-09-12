package org.intellij.trinkets.research.engines;

import org.intellij.trinkets.research.ResearchResult;
import org.intellij.trinkets.research.engines.DefaultResearchResult;
import org.intellij.trinkets.research.util.HttpSearchUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.util.Map;
import java.net.URLEncoder;

/**
 * Default research engine.
 *
 * @author Alexey Efimov
 */
public class HttpResearchEngine extends AbstractResearchEngine {
    private final Pattern pattern;
    private final String server;
    private final String action;
    private final String queryParameter;
    private final String[][] additionalParamenters;
    private String researchWord;

    public HttpResearchEngine(String name, Icon icon, Pattern pattern, String server, String action, String queryParameter, String[][] additionalParamenters) {
        super(name, icon);
        this.pattern = pattern;
        this.server = server;
        this.action = action;
        this.queryParameter = queryParameter;
        this.additionalParamenters = additionalParamenters;
    }


    public final Pattern getPattern() {
        return pattern;
    }

    public final String getServer() {
        return server;
    }

    public final String getAction() {
        return action;
    }

    public final String getQueryParameter() {
        return queryParameter;
    }

    public final String[][] getAdditionalParamenters() {
        return additionalParamenters;
    }

    public String getLastResearchWord() {
        return researchWord;
    }

    public String getLastLocation() {
        try {
            if (researchWord == null) {
                return null;
            }
            StringBuffer url = new StringBuffer(server);
            url.append(action);
            url.append('?');
            url.append(queryParameter);
            url.append('=');
            url.append(URLEncoder.encode(getQuery(researchWord), "UTF-8"));
            if (additionalParamenters != null) {
                for (String[] additionalParamenter : additionalParamenters) {
                    url.append('&');
                    url.append(URLEncoder.encode(additionalParamenter[0], "UTF-8"));
                    url.append('=');
                    url.append(URLEncoder.encode(additionalParamenter[1], "UTF-8"));
                }
            }
            return url.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public final synchronized ResearchResult research(String researchWord) throws IOException {
        setSearching(true);
        try {
            this.researchWord = researchWord;
            Map<String, String> result = HttpSearchUtil.search(
                    server, action, queryParameter, getQuery(researchWord), pattern, additionalParamenters
            );
            setResult(new DefaultResearchResult(result));
            return getLastResult();
        } finally {
            setSearching(false);
        }
    }

    protected String getQuery(String researchWord) {
        return researchWord;
    }
}

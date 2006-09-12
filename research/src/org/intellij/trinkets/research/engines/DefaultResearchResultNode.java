package org.intellij.trinkets.research.engines;

import org.intellij.trinkets.research.ResearchResultNode;

final class DefaultResearchResultNode implements ResearchResultNode {
    private final String url;
    private final String subject;

    public DefaultResearchResultNode(String url, String subject) {
        this.url = url;
        this.subject = subject;
    }

    public String getURL() {
        return url;
    }

    public String getSubject() {
        return subject;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultResearchResultNode that = (DefaultResearchResultNode) o;

        return !((subject != null ? !subject.equals(that.subject) : that.subject != null) || (url != null ? !url.equals(that.url) : that.url != null));

    }

    public int hashCode() {
        int result;
        result = (url != null ? url.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        return result;
    }
}

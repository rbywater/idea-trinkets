package org.intellij.trinkets.editorTree.history;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.util.containers.HashMap;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * History item for opened files.
 *
 * @author Alexey Efimov
 */
public final class FileHistory implements JDOMExternalizable, Comparable<FileHistory> {
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyyMMddHHmm");
    public static final FileHistory[] EMPTY_ARRAY = new FileHistory[]{};

    private final Map<String, String> fromMap = new HashMap<String, String>(0);
    private Date opened;
    private Date closed;
    private String url;

    public FileHistory(@NotNull Date date, @NotNull String url) {
        this.opened = date;
        this.url = url;
    }

    public FileHistory(Element h) throws InvalidDataException {
        readExternal(h);
    }

    public Date getOpened() {
        return opened;
    }

    public String getUrl() {
        return url;
    }

    public String getPresentableName() {
        return new File(VfsUtil.urlToPath(url)).getName();
    }

    public Date getClosed() {
        return closed;
    }

    public void setOpened(@NotNull Date opened) {
        this.opened = opened;
    }

    public void setClosed(@NotNull Date closed) {
        this.closed = closed;
    }

    public void readExternal(Element element) throws InvalidDataException {
        try {
            opened = DF.parse(element.getAttributeValue("opened"));
            String value = element.getAttributeValue("closed");
            if (value != null) {
                closed = DF.parse(value);
            }
            url = element.getAttributeValue("url");
            if (url == null) {
                throw new InvalidDataException("url attribute is missed");
            }

            List list = element.getChildren("from");
            for (Object o : list) {
                Element e = (Element) o;
                String fromUrl = e.getAttributeValue("url");
                if (fromUrl != null) {
                    fromMap.put(fromUrl, e.getAttributeValue("url"));
                }
            }
        } catch (Exception e) {
            throw new InvalidDataException(e);
        }
    }

    public void writeExternal(Element element) throws WriteExternalException {
        element.setAttribute("opened", DF.format(opened));
        if (closed != null) {
            element.setAttribute("closed", DF.format(opened));
        }
        element.setAttribute("url", url);

        Set<String> fromUrls = fromMap.keySet();
        for (String fromUrl : fromUrls) {
            Element e = new Element("from");
            e.setAttribute("url", fromUrl);
            String action = fromMap.get(fromUrl);
            if (action != null) {
                e.setAttribute("action", action);
            }
        }
    }

    public int compareTo(FileHistory o) {
        int compareDate = opened.compareTo(o.getOpened());
        if (compareDate == 0) {
            if (closed != null) {
                if (o.getClosed() == null) {
                    compareDate = 1;
                } else {
                    compareDate = closed.compareTo(o.getClosed());
                }
            } else if (o.getClosed() != null) {
                compareDate = -1;
            }
            if (compareDate == 0) {
                return getPresentableName().compareTo(o.getPresentableName());
            }
        }
        return -compareDate;
    }

    public void addFrom(String url, String actionId) {
        fromMap.put(url, actionId);
    }

    @NotNull
    public Set<String> getFromUrls() {
        return fromMap.keySet();
    }

    public String getFromActionId(String fromUrl) {
        return fromMap.get(fromUrl);
    }
}

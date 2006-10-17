package org.intellij.trinkets.editorTree.history.tree.elements;

import com.intellij.openapi.util.Pair;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import org.intellij.trinkets.editorTree.history.FileHistoryUtil;

import javax.swing.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Date root renderable cell.
 *
 * @author Alexey Efimov
 */
public final class DateTreeElement extends AbstractTreeElement<Date> {
    public static final Comparator<Date> COMPARATOR = new Comparator<Date>() {
        public int compare(Date o1, Date o2) {
            return o2.compareTo(o1);
        }
    };
    private static final SimpleDateFormat WEEK_DF = new SimpleDateFormat("EEEE, d MMMM");
    private static final SimpleDateFormat DEFAULT_DF = new SimpleDateFormat("dd.MM.yyyy");

    public DateTreeElement(Date date) {
        super(date, false);
    }

    public String getSpeedSearchText() {
        return null;
    }

    public void render(ColoredTreeCellRenderer renderer, JTree tree, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        renderer.setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
        Pair<String, SimpleTextAttributes> fragment = formatDate();
        renderer.append(fragment.getFirst(), fragment.getSecond());
        renderer.append(MessageFormat.format(" ({0,choice, 0#No files|1#1 file|2#{0,number} files})", getChildCount()), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
    }

    private Pair<String, SimpleTextAttributes> formatDate() {
        Calendar today = FileHistoryUtil.day(new Date());
        Date date = getValue();
        Calendar day = FileHistoryUtil.day(date);
        if (day.equals(today)) {
            return new Pair<String, SimpleTextAttributes>("Today", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
        } else {
            // Yesterday
            long delta = (today.getTime().getTime() - day.getTime().getTime()) / 3600000L;
            if (delta <= 24) {
                return new Pair<String, SimpleTextAttributes>("Yesterday", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            } else if (delta <= 168) {
                // Weak
                return new Pair<String, SimpleTextAttributes>(WEEK_DF.format(date), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
            }
        }
        return new Pair<String, SimpleTextAttributes>(DEFAULT_DF.format(date), SimpleTextAttributes.GRAYED_ATTRIBUTES);
    }
}

package org.intellij.trinkets.editorTree.history;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * File history utility.
 *
 * @author Alexey Efimov
 */
public final class FileHistoryUtil {
    private static final Date NO_DATE = new Date(0L);

    /**
     * Filter history by date. From date up to date.
     *
     * @param items Items
     * @param from  From date (included)
     * @param to    To date (excluded)
     * @return Filtered list
     */
    @NotNull
    public static Collection<FileHistory> filterByDate(@NotNull Collection<FileHistory> items, Date from, Date to) {
        Collection<FileHistory> filtered = new ArrayList<FileHistory>();
        for (FileHistory item : items) {
            if ((from == null || !from.after(item.getOpened())) &&
                    (to == null || to.after(item.getOpened()))) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    @NotNull
    public static Collection<FileHistory> filterDay(@NotNull Collection<FileHistory> items, @NotNull Date date) {
        Calendar dayStart = day(date);
        Calendar dayEnd = (Calendar) dayStart.clone();
        dayEnd.add(Calendar.DATE, 1);
        return filterByDate(items, dayStart.getTime(), dayEnd.getTime());
    }

    public static Calendar day(Date date) {
        Calendar current = Calendar.getInstance();
        current.setTime(date);
        Calendar day = Calendar.getInstance();
        day.clear();
        day.set(Calendar.YEAR, current.get(Calendar.YEAR));
        day.set(Calendar.MONTH, current.get(Calendar.MONTH));
        day.set(Calendar.DATE, current.get(Calendar.DATE));
        return day;
    }

    @NotNull
    public static Map<Date, Collection<FileHistory>> splitByDays(@NotNull Collection<FileHistory> items, int maxMapSize) {
        Map<Date, Collection<FileHistory>> map = new HashMap<Date, Collection<FileHistory>>();
        Collection<FileHistory> all = new ArrayList<FileHistory>(items);
        while (maxMapSize - 1 > map.size() && !all.isEmpty()) {
            // Next item
            FileHistory next = all.iterator().next();
            Collection<FileHistory> histories = filterDay(all, next.getOpened());
            all.removeAll(histories);
            map.put(next.getOpened(), histories);
        }
        if (maxMapSize > map.size() && !all.isEmpty()) {
            map.put(NO_DATE, all);
        }
        return map;
    }

    @NotNull
    public static VirtualFile[] resolve(@NotNull FileHistory[] histories) {
        List<VirtualFile> files = new ArrayList<VirtualFile>();
        for (FileHistory history : histories) {
            VirtualFile vf = resolve(history);
            if (vf != null) {
                files.add(vf);
            }
        }
        return files.toArray(VirtualFile.EMPTY_ARRAY);
    }

    @Nullable
    public static VirtualFile resolve(@NotNull FileHistory history) {
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        return virtualFileManager.findFileByUrl(history.getUrl());
    }

    private FileHistoryUtil() {
    }
}

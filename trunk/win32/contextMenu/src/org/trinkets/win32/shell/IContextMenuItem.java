package org.trinkets.win32.shell;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.image.*;
import java.util.LinkedList;
import java.util.List;

/**
 * IContextMenu item.
 *
 * @author Alexey Efimov
 */
public final class IContextMenuItem {
    public static final IContextMenuItem[] EMPTY_ARRAY = {};
    private static final int[] MASK = {0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000};

    private final int id;
    private final String text;
    private final String description;
    private final boolean subMenu;
    private final DataBuffer imageBuffer;
    private final int imageWidth;
    private final int imageHeight;
    private IContextMenuItem parent;
    private Icon icon;

    public IContextMenuItem(int id, String text, String description, boolean subMenu, DataBuffer imageBuffer, int imageWidth, int imageHeight) {
        this.id = id;
        this.text = text;
        this.description = description;
        this.subMenu = subMenu;
        this.imageBuffer = imageBuffer;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSubMenu() {
        return subMenu;
    }

    public IContextMenuItem getParent() {
        return parent;
    }

    public void setParent(IContextMenuItem parent) {
        this.parent = parent;
    }

    @NotNull
    public IContextMenuItem[] getParentPath() {
        List<IContextMenuItem> path = new LinkedList<IContextMenuItem>();
        fillParentPath(path);
        return path.toArray(EMPTY_ARRAY);
    }

    private void fillParentPath(List<IContextMenuItem> path) {
        if (parent != null) {
            parent.fillParentPath(path);
            path.add(parent);
        }
    }

    public IContextMenuItem[] getPath() {
        List<IContextMenuItem> path = new LinkedList<IContextMenuItem>();
        fillParentPath(path);
        path.add(this);
        return path.toArray(EMPTY_ARRAY);
    }

    public DataBuffer getImageBuffer() {
        return imageBuffer;
    }

    public Icon getIcon() {
        if (icon == null) {
            if (imageBuffer != null) {
                ColorModel colorModel = new DirectColorModel(32, MASK[0], MASK[1], MASK[2], MASK[3]);
                int size = imageBuffer.getSize();
                // Corner points detection
                int[] corners = {
                        imageBuffer.getElem(0),
                        imageBuffer.getElem(imageWidth),
                        imageBuffer.getElem(size - imageWidth - 1),
                        imageBuffer.getElem(size - 1)
                };
                // Find equals corners
                int colorIndex = -1;
                for (int i = 0; colorIndex == -1 && i < corners.length; i++) {
                    int c1 = corners[i];
                    for (int j = i + 1; colorIndex == -1 && j < corners.length; j++) {
                        int c2 = corners[j];
                        if (c1 == c2) {
                            colorIndex = i;
                        }
                    }
                }
                int color = colorIndex != -1 ? corners[colorIndex] : 0x00ffffff;
                // Delete color
                for (int i = 0; i < size; i++) {
                    int elem = imageBuffer.getElem(i);
                    if ((elem & 0xff000000) == 0 && (elem != color)) {
                        elem |= 0xff000000;
                        imageBuffer.setElem(i, elem);
                    }
                }

                WritableRaster writableRaster = Raster.createPackedRaster(imageBuffer, imageWidth, imageHeight, imageWidth, MASK, null);
                BufferedImage image = new BufferedImage(colorModel, writableRaster, true, null);

                icon = new ImageIcon(image);
            }
        }
        return icon;
    }
}

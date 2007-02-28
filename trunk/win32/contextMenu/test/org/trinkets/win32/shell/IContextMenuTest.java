package org.trinkets.win32.shell;

import com.jniwrapper.DefaultLibraryLoader;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Test for {@link IContextMenu}.
 *
 * @author Alexey Efimov
 */
public class IContextMenuTest extends TestCase {
    protected void setUp() throws Exception {
        DefaultLibraryLoader loader = DefaultLibraryLoader.getInstance();
        loader.addPath(new File("cpp/Debug").getAbsolutePath());
        loader.loadLibrary("IContextMenu_JNI");
        super.setUp();
    }

    public void testIContextMenuForFile() throws FileNotFoundException {
//        IContextMenu menu = new IContextMenuImpl("cpp/StdAfx.cpp");
//        IContextMenuItem[] items = menu.getItems();
//        assertTrue("Menu are empty", items.length > 0);
    }

    public void testIContextMenuForDirectory() throws FileNotFoundException {
//        IContextMenu menu = new IContextMenuImpl("cpp");
//        IContextMenuItem[] items = menu.getItems();
//        assertTrue("Menu are empty", items.length > 0);
    }
}

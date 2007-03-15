package org.trinkets.util;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.zip.ZipInputStream;

public class LibraryLoader {
    private final File librariesHome;
    private final String libraryExtension;
    private final LibraryClassLoader loader;


    public LibraryLoader(File librariesHome, String libraryExtension) {
        this.librariesHome = librariesHome;
        this.libraryExtension = libraryExtension;
        this.loader = new LibraryClassLoader(LibraryLoader.class.getClassLoader(), librariesHome, libraryExtension);
    }

    private static final class LibraryClassLoader extends ClassLoader {
        private final File librariesHome;
        private final String libraryExtension;

        public LibraryClassLoader(ClassLoader parent, File librariesHome, String libraryExtension) {
            super(parent);
            this.librariesHome = librariesHome;
            this.libraryExtension = libraryExtension;
        }

        protected final Class<?> defineClass(String name) throws ClassNotFoundException {
            URL classURL = getParent().getResource(name.replace('.', '/').concat(".class"));
            if (classURL != null) {
                try {
                    InputStream inputStream = classURL.openStream();
                    try {
                        byte[] bytecode = null;
                        int length = 0;
                        byte[] buffer = new byte[1024];
                        for (int n = inputStream.read(buffer); n > 0; n = inputStream.read(buffer)) {
                            while (bytecode == null || (bytecode.length - length - n) <= 0) {
                                byte[] oldValue = bytecode;
                                bytecode = new byte[(oldValue != null ? oldValue.length : 0) + (1024 << 1)];
                                if (oldValue != null) {
                                    System.arraycopy(oldValue, 0, bytecode, 0, oldValue.length);
                                }
                            }
                            System.arraycopy(buffer, 0, bytecode, length, n);
                            length += n;
                        }
                        return defineClass(name, bytecode, 0, length);
                    } finally {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    // Ignore
                }
            }
            throw new ClassNotFoundException(name);
        }

        protected String findLibrary(String libname) {
            String libFileName = libname + libraryExtension;
            File[] files = librariesHome.listFiles();
            for (File file : files) {
                if (file.getName().equals(libFileName)) {
                    return file.getAbsolutePath();
                }
            }
            return super.findLibrary(libname);
        }
    }

    @SuppressWarnings({"unchecked"})
    public final Class registerNativeSupport(String type) {
        try {
            return loader.defineClass(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public final Object newInstanceNativeSupport(String type, Class[] args, Object[] values) {
        try {
            Class aClass = registerNativeSupport(type);
            Constructor constructor = aClass.getConstructor(args);
            return constructor.newInstance(values);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This extract resource from classpath to temporary file.
     *
     * @param resourceName Resource URL of zipped library binary
     * @param libraryName  File name after extracting
     */
    public void extractLibrary(String resourceName, String libraryName) {
        try {
            URL resource = loader.getParent().getResource(resourceName);
            if (resource != null) {
                librariesHome.mkdirs();
                librariesHome.deleteOnExit();
                File file = new File(librariesHome, libraryName + libraryExtension);
                file.deleteOnExit();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                ZipInputStream zip = new ZipInputStream(resource.openStream());
                zip.getNextEntry();
                InputStream inputStream = new BufferedInputStream(zip, 1024);
                try {
                    byte[] buffer = new byte[1024];
                    for (int n = inputStream.read(buffer); n != -1; n = inputStream.read(buffer)) {
                        fileOutputStream.write(buffer, 0, n);
                    }
                } finally {
                    inputStream.close();
                    fileOutputStream.close();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}

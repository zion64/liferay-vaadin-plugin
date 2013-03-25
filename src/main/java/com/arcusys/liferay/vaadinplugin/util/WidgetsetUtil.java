package com.arcusys.liferay.vaadinplugin.util;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public abstract class WidgetsetUtil {
    private static Log log = LogFactoryUtil.getLog(WidgetsetUtil.class);

    private static final String TMP_DIR_PREFIX = "vaadinws";
    private static final String BCKP_DIR_PREFIX = "vaadinbckp";

    private static final int MAX_NUMBER_OF_WIDGETSET_BACKUPS = 5;

    public static final FilenameFilter JAR_FILES_ONLY = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };

    public static List<VaadinAddonInfo> getAvailableWidgetSets(File dir) {
        List<VaadinAddonInfo> addons = new ArrayList<VaadinAddonInfo>();
        File[] jars = dir.listFiles(JAR_FILES_ONLY);
        for (File jar : jars) {
            includeVaadinAddonJar(jar, addons);
        }
//        StringBuilder sb = new StringBuilder();
        // sb.append("Widgetsets found:\n");
        // for (String ws : widgetsets.keySet()) {
        // sb.append("\t");
        // sb.append(ws);
        // sb.append(" in ");
        // sb.append(widgetsets.get(ws));
        // sb.append("\n");
        // }
        // System.out.println(sb.toString());
        return addons;
    }

    private static void includeVaadinAddonJar(File file,
                                              List<VaadinAddonInfo> addons) {
        try {
            URL url = new URL("file:" + file.getCanonicalPath());
            url = new URL("jar:" + url.toExternalForm() + "!/");
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            JarFile jarFile = conn.getJarFile();
            if (jarFile != null) {
                Manifest manifest = jarFile.getManifest();
                if (manifest == null) {
                    // No manifest so this is not a Vaadin Add-on
                    return;
                }

                Attributes attrs = manifest.getMainAttributes();
                String value = attrs.getValue("Vaadin-Widgetsets");
                if (value != null) {
                    String name = attrs.getValue("Implementation-Title");
                    String version = attrs.getValue("Implementation-Version");
                    if (name == null || version == null) {
                        // A jar file with Vaadin-Widgetsets but name or version
                        // missing. Most probably vaadin.jar itself, skipping it
                        // here
                        return;
                    }

                    List<String> widgetsets = new ArrayList<String>();
                    String[] widgetsetNames = value.split(",");
                    for(String wName: widgetsetNames )
                    {
                        String widgetsetname = wName.trim()
                                .intern();
                        if (!widgetsetname.equals("")) {
                            widgetsets.add(widgetsetname);
                        }
                    }

                    if (!widgetsets.isEmpty()) {
                        addons.add(new VaadinAddonInfo(name, version, file,
                                widgetsets));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Exception trying to include Vaadin Add-ons.", e);
        }

    }

    /**
     * Creates a widgetset .gwt.xml file under a given directory.
     *
     * @param widgetset
     *            the name of Widgetset. For example: com.example.TestWidgetSet
     * @throws java.io.IOException
     */
    public static void createWidgetset(File tmpDir, String widgetset,
                                       Set<String> includeWidgetsets) throws IOException {

        String dir = widgetset.substring(0, widgetset.lastIndexOf("."))
                .replace(".", ControlPanelPortletUtil.FileSeparator);
        String file = widgetset.substring(widgetset.lastIndexOf(".") + 1,
                widgetset.length()) + ".gwt.xml";

        File widgetsetDir = new File(tmpDir, dir);
        if (!widgetsetDir.mkdirs()) {
            throw new IOException("Could not create dir: "
                    + widgetsetDir.getAbsolutePath());
        }
        File widgetsetFile = new File(widgetsetDir, file);
        if (!widgetsetFile.createNewFile()) {
            throw new IOException("");
        }

        PrintStream printStream = new PrintStream(new FileOutputStream(
                widgetsetFile));
        printStream.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE module PUBLIC \"-//Google Inc.//DTD "
                + "Google Web Toolkit 1.7.0//EN\" \"http://google"
                + "-web-toolkit.googlecode.com/svn/tags/1.7.0/dis"
                + "tro-source/core/src/gwt-module.dtd\">\n");
        printStream.print("<module>\n");

        for (String ws : includeWidgetsets) {
            printStream.print("<inherits name=\"" + ws + "\" />\n");
        }

        printStream.print("\n</module>\n");
        printStream.close();
    }

    public static File createTempDir() throws IOException {
        return createTmpWorkDir(TMP_DIR_PREFIX);
    }

    public static File createBackupDir() throws IOException {
        return createTmpWorkDir(BCKP_DIR_PREFIX);
    }

    private static File createTmpWorkDir(String dirName)throws IOException {
        File temp = File.createTempFile(dirName, null);

        if (!temp.delete()) {
            throw new IOException("Could not delete temp file: "
                    + temp.getAbsolutePath());
        }

        if (!temp.mkdir()) {
            throw new IOException("Could not create temp directory: "
                    + temp.getAbsolutePath());
        }

        return temp;
    }


    public static void backupOldWidgetset(String originalWidgetset)
            throws IOException {
        File srcDir = new File(originalWidgetset);
        File destDir = new File(originalWidgetset + ".0.bak");
        if (srcDir.exists() || srcDir.isDirectory()) {
            FileUtils.deleteDirectory(destDir);
            FileUtils.moveDirectory(srcDir, destDir);
        }
    }

    public static void rotateWidgetsetBackups(String originalWidgetset)
            throws IOException {
        for (int i = MAX_NUMBER_OF_WIDGETSET_BACKUPS - 1; i > 0; i--) {
            File srcDir = new File(originalWidgetset + "." + (i - 1) + ".bak");
            File destDir = new File(originalWidgetset + "." + i + ".bak");
            if (srcDir.exists() || srcDir.isDirectory()) {
                FileUtils.deleteDirectory(destDir);
                FileUtils.moveDirectory(srcDir, destDir);
            }
        }
    }
}

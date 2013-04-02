package com.arcusys.liferay.vaadinplugin.util;

/*
 * #%L
 * Liferay Vaadin Plugin
 * %%
 * Copyright (C) 2013 Arcusys Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.util.PortalUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.portlet.PortletConfig;

import com.vaadin.server.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 14.02.13
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
public abstract class ControlPanelPortletUtil {
    private static Log log = LogFactoryUtil.getLog(ControlPanelPortletUtil.class);

    private static final String VAADIN_VERSION_MANIFEST_STRING = "Bundle-Version";
    public static final String VAADIN_DOWNLOAD_URL = "http://vaadin.com/download/";
    public static final String VAADIN_ALL_NAME = "vaadin-all";
    public static final String VAADIN_SERVER_JAR = "vaadin-server.jar";
    public static final String VAADIN_THEMES_JAR = "vaadin-themes.jar";
    public static final String VAADIN_THEME_COMPILER_JAR = "vaadin-theme-compiler.jar";

    public static final String VAADIN_CLIENT_COMPILED_JAR = "vaadin-client-compiled.jar";
    public static final String VAADIN_CLIENT_COMPILER_JAR = "vaadin-client-compiler.jar";
    public static final String VAADIN_CLIENT_JAR = "vaadin-client.jar";
    public static final String VAADIN_SHARED_JAR = "vaadin-shared.jar";
    public static final String VAADIN_SHARED_DEPS_JAR = "vaadin-shared-deps.jar";
    public static final String JSOUP_JAR = "jsoup.jar";
    public static final String ANT_JAR = "ant.jar";

    public static final String VAADIN_JAR = "vaadin.jar";
    public static final String VAADIN_ALL_ZIP = "vaadin-all.zip";

    //public static final String LATEST_VAADIN_INFO = "http://vaadin.com/download/LATEST?source=liferay&version=@VERSION@";
    public static final String LATEST_VAADIN_INFO = "http://vaadin.com/download/release/7.0/LATEST?source=liferay&version=@VERSION@";
    public static final String ALL_VERSIONS_INFO = "http://vaadin.com/download/VERSIONS_ALL?source=liferay&version=@VERSION@";

    private static Collection<VaadinFileInfo> vaadinFiles = null;

    public static final String FileSeparator = File.separator;

    public static  Collection<VaadinFileInfo> getVaadinFilesInfo(){

        if(vaadinFiles == null)
        {
            String portalPath =  getPortalLibDir();
            String vaadinClientJarsPath =  getVaadinClientJarsDir();

            vaadinFiles = Arrays.asList(
                    new VaadinFileInfo( VAADIN_SERVER_JAR, portalPath ),
                    new VaadinFileInfo( VAADIN_SHARED_JAR, portalPath),
                    new VaadinFileInfo( VAADIN_SHARED_DEPS_JAR, portalPath, FileSeparator +  "lib" + FileSeparator),
                    new VaadinFileInfo( JSOUP_JAR, portalPath, FileSeparator +  "lib" + FileSeparator),
                    new VaadinFileInfo( VAADIN_THEME_COMPILER_JAR, portalPath ),
                    new VaadinFileInfo( VAADIN_THEMES_JAR, portalPath ),
                    new VaadinFileInfo( VAADIN_CLIENT_COMPILER_JAR, vaadinClientJarsPath ),
                    new VaadinFileInfo( VAADIN_CLIENT_JAR, vaadinClientJarsPath )
            );
        }
        return  vaadinFiles;
    }


    public static String getPortalLibDir() {
        // return ".../tomcat-6.0.29/webapps/ROOT/WEB-INF/lib/";
        return PortalUtil.getPortalLibDir();
    }

    private static String getPortalWebDir()
    {
        return PortalUtil.getPortalWebDir();
    }

    public static  String getVaadinClientJarsDir(){
        // return ".../tomcat-{version}/webapps/ROOT/WEB-INF/vaadin-clients-jars/";
        return getPortalWebDir() +"WEB-INF" + FileSeparator + "vaadin-clients-jars" + FileSeparator;
    }

    public static File get6VersionVaadinJarLocation() {
        // return ".../tomcat-6.0.29/webapps/ROOT/WEB-INF/lib/vaadin.jar";
        File portalLibDir = new File(getPortalLibDir());
        return new File(portalLibDir, VAADIN_JAR);
    }

    public static File getVaadinServerJarLocation() {
        // return ".../tomcat-6.0.29/webapps/ROOT/WEB-INF/lib/";
        File portalLibDir = new File(getPortalLibDir());
        return new File(portalLibDir, VAADIN_SERVER_JAR);
    }

    public static File getVaadinClientCompilerJarLocation() {
        File portalLibDir = new File(getVaadinClientJarsDir());
        return new File(portalLibDir, VAADIN_CLIENT_COMPILER_JAR);
    }

    public static File getVaadinClientJarLocation() {
        File portalLibDir = new File(getVaadinClientJarsDir());
        return new File(portalLibDir, VAADIN_CLIENT_JAR);
    }

    public static File getVaadinSharedJarLocation() {
        // return ".../tomcat-{version}/webapps/ROOT/WEB-INF/lib/";
        File portalLibDir = new File(getPortalLibDir());
        return new File(portalLibDir, VAADIN_SHARED_JAR);
    }

    public static File getVaadinSharedDepsJarLocation() {
        // return ".../tomcat-{version}/webapps/ROOT/WEB-INF/lib/";
        File portalLibDir = new File(getPortalLibDir());
        return new File(portalLibDir, VAADIN_SHARED_DEPS_JAR);
    }

    public static File getAntJarLocation() {
        File portalLibDir = new File(getPortalLibDir());
        return new File(portalLibDir, ANT_JAR);
    }

    /**
     * Returns the Vaadin version for the Vaadin jar used in the portal.
     *
     * @return The version as a String or null if the required version could not
     *         be determined
     * @throws java.io.IOException
     *             If the portal's Vaadin jar cannot be read
     */
    public static String getPortalVaadinVersion() throws IOException {
        JarFile jarFile = new JarFile(getVaadinServerJarLocation());

        try {
            // Check Vaadin 7 version from manifest
            String manifestVaadinVersion = getManifestVaadinVersion(jarFile);
            if (manifestVaadinVersion != null) {
                return manifestVaadinVersion;
            }
            return  null;
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        }
    }

    public static String getPortalVaadin6Version() throws IOException {

        JarFile jarFile = new JarFile(get6VersionVaadinJarLocation());

        try {
            // Check Vaadin 6 version from manifest
            String manifestVaadinVersion = getManifestVaadin6Version(jarFile);
            if (manifestVaadinVersion != null) {
                return manifestVaadinVersion;
            }
            return null;
        }finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        }
    }

    private static String getManifestVaadinVersion(JarFile jarFile)
            throws IOException {
        return getManifestAttribute(jarFile, VAADIN_VERSION_MANIFEST_STRING);
    }

    private static String getManifestVaadin6Version(JarFile jarFile)
            throws IOException {
        return getManifestAttributeForVaadin6(jarFile, VAADIN_VERSION_MANIFEST_STRING);
    }

    private static String getManifestAttribute(JarFile jarFile, String versionAttribute) throws IOException {
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return null;
        }
        Attributes attr = manifest.getMainAttributes();
        String bundleName = attr.getValue("Bundle-Name");
        if (bundleName != null && bundleName.toLowerCase().equals("vaadin-server")) {
            return attr.getValue(versionAttribute);
        }

        return null;
    }

    private static String getManifestAttributeForVaadin6(JarFile jarFile, String versionAttribute) throws IOException {
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return null;
        }
        Attributes attr = manifest.getMainAttributes();
        String bundleName = attr.getValue("Bundle-Name");
        if (bundleName != null && bundleName.equals("Vaadin")) {
            return attr.getValue(versionAttribute);
        }

        return null;
    }

    public static String getPortalWidgetset() {
        // return "com.vaadin.portal.gwt.PortalDefaultWidgetSet";
        return PropsUtil.get("vaadin.widgetset");
    }

    public static String getControlPanelWidgetset(PortletConfig portletConfig) {
        return portletConfig.getInitParameter(Constants.PARAMETER_WIDGETSET);
    }

    public static String getVaadinResourceDir() {
        // return ".../tomcat-6.0.29/webapps/ROOT/html/VAADIN/";
        return PortalUtil.getPortalWebDir()
                + PropsUtil.get("vaadin.resources.path") + "/VAADIN/";
    }

    public static String getWidgetsetDir() {
        // return ".../tomcat-6.0.29/webapps/ROOT/html/VAADIN/widgetsets/";
        return getVaadinResourceDir() + "/widgetsets/";
    }

    public static List<File> getLibs(List<File> exclude) {
        List<File> libs = new ArrayList<File>();

        // Add JARs in portal lib directory
        File[] jars = new File(getPortalLibDir())
                .listFiles(WidgetsetUtil.JAR_FILES_ONLY);
        for (File jar : jars) {
            if (!exclude.contains(jar)) {
                libs.add(jar);
            }
        }

        return libs;
    }

    /**
     * Downloads the given URL to the targetDir and names it
     * {@code targetFilename}. Creates the directory and its parent(s) if it
     * does not exist.
     *
     * @param downloadUrl
     * @param targetDir
     * @param targetFilename
     * @throws IOException
     */
    public static void download(String downloadUrl, String targetDir, String targetFilename) throws IOException {
        File f = new File(targetDir);
        f.mkdirs();
        URL url = new URL(downloadUrl);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(10000);
        InputStream is = conn.getInputStream();
        FileOutputStream out = new FileOutputStream(new File(targetDir,
                targetFilename));
        IOUtils.copy(is, out);
        close(out);
        close(is);
    }

    /**
     * Extracts the jarEntry from the jarFile to the target directory.
     *
     * @param jarFile
     * @param jarEntry
     * @param targetDir
     * @return true if extraction was successful, false otherwise
     */
    public static boolean extractJarEntry(JarFile jarFile, JarEntry jarEntry,
                                          String targetDir) {
        boolean extractSuccessful = false;
        File file = new File(targetDir);
        if (!file.exists()) {
            file.mkdir();
        }
        if (jarEntry != null) {
            InputStream inputStream = null;
            try {
                inputStream = jarFile.getInputStream(jarEntry);
                file = new File(targetDir + jarEntry.getName());
                if (jarEntry.isDirectory()) {
                    file.mkdir();
                } else {
                    int size;
                    byte[] buffer = new byte[2048];

                    FileOutputStream fileOutputStream = new FileOutputStream(
                            file);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                            fileOutputStream, buffer.length);

                    try {
                        while ((size = inputStream.read(buffer, 0,
                                buffer.length)) != -1) {
                            bufferedOutputStream.write(buffer, 0, size);
                        }
                        bufferedOutputStream.flush();
                    } finally {
                        bufferedOutputStream.close();
                    }
                }
                extractSuccessful = true;
            } catch (Exception e) {
                log.warn(e);
            } finally {
                close(inputStream);
            }
        }
        return extractSuccessful;
    }

    public static void close(OutputStream out) {
        if (out == null) {
            return;
        }
        try {
            out.close();
        } catch (IOException e) {
            log.warn(e);
        }
    }

    public static void close(InputStream in) {
        if (in == null) {
            return;
        }
        try {
            in.close();
        } catch (IOException e) {
            log.warn(e);
        }
    }

    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            log.warn(e);
        }
    }
}



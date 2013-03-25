package com.arcusys.liferay.vaadinplugin;

import com.arcusys.liferay.vaadinplugin.util.ControlPanelPortletUtil;
import com.arcusys.liferay.vaadinplugin.util.ILog;
import com.arcusys.liferay.vaadinplugin.util.VaadinFileInfo;
import com.arcusys.liferay.vaadinplugin.util.WidgetsetUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 15.02.13
 * Time: 19:00
 */

/*
integrating steps

* The next step is to remove the integrated Vaadin 6 from the package. What you need to remove is:
liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/html/VAADIN
liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/WEB-INF/lib/vaadin.jar

The removed jars need to be replaced by the Vaadin 7 version:
Extract the VAADIN folder from vaadin-server.jar into liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/html/VAADIN. This extracts vaadinBootstrap.js which is needed by all Vaadin applications.
Extract the VAADIN folder from vaadin-themes.jar into liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/html/VAADIN. This extracts all Vaadin themes.
Extract the VAADIN folder from vaadin-client-compiled.jar into liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/html/VAADIN. This extracts the default widget set.
Copy vaadin-server.jar, vaadin-shared.jar, vaadin-shared-deps.jar, jsoup.jar into liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/WEB-INF/lib. You can optionally remove the version numbers if you want to follow the Liferay standard.

You have now integrated Vaadin into your Liferay Porta and are ready to start creating Vaadin portlets.
*
* */
public class VaadinUpdater implements Runnable {
    private static Log log = LogFactoryUtil.getLog(VaadinUpdater.class);

    private final UpgradeListener upgradeListener;

    private final String downloadLocation;
    private ILog outputLog;

    public interface UpgradeListener {
        void updateComplete();

        void updateFailed(String message);
    }

    public VaadinUpdater(String downloadLocation, UpgradeListener upgradeListener, ILog outputLog) {
        this.downloadLocation = downloadLocation;
        this.upgradeListener = upgradeListener;
        this.outputLog = outputLog;
    }

   private File backupDir = null;
   private String backupPath = null;

   private  String fileSeparator = ControlPanelPortletUtil.FileSeparator;


    public void run() {
        File tmpDir = null;
        File vaadinZipFile;
        String tmpPath;

        try {
            tmpDir = WidgetsetUtil.createTempDir();
            tmpPath = tmpDir.getPath();

            backupDir = WidgetsetUtil.createBackupDir();
            backupPath = backupDir.getPath();

            String vaadinClientJarsDirPath = ControlPanelPortletUtil.getVaadinClientJarsDir();

            File vaadinClientJarsDir = new File(vaadinClientJarsDirPath);
            if (!vaadinClientJarsDir.exists()) {
                vaadinClientJarsDir.mkdir();
            }

            try {
                outputLog.log("Downloading " + downloadLocation + " to " + tmpPath);
                ControlPanelPortletUtil.download(downloadLocation, tmpPath, ControlPanelPortletUtil.VAADIN_ALL_ZIP);
                vaadinZipFile = new File(tmpDir, ControlPanelPortletUtil.VAADIN_ALL_ZIP);

                outputLog.log("Download complete.");
            } catch (Exception e) {
                log.warn("Download failed.", e);
                upgradeListener.updateFailed("Download failed: " + e.getMessage());
                return;
            }

            outputLog.log("Extracting files... ");
            String zipDestinationPath;
            try {
                zipDestinationPath = exctractZipFile(vaadinZipFile, tmpPath);
            } catch (Exception e) {
                log.warn("Unzip failed.", e);
                upgradeListener.updateFailed("Extraction failed: " + e.getMessage());
                return;
            }

            if (zipDestinationPath == null) return;

            backupOldFiles();

            String vaadinResourcePath = ControlPanelPortletUtil.getVaadinResourceDir();
            File vaadinResource = new File(vaadinResourcePath);
            if (vaadinResource.exists()) {
                outputLog.log("Removing old vaadin resources : " + vaadinResourcePath);
                FileUtils.deleteDirectory(vaadinResource);
            }

            File vaadin6Version = ControlPanelPortletUtil.get6VersionVaadinJarLocation();
            if (vaadin6Version.exists()) {
                outputLog.log("Removing old vaadin.jar : " + vaadin6Version.getAbsolutePath());
                vaadin6Version.delete();
            }

            /*
            Extract the VAADIN folder from vaadin-server.jar into liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/html/VAADIN.
            This extracts vaadinBootstrap.js which is needed by all Vaadin applications.
*/
            if (extractVAADINFolder(zipDestinationPath, ControlPanelPortletUtil.VAADIN_SERVER_JAR, fileSeparator + "vaadin-server" + fileSeparator, vaadinResourcePath)) {
                return;
            }

            /*
            Extract the VAADIN folder from vaadin-themes.jar into liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/html/VAADIN.
            This extracts all Vaadin themes.
             */
            if (extractVAADINFolder(zipDestinationPath, ControlPanelPortletUtil.VAADIN_THEMES_JAR, fileSeparator + "vaadin-themes" + fileSeparator, vaadinResourcePath)) {
                return;
            }

            /*
            Extract the VAADIN folder from vaadin-client-compiled.jar into liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/html/VAADIN.
            This extracts the default widget set.
            */
            if (extractVAADINFolder(zipDestinationPath, ControlPanelPortletUtil.VAADIN_CLIENT_COMPILED_JAR, fileSeparator + "vaadin-client-compiled" + fileSeparator, vaadinResourcePath)) {
                return;
            }

            /*
            * Copy vaadin-server.jar, vaadin-shared.jar, vaadin-shared-deps.jar, jsoup.jar into
            * liferay-portal-6.1.1-ce-ga2/tomcat-7.0.27/webapps/ROOT/WEB-INF/lib.
            * You can optionally remove the version numbers if you want to follow the Liferay standard.
            *
            * */

            Collection<VaadinFileInfo> vaadinFileInfos = ControlPanelPortletUtil.getVaadinFilesInfo();

            for (VaadinFileInfo fileInfo : vaadinFileInfos) {
                replaceFile(zipDestinationPath + fileInfo.getInnerSourcePath(), fileInfo.getPlace(), fileInfo.getName());
            }
            upgradeListener.updateComplete();
        } catch (Exception e) {
            log.warn("Exception while updating Vaadin version.", e);
            upgradeListener.updateFailed("Upgrade failed: " + e.getMessage());
        } finally {
            try {
                FileUtils.deleteDirectory(tmpDir);
            } catch (IOException e) {
                log.warn(e);
            }
        }
    }

    private void backupOldFiles() throws IOException {
        outputLog.log("Backup old vaadin files to " + backupPath);

        String vaadinResourcePath = ControlPanelPortletUtil.getVaadinResourceDir();
        File vaadinResource = new File(vaadinResourcePath);
        outputLog.log("Backup old vaadin resources : " + vaadinResourcePath + " to " + backupPath);
        String backupreSourcesPath = backupDir.getPath() + fileSeparator +  "resources";
        File backupreSourcesDir = new File(backupreSourcesPath);
        if (backupreSourcesDir.exists()) FileUtils.deleteDirectory(backupreSourcesDir);
        if(!backupreSourcesDir.mkdir()){
            outputLog.log("Can't backup resources. Can't create directory " + backupreSourcesPath);
        }else
        {
            FileUtils.copyDirectory(vaadinResource, backupreSourcesDir);
        }

        String backupFilesPath = backupPath + fileSeparator;

        File vaadin6Version = ControlPanelPortletUtil.get6VersionVaadinJarLocation();
        if (vaadin6Version.exists()) {
            outputLog.log("Backup vaadin.jar : " + vaadin6Version.getAbsolutePath());
            replaceFile(vaadin6Version.getParent() + fileSeparator, backupFilesPath, vaadin6Version.getName());
        }

        Collection<VaadinFileInfo> vaadinFileInfos = ControlPanelPortletUtil.getVaadinFilesInfo();
        StringBuffer sb = new StringBuffer();
        Boolean isExistsNotBackuped = false;

        for (VaadinFileInfo fileInfo : vaadinFileInfos) {
            try {
                replaceFile(fileInfo.getPlace(), backupFilesPath, fileInfo.getName());
            } catch (Exception ex) {
                sb.append(fileInfo.getName()).append(", ");
                isExistsNotBackuped = true;
            }
        }

        if (isExistsNotBackuped) {
            outputLog.log("Can't backup next files : " + sb.toString());
        }
    }

    public void restoreFromBackup() throws IOException {

        outputLog.log("Restore old vaadin files from " + backupPath);

        String vaadinResourcePath = ControlPanelPortletUtil.getVaadinResourceDir();
        File vaadinResource = new File(vaadinResourcePath);
        outputLog.log("Restore old vaadin resources : " + vaadinResourcePath + " to " + backupPath);
        String backupreSourcesPath = backupDir.getPath() + "/resources";
        File backupreSourcesDir = new File(backupreSourcesPath);
        if (!backupreSourcesDir.exists()) {
            outputLog.log("Can't restore resources. Can't find directory " + backupreSourcesPath);
        }else
        {
            FileUtils.copyDirectory(backupreSourcesDir, vaadinResource);
        }

//        File vaadin6Version = ControlPanelPortletUtil.get6VersionVaadinJarLocation();
//        if (vaadin6Version.exists()) {
//            outputLog.log("Backup vaadin.jar : " + vaadin6Version.getAbsolutePath());
//            FileUtils.copyFile(vaadin6Version, backupDir);
//        }

        String backupFilesPath = backupPath + "/";

        Collection<VaadinFileInfo> vaadinFileInfos = ControlPanelPortletUtil.getVaadinFilesInfo();
        StringBuffer sb = new StringBuffer();
        Boolean isExistsNotBackuped = false;

        for (VaadinFileInfo fileInfo : vaadinFileInfos) {
            try {
                replaceFile(backupFilesPath,fileInfo.getPlace() , fileInfo.getName());
            } catch (Exception ex) {
                sb.append(fileInfo.getName()).append(", ");
                isExistsNotBackuped = true;
            }
        }

        if (isExistsNotBackuped) {
            outputLog.log("Can't restore next files : " + sb.toString());
        }
    }

    private String exctractZipFile(File vaadinZipFile, String tmpPath) throws IOException {
        byte[] buf = new byte[1024];
        String zipDestinationPath = tmpPath +  fileSeparator +"unzip" + fileSeparator;
        File unzipDirectory = new File(zipDestinationPath);
        if (!unzipDirectory.mkdir()) {
            log.warn("Zip extract failed.");
            upgradeListener.updateFailed("Zip extract failed: Can not create directory " + zipDestinationPath);
            return null;
        }

        ZipInputStream zinstream = new ZipInputStream(new FileInputStream(vaadinZipFile.getAbsolutePath()));
        ZipEntry zentry = zinstream.getNextEntry();

        while (zentry != null) {
            String entryName = zentry.getName();
            if (zentry.isDirectory()) {
                File newFile = new File(zipDestinationPath + entryName);
                if (!newFile.mkdir()) {
                    break;
                }
                zentry = zinstream.getNextEntry();
                continue;
            }
            outputLog.log("Extracting " + entryName);
            FileOutputStream outstream = new FileOutputStream(zipDestinationPath + getFileNameWithoutVersion(entryName));
            int n;

            while ((n = zinstream.read(buf, 0, 1024)) > -1) {
                outstream.write(buf, 0, n);
            }

            outputLog.log("Successfully Extracted File Name : " + entryName);
            outstream.close();

            zinstream.closeEntry();
            zentry = zinstream.getNextEntry();
        }
        zinstream.close();

        return zipDestinationPath;
    }

    private void replaceFile(String sourceDir, String destinationDir, String fileName) throws IOException {
        String destinatoinFilePath = destinationDir + fileName;
        String sourceFilePath = sourceDir + fileName;

        outputLog.log("Copying files: " + sourceFilePath + " to " + destinatoinFilePath);
        File newJar = new File(sourceFilePath);
        File oldJar = new File(destinatoinFilePath);
        FileUtils.copyFile(newJar, oldJar, true);
    }

    private boolean extractVAADINFolder(String sourceDirPath, String jarName, String tmpFolderName, String destination) throws IOException {
        String vaadinJarFilePath = sourceDirPath + fileSeparator + jarName;
        JarFile vaadinJar = new JarFile(vaadinJarFilePath);
        String vaadinExtractedPath = sourceDirPath + tmpFolderName;
        outputLog.log("Extracting " + jarName);
        try {
            Enumeration<JarEntry> entries = vaadinJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                boolean extractSuccessful = ControlPanelPortletUtil
                        .extractJarEntry(vaadinJar, entry, vaadinExtractedPath);
                if (!extractSuccessful) {
                    outputLog.log("Extraction failed: " + entry.getName());
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("Extracting VAADIN folder failed.", e);
            upgradeListener.updateFailed("Extraction failed: " + e.getMessage());
            return true;
        } finally {
            vaadinJar.close();
        }

        String vaadinExtractedVaadinPath = vaadinExtractedPath + fileSeparator +  "VAADIN" + fileSeparator;
        File vaadinExtractedVaadin = new File(vaadinExtractedVaadinPath);
        if (!vaadinExtractedVaadin.exists()) {
            upgradeListener.updateFailed("Could not find " + vaadinExtractedVaadinPath);
            return true;
        }

        FileUtils.copyDirectory(vaadinExtractedVaadin, new File(destination));
        return false;
    }

    private String getFileNameWithoutVersion(String fileName) {
        return fileName.replaceAll("-[0-9.]+.*", ".jar");
    }
}

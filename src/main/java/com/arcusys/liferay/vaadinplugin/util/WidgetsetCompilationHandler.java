package com.arcusys.liferay.vaadinplugin.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import com.arcusys.vaadin.controlpanel.util.WidgetsetCompiler.CompileOutputConsumer;

public class WidgetsetCompilationHandler implements Runnable {
    private static Log log = LogFactoryUtil.getLog(WidgetsetCompilationHandler.class);

    private WidgetsetCompiler compiler;

    private String widgetset;
    private List<VaadinAddonInfo> includeAddons;
    private List<File> additionalDependencies;

    private ILog outputLog;

    public WidgetsetCompilationHandler(String widgetset, List<VaadinAddonInfo> includeAddons, List<File> additionalDependencies, ILog outputLog){
        this.widgetset = widgetset;
        this.includeAddons = includeAddons;
        this.additionalDependencies = additionalDependencies;
        this.outputLog = outputLog;
    }

    public void run() {
        File tmpDir = null;
        try {
            tmpDir = WidgetsetUtil.createTempDir();

            WidgetsetUtil.createWidgetset(tmpDir, widgetset, getIncludeWidgetsets());

            compiler = new WidgetsetCompiler(outputLog);
            compiler.setWidgetset(widgetset);
            compiler.setOutputDir(tmpDir.getAbsolutePath());
            compiler.setClasspathEntries(getClasspathEntries(tmpDir));

            try {
                compiler.compileWidgetset();
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }

            File compiledWidgetset = new File(tmpDir, widgetset);
            if (compiledWidgetset.exists() && compiledWidgetset.isDirectory()) {
                String ws = ControlPanelPortletUtil.getWidgetsetDir() + widgetset;
                WidgetsetUtil.rotateWidgetsetBackups(ws);
                WidgetsetUtil.backupOldWidgetset(ws);
                File destDir = new File(ws);

                outputLog.log("Copying widgetset from " + compiledWidgetset + " to " + destDir);

                System.out.println("Copying widgetset from " + compiledWidgetset + " to " + destDir);

                FileUtils.copyDirectory(compiledWidgetset, destDir);

                outputLog.log("Copying done");
                System.out.println("Copying done");
            }

        } catch (IOException e) {
            log.warn("Could not compile widgetsets.", e);
            System.out.println("Could not compile widgetsets. " + e.getMessage());
        } finally {
            try {
                System.out.println("Remove temp directory");
                FileUtils.deleteDirectory(tmpDir);
                System.out.println("Removing done...");
            } catch (IOException e) {
                log.warn("Could not delete temporary directory: " + tmpDir, e);
                System.out.println("Could not delete temporary directory: " + tmpDir + "  "  + e);
            }

            compilationFinished();
        }
    }
    private List<File> getClasspathEntries(File entry) {
        List<File> classpathEntries = new ArrayList<File>();
        classpathEntries.add(entry);

        // The vaadin-client-compiler JAR is located in the portal lib dir
        classpathEntries.add(ControlPanelPortletUtil.getVaadinClientCompilerJarLocation());

        // The vaadin-client JAR is located in the portal lib dir
        classpathEntries.add(ControlPanelPortletUtil.getVaadinClientJarLocation());

        // The vaadin-server JAR is located in the portal lib dir
        classpathEntries.add(ControlPanelPortletUtil.getVaadinServerJarLocation());

        // The vaadin-shared.jar is located in the portal lib dir
        classpathEntries.add(ControlPanelPortletUtil.getVaadinSharedJarLocation());

        // The vaadin-shared-deps.jar is located in the portal lib dir
        classpathEntries.add(ControlPanelPortletUtil.getVaadinSharedDepsJarLocation());

        // The ant.jar is located in the portal lib dir
        classpathEntries.add(ControlPanelPortletUtil.getAntJarLocation());

        for (VaadinAddonInfo addon : includeAddons) {
            classpathEntries.add(addon.getJarFile());
        }
        classpathEntries.addAll(additionalDependencies);

        return classpathEntries;
    }

    private Set<String> getIncludeWidgetsets() {
        Set<String> widgetsets = new HashSet<String>();
        if (includeAddons != null) {
            for (VaadinAddonInfo addon : includeAddons) {
                widgetsets.addAll(addon.getWidgetsets());
            }
        }
        widgetsets.add("com.vaadin.DefaultWidgetSet");
        return widgetsets;
    }

    public void terminate() {
        compiler.terminate();
        compilationFinished();
    }

    public void compilationFinished() {
    }
}

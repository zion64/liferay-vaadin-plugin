package com.arcusys.liferay.vaadinplugin;

/*
 * #%L
 * Liferay Vaadin Plugin
 * %%
 * Copyright (C) 2010 - 2013 Vaadin Ltd.
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

import com.arcusys.liferay.vaadinplugin.ui.ChangeVersionWindow;
import com.arcusys.liferay.vaadinplugin.ui.DetailsWindow;
import com.arcusys.liferay.vaadinplugin.util.*;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.arcusys.liferay.vaadinplugin.ui.OutputConsole;
import com.arcusys.liferay.vaadinplugin.ui.AdditionalDependenciesWindow;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;


@SuppressWarnings("serial")
public class ControlPanelUI extends UI
{
    private static final String WARNING_UPGRADE_VAADIN_VERSION_NOT_FOUND = "Could not determine the newest Vaadin version. Please download it manually from "
            + ControlPanelPortletUtil.VAADIN_DOWNLOAD_URL;
    private static final String WARNING_UPGRADE_VAADIN_VERSION = "Changing the Vaadin version will affect all the portlets. Two Vaadin versions can't be used at the same time.";
    private static final int POLLING_INTERVAL_MS = 500;

    private VerticalLayout mainLayout;
    private Panel settingsPanel;
    private FormLayout settingsLayout;

    private ProgressIndicator versionUpgradeProgressIndicator;

    private Button changeVersionButton;

    private Button updateVaadinVersionButton;

    private HorizontalLayout vaadinVersionLayout;

    private Label activeWidgetsetLabel;
    private Label addonLibDirLabel;

    private Button refreshButton;
    private Set<VaadinAddonInfo> selectedAddons;
    private OptionGroup includeAddonsOptionGroup;

    private VerticalLayout addonsListHolder;
    private Label addonsNotFoundLabel;
    private List<File> additionalDependencies = null;
    private OutputConsole outputConsole;

    private Label additionalDependenciesLabel;
    private List<File> includedDependencies = null;

    private boolean needsUpdating;

    private Button additionalDependenciesButton;

    private Button compileWidgetsetButton;

    private static final Log log = LogFactoryUtil.getLog(ControlPanelUI.class);

    private WidgetsetCompilationHandler compiler;

    private Button terminateCompilationButton;

    private ProgressIndicator compilationProgressIndicator;

    private NewestVaadinVersion newestVaadinVersion;

    private VaadinUpdater vaadinUpdater;
    private ILog outputLog;

    private Button detailsButton;

    @Override
    protected void init(final VaadinRequest request) {
        //checkResources();

        newestVaadinVersion = new NewestVaadinVersion();

        onRequestStart((PortletRequest) request);

        createUI();

        outputLog = new ILog() {
            @Override
            public void log(String message) {
                try {
                    getSession().getLockInstance().lock();
                    outputConsole.log(message);
                } finally {
                    getSession().getLockInstance().unlock();
                }
            }
        };

        refreshAddons();
    }

    private void createUI() {
        //create main layout
        mainLayout = new VerticalLayout();
        mainLayout.setWidth("720px");
        mainLayout.setSpacing(true);

        setContent(mainLayout);

        //create Setting layout
        settingsPanel = new Panel("Settings");
        settingsLayout = new FormLayout();
        settingsPanel.setContent(settingsLayout);

        versionUpgradeProgressIndicator = createProgressIndicator();

        changeVersionButton = createChangeVersionButton();
        updateVaadinVersionButton = createUpdateVaadinVersionButton();

        String vaadinNewestVersion = newestVaadinVersion.getVersion();
        //add details
        detailsButton = createDetailsButton();

        vaadinVersionLayout = createVaadinVersionLayout(vaadinNewestVersion, changeVersionButton, updateVaadinVersionButton, versionUpgradeProgressIndicator, detailsButton );
        settingsLayout.addComponent(vaadinVersionLayout);


        activeWidgetsetLabel = createActiveWidgetsetLabel();
        settingsLayout.addComponent(activeWidgetsetLabel);

        // Add-on selection
        HorizontalLayout addonDirectoryLayout = createAddonDirectoryLayout();
        settingsLayout.addComponent(addonDirectoryLayout);

        //Add-on included
        includeAddonsOptionGroup = createIncludeAddonsList();
        addonsListHolder = createIncludeAddonsListLayout(includeAddonsOptionGroup);
        settingsLayout.addComponent(addonsListHolder);

        //addition dependencies
        additionalDependenciesLabel = createAdditionalDependenciesLabel();
        settingsLayout.addComponent(additionalDependenciesLabel);

        mainLayout.addComponent(settingsPanel);

        additionalDependenciesButton = createAdditionalDependenciesButton();
        mainLayout.addComponent(additionalDependenciesButton);

        //Compilation layout
        HorizontalLayout compilationlayout = new HorizontalLayout();
        compileWidgetsetButton = createCompileWidgetsetButton();
        compilationlayout.addComponent(compileWidgetsetButton);
        terminateCompilationButton = createTerminateCompilationButton();
        compilationlayout.addComponent(terminateCompilationButton);
        compilationProgressIndicator = createProgressIndicator();
        compilationlayout.addComponent(compilationProgressIndicator);
        compilationlayout.setComponentAlignment(compilationProgressIndicator, Alignment.MIDDLE_LEFT);
        settingsLayout.addComponent(compilationlayout);

        //Output console
        outputConsole = createOutputConsole();
        mainLayout.addComponent(outputConsole);

        addonsNotFoundLabel = createAddonsNotFoundLabel();
    }



    private ProgressIndicator createProgressIndicator() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setIndeterminate(true);
        progressIndicator.setPollingInterval(POLLING_INTERVAL_MS);
        progressIndicator.setEnabled(false);
        progressIndicator.setVisible(false);
        return progressIndicator;
    }

    private Button createChangeVersionButton() {
        Button button = new Button("Change version");
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                addWindow(new ChangeVersionWindow());
            }
        });
        return button;
    }

    private Button createDetailsButton() {
        Button button = new Button("Details");
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                addWindow(new DetailsWindow());
            }
        });
        return button;
    }

    private Button createUpdateVaadinVersionButton() {
        Button button = new Button("Upgrade");
        button.setImmediate(true);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addClickListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {

                if (!newestVaadinVersion.wasFound()) {
                    Notification.show(
                            WARNING_UPGRADE_VAADIN_VERSION_NOT_FOUND,
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }

                String downloadLocation = newestVaadinVersion.getLocation();

                outputLog.log("Location for download: " + downloadLocation);

                try {
                    addWindow(new WarningWindow(downloadLocation));
                } catch (Exception ex) {
                    outputLog.log(ex.getMessage());
                }
            }
        });
        return button;
    }

    private HorizontalLayout createVaadinVersionLayout(String newestVersion, Button changeVersionButton, Button updateVaadinVersionButton, ProgressIndicator versionUpgradeProgressIndicator,Button detailsButton) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setCaption("Vaadin Jar Version");

        Label vaadinVersionLabel = new Label();
        vaadinVersionLabel.setSizeUndefined();
        String version = null;
        try {
            version = ControlPanelPortletUtil.getPortalVaadinVersion();
        } catch (IOException e) {
            log.warn("vaadin-server.jar couldn't be read.", e);
        }

        if(version == null){
        try {
            version = ControlPanelPortletUtil.getPortalVaadin6Version();
        } catch (IOException e) {
            log.warn("vaadin.jar couldn't be read.", e);
        }
        }

        if (version == null) {
            version = "could not be determined";
        }
        vaadinVersionLabel.setValue(version);
        layout.addComponent(vaadinVersionLabel);

        layout.addComponent(detailsButton);

        layout.addComponent(changeVersionButton);

        Label newestVaadinVersionLabel = new Label();
        newestVaadinVersionLabel.setSizeUndefined();
        newestVaadinVersionLabel.setValue("(newest stable version: " + newestVersion + ")");
        layout.addComponent(newestVaadinVersionLabel);

        if (!version.equals(newestVersion)) {
            layout.addComponent(updateVaadinVersionButton);
        }
        layout.addComponent(versionUpgradeProgressIndicator);
        return layout;
    }

    private Label createActiveWidgetsetLabel() {
        Label activeWidgetsetLabel = new Label();
        activeWidgetsetLabel.setCaption("Active Widget Set");
        String value = ControlPanelPortletUtil.getPortalWidgetset();
        activeWidgetsetLabel.setValue(value);
        return activeWidgetsetLabel;
    }

    private HorizontalLayout createAddonDirectoryLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setCaption("Add-on Directory");
        addonLibDirLabel = createAddonLibDirLabel();
        layout.addComponent(addonLibDirLabel);
        refreshButton = createRefreshButton();
        layout.addComponent(refreshButton);
        layout.setComponentAlignment(refreshButton, Alignment.MIDDLE_LEFT);

        return layout;
    }

    private Label createAddonLibDirLabel() {
        Label addonLibDirLabel = new Label();
        addonLibDirLabel.setSizeUndefined();
        String value = ControlPanelPortletUtil.getPortalLibDir();
        addonLibDirLabel.setValue(value);
        return addonLibDirLabel;
    }

    private Button createRefreshButton() {
        Button button = new Button("[re-scan]", new Button.ClickListener() {

            @SuppressWarnings("unchecked")
            public void buttonClick(Button.ClickEvent event) {
                selectedAddons = (Set<VaadinAddonInfo>) includeAddonsOptionGroup
                        .getValue();
                refreshAddons();
            }
        });
        button.setStyleName(BaseTheme.BUTTON_LINK);
        return button;
    }

    private void refreshAddons() {

        final List<VaadinAddonInfo> addons = WidgetsetUtil
                .getAvailableWidgetSets(new File(ControlPanelPortletUtil
                        .getPortalLibDir()));
        includeAddonsOptionGroup.removeAllItems();

        if (addons.isEmpty()) {
            // Replace the option group with a message if no addons
            addonsListHolder.removeAllComponents();
            addonsListHolder.addComponent(addonsNotFoundLabel);
        } else {
            for (VaadinAddonInfo addon : addons) {
                includeAddonsOptionGroup.addItem(addon);
            }
            includeAddonsOptionGroup.setValue(selectedAddons);

            addonsListHolder.removeAllComponents();
            addonsListHolder.addComponent(includeAddonsOptionGroup);
        }

        // Note: the GWT lib dir entries don't have to be excluded as they're
        // not included in the libs returned by getLibs();
        List<File> exclude = new ArrayList<File>();
        exclude.add(ControlPanelPortletUtil.getVaadinServerJarLocation());
        for (VaadinAddonInfo addon : addons) {
            exclude.add(addon.getJarFile());
        }

        additionalDependencies = ControlPanelPortletUtil.getLibs(exclude);
    }

    private OutputConsole createOutputConsole() {
        OutputConsole outputConsole = new OutputConsole("Output Console");
        outputConsole.setWidth("100%");
        outputConsole.setHeight("400px");

        return outputConsole;
    }

    private Label createAdditionalDependenciesLabel() {
        Label dependencyList = new Label("", ContentMode.HTML);
        dependencyList.setCaption("Other Dependencies");
        if (includedDependencies != null) {
            String value = "";
            if (includedDependencies.size() > 0) {
                for (File file : includedDependencies) {
                    if (!value.equals("")) {
                        value += "<br/>";
                    }
                    value += file.getName();
                }
            } else {
                value = "none";
            }

            dependencyList.setValue(value);
        }
        return dependencyList;
    }

    private OptionGroup createIncludeAddonsList() {
        OptionGroup includeAddonsOptionGroup = new OptionGroup();
        includeAddonsOptionGroup.setMultiSelect(true);
        includeAddonsOptionGroup.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                needsUpdating = true;
            }
        });
        return includeAddonsOptionGroup;
    }

    private VerticalLayout createIncludeAddonsListLayout(OptionGroup includeAddonsOptionGroup) {
        VerticalLayout addonsListHolder = new VerticalLayout();
        addonsListHolder.setCaption("Select Add-ons");

        addonsListHolder.addComponent(includeAddonsOptionGroup);
        return addonsListHolder;
    }

    private Button createAdditionalDependenciesButton() {
        Button button = new Button("Manage Additional Dependencies", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                showAdditionalDependenciesWindow();
            }
        });
        button.setStyleName(BaseTheme.BUTTON_LINK);
        return button;
    }

    private void showAdditionalDependenciesWindow() {
        final AdditionalDependenciesWindow additionalDependenciesWindow = new AdditionalDependenciesWindow(
                additionalDependencies, includedDependencies);
        additionalDependenciesWindow.setModal(true);
        additionalDependenciesWindow.addCloseListener(new Window.CloseListener() {

            public void windowClose(Window.CloseEvent e) {
                additionalDependenciesButton.setEnabled(true);
                updateDependencies(additionalDependenciesWindow.getAdditionalDependencies());
            }
        });
        addWindow(additionalDependenciesWindow);
        additionalDependenciesWindow.center();
        additionalDependenciesButton.setEnabled(false);
    }

    private void updateDependencies(List<File> dependencies) {
        includedDependencies = dependencies;
        needsUpdating = true;

        // update the list of selected dependencies
        Label newDependenciesLabel = createAdditionalDependenciesLabel();
        settingsLayout.replaceComponent(additionalDependenciesLabel, newDependenciesLabel);
        additionalDependenciesLabel = newDependenciesLabel;
    }

    private Label createAddonsNotFoundLabel() {
        return new Label("<i>Vaadin add-ons not found from the add-on directory</i>.", ContentMode.HTML);
    }

    private Button createCompileWidgetsetButton() {
        return new Button("Compile Widget Set",
                new Button.ClickListener() {

                    public void buttonClick(Button.ClickEvent event) {
                        setCompilationModeEnabled(true);
                        outputConsole.clear();

                        compiler = new WidgetsetCompilationHandler(activeWidgetsetLabel.getValue(), getIncludeAddons(),includedDependencies, outputLog
                        ) {
                            @Override
                            public void compilationFinished() {
                                System.out.println("Compilation has been finished successfully");
                                getUI().getSession().getLockInstance().lock();
                                outputLog.log("Compilation has been finished successfully");

                                try {
                                    setCompilationModeEnabled(false);

                                } finally {
                                    getUI().getSession().getLockInstance().unlock();
                                }
                            }
                        };
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(compiler);
                    }
                });
    }

    private void setCompilationModeEnabled(boolean enabled) {
        setButtonsEnabled(!enabled);
        compilationProgressIndicator.setEnabled(enabled);
        compilationProgressIndicator.setVisible(enabled);
        terminateCompilationButton.setVisible(enabled);
    }

    @SuppressWarnings("unchecked")
    private List<VaadinAddonInfo> getIncludeAddons() {
        List<VaadinAddonInfo> addons = new ArrayList<VaadinAddonInfo>();
        for (VaadinAddonInfo addon : (Set<VaadinAddonInfo>) includeAddonsOptionGroup
                .getValue()) {
            addons.add(addon);
        }
        return addons;
    }

    private void setButtonsEnabled(boolean enabled) {
        refreshButton.setEnabled(enabled);
        includeAddonsOptionGroup.setEnabled(enabled);
        additionalDependenciesButton.setEnabled(enabled);
        compileWidgetsetButton.setEnabled(enabled);
        changeVersionButton.setEnabled(enabled);
        updateVaadinVersionButton.setEnabled(enabled);
        detailsButton.setEnabled(enabled);
    }

    private Button createTerminateCompilationButton() {
        Button button = new Button("Cancel", new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                compiler.terminate();
            }
        });
        button.setVisible(false);
        return button;
    }


    public void onRequestStart(PortletRequest request) {
        if (includedDependencies == null || selectedAddons == null) {
            PortletPreferences preferences = request.getPreferences();
            String portletResource = ParamUtil.getString(request, "portletResource");
            if (Validator.isNotNull(portletResource)) {
                try {
                    preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);
                } catch (SystemException e) {
                    log.warn(e);
                } catch (PortalException e) {
                    log.warn(e);
                }
            }
            if (includedDependencies == null) {
                loadAdditionalDependencies(preferences);
            }
            if (selectedAddons == null) {
                loadSelectedAddons(preferences);
            }
        }
    }

    private void loadAdditionalDependencies(PortletPreferences preferences) {
        if (includedDependencies == null) {
            includedDependencies = new ArrayList<File>();
        }
        String dependencies = preferences.getValue("additionalDependencies", null);

        if (dependencies != null) {
            File dir = new File(ControlPanelPortletUtil.getPortalLibDir());
            if (dir.exists()) {
                for (String fileName : dependencies.split(";")) {
                    fileName = fileName.trim();
                    if (!"".equals(fileName)) {
                        File file = new File(dir, fileName);
                        if (file.exists()) {
                            includedDependencies.add(file);
                        }
                    }
                }
            }
        }
        log.info("Additional dependencies retrieved.");
    }

    private void loadSelectedAddons(PortletPreferences preferences) {
        if (selectedAddons == null) {
            selectedAddons = new HashSet<VaadinAddonInfo>();
        }
        String addonStrings = preferences.getValue("selectedAddons", null);
        if (addonStrings != null) {
            List<VaadinAddonInfo> allAddons = WidgetsetUtil
                    .getAvailableWidgetSets(new File(ControlPanelPortletUtil
                            .getPortalLibDir()));
            for (String addonString : addonStrings.split(";")) {
                addonString = addonString.trim();
                if (!"".equals(addonString)) {
                    for (VaadinAddonInfo addon : allAddons) {
                        if (addonString.equals(addon.toString())) {
                            selectedAddons.add(addon);
                            break;
                        }
                    }
                }
            }
        }
        log.info("Selected addons retrieved.");
    }


    private void downloadVaadin(String downloadLocation) {

        vaadinUpdater = new VaadinUpdater(downloadLocation,
                new VaadinUpdater.UpgradeListener() {

                    public void updateComplete() {
                        outputLog.log("Vaadin version upgraded successfully.");
                        outputLog.log("Don't forget to compile widgetset.");
                        done(true);
                    }

                    private void done(boolean success) {
                        refreshVersionInfo();
                        // Stop polling
                        versionUpgradeProgressIndicator.setEnabled(false);
                        versionUpgradeProgressIndicator.setVisible(false);
                        setButtonsEnabled(true);

                        vaadinUpdater = null;
                    }

                    public void updateFailed(String message) {
                        outputLog.log(message);
                        try{
                        vaadinUpdater.restoreFromBackup();
                        }catch (Exception ex)
                        {
                            outputLog.log("ERROR: Can't restore files. Exception: " + ex.getMessage());
                        }
                        done(false);
                    }
                }, outputLog);

        versionUpgradeProgressIndicator.setEnabled(true);
        versionUpgradeProgressIndicator.setVisible(true);
        setButtonsEnabled(false);

        new Thread(vaadinUpdater).start();
    }

    private class WarningWindow extends Window {

        public WarningWindow(final String downloadUrl) {
            super("Warning!");
            setModal(true);

            VerticalLayout layout = new VerticalLayout();
            setContent(layout);
            layout.setSpacing(true);
            layout.setWidth("350px");

            layout.addComponent(new Label(WARNING_UPGRADE_VAADIN_VERSION, ContentMode.HTML));

            layout.addComponent(new Label( "After updating, you will need to redeploy all portlets using Vaadin. Otherwise, the portlets might fail randomly because of version conflicts."));

            List<Portlet> detectedVaadinPortlets = new ArrayList<Portlet>();
            List<Portlet> portlets = PortletLocalServiceUtil.getPortlets();
            for (Portlet portlet : portlets) {
                if (isVaadinPortlet(portlet)) {
                    detectedVaadinPortlets.add(portlet);
                }
            }

            if (!detectedVaadinPortlets.isEmpty()) {
                StringBuilder b = new StringBuilder(
                        "You seem to have at least ");
                b.append(detectedVaadinPortlets.size());
                b.append(" portlet");
                if (detectedVaadinPortlets.size() != 1) {
                    b.append('s');
                }
                b.append(" using Vaadin:<ul>");
                for (Portlet portlet : detectedVaadinPortlets) {
                    b.append("<li>").append(portlet.getDisplayName())
                            .append("</li>");
                }
                b.append("</ul>");
                layout.addComponent(new Label(b.toString(), ContentMode.HTML));
            } else {
                layout.addComponent(new Label(
                        "No portlets using Vaadin have been detected."));
            }
            layout.addComponent(new Label(
                    "You Vaadin portlet might not be detected if it uses a custom Portlet class."));

            HorizontalLayout buttonLayout = new HorizontalLayout();
            buttonLayout.setSpacing(true);

            Button buttonUpgrade = new Button("Change version");
            buttonUpgrade.setImmediate(true);
            buttonUpgrade.addClickListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    close();

                    downloadVaadin(downloadUrl);

                }
            });
            buttonLayout.addComponent(buttonUpgrade);

            Button buttonCancel = new Button("Cancel");
            buttonCancel.setImmediate(true);
            buttonCancel.addClickListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    close();
                }
            });
            buttonLayout.addComponent(buttonCancel);

            layout.addComponent(buttonLayout);
        }

        private boolean isVaadinPortlet(Portlet portlet) {
            if(portlet == null) return false;

            if (isVaadin6Portlet(portlet)) return true;
            if (isVaadin7Portlet(portlet)) return true;

            Map<String, String> initParams = portlet.getInitParams();
            return initParams.containsKey("application");
        }

        private boolean isVaadin6Portlet(Portlet portlet) {
            String portletClass = portlet.getPortletClass();
             return  portletClass.startsWith("com.vaadin.terminal.gwt.server");
        }

        private boolean isVaadin7Portlet(Portlet portlet) {
            String portletClass = portlet.getPortletClass();
            return portletClass.startsWith("com.vaadin.server.VaadinPortlet");
        }
    }

    public void showWarningWindow(String downloadUrl) {
        addWindow(new WarningWindow(downloadUrl));
    }

    private void refreshVersionInfo() {
        HorizontalLayout newVersionLayout = createVaadinVersionLayout(newestVaadinVersion.getVersion(), changeVersionButton, updateVaadinVersionButton, versionUpgradeProgressIndicator, detailsButton);
        settingsLayout.replaceComponent(vaadinVersionLayout, newVersionLayout);

        vaadinVersionLayout = newVersionLayout;
    }
}

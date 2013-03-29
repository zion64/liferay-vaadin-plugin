package com.arcusys.liferay.vaadinplugin.ui;

import com.arcusys.liferay.vaadinplugin.ControlPanelUI;
import com.arcusys.liferay.vaadinplugin.util.ControlPanelPortletUtil;
import com.arcusys.liferay.vaadinplugin.util.LinkParser;
import com.arcusys.liferay.vaadinplugin.util.VaadinVersion;
import com.arcusys.liferay.vaadinplugin.util.VaadinVersionFetcher;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class ChangeVersionWindow extends Window {
    private static final Log log = LogFactoryUtil.getLog(ChangeVersionWindow.class);

    private static final String VERSION_PROPERTY = "name";
    private static final String RELEASE_TYPE_PROPERTY = "releaseType";

    private Thread versionFetch = new Thread() {
        @Override
        public void run() {
            try {

                Collection<VaadinVersion.VaadinReleaseType> releaseTypesCollection = new ArrayList<VaadinVersion.VaadinReleaseType>();
                releaseTypesCollection.add(VaadinVersion.VaadinReleaseType.release);
                releaseTypesCollection.add(VaadinVersion.VaadinReleaseType.nightly);
                releaseTypesCollection.add(VaadinVersion.VaadinReleaseType.prerelease);

                VaadinVersionFetcher fetcher  = new VaadinVersionFetcher();

                List<VaadinVersion> versionList =fetcher.fetchVersionList(releaseTypesCollection);

                getUI().getSession().getLockInstance().lock();
                    beanItemContainer.addAll(versionList);
                    updateState(true);
                getUI().getSession().getLockInstance().unlock();
            } catch (Exception e) {
                log.warn("Version list could not be downloaded", e);
                getUI().getSession().getLockInstance().lock();

                layout.removeAllComponents();
                layout.addComponent(new Label( "Version list could not be downloaded: " + e.getMessage()));
                getUI().getSession().getLockInstance().unlock();
            } finally {
                // Release memory
                versionFetch = null;
            }
        }

   /* private List<VaadinVersion> fetchVersionList(Collection<VaadinReleaseType> versiontypes) {
        LinkParser parser = new LinkParser();
        List<VaadinVersion> vaadinVersions = new ArrayList<VaadinVersion>();
        for(VaadinReleaseType type : versiontypes){
        try {
            String vaadinMajorVersionListUrl = ControlPanelPortletUtil.VAADIN_DOWNLOAD_URL + type + "/";
            List<LinkParser.VersionData> majorVersions = getVersions(parser, vaadinMajorVersionListUrl, VAADIN_MAJOR_VERSION);

            List<LinkParser.VersionData> minorVersions = new ArrayList<LinkParser.VersionData>();

            if(type == VaadinReleaseType.prerelease){
                List<LinkParser.VersionData> versions = new ArrayList<LinkParser.VersionData>();
                for(LinkParser.VersionData version : majorVersions){
                    versions.addAll(getVersions(parser, version.getUrl(), version.getVersion()));
                }

                majorVersions = versions;
            }

            for(LinkParser.VersionData version : majorVersions){
                minorVersions.addAll(getVersions(parser, version.getUrl(), version.getVersion()));
            }

            for(LinkParser.VersionData versionData : minorVersions){
                String zipName = "vaadin-all-" + versionData.getVersion() + ".zip";
                VaadinVersion vaadinVersion = new VaadinVersion(versionData.getVersion(), type,versionData.getUrl() + zipName, versionData.getDate());
                if(vaadinVersion.isSupported()) vaadinVersions.add(vaadinVersion);
            }
        }
        catch (Exception e)
        {
            Notification.show("Can't fetch " + type  + " versions", Notification.Type.ERROR_MESSAGE);
        }
        }

        Collections.sort(vaadinVersions, new Comparator<VaadinVersion>(){
            @Override
            public int compare(VaadinVersion o1, VaadinVersion o2) {
                if(o1 == null) return -1;
                if(o2 == null) return 1;

                String vers1 = o1.getVersion().substring(0,5);
                String vers2 = o2.getVersion().substring(0,5);

                if(vers1.compareTo(vers2) == 0){

                if(o1.getReleaseDate() != null && o2.getReleaseDate() != null){
                    return o1.getReleaseDate().compareTo(o2.getReleaseDate());
                }
                else {
                    return o1.getVersion().compareTo(o2.getVersion());
                }
                }else
                {
                    return o1.getVersion().compareTo(o2.getVersion());
                }

            }
        });
        return vaadinVersions;
    }*/

    };

    private final VerticalLayout layout = new VerticalLayout();
    private final BeanItemContainer<VaadinVersion> beanItemContainer = new BeanItemContainer<VaadinVersion>(
            VaadinVersion.class);

    private final OptionGroup includeVersions = new OptionGroup(
            "Also include non-stable versions", EnumSet.of(
            VaadinVersion.VaadinReleaseType.prerelease, VaadinVersion.VaadinReleaseType.nightly));

    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final ComboBox versionSelection = new ComboBox("Select version",
            beanItemContainer);

    private final Button changeVersionButton = new Button("Change version",
            new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    VaadinVersion version = (VaadinVersion) versionSelection
                            .getValue();
                    if (version == null) {

                        Notification.show("Please select a version");
                        return;
                    }

                    ControlPanelUI mainUI = (ControlPanelUI) getUI();
                    mainUI.showWarningWindow(version.getDownloadUrl());
                    close();
                }
            });
    private final Button cancelButton = new Button("Cancel",
            new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    close();
                }
            });

    public ChangeVersionWindow() {
        super("Change Vaadin version");
        setModal(true);
        setSizeUndefined();

        layout.setMargin(true);
        layout.setSizeUndefined();
        layout.setSpacing(true);

        setContent(layout);

        progressIndicator.setCaption("Fetching version list");

        includeVersions.setItemCaptionMode(ComboBox.ItemCaptionMode.ID);

        includeVersions.setMultiSelect(true);
        includeVersions.setValue(Collections
                .singleton(VaadinVersion.VaadinReleaseType.release));
        includeVersions.setImmediate(true);
        includeVersions.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                updateFilter();
            }
        });

        versionSelection.setItemCaptionPropertyId(VERSION_PROPERTY);
        versionSelection.setNullSelectionAllowed(false);
        versionSelection.setRequired(true);

        versionFetch.start();

        layout.addComponent(includeVersions);
        layout.addComponent(versionSelection);

        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(true);
        buttonRow.addComponent(changeVersionButton);
        buttonRow.addComponent(cancelButton);

        layout.addComponent(buttonRow);

        updateState(false);

        updateFilter();
    }

    private void updateFilter() {
        @SuppressWarnings("unchecked")
        final Collection<VaadinVersion.VaadinReleaseType> types = (Collection<VaadinVersion.VaadinReleaseType>) includeVersions
                .getValue();
        beanItemContainer.removeAllContainerFilters();
        beanItemContainer.addContainerFilter(new Container.Filter() {
            public boolean passesFilter(Object itemId, Item item)
                    throws UnsupportedOperationException {
                Object releaseType = item
                        .getItemProperty(RELEASE_TYPE_PROPERTY).getValue();
                return releaseType == VaadinVersion.VaadinReleaseType.release
                        || types.contains(releaseType);
            }

            public boolean appliesToProperty(Object propertyId) {
                return RELEASE_TYPE_PROPERTY.equals(propertyId);
            }

        });
    }

    private void updateState(boolean enabled) {
        versionSelection.setEnabled(enabled);
        includeVersions.setEnabled(enabled);
        changeVersionButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);

        if (enabled && progressIndicator.getParent() != null) {
            layout.removeComponent(progressIndicator);
        } else if (!enabled && progressIndicator.getParent() == null) {
            layout.addComponent(progressIndicator, 0);
        }
    }

}
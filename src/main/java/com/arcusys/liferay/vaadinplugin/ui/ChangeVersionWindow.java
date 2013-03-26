package com.arcusys.liferay.vaadinplugin.ui;

import com.arcusys.liferay.vaadinplugin.ControlPanelUI;
import com.arcusys.liferay.vaadinplugin.util.ControlPanelPortletUtil;
import com.arcusys.liferay.vaadinplugin.util.LinkParser;
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
    private static final Log log = LogFactoryUtil
            .getLog(ChangeVersionWindow.class);

    private static final String VERSION_PROPERTY = "name";
    private static final String RELEASE_TYPE_PROPERTY = "releaseType";
    private  static final String VAADIN_MAJOR_VERSION = "7";

    private enum VaadinReleaseType {
        nightly, prerelease, release
    }

    public final class VaadinVersion {
        private final String version;
        private final String downloadUrl;
        private final VaadinReleaseType releaseType;
        private final DateTime releaseDate;
        private final String name;
        private final DateTime startDate = new DateTime(2012, 10, 5, 11, 0, 0);

        public VaadinVersion(String version, VaadinReleaseType releaseType, String downloadUrl, DateTime releaseDate) {
            this.downloadUrl = downloadUrl;
            this.version = version;
            this.releaseType = releaseType;
            this.releaseDate = releaseDate;
            this.name = version + " (" + releaseDate.toString("dd-MM-yyyy hh:mm") + ")";
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public VaadinReleaseType getReleaseType() {
            return releaseType;
        }

        public String getVersion() {
            return version;
        }

        public String getName(){
            return  name;
        }

        public DateTime getReleaseDate() {
            return releaseDate;
        }

        public boolean isSupported() {
            String[] versionParts = version.split("\\.");
            String majorVersion = versionParts[0];

            // Other major versions than 7 not supported
            if (!VAADIN_MAJOR_VERSION.equals(majorVersion)) return false;

            //releases before 7.0.0.nightly-0ce6f77ab353c1bc1decc7f02203cd07a5ff5d27/ 13-Sep-2012 12:52 not supported
            if (releaseDate.isBefore(startDate)) return false;

            return true;
        }
    }

    private Thread versionFetch = new Thread() {
        @Override
        public void run() {
            try {

                Collection<VaadinReleaseType> releaseTypesCollection = new ArrayList<VaadinReleaseType>();
                releaseTypesCollection.add(VaadinReleaseType.release);
                releaseTypesCollection.add(VaadinReleaseType.nightly);
                releaseTypesCollection.add(VaadinReleaseType.prerelease);

                List<VaadinVersion> versionList = fetchVersionList(releaseTypesCollection);

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

    private List<VaadinVersion> fetchVersionList(Collection<VaadinReleaseType> versiontypes) {
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
    }

    private List<LinkParser.VersionData> getVersions(LinkParser parser, String versionListUrl, String majorVersion) throws IOException {
        String majorVerisonResponse = getResponseString(versionListUrl);

        List<LinkParser.VersionData> versionsAndUrls = parser.getVaadinVersionsAndDates(majorVerisonResponse, majorVersion, versionListUrl);

        return versionsAndUrls;
    }

    private String getResponseString(String downloadUrl) throws IOException {
        URL url;
        InputStream inputStream = null;
        BufferedReader dataInputStream = null;

        try{
        url = new URL(downloadUrl);
        inputStream = url.openStream();
        dataInputStream = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        StringBuffer response = new StringBuffer();
        while ((line = dataInputStream.readLine()) != null) {
            response.append(line);
        }

        return response.toString();

        }finally {
            ControlPanelPortletUtil.close(dataInputStream);
            ControlPanelPortletUtil.close(inputStream);
        }
    }
    };

    private final VerticalLayout layout = new VerticalLayout();
    private final BeanItemContainer<VaadinVersion> beanItemContainer = new BeanItemContainer<VaadinVersion>(
            VaadinVersion.class);

    private final OptionGroup includeVersions = new OptionGroup(
            "Also include non-stable versions", EnumSet.of(
            VaadinReleaseType.prerelease, VaadinReleaseType.nightly));

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
                .singleton(VaadinReleaseType.release));
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
        final Collection<VaadinReleaseType> types = (Collection<VaadinReleaseType>) includeVersions
                .getValue();
        beanItemContainer.removeAllContainerFilters();
        beanItemContainer.addContainerFilter(new Container.Filter() {
            public boolean passesFilter(Object itemId, Item item)
                    throws UnsupportedOperationException {
                Object releaseType = item
                        .getItemProperty(RELEASE_TYPE_PROPERTY).getValue();
                return releaseType == VaadinReleaseType.release
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
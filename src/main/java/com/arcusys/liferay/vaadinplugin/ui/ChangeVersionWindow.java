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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class ChangeVersionWindow extends Window {
    private static final Log log = LogFactoryUtil
            .getLog(ChangeVersionWindow.class);

    private static final String VERSION_PROPERTY = "version";
    private static final String RELEASE_TYPE_PROPERTY = "releaseType";
    private  static final String VAADIN_MAJOR_VERSION = "7";

    private enum VaadinReleaseType {
        nightly, prerelease, release
    }

    public final class VaadinVersion {
        private final String version;
        private final String downloadUrl;
        private final VaadinReleaseType releaseType;

        public VaadinVersion(String version, VaadinReleaseType releaseType, String downloadUrl) {
            this.downloadUrl = downloadUrl;
            this.version = version;
            this.releaseType = releaseType;
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

        public boolean isSupported() {
            String[] versionParts = version.split("\\.");
            String majorVersion = versionParts[0];
            if (!VAADIN_MAJOR_VERSION.equals(majorVersion)) {
                // Other major versions than 7 not supported
                return false;
            }

            if (version.contains("alpha")) return false;

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

    private List<VaadinVersion> fetchVersionList(Collection<VaadinReleaseType> versiontypes) throws IOException {
        LinkParser parser = new LinkParser();
        List<VaadinVersion> vaadinVersions = new ArrayList<VaadinVersion>();
        for(VaadinReleaseType type : versiontypes){
        try {
            String vaadinMajorVersionListUrl = ControlPanelPortletUtil.VAADIN_DOWNLOAD_URL + type + "/";
            HashMap<String, String> majorVersions = getVersions(parser, vaadinMajorVersionListUrl, VAADIN_MAJOR_VERSION);

            HashMap<String, String> minorVersions = new HashMap<String, String>();

            if(type == VaadinReleaseType.prerelease){
                HashMap<String, String> versions = new HashMap<String, String>();
                for( String version : majorVersions.keySet()){
                    versions.putAll(getVersions(parser, majorVersions.get(version), version));
                }

                majorVersions = versions;
            }

            for( String version : majorVersions.keySet()){
                minorVersions.putAll(getVersions(parser, majorVersions.get(version), version));
            }

            for( String version : minorVersions.keySet()){
                String zipName = "vaadin-all-" + version + ".zip";
                VaadinVersion vaadinVersion = new VaadinVersion(version, type, minorVersions.get(version) + zipName);
                if(vaadinVersion.isSupported()) vaadinVersions.add(vaadinVersion);
            }
        }
        catch (Exception e)
        {
            Notification.show("Can't fetch " + type  + " versions", Notification.Type.ERROR_MESSAGE);
        }
        }
        return vaadinVersions;
    }

    private HashMap<String, String> getVersions(LinkParser parser, String versionListUrl, String majorVersion) throws IOException {
        String majorVerisonResponse = getResponseString(versionListUrl);
        HashMap<String, String> versionsAndUrls = new HashMap<String, String>();

        List<String> versionsList = parser.getVaadinVersions(majorVerisonResponse, majorVersion);

        for(String version: versionsList){
            String url = versionListUrl + version + "/";
            versionsAndUrls.put(version, url );
        }
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
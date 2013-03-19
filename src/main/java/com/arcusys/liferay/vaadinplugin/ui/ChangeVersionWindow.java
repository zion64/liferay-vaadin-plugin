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
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 22.02.13
 * Time: 12:49
 * To change this template use File | Settings | File Templates.
 */
public class ChangeVersionWindow extends Window {
    private static final Log log = LogFactoryUtil
            .getLog(ChangeVersionWindow.class);

    private static final String VERSION_PROPERTY = "version";
    private static final String RELEASE_TYPE_PROPERTY = "releaseType";

    private enum VaadinReleaseType {
        nightly, prerelease, release;
    }

    public final class VaadinVersion {
        private final String version;
        private final String downloadUrl;
        private final VaadinReleaseType releaseType;

        public VaadinVersion(String versionRow) {
            String[] parts = versionRow.split(",");
            releaseType = VaadinReleaseType.valueOf(parts[0]);
            version = parts[1];
            downloadUrl = parts[2].replace(".jar", ".zip");
        }

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
            if (!"7".equals(majorVersion)) {
                // Other major versions than 7 not supported
                return false;
            }
            //TODO filter alfa versions
//            int minorVersion = Integer.parseInt(versionParts[1]);
//            if (minorVersion < 6) {
//                // Versions prior to 6.6.2 not supported as they doesn't bundle
//                // the liferay theme
//                // compilation scheme
//                return false;
//            }
//            if (minorVersion == 6 && Integer.parseInt(versionParts[2]) < 2) {
//                return false;
//            }
            return true;
        }
    }

    private Thread versionFetch = new Thread() {
        @Override
        public void run() {
            try {
                List<VaadinVersion> versionList = fetchVersionList();
                getUI().getSession().getLockInstance().lock();
                    beanItemContainer.addAll(versionList);
                    updateState(true);
                getUI().getSession().getLockInstance().unlock();
            } catch (Exception e) {
                log.warn("Version list could not be downloaded", e);
                getUI().getSession().getLockInstance().lock();

                layout.removeAllComponents();
                layout.addComponent(new Label(
                            "Version list could not be downloaded: "
                                    + e.getMessage()));
                getUI().getSession().getLockInstance().unlock();
            } finally {
                // Release memory
                versionFetch = null;
            }
        }

        private List<VaadinVersion> fetchVersionList() throws IOException {
            URL vaadinVersionsUrl;
            InputStream inputStream = null;
            BufferedReader dataInputStream = null;
            try {
                vaadinVersionsUrl = new URL(ControlPanelPortletUtil.ALL_VERSIONS_INFO);
                inputStream = vaadinVersionsUrl.openStream();
                dataInputStream = new BufferedReader(new InputStreamReader(
                        inputStream));

                List<VaadinVersion> list = new ArrayList<VaadinVersion>();
                String line;
                while ((line = dataInputStream.readLine()) != null) {
                    VaadinVersion vaadinVersion = new VaadinVersion(line);
                    if (vaadinVersion.isSupported()) {
                        list.add(vaadinVersion);
                    }
                }

                return list;
            } finally {
                ControlPanelPortletUtil.close(dataInputStream);
                ControlPanelPortletUtil.close(inputStream);
            }
        }
    };

    private List<VaadinVersion> fetchVersionList2(Collection<VaadinReleaseType> versiontypes) throws IOException {
        LinkParser parser = new LinkParser();

        for(VaadinReleaseType type : versiontypes){
        try {

            List<VaadinVersion> vaadinVersions = new ArrayList<VaadinVersion>();


            String vaadinMajorVersionListUrl = ControlPanelPortletUtil.VAADIN_DOWNLOAD_URL + type + "/";
//            String majorVerisonResponse = getResponseString(vaadinMajorVersionListUrl);
//
//            List<String> majorVersionsList = parser.getVaadinMajorVersions(majorVerisonResponse);
//
//            for(String majorVersion: majorVersionsList){
//                String url = vaadinMajorVersionListUrl + majorVersion + "/";
//                majorVersions.put(majorVersion, url );
//            }

            HashMap<String, String> majorVersions = getVersions(parser, vaadinMajorVersionListUrl, "7");

            HashMap<String, String> minorVersions = new HashMap<String, String>();

            for( String version : majorVersions.keySet()){
                /*List<String> minorVersionsList = new ArrayList<String>();
                String vaadinMinorVersionListUrl = majorVersions.get(version);
                String minorVerisonResponse = getResponseString(vaadinMinorVersionListUrl);
                minorVersionsList.addAll(parser.getVaadinMinorVersions(minorVerisonResponse, version));

                for(String minorVersion: minorVersionsList)
                {
                    String url = vaadinMinorVersionListUrl + minorVersion + "/";
                    minorVersions.put( minorVersion, url);
                }*/

                minorVersions.putAll(getVersions(parser, majorVersions.get(version), version));
            }

            for( String version : minorVersions.keySet()){
                String zipName = "vaadin-all-" + version + ".zip";
                vaadinVersions.add(new VaadinVersion(version, type, minorVersions.get(version) + zipName));
            }

            layout.addComponent(new Label("Minor versions", ContentMode.TEXT ));
            for( VaadinVersion version : vaadinVersions){
                layout.addComponent(new Label(version.version + " " + version.releaseType + "( " + version.downloadUrl + " )", ContentMode.TEXT));
            }

        } finally {

        }

    }
        return new ArrayList<VaadinVersion>();
    }

    private HashMap<String, String> getVersions(LinkParser parser, String versionListUrl, String majorVersion) throws IOException {
        String majorVerisonResponse = getResponseString(versionListUrl);
        HashMap<String, String> versionsAndUrls = new HashMap<String, String>();

        List<String> versionsList = parser.getVaadinMinorVersions(majorVerisonResponse, majorVersion);

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

        Button testfetch = new Button("Test version fetching");//
        testfetch.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
//                ArrayList<String> types= new ArrayList<String>();
//                types.add("release");

                try{

                    Collection<VaadinReleaseType> releaseTypesCollection = (Collection<VaadinReleaseType>) includeVersions.getValue();
//
//                    if (releaseTypesCollection.contains(VaadinReleaseType.nightly)) {
//                        types.add("nightly");
//                    }
//
//                    if (releaseTypesCollection.contains(VaadinReleaseType.prerelease)) {
//                        types.add("prerelease");
//                    }

                    fetchVersionList2(releaseTypesCollection);
                }catch (Exception e)
                {
                    Notification.show("Error: " + e.getMessage(), Notification.Type.ERROR_MESSAGE );
                }
            }
        });

        layout.addComponent(testfetch);

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
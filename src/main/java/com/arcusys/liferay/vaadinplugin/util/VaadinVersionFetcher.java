package com.arcusys.liferay.vaadinplugin.util;

import com.vaadin.ui.Notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 29.03.13
 * Time: 11:46
 */
public class VaadinVersionFetcher {

    public  List<VaadinVersion> fetchVersionList(Collection<VaadinVersion.VaadinReleaseType> versiontypes) {
        LinkParser parser = new LinkParser();
        List<VaadinVersion> vaadinVersions = new ArrayList<VaadinVersion>();
        for(VaadinVersion.VaadinReleaseType type : versiontypes){
            try {
                String vaadinMajorVersionListUrl = ControlPanelPortletUtil.VAADIN_DOWNLOAD_URL + type + "/";
                List<LinkParser.VersionData> majorVersions = getVersions(parser, vaadinMajorVersionListUrl, VaadinVersion.VAADIN_MAJOR_VERSION);

                List<LinkParser.VersionData> minorVersions = new ArrayList<LinkParser.VersionData>();

                if(type == VaadinVersion.VaadinReleaseType.prerelease){
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
                    VaadinVersion vaadinVersion = new VaadinVersion(versionData.getVersion(), type ,versionData.getUrl() + zipName, versionData.getDate());
                    if(vaadinVersion.isSupported()) vaadinVersions.add(vaadinVersion);
                }
            }
            catch (Exception e)
            {
                Notification.show("Can't fetch " + type + " versions", Notification.Type.ERROR_MESSAGE);
            }
        }

        Collections.sort(vaadinVersions, new Comparator<VaadinVersion>() {
            @Override
            public int compare(VaadinVersion o1, VaadinVersion o2) {
                if (o1 == null) return -1;
                if (o2 == null) return 1;

                String vers1 = o1.getVersion().substring(0, 5);
                String vers2 = o2.getVersion().substring(0, 5);

                if (vers1.compareTo(vers2) == 0) {

                    if (o1.getReleaseDate() != null && o2.getReleaseDate() != null) {
                        return o1.getReleaseDate().compareTo(o2.getReleaseDate());
                    } else {
                        return o1.getVersion().compareTo(o2.getVersion());
                    }
                } else {
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
}

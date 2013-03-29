package com.arcusys.liferay.vaadinplugin;

import com.arcusys.liferay.vaadinplugin.util.ControlPanelPortletUtil;
import com.arcusys.liferay.vaadinplugin.util.VaadinVersion;
import com.arcusys.liferay.vaadinplugin.util.VaadinVersionFetcher;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class NewestVaadinVersion {
    private final Log log = LogFactoryUtil.getLog(NewestVaadinVersion.class);
    private String newestVaadinVersion;
    private String newestVaadinVersionLocation;

    public NewestVaadinVersion() {
        retrieveNewestVaadinVersionAndLocation();
    }

    public String getVersion() {
        return newestVaadinVersion;
    }

    public String getLocation() {
        return newestVaadinVersionLocation;
    }

    public boolean wasFound() {
        return !("".equals(newestVaadinVersionLocation));
    }

    private void retrieveNewestVaadinVersionAndLocation() {
//        URL newestVaadinVersionURL;
//        InputStream inputStream = null;
//        BufferedReader dataInputStream = null;
        String s;
        String version = "";
        String location = "";

        VaadinVersionFetcher versionFetcher = new VaadinVersionFetcher();

        try {
           /* newestVaadinVersionURL = new URL(ControlPanelPortletUtil.LATEST_VAADIN_INFO);
            inputStream = newestVaadinVersionURL.openStream();
            dataInputStream = new BufferedReader(new InputStreamReader(
                    inputStream));
            if ((s = dataInputStream.readLine()) != null) {
                version = s.trim();
            }
            if ((s = dataInputStream.readLine()) != null) {
                location= s.trim();
            }*/

            VaadinVersion latestVersion =  versionFetcher.fetchLatestReleaseVersion();

            location = latestVersion.getDownloadUrl();
            version = latestVersion.getVersion();


//            if (!version.startsWith(maxMajorVersion)) {
//                version = "Can't find latest " + maxMajorVersion + ".* version";
//                location = "";
//            }

        } catch (Exception e) {
            log.warn(e);
            version = "unknown";
            location = "";
        } finally {
            newestVaadinVersion = version;
            newestVaadinVersionLocation = location;
//            ControlPanelPortletUtil.close(dataInputStream);
//            ControlPanelPortletUtil.close(inputStream);
        }
    }
}

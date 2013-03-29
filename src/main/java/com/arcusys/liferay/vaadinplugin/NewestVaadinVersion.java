package com.arcusys.liferay.vaadinplugin;

import com.arcusys.liferay.vaadinplugin.util.VaadinVersion;
import com.arcusys.liferay.vaadinplugin.util.VaadinVersionFetcher;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

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
        String s;
        String version = "";
        String location = "";

        VaadinVersionFetcher versionFetcher = new VaadinVersionFetcher();

        try {
            VaadinVersion latestVersion =  versionFetcher.fetchLatestReleaseVersion();

            if(latestVersion == null){
                version = "unknown";
                location = "";
            }else{
                location = latestVersion.getDownloadUrl();
                version = latestVersion.getVersion();
            }

        } catch (Exception e) {
            log.warn(e);
            version = "unknown";
            location = "";
        } finally {
            newestVaadinVersion = version;
            newestVaadinVersionLocation = location;
        }
    }
}

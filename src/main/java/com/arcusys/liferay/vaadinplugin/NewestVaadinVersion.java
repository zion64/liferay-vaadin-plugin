package com.arcusys.liferay.vaadinplugin;

import com.arcusys.liferay.vaadinplugin.util.VaadinVersion;
import com.arcusys.liferay.vaadinplugin.util.VaadinVersionFetcher;
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

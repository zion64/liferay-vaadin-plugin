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

import com.arcusys.liferay.vaadinplugin.util.ControlPanelPortletUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class NewestVaadinVersion {
    private final Log log = LogFactoryUtil.getLog(NewestVaadinVersion.class);
    private final String maxMajorVersion = "7";
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
        URL newestVaadinVersionURL;
        InputStream inputStream = null;
        BufferedReader dataInputStream = null;
        String s;
        String version = "";
        String location = "";
        try {
            newestVaadinVersionURL = new URL(ControlPanelPortletUtil.LATEST_VAADIN_INFO);
            inputStream = newestVaadinVersionURL.openStream();
            dataInputStream = new BufferedReader(new InputStreamReader(
                    inputStream));
            if ((s = dataInputStream.readLine()) != null) {
                version = s.trim();
            }
            if ((s = dataInputStream.readLine()) != null) {
                location= s.trim();
            }

            if (!version.startsWith(maxMajorVersion)) {
                version = "Can't find latest " + maxMajorVersion + ".* version";
                location = "";
            }

        } catch (Exception e) {
            log.warn(e);
            version = "unknown";
            location = "";
        } finally {
            newestVaadinVersion = version;
            newestVaadinVersionLocation = location;
            ControlPanelPortletUtil.close(dataInputStream);
            ControlPanelPortletUtil.close(inputStream);
        }
    }
}

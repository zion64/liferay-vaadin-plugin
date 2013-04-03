package com.arcusys.liferay.vaadinplugin.util;

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
 *
 * */

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 29.03.13
 * Time: 11:47
 */

import org.joda.time.DateTime;

public final class VaadinVersion {
    private final String version;
    private final String downloadUrl;
    private final VaadinReleaseType releaseType;
    public static final String VAADIN_MAJOR_VERSION = "7";
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

    public enum VaadinReleaseType {
        nightly, prerelease, release
    }
}

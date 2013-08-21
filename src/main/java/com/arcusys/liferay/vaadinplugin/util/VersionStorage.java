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
 */

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Maxim Minin
 * Date: 15/08/13
 * Time: 11:39 AM
 */

public class VersionStorage {

    private List<DownloadInfo> versions;
    private long expiredTime;

    public List<DownloadInfo> getVersions() {
        if (versions == null) return null;
        if (new Date().getTime() > expiredTime) {
            versions = null;
        }
        return versions;
    }

    public void setVersions(List<DownloadInfo> versions, long lifetime) {
        this.versions = versions;
        this.expiredTime = new Date().getTime() + lifetime;
    }
}

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

    public  List<DownloadInfo> fetchAllVersionList() {
        Collection<DownloadInfo.VaadinReleaseType> releaseTypesCollection = new ArrayList<DownloadInfo.VaadinReleaseType>();
        releaseTypesCollection.add(DownloadInfo.VaadinReleaseType.release);
        releaseTypesCollection.add(DownloadInfo.VaadinReleaseType.nightly);
        releaseTypesCollection.add(DownloadInfo.VaadinReleaseType.prerelease);

        return fetchVersionList(releaseTypesCollection);
    }

    public DownloadInfo fetchLatestReleaseVersion() {
        Collection<DownloadInfo.VaadinReleaseType> releaseTypesCollection = new ArrayList<DownloadInfo.VaadinReleaseType>();
        releaseTypesCollection.add(DownloadInfo.VaadinReleaseType.release);

        List<DownloadInfo> versions =  fetchVersionList(releaseTypesCollection);

        return versions.get(versions.size() - 1);
    }

    private   List<DownloadInfo> fetchVersionList(Collection<DownloadInfo.VaadinReleaseType> versiontypes) {
        LinkParser parser = new LinkParser();
        List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
        for(DownloadInfo.VaadinReleaseType type : versiontypes){
            try {
                String vaadinMajorVersionListUrl = ControlPanelPortletUtil.VAADIN_DOWNLOAD_URL + type + "/";
                List<LinkParser.VersionData> majorVersions = getVersions(parser, vaadinMajorVersionListUrl, DownloadInfo.VAADIN_MAJOR_VERSION.toString());

                List<LinkParser.VersionData> minorVersions = new ArrayList<LinkParser.VersionData>();

                if(type == DownloadInfo.VaadinReleaseType.prerelease){
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
                    Version version = new Version(versionData.getVersion());
                    DownloadInfo downloadInfo = new DownloadInfo(version, type ,versionData.getUrl() + zipName, versionData.getDate());
                    if(downloadInfo.isSupported()) downloadInfos.add(downloadInfo);
                }
            }
            catch (Exception e)
            {
                Notification.show("Can't fetch " + type + " versions", Notification.Type.ERROR_MESSAGE);
            }
        }

        Collections.sort(downloadInfos, new Comparator<DownloadInfo>() {
            @Override
            public int compare(DownloadInfo o1, DownloadInfo o2) {
                if (o1 == null) return -1;
                if (o2 == null) return 1;

                String vers1 = o1.getVersion().toString().substring(0, 5);
                String vers2 = o2.getVersion().toString().substring(0, 5);

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
        return downloadInfos;
    }

    private List<LinkParser.VersionData> getVersions(LinkParser parser, String versionListUrl, String majorVersion) throws IOException {
        String majorVerisonResponse = getResponseString(versionListUrl);

        return  parser.getVaadinVersionsAndDates(majorVerisonResponse, majorVersion, versionListUrl);
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

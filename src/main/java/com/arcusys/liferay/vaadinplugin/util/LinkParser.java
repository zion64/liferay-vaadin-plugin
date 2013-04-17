package com.arcusys.liferay.vaadinplugin.util;

/*
 * #%L
 * Liferay Vaadin Plugin
 * %%
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 18.03.13
 * Time: 11:10
 */
public class LinkParser {

    public List<VersionData> getVaadinVersionsAndDates(String response, String majorVersion, String parentUrl) {
        Pattern pattern = Pattern.compile("<a href=\"" + majorVersion + ".*?.\\d{2}-\\w{3}-\\d{4}\\s\\d{2}:\\d{2}?", Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(response);

        ArrayList<VersionData> versionList = new ArrayList<VersionData>();

        Pattern versionPattern = Pattern.compile(majorVersion + ".*?./\"", Pattern.CASE_INSENSITIVE);
        Pattern datePattern = Pattern.compile("\\d{2}-\\w{3}-\\d{4}\\s\\d{2}:\\d{2}", Pattern.CASE_INSENSITIVE);

        DateTime datetime = null;

        while (matcher.find()) {
            String versionstring = matcher.group();
            Matcher versionMatcher = versionPattern.matcher(versionstring);
            Matcher dateMatcher = datePattern.matcher(versionstring);
            String version = "";
            String date = "";
            while (versionMatcher.find()) {
                version = versionMatcher.group().replace("\"", "").replace("/", "").trim();
            }
            while (dateMatcher.find()) {
                date = dateMatcher.group().replace("\"", "").replace("/", "").trim();
            }

            if (!"".equals(date)) {
                DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm").withLocale(Locale.ENGLISH);
                try {
                    datetime = fmt.parseDateTime(date);
                } catch (IllegalArgumentException ex) {
                    datetime = null;
                }
            }

            if (!version.isEmpty()) {
                String url = parentUrl + version + "/";
                versionList.add(new VersionData(version, datetime, url));
            }
        }
        return versionList;
    }

    public class VersionData {
        private final DateTime date;
        private final String version;
        private final String url;

        public VersionData(String version, DateTime date, String url) {
            this.version = version;
            this.date = date;
            this.url = url;
        }

        public DateTime getDate() {
            return date;
        }

        public String getVersion() {
            return version;
        }

        public String getUrl() {
            return url;
        }
    }
}

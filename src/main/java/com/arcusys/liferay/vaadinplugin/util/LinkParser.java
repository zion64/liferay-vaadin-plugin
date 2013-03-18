package com.arcusys.liferay.vaadinplugin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 18.03.13
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
public class LinkParser {


    public List<String> getVaadinMajorVersions(String response)
    {
        Pattern pattern = Pattern.compile("<a href=\"7\\..*?./\"", Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(response);

        ArrayList<String> versionList = new ArrayList<String>();

        while (matcher.find()){
            String versionstring = matcher.group();
            Pattern versionPattern = Pattern.compile("\"7\\..*?./\"", Pattern.CASE_INSENSITIVE);
            Matcher versionMatcher = versionPattern.matcher(versionstring);

            while(versionMatcher.find())
            {
                String version = versionMatcher.group().replace("\"", "").replace("/","");
                versionList.add(version);
            }
        }

        return versionList;
    }


    public List<String> getVaadinMinorVersions(String response, String majorVersion)
    {
        Pattern pattern = Pattern.compile("<a href=\"" + majorVersion + ".*?./\"", Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(response);

        ArrayList<String> versionList = new ArrayList<String>();

        while (matcher.find()){
            String versionstring = matcher.group();
            Pattern versionPattern = Pattern.compile(majorVersion + ".*?./\"", Pattern.CASE_INSENSITIVE);
            Matcher versionMatcher = versionPattern.matcher(versionstring);
            while(versionMatcher.find())
            {
                String version = versionMatcher.group().replace("\"", "").replace("/","");
                versionList.add(version);
            }
        }

        return versionList;
    }


    public void getLinkList(String link){



    }
}

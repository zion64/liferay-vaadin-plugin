package com.arcusys.liferay.vaadinplugin.util;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Maxim Minin
 * Date: 15/08/13
 * Time: 11:39 AM
 */

public class VersionStorage {
    private static final VersionStorage instance = new VersionStorage();

    public static VersionStorage GetInstance() {
        return instance;
    }

    private List<VaadinVersion> versions;
    private long expiredTime;

    private VersionStorage() {
    }

    public List<VaadinVersion> getVersions() {
        if (versions == null) return null;
        if (new Date().getTime() > expiredTime) {
            versions = null;
        }
        return versions;
    }

    public void setVersions(List<VaadinVersion> versions, long lifetime) {
        this.versions = versions;
        this.expiredTime = new Date().getTime() + lifetime;
    }
}
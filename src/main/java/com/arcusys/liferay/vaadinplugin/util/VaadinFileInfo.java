package com.arcusys.liferay.vaadinplugin.util;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 21.03.13
 * Time: 12:22
 */
public class VaadinFileInfo {
    private final String name;
    private final String place;
    private final String innerSourcePath;

    public VaadinFileInfo(String name, String place)
    {
        this(name, place, "");
    }

    public VaadinFileInfo(String name, String place, String innerSourcePath)
    {
        this.name = name;
        this.place = place;
        this.innerSourcePath = innerSourcePath;
    }

    public String getName()
    {
        return  name;
    }

    public String getPlace()
    {
        return  place;
    }

    public String getInnerSourcePath()
    {
        return  innerSourcePath;
    }
}

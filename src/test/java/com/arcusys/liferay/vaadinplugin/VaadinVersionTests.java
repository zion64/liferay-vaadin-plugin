package com.arcusys.liferay.vaadinplugin;

import com.arcusys.liferay.vaadinplugin.util.Version;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: mminin
 * Date: 20/08/13
 * Time: 10:51 AM
 */

public class VaadinVersionTests {

    @Test
    public void compareToTest() {
        Version version = new Version("7.0.2 test");
        System.out.println(Arrays.toString(version.getNumericVersion()));
        Assert.assertEquals(1, version.compareTo(new Version("7.0.1")));
        Assert.assertEquals(1, version.compareTo(new Version("7.0.1.994")));
        Assert.assertEquals(0, version.compareTo(new Version("7.0.2")));
        Assert.assertEquals(-1, version.compareTo(new Version("7.0.2.12")));
        Assert.assertEquals(-1, version.compareTo(new Version("7.0.3")));
        Assert.assertEquals(-1, version.compareTo(new Version("10")));
    }

    @Test
    public void compareToTest2() {
        Version version = new Version("7.0.2.beta.12");
        Assert.assertEquals(1, version.compareTo(new Version("7.0.1")));
        Assert.assertEquals(1, version.compareTo(new Version("7.0.1.994")));
        Assert.assertEquals(0, version.compareTo(new Version("7.0.2")));
        Assert.assertEquals(-1, version.compareTo(new Version("7.0.2.12")));
        Assert.assertEquals(-1, version.compareTo(new Version("7.0.3")));
        Assert.assertEquals(-1, version.compareTo(new Version("10")));
    }
}

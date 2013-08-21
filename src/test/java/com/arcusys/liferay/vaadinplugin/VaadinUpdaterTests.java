package com.arcusys.liferay.vaadinplugin;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: mminin
 * Date: 20/08/13
 * Time: 10:51 AM
 */

public class VaadinUpdaterTests {

    @Test
    public void getFileNameWithoutVersionTest() {
        Assert.assertEquals("vaadin.jar", VaadinUpdater.getFileNameWithoutVersion("vaadin-0.5.3.jar"));
        Assert.assertEquals("vaadin.jar", VaadinUpdater.getFileNameWithoutVersion("vaadin-0.5.3.test.jar"));

        Assert.assertEquals("validation-api.GA.jar", VaadinUpdater.getFileNameWithoutVersion("validation-api-1.0.0.GA.jar"));

        Assert.assertEquals("validation-api.GA-sources.jar", VaadinUpdater.getFileNameWithoutVersion("validation-api-1.0.0.GA-sources.jar"));
    }
}

package com.arcusys.liferay.vaadinplugin.ui;

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

import com.arcusys.liferay.vaadinplugin.util.ControlPanelPortletUtil;
import com.arcusys.liferay.vaadinplugin.util.VaadinFileInfo;
import com.arcusys.liferay.vaadinplugin.util.Version;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 11.04.13
 * Time: 16:57
 */
public class DetailsWindow extends Window {

    private static final Log log = LogFactoryUtil.getLog(DetailsWindow.class);

    public DetailsWindow() {
        super("Vaadin components info");
        setModal(true);
        setContent(createVaadinDetails());
    }

    private Layout createVaadinDetails() {

        Layout vaadinDetailLayout = new VerticalLayout();
        vaadinDetailLayout.setWidth("900px");

        VerticalLayout vaadinDetails = new VerticalLayout();
        vaadinDetails.setMargin(new MarginInfo(true, true, false, true));

        Version currentVersion = ControlPanelPortletUtil.getPortalVaadinVersion();
        Collection<VaadinFileInfo> fileInfos = ControlPanelPortletUtil.getVaadinFilesInfo(currentVersion);

        Collections.sort((List<VaadinFileInfo>) fileInfos, new Comparator<VaadinFileInfo>() {
            @Override
            public int compare(VaadinFileInfo o1, VaadinFileInfo o2) {
                if (o1 == null) return -1;
                if (o2 == null) return 1;
                return o1.getOrderPriority().compareTo(o2.getOrderPriority());
            }
        }
        );

        for (VaadinFileInfo info : fileInfos) {
            VerticalLayout infoLayout = new VerticalLayout();
            infoLayout.setCaption(info.getName());

            infoLayout.setMargin(new MarginInfo(false, true, true, false));

            Layout versionLayout = new HorizontalLayout();
            versionLayout.setSizeUndefined();
            versionLayout.addComponent(new Label("Version: "));
            String vaadinJarVersion;
            try {
                vaadinJarVersion = ControlPanelPortletUtil.getPortalVaadinJarVersion(info.getPlace() + ControlPanelPortletUtil.FileSeparator + info.getName());
            } catch (Exception ex) {
                vaadinJarVersion = "";
                log.warn("Version for " + vaadinJarVersion + " couldn't be read.", ex);
            }

            versionLayout.addComponent(new Label(vaadinJarVersion));

            infoLayout.addComponent(versionLayout);

            Layout pathLayout = new HorizontalLayout();

            pathLayout.setSizeUndefined();
            pathLayout.addComponent(new Label("Path: "));
            String path = info.getPlace();
            pathLayout.addComponent(new Label(path));

            infoLayout.addComponent(pathLayout);

            vaadinDetails.addComponent(infoLayout);
        }

        vaadinDetailLayout.addComponent(vaadinDetails);
        return vaadinDetailLayout;
    }
}

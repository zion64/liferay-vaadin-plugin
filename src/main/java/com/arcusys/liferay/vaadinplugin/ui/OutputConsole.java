package com.arcusys.liferay.vaadinplugin.ui;

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

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification;

@SuppressWarnings("serial")
public class OutputConsole extends CustomComponent {

    private Label outputLabel = new Label("", ContentMode.HTML);
    private Label scrollToLabel = new Label();

    public OutputConsole(String caption) {
        Panel panel = new Panel();
        panel.setCaption(caption);
        panel.setSizeFull();
        setCompositionRoot(panel);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeUndefined();
        panel.setContent(layout);

        VerticalLayout outputLabelLayout = new VerticalLayout();
        outputLabelLayout.setSizeUndefined();
        outputLabelLayout.setMargin(true);
        layout.addComponent(outputLabelLayout);

        outputLabel.setSizeUndefined();
        outputLabelLayout.addComponent(outputLabel);

        scrollToLabel.setWidth("0px");
        scrollToLabel.setHeight("0px");
        layout.addComponent(scrollToLabel);
    }

    public void clear() {
        outputLabel.setValue("");
    }

    public void log(String msg) {
        synchronized (getUI()) {
            msg = "<div>" + msg + "</div>";
            outputLabel.setValue(outputLabel.getValue() + msg);
            getUI().scrollIntoView(scrollToLabel);
        }
    }
}

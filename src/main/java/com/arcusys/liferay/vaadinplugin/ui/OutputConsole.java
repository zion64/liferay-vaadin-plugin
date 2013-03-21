package com.arcusys.liferay.vaadinplugin.ui;

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

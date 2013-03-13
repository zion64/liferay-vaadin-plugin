package com.arcusys.liferay.vaadinplugin;

import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 15.02.13
 * Time: 16:28
 * To change this template use File | Settings | File Templates.
 */
public class ControlPanelUIProvider extends DefaultUIProvider {

    @Override
    public Class<? extends UI> getUIClass(final UIClassSelectionEvent event) {
        return ControlPanelUI.class;
    }

    @Override
    public UI createInstance(final UICreateEvent event) {

        final UI ui = super.createInstance(event);

        ui.setSession(VaadinSession.getCurrent());

       /* if (ui instanceof ControlPanelUI) {
            final ControlPanelUI controlPanelUi = (ControlPanelUI) ui;
            controlPanelUi.initApiLoader(event.getRequest());
        } else {
            log.getLogger(getClass()).warn(
                    "Created UI is not an instance of "
                            + ControlPanelUI.class.getName() + " but "
                            + ui.getClass().getName()
                            + ". Might cause problems.");
        }*/
        return ui;
    }
}

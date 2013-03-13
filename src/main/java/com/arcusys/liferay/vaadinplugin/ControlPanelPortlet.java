package com.arcusys.liferay.vaadinplugin;

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinPortlet;

public class ControlPanelPortlet extends VaadinPortlet {

    @Override
    protected void portletInitialized() {
        getService().addSessionInitListener(new SessionInitListener() {

            @Override
            public void sessionInit(final SessionInitEvent event)
                    throws ServiceException {
                event.getSession().addUIProvider(new ControlPanelUIProvider());
            }
        });
    }
//    @Override
//    protected void handleRequest(final PortletRequest request,
//                                 final PortletResponse response) throws PortletException,
//            IOException {
//
//            super.handleRequest(request, response);
//    }
}

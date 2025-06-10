/*******************************************************************************
 * Copyright (c) 2025 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.runner;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NatTableExampleApplication implements ApplicationConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(NatTableExampleApplication.class);

    @Override
    public void configure(Application application) {
        LOG.info("Configure " + getClass());
        Map<String, String> properties = new HashMap<>();
        properties.put(WebClient.PAGE_TITLE, "NatTable Examples Application");
        application.setOperationMode(OperationMode.SWT_COMPATIBILITY);
        application.addEntryPoint("/examples", NatExampleEntryPoint.class, properties);
        application.addStyleSheet(RWT.DEFAULT_THEME_ID, "theme/theme.css");

        // Register the service handler for the download
        // https://eclipse.dev/rap/developers-guide/resources.html
        ServiceHandler handler = new DownloadServiceHandler();
        application.addServiceHandler("downloadServiceHandler", handler);
    }
}

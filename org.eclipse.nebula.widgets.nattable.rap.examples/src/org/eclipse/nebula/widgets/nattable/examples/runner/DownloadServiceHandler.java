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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.rap.rwt.service.ServiceHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DownloadServiceHandler implements ServiceHandler {

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String fileUri = request.getParameter("fileURI");

        // Get the file content
        URI uri = URI.create(fileUri);
        byte[] download = Files.readAllBytes(Paths.get(uri));

        // Set the file name to be downloaded
        File file = new File(fileUri);
        String fileName = file.getName();

        // Send the file in the response
        response.setContentType("application/octet-stream");
        response.setContentLength(download.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.getOutputStream().write(download);
    }
}

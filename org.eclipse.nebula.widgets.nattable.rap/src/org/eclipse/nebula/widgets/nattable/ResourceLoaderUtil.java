/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Utility class to load resources from the classpath, e.g. JavaScript files.
 * <p>
 * Have a look at <a href="https://eclipse.dev/rap/developers-guide/scripting.html">RAP Developer's Guide - Scripting</a>
 * for further information about the usage of JavaScript files in RAP. 
 * </p>
 */
public class ResourceLoaderUtil {

	private static final ClassLoader CLASSLOADER = ResourceLoaderUtil.class.getClassLoader();

	/**
	 * Reads the content of a resource file from the classpath.
	 *
	 * @param resource the name of the resource file
	 * @return the content of the resource file as a string
	 * @throws IllegalArgumentException if the resource file cannot be found or read
	 */
	public static String readTextContent(String resource) {
		try {
			return readTextChecked(resource);
		} catch (IOException | URISyntaxException e) {
			throw new IllegalArgumentException("Failed to read: " + resource, e);
		}
	}

	private static String readTextChecked(String resource) throws URISyntaxException, IOException {
		// use an absolute path to the resource to make it work in OSGi
		URL url = CLASSLOADER.getResource("org/eclipse/nebula/widgets/nattable/" + resource);
		
		// if we are in an OSGi context, we need to convert the bundle URL to a file URL
		final Bundle bundle = FrameworkUtil.getBundle(ResourceLoaderUtil.class);
		if (bundle != null) {
			try {
				url = FileLocator.toFileURL(url);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		if (url == null) {
			throw new IllegalArgumentException("Resource file not found: " + resource);
		}
		
		Path path = Paths.get(url.toURI());
		try (Stream<String> lines = Files.lines(path)) {
			return lines.collect(Collectors.joining("\n"));
		}
	}

}
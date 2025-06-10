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

import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.internal.widgets.canvaskit.CanvasLCA;
import org.eclipse.swt.widgets.Canvas;

/**
 * Custom {@link WidgetLCA} for the NatTable widget that forwards to the {@link CanvasLCA}.
 * Needed because the {@link CanvasLCA} is a final class and cannot be extended.
 * We need to set a custom handler for the NatTable widget to handle the events correctly.
 */
@SuppressWarnings("restriction")
public final class NatTableLCA extends WidgetLCA<Canvas> {

	public static final NatTableLCA INSTANCE = new NatTableLCA();

	@Override
	public void preserveValues(Canvas widget) {
		CanvasLCA.INSTANCE.preserveValues(widget);
	}

	@Override
	public void renderInitialization(Canvas widget) throws IOException {
		CanvasLCA.INSTANCE.renderInitialization(widget);

		// Register the NatTableOperationHandler as handler for the NatTable widget
		RemoteObject remoteObject = getRemoteObject(widget);
		remoteObject.setHandler(new NatTableOperationHandler(widget));
	}

	@Override
	public void renderChanges(Canvas widget) throws IOException {
		CanvasLCA.INSTANCE.renderChanges(widget);
	}

	@Override
	public void renderDispose(Canvas canvas) throws IOException {
		CanvasLCA.INSTANCE.renderDispose(canvas);
	}
}
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

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MOUSE_DOUBLE_CLICK;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MOUSE_DOWN;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MOUSE_UP;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_BUTTON;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_TIME;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_X;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_Y;
import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.wasEventSent;
import static org.eclipse.swt.internal.events.EventLCAUtil.translateButton;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.canvaskit.CanvasOperationHandler;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * Specialized {@link CanvasOperationHandler} for NatTable. Needed to
 * <ul>
 * <li>handle the mouse events correctly as the button state mask is not set in SWT</li>
 * <li>add a handler for mouse wheel</li>
 * <li>add a handler for mouse move</li>
 * </ul>
 */
@SuppressWarnings("restriction")
public class NatTableOperationHandler extends CanvasOperationHandler {
	private final Canvas canvas;

	public NatTableOperationHandler(Canvas canvas) {
		super(canvas);
		this.canvas = canvas;
	}

	@Override
	public void handleNotify(String eventName, JsonObject properties) {
		if (EVENT_MOUSE_DOWN.equals(eventName)) {
			handleNotifyMouseDown(properties);
		} else if (EVENT_MOUSE_DOUBLE_CLICK.equals(eventName)) {
			handleNotifyMouseDoubleClick(properties);
		} else if (EVENT_MOUSE_UP.equals(eventName)) {
			handleNotifyMouseUp(properties);
		} else if ("MouseWheel".equals(eventName)) {
			handleNotifyMouseWheel(properties);
		} else if ("MouseMove".equals(eventName)) {
			handleNotifyMouseMove(properties);
		} else {
			handleNotify(this.canvas, eventName, properties);
		}
	}

	public void handleNotifyMouseDown(JsonObject properties) {
		Event event = createMouseEvent(SWT.MouseDown, this.canvas, properties);
		if (allowMouseEvent(this.canvas, event.x, event.y)) {
			this.canvas.notifyListeners(event.type, event);
		}
	}

	public void handleNotifyMouseDoubleClick(JsonObject properties) {
		Event event = createMouseEvent(SWT.MouseDoubleClick, this.canvas, properties);
		if (allowMouseEvent(this.canvas, event.x, event.y)) {
			this.canvas.notifyListeners(event.type, event);
		}
	}

	public void handleNotifyMouseUp(JsonObject properties) {
		Event event = createMouseEvent(SWT.MouseUp, this.canvas, properties);
		if (allowMouseEvent(this.canvas, event.x, event.y)) {
			this.canvas.notifyListeners(event.type, event);
		}
	}

	/*
	 * PROTOCOL NOTIFY MouseWheel
	 *
	 * @param count (int) 1 for up and -1 for down
	 */
	public void handleNotifyMouseWheel(JsonObject properties) {
		Event event = createMouseEvent(SWT.MouseWheel, this.canvas, properties);
		if (allowMouseEvent(this.canvas, event.x, event.y)) {
			event.count = properties.get("count").asInt();
			this.canvas.notifyListeners(event.type, event);
		}
	}

	public void handleNotifyMouseMove(JsonObject properties) {
		Event event = createMouseEvent(SWT.MouseMove, this.canvas, properties);
		if (allowMouseEvent(this.canvas, event.x, event.y)) {
			this.canvas.notifyListeners(event.type, event);
		}
	}

	static Event createMouseEvent(int eventType, Control control, JsonObject properties) {
		Event event = new Event();
		event.type = eventType;
		event.widget = control;
		event.button = properties.get(EVENT_PARAM_BUTTON).asInt();
		int x = properties.get(EVENT_PARAM_X).asInt();
		int y = properties.get(EVENT_PARAM_Y).asInt();
		Point point = control.getDisplay().map(null, control, x, y);
		event.x = point.x;
		event.y = point.y;
		event.time = properties.get(EVENT_PARAM_TIME).asInt();
		event.stateMask = readStateMask(properties);

		// The button mask confuses NatTable MouseEventMatcher since the
		// button mask is not there in SWT
		// except in MouseUp event, see Widget.class line 1351
		switch (eventType) {
			case SWT.MouseUp:
				event.stateMask |= translateButton(event.button);
				break;
		}

		// TODO: send count by the client
		event.count = determineCount(eventType, control);

		JsonValue data = properties.get("data");
		if (data != null) {
			event.data = data.asString();
		}
		return event;
	}

	private static int determineCount(int eventType, Control control) {
		if (eventType == SWT.MouseDoubleClick || wasEventSent(getId(control), EVENT_MOUSE_DOUBLE_CLICK)) {
			return 2;
		}
		return 1;
	}

	@Override
	protected boolean allowMouseEvent(Canvas control, int x, int y) {
		Point size = control.getSize();
		int borderWidth = control.getBorderWidth();
		Rectangle outerBounds = new Rectangle(-borderWidth, -borderWidth, size.x, size.y);
		Rectangle innerBounds = new Rectangle(0, 0, size.x - 2 * borderWidth, size.y - 2 * borderWidth);
		return !outerBounds.contains(x, y) || innerBounds.contains(x, y);
	}
}
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

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;

/**
 * {@link ClientListener} for the {@link SWT#MouseUp} event.
 */
public class MouseUpClientListener extends ClientListener {

	public static MouseUpClientListener getInstance() {
		return SingletonUtil.getSessionInstance(MouseUpClientListener.class);
	}

	private MouseUpClientListener() {
		super(ResourceLoaderUtil.readTextContent("MouseUpListener.js"));
	}

}
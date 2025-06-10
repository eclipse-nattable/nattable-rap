/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.nattable.examples.INatExample;
import org.eclipse.swt.graphics.Image;

public class NavLabelProvider implements ILabelProvider {

    protected final NavContentProvider contentProvider;
    protected final NatExampleEntryPoint exampleEntryPoint;

    public NavLabelProvider(NavContentProvider contentProvider, NatExampleEntryPoint exampleEntryPoint) {
        this.contentProvider = contentProvider;
        this.exampleEntryPoint = exampleEntryPoint;
    }

    @Override
    public String getText(Object element) {
        String str = (String) element;
        if (!this.contentProvider.hasChildren(element)) {
            INatExample example = this.exampleEntryPoint.getExample(str);
            return example.getName();
        }

        int lastSlashIndex = str.lastIndexOf('/');
        if (lastSlashIndex < 0) {
            return format(str);
        } else {
            return format(str.substring(lastSlashIndex + 1));
        }
    }

    protected String format(String str) {
        return str.replaceAll("^_[0-9]*_", "").replace('_', ' ');
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public Image getImage(Object element) {
        return null;
    }

}

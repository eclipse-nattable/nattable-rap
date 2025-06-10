/*******************************************************************************
 * Copyright (c) 2012, 2025 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.examples.examples._104_Styling;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _010_NatTable_with_grid_painting_of_remainder_space extends
        AbstractNatExample {

    @Override
    public Control createExampleControl(Composite parent) {
        NatTable natTable = new NatTable(parent);
        NatGridLayerPainter layerPainter = new NatGridLayerPainter(natTable);
        natTable.setLayerPainter(layerPainter);
        return natTable;
    }

}

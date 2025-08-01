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
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _001_Vertical_layer_composition extends AbstractNatExample {

    @Override
    public String getDescription() {
        return "NatTable encapsulates functionality into layers that can be stacked on top of each other to provide augmented behavior. "
                + "This example shows a basic DataLayer that has a SelectionLayer and ViewportLayer stacked on top of it. The "
                + "SelectionLayer tracks what cells are selected and enables those cells to be displayed using a different style according "
                + "to their selected state. The ViewportLayer enables the underlying layer to be scrolled. Just for the heck of it we are "
                + "scrolling over a 1,000,000 column by 1,000,000 row data layer.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        ViewportLayer layer = new ViewportLayer(new SelectionLayer(
                new DataLayer(new DummyBodyDataProvider(1000000, 1000000))));
        layer.setRegionName(GridRegion.BODY);
        return new NatTable(parent, layer);
    }

}

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
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers._101_Header;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ColumnHeaderViewportSelectionDataLayerExample extends
        AbstractNatExample {

    @Override
    public String getDescription() {
        return "This example shows a very large (500 columns x 1 million rows) data layer with a column header attached.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        DummyBodyDataProvider bodyDataProvider = new DummyBodyDataProvider(500,
                1000000);
        SelectionLayer selectionLayer = new SelectionLayer(new DataLayer(
                bodyDataProvider));
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        ILayer columnHeaderLayer = new ColumnHeaderLayer(new DataLayer(
                new DummyColumnHeaderDataProvider(bodyDataProvider)),
                viewportLayer, selectionLayer);

        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER,
                columnHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);

        return new NatTable(parent, compositeLayer);
    }

}

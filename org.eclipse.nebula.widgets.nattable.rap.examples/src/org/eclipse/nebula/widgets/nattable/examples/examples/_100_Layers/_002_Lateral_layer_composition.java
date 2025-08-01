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
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _002_Lateral_layer_composition extends AbstractNatExample {

    @Override
    public String getDescription() {
        return "In addition to being able to stack layers on top of each other, layers can also be arranged laterally to form aggregate "
                + "layers. A common example of this is a 2 by 2 grid layer consisting of a corner, column header, row header, and body "
                + "region. If you only need this common case, then you can use the provided DefaultGridLayer class. If you require more "
                + "customization however, you can assemble your own composite grid layer. This example shows how to assemble a layer that "
                + "only has a column header and body region.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        DummyBodyDataProvider bodyDataProvider = new DummyBodyDataProvider(200,
                1000000);
        SelectionLayer selectionLayer = new SelectionLayer(
                new ColumnReorderLayer(new DataLayer(bodyDataProvider)));
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        ILayer columnHeaderLayer = new ColumnHeaderLayer(new DataLayer(
                new DummyColumnHeaderDataProvider(bodyDataProvider)),
                viewportLayer, selectionLayer);

        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER,
                columnHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);

        NatTable natTable = new NatTable(parent, compositeLayer, false);

        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DebugMenuConfiguration(natTable));

        natTable.configure();

        return natTable;
    }

}

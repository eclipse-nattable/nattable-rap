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
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _000_Configuring_NatTable_with_a_layer extends AbstractNatExample {

    @Override
    public String getDescription() {
        return "A central concept in NatTable is the Layer. A layer is a rectangular region of grid cells. Layers can transform other "
                + "layers or create aggregate compositions of other layers. Much of the functionality in NatTable is compartmentalized into "
                + "different layers which encapsulate specific functions and can be composed together into custom arrangements.\n"
                + "\n"
                + "A NatTable instance must be backed by a layer. This example shows how to configure NatTable with a different layer than "
                + "the default example layer. In this case the backing layer is a basic data layer that simply displays dummy data and does "
                + "nothing else.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        ILayer layer = new DataLayer(new DummyBodyDataProvider(20, 20));
        return new NatTable(parent, layer);
    }

}

/*******************************************************************************
 * Copyright (c) 2013, 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._504_Viewport;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.print.command.MultiTurnViewportOffCommandHandler;
import org.eclipse.nebula.widgets.nattable.print.command.MultiTurnViewportOnCommandHandler;
import org.eclipse.nebula.widgets.nattable.util.ClientAreaAdapter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.IScroller;
import org.eclipse.nebula.widgets.nattable.viewport.SliderScroller;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;

/**
 * Example showing how to implement NatTable that contains two horizontal split
 * viewports.
 */
public class _5042_HorizontalSplitViewportExample extends AbstractNatExample {

    @Override
    public String getDescription() {
        return "This example shows a NatTable that contains two separately scrollable "
                + "horzizontal split viewports.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday",
                "address.street", "address.housenumber", "address.postalCode", "address.city" };

        IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        IDataProvider bodyDataProvider = new ListDataProvider<>(
                PersonService.getPersonsWithAddress(50), columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        // use a cell layer painter that is configured for left clipping
        // this ensures that the rendering works correctly for split viewports
        bodyDataLayer.setLayerPainter(new GridLineCellLayerPainter(true, false));

        // create a ViewportLayer for the left part of the table and configure
        // it to only contain the first 5 columns
        final ViewportLayer viewportLayerLeft = new ViewportLayer(bodyDataLayer);
        viewportLayerLeft.setMaxColumnPosition(5);

        // create a ViewportLayer for the right part of the table and configure
        // it to only contain the last 4 columns
        ViewportLayer viewportLayerRight = new ViewportLayer(bodyDataLayer);
        viewportLayerRight.setMinColumnPosition(5);

        // create a CompositeLayer that contains both ViewportLayers
        CompositeLayer compositeLayer = new CompositeLayer(2, 1);
        compositeLayer.setChildLayer("REGION_A", viewportLayerLeft, 0, 0);
        compositeLayer.setChildLayer("REGION_B", viewportLayerRight, 1, 0);

        // in order to make printing and exporting work correctly you need to
        // register the following command handlers
        // although in this example printing and exporting is not enabled, we
        // show the registering
        compositeLayer.registerCommandHandler(
                new MultiTurnViewportOnCommandHandler(viewportLayerLeft, viewportLayerRight));
        compositeLayer.registerCommandHandler(
                new MultiTurnViewportOffCommandHandler(viewportLayerLeft, viewportLayerRight));

        // as the CompositeLayer is setting a IClientAreaProvider for the
        // composition we need to set a special ClientAreaAdapter after the
        // creation of the CompositeLayer to support split viewports
        ClientAreaAdapter leftClientAreaAdapter =
                new ClientAreaAdapter(viewportLayerLeft.getClientAreaProvider());
        viewportLayerLeft.setClientAreaProvider(leftClientAreaAdapter);

        // Wrap NatTable in composite so we can slap on the external horizontal
        // sliders
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory
                .swtDefaults()
                .numColumns(2)
                .margins(0, 0)
                .spacing(0, 0)
                .applyTo(composite);

        NatTable natTable = new NatTable(composite, compositeLayer, false);
        GridDataFactory
                .fillDefaults()
                .grab(true, true)
                .applyTo(natTable);

        // set the width of the left viewport to only showing 2 columns at the
        // same time - need to set the width AFTER creating the NatTable to take
        // the scaling into account
        int leftWidth = bodyDataLayer.getStartXOfColumnPosition(2);
        leftClientAreaAdapter.setWidth(leftWidth);

        createSplitSliders(composite, viewportLayerLeft, viewportLayerRight);

        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.configure();

        // add an IOverlayPainter to ensure the right border of the left
        // viewport always this is necessary because the left border of layer
        // stacks is not rendered by default
        natTable.addOverlayPainter(new IOverlayPainter() {

            @Override
            public void paintOverlay(GC gc, ILayer layer) {
                Color beforeColor = gc.getForeground();
                gc.setForeground(GUIHelper.COLOR_RED);
                int viewportBorderX = viewportLayerLeft.getWidth() - 1;
                gc.drawLine(viewportBorderX, 0, viewportBorderX, layer.getHeight() - 1);
                gc.setForeground(beforeColor);
            }
        });

        return composite;
    }

    private void createSplitSliders(Composite natTableParent, ViewportLayer left, ViewportLayer right) {

        // calculate the slider width according to the display scaling
        int sliderWidth = GUIHelper.convertVerticalPixelToDpi(16, true);

        // vertical scrollbar wrapped in another composite for layout
        Composite verticalComposite = new Composite(natTableParent, SWT.NONE);
        GridLayoutFactory
                .swtDefaults()
                .margins(0, 0)
                .spacing(0, 0)
                .applyTo(verticalComposite);
        GridDataFactory
                .swtDefaults()
                .hint(sliderWidth, SWT.DEFAULT)
                .align(SWT.BEGINNING, SWT.FILL)
                .grab(false, true)
                .applyTo(verticalComposite);

        Slider vertical = new Slider(verticalComposite, SWT.VERTICAL);
        GridDataFactory
                .fillDefaults()
                .grab(true, true)
                .applyTo(vertical);
        IScroller<Slider> verticalScroller = new SliderScroller(vertical, false);
        left.setVerticalScroller(verticalScroller);
        right.setVerticalScroller(verticalScroller);

        // calculate the slider height according to the display scaling
        int sliderHeight = GUIHelper.convertHorizontalPixelToDpi(16, true);

        Composite sliderComposite = new Composite(natTableParent, SWT.NONE);
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .hint(sliderHeight, SWT.DEFAULT)
                .applyTo(sliderComposite);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        sliderComposite.setLayout(gridLayout);

        // Slider Left
        // Need a composite here to set preferred size because Slider can't be
        // subclassed.
        Composite sliderLeftComposite = new Composite(sliderComposite, SWT.NONE) {
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                int width = ((ClientAreaAdapter) left.getClientAreaProvider()).getWidth();
                return new Point(width, sliderHeight);
            }
        };
        sliderLeftComposite.setLayout(new FillLayout());
        GridDataFactory.swtDefaults()
                .align(GridData.BEGINNING, GridData.BEGINNING)
                .hint(sliderHeight, SWT.DEFAULT)
                .applyTo(sliderLeftComposite);

        Slider sliderLeft = new Slider(sliderLeftComposite, SWT.HORIZONTAL);
        left.setHorizontalScroller(new SliderScroller(sliderLeft, false));

        // Slider Right
        Slider sliderRight = new Slider(sliderComposite, SWT.HORIZONTAL);
        GridDataFactory.swtDefaults()
                .align(GridData.FILL, GridData.BEGINNING)
                .grab(true, false)
                .hint(sliderHeight, SWT.DEFAULT)
                .applyTo(sliderRight);

        right.setHorizontalScroller(new SliderScroller(sliderRight, false));
    }
}

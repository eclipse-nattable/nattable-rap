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
package org.eclipse.nebula.widgets.nattable.examples._700_AdditionalFunctions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _751_CopyExample extends AbstractNatExample {

    @Override
    public String getDescription() {
        return "This example shows how to register a different copy handling for a NatTable grid "
                + "that adds the header information to the copied body values and uses the "
                + "configured IDisplayConverter to create the String representation of the "
                + "copied values.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        panel.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Composite gridPanel = new Composite(panel, SWT.NONE);
        gridPanel.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and setting the ViewportLayer as
        // underlying layer. But in this case using the ViewportLayer directly
        // as body layer is also working.
        IDataProvider bodyDataProvider =
                new DefaultBodyDataProvider<>(PersonService.getPersons(100), propertyNames);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(
                        rowHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(
                        columnHeaderDataProvider,
                        rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(
                        cornerDataLayer,
                        rowHeaderLayer,
                        columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(
                        viewportLayer,
                        columnHeaderLayer,
                        rowHeaderLayer,
                        cornerLayer);

        // register a CopyDataCommandHandler that also copies the headers and
        // uses the configured IDisplayConverters
        CopyDataCommandHandler copyHandler =
                new CopyDataCommandHandler(
                        selectionLayer,
                        columnHeaderDataLayer,
                        rowHeaderDataLayer);
        copyHandler.setCopyFormattedText(true);
        gridLayer.registerCommandHandler(copyHandler);

        final NatTable natTable = new NatTable(gridPanel, gridLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Button addColumnButton = new Button(buttonPanel, SWT.PUSH);
        addColumnButton.setText("Copy");
        addColumnButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(
                        new CopyDataToClipboardCommand("\t", //$NON-NLS-1$
                                System.getProperty("line.separator"), //$NON-NLS-1$
                                natTable.getConfigRegistry()));
            }
        });

        return panel;
    }

}

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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.ILayerExporter;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommand;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommandHandler;
import org.eclipse.nebula.widgets.nattable.export.config.DefaultExportBindings;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.painter.NatTableBorderOverlayPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _771_ExcelExportExample extends AbstractNatExample {

    @Override
    public String getDescription() {
        return "This example shows how to trigger an export for a NatTable.\n"
                + "You can also use the [Ctrl] + [E] to trigger the export via key bindings.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
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

        IDataProvider bodyDataProvider =
                new DefaultBodyDataProvider<>(PersonService.getPersons(10), propertyNames);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        viewportLayer.setRegionName(GridRegion.BODY);

        // add the ExportCommandHandler to the ViewportLayer in order to make
        // exporting work
        viewportLayer.registerCommandHandler(new ExportCommandHandler(viewportLayer));

        final NatTable natTable = new NatTable(gridPanel, viewportLayer, false);

        // adding this configuration adds the styles and the painters to use
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DefaultExportBindings());

        natTable.addOverlayPainter(new NatTableBorderOverlayPainter());

        natTable.configure();

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Button exportButton = new Button(buttonPanel, SWT.PUSH);
        exportButton.setText("Export");
        exportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(
                        new ExportCommand(
                                natTable.getConfigRegistry(),
                                natTable.getShell(),
                                false,
                                false,
                                null,
                                false,
                                () -> {
                                    MessageDialog.openInformation(
                                            natTable.getShell(),
                                            "Export Succeeded",
                                            "The export finished successfully");
                                }));
            }
        });

        Button downloadButton = new Button(buttonPanel, SWT.PUSH);
        downloadButton.setText("Download");
        downloadButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ILayerExporter exporter = natTable.getConfigRegistry().getConfigAttribute(
                        ExportConfigAttributes.EXPORTER,
                        DisplayMode.NORMAL);

                if (exporter != null) {
                    if (exporter.getResult() instanceof File) {
                        File file = (File) exporter.getResult();
                        UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
                        StringBuilder url = new StringBuilder();
                        url.append(RWT.getServiceManager().getServiceHandlerUrl("downloadServiceHandler"));
                        url.append('&').append("fileURI").append('=').append(file.toURI());
                        launcher.openURL(url.toString());
                    } else {
                        MessageDialog.openInformation(natTable.getShell(), "Download Export", "There is no export result available.");
                    }
                }
            }
        });

        return panel;
    }

}

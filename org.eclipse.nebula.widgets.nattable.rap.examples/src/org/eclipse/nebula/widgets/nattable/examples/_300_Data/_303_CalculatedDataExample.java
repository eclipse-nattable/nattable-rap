/*******************************************************************************
 * Copyright (c) 2012, 2025 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.NumberValues;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * Example that demonstrates how to implement a NatTable instance that shows
 * calculated values.
 */
public class _303_CalculatedDataExample extends AbstractNatExample {

    public static final String COLUMN_ONE_LABEL = "ColumnOneLabel";
    public static final String COLUMN_TWO_LABEL = "ColumnTwoLabel";
    public static final String COLUMN_THREE_LABEL = "ColumnThreeLabel";
    public static final String COLUMN_FOUR_LABEL = "ColumnFourLabel";
    public static final String COLUMN_FIVE_LABEL = "ColumnFiveLabel";

    private EventList<NumberValues> valuesToShow = GlazedLists.eventList(new ArrayList<NumberValues>());

    @Override
    public String getDescription() {
        return "This example demonstrates how to create a NatTable that contains calculated values.\n"
                + "The first three columns are editable, while the last two columns contain the calculated values.\n"
                + "The values in column four and five will automatically update when committing the edited values.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Composite gridPanel = new Composite(panel, SWT.NONE);
        gridPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        // property names of the NumberValues class
        String[] propertyNames = { "columnOneNumber", "columnTwoNumber",
                "columnThreeNumber", "columnFourNumber", "columnFiveNumber" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("columnOneNumber", "100%");
        propertyToLabelMap.put("columnTwoNumber", "Value One");
        propertyToLabelMap.put("columnThreeNumber", "Value Two");
        propertyToLabelMap.put("columnFourNumber", "Sum");
        propertyToLabelMap.put("columnFiveNumber", "Percentage");

        this.valuesToShow.add(createNumberValues());
        this.valuesToShow.add(createNumberValues());

        ConfigRegistry configRegistry = new ConfigRegistry();

        CalculatingGridLayer gridLayer =
                new CalculatingGridLayer(this.valuesToShow, configRegistry, propertyNames, propertyToLabelMap);
        DataLayer bodyDataLayer = gridLayer.getBodyDataLayer();

        final ColumnOverrideLabelAccumulator columnLabelAccumulator =
                new ColumnOverrideLabelAccumulator(bodyDataLayer);
        bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
        registerColumnLabels(columnLabelAccumulator);

        final NatTable natTable = new NatTable(gridPanel, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new CalculatingEditConfiguration());
        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Button addRowButton = new Button(buttonPanel, SWT.PUSH);
        addRowButton.setText("add row");
        addRowButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                _303_CalculatedDataExample.this.valuesToShow.add(createNumberValues());
                natTable.refresh();
            }
        });

        Button resetButton = new Button(buttonPanel, SWT.PUSH);
        resetButton.setText("reset");
        resetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                _303_CalculatedDataExample.this.valuesToShow.clear();
                _303_CalculatedDataExample.this.valuesToShow.add(createNumberValues());
                _303_CalculatedDataExample.this.valuesToShow.add(createNumberValues());
                natTable.refresh();
            }
        });

        return panel;
    }

    private void registerColumnLabels(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
        columnLabelAccumulator.registerColumnOverrides(0, COLUMN_ONE_LABEL);
        columnLabelAccumulator.registerColumnOverrides(1, COLUMN_TWO_LABEL);
        columnLabelAccumulator.registerColumnOverrides(2, COLUMN_THREE_LABEL);
        columnLabelAccumulator.registerColumnOverrides(3, COLUMN_FOUR_LABEL);
        columnLabelAccumulator.registerColumnOverrides(4, COLUMN_FIVE_LABEL);
    }

    private NumberValues createNumberValues() {
        NumberValues nv = new NumberValues();
        nv.setColumnOneNumber(100); // the value which should be used as 100%
        nv.setColumnTwoNumber(20); // the value 1 for calculation
        nv.setColumnThreeNumber(30); // the value 2 for calculation
        // as column 4 and 5 should be calculated values, we don't set them to
        // the NumberValues object
        return nv;
    }

    /**
     * The column accessor which is used for retrieving the data out of the
     * model. While the values for the first three columns are returned
     * directly, the values for column four and five are calculated.
     */
    class CalculatingDataProvider implements IColumnAccessor<NumberValues> {

        @Override
        public Object getDataValue(NumberValues rowObject, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return rowObject.getColumnOneNumber();
                case 1:
                    return rowObject.getColumnTwoNumber();
                case 2:
                    return rowObject.getColumnThreeNumber();
                case 3: // calculate the sum
                    return rowObject.getColumnTwoNumber()
                            + rowObject.getColumnThreeNumber();
                case 4: // calculate the percentage
                    return Double.valueOf((double) rowObject.getColumnTwoNumber()
                            + (double) rowObject.getColumnThreeNumber())
                            / rowObject.getColumnOneNumber();
            }
            return null;
        }

        @Override
        public void setDataValue(NumberValues rowObject, int columnIndex, Object newValue) {
            // because of the registered conversion, the new value has to be an
            // Integer
            switch (columnIndex) {
                case 0:
                    rowObject.setColumnOneNumber((Integer) newValue);
                    break;
                case 1:
                    rowObject.setColumnTwoNumber((Integer) newValue);
                    break;
                case 2:
                    rowObject.setColumnThreeNumber((Integer) newValue);
                    break;
            }
        }

        @Override
        public int getColumnCount() {
            // this example will show exactly 5 columns
            return 5;
        }

    }

    /**
     * The body layer stack for the {@link _303_CalculatedDataExample}. Consists
     * of
     * <ol>
     * <li>ViewportLayer</li>
     * <li>SelectionLayer</li>
     * <li>ColumnHideShowLayer</li>
     * <li>ColumnReorderLayer</li>
     * <li>DataLayer</li>
     * </ol>
     */
    class CalculatingBodyLayerStack extends AbstractLayerTransform {

        private final DataLayer bodyDataLayer;
        private final ColumnReorderLayer columnReorderLayer;
        private final ColumnHideShowLayer columnHideShowLayer;
        private final SelectionLayer selectionLayer;
        private final ViewportLayer viewportLayer;

        public CalculatingBodyLayerStack(EventList<NumberValues> valuesToShow, ConfigRegistry configRegistry) {
            IDataProvider dataProvider =
                    new ListDataProvider<>(valuesToShow, new CalculatingDataProvider());
            this.bodyDataLayer = new DataLayer(dataProvider);
            this.columnReorderLayer = new ColumnReorderLayer(this.bodyDataLayer);
            this.columnHideShowLayer = new ColumnHideShowLayer(this.columnReorderLayer);
            this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
            this.viewportLayer = new ViewportLayer(this.selectionLayer);
            setUnderlyingLayer(this.viewportLayer);

            registerCommandHandler(new CopyDataCommandHandler(this.selectionLayer));
        }

        public DataLayer getDataLayer() {
            return this.bodyDataLayer;
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }
    }

    /**
     * The {@link GridLayer} used by the {@link _303_CalculatedDataExample}.
     */
    class CalculatingGridLayer extends GridLayer {

        public CalculatingGridLayer(
                EventList<NumberValues> valuesToShow,
                ConfigRegistry configRegistry,
                final String[] propertyNames,
                Map<String, String> propertyToLabelMap) {

            super(true);
            init(valuesToShow, configRegistry, propertyNames, propertyToLabelMap);
        }

        private void init(
                EventList<NumberValues> valuesToShow,
                ConfigRegistry configRegistry,
                final String[] propertyNames,
                Map<String, String> propertyToLabelMap) {

            // Body
            CalculatingBodyLayerStack bodyLayer = new CalculatingBodyLayerStack(valuesToShow, configRegistry);

            SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

            // Column header
            IDataProvider columnHeaderDataProvider =
                    new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
            ILayer columnHeaderLayer =
                    new ColumnHeaderLayer(
                            new DefaultColumnHeaderDataLayer(columnHeaderDataProvider), bodyLayer, selectionLayer);

            // Row header
            IDataProvider rowHeaderDataProvider =
                    new DefaultRowHeaderDataProvider(bodyLayer.getDataLayer().getDataProvider());
            ILayer rowHeaderLayer =
                    new RowHeaderLayer(
                            new DefaultRowHeaderDataLayer(rowHeaderDataProvider), bodyLayer, selectionLayer);

            // Corner
            ILayer cornerLayer = new CornerLayer(
                    new DataLayer(
                            new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)),
                    rowHeaderLayer,
                    columnHeaderLayer);

            setBodyLayer(bodyLayer);
            setColumnHeaderLayer(columnHeaderLayer);
            setRowHeaderLayer(rowHeaderLayer);
            setCornerLayer(cornerLayer);
        }

        public DataLayer getBodyDataLayer() {
            return ((CalculatingBodyLayerStack) getBodyLayer()).getDataLayer();
        }
    }

    /**
     * Configuration for enabling and configuring edit behaviour.
     */
    class CalculatingEditConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.ALWAYS_EDITABLE);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.NEVER_EDITABLE, DisplayMode.EDIT,
                    _303_CalculatedDataExample.COLUMN_FOUR_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.NEVER_EDITABLE, DisplayMode.EDIT,
                    _303_CalculatedDataExample.COLUMN_FIVE_LABEL);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultIntegerDisplayConverter(), DisplayMode.NORMAL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultIntegerDisplayConverter(), DisplayMode.EDIT);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new PercentageDisplayConverter(), DisplayMode.NORMAL,
                    _303_CalculatedDataExample.COLUMN_FIVE_LABEL);
        }
    }

}

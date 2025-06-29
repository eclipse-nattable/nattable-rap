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
package org.eclipse.nebula.widgets.nattable.examples.fixtures;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.examples.examples._104_Styling._000_Styled_grid;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

/**
 * Extends the default column header style configuration to add custom painters
 * for the column headers. This has to be added to the table using the
 * addConfiguration() method.
 *
 * @see _000_Styled_grid
 */
public class StyledColumnHeaderConfiguration extends DefaultColumnHeaderStyleConfiguration {

    public StyledColumnHeaderConfiguration() {
        this.font = GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD));
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        super.configureRegistry(configRegistry);
        addNormalModeStyling(configRegistry);
        addSelectedModeStyling(configRegistry);
    }

    private void addSelectedModeStyling(IConfigRegistry configRegistry) {
        // FIXME replace with gradient background painter
        // Image selectedBgImage =
        // GUIHelper.getImageByURL("selectedColumnHeaderBg",
        // getClass().getResource("selected_column_header_bg.png"));
        //
        // TextPainter txtPainter = new TextPainter(false, false);
        // ICellPainter selectedCellPainter = new BackgroundImagePainter(
        // txtPainter, selectedBgImage, GUIHelper.getColor(192, 192, 192));
        // // If sorting is enables we still want the sort icon to be drawn.
        // SortableHeaderTextPainter selectedHeaderPainter = new
        // SortableHeaderTextPainter(
        // selectedCellPainter, false, true);
        //
        // configRegistry.registerConfigAttribute(
        // CellConfigAttributes.CELL_PAINTER, selectedHeaderPainter,
        // DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
    }

    private void addNormalModeStyling(IConfigRegistry configRegistry) {
        // FIXME replace with gradient background painter
        // Image bgImage = GUIHelper.getImageByURL("columnHeaderBg",
        // getClass().getResource("column_header_bg.png"));
        //
        // TextPainter txtPainter = new TextPainter(false, false);
        // ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter,
        // bgImage, GUIHelper.getColor(192, 192, 192));
        // SortableHeaderTextPainter headerPainter = new
        // SortableHeaderTextPainter(
        // bgImagePainter, false, true);
        //
        // configRegistry.registerConfigAttribute(
        // CellConfigAttributes.CELL_PAINTER, headerPainter,
        // DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
        // configRegistry.registerConfigAttribute(
        // CellConfigAttributes.CELL_PAINTER, headerPainter,
        // DisplayMode.NORMAL, GridRegion.CORNER);
    }
}

//*******************************************************************************
// * Copyright (c) 2025 Dirk Fauth and others.
// *
// * This program and the accompanying materials are made
// * available under the terms of the Eclipse Public License 2.0
// * which is available at https://www.eclipse.org/legal/epl-2.0/
// *
// * SPDX-License-Identifier: EPL-2.0
// *
// * Contributors:
// *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
// ******************************************************************************/
var handleEvent = function(event) {
    var columnHeaderDimensions = event.widget.getData("columnHeaderDimensions");
    var columnBorders = event.widget.getData("columnBorders");
    var rowHeaderDimensions = event.widget.getData("rowHeaderDimensions");
    var rowBorders = event.widget.getData("rowBorders");

    var columnDragEnabled = event.widget.getData("columnDragEnabled");
    var rowDragEnabled = event.widget.getData("rowDragEnabled");

    // only react on left mouse button
    if (event.button == 1) {
        if (columnHeaderDimensions) {
            let chd = JSON.parse(columnHeaderDimensions);
            if (event.y > chd[0] && event.y < chd[1]) {
                for (let i = 0; i < columnBorders.length; i++) {
                    let border = columnBorders[i];
                    if (event.x > (border - 5) && event.x < (border + 5)) {
                        event.widget.setData("columnPositionToResize", i);
                        event.widget.setData("initialResizeX", border);
                        break;
                    }
                }
                
                if (columnDragEnabled) {
                    event.widget.setData("columnDragStartX", event.x);
                }
            }
        }

        if (rowHeaderDimensions) {
            let rhd = JSON.parse(rowHeaderDimensions);
            if (event.x > rhd[0] && event.x < rhd[1]) {
                for (let i = 0; i < rowBorders.length; i++) {
                    let border = rowBorders[i];
                    if (event.y > (border - 5) && event.y < (border + 5)) {
                        event.widget.setData("rowPositionToResize", i);
                        event.widget.setData("initialResizeY", border);
                        break;
                    }
                }
        
                if (rowDragEnabled) {
                    event.widget.setData("rowDragStartY", event.y);
                }
            }
        }
    }
};
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
    var columnResizeBorders = event.widget.getData("columnResizeBorders");
    var rowHeaderDimensions = event.widget.getData("rowHeaderDimensions");
    var rowResizeBorders = event.widget.getData("rowResizeBorders");

    var columnDragEnabled = event.widget.getData("columnDragEnabled");
    var rowDragEnabled = event.widget.getData("rowDragEnabled");

    // only react on left mouse button
    if (event.button == 1) {
        if (columnHeaderDimensions) {
            let chd = JSON.parse(columnHeaderDimensions);
            if (event.y > chd[0] && event.y < chd[1]) {
				let resize = false;
                for (let i = 0; i < columnResizeBorders.length; i++) {
                    let border = columnResizeBorders[i];
                    if (event.x > (border - 5) && event.x < (border + 5)) {
                        event.widget.setData("columnPositionToResize", i);
                        event.widget.setData("initialResizeX", border);
						resize = true;
                        break;
                    }
                }
                
                if (columnDragEnabled && !resize) {
                    event.widget.setData("columnDragStartX", event.x);
                }
            }
        }

        if (rowHeaderDimensions) {
            let rhd = JSON.parse(rowHeaderDimensions);
            if (event.x > rhd[0] && event.x < rhd[1]) {
				let resize = false;
                for (let i = 0; i < rowResizeBorders.length; i++) {
                    let border = rowResizeBorders[i];
                    if (event.y > (border - 5) && event.y < (border + 5)) {
                        event.widget.setData("rowPositionToResize", i);
                        event.widget.setData("initialResizeY", border);
						resize = true;
                        break;
                    }
                }
        
                if (rowDragEnabled && !resize) {
                    event.widget.setData("rowDragStartY", event.y);
                }
            }
        }
    }
};
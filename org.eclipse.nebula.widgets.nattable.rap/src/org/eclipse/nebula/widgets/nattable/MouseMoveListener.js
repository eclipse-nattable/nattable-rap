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
    var columnPositionToResize = event.widget.getData("columnPositionToResize");
    var rowPositionToResize = event.widget.getData("rowPositionToResize");
    var columnDragStartX = event.widget.getData("columnDragStartX");
    var rowDragStartY = event.widget.getData("rowDragStartY");

    // only change the cursor if a header is available and if no column or row is currently resized
    if (columnPositionToResize === null && rowPositionToResize === null) {
        let chd = columnHeaderDimensions ? JSON.parse(columnHeaderDimensions) : null;
        let rhd = rowHeaderDimensions ? JSON.parse(rowHeaderDimensions) : null;
        if (chd && event.y > chd[0] && event.y < chd[1]) {
            if (columnDragStartX) {
                document.body.style.cursor = 'grabbing';
            }
            else if (isOverColumnBorder(columnBorders, event)) {
                document.body.style.cursor = 'col-resize';
            }
            else {
                document.body.style.cursor = 'default';
            }
        } 
        else if (rhd && event.x > rhd[0] && event.x < rhd[1]) {
            if (columnDragStartX || rowDragStartY) {
                document.body.style.cursor = 'grabbing';
            }
            else if (isOverRowBorder(rowBorders, event)) {
                document.body.style.cursor = 'row-resize';
            }            
            else {
                document.body.style.cursor = 'default';
            }
        } 
        else {
            document.body.style.cursor = 'default';
        }
    } 

    // TODO paint the drag line
    /*
    if (columnPositionToResize) {
        var natTable = rap.getObject(event.widget.getData("control"));
        const ctx = natTable.$el.get()[0].firstChild.getContext("2d");

        // Start a new Path
        ctx.beginPath();
        ctx.moveTo(event.x, 0);
        ctx.lineTo(event.x, natTable.getClientArea()[2]);
        
        // Draw the Path
        ctx.stroke();
    }
    */
};

function isOverColumnBorder(columnBorders, event) {
    var border = 0;
    for (let i = 0; i < columnBorders.length; i++) {
        border = columnBorders[i];
        if (event.x > (border - 5) && event.x < (border + 5)) {
            return true;
        }
    }
    return false;
}

function isOverRowBorder(rowBorders, event) {
    var border = 0;
    for (let i = 0; i < rowBorders.length; i++) {
        border = rowBorders[i];
        if (event.y > (border - 5) && event.y < (border + 5)) {
            return true;
        }
    }
    return false;
}

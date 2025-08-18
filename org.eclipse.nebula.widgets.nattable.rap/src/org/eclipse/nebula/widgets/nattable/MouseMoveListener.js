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

    // paint the resize drag line
    if (columnPositionToResize !== null || rowPositionToResize !== null) {
        var natTable = rap.getObject(event.widget.getData("control"));
        
        // check for an overlay canvas
        // create one if it has not been created yet
        var overlayCanvas = document.getElementById("resizeOverlay");
        if (!overlayCanvas) {
            const natTableCanvas = natTable.$el.get()[0].firstChild;
            overlayCanvas = document.createElement("canvas");
            overlayCanvas.id = "resizeOverlay";
            overlayCanvas.width = natTableCanvas.width;
            overlayCanvas.height = natTableCanvas.height;

            const natTableParent = natTableCanvas.parentElement;    
            const styles = "position:absolute;left:" + natTableParent.style.left + ";top:" + natTableParent.style.top + ";width:" + natTableCanvas.style.width + ";height:" + natTableCanvas.style.height + ";";
            overlayCanvas.style = styles;
            
            natTableCanvas.parentElement.appendChild(overlayCanvas);
        }
                
        const ctx = overlayCanvas.getContext("2d");
        
        // clear
        ctx.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height);
        
            // Start a new Path
        if (columnPositionToResize !== null) {
            ctx.beginPath();
            ctx.moveTo(event.x, 0);
            ctx.lineTo(event.x, natTable.getClientArea()[2]);
        }

        if (rowPositionToResize !== null) {
            ctx.beginPath();
            ctx.moveTo(0, event.y);
            ctx.lineTo(natTable.getClientArea()[3], event.y);
        }

        // Draw the Path
        ctx.stroke();
    }
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

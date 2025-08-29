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
    var columnBorders = event.widget.getData("columnBorders");
    var rowHeaderDimensions = event.widget.getData("rowHeaderDimensions");
	var rowResizeBorders = event.widget.getData("rowResizeBorders");
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
            if (columnDragStartX !== null) {
                document.body.style.cursor = 'grabbing';
            }
            else if (isOverBorder(columnResizeBorders, event.x)) {
                document.body.style.cursor = 'col-resize';
            }
            else {
                document.body.style.cursor = 'default';
            }
        } 
        else if (rhd && event.x > rhd[0] && event.x < rhd[1]) {
            if (columnDragStartX !== null || rowDragStartY !== null) {
                document.body.style.cursor = 'grabbing';
            }
            else if (isOverBorder(rowResizeBorders, event.y)) {
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

	var natTable = rap.getObject(event.widget.getData("control"));
	const overlayCanvas = getOverlayCanvas(natTable);
	const ctx = overlayCanvas.getContext("2d");
	
    // clear
    ctx.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height);

    // paint the column resize drag line
    if (columnPositionToResize !== null) {
        ctx.beginPath();
        ctx.moveTo(event.x, 0);
        ctx.lineTo(event.x, natTable.getClientArea()[2]);

		// Draw the Path
		ctx.stroke();
    }

    // paint the row resize drag line
    if (rowPositionToResize !== null) {
        ctx.beginPath();
        ctx.moveTo(0, event.y);
        ctx.lineTo(natTable.getClientArea()[3], event.y);

		// Draw the Path
		ctx.stroke();
    }
	
	// paint the column reorder drag line
	if (columnDragStartX !== null) {
		var target = getTargetBorders(event.x, columnBorders);
		let halfWidth = (target[1] - target[0]) / 2;
		if (event.x < (target[0] + halfWidth)) {
			ctx.beginPath();
			ctx.moveTo(target[0], 0);
			ctx.lineTo(target[0], natTable.getClientArea()[2]);

			// Draw the Path
			ctx.stroke();			
		} else if (target[0] > -1 && event.x <= target[1]) {
			ctx.beginPath();
			ctx.moveTo(target[1], 0);
			ctx.lineTo(target[1], natTable.getClientArea()[2]);

			// Draw the Path
			ctx.stroke();						
		}
	}
				
	// paint the row reorder drag line
	if (rowDragStartY !== null) {
		var target = getTargetBorders(event.y, rowBorders);
		let halfHeight = (target[1] - target[0]) / 2;
		if (event.y < (target[0] + halfHeight)) {
			ctx.beginPath();
			ctx.moveTo(0, target[0]);
			ctx.lineTo(natTable.getClientArea()[2], target[0]);
	
			// Draw the Path
			ctx.stroke();			
		} else if (target[0] > -1 && event.y <= target[1]) {
			ctx.beginPath();
			ctx.moveTo(0, target[1]);
			ctx.lineTo(natTable.getClientArea()[2], target[1]);
	
			// Draw the Path
			ctx.stroke();						
		}
	}
};

function isOverBorder(resizeBorders, eventPos) {
    var border = 0;
    for (let i = 0; i < resizeBorders.length; i++) {
        border = resizeBorders[i];
        if (eventPos > (border - 5) && eventPos < (border + 5)) {
            return true;
        }
    }
    return false;
}

function getTargetBorders(pos, borders) {
	var border = -1;
	var left = -1;
	var right = -1;
	var i = 0;
	while (pos > border && i < borders.length) {
		left = border;
		border = borders[i++];
	}
	right = border;
	return [left, right];
}

function getOverlayCanvas(natTable) {
	// check for an overlay canvas
	// create one if it has not been created yet
	var overlayCanvas = document.getElementById("overlayCanvas");
	const natTableCanvas = natTable.$el.get()[0].firstChild;
	if (!overlayCanvas) {
	    overlayCanvas = document.createElement("canvas");
	    overlayCanvas.id = "overlayCanvas";
	    overlayCanvas.width = natTableCanvas.width;
	    overlayCanvas.height = natTableCanvas.height;

	    const styles = "position:absolute;left:0;top:0;width:" + natTableCanvas.style.width + ";height:" + natTableCanvas.style.height + ";";
	    overlayCanvas.style = styles;
	    
	    natTableCanvas.parentElement.appendChild(overlayCanvas);
		        
		const ntCtx = natTableCanvas.getContext("2d");
	
		const ctx = overlayCanvas.getContext("2d");
		ctx.setTransform(ntCtx.getTransform());
	}

	return overlayCanvas;
}

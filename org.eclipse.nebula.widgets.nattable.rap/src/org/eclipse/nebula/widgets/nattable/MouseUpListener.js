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
    var natTable = rap.getObject(event.widget.getData("control"));
    
    var columnPositionToResize = event.widget.getData("columnPositionToResize");
    var initialResizeX = event.widget.getData("initialResizeX");
    if (columnPositionToResize !== null) {

        var widthDiff = event.x - initialResizeX;

        var connection = rwt.remote.Connection.getInstance();
        connection.getMessageWriter().appendNotify(
            event.widget.getData("control"),
            "MouseUp",
            {
                count: event.count,
                button: event.button,
                x: event.x,
                y: event.y,
                time: 0,
                data: "columnResize " + columnPositionToResize + " " + widthDiff
            }
        );
        connection.send();

        event.widget.setData("columnPositionToResize", null);
        event.widget.setData("initialResizeX", null);
    }

    var rowPositionToResize = event.widget.getData("rowPositionToResize");
    var initialResizeY = event.widget.getData("initialResizeY");
    if (rowPositionToResize !== null) {

        var heightDiff = event.y - initialResizeY;

        var connection = rwt.remote.Connection.getInstance();
        connection.getMessageWriter().appendNotify(
            event.widget.getData("control"),
            "MouseUp",
            {
                count: event.count,
                button: event.button,
                x: event.x,
                y: event.y,
                time: 0,
                data: "rowResize " + rowPositionToResize + " " + heightDiff
            }
        );
        connection.send();

        event.widget.setData("rowPositionToResize", null);
        event.widget.setData("initialResizeY", null);
    }

    var overlayCanvas = document.getElementById("resizeOverlay");
    if (overlayCanvas) {
        overlayCanvas.remove();
    }
    
    var columnDragStartX = event.widget.getData("columnDragStartX");
    if (columnDragStartX !== null) {
        var connection = rwt.remote.Connection.getInstance();
        connection.getMessageWriter().appendNotify(
            event.widget.getData("control"),
            "MouseUp",
            {
                count: event.count,
                button: event.button,
                x: event.x,
                y: event.y,
                time: 0,
                data: "columnDrag " + columnDragStartX + " " + event.x + " " + event.y
            }
        );
        connection.send();
        
        event.widget.setData("columnDragStartX", null);
    }
    
    var rowDragStartY = event.widget.getData("rowDragStartY");
    if (rowDragStartY !== null) {
        var connection = rwt.remote.Connection.getInstance();
        connection.getMessageWriter().appendNotify(
            event.widget.getData("control"),
            "MouseUp",
            {
                count: event.count,
                button: event.button,
                x: event.x,
                y: event.y,
                time: 0,
                data: "rowDrag " + rowDragStartY + " " + event.x + " " + event.y
            }
        );
        connection.send();

        event.widget.setData("rowDragStartY", null);
    }
};

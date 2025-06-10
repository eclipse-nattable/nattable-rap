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
    var connection = rwt.remote.Connection.getInstance();
    connection.getMessageWriter().appendNotify(
        event.widget.getData("control"),
        "MouseWheel",
        {
            count: event.count,
            button: event.button,
            x: event.x,
            y: event.y,
            time: 0,
        }
    );
    connection.send();
};

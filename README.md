# NatTable RAP

This project contains an OSGi fragment for `org.eclipse.nebula.widgets.nattable.core`. Adding this fragment together with NatTable Core into a RAP application runtime, it is possible to include NatTable in RAP.

The fragment uses [Byte Buddy](https://github.com/raphw/byte-buddy) to inject the necessary RAP features to NatTable. 
This is necessary because `NatTable` is based on `Canvas`, but we need a specialized `CanvasOperationHandler` to get everything working in NatTable. 
This is especially necessary for the mouse interactions.

Additionally `NatTable#configure()` is extended this way to add support for scrolling and mouse interactions like mouse move and drag operations.

Note that there are several modifications needed in NatTable Core, to make NatTable work in RAP. You need at least NatTable 2.6.0 to get NatTable work in a RAP environment.

Further details on the modifications in NatTable Core are documented in [Modifications to support NatTable in RAP](https://github.com/eclipse-nattable/nattable/pull/146).

Further information about NatTable itself can be found in the [NatTable main repository](https://github.com/eclipse-nattable/nattable).


## Requirements

NatTable RAP needs at least:

* Java 21
* NatTable 2.6.0
* RAP 4.0.0

The necessary modifications in NatTable to make it work with RAP are documented in [this PR](https://github.com/eclipse-nattable/nattable/pull/146).


## Features

The following section contains information about the features added to NatTable in RAP that would not work there although working fine in SWT.

### Scrolling

NatTable is a _virtual table_ and only renders what is currently visible. This is done by only painting the currently visible part in the `Canvas`.
Internally the scrollbars are used to implement that faked scrolling behavior. In SWT this works although `Canvas` is not really scrollable.

In RAP/RWT a `Canvas` does not provide scrollbars, because it is not scrollable. This is discussed in the forum, e.g.
- [https://www.eclipse.org/forums/index.php/t/1104906/](https://www.eclipse.org/forums/index.php/t/1104906/)
- [https://www.eclipse.org/forums/index.php/t/239034/](https://www.eclipse.org/forums/index.php/t/239034/)

The suggestion to wrap the `Canvas`, respectively `NatTable` in a `ScrolledComposite` doesn't make sense, because it would violate the _virtual table_ concept.

NatTable provides a mechanism to replace the default SWT scrollbars of the `Composite` with custom scrollbars, e.g. `Slider`.
An example can be found in [NatTable with custom scrollbars](https://vogella.com/blog/nattable-with-custom-scrollbars/).

The RAP fragment makes use of this mechanism, re-layouts the NatTable and adds custom scrollbars. Additionally it adds a `MouseWheel` listener to support scrolling via mouse wheel.

### Re-rendering updates

To avoid that NatTable gets re-rendered too often, events that trigger a repaint are conflated. 
But the repaint is then triggered from a background thread, which does not work in RAP without a modification. 
To make it work a `ServerPushSession` is started automatically on `NatTable#configure()` following the documentation in [Server Push](https://eclipse.dev/rap/developers-guide/server-push.html).

### Mouse Move

In order to support mouse cursor changes when hovering over the column or row header, a `MouseMove` listener is added that uses client side [Scripting](https://eclipse.dev/rap/developers-guide/scripting.html). 
The necessary data is added in `NatTable#configure()` which is extended in `org.eclipse.nebula.widgets.nattable.RAPInitializer`.

### Mouse Drag (Reorder, Resize, GroupBy)

Via mouse drag operations it is possible to resize columns and rows and to reorder columns, column groups, rows and row groups. 
It is also possible to perform a groupBy action if that feature is used. 
As a mouse drag operation involves mouse movement, and the `MouseMove` event is by design not supported in RAP to avoid massive client-server communication, the drag operations need to be added in NatTable RAP in a different way. 
There are new listeners for `MouseDown`, `MouseMove` and `MouseUp` that use client side [Scripting](https://eclipse.dev/rap/developers-guide/scripting.html). 
While `MouseDown` and `MouseMove` only operate on the client side, `MouseUp` will trigger the commands to perform the actions in charge.

The implementation might not be 100% in sync with the features supported in NatTable Core, but the should be almost equal.

### Export / File Download

In SWT it is possible to open a `FileDialog` to specify the location to which a export should be generated to. 
This is not possible with RAP, which is described in [File Upload - File Dialog](https://eclipse.dev/rap/developers-guide/file-upload.html). 
In NatTable Core a modification was added to simply create the export file in a temporary directory when the platform is RAP. 
A user can then download the export file via the browser functionality, if a download link is provided. To do this you need to:

1. Add a download `ServiceHandler` that is able to provide the export file for download

```java
public class DownloadServiceHandler implements ServiceHandler {

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String fileUri = request.getParameter("fileURI");

        // Get the file content
        URI uri = URI.create(fileUri);
        byte[] download = Files.readAllBytes(Paths.get(uri));

        // Set the file name to be downloaded
        File file = new File(fileUri);
        String fileName = file.getName();

        // Send the file in the response
        response.setContentType("application/octet-stream");
        response.setContentLength(download.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.getOutputStream().write(download);
    }
}
```

2. Register that download server once for your application

Either via `ApplicationConfiguration`

```java
ServiceHandler handler = new DownloadServiceHandler();
application.addServiceHandler("downloadServiceHandler", handler);
```

or programmatically at runtime

```java
ServiceManager manager = RWT.getServiceManager();
ServiceHandler handler = new DownloadServiceHandler();
manager.registerServiceHandler("downloadServiceHandler", handler);
```

3. Provide a way so a user is able to download the export file, e.g. via a button

```java
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
```

This is also described in [Static Resources and Downloads](https://eclipse.dev/rap/developers-guide/resources.html).

## Not supported

As RAP has several limitations compared to SWT, there are features in NatTable that are not supported in RAP:

- Printing
- Open an export directly
- `NatTableContentTooltip` and subclasses like `FormulaTooltipErrorReporter` as the JFace Tooltip implementations are not available
- NatTable scaling, which is not really necessary in RAP as scaling can be done via the browser
- text decorations (underline, strikethrough) as the necessary `Font` API is missing
- `BackgroundImagePainter` and `PercentageBarDecorator` as the necessary SWT classes are missing
- Auto resizing because this uses in-memory pre-rendering of NatTable on an `Image` which is not supported in RAP
- As of now the Nebula Extension and the E4 Extension are not usable with RAP. This is mostly due to dependency issues, e.g. there are versioned package imports that can not be satisfied by RAP/RWT or there are `Require-Bundle` dependencies on `org.eclipse.swt` which can not be resolved.


## Downloads

You can find update site URLs and downloadable repository archives of the current release and all older releases in [NatTable Downloads](https://eclipse.dev/nattable/download.php).

If you are interested in SNAPSHOT builds, you can find the update site URLs and all downloadable resources in [NatTable RAP SNAPSHOTS](https://download.eclipse.org/nattable/nattable-rap/snapshots/) 

## Additional information

* [Project Details](https://projects.eclipse.org/projects/technology.nebula.nattable)
* [Contribution Guide](CONTRIBUTING.md)
* [EPLv2 License](LICENSE.md)

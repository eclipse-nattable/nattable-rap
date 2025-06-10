/*******************************************************************************
 * Copyright (c) 2025 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.examples.runner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.nebula.widgets.nattable.examples.INatExample;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NatExampleEntryPoint extends AbstractEntryPoint {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(NatExampleEntryPoint.class);

    private CTabFolder tabFolder;
    private final Map<INatExample, Control> exampleControlMap = new HashMap<>();
    private Map<String, INatExample> examplePathMap = new HashMap<>();
    private Link link;

    @Override
    protected void createContents(Composite parent) {
        try {
            List<String> examplePaths;
            InputStream inputStream = getClass().getResourceAsStream("/examples.index");
            if (inputStream != null) {
                examplePaths = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                    String line = reader.readLine();
                    while (line != null) {
                        examplePaths.add(line);
                        line = reader.readLine();
                    }
                }
            } else {
                System.out.println("examples.index not found, reconstructing");
                examplePaths = createExamplesIndex(null);
            }
            parent.setLayout(new GridLayout(2, false));

            // Nav tree
            final TreeViewer navTreeViewer = new TreeViewer(parent);
            GridData gridData = new GridData(GridData.FILL_VERTICAL);
            gridData.widthHint = 300;
            navTreeViewer.getControl().setLayoutData(gridData);
            final NavContentProvider contentProvider = new NavContentProvider();
            navTreeViewer.setContentProvider(contentProvider);
            navTreeViewer.setLabelProvider(new NavLabelProvider(contentProvider, this));
            navTreeViewer.setInput(examplePaths.toArray(new String[0]));
            navTreeViewer.addDoubleClickListener(new IDoubleClickListener() {

                @Override
                public void doubleClick(DoubleClickEvent event) {
                    TreeSelection selection = (TreeSelection) event.getSelection();
                    for (TreePath path : selection.getPaths()) {
                        // check for item - if node expand/collapse, if child
                        // open
                        if (contentProvider.hasChildren(path.getLastSegment()
                                .toString())) {
                            boolean expanded = navTreeViewer.getExpandedState(path);
                            navTreeViewer.setExpandedState(path, !expanded);
                        } else {
                            openExampleInTab(path.getLastSegment().toString());
                        }
                    }
                }

            });

            this.tabFolder = new CTabFolder(parent, SWT.BORDER);
            this.tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        } catch (IOException e) {
            MessageDialog.openError(getShell(), "Error", e.toString());
        }
    }

    public List<String> createExamplesIndex(String basedir) throws IOException {
        List<String> examples = new ArrayList<>();

        File examplesDir = new File(basedir, "src" + INatExample.BASE_PATH);
        findTutorialExamples(examplesDir, examples);

        examplesDir = new File(basedir, "src" + INatExample.CLASSIC_BASE_PATH);
        findExamples(examplesDir, examples, INatExample.CLASSIC_EXAMPLES_PREFIX);

        File examplesIndexFile = new File(new File(basedir, "src"), "examples.index");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(examplesIndexFile))) {
            for (String example : examples) {
                writer.write(example + "\n");
            }
            writer.flush();
        }

        return examples;
    }

    private void findTutorialExamples(File dir, List<String> examples) throws IOException {
        FilenameFilter packageFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches("_\\d{3}.*");
            }
        };

        List<String> sortedList = Arrays.asList(dir.list(packageFilter));
        Collections.sort(sortedList);
        for (String packageName : sortedList) {
            File f = new File(dir, packageName);
            if (f.isDirectory()) {
                findExamples(f, examples, INatExample.TUTORIAL_EXAMPLES_PREFIX);
            }
        }
    }

    public void findExamples(File dir, List<String> examples, String prefix) throws IOException {
        List<String> sortedList = Arrays.asList(dir.list());
        Collections.sort(sortedList);
        for (String s : sortedList) {
            File f = new File(dir, s);
            if (f.isDirectory()) {
                findExamples(f, examples, prefix);
            } else {
                String examplePath = dir.getCanonicalPath() + File.separator + s;
                // Convert to /-delimited path
                examplePath = examplePath.replace(File.separator, "/");
                if (examplePath.endsWith(".java")) {
                    examplePath = examplePath.replaceAll("^.*/src/", "").replaceAll("\\.java$", "");
                    examples.add(prefix + examplePath);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Class<? extends INatExample> getExampleClass(String examplePath) {
        String className = examplePath.replace('/', '.');
        try {
            Class<?> clazz = Class.forName(className);
            if (INatExample.class.isAssignableFrom(clazz)
                    && !Modifier.isAbstract(clazz.getModifiers())) {
                return (Class<? extends INatExample>) clazz;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public INatExample getExample(String examplePath) {
        INatExample example = this.examplePathMap.get(examplePath);
        if (example == null) {
            String path = examplePath;
            if (examplePath.startsWith("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX)) {
                path = examplePath.replace("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX, INatExample.BASE_PATH + "/");
            } else if (examplePath.startsWith("/" + INatExample.CLASSIC_EXAMPLES_PREFIX)) {
                path = examplePath.replace("/" + INatExample.CLASSIC_EXAMPLES_PREFIX, INatExample.CLASSIC_BASE_PATH + "/");
            }

            if (path.startsWith("/"))
                path = path.substring(1);

            Class<? extends INatExample> exampleClass = getExampleClass(path);
            if (exampleClass != null) {
                try {
                    example = exampleClass.getDeclaredConstructor().newInstance();
                    this.examplePathMap.put(examplePath, example);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return example;
    }

    private void openExampleInTab(final String examplePath) {
        final INatExample example = getExample(examplePath);
        if (example == null) {
            return;
        }

        final String exampleName = example.getName();
        final CTabItem tabItem = new CTabItem(this.tabFolder, SWT.CLOSE);
        tabItem.setText(exampleName);
        tabItem.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                // Stop
                example.onStop();

                Control exampleControl = NatExampleEntryPoint.this.exampleControlMap.get(example);
                if (exampleControl != null && !exampleControl.isDisposed()) {
                    exampleControl.dispose();
                }

                NatExampleEntryPoint.this.exampleControlMap.remove(example);
                NatExampleEntryPoint.this.examplePathMap.remove(examplePath);
                NatExampleEntryPoint.this.link.dispose();
            }
        });

        final Composite tabComposite = new Composite(this.tabFolder, SWT.NONE);
        tabComposite.setLayout(new GridLayout(1, false));

        // Create example control
        final Control exampleControl = example.createExampleControl(tabComposite);
        exampleControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.exampleControlMap.put(example, exampleControl);

        // Description
        final String description = example.getDescription();
        if (description != null && description.length() > 0) {
            final Group descriptionGroup = new Group(tabComposite, SWT.NONE);
            descriptionGroup.setText("Description");
            descriptionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            descriptionGroup.setLayout(new FillLayout());

            final Label descriptionLabel = new Label(descriptionGroup, SWT.WRAP);
            descriptionLabel.setText(description);
        }

        this.link = new Link(tabComposite, SWT.NONE);
        this.link.setText("<a href=\"" + examplePath + "\">View source</a>");

        final SelectionAdapter linkSelectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String path = event.text;
                if (path.startsWith("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX)) {
                    path = path.replace("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX, INatExample.BASE_PATH + "/");
                } else if (path.startsWith("/" + INatExample.CLASSIC_EXAMPLES_PREFIX)) {
                    path = path.replace("/" + INatExample.CLASSIC_EXAMPLES_PREFIX, INatExample.CLASSIC_BASE_PATH + "/");
                }
                String source = getResourceAsString(path + ".java");
                if (source != null) {
                    viewSource(exampleName, source);
                }
            }
        };
        this.link.addSelectionListener(linkSelectionListener);

        tabItem.setControl(tabComposite);

        // Start
        example.onStart();

        this.tabFolder.setSelection(tabItem);
    }

    private static String getResourceAsString(String resource) {
        try (InputStream inStream = NatExampleEntryPoint.class.getResourceAsStream(resource)) {
            if (inStream != null) {
                StringBuilder strBuf = new StringBuilder();
                int i = -1;
                while ((i = inStream.read()) != -1) {
                    strBuf.append((char) i);
                }

                return strBuf.toString();
            } else {
                System.out.println("null stream for resource " + resource);
            }
        } catch (IOException e) {
            LOG.error("Failed to read resource {}", resource, e);
        }

        return null;
    }

    private static void viewSource(String title, String source) {
        Display display = Display.getCurrent();
        Shell shell = new Shell(display);
        shell.setText(title);
        shell.setLayout(new FillLayout());

        Browser text = new Browser(shell, SWT.MULTI);
        text.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        text.setText("<pre>" + source + "</pre>");

        shell.open();
    }

}

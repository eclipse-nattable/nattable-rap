/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderCommand;
import org.eclipse.nebula.widgets.nattable.groupby.GroupByCommand;
import org.eclipse.nebula.widgets.nattable.groupby.GroupByCommand.GroupByAction;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.viewport.IScroller;
import org.eclipse.nebula.widgets.nattable.viewport.SliderScroller;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Slider;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
/**
 * Initializer that is called by the NatTable Activator to initialize the RAP environment for NatTable.
 */
public class RAPInitializer {

    /**
     * Initializes the RAP environment for NatTable. Installs an agent on the currently running Java virtual machine 
     * and adds an advice to override the getAdapter() method of the Canvas class to return the NatTableLCA instance
     * if getAdapter() of NatTable is called. 
     */
    public static void initialize() {
        // Installs an agent on the currently running Java virtual machine.
        ByteBuddyAgent.install();

        // Inject the NatTableLCA and NatTableOperationHandler classes into the class loader of the Canvas class
        Map<TypeDescription, byte[]> map = Map.of(
            TypeDescription.ForLoadedType.of(NatTableLCA.class), ClassFileLocator.ForClassLoader.read(NatTableLCA.class), 
            TypeDescription.ForLoadedType.of(NatTableOperationHandler.class), ClassFileLocator.ForClassLoader.read(NatTableOperationHandler.class));
        new ClassInjector.UsingUnsafe(Canvas.class.getClassLoader()).inject(map);
        
        // Redefine the Canvas class to return the NatTableLCA instance in getAdapter() when the WidgetLCA is requested
        ByteBuddy byteBuddy = new ByteBuddy();
        byteBuddy.redefine(Canvas.class)
            .visit(
                Advice.withCustomMapping()
                    .with(new Advice.AssignReturned.Factory())
                    .to(AdapterAdvice.class)
                    .on(ElementMatchers.named("getAdapter")))
            .make()
            .load(NatTable.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
            
        byteBuddy.redefine(NatTable.class)
            .visit(
                Advice
                    .to(ConfigureAdvice.class)
                    .on(ElementMatchers.named("configure")))
            .make()
            .load(NatTable.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }
    
    /**
     * Advice implementation that is used to redefine the getAdapter() method of the Canvas class.
     */
    public class AdapterAdvice {

        @SuppressWarnings({ "unchecked", "restriction" })
        @Advice.OnMethodEnter
        private static <T> T onEnter(@Advice.Argument(0) Class<T> adapter, @Advice.This Object thiz) {

            if ("org.eclipse.nebula.widgets.nattable.NatTable".equals(thiz.getClass().getName())
                    && adapter == WidgetLCA.class) {
                return (T) NatTableLCA.INSTANCE;
            }
            return null;
        }
        
        @Advice.OnMethodExit
        @Advice.AssignReturned.ToReturned
        private static <T> T onExit(
            @Advice.Enter T enterValue,
            @Advice.Return(readOnly = false) T returnValue) {
            
            if (enterValue != null) {
                return enterValue;
            }
            return returnValue;
        }
    }
    
    
    public class ConfigureAdvice {
        
        @Advice.OnMethodExit
        private static void onExit(@Advice.This Object thiz) {
            
        	NatTable natTable = (NatTable) thiz;
        	
        	// Add data that can be inspected by the client-side JavaScript code.
        	// https://eclipse.dev/rap/developers-guide/scripting.html
            WidgetUtil.registerDataKeys(
            		"control",
            		"columnHeaderDimensions",
            		"rowHeaderDimensions",
            		"columnBorders", 
            		"columnPositionToResize",
            		"initialResizeX",
            		"rowBorders", 
            		"rowPositionToResize",
            		"initialResizeY",
            		"columnDragEnabled",
            		"columnDragStartX",
            		"rowDragEnabled",
            		"rowDragStartY");
        	natTable.setData("control", WidgetUtil.getId(natTable));
        	
            natTable.addPaintListener(new PaintListener() {

                @Override
                public void paintControl(PaintEvent event) {
                	int columnHeaderBottomRow = findColumnHeaderBottomRow(natTable.getLayer());
                	int rowHeaderRightmostColumn = findRowHeaderRightmostColumn(natTable.getLayer());
                	if (columnHeaderBottomRow >= 0) {
                		int columnHeaderColumn = rowHeaderRightmostColumn >= 0 ? rowHeaderRightmostColumn + 1 : 0;
                		int[] columnHeaderDimensions = findColumnHeaderDimensions(natTable.getLayer());
                		natTable.setData("columnHeaderDimensions", Arrays.toString(columnHeaderDimensions));
                		
                		MutableIntList columnBorders = IntLists.mutable.empty();
                		for (int i = 0; i < columnHeaderColumn; i++) {
            				// add a negative value to indicate that the column is not resizable
            				columnBorders.add(-10);
                		}
                		for (int i = columnHeaderColumn; i < natTable.getColumnCount(); i++) {
                			if (natTable.isColumnPositionResizable(i)) {
                				Rectangle bounds = natTable.getCellByPosition(i, columnHeaderBottomRow).getBounds();
                				columnBorders.add(bounds.x + bounds.width);
                			} else {
                				// add a negative value to indicate that the column is not resizable
                				columnBorders.add(-10);
                			}
                		}
                		natTable.setData("columnBorders", columnBorders.toArray());
                		
                		if (findLayer(natTable.getLayer(), 0, ColumnReorderLayer.class) != null 
                				|| natTable.getProvidedLabels().contains(GridRegion.GROUP_BY_REGION)) {
							natTable.setData("columnDragEnabled", Boolean.TRUE);
						}
                		
                		if (findLayer(natTable.getLayer(), 0, RowReorderLayer.class) != null) {
                			natTable.setData("rowDragEnabled", Boolean.TRUE);
                		}
                	}          		

                	if (rowHeaderRightmostColumn >= 0) {
                		int rowHeaderRow = columnHeaderBottomRow >= 0 ? columnHeaderBottomRow + 1 : 0;
                		int[] rowHeaderDimensions = findRowHeaderDimensions(natTable.getLayer());
                		natTable.setData("rowHeaderDimensions", Arrays.toString(rowHeaderDimensions));
		                	
	                	MutableIntList rowBorders = IntLists.mutable.empty();
                		for (int i = 0; i < rowHeaderRow; i++) {
            				// add a negative value to indicate that the row is not resizable
                			rowBorders.add(-10);
                		}
	                    for (int i = rowHeaderRow; i < natTable.getRowCount(); i++) {
	                    	if (natTable.isRowPositionResizable(i)) {
	                    		Rectangle bounds = natTable.getCellByPosition(rowHeaderRightmostColumn, i).getBounds();
	                    		rowBorders.add(bounds.y + bounds.height);
	                    	} else {
	                    		// add a negative value to indicate that the row is not resizable
	                    		rowBorders.add(-10);
	                    	}
	                    }
	                    natTable.setData("rowBorders", rowBorders.toArray());
                	}

                }
            });

            natTable.addListener(SWT.MouseDown, MouseDownClientListener.getInstance());
            natTable.addListener(SWT.MouseMove, MouseMoveClientListener.getInstance());
            addMouseUpListener(natTable);

			// check if there is a ViewportLayer in the stack
        	// if one exists, add scrollbars by using Slider
			ViewportLayer viewPortLayer = findLayer(natTable.getLayer(), 0, ViewportLayer.class);
			if (viewPortLayer != null) {
				addScrollbars(natTable, viewPortLayer);
			}
            
			// finally open a ServerPushSession to be able to push events to the client from
			// a background thread. Needed for the EventConflaterChain that collects visual
			// changes and redraw only once every 100ms.
			// https://eclipse.dev/rap/developers-guide/server-push.html
        	ServerPushSession pushSession = new ServerPushSession();
        	pushSession.start();
        	natTable.addDisposeListener(new PushSessionDisposeListener(pushSession));
        }
    	
    }
    
    /**
	 * DisposeListener that is used to stop the {@link ServerPushSession} when the NatTable is disposed.
	 */
    public static class PushSessionDisposeListener implements DisposeListener {

    	final ServerPushSession pushSession;
    	
    	public PushSessionDisposeListener(ServerPushSession pushSession) {
    		this.pushSession = pushSession;
    	}
    	
		@Override
		public void widgetDisposed(DisposeEvent event) {
			pushSession.stop();
		}
    					
    }
    
	/**
	 * Find the {@link ILayer} of the requested type in the layer stack for the
	 * given column position.
	 * 
	 * @param layer          The layer to start searching from.
	 * @param columnPosition The column position to start the search.
	 * @param layerClass     The type of the layer to search for.
	 * @return The {@link ILayer} if found, otherwise <code>null</code>.
	 */
    @SuppressWarnings("unchecked")
	public static <T extends ILayer> T findLayer(ILayer layer, int columnPosition, Class<T> layerClass) {

        if (layerClass.isInstance(layer)) {
            return (T) layer;
        }

        // handle collection
        T result = null;
        Collection<ILayer> underlyingLayers = layer.getUnderlyingLayersByColumnPosition(columnPosition);
        if (underlyingLayers != null) {
            for (ILayer underlyingLayer : underlyingLayers) {
                if (underlyingLayer != null) {
                    result = findLayer(underlyingLayer, columnPosition, layerClass);
                }
            }
        }

        // handle vertical dependency
        if (result == null && layer instanceof DimensionallyDependentLayer) {
            result = findLayer(((DimensionallyDependentLayer) layer).getVerticalLayerDependency(), columnPosition, layerClass);
        }

        // handle horizontal dependency
        if (result == null && layer instanceof DimensionallyDependentLayer) {
            result = findLayer(((DimensionallyDependentLayer) layer).getHorizontalLayerDependency(), columnPosition, layerClass);
        }

        // in case of the CompositeFreezeLayer it can happen that for the last
        // columns in scrolled state the path cannot be determined as
        // getUnderlyingLayersByPosition() returns an empty collection because
        // it is above the ViewportLayer. We therefore need a special handling
        // to check additionally below the ViewportLayer.
        if (result == null && layer instanceof CompositeFreezeLayer) {
            result = findLayer(((CompositeFreezeLayer) layer).getChildLayerByLayoutCoordinate(1, 1), columnPosition, layerClass);
        }

        return result;
    }
    
	/**
	 * Find the bottom row position of the column header in the given layer.
	 * 
	 * @param layer The layer to search in.
	 * @return The bottom row position of the column header, or -1 if not found.
	 */
    public static int findColumnHeaderBottomRow(ILayer layer) {
        if (layer instanceof CompositeLayer) {
            CompositeLayer compositeLayer = (CompositeLayer) layer;
            ILayer columnHeader = compositeLayer.getChildLayerByRegionName(GridRegion.COLUMN_HEADER);
            if (columnHeader == null) {
                for (int x = 0; x < compositeLayer.getLayoutXCount(); x++) {
                    int rowOffset = 0;
                    for (int y = 0; y < compositeLayer.getLayoutYCount(); y++) {
                        ILayer childLayer = compositeLayer.getChildLayerByLayoutCoordinate(x, y);
                        int bottomRow = findColumnHeaderBottomRow(childLayer);
                        if (bottomRow >= 0) {
                            return bottomRow + rowOffset;
                        } else {
                            rowOffset += childLayer.getRowCount();
                        }
                    }
                }
            } else {
                return columnHeader.getRowCount() - 1;
            }
        }
        return -1;
    }

    public static int[] findColumnHeaderDimensions(ILayer layer) {
        if (layer instanceof CompositeLayer) {
            CompositeLayer compositeLayer = (CompositeLayer) layer;
            ILayer columnHeader = compositeLayer.getChildLayerByRegionName(GridRegion.COLUMN_HEADER);
            if (columnHeader == null) {
                for (int x = 0; x < compositeLayer.getLayoutXCount(); x++) {
                    int rowOffset = 0;
                    for (int y = 0; y < compositeLayer.getLayoutYCount(); y++) {
                        ILayer childLayer = compositeLayer.getChildLayerByLayoutCoordinate(x, y);
                        int[] bottomRow = findColumnHeaderDimensions(childLayer);
                        if (bottomRow[0] >= 0) {
                            bottomRow[0] += rowOffset;
                            bottomRow[1] += rowOffset;
                            return bottomRow;
                        } else {
                            rowOffset += childLayer.getHeight();
                        }
                    }
                }
            } else {
            	ILayerCell bottomRowCell = columnHeader.getCellByPosition(0, columnHeader.getRowCount() - 1);
            	if (bottomRowCell != null) {
            		Rectangle bottomRowBounds = bottomRowCell.getBounds();
            		return new int[] {
            				columnHeader.getStartYOfRowPosition(0),
            				bottomRowBounds.y + bottomRowBounds.height };
            	}
            }
        }
        return new int[] { -1 };
    }
    
	/**
	 * Find the rightmost column position of the row header in the given layer.
	 * 
	 * @param layer The layer to search in.
	 * @return The rightmost column position of the row header, or -1 if not found.
	 */
    public static int findRowHeaderRightmostColumn(ILayer layer) {
    	if (layer instanceof CompositeLayer) {
    		CompositeLayer compositeLayer = (CompositeLayer) layer;
    		ILayer rowHeader = compositeLayer.getChildLayerByRegionName(GridRegion.ROW_HEADER);
    		if (rowHeader == null) {
    			for (int y = 0; y < compositeLayer.getLayoutYCount(); y++) {
    				int columnOffset = 0;
    				for (int x = 0; x < compositeLayer.getLayoutXCount(); x++) {
    					ILayer childLayer = compositeLayer.getChildLayerByLayoutCoordinate(x, y);
    					int bottomColumn = findRowHeaderRightmostColumn(childLayer);
    					if (bottomColumn >= 0) {
    						return bottomColumn + columnOffset;
    					} else {
    						columnOffset += childLayer.getColumnCount();
    					}
    				}
    			}
    		} else {
    			return rowHeader.getColumnCount() - 1;
    		}
    	}
    	return -1;
    }

    public static int[] findRowHeaderDimensions(ILayer layer) {
    	if (layer instanceof CompositeLayer) {
    		CompositeLayer compositeLayer = (CompositeLayer) layer;
    		ILayer rowHeader = compositeLayer.getChildLayerByRegionName(GridRegion.ROW_HEADER);
    		if (rowHeader == null) {
    			for (int y = 0; y < compositeLayer.getLayoutYCount(); y++) {
    				int columnOffset = 0;
    				for (int x = 0; x < compositeLayer.getLayoutXCount(); x++) {
    					ILayer childLayer = compositeLayer.getChildLayerByLayoutCoordinate(x, y);
    					int[] bottomColumn = findRowHeaderDimensions(childLayer);
                        if (bottomColumn[0] >= 0) {
                        	bottomColumn[0] += columnOffset;
                        	bottomColumn[1] += columnOffset;
                            return bottomColumn;
                        } else {
                        	columnOffset += childLayer.getWidth();
                        }
    				}
    			}
    		} else {
    			ILayerCell rightMostCell = rowHeader.getCellByPosition(rowHeader.getColumnCount() - 1, 0);
    			if (rightMostCell != null) {
    				Rectangle rightMostBounds = rightMostCell.getBounds();
    				return new int[] {
    						rowHeader.getStartXOfColumnPosition(0),
    						rightMostBounds.x + rightMostBounds.width };
    			}
    		}
    	}
        return new int[] { -1 };
    }

	/**
	 * Adds custom scrollbars to the NatTable instance. The scrollbars are created
	 * using the SWT Slider widget.
	 * 
	 * @param natTable      The NatTable instance to which the scrollbars should be
	 *                      added.
	 * @param viewportLayer The ViewportLayer instance that is used to scroll the
	 *                      NatTable.
	 */
    public static void addScrollbars(NatTable natTable, ViewportLayer viewportLayer) {
    	Composite parent = natTable.getParent();
    	Layout parentLayout = parent.getLayout();
    	Object parentLayoutData = parent.getLayoutData();
    	if (parentLayoutData == null) {
    		if (parentLayout instanceof GridLayout) {
    			parentLayoutData = GridDataFactory
    					.fillDefaults()
    					.grab(true, true)
    					.create();
    		}
		}
    	
    	// create a composite with custom scrollbars as described in https://vogella.com/blog/nattable-with-custom-scrollbars/
    	Composite container = new Composite(parent, SWT.NONE);
    	container.setLayoutData(parentLayoutData);
		GridLayoutFactory
			.swtDefaults()
			.numColumns(2)
			.margins(0, 0)
			.spacing(0, 0)
			.applyTo(container);

		GridDataFactory
			.fillDefaults()
			.grab(true, true)
			.applyTo(natTable);
		natTable.setParent(container);
    	
		// vertical scrollbar wrapped in another composite for layout 
		Composite verticalComposite = new Composite(container, SWT.NONE); 
		GridLayoutFactory
		    .swtDefaults()
		    .margins(0, 0)
		    .spacing(0, 0)
		    .applyTo(verticalComposite); 
		GridDataFactory
		    .swtDefaults()
		    .hint(14, SWT.DEFAULT)
		    .align(SWT.BEGINNING, SWT.FILL)
		    .grab(false, true)
		    .applyTo(verticalComposite); 

		Slider vertical = new Slider(verticalComposite, SWT.VERTICAL); 
		GridDataFactory
		    .fillDefaults()
		    .grab(true, true)
		    .applyTo(vertical);
		IScroller<Slider> verticalScroller = new SliderScroller(vertical);

		// horizontal scrollbar wrapped in another composite for layout 
		Composite horizontalComposite = new Composite(container, SWT.NONE); 
		GridLayoutFactory
		    .swtDefaults()
		    .margins(0, 0)
		    .spacing(0, 0)
		    .applyTo(horizontalComposite); 
		GridDataFactory
		    .swtDefaults()
		    .hint(SWT.DEFAULT, 14)
		    .align(SWT.FILL, SWT.BEGINNING)
		    .grab(true, false)
		    .applyTo(horizontalComposite);

		Slider horizontal = new Slider(horizontalComposite, SWT.HORIZONTAL); 
		GridDataFactory
		    .fillDefaults()
		    .grab(true, true)
		    .applyTo(horizontal); 
		IScroller<Slider> horizontalScroller = new SliderScroller(horizontal);

		addMouseWheelListener(natTable, vertical);
		
		viewportLayer.setVerticalScroller(verticalScroller);
		viewportLayer.setHorizontalScroller(horizontalScroller);
    }
    
	/**
	 * Adds a mouse wheel listener to the NatTable instance that triggers a
	 * scrolling operation on the given NatTable.
	 * 
	 * @param natTable The NatTable instance to which the mouse wheel listener
	 *                 should be added.
	 * @param slider   The slider instance that is used to scroll the NatTable.
	 */
    public static void addMouseWheelListener(NatTable natTable, Slider slider) {
    	
        natTable.addListener(SWT.MouseWheel, MouseWheelClientListener.getInstance());

        natTable.addListener(SWT.MouseWheel, event -> {
            if (event.count > 0) {
                slider.setSelection(slider.getSelection() - slider.getIncrement());
            }
            else if (event.count < 0) {
            	slider.setSelection(slider.getSelection() + slider.getIncrement());
            }
            Event scrollEvent = new Event();
            scrollEvent.detail = 4;
            slider.notifyListeners(SWT.Selection, scrollEvent);
        });

	}
    
    /**
	 * Adds a mouse up listener to the NatTable instance that triggers a column
	 * resize operation on the given NatTable.
	 * 
	 * @param natTable The NatTable instance to which the mouse up listener should
	 *                 be added.
	 */
    public static void addMouseUpListener(NatTable natTable) {
    	
        natTable.addListener(SWT.MouseUp, MouseUpClientListener.getInstance());

        natTable.addListener(SWT.MouseUp, event -> {
            Object data = event.data;
            if (data != null) {
                String[] dataArray = data.toString().split(" ");
                if ("columnResize".equals(dataArray[0])) {
                    int pos = Integer.parseInt(dataArray[1]);
                    int widthDiff = Integer.parseInt(dataArray[2]);
                    int width = natTable.getColumnWidthByPosition(pos) + widthDiff;
                    natTable.doCommand(new ColumnResizeCommand(natTable, pos, width));
                    event.doit = false;                }
                else if ("rowResize".equals(dataArray[0])) {
                	int pos = Integer.parseInt(dataArray[1]);
                	int heightDiff = Integer.parseInt(dataArray[2]);
                	int height = natTable.getRowHeightByPosition(pos) + heightDiff;
                	natTable.doCommand(new RowResizeCommand(natTable, pos, height));
                	event.doit = false;
                } else if ("columnDrag".equals(dataArray[0])) {
					int startX = Integer.parseInt(dataArray[1]);
					// can not use the event.x and event.y as it seems they change somehow in the processing
					int endX = Integer.parseInt(dataArray[2]);
					int endY = Integer.parseInt(dataArray[3]);
					int fromColumn = natTable.getColumnPositionByX(startX);
					
					// check the region label to determine whether the column is dragged for column
					// reorder, column group reorder or for groupby
					LabelStack regionLabels = natTable.getRegionLabelsByXY(endX, endY);
			        if (regionLabels != null
			                && regionLabels.hasLabel(GridRegion.GROUP_BY_REGION)) {
			            natTable.doCommand(new GroupByCommand(GroupByAction.ADD, natTable.getColumnIndexByPosition(fromColumn)));
			            event.doit = false;
			        } else if (regionLabels != null 
			        		&& regionLabels.hasLabel(GridRegion.COLUMN_GROUP_HEADER)) {
			        	int toRow = natTable.getRowPositionByY(endY);
			        	int columnHeaderBottomRow = findColumnHeaderBottomRow(natTable.getLayer());
			        	int level = columnHeaderBottomRow - toRow - 1;
			        	int toColumn = getDragToGridColumnPosition(natTable, endX);
			        	ILayerCell cell = natTable.getCellByPosition(toColumn, toRow);
			        	int diffStart = toColumn - cell.getOriginColumnPosition();
			        	int diffEnd = (cell.getOriginColumnPosition() + cell.getColumnSpan()) - toColumn;
			        	if (diffStart < diffEnd) {
			        		toColumn = cell.getOriginColumnPosition();
			        		
			        	} else {
			        		toColumn = cell.getOriginColumnPosition() + cell.getColumnSpan();
			        	}
			        	natTable.doCommand(new ColumnGroupReorderCommand(natTable, level, fromColumn, toColumn));
			        	event.doit = false;
			        } else {
			        	int toColumn = getDragToGridColumnPosition(natTable, endX);
			        	natTable.doCommand(new ColumnReorderCommand(natTable, fromColumn, toColumn));
			        	event.doit = false;
			        }
				} else if ("rowDrag".equals(dataArray[0])) {
					int startY = Integer.parseInt(dataArray[1]);
					int endX = Integer.parseInt(dataArray[2]);
					int endY = Integer.parseInt(dataArray[3]);
					int fromRow = natTable.getRowPositionByY(startY);
					int toRow = getDragToGridRowPosition(natTable, endY);

					// check the region label to determine whether the row is dragged for row
					// reorder or row group reorder
					LabelStack regionLabels = natTable.getRegionLabelsByXY(endX, endY);
			        if (regionLabels != null 
			        		&& regionLabels.hasLabel(GridRegion.ROW_GROUP_HEADER)) {
			        	int toColumn = natTable.getColumnPositionByX(endX);
			        	int rowHeaderRightmostColumn = findRowHeaderRightmostColumn(natTable.getLayer());
			        	int level = rowHeaderRightmostColumn - toColumn - 1;
			        	ILayerCell cell = natTable.getCellByPosition(toColumn, toRow);
			        	int diffStart = toRow - cell.getOriginRowPosition();
			        	int diffEnd = (cell.getOriginRowPosition() + cell.getRowSpan()) - toRow;
			        	if (diffStart < diffEnd) {
			        		toRow = cell.getOriginRowPosition();
			        		
			        	} else {
			        		toRow = cell.getOriginRowPosition() + cell.getRowSpan();
			        	}
			        	natTable.doCommand(new RowGroupReorderCommand(natTable, level, fromRow, toRow));
			        	event.doit = false;
			        } else {
			        	natTable.doCommand(new RowReorderCommand(natTable, fromRow, toRow));
			        	event.doit = false;
			        }
				}
            }
        });
    	
    }
    
    
    protected static int getDragToGridColumnPosition(NatTable natTable, int x) {
        int dragToGridColumnPosition = -1;

        int gridColumnPosition = natTable.getColumnPositionByX(x);
        CellEdgeEnum moveDirection = getHorizontalMoveDirection(natTable, x);
        if (moveDirection != null) {
            if (moveDirection == CellEdgeEnum.LEFT) {
                dragToGridColumnPosition = gridColumnPosition;
            } else if (moveDirection == CellEdgeEnum.RIGHT) {
                dragToGridColumnPosition = gridColumnPosition + 1;
            }
        }

        return dragToGridColumnPosition;
    }

    protected static CellEdgeEnum getHorizontalMoveDirection(NatTable natTable, int x) {
        ILayerCell cell = getColumnCell(natTable, x);
        if (cell != null) {
            Rectangle selectedColumnHeaderRect = cell.getBounds();
            return CellEdgeDetectUtil.getHorizontalCellEdge(
            		selectedColumnHeaderRect, 
            		new Point(x, selectedColumnHeaderRect.y + (selectedColumnHeaderRect.height / 2)));
        }

        return null;
    }

    protected static ILayerCell getColumnCell(NatTable natTable, int x) {
        int gridColumnPosition = natTable.getColumnPositionByX(x);
        int gridRowPosition = findColumnHeaderBottomRow(natTable.getLayer());
        return natTable.getCellByPosition(gridColumnPosition, gridRowPosition);
    }

    
    
    protected static int getDragToGridRowPosition(NatTable natTable, int y) {
        int dragToGridRowPosition = -1;

    	int gridRowPosition = natTable.getRowPositionByY(y);
    	CellEdgeEnum moveDirection = getVerticalMoveDirection(natTable, y);
        if (moveDirection != null) {
            if (moveDirection == CellEdgeEnum.TOP) {
                dragToGridRowPosition = gridRowPosition;
            } else if (moveDirection == CellEdgeEnum.BOTTOM) {
                dragToGridRowPosition = gridRowPosition + 1;
            }
        }

        return dragToGridRowPosition;
    }

    protected static CellEdgeEnum getVerticalMoveDirection(NatTable natTable, int y) {
        ILayerCell cell = getRowCell(natTable, y);
        if (cell != null) {
            Rectangle selectedRowHeaderRect = cell.getBounds();
            return CellEdgeDetectUtil.getVerticalCellEdge(
            		selectedRowHeaderRect, 
            		new Point(selectedRowHeaderRect.x + (selectedRowHeaderRect.width / 2), y));
        }

        return null;
    }
    
    protected static ILayerCell getRowCell(NatTable natTable, int y) {
    	int gridColumnPosition = findRowHeaderRightmostColumn(natTable.getLayer());
    	int gridRowPosition = natTable.getRowPositionByY(y);
    	return natTable.getCellByPosition(gridColumnPosition, gridRowPosition);
    }

}

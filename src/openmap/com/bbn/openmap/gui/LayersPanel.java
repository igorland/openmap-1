// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayersPanel.java,v $
// $RCSfile: LayersPanel.java,v $
// $Revision: 1.3 $
// $Date: 2003/03/20 06:59:05 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.Serializable;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.*;
import javax.accessibility.*;

import com.bbn.openmap.*;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.LayerSupport;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The LayersPanel displays the list of layers that OpenMap can display.
 * The layer name is displayed accompanied by an on/off button and a
 * tool palette button. Pressing the on/off button will cause the the
 * map to display/remove the layer. Pressing the tool palette button 
 * will cause a window to be displayed containing widgets specific to 
 * that layer. <p>
 *
 * The order of the layers in the list reflects the order that the
 * layers are displayed on the map, with the bottom-most layer listed
 * on the panel underneath all the the other layers displayed on the
 * map.  The order of the layers is determined by their order in the
 * Layer[] passed in the setLayers method.  <p>
 *
 * The order of the layers can be changed by sending the LayersPanel
 * an ActionEvent with one of the string commands in the class, or by
 * sending a PropertyChangeEvent with a command and a Layer as the new
 * value. <P>
 *
 * In the standard GUI, the order can be changed by selecting a layer
 * by clicking on the layer's name (or on either of buttons), then
 * clicking on one of the four buttons on the left side of the panel.
 * The four buttons signify, from top to bottom: Move the selected
 * layer to the top; Move the selected layer up one position; Move the
 * selected layer down one position; Move the selected layer to the
 * bottom. <P>
 *
 * The LayersPanel can be used within a BeanContext.  If it is added
 * to a BeanConext, it will look for a LayerHandler to add itself to
 * as a LayerListener.  The LayersPanel can only listen to one
 * LayerHandler, so if more than one is found, only the last one found
 * will be used.  If another LayerHandler is added to the BeanContext
 * later, the new LayerHandler will be used.  The LayersPanel is also
 * considered to be a Tool, which will cause a button that will bring
 * up the LayersPanel to be automatically added to the ToolPanel if a
 * ToolPanel is part of the BeanContext.<P>
 *
 * When the LayersPanel discovers a BufferedLayerMapBean is being
 * used, it adds a special LayerPane to its LayerPane list that shows
 * which layers are being buffered in the MapBean.  This special
 * LayerPane shows up as a line in the list, and all layers below that
 * line are being specially buffered by the BufferedLayerMapBean. <P>
 *
 * The properties that can be set for the LayersPanel: <pre>
 * # When the BufferedLayerMapBean is used, a divider will be
 * # displayed in the list of layers showing which layers are in the
 * # MapBean buffer (below the line).  Commands to move layers, by
 * # default, respect this divider, requiring more commands to have
 * # layers cross it.
 * boundary=true
 * # Add control buttons - use "none" for no button.  If undefined,
 * # the LayerControlButtonPanel will be created automatically.
 * controls=com.bbn.openmap.gui.LayerControlButtonPanel
 * # Any control properties added here, prepended by "controls"...
 * controls.configuration=WEST
 * </pre>
 */
public class LayersPanel extends OMToolComponent
    implements Serializable, ActionListener, LayerListener, PropertyChangeListener
{
    /** Action command for the layer order buttons. */
    public final static String LayerTopCmd = "LayerTopCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerBottomCmd = "LayerBottomCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerUpCmd = "LayerUpCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerDownCmd = "LayerDownCmd";
    /** Action command removing a layer. */
    public final static String LayerRemoveCmd = "LayerRemoveCmd";
    /** Action command adding a layer. */
    public final static String LayerAddCmd = "LayerAddCmd";
    /** Action command for notification that a layer has been selected. */
    public final static String LayerSelectedCmd = "LayerSelected";
    /** 
     * Action command for notification that a layer has been
     * deselected. Not so reliable.  Usually a selection notification
     * means that others are deselected.
     */
    public final static String LayerDeselectedCmd = "LayerDeselected";

    /**
     * A property to set the class to create for layer order controls.
     * If undefined, a LayerControlButtonPanel in its default configuration
     * will be created.  For no controls added, use (none) for this
     * property.
     */
    public final static String ControlButtonsProperty = "controls";
    /**
     * A property that can be used for controlling how the to top and
     * to bottom cammands will be interpreted when a
     * BufferedLayerMapBean is used.  See the definition of
     * bufferedBoundary.
     */
    public final static String BufferedBoundaryProperty = "boundary";
    /**
     * A value for the (controls) property to not include control
     * buttons in the interface.
     */
    public final static String NO_CONTROLS = "none";

    /** Default key for the LayersPanel Tool. */
    public final static String defaultKey = "layerspanel";

    /**
     * The LayerHandler to listen to for LayerEvents, and also to
     * notify if the layer order should change. 
     */
    protected transient LayerHandler layerHandler = null;
    /**
     * Panel that lets you dynamically add and configure layers.
     */
    protected transient LayerAddPanel layerAddPanel = null;
    /**
     * The components holding the layer name label, the on/off
     * indicator and on button, and the palette on/off indicator and
     * palette on button. 
     */
    protected transient LinkedList panes;
    /** The internal component that holds the panes. */
    protected transient JPanel panesPanel;
    /** The scroll pane to use for panes. */
    protected transient JScrollPane scrollPane;
    /** The Layer order adjustment button group. */
    protected transient ButtonGroup bg;
    /** The ActionListener that will bring up the LayersPanel. */
    protected ActionListener actionListener;
    /**
     * The frame used when the LayersPanel is used in an application.  
     */
    protected transient JFrame layersWindowFrame;
    /** The frame used when the LayersPanel is used in an applet. */
    protected transient JInternalFrame layersWindow;
    /** The set of buttons that control the layers. */
    protected LayerControlButtonPanel controls = null;
    /**
     * Hashtable that tracks LayerPanes for layers, with the layer as
     * the key and LayerPane as the value.
     */
    protected Hashtable paneLookUp = new Hashtable();
    /**
     * A special LayerPane used when the LayersPanel senses that a
     * BufferedLayerMapBean is being used.  This LayersPanel is a
     * separating line showing which layers are part of the MapBean's
     * buffer, and which are not.
     */
    protected LayerPane backgroundLayerSeparator = null;
    /**
     * Behavior flag so that if there is a background buffered layer
     * on the MapBean, and a buffered layer divider in the
     * LayersPanel, whether commands instructing a layer to the top or
     * bottom of the list should honor the virtual boundary between
     * buffered and unbuffered layers.  That is, if a layer is on the
     * bottom of the buffered list and is instructed to go to the top
     * of the overal list, it will only first travel to the top of the
     * buffered layers.  On a subsequent top command, it will go to
     * the top of the list.  The same behavior applies for going down.
     * True is default.  If set to false, these commands will just
     * send the selected layer to the top and bottom of the entire
     * list.
     */
    protected boolean bufferedBoundary = true;

    /**
     * Construct the LayersPanel.
     *
     * @param lHandler the LayerHandler controlling the layers.
     */
    public LayersPanel() {
	super();
	setKey(defaultKey);
	setLayout(new BorderLayout());
    }

    /**
     * Construct the LayersPanel.
     *
     * @param lHandler the LayerHandler controlling the layers.
     */
    public LayersPanel(LayerHandler lHandler) {
	this();
	setLayerHandler(lHandler);
    }

    /** 
     * Set the LayerHandler that the LayersPanel listens to.  If the
     * LayerHandler passed in is not null, the LayersMenu will be
     * added to the LayerHandler LayerListener list, and the
     * LayersMenu will receive a LayerEvent with the current
     * layers. <P>
     *
     * If there is a LayerHandler that is already being listened to,
     * then the LayersPanel will remove itself from current LayerHandler
     * as a LayerListener, before adding itself to the new LayerHandler. <P>
     *
     * Lastly, if the LayerHandler passed in is null, the LayersPanel
     * will disconnect itself from any LayerHandler currently held,
     * and reset itself with no layers.
     *
     * @param lh LayerHandler to listen to, and to use to reorder the
     * layers.  
     */
    public void setLayerHandler(LayerHandler lh) {
	if (layerHandler != null) {
	    layerHandler.removeLayerListener(this);
	}
	layerHandler = lh;
	if (layerHandler != null) {
	    layerHandler.addLayerListener(this);
	} else {
	    setLayers(new Layer[0]);
	}
	updateLayerPanes(layerHandler);
    }

    /** 
     * Get the LayerHandler that the LayersPanel listens to and uses
     * to reorder layers.
     * @return LayerHandler.
     */
    public LayerHandler getLayerHandler() {
	return layerHandler;
    }

    /**
     * Set the layerpanes with the given layerhandler
     * @param layerHandler The LayerHandler controlling the layers
     */
    protected void updateLayerPanes(LayerHandler layerHandler) {
	Iterator it = getPanes().iterator();
	while (it.hasNext()) {
	    ((LayerPane)it.next()).setLayerHandler(layerHandler);
	}
    }

    /**
     * LayerListener interface method.  A list of layers will be
     * added, removed, or replaced based on on the type of LayerEvent.
     * The LayersPanel only reacts to LayerEvent.ALL events, to reset
     * the components in the LayersPanel.
     *
     * @param evt a LayerEvent.  
     */
    public void setLayers(LayerEvent evt) {
        Layer[] layers = evt.getLayers();
	int type = evt.getType();

	if (type==LayerEvent.ALL) {
	    Debug.message("layerspanel", "LayersPanel received layers update");
	    setLayers(layers);
	}
    }

    /** 
     * Tool interface method. The retrieval tool's interface. This
     * method creates a button that will bring up the LayersPanel.
     *
     * @return String The key for this tool.  
     */
    public Container getFace() {
	JButton layerButton = null;

	if (getUseAsTool()) {
	    layerButton = new JButton(new ImageIcon(OMToolSet.class.getResource("layers.gif"), "Layer Controls"));
	    layerButton.setBorderPainted(false);
	    layerButton.setToolTipText("Layer Controls");
	    layerButton.setMargin(new Insets(0,0,0,0));
	    layerButton.addActionListener(getActionListener());
	}

	return layerButton;
    }	
    
    /** 
     * Get the ActionListener that triggers the LayersPanel.  Useful
     * to have to provide an alternative way to bring up the
     * LayersPanel.  
     * 
     * @return ActionListener
     */
    public ActionListener getActionListener() {

	if (actionListener == null) {
	    // Try to group the applet-specific stuff in here...
	    if (Environment.getBoolean(Environment.UseInternalFrames)) {

		layersWindow = new JInternalFrame(
		    "Layers",
		    /*resizable*/ true,
		    /*closable*/ true,
		    /*maximizable*/ false,
		    /*iconifiable*/ true);
 		layersWindow.setBounds(2, 2, 328, 300);
		layersWindow.setContentPane(this);
		layersWindow.setOpaque(true);
		try {
		    layersWindow.setClosed(true);//don't show until it's needed
		} catch (java.beans.PropertyVetoException e) {}
		
		actionListener = ( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
			    try {
				if (layersWindow.isClosed()) {
				    layersWindow.setClosed(false);
				    // hmmm is this the best way to do this?

				    JLayeredPane desktop = 
					Environment.getInternalFrameDesktop();

				    if (desktop != null) {
					desktop.remove(layersWindow);
					desktop.add(layersWindow, 
						    JLayeredPane.PALETTE_LAYER);
					layersWindow.setVisible(true);
				    }
				}
			    } catch (java.beans.PropertyVetoException e) {
				System.err.println(e);
			    }
			}
		    });
		
	    } else { // Working as an application...
		layersWindowFrame = new JFrame("Layers");
 		layersWindowFrame.setBounds(2, 2, 328, 300);
		layersWindowFrame.setContentPane(this);
		layersWindowFrame.setVisible(false);//don't show until it's needed
		
		actionListener = ( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
			    layersWindowFrame.setVisible(true);
			    layersWindowFrame.setState(java.awt.Frame.NORMAL);
			}
		    });
	    }
	}

	return actionListener;
    }

    /**
     * Set the layers that are in the LayersPanel.  Make sure that the
     * layer[] is the same as that passed to any other OpenMap
     * component, like the LayersMenu.  This method checks to see if
     * the layer[] has actually changed, in order or in size.  If it
     * has, then createPanel() is called to rebuild the LayersPanel.
     *
     * @param inLayers the array of layers.  
     */
    public void setLayers(Layer[] inLayers) {
	Layer[] layers = inLayers;

	if (inLayers == null) {
	    layers = new Layer[0];
	}

	if (Debug.debugging("layerspanel")) {
	    Debug.output("LayersPanel.setLayers() with " + 
			 layers.length + " layers.");
	}

	LinkedList panes = getPanes();
	int separatorOffset = 0;
	if (backgroundLayerSeparator != null &&
	    panes.contains(backgroundLayerSeparator)) {
	    separatorOffset = 1;
	}

	if (panes.size() - separatorOffset != layers.length) {
	    // if the panel hasn't been created yet, or if someone has
	    // changed the layers on us, rebuild the panel.
	    createPanel(layers);
	    return;
	}

	int i = 0;
	Iterator it = panes.iterator();
	while (it.hasNext() && i < layers.length) {
	    LayerPane pane = (LayerPane)it.next();

	    if (pane == backgroundLayerSeparator) {
		continue;
	    }

	    if (pane.getLayer() != layers[i]) {
		// If the layer order sways at all, then we start over
		// and rebuild the panel
		createPanel(layers);
		return;
	    } else {
		pane.updateLayerLabel();
	    }

	    // Do this just in case someone has changed something
	    // somewhere else...
	    pane.setLayerOn(layers[i].isVisible());
	    i++;
	}

	// One last check for a mismatch...
	if (it.hasNext() || i < layers.length) {
	    createPanel(layers);
	}
	//  If we get here, it means that what we had is what we
	//  wanted.
    }

    protected LinkedList getPanes() {
	if (panes == null) {
	    panes = new LinkedList();
	}
	return panes;
    }

    protected void setPanes(LinkedList lpa) {
	panes = lpa;
    }

    /**
     * Create the panel that shows the LayerPanes.  This method
     * creates the on/off buttons, palette buttons, and layer labels,
     * and adds them to the scrollPane used to display all the layers.
     *
     * @param inLayers the Layer[] that reflects all possible layers
     * that can be added to the map.
     */
    public void createPanel(Layer[] inLayers) {
	Debug.message("layerspanel", "LayersPanel.createPanel()");

	if (scrollPane != null) {
	    remove(scrollPane);
	}

	Layer[] layers = inLayers;
	if (layers == null) {
	    layers = new Layer[0];
	}

	if (panesPanel == null) {
	    panesPanel = new JPanel();
	    panesPanel.setLayout(new BoxLayout(panesPanel, BoxLayout.Y_AXIS));
	    panesPanel.setAlignmentX(LEFT_ALIGNMENT);
	    panesPanel.setAlignmentY(BOTTOM_ALIGNMENT);
	} else {
	    ((BoxLayout)panesPanel.getLayout()).invalidateLayout(panesPanel);
	    panesPanel.removeAll();
	}

 	if (bg == null) {
	    bg = new ButtonGroup();
	}

	LinkedList panes = new LinkedList();
	LinkedList backgroundPanes = new LinkedList();

	// populate the arrays of CheckBoxes and strings used to fill
	// the JPanel for the panes
	for (int i = 0; i < layers.length; i++) {
	    Layer layer = layers[i];
	    if (layer == null) {
		Debug.output("LayersPanel caught null layer, " + i +
			     " out of " + layers.length);
		continue;
	    }

	    LayerPane lpane = (LayerPane)paneLookUp.get(layer);

	    if (lpane == null) {
		if (Debug.debugging("layercontrol")) {
		    Debug.output("LayersPanel: Creating LayerPane for " + 
				 layer.getName());
		}
		lpane = createLayerPaneForLayer(layer, layerHandler, bg);
		lpane.addPropertyChangeListener(LayerSelectedCmd, this);
		lpane.addPropertyChangeListener(LayerDeselectedCmd, this);
		paneLookUp.put(layer, lpane);
	    } else {
		// In case this has been modified elsewhere...
		lpane.setLayerOn(layer.isVisible());
	    }

	    if (layer.getAddAsBackground() &&
		backgroundLayerSeparator != null) {
		backgroundPanes.add(lpane);
	    } else {
		panes.add(lpane);
		panesPanel.add(lpane);
	    }
        }

	if (backgroundPanes.size() != 0) {
	    if (Debug.debugging("layerspanel")) {
		Debug.output("Adding BackgroundLayerSeparator");
	    }
	    panes.add(backgroundLayerSeparator);
	    panesPanel.add(backgroundLayerSeparator);
	    panes.addAll(backgroundPanes);

	    Iterator it = backgroundPanes.iterator();
	    while (it.hasNext()) {
		panesPanel.add((LayerPane)it.next());
	    }

	} else if (backgroundLayerSeparator != null) {
	    if (Debug.debugging("layerspanel")) {
		Debug.output("No layers are background layers, adding separator");
	    }
	    panes.add(backgroundLayerSeparator);
	    panesPanel.add(backgroundLayerSeparator);
	}
	
	setPanes(panes);

	if (scrollPane != null) {
	    remove(scrollPane);
	    scrollPane.removeAll();
	    scrollPane = null;
	}

	scrollPane = new JScrollPane(
	    panesPanel, 
	    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
	    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	add(scrollPane, BorderLayout.CENTER);
	revalidate();
    }

    /**
     * Called when a new LayerPane needs to be created for a layer.
     * You can use this to extend LayerPane and return something else
     * that fits your GUI.
     */
    protected LayerPane createLayerPaneForLayer(Layer layer, 
						LayerHandler layerHandler,
						ButtonGroup bg) {
	return new LayerPane(layer, layerHandler, bg);
    }

    public void deletePanes(LinkedList dpanes) {
	Debug.message("layerspanel", "LayersPanel.deletePanes()");
	if (dpanes != null) {
	    paneLookUp.clear();
	    Iterator it = dpanes.iterator();
	    while (it.hasNext()) {
		LayerPane pane = (LayerPane)it.next();
		if (pane != null && pane != backgroundLayerSeparator) {
		    pane.removePropertyChangeListener(this);
		    pane.cleanup(bg);
		}
	    }
	}

	// Shouldn't call this, but it's the only thing
	// that seems to make it work...
	if (Debug.debugging("helpgc")) {
	    System.gc();
	}
    }

    /**
     * Set up the buttons used to move layers up and down, or
     * add/remove layers.  The button component should hook itself up
     * to the LayersPanel, and assume that the LayersPanel has a
     * BorderLayout with the list in the center spot.
     */
    protected void createControlButtons() {
	controls = new LayerControlButtonPanel(this);
    }

    public void setControls(LayerControlButtonPanel lpb) {
	controls = lpb;
	if (lpb != null) {
	    lpb.setLayersPanel(this);
	}
    }

    public LayerControlButtonPanel getControls() {
	return controls;
    }

    /**
     * Method associated with the ActionListener interface.  This
     * method listens for action events meant to change the order of
     * the layers, as fired by the layer order buttons.
     *
     * @param e ActionEvent 
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
	String command = e.getActionCommand();
	if (Debug.debugging("layerspanel")) {
	    Debug.output("LayersPanel.actionPerformed(): " + command);
	}

	try {
	    LayerPane pane = findSelectedPane();
	    if (pane != null) {
		moveLayer(pane, command);
	    }
	} catch (NullPointerException npe) {
	} catch (ArrayIndexOutOfBoundsException aioobe) {
	}
    }

    /**
     * Change a layer's position.
     */
    public void moveLayer(Layer layer, String command) {
	if (Debug.debugging("layercontrol")) {
	    Debug.output("LayersPanel.moveLayer(): " + command +
			 " for " + layer.getName());
	}

	moveLayer((LayerPane)paneLookUp.get(layer), command);
    }


    /**
     * Change a layer's position, with the layer represented by a
     * LayerPane.
     */
    protected void moveLayer(LayerPane lp, String command) {

	if (lp == null) {

	    if (Debug.debugging("layercontrol")) {
		Debug.output("LayersPanel.moveLayer(): LayerPane not represented on list");
	    }

	    if (command == LayerRemoveCmd) {
		// OK, here's a hidden trick. If no layers are selected
		// and the minus sign is clicked, then this is called.
		System.gc();
	    }
	    return;
	}

	LinkedList panes = getPanes();
	int row = panes.indexOf(lp);

	boolean boundary = false;
	int bls_row = -1;
	if (backgroundLayerSeparator != null) {
	    bls_row = panes.indexOf(backgroundLayerSeparator);
	    boundary = bufferedBoundary;
	}

	if (command.equals(LayerTopCmd)) {
	    // Move layer selected layer to top
	    panes.remove(lp);
	    if (boundary && bls_row > 0 && row > bls_row + 1) {
		// If the backgroundLayerSeparator is more than one 
		// above it, move to just below it on the first top
		// command.
		panes.add(bls_row + 1, lp);
	    } else {
		panes.addFirst(lp);
	    }

	    rejiggerMapLayers();
	} else if (command.equals(LayerBottomCmd)) {
	    // Move layer selected layer to bottom
	    panes.remove(lp);

	    if (boundary && bls_row > 0 && row < bls_row - 1) {
		// If the backgroundLayerSeparator is more than one 
		// below it, move to just above it on the first top
		// command.
		panes.add(bls_row - 1, lp);
	    } else {
		panes.addLast(lp);
	    }

	    rejiggerMapLayers();
	} else if (command.equals(LayerUpCmd)) {
	    // Move layer selected layer up one
	    if (row <= 0) return;
	    panes.remove(row);
	    panes.add(row - 1, lp);
	    rejiggerMapLayers();
	} else if (command.equals(LayerDownCmd)) {
	    // Move layer selected layer up one
	    if (row < 0 || row == panes.size() - 1) return;
	    panes.remove(row);
	    panes.add(row + 1, lp);
	    rejiggerMapLayers();
	} else if (command.equals(LayerRemoveCmd)) {

	    if (layerHandler == null) {
		return;
	    }

	    // This order is somewhat important.  lp.getLayer() will 
	    // be null after lp.cleanup.  lp.setSelected() will cause
	    // a series of property change notifications.
	    lp.setSelected(false);
	    lp.getLayer().setPaletteVisible(false);
	    paneLookUp.remove(lp.getLayer());
	    layerHandler.removeLayer(lp.getLayer());
	    lp.cleanup(bg);

	    // Shouldn't call this, but it's the only thing
	    // that seems to make it work...
	    if (Debug.debugging("helpgc")) {
		System.gc();
	    }
	    
	    return;

	} else if (command.equals(LayerAddCmd)) {
	    if (layerAddPanel != null) {
		layerAddPanel.showPanel();
	    }
	}
    }

    /**
     * Find the selected LayerPane in the current LayerPane list.
     * Will return null if there isn't a selected pane.
     */
    protected LayerPane findSelectedPane() {
	Iterator it = getPanes().iterator();
	while (it.hasNext()) {
	    LayerPane pane = (LayerPane)it.next();
	    if (pane.isSelected()) {
		return pane;
	    }
	}
	return null;
    }

   /**
     * Makes a new layer cake of active layers to send to
     * LayerHandler.setLayers().
     *
     * @param neworder tells whether the order of the layers has
     * changed.
     * @param selectedRow the currently selected layer in the panel,
     * used to reset the scrollPane so that the row is visible (set to
     * -1 if unknown).  
     */
    protected void rejiggerMapLayers() {
	Debug.message("layerspanel", "LayersPanel.rejiggerMapLayers()");

	if (layerHandler == null) {
	    // Why bother doing anything??
	    return;
	}

	int selectedRow = -1;

	panesPanel.removeAll();

	LinkedList panes = getPanes();
	LinkedList layerList = new LinkedList();

	int bufferIndex = Integer.MAX_VALUE;

	int i = 0; // track layer index
	Iterator it = panes.iterator();
	while (it.hasNext()) {

	    LayerPane pane = (LayerPane)it.next();

	    if (pane == backgroundLayerSeparator) {
		panesPanel.add(backgroundLayerSeparator);
		bufferIndex = i++;
		continue;
	    }

	    Layer layer = pane.getLayer();
	    layer.setAddAsBackground(i > bufferIndex);
	    panesPanel.add(pane);
	    layerList.add(layer);

	    if (pane.isSelected()) {
		selectedRow = i;
	    }
	    i++;
	}

	scrollPane.revalidate();
	
	// Scroll up or down as necessary to keep selected row viewable
	if (selectedRow >= 0) {
	    int spheight = scrollPane.getHeight();
	    JScrollBar sb = scrollPane.getVerticalScrollBar();
	    int sv = sb.getValue();
	    int paneheight = ((LayerPane)panes.get(selectedRow)).getHeight();
	    int rowvalue = selectedRow*paneheight;
	    // Don't reset scrollBar unless the selected row
	    // is not in the viewable range
	    if (!((rowvalue > sv) && (rowvalue < spheight+sv))) {
		sb.setValue(rowvalue);
	    }
	}

	Object[] layerArray = layerList.toArray();
	int length = layerArray.length;
	Layer[] newLayers = new Layer[length];

	for (int j = 0; j < length; j++) {
	    newLayers[j] = (Layer)layerArray[j];
	}

	layerHandler.setLayers(newLayers);
    }

    /** 
     * Update the layer names - if a layer name has changed, tell the
     * LayerPanes to check with their layers to update their labels. 
     */
    public synchronized void updateLayerLabels() {
	Iterator it = getPanes().iterator();
	while (it.hasNext()) {
	    ((LayerPane)it.next()).updateLayerLabel();
	}
    }

    public void propertyChange(PropertyChangeEvent pce) {
	String command = pce.getPropertyName();
	Object obj = pce.getNewValue();

	if (Debug.debugging("layercontrol")) {
	    Debug.output("LayersPanel receiving PropertyChangeEvent " + 
			 command + ", " + pce.toString());
	}

	if ((command == LayerSelectedCmd ||
	     command == LayerDeselectedCmd) && 
	    obj instanceof Layer) {

	    if (Debug.debugging("layercontrol")) {
		Debug.output("LayersPanel: layer panel notification that layer is selected: " + ((Layer)obj).getName());
	    }
	    firePropertyChange(command, null, ((Layer)obj));

	} else if ((command == LayersPanel.LayerTopCmd ||
		    command == LayersPanel.LayerBottomCmd ||
		    command == LayersPanel.LayerUpCmd ||
		    command == LayersPanel.LayerDownCmd ||
		    command == LayersPanel.LayerRemoveCmd) && 
		   obj instanceof Layer) {
	    if (Debug.debugging("layercontrol")) {
		Debug.output("LayersPanel: layer panel notification that layer should be raised: " + ((Layer)obj).getName());
	    }
	    moveLayer((Layer)obj, command);
	}
    }
    
    /**
     * Called when the LayersPanel is added the BeanContext, or when
     * another object is added to the BeanContext after the
     * LayerHandler has been added.  This allows the LayersPanel to
     * keep up-to-date with any objects that it may be interested in,
     * namely, the LayerHandler.  If a LayerHandler has already been
     * added, the new LayerHandler will replace it.
     *
     * @param it Iterator to use to go through the objects added to
     * the BeanContext.  
     */
    public void findAndInit(Object someObj) {
	if (someObj instanceof LayerHandler) {
	    // do the initializing that need to be done here
	    Debug.message("layerspanel","LayersPanel found a LayerHandler");
	    setLayerHandler((LayerHandler)someObj);
	}

	if (someObj instanceof BufferedLayerMapBean) {
	    if (Debug.debugging("layerspanel")) {
		Debug.output("LayersPanel found BufferedLayerMapBean, creating separator panel");
	    }
	    backgroundLayerSeparator = LayerPane.getBackgroundLayerSeparator(" --- Background Layers --- ");
	}

	// Don't want to forward ourselves on to controls, supposedly
	// they already know.
	if (controls instanceof LightMapHandlerChild && someObj != this) {
	    ((LightMapHandlerChild)controls).findAndInit(someObj);
	}
    }

    /** 
     * BeanContextMembershipListener method.  Called when an object
     * has been removed from the parent BeanContext.  If a
     * LayerHandler is removed, and it's the current one being
     * listened to, then the layers in the panel will be wiped clean.
     *
     * @param bcme event that provides an iterator to use for the
     * removed objects.  
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof LayerHandler) {
	    // do the initializing that need to be done here
	    Debug.message("layerspanel","LayersPanel removing LayerHandler");
	    if (getLayerHandler() == (LayerHandler) someObj) {
		setLayerHandler(null);
	    }
	}

	// Don't want to forward ourselves on to controls, supposedly
	// they already know.
	if (controls instanceof LightMapHandlerChild && someObj != this) {
	    ((LightMapHandlerChild)controls).findAndUndo(someObj);
	}
    }

    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);
	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	String controlString = 
	    props.getProperty(prefix + ControlButtonsProperty);

	if (controlString != NO_CONTROLS) {
	    if (controlString == null) {
		createControlButtons();
	    } else {
		Object obj = ComponentFactory.create(
		    controlString, prefix + ControlButtonsProperty, props);
		
		if (obj instanceof LayerControlButtonPanel) {
		    setControls((LayerControlButtonPanel)obj);
		}
	    }
	}
    }

    public Properties getProperties(Properties props) {
	props = super.getProperties(props);

	String prefix = PropUtils.getScopedPropertyPrefix(this);
	LayerControlButtonPanel controls = getControls();
	if (controls != null) {
	    props.put(prefix + ControlButtonsProperty, controls.getClass().getName());
	    controls.getProperties(props);
	}
	return props;
    }

    public Properties getPropertyInfo(Properties props) {
	props = super.getPropertyInfo(props);
	props.put(ControlButtonsProperty, "Class to use for layer control buttons (Optional)");
	LayerControlButtonPanel controls = getControls();
	if (controls != null) {
	    controls.getPropertyInfo(props);
	}
	return props;
    }
}


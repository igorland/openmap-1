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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMGraphic.java,v $
// $RCSfile: EditableOMGraphic.java,v $
// $Revision: 1.2 $
// $Date: 2003/04/26 01:10:46 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.event.MouseEvent;
import java.awt.*;

import com.bbn.openmap.event.MapMouseAdapter;
import com.bbn.openmap.layer.util.stateMachine.*;
import com.bbn.openmap.omGraphics.editable.EOMGStateMachine;
import com.bbn.openmap.omGraphics.event.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The EditableOMGraphic is a shell that controls actions to edit or
 * create a graphic.  This class contains a state machine that defines
 * how mouse events will be interpreted to modify the OMGraphic
 * contained within.  Any class that extends this one is responsible
 * for assigning the appropriate state machine and OMGraphic to
 * itself.  Also, an EditableOMGraphic has a notion of a list of
 * GrabPoints, which can be used as handles to the OMGraphic to
 * provide controlled modifications.
 */
public abstract class EditableOMGraphic extends MapMouseAdapter {

    /**
     * The state machine that interprets the mouse events (and other
     * events) and modifies the OMGraphics accordingly.
     *
     * @see com.bbn.openmap.layer.util.stateMachine.StateMachine
     */
    protected EOMGStateMachine stateMachine;
    /**
     * This is here for the MapMouseListener interface.  This may not
     * be important, depending on what is funneling mouse events to
     * the graphic.
     */
    protected String[] mouseModeServiceList;
    /**
     * The array of GrabPoints.
     */
    protected GrabPoint[] gPoints;
    /**
     * The projection of the map.  This can be retrieved from the
     * mouse events, provided that the mouse events source is the
     * MapBean.
     */
    protected Projection projection;
    /**
     * This GrabPoint is one that has been grabbed by the mouse, and
     * is being moved.  
     */
    protected GrabPoint movingPoint = null;

    protected EOMGListenerSupport listeners = null;

    /**
     * Flag to indicate whether a GUI for this EOMG should be
     * presented to allow edits to it's attributes.
     */
    protected boolean showGUI = true;

    /**
     * Flag to let states know if the edges of the graphic can be
     * grabbed directly, for movement or manipulation, as opposed to
     * just allowing those actions through the grab points. 
     */
    protected boolean canGrabGraphic = true;

    /**
     * Action mask for this graphic.  Used as a holder for modifying
     * objects to let this EditableOMGraphic know what is being done
     * to it.
     */
    protected int actionMask = 0;

    /**
     * Set the StateMachine for this EditableOMGraphic.
     *
     * @param sm StateMachine.
     * @see com.bbn.openmap.layer.util.stateMachine.StateMachine
     */
    public void setStateMachine(EOMGStateMachine sm) {
	stateMachine = sm;
    }

    /**
     * Get the state machine for this EditableOMGraphic.
     */
    public EOMGStateMachine getStateMachine() {
	return stateMachine;
    }

    /**
     * Set the list of MouseMode names that this EditableOMGraphic
     * will respond to, if it is dealing directly with a
     * MouseDelegator.
     */
    public void setMouseModeServiceList(String[] list) {
	mouseModeServiceList = list;
    }

    /**
     * Get the list of MouseMode names that this EditableOMGraphic
     * will respond to, if it is dealing directly with a
     * MouseDelegator.
     */
    public String[] getMouseModeServiceList() {
	return mouseModeServiceList;
    }

    /**
     * Set whether this EOMG should provide a user interface to have
     * the attributes modified.
     * @param set true if the GUI should be shown.
     */
    public void setShowGUI(boolean set) {
	showGUI = set;
	OMGraphic graphic = getGraphic();
	if (graphic != null) {
	    graphic.setShowEditablePalette(set);
	}
    }

    public boolean getShowGUI() {
	if (getGraphic() != null) {
	    return getGraphic().getShowEditablePalette();
	} else {
	    return showGUI;
	}
    }

    /**
     * Set whether a graphic can be manipulated by its edges, rather
     * than just by its grab points.  Used internally.
     */
    public void setCanGrabGraphic(boolean set) {
	canGrabGraphic = set;
    }

    /**
     * Get whether a graphic can be manipulated by its edges, rather
     * than just by its grab points.
     */
    public boolean getCanGrabGraphic() {
	return canGrabGraphic;
    }

    /**
     * Set the OMGraphic that is being modified by the
     * EditableOMGraphic.  The type of OMGraphic needs to match what
     * the EditableOMGraphic is expecting.  Assume that if the graphic
     * passed in is null, that a proper graphic will be created.
     *
     * @param graphic OMGraphic.  
     */
    public abstract void setGraphic(OMGraphic graphic);

    /**
     * Create the OMGraphic that is to be modified by the
     * EditableOMGraphic.  
     *
     * @param ga GraphicAttributes, describing the graphic to be
     * created.
     */
    public abstract void createGraphic(GraphicAttributes ga);

    /**
     * Get the OMGraphic that is being created/modified by the
     * EditableOMGraphic.
     */
    public abstract OMGraphic getGraphic();

    /**
     * Remove all changes and put graphic as it was before
     * modifications.  If the graphic is being created, start over.
     */
    public void reset() {
	Debug.output("EditableOMGraphic.reset(): not yet supported");
    }

    /**
     * Set the grab point objects within the EditableOMGraphic array.
     * The size and layout of the points in the array are carefully
     * determined by the EditableOMGraphic, so this method merely
     * replaces objects within the array, not replacing the array
     * itself, so that you cannot reset the number of grab points an
     * EditableOMGraphic uses for a particular OMGraphic.
     * 
     * @param points a GrabPoint[]
     * @return true if the grab point array was exactly what the
     * EditableOMGraphic was expecting, in terms of length of the
     * GrabPoint array length.  The method copies the array values
     * that fit into the resident array.  
     */
    public boolean setGrabPoints(GrabPoint[] points) {
	if (points == null || gPoints == null) {
	    return false;
	}

	for (int i = 0; i < points.length && i < gPoints.length; i++) {
	    gPoints[i] = points[i];
	}

	return (points.length == gPoints.length);
    }

    /**
     * Method to allow objects to set OMAction masks on this editable
     * graphic.
     */
    public void setActionMask(int mask) {
	actionMask = mask;
    }

    /**
     * Get the OMAction mask for this graphic.
     */
    public int getActionMask() {
	return actionMask;
    }

    /**
     * Tells the EditableOMGraphic that the locations of the grab
     * points have been modified, and that the parameters of the
     * OMGraphic need to be modified accordingly.
     */
    public abstract void setGrabPoints();

    /**
     * Get the array of grab points used for the EditableOMGraphic.
     * Given a mouse event, you can see if one of these is affected,
     * and move it accordingly.  Call setGrabPoints() when
     * modifications are done, so that the OMGraphic is modified.
     */
    public GrabPoint[] getGrabPoints() {
	return gPoints;
    }

    /**
     * Set the GrabPoint at a particule index of the array. This can
     * be used to tie two different grab points together. 
     *
     * @param gb GrabPoint to assign within array.
     * @param index the index of the array to put the GrabPoint.  The
     * EditableOMGraphic should be able to provide the description of
     * the proper placement indexes.
     * @return If the grab point or array is null, or if the index is
     * outside the range of the array, false is returned.  If
     * everything goes OK, then true is returned.  
     */
    public boolean setGrabPoint(GrabPoint gb, int index) {
	if (gPoints != null && gb != null && 
	    index >= 0 && index < gPoints.length) {

	    gPoints[index] = gb;
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Return a particular GrabPoint at a particular point in the
     * array.  The EditableOMGraphic should describe which indexes
     * refer to which grab points in the EOMG GrabPoint array. If the
     * index is outside the range of the array, null is returned.
     */
    public GrabPoint getGrabPoint(int index) {
	if (gPoints != null && index >= 0 && index < gPoints.length) {
	    return gPoints[index];
	} else {
	    return null;
	}
    }

    /**
     * Set the GrabPoint that is in the middle of being modified, as a
     * result of a mouseDragged event, or other selection.
     */
    public void setMovingPoint(GrabPoint gp) {
	movingPoint = gp;
    }

    /**
     * Get the GrabPoint that is being moved.  If it's null, then
     * there isn't one.  
     */
    public GrabPoint getMovingPoint() {
	return movingPoint;
    }

    /**
     * Given a MouseEvent, find a GrabPoint that it is touching, and
     * set the moving point to that GrabPoint.  Called when a
     * MouseEvent happens, and you want to find out if a GrabPoint
     * should be used to make modifications to the graphic or its
     * position.
     *
     * @param e MouseEvent
     * @return GrabPoint that is touched by the MouseEvent, null if
     * none are.
     */
    public GrabPoint getMovingPoint(MouseEvent e) {

	movingPoint = null;

	GrabPoint[] gb = getGrabPoints();
	int x = e.getX();
	int y = e.getY();

	for (int i = gb.length - 1; i >=0; i--) {
	    if (gb[i] != null && gb[i].distance(x, y) == 0) {
		setMovingPoint(gb[i]);
		// in case the points are on top of each other, the
		// last point in the array will take precidence.
		return gb[i];
	    }
	}

	setMovingPoint(null);
	return null;
    }

    /**
     * Called to set the OffsetGrabPoint to the current mouse
     * location, and update the OffsetGrabPoint with all the other
     * GrabPoint locations, so everything can shift smoothly.  Should
     * also set the OffsetGrabPoint to the movingPoint.
     */
    public abstract void move(MouseEvent e);

    /**
     * Clean the surface all the painting is taking place over.
     */
    public void cleanMap(MouseEvent e) {
	Object obj = e.getSource();
	if (!(obj instanceof com.bbn.openmap.MapBean)) {
	    return;
	}

	// Could call repaint(), but I think we should paint in this
	// thread...
	com.bbn.openmap.MapBean map = (com.bbn.openmap.MapBean)obj;
	// Gets the buffer cleaned out.
	map.setBufferDirty(true);
  	map.paintChildren(map.getGraphics());
    }

    /** Same as redraw(e, false) */
    public void redraw(MouseEvent e) {
	redraw(e, false);
    }

    public void redraw(MouseEvent e, boolean firmPaint) {
	redraw(e, firmPaint, true);
    }

    /**
     * Given a MouseEvent, check the source, and if it's a MapBean,
     * then grab the projection and java.awt.Graphics from it to use
     * for generation and rendering of the EditableOMGraphic objects.
     *
     * @param e MouseEvent
     * @param firmPaint true if the graphic is being rendered at rest,
     * with fill colors and true colors, with the grab point if the
     * state allows it.  If false, then the fill color will not be
     * used, and just the graphic will be drawn.  Use false for graphics
     * that are moving.  
     */
    public void redraw(MouseEvent e, boolean firmPaint, boolean drawXOR) {
	if (Debug.debugging("eomg")) {
	    Debug.output("EditableOMGraphic.redraw(" + 
			 (firmPaint?"firmPaint)":")"));
	}

	Object obj = e.getSource();
	if (!(obj instanceof com.bbn.openmap.MapBean)) {
	    return;
	}
	    
	com.bbn.openmap.MapBean map = (com.bbn.openmap.MapBean)obj;
	Graphics g = map.getGraphics();

	OMGraphic graphic = getGraphic();
	Paint holdFillPaint = graphic.getFillPaint();
	Paint holdLinePaint = graphic.getLinePaint();

	if (firmPaint) {
	    // So, with a firm paint, we want to clean the screen.  If
	    // the map is being buffered, we need to clean out the
	    // buffer, which is why we set the Request paint to true,
	    // to get the image rebuilt.  Otherwise, a copy of the
	    // graphic remains.
	    map.setBufferDirty(true);
//  	    map.paintChildren(map.getGraphics());
	    graphic.generate(getProjection());
	    map.repaint();
	} else {
	    // If we get here, we are painting a moving object, so we
	    // only want to do the outline to make it as fast as
	    // possible.
	    graphic.setFillPaint(OMColor.clear);
	    graphic.setLinePaint(Color.black);
	    graphic.regenerate(getProjection());

	    if (drawXOR) {
		g.setXORMode(Color.lightGray);
		Paint paint = graphic.getDisplayPaint();

		if (paint instanceof Color) {
		    g.setColor((Color)paint);
		}

		render(g);
	    }

	    GrabPoint gp = getMovingPoint();
	    if (gp != null) {
		gp.set(e.getX(), e.getY());
		if (gp instanceof OffsetGrabPoint) {
		    ((OffsetGrabPoint)gp).moveOffsets();
		}
		setGrabPoints();
	    }
	}

	if (!firmPaint) {
	    generate(getProjection());
	    render(g);

	    graphic.setFillPaint(holdFillPaint);
	    graphic.setLinePaint(holdLinePaint);
	}
	g.dispose();

	lastMouseEvent = e;
    }

    private MouseEvent lastMouseEvent;

    public void repaint() {
	if (lastMouseEvent != null) {
	    redraw(lastMouseEvent, true);
	}
    }

    public void finalize() {
	if (getGraphic() != null) {
	    getGraphic().setVisible(true);
	}
	if (Debug.debugging("gc")) {
	    Debug.output("EditableOMGraphic gone.");
	}
    }

    /**
     * Use the current projection to place the graphics on the screen.
     * Has to be called to at least assure the graphics that they are
     * ready for rendering.  Called when the graphic position changes.
     *
     * @param proj com.bbn.openmap.proj.Projection
     * @return true 
     */
    public abstract boolean generate(Projection proj);

    /**
     * Given a new projection, the grab points may need to be
     * repositioned off the current position of the graphic. Called
     * when the projection changes.  IMPORTANT! Set the GrabPoints for
     * the graphic here.
     */
    public abstract void regenerate(Projection proj);

    public void repaintRender(Graphics g) {
// 	if (getMovingPoint() != null) {
// 	    return;
// 	}
	render(g);
    }

    /**
     */
    public abstract void render(Graphics g);

    /**
     * Set the current projection.
     */
    public void setProjection(Projection proj) {
	projection = proj;
	// This is important.  In the EditableOMGraphics, the
	// GrabPoints are set when regenerate is called.
	regenerate(proj);
    }

    /**
     * Get the current projection.
     */
    public Projection getProjection() {
	return projection;
    }

    // Mouse Listener events
    ////////////////////////

    /**
     */
    public boolean mousePressed(MouseEvent e) {
	Debug.message("eomgdetail", "EditableOMGraphic.mousePressed()");
	if (!mouseOnMap) return false;
	return stateMachine.getState().mousePressed(e);
    }
 
    /**
     */
    public boolean mouseReleased(MouseEvent e) {
	Debug.message("eomgdetail", "EditableOMGraphic.mouseReleased()");
	if (!mouseOnMap) return false;
	return stateMachine.getState().mouseReleased(e);
    }

    /**
     */
    public boolean mouseClicked(MouseEvent e) {
	Debug.message("eomgdetail", "EditableOMGraphic.mouseClicked()");
	if (!mouseOnMap) return false;
	return stateMachine.getState().mouseClicked(e);
    }
 
    boolean mouseOnMap = true;

    /**
     */
    public void mouseEntered(MouseEvent e) {
	Debug.message("eomgdetail", "EditableOMGraphic.mouseEntered()");
	mouseOnMap = true;
	stateMachine.getState().mouseEntered(e);
    }
 
    /**
     */
    public void mouseExited(MouseEvent e) {
	Debug.message("eomgdetail", "EditableOMGraphic.mouseExited()");
	mouseOnMap = false;
	stateMachine.getState().mouseExited(e);
    }

    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     */
    public boolean mouseDragged(MouseEvent e) {
	Debug.message("eomgdetail", "EditableOMGraphic.mouseDragged()");
	if (!mouseOnMap) return false;
	return stateMachine.getState().mouseDragged(e);
    }

    /**
     */
    public boolean mouseMoved(MouseEvent e) {
	Debug.message("eomgdetail", "EditableOMGraphic.mouseMoved()");
	if (!mouseOnMap) return false;
	return stateMachine.getState().mouseMoved(e);
    }

    /**
     */
    public void mouseMoved() {
	Debug.message("eomgdetail", "EditableOMGraphic.mouseMoved()");
	if (!mouseOnMap) return;
	stateMachine.getState().mouseMoved();
    }

    /**
     * Add a EOMGListener.
     * @param l EOMGListener
     */
    public synchronized void addEOMGListener(EOMGListener l) {
	if (listeners == null) {
	    listeners = new EOMGListenerSupport(this);
	}
	listeners.addEOMGListener(l);
    }

    /**
     * Remove a EOMGListener.
     * @param l EOMGListener
     */
    public synchronized void removeEOMGListener(EOMGListener l) {
	if (listeners == null) {
	    return;
	}
	listeners.removeEOMGListener(l);

	// Should we get rid of the support if there are no listeners?
	// The support will get created when a listener is added.
	if (listeners.getListeners().size() == 0) {
	    listeners = null;
	}
    }

    /**
     * The method to call if you want to let listeners know that the
     * state has changed.  Usually called when a graphic is selected
     * or not, so that GUIs can be directed.
     */
    public void fireEvent(EOMGEvent event) {
	if (listeners != null) {
  	    listeners.fireEvent(event);
	}
    }

    /**
     * Create the event with a Cursor and/or message, and then fire
     * it.  
     *
     * @param cursor Cursor to be used.
     * @param message an instruction/error to be displayed to the user.
     */
    public void fireEvent(Cursor cursor, String message) {
	fireEvent(cursor, message, null);
    }

    /**
     * Create the event with the Cursor, message and/or MouseEvent.
     * @param cursor Cursor to be used.
     * @param message an instruction/error to be displayed to the user.
     * @param mouseEvent where that caused the EOMGEvent.  May be null.
     */
    public void fireEvent(Cursor cursor, String message, MouseEvent mouseEvent) {
	if (listeners != null) {
	    EditableOMGraphic theSource = listeners.getEOMG();
	    EOMGEvent event = new EOMGEvent(theSource, cursor, message, mouseEvent);
	    fireEvent(event);
	}
    }

    /**
     * Create the event with no cursor change or message to be
     * displayed.
     */
    public void fireEvent() {
	fireEvent(null, null);
    }

    /**
     * If this EditableOMGraphic has parameters that can be
     * manipulated that are independent of other EditableOMGraphic
     * types, then you can provide the widgets to control those
     * parameters here.  By default, this method returns null, which
     * indicates that you can extend this method to return a Component
     * that controls parameters for the EditableOMGraphic other than
     * the GraphicAttribute parameters.
     * @return Component to control EOMG parameters, without
     * the GraphicAttribute GUI.
     */
    public Component getGUI() {
	return getGUI(null);
    } 

    /**
     * If this EditableOMGraphic has parameters that can be
     * manipulated that are independent of other EditableOMGraphic
     * types, then you can provide the widgets to control those
     * parameters here.  By default, returns the GraphicAttributes GUI
     * widgets.  If you don't want a GUI to appear when a widget is
     * being created/edited, then don't call this method from the
     * EditableOMGraphic implementation, and return a null Component
     * from getGUI.
     * @param graphicAttributes the GraphicAttributes to use to get
     * the GUI widget from to control those parameters for this EOMG.
     * @return Component to use to control parameters for this EOMG.
     */
    public Component getGUI(GraphicAttributes graphicAttributes) {
	if (showGUI && graphicAttributes != null) {
	    javax.swing.Box attributeBox = 
		javax.swing.Box.createHorizontalBox();
	    attributeBox.add(graphicAttributes.getGUI());
	    return attributeBox;
	}
	return null;
    }

    /**
     * Return a small GUI, fit for a toolbar, for modifying the
     * type-specific attributes of an OMGraphic.  Can return a button
     * to bring up a larger interface in it's own window.
     */
    public Component getToolBarGUI() {
	return null;
    }
}

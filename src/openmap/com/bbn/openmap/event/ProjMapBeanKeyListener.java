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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ProjMapBeanKeyListener.java,v $
// $RCSfile: ProjMapBeanKeyListener.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.ProjectionStack;
import com.bbn.openmap.proj.ProjectionStackTrigger;
import com.bbn.openmap.util.Debug;

/**
 * The ProjMapBeanKeyListener is a KeyListener that gets events when
 * the MapBean has focus, and responds to certain keys by changing the
 * projection.  The arrow keys pan the map, and 'z' zooms in.  Shift-z
 * zooms out. The less than/comma key tells a projection stack to go
 * back to the last projection, and the greater than/period tells it
 * to go to the next projection.  The MapBean has to have focus for
 * these to work which is usually gained by clicking on the map.
 */
public class ProjMapBeanKeyListener extends MapBeanKeyListener 
    implements ProjectionStackTrigger {

    /**
     * Default Zoom In Factor is 2, meaning that the scale number will
     * be cut in half to zoom in and doubled to zoom out.
     */
    protected transient float zoomFactor = 2f;

    protected PanSupport panners;
    protected ZoomSupport zoomers;
    protected ListenerSupport projListeners;

    public ProjMapBeanKeyListener() {
        panners = new PanSupport(this);
        zoomers = new ZoomSupport(this);
        projListeners = new ListenerSupport(this);
    }

    public void keyReleased(KeyEvent e) {

        int keyCode = e.getKeyCode();

        // When we can control rates, we'll use shift for double pan,
        // and ctrl for half pan
//      int modifiers = e.getModifiers();

        switch (keyCode) {
        case KeyEvent.VK_UP:
        case KeyEvent.VK_KP_UP:
            panners.firePan(PanEvent.NORTH);
            break;
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_KP_DOWN:
            panners.firePan(PanEvent.SOUTH);
            break;
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_KP_LEFT:
            panners.firePan(PanEvent.WEST);
            break;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_KP_RIGHT:
            panners.firePan(PanEvent.EAST);
            break;
        case KeyEvent.VK_Z:
            if (e.isShiftDown()) {
                zoomers.fireZoom(ZoomEvent.RELATIVE, zoomFactor);
            } else {
                zoomers.fireZoom(ZoomEvent.RELATIVE, 1f/zoomFactor);
            }

            break;
        case KeyEvent.VK_PLUS:
            zoomers.fireZoom(ZoomEvent.RELATIVE, 1f/zoomFactor);
            break;
        case KeyEvent.VK_MINUS:
            zoomers.fireZoom(ZoomEvent.RELATIVE, zoomFactor);
            break;
        case KeyEvent.VK_COMMA:
            fireProjectionStackEvent(ProjectionStack.BackProjCmd);
            break;
        case KeyEvent.VK_PERIOD:
            fireProjectionStackEvent(ProjectionStack.ForwardProjCmd);
            break;
        }
    }

    /**
     * In addition to the super.setMapBean() method, also sets the
     * MapBean as a zoom and pan listener.
     */
    public void setMapBean(MapBean map) {
        if (mapBean != null) {
            panners.removePanListener(map);
            zoomers.removeZoomListener(map);
        }

        super.setMapBean(map);

        if (mapBean != null) {
            panners.addPanListener(map);
            zoomers.addZoomListener(map);
        }
    }

    /**
     * Called by keyReleased when the period/comma keys are pressed.
     */
    protected void fireProjectionStackEvent(String command) {
        if (projListeners.size() == 0) {
            return;
        }

        ActionEvent event = new ActionEvent(this, 0, command);
        Iterator it = projListeners.iterator();
        while (it.hasNext()) {
            ((ActionListener)it.next()).actionPerformed(event);
        }

    }

    /**
     * Add an ActionListener for events that trigger events to shift the
     * Projection stack.
     */
    public void addActionListener(ActionListener al) {
        projListeners.addListener(al);
    }

    /**
     * Remove an ActionListener that receives events that trigger
     * events to shift the Projection stack.
     */
    public void removeActionListener(ActionListener al) {
        projListeners.removeListener(al);
    }

    /**
     * To receive a status to let the trigger know if any projections
     * in the forward or backward stacks exist, possibly to disable
     * any gui widgets.  Does nothing, we don't care here.
     *
     * @param containsBackProjections there is at least one past
     * projection in the back cache.  
     * @param containsForwardProjections there is at least one future
     * projection in the forward cache.  Used when a past projection
     * is being used. 
     */
    public void updateProjectionStackStatus(boolean containsBackProjections,
                                            boolean containsForwardProjections) {}

    /**
     * In addition to the MapBean, find a projection stack so the less
     * than/greater than works on that.
     */
    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof ProjectionStack) {
            addActionListener((ActionListener)someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        if (someObj instanceof ProjectionStack) {
            removeActionListener((ActionListener)someObj);
        }
    }

}

// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/StandardRenderPolicy.java,v $
// $RCSfile: StandardRenderPolicy.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.policy;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import java.awt.Graphics;

/**
 * The StandardRenderPolicy is a RenderPolicy that simply paints the
 * current graphic list. No conditions or deviations are considered.
 */
public class StandardRenderPolicy extends OMComponent implements RenderPolicy {

    /**
     * Don't let this be null, nothing will happen. At all.
     */
    protected OMGraphicHandlerLayer layer;

    protected boolean DEBUG = false;

    public StandardRenderPolicy() {
        DEBUG = Debug.debugging("layer") || Debug.debugging("policy");
    }

    /**
     * Don't pass in a null layer.
     */
    public StandardRenderPolicy(OMGraphicHandlerLayer layer) {
        this();
        setLayer(layer);
    }

    public void setLayer(OMGraphicHandlerLayer l) {
        layer = l;
    }

    public OMGraphicHandlerLayer getLayer() {
        return layer;
    }

    public OMGraphicList prepare() {
        if (layer != null) {
            return layer.prepare();
        } else {
            return null;
        }
    }

    public void paint(Graphics g) {
        if (layer != null) {
            OMGraphicList list = layer.getList();
            if (list != null) {
                Projection proj = layer.getProjection();
                if (proj != null) {
                    g.setClip(0, 0, proj.getWidth(), proj.getHeight());
                }

                list.render(g);
            } else if (DEBUG) {
                Debug.output(layer.getName()
                        + ".paint(): NULL list, skipping...");
            }
        } else {
            Debug.error("RenderPolicy.paint():  NULL layer, skipping...");
        }
    }
}
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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ProjectionEvent.java,v $
// $RCSfile: ProjectionEvent.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import com.bbn.openmap.proj.Projection;

/**
 * An event with an updated MapBean projection.
 */
public class ProjectionEvent extends java.util.EventObject {

    protected Projection projection;

    /**
     * Construct a ProjectionEvent.
     * @param source Object
     * @param aProj Projection
     */
    public ProjectionEvent(Object source, Projection aProj) {
        super(source);
        projection = aProj;
    }

    /**
     * Get the Projection.
     * @return Projection
     */
    public Projection getProjection() {
        return projection;
    }
}

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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMRectLoader.java,v $
// $RCSfile: OMRectLoader.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:15 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.*;

/**
 * Loader that knows how to create/edit OMRect objects.
 */
public class OMRectLoader extends AbstractToolLoader 
    implements EditToolLoader {

    protected String graphicClassName = "com.bbn.openmap.omGraphics.OMRect";

    public OMRectLoader() {
        init();
    }

    public void init() {
        EditClassWrapper ecw = 
            new EditClassWrapper(graphicClassName,
                                 "com.bbn.openmap.omGraphics.EditableOMRect",
                                 "editablerect.gif",
                                 "Rectangle");
        addEditClassWrapper(ecw);
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic.  The GraphicAttributes
     * object lets you set some of the initial parameters of the rect,
     * like rect type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(String classname, 
                                                GraphicAttributes ga) {
        if (classname.intern() == graphicClassName) {
            return new EditableOMRect(ga);
        }
        return null;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
        if (graphic instanceof OMRect) {
            return new EditableOMRect((OMRect)graphic);
        }
        return null;
    }
}

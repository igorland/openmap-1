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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/CircleStateMachine.java,v $
// $RCSfile: CircleStateMachine.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:13 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.editable;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.layer.util.stateMachine.*;
import com.bbn.openmap.util.Debug;

public class CircleStateMachine extends EOMGStateMachine {

    public CircleStateMachine(EditableOMCircle circle){
        super(circle);
    }


    protected State[] init(){
        State[] states = super.init();
        Debug.message("eomc", "CircleStateMachine.init()");

        //  These are the only two states that need something special
        //  to happen.
        states[GRAPHIC_UNDEFINED] = new CircleUndefinedState((EditableOMCircle)graphic);
        states[GRAPHIC_SELECTED] = new CircleSelectedState((EditableOMCircle)graphic);
        states[GRAPHIC_SETOFFSET] = new CircleSetOffsetState((EditableOMCircle)graphic);
        return states;
    }
}

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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeMOOTWCategory.java,v $
// $RCSfile: CodeMOOTWCategory.java,v $
// $Revision: 1.5 $
// $Date: 2003/12/18 19:11:11 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

/**
 * A CodeMOOTWCategory is similar to the Tactical Graphics
 * CodeCategory, but it applies to the MOOTW symbol set instead.  This
 * CodePosition notes the third character in MOOTW symbol codes, and
 * represents whether a MOOTW event concerns violent activities,
 * locations, operations, or items.
 */
public class CodeMOOTWCategory extends CodePosition {

    public CodeMOOTWCategory() {
	super("MOOTW Categories", 3, 3);
    }

}

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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/shape/ESRISpecialistPolygonRecord.java,v $
// $RCSfile: ESRISpecialistPolygonRecord.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:04 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.specialist.shape;

import java.io.IOException;
import java.util.Vector;

import com.bbn.openmap.CSpecialist.*;
import com.bbn.openmap.CSpecialist.GraphicPackage.*;
import com.bbn.openmap.CSpecialist.PolyPackage.*;
import com.bbn.openmap.layer.shape.*;
import com.bbn.openmap.layer.specialist.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.ProjMath;

/**
 */
public class ESRISpecialistPolygonRecord extends ESRIPolygonRecord 
    implements ESRISpecialistRecord {

    public ESRISpecialistPolygonRecord() {
        super();
    }

    public ESRISpecialistPolygonRecord(byte b[], int off)
        throws IOException{
        super(b, off);
    }

    /**
     * Generates SGraphics and adds them to the given list.  <p> The
     * poly contains the coordinates in radians in a float array.  The
     * specialist needs them sent back as an array of LLPoints, in
     * decimal degrees..
     *
     * @param list the Vector to write the graphic into.
     * @param lineColor the line color to use.
     * @param fillColor the fill color to use.
     */
    public void writeGraphics (Vector list, SColor lineColor, SColor fillColor) 
        throws IOException {

        int nPolys = polygons.length;
        if (nPolys <= 0) return;
        SPoly sp=null;
        float[] pts;
        LLPoint[] ll;
        boolean ispolyg = isPolygon();

        for (int i=0; i<nPolys; i++) {
            // these points are already in RADIAN lat,lon order!...
            pts = ((ESRIPoly.ESRIFloatPoly)polygons[i]).getDecimalDegrees();
            ll = new LLPoint[pts.length/2];

            for (int j = 0; j < ll.length; j++){
                ll[j] = new LLPoint(pts[j*2], pts[(j*2)+1]);
            }

            sp = new SPoly(ll, LineType.LT_GreatCircle); 
            sp.color(lineColor);
            if (fillColor != null) {
                sp.fillColor(fillColor);
            }
            list.addElement(sp);
        }
    }
}

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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/SieveListener.java,v $
// $RCSfile: SieveListener.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:11 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.http;

import java.io.Writer;
import java.io.IOException;


/**
 * An HttpRequestListener that returns the request to the client.
 *
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public class SieveListener implements HttpRequestListener {
    public SieveListener () {}

    /**
     * Just write the request out to the client.
     */
    public void httpRequest (HttpRequestEvent e) throws IOException {
        e.getWriter().write(e.getRequest());
    }
}

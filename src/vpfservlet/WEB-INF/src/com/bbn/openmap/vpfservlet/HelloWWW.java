// **********************************************************************
// <copyright>
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/HelloWWW.java,v $
// $Revision: 1.3 $ $Date: 2004/10/14 18:06:33 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Test class, not used
 */
public class HelloWWW extends HttpServlet {
    public void doGet(HttpServletRequest req,
                      HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n";
        out.println(docType + "<HTML>\n<HEAD><TITLE>Hello WWW</TITLE></HEAD>\n<BODY>\n<H1>Hello WWW</H1>\n</BODY></HTML>\n");
    }
}

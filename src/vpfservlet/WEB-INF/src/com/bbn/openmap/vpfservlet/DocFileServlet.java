// **********************************************************************
// <copyright>
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/DocFileServlet.java,v $
// $Revision: 1.2 $ $Date: 2004/01/26 18:18:16 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.bbn.openmap.layer.util.html.*;
import com.bbn.openmap.layer.vpf.*;
import com.bbn.openmap.io.FormatException;

/**
 * This class handles displaying VPF .doc files
 */
public class DocFileServlet extends VPFHttpServlet {
    /** the columns we need from a VPF doc file */
    static final String FieldColumns[] = {"text"};
    /** the fields in a VPF doc file we need */
    static final char[] FieldTypeSchema = {'T'};
    /** the field lengths in a VPF doc file we need */
    static final int[] FieldLengthSchema = {-1};

    /**
     * A do-nothing constructor - init does all the work.
     */
    public DocFileServlet() {
        super();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String filePath = (String)request.getAttribute(DispatchServlet.ROOTPATH_FILENAME);
        if (filePath == null) {
            String pathInfo = setPathInfo(request);
            filePath = contextInfo.resolvePath(pathInfo);
            if (!pathOkay(filePath, pathInfo, response)) {
                return;
            }
        }

        DcwRecordFile docfile = null;
        try {
            docfile = new DcwRecordFile(filePath);
        } catch (FormatException fe) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                               " docfile not found");
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(HTML_DOCTYPE);
        out.println(getStylesheetHTML(request));

        String tableName = docfile.getTableName();
        out.println("<HTML>");
        String title = "VPF Documentation File " + tableName;
        out.println("<HEAD><TITLE>" + title + "</TITLE></HEAD>");
        out.println("<BODY><H1>" + title + "</H1>");
        
        try {
            docfile.lookupSchema(FieldColumns, true,
                                 FieldTypeSchema, FieldLengthSchema, false);
        } catch (FormatException fe) {
            out.println("The documentation file appears to be invalid.");
            RequestDispatcher rd = request.getRequestDispatcher("/Schema");
            rd.include(request, response);
            out.println("</BODY></HTML>");
            docfile.close();
            return;
        }
        
        ArrayList al = new ArrayList(FieldTypeSchema.length);
        out.println("<pre>");
        try {
            while (docfile.parseRow(al)) {
                out.println("   " + al.get(1).toString());
            }
            out.println("</pre>");
        } catch (FormatException fe) {
            out.println("/pre>");
            out.println("File Format Exception processing data: " + fe);
        }
        out.println("</BODY></HTML>");
        docfile.close();
    }
}

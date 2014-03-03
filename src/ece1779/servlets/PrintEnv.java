package ece1779.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class PrintEnv extends HttpServlet {
	
	
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Env Variables</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Values of  Environment Variables</h1>");
        out.println("SERVERNAME=" + request.getServerName() + "<br/>");
        out.println("SERVERPROTOCOL=" + request.getProtocol() + "<br/>");
        out.println("REQUEST_METHOD=" + request.getMethod() + "<br/>");
        out.println("REMOTE_USER=" + request.getRemoteHost() + "<br/>");
        
        out.println("</body>");
        out.println("</html>");
    }
    
    

    
}

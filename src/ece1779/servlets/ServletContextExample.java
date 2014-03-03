package ece1779.servlets;

import java.io.*;
import java.lang.Integer;
import javax.servlet.*;
import javax.servlet.http.*;
  
public class ServletContextExample extends HttpServlet {
    public void init() {
    	this.getServletContext().setAttribute("counter", new Integer(0));
    }
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Counter</title>");
        out.println("</head>");
        out.println("<body>");
        
        int totalcalls = ((Integer)this.getServletContext().getAttribute("counter")).intValue() + 1;
         
        this.getServletContext().setAttribute("counter", new Integer(totalcalls));
        
        int usercounter = 1;
          
        if(request.getSession().getAttribute("usercounter") != null)
        	usercounter = ((Integer)request.getSession().getAttribute("usercounter")).intValue() + 1;
        
        request.getSession().setAttribute("usercounter", new Integer(usercounter));
        
        String sID = request.getSession().getId();
        
        out.println("Servlet has been called " + totalcalls + " times. <br />");
        
        out.println("User " + sID +  " has called servlet " + usercounter + " time.");
        
        request.getSession().getAttribute("counter");
        
        out.println("</body>");
        out.println("</html>");
    }
}

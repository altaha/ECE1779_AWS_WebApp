package ece1779.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SessionExample extends HttpServlet {
    public void doGet(HttpServletRequest request,
	              HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
	
        HttpSession session = request.getSession();
	String  loggedIn = (String)session.getAttribute("loggedIn");
	Integer numTimes = (Integer)session.getAttribute("numTimes");

        String login = null;
        
        int nTimes = 1;

	if (numTimes != null)
	    nTimes = numTimes.intValue() + 1;

        if (loggedIn == null) {
            login = request.getParameter("login");
            String password = request.getParameter("password");

            if (login != null && login.compareTo("hello") == 0 && 
                password != null && password.compareTo("world") == 0) {
                loggedIn = "true";
            }
        }
        
	out.println("<head><title>Simple Session Example</title></head>");
	out.println("<body>");
	out.println("<h1>A Simple Session Example</h1>");

	if (loggedIn != null) {
            session.setAttribute("loggedIn", loggedIn);
            session.setAttribute("numTimes", new Integer(nTimes));

            out.println("You have visited this page " + nTimes + " times.");
	    out.println("<li><a href='SessionLogout'>Logout</a></li>");    
	}
	else {
	    if (login != null)
		out.println("Login failed!  Try again. <br>");
	    out.println("<form action='SessionExample'> ");
	    out.println("Login <input type='text' name='login' />");
	    out.println("Password <input type='text' name='password' />");
	    out.println("<input type='submit' />");
	    out.println("</form>");
	}
	out.println("<li><a href='");
	out.print(response.encodeURL("SessionExample"));
	out.print("'>Count</a></li>");
        out.println("</body>");
        out.println("</html>");
    }
}

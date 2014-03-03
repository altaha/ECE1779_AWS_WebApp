package ece1779.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CookieExample extends HttpServlet {
    public void doGet(HttpServletRequest request,
	              HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
	boolean loggedIn = false;
	int numTimes = 0;

	Cookie [] pageCookies = request.getCookies();

	for (int x=0; pageCookies != null && x < pageCookies.length; x++) {
	    if (pageCookies[x].getName().compareTo("loggedIn")==0) 
		loggedIn = true;
	    if (pageCookies[x].getName().compareTo("numTimes")==0) 
		numTimes = (new Integer(pageCookies[x].getValue())).intValue()+1;
	}

	String login = request.getParameter("login");
	String password = request.getParameter("password");

	if (login != null && login.compareTo("hello") == 0 && 
	    password != null && password.compareTo("world") == 0) {
	    loggedIn = true;
	}
	
	if (loggedIn) {
	    response.addCookie(new Cookie("loggedIn","true"));
	    response.addCookie(new Cookie("numTimes",Integer.toString(numTimes)));
	}
	

	out.println("<head><title>Simple Cookie Example</title></head>");
	out.println("<body>");
	out.println("<h1>A Simple Cookie Example</h1>");


	if (loggedIn) {
	    out.println("You have visited this page " + numTimes + " times.");
	    out.println("<li><a href='CookieLogout'>Logout</a></li>");    
	}
	else {
	    if (login != null)
	    	out.println("Login failed!  Please try again. <br>");
	    out.println("<form action='CookieExample'> ");
	    out.println("Login <input type='text' name='login' />");
	    out.println("Password <input type='text' name='password' />");
	    out.println("<input type='submit' />");
	    out.println("</form>");
	}
	out.println("<li><a href='CookieExample'>Count</a></li>");
        out.println("</body>");
        out.println("</html>");
    }
}

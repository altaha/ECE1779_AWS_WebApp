package ece1779.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CookieLogout extends HttpServlet {
    public void doGet(HttpServletRequest request,
	              HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

	Cookie myCookie = new Cookie("loggedIn","true");
        myCookie.setMaxAge(0);   
        response.addCookie(myCookie);
        
	out.println("<head><title>Simple Cookie Example</title></head>");
	out.println("<body>");
	out.println("<h1>A Simple Cookie Example</h1>");
	out.println("Thanks for visiting, please come again.");
	out.println("<a href='CookieExample'>Count</a>");
        out.println("</body>");
        out.println("</html>");
    }

}

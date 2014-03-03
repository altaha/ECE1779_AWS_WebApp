package ece1779.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SessionLogout extends HttpServlet {
    public void doGet(HttpServletRequest request,
	              HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

	HttpSession session = request.getSession();
	session.invalidate();

        
	out.println("<head><title>Simple Cookie Example</title></head>");
	out.println("<body>");
	out.println("<h1>A Simple Cookie Example</h1>");
	out.println("Thanks for visiting, please come again.");
	out.println("<a href='SessionExample'>Count</a>");
        out.println("</body>");
        out.println("</html>");
    }

}

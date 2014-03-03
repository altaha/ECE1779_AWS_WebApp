package ece1779.ec2;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class UserLogout extends HttpServlet {
    public void doGet(HttpServletRequest request,
	              HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        Cookie myCookie = new Cookie("UserLoggedIn","false");
        myCookie.setMaxAge(0);   
        response.addCookie(myCookie);
        
        out.println("<head><title>User Login</title></head>");
		out.println("<body>");
		out.println("<h1>User Login</h1>");
		out.println("Thanks for visiting, please come again.");
		out.println("<a href='UserLogin'>Login Page</a>");
        out.println("</body>");
        out.println("</html>");
    }

}

package ece1779.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Form extends HttpServlet {
    public void doGet(HttpServletRequest request,
	              HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
	out.println("<head><title>A Simple Example</title></head>");
	out.println("<body>");
	out.println("<h1>A Simple Example</h1>");
	out.println("<form method='post' action='Form'>");
	out.println("What's your name? <input type='text' name='name'  /><p />");
	out.println("What's the combination?<p />");
	out.println("<input type='checkbox' name='words' value='eenie' checked />eenie");
	out.println("<input type='checkbox' name='words' value='meenie' />meenie");
	out.println("<input type='checkbox' name='words' value='minie' checked />minie");
	out.println("<input type='checkbox' name='words' value='moe' />moe<p />");

	out.println("What's your favorite color? ");
	out.println("<select name='color'>");
	out.println("<option  value='red'>red</option>");
	out.println("<option  value='green'>green</option>");
	out.println("<option  value='blue'>blue</option>");
	out.println("<option  value='chartreuse'>chartreuse</option>");
	out.println("</select>");
	out.println("<p />");
	out.println("<input type='submit' name='.submit' />");
	out.println("</form>");
	out.println("<hr />");
	
	if (request.getParameterNames().hasMoreElements()) {
	    out.println("Your name is <em>"+request.getParameter("name")+"</em><p>");
	    out.println("The keywords are: <em> ");
	    String [] words=request.getParameterValues("words");

	    for (int i=0; words != null && i<words.length; i++)
		out.println(words[i] + " ");

	    out.println("</em><p>Your favorite color is <em>");
	    out.println(request.getParameter("color")+"</em><hr>");
	}
        out.println("</body>");
        out.println("</html>");
    }
    // Do this because the servlet uses both post and get
    public void doPost(HttpServletRequest request,
	               HttpServletResponse response)
       	throws IOException, ServletException {
	doGet(request,response);
    }
}

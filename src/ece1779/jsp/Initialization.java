package ece1779.jsp;

import javax.servlet.http.*;
 
public class Initialization extends HttpServlet {
    public void init() {
    	Movieplex moviedata = new Movieplex();
    	this.getServletContext().setAttribute("moviedata", moviedata);
    }
}

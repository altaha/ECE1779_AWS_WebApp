package ece1779.ec2;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ManagerLogin extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		boolean loggedIn = false;

		String username = request.getParameter("userID");
		String password = request.getParameter("password");
		
		Cookie [] pageCookies = request.getCookies();

		for (int x=0; pageCookies != null && x < pageCookies.length; x++) {
		    if (pageCookies[x].getName().compareTo("ManagerLoggedIn")==0)
		    {
		    	//TODO : for added protection also store token but for now this is good
		    	loggedIn = true;
		    }
		}

		if (username != null && password != null) {
		    if (isUserValid(username, password))
		    	loggedIn = true;
		}
		
		if (loggedIn) {
			//TODO : for added protection also store token but for now this is good
			Cookie c = new Cookie("ManagerLoggedIn", "true");
			c.setMaxAge(60*60); //1 hour expiry
		    response.addCookie(c);
		}
		

		out.println("<html>");
		out.println("  <head><title>Manager Page</title></head>");
		out.println("  <body>");

		if (loggedIn) {
			// Get DB connection from pool
		    out.println("  <h1> Welcome to Manager Controls </h1>");
		    out.println("  <p> Workers <br />");
		    //TODO: list workers and their CPU utilization
		    out.println("  INSERT WORKER INFO HERE...<br />");
		    printInstancesHealth(out);
		    out.println("  </p>");
		    
		    out.println("  <p>");
		    out.println("  <form id='pool_frm' name=pool_size action='/ece1779/servlet/ManagerLogin' method='post'>");
		    out.println("  Pool size :");
			out.println("  <button type='submit' name='poolSize' value='1'>Increase</button>");
			out.println("  <button type='submit' name='poolSize' value='0'>Decrease</button>");
			out.println("  </p>");
			
			out.println("  <p> Control Params: <br />");
			out.println("  <form id='control_frm' name='pool_manage' action='/ece1779/servlet/ManagerLogin' method='post'>");
			out.println("	   CPU Threshold Grow   <input type='text' name='CPUGrow' value='80'/><br />");
			out.println("	   CPU Threshold Shrink <input type='text' name='CPUShrink' value='20'/><br />");
			out.println("	   Ratio Pool Expand <input type='text' name='RatioExpand' value='1'/><br />");
			out.println("	   Ratio Pool Shrink <input type='text' name='RatioShrink' value='1'/><br />");
			out.println("      <input type='submit' value='Send'>");
			out.println("  </form>");
			out.println("  </p>");
			out.println("<li><a href='ManagerDeleteAll'>Delete all data</a></li>");
			out.println("  <br>");
			out.println("  <li><a href='ManagerLogout'>Logout</a></li>");
		}
		else {
			out.println("  <h1> Welcome to Manager Login </h1>");
		    if (username != null)
		    	out.println("Login failed!  Please try again. <br>");
			out.println("<form action='/ece1779/servlet/ManagerLogin' method='post'>");
			out.println("  Manager ID <input type='text' name='userID'/><br />");
			out.println("  Password <input type='password' name='password'/><br />");
			out.println("  <input type='submit' value='Login'>");
			out.println("</form>");
			out.println("<br>");
			out.println("<li><a href='/ece1779/ec2/index.html'>Home</a></li>");
		}
		
		out.println("  </body>");
		out.println("</html>");
	}
	
	public void printInstancesHealth(PrintWriter out)
	{
		out.println("<table style='width:60%'>");
		//TODO add print of running instances here...
	}
	
	public boolean isUserValid(String username, String password)
	{
		boolean userValid = false;
	    if (username.equals(getServletContext().getAttribute("adminUserID")) && password.equals(getServletContext().getAttribute("adminPassword")))
	    {
	    	userValid = true;
	    }
	    
	    return userValid;
	}

	// Do this because the servlet uses both post and get
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		doGet(request, response);
	}
}
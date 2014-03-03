package ece1779.ec2;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserLogin extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		boolean loggedIn = false;

		String username = request.getParameter("userID");
		String password = request.getParameter("password");
		
		Cookie [] pageCookies = request.getCookies();

		for (int x=0; pageCookies != null && x < pageCookies.length; x++) {
		    if (pageCookies[x].getName().compareTo("UserLoggedIn")==0)
		    {
		    	//TODO : for added protection also store token but for now this is good
		    	username = pageCookies[x].getValue();
		    	loggedIn = true;
		    }
		}

		if (username != null && password != null) {
		    if (isUserValid(username, password, out))
		    	loggedIn = true;
		}
		
		if (loggedIn) {
			//TODO : for added protection also store token but for now this is good
			Cookie c = new Cookie("UserLoggedIn",username);
			c.setMaxAge(60*60); //1 hour expiry
		    response.addCookie(c);
		}
		

		out.println("<html>");
		out.println("<head><title>User Page</title></head>");
		out.println("<body>");

		if (loggedIn) {
			out.println("<h1> Hey there "+username+"</h1>");
			out.println("<li><a href='UserFileUpload'>Upload Image File</a></li>");
			out.println("<li><a href='UserImages'>View Uploaded Images</a></li>");
		    out.println("<li><a href='UserLogout'>Logout</a></li>");    
		}
		else {
			out.println("  <h1> Welcome to User Login </h1>");
		    if (username != null)
		    {
		    	out.println("Login failed!  Please try again. <br>");
		    }
			out.println("<form action='/ece1779/servlet/UserLogin' method='post'>");
			out.println("  <p>Login or Create New Account</p>");
			out.println("  User ID <input type='text' name='userID'/><br />");
			out.println("  Password <input type='password' name='password'/><br />");
			out.println("  <input type='submit' value='Send'>");
			out.println("</form>");
			out.println("<br>");
			out.println("<li><a href='/ece1779/ec2/index.html'>Home</a></li>");
		}
		out.println("  </body>");
		out.println("</html>");
	}

	
	public boolean isUserValid(String username, String password, PrintWriter out)
			throws IOException, ServletException
	{
		boolean userValid = false;
		Connection con = null;
		
		try
		{
			// Get DB connection from pool
		    DataSource dbcp = (DataSource)getServletContext().getAttribute("dbpool");
		    con = dbcp.getConnection();
			
		    // Execute SQL query
	        String    sqlGetUserString = "select * from users where login = (?);";
	        PreparedStatement sqlGetUserStmt = con.prepareStatement(sqlGetUserString);
	        sqlGetUserStmt.setString(1, username);
	        ResultSet rs = sqlGetUserStmt.executeQuery();
	        
	        if (rs.next())
	        {
	        	int dbUserID = rs.getInt("id");
	        	String dbLoginID = rs.getString("login");
	        	String dbPassword = rs.getString("password");
	        	
	        	if (dbPassword.equals(password))
	        	{
	        		userValid = true;
	        	}
	        	//out.println("<p> User exists - dbUserID:"+dbUserID+" dbLoginID:"+dbLoginID+" dbPassword:"+dbPassword+"</p>");
	        }
	    	else
	    	{
	        	//out.println("<p> User doesnt exist</p>");
		        String    sqlSetUserString = "insert into users (login, password) values (?,?);";
		        PreparedStatement sqlSetUserStmt = con.prepareStatement(sqlSetUserString);
		        sqlSetUserStmt.setString(1, username);
		        sqlSetUserStmt.setString(2, password);
		        int rc = sqlSetUserStmt.executeUpdate();
		        if (rc == 1)
		        {
		        	userValid = true;
		        	//out.println("<p><i> User added successfully</i></p>");
		        }
	        }
	    }
		catch(Exception ex) {
			getServletContext().log(ex.getMessage());  
		}    	
		finally {
			try {
				con.close();
			} 
			catch (Exception e) {
	            getServletContext().log(e.getMessage());  
			}
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
package ece1779.ec2;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

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
		    out.println("  <h1> Welcome to Manager Controls </h1>");
		    out.println("  <p> Workers </p>");
		    printInstancesHealth(out); // Get instances and CPU utiliation
		    
		    out.println("  <p>");
		    out.println("  <form name=add_instance action='/ece1779/servlet/InstanceStart' method='get'>");
			out.println("  <button type='submit'>Add Worker</button>");
			out.println("  </form>");
			out.println("  </p>");
		    out.println("  <br>");

			out.println("  <p> Instance Scaling Control Parameters <br />");
			out.println("  <form id='control_frm' name='pool_manage' action='/ece1779/servlet/ManagerLogin' method='post'>");
			out.println("	   CPU Grow Threshold   <input type='text' name='CPUGrow' value='" + HealthMonitor.cpuHighThreshold + "'/><br />");
			out.println("	   CPU Shrink Threshold   <input type='text' name='CPUShrink' value='" + HealthMonitor.cpuLowThreshold + "'/><br />");
			out.println("	   Pool Grow Ratio     <input type='text' name='RatioGrow' value='" + HealthMonitor.growRatio + "'/><br />");
			out.println("	   Pool Shrink Ratio   <input type='text' name='RatioShrink' value='" + HealthMonitor.shrinkRatio + "'/><br />");
			out.println("	   Enable Load Scaling   <input type='checkbox' name='EnableScaling' value='enabled' ");
				if  (HealthMonitor.enableScaling == 1)
					out.println("checked='yes' /><br />");
				else
					out.println("/><br />");
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
			throws IOException {

		/* print table header */
    	out.println("<table style='width:60%' border='1'");
    	out.println("<tr bgcolor='silver'>");
    	out.println("<td> Instance ID </td>");
    	out.println("<td> CPU utilization </td>");
    	out.println("<td> Manage </td>");
    	out.println("</tr>");
    	
    	/* print instances and CPU utilization */
    	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)getServletContext().getAttribute("AWSCredentials");
    	
    	String adminImageId =  (String)getServletContext().getAttribute("adminImageId");
    	String workerImageId = (String)getServletContext().getAttribute("workerImageId");
    	
    	List<String> imageIDs = new ArrayList<String>();
    	imageIDs.add(adminImageId);
    	imageIDs.add(workerImageId);
    	
    	double averageCpuLoad = 0;
    	
    	try {
	    	for (String imageId : imageIDs) {
	    		List<String> instanceIds = HelperMethods.getRunningInstances(awsCredentials, imageId);
	    		
	    		for (String instanceId : instanceIds) {
	    			double cpuLoad = HelperMethods.getCPULoad(awsCredentials, instanceId);
	    			
	            	out.println("<tr bgcolor='white'>");
	            	out.println("<td> " + instanceId + " </td>");
	            	out.println("<td> " + cpuLoad + " </td>");
	            	/* the manager instance cannot be stopped */
	            	if (imageId.equals(adminImageId)) {
	            		out.println("<td> Admin Instance </td>");
	            	} else {
	            		out.println("<td><a href='InstanceStop?" + instanceId + "'>Stop Worker</a></td>");
	            		averageCpuLoad += cpuLoad / instanceIds.size();
	            	}
	            	out.println("</tr>");
	    		}
	    	}
    	} catch (AmazonServiceException ase) {
            out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon EC2, but was rejected with an error response for some reason.");
            out.println("Error Message:    " + ase.getMessage());
            out.println("HTTP Status Code: " + ase.getStatusCode());
            out.println("AWS Error Code:   " + ase.getErrorCode());
            out.println("Error Type:       " + ase.getErrorType());
            out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with EC2, "
                    + "such as not being able to access the network.");
            out.println("Error Message: " + ace.getMessage());
        }
    	finally {
    		out.println("</table>");
    		out.println("<pr> Average Worker CPU load = " + averageCpuLoad + "</p>");
    	}
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

	/* Process log in and load scaling post requests */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		try {
			int cpuGrowThresh = Integer.parseInt(request.getParameter("CPUGrow"));
			int cpuShrinkThresh = Integer.parseInt(request.getParameter("CPUShrink"));
			int ratioGrow = Integer.parseInt(request.getParameter("RatioGrow"));
			int ratioShrink = Integer.parseInt(request.getParameter("RatioShrink"));
			int enableScaling = 0;
			if (request.getParameter("EnableScaling") != null)
				enableScaling = 1;
			
    		HealthMonitor.cpuHighThreshold = cpuGrowThresh;
    		HealthMonitor.cpuLowThreshold = cpuShrinkThresh;
    		HealthMonitor.growRatio = ratioGrow;
    		HealthMonitor.shrinkRatio = ratioShrink;
    		HealthMonitor.enableScaling = enableScaling;
		} catch (NumberFormatException nfe) {
		}

		doGet(request, response);
	}
}
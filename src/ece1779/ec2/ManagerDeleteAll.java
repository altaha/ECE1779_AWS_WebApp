package ece1779.ec2;

import java.io.*;
import java.sql.Connection;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;


public class ManagerDeleteAll extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		doPost(request,response);
	}
	
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException
	{
    	Connection con = null;
    	
        try
        {
        	String pageTitle = "Manager Delete All";
        	
            PrintWriter out = response.getWriter();
        	response.setContentType("text/html");
            
        	boolean loggedIn = false;
        	
    		Cookie [] pageCookies = request.getCookies();

    		for (int x=0; pageCookies != null && x < pageCookies.length; x++) {
    		    if (pageCookies[x].getName().compareTo("ManagerLoggedIn")==0)
    		    {
    		    	//TODO : for added protection also store token but for now this is good
    		    	loggedIn = true;
    		    }
    		}
        	
            if (loggedIn)
            {
            	boolean success = true;
            	
    			out.println("<html>");
    			out.println("  <head><title>"+pageTitle+"</title></head>");
    			out.println("  <body>");
    			
            	// Get DB connection from pool
    		    DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");
    		    con = dbcp.getConnection();
    		    Statement dbStmt = con.createStatement();
    		    con.setAutoCommit(false);
    		    
    		    dbStmt.executeUpdate("delete from images");
    		    dbStmt.executeUpdate("delete from users");
    		    
	        	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)this.getServletContext().getAttribute("AWSCredentials");
	
	        	AmazonS3 s3 = new AmazonS3Client(awsCredentials);
	            
	        	String bucketName = getServletContext().getAttribute("s3BucketName").toString();
	
	            try {
	            	
	            	ObjectListing objects = s3.listObjects(bucketName);
	
	           		List<S3ObjectSummary> summaries = objects.getObjectSummaries();
	        		for (S3ObjectSummary item : summaries) {
	        			String key = item.getKey();
	        			s3.deleteObject(bucketName, key);
	        			out.println("<p>Deleted " + key +"</p>");
	            		out.flush();
	        		}
	            	
	            	while (objects.isTruncated()) {
	            		objects = s3.listNextBatchOfObjects(objects);
	            		summaries = objects.getObjectSummaries();
	            		for (S3ObjectSummary item : summaries) {
	            			String key = item.getKey();
	            			s3.deleteObject(bucketName, key);
	            			out.println("<p>Deleted " + key +"</p>");
	                		out.flush();
	            		}
	            	}
	
	            	//TODO do we need to delete bucket as well ?
	            	//s3.deleteBucket(bucketName);
	            	out.println("<p>Deleted everything under bucket " + bucketName + "</p>");
	        		
	            	
	            }
	            catch (AmazonServiceException ase) {
	            	success = false;
	            	con.rollback();
	                out.println("Caught an AmazonServiceException, which means your request made it "
	                        + "to Amazon S3, but was rejected with an error response for some reason.");
	                out.println("Error Message:    " + ase.getMessage());
	                out.println("HTTP Status Code: " + ase.getStatusCode());
	                out.println("AWS Error Code:   " + ase.getErrorCode());
	                out.println("Error Type:       " + ase.getErrorType());
	                out.println("Request ID:       " + ase.getRequestId());
	            }
	            catch (AmazonClientException ace) {
	            	success = false;
	            	con.rollback();
	                out.println("Caught an AmazonClientException, which means the client encountered "
	                        + "a serious internal problem while trying to communicate with S3, "
	                        + "such as not being able to access the network.");
	                out.println("Error Message: " + ace.getMessage());
	            }
	            catch(Exception ex) {
	            	success = false;
	            	con.rollback();
	                getServletContext().log(ex.getMessage());  
		      	}    	
		      	finally
		      	{
		      		try {
			            con.commit();
		      			con.close();
		      		} 
		      		catch (Exception e) {
		                  getServletContext().log(e.getMessage()); 
		                  success = false;
		      		}
		      	} 
     	
		      	if(success)
		      		out.println("<h1>Deleted All Data Successfully</h1>");
		      	else
		      		out.println("<h1>Failed to delete all data</h1>");
		      	
		      	out.println("    <li><a href='ManagerLogin'>Manager Control Portal</a></li>");
    			out.println("  </body>");
    			out.println("</html>");
            
            }
            else {
    			out.println("<html>");
    			out.println("  <head><title>"+pageTitle+"</title></head>");
    			out.println("  <body>");
    			
    			out.println("    <h1>Delete All Data</h1>");
    			out.println("Please Login first<br>");
    		    out.println("    <li><a href='ManagerLogin'>Manager Login</a></li>");    
    			out.println("  </body>");
    			out.println("</html>");
            }
            	
        }
		catch (Exception ex) {
		    throw new ServletException (ex);
		}
	
    }
    

    
    
}
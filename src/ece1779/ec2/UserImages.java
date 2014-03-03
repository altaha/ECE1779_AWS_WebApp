package ece1779.ec2;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

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

import org.im4java.core.*;


public class UserImages extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		boolean loggedIn = false;

		String username = "";
		String pageTitle = "User Images";
		
		Cookie [] pageCookies = request.getCookies();

		for (int x=0; pageCookies != null && x < pageCookies.length; x++) {
		    if (pageCookies[x].getName().compareTo("UserLoggedIn")==0)
		    {
		    	//TODO : for added protection also store token but for now this is good
		    	username = pageCookies[x].getValue();
		    	loggedIn = true;
		    }
		}
		
		if (loggedIn)
		{
			doPost(request, response, username);
		}
		else
		{
			out.println("<html>");
			out.println("  <head><title>"+pageTitle+"</title></head>");
			out.println("  <body>");
			
			out.println("    <h1>Upload File</h1>");
			out.println("Please Login<br>");
		    out.println("    <li><a href='UserLogin'>Login</a></li>");    
			out.println("  </body>");
			out.println("</html>");
		}
	}
	
    public void doPost(HttpServletRequest request, HttpServletResponse response,String username)
	throws IOException, ServletException {
        try {

        	// Create a factory for disk-based file items
        	FileItemFactory factory = new DiskFileItemFactory();

    		String pageTitle = "User Images";
   
            // get root directory of web application
            String path = this.getServletContext().getRealPath("/");        
            
            PrintWriter out = response.getWriter();
            
        	
        	response.setContentType("text/html");
            
			out.println("<html>");
			out.println("<hl>Uploaded images for - " +username+"</hl><br>");
			out.println("<body>");
			
            getImages(username, out);
			//TODO : Display thumbnails...
			//TODO : add option to click on thumbnail to view image and all its transforms
			out.println("<br>");
			out.println("<li><a href='UserLogin'>Back To User Page</a></li>");
		    out.println("<li><a href='UserLogout'>Logout</a></li>");    
           
            
            out.println("</body></html>");
	}
	catch (Exception ex) {
	    throw new ServletException (ex);
	}
	
    }
 
    public int getUserId(Connection con, String username)
	throws IOException{
	int dbUserID = -1;
	
	try{ 
    	String    sqlGetUserString = "select id from users where login = (?);";
        PreparedStatement sqlGetUserStmt = con.prepareStatement(sqlGetUserString);
        sqlGetUserStmt.setString(1, username);
        ResultSet rs = sqlGetUserStmt.executeQuery();
        
        if (rs.next())
        {
        	//int fetchsize = rs.getFetchSize();
        	dbUserID = rs.getInt("id");
        }
	}
    catch(Exception ex) {
          getServletContext().log(ex.getMessage());  
	}
    
    return dbUserID;
}
    
    public void getImages(String username, PrintWriter out) {
    	Connection con = null;
    	try{
    		String s3_bucket = getServletContext().getAttribute("s3BucketName").toString();
        	String s3_path = "https://s3.amazonaws.com/" + s3_bucket + "/";
    		DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");
    		con = dbcp.getConnection();
    		
    		int userid = getUserId(con, username);
    		if (userid != -1){
        		Statement sqlQuery = con.createStatement();
        		String sqlGetImage = "select * from images where userId = (?);";
                PreparedStatement sqlGetImageStmt = con.prepareStatement(sqlGetImage);
                sqlGetImageStmt.setInt(1, userid);
                ResultSet result = sqlGetImageStmt.executeQuery();
        		
        		if (result != null){
        			while (result.next()){
        				int imageId = result.getInt("id");
        				String imgURL = result.getString("key1");
        				out.println("<img src='" + s3_path + imgURL + "' /></a><br>");
        			}
        		}
    		}
    		else
    			out.println("<br><li>Bad username</li>");
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
    }
    
    public void updateDatabase(String key, PrintWriter out) {
    	Connection con = null;
    	try{ 
            // In real life, you should get these values from a shopping cart
            int userid = 1;
            
		    // Get DB connection from pool
		    DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");
		    con = dbcp.getConnection();
	
	
		    // Execute SQL query
		    Statement stmt = con.createStatement();
            String    sql = "insert into files (id, s3Key) "+
                      "value(" + userid + ",'" + key + "')";
            stmt.execute(sql);
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

    }   
     
    
    public void s3SaveFile(File file, String key, PrintWriter out) throws IOException {

    	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)this.getServletContext().getAttribute("AWSCredentials");

    	AmazonS3 s3 = new AmazonS3Client(awsCredentials);
        
        String bucketName = "ece1779winter2013";
 


        try {
            s3.putObject(new PutObjectRequest(bucketName, key, file));
            
            s3.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);

        } catch (AmazonServiceException ase) {
            out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            out.println("Error Message:    " + ase.getMessage());
            out.println("HTTP Status Code: " + ase.getStatusCode());
            out.println("AWS Error Code:   " + ase.getErrorCode());
            out.println("Error Type:       " + ase.getErrorType());
            out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            out.println("Error Message: " + ace.getMessage());
        }
    }
    
    
}
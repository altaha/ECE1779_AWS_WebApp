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


public class UserFileUpload extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		boolean loggedIn = false;

		String pageTitle = "User Upload File Page";
		String username = "";
		
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
			out.println("<html>");
			out.println("  <head><title> "+pageTitle+" - "+username+"</title></head>");
			out.println("  <body>");
			
			out.println("    <h1>Upload File</h1>");
			out.println("    <form action='/ece1779/servlet/UserFileUpload'  enctype='multipart/form-data' method='post'>");
			out.println("      User ID <input type='text' name='userID' value='"+username+"'><br />"); //TODO: do we need this?
			out.println("      What is the image files to upload? <input type='file' name='theFile'><br />");
			out.println("      <input type='submit' value='Send'>");
			out.println("      <input type='reset'>");
			out.println("    </form>");
			out.println("<br>");
			out.println("<li><a href='UserLogin'>Back To User Page</a></li>");
		    out.println("<li><a href='UserLogout'>Logout</a></li>");    
			out.println("  </body>");
			out.println("</html>");
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
	
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException
	{
        try
        {

            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            
        	String s3_bucket = getServletContext().getAttribute("s3BucketName").toString();
        	String s3_path = "https://s3.amazonaws.com/" + s3_bucket + "/";
        	
        	// Create a factory for disk-based file items
        	FileItemFactory factory = new DiskFileItemFactory();

        	// Create a new file upload handler
        	ServletFileUpload upload = new ServletFileUpload(factory);

        	// Parse the request
        	List /* FileItem */ items = upload.parseRequest(request);
        	
        	
        	//TODO : get userid from cookie instead?
        	// User ID
        	FileItem item1 = (FileItem)items.get(0);
        	
        	String name = item1.getFieldName();
        	String username = item1.getString();
        	
        	//Uploaded File
            FileItem theFile = (FileItem)items.get(1);


            // filename on the client
            String fileName = theFile.getName();
   
            // get root directory of web application
            String path = this.getServletContext().getRealPath("/");        

            
            String key1 = "MyObjectKey_" + UUID.randomUUID();
            String key2 = "MyObjectKey_" + UUID.randomUUID();
            String key3 = "MyObjectKey_" + UUID.randomUUID();
            String key4 = "MyObjectKey_" + UUID.randomUUID();

            
            String name1 = path+key1;
            String name2 = path+key2;
            String name3 = path+key3;
            String name4 = path+key4;
            
            // store file in server
            File file1 = new File(name1); 
            theFile.write(file1);

            // Use imagemagik to transform image
            IMOperation op = new IMOperation();
            op.addImage();
            op.resize(320,240);
            op.addImage();
            
            ConvertCmd cmd = new ConvertCmd();
            cmd.run(op, name1, name2);

            File file2 = new File(name2);
            
            
            // transform2 - rotate 90
            IMOperation op2 = new IMOperation();
            op2.addImage();
            op2.rotate(90.0);
            op2.addImage();
            
            ConvertCmd cmd2 = new ConvertCmd();
            cmd2.run(op2, name2, name3);

            File file3 = new File(name3);
            
            // transform3 - solarize
            IMOperation op3 = new IMOperation();
            op3.addImage();
            op3.flip();
            op3.addImage();
            
            ConvertCmd cmd3 = new ConvertCmd();
            cmd3.run(op3, name3, name4);

            File file4 = new File(name4);
            
            out.println("<html><head><title>File Uploaded - "+username+"</title></head>");
            out.println("<body>");
            
        	if (updateDatabase(username,key1,key2,key3,key4,out))
        	{
                s3SaveFile(file1, key1, out);
            	s3SaveFile(file2, key2, out);
            	s3SaveFile(file3, key3, out);
            	s3SaveFile(file4, key4, out);
                
                out.println("<h1>1st Image</h1>");
                out.println("<img src='"+ s3_path + key1 + "' />");
                out.println("<h1>2nd Image</h1>");
                out.println("<img src='"+ s3_path + key2 + "' />");
                out.println("<h1>3rd Image</h1>");
                out.println("<img src='"+ s3_path + key3 + "' />");
                out.println("<h1>4th Image</h1>");
                out.println("<img src='"+ s3_path + key4 + "' />");
                out.println("<br>");
    			out.println("<li><a href='UserLogin'>Back To User Page</a></li>");
    		    out.println("<li><a href='UserLogout'>Logout</a></li>"); 
                out.println("</body></html>");
        	}
        	else
        	{
        		out.println("<h1>Failed! Please reupload files</h1>");
        	}
        	   
        	file1.delete();
        	file2.delete();
        	file3.delete();
        	file4.delete();

             
	}
	catch (Exception ex) {
	    throw new ServletException (ex);
	}
	
    }
    
    public int getUserId(Connection con, String username)
    	throws IOException
    {
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
    
    public boolean updateDatabase(String username, String key1, String key2, String key3, String key4, PrintWriter out)
    	throws IOException
    {
    	boolean success = false;
    	Connection con = null;
    	try{ 
		    // Get DB connection from pool
		    DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");
		    con = dbcp.getConnection();
		    
		    // Get user id
        	int userid = getUserId(con, username);
        	if (userid != -1)
        	{
			    // Execute SQL query
	        	String    sqlInsertUserString = "insert into images (userid, key1, key2, key3, key4) values (?,?,?,?,?);";
	        	PreparedStatement sqlInsertUserStmt = con.prepareStatement(sqlInsertUserString);
	        	sqlInsertUserStmt.setInt(1, userid);
	        	sqlInsertUserStmt.setString(2, key1);
	        	sqlInsertUserStmt.setString(3, key2);
	        	sqlInsertUserStmt.setString(4, key3);
	        	sqlInsertUserStmt.setString(5, key4);
	        	sqlInsertUserStmt.executeUpdate();
	        	success = true;
        	}
        	else
        	{
        		out.println("<h1>failed to get userID "+userid+"</h1>");
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

    	return success;
    }   
     
    
    public void s3SaveFile(File file, String key, PrintWriter out) throws IOException {

    	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)this.getServletContext().getAttribute("AWSCredentials");

    	AmazonS3 s3 = new AmazonS3Client(awsCredentials);

    	String bucketName = "ece1779Bouzelouf";
 
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
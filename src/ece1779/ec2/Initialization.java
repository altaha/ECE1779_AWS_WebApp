package ece1779.ec2;

import javax.servlet.http.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import com.amazonaws.auth.BasicAWSCredentials; 

public class Initialization extends HttpServlet {
    public void init(ServletConfig config) {
    	try {
		    //Initialize connection pool
    		String accessKey = config.getInitParameter("AWSaccessKey");
    		String secretKey = config.getInitParameter("AWSsecretKey");
    		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
    		ServletContext context = config.getServletContext();
    			
    		context.setAttribute("AWSCredentials", awsCredentials);
    		
    		String dbDriver = config.getInitParameter("dbDriver");
    		String dbURL = config.getInitParameter("dbURL");
    		String dbUser = config.getInitParameter("dbUser");
    		String dbPassword = config.getInitParameter("dbPassword");
    		
		    DriverAdapterCPDS ds = new DriverAdapterCPDS();
		    ds.setDriver(dbDriver);
		    ds.setUrl(dbURL);
		    
		    ds.setUser(dbUser);
		    ds.setPassword(dbPassword);
		    
		    SharedPoolDataSource dbcp = new SharedPoolDataSource();
		    dbcp.setConnectionPoolDataSource(ds);

		    context.setAttribute("dbpool",dbcp);
		    
		    //store admin user and pass in context
		    String adminUserID = config.getInitParameter("adminUserID");
    		String adminPassword = config.getInitParameter("adminPassword");
    		context.setAttribute("adminUserID",adminUserID);
    		context.setAttribute("adminPassword",adminPassword);
    		
    		//store s3 bucket name
    		String s3BucketName = config.getInitParameter("s3BucketName");
    		context.setAttribute("s3BucketName",s3BucketName);
		}
		catch (Exception ex) {
		    getServletContext().log("SQLGatewayPool Error: " + ex.getMessage());
		}
    }
}

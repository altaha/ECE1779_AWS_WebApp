package ece1779.ec2;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;



public class InstanceStart extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Env Variables</title>");
        out.println("</head>");
        out.println("<body>");
 
        ec2example(out);
        
        out.println("</body>");
        out.println("</html>");
    }
    
    
    void ec2example(PrintWriter out) throws IOException {
        /*
         * Important: Be sure to fill in your AWS access credentials in the
         *            AwsCredentials.properties file before you try to run this
         *            sample.
         * http://aws.amazon.com/security-credentials
         */
    	
    	
    	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)getServletContext().getAttribute("AWSCredentials");
    	
		
    	
        AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
        
        
        out.println("===========================================");
        out.println("Getting Started with Amazon EC2");
        out.println("===========================================\n");

        try {
        	String imageId = "ami-1f4e4d76";
        	RunInstancesRequest request = new RunInstancesRequest(imageId,1,1);
        	request.setKeyName("ece1779winter2014v3");
        	RunInstancesResult result = ec2.runInstances(request);
        	Reservation reservation = result.getReservation();
        	List<Instance> instances = reservation.getInstances();
        	Instance inst = instances.get(0);

        	
        	
        	out.println("Instance Info = " + inst.toString());
        	/*
             * List the buckets in your account
             */
            out.println("Listing buckets");
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
    }
    
}
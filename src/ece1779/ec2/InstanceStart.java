package ece1779.ec2;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;


public class InstanceStart extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Start Instance</title>");
        out.println("</head>");
        out.println("<body>");
 
        doInstanceStart(out);
        
        out.println("</body>");
        out.println("</html>");
    }
    
    
    void doInstanceStart(PrintWriter out) throws IOException {
 	
		BasicAWSCredentials awsCredentials = (BasicAWSCredentials)getServletContext().getAttribute("AWSCredentials");

		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
		AmazonElasticLoadBalancing loadBalancer = new  AmazonElasticLoadBalancingClient(awsCredentials);
		
		try {
        	String imageId = "ami-1f4e4d76";
        	RunInstancesRequest request = new RunInstancesRequest(imageId,1,1);
        	request.setKeyName("ece1779winter2014v3");
        	request.withMonitoring(true);
        	
        	String securityGroup = "ece1779security";
        	List<String> securityGroups = Arrays.asList(securityGroup);
        	request.setSecurityGroupIds(securityGroups);

        	/* create new instance */
        	RunInstancesResult result = ec2.runInstances(request);
        	Reservation reservation = result.getReservation();
        	List<Instance> instances = reservation.getInstances();
        	Instance inst = instances.get(0);
        	
        	/* add to Load Balancer */
        	String balancerName = "BouzeloufBalancer";
        	com.amazonaws.services.elasticloadbalancing.model.Instance balanceInstance = new com.amazonaws.services.elasticloadbalancing.model.Instance(inst.getInstanceId());
            List <com.amazonaws.services.elasticloadbalancing.model.Instance> balanceInstances = Arrays.asList(balanceInstance);

        	RegisterInstancesWithLoadBalancerRequest balancerRequest =
        			new RegisterInstancesWithLoadBalancerRequest(balancerName, balanceInstances);
        	RegisterInstancesWithLoadBalancerResult balancerResult = loadBalancer.registerInstancesWithLoadBalancer(balancerRequest);

        	out.println("<p>New Instance Info: </p>");
        	out.println("<p> " + inst.toString() + " </p>");
        	out.println("<p>Load Balancer Registration Info: </p>");
        	out.println("<p> " + balancerResult.toString() + " </p>");

			out.println("<br>");
			out.println("<p><a href='ManagerLogin'>Manager Page</a></p>");

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

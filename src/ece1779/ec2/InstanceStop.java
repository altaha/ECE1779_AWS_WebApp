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
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;


public class InstanceStop extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Stop Instance</title>");
        out.println("</head>");
        out.println("<body>");
 
        /* Note: the caller sends a URL of the form: 'InstanceStop?<instanceId>' */
        doInstanceStop(out, request.getQueryString());
        
        out.println("</body>");
        out.println("</html>");
    }
	
	void doInstanceStop(PrintWriter out, String instanceId) throws IOException {
		
		BasicAWSCredentials awsCredentials = (BasicAWSCredentials)getServletContext().getAttribute("AWSCredentials");

		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
		AmazonElasticLoadBalancing loadBalancer = new  AmazonElasticLoadBalancingClient(awsCredentials);
		
		/* the manager instance cannot be stopped */
    	if (instanceId.equals("i-7dce8d53")) {
    		out.println("Cannot terminate main instance");
    		return;
    	}
		
		try {
			/* remove fro load balancer */
			String balancerName = "BouzeloufBalancer";
        	com.amazonaws.services.elasticloadbalancing.model.Instance balanceInstance = new com.amazonaws.services.elasticloadbalancing.model.Instance(instanceId);
            List <com.amazonaws.services.elasticloadbalancing.model.Instance> balanceInstances = Arrays.asList(balanceInstance);

        	DeregisterInstancesFromLoadBalancerRequest balancerRequest =
        			new DeregisterInstancesFromLoadBalancerRequest(balancerName, balanceInstances);
        	DeregisterInstancesFromLoadBalancerResult balancerResult = loadBalancer.deregisterInstancesFromLoadBalancer(balancerRequest);
			
			/* terminate instance */
			List<String> instanceIdList = Arrays.asList(instanceId);
			TerminateInstancesRequest request = new TerminateInstancesRequest(instanceIdList);
			TerminateInstancesResult result = ec2.terminateInstances(request);
			
			out.println("<p>Terminated instanceId " + instanceId + "</p>");
			out.println("<p>" + result.toString() + "</p>");
			out.println("<br>");
			out.println("<p><a href='ManagerLogin'>Manager Page</a></p>");
			
        } catch (AmazonClientException ace) {
            out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with EC2, "
                    + "such as not being able to access the network.");
            out.println("Error Message: " + ace.getMessage());
        }
		
	}
}

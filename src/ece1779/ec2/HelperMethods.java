package ece1779.ec2;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Vector;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;

public class HelperMethods {
	
	public static List<String> getRunningInstances (BasicAWSCredentials awsCredentials, String imageId)
		throws AmazonServiceException, AmazonClientException {
		
		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);

		List<String> runningInstanceIds = new ArrayList<String>();
		
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		
		// filter for running instances of given image ID
		List<Filter> instanceFilters = new ArrayList<Filter>();
		instanceFilters.add(new Filter("image-id", Arrays.asList(imageId)));
		instanceFilters.add(new Filter("instance-state-name", Arrays.asList("running")));
		
		describeInstancesRequest.setFilters(instanceFilters);
		
		//get instances description
		DescribeInstancesResult describeInstancesResult = ec2.describeInstances (describeInstancesRequest);
		
		List<Reservation> reservations = describeInstancesResult.getReservations();
		
		for (Reservation reservation : reservations) {
			Instance instance = reservation.getInstances().get(0);
			runningInstanceIds.add(instance.getInstanceId());
		}
		
		return runningInstanceIds;
	}
	
	public static double getCPULoad (BasicAWSCredentials awsCredentials, String instanceID)
		throws AmazonServiceException, AmazonClientException {
		
		AmazonCloudWatch cw = new AmazonCloudWatchClient(awsCredentials);
		
		// get metrics only for given instanceID
		List<DimensionFilter> instanceFilter = new ArrayList<DimensionFilter>();
		instanceFilter.add(new DimensionFilter().withName("InstanceId").withValue(instanceID));
		
  		ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
    	listMetricsRequest.setMetricName("CPUUtilization");
    	listMetricsRequest.setNamespace("AWS/EC2");
    	listMetricsRequest.setDimensions(instanceFilter);
    	
    	ListMetricsResult result = cw.listMetrics(listMetricsRequest);
    	java.util.List<Metric> metrics = result.getMetrics();

    	for (Metric metric : metrics) {
    		String namespace = metric.getNamespace();
    		String metricName = metric.getMetricName();
    		List<Dimension> dimensions = metric.getDimensions();
    		
        	GetMetricStatisticsRequest statisticsRequest = new GetMetricStatisticsRequest();
        	statisticsRequest.setNamespace(namespace);
        	statisticsRequest.setMetricName(metricName);
        	statisticsRequest.setDimensions(dimensions);

        	Date endTime = new Date();
        	Date startTime = new Date(0);
        	startTime.setTime(endTime.getTime()-120000); /* 2 minutes */
        	statisticsRequest.setStartTime(startTime);
        	statisticsRequest.setEndTime(endTime);
        	statisticsRequest.setPeriod(60); /* 60 second granulatiry */
        	
        	Vector<String>statistics = new Vector<String>();
        	statistics.add("Maximum");
        	statisticsRequest.setStatistics(statistics);
        	GetMetricStatisticsResult stats = cw.getMetricStatistics(statisticsRequest);
        	
        	if (stats.getDatapoints().size() == 0)
        		return 0;
        	
        	/* get the latest CPU timestamp */
        	List<Datapoint> dataPoints = stats.getDatapoints();
        	Date latestTime = new Date(0);
        	Datapoint latestPoint = null;
        	for (Datapoint dataPoint : dataPoints) {
        		if (dataPoint.getTimestamp().after(latestTime)) {
        			latestTime = dataPoint.getTimestamp();
        			latestPoint = dataPoint;
        		}
        	}

        	return latestPoint.getMaximum();
    	}
    	
    	return 0;
	}

}

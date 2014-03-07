package ece1779.ec2;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.*;


public class HealthMonitor extends TimerTask {
	
	//public
	public static double lastAvgCPU;
	public static int cpuHighThreshold;
	public static int cpuLowThreshold;
	public static int growRatio;
	public static int shrinkRatio;

	public static int enableScaling;
	public static String accessKey;
	public static String secretKey;
	

  /** Construct and use a TimerTask and Timer. */
  public static void startTimer (long period) {
	  
	  if (timerStarted != 0)
		  return;
		  
	  timerStarted = 1; /* only run a single timer */
	  
	  scaler = new LoadScaler();
	  
	  TimerTask healthMonitor = new HealthMonitor();
	  if (period > fONCE_PER_Hour || period < 0)
		  period = fONCE_PER_Hour;
	  
	  Timer timer = new Timer();
	  timer.scheduleAtFixedRate(healthMonitor, 0*1000, period); /* start timer after 10 second delay */
  }

  /**
  * Implements TimerTask's abstract run method.
  */
  @Override public void run(){
	  
	  double cpuLoad = scaler.getCpuLoad();
	  
	  if (cpuLoad > cpuHighThreshold) {
		  scaler.growInstances(growRatio);
	  } else if (cpuLoad < cpuLowThreshold) {
		  scaler.shrinkInstances(shrinkRatio);
	  }
  }

  //PRIVATE
  private static int timerStarted;
  private static LoadScaler scaler;

  private final static long fONCE_PER_Hour = 1000*60*60; //in milliseconds
}

class LoadScaler {
	private static final String imageId = "ami-2d888444";
	private static final String keyName = "ece1779winter2014v3";
	private static final String loadBalancerName = "BouzeloufBalancer";

	private BasicAWSCredentials awsCredentials;
	
	public LoadScaler() {
		awsCredentials = new BasicAWSCredentials(HealthMonitor.accessKey, HealthMonitor.secretKey);
	}
	
	public double getCpuLoad() {
		
		double cpu_avg = 100;

        AmazonCloudWatch cw = new AmazonCloudWatchClient(this.awsCredentials);

    	/* print instance entries */
    	try {

    		/* Filter for instances running our AMI */
    		List<DimensionFilter> amiFilter = new ArrayList<DimensionFilter>();
    		amiFilter.add(new DimensionFilter().withName("ImageId").withValue(imageId));
    		
    		ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
        	listMetricsRequest.setMetricName("CPUUtilization");
        	listMetricsRequest.setNamespace("AWS/EC2");
        	listMetricsRequest.setDimensions(amiFilter);

        	ListMetricsResult result = cw.listMetrics(listMetricsRequest);
        	java.util.List<Metric> 	metrics = result.getMetrics();

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

            	cpu_avg = latestPoint.getMaximum();
        	}

        } catch (AmazonServiceException ase) {
        	cpu_avg = 200;
        } catch (AmazonClientException ace) {
        	cpu_avg = 300; 
        }
        
        HealthMonitor.lastAvgCPU = cpu_avg;
		
		return cpu_avg;
	}
	
	public void growInstances(int growRatio) {
	}
	
	public void shrinkInstances(int shrinkRatio) {
	}
}


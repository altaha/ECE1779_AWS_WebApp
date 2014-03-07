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
	public static int cpuHighThreshold;
	public static int cpuLowThreshold;
	public static int growRatio;
	public static int shrinkRatio;
	public static int enableScaling;
	
	public static int status;
	public static String statusString;

	public static String accessKey;
	public static String secretKey;
	public static String workerImageId;
	

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
	  timer.scheduleAtFixedRate(healthMonitor, 10*1000 , period); /* start timer after 10 second delay */
  }

  /**
  * Implements TimerTask's abstract run method.
  */
  @Override public void run(){

	  if (enableScaling == 0)
		  return;

	  //wait 60 seconds before scaling operations
	  if (scaler.getTimeSinceLastScale() <= 60 * 1000)
		  return;
	  
	  HealthMonitor.status = 20;
	  
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

	private BasicAWSCredentials awsCredentials;
	
	private Date lastScaled;
	
	public LoadScaler() {
		awsCredentials = new BasicAWSCredentials(HealthMonitor.accessKey, HealthMonitor.secretKey);

		lastScaled = new Date(0);
	}
	
	public double getCpuLoad() {
		
		double averageCpuLoad = 0;

    	try {
    		List<String> instanceIds = HelperMethods.getRunningInstances(
    				this.awsCredentials, HealthMonitor.workerImageId);
    		
    		for (String instanceId : instanceIds) {
    			averageCpuLoad += HelperMethods.getCPULoad(this.awsCredentials, instanceId);
    		}
    		
    		if (instanceIds.size() > 0)
    			averageCpuLoad /= instanceIds.size();

        } catch (AmazonServiceException ase) {
        } catch (AmazonClientException ace) {
        }

        return averageCpuLoad;
	}
	
	public void growInstances(int growRatio) {
		try {    		
    		List<String> instanceIds = HelperMethods.getRunningInstances(
    				this.awsCredentials, HealthMonitor.workerImageId);
    		
    		int numInstancesToAdd = 0;
    		int numRunningInstances = instanceIds.size();
    		if (numRunningInstances <= 0) {
    			numInstancesToAdd = 1;
    		} else {
    			numInstancesToAdd = numRunningInstances * growRatio - numRunningInstances;
    		}
    		
    		if (numInstancesToAdd <= 0)
    			return;
    		
    		for (int i = 0; i < numInstancesToAdd; i++) {
    			HelperMethods.startInstance(this.awsCredentials, HealthMonitor.workerImageId, null);
    		}
    		
    		this.lastScaled = new Date();
    		
    		HealthMonitor.status = 50;

        } catch (AmazonServiceException ase) {
        } catch (AmazonClientException ace) {
        }
	}
	
	public void shrinkInstances(int shrinkRatio) {
		
		try {

    		List<String> instanceIds = HelperMethods.getRunningInstances(
    				this.awsCredentials, HealthMonitor.workerImageId);
    		
    		int numRunningInstances = instanceIds.size();
    		if (numRunningInstances <= 0 || shrinkRatio <= 0)
    			return;
    		
    		int shrinkedInstanceCount = (int) Math.ceil(numRunningInstances / shrinkRatio);
    		if (shrinkedInstanceCount <= 0)
    			shrinkedInstanceCount = 1;
    		
    		int numInstancesToStop = numRunningInstances - shrinkedInstanceCount;
    		if (numInstancesToStop <= 0)
    			return;
    		
    		for (int i = 0; i < numInstancesToStop; i++) {
    			HelperMethods.stopInstance(this.awsCredentials, instanceIds.get(i), null);
    		}

    		this.lastScaled = new Date();
    		
    		HealthMonitor.status = 100;

        } catch (AmazonServiceException ase) {
        } catch (AmazonClientException ace) {
        }

	}
	
	public long getTimeSinceLastScale() {
		Date endTime = new Date();
		return (endTime.getTime() - this.lastScaled.getTime());
	}
}


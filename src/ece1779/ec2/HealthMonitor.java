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
	
	//public static HealthMonitorTimer periodicMonitor;
	
	public static int cpuHighThreshold;
	public static int cpuLowThreshold;
	public static int growRatio;
	public static int shrinkRatio;
	public static int enableScaling;
	
	private static int timerStarted;
	
	private static LoadScaler scaler;
	
	/* public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	} */
/*}


class HealthMonitorTimer extends TimerTask {
*/

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
	  timer.scheduleAtFixedRate(healthMonitor, 0, period);
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

  // PRIVATE

  //expressed in milliseconds
  private final static long fONCE_PER_Hour = 1000*60*60;
}

class LoadScaler {
	private static final String imageId = "ami-1f4e4d76";
	private static final String keyName = "ece1779winter2014v3";
	private static final String loadBalancerName = "BouzeloufBalancer";
	
	private int cpuAvg;
	private int numInstances;
	
	public LoadScaler() {
		cpuAvg = 0;
		numInstances = 0;
	}
	
	public double getCpuLoad() {
		return 0;
	}
	
	public void growInstances(int growRatio) {
	}
	
	public void shrinkInstances(int shrinkRatio) {
	}
}


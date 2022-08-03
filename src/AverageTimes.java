import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class AverageTimes {

	private HashMap<String, List<Double>> stepAttempts;
	private HashMap<String, Double> averageTimes;
	private HashMap<String, Double> sdTimes;
	
	public AverageTimes() {
		stepAttempts = new HashMap<String, List<Double>>();
		averageTimes = new HashMap<String, Double>();
		sdTimes = new HashMap<String, Double>();
	}
	
	public void addStepAttempt(String step, double time) {
		if (!stepAttempts.containsKey(step)) {
			stepAttempts.put(step, new ArrayList<Double>());
		}
		
		stepAttempts.get(step).add(time);
	}
	
	public void computeStepValues() {
		// Loop for every steps
		for (Entry<String, List<Double>> e : stepAttempts.entrySet()) {
			double totalTime = 0;
			
			// First loop: computes average
			for (Double time : e.getValue()) {
				totalTime += time;
			}
			
			averageTimes.put(e.getKey(), totalTime / ((double)e.getValue().size()));
			double totalSquaredDeviations = 0;
			
			// Second loop: computes standard deviations
			for (Double time : e.getValue()) {
				totalSquaredDeviations += Math.pow(time - averageTimes.get(e.getKey()), 2);
			}
			
			sdTimes.put(e.getKey(), Math.sqrt(totalSquaredDeviations/((double)e.getValue().size())) );
		}
	}
	
	public double unitizeStepTime(String step, double time) {
		return (time - averageTimes.get(step)) / sdTimes.get(step);
	}
	
	public double getAverage(String step) {
		return averageTimes.get(step);
	}
	
	public double getSD(String step) { 
		return sdTimes.get(step);
	}
}

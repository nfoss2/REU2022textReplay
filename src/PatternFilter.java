import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class PatternFilter {

	public void matchPatterns(List<Pattern> patterns, Map<Clip, BehaviorInterpretation> clipInterpretations, Map<Integer, Boolean> clipClassifications) {
		
		int tempCount = 0;
		
		for (Entry<Clip, BehaviorInterpretation> entry : clipInterpretations.entrySet()) {
			Clip clip = entry.getKey();
			BehaviorInterpretation interpretation = entry.getValue();
			
			List<UserAction> actions = clip.getActions();
			for (int i = 0; i < actions.size(); i++) {
				UserAction previousPreviousAction = null;
				UserAction previousAction = null;
				UserAction currentAction = null;
				
				if (i > 0) {
					previousAction = actions.get(i - 1);
					currentAction = actions.get(i);
				}
				
				if (i > 1) {
					previousPreviousAction = actions.get(i - 2);
				}
				
				for (Pattern pattern : patterns) {
					if (pattern.getNumberOfAction() == 2 && previousAction != null) {
						List<UserAction> actionList = new ArrayList<>();
						actionList.add(previousAction);
						actionList.add(currentAction);
						
						if (pattern.matches(interpretation, actionList)) {
							int id = clip.getID();
							if(clipClassifications.get(id)) {
								pattern.addGamingClip(clip);
							} else {
								pattern.addNonGamingClip(clip);
							}
						}
					} else if (pattern.getNumberOfAction() == 3 && previousPreviousAction != null) {
						List<UserAction> actionList = new ArrayList<>();
						actionList.add(previousPreviousAction);
						actionList.add(previousAction);
						actionList.add(currentAction);
						if (pattern.matches(interpretation, actionList)) {
							int id = clip.getID();
							if(clipClassifications.get(id)) {
								pattern.addGamingClip(clip);
							} else {
								pattern.addNonGamingClip(clip);
							}
						}
					}
				}
			}
			
			tempCount++;
			if (tempCount % 100 == 0) {
				System.out.println(tempCount);
			}
		}
	}
	
	public void calculateKappas(List<Pattern> allPatterns, int totalGaming, int totalNonGaming) {
		
		for (Pattern pattern : allPatterns) {
			pattern.calculateKappa(totalGaming, totalNonGaming);
		}
	}
	
	public List<Pattern> executeForwardSelection(String modelString, List<Pattern> allPatterns, Map<Clip, BehaviorInterpretation> clipInterpretations, Map<Integer, Boolean> clipClassifications) throws IOException {
		// Forward selection of best patterns
		FileWriter bestPatternWriter = new FileWriter("bestPatterns-" + modelString + ".csv");
		bestPatternWriter.write(Pattern.getHeader() + ",best Kappa\n");
		
		double minimumTPFPRatio = 1.00;
		
		int testVariable = 0;
		int currentTP = 0;
		int currentFP = 0;
		double currentKappa = 0;
		
		List<Pattern> remainingPatterns = new ArrayList<>(allPatterns);
		List<Pattern> bestPatterns = new ArrayList<>();
		
		Map<Clip, BehaviorInterpretation> remainingClipInterpretations = new HashMap<Clip, BehaviorInterpretation>(clipInterpretations);
		
		int totalGaming = 0;
		int totalNonGaming = 0;
		
		for (Boolean label : clipClassifications.values()) {
			if (label.booleanValue()) {
				totalGaming++;
			} else {
				totalNonGaming++;
			}
		}
		
		matchPatterns(remainingPatterns, remainingClipInterpretations, clipClassifications);
		calculateKappas(remainingPatterns, totalGaming, totalNonGaming);
		
		for (int index = remainingPatterns.size() - 1; index >= 0; index--) {
			if (remainingPatterns.get(index).getKappa() < 0.050) {
				remainingPatterns.remove(index);
			}
		}
		
		while (testVariable == 0) {
			// First eliminate bad patterns
			for (int index = remainingPatterns.size() - 1; index >= 0; index--) {
				remainingPatterns.get(index).calculateKappa(totalGaming, totalNonGaming);
				if (remainingPatterns.get(index).getKappa() <= 0) {
					remainingPatterns.remove(index);
				}
			}
			
			// Find best pattern
			int bestPatternIndex = -1;
			double bestKappa = currentKappa;
			
			for (int index = 0; index < remainingPatterns.size(); index++) {
				Pattern pattern = remainingPatterns.get(index);
				if (pattern.getTPFPRatio() >= minimumTPFPRatio) {
					int totalTP = currentTP + pattern.getTP();
					int totalFP = currentFP + pattern.getFP();
					
					double newKappa = Kappa.calculateKappa(totalGaming, totalNonGaming, totalTP, totalFP);
					if (newKappa > bestKappa) {
						bestKappa = newKappa;
						bestPatternIndex = index;
					}
				}
			}
			
			if (bestPatternIndex == -1) {
				// If no best pattern, reduce minimum TP/FP Ratio (break if minimum is already 0)
				minimumTPFPRatio -= 0.05;
				if (minimumTPFPRatio < 0)
					break;
			} else {
				// If a best pattern is found
				// add this pattern to the list of best pattern
				Pattern bestPattern = remainingPatterns.get(bestPatternIndex);
				remainingPatterns.remove(bestPatternIndex);
				bestPatterns.add(bestPattern);
				
				// remove clips matching this pattern.
				for (Clip clip : bestPattern.getAllMatchingClips()) {
					remainingClipInterpretations.remove(clip);
				}
				
				//  output the pattern to the result file
				bestPatternWriter.write(bestPattern.getString() + "," + bestKappa + "\n");
				
				currentKappa = bestKappa;
				currentTP += bestPattern.getTP();
				currentFP += bestPattern.getFP();
			}
			
			// Reset every patterns
			for (Pattern pattern : remainingPatterns) {
				pattern.resetPerformance();
			}
			
			System.out.println("=========== forward selection cycle (" + bestPatterns.size() + ") (" + currentKappa + ") ============");
			
			// Compute the pattern matches for the remaining patterns and clips
			
			matchPatterns(remainingPatterns, remainingClipInterpretations, clipClassifications);
		}
		
		bestPatternWriter.write("\n\n" + currentKappa + "\n");
		
		bestPatternWriter.flush();
		bestPatternWriter.close();
		
		return bestPatterns;
	}
}

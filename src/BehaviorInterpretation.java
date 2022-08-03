import java.nio.file.NotDirectoryException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.soap.SOAPBinding.Use;

public class BehaviorInterpretation {

	private final Clip clip;
	private Behavior diagnostic;
	
	private Map<UserAction, List<InterpretationElement>> interpretationBeforeAction;
	private Map<UserAction, List<InterpretationElement>> interpretationAfterAction;
	
	private Map<GamingPattern, Integer> patternCounts;
	
	public BehaviorInterpretation(Clip clip) {
		this.clip = clip;
		interpretationBeforeAction = new HashMap<UserAction, List<InterpretationElement>>();
		interpretationAfterAction = new HashMap<UserAction, List<InterpretationElement>>();
		
		patternCounts = new HashMap<GamingPattern, Integer>();
		for (GamingPattern pattern : GamingPattern.values()) {
			patternCounts.put(pattern, 0);
		}
	}
	
	public void setDiagnostic(Behavior behavior) {
		diagnostic = behavior;
	}
	
	public Behavior getDiagnostic() {
		return diagnostic;
	}
	
	public Clip getClip() {
		return clip;
	}
	
	public void addInterpretationBefore(UserAction action, InterpretationElement element) {
		if (element == null) {
			return;
		}
		
		if (!interpretationBeforeAction.containsKey(action)) {
			interpretationBeforeAction.put(action, new ArrayList<InterpretationElement>());
		}
		
		interpretationBeforeAction.get(action).add(element);
		
		addToInterpretationsCount(element);
	}
	
	public void addInterpretationAfter(UserAction action, InterpretationElement element) {
		if (element == null) {
			return;
		}
		
		if (!interpretationAfterAction.containsKey(action)) {
			interpretationAfterAction.put(action, new ArrayList<InterpretationElement>());
		}
		
		interpretationAfterAction.get(action).add(element);
		
		addToInterpretationsCount(element);
	}

	// TODO : temp for now basic model of diagnosis (decide later if this method is still useful)
	public List<InterpretationElement> getAllInterpretationElements() {
		List<InterpretationElement> elements = new ArrayList<InterpretationElement>();
		
		for (UserAction action : clip.getActions()) {
			if (interpretationBeforeAction.containsKey(action)) {
				elements.addAll(interpretationBeforeAction.get(action));
			}
			
			if (interpretationAfterAction.containsKey(action)) {
				elements.addAll(interpretationAfterAction.get(action));
			}
		}
		
		return elements;
	}

	public List<Integer> joinLists(List<Integer> l1, List<Integer> l2) {
		List<Integer> newL = new ArrayList<Integer>(Collections.nCopies(20, 0));
		Integer index = 0;
		for (Integer i : l1) {
			if (i == 1) {
				newL.set(index, 1);
			}
			index += 1;
		}
		index = 0;
		for (Integer i : l2) {
			if (i == 1) {
				newL.set(index, 1);
			}
			index += 1;
		}
		return newL;
	}

	public String listToString(List<Integer> interpList) {
		StringBuilder builder = new StringBuilder();
		for (Integer i : interpList) {
			builder.append(i.toString() + ",");
		}
		return builder.toString();
	}

	public String getInterpretationOther(Integer clip_num) {
		StringBuilder builder = new StringBuilder();

		if (diagnostic == Behavior.BAD_CLIP) {
			builder.append("Bad Clip!");
		}
		if (clip_num == 1061) {
			System.out.println(clip.getActionCountString());
		}

		Integer action_id = 0;
		for (UserAction action : clip.getActions()) {
			// index 0: clip num
			builder.append(clip_num.toString() + ",");
			// index 1: action number
			builder.append(action_id.toString() + ",");

			// index 2: action type (HELP, RIGHT, WRONG, BUG)
			if (action instanceof HelpRequest) {
				builder.append("HELP,");
			}
			else {
				StepAttempt attempt = (StepAttempt) action;
				Assessment assessment = attempt.getAssessment();
				builder.append(assessment + ",");
			}

			// index 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22: interpretations
			// if there are both
			if (interpretationBeforeAction.containsKey(action) && interpretationAfterAction.containsKey(action)) {
				List<Integer> interpListBefore = new ArrayList<Integer>(Collections.nCopies(20, 0));
				for (InterpretationElement element : interpretationBeforeAction.get(action)) {
					element.flipBit(element);
					interpListBefore = joinLists(interpListBefore, element.all);
				}

				List<Integer> interpListAfter = new ArrayList<Integer>(Collections.nCopies(20, 0));
				for (InterpretationElement element : interpretationAfterAction.get(action)) {
					element.flipBit(element);
					interpListAfter = joinLists(interpListAfter, element.all);
				}

				interpListAfter = joinLists(interpListAfter,interpListBefore);
				builder.append(listToString(interpListAfter));
			}
			// before only
			else if (interpretationBeforeAction.containsKey(action)) {
				List<Integer> interpListBefore = new ArrayList<Integer>(Collections.nCopies(20, 0));
				for (InterpretationElement element : interpretationBeforeAction.get(action)) {
					element.flipBit(element);
					interpListBefore = joinLists(interpListBefore, element.all);
				}
				builder.append(listToString(interpListBefore));
			}
			// after only
			else if (interpretationAfterAction.containsKey(action)) {
				List<Integer> interpListAfter = new ArrayList<Integer>(Collections.nCopies(20, 0));
				for (InterpretationElement element : interpretationAfterAction.get(action)) {
					element.flipBit(element);
					interpListAfter = joinLists(interpListAfter, element.all);
				}
				builder.append(listToString(interpListAfter));
			}
			else {
				builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0," ); //no interpretations
			}

			// index 23: time
			builder.append(action.getTime());

			builder.append("\n");
			action_id = action_id + 1;
		}
		// builder.append("\n");
		return builder.toString();
	}
	


	public String getInterpretationString() {
		
		StringBuilder builder = new StringBuilder();
		int actionNumber = 1;
		
		if (diagnostic == Behavior.BAD_CLIP) {
			builder.append("Bad Clip!\r\n\r\n\r\n");
		}
		
		for (UserAction action : clip.getActions()) {
			
			if (actionNumber != 1) {
			    builder.append("\r\n============================= New action =============================\r\n\r\n");
			}
			
			if (interpretationBeforeAction.containsKey(action)) {
				for (InterpretationElement element : interpretationBeforeAction.get(action)) {
					builder.append(element.toString());
					builder.append("\r\n");
				}
				builder.append("\r\n");
			}
			
			builder.append(actionNumber + " - ");
			builder.append(action.toString());
			builder.append("\r\n");
			builder.append("\r\n");
			
			if (interpretationAfterAction.containsKey(action)) {
				for (InterpretationElement element : interpretationAfterAction.get(action)) {
					builder.append(element.toString());
					builder.append("\r\n");
				}
			}
			
			actionNumber++;
		}
		
		builder.append("\r\n\r\n\r\n");
		if (diagnostic == Behavior.GAMING) {
			builder.append("Gaming");
		} else if (diagnostic == Behavior.NOT_GAMING) {
			builder.append("Not gaming");
		} else {
			builder.append("bad clip");
		}
		
		return builder.toString();
	}

	public boolean hasInterpretation(UserAction action, Class<?> interpretationType) {
		List<InterpretationElement> elements = new ArrayList<InterpretationElement>();
		if (interpretationBeforeAction.containsKey(action))
			elements.addAll(interpretationBeforeAction.get(action));
		
		if (interpretationAfterAction.containsKey(action))
			elements.addAll(interpretationAfterAction.get(action));
		
		for (InterpretationElement element : elements) {
			if (element.getClass().equals(interpretationType))
				return true;
		}
		
		return false;
	}
	
	public void addPatternCount(GamingPattern pattern) {
		int previousCount = patternCounts.get(pattern);
		patternCounts.put(pattern, previousCount + 1);
	}

	public static String getClipFileHeader() {
		// TODO refactor the intepretation elments string to use an enum instead?
		String interpretationElementsHeader = "DidNotThinkBeforeHelpRequest,ThoughtBeforeHelpRequest,ReadingHelpMessage,ScanningHelpMessage,SearchingForBottomOutHint,ThoughtBeforeStepAttempt," +
		           "GuessingStep,ThoughtAboutDuringLastStep,ThoughtAboutStepButFlawInProcedure,GuessingStepWithValuesFromProblem,DidNotReadErrorMessage,ReadErrorMessage," +
				   "ThoughtAboutError,SwitchedContextBeforeRight,RepeatedStep,SameAnswerDifferentContext,SameAnswerSameContextDifferentAction,SimilarAnswerInputs,NotRepeatedStep,DidNotSwitchContext"; 
		
		StringBuilder patternHeader = new StringBuilder();
		for (GamingPattern pattern : GamingPattern.values()) {
			patternHeader.append(",");
			patternHeader.append(pattern.toString());
		}
		
		return interpretationElementsHeader + patternHeader; 
	}
	
	private int didNotThinkBeforeHelpRequestCount = 0;
	private int thoughtBeforeHelpRequestCount = 0;
	private int readingHelpMessageCount = 0;
	private int scanningHelpMessageCount = 0;
	private int searchingForBottomOutHintCount = 0;
	private int thoughtBeforeStepAttemptCount = 0;
	private int guessingStepCount = 0;
	private int thoughtAboutDuringLastStepCount = 0;
	private int thoughtAboutStepButFlawInProcedureCount = 0;
	private int guessingStepWithValuesFromProblemCount = 0;
	private int didNotReadErrorMessageCount = 0;
	private int readErrorMessageCount = 0;
	private int thoughtAboutErrorCount = 0;
	private int switchedContextBeforeRightCount = 0;
	private int repeatedStepCount = 0;
	private int sameAnswerDifferentContextCount = 0;
	private int sameAnswerSameContextDifferentActionCount = 0;
	private int similarAnswerInputsCount = 0;
	private int notRepeatedStepCount = 0;
	private int didNotSwitchContextCount = 0;
	
	private void addToInterpretationsCount(InterpretationElement element) {
		if (element instanceof DidNotThinkBeforeHelpRequest) {
			didNotThinkBeforeHelpRequestCount++;
		} else if (element instanceof ThoughtBeforeHelpRequest) {
			thoughtBeforeHelpRequestCount++;
		} else if (element instanceof ReadingHelpMessage) {
			readingHelpMessageCount++;
		} else if (element instanceof ScanningHelpMessage) {
			scanningHelpMessageCount++;
		} else if (element instanceof SearchingForBottomOutHint) {
			searchingForBottomOutHintCount++;
		} else if (element instanceof ThoughtBeforeStepAttempt) {
			thoughtBeforeStepAttemptCount++;
		} else if (element instanceof GuessingStep) {
			guessingStepCount++;
		} else if (element instanceof ThoughtAboutDuringLastStep) {
			thoughtAboutDuringLastStepCount++;
		} else if (element instanceof ThoughtAboutStepButFlawInProcedure) {
			thoughtAboutStepButFlawInProcedureCount++;
		} else if (element instanceof GuessingStepWithValuesFromProblem) {
			guessingStepWithValuesFromProblemCount++;
		} else if (element instanceof DidNotReadErrorMessage) {
			didNotReadErrorMessageCount++;
		} else if (element instanceof ReadErrorMessage) {
			readErrorMessageCount++;
		} else if (element instanceof ThoughtAboutError) {
			thoughtAboutErrorCount++;
		} else if (element instanceof SwitchedContextBeforeRight) {
			switchedContextBeforeRightCount++;
		} else if (element instanceof RepeatedStep) {
			repeatedStepCount++;
		} else if (element instanceof SameAnswerDifferentContext) {
			sameAnswerDifferentContextCount++;
		} else if (element instanceof SameAnswerSameContextDifferentAction) {
			sameAnswerSameContextDifferentActionCount++;
		} else if (element instanceof SimilarAnswerInputs) {
			similarAnswerInputsCount++;
		} else if (element instanceof NotRepeatedStep) {
			notRepeatedStepCount++;
		} else if (element instanceof DidNotSwitchContext) {
			didNotSwitchContextCount++;
		}
	}
	
	public String getClipFileText(boolean useRatios) {
		
		String interpretationElementsString;
		
		int totalActions = clip.getActions().size();
		
		if (!useRatios) {
			interpretationElementsString = didNotThinkBeforeHelpRequestCount + "," + thoughtBeforeHelpRequestCount + "," + readingHelpMessageCount + "," + scanningHelpMessageCount + "," +
					searchingForBottomOutHintCount + "," + thoughtBeforeStepAttemptCount + "," + guessingStepCount + "," + thoughtAboutDuringLastStepCount + "," +
					thoughtAboutStepButFlawInProcedureCount + "," + guessingStepWithValuesFromProblemCount + "," + didNotReadErrorMessageCount + "," +
					readErrorMessageCount + "," + thoughtAboutErrorCount + "," + switchedContextBeforeRightCount + "," + repeatedStepCount + "," +
					sameAnswerDifferentContextCount + "," + sameAnswerSameContextDifferentActionCount + "," + similarAnswerInputsCount + "," + notRepeatedStepCount + "," + didNotSwitchContextCount;
		} else {
			interpretationElementsString = ((float) didNotThinkBeforeHelpRequestCount / (float) totalActions) + "," + 
										   ((float) thoughtBeforeHelpRequestCount / (float) totalActions) + "," + 
										   ((float) readingHelpMessageCount / (float) totalActions) + "," + 
										   ((float) scanningHelpMessageCount / (float) totalActions) + "," +
										   ((float) searchingForBottomOutHintCount / (float) totalActions) + "," + 
										   ((float) thoughtBeforeStepAttemptCount / (float) totalActions) + "," + 
										   ((float) guessingStepCount / (float) totalActions) + "," + 
										   ((float) thoughtAboutDuringLastStepCount / (float) totalActions) + "," +
										   ((float) thoughtAboutStepButFlawInProcedureCount / (float) totalActions) + "," + 
										   ((float) guessingStepWithValuesFromProblemCount / (float) totalActions) + "," + 
										   ((float) didNotReadErrorMessageCount / (float) totalActions) + "," +
										   ((float) readErrorMessageCount / (float) totalActions) + "," + 
										   ((float) thoughtAboutErrorCount / (float) totalActions) + "," + 
										   ((float) switchedContextBeforeRightCount / (float) totalActions) + "," + 
										   ((float) repeatedStepCount / (float) totalActions) + "," +
										   ((float) sameAnswerDifferentContextCount / (float) totalActions) + "," + 
										   ((float) sameAnswerSameContextDifferentActionCount / (float) totalActions) + "," + 
										   ((float) similarAnswerInputsCount / (float) totalActions) + "," +
										   ((float) notRepeatedStepCount / (float) totalActions) + "," +
										   ((float) didNotSwitchContextCount / (float) totalActions);
		}
		
		StringBuilder patternString = new StringBuilder();
		for (GamingPattern pattern : GamingPattern.values()) {
			patternString.append(",");
			//patternString.append(patternCounts.get(pattern));
			patternString.append(patternCounts.get(pattern) > 0 ? 1 : 0);
		}
		
		return interpretationElementsString + patternString;
	}
}

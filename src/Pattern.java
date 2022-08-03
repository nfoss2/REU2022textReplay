import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Pattern {
	
	private List<ActionType> actionTypes;
	private List<Set<Class<?>>> interpretations;
	
	private Set<Clip> gamingClips;
	private Set<Clip> nonGamingClips;
	
	private double kappa;
	
	public Pattern() {
		actionTypes = new ArrayList<>();
		interpretations = new ArrayList<>();
		
		gamingClips = new HashSet<Clip>();
		nonGamingClips = new HashSet<Clip>();
	}
	
	public void addAction(ActionType type) {
		addAction(type, new HashSet<Class<?>>());
	}
	
	public void addAction(ActionType type, Class<?> firstConstitutant) {
		Set<Class<?>> constituants = new HashSet<>();
		constituants.add(firstConstitutant);
		addAction(type, constituants);
	}
	
	public void addAction(ActionType type, Class<?> firstConstituant, Class<?> secondConstituant) {
		Set<Class<?>> constituants = new HashSet<>();
		constituants.add(firstConstituant);
		constituants.add(secondConstituant);
		addAction(type, constituants);
	}
	
	private void addAction(ActionType type, Set<Class<?>> interpretation) {
		actionTypes.add(type);
		interpretations.add(interpretation);
	}
	
	public int getNumberOfAction() {
		return actionTypes.size();
	}
	
	public void addGamingClip(Clip clip) {
		gamingClips.add(clip);
	}
	
	public void addNonGamingClip(Clip clip) {
		nonGamingClips.add(clip);
	}
	
	public boolean matches(BehaviorInterpretation interpretation, List<UserAction> actions) {
		for (int i = 0; i < actions.size(); i++) {
			
			if (actionTypes.get(i) == ActionType.helpRequest) {
				if (!(actions.get(i) instanceof HelpRequest)) {
					return false;
				}
			} else {
				if (!(actions.get(i) instanceof StepAttempt))
					return false;
				
				StepAttempt attempt = (StepAttempt) actions.get(i);
				
				if (actionTypes.get(i) == ActionType.incorrectStepAttempt && attempt.getAssessment() == Assessment.RIGHT)
					return false;
				if (actionTypes.get(i) == ActionType.bugStepAttempt && attempt.getAssessment() != Assessment.BUG)
					return false;
			}
			
			for (Class<?> interpretationElement : interpretations.get(i)) {
				if (!interpretation.hasInterpretation(actions.get(i), interpretationElement))
					return false;
			}
		}
		
		return true;
	}
	
	public static List<Pattern> generatePossiblePatterns() {
		List<Pattern> possiblePatterns = new ArrayList<Pattern>();
		
		generatePossibleInterpretationCombinations();
		
		// Generate patterns with 2 actions
		for (ActionType firstActionType : ActionType.values()) {
			for (ActionType secondActionType : ActionType.values()) {
				if (firstActionType == ActionType.helpRequest && secondActionType == ActionType.helpRequest) {
					break;
				}
				
				List<Set<Class<?>>> firstActionPossibleInterpretations = getPossibleInterpretations(firstActionType);
				List<Set<Class<?>>> secondActionPossibleInterpretations = getPossibleInterpretations(secondActionType);
				
				for (Set<Class<?>> firstInterpretations : firstActionPossibleInterpretations) {
					for (Set<Class<?>> secondInterpretations : secondActionPossibleInterpretations) {
						Pattern pattern = new Pattern();
						pattern.addAction(firstActionType, firstInterpretations);
						pattern.addAction(secondActionType, secondInterpretations);
						possiblePatterns.add(pattern);
					}
				}
			}
		}
		
		// Generate patterns with 3 actions
		for (ActionType firstActionType : ActionType.values()) {
			for (ActionType secondActionType : ActionType.values()) {
				for (ActionType thirdActionType : ActionType.values()) {
					if (firstActionType == ActionType.helpRequest && secondActionType == ActionType.helpRequest) {
						break;
					}
					if (secondActionType == ActionType.helpRequest && thirdActionType == ActionType.helpRequest) {
						break;
					}
					
					List<Set<Class<?>>> firstActionPossibleInterpretations = getPossibleInterpretations(firstActionType);
					List<Set<Class<?>>> secondActionPossibleInterpretations = getPossibleInterpretations(secondActionType);
					List<Set<Class<?>>> thirdActionPossibleInterpretations = getPossibleInterpretations(thirdActionType);
					
					for (Set<Class<?>> firstInterpretations : firstActionPossibleInterpretations) {
						for (Set<Class<?>> secondInterpretations : secondActionPossibleInterpretations) {
							for (Set<Class<?>> thirdInterpretations : thirdActionPossibleInterpretations) {
								Pattern pattern = new Pattern();
								pattern.addAction(firstActionType, firstInterpretations);
								pattern.addAction(secondActionType, secondInterpretations);
								pattern.addAction(thirdActionType, thirdInterpretations);
								possiblePatterns.add(pattern);
							}
						}
					}
				}
			}
		}
		
		// TODO: Generate patterns with 4 actions? Probably too many such patterns
		
		return possiblePatterns;
	}
	
	private static List<Set<Class<?>>> getPossibleInterpretations(ActionType actionType) {
		if (actionType == ActionType.helpRequest) {
			return helpRequestInterpretationCombinations;
		} else if (actionType == ActionType.stepAttempt) {
			return stepInterpretationCombinations;
		//} else if (actionType == ActionType.rightStepAttempt) {
		//	return stepInterpretationCombinations;
		//} else if (actionType == ActionType.wrongStepAttempt) {
		//	return stepInterpretationCombinations;
		} else if (actionType == ActionType.incorrectStepAttempt) {
			return stepInterpretationCombinations;
		} else if (actionType == ActionType.bugStepAttempt) {
			return bugStepInterpretationCombinations;
		}
		
		
		return null;
	}

	private static List<Set<Class<?>>> helpRequestInterpretationCombinations;
	private static List<Set<Class<?>>> stepInterpretationCombinations;
	//private static List<Set<Class<?>>> incorrectStepInterpretationCombinations;
	private static List<Set<Class<?>>> bugStepInterpretationCombinations;
	
	private static void generatePossibleInterpretationCombinations() {
		
		// TODO: remove from combination interpretation elements that are not linked to gaming (ie reading error messages, thinking about a step, etc)?
		
		generatePossibleHelpRequestInterpretationCombinations();
		generatePossibleStepInterpretationCombinations();
		//generatePossibleIncorrectStepInterpretationCombinations();
		generatePossibleBugStepInterpretationCombinations();
	}

	private static void generatePossibleHelpRequestInterpretationCombinations() {
		helpRequestInterpretationCombinations = new ArrayList<Set<Class<?>>>();
		
		helpRequestInterpretationCombinations.add(new HashSet<Class<?>>());
		for (HelpRequestNegativeInterpretation helpInterpretation : HelpRequestNegativeInterpretation.values()) {
			Set<Class<?>> newSet = new HashSet<Class<?>>();
			newSet.add(HelpRequestNegativeInterpretation.getValue(helpInterpretation));
			helpRequestInterpretationCombinations.add(newSet);
		}
		
		for (HelpRequestNegativeInterpretation thinkingInterpretation : HelpRequestNegativeInterpretation.getThinkingInterpretations()) {
			Set<Class<?>> newSet1 = new HashSet<Class<?>>();
			
			newSet1.add(HelpRequestNegativeInterpretation.getValue(thinkingInterpretation));
			newSet1.add(HelpRequestNegativeInterpretation.getValue(HelpRequestNegativeInterpretation.switchedContextBeforeRight));
			
			helpRequestInterpretationCombinations.add(newSet1);
			
			Set<Class<?>> newSet2 = new HashSet<Class<?>>();
			
			newSet2.add(HelpRequestNegativeInterpretation.getValue(thinkingInterpretation));
			newSet2.add(HelpRequestNegativeInterpretation.getValue(HelpRequestNegativeInterpretation.didNotSwitchContext));
			
			helpRequestInterpretationCombinations.add(newSet2);
			
			for (HelpRequestNegativeInterpretation readingInterpretation : HelpRequestNegativeInterpretation.getReadingInterpretations()) {
				Set<Class<?>> newSet3 = new HashSet<Class<?>>();
				newSet3.add(HelpRequestNegativeInterpretation.getValue(thinkingInterpretation));
				newSet3.add(HelpRequestNegativeInterpretation.getValue(readingInterpretation));
				helpRequestInterpretationCombinations.add(newSet2);
				
				// TODO : here we could add a set with 3 interpretations (including switchedContextBeforeRight)
			}
		}
		
		for (HelpRequestNegativeInterpretation readingInterpretation : HelpRequestNegativeInterpretation.getReadingInterpretations()) {
			Set<Class<?>> newSet = new HashSet<Class<?>>();
			newSet.add(HelpRequestNegativeInterpretation.getValue(HelpRequestNegativeInterpretation.switchedContextBeforeRight));
			newSet.add(HelpRequestNegativeInterpretation.getValue(readingInterpretation));
			helpRequestInterpretationCombinations.add(newSet);
		}
	}
	
	private static void generatePossibleStepInterpretationCombinations() {
		stepInterpretationCombinations = new ArrayList<Set<Class<?>>>();
		
		stepInterpretationCombinations.add(new HashSet<Class<?>>());
		for (StepNegativeInterpretation stepInterpretation : StepNegativeInterpretation.values()) {
			Set<Class<?>> newSet = new HashSet<Class<?>>();
			newSet.add(StepNegativeInterpretation.getValue(stepInterpretation));
			stepInterpretationCombinations.add(newSet);
		}
		
		for (StepNegativeInterpretation thinkingInterpretation : StepNegativeInterpretation.getThinkingInterpretations()) {
					
			for (StepNegativeInterpretation answerInterpretation : StepNegativeInterpretation.getAnswerInterpretations()) {
				Set<Class<?>> newSet = new HashSet<Class<?>>();
				newSet.add(StepNegativeInterpretation.getValue(thinkingInterpretation));
				newSet.add(StepNegativeInterpretation.getValue(answerInterpretation));
				stepInterpretationCombinations.add(newSet);
			}
		}
	}
	
//	private static void generatePossibleIncorrectStepInterpretationCombinations() {
//		incorrectStepInterpretationCombinations = new ArrayList<Set<Class<?>>>();
//		
//		incorrectStepInterpretationCombinations.add(new HashSet<Class<?>>());
//		for (StepNegativeInterpretation stepInterpretation : StepNegativeInterpretation.values()) {
//			Set<Class<?>> newSet1 = new HashSet<Class<?>>();
//			newSet1.add(StepNegativeInterpretation.getValue(stepInterpretation));
//			incorrectStepInterpretationCombinations.add(newSet1);
//			
//			Set<Class<?>> newSet2 = new HashSet<Class<?>>();
//			newSet2.add(StepNegativeInterpretation.getValue(stepInterpretation));
//			newSet2.add(ThoughtAboutError.class);
//			incorrectStepInterpretationCombinations.add(newSet2);
//		}
//		
//		Set<Class<?>> thoughtAboutErrorSet = new HashSet<>();
//		thoughtAboutErrorSet.add(ThoughtAboutError.class);
//		incorrectStepInterpretationCombinations.add(thoughtAboutErrorSet);
//		
//		for (StepNegativeInterpretation thinkingInterpretation : StepNegativeInterpretation.getThinkingInterpretations()) {
//					
//			for (StepNegativeInterpretation answerInterpretation : StepNegativeInterpretation.getAnswerInterpretations()) {
//				Set<Class<?>> newSet = new HashSet<Class<?>>();
//				newSet.add(StepNegativeInterpretation.getValue(thinkingInterpretation));
//				newSet.add(StepNegativeInterpretation.getValue(answerInterpretation));
//				incorrectStepInterpretationCombinations.add(newSet);
//			}
//		}
//	}
	
	private static void generatePossibleBugStepInterpretationCombinations() {
		bugStepInterpretationCombinations = new ArrayList<Set<Class<?>>>();
		
		bugStepInterpretationCombinations.add(new HashSet<Class<?>>());
		for (BugStepNegativeInterpretation stepInterpretation : BugStepNegativeInterpretation.values()) {
			Set<Class<?>> newSet1 = new HashSet<Class<?>>();
			newSet1.add(BugStepNegativeInterpretation.getValue(stepInterpretation));
			bugStepInterpretationCombinations.add(newSet1);
			
			Set<Class<?>> newSet2 = new HashSet<Class<?>>();
			newSet2.add(BugStepNegativeInterpretation.getValue(stepInterpretation));
			newSet2.add(DidNotReadErrorMessage.class);
			bugStepInterpretationCombinations.add(newSet2);
		}
		
		Set<Class<?>> didNotReadErrorMessageSet = new HashSet<Class<?>>();
		didNotReadErrorMessageSet.add(DidNotReadErrorMessage.class);
		bugStepInterpretationCombinations.add(didNotReadErrorMessageSet);
		
		for (BugStepNegativeInterpretation thinkingInterpretation : BugStepNegativeInterpretation.getThinkingInterpretations()) {
					
			for (BugStepNegativeInterpretation answerInterpretation : BugStepNegativeInterpretation.getAnswerInterpretations()) {
				Set<Class<?>> newSet = new HashSet<Class<?>>();
				newSet.add(BugStepNegativeInterpretation.getValue(thinkingInterpretation));
				newSet.add(BugStepNegativeInterpretation.getValue(answerInterpretation));
				bugStepInterpretationCombinations.add(newSet);
			}
		}
	}
	
	public static List<Pattern> getEDM2014PatternList() {
		
    	List<Pattern> patterns = new ArrayList<>();
    	
    	// Refer to the EDM 2014 pattern for a lists of the pattern
    	
    	// F1
    	Pattern f1 = new Pattern();
    	f1.addAction(ActionType.incorrectStepAttempt);
    	f1.addAction(ActionType.incorrectStepAttempt, SameAnswerDifferentContext.class);
    	patterns.add(f1);
    	
    	// F2
    	Pattern f2 = new Pattern();
    	f2.addAction(ActionType.incorrectStepAttempt, NotRepeatedStep.class);
    	f2.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	f2.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	patterns.add(f2);
		
    	// F3
    	Pattern f3 = new Pattern();
    	f3.addAction(ActionType.bugStepAttempt, DidNotReadErrorMessage.class);
    	f3.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	f3.addAction(ActionType.stepAttempt, NotRepeatedStep.class);
    	patterns.add(f3);
    	
    	// F4
    	Pattern f4 = new Pattern();
    	f4.addAction(ActionType.incorrectStepAttempt);
    	f4.addAction(ActionType.stepAttempt, SameAnswerDifferentContext.class);
    	f4.addAction(ActionType.bugStepAttempt);
    	patterns.add(f4);
    	
    	// F5
    	Pattern f5 = new Pattern();
    	f5.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	f5.addAction(ActionType.stepAttempt, GuessingStep.class, SimilarAnswerInputs.class);
    	f5.addAction(ActionType.helpRequest, DidNotSwitchContext.class, DidNotThinkBeforeHelpRequest.class);
    	patterns.add(f5);
    	
    	// F6
    	Pattern f6 = new Pattern();
    	f6.addAction(ActionType.incorrectStepAttempt);
    	f6.addAction(ActionType.stepAttempt, GuessingStep.class, SimilarAnswerInputs.class);
    	f6.addAction(ActionType.incorrectStepAttempt, SwitchedContextBeforeRight.class);
    	patterns.add(f6);
    	
    	// F7
    	Pattern f7 = new Pattern();
    	f7.addAction(ActionType.bugStepAttempt);
    	f7.addAction(ActionType.bugStepAttempt, GuessingStep.class, NotRepeatedStep.class);
    	f7.addAction(ActionType.stepAttempt, GuessingStep.class, NotRepeatedStep.class);
    	patterns.add(f7);
    	
    	// F8
    	Pattern f8 = new Pattern();
    	f8.addAction(ActionType.helpRequest, DidNotThinkBeforeHelpRequest.class, DidNotSwitchContext.class);
    	f8.addAction(ActionType.stepAttempt);
    	f8.addAction(ActionType.incorrectStepAttempt, GuessingStep.class, SimilarAnswerInputs.class);
    	patterns.add(f8);
    	
    	// F9
    	Pattern f9 = new Pattern();
    	f9.addAction(ActionType.bugStepAttempt);
    	f9.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	f9.addAction(ActionType.bugStepAttempt, NotRepeatedStep.class);
    	patterns.add(f9);
    	
    	// F10
    	Pattern f10 = new Pattern();
    	f10.addAction(ActionType.stepAttempt, GuessingStep.class, DidNotSwitchContext.class);
    	f10.addAction(ActionType.incorrectStepAttempt, DidNotSwitchContext.class);
    	f10.addAction(ActionType.incorrectStepAttempt, GuessingStep.class, SimilarAnswerInputs.class);
    	patterns.add(f10);
    	
    	// F11
    	Pattern f11 = new Pattern();
    	f11.addAction(ActionType.incorrectStepAttempt, GuessingStep.class, DidNotSwitchContext.class);
    	f11.addAction(ActionType.stepAttempt, NotRepeatedStep.class);
    	f11.addAction(ActionType.incorrectStepAttempt, SwitchedContextBeforeRight.class);
    	patterns.add(f11);
    	
    	// F12
    	Pattern f12 = new Pattern();
    	f12.addAction(ActionType.incorrectStepAttempt, GuessingStep.class, SimilarAnswerInputs.class);
    	f12.addAction(ActionType.incorrectStepAttempt, NotRepeatedStep.class);
    	f12.addAction(ActionType.helpRequest, SearchingForBottomOutHint.class);
    	patterns.add(f12);
    	
    	// F13
    	Pattern f13 = new Pattern();
    	f13.addAction(ActionType.incorrectStepAttempt);
    	f13.addAction(ActionType.bugStepAttempt, SimilarAnswerInputs.class);
    	f13.addAction(ActionType.stepAttempt, SameAnswerDifferentContext.class);
    	patterns.add(f13);
    	
    	// F14
    	Pattern f14 = new Pattern();
    	f14.addAction(ActionType.incorrectStepAttempt, GuessingStep.class, NotRepeatedStep.class);
    	f14.addAction(ActionType.bugStepAttempt, GuessingStepWithValuesFromProblem.class);
    	f14.addAction(ActionType.stepAttempt, GuessingStep.class, NotRepeatedStep.class);
    	patterns.add(f14);
    	
    	// F15
    	Pattern f15 = new Pattern();
    	f15.addAction(ActionType.bugStepAttempt, DidNotSwitchContext.class, DidNotReadErrorMessage.class);
    	f15.addAction(ActionType.stepAttempt, NotRepeatedStep.class);
    	f15.addAction(ActionType.incorrectStepAttempt, GuessingStep.class);
    	patterns.add(f15);
    	
    	// F16
    	Pattern f16 = new Pattern();
    	f16.addAction(ActionType.stepAttempt, GuessingStep.class, SameAnswerDifferentContext.class);
    	f16.addAction(ActionType.incorrectStepAttempt);
    	f16.addAction(ActionType.incorrectStepAttempt, NotRepeatedStep.class);
    	patterns.add(f16);
    	
    	// F17
    	Pattern f17 = new Pattern();
    	f17.addAction(ActionType.incorrectStepAttempt);
    	f17.addAction(ActionType.bugStepAttempt, GuessingStep.class, NotRepeatedStep.class);
    	f17.addAction(ActionType.incorrectStepAttempt, NotRepeatedStep.class);
    	patterns.add(f17);
    	
    	// F18
    	Pattern f18 = new Pattern();
    	f18.addAction(ActionType.incorrectStepAttempt, GuessingStep.class, DidNotSwitchContext.class);
    	f18.addAction(ActionType.incorrectStepAttempt, NotRepeatedStep.class);
    	f18.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	patterns.add(f18);
    	
    	// F19
    	Pattern f19 = new Pattern();
    	f19.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	f19.addAction(ActionType.incorrectStepAttempt, DidNotSwitchContext.class);
    	f19.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	patterns.add(f19);
    	
    	// F20
    	Pattern f20 = new Pattern();
    	f20.addAction(ActionType.incorrectStepAttempt, SimilarAnswerInputs.class);
    	f20.addAction(ActionType.incorrectStepAttempt, GuessingStep.class, SimilarAnswerInputs.class);
    	f20.addAction(ActionType.stepAttempt, SimilarAnswerInputs.class);
    	patterns.add(f20);
		
    	assert (patterns.size() == 20);
    	
		return patterns;
	}
	
	public static String getHeader() {
		return "pattern,TP,FP,Kappa";
	}

	public void calculateKappa(int expertGaming, int expertNotGaming) {
		kappa = Kappa.calculateKappa(expertGaming, expertNotGaming, gamingClips.size(), nonGamingClips.size());
	}
	
	public double getKappa() {
		return kappa;
	}
	
	public double getTPFPRatio() {
		if (nonGamingClips.size() == 0) {
			return 0;
		}
		
		return ((float) gamingClips.size()) / ((float) nonGamingClips.size());
	}
	
	public double getPositivePredictiveValue() {
		return ((float) gamingClips.size()) / ((float) nonGamingClips.size() + (float) gamingClips.size());
	}
	
	public int getTP() {
		return gamingClips.size();
	}
	
	public int getFP() {
		return nonGamingClips.size();
	}
	
	public Set<Clip> getAllMatchingClips() {
		Set<Clip> matchingClips = new HashSet<>();
		matchingClips.addAll(gamingClips);
		matchingClips.addAll(nonGamingClips);
		
		return matchingClips;
	}
	
	public void resetPerformance() {
		kappa = 0;
		gamingClips.clear();
		nonGamingClips.clear();
	}
	
	public String toString() {
		StringBuilder patternString = new StringBuilder();
		for (int i = 0; i < actionTypes.size(); i++) {
			for (Class<?> interpretation : interpretations.get(i)) {
				patternString.append(interpretation.toString());
				patternString.append("=>");
			}
			
			patternString.append(actionTypes.get(i).toString());
			patternString.append("=>");
		}
		
		return patternString.toString();
	}
	
	public String getString() {
	
		return toString() + "," + gamingClips.size() + "," + nonGamingClips.size() + "," + kappa;
	}
}

enum ActionType {
	// TODO: keep only stepAttempt, incorrectStepAttempt, bugStepAttempt and helpRequest?
	stepAttempt,
	//rightStepAttempt,
	//wrongStepAttempt,
	incorrectStepAttempt,
	bugStepAttempt,
	helpRequest;
}

enum HelpRequestNegativeInterpretation {
	didNotThinkBeforeHelpRequest,
	//thoughtBeforeHelpRequest,
	///readingHelpMessage,
	scanningHelpMessage,
	searchingForBottomOutHint,
	switchedContextBeforeRight,
	didNotSwitchContext;
	
	public static Class<?> getValue(HelpRequestNegativeInterpretation type) {
		if (type == didNotThinkBeforeHelpRequest) {
			return DidNotThinkBeforeHelpRequest.class;
		//} else if (type == thoughtBeforeHelpRequest) {
		//	return ThoughtBeforeHelpRequest.class;
		//} else if (type == readingHelpMessage) {
		//	return ReadingHelpMessage.class;
		} else if (type == scanningHelpMessage) {
			return ScanningHelpMessage.class;
		} else if (type == searchingForBottomOutHint) {
			return SearchingForBottomOutHint.class;
		} else if (type == switchedContextBeforeRight) {
			return SwitchedContextBeforeRight.class;
		} else if (type == didNotSwitchContext) {
			return DidNotSwitchContext.class;
		}
		
		throw new IllegalArgumentException();
	}
	
	public static Set<HelpRequestNegativeInterpretation> getThinkingInterpretations() {
		Set<HelpRequestNegativeInterpretation> set = new HashSet<>();
		set.add(didNotThinkBeforeHelpRequest);
		//set.add(thoughtBeforeHelpRequest);
		return set;
	}
	
	public static Set<HelpRequestNegativeInterpretation> getReadingInterpretations() {
		Set<HelpRequestNegativeInterpretation> set = new HashSet<>();
		//set.add(readingHelpMessage);
		set.add(scanningHelpMessage);
		set.add(searchingForBottomOutHint);
		return set;
	}
}

enum BugStepNegativeInterpretation {
	//thoughtAboutStepButFlawInProcedure,
	guessingStepWithValuesFromProblem,
	didNotReadErrorMessage,
	//readErrorMessage,
	//thoughtBeforeStepAttempt,
	guessingStep,
	//thoughtAboutDuringLastStep,
	switchedContextBeforeRight,
	didNotSwitchContext,
	repeatedStep,
	notRepeatedStep,
	sameAnswerDifferentContext,
	sameAnswerSameContextDifferentAction,
	similarAnswerInputs;
		
	public static Class<?> getValue(BugStepNegativeInterpretation type) {
		//if (type == thoughtBeforeStepAttempt) {
		//	return ThoughtBeforeStepAttempt.class;
		if (type == guessingStep) {
			return GuessingStep.class;
		//} else if (type == thoughtAboutDuringLastStep) {
		//	return ThoughtAboutDuringLastStep.class;
		} else if (type == switchedContextBeforeRight) {
			return SwitchedContextBeforeRight.class;
		} else if (type == didNotSwitchContext) {
			return DidNotSwitchContext.class;
		} else if (type == repeatedStep) {
			return RepeatedStep.class;
		} else if (type == notRepeatedStep) {
			return NotRepeatedStep.class;
		} else if (type == sameAnswerDifferentContext) {
			return SameAnswerDifferentContext.class;
		} else if (type == sameAnswerSameContextDifferentAction) {
			return SameAnswerSameContextDifferentAction.class;
		} else if (type == similarAnswerInputs) {
			return SimilarAnswerInputs.class;
		} else if (type == didNotReadErrorMessage) {
			return DidNotReadErrorMessage.class;
		} else if (type == guessingStepWithValuesFromProblem) {
			return GuessingStepWithValuesFromProblem.class;
		}
			
		throw new IllegalArgumentException();
	}
		
	public static Set<BugStepNegativeInterpretation> getThinkingInterpretations() {
		Set<BugStepNegativeInterpretation> set = new HashSet<>();
		//set.add(thoughtBeforeStepAttempt);
		set.add(guessingStep);
		set.add(guessingStepWithValuesFromProblem);
		//set.add(thoughtAboutDuringLastStep);
		return set;
	}
	
	public static Set<BugStepNegativeInterpretation> getAnswerInterpretations() {
		Set<BugStepNegativeInterpretation> set = new HashSet<>();
		set.add(switchedContextBeforeRight);
		set.add(notRepeatedStep);
		set.add(repeatedStep);
		set.add(didNotSwitchContext);
		set.add(sameAnswerDifferentContext);
		set.add(sameAnswerSameContextDifferentAction);
		set.add(similarAnswerInputs);
		return set;
	}
}

enum StepNegativeInterpretation {
	//thoughtBeforeStepAttempt,
	guessingStep,
	//thoughtAboutDuringLastStep,
	switchedContextBeforeRight,
	didNotSwitchContext,
	repeatedStep,
	notRepeatedStep,
	sameAnswerDifferentContext,
	sameAnswerSameContextDifferentAction,
	similarAnswerInputs;
	
	public static Class<?> getValue(StepNegativeInterpretation type) {
		//if (type == thoughtBeforeStepAttempt) {
		//	return ThoughtBeforeStepAttempt.class;
		if (type == guessingStep) {
			return GuessingStep.class;
		//} else if (type == thoughtAboutDuringLastStep) {
		//	return ThoughtAboutDuringLastStep.class;
		} else if (type == switchedContextBeforeRight) {
			return SwitchedContextBeforeRight.class;
		} else if (type == didNotSwitchContext) {
			return DidNotSwitchContext.class;
		} else if (type == repeatedStep) {
			return RepeatedStep.class;
		} else if (type == notRepeatedStep) {
			return NotRepeatedStep.class;
		} else if (type == sameAnswerDifferentContext) {
			return SameAnswerDifferentContext.class;
		} else if (type == sameAnswerSameContextDifferentAction) {
			return SameAnswerSameContextDifferentAction.class;
		} else if (type == similarAnswerInputs) {
			return SimilarAnswerInputs.class;
		}
		
		throw new IllegalArgumentException();
	}
	
	public static Set<StepNegativeInterpretation> getThinkingInterpretations() {
		Set<StepNegativeInterpretation> set = new HashSet<>();
		//set.add(thoughtBeforeStepAttempt);
		set.add(guessingStep);
		//set.add(thoughtAboutDuringLastStep);
		return set;
	}
	
	public static Set<StepNegativeInterpretation> getAnswerInterpretations() {
		Set<StepNegativeInterpretation> set = new HashSet<>();
		set.add(switchedContextBeforeRight);
		set.add(didNotSwitchContext);
		set.add(repeatedStep);
		set.add(notRepeatedStep);
		set.add(sameAnswerDifferentContext);
		set.add(sameAnswerSameContextDifferentAction);
		set.add(similarAnswerInputs);
		return set;
	}
}
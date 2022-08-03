import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.MediaSize.Other;


public class CognitiveModel {
	
	private ENAWriter enaWriter = null;
	
	public BehaviorInterpretation evaluateBehavior(Clip clip) throws IOException {
		
		//if (isBadClip(clip)) {
		//	BehaviorInterpretation badClip = new BehaviorInterpretation(clip);
		//	badClip.setDiagnostic(Behavior.BAD_CLIP);
		//	return badClip;
		//}
		
		// First loop over the actions to generate interpretations for the pauses
		BehaviorInterpretation interpretation = generateInterpretation(clip); 
		
		//dont need
		generateDiagnosis(interpretation);
		
		return interpretation;
	}
	
	public void setENAWriter(ENAWriter writer) {
		
		enaWriter = writer;
	}
	
	private List<Integer> getNewConstituentVector() {
		
		List<Integer> vector = new ArrayList<>();
		
		for (InterpretationConstituent constituent : InterpretationConstituent.values()) {
			vector.add(0);
		}
		
		return vector;
	}
	
	private BehaviorInterpretation generateInterpretation(Clip clip) throws IOException {
		BehaviorInterpretation interpretation = new BehaviorInterpretation(clip);
		
		List<UserAction> actions = clip.getActions();
		//Map<String, List<String>> previousStepAttempts = new HashMap<String, List<String>>();
		
		for (int i = 0; i < actions.size(); i++) {
			
			List<Integer> constituentVector = getNewConstituentVector();
			
			UserAction currentAction = actions.get(i);
			String currentCell = currentAction.getCell();
			
			// If this is not the first action, check to see whether the cell changed. 
			// If the cell changed and the previous action was not a correct step, than the student possibly "abandonned the step"
			if (i > 0) {
				UserAction previousAction = actions.get(i - 1);
				if (!currentCell.equals(previousAction.getCell())) {
					
					if (!(previousAction instanceof StepAttempt) || ((StepAttempt) previousAction).getAssessment() != Assessment.RIGHT) {
						interpretation.addInterpretationBefore(currentAction, new SwitchedContextBeforeRight());
						
						constituentVector.set(InterpretationConstituent.getIndex(InterpretationConstituent.switchedContextBeforeRight) , 1);
					}
				} else {
					interpretation.addInterpretationBefore(currentAction, new DidNotSwitchContext());
					constituentVector.set(InterpretationConstituent.getIndex(InterpretationConstituent.didNotSwitchContext) , 1);
				}
			}
			
			if (currentAction instanceof HelpRequest) {
				
				HelpRequest helpRequest = (HelpRequest) currentAction;
				
				if (i > 0) {
					InterpretationElement interpretationBeforeHelp = getInterpretationBeforeHelpRequest(helpRequest);
					interpretation.addInterpretationBefore(helpRequest, interpretationBeforeHelp);
					
					constituentVector.set(InterpretationConstituent.getIndex(interpretationBeforeHelp) , 1);
				}
				
				// if it's not the last action, check whether the student spent enough time to read the hints
				if (i < actions.size() - 1) {
					
					InterpretationElement interpretationAfterHelp = getInterpretationAfterHelpRequest(helpRequest, actions.get(i + 1));
				    interpretation.addInterpretationAfter(helpRequest, interpretationAfterHelp);
				    
				    constituentVector.set(InterpretationConstituent.getIndex(interpretationAfterHelp) , 1);
				}
			} else if (currentAction instanceof StepAttempt) {
				
				StepAttempt stepAttempt = (StepAttempt) currentAction;
				
				// If it's not the first action of the clip, see if there is enough time before the action to think about the step
				if (i > 0) {
					UserAction previousAction = actions.get(i - 1);
					if (interpretation.hasInterpretation(previousAction, SearchingForBottomOutHint.class)) {
						// If the student is searching for a bottom out hint, then he/she is automatically guessing the next step
						interpretation.addInterpretationBefore(stepAttempt, new GuessingStep());
						constituentVector.set(InterpretationConstituent.getIndex(InterpretationConstituent.guessingStep) , 1);
					} else {
						InterpretationElement interpretationBeforeStepAttempt = getInterpretationBeforeStepAttempt(stepAttempt, previousAction, i == 1);
						interpretation.addInterpretationBefore(stepAttempt, interpretationBeforeStepAttempt);
						
						constituentVector.set(InterpretationConstituent.getIndex(interpretationBeforeStepAttempt) , 1);
					}
				}
				
				if (i > 0) {
					UserAction previousAction = actions.get(i - 1);
					InterpretationElement element = getSimilarityInterpretation(stepAttempt, previousAction);
				    interpretation.addInterpretationBefore(stepAttempt, element);
				    
				    if (element != null) {
				    	constituentVector.set(InterpretationConstituent.getIndex(element) , 1);
				    }
				    
				    if (!(element instanceof RepeatedStep)) {
				    	interpretation.addInterpretationBefore(stepAttempt, new NotRepeatedStep());
				    	constituentVector.set(InterpretationConstituent.getIndex(InterpretationConstituent.notRepeatedStep) , 1);
				    }
				}
				
				Assessment assessment = stepAttempt.getAssessment();
				if (assessment == Assessment.BUG) {
					if (i > 0) {
						InterpretationElement interpretationBeforeBug = getInterpretationBeforeBug(stepAttempt);
					    interpretation.addInterpretationBefore(stepAttempt, interpretationBeforeBug);
					    
					    constituentVector.set(InterpretationConstituent.getIndex(interpretationBeforeBug) , 1);
					}
					
					if (i < actions.size() - 1) {
						InterpretationElement interpretationAfterBug = getInterpretationAfterBug(stepAttempt, actions.get(i + 1));
						interpretation.addInterpretationAfter(stepAttempt, interpretationAfterBug);
						
						constituentVector.set(InterpretationConstituent.getIndex(interpretationAfterBug) , 1);
					}
				} 
				if (assessment != Assessment.RIGHT && i < actions.size() - 1) {
					
					// TODO how to handle the "thinking about the error" after a bug (since there is already a "reading the error message")?
					InterpretationElement interpretationAfterStepAttempt = getInterpretationAfterStepAttempt(stepAttempt, actions.get(i + 1));
					interpretation.addInterpretationAfter(stepAttempt, interpretationAfterStepAttempt);
					
					if (interpretationAfterStepAttempt != null) {
						constituentVector.set(InterpretationConstituent.getIndex(interpretationAfterStepAttempt) , 1);
					}
				}
				
				//if (!previousStepAttempts.containsKey(stepAttempt.getAnswer())) {
				//	previousStepAttempts.put(stepAttempt.getAnswer(), new ArrayList<String>());
				//}
				//previousStepAttempts.get(stepAttempt.getAnswer()).add(stepAttempt.getCell());
			} else {
				throw new IllegalArgumentException();
			}
			
			if (enaWriter != null) {
				enaWriter.writeNewLine(constituentVector);
			}
		}
		
		return interpretation;
	}

	private InterpretationElement getInterpretationBeforeHelpRequest(HelpRequest currentAction) {
		// TODO time thresholds are abitrary right now 
		// TODO should we check to see whether it's the same cell as the previous attempt? (ie could have thought before the attempt)
		//      or should this check be made later when trying to diagnose the behavior?
		if (currentAction.getTime() < 5) {
			return new DidNotThinkBeforeHelpRequest();
		} else {
			return new ThoughtBeforeHelpRequest();
		}
	}
	
	private InterpretationElement getInterpretationAfterHelpRequest(HelpRequest currentAction, UserAction nextAction) {
		// TODO time thresholds are abitrary right now
		double totalTime = nextAction.getTime();
		double timePerHelpStep = totalTime / currentAction.getNumSteps();
		
		if (timePerHelpStep > 8) {
			return new ReadingHelpMessage();
		} else if (timePerHelpStep > 3) {
			return new ScanningHelpMessage();
		} else {
			return new SearchingForBottomOutHint();
		}
	}
	
	private InterpretationElement getInterpretationBeforeStepAttempt(StepAttempt currentAction, UserAction previousAction, boolean previousActionIsFirstOfClip) {
		// TODO time thresholds are abitrary right now
		if (currentAction.getTime() > 5) {
			return new ThoughtBeforeStepAttempt();
		} else if (!previousActionIsFirstOfClip && previousAction.getTime() > 10 && 
				   (previousAction instanceof StepAttempt && !previousAction.getCell().equals(currentAction.getCell())) &&
				   (previousAction instanceof StepAttempt && ((StepAttempt) previousAction).getAssessment() == Assessment.RIGHT)) {
			// If this action is the first of the clip, the expert can't see the time before that action
			// For now we only consider that the student can think one step ahead if he the previous action was a step attempt in a different cell 
			return new ThoughtAboutDuringLastStep();
		} else {
			return new GuessingStep();
		}
	}
	
	private InterpretationElement getInterpretationBeforeBug(StepAttempt currentAction) {
		// TODO time thresholds are abitrary right now
		if (currentAction.getTime() > 5) {
			return new ThoughtAboutStepButFlawInProcedure();
		} else {
			// TODO : do not use this interpretation if the student could have planned ahead
			return new GuessingStepWithValuesFromProblem();
		}
	}
	
	private InterpretationElement getInterpretationAfterBug(StepAttempt currentAction, UserAction nextAction) {
		// TODO time thresholds are abitrary right now
		double timeAfter = nextAction.getTime();
		if (timeAfter > 8) {
			return new ReadErrorMessage();
		} else {
			return new DidNotReadErrorMessage();
		}
	}
	
	private InterpretationElement getInterpretationAfterStepAttempt(StepAttempt currentAction, UserAction nextAcion) {
		// TODO time thresholds are abitrary right now
		double timeAfter = nextAcion.getTime();
		if (timeAfter > 5) {
			return new ThoughtAboutError();
		} else {
			return null;
		}
	}
	
	private InterpretationElement getSimilarityInterpretation(StepAttempt currentAttempt, UserAction previousAction) {
		if (!(previousAction instanceof StepAttempt))
			return null;
		
		StepAttempt previousAttempt = (StepAttempt) previousAction;
		
		if (previousAttempt.getAnswer().equals(currentAttempt.getAnswer())) {
			if (previousAttempt.getCell().equals(currentAttempt.getCell())) {
				if (previousAttempt.getAction().equals(currentAttempt.getAction())) {
					return new RepeatedStep();
				} else {
					return new SameAnswerSameContextDifferentAction();
				}
			} else {
				return new SameAnswerDifferentContext();
			}
		} else if (LevenshteinDistance.compute(previousAttempt.getAnswer(), currentAttempt.getAnswer()) <= 2) {
			return new SimilarAnswerInputs();
		}
		
		return null;
	}
	
	//private InterpretationElement getSimilarityInterpretation(StepAttempt stepAttempt, Map<String, List<String>> previousStepAttempts) {
	//	String currentAnswer = stepAttempt.getAnswer();
	//	String currentCell = stepAttempt.getCell();
		
	//	// TODO : should this interpretation only be considered for two subsequent steps?
	//	if (previousStepAttempts.containsKey(currentAnswer)) {
	//		if (previousStepAttempts.get(currentAnswer).contains(currentCell)) {
	//			return new RepeatedStep();
	//		} else {
	//			return new SameAnswerDifferentContext();
	//		}
	//	}
		
	//	return null;
	//}
	
	private boolean isBadClip(Clip clip) {
		boolean firstAction = true;
		UserAction previousAction = null;
		
		for (UserAction currentAction : clip.getActions()) {
			if (!firstAction) {
				if (currentAction.getTime() < 0) {
					return true;
				}

				if (currentAction instanceof StepAttempt && previousAction instanceof StepAttempt) {
					StepAttempt currentStep = (StepAttempt) currentAction;
					StepAttempt previousStep = (StepAttempt) previousAction;
					
					// It shouldn't be possible to have 2 "right" steps in the same cell
					if (currentStep.getAssessment() == Assessment.RIGHT && previousStep.getAssessment() == Assessment.RIGHT &&
						currentStep.getCell().equals(previousAction.getCell())) {
						return true;
					}
				}
			}
			
			previousAction = currentAction;
			firstAction = false;
		}
		return false;
	}
	
	private void generateDiagnosis(BehaviorInterpretation interpretation) {
		
		// Temporary very simple model of diagnosis.
		// This old model only account for 30 gaming clips that are not already included in the three patterns (but much more misdiagnosis).
		// (as of 15/12/2013 at 3:33 pm)
		/*int countGamingElements = 0;
		for (InterpretationElement element : interpretation.getAllInterpretationElements()) {
			if (element.getGamingWeight() > 0) {
				countGamingElements += 1;
			}
		}
		
		if (countGamingElements > 6) {
			interpretation.setDiagnostic(Behavior.GAMING);
		} else {
			interpretation.setDiagnostic(Behavior.NOT_GAMING);
		}*/
		
		
		Behavior diagnosis = Behavior.NOT_GAMING;
		
		boolean targetedPattern = false;
		
		// TODO - Is only one pattern enough?
		//      - Are there patterns for which one is enought, but other for which there must be more than one?
		//      - ie, some patterns are stronger indicator of gaming.
		List<UserAction> actions = interpretation.getClip().getActions();
		
		int singleSameDiffPatternCount = 0;
		
		for (int i = 0; i < actions.size(); i++) {
			
			//if (isSearchingForBottomOutHint(interpretation, actions.get(i))) {
			//	
			//}
			
			// Patterns of 2 actions
			if (i > 0) {
				UserAction firstAction = actions.get(i - 1);
				UserAction secondAction = actions.get(i);
				
				//if (isHelpReadRight(interpretation, firstAction, secondAction)) {
				//	interpretation.addPatternCount(GamingPattern.helpReadRight);
				//}
				
				//if (isNotRightDidNotThinkHelp(interpretation, firstAction, secondAction)) {
				//	interpretation.addPatternCount(GamingPattern.notRightDidNotThinkHelp);
				//}
				
				//if (isHelpBottomOutNotRight(interpretation, firstAction, secondAction)) {
				//	interpretation.addPatternCount(GamingPattern.helpBottomOutNotRight);
				//}
				
				// Included in best as of 19/12/2013 at 9:45 am
				if (isSameWrongAnswerDifferentContextPattern(interpretation, firstAction, secondAction)) {
					interpretation.addPatternCount(GamingPattern.sameWrongAnswerDifferentContext);
					targetedPattern = true;
				}
				
				// Included in best as of 19/12/2013 at 10:44 am
				//if (isGuessNotRightSimilarNotRight(interpretation, firstAction, secondAction)) {

				//}
				
				//if (test(interpretation, firstAction, secondAction)) {
				//	singleSameDiffPatternCount++;
				//}
				
				//if (singleSameDiffPatternCount >= 2) {

				//}
				
				//if (isRepeatedBug(interpretation, firstAction, secondAction)) {

				//}
				
				// TODO: test individually!
				//if (isBugSameDiffGuess(interpretation, firstAction, secondAction)) {

				//}
				
				//if (isNotRightSimilarBug(interpretation, firstAction, secondAction)) {

				//}
			}
			
			// Patterns of 3 actions
			if (i > 1) {
				UserAction firstAction = actions.get(i - 2);
				UserAction secondAction = actions.get(i - 1);
				UserAction thirdAction = actions.get(i);
				
				//if (isRightRightRight(interpretation, firstAction, secondAction, thirdAction)) {
				//	interpretation.addPatternCount(GamingPattern.rightRightRight);
				//}
				
				//if (isNotRightNotRightNotRight(interpretation, firstAction, secondAction, thirdAction)) {
				//	interpretation.addPatternCount(GamingPattern.notRightNotRightNotRight);
				//}
				
				//if (isNotRightRightNotRight(interpretation, firstAction, secondAction, thirdAction)) {
				//	interpretation.addPatternCount(GamingPattern.notRightRightNotRight);
				//}
				
				//if (isNotRightNotRightRight(interpretation, firstAction, secondAction, thirdAction)) {
				//	interpretation.addPatternCount(GamingPattern.notRightNotRightRight);
				//}
				
				//if (isBugBugBug(interpretation, firstAction, secondAction, thirdAction)) {
				//	interpretation.addPatternCount(GamingPattern.bugBugBug);
				//}
				
				// Included in best as of 19/12/2013 at 9:45 am
				//if (isBottomOutFollowedBySameWrongAnswerDiffContexts(interpretation, firstAction, secondAction, thirdAction)) {
				
				//}
				
				// Included in best as of 19/12/2013 at 9:45 am
				//if (isBottomOutWrongGuessWrong(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				// Included in best as of 19/12/2013 at 9:45 am
				//if (isRepeatedSameAnswerDiffContext(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				// Included in best as of 19/12/2013 at 9:45 am
				// Removing this pattern slightly increases the kappa when adding "isRepeatedNotRightWithOneSimilar" instead (but removes 20 correct diagnosis)
				if (isRepeatedSimilarAnswers(interpretation, firstAction, secondAction, thirdAction)) {
					interpretation.addPatternCount(GamingPattern.repeatedSimilarAnswers);
					targetedPattern = true;
				}
				
				if (isNotRightSimilarNotRightSameAnswerDiffContext(interpretation, firstAction, secondAction, thirdAction)) {
					interpretation.addPatternCount(GamingPattern.notRightSimilarNotRightSameAnswerDiffContext);
					targetedPattern = true;
				}
				
				//if (isRepeatedNotRightWithOneSimilar(interpretation, firstAction, secondAction, thirdAction)) {
				
				//}
				
				if (isRepeatedWrongGuessesPattern(interpretation, firstAction, secondAction, thirdAction)) {
					interpretation.addPatternCount(GamingPattern.repeatedWrongGuessesPattern);
					targetedPattern = true;
				}
				
				// removed from best as of 19/12/2013 at 12:01 pm (only added 5 gaming, while adding 20 misdiagnosis)
				//if (isAnswerFromBugMessageFollowedByGuess(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				if (isNotRightSimilarNotRightGuess(interpretation, firstAction, secondAction, thirdAction)) {
					interpretation.addPatternCount(GamingPattern.notRightSimilarNotRightGuess);
					targetedPattern = true;
				}
				
				if (isBottomOutNotRightSimilarNotRight(interpretation, firstAction, secondAction, thirdAction)) {
					interpretation.addPatternCount(GamingPattern.bottomOutNotRightSimilarNotRight);
					targetedPattern = true;
				}
				
				//if (isNotRightSameAnswerDifferentContextNotRightSimilarNotRight(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				if (isNotRightSameDiffNotRightContextSwitch(interpretation, firstAction, secondAction, thirdAction)) {
					interpretation.addPatternCount(GamingPattern.notRightSameDiffNotRightContextSwitch);
					targetedPattern = true;
				}
				
				// TODO: test individually!
				//if (isNotRightGuessNotRightDidNotThinkHelp(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				//if(isDidNotThinkBugSameDiffRightNotRight(interpretation, firstAction, secondAction, thirdAction)) {
				//	interpretation.addPatternCount(GamingPattern.didNotThinkBugSameDiffRightNotRight);
				//}
				
				if (isBugSameDiffRightBug(interpretation, firstAction, secondAction, thirdAction)) {
					interpretation.addPatternCount(GamingPattern.bugSameDiffRightBug);
					targetedPattern = true;
				}
				
				//if (isNotRightDidNotThinkHelpBottomOutNotRight(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				//if (isNotRightNotRightSimilar(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				//if (isGuessSimilarRightNotRightGuessSimilar(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				//if (isBugBugBugWithOneSimilar(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				//if (isBugHelpBug(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				//if (isHelpBugBug(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				//if (isHelpBottomOutNotRight(interpretation, firstAction, secondAction, thirdAction)) {

				//}
				
				 //XXX this is 2 different patterns in the current model. Explain in the paper that we merged the patterns? Or expand in 2 patterns here?
				if (isRepeatedNotRightOneSimilarOneSwitchContext(interpretation, firstAction, secondAction, thirdAction)) {
					//interpretation.addPatternCount(GamingPattern.repeatedNotRightOneSimilarOneSwitchContext);
					targetedPattern = true;
					
					if (isNotRightSimilarNotRightContextSwitchNotRight(interpretation, firstAction, secondAction, thirdAction)) {
						interpretation.addPatternCount(GamingPattern.isNotRightSimilarNotRightContextSwitchNotRight);
					}
					if (isNotRightContextSwitchNotRightSimilarNotRight(interpretation, firstAction, secondAction, thirdAction)) {
						interpretation.addPatternCount(GamingPattern.isNotRightContextSwitchNotRightSimilarNotRight);
					}
				}
				
				//if (isGuessGuessGuess(interpretation, firstAction, secondAction, thirdAction)) {

				//}
			}
			
			// patterns of 4 actions
			if (i > 2) {
				
				UserAction firstAction = actions.get(i - 3);
				UserAction secondAction = actions.get(i - 2);
				UserAction thirdAction = actions.get(i - 1);
				UserAction fourthAction = actions.get(i);
				
				if (isNotRightSimilarNotRightQuickHelpNotRight(interpretation, firstAction, secondAction, thirdAction, fourthAction)) {
					interpretation.addPatternCount(GamingPattern.notRightSimilarNotRightQuickHelpNotRight);
					targetedPattern = true;
				}
				
				if (isHelpRepeatedNotRightOneSimilar(interpretation, firstAction, secondAction, thirdAction, fourthAction)) {
					interpretation.addPatternCount(GamingPattern.helpRepeatedNotRightOneSimilar);
					targetedPattern = true;
				}
				
				if (isRepeatedNotRightOneSimilarQuickHelp(interpretation, firstAction, secondAction, thirdAction, fourthAction)) {
					interpretation.addPatternCount(GamingPattern.repeatedNotRightOneSimilarQuickHelp);
					targetedPattern = true;
				}
			}
		}
		
		if (targetedPattern) {
			diagnosis = Behavior.GAMING;
		}
		
		interpretation.setDiagnostic(diagnosis);
	}

	private boolean isSearchingForBottomOutHint(BehaviorInterpretation interpretation, UserAction action) {
		
		if (!(action instanceof HelpRequest))
			return false;
		
		if (!interpretation.hasInterpretation(action, SearchingForBottomOutHint.class))
			return false;
		
		return true;
	}
	
	private boolean isRightRightRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() != Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() != Assessment.RIGHT)
			return false;
		
		return true;
	}
	
	private boolean isNotRightNotRightNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		return true;
	}
	
	private boolean isNotRightRightNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() != Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		return true;
	}
	
    private boolean isNotRightNotRightRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() != Assessment.RIGHT)
			return false;
		
		return true;
	}
	
	private boolean isHelpReadRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction) {
		
		if (!(firstAction instanceof HelpRequest) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() != Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, ReadingHelpMessage.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightDidNotThinkHelp(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction) {
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof HelpRequest))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, DidNotThinkBeforeHelpRequest.class))
			return false;
		
		return true;
	}
	
	private boolean isHelpBottomOutNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction) {
		
		if (!(firstAction instanceof HelpRequest) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, SearchingForBottomOutHint.class))
			return false;
		
		return true;
	}
	
	private boolean isRepeatedWrongGuessesPattern(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x correct diagnosis and x misdiagnosis. Seems like a good pattern, but could be more stringent.
		
		// Searching for the following pattern:
		// Guess -> Not Right -> Guess -> Not Right -> Guess
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, GuessingStep.class) || !interpretation.hasInterpretation(secondAction, GuessingStep.class) || !interpretation.hasInterpretation(thirdAction, GuessingStep.class))
			return false;
		
		if (interpretation.hasInterpretation(secondAction, RepeatedStep.class) || interpretation.hasInterpretation(thirdAction, RepeatedStep.class))
			return false;
		
		return true;
	}
	
	private boolean isSameWrongAnswerDifferentContextPattern(BehaviorInterpretation interpretation, UserAction firstAction,	UserAction secondAction) {
		 
		// 88 correct diagnosis and 55 misdiagnosis. Seems like a pretty good pattern, maybe still some way to improve it.
		// 112 correct diagnosis and 96 misdiagnosis (without the "guessing step" for the second action)
		
		// Searching for the following pattern:
		// Not Right -> guess -> same answer (for both first and second action) different context -> not right
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, GuessingStep.class))
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class))
			return false;
		
		return true;
	}
	

	
	private boolean isAnswerFromBugMessageFollowedByGuess(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// 13 good diagnosis, 26 misdiagnosis. Alright, but not that good ...
		
		// Searching for the following pattern:
		// Bug -> Guess (probably got the answer from the bug message) -> Right -> Guess -> Wrong (or Bug)
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.BUG)
			return false;
		
		// TODO: try removing the "guess" here?
		if (!interpretation.hasInterpretation(secondAction, GuessingStep.class))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() != Assessment.RIGHT)
			return false;
		
		// TODO: make sure that the right action is in the same context?
		//     : might also be the same answer in a different context ... already included in a different pattern?
		
		if (!interpretation.hasInterpretation(thirdAction, GuessingStep.class))
			return false;
		
		if (((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		return true;
	}
	

	
	private boolean isBottomOutFollowedBySameWrongAnswerDiffContexts(BehaviorInterpretation interpretation, UserAction firstAction,	UserAction secondAction, UserAction thirdAction) {
		// 4 correct diagnosis and 5 misdiagnosis. 
		// High probability of gaming if this pattern is seen, but not much clip match it. Maybe too restrictive?
		
		// Searching for the following pattern:
		// Help -> Searching for bottom-out -> Not Right -> same answer diff context
		
		if (!(firstAction instanceof HelpRequest) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, SearchingForBottomOutHint.class))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SameAnswerDifferentContext.class))
			return false;
		
		return true;
	}
	
	private boolean isBottomOutWrongGuessWrong(BehaviorInterpretation interpretation, UserAction firstAction,	UserAction secondAction, UserAction thirdAction) {
		// 28 correct diagnosis and 66 misdiagnosis.
		// Probably a lot of overlap with "isRepeatedWrongGuessesPattern"
		
		// Searching for the following pattern:
		// Help -> Searching for bottom-out -> Not Right -> guess -> Not Right
		
		if (!(firstAction instanceof HelpRequest) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, SearchingForBottomOutHint.class))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, GuessingStep.class))
			return false;
		
		return true;
	}
	
	private boolean isRepeatedSameAnswerDiffContext(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// 67 correct diagnosis and 34 misdiagnosis (without switched context before right)
		// 79 correct diagnsosis and 54 misdiagnosis (with switched context before right)
				
		// Searching for the following pattern:
		// Not right -> same answer diff context -> not right -> (same answer diff context or help request without thinking or guess or context switch).
		// Should this be seperated in 3 different patterns?
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, DidNotThinkBeforeHelpRequest.class) && 
			!interpretation.hasInterpretation(thirdAction, SameAnswerDifferentContext.class) &&
			!interpretation.hasInterpretation(thirdAction, GuessingStep.class) &&
			!interpretation.hasInterpretation(thirdAction, SwitchedContextBeforeRight.class))
			return false;
		
		return true;
	}
	
	private boolean isRepeatedSimilarAnswers(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// 131 gaming, 200 misdiagnosed. Could be more stringent?
		
		// Searching for the following pattern:
		// Not right -> similar answer -> not right -> similar answer
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class))
			return false;
		
		// Make sure that the context didn't switch
		if (interpretation.hasInterpretation(secondAction, SwitchedContextBeforeRight.class) || interpretation.hasInterpretation(thirdAction, SwitchedContextBeforeRight.class))
			return false;
		
		return true;
	}
	
	private boolean isRepeatedNotRightWithOneSimilar(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// 206 gaming, 356 misdiagnosed ... should be much more stringent ...
	    // Adds 58 gaming diagnosis (but also 177 misdiagnosis). Should try to isolate the new diagnosis.
	    // Potential to be a good pattern if can make it more stringent
	    // Should look at the misdiagnosis and try to determine how to make the pattern more stringent
		
		// Searching for the following pattern:
		// Not right -> Not right -> Not Right
	    // with at least 1 similar answer
	
	
    	// With additional condition of excluding patterns where there is one repeated step additional condition:
		// 185 gaming, 282 misdiagnosis
		// adds 48 new true positive to the current model and ? misdiagnosis, better than without this condition for the full model (maybe could still be improved) 
	
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class) && !interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class))
			return false;
	
		//if (interpretation.hasInterpretation(secondAction, RepeatedStep.class) || interpretation.hasInterpretation(thirdAction, RepeatedStep.class))
		//	return false;
		
		// With additional condition of excluding patterns where there was a context switch before getting the answer right
		// 122 gaming, 207 misdiagnosis
		//if (interpretation.hasInterpretation(secondAction, SwitchedContextBeforeRight.class) || interpretation.hasInterpretation(thirdAction, SwitchedContextBeforeRight.class))
		//	return false;
		
		// TODO: take the basic form for this pattern, generate a file of the true positive, and look at them with Adrianna to try to figure out the different patterns.
		
		return true;
	}
	
	private boolean isRepeatedNotRightOneSimilarOneSwitchContext(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// Searching for the following pattern:
		// Not right -> Not right -> Not Right
	    // with at least 1 similar answer
		// And the other a context switch 
	
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		
		if ((!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class) || !interpretation.hasInterpretation(thirdAction, SwitchedContextBeforeRight.class)) && 
			(!interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class) || !interpretation.hasInterpretation(secondAction, SwitchedContextBeforeRight.class)))
			return false;
		
		return true;
	}
	
	private boolean isNotRightSimilarNotRightContextSwitchNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// Searching for the following pattern:
		// Not right -> Not right -> Not Right
	    // with at least 1 similar answer
		// And the other a context switch 
	
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		
		if ((!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class) || !interpretation.hasInterpretation(thirdAction, SwitchedContextBeforeRight.class)))
			return false;
		
		return true;
	}
	
	private boolean isNotRightContextSwitchNotRightSimilarNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// Searching for the following pattern:
		// Not right -> Not right -> Not Right
	    // with at least 1 similar answer
		// And the other a context switch 
	
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		
		if ((!interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class) || !interpretation.hasInterpretation(secondAction, SwitchedContextBeforeRight.class)))
			return false;
		
		return true;
	}
	
	private boolean isNotRightSimilarNotRightQuickHelpNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction, UserAction fourthAction) {
		
		// x gaming, x misdiagnosed.
		
		// Searching for the following pattern:
		// Not right -> similar answer -> not right -> did not think -> help -> not right
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof HelpRequest) || !(fourthAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) fourthAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, DidNotThinkBeforeHelpRequest.class))
			return false;
		
		String firstAnswer = ((StepAttempt) firstAction).getAnswer();
		String secondAnswer = ((StepAttempt) secondAction).getAnswer();
		String fourthAnswer = ((StepAttempt) fourthAction).getAnswer();
		
		if (LevenshteinDistance.compute(firstAnswer, fourthAnswer) > 2 && LevenshteinDistance.compute(secondAnswer, fourthAnswer) > 2)
			return false;
		
		return true;
	}
	
	private boolean isHelpRepeatedNotRightOneSimilar(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction, UserAction fourthAction) {
		
		// x gaming, x misdiagnosed.
		
		// Searching for the following pattern:
		// Help -> not right -> not right -> not right (with one similar between attempts)
		
		if (!(firstAction instanceof HelpRequest) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt) || !(fourthAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) fourthAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class) && !interpretation.hasInterpretation(fourthAction, SimilarAnswerInputs.class))
			return false;
		
		return true;
	}
	
	private boolean isRepeatedNotRightOneSimilarQuickHelp(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction, UserAction fourthAction) {
		
		// x gaming, x misdiagnosed.
		
		// Searching for the following pattern:
		// not right -> not right -> not right (with one similar between attempts) -> Did not think -> Help
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt) || !(fourthAction instanceof HelpRequest))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class) && !interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class))
			return false;
		
		if (!interpretation.hasInterpretation(fourthAction, DidNotThinkBeforeHelpRequest.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightSimilarNotRightGuess(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// 105 gaming, 175 misdiagnosed.
		// adds 11 gaming diagnosis to current best model, but too much misdiagnosis to improve kappa.
		
		// Searching for the following pattern:
		// Not right -> similar answer -> not right -> guess
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, GuessingStep.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightSimilarNotRightSameAnswerDiffContext(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// 30 gaming, 14 misdiagnosed.
		// adds 6 gaming diagnosis to current best model, and 6 misdiagnosis. Improves kappa slightly.
		
		// Searching for the following pattern:
		// Not right -> similar answer -> not right -> same answer different context
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SameAnswerDifferentContext.class))
			return false;
		
		return true;
	}
	
	private boolean isGuessNotRightSimilarNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction) {

		// 112 gaming, 161 misdiagnosed.
		// adds 10 gaming diagnosis to current best model, and 26 misdiagnosis. Slightly improves kappa
		// Is there a way to make it more stringent and limit the amount of misdiagnosis added to the model?
				
		// Searching for the following pattern:
		// Guess -> not right -> similar answer -> not right
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, GuessingStep.class))
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class))
			return false;
		
		return true;
	}
	
	private boolean isRepeatedBug(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction) {

		// 82 gaming, 227 misdiagnosed. Seem to be an indicator of gaming, but enough by itself
		
		// Searching for the following pattern:
		// Bug -> (guess, did not read error message or same answer different context) -> Bug
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.BUG || ((StepAttempt) secondAction).getAssessment() != Assessment.BUG)
			return false;
		
		if (!(interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class) || interpretation.hasInterpretation(secondAction, DidNotReadErrorMessage.class) || interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class)))
			return false;

		return true;
	}
	
	//private boolean isGuessNotRightSimilarNotRight3Actions(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {

		// 77 gaming, 99 misdiagnosed.
				
		// Searching for the following pattern:
		// Guess -> not right -> similar answer -> not right -> (same answer diff context or help request without thinking or guess or context switch)
		
	//	if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt))
	//		return false;
		
	//	if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
	//		return false;
		
	//	if (!interpretation.hasInterpretation(firstAction, GuessingStep.class))
	//		return false;
		
	//	if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class))
	//		return false;
		
	//	if (!interpretation.hasInterpretation(thirdAction, DidNotThinkBeforeHelpRequest.class) && 
	//			!interpretation.hasInterpretation(thirdAction, SameAnswerDifferentContext.class) &&
	//			!interpretation.hasInterpretation(thirdAction, GuessingStep.class) &&
	//			!interpretation.hasInterpretation(thirdAction, SwitchedContextBeforeRight.class))
	//			return false;
		
	//	return true;
	//}

	private boolean isBottomOutNotRightSimilarNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {

		// 17 gaming, 33 misdiagnosed.
		// adds 0 gaming diagnosis to current best model, and 0 misdiagnosis.
				
		// Searching for the following pattern:
		// Hint -> search for bottom-out -> not right -> similar answer -> not right
		
		if (!(firstAction instanceof HelpRequest) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, SearchingForBottomOutHint.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightSameAnswerDifferentContextNotRightSimilarNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {

		// 19 gaming, 11 misdiagnosed.
		// adds 0 gaming diagnosis to current best model, and 2 misdiagnosis.
				
		// Searching for the following pattern:
		// Not right -> same answer different context -> not right -> similar answer -> not right
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightSameDiffNotRightContextSwitch(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// 69 gaming, 41 misdiagnosed.
		// Seems like it's already completely included in other patters? Check in more details
		// Might still be useful ...
						
		// Searching for the following pattern:
		// Not right -> same answer different context -> not right -> context switch
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SwitchedContextBeforeRight.class))
			return false;
		
		return true;
	}
	
	
	// TODO: change the name and enter the appropriate comments for that pattern!
	private boolean test(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction) {

		
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class) && !interpretation.hasInterpretation(secondAction, SameAnswerSameContextDifferentAction.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightGuessNotRightDidNotThinkHelp(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 6 new gaming, 92 new misdiagnosis ...
		
		// This becomes 1 new gaming, 7 new misdiagnosis if add the restriction that the first action must be guessed.
						
		// Searching for the following pattern:
		// Not Right -> Guess -> Not Right -> Did not think -> Help
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof HelpRequest))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		//if (!interpretation.hasInterpretation(firstAction, GuessingStep.class))
		//	return false;
		
		if (!interpretation.hasInterpretation(secondAction, GuessingStep.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, DidNotThinkBeforeHelpRequest.class))
			return false;
		
		return true;
	}
	
	private boolean isDidNotThinkBugSameDiffRightNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 2 new gaming, 5 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Did not think -> Bug -> Same/diff -> Right -> Not Right
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.BUG || ((StepAttempt) secondAction).getAssessment() != Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, GuessingStepWithValuesFromProblem.class))
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class))
			return false;
		
		return true;
	}
	
	private boolean isBugSameDiffRightBug(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 6 new gaming, 8 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Bug -> Same/diff -> Right -> Bug
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.BUG || ((StepAttempt) secondAction).getAssessment() != Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() != Assessment.BUG)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class))
			return false;
		
		return true;
	}
	
	private boolean isBugSameDiffGuess(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction) {
		
		// x gaming, x misdiagnosed.
		// 15 new gaming, 84 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Bug -> Same/diff -> Guess
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.BUG)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SameAnswerDifferentContext.class))
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, GuessingStep.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightDidNotThinkHelpBottomOutNotRight(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 7 new gaming, 35 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Not Right -> Did not think -> Help -> Bottom-out
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof HelpRequest) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, DidNotThinkBeforeHelpRequest.class))
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SearchingForBottomOutHint.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightNotRightSimilar(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 4 new gaming, 81 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Not Right -> Not Right -> Similar answer
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class))
			return false;
		
		return true;
	}
	
	private boolean isNotRightSimilarBug(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction) {
		
		// x gaming, x misdiagnosed.
		// 1 new gaming, 32 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Not Right -> Similar answer -> Bug
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() != Assessment.BUG)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class))
			return false;
		
		return true;
	}
	
	private boolean isGuessSimilarRightNotRightGuessSimilar(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 4 new gaming, 29 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Guess -> Similar -> Right -> Not Right -> Guess -> Similar 
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.RIGHT || ((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, GuessingStep.class))
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, SimilarAnswerInputs.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, GuessingStep.class))
			return false;
		
		if (!interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class))
			return false;
		
		return true;
	}
	
	private boolean isBugBugBug(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 4 new gaming, 9 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Bug -> Bug -> Bug 
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.BUG || ((StepAttempt) secondAction).getAssessment() != Assessment.BUG || ((StepAttempt) thirdAction).getAssessment() != Assessment.BUG)
			return false;
		
		return true;
	}
	
	private boolean isBugBugBugWithOneSimilar(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
						
		// Searching for the following pattern:
		// Bug -> Bug -> Bug (with one similar)
		// Make sure that there is no repeated steps between the bugs 
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.BUG || ((StepAttempt) secondAction).getAssessment() != Assessment.BUG || ((StepAttempt) thirdAction).getAssessment() != Assessment.BUG)
			return false;
		
		if (!interpretation.hasInterpretation(secondAction, SimilarAnswerInputs.class) && !interpretation.hasInterpretation(thirdAction, SimilarAnswerInputs.class))
			return false;
		
		if (interpretation.hasInterpretation(secondAction, RepeatedStep.class) || interpretation.hasInterpretation(thirdAction, RepeatedStep.class))
			return false;
		
		return true;
	}
	
	private boolean isBugHelpBug(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 1 new gaming, 9 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Bug -> Help -> Bug 
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof HelpRequest) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) firstAction).getAssessment() != Assessment.BUG || ((StepAttempt) thirdAction).getAssessment() != Assessment.BUG)
			return false;
		
		return true;
	}
	
	private boolean isHelpBugBug(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// 0 new gaming, 9 new misdiagnosis ...
						
		// Searching for the following pattern:
		// Help -> Bug -> Bug 
		
		if (!(firstAction instanceof HelpRequest) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() != Assessment.BUG || ((StepAttempt) thirdAction).getAssessment() != Assessment.BUG)
			return false;
		
		return true;
	}
	
	private boolean isHelpBottomOutNotRightHelp(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// x gaming, x misdiagnosed.
		// x new gaming, x new misdiagnosis ...
						
		// Searching for the following pattern:
		// Help -> Bottom out -> right -> Help
		
		if (!(firstAction instanceof HelpRequest) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof HelpRequest))
			return false;
		
		if (((StepAttempt) secondAction).getAssessment() != Assessment.RIGHT)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, SearchingForBottomOutHint.class))
			return false;
		
		return true;
	}
	
	private boolean isGuessGuessGuess(BehaviorInterpretation interpretation, UserAction firstAction, UserAction secondAction, UserAction thirdAction) {
		
		// 66 new gaming, 691 new misdiagnosis ...
		// Might actually contains some interesting patterns that haven't been used yet ...
						
		// Searching for the following pattern:
		// Guess -> Guess -> Guess (with a maximum of 1 right answer)
		
		if (!(firstAction instanceof StepAttempt) || !(secondAction instanceof StepAttempt) || !(thirdAction instanceof StepAttempt))
			return false;
		
		int rightCount = 0;
		
		if (((StepAttempt) firstAction).getAssessment() == Assessment.RIGHT)
			rightCount++;
		
		if (((StepAttempt) secondAction).getAssessment() == Assessment.RIGHT)
			rightCount++;
		
		if (((StepAttempt) thirdAction).getAssessment() == Assessment.RIGHT)
			rightCount++;
		
		if (rightCount > 1)
			return false;
		
		if (!interpretation.hasInterpretation(firstAction, GuessingStep.class) && !interpretation.hasInterpretation(secondAction, GuessingStep.class) && !interpretation.hasInterpretation(thirdAction, GuessingStep.class))
			return false;
		
		return true;
	}
}

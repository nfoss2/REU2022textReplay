import java.security.InvalidParameterException;

import com.vertica.dsi.exceptions.InvalidArgumentException;


public enum InterpretationConstituent {
	
	didNotThinkBeforeHelpRequest,
	thoughtBeforeHelpRequest,
	readHelpMessage,
	scanningHelpMessage,
	searchingForBottomOutHint,
	thoughtBeforeStepAttempt,
	guessingStep,
	thoughtAboutDuringLastStep,
	thoughtAboutStepButFlawInProcedure,
	guessingStepWithValuesFromProblem,
	didNotReadErrorMessage,
	readErrorMessage,
	thoughtAboutError,
	switchedContextBeforeRight,
	didNotSwitchContext,
	repeatedStep,
	notRepeatedStep,
	sameAnswerDifferentContext,
	sameAnswerSameContextDifferentAction,
	similarAnswerInputs;
	
	public static String getTextValue(InterpretationConstituent constituent) {
		
		if (constituent == didNotThinkBeforeHelpRequest) {
			return "[Did not think before help request]";
		} else if (constituent == thoughtBeforeHelpRequest) {
			return "[Thought before help request]";
		} else if (constituent == readHelpMessage) {
			return "[Read help message]";
		} else if (constituent == scanningHelpMessage) {
			return "[Scanning help message]";
		} else if (constituent == searchingForBottomOutHint) {
			return "[Searching for bottom out hint]";
		} else if (constituent == thoughtBeforeStepAttempt) {
			return "[Thought before step attempt]";
		} else if (constituent == guessingStep) {
			return "[Guessing step]";
		} else if (constituent == thoughtAboutDuringLastStep) {
			return "[planned ahead]";
		} else if (constituent == thoughtAboutStepButFlawInProcedure) {
			return "[unsuccessful but sincere attempt]";
		} else if (constituent == guessingStepWithValuesFromProblem) {
			return "[guessing with values from problem]";
		} else if (constituent == didNotReadErrorMessage) {
			return "[did not read error message]";
		} else if (constituent == readErrorMessage) {
			return "[read error message]";
		} else if (constituent == thoughtAboutError) {
			return "[thought about error]";
		} else if (constituent == switchedContextBeforeRight) {
			return "[switched context before right]";
		} else if (constituent == didNotSwitchContext) {
			return "[same context]";
		} else if (constituent == repeatedStep) {
			return "[repeated step]";
		} else if (constituent == notRepeatedStep) {
			return "[different step]";
		} else if (constituent == sameAnswerDifferentContext) {
			return "[same answer/diff. context]";
		} else if (constituent == sameAnswerSameContextDifferentAction) {
			return "[same answer/same context/diff. action]";
		} else if (constituent == similarAnswerInputs) {
			return "[similar answer]";
		}
		
		throw new InvalidParameterException();
	}
	
	public static int getIndex(InterpretationConstituent constituent) {
		
		if (constituent == didNotThinkBeforeHelpRequest) {
			return 0;
		} else if (constituent == thoughtBeforeHelpRequest) {
			return 1;
		} else if (constituent == readHelpMessage) {
			return 2;
		} else if (constituent == scanningHelpMessage) {
			return 3;
		} else if (constituent == searchingForBottomOutHint) {
			return 4;
		} else if (constituent == thoughtBeforeStepAttempt) {
			return 5;
		} else if (constituent == guessingStep) {
			return 6;
		} else if (constituent == thoughtAboutDuringLastStep) {
			return 7;
		} else if (constituent == thoughtAboutStepButFlawInProcedure) {
			return 8;
		} else if (constituent == guessingStepWithValuesFromProblem) {
			return 9;
		} else if (constituent == didNotReadErrorMessage) {
			return 10;
		} else if (constituent == readErrorMessage) {
			return 11;
		} else if (constituent == thoughtAboutError) {
			return 12;
		} else if (constituent == switchedContextBeforeRight) {
			return 13;
		} else if (constituent == didNotSwitchContext) {
			return 14;
		} else if (constituent == repeatedStep) {
			return 15;
		} else if (constituent == notRepeatedStep) {
			return 16;
		} else if (constituent == sameAnswerDifferentContext) {
			return 17;
		} else if (constituent == sameAnswerSameContextDifferentAction) {
			return 18;
		} else if (constituent == similarAnswerInputs) {
			return 19;
		}
		
		throw new InvalidParameterException();
	}
	
	public static int getIndex(InterpretationElement element) {
		
		InterpretationConstituent constituent;
		
		if (element instanceof DidNotThinkBeforeHelpRequest) {
			constituent = didNotThinkBeforeHelpRequest;
		} else if (element instanceof ThoughtBeforeHelpRequest) {
			constituent = thoughtBeforeHelpRequest;
		} else if (element instanceof ReadingHelpMessage) {
			constituent = readHelpMessage;
		} else if (element instanceof ScanningHelpMessage) {
			constituent = scanningHelpMessage;
		} else if (element instanceof SearchingForBottomOutHint) {
			constituent = searchingForBottomOutHint;
		} else if (element instanceof ThoughtBeforeStepAttempt) {
			constituent = thoughtBeforeStepAttempt;
		} else if (element instanceof GuessingStep) {
			constituent = guessingStep;
		} else if (element instanceof ThoughtAboutDuringLastStep) {
			constituent = thoughtAboutDuringLastStep;
		} else if (element instanceof ThoughtAboutStepButFlawInProcedure) {
			constituent = thoughtAboutStepButFlawInProcedure;
		} else if (element instanceof GuessingStepWithValuesFromProblem) {
			constituent = guessingStepWithValuesFromProblem;
		} else if (element instanceof DidNotReadErrorMessage) {
			constituent = didNotReadErrorMessage;
		} else if (element instanceof ReadErrorMessage) {
			constituent = readErrorMessage;
		} else if (element instanceof ThoughtAboutError) {
			constituent = thoughtAboutError;
		} else if (element instanceof SwitchedContextBeforeRight) {
			constituent = switchedContextBeforeRight;
		} else if (element instanceof DidNotSwitchContext) {
			constituent = didNotSwitchContext;
		} else if (element instanceof RepeatedStep) {
			constituent = repeatedStep;
		} else if (element instanceof NotRepeatedStep) {
			constituent = notRepeatedStep;
		} else if (element instanceof SameAnswerDifferentContext) {
			constituent = sameAnswerDifferentContext;
		} else if (element instanceof SameAnswerSameContextDifferentAction) {
			constituent = sameAnswerSameContextDifferentAction;
		} else if (element instanceof SimilarAnswerInputs) {
			constituent = similarAnswerInputs;
		} else {
			throw new InvalidParameterException();
		}
		
		return getIndex(constituent);
	}
}

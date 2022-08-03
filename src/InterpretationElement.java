import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;


public abstract class InterpretationElement {
	
	public List<Integer> all = new ArrayList<Integer>(Collections.nCopies(20, 0));

	@Override
	public abstract String toString();

	public abstract void flipBit(InterpretationElement e);

	public List<String> colNames = new ArrayList<String>(Arrays.asList(
		"DidNotThinkBeforeHelpRequest",
		"ThoughtBeforeHelpRequest",
		"ReadingHelpMessage",
		"ScanningHelpMessage",
		"SearchingForBottomOutHint",
		"ThoughtBeforeStepAttempt",
		"GuessingStep",
		"ThoughtAboutDuringLastStep",
		"ThoughtAboutStepButFlawInProcedure",
		"GuessingStepWithValuesFromProblem",
		"DidNotReadErrorMessage",
		"ReadErrorMessage",
		"ThoughtAboutError",
		"SwitchedContextBeforeRight",
		"DidNotSwitchContext",
		"RepeatedStep",
		"NotRepeatedStep",
		"SameAnswerDifferentContext",
		"SameAnswerSameContextDifferentAction",
		"SimilarAnswerInputs"
	));
}

// NTBHR
class DidNotThinkBeforeHelpRequest extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Did not think about the next step before asking for help";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(0,1);
	}
}

// TBHR
class ThoughtBeforeHelpRequest extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Thought about the next step before asking for help";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(1,1);
	}
}

// RHM
class ReadingHelpMessage extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Reading help messages";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(2,1);
	}
}

// SHM
class ScanningHelpMessage extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Scanning help messages";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(3,1);
	}
}

// SBOH
class SearchingForBottomOutHint extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Searching for bottom out hint";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(4,1);
	}
}

// TBSA
class ThoughtBeforeStepAttempt extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Thought before attempting the step";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(5,1);
	}
}

// GS
class GuessingStep extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Guessed the answer for the step";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(6,1);
	}
}

// TADLS
class ThoughtAboutDuringLastStep extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Planned one step ahead";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(7,1);
	}
}

// TASBW
class ThoughtAboutStepButFlawInProcedure extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Thought about the next step, but there was a flaw in the student's procedure";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(8,1);
	}
}

// GSVFP
class GuessingStepWithValuesFromProblem extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Trying to guess the answer for this step by copying values from the problem description";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(9,1);
	}
}

// NREM
class DidNotReadErrorMessage extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Did not read the error message";
	}


	public void flipBit(InterpretationElement e) {
		e.all.set(10,1);
	}
}

// REM
class ReadErrorMessage extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Read the error message";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(11,1);
	}
}

// TAE
class ThoughtAboutError extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Thought about the error";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(12,1);
	}
}

// SCBR
class SwitchedContextBeforeRight extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Changed context before getting the right answer";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(13,1);
	}
}

// DNSC
class DidNotSwitchContext extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Did not switch context";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(14,1);
	}
}

// RS
class RepeatedStep extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Did not think about the next step before asking for help";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(15,1);
	}
}
// NRS
class NotRepeatedStep extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Did not repeat the same step";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(16,1);
	}
}

// SADC
class SameAnswerDifferentContext extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Tried the same answer in a different context before";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(17,1);
	}
}

// SASCDA
class SameAnswerSameContextDifferentAction extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Tried the same answer, in the same context, but with a different aciton";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(18,1);
	}
}

// SAI
// TODO: this interpretation element is harder to get right. Right now it depends on the Levenshtein distance between the inputs
class SimilarAnswerInputs extends InterpretationElement {
	
	@Override
	public String toString() {
		return "Entered an answer similar to the previous one";
	}

	public void flipBit(InterpretationElement e) {
		e.all.set(19,1);
	}
}
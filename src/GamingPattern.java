
public enum GamingPattern {
	// patterns intended for "not-gaming" detection
	//rightRightRight,
	//helpReadRight,
	// patterns intended to be partial "gaming" patterns
	//notRightNotRightNotRight,
	//notRightRightNotRight,
	//notRightNotRightRight,
	//bugBugBug,
	//notRightDidNotThinkHelp,
	//helpBottomOutNotRight,
	// patterns intended for "gaming" detection
	sameWrongAnswerDifferentContext,
	repeatedSimilarAnswers,
	notRightSimilarNotRightSameAnswerDiffContext,
	repeatedWrongGuessesPattern,
	notRightSimilarNotRightGuess,
	bottomOutNotRightSimilarNotRight,
	notRightSameDiffNotRightContextSwitch,
	//didNotThinkBugSameDiffRightNotRight,
	bugSameDiffRightBug,
	//repeatedNotRightOneSimilarOneSwitchContext,
	isNotRightSimilarNotRightContextSwitchNotRight,
	isNotRightContextSwitchNotRightSimilarNotRight,
	notRightSimilarNotRightQuickHelpNotRight,
	helpRepeatedNotRightOneSimilar,
	repeatedNotRightOneSimilarQuickHelp
}

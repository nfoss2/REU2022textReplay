# Important files

text_obs_PSLC_driverB.java

> This file contains the main function

Clip.java

> This file created the clips and contains useful functions like getActions and getActionCountString for debugging.

BehaviorInterpretation.java

> This file contains my added function getInterpretationOther that will genterate the string of interpretations that is written to the csv file in main.

InterpretationElement.java

> This file contains the method of getting all of the interpretations for an action

CognitiveModel.java

> This file contatins the code to generate the interpretations (generateInterpretation)

# How to run

From the TextReplay directory compile:

```bash
javac -cp "./bin/lib/*" -d bin src/*.java
```

Now run from the bin directory:

```bash
cd bin
java text_obs_PSLC_driverB algebra model
```

# Data

In the TextReplay directory have the following data files:

* HamptonAlgI0506B-TR.txt
* algebra_replay_nums_ADC
* algebra_replay_nums_model-test
* algebra_replay_nums_model-training
* observationsALG-Adriana-combined
* featureOutput.csv


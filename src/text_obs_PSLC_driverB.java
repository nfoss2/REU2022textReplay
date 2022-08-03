import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class text_obs_PSLC_driverB{

    String a1_action_data_file = "../HamptonAlgI0506B-TR.txt";
	//String a1_action_data_file = "wilkinsburg_Alg_2005-MLA.txt";
	//String a1_action_data_file = "cwctc_Alg_2005-MLA.txt";
	
    String geo_action_data_file = "HamptonGeoFall05A-TR.txt";

    int max = 0;

    String action_data_file = "";

    // must have quote marks around all inputs
    // eliminate MEEP lines
   
    String outfile_nothi = "observations";
    String repeatfile = "";
    
    public String coder_id = "BOB";
    
    FileReader st_;
    FileWriter fw_;

    public int totalex = 0;
    int curchar = 0;
    String lessonmapfile = "";

    public boolean repeatOldFirst = true; // for planned coding
    
    private AverageTimes averageTimes = new AverageTimes();

    public void text_observationdriverC(){}

    public FileReader create_tokenizer(){		      
		
		try{
		    return new FileReader(action_data_file);
		}
		catch (FileNotFoundException fnfe){
		    fnfe.printStackTrace();
		}
		return null;
    }
    
    public void getLengthOfExisting(){
		try{
		    StreamTokenizer st = new StreamTokenizer(new FileReader(outfile_nothi+coder_id));
		    int tt= StreamTokenizer.TT_NUMBER; totalex = 0;
		    while (tt!= StreamTokenizer.TT_EOF){
			tt = st.nextToken();
			if (tt == StreamTokenizer.TT_EOL)
			    totalex++;
			if ((tt == StreamTokenizer.TT_WORD)&&(st.sval.equals(coder_id)))
			    totalex++;
		    }
		}
		catch (Exception fnfe){
		}
    }

    public int getRepeatClip(int goalclip){
		try{
		    StreamTokenizer st = new StreamTokenizer(new FileReader(repeatfile+"_"+coder_id));
		    int tt= StreamTokenizer.TT_NUMBER; int curclipnum = 0; int toret = -1;
		    while (tt!= StreamTokenizer.TT_EOF){
			tt = st.nextToken();
			if (tt == StreamTokenizer.TT_EOL)
			    curclipnum++;
			if (tt == StreamTokenizer.TT_WORD)
			    curclipnum++;
			if (curclipnum == goalclip)
			    return toret;
			if (tt == StreamTokenizer.TT_NUMBER)
			    toret = (new Double(st.nval)).intValue();
		    }
		}
		catch (Exception fnfe){
		    fnfe.printStackTrace();
		    return -1;
		}
		return -1;
	    }
	
	    public FileWriter out_tokenizer(){
		try{
		    return new FileWriter(outfile_nothi+coder_id,true);
		}
		catch (Exception fnfe){
		    fnfe.printStackTrace();
		}
		return null;
    }

    public void append_record (String toadd){
		try{
		    fw_ = out_tokenizer();
		    fw_.write(toadd);
		    fw_.flush();
		    fw_.close();
		}
		
		catch (Exception fnfe){
		    fnfe.printStackTrace();
		}
    }
    
    public String getIdentity (){
    	return javax.swing.JOptionPane.showInputDialog("Input Coder ID");
    }

    public Object query_gaming(String title, String msg){
		Object[] options = { "GAMING", "NOT GAMING", "BAD CLIP" }; 
		return new Integer(javax.swing.JOptionPane.showOptionDialog(null, msg, title, javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE, null, options, options[1]));
    }

    public Object query_gaming_more(String title, String msg){
		Object[] options = { "GAMING", "NOT GAMING", "BAD CLIP", "MORE" };
		return new Integer(javax.swing.JOptionPane.showOptionDialog(null, msg, title, javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE, null, options, options[1]));
    }

    public void skipToEOL() throws IOException{
		while ((curchar != 10)&&(curchar != -1))
		    curchar = st_.read();
    }

    public String read_in_field() throws IOException{	
		char[] forconvert = new char[1];
		
		String toret = ""; 
		curchar = st_.read(); 
		while ((curchar != 9)&&(curchar != -1)&&(curchar != 10)){
		    if ((curchar != 9)&&(curchar != -1)&&(curchar != 10)){
			forconvert[0] = (char)curchar;
			toret = toret + new String(forconvert);
			curchar = st_.read(); 
		    }
		    if (curchar == 10)
			throw new RuntimeException("Unexpected EOLN encountered");
		    //System.out.println(curchar);
		}
		//System.out.println();
		if (toret == null)
		    return ("BLANK");
		return toret;
    }    

    public String simplify_prod(String prodin){
		String prodout = ""; int incount = 12; int outcount = 0;
		char c[] = new char[1]; c[0]='0'; 
		if (prodin.charAt(0)!='[')
		    incount=0;
		while (c[0] !=';'){
		    try{
			c[0] = prodin.charAt(incount);
			if ((c[0]==' ')||(java.lang.Character.isUpperCase(c[0]))||(java.lang.Character.isLowerCase(c[0]))){
			    prodout = prodout + new String(c);
			    outcount++;
			}		
		    }
		    catch (IndexOutOfBoundsException e){ c[0]=';'; }
		    incount++;
		}
		return prodout;
    }    


    String lesson_[] = new String[2000000];
    String student_[] = new String[2000000];
    String assess_[] = new String[2000000];
    String cell_[] = new String[2000000];
    String celltype_[] = new String[2000000];
    String action_[] = new String[2000000];
    String answer_[] = new String[2000000];
    String prod_[] = new String[2000000];
    double time_[] = new double[2000000];
    double numsteps_[] = new double[2000000];
    double helpintermedtime_[] = new double[2000000];
	
    public int numberOfActions = 0;
    
    // humaninterpretable has headers and prod names, etc etc
    public void readInActions(){
		System.out.println("Inputting data.");
	
		st_ = create_tokenizer();
	
		int num = -1; String lesson = ""; String student = "";
		String assess=""; String action="";
		String cell = ""; String celltype = ""; String prod = "";
		String answer = "";
		double time = 0; 
	
		String help = "0"; double numsteps = 0; double helpintermedtime = 0;
	
		String curstu = ""; String punchange = "0";
		try{
			while (curchar != -1){ 
			    if ((num % 20000) == 0){
				System.out.print((new Double(num)).doubleValue()/(new Double(max)).doubleValue()*100.0);
				System.out.println("% Complete.");
			    }
			    student = ""; assess = "";
			    cell = ""; celltype = ""; prod = "";
			    time = 0; answer = ""; action = "";
			    helpintermedtime = 0; numsteps = 0;
		
			    num++;
			    
			    lesson = read_in_field();
			    if (lesson.equals("")){
				break;
			    }
		
			    student = read_in_field();
			    assess = read_in_field();
			    cell = read_in_field();
			    read_in_field();
			    action = read_in_field();
			    answer = read_in_field();
			    read_in_field(); read_in_field();
			    prod = simplify_prod(read_in_field());
			    read_in_field();
			    time = java.lang.Double.parseDouble(read_in_field());
			    helpintermedtime = java.lang.Double.parseDouble(read_in_field()); 
			    numsteps = java.lang.Double.parseDouble(read_in_field()); 
			    
			    lesson_[num] = lesson;
			    student_[num] = student;
			    assess_[num] = assess;
			    cell_[num] = cell;
			    //celltype_[num] = celltype;
			    prod_[num] = prod;
			    action_[num] = action;
			    answer_[num] = answer;
			    time_[num] = time;
			    numsteps_[num] = numsteps;
			    helpintermedtime_[num] = helpintermedtime;	 
			    skipToEOL();
			    
			    averageTimes.addStepAttempt(lesson + cell, time);
			    
			    numberOfActions++;
			} 
		} catch (Exception e){
			e.printStackTrace();
			System.out.print("Exception reading in file! Line: ");
			System.out.println(num);curchar = -1;
		}
		
		averageTimes.computeStepValues();
		
		System.out.println("100% Complete.");
    }
    
    public void displayClip (int clipID){
		displayClip(clipID, clipID, 0.0);
    }

    public String getStudentFor(String newlesson){
		try{
		    StreamTokenizer st = new StreamTokenizer(new FileReader(lessonmapfile));
		    int tt= StreamTokenizer.TT_NUMBER; 
		    while (tt!= StreamTokenizer.TT_EOF){
			tt = st.nextToken();
			if ((tt == StreamTokenizer.TT_WORD)&&(st.sval.equals(newlesson))){   
			    tt = st.nextToken();		   
			    tt = st.nextToken();
			    if (st.sval.equals("not"))
				return "NOT POSSIBLE";
			    String toret = "_" + st.sval;
			    tt = st.nextToken();
			    toret = toret + " " + st.sval;
			    return toret;
			}
		    }
		}
		catch (Exception e){ e.printStackTrace();}
		return "NOT FOUND";
    }

    String prevlesson = "";

    public void switchLesson(String newlesson){ 
		String studenttouse = getStudentFor(newlesson);
	
		if (studenttouse.equals("NOT FOUND")){	
		    Object[] options = { "I WILL CONTACT RYAN" }; 
		    String title = "Error!\n\n";
		    String msg = "An error has occurred.\nLesson ";
		    msg = msg + newlesson;
		    msg = msg +  " was not found.\n\n Please quit the program and email Ryan.\n\n";
		    javax.swing.JOptionPane.showOptionDialog(null, msg, title, javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		}
		else if (studenttouse.equals("NOT POSSIBLE")){	
		    Object[] options = { "I'M READY TO OBSERVE" }; 
		    String title = "Observations From a New Lesson";
		    String msg = "You will now be observing actions from a new lesson.\n\n";
		    msg = msg +  "This lesson is not currently runnable from the tutor,\nbut it resembles lessons you have already seen.\n\n";
		    javax.swing.JOptionPane.showOptionDialog(null, msg, title, javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		}
		else{
		Object[] options = { "I'M READY TO OBSERVE" }; 
		String title = "Observations From a New Lesson";
		String msg = "You will now be observing actions from a new lesson.\n(or this is your first observation of the day)\n\n";
		msg = msg +  "Please run the Cognitive Tutor using student\n";
		msg = msg + studenttouse;
		msg = msg + "\n\nWhen you feel ready to conduct observations on this lesson,\nclick on READY\n\n";
		javax.swing.JOptionPane.showOptionDialog(null, msg, title, javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE, null, options, options[0]);}
    }

    public void displayClip (int clipID, int originalID, double totaltime){
	
		if (!lesson_[clipID].equals(prevlesson)){
		    switchLesson(lesson_[clipID]);
		    prevlesson = lesson_[clipID];
		}
		
		String clip_text = ""; String title = "";
	
		title = "Observation " + (new Integer(totalex+1)).toString() + " for coder " + coder_id + ": Clip " +  (new Integer(clipID)).toString() + "\n";	
		int curnum = clipID; int actioncount= 0;
	   
		while (actioncount<5){
		    actioncount++;
		    clip_text = clip_text + "Time " + (new Double(totaltime)).toString() + ":\n" ;
		    
		    // TODO : temp for test purpose
		    String step = lesson_[curnum] + cell_[curnum];
		    clip_text = clip_text + "(Average: " + averageTimes.getAverage(step) + " SD: " + averageTimes.getSD(step) + " Unitized: " + averageTimes.unitizeStepTime(step, time_[curnum]) + "\n";
		    // end temp
	
		    if (!(assess_[curnum].equals("HELP"))){
			if (!action_[curnum].equals("BLANK"))
			    clip_text = clip_text + "Action: " + action_[curnum] + "\n";
			
			clip_text = clip_text + "Input: " + answer_[curnum] + "\nCell or Context: " + cell_[curnum] +  "\n";
			
			clip_text = clip_text + "Assessment: " + assess_[curnum] + "\n";
			
			if (prod_[curnum].equals("BLANK"))		   
			    clip_text = clip_text + "Production: UNKNOWN or GIVEN"; 
			else
			    clip_text = clip_text + "Production: " + prod_[curnum]; 
	
			clip_text = clip_text + "\n\n"; 
		    }else{
			clip_text = clip_text + "Requested help on production: "; 
			clip_text = clip_text + prod_[curnum]+"\n"; 
			clip_text = clip_text + "Cell or Context: " + cell_[curnum] +  "\n";
		
			if (numsteps_[curnum]==-1)
			    clip_text = clip_text + "Read UNKNOWN steps." + "\n";
			else
			    clip_text = clip_text + "Read " + (new Double(numsteps_[curnum]+1)).toString() + " steps." + "\n";
	
			clip_text = clip_text + "\n"; 
		    }
		    totaltime += time_[curnum+1];
		    curnum++;
		    if ((actioncount==5)&&(totaltime<20))
			clip_text = clip_text + "More....";
		}
		//System.out.print(clip_text);
		Object result_o = null;
		if (student_[clipID].equals(student_[originalID])){	 
		    if ((actioncount == 5)&&(totaltime<20)) 
			result_o = query_gaming_more(title, clip_text);
		    else
			result_o = query_gaming(title, clip_text);
		    int result = ((Integer)result_o).intValue();
		    String result_s = "?";
		    if (result==0)
			result_s = "G";
		    if (result==1)
			result_s = "N";
		    if (result==2)
			result_s = "?";
		    if (result<3){
			String toadd = coder_id + " " + (new Integer(originalID)).toString() + " " + result_s + "\n";
			append_record(toadd);
		    }
		    else
			displayClip(curnum, clipID, totaltime);
		}
    }
    
    private Clip getClip(int clipID) {
    	Clip clip = new Clip(clipID);
    	
    	clip.setStudent(student_[clipID]);
    	
    	int actionCount = 0;
    	double totalTime = 0;
    	
    	boolean prevIsHelpRequest = false;
    	double prevHelpRequestNumStep = 0.0;
    	String prevHelpRequestLesson = "";
    	String prevHelpRequestCell = "";
    	double prevHelpRequestTime = 0.0;
    	// while ((clip.getActions().size() % 5) != 0 || totalTime < 20) {
		while ((actionCount % 5) != 0 || totalTime < 20) {

			if (clipID == 1061) {
				System.out.println("clip action size: "+ clip.getActions().size());
			}
    		int currentID = clipID + actionCount;
    		
    		if (currentID >= numberOfActions ||
    			(currentID > clipID && !student_[currentID].equals(student_[currentID - 1])) ||
    			(currentID > clipID && !lesson_[currentID].equals(lesson_[currentID - 1]))) {
    			
    			// If we reached the end of the log file
    			// or we changed student
    			// or we changed lesson
    			// End the clip
				if (clipID == 1061) {
					System.out.println("breaking here");
				}
    			break;
    		}
    		
    		double time = time_[currentID];
    		String lesson = lesson_[currentID];
    		String cell = cell_[currentID];
    		
    		if (!(assess_[currentID].equals("HELP"))) {
    			if (prevIsHelpRequest) {
    				clip.addAction(new HelpRequest(prevHelpRequestLesson, prevHelpRequestCell, prevHelpRequestTime, prevHelpRequestNumStep));
    				prevIsHelpRequest = false;
    			}
    			
    			// Step attempt
    			Assessment assessment = Assessment.getAssessment(assess_[currentID]);
    			String answer = answer_[currentID].toLowerCase();
    			String action = action_[currentID].toLowerCase();
    			
    			clip.addAction(new StepAttempt(lesson, cell, time, action, answer, assessment));
    		} else {
    			if (prevIsHelpRequest && (!prevHelpRequestCell.equals(cell) || !prevHelpRequestLesson.equals(lesson))) {
    				clip.addAction(new HelpRequest(prevHelpRequestLesson, prevHelpRequestCell, prevHelpRequestTime, prevHelpRequestNumStep));
    			}
    			
    			prevIsHelpRequest = true;
    			
    			prevHelpRequestNumStep += numsteps_[currentID]+1;
    			prevHelpRequestTime += time;
    			prevHelpRequestLesson = lesson;
    			prevHelpRequestCell = cell;
    			
    			//double numSteps = numsteps_[currentID]+1;
    			//clip.addAction(new HelpRequest(lesson, cell, time, numSteps));
    		}
    		
    		if (actionCount != 0) {
    			// Don't add the time for the first action
    			
    			// Sometimes the time is negative, I don't know why ...
    			// XXX Need to talk to Ryan about this
    			
    			if (time >= 0) {
    				totalTime += time;
    			}  else {
    				
					// mark the clip as invalid and end it
					// clip.invalidate();
					// break;
    			}
    		}
			if (clipID == 1061) {
				System.out.println("clip time: "+ totalTime);
			}
    		actionCount++;
    	}
    	
    	if (prevIsHelpRequest) {
			clip.addAction(new HelpRequest(prevHelpRequestLesson, prevHelpRequestCell, prevHelpRequestTime, prevHelpRequestNumStep));
			prevIsHelpRequest = false;
		}
    	
    	clip.setNumberOfRowsInLogFile(actionCount);
    	
    	return clip;
    }

    public static void main (String args[]){
    	// System.out.println("______________testing______________");
    	boolean useCogModel = false;
    	CognitiveModel model = null;
    	List<Integer> modelClipIDs = new ArrayList<Integer>();
    	
		int clipID = -1;
		
		text_obs_PSLC_driverB mdd = new text_obs_PSLC_driverB();
		
		if (args[0].equals("algebra")){
		    mdd.action_data_file=mdd.a1_action_data_file;
		    mdd.max = 399158;    
		    mdd.repeatfile = "algebra_replay_nums";
		    mdd.lessonmapfile = "a1_lessonmapping.txt";
		    mdd.outfile_nothi = "observationsALG-";
		}
		else if (args[0].equals("geometry")){
		    mdd.action_data_file=mdd.geo_action_data_file; 
		    mdd.max = 197157;
		    mdd.repeatfile = "geometry_replay_nums";
		    mdd.lessonmapfile = "geo_lessonmapping.txt";
		    mdd.outfile_nothi = "observationsGEO-";
		}else
		    System.out.println("Unknown data set");
		
		mdd.coder_id = mdd.getIdentity();
		
		if (args.length >= 2 && args[1].equals("model")) {
			useCogModel = true;
			model = new CognitiveModel();
			
			try {
				// change this to testing or training?
				BufferedReader reader = new BufferedReader(new FileReader("../"+mdd.repeatfile+"_"+ mdd.coder_id));
				
				while (reader.ready()) {
					String line = reader.readLine();
					String id = line.substring(0, line.indexOf("\t"));
					modelClipIDs.add(new Integer(id));
				}
				reader.close();
			} catch (Exception e) {
				System.out.println("something has gone wrong!!!! in the catch statement. Exception: " + e);
			}
		}
		
		mdd.getLengthOfExisting();
		
		mdd.readInActions();	
		
		if (!useCogModel) {
			while (true){
				
				// For Natalie: This is not useful for you, you should be able to safely ignore it
			    clipID = mdd.getRepeatClip(mdd.totalex+1);
		
			    if (clipID == -1){
					System.out.println("Observations completed!");
					break;
			    }
			    
			    mdd.displayClip(clipID);
			    
			    mdd.getLengthOfExisting();
			}
		} else {
			try {
				
				// For Natalie: This part here is the most important for you
				
				// This is a mapping of the clip's starting index and the label for that clip
				Map<Integer, Boolean> clipClassifications = getClipClassifications();
				
				// This is used to generate a data file. If I remember correctly, this data file is an aggregate of the consituents (and other things) for each of the clips
				// You will want to generate a different data file, but I'm leaving this here in case it's useful as a starting point (and maybe to make sure that the existing code is working)
				FileWriter clipWriter = new FileWriter("clips-" + mdd.coder_id + ".csv");
				
				clipWriter.write("clipID,studentID," + Clip.getActionCountHeaderString() + "," + BehaviorInterpretation.getClipFileHeader() + "\n");
				// Note: this talks in terms of "training" clips because initially I had some training data and some testing data. You don't need to test anything here (and I removed the part of the code that did the test).
				Map<Clip, BehaviorInterpretation> trainingClipInterpretations = new HashMap<Clip, BehaviorInterpretation>();

				//creating output file for formatted data
				try {
					FileWriter featureOutput = new FileWriter("../featureOutput.txt");

					// creating the header line
					InterpretationElement e = new DidNotThinkBeforeHelpRequest();
					StringBuilder builder = new StringBuilder();
					builder.append("clip,num,action,");
					for (String i : e.colNames) {
						builder.append(i + ",");
					}
					builder.append("time\n");
					featureOutput.write(builder.toString());

					for (Integer id : modelClipIDs) {
						
						// This method takes the index has an input and creates the Clip object. Clips are generally 5 actions, but there are some exception
						Clip clip = mdd.getClip(id);

						

						// if (id == 373461) {
						// 	System.out.println(clip.getActions().toString());
						// }
						
						// featureOutput.write("______________________New interpretation______________________\n");
						
						// This takes the clip objects and generate's an interpretation. i.e., it generates the constituents and also generates a prediction (you don't need the prediction)
						BehaviorInterpretation interpretation = model.evaluateBehavior(clip);
						trainingClipInterpretations.put(clip, interpretation);
						
						// System.out.println(interpretation.getInterpretationString());
						featureOutput.write(interpretation.getInterpretationOther(id));
						clipWriter.write(id + "," + clip.getStudent() + "," + clip.getActionCountString() + "," + interpretation.getClipFileText(false) + "\n");
					}
					featureOutput.close();
				}
				catch (Exception e) {
					System.out.print("file error: " + e.toString());
				}
					
				clipWriter.flush();
				clipWriter.close();
				
			} catch (Exception e) {
				System.out.println("error when writing clip file, " + e.toString());
			}
			
			System.out.println("Observations completed!");
		}
    }

    private static int trainingGamingCount = 0;
    private static int trainingNonGamingCount = 0;
    
    private static int testGamingCount = 0;
    private static int testNonGamingCount = 0;
    
	private static Map<Integer, Boolean> getClipClassifications() {
		
		Map<Integer, Boolean> clipClassifications = new HashMap<Integer, Boolean>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("observationsALG-Adriana-combined"));
			
			while (reader.ready()) {
				String line = reader.readLine();
				String[] tokens = line.split(" ");
				if (tokens[0].equals("ADC")) {
					Integer clipID = Integer.parseInt(tokens[1]);
					if (tokens[2].equals("G")) {
						clipClassifications.put(clipID, true);
					} else if (tokens[2].equals("N")) {
						clipClassifications.put(clipID, false);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			
		}
		
		return clipClassifications;
	}
	
    private static void outputPatternForwardSelectionFile(List<Integer> modelClipIDs, Map<Integer, Map<Pattern, Integer>> patternMatchCounts, Map<Integer, Boolean> clipClassifications, List<Pattern> bestPatterns) throws IOException {
    	FileWriter forwardSelectionFileWriter = new FileWriter("patternsForwardSelection.csv");
    	forwardSelectionFileWriter.write("clipID");
    	
    	for (Pattern pattern : bestPatterns) {
    		forwardSelectionFileWriter.write("," + pattern.toString());
    	}
    	
    	forwardSelectionFileWriter.write("\n");
    	
    	for (Integer i : modelClipIDs) {
    		
    		if (!clipClassifications.containsKey(i)) {
    			continue;
    		}
    		
    		forwardSelectionFileWriter.write(i.toString());
    		
    		for (Pattern pattern : bestPatterns) {
    			
    			if (patternMatchCounts.get(i).containsKey(pattern)) {
    				forwardSelectionFileWriter.write("," + (patternMatchCounts.get(i).get(pattern) > 0 ? 1 : 0));
    			} else {
    				forwardSelectionFileWriter.write(",0");
    			}
        	}
        	
        	forwardSelectionFileWriter.write("\n");
    	}
    	
    	forwardSelectionFileWriter.flush();
    	forwardSelectionFileWriter.close();
	}
    
    private static void applyModelToAll(text_obs_PSLC_driverB mdd) {
    	
    	mdd.getLengthOfExisting();
		mdd.readInActions();
		
		CognitiveModel model = new CognitiveModel();
		
		// Read all the clips from the beginning
		int currentIndex = 0;
		List<Clip> clips = new ArrayList<>();
		
		while (currentIndex < mdd.numberOfActions) {
			
			// Need to make sure that the code to generate a clip doesn't generate clips that spans action from multiple students
			// Allow or don't allow clips that spans multiple different lessons?
			// Need to ensure that the clip creation process ends when there are no more actions available
			Clip clip = mdd.getClip(currentIndex);
			
			if (clip.isValid()) {
				clips.add(clip);
			}
			
			currentIndex += clip.getNumberOfRowsInLogFile();
		}
		
		List<BehaviorInterpretation> clipsInterpretation = new ArrayList<>();
		
		// For each clip, apply the cognitive model
		for (Clip clip: clips) {
			
			try {
				BehaviorInterpretation behavior = model.evaluateBehavior(clip);
				clipsInterpretation.add(behavior);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Output the result
		try {
			FileWriter output = new FileWriter("ModelApplicationToAllClips-Algebra.csv");
			
			// Write the header
			output.write("Clip ID,student,lesson,num actions in clip,num rows in log file,");
			output.write(BehaviorInterpretation.getClipFileHeader() + "\r\n");
			
			output.flush();
			
			for (BehaviorInterpretation interpretation: clipsInterpretation) {
				
				output.write(Integer.toString(interpretation.getClip().getID()));
				output.write("," + interpretation.getClip().getStudent());
				output.write("," + interpretation.getClip().getLesson());
				output.write("," + interpretation.getClip().getActions().size());
				output.write("," + interpretation.getClip().getNumberOfRowsInLogFile());
				output.write("," + interpretation.getClipFileText(true));
				output.write("\r\n");
				output.flush();
			}
			
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}

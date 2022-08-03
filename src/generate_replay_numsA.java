import java.io.*;
import java.util.*;

public class generate_replay_numsA{

    String midsch2002_action_data_file = "Midsch2002-TR-A.txt";

    int max = 0;

    String action_data_file = "";

    // must have quote marks around all inputs
    // eliminate MEEP lines
   
    String outfile_nothi = "observations";
    String repeatfile = "toredo";
    
    public String coder_id = "BOB";
    
    FileReader st_;
    FileWriter fw_;

    public int totalex = 0;
    int curchar = 0;

    public boolean lastofline = false;

    public boolean repeatOldFirst = true; // for second coder, etc.

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
	    StreamTokenizer st = new StreamTokenizer(new FileReader(repeatfile+coder_id));
	    int tt= StreamTokenizer.TT_NUMBER; int curclipnum = 0; int toret = -1;
	    while (tt!= StreamTokenizer.TT_EOF){
		tt = st.nextToken();
		if (tt == StreamTokenizer.TT_EOL)
		    curclipnum++;
		if ((tt == StreamTokenizer.TT_WORD)&&((st.sval.equals("N"))||(st.sval.equals("G"))))
		    curclipnum++;
		if (curclipnum == goalclip)
		    return toret;
		if (tt == StreamTokenizer.TT_NUMBER)
		    toret = (new Double(st.nval)).intValue();
	    }
	}
	catch (Exception fnfe){
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

    public int sparsityfactor(String lesson){
	// midsch 2002

	if (lesson.equals("3-DZVolumeZ"))
	    return 0;	
	if (lesson.equals("DecZArithZAZUni"))
	    return 90;	
	if (lesson.equals("DecZArithZBZUni"))
	    return 65;	
	if (lesson.equals("DecZArithZCZUni"))
	    return 94;	
	if (lesson.equals("DiagramZFractio"))
	    return 97;	
	if (lesson.equals("DiagramZSor"))
	    return 0;	
	if (lesson.equals("FractionZDivisionZUni"))
	    return 0;	
	if (lesson.equals("MidZSchoolZGeometr"))
	    return 60;	
	if (lesson.equals("MidZSchoolZNumberZLineZ"))
	    return 93;	
	if (lesson.equals("MidZSchoolZPictureZAlgebr"))
	    return 97;	
	if (lesson.equals("MidZSchoolZPlaceZValueZ"))
	    return 95;	
	if (lesson.equals("MidZSchoolZProbabilit"))
	    return 97;	
	if (lesson.equals("MidZSchZCommonZMultiplesZLC"))
	    return 90;	
	if (lesson.equals("MidZSchZFunctionZGenZ1ZUni"))
	    return 89;	
	if (lesson.equals("MidZSchZFunctionZGenZ2ZUni"))
	    return 96;	
	if (lesson.equals("MidZSchZFunctionZGenZ6-1ZUni"))
	    return 96;	
	if (lesson.equals("MidZSchZGraphZInterpre"))
	    return 88;	
	if (lesson.equals("MidZSchZSIFZModelin"))
	    return 96;	
	if (lesson.equals("MidZSchZSymbolicZModelingZ"))
	    return 90;	
	if (lesson.equals("NegativeZDecimalsZ1ZUni"))
	    return 91;	
	if (lesson.equals("NegativeZIntegersZ1ZUni"))
	    return 96;	
	if (lesson.equals("pictureZdivisio"))
	    return 92;	
	if (lesson.equals("PRECENTS-ALGEBRAZ1ZUni"))
	    return 94;	
	if (lesson.equals("PRECENTS-ALGEBRAZ2ZUni"))
	    return 84;	
	if (lesson.equals("ProportionalityZ1ZUni"))
	    return 90;	
	if (lesson.equals("ProportionalityZ2ZUni"))
	    return 89;	
	if (lesson.equals("ProportionalZReasoningZ1ZUni"))
	    return 91;	
	if (lesson.equals("Qual-Graph-InterpretationZUni"))
	    return 94;	
	if (lesson.equals("ScalingZ1ZUni"))
	    return 96;	
	if (lesson.equals("SimplifiedZDivisionZUni"))
	    return 67;	
	if (lesson.equals("SymbolicZModelingZwithZDisributionZ1ZUni"))
	    return 0;	

	if (lesson.equals("CompatZNumZ1ZUni"))
	    return 110; // eliminate, too rare
	if (lesson.equals("MidZSchoolZCombinatoricsZ"))
	    return 110; // eliminate, too rare

	return 80; // baseline
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
	    if ((curchar == 10)&&(!lastofline))
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


    String lesson_[] = new String[900000];
    String student_[] = new String[900000];
    String assess_[] = new String[900000];
    String cell_[] = new String[900000];
    String celltype_[] = new String[900000];
    String action_[] = new String[900000];
    String answer_[] = new String[900000];
    String prod_[] = new String[900000];
    double time_[] = new double[900000];
    double numsteps_[] = new double[900000];
    double helpintermedtime_[] = new double[900000]; 
	
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
	Random gen = new Random(); int sincelast = 0;
	try{
	    while ((curchar != -1)&&(num != max)){ 
	    

	    student = ""; assess = "";
	    cell = ""; celltype = ""; prod = "";
	    time = 0; answer = ""; action = "";
	    helpintermedtime = 0; numsteps = 0;

	    sincelast++;
	  	    
	    num = java.lang.Integer.parseInt(read_in_field());

	    lesson = read_in_field();
	    if (lesson.equals("")){
		break;
	    }
	    if (sincelast>10){
		if (gen.nextInt(100)>sparsityfactor(lesson)){
		    sincelast = 0;
		    System.out.print(num);
		    System.out.print("\t");
		    System.out.print(lesson);
		    System.out.print("\n");
		}
	    }
	    
	    read_in_field(); //problem
	    student = read_in_field();
	    assess = read_in_field();
	    cell = read_in_field();
	    action = read_in_field();
	    answer = read_in_field();
	    prod = read_in_field();
	    read_in_field();
	    time = java.lang.Double.parseDouble(read_in_field());
	    helpintermedtime = java.lang.Double.parseDouble(read_in_field()); 
	    lastofline = true;
	    numsteps = java.lang.Double.parseDouble(read_in_field()); 
	    lastofline = false;

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
	} 
	} catch (Exception e){e.printStackTrace();System.out.print("Exception reading in file! Line: ");System.out.println(num);curchar = -1;}
	System.out.println("100% Complete.");
    }
    
    public static void main (String args[]){
	int clipID = -1;
	
	generate_replay_numsA mdd = new generate_replay_numsA();
	
	if (args[0].equals("midsch2002")){
	    mdd.action_data_file=mdd.midsch2002_action_data_file; 
	    mdd.max = 872483;
	    }else
	     System.out.println("Unknown data set");
	
	mdd.readInActions();	
 
    }
}

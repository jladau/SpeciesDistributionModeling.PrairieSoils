package edu.ucsf.sdm;

import edu.ucsf.base.*;

public class Main {

	/** Fits model and outputs alpha-diversity map */
	
	public static void main(String rgsArgs[]){
		
		//sdm1 = sdm analysis object
		//rgs2 = arguments
		//ppm1 = PrintPredictionMap object
		//arg1 = arguments
		//rgsIndependentData = independent data set
		
		Arguments arg1;
		SDM sdm1 = null;
		PrintPredictionMap ppm1 = null;
		String rgs2[][]; String rgsIndependentData[][] = null;
		
		//loading arguments
		arg1 = new Arguments(rgsArgs);
		
		//waiting for file completion
		//FileIO.checkAndWaitForCompletion(arg1.getValueString("sDataPath").replace(".data", ".modl"));
		
		//loading sdm arguments
		if(arg1.getValueString("sAnalysisMode").equals("beta-diversity")){
			sdm1 = new SDM_BetaDiversity();
		}else if(arg1.getValueString("sAnalysisMode").equals("alpha-diversity")){
			sdm1 = new SDM_AlphaDiversity();
		}
		for(String s:arg1.mapAllArguments.keySet()){
			
			//loading argument
			sdm1.arg1.loadArgument(s,arg1.mapAllArguments.get(s));
		}
		
		//loading arguments from file if appropriate
		if(arg1.mapAllArguments.containsKey("sPathArguments")){
			rgs2 = FileIO.readFile(arg1.getValueString("sPathArguments"), "=",2); 
			for(int i=0;i<rgs2.length;i++){
				
				//loading argument
				sdm1.arg1.loadArgument(rgs2[i][0],rgs2[i][1]);
			}
		}
		
		//loading data
		sdm1.loadObservationalData();
		sdm1.loadRasterData();
		
		//initializing SDMAnalysis object
		if(sdm1 instanceof SDM_BetaDiversity){
			ppm1 = new PrintPredictionMap_BetaDiversity(sdm1);
		}else if(sdm1 instanceof SDM_AlphaDiversity){
			ppm1 = new PrintPredictionMap_AlphaDiversity(sdm1);
		}
			
		//clearing data (to save memory)
		if(arg1.getValueBoolean("bPrintCrossValidation")==false){
			sdm1.obs1.closeData();
		}
			
		//loading independent data set
		rgsIndependentData = FileIO.readFile(sdm1.arg1.sPathIndependentData, ","); 
		
		//loading predictions object
		sdm1.loadPredictions();
		
		//running cross validation
		if(arg1.getValueBoolean("bPrintCrossValidation")==true){
			try{
				ppm1.printCrossValidation(sdm1);
			}catch(Exception e){
				arg1.printArguments(sdm1.arg1.sPathLog, true, "DrawMap");
				FileIO.writeCompletionFile(sdm1.arg1.sMapPath);
				System.out.println("Done.");
				return;
			}
		}
		
		//outputting independent validation
		if(rgsIndependentData!=null){
			FileIO.writeFile(sdm1.prd1.predictToIndependentDataSet(rgsIndependentData), sdm1.arg1.sPathIndependentValidation, 0, false);
		}
			
		//outputting map
		try{
			ppm1.printMap(arg1.getValueInt("iTaskID"),arg1.getValueInt("iTotalTasks"));
		}catch(Exception e){
			arg1.printArguments(sdm1.arg1.sPathLog, true, "DrawMap");
			FileIO.writeCompletionFile(sdm1.arg1.sMapPath);
			System.out.println("Done.");
			return;
		}
			
		//outputting estimate coefficients
		FileIO.writeFile(sdm1.mdl1.printFittedModel(), sdm1.arg1.sPathCoefficients, 0, false);
				
		//terminating
		arg1.printArguments(sdm1.arg1.sPathLog, true, "DrawMap");
		//FileIO.writeCompletionFile(sdm1.arg1.sMapPath);
		System.out.println("Done.");
	}
}
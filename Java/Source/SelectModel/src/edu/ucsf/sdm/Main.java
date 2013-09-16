package edu.ucsf.sdm;

import java.util.ArrayList;

import edu.ucsf.base.*;

/**
 * This code runs model selection.
 * @author jladau
 */
public class Main {

	public static void main(String rgsArgs[]){
		
		//sResponse = response variable
		//slm1 = SelectModel object
		//rgsOut = output
		//iRow = output row
		//sModel = current model
		//sProgress = progress update
		//sdm1 = sdm object
		//arg1 = arguments
		//lstVars = list of variables
		
		Arguments arg1;
		String rgsOut[][];
		String sResponse = null; String sModel; String sProgress; 
		int iRow; 
		SelectModel slm1;
		SDM sdm1 = null;
		ArrayList<String> lstVars;
		
		//loading arguments
		arg1 = new Arguments(rgsArgs);
		if(arg1.getValueString("sAnalysisMode").equals("beta-diversity")){
			sdm1 = new SDM_BetaDiversity();
		}else if(arg1.getValueString("sAnalysisMode").equals("alpha-diversity")){
			sdm1 = new SDM_AlphaDiversity();
		}
		for(String s:arg1.mapAllArguments.keySet()){
			
			//loading argument
			sdm1.arg1.loadArgument(s,arg1.mapAllArguments.get(s));
		}
		
		//waiting until mess file is complete
		//FileIO.checkAndWaitForCompletion(sdm1.arg1.sMESSPath);
		
		//loading observational data
		sdm1.loadObservationalData();
		sdm1.obs1.loadCandidateCovariates();
		sdm1.loadModel();
		
		//loading response and output suffix
		sResponse = sdm1.arg1.sResponse;
		
		//outputting progress
		sProgress = "RESPONSE VARIABLE '" + sResponse + "'";
		System.out.println(sProgress);
		System.out.println(createBreak(sProgress.length()));
		
		//initializing SelectModel object
		slm1 = new SelectModel(sdm1, false, arg1.getValueInt("iTaskID"), arg1.getValueInt("iTotalTasks"));
		
		//outputting results
		rgsOut = new String[3*(sdm1.arg1.iMaxVars+1)+1][1];
		rgsOut[0][0] = "RESPONSE_VARIABLE,PREDICTORS,NUMBER_OF_PARAMETERS,STATISTIC,VALUE";
		iRow=0;
		for(int i=0;i<=sdm1.arg1.iMaxVars;i++){
			iRow++;
			sModel = slm1.getBestModelPRESS(i);
			sModel = sResponse + "," + sModel.replace(",", ";");
			rgsOut[iRow][0]= sModel + "," + i + ",PRESS," + slm1.getPRESS(slm1.getBestModelPRESS(i));
			iRow++;
			sModel = slm1.getBestModelR2(i);
			sModel = sResponse + "," + sModel.replace(",", ";");
			rgsOut[iRow][0]= sModel + "," + i + ",R^2," + slm1.getR2(slm1.getBestModelR2(i));
			iRow++;
			sModel = slm1.getBestModelAdjustedR2(i);
			sModel = sResponse + "," + sModel.replace(",", ";");
			rgsOut[iRow][0]= sModel + "," + i + ",Adjusted R^2," + slm1.getAdjustedR2(slm1.getBestModelAdjustedR2(i),(double) i);
		}
		
		//outputting result
		if(arg1.getValueInt("iTaskID")==-9999){
			//FileIO.writeFile(rgsOut, sdm1.arg1.sDirOutput + "/SelectModel_Output_" + sOutputSuffix + ".csv", "", 0, false);
			FileIO.writeFile(rgsOut, sdm1.arg1.sModelPath, "", 0, false);
		}else{
			//FileIO.writeFile(rgsOut, sdm1.arg1.sDirOutput + "/SelectModel_Output_" + sOutputSuffix + "_" + arg1.getValueInt("iTaskID") + ".csv", "", 0, false);
			FileIO.writeFile(rgsOut, sdm1.arg1.sModelPath + "_" + arg1.getValueInt("iTaskID"), "", 0, false);	
		}
		
		//outputting candidate variables
		lstVars = new ArrayList<String>();
		lstVars.add("CANDIDATE VARIABLES");
		lstVars.add("-------------------");
		for(int i=0;i<sdm1.obs1.rgsCandidateVars.length;i++){
			lstVars.add(sdm1.obs1.rgsCandidateVars[i]);
		}
		//outputting result
		if(arg1.getValueInt("iTaskID")==-9999){
			//FileIO.writeFile(rgsOut, sdm1.arg1.sDirOutput + "/SelectModel_Output_" + sOutputSuffix + ".csv", "", 0, false);
			FileIO.writeFile(lstVars, sdm1.arg1.sVarsPath, 0, false);
			FileIO.writeCompletionFile(sdm1.arg1.sModelPath);
		}else{
			//FileIO.writeFile(rgsOut, sdm1.arg1.sDirOutput + "/SelectModel_Output_" + sOutputSuffix + "_" + arg1.getValueInt("iTaskID") + ".csv", "", 0, false);
			FileIO.writeFile(lstVars, sdm1.arg1.sVarsPath + "_" + arg1.getValueInt("iTaskID"), 0, false);	
			FileIO.writeCompletionFile(sdm1.arg1.sModelPath + "_" + arg1.getValueInt("iTaskID"));
		}
		
		//terminating
		if(arg1.getValueInt("iTaskID")==-9999 || arg1.getValueInt("iTotalTasks")==-9999 || (arg1.getValueInt("iTaskID")==1 && arg1.getValueInt("iTotalTasks")!=-9999)){
			arg1.printArguments(sdm1.arg1.sPathLog, true, "SelectModel");
		}
		System.out.println("Done.");
	}
	
	private static String createBreak(int iLength){
		
		StringBuilder sbl1;
		
		sbl1 = new StringBuilder();
		for(int i=0;i<iLength;i++){
			sbl1.append("-");
		}
		return sbl1.toString();
	}
}

package edu.ucsf.sdm;

import edu.ucsf.base.*;

/**
 * Runs SDM analysis with given inputs.
 * @author jladau
 */
public abstract class PrintPredictionMap {

	//sdm1 = arguments
	
	protected SDM sdm1;
	
	/**
	 * Constructor
	 */
	public PrintPredictionMap(SDM sdm1){
		
		//loading arguments
		this.sdm1 = sdm1;
		
		//loading model
		sdm1.loadModel();
	}
	
	/**
	 * Runs cross validation and outputs result
	 */
	public void printCrossValidation(SDM sdm1){
		
		//running cross validation for lm richness model
		sdm1.mdl1.runCrossValidation(sdm1.arg1.sResponse, sdm1.arg1.rgsPredictors);
		//FileIO.writeFile(sdm1.mdl1.findPredictions(), sdm1.arg1.sDirOutput + "/BetaDiversityLM_CrossValidation_" + sdm1.arg1.lstSuffixes.get(iModelIndex) + ".csv", ",", 0, false);
		FileIO.writeFile(sdm1.mdl1.findPredictions(), sdm1.arg1.sCrossvalidationPath, ",", 0, false);
	}
	
	/**
	 * Prints map for specified model
	 * @param iTaskID Task ID for parallel processing; if -9999 complete map is output
	 * @param iTotalTasks Total number of tasks
	 */
	public abstract void printMap(int iTaskID, int iTotalTasks);
	
	/**
	 * Prints non-vector map
	 */
	public void printNonVectorMap(int iTaskID, String sName){
		
		//cdfWriter = output cdf
		
		NetCDF_IO cdfWriter;
		
		//fitting model
		sdm1.mdl1.fitModel(sdm1.arg1.sResponse, sdm1.arg1.rgsPredictors);
		sdm1.mdl1.loadCoefficients();
		
		//initializing writers
		//cdfWriter = new NetCDF_IO(sdm1.arg1.sDirOutput + "/" + sName + "_" + sdm1.arg1.lstSuffixes.get(iModelIndex) + ".nc","writing");
		cdfWriter = new NetCDF_IO(sdm1.arg1.sMapPath,"writing");
		cdfWriter.initializeWriter(0.5, "Meters", sdm1.arg1.lstElevationsCDF, "Month", sdm1.arg1.lstTimesCDF, sdm1.arg1.sResponse, "");
		
		//outputting richness map
		for(int i=0;i<sdm1.arg1.lstTimes.size();i++){
			
			//loading predictions
			sdm1.prd1.loadMap(i, iTaskID);
			
			//outputting predictions
			cdfWriter.writeGrid(sdm1.prd1.rgdMap, sdm1.arg1.lstElevations.get(i), sdm1.arg1.lstTimes.get(i));
		}
		
		//closing writer
		cdfWriter.closeWriter();	
	}

	/**
	 * Prints non-vector map
	 * @param iTaskID Task ID
	 * @param iTotalTasks Total number of tasks
	 */
	public void printNonVectorMapPartial(int iTaskID, int iTotalTasks, String sName){
		
		//fitting model
		sdm1.mdl1.fitModel(sdm1.arg1.sResponse, sdm1.arg1.rgsPredictors);
		sdm1.mdl1.loadCoefficients();
		
		//outputting maps
		for(int i=0;i<sdm1.arg1.lstTimes.size();i++){
			
			//loading predictions
			sdm1.prd1.loadPartialMap(i, iTaskID, iTotalTasks);
			
			//outputting predictions
			if(i==0){
				//FileIO.writeFile(sdm1.prd1.lstMap, sdm1.arg1.sDirOutput + "/" + sName + "_" + sdm1.arg1.lstSuffixes.get(iModelIndex) + "_" + iTaskID + ".csv", 0, false);
				FileIO.writeFile(sdm1.prd1.lstMap, sdm1.arg1.sMapPath + "_" + iTaskID + ".csv", 0, false);
			}else{
				//FileIO.writeFile(sdm1.prd1.lstMap, sdm1.arg1.sDirOutput + "/" + sName + "_" + sdm1.arg1.lstSuffixes.get(iModelIndex) + "_" + iTaskID + ".csv", 0, true);
				FileIO.writeFile(sdm1.prd1.lstMap, sdm1.arg1.sMapPath + "_" + iTaskID + ".csv", 0, true);
			}
		}
	}
	
}
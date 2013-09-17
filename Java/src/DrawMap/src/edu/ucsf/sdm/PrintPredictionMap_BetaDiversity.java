package edu.ucsf.sdm;

import edu.ucsf.base.*;

/**
 * Runs SDM analysis with given inputs.
 * @author jladau
 */
public class PrintPredictionMap_BetaDiversity extends PrintPredictionMap{

	/**
	 * Constructor
	 */
	public PrintPredictionMap_BetaDiversity(SDM sdm1){
		super(sdm1);
	}
	
	/**
	 * Prints map for specified model
	 * @param iTaskID Task ID for parallel processing; if -9999 complete map is output
	 * @param iTotalTasks Total number of tasks
	 */
	public void printMap(int iTaskID, int iTotalTasks){
		
		//complete map being output
		if(iTaskID==-9999){
		
			//checking if vector map
			if(((SDMArguments_BetaDiversity) sdm1.arg1).sMapType.equals("vector")){
				printVectorMap(iTaskID);
			}else{
				printNonVectorMap(iTaskID,"BetaDiversity");
			}
			
		//partial map being output	
		}else{
			
			//checking if vector map
			if(((SDMArguments_BetaDiversity) sdm1.arg1).sMapType.equals("vector")){
				printVectorMapPartial("minimum",iTaskID,iTotalTasks);
			}else{
				printNonVectorMapPartial(iTaskID,iTotalTasks,"BetaDiversity");
			}
		}
	}
	
	/**
	 * Prints map with given predictor values
	 * @param rgsPredValues Predictor values
	 */
	private void printVectorMap(int iTaskID){
		
		//cdfWriterX = output cdf for X
		//cdfWriterY = output cdf for Y
		
		NetCDF_IO cdfWriterX; NetCDF_IO cdfWriterY;
			
		//fitting model
		sdm1.mdl1.fitModel(sdm1.arg1.sResponse, sdm1.arg1.rgsPredictors);
		sdm1.mdl1.loadCoefficients();
				
		//initializing writers
		cdfWriterX = new NetCDF_IO(sdm1.arg1.sDirOutput + "/BetaDiversity_VectorX_" + sdm1.arg1.sResponse + ".nc","writing");
		cdfWriterX.initializeWriter(0.5, "Meters", sdm1.arg1.lstElevationsCDF, "Month", sdm1.arg1.lstTimesCDF, sdm1.arg1.sResponse, "");
		
		cdfWriterY = new NetCDF_IO(sdm1.arg1.sDirOutput + "/BetaDiversity_VectorY_" + sdm1.arg1.sResponse + ".nc","writing");
		cdfWriterY.initializeWriter(0.5, "Meters", sdm1.arg1.lstElevationsCDF, "Month", sdm1.arg1.lstTimesCDF, sdm1.arg1.sResponse, "");
		
		//outputting maps
		for(int i=0;i<sdm1.arg1.lstTimes.size();i++){
			
			//loading predictions
			sdm1.prd1.loadMap(i, iTaskID);
			cdfWriterX.writeGrid(((SDMPrediction_BetaDiversity) sdm1.prd1).rgdMapX, sdm1.arg1.lstElevations.get(i), sdm1.arg1.lstTimes.get(i));
			cdfWriterY.writeGrid(((SDMPrediction_BetaDiversity) sdm1.prd1).rgdMapY, sdm1.arg1.lstElevations.get(i), sdm1.arg1.lstTimes.get(i));
		}
		
		//closing writer
		cdfWriterX.closeWriter();
		cdfWriterY.closeWriter();
	}
	
	/**
	 * Prints vector map
	 * @param iTaskID Task ID
	 * @param iTotalTasks Total number of tasks
	 */
	private void printVectorMapPartial(String sDirection, int iTaskID, int iTotalTasks){
		
		//fitting model
		sdm1.mdl1.fitModel(sdm1.arg1.sResponse, sdm1.arg1.rgsPredictors);
		sdm1.mdl1.loadCoefficients();
		
		//outputting maps
		for(int i=0;i<sdm1.arg1.lstTimes.size();i++){
			
			//loading predictions
			sdm1.prd1.loadPartialMap(i, iTaskID, iTotalTasks);
			
			//outputting predictions
			if(i==0){
				FileIO.writeFile(((SDMPrediction_BetaDiversity) sdm1.prd1).lstMapX, sdm1.arg1.sDirOutput + "/BetaDiversity_VectorX_" + sdm1.arg1.sResponse + "_" + iTaskID + ".csv", 0, false);
				FileIO.writeFile(((SDMPrediction_BetaDiversity) sdm1.prd1).lstMapY, sdm1.arg1.sDirOutput + "/BetaDiversity_VectorY_" + sdm1.arg1.sResponse + "_" + iTaskID + ".csv", 0, false);
			}else{
				FileIO.writeFile(((SDMPrediction_BetaDiversity) sdm1.prd1).lstMapX, sdm1.arg1.sDirOutput + "/BetaDiversity_VectorX_" + sdm1.arg1.sResponse + "_" + iTaskID + ".csv", 0, true);
				FileIO.writeFile(((SDMPrediction_BetaDiversity) sdm1.prd1).lstMapY, sdm1.arg1.sDirOutput + "/BetaDiversity_VectorY_" + sdm1.arg1.sResponse + "_" + iTaskID + ".csv", 0, true);
			}
		}
	}
}
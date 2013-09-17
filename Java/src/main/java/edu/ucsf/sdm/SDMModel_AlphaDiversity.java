package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.ucsf.base.FitLM_Apache;
import edu.ucsf.base.RasterLocation;

/**
 * CModel object for beta-diversity niche modeling.
 * @author jladau
 */
public class SDMModel_AlphaDiversity extends SDMModel{

	//obs1 = ObservationalData object
	//ras1 = RasterData object
	//arg1 = Arguments object

	private SDMObservationalData_AlphaDiversity obs1;
	private SDMRasterData_AlphaDiversity ras1;
	private SDMArguments_AlphaDiversity arg1;
	
	/**
	 * Constructor
	 * @param obs1 ObservationalData object
	 */
	public SDMModel_AlphaDiversity(SDMObservationalData_AlphaDiversity obs1, SDMRasterData_AlphaDiversity ras1, SDMArguments_AlphaDiversity arg1){
		
		//initializing superclass
		super(obs1.rgsData);
		
		//saving data
		this.obs1 = obs1;
		this.ras1 = ras1;
		this.arg1 = arg1;
	}
		
	/**
	 * Finds the prediction at the specified pair of locations
	 * @param rsl1 Raster location
	 * @return Predicted value
	 */
	public double findPrediction(RasterLocation rsl1){
		
		//s1 = predictor values
		
		String s1;
		
		//loading predictor values
		s1 = ras1.getRasterValue(rsl1);
		
		//checking for error
		if(s1.equals("-9999")){
			return -9999;
		}
		
		//**********************
		//if(this.findPrediction(s1)==-9999){
		//	System.out.println("HERE");
		//}
		//**********************
		
		
		//returning predicted distance
		return this.findPrediction(s1);
	}

	/**
	 * Fits model with given response variable and predictors. Data are loaded first for given model.   
	 * @param sResponse Response variable name.
	 * @param rgsPredictors Predictor names.		
	 */
	public void runCrossValidation(String sResponse, String[] rgsPredictors){
	
		//lstDataTruncated = data set with all rows containing current samples removed
		//rgdX = values of environmental variables where prediction is to be made
		//flm2 = fitLM object being used for cross validation
		//dObservation = current observation
		//rgd1 = current predicted value
		
		ArrayList<double[]> lstDataTruncated;
		double rgd1[]; double rgdX[][];
		FitLM_Apache flm2;
		double dObservation;
		
		//checking if null case
		if(rgsPredictors==null){
			return;
		}
		
		//loading non-cross validation results
		this.fitModel(sResponse, rgsPredictors);
		
		//saving press statistic
		dPRESS = this.findPRESS();
		
		//initializing predictions
		lstPredictions = new ArrayList<String>();
		lstPredictions.add("PREDICTED,OBSERVED");
		for(int i=1;i<obs1.rgsData.length;i++){
			
			//loading truncated data set and prediction row
			lstDataTruncated = new ArrayList<double[]>();
			for(int k=1;k<obs1.rgsData.length;k++){
				if(k!=i){
					lstDataTruncated.add(obs1.rgdData[k-1]);
				}
			}
			
			//loading cross validation modeling object
			flm2 = new FitLM_Apache(lstDataTruncated, this.getColumnMap());
			flm2.fitModel(sResponse, rgsPredictors);

			//loading environmental values for prediction
			rgdX = this.findPredictors(i, rgsPredictors, sResponse);
			
			//loading observed response for prediction
			dObservation = this.findObservation(i, sResponse);
			
			//loading prediction for left out pair
			rgd1 = flm2.findPrediction(rgdX);
			
			//updating predictions list
			lstPredictions.add(rgd1[0] + "," + dObservation);
		}
	}
	
	public double getPRESS(){
		return dPRESS;
	}
}

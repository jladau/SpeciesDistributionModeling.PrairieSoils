package edu.ucsf.sdm;

import java.util.ArrayList;

import edu.ucsf.base.FitLM_Apache;

/**
 * CModel object for beta-diversity niche modeling.
 * @author jladau
 */
public abstract class SDMModel extends FitLM_Apache{

	//dPRESS = press statistic
	//lstPredictions = observed and predicted values
	
	public double dPRESS;
	public ArrayList<String> lstPredictions;
	
	/**
	 * Constructor
	 */
	public SDMModel(String rgsData[][]){
		
		//initializing superclass
		super(rgsData);
	}
	
	public String[][] findPredictions(){
		
		//rgs1 = output
		
		String rgs1[][];
		
		rgs1 = new String[lstPredictions.size()][1];
		for(int i=0;i<lstPredictions.size();i++){
			rgs1[i][0]=lstPredictions.get(i);
		}
		return rgs1;
	}
	
	/**
	 * Fits model with given response variable and predictors. Data are loaded first for given model.   
	 * @param sResponse Response variable name.
	 * @param rgsPredictors Predictor names.		
	 */
	public abstract void runCrossValidation(String sResponse, String[] rgsPredictors);
}

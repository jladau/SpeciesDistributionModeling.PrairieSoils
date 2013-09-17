package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.ucsf.base.FileIO;

/**
 * Community observation data for beta diversity analysis
 * @author jladau
 */
public abstract class SDMObservationalData {

	//rgdData = data
	//rgsData = data
	//mapMinimum(sPredictor) = returns the minimum observed value of predictor
	//mapMaximum(sPredictor) = returns the maximum observed value of predictor
	//mapRange(sPredictor) = returns the observed range of predictor
	//rgsCandidateVars = candidate covariates
	//lstCandidateVars = list of candidate covariates
	//arg1 = Arguments_BetaDiversity object
	
	public Map<String,Double> mapMinimum;
	public Map<String,Double> mapMaximum;
	public Map<String,Double> mapRange;
	public String[][] rgsData;
	public double[][] rgdData;
	public ArrayList<String> lstCandidateVars;
	public String[] rgsCandidateVars;
	public SDMArguments arg1;
	
	/**
	 * Constructor
	 */
	public SDMObservationalData(SDMArguments arg1){
		this.arg1 = arg1;
		loadData();
	}
	
	public void closeData(){
		rgsData = null;
	}

	/**
	 * Loads candidate covariates list
	 */
	public void loadCandidateCovariates(){
		
		//rgs1 = file
		//mapMaxProportion(sVariable,dProportion) = returns the maximum observed proportion for the given variable
		//mapMinArea(sVariable,dArea) = returns the minimum area of data for given variable (in proportion format)
		//dProportion = current proportion
		//dArea = current area
		//sVariable = current variable
		
		String rgs1[][];
		Map<String,Double> mapMaxProportion; Map<String,Double> mapMinArea;
		double dProportion; double dArea;
		String sVariable;
		
		//initializing maps
		mapMaxProportion = new HashMap<String,Double>();
		mapMinArea = new HashMap<String,Double>();
			
		//loading file
		rgs1 = FileIO.readFile(arg1.sMESSPath,",");
		
		//looping through lines and updating results
		for(int k=1;k<rgs1.length;k++){
			sVariable = rgs1[k][0];
			dProportion = Double.parseDouble(rgs1[k][2]);
			dArea = Double.parseDouble(rgs1[k][1]);
			mapMaxProportion.put(sVariable, dProportion);
			mapMinArea.put(sVariable, dArea);
		}
		
		//initializing list of candidate covariates
		lstCandidateVars = new ArrayList<String>();
		
		//loading list of candidate vars
		for(String s:mapMaxProportion.keySet()){
			
			if(mapMaxProportion.get(s)<arg1.dMESSCutoff && mapMinArea.get(s)>0.85){
				lstCandidateVars.add(s);
			}
		}
		
		//loading array
		this.loadCandidateCovariateArray();
	}

	public void loadRanges(){
		
		//dValue = current value
		
		double dValue;
		String sPredictor;
		
		//initializing arrays
		mapMinimum = new HashMap<String,Double>();
		mapMaximum = new HashMap<String,Double>();
		mapRange = new HashMap<String,Double>();
		
		//looping through data columns
		for(int j=0;j<this.rgsData[0].length;j++){
			sPredictor = rgsData[0][j];
			mapMinimum.put(sPredictor, 99999999999999.);
			mapMaximum.put(sPredictor, -99999999999999.);
			mapRange.put(sPredictor, -9999.);
		}
		
		//loading values
		for(int j=0;j<this.rgsData[0].length;j++){
			for(int i=1;i<rgsData.length;i++){
				try{
					dValue = Double.parseDouble(rgsData[i][j]);
				}catch(Exception e){
					dValue=-9999;
				}
				if(dValue!=-9999){
					if(mapMinimum.get(this.rgsData[0][j])>dValue){
						mapMinimum.put(this.rgsData[0][j], dValue);
					}
					if(mapMaximum.get(this.rgsData[0][j])<dValue){
						mapMaximum.put(this.rgsData[0][j], dValue);
					}
				}
			}
			mapRange.put(this.rgsData[0][j], mapMaximum.get(this.rgsData[0][j])-mapMinimum.get(this.rgsData[0][j]));
		}
	}
	
	/**
	 * Loads array of candidate covariates. Removes variables that have errors in data file.
	 */
	abstract protected void loadCandidateCovariateArray();

	/**
	 * Loads data file
	 */
	abstract protected void loadData();
}

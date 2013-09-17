package edu.ucsf.sdm;

import edu.ucsf.base.FileIO;

/**
 * Community observation data for alpha diversity analysis
 * @author jladau
 */

//TODO Update this class with alpha-diversity methods

public class SDMObservationalData_AlphaDiversity extends SDMObservationalData{

	/**
	 * Constructor
	 */
	public SDMObservationalData_AlphaDiversity(SDMArguments_AlphaDiversity arg1){
		super(arg1);
	}
	
	/**
	 * Loads array of candidate covariates. Removes variables that have errors in data file.
	 */
	protected void loadCandidateCovariateArray(){
		
		//removing variables that have errors in data file
		for(int j=0;j<rgsData[0].length;j++){
			
			//checking if variable is in list
			if(lstCandidateVars.contains(rgsData[0][j])){
				
				//checking for errors
				for(int i=1;i<rgsData.length;i++){
					if(Double.parseDouble(rgsData[i][j])==-9999){
						lstCandidateVars.remove(rgsData[0][j]);
						break;
					}
				}
			}
		}
		
		//initializing array of variables
		rgsCandidateVars = new String[lstCandidateVars.size()];
		for(int i=0;i<lstCandidateVars.size();i++){
			rgsCandidateVars[i]=lstCandidateVars.get(i);
		}
	}

	/**
	 * Loads data file
	 */
	protected void loadData(){
		
		//d1 output double
		
		double d1;
		
		//loading data and updating headers
		rgsData = FileIO.readFile(arg1.sPathData, ",");
		for(int j=0;j<rgsData[0].length;j++){
			if(rgsData[0][j].contains("Raster")){
				rgsData[0][j]=rgsData[0][j].replace("Raster", "");
			}
		}
		
		//saving data
		rgdData = new double[rgsData.length-1][rgsData[0].length];
		for(int j=0;j<rgsData[0].length;j++){
			for(int i=1;i<rgsData.length;i++){
				
				//loading data in double format
				try{
					d1 = Double.parseDouble(rgsData[i][j]);
				}catch(Exception e){
					d1=-9999;
				}
				rgdData[i-1][j]=d1;
			}
		}
	}
}

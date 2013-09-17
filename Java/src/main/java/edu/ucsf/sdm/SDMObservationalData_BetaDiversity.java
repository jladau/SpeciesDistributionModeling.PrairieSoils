package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.ucsf.base.FileIO;
import edu.ucsf.base.RasterLocation;

/**
 * Community observation data for beta diversity analysis
 * @author jladau
 */
public class SDMObservationalData_BetaDiversity extends SDMObservationalData{

	//rgsDataSingleLocation = single location data
	//mapSampleRows(sSample1,sSample2) = returns a list of rows of data having neither sample
	//mapSampleLocation(sSample) = returns the location of the specified sample
	//mapSingleLocationMinimum(sPredictor) = returns the minimum observed value of the predictor across samples
	//mapSingleLocationMaximum(sPredictor) = returns the maximum observed value of the predictor across samples
	
	public Map<String,ArrayList<Integer>> mapSampleRows;
	public Map<String,RasterLocation> mapSampleLocation;
	public Map<String,Double> mapSingleLocationMinimum;
	public Map<String,Double> mapSingleLocationMaximum;
	public String rgsDataSingleLocation[][];
	
	/**
	 * Constructor
	 */
	public SDMObservationalData_BetaDiversity(SDMArguments_BetaDiversity arg1){
		super(arg1);
	}
	
	/**
	 * Loads single location data (for novelty analysis).
	 */
	@Deprecated
	public void loadSingleLocationData(){
		
		//rsl1 = raster location
		//sName = name of current variable
		//dValue = current value
		//dMin = current minimum
		//dMax = current maximum
		
		RasterLocation rsl1;
		String sName;
		double dValue; double dMin; double dMax;
		
		//loading data
		rgsDataSingleLocation = FileIO.readFile(((SDMArguments_BetaDiversity) arg1).sSingleLocationDataPath, ",");
		
		//initializing maps
		mapSampleLocation = new HashMap<String,RasterLocation>();
		mapSingleLocationMinimum = new HashMap<String,Double>();
		mapSingleLocationMaximum = new HashMap<String,Double>();
		for(int j=0;j<rgsDataSingleLocation[0].length;j++){
			
			//checking if raster variable
			if(!rgsDataSingleLocation[0][j].endsWith("Raster")){
				continue;
			}
			
			//loading name
			sName = rgsDataSingleLocation[0][j].replace("Raster", "");
			
			//initializing variable
			dMin = 9999999999999999.;
			dMax = -9999999999999999.;
			
			//loading minimum and maximum
			for(int i=1;i<rgsDataSingleLocation.length;i++){
				
				//loading value
				dValue = Double.parseDouble(rgsDataSingleLocation[i][j]);
				
				//checking if minimum or maximum
				if(dValue<dMin && dValue!=-9999){
					dMin = dValue;
				}
				if(dValue>dMax && dValue!=-9999){
					dMax = dValue;
				}
			}
			
			//initializing variable
			mapSingleLocationMinimum.put(sName, dMin);
			mapSingleLocationMaximum.put(sName, dMax);
		}
		
		//looping through values
		for(int i=1;i<rgsDataSingleLocation.length;i++){
			
			//loading location
			rsl1 = new RasterLocation(Double.parseDouble(rgsDataSingleLocation[i][3]),Double.parseDouble(rgsDataSingleLocation[i][2]),Double.parseDouble(rgsDataSingleLocation[i][4]),Double.parseDouble(rgsDataSingleLocation[i][6]),-9999,-9999,"-9999");
			
			//saving location
			mapSampleLocation.put(rgsDataSingleLocation[i][0], rsl1);
		}
	}
	
	/**
	 * Loads array of candidate covariates. Removes variables that have errors in data file.
	 */
	protected void loadCandidateCovariateArray(){
		
		//lst1 = contains variables for list
		
		ArrayList<String> lst1;
		
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
		
		//loading list of variables
		lst1 = new ArrayList<String>();
		for(int i=0;i<lstCandidateVars.size();i++){
			
			if(((SDMArguments_BetaDiversity) arg1).sDirectionality.equals("directional") && (lstCandidateVars.get(i).endsWith("_Diff") || lstCandidateVars.get(i).endsWith("_Max"))){
				lst1.add(lstCandidateVars.get(i));
			}
			if(((SDMArguments_BetaDiversity) arg1).sDirectionality.equals("nondirectional") && (lstCandidateVars.get(i).endsWith("_AbsDiff") || lstCandidateVars.get(i).endsWith("_Max"))){
				lst1.add(lstCandidateVars.get(i));
			}
		}
		
		//initializing array of variables
		rgsCandidateVars = new String[lst1.size()];
		for(int i=0;i<lst1.size();i++){
			rgsCandidateVars[i]=lst1.get(i);
		}
	}

	/**
	 * Loads data file
	 */
	protected void loadData(){
		
		//d1 output double
		//sSample1 = current first sample
		//sSample2 = current second sample
		//lst1 = current list being added to mapSampleRows
		
		double d1;
		String sSample1; String sSample2;
		ArrayList<Integer> lst1;
		
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
		
		//loading sample rows map	
		mapSampleRows = new HashMap<String,ArrayList<Integer>>();
		
		//loading list of rows of data
		for(int i=1;i<rgsData.length;i++){
			
			//loading current pair of samples
			sSample1 = rgsData[i][0];
			sSample2 = rgsData[i][1];
			
			//loading rows
			lst1 = new ArrayList<Integer>(rgsData.length);
			for(int k=1;k<rgsData.length;k++){
				
				//updating truncated data
				if(!rgsData[k][0].equals(sSample1) && !rgsData[k][0].equals(sSample2)){
					if(!rgsData[k][1].equals(sSample1) && !rgsData[k][1].equals(sSample2)){
						lst1.add(k-1);
					}
				}
			}
			
			//saving rows
			mapSampleRows.put(sSample1 + "," + sSample2, lst1);
		}
	}
}

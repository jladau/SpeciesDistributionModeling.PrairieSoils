package edu.ucsf.sdm;

import java.util.HashMap;
import java.util.Map;

import edu.ucsf.base.NetCDF_IO;
import edu.ucsf.base.PointOnLand;
import edu.ucsf.base.RasterIterator;
import edu.ucsf.base.RasterLocation;

/**
 * Raster data for beta diversity analysis
 * @author jladau
 */
public class SDMRasterData {

	//mapCDF(sVariable) = returns cdf object for given variable
	//mapPath(sVariable) = returns the path to the cdf file for the given variable
	//mapVar = map linking predictor numbers to variable names
	//mapMin(sVariable,sElevationTime) = returns the minimum value for the given variable
	//mapMax(sVariable,sElevationTime) = returns the maximum value for the given variable
	
	public Map<String,NetCDF_IO> mapCDF;
	public Map<String,String> mapPath;
	public Map<Integer,String> mapVar;
	public Map<String,Double> mapMin;
	public Map<String,Double> mapMax;
	
	/**
	 * Constructor
	 * @param bdo1 BetaDiversityObservationalData object
	 */
	public SDMRasterData(){
		
		//initializing minimum and maximum map
		mapMin = new HashMap<String,Double>();
		mapMax = new HashMap<String,Double>();
		
		//initializing CDF map
		mapCDF = new HashMap<String,NetCDF_IO>();
		
		//loading path map and variable map
		mapPath = new HashMap<String,String>();
		mapVar = new HashMap<Integer,String>();
	}
	
	/**
	 * Closes NetCDF file for specified variable
	 * @param sVariable
	 */
	public void closeCDF(String sVariable){
		if(mapCDF.containsKey(sVariable)){
			mapCDF.get(sVariable).closeReader();
			mapCDF.remove(sVariable);
		}
	}
	
	/**
	 * Closes all NetCDF files
	 */
	public void closeCDFAll(){
		for(String s:mapCDF.keySet()){
			mapCDF.get(s).closeReader();
		}
		mapCDF = new HashMap<String,NetCDF_IO>();
	}
	
	/**
	 * Returns maximum for specified variable
	 * @param dVert Elevation
	 * @param dTime Time
	 * @param sVar Variable name
	 * @param bLand True for land, false otherwise
	 * @param sLandRasterPath Path to land raster
	 * @return Minimum for raster
	 */
	public double findMax(double dVert, double dTime, String sVar, SDMArguments_BetaDiversity arg1){
		
		//sKey = current key
		
		String sKey;
		
		//loading key
		sKey = sVar + "," + dVert + "," + dTime;
		
		//checking if value already entered
		if(mapMax.containsKey(sKey)){
			return mapMax.get(sKey);
		}else{
			findMin(dVert,dTime,sVar, arg1);
			return mapMax.get(sKey);
		}
	}
	
	/**
	 * Returns minimum for specified variable
	 * @param dVert Elevation
	 * @param dTime Time
	 * @param sVar Variable name
	 * @param bLand True for land, false otherwise
	 * @return Minimum for raster
	 */
	public double findMin(double dVert, double dTime, String sVar, SDMArguments arg1){
		
		//sKey = current key
		//dMin = minimum value
		//dMax = maximum value
		//rti1 = raster iterator
		//dValue = candidate value
		//ptl1 = point on land object
		
		RasterIterator rti1;
		double dMin; double dMax; double dValue;
		String sKey;
		PointOnLand ptl1;
		
		//loading key
		sKey = sVar + "," + dVert + "," + dTime;
		
		//checking if value already entered
		if(mapMin.containsKey(sKey)){
			return mapMin.get(sKey);
		}
		
		//loading variable if necessary
		if(!mapCDF.containsKey(sVar)){
			mapCDF.put(sVar, new NetCDF_IO(mapPath.get(sVar),"reading"));
		}
		
		//initializing minimum and maximum
		dMin = 9999999999999999.;
		dMax = -9999999999999999.;
		
		//initializing point on land
		ptl1 = new PointOnLand(arg1.sPathGlobalTopography);
		
		//looping through values
		rti1 = new RasterIterator(dVert,dTime,sVar);
		while(rti1.hasNext()){
			
			//loading next value
			rti1.next();
			
			//checking if on land
			if((ptl1.isOnLand(rti1.dLat, rti1.dLon)==true && arg1.sLocation.equals("marine")) || (ptl1.isOnLand(rti1.dLat, rti1.dLon)==false && arg1.sLocation.equals("terrestrial"))){
				continue;
			}
			
			//loading value
			dValue = this.getRasterValue(rti1.getRasterLocation(), sVar);
			
			//checking if minimum and maximum
			if(dValue<dMin){
				dMin = dValue;
			}
			if(dValue>dMax){
				dMax = dValue;
			}
		}
		
		//saving results
		mapMin.put(sKey, dMin);
		mapMax.put(sKey, dMax);
		
		//returning result
		return dMin;
	}
	
	/**
	 * Gets the raster value at the specified location
	 * @param rsl1 Raster location
	 * @return Raster value
	 */
	public String getRasterValue(RasterLocation rsl1){
		
		//sVar = variable name
		//sbl1 = output
		//dValue = candidate value
		
		String sVar;
		StringBuilder sbl1;
		double dValue;
		
		//initializing output
		sbl1 = new StringBuilder();
		
		//looping through variables
		for(int i=0;i<mapVar.size();i++){
		
			//loading variable name
			sVar = mapVar.get(i);
			
			//loading value
			dValue = getRasterValue(rsl1, sVar);
			
			//checking for error
			if(dValue==-9999){
				return "-9999";
			}
			
			//loading result
			if(i>0){
				sbl1.append(",");
			}
			sbl1.append(sVar + ":" + dValue);
		}
		
		//returning result
		return sbl1.toString();
	}
	
	/**
	 * Gets the raster value at the specified location
	 * @param rsl1 Raster location
	 * @param sVar Variable name
	 * @return Raster value
	 */
	public double getRasterValue(RasterLocation rsl1, String sVar){
		
		//loading variable if necessary
		if(!mapCDF.containsKey(sVar)){
			mapCDF.put(sVar, new NetCDF_IO(mapPath.get(sVar),"reading"));
		}
		
		//getting value
		return mapCDF.get(sVar).readValue(rsl1.dLat,rsl1.dLon,rsl1.dVert,rsl1.dTime);
	}
	
	/**
	 * Gets the raster value at the specified location
	 * @param rsl1 Raster location
	 * @param iPredictor Predictor ID
	 * @return Raster value
	 */
	public double getRasterValue(RasterLocation rsl1, int iPredictor){
		
		//sVar = variable name
		
		String sVar;
		
		//loading variable name
		sVar = mapVar.get(iPredictor);
		
		//returning result
		return getRasterValue(rsl1, sVar);
	}
}

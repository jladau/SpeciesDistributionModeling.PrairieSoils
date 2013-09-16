package edu.ucsf.sdm;

import java.util.HashMap;
import java.util.Map;

import edu.ucsf.base.Function;
import edu.ucsf.base.NetCDF_IO;
import edu.ucsf.base.RasterLocation;

/**
 * Raster data for beta diversity analysis
 * @author jladau
 */
public class SDMRasterData_BetaDiversity extends SDMRasterData{

	//mapFcn(sVariable) = returns the function for the given variable
	
	private Map<String,Function> mapFcn;
	
	/**
	 * Constructor
	 * @param bdo1 BetaDiversityObservationalData object
	 */
	public SDMRasterData_BetaDiversity(SDMArguments_BetaDiversity arg1){
		
		//sVar = current variable name
		//map1 = current map being added to variable map
		
		//calling super
		super();
		
		String sVar;
		Map<Integer,String> map1;
	
		//loading path map and function map and variable map
		mapFcn = new HashMap<String,Function>();
		
		//initializing current variable map
		map1 = new HashMap<Integer,String>();
		for(int i=0;i<arg1.rgsPredictors.length;i++){
		
			//loading variable name
			sVar = arg1.rgsPredictors[i];
			
			//saving results as necessary
			if(!mapPath.containsKey(sVar)){
				mapPath.put(sVar, arg1.sDirRasters + "/" + arg1.lstPredictorFiles.get(i));
				mapFcn.put(sVar, new Function(arg1.lstPredictorFunctions.get(i)));	
			}
			
			//saving variable name
			map1.put(i, sVar);
		}
		
		//saving results to variable map
		mapVar= map1;
	}
	
	/**
	 * Gets the raster value at the specified location
	 * @param rslStart Starting raster location
	 * @param rslEnd Ending raster location
	 * @return Raster value
	 */
	public String getRasterValue(RasterLocation rslStart, RasterLocation rslEnd){
		
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
			dValue = getRasterValue(rslStart, rslEnd, sVar);
			
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
	 * @param rslStart Start location
	 * @param rslEnd End location
	 * @param sVar Variable name
	 * @return Raster value
	 */
	public double getRasterValue(RasterLocation rslStart, RasterLocation rslEnd, String sVar){
		
		//dStart = starting value
		//dEnd = ending value
		
		double dStart; double dEnd;
		
		//loading variable if necessary
		if(!mapCDF.containsKey(sVar)){
			mapCDF.put(sVar, new NetCDF_IO(mapPath.get(sVar),"reading"));
		}
		
		//getting starting value
		dStart = mapCDF.get(sVar).readValue(rslStart.dLat,rslStart.dLon,rslStart.dVert,rslStart.dTime);
		
		//getting ending value
		dEnd = mapCDF.get(sVar).readValue(rslEnd.dLat,rslEnd.dLon,rslEnd.dVert,rslEnd.dTime);
		
		//outputting results
		return mapFcn.get(sVar).applyFcn(dStart,dEnd);
	}
	
	/**
	 * Gets the raster value at the specified location
	 * @param rslStart Starting location
	 * @param dEndValue Ending value
	 * @param sVar Variable name
	 * @return Raster value
	 */
	public double getRasterValue(RasterLocation rslStart, double dEndValue, String sVar){
		
		//dStart = starting value
		
		double dStart;
		
		//loading variable if necessary
		if(!mapCDF.containsKey(sVar)){
			mapCDF.put(sVar, new NetCDF_IO(mapPath.get(sVar),"reading"));
		}
		
		//getting starting value
		dStart = mapCDF.get(sVar).readValue(rslStart.dLat,rslStart.dLon,rslStart.dVert,rslStart.dTime);
		
		//outputting results
		return mapFcn.get(sVar).applyFcn(dStart,dEndValue);
	}
}

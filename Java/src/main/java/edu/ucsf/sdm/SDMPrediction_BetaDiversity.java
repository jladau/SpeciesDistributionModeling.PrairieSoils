package edu.ucsf.sdm;

import java.util.ArrayList;

import edu.ucsf.base.RasterIterator;
import edu.ucsf.base.RasterLocation;
import edu.ucsf.base.SphericalGeometry;

/**
 * Beta diversity output map object
 * @author jladau
 */

public class SDMPrediction_BetaDiversity extends SDMPrediction{

	//rgdMapX = output map (vector: x)
	//rgdMapY = output map (vector: y)
	//arg1 = arguments object
	//mdl1 = model object
	//sph1 = SphericalGeometry object
	//obs1 = observed data object
	//lstMapX = output list for partial x-vector map; entries are lat, lon, vert, time, value
	//lstMapY = output list for partial y-vector map; entries are lat, lon, vert, time, value
	
	public double rgdMapX[][];
	public double rgdMapY[][];
	public ArrayList<String> lstMapX;
	public ArrayList<String> lstMapY;
	private SDMArguments_BetaDiversity arg1;
	private SDMModel_BetaDiversity mdl1;
	private SDMRasterData_BetaDiversity ras1;
	private SphericalGeometry sph1;
	private SDMObservationalData_BetaDiversity obs1;
	
	public SDMPrediction_BetaDiversity(SDMArguments_BetaDiversity arg1, SDMModel_BetaDiversity mdl1, SDMRasterData_BetaDiversity ras1, SDMObservationalData_BetaDiversity obs1){
		
		//running super
		super(arg1);
		
		//saving model and arguments object
		this.arg1 = arg1;
		this.mdl1 = mdl1;
		this.ras1 = ras1;
		this.obs1 = obs1;
	}
	
	/**
	 * Loads value into partial map
	 * @param rit1 Raster iterator with location
	 */
	public void loadPartialMapValue(RasterIterator rit1){
		
		//rgd1 = vector value output
		
		double rgd1[];
		
		if(arg1.sMapType.equals("community clustering")){
			lstMap.add(rit1.iRow + "," + rit1.iCol + "," + rit1.dLat + "," + rit1.dLon + "," + rit1.dVert + "," + rit1.dTime + "," + mdl1.clusterLocation(rit1.getRasterLocation()));
		}else if(arg1.sMapType.equals("local turnover")){
			lstMap.add(rit1.iRow + "," + rit1.iCol + "," + rit1.dLat + "," + rit1.dLon + "," + rit1.dVert + "," + rit1.dTime + "," + findLocalTurnover(rit1.getRasterLocation()));
		}else if(arg1.sMapType.equals("community novelty")){
			lstMap.add(rit1.iRow + "," + rit1.iCol + "," + rit1.dLat + "," + rit1.dLon + "," + rit1.dVert + "," + rit1.dTime + "," + findNovelty(rit1.getRasterLocation()));
		}else if(arg1.sMapType.equals("vector")){
			rgd1 = this.findVector(rit1.getRasterLocation(), "maximum");
			lstMapX.add(rit1.iRow + "," + rit1.iCol + "," + rit1.dLat + "," + rit1.dLon + "," + rit1.dVert + "," + rit1.dTime + "," + rgd1[0]);
			lstMapY.add(rit1.iRow + "," + rit1.iCol + "," + rit1.dLat + "," + rit1.dLon + "," + rit1.dVert + "," + rit1.dTime + "," + rgd1[1]);
		}
	}
	
	/**
	 * Loads value into map
	 * @param rit1 Raster iterator with location
	 */
	public void loadMapValue(RasterIterator rit1){
		
		//rgd1 = vector value output
		
		double rgd1[];
		
		if(arg1.sMapType.equals("community clustering")){
			rgdMap[rit1.iRow][rit1.iCol] = mdl1.clusterLocation(rit1.getRasterLocation());
		}else if(arg1.sMapType.equals("local turnover")){
			rgdMap[rit1.iRow][rit1.iCol] = findLocalTurnover(rit1.getRasterLocation());
		}else if(arg1.sMapType.equals("community novelty")){
			rgdMap[rit1.iRow][rit1.iCol] = findNovelty(rit1.getRasterLocation());
		}else if(arg1.sMapType.equals("vector")){
			rgd1 = this.findVector(rit1.getRasterLocation(), "maximum");
			rgdMapX[rit1.iRow][rit1.iCol] = rgd1[0];
			rgdMapY[rit1.iRow][rit1.iCol] = rgd1[1];
		}
	}
	
	/**
	 * Initializes partial map
	 * @param iVertTimeIndex
	 * @param iTaskID
	 */
	public void initializePartialMap(int iVertTimeIndex, int iTaskID){
		
		//initializing output
		if(arg1.sMapType.equals("vector")){
			lstMapX = new ArrayList<String>();
			lstMapY = new ArrayList<String>();
		}else{
			lstMap = new ArrayList<String>();
		}
		
		//initializing clusterer if necessary
		if(arg1.sMapType.equals("community clustering")){
			mdl1.initalizeClusterer(iTaskID);
		}
		
		//initializing spherical geometry object if necessary
		if(arg1.sMapType.equals("local turnover") || arg1.sMapType.equals("vector")){
			sph1 = new SphericalGeometry();
		}
	}
	
	/**
	 * Initializes map
	 * @param iVertTimeIndex
	 * @param iTaskID
	 */
	public void initializeMap(int iVertTimeIndex, int iTaskID){
		
		//initializing output
		if(arg1.sMapType.equals("vector")){
			rgdMapX = new double[360][720];
			rgdMapY = new double[360][720];
		}else{
			rgdMap = new double[360][720];
		}
		
		//initializing clusterer if necessary
		if(arg1.sMapType.equals("community clustering")){
			mdl1.initalizeClusterer(iTaskID);
		}
		
		//initializing spherical geometry object if necessary
		if(arg1.sMapType.equals("local turnover") || arg1.sMapType.equals("vector")){
			sph1 = new SphericalGeometry();
		}
	}
	
	/**
	 * 
	 * @param sDirection "minimum" for direction of minimum change, "maximum" for direction of maximum change
	 * @return
	 */
	private double[] findVector(RasterLocation rsl2, String sDirection){
		
		//dDirection = direction of minimum/maximum
		//dMagnitude = magnitude of minimum/maximum
		//dDirection = direction of minimum/maximum
		//dMagnitudeCandidate = current candidate magnitude value
		//dMagnitudeMin = minimum magnitude
		//rsl1 = location of second point
		//rgdOut = output; first entry with x value, second entry with y value
		//rgd1 = current adjacent location
		
		double dMagnitude = 0; double dDirection; double dMagnitudeCandidate; 
		RasterLocation rsl1;
		double rgdOut[]; double rgd1[];
		
		//initializing output
		rgdOut = new double[2];
		rgdOut[0] = -9999;
		rgdOut[1] = -9999;
		
		//checking for error
		if(ras1.getRasterValue(rsl2).equals("-9999")){	
			return rgdOut;
		}
		
		//initializing magnitude and direction
		if(sDirection.equals("minimum")){
			dMagnitude=999999999999.;
		}else if(sDirection.equals("maximum")){
			dMagnitude=-999999999999.;
		}
		dDirection=-9999;
		
		//looping through adjacent locations
		for(double d=0;d<2.*Math.PI;d+=2*Math.PI/8.){
		
			//loading location
			rgd1 = sph1.findDestination(rsl2.dLat, rsl2.dLon, d, arg1.dDistance);
			rsl1 = new RasterLocation(rgd1[0],rgd1[1],rsl2.dVert,rsl2.dTime,-9999,-9999,null);
			
			//loading value
			dMagnitudeCandidate = mdl1.findPrediction(rsl2, rsl1);
			
			//checking for error value
			if(dMagnitudeCandidate==-9999){
				return rgdOut;
			}else{
				
				//updating magnitude and direction, as appropriate
				if((sDirection.equals("minimum") && dMagnitudeCandidate<dMagnitude) || (sDirection.equals("maximum") && dMagnitudeCandidate>dMagnitude)){
					dMagnitude=dMagnitudeCandidate;
					dDirection=Math.PI/2.-d;
					if(dDirection<0){
						dDirection=2.*Math.PI+dDirection;
					}
				}
			}
		}
		
		//returning result
		rgdOut[0]=Math.cos(dDirection)*(Math.log(dMagnitude));
		rgdOut[1]=Math.sin(dDirection)*(Math.log(dMagnitude));
		return rgdOut;
	}
	
	/**
	 * Finds maximum observed similarity to samples at observed location
	 * @param rsl2 Location
	 * @return Local turnover.
	 */
	@Deprecated
	private double findNovelty(RasterLocation rsl2){
	
		//dMin = minimum observed distance
		//dValue = current value
		
		double dMin; double dValue;
		
		//initializing minimum
		dMin = 99999999999999999999999999.;
		
		//checking for error
		if(ras1.getRasterValue(rsl2).equals("-9999")){
			
			return -9999;
			
		}else{
		
			//looping through sampled points
			for(String s:obs1.mapSampleLocation.keySet()){
			
				//loading value
				dValue = mdl1.findPrediction(rsl2, obs1.mapSampleLocation.get(s));
				
				//checking for error
				if(dValue!=-9999){
					
					//updating minimum if appropriate
					if(dValue<dMin){
						dMin = dValue;
					}
				}
			}
			
			//saving result
			if(dMin==99999999999999999999999999.){
				
				//returning error values
				return -9999.;
			}else{
				
				//returning result
				return dMin;
			}
		}
	}
		
	/**
	 * Finds local turnover at given location
	 * @param rsl2 Location
	 * @return Local turnover.
	 */
	private double findLocalTurnover(RasterLocation rsl2){
		
		//dTotal = total
		//dCount = count
		//dValue = current value
		//rgd1 = current neighboring location
		//rsl1 = neighboring raster location
		
		RasterLocation rsl1;
		double dTotal = 0; double dCount = 0; double dValue;
		double rgd1[];
		
		//checking for error
		if(ras1.getRasterValue(rsl2).equals("-9999")){	
			return -9999;
		}
		
		//looping through adjacent locations
		for(double d=0;d<2.*Math.PI;d+=2*Math.PI/10.){
		
			//loading location
			rgd1 = sph1.findDestination(rsl2.dLat, rsl2.dLon, d, arg1.dDistance);
			rsl1 = new RasterLocation(rgd1[0],rgd1[1],rsl2.dTime,rsl2.dTime,-9999,-9999,null);
			
			//loading value
			dValue = mdl1.findPrediction(rsl2, rsl1);
			
			//checking value
			if(dValue!=-9999){
				dCount++;
				dTotal+=dValue;
			}
		}
		
		//checking if no data
		if(dCount==0){
			
			//returning error value
			return -9999.;
		}else{
			
			//updating map
			return dTotal/dCount;
		}
	}

	public ArrayList<String> predictToIndependentDataSet(String[][] rgsData) {
		return null;
	}
}

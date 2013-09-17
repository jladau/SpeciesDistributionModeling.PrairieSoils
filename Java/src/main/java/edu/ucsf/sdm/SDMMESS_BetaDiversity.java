package edu.ucsf.sdm;

import edu.ucsf.base.RasterIterator;
import edu.ucsf.base.RasterLocation;

public class SDMMESS_BetaDiversity extends SDMMESS{

	/** draws diversity map using the model fit in the given AssembleFirst object */
	
	//arg1 = arguments object
	//obs1 = observations object
	//ras1 = rasters object
	
	private SDMObservationalData_BetaDiversity obs1;
	private SDMArguments_BetaDiversity arg1;
	private SDMRasterData_BetaDiversity ras1;
	
	/**
	 * Constructor
	 * @param sdm1 SDM_BetaDiversity object
	 */
	public SDMMESS_BetaDiversity(SDMObservationalData_BetaDiversity obs1, SDMArguments_BetaDiversity arg1, SDMRasterData_BetaDiversity ras1){
		
		//calling super
		super();
		
		//saving arguments and data
		this.arg1 = arg1;
		this.obs1 = obs1;
		this.ras1 = ras1;
	}
	
	/**
	 * Loads mess map
	 * @param iFixedPredictorsIndex Index of fixed predictors currently under consideration
	 * @param iVariable Index of variable in arguments.rgsPredictors
	 */
	public void loadMESSMap(int iFixedPredictorsIndex, int iVariable){

		//rit1= RasterIterator object
		
		RasterIterator rit1;
		
		//initializing map
		rgdMap = new double[360][720];
		
		//initializing iterator
		rit1 = new RasterIterator(arg1.lstVert.get(iFixedPredictorsIndex), arg1.lstTime.get(iFixedPredictorsIndex), arg1.rgsPredictors[iVariable]);
		
		//looping through locations
		while(rit1.hasNext()){
			
			//updating iterator
			rit1.next();
			
			//saving MESS value
			if(arg1.sMapType.equals("local turnover") || arg1.sMapType.equals("vector")){
				rgdMap[rit1.iRow][rit1.iCol]=findMESSLocal(rit1);
			}else if(arg1.sMapType.equals("community clustering")){
				rgdMap[rit1.iRow][rit1.iCol]=findMESSClassification(rit1);
			}else if(arg1.sMapType.equals("community novelty")){
				rgdMap[rit1.iRow][rit1.iCol]=findMESSNovelty(rit1);
			}
		}
		
	}
	
	
	/**
	 * Finds MESS value for local spatial turnover mapping
	 * @param rit1 RasterIterator giving current (focal) location in raster
	 * @return MESS value
	 */
	private double findMESSLocal(RasterIterator rit1){
		
		//rgd1 = current latitude and longitude
		//dValue = current value
		//dMESS = current mess value
		//dOut = output value
		//rsl1 = location of adjacent point
		
		double rgd1[];
		double dValue; double dMESS; double dOut;
		RasterLocation rsl1;
		
		//initializing mess value
		dOut = -9999;
		
		//looping through adjacent locations
		for(double d=0;d<2.*Math.PI;d+=2*Math.PI/10.){
			
			//loading location
			rgd1 = sph1.findDestination(rit1.dLat, rit1.dLon, d, arg1.dDistance);
			rsl1 = new RasterLocation(rgd1[0],rgd1[1],rit1.dVert,rit1.dTime,-9999,-9999,null);
			
			//loading value
			dValue = ras1.getRasterValue(rit1.getRasterLocation(), rsl1, rit1.sVar);
			
			//checking for error
			if(dValue==-9999){
				continue;
			}
			
			//checking mess value
			if(dValue<obs1.mapMinimum.get(rit1.sVar)){
				dMESS = (dValue - obs1.mapMinimum.get(rit1.sVar))/obs1.mapRange.get(rit1.sVar);
			}else if(dValue>obs1.mapMaximum.get(rit1.sVar)){
				dMESS = (obs1.mapMaximum.get(rit1.sVar) - dValue)/obs1.mapRange.get(rit1.sVar);
			}else{
				dMESS = 0;
			}
			if(dMESS<dOut || dOut==-9999){
				dOut = dMESS;
			}
		}
		
		//outputting result
		if(dOut!=-9999){
			dOut*=100.;
		}
		return dOut;
	}
	
	/**
	 * Finds MESS value for biome classification mapping
	 * @param rit1 RasterIterator giving current (focal) location in raster
	 * @return MESS value
	 */
	private double findMESSClassification(RasterIterator rit1){

		//dValue = current value
		//dMESS = current mess value
		//dOut = output value
		//rgd1 = contains minimum and maximum value
		
		double rgd1[];
		double dValue; double dMESS; double dOut;
		
		//initializing mess value
		dOut = -9999;
		
		//initializing minimum and maximum value
		rgd1 = new double[2];
		rgd1[0]=ras1.findMin(rit1.dVert, rit1.dTime, rit1.sVar, arg1);
		rgd1[1]=ras1.findMax(rit1.dVert, rit1.dTime, rit1.sVar, arg1);
		
		//looping through minimum and maximum
		for(int i=0;i<2;i++){
			
			//loading value
			dValue = ras1.getRasterValue(rit1.getRasterLocation(), rgd1[i], rit1.sVar);
			
			//checking for error
			if(dValue==-9999){
				continue;
			}
			
			//checking mess value
			if(dValue<obs1.mapMinimum.get(rit1.sVar)){
				dMESS = (dValue - obs1.mapMinimum.get(rit1.sVar))/obs1.mapRange.get(rit1.sVar);
			}else if(dValue>obs1.mapMaximum.get(rit1.sVar)){
				dMESS = (obs1.mapMaximum.get(rit1.sVar) - dValue)/obs1.mapRange.get(rit1.sVar);
			}else{
				dMESS = 0;
			}
			if(dMESS<dOut || dOut==-9999){
				dOut = dMESS;
			}
		}
		
		//outputting result
		if(dOut!=-9999){
			dOut*=100.;
		}
		return dOut;
	}

	/**
	 * Finds MESS value for biome classification mapping
	 * @param rit1 RasterIterator giving current (focal) location in raster
	 * @return MESS value
	 */
	@Deprecated
	private double findMESSNovelty(RasterIterator rit1){
	
		//dValue = current value
		//dMESS = current mess value
		//dOut = output value
		//rgd1 = contains minimum and maximum value
		
		double rgd1[];
		double dValue; double dMESS; double dOut;
		
		//initializing mess value
		dOut = -9999;
		
		//initializing minimum and maximum value
		rgd1 = new double[2];
		rgd1[0]=obs1.mapSingleLocationMinimum.get(rit1.sVar.split("_")[0]);
		rgd1[1]=obs1.mapSingleLocationMaximum.get(rit1.sVar.split("_")[0]);
		
		//looping through minimum and maximum
		for(int i=0;i<2;i++){
			
			//loading value
			dValue = ras1.getRasterValue(rit1.getRasterLocation(), rgd1[i], rit1.sVar);
			
			//checking for error
			if(dValue==-9999){
				continue;
			}
			
			//checking mess value
			if(dValue<obs1.mapMinimum.get(rit1.sVar)){
				dMESS = (dValue - obs1.mapMinimum.get(rit1.sVar))/obs1.mapRange.get(rit1.sVar);
			}else if(dValue>obs1.mapMaximum.get(rit1.sVar)){
				dMESS = (obs1.mapMaximum.get(rit1.sVar) - dValue)/obs1.mapRange.get(rit1.sVar);
			}else{
				dMESS = 0;
			}
			if(dMESS<dOut || dOut==-9999){
				dOut = dMESS;
			}
		}
		
		//outputting result
		if(dOut!=-9999){
			dOut*=100.;
		}
		return dOut;
	}
}
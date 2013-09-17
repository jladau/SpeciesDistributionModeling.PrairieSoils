package edu.ucsf.sdm;

import edu.ucsf.base.RasterIterator;

public class SDMMESS_AlphaDiversity extends SDMMESS{

	/** draws diversity map using the model fit in the given AssembleFirst object */
	
	//arg1 = arguments object
	//obs1 = observations object
	//ras1 = rasters object
	
	private SDMObservationalData_AlphaDiversity obs1;
	private SDMArguments_AlphaDiversity arg1;
	private SDMRasterData_AlphaDiversity ras1;
	
	/**
	 * Constructor
	 * @param sdm1 SDM_BetaDiversity object
	 */
	public SDMMESS_AlphaDiversity(SDMObservationalData_AlphaDiversity obs1, SDMArguments_AlphaDiversity arg1, SDMRasterData_AlphaDiversity ras1){
		
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
			rgdMap[rit1.iRow][rit1.iCol]=findMESSAlpha(rit1);
		}
	}
	
	/**
	 * Finds the MESS value associated with the given raster value
	 * @param sRasterValue Raster value in string format, different variables separated by commas
	 * @return MESS value (minimum across variables)
	 */
	public double findMESSAlpha(String sRasterValue){
		
		//dOut = output
		//rgs1 = raster value in string format
		//sVar = current variable
		//dValue = current variable value
		//dMESS = current mess value
		
		double dOut; double dValue; double dMESS;
		String rgs1[];
		String sVar;
		
		//checking for error
		if(sRasterValue.equals("-9999")){
			return -9999;
		}
		
		//initializing output
		dOut = 9999999999999.;
		
		//loading observed ranges
		obs1.loadRanges();
		
		//looping through variables
		rgs1 = sRasterValue.split(",");
		for(int i=0;i<rgs1.length;i++){
		
			//loading variable and value
			sVar = rgs1[i].split(":")[0];
			dValue = Double.parseDouble(rgs1[i].split(":")[1]);
			
			//checking for error
			if(dValue==-9999){
				return -9999;
			}
			
			//finding mess value
			if(dValue<obs1.mapMinimum.get(sVar)){
				dMESS = (dValue - obs1.mapMinimum.get(sVar))/obs1.mapRange.get(sVar);
			}else if(dValue>obs1.mapMaximum.get(sVar)){
				dMESS = (obs1.mapMaximum.get(sVar) - dValue)/obs1.mapRange.get(sVar);
			}else{
				dMESS = 0;
			}
			
			//updating output
			if(dMESS<dOut){
				dOut=dMESS;
			}
		}
		
		//returning result
		return dOut;
	}
	
	/**
	 * Finds MESS value for local spatial turnover mapping
	 * @param rit1 RasterIterator giving current (focal) location in raster
	 * @return MESS value
	 */
	private double findMESSAlpha(RasterIterator rit1){
		
		//dValue = current value
		//dMESS = current mess value
		
		double dValue; double dMESS; 
		
		//loading value
		try{	
			dValue = ras1.getRasterValue(rit1.getRasterLocation(), rit1.sVar);
		}catch(Exception e){
			dValue=-9999;
		}
		
		//checking for error
		if(dValue==-9999){
			return -9999;
		}
		
		//checking mess value
		if(dValue<obs1.mapMinimum.get(rit1.sVar)){
			dMESS = (dValue - obs1.mapMinimum.get(rit1.sVar))/obs1.mapRange.get(rit1.sVar);
		}else if(dValue>obs1.mapMaximum.get(rit1.sVar)){
			dMESS = (obs1.mapMaximum.get(rit1.sVar) - dValue)/obs1.mapRange.get(rit1.sVar);
		}else{
			dMESS = 0;
		}
		
		//outputting result
		dMESS*=100.;
		return dMESS;
	}
}
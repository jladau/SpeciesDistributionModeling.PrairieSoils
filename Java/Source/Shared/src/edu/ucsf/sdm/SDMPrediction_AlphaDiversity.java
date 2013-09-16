package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.base.RasterIterator;
import edu.ucsf.base.RasterLocation;
import edu.ucsf.base.SphericalGeometry;

/**
 * Beta diversity output map object
 * @author jladau
 */

public class SDMPrediction_AlphaDiversity extends SDMPrediction{

	//arg1 = arguments object
	//mdl1 = model object
	//sph1 = SphericalGeometry object
	//obs1 = observed data object
	
	private SDMArguments_AlphaDiversity arg1;
	private SDMModel_AlphaDiversity mdl1;
	private SDMRasterData_AlphaDiversity ras1;
	private SphericalGeometry sph1;
	private SDMObservationalData_AlphaDiversity obs1;
	
	public SDMPrediction_AlphaDiversity(SDMArguments_AlphaDiversity arg1, SDMModel_AlphaDiversity mdl1, SDMRasterData_AlphaDiversity ras1, SDMObservationalData_AlphaDiversity obs1){
		
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
		lstMap.add(rit1.iRow + "," + rit1.iCol + "," + rit1.dLat + "," + rit1.dLon + "," + rit1.dVert + "," + rit1.dTime + "," + findAlphaDiversity(rit1.getRasterLocation()));
	}
	
	/**
	 * Loads value into map
	 * @param rit1 Raster iterator with location
	 */
	public void loadMapValue(RasterIterator rit1){
		rgdMap[rit1.iRow][rit1.iCol] = findAlphaDiversity(rit1.getRasterLocation());
	}
	
	/**
	 * Initializes partial map
	 * @param iVertTimeIndex
	 * @param iTaskID
	 */
	public void initializePartialMap(int iVertTimeIndex, int iTaskID){
		
		//initializing output
		lstMap = new ArrayList<String>();
	}
	
	/**
	 * Initializes map
	 * @param iVertTimeIndex
	 * @param iTaskID
	 */
	public void initializeMap(int iVertTimeIndex, int iTaskID){
		
		//initializing output
		rgdMap = new double[360][720];
	}
	
	
	/**
	 * Generates a list of predictions to an independent data set
	 */
	public ArrayList<String> predictToIndependentDataSet(String rgsData[][]){
	
		//iLat = column with latitudes
		//iLon = column with longitudes
		//iMonth = column with months
		//iElevation = column with elevations
		//iResponse = column with response variable
		//iSample = column with sample
		//lstOut = output
		//rsl1 = raster location object
		//dPrediction = current prediction
		//mss1 = mess object
		//mapPredictorColumn(sPredictor) = returns the predictor column with data
		//sblRasterValue = current raster value
		//iStudy = study
		//bError = flag for whether error found
		
		int iLat = -9999; int iLon = -9999; int iMonth = -9999; int iElevation = -9999; int iResponse = -9999; int iSample = -9999; int iStudy = -9999;
		ArrayList<String> lstOut;
		RasterLocation rsl1;
		double dPrediction;
		SDMMESS_AlphaDiversity mss1;
		HashMap<String,Integer> mapPredictorColumn;
		StringBuilder sblRasterValue;
		boolean bError;
		
		//initializing predictor column map
		mapPredictorColumn = new HashMap<String,Integer>();
		
		//finding response variable columns
		for(int j=0;j<rgsData[0].length;j++){
			if(rgsData[0][j].equals("Latitude")){
				iLat=j;
			}else if(rgsData[0][j].equals("Longitude")){
				iLon=j;
			}else if(rgsData[0][j].equals("Month")){
				iMonth=j;
			}else if(rgsData[0][j].equals("Elevation")){
				iElevation=j;
			}else if(rgsData[0][j].equals(this.arg1.sResponse)){
				iResponse=j;
			}else if(rgsData[0][j].equals("SampleID")){
				iSample=j;
			}else if(rgsData[0][j].equals("Study")){
				iStudy=j;
			}
			
			//loading predictor column
			for(String s:ras1.mapPath.keySet()){
				if(rgsData[0][j].equals(s + "Raster")){
					mapPredictorColumn.put(s,j);
				}
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		if(iStudy==-9999){	
			lstOut.add("SAMPLE_ID,MESS_VALUE,OBSERVED,PREDICTED");
		}else{
			lstOut.add("STUDY,SAMPLE_ID,MESS_VALUE,OBSERVED,PREDICTED");
		}
			
		//fitting model
		mdl1.fitModel(arg1.sResponse, arg1.rgsPredictors);
		mdl1.loadCoefficients();
		
		//initializing mess object
		mss1 = new SDMMESS_AlphaDiversity(obs1, arg1, ras1);
		
		//loading output
		for(int i=1;i<rgsData.length;i++){
			
			//initializing raster location
			//rsl1 = new RasterLocation(Double.parseDouble(rgsData[i][iLat]),Double.parseDouble(rgsData[i][iLon]),Double.parseDouble(rgsData[i][iElevation]),Double.parseDouble(rgsData[i][iMonth]),0,0,"");
			
			//loading raster values
			sblRasterValue = new StringBuilder();
			bError = false;
			for(String s:mapPredictorColumn.keySet()){
				if(Double.parseDouble(rgsData[i][mapPredictorColumn.get(s)])==-9999 || bError==true){
					bError=true;
					continue;
				}else{
					if(!sblRasterValue.toString().equals("")){
						sblRasterValue.append(",");
					}
					sblRasterValue.append(s + ":" + rgsData[i][mapPredictorColumn.get(s)]);
				}
			}
			
			//checking if error found
			if(bError==true){
				if(iStudy==-9999){
					lstOut.add(rgsData[i][iSample] + ",--,--,--");
				}else{
					lstOut.add(rgsData[i][iStudy] + "," + rgsData[i][iSample] + ",--,--,--");
				}	
				continue;
			}
			
			//********************
			//System.out.println(mss1.findMESSAlpha(sblRasterValue.toString()) + ";" + sblRasterValue.toString());
			//********************
			
			//checking mess value
			//if(mss1.findMESSAlpha(ras1.getRasterValue(rsl1))<0){
			//if(mss1.findMESSAlpha(sblRasterValue.toString())<0){
			//	continue;
			//}
			
			//loading prediction
			//dPrediction = this.findAlphaDiversity(rsl1);
			dPrediction = this.findAlphaDiversity(sblRasterValue.toString());
			
			//outputting results
			if(dPrediction!=-9999){
				if(iStudy==-9999){
					lstOut.add(rgsData[i][iSample] + "," + mss1.findMESSAlpha(sblRasterValue.toString()) + "," + rgsData[i][iResponse] + "," + dPrediction);
				}else{
					lstOut.add(rgsData[i][iStudy] + "," + rgsData[i][iSample] + "," + mss1.findMESSAlpha(sblRasterValue.toString()) + "," + rgsData[i][iResponse] + "," + dPrediction);
				}
			}else{
				if(iStudy==-9999){
					lstOut.add(rgsData[i][iSample] + "," + mss1.findMESSAlpha(sblRasterValue.toString()) + "," + rgsData[i][iResponse] + ",--");
				}else{
					lstOut.add(rgsData[i][iStudy] + "," + rgsData[i][iSample] + "," + mss1.findMESSAlpha(sblRasterValue.toString()) + "," + rgsData[i][iResponse] + ",--");
				}
			}
				
			//*********************
			//if(this.findAlphaDiversity(rsl1)==-9999){
			//	System.out.println(i);
			//}
			//*********************
		
		}
		
		//returning result
		return lstOut;	
	}
	
	/**
	 * Finds local turnover at given location
	 * @param rsl2 Location
	 */
	private double findAlphaDiversity(RasterLocation rsl2){
		
		//checking for error
		if(ras1.getRasterValue(rsl2).equals("-9999")){	
			return -9999;
		}
			
		//returning value
		return mdl1.findPrediction(rsl2);
	}
	
	/**
	 * Finds local turnover at given location
	 * @param sRasterValue Raster value
	 */
	private double findAlphaDiversity(String sRasterValue){
		
		//checking for error
		if(sRasterValue.equals("-9999")){	
			return -9999;
		}
			
		//returning value
		return mdl1.findPrediction(sRasterValue);
	}
	

	/**
	 * Generates a list of predictions to an independent data set
	 */
	public ArrayList<String> predictToIndependentDataSetNoInterpolation(String rgsData[][]){
	
		//iLat = column with latitudes
		//iLon = column with longitudes
		//iMonth = column with months
		//iElevation = column with elevations
		//iResponse = column with response variable
		//iSample = column with sample
		//lstOut = output
		//rsl1 = raster location object
		//dPrediction = current prediction
		//mss1 = mess object
		
		int iLat = -9999; int iLon = -9999; int iMonth = -9999; int iElevation = -9999; int iResponse = -9999; int iSample = -9999;
		ArrayList<String> lstOut;
		RasterLocation rsl1;
		double dPrediction;
		SDMMESS_AlphaDiversity mss1;
		
		//finding response variable columns
		for(int j=0;j<rgsData[0].length;j++){
			if(rgsData[0][j].equals("Latitude")){
				iLat=j;
			}else if(rgsData[0][j].equals("Longitude")){
				iLon=j;
			}else if(rgsData[0][j].equals("Month")){
				iMonth=j;
			}else if(rgsData[0][j].equals("Elevation")){
				iElevation=j;
			}else if(rgsData[0][j].equals(this.arg1.sResponse)){
				iResponse=j;
			}else if(rgsData[0][j].equals("SampleID")){
				iSample=j;
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("SAMPLEID,OBSERVED,PREDICTED");
		
		//fitting model
		mdl1.fitModel(arg1.sResponse, arg1.rgsPredictors);
		mdl1.loadCoefficients();
		
		//initializing mess object
		mss1 = new SDMMESS_AlphaDiversity(obs1, arg1, ras1);
		
		//loading output
		for(int i=1;i<rgsData.length;i++){
			
			//initializing raster location
			rsl1 = new RasterLocation(Double.parseDouble(rgsData[i][iLat]),Double.parseDouble(rgsData[i][iLon]),Double.parseDouble(rgsData[i][iElevation]),Double.parseDouble(rgsData[i][iMonth]),0,0,"");
			
			//checking mess value
			if(mss1.findMESSAlpha(ras1.getRasterValue(rsl1))<0){
				continue;
			}
			
			//loading prediction
			dPrediction = this.findAlphaDiversity(rsl1);
			
			//outputting results
			if(dPrediction!=-9999){
				lstOut.add(rgsData[i][iSample] + "," + rgsData[i][iResponse] + "," + dPrediction);
			}else{
				lstOut.add(rgsData[i][iSample] + "," + rgsData[i][iResponse] + ",--");
			}
				
			//*********************
			//if(this.findAlphaDiversity(rsl1)==-9999){
			//	System.out.println(i);
			//}
			//*********************
		
		}
		
		//returning result
		return lstOut;	
	}
}

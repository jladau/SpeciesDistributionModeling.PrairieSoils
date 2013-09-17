package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.ucsf.base.Clusterer_Hierarchical;
import edu.ucsf.base.Clusterer_KNearestNeighbor;
import edu.ucsf.base.FileIO;
import edu.ucsf.base.FitLM_Apache;
import edu.ucsf.base.RasterLocation;
import edu.ucsf.base.SphericalGeometry;

/**
 * CModel object for beta-diversity niche modeling.
 * @author jladau
 */
public class SDMModel_BetaDiversity extends SDMModel{

	//obs1 = ObservationalData object
	//ras1 = RasterData_BetaDiversity object
	//arg1 = RasterData_Arguments object
	//dThreshold = threshold value for outputting distance
	//mapRasterLocation(sIndex) = returns raster location for given point used in hierarchical classification
	//clk1 = k-nearest neighbor classifier
		
	private double dThreshold;
	private Map<String,RasterLocation> mapRasterLocation=null;
	private Clusterer_KNearestNeighbor clk1;
	private SDMObservationalData_BetaDiversity obs1;
	private SDMRasterData_BetaDiversity ras1;
	private SDMArguments_BetaDiversity arg1;
	
	/**
	 * Constructor
	 * @param obs1 ObservationalData object
	 */
	public SDMModel_BetaDiversity(SDMObservationalData_BetaDiversity obs1, SDMRasterData_BetaDiversity ras1, SDMArguments_BetaDiversity arg1){
		
		//initializing superclass
		super(obs1.rgsData);
		
		//saving data
		this.obs1 = obs1;
		this.ras1 = ras1;
		this.arg1 = arg1;
	}
		
	/**
	 * Finds the prediction at the specified pair of locations
	 * @param rslStart Starting raster location
	 * @param rslEnd Ending raster location
	 * @return Predicted value
	 */
	public double findPrediction(RasterLocation rslStart, RasterLocation rslEnd){
		
		//s1 = predictor values
		
		String s1;
		
		//loading predictor values
		s1 = ras1.getRasterValue(rslStart, rslEnd);
		
		//checking for error
		if(s1.equals("-9999")){
			return -9999;
		}
		
		//returning predicted distance
		return this.findPrediction(s1);
	}

	public double findPRESS(){
		return dPRESS;
	}
	
	/**
	 * Fits model with given response variable and predictors. Data are loaded first for given model.   
	 * @param sResponse Response variable name.
	 * @param rgsPredictors Predictor names.		
	 */
	public void runCrossValidation(String sResponse, String[] rgsPredictors){
		
		//sSample1 = current first sample
		//sSample2 = current second sample
		//lstDataTruncated = data set with all rows containing current samples removed
		//flm2 = fitLM object being used for cross validation
		//rgd1 = current predicted value
		//rgdX = values of environmental variables where prediction is to be made
		//i1 = row of data for prediction
		//dSSE = sum of squares due to error
		//dObservation = current observation
		//lst1 = current list of rows
		
		String sSample1; String sSample2;
		ArrayList<double[]> lstDataTruncated; ArrayList<Integer> lst1;
		FitLM_Apache flm2;
		double rgd1[]; double rgdX[][];
		int i1;
		double dSSE; double dObservation;
	
		//initializing predictions
		lstPredictions = new ArrayList<String>();
		lstPredictions.add("PREDICTED,OBSERVED");
		
		//checking if null case
		if(rgsPredictors==null){
			return;
		}
		
		//loading non-cross validation results
		this.fitModel(sResponse, rgsPredictors);
		
		//loading cross validation results: looping through pairs of samples
		dSSE=0;
		for(int i=1;i<obs1.rgsData.length;i++){
			
			//loading current pair of samples
			sSample1 = obs1.rgsData[i][0];
			sSample2 = obs1.rgsData[i][1];
				
			//saving prediction row
			i1 = i;
			
			//loading truncated data set and prediction row
			lstDataTruncated = new ArrayList<double[]>();
			lst1 = obs1.mapSampleRows.get(sSample1 + "," + sSample2);
			for(int k=0;k<lst1.size();k++){
				lstDataTruncated.add(obs1.rgdData[lst1.get(k)]);
			}
			
			//loading cross validation modeling object
			flm2 = new FitLM_Apache(lstDataTruncated, this.getColumnMap());
			flm2.fitModel(sResponse, rgsPredictors);

			//loading environmental values for prediction
			rgdX = this.findPredictors(i1, rgsPredictors, sResponse);
			
			//loading observed response for prediction
			dObservation = this.findObservation(i1, sResponse);
			
			//loading prediction for left out pair
			rgd1 = flm2.findPrediction(rgdX);
			
			//updating sse
			dSSE += (rgd1[0]-dObservation)*(rgd1[0]-dObservation);
			
			//updating predictions list
			lstPredictions.add(rgd1[0] + "," + dObservation);
		}
		
		//saving press statistics
		dPRESS = dSSE;
	}
	
	/**
	 * Runs hierarchical clusterer and outputs files with results
	*/
	public void runHierarchicalClustering(){
		
		//dPercentile = percentile for threshold
		//clh1 = hierarchical classifier object
		//clk1 = k-nearest neighbor classifier
		
		Clusterer_Hierarchical clh1;
		double dPercentile;
	
		//loading percentile
		dPercentile = 1;
		
		//loading threshold
		if(dPercentile<1){
			loadClusteringThreshold(dPercentile);
		}else{
			dThreshold = 99999999999.;
		}
		
		//outputting distance matrix
		outputClusteringDistanceMatrix(arg1.iHierachicalLocations);
		
		//initializing hierarchical clusterer
		clh1 = new Clusterer_Hierarchical(arg1.sPathHCluster,arg1.sDirOutput,"distances.dist");
		
		//running hierarchical classification
		clh1.runHCluster();
		clh1.runUPGMA();
	}

	/**
	 * Initializes clusterer after hierarchical clustering has been run
	 * @param iTaskID Task ID
	*/
	public void initalizeClusterer(int iTaskID){
		
		//clh1 = hierarchical classifier object
		
		Clusterer_Hierarchical clh1;

		//initializing hierarchical classifier
		clh1 = new Clusterer_Hierarchical(arg1.sPathHCluster,arg1.sHClusterOutputDirectory,"distances.dist");
			
		//loading classification
		clh1.loadClassificationClustersThreshold(arg1.iHierarchicalClusters,"_upgma");
		
		//initializing k-nearest neighbor classification
		clk1 = new Clusterer_KNearestNeighbor(10,clh1.mapClass);
	}

	/**
	 * Clusters location given by rsl1
	 * @param rsl1 Location to be clustered
	 * @return Cluster of location.
	 */
	public int clusterLocation(RasterLocation rsl1){
		
		//map1 = map of distances from current point to pre-classified point.  keys are names of pre-classified points.
		//rsl2 = current raster location
		//rgsLocation = locations of clustered points
	
		Map<String,Double> map1;
		RasterLocation rsl2;
		String rgsLocation[][];
		
		//checking whether a valid point
		if(ras1.getRasterValue(rsl1).equals("-9999")){
			return -9999;
		}
		
		//initializing map of distances
		map1 = new HashMap<String,Double>();
		
		//initializing mapRasterLocation if necessary
		if(mapRasterLocation==null){
			mapRasterLocation = new HashMap<String,RasterLocation>();
			rgsLocation = FileIO.readFile(arg1.sHClusterOutputDirectory + "/distances.locations",";");
			for(int i=0;i<rgsLocation.length;i++){
			
				//loading raster location
				mapRasterLocation.put(rgsLocation[i][0], new RasterLocation(rgsLocation[i][1]));
			}
		}
		
		//loading distances
		for(String s:mapRasterLocation.keySet()){
			
			//loading 
			rsl2 = mapRasterLocation.get(s);
			
			//loading predicted distance
			map1.put(s, this.findPrediction(rsl1,rsl2));
		}
		
		//returning result
		return Integer.parseInt(clk1.classifyLocation(map1));
	}
	

	/**
	 * Finds threshold for distance matrix so that a reasonable number of values are output
	 * @param dPercentile Percentile for threshold
	 */
	private void loadClusteringThreshold(double dPercentile){
		
		//rgdDistance = list of distances
		//iRow = output row
		//dCount = total number of observations for threshold calculation
		//iElevationTime = current randomly chosen elevation-time combination
		//rgd1 = current randomly chosen point
		//rgr1 = current pair of randomly chosen locations
		
		RasterLocation[] rgr1;
		double rgdDistance[]; double rgd1[];
		int iRow; int iElevationTime;
		double dCount;
		SphericalGeometry sph1;
		
		//loading count
		dCount = 1000;
		
		//initializing distance
		rgdDistance = new double[(int) dCount];
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//looping through random pairs of points
		for(int i=0;i<dCount;i++){
			
			//loading randomly chosen locations
			rgr1 = new RasterLocation[2];
			for(int j=0;j<2;j++){
				do{
					iElevationTime = (int) Math.floor(((double) arg1.lstVert.size())*Math.random());
					rgd1 = sph1.findRandomPoint();
					rgr1[j] = new RasterLocation(rgd1[0],rgd1[1],arg1.lstVert.get(iElevationTime), arg1.lstTime.get(iElevationTime),-9999,-9999,"-9999");
				}while(ras1.getRasterValue(rgr1[j]).equals("-9999"));
			}
			
			//loading distance
			rgdDistance[i] = this.findPrediction(rgr1[0],rgr1[1]);
		}
		
		//sorting distance array
		Arrays.sort(rgdDistance);
		
		//getting result
		iRow = (int) Math.floor(dPercentile*dCount);
		if(iRow==rgdDistance.length){
			iRow--;
		}
		dThreshold = rgdDistance[iRow];
	}
	

	/**
	 * Outputs distance matrix.  Row indices in lstNonErrors are used to identify cells.   Only pairs below threshold distance are output.
	 */
	private void outputClusteringDistanceMatrix(int iLocations){
		
		//s1 = current predictor values
		//dDistance = current predicted distance
		//rsl1 = current randomly chosen point
		//sph1 = Spherical geometry object
		//lst1 = list of raster locations
		//lst2 = list of raster locations in string format (to prevent duplication)
		//lst3 = raster location map in string format
		//rgd1 = current candidate location
		//iElevationTime = current candidate elevation-time combination
		//i1 = index of randomly chosen elevation-time combination
		//bExit = flag for whether point found
		//dDistanceMax = maximum distance
		//dDistanceMin = minimum distance
		//dDistanceRange = range of distances
		//lstOut = output
		//iOutRow = current output row
		//bAppend = flag for appending output
		
		RasterLocation rsl1;
		int i1; int iOutRow;
		double rgd1[];
		ArrayList<String> lstOut; ArrayList<String> lst2; ArrayList<RasterLocation> lst1; ArrayList<String> lst3;
		SphericalGeometry sph1;
		String s1;
		double dDistance; double dDistanceMax; double dDistanceMin; double dDistanceRange;
		boolean bExit; boolean bAppend;
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//initializing list of raster locations
		lst1 = new ArrayList<RasterLocation>();
		lst2 = new ArrayList<String>();
		
		//loading locations
		for(int i=0;i<iLocations;i++){
			
			//initializing exit flag
			bExit = false;
			
			//looping until suitable point found
			do{
				
				//loading randomly chosen point
				rgd1 = sph1.findRandomPoint();
				rgd1[0] = Double.parseDouble(this.roundCoordinate(rgd1[0]));
				rgd1[1] = Double.parseDouble(this.roundCoordinate(rgd1[1]));
				
				//initializing point
				rsl1 = new RasterLocation(rgd1[0],rgd1[1],-9999,-9999,-9999,-9999,"-9999");
				
				//loading randomly chosen elevation-time
				i1 = (int) Math.floor(((double) arg1.lstVert.size())*Math.random());
				rsl1.dVert = arg1.lstVert.get(i1);
				rsl1.dTime = arg1.lstTime.get(i1);
				
				//checking value
				if(!ras1.getRasterValue(rsl1).equals("-9999")){
					if(!lst2.contains(rsl1.toString())){
						
						//saving result and setting exit flag
						lst2.add(rsl1.toString());
						lst1.add(rsl1);
						bExit=true;
					}
				}
			}while(bExit==false);
		}
		
		//loading minimum and maximum distance
		dDistanceMin = 999999999999.; dDistanceMax = -9999999999999999.;
		
		//initializing map linking index to raster location
		mapRasterLocation = new HashMap<String,RasterLocation>();
		mapRasterLocation.put(Integer.toString(0), lst1.get(0));
		
		//looping through pairs of locations to load msdm1.arg1.lstElevationTime.get(iFixedPredictorsIndex),"NA"aximum and minimum
		for(int i=1;i<lst1.size();i++){
			
			//updating progress
			System.out.println("Analyzing line " + i + "...");
		
			//saving location
			mapRasterLocation.put(Integer.toString(i), lst1.get(i));
			
			for(int j=0;j<i;j++){
				
				//loading predictor values
				s1 = ras1.getRasterValue(lst1.get(i), lst1.get(j));
				
				//loading predicted distance
				dDistance = this.findPrediction(s1);
				
				//outputting distance
				if(dDistance<dThreshold){
					
					//updating minimum and maximum
					if(dDistance<dDistanceMin){
						dDistanceMin = dDistance;
					}
					if(dDistance>dDistanceMax){
						dDistanceMax = dDistance;
					}
				}
			}
		}
		
		//loading range and adding epsilon
		dDistanceMin-=0.0001;
		dDistanceMax+=0.0001;
		dDistanceRange = dDistanceMax - dDistanceMin;
		
		//*************************
		//dDistanceMin=1./(1.+Math.exp(-dDistanceMin));
		//dDistanceMax=1./(1.+Math.exp(-dDistanceMax));
		//dDistanceRange = dDistanceMax-dDistanceMin;
		//*************************
		
		
		//looping through pairs of locations and outputting results
		iOutRow = 0;
		lstOut = new ArrayList<String>(1000);
		bAppend=false;
		for(int i=1;i<lst1.size();i++){
			
			//updating progress
			System.out.println("Analyzing line " + i + "...");
			
			for(int j=0;j<i;j++){
				
				//loading predicted distance
				dDistance = this.findPrediction(lst1.get(i),lst1.get(j));
				
				//***************************
				//dDistance=1./(1.+Math.exp(-dDistance));
				//***************************
				
				//outputting distance
				if(dDistance<dThreshold){
					
					//outputting if appropriate
					if(iOutRow==1000){
						FileIO.writeFile(lstOut, arg1.sDirOutput + "/distances.dist", 0, bAppend);
						bAppend=true;
						lstOut = new ArrayList<String>(1000);
						iOutRow = 0;
					}
				
					//saving current output
					lstOut.add(i + " " + j + " " + (dDistance-dDistanceMin)/dDistanceRange);
					iOutRow++;
				}
			}
		}
		
		//final output
		FileIO.writeFile(lstOut, arg1.sDirOutput + "/distances.dist", 0, bAppend);
		
		//outputting raster location map
		lst3 = new ArrayList<String>();
		for(String s:mapRasterLocation.keySet()){
			lst3.add(s + ";" + mapRasterLocation.get(s).toString());
		}
		FileIO.writeFile(lst3, arg1.sDirOutput + "/distances.locations", 0, false);
	}
	
	/**
	 * Rounds coordinate.
	 * Code assumes 0.5 degree grid cells.
	 * @param d1 Unrounded coordinate.
	 * @return Rounded coordinate to nearest 0.25 or 0.75.
	 */
	private String roundCoordinate(double d1){
		
		//NOTE: this code assumes 0.5 degree cells
		
		//d2 = rounded value
		
		double d2;
		
		//loading d2
		d2 = (double) Math.round(d1);
			
		//rounded down
		if(d2<=d1){
			return Double.toString(d2+0.25);
			
		//rounded up
		}else{
			return Double.toString(d2-0.25);
		}
	}
}

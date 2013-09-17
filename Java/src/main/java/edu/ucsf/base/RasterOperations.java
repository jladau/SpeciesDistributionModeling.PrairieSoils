package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

/**
 * This project contains utilities for postprocessing maps.
 * @author jladau
 */


public class RasterOperations {

	/**
	 * Finds all correlations between pairs of grids in specified directory
	 * @param sDir Directory with grids (rasters)
	 * @return String array with correlations.
	 */
	public static String[][] findCorrelations(String sDir){
		
		//rgsOut = output; one row and column for each variable
		//rgsFiles = list of rasters
		//lstFiles = list of netcdf files
		//cdf1 = first netcdf object
		//cdf2 = second netcdf object
		//lst1 = list of values from first grid
		//lst2 = list of values from second grid
		//rgd1 = values from first grid
		//rgd2 = values from second grid
		//prs1 = PearsonsCorrelation object
		//rgdLocation = current randomly selected location
		//dMonth = current randomly selected month
		//dValue1 = current value from first netcdf object
		//dValue2 = current value from second netcdf object
		//sph1 = spherical geometry object
		//dR = current pearson correlation coefficient
		
		String rgsOut[][]; String rgsFiles[];
		NetCDF_IO cdf1; NetCDF_IO cdf2;
		ArrayList<String> lstFiles; ArrayList<Double> lst1; ArrayList<Double> lst2;
		double rgd1[]; double rgd2[]; double rgdLocation[];
		PearsonsCorrelation prs1;
		SphericalGeometry sph1;
		double dMonth; double dValue1; double dValue2; double dR;
		
		//loading list of rasters
		rgsFiles = FileIO.getFileList(sDir);
		
		//loading list of valid files
		lstFiles = new ArrayList<String>();
		for(int i=0;i<rgsFiles.length;i++){
			if(rgsFiles[i].endsWith(".nc")){
				lstFiles.add(rgsFiles[i]);
			}
		}
		
		//initializing output
		rgsOut = new String[lstFiles.size()+1][lstFiles.size()+1];
		for(int i=0;i<lstFiles.size();i++){
			rgsOut[i+1][0]=lstFiles.get(i).replace(".nc","");
			rgsOut[0][i+1]=lstFiles.get(i).replace(".nc","");
		}
		
		//initializing Pearson Correlation object
		prs1 = new PearsonsCorrelation();
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//looping through pairs of variables
		for(int i=1;i<lstFiles.size();i++){
			
			//loading first netCDF object
			cdf1 = new NetCDF_IO(sDir + "/" + lstFiles.get(i),"reading");
			for(int j=0;j<i;j++){
			
				//updating progress
				System.out.println("Analyzing rasters " + j + "," + i + "...");
				
				//loading second netCDF object
				cdf2 = new NetCDF_IO(sDir + "/" + lstFiles.get(j),"reading");
				
				//clearing lists of values
				lst1 = new ArrayList<Double>();
				lst2 = new ArrayList<Double>();
				
				//loading randomly selected points
				for(int k=0;k<10000;k++){
					
					do{
						rgdLocation = sph1.findRandomPoint();
						dMonth = Math.floor(Math.random()*12 + 1.);
						dValue1 = cdf1.readValue(rgdLocation[0], rgdLocation[1],0.,dMonth);
						dValue2 = cdf2.readValue(rgdLocation[0],rgdLocation[1],0.,dMonth);
					}while(dValue1==-9999 || dValue2==-9999);
					
					//saving values
					lst1.add(dValue1);
					lst2.add(dValue2);
				}
				
				//converting lists of values to double arrays
				rgd1 = new double[lst1.size()];
				rgd2 = new double[lst2.size()];
				for(int k=0;k<lst1.size();k++){
					rgd1[k]=lst1.get(k);
					rgd2[k]=lst2.get(k);
				}
				
				//saving correlation
				dR = prs1.correlation(rgd1, rgd2);
				rgsOut[i+1][j+1]=Double.toString(dR*dR);
				
				//closing netCDF object
				cdf2.closeReader();
			}
			
			//closing netCDF object
			cdf1.closeReader();
		}
		
		//outputting result
		 return rgsOut;
	}
	
	/**
	 * Makes raster showing the minimal or maximal value within a set of rasters.
	 * @param sDir directory with set of rasters to consider
	 * @param sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
	 * @param sType "maximum" or "minimum"
	 * @return Array: first entry with time	
	 */
	public static double[][][] findExtremeRaster(String sDir, String sLandOcean, String sType, String sPathGlobalTopography){
		
		//sDir = directory with set of rasters to consider
		//lstCDF = list of cdf objects to consider
		//rgsFiles = list of files in directory
		//cdf1 = current netCDF object
		//lstElevations = list of elevations
		//lstTimes = list of times
		//rgdMapOut = current output map
		//ncf1 = NetCDF_Operations object
		//sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
		
		ArrayList<Double> lstElevations; ArrayList<Double> lstTimes; ArrayList<NetCDF_IO> lstCDF;
		String rgsFiles[];
		NetCDF_IO cdf1 = null;
		double rgdMapOut[][][];
		NetCDF_Operations ncf1;
		
		//loading list of elevations and times
		lstElevations = new ArrayList<Double>();
		lstTimes = new ArrayList<Double>();
		lstElevations.add(0.);
		for(int k=1;k<=12;k++){
			lstTimes.add((double) k);
		}
		
		//initializing list of cdf objects
		lstCDF = new ArrayList<NetCDF_IO>();
		
		//loading list of cdf objects
		rgsFiles = FileIO.getFileList(sDir);
		for(int i=0;i<rgsFiles.length;i++){
			if(rgsFiles[i].endsWith(".nc")){
				
				//loading current file
				cdf1 = new NetCDF_IO(sDir + "/" + rgsFiles[i],"reading");
				lstCDF.add(cdf1);
			}
		}
		
		//initializing NetCDF_Operations object
		ncf1 = new NetCDF_Operations(sLandOcean,sPathGlobalTopography);
		
		//initializing output map
		rgdMapOut = new double[12][360][720];
		
		//looping through times
		for(int k=1;k<=12;k++){
			
			if(sType.equals("maximum")){
				
				//maximizing maps
				rgdMapOut[k-1] = ncf1.findMaximumGrid(lstCDF, 0., (double) k);
			}else if(sType.equals("minimum")){
			
				//minimizing maps
				rgdMapOut[k-1] = ncf1.findMinimumGrid(lstCDF, 0., (double) k);
			}
		}

		//outputting result
		return rgdMapOut;	
	}

	/**
	 * Makes histograms
	 * @param sDir Directory with the set of rasters for which to construct histograms
	 * @param sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used
	 * @param sTime What to do with different months: "total" for total across months, "maxbelowthreshold" gives histogram for month with maximum mass below threshold, "1-12" for particular month
	 * @return Map, keys are files, values are histograms (in string format)
	 */
	public static Map<String,String[][]> findHistograms(String sDir, String sLandOcean, String sTime,  String sPathGlobalTopography){
		
		//iBins = number of bins.
		//sDir = directory of maps to analyze
		//sPathMapTest = path to map with data for deciding which map to use
		//dElevation = depth to consider
		//cdf1 = cdf object of map in use
		//lstElevations = list of elevations
		//lstTimes = list of times
		//rgsFiles = list of maps to be merged
		//rgd1 = current histogram
		//rgdHist = current total histogram
		//d1 = minimum for current month
		//d2 = maximum for current month
		//dMax = current maximum value
		//dMin = current minimum value
		//rgsOut = output
		//ncf1 = NetCDF_Operations object
		//sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
		//sTime = what to do with different months: "total" for total across months, "maxbelowthreshold" gives histogram for month with maximum mass below threshold, "1-12" for particular month.
		//d3 = maximum mass below threshold
		//d4 = current mass below threshold
		//mapOut = output map
		
		Map<String,String[][]> mapOut;
		ArrayList<Double> lstElevations; ArrayList<Double> lstTimes;
		double dElevation; double dMax; double dMin; double d1; double d2; double d3; double d4;
		double rgd1[][]; double rgdHist[][] = null;
		NetCDF_IO cdf1; 
		String rgsFiles[]; String rgsOut[][];
		int iBins;
		NetCDF_Operations ncf1;
		
		//initializing output
		mapOut = new HashMap<String,String[][]>();
		
		//loading number of bins
		iBins = 100;
		
		//loading list of elevations and times
		lstElevations = new ArrayList<Double>();
		lstTimes = new ArrayList<Double>();
		lstElevations.add(0.);
		for(int k=1;k<=12;k++){
			lstTimes.add((double) k);
		}
		
		//loading depth
		dElevation = 0;
		
		//loading list of files
		rgsFiles = FileIO.getFileList(sDir);
		
		//initializing NetCDF_Operations object
		ncf1 = new NetCDF_Operations(sLandOcean, sPathGlobalTopography);
		
		//looping through files
		for(int l=0;l<rgsFiles.length;l++){
			
			//updating progress
			System.out.println("Analyzing raster " + (l+1) + " of " + rgsFiles.length + "...");
			
			//checking file type
			if(!rgsFiles[l].endsWith(".nc")){
				continue;
			}
			
			//initializing cdf object
			cdf1 = new NetCDF_IO(sDir + "/" + rgsFiles[l],"reading");
			
			//loading minimum and maximum values
			dMin = 1000000000000.;
			dMax = -1000000000000.;
			for(int k=1;k<=12;k++){
				
				ncf1.loadExtremes(cdf1, dElevation, (double) k);
				
				d1 = ncf1.getMinimum();
				d2 = ncf1.getMaximum();
				if(d1<dMin){
					dMin=d1;
				}
				if(d2>dMax){
					dMax=d2;
				}
			}
			
			//loading total across months histogram
			if(sTime.equals("maxbelowthreshold")){
			
				//initializing d3
				d3 = 0.;
				
				//looping through times
				for(int k=1;k<=12;k++){
					
					//loading histogram
					rgd1 = ncf1.findHistogram(cdf1, dElevation, (double) k, dMin, dMax, (dMax-dMin)/((double) iBins));
					
					//loading mass below threshold
					d4 = findMassBelowValue(rgd1,0.);
					if(d4>d3){
						
					    //outputting month
						System.out.println("Saving month " + k + ", raster " + rgsFiles[l] + "...");
						
						//updating d3
						d3 = d4;
					
						rgdHist = new double[rgd1.length][rgd1[0].length];
						for(int i=0;i<rgd1.length;i++){
							rgdHist[i][0]=rgd1[i][0];
							rgdHist[i][1]=rgd1[i][1];
							rgdHist[i][2]=rgd1[i][2];
						}
					}
				}
			}else if(sTime.equals("total")){

				//looping through times
				for(int k=1;k<=12;k++){
					
					//loading histogram
					rgd1 = ncf1.findHistogram(cdf1, dElevation, (double) k, dMin, dMax, (dMax-dMin)/((double) iBins));
					
					if(k==1){
						rgdHist = new double[rgd1.length][rgd1[0].length];
						for(int i=0;i<rgd1.length;i++){
							rgdHist[i][0]=rgd1[i][0];
							rgdHist[i][1]=rgd1[i][1];
						}
					}
					
					//updating total
					for(int i=0;i<rgd1.length;i++){
						rgdHist[i][2]+=rgd1[i][2];
					}
				}
			}else{
				
				//looping through times
				for(int k=1;k<=12;k++){
					
					//checking if correct month
					if(k==Integer.parseInt(sTime)){
					
						//loading histogram
						rgd1 = ncf1.findHistogram(cdf1, dElevation, (double) k, dMin, dMax, (dMax-dMin)/((double) iBins));	
						rgdHist = new double[rgd1.length][rgd1[0].length];
						for(int i=0;i<rgd1.length;i++){
							rgdHist[i][0]=rgd1[i][0];
							rgdHist[i][1]=rgd1[i][1];
							rgdHist[i][2]=rgd1[i][2];
						}
						
						//breaking
						break;
					}
				}
			}
			
			//saving histogram
			rgsOut = new String[rgdHist.length+1][1];
			rgsOut[0][0]="MIN,MAX,AREA";
			for(int i=0;i<rgdHist.length;i++){
				rgsOut[i+1][0]=rgdHist[i][0] + "," + rgdHist[i][1] + "," + rgdHist[i][2];
			}
			mapOut.put(rgsFiles[l].replace(".nc", ".csv"), rgsOut);
			
			//closing reader
			cdf1.closeReader();
		}
		
		//returning result
		return mapOut;
	}
	
	/**
	 * Merges maps
	 * @param sDirFalse Directory with the first set of maps (values from these map will be used if test condition is false)
	 * @param sDirTrue Directory with the second set of maps (values from this map will be used if test condition is true)
	 * @param sPathMapTest Path to the map with the data to be used as a criterion for merging
	 * @return Map giving merged rasters; keys are file names.
	 */
	public static Map<String,double[][][]> mergeMaps(String sDirFalse, String sDirTrue, String sPathMapTest){
		
		//sDirFalse = directory of maps to use if test value is false
		//sDirTrue = directory of maps to use if test value is true
		//sPathMapTest = path to map with data for deciding which map to use
		//dElevation = depth to consider
		//rgdMapOut = current output map
		//cdfFalse = cdf object of map to use if test value is false
		//cdfTrue = cdf object of map to use if test value is true
		//cdfTest = cdf object for test map
		//lstElevations = list of elevations
		//lstTimes = list of times
		//rgsFiles = list of maps to be merged
		//mapOut = output
		
		Map<String,double[][][]> mapOut;
		ArrayList<Double> lstElevations; ArrayList<Double> lstTimes;
		double dElevation;
		double rgdMapOut[][][];
		NetCDF_IO cdfFalse; NetCDF_IO cdfTrue; NetCDF_IO cdfTest;
		String rgsFiles[];
		
		//loading list of elevations and times
		lstElevations = new ArrayList<Double>();
		lstTimes = new ArrayList<Double>();
		lstElevations.add(0.);
		for(int k=1;k<=12;k++){
			lstTimes.add((double) k);
		}
		
		//loading depth
		dElevation = 0;
		
		//loading list of files
		rgsFiles = FileIO.getFileList(sDirFalse);
		
		//initializing test raster
		cdfTest = new NetCDF_IO(sPathMapTest,"reading");
		
		//initializing output
		mapOut = new HashMap<String, double[][][]>();
		
		//looping through files
		for(int l=0;l<rgsFiles.length;l++){
			
			//updating progress
			System.out.println("Analyzing raster " + (l+1) + " of " + rgsFiles.length + "...");
			
			//checking file type
			if(!rgsFiles[l].endsWith(".nc")){
				continue;
			}
			
			//initializing cdf objects
			cdfFalse = new NetCDF_IO(sDirFalse + "/" + rgsFiles[l],"reading");
			cdfTrue = new NetCDF_IO(sDirTrue + "/" + rgsFiles[l],"reading");
			
			//looping through times
			rgdMapOut = new double[12][360][720];
			for(int k=1;k<=12;k++){
				
				//merging maps
				rgdMapOut[k-1] = NetCDF_Operations.mergeGrids(cdfTest, cdfFalse, cdfTrue, (double) k, dElevation);
			}
			
			//saving output
			mapOut.put(rgsFiles[l].replace(".nc","_Merged.nc"), rgdMapOut);
			
			//closing objects
			cdfFalse.closeReader();
			cdfTrue.closeReader();
		}
			
		//closing test reader
		cdfTest.closeReader();
		
		//returning result
		return mapOut;
		
	}

	/**
	 * Finds proportion of area below threshold for each map
	 * @param sDir Directory with the set of rasters
	 * @param sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
	 * @param dThreshold Threshold
	 * @param sFile Name of file to consider; -9999 to consider all files
	 * @return String array giving results
	 */
	public static String[][] findProportionBelowThreshold(String sDir, String sLandOcean, double dThreshold, String sFile, String sPathGlobalTopography){
		
		//sDir = directory of maps to analyze
		//sPathMapTest = path to map with data for deciding which map to use
		//dElevation = depth to consider
		//cdf1 = cdf object of map in use
		//lstElevations = list of elevations
		//lstTimes = list of times
		//rgsFiles = list of maps to be merged
		//rgsOut = output
		//ncf1 = NetCDF_Operations object
		//lst1 = list of output values: sVariable,sMonth,sProportion
		//dThreshold = threshold
		//sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
		
		ArrayList<Double> lstElevations; ArrayList<Double> lstTimes; ArrayList<String> lst1;
		double dElevation;
		NetCDF_IO cdf1; 
		String rgsFiles[]; String rgsOut[][];
		NetCDF_Operations ncf1;
		
		//loading list of elevations and times
		lstElevations = new ArrayList<Double>();
		lstTimes = new ArrayList<Double>();
		lstElevations.add(0.);
		for(int k=1;k<=12;k++){
			lstTimes.add((double) k);
		}
		
		//loading depth
		dElevation = 0;
		
		//loading list of files
		rgsFiles = FileIO.getFileList(sDir);
		
		//initializing NetCDF_Operations object
		ncf1 = new NetCDF_Operations(sLandOcean, sPathGlobalTopography);
		
		//initializing output list
		lst1 = new ArrayList<String>();
		
		//looping through files
		for(int l=0;l<rgsFiles.length;l++){
			
			//updating progress
			//System.out.println("Analyzing raster " + (l+1) + " of " + rgsFiles.length + "...");
			
			//checking if file should be used
			if(!sFile.equals("-9999") && !sFile.equals(rgsFiles[l])){
				continue;
			}
			
			//checking file type
			if(!rgsFiles[l].endsWith(".nc")){
				continue;
			}
			
			//initializing cdf object
			cdf1 = new NetCDF_IO(sDir + "/" + rgsFiles[l],"reading");
			
			//looping through times
			for(int k=1;k<=12;k++){
				
				//loading proportion and saving
				lst1.add(rgsFiles[l].replace(".nc","") + "," + k + "," + ncf1.findProportionBelowThreshold(cdf1, dElevation, (double) k, dThreshold));
			}
			
			//closing reader
			cdf1.closeReader();
		}
		
		//outputting results
		rgsOut = new String[lst1.size()+1][1];
		rgsOut[0][0] = "VARIABLE,MONTH,AREA_BELOW_THRESHOLD,TOTAL_AREA,PROPORTION";
		for(int i=0;i<lst1.size();i++){
			rgsOut[i+1][0]=lst1.get(i);
		}
		
		//returning results
		return rgsOut;
	}
	
	
	
	/**
	 * Finds proportion of area below threshold for each map
	 * @param sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
	 * @param dThreshold Threshold
	 * @param rgdGrid Grid with data.
	 * @param sVariable Variable
	 * @return String array giving results
	 */
	public static String[][] findProportionBelowThreshold(String sVariable, int iMonth, double rgdGrid[][], String sLandOcean, double dThreshold, String sPathGlobalTopography){
		
		//sPathMapTest = path to map with data for deciding which map to use
		//dElevation = depth to consider
		//lstElevations = list of elevations
		//lstTimes = list of times
		//rgsOut = output
		//ncf1 = NetCDF_Operations object
		//dThreshold = threshold
		//sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
		
		ArrayList<Double> lstElevations; ArrayList<Double> lstTimes;
		double dElevation;
		String rgsOut[][];
		NetCDF_Operations ncf1;
		
		//loading list of elevations and times
		lstElevations = new ArrayList<Double>();
		lstTimes = new ArrayList<Double>();
		lstElevations.add(0.);
		for(int k=1;k<=12;k++){
			lstTimes.add((double) k);
		}
		
		//loading depth
		dElevation = 0;
		
		//initializing NetCDF_Operations object
		ncf1 = new NetCDF_Operations(sLandOcean, sPathGlobalTopography);
		
		//outputting results
		rgsOut = new String[2][1];
		rgsOut[0][0] = "VARIABLE,MONTH,AREA_BELOW_THRESHOLD,TOTAL_AREA,PROPORTION,TOTAL_POSSIBLE_AREA";
		rgsOut[1][0] = sVariable + "," + iMonth + "," + ncf1.findProportionBelowThreshold(rgdGrid, dElevation, (double) iMonth, dThreshold);
		
		
		//returning results
		return rgsOut;
	}

	/**
	 * Finds the mass in the current histogram that is below the specified value.
	 * @param rgdHist Histogram.
	 * @param dCutoff Cutoff Value.
	 * @return Total mass below cutoff value;
	 */
	private static double findMassBelowValue(double rgdHist[][], double dCutoff){
		
		//d1 = mass below value
		
		double d1 = 0;
		
		//looping through histogram values until cutoff value is found
		for(int i=0;i<rgdHist.length;i++){
			
			//checking if cutoff value is exceeded
			if(rgdHist[i][1]<=dCutoff){
				d1+=rgdHist[i][2];
			}else{
				if(i==0){
					d1+=Interpolation.interpolateLinear(dCutoff, rgdHist[i][0], rgdHist[i][1], 0, rgdHist[i][2]);
				}else{
					d1+=Interpolation.interpolateLinear(dCutoff, rgdHist[i][0], rgdHist[i][1], rgdHist[i-1][2], rgdHist[i][2]);
				}
				break;
			}
		}
		
		//returning result
		return d1;
	}
}

package edu.ucsf.base;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * This class contains utilities for operating on netcdf grids; for example, adding two grids together.
 * @author jladau
 */
public class NetCDF_Operations {

	//rgdAreaGrid = grid gives the area of each cell
	//dMin = minimum value for current grid
	//dMax = maximum value for current grid
	//rgbLandGrid = grid returns true if cell should be included in analyses and false otherwise
	//ptl1 = point on land object
	
	private boolean rgbLandGrid[][];
	private double rgdAreaGrid[][];
	private double dMin;
	private double dMax;
	
	/**
	 * Constructor.
	 * @param sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
	 */
	public NetCDF_Operations(String sLandOcean, String sPathGlobalTopography){

		//loading land grid
		loadLandGrid(sLandOcean,sPathGlobalTopography);
		
		//loading area grid
		rgdAreaGrid = loadAreaGrid();
	}
	
	/**
	 * Finds the proportion of area with non-error values above threshold.
	 * @param cdf1 Grids to consider.
	 * @return String with area below threshold, total area, and proportion of area below threshold.
	 */
	public String findProportionBelowThreshold(NetCDF_IO cdf1, double dElevation, double dTime, double dThreshold){
		
		//dPass = total area below threshold
		//dTotal = total area with non-error
		//rgdGrid = grid.
		
		double dPass; double dTotal;
		double rgdGrid[][];
		
		//initializing output: first grid
		rgdGrid = cdf1.readGrid(0.5, dElevation, dTime);
		
		//looping through entries
		dTotal=0;
		dPass=0;
		for(int i=0;i<360;i++){
			for(int j=0;j<720;j++){
				if(rgbLandGrid[i][j]==true){
					if(rgdGrid[i][j]!=-9999){
						dTotal+=rgdAreaGrid[i][j];
						if(rgdGrid[i][j]<=dThreshold){
							dPass+=rgdAreaGrid[i][j];
						}
					}
				}
			}
		}

		//outputting result
		if(dTotal==0){
			return dPass + "," + dTotal + ",-9999";
		}else{
			return dPass + "," + dTotal + "," + dPass/dTotal;
		}
	}
	
	/**
	 * Finds the proportion of area with non-error values above threshold.
	 * @param cdf1 Grids to consider.
	 * @return String with area below threshold, total area, proportion of area below threshold, possible area specified by land/marine variable.
	 */
	public String findProportionBelowThreshold(double rgdGrid[][], double dElevation, double dTime, double dThreshold){
		
		//dPass = total area below threshold
		//dTotal = total area with non-error
		//dTotalPossibleArea = total possible area
		
		double dPass; double dTotal;double dTotalPossibleArea;
		
		//looping through entries
		dTotal=0;
		dPass=0;
		dTotalPossibleArea=0;
		for(int i=0;i<360;i++){
			for(int j=0;j<720;j++){
				if(rgbLandGrid[i][j]==true){
					dTotalPossibleArea+=rgdAreaGrid[i][j];
					if(rgdGrid[i][j]!=-9999){
						dTotal+=rgdAreaGrid[i][j];
						if(rgdGrid[i][j]<=dThreshold){
							dPass+=rgdAreaGrid[i][j];
						}
					}
				}
			}
		}
	
		//outputting result
		if(dTotal==0){
			return dPass + "," + dTotal + ",-9999," + dTotalPossibleArea;
		}else{
			return dPass + "," + dTotal + "," + dPass/dTotal + "," + dTotalPossibleArea;
		}
	}

	/**
	 * Finds the proportion of area with non-error values above threshold.
	 * @param cdf1 Grids to consider.
	 * @return String with area below threshold, total area, and proportion of area below threshold.
	 */
	public String findProportionAboveThreshold(NetCDF_IO cdf1, double dElevation, double dTime, double dThreshold){
		
		//dPass = total area below threshold
		//dTotal = total area with non-error
		//rgdGrid = grid.
		
		double dPass; double dTotal;
		double rgdGrid[][];
		
		//initializing output: first grid
		rgdGrid = cdf1.readGrid(0.5, dElevation, dTime);
		
		//looping through entries
		dTotal=0;
		dPass=0;
		for(int i=0;i<360;i++){
			for(int j=0;j<720;j++){
				if(rgbLandGrid[i][j]==true){
					if(rgdGrid[i][j]!=-9999){
						dTotal+=rgdAreaGrid[i][j];
						if(rgdGrid[i][j]>=dThreshold){
							dPass+=rgdAreaGrid[i][j];
						}
					}
				}
			}
		}

		//outputting result
		if(dTotal==0){
			return dPass + "," + dTotal + ",-9999";
		}else{
			return dPass + "," + dTotal + "," + dPass/dTotal;
		}
	}
	
	/**
	 * Finds the grid giving the minimum value of a set of grids.
	 * @param lst1 List of grids to consider.
	 * @return Grid with minimum values in each cell.
	 */
	public double[][] findMinimumGrid(ArrayList<NetCDF_IO> lst1, double dElevation, double dTime){
		
		//rgd1 = output
		//rgdGrid = current grid under consideration
		
		double rgd1[][]; double rgdGrid[][];
		
		//updating progress
		System.out.println("Analyzing raster 1 of " + lst1.size() + "...");
		
		//initializing output: first grid
		rgdGrid = lst1.get(0).readGrid(0.5, dElevation, dTime);
		rgd1 = new double[360][720];
		for(int i=0;i<360;i++){
			for(int j=0;j<720;j++){
				if(rgbLandGrid[i][j]==true){
					rgd1[i][j]=rgdGrid[i][j];
				}else{
					rgd1[i][j]=-9999;
				}
			}
		}
		
		//looping through grids to find minimum
		for(int k=1;k<lst1.size();k++){
			
			//updating progress
			System.out.println("Analyzing raster " + (k+1) + " of " + lst1.size() + "...");
			
			//loading current grid
			rgdGrid = lst1.get(k).readGrid(0.5, dElevation, dTime);
			
			//looping through entries
			for(int i=0;i<360;i++){
				for(int j=0;j<720;j++){
					if(rgbLandGrid[i][j]==true){
						if((rgdGrid[i][j]<rgd1[i][j] || rgd1[i][j]==-9999) && rgdGrid[i][j]!=-9999){
							rgd1[i][j]=rgdGrid[i][j];
						}
					}
				}
			}
		}
		
		//outputting result
		return rgd1;
	}
	
	/**
	 * Finds the grid giving the maximum value of a set of grids.
	 * @param lst1 List of grids to consider.
	 * @return Grid with minimum values in each cell.
	 */
	public double[][] findMaximumGrid(ArrayList<NetCDF_IO> lst1, double dElevation, double dTime){
		
		//rgd1 = output
		//rgdGrid = current grid under consideration
		
		double rgd1[][]; double rgdGrid[][];
		
		//updating progress
		System.out.println("Analyzing raster 1 of " + lst1.size() + "...");
		
		//initializing output: first grid
		rgdGrid = lst1.get(0).readGrid(0.5, dElevation, dTime);
		rgd1 = new double[360][720];
		for(int i=0;i<360;i++){
			for(int j=0;j<720;j++){
				if(rgbLandGrid[i][j]==true){
					rgd1[i][j]=rgdGrid[i][j];
				}else{
					rgd1[i][j]=-9999;
				}
			}
		}
		
		//looping through grids to find minimum
		for(int k=1;k<lst1.size();k++){
			
			//updating progress
			System.out.println("Analyzing raster " + (k+1) + " of " + lst1.size() + "...");
			
			//loading current grid
			rgdGrid = lst1.get(k).readGrid(0.5, dElevation, dTime);
			
			//looping through entries
			for(int i=0;i<360;i++){
				for(int j=0;j<720;j++){
					if(rgbLandGrid[i][j]==true){
						if((rgdGrid[i][j]>rgd1[i][j] || rgd1[i][j]==-9999) && rgdGrid[i][j]!=-9999){
							rgd1[i][j]=rgdGrid[i][j];
						}
					}
				}
			}
		}
		
		//outputting result
		return rgd1;
	}
	
	/**
	 * This code merges two grids, using values from one if a test grid is false and values from the other if a test grid is true
	 * @param cdfTest Test grid.
	 * @param cdfFalse Grid from which to take values if test grid value is false.
	 * @param cdfTrue Grid from which to take values if test grid is true.
	 * @param dTime Time to consider.
	 * @param dElevation Elevation to consider.
	 * @return Merged grid.
	 */
	public static double[][] mergeGrids(NetCDF_IO cdfTest, NetCDF_IO cdfFalse, NetCDF_IO cdfTrue, double dTime, double dElevation){
		
		//rgdMapTest = test grid
		//rgdMapFalse = false grid
		//rgdMapTrue = true grid
		//rgdMapOut = output
		
		double rgdMapTest[][]; double rgdMapFalse[][]; double rgdMapTrue[][]; double rgdMapOut[][];
		
		//loading maps
		rgdMapTrue = cdfTrue.readGrid(0.5,dElevation,dTime);
		rgdMapFalse = cdfFalse.readGrid(0.5,dElevation,dTime);
		rgdMapTest = cdfTest.readGrid(0.5,dElevation,dTime);
		
		//initializing output
		rgdMapOut = new double[rgdMapTest.length][rgdMapTest[0].length];
		
		//looping through rows and columns
		for(int i=0;i<rgdMapOut.length;i++){
			for(int j=0;j<rgdMapOut[0].length;j++){
				
				//checking for error
				if(rgdMapTest[i][j]==-9999){
					rgdMapOut[i][j]=-9999;
				}else{
				
					//checking if condition is met
					if(rgdMapTest[i][j]==0){
						if(rgdMapFalse[i][j]==-9999){
							rgdMapOut[i][j]=-9999;
						}else{
							rgdMapOut[i][j]=rgdMapFalse[i][j];
						}
					}else{
						if(rgdMapTrue[i][j]==-9999){
							rgdMapOut[i][j]=-9999;
						}else{
							rgdMapOut[i][j]=rgdMapTrue[i][j];
						}
					}
				}
			}
		}
		
		//outputting result
		return rgdMapOut;
	}
	
	/**
	  * Finds a histogram of the non-error values in cdf grid.
	  * @param cdf1 NetCDF object to consider.
	  * @param dElevation Elevation to consider.
	  * @param dTime Time to consider.
	  * @param dMin Minimum value for histogram.
	  * @param dMax Maximum value for histogram.
	  * @param dStep Step size for histogram.
	  * @return First column with bin start, second column with bin end, third column with count.
	  */
	public double[][] findHistogram(NetCDF_IO cdf1, double dElevation, double dTime, double dMin, double dMax, double dStep){
		
		//map1(dValue) = contains the bin index for given value.
		//iBin = current bin
		//dCeiling = ceiling value for current bin
		//rgd1 = grid from netcdf
		//rgd2 = output
		//dMaxHist = maximum value in histogram
		
		TreeMap<Double,Integer> map1;
		int iBin;
		double dCeiling; double dMaxHist;
		double rgd1[][]; double rgd2[][];
		
		//loading map of bins
		map1 = new TreeMap<Double,Integer>();
		iBin=0;
		dCeiling = dMin + dStep;
		while(dCeiling-dStep<dMax){
			map1.put(dCeiling, iBin);
			dCeiling+=dStep;
			iBin++;
		}
		
		//loading maximum value in histogram
		dMaxHist = map1.lastKey();
		
		//initializing output
		rgd2 = new double[map1.size()][3];
		for(int i=0;i<rgd2.length;i++){
			rgd2[i][0]=((double) i)*dStep+dMin;
			rgd2[i][1]=rgd2[i][0]+dStep;
		}
		
		//looping through values
		rgd1 = cdf1.readGrid(0.5, dElevation, dTime);
		for(int i=0;i<360;i++){
			for(int j=0;j<720;j++){
				if(rgd1[i][j]!=-9999){
					if(rgbLandGrid[i][j]==true){
						if(dMin<=rgd1[i][j] && rgd1[i][j]<=dMaxHist){
							iBin = map1.get(map1.ceilingKey(rgd1[i][j]));
							rgd2[iBin][2]+=rgdAreaGrid[i][j];
						}
					}
				}
			}
		}
		
		//outputting result
		return rgd2;
	}
	
	 /**
	  * Finds a histogram of the non-error values in cdf grid.
	  * @param cdf1 NetCDF object to consider.
	  * @param dElevation Elevation to consider.
	  * @param dTime Time to consider.
	  * @param dMin Minimum value for histogram.
	  * @param dMax Maximum value for histogram.
	  * @param dStep Step size for histogram.
	  * @param sTransformation Transformation to apply.
	  * @return First column with bin start, second column with bin end, third column with count.
	  */
	public double[][] findHistogram(NetCDF_IO cdf1, double dElevation, double dTime, double dMin, double dMax, double dStep, String sTransformation){
		
		//map1(dValue) = contains the bin index for given value.
		//iBin = current bin
		//dCeiling = ceiling value for current bin
		//rgd1 = grid from netcdf
		//rgd2 = output
		//dMaxHist = maximum value in histogram
		//d1 = log offset
		
		TreeMap<Double,Integer> map1;
		int iBin;
		double dCeiling; double dMaxHist; double d1;
		double rgd1[][]; double rgd2[][];
		
		//loading map of bins
		map1 = new TreeMap<Double,Integer>();
		iBin=0;
		dCeiling = dMin + dStep;
		while(dCeiling-dStep<dMax){
			map1.put(dCeiling, iBin);
			dCeiling+=dStep;
			iBin++;
		}
		
		//loading maximum value in histogram
		dMaxHist = map1.lastKey();
		
		//initializing output
		rgd2 = new double[map1.size()][3];
		for(int i=0;i<rgd2.length;i++){
			rgd2[i][0]=((double) i)*dStep+dMin;
			rgd2[i][1]=rgd2[i][0]+dStep;
		}
		
		//loading grid
		rgd1 = cdf1.readGrid(0.5, dElevation, dTime);
		if(sTransformation.startsWith("log")){
			d1 = Double.parseDouble(sTransformation.split(":")[1]);
			for(int i=0;i<360;i++){
				for(int j=0;j<720;j++){
					if(rgd1[i][j]!=-9999){
						rgd1[i][j]=Math.log(rgd1[i][j]+d1);
					}
				}	
			}
		}
		
		//looping through values
		for(int i=0;i<360;i++){
			for(int j=0;j<720;j++){
				if(rgd1[i][j]!=-9999){
					if(rgbLandGrid[i][j]==true){
						if(dMin<=rgd1[i][j] && rgd1[i][j]<=dMaxHist){
							iBin = map1.get(map1.ceilingKey(rgd1[i][j]));
							rgd2[iBin][2]+=rgdAreaGrid[i][j];
						}
					}
				}
			}
		}
		
		//outputting result
		return rgd2;
	}
	
	/**
	 * Finds minimum and maximum values in cdf object.
	 * @param cdf1 NetCDF_IO object.
	 * @param dElevation Elevation of interest.
	 * @param dTime Time of interest.
	 */
	public void loadExtremes(NetCDF_IO cdf1, double dElevation, double dTime){
		
		//rgd1 = grid of values

		double rgd1[][];
		
		//loading grid
		rgd1 = cdf1.readGrid(0.5, dElevation, dTime);
		
		//initializing minimum and maximum
		dMin = 100000000000000.;
		dMax = -100000000000000.;
		
		//looping through entries
		for(int i=0;i<360;i++){
			for(int j=0;j<720;j++){
				if(rgbLandGrid[i][j]==true){
					if(rgd1[i][j]<dMin && rgd1[i][j]!=-9999){
						dMin = rgd1[i][j];
					}
					if(rgd1[i][j]>dMax && rgd1[i][j]!=-9999){
						dMax = rgd1[i][j];
					}
				}
			}
		}
	}
	
	/**
	 * Gets maximum for last NetCDF object
	 * @return Maximum.
	 */
	public double getMaximum(){
		return dMax;
		
	}
	
	/**
	 * Gets minimum for last NetCDF object
	 * @return Minimum.
	 */
	public double getMinimum(){
		return dMin;
		
	}

	/**
	 * Loads an array containing grid areas.
	 * @return Array with cell areas.
	 */
	private double[][] loadAreaGrid(){
		
		//sph1 = spherical geometry object
		//rgd1 = output
		//dLat = current latitude
		//dLng = current longitude
		
		double dLat; double dLng;
		SphericalGeometry sph1;
		double[][] rgd1;
		
		//initializing output
		rgd1 = new double[360][720];
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//looping through latitudes
		dLat = 90.25;
		for(int i=0;i<360;i++){
			
			//updating dLat
			dLat-=0.5;
			
			//looping through longitudes
			dLng=-180.25;
			for(int j=0;j<720;j++){
			
				//updating dLng
				dLng+=0.5;
				
				//loading entry
				rgd1[i][j]=sph1.findAreaCell(dLat, dLng, 0.5, 0.5);
			}
		}
		
		//returning result
		return rgd1;
	}

	/**
	 * Loads Land Grid
	 * @param sLandOcean "terrestrial" if only land observations should be used, "marine" if only ocean observations should be used, "both" if all observations should be used.
	*/
	private void loadLandGrid(String sLandOcean, String sPathGlobalTopography){
		
		//ptl1 = PointOnLand object
		//dLat = current latitude
		//dLng = current longitude
		
		PointOnLand ptl1;
		double dLat; double dLng;
		
		//initializing point on land object
		ptl1 = new PointOnLand(sPathGlobalTopography);
		//ptl1 = new PointOnLand("/home/jladau/Documents/Research/Java/PointOnLand Data/globaltopography.nc");
	
		//initializing land grid
		rgbLandGrid = new boolean[360][720];
		
		//checking if null (i.e., both ocean and land
		if(sLandOcean.equals("both")){
			for(int i=0;i<360;i++){
				for(int j=0;j<720;j++){
					rgbLandGrid[i][j]=true;
				}
			}
		}
		
		//looping through latitudes and longitudes
		dLat = 90.25;
		for(int i=0;i<360;i++){
			dLat -= 0.5;
			dLng = -180.25;
			for(int j=0;j<720;j++){
				dLng+=0.5;
				
				//checking value
				if(ptl1.isOnLand(dLat, dLng)==true){
					if(sLandOcean.equals("terrestrial")){	
						rgbLandGrid[i][j]=true;
					}else{
						rgbLandGrid[i][j]=false;
					}
				}else{
					if(sLandOcean.equals("marine")){	
						rgbLandGrid[i][j]=true;
					}else{
						rgbLandGrid[i][j]=false;
					}
				}
			}
		}
	}	
}

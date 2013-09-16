package edu.ucsf.sdm;

import java.util.ArrayList;

import edu.ucsf.base.ClusterIterator;
import edu.ucsf.base.RasterIterator;

/**
 * Beta diversity output map object
 * @author jladau
 */

public abstract class SDMPrediction {

	//rgdMap = output map (non-vector)
	//arg1 = arguments object
	//obs1 = observed data object
	//lstMap = output list for partial map; entries are lat, lon, vert, time, value
	
	public double rgdMap[][];
	public ArrayList<String> lstMap;
	private SDMArguments arg1;
	
	public SDMPrediction(SDMArguments arg1){
		this.arg1 = arg1;
	}
	
	/**
	 * Loads partial map (for parallel computing)
	 * @param iVertTimeIndex Elevation-time combination index
	 */
	public void loadPartialMap(int iVertTimeIndex, int iTaskID, int iTotalTasks){
		
		//rit1 = raster iterator
		//itr1 = ClusterIterator
		
		RasterIterator rit1;
		ClusterIterator itr1;
			
		//initializing map
		initializePartialMap(iVertTimeIndex,iTaskID);
		
		//looping through points
		itr1 = new ClusterIterator(iTaskID, iTotalTasks);
		rit1 = new RasterIterator(arg1.lstVert.get(iVertTimeIndex),arg1.lstTime.get(iVertTimeIndex),"NA");
		while(rit1.hasNext()){
			
			//loading next
			rit1.next();
			itr1.next();
			
			//updating progress
			rit1.updateProgress();
			
			//checking if task should be performed
			if(itr1.bInclude==true){
			//if(iTaskID == -9999 || ( iTotalTasks != -9999 && (rit1.iCounter % iTotalTasks) == (iTaskID-1))){
			
				//outputting value
				loadPartialMapValue(rit1);
			}
		}
	}
	
	/**
	 * Loads value into partial map
	 * @param rit1 Raster iterator with location
	 */
	public abstract void loadPartialMapValue(RasterIterator rit1);
	
	/**
	 * Loads map
	 * @param iVertTimeIndex Elevation-time combination index
	 */
	public void loadMap(int iVertTimeIndex, int iTaskID){
		
		//rit1 = raster iterator
		
		RasterIterator rit1;
	
		//initializing map
		initializeMap(iVertTimeIndex,iTaskID);
		
		//looping through points
		rit1 = new RasterIterator(arg1.lstVert.get(iVertTimeIndex),arg1.lstTime.get(iVertTimeIndex),"NA");
		while(rit1.hasNext()){
			
			//loading next
			rit1.next();
			
			//updating progress
			rit1.updateProgress();
			
			//loading value
			loadMapValue(rit1);
		}
	}
	
	/**
	 * Loads value into map
	 * @param rit1 Raster iterator with location
	 */
	public abstract void loadMapValue(RasterIterator rit1);
	
	/**
	 * Initializes partial map
	 * @param iVertTimeIndex
	 * @param iTaskID
	 */
	public abstract void initializePartialMap(int iVertTimeIndex, int iTaskID);
	
	/**
	 * Initializes map
	 * @param iVertTimeIndex
	 * @param iTaskID
	 */
	public abstract void initializeMap(int iVertTimeIndex, int iTaskID);
	
	/**
	 * Generates predictions to independent data set
	 * @param rgsData Independent data set to predict to
	 */
	public abstract ArrayList<String> predictToIndependentDataSet(String rgsData[][]);
	
}

package edu.ucsf.ranges;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import edu.ucsf.base.*;

/**
 * Loads range by looking at appropriate dbf file (in csv format) and shapefile (in text format using shpdump)
 * @author jladau
 */
public class SmoothedGeographicRanges {
	
	//bfr1 = shapefile buffered reader
	//sSpecies = current species
	//bDone = flag for whether done
	//rgsPreviousLine = previous line
	//mapSmoothedRange(sSpecies) = returns the smoothed range for the given species
	//dRadius = radius of smoothing
	//mapArea(sSpecies) = returns the area of the given smoothed range (in square km)
	//mapPerimeter(sSpecies) = returns the perimeter of the given smoothed range (in km)
	//mapDiameter(sSpecies) = returns the diameter of the given range (in km)
	//dTotalRangeArea = total smoothed range area
	//dTotalRangePerimeter = total smoothed range perimeter
	
	private BufferedReader bfr1;
	private String sSpecies="hdgsjdfk";
	private boolean bDone = false;
	private String[] rgsPreviousLine = null;
	public Map<String,Polygon> mapSmoothedRange;
	public double dRadius;
	public Map<String,Double> mapArea;
	public Map<String,Double> mapPerimeter;
	public Map<String,Double> mapDiameter;
	public double dTotalRangeArea;
	public double dTotalRangePerimeter;
	
	/**
	 * Constructor.
	 * @param sSmoothedRangePath Path to smoothed range file.
	 * @param dRadius Radius of smoothing
	 */
	public SmoothedGeographicRanges(Arguments arg1){

		//saving radius
		this.dRadius = arg1.getValueDouble("dRadius");
		
		//loading ranges
		this.loadRanges(arg1.getValueString("sPathSmoothedRanges"));
		
		//loading areas and perimeters and diameters
		loadPerimetersAreasDiameters(arg1.getValueString("sPathSmoothedRanges"));
	}
	
	/**
	 * Constructor.
	 * @param sSmoothedRangePath Path to smoothed range file.
	 * @param dRadius Radius of smoothing
	 * @param bLoadPerimeterArea True if load perimeter area, false otherwise
	 */
	public SmoothedGeographicRanges(Arguments arg1, boolean bLoadPerimeterArea){

		//saving radius
		this.dRadius = arg1.getValueDouble("dRadius");
		
		//loading ranges
		this.loadRanges(arg1.getValueString("sPathSmoothedRanges"));
		
		//loading areas and perimeters and diameters
		if(bLoadPerimeterArea==true){
			loadPerimetersAreasDiameters(arg1.getValueString("sPathSmoothedRanges"));
		}
	}
	
	private void loadPerimetersAreasDiameters(String sSmoothedRangePath){
		
		//rgsData = file with perimeters and areas
		
		String rgsData[][];
		
		//loading data
		rgsData = FileIO.readFile(sSmoothedRangePath.replace(".csv", "_PerimeterArea.csv"), ",");
		
		//initializing arrays
		mapArea = new HashMap<String,Double>();
		mapPerimeter = new HashMap<String,Double>();
		if(rgsData[0].length==4){
			mapDiameter = new HashMap<String,Double>();
		}
			
		//looping through data
		dTotalRangeArea = 0;
		dTotalRangePerimeter = 0;
		for(int i=1;i<rgsData.length;i++){
			mapArea.put(rgsData[i][0], Double.parseDouble(rgsData[i][2]));
			dTotalRangeArea+=mapArea.get(rgsData[i][0]);
			mapPerimeter.put(rgsData[i][0], Double.parseDouble(rgsData[i][1]));
			dTotalRangePerimeter+=mapPerimeter.get(rgsData[i][0]);
			if(rgsData[0].length==4){
				mapDiameter.put(rgsData[i][0], Double.parseDouble(rgsData[i][3]));
			}
		}
	}
	
	private void loadRanges(String sSmoothedRangePath){
		
		//lstRange = current range in list format
		//ply1 = current range in polygon format
		
		ArrayList<String[]> lstRange;
		Polygon ply1;
		
		//initializing buffered reader
		try {
			bfr1 = new BufferedReader(new FileReader(sSmoothedRangePath));
		} catch (FileNotFoundException e1) {
			System.out.println("ERROR: file not found.");
		}
		this.readNextLine();
		
		//initializing map of ranges
		mapSmoothedRange = new HashMap<String,Polygon>(5000);
		
		//loading first range
		lstRange = this.getNextRange();
		
		//looping through ranges
		while(lstRange!=null){
		
			//updating progress
			System.out.println("Loading range of " + this.getCurrentSpecies() + "...");
			
			//loading current range
			ply1 = new Polygon(lstRange,1234,true);
			
			//saving polygon
			mapSmoothedRange.put(this.getCurrentSpecies(), ply1);
			
			//loading next range
			lstRange = this.getNextRange();
		}
		
		//closing lrs object
		this.closeReader();
	}
	
	/**
	 * Closes reader.
	 */
	private void closeReader(){
		try {
			bfr1.close();
		} catch (IOException e) {
		}
	}
	
	/**
	 * Returns the current species.
	 * @return Name of the current species
	 */
	private String getCurrentSpecies(){
		return sSpecies.replace("\""," ");
	}
	
	/**
	 * Gets the next range
	 * @return Next range in list format: entries are polygon ID, longitude, latitude
	 */
	private ArrayList<String[]> getNextRange(){
		
		//sLine = current line
		//rgs1 = current vertex being added
		//rgs2 = current line in split format
		//lst1 = output
		
		String sLine;
		ArrayList<String[]> lst1;
		String rgs1[]; String rgs2[];
		
		//checking if done
		if(bDone == true){
			this.closeReader();
			return null;
		}
		
		//initializing output
		lst1 = new ArrayList<String[]>(10000);
		
		//loading previous line if available
		if(rgsPreviousLine!=null){
			rgs1 = new String[3];
			rgs1[0]=rgsPreviousLine[1];
			rgs1[1]=rgsPreviousLine[2];
			rgs1[2]=rgsPreviousLine[3];
			lst1.add(rgs1);
			sSpecies = rgsPreviousLine[0];
		}
		
		//looping until new species found
		do{
		
			//loading current line
			sLine = this.readNextLine();
			
			//checking if done
			if(sLine==null){
				break;
			}
			
			//splitting current line
			rgs2 = sLine.split(",");
			
			//loading species if second line
			if(sSpecies.equals("hdgsjdfk")){
				sSpecies = rgs2[0];
			}
			
			//checking current species
			if(!rgs2[0].equals(sSpecies)){
				
				//saving line
				rgsPreviousLine = rgs2;
	
				//exiting
				break;
			}
			
			//loading current vertex
			rgs1 = new String[3];
			rgs1[0] = rgs2[1];
			rgs1[1] = rgs2[2];
			rgs1[2] = rgs2[3];
			lst1.add(rgs1);
			
		}while(sLine!=null);
		
		//checking if done
		if(sLine==null){
			bDone = true;
		}
		
		//returning result
		return lst1;
	}
	
	/**
	 * Returns the next line of the shapefile
	 * @return Next line of shapefile; null if done.
	 */
	private String readNextLine(){
		
		//returning line
		try {
			return bfr1.readLine();
		} catch (IOException e) {
			return null;
		}
	}
	
}

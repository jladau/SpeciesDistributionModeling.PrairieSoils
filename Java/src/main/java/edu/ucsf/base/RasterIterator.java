package edu.ucsf.base;

import java.util.Iterator;

/**
 * Iterator for rasters
 * @author jladau
 *
 */

public class RasterIterator implements Iterator{

	//iRow = current output row
	//iCol = current output column
	//dLat = current latitude
	//dLon = current longitude
	//dVert = elevation
	//dTime = time
	//iCounter = counter
	//sVar = variable name for raster
	//dLatPrevious = latitude of previous value
	
	public String sVar;
	public int iRow; 
	public int iCol; 
	public int iCounter;
	public double dLat; 
	public double dLon;
	public double dLatPrevious;
	public double dVert;
	public double dTime;
	
	/**
	 * Constructor
	 * @param sElevationTime Elevation and time argument; can be null
	 * @param sVar Variable name
	 */
	public RasterIterator(double dVert, double dTime, String sVar){
		dLat = 90.25;
		iRow = -1;
		iCol = 719;
		iCounter = 0;
		this.dTime = dTime;
		this.dVert = dVert;
		this.sVar = sVar;
		dLatPrevious = -9999;
	}
	
	/**
	 * Returns true if next iteration not complete; false otherwise
	 */
	public boolean hasNext(){
		
		if(iRow==359 && iCol==719){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Returns counter associated with iterator.
	 */
	public Object next(){
		
		//saving latitude
		dLatPrevious = dLat;
		
		//updating column
		iCol++;
		
		//checking if new row
		if(iCol==720){
			iRow++;
			iCol=0;
			dLat-=0.5;
			dLon=-179.75;
		}else{
			dLon+=0.5;
		}
		
		//rounding numbers
		dLat = round(dLat);
		dLon = round(dLon);
		
		//updating counter
		iCounter++;
		
		//returning result
		return iCounter;
	}
	
	public void remove(){
	}
	
	public void updateProgress(){
		if(dLat!=dLatPrevious){
			System.out.println("Analyzing latitude " + dLat + "...");
		}
	}
	
	public RasterLocation getRasterLocation(){
		return new RasterLocation(dLat,dLon,dVert,dTime,iRow,iCol,sVar);
	}
	
	/**
	 * Rounds to the nearest 0.25
	 */
	private double round(double d1){
	
		//d2 = temporary value
		
		double d2;
		
		d2 = d1 * 4.;
		d2 = Math.round(d2);
		return d2/4.;
	}
}

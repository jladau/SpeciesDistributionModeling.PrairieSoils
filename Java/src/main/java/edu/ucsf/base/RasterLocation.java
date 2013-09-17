package edu.ucsf.base;

/**
 * Contains fields for saving a raster location
 * @author jladau
 */

public class RasterLocation {

	//dLat = latitude
	//dLon = longitude
	//dVert = elevation
	//dTime = time
	//iRow = row
	//iCol = col
	//sVar = variable
	
	public double dLat;
	public double dLon;
	public double dVert;
	public double dTime;
	public int iRow;
	public int iCol;
	public String sVar;
	
	public RasterLocation(double dLat, double dLon, double dVert, double dTime, int iRow, int iCol, String sVar){
		this.dLat = dLat;
		this.dLon = dLon;
		this.dVert = dVert;
		this.dTime = dTime;
		this.iRow = iRow;
		this.iCol = iCol;
		this.sVar = sVar;
	}
	
	public RasterLocation(String sLocation){
		
		//rgs1 = location in split form
		//rgs2 = current attribute in split form
		
		String rgs1[]; String rgs2[];
		
		rgs1 = sLocation.split(",");
		for(int i=0;i<rgs1.length;i++){
			
			rgs2 = rgs1[i].split(":");
			
			if(rgs2[0].equals("lat")){
				this.dLat = Double.parseDouble(rgs2[1]);
			}else if(rgs2[0].equals("lon")){
				this.dLon = Double.parseDouble(rgs2[1]);
			}else if(rgs2[0].equals("vert")){
				this.dVert = Double.parseDouble(rgs2[1]);
			}else if(rgs2[0].equals("time")){
				this.dTime = Double.parseDouble(rgs2[1]);
			}			
		}
	}
	
	public String toString(){
			return "lat:" + dLat + ",lon:" + dLon + ",vert:" + dVert + ",time:" + dTime;
	}
}

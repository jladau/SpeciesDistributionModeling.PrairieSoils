package edu.ucsf.base;

/**
 * This class give a geographic bounding box
 * @author jladau
 */

public class GeographicPointBounds {
	
	//dLatitudeMin = minimum latitude
	//dLatitudeMax = maximum latitude
	//dLongitudeMin = minimum longitude
	//dLongitudeMax = maximum longitude
	//dHeight = height (difference between latitude max and min)
	//dWidth = width (difference between longitude max and min)
	//bValid = true if valid bounding box, false otherwise
	//rgdArray = bounds in array format; entries in order are lon-min, lon-max, lat-min, lat-max
	
	public double dLatitudeMin;
	public double dLatitudeMax;
	public double dLongitudeMin;
	public double dLongitudeMax;
	public double dHeight;
	public double dWidth;
	public boolean bValid;
	public double[] rgdArray;
	
	public GeographicPointBounds(){	
	}
	
	public boolean equals(Object bds1){
		
		//bds2 = bds1 coerced to GeographicPointBounds
		
		GeographicPointBounds bds2;
		
		if(!(bds1 instanceof GeographicPointBounds)){
			return false;
		}
		
		bds2 = (GeographicPointBounds) bds1;
		if(bds2.dLatitudeMax!=this.dLatitudeMax){
			return false;
		}
		if(bds2.dLatitudeMin!=this.dLatitudeMin){
			return false;
		}
		if(bds2.dLongitudeMax!=this.dLongitudeMax){
			return false;
		}
		if(bds2.dLongitudeMin!=this.dLongitudeMin){
			return false;
		}
		return true;
	}
	
	public GeographicPointBounds(double dLatitudeMin, double dLatitudeMax, double dLongitudeMin, double dLongitudeMax){	
		this.dLatitudeMin = dLatitudeMin;
		this.dLatitudeMax = dLatitudeMax;
		this.dLongitudeMin = dLongitudeMin;
		this.dLongitudeMax = dLongitudeMax;
		this.dHeight = dLatitudeMax - dLatitudeMin;
		this.dWidth = dLongitudeMax - dLongitudeMin;
		rgdArray = new double[4];
		rgdArray[0] = dLongitudeMin;
		rgdArray[1] = dLongitudeMax;
		rgdArray[2] = dLatitudeMin;
		rgdArray[3] = dLatitudeMax;
		checkValidity();
	}
	
	/**
	 * Checks if point is in bounds
	 * @param dLat Latitude of point
	 * @param dLon Longitude of point
	 * @return True if point is within bounds, false otherwise
	 */
	public boolean isPointInBounds(double dLat, double dLon){
		if(dLatitudeMin<=dLat){
			if(dLat<=dLatitudeMax){
				if(dLongitudeMin<=dLon){
					if(dLon<=dLongitudeMax){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Counts vertices of edge that are in bounds
	 * @param edg1 Edge
	 * @return Returns number of vertices that are in bounds: 0,1,2
	 */
	public int countVerticeInBounds(Edge edg1){
		
		if(this.isPointInBounds(edg1.dLatStart, edg1.dLonStart)){
			if(this.isPointInBounds(edg1.dLatEnd, edg1.dLonEnd)){
				return 2;
			}else{
				return 1;
			}
		}else{
			if(this.isPointInBounds(edg1.dLatEnd, edg1.dLonEnd)){
				return 1;
			}else{
				return 0;
			}
		}
	}
	
	private void checkValidity(){
		
		if(dLatitudeMin<-90){
			bValid=false;
			return;
		}
		if(dLatitudeMax>90){
			bValid=false;
			return;
		}
		if(dLongitudeMin<-180){
			bValid=false;
			return;
		}
		if(dLongitudeMax>180){
			bValid=false;
			return;
		}
		if(dHeight<0 || dHeight>180){
			bValid=false;
			return;
		}
		if(dWidth<0 || dWidth>360){
			bValid=false;
			return;
		}
		bValid=true;
	}
}

package edu.ucsf.base;

/**
 * Class gives edges of polygon
 * @author jladau
 */

public class Edge {

	//dLatStart = starting latitude
	//dLatEnd = ending latitude
	//dLonStart = starting longitude
	//dLonEnd = ending longitude
	//dLength = length of edge in km
	//bCross180 = true if edge crosses 180E/W, false otherwise
	//iWinding = winding number of edge
	
	public double dLength;
	public double dLatStart;
	public double dLatEnd;
	public double dLonStart;
	public double dLonEnd;
	public boolean bCross180;
	public int iWinding;
	
	/**
	 * Constructor.
	 * @param dLatStart Starting latitude
	 * @param dLatEnd Ending latitude
	 * @param dLonStart Starting longitude
	 * @param dLonEnd Ending longitude
	 */
	public Edge(double dLatStart, double dLatEnd, double dLonStart, double dLonEnd){	
		
		//sph1 = spherical geometry object
		
		SphericalGeometry sph1;
		
		//loading start and end points of edge
		this.dLatStart = dLatStart;
		this.dLatEnd = dLatEnd;
		this.dLonStart = dLonStart;
		this.dLonEnd = dLonEnd;
		
		//loading length of edge
		sph1 = new SphericalGeometry();
		this.dLength = sph1.findDistance(dLatStart, dLonStart, dLatEnd, dLonEnd);
		
		//checking if edge crosses 180E
		if(Math.abs(dLonStart-dLonEnd)>180.){
			bCross180=true;
		}
		
		//loading winding number
		if(dLonStart<dLonEnd){
			iWinding = 1;
		}else{
			iWinding = -1;
		}
		if(bCross180){
			iWinding=-1*iWinding;
		}
	}
	
	/**
	 * Redefines edge equality so that edges are equal iff they have the same endpoints
	 */
	public boolean equals(Object edg1){
		
		//edg2 = edg1 cast to edge
		
		Edge edg2;
		
		//checking if edg1 is an Edge, returning false if not
		if(!(edg1 instanceof Edge)){
			return false;
		}
		
		//casting edg1 to edge
		edg2 = (Edge) edg1;
		
		//checking for same starting and ending locations
		if(edg2.dLatEnd==this.dLatEnd && edg2.dLatStart==this.dLatStart && edg2.dLonEnd==this.dLonEnd && edg2.dLonStart==this.dLonStart){
			return true;
		}
		if(edg2.dLatEnd==this.dLatStart && edg2.dLatStart==this.dLatEnd && edg2.dLonEnd==this.dLonStart && edg2.dLonStart==this.dLonEnd){
			return true;
		}
		return false;
	}
	
	public double getLatMinimum(){
		if(dLatStart<dLatEnd){
			return dLatStart;
		}else{
			return dLatEnd;
		}
	}
	
	public double getLatMaximum(){
		if(dLatStart>dLatEnd){
			return dLatStart;
		}else{
			return dLatEnd;
		}
	}
	
	public double getLonMinimum(){
		if(dLonStart<dLonEnd){
			return dLonStart;
		}else{
			return dLonEnd;
		}
	}
	
	public double getLonMaximum(){
		if(dLonStart>dLonEnd){
			return dLonStart;
		}else{
			return dLonEnd;
		}
	}
}

package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class creates bounding regions for a polygon
 * @author jladau
 */

public class GeographicPolygonBounds {

	//bdsOverall = overall bounds for polygon
	//mapEdges(rgbBounds) = returns the set of edges within the given bounds
	//ply1 = polygon of interest
	//sph1 = SphericalGeometry object
	
	public GeographicPointBounds bdsOverall;
	public Polygon ply1;
	public HashMap<GeographicPointBounds,ArrayList<Edge>> mapEdges;
	public SphericalGeometry sph1;
	
	public GeographicPolygonBounds(Polygon ply1){
	
		//dStepLat = latitude step size for loading geographic bounds
		//bds1 = current bounds object being added to mapEdges
		//edg1 = current edge
		//bAddressFound = true if address found for current edge, false otherwise
		//lst1 = list of extra edges
		//lst2 = list of bounds that contain no edges
		
		double dStepLat;
		GeographicPointBounds bds1;
		Edge edg1;
		boolean bAddressFound;
		ArrayList<Edge> lst1;
		ArrayList<GeographicPointBounds> lst2;
		
		//loading overall bounds
		bdsOverall = new GeographicPointBounds();
		bdsOverall.dLatitudeMin = ply1.dLatMinimum;
		if(bdsOverall.dLatitudeMin<-90){
			bdsOverall.dLatitudeMin = -90;
		}
		bdsOverall.dLatitudeMax = ply1.dLatMaximum;
		if(bdsOverall.dLatitudeMax>90){
			bdsOverall.dLatitudeMax = 90;
		}
		bdsOverall.dLongitudeMin = ply1.dLonMinimum;
		if(bdsOverall.dLongitudeMin<-180){
			bdsOverall.dLongitudeMin = -180;
		}
		bdsOverall.dLongitudeMax = ply1.dLonMaximum;
		if(bdsOverall.dLongitudeMax>180){
			bdsOverall.dLongitudeMax = 180;
		}
		
		//initializing map of edges
		mapEdges = new HashMap<GeographicPointBounds,ArrayList<Edge>>();
		dStepLat = 1.;
		for(double dLat=-90;dLat<90;dLat+=dStepLat){
			dLat = ElementaryMathOperations.round(dLat, 0);
			bds1 = new GeographicPointBounds(dLat,ElementaryMathOperations.round(Math.min(dLat+dStepLat,90.),0),-9999,9999);
			mapEdges.put(bds1, new ArrayList<Edge>());
		}
		for(double dLat=-90+dStepLat/2.;dLat<90;dLat+=dStepLat){
			dLat = ElementaryMathOperations.round(dLat, 1);
			bds1 = new GeographicPointBounds(dLat,ElementaryMathOperations.round(Math.min(dLat+dStepLat,90.),1),-9999,9999);
			mapEdges.put(bds1, new ArrayList<Edge>());
		}
		
		//initializing list of unclassifiable edges
		lst1 = new ArrayList<Edge>();
		
		//loading bounds
		for(int i=0;i<ply1.getEdgeCount();i++){
			
			//loading current edge
			edg1 = ply1.getEdge(i);
			
			//initializing flag
			bAddressFound = false;
			
			//finding address of edge
			for(GeographicPointBounds bds:mapEdges.keySet()){
				if(bds.countVerticeInBounds(edg1)==2){
					mapEdges.get(bds).add(edg1);
					bAddressFound=true;
					break;
				}
			}
			
			//saving edge if no address found
			if(bAddressFound==false){
				lst1.add(edg1);
			}
		}
		
		//saving extra edges
		//************************
		//System.out.println(lst1.size() + " of " + ply1.getEdgeCount() + " edges unclassified.");
		//************************
		mapEdges.put(new GeographicPointBounds(-90,90,-180,180), lst1);
	
		//removing bounds that contain no edges
		lst2 = new ArrayList<GeographicPointBounds>();
		for(GeographicPointBounds bds:mapEdges.keySet()){
			if(mapEdges.get(bds).size()==0){
				lst2.add(bds);
			}
		}
		for(int i=0;i<lst2.size();i++){
			mapEdges.remove(lst2.get(i));
		}
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
	}
	
	public void GeographicPolygonBounds0(Polygon ply1){
	
		//dStepLat = latitude step size for loading geographic bounds
		//dStepLon = longitude step size for loading geographic bounds
		//bds1 = current bounds object being added to mapEdges
		//edg1 = current edge
		//bAddressFound = true if address found for current edge, false otherwise
		//lst1 = list of extra edges
		//lst2 = list of bounds that contain no edges
		
		double dStepLat; double dStepLon;
		GeographicPointBounds bds1;
		Edge edg1;
		boolean bAddressFound;
		ArrayList<Edge> lst1;
		ArrayList<GeographicPointBounds> lst2;
		
		//loading overall bounds
		bdsOverall = new GeographicPointBounds();
		bdsOverall.dLatitudeMin = ply1.dLatMinimum;
		if(bdsOverall.dLatitudeMin<-90){
			bdsOverall.dLatitudeMin = -90;
		}
		bdsOverall.dLatitudeMax = ply1.dLatMaximum;
		if(bdsOverall.dLatitudeMax>90){
			bdsOverall.dLatitudeMax = 90;
		}
		bdsOverall.dLongitudeMin = ply1.dLonMinimum;
		if(bdsOverall.dLongitudeMin<-180){
			bdsOverall.dLongitudeMin = -180;
		}
		bdsOverall.dLongitudeMax = ply1.dLonMaximum;
		if(bdsOverall.dLongitudeMax>180){
			bdsOverall.dLongitudeMax = 180;
		}
		
		//initializing map of edges
		mapEdges = new HashMap<GeographicPointBounds,ArrayList<Edge>>();
		dStepLat = 30.;
		dStepLon = 30.;
		for(double dLat=-90;dLat<90;dLat+=dStepLat){
			dLat = ElementaryMathOperations.round(dLat, 0);
			for(double dLon=-180;dLon<180;dLon+=dStepLon){
				dLon = ElementaryMathOperations.round(dLon,0);
				bds1 = new GeographicPointBounds(dLat,ElementaryMathOperations.round(Math.min(dLat+dStepLat,90.),0),dLon,ElementaryMathOperations.round(Math.min(dLon+dStepLon,180.),0));
				mapEdges.put(bds1, new ArrayList<Edge>());
			}
		}
		
		//initializing list of unclassifiable edges
		lst1 = new ArrayList<Edge>();
		
		//loading bounds
		for(int i=0;i<ply1.getEdgeCount();i++){
			
			//loading current edge
			edg1 = ply1.getEdge(i);
			
			//initializing flag
			bAddressFound = false;
			
			//finding address of edge
			for(GeographicPointBounds bds:mapEdges.keySet()){
				if(bds.countVerticeInBounds(edg1)==2){
					mapEdges.get(bds).add(edg1);
					bAddressFound=true;
					break;
				}
			}
			
			//saving edge if no address found
			if(bAddressFound==false){
				lst1.add(edg1);
			}
		}
		
		//saving extra edges
		mapEdges.put(new GeographicPointBounds(-90,90,-180,180), lst1);
	
		//removing bounds that contain no edges
		lst2 = new ArrayList<GeographicPointBounds>();
		for(GeographicPointBounds bds:mapEdges.keySet()){
			if(mapEdges.get(bds).size()==0){
				lst2.add(bds);
			}
		}
		for(int i=0;i<lst2.size();i++){
			mapEdges.remove(lst2.get(i));
		}
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
	}

	/**
	 * Finds the edges potentially within a specified radius of the given point
	 * @param dLat Latitude of point
	 * @param dLon Longitude of point
	 * @param dRadius Radius
	 * @return An array of arrays of edges
	 */
	
	public ArrayList<ArrayList<Edge>> findEdgesWithinRadius(double dLat, double dLon, double dRadius){
		
		//lst1 = output
		
		ArrayList<ArrayList<Edge>> lst1;
		
		//initializing output
		lst1 = new ArrayList<ArrayList<Edge>>();
		for(GeographicPointBounds bds:mapEdges.keySet()){
			if(sph1.checkPointWithinDistanceOfBounds(bds, dLat, dLon, dRadius)){
				lst1.add(mapEdges.get(bds));
			}
		}
		
		//returning result
		return lst1;	
	}
}

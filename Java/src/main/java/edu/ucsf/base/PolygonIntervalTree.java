package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Object for looking up edges that overlap a given point.
 * @author jladau
 */

public class PolygonIntervalTree {

	//mapIntervalLat(dLatitude) = interval tree of latitudes, returns edges present at given interval endpoint excluding those that end at the endpoint.
	//mapIntervalLon(dLongitude) = interval tree of longitudes, returns edges present at given interval endpoint excluding those that end at the endpoint.
	//dMaxLat = maximum latitude
	//dMaxLon = maximum longitude
	
	private double dMaxLat = -9999;
	private double dMaxLon = -9999;
	private TreeMap<Double,ArrayList<Edge>> mapIntervalLat;
	private TreeMap<Double,ArrayList<Edge>> mapIntervalLon;
	
	/**
	 * constructor
	 * @param lstEdges List of edges to consider
	 */
	public PolygonIntervalTree(ArrayList<Edge> lstEdges){
		
		//loading longitude trees
		loadLongitudeTree(lstEdges);
		
		//loading latitude trees
		loadLatitudeTree(lstEdges);
	}
	
	/**
	 * Finds edges that overlap specified longitude
	 * @param dLon Longitude of interest
	 * @return List of edges that overlap specified longitude
	 */
	public ArrayList<Edge> findEdgesLongitude(Double dLon){
		
		//d1 = floor key
		
		Double d1;
		
		//checking if any edges are within bounds
		if(dLon<mapIntervalLon.firstKey() || dLon>dMaxLon){
			return null;
		}else{
			d1 = mapIntervalLon.floorKey(dLon);
			return mapIntervalLon.get(d1);
		}
	}
	
	/**
	 * Finds edges that overlap specified latitude
	 * @param dLat Latitude of interest
	 * @return List of edges that overlap specified latitude
	 */
	public ArrayList<Edge> findEdgesLatitude(Double dLat){
		
		//d1 = floor key
		
		Double d1;
		
		//checking if any edges are within bounds
		if(dLat<mapIntervalLat.firstKey() || dLat>dMaxLat){
			return null;
		}else{
			d1 = mapIntervalLat.floorKey(dLat);
			return mapIntervalLat.get(d1);
		}
	}
	
	/**
	 * Finds edges that overlap a given latitude and longitude
	 * @param dLat Latitude of interest
	 * @param dLon Longitude of interest
	 * @return Edges that overlap given latitude and longitude.
	 */
	public ArrayList<Edge> findEdges(Double dLat, Double dLon){
		
		//lst1 = list of edges overlapping longitude
		//lst2 = list of edges overlapping latitude
		//lst3 = output
		//edg1 = current edge under consideration
		
		ArrayList<Edge> lst1; ArrayList<Edge> lst2; ArrayList<Edge> lst3;
		Edge edg1;
		
		//loading latitude and longitude lists
		lst1 = findEdgesLongitude(dLon);
		if(lst1==null){
			return null;
		}
		lst2 = findEdgesLatitude(dLat);
		if(lst2==null){
			return null;
		}
		
		//finding intersection
		lst3 = new ArrayList<Edge>();
		if(lst1.size()<lst2.size()){
			for(int i=0;i<lst1.size();i++){
				edg1 = lst1.get(i);
				if(lst2.contains(edg1)){
					lst3.add(edg1);
				}
			}
		}else{
			for(int i=0;i<lst2.size();i++){
				edg1 = lst2.get(i);
				if(lst1.contains(edg1)){
					lst3.add(edg1);
				}
			}
		}
		
		//returning result
		return lst3;
	}
	
	/**
	 * Loads longitude tree
	 * @param lstEdges List of edges in polygon.  Edges that cross 180 are assumed to have enpoints inserted at that maerdian.
	 */
	private void loadLatitudeTree(ArrayList<Edge> lstEdges){
		
		//edg1 = current edge
		//lst1 = current list of edges
		//dLatStart = current starting latitude
		//dLatEnd = current ending latitude
		//set1 = set of edge start and end locations
		//set2 = subset of set1 between specified endpoints
		
		ArrayList<Edge> lst1;
		Edge edg1;
		Double dLatStart; Double dLatEnd;
		TreeSet<Double> set1; 
		Set<Double> set2;
		
		//initializing treemaps
		mapIntervalLat = new TreeMap<Double,ArrayList<Edge>>();
		
		//initializing treeset
		set1 = new TreeSet<Double>();
		
		//looping through edges to load end treemap
		for(int i=0;i<lstEdges.size();i++){
			
			//loading current edge
			edg1 = lstEdges.get(i);
			dLatStart = (Double) edg1.dLatStart;
			dLatEnd = (Double) edg1.dLatEnd;
			
			//saving start and end points
			set1.add(dLatStart);
			set1.add(dLatEnd);
			
			//updating maximum latitude
			if(dMaxLat<dLatStart){
				dMaxLat=dLatStart;
			}
			if(dMaxLat<dLatEnd){
				dMaxLat=dLatEnd;
			}
		}
		
		//loading interval map
		for(int i=0;i<lstEdges.size();i++){
			
			//******************************
			//System.out.println("Loading edge " + i + " of " + lstEdges.size() + "...");
			//******************************
			
			//loading current edge
			edg1 = lstEdges.get(i);
			dLatStart = (Double) edg1.dLatStart;
			dLatEnd = (Double) edg1.dLatEnd;
			
			//getting subset of endpoints between current endpoints
			if(dLatStart<dLatEnd){	
				set2 = set1.subSet(dLatStart, true, dLatEnd, false);
			}else{
				set2 = set1.subSet(dLatEnd, true, dLatStart, false);
			}
		
			//looping through elements of subset and updating interval map
			for(Double d : set2){
			
				//loading list of edges at given point
				if(mapIntervalLat.containsKey(d)){
					
					//***********************
					//loading current list of edges
					//lst1 = mapIntervalLat.get(d);
					//lst1.add(edg1);
					
					mapIntervalLat.get(d).add(edg1);
					//*********************
				}else{
				
					//initializing list edges
					lst1 = new ArrayList<Edge>(5);
					lst1.add(edg1);
				
					//********************
					//saving list of edges
					mapIntervalLat.put(d, lst1);
				}
				
				//saving list of edges
				//mapIntervalLat.put(d, lst1);
				//*******************
			}

			//**********************
			//System.out.println(i + "," + Runtime.getRuntime().totalMemory());
			//**********************
			
		}
	}

	/**
	 * Loads longitude tree
	 * @param lstEdges List of edges in polygon.  Edges that cross 180 are assumed to have endpoints inserted at that merdian.
	 */
	private void loadLongitudeTree(ArrayList<Edge> lstEdges){
		
		//edg1 = current edge
		//lst1 = current list of edges
		//dLonStart = current starting longitude
		//dLonEnd = current ending longitude
		//set1 = set of edge start and end locations
		//set2 = subset of set1 between specified endpoints
		
		ArrayList<Edge> lst1;
		Edge edg1;
		Double dLonStart; Double dLonEnd;
		TreeSet<Double> set1; 
		Set<Double> set2;
		
		//initializing treemaps
		mapIntervalLon = new TreeMap<Double,ArrayList<Edge>>();
		
		//initializing treeset
		set1 = new TreeSet<Double>();
		
		//looping through edges to load start and end treemaps
		for(int i=0;i<lstEdges.size();i++){
			
			//loading current edge
			edg1 = lstEdges.get(i);
			dLonStart = (Double) edg1.dLonStart;
			dLonEnd = (Double) edg1.dLonEnd;
			
			//saving start and end points
			set1.add(dLonStart);
			set1.add(dLonEnd);
			
			//saving start and end points to interval map
			mapIntervalLon.put(dLonStart,null);
			mapIntervalLon.put(dLonEnd,null);
			
			//updating maximum latitude
			if(dMaxLon<dLonStart){
				dMaxLon=dLonStart;
			}
			if(dMaxLon<dLonEnd){
				dMaxLon=dLonEnd;
			}
		}
		
		//loading interval map
		for(int i=0;i<lstEdges.size();i++){
			
			//loading current edge
			edg1 = lstEdges.get(i);
			dLonStart = (Double) edg1.dLonStart;
			dLonEnd = (Double) edg1.dLonEnd;
			
			//getting subset of endpoints between current endpoints
			if(dLonStart<dLonEnd){	
				set2 = set1.subSet(dLonStart, true, dLonEnd, false);
			}else{
				set2 = set1.subSet(dLonEnd, true, dLonStart, false);
			}
		
			//looping through elements of subset and updating interval map
			for(Double d : set2){
			
				//*******************
				if(d==-121.46399999353417){
					System.out.println("XXXX " + edg1.dLonStart + "," + edg1.dLatStart + "," + edg1.dLonEnd + "," + edg1.dLatEnd);
				}
				//*******************
				
				//loading list of edges at given point
				if(mapIntervalLon.get(d)!=null){
				//if(mapIntervalLon.containsKey(d)){
					
					//***********************
					//loading current list of edges
					//lst1 = mapIntervalLon.get(d);
					//lst1.add(edg1);
					
					mapIntervalLon.get(d).add(edg1);
					//************************
				}else{
				
					//initializing list edges
					lst1 = new ArrayList<Edge>(5);
					lst1.add(edg1);
				
					//*******************
					//saving list of edges
					mapIntervalLon.put(d, lst1);
				}
				
				//saving list of edges
				//mapIntervalLon.put(d, lst1);
				//********************
			}
		}
	}
}

package edu.ucsf.base;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.util.HashMap;
//import java.util.Map;

/**
 * Simplifies polygon
 * @author jladau
 */
public class SimplifyPolygon {

	/**
	 * Constructor
	 */
	public SimplifyPolygon(){
	}
	
	
	/**
	 * Removes duplicate edges from polygon
	 * @param ply1 Polygon.
	 * @param bCheckLatLon True if check that latitude is between -90 and 90, longitude is between -180 and 180; false otherwise
	 * @return Polygon with duplicate edges removed; winding numbers are preserved.
	 */
	public static Polygon removeDuplicateEdges(Polygon ply1, boolean bCheckLatLon){
		
		//mapCount(Line2D) = count of number of edges for given line (negative if more revered direction edges than given direction
		//mapReverseLine(Line2D) = returns reverse of given edge
		//mapLine(sLine) = returns the given line in Line2D.Double format
		//edg1 = current edge
		//sLine1 = current line
		//sLine2 = reverse of current line
		//lin1 = current line
		//lin2 = reverse of current line
		//i1 = current count
		//plyOut = output polygon
		
		String sLine1; String sLine2;
		Edge edg1;
		Line2D.Double lin1; Line2D.Double lin2;
		Map<String,Integer> mapCount; Map<String,String> mapReverseLine; Map<String,Line2D.Double> mapLine;
		int i1;
		Polygon plyOut;
		
		//initializing maps
		mapCount = new HashMap<String,Integer>((int) ply1.getEdgeCount());
		mapReverseLine = new HashMap<String,String>();
		mapLine = new HashMap<String,Line2D.Double>((int) ply1.getEdgeCount());
		
		//looping through edges
		for(int i=0;i<ply1.getEdgeCount();i++){
			
			//loading current edge
			edg1 = ply1.getEdge(i);
			sLine1 = round(edg1.dLonStart,10000000) + "," + round(edg1.dLatStart,10000000) + ";" + round(edg1.dLonEnd,10000000) + "," + round(edg1.dLatEnd,10000000);
			lin1 = new Line2D.Double(round(edg1.dLonStart,10000000),round(edg1.dLatStart,10000000),round(edg1.dLonEnd,10000000),round(edg1.dLatEnd,10000000));
			
			//checking if edge has already been entered
			if(mapCount.containsKey(sLine1)){
				i1 = mapCount.get(sLine1);
				i1++;
			}else{
				
				//checking if reverse edge has already been entered
				if(mapReverseLine.containsKey(sLine1)){
					sLine2 = mapReverseLine.get(sLine1);
					i1 = mapCount.get(sLine2);
					i1--;
				
				//edge not seen before
				}else{
					
					lin2 = new Line2D.Double(lin1.x2,lin1.y2,lin1.x1,lin1.y1);
					sLine2 = lin1.x2 + "," + lin1.y2 + ";" + lin1.x1 + "," + lin1.y1;
					mapLine.put(sLine1, lin1);
					mapLine.put(sLine2, lin2);
					mapReverseLine.put(sLine1,sLine2);
					mapReverseLine.put(sLine2,sLine1);
					i1=1;
				}
			}
			
			//updating count
			mapCount.put(sLine1, i1);
		}
		
		//outputting results
		plyOut = new Polygon();
		for(String s:mapCount.keySet()){
			
			//loading count
			i1 = mapCount.get(s);
			
			//*****************
			if(i1>1){
				System.out.println(i1);
			}
			//*****************
			
			//checking if positive or negative
			if(i1>0){
				lin1 = mapLine.get(s);
				plyOut.addEdge(new Edge(lin1.y1,lin1.y2,lin1.x1,lin1.x2),bCheckLatLon);
			}else{
				sLine2 = mapReverseLine.get(s);
				lin2 = mapLine.get(sLine2);
				plyOut.addEdge(new Edge(lin2.y1,lin2.y2,lin2.x1,lin2.x2),bCheckLatLon);
			}
		}
		
		//outputting result
		return plyOut;
	}
	
	
	/**
	 * Smoothes polygon.
	 * @param ply1 Polygon to be smoothed.
	 * @param dEpsilon Maximum allowable perpendicular distance.
	 * @return Smoothed polygon
	 */
	public static Polygon simplifyPolygon(Polygon ply1, double dEpsilon, boolean bCheckLatLon){
		
		//lst1 = vertices of polygon
		//lst2 = current set of vertices
		//lst3 = list of all vertices
		//ply2 = smoothed polygon
		//edg2 = start edge of current polygon
		//edg3 = terminating edge of current polygon
		//rgd1 = current vertex
		
		Polygon ply2;
		ArrayList<double[]> lst1; ArrayList<double[]> lst2 = null; ArrayList<double[]> lst3;
		double rgd1[];
		
		//loading vertices
		lst1 = ply1.convertToVertexList();
		
		//case 1: 100 edges added - smooth, add new edge
		//case 2: new polygon start - smooth, add new edge
		//case 3: last edge - add new edge, smooth
		
		//initializing list of edges 
		lst3 = new ArrayList<double[]>();
		
		//initializing current polygon
		lst2 = new ArrayList<double[]>(1000);
		
		//looping through vertices
		for(int i=0;i<lst1.size();i++){
			
			//loading current vertex
			rgd1 = lst1.get(i);
			
			//saving current vertex
			lst2.add(rgd1);
			
			//checking if end of current polygon
			if((i<lst1.size()-1 && rgd1[0]!=lst1.get(i+1)[0]) || i==lst1.size()-1){
			
				//simplifying polygon
				lst2 = simplifyPolygon(lst2,dEpsilon);
				
				//saving vertices
				for(int k=0;k<lst2.size();k++){
					lst3.add(lst2.get(k));
				}
				
				//clearing current polygon
				lst2 = new ArrayList<double[]>(1000);
			}
		}
		

		//initializing smoothed polygon
		ply2 = new Polygon();
		
		//saving polygon
		for(int i=1;i<lst3.size();i++){
			
			//checking if same polygon
			if(lst3.get(i-1)[0]==lst3.get(i)[0]){
			
				//saving edge
				ply2.addEdge(new Edge(lst3.get(i-1)[1],lst3.get(i)[1],lst3.get(i-1)[2],lst3.get(i)[2]),bCheckLatLon);		
			}
		}
		
		//returning polygon
		return ply2;
	}

	/**
	 * Reduces number of vertices by removing vertices that are within specified distance
	 * @param lst1 List of vertices.
	 * @param dEpsilon Threshold distance.
	 * @return Simplified polygon
	 */
	private static ArrayList<double[]> reduceVertices(ArrayList<double[]> lst1, double dEpsilon){
		
		//lst2 = output
		//iStart = current start vertex index
		//iTest = current test vertex index
		//sph1 = spherical geometry object
		
		SphericalGeometry sph1;
		ArrayList<double[]> lst2;
		int iStart; int iTest;
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//initializing output
		lst2 = new ArrayList<double[]>(lst1.size());
		lst2.add(lst1.get(0));
		
		//initializing vertices
		iStart = 0; iTest = 1;	
		do{
		
			//checking if threshold distance exceeded
			if(sph1.findDistance(lst1.get(iStart)[1], lst1.get(iStart)[2], lst1.get(iTest)[1], lst1.get(iTest)[2])>dEpsilon){
				
				//saving new vertex and updating
				lst2.add(lst1.get(iTest));
				iStart = iTest;
			}	
			
			//updating test
			iTest++;
		}while(iTest<lst1.size()-1);
		
		//adding last vertex
		lst2.add(lst1.get(iTest));
		
		//outputting result
		return lst2;
	}
	
	
	/**
	 * Simplfies polygon (must have same ending and starting point)
	 * @param lst1 Polygon vertices.
	 * @param dEpsilon Threshold for D-P algorithm.
	 * @return Simplified polygon.
	 */
	private static ArrayList<double[]> simplifyPolygon(ArrayList<double[]> lst1, double dEpsilon){
		
		//lst4 = current simplified polygon
		//lst3 = current third
		//lst2 = output
		//rgl1 = contains first, second, and third thirds simplified.
		//iStart = current start vertex index
		//iEnd = current end vertex index
		
		ArrayList<double[]> lst2; ArrayList<double[]> lst3; ArrayList<double[]> lst4;
		int iStart; int iEnd;
		
		//checking if a triangle: no simplification possible.
		if(lst1.size()<7){
			return lst1;
		}
		
		//initializing output
		lst2 = new ArrayList<double[]>(lst1.size());
		
		//initializing first third
		iStart = 0; iEnd = lst1.size()/3;
		lst3 = new ArrayList<double[]>(iEnd-iStart+1);
		for(int i=iStart;i<=iEnd;i++){
			lst3.add(lst1.get(i));
		}
		
		//*************************
		//lst3 = reduceVertices(lst3,dEpsilon);
		//*************************
		
		//simplifying
		lst4 = runDouglasPeuker(lst3,dEpsilon);
		
		//saving results to lst2
		for(int i=0;i<lst4.size();i++){
			lst2.add(lst4.get(i));
		}
		
		//initializing second third
		iStart = iEnd; iEnd = 2*lst1.size()/3;
		lst3 = new ArrayList<double[]>(iEnd-iStart+1);
		for(int i=iStart;i<=iEnd;i++){
			lst3.add(lst1.get(i));
		}
		
		//*************************
		//lst3 = reduceVertices(lst3,dEpsilon);
		//*************************
		
		
		//simplifying
		lst4 = runDouglasPeuker(lst3,dEpsilon);
		
		//saving results to lst2
		for(int i=0;i<lst4.size();i++){
			lst2.add(lst4.get(i));
		}
		
		//initializing last third
		iStart = iEnd; iEnd = lst1.size()-1;
		lst3 = new ArrayList<double[]>(iEnd-iStart+1);
		for(int i=iStart;i<=iEnd;i++){
			lst3.add(lst1.get(i));
		}
		
		//*************************
		//lst3 = reduceVertices(lst3,dEpsilon);
		//*************************
		
		//simplifying
		lst4 = runDouglasPeuker(lst3,dEpsilon);
		
		//saving results to lst2
		for(int i=0;i<lst4.size();i++){
			lst2.add(lst4.get(i));
		}
		
		//outputting
		return lst2;
	}
	
	/**
	 * Runs Douglas-Peucker recursion
	 * @param dEpsilon Maximum allowable perpendicular distance
	 * @parem lst1 List of vertices in order (lat,lon)
	 * @return Smoothed polygon.
	 */
	private static ArrayList<double[]> runDouglasPeuker(ArrayList<double[]> lst1, double dEpsilon){
		
		//rdg1 = current vertex
		//dMax = maximum observed cross track distance
		//i1 = index of point
		//d1 = current distance
		//lst2 = first set of recursion results
		//lst3 = second set of recursion results
		//lst4 = output
		//sph1 = spherical geometry object
		//edg2 = edge between start and end point
		
		SphericalGeometry sph1;
		int i1=0;
		double dMax=0; double d1=0;
		Edge edg2;
		ArrayList<double[]> lst2; ArrayList<double[]> lst3; ArrayList<double[]> lst4;
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//initializing edge from first to last vertex
		edg2 = new Edge(lst1.get(0)[1],lst1.get(lst1.size()-1)[1],lst1.get(0)[2],lst1.get(lst1.size()-1)[2]);
		
		//looping through edges
		for(int i=1;i<lst1.size()-1;i++){
		
			//loading distance
			d1 = sph1.findMinimumDistance(edg2, lst1.get(i)[1], lst1.get(i)[2]);

			//updating index
			if(d1>dMax){
				dMax = d1;
				i1 = i;
			}
		}
		
		//recursively simplifying
		if(dMax>=dEpsilon){
		
			//loading recursion lists
			lst2 = runDouglasPeuker(findSublist(lst1,0, i1),dEpsilon);
			lst3 = runDouglasPeuker(findSublist(lst1, i1 , lst1.size()-1),dEpsilon);
			
			//merging recursion lists
			lst4 = new ArrayList<double[]>(lst2.size()-1+lst3.size());
			for(int i=0;i<lst2.size()-1;i++){
				lst4.add(lst2.get(i));
			}
			for(int i=0;i<lst3.size();i++){
				lst4.add(lst3.get(i));
			}
		}else{
			
			//outputting result
			//lst4 = new ArrayList<Edge>(2);
			//lst4.add(lst1.get(0));
			//if(lst1.size()>2){	
			//	lst4.add(new Edge(lst1.get(0).getLatEnd(),lst1.get(lst1.size()-1).getLatStart(),lst1.get(0).getLonEnd(),lst1.get(lst1.size()-1).getLonStart()));
			//}
			//lst4.add(lst1.get(lst1.size()-1));
			lst4 = new ArrayList<double[]>(2);
			lst4.add(lst1.get(0));
			lst4.add(lst1.get(lst1.size()-1));
		}
		
		//returning result
		return lst4;
	}
	
	/**
	 * Creates sublist.
	 * @param lst1 List to be sublisted.
	 * @param iStart Start index.
	 * @param iEnd End index.
	 */
	private static ArrayList<double[]> findSublist(ArrayList<double[]> lst1, int iStart, int iEnd){
		
		//lst2 = output
		
		ArrayList<double[]> lst2;
		
		lst2 = new ArrayList<double[]>(iEnd-iStart+1);
		for(int i=iStart;i<=iEnd;i++){
			lst2.add(lst1.get(i));
		}
		return lst2;
	}
	
	private static double round(double dNumber, double dPlaces){
		return (double) Math.round(dNumber * dPlaces) / dPlaces;
	}

	
}

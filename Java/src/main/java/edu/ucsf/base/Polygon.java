package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

/**
 * Class implements collection of edges of polygon.
 * @author jladau
 */
public class Polygon {

	//lstEdges = list of edges
	//dLatMinimum = minimum latitude
	//dLatMaximum = maximum latitude
	//dLonMinimum = minimum longitude
	//dLatMaximum = maximum longitude
	//int1 = PolygonIntervalTree for edges
	//int2 = PolygonIntervalTree for vertices + radius
	//dRadius = radius used for building vertex interval tree
	//sph1 = spherical geometry object
	//rgdBounds = bounds
	//bds1 = Polygon bounds
	//mapDisjointRegion(iEdge) = returns the disjoint region to which given edge belongs
	
	public GeographicPolygonBounds bds1 = null;
	private double rgdBounds[]=null;
	private SphericalGeometry sph1=null;
	private double dRadius = -9999.;
	public double dLatMinimum = 9999.;
	public double dLatMaximum = -9999.;
	public double dLonMinimum = 9999.;
	public double dLonMaximum = -9999.;
	private ArrayList<Edge> lstEdges;
	private PolygonIntervalTree int1=null;
	private PolygonIntervalTree int2=null;
	private Map<Integer,Integer> mapDisjointRegion=null;
	
	private static final double LAT_DISTANCE = 111.2;
	private static final double RAD_TO_DEG = 57.295779513;
	private static final double DEG_TO_RAD = 0.017453293;
	private static final double EARTH_RADIUS = 6371;
	
	/**
	 * Constructor
	 */
	public Polygon(){
		lstEdges = new ArrayList<Edge>();
		sph1 = new SphericalGeometry();
	}
	
	/**
	 * Constructor
	 * @param lstPolygon Polygon in string format.  Polygon is assumed to be closed (start vertex occurs twice).
	 * @param bCheckLatLon True if check that latitude is between -90 and 90, longitude is between -180 and 180; false otherwise
	 */
	public Polygon(ArrayList<String[]> lstPolygon, int iRandomSeed, boolean bCheckLatLon){
		
		//edg1 = current edge being added
		//dOffset = small random offset so that test rays in interior point algorithm do not hit endpoints of edges
		//rnd1 = random number generator
		
		double dOffset;
		Random rnd1;
		Edge edg1;

		//initializing set of edges
		lstEdges = new ArrayList<Edge>(lstPolygon.size());
		
		//initializing random number generator
		rnd1 = new Random(iRandomSeed);
		
		//loading offset to prevent boundary cases
		dOffset = 0.00000001*rnd1.nextDouble();
		
		//looping through edges
		for(int i=1;i<lstPolygon.size();i++){

			//loading edge
			if(lstPolygon.get(i)[0].equals(lstPolygon.get(i-1)[0])){
				edg1 = new Edge(Double.parseDouble(lstPolygon.get(i-1)[2])+dOffset,Double.parseDouble(lstPolygon.get(i)[2])+dOffset,Double.parseDouble(lstPolygon.get(i-1)[1])+dOffset,Double.parseDouble(lstPolygon.get(i)[1])+dOffset);
				this.addEdge(edg1, bCheckLatLon);
			}
		}
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
	}
	
	
	/**
	 * Constructor
	 * @param rgs1 Polygon in string format
	 * @param bCheckLatLon True if check that latitude is between -90 and 90, longitude is between -180 and 180; false otherwise
	 */
	public Polygon(String rgsPolygon[][], int iRandomSeed, boolean bCheckLatLon){
		
		//edg1 = current edge being added
		//dOffset = small random offset so that test rays in interior point algorithm do not hit endpoints of edges
		//rnd1 = random number generator
		//i1 = coordinate of beginning of current polygon
		
		double dOffset;
		Random rnd1;
		Edge edg1;
		int i1;
		
		//initializing list of edges
		lstEdges = new ArrayList<Edge>();
		
		//initializing random number generator
		rnd1 = new Random(1234);
		
		//loading offset to prevent boundary cases
		dOffset = 0.0000000001*rnd1.nextDouble();
		
		//looping through edges
		i1=1;
		for(int i=2;i<rgsPolygon.length;i++){
			
			//loading edge: same polygon
			if(rgsPolygon[i][0].equals(rgsPolygon[i-1][0])){
				edg1 = new Edge(Double.parseDouble(rgsPolygon[i-1][2])+dOffset,Double.parseDouble(rgsPolygon[i][2])+dOffset,Double.parseDouble(rgsPolygon[i-1][1])+dOffset,Double.parseDouble(rgsPolygon[i][1])+dOffset);
				this.addEdge(edg1, bCheckLatLon);
			
			//loading edge: different polygon
			}else{
				edg1 = new Edge(Double.parseDouble(rgsPolygon[i-1][2])+dOffset,Double.parseDouble(rgsPolygon[i1][2])+dOffset,Double.parseDouble(rgsPolygon[i-1][1])+dOffset,Double.parseDouble(rgsPolygon[i1][1])+dOffset);
				this.addEdge(edg1, bCheckLatLon);	
				i1 = i;
			}
		}
		
		//loading spherical geometry object
		sph1 = new SphericalGeometry();
	}
	
	/**
	 * Initializes polygon bounds
	 */
	public void initializeBounds(){
		bds1 = new GeographicPolygonBounds(this);
	}
	
	/**
	 * Adds an edge and corrects it if its longitude exceeds [-180,180]
	 * @param edg1 Edge being added.
	 * @return true if edge added successfully; false if edge already exists in polygon (no edge added in this case)
	  */
	public boolean addAndCorrectEdge(Edge edg1){
		
		//edg2 = split edge (if edge crosses 180)
		//rge1 = split edge if edge crosses -180 or 180
		
		Edge edg2;
		Edge[] rge1;
		
		//checking if edge is broader that 360 degrees
		if(Math.abs(edg1.getLonMinimum()-edg1.getLonMaximum())>360){
			
		}else{
			
			//checking if edge crosses 180
			if(edg1.getLonMinimum()<180 && 180<edg1.getLonMaximum()){
			
				//loading split edge
				rge1 = this.splitEdge(edg1, 180);
				
				//checking if edge has already been added
				if(lstEdges.contains(rge1[0]) && lstEdges.contains(rge1[1])){
					return false;
				}
				
				//saving split edge
				for(int i=0;i<2;i++){
					if(!lstEdges.contains(rge1[i])){
						lstEdges.add(rge1[i]);
						updateBounds(rge1[i]);
					}
				}
				
			}else{
				
				//checking if edge crosses -180
				if(edg1.getLonMinimum()<-180 && -180<edg1.getLonMaximum()){
					
					//loading split edge
					rge1 = this.splitEdge(edg1, -180);
					
					//checking if edge has already been added
					if(lstEdges.contains(rge1[0]) && lstEdges.contains(rge1[1])){
						return false;
					}
					
					//saving split edge
					for(int i=0;i<2;i++){
						if(!lstEdges.contains(rge1[i])){
							lstEdges.add(rge1[i]);
							updateBounds(rge1[i]);
						}
					}
				}else{
					
					//checking if edge is at greater than 180 or less than -180
					if(edg1.dLonStart>180 || edg1.dLonEnd<-180){
					
						//loading edge
						edg2 = new Edge(edg1.dLatStart,edg1.dLatEnd,sph1.correctLongitude(edg1.dLonStart),sph1.correctLongitude(edg1.dLonEnd));
						if(lstEdges.contains(edg2)){
							return false;
						}
						lstEdges.add(edg2);
						updateBounds(edg2);
						
					//entering edge as is
					}else{
						if(lstEdges.contains(edg1)){
							return false;
						}
						lstEdges.add(edg1);
						updateBounds(edg1);
					}
				}
			}
		}	
		return true;
	}
	
	/**
	 * Adds an edge.
	 * @param edg1 Edge being added.
	 * @param bCheckLatLon True if check that latitude is between -90 and 90, longitude is between -180 and 180; false otherwise
	 */
	public void addEdge(Edge edg1, boolean bCheckLatLon){
		
		//sph1 = spherical geometry object
		//edg2 = split edge (if edge corsses 180)
		//dLat = latitude at which edge crosses 180
		
		SphericalGeometry sph1;
		Edge edg2;
		double dLat;
		
		//checking if zero length
		if(edg1.dLength==0){
			return;
		}
		
		//checking if edge is outside of bounds
		if(bCheckLatLon==true){
			if(edg1.getLatMaximum()>90 && edg1.getLatMinimum()>90){
				return;
			}
			if(edg1.getLatMaximum()<-90 && edg1.getLatMinimum()<-90){
				return;
			}
			if(edg1.getLonMaximum()>180 && edg1.getLonMinimum()>180){
				return;
			}
			if(edg1.getLonMaximum()<-180 && edg1.getLonMinimum()<-180){
				return;
			}
		}
		
		//checking if edge crosses 180
		if(Math.abs(edg1.getLonMinimum()-edg1.getLonMaximum())>180){
		
			//splitting edge and loading values
			sph1 = new SphericalGeometry();
			dLat = sph1.findLatitudeDegree(edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd, 180);
			if(edg1.dLonStart<0){
				edg2 = new Edge(edg1.dLatStart,dLat,edg1.dLonStart,-180);
				if(edg2.dLength>0){
					lstEdges.add(edg2);
				}
				edg2 = new Edge(dLat,edg1.dLatEnd,180,edg1.dLonEnd);
				if(edg2.dLength>0){
					lstEdges.add(edg2);
				}
			}else{
				edg2 = new Edge(edg1.dLatStart,dLat,edg1.dLonStart,180);
				if(edg2.dLength>0){
					lstEdges.add(edg2);
				}
				edg2 = new Edge(dLat,edg1.dLatEnd,-180,edg1.dLonEnd);
				if(edg2.dLength>0){
					lstEdges.add(edg2);
				}
			}

		}else{
			lstEdges.add(edg1);
		}
	
		//updating bounds
		updateBounds(edg1);	
	}
	
	/**
	 * Converts to vertex list
	 * @return Vertex list: first entry with polygon id, second entry with latitude, third entry with longitude
	 */
	public ArrayList<double[]> convertToVertexList(){
		
		//lst1 = output
		//rgd1 = current vertex
		//d1 = current polygon id
		
		double rgd1[];
		ArrayList<double[]> lst1;
		double d1;
		
		//initializing output
		lst1 = new ArrayList<double[]>(lstEdges.size()+10);
	
		//initializing vertex array
		d1 = 0;
		
		//looping through edges of polygon
		for(int i=0;i<lstEdges.size();i++){
			
			
			//first entry
			if(i==0){
				
				//saving first vertex
				rgd1 = new double[3];
				rgd1[0] = d1; rgd1[1] = lstEdges.get(i).dLatStart; rgd1[2] = lstEdges.get(i).dLonStart;
				lst1.add(rgd1);
			}
			
			//new polygon
			if(i>0 && (lstEdges.get(i).dLatStart!=lstEdges.get(i-1).dLatEnd || lstEdges.get(i).dLonStart!=lstEdges.get(i-1).dLonEnd)){
			
				//updating polygon id
				d1++;
				
				//saving first vertex
				rgd1 = new double[3];
				rgd1[0]=d1; rgd1[1] = lstEdges.get(i).dLatStart; rgd1[2] = lstEdges.get(i).dLonStart;
				lst1.add(rgd1);
			}
			
			//loading ending vertex
			rgd1 = new double[3];
			rgd1[0]=d1; rgd1[1] = lstEdges.get(i).dLatEnd; rgd1[2] = lstEdges.get(i).dLonEnd;
			
			//saving ending vertex
			lst1.add(rgd1);
		}
		
		///returning result
		return lst1;
	}

	/**
	 * Finds area of polygon by monte carlo integration.
	 * @param rgdBounds Bounds for polygon.
	 * @param rgdSamplingBounds Area of intersection of this region with polygon will be computed.  Null if total area of polygon is to be found.
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @return Area of polygon.
	 */
	public double findAreaMonteCarlo(double rgdSamplingBounds[], String sAlgorithm){
	
		//i1 = number of points within polygon
		//i2 = number of points sampled
		//rgdBounds = polygon bounds
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//du = first current random number
		//dv = second current random number
		//dArea = area of bounding box (latitudinal edges are not great circle edges)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
		//rgd2 = sampling bounds in radians
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//iIteration = number of sampling points
		
		int i1=0; int i2=0;
		double dArea;
		double du; double dv; double dLat; double dLng; double d2 = 0; double d3 = 0; double d4 = 0;
		int i;
		double rgd1[] = null; double rgd2[] = null;
		int iIterations;
		
		//loading number of iterations
		iIterations = 100000;
		
		//loading variables
		if(rgdSamplingBounds==null){
			
			//loading rgd1
			rgd1 = new double[4];
			rgd1[0] = DEG_TO_RAD*-90.;
			rgd1[1] = DEG_TO_RAD*90.;
			rgd1[2] = DEG_TO_RAD*dLonMinimum;
			rgd1[3] = DEG_TO_RAD*dLonMaximum;
			
			//loading d2
			d2 = rgd1[3]-rgd1[2];
			
			//loading d3 and d4
			d3 = Math.cos(rgd1[0]+Math.PI/2.);
			d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd1[3]-rgd1[2])*d4);
			
		}else{
		
			rgd2 = new double[4];
			rgd2[0] = DEG_TO_RAD*rgdSamplingBounds[2];
			rgd2[1] = DEG_TO_RAD*rgdSamplingBounds[3];
			rgd2[2] = DEG_TO_RAD*rgdSamplingBounds[0];
			rgd2[3] = DEG_TO_RAD*rgdSamplingBounds[1];
			
			d2 = rgd2[3]-rgd2[2];
			d3 = Math.cos(rgd2[0]+Math.PI/2.);
			d4 = Math.cos(rgd2[1]+Math.PI/2.)-d3;	
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd2[3]-rgd2[2])*d4);
			
		}
			
		//looping through sampling points
		for(i=1;i<=iIterations;i++){
			
			//loading random number
			du = Math.random(); dv = Math.random();
			
			//loading random point
			if(rgdSamplingBounds==null){
			
				dLng = du*d2+rgd1[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}else{
				
				//loading Lat and Lng
				dLng = du*d2+rgd2[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}
			
			//checking if sampling point is in range
			if(sph1.isPointPolygon(dLat, dLng, this, sAlgorithm)==1){
				i1++;
			}
			
			//updating i2
			i2++;
		}
		
		//saving result
		return ((double) i1)/((double) i2)*dArea;
	}
	
	/**
	 * Finds area of polygon by monte carlo integration.
	 * @param rgdSamplingBounds Area of intersection of this region with polygon will be computed.  Null if total area of polygon is to be found.
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @return Area of polygon.
	 */
	public double findAreaExact(String sAlgorithm){
	
		//d1 = current area component
		//dOut = output

		double dOut; double d1;
		
		//initializing output
		dOut = 0.;
		
		//looping through edges
		for(int i=0;i<this.lstEdges.size();i++){
			
			//loading unsigned area
			d1=sph1.findAreaSphericalTriangle(this.lstEdges.get(i));
			
			//finding sign of area
			//if(sph1.isPointPolygon(this.lstEdges.get(i).dLatStart-0.1, this.lstEdges.get(i).dLonStart, this, sAlgorithm)==0){
			//	d1=-1.*d1;
			//}
			if(sph1.isPointPolygon((this.lstEdges.get(i).dLatStart+this.lstEdges.get(i).dLatEnd)/2.+0.05, (this.lstEdges.get(i).dLonStart+this.lstEdges.get(i).dLonEnd)/2., this, sAlgorithm)==0){
				d1=-1.*d1;
			}
			
			//************************
			//System.out.println((this.lstEdges.get(i).dLatStart+this.lstEdges.get(i).dLatEnd)/2.+0.05 + "," + (this.lstEdges.get(i).dLonStart+this.lstEdges.get(i).dLonEnd)/2.);
			//if(d1<0 && this.lstEdges.get(i).dLatEnd<19.8749){	
			//	System.out.println(i + "," + this.lstEdges.get(i).dLonStart + "," + this.lstEdges.get(i).dLatStart + "," + this.lstEdges.get(i).dLonEnd + "," + this.lstEdges.get(i).dLatEnd);
			//}
			//************************
			
			
			//updating output
			dOut+=d1;
		}
		
		//returning result
		return dOut;
	}

	/**
	 * Finds area of intersection of current polygon and another polygon by monte carlo integration.
	 * @param rgdBounds Bounds for polygon.
	 * @param rgdSamplingBounds Area of intersection of this region with polygon will be computed.  Null if total area of polygon is to be found.
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @param setPolygons Set of polygons to consider
	 * @return Area of polygon.
	 */
	public double findAreaIntersection0(double rgdSamplingBounds[], String sAlgorithm, HashSet<Polygon> setPolygons){
	
		//i1 = number of points within polygon
		//i2 = number of points sampled
		//rgdBounds = polygon bounds
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//du = first current random number
		//dv = second current random number
		//dArea = area of bounding box (latitudinal edges are not great circle edges)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
		//rgd2 = sampling bounds in radians
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//iIteration = number of sampling points
		//bIncludeCount = flag for whether polygon should be counted
		
		boolean bIncludeCount;
		int i1=0; int i2=0;
		double dArea;
		double du; double dv; double dLat; double dLng; double d2 = 0; double d3 = 0; double d4 = 0;
		int i;
		double rgd1[] = null; double rgd2[] = null;
		int iIterations;
		
		//loading number of iterations
		iIterations = 1000;
		
		//loading variables
		if(rgdSamplingBounds==null){
			
			//loading rgd1
			rgd1 = new double[4];
			rgd1[0] = DEG_TO_RAD*dLatMinimum;
			rgd1[1] = DEG_TO_RAD*dLatMaximum;
			rgd1[2] = DEG_TO_RAD*dLonMinimum;
			rgd1[3] = DEG_TO_RAD*dLonMaximum;
			
			//loading d2
			d2 = rgd1[3]-rgd1[2];
			
			//loading d3 and d4
			d3 = Math.cos(rgd1[0]+Math.PI/2.);
			d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd1[3]-rgd1[2])*d4);
			
		}else{
		
			rgd2 = new double[4];
			rgd2[0] = DEG_TO_RAD*rgdSamplingBounds[2];
			rgd2[1] = DEG_TO_RAD*rgdSamplingBounds[3];
			rgd2[2] = DEG_TO_RAD*rgdSamplingBounds[0];
			rgd2[3] = DEG_TO_RAD*rgdSamplingBounds[1];
			
			d2 = rgd2[3]-rgd2[2];
			d3 = Math.cos(rgd2[0]+Math.PI/2.);
			d4 = Math.cos(rgd2[1]+Math.PI/2.)-d3;	
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd2[3]-rgd2[2])*d4);
			
		}
			
		//looping through sampling points
		for(i=1;i<=iIterations;i++){
			
			//loading random number
			du = Math.random(); dv = Math.random();
			
			//loading random point
			if(rgdSamplingBounds==null){
			
				dLng = du*d2+rgd1[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}else{
				
				//loading Lat and Lng
				dLng = du*d2+rgd2[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}
			
			//checking if sampling point is in range
			bIncludeCount=true;
			for(Polygon ply:setPolygons){
				if(sph1.isPointPolygon(dLat, dLng, ply, sAlgorithm)==0){
					bIncludeCount=false;
					break;	
				}
			}
			if(bIncludeCount==true){
				i1++;
			}
			
			//updating i2
			i2++;
		}
		
		//saving result
		return ((double) i1)/((double) i2)*dArea;
	}

	/**
	 * Finds area of intersection of current polygon and another polygon by monte carlo integration.
	 * @param rgdBounds Bounds for polygon.
	 * @param rgdSamplingBounds Area of intersection of this region with polygon will be computed.  Null if total area of polygon is to be found.
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @param iIterations Number of sampling points to use for monte carlo integration
	 * @param setPolygons Set of polygons to consider
	 * @return Map: keys are multiplicities of intersections (e.g., 3 is for intersections of three species), values are mean areas of intersection (e.g., mean area of three-way intersections across all 3-subsets of species)
	 */
	public Map<Double,Double> findAreaIntersections(double rgdSamplingBounds[], String sAlgorithm, int iIterations, HashSet<Polygon> setPolygons){
	
		//rgdBounds = polygon bounds
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//du = first current random number
		//dv = second current random number
		//dArea = area of bounding box (latitudinal edges are not great circle edges)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
		//rgd2 = sampling bounds in radians
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//rgdCounts = returns number of species observed at location
		//mapOut(dIntersectionCount) = returns mean area for number of intersections
		//dValue = current value in output map
		
		Map<Double,Double> mapOut;
		double dArea;
		double du; double dv; double dLat; double dLng; double d2 = 0; double d3 = 0; double d4 = 0; double dValue;
		double rgd1[] = null; double rgd2[] = null; double rgdCounts[];
		
		//loading variables
		if(rgdSamplingBounds==null){
			
			//loading rgd1
			rgd1 = new double[4];
			rgd1[0] = DEG_TO_RAD*dLatMinimum;
			rgd1[1] = DEG_TO_RAD*dLatMaximum;
			rgd1[2] = DEG_TO_RAD*dLonMinimum;
			rgd1[3] = DEG_TO_RAD*dLonMaximum;
			
			//loading d2
			d2 = rgd1[3]-rgd1[2];
			
			//loading d3 and d4
			d3 = Math.cos(rgd1[0]+Math.PI/2.);
			d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd1[3]-rgd1[2])*d4);
			
		}else{
		
			rgd2 = new double[4];
			rgd2[0] = DEG_TO_RAD*rgdSamplingBounds[2];
			rgd2[1] = DEG_TO_RAD*rgdSamplingBounds[3];
			rgd2[2] = DEG_TO_RAD*rgdSamplingBounds[0];
			rgd2[3] = DEG_TO_RAD*rgdSamplingBounds[1];
			
			d2 = rgd2[3]-rgd2[2];
			d3 = Math.cos(rgd2[0]+Math.PI/2.);
			d4 = Math.cos(rgd2[1]+Math.PI/2.)-d3;	
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd2[3]-rgd2[2])*d4);
		}
			
		//initializing counts array
		rgdCounts = new double[iIterations];
		
		//looping through sampling points
		for(int i=1;i<=iIterations;i++){
			
			//loading random number
			du = Math.random(); dv = Math.random();
			
			//loading random point
			if(rgdSamplingBounds==null){
			
				dLng = du*d2+rgd1[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}else{
				
				//loading Lat and Lng
				dLng = du*d2+rgd2[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}
			
			//checking if sampling point is in range
			for(Polygon ply:setPolygons){
				if(sph1.isPointPolygon(dLat, dLng, ply, sAlgorithm)==1){
					rgdCounts[i-1]++;
				}
			}
		}
		
		//initializing output
		mapOut = new HashMap<Double,Double>();
		for(int i=1;i<=setPolygons.size();i++){
			mapOut.put((double) i, 0.);
		}
		for(int i=0;i<rgdCounts.length;i++){
	
			//looping through values
			for(double k=1;k<=rgdCounts[i];k++){
				
				//adding binomial coefficient
				dValue = mapOut.get(k);
				dValue+=this.findBinomial(rgdCounts[i], k);
				
				//saving value
				mapOut.put(k, dValue);
			}
		}
	
		//updating means
		for(double k:mapOut.keySet()){
			
			//loading value
			dValue = mapOut.get(k);
			
			//updating value
			dValue = dValue*dArea / ((double) iIterations);
			dValue = dValue/this.findBinomial((double) setPolygons.size(), k);
			
			//saving updated value
			mapOut.put(k,dValue);
		}
		
		//returning result
		return mapOut;
	}

	/**
	 * Finds area of intersection of current polygon and another polygon by monte carlo integration.
	 * @param rgdBounds Bounds for polygon.
	 * @param rgdSamplingBounds Area of intersection of this region with polygon will be computed.  Null if total area of polygon is to be found.
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @param iIterations Number of sampling points to use for monte carlo integration
	 * @param mapPolygon Returns the polygon for each species
	 * @param lstSpeciesSets List of subsets of species to consider
	 * @return Map: keys are sets of species, values are areas of intersection
	 */
	public Map<HashSet<String>,Double> findAreaIntersections(double rgdSamplingBounds[], String sAlgorithm, int iIterations, Map<String,Polygon> mapPolygon, ArrayList<HashSet<String>> lstSpeciesSets){
	
		//rgdBounds = polygon bounds
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//du = first current random number
		//dv = second current random number
		//dArea = area of bounding box (latitudinal edges are not great circle edges)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
		//rgd2 = sampling bounds in radians
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//lstOccur = list of all species that occurred
		//mapArea(setSpecies) = returns the area of intersection for the set of species
		//iMaxSubsetSize = maximum subset size
		//lstCurrentSubsets = current list of subsets
		//dValue = current map value being updated
		
		ArrayList<HashSet<String>> lstCurrentSubsets; ArrayList<String> lstOccur;
		Map<HashSet<String>,Double> mapArea;
		double dArea; double dValue;
		double du; double dv; double dLat; double dLng; double d2 = 0; double d3 = 0; double d4 = 0;
		double rgd1[] = null; double rgd2[] = null;
		int iMaxSubsetSize;
		
		//loading variables
		if(rgdSamplingBounds==null){
			
			//loading rgd1
			rgd1 = new double[4];
			rgd1[0] = DEG_TO_RAD*dLatMinimum;
			rgd1[1] = DEG_TO_RAD*dLatMaximum;
			rgd1[2] = DEG_TO_RAD*dLonMinimum;
			rgd1[3] = DEG_TO_RAD*dLonMaximum;
			
			//loading d2
			d2 = rgd1[3]-rgd1[2];
			
			//loading d3 and d4
			d3 = Math.cos(rgd1[0]+Math.PI/2.);
			d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd1[3]-rgd1[2])*d4);
			
		}else{
		
			rgd2 = new double[4];
			rgd2[0] = DEG_TO_RAD*rgdSamplingBounds[2];
			rgd2[1] = DEG_TO_RAD*rgdSamplingBounds[3];
			rgd2[2] = DEG_TO_RAD*rgdSamplingBounds[0];
			rgd2[3] = DEG_TO_RAD*rgdSamplingBounds[1];
			
			d2 = rgd2[3]-rgd2[2];
			d3 = Math.cos(rgd2[0]+Math.PI/2.);
			d4 = Math.cos(rgd2[1]+Math.PI/2.)-d3;	
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd2[3]-rgd2[2])*d4);
		}
		
		//initializing area map and maximum subset size
		mapArea = new HashMap<HashSet<String>,Double>();
		iMaxSubsetSize = -9999;
		for(int i=0;i<lstSpeciesSets.size();i++){
			mapArea.put(lstSpeciesSets.get(i), 0.);
			if(lstSpeciesSets.get(i).size()>iMaxSubsetSize){
				iMaxSubsetSize=lstSpeciesSets.get(i).size();
			}
		}
		
		//looping through sampling points
		for(int i=1;i<=iIterations;i++){
			
			//loading random number
			du = Math.random(); dv = Math.random();
			
			//loading random point
			if(rgdSamplingBounds==null){
			
				dLng = du*d2+rgd1[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}else{
				
				//loading Lat and Lng
				dLng = du*d2+rgd2[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}
			
			//loading list of species that occurred
			lstOccur = new ArrayList<String>(mapPolygon.keySet().size());
			for(String s:mapPolygon.keySet()){
				if(sph1.isPointPolygon(dLat, dLng, mapPolygon.get(s), sAlgorithm)==1){
					lstOccur.add(s);
				}
			}
			
			//checking if at least one species observed
			if(lstOccur.size()==0){
				continue;
			}
			
			//loading current subsets
			lstCurrentSubsets = this.loadSpeciesSubsets(lstOccur, iMaxSubsetSize);
			
			//looping through subsets
			for(int j=0;j<lstCurrentSubsets.size();j++){
				if(mapArea.containsKey(lstCurrentSubsets.get(j))){
					dValue = mapArea.get(lstCurrentSubsets.get(j));
					dValue++;
					mapArea.put(lstCurrentSubsets.get(j),dValue);
				}
			}
		}
	
		//updating areas
		for(HashSet<String> s:mapArea.keySet()){
			dValue = mapArea.get(s);
			dValue=dValue*dArea / ((double) iIterations);
			mapArea.put(s, dValue);
		}
		
		//returning result
		return mapArea;
	}

	/**
	 * Finds area of intersection of current polygon and another polygon by monte carlo integration.
	 * @param rgdBounds Bounds for polygon.
	 * @param rgdSamplingBounds Area of intersection of this region with polygon will be computed.  Null if total area of polygon is to be found.
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @param iIterations Number of sampling points to use for monte carlo integration
	 * @param mapPolygon Returns the polygon for each species
	 * @param lstSpeciesSets List of subsets of species to consider
	 * @return Map: keys are multiplicities of intersections (e.g., 3 is for intersections of three species), values are mean areas of intersection (e.g., mean area of three-way intersections across all 3-subsets of species)
	 */
	public double[] findAreaIntersections0(double rgdSamplingBounds[], String sAlgorithm, int iIterations, Map<String,Polygon> mapPolygon, ArrayList<HashSet<String>> lstSpeciesSets){
	
		//rgdBounds = polygon bounds
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//du = first current random number
		//dv = second current random number
		//dArea = area of bounding box (latitudinal edges are not great circle edges)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
		//rgd2 = sampling bounds in radians
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//mapOccur(sSpecies) = returns true if species occurs in current location, false otherwise
		//rgdArea = returns the area of intersection for the corresponding subset in lstSpeciesSets
		//bAddOne = flag for whether to increment current species subset
		//bAtLeastOne = flag for whether at least one species was observed
		
		Map<String,Boolean> mapOccur;
		double dArea;
		double du; double dv; double dLat; double dLng; double d2 = 0; double d3 = 0; double d4 = 0;
		double rgd1[] = null; double rgd2[] = null; double rgdArea[];
		boolean bAddOne; boolean bAtLeastOne;
		
		//loading variables
		if(rgdSamplingBounds==null){
			
			//loading rgd1
			rgd1 = new double[4];
			rgd1[0] = DEG_TO_RAD*dLatMinimum;
			rgd1[1] = DEG_TO_RAD*dLatMaximum;
			rgd1[2] = DEG_TO_RAD*dLonMinimum;
			rgd1[3] = DEG_TO_RAD*dLonMaximum;
			
			//loading d2
			d2 = rgd1[3]-rgd1[2];
			
			//loading d3 and d4
			d3 = Math.cos(rgd1[0]+Math.PI/2.);
			d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd1[3]-rgd1[2])*d4);
			
		}else{
		
			rgd2 = new double[4];
			rgd2[0] = DEG_TO_RAD*rgdSamplingBounds[2];
			rgd2[1] = DEG_TO_RAD*rgdSamplingBounds[3];
			rgd2[2] = DEG_TO_RAD*rgdSamplingBounds[0];
			rgd2[3] = DEG_TO_RAD*rgdSamplingBounds[1];
			
			d2 = rgd2[3]-rgd2[2];
			d3 = Math.cos(rgd2[0]+Math.PI/2.);
			d4 = Math.cos(rgd2[1]+Math.PI/2.)-d3;	
		
			//finding area of bounding polygon
			dArea = Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd2[3]-rgd2[2])*d4);
		}
		
		//initializing area map
		rgdArea = new double[lstSpeciesSets.size()];
		
		//looping through sampling points
		for(int i=1;i<=iIterations;i++){
			
			//loading random number
			du = Math.random(); dv = Math.random();
			
			//loading random point
			if(rgdSamplingBounds==null){
			
				dLng = du*d2+rgd1[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}else{
				
				//loading Lat and Lng
				dLng = du*d2+rgd2[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
			}
			
			//loading list of species that occurred
			mapOccur = new HashMap<String,Boolean>();
			bAtLeastOne = false;
			for(String s:mapPolygon.keySet()){
				if(sph1.isPointPolygon(dLat, dLng, mapPolygon.get(s), sAlgorithm)==1){
					mapOccur.put(s, true);
					bAtLeastOne=true;
				}else{
					mapOccur.put(s, false);
				}
			}
			
			//checking if at least one species observed
			if(bAtLeastOne==false){
				continue;
			}
			
			//looping through subsets
			for(int j=0;j<lstSpeciesSets.size();j++){
				bAddOne=true;
				for(String s:lstSpeciesSets.get(j)){
					if(mapOccur.get(s)==false){
						bAddOne=false;
						break;
					}
				}
				if(bAddOne==true){
					rgdArea[j]++;
				}
			}
		}
	
		//updating areas
		for(int i=0;i<rgdArea.length;i++){
			rgdArea[i]=rgdArea[i]*dArea / ((double) iIterations);
		}
		
		//returning result
		return rgdArea;
	}
	
	/**
	 * Finds N choose K
	 * @return Value of N choose K
	 */
	
	private double findBinomial(double dN, double dK){
		
		//d1 = output
		
		double d1;
		
		//checking value
		if(dN<dK){
			return 0.;
		}
		
		//initializing output
		d1 = 0.;
		
		//checking for most efficient approach
		if(dK>dN-dK){
		
			//loading output
			for(double d=dN;d>dK;d--){
				d1+=Math.log(d);
			}
			for(double d=dN-dK;d>1;d--){
				d1-=Math.log(d);
			}
		}else{
			for(double d=dN;d>dN-dK;d--){
				d1+=Math.log(d);
			}
			for(double d=dK;d>1;d--){
				d1-=Math.log(d);
			}
		}
		
		//returning result
		return Math.round(Math.exp(d1));
	}
	
	/**
	 * Finds diameter of polygon.
	 * @return Diameter of polygon (distance between furthest pair of vertices)
	 */
	
	public double findDiameter(){
		
		//dOut = output
		//dLat = current latitude
		//dLon = current longitude
		//d1 = current distance
		
		double dOut; double dLat; double dLon; double d1;
		
		//initializing output
		dOut = -999999999999999999.;
		
		//looping through edges
		for(int i=0;i<lstEdges.size();i++){
			
			//loading current latitude and longitude
			dLat = lstEdges.get(i).dLatStart;
			dLon = lstEdges.get(i).dLonStart;
			
			//looping through distances
			for(int j=0;j<lstEdges.size();j++){
				
				//loading current distance
				d1 = sph1.findDistanceToEdge(dLat, dLon, lstEdges.get(j));
				
				//checking if distance should be updated
				if(d1>dOut){
					dOut = d1;
				}
			}
		}
		
		//returning result
		return dOut;
	}
	
	/**
	 * Finds maximum distance to polygon
	 * @param dResolution Resolution of grid to use for searching for most distance point
	 * @return Distance from most distance point to polygon
	 */
	public double findMaximumDistance(double dResolution){
		
		//dOut = output
		//dLat = current latitude
		//dLon = current longitude
		//d1 = current distance
		
		double dOut; double dLat; double dLon; double d1;
		
		//initializing output
		dOut = -999999999999999999.;
		
		//looping through latitudes and longitudes
		dLat = 90.-dResolution;
		do{
		
			//looping through longitudes
			dLon = -180.+dResolution;
			do{
			
				//finding distance to polygon
				d1 = sph1.findDistanceToPolygon(dLat, dLon, this, "even-odd")[0];
					
				//checking if output distance should be updated
				if(d1>dOut){
					dOut = d1;
				}
				
				//updating longitude
				dLon+=dResolution;
			}while(dLon<180);
			
			//updating latitude
			dLat-=dResolution;
			
			System.out.println(dLat);
			
		}while(dLat>-90);
			
		//returning result
		return dOut;
	}

	/**
	 * Loads hashmap giving the disjoint region to which each edge belongs
	 */
	private void loadDisjointRegionMap(){
		
		//edg1 = current edge
		//sVertexStart = starting point in string form
		//sVertexEnd = ending point in string form
		//rgd1 = current start and end latitudes and longitude
		//iCurrentRegion = current region counter
		//map1(sVertex) = return region to which vertex belongs
		//iRegion = region to which edge belongs
		
		String sVertexStart; String sVertexEnd;
		Edge edg1;
		double rgd1[][];
		int iCurrentRegion; int iRegion;
		Map<String,Integer> map1;
		
		//initializing maps
		mapDisjointRegion = new HashMap<Integer,Integer>();
		map1 = new HashMap<String,Integer>();
		
		//initializing current region counter
		iCurrentRegion = 0;
		
		//looping through edges
		for(int i=0;i<lstEdges.size();i++){
		
			//loading current edge
			edg1 = lstEdges.get(i);
			
			//loading vertices
			rgd1 = new double[2][2];
			rgd1[0][0]=edg1.dLatStart;
			rgd1[0][1]=edg1.dLonStart;
			rgd1[1][0]=edg1.dLatEnd;
			rgd1[1][1]=edg1.dLonEnd;
			
			//initializing region for edge
			iRegion = -9999;
			
			//checking if edges ends at -180
			for(int k=0;k<2;k++){
				if(rgd1[k][1]==-180){
					rgd1[k][1]=180;
				}
			}
			
			//loading vertices
			sVertexStart = rgd1[0][0] + "," + rgd1[0][1];
			sVertexEnd = rgd1[1][0] + "," + rgd1[1][1];
			
			//checking if vertices are already labeled
			if(map1.containsKey(sVertexStart)){
				iRegion = map1.get(sVertexStart);
				map1.put(sVertexEnd, iRegion);
			}else{
				if(map1.containsKey(sVertexEnd)){
					iRegion = map1.get(sVertexEnd);
					map1.put(sVertexStart,iRegion);
				}else{
					iCurrentRegion++;
					iRegion=iCurrentRegion;
					map1.put(sVertexStart, iRegion);
					map1.put(sVertexEnd, iRegion);
				}
			}
			
			//classifying edge
			mapDisjointRegion.put(i, iRegion);
		}
	}
	
	/**
	 * Counts number of disjoint portions of polygon.
	 * @param bds1 Bounds for counting disjoint regions.  If null, then total number of regions are counted.
	 * @return Number of disjoint regions
	 */
	public double findNumberOfDisjointRegions(GeographicPointBounds bds1){
		
		//edg1 = current edge
		//iRegion = current region
		//mapCrossings(iRegion) = returns the number of times edges of given region cross bounds
		//mapEntirelyWithinBounds(iRegion) = returns true if region is entirely within bounds
		//dLengthInBounds = length in bounds
		//i1 = count for bounds crossings
		//dOut = output
		
		Edge edg1;
		int iRegion; int i1;
		Map<Integer,Integer> mapCrossings; Map<Integer,Boolean> mapEntirelyWithinBounds;
		double dLengthInBounds; double dOut;
		
		//initializing disjoint region map
		if(mapDisjointRegion==null){
			this.loadDisjointRegionMap();
		}
		
		//initializing map of crossings
		mapCrossings = new HashMap<Integer,Integer>();
		mapEntirelyWithinBounds = new HashMap<Integer,Boolean>();
		
		//looping through edges
		for(int i=0;i<lstEdges.size();i++){
		
			//loading current edge
			edg1 = lstEdges.get(i);
			
			//loading current region
			iRegion = mapDisjointRegion.get(i);
			
			//checking if bounded
			if(bds1==null){
				if(!mapEntirelyWithinBounds.containsKey(iRegion)){
					mapEntirelyWithinBounds.put(iRegion, true);
				}
			}else{
				
				//checking if region has been initialized
				if(!mapEntirelyWithinBounds.containsKey(iRegion)){
					mapEntirelyWithinBounds.put(iRegion, true);
					mapCrossings.put(iRegion, 0);
				}
				
				//loading length in bounds
				dLengthInBounds = sph1.findShortEdgeLengthInBounds(edg1, bds1.rgdArray);
				
				//checking whether edge crosses bounds or is out of bounds
				if(Math.abs(dLengthInBounds-edg1.dLength)>0.0000001){
					mapEntirelyWithinBounds.put(iRegion,false);
					if(dLengthInBounds>0){
						i1 = mapCrossings.get(iRegion);
						i1++;
						mapCrossings.put(iRegion, i1);
					}
				}
			}
		}
		
		//loading output
		dOut = 0;
		for(Integer i:mapEntirelyWithinBounds.keySet()){
			if(mapEntirelyWithinBounds.get(i)==true){
				dOut++;
			}
			if(bds1!=null){
				dOut+=Math.ceil(mapCrossings.get(i)/2.);
			}
		}
		
		//returning result
		return dOut;
	}
	
	/**
	 * Finds perimeter of polygon.
	 * @param rgdSamplingBounds Area of intersection of this region with polygon will be computed.  Null if total perimeter of polygon is to be found.
	 * @return Perimeter of polygon.
	 */
	public double findPerimeter(double rgdSamplingBounds[]){
		
		//d1 = output
		//edg1 = current edge
		
		double d1=0; 
		Edge edg1;
		
		//initializing spherical geometry object
		if(sph1 ==null){
			sph1 = new SphericalGeometry();
		}
		
		//no bounding box
		if(rgdSamplingBounds==null){
		
			//looping through edges
			for(int i=0;i<lstEdges.size();i++){
	
				//adding length of current edge
				d1+=lstEdges.get(i).dLength;
			}	
		}else{
			
			//looping through edges
			for(int i=0;i<lstEdges.size();i++){
		
				//loading edge
				edg1 = lstEdges.get(i);
				
				//updating distance
				d1+=sph1.findShortEdgeLengthInBounds(edg1, rgdSamplingBounds);
				
				////checking if vertices are in polygon
				//i1 = sph1.checkPointInBounds(edg1.dLatStart, edg1.dLonStart, rgdSamplingBounds);
				//i2 = sph1.checkPointInBounds(edg1.dLatEnd, edg1.dLonEnd, rgdSamplingBounds);
				//if(i1==1 && i2==1){
				//	
				//	//adding length of current edge
				//	d1+=edg1.dLength;
				//}else if(i1==1 && i2==0){
				//	dPartial+=edg1.dLength;
				//}else if(i2==0 && i2==1){
				//	dPartial+=edg1.dLength;
				//}
			}
			//d1+=dPartial/2.;
		}
		
		//saving result
		return d1;
	}
	
	
	/**
	 * Formats current polygon for printing
	 * @return Current polygon formattted for printing.
	 */
	public String[][] formatForPrinting(String sSpecies){
		
		//lst1 = polygon in vertex list format
		//rgs1 = output
		
		
		String rgs1[][];
		ArrayList<double[]> lst1;
		
		//loading current polygon in vertex list format
		lst1 = this.convertToVertexList();
		
		//loading output
		rgs1 = new String[lst1.size()][1];
		for(int i=0;i<rgs1.length;i++){
			rgs1[i][0]=sSpecies + "," + Integer.toString((int) lst1.get(i)[0]) + "," + Double.toString(lst1.get(i)[2]) + "," + Double.toString(lst1.get(i)[1]);
		}
		
		//returning result
		return rgs1;
	}
	
	/**
	 * Gets bounds for polygon
	 * @return Bonds in following order: lonmin, lonmax, latmin, latmax
	 */
	public double[] getBounds(){
		
		if(rgdBounds==null){
		
			//initializing bounds
			rgdBounds = new double[4];
	
			//outputting results
			rgdBounds[0]=this.dLonMinimum;
			rgdBounds[1]=this.dLonMaximum;
			rgdBounds[2]=this.dLatMinimum;
			rgdBounds[3]=this.dLatMaximum;
		}
		
		return rgdBounds;
	}

	/**
	 * Gets edge with specified index
	 * @param iEdge Edge to return
	 * @return Edge
	 */
	public Edge getEdge(int iEdge){
		return lstEdges.get(iEdge);
	}

	public double getEdgeCount(){
		return lstEdges.size();
	}

	/**
	 * Gets list of edges intersecting given latitude and longitude
	 * @param dLat Latitude of interest
	 * @param dLon Longitude of interest
	 * @return List of edges intersecting given Latitude
	 */
	public ArrayList<Edge> getIntersectingEdges(double dLat, double dLon){
		
		//loading interval tree if necessary
		if(int1==null){
			int1 = new PolygonIntervalTree(lstEdges);
		}
		
		//outputting result
		return int1.findEdges(dLat,dLon);
	}

	/**
	 * Gets list of edges intersecting given latitude
	 * @param dLat Latitude of interest
	 * @return List of edges intersecting given Latitude
	 */
	public ArrayList<Edge> getIntersectingEdgesLatitude(double dLat){
		
		//loading interval tree if necessary
		if(int1==null){
			int1 = new PolygonIntervalTree(lstEdges);
		}
		
		//outputting result
		return int1.findEdgesLatitude(dLat);
	}

	/**
	 * Gets list of edges intersecting given longitude
	 * @param dLon Longitude of interest
	 * @return List of edges intersecting given longitude
	 */
	public ArrayList<Edge> getIntersectingEdgesLongitude(double dLon){
		
		//loading interval tree if necessary
		if(int1==null){
			int1 = new PolygonIntervalTree(lstEdges);
		}
		
		//outputting result
		return int1.findEdgesLongitude(dLon);
	}

	/**
	 * Gets list of vertices+dRadius intersecting given latitude
	 * @param dLat Latitude of interest
	 * @return List of vertices (dLat,dLon) intersecting given Latitude
	 */
	public ArrayList<Edge> getIntersectingOffsetEdgesLatitude(double dRadius, double dLat){
		
		//lst1 = list of 
		//lst2 = output
		//dOffset = radius in degrees of latitude
		//edg1 = current edge
		
		double dOffset;
		ArrayList<Edge> lst1; ArrayList<Edge> lst2;
		Edge edg1;
		
		//loading offset
		dOffset = dRadius/LAT_DISTANCE;
		
		//loading interval tree if necessary
		if(this.dRadius!=dRadius){
			initializeOffsetTree(dRadius);
		}
		
		//loading list of edges
		lst1 =  int2.findEdgesLatitude(dLat);
		
		//checking if lst1 is null
		if(lst1==null){
			return null;
		}
		
		//removing offsets
		lst2 = new ArrayList<Edge>();
		for(int i=0;i<lst1.size();i++){
			
			//loading edge
			edg1 = lst1.get(i);
			
			//saving updated edge
			if(edg1.dLatStart<edg1.dLatEnd){
				lst2.add(new Edge(edg1.dLatStart+dOffset,edg1.dLatEnd-dOffset,edg1.dLonStart,edg1.dLonEnd));
			}else{
				lst2.add(new Edge(edg1.dLatStart-dOffset,edg1.dLatEnd+dOffset,edg1.dLonStart,edg1.dLonEnd));
			}
		}
		
		//returning result
		return lst2;	
	}
	
	/**
	 * Initializes interval tree.
	 */
	public void initializeIntervalTree(){
		int1 = new PolygonIntervalTree(lstEdges);
	}
	
	/**
	 * Initializes tree for looking up edges + radius overlapping a given latitude
	 * @param dRadius Radius of interest.
	 */
	private void initializeOffsetTree(double dRadius){
		
		//lst1 = list of edges with latitudinal offset
		//dOffset = radius in degrees of latitude
		//edg1 = current edge being added
		
		double dOffset;
		ArrayList<Edge> lst1;
		Edge edg1;
		
		//loading radius
		this.dRadius = dRadius;
		
		//loading offset
		dOffset = dRadius/LAT_DISTANCE;
		
		//loading list of edges
		lst1 = new ArrayList<Edge>();
		for(int i=0;i<lstEdges.size();i++){
			if(lstEdges.get(i).dLatStart<lstEdges.get(i).dLatEnd){
				edg1 = new Edge(lstEdges.get(i).dLatStart-dOffset,lstEdges.get(i).dLatEnd+dOffset,lstEdges.get(i).dLonStart,lstEdges.get(i).dLonEnd);
			}else{
				edg1 = new Edge(lstEdges.get(i).dLatStart+dOffset,lstEdges.get(i).dLatEnd-dOffset,lstEdges.get(i).dLonStart,lstEdges.get(i).dLonEnd);
			}
			lst1.add(edg1);
		}
		
		//loading interval tree
		int2 = new PolygonIntervalTree(lst1);
	}

	/**
	 * Loads lists of species subsets for which to calculate areas of intersections
	 * @param setSpecies Set of all from which to assemble subsets
	 * @param iMaxSubsetSize Maximum subset size
	 * @return List giving subsets of species
	 */
	private ArrayList<HashSet<String>> loadSpeciesSubsets(ArrayList<String> lstSpecies, int iMaxSubsetSize){
		
		//lstSpeciesSets = list of subsets of species
		//rgi1 = current subset
		//com1 = CombinatoricAlgorithms object
		//iCounter = counter for adding to subset
		
		int iCounter;
		ArrayList<HashSet<String>> lstSpeciesSets;
		int rgi1[];
		CombinatoricAlgorithms com1;
		
		//initializing output
		lstSpeciesSets = new ArrayList<HashSet<String>>();
		
		//initializing counter
		iCounter=0;
		
		//looping through subset sizes
		for(int i=1;i<=lstSpecies.size();i++){
			
			//checking number of subsets
			//if(this.findBinomial((double) lstSpecies.size(), (double) i)>iMaxSubsets){
			if(i>iMaxSubsetSize){
				return lstSpeciesSets;
			}
			
			//initializing combinatoric algorithms object
			com1 = new CombinatoricAlgorithms();
			
			
			//looping through subsets
			rgi1 = com1.NEXKSB(lstSpecies.size(), i);
			while(rgi1[0]!=-9999){
				
				//loading current set of species
				lstSpeciesSets.add(new HashSet<String>());
				for(int j=0;j<rgi1.length;j++){
					lstSpeciesSets.get(iCounter).add(lstSpecies.get(rgi1[j]-1));
				}
				iCounter++;
			
				//loading next subset
				rgi1 = com1.NEXKSB(lstSpecies.size(), i);	
			}
		}
		
		//returning result
		return lstSpeciesSets;
	}
	
	/**
	 * Splits edge that crosses -180 or 180 so that two component edges are within -180 to 180
	 * @param edg1 Edge to split
	 * @param dLonSplit Splitting longitude
	 * @return Pair of edges that correspond to split edge
	 */
	private Edge[] splitEdge(Edge edg1, double dLonSplit){
		
		//dLat = latitude where edge crosses splitting longitude
		//rge1 = output
		
		Edge rge1[];
		double dLat;
		
		//initializing output
		rge1 = new Edge[2];
		
		//checking that splitting longitude is -180 or 180
		if(dLonSplit!=-180 && dLonSplit!=180){
			return null;
		}
		
		//loading latitude where edge crosses splitting longitude
		dLat = sph1.findLatitudeDegree(edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd, dLonSplit);
		
		//rounding latitude
		dLat = ElementaryMathOperations.round(dLat,7.);
		
		//checking cases
		if(dLonSplit==-180 && edg1.dLonStart<-180.){
			rge1[0] = new Edge(edg1.dLatStart,dLat,edg1.dLonStart + 360.,180.);
			rge1[1] = new Edge(dLat,edg1.dLatEnd,-180.,edg1.dLonEnd);
		}else if(dLonSplit==-180. && edg1.dLonEnd<-180.){
			rge1[0] = new Edge(edg1.dLatStart,dLat,edg1.dLonStart,-180.);
			rge1[1] = new Edge(dLat,edg1.dLatEnd,180,edg1.dLonEnd+360.);
		}else if(dLonSplit==180. && edg1.dLonEnd>180.){
			rge1[0] = new Edge(edg1.dLatStart,dLat,edg1.dLonStart,180.);
			rge1[1] = new Edge(dLat,edg1.dLatEnd,-180,edg1.dLonEnd-360.);
		}else if(dLonSplit==180. && edg1.dLonStart>180.){
			rge1[0] = new Edge(edg1.dLatStart,dLat,edg1.dLonStart-360.,-180.);
			rge1[1] = new Edge(dLat,edg1.dLatEnd,180.,edg1.dLonEnd);
		}else{
			rge1 = null;
		}
		
		//************************
		//for(int i=0;i<2;i++){
		//	if(rge1[i].getLatMinimum()<0){
		//		System.out.println("HERE");
		//	}
		//}
		//************************
		
		
		
		
		//returning result
		return rge1;
	}

	/**
	 * Updated bounds
	 * @param edg1 Edge being added.
	 */
	private void updateBounds(Edge edg1){
		
		//updating bounds
		if(dLatMinimum>edg1.getLatMinimum()){
			dLatMinimum = edg1.getLatMinimum();
		}
		if(dLatMaximum<edg1.getLatMaximum()){
			dLatMaximum = edg1.getLatMaximum();
		}
		if(edg1.bCross180==false){
			if(dLonMinimum>edg1.getLonMinimum()){
				dLonMinimum = edg1.getLonMinimum();
			}
			if(dLonMaximum<edg1.getLonMaximum()){
				dLonMaximum = edg1.getLonMaximum();
			}
		}else{
			dLonMinimum = -180;
			dLonMaximum = 180;
		}
	}
	
}

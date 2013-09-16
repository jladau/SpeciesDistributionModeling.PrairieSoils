package edu.ucsf.base;

import java.util.ArrayList;

/**
 * Implements various spherical geometry algorithms.
 * @author jladau
 */

public class SphericalGeometry {

	private static final double RAD_TO_DEG = 57.2957795130823;
	private static final double EARTH_RADIUS = 6371;
	private static final double EARTH_RADIUS_SQUARED = 40589641;
	private static final double DEG_TO_RAD = 0.01745329251994;
	private static final double LAT_DISTANCE = 111.2;
	
	/**
	 * Constructor: currently empty
	 */
	public SphericalGeometry(){
	}

	/**
	 * Checks whether a disk of given radius intersects a bounding box.
	 * @param dLat Latitude of center of disk.
	 * @param dLng Longitude of center of disk.
	 * @param rgdBounds Bounding box being checked.
	 * @param dRadius Radius of disk (in km).
	 * @return Returns 1 if there is an intersection; 0 otherwise.
	 */
	private int checkDiskBoundsIntersection(double dLat, double dLng, double rgdBounds[], double dRadius){
		 
		 //rgdX = top, right, bottom, or left coordinate of disk
		 
		 double rgdX[];
		 
		 //checking if point is within bounds
		 if(checkPointInBounds(dLat, dLng, rgdBounds)==1){
			 return 1;
		 }
		 
		 //checking top point
		 rgdX = findDestination(dLat, dLng, 0, dRadius);
		 if(checkPointInBounds(rgdX[0], rgdX[1], rgdBounds)==1){
			 return 1;
		 }
		 
		 //checking right point
		 rgdX = findDestination(dLat, dLng, Math.PI/2., dRadius);
		 if(checkPointInBounds(rgdX[0], rgdX[1], rgdBounds)==1){
			 return 1;
		 }
		 
		 //checking bottom point
		 rgdX = findDestination(dLat, dLng, Math.PI, dRadius);
		 if(checkPointInBounds(rgdX[0], rgdX[1], rgdBounds)==1){
			 return 1;
		 }
		 
		 //checking left point
		 rgdX = findDestination(dLat, dLng, 3.*Math.PI/2., dRadius);
		 if(checkPointInBounds(rgdX[0], rgdX[1], rgdBounds)==1){
			 return 1;
		 }
		 
		 //checking corners
		 if(findDistance(dLat, dLng, rgdBounds[2], rgdBounds[0])<=dRadius){
			 return 1;
		 }
		 if(findDistance(dLat, dLng, rgdBounds[3], rgdBounds[0])<=dRadius){
			 return 1;
		 }
		 if(findDistance(dLat, dLng, rgdBounds[2], rgdBounds[1])<=dRadius){
			 return 1;
		 }
		 if(findDistance(dLat, dLng, rgdBounds[3], rgdBounds[1])<=dRadius){
			 return 1;
		 }
		
		 //no intersection
		 return 0; 
	 }
	
	/**
	 * Checks whether disk is entirely within bounding box.
	 * @param dLat Latitude of center of disk.
	 * @param dLng Longitude of center of disk.
	 * @param dRadius Radius of disk
	 * @param rgdBounds Bounding box.
	 * @return 1 if point is within bounding box; 0 otherwise.
	 */
	public int checkDiskInBounds(double dLat, double dLng, double dRadius, double rgdBounds[]){
		 
		 //i1 = output
		 //d1 = distance in latitude degrees of radius
		 //d2 = distance in longitude degrees of radius
		
		 double d1; double d2;
		 int i1=0;
		 
		 //loading distances
		 d1 = dRadius/LAT_DISTANCE;
		 d2 = dRadius/(this.findDistance(dLat, 0, dLat, 1));
		 
		 //checking if point is within bounds
		 if(dLat>=rgdBounds[2]+d1){
			 if(dLat<=rgdBounds[3]-d1){
				 if(dLng>=rgdBounds[0]+d2){
					 if(dLng<=rgdBounds[1]-d2){
						 i1=1;
					 }
				 }
			 }
		 }
		 
		 //returning result
		 return i1;
	 }

	/**
	 * Checks whether a point is within a (polygon + disk of given radius)
	 * @param dLat Latitude of point.
	 * @param dLng Longitude of point.
	 * @param rgdPolygon Polygon.
	 * @param rgdBounds Bounding box for polygon.
	 * @param dRadius Radius of disk being checked.
	 * @param dThreshProp Threshold.
	 * @return 1 if point is within disk; 0 otherwise.
	 */
	public int checkDiskIntersection(double dLat, double dLng, double rgdPolygon[][], double rgdBounds[], double dRadius, double dThreshProp){
			
		 //rgd1 = first entry with cross track distance between great circle joining vertices and dLat,dLng; second entry gives along track distance
		 //d1 = shortest distance from arc segment to dLat,dLng
		 //dThreshold = distance distance at which to call interior points
		 //i1 = flag for whether to run initial bounds loop
		 //d2 = angular difference
		
		 int i;
		 double d1=0; double dThreshold; double d2;
		 double rgd1[];
		 
		 //loading dThreshold
		 dThreshold = dThreshProp*dRadius;
		 
		 //checking bounds	 
		 if(checkDiskBoundsIntersection(dLat, dLng, rgdBounds, dRadius)==0){
			 return 0;
		 }
	
		 //checking if dLat,dLng is within polygon
	 	 if(isPointPolygonEvenOdd(dLat, dLng, rgdPolygon, rgdBounds)==1){
			 return 1;
		 }
		 
		 //looping through vertices of polygon
		 for(i=0;i<rgdPolygon.length-1;i++){
			 
			 //checking if same polygon
			 if(rgdPolygon[i][2]==rgdPolygon[i+1][2]){
			 
				 //computing angular difference
				 d2 = findAngularDifference(rgdPolygon[i][1], rgdPolygon[i][0], rgdPolygon[i+1][1], rgdPolygon[i+1][0], dLat, dLng);
				 
				 //computing distance from first vertex to point
				 d1 = findDistance(dLat, dLng, rgdPolygon[i][1], rgdPolygon[i][0]);
				 
				 //checking angular difference
				 if(d2>Math.PI/2. && d2<3.*Math.PI/2.){
					 
					 //checking distance from first vertex to point
					 if(d1<dThreshold){
						 return 1;
					 }
				 }else{
					 
					 //computing along track and cross track distances
					 rgd1 = findTrackDistance(d1, d2);
						
					 //checking whether along track distance is greater than distance between vertex
					 if(rgd1[1]>rgdPolygon[i+1][3]){
						 
						 //checking distance from second vertex to point
						 if(findDistance(dLat, dLng, rgdPolygon[i+1][1], rgdPolygon[i+1][0])<dThreshold){
							 return 1;
						 }
					 }else{
						 
						 //checking cross track distance
						 if(rgd1[0]<dThreshold){
							 return 1;
						 }
					 } 
				 }
			 }
		 }
		 
		 //no intersection
		 return 0; 
	 }

	/**
	 * Checks if disk of specified radius intersects polygon
	 * @param dLat Latitude of point.
	 * @param dLng Longitude of point.
	 * @param ply1 Polygon.
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @return True if point is within distance, false otherwise
	 */
	public boolean checkDiskIntersection(double dLat, double dLng, Polygon ply1, String sAlgorithm, double dRadius){
			
		 //rgd1 = first entry with cross track distance between great circle joining vertices and dLat,dLng; second entry gives along track distance
		 //d1 = shortest distance from arc segment to dLat,dLng
		 //d2 = angular difference
		 //edg1 = current edge
		 //lstEdges = list of edges 
		
		 double d1=0; double d2;
		 double rgd1[];
		 Edge edg1;
		 ArrayList<ArrayList<Edge>> lstEdges;
		 
		 //checking if dLat,dLng is within polygon
		 //************************
		 //for(int i=0;i<ply1.getEdgeCount();i++){
		 //	 System.out.println(ply1.getEdge(i).dLonStart + "," + ply1.getEdge(i).dLatStart + "," + ply1.getEdge(i).dLonEnd + "," + ply1.getEdge(i).dLonEnd);
		 //}
		 //************************
		 if(isPointPolygon(dLat, dLng, ply1, sAlgorithm)==1){
			 return true;
		 }
	
		 //*****************************
		 dLng = this.correctLongitude(dLng);
		 //if(dLng>180){
		 //	 dLng-=360.;
		 //}
		 //if(dLng<-180){
		 //	 dLng+=360.;
		 //}
		 //*****************************
		 
		 
		 //initializing polygon bounds
		 if(ply1.bds1==null){
			 ply1.initializeBounds();
		 }
		 
		 //loading list of edges to check
		 lstEdges = ply1.bds1.findEdgesWithinRadius(dLat, dLng, dRadius);
		 
		 //looping through edges
		 for(int i=0;i<lstEdges.size();i++){
			 for(int j=0;j<lstEdges.get(i).size();j++){
				 
				 //loading current edge
				 edg1 = lstEdges.get(i).get(j);
				 
				 //computing angular difference
				 d2 = findAngularDifference(edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd, dLat, dLng);
				                                                                                                                                                                                                                                                                 
				 //computing distance from first vertex to point
				 d1 = findDistance(dLat, dLng, edg1.dLatStart, edg1.dLonStart);
				 
				 //checking angular difference
				 if(d2>Math.PI/2. && d2<3.*Math.PI/2.){
					 
					 //checking distance from first vertex to point
					 if(d1<dRadius){
						 return true;
					 }
				 }else{
					 
					 //computing along track and cross track distances
					 rgd1 = findTrackDistance(d1, d2);
						
					 //checking whether along track distance is greater than distance between vertex
					 if(rgd1[1]>edg1.dLength){
						 
						 //checking distance from second vertex to point
						 d1 = findDistance(dLat, dLng, edg1.dLatEnd, edg1.dLonEnd);
						 if(d1<dRadius){
							return true;
						 }
					 }else{
						 
						 //checking cross track distance
						 if(rgd1[0]<dRadius){
							 return true;
						 }
					 } 
				 }
			 }
		 }
		 	
		 //returning result
		 return false;
	 }

	/**
	 * Checks whether point is within bounding box.
	 * @param dLat Latitude of point.
	 * @param dLng Longitude of point.
	 * @param rgdBounds Bounding box.
	 * @return 1 if point is within bounding box; 0 otherwise.
	 */
	public int checkPointInBounds(double dLat, double dLng, double rgdBounds[]){
		 
		 //i1 = output
		 
		 int i1=0;
		 
		 //checking if point is within bounds
		 if(dLat>=rgdBounds[2]){
			 if(dLat<=rgdBounds[3]){
				 if(dLng>=rgdBounds[0]){
					 if(dLng<=rgdBounds[1]){
						 i1=1;
					 }
				 }
			 }
		 }
		 
		 //returning result
		 return i1;
	 }

	/**
	 * Checks whether point is within bounding box.
	 * @param dLat Latitude of point.
	 * @param dLng Longitude of point.
	 * @param ply1 Polygon
	 * @return 1 if point is within bounding box; 0 otherwise.
	 */
	public int checkPointInBounds(double dLat, double dLng, Polygon ply1){
		 
		 //i1 = output
		 
		 int i1=0;
		 
		 //checking if point is within bounds
		 if(dLat>=ply1.dLatMinimum){
			 if(dLat<=ply1.dLatMaximum){
				 if(dLng>=ply1.dLonMinimum){
					 if(dLng<=ply1.dLonMaximum){
						 i1=1;
					 }
				 }
			 }
		 }
		 
		 //returning result
		 return i1;
	 }
	
	/**
	 * Checks whether point is within specified distance of bounds.  Bounds are assumed to be a strip (longitudes unbounded)
	 * @param bds1 Bounds of interest
	 * @param dLat Latitude of point
	 * @param dLon Longitude of point
	 * @param dRadius Distance from point to bounds
	 * @return True if distance from point to bounds is less than radius, false otherwise
	 */
	public boolean checkPointWithinDistanceOfBounds(GeographicPointBounds bds1, double dLat, double dLon, double dRadius){
		
		
		//d1 = current offset value
		
		double d1;
		
		//checking if point is within bounds
		if(bds1.isPointInBounds(dLat, dLon)==true){
			return true;
		}
		
		//checking for overlap in upward direction
		d1 = bds1.dLatitudeMax+dRadius/LAT_DISTANCE;
		if(d1<=90){
			if(bds1.dLatitudeMax<=dLat && dLat<=d1){
				return true;
			}
		}else{
			d1=-d1+180.;
			if(bds1.dLatitudeMax<=dLat || d1<=dLat){
				return true;
			}
		}
		
		//checking for overlap in the downward direction
		d1 = bds1.dLatitudeMin-dRadius/LAT_DISTANCE;
		if(d1>=-90){
			if(d1<=dLat && dLat<=bds1.dLatitudeMin){
				return true;
			}
		}else{
			d1=-d1-180.;
			if(dLat<=bds1.dLatitudeMin || dLat<=d1){
				return true;
			}
		}
		
		//not within distance
		return false;
	}

	/**
	 * Corrects longitude so it falls between -180 and 180
	 * @param dLongitude Longitude in question (may be greater than 180 or less than -180)
	 * @return Corrected longitude
	 */
	public double correctLongitude(double dLongitude){
		
		//dOut = output
		
		double dOut;
		
		dOut = ((dLongitude + 180.) % 360.);
		if(dOut<0){
			dOut+=360.;
		}
		dOut-=180;
		return dOut;
	}

	/**
	 * Checks whether two sets of bounds overlap
	 * @param rgdBounds1 First set of bounds
	 * @param rgdBounds2 Second set of bounds
	 * @return True if bounds overlap, false otherwise
	 */
	public boolean doBoundsOverlap(double rgdBounds1[], double rgdBounds2[]){
		
		for(int i=0;i<2;i++){
			for(int j=2;j<4;j++){
				if(checkPointInBounds(rgdBounds1[j],rgdBounds1[i],rgdBounds2)==1){
					return true;
				}
				if(checkPointInBounds(rgdBounds2[j],rgdBounds2[i],rgdBounds1)==1){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Finds the angular difference in initial bearings along paths from point0 to point1 and point0 and point12
	 * @param dLat0 Latitude of initial point
	 * @param dLng0 Longitude of initial point
	 * @param dLat1 Latitude of first destination point
	 * @param dLng1 Longitude of first destiantion point
	 * @param dLat2 Latitude of second destiation point
	 * @param dLng2 Longitude of second destination point
	 * @return Angular difference in initial paths (in radians)
	 */
	private double findAngularDifference(double dLat0, double dLng0, double dLat1, double dLng1, double dLat2, double dLng2){
		
		//dTheta02 = initial bearing from point0 to point2
		//dTheta01 = initial bearing from point0 to point1
		//d2Pi = 2*pi
		
		double dTheta01; double dTheta02; double d2Pi;
		
		//loading 2*pi
		d2Pi = 2.*Math.PI;
		
		//loading initial bearings
		dTheta02 = DEG_TO_RAD*findInitialBearing(dLat0, dLng0, dLat2, dLng2);
		dTheta01 = DEG_TO_RAD*findInitialBearing(dLat0, dLng0, dLat1, dLng1);
		
		//correcting initial bearing between point0 and point2
		if(dTheta02<0){
			do{
				dTheta02 = dTheta02+d2Pi;
			}while(dTheta02<0);
		}else if(dTheta02>d2Pi){
			do{
				dTheta02 = dTheta02-d2Pi;
			}while(dTheta02>d2Pi);	
		}
		
		//correcting initial bearing between point0 and point1
		if(dTheta01<dTheta02){
			do{
				dTheta01 = dTheta01+d2Pi;
			}while(dTheta01<dTheta02);
		}else if(dTheta01-d2Pi>dTheta02){
			do{
				dTheta01 = dTheta01-d2Pi;
			}while(dTheta01-d2Pi>dTheta02);
		}
		
		//outputting difference
		return dTheta01-dTheta02;
	}

	/**
	 * Finds the area of the region within the given bounds.
	 * @param rgdBounds Bounds within which to find area.
	 * @return Area.
	 */
	public double findAreaBounds(double rgdBounds[]){
		
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
	
		double rgd1[];
		double d3; double d4;
		
		//loading rgd1
		rgd1 = new double[4];
		rgd1[0] = DEG_TO_RAD*rgdBounds[2];
		rgd1[1] = DEG_TO_RAD*rgdBounds[3];
		rgd1[2] = DEG_TO_RAD*rgdBounds[0];
		rgd1[3] = DEG_TO_RAD*rgdBounds[1];
		
		//loading d3 and d4
		d3 = Math.cos(rgd1[0]+Math.PI/2.);
		d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
		//returning result
		return Math.abs(EARTH_RADIUS*EARTH_RADIUS*(rgd1[3]-rgd1[2])*d4);
	}

	/**
	 * Finds the area of a rectangular cell with given center, width, and height
	 * @param dLat Latitude of center of cell
	 * @param dLng Longitude of center of cell
	 * @param dHeight Height of cell (in degrees)
	 * @param dWidth Width of cell (in degrees)
	 * @return Area of cell in (in km^2)
	 */
	public double findAreaCell(double dLat, double dLng, double dWidth, double dHeight){
		
		//d1 = length (in km) of first base of cell
		//d2 = length (in km) of second base of cell
		//dHeight2 = height divided by 2
		//dWidth2 = width divided by 2
		
		double d1; double d2; double dHeight2; double dWidth2;
		
		//loading height and width divided by 2	
		dHeight2 = dHeight/2.;
		dWidth2 = dWidth/2.;
		
		//loading lengths of bases of cell
		if(dLat-dHeight2>-90){
			d1 = findDistance(dLat-dHeight2, dLng-dWidth2, dLat-dHeight2, dLng+dWidth2);
		}else{
			d1=0;
		}
		if(dLat+dHeight2<90){
			d2 = findDistance(dLat+dHeight2, dLng-dWidth2, dLat+dHeight2, dLng+dWidth2);
		}else{
			d2 = 0;
		}
			
		//finding area
		return LAT_DISTANCE*dHeight*(d1+d2)/2.;
	}

	/**
	 * Finds the unsigned area of the spherical triangle bounded by the given edge.   Top of triangle is given by edge.  Vertex of triangle is given by south pole.
	 * @param edg1 Edge bounding triangle
	 * @return Area of edge
	 */
	public double findAreaSphericalTriangle0(Edge edg1){
		
		//dLatStart = starting latitude in radians
		//dLonStart = starting longitude in radians
		//dLatEnd = ending latitude in radians
		//dLonEnd = ending longitude in radians
		//dOut = output
		
		double dOut; double dLatStart; double dLonStart; double dLatEnd; double dLonEnd;
		
		//loading starting and ending latitudes in radians
		dLatStart = DEG_TO_RAD*edg1.dLatStart;
		dLonStart = DEG_TO_RAD*edg1.dLonStart;
		dLatEnd = DEG_TO_RAD*edg1.dLatEnd;
		dLonEnd = DEG_TO_RAD*edg1.dLonEnd;
		
		//outputting result
		//dOut = 0.5*EARTH_RADIUS_SQUARED*(dLonEnd-dLonStart)*(2.+Math.sin(dLatStart)+Math.sin(dLatEnd));
		dOut = EARTH_RADIUS_SQUARED*(dLonEnd-dLonStart)*(1.-0.5*Math.sin(dLatStart)-0.5*Math.sin(dLatEnd));
		return Math.abs(dOut);
	}
	
	/**
	 * Finds the unsigned area of the spherical triangle bounded by the given edge.   Top of triangle is given by edge.  Vertex of triangle is given by south pole.
	 * @param edg1 Edge bounding triangle
	 * @return Area of edge
	 */
	public double findAreaSphericalTriangle(Edge edg1){
		
		//d1 = distance from first vertex to north pole
		//d2 = distance from second vertex to north pole
		//d3 = length of edge
		//dS = semiperimeter
		//dE = spherical excess
		
		double d1; double d2; double d3; double dS; double dE;
		
		//checking if longitudes are the same
		if(edg1.dLonStart==edg1.dLonEnd){
			return 0.;
		}
		
		//loading distances
		d1 = DEG_TO_RAD*(90.-edg1.dLatStart);
		d2 = DEG_TO_RAD*(90.-edg1.dLatEnd);
		d3 = edg1.dLength/EARTH_RADIUS;
		
		//loading semiperimeter
		dS = 0.5*(d1+d2+d3);
		
		//loading output
		dE = Math.tan(0.5*dS);
		//System.out.println(Math.tan(0.5*dS));
		dE*=Math.tan(0.5*(dS-d1));
		//System.out.println(0.5*(dS-d1));
		dE*=Math.tan(0.5*(dS-d2));
		//System.out.println(0.5*(dS-d2));
		dE*=Math.tan(0.5*(dS-d3));
		//System.out.println(0.5*(dS-d3));
		if(dE<0){
			System.out.println("ERROR: findAreaSphericalTriangle");
		}
		dE = Math.sqrt(dE);
		dE = Math.atan(dE);
		dE = 4.*dE;
		
		//returning result
		return EARTH_RADIUS_SQUARED*dE;
	}

	/**
	 * Finds width (in degrees) of bounding box ith the given area and min and max latitude
	 * @param dArea Target area (in square km)
	 * @param dLatMin Minimum latitude.
	 * @param dLatMax Maximum latitude
	 * @return Width (in degrees) of bounding box
	 */
	public double findBoundsWidth(double dArea, double dLatMin, double dLatMax){
		
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
	
		double rgd1[];
		double d3; double d4;
		
		//loading rgd1
		rgd1 = new double[4];
		rgd1[0] = DEG_TO_RAD*dLatMin;
		rgd1[1] = DEG_TO_RAD*dLatMax;
		
		//loading d3 and d4
		d3 = Math.cos(rgd1[0]+Math.PI/2.);
		d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
		//returning result
		return RAD_TO_DEG*Math.abs(dArea/(EARTH_RADIUS*EARTH_RADIUS*d4));
	}

	/**
	 * Finds the area of the specified spheircal cap
	 * @param dRadius Radius (on earth) of spherical cap
	 * @return Area in square kilometers of spherical cap
	 */
	public double findCapArea(double dRadius){
		return 2*Math.PI*EARTH_RADIUS*EARTH_RADIUS*(1.-Math.cos(dRadius/EARTH_RADIUS));
	}

	/**
	 * Finds the destination point from given starting point at given bearing and distance.
	 * @param dLat1 Latitude of starting point.
	 * @param dLng1 Longitude of starting point.
	 * @param dDirection Direction of travel (in radians).
	 * @param dDistance Distance of travel (in km).
	 * @return (Latitude, Longitude) of destination point.
	 */
	public double[] findDestination(double dLat1, double dLng1, double dDirection, double dDistance){
		 	 
		 //rgd1 = output; first entry with latitude, second entry with longitude (in degrees)
		 //dLat1R = latitude 1 converted to radians
		 //dLng1R = longitude 1 converted to radians
		 //dLat1R = latitude 1 converted to radians
		 
		 double dLng1R; double dLat1R;
		 double rgd1[];
		 
		 //initializing output
		 rgd1 = new double[2];
		 
		 //converting to radians
		 dLat1R = dLat1*DEG_TO_RAD;
		 dLng1R = dLng1*DEG_TO_RAD;
		 
		 //loading result
		 rgd1[0] = Math.asin( Math.sin(dLat1R)*Math.cos(dDistance/EARTH_RADIUS) + Math.cos(dLat1R)*Math.sin(dDistance/EARTH_RADIUS)*Math.cos(dDirection) );
		 rgd1[1] = dLng1R + Math.atan2(Math.sin(dDirection)*Math.sin(dDistance/EARTH_RADIUS)*Math.cos(dLat1R), Math.cos(dDistance/EARTH_RADIUS)-Math.sin(dLat1R)*Math.sin(rgd1[0]));
		 rgd1[0] = rgd1[0]*RAD_TO_DEG;
		 rgd1[1] = rgd1[1]*RAD_TO_DEG;
		 
		 //correcting longitude if crosses 180W
		 if(Math.abs(rgd1[1])>180.){
			 if(rgd1[1]<0){
				rgd1[1] = 180. + rgd1[1] + 180.; 
			 }else{
				rgd1[1] = -180. + rgd1[1] -180.;
			 }
		 }
		 
		 //outputting result
		 return rgd1;
	 }

	/**
	 * Finds the distance between a pair of points.
	 * @param dLat1 Latitude of first point.
	 * @param dLng1 Longitude of first point.
	 * @param dLat2 Latitude of second point.
	 * @param dLng2 Longitude of second point.
	 * @return Distance between points.
	 */
	public double findDistance(double dLat1, double dLng1, double dLat2, double dLng2){
		  
		 double earthRadius = EARTH_RADIUS;
		 double dLat = Math.toRadians(dLat2-dLat1); 
		 double dLng = Math.toRadians(dLng2-dLng1); 
		 double a = Math.sin(dLat/2.) * Math.sin(dLat/2.) + Math.cos(Math.toRadians(dLat1)) * Math.cos(Math.toRadians(dLat2)) * Math.sin(dLng/2.) * Math.sin(dLng/2.);
		 double c = 2. * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		 double dist = earthRadius * c;
		 return dist;
	}

	/**
	 * Finds shortest distance from specified point to edge
	 * @param dLat Latitude of point.
	 * @param dLng Longitude of point.
	 * @param edg1 Edge
	 * @return Distance to edge
	 */
	public double findDistanceToEdge(double dLat, double dLng, Edge edg1){
			
		//rgd1 = first entry with cross track distance between great circle joining vertices and dLat,dLng; second entry gives along track distance
		 //d1 = shortest distance from arc segment to dLat,dLng
		 //d2 = angular difference
		
		 double d1=0; double d2;
		 double rgd1[];
		 
		 //checking if adjacent edge
		 if(dLat==edg1.dLatStart && dLng==edg1.dLonStart){
			 return 0;
		 }
		 if(dLat==edg1.dLatEnd && dLng==edg1.dLonEnd){
			 return 0;
		 }
		 
		 //computing angular difference
		 d2 = findAngularDifference(edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd, dLat, dLng);
		                                                                                                                                                                                                                                                                 
		 //computing distance from first vertex to point
		 d1 = findDistance(dLat, dLng, edg1.dLatStart, edg1.dLonStart);
		 
		 //checking angular difference
		 if(d2>Math.PI/2. && d2<3.*Math.PI/2.){
			 
		 }else{
			 
			 //computing along track and cross track distances
			 rgd1 = findTrackDistance(d1, d2);
				
			 //checking whether along track distance is greater than distance between vertex
			 if(rgd1[1]>edg1.dLength){
				 
				 //checking distance from second vertex to point
				 d1 = findDistance(dLat, dLng, edg1.dLatEnd, edg1.dLonEnd);
			 }else{
				 
				 //loading distance
				 d1 = rgd1[0];
			 } 
		 }
		 	
		 //returning result
		 return d1;
	 }

	/**
	 * Finds shortest and longest distance from specified point to polygon
	 * @param dLat Latitude of point.
	 * @param dLng Longitude of point.
	 * @param ply1 Polygon.
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @return Array with minimum (0) and maximum (1) distance to polygon
	 */
	public double[] findDistanceToPolygon(double dLat, double dLng, Polygon ply1, String sAlgorithm){
			
		 //dMinDistance = minimum distance
		 //dMaxDistance = maximum distance
		 //rgd1 = first entry with cross track distance between great circle joining vertices and dLat,dLng; second entry gives along track distance
		 //d1 = shortest distance from arc segment to dLat,dLng
		 //d2 = angular difference
		 //edg1 = current edge
		 //rgdOut = output
		
		 double d1=0; double d2; double dMinDistance; double dMaxDistance;
		 double rgd1[]; double rgdOut[];
		 Edge edg1;
		 
		 //initializing output
		 rgdOut = new double[2];
		 
		 //checking if dLat,dLng is within polygon
		 if(isPointPolygon(dLat, dLng, ply1, sAlgorithm)==1){
			 return rgdOut;
		 }
	
		 //initializing minimum and maximum distance
		 dMinDistance = 99999999999999999999999999.;
		 dMaxDistance = -99999999999999999999999999.;
		 
		 //looping through edges
		 for(int i=0;i<ply1.getEdgeCount();i++){
			 
			 //loading current edge
			 edg1 = ply1.getEdge(i);
			 
			 //computing angular difference
			 d2 = findAngularDifference(edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd, dLat, dLng);
			                                                                                                                                                                                                                                                                 
			 //computing distance from first vertex to point
			 d1 = findDistance(dLat, dLng, edg1.dLatStart, edg1.dLonStart);
			 
			 //checking angular difference
			 if(d2>Math.PI/2. && d2<3.*Math.PI/2.){
				 
				 //checking distance from first vertex to point
				 if(d1<dMinDistance){
					 dMinDistance=d1;
				 }
				 
				 //checking distance from first vertex to point
				 if(d1>dMaxDistance){
					 dMaxDistance=d1;
				 }
			 }else{
				 
				 //computing along track and cross track distances
				 rgd1 = findTrackDistance(d1, d2);
					
				 //checking whether along track distance is greater than distance between vertex
				 if(rgd1[1]>edg1.dLength){
					 
					 //checking distance from second vertex to point
					 d1 = findDistance(dLat, dLng, edg1.dLatEnd, edg1.dLonEnd);
					 if(d1<dMinDistance){
						 dMinDistance = d1;
					 }
					 if(d1>dMaxDistance){
						 dMaxDistance = d1;
					 }
				 }else{
					 
					 //checking cross track distance
					 if(rgd1[0]<dMinDistance){
						 dMinDistance = rgd1[0];
					 }
					 if(rgd1[0]>dMaxDistance){
						 dMaxDistance = rgd1[0];
					 }
				 } 
			 }
		 }
		 	
		 //returning result
		 rgdOut[0]=dMinDistance;
		 rgdOut[1]=dMaxDistance;
		 return rgdOut;
	 }

	/**
	 * Finds initial bearing of great circle path between two points.
	 * @param dLat0 Latitude of starting point.
	 * @param dLng0 Longitude of starting point.
	 * @param dLat1 Latitude of ending point.
	 * @param dLng1 Longitude of ending point.
	 * @return Bearing in degrees.
	 */
	public double findInitialBearing (double dLat0, double dLng0, double dLat1, double dLng1){
		 
		 //d1 = difference between longitudes
		 
		 double d1;
		 
		 //loading d1
		 d1 = dLng1 - dLng0;
		 d1 = d1*DEG_TO_RAD;
		 
		 //outputting result
		 return RAD_TO_DEG*Math.atan2(Math.sin(d1)*Math.cos(DEG_TO_RAD*dLat1), Math.cos(DEG_TO_RAD*dLat0)*Math.sin(DEG_TO_RAD*dLat1)-Math.sin(DEG_TO_RAD*dLat0)*Math.cos(DEG_TO_RAD*dLat1)*Math.cos(d1));
	 }

	/**
	 * Finds the latitude at which the great circle connecting two points intersects a given meridian
	 * @param dLat1 Latitude of first point (in radians).
	 * @param dLng1 Longitude of first point (in radians).
	 * @param dLat2 Latitude of second point (in radians).
	 * @param dLng2 Longitude of second point (in radians).
	 * @param dLng Meridian being checked (in radians).
	 * @return Latitude of intersection.
	 */
	 public double findLatitude(double dLat1, double dLng1, double dLat2, double dLng2, double dLng){
		 
		 //d1 = output
		 
		 double d1;
		 
		 //computing intersection
		 d1 = Math.atan((Math.tan(dLat1)*Math.sin(dLng - dLng2) - Math.tan(dLat2)*Math.sin(dLng - dLng1)) / Math.sin(dLng1 - dLng2));
		 
		 //outputting result
		 return d1;
	 }

	/**
	 * Finds the latitude at which the great circle connecting two points intersects a given meridian
	 * @param dLat1 Latitude of first point (in degrees).
	 * @param dLng1 Longitude of first point (in degrees).
	 * @param dLat2 Latitude of second point (in degrees).
	 * @param dLng2 Longitude of second point (in degrees).
	 * @param dLng Meridian being checked (in degrees).
	 * @return Latitude of intersection.
	 */
	 public double findLatitudeDegree(double dLat1, double dLng1, double dLat2, double dLng2, double dLng){
		 
		 //d1 = output
		 
		 double d1;
		 
		 //computing intersection
		 d1 = Math.atan((Math.tan(dLat1*DEG_TO_RAD)*Math.sin(dLng*DEG_TO_RAD - dLng2*DEG_TO_RAD) - Math.tan(dLat2*DEG_TO_RAD)*Math.sin(dLng*DEG_TO_RAD - dLng1*DEG_TO_RAD)) / Math.sin(dLng1*DEG_TO_RAD - dLng2*DEG_TO_RAD));
		 
		 //outputting result
		 return d1*RAD_TO_DEG;
	 }

	/**
	  * Finds minimum distance between edge and point.
	  * @param egd1 Edge of interest.
	  * @param dLat Latitude of point.
	  * @param dLon Longitude of point.
	  * @return Minimum distance.
	  */
	 public double findMinimumDistance(Edge edg1, double dLat, double dLng){
		 
		 //d2 = angular difference
		 //rgd1 = first entry with cross track distance between great circle joining vertices and dLat,dLng; second entry gives along track distance
		 
		 double rgd1[];
		 double d2; double d1;
		 
		 //computing angular difference
		 d2 = findAngularDifference(edg1.dLatStart, edg1.dLonStart, edg1.dLatEnd, edg1.dLonEnd, dLat, dLng);
		                                                                                                                                                                                                                                                                 
		 //computing distance from first vertex to point
		 d1 = findDistance(dLat, dLng, edg1.dLatStart, edg1.dLonStart);
		 
		 //checking angular difference
		 if(d2>Math.PI/2. && d2<3.*Math.PI/2.){
			 
			 return d1;
			 
		 }else{
			 
			 //computing along track and cross track distances
			 rgd1 = findTrackDistance(d1, d2);
				
			 //checking whether along track distance is greater than distance between vertex
			 if(rgd1[1]>edg1.dLength){
				 
				 //checking distance from second vertex to point
				 return findDistance(dLat, dLng, edg1.dLatEnd, edg1.dLonEnd);
			 }else{
				 
				 //checking cross track distance
				 return rgd1[0];
			 } 
		 }
		 
	 }

	/**
	 * Finds a random (uniformly distributed) point on a sphere.
	 * @return (Latitude,Longitude) of point.
	 */
	public double[] findRandomPoint(){
		
		//du = first random number
		//dv = second random number
		//rgd1  = output
		
		double du; double dv;
		double rgd1[];
		
		//loading random numbers
		du = Math.random(); dv = Math.random();
		
		//initializing output
		rgd1 = new double[2];
		
		//loading output
		rgd1[1] = RAD_TO_DEG*2.*Math.PI*du - 180.;
		rgd1[0] = RAD_TO_DEG*Math.acos(2*dv-1.) - 90.;
		
		//outputting results
		return rgd1;
	}

	/**
	 * Finds the length of the given edge that is within the specified bounds.
	 * @param edg1 Edge of interest.
	 * @param rgdBounds Bounds.
	 * @return Length of edge within bounds (0 if edge is entirely out of bounds)
	 */
	public double findShortEdgeLengthInBounds(Edge edg1, double[] rgdBounds){
		
		//bStart = true if start is within bounds, false otherwise
		//bEnd = true if end is within bounds, false otherwise
		//dLat0 = starting latitude
		//dLon0 = starting longitude
		//dLatDelta = latitude difference
		//dLonDelta = longitude difference
		//dLat = current latitude
		//dLon = current longitude
		//d1 = current proportion offset
		
		double dLat = 0; double dLat0; double dLatDelta; double dLon = 0; double dLon0; double dLonDelta; double d1;
		boolean bStart; boolean bEnd;
		
		//loading start values
		if(this.checkPointInBounds(edg1.dLatStart, edg1.dLonStart, rgdBounds)==1){
			bStart=true;
		}else{
			bStart=false;
		}
		if(this.checkPointInBounds(edg1.dLatEnd, edg1.dLonEnd, rgdBounds)==1){
			bEnd=true;
		}else{
			bEnd=false;
		}
		
		//checking for early exit
		if(bStart==false && bEnd==false){
			return 0.;
		}else if(bStart==true && bEnd==true){
			return edg1.dLength;
		}
			
		//loading starting values and differences
		if(bStart==true){
			dLat0 = edg1.dLatStart;
			dLon0 = edg1.dLonStart;
			dLatDelta = edg1.dLatEnd-dLat0;
			dLonDelta = edg1.dLonEnd-dLon0;
		}else{
			dLat0 = edg1.dLatEnd;
			dLon0 = edg1.dLonEnd;
			dLatDelta = edg1.dLatStart-dLat0;
			dLonDelta = edg1.dLonStart-dLon0;
		}
			
		//initializing proportion of offset
		d1 = 0.5;
		
		//looping through points
		for(int i=0;i<5;i++){
			
			//loading current latitude and longitude
			dLat = dLat0 + d1*dLatDelta;
			dLon = dLon0 + d1*dLonDelta;
			
			if(this.checkPointInBounds(dLat, dLon, rgdBounds)==1){
				d1+=Math.pow(0.5,(double) (i+2));
			}else{
				d1-=Math.pow(0.5,(double) (i+2));
			}
		}
			
		//outputting result
		return this.findDistance(dLat0, dLon0, dLat, dLon);
	}

	/**
	 * Finds the cross-track and along-track distances.
	 * @param d02 Distance along path.
	 * @param dAngleDiff Initial angular difference in paths (in radians) .
	 * @return Entry 0 with cross-track distance, entry 1 with along-track distance (in km).
	 */
	public double[] findTrackDistance(double dD02, double dAngleDiff){
		 
		 //dD02 = distance from start point to third point
		 //dAngleDiff = difference in initial bearings (in radians)
		 //rgd1 = output; entry 0 with cross track distance, entry 1 with along track distance
		
		 double rgd1[];
		 
		 //initializing output
		 rgd1 = new double[2];
		 
		 //returning cross track distance
		 rgd1[0]= Math.asin(Math.sin(dD02/EARTH_RADIUS)*Math.sin(dAngleDiff))*EARTH_RADIUS;
		 
		 //returning along track distance
		 rgd1[1] = Math.acos(Math.cos(dD02/EARTH_RADIUS)/Math.cos(rgd1[0]/EARTH_RADIUS)) * EARTH_RADIUS;
		 rgd1[1] = Math.abs(rgd1[1]);
		 
		 //correcting negative signs
		 rgd1[0]=Math.abs(rgd1[0]);
		 
		 //returning result
		 return rgd1;
	 }

	/**
	 * Checks whether point is in given polygon.
	 * Assumes that dsouth pole is not within polygon.
	 * @param dLat Latitude of test point.
	 * @param dLng Longitude of test point.
	 * @param ply1 Polygon
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @return 0 if point is outside of polygon; 1 if it is within polygon.
	 */
	public int isPointPolygon(double dLat, double dLng, Polygon ply1, String sAlgorithm){
		 
		 //i1 = output
		 //i2 = count of intersections
		 //dX1 = current first longitude
		 //dY1 = current first latitude
		 //dX2 = current second longitude
		 //dY2 = current second latitude
		 //dXR = longitude in radians
		 //dYR = latitude in radians
		 //lst1 = list of edges bounding point	
		
		 ArrayList<Edge> lst1;
		 int i1=0; int i2=0;
		 double dX1; double dY1; double dX2; double dY2; double dXR; double dYR;
		 
		 //checking if within bounds
		 if(dLng<ply1.dLonMinimum){
			 return 0;
		 }
		 if(dLng>ply1.dLonMaximum){
			 return 0;
		 }
		 //if(dLat<ply1.dLatMinimum){
		 //	 return 0;
		 //}
		 //if(dLat>ply1.dLatMaximum){
		 //	 return 0;
		 //}
		 
		 //loading list of edges intersecting dLng
		 //dLng=136;
		 lst1 = ply1.getIntersectingEdgesLongitude(dLng);
		 
		 //looping though edges intersecting dLng
		 if(lst1!=null){
		 	for(int i=0;i<lst1.size();i++){
				 
				 //checking if latitudes allow for intersection
				 if(lst1.get(i).getLatMinimum()>dLat){
					 continue;
				 }
				 
				 //checking if latitudes imply intersection
			 	 if(lst1.get(i).getLatMaximum()<dLat){
					 if(sAlgorithm.equals("even-odd")){
			 		 	i2++;
					 }else if(sAlgorithm.equals("winding")){
						i2+=lst1.get(i).iWinding;
					 }
			 		 continue;
				 }
				 
				 //loading current latitude and longitude
				 dX1 = lst1.get(i).dLonStart;
				 dY1 = lst1.get(i).dLatStart;
				 dX2 = lst1.get(i).dLonEnd;
				 dY2 = lst1.get(i).dLatEnd;
				 //*************************
				 //System.out.println(dX1);
				 //System.out.println(dY1);
				 //System.out.println(dX2);
				 //System.out.println(dY2);
				 //*************************
				 
				 //converting to radians
				 dX1=dX1*DEG_TO_RAD;
				 dY1=dY1*DEG_TO_RAD;
				 dX2=dX2*DEG_TO_RAD;
				 dY2=dY2*DEG_TO_RAD;
				 
				 //loading dXR and DYR
				 dXR = dLng*DEG_TO_RAD;
				 dYR = dLat*DEG_TO_RAD;
				 
				 //checking for intersection
				 //************************
				 //System.out.println(findLatitude(dY1,dX1,dY2,dX2,dXR)*RAD_TO_DEG);
				 //************************
				 if(findLatitude(dY1,dX1,dY2,dX2,dXR)<=dYR){
					 if(sAlgorithm.equals("even-odd")){
			 		 	i2++;
					 }else if(sAlgorithm.equals("winding")){
						i2+=lst1.get(i).iWinding;
					 }
				 }
			 }
		 }
	
		 //finding result
		 if(sAlgorithm.equals("even-odd")){
			 if(i2 % 2 ==1){
				 i1=1;
			 }
		 }else if(sAlgorithm.equals("winding")){
			 if(i2!=0){
				 i1=1;
			 }
		 }
		 
		 //outputting result
		 return i1; 
	 }
	
	/**
	 * Checks whether point is in given polygon.
	 * Assumes that dsouth pole is not within polygon.
	 * @param dLat Latitude of test point.
	 * @param dLng Longitude of test point.
	 * @param rgdPolygon Polygon.
	 * @param rgdBounds Bounding box for polygon.
	 * @return 0 if point is outside of polygon; 1 if it is within polygon.
	 */
	public int isPointPolygonEvenOdd(double dLat, double dLng, double rgdPolygon[][], double rgdBounds[]){
		 
		 //i1 = output
		 //i2 = count of intersections
		 //dX1 = current first longitude
		 //dY1 = current first latitude
		 //dX2 = current second longitude
		 //dY2 = current second latitude
		 //b1 = indicator for whether longitude and latitude bounding passed: 0 if failed, 1 if passed, 2 if passed and definite intersection
		 //dXR = longitude in radians
		 //dYR = latitude in radians
		 
		 int i1=0; int i2=0;
		 int i;
		 double dX1; double dY1; double dX2; double dY2; double dXR; double dYR;
		 byte b1;
		 
		 //checking if within bounds
		 if(dLng<rgdBounds[0]){
			 return 0;
		 }
		 if(dLng>rgdBounds[1]){
			 return 0;
		 }
		 if(dLat<rgdBounds[2]){
			 return 0;
		 }
		 if(dLat>rgdBounds[3]){
			 return 0;
		 }
		 
		 //loading dXR and dYR
		 dXR = dLng*DEG_TO_RAD;
		 dYR = dLat*DEG_TO_RAD;
		 
		 //looping through points in polygon
		 for(i=0;i<rgdPolygon.length-1;i++){
			 
			 //checking if same polygon
			 if(rgdPolygon[i][2]==rgdPolygon[i+1][2]){
			 
				 //loading current latitude and longitude
				 dX1 = rgdPolygon[i][0]; dY1 = rgdPolygon[i][1];
				 dX2 = rgdPolygon[i+1][0]; dY2 = rgdPolygon[i+1][1];
			 
				 //initializing b1
				 b1=1;
				 
				 //checking if longitudes of segment bound longitude of point
				 if(Math.abs(dX1-dX2)>180){
					 if(dX1>dX2){
						 if(dX2<=dLng && dLng<=dLng){
							 b1=0;
						 }
					 }else{
						 if(dX1<=dLng && dLng<=dX2){
							 b1=0;
						 }
					 }
				 }else{
					 if(dX2<dX1){
						if(dLng<=dX2 || dX1<=dLng){
							b1=0;
						}
					 }else{
						 if(dLng<=dX1 || dX2<=dLng)
							b1=0;
					 }
				 }
				 
				 //checking if latitudes imply intersection
				 if(b1==1){
				 	if(dY1<=0 && dY2<=0 && dLat>=0){
						 b1=2;
					 }
				 }
				 	
				 //checking if latitudes allow for intersection
				 if(dY1>0 && dY2>0 && dLat<0){
					 b1=0;
				 }
				 
				 //checking if necessary to perform trig calculations
				 if(b1==2){
					 i2++;
				 }else if(b1==1){
					 
					 //converting to radians
					 dX1=dX1*DEG_TO_RAD;
					 dY1=dY1*DEG_TO_RAD;
					 dX2=dX2*DEG_TO_RAD;
					 dY2=dY2*DEG_TO_RAD;
					 
					 //checking for intersection
					 if(findLatitude(dY1,dX1,dY2,dX2,dXR)<=dYR){
						 i2++;
					 }
				 }
			 }
		 }
	
		 //finding result
		 if(i2 % 2 ==1){
			 i1=1;
		 }
		 
		 //outputting result
		 return i1; 
	 }

	/**
	 * Checks whether point is in given polygon.
	 * Assumes that dsouth pole is not within polygon.
	 * @param dLat Latitude of test point.
	 * @param dLng Longitude of test point.
	 * @param ply1 Polygon
	 * @param iMultiplicity Multiplicity of polygon (e.g., 2 if all edges are double)
	 * @return 0 if point is outside of polygon; 1 if it is within polygon.
	 */
	public int isPointPolygonEvenOdd_TEMP(double dLat, double dLng, Polygon ply1, int iMultiplicity){
		 
		 //i1 = output
		 //i2 = count of intersections
		 //dX1 = current first longitude
		 //dY1 = current first latitude
		 //dX2 = current second longitude
		 //dY2 = current second latitude
		 //dXR = longitude in radians
		 //dYR = latitude in radians
		 //lst1 = list of edges bounding point	
		
		 ArrayList<Edge> lst1;
		 int i1=0; int i2=0;
		 double dX1; double dY1; double dX2; double dY2; double dXR; double dYR;
		 
		 //checking if within bounds
		 if(dLng<ply1.dLonMinimum){
			 return 0;
		 }
		 if(dLng>ply1.dLonMaximum){
			 return 0;
		 }
		 if(dLat<ply1.dLatMinimum){
			 return 0;
		 }
		 if(dLat>ply1.dLatMaximum){
			 return 0;
		 }
		 
		 //loading list of edges intersecting dLng
		 //dLng=136;
		 lst1 = ply1.getIntersectingEdgesLongitude(dLng);
		 
		 //looping though edges intersecting dLng
		 if(lst1!=null){
		 	for(int i=0;i<lst1.size();i++){
				 
				 //checking if latitudes allow for intersection
				 if(lst1.get(i).getLatMinimum()>dLat){
					 continue;
				 }
				 
				 //checking if latitudes imply intersection
			 	 if(lst1.get(i).getLatMaximum()<dLat){
					 i2++;
					 continue;
				 }
				 
				 //loading current latitude and longitude
				 dX1 = lst1.get(i).dLonStart;
				 dY1 = lst1.get(i).dLatStart;
				 dX2 = lst1.get(i).dLonEnd;
				 dY2 = lst1.get(i).dLatEnd;
				  
				 //converting to radians
				 dX1=dX1*DEG_TO_RAD;
				 dY1=dY1*DEG_TO_RAD;
				 dX2=dX2*DEG_TO_RAD;
				 dY2=dY2*DEG_TO_RAD;
				 
				 //loading dXR and DYR
				 dXR = dLng*DEG_TO_RAD;
				 dYR = dLat*DEG_TO_RAD;
				 
				 //checking for intersection
				 if(findLatitude(dY1,dX1,dY2,dX2,dXR)<=dYR){
					 i2++;
				 }
			 }
		 }
	
		 //finding result
		 if(iMultiplicity==1){
			 if(i2 % 2 ==1){
					i1=1;
			}
		 }else{ 
			 if((i2/iMultiplicity) % 2 ==1){
				 i1=1;
			 }
		 }
		 
		 //outputting result
		 return i1; 
	 }
	
	
	
	
}

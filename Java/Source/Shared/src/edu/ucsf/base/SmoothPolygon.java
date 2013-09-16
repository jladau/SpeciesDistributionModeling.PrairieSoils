package edu.ucsf.base;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs range smoothing.
 * @author jladau
 */


public class SmoothPolygon {

	//sph1 = SphericalGeometry object
	//mapMeridianDistance(latitude) = returns distance dRadius between meridians in degrees at given latitude
	//dResolution = resolution for smoothing
	//dRadius = radius for smoothing
	//map1(pntVertex) = returns true if vertex is in parallel set, false otherwise
	//map2(lin) = returns crossing point of boundary of parallel set of given edge (line)
	//plyUnsmoothed = initial polygon
	//plySmoothed = smoothed polygon
	//lstUsedBoxes = list of used boxes (identified by UL corners)
	//dRound = number of digits for rounding
	//lstDoubleCounts = list of boxes with double edges
	//mapMinimumDistance(sLatitude,sLongitude) = returns minimum distance from polygon to specified location
	
	private ArrayList<Point2D.Double> lstDoubleCounts;
	private SphericalGeometry sph1;
	private Map<Double,Double> mapMeridianDistance;
	private double dResolution;
	private double dRadius;
	private Map<Point2D.Double,Boolean> map1; 
	private Map<Line2D.Double,Point2D.Double> map2;
	private Polygon plyUnsmoothed;
	private Polygon plySmoothed;
	private ArrayList<Point2D.Double> lstUsedBoxes;
	private double dRound;
	public HashMap<String,Double> mapMinimumDistance;
	
	private static final double LAT_DIST = 111.5;
	
	/**
	 * Constructor
	 * @param dResolution Resolution for smoothing (in degrees)
	 * @param dRadius Radius for smoothing
	 * @param ply1 Polygon
	 * @param mapMinimumDistance Returns minimum distance from polygon to specified location
	 */
	public SmoothPolygon(double dRadius, double dResolution, Polygon ply1, HashMap<String,Double> mapMinimumDistance){
		
		//dLat = current latitude
		
		double dLat;
		
		//loading number of digits for rounding
		this.dRound = 1000000.;
		
		//saving polygon
		this.plyUnsmoothed = ply1;
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//initializing mapMeridianDistance
		mapMeridianDistance = new HashMap<Double,Double>();
		
		//loading radius and resolution
		this.dResolution = dResolution;
		this.dRadius = dRadius;
		
		//looping through latitudes
		dLat = 90.-dResolution/2;
		while(dLat>=-90){
			
			//updating meridian distance
			mapMeridianDistance.put(dLat, dRadius/sph1.findDistance(dLat, 0, dLat, 1));
			
			//updating dLat
			dLat-=dResolution;
		}
		
		//initializing maps of counts of times boxes with two edges have been used
		lstDoubleCounts = new ArrayList<Point2D.Double>(100);
		
		//initializing distance map
		if(mapMinimumDistance==null){
			this.mapMinimumDistance = new HashMap<String,Double>((int) (360.*720./(dResolution*dResolution)));
		}else{
			this.mapMinimumDistance=mapMinimumDistance;
		}
	}
	
	/**
	 * Finds the point at which the boundary of exterior parallel set crosses the line between the given points
	 * @param ply1 Polygon
	 * @param pnt1 First point
	 * @param pnt2 Second point
	 * @param bPoint1 True if first point in exterior parallel set; false otherwise
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @return Point of crossing
	 */
	private Point2D.Double findCrossingPoint(Polygon ply1, Point2D.Double pnt1, Point2D.Double pnt2, boolean bPoint1, String sAlgorithm){
		
		//d1 = current proportion of distance
		//i1 = value of current test point
		//dResolution = resolution
		//iValue = 1 if first point is parallel set, 0 otherwise
		//pnt3 = current test point
		
		double d1;
		Point2D.Double pnt3 = null;
		int i1; int iValue;
		
		//initializing d1
		d1 = 0.5;
		
		//initializing iValue
		if(bPoint1 ==true){
			iValue = 1;
		}else{
			iValue = 0;
		}
		
		for(int i=1;i<=5;i++){
			
			//loading current test point
			pnt3 = new Point2D.Double(pnt1.x + d1*(pnt2.x-pnt1.x),pnt1.y + d1*(pnt2.y-pnt1.y));
			
			//finding value of test point
			//i1 = sph1.checkDiskIntersection(pnt3.y, pnt3.x, ply1, dRadius, 1.,sAlgorithm);
			//if(sph1.findDistanceToPolygon(pnt3.y, pnt3.x, ply1, sAlgorithm)[0]<=dRadius){
			if(sph1.checkDiskIntersection(pnt3.y, pnt3.x, ply1, sAlgorithm, dRadius)==true){
				i1=1;
			}else{
				i1=0;
			}
			
			//checking value of i1
			if(i1==iValue){
				d1 = d1 + 1/Math.pow(2.,i+1);
			}else{
				d1 = d1 - 1/Math.pow(2.,i+1);
			}
		}
		
		//returning result
		return pnt3;
	}
	
	/**
	 * Finds the edge beginning at the given starting point through the specified box
	 * @param rgpBox Contains corners of box being searched (UL,LL,LR,UR)
	 * @param dLatEdgeStart Starting latitude of edge
	 * @param dLonEdgeStart Starting longitude of edge
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 * @return Boundary of parallel set passing through specified box.
	 */
	private Edge findEdge(Point2D.Double[] rgpBox, double dLatEdgeStart, double dLonEdgeStart, String sAlgorithm){
		
		//rgb1 = values in corners of current square (order: UL,LL,LR,UR).  True if corner is in disk, false otherwise.
		//lst1(pnt1) = list of current points where crossings occur 
		//pnt1 = current edge crossing point
		
		boolean rgb1[];
		ArrayList<Point2D.Double> lst1;
		Point2D.Double pnt1;
		
		//checking if box has already been used and updating list appropriately
		if(lstUsedBoxes.contains(rgpBox[0])){
			
			//******************************
			//System.out.println("Used box: " + rgpBox[0].x + "," + rgpBox[0].y);
			//******************************
			
			return null;
		}
		
		//************************
		//System.out.println("NOT USED BOX");
		//************************
		
		//checking if corners are in parallel set
		rgb1 = new boolean[4];
		for(int i=0;i<4;i++){
			if(map1.containsKey(rgpBox[i])){
				rgb1[i]=map1.get(rgpBox[i]);
			}else{
				if(sph1.checkDiskIntersection(rgpBox[i].y, rgpBox[i].x, plyUnsmoothed, sAlgorithm, dRadius)==true){
				//if(findMinimumDistance(rgpBox[i].y, rgpBox[i].x, sAlgorithm)<=dRadius){
				//if(sph1.findDistanceToPolygon(rgpBox[i].y, rgpBox[i].x, plyUnsmoothed, sAlgorithm)[0]<=dRadius){
					rgb1[i]=true;
					map1.put(rgpBox[i], true);
				}else{
					rgb1[i]=false;
					map1.put(rgpBox[i], false);
				}
			}
		}
		
		//initializing list of edge crossings
		lst1 = new ArrayList<Point2D.Double>(4);
		
		//looping through edges and finding edges that flip between being interior and exterior to parallel set: finding crossing points for these edges
		for(int i=1;i<4;i++){
			for(int j=0;j<i;j++){
				
				//checking that not diagonal
				if(i-j!=2){
				
					//using xor operator to check for flip
					if((rgb1[i]^rgb1[j])==true){
						
						//checking if crossing point is already known
						if(map2.containsKey(new Line2D.Double(rgpBox[i],rgpBox[j]))){
							
							//loading crossing point
							pnt1 = map2.get(new Line2D.Double(rgpBox[i],rgpBox[j]));
							
						}else{
							if(map2.containsKey(new Line2D.Double(rgpBox[j],rgpBox[i]))){
							
								//loading crossing point
								pnt1 = map2.get(new Line2D.Double(rgpBox[j],rgpBox[i]));
							}else{
								
								//finding location where edge crosses side
								pnt1 = this.findCrossingPoint(plyUnsmoothed, rgpBox[i], rgpBox[j], rgb1[i], sAlgorithm);
								
								//saving point
								map2.put(new Line2D.Double(rgpBox[i],rgpBox[j]),pnt1);
								map2.put(new Line2D.Double(rgpBox[j],rgpBox[i]),pnt1);
							}
						}
						
						//saving point
						lst1.add(pnt1);
					}
				}
			}
		}
		
		
		//else	
		//	//checking map of double counts
		//	if(mapDoubleCounts.containsKey(rgpBox[0]) && mapDoubleCounts.get(rgpBox[0])==1){
		//		
		//	}else{
		//		lstUsedBoxes.add(rgpBox[0]);
		//	}
		//}
		
		//checking number of crossing points
		if(lst1.size()==0){
			return null;
		}else if(lst1.size()==2)	{
			
			//updating used boxes map
			lstUsedBoxes.add(rgpBox[0]);
			
			if(dLatEdgeStart!=-9999){
			
				//loading edge if two crossing points: starting point specified
				if(Math.abs(lst1.get(0).y - dLatEdgeStart)<0.00000001 && Math.abs(lst1.get(0).x - dLonEdgeStart)<0.00000001){
					return new Edge(dLatEdgeStart,lst1.get(1).y,dLonEdgeStart,lst1.get(1).x);
				}else{
					return new Edge(dLatEdgeStart,lst1.get(0).y,dLonEdgeStart,lst1.get(0).x);
				}
			}else{
				return new Edge(lst1.get(0).y,lst1.get(1).y,lst1.get(0).x,lst1.get(1).x);
			}
		}else if(lst1.size()==4){
			
			//checking if initial point
			if(dLatEdgeStart==-9999){
				
				//passing if two of four crossing points have already been used (cannot tell which edge to use)
				if(lstDoubleCounts.contains(rgpBox[0])){
					return null;
				}else{
					lstDoubleCounts.add(rgpBox[0]);
					return new Edge(lst1.get(0).y,lst1.get(1).y,lst1.get(0).x,lst1.get(1).x);
				}
			}else{
			
				//updating double counts
				if(lstDoubleCounts.contains(rgpBox[0])){
					lstUsedBoxes.add(rgpBox[0]);
				}else{
					lstDoubleCounts.add(rgpBox[0]);
				}
				
				//outputting edge
				return findEdgeFourVertices(lst1,dLatEdgeStart,dLonEdgeStart);
			}
		}else{
			System.out.println("ERROR: " + lst1.size() + " crossing points.");
			return null;
		}
	}

	/**
	 * Finds edge that starts at given location, ends at one of the four specified vertices.
	 * @param lst1 List of candidate ending vertices.
	 * @param dLatEdgeStart Starting latitude of edge.
	 * @param dLonEdgeStart Starting longitude of edge
	 * @return Edge starting at given location, ending at one of the given points.
	 */
	private Edge findEdgeFourVertices(ArrayList<Point2D.Double> lst1, double dLatEdgeStart, double dLonEdgeStart){
		
		//i1 = index of starting vertex
		
		int i1=-9999;
		
		//finding index of starting vertex
		for(int i=0;i<4;i++){
			if(Math.abs(lst1.get(i).y - dLatEdgeStart)<0.00000001 && Math.abs(lst1.get(i).x - dLonEdgeStart)<0.00000001){
				i1=i;
				break;
			}
		}
		
		//returning result
		if(i1==0){
			return new Edge(dLatEdgeStart,lst1.get(1).y,dLonEdgeStart,lst1.get(1).x);
		}else if(i1==1){
			return new Edge(dLatEdgeStart,lst1.get(0).y,dLonEdgeStart,lst1.get(0).x);
		}else if(i1==2){
			return new Edge(dLatEdgeStart,lst1.get(3).y,dLonEdgeStart,lst1.get(3).x);
		}else if(i1==3){
			return new Edge(dLatEdgeStart,lst1.get(2).y,dLonEdgeStart,lst1.get(2).x);
		}else{
			System.out.println("ERROR: findEdgeFourVertices");
			return null;
		}
	}

	/**
	 * Finds minimum distance to unsmoothed polygon from given point
	 * @param dLat Latitude of point
	 * @param Lon Longitude of point
	 * @param sAlgorithm Algorithm to use
	 */
	private double findMinimumDistance(double dLat, double dLon, String sAlgorithm){
		
		//sLocation = location to look up
		//d1 = current minimum distance
		
		String sLocation;
		double d1;
		
		//loading location in string format
		sLocation = dLat + "," + dLon;
		
		//*********************
		//System.out.println(sLocation);
		//*********************
		
		//checking if location has already been entered and entering it if not
		if(mapMinimumDistance.containsKey(sLocation)){
			d1 = mapMinimumDistance.get(sLocation);
		}else{
			d1 = sph1.findDistanceToPolygon(dLat, dLon, plyUnsmoothed, sAlgorithm)[0];
			mapMinimumDistance.put(sLocation, d1);
		}
		return d1;
	}
	
	/**
	 * Finds the box adjacent to the current box edge that includes dLat,dLon
	 * @param pntBox Current box
	 * @param dLat Latitude of point
	 * @param dLon Longitude of point
	 * @return Adjacent box (UL,LL,LR,UR)
	 */
	private Point2D.Double[] findNextBox(Point2D.Double[] pntBox, double dLat, double dLon){
	
		//rgp1 = output
		//dLat1 = UL latitude for new box
		//dLon1 = UL longitude for new box
		
		Point2D.Double[] rgp1;
		double dLat1 = 0; double dLon1 = 0;
		
		//loading starting point for new polygon
		if(dLon==pntBox[0].x){
			dLon1 = dLon-dResolution;
			dLat1 = pntBox[0].y;
		}else if(dLon==pntBox[2].x){
			dLon1 = dLon;
			dLat1 = pntBox[0].y;
		}else{
			if(dLat==pntBox[0].y){
				dLat1 = dLat+dResolution;
				dLon1 = pntBox[0].x;
			}else if(dLat==pntBox[1].y){
				dLat1 = dLat;
				dLon1 = pntBox[0].x;
			}else{
				System.out.println("ERROR: findNextBox");
			}
		}
		
		//correcting round-off errors
		dLon1 = round(dLon1,dRound);
		dLat1 = round(dLat1,dRound);
		
		//loading output
		rgp1 = new Point2D.Double[4];
		rgp1[0] = new Point2D.Double(dLon1,dLat1); 
		rgp1[1] = new Point2D.Double(dLon1,round(dLat1-dResolution,dRound)); 
		rgp1[2] = new Point2D.Double(round(dLon1 + dResolution,dRound),round(dLat1-dResolution,dRound)); 
		rgp1[3] = new Point2D.Double(round(dLon1 + dResolution,dRound),dLat1); 
		
		//outputting result
		return rgp1;
	}

	/**
	 * Finds smoothed polygon.
	 * @param bCheckLatLon True if check that latitude is between -90 and 90, longitude is between -180 and 180; false otherwise
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 */
	public void findSmoothedPolygon(boolean bCheckLatLon, String sAlgorithm){
		
		//dLatMin = minimum latitude
		//dLatMax = maximum latitude
		//dLatStart = starting latitude for looping
		//dLatEnd = ending latitude for looping
		//dLonStart = starting longitude for looping
		//dLonEnd = ending longitude for looping
		//dLat = current latitude
		//dLon = current longitude
		//d1 = current maximal meridional distance
		
		double dLatMin; double dLatMax; double dLatStart=-9999; double dLatEnd=-9999; double dLonStart=-9999; double dLonEnd=-9999; double dLat; double dLon; double d1=-9999;
				
		//loading latitude bounds
		dLatMin = plyUnsmoothed.dLatMinimum-dRadius/LAT_DIST;
		if(dLatMin<-90){
			dLatMin = -90;
		}
		dLatMax = plyUnsmoothed.dLatMaximum+dRadius/LAT_DIST;
		if(dLatMax>90){
			dLatMax = 90;
		}
		
		//looping through latitudes
		dLat = 90.-dResolution/2.;
		while(dLat>=-90){
			
			//checking if less than upper bound
			if(dLatMin<=dLat && dLat<=dLatMax){
			
				//updating starting latitude
				if(dLatStart == -9999){
					dLatStart = dLat;
				}
				
				//updating longitude distance
				if(mapMeridianDistance.get(dLat)>d1){
					d1 = mapMeridianDistance.get(dLat);
				}
			}else if(dLat<dLatMin){
				
				 //updating ending latitude
				 dLatEnd = dLat+dResolution; 
				 break;
			}
			
			//updating dLat
			dLat-=dResolution;
		}
		if(dLatStart==-9999){
			dLatStart=90.-dResolution/2.;
		}
		if(dLatEnd==-9999){
			dLatEnd=-90.+dResolution/2.;
		}
		
		//loading meridian start and end
		dLon = -180+dResolution/2.;
		while(dLon<=180){
			
			//updating
			if(plyUnsmoothed.dLonMinimum-d1 <= dLon && dLon<=plyUnsmoothed.dLonMaximum+d1){
				
				//updating minimum longitude
				if(dLonStart==-9999){
					dLonStart = dLon;
				}
			}else if(dLon>plyUnsmoothed.dLonMaximum+d1){
				
				//updating maximum
				dLonEnd = dLon-dResolution;
				break;
			}
			
			//updating longitude
			dLon+=dResolution;
		}
		if(dLonStart==-9999){
			dLonStart=-180+dResolution/2.;
		}
		if(dLonEnd==-9999){
			dLonEnd=180-dResolution/2.;
		}
		
		//correcting round-off errors
		dLonStart = round(dLonStart,dRound);
		dLatStart = round(dLatStart,dRound);
		dLonEnd = round(dLonEnd,dRound);
		dLatEnd = round(dLatEnd,dRound);
		
		//initializing list of used boxes
		lstUsedBoxes = new ArrayList<Point2D.Double>((int) Math.abs(((dLonEnd-dLonStart)*(dLatEnd-dLatStart)/(dResolution*dResolution))));
		
		//loading edges
		loadPolygon(dLatStart,dLatEnd,dLonStart,dLonEnd,bCheckLatLon,sAlgorithm);
	}

	public Polygon getSmoothedPolygon(){
		return plySmoothed;
	}
	
	/**
	 * Loads edges from vertex list, finding intersection points along boundary
	 * @param dLatStart Starting latitude for looping
	 * @param dLatEnd Ending latitude for looping
	 * @param dLonStart Starting longitude for looping
	 * @param dLonEnd Ending longitude for looping
	 * @param bCheckLatLon True if check that latitude is between -90 and 90, longitude is between -180 and 180; false otherwise
	 * @param sAlgorithm "winding" for winding number, "even-odd" for even-odd algorithm
	 */
	private void loadPolygon(double dLatStart, double dLatEnd, double dLonStart, double dLonEnd, boolean bCheckLatLon, String sAlgorithm){
		
		//dLat = current latitude
		//dLon = current longitude
		//rgp1 = corners of current square (order: UL,LL,LR,UR)
		//edg1 = current edge
		//dLonPolyStart = starting longitude of current polygon
		//dLatPolyStart = starting latitude of current polygon
		//bTerminate = flag for terminating
		
		double dLat; double dLon;
		Point2D.Double rgp1[];
		Edge edg1;
		double dLonPolyStart; double dLatPolyStart;
		boolean bTerminate;
		
		//*****************************
		//System.out.println(sph1.checkDiskIntersection(49.3671875, 0, plyUnsmoothed, "winding",100));
		//System.out.println(sph1.checkDiskIntersection(49.3671875, -100, plyUnsmoothed, "winding",100));
		//*****************************
		
		//initializing output polygon
		plySmoothed = new Polygon();
		
		//initializing list of known vertices
		map1 = new HashMap<Point2D.Double,Boolean>();
		
		//initializing map of crossing points
		map2 = new HashMap<Line2D.Double,Point2D.Double>();
		
		//looping through latitudes and longitudes
		for(dLon=dLonStart-2.*dResolution;dLon<=dLonEnd+2.*dResolution;dLon+=dResolution){
		//for(dLon=Math.max(dLonStart-2.*dResolution,-180.);dLon<=Math.min(dLonEnd+2.*dResolution,180.);dLon+=dResolution){
			
			//updating progress
			System.out.println("Longitude " + dLon);
			
			//*************************
			if(dLon<-180 || dLon>180){
				continue;
			}
			//*************************
			
			//looping through latitudes
			//***********************
			for(dLat=dLatStart+2.*dResolution;dLat>=dLatEnd-2.*dResolution;dLat-=dResolution){
			//for(dLat=dLatStart;dLat>=dLatEnd;dLat-=dResolution){		
			//***********************	
				
				//********************************
				//System.out.println("Latitude " + dLat);
				//System.out.println(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
				//if(dLat==70.625){
				//	System.out.println("HERE");
				//}
				//********************************
				
				//correcting round-off errors
				dLon = round(dLon,dRound);
				dLat = round(dLat,dRound);
				
				//checking if within bounds
				//if(plyUnsmoothed.dLatMaximum+dRadius/LAT_DIST<round(dLat-dResolution,dRound)){
				//	continue;
				//}
				//if(plyUnsmoothed.dLatMinimum-dRadius/LAT_DIST>dLat){
				//	continue;
				//}
				
				//loading list of corners
				rgp1 = new Point2D.Double[4];
				rgp1[0] = new Point2D.Double(dLon,dLat); 
				rgp1[1] = new Point2D.Double(dLon,round(dLat-dResolution,dRound)); 
				rgp1[2] = new Point2D.Double(round(dLon + dResolution,dRound),round(dLat-dResolution,dRound)); 
				rgp1[3] = new Point2D.Double(round(dLon + dResolution,dRound),dLat); 
			
				//loading current edge
				edg1 = this.findEdge(rgp1, -9999., -9999., sAlgorithm);
				
				//checking if start of new polygon
				if(edg1!=null && -90.<edg1.dLatStart && edg1.dLatStart<90. && -90.<edg1.dLatEnd && edg1.dLatEnd<90.){
					
					//saving edge, checking if edge is duplicate, and loading starting point
					if(plySmoothed.addAndCorrectEdge(edg1)==false){
						bTerminate=true;
					}else{
						bTerminate=false;
					}
					dLonPolyStart = sph1.correctLongitude(edg1.dLonStart);
					dLatPolyStart = edg1.dLatStart;
					
					//*******************************
					//System.out.println("Entering do loop...");
					//*******************************
					
					//iterating
					while(bTerminate==false){
						
						//loading next box
						rgp1 = this.findNextBox(rgp1, edg1.dLatEnd, edg1.dLonEnd);
						
						//*******************************
						System.out.println(edg1.dLatStart + "," + edg1.dLonStart + ";" + edg1.dLatEnd + "," + edg1.dLonEnd);						
						//*******************************
						
						//**************************
						//if(edg1.dLonEnd>180 || edg1.dLonEnd<-180){
						//	System.out.println("HERE");
						//}
						//**************************
						
						//loading next edge
						edg1 = this.findEdge(rgp1, edg1.dLatEnd, edg1.dLonEnd, sAlgorithm);
						
						//checking if edge is null
						if(edg1==null){
							bTerminate=true;
						}else{
							
							//***********************
							//System.out.println(sph1.checkDiskIntersection(34.875, 3.875, plyUnsmoothed, "winding",250.));
							//if(edg1.dLatEnd==9.5390625){
							//	System.out.println(edg1.dLonEnd);
							//}
							//***********************
							
							//saving next edge if within bounds; terminating otherwise
							if(-90.<edg1.dLatStart && edg1.dLatStart<90. && -90.<edg1.dLatEnd && edg1.dLatEnd<90.){
								plySmoothed.addAndCorrectEdge(edg1);
							}else{
								bTerminate=true;
							}
							
							//setting terminate flag if appropriate
							if(edg1.dLatEnd==dLatPolyStart && sph1.correctLongitude(edg1.dLonEnd) == dLonPolyStart){
								bTerminate=true;
							}
						}
						
						//if(edg1==null){
						//	System.out.println("ERROR: loadPolygon.");
						//}else{
						//	plySmoothed.addAndCorrectEdge(edg1);
						//}
						
						//*******************************
						//System.out.println(edg1.dLatEnd + "," + edg1.dLonEnd + ";" + dLatPolyStart + "," + dLonPolyStart);
						//System.out.println(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
						//*******************************
						
							
					}
					
					//*******************************
					//System.out.println("Do loop exited...");
					//*******************************
				}
			}
		}
		
		//checking if south pole is in smoothed range; adding artificial lower bound if it is (for checking whether points are in polygon using even-odd algorithm
		//if(sph1.findDistanceToPolygon(-89.99, 0., plyUnsmoothed, "winding")[0]<=dRadius){
		if(sph1.checkDiskIntersection(-89.99, 0, plyUnsmoothed, "winding", dRadius)==true){
			
			dLat=-89.99;
			plySmoothed.addEdge(new Edge(dLat,dLat,-180,0), false);
			plySmoothed.addEdge(new Edge(dLat,dLat,0,180), false);
		}
		
		//********************************
		//for(int i=0;i<plySmoothed.getEdgeCount();i++){
		//	edg1 = plySmoothed.getEdge(i);
		//	System.out.println(edg1.getLatMinimum() + "," + edg1.getLonMinimum() + ";" + edg1.getLatMaximum() + "," + edg1.getLonMaximum());
		//}
		//********************************
		
		
	}
	
	private double round(double dNumber, double dPlaces){
		return (double) Math.round(dNumber * dPlaces) / dPlaces;
	}
	
}

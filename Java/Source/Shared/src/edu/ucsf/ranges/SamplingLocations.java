package edu.ucsf.ranges;

import java.util.ArrayList;
import java.util.Random;
import edu.ucsf.base.*;

/**
 * This code generates lists of sampling locations under a variety of spatial sampling schemes.
 * @author jladau
 */

public class SamplingLocations {

	//arg1 = arguments object
	//ptl1 = PointOnLand object
	//bds1 = GeographicBounds
	//rgdSamplingPoints = sampling points
	//sMode = type of locations, "beta-diversity" for beta-diversity
	//sLocation = "Land", "Ocean", or "Coast" depending on where bounds are located
	//bLocationsFound = true if sampling locations successfully found, false otherwise
	//dSamplingArea = area sampled
	//sph1 = spherical geometry object
	//dProportionOnLand = proportion of sampling region on land
	//dDistance = distance between sampling plots (for beta-diversity sampling)
	
	private Arguments arg1;
	private PointOnLand ptl1;
	public GeographicPointBounds bds1;
	public double rgdSamplingPoints[][];
	public String sMode;
	public String sLocation;
	public boolean bLocationsFound;
	public double dSamplingArea;
	public double dProportionOnLand;
	private SphericalGeometry sph1;
	public double dDistance;
	
	private static final double RAD_TO_DEG = 57.295779513;
	private static final double DEG_TO_RAD = 0.017453293;
	
	/**
	 * Constructor: makes sampling locations object with specified set of locations
	 * @param lstLatLon List of locations
	 */
	public SamplingLocations(double dLat, double dLon){
		
		//dLatMin = minimum latitude
		//dLatMax = maximum latitude
		//dLonMin = minimum longitude
		//dLonMax = maximum longitude
		
		double dLatMin; double dLatMax; double dLonMin; double dLonMax;
		
		rgdSamplingPoints = new double[1][2];
		rgdSamplingPoints[0][0]=dLat;
		rgdSamplingPoints[0][1]=dLon;
		sMode="alpha-diversity";
		
		//loading bounds
		dLatMin = dLat;
		dLatMax = dLat;
		dLonMin = dLon;
		dLonMax = dLon;
		bds1 = new GeographicPointBounds(dLatMin,dLatMax,dLonMin,dLonMax);
	}
	
	
	/**
	 * Constructor: makes sampling locations object with specified set of locations
	 * @param lstLatLon List of locations
	 */
	public SamplingLocations(ArrayList<Double[]> lstLatLon){
		
		//dLatMin = minimum latitude
		//dLatMax = maximum latitude
		//dLonMin = minimum longitude
		//dLonMax = maximum longitude
		
		double dLatMin; double dLatMax; double dLonMin; double dLonMax;
		
		rgdSamplingPoints = new double[lstLatLon.size()][lstLatLon.get(0).length];
		for(int i=0;i<lstLatLon.size();i++){
			for(int j=0;j<lstLatLon.get(i).length;j++){
				rgdSamplingPoints[i][j]=lstLatLon.get(i)[j];
			}
		}
		if(rgdSamplingPoints[0].length==4){
			sMode="beta-diversity";
		}else{
			sMode="alpha-diversity";
		}
		
		//loading bounds
		dLatMin = 9999;
		dLatMax = -9999;
		dLonMin = 9999;
		dLonMax = -9999;
		for(int i=0;i<rgdSamplingPoints.length;i++){
			if(rgdSamplingPoints[i][0]<dLatMin){
				dLatMin = rgdSamplingPoints[i][0];
			}
			if(rgdSamplingPoints[i][0]>dLatMax){
				dLatMax = rgdSamplingPoints[i][0];
			}
			if(rgdSamplingPoints[i][1]<dLonMin){
				dLonMin = rgdSamplingPoints[i][1];
			}
			if(rgdSamplingPoints[i][1]>dLonMax){
				dLonMax = rgdSamplingPoints[i][1];
			}
		}
		bds1 = new GeographicPointBounds(dLatMin,dLatMax,dLonMin,dLonMax);
	}
	
	public SamplingLocations(Arguments arg1, GeographicPointBounds bds1){
		
		//saving arguments
		this.arg1 = arg1;
		
		//loading point on land object
		ptl1 = new PointOnLand(arg1.getValueString("sPathLandRaster"));
		
		//saving bounds
		this.bds1 = bds1;
		
		//saving mode
		this.sMode = arg1.getValueString("sMode");

		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//loading sampling area and proportion on land and sampling location
		if(bds1==null){
			dSamplingArea = 510072000;
			dProportionOnLand = 0.292;
			sLocation = "Coast";
		}else{
			dSamplingArea = sph1.findAreaBounds(bds1.rgdArray);
			dProportionOnLand = ptl1.findProportionOnLand(bds1);
			if(ptl1.isOnLand(bds1)){
				sLocation="Land";
			}else if(ptl1.isInWater(bds1)){
				sLocation="Ocean";
			}else{
				sLocation="Coast";
			}
		}
		
		//loading sampling points	
		if(sMode.equals("beta-diversity")){
			
			//loading distance
			dDistance = arg1.getValueDouble("dDistance");
			
			//loading sampling locations
			loadSamplingPointsBetaDiversity();
		}else if(sMode.equals("alpha-diversity")){
			
			//loading sampling locations
			loadSamplingPointsAlphaDiversity();
			
			//updating sampling area
			if(arg1.getValueString("sLand").equals("marine")){
				dSamplingArea=(1.-dProportionOnLand)*dSamplingArea;
			}
			if(arg1.getValueString("sLand").equals("terrestrial")){
				dSamplingArea=dProportionOnLand*dSamplingArea;
			}
		}
	}
	
	/**
	 * Loads sampling points.
	 * @param rgdSamplingBounds Sampling bounds.
	 * @param iRndSeed Random seed.
	 * @param iIterations Number of pairs of points
	 * @param dRadius Radius
	 * @return Pairs of sampling points; each row for an iteration, column 0 with Lat, column 1 with Lon
	 */
	private void loadSamplingPointsAlphaDiversity(){

		//rnd1 = current random number generator
		//du = current first random number
		//dv = current second random number
		//d2 = distance between minimum and maximum longitude bounds
		//dLat = latitude of first sampling point
		//dLng = longitude of first sampling point
		//bContinue = flag for whether to continue
		//iCounter = counter for current iteration
		//rgd2 = current randomly chosen location
		
		Random rnd1;
		double rgd1[]; double rgd2[];
		double du; double dv; double d2; double d3; double d4; double dLat; double dLng;
		boolean bContinue;
		int iCounter;
		
		//initializing random number generator
		rnd1 = new Random(arg1.getValueInt("iRndSeed"));
		
		//loading sampling points
		rgdSamplingPoints = new double[arg1.getValueInt("iIterations")][2];
		for(int i=0;i<arg1.getValueInt("iIterations");i++){
		
			//initializing counter
			iCounter = 0;
			
			do{
			
				//setting continuation flag
				bContinue = true;
				
				//updating counter
				iCounter++;
				
				//checking if bounds are given (no bounds implies global sampling
				if(bds1==null){
				
					//loading random point
					rgd2 = sph1.findRandomPoint();
					dLat = rgd2[0];
					dLng = rgd2[1];
					
				}else{
				
					//loading random numbers
					du = rnd1.nextDouble();
					dv = rnd1.nextDouble();
					
					//loading rgd1
					rgd1 = new double[4];
					rgd1[0] = DEG_TO_RAD*bds1.rgdArray[2];
					rgd1[1] = DEG_TO_RAD*bds1.rgdArray[3];
					rgd1[2] = DEG_TO_RAD*bds1.rgdArray[0];
					rgd1[3] = DEG_TO_RAD*bds1.rgdArray[1];
					
					//loading Lat and Lng
					d2 = rgd1[3]-rgd1[2];
					d3 = Math.cos(rgd1[0]+Math.PI/2.);
					d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
					dLng = du*d2+rgd1[2];
					dLng = RAD_TO_DEG*dLng;
					dLat = Math.acos(d3+d4*dv);
					dLat = RAD_TO_DEG*dLat-90.;
					
					//checking if first point is in sampling bounds, returning error if not
					if(arg1.getValueDouble("dRadius")!=0){	
						if(sph1.checkDiskInBounds(dLat, dLng, arg1.getValueDouble("dRadius"), bds1.rgdArray)==0){
							bContinue=false;
						}
					}
				}
					
				//checking if points are on land
				if(ptl1!=null){
					if(arg1.getValueString("sLand").equals("marine") && ptl1.isOnLand(dLat, dLng)==true){
						bContinue = false;
					}
					if(arg1.getValueString("sLand").equals("terrestrial") && ptl1.isOnLand(dLat, dLng)==false){
						bContinue=false;
					}	
				}
			}while(iCounter<10000 && bContinue==false);
			
			//checking if counter timed out
			if(iCounter==10000){
				bLocationsFound=false;
				return;
			}
			
			//saving values
			rgdSamplingPoints[i][0]=dLat; rgdSamplingPoints[i][1]=dLng;
		}
		
		//outputting success value
		bLocationsFound=true;
	}

	/**
	 * Loads sampling points.
	 * @param rgdSamplingBounds Sampling bounds.
	 * @param iRndSeed Random seed.
	 * @param iIterations Number of pairs of points
	 * @param dRadius Radius
	 * @return Pairs of sampling points; each row for an iteration, column 0 with Lat1, column 1 with Lon1, column 2 with Lat2, column 3 with Lon2
	 */
	private void loadSamplingPointsBetaDiversity(){
	
		//rnd1 = current random number generator
		//du = current first random number
		//dv = current second random number
		//dw = current third random number
		//d2 = distance between minimum and maximum longitude bounds
		//dLat = latitude of first sampling point
		//dLng = longitude of first sampling point
		//rgdX = latitude,longitude of second sampling point
		//dTheta = current angle
		//bContinue = flag for whether to continue
		//iCounter = counter for current iteration
		
		Random rnd1;
		double rgd1[]; double rgdX[];
		double du; double dv; double dw; double d2; double d3; double d4; double dLat; double dLng; double dTheta;
		boolean bContinue;
		int iCounter;
		
		//initializing random number generator
		rnd1 = new Random(arg1.getValueInt("iRndSeed"));
		
		//loading sampling points
		rgdSamplingPoints = new double[arg1.getValueInt("iIterations")][4];
		for(int i=0;i<arg1.getValueInt("iIterations");i++){
		
			//initializing counter
			iCounter = 0;
			
			do{
			
				//setting continuation flag
				bContinue = true;
				
				//updating counter
				iCounter++;
				
				//loading random numbers
				du = rnd1.nextDouble();
				dv = rnd1.nextDouble();
				dw = rnd1.nextDouble();
				
				//loading rgd1
				rgd1 = new double[4];
				rgd1[0] = DEG_TO_RAD*bds1.rgdArray[2];
				rgd1[1] = DEG_TO_RAD*bds1.rgdArray[3];
				rgd1[2] = DEG_TO_RAD*bds1.rgdArray[0];
				rgd1[3] = DEG_TO_RAD*bds1.rgdArray[1];
				
				//loading Lat and Lng
				d2 = rgd1[3]-rgd1[2];
				d3 = Math.cos(rgd1[0]+Math.PI/2.);
				d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
				dLng = du*d2+rgd1[2];
				dLng = RAD_TO_DEG*dLng;
				dLat = Math.acos(d3+d4*dv);
				dLat = RAD_TO_DEG*dLat-90.;
				
				//loading second sampling point
				dTheta = 2*Math.PI*dw;
				rgdX=sph1.findDestination(dLat, dLng, dTheta, dDistance);
				
				//checking if first point is in sampling bounds, returning error if not
				if(sph1.checkDiskInBounds(dLat, dLng, arg1.getValueDouble("dRadius"), bds1.rgdArray)==0){
					bContinue=false;
				}
				
				//checking if second point is in sampling bounds, returning error if not
				if(sph1.checkDiskInBounds(rgdX[0], rgdX[1], arg1.getValueDouble("dRadius"), bds1.rgdArray)==0){
					bContinue=false;
				}
				
				//checking if points are on land
				if(ptl1!=null){
					if(arg1.getValueString("sLand").equals("marine") && ptl1.isOnLand(dLat, dLng)==true){
						bContinue = false;
					}
					if(arg1.getValueString("sLand").equals("land") && ptl1.isOnLand(rgdX[0], rgdX[1])==false){
						bContinue=false;
					}	
				}
			}while(iCounter<10000 && bContinue==false);
			
			//checking if counter timed out
			if(iCounter==10000){
				bLocationsFound=false;
				return;
			}
			
			//saving values
			rgdSamplingPoints[i][0]=dLat; rgdSamplingPoints[i][1]=dLng;
			rgdSamplingPoints[i][2]=rgdX[0]; rgdSamplingPoints[i][3]=rgdX[1];
		}
		
		//outputting success value
		bLocationsFound=true;
	}
}

package edu.ucsf.base;

import java.util.ArrayList;

import edu.ucsf.ranges.GeographicRange;

/**
 * Class checks whether a point is on land
 * @author jladau
 */

public class PointOnLand {

	//cdf1 = NetCDF_IO object
	//lst1 = list of polygons
	//sMode = cdf or shp
	//sph1 = spherical geometry object
	
	private String sMode;
	private NetCDF_IO cdf1;
	private ArrayList<Polygon> lstPolygons;
	private final double RAD_TO_DEG = 57.295779513;
	private final double DEG_TO_RAD = 0.017453293;
	private SphericalGeometry sph1;
	
	/**
	 * Constructor
	 * @param sLandRasterPath Path to topography raster
	 */
	public PointOnLand(String sLandRasterPath){
		
		//rng1 = GeographicRange object
		//lst1 = current polygon in arraylist format
		
		GeographicRange rng1;
		ArrayList<String[]> lst1;
		
		if(sLandRasterPath.endsWith(".nc")){
		
			//loading mode
			sMode = "cdf";
			
			//loading cdf object
			cdf1 = new NetCDF_IO(sLandRasterPath, "reading");
		}else if(sLandRasterPath.endsWith(".shp.txt")){
			
			//loading mode
			sMode = "shp";
			
			//initializing spherical geometry object
			sph1 = new SphericalGeometry();
			
			//loading range object
			rng1 = new GeographicRange(sLandRasterPath);
			
			//loading list of polygons
			lstPolygons = new ArrayList<Polygon>();
			lst1 = rng1.getNextRange();
			while(lst1!=null){
				lstPolygons.add(new Polygon(lst1,1234,false));
				lst1 = rng1.getNextRange();
			}
		}
	}
	
	/**
	 * Checks whether given location is on land.
	 * @param dLat Latitude of location.
	 * @param dLon Longitude of location.
	 * @return True if on land, false if not on land.
	 */
	public boolean isOnLand(double dLat, double dLon){
		
		//checking whether reading from cdf file or shapefile
		if(sMode.equals("cdf")){
		
			//checking value
			if(cdf1.readValue(dLat,dLon,-9999,-9999)!=-9999){
				return true;
			}else{
				return false;
			}
		}else if(sMode.equals("shp")){
			
			//checking value
			for(int i=0;i<lstPolygons.size();i++){
				if(sph1.isPointPolygon(dLat, dLon, lstPolygons.get(i), "winding")==1){
					return true;
				}
			}
			
			//outputting failure value
			return false;
		}else{
			return false;
		}
	}

	/**
	 * Checks if specified bounds are entirely over land
	 * @param bds1 GeographicBounds
	 * @return True if entirely over land, false otherwise.
	 */
	public boolean isOnLand(GeographicPointBounds bds1){
		
		//iIterations = number of random points to place
		//du = first current random number
		//dv = second current random number
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//i1 = number of points within polygon
		//i2 = number of points sampled
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
		
		double rgd1[];
		double dLat; double dLng; double du; double dv;
		int iIterations;
		double d2 = 0; double d3 = 0; double d4 = 0;
		
		//loading number of iterations
		iIterations = 1000;
		
		//loading rgd1
		rgd1 = new double[4];
		rgd1[0] = DEG_TO_RAD*bds1.dLatitudeMin;
		rgd1[1] = DEG_TO_RAD*bds1.dLatitudeMax;
		rgd1[2] = DEG_TO_RAD*bds1.dLongitudeMin;
		rgd1[3] = DEG_TO_RAD*bds1.dLongitudeMax;
		
		//loading d2
		d2 = rgd1[3]-rgd1[2];
		
		//loading d3 and d4
		d3 = Math.cos(rgd1[0]+Math.PI/2.);
		d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
		//looping through sampling points
		for(int i=1;i<=iIterations;i++){
			
			//loading random number
			du = Math.random(); dv = Math.random();
			
			//loading random point	
			dLng = du*d2+rgd1[2];
			dLng = RAD_TO_DEG*dLng;
			dLat = Math.acos(d3+d4*dv);
			dLat = RAD_TO_DEG*dLat-90.;
		
			//checking if sampling point is on land
			if(!isOnLand(dLat,dLng)){
				return false;
			}
		}
		
		//outputting successful result
		return true;
	}
	
	/**
	 * Checks if specified bounds are entirely over water
	 * @param rgdBounds Bounds of interest.
	 * @return True if entirely over land, false otherwise.
	 */
	public boolean isInWater(GeographicPointBounds bds1){
		
		//iIterations = number of random points to place
		//du = first current random number
		//dv = second current random number
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//i1 = number of points within polygon
		//i2 = number of points sampled
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
		
		double rgd1[];
		double dLat; double dLng; double du; double dv;
		int iIterations;
		double d2 = 0; double d3 = 0; double d4 = 0;
		
		//loading number of iterations
		iIterations = 1000;
		
		//loading rgd1
		rgd1 = new double[4];
		rgd1[0] = DEG_TO_RAD*bds1.dLatitudeMin;
		rgd1[1] = DEG_TO_RAD*bds1.dLatitudeMax;
		rgd1[2] = DEG_TO_RAD*bds1.dLongitudeMin;
		rgd1[3] = DEG_TO_RAD*bds1.dLongitudeMax;
		
		//loading d2
		d2 = rgd1[3]-rgd1[2];
		
		//loading d3 and d4
		d3 = Math.cos(rgd1[0]+Math.PI/2.);
		d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
		//looping through sampling points
		for(int i=1;i<=iIterations;i++){
			
			//loading random number
			du = Math.random(); dv = Math.random();
			
			//loading random point	
			dLng = du*d2+rgd1[2];
			dLng = RAD_TO_DEG*dLng;
			dLat = Math.acos(d3+d4*dv);
			dLat = RAD_TO_DEG*dLat-90.;
		
			//checking if sampling point is on land
			if(isOnLand(dLat,dLng)){
				return false;
			}
		}
		
		//outputting successful result
		return true;
	}
	
	/**
	 * Finds proportion of are on land within specified bounds
	 * @param rgdBounds Bounds of interest.
	 * @return Proportion on land.
	 */
	public double findProportionOnLand(GeographicPointBounds bds1){
		
		//iIterations = number of random points to place
		//du = first current random number
		//dv = second current random number
		//dLat = latitude of sampling point
		//dLng = longitude of sampling point
		//i1 = number of points within polygon
		//i2 = number of points sampled
		//d2 = number of degrees between upper and lower bounds
		//d3 = cos(minimum latitude)
		//d4 = cos(maximum latitude) - cos(minimum latitude)
		//rgd1 = lat0,lat1,lng0,lng1 in radians
		
		double rgd1[];
		double dLat; double dLng; double du; double dv;
		int iIterations; int i1=0; int i2=0;
		double d2 = 0; double d3 = 0; double d4 = 0;
		
		//loading number of iterations
		iIterations = 1000;
		
		//loading rgd1
		rgd1 = new double[4];
		rgd1[0] = DEG_TO_RAD*bds1.dLatitudeMin;
		rgd1[1] = DEG_TO_RAD*bds1.dLatitudeMax;
		rgd1[2] = DEG_TO_RAD*bds1.dLongitudeMin;
		rgd1[3] = DEG_TO_RAD*bds1.dLongitudeMax;
		
		//loading d2
		d2 = rgd1[3]-rgd1[2];
		
		//loading d3 and d4
		d3 = Math.cos(rgd1[0]+Math.PI/2.);
		d4 = Math.cos(rgd1[1]+Math.PI/2.)-d3;
		
		//looping through sampling points
		for(int i=1;i<=iIterations;i++){
			
			//loading random number
			du = Math.random(); dv = Math.random();
			
			//loading random point	
			dLng = du*d2+rgd1[2];
			dLng = RAD_TO_DEG*dLng;
			dLat = Math.acos(d3+d4*dv);
			dLat = RAD_TO_DEG*dLat-90.;
		
			//checking if sampling point is on land
			if(isOnLand(dLat,dLng)){
				i1++;
			}
			
			//updating i2
			i2++;
		}
		
		//outputting result
		return ((double) i1)/((double) i2);
	}
}

package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.TreeMap;

import edu.ucsf.base.*;

public class SDMRasterData_Interpolated extends SDMRasterData {

	//mapElevation(sVar)  = list of raster elevations for looking up floor and ceiling elevations, returns empty value ("true")
	//mapTime(iDayInYear) = list of raster times for looking up floor and ceiling times, return the month corresponding to the given day in year.
	//sVar = variable
	
	private TreeMap<Double,Boolean> mapElevation;
	private TreeMap<Integer,Integer> mapTime;
	private String sVar;
	
	public SDMRasterData_Interpolated(String sVar, String sPathCDF){
		
		super();
		mapElevation = new TreeMap<Double,Boolean>();
		mapTime = new TreeMap<Integer,Integer>();
		mapPath.put(sVar, sPathCDF);
		this.sVar = sVar;
	}
	
	/**
	 * Interpolates raster values to given day and month and depth
	 * @param map2 Map with rasters.  Keys are depths for rasters, values are treemaps containing monthly rasters for given depth.
	 * @param iDay Day to look up.
	 * @param iMonth Month to look up.
	 * @param dElevation Elevation to look up
	 * @param dLat Latitude
	 * @param dLng Longitude
	 * @param sRasterDir Directory with rasters
	 * @param sVarNames List of variable names in order that they were initially output (for checking that output order is constant)
	 * @return Raster values in the format output by RasterData object.
	 */
	public double findValueInterpolated(int iDay, int iMonth, double dElevation, double dLat, double dLng){
		
		//iDayInYear = day in year to look up
		//iDay1 = day in year for lower bracketing raster
		//iDay2 = day in year for upper bracketing raster
		//iMonth1 = month for lower bracketing raster
		//iMonth2 = month for upper bracketing raster
		//dElevation1 = lower bracketing elevation
		//dElevation2 = upper bracketing elevation
		//rgsRefData =  Reference data.  Each row represents a data point (order is x1,y1; x2,y1; x1,y2; x2,y2).   Column 0 with day, column 1 with depth, column 2 with raster value
		//rsl1 = current raster location
		//d1 = current value being added to map
		//lstDims = list of current dimension values
		
		ArrayList<String> lstDims;
		RasterLocation rsl1;
		double d1; double dElevation1; double dElevation2;
		int iDayInYear; int iDay1; int iDay2; int iMonth1; int iMonth2;
		String rgsRefData[][];
		
		//loading elevation and time treemaps if necessary
		if(mapElevation.size()==0){
			
			//loading list of dimension values for time
			lstDims = getDims("time");
			for(int k=0;k<lstDims.size();k++){
				d1 = Double.parseDouble(lstDims.get(k));
				if(!mapTime.containsKey((int) d1)){
					mapTime.put(findDayInYear(15, (int) d1), (int) d1);
				}
			}
			
			//loading list of dimension values for elevation
			lstDims = getDims("vert");
			for(int k=0;k<lstDims.size();k++){
				d1 = Double.parseDouble(lstDims.get(k));
				if(!mapElevation.containsKey(d1)){
					mapElevation.put(d1, true);
				}
			}
			
			//checking if dims are present
			if(mapElevation.size()==0){
				mapElevation.put(0., false);
			}
			if(mapTime.size()==0){
				mapTime.put(1,1);
			}
		}
		
		//loading day in year
		iDayInYear = findDayInYear(iDay,iMonth);
		
		//loading bracketing depths
		if(dElevation<mapElevation.firstKey()){
			dElevation1=mapElevation.firstKey();
			dElevation2=mapElevation.firstKey();
		}else if(mapElevation.lastKey()<dElevation){
			dElevation1=mapElevation.lastKey();
			dElevation2=mapElevation.lastKey();
		}else{
			dElevation1 = mapElevation.floorKey(dElevation);
			dElevation2 = mapElevation.ceilingKey(dElevation);
		}
			
		//loading bracketing days
		if(iDayInYear<mapTime.firstKey() || mapTime.lastKey()<iDayInYear){
			iDay1=349-365;
			iDay2=15;
			if(iDayInYear>iDay2){
				iDayInYear-=365;
			}
			iMonth1 = 12;
			iMonth2 = 1;
		}else{
			iDay1 = mapTime.floorKey(iDayInYear);
			iDay2 = mapTime.ceilingKey(iDayInYear);
			iMonth1 = mapTime.get(iDay1);
			iMonth2 = mapTime.get(iDay2);
		}
		
		//initializing rgsRefData
		rgsRefData = new String[4][3];
		rgsRefData[0][0]=Integer.toString(iDay1);
		rgsRefData[1][0]=Integer.toString(iDay2);
		rgsRefData[2][0]=Integer.toString(iDay1);
		rgsRefData[3][0]=Integer.toString(iDay2);	
		rgsRefData[0][1]=Double.toString(dElevation1);
		rgsRefData[1][1]=Double.toString(dElevation1);
		rgsRefData[2][1]=Double.toString(dElevation2);
		rgsRefData[3][1]=Double.toString(dElevation2);
		
		//loading values
		rsl1 = new RasterLocation(dLat, dLng, dElevation1, (double) iMonth1, -9999, -9999, "-9999");
		rgsRefData[0][2] = Double.toString(getRasterValueNeighborhood(rsl1,5.));
		rsl1 = new RasterLocation(dLat, dLng, dElevation1, (double) iMonth2, -9999, -9999, "-9999");
		rgsRefData[1][2] = Double.toString(getRasterValueNeighborhood(rsl1,5.));
		rsl1 = new RasterLocation(dLat, dLng, dElevation2, (double) iMonth1, -9999, -9999, "-9999");
		rgsRefData[2][2] = Double.toString(getRasterValueNeighborhood(rsl1,5.));
		rsl1 = new RasterLocation(dLat, dLng, dElevation2, (double) iMonth2, -9999, -9999, "-9999");
		rgsRefData[3][2] = Double.toString(getRasterValueNeighborhood(rsl1,5.));
		
		//running bilinear interpolation and returning result
		return Double.parseDouble(Interpolation.interpolateBilinear(rgsRefData,iDayInYear + "," + dElevation).split(",")[0]);
	}
	
	/**
	 * Finds the mean raster value in a circle of given radius around given point (error values are ignored); if raster value at given point is available, it will be returned instead.
	 * @param sLat Latitude of point
	 * @param sLng Longitude of point
	 * @param sElevation Elevation of interest.
	 * @param sTime Time (e.g., month) of interest.
	 * @param dRadius Radius (in km)
	 * @param cdf1 NetCDF object to consider.
	 * @return Mean raster value if it is available; -9999 otherwise.
	 */
	private double getRasterValueNeighborhood(RasterLocation rsl1, double dRadius){
		
		//dValue = current value
		//dTotal = total along circle
		//dCount = number of non-error values along circle
		//sph1 = spherical geometry object
		//rgd1 = current latitude and longitude
		//rsl2 = current neighboring raster location
		
		double dValue; double dTotal=0; double dCount=0;
		SphericalGeometry sph1;
		double rgd1[];
		RasterLocation rsl2;
		
		//checking center point
		dValue = this.getRasterValue(rsl1, sVar);
		if(dValue!=-9999.){
			return dValue;
		}
		
		//loading spherical geometry object
		sph1 = new SphericalGeometry();
		
		//looping along radius
		for(double d=0;d<2*Math.PI;d+=Math.PI/5){
		
			//loading current point
			rgd1 = sph1.findDestination(rsl1.dLat, rsl1.dLon, d, dRadius);
			rsl2 = new RasterLocation(rgd1[0],rgd1[1],rsl1.dVert,rsl1.dTime,-9999,-9999,null);
			
			//loading current value
			dValue = this.getRasterValue(rsl2, sVar);
			
			//updating total
			if(dValue!=-9999.){
				dTotal+=dValue;
				dCount+=1;
			}
		}
		
		//outputting average
		if(dCount==0){
			if(dRadius>175){
				return -9999;
			}else{
				return getRasterValueNeighborhood(rsl1,dRadius+5.);
			}
		}else{
			return dTotal/dCount;
		}
	}
	
	/**
	 * Finds the day in year (1 to 365) of given date.
	 * @param iDay Day in month of date.
	 * @param iMonth Month of date.
	 * @return Day in year.
	 */
	private int findDayInYear(int iDay, int iMonth){
		
		//i1 = output
		
		int i1=-9999;
		
		//finding day of start of month
		if(iMonth==1){
			i1=1;
		}else if(iMonth==2){
			i1=32;
		}else if(iMonth==3){
			i1=60;
		}else if(iMonth==4){
			i1=91;
		}else if(iMonth==5){
			i1=121;
		}else if(iMonth==6){
			i1=152;
		}else if(iMonth==7){
			i1=182;
		}else if(iMonth==8){
			i1=213;
		}else if(iMonth==9){
			i1=244;
		}else if(iMonth==10){
			i1=274;
		}else if(iMonth==11){
			i1=305;
		}else if(iMonth==12){
			i1=335;
		}
		
		//adding day
		i1+=iDay-1;
		
		//outputting result
		return i1;	
	}
	
	private ArrayList<String> getDims(String sDimension){
		
		//loading variable if necessary
		if(!mapCDF.containsKey(sVar)){
			mapCDF.put(sVar, new NetCDF_IO(mapPath.get(sVar),"reading"));
		}
		
		//returning result
		return mapCDF.get(sVar).getDimensionValues(sDimension);
	}
	
	public void closeCDF(){
		this.closeCDF(sVar);
	}
	
	
}

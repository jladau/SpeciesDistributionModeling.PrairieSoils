package edu.ucsf.sdm;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import edu.ucsf.base.*;

/**
 * Class interpolates grid to a 0.5 degree resolution.  Uses bilinear interpolation.
 * @author jladau
 */

public class InterpolateAndMaskGrid {

	//mapLat = returns lookup latitude value for netcdf object for given latitude in initial reference grid
	//mapLng = returns lookup longitude value for netcdf object for given longitude in initial reference grid
	//ptl1 = point on land object, for masking
	
	private TreeMap<Double,Double> mapLat; 
	private TreeMap<Double,Double> mapLng;
	private PointOnLand ptl1 = null;
	
	/**
	 * Constructor.
	 */
	public InterpolateAndMaskGrid(String sMaskPath){
		
		//dLat = current latitude
		//dLng = current longitude
		
		double dLat; double dLng;
		
		//creating latitude map
		mapLat = new TreeMap<Double,Double>();
		dLat = 90.25;
		for(int i=0;i<360;i++){
			dLat-=0.5;
			mapLat.put(dLat, dLat+0.0125);
		}
		
		//creating longitude map
		mapLng = new TreeMap<Double,Double>();
		dLng = -180.25;
		for(int j=0;j<720;j++){
			dLng+=0.5;
			mapLng.put(dLng,dLng+0.0125);
		}
		mapLng.put(-180.25,mapLng.get(179.75));
		mapLng.put(180.25,mapLng.get(-179.75));
		
		//initializing mask if appropriate
		if(sMaskPath!=null){
			ptl1 = new PointOnLand(sMaskPath);
		}
		
	}
	
	/**
	 * Interpolates grid.
	 * @param cdf1 NetCDF object with grid to be interpolated.
	 */
	public double[][] interpolateGrid(double dVert, double dTime, NetCDF_IO cdf1, double dResolution){
		
		//rgd1 = output
		//dLat = current latitude
		//dLng = current longitude
		//dLat1 = current latitude where value is being read
		//dLng1 = current longitude where value is being read
		//rgdLatBounds = latitude bounds
		//rgdLngBound = longitude bounds
		//rgsData = data for interpolation
		//sArgValues = coordinates being looked up
		//mapValue(sArgValues) = returns value for location (avoids repeated netcdf lookups)
		//dValue = current value
		//iRows = number of rows in output
		
		int iRows;
		double rgd1[][]; double rgdLatBounds[]; double rgdLngBounds[];
		double dLat; double dLng; double dValue; double dLat1; double dLng1;
		String rgsData[][];
		String sArgValues;
		Map<String,Double> mapValue;
		
		//initializing output
		iRows = (int) (180./dResolution);
		rgd1 = new double[iRows][2*iRows];
		
		//initializing values map
		mapValue = new HashMap<String,Double>();
		
		//looping through latitudes in output grid
		dLat=90. + dResolution/2.;
		for(int i=0;i<rgd1.length;i++){
			dLat-=dResolution;

			//loading current latitude bounds
			rgdLatBounds =new double[2];
			if(dLat<mapLat.firstKey()){
				rgdLatBounds[0]=mapLat.firstKey();
				rgdLatBounds[1]=mapLat.firstKey();
			}else if(dLat>mapLat.lastKey()){
				rgdLatBounds[0]=mapLat.lastKey();
				rgdLatBounds[1]=mapLat.lastKey();
			}else{
				rgdLatBounds[0]=mapLat.floorKey(dLat);
				rgdLatBounds[1]=mapLat.ceilingKey(dLat);
			}
			
			//looping through longitudes in output grid
			dLng=-180.-dResolution/2.;
			
			for(int j=0;j<rgd1[0].length;j++){
				dLng+=dResolution;
				
				//checking if masked point
				if(ptl1!=null){
					if(!ptl1.isOnLand(dLat, dLng)){
						rgd1[i][j]=-9999;
						continue;
					}
				}
				
				//loading longitude bounds
				rgdLngBounds = new double[2];
				rgdLngBounds[0]=mapLng.floorKey(dLng);
				rgdLngBounds[1]=mapLng.ceilingKey(dLng);
				
				//loading data for interpolation
				rgsData = new String[4][3];
				for(int k=0;k<4;k++){
					rgsData[k][0]=Double.toString(rgdLngBounds[k%2]);
					if(k<2){
						rgsData[k][1]=Double.toString(rgdLatBounds[0]);
						sArgValues = "lat:" + mapLat.get(rgdLatBounds[0]);
						dLat1 = mapLat.get(rgdLatBounds[0]);
					}else{
						rgsData[k][1]=Double.toString(rgdLatBounds[1]);
						sArgValues = "lat:" + mapLat.get(rgdLatBounds[1]);
						dLat1 = mapLat.get(rgdLatBounds[1]);
					}
					sArgValues += ",lon:" + mapLng.get(rgdLngBounds[k%2]);
					dLng1 = mapLng.get(rgdLngBounds[k%2]);
					if(mapValue.containsKey(sArgValues)){
						dValue = mapValue.get(sArgValues);
					}else{
						dValue = cdf1.readValue(dLat1,dLng1,dVert,dTime);
						mapValue.put(sArgValues,dValue);
					}
					rgsData[k][2]=Double.toString(dValue);
				}
				
				//interpolating
				rgd1[i][j]=Double.parseDouble(Interpolation.interpolateBilinear(rgsData, dLng + "," + dLat));
			}
		}
		
		//returning result
		return rgd1;
	}
}

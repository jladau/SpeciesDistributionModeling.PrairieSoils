package edu.ucsf.base;

import java.util.HashMap;
import java.util.Map;

/**
 * This class looks up the continent of a given latitude and longitude.
 * @author jladau
 */

public class LookupContinent {

	//mapContinent(sLat,sLon) = returns the continent of given latitude/longitude
	
	private Map<String,String> mapContinent;
	
	/**
	 * Constructor
	 * @param sPathContinents Path to file with continent data.
	 */
	public LookupContinent(String sPathContinents){
		
		//rgs1 = continents file
		
		String rgs1[][];
		
		//loading continents file
		rgs1 = FileIO.readFile(sPathContinents, ",");
		
		//looping through file and loading map
		mapContinent = new HashMap<String,String>(259200);
		for(int i=1;i<rgs1.length;i++){
			mapContinent.put(rgs1[i][0] + "," + rgs1[i][1], rgs1[i][2]);
		}
	}
	
	/**
	 * Looks up continent of given latitude and longitude
	 * @param dLat Latitude of interest.
	 * @param dLon Longitude of interest.
	 * @param bRejectNA If true, a window of five degrees will be searched until a value not equal to NA is found
	 * @return Continent.
	 */
	public String findContinent(double dLat, double dLon, boolean bRejectNA){
		
		//dLatRound = rounded latitude
		//dLonRound = rounded longitude
		//s1 = lookup key
		//sOut = output
		
		String sOut;
		String s1;
		double dLatRound; double dLonRound;
		
		//loading rounded values
		dLatRound = this.floorHalf(dLat);
		dLonRound = this.floorHalf(dLon);
		
		//loading key
		s1 = dLatRound + "," + dLonRound;
		
		//looking up value
		if(!mapContinent.containsKey(s1)){
			System.out.println("ERROR: LookupContinent.");
			return "-9999";
		}else{
			sOut = mapContinent.get(s1);
			
			//searching through neighboring points if NAs are to be rejected
			if(sOut.equals("NA") && bRejectNA==true){
				for(double d=0.5;d<=2.5;d++){
					sOut = this.findContinent(dLat+d, dLon+d, false);
					if(!sOut.equals("NA")){
						return sOut;
					}
				}
			}		
			return sOut;
		}
	}
	
	public double floorHalf(double d1){
		
		//rounds given number down to the nearest 0.5
		
		//d2 = double of current number
		
		double d2;
		
		d2 = 2.*d1;
		d2 = Math.floor(d2);
		
		return d2/2.;
	}
	
}

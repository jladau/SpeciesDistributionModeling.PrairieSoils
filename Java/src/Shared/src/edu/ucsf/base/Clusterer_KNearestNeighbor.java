package edu.ucsf.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This code performs k-nearest nieghbor classification
 * @author jladau
 */
public class Clusterer_KNearestNeighbor {

	//iK = setting of k
	//mapInitialClass = initial classification map
	
	private int iK;
	private Map<String,String> mapInitialClass;
	
	/**
	 * Constructor
	 * @param iK Setting of k for classification
	 * @param mapInitialClass Initial classification map.  Keys are objects, values are classes of keys.
	 */
	public Clusterer_KNearestNeighbor(int iK, Map<String,String> mapInitialClass){
		
		//saving values
		this.mapInitialClass = mapInitialClass;
		this.iK = iK;
	}
	
	/**
	 * Classifies location
	 * @param mapDistances Keys are names of classified objects.  Values are distances from query point to given classified object. 
	 * @return Classification of location
	 */
	public String classifyLocation(Map<String,Double> mapDistances){
		
		//sClass = current classification
		//mapFrequency(sObject) = frequency of current classification
		//dDistance = current distance
		//dThreshold = threshold
		//dCount = current count
		//dMaxCount = maximum count
		//sMaxClass = maximum class
		//rgdDistances = array of distances 
		//iRow = output row
		
		double rgdDistances[];
		double dDistance; double dThreshold; double dCount; double dMaxCount;
		String sClass; String sMaxClass;
		Map<String,Double> mapFrequency;
		int iRow;
		
		//loading array of distances for finding threshold
		rgdDistances = new double[mapDistances.size()];
		iRow = 0;
		for(String s:mapDistances.keySet()){
			rgdDistances[iRow]=mapDistances.get(s);
			iRow++;
		}
		
		//sorting array and finding threshold
		Arrays.sort(rgdDistances);
		dThreshold = rgdDistances[iK-1];
		
		//initializing maximum count
		dMaxCount = -9999.;
		sMaxClass = "-9999";
		
		//loading frequency map
		mapFrequency = new HashMap<String,Double>();
		for(String s:mapDistances.keySet()){
			
			//loading current classification
			sClass = mapInitialClass.get(s);
			
			//loading current distance
			dDistance = mapDistances.get(s);
			
			//checking if threshold met
			if(dDistance<=dThreshold){
				
				if(mapFrequency.containsKey(sClass)){
					dCount = mapFrequency.get(sClass);
				}else{
					dCount = 0.;
				}
				dCount+=1./dDistance;
				mapFrequency.put(sClass, dCount);
				
				//updating maxima
				if(dCount>dMaxCount){
					dMaxCount = dCount;
					sMaxClass = sClass;
				}
			}
		}
		
		//returning output
		return sMaxClass;
	}
}

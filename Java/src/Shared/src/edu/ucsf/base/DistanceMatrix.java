package edu.ucsf.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Distance matrix object.
 * @author jladau
 */

public class DistanceMatrix {

	//mapDistance(iObject1,iObject2) = returns the distance between pair of objects
	//iMaxObject = current maximum index for an object
	//setObjects = set of all objects
	//sMode = mode for merging objects: "maximum" for new distance to be the maximum distance, "minimum" for new distance to be minimum distance, "average" for new distance to be mean distance
	//mapCluster(iObject) = returns the set of original objects in specified cluster
	
	public Map<Integer,Map<Integer,Double>> mapDistance;
	public Map<Integer,Set<Integer>> mapCluster;
	private int iMaxObject;
	private Set<Integer> setObjects;
	private String sMode;
	
	/**
	 * Constructor
	 * @param sMode Mode for merging objects: "maximum" for new distance to be the maximum distance, "minimum" for new distance to be minimum distance, "average" for new distance to be mean distance
	 */
	public DistanceMatrix(String sMode){
		
		//saving mode
		this.sMode = sMode;
		
		//initializing distance map
		mapDistance = new HashMap<Integer,Map<Integer,Double>>();
		
		//initializing maximum object counter
		iMaxObject = 0;
		
		//initializing set of objects
		setObjects = new HashSet<Integer>();
		
		//initializing map with set of original objects in current objects
		mapCluster = new HashMap<Integer,Set<Integer>>();
	}
	
	/**
	 * Loads distance between pair of objects
	 * @param iObject1 First object
	 * @param iObject2 Second object
	 * @param dDistance Distance
	 */
	public void loadDistance(int iObject1, int iObject2, double dDistance){
		
		//set1 = current set of elements being in object
		//rgi1 = pair of objects
		
		int rgi1[];
		Set<Integer> set1;
		
		//adding distance
		this.addDistance(iObject1, iObject2, dDistance);
		
		//loading pair of objects
		rgi1 = new int[2];
		rgi1[0]=iObject1;
		rgi1[1]=iObject2;
		
		for(int i=0;i<2;i++){
		
			//updating max object
			if(rgi1[i]>iMaxObject){
				iMaxObject = rgi1[i];
			}
			
			//updating set of all objects
			setObjects.add(rgi1[i]);
			
			//updating set of objects within each object
			if(!mapCluster.containsKey(rgi1[i])){
				set1 = new HashSet<Integer>();
				set1.add(rgi1[i]);
				mapCluster.put(rgi1[i],set1);
			}
		}	
	}
	
	/**
	 * Merges pair of closest objects
	 */
	public void mergeClosestObjects(){
		
		//d1 = current minimum distance
		//rgi1 = pair of closest objects
		//dDistance = current distance
		
		double d1; double dDistance;
		int rgi1[];
		
		//initializing minimum distance
		d1 = 99999999999999999999999.;
		
		//initializing output
		rgi1 = new int[2];
		
		//looping through pairs of observations
		for(Integer i:mapDistance.keySet()){
			for(Integer j:mapDistance.get(i).keySet()){
				if(i!=j){
					dDistance = this.getDistance(i, j);
					if(dDistance<d1){
						rgi1[0]=i;
						rgi1[1]=j;
						d1 = dDistance;
					}
				}
			}
		}
		
		//************************
		//System.out.println(rgi1[0] + "," + rgi1[1]);
		//************************
		
		//merging objects
		this.mergeObjects(rgi1[0], rgi1[1]);
	}
	
	/**
	 * Gets current clustering of original objects
	 * @return Current clustering.
	 */
	public String printCurrentClustering(){
		
		//sbl1 = output
		
		StringBuilder sbl1;
		
		//initializing output
		sbl1 = new StringBuilder();
		
		//looping through clusters
		for(Integer i:mapCluster.keySet()){
			sbl1.append("|");
			for(Integer j:mapCluster.get(i)){
				if(!sbl1.toString().endsWith("|")){
					sbl1.append(" ");
				}
				sbl1.append(j);
			}
		}
		sbl1.append("|");
		
		//returning result
		return sbl1.toString();
	}

	private void addDistance(int iObject1, int iObject2, double dDistance){
		
		//map1 = temporary map being loaded
		//i1 = least index of iObject1 and iObject2
		//i2 = greatest index of iObject1 and iObject2
		
		int i1; int i2;
		Map<Integer,Double> map1;
		
		if(iObject1<iObject2){
			i1 = iObject1;
			i2 = iObject2;
		}else{
			i2 = iObject1;
			i1 = iObject2;
		}
		
		//updating distance list
		if(!mapDistance.containsKey(i1)){
			map1 = new HashMap<Integer,Double>();
			mapDistance.put(i1, map1);
		}
		mapDistance.get(i1).put(i2, dDistance);
	}

	/**
	 * Finds new distance
	 * @param iObject1 First object being merged
	 * @param iObject2 Second object being merged
	 * @param iObject3 Object to which distance is being found
	 * @return Distance between merged objects 1 and 2 to third object
	 */
	private double findNewDistance(int iObject1, int iObject2, int iObject3){
		
		//dDistance1 = distance from object1 to object3
		//dDistance2 = distance from object2 to object3
		//dCount1 = number of objects in object1
		//dCount2 = number of objects in object2
		//dCount3 = number of objects in object3
		//dOut = output
		
		double dDistance1; double dDistance2; double dCount1; double dCount2; double dCount3; double dOut;
		
		//loading distances
		dDistance1 = this.getDistance(iObject1, iObject3);
		dDistance2 = this.getDistance(iObject2, iObject3);
		
		//outputting results
		if(sMode.equals("maximum")){
			if(dDistance1>dDistance2){
				return dDistance1;
			}else{
				return dDistance2;
			}
		}else if(sMode.equals("minimum")){
			if(dDistance1<dDistance2){
				return dDistance2;
			}else{
				return dDistance1;
			}
		}else if(sMode.equals("average")){
			
			//loading counts
			dCount1 = (double) mapCluster.get(iObject1).size();
			dCount2 = (double) mapCluster.get(iObject2).size();
			dCount3 = (double) mapCluster.get(iObject3).size();
			
			//loading output
			dOut = dCount1*dCount3*dDistance1 + dCount2*dCount3*dDistance2;
			dOut = dOut/(dCount1*dCount3+dCount2*dCount3);
			
			//returning output
			return dOut;
		}else{
			return -9999.;
		}
	}

	private double getDistance(int iObject1, int iObject2){
		if(iObject1<iObject2){
			return mapDistance.get(iObject1).get(iObject2);
		}else{
			return mapDistance.get(iObject2).get(iObject1);
		}
	}

	/**
	 * Merges objects into a single object and updates distance matrix
	 * @param iObject1 First object
	 * @param iObject2 Second object
	 * @param iObjectNew New object
	 */
	private void mergeObjects(int iObject1, int iObject2){
		
		//dDistance = new distance
		//set1 = set of original objects in iObject1
		//set2 = set of original objects in iObject2
		
		Set<Integer> set1; Set<Integer> set2;
		double dDistance;
		
		//updating new object counter
		iMaxObject++;
		
		//looping through objects
		for(Integer i:setObjects){
			if(i!=iObject1 && i!=iObject2){
				
				//loading new distance
				dDistance = this.findNewDistance(iObject1, iObject2, i);
				
				//saving new distance
				this.addDistance(iMaxObject, i, dDistance);
				
				//removing old distances
				this.removeDistance(iObject1, i);
				this.removeDistance(iObject2, i);
			}
		}
		
		//removing distance between pair of closest objects
		this.removeDistance(iObject1, iObject2);
		
		//updating set of objects
		setObjects.remove(iObject1);
		setObjects.remove(iObject2);
		setObjects.add(iMaxObject);
		
		//updating list of objects
		set1 = mapCluster.get(iObject1);
		set2 = mapCluster.get(iObject2);
		set1.addAll(set2);
		mapCluster.put(iMaxObject, set1);
		mapCluster.remove(iObject1);
		mapCluster.remove(iObject2);
	}
	
	private void removeDistance(int iObject1, int iObject2){
		if(iObject1<iObject2){
			mapDistance.get(iObject1).remove(iObject2);
			if(mapDistance.get(iObject1).size()==0){
				mapDistance.remove(iObject1);
			}
		}else{
			mapDistance.get(iObject2).remove(iObject1);
			if(mapDistance.get(iObject2).size()==0){
				mapDistance.remove(iObject2);
			}
		}
	}
}

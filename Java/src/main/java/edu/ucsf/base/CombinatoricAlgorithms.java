package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Contains implementations of various combinatoric algorithms from Nijenhuis and Wilf (1978) unless otherwise noted.
 * @author jladau
 */
public class CombinatoricAlgorithms {

	private int[] rgiExcluded;
	
	private int iIndex=0;
	private int[] rgi1;
	private int i1;
	private ArrayList<Integer> lst1;
	
	private boolean bFinal = false;
	private int m = -9999;
	private int h;
	private Map<Integer,Integer> a;
	
	/**
	 * Constructor
	 */
	public CombinatoricAlgorithms(){
	}
	
	/**
	 * Returns the set of all subsets from the given set
	 * @param set1 Set for which to find subsets
	 * @param k Size of subsets
	 * @return Set of all subsets
	 */
	public HashSet<HashSet<String>> findAllSubsets(HashSet<String> set1, int k){
		
		//lst1 = set of initial objects in list format
		
		ArrayList<String> lst1;
		
		//checking if k is greater than the size of the initial set
		if(k>set1.size()){
			return null;
		}
		
		//loading set in list format
		lst1 = new ArrayList<String>(set1.size());
		for(String s:set1){
			lst1.add(s);
		}
		
		//returning result
		return this.findAllSubsets(lst1, k);
	}
	
	/**
	 * Returns the set of all subsets from the given set
	 * @param lst1 List from which to find subsets
	 * @param k Size of subsets
	 * @return Set of all subsets
	 */
	public HashSet<HashSet<String>> findAllSubsets(ArrayList<String> lst1, int k){
		
		//rgiSubset = current subset
		//setCurrent = set being added
		//setOut = output
		
		int rgiSubset[];
		HashSet<String> setCurrent; HashSet<HashSet<String>> setOut;
		
		//checking if k is greater than the size of the initial set
		if(k>lst1.size()){
			return null;
		}
		
		//initializing output
		setOut = new HashSet<HashSet<String>>();
		
		//looping through subsets
		rgiSubset = this.NEXKSB(lst1.size(), k);
		while(rgiSubset[0]!=-9999){
			
			//initializing current set being output
			setCurrent = new HashSet<String>();
			
			//loading current set of species
			for(int j=0;j<rgiSubset.length;j++){
				setCurrent.add(lst1.get(rgiSubset[j]-1));
			}
			
			//saving current set of species
			setOut.add(setCurrent);
		
			//loading next subset
			rgiSubset = this.NEXKSB(lst1.size(), k);	
		}
		
		//returning result
		return setOut;
	}
	
	public int[] findNeighbor(int n, int k, int rgiInitialSubset[]){
		
		//rgiOut = output
		//i1 = current value being swapped
		//i2 = index of first element being swapped
		//i3 = index of second element being swapped
		
		int i1; int i2; int i3;
		int rgiOut[];
		
		//initializing list of excluded values, if necessary
		if(rgiInitialSubset==null){
			
			rgiOut = new int[k];
			for(int i=1;i<=k;i++){
				rgiOut[i-1]=i;
			}
			rgiExcluded = new int[n-k];
			for(int i=k+1;i<=n;i++){
				rgiExcluded[i-k-1]=i;
			}
		}else{
		
			rgiOut = new int[k];
			for(int i=0;i<k;i++){
				rgiOut[i]=rgiInitialSubset[i];
			}
			i2 = (int) Math.floor(Math.random()*k);
			i3 = (int) Math.floor(Math.random()*(n-k));
			i1 = rgiOut[i2];
			rgiOut[i2] = rgiExcluded[i3];
			rgiExcluded[i3]=i1;
		}
		return rgiOut;
	}
	
	public int[] findNextAddOneSubset(int n, int[] rgiInitialSubset){
		
		boolean bDone = true;
		
		//initializing if appropriate
		if(iIndex==0){
			
			if(rgiInitialSubset!=null){
				lst1 = new ArrayList<Integer>(rgiInitialSubset.length);
				rgi1 = new int[rgiInitialSubset.length+1];
				for(int i=0;i<rgiInitialSubset.length;i++){
					rgi1[i]=rgiInitialSubset[i];
					lst1.add(rgi1[i]);
				}
				i1=rgiInitialSubset.length;
			}else{
				lst1 = new ArrayList<Integer>();
				rgi1 = new int[1];
				i1=0;
			}
		}
		
		//looping through values until new value found
		for(int i=iIndex;i<n;i++){
			if(!lst1.contains(i+1)){
				iIndex=i+1;
				rgi1[i1]=i+1;
				lst1.add(i+1);
				bDone = false;
				break;
			}
		}
		
		//returning result
		if(bDone==true){
			rgi1[0]=-9999;
		}
		return rgi1;
	}
	
	/**
	 * Finds next k-subset from integers 1,...,n
	 * @param n Total number of objects.
	 * @param k Number of objects being chosen.
	 * @return Current subset
	 */
	public int[] NEXKSB(int n, int k){
		
		//rgi1 = output
		int rgi1[];
		
		//checking if done
		if(bFinal==true){
			rgi1 = new int[1];
			rgi1[0] = -9999;
			return rgi1;
		}
		
		if(m==-9999){
			
			//A
			m = 0;
			h = k;
			a = new HashMap<Integer,Integer>();
		}else{
			
			//B
			if(m<n-h){
				h=0;
			}
			
			//C
			h++;
			m=a.get(k+1-h);
		
			
			/*
			//B
			if(m>=n-h){
			
				//C
				h++;
				m=a.get(k+1-h);
			}else{
				h=0;
			}
			*/
		}
		
		//D
		for(int j=1;j<=h;j++){
			a.put(k+j-h, m+j);
		}
		if(a.get(1)==n-k+1){
			bFinal = true;
		}
		
		//outputting result	
		rgi1 = new int[k];
		for(int j=0;j<k;j++){
			rgi1[j]=a.get(j+1);
		}
		return rgi1;
	}	
}

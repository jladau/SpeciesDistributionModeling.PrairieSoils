package edu.ucsf.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cluster object.  Clustering is read from file in format output by hcluster.
 * @author jladau
 */

public class Clustering {

	//mapCluster(iObject) = returns the cluster to which given object belongs
	//mapElements(iCluster) = returns the set of elements in the specified cluster
	//iClusters = number of clusters
	//bClusterError = true if specified number of clusters not found; false otherwise
	//sSuffix = suffix for clustering output ("upgma", "complete", or "single")
	//sDirHClusterOutput = directory with hcluster output
	
	private String sDirHClusterOutput;
	private int iClusters;
	public Map<Integer,Integer> mapCluster;
	public Map<Integer,Set<Integer>> mapElements;
	public boolean bClusterError;
	private String sSuffix;
	
	/**
	 * Constructor
	 * @param iClusters Number of clusters
	 * @param sDirDistances Directory with distances file
	 * @param sSuffix Clusterer suffix
	 * */
	public Clustering(int iClusters, String sDirHClusterOutput, String sSuffix){
		
		//loading values
		this.iClusters = iClusters;
		this.sDirHClusterOutput = sDirHClusterOutput;
		if(!this.sDirHClusterOutput.endsWith("/")){
			this.sDirHClusterOutput+="/";
		}
		
		this.sSuffix = sSuffix;
		
		//loading cluster map
		this.loadClusterMap();
		
		//loading elements map
		this.loadElementsMap();
	}
	
	/**
	 * Loads map giving the elements in each cluster
	 */
	private void loadElementsMap(){
		
		//set1 = current set being added
		//iCluster = current cluster
		
		Set<Integer> set1;
		int iCluster;
		
		//loading output map
		mapElements = new HashMap<Integer,Set<Integer>>();
		for(Integer i:mapCluster.keySet()){
			iCluster = mapCluster.get(i);
			if(!mapElements.containsKey(iCluster)){
				set1 = new HashSet<Integer>();
				mapElements.put(iCluster, set1);
			}
			mapElements.get(iCluster).add(i);
		}
	}
	
	/**
	 * Loads map linking objects to their clusters
	 */
	private void loadClusterMap(){
		
		//rgsFile = current file
		//dThreshold = threshold value for desired number of clusters
		//rgs2 = current row of classification
		//rgs3 = objects in current class
				
		double dThreshold = 0;
		String rgsFile[][]; String rgs2[] = null; String rgs3[];
		
		//initializing cluster map
		mapCluster = new HashMap<Integer,Integer>();
		
		//loading threshold for given number of clusters
		rgsFile = FileIO.readFile(sDirHClusterOutput + "distances" + sSuffix + ".OTU", " ");
		bClusterError = true;
		for(int i=1;i<rgsFile.length;i++){
			if(Integer.parseInt(rgsFile[i][1])==iClusters){
				dThreshold = Double.parseDouble(rgsFile[i][0]);
				bClusterError = false;
				break;
			}
		}
		
		//exiting if cluster error found
		if(bClusterError==true){
			return;
		}
		
		//loading assignments
		rgsFile = FileIO.readFile(sDirHClusterOutput + "distances" + sSuffix + ".Cluster", "fshfjkdshkja");
		
		//looping through rows until correct number of classes found
		for(int i=0;i<rgsFile.length;i++){
			rgs2 = rgsFile[i][0].replace("|",",").split(",");
			if(Double.parseDouble(rgs2[0])==dThreshold){
				//System.out.println(rgsFile[i][0]);
				break;
			}
		}
		
		//loading classification
		mapCluster = new HashMap<Integer,Integer>();
		for(int i=1;i<rgs2.length;i++){
			
			//loading current elements
			rgs3 = rgs2[i].split(" ");
			
			//saving elements
			for(int k=0;k<rgs3.length;k++){
				mapCluster.put(Integer.parseInt(rgs3[k]), i);
			}
		}
	}
	
}

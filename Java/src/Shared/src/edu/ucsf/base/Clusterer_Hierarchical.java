package edu.ucsf.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs hierarchical clustering using ESPRIT
 * @author jladau
 */
public class Clusterer_Hierarchical {

	//sPathHCluster = Path to hcluster binary
	//sFileDistances = Distances file
	//sDirDistances = Directory with distances
	//mapClass(sObject) = returns class for given object
	
	private String sPathHCluster;
	private String sFileDistances;
	private String sDirDistances;
	public Map<String,String> mapClass;
	
	/**
	 * Constructor
	 * @param sPathHCluster Path to hcluster binary
	 * @param sFileDistances Distances file
	 * @param sDirDistances Directory with distances file
	 */
	public Clusterer_Hierarchical(String sPathHCluster, String sDirDistances, String sFileDistances){
		
		//saving paths
		this.sPathHCluster = sPathHCluster;
		this.sFileDistances = sFileDistances;
		this.sDirDistances = sDirDistances;
	}
	
	/**
	 * Runs classifier
	 * @param dSimilarity Similarity for clustering
	 */
	
	public void runHCluster(){
		
		//running complete linkage classification using ESPRIT
		runCommand("mv " + sDirDistances + "/" + sFileDistances + " " + sDirDistances + "/" + sFileDistances.replace(".dist", "_complete.dist"));
		runCommand(sPathHCluster + " -p 0 -s 0.001 " + sDirDistances + "/" + sFileDistances.replace(".dist", "_complete.dist"));
		
		//running single linkage clustering using ESPRIT
		runCommand("mv " + sDirDistances + "/" + sFileDistances.replace(".dist", "_complete.dist") + " " + sDirDistances + "/" + sFileDistances.replace(".dist", "_single.dist"));
		runCommand(sPathHCluster + " -p 1 -s 0.001 " + sDirDistances + "/" + sFileDistances.replace(".dist", "_single.dist"));
		
		//restoring original distances file
		runCommand("mv " + sDirDistances + "/" + sFileDistances.replace(".dist", "_single.dist") + " " + sDirDistances + "/" + sFileDistances);
	}
	
	/**
	 * Runs UPGMA clustering
	 */
	public void runUPGMA(){
		
		//dst1 = DistanceMatrix object
		//rgsDistances = distances matrix
		//lstOut = output
		//iCounter = counter for output
		
		String rgsDistances[][];
		DistanceMatrix dst1;
		ArrayList<String> lstOut;
		int iCounter;
		
		//loading distances matrix
		rgsDistances = FileIO.readFile(sDirDistances + "/" + sFileDistances," ");
		
		//initializing distance matrix object
		dst1 = new DistanceMatrix("average");
		for(int i=0;i<rgsDistances.length;i++){
			dst1.loadDistance(Integer.parseInt(rgsDistances[i][0]), Integer.parseInt(rgsDistances[i][1]), Double.parseDouble(rgsDistances[i][2]));
		}
		
		//looping and outputting results
		lstOut = new ArrayList<String>();
		iCounter = 0;
		do{
			
			//updating progress
			System.out.println("Analyzing clustering " + (iCounter+1) + "...");
			
			//saving current cluster
			lstOut.add(" " + iCounter + " " + dst1.printCurrentClustering());
			
			//merging closest clusters
			dst1.mergeClosestObjects();
			
			//updating counter
			iCounter++;
			
		}while(dst1.mapCluster.size()>1);
		
		//saving final clustering
		lstOut.add(" " + iCounter + " " + dst1.printCurrentClustering());
		
		//outputting results
		FileIO.writeFile(lstOut, sDirDistances + "/distances_upgma.Cluster", 0, false);
		
		//outputting OTU file
		lstOut = new ArrayList<String>();
		lstOut.add("Dist NumClusters");
		for(int i=0;i<=iCounter;i++){
			lstOut.add(" " + i + " " + (iCounter-i+1));
		}
		FileIO.writeFile(lstOut, sDirDistances + "/distances_upgma.OTU", 0, false);
	}
	
	public void loadClassificationSimilarityThreshold(double dSimilarity){
		
		//prc1 = ESPRIT process for classification
		//rgs1 = classification
		//rgs2 = current row of classification
		//rgs3 = objects in current class
		
		String rgs1[][]; String rgs2[] = null; String rgs3[];
		
		//loading output
		rgs1 = FileIO.readFile(sDirDistances + "/" + sFileDistances.replace(".dist", ".Cluster"), "fshfjkdshkja");
		
		//looping through rows until correct number of classes found
		for(int i=0;i<rgs1.length;i++){
			rgs2 = rgs1[i][0].replace("|",",").split(",");
			if(Double.parseDouble(rgs2[0])>=dSimilarity){
				System.out.println(rgs1[i][0]);
				break;
			}
		}
		
		//loading classification
		mapClass = new HashMap<String,String>();
		for(int i=1;i<rgs2.length;i++){
			
			//loading current elements
			rgs3 = rgs2[i].split(" ");
			
			//saving elements
			for(int k=0;k<rgs3.length;k++){
				mapClass.put(rgs3[k], Integer.toString(i));
			}
		}
		
	}
	
	public void loadClassificationClustersThreshold(int iClusters, String sSuffix){
		
		//prc1 = ESPRIT process for classification
		//rgs1 = classification
		//rgs2 = current row of classification
		//rgs3 = objects in current class
		
		String rgs1[][]; String rgs2[] = null; String rgs3[];
		
		//loading output
		rgs1 = FileIO.readFile(sDirDistances + "/" + sFileDistances.replace(".dist", sSuffix + ".Cluster"), "fshfjkdshkja");
		
		//looping through rows until correct number of classes found
		for(int i=0;i<rgs1.length;i++){
			rgs2 = rgs1[i][0].replace("|",",").split(",");
			if(rgs2.length==iClusters+1){
				System.out.println(rgs1[i][0]);
				break;
			}
		}
		
		//loading classification
		mapClass = new HashMap<String,String>();
		for(int i=1;i<rgs2.length;i++){
			
			//loading current elements
			rgs3 = rgs2[i].split(" ");
			
			//saving elements
			for(int k=0;k<rgs3.length;k++){
				mapClass.put(rgs3[k], Integer.toString(i));
			}
		}
		
	}
	

	private void runCommand(String sCommand){
		
		Process prc1;
	
		try {
			prc1 = Runtime.getRuntime().exec(sCommand);
			try {
				prc1.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

package edu.ucsf.base;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes silhouette scores for clustering output.  Output files must be as those created by hcluster.
 * @author jladau
 */
public class ClusteringDiagnostics {

	//mapSilhouette(iObject) = returns the silhouette value for the given object
	//cls1 = Clustering object
	//sFileDistances = file with distances
	
	private String sFileDistances;
	private Map<Integer,Double> mapSilhouette;
	private Clustering cls1;
	
	
	/**
	 * Constructor
	 * @param iClusters Number of clusters for which to generate output
	 * @param sFileDistances Distances file
	 * @param sDirDistances Directory with distances file
	 * */
	public ClusteringDiagnostics(int iClusters, String sDirHClusterOutput, String sFileDistances, String sSuffix){
		
		//loading distances path
		this.sFileDistances = sFileDistances;
		
		//loading clustering object
		cls1 = new Clustering(iClusters, sDirHClusterOutput, sSuffix);
		
		//checking for error
		if(cls1.bClusterError==true){
			return;
		}
		
		//loading silhouette map
		try {
			this.loadSilhouetteMap();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints distances heatmap
	 * @param sPathOutput Output path for image (.png)
	 * @param sTitle Title
	 */
	public void printDistanceHeatMap(String sPathOutput, String sTitle){
	
		//rgd1 = distances matrix
		//mapRow(iObject) = row in distances matrix for object
		//mapCol(iObject) = column in distances matrix for object
		//i1 = counter for output row and column maps
		//iTotal = total number of objects
		//bfr1 = buffered reader
		//s1 = current line
		//rgs1 = current line in split format
		//iObject1 = current first object
		//iObject2 = current second object
		//dDistance = current distance
		//dhc1 = DrawHeatChart object
		//rgoXValues = x axis tick labels
		//rgoYVlaues = y axis tick labels
		
		Object rgoXValues[]; Object rgoYValues[];
		Map<Integer,Integer> mapRow; Map<Integer,Integer> mapCol;
		double rgd1[][];
		int i1; int iTotal; int iObject1; int iObject2;
		String rgs1[];
		String s1;
		BufferedReader bfr1;
		double dDistance;
		DrawHeatChart dhc1;
		
		//loading row and column maps axis tick labels
		i1=0;
		iTotal = cls1.mapCluster.size();
		rgoXValues = new Object[iTotal];
		rgoYValues = new Object[iTotal];
		mapRow = new HashMap<Integer,Integer>();
		mapCol = new HashMap<Integer,Integer>();
		for(Integer i:cls1.mapElements.keySet()){
			for(Integer j:cls1.mapElements.get(i)){
				
				//updating dictionaries
				mapRow.put(j, iTotal-i1-1);
				mapCol.put(j, i1);
			

				//no cluster division
				rgoXValues[i1]="";
				rgoYValues[iTotal-i1-1]="";
				
				//updating counter
				i1++;
				
			}
			
			//putting cluster division
			if(i1<iTotal){
				rgoXValues[i1-1]="*";
				rgoYValues[iTotal-i1]="*";
			}
		}
	
		//loading distance matrix
		rgd1 = new double[iTotal][iTotal];
		try{
			bfr1 = new BufferedReader(new FileReader(sFileDistances));
			while((s1 = bfr1.readLine()) != null){
				
				//splitting current line
				rgs1 = s1.split(" ");
				
				//loading objects and distance
				iObject1 = Integer.parseInt(rgs1[0]);
				iObject2 = Integer.parseInt(rgs1[1]);
				dDistance = Double.parseDouble(rgs1[2]);
				
				//saving distance
				rgd1[mapRow.get(iObject1)][mapCol.get(iObject2)]=dDistance;
				rgd1[mapRow.get(iObject2)][mapCol.get(iObject1)]=dDistance;
			}
		}catch(Exception e){
		}
		
		dhc1 = new DrawHeatChart(rgd1,rgoXValues,rgoYValues,sTitle,"","");
		dhc1.PrintChart(sPathOutput);
	}
	
	/**
	 * Finds mean 
	 * @return
	 */
	
	public double findMeanSilhouette(){
		
		//dTotal = total silhouette value
		
		double dTotal;
		
		//checking for error
		if(cls1.bClusterError==true){
			return -9999.;
		}
		
		//loading mean
		dTotal = 0.;
		for(Integer i:this.mapSilhouette.keySet()){
			dTotal+=mapSilhouette.get(i);
		}
		
		//returning result
		return dTotal/((double) mapSilhouette.size());
	}
	
	/**
	 * Finds silhouette graph, not sorted
	 * @return Silhouette graph: first column with object id, second column with cluster, third column with silhouette score
	 */
	public String[][] printSilhouetteGraph(){
		
		//rgsOut = output
		//iRow = output row
		
		int iRow;
		String rgsOut[][];
		
		rgsOut = new String[mapSilhouette.size()+1][1];
		rgsOut[0][0] = "OBJECT_ID,CLUSTER,SILHOUETTE_VALUE";
		iRow=1;
		for(Integer i:mapSilhouette.keySet()){
			rgsOut[iRow][0]=i + "," + cls1.mapCluster.get(i) + "," + mapSilhouette.get(i);
			iRow++;
		}
		return rgsOut;
	}
	
	/**
	 * Loads map giving the silhouette score for a given object
	 * @throws IOException 
	 */
	private void loadSilhouetteMap() throws IOException{
		
		//bfr1 = buffered reader
		//s1 = current line
		//rgs1 = current line in split format
		//iObject1 = current first object
		//iObject2 = current second object
		//iCluster1 = cluster of first object
		//iCluster2 = cluster of second object
		//dDistance = current distance
		//mapTotal(iObject) = returns map which gives total distances (keys are clusters)
		//dTotal = current total
		//mapCount(iCluster) = returns the total number of objects in cluster
		//dA = current mean within cluster distance
		//dB = current mean minimum between cluster distance
		//dMean = current mean
		
		String rgs1[];
		String s1;
		BufferedReader bfr1;
		int iObject1; int iObject2; int iCluster1; int iCluster2;
		double dDistance; double dTotal; double dA; double dB; double dMean;
		Map<Integer,Map<Integer,Double>> mapTotal; Map<Integer,Double> mapCount;
		
		//initializing total map
		mapTotal = new HashMap<Integer,Map<Integer,Double>>(); 
		
		//looping through lines of distance file
		bfr1 = new BufferedReader(new FileReader(sFileDistances));
		while((s1 = bfr1.readLine()) != null){
			
			//splitting current line
			rgs1 = s1.split(" ");
			
			//loading objects and distance
			iObject1 = Integer.parseInt(rgs1[0]);
			iObject2 = Integer.parseInt(rgs1[1]);
			dDistance = Double.parseDouble(rgs1[2]);
			
			//loading clusters of objects
			iCluster1 = cls1.mapCluster.get(iObject1);
			iCluster2 = cls1.mapCluster.get(iObject2);
			
			//checking if appropriate object-cluster combinations initialized
			if(!mapTotal.containsKey(iObject1)){
				mapTotal.put(iObject1, new HashMap<Integer,Double>());
			}
			if(!mapTotal.get(iObject1).containsKey(iCluster2)){
				mapTotal.get(iObject1).put(iCluster2,0.);
			}
			if(!mapTotal.containsKey(iObject2)){
				mapTotal.put(iObject2, new HashMap<Integer,Double>());
			}
			if(!mapTotal.get(iObject2).containsKey(iCluster1)){
				mapTotal.get(iObject2).put(iCluster1,0.);
			}
			
			//updating maps
			dTotal = mapTotal.get(iObject1).get(iCluster2);
			dTotal+=dDistance;
			mapTotal.get(iObject1).put(iCluster2, dTotal);
			dTotal = mapTotal.get(iObject2).get(iCluster1);
			dTotal+=dDistance;
			mapTotal.get(iObject2).put(iCluster1, dTotal);
		}
		bfr1.close();
		
		//loading counts
		mapCount = new HashMap<Integer,Double>();
		for(Integer i:cls1.mapCluster.keySet()){
			iCluster1 = cls1.mapCluster.get(i);
			if(!mapCount.containsKey(iCluster1)){
				mapCount.put(iCluster1, 0.);
			}
			dTotal = mapCount.get(iCluster1);
			dTotal+=1.;
			mapCount.put(iCluster1,dTotal);
		}
		
		//initializing silhouette map
		mapSilhouette = new HashMap<Integer,Double>();
		
		//looping through objects
		for(Integer iObject:mapTotal.keySet()){
			
			//initializing means
			dA = 0;
			dB = 999999999999999999999999999.;
			
			//loading cluster of current object
			iCluster1 = cls1.mapCluster.get(iObject);
			
			//looping through counts for each cluster
			for(Integer iCluster:mapTotal.get(iObject).keySet()){
				
				//checking if cluster is the cluster of current object
				if(iCluster==iCluster1){
					dMean = mapTotal.get(iObject).get(iCluster)/(mapCount.get(iCluster)-1.);
					dA = dMean;
				}else{
					dMean = mapTotal.get(iObject).get(iCluster)/mapCount.get(iCluster);
					if(dMean<dB){
						dB=dMean;
					}
				}
			}
			
			//saving silhouette value
			mapSilhouette.put(iObject, (dB-dA)/Math.max(dA,dB));
		}
	}
}

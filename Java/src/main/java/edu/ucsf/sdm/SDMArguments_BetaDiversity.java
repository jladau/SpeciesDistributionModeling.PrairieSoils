package edu.ucsf.sdm;

import java.util.ArrayList;

/**
 * Community observation data for beta diversity analysis
 * @author jladau
 */
public class SDMArguments_BetaDiversity extends SDMArguments{

	//dDistance = distance
	//sDirectionality = indicator for type of response variable. "directional" or "nondirectional"
	//sSingleLocationDataPath = path to file with sample locations
	//lstMapType = type of map for given analysis: "community novelty," "vector," "community classification," or "local turnover"
	//sPathHCluster = path to hcluster
	//dHierarchicalSim = similarity value to use for hierarchical clustering
	//iHierarchicalLocations = number of locations to be classified using hierarchical clustering (other locations are clustered using k-means)
	//sHClusterOutputDirectory = h-cluster output directory
	//iHierarchicalClusters = number of clusters to use
	
	public String sHClusterOutputDirectory;
	public String sPathHCluster;
	public double dDistance;
	public ArrayList<String> lstPredictorFunctions; 
	public String sDirectionality;
	@Deprecated
	public String sSingleLocationDataPath;
	public String sMapType;
	public double dHierarchicalSim;
	public int iHierachicalLocations;
	public int iHierarchicalClusters;
	
	/**
	 * Constructor
	 */
	public SDMArguments_BetaDiversity(){
		
		super();
		
		//initializing additional variable lists
		lstPredictorFunctions = new ArrayList<String>();
	}
	
	/**
	 * Loads argument (value is string)
	 * @param sName Name of argument
	 * @param sValue Value of argument
	 */
	public void loadArgument(String sName, String sValue){
	
		//calling superclass
		super.loadArgument(sName,sValue);
		
		//loading additional arguments
		if(sName.equals("hcluster output directory")){
			sHClusterOutputDirectory = sValue;
		}else if(sName.equals("number of hierarchical clustering locations")){
			iHierachicalLocations = Integer.parseInt(sValue);
		}else if(sName.equals("hierarchical clustering similarity")){
			dHierarchicalSim = Double.parseDouble(sValue);
		}else if(sName.equals("map type")){
			sMapType = sValue;
		}else if(sName.equals("single location data path")){
			sSingleLocationDataPath = sValue;
		}else if(sName.equals("number of clusters")){
			iHierarchicalClusters = Integer.parseInt(sValue);
		}else if(sName.equals("directionality")){
			
			//loading list of candidate variables
			sDirectionality = sValue;
		}else if(sName.equals("hcluster path")){
			sPathHCluster = sValue;
		}else if(sName.equals("distance")){	

			//loading response variable
			dDistance = Double.parseDouble(sValue);
		}	
	}
	
	/**
	 * Loads current predictors in list format
	 */
	protected void loadPredictorList(String sValue){
		
		//rgs1 = predictors in array format
		//lst1 = current list of files being added
		//lst2 = current list of functions being added
		
		String rgs1[];
		ArrayList<String> lst1; ArrayList<String> lst2;
		
		rgs1 = sValue.split(",");
		lst1 = new ArrayList<String>(rgs1.length);
		lst2 = new ArrayList<String>(rgs1.length);
		for(int i=0;i<rgs1.length;i++){
			lst1.add(rgs1[i].split("_")[0] + ".nc");
			lst2.add(rgs1[i].split("_")[1]);
		}
		lstPredictorFiles = lst1;
		lstPredictorFunctions = lst2;
	}
}

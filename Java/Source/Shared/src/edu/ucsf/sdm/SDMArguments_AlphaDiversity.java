package edu.ucsf.sdm;

import java.util.ArrayList;

/**
 * Community observation data for beta diversity analysis
 * @author jladau
 */
public class SDMArguments_AlphaDiversity extends SDMArguments{
	
	/**
	 * Constructor
	 */
	public SDMArguments_AlphaDiversity(){
		
		super();
		
		//initializing additional variable lists
		
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
	}
	
	/**
	 * Loads current predictors in list format
	 */
	protected void loadPredictorList(String sValue){
		
		//rgs1 = predictors in array format
		//lst1 = current list of files being added
		
		String rgs1[];
		ArrayList<String> lst1;
		
		rgs1 = sValue.split(",");
		lst1 = new ArrayList<String>(rgs1.length);
		for(int i=0;i<rgs1.length;i++){
			lst1.add(rgs1[i].split("_")[0] + ".nc");
		}
		lstPredictorFiles = lst1;
	}
}

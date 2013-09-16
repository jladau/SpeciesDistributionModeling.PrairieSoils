package edu.ucsf.sdm;

/**
 * Runs SDM analysis with given inputs.
 * @author jladau
 */
public class PrintPredictionMap_AlphaDiversity extends PrintPredictionMap{

	/**
	 * Constructor
	 */
	public PrintPredictionMap_AlphaDiversity(SDM sdm1){
		super(sdm1);
	}
	
	/**
	 * Prints map for specified model
	 * @param iTaskID Task ID for parallel processing; if -9999 complete map is output
	 * @param iTotalTasks Total number of tasks
	 */
	public void printMap(int iTaskID, int iTotalTasks){
		
		//complete map being output
		if(iTaskID==-9999){
			printNonVectorMap(iTaskID,"AlphaDiversity");
			
		//partial map being output	
		}else{
			printNonVectorMapPartial(iTaskID,iTotalTasks,"AlphaDiversity");
		}
	}
}
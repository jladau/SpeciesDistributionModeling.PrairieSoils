package edu.ucsf.sdm;

import java.util.HashMap;
import java.util.Map;

/**
 * Raster data for beta diversity analysis
 * @author jladau
 */
public class SDMRasterData_AlphaDiversity extends SDMRasterData{

	/**
	 * Constructor
	 * @param bdo1 BetaDiversityObservationalData object
	 */
	public SDMRasterData_AlphaDiversity(SDMArguments_AlphaDiversity arg1){
		
		//sVar = current variable name
		//map1 = current map being added to variable map
		
		//calling super
		super();
		
		String sVar;
		Map<Integer,String> map1;
	
		//initializing current variable map
		map1 = new HashMap<Integer,String>();
		for(int i=0;i<arg1.rgsPredictors.length;i++){
		
			//loading variable name
			sVar = arg1.rgsPredictors[i];
			
			//saving results as necessary
			if(!mapPath.containsKey(sVar)){
				mapPath.put(sVar, arg1.sDirRasters + "/" + arg1.lstPredictorFiles.get(i));
			}
			
			//saving variable name
			map1.put(i, sVar);
		}
		
		//saving results to variable map
		mapVar = map1;
	}
}
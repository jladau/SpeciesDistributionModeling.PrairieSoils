package edu.ucsf.sdm;

import edu.ucsf.base.Arguments;

/**
 * Contains four elements for sdm analysis: sdm object, raster object, observation object, and model object
 * @author jladau
 *
 */

public class SDM_BetaDiversity extends SDM{
	
	public SDM_BetaDiversity(){
		arg1 = new SDMArguments_BetaDiversity();
	}
	
	public void loadUncompiledData(Arguments arg1){
		unc1 = new SDMUncompiledData_BetaDiversity(arg1);
	}
	
	public void loadObservationalData(){
		obs1 = new SDMObservationalData_BetaDiversity((SDMArguments_BetaDiversity) arg1);
	}
	
	public void loadRasterData(){
		ras1 = new SDMRasterData_BetaDiversity((SDMArguments_BetaDiversity) arg1);
	}
	
	public void loadModel(){
		mdl1 = new SDMModel_BetaDiversity((SDMObservationalData_BetaDiversity) obs1, (SDMRasterData_BetaDiversity) ras1, (SDMArguments_BetaDiversity) arg1);
	}
	
	public void loadMESS(){
		mss1 = new SDMMESS_BetaDiversity((SDMObservationalData_BetaDiversity) obs1,(SDMArguments_BetaDiversity) arg1,(SDMRasterData_BetaDiversity) ras1);
	}
	
	public void loadPredictions(){
		prd1 = new SDMPrediction_BetaDiversity((SDMArguments_BetaDiversity) arg1,(SDMModel_BetaDiversity) mdl1,(SDMRasterData_BetaDiversity) ras1,(SDMObservationalData_BetaDiversity) obs1); 
	}
}

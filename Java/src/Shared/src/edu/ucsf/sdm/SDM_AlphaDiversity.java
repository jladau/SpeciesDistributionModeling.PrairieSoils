package edu.ucsf.sdm;

import edu.ucsf.base.Arguments;

/**
 * Contains four elements for sdm analysis: sdm object, raster object, observation object, and model object
 * @author jladau
 *
 */

public class SDM_AlphaDiversity extends SDM{
	
	public SDM_AlphaDiversity(){
		arg1 = new SDMArguments_AlphaDiversity();
	}
	
	public void loadUncompiledData(Arguments arg1){
		unc1 = new SDMUncompiledData_AlphaDiversity(arg1);
	}
	
	public void loadObservationalData(){
		obs1 = new SDMObservationalData_AlphaDiversity((SDMArguments_AlphaDiversity) arg1);
	}
	
	public void loadRasterData(){
		ras1 = new SDMRasterData_AlphaDiversity((SDMArguments_AlphaDiversity) arg1);
	}
	
	public void loadModel(){
		mdl1 = new SDMModel_AlphaDiversity((SDMObservationalData_AlphaDiversity) obs1, (SDMRasterData_AlphaDiversity) ras1, (SDMArguments_AlphaDiversity) arg1);
	}
	
	public void loadMESS(){
		mss1 = new SDMMESS_AlphaDiversity((SDMObservationalData_AlphaDiversity) obs1,(SDMArguments_AlphaDiversity) arg1,(SDMRasterData_AlphaDiversity) ras1);
	}
	
	public void loadPredictions(){
		prd1 = new SDMPrediction_AlphaDiversity((SDMArguments_AlphaDiversity) arg1,(SDMModel_AlphaDiversity) mdl1,(SDMRasterData_AlphaDiversity) ras1,(SDMObservationalData_AlphaDiversity) obs1); 
	}
}

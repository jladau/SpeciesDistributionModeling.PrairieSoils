package edu.ucsf.sdm;

import edu.ucsf.base.Arguments;

public abstract class SDM {

	//unc1 = uncompiled data object
	//arg1 = arguments
	//ras1 = rasters
	//obs1 = community observations
	//mdl1 = model
	//mss1 = mess object
	//prd1 = prediction object
	
	public SDMUncompiledData unc1;
	public SDMArguments arg1;
	public SDMRasterData ras1;
	public SDMObservationalData obs1;
	public SDMModel mdl1;
	public SDMMESS mss1;
	public SDMPrediction prd1;
	
	public abstract void loadUncompiledData(Arguments arg1);
	
	public abstract void loadObservationalData();
	
	public abstract void loadRasterData();
	
	public abstract void loadModel();
	
	public abstract void loadMESS();
	
	public abstract void loadPredictions();
}

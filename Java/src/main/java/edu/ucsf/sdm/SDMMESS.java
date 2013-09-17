package edu.ucsf.sdm;

import edu.ucsf.base.SphericalGeometry;

public abstract class SDMMESS {

	/** draws diversity map using the model fit in the given AssembleFirst object */
	
	//rgdMap = map
	//sph1 = spherical geometry object
	
	public double rgdMap[][];
	public SphericalGeometry sph1;
	
	/**
	 * Constructor
	 */
	public SDMMESS(){
			
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
	}
	
	/**
	 * Loads mess map
	 * @param iFixedPredictorsIndex Index of fixed predictors currently under consideration
	 * @param iVariable Index of variable in arguments.rgsPredictors array
	 */
	public abstract void loadMESSMap(int iFixedPredictorsIndex, int iVariable);
}
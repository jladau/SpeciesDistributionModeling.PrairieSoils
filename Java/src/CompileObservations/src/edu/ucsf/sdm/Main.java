package edu.ucsf.sdm;

import edu.ucsf.base.*;

/**
 * This code compiles observations for species distribution modeling analysis.
 * @author jladau
 */
public class Main {

	public static void main(String rgsArgs[]){
	
		//arg1 = arguments object
		//sdm1 = sdm object
		
		Arguments arg1;
		SDM sdm1 = null;
		
		//loading arguments object
		arg1 = new Arguments(rgsArgs);
		
		//loading sdm object
		if(arg1.getValueString("sAnalysisMode").equals("beta-diversity")){
			sdm1 = new SDM_BetaDiversity();
		}else if(arg1.getValueString("sAnalysisMode").equals("alpha-diversity")){
			sdm1 = new SDM_AlphaDiversity();
		}
		
		//loading raster data object
		sdm1.loadUncompiledData(arg1);
		
		//compiling data
		sdm1.unc1.compileData();
		
		//printing output
		FileIO.writeFile(sdm1.unc1.rgsMetadata, arg1.getValueString("sOutputPath"), ",", 0, false);
		
		//terminating
		//FileIO.writeCompletionFile(arg1.getValueString("sOutputPath"));
		if(arg1.getValueString("sOutputPath").endsWith(".idat")){	
			arg1.printArguments(arg1.getValueString("sOutputPath").replace(".idat", ".log"), true, "CompileObservations");
		}else if(arg1.getValueString("sOutputPath").endsWith(".data")){
			arg1.printArguments(arg1.getValueString("sOutputPath").replace(".data", ".log"), true, "CompileObservations");
		}
		System.out.println("Done.");
	}
}
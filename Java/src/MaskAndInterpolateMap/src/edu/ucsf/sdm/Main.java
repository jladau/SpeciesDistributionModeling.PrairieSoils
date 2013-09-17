package edu.ucsf.sdm;

import edu.ucsf.base.*;

/**
 * This code interpolates specified map and also masks it if requested
 * @author jladau
 */
public class Main {

	public static void main(String rgsArgs[]){
		
		//cdf1 = current netcdf file
		//ipl1 = InterpolateGrid object
		//arg1 = argument values
		//cdfWriter = output cdf
		//dVert = current vert value
		//dTime = current time value
		//sPathRaster = raster path
		
		Arguments arg1;
		InterpolateAndMaskGrid ipl1;
		NetCDF_IO cdf1;
		NetCDF_IO cdfWriter;
		double dVert; double dTime;
		String sPathRaster;
		
		//loading arguments
		arg1 = new Arguments(rgsArgs);
		
		//loading raster path
		if(arg1.mapAllArguments.containsKey("sPathRaster")){
			sPathRaster = arg1.getValueString("sPathRaster");
		}else{
			sPathRaster = arg1.getValueString("sDataPath").replace(".data",".nc");
		}
		
		//initializing interpolategrid object
		ipl1 = new InterpolateAndMaskGrid(arg1.getValueString("sPathMask"));
		
		//waiting for file completion
		//FileIO.checkAndWaitForCompletion(sPathRaster);
		
		//loading grid
		cdf1 = new NetCDF_IO(sPathRaster, "reading");
		
		//initializing output
		cdfWriter = new NetCDF_IO(sPathRaster.replace(".nc", ".nc.temp"),"writing");
		try{
			cdf1.getDimensionValues("vert");
		}catch(Exception e){
			FileIO.writeCompletionFile(sPathRaster.replace(".nc", ".nc.interpolated"));
			arg1.printArguments(arg1.getValueString("sDataPath").replace(".data",".log"), true, "MaskAndInterpolateMap");
			System.out.println("Done.");
			return;
		}
		cdfWriter.initializeWriterStringLists(arg1.getValueDouble("dResolution"), "Meters", cdf1.getDimensionValues("vert"), "Month", cdf1.getDimensionValues("time"), cdf1.getVariableName(), cdf1.getUnits());
		
		//looping through times and elevations
		for(int i=0;i<cdf1.getDimensionValues("vert").size();i++){
			for(int j=0;j<cdf1.getDimensionValues("time").size();j++){
				
				//loading time and vert value
				dVert = Double.parseDouble(cdf1.getDimensionValues("vert").get(i));
				dTime = Double.parseDouble(cdf1.getDimensionValues("time").get(j));
				
				//outputting results
				cdfWriter.writeGrid(ipl1.interpolateGrid(dVert, dTime, cdf1, arg1.getValueDouble("dResolution")), dVert, dTime);
			}
		}
	
		//closing writer
		cdfWriter.closeWriter();
		
		//moving file
		FileIO.moveFile(sPathRaster.replace(".nc", ".nc.temp"),sPathRaster);
		
		//terminating
		//FileIO.writeCompletionFile(sPathRaster.replace(".nc", ".nc.interpolated"));
		arg1.printArguments(arg1.getValueString("sDataPath").replace(".data",".log"), true, "MaskAndInterpolateMap");
		System.out.println("Done.");
	}
}

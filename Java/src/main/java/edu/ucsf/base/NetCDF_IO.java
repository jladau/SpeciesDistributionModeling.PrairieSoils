package edu.ucsf.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;

/**
 * Wrapper for writing and reading geographic information from netCDF files.
 * @author jladau
 */
public class NetCDF_IO {
	
	//mapLat(dValue) = returns the index of the value for latitude
	//mapLon(dValue) = returns the index of the value for longitude
	//mapVert(dValue) = returns the index of the value for vert
	//mapTime(dValue) = returns the index of the value for time
	//var1 = variable of interest
	//ncf1 = NetCDF file for reading
	//ncf2 = NetCDF file for writing
	//rgiShape = shape (assuming extracting one record at a time)
	//sUnits = units of variable
	//sVarName = name of variable of interest
	//bVertTime = true if Elevation and time included in raster for reading; false otherwise
	
	private NetcdfFile ncf1 = null;
	private NetcdfFileWriteable ncf2 = null;
	private TreeMap<Double, Integer> mapLat = null;
	private TreeMap<Double, Integer> mapVert = null;
	private TreeMap<Double, Integer> mapTime = null;
	private TreeMap<Double, Integer> mapLon = null;
	private Variable var1 = null;
	private int rgiShape[];
	private String sUnits;
	private String sVarName;
	private boolean bVertTime;
	
	/**
	 * Constructor
	 * @param sPath Path to NetCDF file being read or written.  Use static method writeNetCDF for writing grids without a time and elevation component; use non-static method for writing grids with time and elevation component. 
	 * @param sIOMode "reading" for reading, "writing" for writing
	 */
	public NetCDF_IO(String sPath, String sIOMode){
		
		//netCDF_IO constructor
		
		if(sIOMode.equals("reading")){
			
			//opening file
			try {
				ncf1 = NetcdfFile.open(sPath);
				buildReaderFirstVar();
			} catch (IOException ioe) {
				System.out.println("ERROR.");
		    }
		}else if(sIOMode.equals("writing")){
			
			//creating file
			try {
				ncf2 = NetcdfFileWriteable.createNew(sPath, false);
			} catch (IOException ioe) {
				System.out.println("ERROR.");
		    }
		}
	}
	
	/**
	 * Closes NetCDF file.
	 */
	public void closeReader(){
		try{
		    this.ncf1.close();
		}catch (IOException ioe) {
		    System.out.println("ERROR: CloseCDF method.");
		}
	}

	/**
	 * Closes grid for writing.
	 */
	public void closeWriter(){
		//closing file
		try {
			ncf2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Looks up list of values for given dimension.
	 * @param sDimension Dimension to look up.
	 * @return List of values for dimension.
	 */
	public ArrayList<String> getDimensionValues(String sDimension){
		
		//lst1 = output
		//map1 = map with dimension values
		
		ArrayList<String> lst1;
		Map<Double,Integer> map1 = null;
		
		//initializing output
		lst1 = new ArrayList<String>();
		
		//loading appropriate map
		if(sDimension.equals("lat")){
			map1 = mapLat;
		}else if(sDimension.equals("lon")){
			map1 = mapLon;
		}else if(sDimension.equals("vert")){
			map1 = mapVert;
		}else if(sDimension.equals("time")){
			map1 = mapTime;
		}
		
		//looping through values and transferring to list
		for(double d : map1.keySet()){
			lst1.add(Double.toString(d));
		}
		
		//returning result
		return lst1;
	}
	
	/**
	 * Gets the units for the current response variable.
	 * @return Name of the units for the current response variable.
	 */
	public String getUnits(){
		return sUnits;
	}

	/**
	 * Gets the name of the current response variable.
	 * @return Name of the current response variable.
	 */
	public String getVariableName(){
		return sVarName;
	}
	
	/**
	 * Initializes writer that allows NetCDF to have data from different times and elevations
	 * @param dCellSize Cell size in degrees.
	 * @param sVarName Variable name.
	 * @param sElevationUnits Units for Elevation variable.
	 * @param sTimeUnits Units for time variable.
	 * @param sVarUnits Units for variable.
	 * @param lstElevations Elevation variable values.
	 * @param lstTimes Time variable values.
	 * @param sOutPath Output path.
	 */
	public void initializeWriterStringLists(double dCellSize, String sElevationUnits, ArrayList<String> lstElevations, String sTimeUnits, ArrayList<String> lstTimes, String sVarName, String sVarUnits){
		
		//lstElevationsDouble = elevations list in double format
		//lstTimesDouble = time list in double format
		
		ArrayList<Double> lstElevationsDouble;
		ArrayList<Double> lstTimesDouble;
		
		//loading array lists in double format
		lstElevationsDouble = new ArrayList<Double>();
		for(int i=0;i<lstElevations.size();i++){
			lstElevationsDouble.add(Double.parseDouble(lstElevations.get(i)));
		}
		lstTimesDouble = new ArrayList<Double>();
		for(int i=0;i<lstTimes.size();i++){
			lstTimesDouble.add(Double.parseDouble(lstTimes.get(i)));
		}
		
		//running double version
		this.initializeWriter(dCellSize, sElevationUnits, lstElevationsDouble, sTimeUnits, lstTimesDouble, sVarName, sVarUnits);
	}
	
	/**
	 * Initializes writer that allows NetCDF to have data from different times and elevations
	 * @param dCellSize Cell size in degrees.
	 * @param sVarName Variable name.
	 * @param sElevationUnits Units for Elevation variable.
	 * @param sTimeUnits Units for time variable.
	 * @param sVarUnits Units for variable.
	 * @param lstElevations Elevation variable values.
	 * @param lstTimes Time variable values.
	 * @param sOutPath Output path.
	 */
	public void initializeWriter(double dCellSize, String sElevationUnits, ArrayList<Double> lstElevations, String sTimeUnits, ArrayList<Double> lstTimes, String sVarName, String sVarUnits){
		
		//sVarName = name of variable
		//sVarUnits = units of variable
		//iLat = latitude dimension
		//iLng = longitude dimension
		//iVert = Elevation dimension
		//iTime = time dimension
		//rga1 = current data array
		//dimLat = latitude dimension
		//dimLng = longitude dimension
		//dimVert = vertical dimension
		//dimTime = time dimension
		//lst1 = list of dimensions
		//ary1 = current data being written
		//rgx1 = index array
		//rgiO = origin
		
		int iLat; int iLng; int iVert; int iTime;
		ArrayDouble rga1;
		ArrayList<Dimension> lst1;
		Index rgx1;
		int rgiO[];
		Dimension dimLat; Dimension dimLng; Dimension dimVert; Dimension dimTime;
		
		//loading variable name
		this.sVarName = sVarName;
		
		//loading latitude and longitude dimension
		iLat = (int) (180/dCellSize); iLng = (int) (360/dCellSize); iVert = lstElevations.size(); iTime = lstTimes.size();
		
		//adding dimensions
		dimLat = ncf2.addDimension("lat", iLat);
		dimLng = ncf2.addDimension("lon", iLng);
		dimVert = ncf2.addDimension("vert", iVert);
		dimTime = ncf2.addDimension("time", iTime);
		lst1 = new ArrayList<Dimension>();
		lst1.add(dimLng);
		ncf2.addVariable("lon",DataType.FLOAT, lst1);
		ncf2.addVariableAttribute("lon","units","degrees_east");
		lst1 = new ArrayList<Dimension>();
		lst1.add(dimLat);		
		ncf2.addVariable("lat",DataType.FLOAT, lst1);
		ncf2.addVariableAttribute("lat","units","degrees_north");
		lst1 = new ArrayList<Dimension>();
		lst1.add(dimVert);
		ncf2.addVariable("vert",DataType.FLOAT, lst1);
		ncf2.addVariableAttribute("vert","units",sElevationUnits);
		lst1 = new ArrayList<Dimension>();
		lst1.add(dimTime);
		ncf2.addVariable("time",DataType.FLOAT, lst1);
		ncf2.addVariableAttribute("time","units",sTimeUnits);
		
		//adding variables
		lst1 = new ArrayList<Dimension>();
		lst1.add(dimLat);
		lst1.add(dimLng);
		lst1.add(dimVert);
		lst1.add(dimTime);
		ncf2.addVariable(sVarName,DataType.FLOAT, lst1);
		ncf2.addVariableAttribute(sVarName,"units",sVarUnits);
		ncf2.addVariableAttribute(sVarName,"missing_value",-9999);
		
		//creating file
		try {
			ncf2.create();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//adding latitude data
		rga1 = new ArrayDouble.D1(iLat);
		rgx1 = rga1.getIndex();
		for(int i=0; i<iLat; i++) {
			rga1.setDouble(rgx1.set(i), -90 + dCellSize/2. + ((double) i)*dCellSize);
		}
		rgiO = new int[2];
		try {
			try {
				ncf2.write("lat", rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		
		//adding longitude data
		rga1 = new ArrayDouble.D1(iLng);
		rgx1 = rga1.getIndex();
		for(int i=0; i<iLng; i++) {
			rga1.setDouble(rgx1.set(i), -180 + dCellSize/2. + ((double) i)*dCellSize);
		}
		rgiO = new int[2];
		try {
			try {
				ncf2.write("lon", rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		
		//adding Elevation data
		mapVert = new TreeMap<Double,Integer>();
		rga1 = new ArrayDouble.D1(iVert);
		rgx1 = rga1.getIndex();
		for(int i=0; i<iVert; i++) {
			rga1.setDouble(rgx1.set(i), lstElevations.get(i));
			mapVert.put(lstElevations.get(i), i);
		}
		rgiO = new int[2];
		try {
			try {
				ncf2.write("vert", rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		
		//adding time data
		mapTime = new TreeMap<Double,Integer>();
		rga1 = new ArrayDouble.D1(iTime);
		rgx1 = rga1.getIndex();
		for(int i=0; i<iTime; i++) {
			rga1.setDouble(rgx1.set(i), lstTimes.get(i));
			mapTime.put(lstTimes.get(i), i);
		}
		rgiO = new int[2];
		try {
			try {
				ncf2.write("time", rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads value of current variable at the given value of the arguments
	 * @param dLat Latitude
	 * @param dLon Longitude
	 * @param dVert Elevation
	 * @param dTime Time
	 * @return Value at given arguments.
	 */
	public double readValue(double dLat, double dLon, double dVert, double dTime){
		
		//bClose = flag for whether to close file
		//i2 = lookup index of current argument
		//rgiOrigin = lookup array
		//ary1 = current data array
		
		int i1; int i2;
		int rgiOrigin[];
		Array ary1 = null;
		
		//loading arguments
		if(bVertTime==true){
			rgiOrigin = new int[4];
		}else{
			rgiOrigin = new int[2];
		}
		
		//loading latitude
		i1 = var1.findDimensionIndex("lat");
		i2 = lookupValue(mapLat, dLat);
		rgiOrigin[i1] = i2;
		
		//loading longitude
		i1 = var1.findDimensionIndex("lon");
		i2 = lookupValue(mapLon, dLon);
		rgiOrigin[i1] = i2;
		
		//checking if vert and time should be added
		if(bVertTime==true){
			
			//loading vert
			i1 = var1.findDimensionIndex("vert");
			if(mapVert.size()==1){
				i2 = 0;
			}else{
				i2 = lookupValue(mapVert, dVert);
			}
			rgiOrigin[i1] = i2;
			
			//loading time
			i1 = var1.findDimensionIndex("time");
			if(mapTime.size()==1){
				i2 = 0;
			}else{
				i2 = lookupValue(mapTime, dTime);
			}
			rgiOrigin[i1] = i2;
		}
		
		//looking up value and loading result in array format
		try {
			ary1 = var1.read(rgiOrigin, rgiShape).reduce();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		
		//outputting result
		return ary1.getDouble(0);
	}

	/**
	 * Reads value of current variable at the given value of the arguments
	 * @param sArgValues Arguments at which value should be obtained: e.g., "lat:30.25,lng:20.25,time:2."
	 * @return Value at given arguments.
	 */
	public double readValue0(String sArgValues){
		
		//rgs1 = list of arguments and values
		//bClose = flag for whether to close file
		//i2 = lookup index of current argument
		//rgiOrigin = lookup array
		//ary1 = current data array
		//sDim = current dimension
		//dValue = current value of variable
		//rgs2 = current variable value pair split
		
		String rgs1[]; String rgs2[];
		int i1; int i2;
		int rgiOrigin[];
		Array ary1 = null;
		String sDim;
		double dValue;
		
		//loading arguments
		rgs1 = sArgValues.split(",");
		
		//loading arguments
		if(bVertTime==true){
			rgiOrigin = new int[4];
		}else{
			rgiOrigin = new int[2];
		}
		for(int i=0;i<rgs1.length;i++){
			
			//loading variable and name
			rgs2 = rgs1[i].split(":");
			sDim = rgs2[0];
			dValue = Double.parseDouble(rgs2[1]);
			
			//checking for VertTime
			if(bVertTime==false && (sDim.equals("vert") || sDim.equals("time"))){
				continue;
			}
			
			//finding index of current variable
			i1 = var1.findDimensionIndex(sDim);
			
			//finding value for current variable
			if(sDim.equals("lat")){
				i2 = lookupValue(mapLat, dValue);
			}else if(sDim.equals("lon")){
				i2 = lookupValue(mapLon, dValue);
			}else if(sDim.equals("vert")){
				
				//checking if raster contains elevation data; otherwise taking data from only given elevation
				if(mapVert.size()==1){
					i2 = 0;
				}else{
					i2 = lookupValue(mapVert, dValue);
				}
			}else if(sDim.equals("time")){
				
				//checking if raster contains time data; otherwise taking data from only given time
				if(mapTime.size()==1){
					i2 = 0;
				}else{
					i2 = lookupValue(mapTime, dValue);
				}
			}else{
				i2 = -9999;
			}
			
			//loading lookup array
			rgiOrigin[i1] = i2;
		}
		
		//looking up value and loading result in array format
		try {
			ary1 = var1.read(rgiOrigin, rgiShape).reduce();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		
		//outputting result
		return ary1.getDouble(0);
	}
	
	/**
	 * Reads and returns entire grid.
	 * @param dCellSize Cell size in degrees.
	 * @param dElevation Elevation value to read from.
	 * @param dTime Time Value to read from.
	 * @return Grid in double format
	 */
	public double[][] readGrid(double dCellSize, double dElevation, double dTime){
		
		//rgdGrid = output
		//iLat = number of latitude values
		//iLng = number of longitude values
		//dLat = current latitude
		//dLng = current longitude
		
		double rgdGrid[][];
		int iLat; int iLng;
		double dLat; double dLng;
		
		//loading size of grid
		iLat = (int) (180/dCellSize);
		iLng = (int) (360/dCellSize);
		
		//initializing grid
		rgdGrid = new double[iLat][iLng];
		
		//looping through latitudes
		dLat = 90+dCellSize/2.;
		for(int i=0;i<360;i++){
			
			//updating latitude
			dLat-=dCellSize;
			
			//initializing longitude
			dLng = -180 - dCellSize/2.;
			
			//looping through longitude
			for(int j=0;j<720;j++){
				
				//updating longitude
				dLng+=dCellSize;
				
				//saving result
				rgdGrid[i][j]=this.readValue(dLat,dLng,dElevation,dTime);	
			}
		}
		
		//returning result
		return rgdGrid;
	}
	
	/**
	 * Reads and returns entire grid.
	 * @param dCellSize Cell size in degrees.
	 * @return Grid in double format
	 */
	public double[][] readGrid(double dCellSize){
		
		//rgdGrid = output
		//iLat = number of latitude values
		//iLng = number of longitude values
		//dLat = current latitude
		//dLng = current longitude
		
		double rgdGrid[][];
		int iLat; int iLng;
		double dLat; double dLng;
		
		//loading size of grid
		iLat = (int) (180/dCellSize);
		iLng = (int) (360/dCellSize);
		
		//initializing grid
		rgdGrid = new double[iLat][iLng];
		
		//looping through latitudes
		dLat = 90+dCellSize/2.;
		for(int i=0;i<360;i++){
			
			//updating latitude
			dLat-=dCellSize;
			
			//initializing longitude
			dLng = -180 - dCellSize/2.;
			
			//looping through longitude
			for(int j=0;j<720;j++){
				
				//updating longitude
				dLng+=dCellSize;
				
				//saving result
				rgdGrid[i][j]=this.readValue(dLat,dLng,-9999,-9999);	
			}
		}
		
		//returning result
		return rgdGrid;
	}
	
	/**
	 * Writes value to open writer
	 * @param rgdGrid Grid being written.
	 * @param dTime Time value.
	 * @param dElevation Elevation to write to.
	 */
	public void writeValue(int iRow, int iCol, double dElevation, double dTime, double dValue){
		
		//sVarName = name of variable
		//sVarUnits = units of variable
		//rga1 = current data array
		//ary1 = current data being written
		//rgx1 = index array
		//rgiO = origin
		
		ArrayDouble rga1;
		Index rgx1;
		int rgiO[];
		
		//outputting grid
		rga1 = new ArrayDouble.D4(360, 720, 1, 1);
		rgx1 = rga1.getIndex();
		rga1.setDouble(rgx1.set(360-iRow-1,iCol,0,0), dValue);
		
		rgiO = new int[4];
		rgiO[2] = mapVert.get(dElevation);
		rgiO[3] = mapTime.get(dTime);
		try{
			try {
				ncf2.write(sVarName, rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (InvalidRangeException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Writes grid to open writer.
	 * @param rgdGrid Grid being written.
	 * @param dTime Time value.
	 * @param dElevation Elevation to write to.
	 */
	public void writeGrid(double rgdGrid[][], double dElevation, double dTime){
		
		//sVarName = name of variable
		//sVarUnits = units of variable
		//iLat = latitude dimension
		//iLng = longitude dimension
		//rga1 = current data array
		//ary1 = current data being written
		//rgx1 = index array
		//rgiO = origin
		
		int iLat; int iLng;
		ArrayDouble rga1;
		Index rgx1;
		int i; int j;
		int rgiO[];
		
		//loading latitude and longitude dimension
		iLat = rgdGrid.length; iLng = rgdGrid[0].length;
		
		//outputting grid
		rga1 = new ArrayDouble.D4(iLat, iLng, 1, 1);
		rgx1 = rga1.getIndex();
		for(i=0; i<iLat; i++) {
			for(j=0; j<iLng; j++) {
				rga1.setDouble(rgx1.set(i,j,0,0), rgdGrid[iLat-i-1][j]);
		    }
		}
		rgiO = new int[4];
		rgiO[2] = mapVert.get(dElevation);
		rgiO[3] = mapTime.get(dTime);
		try{
			try {
				ncf2.write(sVarName, rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (InvalidRangeException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Writes given grid (in ascii grid format) to netCDf file at path sOutPath.  NetCDF object is closed at end of writing
	 * @param rgdGrid Grid being written.
	 * @param sVarName Name of the variable being written.
	 * @param sVarUnits Units of the variable being written.
	 * @param sOutPath Path of file being written.
	 */
	public static void writeNetCDF(double rgdGrid[][], String sVarName, String sVarUnits, String sOutPath) {
		
		//sVarName = name of variable
		//sVarUnits = units of variable
		//iLat = latitude dimension
		//iLng = longitude dimension
		//rga1 = current data array
		//cdf1 = netcdf file
		//dimLat = latitude dimension
		//dimLng = longitude dimension
		//lst1 = list of dimensions
		//ary1 = current data being written
		//rgx1 = index array
		//dCellSize = cell size in degrees
		//rgiO = origin
		
		int iLat; int iLng;
		ArrayDouble rga1;
		ArrayList<Dimension> lst1;
		Index rgx1;
		int i; int j;
		double dCellSize;
		int rgiO[];
		NetcdfFileWriteable cdf1 = null;
		Dimension dimLat; Dimension dimLng;
		
		//loading latitude and longitude dimension
		iLat = rgdGrid.length; iLng = rgdGrid[0].length;
		
		//loading cell size
		dCellSize = 180./((double) iLat);
		
		//initializing file
		try {
			cdf1 = NetcdfFileWriteable.createNew(sOutPath, false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
		
		//adding dimensions
		dimLat = cdf1.addDimension("lat", iLat);
		dimLng = cdf1.addDimension("lon", iLng);
		lst1 = new ArrayList<Dimension>();
		lst1.add(dimLng);
		cdf1.addVariable("lon",DataType.FLOAT, lst1);
		cdf1.addVariableAttribute("lon","units","degrees_east");
		lst1 = new ArrayList<Dimension>();
		lst1.add(dimLat);		
		cdf1.addVariable("lat",DataType.FLOAT, lst1);
		cdf1.addVariableAttribute("lat","units","degrees_north");
		
		//adding variables
		lst1 = new ArrayList<Dimension>();
		lst1.add(dimLat);
		lst1.add(dimLng);
		cdf1.addVariable(sVarName,DataType.FLOAT, lst1);
		cdf1.addVariableAttribute(sVarName,"units",sVarUnits);
		cdf1.addVariableAttribute(sVarName,"missing_value",-9999);
		
		//creating file
		try {
			cdf1.create();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//adding latitude data
		rga1 = new ArrayDouble.D1(iLat);
		rgx1 = rga1.getIndex();
		for(i=0; i<iLat; i++) {
			rga1.setDouble(rgx1.set(i), -90 + dCellSize/2. + ((double) i)*dCellSize);
		}
		rgiO = new int[2];
		try {
			try {
				cdf1.write("lat", rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InvalidRangeException e) {
			System.out.println("ERROR!");
		}
		
		//adding longitude data
		rga1 = new ArrayDouble.D1(iLng);
		rgx1 = rga1.getIndex();
		for(i=0; i<iLng; i++) {
			rga1.setDouble(rgx1.set(i), -180 + dCellSize/2. + ((double) i)*dCellSize);
		}
		rgiO = new int[2];
		try {
			try {
				cdf1.write("lon", rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InvalidRangeException e) {
			System.out.println("ERROR!");
		}
		
		//outputting grid
		rga1 = new ArrayDouble.D2(iLat, iLng);
		rgx1 = rga1.getIndex();
		for(i=0; i<iLat; i++) {
			for(j=0; j<iLng; j++) {
				rga1.setDouble(rgx1.set(i,j), rgdGrid[iLat-i-1][j]);
		    }
		}
		rgiO = new int[2];
		try{
			try {
				cdf1.write(sVarName, rgiO, rga1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (InvalidRangeException e) {
			System.out.println("ERROR.");
		}		
		
		//closing file
		try {
			cdf1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds a reader that returns values for specified variable
	 * @param sVar Variable for which to build reader.
	 */
	private void buildReader(String sVar){
		
		//varArg = current argument variable
		//sInPath = path to NetCDF file
		//rgs2 = list of variables for arguments
		//sVarArg = name of current argument variable
		//ary1 = current data array
		
		Variable varArg;
		String rgs2[];
		String sVarArg;
		Array ary1 = null;
		
		//initializing map1
		mapLat = new TreeMap<Double,Integer>();	
		mapLon = new TreeMap<Double,Integer>();	
		mapVert = new TreeMap<Double,Integer>();	
		mapTime = new TreeMap<Double,Integer>();	
		
		//loading variable
		var1 = ncf1.findVariable(sVar);
		
		//loading list of variable names
		rgs2 = var1.getDimensionsString().split(" ");
		
		//looping through variables
		for(int i=0;i<rgs2.length;i++){
			
			//loading name of variable
			sVarArg = rgs2[i];
			
			//loading variable
			varArg = ncf1.findVariable(sVarArg);
			
			//loading array
			try {
				ary1 = varArg.read().reduce();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//looping through array
			if(ary1.getSize()==1){
				
				//updating map
				if(sVarArg.equals("lat")){
					mapLat.put((double) ary1.getFloat(0),0);
				}else if(sVarArg.equals("lon")){
					mapLon.put((double) ary1.getFloat(0),0);
				}else if(sVarArg.equals("vert")){
					mapVert.put((double) ary1.getFloat(0),0);
				}else if(sVarArg.equals("time")){
					mapTime.put((double) ary1.getFloat(0),0);
				}
			}else{
				for(int j=0;j<ary1.getShape()[0];j++){
					
					//updating map
					if(sVarArg.equals("lat")){
						mapLat.put((double) ary1.getFloat(j),j);
					}else if(sVarArg.equals("lon")){
						mapLon.put((double) ary1.getFloat(j),j);
					}else if(sVarArg.equals("vert")){
						mapVert.put((double) ary1.getFloat(j),j);
					}else if(sVarArg.equals("time")){
						mapTime.put((double) ary1.getFloat(j),j);
					}	
				}
			}
		}
		
		//initializing rgiShape
		rgiShape = var1.getShape();
		for(int i=0;i<rgiShape.length;i++){
			rgiShape[i] = 1;
		}
		
		//loading units
		sUnits = var1.getUnitsString();
		
		//loading variable name
		sVarName = sVar;
	}

	/**
	 * Builds a netcdf reader using the first variable found within the netCDF file that is not latitude, longitude, or time
	 */
	private void buildReaderFirstVar(){
		
		//rgsVars = list of variables
		
		String rgsVars[];
		
		//loading list of variables
		rgsVars = this.getCDFVars();
		
		//initializing flag for Elevation/time
		bVertTime = false;
		
		//looping through variables to find the one that is not latitude or longitude or time
		for(int j=0;j<rgsVars.length;j++){
			if(rgsVars[j].equals("lat")==false && rgsVars[j].equals("lon")==false && rgsVars[j].equals("time")==false && rgsVars[j].equals("vert")==false){
				this.buildReader(rgsVars[j]);
			}else if(rgsVars[j].equals("time")==true || rgsVars[j].equals("vert")==true){
				bVertTime=true;
			}
		}
	}

	/**
	 * Gets a list of variables in the NetCDF file
	 * @return A string array giving the list of variables.
	 */
	private String[] getCDFVars(){
		
		//rgs1 = output
		//lst1 = list of variables
		//itr1 = list iterator
		//i1 = number of variables
		//rgs1 = output
		//var2 = current variable
		
		Variable var2;
		int i=0;
		List<Variable> lst1;
		Iterator<Variable> itr1;
		int i1=0;
		String rgs1[];
		
		//loading list of variables
		lst1 = this.ncf1.getVariables();
		
		//looping through variables
		for(itr1 = lst1.iterator(); itr1.hasNext();){
			itr1.next();
			i1++;
		}
		
		//outputting results
		rgs1 = new String[i1];
		for(itr1 = lst1.iterator(); itr1.hasNext();){
			var2 = (Variable) itr1.next();
			rgs1[i] = var2.getShortName();
			i++;
		}
		
		//outputting result
		return rgs1;	
	}
	

	/**
	 * Looks up value for key closest to the specified key in the given map.
	 * @param map1 Map for lookup.
	 * @param dKey Map for lookup.
	 * @return Value of closest key. 
	 */
	private Integer lookupValue(TreeMap<Double,Integer> map1, double dKey){
		
		//dFloorKey = floor key
		//dCeilingKey = ceiling key
		
		double dFloorKey; double dCeilingKey;
		
		//checking if key is in map
		if(map1.containsKey(dKey)){
			return map1.get(dKey);
		}else{
			
			//checking if smaller than the smallest key
			if(dKey<map1.firstKey()){
				return map1.get(map1.firstKey());
				
			//checking if larger than the largest key	
			}else if(dKey>map1.lastKey()){
				return map1.get(map1.lastKey());
				
			//finding nearest key	
			}else{
				
				//loading floor and ceiling keys
				dFloorKey = map1.floorKey(dKey);
				dCeilingKey = map1.ceilingKey(dKey);
				
				//returning closest value
				if((dKey-dFloorKey)<(dCeilingKey-dKey)){
					return map1.get(dFloorKey);
				}else{
					return map1.get(dCeilingKey);
				}
			}
		}
	}
}
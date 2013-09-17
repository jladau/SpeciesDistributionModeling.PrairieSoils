package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.ucsf.base.*;

/**
 * Uncompiled (observational) data object
 * @author jladau
 */
public abstract class SDMUncompiledData {

	//rgsMetadata = data (from file)
	//arg1 = arguments object
	//sdm1 = sdm object (with SDMRasterData loaded)
	//iSampleID = column with sample ID in metadata
	//rgsSamples = samples matrix
	
	protected String rgsSamples[][];
	public int iSampleIDCol=-9999;
	public String rgsMetadata[][];
	public Arguments arg1;
	
	public SDMUncompiledData(Arguments arg1){
		
		//saving arguments
		this.arg1 = arg1;
		
		//loading samples
		rgsSamples = FileIO.readFile(arg1.getValueString("sPathSamples"), ",");
		
		//***********************************
		//HashSet setTemp = new HashSet<String>();
		//for(int i=0;i<rgsSamples.length;i++){
		//	String[] rgsTEMP = rgsSamples[i][1].split(";");
		//	for(int k=0;k<rgsTEMP.length;k++){
		//		String sTEMP = rgsTEMP[k].trim();
		//		if(sTEMP.startsWith("p__")){
		//			setTemp.add(rgsTEMP[k]);
		//		}
		//	}
		//}
		//***********************************
		
		
		
		//loading metadata
		rgsMetadata = FileIO.readFile(arg1.getValueString("sPathMetadata"), ",");
		for(int k=0;k<rgsMetadata.length;k++){
			for(int j=0;j<rgsMetadata[0].length;j++){
				rgsMetadata[k][j]=rgsMetadata[k][j].replace("\"","");
			}
		}
		
		//filtering taxa
		System.out.println("Filtering taxa...");
		if(!arg1.getValueString("sTaxonInclude").equals("all")){
			filterTaxa(arg1.getValueString("sTaxonInclude"));
		}
		
		//loading sample id column
		for(int j=0;j<rgsMetadata[0].length;j++){
			if(rgsMetadata[0][j].toLowerCase().contains("sampleid")){
				iSampleIDCol=j;
				break;
			}
		}
		if(iSampleIDCol==-9999){
			System.out.println("ERROR:ProcessData.constructor - no column with \"SampleID\" header found in metadata file");
		}
	}
	
	public void compileData(){
		
		//flt1 = FilterMatrix object
		//rgsFilters = filters matrix
		
		String rgsFilters[][];
		FilterMatrix flt1;
		
		//updating headers to remove spaces and adjust cases
		for(int j=0;j<rgsMetadata[0].length;j++){
			this.rgsMetadata[0][j]=toProperCase(this.rgsMetadata[0][j]);
		}
		
		//loading filters
		rgsFilters = FileIO.readFile(arg1.getValueString("sPathFilters"), ",");
		
		//filtering rgsMetadata according to user inputs
		System.out.println("Filtering metadata...");
		for(int i=1;i<rgsFilters.length;i++){
			flt1 = new FilterMatrix(this.rgsMetadata,rgsFilters[i][0]);
			this.rgsMetadata = flt1.getMatrix();
		}
		
		//appending raster values
		System.out.println("Appending raster values...");
		this.appendRasterValues(arg1.getValueString("sRasterDir"),0);
		
		//filtering out samples in rgsMetadata that don't have raster data
		//System.out.println("Removing samples lacking raster data...");
		//filterNoData();
		
		//removing samples from rgsSamples that are not in metadata
		System.out.println("Removing samples lacking sequence data or metadata...");
		removeSamples();
		
		//running compiling specific to analysis type
		runSpecificCompiling();
		
		//*********************
		//for(int i=1;i<rgsMetadata.length;i++){
		//	if(rgsMetadata[i][0].equals("MPI_0010_2004_05_17")){
		//		System.out.println("HERE");
		//	}
		//}
		//*********************
		
		
		//rarefying and appending results
		System.out.println("Rarefying...");
		appendRarefactions(arg1.getValueInt("iRarefactionIterations"), arg1.getValueInt("iTotalReads"));
		

		//*********************
		//for(int i=1;i<rgsMetadata.length;i++){
		//	if(rgsMetadata[i][0].equals("MPI_0010_2004_05_17")){
		//		System.out.println("HERE");
		//	}
		//}
		//*********************
		
		
		//standardizing error values
		System.out.println("Standardizing error values...");
		for(int i=0;i<rgsMetadata.length;i++){
			for(int j=0;j<rgsMetadata[i].length;j++){
				rgsMetadata[i][j].replace(",,", ",-9999,");
				rgsMetadata[i][j].replace(",--,", ",-9999,");
				if(rgsMetadata[i][j].trim().equals("")){
					rgsMetadata[i][j]="-9999";
				}
			}
		}
	}
	
	/**
	 * Runs compiling specific to analysis type
	 */
	public abstract void runSpecificCompiling();
	
	/**
	 * Appends rarefaction results
	 */
	public abstract void appendRarefactions(int iRarefactionIterations, int iTotalReads);
	
	/**
	 * Converts specified to string to proper case, removing spaces.
	 * @param s1 String to be converted.
	 * @return String in proper case.
	 */
	private static String toProperCase(String s1){
		
		//sbl1 = current string builder
		//b1 = true if current character should be upper case, false otherwise
		//c1 = current character
		
		char c1;
		StringBuilder sbl1;
		boolean b1 = true;
		
		//initializing string builder
		sbl1 = new StringBuilder();
		
		//looping through characters
		for(int i=0;i<s1.length();i++){
			
			//loading current character
			c1 = s1.charAt(i);
			//s2 = s1.substring(i,i+1);
			
			//checking if space
			if(!Character.isLetter(c1) & !Character.isDigit(c1)){
				b1=true;
				if(c1==','){
					sbl1.append(c1);
				}
			}else{
				if(b1==true){
					sbl1.append(Character.toUpperCase(c1));
					b1 = false;
				}else{
					sbl1.append(c1);
				}
			}
		}
		
		//returning result
		return sbl1.toString();
	}
	
	/**
	 * Removes samples from samples matrix that are not listed in metadata.
	 */
	private void removeSamples(){
		
		//lst1 = list of samples in metadata
		//lst2 = list of samples in rgsSamples
		//lst3 = list of samples occurring in just rgsMetadata
		//lst4 = list of samples occurring in just rgsSamples
		//i1 = column with sample names
		//rgsOut = output
		//flt1 = FilterMatrix object
		
		FilterMatrix flt1;
		int i1=-9999;
		ArrayList<String> lst1; ArrayList<String> lst2; ArrayList<String> lst3; ArrayList<String> lst4;
		
		//loading sample id column
		for(int j=0;j<rgsMetadata[0].length;j++){
			if(rgsMetadata[0][j].toLowerCase().contains("sampleid")){
				i1=j;
				break;
			}
		}
		
		//loading list of samples in metadata
		lst1 = new ArrayList<String>();
		for(int i=1;i<rgsMetadata.length;i++){
			lst1.add(rgsMetadata[i][i1]);
		}
		
		//loading list of samples in rgsSamples
		lst2 = new ArrayList<String>();
		for(int i=1;i<rgsSamples.length;i++){
			if(!lst2.contains(rgsSamples[i][0])){
				lst2.add(rgsSamples[i][0]);
			}
		}
		
		//loading list of samples occurring just in metadata
		lst3 = new ArrayList<String>();
		for(int i=0;i<lst1.size();i++){
			if(!lst2.contains(lst1.get(i))){
				lst3.add(lst1.get(i));
			}
		}
		
		//loading list of samples occurring just in rgsSamples
		lst4 = new ArrayList<String>();
		for(int i=0;i<lst2.size();i++){
			if(!lst1.contains(lst2.get(i))){
				lst4.add(lst2.get(i));
			}
		}
		
		//filtering samples
		for(int i=0;i<lst4.size();i++){
			flt1 = new FilterMatrix(rgsSamples,rgsSamples[0][0] + ":notequals:" + lst4.get(i));
			rgsSamples = flt1.getMatrix();
		}
		
		//filtering metadata
		for(int i=0;i<lst3.size();i++){
			flt1 = new FilterMatrix(rgsMetadata,rgsMetadata[0][i1] + ":notequals:" + lst3.get(i));
			rgsMetadata = flt1.getMatrix();
		}
	}
	
	

	/**
	 * Appends raster values to the final column of rgsMetadata (assuming comma delimeter)
	 * @param sRasterDir Directory with rasters.
	 * @param iLag Monthly lag to use
	 */
	private void appendRasterValues(String sRasterDir, int iLag){
		
		//iLatCol = column with latitude data
		//iLngCol = column with longitude data
		//rst1 = RasterValues object
		//i1 = number of columns in rgsMetadata
		//iMonthCol = column of metadata with month
		//iDayCol = column of metadata with day
		//iElevationCol = column of metadata with elevation
		//iMonth = current month
		//rgsFiles = list of files
		//sVar = variable name
		
		String rgsFiles[];
		int iMonth; int iLatCol=-9999; int iLngCol=-9999; int i1; int iElevationCol=-9999; int iMonthCol = -9999; int iDayCol = -9999;
		SDMRasterData_Interpolated rst1;
		String sVar;
		
		//loading latitude and longitude and date and elevation columns
		for(int j=0;j<rgsMetadata[0].length;j++){
			if(rgsMetadata[0][j].toLowerCase().equals("latitude")){
				iLatCol = j;
			}
			if(rgsMetadata[0][j].toLowerCase().equals("longitude")){
				iLngCol = j;
			}
			if(rgsMetadata[0][j].toLowerCase().equals("month")){
				iMonthCol = j;
			}
			if(rgsMetadata[0][j].toLowerCase().equals("day")){
				iDayCol = j;
			}
			if(rgsMetadata[0][j].toLowerCase().equals("elevation")){
				iElevationCol = j;
			}
		}
		
		//loading list of files
		rgsFiles = FileIO.getFileList(sRasterDir);
		
		//loading number of columns
		i1 = rgsMetadata[0].length;
		
		//looping through files
		for(int k=0;k<rgsFiles.length;k++){
		
			//checking value
			if(!rgsFiles[k].endsWith(".nc")){
				continue;
			}
			
			//loading variable name
			sVar = rgsFiles[k].replace(".nc", "") + "Raster";
			
			//initializing RasterData object
			rst1 = new SDMRasterData_Interpolated(sVar, sRasterDir + "/" + rgsFiles[k]);
			
			//writing header
			rgsMetadata[0][i1-1]+="," + sVar;
			
			//looping through rows
			for(int i=1;i<rgsMetadata.length;i++){
				iMonth = Integer.parseInt(rgsMetadata[i][iMonthCol]) + iLag;
				if(iMonth>12){
					iMonth-=12;
				}else if(iMonth<1){
					iMonth+=12;
				}
				rgsMetadata[i][i1-1]+="," + rst1.findValueInterpolated(Integer.parseInt(rgsMetadata[i][iDayCol]), iMonth, Double.parseDouble(rgsMetadata[i][iElevationCol]), Double.parseDouble(rgsMetadata[i][iLatCol]), Double.parseDouble(rgsMetadata[i][iLngCol]));
			}
			
			//closing file
			rst1.closeCDF();
		}
	}
	
	/**
	 * Filters out taxon strings (converts to unclassified) that do not contain sTaxonInclude
	 * @param sTaxonInclude String to look for within taxon string (e.g., "Verrucomicrobia")
	 */
	private void filterTaxa(String sTaxonInclude){
		
		//mapAbundance(sSample,sTaxon) = abundance of taxon in sample
		//iAbundance = current abundance
		//rgs1 = current key in split format
		//iRow = current row
		
		Map<String,Integer> mapAbundance;
		int iAbundance; int iRow;
		String rgs1[];
		
		
		//****************************
		//mapAbundance = new HashMap<String,Integer>();
		//for(int i=1;i<rgsSamples.length;i++){
		//	if(mapAbundance.containsKey(rgsSamples[i][0])){
		//		iAbundance=mapAbundance.get(rgsSamples[i][0]);
		//	}else{
		//		iAbundance=0;
		//	}
		//	iAbundance+=Integer.parseInt(rgsSamples[i][2]);
		//	mapAbundance.put(rgsSamples[i][0], iAbundance);
		//}
		//for(String s:mapAbundance.keySet()){
		//	System.out.println(s + "," + mapAbundance.get(s));
		//}
		//System.out.println("HERE");
		//****************************
		
		//initializing abundance map
		mapAbundance = new HashMap<String,Integer>();
		
		//looping through entries
		for(int i=1;i<rgsSamples.length;i++){
			iAbundance = Integer.parseInt(rgsSamples[i][2]);
			if(!rgsSamples[i][1].contains(sTaxonInclude)){
				if(mapAbundance.containsKey(rgsSamples[i][0] + ",unclassified")){
					iAbundance+=mapAbundance.get(rgsSamples[i][0] + ",unclassified");
				}
				mapAbundance.put(rgsSamples[i][0] + ",unclassified",iAbundance);
			}else{
				if(mapAbundance.containsKey(rgsSamples[i][0] + "," + rgsSamples[i][1])){
					iAbundance+=mapAbundance.get(rgsSamples[i][0] + "," + rgsSamples[i][1]);
				}
				mapAbundance.put(rgsSamples[i][0] + "," + rgsSamples[i][1],iAbundance);
			}
		}
		
		//reloading matrix
		rgsSamples = new String[mapAbundance.size()+1][3];
		rgsSamples[0][0]="SAMPLE"; rgsSamples[0][1]="OTU"; rgsSamples[0][2]="ABUNDANCE";
		iRow=1;
		for(String s:mapAbundance.keySet()){
			rgs1=s.split(",");
			rgsSamples[iRow][0]=rgs1[0];
			rgsSamples[iRow][1]=rgs1[1];
			rgsSamples[iRow][2]=Integer.toString(mapAbundance.get(s));
			iRow++;
		}
		
		//****************************
		//mapAbundance = new HashMap<String,Integer>();
		//for(int i=1;i<rgsSamples.length;i++){
		//	if(mapAbundance.containsKey(rgsSamples[i][0])){
		//		iAbundance=mapAbundance.get(rgsSamples[i][0]);
		//	}else{
		//		iAbundance=0;
		//	}
		//	iAbundance+=Integer.parseInt(rgsSamples[i][2]);
		//	mapAbundance.put(rgsSamples[i][0], iAbundance);
		//}
		//for(String s:mapAbundance.keySet()){
		//	System.out.println(s + "," + mapAbundance.get(s));
		//}
		//System.out.println("HERE");
		//****************************
		
		
	}
}

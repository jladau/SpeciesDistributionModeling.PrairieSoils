package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads arguments from command line
 * @author jladau
 */

public class Arguments {

	//mapStringValue(sKey) = returns string argument
	//mapIntValue(sKey) = returns integer argument
	//mapDoubleValue(sKey) = returns double argument 
	//mapBoolean(sKey) = returns boolean argument
	//mapAllArguments(sKey) = returns argument in string format (all arguments in map)
	//mapDoubleArrayValue(sKey) = returns double array argument
	
	private Map<String,Double[]> mapDoubleArrayValue;
	private Map<String,String> mapStringValue;
	private Map<String,Integer> mapIntegerValue;
	private Map<String,Double> mapDoubleValue;
	private Map<String,Boolean> mapBooleanValue;
	public Map<String,String> mapAllArguments;
		
	/**
	 * Constructor
	 * @param rgsArgs Arguments
	 */
	public Arguments(String rgsArgs[]){
		
		//rgs1 = current argument in split format
		//lstArgs = arguments in list form (for parsing arguments with spaces)
		//s1 = current argument
		//bComment = flag for whether within comment
		//rgs2 = current array value in split string format
		//rgd1 = current double array being loaded
		
		Double rgd1[];
		ArrayList<String> lstArgs;
		String rgs1[]; String rgs2[];
		String s1="";
		boolean bComment;
		
		//initializing maps
		mapStringValue = new HashMap<String,String>();
		mapIntegerValue = new HashMap<String,Integer>();
		mapDoubleValue = new HashMap<String,Double>();
		mapBooleanValue = new HashMap<String,Boolean>();
		mapAllArguments = new HashMap<String,String>();
		mapDoubleArrayValue = new HashMap<String,Double[]>();
		
		//loading list of arguments
		lstArgs = new ArrayList<String>();
		bComment = false;
		for(int i=0;i<rgsArgs.length;i++){
			if(rgsArgs[i].startsWith("//")){
				bComment=true;
				continue;
			}
			if(rgsArgs[i].startsWith("--")){
				bComment=false;
				if(i!=0){
					lstArgs.add(s1);
				}
				s1 = rgsArgs[i];
			}else{
				if(bComment==false){
					s1 += " " + rgsArgs[i];
				}
			}
		}
		lstArgs.add(s1);
		
		//looping through arguments
		for(int i=0;i<lstArgs.size();i++){
			
			//splitting argument
			rgs1 = lstArgs.get(i).split("=");
			
			//loading argument
			if(rgs1[0].startsWith("--s")){
				mapStringValue.put(rgs1[0].replace("--",""), rgs1[1]);
			}else if(rgs1[0].startsWith("--i")){
				mapIntegerValue.put(rgs1[0].replace("--",""), Integer.parseInt(rgs1[1]));
			}else if(rgs1[0].startsWith("--d")){
				mapDoubleValue.put(rgs1[0].replace("--",""), Double.parseDouble(rgs1[1]));
			}else if(rgs1[0].startsWith("--b")){
				mapBooleanValue.put(rgs1[0].replace("--",""), Boolean.parseBoolean(rgs1[1]));
			}else if(rgs1[0].startsWith("--rgd")){
				rgs2 = rgs1[1].split(",");
				rgd1 = new Double[rgs2.length];
				for(int k=0;k<rgs2.length;k++){
					rgd1[k]=Double.parseDouble(rgs2[k]);
				}
				mapDoubleArrayValue.put(rgs1[0].replace("--",""), rgd1);
			}
			
			//loading to all arguments map
			mapAllArguments.put(rgs1[0].replace("--", ""), rgs1[1]);
		}
	}
	
	/**
	 * Prints arguments to the specified output path
	 * @param sPath Output path
	 */
	public void printArguments(String sPath, boolean bAppend, String sHeader){
		
		//lstOut = output list
		//rgc1 = header separator array
		
		ArrayList<String> lstOut;
		char rgc1[];
		
		lstOut = new ArrayList<String>(this.mapAllArguments.size()+3);
		if(bAppend==true && FileIO.checkFileExistence(sPath)){
			lstOut.add("");
		}
		if(sHeader!=null){
			lstOut.add(sHeader);
			rgc1 = new char[sHeader.length()];
		    Arrays.fill(rgc1, '-');
		    lstOut.add(new String(rgc1));
		}
		for(String s:mapAllArguments.keySet()){
			lstOut.add(s + "=" + mapAllArguments.get(s));
		}
		FileIO.writeFile(lstOut, sPath, 0, bAppend);
	}
	
	public Double[] getValueDoubleArray(String sArgument){
		if(mapDoubleArrayValue.containsKey(sArgument)){	
			return mapDoubleArrayValue.get(sArgument);
		}else{
			System.out.println("ERROR: argument " + sArgument + " not found.");
			return null;
		}
	}
	
	public double getValueDouble(String sArgument){
		if(mapDoubleValue.containsKey(sArgument)){	
			return mapDoubleValue.get(sArgument);
		}else{
			System.out.println("ERROR: argument " + sArgument + " not found.");
			return -9999;
		}
	}
	
	public int getValueInt(String sArgument){
		if(mapIntegerValue.containsKey(sArgument)){
			return mapIntegerValue.get(sArgument);
		}else{
			System.out.println("ERROR: argument " + sArgument + " not found.");
			return -9999;
		}
	}
	
	public String getValueString(String sArgument){
		if(mapStringValue.containsKey(sArgument)){
			return mapStringValue.get(sArgument);
		}else{
			System.out.println("ERROR: argument " + sArgument + " not found.");
			return "-9999";
		}
	}
	
	public boolean getValueBoolean(String sArgument){
		if(mapBooleanValue.containsKey(sArgument)){
			return mapBooleanValue.get(sArgument);
		}else{
			System.out.println("ERROR: argument " + sArgument + " not found.");
			return false;
		}
	}
	
	public void updateArgument(String sKey, int iValue){
		mapIntegerValue.put(sKey, iValue);
	}
	
	public void updateArgument(String sKey, double dValue){
		mapDoubleValue.put(sKey, dValue);
	}
	
	public void updateArgument(String sKey, String sValue){
		mapStringValue.put(sKey, sValue);
	}
	
	public void updateArgument(String sKey, boolean bValue){
		mapBooleanValue.put(sKey, bValue);
	}
}

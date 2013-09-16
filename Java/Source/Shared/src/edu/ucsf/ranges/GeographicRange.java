package edu.ucsf.ranges;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import edu.ucsf.base.*;

/**
 * Loads range by looking at appropriate dbf file (in csv format) and shapefile (in text format using shpdump)
 * @author jladau
 */
public class GeographicRange {
	
	//rgsDBF = dbf file
	//bfr1 = shapefile buffered reader
	//iShape = current shape id; corresponds to current row of dbf file
	//sSpecies = current species
	//bDone = flag for whether done
	
	private int iShape;
	private String rgsDBF[][];
	private BufferedReader bfr1;
	private String sSpecies;
	private boolean bDone = false;
	
	/**
	 * Constructor.
	 * @param sShapeFilePath Path to shapefile.
	 */
	public GeographicRange(String sShapeFilePath){

		//loading dbf file
		rgsDBF = FileIO.readFile(sShapeFilePath.replace(".shp.txt",".dbf.csv"), ",");
		for(int i=1;i<rgsDBF.length;i++){
			rgsDBF[i][2]=rgsDBF[i][2].replace("\"","");
		}
		
		//initializing buffered reader
		try {
			bfr1 = new BufferedReader(new FileReader(sShapeFilePath));
		} catch (FileNotFoundException e1) {
		}
		for(int i=0;i<5;i++){
			this.readNextLine();
		}
		
		//initializing shape
		iShape = 1;
		
	}
	
	/**
	 * Gets the next range
	 * @return Next range in list format: entries are polygon ID, longitude, latitude
	 */
	public ArrayList<String[]> getNextRange(){
		
		//sLine = current line
		//i1 = current shape counter
		//rgs1 = current vertex being added
		//rgs2 = current line in split format
		//lst1 = output
		
		String sLine;
		int i1=0;
		ArrayList<String[]> lst1;
		String rgs1[]; String rgs2[];
		
		//checking if done
		if(bDone == true){
			this.closeReader();
			return null;
		}
		
		//initializing output
		lst1 = new ArrayList<String[]>(10000);
		
		//loading current species
		sSpecies = rgsDBF[iShape][2];
		
		//looping until new species found
		do{
		
			//loading current line
			sLine = this.readNextLine();
			
			//checking if done
			if(sLine==null){
				bDone=true;
				break;
			}
			
			//replacing spaces
			sLine = sLine.replace(" ", "");
			
			//checking if end of current shape and going to next line if next shape is for the same species
			if(sLine.equals("")){
				iShape++;
				continue;
			}
			
			//checking if start of new shape
			if(sLine.startsWith("Shape")){
				this.readNextLine();
				this.readNextLine();
				i1++;
				continue;
			}
			
			//checking if start of new ring
			if(sLine.startsWith("+")){
				i1++;
				sLine = sLine.replace("+","");
			}
			
			//vertex line: cleaning
			sLine = sLine.replace("(", "");
			sLine = sLine.replace(")", "");
			sLine = sLine.replace("(", "");
			rgs2 = sLine.split(",");
			
			//loading current vertex
			rgs1 = new String[3];
			rgs1[0] = Integer.toString(i1);
			rgs1[1] = rgs2[0];
			rgs1[2] = rgs2[1];
			lst1.add(rgs1);
			
		}while(iShape<rgsDBF.length && rgsDBF[iShape][2].equals(sSpecies));
		
		//checking if done
		//if(iShape==rgsDBF.length-1){
		//	bDone = true;
		//}
		
		//returning result
		return lst1;
	}
	
	/**
	 * Returns the current species.
	 * @return Name of the current species
	 */
	public String getCurrentSpecies(){
		return sSpecies.replace(" "," ");
	}
	
	/**
	 * Closes reader.
	 */
	public void closeReader(){
		try {
			bfr1.close();
		} catch (IOException e) {
		}
	}
	
	
	/**
	 * Returns the next line of the shapefile
	 * @return Next line of shapefile; null if done.
	 */
	private String readNextLine(){
		
		//returning line
		try {
			return bfr1.readLine();
		} catch (IOException e) {
			return null;
		}
	}
	
}

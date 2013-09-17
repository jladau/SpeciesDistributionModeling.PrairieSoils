package edu.ucsf.base;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

/**
 * Wrapper for various IO operations.
 * @author jladau
 */
public class FileIO {

	//lstFiles = list of all files in given directory; used by findAllFiles
	
	/**
	 * Checks if two files are identical
	 * @param sPath1 Path to first file
	 * @param sPath2 Path to second file
	 * @return True if files are identical, false otherwise
	 */
	public static boolean areFilesIdentical(String sPath1, String sPath2){
		
		//bfr1 = first reader
		//bfr2 = second reader
		//s1 = line from first reader
		//s2 = line from second reader
		
		BufferedReader bfr1; BufferedReader bfr2;
		String s1; String s2;
		
		try{
			bfr1 = new BufferedReader(new FileReader(sPath1));
			bfr2 = new BufferedReader(new FileReader(sPath2));
			while((s1 = bfr1.readLine()) != null){
				
				s2 = bfr2.readLine();
				if(!s1.equals(s2)){
					bfr1.close();
					bfr2.close();
					return false;
				}
			}
			bfr1.close();
			bfr2.close();
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	/**
	 * Checks for the existence of a file
	 * @param sPath Directory with file
	 * @param sName Name of file
	 * @return True if file exists, false otherwise
	 */
	public static boolean checkFileExistence(String sDirectory, String sName){
		
		//rgs1 = list of files in directory
		
		String rgs1[];
		
		if(!sDirectory.endsWith("/")){
			sDirectory += "/";
		}
		rgs1 = FileIO.getFileList(sDirectory);
		for(int i=0;i<rgs1.length;i++){
			if(rgs1[i].equals(sName)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if file exists
	 * @param sPath Path to file
	 * @return True if file exists, false otherwise
	 */
	public static boolean checkFileExistence(String sPath){
		
		//fil1 = file object
		
		File fil1;
		
		fil1 = new File(sPath);
		if(fil1.exists()){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * Deletes specified file
	 * @param sPath Path to file.
	 */
	public static void deleteFile(String sPath){
		
		//fil1 = file object
		
		File fil1;
		
		//initializing file object
		fil1 = new File(sPath);
		
		//deleting file
		fil1.delete();
	}
	
	/**
	 * Moves specified file
	 * @param sPathStart Original path to file.
	 * @param sPathDestination New path to file
	 */
	public static void moveFile(String sPathStart, String sPathDestination){
		
		//fil1 = file object
		
		File fil1;
		
		//initializing file object
		fil1 = new File(sPathDestination);
		
		//checking if conflicting file exists
		if(fil1.exists()){
			fil1.delete();
		}
		
		//moving file
		fil1 = new File(sPathStart);
		fil1.renameTo(new File(sPathDestination));
	}

	/**
	 * Downloads the the specified file and saves to the given path.
	 * @param sURL URL of the file being downloaded.
	 * @param sDestinationPath Path to which downloaded file is to be saved.
	 * @param iBufferSize Buffer size.
	 * @param bQuiet True if download progress should not be updated.
	 */
	public static void downloadFile(String sURL, String sDestinationPath, int iBufferSize, boolean bQuiet){
		
		//url1 = url object
		//fos1 = file output stream
		//rgbBuffer = buffer
		//iBytesRead = number of bytes read
		//iTotalkb = total kilobytes read
		//ist1 = input stream object
		
		URL url1;
		FileOutputStream fos1;
		byte rgbBuffer[];
		int iBytesRead; int iTotalKb;
		InputStream ist1;
		
		try{
		    
		    //connecting to URL
			url1 = new URL(sURL);
			url1.openConnection();
			ist1 = url1.openStream();
			 
			//setting up file writer
			fos1 = new FileOutputStream(sDestinationPath);
			rgbBuffer = new byte[iBufferSize];
			iBytesRead = 0;
			iTotalKb=0;
			 
			//reading file
			while ((iBytesRead = ist1.read(rgbBuffer)) > 0){  
				fos1.write(rgbBuffer, 0, iBytesRead);
				rgbBuffer = new byte[iBufferSize];
				iTotalKb+=iBytesRead;
				if(bQuiet==false){
					if(iTotalKb<1000000){
						System.out.println(((double) iTotalKb)/1000. + " kB read...");
					}else{
						System.out.println(((double) iTotalKb)/1000000. + " MB read...");
					}
				}
			}
			fos1.close();
			ist1.close();
		}catch (MalformedURLException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Finds the names of files in a given directory.
	 * @param sDirectory Directory to search.
	 * @return Array of file names.
	 */
	public static String[] getFileList(String sDirectory){
		
		//fil1 = current file object
		
		File fil1;
		
		//checking sDirectory string
		if(sDirectory.endsWith("/")==false){
			sDirectory = sDirectory + "/";
		}
		
		//loading files
		fil1= new File(sDirectory);

		//outputting result
		return fil1.list();
	}

	/**
	 * Outputs completion file
	 * @param sOutputPath Path to output file from code (".complete" will be appended)
	 */
	public static void writeCompletionFile(String sOutputPath){
		FileIO.writeFile("", sOutputPath + ".complete", 0, false);
	}
	
	/**
	 * Checks and waits for completion of output file (checks for completion file)
	 * @param sOutputPath Output file to check for
	 */
	public static boolean checkAndWaitForCompletion(String sOutputPath){
		
		//fil1 = file object
		
		File fil1;
		
		//initializing file object
		fil1 = new File(sOutputPath + ".complete");
		
		//waiting until file is complete
		while(!fil1.exists()){
			try{	
				Thread.sleep(2500);
			}catch(Exception e){
			}
		}
		
		//returning true result
		return true;
	}
	
	/**
	 * Checks and waits for completion of all output files with suffixes between two specified indices (checks for completion files)
	 * @param sOutputPath Output file to check for
	 */
	public static boolean checkAndWaitForCompletion(String sOutputPath, int iSuffixMin, int iSuffixMax){
		
		//fil1 = current file
		
		File fil1;
		
		//looping through files
		
		for(int i=iSuffixMin;i<=iSuffixMax;i++){
			fil1 =  new File(sOutputPath + "_" + i + ".complete");
			
			//waiting until file is complete
			while(!fil1.exists()){
				try{	
					Thread.sleep(2500);
				}catch(Exception e){
				}
			}
		}
		
		//returning true result
		return true;
	}
	
	/**
	 * Checks and waits for completion of a particular output file specified by given suffix (checks for completion file)
	 * @param sOutputDir Output directory to check in
	 * @param SOutputSuffix Suffix to check for
	 */
	public static boolean checkAndWaitForCompletion(String sOutputDir, String sOutputSuffix){
		
		//rgsFiles = list of file
		//bFileFound = flag for whether file is found
		
		String[] rgsFiles;
		boolean bFileFound=false;
		
		//looping through files
		do{
			//loading list of files
			rgsFiles = FileIO.getFileList(sOutputDir);
			
			//checking for files
			for(int i=0;i<rgsFiles.length;i++){
				if(rgsFiles[i].endsWith(sOutputSuffix)){
					bFileFound=true;
					break;
				}
			}
			
			//sleeping if necessary
			if(bFileFound==false){
				
				//waiting
				try{
					Thread.sleep(2500);
				}catch(Exception e){			
				}
			}
		}while(bFileFound==false);
		
		//returning true result
		return true;
	}

	/**
	 * Extracts file name from path string.
	 * @param sPath Path with file name.
	 * @return string giving file name (includes suffix).
	 */
	public static String getFileName(String sPath){
		
		//rgs1 = path in split format
		
		String rgs1[];
		
		rgs1 = sPath.split("/");
		return rgs1[rgs1.length-1];
	}
	
	/**
	 * Extracts file name from path string.
	 * @param sPath Path with file name.
	 * @return string giving file name (includes suffix).
	 */
	public static String getFileDirectory(String sPath){
		
		//rgs1 = path in split format
		//sbl1 = output
		
		String rgs1[];
		StringBuilder sbl1;
		
		rgs1 = sPath.split("/");
		sbl1 = new StringBuilder();
		sbl1.append(rgs1[0]);
		for(int i=1;i<rgs1.length-1;i++){
			sbl1.append("/" + rgs1[i]);
			
		}
		return sbl1.toString();
	}
	

	/**
	 * Queries user for path to file or directory and returns appropriately trimmed string.
	 * @param sPrompt Prompt for query.
	 * @param sDefault default value for query.
	 * @return Result in trimmed format.
	 */
	public static String getPath(String sPrompt, String sDefault){
	
		//s1 = output
		
		String s1;
	
		//loading path
		s1 = JOptionPane.showInputDialog(null, sPrompt, sDefault);
		s1 = s1.replace("file://","");
		s1 = s1.replace("%20"," ");
		s1 = s1.trim();
	
		//outputting result
		return s1;
	}

	/**
	 * Reads contents of a file into string array.
	 * @param sPath Path to file.
	 * @param sDelim Column delimeter for file.
	 * @return String array with contents of file.
	 */
	public static String[][] readFile(String sPath, String sDelim){
	
		//i1 = number of rows of data
		//i2 = number of columns of data
		//s1 = current line
		//rgs1 = output array
		//bfr1 = first buffered reader
		//bfr2 = second buffered reader
		
		BufferedReader bfr1; BufferedReader bfr2; 
		int i1; int i2;
		int i; int j;
		String s1;
		String rgs1[][]; String rgs2[];
		
		//initializing rgs1
		rgs1=new String[0][0];
		
		try{
	
			//initializing i1 and i2 and i
			i1=0;
			i2=0;
			i=0;
			
			//finding number of lines and columns of data
			bfr1 = new BufferedReader(new FileReader(sPath));
			while((s1 = bfr1.readLine()) != null){
				if(i2==0){
					i2=s1.split(sDelim).length;						
				}
				i1++;
			}
			bfr1.close();
			
			//initializing output
			rgs1 = new String[i1][i2];
	
			//writing output
			bfr2 = new BufferedReader(new FileReader(sPath));
			while ((s1 = bfr2.readLine()) != null){
				rgs2=s1.trim().split(sDelim);					
				for(j=0; j<rgs2.length; j++){
					if(j<i2){rgs1[i][j]=rgs2[j];}
				}	
				i++;
			}
			bfr2.close();
		}catch (Exception e){
			return null;
		}
		
		return rgs1;
	}

	/**
	 * Reads contents of a file into string array.
	 * @param sPath Path to file.
	 * @param sDelim Column delimeter for file.
	 * @param iColumns Number of columns of data
	 * @return String array with contents of file.
	 */
	public static String[][] readFile(String sPath, String sDelim, int iColumns){
	
		//i1 = number of rows of data
		//i2 = number of columns of data
		//s1 = current line
		//rgs1 = output array
		
		int i1; int i2;
		int i; int j;
		String s1;
		String rgs1[][]; String rgs2[];
		
		//initializing rgs1
		rgs1=new String[0][0];
		
		try{
	
			//initializing i1 and i2 and i
			i1=0;
			i2=0;
			i=0;
			
			//finding number of lines and columns of data
			BufferedReader bfr1 = new BufferedReader(new FileReader(sPath));
			while((s1 = bfr1.readLine()) != null){
				if(i2==0){
					i2=s1.split(sDelim).length;						
				}
				i1++;
		    }
			bfr1.close();
			
			//overriding number of columns
			i2 = iColumns;
			
			//initializing output
			rgs1 = new String[i1][i2];
	
			//writing output
			BufferedReader bfr2 = new BufferedReader(new FileReader(sPath));
			while((s1 = bfr2.readLine()) != null){
				rgs2=s1.split(sDelim);					
				for(j=0; j<rgs2.length; j++){
					if(j<i2){rgs1[i][j]=rgs2[j];}
				}	
				i++;
		    }
			bfr2.close();
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
		return rgs1;
	}
	
	/**
	 * Writes the contests of a string array into a file.
	 * @param lst1 Output list
	 * @param sPath Output path.
	 * @param sDelim Delimeter for output.
	 * @param iStart Line of string array to start at.
	 * @param bAppend True if results should be appended to file; false otherwise.
	 * @return 1 if successful.
	 */
	public static void writeFile(ArrayList<String> lst1, String sPath, int iStart, boolean bAppend){
		
		try{
		
			//initializing output
			PrintWriter prt1 = new PrintWriter(new FileWriter(sPath, bAppend));
			
			//looping through output array
			for(int i=iStart; i<lst1.size(); i++){
				prt1.println(lst1.get(i));
			}
			prt1.close();
		}catch (IOException e){
			  System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Writes the contests of a string array into a file.
	 * @param rgs1 Array being output.
	 * @param sPath Output path.
	 * @param sDelim Delimeter for output.
	 * @param iStart Line of string array to start at.
	 * @param bAppend True if results should be appended to file; false otherwise.
	 * @return 1 if successful.
	 */
	public static int writeFile(String rgs1[][], String sPath, String sDelim, int iStart, boolean bAppend){
		
		int j;
		
		try{
		
			//initializing output
			PrintWriter prt1 = new PrintWriter(new FileWriter(sPath, bAppend));
			
			//looping through output array
			for(int i=iStart; i<rgs1.length; i++){
				for(j=0; j<rgs1[0].length-1; j++){
					prt1.print(rgs1[i][j]+sDelim);
				}
				prt1.println(rgs1[i][j]);
			}
			prt1.close();
		}catch (IOException e){
			  System.err.println("Error: " + e.getMessage());
		}
		
		//returning success value
		return 1;
	}
	
	/**
	 * Writes the contests of a string array into a file.
	 * @param sOut String being output
	 * @param sPath Output path.
	 * @param iStart Line of string array to start at.
	 * @param bAppend True if results should be appended to file; false otherwise.
	 * @return 1 if successful.
	 */
	public static int writeFile(String sOut, String sPath, int iStart, boolean bAppend){
		
		try{
		
			//initializing output
			PrintWriter prt1 = new PrintWriter(new FileWriter(sPath, bAppend));
			
			//looping through output array
			prt1.println(sOut);
			prt1.close();
		}catch (IOException e){
			  System.err.println("Error: " + e.getMessage());
		}
		
		//returning success value
		return 1;
	}
	
	
	private ArrayList<String> lstAllFiles;
	
	public FileIO(){
	}

	/**
	 * Finds all files in directories and subdirectories therein that end with specified suffix.
	 * @param sDir Directory being searched.
	 * @param sSuffix Suffix for file.
	 * @return List of all files.
	 */
	public ArrayList<String> listAllFiles(String sDir, String sSuffix){
		
		//rgf1 = list of initial files
		
		File[] rgf1;
		
		//initializing output
		lstAllFiles = new ArrayList<String>();
		
		//initializing list of files
		rgf1 = new File(sDir).listFiles();
		
		//loading files
		loadFiles(rgf1,sSuffix);
		
		//outputting result
		return lstAllFiles;
	}

	/**
	 * Loads list of files through recursion
	 * @param rgf1 Current list of files
	 */
	private void loadFiles(File[] rgf1, String sSuffix){
		for(File f:rgf1){		
			if(f.isDirectory()){
				loadFiles(f.listFiles(),sSuffix);
			}else{
				if(f.getName().endsWith(sSuffix)){
					lstAllFiles.add(f.getPath());
				}
			}
		}
	}	
}

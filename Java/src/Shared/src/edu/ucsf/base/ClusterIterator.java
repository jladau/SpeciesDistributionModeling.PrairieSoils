package edu.ucsf.base;

import java.util.Iterator;

/**
 * Iterator for running process in parallel
 * @author jladau
 */

public class ClusterIterator implements Iterator {

	//iIteration = internal iteration counter
	//bInclude = true if iteration should be included; false otherwise
	//iTaskID = task ID
	//iTotalTasks = total tasks
	
	public boolean bInclude;
	public int iIteration;
	private int iTotalTasks;
	private int iTaskID;
	
	public ClusterIterator(Arguments arg1){
		iIteration=0;
		this.iTaskID = arg1.getValueInt("iTaskID");
		this.iTotalTasks = arg1.getValueInt("iTotalTasks");
	}
	
	public ClusterIterator(int iTaskID, int iTotalTasks){
		iIteration=0;
		this.iTaskID = iTaskID;
		this.iTotalTasks = iTotalTasks;
	}
	
	public boolean hasNext(){
		return true;
	}
	
	public Object next(){
		iIteration++;
		
		if(iTaskID==-9999 || iTotalTasks==-9999){
			bInclude=true;
		}else{
			if((iIteration % iTotalTasks) == (iTaskID-1)){
				bInclude=true;
			}else{
				bInclude=false;
			}
		}
		return null;
	}
	
	public void remove(){
	}
}

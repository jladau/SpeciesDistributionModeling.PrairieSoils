package edu.ucsf.base;

import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class DrawHeatChart {

	/** class draws heat map */
	
	//cht1 = heat char object
	
	private HeatChart cht1;
	
	/**
	 * Constructor
	 * @param rgdData Data to be plotted
	 * @param rgoXValues Values for x axis
	 * @param rgoYValues Values for y axis
	 * @param sTitle Title
	 * @param sXAxisLabel X-axis label
	 * @param sYAxisLabel Y-axis label
	 */
	public DrawHeatChart(double rgdData[][], Object rgoXValues[], Object rgoYValues[], String sTitle, String sXAxisLabel, String sYAxisLabel) {

		//constructor
		
		//iCols = number of columns in rgdData
		//iRows = number of rows in rgdData
		//iCells = number of cells
		
		int iCells; int iCols; int iRows;
		Dimension dim1 = new Dimension(100,100);
		Color clrLowColor; Color clrHighColor; Color clrError;
		Font fntAxisLabelsFont; Font fntAxisValuesFont; Font fntTitleFont;
		Dimension dimCellSize;
		
		//loading number of rows and columns
		iRows = rgdData.length;
		iCols = rgdData[0].length;
		
		//initializing colors
		clrLowColor = new Color(0,0,255);
		clrHighColor = new Color(255,0,0);
		clrError = new Color(220,220,220);
		
		//initializing cell size
		iCells = Math.max(rgdData.length, 750);
		dimCellSize = new Dimension(iCells/iCols,iCells/iRows);
		
		//initializing title font
		fntTitleFont = new Font("Sans-Serif", Font.PLAIN, 30*Math.max(iCells,750)/750);
	
		//initializing axis label font
		fntAxisValuesFont = new Font("Sans-Serif", Font.PLAIN, 20*Math.max(iCells,750)/750);
		
		//initializing axis font
		fntAxisLabelsFont = new Font("Sans-Serif", Font.PLAIN, 25*Math.max(iCells,750)/750);
		
	    //constructing chart
	    cht1 = new HeatChart(rgdData);
	    
	    //updating chart fields
	    cht1.setTitle(sTitle);
	    cht1.setXAxisLabel(sXAxisLabel);
	    cht1.setYAxisLabel(sYAxisLabel);
	    cht1.setCellSize(dim1);
	    cht1.setLowValueColour(clrLowColor);
	    cht1.setHighValueColour(clrHighColor);
	    cht1.setAxisLabelsFont(fntAxisLabelsFont);
	    cht1.setAxisValuesFont(fntAxisValuesFont);
	    cht1.setTitleFont(fntTitleFont);
	    cht1.setAxisThickness(0);
	    cht1.setXValuesHorizontal(true);
	    cht1.setCellSize(dimCellSize);
	    //cht1.setXAxisValuesFrequency(4);
	    //cht1.setYAxisValuesFrequency(4);
	    cht1.setXValues(rgoXValues);
	    cht1.setYValues(rgoYValues);
	    cht1.setErrorColour(clrError);
	}

	public void PrintChart(String sPath){
		
		//outputs heat map to file
		
	    try {	
	    	cht1.saveToFile(new File(sPath));
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }	
	}
	
	
}

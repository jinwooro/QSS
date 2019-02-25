package com.pretzel.structure;

public class Line {
	private boolean type; // it is an indicator if this line is incoming (true), otherwise false.
	private String LineFrom; 
	private int LineFromIndex;
	private String LineTo;
	private int LineToIndex;
	
	public Line(String inname, int inport, String outname, int outport, boolean type) {
		LineFromIndex = inport;
		LineFrom = inname;
		LineToIndex = outport;
		LineTo = outname;
		this.type = type;
	}

	public String getLineFrom() {
		return LineFrom;
	}
	
	public boolean isOutward() {
		return !type;
	}

	public boolean isInward() {
		return type;
	}
	
	public void setLineFrom(String lineFrom) {
		LineFrom = lineFrom;
	}

	public int getLineFromIndex() {
		return LineFromIndex;
	}

	public void setLineFromIndex(int lineFromIndex) {
		LineFromIndex = lineFromIndex;
	}

	public String getLineTo() {
		return LineTo;
	}

	public void setLineTo(String lineTo) {
		LineTo = lineTo;
	}

	public int getLineToIndex() {
		return LineToIndex;
	}

	public void setLineToIndex(int lineToIndex) {
		LineToIndex = lineToIndex;
	}
	
}

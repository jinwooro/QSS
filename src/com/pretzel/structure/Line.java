package com.pretzel.structure;

public class Line {
	private boolean type; // it is an indicator if this line is incoming (true), otherwise false.
	private String SrcName; 
	private int SrcPort;
	private String DstName;
	private int DstPort;
	
	public Line(String inname, int inport, String outname, int outport, boolean type) {
		SrcPort = inport;
		SrcName = inname;
		DstPort = outport;
		DstName = outname;
		this.type = type;
	}

	public String getSrcName() {
		return SrcName;
	}

	public void setSrcName(String srcName) {
		SrcName = srcName;
	}

	public int getSrcPort() {
		return SrcPort;
	}

	public void setSrcPort(int srcPort) {
		SrcPort = srcPort;
	}

	public String getDstName() {
		return DstName;
	}

	public void setDstName(String dstName) {
		DstName = dstName;
	}

	public int getDstPort() {
		return DstPort;
	}

	public void setDstPort(int dstPort) {
		DstPort = dstPort;
	}

	public boolean isOutward() {
		return !type;
	}

	public boolean isInward() {
		return type;
	}

	@Override
	public String toString() {
		return "Line [SrcName=" + SrcName + ", SrcPort=" + SrcPort + ", DstName=" + DstName + ", DstPort=" + DstPort
				+ "]";
	}

}

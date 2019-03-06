package com.pretzel.structure;

public class Line {
	private String SrcBlockName; 
	private int SrcPortIndex;
	private String DstBlockName;
	private int DstPortIndex;
	private String SrcPortName;
	private String DstPortName;
	
	public Line(String SrcBlockName, int SrcPortIndex, String DstBlockName, int DstPortIndex) {
		this.SrcBlockName = SrcBlockName;
		this.DstBlockName = DstBlockName;
		this.SrcPortIndex = SrcPortIndex;
		this.DstPortIndex = DstPortIndex;
		// TODO:
		//this.SrcPortName = SrcPortName; 
		//this.DstPortName = DstPortName;
	}

	public String getSrcBlockName() {
		return SrcBlockName;
	}

	public int getSrcPortIndex() {
		return SrcPortIndex;
	}

	public String getDstBlockName() {
		return DstBlockName;
	}

	public int getDstPortIndex() {
		return DstPortIndex;
	}

	public String getSrcPortName() {
		return SrcPortName;
	}

	public String getDstPortName() {
		return DstPortName;
	}

	@Override
	public String toString() {
		return "Line [SrcBlockName=" + SrcBlockName + ", SrcPortIndex=" + SrcPortIndex + ", DstBlockName="
				+ DstBlockName + ", DstPortIndex=" + DstPortIndex + ", SrcPortName=" + SrcPortName + ", DstPortName="
				+ DstPortName + "]";
	}

}

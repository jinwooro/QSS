package com.pretzel.structure;

public class Line {
	private String srcBlockName, dstBlockName;
	private String srcVarName, dstVarName;
	
	public Line(String srcBlockName, String srcVarName, String dstBlockName, String dstVarName) {
		this.srcVarName = srcVarName;
		this.dstVarName = dstVarName;
		this.srcBlockName = srcBlockName;
		this.dstBlockName = dstBlockName; 
	}
	
	public String getSrcPort() {
		return srcVarName;
	}
	
	public String getDstPort() {
		return dstVarName;
	}
	
	public String getDstBlockName() {
		return this.dstBlockName;
	}
	
	public String getSrcBlockName() {
		return this.srcBlockName;
	}

	@Override
	public String toString() {
		return "\r\nLine [srcBlockName=" + srcBlockName + ", dstBlockName=" + dstBlockName + ", SrcPort=" + srcVarName
				+ ", DstPort=" + dstVarName + "]";
	}
}

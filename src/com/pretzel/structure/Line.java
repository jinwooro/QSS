package com.pretzel.structure;

public class Line {
	private String SrcBlockName, DstBlockName;
	private Port SrcPort, DstPort;
	
	public Line(String SrcBlockName, Port SrcPort, String DstBlockName, Port DstPort) {
		this.SrcPort = SrcPort;
		this.DstPort = DstPort;
		this.SrcBlockName = SrcBlockName;
		this.DstBlockName = DstBlockName; 
	}
	
	public Port getSrcPort() {
		return SrcPort;
	}
	
	public Port getDstPort() {
		return DstPort;
	}
	
	public String getDstBlockName() {
		return this.DstBlockName;
	}
	
	public String getSrcBlockName() {
		return this.SrcBlockName;
	}

	@Override
	public String toString() {
		return "\r\nLine [SrcBlockName=" + SrcBlockName + ", DstBlockName=" + DstBlockName + ", SrcPort=" + SrcPort
				+ ", DstPort=" + DstPort + "]";
	}
}

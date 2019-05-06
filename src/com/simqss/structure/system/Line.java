package com.simqss.structure.system;

/**
 * A Line describes a connection between QSHIOA.
 * @author Jin Woo Ro
 *
 */
public class Line {
	private String srcBlockName, dstBlockName;
	private String srcVarName, dstVarName;
	
	/**
	 * Constructor.
	 * @param srcBlockName Name of the source block.
	 * @param srcVarName Name of the source variable.
	 * @param dstBlockName Name of the destination block.
	 * @param dstVarName Name of the destination variable.
	 */
	public Line(String srcBlockName, String srcVarName, String dstBlockName, String dstVarName) {
		this.srcVarName = srcVarName;
		this.dstVarName = dstVarName;
		this.srcBlockName = srcBlockName;
		this.dstBlockName = dstBlockName; 
	}
	
	/**
	 * @return Returns the source variable name.
	 */
	public String getSrcPortName() {
		return srcVarName;
	}
	
	/**
	 * @return Returns the destination variable name.
	 */
	public String getDstPortName() {
		return dstVarName;
	}
	
	/**
	 * @return Returns the destination block name.
	 */
	public String getDstBlockName() {
		return this.dstBlockName;
	}
	
	/**
	 * @return Returns the source block name.
	 */
	public String getSrcBlockName() {
		return this.srcBlockName;
	}

	@Override
	public String toString() {
		return "Line [srcBlockName=" + srcBlockName + ", dstBlockName=" + dstBlockName + ", SrcPort=" + srcVarName
				+ ", DstPort=" + dstVarName + "]";
	}
}

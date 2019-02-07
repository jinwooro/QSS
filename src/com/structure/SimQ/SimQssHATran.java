package com.structure.SimQ;

public class SimQssHATran {
	private String from, to;
	private String G;
	private String R;
	
	public SimQssHATran(String src, String dst){
		this.from = src;
		this.to = dst;		
	}
	
	public void setGuard(String guard) {
		this.G = guard;
	}
	
	public void setReset(String reset) {
		this.R = reset;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[\n\tfrom=" + from + "\n\tto=" + to + "\n\tG=" + G + "\n\tR=" + R + "]\n";
	}
}

package com.pretzel.structure;

import java.util.Arrays;
import java.util.HashSet;

public class Transition {
	private int src_id, dst_id;
	private HashSet<String> G = new HashSet<String>();
	private HashSet<String> R = new HashSet<String>();
	
	public Transition(int src, int dst){
		src_id = src;
		dst_id = dst;		
	}
	
	public int getSrcId() {
		return src_id;
	}
	
	public int getDstId() {
		return dst_id;
	}
	
	public HashSet<String> getGuard(){
		return G;
	}
	
	public String getGuardString() {
		String s = "";
		boolean first = true;
		for (String temp : G) {
			if (first) {
				s += "(" + temp + ")";
			} else {
				s += " and (" + temp + ")";
			}
		}
		return s;
	}
	
	public HashSet<String> getReset(){
		return R;
	}
	
	public void setGuard(String guard) {
		String members[] = guard.replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
		G.addAll(Arrays.asList(members));
	}
	
	public void setReset(String reset) {
		System.out.println("hell~~~~~ " + reset);
		String members[] = reset.replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
		R.addAll(Arrays.asList(members));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[\n\tfrom=" + src_id + "\n\tto=" + dst_id + "\n\tG=" + G + "\n\tR=" + R + "]\n";
	}
}

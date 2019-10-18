package com.simqss.structure;

import java.util.HashSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NetworkHIOA {
	private String originalFile; // is the file name with the file extension
	private String systemName; // is the file name without the file extension
	private HashSet<HIOA> HIOAs = new HashSet<HIOA>();
	private HashSet<Line> Lines = new HashSet<Line>();
		
	public NetworkHIOA(String filename) {
		this.originalFile = filename;
		this.systemName = filename.substring(filename.lastIndexOf("/")+1, filename.indexOf(".")); 
	}

	public String getFileName() {
		return originalFile;
	}
	
	public String getName() {
		return systemName;
	}
	
	public void addHIOA (HIOA q) {
		HIOAs.add(q);
	}
	
	public HIOA getHIOAbyName(String name) {
		for (HIOA q : HIOAs) {
			if (q.getName().equals(name)) {
				return q;
			}
		}
		return null;
	}
	
	public HashSet<HIOA> getQSHIOAs(){
		return HIOAs;
	}
	
	public void addLine(Line l) {
		Lines.add(l);
	}
	
	public HashSet<Line> getLines(){
		return Lines;
	}
	
	@Override
	public String toString() {
		return "NetworkHIOA [systemName=" + originalFile + ",\r\nQSHIOAs=\r\n" + HIOAs + ",\r\nLines=\r\n" + Lines + "]";
	}
	

}

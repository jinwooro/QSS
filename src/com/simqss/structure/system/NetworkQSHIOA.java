package com.simqss.structure.system;

import java.util.HashSet;

import com.simqss.structure.automata.QSHIOA;

public class NetworkQSHIOA {
	private String originalFile; // is the file name
	private String systemName;
	private HashSet<QSHIOA> QSHIOAs = new HashSet<QSHIOA>();
	private HashSet<Line> Lines = new HashSet<Line>();
		
	public NetworkQSHIOA(String filename) {
		this.originalFile = filename;
		this.systemName = filename.substring(filename.lastIndexOf("/")+1, filename.indexOf(".")); 
	}

	public String getFileName() {
		return originalFile;
	}
	
	public String getName() {
		return systemName;
	}
	
	public void addQSHIOA (QSHIOA q) {
		QSHIOAs.add(q);
	}
	
	public QSHIOA getQSHIOAbyName(String name) {
		for (QSHIOA q : QSHIOAs) {
			if (q.getName().equals(name)) {
				return q;
			}
		}
		return null;
	}
	
	public HashSet<QSHIOA> getQSHIOAs(){
		return QSHIOAs;
	}
	
	public void addLine(Line l) {
		Lines.add(l);
	}
	
	public HashSet<Line> getLines(){
		return Lines;
	}

	@Override
	public String toString() {
		return "NetworkQSHIOA [systemName=" + originalFile + ",\r\nQSHIOAs=\r\n" + QSHIOAs + ",\r\nLines=\r\n" + Lines + "]";
	}
}

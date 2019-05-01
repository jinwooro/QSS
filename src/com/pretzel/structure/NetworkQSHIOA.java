package com.pretzel.structure;

import java.util.HashSet;

import com.pretzel.structure.automata.QSHIOA;

public class NetworkQSHIOA {
	private String systemName; // is the file name
	private HashSet<QSHIOA> QSHIOAs = new HashSet<QSHIOA>();
	private HashSet<Line> Lines = new HashSet<Line>();
		
	public NetworkQSHIOA(String systemName) {
		this.systemName = systemName;
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
	
	public void addLine(Line l) {
		Lines.add(l);
	}
	
	public HashSet<Line> getLines(){
		return Lines;
	}

	@Override
	public String toString() {
		return "NetworkQSHIOA [systemName=" + systemName + ", QSHIOAs=" + QSHIOAs + ", Lines=" + Lines + "]";
	}
}

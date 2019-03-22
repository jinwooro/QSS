package com.pretzel.structure;

import java.util.HashSet;

public class SystemModel {
	private String systemName;
	private HashSet<BlockInterface> Automata = new HashSet<BlockInterface>();
	private HashSet<BlockInterface> SimpleBlocks = new HashSet<BlockInterface>();
	private HashSet<Line> Lines = new HashSet<Line>();
	
	
	public SystemModel(String systemName) {
		this.systemName = systemName;
	}

	public void addAutomaton(BlockInterface b) {
		Automata.add(b);
	}
	
	public void addSimpleBlocks(BlockInterface b) {
		SimpleBlocks.add(b);
	}
	
	public void addLine(Line l) {
		Lines.add(l);
	}
	
	public HashSet<BlockInterface> getAutomata(){
		return Automata;
	}
	
	public HashSet<BlockInterface> getSimpleBlocks(){
		return SimpleBlocks;
	}
	
	public HashSet<Line> getLines(){
		return Lines;
	}

	public BlockInterface getBlockInterfaceByName(String BlockName) {
		HashSet<BlockInterface> union = new HashSet<BlockInterface>();
		union.addAll(Automata);
		union.addAll(SimpleBlocks);
		
		for (BlockInterface b : union) {
			if (b.getInstanceName().equals(BlockName)) {
				return b;
			}
		}
		
		return null;
	}

	@Override
	public String toString() {
		return "SysetmModel [systemName=" + systemName + "\r\nAutomata=" + Automata + "\r\nSimpleBlocks=" + SimpleBlocks
				+ "\r\nLines=" + Lines + "]";
	}
}

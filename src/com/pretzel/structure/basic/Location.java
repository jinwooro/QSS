package com.pretzel.structure.basic;
import java.util.HashSet;

public class Location {
	private String name;
	private int id; // id is used for a number representation of this location
	private HashSet<Formula> invariants = new HashSet<Formula>();
	private HashSet<Formula> ODEs = new HashSet<Formula>();
	
	// For hybrid automata, the set outputUpdates is empty
	private HashSet<Formula> outputUpdates = new HashSet<Formula>();
	private HashSet<Formula> entries = new HashSet<Formula>();
	private HashSet<Transition> outwardTransition = new HashSet<Transition>();
	
	public Location(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public void addInvariant(Formula s) {
		invariants.add(s);
	}
	
	public HashSet<Formula> getInvariants() {
		return invariants;
	}
	
	public void addODE(Formula s) {
		ODEs.add(s);
	}
	
	public HashSet<Formula> getODEs() {
		return ODEs;
	}
	
	public void addOutputUpdateAction(Formula s) {
		outputUpdates.add(s);
	}
	
	public HashSet<Formula> getOutputUpdateActions(){
		return outputUpdates;
	}
	
	public void addEntryAction(Formula s) {
		entries.add(s);
	}
	
	public HashSet<Formula> getEntryActions(){
		return entries;
	}
	
	public void addOutgoingTransition(Transition t) {
		outwardTransition.add(t);
	}

	public HashSet<Transition> getOutgoingTransitions(){
		return outwardTransition;
	}

	@Override
	public String toString() {
		return "Location [name=" + name + ", invariants=" + invariants + ", ODEs=" + ODEs + ", outputUpdates="
				+ outputUpdates + ", entries=" + entries + "]";
	}

}

package com.pretzel.structure.basic;

import java.util.HashSet;

public class Transition {
	private Location src, dst;
	private HashSet<Formula> guards = new HashSet<Formula>();
	private HashSet<Formula> resets = new HashSet<Formula>();
	
	public Transition(Location src, Location dst){
		this.src = src;
		this.dst = dst;
	}
	
	public HashSet<Formula> getGuards(){
		return guards;
	}
	
	public HashSet<Formula> getResets(){
		return resets;
	}
	
	public void addGuard(Formula f) {
		guards.add(f);
	}
	
	public void addReset(Formula f) {
		resets.add(f);
	}

	@Override
	public String toString() {
		return "Transition [src=" + src.getName() + ", dst=" + dst.getName() + ", guards=" + guards + ", resets=" + resets + "]";
	}

	
}

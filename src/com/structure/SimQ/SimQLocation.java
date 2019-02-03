package com.structure.SimQ;
import java.util.HashSet; 

// A class structure for Location which contains,
// 1. Name (String)
// 2. Invariants (set of Strings)
// 3. f (set of Strings)
// 4. h (set of Strings)
public class SimQLocation {
	private String name;
	private HashSet<String> Invariants = new HashSet<String>();
	private HashSet<String> f = new HashSet<String>();
	private HashSet<String> h = new HashSet<String>();
	
	SimQLocation(String name){
		this.name = name;
	}
	
	public void addInvariant(String inv) {
		this.Invariants.add(inv);
	}
	
	public void addVectorF(String f) {
		this.f.add(f);
	}
	
	public void addVectorH(String h) {
		this.h.add(h);
	}
	
	public HashSet<String> getVectorF(){
		return this.f;
	}
	
	public HashSet<String> getVectorH(){
		return this.h;
	}
	
	public String getName() {
		return this.name;
	}
	
	public HashSet<String> getInvariants(){
		return this.Invariants;
	}
}

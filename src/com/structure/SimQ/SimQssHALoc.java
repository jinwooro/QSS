package com.structure.SimQ;
import java.util.HashSet;
import java.util.Set; 

// A class structure for Location which contains,
// 1. Name (String)
// 2. Invariants (set of Strings)
// 3. f (set of Strings)
// 4. h (set of Strings)
public class SimQssHALoc {
	private String name;
	private Set<String> inv = new HashSet<>();
	private Set<String> f = new HashSet<>();
	private Set<String> h = new HashSet<>();
	
	public SimQssHALoc(String loc) {
		this.name = loc;
	}
	
	public void addInvariant(String inv) {
		this.inv.add(inv);
	}
	
	public void addODE(String f) {
		this.f.add(f);
	}
	
	public void addOutputUpdate(String h) {
		this.h.add(h);
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return "SimQssHALoc [name=" + name + ", inv=" + inv + ", f=" + f + ", h=" + h + "]";
	}	
}

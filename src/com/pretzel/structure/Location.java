package com.pretzel.structure;
import java.util.HashSet;
import java.util.Set; 

// A class structure for Location which contains,
// 1. Name (String)
// 2. Invariants (set of Strings)
// 3. f (set of Strings)
// 4. h (set of Strings)
public class Location {
	private String name;
	private int id;
	private Set<String> inv = new HashSet<>();
	private Set<String> f = new HashSet<>();
	private Set<String> h = new HashSet<>();
	private Set<String> init = new HashSet<>();
	
	public Location(String name) {
		this.name = name;
	}
	
	public void addInvariant(String s) {
		inv.add(s);
	}
	
	public void addODE(String s) {
		f.add(s);
	}
	
	public void addOutputUpdate(String s) {
		h.add(s);
	}
	
	public void addExtraInit(String s) {
		init.add(s);
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public Set<String> getf() {
		return f;
	}
	
	public Set<String> geth(){
		return h;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return id;
	}

	public boolean checkByID(int id) {
		if (this.id == id) {
			return true;
		}
		return false;
	}
	
	public boolean checkByName(String name) {
		if (this.name.equals(name)) {
			return true;
		}
		return false;
	}
	
	public String getLHS(String eq, boolean ode) {
		String LHS[] = eq.split("=");
		if (LHS[0].isEmpty()) {
			System.out.println("Error: ODE is defined incorrectly (" + eq + ")");
			System.exit(0);
		}
		if (ode) {
			return LHS[0].split("_dot")[0];
		}
		else {
			return LHS[0];
		}
	}
	
	public String getRHS(String eq) {
		try {
			if (eq.split("=")[1].isEmpty()) {
				System.out.println("Error: ODE has no RHS (" + eq + ")");
				System.exit(0);
			}
		}catch(Exception e){
			System.out.println("Error: invalid equation (" + eq + ")");
		}
		return eq.split("=")[1];
	}
	
	@Override
	public String toString() {
		return "[\n\t name=" + name + "\n\t id=" + id + "\n\t inv=" + inv + "\n\t f=" + f + "\n\t h=" + h + "]\n";
	}

	public String getInvariantString() {
		String s = "";
		boolean first = true;
		for (String temp : inv) {
			if (first) {
				s += "(" + temp + ")";
			} else {
				s += " and (" + temp + ")";
			}
		}
		return s;
	}
}

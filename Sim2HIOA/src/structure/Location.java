package structure;

import java.util.HashSet;
import java.util.Set;
import com.google.gson.Gson;

/**
 * Structure of a single location.
 * @author Jin Woo Ro
 *
 */
public class Location {
	private String name;
	private int id; // id is used for a number representation of this location
	private HashSet<Formula> invariants = new HashSet<Formula>();
	private HashSet<Formula> ODEs = new HashSet<Formula>();	
	private HashSet<Formula> outputUpdates = new HashSet<Formula>(); // This is h
	
	// Extra information to ease the implementation:
	private HashSet<Formula> entries = new HashSet<Formula>();
	
	/**
	 * Constructor.
	 * @param name Name of this location.
	 */
	public Location(String name) {
		this.name = name;
	}
	
	/**
	 * @return Returns the name of this location.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param id Id of this location.
	 */
	public void setID(int id) {
		this.id = id;
	}
	
	/**
	 * @return Id of this location.
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Adds an invariant to this location.
	 * @param s A formula object.
	 */
	public void addInvariant(Formula s) {
		invariants.add(s);
	}
	
	/**
	 * @return Returns all the invariants belongs to this location.
	 */
	public HashSet<Formula> getInvariants() {
		return invariants;
	}
	
	/**
	 * Adds an ODE to this location.
	 * @param s An ODE.
	 */
	public void addODE(Formula s) {
		ODEs.add(s);
	}
	
	/**
	 * @return The set of ODEs in this location.
	 */
	public HashSet<Formula> getODEs() {
		return ODEs;
	}
	
	/**
	 * Adds an output update function.
	 * @param s A Formula
	 */
	public void addOutputUpdateAction(Formula s) {
		outputUpdates.add(s);
	}
	
	/**
	 * @return THe output update functions.
	 */
	public HashSet<Formula> getOutputUpdateActions(){
		return outputUpdates;
	}
	
	/**
	 * Adds an Entry action. Although it is not a part of the formal definition of QSHIOA, it can ease the code generation. 
	 * @param s An Entry action.
	 */
	public void addEntryAction(Formula s) {
		entries.add(s);
	}
	
	/**
	 * @return The entry actions.
	 */
	public HashSet<Formula> getEntryActions(){
		return entries;
	}
	
	@Override
	public String toString() {
		return "Location [name=" + name + ", invariants=" + invariants + ", ODEs=" + ODEs + ", outputUpdates="
				+ outputUpdates + ", entries=" + entries + "]";
	}

	
}

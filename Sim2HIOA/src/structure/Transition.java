package structure;


import java.util.HashSet;

/**
 * The structure of a edge transition 
 * @author Jin Woo Ro
 *
 */
public class Transition {
	private String src, dst;
	private HashSet<Formula> guards = new HashSet<Formula>();
	private HashSet<Formula> resets = new HashSet<Formula>();
	
	/**
	 * Constructor.
	 * @param src The source location.
	 * @param dst The destination location.
	 */
	public Transition(String src, String dst){
		this.src = src;
		this.dst = dst;
	}
	
	/**
	 * A guard is a Formula object.
	 * @return The guards.
	 */
	public HashSet<Formula> getGuards(){
		return guards;
	}
	
	/**
	 * @return Returns the source location.
	 */
	public String getSrc() {
		return src;
	}
	
	/**
	 * @return Returns the destination location.
	 */
	public String getDst() {
		return dst;
	}
	
	/**
	 * @return The resets.
	 */
	public HashSet<Formula> getResets(){
		return resets;
	}
	
	/**
	 * Adds a guard to this transition.
	 * @param f A guard condition.
	 */
	public void addGuard(Formula f) {
		guards.add(f);
	}
	
	/**
	 * Adds a reset relation to this transition.
	 * @param f A reset relation.
	 */
	public void addReset(Formula f) {
		resets.add(f);
	}

	@Override
	public String toString() {
		return "Transition [src=" + src + ", dst=" + dst + ", guards=" + guards + ", resets=" + resets + "]";
	}


	
}

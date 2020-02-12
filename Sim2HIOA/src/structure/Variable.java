package structure;


/**
 * This class describes the structure of a variable.
 * @author Jin Woo Ro
 *
 */
public class Variable {
	private String name;
	private String type;
	private String scope;
	private double initialValue = 0; // default 0
	
	/**
	 * Constructor.
	 * @param name Name of this variable.
	 */
	public Variable(String name) {
		this.name = name;
	}
	
	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return The type.
	 */
	public String getType() {
		return type;
	}
	/** 
	 * Sets the type of this variable.
	 * @param type The type.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return The initial value.
	 */
	public double getInitialValue() {
		return initialValue;
	}
	/**
	 * Sets the initial value.
	 * @param initialValue The value.
	 */
	public void setInitialValue(double initialValue) {
		this.initialValue = initialValue;
	}

	@Override
	public String toString() {
		return "Variable [name=" + name + ", type=" + type + ", scope=" + scope + ", initialValue=" + initialValue
				+ "]";
	}
}

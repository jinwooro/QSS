package com.pretzel.structure;

import java.util.HashSet;

import com.pretzel.structure.basic.Variable;

public class BlockInterface {	
	public enum ObjectType{
		HA,
		HIOA,
		QSHIOA;
	}
	
	private String InstanceName;
	private int id;
	private HashSet<Port> inputs = new HashSet<Port>();
	private HashSet<Port> outputs = new HashSet<Port>();
	private Object object;
	private ObjectType type;
	
	// Sampling rate of -1 means "flexible"
	private double samplingRate = -1;
	
	public BlockInterface(String InstanceName, ObjectType type, Object obj) {
		this.object = obj;
		this.type = type;
		this.InstanceName = InstanceName;
	}
	
	public BlockInterface(String InstanceName, ObjectType type) {
		this.type = type;
		this.InstanceName = InstanceName;
	}
	
	public BlockInterface(String InstanceName) {
		this.InstanceName = InstanceName;
	}
	
	// Setters
	public void setObject(Object o) {
		object = o;
	}
	
	public void setType(ObjectType type) {
		this.type = type;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void addInputPort(Port p) {
		inputs.add(p);
	}
	
	
	public void addOutputPort(Port p) {
		outputs.add(p);
	}	
	
	public void setSamplingRate(double s) {
		samplingRate = s;
	}
	
	// Getters
	public String getInstanceName() {
		return InstanceName;
	}

	public int getId() {
		return id;
	}
	
	public Port getInPortByName(String name) {
		//System.out.println("Requested : " + name + " , result : " + inputs );
		for (Port p : inputs) {
			if (p.getPortName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	public Port getOutPortByName(String name) {
		//System.out.println("Requested : " + name + " , result : " + outputs );
		for (Port p : outputs) {
			if (p.getPortName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	public ObjectType getObjectType() {
		return type;	
	}

	public HashSet<Port> getInputs() {
		return inputs;
	}

	public HashSet<Port> getOutputs() {
		return outputs;
	}

	public double getSamplingRate() {
		return samplingRate;
	}
	
	public Object getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "\r\nBlockInterface [InstanceName=" + InstanceName + "\r\n\tinputs=" + inputs + "\r\n\toutputs="
				+ outputs + "]";
	}
}

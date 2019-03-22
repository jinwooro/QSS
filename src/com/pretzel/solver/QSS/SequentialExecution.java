package com.pretzel.solver.QSS;

import java.util.HashMap;
import java.util.HashSet;

import com.pretzel.structure.BlockInterface;
import com.pretzel.structure.Line;

public class SequentialExecution {
	
	// String = name, Integer = order (1 means firstly executed)
	HashMap<String, Integer> vertices = new HashMap<String, Integer>();
	HashMap<String, String> edges = new HashMap<String, String>();
	HashSet<ExecutionSet> esets = new HashSet<ExecutionSet>();


	// The constructor maps the lines and blocks into a graph, in which the order and execution sets are identified.
	public SequentialExecution(HashSet<Line> lines, HashSet<BlockInterface> blocks) {
		for (BlockInterface b : blocks) {
			vertices.put(b.getInstanceName(), 0);
			
		}
		for (Line l: lines) {
			edges.put(l.getSrcBlockName(), l.getDstBlockName());
		}
		
		
	}

	
	
}

#!/usr/bin/env python3
import json
import sys
import config
import os
from simulator.Simulator import Simulator

# usage : python3 conversion.py "example.mdl"
if __name__ == "__main__":

    # specify simulink mdl file
    simulink_file_path = sys.argv[1]
    filename = os.path.basename(simulink_file_path).split(".")[0] # file name extraction

    # RUN SCRIPT 1: convert simulink/stateflow -> hioa
    cmd = "java -jar runnable/Simulink2HIOA.jar " + simulink_file_path
    os.system(cmd)

    # RUN SCRIPT 2: precompute derivative expressions
    cmd = "python3 runnable/PrecomputeDiff.py generated/" + filename + ".json " + str(config.approx['derivative-order'])
    os.system(cmd)

    # open the input file generated from RUN SCRIPT 2
    file = "generated/" + filename + "-precomputed.json"
    with open(file) as json_file:
        model = json.load(json_file)

    print("Simulation is now started")

    # initiate the simulation 
    simulation = Simulator(model)

    # run
    simulation.run()

    # finish
    simulation.finish()

    print("Simulation is finished")



            
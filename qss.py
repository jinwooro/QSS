#!/usr/bin/env python3

import json
import sys
from pylib.simulator import Simulator 

if __name__ == "__main__":

    file = sys.argv[1]
    # open the json file
    with open(file) as json_file:
        model = json.load(json_file)

    # instantiate the simulation 
    simulation = Simulator(model)

    # run
    simulation.run()

    # finish
    simulation.finish()



            
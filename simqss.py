#!/usr/bin/env python3

import sympy as S
import numpy as np
import sys
import json
import csv
import config
import pylib.structure as Struct

# This function extracts the information from the json file
def load_input_file(input_file_name):
    # open the json file
    with open(input_file_name) as json_file:
        data = json.load(json_file)

    # extract the system name and the order of derivatives
    rank = data['qss_rank']
    sys_name = data['systemName']

    # extract the QSHIOA instances and the inter-connection between the QSHIOAs
    QSHIOAs = { q['name'] : Struct.QSHIOA(q) for q in data['QSHIOAs']}
    Lines = data['Lines']

    return sys_name, rank, QSHIOAs, Lines



if __name__ == "__main__":

    input_file = sys.argv[1]
    
    # system data loading
    system, rank, QSHIOAs, Lines = load_input_file(input_file)

    time = 0
    times = []

    # initialization
    for q in QSHIOAs.values():
        q.initialization()
        q.output_update()

    # simulation steps
    while(time < config.SIMULATION_TIME):

        # record the time instance
        print ('simulation time: ' + str(time))
        times.append(time)

        # I/O exchange (just the current value)
        for line in Lines:
            # obtain the variable object by using "blockName" and "VarName"
            src_output_var = QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]
            dst_input_var = QSHIOAs[ line['dstBlockName'] ].I[ line['dstvarName'] ]
            dst_input_var.token[0].value = src_output_var.token[0].value
            
"""
        # execute any enabled inter-location transitions
        inter_location_triggered = False
        for q in QSHIOAs.values():
            # if there is at least one inter-location transition, indicate it by using a bool flag
            if q.inter_location_transition() == True:
                inter_location_triggered = True
        if inter_location_triggered == True:
            continue 

        # Smooth token exchange
        for index in range(1, rank+1):
            # compute the 'i'th token for each QSHIOA instances
            for q in QSHIOAs.values():
                q.internal_value_update(index)
                q.output_update(index)
            # token exchanges
            for line in Lines:
                src_output_var = QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]
                dst_input_var = QSHIOAs[ line['dstBlockName'] ].I[ line['dstvarName'] ]
                dst_input_var.token[index].value = src_output_var.token[index].value

        # generate qss and compute delta
        delta = []
        for q in QSHIOAs.values():
            q.generate_qss(rank)
            q.check() # for debugging
            delta.append(q.get_delta())
        final_delta = min(delta)

        print ("Delta = " + str(final_delta))

        # intra-location transition execution
        for q in QSHIOAs.values():
            q.intra_location_transition(final_delta)
            q.output_update()

        # get the minimum delta
        time += final_delta

    # writing the results
    with open('result.csv', mode='w') as result_file:
        result_writer = csv.writer(result_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        for row in result_table:
            result_writer.writerow(row)
    
    result_file.close()
"""
from QSHIOA import QSHIOA
import csv
import json
import time as T

# MQSS implementation needs serious code refinement ...
# This is because this implementation is in the middle stage of extending to HOHA
# It worsk as inteneded tho.

class Simulator:
    def __init__(self, data, setup):
        # name of the system for the output file writing
        self.system_name = data['systemName']
        # initialize QSHIOAs
        self.QSHIOAs = { q['name'] : QSHIOA(q) for q in data['QSHIOAs']}
        self.Lines = data['Lines']
        
        self.max_time = setup['max-time']
        self.default_step = setup['default-step']
        self.vtol = setup['vtol']
        self.ttol = setup['ttol']
        self.iter = setup['iteration']

    def run(self):
        # simulation preparation
        time = 0
        count = 1
        self.csv_setup()

        each_execution_time = 0.0
        full_execution_time = 0.0

        beginning = T.time()
        # main loop
        while(True):
            # Load the input variable values by exchanging the tokens
            self.token_exchange()

            # inter-location transition execution
            inter_transition_triggered = False
            for q in self.QSHIOAs.values():
                # if there is at least one inter-location transition, indicate it by using a bool flag
                if q.inter_location_transition(self.vtol) == True:
                    inter_transition_triggered = True
            if inter_transition_triggered == True:
                # record zener transition
                # self.csv_record_system_state(time)
                continue 

            # Update X tokens, then update O tokens 
            for q in self.QSHIOAs.values():
                q.update_token_X(1)
                q.update_token_O(1)
            # exchange (derivative) tokens
            self.token_exchange(1)

            # using the token values, derive variable expressions and exchange the expression.
            self.expression_exchange()

            # write to the file
            self.csv_record_system_state(time, each_execution_time)

            # terminate the simulation when the maximum time is reached
            if time == self.max_time:
                print("Time: %s" % str(time))
                break

            start = T.time() # to measure execution time of each simulation step

            # compute the next time step
            delta = []
            for q in self.QSHIOAs.values():
                qshioa_delta = q.compute_delta(self.iter, self.ttol, self.default_step)
                delta.append(qshioa_delta)
            
            time_step = min(delta)
            if time == 0:
                time_step = 0.001 # initial step size ... 

            # if the next time step exceeds the maximum simulation time,
            # we adjust the time step to stop exactly at the maximum simulation time
            if (time + time_step) > self.max_time:
                time_step = self.max_time - time

            # update the state variables
            for q in self.QSHIOAs.values():
                q.intra_location_transition(time_step)
                q.update_token_O()

            end = T.time() # to measure simulation step execution time
            each_execution_time = end - start

            print("%d, Time: %f, Step: %f, ET: %f" % (count, time, time_step, each_execution_time))
            time += time_step
            count += 1

        finish = T.time()
        full_execution_time = finish - beginning
        print("Simulation Completed (Execution Time %f sec)" % full_execution_time)

    def expression_exchange(self):
        for q in self.QSHIOAs.values():
            q.derive_variable_expression()
            
        for line in self.Lines:
            src_output_var = self.QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]
            dst_input_var = self.QSHIOAs[ line['dstBlockName'] ].I[ line['dstVarName'] ]
            dst_input_var.ex1 = src_output_var.ex1
            dst_input_var.ex2 = src_output_var.ex2

    def token_exchange(self, index=0):
        for line in self.Lines:
            src_output_var = self.QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]
            dst_input_var = self.QSHIOAs[ line['dstBlockName'] ].I[ line['dstVarName'] ]
            new_value = src_output_var.get_token_value(index)
            dst_input_var.set_token_value(new_value, index)

    def csv_record_system_state(self, time, ET):
        row = [time, ET]
        for q in self.QSHIOAs.values():
            row += q.get_current_state()
        self.rows.append(row) # save multiple rows
        # don't open the file for every line,
        # but when we have 50 lines to write
        if len(self.rows) > 50:
            with open(self.system_name + '.csv', 'a') as csvFile:
                writer = csv.writer(csvFile)
                for r in self.rows:
                    writer.writerow(r)
            csvFile.close()
            self.rows.clear()

    def csv_setup(self):
        # this is the 1st row, with column titles
        row = ["Time", "ExecutionTime"]
        for name, q in self.QSHIOAs.items():
            row.append(str(name) + " location")
            row += q.get_variable_names()
    
        # create the csv file
        file_name = self.system_name + '.csv'
        with open(file_name, 'w') as csvFile:
            writer = csv.writer(csvFile)
            writer.writerow(row)
        csvFile.close()
        self.rows = []

    # callback function for the end of simulation
    def finish(self):
        # write the remaining rows 
        with open(self.system_name + '.csv', 'a') as csvFile:
            writer = csv.writer(csvFile)
            for r in self.rows:
                writer.writerow(r)
            csvFile.close()
            self.rows.clear()


if __name__ == "__main__":
    # load setup
    with open("setup.json") as setupFile:
        setup = json.load(setupFile)
    # load input file
    with open(setup['intermediate-file']) as inputFile:
        infile = json.load(inputFile)

    mySim = Simulator(infile, setup)
    mySim.run()
    mySim.finish()
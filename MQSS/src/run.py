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

        # simulation configuration parameters
        self.max_time = setup['max-time']
        self.default_step = setup['default-step']
        self.vtol = setup['vtol']
        self.ttol = setup['ttol']
        self.iter = setup['iteration']
        self.MQSSorder = setup['order'] # MQSS order

        # instantiate objects : QSHIOAs and lines
        self.QSHIOAs = { q['name'] : QSHIOA(q) for q in data['QSHIOAs']}
        self.Lines = data['Lines']

        # initial I/O exchange
        self.IO_exchange()
        
        # output file initialization
        with open(file_name, 'w') as csvFile:
            writer = csv.writer(csvFile)
            top_row = ["Time"] # title row
            for name, q in self.QSHIOAs.items():
                top_row.append(str(name) + " location")
                top_row += q.get_variable_names()
            file_name = self.system_name + '.csv'
            writer.writerow(top_row)
        csvFile.close()
        self.rows = [] # each row = system state at an instance
        
    def run(self):
        # simulation preparation
        time = 0
        count = 1

        # main loop
        while(True):
            # Load the input variable values by exchanging the tokens
            self.token_exchange()
            # output the current time and state
            self.csv_record_system_state(time)

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
            
            # terminate the simulation when the maximum time is reached
            if time == self.max_time:
                print("Time: %s" % str(time))
                break

            start = T.time() # to measure execution time of each simulation step

            # compute the next time step
            delta = []
            for q in self.QSHIOAs.values():
                delta = delta + q.compute_delta(self.iter, self.ttol)
            
            if not delta:
                time_stpe = self.default_step
            else:
                time_step = min(delta)

            if time == 0:
                time_step = 0.001 # initial step size ... 
            elif time_step > 0.1:
                time_step = 0.1

            # if the next time step exceeds the maximum simulation time,
            # we adjust the time step to stop exactly at the maximum simulation time
            if (time + time_step) > self.max_time:
                time_step = self.max_time - time

            # update the state variables
            for q in self.QSHIOAs.values():
                q.intra_location_transition(time_step)
                q.update_token_O()

            print("%d, Time: %f, Step: %f" % (count, time, time_step))
            time += time_step
            count += 1

    def IO_exchange(self):
        for line in self.Lines:
            self.QSHIOAs[ line['dstBlockName'] ].I[ line['dstVarName'] ] = self.QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]

    def token_exchange(self, index=0):
        for line in self.Lines:
            src_output_var = self.QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]
            dst_input_var = self.QSHIOAs[ line['dstBlockName'] ].I[ line['dstVarName'] ]
            new_value = src_output_var.get_token_value(index)
            dst_input_var.set_token_value(new_value, index)

    def csv_record_system_state(self, time):
        row = [time]
        for q in self.QSHIOAs.values():
            row += q.get_current_state()
        self.rows.append(row)

        if len(self.rows) > 50:
            with open(self.system_name + '.csv', 'a') as csvFile:
                writer = csv.writer(csvFile)
                for r in self.rows:
                    writer.writerow(r)
            csvFile.close()
            self.rows.clear()

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
    
    start_time = T.time()
    mySim.run()
    execution_time = T.time() - start_time
    print("Simulation Completed (Execution Time %f sec)" % execution_time)

    mySim.finish()
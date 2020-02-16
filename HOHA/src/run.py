from QSHIOA import QSHIOA
import csv
import sys
import json
import time as T

class Simulator:
    def __init__(self, data, setup):
        # name of the system for the output file writing
        self.system_name = data['systemName']

        # extract data
        self.QSHIOAs = { q['name'] : QSHIOA(q) for q in data['HOHAs']}
        self.Lines = data['Lines']

        self.order = setup['order']
        self.max_time = setup['max-time']
        self.max_step = setup['max-step']
        self.vtol = setup['vtol']

    def run(self):
        time = 0    # simulation time (NOT physical time)
        count = 0   # simulation step count 

        # setup csv writing for measuring
        self.csv_setup()
        # start measuring time for the entire simulation
        start_time = T.time()

        calculation_time = 0.0

        while(True):

        # 1. Exchange IO

            self.exchange_value(index = 0)

        # 2. Inter-location transition (in short, InterLT)

            for q in self.QSHIOAs.values():
                q.inter_location_transition(self.vtol)

        # 3. Exchange smooth-tokens (these are required for taylor approximation)

            # Done iteratively due to the token dependencies
            for index in range(1, self.order + 1):
                for q in self.QSHIOAs.values():
                    q.update_X(index)
                    q.update_O(index)
                self.exchange_value(index)

            # Recording the current tick (before we increase the time)
            self.csv_record_system_state(time)

        # 4. Terminate the simulation at the maximum time

            if time >= self.max_time:
                print("Time: %s" % str(time))
                break
            
        # 5. Compute and collect the delta values from each QSHIOA instances

            calculation_start = T.time() 
            delta = [q.compute_delta(self.order, self.vtol, self.max_step) for q in self.QSHIOAs.values()]
            # next step size is the minimum delta
            time_step = min(delta)
            calculation_end = T.time()
            calculation_time = calculation_time + (calculation_end - calculation_start)

            # make sure the maximum simulation time
            if (time + time_step) >= self.max_time:
                time_step = self.max_time - time

            # update the state variables
            for q in self.QSHIOAs.values():
                q.intra_location_transition(time_step)
                q.update_O()

            print("Step Count:%d, Simulation Time:%f, Step size:%f, ET:%f" % (count, time, time_step, (calculation_end - calculation_start)))
            time += time_step
            count += 1

        end_time = T.time()
        full_execution_time = end_time - start_time
        print("Simulation Completed (Execution Time %.6f sec, calculation time %.6f" % (full_execution_time, calculation_time))

    def exchange_value(self, index=0):
        for line in self.Lines:
            o = self.QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]
            output = o.get_value(index)
            self.QSHIOAs[ line['dstBlockName'] ].I[ line['dstVarName'] ].set_value(output, index)

    def csv_record_system_state(self, time):
        row = [time]
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
        row = ["Time"]
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
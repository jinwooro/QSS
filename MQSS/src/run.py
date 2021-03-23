from QSHIOA import QSHIOA
import csv
import json
import time as T

class Simulator:
    def __init__(self, data, setup):
        # name of the system for the output file writing
        self.system_name = data['systemName']

        # simulation configuration parameters
        self.max_time = setup['max-time']
        self.default_step = setup['default-step']
        self.vtol = float(setup['vtol'])
        self.ttol = float(setup['ttol'])
        self.iter = int(setup['iteration'])

        # instantiate objects : QSHIOAs and lines
        self.QSHIOAs = { q['name'] : QSHIOA(q) for q in data['QSHIOAs']}
        self.Lines = data['Lines']
        
    def run(self):
        # simulation starts
        time = 0            # simulation time
        count = 1           # step counter
        self.csv_setup()    # output recording init
        self.record(time)   # recording the initial system state 

        # main loop
        while(True):

            # Loading the input variable values
            self.IO_exchange() 

            # Inter-location transition
            interLocEnabled = False
            for q in self.QSHIOAs.values():
                interLocEnabled = interLocEnabled or q.inter_location_transition(self.vtol)
            if interLocEnabled: # if there are any inter-loc transition, 
                continue # we don't proceed to the intra-loc transition execution

            # step size calculation
            delta = []
            for q in self.QSHIOAs.values():
                d_step = q.compute_delta(self.iter, self.ttol)
                if d_step : delta.append( min(d_step) )

            # we have no clue about finding the step size, if there is no transition in the system
            if not delta:
                # sys.exit() # we can just exit the simulation
                time_step = self.default_step 
            else:
                time_step = min(delta)

            if count == 1 : time_step = 0.001 # temporarily we use the initial step size 
            if (time + time_step) > self.max_time : time_step = self.max_time - time
            if time == self.max_time: 
                print("Simulation time %f second(s) is reached. End of simulation." % time)
                break

            # Intra-location transition
            for q in self.QSHIOAs.values():
                q.intra_location_transition(time_step)

            print("%d, Time: %f, Step: %f" % (count, time, time_step))
            time += time_step
            count += 1
            self.record(time)

    def IO_exchange(self):
        for line in self.Lines:
            self.QSHIOAs[ line['dstBlockName'] ].I[ line['dstVarName'] ] = self.QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]

    def record(self, time):
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

    # instantiate a simulator object
    mySim = Simulator(infile, setup)
    
    start_time = T.time()
    mySim.run()
    execution_time = T.time() - start_time

    print("Execution Time %f sec" % execution_time)
    mySim.finish()
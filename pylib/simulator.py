import config
from pylib.QSHIOA import QSHIOA
from pylib.LevelCrossingDetection import *
import csv

class Simulator:

    def __init__(self, data):
        self.rank = data['qss_rank']
        self.system_name = data['systemName']

        self.select_solver()

        # extract the QSHIOA instances and the inter-connection between the QSHIOAs
        self.QSHIOAs = { q['name'] : QSHIOA(q) for q in data['QSHIOAs']}
        self.Lines = data['Lines']

    def select_solver(self):
        self.solver = Modified_QSS

    def run(self):
        time = 0

        # initialization
        for q in self.QSHIOAs.values():
            q.initialization()
            q.update_O()

        self.csv_setup()

        while(time < config.SIMULATION_TIME):

            # current value exchange
            self.token_exchange()

            # inter-location transition execution
            inter_transition_triggered = False
            for q in self.QSHIOAs.values():
                # if there is at least one inter-location transition, indicate it by using a bool flag
                if q.inter_location_transition() == True:
                    inter_transition_triggered = True
            if inter_transition_triggered == True:
                continue 

            # Smooth token exchange
            for index in range(1, self.rank+1):
                # Update X tokens, then update O tokens 
                for q in self.QSHIOAs.values():
                    q.update_X(index)
                    q.update_O(index)
                # exchange (derivative) tokens
                self.token_exchange(index)

            # before updating the variables, write to the output file
            self.csv_record_system_state(time)

            # compute the next time step
            temp = []
            for q in self.QSHIOAs.values():
                delta = q.compute_delta(self.solver)
                temp.append(delta)

            if min(temp) == 0: #or min(temp) >= config.MAX_STEP_SIZE:
                time_step = config.MAX_STEP_SIZE # a predefined fixed time-progression
            else:
                time_step = min(temp)

            # update the state variables
            for q in self.QSHIOAs.values():
                q.intra_location_transition(time_step)
                q.update_O()
            
            print("Time: %s, Step: %s" %(str(time), str(time_step)))
            time += time_step
        
        print("Simulation is finished")
        self.finish()

    def token_exchange(self, i=0):
        for line in self.Lines:
            src_output_var = self.QSHIOAs[ line['srcBlockName'] ].O[ line['srcVarName'] ]
            dst_input_var = self.QSHIOAs[ line['dstBlockName'] ].I[ line['dstVarName'] ]
            dst_input_var.token[i].value = src_output_var.token[i].value


    def csv_setup(self):
        row = ["Time"]
        for name, q in self.QSHIOAs.items():
            row.append(str(name) + " location")
            name_list = q.get_list_of_names()
            row += name_list
    
        file_name = self.system_name + '.csv'
        with open(file_name, 'w') as csvFile:
            writer = csv.writer(csvFile)
            writer.writerow(row)
        csvFile.close()
        self.rows = []

    def csv_record_system_state(self, time):
        row = [time]
        for q in self.QSHIOAs.values():
            row += q.get_states(tokens = True)
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

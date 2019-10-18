from .QSHIOA import QSHIOA
import config
import csv
import sys

class Simulator:
    def __init__(self, data):
        # name of the system for the output file writing
        self.system_name = data['systemName']

        # initialize QSHIOAs
        self.QSHIOAs = { q['name'] : QSHIOA(q) for q in data['QSHIOAs']}
        self.Lines = data['Lines']

    def run(self):
        # simulation preparation
        time = 0
        count = 1
        self.csv_setup()
        
        # main loop
        while(True):
            # Load the input variable values by exchanging the tokens
            self.token_exchange()

            # inter-location transition execution
            inter_transition_triggered = False
            for q in self.QSHIOAs.values():
                # if there is at least one inter-location transition, indicate it by using a bool flag
                if q.inter_location_transition() == True:
                    inter_transition_triggered = True
            if inter_transition_triggered == True:
                # record zener transition
                # self.csv_record_system_state(time)
                continue 

            # Smooth token exchange
            for index in range(1, config.approx['derivative-order'] + 1):
                # Update X tokens, then update O tokens 
                for q in self.QSHIOAs.values():
                    q.update_token_X(index)
                    q.update_token_O(index)
                # exchange (derivative) tokens
                self.token_exchange(index)

            # using the token values, derive variable expressions and exchange the expression.
            self.expression_exchange()

            # write to the file
            self.csv_record_system_state(time)

            # terminate the simulation when the maximum time is reached
            if time == config.MAX_TIME:
                print("Time: %s" % str(time))
                break

            # compute the next time step
            delta = []
            for q in self.QSHIOAs.values():
                flag, ans = q.compute_delta()
                if flag == False:
                    sys.exit()                
                delta.append(ans)
            
            time_step = min(delta)

            if time_step > config.MAX_STEP:
                time_step = config.MAX_STEP

            # if the next time step exceeds the maximum simulation time,
            # we adjust the time step to stop exactly at the maximum simulation time
            if (time + time_step) > config.MAX_TIME:
                time_step = config.MAX_TIME - time

            # update the state variables
            for q in self.QSHIOAs.values():
                q.intra_location_transition(time_step)
                q.update_token_O()

            print("Count: %d, Time: %s, Step: %s" %(count, str(time), str(time_step)))
            time += time_step
            count += 1

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

    def csv_record_system_state(self, time):
        row = [time]
        for q in self.QSHIOAs.values():
            row += q.get_current_state()
        self.rows.append(row) # save multiple rows
        # don't open the file for every line,
        # but when we have 50 lines to write
        if len(self.rows) > 50:
            with open("generated/" + self.system_name + '.csv', 'a') as csvFile:
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
        file_name = "generated/" + self.system_name + '.csv'
        with open(file_name, 'w') as csvFile:
            writer = csv.writer(csvFile)
            writer.writerow(row)
        csvFile.close()
        self.rows = []

    # callback function for the end of simulation
    def finish(self):
        # write the remaining rows 
        with open("generated/" + self.system_name + '.csv', 'a') as csvFile:
            writer = csv.writer(csvFile)
            for r in self.rows:
                writer.writerow(r)
            csvFile.close()
            self.rows.clear()
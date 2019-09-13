# Defines QSHIOA and Trnasition classes
from pylib.Equation import Equation
from pylib.Variable import Variable
from pylib.Transition import Transition
from pylib.LevelCrossingDetection import *

class QSHIOA:

    def __init__(self, data):
        self.instance_name = data['name']
        self.qshioa_data = data

        self.location_names = { loc['id'] : loc['name'] for loc in data['locations']}

        # ODEs and update functions
        self.ODEs = { loc['id'] : [Equation(fx, 'ODE') for fx in loc['ODEs']] for loc in data['locations']}
        self.Updates = { loc['id'] : [Equation(hx, 'NORMAL') for hx in loc['outputUpdates']] for loc in data['locations']}

        # Egress transitions
        self.transitions = { loc['id'] : [] for loc in data['locations']}
        for t in data['transitions']:
            self.transitions[t['sid']].append(Transition(t))

        # initial location
        self.loc = data['initialLocation']['id']

        # variable objects
        self.I = { i['name'] : Variable(i) for i in data['I']}
        self.O = { o['name'] : Variable(o) for o in data['O']}
        self.X = { x['name'] : Variable(x) for x in data['X_C']}


    def initialization(self):
        # there are three ways to set initial values in Simulink model
        # 1. predefined initial value
        for v in self.qshioa_data['I']:
            self.I[v['name']].set_current_value(v['initialValue'])
        for v in self.qshioa_data['O']:
            self.O[v['name']].set_current_value(v['initialValue'])
        for v in self.qshioa_data['X_C']:
            self.X[v['name']].set_current_value(v['initialValue'])
        # 2. reset on the first transition
        for fx in self.qshioa_data['initialization']:
            if fx['LHS'] in self.X:
                self.X[fx['LHS']].set_current_value(fx['RHS'])
            elif fx['LHS'] in self.O:
                self.O[fx['LHS']].set_current_value(fx['RHS'])
        # 3. initial location entry actions
        for en in self.qshioa_data['initialLocation']['entries']:
            if en['LHS'] in self.X:
                self.X[en['LHS']].set_current_value(fx['RHS'])
            elif en['LHS'] in self.O:
                self.O[en['LHS']].set_current_value(fx['RHS'])
        
        self.transition_enabled_flag = False

    def update_O(self, index = 0):
        for h in self.Updates[self.loc]:
            new_token = h.compute_token(self.X, index)
            self.O[h.subject].token[index].set_value(new_token)

    def update_X(self, index = 0):
        input_arguments = {**self.I, **self.X}
        for f in self.ODEs[self.loc]:
            new_token = f.compute_token(input_arguments, index)
            self.X[f.subject].token[index].set_value(new_token)
            
    def reset_tokens(self):
        for var in {**self.I, **self.O, **self.X}.values():
            var.reset_derivatives()

    def inter_location_transition(self):
        # iterate over the egress transitions
        if self.transition_enabled_flag == True:
            return False

        for t in self.transitions[self.loc]:
            if t.guards_enabled(self.I, self.X) == True:
                t.reset(self.O, self.X)
                self.loc = t.next_location
                self.update_O()
                print("QSHIOA:%s inter-location transition" % self.instance_name)
                self.transition_enabled_flag = True
                return True
        return False

    def compute_delta(self, level_detection_function):
        temp = [config.MAX_STEP_SIZE]
        for t in self.transitions[self.loc]:
            guard = t.get_guard()
            time = level_detection_function(guard, {**self.I, **self.X})
            temp.append(time)

        # if there is no time computed
        #if len(temp) == 0:
        #    temp.append(config.MAX_STEP_SIZE)
        return min(temp)

    def intra_location_transition(self, time):
        self.transition_enabled_flag = False
        for var in self.X.values():
            var.compute_next_value(time)

    def get_states(self, tokens=False):
        location_name = self.location_names[self.loc]
        state = [location_name]
        variables = {**self.X, **self.I, **self.O}
        for v in self.list_of_names:
            if tokens == False:
                state.append(variables[v].token[0].get_value())
            else:
                tok_string = ":".join(map(str, variables[v].get_token_values()))
                state.append(tok_string)
        return state

    def get_list_of_names(self, tokens=False):
        self.list_of_names = [ name for name in {**self.X, **self.I, **self.O} ]
        return self.list_of_names

    def check(self):
        variables = {**self.I, **self.O, **self.X}
        print(self.instance_name + "----------------------------")
        print("Location: " + str(self.loc))
        for key, value in variables.items():
            print( str(value.current_value()))
        print("--------------------------------------")

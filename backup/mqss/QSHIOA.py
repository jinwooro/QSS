# Defines QSHIOA and Trnasition classes
from mqss.Equation import Equation
from mqss.Variable import Variable
from mqss.Transition import Transition
from mqss.LevelCrossingDetection import *

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


    ######################################################################
    def evaluate_token_O (self, token_order = 0):
        for h in self.Updates[self.loc]:
            new_value = h.compute_token(self.X, token_order)
            self.O[h.subject].set_token_value(new_value, token_order)

    def evaluate_token_X (self, token_order = 0):
        union = {**self.I, **self.X}
        for f in self.ODEs[self.loc]:
            new_value = f.compute_token(union, token_order)
            self.X[f.subject].set_token_value(new_value, token_order)
            
    def reset_tokens(self):
        for var in {**self.I, **self.O, **self.X}.values():
            var.reset_derivative_tokens()

    def inter_location_transition(self):
        # iterate over the egress transitions
        if self.transition_enabled_flag == True:
            return False

        for t in self.transitions[self.loc]:
            flag = t.trigger_if_possible(self.I, self.O, self.X)
            if flag == True:
                self.loc = t.get_destination()
                self.evaluate_token_O()
                self.reset_tokens()
                print("QSHIOA:%s inter-location transition" % self.instance_name)
                self.transition_enabled_flag = True
                return True
        return False

    def intra_location_transition(self, time):
        self.transition_enabled_flag = False
        for var in self.X.values():
            var.evolve_based_on_time(time)

    def get_egress_guards(self):
        return self.transitions[self.loc]

    def compute_delta(self, level_detection_function):
        temp = [config.MAX_STEP_SIZE]
        for t in self.transitions[self.loc]:
            guard = t.get_guard()
            time = level_detection_function(guard, {**self.I, **self.X})
            temp.append(time)

        return min(temp)

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

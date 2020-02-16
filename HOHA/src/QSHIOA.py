# Defines QSHIOA and Trnasition classes
from Variable import Variable
from Location import Location
from Transition import Transition
from Calculation import *
from PrecomputedLagrange import *
import math

class QSHIOA:

    def __init__(self, data):
        # name
        self.instance_name = data['name']
        # locations
        self.locations = { loc['id'] : Location( loc ) for loc in data['locations']}
        # outgoing transitions from each location
        for t in data['transitions']:
            tid = t['sid']
            transition = Transition(t)
            self.locations[tid].add_exit_transition(transition)

        # current location setup
        self.loc_id = data['initialLocation']['id'] # initial location id
        self.current_location = self.locations[self.loc_id]

        # variable objects
        self.I = { i['name'] : Variable(i) for i in data['I']}
        self.O = { o['name'] : Variable(o) for o in data['O']}
        self.X = { x['name'] : Variable(x) for x in data['X_C']}

        # Simulink has multiple options for initialization
        # 1. reset on the first transition
        for fx in data['initialization']:
            if fx['LHS'] in self.X:
                self.X[fx['LHS']].set_current_value(fx['RHS'])
            elif fx['LHS'] in self.O:
                self.O[fx['LHS']].set_current_value(fx['RHS'])
        # 2. initial location entry actions
        for en in data['initialLocation']['entries']:
            if en['LHS'] in self.X:
                self.X[en['LHS']].set_current_value(fx['RHS'])
            elif en['LHS'] in self.O:
                self.O[en['LHS']].set_current_value(fx['RHS'])

        self.update_O(index = 0)

    def update_O (self, index = 0):
        self.current_location.compute_O(self.X, self.O, index)

    def update_X (self, index = 0):
        self.current_location.compute_X(self.I, self.X, index)

    def inter_location_transition(self, vtol):
        flag, loc_id = self.current_location.take_transition(self.I, self.O, self.X, vtol)
        if flag == True: # inter-location transition is triggered
            self.current_location = self.locations[loc_id]
            self.loc_id = loc_id
            self.update_O()
            for var in {**self.I, **self.O, **self.X}.values():
                var.reset_gradients()
            print("QSHIOA:%s %s inter-location transition" % (self.instance_name, self.current_location.name)) 

    def intra_location_transition(self, time):
        for x in self.X.values():
            derivatives = x.values
            new_value = derivatives[0] # current value
            for i in range(1, len(derivatives)):
                new_value = new_value + derivatives[i] * (time ** i) / math.factorial(i)
            x.set_current_value(new_value)

    # returns a collection of the variable names
    def get_variable_names(self):
        names = [ name for name in {**self.X, **self.I, **self.O} ]
        return names

    def compute_delta(self, order, vtol, max_step):
        LTE = vtol
        Var = {**self.I, **self.X} # union of I and X
        name = self.instance_name
        loc = self.loc_id

        # (name, loc) is the key to find precomputed Lagrange error expressions
        Lagrange_equations = lagrange_loc[(name, loc)] (Var)
        validity_time = calculate_validity_time(Lagrange_equations, LTE, order, max_step)

        exit_guards = [ t.guards for t in self.current_location.Transitions]
        zero_crossing_time = calculate_zero_crossing_time(exit_guards, Var)

        if zero_crossing_time == None: 
            return validity_time
        
        if validity_time < zero_crossing_time:
            return validity_time
        else:
            return zero_crossing_time

    # returns a collection of the current value of variables
    def get_current_state(self, tokens=False):
        loc_name = self.current_location.get_name()
        state = [loc_name]
        variables = {**self.X, **self.I, **self.O}

        # if tokens == false, then only returns the current value of the state variables
        if tokens == False:
            state += [ var.get_current_value() for var in variables.values() ]
        else:
            state += [ ":".join(map(str, var.get_token_values())) for var in variables.values() ] 

        return state
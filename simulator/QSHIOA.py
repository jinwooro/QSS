# Defines QSHIOA and Trnasition classes
from .Variable import Variable
from .Location import Location
from .Transition import Transition
from .Util import *
import config

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
            self.locations[tid].add_outgoing_transition(transition)

        # current location setup
        loc_id = data['initialLocation']['id'] # initial location id
        self.current_location = self.locations[loc_id]

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
        self.update_token_O()

    def update_token_O (self, token_index = 0):
        self.current_location.compute_O_token(self.X, self.O, token_index)

    def update_token_X (self, token_index = 0):
        self.current_location.compute_X_token(self.I, self.X, token_index)

    def inter_location_transition(self):
        flag, loc_id = self.current_location.take_transition(self.I, self.O, self.X)
        if flag == True: # transition is triggered
            self.current_location = self.locations[loc_id]
            # execute h in the new location
            self.update_token_O()
            self.reset_tokens()
            print("QSHIOA:%s %s inter-location transition" % (self.instance_name, self.current_location.name))

    def reset_tokens(self):
        for var in {**self.I, **self.O, **self.X}.values():
            var.reset_values()

    def intra_location_transition(self, time):
        for x in self.X.values():
            x.evolve_based_on_time(time)

    def derive_variable_expression(self):
        if config.approx['approx'] == 'mqss12':
            # you need to parse the current location, which includes 
            # the derivative function f. Needed for x(t) = f(q(t), t)
            mqss12_derive(self.current_location, self.I, self.X, self.O)
        elif config.approx['approx'] == 'taylor12':
            taylor12_derive(self.X, self.O)
        elif config.approx['approx'] == 'rk12':
            rk12_derive(self.current_location, self.I, self.X, self.O)

    def get_egress_guards(self):
        return self.transitions[self.loc]

    # returns a collection of the variable names
    def get_variable_names(self):
        names = [ name for name in {**self.X, **self.I, **self.O} ]
        return names

    def compute_delta(self):
        zero_crossings = [] # normal zero-crossings, which satisfies modifid delta-q
        hits = [] # hit is a special zero-crossing, which satisfies the guard condition itself
        logs = [] # to record the log messages
        
        if config.algo['name'] == 'algorithm1':
            for t in self.current_location.Transitions:
                ans, hit, log = algorithm1(t, self.I, self.X, config.algo['iteration'], config.algo['ttol']) 
                # if this delta value satisfies the guard condition without modifying delta-q,
                # save it to a special array, hits
                if hit == True:
                    hits.append(ans)
                else:
                    zero_crossings.append(ans) 
                logs.append(log)
        # elif ... other algorithm
        
        # filter out the delta values smaller than the sensitivity parameter
        zero_crossings = [i for i in zero_crossings if i > config.DELTA_SENSITIVITY]
        final_delta = hits + zero_crossings
    
        # if there is no delta found with the specified iteration depth, 
        # we can do either:
        # 1. terminate the simulation.
        # 2. continue by forcing the time to move forward
        if len(final_delta) == 0 and config.FORCE_FORWARD == False:
            print ("Simulation aborted due to no delta is found at QSHIOA:" + self.instance_name + ", location: " + self.current_location.name)
            print ("Try to relax the tolerance (ttol), or increase the iteration parameter")
            print ("Alternatively, set 'force-forward' to true")
            print ("Problem details are as follow:")
            for log in logs:
                for l in log:
                    print (l)
            return False, 0
        elif len(final_delta) == 0 and config.FORCE_FORWARD == True:
            if config.DEBUG:
                for log in logs:
                    for l in log:
                        print (l)
            return True, config.ESCAPE_STEP
        # normally, we choose the minimum delta value
        else:
            return True, min(final_delta)

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
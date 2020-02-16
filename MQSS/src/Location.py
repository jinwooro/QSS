import sympy as S
from Util import *

class Location:

    def __init__(self, json_data):
        # location name
        self.name = json_data['name'] 
        # ODEs, key = x_name, value = array of sympy derivative format
        self.ODEs = { f['LHS'] : S.sympify(f['derivatives']) for f in json_data['ODEs'] }
        # updates, key = x_name, value = array of sympy derivative format
        self.Updates = { h['LHS'] : S.sympify(h['derivatives']) for h in json_data['outputUpdates'] }
        self.Transitions = []

    def add_outgoing_transition(self, tran):
        self.Transitions.append(tran)

    def compute_O_token(self, X, O, token_index):
        for o, H in self.Updates.items():
            h = H[token_index] # select the correct expression based on the token index 
            new_value = calculate_value(h, X)
            O[o].set_token_value(new_value, token_index)

    def compute_X_token(self, I, X, token_index):
        union = {**I, **X}
        for x_dot, F in self.ODEs.items():
            f = F[token_index]
            new_value = calculate_value(f, union)
            name = x_dot.split("_dot")[0]
            X[name].set_token_value(new_value, token_index)

    def take_transition(self, I, O, X, vtol):
        # for each transition, 
        for tran in self.Transitions:
            flag = tran.trigger(I, O, X, vtol)
            # True, if the transition is triggered
            if flag == True:
                return True, tran.destination
        return False, 0

    def get_f(self):
        f_vector = { x_dot.split("_dot")[0] : f[1] for x_dot, f in self.ODEs.items() }
        return f_vector

    def get_h(self):
        h_vector = { o : h[0] for o, h in self.Updates.items() } 
        return h_vector

    def get_Updates(self):
        return self.Updates
    
    def get_name(self):
        return self.name

    def get_transitions(self):
        return self.Transitions






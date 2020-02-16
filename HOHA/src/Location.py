import sympy as S
from Calculation import substitution

class Location:

    def __init__(self, json_data):
        # location name
        self.name = json_data['name'] 

        # key = x_name, value = sympy derivative array
        self.ODEs = { f['LHS'] : S.sympify(f['derivatives']) for f in json_data['ODEs'] }
        
        # key = x_name, value = sympy derivative array
        self.Updates = { h['LHS'] : S.sympify(h['derivatives']) for h in json_data['outputUpdates'] }
        self.Transitions = []

    def add_exit_transition(self, tran):
        self.Transitions.append(tran)

    def compute_O(self, X, O, index):
        for name, equations in self.Updates.items():
            h = equations[index] 
            new_value = substitution(h, X)
            O[name].set_value(new_value, index)

    def compute_X(self, I, X, index):
        union = {**I, **X}
        for x_dot, equations in self.ODEs.items():
            f = equations[index]
            new_value = substitution(f, union)
            name = x_dot.split("_dot")[0]
            X[name].set_value(new_value, index)

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

    def get_exit_transitions(self):
        return self.Transitions






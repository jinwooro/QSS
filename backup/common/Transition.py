# Defines QSHIOA and Trnasition classes
import sympy as S
from python import config

class Transition:
    def __init__(self, data):
        self.current_location = data['sid'] # source location id = where the transition starts
        self.next_location = data['did'] # destination location id = where the transition ends
        self.resets = [(r['LHS'], S.sympify(r['RHS'])) for r in data['resets']]
        self.guards = [S.sympify(g['rearranged']) for g in data['guards']]

    # returns true if this transition is enabled (vtol is the tolerance)
    def guards_enabled(self, I, X):
        # check each guard if it is true
        var_values = { key : var.current_value() for key, var in {**I, **X}.items()}
        for guard in self.guards:
            g = guard
            # print("guard checking: " + str(g) + " with subs " + str(var_values))
            
            for symbol, value in var_values.items():
                g = g.subs(S.sympify(symbol), S.sympify(value))
    
            if abs(g) <= config.LEVEL_CROSSING_TOLERANCE: # the first detection of any enabled guard,
                return True

        return False

    def get_guard(self):
        return self.guards

    # assign new values to X and O variables
    def reset(self, O, X):
        X_pre = X
        for r in self.resets:
            name = r[0]
            equation = r[1]
            if equation.is_constant():
                if name in X : X[name].set_current_value(equation)
                if name in O : O[name].set_current_value(equation)
            else:
                for key, var in X_pre.items():
                    equation = equation.subs(key, var.current_value())
                if name in X : X[name].set_current_value(equation)
                if name in O : O[name].set_current_value(equation)

# Defines QSHIOA and Trnasition classes
import sympy as S
import config

class Transition:
    def __init__(self, data):
        self.source = data['sid'] # source location id
        self.destination = data['did'] # destination location id
        # a reset is a pair (LHS, RHS), representing an equation
        self.resets = [(r['LHS'], S.sympify(r['RHS'])) for r in data['resets']] 
        # a guard is a pair (ZC, comparator), where ZC is a zero-crossing expression, and comparator is the inequality
        self.guards = [(S.sympify(g['rearranged']), g['relation']) for g in data['guards']] 

    def get_guards(self):
        exprs = [ g[0] for g in guards ]
        return exprs

    # Trigger this transition,
    # returns false if cannot be triggered
    # returns true if triggered successfully
    def trigger(self, I, O, X):
        union = {**I, **X}
        
        # detect if the guard condition is not satisfied
        for g, relation in self.guards:
            # evaluate the guard
            for name, variable in union.items():
                g = g.subs(S.sympify(name), variable.get_current_value())
            # filter the not satisfied situations
            if (relation == "<=" or relation == "<") and (g > config.VTOL):
                return False
            elif (relation == ">=" or relation == ">") and (g < -config.VTOL):
                return False

        # if the guards are satisifed, perform reset relation
        for r in self.resets:
            name = r[0] # LHS
            equation = r[1] # RHS
            if equation.is_constant():
                if name in X:
                    X[name].set_current_value(equation)
                elif name in O:
                    O[name].set_current_value(equation)
            else:
                for key, var in X.items():
                    equation = equation.subs(key, var.current_value())
                if name in X:
                    X[name].set_current_value(equation)
                if name in O:
                    O[name].set_current_value(equation)

        # passing all the filtering implies that this transition is enabled
        return True

# Defines QSHIOA and Trnasition classes
import sympy as S

class Transition:
    def __init__(self, data):
        self.source = data['sid'] # source location id
        self.destination = data['did'] # destination location id
        # a reset is a pair (LHS, RHS), representing an equation
        self.resets = [(r['LHS'], S.sympify(r['RHS'])) for r in data['resets']] 
        # a guard is a pair (ZC, comparator), where ZC is the zero-crossing form, and comparator is the inequality
        self.guards = [(S.sympify(g['rearranged']), g['relation']) for g in data['guards']] 

    # Trigger this transition,
    # returns false if cannot be triggered
    # returns true if triggered successfully
    def trigger(self, I, O, X, vtol):
        union = {**I, **X}
        
        # detect if the guard condition is not satisfied
        for (guard, relation) in self.guards:
            g0 = guard[0] # the guard expression (non-derivative)
            # evaluate the guard
            for variable in union.values():
                g0 = g0.subs(variable.get_symbol(), variable.get_current_value())
            # filter the not satisfied situations
            if (relation == "<=" or relation == "<") and (g0 > vtol):
                return False
            elif (relation == ">=" or relation == ">") and (g0 < -vtol):
                return False
            elif (relation == "=="):
                if (g0 > vtol) or (g0 < -vtol):
                    return False

        # if the guards are satisifed, perform the reset assignment
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
                    equation = equation.subs(key, var.get_current_value())
                if name in X:
                    X[name].set_current_value(equation)
                if name in O:
                    O[name].set_current_value(equation)

        # passing all the filtering implies that this transition is enabled
        return True

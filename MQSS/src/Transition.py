# Defines QSHIOA and Trnasition classes
import sympy as S

class Transition:
    def __init__(self, data):
        self.source = data['sid'] # source location id
        self.destination = data['did'] # destination location id
        self.resets = [(r['LHS'], S.sympify(r['RHS'])) for r in data['resets']] 
        self.guards = [ (S.sympify(g['LHS']), g['relation']) for g in data['guards']] 

    def is_enabled(self, I, X, vtol):
        Variables = [ (S.sympify(name), value) for name, value in {**I, **X}.items() ]
        # all the guards have the same format: e.g., g(X, I) < 0, where RHS is always zero
        for (guard, relation) in self.guards:
            # current guard value
            g0 = guard.subs(Variables)
            if abs(g0) <= vtol : g0 = 0 
            # note that "<" is also considered as "<=" based on the right-hand limit
            if (relation == "<=" or relation == "<") and (g0 > 0): 
                # any unsatisfied guard results in disabled transition. Return False directly.
                return False
            elif (relation == ">=" or relation == ">") and (g0 < 0):
                return False
            if (relation == "==") and (g0 != 0):
                return False
        return True
    
    def R(self, I, O, X):
        Variables = [ (S.sympify(name), value) for name, value in {**I, **X}.items() ]
        for lhs, rhs in self.resets:
            if rhs.is_constant():
                if lhs in X : X[lhs] = float(rhs)
                else : O[lhs] = float(rhs)
            else:
                new_value = rhs.subs(Variables)
                if lhs in X : X[lhs] = float(new_value)
                else : O[lhs] = float(new_value)

    def get_guard_expressions(self):
        g_exp = [ g[0] for g in self.guards ]

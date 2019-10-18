import sympy as S

class Equation:
    def __init__(self, fx, type='NORMAL'):
        if type == 'NORMAL':
            self.subject = fx['LHS']
        elif type == 'ODE':
            self.subject = fx['LHS'].split('_dot')[0]
        # store the derivative expressions (including zero order)
        self.derivatives = [S.sympify(der) for der in fx['derivatives']]

    # comptue the tokens based on the input variable values
    def compute_token(self, variables, index):
        value = self.derivatives[index]
        for key, v in variables.items():
            # reversed order is very important, because we want to substitute the highest rank derivative first
            for pair in reversed(v.get_smooth_token()):
                value = value.subs(pair[0], pair[1])
        return value 

    def __repr__(self):
        return str(self.subject) + str(self.derivatives)

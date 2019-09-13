import sympy as S

class Equation:
    def __init__(self, fx, type='NORMAL'):
        if type == 'NORMAL':
            self.subject = fx['LHS']
        elif type == 'ODE':
            self.subject = fx['LHS'].split('_dot')[0]
        # store the derivative expressions
        self.derivatives = [S.sympify(der) for der in fx['derivatives']]

    # comptue the token based on the input variable values
    def compute_token(self, variables, index):
        equation_to_solve = self.derivatives[index]
        # substitute all the variables with its value
        for key, var in variables.items():
            # reversed order is very important, because we want to substitute the highest rank derivative first
            for token in reversed(var.token):
                equation_to_solve = equation_to_solve.subs(S.sympify(token.symbol), token.value)
        return equation_to_solve # this equation must be solved and returned

    def to_string(self):
        string = str(self.subject) + str(self.derivatives)
        return string

    """
    def get_qss(self, variables, initial_value, mode):
        print("Test: " + self.subject + "=" + str(self.derivatives[1]))
        ode = self.derivatives[1]
        for var in variables.values():
            if mode == 'LOW' :
                ode = ode.subs(S.sympify(var.symbol), S.sympify(var.qss_low_intp))
            if mode == 'HIGH' :
                ode = ode.subs(S.sympify(var.symbol), S.sympify(var.qss_high_intp))

        final_qss_equation = S.sympify(initial_value + ode * S.sympify('t'))
        return final_qss_equation 
    """
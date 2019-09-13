
import sympy as S
import numpy as np


def exchange_qss(a, b):
    a.qss_high = b.qss_high
    a.qss_low = b.qss_low

def solve(expression, subs_dict):
    expr = expression
    for symbol, formula in subs_dict.items():
        expr = expr.subs(S.sympify(symbol), S.sympify(formula))
    return expr

def poly_solver(poly_expr):
    # extract coefficients
    c = S.Poly(poly_expr, S.sympify('t')).coeffs()
    # revert the order
    c.reverse()
    # find the roots using numpy
    ans = np.roots(c)
    # filter the complex roots
    ans = ans.real[abs(ans.imag) < 1e-5]
    # filter values that are less than zero
    ans = ans[ans >= 0]
    return ans


class Token:
    def __init__(self, symbol, value):
        self.symbol = symbol
        self.value = float(value)

    # the setter and getter are used to ensure that 'value' is always float type
    def get_value(self):
        return self.value
    
    def set_value(self, value):
        self.value = float(value)

    def __repr__(self):
        return (str(self.value))


class Variable:
    def __init__(self, data):
        self.name = data['name'] # used as the key in the dictionary
        self.symbol = data['smoothTokens'][0] # used as the symbol in the equation
        self.token = [Token(s,0) for s in data['smoothTokens']]
    
    # this method calculates the value of variable at 'time'
    def compute_next_value(self, time):
        # obtain the polynomial equation
        t = S.sympify('t')
        taylor_approx = S.sympify(self.token[0].value) # first taylor term
        for i in range(1, len(self.token)+1):
            taylor_approx = taylor_approx + self.token[i].value * (t ** i) # add more taylor terms
        
        # compute the polynomial equation given the time value
        next_value = solve(taylor_approx, {S.sympify('t') : time } )
        # update the variable value
        self.token[0].set_value(next_value)
        # reset the derivative values
        for i in range(1, len(self.token)+1):
            self.token[i].set_value(0)

    def set_current_value(self, value):
        self.token[0].set_value(value)

    def current_value(self):
        return self.token[0].get_value()

    def __repr__(self):
        return str(self.token)
    

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
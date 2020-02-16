import sympy as S
import math
from mpmath import mp, findroot
from scipy.optimize import minimize_scalar
import numpy as np

# frequently used variable
t = S.sympify('t')

# Helper function
def substitution(expr, variables):
    for name, v in variables.items():
        # reversed order is very important
        for symbol, value in reversed(v.get_symbol_value_pairs()):
            expr = expr.replace(symbol, value)
    return expr

def filter_positive_real_roots(roots):
    final = []
    for number in S.sympify(roots):
        real_part = number.as_real_imag()[0]
        imag_part = number.as_real_imag()[1]
        if (abs(imag_part) <= 1e-6) and (real_part >= 0): # tolerable imaginary value
            final.append(real_part)
    return final

# This function returns the minimum time of the Lagrange error until it becomes
# larger than the requirement 
def calculate_validity_time (Lagranges, LTE, order, max_step):
    time = max_step # max time
    for eq in Lagranges: 
        res = minimize_scalar(eq, bounds = [0, time], method='bounded')
        if res.fun == 0:
            continue # this will make the Lagrange term zero
        else: 
            e = abs(res.fun)
            # check if the expected error meets the requirement
            expected_error = e * time ** (order + 1) / math.factorial(order + 1)
            if expected_error >= LTE: # when it fails, we compute the time
                time = (LTE * math.factorial(order + 1) / e) ** (1 / (order + 1))
    return time

def calculate_zero_crossing_time(guard_set, Var):

    for guard in guard_set:
        # for now, we ignore compositional guards
        g = guard[0][0] # so this is a single guard expression

        # g contains derivative expressions for this guard. Substitute the variables with numbers.
        derivatives = [ substitution(ele, Var) for ele in g ]

        # Obtain the taylor coefficients of this guard
        taylor_coefficients = []
        for i in range(0, len(derivatives)):
            taylor_coefficients.append( float(derivatives[i] / math.factorial(i) ) )
        taylor_coefficients.reverse() # existing solvers requires the reverse order

        # We check the guard's taylor intersects with one of either: zero or g0 * 2
        # Based on our theorem, there is always at least one positive real root.
        g0 = derivatives[0] # current value of the guard

        # check if the guard meets the zero line
        roots = filter_positive_real_roots( np.roots(taylor_coefficients) )
        if not roots: # only if no root, check g0 * 2
            taylor_coefficients[len(taylor_coefficients)-1] = -g0 
            roots = filter_positive_real_roots( np.roots(taylor_coefficients) )
            if not roots: 
                return 1
            else:
                return min(roots)
        else:
            return min(roots)


import sympy as S
import math
from mpmath import mp, findroot
import numpy as np

# A time variable, which will be declared many times within this file scope
t = S.sympify('t')

# For the input expression (multivariative), caucluate its value by substituting the variable values.
# Input: expr = sympify equation, variables = a dictionary { var_name : var_object }
# Output: expr = sympify equation, which is a number after substitution of the variable values
def calculate_value(expr, variables):
    for name, v in variables.items():
        # reversed order is very important, because we want to substitute the highest rank derivative first
        for symbol, value in reversed(v.smooth_tokens):
            expr = expr.subs(symbol, value)
    return expr

# Returns True if the input number is a positive real number
def check_positive_real(number):
    real_part = number.as_real_imag()[0]
    imag_part = number.as_real_imag()[1]
    if real_part < 0.000001: # consider it as non-positive or zero
        return 0
    elif abs(imag_part) > 1e-6: # untolerable imaginary value
        return 0
    else:
        return real_part

# Find the first positive non-zero root of the input function
# The input function is an univariative function
# Input: sympify equation, guess
# Output: a root in an array, which is empty if no valid value is found
def root_finding(expression, guess=0):
    mp.dps = 6
    for t0 in range(0, 1):
        try:
            ans = S.nsolve(expression, t, guess + t0)
            ans = check_positive_real(ans)
            if ans > 0:
                return [ans]
        except:
            pass
    return []

# Makes Taylor expansion for order 1 and 2
# Input: X = a dictionary of cont. variables, O = a dictionary of output variables
def taylor12_derive(X, O):
    for x in X.values():
        c = x.get_token_values()
        # taylor order 1 is linear
        x.ex1 = c[0] + c[1] * t
        # taylor order 2 is quadratic
        x.ex2 = c[0] + c[1] * t + 0.5 * c[2] * (t**2)
    # do the same for O
    for o in O.values():
        c = o.get_token_values()
        o.ex1 = c[0] + c[1] * t
        o.ex2 = c[0] + c[1] * t + 0.5 * c[2] * (t**2) 

# 1st order RK is Euler
# 2nd order RK, midpoint method
def rk12_derive(loc, I, X, O):
    union = {**I, **X}
    for name, f in loc.get_f().items():
        # rk1 is Euler method
        c = X[name].get_token_values()
        X[name].ex1 = c[0] + c[1] * t
        # rk2 - midpoint method
        for v in union.values():
            d = v.get_token_values() # smooth tokens
            k1 = d[0] + 0.5 * d[1] * t
            f = f.subs(v.get_symbol(), k1)
        X[name].ex2 = c[0] + t * f

    for name, h in loc.get_h().items():
        h2 = h
        for x in X.values():
            h = h.subs(x.get_symbol(), x.ex1)
            h2 = h2.subs(x.get_symbol(), x.ex2)
        O[name].ex1 = h
        O[name].ex2 = h2

# Makes QSS1 and QSS2 equations
# Input: loc, I, X, O
def mqss12_derive(loc, I, X, O):
    union = {**I, **X}
    # do: x(t) = f'(q(t), t) 
    # ex1 <- x(t) based on qss1
    # ex2 <- x(t) based on qss2
    for name, f in loc.get_f().items():
        # qss1 is simply the Euler method
        c = X[name].get_token_values()
        X[name].ex1 = c[0] + c[1] * t
        # for qss2, we need to put q(t) into f(q(t))
        for v in union.values():
            d = v.get_token_values()
            qss2 = d[0] + d[1] * t
            f = f.subs(v.get_symbol(), qss2)
        # f(q(t)) is again, approximated using Euler method
        X[name].ex2 = c[0] + t * f

    # do: o(t) = h(x(t), t)
    # ex1 <- o(t) = h(x(t)), where x(t) is obtained using qss1
    # ex2 <- o(t) = h(x(t)), where x(t) is obtained using qss2
    for name, h in loc.get_h().items():
        h2 = h
        for x in X.values():
            h = h.subs(x.get_symbol(), x.ex1)
            h2 = h2.subs(x.get_symbol(), x.ex2)
        O[name].ex1 = h
        O[name].ex2 = h2

def algorithm3(transition, I, X, vtol):

    def get_remainder(expr, variables):
        for v in variables.values():
            # reversed order otherwise the substitution may not work correctly
            for symbol, value in reversed(v.smooth_tokens):

                expr = expr.subs(symbol, value)
        return expr

    variables = {**I, **X}
    guard = transition.guards[0][0] # ignore the compositional guards for now

    # calculate the taylor coefficients of the guard
    taylor_coef = [ calculate_value(exp, variables) for exp in guard ]
    order = len(taylor_coef) - 1 
    for i in range(1, order + 1 ):
        taylor_coef[i] = float(taylor_coef[i] * 1 / math.factorial(i))
    taylor_coef.reverse() # this contains the Taylor coefficients
    log = ["ZCD; g: %s, taylor coef: %s" % (guard[0], str(taylor_coef) )]

    # To calculate Lagrange remainder,
    # Get the f^(n+1) derivative expression (last element of the list)
    remainder_expr = guard[-1]
    remainder = get_remainder(remainder_expr, variables)
    

    if taylor_coef[0] == 0:
        valid_time = 99 # an arbitrary big value
    else:
        LTE_coef = [ 0 ] * len(taylor_coef)
        LTE_coef[ 0 ] = taylor_coef[ 0 ] # LTE is the last term
        if taylor_coef[0] < 0:
            LTE_coef[ order ] = vtol
        if taylor_coef[0] > 0:
            LTE_coef[ order ] = -vtol
        # we know that the remainder root always exist
        valid_time = min( [ i for i in np.roots(LTE_coef) if i > 0 ])
        
    # find if zero crossing occurs
    zero_crossings = [ check_positive_real( S.sympify(sol) ) for sol in np.roots(taylor_coef) if check_positive_real( S.sympify(sol) ) > 0 ]
    if len(zero_crossings) == 0:
        log.append("no zero-crossings")
        return valid_time, log

    # check if the zero-crossing orrurs within the valid time
    zcd = min(zero_crossings)
    log.append("zcd: %f, valid-time: %f" % ( zcd, valid_time ) )
    if zcd < valid_time:
        return zcd, log
    else:
        return valid_time, log

def algorithm2(transition, I, X, vtol):
    variables = {**I, **X}
    guard = transition.guards[0][0] # ignore the compositional guards for now

    # calculate the taylor coefficients of the guard
    taylor_coef = [ calculate_value(exp, variables) for exp in guard ]
    order = len(taylor_coef) - 1 
    for i in range(1, order + 1 ):
        taylor_coef[i] = float(taylor_coef[i] * 1 / math.factorial(i))
    taylor_coef.reverse() # this contains the Taylor coefficients
    log = ["ZCD; g: %s, taylor coef: %s" % (guard[0], str(taylor_coef) )]

    if taylor_coef[0] == 0:
        valid_time = 99 # an arbitrary big value
    else:
        LTE_coef = [ 0 ] * len(taylor_coef)
        LTE_coef[ 0 ] = taylor_coef[ 0 ] # LTE is the last term
        if taylor_coef[0] < 0:
            LTE_coef[ order ] = vtol
        if taylor_coef[0] > 0:
            LTE_coef[ order ] = -vtol
        # we know that the remainder root always exist
        valid_time = min( [ i for i in np.roots(LTE_coef) if i > 0 ])
        
    # find if zero crossing occurs
    zero_crossings = [ check_positive_real( S.sympify(sol) ) for sol in np.roots(taylor_coef) if check_positive_real( S.sympify(sol) ) > 0 ]
    if len(zero_crossings) == 0:
        log.append("no zero-crossings")
        return valid_time, log

    # check if the zero-crossing orrurs within the valid time
    zcd = min(zero_crossings)
    log.append("zcd: %f, valid-time: %f" % ( zcd, valid_time ) )
    if zcd < valid_time:
        return zcd, log
    else:
        return valid_time, log

def algorithm1(transition, I, X, iteration, ttol):

    def reform_guards (transition, variables):
        guard = transition.guards[0][0][0] # ignore the compositional guards for now, just consider one guard
        eq1 = eq2 = g0 = guard
        # obtain eq1 and eq2, which are two equations obtained using different methods
        for name, v in variables.items():
            eq1 = eq1.subs(v.get_symbol(), v.ex1) # substitute ex1
            eq2 = eq2.subs(v.get_symbol(), v.ex2) # substitute ex2
            g0 = g0.subs(v.get_symbol(), v.get_current_value()) # the current guard value
        return guard, g0, eq1, eq2

    guard, g0, eq1, eq2 = reform_guards (transition, {**I, **X})
    log = ["ZCD; g: %s, g0: %s, eq1: %s, eq2: %s" % (guard, g0, eq1, eq2)]

    # check if either the expressions is constant (i.e., zero gradient)
    if eq1.is_constant() or eq2.is_constant():
        return 0, log
             
    for i in range(0, iteration):
        offset = g0 * (1 - 0.5 ** i)
        # check negative delta-q (decreasing value)
        zcd1 = root_finding(eq1 - offset) # eq1 = offset -> eq1 - offset = 0
        zcd2 = root_finding(eq2 - offset)
        log.append("attempt: %d, (%s, %s)" % (i, str(zcd1), str(zcd2)))
        if len(zcd1) != 0 and len(zcd2) != 0:
            if abs(min(zcd1) - min(zcd2)) <= ttol:
                if i == 0:
                    return min(zcd2), log
                else:
                    return min(zcd2), log
        # check positive delta-q (increasing value) 
        zcd1 = root_finding(eq1 - (2 * g0 - offset))
        zcd2 = root_finding(eq2 - (2 * g0 - offset))
        log.append("attempt: %d, (%s, %s)" % (i, str(zcd1), str(zcd2)))
        if len(zcd1) != 0 and len(zcd2) != 0:
            if abs(min(zcd1) - min(zcd2)) <= ttol:
                if i == 0:
                    return min(zcd2), log
                else:
                    return min(zcd2), log

    # we could not find the delta with the given iteration depth
    return 0, log 


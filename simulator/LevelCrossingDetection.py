import sympy as S
import numpy as np 
from mpmath import mp, findroot
import config

# returns a polynomial equation based on the given tokens
def get_polynomial(tokens):
    rank = len(tokens)
    t = S.sympify('t')
    eq = S.sympify(tokens[0])
    for i in range(1, rank):
        eq = eq + tokens[i] * (t ** i)
    return eq

def qss_polynomial(tokens, order):
    t = S.sympify('t')

    #for i in range(order)
    #p = tokens[0] * tokens[1] * t

# from the input array, remove the negative and complex numbers
def only_positive_real_numbers(array):
    filtered_array = []
    for s in array:
        real_part = s.as_real_imag()[0]
        imag_part = s.as_real_imag()[1]
        if real_part <= 0 or abs(imag_part) > 1e-6:
            continue
        if real_part in filtered_array:
            continue
        else:
            filtered_array.append(real_part)       
    return filtered_array

# Inputs: level-crossing value, and an expression
# Output: returns the solution then the expression is equal to the level-crossing value
def level_crossing_detection(level, expression):
    mp.dps = 30         # 30 decimal places
    t = S.sympify('t')   
    solution = []
    # converts the level crossing detection into zero crossing detection
    constraint = expression - level
    # guess 5 values for the numerical solving
    for t0 in range(0,1):
        try:
            sol = S.nsolve(constraint, t, t0 * config.MAX_STEP_SIZE)
            solution.append(sol)
        except:
            pass
    solution = only_positive_real_numbers(solution)
    return solution

def Taylor_qss(guard, variables):
    # currently, logical guard composition is disabled
    low_qss = guard[0]
    high_qss = guard[0]
    g_value = guard[0]
    for name, v in variables.items():
        high_poly = get_polynomial(v.get_token_values())
        low_poly = get_polynomial(v.get_token_values()[:-1]) # remove the last token
        high_qss = high_qss.subs(S.sympify(name), high_poly)
        low_qss = low_qss.subs(S.sympify(name), low_poly)
        g_value = g_value.subs(S.sympify(name), v.get_token_values()[0])
    
    # --- Debugging purpose ---
    # print ("ZCD; g: %s , val: %f \n h: %s \n l: %s \n" % (guard[0], g_value, high_qss, low_qss) )

    for i in range(0,config.MAX_ITERATION):
        g_next = g_value * (0.5**i)
        level = g_value - g_next
        sol_h = level_crossing_detection(level, high_qss)
        sol_l = level_crossing_detection(level, low_qss)
        # if any qss solution is NULL
        if len(sol_h) == 0 or len(sol_l) == 0:
            continue
        else:
            t_l = min(sol_l)
            t_h = min(sol_h)
            if abs(t_h - t_l) <= config.DELTA_TOLERANCE:
                return t_h

    # if the guard is not reachable 
    return config.MAX_STEP_SIZE

# Use QSS1 and QSS2 to compute the time value
# Input: guard is an array of guard conditions, variables is a dictionary for all variables
def Modified_QSS(guard, variables):
    # currently, logical guard composition is disabled
    low_qss = guard[0]
    high_qss = guard[0]
    g_value = guard[0]
    for name, v in variables.items():

        high_poly = get_polynomial(v.get_token_values())
        low_poly = get_polynomial(v.get_token_values()[:-1]) # remove the last token
        high_qss = high_qss.subs(S.sympify(name), high_poly)
        low_qss = low_qss.subs(S.sympify(name), low_poly)
        g_value = g_value.subs(S.sympify(name), v.get_token_values()[0])
    
    # --- Debugging purpose ---
    # print ("ZCD; g: %s , val: %f \n h: %s \n l: %s \n" % (guard[0], g_value, high_qss, low_qss) )

    for i in range(0,config.MAX_ITERATION):
        g_next = g_value * (0.5**i)
        level = g_value - g_next
        sol_h = level_crossing_detection(level, high_qss)
        sol_l = level_crossing_detection(level, low_qss)
        # if any qss solution is NULL
        if len(sol_h) == 0 or len(sol_l) == 0:
            continue
        else:
            t_l = min(sol_l)
            t_h = min(sol_h)
            if abs(t_h - t_l) <= config.DELTA_TOLERANCE:
                return t_h

    # if the guard is not reachable 
    return config.MAX_STEP_SIZE




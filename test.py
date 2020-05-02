# Defines QSHIOA and Trnasition classes
import sympy as S
import math
from mpmath import mp, findroot
import numpy as np
import sys

# Find the first positive non-zero root of the input function
# The input function is an univariative function
# Input: sympify equation, guess
# Output: a root in an array, which is empty if no valid value is found
def root_finding(expression, guess=0):
    mp.dps = 6
    for t0 in range(0, 3):
        try:
            ans = S.nsolve(expression, t, guess + t0)
            ans = check_positive_real(ans)
            if ans > 0:
                return [ans]
        except:
            pass
    return []

# Returns True if the input number is a positive real number
def check_positive_real(number):
    real_part = number.as_real_imag()[0]
    imag_part = number.as_real_imag()[1]
    if real_part < 1e-6: # consider it as non-positive or zero
        return 0
    elif abs(imag_part) > 1e-6: # untolerable imaginary value
        return 0
    else:
        return real_part

def halving_algorithm(lqss, hqss, g0, ttol, iteration):

    # check if either the expressions is constant (i.e., zero gradient)
    if lqss.is_constant() or hqss.is_constant():
        return 999 # this guard will never produce zero-crossing. Theoretically, the step size can be infinity.

    for i in range(0, iteration):
        # for i = 0, 1, 2 ... , offset = 0, 0.5*g0, 0.75*g0, ... 
        offset = g0 * (1 - 0.5 ** i) 
        # check zero-crossing detection for the guard approaching the zero line
        zcd_h = root_finding(hqss - offset) 
        zcd_l = root_finding(lqss - offset)

        if zcd_h and zcd_l:
            if abs(min(zcd_l) - min(zcd_h)) <= ttol:
                return min(zcd_h) # return the higher order answer

        # this time, check zero-crossing with the line at (2 * g0)
        zcd_h = root_finding(hqss - (2 * g0 - offset))
        zcd_l = root_finding(lqss - (2 * g0 - offset))

        if zcd_h and zcd_l:
            if abs(min(zcd_l) - min(zcd_h)) <= ttol:
                return min(zcd_h) # return the higher order answer

    return None # after all the iterations, if we still cannot find the step size, then return None

t = S.sympify('t')
#hqss = S.sympify(t * (-9.81 * t - 2.26611) + 9.72697789)
#lqss = S.sympify(9.72697789 - 2.26611 * t)
g0 = S.sympify(9.72697789)
hqss = S.sympify("t * (-9.81 * t - 2.26611) + g0")
hqss = hqss.subs("g0", g0)
lqss = S.sympify("g0 - 2.26611 * t")
lqss = lqss.subs("g0", g0)

my_solution = halving_algorithm(lqss, hqss, g0, 0.001, 10)
print(my_solution)
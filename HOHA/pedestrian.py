import sympy as S
import math
from mpmath import mp, findroot
from scipy.optimize import minimize_scalar
import numpy as np

# This is an implementation of HOHA for solving the pedestrian example
time = 0

# Initial locations
carLoc = 0
lidarLoc = 0

# variables
d = 50
x = 0
d_out = d
x_out = x
d_in = 0
x_in = 0

# smooth tokens
p = 2 # up to pth order derivatives
x_st = [0] * p
d_st = [0] * p
x_out_st = [0] * p
d_out_st = [0] * p
x_in_st = [0] * p
d_in_st = [0] * p

# Higher order ODEs
Car_ODE = {
    0 : {
        'x' : [
            lambda d, dst : 0.2 * d,
            lambda d, dst : 0.2 * dst[0], 
            lambda d, dst : 0.2 * dst[1],
            lambda d, dst : 0.2 * dst[2],
            lambda d, dst : 0.2 * dst[3],
        ],
    },
    1 : {
        'x' : [
            lambda d, dst : 0, 
            lambda d, dst : 0, 
            lambda d, dst : 0, 
            lambda d, dst : 0, 
            lambda d, dst : 0, 
        ],
    },
}

Lidar_ODE = {
    0 : {
        'd' : [
            lambda x, xst : 0.2 * x - 8,
            lambda x, xst : 0.2 * xst[0],
            lambda x, xst : 0.2 * xst[1],
            lambda x, xst : 0.2 * xst[2],
            lambda x, xst : 0.2 * xst[3],
        ]
    }
}

# Lagrange error functions
# when p = 2, 3rd order term is the Lagrange error
Car_Lagrange = {
    0 : [
        lambda t : 0, # 
        lambda t : 0,
    ],
    1 : [
        lambda t : 0,
    ],
}
Lidar_Lagrange = {
    0 : [
        lambda t : 0,
    ],
}
# when p = 3

Car_Egress = {
    0 : [

    ],
    1 : [],
}


def manage_tokens():
    for i in range(0, p):
        x_st[i] = Car_ODE[carLoc]['x'][i](d_in, d_in_st)
        x_out_st[i] = x_st[i]
        d_st[i] = Lidar_ODE[lidarLoc]['d'][i](x_in, x_in_st)
        d_out_st[i] = d_st[i]
        d_in_st[i] = d_out_st[i]
        x_in_st[i] = x_out_st[i]

# This function returns the minimum time of the Lagrange error until it becomes
# larger than the requirement 
def solve_lagrange (Lagranges, LTE, order, max_step):
    time = max_step # max time
    for eq in Lagranges: 
        res = minimize_scalar(eq, bounds = [0, time], method='bounded')
        if res.fun == 0:
            continue 
        else: 
            e = abs(res.fun)
            # check if the expected error meets the requirement
            expected_error = e * time ** (order + 1) / math.factorial(order + 1)
            if expected_error >= LTE: # when it fails, we compute the time
                time = (LTE * math.factorial(order + 1) / e) ** (1 / (order + 1))
    return time

result = [ ('time', 'x', 'd') ]
while time <= 10:
    # record system output
    output = (time, x, d)
    result.append(output)

    # exchange IO
    d_in = d_out 
    x_in = x_out

    # check inter-location
    block = False
    if carLoc == 0:
        # check transition
        if pow(d_in * d_in - 900, 0.5) <= 1 :
            # execute inter
            carLoc = 1
            block = True

    if block == True:
        continue

    # exchange token
    manage_tokens() 

    # delta calculation
    delta = []
    Lagrange_equations = Car_Lagrange[carLoc] + Lidar_Lagrange[lidarLoc]
    for eq in Lagrange_equations:
        delta.append( solve_lagrange(eq) )
    
    egress_guards = Car_Egress[carLoc]
    for eq in egress_guards:
        delta.append( solve_zero_crossing(eq) )
    
    step = min(delta)

    # intra-location


    time = time + step

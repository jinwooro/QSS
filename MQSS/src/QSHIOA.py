# Defines QSHIOA and Trnasition classes
from Location import Location
from Transition import Transition
import sympy as S
import math
from mpmath import mp, findroot
import numpy as np
import sys

t = S.sympify('t')

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
    t = S.sympify('t')
    lqss = S.sympify(lqss)
    hqss = S.sympify(hqss)
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

class QSHIOA:
    def __init__(self, data):
        # name
        self.instance_name = data['name']
        # locations
        self.locations = { loc['id'] : Location( loc ) for loc in data['locations']}
        # register exiting transitions to each location object
        for t in data['transitions']:
            tid = t['sid']
            transition = Transition(t)
            self.locations[tid].add_outgoing_transition(transition)

        # current location setup
        loc_id = data['initialLocation'] # initial location id
        self.current_location = self.locations[loc_id]

        # variable objects
        self.I = { i['name'] : float(i['initialValue']) for i in data['I']}
        self.O = { o['name'] : float(o['initialValue']) for o in data['O']}
        self.X = { x['name'] : float(x['initialValue']) for x in data['X_C']}

        # Simulink has multiple options for initialization
        # 1. reset on the first transition
        for fx in data['initialization']:
            if fx['LHS'] in self.X:
                self.X[fx['LHS']] = fx['RHS']
            elif fx['LHS'] in self.O:
                self.O[fx['LHS']] = fx['RHS']
        # 2. initial location entry actions
        initLocID = data['initialLocation'] 
        self.locations[initLocID].en(self.I, self.O, self.X) # evaluate "en" of a location
        self.update_O()

    def update_O(self):
        self.current_location.h(self.O, self.X)

    def inter_location_transition(self, vtol):
        flag = False
        while(True):
            trans = self.current_location.get_enabled_transition(self.I, self.X, vtol)
            # trans == None if there is no enabled transition
            if trans == None:
                break
            else:
                self.current_location = self.locations[ trans.destination ] # update the current location
                trans.R(self.I, self.O, self.X) # reset relation
                self.current_location.en(self.X, self.I, self.O) # entry action of the new location
                flag = True
                print("QSHIOA:%s %s inter-location transition" % (self.instance_name, self.current_location.name))     
        if flag:
            self.update_O()
            return True # return true means that at least one inter-location has occurred
        else:
            return False # return false means that no inter-location has triggered

    def intra_location_transition(self, time):
        self.current_location.update_continuous_variables(self.X, time)
        self.update_O()

    def compute_delta(self, iteration, ttol):
        egress_transitions = self.current_location.Transitions
        Variables = [ (S.sympify(name), value) for name, value in {**self.X, **self.I}.items() ] # convert dictionary to list of tuples
        # hqss (higher order QSS) and lqss (lower order QSS) in function of time
        hqss_t, lqss_t = self.current_location.get_QSS(Variables) 
        deltas = [] # a collection of delta values

        for t in egress_transitions:
            temp = []
            for guard, relation in t.guards: # for each guard expression, we apply MQSS algorithm
                g0 = guard.subs(Variables) # g0 is the current value of the guard

                # For a time-variant variable x, we express it using higher order QSS, denoted as x_hq(t)
                # Thus, the original guard is : g(x(t))
                # Instead of x(t), we can substitute it with x_hq(t) : g(x_hq(t))
                g_hqss = guard.subs(hqss_t) # get the guard expression by substituting x_hq(t)
                g_hqss = g_hqss.subs(Variables) # substitute the remaining variable values

                # Like the higher order QSS, lower order QSS variables x_lq(t) are substituted into the guard expression
                g_lqss = guard.subs(lqss_t)
                g_lqss = g_lqss.subs(Variables)

                time = halving_algorithm(g_lqss, g_hqss, g0, ttol, iteration)
                # print( "g: %s, g0: %s, g_lqss: %s, g_hqss: %s, sol: %s" % (str(guard), g0, str(g_lqss), str(g_hqss), time) )
                if time == None:
                    time = 0.01 # this is temporary solution for problematic situation, when the gradient is near zero, we cannot find MQSS solution
                    """
                    print("Cannot continue the simulation. There is a transition guard, where we cannot decide the time step size.")
                    print("  Problematic situation:")
                    print("  g: %s, g0: %s, g_lqss: %s, g_hqss: %s" % (str(guard), g0, str(g_lqss), str(g_hqss)) )
                    sys.exit()
                    """
                temp.append(time)
            # min(temp) is the step size solution for a single transition
            deltas.append( min(temp) )

        return deltas

    # returns a collection of the variable names
    def get_variable_names(self):
        names = [ name for name in {**self.X, **self.O} ]
        return names

    # returns a collection of the current value of variables
    def get_current_state(self):
        loc_name = self.current_location.get_name()
        state = [ var for var in {**self.X, **self.O}.values() ]
        state.insert(0, loc_name) # add the current location name 
        return state
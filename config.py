
xtol = 0.000001

# continuous variable approximation methods
mqss12 = {
            'approx' : 'mqss12', 
            'derivative-order' : 1,
        }

taylor12 = {
            'approx' : 'taylor12', 
            'derivative-order' : 2,
        }  

rk12 = {
            'approx' : 'rk12', 
            'derivative-order' : 1,
    }

taylor3 = {
            'approx' : 'taylor3',
            'derivative-order' : 3,
        }

# algorithms for solving
algorithm1 = {
            'name' : 'algorithm1', # temp name
            'iteration' : 15,
            'ttol' : 0.001,
        }

algorithm2 = {
            'name' : 'algorithm2',
        }

algorithm3 = {
            'name' : 'algorithm3',
        }

approx = taylor12
algo = algorithm2
# algorithm1 : mqss12, taylor12, rk12
# algorithm2 : none

MAX_TIME = 40
# This is the maximum simulation time

MAX_STEP = 1
# Setting max step allows to produce extra data points for observation
# If the max step size is very large, the system state directly jumps to the guard condition

VTOL = 0.001

FORCE_FORWARD = True
ESCAPE_STEP = 0.01
# each QSHIOA from its current state, delta value should be produced.

# DELTA_SENSITIVITY = 0.01
# if the calculated delta is smaller than this value, we ignore it.

DEBUG = False
# if True, then print the the guard conditions and the variable interpretors
# that resulted in no delta solution. Basically, if no delta value is found at a location
# for all transition guards, we print this problematic situation.






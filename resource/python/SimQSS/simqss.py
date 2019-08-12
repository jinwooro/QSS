#!/usr/bin/env python3

import sympy as S
import numpy as np
import sys
import json
import csv

# assign tokens 'b' -> 'a' at the position 'index'
def assignment (a, b, index=0):
    # a, b are in the format: [Token1, Token2, Token3, ... ]
    a.token[index].value = b.token[index].value
    return a

def exchange_qss(a, b):
    a.qss_high = b.qss_high
    a.qss_low = b.qss_low

# returns a polynomial equation based on the given tokens
def get_polynomials(tokens, rank):
    t = S.sympify('t')
    eq = S.sympify(tokens[0].value)
    for i in range(1, rank+1):
        # this is not taylor series but similar
        eq = eq + tokens[i].value * (t ** i)
    return eq

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
        # qss related values (equations)
        self.qss_low = 0
        self.qss_high = 0
    
    # this function generates qss_interpretation for qss low and high
    def gen_qss(self, qss_rank):
        self.qss_low = get_polynomials(self.token, qss_rank-1)
        self.qss_high = get_polynomials(self.token, qss_rank)

    def compute_next_value(self, time):
        self.token[0].set_value(solve(self.qss_high, {S.sympify('t') : time}))
        # since the value is changed, derivatives are expired hence reset to zero
        for i in range(1, len(self.token)):
            self.token[i].set_value(0)

    def set_current_value(self, value):
        self.token[0].set_value(value)

    def current_value(self):
        return self.token[0].get_value()

    def __repr__(self):
        return str(self.token)
    
class EQN:
    def __init__(self, fx, type='NORMAL'):
        if type == 'NORMAL':
            self.subject = fx['LHS']
        elif type == 'ODE':
            self.subject = fx['LHS'].split('_dot')[0]
        # store the derivative expressions
        self.derivatives = [S.sympify(der) for der in fx['derivatives']]

    # comptue and return the token at the position 'index' 
    def get_token(self, variables, index):
        to_solve = self.derivatives[index]
        # substitute all the variables with its value
        for key, var in variables.items():
            # reversed order is very important during the substitution
            for token in reversed(var.token):
                to_solve = to_solve.subs(S.sympify(token.symbol), token.value)
        return to_solve
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

class Transition:
    def __init__(self, data):
        self.current_location = data['sid']
        self.next_location = data['did']
        self.resets = [(r['LHS'], S.sympify(r['RHS'])) for r in data['resets']]
        self.guards = [S.sympify(g['rearranged']) for g in data['guards']]

    # returns true if this transition is enabled (vtol is the tolerance)
    def guards_enabled(self, I, X, vtol=0.0001):
        # check each guard if it is true
        subs_dict = { key : var.current_value() for key, var in {**I, **X}.items()}
        for g in self.guards:
            print("guard checking: " + str(g) + " with subs " + str(subs_dict))
            ans = solve(g, subs_dict)
            print(ans)
            if abs(ans) <= vtol: # the first detection of any enabled guard,
                return True
        return False

    def compute_delta(self, variables, ttol = 0.001, iteration = 100):
        delta = []
        qx_low = { key : var.qss_low for key, var in variables.items()}
        qx_high = { key : var.qss_high for key, var in variables.items()}
        current_values = { key : var.current_value() for key, var in variables.items() }
        for g in self.guards:
            # current value of the guard condition
            dq = solve(g, current_values)
            # start the iteration
            count = 0
            while (count < iteration):
                # Modify the guard in each iteration
                if count == 0:
                    guard = g
                    print("guard : " + str(guard))
                else:
                    guard = S.sympify(g - dq / (2 ** count))
                    print("guard : " + str(guard))
                # solve the guard
                expr1 = solve(guard, qx_low)
                expr2 = solve(guard, qx_high)
                print(expr1)
                print(expr2)
                t1 = poly_solver(expr1) # numerical solve
                t2 = poly_solver(expr2) # numerical solve
                #t = S.var('t', real=True)
                #t1 = [S.N(i) for i in S.solve(expr1, t)] # symblolic sol
                #t2 = [S.N(i) for i in S.solve(expr2, t)] # symblolic sol

                # filter values that are less than zero
                t1 = [i for i in t1 if i >= 0]
                t2 = [i for i in t2 if i >= 0]
                print("count : " + str(count) + ' / Roots: ' + str(t1) + ", " + str(t2))

                # if either t1 or t2 is empty, it means no real root is found
                # this means that the guard is not reachable
                if len(t1) == 0 or len(t2) == 0:
                    # in this case, time progress by a predefiend fixed value
                    return 0.1
                # if two time values are close enough, this is the delta
                if (abs(min(t1) - min(t2)) <= ttol):
                    return min(t2)
                else:
                    return min(t2)
                    count += 1
                    if count == iteration:
                        print("Error: cannot solve the guard")
                        exit()
        return 0 # this return statement is never reached

    # this function : O, X -> O (directly changed)
    def resets_assign(self, O, X):
        X_pre = X
        for r in self.resets:
            name = r[0]
            equation = r[1]
            if equation.is_constant():
                if name in X : X[name].set_current_value(equation)
                if name in O : O[name].set_current_value(equation)
            else:
                for key, var in X_pre.items():
                    equation = equation.subs(key, var.current_value())
                if name in X : X[name].set_current_value(equation)
                if name in O : O[name].set_current_value(equation)

class QSHIOA:
    def __init__(self, qshioa_data):
        self.name = qshioa_data['name']
        # save the original qshioa information (needed for the initialization)
        self.qshioa_data = qshioa_data

        # A dictionary of equations with the key = location id
        self.ODEs = { loc['id'] : [EQN(fx, 'ODE') for fx in loc['ODEs']] for loc in qshioa_data['locations']}
        self.updates = { loc['id'] : [EQN(hx, 'NORMAL') for hx in loc['outputUpdates']] for loc in qshioa_data['locations']}
        # A dictionary of locations and transitions from each location
        self.transitions = { loc['id'] : [] for loc in qshioa_data['locations']}
        for t in qshioa_data['transitions']:
            self.transitions[t['sid']].append(Transition(t))

        # initial location id
        self.loc = qshioa_data['initialLocation']['id']

        # variable objects
        self.I = { i['name'] : Variable(i) for i in qshioa_data['I']}
        self.O = { o['name'] : Variable(o) for o in qshioa_data['O']}
        self.X = { x['name'] : Variable(x) for x in qshioa_data['X_C']}

    # initialization as a relation (assignment)
    def initialization(self):
        # there are three ways to set initial values in Simulink model
        # 1. initial value setup/configuration
        for v in self.qshioa_data['I']:
            self.I[v['name']].set_current_value(v['initialValue'])
        for v in self.qshioa_data['O']:
            self.O[v['name']].set_current_value(v['initialValue'])
        for v in self.qshioa_data['X_C']:
            self.X[v['name']].set_current_value(v['initialValue'])
        # 2. the first transition assignment
        for fx in self.qshioa_data['initialization']:
            if fx['LHS'] in self.X:
                self.X[fx['LHS']].set_current_value(fx['RHS'])
            elif fx['LHS'] in self.O:
                self.O[fx['LHS']].set_current_value(fx['RHS'])
        # 3. the entry action in the initial location
        for en in self.qshioa_data['initialLocation']['entries']:
            if en['LHS'] in self.X:
                self.X[en['LHS']].set_current_value(fx['RHS'])
            elif en['LHS'] in self.O:
                self.O[en['LHS']].set_current_value(fx['RHS'])

    # update the output variable token at 'index' by default 0
    def output_update(self, index = 0):
        for h in self.updates[self.loc]:
            self.O[h.subject].token[index].set_value(h.get_token(self.X, index))

    # execute ODEs at the current location
    def internal_value_update(self, index = 0):
        combined = {**self.I, **self.X}
        for f in self.ODEs[self.loc]:
            self.X[f.subject].token[index].set_value(f.get_token(combined, index))
            
    def reset_derivative_tokens(self):
        for var in {**self.I, **self.O, **self.X}.values():
            var.reset_derivatives()

    def inter_location_transition(self):
        # iterate over the out-going transitions at the current location
        for t in self.transitions[self.loc]:
            if t.guards_enabled(self.I, self.X) == True:
                t.resets_assign(self.O, self.X) # X and O can be modified by the reset relations
                self.loc = t.next_location
                self.output_update()
                return True
        return False

    def generate_qss(self, qss_rank):
        # compute the qss interpretation for each variable
        for var in {**self.I, **self.O, **self.X}.values():
            var.gen_qss(qss_rank)

        # This part needs to be considered later
        """
        # for each local variable we obtain the algebraic expression (function of t)
        for f in self.ODEs[self.loc]:
            initial_value = self.X[f.subject].current_value()
            high = f.get_qss({**self.I, **self.X}, initial_value, mode = 'HIGH')
            low = f.get_qss({**self.I, **self.X}, initial_value, mode = 'LOW')
            self.X[f.subject].set_qss_low(low)
            self.X[f.subject].set_qss_high(high)

        # for each output variable we obtain the algebraic exrpession as a function of t
        for h in self.updates[self.loc]:
            initial_value = self.O[h.subject].current_value()
            high = h.get_qss(self.X, initial_value, mode = 'HIGH')
            low = h.get_qss(self.X, initial_value, mode = 'LOW')
            self.O[h.subject].set_qss_low(low)
            self.O[h.subject].set_qss_high(high)
        """

    def get_delta(self):
        delta = []
        for t in self.transitions[self.loc]:
            delta.append(t.compute_delta({**self.I, **self.X}))
        if len(delta) == 0:
            return 1 # default minimum value if no guard condition is reachable 
        return min(delta)

    def intra_location_transition(self, time):
        for var in self.X.values():
            var.compute_next_value(time)

    def check(self):
        variables = {**self.I, **self.O, **self.X}
        print(self.name + "----------------------------")
        print("Location: " + str(self.loc))
        for key, value in variables.items():
            print( key + " : " + str(value.qss_low) + " , " + str(value.qss_high) + " : " + str(value.current_value()))
        print("--------------------------------------")


if __name__ == "__main__":
    # read the input qshioa file
    input_file = sys.argv[1]
    # simulation time
    simulation_finish_time = 50

    with open(input_file) as json_file:
        data = json.load(json_file)

    # extract the system level parameters
    qss_rank = data['qss_rank']
    sysName = data['systemName']

    # instantiate the QSHIOA objects
    QSHIOAs = { q['name'] : QSHIOA(q) for q in data['QSHIOAs']}
    Lines = data['Lines']

    output_labels = ['time'] # more will be added in the next for loop

    # simulation initialization
    time = 0
    for q in QSHIOAs.values():
        q.initialization()
        q.output_update()

    # record the results
    temp = ['time']
    for q in QSHIOAs.values():
        temp.append(q.name)
        temp += [ o.name for o in q.O.values() ]

    result_table = [temp]

    # simulation process
    while(time < simulation_finish_time):
        print ('simulation time: ' + str(time))
        # store the result
        temp = [time]
        for q in QSHIOAs.values():
            temp += [''] + [ o.current_value() for o in q.O.values()]
        result_table.append(temp)

        # I/O exchange (no smooth token exchange, just the current values)
        for line in Lines:
            src = QSHIOAs[ line['srcBlockName'] ]
            dst = QSHIOAs[ line['dstBlockName'] ]
            assignment(dst.I[line['dstVarName']], src.O[line['srcVarName']])

        # inter-location transition checking
        inter = False
        for q in QSHIOAs.values():
            # if any QSHIOA instance executes an inter-location transition, then it sets the inter flag to "True"
            if q.inter_location_transition() == True : inter = True
        # if any inter-location transition is executed, the time is not progressed
        if inter == True:
            print("inter-location triggered")
            continue # continue the next simulation iteration

        # if no inter-location transition is triggered, perform the intra-location transition
        # firstly, smooth token exchange
        for index in range(1, qss_rank+1):
            # compute 'i'th token in each QSHIOA
            for q in QSHIOAs.values():
                q.internal_value_update(index)
                q.output_update(index)
            # share the 'i'th token
            for line in Lines:
                src = QSHIOAs[ line['srcBlockName'] ]
                dst = QSHIOAs[ line['dstBlockName'] ]
                assignment(dst.I[line['dstVarName']], src.O[line['srcVarName']], index)

        # generate qss and compute delta
        delta = []
        for q in QSHIOAs.values():
            q.generate_qss(qss_rank)
            q.check() # for debugging
            delta.append(q.get_delta())
        final_delta = min(delta)

        print ("Delta = " + str(final_delta))

        # intra-location transition execution
        for q in QSHIOAs.values():
            q.intra_location_transition(final_delta)
            q.output_update()

        # get the minimum delta
        time += final_delta

    # writing the results
    with open('result.csv', mode='w') as result_file:
        result_writer = csv.writer(result_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        for row in result_table:
            result_writer.writerow(row)
    
    result_file.close()

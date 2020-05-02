#!/usr/bin/env python3

import sys
import sympy as S
from sympy import Pow
import json
import pprint
import math

# convert some Matlab function notation into sympy notation
def conversionSympyMath(MatlabExpr):
    # the first keyword to convert is 'power' to 'Pow'
    StringExpr = str(MatlabExpr)
    StringExpr = StringExpr.replace('power', 'Pow')
    StringExpr = StringExpr.replace('//', '/') # some mistakes from the Simulink to json parser
    return StringExpr

def writeJson(file_name, data):
    # modify the filename extension to .qshioa
    new_name = file_name.split('.')[0] + ".json"
    with open(new_name, 'w') as outfile:
        json.dump(data, outfile, indent=4, sort_keys=True)

def differentiate(equation, variables, order):
    ode = S.sympify(equation)
    ode = ode.subs(variables)

    derivatives = [ode]
    # differentiate until n-th order
    for i in range(1, order+1):
        der = derivatives[i-1].diff() # differentiate the previous term
        derivatives.append(der)

    # cast every element as a string
    derivatives = [str(ft) for ft in derivatives]
    return derivatives

def reformODEs(ODEs, order):
    # stores each ode as a pair, e.g., ( \dot{x}, f(x) )
    reformed_odes = [] 

    for i in range(1, order+1):
        for data in ODEs:
            subject = S.sympify(data['subject'])
            for j in range(0, i): 
                subject = subject.diff()
            target_ode = S.sympify(data['derivatives'][i])
            # replace the higher order derivatives
            new_ode = target_ode.subs(reformed_odes)
            reformed_odes.append( (subject, new_ode) )
    return reformed_odes

def getLambdaLagrange(equation, symbols, order):
    t = S.sympify('t')
    # Sympify does not work well with the python list notation. So, we work on the equation as a string
    lag = str(equation)
    for name, sym_list in symbols.items():
        counter = order
        for sym in sym_list:
            if sym == 0 : return "lambda t : 999999999999"
            elif S.sympify(sym).is_constant() : return "lambda t : " + str(1/sym)
            # create polynomial approximation
            py_sym = "Var['" + name + "'][" + str(counter) + "]"
            for i in range(0, order-counter):
                multiplier = t ** (i + 1) / math.factorial(i + 1)
                py_sym = py_sym + "+ Var['" + name + "'][" + str( counter + i + 1 ) + "] * " + str(multiplier)
                
            py_sym = "(" + py_sym + ")"
            lag = lag.replace(str(sym), py_sym)
            counter = counter - 1

    # new_Lag = "lambda t : divide(1, abs(" + str(lag) + ") )"
    new_Lag = "lambda t : -abs(" + str(lag) + ") "
    return new_Lag

if __name__ == "__main__":
    # get the file name and the desired order from the input arguments
    file_name = sys.argv[1]
    order = int(sys.argv[2])

    if order < 1:
        print("We only allow HOHA order to be >= 1")
        exit()

    # read the json file
    with open(file_name) as json_file:
        tree_object = json.load(json_file)

    # Load all the HIOAs for the conversion process to HOHA
    HOHAs = tree_object.pop('HIOAs')

    # We will store the Lagrange terms
    Lagrange_loc = {}
    Lagrange_guard = {}

    for h in HOHAs:
        L = h['locations'] # set of all locations
        smooth_tokens = {}
        reformed_ODEs_loc = {} # this will list ODEs in each location
        name_symbol_pairs = []

        # Here, we create smooth tokens for the input/output/internal variables
        for var in (h['I'] + h['O'] + h['X_C']):
            name = var['name']
            symbol = name + '(t)'
            pair = ( name, S.sympify(symbol) )
            name_symbol_pairs.append(pair)
            # differentiate the variable to obtain smooth token symbols
            derivatives = differentiate(name, [pair], order)
            var['smoothTokens'] = derivatives
            # store the smooth tokens in the decending order for the later computation
            smooth_tokens[name] = [ s for s in reversed(derivatives)]

        # Here, we obtain the higher order ODEs and their error terms in each location
        for l in L:
            # we will store the error terms
            Lagrange_terms = []
            # we further differentiate to obtain higher order ODEs
            for f in l['ODEs']:
                # manage LHS (of ODE)
                alias = f['LHS'].split("_dot")[0]
                subject = S.sympify(alias + "(t)")
                f['subject'] = str(subject)
                # manage RHS
                equation = conversionSympyMath(f['RHS'])
                derivatives = differentiate(equation, name_symbol_pairs, order)
                # zero-th order term is added in the derivative list
                derivatives.insert(0, str(subject) )
                # pop the last derivative, which will be used for the Lagrange error calculation
                last_term = derivatives.pop() 
                Lagrange_terms.append(last_term)
                f['derivatives'] = derivatives

            # Here, we make each ODE only based on the ground variables (no derivative)            
            grounded_ODEs = reformODEs(l['ODEs'], order)
            reformed_ODEs_loc[l['id']] = grounded_ODEs # we will need this when managing the gaurds
            # Now, each error term is tranformed into a lambda function
            lambda_functions = []
            for lag in Lagrange_terms:
                lag = S.sympify(lag)
                lag = lag.subs(grounded_ODEs) # substitution
                lag_func = getLambdaLagrange(lag, smooth_tokens, order)
                lambda_functions.append(lag_func)
            # store the lambda functions
            Lagrange_loc[(h['name'], l['id'])] = lambda_functions

            # Here, we differentiate the outputUpdate funcdtions
            for f in l['outputUpdates']:
                equation = conversionSympyMath(f['RHS'])
                ## collect all the variable names 
                time_variants = [var['name'] for var in h['X_C']]
                ders = differentiate(equation, name_symbol_pairs, order)
                f['derivatives'] = ders

        # Here, we manage each transition
        for t in h['transitions']:
            for l in L:
                if t['src'] == l['name']:
                    sid = l['id']
                    t['sid'] = sid
                if t['dst'] == l['name'] : t['did'] = l['id']

            # Now, we rearrange the guard expression. The right-hand side is zero.
            # TODO: conjunction guards
            for g in t['guards']:
                # Left-hand side
                lexpr = S.sympify(conversionSympyMath(g.pop('LHS')))
                # Right-hand side
                rexpr = S.sympify(conversionSympyMath(g.pop('RHS')))
                # move the right-hand side to the left-hand side, so that the right-hand side becomes zero.
                combined = lexpr - rexpr

                # To get the error term, differentiate the guard
                derivatives = differentiate(combined, name_symbol_pairs, order)
                # the last term is the error term
                last_term = derivatives.pop()
                last_term = S.sympify(last_term)
                # load the grounded ODEs and substitue them into the lagrange term
                grounded_ODEs = reformed_ODEs_loc[sid]
                last_term = last_term.subs(grounded_ODEs)
                new_lagr = getLambdaLagrange(last_term, smooth_tokens, order)
                g['rearranged'] = derivatives

            if (h['name'], t['sid']) in Lagrange_guard:
                Lagrange_guard[(h['name'], t['sid'])].append(new_lagr)
            else:
                Lagrange_guard[(h['name'], t['sid'])] = [new_lagr]

    # write json file of this network of QSHIOAs
    tree_object['HOHAs'] = HOHAs
    writeJson(file_name, tree_object)

    """
    pp = pprint.PrettyPrinter(indent = 4)
    pp.pprint(Lagrange_loc)
    pp.pprint(Lagrange_guard)
    """

    # Merge Lagrange_loc and Lagrange_guard
    # note that they were separated for easy debugging (so, can be optmized later if needed)
    for key, value in Lagrange_guard.items():
        if key in Lagrange_loc:
            Lagrange_loc[key] = Lagrange_loc[key] + value
        else:
            Lagrange_loc[key] = value
    Lagranges = Lagrange_loc

    # --- Now, create a python file --- 
    pfile = open('generated/PrecomputedLagrange.py', 'w')
    pfile.write('from math import *\r\n')
    pfile.write('import sympy as S\r\n\r\n')
    pfile.write('def divide(a,b):\r\n')
    pfile.write('    if S.sympify(b) == 0:\r\n')
    pfile.write('        return 999999\r\n')
    pfile.write('    else:\r\n')
    pfile.write('        return a/b\r\n\r\n')

    for key, val in Lagranges.items():
        name, id = key
        StringBuilder = ['def ' + name + '_loc_' + str(id) + '(Var):']
        StringBuilder.append('    lagset = []')
        for eq in val:
            StringBuilder.append('    lagset.append(' + str(eq) +')')
        StringBuilder.append('    return lagset' )
        StringBuilder.append('\r\n')
        string = '\r\n'.join([s for s in StringBuilder])
        pfile.write(string)

    StringBuilder = ['lagrange_loc = {']
    for key in Lagranges.keys():
        name, id = key
        StringBuilder.append( str(key) + ' : ' + name + '_loc_' + str(id) + ',' )
    StringBuilder.append('}\r\n')
    string = ' '.join([s for s in StringBuilder])
    pfile.write(string)


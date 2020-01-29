#!/usr/bin/env python3

import sys
import sympy as S
from sympy import Pow
import json
import pprint
import math

def readJson(file_name):
    with open(file_name) as json_file:
        data = json.load(json_file)
    return data

# convert some Matlab function notation into sympy notation
def conversionSympyMath(MatlabExpr):
    # the first keyword to convert is 'power' to 'Pow'
    SymExpr = MatlabExpr.replace('power', 'Pow')
    SymExpr = SymExpr.replace('//', '/') # some mistakes from the Simulink to json parser
    return SymExpr

def writeJson(file_name, data):
    # modify the filename extension to .qshioa
    new_name = file_name.split('.')[0] + "-precomputed.json"
    with open(new_name, 'w') as outfile:
        json.dump(data, outfile, indent=4, sort_keys=True)

def differentiate(equation, time_variants, rank):
    ode = S.sympify(equation)
    # replace the variable names in the equation with respective symbols 
    for x in time_variants:
        ode = ode.subs(x, S.sympify(x + '(t)'))

    derivatives = [ode]
    for i in range(0, rank):
        der = derivatives[i].diff()
        derivatives.append(der)

    # cast every element as a string
    derivatives = [str(ft) for ft in derivatives]
    return derivatives

def getLambdaLagrange(equation, symbols, rank):
    # Sympify does not work well with the python list notation. So, we work on the equation as a string
    lag = str(equation)
    for name, sym_list in symbols.items():
        counter = rank
        for sym in sym_list:
            # create polynomial approximation
            py_sym = "Var['" + name + "'][" + str(counter) + "]"
            for i in range(0, rank-counter):
                t = S.sympify('t')
                multiplier = t ** (i + 1) / math.factorial(i + 1)
                py_sym = py_sym + "+ Var['" + name + "'][" + str( counter + i + 1 ) + "] * " + str(multiplier)

            py_sym = "(" + py_sym + ")"
            lag = lag.replace(str(sym), py_sym)
            counter = counter - 1

    new_Lag = "lambda t : -abs(" + str(lag) + ")"
    return new_Lag

if __name__ == "__main__":
    if len(sys.argv) == 1:
        print("Need arguments")
        exit()
    elif len(sys.argv) == 2:
        print("By default, the derivative order (i.e., rank) is set to 1")

    # get the file name and the desired rank from the input arguments
    file_name = sys.argv[1]
    rank = int(sys.argv[2])

    if rank < 1:
        print("the rank must be >= 1")
        exit()

    # read the json file and create a tree object
    tree_object = readJson(file_name)
    tree_object['qss_rank'] = rank

    # We will store the Lagrange terms
    Lagrange_loc = {}
    Lagrange_guard = {}

    # Copy all HIOA for the conversion process
    QSHIOAs = tree_object.pop('HIOAs')

    for h in QSHIOAs:
        locations = h['locations']

        # Later, we will need to transform the symbols in sympy into python list calls
        der_symbols = {}

        # set token names for input/output/internal variables
        for var in (h['I'] + h['O'] + h['X_C']):
            var_name = var['name']
            smooth_tokens = differentiate(var_name, [var_name], rank)
            var['smoothTokens'] = smooth_tokens
            # Also save the token symbols in a list in reversed order (required)
            clone = [ s for s in reversed(smooth_tokens)]
            der_symbols[var_name] = clone

        for l in locations:
            # this will store lagrange error equations (expressed in simpify)
            lagr_set = []

            # we will do differentiation
            for f in l['ODEs']:
                equation = conversionSympyMath(f['RHS'])
                ## collect all the variable names for differentiation
                time_variants = [var['name'] for var in (h['X_C'] + h['I'])]
                derivatives = differentiate(equation, time_variants, rank)

                # pop the last derivative, because it will be used to compute Lagrange error
                last_der = derivatives.pop()
                lagr = getLambdaLagrange(last_der, der_symbols, rank)
                lagr_set.append(lagr)

                ## subject variable representing the derivative
                subject = f['LHS'].split("_dot")[0] + "(t)"
                #print(" Original symbol : " + str(f['LHS']) + ", converted to : " + str(subject) )
                derivatives.insert(0, subject)
                f['derivatives'] = derivatives # add to the json data

            Lagrange_loc[(h['name'], l['id'])] = lagr_set

            # outputUpdate funcdtions are also differentiated
            for f in l['outputUpdates']:
                equation = conversionSympyMath(f['RHS'])
                ## collect all the variable names 
                time_variants = [var['name'] for var in h['X_C']]
                ders = differentiate(equation, time_variants, rank)
                f['derivatives'] = ders

        # for each transition, declare attach the id numbers of the locations
        for t in h['transitions']:
            for l in locations:
                if t['src'] == l['name'] : t['sid'] = l['id']
                if t['dst'] == l['name'] : t['did'] = l['id']

            # also, rearrange the guard expression, so the right-hand side is zero
            # later, conjunction guards can be handled
            for g in t['guards']:
                ## collect all the variable names for differentiation
                time_variants = [var['name'] for var in (h['X_C'] + h['I'])]
                lexpr = S.sympify(conversionSympyMath(g.pop('LHS')))
                rexpr = S.sympify(conversionSympyMath(g.pop('RHS')))
                combined = lexpr - rexpr
                derivatives = differentiate(combined, time_variants, rank)

                # derive the Lagrange equation
                lagr = derivatives.pop()
                new_lagr = getLambdaLagrange(lagr, der_symbols, rank)
                g['rearranged'] = derivatives

            if (h['name'], t['sid']) in Lagrange_guard:
                Lagrange_guard[(h['name'], t['sid'])].append(new_lagr)
            else:
                Lagrange_guard[(h['name'], t['sid'])] = [new_lagr]

    # write json file of this network of QSHIOAs
    tree_object['QSHIOAs'] = QSHIOAs
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
    pfile = open('PrecomputedLagrange.py', 'w')
    pfile.write('from math import *\r\n\r\n')

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


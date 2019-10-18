#!/usr/bin/env python3

import sys
import sympy as S
from sympy import Pow
import json

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
    derivatives = [str(fp) for fp in derivatives]
    return derivatives

if __name__ == "__main__":
    if len(sys.argv) == 1:
        print("Need arguments")
        exit()
    elif len(sys.argv) == 2:
        print("By default, the qss rank is set to 1")

    # get the file name and the desired qss rank from the input arguments
    file_name = sys.argv[1]
    qss_rank = int(sys.argv[2])

    if qss_rank < 1:
        print("the rank must be >= 1")
        exit()

    # read the json file and create a tree object
    tree_object = readJson(file_name)
    tree_object['qss_rank'] = qss_rank

    # Copy all HIOA for the conversion process
    QSHIOAs = tree_object.pop('HIOAs')

    # conversion (differentiation)
    for h in QSHIOAs:
        locations = h['locations']
        # obtain derivatives (symoblic) for QSS interpretation
        for l in locations:
            # differentiate the ODEs
            for f in l['ODEs']:
                equation = conversionSympyMath(f['RHS'])
                ## collect all the variable names for differentiation
                time_variants = [var['name'] for var in (h['X_C'] + h['I'])]
                derivatives = differentiate(equation, time_variants, qss_rank-1)
                ## integral of the derivative
                integral = f['LHS'].split("_dot")[0] + "(t)"
                #print(" Original symbol : " + str(f['LHS']) + ", converted to : " + str(integral) )
                derivatives.insert(0, integral)
                f['derivatives'] = derivatives # add to the json data
            
            # outputUpdate funcdtions are also differentiated
            for f in l['outputUpdates']:
                equation = conversionSympyMath(f['RHS'])
                ## collect all the variable names 
                time_variants = [var['name'] for var in h['X_C']]
                ders = differentiate(equation, time_variants, qss_rank)
                f['derivatives'] = ders

        # input/output/internal smooth tokens name tags
        for var in (h['I'] + h['O'] + h['X_C']):
            var['smoothTokens'] = differentiate(var['name'], [var['name']], qss_rank)

        # for each transition, declare attach the id numbers of the locations
        for t in h['transitions']:
            for l in locations:
                if t['src'] == l['name'] : t['sid'] = l['id']
                if t['dst'] == l['name'] : t['did'] = l['id'] 
            # also, rearrange the guard expression, such that
            # the right-hand side is zero
            for g in t['guards']:
                lexpr = S.sympify(conversionSympyMath(g['LHS']))
                rexpr = S.sympify(conversionSympyMath(g['RHS']))
                combined = lexpr - rexpr
                g['rearranged'] = str(combined)

    # write json file of this network of QSHIOAs
    tree_object['QSHIOAs'] = QSHIOAs
    writeJson(file_name, tree_object)
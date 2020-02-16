#!/usr/bin/env python3

import sys
import sympy as S
from sympy import Pow
import json

# convert some Matlab function notation into sympy notation
def conversionSympyMath(MatlabExpr):
    # the first keyword to convert is 'power' to 'Pow'
    SymExpr = MatlabExpr.replace('power', 'Pow')
    SymExpr = SymExpr.replace('//', '/') # some mistakes from the Simulink to json parser
    return SymExpr

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

if __name__ == "__main__":
    # get the file name and the desired rank from the input arguments
    file_name = sys.argv[1]
    rank = 2 # mqss has rank always 2

    # read the json file and create a tree object
    with open(file_name) as json_file:
        tree_object = json.load(json_file)

    # Copy all HIOA for the conversion process
    QSHIOAs = tree_object.pop('HIOAs')

    id_gen = 0
    for h in QSHIOAs:
        locations = h['locations']
        # obtain derivatives (symoblic) for QSS interpretation
        h['X_C'] = h['X_C'] + h.pop('X_D')
        for l in locations:
            # differentiate the ODEs
            for f in l['ODEs']:
                equation = conversionSympyMath(f['RHS'])
                ## collect all the variable names for differentiation
                time_variants = [var['name'] for var in (h['X_C'] + h['I'])]
                derivatives = differentiate(equation, time_variants, rank-1)
                ## subject variable representing the derivative
                subject = f['LHS'].split("_dot")[0] + "(t)"
                #print(" Original symbol : " + str(f['LHS']) + ", converted to : " + str(subject) )
                derivatives.insert(0, subject)
                f['derivatives'] = derivatives # add to the json data
            
            # outputUpdate funcdtions are also differentiated
            for f in l['outputUpdates']:
                equation = conversionSympyMath(f['RHS'])
                ## collect all the variable names 
                time_variants = [var['name'] for var in h['X_C']]
                ders = differentiate(equation, time_variants, rank)
                f['derivatives'] = ders

        # input/output/internal smooth tokens name tags
        for var in (h['I'] + h['O'] + h['X_C']):
            var['smoothTokens'] = differentiate(var['name'], [var['name']], rank)

        # for each transition, declare attach the id numbers of the locations
        for t in h['transitions']:
            for l in locations:
                if t['src'] == l['name'] : t['sid'] = l['id']
                if t['dst'] == l['name'] : t['did'] = l['id'] 
            # also, rearrange the guard expression, such that
            # the right-hand side is zero
            for g in t['guards']:
                ## collect all the variable names for differentiation
                time_variants = [var['name'] for var in (h['X_C'] + h['I'])]
                lexpr = S.sympify(conversionSympyMath(g.pop('LHS')))
                rexpr = S.sympify(conversionSympyMath(g.pop('RHS')))
                combined = lexpr - rexpr
                derivatives = differentiate(combined, time_variants, rank)
                g['rearranged'] = derivatives

    # write json file of this network of QSHIOAs
    tree_object['QSHIOAs'] = QSHIOAs
    # modify the filename extension to .qshioa
    with open(file_name, 'w') as outfile:
        json.dump(tree_object, outfile, indent=4)



    
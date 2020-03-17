#!/usr/bin/env python3

import sys
import sympy as S
from sympy import Pow
import json

# convert some Matlab function notation into sympy notation
def conversionSympyMath(MatlabExpr):
    # the first keyword to convert is 'power' to 'Pow'
    StringExpr = str(MatlabExpr)
    StringExpr = StringExpr.replace('power', 'Pow')
    StringExpr = StringExpr.replace('//', '/') # some mistakes from the Simulink to json parser
    return StringExpr

if __name__ == "__main__":
    # get the file name
    file_name = sys.argv[1]

    # read the json file and create a tree object
    with open(file_name) as json_file:
        tree_object = json.load(json_file)

    # Copy all HIOA for the conversion process
    QSHIOAs = tree_object.pop('HIOAs')

    for h in QSHIOAs:
        # X_D can be simply X_C with zero derivative
        h['X_C'] = h['X_C'] + h.pop('X_D')
        h.pop('X_DOT') # we don't need this information
        
        # to store variable names
        name_space = []
        for v in (h['I'] + h['O'] + h['X_C']):
            name_space.append(v['name'])
            v['name'] = v['name'] + '(t)'
        
        for x in h['X_C']:
            var = S.sympify(x['name'])
            der = [str(var.diff())]
            x['derivatives'] = der

        # refine ODEs
        for l in h['locations']:
            for f in l['ODEs']:
                equation = S.sympify(conversionSympyMath(f['RHS']) )
                subject_name = S.sympify(f['LHS'].split("_dot")[0] + "(t)")
                f['LHS'] = str(subject_name.diff())
                ## collect all the variable names for differentiation
                for name in name_space:
                    equation = equation.subs(name, S.sympify(name + '(t)'))
                f['RHS'] = str(equation)

        # refine output updates
            # outputUpdate funcdtions are also differentiated
            for f in l['outputUpdates']:
                equation = S.sympify(conversionSympyMath(f['RHS']) )
                f['LHS'] = f['LHS'] + "(t)"
                for name in name_space:
                    equation = equation.subs(name, S.sympify(name + '(t)'))
                f['RHS'] = str(equation)

        for t in h['transitions']:
            # set the source and destination node id
            for l in h['locations']:
                if t['src'] == l['name'] : t['sid'] = l['id']
                if t['dst'] == l['name'] : t['did'] = l['id'] 

            # refine guards
            for g in t['guards']:
                lexpr = S.sympify(conversionSympyMath(g.pop('LHS')))
                rexpr = S.sympify(conversionSympyMath(g.pop('RHS')))
                equation = lexpr - rexpr
                for name in name_space:
                    equation = equation.subs(name, S.sympify(name + '(t)'))
                g['LHS'] = str(equation)
                g['RHS'] = 0

            # refine resets
            for r in t['resets']:
                r['LHS'] = r['LHS'] + '(t)'
                equation = S.sympify(conversionSympyMath(r.pop('RHS') ))
                for name in name_space:
                    equation = equation.subs(name, S.sympify(name + '(t)'))
                r['RHS'] = str(equation)
            
        # initialization
        h['initialLocation'] = h['initialLocation']['id']
        initialization = h['initialization']
        for init in initialization:
            init['LHS'] = init['LHS'] + '(t)'
            equation = S.sympify(conversionSympyMath(init.pop('RHS') ))
            for name in name_space:
                equation = equation.subs(name, S.sympify(name + '(t)'))
            init['RHS'] = str(equation)

    Lines = tree_object['Lines']
    for line in Lines:
        line['srcVarName'] = line['srcVarName'] + '(t)'
        line['dstVarName'] = line['dstVarName'] + '(t)'
        
    # write json file of this network of QSHIOAs
    tree_object['QSHIOAs'] = QSHIOAs
    # modify the filename extension to .qshioa
    with open("1" + file_name, 'w') as outfile:
        json.dump(tree_object, outfile, indent=4)



    
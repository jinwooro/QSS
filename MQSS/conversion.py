#!/usr/bin/env python3

import sys
import sympy as S
from sympy import Pow
import json
import pprint

pp = pprint.PrettyPrinter(indent = 4)

# convert some Matlab function notation into sympy notation
def conversionSympyMath(MatlabExpr):
    # the first keyword to convert is 'power' to 'Pow'
    StringExpr = str(MatlabExpr)
    StringExpr = StringExpr.replace('power', 'Pow')
    StringExpr = StringExpr.replace('//', '/') # some mistakes from the Simulink to json parser
    return StringExpr

def makeFunctionOfTime(eq):
    pass

if __name__ == "__main__":
    # get the file name
    file_name = sys.argv[1]
    order = int(sys.argv[2])

    # read the json file and create a tree object
    with open(file_name) as json_file:
        tree_object = json.load(json_file)

    # Copy all HIOA for the conversion process
    QSHIOAs = tree_object.pop('HIOAs')

    for h in QSHIOAs:
        # X_D can be simply X_C with zero derivative
        h['X_C'] = h['X_C'] + h.pop('X_D')
        h.pop('X_DOT') # we don't need this information
        
        # to store all variable names
        all_variables = []
        for v in (h['I'] + h['O'] + h['X_C']):
            name = v['name']
            sym = v['name'] + '(t)'
            all_variables.append( (S.sympify(name), S.sympify(sym) ) )
            v['name'] = sym # the new name has the form x(t)
        
        for x in h['X_C']:
            var = S.sympify(x['name'])
            der = [str(var.diff())]
            x['derivatives'] = der

        # refine ODEs and generate QSS solution equations
        for l in h['locations']:
            for f in l['ODEs']:
                equation = S.sympify(conversionSympyMath(f['RHS']) )
                alias = f['LHS'].split("_dot")[0]
                subject_name = S.sympify(alias + "(t)")
                f['subject'] = str(subject_name)
                f['LHS'] = str(subject_name.diff())
                equation = equation.subs(all_variables)
                f['RHS'] = str(equation)

            t = S.sympify('t')

            qss_set = []
            # create qss1 interpretation for the variables that changes (have ODEs)
            qss1_interpretation = []
            for f in l['ODEs']:
                alias = f['subject'].split("(")[0] 
                # basically, in QSS1, the qss interpretation is simply the hysteresis, i.e., x(t) = x(0) = x
                pair = ( S.sympify(f['subject']) , S.sympify(alias) )
                qss1_interpretation.append(pair)
            qss_set.append(qss1_interpretation)

            ode_dict = { S.sympify(f['subject']) : S.sympify(f['RHS']) for f in l['ODEs']}

            # QSS order is always >= 2
            for i in range(1, order+1):
                current_interpretation = qss_set[i-1] # the next order QSS interpretation is simply the solution of the previous order QSS.
                next_interpretation = []
                
                for f in l['ODEs']:
                    ode = S.sympify(f['RHS'])      # for a given ODE,
                    new_ode = ode.subs(current_interpretation)  # substitute the QSS interpretations
                    solution = S.sympify(f['subject'].split("(")[0]) + new_ode * t  # then, apply Euler's method using the QSS substituted ODE
                    pair = ( S.sympify(f['subject']), solution )
                    next_interpretation.append(pair)
                
                qss_set.append(next_interpretation)

            lowQss = { str(tup[0]) : S.sympify(tup[1]).subs(all_variables) for tup in qss_set[order-2] }
            highQss = { str(tup[0]) : S.sympify(tup[1]).subs(all_variables) for tup in qss_set[order-1] }

            for f in l['ODEs']:
                subject = f['subject']
                f['lowQSS'] = str(lowQss[subject])
                f['highQSS'] = str(highQss[subject])
                del f['relation']

        # refine output updates
            # outputUpdate funcdtions are also differentiated
            for f in l['outputUpdates']:
                equation = S.sympify(conversionSympyMath(f['RHS']) )
                f['LHS'] = f['LHS'] + "(t)"
                equation = equation.subs(all_variables)
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
                equation = equation.subs(all_variables)
                g['LHS'] = str(equation)
                g['RHS'] = 0

            # refine resets
            for r in t['resets']:
                r['LHS'] = r['LHS'] + '(t)'
                equation = S.sympify(conversionSympyMath(r.pop('RHS') ))
                equation = equation.subs(all_variables)
                r['RHS'] = str(equation)
            
        # initialization
        h['initialLocation'] = h['initialLocation']['id']
        initialization = h['initialization']
        for init in initialization:
            init['LHS'] = init['LHS'] + '(t)'
            equation = S.sympify(conversionSympyMath(init.pop('RHS') ))
            equation = equation.subs(all_variables)
            init['RHS'] = str(equation)

    Lines = tree_object['Lines']
    for line in Lines:
        line['srcVarName'] = line['srcVarName'] + '(t)'
        line['dstVarName'] = line['dstVarName'] + '(t)'
        
    # write json file of this network of QSHIOAs
    tree_object['QSHIOAs'] = QSHIOAs
    # modify the filename extension to .qshioa
    names = file_name.split(".")
    with open(names[0] + "." + names[1], 'w') as outfile:
        json.dump(tree_object, outfile, indent=4)



    
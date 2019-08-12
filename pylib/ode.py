#!/usr/bin/env python
# -*- coding: utf-8 -*- 

import sympy as S
import mpmath as poly
import numpy as N
import math

class NoRealRoots(Exception):
    pass

# A helper function that reforms the input expression
# Input: An expression, a dictionary (of the form ''key' : (a,b)')
# Output: a couple of (a',b')
def substitution(expr, Xq):
    newExpr1 = S.sympify(expr)
    newExpr2 = S.sympify(expr)
    for key, val in Xq.items():
        newExpr1 = newExpr1.subs(key + '(t)', val[0])
        newExpr2 = newExpr2.subs(key + '(t)', val[1])
    return (newExpr1, newExpr2)

# A helper function that:
# input: an expression h that o = h(X), tokens for X, rank
# output: nth derivative of o (as a value)
def getToken(expr, X, rank):
    newExpr = S.sympify(expr)
    for i in range(rank):
        newExpr = newExpr.diff()
    for x, tokens in X.items():
        sym = S.sympify(x + '(t)')
        sym_der = sym.diff()
        for i in range(1, rank+1):
            newExpr = newExpr.subs(sym_der, tokens[i])
        newExpr = newExpr.subs(sym, tokens[0])
    return newExpr

# returns a polynomial equation based on the given tokens
def getPolynomialApprox(tokens, rank):
    t = S.sympify('t')
    eq = S.sympify(tokens[0])
    for i in range(1, rank):
        eq = eq + tokens[i] * (t ** i)
    return eq

# defined as a function to apply various numerical integration approaches
# in this function (possible future works)
def Numerical_Integration(init, derivative):
    return S.sympify(init + derivative * S.sympify('t'))

# A helper function that computes based on the current value of X and I (i.e., X[0] and I[0]).
def compute(expr, X, I):
    eq = S.sympify(expr)
    if eq.is_number:
        return eq
    for x, tokens in X.items():
        eq = eq.subs(x + '(t)', tokens[0])
    for i, tokens in I.items():
        eq = eq.subs(i + '(t)', tokens[0])
    return eq

class GUARD:
    def __init__(self, lexpr, comparator, rexpr, vtol = 0.01, ttol = 0.01, iteration = 100):
        # lexpr and rexpr should be in the sympify format
        lk = 0
        rk = 0
        for a in S.Add.make_args(lexpr):
            if (a.is_number):
                lk = a
                break
        for b in S.Add.make_args(rexpr):
            if (b.is_number):
                rk = b
                break
        Q = lk - rk
        # now, we have all the variable terms on the left-hand
        # only a number on the right-hand
        self.lvalue = lexpr - rexpr - Q
        self.rvalue = Q
        self.vtol = vtol
        self.ttol = ttol
        self.iteration = iteration
        self.comparator = comparator        

    # inputs:
    # 1) Xq is a dictionary, where the key is the variable name and the value is a couple of two qss equations.
    # 2) Iq is a dictionary similar to Xq but the input variables.
    def getDelta(self, Xq, Iq):
        z = self.lvalue
        z0 = z
        for x, equations in Xq.items():
            z0 = z0.subs(x + '(t)', equations[0].subs('t', 0))
        for i, equations in Iq.items():
            z0 = z0.subs(i + '(t)', equations[0].subs('t', 0))
        # Now, z0 must be a number (after replacing the variables with numbers)
        Q = self.rvalue
        lqss = z # to solve z with low order qss
        hqss = z # to solve z with high order qss
        for x, equations in Xq.items():
            lqss = lqss.subs(x + '(t)', equations[0])
            hqss = hqss.subs(x + '(t)', equations[1])
        for i, equations in Iq.items():
            lqss = lqss.subs(x + '(t)', equations[0])
            hqss = hqss.subs(x + '(t)', equations[1])

        iter = 0
        while(iter <= self.iteration):
            iter = iter + 1
            tl = S.solve(lqss - Q)
            th = S.solve(hqss - Q)
            # Remove the complex roots and negative roots
            for a in tl:
                if (S.sympify(a).is_real == False):
                    tl.remove(a)
                    continue
                if a < 0:
                    tl.remove(a)
            for b in th:
                if (S.sympify(b).is_real == False):
                    th.remove(b)
                if b < 0:
                    th.remove(b)

            # If any of them are not reaching to the boundary
            if (len(tl) == 0) or (len(th) == 0):
                Q = (z0 + Q) / 2
                continue             

            t1 = min(tl)
            t2 = min(th)
            # Check if two time values are within the time tolerance value
            if (abs(t1 - t2) <= self.ttol):
                return t2

            # If two time values are not similar, then reduce Q by half
            Q = (z0 + Q) / 2
        return math.inf # return infinite to indicate that this guard condition can be ignored


    # Input: current values of X and I
    # Output: True if this guard condition is satisfied. Otherwise, false.
    def isTrue(self, X, I):
        l = self.lvalue
        r = self.rvalue
        for x, tokens in X.items():
            l = l.subs(S.sympify(x + '(t)'), tokens[0])
        for i, tokens in I.items():
            l = l.subs(S.sympify(i + '(t)'), tokens[0])
        # if the difference is within the vtol boundary
        if (abs(l - r) <= self.vtol):
            return True
        else:
            return False

    def __str__(self):
        ret = (str(self.lvalue) + self.comparator + str(self.rvalue))
        return ret

class ODE:
    def __init__(self, subject, rvalue, order = 1):      
        self.subject = subject
        self.lvalue = [S.sympify(subject + '(t)').diff()]
        self.rvalue = [S.sympify(rvalue)]
        for rank in range(order-1):
            diff = self.lvalue[rank].diff()
            self.lvalue.append(diff)
            diff = self.rvalue[rank].diff()
            self.rvalue.append(diff)
        self.order = order

    # rank ≥ 1, E.g., rank = 1 returns the first derivative 
    def getToken(self, X, I, rank):
        DE = self.rvalue[rank-1]
        for var, tokens in I.items():
            symbol = S.sympify(var + '(t)')
            for r in range(1, rank):
                symbol = symbol.diff()
                DE = DE.subs(symbol, tokens[r])
            DE = DE.subs(S.sympify(var + '(t)'), tokens[0])
        for var, tokens in X.items():
            symbol = S.sympify(var + '(t)')
            for r in range(1, rank):
                symbol = symbol.diff()
                DE = DE.subs(symbol, tokens[r])
            DE = DE.subs(S.sympify(var + '(t)'), tokens[0])
        return DE

    # returns the equation of the qss approximation in the given rank
    # rank == 1 is QSS2, rank == 2 is QSS3
    def getQSS(self, X, I, rank):
        xq_dot = self.rvalue[0] # the first derivative equation
        for x, tokens in X.items():
            xq = getPolynomialApprox(tokens, rank)
            xq_dot = xq_dot.subs(S.sympify(x + '(t)'), xq)
        for i, tokens in I.items():
            xq = getPolynomialApprox(tokens, rank)
            xq_dot = xq_dot.subs(S.sympify(i + '(t)'), xq)
        initial_value = X[self.subject][0]
        return Numerical_Integration(initial_value, xq_dot)
        
    def solve(self, time, qss):
        return qss.subs('t', time)

    def __str__(self):
        ode = str(self.lvalue) + ' = ' + str(self.rvalue)
        ret = ' '.join([ode])
        return ret

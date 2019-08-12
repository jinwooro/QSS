#!/usr/bin/env python3

import sympy as S
import math

# A helper function is declared in the local file scope

# A helper function for creating a couple of qss equations for the output variables
def reformCoupledQss(expr, Xq):
    newExpr1 = S.sympify(expr)
    newExpr2 = S.sympify(expr)
    for key, val in Xq.items():
        newExpr1 = newExpr1.subs(key + '(t)', val[0])
        newExpr2 = newExpr2.subs(key + '(t)', val[1])
    return (newExpr1, newExpr2)

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

def substituteTime(expr, t):
    newExpr = expr
    newExpr = newExpr.subs(S.sympify('t'), t)
    return newExpr

# A ordinary differential equation (ODE)
class ODE:
    def __init__(self, subject, rvalue, qorder = 1):
        self.subject = subject
        # qorder == 1 means QSS1. Thus, qorder ≥ 1 for always.
        self.qorder = qorder
        # self.rvalue is an array with the index indicates the derivative rank
        # index = 0, derivative rank = 0
        # index = 1, derivative rank = 1
        self.rvalue = [None, S.sympify(rvalue)] 
        # obtain the higher level derivatives
        for rank in range(1, qorder):
            diff = self.rvalue[rank].diff()
            self.rvalue.append(diff)

    # Returns the derivative coefficient
    # rank ≥ 1, E.g., rank = 1 indicates the first derivative
    def getToken(self, X, I, rank):
        # setup the equation for the token
        token = self.rvalue[rank] 
        # Z = X union I
        Z = X.copy()
        Z.update(I)
        for var, tokens in Z.items():
            symbol = S.sympify(var + '(t)')
            for i in range(1, rank):
                symbol = symbol.diff()
                token = token.subs(symbol, tokens[i])
            token = token.subs(S.sympify(var + '(t)'), tokens[0])
        return token

    # Returns the equation of the qss approximation at the given rank
    # E.g., rank == 2 is QSS2, rank == 3 is QSS3
    def getQSS(self, X, I, rank):
        if (rank == 0):
            return X[self.subject][0]
        # Starting from the first derivative equation
        xq_dot = self.rvalue[1] 
        # Z = X union I (shallow copy)
        Z = X.copy()
        Z.update(I)
        for z, tokens in Z.items():
            xq = getPolynomialApprox(tokens, rank)
            xq_dot = xq_dot.subs(S.sympify(z + '(t)'), xq)
        initial_value = Z[self.subject][0]
        return Numerical_Integration(initial_value, xq_dot)

    def __str__(self):
        ode = str(self.subject) + ' = ' + str(self.rvalue)
        ret = ' '.join([ode])
        return ret

# A normal equation
class EQN:
    def __init__(self, subject, rvalue):
        self.subject = subject
        self.rvalue = S.sympify(rvalue)

    # rank ≥ 1, E.g., rank = 1 returns the first derivative 
    def getToken(self, X, rank):
        token = self.rvalue
        # obtain the derivative (TODO: consider this maybe precomputed at initialization)
        for i in range(rank):
            token = token.diff()
        for var, tokens in X.items():
            symbol = S.sympify(var + '(t)')
            for i in range(1, rank+1): # rank+1 is very important
                symbol = symbol.diff()
                token = token.subs(symbol, tokens[i])
            token = token.subs(S.sympify(var + '(t)'), tokens[0])
        return token

    def compute(self, X):
        expr = self.rvalue
        for x, tokens in X.items():
            expr = expr.subs(S.sympify(x + '(t)'), tokens[0])
        return expr

    def __str__(self):
        ode = str(self.subject) + ' = ' + str(self.rvalue)
        ret = ' '.join([ode])
        return ret

class GUARD:
    def __init__(self, lexpr, comparator, rexpr, vtol = 0.01, ttol = 0.001, iteration = 100):
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
        self.rvalue = Q * (-1)
        self.vtol = vtol
        self.ttol = ttol
        self.iteration = iteration
        self.comparator = comparator 

    # inputs:
    # 1) Xq is a dictionary, where the key is the variable name and the value is a couple of two qss equations.
    # 2) Iq is a dictionary similar to Xq but the input variables.
    def computeDelta(self, Xq, Iq):
        # g is an expression that contains all the variables
        g = self.lvalue 
        g0 = g
        # Z = Xq union Iq
        Z = Xq.copy()
        Z.update(Iq)
        # calculate the initial g value "g0" at t = 0
        for z, q in Z.items():
            g0 = g0.subs(S.sympify(z + '(t)'), S.sympify(q[0]).subs(S.sympify('t'), 0))
        Q = self.rvalue
        lqss = g # to solve z with low order qss
        hqss = g # to solve z with high order qss
        for z, eq in Z.items():
            lqss = lqss.subs(S.sympify(z + '(t)'), eq[0])
            hqss = hqss.subs(S.sympify(z + '(t)'), eq[1])
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
                if (abs(Q) < self.vtol):
                    break
                Q = (g0 + Q) / 2
                iter = iter + 1
                continue 
            t1 = min(tl)
            t2 = min(th)
            # If two time values are not similar, then reduce Q by half
            if (abs(t1 - t2) > self.ttol):
                Q = (g0 + Q) / 2
                iter = iter + 1
                continue
            return t2
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
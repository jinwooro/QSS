#!/usr/bin/env python3

import sympy as S
from .equation import *

class Edge:
    def __init__(self, src_id, dst_id):
        self.src = src_id
        self.dst = dst_id
        self.resets = {}
        self.guards = []

    def isGuardTrue(self, X, I):
        # TODO: for now, if any guard is true, then just return true
        # In the future, it should be OR and AND relationship enabled
        for guard in self.guards:
            if guard.isTrue(X, I):
                return True
        return False

    def computeDelta(self, Xq, Iq):
        # Here, we need to consider various guard conditions
        delta = set()
        for guard in self.guards:
            delta.add(guard.computeDelta(Xq, Iq))
        return min(delta)

    def addReset(self, subject, rvalue):
        self.resets[subject] = EQN(subject, rvalue)

    def addGuard(self, lvalue, comparator, rvalue):
        self.guards.append(GUARD(lvalue, comparator, rvalue))

    def executeResets(self, X, I):
        # Z = X union I
        Z = X.copy()
        Z.update(I)
        for var, eqn in self.resets.items():
            X[var][0] = eqn.compute(Z)

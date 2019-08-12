#!/usr/bin/env python3

from .equation import *
from .edge import *

class Location:
    def __init__(self, id):
        self.id = id # location id
        self.f = {} # for intra-location transition
        self.h = {} # for output updating
        self.outgoingEdges = [] # for inter-location transition

    def addODE(self, subject, rvalue, qorder):
        self.f[subject] = ODE(subject, rvalue, qorder)

    def addOutputUpdate(self, subject, rvalue):
        self.h[subject] = EQN(subject, rvalue)

    def addOutgoingEdge(self, edge):
        self.outgoingEdges.append(edge) 

    # inter-location 
    def anyEdgeEnabled(self, X, I):
        for edge in self.outgoingEdges:
            if edge.isGuardTrue(X, I):
                edge.executeResets(X, I)
                return True, edge.dst
        return False, self.id

    # get token
    def updateTokens(self, X, I, O, rank):
        # Must compute X before O
        for key in X:
            if key in self.f:
                X[key][rank] = self.f[key].getToken(X, I, rank)
            else:
                X[key][rank] = 0
        # compute O
        for key in O:
            if key in self.h:
                O[key][rank] = self.h[key].getToken(X, rank)
            else:
                O[key][rank] = 0

    # update the value of the output variables
    def updateOutput(self, X, O):
        for key, eq in self.h.items():
            O[key][0] = eq.compute(X)

    # future work
    def entryAction(self):
        pass

    # future work
    def exitAction(self):
        pass

    # get qss equation 
    def getQssEquation(self, X, I, O, qorder):
        Xq = {}
        Oq = {}
        # fill Xq first before filling Oq
        for key in X:
            if key in self.f:
                qss_l = self.f[key].getQSS(X, I, rank=qorder-1)
                qss_h = self.f[key].getQSS(X, I, rank=qorder)
                Xq[key] = (qss_l, qss_h)
            else:
                Xq[key] = (X[key][0], X[key][0])
        # fill Oq
        for key in O:
            if key in self.h:
                Oq[key] = reformCoupledQss(self.h[key].rvalue, Xq) 
            else:
                Oq[key] = (O[key][0],O[key][0])
        return Xq, Oq

    # intra-location
    def updateContinuousVariables(self, X, Xq, time):
        # update the variables that are defeind as ODE in this location
        # the rest continuous variables are left intact
        for key in self.f:
            X[key][0] = substituteTime(Xq[key][1], time)

    def getDelta(self, Xq, Iq):
        delta = set()
        for edge in self.outgoingEdges:
            delta.add(edge.computeDelta(Xq, Iq))
        if len(delta) == 0:
            return 0.1 # TODO: this is just a value... need to be fixed
        else:
            return min(delta)


        

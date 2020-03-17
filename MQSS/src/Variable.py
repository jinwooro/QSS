import sympy as S
from Util import *

class Variable:
    def __init__(self, data):
        self.name = data['name'] # used as the key in the dictionary
        self.value = float(data['initialValue'])
        self.derivative = 0
    
    def __repr__(self):
        return str(self.ex1) + ", " + str(self.ex2)
        #return str(self.smooth_tokens)
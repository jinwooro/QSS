import sympy as S
from .Util import *

class Variable:
    def __init__(self, data):
        self.name = data['name'] # used as the key in the dictionary
        # A token is triple ( symbol, value, expression)
        self.smooth_tokens = [ [S.sympify(token), 0] for token in data['smoothTokens']]

        # q will store the q_x(t) expression of x(t)
        # in the initialization, it is just zero
        self.ex1 = 0 # lower order x(t), e.g., QSS1
        self.ex2 = 0 # higher order x(t), e.g., QSS2

        # initial value
        self.set_current_value(data['initialValue'])

    def get_symbol(self):
        return self.smooth_tokens[0][0]

    # returns a specific token value
    def get_token_value(self, token_order):
        return self.smooth_tokens[token_order][1]

    def get_token_values(self):
        values = [ token[1] for token in self.smooth_tokens ]
        return values

    def set_token_value(self, new_value, token_order):
        self.smooth_tokens[token_order][1] = new_value

    def reset_values(self):
        for i in range(1, len(self.smooth_tokens)):
            self.smooth_tokens[i][1] = 0
        self.ex1 = 0
        self.ex2 = 0

    def get_current_value(self):
        return self.smooth_tokens[0][1]

    def set_current_value(self, value):
        self.smooth_tokens[0][1] = float(value)

    def evolve_based_on_time(self, time):
        qx = self.ex2
        self.set_current_value(qx.subs( S.sympify('t') , time))

    def __repr__(self):
        return str(self.ex1) + ", " + str(self.ex2)
        #return str(self.smooth_tokens)
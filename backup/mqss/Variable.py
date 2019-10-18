import sympy as S

class Variable:
    def __init__(self, data):
        self.name = data['name'] # used as the key in the dictionary
        self.smooth_token = [ [s, 0] for s in data['smoothTokens']]
        self.expression = S.sympify("0")

    def get_name(self):
        return self.name
    
    def get_smooth_token(self):
        return self.smooth_token

    def get_token_value(self, token_order):
        return self.smooth_token[token_order][1]

    def set_token_value(self, new_value, token_order):
        self.smooth_token[token_order][1] = new_value

    def reset_derivative_tokens(self):
        for i in range(1, len(self.smooth_token)):
            self.smooth_token[i][1] = 0

    def get_current_value(self):
        return self.smooth_token[0][0]

    def set_current_value(self, value):
        self.smooth_token[0][0] = float(value)

    def set_expression(self, new_expression):
        self.expression = new_expression

    def get_expression(self):
        return self.expression

    def evolve_based_on_time(self, time):
        value = self.expression
        value = value.subs(S.sympify('t'), time)
        self.current_value = value

    def __repr__(self):
        return str(self.tokens)



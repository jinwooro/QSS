import sympy as S

class Variable:
    def __init__(self, data):
        self.name = data['name']

        # list of sympify symbols 
        self.symbols = [ S.sympify(token) for token in data['smoothTokens'] ]

        # contains values including smooth tokens
        # Specific order, [a, b, c, ... ], where 
        # 'a' is the current value
        # 'b' is the current first derivative value
        # 'c' is the current second derivative value ... so on
        self.values = [ 0.0 for i in range(0, len(self.symbols))]
        
        # set initial value
        initial_value = float(data['initialValue'])
        self.values[0] = initial_value

    def get_symbol(self):
        return S.sympify(self.name + '(t)')

    # return value based on the index
    def get_value(self, index):
        return float(self.values[index])

    def set_value(self, new_value, index):
        self.values[index] = float(new_value)

    def get_symbol_value_pairs(self):
        return [ (self.symbols[i], self.values[i]) for i in range(0, len(self.values)) ]

    def reset_gradients(self):
        size = len(self.values)
        for i in range(1, size):
            self.values[i] = 0

    def get_current_value(self):
        return self.values[0]

    def set_current_value(self, value):
        self.values[0] = float(value)

    def __repr__(self):
        return self.name + " = " + str(self.values)

    def __getitem__(self, i):
        return self.values[i]
    

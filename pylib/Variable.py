import sympy as S

# inner class
class Token:
    def __init__(self, symbol, value):
        self.symbol = symbol
        self.value = float(value)

    # the setter and getter are used to ensure that 'value' is always float type
    def get_value(self):
        return self.value
    
    def set_value(self, value):
        self.value = float(value)

    def __repr__(self):
        return (str(self.value))

class Variable:
    def __init__(self, data):
        self.name = data['name'] # used as the key in the dictionary
        self.symbol = data['smoothTokens'][0] # used as the symbol in the equation
        self.token = [Token(s,0) for s in data['smoothTokens']]
    
    # this method calculates the value of variable at 'time'
    def compute_next_value(self, time):
        value = 0        
        for i in range(0, len(self.token)):
            coeff = self.token[i].get_value()
            value += coeff * (time ** i)
            # reset the values
            self.token[i].set_value(0)
        self.token[0].set_value(value)

    def get_token_values(self):
        values = [k.value for k in self.token]
        return values

    def set_current_value(self, value):
        self.token[0].set_value(value)

    def current_value(self):
        return self.token[0].get_value()

    def __repr__(self):
        return str(self.token)



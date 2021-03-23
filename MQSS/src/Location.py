import sympy as S

class Location:

    def __init__(self, json_data):
        # location name
        self.name = json_data['name'] 
        # ODEs, key = x_name, value = array of sympy derivative format
        self.ODEs = { f['subject'] : ( S.sympify(f['lowQSS']), S.sympify(f['highQSS']) ) for f in json_data['ODEs'] }
        # updates, key = x_name, value = array of sympy derivative format
        self.Updates = { h['LHS'] : S.sympify(h['RHS']) for h in json_data['outputUpdates'] }
        self.entries = { e['LHS'] : S.sympify(e['RHS']) for e in json_data['entries'] }
        self.Transitions = []

    def get_QSS(self, Variables):
        self.hqss_list = []
        lqss_list = []
        for name, (lqss, hqss) in self.ODEs.items():
            hqss_pair = (S.sympify(name), hqss.subs(Variables))
            lqss_pair = (S.sympify(name), lqss.subs(Variables))
            self.hqss_list.append(hqss_pair)
            lqss_list.append(lqss_pair)
        return self.hqss_list, lqss_list

    def add_outgoing_transition(self, tran):
        self.Transitions.append(tran)

    def update_continuous_variables(self, X, time):
        for name, hqss in self.hqss_list:
            name = str(name)
            value = hqss.subs(S.sympify('t'), time)
            X[name] = value

    def en(self, I, O, X):
        Variables = [ (S.sympify(name), value) for name, value in {**X, **I}.items() ] # convert dictionary to list of tuples
        for LHS, RHS in self.entries.items():
            if LHS in X: # entries can modify X
                X[LHS] = RHS.subs(Variables)
            elif LHS in O: # entries can modify O
                O[LHS] = RHS.subs(Variables)

    def h(self, O, X):
        Variables = [ (S.sympify(name), value) for name, value in X.items() ] # convert dictionary to list of tuples
        for LHS, RHS in self.Updates.items():
            O[LHS] = RHS.subs(Variables)

    def get_name(self):
        return self.name

    def get_enabled_transition(self, I, X, vtol):
        # return any enabled transition
        # when more than one transition is enabled, then return the first found one
        for t in self.Transitions:
            if t.is_enabled(I, X, vtol): # vtol is the state tolerance value
                return t
        # if no transition is enabled, then return None
        return None








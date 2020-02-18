Stateflow chart in Simulink is a modelling language for hybrid systems. It captures the continuous and discete system behaviours throush Ordinary Differential Equations and control mode state transition, respectively. Details of Stateflow can be found [here](https://www.mathworks.com/help/stateflow/ref/chart.html). 

The execution/simulation of Stateflow charts uses the ODE solvers and zero-crossing algorithm in Simulink. By default, RK45 solver with variable step size is used for numerical integration, and adaptive 'bracket' algorithm (see [here](https://folk.ntnu.no/skoge/prost/proceedings/ifac2008/data/papers/3498.pdf)) is used for zero-crossing detection. However, there exist several cases where the Stateflow simulation will produce incorrect result. In particular, even number of zero-crossings and the system state going into complex plane cannot be handled [[Ro, et al., 2019]](https://dl.acm.org/doi/pdf/10.1145/3359986.3361198).

The **goal** of this project is to simulate the Stateflow model correctly.


Step 1: Convering Simulink/Stateflow file into HIOA

    java -jar Simulink2HIOA resource/collision.mdl

Step 2: Converting the generated HIOA json file into QSHIOA

    python3 HIOA4QSS.py <hioa> <num>

Step 3: Running the QSS simulation based on the .QSHIOA file

    python3 qss.py <qshioa>


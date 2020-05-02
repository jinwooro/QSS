# Introduction

In the simulation of hybrid systems, the continuous state evolution is captured via solving ODEs and the discrete state transitions are detected via zero-crossing events. Conventionally, hybrid systems are modelled using Hybrid Automata (HA), and many existing simulation tools, such as Stateflow/Simulink, OpenModelica, and Ptolemy II, allow the simulation of HA. However, 
they sometimes produce incorrect simulation results due to missing zero-crossing events~\cite{MQSS paper}. This is because their zero-crossing detection algorithms are based on overshooting the guard condition (i.e., detecting the sign change), and it is problematic in some cases~\cite{}.

Higher Order Hybrid Automata (HOHA) is an extension of HA, designed to incorporate higher order ODEs to captrue the continuous dynamics more precisely. Furthermore, the execution of HOHA based on a special step size calculation algorithm that can deal with discontiuities. In particular, the guard conditions are converted into an equivalent Taylor polynomials using the higher order derivatives, and solved for the time of next zero-crossing event. The found time value is then considered for deciding the simulation step size. 

This implementation not only comes with the execution of HOHA, but also the syntactic conversion of the Stateflow/Simulink model (`.mdl`) into HOHA (`.json`). No special software installation is required other than Matlab/Simulink (for modelling in Stateflow) and Python3 (for simulation). 

# How to run the HOHA simulation

Running the simulation can be done by entering the following command format:

```
python3 simulation.py [solver] [Time] [File]
```

* solver : two solvers are available, `HOHA` or `MQSS`.
* Time : simulation end time (in seconds)
* File : path to the model file

FOr example, the following start the simulation of `example.mdl` model using `HOHA` for 10 seconds 
```
python3 simulation.py HOHA 10 example.mdl
```

## Bouncing Ball Example

Find the boncing ball example in the example folder. Navigate to *example/bouncing_ball/* and double-click the `bouncingBall.mdl` file. If Matlab is correctly installed, this will open Simulink. Alternatively, the file can be opened in Simulink manually. There is a Stateflow chart in the model as shown below:

![bouncingBall](images/bouncing_ball.png)

The variable `x` is the vertical position of the ball, and `v` is the velocity. The initial values are `x=10` and `v=0`.  `v_out` and `x_out` are the output variables, and they have the same value as `v` and `x`, respectively. When `x <= 0` on the transition is satisfied, the velocity is instantaneously changed to `v = -0.8 v`, and the position is `x= 0.001` (to avoid `x=0` re-triggering the transition endlessly). After the simulation, we can see the behaviour of the bouncing ball.



python3 simulation.py HOHA 10 example/bouncing_ball/bouncingBall.mdl




 Also, it has an ability to use Modified Quantized State System (MQSS)~\cite{} as the simulation solver. 



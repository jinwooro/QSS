# Introduction

In the simulation of hybrid systems, the continuous state evolution is captured via solving ODEs and the discrete state transitions are detected via zero-crossing events. Conventionally, hybrid systems are modelled using Hybrid Automata (HA), and many existing simulation tools, such as Stateflow/Simulink, OpenModelica, and Ptolemy II, allow the simulation of HA. However, 
they sometimes produce incorrect simulation results due to missing zero-crossing events~\cite{MQSS paper}. This is because their zero-crossing detection algorithms are based on overshooting the guard condition (i.e., detecting the sign change), and it is problematic in some cases~\cite{}.

Higher Order Hybrid Automata (HOHA) is an extension of HA, designed to incorporate higher order ODEs to captrue the continuous dynamics more precisely. Furthermore, the execution of HOHA based on a special step size calculation algorithm that can deal with discontiuities. In particular, the guard conditions are converted into an equivalent Taylor polynomials using the higher order derivatives, and solved for the time of next zero-crossing event. The found time value is then considered for deciding the simulation step size. 

This implementation not only comes with the execution of HOHA, but also the syntactic conversion of the Stateflow/Simulink model (.mdl file) into HOHA (.json). No special software installation is required other than Matlab/Simulink (for modelling in Stateflow) and Python3 (for simulation). 

# How to run the HOHA simulation

Running the simulation can be done by entering the following command format:

python3 simulation.py \[solver\] \[Time\] \[File\]

* solver : two solvers are available, HOHA or MQSS.
* Time : simulation end time
* File : path to the model file

## Bouncing Ball Example

Find the boncing ball example in the example folder. Navigate to the folder called 'example/bouncing_ball/' and open 'bouncingBall.mdl' model by double-click. This will open Matlab Simulink, and you will be able to see a Stateflow chart in the model as shown below:

Figure here.

Now, the HOHA simulation of this stateflow model can start by typing the following command

python3 simulation.py HOHA 10 example/bouncing_ball/bouncingBall.mdl




 Also, it has an ability to use Modified Quantized State System (MQSS)~\cite{} as the simulation solver. 



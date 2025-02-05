# Introduction

In the simulation of hybrid systems, the continuous state evolution is captured via solving ODEs and the discrete state transitions via zero-crossing events. Conventionally, hybrid systems are modelled using Hybrid Automata (HA), and many existing simulation tools, such as Stateflow/Simulink, OpenModelica, and Ptolemy II, allow the simulation of HA. However, 
they sometimes produce incorrect simulation results due to the missing zero-crossing detection problem. This is because their zero-crossing detection algorithms are based on overshooting the guard condition (i.e., detecting the sign change), which cannot deal with the even number of zero-crossing problem (mostly in steep functions) and the transcendental functions that can lead to complex numbers.

Higher Order Hybrid Automata (HOHA) is an extension of HA, designed to incorporate higher order ODEs to captrue the continuous dynamics more precisely. Furthermore, the execution of HOHA based on a special step size calculation algorithm that can deal with the discontiuities based on transendental guard conditions. In particular, the guard conditions are converted into an equivalent Taylor polynomials using the higher order derivatives, then solved for the time which the next zero-crossing happens. The found time value is considered during deciding the simulation step size. 

In this implementation, we provide not only the execution of HOHA, but also the syntactic conversion of the Stateflow/Simulink model (`.mdl`) into HOHA (`.json`). For modelling in Stateflow chart, Matlab/Simulink installation is required. This implementation uses Python3 and Java for simulation and syntactic conversion, respectively.

# <a name="all-in-one"></a> How to run the HOHA simulation (all-in-one command)

Running the HOHA simulation requires some preparation processes. For instance, the syntactic translation from stateflow  &rarr; Hybrid Input Output Automata (HIOA) &rarr; HOHA needs to be done. For convenience, these can be done in one single all-in-one command.

```
python3 simulation.py <solver> <Time> [config] <FilePath>
```

* solver : two solvers are available, `HOHA` or `MQSS`.
* Time : simulation end time (in seconds)
* FilePath : path to the model file
* config : simulation configuration (optional)

For example, the following command starts the simulation of *example.mdl* model using *HOHA* for 10 seconds 
```
python3 simulation.py HOHA 10 example.mdl
```

Note: we will describe [configuration options](#simulation-configuration) after presenting the bouncing ball example.

Instead of using the all-in-one command, Section [more commands](#more-commands) presents how to separately execute the syntactic conversion and the simulation. This can be useful when doing the same simulation again, where repreated syntactic conversion is not required. Note that the simulation configuration can be changed without the syntactic conversion.

## Bouncing Ball Example

Find the boncing ball example in the example folder. Navigate to *example/bouncing_ball/* and double-click the `bouncingBall.mdl` file. If Matlab is correctly installed, this will open Simulink. Alternatively, the file can be opened in Simulink manually. There is a Stateflow chart in the model as shown below:

<img src="images/bouncing_ball.png" width=400>

The variable `x` is the vertical position (m) of the ball, and `v` is the velocity (m/s). The initial values are `x=10` and `v=0`.  `v_out` and `x_out` are the output variables, and they have the same value as `v` and `x`, respectively. When `x <= 0` on the transition is satisfied, the velocity is instantaneously changed to `v = -0.8 v`, and the position is `x= 0.001` (to avoid retriggering the transition endlessly at `x=0`). The simulation result of the bouncing ball model is:

<img src="images/bouncing_ball_result.png" width=700>

Next, we run the HOHA simulation for the same example. On the terminal, run the following command:
```
python3 simulation.py HOHA 15 example/bouncing_ball/bouncingBall.mdl
```

The folder called `generated` contains the generated files for/from the simulation. This includes a `csv` file, which records the values of every variable in each simulation step. Open this file with an editor program such as Excel, and plot the data.
Note that the line from the Simulink simulation perfectly overlaps with this line.

<img src="images/HOHA_bouncing_ball.png" width=700>

## Simulation configuration

Simulation of HOHA can be configured in two ways. 1) using the commandline flags, 2) modifying the default_setup.json file. 

### Commandline configuration

- HOHA order : -n <value>
- LTE (desired local truncation error) : -v <value>
- Maximum step size : -x <value>

For example, the following command will start the third order HOHA simulation with desired LTE = 0.00001
```
python3 simulation.py HOHA 100 -n 3 -v 0.00001 example.mdl
```

Althernatively, simply open the `default_setup.json` with any text editor you like, and modify the parameters.

### Maximum step size configuration

The purpose of simulation is to generate system traces useful for human to observe and investigate. If the simulation is so perfect such that it can compute the final answer directly from the given initial values, then it will only produce two data points, which are initial values and the final answer. Although the final answer is correct, only two data points are not enough to visualize the traces. For this purpose, the maximum step size option enforces a certain resolution of the simulation visualization. Especially, when the high order HOHA is used, many simulation steps are actualy skipped (because the large step size still ensures the small error), and resulting in the aforementioned problem.

Let us run the bouncing ball example with the fourth order HOHA. The maximum step size is 1 second (default)
```
python3 simulation.py HOHA 15 -n 4 -x 1 example/bouncing_ball/bouncingBall.mdl
```

The plotted graph of the generated `csv` file in the `generated` folder looks like this:

<img src="images/HOHA_fourth.png" width=700>

As can be seen, the resolution is very coarse due to small number of steps. In fact, less number of steps are preferrable for the simulation performance, however, too small number of steps are not useful for trace observation. 

Now, try the same simulation with different maximum step size. 

```
python3 simulation.py HOHA 15 -n 4 -x 0.1 example/bouncing_ball/bouncingBall.mdl
```

<img src="images/HOHA_fourth2.png" width=700>

# More Commands

The [all-in-one command](#all-in-one) is actually a sequence of three other commands. 

- Conversion: Stateflow &rarr; HIOA
- Conversion: HIOA &rarr; HOHA
- Run: HOHA simulation

## Stateflow to HIOA conversion

This conversion is done using a `.jar` file. The all-in-one command automatically compiles the java source files in this repository. If the all-in-one command is used at least once, the file *Sim2HIOA/bin/Sim2HIOA.jar* should be already generated. If not, then open the terminal at the path *Sim2HIOA*, and execute the command:
```
./automake.sh
```
Notice that this generates the `.jar` file in *Sim2HIOA/bin/* folder.
Next, the generated jar file can convert a stateflow model into HIOA.
Go to the root path of this repository and execute the following command:
```
java -jar Sim2HIOA/bin/Sim2HIOA.jar example/bouncing_ball/bouncingBall.mdl
```
This will convert the bouncing ball model in Stateflow to HIOA. 
Notice that. it also generates a folder called *generated*.
In this folder, the `.json` file contains the HIOA model.

## HIOA to HOHA conversion

There is a python script file called `conversion.py` in the *HOHA* folder. 
```
python3 HOHA/conversion.py [destination] [HIOA_file] [order]
```
The \[destination\] is the folder that will contain the generated file, \[HIOA_file\] is the path of the HIOA model (`.json`), and \[order\] is the order of HOHA.

For example, 
```
python3 HOHA/conversion.py generated/ generated/HIOA_model_name.json 4
```
will geneate the fourth order HOHA model in the generated folder.

## Running the HOHA simulation

Notice that, once the user run the [all-in-one command](#all-in-one), there is a file generated called *run.py* in the *generated* folder. One can simply run the simulation without going through all the syntactic conversion again. 
```
python3 run.py
```
To change the simulation configuration, modify the file called *setup.json* in the *generated* folder using a text editor. 

# Modified Quantized State System (MQSS)

We also implemented Modified Quantized State System (MQSS)~\cite{} as an alternative simulation solver. This solver is based on a special data structure called *Quantize State Hybrid Input Output Automata* (QSHIOA). MQSS can be run using the all-in-one command. For example, 
```
python3 simulation.py MQSS 10 example.mdl

```

MQSS has more parameters. See *default_setup.json* file.

## HIOA to QSHIOA conversion

Similar to [HIOA to HOHA conversion](#hioa-to-hoha-conversion), conversion to QSHIOA is done using the *conversion.py* file in the *MQSS* folder. 
```
python3 MQSS/conversion.py [HIOA_file] [order]
```



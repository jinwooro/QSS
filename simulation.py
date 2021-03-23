#!/usr/bin/env python3
import json
import sys
import os
import pprint
#import tkinter
from sys import platform


def execute_HOHA(command):
    #conversion_command = ["java -jar Sim2HIOA/bin/Sim2HIOA.jar", command["input-file"]]
    #os.system(' '.join([str(s) for s in conversion_command]))
    os.system("python3 HOHA/conversion.py generated/" + command["intermediate-file"] + " " + str(command["order"])) # run the conversion script 
    if platform == "win32":
        os.system("copy HOHA/src/* generated/") # copy the src files in the MQSS folder
    else:
        os.system("cp HOHA/src/* generated/") # copy the src files in the MQSS folder

    # run the HOHA simulation
    current_path = os.getcwd()
    os.chdir(current_path + '/generated') # navigate to this folder
    os.system("python3 run.py")
    os.chdir(current_path) # get back to the original path
    print("HOHA execution")

def execute_MQSS(command):
    if command['order'] <= 1:
        print("MQSS order should be at least 2")
        sys.exit()
    # convert the symbols in the system into sympy symbols
    os.system("python3 MQSS/conversion.py generated/" + command["intermediate-file"] + " " + str(command["order"])) # run the conversion script 
    if platform == "win32":
        os.system("copy MQSS/src/* generated/") # copy the src files in the MQSS folder
    else:
        os.system("cp MQSS/src/* generated/") # copy the src files in the MQSS folder

    # run the simulation
    current_path = os.getcwd() # absolute current path
    os.chdir(current_path + '/generated') # navigate to this folder
    os.system("python3 run.py")
    os.chdir(current_path) # get back


# usage : python3 conversion.py "example.mdl"
if __name__ == "__main__":

    # extract and recognize the command
    command = {}
    # target command structure: 
    #       python3 simulation.py <MQSS/HOHA> <sim_time> [OPTIONS] <mdl_file> 
    #       [OPTIONS] = ... (TODO)
    if len(sys.argv) < 4:
        print("argument error message")
        exit()
    else:
        # load the default simulation setup    
        with open("default_setup.json") as setting_json:
            setting = json.load(setting_json)

        sys.argv.pop(0) # the first argument is useless

        if sys.argv[0] not in ["HOHA", "MQSS"]:
            print("Approach not in [MQSS, HOHA]")
            exit()
        elif not sys.argv[1].isdigit():
            print("Simulation max time is not a number")
            exit()

        command["approach"] = sys.argv.pop(0)
        command["max-time"] = float(sys.argv.pop(0))
    
        command.update(setting[ command["approach"] ])
        while (len(sys.argv)-1 > 0):
            arg = sys.argv.pop(0)
            if arg == "-n":
                command["order"] = int(sys.argv.pop(0))
            elif arg == "-t":
                command["ttol"] = float(sys.argv.pop(0))
            elif arg == "-v":
                command["vtol"] = float(sys.argv.pop(0))
            elif arg == "-x":
                command["max-step"] = float(sys.argv.pop(0))
            elif arg == "-i":
                command["iteration"] = float(sys.argv.pop(0))
            elif arg == "-d":
                command["default-step"] = float(sys.argv.pop(0))

    input_file_path = sys.argv.pop()
    # check if this target file actually exist
    if not os.path.exists(input_file_path):
        print("File '%s' does not exist" % input_file_path)
        exit()

    command["input-file"] = input_file_path

    print("User command summary:") # show the user the command summary
    #pp = pprint.PrettyPrinter(indent = 4)
    #pp.pprint(command)


    # start generating codes
    if platform == "win32":
        os.system("del -rf generated/") # delete the previously generated folder
    else:
        os.system("rm -rf generated/") # delete the previously generated folder
    
    print ("\r\nTrying to convert " + input_file_path + " file into a network of HIOA ... ")
    # check if Sim2HIOA program is built
    if not os.path.exists('Sim2HIOA/bin/Sim2HIOA.jar'):
        print("Sim2HIOA has not been compiled. Creating an executable now ... ")
        current_path = os.getcwd() # absolute current path
        os.chdir(current_path + '/Sim2HIOA') # navigate to this folder
        os.system("./automake.sh")
        os.chdir(current_path) # get back
    else:
        pass 
    
    os.system("java -jar Sim2HIOA/bin/Sim2HIOA.jar " + command["input-file"])
    # intermediate file created
    just_name = os.path.basename(command["input-file"]).split(".")[0] # get the file name without the file extension
    command["intermediate-file"] = just_name + ".json"

    # save the command in a json file    
    with open('generated/setup.json', 'w') as setupFile:
        json.dump(command, setupFile)

    if command["approach"] == "MQSS":
        execute_MQSS(command)
    elif command["approach"] == "HOHA":
        execute_HOHA(command)


    """ 
    # gui implementation in the future
    window = tkinter.Tk()

    window.title("Hybrid System Simulation")
    window.geometry("640x400+100+100")
    window.resizable(False,False)

    window.mainloop()
    """



            
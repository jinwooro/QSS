Files and Folders:
1. archive: includes unused source files that might be helpful in the future.
2. generated: the folder which will contain the output files after the conversion process
3. lib: java libraries required for converting Simulink/Stateflow into HIOA
4. pyScripts: includes python programs that converts HIOA into QSHIOA
5. src: includes the java source files
6. resource: includes the Simulink/Stateflow files (.mdl) files.
7. SimQSS.jar file: this is the runnable java file for conversion to HIOA



Step 1: Convering Simulink/Stateflow file into HIOA

java -jar SimQSS resource/collision.mdl

Where, "resource/collision.mdl" is the Simulink/Stateflow file path. This command will output a json file in the generated folder.


Step 2: Converting the generated HIOA json file into QSHIOA



Step 3: Running the QSS simulation based on the .QSHIOA file




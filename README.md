Step 1: Convering Simulink/Stateflow file into HIOA

    java -jar Simulink2HIOA resource/collision.mdl

Step 2: Converting the generated HIOA json file into QSHIOA

    python3 HIOA4QSS.py <hioa> <num>

Step 3: Running the QSS simulation based on the .QSHIOA file

    python3 qss.py <qshioa>


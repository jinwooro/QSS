To build the project, run the compile.sh file.
It will generate the class files in the  build folder.
Alternatively, run the command:

>> cd src
>> javac -d ../build -cp ".:../lib/*" SimQSS.java

To run the compiled file, execute 

>> java -cp ".:../lib/*" SimQSS <file>


To generate the executable jar file, manifest.txt file needs to be placed in the build folder. Then, inside the build folder, execute

>> jar cfvm Sim2HIOA.jar manifest.txt


This will create a jar file. Now, you can convert a mdl file to HIOA json

>> java -jar Sim2HIOA.jar <.mdl file> 


Running it will create a folder called generated, which contains the resulting json file.


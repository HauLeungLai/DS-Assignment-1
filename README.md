# Distributed Systems - Assignment 1
This repo included 4 java files and 2 test file. 
To start, make sure your terminal are in the correct file path, such as "Assignment 1" -> "src" and follow the instrtuction for compilation. 

# Compliation Instructions
Once you are in the "src" file, type "javac *.java". This will generate the corresponding .class file. 

# Running Instructions - Linux Environment 
Step 1: Start RMI Registry
Run the RMU registry in the background on port 1099:
"rmiregistry 1099 &" and leave this running  while testing. 

Step 2: Start the Server
In a new terminal, navigate to the "src" file, run:
"java CalculatorServer" - This will bind the calculator service under the name Calculator. 

Step 3: Run a Single Client
In another new terminal, navigate to the "src" file, test single-client operation, run:
"Java CalculatorClient localhost 1099 Calculator" 
Example output may look like this:
After gcd of {6,9,15}, pop() => 3
min pop => 4
max pop => 20
lcm pop => 24
delayPop(500) => 42
isEmpty? true

Step 4: Run Multiple Clients
To simulate multiple clients, such as 5 concurrent clients, run:
"java MultiClientTest localhost 1099 Calculator 5"
This test can test concurrenct and make sure methods are loading correctly. 

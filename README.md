# cis-401-groupProject

For this project you can work in team (two per team).

Design a simple application that meets the following requirements: (60 points)

Your application should have at least two separate servers programs (server1, server2) and a client program.
The client should communicate with server1 using TCP and with server2 using UDP or vice versa.
At least one of the servers should be multi-threaded
Should involve some sort of database component -- you can use a file for this part
At some point, server1 and server2 should interact with each other
For example, consider a simple shopping website with two servers.

Server1 is the main server that the client interacts with. Server1 first asks the user for credentials and verifies them by accessing its login.txt file which has registered username and password details.
For a valid user, Server1 then provides a list of items to the client. The user, through the client program, then chooses from the list. Server1 then generates a receipt and returns it to the client.
The client will then send the user's credit card details in the encrypted format to server1. Server1 forwards it to server2.
Server2 validates the credit card info. and notifies server1 of payment, and server1 confirms it to the client.
Submission: (10 points)

Report
Description of the project - summarizing the features/services of the application, similar to the example provided above.
Screenshots demonstrating the project test cases
Any issues/bugs with the code
Contributions made by each team member in design, development, and documentation phases.
2. Source code

Demonstration (30 points)

I will setup a demo time for each team-- will be during the last week of the semester.  During demo, you will need to setup your project in the SE 137 lab and demonstrate your application as a team. Expect about 15-20 minutes of demo. Both the team members should contribute in every phase of the project.
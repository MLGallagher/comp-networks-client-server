# comp-networks-client-server
This was my first HW assignment for a Computer Networks class I took while in Grad School. The goal was to create a Client-Server system where multiple clients could chat with each other via a centralized server.

# Brief description of the code
The files included are as follows: makefile, Server.java, Client.java, userpass.txt, README.md

The makefile compiles the Client and Server files.
Server.java contains the code for Server. Each time a Client tries to connect, it creates a new thread through which to communicate with that client.
Client.java contains the code for the Client. It utilizes 2 threads (one for input and the other for output) to communicate with the Server.
userpass.txt contain the usernames and passwords that were given to us for use.

# Details on development environment

I used Java 8 Development Kit and Java 8 Runtime Environment.

# Instructions on how to run thecode

1) Run the command make to compile Server.java and Client.java
2) Type "java Server 4911" or use whatever port you wish instead of 4911. The server will start.
3) Type "java Client INSERT_SERVER_IP_ADDRESS 4911" and insert the appropriate ip address. Replace 4911 with the server's port from step 2.
4) You will be prompted for a username and password.
5) Create as many clients as there are usernames (or as many as you wish).
6) Run any of the chat commands described below

# Sample commands to run code

1) make
2) java Server 4911
3) java Client 192.168.2.106 4911
4) Repeat for as many clients as you wish (or until all usernames are in use)

# Commands that the client can use once logged in

Once there are 2 or more clients logged in, they can use the following commands to find and chat with each other:
1) whoelse: displays the names of other connected users
2) message "user" "message": sends "message" to "user"
3) broadcast message "message": sends "message" to all users

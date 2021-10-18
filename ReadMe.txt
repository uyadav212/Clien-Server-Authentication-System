1. Compile and run the program in Ubuntu OS
	# For server
		1. Compile:		javac AuthServer.java
		2. Run:			java AuthServer <port_number>
						eg:- java AuthServer 9090

	# For client
		1. Compile:		javac Client.java
		2. Run:			java Client <server_address> <server_port_number>
						eg:- java Client 127.0.0.1 9090

2. To add user and password in server DB, add below entry in "UserAndPassword.txt" file
	<username> <password>
	
	NOTE: 1. username and password are space seperated
		  2. one username and password pair in one line

3. To get process id from terminal (will list all the pid of .java running files)
	ps -el | grep .java

4. To watch the thread count
	watch ps -o thcount <pid>

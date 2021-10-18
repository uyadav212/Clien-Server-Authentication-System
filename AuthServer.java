/*=============================== Authentcation server =========================================
*
*	Java code for authentication server. Below are the main features of the authentication server:
*		1. Server needs only one argument which is port number.
*		2. Server will get all the users and corresponding passwords from "UserAndPassword.txt" file.
*		3. Server will keep listening on <port_number> and will run client authentication on seperate thread after connection is established. Hence, multiple
*			client can connect with authentication server simultaneously.
*		4. Server will send messages mentioned below to client
*				a. "CorrectUsrPwdACK" message
*				b. "InvalidUsrACK" message
*				d. "IncorrectPwdACK" message
*		5. Server will close socket connection after receiving "CloseSocket" message from client.
*
*	To compile and run server
*		1. Compile:		javac AuthServer.java
*		2. Run:			java AuthServer <port_number>
*/

import java.net.*;
import java.io.*;
import java.util.*;

class RunOnNewThread implements Runnable {
	
	String threadName;
	Thread newThread;
	Socket clientSocket = null;
	DataInputStream inFromClient = null;
	DataOutputStream outToClient = null;
	AuthServer authServer;

	RunOnNewThread(String name, Socket socket, AuthServer server) {

		threadName = name;
		clientSocket = socket;
		authServer = server;
		//System.out.println("New thread created");	
		newThread = new Thread(this, threadName);
		newThread.start();
	}

	public void run() {

		try {

			String msg = "";
			String usr = "";
			String pwd = "";

			//To read inputs from the client socket
			inFromClient = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
				
			//To send outputs to the client socket
			outToClient = new DataOutputStream(clientSocket.getOutputStream());

			try	{

				msg = inFromClient.readUTF();							//read user from client 
				System.out.println(": <<<<<< \"" + msg + "\" username received\t\t\t[" + Thread.currentThread().getName() + "]");
				usr = msg;
				System.out.println(": Sending ACK to client >>>>>>\t\t\t\t[" + Thread.currentThread().getName() + "]");
				outToClient.writeUTF("ACK");

				msg = inFromClient.readUTF();							//read password from client 
				System.out.println(": <<<<<< \"" + msg + "\" password received\t\t\t[" + Thread.currentThread().getName() + "]");

				if(!authServer.usrPwdMap.containsKey(usr)) {			//verify user and password pair

					System.out.println(": \"" + usr + "\" is an INVALID username\t\t\t\t[" + Thread.currentThread().getName() + "]");
					System.out.println(": Sending ACK to client >>>>>>\t\t\t\t[" + Thread.currentThread().getName() + "]");
					outToClient.writeUTF("InvalidUsrACK");
				} else {

					pwd = authServer.usrPwdMap.get(usr);
					if(pwd.equals(msg)) {
						
						System.out.println(": " + usr + " {" + pwd + "} authenticated successfully\t\t[" + Thread.currentThread().getName() + "]");
						System.out.println(": Sending ACK to client >>>>>>\t\t\t\t[" + Thread.currentThread().getName() + "]");
						outToClient.writeUTF("CorrectUsrPwdACK");
					} else {

						System.out.println(": " + usr + " {" + msg + "} WRONG password entered\t\t\t[" + Thread.currentThread().getName() + "]");
						System.out.println(": Sending ACK to client >>>>>>\t\t\t\t[" + Thread.currentThread().getName() + "]");
						outToClient.writeUTF("IncorrectPwdACK");
					}
				}
			} catch(IOException exp) {
				System.out.println(": Exception caught => " + exp + "\t\t[" + Thread.currentThread().getName() + "]");
			}

			//Once user and password are processed, close the socket after client closes the socket
			if(!msg.equals("CloseSocket"))
				msg = inFromClient.readUTF();							//read client message to close socket
			
			if(msg.equals("CloseSocket")) {

				System.out.println(": Client closed the socket connection\t\t\t[" + Thread.currentThread().getName() + "]");
				inFromClient.close();
				outToClient.close();
				clientSocket.close();
				System.out.println(": Server closed the socket connection\t\t\t[" + Thread.currentThread().getName() + "]");
			}
		} catch(IOException exp) {
			System.out.println(": Exception caught => " + exp + "\t\t[" + Thread.currentThread().getName() + "]");
		}
	}
}

public class AuthServer extends Thread {

	ServerSocket server = null;
	int portNumber = 0;
	public HashMap<String, String> usrPwdMap = new HashMap<>();					//Map to hold user and password combination
	
	public AuthServer(int port) {
		
		this.portNumber = port;

		//Read user and password from file and fill the map
		try {
			File inputFile = new File("UserAndPassword.txt");
	        BufferedReader buffer = new BufferedReader(new FileReader(inputFile));

	        String line;
	        while ((line = buffer.readLine()) != null) {

				String[] usrPwdPair = line.split("\\s");
				usrPwdMap.put(usrPwdPair[0], usrPwdPair[1]);
	        }
	    } catch(IOException exp) {
			System.out.println(": Exception caught => " + exp);
	    }
	}

	public void run() {

		try {														//Authentication server started and listening for clients
			int clientCount = 1;
			server = new ServerSocket(portNumber);
			System.out.println(":========== Authentication Server started [" + portNumber + "] ==========");
			System.out.println(":\n: Waiting for client ...");

			while(true) {

				Socket socket = server.accept();
				System.out.println(": Client-Server connected for authentication\t\t[Client" + clientCount + "]");
				
				//once connection is established, handle further work on seperate thread to ensure multiple client request to authentication server
				new RunOnNewThread("Client" + clientCount, socket, this);
				clientCount++;
			}
		} catch(Exception exp) {
			System.out.println(": Exception caught => " + exp);
		}
	}
	
	public static void main(String args[]) {

		AuthServer authServer = new AuthServer(Integer.valueOf(args[0]));				//Authentication server with listening port: 7070
		authServer.start();
	}
}
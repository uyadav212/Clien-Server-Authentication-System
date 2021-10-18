/*=============================== Client application =========================================
*
*	Java code for client application. Below are the main features of the client application:
*		1. Client needs two argument which are mentioned below
*				a. Server address
*				b. Server port number
*		2. Client application will ask for username and after receiving ACK client application will ask for password.
*		3. If both username and password are correct, client application will echo back user input untill "exit()" is typed.
*		4. Client will send below mentioned message to server
*				a. Username entered by client
*				b. Password entered by client
*				c. "CloseSocket" message
*
*	To compile and run client application
*		1. Compile:		javac Client.java
*		2. Run:			java Client <server_address> <server_port_number>
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class Client extends Thread {

	Socket socket = null;
	String iPAddress = "";
	int portNumber = 0;
	DataInputStream clientInput = null;
	DataOutputStream outToServer = null;
	DataInputStream inFromServer = null;

	Client(String address,int port) {
		this.iPAddress = address;
		this.portNumber = port;
	}

	void closeSocket() throws Exception {

		// close socket connection
		try {
			
			outToServer.writeUTF("CloseSocket");				//send message to server

			outToServer.close();
			inFromServer.close();
			socket.close();
			System.out.println(": Socket connection is CLOSED");
		} catch(Exception exp) {
			System.out.println(": Exception caught => " + exp);
		}
	}

	public void run() {

		System.out.println(":=================== Client application started [WELCOME] ===================");
		try {
			try {
			    socket = new Socket(this.iPAddress, this.portNumber);
			} catch(Exception exp) {
				System.out.println(": Client not able to connect to server [" + this.iPAddress + "/" + this.portNumber + "]");
				System.out.println(":\n: [Exception while connecting => " + exp + "]");
				System.out.println(": Client application is closing");
				return;
			}
			    
			if(socket.isConnected()) {

				System.out.println(": Client-Server connected for authentication\t [Server: " + this.iPAddress + "/" + this.portNumber + "]");
				String msg = "";
				int authComplete = 0;										//to keep track if authentication is done or not

				//To read/take input from standard input [terminal]
				clientInput = new DataInputStream(System.in);
				
				//To read inputs from the server on socket
				inFromServer = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

				//To send outputs to server on socket
				outToServer = new DataOutputStream(socket.getOutputStream());
			
				do {						// keep reading client input untill "exit()" is encountered
						
					try	{

						if(authComplete == 0) {

							System.out.println(":\n: Enter username for authentication ");
							msg = clientInput.readLine();
							outToServer.writeUTF(msg);						//send username to server

							msg = inFromServer.readUTF();

							if(msg.equals("ACK")) {							//wait for authentication server ACK

								System.out.println(":\n: Enter password for authentication ");
								msg = clientInput.readLine();
								outToServer.writeUTF(msg);						//send password to server
								msg = inFromServer.readUTF();					//wait for authentication server response
										
								if(msg.equals("CorrectUsrPwdACK")) {				//check authentication server response

									System.out.println(": User entered password is VALID\t [Authentication SUCCESSFUL]");
									authComplete = 1;
									closeSocket();								// close socket connection
								} else if(msg.equals("InvalidUsrACK")) {
									
									System.out.println(": Username entered is INVALID\t [Authentication FAILED]");
									closeSocket();								// close socket connection
									break;
								} else if(msg.equals("IncorrectPwdACK")) {

									System.out.println(": Password entered is INVALID\t [Authentication FAILED]");
									closeSocket();								// close socket connection
									break;
								}
							} else {

								System.out.println(": Failed to receive ACK from server");
								closeSocket();									// close socket connection
								break;
							}
						}

						System.out.println(":\n: Client application will echo back user entered messages [\"exit()\": terminates client application]");
						//read other client inputs and echo back untill "exit()" is encountered
						msg = clientInput.readLine();
						System.out.println(": User entered text = > " + msg );
					} catch(Exception exp) {
						System.out.println(": Exception caught => " + exp);
						break;
					}
				}while (!msg.equals("exit()"));
				clientInput.close();
			}
			System.out.println(": Client application is closing");
		} catch(IOException exp) {
			System.out.println(": Exception caught => " + exp);
		}
	}

	public static void main(String args[]) {

		Client client = new Client(args[0], Integer.valueOf(args[1]));				//Authentication server running on localhost with listening port 7070
		client.start();
	}
}
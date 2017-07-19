import java.io.*;
import java.net.*;
import java.util.*;


public class Client {

	private static Socket socket;

	//main method that hands off to run1 and run2

	public static void main(String[] args) {
		Client client = new Client();

		//My IP address
		//192.168.2.106
		//port under use: 4911

		//create the socket connections here
		
		try {
			Integer portNum = Integer.parseInt(args[1]);
			InetAddress ipAddress = InetAddress.getByName(args[0]);

			//InetAddress serverAddress = InetAddress.getLocalHost();
			socket = new Socket(ipAddress, portNum);

			//call new Reader and Writer threads
			//start both threads
			new Reader(new BufferedReader(new InputStreamReader(socket.getInputStream()))).start();
			new Writer(new PrintWriter(socket.getOutputStream(),true)).start();
		}
		catch (Exception e) {
			System.out.println("Goodbye!");
		}

	}

	private static class Reader extends Thread {
		private BufferedReader in;

		public Reader (BufferedReader in) {
			this.in = in;
		}
		//run method here
		public void run() {
			//try a while loop that is forever true
			//constantly read lines in
			//print them out to the user in the console
			String input;

			try {
				while (true) {
					input = in.readLine();
					if (input.equals("closesocketnow")) {
						System.out.println("Press Ctrl+c to return to the command line.");
						break;
					}
					System.out.println(input);
				}
				socket.close();
				in.close();
			}
			catch (Exception e){
				System.out.println("The system has shutdown.");
			}
		}
	}

	private static class Writer extends Thread {
		private PrintWriter out;


		public Writer (PrintWriter out) {
			this.out = out;
		}

		//run method here
		public void run() {
			//try a while loop that is forever true
			//constantly wait for the user to type something
			//send it to the server
			//print it out for the user to see
			Scanner scan = new Scanner(System.in);
			String userTyped;

			try {
				while (true) {
					userTyped = scan.nextLine();
					out.println(userTyped);
				}
				
			}
			catch (Exception e) {
				try {
					out.close();
				}
				catch (Exception f) {
				}
			}
		}
	}
}
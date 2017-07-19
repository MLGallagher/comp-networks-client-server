import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.Calendar.*;

// the server that will manage the process of incoming chat clients

public class Server {

	// port that the server will listen on
	private static final int PORT = 4911;

	//this might not be the right time
	private static int BLOCK_TIME = 60;

	//time out after 30 minutes
	private static int TIME_OUT = 30;



	// create a hashtable that store the username and passwords
	private static Map<String, String> userpasses = new HashMap<String, String>();

	// create the shared hashtable
	private static Map<String, User> users = new HashMap<String, User>();

	// create the blocklist hashtable of hashtables
	private static HashMap<String, HashMap<String, Calendar>> blockList = new HashMap<String,HashMap<String, Calendar>>();

	public static void main(String[] args) throws Exception {

		//read the username and password file
		URL path = Server.class.getResource("user_pass.txt");
		System.out.println(path);
		File f = new File(path.getFile());
		BufferedReader reader = new BufferedReader(new FileReader(f));

		//add the set list of names to both hashtables
		String thisLine = null;
		while ((thisLine = reader.readLine())!= null) {
			//parse the line
			String[] userpass = thisLine.split("\\s+");

			//insert the key, value pair into the hashtable
			System.out.println(userpass[0]+", "+userpass[1]);
			userpasses.put(userpass[0], userpass[1]);
			users.put(userpass[0],null);
			HashMap<String, Calendar> holder = new HashMap<String, Calendar>();
			blockList.put(userpass[0],holder);
		}

		//get the port number from args
		Integer portNum = Integer.parseInt(args[0]);

		//create the serversocket listener
		ServerSocket listener = new ServerSocket(portNum);
		
		//start the thread that will check for inactivity
		new ActivityKeeper().start();
		
		//make your presence known
		System.out.println("The server is up!");

		//try -> finally
		try {
			while (true) {

				//check for the ipAddress in the keyset of the hashtable
				//if it doesn't exist, add it along with a reference to the User
				//if it does exist, add the thread to that User
				//which thread do you send the "Username: " prompt to?
				//send a test message??
				//use a try catch clause to swap because an exception will come
				//
				new User(listener.accept()).start();
			}
		}
		finally {
			listener.close();
		}
	}

	//This is a thread that checks for inactivity every 20 seconds
	private static class ActivityKeeper extends Thread {
		
		public void run() {
			try {
				while (true) {
					Calendar checkAwakeUsers = Calendar.getInstance();
					checkAwakeUsers.add(Calendar.MINUTE, -1*TIME_OUT);
					for (String awakeUser : users.keySet()) {
						if (users.get(awakeUser) != null && users.get(awakeUser).getLastActivityTime().before(checkAwakeUsers)) {
							//set the user's flag to false, which will trigger a closing of the socket
							users.get(awakeUser).getWriter().println("You have been inactive for too long and have been signed out. Press Ctrl+c to return to the command line.");
							users.get(awakeUser).getSocket().close();
						}
					}
					Thread.sleep(20*1000);
				}
			}
			catch (Exception e) {
				System.out.println();
			}
		}
	}

	private static class User extends Thread {
		// the user class and all its attributes
		private boolean loggedIn;
		private Calendar lastActivityTime;
		private Calendar timeSignedOn;
		private String username;
		private String password;
		private String ipAddress;

		//socket variable
		private Socket socket;
		//I/O variables
		private BufferedReader in;
		private PrintWriter out;

		//login attempts by IP and username
		//block lists by IP and username
		private Map<String, Integer> loginAttempts = new HashMap<String,Integer>();
		//private Map<String, String> blockList = new Map<String,String>();

		public User(Socket socket) {
			//test for a null value and add to it if it's null
			this.socket = socket;
		}

		public String getUsername() {
			return username;
		}

		public PrintWriter getWriter() {
			return out;
		}

		public Calendar getTimeSignedOn() {
			return timeSignedOn;
		}

		public Calendar getLastActivityTime() {
			return lastActivityTime;
		}

		public Socket getSocket() {
			return socket;
		}

		public void run() {

			//do the username password handshake
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(),true);
				ipAddress = socket.getRemoteSocketAddress().toString();
				String uname = new String();
				String pword = new String();

				boolean blocked = false;
				while (true) {
					//ask the user to input a username
					out.println("Username:");
					System.out.println(InetAddress.getLocalHost());

					uname = in.readLine();
					System.out.println(uname);

					System.out.println(blockList.get(uname));
					System.out.println(socket==null);

					if (uname == null) {
						System.out.println("1 CHECK IT YOOO");
						out.println("You entered a null value. Please enter a username next time!");
						blocked = true;
						break;
					}
					//check if the username is valid
					else if (!userpasses.containsKey(uname)) {
						System.out.println("2 CHECK IT YOOO");
						out.println("We don't recognize that username. Please enter another!");
						blocked = true;
					}
					//check if someone is signed in; they're signed in if they're 
					//in the users hashtable with a non-null value
					else if (users.get(uname) != null) {
						System.out.println("3 CHECK IT YOOO");
						out.println("That user is already signed in. Please enter another username!");
						blocked = true;
					}
					//check if user is on the blocklist and whether they need to remain there
					else if (blockList.get(uname).containsKey(ipAddress)) {
						System.out.println("4 CHECK IT YOOO");
							//it does exist, then check if they need to wait
							//if they do, tell them to wait for X seconds
							//if they don't, then do a standard login
						if (blockList.get(uname).get(ipAddress).before(Calendar.getInstance())) {
							//remove the key value pair from blocked list
							blockList.get(uname).remove(ipAddress);
							blocked = false;
						}
						else {
							blocked = true;
							out.println("You are on the blocklist for this username. Try another!");
							out.println("Username:");
						}
					}

					if (blocked == false) {
						//prompt for password
						out.println("Password:");
						pword = in.readLine();
						//check for passwords
						System.out.println("This is the password stored in hash: " +userpasses.get(uname));
						System.out.println("This the user inputted password: " +pword);


						if (pword.equals(userpasses.get(uname))) {
							username = uname;
							password = pword;
							users.put(uname, this);
							out.println("Congrats, you are logged in as: "+uname);
							timeSignedOn = Calendar.getInstance();
							break;
						}
						//username and password don't match
						else {
							





							//redo blocklist




							Integer loginAttemptsCount = loginAttempts.get(uname);
							loginAttempts.put(uname, loginAttemptsCount != null ? loginAttemptsCount + 1 : 1);
							out.println("The username and password are incorrect.");
							if (loginAttempts.get(uname) >= 3) {
								//create new calendar date
								Calendar blockTimeAdd = Calendar.getInstance();
								blockTimeAdd.add(Calendar.SECOND, BLOCK_TIME);
								//Add to blockList
								blockList.get(uname).put(ipAddress, blockTimeAdd);
								out.println("That was 3 login attempts with the same username. Goodbye!");
								out.println("closesocketnow");
								socket.close();
							}
							else {
								out.println("Please try again.");
							}
						}
					}
					//set blocked to false so that we can check again if they need it
					blocked = false;
					System.out.println("end of loop");
				}
				System.out.println("You got through the username and password handshake!");
				lastActivityTime = Calendar.getInstance();


				String messageInput;
				//create a loop to manage messages
				while (true) {
				//Implement the following
				//	whoelse
					messageInput = in.readLine();
					lastActivityTime = Calendar.getInstance();

					ArrayList<String> onlineUsers = new ArrayList<String>();
					if (messageInput.equals("whoelse")) {
						for (String str : users.keySet()) {
							if (users.get(str) != null) {
								onlineUsers.add(str);
							}
						}
						//check for null list; send appropriate response in either case
						if (onlineUsers.size() == 1) {
							out.println("There is no one else online.");
						}
						else {
							out.println("The following users are online: ");
							//unles
							for (String strg : onlineUsers) {
								if (!strg.equals(username)){
									out.println("  "+strg);
								}
							}
						}
					}
					//	logout
					else if (messageInput.equals("logout")) {
						out.println("closesocketnow");
						break;
					}

				
				//	wholast
					else if (messageInput.startsWith("wholast ")) {
						
						//parse the input and get the number minutes
						String[] whoLastHolder = messageInput.split(" ");
						Integer whoLastMinutes;
						String whoLastOutPut;
						try {
							whoLastMinutes = Integer.parseInt(whoLastHolder[1]);
						}
						catch (Exception z) {
							whoLastMinutes = -1;
						}
						
						if (whoLastHolder.length > 2)
							out.println("That input was not valid. Please try again.");
						else if (whoLastMinutes < 0) {
							out.println("Please input a number between 0 and 60 for the number of minutes.");
						}
						else {
							
							Calendar currTime = Calendar.getInstance();
							currTime.add(Calendar.MINUTE,-1*whoLastMinutes);
							//subtract current time from connection time and compare to whoLastMinutes integer
							//need a loop here

							for (String stg : users.keySet()) {
								//System.out.println(users.get(stg).getTimeSignedOn());

								if (users.get(stg) != null) {
									//System.out.println(currTime.getTime());
									//System.out.println(users.get(stg).getTimeSignedOn().getTime());

									if (currTime.before(users.get(stg).getTimeSignedOn())) {
										out.println("  "+users.get(stg).getUsername());
									}
								}
							}
						}
					}

				// broadcast to all users that are online
					else if (messageInput.startsWith("broadcast message ")) {
						//store the message in a string
						String writerHolder = messageInput.replaceFirst("broadcast message ","");

						//send the message to everyone who is online and is not the current user
						for (String strr : users.keySet()) {
							if (users.get(strr) != null && users.get(strr).getUsername() != username) {
								users.get(strr).getWriter().println(username+": "+ writerHolder);

							}
						}
					}
				// broadcast to specific users
					else if (messageInput.startsWith("broadcast user ")) {
						Pattern pattern = Pattern.compile("(broadcast user )(.*)( message )(.*)");
						Matcher matcher = pattern.matcher(messageInput);
						matcher.matches();
						
						String[] userToMessage = matcher.group(2).split(" ");
						for (String strrr : users.keySet()) {
							if(users.get(strrr) != null && Arrays.asList(userToMessage).contains(strrr)) {
								users.get(strrr).getWriter().println("  "+username+": "+ matcher.group(4));
							}
						}
					}
				//	private message
					else if (messageInput.startsWith("message ")) {
						//replace with
						//separate first word until space, second word until space, the rest
						Pattern pattern2 = Pattern.compile("(message )(.*?)(\\s)(.*)");
						Matcher matcher2 = pattern2.matcher(messageInput);
						matcher2.matches();

						if (users.get(matcher2.group(2)) != null) {
							users.get(matcher2.group(2)).getWriter().println(username+": "+matcher2.group(4));
						}
					}
				







				//manage the sign out process; analyze each parent hashtable and data structure for what needs to happen on logout
				//logout
					//logout message is sent
					//user shuts down connection with ctrl+c
					//other???
				//blocklist
				//make file







				//	graceful exit of client and server programs using control+c
					else {
						out.println("We don't recognize that message. Please try again.");
					}
				}
			}
			catch (IOException e){
				System.out.println(e);
			}
			finally{
				users.put(username, null);
				try {
					socket.close();
					System.out.println("The connection has closed.");
				}
				catch (IOException e) {
					System.out.println(e);
					System.out.println("The system has closed.");
				}
			}
		}
	}
}
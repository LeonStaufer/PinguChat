package pgdp.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatServer {
	private ServerSocket server;
	static List<Connection> connections = new ArrayList<>();
	static HashMap<String, Connection> userConnection = new HashMap<>();
	private boolean running;

	public ChatServer() {
		this("3000");
	}

	/**
	 * standard constructor that checks validity of parameters
	 *
	 * @param portNumber of server
	 */
	public ChatServer(String portNumber) {
		if (portNumber.isBlank())
			throw new IllegalArgumentException("arguments cannot be blank");
		int port;
		try {
			port = Integer.parseInt(portNumber);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("port number invalid");
		}

		try {
			this.server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.running = true;
		this.run();
	}

	/**
	 * helper method to start the server
	 */
	private void run() {
		System.out.println("Server started");
		try {
			while (running) {
				//accept all connections and create a thread for them
				Connection connection = new Connection(this.server.accept());
				connection.start();
				connections.add(connection);

				//limit number of connections to 50
				running = connections.size() < 50;
			}

			//close server
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * start server
	 *
	 * @param args port
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			new ChatServer();
		} else {
			new ChatServer(args[0]);
		}
	}
}

class Connection extends Thread {
	//SimpleDateFormat to make the date look nice
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy HH:mm");
	private Socket socket;
	private String username;
	private Date connectedSince;

	public Connection(Socket socket) {
		this.socket = socket;
		this.connectedSince = new Date();
	}

	public String getUsername() {
		return username;
	}

	public Date getConnectedSince() {
		return connectedSince;
	}

	/**
	 * send a message to the socket
	 *
	 * @param msg to be sent
	 */
	private void send(String msg) {
		try {
			PrintWriter output = new PrintWriter(this.socket.getOutputStream());
			output.println(msg);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * helper function that sends msg to everyone but oneself
	 *
	 * @param msg to be sent
	 */
	private void sendToOthers(String msg) {
		for (Connection connection : ChatServer.connections) {
			if (connection.getUsername() == null || getUsername() == null) continue;
			if (!connection.getUsername().equals(getUsername())) {
				connection.send(msg);
			}
		}
	}

	/**
	 * string formatter for regular messages
	 *
	 * @param user that sent the message
	 * @param msg
	 * @return formatted string
	 */
	private String messageFormat(String user, String msg) {
		return String.format("%s%s%s❯ %s", ConsoleColors.YELLOW_BOLD_BRIGHT, username, ConsoleColors.RESET, msg);
	}

	/**
	 * string formatter for private messages
	 *
	 * @param user
	 * @param msg
	 * @return formatted string
	 */
	private String privateMessageFormat(String user, String msg) {
		return String.format("%s%s%s❯❯ %s", ConsoleColors.YELLOW_BOLD_BRIGHT, user, ConsoleColors.RESET, msg);
	}

	/**
	 * string formatter for information messages
	 *
	 * @param msg
	 * @return formatted string
	 */
	private String infoFormat(String msg) {
		return String.format("%s%s%s", ConsoleColors.WHITE_BOLD, msg, ConsoleColors.RESET);
	}

	/**
	 * string formatter for error messages
	 *
	 * @param msg
	 * @return formatted string
	 */
	private String errorFormat(String msg) {
		return String.format("%s⚠ ERROR: %s%s", ConsoleColors.RED_BOLD_BRIGHT, msg, ConsoleColors.RESET);
	}

	/**
	 * string formatter for penguin facts
	 *
	 * @param fact
	 * @return formatted string
	 */
	private String pinguFormat(String fact) {
		return String.format("%s%nDid you know: %s%n%n%s", ConsoleColors.BLUE_BACKGROUND, fact, ConsoleColors.RESET);
	}

	/**
	 * check if username is valid
	 * cannot be blank, contain spaces, or already be in use
	 *
	 * @param input username
	 * @return invalid username
	 */
	private boolean inputInvalid(String input) {
		return input.isBlank() || input.contains(" ") || ChatServer.userConnection.containsKey(input);
	}

	/**
	 * run method of thread
	 */
	@Override
	public void run() {
		System.out.printf("Instance started | %s | on %s %n", socket.getInetAddress(), dateFormat.format(new Date()));
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			//welcome procedure consists of getting username and welcoming user
			String username;
			do {
				//request username from client
				send("ENTER_USERNAME");
				username = in.readLine();
			} while (inputInvalid(username));

			//save username if it is valid
			this.username = username;
			ChatServer.userConnection.put(username, this);
			send("VALID");

			//welcome user
			send(infoFormat(String.format("Welcome %s!", username)));
			sendToOthers(infoFormat(String.format("%s joined the chat!", username)));

			String input;
			while ((input = in.readLine()) != null) {
				if (input.indexOf("@") == 0) {
					//find the index where the message begins
					int separation = input.indexOf(" ");
					try {
						//get the username
						String user = input.substring(1, separation);
						if (!ChatServer.userConnection.containsKey(user)) {
							send(errorFormat(String.format("%s could not be found!", user)));
							continue;
						}
						//send a message to that user
						ChatServer.userConnection.get(user).send(privateMessageFormat(username, input.substring(separation + 1)));
					} catch (StringIndexOutOfBoundsException exc) {
						send(errorFormat("Must supply a message"));
					}
				} else if (input.equals("WHOIS")) {
					//create a list of connected users and send it to yourself
					StringBuilder builder = new StringBuilder();
					for (Connection connection : ChatServer.connections) {
						builder.append(String.format("- %s connected since %s%n", connection.getUsername(), dateFormat.format(connection.getConnectedSince())));
					}
					send(builder.toString());
				} else if (input.equals("LOGOUT")) {
					//say goodbye to everyone and then close the socket
					send(infoFormat(String.format("Goodbye %s!", username)));
					socket.close();
				} else if (input.equals("PENGU")) {
					//send some great penguin facts
					send(pinguFormat(PinguinFacts.getRandomFact()));
				} else {
					//send a message to everyone except yourself
					sendToOthers(messageFormat(username, input));
				}
			}

			//close the connection
			in.close();
			socket.close();
		} catch (SocketException e) {
			//do nothing :)
			//because I already handle the exception by removing users
		} catch (IOException e) {
			e.printStackTrace();
		}
		//remove users
		ChatServer.connections.remove(this);
		ChatServer.userConnection.remove(username);

		//inform other users
		sendToOthers(infoFormat(String.format("%s has left the chat!", username)));
	}
}
    

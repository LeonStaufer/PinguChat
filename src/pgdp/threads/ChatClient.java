package pgdp.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient {
	Socket socket;
	String address;
	int portNumber;
	boolean running;

	public ChatClient() {
		this("localhost", "3000");
	}

	/**
	 * standard constructor that checks validity of parameters
	 *
	 * @param address    of server
	 * @param portNumber of server
	 */
	public ChatClient(String address, String portNumber) {
		if (address.isBlank() || portNumber.isBlank())
			throw new IllegalArgumentException("arguments cannot be blank");

		this.address = address;
		try {
			this.portNumber = Integer.parseInt(portNumber);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("port number invalid");
		}

		try {
			this.socket = new Socket(InetAddress.getByName(address), this.portNumber);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("invalid host");
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.running = true;
	}

	/**
	 * sends a message to the server
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
	 * helper method that starts the client
	 */
	private void run() {
		try {
			Scanner scanner = new Scanner(System.in);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// request valid username and send it to server until it is correct
			String response = in.readLine();
			// request username as long as the server requires it
			while (response == null || response.equals("ENTER_USERNAME")) {
				System.out.print("Please input a username: ");
				String input = scanner.nextLine().strip();
				//send username to the server
				send(input);
				response = in.readLine();
			}
		} catch (IOException exc) {
			exc.printStackTrace();
		}

		// start separate threads to allow for messages to be written and sent at the same time
		new WriteThread(socket).start();
		new TypeThread(socket).start();
	}

	/**
	 * start client
	 *
	 * @param args server address, port
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			new ChatClient().run();
		} else {
			new ChatClient(args[0], args[1]).run();
		}
	}
}

class TypeThread extends Thread {
	private Socket socket;

	public TypeThread(Socket socket) {
		this.socket = socket;
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

	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);

		// handle normal communication
		String input;
		while ((input = scanner.nextLine()) != null) {
			//ignore blank input
			if (input.isBlank()) continue;

			//send trimmed input
			send(input.trim());

			//end thread if the user logs out
			if (input.equals("LOGOUT")) return;
		}
	}
}

class WriteThread extends Thread {
	private Socket socket;

	public WriteThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// handle normal communication
			String input;
			while ((input = in.readLine()) != null) {
				System.out.println(input);
			}

			//close socket
			in.close();
			socket.close();
		} catch (SocketException e) {
			// handled by the logout method
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
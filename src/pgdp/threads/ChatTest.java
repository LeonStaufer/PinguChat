package pgdp.threads;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

class ChatTest {
	@BeforeAll
	static void setUpServer() {
		new Thread(ChatServer::new).start();
	}

	@Test
	@DisplayName("Setup with weird inputs")
	void setUpWeirdInputs() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ChatServer("    "));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ChatServer("invalid"));
	}

	@Test
	@DisplayName("setup via main method")
	void setUpViaMain() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> ChatServer.main(new String[]{"   "}));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ChatServer.main(new String[]{"invalid"}));

		Assertions.assertThrows(IllegalArgumentException.class, () -> ChatClient.main(new String[]{"localhost", "  "}));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ChatClient.main(new String[]{"localhost", "invalid"}));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ChatClient.main(new String[]{"not$valid", "123"}));
	}

	@Test
	@DisplayName("Invalid username inputs")
	void invalidUsername() throws IOException {
		//setup first client
		ChatClient c1 = new ChatClient();
		PrintWriter out1 = new PrintWriter(c1.socket.getOutputStream(), true);
		BufferedReader in1 = new BufferedReader(new InputStreamReader(c1.socket.getInputStream()));

		//setup second client
		ChatClient c2 = new ChatClient();
		PrintWriter out2 = new PrintWriter(c2.socket.getOutputStream(), true);
		BufferedReader in2 = new BufferedReader(new InputStreamReader(c2.socket.getInputStream()));

		//test invalid inputs for the first client
		in1.readLine();
		out1.println("te st");
		Assertions.assertEquals("ENTER_USERNAME", in1.readLine(), "Cannot contain spaces");
		out1.println("    ");
		Assertions.assertEquals("ENTER_USERNAME", in1.readLine(), "Cannot be blank");
		out1.println("username");
		Assertions.assertEquals("VALID", in1.readLine(), "Valid username was rejected");

		//check the second client
		in2.readLine();
		out2.println("username");
		Assertions.assertEquals("ENTER_USERNAME", in2.readLine(), "Should not be able to enter same name twice");
		out2.println("username2");
		Assertions.assertEquals("VALID", in2.readLine(), "Valid username was rejected");

		// close sockets
		c1.socket.close();
		c2.socket.close();
	}

	@Test
	@DisplayName("Full conversation between two clients")
	void normalConversation() throws IOException {
		//setup first client
		ChatClient c1 = new ChatClient();
		PrintWriter out1 = new PrintWriter(c1.socket.getOutputStream(), true);
		BufferedReader in1 = new BufferedReader(new InputStreamReader(c1.socket.getInputStream()));

		//setup second client
		ChatClient c2 = new ChatClient();
		PrintWriter out2 = new PrintWriter(c2.socket.getOutputStream(), true);
		BufferedReader in2 = new BufferedReader(new InputStreamReader(c2.socket.getInputStream()));

		//test invalid inputs for the first client
		in1.readLine();
		out1.println("username");
		in1.readLine();
		String result = in1.readLine();
		Assertions.assertTrue(result.contains("Welcome username!"), "User was not welcomed\nResult: " + result);

		//check the second client
		in2.readLine();
		out2.println("username2");
		in2.readLine();
		in2.readLine();

		result = in1.readLine();
		Assertions.assertTrue(result.contains("username2 joined the chat!"), "Other user was not welcomed\nResult: " + result);

		out1.println("hello");
		result = in2.readLine();
		Assertions.assertTrue(result.contains("username") && result.contains("hello"), "Message was not sent properly \nResult: " + result);

		out2.println("@username secret");
		result = in1.readLine();
		Assertions.assertTrue(result.contains("username2") && result.contains("secret"), "Private message not sent properly\nResult: " + result);

		out1.println("LOGOUT");
		result = in1.readLine();
		Assertions.assertTrue(result.contains("Goodbye username!"), "User was not greeted when leaving\nResult: " + result);
		result = in2.readLine();
		Assertions.assertTrue(result.contains("username has left"), "Other users were not informed of the user leaving\nResult: " + result);

		// close sockets
		c1.socket.close();
		c2.socket.close();
	}

	@Test
	@DisplayName("PENGU facts")
	void penguFacts() throws IOException {
		//setup first client
		ChatClient c1 = new ChatClient();
		PrintWriter out1 = new PrintWriter(c1.socket.getOutputStream(), true);
		BufferedReader in1 = new BufferedReader(new InputStreamReader(c1.socket.getInputStream()));

		//test if PENGU facts are returned
		in1.readLine();
		out1.println("username");
		in1.readLine();
		in1.readLine();
		out1.println("PENGU");
		in1.readLine();
		Assertions.assertTrue(in1.readLine().contains("Did you know:"));

		//close socket
		c1.socket.close();
	}

	@Test
	@DisplayName("WHOIS")
	void whois() throws IOException {
		//setup first client
		ChatClient c1 = new ChatClient();
		PrintWriter out1 = new PrintWriter(c1.socket.getOutputStream(), true);
		BufferedReader in1 = new BufferedReader(new InputStreamReader(c1.socket.getInputStream()));

		//test if WHOIS is returned correctly
		in1.readLine();
		out1.println("username");
		in1.readLine();
		in1.readLine();
		out1.println("WHOIS");
		String result = in1.readLine();
		Assertions.assertTrue(result.contains("username"));

		//close socket
		c1.socket.close();
	}

	@Test
	@DisplayName("Private messages")
	void privateMessages() throws IOException {
		//setup first client
		ChatClient c1 = new ChatClient();
		PrintWriter out1 = new PrintWriter(c1.socket.getOutputStream(), true);
		BufferedReader in1 = new BufferedReader(new InputStreamReader(c1.socket.getInputStream()));

		//test if private messages can be sent
		in1.readLine();
		out1.println("username");
		in1.readLine();
		in1.readLine();
		out1.println("@username secret");
		String result = in1.readLine();
		Assertions.assertTrue(result.contains("username") && result.contains("secret"), "Private message not sent properly\nResult: " + result);

		//test if invalid inputs fail for non existent user
		out1.println("@doesnotexist secret");
		result = in1.readLine();
		Assertions.assertTrue(result.contains("doesnotexist could not be found"), "Should fail when sending to non existent user\nResult: " + result);

		//test if invalid inputs fail for empty message
		out1.println("@username");
		result = in1.readLine();
		Assertions.assertTrue(result.contains("Must supply a message"), "Should fail when sending empty messages\nResult: " + result);

		//close socket
		c1.socket.close();
	}
}
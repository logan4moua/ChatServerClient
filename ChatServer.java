/**
 * @author Surin Assawajaroenkoon
 * ChatServer class
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
	int serverPort; // port which this server are listening
	ServerSocket sSocket; // Server socket holding server port and Internet address
	Socket cSocket; // a client socket
	

	
	private ArrayList<AcceptedClient> clients; // collection of clients
	
	public ChatServer(int port){
		serverPort = port;
		clients = new ArrayList<AcceptedClient>();
		System.out.println("Connecting to port "+ serverPort);
		try {
			sSocket = new ServerSocket(serverPort);
		} catch (IOException e) {
			
			System.out.println("Socket cannot bind to port"+ serverPort);
			System.out.println(e.getMessage());
		}
		System.out.println("Successfully bind to port "+sSocket.getLocalPort());
		System.out.println("Chat server started...");		
		
		Runnable r = new Runnable(){
			public void run(){
				startServer();
			}
		};
		
		new Thread(r).start();
		
	}
	/**
	 * start a server and wait for client(s) to connect
	 */
	private void startServer(){

		while(true){
			System.out.println("Waiting for client to connect...");
			try {
				cSocket = sSocket.accept();
				
			} catch (IOException e) {
				System.out.println("Failed to accept a client");
				System.out.println(e.getMessage());
			}
			
			System.out.println("Connection successful: "+ cSocket.getPort());
			addNewClient(cSocket);
		}
	}
	/**
	 * add a new client to clients collection list
	 * @param socket
	 */
	private void addNewClient(Socket socket){
		AcceptedClient ac = new AcceptedClient(cSocket, this);
		clients.add(ac);
	}
	
	/**
	 * sending out messages to each clients in this chat server
	 * @param ac
	 * @param msg
	 */
	public synchronized void handleChat(AcceptedClient ac, String msg ){
		if(msg.compareTo("EXIT")==0){
			clients.forEach(c -> c.send(ac.getAcName()+": --Client Exiting Chat--"));
			remove(ac);
		}
		else{
			// use for each only with java 8 and above
			clients.forEach(c -> c.send(ac.getAcName()+": "+msg));
			
		}
	}
	/**
	 * remove a client from chat server: close all in and out streams and remove the clients
	 * @param ac
	 */
	private void remove(AcceptedClient ac){
		ac.close();
		clients.remove(ac);
		
	}

	

	
	/// inner class start here
	/**
	 * @author Surin Assawajaroenkoon
	 * AcceptedClient class
	 */


	public class AcceptedClient {
		
		private Thread acThread; // An internal thread to start a client session
		private ChatServer server; // The server this client is on
		private Socket acSocket;	// The socket this client is using to connect to server,
									// not the one the server is listening to
		
		private BufferedReader reader; // to read incoming streams from client side input
		private PrintWriter writer; // to send out outgoing streams to clients
		
		private int ID;
		private String acName;
		
		public AcceptedClient(Socket socket, ChatServer server){
			acSocket = socket;
			this.server = server;
			setID(socket.getPort());
			setAcName("Annonymous");
			
			Runnable r = new Runnable(){
				public void run(){
					startSession();
				}
			};
			
			acThread = new Thread(r);
			acThread.start();
		}
		/**
		 * Allows chat session to start
		 */
		private void startSession(){
			
			openChannel();
			String line = new String(); // represents a line reading from client
			try {
				
				acName = reader.readLine();
				server.handleChat(this, "Hi, everyone. My name is "+ acName);
				
			} catch (IOException e1) {
				System.out.println("IO exception: "+e1.getMessage());
			}
			
			try {
				while((line = reader.readLine()) != null){
					
					System.out.println(acName+": "+ line); // print on server console
					server.handleChat(this, line); // server sent message to each clients
					
				}
			} catch (IOException e) {
				System.out.println("Client exited: "+ e.getMessage());
			}
		}
		
		/**
		 * Open reader for incoming streams and writer for out going streams
		 */
		private void openChannel(){
			try {
				reader = new BufferedReader(new InputStreamReader(acSocket.getInputStream()));
				writer = new PrintWriter(acSocket.getOutputStream(), true);
			} catch (IOException e) {
				System.out.println("Failed to open reader or writer");
				System.out.println(e.getMessage());
			}
		}
		/**
		 * to send a message out to client
		 * @param msg
		 */
		protected void send(String msg){
			writer.println(msg);
		}
		
		/**
		 * Close reader incoming streams, writer outgoing streams, the socket, and the thread
		 */
		@SuppressWarnings("deprecation")
		protected void close(){
			try {
				reader.close();
				acSocket.close();
			} catch (IOException e) {
				System.out.println("Failed to close! "+e.getMessage());
			}
			writer.close();
			acThread.stop();
			
		}

		public String getAcName() {
			return acName;
		}

		public void setAcName(String acName) {
			this.acName = acName;
		}

		public int getID() {
			return ID;
		}

		protected void setID(int iD) {
			ID = iD;
		}

	}

	
}


